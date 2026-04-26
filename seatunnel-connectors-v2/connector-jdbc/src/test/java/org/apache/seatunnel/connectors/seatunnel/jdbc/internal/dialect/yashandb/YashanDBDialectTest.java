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

package org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.yashandb;

import org.apache.seatunnel.api.table.catalog.TablePath;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Test for {@link YashanDBDialect}. */
public class YashanDBDialectTest {

    private final YashanDBDialect dialect = new YashanDBDialect();

    @Test
    public void testDialectName() {
        Assertions.assertEquals("YashanDB", dialect.dialectName());
    }

    @Test
    public void testQuoteIdentifierSimple() {
        String result = dialect.quoteIdentifier("column_name");
        Assertions.assertEquals("\"column_name\"", result);
    }

    @Test
    public void testQuoteIdentifierWithSchema() {
        String result = dialect.quoteIdentifier("schema.table");
        Assertions.assertEquals("\"schema\".\"table\"", result);
    }

    @Test
    public void testQuoteIdentifierWithThreeParts() {
        String result = dialect.quoteIdentifier("db.schema.table");
        Assertions.assertEquals("\"db\".\"schema\".\"table\"", result);
    }

    @Test
    public void testTableIdentifier() {
        String result = dialect.tableIdentifier("database", "table_name");
        Assertions.assertEquals("\"table_name\"", result);
    }

    @Test
    public void testTableIdentifierWithTablePath() {
        TablePath tablePath = TablePath.of("SCHEMA.TABLE", true);
        String result = dialect.tableIdentifier(tablePath);
        Assertions.assertEquals("\"SCHEMA\".\"TABLE\"", result);
    }

    @Test
    public void testParseTablePath() {
        TablePath result = dialect.parse("SCHEMA.TABLE");
        Assertions.assertEquals("SCHEMA", result.getSchemaName());
        Assertions.assertEquals("TABLE", result.getTableName());
    }

    @Test
    public void testDualTable() {
        Assertions.assertEquals(" FROM dual ", dialect.dualTable());
    }

    @Test
    public void testHashModForField() {
        String result = dialect.hashModForField("column_name", 10);
        Assertions.assertEquals("MOD(ORA_HASH(\"column_name\"),10)", result);
    }

    @Test
    public void testHashModForFieldWithQuotedName() {
        String result = dialect.hashModForField("my_column", 100);
        Assertions.assertTrue(result.contains("ORA_HASH"));
        Assertions.assertTrue(result.contains("MOD"));
        Assertions.assertTrue(result.contains("100"));
    }

    @Test
    public void testGetRowConverter() {
        Assertions.assertNotNull(dialect.getRowConverter());
        Assertions.assertTrue(dialect.getRowConverter() instanceof YashanDBJdbcRowConverter);
    }

    @Test
    public void testGetTypeConverter() {
        Assertions.assertNotNull(dialect.getTypeConverter());
        Assertions.assertTrue(dialect.getTypeConverter() instanceof YashanDBTypeConverter);
    }

    @Test
    public void testGetJdbcDialectTypeMapper() {
        Assertions.assertNotNull(dialect.getJdbcDialectTypeMapper());
        Assertions.assertTrue(dialect.getJdbcDialectTypeMapper() instanceof YashanDBTypeMapper);
    }

    @Test
    public void testUpsertStatement() {
        String[] fieldNames = {"id", "name", "value"};
        String[] uniqueKeyFields = {"id"};

        var result = dialect.getUpsertStatement("database", "table_name", fieldNames, uniqueKeyFields);

        Assertions.assertTrue(result.isPresent());
        String upsertSql = result.get();
        Assertions.assertTrue(upsertSql.contains("MERGE INTO"));
        Assertions.assertTrue(upsertSql.contains("TARGET"));
        Assertions.assertTrue(upsertSql.contains("SOURCE"));
        Assertions.assertTrue(upsertSql.contains("WHEN MATCHED THEN"));
        Assertions.assertTrue(upsertSql.contains("UPDATE SET"));
        Assertions.assertTrue(upsertSql.contains("WHEN NOT MATCHED THEN"));
        Assertions.assertTrue(upsertSql.contains("INSERT"));
    }

