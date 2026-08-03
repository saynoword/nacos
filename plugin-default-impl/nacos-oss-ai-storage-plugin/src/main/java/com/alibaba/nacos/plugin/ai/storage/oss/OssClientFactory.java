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

import com.aliyun.oss.OSS;

/**
 * Extension point for creating the OSS SDK client used by AI resource storage.
 *
 * <p>A distribution can provide a higher-priority factory to integrate its credential,
 * endpoint, and observability facilities without registering another {@code oss} storage.</p>
 */
public interface OssClientFactory {
    
    /**
     * Create an initialized OSS client.
     *
     * @param config validated OSS storage configuration
     * @return OSS client
     */
    OSS create(OssStorageConfig config);
    
    /**
     * Get selection priority. The highest priority wins.
     *
     * @return selection priority
     */
    default int priority() {
        return 0;
    }
}
