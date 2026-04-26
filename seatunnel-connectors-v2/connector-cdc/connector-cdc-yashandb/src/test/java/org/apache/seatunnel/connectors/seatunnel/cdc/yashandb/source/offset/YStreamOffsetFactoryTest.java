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

package org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.source.offset;

import org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.config.YashanSourceConfigFactory;
import org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.source.YashanDialect;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Test for {@link YStreamOffsetFactory}. */
public class YStreamOffsetFactoryTest {

    @Test
    public void testEarliest() {
        YashanSourceConfigFactory configFactory = new YashanSourceConfigFactory();
        YashanDialect dialect = new YashanDialect(configFactory, java.util.Collections.emptyList());
        YStreamOffsetFactory factory = new YStreamOffsetFactory(configFactory, dialect);

        YStreamOffset offset = (YStreamOffset) factory.earliest();

        Assertions.assertNotNull(offset);
        Assertions.assertEquals("0", offset.getScn());
        Assertions.assertEquals("0", offset.getCommitScn());
    }

    @Test
    public void testNeverStop() {
        YashanSourceConfigFactory configFactory = new YashanSourceConfigFactory();
        YashanDialect dialect = new YashanDialect(configFactory, java.util.Collections.emptyList());
        YStreamOffsetFactory factory = new YStreamOffsetFactory(configFactory, dialect);

        Assertions.assertNull(factory.neverStop());
    }

    @Test
    public void testLatest() {
        YashanSourceConfigFactory configFactory = new YashanSourceConfigFactory();
        YashanDialect dialect = new YashanDialect(configFactory, java.util.Collections.emptyList());
        YStreamOffsetFactory factory = new YStreamOffsetFactory(configFactory, dialect);

        Assertions.assertNull(factory.latest());
    }

    @Test
    public void testSpecificWithMap() {
        YashanSourceConfigFactory configFactory = new YashanSourceConfigFactory();
        YashanDialect dialect = new YashanDialect(configFactory, java.util.Collections.emptyList());
        YStreamOffsetFactory factory = new YStreamOffsetFactory(configFactory, dialect);

        java.util.Map<String, String> offsetMap = new java.util.HashMap<>();
        offsetMap.put("scn", "1000");
        offsetMap.put("commit_scn", "1001");

        Assertions.assertNull(factory.specific(offsetMap));
    }

    @Test
    public void testSpecificWithFilenameAndPosition() {
        YashanSourceConfigFactory configFactory = new YashanSourceConfigFactory();
        YashanDialect dialect = new YashanDialect(configFactory, java.util.Collections.emptyList());
        YStreamOffsetFactory factory = new YStreamOffsetFactory(configFactory, dialect);

        Assertions.assertNull(factory.specific("file.log", 100L));
    }

    @Test
    public void testTimestamp() {
        YashanSourceConfigFactory configFactory = new YashanSourceConfigFactory();
        YashanDialect dialect = new YashanDialect(configFactory, java.util.Collections.emptyList());
        YStreamOffsetFactory factory = new YStreamOffsetFactory(configFactory, dialect);

        long timestamp = System.currentTimeMillis();
        YStreamOffset offset = (YStreamOffset) factory.timestamp(timestamp);

        Assertions.assertNotNull(offset);
        Assertions.assertEquals(Long.toString(timestamp), offset.getScn());
        Assertions.assertEquals(Long.toString(timestamp), offset.getCommitScn());
    }

    @Test
    public void testTimestampWithZero() {
        YashanSourceConfigFactory configFactory = new YashanSourceConfigFactory();
        YashanDialect dialect = new YashanDialect(configFactory, java.util.Collections.emptyList());
        YStreamOffsetFactory factory = new YStreamOffsetFactory(configFactory, dialect);

        YStreamOffset offset = (YStreamOffset) factory.timestamp(0L);

        Assertions.assertNotNull(offset);
        Assertions.assertEquals("0", offset.getScn());
        Assertions.assertEquals("0", offset.getCommitScn());
    }

    @Test
    public void testTimestampWithLargeValue() {
        YashanSourceConfigFactory configFactory = new YashanSourceConfigFactory();
        YashanDialect dialect = new YashanDialect(configFactory, java.util.Collections.emptyList());
        YStreamOffsetFactory factory = new YStreamOffsetFactory(configFactory, dialect);

        long largeTimestamp = 9999999999999L;
        YStreamOffset offset = (YStreamOffset) factory.timestamp(largeTimestamp);

        Assertions.assertNotNull(offset);
        Assertions.assertEquals(Long.toString(largeTimestamp), offset.getScn());
    }

    @Test
    public void testConstructor() {
        YashanSourceConfigFactory configFactory = new YashanSourceConfigFactory();
        YashanDialect dialect = new YashanDialect(configFactory, java.util.Collections.emptyList());

        YStreamOffsetFactory factory = new YStreamOffsetFactory(configFactory, dialect);

        Assertions.assertNotNull(factory);
    }
}