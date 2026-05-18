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

package com.alibaba.nacos.config.server.service.query;

import com.alibaba.nacos.config.server.service.query.model.ConfigQueryChainRequest;
import com.alibaba.nacos.config.server.service.query.model.ConfigQueryChainResponse;
import com.alibaba.nacos.sys.env.EnvUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.StandardEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConfigQueryChainServiceTest {
    
    private ConfigQueryChainService configQueryChainService;
    
    @BeforeEach
    void setUp() {
        EnvUtil.setEnvironment(new StandardEnvironment());
        configQueryChainService = new ConfigQueryChainService();
    }
    
    @AfterEach
    void tearDown() {
        EnvUtil.setEnvironment(null);
    }
    
    @Test
    void handleHidesAiResourceGroupOnly() {
        ConfigQueryChainRequest request = new ConfigQueryChainRequest();
        request.setDataId("SKILL.md");
        request.setGroup("skill_enc.6d79__enc.312e");
        request.setTenant("public");
        
        ConfigQueryChainResponse response = configQueryChainService.handle(request);
        
        assertEquals(ConfigQueryChainResponse.ConfigQueryStatus.CONFIG_NOT_FOUND,
            response.getStatus());
        assertNull(response.getContent());
    }
    
    @Test
    void handleHidesAiResourceCompound() {
        ConfigQueryChainRequest request = new ConfigQueryChainRequest();
        request.setDataId("content.json");
        request.setGroup("prompt__myPrompt");
        request.setTenant("public");
        
        ConfigQueryChainResponse response = configQueryChainService.handle(request);
        
        assertEquals(ConfigQueryChainResponse.ConfigQueryStatus.CONFIG_NOT_FOUND,
            response.getStatus());
        assertNull(response.getContent());
    }
    
}
