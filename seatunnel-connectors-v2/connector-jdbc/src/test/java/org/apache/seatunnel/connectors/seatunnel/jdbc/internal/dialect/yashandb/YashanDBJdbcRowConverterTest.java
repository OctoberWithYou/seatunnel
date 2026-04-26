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

import org.apache.seatunnel.api.table.type.BasicType;
import org.apache.seatunnel.api.table.type.PrimitiveByteArrayType;
import org.apache.seatunnel.api.table.type.SeaTunnelDataType;
import org.apache.seatunnel.api.table.type.SqlType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/** Test for {@link YashanDBJdbcRowConverter}. */
public class YashanDBJdbcRowConverterTest {

    private final YashanDBJdbcRowConverter converter = new YashanDBJdbcRowConverter();

    @Test
    public void testConverterName() {
        Assertions.assertEquals("YashanDB", converter.converterName());
    }

    @Test
    public void testSetValueToStatementByDataTypeForBlob() throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);
        byte[] testData = new byte[] {1, 2, 3, 4, 5};

        converter.setValueToStatementByDataType(
                testData, statement, PrimitiveByteArrayType.INSTANCE, 1, "BLOB");

        // For BLOB type, should use setBinaryStream
        verify(statement).setBinaryStream(anyInt(), any(ByteArrayInputStream.class), anyInt());
        verifyNoMoreInteractions(statement);
    }

    @Test
    public void testSetValueToStatementByDataTypeForRaw() throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);
        byte[] testData = new byte[] {1, 2, 3, 4, 5};

        converter.setValueToStatementByDataType(
                testData, statement, PrimitiveByteArrayType.INSTANCE, 1, "RAW");

        // For RAW type, should use setBytes
        verify(statement).setBytes(anyInt(), any(byte[].class));
        verifyNoMoreInteractions(statement);
    }

    @Test
    public void testSetValueToStatementByDataTypeForClob() throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);
        String testString = "test clob content";

        converter.setValueToStatementByDataType(
                testString, statement, BasicType.STRING_TYPE, 1, "CLOB");

        // For CLOB type, should use setCharacterStream
        verify(statement).setCharacterStream(anyInt(), any(StringReader.class), anyInt());
        verifyNoMoreInteractions(statement);
    }

    @Test
    public void testSetValueToStatementByDataTypeForNclob() throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);
        String testString = "test nclob content";

        converter.setValueToStatementByDataType(
                testString, statement, BasicType.STRING_TYPE, 1, "NCLOB");

        // For NCLOB type, should use setNCharacterStream
        verify(statement).setNCharacterStream(anyInt(), any(StringReader.class), anyInt());
        verifyNoMoreInteractions(statement);
    }

    @Test
    public void testSetValueToStatementByDataTypeForVarchar() throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);
        String testString = "test varchar content";

        converter.setValueToStatementByDataType(
                testString, statement, BasicType.STRING_TYPE, 1, "VARCHAR");

        // For VARCHAR type, should use setString
        verify(statement).setString(anyInt(), anyString());
        verifyNoMoreInteractions(statement);
    }

    @Test
    public void testSetValueToStatementByDataTypeForChar() throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);
        String testString = "test char content";

        converter.setValueToStatementByDataType(
                testString, statement, BasicType.STRING_TYPE, 1, "CHAR");

        // For CHAR type, should use setString
        verify(statement).setString(anyInt(), anyString());
        verifyNoMoreInteractions(statement);
    }

    @Test
    public void testSetValueToStatementByDataTypeForNullSourceType() throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);
        String testString = "test content";

        converter.setValueToStatementByDataType(
                testString, statement, BasicType.STRING_TYPE, 1, null);

        // When sourceType is null, should use setString for STRING type
        verify(statement).setString(anyInt(), anyString());
        verifyNoMoreInteractions(statement);
    }

    @Test
    public void testSetValueToStatementByDataTypeForBytesWithNullSourceType() throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);
        byte[] testData = new byte[] {1, 2, 3, 4, 5};

        converter.setValueToStatementByDataType(
                testData, statement, PrimitiveByteArrayType.INSTANCE, 1, null);

        // When sourceType is null for BYTES type, should use setBytes
        verify(statement).setBytes(anyInt(), any(byte[].class));
        verifyNoMoreInteractions(statement);
    }

    @Test
    public void testSetValueToStatementByDataTypeForOtherTypes() throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);

        // Test INT type - should delegate to parent
        converter.setValueToStatementByDataType(123, statement, BasicType.INT_TYPE, 1, null);

        verify(statement).setInt(anyInt(), anyInt());
    }

    @Test
    public void testSetValueToStatementByDataTypeForLong() throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);

        converter.setValueToStatementByDataType(123456789L, statement, BasicType.LONG_TYPE, 1, null);

        verify(statement).setLong(anyInt(), anyInt());
    }

    @Test
    public void testSetValueToStatementByDataTypeForBoolean() throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);

        converter.setValueToStatementByDataType(true, statement, BasicType.BOOLEAN_TYPE, 1, null);

        verify(statement).setBoolean(anyInt(), anyInt());
    }

    @Test
    public void testSetValueToStatementByDataTypeForFloat() throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);

        converter.setValueToStatementByDataType(1.5f, statement, BasicType.FLOAT_TYPE, 1, null);

        verify(statement).setFloat(anyInt(), anyInt());
    }

    @Test
    public void testSetValueToStatementByDataTypeForDouble() throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);

        converter.setValueToStatementByDataType(2.5d, statement, BasicType.DOUBLE_TYPE, 1, null);

        verify(statement).setDouble(anyInt(), anyInt());
    }

    @Test
    public void testSetValueToStatementByDataTypeForShort() throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);

        converter.setValueToStatementByDataType((short) 10, statement, BasicType.SHORT_TYPE, 1, null);

        verify(statement).setShort(anyInt(), anyInt());
    }

    @Test
    public void testSetValueToStatementByDataTypeForByte() throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);

        converter.setValueToStatementByDataType((byte) 5, statement, BasicType.BYTE_TYPE, 1, null);

        verify(statement).setByte(anyInt(), anyInt());
    }

    @Test
    public void testBlobHandlingWithEmptyByteArray() throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);
        byte[] emptyData = new byte[0];

        converter.setValueToStatementByDataType(
                emptyData, statement, PrimitiveByteArrayType.INSTANCE, 1, "BLOB");

        verify(statement).setBinaryStream(anyInt(), any(ByteArrayInputStream.class), anyInt());
    }

    @Test
    public void testClobHandlingWithEmptyString() throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);
        String emptyString = "";

        converter.setValueToStatementByDataType(
                emptyString, statement, BasicType.STRING_TYPE, 1, "CLOB");

        verify(statement).setCharacterStream(anyInt(), any(StringReader.class), anyInt());
    }

    @Test
    public void testBlobHandlingWithLargeByteArray() throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);
        byte[] largeData = new byte[10000];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 256);
        }

        converter.setValueToStatementByDataType(
                largeData, statement, PrimitiveByteArrayType.INSTANCE, 1, "BLOB");

        verify(statement).setBinaryStream(anyInt(), any(ByteArrayInputStream.class), anyInt());
    }

    @Test
    public void testClobHandlingWithLargeString() throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("a");
        }
        String largeString = sb.toString();

        converter.setValueToStatementByDataType(
                largeString, statement, BasicType.STRING_TYPE, 1, "CLOB");

        verify(statement).setCharacterStream(anyInt(), any(StringReader.class), anyInt());
    }

    @Test
    public void testDifferentStatementIndex() throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);
        String testString = "test";

        converter.setValueToStatementByDataType(testString, statement, BasicType.STRING_TYPE, 5, "VARCHAR");

        verify(statement).setString(5, testString);
    }

    @Test
    public void testMultipleSetOperations() throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);

        converter.setValueToStatementByDataType("str1", statement, BasicType.STRING_TYPE, 1, "VARCHAR");
        converter.setValueToStatementByDataType("str2", statement, BasicType.STRING_TYPE, 2, "CLOB");
        converter.setValueToStatementByDataType(new byte[] {1, 2}, statement, PrimitiveByteArrayType.INSTANCE, 3, "BLOB");

        verify(statement).setString(1, "str1");
        verify(statement).setCharacterStream(2, any(StringReader.class), anyInt());
        verify(statement).setBinaryStream(3, any(ByteArrayInputStream.class), anyInt());
    }
}