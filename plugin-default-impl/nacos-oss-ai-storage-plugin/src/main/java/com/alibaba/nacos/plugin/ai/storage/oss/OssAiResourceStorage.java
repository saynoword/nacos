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
    
    private final OSS ossClient;
    
    private final String bucketName;
    
    private final String objectPrefix;
    
    private final long maxObjectSize;
    
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
        this.ossClient = Objects.requireNonNull(ossClient, "ossClient");
        if (StringUtils.isBlank(bucketName)) {
            throw new IllegalArgumentException("bucketName is blank");
        }
        if (maxObjectSize <= 0) {
            throw new IllegalArgumentException("maxObjectSize must be positive");
        }
        this.bucketName = bucketName.trim();
        this.objectPrefix = normalizePrefix(objectPrefix);
        this.maxObjectSize = maxObjectSize;
    }
    
    @Override
    public String type() {
        return TYPE;
    }
    
    @Override
    public void save(StorageKey storageKey, byte[] content) throws NacosException {
        String objectKey = buildObjectKey(storageKey);
        byte[] actualContent = content == null ? new byte[0] : content;
        if (actualContent.length > maxObjectSize) {
            throw new NacosException(NacosException.INVALID_PARAM,
                "AI resource content exceeds the OSS object size limit");
        }
        try (InputStream input = new ByteArrayInputStream(actualContent)) {
            ossClient.putObject(bucketName, objectKey, input);
        } catch (OSSException | ClientException | IOException e) {
            throw storageException("save", e);
        }
    }
    
    @Override
    public byte[] get(StorageKey storageKey) throws NacosException {
        String objectKey = buildObjectKey(storageKey);
        try (OSSObject ossObject = ossClient.getObject(bucketName, objectKey)) {
            if (ossObject == null) {
                return null;
            }
            validateContentLength(ossObject.getObjectMetadata());
            return readContent(ossObject.getObjectContent());
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
        String objectKey = buildObjectKey(storageKey);
        try {
            ossClient.deleteObject(bucketName, objectKey);
        } catch (OSSException | ClientException e) {
            throw storageException("delete", e);
        }
    }
    
    private String buildObjectKey(StorageKey storageKey) {
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
    
    private void validateContentLength(ObjectMetadata metadata) throws NacosException {
        if (metadata != null && metadata.getContentLength() > maxObjectSize) {
            throw new NacosException(NacosException.SERVER_ERROR,
                "OSS object exceeds the configured AI resource size limit");
        }
    }
    
    private byte[] readContent(InputStream input) throws IOException, NacosException {
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
}
