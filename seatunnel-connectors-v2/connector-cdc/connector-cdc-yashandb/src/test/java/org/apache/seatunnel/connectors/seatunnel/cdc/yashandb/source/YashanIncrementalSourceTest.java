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

import org.apache.seatunnel.api.configuration.ReadonlyConfig;
import org.apache.seatunnel.api.table.catalog.CatalogTable;
import org.apache.seatunnel.api.table.schema.SchemaChangeType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Test for {@link YashanIncrementalSource}. */
public class YashanIncrementalSourceTest {

    @Test
    public void testGetPluginName() {
        Assertions.assertEquals("YashanDB-CDC", YashanIncrementalSource.IDENTIFIER);
    }

    @Test
    public void testDriverName() {
        YashanIncrementalSource<Object> source = createSource();
        Optional<String> driverName = source.driverName();

        Assertions.assertTrue(driverName.isPresent());
        Assertions.assertEquals("com.yashandb.jdbc.Driver", driverName.get());
    }

    @Test
    public void testSupportsSchemaChangeTypes() {
        YashanIncrementalSource<Object> source = createSource();
        List<SchemaChangeType> supportedTypes = source.supports();

        Assertions.assertNotNull(supportedTypes);
        Assertions.assertTrue(supportedTypes.contains(SchemaChangeType.ADD_COLUMN));
        Assertions.assertTrue(supportedTypes.contains(SchemaChangeType.DROP_COLUMN));
        Assertions.assertTrue(supportedTypes.contains(SchemaChangeType.RENAME_COLUMN));
        Assertions.assertTrue(supportedTypes.contains(SchemaChangeType.UPDATE_COLUMN));
        Assertions.assertEquals(4, supportedTypes.size());
    }

    @Test
    public void testGetStartupModeOption() {
        YashanIncrementalSource<Object> source = createSource();
        Assertions.assertNotNull(source.getStartupModeOption());
        Assertions.assertEquals(YashanIncrementalSourceOptions.STARTUP_MODE, source.getStartupModeOption());
    }

    @Test
    public void testGetStopModeOption() {
        YashanIncrementalSource<Object> source = createSource();
        Assertions.assertNotNull(source.getStopModeOption());
        Assertions.assertEquals(YashanIncrementalSourceOptions.STOP_MODE, source.getStopModeOption());
    }

    private YashanIncrementalSource<Object> createSource() {
        ReadonlyConfig config = ReadonlyConfig.fromMap(Collections.emptyMap());
        List<CatalogTable> catalogTables = Collections.emptyList();
        return new YashanIncrementalSource<>(config, catalogTables);
    }
}