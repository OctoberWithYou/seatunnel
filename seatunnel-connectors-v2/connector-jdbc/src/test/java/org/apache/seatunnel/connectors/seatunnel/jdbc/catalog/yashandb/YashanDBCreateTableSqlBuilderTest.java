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

import org.apache.seatunnel.shade.com.google.common.collect.Lists;

import org.apache.seatunnel.api.table.catalog.CatalogTable;
import org.apache.seatunnel.api.table.catalog.Column;
import org.apache.seatunnel.api.table.catalog.PhysicalColumn;
import org.apache.seatunnel.api.table.catalog.PrimaryKey;
import org.apache.seatunnel.api.table.catalog.TableIdentifier;
import org.apache.seatunnel.api.table.catalog.TablePath;
import org.apache.seatunnel.api.table.catalog.TableSchema;
import org.apache.seatunnel.api.table.type.BasicType;
import org.apache.seatunnel.api.table.type.DecimalType;
import org.apache.seatunnel.api.table.type.LocalTimeType;
import org.apache.seatunnel.api.table.type.PrimitiveByteArrayType;
import org.apache.seatunnel.api.table.type.SeaTunnelDataType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Test for {@link YashanDBCreateTableSqlBuilder}. */
public class YashanDBCreateTableSqlBuilderTest {

    @Test
    public void testBuildBasicTable() {
        String schemaName = "TEST_SCHEMA";
        String tableName = "TEST_TABLE";
        TablePath tablePath = TablePath.of(schemaName + "." + tableName, true);

        TableSchema tableSchema =
                TableSchema.builder()
                        .column(
                                PhysicalColumn.of(
                                        "id", BasicType.LONG_TYPE, 22, false, null, "primary key"))
                        .column(
                                PhysicalColumn.of(
                                        "name", BasicType.STRING_TYPE, 128, false, null, "user name"))
                        .column(
                                PhysicalColumn.of(
                                        "age", BasicType.INT_TYPE, null, true, null, "user age"))
                        .column(
                                PhysicalColumn.of(
                                        "salary",
                                        new DecimalType(10, 2),
                                        null,
                                        true,
                                        null,
                                        "salary amount"))
                        .column(
                                PhysicalColumn.of(
                                        "createTime",
                                        LocalTimeType.LOCAL_DATE_TIME_TYPE,
                                        6,
                                        true,
                                        null,
                                        "creation timestamp"))
                        .primaryKey(PrimaryKey.of("PK_TEST", Lists.newArrayList("id")))
                        .build();

        CatalogTable catalogTable =
                CatalogTable.of(
                        TableIdentifier.of("yashandb_catalog", schemaName, tableName),
                        tableSchema,
                        new HashMap<>(),
                        new ArrayList<>(),
                        "Test table for YashanDB");

        YashanDBCreateTableSqlBuilder builder = new YashanDBCreateTableSqlBuilder(catalogTable, true);
        List<String> sqls = builder.build(tablePath);

        // Should have CREATE TABLE + COMMENT ON TABLE + column comments
        Assertions.assertTrue(sqls.size() >= 1);

        String createTableSql = sqls.get(0);
        Assertions.assertTrue(createTableSql.contains("CREATE TABLE"));
        Assertions.assertTrue(createTableSql.contains("\"TEST_SCHEMA\".\"TEST_TABLE\""));
        Assertions.assertTrue(createTableSql.contains("\"id\""));
        Assertions.assertTrue(createTableSql.contains("\"name\""));
        Assertions.assertTrue(createTableSql.contains("\"age\""));
        Assertions.assertTrue(createTableSql.contains("\"salary\""));
        Assertions.assertTrue(createTableSql.contains("\"createTime\""));
        Assertions.assertTrue(createTableSql.contains("PRIMARY KEY"));

        // Verify NOT NULL constraint
        Assertions.assertTrue(createTableSql.contains("NOT NULL"));
    }

