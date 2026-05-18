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

package com.alibaba.nacos.config.server.service.query;

/**
 * ConfigQueryHandlerChainBuilder.
 *
 * @author Nacos
 */
public interface ConfigQueryHandlerChainBuilder {
    
    /**
     * Builds the internal configuration query handler chain. The internal chain skips visibility
     * filtering so server-side callers (e.g. AI module bootstrap, indexes, MCP/A2A/Skill operation
     * services) can read every config.
     *
     * @return the internal configuration query handler chain
     */
    ConfigQueryHandlerChain build();
    
    /**
     * Builds the external configuration query handler chain. The external chain prepends visibility
     * filtering so user-facing callers cannot see configs that are reserved for internal subsystems
     * (e.g. AI resource configs).
     *
     * @return the external configuration query handler chain
     */
    ConfigQueryHandlerChain buildForExternal();
    
    /**
     * Gets the name of the builder.
     *
     * @return the name of the builder
     */
    String getName();
}
