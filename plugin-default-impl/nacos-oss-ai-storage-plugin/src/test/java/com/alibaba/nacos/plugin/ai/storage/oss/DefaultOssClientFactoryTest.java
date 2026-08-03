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

import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.common.auth.InstanceProfileCredentialsProvider;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class DefaultOssClientFactoryTest {
    
    private final DefaultOssClientFactory factory = new DefaultOssClientFactory();
    
    @Test
    void shouldUseStaticCredentialsWhenConfigured() {
        Map<String, String> input = requiredConfig();
        input.put(OssStorageConfig.ACCESS_KEY_ID, "ak");
        input.put(OssStorageConfig.ACCESS_KEY_SECRET, "secret");
        
        assertInstanceOf(DefaultCredentialProvider.class,
            factory.createCredentialsProvider(OssStorageConfig.from(input)));
    }
    
    @Test
    void shouldUseRamRoleWhenConfigured() {
        Map<String, String> input = requiredConfig();
        input.put(OssStorageConfig.RAM_ROLE_NAME, "nacos-role");
        
        assertInstanceOf(InstanceProfileCredentialsProvider.class,
            factory.createCredentialsProvider(OssStorageConfig.from(input)));
    }
    
    @Test
    void shouldUseEnvironmentCredentialsByDefault() {
        assertInstanceOf(EnvironmentVariableCredentialsProvider.class,
            factory.createCredentialsProvider(OssStorageConfig.from(requiredConfig())));
    }
    
    private static Map<String, String> requiredConfig() {
        Map<String, String> result = new HashMap<>();
        result.put(OssStorageConfig.ENDPOINT, "https://oss-cn-hangzhou.aliyuncs.com");
        result.put(OssStorageConfig.BUCKET_NAME, "nacos-ai");
        return result;
    }
}
