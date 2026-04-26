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

import org.apache.seatunnel.shade.org.apache.commons.lang3.StringUtils;

import org.apache.seatunnel.api.table.catalog.Column;
import org.apache.seatunnel.api.table.catalog.TablePath;
import org.apache.seatunnel.api.table.converter.BasicTypeDefine;
import org.apache.seatunnel.api.table.converter.TypeConverter;
import org.apache.seatunnel.api.table.schema.event.AlterTableAddColumnEvent;
import org.apache.seatunnel.api.table.schema.event.AlterTableChangeColumnEvent;
import org.apache.seatunnel.api.table.schema.event.AlterTableColumnEvent;
import org.apache.seatunnel.api.table.schema.event.AlterTableModifyColumnEvent;
import org.apache.seatunnel.connectors.seatunnel.jdbc.config.JdbcCommonOptions;
import org.apache.seatunnel.connectors.seatunnel.jdbc.internal.converter.JdbcRowConverter;
import org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.DatabaseIdentifier;
import org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.JdbcDialect;
import org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.JdbcDialectTypeMapper;
import org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.SQLUtils;
import org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.dialectenum.FieldIdeEnum;
import org.apache.seatunnel.connectors.seatunnel.jdbc.source.JdbcSourceTable;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JDBC dialect implementation for YashanDB (崖山数据库).
 *
 * <p>YashanDB is a relational database developed in China, compatible with Oracle syntax
 * and protocols. This dialect provides YashanDB-specific SQL generation and database operations.
 *
 * <p>Key features of this dialect:
 * <ul>
 *   <li>Oracle-compatible SQL syntax (MERGE INTO for upsert, DUAL table for queries)</li>
 *   <li>Support for YashanDB-specific data types (NUMBER, TIMESTAMP WITH TIME ZONE, etc.)</li>
 *   <li>Schema change operations (ADD, MODIFY, CHANGE columns)</li>
 *   <li>Chunk-based parallel reading with row count estimation</li>
 * </ul>
 *
 * <p><b>JDBC URL Format:</b> Only <code>jdbc:yasdb://host:port/database</code> is supported.
 *
 * <p><b>Driver Class:</b> <code>com.yashandb.jdbc.Driver</code>
 *
 * <p><b>Known Limitations:</b>
 * <ul>
 *   <li>PreparedStatement.getMetaData() is not supported by YashanDB JDBC driver.
 *       Use ResultSet.getMetaData() instead (see {@link #getResultSetMetaData})</li>
 *   <li>ALL_TABLES view does not have IOT_NAME column (handled in catalog)</li>
 *   <li>Maximum TIMESTAMP/TIME scale is 6 (fractional seconds precision)</li>
 * </ul>
 *
 * @see YashanDBDialectFactory
 * @see YashanDBTypeConverter
 * @see YashanDBCatalog
 */
@Slf4j
public class YashanDBDialect implements JdbcDialect {

    /** Default fetch size for JDBC result sets. */
    private static final int DEFAULT_FETCH_SIZE = 128;

    /** Field naming convention (original, uppercase, lowercase). */
    public String fieldIde = FieldIdeEnum.ORIGINAL.getValue();

    /** Whether to handle BLOB type as String instead of byte array. */
    private final boolean handleBlobAsString;

    /**
     * Creates a dialect with field naming convention.
     *
     * @param fieldIde the field naming convention to use
     */
    public YashanDBDialect(String fieldIde) {
        this(fieldIde, JdbcCommonOptions.HANDLE_BLOB_AS_STRING.defaultValue());
    }

    /**
     * Creates a dialect with default settings.
     *
     * <p>Uses original field naming and default BLOB handling.
     */
    public YashanDBDialect() {
        this(
                FieldIdeEnum.ORIGINAL.getValue(),
                JdbcCommonOptions.HANDLE_BLOB_AS_STRING.defaultValue());
    }

    /**
     * Creates a dialect with full configuration.
     *
     * @param fieldIde the field naming convention to use
     * @param handleBlobAsString whether to handle BLOB as String type
     */
    public YashanDBDialect(String fieldIde, boolean handleBlobAsString) {
        this.fieldIde = fieldIde;
        this.handleBlobAsString = handleBlobAsString;
    }

    /**
     * Returns the dialect name identifier.
     *
     * @return "YASHANDB" constant
     */
    @Override
    public String dialectName() {
        return DatabaseIdentifier.YASHANDB;
    }

    /**
     * Returns the row converter for YashanDB.
     *
     * <p>The converter handles special data types like BLOB and CLOB
     * with appropriate JDBC statement binding methods.
     *
     * @return a new {@link YashanDBJdbcRowConverter} instance
     */
    @Override
    public JdbcRowConverter getRowConverter() {
        return new YashanDBJdbcRowConverter();
    }

    /**
     * Returns the type converter for YashanDB.
     *
     * <p>The converter handles mapping between SeaTunnel types and YashanDB types,
     * including NUMBER precision/scale handling and TIMESTAMP scale limitations.
     *
     * @return a new {@link YashanDBTypeConverter} instance
     */
    @Override
    public TypeConverter<BasicTypeDefine> getTypeConverter() {
        return new YashanDBTypeConverter(true, handleBlobAsString);
    }

    /**
     * Generates a hash expression for field partitioning.
     *
     * <p>Uses Oracle's ORA_HASH function for consistent hash distribution.
     *
     * @param fieldName the field name to hash
     * @param mod the modulo value for hash bucket count
     * @return SQL expression like "MOD(ORA_HASH("field"), N)"
     */
    @Override
    public String hashModForField(String fieldName, int mod) {
        return "MOD(ORA_HASH(" + quoteIdentifier(fieldName) + ")," + mod + ")";
    }

    /**
     * Returns the JDBC type mapper for YashanDB.
     *
     * @return a new {@link YashanDBTypeMapper} instance
     */
    @Override
    public JdbcDialectTypeMapper getJdbcDialectTypeMapper() {
        return new YashanDBTypeMapper(true, handleBlobAsString);
    }

    /**
     * Quotes an identifier with double quotes.
     *
     * <p>Handles both simple identifiers and schema.table format.
     * For schema.table format, quotes each part separately.
     *
     * <p>Example: "schema.table" becomes "\"schema\".\"table\""
     *
     * @param identifier the identifier to quote
     * @return the quoted identifier
     */
    @Override
    public String quoteIdentifier(String identifier) {
        if (identifier.contains(".")) {
            String[] parts = identifier.split("\\.");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.length - 1; i++) {
                sb.append("\"").append(parts[i]).append("\"").append(".");
            }
            return sb.append("\"")
                    .append(getFieldIde(parts[parts.length - 1], fieldIde))
                    .append("\"")
                    .toString();
        }
        return "\"" + getFieldIde(identifier, fieldIde) + "\"";
    }

    /**
     * Returns table identifier for given database and table name.
     *
     * <p>For YashanDB, database name is not used in table identifier
     * since schema is part of the table path.
     *
     * @param database the database name (not used)
     * @param tableName the table name
     * @return the quoted table identifier
     */
    @Override
    public String tableIdentifier(String database, String tableName) {
        return quoteIdentifier(tableName);
    }

    /**
     * Generates an upsert (MERGE INTO) statement for YashanDB.
     *
     * <p>YashanDB uses Oracle-compatible MERGE INTO syntax for upsert operations.
     * This method generates a statement that:
     * <ul>
     *   <li>Merges new data into existing table</li>
     *   <li>Updates existing rows when key matches</li>
     *   <li>Inserts new rows when key does not match</li>
     * </ul>
     *
     * @param database the database name
     * @param tableName the table name
     * @param fieldNames all field names in the table
     * @param uniqueKeyFields the unique key field names for matching
     * @return the MERGE INTO SQL statement
     */
    @Override
    public Optional<String> getUpsertStatement(
            String database, String tableName, String[] fieldNames, String[] uniqueKeyFields) {
        List<String> nonUniqueKeyFields =
                Arrays.stream(fieldNames)
                        .filter(fieldName -> !Arrays.asList(uniqueKeyFields).contains(fieldName))
                        .collect(Collectors.toList());
        String valuesBinding =
                Arrays.stream(fieldNames)
                        .map(fieldName -> ":" + fieldName + " " + quoteIdentifier(fieldName))
                        .collect(Collectors.joining(", "));

        String usingClause = String.format("SELECT %s FROM DUAL", valuesBinding);
        String onConditions =
                Arrays.stream(uniqueKeyFields)
                        .map(
                                fieldName ->
                                        String.format(
                                                "TARGET.%s=SOURCE.%s",
                                                quoteIdentifier(fieldName),
                                                quoteIdentifier(fieldName)))
                        .collect(Collectors.joining(" AND "));
        String updateSetClause =
                nonUniqueKeyFields.stream()
                        .map(
                                fieldName ->
                                        String.format(
                                                "TARGET.%s=SOURCE.%s",
                                                quoteIdentifier(fieldName),
                                                quoteIdentifier(fieldName)))
                        .collect(Collectors.joining(", "));
        String insertFields =
                Arrays.stream(fieldNames)
                        .map(this::quoteIdentifier)
                        .collect(Collectors.joining(", "));
        String insertValues =
                Arrays.stream(fieldNames)
                        .map(fieldName -> "SOURCE." + quoteIdentifier(fieldName))
                        .collect(Collectors.joining(", "));

        String upsertSQL =
                String.format(
                        " MERGE INTO %s TARGET"
                                + " USING (%s) SOURCE"
                                + " ON (%s) "
                                + " WHEN MATCHED THEN"
                                + " UPDATE SET %s"
                                + " WHEN NOT MATCHED THEN"
                                + " INSERT (%s) VALUES (%s)",
                        tableIdentifier(database, tableName),
                        usingClause,
                        onConditions,
                        updateSetClause,
                        insertFields,
                        insertValues);

        return Optional.of(upsertSQL);
    }

    /**
     * Creates a PreparedStatement with appropriate fetch size.
     *
     * <p>Configures the statement for forward-only, read-only access
     * with optimized fetch size for YashanDB.
     *
     * @param connection the database connection
     * @param queryTemplate the SQL query template
     * @param fetchSize the desired fetch size (0 or negative uses default)
     * @return the configured PreparedStatement
     * @throws SQLException if statement creation fails
     */
    @Override
    public PreparedStatement creatPreparedStatement(
            Connection connection, String queryTemplate, int fetchSize) throws SQLException {
        PreparedStatement statement =
                connection.prepareStatement(
                        queryTemplate, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        if (fetchSize > 0) {
            statement.setFetchSize(fetchSize);
        } else {
            statement.setFetchSize(DEFAULT_FETCH_SIZE);
        }
        return statement;
    }

    /**
     * Parses a table path string into TablePath object.
     *
     * <p>YashanDB uses schema.table format, so the second parameter is true.
     *
     * @param tablePath the table path string (e.g., "schema.table")
     * @return the parsed TablePath object
     */
    @Override
    public TablePath parse(String tablePath) {
        return TablePath.of(tablePath, true);
    }

    /**
     * Returns the full quoted table identifier with schema.
     *
     * @param tablePath the table path containing schema and table name
     * @return the quoted schema.table identifier
     */
    @Override
    public String tableIdentifier(TablePath tablePath) {
        return quoteIdentifier(tablePath.getSchemaAndTableName());
    }

    /**
     * Estimates the approximate row count for a table or query.
     *
     * <p>For tables without WHERE clause, uses ALL_TABLES.NUM_ROWS statistics.
     * For complex queries, uses SELECT COUNT(*) subquery.
     *
     * <p>When using table statistics, optionally runs ANALYZE TABLE first
     * to get accurate statistics (unless skipAnalyze is true).
     *
     * @param connection the database connection
     * @param table the source table information
     * @return the estimated row count
     * @throws SQLException if query execution fails
     */
    @Override
    public Long approximateRowcntStatement(Connection connection, JdbcSourceTable table)
            throws SQLException {
        String query = table.getQuery();

        boolean useTableStats =
                StringUtils.isBlank(query)
                        || (!query.toLowerCase().contains("where")
                                && table.getTablePath() != null
                                && !TablePath.DEFAULT
                                        .getFullName()
                                        .equals(table.getTablePath().getFullName()));

        if (table.getUseSelectCount()) {
            useTableStats = false;
            if (StringUtils.isBlank(query)) {
                query = "SELECT * FROM " + tableIdentifier(table.getTablePath());
            }
        }

        if (useTableStats) {
            TablePath tablePath = table.getTablePath();
            String rowCountQuery =
                    String.format(
                            "select NUM_ROWS from all_tables where OWNER = '%s' AND TABLE_NAME = '%s' ",
                            tablePath.getSchemaName(), tablePath.getTableName());
            try (Statement stmt = connection.createStatement()) {
                String analyzeTable =
                        String.format(
                                "analyze table %s compute statistics for table",
                                tableIdentifier(tablePath));
                if (!table.getSkipAnalyze()) {
                    log.info("Split Chunk, approximateRowCntStatement: {}", analyzeTable);
                    stmt.execute(analyzeTable);
                } else {
                    log.warn("Skip analyze, approximateRowCntStatement: {}", analyzeTable);
                }
                log.info("Split Chunk, approximateRowCntStatement: {}", rowCountQuery);
                try (ResultSet rs = stmt.executeQuery(rowCountQuery)) {
                    if (!rs.next()) {
                        throw new SQLException(
                                String.format(
                                        "No result returned after running query [%s]",
                                        rowCountQuery));
                    }
                    return rs.getLong(1);
                }
            }
        }
        return SQLUtils.countForSubquery(connection, query);
    }

    /**
     * Queries the maximum value for the next chunk boundary.
     *
     * <p>Used for chunk-based parallel reading. Finds the max value
     * within a specified chunk size starting from a lower bound.
     *
     * <p>Uses ROWNUM limiting for efficient chunk boundary detection.
     *
     * @param connection the database connection
     * @param table the source table information
     * @param columnName the split column name
     * @param chunkSize the chunk size limit
     * @param includedLowerBound the starting lower bound value
     * @return the maximum value for this chunk
     * @throws SQLException if query execution fails
     */
    @Override
    public Object queryNextChunkMax(
            Connection connection,
            JdbcSourceTable table,
            String columnName,
            int chunkSize,
            Object includedLowerBound)
            throws SQLException {
        String quotedColumn = quoteIdentifier(columnName);
        String sqlQuery;
        if (StringUtils.isNotBlank(table.getQuery())) {
            sqlQuery =
                    String.format(
                            "SELECT MAX(%s) FROM ("
                                    + "SELECT %s FROM (%s) WHERE %s >= ? ORDER BY %s ASC "
                                    + ") WHERE ROWNUM <= %s",
                            quotedColumn,
                            quotedColumn,
                            table.getQuery(),
                            quotedColumn,
                            quotedColumn,
                            chunkSize);
        } else {
            sqlQuery =
                    String.format(
                            "SELECT MAX(%s) FROM ("
                                    + "SELECT %s FROM %s WHERE %s >= ? ORDER BY %s ASC "
                                    + ") WHERE ROWNUM <= %s",
                            quotedColumn,
                            quotedColumn,
                            tableIdentifier(table.getTablePath()),
                            quotedColumn,
                            quotedColumn,
                            chunkSize);
        }

        try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
            ps.setObject(1, includedLowerBound);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException(
                            String.format("No result returned after running query [%s]", sqlQuery));
                }
                return rs.getObject(1);
            }
        }
    }

    /**
     * Samples data from a column for chunk distribution analysis.
     *
     * <p>Used to analyze data distribution for even chunk splitting.
     * Samples values at specified sampling rate and returns sorted array.
     *
     * @param connection the database connection
     * @param table the source table information
     * @param columnName the column to sample
     * @param samplingRate the sampling rate (e.g., every Nth row)
     * @param fetchSize the JDBC fetch size
     * @return sorted array of sampled values
     * @throws Exception if query execution fails or thread interrupted
     */
    @Override
    public Object[] sampleDataFromColumn(
            Connection connection,
            JdbcSourceTable table,
            String columnName,
            int samplingRate,
            int fetchSize)
            throws Exception {
        String sampleQuery;
        if (StringUtils.isNotBlank(table.getQuery())) {
            sampleQuery =
                    String.format(
                            "SELECT %s FROM (%s) T", quoteIdentifier(columnName), table.getQuery());
        } else {
            sampleQuery =
                    String.format(
                            "SELECT %s FROM %s",
                            quoteIdentifier(columnName), tableIdentifier(table.getTablePath()));
        }

        try (PreparedStatement stmt = creatPreparedStatement(connection, sampleQuery, fetchSize)) {
            try (ResultSet rs = stmt.executeQuery()) {
                int count = 0;
                List<Object> results = new ArrayList<>();

                while (rs.next()) {
                    count++;
                    if (count % samplingRate == 0) {
                        results.add(rs.getObject(1));
                    }
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException("Thread interrupted");
                    }
                }
                Object[] resultsArray = results.toArray();
                Arrays.sort(resultsArray);
                return resultsArray;
            }
        }
    }

    /**
     * Applies ADD column schema change to the table.
     *
     * <p>Generates and executes ALTER TABLE ADD column SQL with optional comment.
     *
     * @param connection the database connection
     * @param tablePath the table path
     * @param event the add column event
     * @throws SQLException if execution fails
     */
    @Override
    public void applySchemaChange(
            Connection connection, TablePath tablePath, AlterTableAddColumnEvent event)
            throws SQLException {
        List<String> ddlSQL = new ArrayList<>();
        ddlSQL.add(buildUpdateColumnSQL(connection, tablePath, event));

        if (event.getColumn().getComment() != null) {
            ddlSQL.add(buildUpdateColumnCommentSQL(tablePath, event.getColumn()));
        }

        try (Statement statement = connection.createStatement()) {
            for (String sql : ddlSQL) {
                log.info("Executing add column SQL: {}", sql);
                statement.execute(sql);
            }
        }
    }

    /**
     * Applies CHANGE (rename) column schema change to the table.
     *
     * <p>Handles column rename operations. If the column type also changes,
     * delegates to MODIFY column handler.
     *
     * @param connection the database connection
     * @param tablePath the table path
     * @param event the change column event
     * @throws SQLException if execution fails
     */
    @Override
    public void applySchemaChange(
            Connection connection, TablePath tablePath, AlterTableChangeColumnEvent event)
            throws SQLException {
        List<String> ddlSQL = new ArrayList<>();
        if (event.getOldColumn() != null
                && !(event.getColumn().getName().equals(event.getOldColumn()))) {
            StringBuilder sqlBuilder =
                    new StringBuilder()
                            .append("ALTER TABLE ")
                            .append(tableIdentifier(tablePath))
                            .append(" RENAME COLUMN ")
                            .append(quoteIdentifier(event.getOldColumn()))
                            .append(" TO ")
                            .append(quoteIdentifier(event.getColumn().getName()));
            ddlSQL.add(sqlBuilder.toString());
        }

        try (Statement statement = connection.createStatement()) {
            for (String sql : ddlSQL) {
                log.info("Executing change column SQL: {}", sql);
                statement.execute(sql);
            }
        }

        if (event.getColumn().getDataType() != null) {
            applySchemaChange(
                    connection,
                    tablePath,
                    AlterTableModifyColumnEvent.modify(event.tableIdentifier(), event.getColumn()));
        }
    }

    /**
     * Applies MODIFY column schema change to the table.
     *
     * <p>Handles column type modification and nullable changes.
     *
     * @param connection the database connection
     * @param tablePath the table path
     * @param event the modify column event
     * @throws SQLException if execution fails
     */
    @Override
    public void applySchemaChange(
            Connection connection, TablePath tablePath, AlterTableModifyColumnEvent event)
            throws SQLException {
        List<String> ddlSQL = new ArrayList<>();
        ddlSQL.add(buildUpdateColumnSQL(connection, tablePath, event));

        if (event.getColumn().getComment() != null) {
            ddlSQL.add(buildUpdateColumnCommentSQL(tablePath, event.getColumn()));
        }

        try (Statement statement = connection.createStatement()) {
            for (String sql : ddlSQL) {
                log.info("Executing modify column SQL: {}", sql);
                statement.execute(sql);
            }
        }
    }

    /**
     * Builds the ALTER TABLE column SQL statement.
     *
     * <p>Generates ADD or MODIFY column SQL based on event type.
     * Handles column type, default value, and nullable specification.
     *
     * @param connection the database connection
     * @param tablePath the table path
     * @param event the column event (ADD or MODIFY)
     * @return the generated SQL statement
     * @throws SQLException if column nullable check fails
     */
    private String buildUpdateColumnSQL(
            Connection connection, TablePath tablePath, AlterTableColumnEvent event)
            throws SQLException {
        String actionType;
        Column column;
        if (event instanceof AlterTableModifyColumnEvent) {
            actionType = "MODIFY";
            column = ((AlterTableModifyColumnEvent) event).getColumn();
        } else if (event instanceof AlterTableAddColumnEvent) {
            actionType = "ADD";
            column = ((AlterTableAddColumnEvent) event).getColumn();
        } else {
            throw new IllegalArgumentException("Unsupported AlterTableColumnEvent: " + event);
        }
        String sourceDialectName = event.getSourceDialectName();
        boolean sameCatalog = StringUtils.equals(dialectName(), sourceDialectName);
        BasicTypeDefine typeDefine = getTypeConverter().reconvert(column);
        String columnType = sameCatalog ? column.getSourceType() : typeDefine.getColumnType();
        StringBuilder sqlBuilder =
                new StringBuilder()
                        .append("ALTER TABLE  ")
                        .append(tableIdentifier(tablePath))
                        .append(" ")
                        .append(actionType)
                        .append(" ")
                        .append(quoteIdentifier(column.getName()))
                        .append(" ")
                        .append(columnType);
        if (column.getDefaultValue() != null && sameCatalog) {
            sqlBuilder.append(" ").append(sqlClauseWithDefaultValue(typeDefine, sourceDialectName));
        }
        if (event instanceof AlterTableModifyColumnEvent) {
            boolean targetColumnNullable =
                    columnIsNullable(connection, tablePath, column.getName());
            if (column.isNullable() != targetColumnNullable) {
                sqlBuilder.append(" ").append(column.isNullable() ? "NULL" : "NOT NULL");
            }
        } else {
            sqlBuilder.append(" ").append(column.isNullable() ? "NULL" : "NOT NULL");
        }
        return sqlBuilder.toString();
    }

    /**
     * Builds the COMMENT ON COLUMN SQL statement.
     *
     * @param tablePath the table path
     * @param column the column with comment
     * @return the generated comment SQL
     */
    private String buildUpdateColumnCommentSQL(TablePath tablePath, Column column) {
        return String.format(
                "COMMENT ON COLUMN %s.%s IS '%s'",
                tableIdentifier(tablePath), quoteIdentifier(column.getName()), column.getComment());
    }

    /**
     * Checks if a column is nullable in the database.
     *
     * <p>Queries ALL_TAB_COLUMNS view for column nullable status.
     *
     * @param connection the database connection
     * @param tablePath the table path
     * @param column the column name
     * @return true if column is nullable, false otherwise
     * @throws SQLException if query fails
     */
    private boolean columnIsNullable(Connection connection, TablePath tablePath, String column)
            throws SQLException {
        String selectColumnSQL =
                "SELECT"
                        + "        NULLABLE FROM"
                        + "        ALL_TAB_COLUMNS c"
                        + "        WHERE c.owner = '"
                        + tablePath.getSchemaName()
                        + "'"
                        + "        AND c.table_name = '"
                        + tablePath.getTableName()
                        + "'"
                        + "        AND c.column_name = '"
                        + column
                        + "'";
        try (Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery(selectColumnSQL);
            rs.next();
            return rs.getString("NULLABLE").equals("Y");
        }
    }

    /**
     * Returns the DUAL table clause for YashanDB.
     *
     * <p>DUAL is a special one-row table used for selecting values
     * without a real table (Oracle-compatible syntax).
     *
     * @return " FROM dual " clause
     */
    @Override
    public String dualTable() {
        return " FROM dual ";
    }

    /**
     * Gets ResultSet metadata by executing the query.
     *
     * <p><b>Important:</b> YashanDB JDBC driver does NOT support
     * PreparedStatement.getMetaData() method. This workaround executes
     * the query and gets metadata from the ResultSet.
     *
     * <p>This method closes the PreparedStatement and ResultSet after
     * obtaining the metadata, so it should only be used for metadata
     * queries that don't need the actual data.
     *
     * @param conn the database connection
     * @param query the SQL query to execute
     * @return the ResultSetMetaData from the executed query
     * @throws SQLException if query execution fails
     */
    @Override
    public ResultSetMetaData getResultSetMetaData(Connection conn, String query)
            throws SQLException {
        // YashanDB JDBC driver does not support PreparedStatement.getMetaData()
        // So we execute the query and get metadata from ResultSet
        try (PreparedStatement preparedStatement = conn.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            return resultSet.getMetaData();
        }
    }

    /**
     * Builds collation SQL for string comparison.
     *
     * <p>Uses NLSSORT function for locale-specific string sorting.
     *
     * @param collate the collation name (e.g., "BINARY", "CI" for case-insensitive)
     * @return the NLSSORT SQL expression or original value if collate is empty
     */
    @Override
    public String getCollateSql(String collate) {
        if (StringUtils.isNotBlank(collate)) {
            StringBuilder sql = new StringBuilder();
            sql.append("NLSSORT(")
                    .append("char_val")
                    .append(", 'NLS_SORT=")
                    .append(collate)
                    .append("')");
            return sql.toString();
        } else {
            return "char_val";
        }
    }
}