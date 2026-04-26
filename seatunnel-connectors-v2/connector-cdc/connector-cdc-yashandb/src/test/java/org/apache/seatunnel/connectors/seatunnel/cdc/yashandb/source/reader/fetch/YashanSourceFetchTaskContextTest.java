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

package org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.source.reader.fetch;

import org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.config.YashanSourceConfigFactory;
import org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.source.YashanDialect;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

/** Test for {@link YashanSourceFetchTaskContext}. */
public class YashanSourceFetchTaskContextTest {

    @Test
    public void testConstructor() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");

        YashanDialect dialect = new YashanDialect(factory, Collections.emptyList());
        YashanSourceFetchTaskContext context = new YashanSourceFetchTaskContext(factory.create(0), dialect);

        Assertions.assertNotNull(context);
    }

    @Test
    public void testGetDatabaseSchemaReturnsNull() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");

        YashanDialect dialect = new YashanDialect(factory, Collections.emptyList());
        YashanSourceFetchTaskContext context = new YashanSourceFetchTaskContext(factory.create(0), dialect);

        Assertions.assertNull(context.getDatabaseSchema());
    }

    @Test
    public void testGetSplitTypeReturnsNull() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");

        YashanDialect dialect = new YashanDialect(factory, Collections.emptyList());
        YashanSourceFetchTaskContext context = new YashanSourceFetchTaskContext(factory.create(0), dialect);

        io.debezium.relational.Table table = io.debezium.relational.Table.builder()
                .id(new io.debezium.relational.TableId("db", "schema", "table"))
                .build();

        Assertions.assertNull(context.getSplitType(table));
    }

    @Test
    public void testGetErrorHandlerReturnsNull() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");

        YashanDialect dialect = new YashanDialect(factory, Collections.emptyList());
        YashanSourceFetchTaskContext context = new YashanSourceFetchTaskContext(factory.create(0), dialect);

        Assertions.assertNull(context.getErrorHandler());
    }

    @Test
    public void testGetDispatcherReturnsNull() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");

        YashanDialect dialect = new YashanDialect(factory, Collections.emptyList());
        YashanSourceFetchTaskContext context = new YashanSourceFetchTaskContext(factory.create(0), dialect);

        Assertions.assertNull(context.getDispatcher());
    }

    @Test
    public void testGetOffsetContextReturnsNull() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");

        YashanDialect dialect = new YashanDialect(factory, Collections.emptyList());
        YashanSourceFetchTaskContext context = new YashanSourceFetchTaskContext(factory.create(0), dialect);

        Assertions.assertNull(context.getOffsetContext());
    }

    @Test
    public void testGetPartitionReturnsNull() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");

        YashanDialect dialect = new YashanDialect(factory, Collections.emptyList());
        YashanSourceFetchTaskContext context = new YashanSourceFetchTaskContext(factory.create(0), dialect);

        Assertions.assertNull(context.getPartition());
    }

    @Test
    public void testConfigureDoesNotThrow() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");

        YashanDialect dialect = new YashanDialect(factory, Collections.emptyList());
        YashanSourceFetchTaskContext context = new YashanSourceFetchTaskContext(factory.create(0), dialect);

        org.apache.seatunnel.connectors.cdc.base.source.split.SourceSplitBase split =
                new org.apache.seatunnel.connectors.cdc.base.source.split.SnapshotSplit(
                        "split-0",
                        "db",
                        "schema",
                        "table",
                        null,
                        null,
                        null,
                        Collections.emptyList());

        context.configure(split);
    }

    @Test
    public void testGetQueueReturnsNull() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");

        YashanDialect dialect = new YashanDialect(factory, Collections.emptyList());
        YashanSourceFetchTaskContext context = new YashanSourceFetchTaskContext(factory.create(0), dialect);

        Assertions.assertNull(context.getQueue());
    }

    @Test
    public void testGetTableFilterReturnsNull() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");

        YashanDialect dialect = new YashanDialect(factory, Collections.emptyList());
        YashanSourceFetchTaskContext context = new YashanSourceFetchTaskContext(factory.create(0), dialect);

        Assertions.assertNull(context.getTableFilter());
    }

    @Test
    public void testGetStreamOffsetReturnsNull() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");

        YashanDialect dialect = new YashanDialect(factory, Collections.emptyList());
        YashanSourceFetchTaskContext context = new YashanSourceFetchTaskContext(factory.create(0), dialect);

        org.apache.kafka.connect.source.SourceRecord record = new org.apache.kafka.connect.source.SourceRecord(
                Collections.emptyMap(),
                Collections.emptyMap(),
                "topic",
                null,
                null,
                null);

        Assertions.assertNull(context.getStreamOffset(record));
    }

    @Test
    public void testCloseDoesNotThrow() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("SYS");
        factory.password("yasdb_123");

        YashanDialect dialect = new YashanDialect(factory, Collections.emptyList());
        YashanSourceFetchTaskContext context = new YashanSourceFetchTaskContext(factory.create(0), dialect);

        context.close();
    }
}