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
import org.apache.seatunnel.api.table.catalog.PhysicalColumn;
import org.apache.seatunnel.api.table.converter.BasicTypeDefine;
import org.apache.seatunnel.api.table.converter.TypeConverter;
import org.apache.seatunnel.api.table.type.BasicType;
import org.apache.seatunnel.api.table.type.DecimalType;
import org.apache.seatunnel.api.table.type.LocalTimeType;
import org.apache.seatunnel.api.table.type.PrimitiveByteArrayType;
import org.apache.seatunnel.common.exception.CommonError;
import org.apache.seatunnel.connectors.seatunnel.common.source.TypeDefineUtils;
import org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.DatabaseIdentifier;

import com.google.auto.service.AutoService;
import lombok.extern.slf4j.Slf4j;

/**
 * Type converter for YashanDB (崖山数据库) data types.
 *
 * <p>This converter handles bidirectional mapping between SeaTunnel types and YashanDB types.
 * YashanDB is compatible with Oracle syntax, so many type mappings follow Oracle conventions.
 *
 * <p><b>Supported YashanDB Data Types:</b>
 *
 * <h3>Numeric Types:</h3>
 * <ul>
 *   <li>TINYINT, SMALLINT - mapped to SHORT_TYPE</li>
 *   <li>INT, INTEGER - mapped to INT_TYPE</li>
 *   <li>BIGINT - mapped to LONG_TYPE</li>
 *   <li>NUMBER, NUMERIC - mapped to DecimalType with precision/scale handling</li>
 *   <li>BINARY_FLOAT, REAL - mapped to FLOAT_TYPE</li>
 *   <li>DOUBLE, BINARY_DOUBLE - mapped to DOUBLE_TYPE</li>
 *   <li>BIT - mapped to BOOLEAN_TYPE</li>
 * </ul>
 *
 * <h3>Character Types:</h3>
 * <ul>
 *   <li>CHAR, NCHAR - fixed-length strings mapped to STRING_TYPE</li>
 *   <li>VARCHAR, NVARCHAR - variable-length strings mapped to STRING_TYPE</li>
 *   <li>CLOB, NCLOB - large text mapped to STRING_TYPE</li>
 * </ul>
 *
 * <h3>Datetime Types:</h3>
 * <ul>
 *   <li>DATE - mapped to LOCAL_DATE_TIME_TYPE</li>
 *   <li>TIME - mapped to LOCAL_TIME_TYPE</li>
 *   <li>TIMESTAMP - mapped to LOCAL_DATE_TIME_TYPE (max scale 6)</li>
 *   <li>TIMESTAMP WITH TIME ZONE - mapped to LOCAL_DATE_TIME_TYPE</li>
 *   <li>TIMESTAMP WITH LOCAL TIME ZONE - mapped to LOCAL_DATE_TIME_TYPE</li>
 * </ul>
 *
 * <h3>Binary Types:</h3>
 * <ul>
 *   <li>BLOB - mapped to PrimitiveByteArrayType or STRING_TYPE (configurable)</li>
 *   <li>RAW - mapped to PrimitiveByteArrayType</li>
 * </ul>
 *
 * <h3>Other Types:</h3>
 * <ul>
 *   <li>BOOLEAN - mapped to BOOLEAN_TYPE</li>
 *   <li>INTERVAL YEAR TO MONTH - mapped to STRING_TYPE</li>
 *   <li>INTERVAL DAY TO SECOND - mapped to STRING_TYPE (max scale 6)</li>
 * </ul>
 *
 * <p><b>Not Supported:</b>
 * <ul>
 *   <li>PLS_INTEGER - removed (use INTEGER instead)</li>
 *   <li>FLOAT - removed (use BINARY_FLOAT instead)</li>
 *   <li>DECIMAL - removed (use NUMBER instead)</li>
 *   <li>VARCHAR2 - removed (use VARCHAR instead)</li>
 *   <li>XML, JSON, ROWID, UROWID, ST_GEOMETRY, BFILE, UDT</li>
 * </ul>
 *
 * <p><b>Limitations:</b>
 * <ul>
 *   <li>TIMESTAMP/TIME/INTERVAL DAY TO SECOND fractional_seconds_precision max is 6</li>
 *   <li>USE_NATIVE_TYPE=FALSE mode is not supported</li>
 *   <li>YashanDB MySQL compatibility mode is not supported</li>
 * </ul>
 *
 * @see YashanDBDialect
 * @see BasicTypeDefine
 */