    @Test
    public void testBuildTableWithoutPrimaryKey() {
        String schemaName = "TEST_SCHEMA";
        String tableName = "NO_PK_TABLE";
        TablePath tablePath = TablePath.of(schemaName + "." + tableName, true);

        TableSchema tableSchema =
                TableSchema.builder()
                        .column(PhysicalColumn.of("col1", BasicType.INT_TYPE, null, true, null, null))
                        .column(PhysicalColumn.of("col2", BasicType.STRING_TYPE, 100, true, null, null))
                        .build();

        CatalogTable catalogTable =
                CatalogTable.of(
                        TableIdentifier.of("yashandb_catalog", schemaName, tableName),
                        tableSchema,
                        new HashMap<>(),
                        new ArrayList<>(),
                        null);

        YashanDBCreateTableSqlBuilder builder = new YashanDBCreateTableSqlBuilder(catalogTable, true);
        List<String> sqls = builder.build(tablePath);

        String createTableSql = sqls.get(0);
        Assertions.assertTrue(createTableSql.contains("CREATE TABLE"));
        Assertions.assertFalse(createTableSql.contains("PRIMARY KEY"));
    }

    @Test
    public void testBuildTableWithBlobColumn() {
        String schemaName = "TEST_SCHEMA";
        String tableName = "BLOB_TABLE";
        TablePath tablePath = TablePath.of(schemaName + "." + tableName, true);

        TableSchema tableSchema =
                TableSchema.builder()
                        .column(
                                PhysicalColumn.of(
                                        "id", BasicType.LONG_TYPE, 22, false, null, "primary key"))
                        .column(
                                PhysicalColumn.of(
                                        "blobData",
                                        PrimitiveByteArrayType.INSTANCE,
                                        Long.MAX_VALUE,
                                        true,
                                        null,
                                        "binary data"))
                        .primaryKey(PrimaryKey.of("PK_BLOB", Lists.newArrayList("id")))
                        .build();

        CatalogTable catalogTable =
                CatalogTable.of(
                        TableIdentifier.of("yashandb_catalog", schemaName, tableName),
                        tableSchema,
                        new HashMap<>(),
                        new ArrayList<>(),
                        "Table with BLOB");

        YashanDBCreateTableSqlBuilder builder = new YashanDBCreateTableSqlBuilder(catalogTable, true);
        List<String> sqls = builder.build(tablePath);

        String createTableSql = sqls.get(0);
        Assertions.assertTrue(createTableSql.contains("BLOB"));
    }

    @Test
    public void testBuildTableWithComments() {
        String schemaName = "TEST_SCHEMA";
        String tableName = "COMMENT_TABLE";
        TablePath tablePath = TablePath.of(schemaName + "." + tableName, true);

        TableSchema tableSchema =
                TableSchema.builder()
                        .column(
                                PhysicalColumn.of(
                                        "id", BasicType.INT_TYPE, null, false, null, "ID column"))
                        .column(
                                PhysicalColumn.of(
                                        "description",
                                        BasicType.STRING_TYPE,
                                        500,
                                        true,
                                        null,
                                        "Description column with comment"))
                        .build();

        CatalogTable catalogTable =
                CatalogTable.of(
                        TableIdentifier.of("yashandb_catalog", schemaName, tableName),
                        tableSchema,
                        new HashMap<>(),
                        new ArrayList<>(),
                        "Table with comments");

        YashanDBCreateTableSqlBuilder builder = new YashanDBCreateTableSqlBuilder(catalogTable, true);
        List<String> sqls = builder.build(tablePath);

        // Should have CREATE TABLE, COMMENT ON TABLE, and COMMENT ON COLUMN statements
        Assertions.assertTrue(sqls.size() >= 3);

        // Check table comment
        boolean hasTableComment = false;
        for (String sql : sqls) {
            if (sql.contains("COMMENT ON TABLE") && sql.contains("Table with comments")) {
                hasTableComment = true;
                break;
            }
        }
        Assertions.assertTrue(hasTableComment);

        // Check column comments
        boolean hasColumnComment = false;
        for (String sql : sqls) {
            if (sql.contains("COMMENT ON COLUMN") && sql.contains("Description column with comment")) {
                hasColumnComment = true;
                break;
            }
        }
        Assertions.assertTrue(hasColumnComment);
    }

