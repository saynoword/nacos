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
import com.alibaba.nacos.plugin.ai.storage.model.StorageKey;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OssAiResourceStorageTest {
    
    private static final String BUCKET = "nacos-ai";
    
    private static final String OBJECT_KEY = "nacos/ai/ns:skill:v1:SKILL.md";
    
    private static final StorageKey STORAGE_KEY =
        new StorageKey(OssAiResourceStorage.TYPE, "ns:skill:v1:SKILL.md");
    
    @Mock
    private OSS ossClient;
    
    @Mock
    private OSSObject ossObject;
    
    @Mock
    private ObjectMetadata objectMetadata;
    
    private OssAiResourceStorage storage;
    
    @BeforeEach
    void setUp() {
        storage = new OssAiResourceStorage(ossClient, BUCKET, "/nacos/ai/", 16);
    }
    
    @Test
    void shouldExposeOssType() {
        assertEquals("oss", storage.type());
    }
    
    @Test
    void shouldSaveContentWithNormalizedPrefix() throws Exception {
        byte[] content = "content".getBytes(StandardCharsets.UTF_8);
        AtomicReference<byte[]> uploaded = new AtomicReference<>();
        doAnswer(invocation -> {
            uploaded.set(invocation.<InputStream>getArgument(2).readAllBytes());
            return null;
        }).when(ossClient).putObject(eq(BUCKET), eq(OBJECT_KEY), any(InputStream.class));
        
        storage.save(STORAGE_KEY, content);
        
        assertArrayEquals(content, uploaded.get());
    }
    
    @Test
    void shouldSaveNullAsEmptyContent() throws Exception {
        AtomicReference<byte[]> uploaded = new AtomicReference<>();
        doAnswer(invocation -> {
            uploaded.set(invocation.<InputStream>getArgument(2).readAllBytes());
            return null;
        }).when(ossClient).putObject(eq(BUCKET), eq(OBJECT_KEY), any(InputStream.class));
        
        storage.save(STORAGE_KEY, null);
        
        assertArrayEquals(new byte[0], uploaded.get());
    }
    
    @Test
    void shouldRejectContentOverLimit() {
        byte[] content = new byte[17];
        
        NacosException exception =
            assertThrows(NacosException.class, () -> storage.save(STORAGE_KEY, content));
        
        assertEquals(NacosException.INVALID_PARAM, exception.getErrCode());
    }
    
    @Test
    void shouldGetContent() throws Exception {
        byte[] content = "content".getBytes(StandardCharsets.UTF_8);
        when(ossClient.getObject(BUCKET, OBJECT_KEY)).thenReturn(ossObject);
        when(ossObject.getObjectMetadata()).thenReturn(objectMetadata);
        when(objectMetadata.getContentLength()).thenReturn((long) content.length);
        when(ossObject.getObjectContent()).thenReturn(new ByteArrayInputStream(content));
        
        byte[] actual = storage.get(STORAGE_KEY);
        
        assertArrayEquals(content, actual);
    }
    
    @Test
    void shouldReturnNullWhenObjectDoesNotExist() {
        OSSException notFound =
            new OSSException("not found", "NoSuchKey", null, null, null, null, null);
        when(ossClient.getObject(BUCKET, OBJECT_KEY)).thenThrow(notFound);
        
        assertNull(assertDoesNotThrow(() -> storage.get(STORAGE_KEY)));
    }
    
    @Test
    void shouldRejectDownloadedContentOverLimit() {
        when(ossClient.getObject(BUCKET, OBJECT_KEY)).thenReturn(ossObject);
        when(ossObject.getObjectMetadata()).thenReturn(objectMetadata);
        when(objectMetadata.getContentLength()).thenReturn(17L);
        
        NacosException exception =
            assertThrows(NacosException.class, () -> storage.get(STORAGE_KEY));
        
        assertEquals(NacosException.SERVER_ERROR, exception.getErrCode());
    }
    
    @Test
    void shouldEnforceLimitWhenContentLengthIsUnknown() {
        when(ossClient.getObject(BUCKET, OBJECT_KEY)).thenReturn(ossObject);
        when(ossObject.getObjectMetadata()).thenReturn(objectMetadata);
        when(objectMetadata.getContentLength()).thenReturn(-1L);
        when(ossObject.getObjectContent()).thenReturn(new ByteArrayInputStream(new byte[17]));
        
        NacosException exception =
            assertThrows(NacosException.class, () -> storage.get(STORAGE_KEY));
        
        assertEquals(NacosException.SERVER_ERROR, exception.getErrCode());
    }
    
    @Test
    void shouldConvertSdkFailure() {
        when(ossClient.getObject(BUCKET, OBJECT_KEY))
            .thenThrow(new ClientException("connection failed"));
        
        NacosException exception =
            assertThrows(NacosException.class, () -> storage.get(STORAGE_KEY));
        
        assertEquals(NacosException.SERVER_ERROR, exception.getErrCode());
    }
    
    @Test
    void shouldConvertReadFailure() {
        when(ossClient.getObject(BUCKET, OBJECT_KEY)).thenReturn(ossObject);
        when(ossObject.getObjectMetadata()).thenReturn(objectMetadata);
        when(objectMetadata.getContentLength()).thenReturn(-1L);
        when(ossObject.getObjectContent()).thenReturn(new InputStream() {
            
            @Override
            public int read() throws IOException {
                throw new IOException("read failed");
            }
        });
        
        NacosException exception =
            assertThrows(NacosException.class, () -> storage.get(STORAGE_KEY));
        
        assertEquals(NacosException.SERVER_ERROR, exception.getErrCode());
    }
    
    @Test
    void shouldDeleteObject() throws Exception {
        storage.delete(STORAGE_KEY);
        
        verify(ossClient).deleteObject(BUCKET, OBJECT_KEY);
    }
    
    @Test
    void shouldRejectBlankStorageKey() {
        assertThrows(IllegalArgumentException.class,
            () -> storage.get(new StorageKey(OssAiResourceStorage.TYPE, " ")));
    }
    
}
