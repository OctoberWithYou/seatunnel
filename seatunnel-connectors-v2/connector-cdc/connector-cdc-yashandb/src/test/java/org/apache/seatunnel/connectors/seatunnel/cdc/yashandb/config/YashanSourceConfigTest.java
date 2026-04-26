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

import org.apache.seatunnel.connectors.cdc.base.config.StartupConfig;
import org.apache.seatunnel.connectors.cdc.base.config.StopConfig;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

/** Test for {@link YashanSourceConfig}. */
public class YashanSourceConfigTest {

    @Test
    public void testConstructor() {
        YashanSourceConfig config = createConfig();
        Assertions.assertNotNull(config);
    }

    @Test
    public void testGetDriverClassName() {
        YashanSourceConfig config = createConfig();
        Assertions.assertEquals("com.yashandb.jdbc.Driver", config.getDriverClassName());
    }

    @Test
    public void testGetHostname() {
        YashanSourceConfig config = createConfig();
        Assertions.assertEquals("localhost", config.getHostname());
    }

    @Test
    public void testGetPort() {
        YashanSourceConfig config = createConfig();
        Assertions.assertEquals(1688, config.getPort());
    }

    @Test
    public void testGetUsername() {
        YashanSourceConfig config = createConfig();
        Assertions.assertEquals("SYS", config.getUsername());
    }

    @Test
    public void testGetPassword() {
        YashanSourceConfig config = createConfig();
        Assertions.assertEquals("yasdb_123", config.getPassword());
    }

    @Test
    public void testGetOriginUrl() {
        YashanSourceConfig config = createConfig();
        Assertions.assertEquals("jdbc:yasdb://localhost:1688/testdb", config.getOriginUrl());
    }

    @Test
    public void testGetFetchSize() {
        YashanSourceConfig config = createConfig();
        Assertions.assertEquals(1000, config.getFetchSize());
    }

    @Test
    public void testGetServerTimeZone() {
        YashanSourceConfig config = createConfig();
        Assertions.assertEquals("UTC", config.getServerTimeZone());
    }

    @Test
    public void testGetConnectTimeoutMillis() {
        YashanSourceConfig config = createConfig();
        Assertions.assertEquals(30000L, config.getConnectTimeoutMillis());
    }

    @Test
    public void testGetConnectMaxRetries() {
        YashanSourceConfig config = createConfig();
        Assertions.assertEquals(3, config.getConnectMaxRetries());
    }

    @Test
    public void testGetConnectionPoolSize() {
        YashanSourceConfig config = createConfig();
        Assertions.assertEquals(10, config.getConnectionPoolSize());
    }

    @Test
    public void testIsExactlyOnce() {
        YashanSourceConfig config = createConfig();
        Assertions.assertTrue(config.isExactlyOnce());
    }

    @Test
    public void testGetDatabaseList() {
        YashanSourceConfig config = createConfig();
        Assertions.assertNotNull(config.getDatabaseList());
        Assertions.assertEquals(1, config.getDatabaseList().size());
        Assertions.assertEquals("testdb", config.getDatabaseList().get(0));
    }

    @Test
    public void testGetTableList() {
        YashanSourceConfig config = createConfig();
        Assertions.assertNotNull(config.getTableList());
        Assertions.assertEquals(1, config.getTableList().size());
        Assertions.assertEquals("PUBLIC.TEST_TABLE", config.getTableList().get(0));
    }

    @Test
    public void testGetSplitSize() {
        YashanSourceConfig config = createConfig();
        Assertions.assertEquals(8096, config.getSplitSize());
    }

    @Test
    public void testGetDbzConnectorConfigReturnsNull() {
        YashanSourceConfig config = createConfig();
        Assertions.assertNull(config.getDbzConnectorConfig());
    }

    @Test
    public void testGetStartupConfig() {
        YashanSourceConfig config = createConfig();
        Assertions.assertNotNull(config.getStartupConfig());
    }

    @Test
    public void testGetStopConfig() {
        YashanSourceConfig config = createConfig();
        Assertions.assertNotNull(config.getStopConfig());
    }

    @Test
    public void testGetDistributionFactorUpper() {
        YashanSourceConfig config = createConfig();
        Assertions.assertEquals(10.0, config.getDistributionFactorUpper());
    }

    @Test
    public void testGetDistributionFactorLower() {
        YashanSourceConfig config = createConfig();
        Assertions.assertEquals(0.1, config.getDistributionFactorLower());
    }

    @Test
    public void testGetSampleShardingThreshold() {
        YashanSourceConfig config = createConfig();
        Assertions.assertEquals(10000, config.getSampleShardingThreshold());
    }

    @Test
    public void testGetInverseSamplingRate() {
        YashanSourceConfig config = createConfig();
        Assertions.assertEquals(1000, config.getInverseSamplingRate());
    }

    private YashanSourceConfig createConfig() {
        StartupConfig startupConfig = new StartupConfig();
        StopConfig stopConfig = new StopConfig();

        return new YashanSourceConfig(
                startupConfig,
                stopConfig,
                Collections.singletonList("testdb"),
                Collections.singletonList("PUBLIC.TEST_TABLE"),
                8096,
                new HashMap<>(),
                10.0,
                0.1,
                10000,
                1000,
                new Properties(),
                "com.yashandb.jdbc.Driver",
                "localhost",
                1688,
                "SYS",
                "yasdb_123",
                "jdbc:yasdb://localhost:1688/testdb",
                1000,
                "UTC",
                30000L,
                3,
                10,
                true);
    }
}