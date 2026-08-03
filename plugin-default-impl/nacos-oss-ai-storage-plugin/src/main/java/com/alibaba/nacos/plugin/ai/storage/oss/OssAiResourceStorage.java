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

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.plugin.ConfigItemDefinition;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.ai.storage.model.StorageKey;
import com.alibaba.nacos.plugin.ai.storage.spi.AiResourceStorage;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Alibaba Cloud OSS based {@link AiResourceStorage} implementation.
 *
 * <p>The configured object prefix is prepended to the opaque {@link StorageKey#getKey()}.
 * The configured bucket is not encoded into the storage key.</p>
 */
public class OssAiResourceStorage implements AiResourceStorage {
    
    public static final String TYPE = "oss";
    
    private static final String NO_SUCH_KEY = "NoSuchKey";
    
    private static final int MAX_OBJECT_KEY_BYTES = 1023;
    
    private static final int BUFFER_SIZE = 8192;
    
    private final OssClientFactory clientFactory;
    
    private volatile RuntimeState state;
    
    /**
     * Construct a configurable OSS storage implementation.
     *
     * @param clientFactory OSS client factory
     */
    public OssAiResourceStorage(OssClientFactory clientFactory) {
        this.clientFactory = Objects.requireNonNull(clientFactory, "clientFactory");
    }
    
    /**
     * Construct the OSS storage implementation.
     *
     * @param ossClient     initialized OSS SDK client
     * @param bucketName    target bucket
     * @param objectPrefix  optional object key prefix
     * @param maxObjectSize maximum object size in bytes
     */
    public OssAiResourceStorage(OSS ossClient, String bucketName, String objectPrefix,
        long maxObjectSize) {
        OSS actualClient = Objects.requireNonNull(ossClient, "ossClient");
        if (StringUtils.isBlank(bucketName)) {
            throw new IllegalArgumentException("bucketName is blank");
        }
        if (maxObjectSize <= 0) {
            throw new IllegalArgumentException("maxObjectSize must be positive");
        }
        this.clientFactory = null;
        this.state = new RuntimeState(actualClient, bucketName.trim(),
            normalizePrefix(objectPrefix), maxObjectSize, Collections.emptyMap());
    }
    
    @Override
    public String type() {
        return TYPE;
    }
    
    @Override
    public List<ConfigItemDefinition> getConfigDefinitions() {
        return OssStorageConfig.definitions();
    }
    
    @Override
    public synchronized void applyConfig(Map<String, String> effectiveConfig) {
        OssStorageConfig config = OssStorageConfig.from(effectiveConfig);
        Map<String, String> currentConfig = config.toMap();
        RuntimeState oldState = state;
        if (oldState != null && oldState.currentConfig.equals(currentConfig)) {
            return;
        }
        if (clientFactory == null) {
            throw new IllegalStateException(
                "Directly initialized OSS storage cannot be reconfigured");
        }
        OSS newClient = Objects.requireNonNull(clientFactory.create(config),
            "OSS client factory returned null");
        state = new RuntimeState(newClient, config.getBucketName(),
            normalizePrefix(config.getObjectPrefix()), config.getMaxObjectSize(), currentConfig);
        if (oldState != null) {
            oldState.ossClient.shutdown();
        }
    }
    
    @Override
    public Map<String, String> getCurrentConfig() {
        RuntimeState current = state;
        return current == null ? Collections.emptyMap() : current.currentConfig;
    }
    
    @Override
    public void save(StorageKey storageKey, byte[] content) throws NacosException {
        RuntimeState current = requireState();
        String objectKey = buildObjectKey(storageKey, current.objectPrefix);
        byte[] actualContent = content == null ? new byte[0] : content;
        if (actualContent.length > current.maxObjectSize) {
            throw new NacosException(NacosException.INVALID_PARAM,
                "AI resource content exceeds the OSS object size limit");
        }
        try (InputStream input = new ByteArrayInputStream(actualContent)) {
            current.ossClient.putObject(current.bucketName, objectKey, input);
        } catch (OSSException | ClientException | IOException e) {
            throw storageException("save", e);
        }
    }
    
    @Override
    public byte[] get(StorageKey storageKey) throws NacosException {
        RuntimeState current = requireState();
        String objectKey = buildObjectKey(storageKey, current.objectPrefix);
        try (OSSObject ossObject = current.ossClient.getObject(current.bucketName, objectKey)) {
            if (ossObject == null) {
                return null;
            }
            validateContentLength(ossObject.getObjectMetadata(), current.maxObjectSize);
            return readContent(ossObject.getObjectContent(), current.maxObjectSize);
        } catch (OSSException e) {
            if (NO_SUCH_KEY.equals(e.getErrorCode())) {
                return null;
            }
            throw storageException("get", e);
        } catch (ClientException | IOException e) {
            throw storageException("get", e);
        }
    }
    
    @Override
    public void delete(StorageKey storageKey) throws NacosException {
        RuntimeState current = requireState();
        String objectKey = buildObjectKey(storageKey, current.objectPrefix);
        try {
            current.ossClient.deleteObject(current.bucketName, objectKey);
        } catch (OSSException | ClientException e) {
            throw storageException("delete", e);
        }
    }
    
    private RuntimeState requireState() throws NacosException {
        RuntimeState current = state;
        if (current == null) {
            throw new NacosException(NacosException.SERVER_ERROR,
                "OSS AI resource storage is not initialized");
        }
        return current;
    }
    
    private String buildObjectKey(StorageKey storageKey, String objectPrefix) {
        if (storageKey == null || StringUtils.isBlank(storageKey.getKey())) {
            throw new IllegalArgumentException("StorageKey.key is blank");
        }
        String objectKey = StringUtils.isBlank(objectPrefix) ? storageKey.getKey()
            : objectPrefix + "/" + storageKey.getKey();
        if (objectKey.startsWith("/") || objectKey.startsWith("\\")) {
            throw new IllegalArgumentException("OSS object key must not start with a slash");
        }
        if (objectKey.getBytes(StandardCharsets.UTF_8).length > MAX_OBJECT_KEY_BYTES) {
            throw new IllegalArgumentException("OSS object key exceeds 1023 bytes");
        }
        return objectKey;
    }
    
    private void validateContentLength(ObjectMetadata metadata, long maxObjectSize)
        throws NacosException {
        if (metadata != null && metadata.getContentLength() > maxObjectSize) {
            throw new NacosException(NacosException.SERVER_ERROR,
                "OSS object exceeds the configured AI resource size limit");
        }
    }
    
    private byte[] readContent(InputStream input, long maxObjectSize)
        throws IOException, NacosException {
        if (input == null) {
            return new byte[0];
        }
        try (InputStream actualInput = input;
            ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[BUFFER_SIZE];
            long total = 0;
            int length;
            while ((length = actualInput.read(buffer)) != -1) {
                total += length;
                if (total > maxObjectSize) {
                    throw new NacosException(NacosException.SERVER_ERROR,
                        "OSS object exceeds the configured AI resource size limit");
                }
                output.write(buffer, 0, length);
            }
            return output.toByteArray();
        }
    }
    
    private static String normalizePrefix(String prefix) {
        if (StringUtils.isBlank(prefix)) {
            return StringUtils.EMPTY;
        }
        String result = prefix.trim();
        while (result.startsWith("/")) {
            result = result.substring(1);
        }
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
    
    private static NacosException storageException(String operation, Throwable cause) {
        return new NacosException(NacosException.SERVER_ERROR,
            "Failed to " + operation + " AI resource in OSS", cause);
    }
    
    private static final class RuntimeState {
        
        private final OSS ossClient;
        
        private final String bucketName;
        
        private final String objectPrefix;
        
        private final long maxObjectSize;
        
        private final Map<String, String> currentConfig;
        
        private RuntimeState(OSS ossClient, String bucketName, String objectPrefix,
            long maxObjectSize, Map<String, String> currentConfig) {
            this.ossClient = ossClient;
            this.bucketName = bucketName;
            this.objectPrefix = objectPrefix;
            this.maxObjectSize = maxObjectSize;
            this.currentConfig = currentConfig;
        }
    }
}
