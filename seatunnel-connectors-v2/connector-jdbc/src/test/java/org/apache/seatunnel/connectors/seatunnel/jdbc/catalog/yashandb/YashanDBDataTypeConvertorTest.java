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
import org.apache.seatunnel.api.table.catalog.PhysicalColumn;
import org.apache.seatunnel.api.table.converter.BasicTypeDefine;
import org.apache.seatunnel.api.table.type.BasicType;
import org.apache.seatunnel.api.table.type.DecimalType;
import org.apache.seatunnel.api.table.type.LocalTimeType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Test for {@link YashanDBDataTypeConvertor}. */
public class YashanDBDataTypeConvertorTest {

    private final YashanDBDataTypeConvertor convertor = new YashanDBDataTypeConvertor();

    @Test
    public void testGetIdentity() {
        Assertions.assertEquals("YashanDB", convertor.getIdentity());
    }

    @Test
    public void testToSeaTunnelTypeBasic() {
        // Test without properties
        var result = convertor.toSeaTunnelType("col1", "INT");
        Assertions.assertEquals(BasicType.INT_TYPE, result);
    }

    @Test
    public void testToSeaTunnelTypeVarchar() {
        var result = convertor.toSeaTunnelType("col1", "VARCHAR");
        Assertions.assertEquals(BasicType.STRING_TYPE, result);
    }

    @Test
    public void testToSeaTunnelTypeNumber() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("precision", 10L);
        properties.put("scale", 2);

