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

import org.apache.seatunnel.connectors.cdc.base.option.StartupMode;
import org.apache.seatunnel.connectors.cdc.base.option.StopMode;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Test for {@link YashanIncrementalSourceOptions}. */
public class YashanIncrementalSourceOptionsTest {

    @Test
    public void testIdentifier() {
        Assertions.assertEquals("YashanDB-CDC", YashanIncrementalSourceOptions.IDENTIFIER);
    }

    @Test
    public void testUrlOption() {
        Assertions.assertNotNull(YashanIncrementalSourceOptions.URL);
        Assertions.assertEquals("url", YashanIncrementalSourceOptions.URL.key());
    }

    @Test
    public void testSchemaNamesOption() {
        Assertions.assertNotNull(YashanIncrementalSourceOptions.SCHEMA_NAMES);
        Assertions.assertEquals("schema-names", YashanIncrementalSourceOptions.SCHEMA_NAMES.key());
    }

    @Test
    public void testStartupModeOption() {
        Assertions.assertNotNull(YashanIncrementalSourceOptions.STARTUP_MODE);
        Assertions.assertEquals(StartupMode.INITIAL, YashanIncrementalSourceOptions.STARTUP_MODE.defaultValue());
    }

    @Test
    public void testStartupModeOptions() {
        // Should contain INITIAL, LATEST, TIMESTAMP
        var choices = YashanIncrementalSourceOptions.STARTUP_MODE.choices();
        Assertions.assertTrue(choices.contains(StartupMode.INITIAL));
        Assertions.assertTrue(choices.contains(StartupMode.LATEST));
        Assertions.assertTrue(choices.contains(StartupMode.TIMESTAMP));
    }

    @Test
    public void testStopModeOption() {
        Assertions.assertNotNull(YashanIncrementalSourceOptions.STOP_MODE);
        Assertions.assertEquals(StopMode.NEVER, YashanIncrementalSourceOptions.STOP_MODE.defaultValue());
    }

    @Test
    public void testStopModeOptions() {
        // Should only contain NEVER
        var choices = YashanIncrementalSourceOptions.STOP_MODE.choices();
        Assertions.assertTrue(choices.contains(StopMode.NEVER));
        Assertions.assertEquals(1, choices.size());
    }

    @Test
    public void testUseSelectCountOption() {
        Assertions.assertNotNull(YashanIncrementalSourceOptions.USE_SELECT_COUNT);
        Assertions.assertEquals("use-select-count", YashanIncrementalSourceOptions.USE_SELECT_COUNT.key());
        Assertions.assertFalse(YashanIncrementalSourceOptions.USE_SELECT_COUNT.defaultValue());
    }

    @Test
    public void testSkipAnalyzeOption() {
        Assertions.assertNotNull(YashanIncrementalSourceOptions.SKIP_ANALYZE);
        Assertions.assertEquals("skip-analyze", YashanIncrementalSourceOptions.SKIP_ANALYZE.key());
        Assertions.assertFalse(YashanIncrementalSourceOptions.SKIP_ANALYZE.defaultValue());
    }

    @Test
    public void testUrlOptionNoDefaultValue() {
        Assertions.assertNull(YashanIncrementalSourceOptions.URL.defaultValue());
    }

    @Test
    public void testSchemaNamesNoDefaultValue() {
        Assertions.assertNull(YashanIncrementalSourceOptions.SCHEMA_NAMES.defaultValue());
    }

    @Test
    public void testStartupModeKey() {
        Assertions.assertEquals("startup.mode", YashanIncrementalSourceOptions.STARTUP_MODE.key());
    }

    @Test
    public void testStopModeKey() {
        Assertions.assertEquals("stop.mode", YashanIncrementalSourceOptions.STOP_MODE.key());
    }

    @Test
    public void testOptionsNotNull() {
        Assertions.assertNotNull(YashanIncrementalSourceOptions.URL);
        Assertions.assertNotNull(YashanIncrementalSourceOptions.SCHEMA_NAMES);
        Assertions.assertNotNull(YashanIncrementalSourceOptions.STARTUP_MODE);
        Assertions.assertNotNull(YashanIncrementalSourceOptions.STOP_MODE);
        Assertions.assertNotNull(YashanIncrementalSourceOptions.USE_SELECT_COUNT);
        Assertions.assertNotNull(YashanIncrementalSourceOptions.SKIP_ANALYZE);
    }
}