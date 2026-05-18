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

package com.alibaba.nacos.config.server.service.query.handler;

import com.alibaba.nacos.config.server.service.query.model.ConfigQueryChainRequest;
import com.alibaba.nacos.config.server.service.query.model.ConfigQueryChainResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceVisibilityHandlerTest {
    
    @InjectMocks
    private ResourceVisibilityHandler resourceVisibilityHandler;
    
    @Mock
    private ConfigQueryHandler nextHandler;
    
    @Test
    void handleAiResourceShortCircuitsAsNotFound() throws IOException {
        ConfigQueryChainRequest request = new ConfigQueryChainRequest();
        request.setDataId("SKILL.md");
        request.setGroup("skill_enc.6d79__enc.312e");
        
        ConfigQueryChainResponse response = resourceVisibilityHandler.handle(request);
        
        assertEquals(ConfigQueryChainResponse.ConfigQueryStatus.CONFIG_NOT_FOUND,
            response.getStatus());
        verify(nextHandler, never()).handle(request);
    }
    
    @Test
    void handleNonAiResourceDelegatesToNextHandler() throws IOException {
        ConfigQueryChainRequest request = new ConfigQueryChainRequest();
        request.setDataId("application.properties");
        request.setGroup("DEFAULT_GROUP");
        ConfigQueryChainResponse expectedResponse = new ConfigQueryChainResponse();
        when(nextHandler.handle(request)).thenReturn(expectedResponse);
        
        ConfigQueryChainResponse response = resourceVisibilityHandler.handle(request);
        
        assertEquals(expectedResponse, response);
    }
    
    @Test
    void getName() {
        assertEquals("resourceVisibilityHandler", resourceVisibilityHandler.getName());
    }
    
}
