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

package org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.source.reader.fetch.ystream;

import org.apache.seatunnel.connectors.cdc.base.source.split.IncrementalSplit;
import org.apache.seatunnel.connectors.cdc.base.source.split.SourceSplitBase;
import org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.source.offset.YStreamOffset;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

/** Test for {@link YStreamFetchTask}. */
public class YStreamFetchTaskTest {

    @Test
    public void testConstructor() {
        SourceSplitBase split = createIncrementalSplit();
        YStreamFetchTask task = new YStreamFetchTask(split);
        Assertions.assertNotNull(task);
    }

    @Test
    public void testGetSplit() {
        SourceSplitBase split = createIncrementalSplit();
        YStreamFetchTask task = new YStreamFetchTask(split);
        SourceSplitBase returnedSplit = task.getSplit();
        Assertions.assertNotNull(returnedSplit);
        Assertions.assertEquals(split, returnedSplit);
    }

    @Test
    public void testIsRunningReturnsFalse() {
        SourceSplitBase split = createIncrementalSplit();
        YStreamFetchTask task = new YStreamFetchTask(split);
        Assertions.assertFalse(task.isRunning());
    }

    @Test
    public void testShutdownDoesNotThrow() {
        SourceSplitBase split = createIncrementalSplit();
        YStreamFetchTask task = new YStreamFetchTask(split);
        task.shutdown();
    }

    @Test
    public void testExecuteThrowsUnsupportedOperationException() {
        SourceSplitBase split = createIncrementalSplit();
        YStreamFetchTask task = new YStreamFetchTask(split);

        org.apache.seatunnel.connectors.cdc.base.source.reader.external.FetchTask.Context context =
                new org.apache.seatunnel.connectors.cdc.base.source.reader.external.FetchTask.Context() {
                    @Override
                    public void output(org.apache.seatunnel.api.source.Collector collector) {}
                };

        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            task.execute(context);
        });
    }

    @Test
    public void testExecuteExceptionMessage() {
        SourceSplitBase split = createIncrementalSplit();
        YStreamFetchTask task = new YStreamFetchTask(split);

        org.apache.seatunnel.connectors.cdc.base.source.reader.external.FetchTask.Context context =
                new org.apache.seatunnel.connectors.cdc.base.source.reader.external.FetchTask.Context() {
                    @Override
                    public void output(org.apache.seatunnel.api.source.Collector collector) {}
                };

        UnsupportedOperationException exception = Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> task.execute(context));

        Assertions.assertTrue(exception.getMessage().contains("not yet implemented"));
        Assertions.assertTrue(exception.getMessage().contains("YStream incremental capture"));
    }

    @Test
    public void testConstructorWithSnapshotSplit() {
        org.apache.seatunnel.connectors.cdc.base.source.split.SnapshotSplit snapshotSplit =
                new org.apache.seatunnel.connectors.cdc.base.source.split.SnapshotSplit(
                        "snapshot-0",
                        "testdb",
                        "PUBLIC",
                        "TEST_TABLE",
                        null,
                        null,
                        null,
                        Collections.emptyList());

        YStreamFetchTask task = new YStreamFetchTask(snapshotSplit);
        Assertions.assertNotNull(task);
        Assertions.assertTrue(task.getSplit().isSnapshotSplit());
    }

    private SourceSplitBase createIncrementalSplit() {
        IncrementalSplit split = new IncrementalSplit(
                "incremental-0",
                "testdb",
                Collections.emptyList(),
                new YStreamOffset(0L),
                null);
        return split;
    }
}