    @Test
    public void testUpsertStatementWithMultipleKeys() {
        String[] fieldNames = {"id1", "id2", "name", "value"};
        String[] uniqueKeyFields = {"id1", "id2"};

        var result = dialect.getUpsertStatement("database", "table_name", fieldNames, uniqueKeyFields);

        Assertions.assertTrue(result.isPresent());
        String upsertSql = result.get();
        Assertions.assertTrue(upsertSql.contains("MERGE INTO"));
        Assertions.assertTrue(upsertSql.contains("id1"));
        Assertions.assertTrue(upsertSql.contains("id2"));
    }

    @Test
    public void testUpsertStatementWithDUAL() {
        String[] fieldNames = {"id", "name"};
        String[] uniqueKeyFields = {"id"};

        var result = dialect.getUpsertStatement("database", "table_name", fieldNames, uniqueKeyFields);

        Assertions.assertTrue(result.isPresent());
        String upsertSql = result.get();
        Assertions.assertTrue(upsertSql.contains("FROM DUAL"));
    }

    @Test
    public void testFieldIdeUppercase() {
        YashanDBDialect uppercaseDialect = new YashanDBDialect("uppercase");
        String result = uppercaseDialect.quoteIdentifier("column_name");
        Assertions.assertEquals("\"COLUMN_NAME\"", result);
    }

    @Test
    public void testFieldIdeLowercase() {
        YashanDBDialect lowercaseDialect = new YashanDBDialect("lowercase");
        String result = lowercaseDialect.quoteIdentifier("COLUMN_NAME");
        Assertions.assertEquals("\"column_name\"", result);
    }

    @Test
    public void testFieldIdeOriginal() {
        YashanDBDialect originalDialect = new YashanDBDialect("original");
        String result = originalDialect.quoteIdentifier("Column_Name");
        Assertions.assertEquals("\"Column_Name\"", result);
    }

    @Test
    public void testHandleBlobAsString() {
        YashanDBDialect blobAsStringDialect = new YashanDBDialect("original", true);
        Assertions.assertNotNull(blobAsStringDialect.getTypeConverter());
    }

    @Test
    public void testHandleBlobAsBytes() {
        YashanDBDialect blobAsBytesDialect = new YashanDBDialect("original", false);
        Assertions.assertNotNull(blobAsBytesDialect.getTypeConverter());
    }

    @Test
    public void testGetCollateSqlWithCollate() {
        String result = dialect.getCollateSql("BINARY");
        Assertions.assertTrue(result.contains("NLSSORT"));
        Assertions.assertTrue(result.contains("NLS_SORT=BINARY"));
    }

    @Test
    public void testGetCollateSqlWithoutCollate() {
        String result = dialect.getCollateSql(null);
        Assertions.assertEquals("char_val", result);
    }

    @Test
    public void testGetCollateSqlWithEmptyCollate() {
        String result = dialect.getCollateSql("");
        Assertions.assertEquals("char_val", result);
    }

    @Test
    public void testGetCollateSqlWithCaseInsensitive() {
        String result = dialect.getCollateSql("CI");
        Assertions.assertTrue(result.contains("NLSSORT"));
        Assertions.assertTrue(result.contains("NLS_SORT=CI"));
    }

    @Test
    public void testDefaultConstructor() {
        YashanDBDialect defaultDialect = new YashanDBDialect();
        Assertions.assertEquals("YashanDB", defaultDialect.dialectName());
        Assertions.assertNotNull(defaultDialect.getRowConverter());
        Assertions.assertNotNull(defaultDialect.getTypeConverter());
    }

    @Test
    public void testConstructorWithFieldIdeOnly() {
        YashanDBDialect fieldIdeDialect = new YashanDBDialect("uppercase");
        Assertions.assertEquals("YashanDB", fieldIdeDialect.dialectName());
    }

    @Test
    public void testConstructorWithFieldIdeAndBlobHandling() {
        YashanDBDialect fullConfigDialect = new YashanDBDialect("lowercase", true);
        Assertions.assertEquals("YashanDB", fullConfigDialect.dialectName());
    }
}