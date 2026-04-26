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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Test for {@link YashanDBTypeMapper}. */
public class YashanDBTypeMapperTest {

    private final YashanDBTypeMapper mapper = new YashanDBTypeMapper();

    @Test
    public void testDefaultConstructor() {
        YashanDBTypeMapper defaultMapper = new YashanDBTypeMapper();
        Assertions.assertNotNull(defaultMapper);
    }

    @Test
    public void testConstructorWithDecimalNarrowing() {
        YashanDBTypeMapper narrowingMapper = new YashanDBTypeMapper(true);
        Assertions.assertNotNull(narrowingMapper);
    }

    @Test
    public void testConstructorWithoutDecimalNarrowing() {
        YashanDBTypeMapper noNarrowingMapper = new YashanDBTypeMapper(false);
        Assertions.assertNotNull(noNarrowingMapper);
    }

    @Test
    public void testConstructorWithFullConfig() {
        YashanDBTypeMapper fullConfigMapper = new YashanDBTypeMapper(true, true);
        Assertions.assertNotNull(fullConfigMapper);
    }

    @Test
    public void testConstructorWithBlobHandling() {
        YashanDBTypeMapper blobAsStringMapper = new YashanDBTypeMapper(true, true);
        YashanDBTypeMapper blobAsBytesMapper = new YashanDBTypeMapper(true, false);
        Assertions.assertNotNull(blobAsStringMapper);
        Assertions.assertNotNull(blobAsBytesMapper);
    }

    @Test
    public void testMappingColumnWithBasicTypeDefine() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("INT")
                        .columnType("INT")
                        .nullable(true)
                        .build();

        Column column = mapper.mappingColumn(typeDefine);