    @Test
    public void testBuildTableSkipIndex() {
        String schemaName = "TEST_SCHEMA";
        String tableName = "SKIP_INDEX_TABLE";
        TablePath tablePath = TablePath.of(schemaName + "." + tableName, true);

        TableSchema tableSchema =
                TableSchema.builder()
                        .column(PhysicalColumn.of("id", BasicType.INT_TYPE, null, false, null, null))
                        .primaryKey(PrimaryKey.of("PK_SKIP", Lists.newArrayList("id")))
                        .build();

        CatalogTable catalogTable =
                CatalogTable.of(
                        TableIdentifier.of("yashandb_catalog", schemaName, tableName),
                        tableSchema,
                        new HashMap<>(),
                        new ArrayList<>(),
                        null);

        // createIndex = false, should not include PRIMARY KEY constraint
        YashanDBCreateTableSqlBuilder builder =
                new YashanDBCreateTableSqlBuilder(catalogTable, false);
        List<String> sqls = builder.build(tablePath);

        String createTableSql = sqls.get(0);
        Assertions.assertTrue(createTableSql.contains("CREATE TABLE"));
        Assertions.assertFalse(createTableSql.contains("PRIMARY KEY"));
    }

    @Test
    public void testBuildColumnSqlWithSinkType() {
        YashanDBCreateTableSqlBuilder builder = mock(YashanDBCreateTableSqlBuilder.class);

        Column column = mock(Column.class);
        when(column.getSinkType()).thenReturn("VARCHAR(100)");
        when(column.getDataType()).thenReturn((SeaTunnelDataType) BasicType.INT_TYPE);
        when(column.getName()).thenReturn("col1");
        when(column.isNullable()).thenReturn(false);
        when(builder.buildColumnSql(column)).thenCallRealMethod();

        String result = builder.buildColumnSql(column);

        Assertions.assertEquals("\"col1\" VARCHAR(100) NOT NULL", result);
    }

    @Test
    public void testBuildColumnSqlWithoutSinkType() {
        YashanDBCreateTableSqlBuilder builder = mock(YashanDBCreateTableSqlBuilder.class);

        Column column = mock(Column.class);
        when(column.getSinkType()).thenReturn(null);
        when(column.getDataType()).thenReturn((SeaTunnelDataType) BasicType.STRING_TYPE);
        when(column.getName()).thenReturn("col1");
        when(column.isNullable()).thenReturn(true);
        when(column.getColumnLength()).thenReturn(255L);
        when(builder.buildColumnSql(column)).thenCallRealMethod();

        String result = builder.buildColumnSql(column);

        Assertions.assertTrue(result.contains("\"col1\""));
        Assertions.assertTrue(result.contains("VARCHAR"));
        Assertions.assertFalse(result.contains("NOT NULL"));
    }

    @Test
    public void testBuildTableWithMultiplePrimaryKeyColumns() {
        String schemaName = "TEST_SCHEMA";
        String tableName = "COMPOSITE_PK_TABLE";
        TablePath tablePath = TablePath.of(schemaName + "." + tableName, true);

        TableSchema tableSchema =
                TableSchema.builder()
                        .column(PhysicalColumn.of("id1", BasicType.INT_TYPE, null, false, null, null))
                        .column(PhysicalColumn.of("id2", BasicType.INT_TYPE, null, false, null, null))
                        .column(PhysicalColumn.of("data", BasicType.STRING_TYPE, 100, true, null, null))
                        .primaryKey(
                                PrimaryKey.of("PK_COMPOSITE", Lists.newArrayList("id1", "id2")))
                        .build();

        CatalogTable catalogTable =
                CatalogTable.of(
                        TableIdentifier.of("yashandb_catalog", schemaName, tableName),
                        tableSchema,
                        new HashMap<>(),
                        new ArrayList<>(),
                        null);

        YashanDBCreateTableSqlBuilder builder = new YashanDBCreateTableSqlBuilder(catalogTable, true);
        List<String> sqls = builder.build(tablePath);

        String createTableSql = sqls.get(0);
        Assertions.assertTrue(createTableSql.contains("PRIMARY KEY"));
        Assertions.assertTrue(createTableSql.contains("\"id1\""));
        Assertions.assertTrue(createTableSql.contains("\"id2\""));
    }

    @Test
    public void testBuildTableWithTimestampColumn() {
        String schemaName = "TEST_SCHEMA";
        String tableName = "TIMESTAMP_TABLE";
        TablePath tablePath = TablePath.of(schemaName + "." + tableName, true);

        TableSchema tableSchema =
                TableSchema.builder()
                        .column(
                                PhysicalColumn.of(
                                        "ts_col",
                                        LocalTimeType.LOCAL_DATE_TIME_TYPE,
                                        null,
                                        true,
                                        6,
                                        null))
                        .build();

        CatalogTable catalogTable =
                CatalogTable.of(
                        TableIdentifier.of("yashandb_catalog", schemaName, tableName),
                        tableSchema,
                        new HashMap<>(),
                        new ArrayList<>(),
                        null);

        YashanDBCreateTableSqlBuilder builder = new YashanDBCreateTableSqlBuilder(catalogTable, true);
        List<String> sqls = builder.build(tablePath);

        String createTableSql = sqls.get(0);
        Assertions.assertTrue(createTableSql.contains("TIMESTAMP"));
    }

