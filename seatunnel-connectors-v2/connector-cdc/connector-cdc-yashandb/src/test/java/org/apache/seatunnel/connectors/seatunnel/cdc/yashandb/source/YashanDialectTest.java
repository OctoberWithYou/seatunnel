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

import org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.config.YashanSourceConfigFactory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

/** Test for {@link YashanDialect}. */
public class YashanDialectTest {

    @Test
    public void testGetName() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        YashanDialect dialect = new YashanDialect(factory, Collections.emptyList());

        Assertions.assertEquals("YashanDB-CDC", dialect.getName());
    }

    @Test
    public void testIsDataCollectionIdCaseSensitive() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        YashanDialect dialect = new YashanDialect(factory, Collections.emptyList());

        // YashanDB follows Oracle semantics: table names are case-insensitive by default
        Assertions.assertFalse(dialect.isDataCollectionIdCaseSensitive(null));
    }

    @Test
    public void testConstructorWithEmptyCatalogTables() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        YashanDialect dialect = new YashanDialect(factory, Collections.emptyList());

        Assertions.assertNotNull(dialect);
    }

    @Test
    public void testConstructorWithNullCatalogTables() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        YashanDialect dialect = new YashanDialect(factory, null);

        Assertions.assertNotNull(dialect);
    }

    @Test
    public void testGetNameReturnsIdentifier() {
        YashanSourceConfigFactory factory = new YashanSourceConfigFactory();
        YashanDialect dialect = new YashanDialect(factory, Collections.emptyList());

        Assertions.assertEquals(YashanIncrementalSourceOptions.IDENTIFIER, dialect.getName());
    }
}