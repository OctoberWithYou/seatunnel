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

import org.apache.seatunnel.api.table.catalog.Column;
import org.apache.seatunnel.api.table.catalog.DataTypeConvertor;
import org.apache.seatunnel.api.table.catalog.PhysicalColumn;
import org.apache.seatunnel.api.table.converter.BasicTypeDefine;
import org.apache.seatunnel.api.table.type.SeaTunnelDataType;
import org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.DatabaseIdentifier;
import org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.yashandb.YashanDBTypeConverter;

import org.apache.commons.collections4.MapUtils;

import com.google.auto.service.AutoService;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.seatunnel.shade.com.google.common.base.Preconditions.checkNotNull;

/**
 * Legacy data type convertor for YashanDB.
 *
 * <p><b>⚠️ Deprecated:</b> This class is deprecated and should not be used for new implementations.
 * Use {@link YashanDBTypeConverter} instead, which provides more comprehensive type conversion
 * with better support for YashanDB-specific data types.
 *
 * <p>This class was the original implementation for YashanDB type conversion,
 * but it has been superseded by the more feature-rich TypeConverter framework.
 * It remains registered as a DataTypeConvertor for backward compatibility with
 * older configurations and code paths.
 *
 * <p><b>Key Differences from YashanDBTypeConverter:</b>
 * <ul>
 *   <li>Uses String as the connector data type representation</li>
 *   <li>Simpler type mapping logic</li>
 *   <li>Does not handle all YashanDB-specific types (e.g., BLOB handling options)</li>
 *   <li>Does not support decimal type narrowing configuration</li>
 * </ul>
 *
 * <p><b>Supported Conversions:</b>
 * <ul>
 *   <li>NUMBER type with precision/scale properties</li>
 *   <li>TIMESTAMP types (normalized via {@link #normalizeTimestamp})</li>
 *   <li>Basic SeaTunnel types to YashanDB types</li>
 * </ul>
 *
 * @deprecated Use {@link YashanDBTypeConverter} instead for all new implementations.
 *             This class is retained only for backward compatibility.
 * @see YashanDBTypeConverter
 * @see DataTypeConvertor
 */
/** @deprecated instead by {@link YashanDBTypeConverter} */
@Deprecated
@AutoService(DataTypeConvertor.class)
public class YashanDBDataTypeConvertor implements DataTypeConvertor<String> {

    /** Configuration key for NUMBER precision. */
    public static final String PRECISION = "precision";

    /** Configuration key for NUMBER scale. */
    public static final String SCALE = "scale";

    /** Default precision for NUMBER type when not specified. */
    public static final Long DEFAULT_PRECISION = 38L;

    /** Default scale for NUMBER type when not specified. */
    public static final Integer DEFAULT_SCALE = 18;

    /**
     * Converts a YashanDB type to SeaTunnel type without properties.
     *
     * <p>This is a convenience method that delegates to the full conversion
     * method with an empty properties map.
     *
     * @param field the field name
     * @param connectorDataType the YashanDB type name
     * @return the corresponding SeaTunnel data type
     */
    @Override
    public SeaTunnelDataType<?> toSeaTunnelType(String field, String connectorDataType) {
        return toSeaTunnelType(field, connectorDataType, Collections.emptyMap());
    }

    /**
     * Converts a YashanDB type to SeaTunnel type with properties.
     *
     * <p>Handles NUMBER type specially by extracting precision and scale
     * from the properties map. For other types, uses basic type mapping.
     *
     * <p>The conversion is done by delegating to {@link YashanDBTypeConverter}
     * after building an appropriate BasicTypeDefine instance.
     *
     * @param field the field name
     * @param connectorDataType the YashanDB type name (e.g., "NUMBER", "VARCHAR")
     * @param dataTypeProperties additional type properties (precision, scale)
     * @return the corresponding SeaTunnel data type
     * @throws NullPointerException if connectorDataType is null
     */
    @Override
    public SeaTunnelDataType<?> toSeaTunnelType(
            String field, String connectorDataType, Map<String, Object> dataTypeProperties) {
        checkNotNull(connectorDataType, "YashanDB Type cannot be null");

        Long precision = null;
        Integer scale = null;
        switch (connectorDataType.toUpperCase()) {
            case YashanDBTypeConverter.YASHANDB_NUMBER:
                precision = MapUtils.getLong(dataTypeProperties, PRECISION, DEFAULT_PRECISION);
                scale = MapUtils.getInteger(dataTypeProperties, SCALE, DEFAULT_SCALE);
                break;
            default:
                break;
        }

        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name(field)
                        .columnType(connectorDataType)
                        .dataType(normalizeTimestamp(connectorDataType))
                        .length(precision)
                        .precision(precision)
                        .scale(scale)
                        .build();

        return YashanDBTypeConverter.INSTANCE.convert(typeDefine).getDataType();
    }

    /**
     * Converts a SeaTunnel type to YashanDB type.
     *
     * <p>Builds a Column from the SeaTunnel type and uses
     * {@link YashanDBTypeConverter} to determine the appropriate
     * YashanDB column type string.
     *
     * @param field the field name
     * @param seaTunnelDataType the SeaTunnel data type
     * @param dataTypeProperties additional properties (precision, scale)
     * @return the YashanDB column type string (e.g., "NUMBER(38,18)")
     * @throws NullPointerException if seaTunnelDataType is null
     */
    @Override
    public String toConnectorType(
            String field,
            SeaTunnelDataType<?> seaTunnelDataType,
            Map<String, Object> dataTypeProperties) {
        checkNotNull(seaTunnelDataType, "seaTunnelDataType cannot be null");

        Long precision = MapUtils.getLong(dataTypeProperties, PRECISION);
        Integer scale = MapUtils.getInteger(dataTypeProperties, SCALE);
        Column column =
                PhysicalColumn.builder()
                        .name(field)
                        .dataType(seaTunnelDataType)
                        .columnLength(precision)
                        .scale(scale)
                        .nullable(true)
                        .build();

        BasicTypeDefine typeDefine = YashanDBTypeConverter.INSTANCE.reconvert(column);
        return typeDefine.getColumnType();
    }

    /**
     * Normalizes TIMESTAMP type names to remove precision suffix.
     *
     * <p>YashanDB may return TIMESTAMP types with precision like "TIMESTAMP(6)".
     * This method normalizes them to just "TIMESTAMP" for consistent handling.
     *
     * <p><b>Pattern:</b> Matches "TIMESTAMP" optionally followed by "(n)" where n is a digit.
     *
     * <p><b>Examples:</b>
     * <ul>
     *   <li>"TIMESTAMP" -> "TIMESTAMP"</li>
     *   <li>"TIMESTAMP(6)" -> "TIMESTAMP"</li>
     *   <li>"VARCHAR" -> "VARCHAR" (unchanged)</li>
     * </ul>
     *
     * @param yashandbType the original YashanDB type name
     * @return the normalized type name (TIMESTAMP without precision suffix)
     */
    public static String normalizeTimestamp(String yashandbType) {
        String pattern = "^TIMESTAMP(\\([0-9]\\))?$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(yashandbType);
        if (m.find()) {
            return "TIMESTAMP";
        } else {
            return yashandbType;
        }
    }

    /**
     * Returns the identifier for this convertor.
     *
     * @return "YASHANDB" constant
     */
    @Override
    public String getIdentity() {
        return DatabaseIdentifier.YASHANDB;
    }
}