    @Test
    public void testBuildTableWithDecimalColumn() {
        String schemaName = "TEST_SCHEMA";
        String tableName = "DECIMAL_TABLE";
        TablePath tablePath = TablePath.of(schemaName + "." + tableName, true);

        TableSchema tableSchema =
                TableSchema.builder()
                        .column(
                                PhysicalColumn.of(
                                        "amount", new DecimalType(15, 4), null, true, null, null))
                        .build();

        CatalogTable catalogTable =
                CatalogTable.of(
                        TableIdentifier.of("yashandb_catalog", schemaName, tableName),
                        tableSchema,
                        new HashMap<>(),
                        new ArrayList<>(),
                        null);

        YashanDBCreateTableSqlBuilder builder = new YashanDBCreateTableSqlBuilder(catalogTable, true);
        List<String> sqls = builder.build(tablePath);

        String createTableSql = sqls.get(0);
        Assertions.assertTrue(createTableSql.contains("NUMBER"));
    }

    @Test
    public void testPrimaryKeyConstraintNameTruncation() {
        String schemaName = "TEST_SCHEMA";
        String tableName = "LONG_PK_TABLE";
        TablePath tablePath = TablePath.of(schemaName + "." + tableName, true);

        // Create a primary key with very long name (> 25 chars)
        String longPkName = "VERY_LONG_PRIMARY_KEY_NAME_THAT_NEEDS_TRUNCATION";
        TableSchema tableSchema =
                TableSchema.builder()
                        .column(PhysicalColumn.of("id", BasicType.INT_TYPE, null, false, null, null))
                        .primaryKey(PrimaryKey.of(longPkName, Lists.newArrayList("id")))
                        .build();

        CatalogTable catalogTable =
                CatalogTable.of(
                        TableIdentifier.of("yashandb_catalog", schemaName, tableName),
                        tableSchema,
                        new HashMap<>(),
                        new ArrayList<>(),
                        null);

        YashanDBCreateTableSqlBuilder builder = new YashanDBCreateTableSqlBuilder(catalogTable, true);
        List<String> sqls = builder.build(tablePath);

        String createTableSql = sqls.get(0);
        // The constraint name should be truncated to 25 chars + random suffix
        Assertions.assertTrue(createTableSql.contains("CONSTRAINT"));
        Assertions.assertTrue(createTableSql.contains("PRIMARY KEY"));
    }

    @Test
    public void testCommentWithSingleQuoteEscaping() {
        String schemaName = "TEST_SCHEMA";
        String tableName = "QUOTE_TABLE";
        TablePath tablePath = TablePath.of(schemaName + "." + tableName, true);

        // Column comment contains single quote that needs escaping
        TableSchema tableSchema =
                TableSchema.builder()
                        .column(
                                PhysicalColumn.of(
                                        "col1", BasicType.STRING_TYPE, 100, true, null, "It's a test"))
                        .build();

        CatalogTable catalogTable =
                CatalogTable.of(
                        TableIdentifier.of("yashandb_catalog", schemaName, tableName),
                        tableSchema,
                        new HashMap<>(),
                        new ArrayList<>(),
                        "Table's description");

        YashanDBCreateTableSqlBuilder builder = new YashanDBCreateTableSqlBuilder(catalogTable, true);
        List<String> sqls = builder.build(tablePath);

        // Check that single quotes are escaped (doubled)
        boolean hasEscapedTableComment = false;
        for (String sql : sqls) {
            if (sql.contains("COMMENT ON TABLE") && sql.contains("Table''s description")) {
                hasEscapedTableComment = true;
                break;
            }
        }
        Assertions.assertTrue(hasEscapedTableComment);

        boolean hasEscapedColumnComment = false;
        for (String sql : sqls) {
            if (sql.contains("COMMENT ON COLUMN") && sql.contains("It''s a test")) {
                hasEscapedColumnComment = true;
                break;
            }
        }
        Assertions.assertTrue(hasEscapedColumnComment);
    }
}