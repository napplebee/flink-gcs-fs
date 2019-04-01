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

package org.apache.flink.fs.gcs.common;

import org.apache.flink.core.fs.FSDataOutputStream;
import org.apache.flink.core.fs.FileSystemKind;
import org.apache.flink.core.fs.Path;
import org.apache.flink.core.fs.RecoverableWriter;
import org.apache.flink.fs.gcs.common.writer.GcsRecoverableWriter;
import org.apache.flink.runtime.fs.hdfs.HadoopDataInputStream;
import org.apache.flink.runtime.fs.hdfs.HadoopDataOutputStream;
import org.apache.flink.runtime.fs.hdfs.HadoopFileSystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * HadoopFileSystem extension for a {@link FlinkGcsFileSystem}.
 */
public class FlinkGcsFileSystem extends HadoopFileSystem {
	private static final Logger LOG = LoggerFactory.getLogger(FlinkGcsFileSystem.class);

	private final org.apache.hadoop.fs.FileSystem hadoopFileSystem;

	FlinkGcsFileSystem(org.apache.hadoop.fs.FileSystem hadoopFileSystem) throws IOException {
		super(hadoopFileSystem);
		LOG.debug("Constructor(): Creating FlinkGcsFileSystem for uri={}", hadoopFileSystem.getUri());
		this.hadoopFileSystem = hadoopFileSystem;
	}

	@Override
	public RecoverableWriter createRecoverableWriter() {
		LOG.debug("createRecoverableWriter(): returning a RecoverableWriter");
		return new GcsRecoverableWriter();
	}

	@Override
	public FileSystemKind getKind() {
		LOG.debug("Returning FileSystemKind");
		return FileSystemKind.OBJECT_STORE;
	}

	@Override
	public HadoopDataInputStream open(Path f, int bufferSize) throws IOException {
		LOG.debug("open()");
		return super.open(f, bufferSize);
	}

	@Override
	public HadoopDataInputStream open(Path f) throws IOException {
		LOG.debug("open()");
		return super.open(f);
	}

	@Override
	public HadoopDataOutputStream create(Path f, boolean overwrite, int bufferSize, short replication, long blockSize) throws IOException {
		LOG.debug("create()");
		return super.create(f, overwrite, bufferSize, replication, blockSize);
	}

	@Override
	public HadoopDataOutputStream create(Path f, WriteMode overwrite) throws IOException {
		LOG.debug("create()");
		return super.create(f, overwrite);
	}

	@Override
	public FSDataOutputStream create(Path f, boolean overwrite) throws IOException {
		LOG.debug("create()");
		return super.create(f, overwrite);
	}
}
