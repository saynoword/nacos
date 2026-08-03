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
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.ai.storage.spi.AiResourceStorage;
import com.alibaba.nacos.plugin.ai.storage.spi.AiResourceStorageBuilder;
import com.alibaba.nacos.sys.env.EnvUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;

/**
 * SPI builder for OSS AI resource storage.
 */
public class OssAiResourceStorageBuilder implements AiResourceStorageBuilder {
    
    static final String CONFIG_PREFIX = "nacos.plugin.ai-storage.oss.";
    
    private final Function<String, String> propertyGetter;
    
    private final Collection<OssClientFactory> clientFactories;
    
    /**
     * Construct an OSS storage builder backed by the Nacos environment and SPI factories.
     */
    public OssAiResourceStorageBuilder() {
        this(EnvUtil::getProperty, NacosServiceLoader.load(OssClientFactory.class));
    }
    
    OssAiResourceStorageBuilder(Function<String, String> propertyGetter,
        Collection<OssClientFactory> clientFactories) {
        this.propertyGetter = Objects.requireNonNull(propertyGetter, "propertyGetter");
        this.clientFactories = clientFactories == null ? Collections.emptyList()
            : clientFactories;
    }
    
    @Override
    public String type() {
        return OssAiResourceStorage.TYPE;
    }
    
    @Override
    public AiResourceStorage build() {
        String endpoint = propertyGetter.apply(fullKey(OssStorageConfig.ENDPOINT));
        String bucketName = propertyGetter.apply(fullKey(OssStorageConfig.BUCKET_NAME));
        if (StringUtils.isBlank(endpoint) && StringUtils.isBlank(bucketName)) {
            return null;
        }
        if (StringUtils.isBlank(endpoint) || StringUtils.isBlank(bucketName)) {
            throw new IllegalArgumentException(
                "OSS endpoint and bucket-name must be configured together");
        }
        return new OssAiResourceStorage(selectClientFactory());
    }
    
    static String fullKey(String itemKey) {
        return CONFIG_PREFIX + itemKey;
    }
    
    private OssClientFactory selectClientFactory() {
        return clientFactories.stream().filter(Objects::nonNull)
            .max(Comparator.comparingInt(OssClientFactory::priority)
                .thenComparing(factory -> factory.getClass().getName()))
            .orElseGet(DefaultOssClientFactory::new);
    }
}
