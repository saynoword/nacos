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

package com.alibaba.nacos.plugin.auth.impl.persistence.embedded;

import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.persistence.repository.embedded.operate.DatabaseOperate;
import com.alibaba.nacos.plugin.datasource.dialect.DatabaseDialect;
import com.alibaba.nacos.plugin.datasource.manager.DatabaseDialectManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthEmbeddedPaginationHelperImplTest {
    
    @Mock
    private DatabaseOperate databaseOperate;
    
    @Mock
    private DatabaseDialectManager dialectManager;
    
    @Mock
    private RowMapper<Object> rowMapper;
    
    private MockedStatic<DatabaseDialectManager> dialectManagerStatic;
    
    @BeforeEach
    void setUp() {
        dialectManagerStatic = mockStatic(DatabaseDialectManager.class);
        dialectManagerStatic.when(DatabaseDialectManager::getInstance).thenReturn(dialectManager);
    }
    
    @AfterEach
    void tearDown() {
        dialectManagerStatic.close();
    }
    
    private DatabaseDialect stubDerbyDialect() {
        DatabaseDialect dialect = mock(DatabaseDialect.class);
        when(dialect.getLimitPageSqlWithMark(any())).thenAnswer(
                invocation -> invocation.getArgument(0) + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY ");
        when(dialect.getPagePrevNum(1, 10)).thenReturn(0);
        when(dialect.getPageLastNum(1, 10)).thenReturn(10);
        when(dialectManager.getDialect("derby")).thenReturn(dialect);
        return dialect;
    }
    
    private DatabaseDialect stubMysqlDialect() {
        DatabaseDialect dialect = mock(DatabaseDialect.class);
        when(dialect.getLimitPageSqlWithMark(any())).thenAnswer(
                invocation -> invocation.getArgument(0) + " LIMIT ?,? ");
        when(dialect.getPagePrevNum(1, 10)).thenReturn(0);
        when(dialect.getPageLastNum(1, 10)).thenReturn(10);
        when(dialectManager.getDialect("mysql")).thenReturn(dialect);
        return dialect;
    }
    
    @Test
    void testFetchPageWithDerbyDialect() {
        stubDerbyDialect();
        when(databaseOperate.queryOne(any(String.class), any(Object[].class), eq(Integer.class))).thenReturn(20);
        when(databaseOperate.queryMany(any(String.class), any(Object[].class), any(RowMapper.class))).thenReturn(
                Collections.emptyList());
        
        AuthEmbeddedPaginationHelperImpl<Object> helper = new AuthEmbeddedPaginationHelperImpl<>(databaseOperate,
                "derby");
        Page<Object> page = helper.fetchPage("SELECT count(*) FROM users", "SELECT * FROM users", new Object[] {},
                1, 10, rowMapper);
        
        assertNotNull(page);
        assertEquals(1, page.getPageNumber());
        assertEquals(2, page.getPagesAvailable());
        assertEquals(20, page.getTotalCount());
        
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> argsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(databaseOperate).queryMany(sqlCaptor.capture(), argsCaptor.capture(), any(RowMapper.class));
        
        assertEquals("SELECT * FROM users OFFSET ? ROWS FETCH NEXT ? ROWS ONLY ", sqlCaptor.getValue());
        Object[] capturedArgs = argsCaptor.getValue();
        assertEquals(2, capturedArgs.length);
        assertEquals(0, capturedArgs[0]);
        assertEquals(10, capturedArgs[1]);
    }
    
    @Test
    void testFetchPageWithMysqlDialect() {
        stubMysqlDialect();
        when(databaseOperate.queryOne(any(String.class), any(Object[].class), eq(Integer.class))).thenReturn(20);
        when(databaseOperate.queryMany(any(String.class), any(Object[].class), any(RowMapper.class))).thenReturn(
                Collections.emptyList());
        
        AuthEmbeddedPaginationHelperImpl<Object> helper = new AuthEmbeddedPaginationHelperImpl<>(databaseOperate,
                "mysql");
        Page<Object> page = helper.fetchPage("SELECT count(*) FROM users", "SELECT * FROM users", new Object[] {},
                1, 10, rowMapper);
        
        assertNotNull(page);
        
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(databaseOperate).queryMany(sqlCaptor.capture(), any(Object[].class), any(RowMapper.class));
        
        assertEquals("SELECT * FROM users LIMIT ?,? ", sqlCaptor.getValue());
    }
    
    @Test
    void testFetchPageLimitWithDerbyDialect() {
        stubDerbyDialect();
        when(databaseOperate.queryOne(any(String.class), eq(Integer.class))).thenReturn(15);
        when(databaseOperate.queryMany(any(String.class), any(Object[].class), any(RowMapper.class))).thenReturn(
                Collections.emptyList());
        
        AuthEmbeddedPaginationHelperImpl<Object> helper = new AuthEmbeddedPaginationHelperImpl<>(databaseOperate,
                "derby");
        Page<Object> page = helper.fetchPageLimit("SELECT count(*) FROM users", "SELECT * FROM users", new Object[] {},
                1, 10, rowMapper);
        
        assertNotNull(page);
        assertEquals(2, page.getPagesAvailable());
        
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(databaseOperate).queryMany(sqlCaptor.capture(), any(Object[].class), any(RowMapper.class));
        
        assertEquals("SELECT * FROM users OFFSET ? ROWS FETCH NEXT ? ROWS ONLY ", sqlCaptor.getValue());
    }
    
    @Test
    void testFetchPageLimitWithSeparateArgs() {
        stubDerbyDialect();
        when(databaseOperate.queryOne(any(String.class), any(Object[].class), eq(Integer.class))).thenReturn(5);
        when(databaseOperate.queryMany(any(String.class), any(Object[].class), any(RowMapper.class))).thenReturn(
                Collections.emptyList());
        
        AuthEmbeddedPaginationHelperImpl<Object> helper = new AuthEmbeddedPaginationHelperImpl<>(databaseOperate,
                "derby");
        Page<Object> page = helper.fetchPageLimit("SELECT count(*) FROM users WHERE username=?",
                new Object[] {"nacos"}, "SELECT * FROM users WHERE username=?", new Object[] {"nacos"}, 1, 10,
                rowMapper);
        
        assertNotNull(page);
        
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> argsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(databaseOperate).queryMany(sqlCaptor.capture(), argsCaptor.capture(), any(RowMapper.class));
        
        assertEquals("SELECT * FROM users WHERE username=? OFFSET ? ROWS FETCH NEXT ? ROWS ONLY ",
                sqlCaptor.getValue());
        Object[] capturedArgs = argsCaptor.getValue();
        assertEquals(3, capturedArgs.length);
        assertEquals("nacos", capturedArgs[0]);
        assertEquals(0, capturedArgs[1]);
        assertEquals(10, capturedArgs[2]);
    }
    
    @Test
    void testFetchPageReturnsEmptyWhenPageNoExceedsTotal() {
        when(databaseOperate.queryOne(any(String.class), any(Object[].class), eq(Integer.class))).thenReturn(5);
        
        AuthEmbeddedPaginationHelperImpl<Object> helper = new AuthEmbeddedPaginationHelperImpl<>(databaseOperate,
                "derby");
        Page<Object> page = helper.fetchPage("SELECT count(*) FROM users", "SELECT * FROM users", new Object[] {},
                2, 10, rowMapper);
        
        assertNotNull(page);
        assertEquals(2, page.getPageNumber());
        assertEquals(1, page.getPagesAvailable());
        assertEquals(0, page.getPageItems().size());
    }
    
    @Test
    void testFetchPageThrowsOnInvalidPageArgs() {
        AuthEmbeddedPaginationHelperImpl<Object> helper = new AuthEmbeddedPaginationHelperImpl<>(databaseOperate,
                "derby");
        
        assertThrows(IllegalArgumentException.class,
                () -> helper.fetchPage("SELECT count(*)", "SELECT *", new Object[] {}, 0, 10, rowMapper));
        assertThrows(IllegalArgumentException.class,
                () -> helper.fetchPage("SELECT count(*)", "SELECT *", new Object[] {}, 1, 0, rowMapper));
    }
}
