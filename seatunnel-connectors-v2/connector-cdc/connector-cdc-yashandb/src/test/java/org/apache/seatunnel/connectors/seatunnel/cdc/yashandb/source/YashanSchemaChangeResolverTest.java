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

package org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.source;

import org.apache.seatunnel.api.table.catalog.TablePath;
import org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.config.YashanSourceConfig;
import org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.config.YashanSourceConfigFactory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

/** Test for {@link YashanSchemaChangeResolver}. */
public class YashanSchemaChangeResolverTest {

    @Test
    public void testConstructor() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("user");
        factory.password("pass");
        YashanSourceConfig config = (YashanSourceConfig) factory.create(0);

        YashanSchemaChangeResolver resolver = new YashanSchemaChangeResolver(config);
        Assertions.assertNotNull(resolver);
    }

    @Test
    public void testCreateDdlParserThrowsUnsupportedOperationException() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("user");
        factory.password("pass");
        YashanSourceConfig config = (YashanSourceConfig) factory.create(0);

        YashanSchemaChangeResolver resolver = new YashanSchemaChangeResolver(config);
        TablePath tablePath = TablePath.of("SCHEMA.TABLE", true);

        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            resolver.createDdlParser(tablePath);
        });
    }

    @Test
    public void testCreateDdlParserExceptionMessage() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("user");
        factory.password("pass");
        YashanSourceConfig config = (YashanSourceConfig) factory.create(0);

        YashanSchemaChangeResolver resolver = new YashanSchemaChangeResolver(config);
        TablePath tablePath = TablePath.of("SCHEMA.TABLE", true);

        UnsupportedOperationException exception = Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> resolver.createDdlParser(tablePath));

        Assertions.assertTrue(exception.getMessage().contains("not yet supported"));
        Assertions.assertTrue(exception.getMessage().contains("YashanDB"));
    }

    @Test
    public void testGetAndClearParsedEvents() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("user");
        factory.password("pass");
        YashanSourceConfig config = (YashanSourceConfig) factory.create(0);

        YashanSchemaChangeResolver resolver = new YashanSchemaChangeResolver(config);

        var events = resolver.getAndClearParsedEvents();
        Assertions.assertNotNull(events);
        Assertions.assertEquals(0, events.size());
    }

    @Test
    public void testGetAndClearParsedEventsReturnsEmptyList() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("user");
        factory.password("pass");
        YashanSourceConfig config = (YashanSourceConfig) factory.create(0);

        YashanSchemaChangeResolver resolver = new YashanSchemaChangeResolver(config);

        // Call multiple times, should always return empty
        var events1 = resolver.getAndClearParsedEvents();
        var events2 = resolver.getAndClearParsedEvents();

        Assertions.assertEquals(0, events1.size());
        Assertions.assertEquals(0, events2.size());
    }

    @Test
    public void testGetSourceDialectName() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        factory.hostname("localhost");
        factory.port(1688);
        factory.username("user");
        factory.password("pass");
        YashanSourceConfig config = (YashanSourceConfig) factory.create(0);

        YashanSchemaChangeResolver resolver = new YashanSchemaChangeResolver(config);

        Assertions.assertEquals("YashanDB", resolver.getSourceDialectName());
    }
}