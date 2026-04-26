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

import org.apache.seatunnel.api.table.catalog.Column;
import org.apache.seatunnel.api.table.converter.BasicTypeDefine;
import org.apache.seatunnel.connectors.seatunnel.common.source.TypeDefineUtils;
import org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.JdbcDialectTypeMapper;

import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * JDBC type mapper for YashanDB (崖山数据库).
 *
 * <p>This mapper is responsible for converting JDBC ResultSet metadata
 * into SeaTunnel Column definitions. It handles the mapping between
 * JDBC type information and YashanDB-specific data types.
 *
 * <p><b>Key Responsibilities:</b>
 * <ul>
 *   <li>Extract column metadata from ResultSetMetaData</li>
 *   <li>Handle YashanDB-specific type quirks (e.g., NUMBER with scale=-127)</li>
 *   <li>Convert double-byte character lengths for NCHAR/NVARCHAR</li>
 *   <li>Delegate to {@link YashanDBTypeConverter} for final type conversion</li>
 * </ul>
 *
 * <p><b>Special Type Handling:</b>
 *
 * <h3>NUMBER with scale=-127:</h3>
 * <p>When NUMBER type has scale=-127, it indicates a FLOAT type in YashanDB.
 * This mapper detects this special case and converts "number" to "float"
 * for proper type mapping.
 *
 * <h3>NCHAR/NVARCHAR Length:</h3>
 * <p>National character types (NCHAR, NVARCHAR) use double-byte encoding.
 * The precision value from JDBC needs to be converted from character count
 * to byte count for proper SeaTunnel type definition.
 *
 * @see YashanDBTypeConverter
 * @see YashanDBDialect
 * @see JdbcDialectTypeMapper
 */
@Slf4j
public class YashanDBTypeMapper implements JdbcDialectTypeMapper {

    /** Whether to narrow NUMBER type to smaller integer types when possible. */
    private final boolean decimalTypeNarrowing;

    /** Whether to handle BLOB type as String instead of byte array. */
    private final boolean handleBlobAsString;

    /**
     * Creates a mapper with default settings.
     *
     * <p>Enables decimal type narrowing and disables BLOB as string handling.
     */
    public YashanDBTypeMapper() {
        this(true, false);
    }

    /**
     * Creates a mapper with decimal narrowing option.
     *
     * @param decimalTypeNarrowing whether to narrow NUMBER to smaller types
     */
    public YashanDBTypeMapper(boolean decimalTypeNarrowing) {
        this(decimalTypeNarrowing, false);
    }

    /**
     * Creates a mapper with full configuration.
     *
     * @param decimalTypeNarrowing whether to narrow NUMBER(p,0) to INT/LONG when p is small
     * @param handleBlobAsString whether to treat BLOB as String type
     */
    public YashanDBTypeMapper(boolean decimalTypeNarrowing, boolean handleBlobAsString) {
        this.decimalTypeNarrowing = decimalTypeNarrowing;
        this.handleBlobAsString = handleBlobAsString;
    }

    /**
     * Maps a BasicTypeDefine to a SeaTunnel Column using {@link YashanDBTypeConverter}.
     *
     * <p>This method is a convenience wrapper that creates a type converter
     * with the same configuration and delegates the conversion.
     *
     * @param typeDefine the basic type definition from catalog
     * @return the converted SeaTunnel Column
     */
    @Override
    public Column mappingColumn(BasicTypeDefine typeDefine) {
        return new YashanDBTypeConverter(decimalTypeNarrowing, handleBlobAsString)
                .convert(typeDefine);
    }

    /**
     * Maps a JDBC ResultSetMetaData column to a SeaTunnel Column.
     *
     * <p>This method extracts column information from JDBC metadata and
     * handles YashanDB-specific type quirks before delegating to
     * {@link #mappingColumn(BasicTypeDefine)}.
     *
     * <p><b>Special Handling:</b>
     * <ul>
     *   <li>NUMBER with scale=-127: Converted to FLOAT type indication</li>
     *   <li>NCHAR/NVARCHAR: Precision converted from character count to byte count</li>
     * </ul>
     *
     * <p><b>JDBC Metadata Used:</b>
     * <ul>
     *   <li>getColumnLabel() - column name/alias</li>
     *   <li>getColumnTypeName() - native database type name</li>
     *   <li>isNullable() - nullability indicator</li>
     *   <li>getPrecision() - precision/length</li>
     *   <li>getScale() - scale for numeric types</li>
     * </ul>
     *
     * @param metadata the ResultSet metadata
     * @param colIndex the column index (1-based, as per JDBC convention)
     * @return the converted SeaTunnel Column
     * @throws SQLException if metadata extraction fails
     */
    @Override
    public Column mappingColumn(ResultSetMetaData metadata, int colIndex) throws SQLException {
        String columnName = metadata.getColumnLabel(colIndex);
        String nativeType = metadata.getColumnTypeName(colIndex);
        int isNullable = metadata.isNullable(colIndex);
        long precision = metadata.getPrecision(colIndex);
        int scale = metadata.getScale(colIndex);

        // Handle NUMBER type with scale=-127 (indicates FLOAT in YashanDB)
        if ("number".equalsIgnoreCase(nativeType) && scale == -127) {
            nativeType = "float";
        } else if (Arrays.asList("NVARCHAR", "NCHAR").contains(nativeType.toUpperCase())) {
            // Convert national character precision to double-byte length
            // NCHAR/NVARCHAR use 2 bytes per character, so multiply by 2
            long doubleByteLength = TypeDefineUtils.charToDoubleByteLength(precision);
            precision = doubleByteLength;
        }

        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name(columnName)
                        .columnType(nativeType)
                        .dataType(nativeType)
                        .nullable(isNullable == ResultSetMetaData.columnNullable)
                        .length(precision)
                        .precision(precision)
                        .scale(scale)
                        .build();
        return mappingColumn(typeDefine);
    }
}