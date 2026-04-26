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
import org.apache.seatunnel.api.table.catalog.Column;
import org.apache.seatunnel.api.table.catalog.ConstraintKey;
import org.apache.seatunnel.api.table.catalog.TablePath;
import org.apache.seatunnel.api.table.catalog.exception.CatalogException;
import org.apache.seatunnel.api.table.converter.BasicTypeDefine;
import org.apache.seatunnel.common.utils.JdbcUrlUtil;
import org.apache.seatunnel.connectors.seatunnel.jdbc.catalog.AbstractJdbcCatalog;
import org.apache.seatunnel.connectors.seatunnel.jdbc.catalog.utils.CatalogUtils;
import org.apache.seatunnel.connectors.seatunnel.jdbc.config.JdbcCommonOptions;
import org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.yashandb.YashanDBTypeConverter;
import org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.yashandb.YashanDBTypeMapper;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Catalog implementation for YashanDB (崖山数据库) database.
 *
 * <p>This catalog provides database metadata operations for YashanDB,
 * including table listing, schema discovery, and table structure retrieval.
 * It extends the abstract JDBC catalog and implements YashanDB-specific
 * SQL queries and metadata handling.
 *
 * <p><b>Key Features:</b>
 * <ul>
 *   <li>Table listing from ALL_TABLES view (excluding system tables)</li>
 *   <li>Column metadata retrieval from ALL_TAB_COLUMNS</li>
 *   <li>Column comments from ALL_COL_comments</li>
 *   <li>Table creation with proper YashanDB SQL syntax</li>
 *   <li>Schema change operations (add/modify columns)</li>
 * </ul>
 *
 * <p><b>YashanDB System Views Used:</b>
 * <ul>
 *   <li><b>ALL_TABLES:</b> Lists all tables accessible to the user</li>
 *   <li><b>ALL_TAB_COLUMNS:</b> Contains column definitions for tables</li>
 *   <li><b>ALL_COL_comments:</b> Contains column comments</li>
 * </ul>
 *
 * <p><b>Known Limitations:</b>
 * <ul>
 *   <li>ALL_TABLES does not have IOT_NAME column (different from Oracle)</li>
 *   <li>PreparedStatement.getMetaData() not supported by YashanDB JDBC driver</li>
 *   <li>System tables (MDRT_*, MDRS_*, MDXT_*, SYS_IOT_OVER_*) are excluded</li>
 * </ul>
 *
 * <p><b>Database Structure:</b>
 * <p>YashanDB uses a schema-based structure similar to Oracle:
 * <ul>
 *   <li>Database: The overall database instance (returned as "default" for simplicity)</li>
 *   <li>Schema: Equivalent to Oracle's "owner" - user namespace for tables</li>
 *   <li>Table: The actual table within a schema</li>
 * </ul>
 *
 * <p><b>Example Usage:</b>
 * <pre>
 * YashanDBCatalog catalog = new YashanDBCatalog(
 *     "yashandb_catalog",
 *     "username",
 *     "password",
 *     urlInfo,
 *     "MYSCHEMA",
 *     true,
 *     "com.yashandb.jdbc.Driver",
 *     false
 * );
 * catalog.open();
 * List&lt;String&gt; tables = catalog.listTables("default");
 * CatalogTable table = catalog.getTable(TablePath.of("MYSCHEMA.MY_TABLE", true));
 * </pre>
 *
 * @see YashanDBCatalogFactory
 * @see YashanDBCreateTableSqlBuilder
 * @see AbstractJdbcCatalog
 */
@Slf4j
public class YashanDBCatalog extends AbstractJdbcCatalog {

