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
 * Unit tests for {@link DerbyDatabaseDialect}.
 */
class DerbyDatabaseDialectTest {

    private DerbyDatabaseDialect dialect;

    @BeforeEach
    void setUp() {
        dialect = new DerbyDatabaseDialect();
    }

    @Test
    void testGetType() {
        assertEquals(DatabaseTypeConstant.DERBY, dialect.getType());
    }

    @Test
    void testGetLimitPageSqlWithMark() {
        String result = dialect.getLimitPageSqlWithMark("SELECT * FROM users");
        assertEquals("SELECT * FROM users OFFSET ? ROWS FETCH NEXT ? ROWS ONLY ", result);
    }

    @Test
    void testGetLimitTopSqlWithMark() {
        String result = dialect.getLimitTopSqlWithMark("SELECT * FROM users");
        assertEquals("SELECT * FROM users OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY ", result);
    }

    @Test
    void testGetLimitPageSql() {
        String result = dialect.getLimitPageSql("SELECT * FROM users", 2, 10);
        assertEquals("SELECT * FROM users OFFSET 10 ROWS FETCH NEXT 10 ROWS ONLY", result);
    }

    @Test
    void testGetLimitPageSqlFirstPage() {
        String result = dialect.getLimitPageSql("SELECT * FROM users", 1, 20);
        assertEquals("SELECT * FROM users OFFSET 0 ROWS FETCH NEXT 20 ROWS ONLY", result);
    }

    @Test
    void testGetLimitPageSqlWithOffset() {
        String result = dialect.getLimitPageSqlWithOffset("SELECT * FROM users", 5, 10);
        assertEquals("SELECT * FROM users OFFSET 5 ROWS FETCH NEXT 10 ROWS ONLY", result);
    }

    @Test
    void testGetPagePrevNum() {
        assertEquals(0, dialect.getPagePrevNum(1, 10));
        assertEquals(10, dialect.getPagePrevNum(2, 10));
        assertEquals(40, dialect.getPagePrevNum(3, 20));
    }

    @Test
    void testGetPageLastNum() {
        assertEquals(10, dialect.getPageLastNum(1, 10));
        assertEquals(20, dialect.getPageLastNum(2, 20));
    }

    @Test
    void testGetLikeEscapeClause() {
        assertEquals(" ESCAPE '\\' ", dialect.getLikeEscapeClause());
    }
}