        var result = convertor.toSeaTunnelType("col1", "NUMBER", properties);
        Assertions.assertTrue(result instanceof DecimalType);
        DecimalType decimalType = (DecimalType) result;
        Assertions.assertEquals(10, decimalType.getPrecision());
        Assertions.assertEquals(2, decimalType.getScale());
    }

    @Test
    public void testToSeaTunnelTypeNumberDefault() {
        // Test NUMBER with default precision/scale
        var result = convertor.toSeaTunnelType("col1", "NUMBER", Collections.emptyMap());
        Assertions.assertTrue(result instanceof DecimalType);
    }

    @Test
    public void testToSeaTunnelTypeNumberWithoutScale() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("precision", 15L);

        var result = convertor.toSeaTunnelType("col1", "NUMBER", properties);
        Assertions.assertTrue(result instanceof DecimalType);
    }

    @Test
    public void testToSeaTunnelTypeTimestamp() {
        var result = convertor.toSeaTunnelType("col1", "TIMESTAMP");
        Assertions.assertEquals(LocalTimeType.LOCAL_DATE_TIME_TYPE, result);
    }

    @Test
    public void testToSeaTunnelTypeTimestampWithPrecision() {
        // Test TIMESTAMP(6) normalization
        var result = convertor.toSeaTunnelType("col1", "TIMESTAMP(6)");
        Assertions.assertEquals(LocalTimeType.LOCAL_DATE_TIME_TYPE, result);
    }

    @Test
    public void testToSeaTunnelTypeTimestampWithTimezone() {
        var result = convertor.toSeaTunnelType("col1", "TIMESTAMP WITH TIME ZONE");
        Assertions.assertEquals(LocalTimeType.LOCAL_DATE_TIME_TYPE, result);
    }

    @Test
    public void testToSeaTunnelTypeDate() {
        var result = convertor.toSeaTunnelType("col1", "DATE");
        Assertions.assertEquals(LocalTimeType.LOCAL_DATE_TIME_TYPE, result);
    }

    @Test
    public void testToSeaTunnelTypeBoolean() {
        var result = convertor.toSeaTunnelType("col1", "BOOLEAN");
        Assertions.assertEquals(BasicType.BOOLEAN_TYPE, result);
    }

    @Test
    public void testToSeaTunnelTypeBlob() {
        var result = convertor.toSeaTunnelType("col1", "BLOB");
        Assertions.assertNotNull(result);
    }

    @Test
    public void testToSeaTunnelTypeClob() {
        var result = convertor.toSeaTunnelType("col1", "CLOB");
        Assertions.assertEquals(BasicType.STRING_TYPE, result);
    }

    @Test
    public void testToConnectorTypeInt() {
        Map<String, Object> properties = Collections.emptyMap();
        String result = convertor.toConnectorType("col1", BasicType.INT_TYPE, properties);
        Assertions.assertEquals("INT", result);
    }

    @Test
    public void testToConnectorTypeLong() {
        String result = convertor.toConnectorType("col1", BasicType.LONG_TYPE, Collections.emptyMap());
        Assertions.assertEquals("BIGINT", result);
    }

    @Test
    public void testToConnectorTypeString() {
        Column column =
                PhysicalColumn.builder()
                        .name("col1")
                        .dataType(BasicType.STRING_TYPE)
                        .columnLength(255L)
                        .build();
        BasicTypeDefine typeDefine = YashanDBTypeConverter.INSTANCE.reconvert(column);

        String result = typeDefine.getColumnType();
        Assertions.assertTrue(result.contains("VARCHAR"));
    }

    @Test
    public void testToConnectorTypeDecimal() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("precision", 10L);
        properties.put("scale", 2);

        String result = convertor.toConnectorType("col1", new DecimalType(10, 2), properties);
        Assertions.assertTrue(result.contains("NUMBER"));
        Assertions.assertTrue(result.contains("10"));
        Assertions.assertTrue(result.contains("2"));
    }

    @Test
    public void testToConnectorTypeDate() {
        String result =
                convertor.toConnectorType("col1", LocalTimeType.LOCAL_DATE_TIME_TYPE, Collections.emptyMap());
        Assertions.assertTrue(result.contains("TIMESTAMP"));
    }

    @Test
    public void testNormalizeTimestampBasic() {
        String result = YashanDBDataTypeConvertor.normalizeTimestamp("TIMESTAMP");
        Assertions.assertEquals("TIMESTAMP", result);
    }

    @Test
    public void testNormalizeTimestampWithPrecision() {
        String result = YashanDBDataTypeConvertor.normalizeTimestamp("TIMESTAMP(6)");
        Assertions.assertEquals("TIMESTAMP", result);
    }

    @Test
    public void testNormalizeTimestampWithPrecision3() {
        String result = YashanDBDataTypeConvertor.normalizeTimestamp("TIMESTAMP(3)");
        Assertions.assertEquals("TIMESTAMP", result);
    }

    @Test
    public void testNormalizeTimestampWithPrecision9() {
        String result = YashanDBDataTypeConvertor.normalizeTimestamp("TIMESTAMP(9)");
        Assertions.assertEquals("TIMESTAMP", result);
    }

    @Test
    public void testNormalizeTimestampNonTimestamp() {
        String result = YashanDBDataTypeConvertor.normalizeTimestamp("VARCHAR");
        Assertions.assertEquals("VARCHAR", result);
    }

    @Test
    public void testNormalizeTimestampNonTimestampDate() {
        String result = YashanDBDataTypeConvertor.normalizeTimestamp("DATE");
        Assertions.assertEquals("DATE", result);
    }

    @Test
    public void testNormalizeTimestampNonTimestampNumber() {
        String result = YashanDBDataTypeConvertor.normalizeTimestamp("NUMBER");
        Assertions.assertEquals("NUMBER", result);
    }

    @Test
    public void testToSeaTunnelTypeWithNullColumnName() {
        // Column name can be null in some scenarios
        var result = convertor.toSeaTunnelType(null, "INT");
        Assertions.assertEquals(BasicType.INT_TYPE, result);
    }

    @Test
    public void testToSeaTunnelTypeWithEmptyColumnName() {
        var result = convertor.toSeaTunnelType("", "VARCHAR");
        Assertions.assertEquals(BasicType.STRING_TYPE, result);
    }

    @Test
    public void testToSeaTunnelTypeWithUpperCase() {
        var result = convertor.toSeaTunnelType("col1", "number");
        Assertions.assertTrue(result instanceof DecimalType);
    }

    @Test
    public void testToSeaTunnelTypeWithMixedCase() {
        var result = convertor.toSeaTunnelType("col1", "Number");
        Assertions.assertTrue(result instanceof DecimalType);
    }

    @Test
    public void testToConnectorTypeWithNullProperties() {
        String result = convertor.toConnectorType("col1", BasicType.INT_TYPE, null);
        Assertions.assertEquals("INT", result);
    }

    @Test
    public void testToConnectorTypeWithEmptyProperties() {
        String result = convertor.toConnectorType("col1", BasicType.SHORT_TYPE, Collections.emptyMap());
        Assertions.assertEquals("SMALLINT", result);
    }

    @Test
    public void testToConnectorTypeBoolean() {
        String result = convertor.toConnectorType("col1", BasicType.BOOLEAN_TYPE, Collections.emptyMap());
        Assertions.assertTrue(result.contains("NUMBER"));
        Assertions.assertTrue(result.contains("1"));
    }

    @Test
    public void testToConnectorTypeFloat() {
        String result = convertor.toConnectorType("col1", BasicType.FLOAT_TYPE, Collections.emptyMap());
        Assertions.assertEquals("BINARY_FLOAT", result);
    }

    @Test
    public void testToConnectorTypeDouble() {
        String result = convertor.toConnectorType("col1", BasicType.DOUBLE_TYPE, Collections.emptyMap());
        Assertions.assertEquals("BINARY_DOUBLE", result);
    }

    @Test
    public void testPrecisionAndScaleConstants() {
        Assertions.assertEquals(38L, YashanDBDataTypeConvertor.DEFAULT_PRECISION);
        Assertions.assertEquals(18, YashanDBDataTypeConvertor.DEFAULT_SCALE);
        Assertions.assertEquals("precision", YashanDBDataTypeConvertor.PRECISION);
        Assertions.assertEquals("scale", YashanDBDataTypeConvertor.SCALE);
    }

    @Test
    public void testToSeaTunnelTypeSmallInt() {
        var result = convertor.toSeaTunnelType("col1", "SMALLINT");
        Assertions.assertEquals(BasicType.SHORT_TYPE, result);
    }

    @Test
    public void testToSeaTunnelTypeTinyInt() {
        var result = convertor.toSeaTunnelType("col1", "TINYINT");
        Assertions.assertEquals(BasicType.SHORT_TYPE, result);
    }

    @Test
    public void testToSeaTunnelTypeBigInt() {
        var result = convertor.toSeaTunnelType("col1", "BIGINT");
        Assertions.assertEquals(BasicType.LONG_TYPE, result);
    }

    @Test
    public void testToSeaTunnelTypeReal() {
        var result = convertor.toSeaTunnelType("col1", "REAL");
        Assertions.assertEquals(BasicType.FLOAT_TYPE, result);
    }

    @Test
    public void testToSeaTunnelTypeBinaryFloat() {
        var result = convertor.toSeaTunnelType("col1", "BINARY_FLOAT");
        Assertions.assertEquals(BasicType.FLOAT_TYPE, result);
    }

    @Test
    public void testToSeaTunnelTypeBinaryDouble() {
        var result = convertor.toSeaTunnelType("col1", "BINARY_DOUBLE");
        Assertions.assertEquals(BasicType.DOUBLE_TYPE, result);
    }

    @Test
    public void testToSeaTunnelTypeBit() {
        var result = convertor.toSeaTunnelType("col1", "BIT");
        Assertions.assertEquals(BasicType.BOOLEAN_TYPE, result);
    }

    @Test
    public void testToSeaTunnelTypeChar() {
        var result = convertor.toSeaTunnelType("col1", "CHAR");
        Assertions.assertEquals(BasicType.STRING_TYPE, result);
    }

    @Test
    public void testToSeaTunnelTypeNChar() {
        var result = convertor.toSeaTunnelType("col1", "NCHAR");
        Assertions.assertEquals(BasicType.STRING_TYPE, result);
    }

    @Test
    public void testToSeaTunnelTypeNVarchar() {
        var result = convertor.toSeaTunnelType("col1", "NVARCHAR");
        Assertions.assertEquals(BasicType.STRING_TYPE, result);
    }

    @Test
    public void testToSeaTunnelTypeNClob() {
        var result = convertor.toSeaTunnelType("col1", "NCLOB");
        Assertions.assertEquals(BasicType.STRING_TYPE, result);
    }

    @Test
    public void testToSeaTunnelTypeRaw() {
        var result = convertor.toSeaTunnelType("col1", "RAW");
        Assertions.assertNotNull(result);
    }

    @Test
    public void testToSeaTunnelTypeIntervalYear() {
        var result = convertor.toSeaTunnelType("col1", "INTERVAL YEAR TO MONTH");
        Assertions.assertEquals(BasicType.STRING_TYPE, result);
    }

    @Test
    public void testToSeaTunnelTypeIntervalDay() {
        var result = convertor.toSeaTunnelType("col1", "INTERVAL DAY TO SECOND");
        Assertions.assertEquals(BasicType.STRING_TYPE, result);
    }
}