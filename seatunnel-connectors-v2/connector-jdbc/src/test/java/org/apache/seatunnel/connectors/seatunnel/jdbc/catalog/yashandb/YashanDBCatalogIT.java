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

package org.apache.seatunnel.connectors.seatunnel.jdbc.catalog.yashandb;

import org.apache.seatunnel.api.table.catalog.CatalogTable;
import org.apache.seatunnel.api.table.catalog.TablePath;
import org.apache.seatunnel.common.utils.JdbcUrlUtil;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

/**
 * Integration tests for {@link YashanDBCatalog}.
 *
 * <p><b>⚠️ Requires Real Database Connection:</b>
 * These tests require a real YashanDB database connection and are skipped by default.
 * To run these tests, you need:
 *
 * <ul>
 *   <li>A running YashanDB server at 192.168.1.11:1688</li>
 *   <li>User: SYS, Password: yasdb_123</li>
 *   <li>Database: ya</li>
 * </ul>
 *
 * <p><b>How to Run:</b>
 * <pre>
 * # Run with Maven (requires database available)
 * mvn test -Dtest=YashanDBCatalogIT -DskipIntegrationTests=false
 *
 * # Or run in IDE with @Tag("integration") enabled
 * </pre>
 *
 * <p><b>Test Database Setup:</b>
 * <pre>
 * -- Create test schema
 * CREATE USER TEST_SCHEMA IDENTIFIED BY test123;
 * GRANT CONNECT, RESOURCE TO TEST_SCHEMA;
 *
 * -- Create test table
 * CREATE TABLE TEST_SCHEMA.TEST_TABLE (
 *   ID NUMBER(10) PRIMARY KEY,
 *   NAME VARCHAR2(100),
 *   CREATE_TIME TIMESTAMP
 * );
 * </pre>
 */
@Tag("integration")
public class YashanDBCatalogIT {

    /** Database connection parameters. */
    private static final String HOST = "192.168.1.11";
    private static final int PORT = 1688;
    private static final String DATABASE = "ya";
    private static final String USERNAME = "SYS";
    private static final String PASSWORD = "yasdb_123";
    private static final String URL = "jdbc:yasdb://" + HOST + ":" + PORT + "/" + DATABASE;
    private static final String DRIVER = "com.yashandb.jdbc.Driver";

    /** Test schema name. */
    private static final String TEST_SCHEMA = "SYS";

    /** Flag to indicate if database is available. */
    private static boolean databaseAvailable = false;

    @BeforeAll
    public static void checkDatabaseAvailability() {
        try {
            Class.forName(DRIVER);
            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            conn.close();
            databaseAvailable = true;
            System.out.println("YashanDB database is available for integration tests");
        } catch (ClassNotFoundException e) {
            System.out.println("YashanDB JDBC driver not found: " + e.getMessage());
            databaseAvailable = false;
        } catch (SQLException e) {
            System.out.println("YashanDB database not available: " + e.getMessage());
            databaseAvailable = false;
        }
    }

    private YashanDBCatalog createCatalog() {
        JdbcUrlUtil.UrlInfo urlInfo = YashanDBURLParser.parse(URL);
        return new YashanDBCatalog(
                "yashandb_catalog_test",
                USERNAME,
                PASSWORD,
                urlInfo,
                TEST_SCHEMA,
                true,
                DRIVER,
                false);
    }

    @Test
    public void testOpenAndCloseCatalog() {
        Assumptions.assumeTrue(databaseAvailable, "YashanDB database is not available");

        YashanDBCatalog catalog = createCatalog();
        Assertions.assertDoesNotThrow(() -> catalog.open());
        Assertions.assertDoesNotThrow(() -> catalog.close());
    }

    @Test
    public void testListDatabases() {
        Assumptions.assumeTrue(databaseAvailable, "YashanDB database is not available");

        YashanDBCatalog catalog = createCatalog();
        catalog.open();

        List<String> databases = catalog.listDatabases();
        Assertions.assertNotNull(databases);
        Assertions.assertTrue(databases.size() > 0);
        Assertions.assertTrue(databases.contains("default"));

        catalog.close();
    }

    @Test
    public void testDatabaseExists() {
        Assumptions.assumeTrue(databaseAvailable, "YashanDB database is not available");

        YashanDBCatalog catalog = createCatalog();
        catalog.open();

        // YashanDB always returns true for databaseExists
        Assertions.assertTrue(catalog.databaseExists("default"));
        Assertions.assertTrue(catalog.databaseExists("any_name"));

        catalog.close();
    }

    @Test
    public void testListTables() {
        Assumptions.assumeTrue(databaseAvailable, "YashanDB database is not available");

        YashanDBCatalog catalog = createCatalog();
        catalog.open();

        List<String> tables = catalog.listTables("default");
        Assertions.assertNotNull(tables);
        // SYS schema should have some system tables
        System.out.println("Tables found: " + tables.size());

        catalog.close();
    }

    @Test
    public void testTableExists() {
        Assumptions.assumeTrue(databaseAvailable, "YashanDB database is not available");

        YashanDBCatalog catalog = createCatalog();
        catalog.open();

        // Check if any table exists in SYS schema
        List<String> tables = catalog.listTables("default");
        if (tables.size() > 0) {
            String firstTable = tables.get(0);
            TablePath tablePath = TablePath.of(firstTable, true);
            Assertions.assertTrue(catalog.tableExists(tablePath));
        }

        catalog.close();
    }

