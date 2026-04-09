/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.datasource.impl.dialect;

import com.alibaba.nacos.plugin.datasource.constants.DatabaseTypeConstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link MysqlDatabaseDialect}.
 */
class MysqlDatabaseDialectTest {

    private MysqlDatabaseDialect dialect;

    @BeforeEach
    void setUp() {
        dialect = new MysqlDatabaseDialect();
    }

    @Test
    void testGetType() {
        assertEquals(DatabaseTypeConstant.MYSQL, dialect.getType());
    }

    @Test
    void testGetLikeEscapeClause() {
        assertEquals("", dialect.getLikeEscapeClause());
    }

    @Test
    void testGetLimitPageSqlWithMark() {
        String result = dialect.getLimitPageSqlWithMark("SELECT * FROM users");
        assertEquals("SELECT * FROM users LIMIT ?,? ", result);
    }
}
