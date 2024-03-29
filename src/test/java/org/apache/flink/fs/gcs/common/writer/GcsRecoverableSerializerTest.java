///*
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.apache.flink.fs.gcs.common.writer;
//
//import org.apache.flink.core.fs.Path;
//
//import org.junit.Test;
//
//import java.io.IOException;
//
//import static org.junit.Assert.assertEquals;
//
///**
// * Tests for the {@link GcsRecoverableSerializer}.
// */
//public class GcsRecoverableSerializerTest {
//
//	private static final String TEST_OBJECT_PATH = "gs://test-object/test-directory/test-upload-id";
//	private final GcsRecoverableSerializer serializer = GcsRecoverableSerializer.INSTANCE;
//
//	private static GcsRecoverable createTestGcsRecoverable() {
//		return new GcsRecoverable(new Path(TEST_OBJECT_PATH));
//	}
//
//	@Test
//	public void serializeEmptyGcsRecoverable() throws IOException {
//		GcsRecoverable originalEmptyRecoverable = createTestGcsRecoverable();
//
//		byte[] serializedRecoverable = serializer.serialize(originalEmptyRecoverable);
//		GcsRecoverable copiedEmptyRecoverable = serializer.deserialize(1, serializedRecoverable);
//
//		assertEquals(originalEmptyRecoverable, copiedEmptyRecoverable);
//	}
//}
