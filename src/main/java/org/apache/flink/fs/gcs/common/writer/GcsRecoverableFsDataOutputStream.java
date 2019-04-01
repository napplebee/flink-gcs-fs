/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE gcsRecoverable
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this gcsRecoverable
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this gcsRecoverable except in compliance
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

import com.google.cloud.WriteChannel;
import org.apache.flink.annotation.PublicEvolving;
import org.apache.flink.core.fs.RecoverableFsDataOutputStream;
import org.apache.flink.core.fs.RecoverableWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A RecoverableFsDataOutputStream to GCS that is based on a Resumable upload:
 * https://cloud.google.com/storage/docs/json_api/v1/how-tos/resumable-upload
 *
 * <p>This class is NOT thread-safe. Concurrent writes tho this stream result in corrupt or
 * lost data.
 *
 * <p>The {@link #close()} method may be called concurrently when cancelling / shutting down.
 * It will still ensure that local transient resources (like streams and temp files) are cleaned up,
 * but will not touch data previously persisted in GCS.
 */
@PublicEvolving
@NotThreadSafe
public final class GcsRecoverableFsDataOutputStream extends RecoverableFsDataOutputStream {
	private static final Logger LOG = LoggerFactory.getLogger(GcsRecoverableFsDataOutputStream.class);

	/**
	 * Previous GCSRecoverable
	 */
	private GcsRecoverable recoverable;
	/**
	 * Write channel
	 */
	private WriteChannel channel;
	/**
	 * Number of bytes written to GCS in this session
	 */
	private long written = 0L;

	GcsRecoverableFsDataOutputStream(GcsRecoverable gcsRecoverable) throws IOException {
		LOG.debug("Constructor: Creating GcsRecoverableFsDataOutputStream for recoverable={}", gcsRecoverable);
		this.recoverable = gcsRecoverable;
		this.channel = gcsRecoverable.getState().restore();
	}

	@Override
	public void write(int b) throws IOException {
	    final int FOUR_BYTES = 4;
		LOG.debug("Write integer");
		channel.write(ByteBuffer.allocate(FOUR_BYTES).putInt(b));
		written += FOUR_BYTES; // four bytes for an integer
	}

	@Override
	public void write(byte[] data, int off, int len) throws IOException {
		LOG.debug("Write bytes, offset={}, length={}, data.length={}", off, len, data.length);
		written += len;
		channel.write(ByteBuffer.wrap(data, off, len));
	}

	@Override
	public void flush() throws IOException {
		LOG.debug("flush(): NOOP");
	}

	@Override
	public long getPos() throws IOException {
	    final long pos = recoverable.getTotalBytesWritten() + written;
	    LOG.debug("getPos(): pos={}", pos);
		return pos;
	}

	@Override
	public void sync() throws IOException {
		LOG.debug("sync(): NOOP");
	}

	/**
	 * Closes this stream. Closing the steam releases the local resources that the stream
	 * uses, but does NOT result in durability of previously written data. This method
	 * should be interpreted as a "close in order to dispose" or "close on failure".
	 *
	 * <p>In order to persist all previously written data, one needs to call the
	 * {@link #closeForCommit()} method and call {@link Committer#commit()} on the retured
	 * committer object.
	 *
	 * @throws IOException Thrown if an error occurred during closing.
	 */
	@Override
	public void close() throws IOException {
		LOG.debug("close(): NOOP");
	}

    /**
     * Ensures all data so far is persistent and returns
     * a handle to recover the stream at the current position.
     */
    @Override
	public RecoverableWriter.ResumeRecoverable persist() throws IOException {
		LOG.debug("persist(): returning a new ResumeRecoverable");
		return recoverable.copy(channel.capture(), recoverable.getTotalBytesWritten() + written);
	}

    /**
     * Closes the stream, ensuring persistence of all data.
     * This returns a Committer that can be used to publish (make visible) the file
     * that the stream was writing to.
     */
	@Override
	public Committer closeForCommit() throws IOException {
		LOG.info("closeForCommit(): returning a new ResumeRecoverable for commit");
        return new GcsCommitter(recoverable.copy(channel.capture(), recoverable.getTotalBytesWritten() + written));
	}
}
