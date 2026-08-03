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
import com.alibaba.nacos.api.plugin.ConfigItemType;
import com.alibaba.nacos.common.utils.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Effective configuration for OSS AI resource storage.
 */
public final class OssStorageConfig {
    
    public static final String ENDPOINT = "endpoint";
    
    public static final String BUCKET_NAME = "bucket-name";
    
    public static final String OBJECT_PREFIX = "object-prefix";
    
    public static final String MAX_OBJECT_SIZE = "max-object-size";
    
    public static final String ACCESS_KEY_ID = "access-key-id";
    
    public static final String ACCESS_KEY_SECRET = "access-key-secret";
    
    public static final String SECURITY_TOKEN = "security-token";
    
    public static final String RAM_ROLE_NAME = "ram-role-name";
    
    public static final String DEFAULT_OBJECT_PREFIX = "nacos/ai";
    
    public static final long DEFAULT_MAX_OBJECT_SIZE = 50L * 1024 * 1024;
    
    private static final List<ConfigItemDefinition> CONFIG_DEFINITIONS =
        Collections.unmodifiableList(Arrays.asList(
            definition(ENDPOINT, "OSS endpoint", ConfigItemType.STRING, "", true, false,
                "OSS service endpoint used by the Java SDK"),
            definition(BUCKET_NAME, "OSS bucket", ConfigItemType.STRING, "", true, false,
                "Bucket that stores AI resource objects"),
            definition(OBJECT_PREFIX, "Object prefix", ConfigItemType.STRING,
                DEFAULT_OBJECT_PREFIX, false, false,
                "Prefix prepended to opaque AI resource object keys"),
            definition(MAX_OBJECT_SIZE, "Maximum object size", ConfigItemType.NUMBER,
                Long.toString(DEFAULT_MAX_OBJECT_SIZE), false, false,
                "Maximum object size in bytes for uploads and downloads"),
            definition(ACCESS_KEY_ID, "Access key ID", ConfigItemType.STRING, "", false, true,
                "Optional static access key ID"),
            definition(ACCESS_KEY_SECRET, "Access key secret", ConfigItemType.STRING, "", false,
                true, "Optional static access key secret"),
            definition(SECURITY_TOKEN, "Security token", ConfigItemType.STRING, "", false, true,
                "Optional security token used with static access keys"),
            definition(RAM_ROLE_NAME, "RAM role name", ConfigItemType.STRING, "", false, false,
                "Optional ECS instance RAM role name")));
    
    private final String endpoint;
    
    private final String bucketName;
    
    private final String objectPrefix;
    
    private final long maxObjectSize;
    
    private final String accessKeyId;
    
    private final String accessKeySecret;
    
    private final String securityToken;
    
    private final String ramRoleName;
    
    private OssStorageConfig(String endpoint, String bucketName, String objectPrefix,
        long maxObjectSize, String accessKeyId, String accessKeySecret, String securityToken,
        String ramRoleName) {
        this.endpoint = endpoint;
        this.bucketName = bucketName;
        this.objectPrefix = objectPrefix;
        this.maxObjectSize = maxObjectSize;
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.securityToken = securityToken;
        this.ramRoleName = ramRoleName;
    }
    
    /**
     * Parse and validate an effective configuration map.
     *
     * @param config effective item-key map
     * @return validated configuration
     */
    public static OssStorageConfig from(Map<String, String> config) {
        Map<String, String> actual = config == null ? Collections.emptyMap() : config;
        String endpoint = value(actual, ENDPOINT, "");
        String bucketName = value(actual, BUCKET_NAME, "");
        String objectPrefix = value(actual, OBJECT_PREFIX, DEFAULT_OBJECT_PREFIX);
        long maxObjectSize = parseMaxObjectSize(value(actual, MAX_OBJECT_SIZE,
            Long.toString(DEFAULT_MAX_OBJECT_SIZE)));
        String accessKeyId = value(actual, ACCESS_KEY_ID, "");
        String accessKeySecret = value(actual, ACCESS_KEY_SECRET, "");
        String securityToken = value(actual, SECURITY_TOKEN, "");
        String ramRoleName = value(actual, RAM_ROLE_NAME, "");
        validateRequired(endpoint, ENDPOINT);
        validateRequired(bucketName, BUCKET_NAME);
        validateCredentials(accessKeyId, accessKeySecret, securityToken, ramRoleName);
        return new OssStorageConfig(endpoint, bucketName, objectPrefix, maxObjectSize,
            accessKeyId, accessKeySecret, securityToken, ramRoleName);
    }
    
