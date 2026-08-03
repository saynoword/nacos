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

import com.alibaba.nacos.api.plugin.ConfigItemDefinition;
import com.alibaba.nacos.api.plugin.ConfigItemEffectMode;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OssStorageConfigTest {
    
    @Test
    void shouldApplyDefaultsAndTrimValues() {
        Map<String, String> input = requiredConfig();
        input.put(OssStorageConfig.ENDPOINT, "  https://oss-cn-hangzhou.aliyuncs.com  ");
        input.put(OssStorageConfig.BUCKET_NAME, "  nacos-ai  ");
        
        OssStorageConfig config = OssStorageConfig.from(input);
        
        assertEquals("https://oss-cn-hangzhou.aliyuncs.com", config.getEndpoint());
        assertEquals("nacos-ai", config.getBucketName());
        assertEquals(OssStorageConfig.DEFAULT_OBJECT_PREFIX, config.getObjectPrefix());
        assertEquals(OssStorageConfig.DEFAULT_MAX_OBJECT_SIZE, config.getMaxObjectSize());
        assertFalse(config.hasStaticCredentials());
        assertFalse(config.hasRamRole());
        assertEquals(Long.toString(OssStorageConfig.DEFAULT_MAX_OBJECT_SIZE),
            config.toMap().get(OssStorageConfig.MAX_OBJECT_SIZE));
    }
    
    @Test
    void shouldDeclareRestartConfigurationAndSensitiveCredentials() {
        List<ConfigItemDefinition> definitions = OssStorageConfig.definitions();
        Map<String, ConfigItemDefinition> definitionsByKey = definitions.stream()
            .collect(Collectors.toMap(ConfigItemDefinition::getKey, item -> item));
        
        assertEquals(8, definitions.size());
        assertTrue(definitions.stream()
            .allMatch(item -> item.getEffectMode() == ConfigItemEffectMode.RESTART));
        assertTrue(definitionsByKey.get(OssStorageConfig.ENDPOINT).isRequired());
        assertTrue(definitionsByKey.get(OssStorageConfig.BUCKET_NAME).isRequired());
        assertTrue(definitionsByKey.get(OssStorageConfig.ACCESS_KEY_ID).isSensitive());
        assertTrue(definitionsByKey.get(OssStorageConfig.ACCESS_KEY_SECRET).isSensitive());
        assertTrue(definitionsByKey.get(OssStorageConfig.SECURITY_TOKEN).isSensitive());
    }
    
    @Test
    void shouldAcceptStaticAndRamRoleCredentialsSeparately() {
        Map<String, String> staticCredentials = requiredConfig();
        staticCredentials.put(OssStorageConfig.ACCESS_KEY_ID, "ak");
        staticCredentials.put(OssStorageConfig.ACCESS_KEY_SECRET, "secret");
        staticCredentials.put(OssStorageConfig.SECURITY_TOKEN, "token");
        assertTrue(OssStorageConfig.from(staticCredentials).hasStaticCredentials());
        
        Map<String, String> ramRole = requiredConfig();
        ramRole.put(OssStorageConfig.RAM_ROLE_NAME, "nacos-role");
        assertTrue(OssStorageConfig.from(ramRole).hasRamRole());
    }
    
    @Test
    void shouldRejectInvalidRequiredAndSizeConfiguration() {
        Map<String, String> missingEndpoint = requiredConfig();
        missingEndpoint.remove(OssStorageConfig.ENDPOINT);
        assertThrows(IllegalArgumentException.class,
            () -> OssStorageConfig.from(missingEndpoint));
        
        Map<String, String> invalidSize = requiredConfig();
        invalidSize.put(OssStorageConfig.MAX_OBJECT_SIZE, "0");
        assertThrows(IllegalArgumentException.class, () -> OssStorageConfig.from(invalidSize));
        invalidSize.put(OssStorageConfig.MAX_OBJECT_SIZE, "not-a-number");
        assertThrows(IllegalArgumentException.class, () -> OssStorageConfig.from(invalidSize));
    }
    
    @Test
    void shouldRejectAmbiguousCredentialConfiguration() {
        Map<String, String> incompleteStatic = requiredConfig();
        incompleteStatic.put(OssStorageConfig.ACCESS_KEY_ID, "ak");
        assertThrows(IllegalArgumentException.class,
            () -> OssStorageConfig.from(incompleteStatic));
        
        Map<String, String> tokenOnly = requiredConfig();
        tokenOnly.put(OssStorageConfig.SECURITY_TOKEN, "token");
        assertThrows(IllegalArgumentException.class, () -> OssStorageConfig.from(tokenOnly));
        
        Map<String, String> mixedCredentials = requiredConfig();
        mixedCredentials.put(OssStorageConfig.ACCESS_KEY_ID, "ak");
        mixedCredentials.put(OssStorageConfig.ACCESS_KEY_SECRET, "secret");
        mixedCredentials.put(OssStorageConfig.RAM_ROLE_NAME, "nacos-role");
        assertThrows(IllegalArgumentException.class,
            () -> OssStorageConfig.from(mixedCredentials));
    }
    
    private static Map<String, String> requiredConfig() {
        Map<String, String> result = new HashMap<>();
        result.put(OssStorageConfig.ENDPOINT, "https://oss-cn-hangzhou.aliyuncs.com");
        result.put(OssStorageConfig.BUCKET_NAME, "nacos-ai");
        return result;
    }
}