// reference https://doc.yashandb.com/yashandb/23.4/zh/All-Manuals/Development-Guide/SQL-Reference-Manual/Data-Types/00Data-Types.html
@Slf4j
@AutoService(TypeConverter.class)
public class YashanDBTypeConverter implements TypeConverter<BasicTypeDefine> {

    // ============================ Numeric Types ======================
    /** YashanDB TINYINT type (1-byte integer). */
    public static final String YASHANDB_TINYINT = "TINYINT";

    /** YashanDB SMALLINT type (2-byte integer). */
    public static final String YASHANDB_SMALLINT = "SMALLINT";

    /** YashanDB INT type (4-byte integer). */
    public static final String YASHANDB_INT = "INT";

    /** YashanDB INTEGER type (4-byte integer, alias for INT). */
    public static final String YASHANDB_INTEGER = "INTEGER";

    /** YashanDB BIGINT type (8-byte integer). */
    public static final String YASHANDB_BIGINT = "BIGINT";

    /** YashanDB NUMBER type (variable precision decimal, Oracle-compatible). */
    public static final String YASHANDB_NUMBER = "NUMBER";

    /** YashanDB NUMERIC type (alias for NUMBER). */
    public static final String YASHANDB_NUMERIC = "NUMERIC";

    /** YashanDB BINARY_FLOAT type (32-bit floating point). */
    public static final String YASHANDB_BINARY_FLOAT = "BINARY_FLOAT";

    /** YashanDB REAL type (alias for BINARY_FLOAT). */
    public static final String YASHANDB_REAL = "REAL";

    /** YashanDB DOUBLE type (64-bit floating point). */
    public static final String YASHANDB_DOUBLE = "DOUBLE";

    /** YashanDB BINARY_DOUBLE type (64-bit floating point, Oracle-compatible). */
    public static final String YASHANDB_BINARY_DOUBLE = "BINARY_DOUBLE";

    /** YashanDB BIT type (boolean bit). */
    public static final String YASHANDB_BIT = "BIT";

    // ============================ Character Types ====================
    /** YashanDB CHAR type (fixed-length character string). */
    public static final String YASHANDB_CHAR = "CHAR";

    /** YashanDB NCHAR type (fixed-length national character string). */
    public static final String YASHANDB_NCHAR = "NCHAR";

    /** YashanDB VARCHAR type (variable-length character string). */
    public static final String YASHANDB_VARCHAR = "VARCHAR";

    /** YashanDB NVARCHAR type (variable-length national character string). */
    public static final String YASHANDB_NVARCHAR = "NVARCHAR";

    // ============================ Datetime Types =====================
    /** YashanDB DATE type (date without time). */
    public static final String YASHANDB_DATE = "DATE";

    /** YashanDB TIME type (time without date). */
    public static final String YASHANDB_TIME = "TIME";

    /** YashanDB TIMESTAMP type (date and time with fractional seconds). */
    public static final String YASHANDB_TIMESTAMP = "TIMESTAMP";

    /** YashanDB TIMESTAMP WITH TIME ZONE type (timestamp with timezone offset). */
    public static final String YASHANDB_TIMESTAMP_WITH_TIME_ZONE = "TIMESTAMP WITH TIME ZONE";

    /** YashanDB TIMESTAMP WITH LOCAL TIME ZONE type (timestamp normalized to local timezone). */
    public static final String YASHANDB_TIMESTAMP_WITH_LOCAL_TIME_ZONE = "TIMESTAMP WITH LOCAL TIME ZONE";

    // ============================ Boolean Type =======================
    /** YashanDB BOOLEAN type. */
    public static final String YASHANDB_BOOLEAN = "BOOLEAN";

    // ============================ LOB Types ==========================
    /** YashanDB CLOB type (character large object). */
    public static final String YASHANDB_CLOB = "CLOB";

    /** YashanDB BLOB type (binary large object). */
    public static final String YASHANDB_BLOB = "BLOB";

    /** YashanDB NCLOB type (national character large object). */
    public static final String YASHANDB_NCLOB = "NCLOB";

    // ============================ Binary Type ========================
    /** YashanDB RAW type (raw binary data, max 2000 bytes). */
    public static final String YASHANDB_RAW = "RAW";

