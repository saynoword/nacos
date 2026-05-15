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

package com.alibaba.nacos.plugin.datasource.constants;

/**
 * AI Resource types stored as Nacos configs whose content should be hidden from the user-facing
 * config GET APIs.
 *
 * <p>Each enum value declares its {@code group_id} prefix and optionally a set of dataId matchers.
 * Two filtering modes:
 * <ul>
 *   <li><b>Group-only</b> ({@code dataIdMatchers == null}): matches every config whose group
 *       starts with the prefix.</li>
 *   <li><b>Compound</b> ({@code dataIdMatchers} populated): matches only when the group prefix
 *       and one of the dataId matchers both apply, reducing false positives for user configs
 *       that share a prefix.</li>
 * </ul>
 *
 * @author sai
 */
public enum AiResourceGroupType {
    
    /**
     * Skill manifest (index) config.
     *
     * <p>Group format: {@code skill_{name}}, dataId fixed to {@code skill_index.json} (legacy
     * {@code skill.json} also matched).
     */
    SKILL_MANIFEST("skill_", new DataIdMatcher[] {
        DataIdMatcher.exact("skill_index.json"),
        DataIdMatcher.exact("skill.json")
    }),
    
    /**
     * Skill version file configs (SKILL.md, README.md, resource files).
     *
     * <p>Group format: {@code skill_enc.{hex}__enc.{hex}}. DataIds are arbitrary file paths so
     * group-only filtering is used.
     */
    SKILL_VERSION("skill_enc.", null),
    
    /**
     * AgentSpec configs.
     */
    AGENTSPEC("agentspec__", new DataIdMatcher[] {
        DataIdMatcher.like("resource_%"),
        DataIdMatcher.like("enc.%"),
        DataIdMatcher.exact("manifest.json"),
        DataIdMatcher.exact("agentspec_index.json")
    }),
    
    /**
     * Prompt configs.
     */
    PROMPT("prompt__", new DataIdMatcher[] {
        DataIdMatcher.exact("content.json")
    });
    
    private final String groupPrefix;
    
    private final DataIdMatcher[] dataIdMatchers;
    
    AiResourceGroupType(String groupPrefix, DataIdMatcher[] dataIdMatchers) {
        this.groupPrefix = groupPrefix;
        this.dataIdMatchers = dataIdMatchers;
    }
    
    public String getGroupPrefix() {
        return groupPrefix;
    }
    
    /**
     * Check if a {@code (group, dataId)} pair matches any AI resource pattern.
     *
     * @param group  the {@code group_id} value
     * @param dataId the {@code data_id} value
     * @return true if the pair matches an internal AI resource config
     */
    public static boolean matches(String group, String dataId) {
        if (group == null) {
            return false;
        }
        for (AiResourceGroupType type : values()) {
            if (!group.startsWith(type.groupPrefix)) {
                continue;
            }
            DataIdMatcher[] matchers = type.dataIdMatchers;
            if (matchers == null) {
                return true;
            }
            if (dataId == null) {
                continue;
            }
            for (DataIdMatcher m : matchers) {
                if (m.like) {
                    String prefix = m.pattern.endsWith("%")
                        ? m.pattern.substring(0, m.pattern.length() - 1)
                        : m.pattern;
                    if (dataId.startsWith(prefix)) {
                        return true;
                    }
                } else if (dataId.equals(m.pattern)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Describes a dataId matching rule — either a SQL-style LIKE pattern or an exact value.
     */
    public static class DataIdMatcher {
        
        private final String pattern;
        
        private final boolean like;
        
        private DataIdMatcher(String pattern, boolean like) {
            this.pattern = pattern;
            this.like = like;
        }
        
        /**
         * Create a LIKE matcher (e.g. {@code resource_%}).
         */
        public static DataIdMatcher like(String pattern) {
            return new DataIdMatcher(pattern, true);
        }
        
        /**
         * Create an exact-match matcher (e.g. {@code skill_index.json}).
         */
        public static DataIdMatcher exact(String value) {
            return new DataIdMatcher(value, false);
        }
    }
}