        Assertions.assertNotNull(column);
        Assertions.assertEquals("col1", column.getName());
        Assertions.assertEquals(BasicType.INT_TYPE, column.getDataType());
    }

    @Test
    public void testMappingColumnWithStringType() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("VARCHAR")
                        .columnType("VARCHAR(100)")
                        .length(100L)
                        .nullable(true)
                        .build();

        Column column = mapper.mappingColumn(typeDefine);

        Assertions.assertNotNull(column);
        Assertions.assertEquals("col1", column.getName());
        Assertions.assertEquals(BasicType.STRING_TYPE, column.getDataType());
    }

    @Test
    public void testMappingColumnWithNumberType() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("NUMBER")
                        .columnType("NUMBER(10,2)")
                        .precision(10L)
                        .scale(2)
                        .nullable(true)
                        .build();

        Column column = mapper.mappingColumn(typeDefine);

        Assertions.assertNotNull(column);
        Assertions.assertEquals("col1", column.getName());
    }

    @Test
    public void testMappingColumnWithTimestampType() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("TIMESTAMP")
                        .columnType("TIMESTAMP(6)")
                        .scale(6)
                        .nullable(true)
                        .build();

        Column column = mapper.mappingColumn(typeDefine);

        Assertions.assertNotNull(column);
        Assertions.assertEquals("col1", column.getName());
    }

    @Test
    public void testMappingColumnWithDateType() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("DATE")
                        .columnType("DATE")
                        .nullable(true)
                        .build();

        Column column = mapper.mappingColumn(typeDefine);

        Assertions.assertNotNull(column);
        Assertions.assertEquals("col1", column.getName());
    }

    @Test
    public void testMappingColumnWithBlobType() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("BLOB")
                        .columnType("BLOB")
                        .nullable(true)
                        .build();

        Column column = mapper.mappingColumn(typeDefine);

        Assertions.assertNotNull(column);
        Assertions.assertEquals("col1", column.getName());
    }

    @Test
    public void testMappingColumnWithClobType() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("CLOB")
                        .columnType("CLOB")
                        .nullable(true)
                        .build();

        Column column = mapper.mappingColumn(typeDefine);

        Assertions.assertNotNull(column);
        Assertions.assertEquals("col1", column.getName());
        Assertions.assertEquals(BasicType.STRING_TYPE, column.getDataType());
    }

    @Test
    public void testMappingColumnWithBooleanType() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("BOOLEAN")
                        .columnType("BOOLEAN")
                        .nullable(true)
                        .build();

        Column column = mapper.mappingColumn(typeDefine);

        Assertions.assertNotNull(column);
        Assertions.assertEquals("col1", column.getName());
        Assertions.assertEquals(BasicType.BOOLEAN_TYPE, column.getDataType());
    }

    @Test
    public void testMappingColumnWithNotNullConstraint() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("INT")
                        .columnType("INT")
                        .nullable(false)
                        .build();

        Column column = mapper.mappingColumn(typeDefine);

        Assertions.assertNotNull(column);
        Assertions.assertFalse(column.isNullable());
    }

    @Test
    public void testMappingColumnWithNullConstraint() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("INT")
                        .columnType("INT")
                        .nullable(true)
                        .build();

        Column column = mapper.mappingColumn(typeDefine);

        Assertions.assertNotNull(column);
        Assertions.assertTrue(column.isNullable());
    }

    @Test
    public void testMappingColumnWithComment() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("INT")
                        .columnType("INT")
                        .comment("This is a test column")
                        .nullable(true)
                        .build();

        Column column = mapper.mappingColumn(typeDefine);

        Assertions.assertNotNull(column);
        Assertions.assertEquals("This is a test column", column.getComment());
    }

    @Test
    public void testMappingColumnWithDefaultValue() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("INT")
                        .columnType("INT")
                        .defaultValue(100)
                        .nullable(true)
                        .build();

        Column column = mapper.mappingColumn(typeDefine);

        Assertions.assertNotNull(column);
        Assertions.assertEquals(100, column.getDefaultValue());
    }

    @Test
    public void testMapperWithDifferentConfigurations() {
        // Test that different configurations produce valid mappers
        YashanDBTypeMapper mapper1 = new YashanDBTypeMapper(true, false);
        YashanDBTypeMapper mapper2 = new YashanDBTypeMapper(false, true);
        YashanDBTypeMapper mapper3 = new YashanDBTypeMapper(false, false);

        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("INT")
                        .columnType("INT")
                        .build();

        Column column1 = mapper1.mappingColumn(typeDefine);
        Column column2 = mapper2.mappingColumn(typeDefine);
        Column column3 = mapper3.mappingColumn(typeDefine);

        Assertions.assertNotNull(column1);
        Assertions.assertNotNull(column2);
        Assertions.assertNotNull(column3);
    }

    @Test
    public void testMappingColumnWithFloatIndicator() {
        // Test NUMBER with scale=-127 (indicates FLOAT)
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("float")
                        .columnType("float")
                        .nullable(true)
                        .build();

        Column column = mapper.mappingColumn(typeDefine);

        Assertions.assertNotNull(column);
        Assertions.assertEquals("col1", column.getName());
    }

    @Test
    public void testMappingColumnWithNCharType() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("NCHAR")
                        .columnType("NCHAR(10)")
                        .length(10L)
                        .nullable(true)
                        .build();

        Column column = mapper.mappingColumn(typeDefine);

        Assertions.assertNotNull(column);
        Assertions.assertEquals("col1", column.getName());
        Assertions.assertEquals(BasicType.STRING_TYPE, column.getDataType());
    }

    @Test
    public void testMappingColumnWithNVarcharType() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("NVARCHAR")
                        .columnType("NVARCHAR(100)")
                        .length(100L)
                        .nullable(true)
                        .build();

        Column column = mapper.mappingColumn(typeDefine);

        Assertions.assertNotNull(column);
        Assertions.assertEquals("col1", column.getName());
        Assertions.assertEquals(BasicType.STRING_TYPE, column.getDataType());
    }

    @Test
    public void testMappingColumnWithRawType() {
        BasicTypeDefine typeDefine =
                BasicTypeDefine.builder()
                        .name("col1")
                        .dataType("RAW")
                        .columnType("RAW(256)")
                        .length(256L)
                        .nullable(true)
                        .build();

        Column column = mapper.mappingColumn(typeDefine);

        Assertions.assertNotNull(column);
        Assertions.assertEquals("col1", column.getName());
    }
}