    /**
     * SQL template for querying column metadata from ALL_TAB_COLUMNS.
     *
     * <p>This query retrieves comprehensive column information including:
     * <ul>
     *   <li>Column name and data type</li>
     *   <li>Full type specification with length/precision/scale</li>
     *   <li>Column length, precision, and scale values</li>
     *   <li>Column comments (joined from ALL_COL_comments)</li>
     *   <li>Default value and nullability</li>
     * </ul>
     *
     * <p><b>Query Structure:</b>
     * <pre>
     * SELECT
     *   cols.COLUMN_NAME,
     *   TYPE_NAME (normalized),
     *   FULL_TYPE_NAME (with length/precision),
     *   COLUMN_LENGTH,
     *   COLUMN_PRECISION,
     *   COLUMN_SCALE,
     *   COLUMN_COMMENT,
     *   DEFAULT_VALUE,
     *   IS_NULLABLE
     * FROM all_tab_columns cols
     * LEFT JOIN all_col_comments com ON ...
     * WHERE cols.owner = '%s' AND cols.table_name = '%s'
     * ORDER BY cols.column_id
     * </pre>
     *
     * <p>The %s placeholders are replaced with schema name and table name.
     */
    private static final String SELECT_COLUMNS_SQL_TEMPLATE =
            "SELECT\n"
                    + "    cols.COLUMN_NAME,\n"
                    + "    CASE \n"
                    + "        WHEN cols.data_type LIKE 'INTERVAL%%' THEN 'INTERVAL'\n"
                    + "        ELSE REGEXP_SUBSTR(cols.data_type, '^[^(]+')\n"
                    + "    END as TYPE_NAME,\n"
                    + "    cols.data_type || \n"
                    + "        CASE \n"
                    + "            WHEN cols.data_type IN ('VARCHAR', 'VARCHAR2', 'CHAR') THEN '(' || cols.data_length || ')'\n"
                    + "            WHEN cols.data_type IN ('NVARCHAR', 'NCHAR') THEN '(' || cols.char_length || ')'\n"
                    + "            WHEN cols.data_type IN ('NUMBER') AND cols.data_precision IS NOT NULL AND cols.data_scale IS NOT NULL THEN '(' || cols.data_precision || ', ' || cols.data_scale || ')'\n"
                    + "            WHEN cols.data_type IN ('NUMBER') AND cols.data_precision IS NOT NULL AND cols.data_scale IS NULL THEN '(' || cols.data_precision || ')'\n"
                    + "            WHEN cols.data_type IN ('RAW') THEN '(' || cols.data_length || ')'\n"
                    + "        END AS FULL_TYPE_NAME,\n"
                    + "    cols.data_length AS COLUMN_LENGTH,\n"
                    + "    cols.data_precision AS COLUMN_PRECISION,\n"
                    + "    cols.data_scale AS COLUMN_SCALE,\n"
                    + "    com.comments AS COLUMN_COMMENT,\n"
                    + "    cols.data_default AS DEFAULT_VALUE,\n"
                    + "    CASE cols.nullable WHEN 'N' THEN 'NO' ELSE 'YES' END AS IS_NULLABLE\n"
                    + "FROM\n"
                    + "    all_tab_columns cols\n"
                    + "LEFT JOIN \n"
                    + "    all_col_comments com ON cols.table_name = com.table_name AND cols.column_name = com.column_name AND cols.owner = com.owner\n"
                    + "WHERE \n"
                    + "    cols.owner = '%s'\n"
                    + "    AND cols.table_name = '%s'\n"
                    + "ORDER BY \n"
                    + "    cols.column_id \n";

    /** Whether to narrow NUMBER type to smaller integer types when possible. */
    private boolean decimalTypeNarrowing;

    /** Whether to handle BLOB type as String instead of byte array. */
    private boolean handleBlobAsString;

    /**
     * Creates a catalog with default configuration.
     *
     * <p>Uses default decimal type narrowing setting from JdbcCommonOptions.
     *
     * @param catalogName the catalog name identifier
     * @param username the database username
     * @param pwd the database password
     * @param urlInfo the parsed JDBC URL information
     * @param defaultSchema the default schema name to use
     * @param driverClass the JDBC driver class name
     */
    public YashanDBCatalog(
            String catalogName,
            String username,
            String pwd,
            JdbcUrlUtil.UrlInfo urlInfo,
            String defaultSchema,
            String driverClass) {
        this(
                catalogName,
                username,
                pwd,
                urlInfo,
                defaultSchema,
                JdbcCommonOptions.DECIMAL_TYPE_NARROWING.defaultValue(),
                driverClass,
                false);
    }

