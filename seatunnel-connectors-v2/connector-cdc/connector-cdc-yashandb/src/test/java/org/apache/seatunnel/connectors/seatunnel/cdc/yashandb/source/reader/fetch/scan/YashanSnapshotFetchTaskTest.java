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

package org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.source.reader.fetch.scan;

import org.apache.seatunnel.connectors.cdc.base.source.split.SnapshotSplit;
import org.apache.seatunnel.connectors.cdc.base.source.split.SourceSplitBase;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

/** Test for {@link YashanSnapshotFetchTask}. */
public class YashanSnapshotFetchTaskTest {

    @Test
    public void testConstructor() {
        SnapshotSplit split = createSplit();
        YashanSnapshotFetchTask task = new YashanSnapshotFetchTask(split);
        Assertions.assertNotNull(task);
    }

    @Test
    public void testGetSplit() {
        SnapshotSplit split = createSplit();
        YashanSnapshotFetchTask task = new YashanSnapshotFetchTask(split);
        SourceSplitBase returnedSplit = task.getSplit();
        Assertions.assertNotNull(returnedSplit);
        Assertions.assertEquals(split, returnedSplit);
    }

    @Test
    public void testIsRunningReturnsFalse() {
        SnapshotSplit split = createSplit();
        YashanSnapshotFetchTask task = new YashanSnapshotFetchTask(split);
        Assertions.assertFalse(task.isRunning());
    }

    @Test
    public void testShutdownDoesNotThrow() {
        SnapshotSplit split = createSplit();
        YashanSnapshotFetchTask task = new YashanSnapshotFetchTask(split);
        task.shutdown();
    }

    @Test
    public void testExecuteThrowsUnsupportedOperationException() {
        SnapshotSplit split = createSplit();
        YashanSnapshotFetchTask task = new YashanSnapshotFetchTask(split);

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
        SnapshotSplit split = createSplit();
        YashanSnapshotFetchTask task = new YashanSnapshotFetchTask(split);

        org.apache.seatunnel.connectors.cdc.base.source.reader.external.FetchTask.Context context =
                new org.apache.seatunnel.connectors.cdc.base.source.reader.external.FetchTask.Context() {
                    @Override
                    public void output(org.apache.seatunnel.api.source.Collector collector) {}
                };

        UnsupportedOperationException exception = Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> task.execute(context));

        Assertions.assertTrue(exception.getMessage().contains("not yet fully wired"));
    }

    @Test
    public void testGetDispatcherReturnsNull() {
        SnapshotSplit split = createSplit();
        YashanSnapshotFetchTask task = new YashanSnapshotFetchTask(split);
        Assertions.assertNull(task.getDispatcher());
    }

    private SnapshotSplit createSplit() {
        return new SnapshotSplit(
                "split-0",
                "testdb",
                "PUBLIC",
                "TEST_TABLE",
                null,
                null,
                null,
                Collections.emptyList());
    }
}