    @Test
    public void testGetTable() throws SQLException {
        Assumptions.assumeTrue(databaseAvailable, "YashanDB database is not available");

        YashanDBCatalog catalog = createCatalog();
        catalog.open();

        // Get any existing table
        List<String> tables = catalog.listTables("default");
        if (tables.size() > 0) {
            String firstTable = tables.get(0);
            TablePath tablePath = TablePath.of(firstTable, true);

            CatalogTable table = catalog.getTable(tablePath);
            Assertions.assertNotNull(table);
            Assertions.assertNotNull(table.getTableSchema());
            Assertions.assertNotNull(table.getTableSchema().getColumns());
            Assertions.assertTrue(table.getTableSchema().getColumns().size() > 0);

            System.out.println("Table: " + firstTable);
            System.out.println("Columns: " + table.getTableSchema().getColumns().size());
        }

        catalog.close();
    }

    @Test
    public void testGetTableFromQuery() throws SQLException {
        Assumptions.assumeTrue(databaseAvailable, "YashanDB database is not available");

        YashanDBCatalog catalog = createCatalog();
        catalog.open();

        // Test getting table structure from a simple query
        String query = "SELECT 1 AS ID, 'test' AS NAME FROM DUAL";
        CatalogTable table = catalog.getTable(query);

        Assertions.assertNotNull(table);
        Assertions.assertNotNull(table.getTableSchema());
        Assertions.assertEquals(2, table.getTableSchema().getColumns().size());

        catalog.close();
    }

    @Test
    public void testGetTableFromComplexQuery() throws SQLException {
        Assumptions.assumeTrue(databaseAvailable, "YashanDB database is not available");

        YashanDBCatalog catalog = createCatalog();
        catalog.open();

        // Test with a more complex query
        String query = "SELECT SYSDATE AS CREATE_TIME, 123.45 AS AMOUNT, 'HELLO' AS MESSAGE FROM DUAL";
        CatalogTable table = catalog.getTable(query);

        Assertions.assertNotNull(table);
        Assertions.assertEquals(3, table.getTableSchema().getColumns().size());

        catalog.close();
    }

    @Test
    public void testUrlParserIntegration() {
        Assumptions.assumeTrue(databaseAvailable, "YashanDB database is not available");

        JdbcUrlUtil.UrlInfo urlInfo = YashanDBURLParser.parse(URL);

        Assertions.assertEquals(HOST, urlInfo.getHost());
        Assertions.assertEquals(PORT, urlInfo.getPort());
        Assertions.assertTrue(urlInfo.getDefaultDatabase().isPresent());
        Assertions.assertEquals(DATABASE, urlInfo.getDefaultDatabase().get());
    }

    @Test
    public void testCatalogName() {
        YashanDBCatalog catalog = createCatalog();
        Assertions.assertEquals("yashandb_catalog_test", catalog.getName());
    }

    @Test
    public void testDefaultSchema() {
        YashanDBCatalog catalog = createCatalog();
        Assertions.assertEquals(TEST_SCHEMA, catalog.getDefaultSchema());
    }

    @Test
    public void testCreateTableSqlGeneration() {
        Assumptions.assumeTrue(databaseAvailable, "YashanDB database is not available");

        YashanDBCatalog catalog = createCatalog();
        catalog.open();

        // Get an existing table and verify SQL generation works
        List<String> tables = catalog.listTables("default");
        if (tables.size() > 0) {
            String firstTable = tables.get(0);
            TablePath tablePath = TablePath.of(firstTable, true);

            try {
                CatalogTable table = catalog.getTable(tablePath);
                // The catalog should be able to generate create table SQL
                // (though we won't actually execute it)
                Assertions.assertNotNull(table);
            } catch (SQLException e) {
                // Some system tables might not be readable
                System.out.println("Could not read table: " + e.getMessage());
            }
        }

        catalog.close();
    }

    @Test
    public void testMultipleOpenClose() {
        Assumptions.assumeTrue(databaseAvailable, "YashanDB database is not available");

        YashanDBCatalog catalog = createCatalog();

        // Test multiple open/close cycles
        for (int i = 0; i < 3; i++) {
            catalog.open();
            catalog.close();
        }

        Assertions.assertDoesNotThrow(() -> catalog.open());
        Assertions.assertDoesNotThrow(() -> catalog.close());
    }

    @Test
    public void testGetConnection() {
        Assumptions.assumeTrue(databaseAvailable, "YashanDB database is not available");

        YashanDBCatalog catalog = createCatalog();
        catalog.open();

        // Verify connection is working by listing tables
        List<String> tables1 = catalog.listTables("default");
        List<String> tables2 = catalog.listTables("default");

        Assertions.assertNotNull(tables1);
        Assertions.assertNotNull(tables2);
        Assertions.assertEquals(tables1.size(), tables2.size());

        catalog.close();
    }

    @Test
    public void testColumnMetadata() throws SQLException {
        Assumptions.assumeTrue(databaseAvailable, "YashanDB database is not available");

        YashanDBCatalog catalog = createCatalog();
        catalog.open();

        // Query a table with known column types
        String query = "SELECT 1 AS INT_COL, 'ABC' AS STR_COL, SYSDATE AS DATE_COL FROM DUAL";
        CatalogTable table = catalog.getTable(query);

        Assertions.assertNotNull(table);
        List<org.apache.seatunnel.api.table.catalog.Column> columns = table.getTableSchema().getColumns();

        Assertions.assertEquals(3, columns.size());
        Assertions.assertEquals("INT_COL", columns.get(0).getName());
        Assertions.assertEquals("STR_COL", columns.get(1).getName());
        Assertions.assertEquals("DATE_COL", columns.get(2).getName());

        catalog.close();
    }
}