    /**
     * Creates a catalog with full configuration.
     *
     * @param catalogName the catalog name identifier
     * @param username the database username
     * @param pwd the database password
     * @param urlInfo the parsed JDBC URL information
     * @param defaultSchema the default schema name to use
     * @param decimalTypeNarrowing whether to narrow NUMBER to smaller types
     * @param driverClass the JDBC driver class name
     * @param handleBlobAsString whether to treat BLOB as String type
     */
    public YashanDBCatalog(
            String catalogName,
            String username,
            String pwd,
            JdbcUrlUtil.UrlInfo urlInfo,
            String defaultSchema,
            boolean decimalTypeNarrowing,
            String driverClass,
            boolean handleBlobAsString) {
        super(catalogName, username, pwd, urlInfo, defaultSchema, driverClass);
        this.decimalTypeNarrowing = decimalTypeNarrowing;
        this.handleBlobAsString = handleBlobAsString;
    }

    /**
     * Returns the SQL query for finding a specific table.
     *
     * <p>Appends schema and table name conditions to the base table listing SQL.
     *
     * @param tablePath the table path containing schema and table name
     * @return the SQL query string
     */
    @Override
    protected String getTableWithConditionSql(TablePath tablePath) {
        return getListTableSql(tablePath.getDatabaseName())
                + "  and  OWNER = '"
                + tablePath.getSchemaName()
                + "' and table_name = '"
                + tablePath.getTableName()
                + "'";
    }

    /**
     * Checks if a database exists.
     *
     * <p><b>Note:</b> YashanDB uses a single database concept, so this always returns true.
     * The actual namespace separation is done through schemas (owners).
     *
     * @param databaseName the database name to check
     * @return always true for YashanDB
     * @throws CatalogException never thrown for YashanDB
     */
    @Override
    public boolean databaseExists(String databaseName) throws CatalogException {
        return true;
    }

    /**
     * Lists all databases.
     *
     * <p><b>Note:</b> YashanDB uses a single database concept. This returns
     * a single "default" database name for compatibility with the catalog API.
     * Actual table organization is done through schemas.
     *
     * @return a list containing only "default"
     * @throws CatalogException never thrown for YashanDB
     */
    @Override
    public List<String> listDatabases() throws CatalogException {
        return new ArrayList<>(Collections.singletonList("default"));
    }

    /**
     * Returns the SQL for creating a table.
     *
     * <p>Delegates to {@link YashanDBCreateTableSqlBuilder} for SQL generation.
     *
     * @param tablePath the table path
     * @param table the catalog table definition
     * @param createIndex whether to create index for primary key
     * @return the CREATE TABLE SQL statement
     */
    @Override
    protected String getCreateTableSql(
            TablePath tablePath, CatalogTable table, boolean createIndex) {
        return getCreateTableSqls(tablePath, table, createIndex).get(0);
    }

    /**
     * Returns the list of SQL statements for creating a table.
     *
     * <p>Generates multiple SQL statements including:
     * <ul>
     *   <li>CREATE TABLE statement</li>
     *   <li>Table comment (if specified)</li>
     *   <li>Column comments (if specified)</li>
     * </ul>
     *
     * @param tablePath the table path
     * @param table the catalog table definition
     * @param createIndex whether to create index for primary key
     * @return list of SQL statements to execute
     */
    protected List<String> getCreateTableSqls(
            TablePath tablePath, CatalogTable table, boolean createIndex) {
        return new YashanDBCreateTableSqlBuilder(table, createIndex).build(tablePath);
    }

    /**
     * Returns the SQL for dropping a table.
     *
     * <p>Generates DROP TABLE statement with quoted schema and table name.
     *
     * @param tablePath the table path to drop
     * @return the DROP TABLE SQL statement
     */
    @Override
    protected String getDropTableSql(TablePath tablePath) {
        return String.format("DROP TABLE %s", tablePath.getSchemaAndTableName("\""));
    }

