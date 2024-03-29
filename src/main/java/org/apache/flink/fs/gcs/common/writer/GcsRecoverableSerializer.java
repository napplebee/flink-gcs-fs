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

import org.apache.flink.core.io.SimpleVersionedSerializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Serializer implementation for a {@link GcsRecoverableSerializer}.
 */
public class GcsRecoverableSerializer implements SimpleVersionedSerializer<GcsRecoverable> {
	private static final Logger LOG = LoggerFactory.getLogger(GcsRecoverableSerializer.class);

	static final GcsRecoverableSerializer INSTANCE = new GcsRecoverableSerializer();

	private final Kryo kyro;

	public GcsRecoverableSerializer() {
		LOG.debug("Constructor: Creating GcsRecoverableSerializer");
		this.kyro = new Kryo();
		this.kyro.register(GcsRecoverable.class);
	}

	@Override
	public int getVersion() {
		return 1;
	}

	@Override
	public byte[] serialize(GcsRecoverable gcsRecoverable) throws IOException {
		LOG.debug("Serializing recoverable={}", gcsRecoverable);
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			try (Output output = new Output(baos)) {
				kyro.writeObject(output, gcsRecoverable);
			}
			return baos.toByteArray();
		}
	}

	@Override
	public GcsRecoverable deserialize(int version, byte[] serialized) throws IOException {
		LOG.debug("Deserializing recoverable for version: {}", version);
		switch (version) {
			case 1:
				try (Input input = new Input(serialized)) {
					return this.kyro.readObject(input, GcsRecoverable.class);
				}
			default:
				LOG.error("Unrecognized version or corrupt state: {}", version);
				throw new IOException("Unrecognized version or corrupt state: " + version);
		}
	}
}
