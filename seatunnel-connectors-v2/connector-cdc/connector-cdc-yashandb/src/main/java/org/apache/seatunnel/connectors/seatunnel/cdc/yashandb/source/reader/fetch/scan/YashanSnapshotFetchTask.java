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

import org.apache.seatunnel.connectors.cdc.base.relational.JdbcSourceEventDispatcher;
import org.apache.seatunnel.connectors.cdc.base.source.reader.external.FetchTask;
import org.apache.seatunnel.connectors.cdc.base.source.split.SnapshotSplit;
import org.apache.seatunnel.connectors.cdc.base.source.split.SourceSplitBase;

/**
 * Snapshot fetch task for YashanDB CDC.
 *
 * <p>Reads a chunk of table data using JDBC during the snapshot phase. This is the full-load part.
 */
public class YashanSnapshotFetchTask implements FetchTask<SourceSplitBase> {

    private final SnapshotSplit split;

    public YashanSnapshotFetchTask(SnapshotSplit split) {
        this.split = split;
    }

    @Override
    public void execute(Context context) throws Exception {
        // TODO: Implement JDBC-based snapshot chunk reading
        // Uses YashanDB JDBC driver to execute:
        //   SELECT * FROM schema.table WHERE chunk_column >= ? AND chunk_column < ?
        // Converts JDBC ResultSet → SeaTunnel RowData via YashanDBTypeMapper
        throw new UnsupportedOperationException(
                "Snapshot fetch task execution not yet fully wired. "
                        + "The JDBC dialect and type mapping infrastructure is in place.");
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public void shutdown() {
        // No resources to close for JDBC snapshot fetch
    }

    @Override
    public SourceSplitBase getSplit() {
        return split;
    }

    public JdbcSourceEventDispatcher getDispatcher() {
        // TODO: Return the event dispatcher
        return null;
    }
}