    // ============================ Interval Types =====================
    /** YashanDB INTERVAL YEAR TO MONTH type. */
    public static final String YASHANDB_INTERVAL_YEAR = "INTERVAL YEAR TO MONTH";

    /** YashanDB INTERVAL DAY TO SECOND type. */
    public static final String YASHANDB_INTERVAL_DAY = "INTERVAL DAY TO SECOND";

    /** Maximum precision for NUMBER type. */
    public static final int MAX_PRECISION = 38;

    /** Default precision when not specified. */
    public static final int DEFAULT_PRECISION = MAX_PRECISION;

    /** Maximum scale for NUMBER type. */
    public static final int MAX_SCALE = 127;

    /** Default scale for NUMBER type. */
    public static final int DEFAULT_SCALE = 18;

    /** Default fractional seconds precision for TIMESTAMP. */
    public static final int TIMESTAMP_DEFAULT_SCALE = 6;

    /**
     * Maximum fractional seconds precision for TIMESTAMP/TIME/INTERVAL.
     *
     * <p><b>Note:</b> YashanDB supports up to 9 digits, but SeaTunnel only supports 6
     * to maintain compatibility with other databases.
     */
    public static final int MAX_TIMESTAMP_SCALE = 6;

    /** Maximum length for RAW type. */
    public static final long MAX_RAW_LENGTH = 2000;

    /** Maximum length for CHAR type. */
    public static final long MAX_CHAR_LENGTH = 8000;

    /** Maximum length for VARCHAR type. */
    public static final long MAX_VARCHAR_LENGTH = 32000;

    /** Size constant: 2GB in bytes. */
    public static final long BYTES_2GB = (long) Math.pow(2, 31);

    /** Size constant: 4GB in bytes (max LOB size). */
    public static final long BYTES_4GB = (long) Math.pow(2, 32);

    /** Singleton instance with default settings. */
    public static final YashanDBTypeConverter INSTANCE = new YashanDBTypeConverter();

    /** Whether to narrow NUMBER type to smaller integer types when possible. */
    private final boolean decimalTypeNarrowing;

    /** Whether to handle BLOB type as String instead of byte array. */
    private final boolean handleBlobAsString;

    /**
     * Creates a converter with default settings.
     *
     * <p>Enables decimal type narrowing and disables BLOB as string handling.
     */
    public YashanDBTypeConverter() {
        this(true, false);
    }

    /**
     * Creates a converter with decimal narrowing option.
     *
     * @param decimalTypeNarrowing whether to narrow NUMBER to smaller types
     */
    public YashanDBTypeConverter(boolean decimalTypeNarrowing) {
        this(decimalTypeNarrowing, false);
    }

    /**
     * Creates a converter with full configuration.
     *
     * @param decimalTypeNarrowing whether to narrow NUMBER(p,0) to INT/LONG when p is small
     * @param handleBlobAsString whether to treat BLOB as String type
     */
    public YashanDBTypeConverter(boolean decimalTypeNarrowing, boolean handleBlobAsString) {
        this.decimalTypeNarrowing = decimalTypeNarrowing;
        this.handleBlobAsString = handleBlobAsString;
    }

    /**
     * Returns the converter identifier.
     *
     * @return "YASHANDB" constant
     */
    @Override
    public String identifier() {
        return DatabaseIdentifier.YASHANDB;
    }

