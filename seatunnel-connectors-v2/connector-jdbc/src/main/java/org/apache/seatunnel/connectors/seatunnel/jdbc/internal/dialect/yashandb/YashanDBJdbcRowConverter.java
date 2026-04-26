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

import org.apache.seatunnel.api.table.type.SeaTunnelDataType;
import org.apache.seatunnel.api.table.type.SqlType;
import org.apache.seatunnel.connectors.seatunnel.jdbc.internal.converter.AbstractJdbcRowConverter;
import org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.DatabaseIdentifier;

import javax.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.yashandb.YashanDBTypeConverter.YASHANDB_BLOB;
import static org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.yashandb.YashanDBTypeConverter.YASHANDB_CLOB;
import static org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.yashandb.YashanDBTypeConverter.YASHANDB_NCLOB;

/**
 * JDBC row converter for YashanDB (崖山数据库).
 *
 * <p>This converter handles the conversion between SeaTunnel row data and
 * JDBC PreparedStatement parameters for YashanDB database operations.
 *
 * <p><b>Special Handling for LOB Types:</b>
 *
 * <p>YashanDB requires special handling for large object (LOB) types:
 * <ul>
 *   <li><b>BLOB:</b> Uses setBinaryStream() instead of setBytes() for proper handling
 *       of binary large objects</li>
 *   <li><b>CLOB:</b> Uses setCharacterStream() for character large objects</li>
 *   <li><b>NCLOB:</b> Uses setNCharacterStream() for national character large objects</li>
 * </ul>
 *
 * <p>This special handling is necessary because YashanDB's JDBC driver
 * may not properly handle LOB data with standard setBytes() or setString() methods,
 * especially for large data sizes exceeding certain thresholds.
 *
 * <p><b>Example Usage:</b>
 * <pre>
 * YashanDBJdbcRowConverter converter = new YashanDBJdbcRowConverter();
 * PreparedStatement ps = connection.prepareStatement("INSERT INTO table VALUES (?, ?, ?)");
 * converter.convert(ps, seaTunnelRow, columnTypes, sourceTypes);
 * </pre>
 *
 * @see YashanDBDialect
 * @see AbstractJdbcRowConverter
 */
public class YashanDBJdbcRowConverter extends AbstractJdbcRowConverter {

    /**
     * Returns the converter name identifier.
     *
     * @return "YASHANDB" constant
     */
    @Override
    public String converterName() {
        return DatabaseIdentifier.YASHANDB;
    }

    /**
     * Sets a value to PreparedStatement with special handling for YashanDB LOB types.
     *
     * <p>This method extends the parent class's implementation to handle
     * YashanDB-specific data types:
     *
     * <h3>BLOB Handling:</h3>
     * <p>For BLOB type columns, uses setBinaryStream() with ByteArrayInputStream
     * instead of setBytes(). This ensures proper handling of binary data
     * that may exceed the driver's internal buffer limits.
     *
     * <h3>CLOB Handling:</h3>
     * <p>For CLOB type columns, uses setCharacterStream() with StringReader
     * to properly stream large character data to the database.
     *
     * <h3>NCLOB Handling:</h3>
     * <p>For NCLOB type columns (national character large objects),
     * uses setNCharacterStream() for proper Unicode character handling.
     *
     * <p>For all other types, delegates to the parent class implementation.
     *
     * @param value the value to set
     * @param statement the PreparedStatement to set value on
     * @param seaTunnelDataType the SeaTunnel data type
     * @param statementIndex the parameter index in the statement (1-based)
     * @param sourceType the source database type name (e.g., "BLOB", "CLOB")
     * @throws SQLException if setting the value fails
     */
    @Override
    protected void setValueToStatementByDataType(
            Object value,
            PreparedStatement statement,
            SeaTunnelDataType<?> seaTunnelDataType,
            int statementIndex,
            @Nullable String sourceType)
            throws SQLException {
        if (seaTunnelDataType.getSqlType().equals(SqlType.BYTES)) {
            // Handle binary data - BLOB requires special streaming handling
            if (YASHANDB_BLOB.equals(sourceType)) {
                byte[] bytes = (byte[]) value;
                statement.setBinaryStream(
                        statementIndex, new ByteArrayInputStream(bytes), bytes.length);
            } else {
                // For RAW type, use standard setBytes()
                statement.setBytes(statementIndex, (byte[]) value);
            }
        } else if (seaTunnelDataType.getSqlType().equals(SqlType.STRING)) {
            // Handle string data - CLOB/NCLOB require special streaming handling
            if (YASHANDB_CLOB.equals(sourceType)) {
                String str = (String) value;
                statement.setCharacterStream(statementIndex, new StringReader(str), str.length());
            } else if (YASHANDB_NCLOB.equals(sourceType)) {
                String str = (String) value;
                statement.setNCharacterStream(statementIndex, new StringReader(str), str.length());
            } else {
                // For VARCHAR/CHAR types, use standard setString()
                statement.setString(statementIndex, (String) value);
            }
        } else {
            // For all other types, use parent class implementation
            super.setValueToStatementByDataType(
                    value, statement, seaTunnelDataType, statementIndex, sourceType);
        }
    }
}