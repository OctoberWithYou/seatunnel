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

/** Test for {@link YashanConnectionUtils}. */
public class YashanConnectionUtilsTest {

    @Test
    public void testPrivateConstructor() {
        // Verify that YashanConnectionUtils cannot be instantiated directly
        Assertions.assertThrows(Exception.class, () -> {
            java.lang.reflect.Constructor<YashanConnectionUtils> constructor =
                    YashanConnectionUtils.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });
    }

    @Test
    public void testConnectionUrlPattern() {
        // The connection URL pattern is defined as:
        // "jdbc:yashanb:thin:@//${hostname}:${port}/${dbname}"
        // Verify the pattern is accessible via reflection
        try {
            java.lang.reflect.Field field = YashanConnectionUtils.class.getDeclaredField("YASHANDB_CONNECTION_URL");
            field.setAccessible(true);
            String urlPattern = (String) field.get(null);
            Assertions.assertNotNull(urlPattern);
            Assertions.assertTrue(urlPattern.contains("${hostname}"));
            Assertions.assertTrue(urlPattern.contains("${port}"));
            Assertions.assertTrue(urlPattern.contains("${dbname}"));
        } catch (Exception e) {
            Assertions.fail("Failed to access YASHANDB_CONNECTION_URL field: " + e.getMessage());
        }
    }

    @Test
    public void testOpenJdbcConnectionThrowsExceptionWithNullConfig() {
        // Since the method requires a valid JdbcSourceConfig, passing null should throw
        Assertions.assertThrows(Exception.class, () -> {
            YashanConnectionUtils.openJdbcConnection(null);
        });
    }
}