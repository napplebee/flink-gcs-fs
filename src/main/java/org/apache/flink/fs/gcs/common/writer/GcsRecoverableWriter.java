/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.fs.gcs.common.writer;

import com.google.cloud.Restorable;
import com.google.cloud.RestorableState;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.apache.flink.annotation.PublicEvolving;
import org.apache.flink.core.fs.Path;
import org.apache.flink.core.fs.RecoverableFsDataOutputStream;
import org.apache.flink.core.fs.RecoverableWriter;
import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * An implementation of the {@link RecoverableWriter} against GCS.
 */
@PublicEvolving
public class GcsRecoverableWriter implements RecoverableWriter {
	private static final Logger LOG = LoggerFactory.getLogger(GcsRecoverableWriter.class);

	public GcsRecoverableWriter() {
		LOG.debug("Constructor: Creating GcsRecoverableWriter");
	}

	public static GcsRecoverable castToGcsRecoverable(CommitRecoverable recoverable) {
		LOG.debug("Casting CommitRecoverable to GcsRecoverable");
		if (recoverable instanceof GcsRecoverable) {
			return (GcsRecoverable) recoverable;
		}
		throw new IllegalArgumentException(
			"GCS File System cannot recover recoverable for other file system: " + recoverable);
	}

	public RestorableState<WriteChannel> captureState(BlobInfo blobInfo) {
		LOG.info("captureState(): blobInfo={}", blobInfo);
		try {
			Storage storage = StorageOptions.getDefaultInstance().getService();
			return storage.writer(blobInfo).capture();
		} catch (Exception ex) {
			LOG.error("Could not capture state", ex);
			throw ex;
		}
	}

	public static String removeRootSlash(String name) {
		if(name.startsWith("/")) {
			return name.substring(1, name.length());
		} else {
			return name;
		}
	}

	/**
	 * Opens a new recoverable stream to write to the given path.
	 * Whether existing files will be overwritten is implementation specific and should
	 * not be relied upon.
	 *
	 * @param path The path of the file/object to write to.
	 * @return A new RecoverableFsDataOutputStream writing a new file/object.
	 * @throws IOException Thrown if the stream could not be opened/initialized.
	 */
	@Override
	public RecoverableFsDataOutputStream open(Path path) throws IOException {
		String name = removeRootSlash(path.toUri().getPath());
		String bucket = path.toUri().getHost();
		LOG.info("open(): path={}, name={}, bucket={}", path, name, bucket);
		BlobId blobId = BlobId.of(bucket, name);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
		return new GcsRecoverableFsDataOutputStream(
				new GcsRecoverable(blobInfo, captureState(blobInfo), 0L)
		);
	}

	/**
	 * Resumes a recoverable stream consistently at the point indicated by the given ResumeRecoverable.
	 * Future writes to the stream will continue / append the file as of that point.
	 *
	 * <p>This method is optional and whether it is supported is indicated through the
	 * {@link #supportsResume()} method.
	 *
	 * @param resumable The opaque handle with the recovery information.
	 * @return A recoverable stream writing to the file/object as it was at the point when the
	 * ResumeRecoverable was created.
	 * @throws IOException                   Thrown, if resuming fails.
	 * @throws UnsupportedOperationException Thrown if this optional method is not supported.
	 */
	@Override
	public RecoverableFsDataOutputStream recover(ResumeRecoverable resumable) throws IOException {
		LOG.info("recover(): returning RecoverableFsDataOutputStream with resumable={}", resumable);
		return new GcsRecoverableFsDataOutputStream(castToGcsRecoverable(resumable));
	}

	@Override
	public boolean requiresCleanupOfRecoverableState() {
		return false;
	}

	@Override
	public boolean cleanupRecoverableState(ResumeRecoverable resumable) throws IOException {
		return false;
	}

	/**
	 * Recovers a recoverable stream consistently at the point indicated by the given CommitRecoverable
	 * for finalizing and committing. This will publish the target file with exactly the data
	 * that was written up to the point then the CommitRecoverable was created.
	 *
	 * @param recoverable The opaque handle with the recovery information.
	 * @return A committer that publishes the target file.
	 * @throws IOException Thrown, if recovery fails.
	 */
	@Override
	public RecoverableFsDataOutputStream.Committer recoverForCommit(CommitRecoverable recoverable) throws IOException {
		LOG.info("recoverForCommit(): return a committer for recoverable={}", recoverable);
		final GcsRecoverable gcsRecoverable = castToGcsRecoverable(recoverable);
		final RecoverableFsDataOutputStream recovered = recover(gcsRecoverable);
		return recovered.closeForCommit();
	}

	/**
	 * The serializer for the CommitRecoverable types created in this writer.
	 * This serializer should be used to store the CommitRecoverable in checkpoint
	 * state or other forms of persistent state.
	 */
	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public SimpleVersionedSerializer<CommitRecoverable> getCommitRecoverableSerializer() {
		LOG.info("getCommitRecoverableSerializer()");
		return (SimpleVersionedSerializer) GcsRecoverableSerializer.INSTANCE;
	}

	/**
	 * The serializer for the ResumeRecoverable types created in this writer.
	 * This serializer should be used to store the ResumeRecoverable in checkpoint
	 * state or other forms of persistent state.
	 */
	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public SimpleVersionedSerializer<ResumeRecoverable> getResumeRecoverableSerializer() {
		LOG.info("getResumeRecoverableSerializer()");
		return (SimpleVersionedSerializer) GcsRecoverableSerializer.INSTANCE;
	}

	// --------------------------- Utils ---------------------------

	/**
	 * Checks whether the writer and its streams support resuming (appending to) files after
	 * recovery (via the {@link #recover(ResumeRecoverable)} method).
	 *
	 * <p>If true, then this writer supports the {@link #recover(ResumeRecoverable)} method.
	 * If false, then that method may not be supported and streams can only be recovered via
	 * {@link #recoverForCommit(CommitRecoverable)}.
	 */
	@Override
	public boolean supportsResume() {
		return true;
	}
}
