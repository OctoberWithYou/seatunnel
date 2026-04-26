/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.config;

import org.apache.seatunnel.connectors.cdc.base.config.JdbcSourceConfig;
import org.apache.seatunnel.connectors.cdc.base.config.JdbcSourceConfigFactory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

/** Test for {@link YashanSourceConfigFactory}. */
public class YashanSourceConfigFactoryTest {

    @Test
    public void testCreateWithBasicConfig() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");

        JdbcSourceConfig config = factory.create(0);

        Assertions.assertNotNull(config);
        Assertions.assertTrue(config instanceof YashanSourceConfig);
    }

    @Test
    public void testCreateWithUrl() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.url("jdbc:yasdb://localhost:1688/testdb");
        factory.username("SYS");
        factory.password("yasdb_123");

        JdbcSourceConfig config = factory.create(0);

        Assertions.assertNotNull(config);
        Assertions.assertEquals("jdbc:yasdb://localhost:1688/testdb", config.getOriginUrl());
    }

    @Test
    public void testCreateWithSchemaList() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");
        factory.schemaList(Collections.singletonList("PUBLIC"));

        JdbcSourceConfig config = factory.create(0);

        Assertions.assertNotNull(config);
    }

    @Test
    public void testCreateWithTableList() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");
        factory.tableList(Collections.singletonList("PUBLIC.TEST_TABLE"));

        JdbcSourceConfig config = factory.create(0);

        Assertions.assertNotNull(config);
        Assertions.assertNotNull(config.getTableList());
    }

    @Test
    public void testCreateWithDatabaseList() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");
        factory.databaseList(Collections.singletonList("testdb"));

        JdbcSourceConfig config = factory.create(0);

        Assertions.assertNotNull(config);
        Assertions.assertNotNull(config.getDatabaseList());
    }

    @Test
    public void testCreateWithSplitSize() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");
        factory.splitSize(1024);

        JdbcSourceConfig config = factory.create(0);

        Assertions.assertNotNull(config);
        Assertions.assertEquals(1024, config.getSplitSize());
    }

    @Test
    public void testCreateWithFetchSize() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");
        factory.fetchSize(500);

        JdbcSourceConfig config = factory.create(0);

        Assertions.assertNotNull(config);
        Assertions.assertEquals(500, config.getFetchSize());
    }

    @Test
    public void testCreateWithServerTimeZone() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");
        factory.serverTimeZone("Asia/Shanghai");

        JdbcSourceConfig config = factory.create(0);

        Assertions.assertNotNull(config);
        Assertions.assertEquals("Asia/Shanghai", config.getServerTimeZone());
    }

    @Test
    public void testCreateWithConnectTimeout() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");
        factory.connectTimeout(60000);

        JdbcSourceConfig config = factory.create(0);

        Assertions.assertNotNull(config);
        Assertions.assertEquals(60000L, config.getConnectTimeoutMillis());
    }

    @Test
    public void testCreateWithUseSelectCount() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");
        factory.useSelectCount(true);

        JdbcSourceConfig config = factory.create(0);

        Assertions.assertNotNull(config);
    }

    @Test
    public void testCreateWithSkipAnalyze() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");
        factory.skipAnalyze(true);

        JdbcSourceConfig config = factory.create(0);

        Assertions.assertNotNull(config);
    }

    @Test
    public void testCreateMissingHostnameThrowsException() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            factory.create(0);
        });
    }

    @Test
    public void testCreateMissingPortThrowsException() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.username("SYS");
        factory.password("yasdb_123");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            factory.create(0);
        });
    }

    @Test
    public void testDriverClassName() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");

        JdbcSourceConfig config = factory.create(0);

        Assertions.assertEquals("com.yashandb.jdbc.Driver", config.getDriverClassName());
    }

    @Test
    public void testCreateWithSubtaskId() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");

        JdbcSourceConfig config0 = factory.create(0);
        JdbcSourceConfig config1 = factory.create(1);

        Assertions.assertNotNull(config0);
        Assertions.assertNotNull(config1);
    }

    @Test
    public void testCreateWithExactlyOnce() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");
        factory.exactlyOnce(true);

        JdbcSourceConfig config = factory.create(0);

        Assertions.assertNotNull(config);
        Assertions.assertTrue(config.isExactlyOnce());
    }

    @Test
    public void testCreateWithConnectionPoolSize() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");
        factory.connectionPoolSize(20);

        JdbcSourceConfig config = factory.create(0);

        Assertions.assertNotNull(config);
        Assertions.assertEquals(20, config.getConnectionPoolSize());
    }

    @Test
    public void testCreateWithConnectMaxRetries() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");
        factory.connectMaxRetries(5);

        JdbcSourceConfig config = factory.create(0);

        Assertions.assertNotNull(config);
        Assertions.assertEquals(5, config.getConnectMaxRetries());
    }
}