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
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.common.auth.InstanceProfileCredentialsProvider;

/**
 * Default OSS client factory.
 */
public class DefaultOssClientFactory implements OssClientFactory {
    
    @Override
    public OSS create(OssStorageConfig config) {
        return new OSSClientBuilder().build(config.getEndpoint(),
            createCredentialsProvider(config));
    }
    
    CredentialsProvider createCredentialsProvider(OssStorageConfig config) {
        if (config.hasStaticCredentials()) {
            if (config.getSecurityToken().isEmpty()) {
                return new DefaultCredentialProvider(config.getAccessKeyId(),
                    config.getAccessKeySecret());
            }
            return new DefaultCredentialProvider(config.getAccessKeyId(),
                config.getAccessKeySecret(), config.getSecurityToken());
        }
        if (config.hasRamRole()) {
            return new InstanceProfileCredentialsProvider(config.getRamRoleName());
        }
        return new EnvironmentVariableCredentialsProvider();
    }
}
