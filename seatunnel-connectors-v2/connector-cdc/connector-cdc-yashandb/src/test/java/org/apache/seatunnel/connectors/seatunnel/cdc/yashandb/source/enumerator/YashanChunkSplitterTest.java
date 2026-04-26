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

package org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.source.enumerator;

import org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.config.YashanSourceConfigFactory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Test for {@link YashanChunkSplitter}. */
public class YashanChunkSplitterTest {

    @Test
    public void testConstructor() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");

        YashanChunkSplitter splitter = new YashanChunkSplitter(factory.create(0));
        Assertions.assertNotNull(splitter);
    }

    @Test
    public void testQueryMinMaxReturnsEmptyArray() throws Exception {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");

        YashanChunkSplitter splitter = new YashanChunkSplitter(factory.create(0));

        // Since the implementation returns empty array for now, test that it doesn't throw
        io.debezium.relational.TableId tableId = new io.debezium.relational.TableId("db", "schema", "table");
        Object[] result = splitter.queryMinMax(null, tableId, "column");

        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.length);
    }

    @Test
    public void testQueryMinReturnsNull() throws Exception {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");

        YashanChunkSplitter splitter = new YashanChunkSplitter(factory.create(0));

        io.debezium.relational.TableId tableId = new io.debezium.relational.TableId("db", "schema", "table");
        Object result = splitter.queryMin(null, tableId, "column", null);

        Assertions.assertNull(result);
    }

    @Test
    public void testSampleDataFromColumnReturnsEmptyArray() throws Exception {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");

        YashanChunkSplitter splitter = new YashanChunkSplitter(factory.create(0));

        io.debezium.relational.TableId tableId = new io.debezium.relational.TableId("db", "schema", "table");
        Object[] result = splitter.sampleDataFromColumn(null, tableId, "column", 10);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.length);
    }

    @Test
    public void testQueryNextChunkMaxReturnsNull() throws Exception {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");

        YashanChunkSplitter splitter = new YashanChunkSplitter(factory.create(0));

        io.debezium.relational.TableId tableId = new io.debezium.relational.TableId("db", "schema", "table");
        Object result = splitter.queryNextChunkMax(null, tableId, "column", 100, null);

        Assertions.assertNull(result);
    }

    @Test
    public void testQueryApproximateRowCntReturnsZero() throws Exception {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");

        YashanChunkSplitter splitter = new YashanChunkSplitter(factory.create(0));

        io.debezium.relational.TableId tableId = new io.debezium.relational.TableId("db", "schema", "table");
        Long result = splitter.queryApproximateRowCnt(null, tableId);

        Assertions.assertEquals(0L, result);
    }

    @Test
    public void testBuildSplitScanQueryReturnsEmptyString() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");

        YashanChunkSplitter splitter = new YashanChunkSplitter(factory.create(0));

        io.debezium.relational.Table table = io.debezium.relational.Table.builder()
                .id(new io.debezium.relational.TableId("db", "schema", "table"))
                .build();

        org.apache.seatunnel.api.table.type.SeaTunnelRowType rowType =
                new org.apache.seatunnel.api.table.type.SeaTunnelRowType(new String[]{"col"},
                        new org.apache.seatunnel.api.table.type.SeaTunnelDataType[]{});

        String result = splitter.buildSplitScanQuery(table, rowType, true, false);

        Assertions.assertEquals("", result);
    }

    @Test
    public void testFromDbzColumnReturnsNull() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");

        YashanChunkSplitter splitter = new YashanChunkSplitter(factory.create(0));

        io.debezium.relational.Column column = io.debezium.relational.Column.builder()
                .name("col")
                .type("VARCHAR")
                .build();

        org.apache.seatunnel.api.table.type.SeaTunnelDataType<?> result = splitter.fromDbzColumn(column);

        Assertions.assertNull(result);
    }
}