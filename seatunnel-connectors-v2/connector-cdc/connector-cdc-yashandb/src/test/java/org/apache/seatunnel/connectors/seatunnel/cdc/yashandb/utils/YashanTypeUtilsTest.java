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

/** Test for {@link YashanTypeUtils}. */
public class YashanTypeUtilsTest {

    @Test
    public void testPrivateConstructor() {
        // Verify that YashanTypeUtils cannot be instantiated directly
        Assertions.assertThrows(Exception.class, () -> {
            java.lang.reflect.Constructor<YashanTypeUtils> constructor =
                    YashanTypeUtils.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });
    }

    @Test
    public void testClassExists() {
        // Verify the class exists and is accessible
        Assertions.assertNotNull(YashanTypeUtils.class);
    }

    @Test
    public void testClassIsUtilityClass() {
        // Verify it's a utility class with only private constructor
        java.lang.reflect.Constructor<?>[] constructors = YashanTypeUtils.class.getDeclaredConstructors();
        Assertions.assertEquals(1, constructors.length);
        Assertions.assertFalse(constructors[0].isAccessible());
    }
}