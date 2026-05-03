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

import org.apache.seatunnel.shade.org.apache.commons.lang3.StringUtils;

import org.apache.seatunnel.api.table.catalog.CatalogTable;
import org.apache.seatunnel.api.table.catalog.Column;
import org.apache.seatunnel.api.table.catalog.PrimaryKey;
import org.apache.seatunnel.api.table.catalog.TablePath;
import org.apache.seatunnel.connectors.seatunnel.jdbc.catalog.utils.CatalogUtils;
import org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.DatabaseIdentifier;
import org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.yashandb.YashanDBTypeConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SQL builder for creating tables in YashanDB (崖山数据库).
 *
 * <p>This class generates the necessary SQL statements to create a table in YashanDB
 * based on a {@link CatalogTable} definition. It produces CREATE TABLE statements,
 * table comments, and column comments following YashanDB's Oracle-compatible syntax.
 *
 * <p><b>Generated SQL Structure:</b>
 * <ol>
 *   <li>CREATE TABLE statement with column definitions and optional primary key constraint</li>
 *   <li>COMMENT ON TABLE statement (if table comment exists)</li>
 *   <li>COMMENT ON COLUMN statements (for columns with comments)</li>
 * </ol>
 *
 * <p><b>Column Type Resolution:</b>
 * The builder resolves column types in the following priority order:
 * <ol>
 *   <li>Sink type ({@link Column#getSinkType()}) - explicitly specified sink type</li>
 *   <li>Source type ({@link Column#getSourceType()}) - original type if source is YashanDB</li>
 *   <li>Converted type - via {@link YashanDBTypeConverter} for other sources</li>
 * </ol>
 *
 * <p><b>Primary Key Handling:</b>
 * Primary key constraints are added inline within the CREATE TABLE statement
 * using unnamed constraints. YashanDB automatically generates system constraint
 * names (e.g., SYS_C_nnn) for unnamed primary keys.
 *
 * <p><b>Identifier Quoting:</b>
 * All identifiers (table names, column names) are quoted with double quotes (")
 * to preserve case sensitivity and handle special characters.
 *
 * <p><b>Example Output:</b>
 * <pre>
 * CREATE TABLE "SCHEMA"."TABLE" (
 *   "ID" NUMBER(10) NOT NULL,
 *   "NAME" VARCHAR(100),
 *   PRIMARY KEY ("ID")
 * );
 * COMMENT ON TABLE "SCHEMA"."TABLE" IS 'Table description';
 * COMMENT ON COLUMN "SCHEMA"."TABLE"."NAME" IS 'Column description';
 * </pre>
 *
 * @see YashanDBCatalog#createTable(TablePath, CatalogTable, boolean)
 * @see YashanDBTypeConverter
 * @see CatalogTable
 */
public class YashanDBCreateTableSqlBuilder {

    /** The columns to be created in the table. */
    private List<Column> columns;

    /** The primary key definition, may be null if no primary key. */
    private PrimaryKey primaryKey;

    /** The table-level comment, may be null. */
    private String comment;

    /** The source catalog name, used to determine if source type should be preserved. */
    protected String sourceCatalogName;

    /** The field identifier escaping mode (e.g., "none", "upper", "lower"). */
    private String fieldIde;

    /** Whether to create primary key index/constraint. */
    private boolean createIndex;

    /**
     * Creates a new SQL builder from a catalog table definition.
     *
     * <p>Extracts all necessary information from the catalog table including
     * columns, primary key, table comment, and configuration options.
     *
     * @param catalogTable the table definition to create SQL for
     * @param createIndex whether to include primary key constraint in CREATE TABLE
     */
    public YashanDBCreateTableSqlBuilder(CatalogTable catalogTable, boolean createIndex) {
        this.columns = catalogTable.getTableSchema().getColumns();
        this.primaryKey = catalogTable.getTableSchema().getPrimaryKey();
        this.comment = catalogTable.getComment();
        this.sourceCatalogName = catalogTable.getCatalogName();
        this.fieldIde = catalogTable.getOptions().get("fieldIde");
        this.createIndex = createIndex;
    }

    /**
     * Builds the complete list of SQL statements to create the table.
     *
     * <p>This method generates:
     * <ol>
     *   <li>CREATE TABLE statement with all columns and optional primary key</li>
     *   <li>COMMENT ON TABLE statement (if table has a comment)</li>
     *   <li>COMMENT ON COLUMN statements for all columns with comments</li>
     * </ol>
     *
     * <p>The generated SQL follows YashanDB's Oracle-compatible syntax with
     * double-quoted identifiers for case sensitivity preservation.
     *
     * @param tablePath the fully qualified table path (schema.table)
     * @return a list of SQL statements to execute in order
     */
    public List<String> build(TablePath tablePath) {
        List<String> sqls = new ArrayList<>();
        StringBuilder createTableSql = new StringBuilder();
        createTableSql
                .append("CREATE TABLE ")
                .append(tablePath.getSchemaAndTableName("\""))
                .append(" (\n");

        // Build column definitions with proper identifier escaping
        List<String> columnSqls =
                columns.stream()
                        .map(column -> CatalogUtils.getFieldIde(buildColumnSql(column), fieldIde))
                        .collect(Collectors.toList());

        // Add primary key constraint inline if requested and exists
        if (createIndex
                && primaryKey != null
                && primaryKey.getColumnNames() != null
                && primaryKey.getColumnNames().size() > 0) {
            columnSqls.add(buildPrimaryKeySql(primaryKey));
        }

        createTableSql.append(String.join(",\n", columnSqls));
        createTableSql.append("\n)");
        sqls.add(createTableSql.toString());

        // Add table comment if present
        if (comment != null) {
            String commentSql =
                    "COMMENT ON TABLE "
                            + tablePath.getSchemaAndTableName("\"")
                            + " IS '"
                            + comment
                            + "'";
            sqls.add(commentSql);
        }

        // Add column comments for columns that have them
        List<String> commentSqls =
                columns.stream()
                        .filter(column -> StringUtils.isNotBlank(column.getComment()))
                        .map(
                                column ->
                                        buildColumnCommentSql(
                                                column, tablePath.getSchemaAndTableName("\"")))
                        .collect(Collectors.toList());
        sqls.addAll(commentSqls);
        return sqls;
    }

    /**
     * Builds the SQL fragment for a single column definition.
     *
     * <p>The column definition includes:
     * <ul>
     *   <li>Column name (quoted with double quotes)</li>
     *   <li>Column type (resolved via type resolution logic)</li>
     *   <li>NOT NULL constraint (if column is non-nullable)</li>
     * </ul>
     *
     * <p><b>Type Resolution Priority:</b>
     * <ol>
     *   <li>{@link Column#getSinkType()} - explicit sink type override</li>
     *   <li>{@link Column#getSourceType()} - original type if source is YashanDB</li>
     *   <li>{@link YashanDBTypeConverter#reconvert(Column)} - type conversion for other sources</li>
     * </ol>
     *
     * <p><b>Example Output:</b>
     * <pre>
     * "ID" NUMBER(10) NOT NULL
     * "NAME" VARCHAR(100)
     * </pre>
     *
     * @param column the column definition to build SQL for
     * @return the SQL fragment for this column definition
     */
    protected String buildColumnSql(Column column) {
        StringBuilder columnSql = new StringBuilder();
        columnSql.append("\"").append(column.getName()).append("\" ");

        // Resolve column type using priority: sinkType > sourceType (if YashanDB) > converted type
        String columnType;
        if (column.getSinkType() != null) {
            columnType = column.getSinkType();
        } else if (StringUtils.equalsIgnoreCase(DatabaseIdentifier.YASHANDB, sourceCatalogName)
                && StringUtils.isNotBlank(column.getSourceType())) {
            columnType = column.getSourceType();
        } else {
            columnType = YashanDBTypeConverter.INSTANCE.reconvert(column).getColumnType();
        }
        columnSql.append(columnType);

        // Add NOT NULL constraint for non-nullable columns
        if (!column.isNullable()) {
            columnSql.append(" NOT NULL");
        }

        return columnSql.toString();
    }

    /**
     * Builds the SQL fragment for a primary key constraint.
     *
     * <p>Uses an unnamed primary key constraint, allowing YashanDB to automatically
     * generate a system-generated constraint name (e.g., SYS_C_nnn).
     *
     * <p><b>Example Output:</b>
     * <pre>
     * PRIMARY KEY ("ID")
     * PRIMARY KEY ("ID", "NAME")
     * </pre>
     *
     * @param primaryKey the primary key definition
     * @return the SQL fragment for the primary key constraint
     */
    private String buildPrimaryKeySql(PrimaryKey primaryKey) {
        String columnNamesString =
                primaryKey.getColumnNames().stream()
                        .map(columnName -> "\"" + columnName + "\"")
                        .collect(Collectors.joining(", "));
        return CatalogUtils.getFieldIde("PRIMARY KEY (" + columnNamesString + ")", fieldIde);
    }

    /**
     * Builds the SQL statement for a column comment.
     *
     * <p>YashanDB uses Oracle-style COMMENT ON COLUMN syntax:
     * <pre>
     * COMMENT ON COLUMN "SCHEMA"."TABLE"."COLUMN" IS 'comment text'
     * </pre>
     *
     * <p><b>Special Handling:</b>
     * <ul>
     *   <li>Single quotes within comments are escaped by doubling them</li>
     *   <li>Column reference includes full table path (schema.table.column)</li>
     * </ul>
     *
     * @param column the column to add a comment for
     * @param tableName the fully qualified table name with quotes (e.g., "SCHEMA"."TABLE")
     * @return the COMMENT ON COLUMN SQL statement
     */
    private String buildColumnCommentSql(Column column, String tableName) {
        StringBuilder columnCommentSql = new StringBuilder();
        columnCommentSql
                .append(CatalogUtils.quoteIdentifier("COMMENT ON COLUMN ", fieldIde))
                .append(tableName)
                .append(".");
        columnCommentSql
                .append(CatalogUtils.quoteIdentifier(column.getName(), fieldIde, "\""))
                .append(CatalogUtils.quoteIdentifier(" IS '", fieldIde))
                .append(column.getComment().replace("'", "''")) // Escape single quotes
                .append("'");
        return columnCommentSql.toString();
    }
}