    /**
     * Get plugin configuration definitions.
     *
     * @return immutable configuration definitions
     */
    public static List<ConfigItemDefinition> definitions() {
        return CONFIG_DEFINITIONS;
    }
    
    /**
     * Convert to an item-key map.
     *
     * @return immutable configuration map
     */
    public Map<String, String> toMap() {
        Map<String, String> result = new LinkedHashMap<>(8);
        result.put(ENDPOINT, endpoint);
        result.put(BUCKET_NAME, bucketName);
        result.put(OBJECT_PREFIX, objectPrefix);
        result.put(MAX_OBJECT_SIZE, Long.toString(maxObjectSize));
        result.put(ACCESS_KEY_ID, accessKeyId);
        result.put(ACCESS_KEY_SECRET, accessKeySecret);
        result.put(SECURITY_TOKEN, securityToken);
        result.put(RAM_ROLE_NAME, ramRoleName);
        return Collections.unmodifiableMap(result);
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public String getBucketName() {
        return bucketName;
    }
    
    public String getObjectPrefix() {
        return objectPrefix;
    }
    
    public long getMaxObjectSize() {
        return maxObjectSize;
    }
    
    public String getAccessKeyId() {
        return accessKeyId;
    }
    
    public String getAccessKeySecret() {
        return accessKeySecret;
    }
    
    public String getSecurityToken() {
        return securityToken;
    }
    
    public String getRamRoleName() {
        return ramRoleName;
    }
    
    public boolean hasStaticCredentials() {
        return StringUtils.isNotBlank(accessKeyId);
    }
    
    public boolean hasRamRole() {
        return StringUtils.isNotBlank(ramRoleName);
    }
    
    private static ConfigItemDefinition definition(String key, String name, ConfigItemType type,
        String defaultValue, boolean required, boolean sensitive, String description) {
        return new ConfigItemDefinition.Builder(key, name, type).description(description)
            .defaultValue(defaultValue).required(required).sensitive(sensitive)
            .effectMode(ConfigItemEffectMode.RESTART).build();
    }
    
    private static String value(Map<String, String> config, String key, String defaultValue) {
        String value = config.get(key);
        return value == null ? defaultValue : value.trim();
    }
    
    private static long parseMaxObjectSize(String value) {
        try {
            long result = Long.parseLong(value);
            if (result <= 0) {
                throw new IllegalArgumentException(MAX_OBJECT_SIZE + " must be positive");
            }
            return result;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(MAX_OBJECT_SIZE + " must be a positive integer", e);
        }
    }
    
    private static void validateRequired(String value, String key) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException(key + " is required");
        }
    }
    
    private static void validateCredentials(String accessKeyId, String accessKeySecret,
        String securityToken, String ramRoleName) {
        boolean hasAccessKeyId = StringUtils.isNotBlank(accessKeyId);
        boolean hasAccessKeySecret = StringUtils.isNotBlank(accessKeySecret);
        if (hasAccessKeyId != hasAccessKeySecret) {
            throw new IllegalArgumentException(
                ACCESS_KEY_ID + " and " + ACCESS_KEY_SECRET + " must be configured together");
        }
        if (StringUtils.isNotBlank(securityToken) && !hasAccessKeyId) {
            throw new IllegalArgumentException(
                SECURITY_TOKEN + " requires static access key credentials");
        }
        if (hasAccessKeyId && StringUtils.isNotBlank(ramRoleName)) {
            throw new IllegalArgumentException(
                RAM_ROLE_NAME + " cannot be combined with static access key credentials");
        }
    }
}
