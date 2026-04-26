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

package org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.utils;

import io.debezium.relational.TableId;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Test for {@link YashanUtils}. */
public class YashanUtilsTest {

    @Test
    public void testDiscoverTablesNullTableList() {
        MockJdbcSourceConfig config = new MockJdbcSourceConfig(
                Collections.singletonList("default"),
                null);

        List<TableId> result = YashanUtils.discoverTables(config);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    public void testDiscoverTablesEmptyTableList() {
        MockJdbcSourceConfig config = new MockJdbcSourceConfig(
                Collections.singletonList("default"),
                Collections.emptyList());

        List<TableId> result = YashanUtils.discoverTables(config);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    public void testDiscoverTablesSingleTable() {
        MockJdbcSourceConfig config = new MockJdbcSourceConfig(
                Collections.singletonList("default"),
                Collections.singletonList("SCHEMA.TABLE"));

        List<TableId> result = YashanUtils.discoverTables(config);
        Assertions.assertEquals(1, result.size());
        TableId tableId = result.get(0);
        Assertions.assertEquals("default", tableId.catalog());
        Assertions.assertEquals("SCHEMA", tableId.schema());
        Assertions.assertEquals("TABLE", tableId.table());
    }

    @Test
    public void testDiscoverTablesFullyQualified() {
        MockJdbcSourceConfig config = new MockJdbcSourceConfig(
                Collections.singletonList("DB"),
                Collections.singletonList("DB.SCHEMA.TABLE"));

        List<TableId> result = YashanUtils.discoverTables(config);
        Assertions.assertEquals(1, result.size());
        TableId tableId = result.get(0);
        Assertions.assertEquals("DB", tableId.catalog());
        Assertions.assertEquals("SCHEMA", tableId.schema());
        Assertions.assertEquals("TABLE", tableId.table());
    }

    @Test
    public void testDiscoverTablesOnlyTableName() {
        MockJdbcSourceConfig config = new MockJdbcSourceConfig(
                Collections.singletonList("default"),
                Collections.singletonList("TABLE"));

        List<TableId> result = YashanUtils.discoverTables(config);
        Assertions.assertEquals(1, result.size());
        TableId tableId = result.get(0);
        Assertions.assertEquals("default", tableId.catalog());
        Assertions.assertNull(tableId.schema());
        Assertions.assertEquals("TABLE", tableId.table());
    }

    @Test
    public void testDiscoverTablesMultipleTables() {
        MockJdbcSourceConfig config = new MockJdbcSourceConfig(
                Collections.singletonList("default"),
                Arrays.asList("SCHEMA1.TABLE1", "SCHEMA2.TABLE2", "SCHEMA3.TABLE3"));

        List<TableId> result = YashanUtils.discoverTables(config);
        Assertions.assertEquals(3, result.size());
    }

    @Test
    public void testDiscoverTablesNullDatabaseList() {
        MockJdbcSourceConfig config = new MockJdbcSourceConfig(
                null,
                Collections.singletonList("SCHEMA.TABLE"));

        List<TableId> result = YashanUtils.discoverTables(config);
        Assertions.assertEquals(1, result.size());
        // Should use "default" when databaseList is null
        Assertions.assertEquals("default", result.get(0).catalog());
    }

    @Test
    public void testDiscoverTablesEmptyDatabaseList() {
        MockJdbcSourceConfig config = new MockJdbcSourceConfig(
                Collections.emptyList(),
                Collections.singletonList("SCHEMA.TABLE"));

        List<TableId> result = YashanUtils.discoverTables(config);
        Assertions.assertEquals(1, result.size());
        // Should use "default" when databaseList is empty
        Assertions.assertEquals("default", result.get(0).catalog());
    }

    @Test
    public void testDiscoverTablesFirstDatabaseUsed() {
        MockJdbcSourceConfig config = new MockJdbcSourceConfig(
                Arrays.asList("DB1", "DB2", "DB3"),
                Collections.singletonList("SCHEMA.TABLE"));

        List<TableId> result = YashanUtils.discoverTables(config);
        Assertions.assertEquals(1, result.size());
        // Should use first database
        Assertions.assertEquals("DB1", result.get(0).catalog());
    }

    @Test
    public void testDiscoverTablesMixedFormats() {
        MockJdbcSourceConfig config = new MockJdbcSourceConfig(
                Collections.singletonList("default"),
                Arrays.asList("DB.SCHEMA.TABLE1", "SCHEMA.TABLE2", "TABLE3"));

        List<TableId> result = YashanUtils.discoverTables(config);
        Assertions.assertEquals(3, result.size());

        // DB.SCHEMA.TABLE1 -> catalog=DB, schema=SCHEMA, table=TABLE1
        Assertions.assertEquals("DB", result.get(0).catalog());
        Assertions.assertEquals("SCHEMA", result.get(0).schema());
        Assertions.assertEquals("TABLE1", result.get(0).table());

        // SCHEMA.TABLE2 -> catalog=default, schema=SCHEMA, table=TABLE2
        Assertions.assertEquals("default", result.get(1).catalog());
        Assertions.assertEquals("SCHEMA", result.get(1).schema());
        Assertions.assertEquals("TABLE2", result.get(1).table());

        // TABLE3 -> catalog=default, schema=null, table=TABLE3
        Assertions.assertEquals("default", result.get(2).catalog());
        Assertions.assertNull(result.get(2).schema());
        Assertions.assertEquals("TABLE3", result.get(2).table());
    }

    @Test
    public void testQueryTableSchemaThrowsException() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            YashanUtils.queryTableSchema(null, null);
        });
    }

    /** Mock implementation of JdbcSourceConfig for testing. */
    private static class MockJdbcSourceConfig implements org.apache.seatunnel.connectors.cdc.base.config.JdbcSourceConfig {

        private final List<String> databaseList;
        private final List<String> tableList;

        MockJdbcSourceConfig(List<String> databaseList, List<String> tableList) {
            this.databaseList = databaseList;
            this.tableList = tableList;
        }

        @Override
        public List<String> getDatabaseList() {
            return databaseList;
        }

        @Override
        public List<String> getTableList() {
            return tableList;
        }

        @Override
        public org.apache.seatunnel.connectors.cdc.base.config.StartupConfig getStartupConfig() {
            return null;
        }

        @Override
        public org.apache.seatunnel.connectors.cdc.base.config.StopConfig getStopConfig() {
            return null;
        }

        @Override
        public int getSplitSize() {
            return 0;
        }

        @Override
        public java.util.Map<String, String> getSplitColumn() {
            return Collections.emptyMap();
        }

        @Override
        public double getDistributionFactorUpper() {
            return 0;
        }

        @Override
        public double getDistributionFactorLower() {
            return 0;
        }

        @Override
        public int getSampleShardingThreshold() {
            return 0;
        }

        @Override
        public int getInverseSamplingRate() {
            return 0;
        }

        @Override
        public java.util.Properties getDbzProperties() {
            return new java.util.Properties();
        }

        @Override
        public String getDriverClassName() {
            return "com.yashandb.jdbc.Driver";
        }

        @Override
        public String getHostname() {
            return "localhost";
        }

        @Override
        public int getPort() {
            return 1688;
        }

        @Override
        public String getUsername() {
            return "user";
        }

        @Override
        public String getPassword() {
            return "password";
        }

        @Override
        public String getOriginUrl() {
            return null;
        }

        @Override
        public int getFetchSize() {
            return 100;
        }

        @Override
        public String getServerTimeZone() {
            return "UTC";
        }

        @Override
        public long getConnectTimeoutMillis() {
            return 30000;
        }

        @Override
        public int getConnectMaxRetries() {
            return 3;
        }

        @Override
        public int getConnectionPoolSize() {
            return 10;
        }

        @Override
        public boolean isExactlyOnce() {
            return false;
        }

        @Override
        public io.debezium.relational.RelationalDatabaseConnectorConfig getDbzConnectorConfig() {
            return null;
        }
    }
}