    /**
     * Converts YashanDB type definition to SeaTunnel Column.
     *
     * <p>This method maps YashanDB native types to SeaTunnel types,
     * handling precision, scale, and length appropriately.
     *
     * <p><b>Special Handling:</b>
     * <ul>
     *   <li>NUMBER type: precision/scale determines mapping to INT, LONG, or Decimal</li>
     *   <li>TIMESTAMP types: scale capped at MAX_TIMESTAMP_SCALE (6)</li>
     *   <li>BLOB type: can be mapped to byte array or String based on configuration</li>
     * </ul>
     *
     * @param typeDefine the YashanDB type definition
     * @return the converted SeaTunnel Column
     * @throws CommonError if the type is not supported
     */
    @Override
    public Column convert(BasicTypeDefine typeDefine) {
        PhysicalColumn.PhysicalColumnBuilder builder =
                PhysicalColumn.builder()
                        .name(typeDefine.getName())
                        .sourceType(typeDefine.getColumnType())
                        .nullable(typeDefine.isNullable())
                        .defaultValue(typeDefine.getDefaultValue())
                        .comment(typeDefine.getComment());

        String yashandbType = typeDefine.getDataType().toUpperCase();

        switch (yashandbType) {
            case YASHANDB_TINYINT:
            case YASHANDB_SMALLINT:
                builder.dataType(BasicType.SHORT_TYPE);
                break;
            case YASHANDB_INT:
            case YASHANDB_INTEGER:
                builder.dataType(BasicType.INT_TYPE);
                break;
            case YASHANDB_BIGINT:
                builder.dataType(BasicType.LONG_TYPE);
                break;
            case YASHANDB_NUMBER:
            case YASHANDB_NUMERIC:
                Long precision = typeDefine.getPrecision();
                if (precision == null || precision == 0 || precision > DEFAULT_PRECISION) {
                    precision = Long.valueOf(DEFAULT_PRECISION);
                }
                Integer scale = typeDefine.getScale();
                if (scale == null) {
                    scale = 127;
                }

                if (scale <= 0) {
                    // Integer NUMBER (scale = 0 or negative)
                    int newPrecision = (int) (precision - scale);
                    if (newPrecision <= 18 && decimalTypeNarrowing) {
                        if (newPrecision == 1) {
                            builder.dataType(BasicType.BOOLEAN_TYPE);
                        } else if (newPrecision <= 9) {
                            builder.dataType(BasicType.INT_TYPE);
                        } else {
                            builder.dataType(BasicType.LONG_TYPE);
                        }
                    } else if (newPrecision < 38) {
                        builder.dataType(new DecimalType(newPrecision, 0));
                        builder.columnLength((long) newPrecision);
                    } else {
                        builder.dataType(new DecimalType(DEFAULT_PRECISION, 0));
                        builder.columnLength((long) DEFAULT_PRECISION);
                    }
                } else if (scale <= DEFAULT_SCALE) {
                    builder.dataType(new DecimalType(precision.intValue(), scale));
                    builder.columnLength(precision);
                    builder.scale(scale);
                } else {
                    builder.dataType(new DecimalType(precision.intValue(), DEFAULT_SCALE));
                    builder.columnLength(precision);
                    builder.scale(DEFAULT_SCALE);
                }
                break;
            case YASHANDB_BINARY_FLOAT:
            case YASHANDB_REAL:
                builder.dataType(BasicType.FLOAT_TYPE);
                break;
            case YASHANDB_DOUBLE:
            case YASHANDB_BINARY_DOUBLE:
                builder.dataType(BasicType.DOUBLE_TYPE);
                break;
            case YASHANDB_BIT:
                builder.dataType(BasicType.BOOLEAN_TYPE);
                break;
            case YASHANDB_CHAR:
                builder.dataType(BasicType.STRING_TYPE);
                builder.columnLength(TypeDefineUtils.charTo4ByteLength(typeDefine.getLength()));
                break;
            case YASHANDB_VARCHAR:
                builder.dataType(BasicType.STRING_TYPE);
                if (typeDefine.getLength() == null || typeDefine.getLength() <= 0) {
                    builder.columnLength(TypeDefineUtils.charTo4ByteLength(MAX_VARCHAR_LENGTH));
                } else {
                    builder.columnLength(TypeDefineUtils.charTo4ByteLength(typeDefine.getLength()));
                }
                break;
            case YASHANDB_NCHAR:
            case YASHANDB_NVARCHAR:
                builder.dataType(BasicType.STRING_TYPE);
                builder.columnLength(
                        TypeDefineUtils.doubleByteTo4ByteLength(typeDefine.getLength()));
                break;
            case YASHANDB_CLOB:
            case YASHANDB_NCLOB:
                builder.dataType(BasicType.STRING_TYPE);
                builder.columnLength(BYTES_4GB - 1);
                break;
            case YASHANDB_BLOB:
                if (handleBlobAsString) {
                    builder.dataType(BasicType.STRING_TYPE);
                    builder.columnLength(BYTES_4GB - 1);
                } else {
                    builder.dataType(PrimitiveByteArrayType.INSTANCE);
                    builder.columnLength(BYTES_4GB - 1);
                }
                break;
            case YASHANDB_RAW:
                builder.dataType(PrimitiveByteArrayType.INSTANCE);
                if (typeDefine.getLength() == null || typeDefine.getLength() == 0) {
                    builder.columnLength(MAX_RAW_LENGTH);
                } else {
                    builder.columnLength(typeDefine.getLength());
                }
                break;
            case YASHANDB_DATE:
                builder.dataType(LocalTimeType.LOCAL_DATE_TIME_TYPE);
                break;
            case YASHANDB_TIME:
                builder.dataType(LocalTimeType.LOCAL_TIME_TYPE);
                break;
            case YASHANDB_TIMESTAMP:
            case YASHANDB_TIMESTAMP_WITH_TIME_ZONE:
            case YASHANDB_TIMESTAMP_WITH_LOCAL_TIME_ZONE:
                builder.dataType(LocalTimeType.LOCAL_DATE_TIME_TYPE);
                if (typeDefine.getScale() == null) {
                    builder.scale(TIMESTAMP_DEFAULT_SCALE);
                } else {
                    builder.scale(typeDefine.getScale());
                }
                break;
            case YASHANDB_BOOLEAN:
                builder.dataType(BasicType.BOOLEAN_TYPE);
                break;
            case YASHANDB_INTERVAL_YEAR:
            case YASHANDB_INTERVAL_DAY:
                builder.dataType(BasicType.STRING_TYPE);
                builder.columnLength(64L);
                break;
            default:
                throw CommonError.convertToSeaTunnelTypeError(
                        DatabaseIdentifier.YASHANDB, yashandbType, typeDefine.getName());
        }
        return builder.build();
    }

