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

package org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Test for {@link YashanSchema}. */
public class YashanSchemaTest {

    @Test
    public void testQueryTableSchemaThrowsUnsupportedOperationException() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            YashanSchema.queryTableSchema(null, null);
        });
    }

    @Test
    public void testQueryTableSchemaWithTableIdThrowsUnsupportedOperationException() {
        io.debezium.relational.TableId tableId = new io.debezium.relational.TableId("db", "schema", "table");

        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            YashanSchema.queryTableSchema(null, tableId);
        });
    }

    @Test
    public void testQueryTableSchemaExceptionMessage() {
        UnsupportedOperationException exception = Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> YashanSchema.queryTableSchema(null, null));

        Assertions.assertTrue(exception.getMessage().contains("not yet implemented"));
    }

    @Test
    public void testPrivateConstructor() {
        // Verify that YashanSchema cannot be instantiated directly
        // This is a utility class with private constructor
        Assertions.assertThrows(Exception.class, () -> {
            java.lang.reflect.Constructor<YashanSchema> constructor =
                    YashanSchema.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });
    }
}