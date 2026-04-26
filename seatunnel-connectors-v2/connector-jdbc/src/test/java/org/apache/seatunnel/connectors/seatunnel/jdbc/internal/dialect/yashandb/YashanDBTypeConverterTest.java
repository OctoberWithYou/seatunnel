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
import org.apache.seatunnel.api.table.type.BasicType;
import org.apache.seatunnel.api.table.type.DecimalType;
import org.apache.seatunnel.api.table.type.LocalTimeType;
import org.apache.seatunnel.api.table.type.PrimitiveByteArrayType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Test for {@link YashanDBTypeConverter}. */
public class YashanDBTypeConverterTest {

    private final YashanDBTypeConverter converter = new YashanDBTypeConverter(true, false);

    @Test
    public void testIdentifier() {
        Assertions.assertEquals("YashanDB", converter.identifier());
    }

    @Test
    public void testConvertTinyInt() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder().name("col1").dataType("TINYINT").columnType("TINYINT").build();
        Column column = converter.convert(typeDefine);
        Assertions.assertEquals(BasicType.SHORT_TYPE, column.getDataType());
    }

    @Test
    public void testConvertSmallInt() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("SMALLINT")
                        .columnType("SMALLINT")
                        .build();
        Column column = converter.convert(typeDefine);
        Assertions.assertEquals(BasicType.SHORT_TYPE, column.getDataType());
    }

    @Test
    public void testConvertInt() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder().name("col1").dataType("INT").columnType("INT").build();
        Column column = converter.convert(typeDefine);
        Assertions.assertEquals(BasicType.INT_TYPE, column.getDataType());
    }

    @Test
    public void testConvertInteger() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("INTEGER")
                        .columnType("INTEGER")
                        .build();
        Column column = converter.convert(typeDefine);
        Assertions.assertEquals(BasicType.INT_TYPE, column.getDataType());
    }

    @Test
    public void testConvertBigInt() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder().name("col1").dataType("BIGINT").columnType("BIGINT").build();
        Column column = converter.convert(typeDefine);
        Assertions.assertEquals(BasicType.LONG_TYPE, column.getDataType());
    }

    @Test
    public void testConvertNumberToDecimal() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("NUMBER")
                        .columnType("NUMBER(10,2)")
                        .precision(10L)
                        .scale(2)
                        .build();
        Column column = converter.convert(typeDefine);
        Assertions.assertTrue(column.getDataType() instanceof DecimalType);
        DecimalType dt = (DecimalType) column.getDataType();
        Assertions.assertEquals(10, dt.getPrecision());
        Assertions.assertEquals(2, dt.getScale());
    }

    @Test
    public void testConvertNumberWithScaleZero() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("NUMBER")
                        .columnType("NUMBER(5,0)")
                        .precision(5L)
                        .scale(0)
                        .build();
        Column column = converter.convert(typeDefine);
        Assertions.assertEquals(BasicType.INT_TYPE, column.getDataType());
    }

    @Test
    public void testConvertNumberWithNegativeScale() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("NUMBER")
                        .columnType("NUMBER(5,-2)")
                        .precision(5L)
                        .scale(-2)
                        .build();
        Column column = converter.convert(typeDefine);
        Assertions.assertEquals(BasicType.INT_TYPE, column.getDataType());
    }

    @Test
    public void testConvertBinaryFloat() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("BINARY_FLOAT")
                        .columnType("BINARY_FLOAT")
                        .build();
        Column column = converter.convert(typeDefine);
        Assertions.assertEquals(BasicType.FLOAT_TYPE, column.getDataType());
    }

    @Test
    public void testConvertReal() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder().name("col1").dataType("REAL").columnType("REAL").build();
        Column column = converter.convert(typeDefine);
        Assertions.assertEquals(BasicType.FLOAT_TYPE, column.getDataType());
    }

    @Test
    public void testConvertBinaryDouble() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("BINARY_DOUBLE")
                        .columnType("BINARY_DOUBLE")
                        .build();
        Column column = converter.convert(typeDefine);
        Assertions.assertEquals(BasicType.DOUBLE_TYPE, column.getDataType());
    }

    @Test
    public void testConvertBit() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder().name("col1").dataType("BIT").columnType("BIT").build();
        Column column = converter.convert(typeDefine);
        Assertions.assertEquals(BasicType.BOOLEAN_TYPE, column.getDataType());
    }

    @Test
    public void testConvertChar() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("CHAR")
                        .columnType("CHAR(10)")
                        .length(10L)
                        .build();
        Column column = converter.convert(typeDefine);
        Assertions.assertEquals(BasicType.STRING_TYPE, column.getDataType());
    }

    @Test
    public void testConvertVarchar2() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("VARCHAR2")
                        .columnType("VARCHAR2(255)")
                        .length(255L)
                        .build();
        Column column = converter.convert(typeDefine);
        Assertions.assertEquals(BasicType.STRING_TYPE, column.getDataType());
    }

    @Test
    public void testConvertNChar() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("NCHAR")
                        .columnType("NCHAR(10)")
                        .length(10L)
                        .build();
        Column column = converter.convert(typeDefine);
        Assertions.assertEquals(BasicType.STRING_TYPE, column.getDataType());
    }

    @Test
    public void testConvertNVarchar() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("NVARCHAR")
                        .columnType("NVARCHAR(100)")
                        .length(100L)
                        .build();
        Column column = converter.convert(typeDefine);
        Assertions.assertEquals(BasicType.STRING_TYPE, column.getDataType());
    }

    @Test
    public void testConvertClob() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder().name("col1").dataType("CLOB").columnType("CLOB").build();
        Column column = converter.convert(typeDefine);
        Assertions.assertEquals(BasicType.STRING_TYPE, column.getDataType());
    }

    @Test
    public void testConvertNClob() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder().name("col1").dataType("NCLOB").columnType("NCLOB").build();
        Column column = converter.convert(typeDefine);
        Assertions.assertEquals(BasicType.STRING_TYPE, column.getDataType());
    }

    @Test
    public void testConvertBlob() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder().name("col1").dataType("BLOB").columnType("BLOB").build();
        Column column = converter.convert(typeDefine);
        Assertions.assertEquals(PrimitiveByteArrayType.INSTANCE, column.getDataType());
    }

    @Test
    public void testConvertBlobAsString() {
        YashanDBTypeConverter blobAsStringConverter = new YashanDBTypeConverter(true, true);
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder().name("col1").dataType("BLOB").columnType("BLOB").build();
        Column column = blobAsStringConverter.convert(typeDefine);
        Assertions.assertEquals(BasicType.STRING_TYPE, column.getDataType());
    }

    @Test
    public void testConvertRaw() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("RAW")
                        .columnType("RAW(256)")
                        .length(256L)
                        .build();
        Column column = converter.convert(typeDefine);
        Assertions.assertEquals(PrimitiveByteArrayType.INSTANCE, column.getDataType());
    }

    @Test
    public void testConvertDate() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder().name("col1").dataType("DATE").columnType("DATE").build();
        Column column = converter.convert(typeDefine);
        Assertions.assertEquals(LocalTimeType.LOCAL_DATE_TIME_TYPE, column.getDataType());
    }

    @Test
    public void testConvertTime() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder().name("col1").dataType("TIME").columnType("TIME").build();
        Column column = converter.convert(typeDefine);
        Assertions.assertEquals(LocalTimeType.LOCAL_TIME_TYPE, column.getDataType());
    }

    @Test
    public void testConvertTimestamp() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("TIMESTAMP")
                        .columnType("TIMESTAMP(6)")
                        .scale(6)
                        .build();
        Column column = converter.convert(typeDefine);
        Assertions.assertEquals(LocalTimeType.LOCAL_DATE_TIME_TYPE, column.getDataType());
    }

    @Test
    public void testConvertBoolean() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("BOOLEAN")
                        .columnType("BOOLEAN")
                        .build();
        Column column = converter.convert(typeDefine);
        Assertions.assertEquals(BasicType.BOOLEAN_TYPE, column.getDataType());
    }

    @Test
    public void testConvertIntervalYear() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("INTERVAL YEAR TO MONTH")
                        .columnType("INTERVAL YEAR(2) TO MONTH")
                        .build();
        Column column = converter.convert(typeDefine);
        Assertions.assertEquals(BasicType.STRING_TYPE, column.getDataType());
    }

    @Test
    public void testConvertIntervalDay() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("INTERVAL DAY TO SECOND")
                        .columnType("INTERVAL DAY(2) TO SECOND(6)")
                        .build();
        Column column = converter.convert(typeDefine);
        Assertions.assertEquals(BasicType.STRING_TYPE, column.getDataType());
    }

    @Test
    public void testReconvertInt() {
        Column column =
                org.apache.seatunnel.api.table.catalog.PhysicalColumn.builder()
                        .name("col1")
                        .dataType(BasicType.INT_TYPE)
                        .nullable(true)
                        .build();
        BasicTypeDefine typeDefine = converter.reconvert(column);
        Assertions.assertEquals("INT", typeDefine.getColumnType());
    }

    @Test
    public void testReconvertBigInt() {
        Column column =
                org.apache.seatunnel.api.table.catalog.PhysicalColumn.builder()
                        .name("col1")
                        .dataType(BasicType.LONG_TYPE)
                        .nullable(true)
                        .build();
        BasicTypeDefine typeDefine = converter.reconvert(column);
        Assertions.assertEquals("BIGINT", typeDefine.getColumnType());
    }

    @Test
    public void testReconvertString() {
        Column column =
                org.apache.seatunnel.api.table.catalog.PhysicalColumn.builder()
                        .name("col1")
                        .dataType(BasicType.STRING_TYPE)
                        .columnLength(255L)
                        .nullable(true)
                        .build();
        BasicTypeDefine typeDefine = converter.reconvert(column);
        Assertions.assertEquals("VARCHAR2(255)", typeDefine.getColumnType());
    }
}