    /**
     * Converts SeaTunnel Column back to YashanDB type definition.
     *
     * <p>This method is used when creating tables or altering schema,
     * mapping SeaTunnel types back to appropriate YashanDB types.
     *
     * <p><b>Mapping Rules:</b>
     * <ul>
     *   <li>BOOLEAN -> NUMBER(1)</li>
     *   <li>TINYINT/SMALLINT -> SMALLINT</li>
     *   <li>INT -> INT</li>
     *   <li>BIGINT -> BIGINT</li>
     *   <li>FLOAT -> BINARY_FLOAT</li>
     *   <li>DOUBLE -> BINARY_DOUBLE</li>
     *   <li>DECIMAL -> NUMBER(p,s)</li>
     *   <li>BYTES -> RAW(n) or BLOB</li>
     *   <li>STRING -> VARCHAR(n) or CLOB</li>
     *   <li>DATE -> DATE</li>
     *   <li>TIME -> TIME</li>
     *   <li>TIMESTAMP -> TIMESTAMP(n)</li>
     * </ul>
     *
     * @param column the SeaTunnel column to convert
     * @return the YashanDB type definition
     * @throws CommonError if the SeaTunnel type is not supported
     */
    @Override
    public BasicTypeDefine reconvert(Column column) {
        BasicTypeDefine.BasicTypeDefineBuilder builder =
                BasicTypeDefine.builder()
                        .name(column.getName())
                        .nullable(column.isNullable())
                        .comment(column.getComment())
                        .defaultValue(column.getDefaultValue());
        switch (column.getDataType().getSqlType()) {
            case BOOLEAN:
                builder.columnType(String.format("%s(%s)", YASHANDB_NUMBER, 1));
                builder.dataType(YASHANDB_NUMBER);
                builder.length(1L);
                break;
            case TINYINT:
            case SMALLINT:
                builder.columnType(YASHANDB_SMALLINT);
                builder.dataType(YASHANDB_SMALLINT);
                break;
            case INT:
                builder.columnType(YASHANDB_INT);
                builder.dataType(YASHANDB_INT);
                break;
            case BIGINT:
                builder.columnType(YASHANDB_BIGINT);
                builder.dataType(YASHANDB_BIGINT);
                break;
            case FLOAT:
                builder.columnType(YASHANDB_BINARY_FLOAT);
                builder.dataType(YASHANDB_BINARY_FLOAT);
                break;
            case DOUBLE:
                builder.columnType(YASHANDB_BINARY_DOUBLE);
                builder.dataType(YASHANDB_BINARY_DOUBLE);
                break;
            case DECIMAL:
                DecimalType decimalType = (DecimalType) column.getDataType();
                long precision = decimalType.getPrecision();
                int scale = decimalType.getScale();
                if (precision <= 0) {
                    precision = DEFAULT_PRECISION;
                    scale = DEFAULT_SCALE;
                    log.warn(
                            "The decimal column {} type decimal({},{}) is out of range, "
                                    + "which is precision less than 0, "
                                    + "it will be converted to decimal({},{})",
                            column.getName(),
                            decimalType.getPrecision(),
                            decimalType.getScale(),
                            precision,
                            scale);
                } else if (precision > MAX_PRECISION) {
                    scale = (int) Math.max(0, scale - (precision - MAX_PRECISION));
                    precision = MAX_PRECISION;
                    log.warn(
                            "The decimal column {} type decimal({},{}) is out of range, "
                                    + "which exceeds the maximum precision of {}, "
                                    + "it will be converted to decimal({},{})",
                            column.getName(),
                            decimalType.getPrecision(),
                            decimalType.getScale(),
                            MAX_PRECISION,
                            precision,
                            scale);
                }
                if (scale < 0) {
                    scale = 0;
                    log.warn(
                            "The decimal column {} type decimal({},{}) is out of range, "
                                    + "which is scale less than 0, "
                                    + "it will be converted to decimal({},{})",
                            column.getName(),
                            decimalType.getPrecision(),
                            decimalType.getScale(),
                            precision,
                            scale);
                } else if (scale > MAX_SCALE) {
                    scale = MAX_SCALE;
                    log.warn(
                            "The decimal column {} type decimal({},{}) is out of range, "
                                    + "which exceeds the maximum scale of {}, "
                                    + "it will be converted to decimal({},{})",
                            column.getName(),
                            decimalType.getPrecision(),
                            decimalType.getScale(),
                            MAX_SCALE,
                            precision,
                            scale);
                }
                builder.columnType(String.format("%s(%s,%s)", YASHANDB_NUMBER, precision, scale));
                builder.dataType(YASHANDB_NUMBER);
                builder.precision(precision);
                builder.scale(scale);
                break;
            case BYTES:
                if (column.getColumnLength() == null || column.getColumnLength() <= 0) {
                    builder.columnType(YASHANDB_BLOB);
                    builder.dataType(YASHANDB_BLOB);
                } else if (column.getColumnLength() <= MAX_RAW_LENGTH) {
                    builder.columnType(
                            String.format("%s(%s)", YASHANDB_RAW, column.getColumnLength()));
                    builder.dataType(YASHANDB_RAW);
                } else {
                    builder.columnType(YASHANDB_BLOB);
                    builder.dataType(YASHANDB_BLOB);
                }
                break;
            case STRING:
                if (column.getColumnLength() == null || column.getColumnLength() <= 0) {
                    builder.columnType(
                            String.format("%s(%s)", YASHANDB_VARCHAR, MAX_VARCHAR_LENGTH));
                    builder.dataType(YASHANDB_VARCHAR);
                } else if (column.getColumnLength() <= MAX_VARCHAR_LENGTH) {
                    builder.columnType(
                            String.format("%s(%s)", YASHANDB_VARCHAR, column.getColumnLength()));
                    builder.dataType(YASHANDB_VARCHAR);
                } else {
                    builder.columnType(YASHANDB_CLOB);
                    builder.dataType(YASHANDB_CLOB);
                }
                break;
            case DATE:
                builder.columnType(YASHANDB_DATE);
                builder.dataType(YASHANDB_DATE);
                break;
            case TIME:
                builder.columnType(YASHANDB_TIME);
                builder.dataType(YASHANDB_TIME);
                break;
            case TIMESTAMP:
                if (column.getScale() == null || column.getScale() <= 0) {
                    builder.columnType(YASHANDB_TIMESTAMP);
                } else {
                    int timestampScale = column.getScale();
                    if (column.getScale() > MAX_TIMESTAMP_SCALE) {
                        timestampScale = MAX_TIMESTAMP_SCALE;
                        log.warn(
                                "The timestamp column {} type timestamp({}) is out of range, "
                                        + "which exceeds the maximum scale of {}, "
                                        + "it will be converted to timestamp({})",
                                column.getName(),
                                column.getScale(),
                                MAX_TIMESTAMP_SCALE,
                                timestampScale);
                    }
                    builder.columnType(
                            String.format("%s(%s)", YASHANDB_TIMESTAMP, timestampScale));
                    builder.scale(timestampScale);
                }
                builder.dataType(YASHANDB_TIMESTAMP);
                break;
            default:
                throw CommonError.convertToConnectorTypeError(
                        DatabaseIdentifier.YASHANDB,
                        column.getDataType().getSqlType().name(),
                        column.getName());
        }
        return builder.build();
    }
}