    /**
     * Returns the SQL for listing all tables.
     *
     * <p>Queries ALL_TABLES view and excludes system tables:
     * <ul>
     *   <li>MDRT_* - Spatial index tables</li>
     *   <li>MDRS_* - Spatial reference tables</li>
     *   <li>MDXT_* - Spatial metadata tables</li>
     *   <li>SYS_IOT_OVER_* - Index-organized table overflow</li>
     * </ul>
     *
     * <p><b>Important:</b> YashanDB's ALL_TABLES does NOT have the IOT_NAME column
     * that Oracle has. The condition has been adjusted to only filter by table name pattern.
     *
     * @param databaseName the database name (not used for YashanDB)
     * @return the SQL query for listing tables
     */
    @Override
    protected String getListTableSql(String databaseName) {
        return "SELECT OWNER, TABLE_NAME FROM ALL_TABLES"
                + "  WHERE TABLE_NAME NOT LIKE 'MDRT_%'"
                + "  AND TABLE_NAME NOT LIKE 'MDRS_%'"
                + "  AND TABLE_NAME NOT LIKE 'MDXT_%'"
                + "  AND TABLE_NAME NOT LIKE 'SYS_IOT_OVER_%'";
    }

    /**
     * Extracts table name from result set row.
     *
     * <p>Combines owner (schema) and table name into a qualified name.
     *
     * @param rs the result set from table listing query
     * @return the qualified table name (schema.table format)
     * @throws SQLException if result set access fails
     */
    @Override
    protected String getTableName(ResultSet rs) throws SQLException {
        return rs.getString(1) + "." + rs.getString(2);
    }

    /**
     * Returns the SQL for querying column metadata.
     *
     * <p>Formats the SELECT_COLUMNS_SQL_TEMPLATE with schema and table name.
     *
     * @param tablePath the table path
     * @return the formatted SQL query
     */
    @Override
    protected String getSelectColumnsSql(TablePath tablePath) {
        return String.format(
                SELECT_COLUMNS_SQL_TEMPLATE, tablePath.getSchemaName(), tablePath.getTableName());
    }

    /**
     * Builds a Column from result set row.
     *
     * <p>Extracts all column metadata and converts to SeaTunnel Column
     * using {@link YashanDBTypeConverter}.
     *
     * <p><b>Metadata Extracted:</b>
     * <ul>
     *   <li>COLUMN_NAME - column name</li>
     *   <li>TYPE_NAME - normalized data type</li>
     *   <li>FULL_TYPE_NAME - complete type with parameters</li>
     *   <li>COLUMN_LENGTH - length for string types</li>
     *   <li>COLUMN_PRECISION - precision for numeric types</li>
     *   <li>COLUMN_SCALE - scale for numeric types</li>
     *   <li>COLUMN_COMMENT - column comment</li>
     *   <li>DEFAULT_VALUE - default value expression</li>
     *   <li>IS_NULLABLE - nullability indicator</li>
     * </ul>
     *
     * @param resultSet the result set from column query
     * @return the converted SeaTunnel Column
     * @throws SQLException if result set access fails
     */
    @Override
    protected Column buildColumn(ResultSet resultSet) throws SQLException {
        String columnName = resultSet.getString("COLUMN_NAME");
        String typeName = resultSet.getString("TYPE_NAME");
        String fullTypeName = resultSet.getString("FULL_TYPE_NAME");
        long columnLength = resultSet.getLong("COLUMN_LENGTH");
        Long columnPrecision = resultSet.getObject("COLUMN_PRECISION", Long.class);
        Integer columnScale = resultSet.getObject("COLUMN_SCALE", Integer.class);
        String columnComment = resultSet.getString("COLUMN_COMMENT");
        Object defaultValue = resultSet.getObject("DEFAULT_VALUE");
        boolean isNullable = resultSet.getString("IS_NULLABLE").equals("YES");

        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name(columnName)
                        .columnType(fullTypeName)
                        .dataType(typeName)
                        .length(columnLength)
                        .precision(columnPrecision)
                        .scale(columnScale)
                        .nullable(isNullable)
                        .defaultValue(defaultValue)
                        .comment(columnComment)
                        .build();
        return new YashanDBTypeConverter(decimalTypeNarrowing, handleBlobAsString)
                .convert(typeDefine);
    }

