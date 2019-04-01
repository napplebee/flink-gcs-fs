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

import com.google.cloud.RestorableState;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.BlobInfo;
import org.apache.flink.core.fs.RecoverableWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Data object to recover an GCS for a recoverable output stream.
 */
public class GcsRecoverable implements RecoverableWriter.ResumeRecoverable {
	private static final Logger LOG = LoggerFactory.getLogger(GcsRecoverable.class);

	/**
	 * GCS Blob Information
	 */
	private BlobInfo blobInfo;
	/**
	 * WriteChannel to recover
	 */
	private RestorableState<WriteChannel> state;
	/**
	 * Total bytes written to GCS
	 */
	private long totalBytesWritten;

	public GcsRecoverable() {
		LOG.debug("Creating GcsRecoverable - default constructor");
	}

	public GcsRecoverable(final BlobInfo blobInfo, final RestorableState<WriteChannel> state, final long totalBytesWritten) {
		LOG.debug("Constructor: Creating GcsRecoverable blobInfo={}, state={}, pos={}", blobInfo, state, totalBytesWritten);
		this.blobInfo = blobInfo;
		this.state = state;
		this.totalBytesWritten = totalBytesWritten;
	}

	public BlobInfo getBlobInfo() {
		return blobInfo;
	}

	public void setBlobInfo(BlobInfo blobInfo) {
		this.blobInfo = blobInfo;
	}

	public RestorableState<WriteChannel> getState() {
		return state;
	}

	public void setState(RestorableState<WriteChannel> state) {
		this.state = state;
	}

	public long getTotalBytesWritten() {
		return totalBytesWritten;
	}

	public void setTotalBytesWritten(long totalBytesWritten) {
		this.totalBytesWritten = totalBytesWritten;
	}

	/**
	 * Returns a new copy of GcsRecoverable
	 */
	public GcsRecoverable copy(RestorableState<WriteChannel> state, long totalBytesWritten) {
		return new GcsRecoverable(this.blobInfo, state, totalBytesWritten);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GcsRecoverable that = (GcsRecoverable) o;
		return Objects.equals(blobInfo, that.blobInfo) &&
				Objects.equals(state, that.state) &&
				Objects.equals(totalBytesWritten, that.totalBytesWritten);
	}

	@Override
	public int hashCode() {
		return Objects.hash(blobInfo, state, totalBytesWritten);
	}

	@Override
	public String toString() {
		return "GcsRecoverable{" +
				"blobInfo=" + blobInfo +
				", state=" + state +
				", totalBytesWritten=" + totalBytesWritten +
				'}';
	}
}
