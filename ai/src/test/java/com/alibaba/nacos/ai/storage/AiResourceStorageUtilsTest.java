/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.ai.storage;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AiResourceStorageUtilsTest {
    
    @Test
    void testBuildBundleKeyEncodesEachPathSegment() {
        assertEquals("team%2Fdemo/prompt/name%25with%2Fslash/%2E%2E/bundle.zip",
            AiResourceStorageUtils.buildBundleKey("team/demo", "prompt", "name%with/slash",
                ".."));
    }
    
    @Test
    void testSingleEntryZipRoundTripIsDeterministic() throws Exception {
        byte[] content = "hello".getBytes(StandardCharsets.UTF_8);
        byte[] first = AiResourceStorageUtils.zipSingleEntry("content.json", content);
        byte[] second = AiResourceStorageUtils.zipSingleEntry("content.json", content);
        
        assertArrayEquals(first, second);
        assertArrayEquals(content,
            AiResourceStorageUtils.readSingleEntry(first, "content.json"));
    }
}
