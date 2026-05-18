/*
 * Copyright 1999-$toady.year Alibaba Group Holding Ltd.
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
import com.alibaba.nacos.plugin.datasource.constants.AiResourceGroupType;

import java.io.IOException;

/**
 * ResourceVisibilityHandler. Filters configs that should be hidden from external callers (currently
 * AI resource configs: MCP / A2A / Skill / Prompt). Sits at the head of the external chain;
 * internal chain skips this handler entirely so AI module callers can still read these configs to
 * materialize their domain APIs.
 *
 * @author Nacos
 */
public class ResourceVisibilityHandler extends AbstractConfigQueryHandler {
    
    private static final String RESOURCE_VISIBILITY_HANDLER = "resourceVisibilityHandler";
    
    @Override
    public String getName() {
        return RESOURCE_VISIBILITY_HANDLER;
    }
    
    @Override
    public ConfigQueryChainResponse handle(ConfigQueryChainRequest request) throws IOException {
        if (isHidden(request)) {
            ConfigQueryChainResponse response = new ConfigQueryChainResponse();
            response.setStatus(ConfigQueryChainResponse.ConfigQueryStatus.CONFIG_NOT_FOUND);
            return response;
        }
        return nextHandler.handle(request);
    }
    
    private boolean isHidden(ConfigQueryChainRequest request) {
        return AiResourceGroupType.matches(request.getGroup(), request.getDataId());
    }
}
