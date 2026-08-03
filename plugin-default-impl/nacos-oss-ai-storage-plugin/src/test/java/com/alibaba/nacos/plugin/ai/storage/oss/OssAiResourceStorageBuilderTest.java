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

package com.alibaba.nacos.plugin.ai.storage.oss;

import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.plugin.ai.storage.spi.AiResourceStorage;
import com.alibaba.nacos.plugin.ai.storage.spi.AiResourceStorageBuilder;
import com.aliyun.oss.OSS;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class OssAiResourceStorageBuilderTest {
    
    @Test
    void shouldDiscoverBuilderAndDefaultFactoryThroughSpi() {
        Collection<AiResourceStorageBuilder> builders =
            NacosServiceLoader.load(AiResourceStorageBuilder.class);
        Collection<OssClientFactory> factories = NacosServiceLoader.load(OssClientFactory.class);
        
        assertTrue(builders.stream().anyMatch(OssAiResourceStorageBuilder.class::isInstance));
        assertTrue(factories.stream().anyMatch(DefaultOssClientFactory.class::isInstance));
    }
    
    @Test
    void shouldSkipRegistrationWithoutBootstrapProperties() {
        OssAiResourceStorageBuilder builder = new OssAiResourceStorageBuilder(key -> null,
            null);
        
        assertNull(builder.build());
    }
    
    @Test
    void shouldRejectPartialBootstrapProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put(OssAiResourceStorageBuilder.fullKey(OssStorageConfig.ENDPOINT),
            "https://oss-cn-hangzhou.aliyuncs.com");
        OssAiResourceStorageBuilder builder = new OssAiResourceStorageBuilder(properties::get,
            null);
        
        assertThrows(IllegalArgumentException.class, builder::build);
    }
    
    @Test
    void shouldSelectHighestPriorityClientFactory() {
        Map<String, String> properties = bootstrapProperties();
        AtomicBoolean defaultSelected = new AtomicBoolean();
        AtomicBoolean commercialSelected = new AtomicBoolean();
        OssClientFactory defaultFactory = factory(0, defaultSelected);
        OssClientFactory commercialFactory = factory(100, commercialSelected);
        OssAiResourceStorageBuilder builder = new OssAiResourceStorageBuilder(properties::get,
            Arrays.asList(defaultFactory, commercialFactory));
        
        AiResourceStorage storage = builder.build();
        storage.applyConfig(effectiveConfig());
        
        assertEquals(OssAiResourceStorage.TYPE, storage.type());
        assertInstanceOf(OssAiResourceStorage.class, storage);
        assertTrue(commercialSelected.get());
        assertFalse(defaultSelected.get());
    }
    
    private static OssClientFactory factory(int priority, AtomicBoolean selected) {
        return new OssClientFactory() {
            
            @Override
            public OSS create(OssStorageConfig config) {
                selected.set(true);
                return mock(OSS.class);
            }
            
            @Override
            public int priority() {
                return priority;
            }
        };
    }
    
    private static Map<String, String> bootstrapProperties() {
        Map<String, String> result = new HashMap<>();
        result.put(OssAiResourceStorageBuilder.fullKey(OssStorageConfig.ENDPOINT),
            "https://oss-cn-hangzhou.aliyuncs.com");
        result.put(OssAiResourceStorageBuilder.fullKey(OssStorageConfig.BUCKET_NAME), "nacos-ai");
        return result;
    }
    
    private static Map<String, String> effectiveConfig() {
        Map<String, String> result = new HashMap<>();
        result.put(OssStorageConfig.ENDPOINT, "https://oss-cn-hangzhou.aliyuncs.com");
        result.put(OssStorageConfig.BUCKET_NAME, "nacos-ai");
        return result;
    }
}