    /**
     * Returns URL for database name (not applicable for YashanDB).
     *
     * <p>YashanDB uses a single URL for all schemas, so this returns
     * the default URL regardless of database name.
     *
     * @param databaseName the database name (not used)
     * @return the default connection URL
     */
    @Override
    protected String getUrlFromDatabaseName(String databaseName) {
        return defaultUrl;
    }

    /**
     * Returns the option table name format.
     *
     * <p>Returns schema.table format for YashanDB.
     *
     * @param tablePath the table path
     * @return the schema.table string
     */
    @Override
    protected String getOptionTableName(TablePath tablePath) {
        return tablePath.getSchemaAndTableName();
    }

    /**
     * Gets table structure from a SQL query.
     *
     * <p><b>Important:</b> YashanDB JDBC driver does NOT support
     * PreparedStatement.getMetaData() method. This implementation
     * executes the query and gets metadata from ResultSet instead.
     *
     * <p>This workaround is necessary because calling getMetaData()
     * on PreparedStatement throws SQLFeatureNotSupportedException
     * in YashanDB's JDBC driver.
     *
     * <p><b>Note:</b> This method closes the PreparedStatement and ResultSet
     * after obtaining metadata. For large queries, this may be inefficient.
     *
     * @param sqlQuery the SQL query to analyze
     * @return the CatalogTable with column definitions
     * @throws SQLException if query execution fails
     */
    @Override
    public CatalogTable getTable(String sqlQuery) throws SQLException {
        Connection defaultConnection = getConnection(defaultUrl);
        // YashanDB JDBC driver does not support PreparedStatement.getMetaData()
        // So we execute the query and get metadata from ResultSet
        try (PreparedStatement ps = defaultConnection.prepareStatement(sqlQuery);
                ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData resultSetMetaData = rs.getMetaData();
            return CatalogUtils.getCatalogTable(
                    resultSetMetaData,
                    new YashanDBTypeMapper(decimalTypeNarrowing, handleBlobAsString),
                    sqlQuery);
        }
    }

    /**
     * Returns the SQL for truncating a table.
     *
     * <p>Generates TRUNCATE TABLE statement with quoted schema and table name.
     *
     * @param tablePath the table path to truncate
     * @return the TRUNCATE TABLE SQL statement
     */
    @Override
    protected String getTruncateTableSql(TablePath tablePath) {
        return String.format(
                "TRUNCATE TABLE \"%s\".\"%s\"",
                tablePath.getSchemaName(), tablePath.getTableName());
    }

    /**
     * Returns the SQL for checking if table has data.
     *
     * <p>Uses ROWNUM = 1 limit for efficient existence check.
     *
     * @param tablePath the table path to check
     * @return the SQL query that returns at most one row
     */
    @Override
    protected String getExistDataSql(TablePath tablePath) {
        return String.format(
                "select * from \"%s\".\"%s\" WHERE rownum = 1",
                tablePath.getSchemaName(), tablePath.getTableName());
    }

    /**
     * Gets constraint keys (foreign keys, unique keys) for a table.
     *
     * <p>Attempts to retrieve constraint information from database metadata.
     * If retrieval fails (e.g., due to permission issues), returns empty list
     * instead of throwing exception.
     *
     * <p><b>Note:</b> Constraint retrieval may fail if the user does not have
     * sufficient privileges to access constraint metadata views.
     *
     * @param metaData the database metadata object
     * @param tablePath the table path
     * @return list of constraint keys, or empty list if retrieval fails
     * @throws SQLException if unexpected error occurs
     */
    @Override
    protected List<ConstraintKey> getConstraintKeys(DatabaseMetaData metaData, TablePath tablePath)
            throws SQLException {
        try {
            return getConstraintKeys(
                    metaData,
                    tablePath.getDatabaseName(),
                    tablePath.getSchemaName(),
                    tablePath.getTableName());
        } catch (SQLException e) {
            log.info("Obtain constraint failure", e);
            return new ArrayList<>();
        }
    }
}