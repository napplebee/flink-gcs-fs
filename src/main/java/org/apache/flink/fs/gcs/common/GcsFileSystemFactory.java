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

import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.core.fs.FileSystemFactory;

import com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem;
import com.google.cloud.hadoop.gcsio.GoogleCloudStorageFileSystem;
import org.apache.flink.runtime.util.HadoopUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

/**
 * Factory implementation for a {@link GcsFileSystemFactory}.
 */
public class GcsFileSystemFactory implements FileSystemFactory {
	private final Logger log = LoggerFactory.getLogger(getClass());
	private Configuration configuration;

	private org.apache.hadoop.conf.Configuration buildHadoopConfiguration(Configuration conf) {
		log.debug("Building Hadoop Configuration");
		log.debug("Flink configuration:\n{}", conf.toString());
		final org.apache.hadoop.conf.Configuration hadoopConf = HadoopUtils.getHadoopConfiguration(configuration);
		log.debug("Hadoop configuration:\n{}", hadoopConf);
		return hadoopConf;
	}

	private FileSystem buildFlinkGcsFileSystem(URI uri, org.apache.hadoop.conf.Configuration conf) throws IOException {
		log.debug("Building and returning a FlinkGcsFileSystem");
		final org.apache.hadoop.fs.FileSystem hfs = new GoogleHadoopFileSystem();
		hfs.initialize(uri, conf);
		return new FlinkGcsFileSystem(hfs);
	}

	@Override
	public String getScheme() {
		log.debug("Returning scheme: {}", GoogleCloudStorageFileSystem.SCHEME);
		return GoogleCloudStorageFileSystem.SCHEME;
	}

	@Override
	public void configure(Configuration configuration) {
		log.debug("Setting configuration:\n{}", configuration.toString());
		this.configuration = configuration;
	}

	@Override
	public FileSystem create(URI uri) throws IOException {
		log.debug("Creating and returning a new FlinkGcsFileSystem");
		return buildFlinkGcsFileSystem(uri, buildHadoopConfiguration(configuration));
	}
}
