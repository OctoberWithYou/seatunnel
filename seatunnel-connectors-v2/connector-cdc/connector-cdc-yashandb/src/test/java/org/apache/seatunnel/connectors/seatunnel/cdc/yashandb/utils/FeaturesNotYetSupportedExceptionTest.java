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

/** Test for {@link FeaturesNotYetSupportedException}. */
public class FeaturesNotYetSupportedExceptionTest {

    @Test
    public void testConstructorWithMessage() {
        String msg = "Test feature";
        FeaturesNotYetSupportedException exception = new FeaturesNotYetSupportedException(msg);

        Assertions.assertNotNull(exception);
        Assertions.assertTrue(exception.getMessage().contains("YashanDB connector not support"));
        Assertions.assertTrue(exception.getMessage().contains(msg));
    }

    @Test
    public void testConstructorWithEmptyMessage() {
        FeaturesNotYetSupportedException exception = new FeaturesNotYetSupportedException("");

        Assertions.assertNotNull(exception);
        Assertions.assertTrue(exception.getMessage().contains("YashanDB connector not support"));
    }

    @Test
    public void testConstructorWithNullMessage() {
        FeaturesNotYetSupportedException exception = new FeaturesNotYetSupportedException(null);

        Assertions.assertNotNull(exception);
        Assertions.assertTrue(exception.getMessage().contains("YashanDB connector not support"));
    }

    @Test
    public void testExceptionIsRuntimeException() {
        FeaturesNotYetSupportedException exception = new FeaturesNotYetSupportedException("test");

        Assertions.assertTrue(exception instanceof RuntimeException);
    }

    @Test
    public void testExceptionCanBeThrown() {
        Assertions.assertThrows(FeaturesNotYetSupportedException.class, () -> {
            throw new FeaturesNotYetSupportedException("test");
        });
    }

    @Test
    public void testExceptionCanBeCaught() {
        try {
            throw new FeaturesNotYetSupportedException("test");
        } catch (FeaturesNotYetSupportedException e) {
            Assertions.assertNotNull(e);
        }
    }

    @Test
    public void testMessageFormat() {
        String feature = "CDC incremental capture";
        FeaturesNotYetSupportedException exception = new FeaturesNotYetSupportedException(feature);

        String expectedPrefix = "YashanDB connector not support Exception.";
        Assertions.assertTrue(exception.getMessage().startsWith(expectedPrefix));
        Assertions.assertTrue(exception.getMessage().endsWith(feature));
    }
}