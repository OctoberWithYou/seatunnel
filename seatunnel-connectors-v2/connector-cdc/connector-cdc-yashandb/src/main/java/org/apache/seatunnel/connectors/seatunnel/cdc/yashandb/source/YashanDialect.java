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

import org.apache.seatunnel.connectors.cdc.base.config.JdbcSourceConfig;
import org.apache.seatunnel.connectors.cdc.base.dialect.JdbcDataSourceDialect;
import org.apache.seatunnel.connectors.cdc.base.source.enumerator.splitter.ChunkSplitter;
import org.apache.seatunnel.connectors.cdc.base.source.reader.external.FetchTask;
import org.apache.seatunnel.connectors.cdc.base.source.split.SourceSplitBase;
import org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.config.YashanSourceConfigFactory;
import org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.source.enumerator.YashanChunkSplitter;
import org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.source.reader.fetch.YashanSourceFetchTaskContext;
import org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.source.reader.fetch.scan.YashanSnapshotFetchTask;
import org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.source.reader.fetch.ystream.YStreamFetchTask;
import org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.utils.YashanConnectionUtils;
import org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.utils.YashanUtils;

import org.apache.seatunnel.api.table.catalog.CatalogTable;

import io.debezium.jdbc.JdbcConnection;
import io.debezium.relational.TableId;
import io.debezium.relational.history.TableChanges;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/** YashanDB CDC dialect. */
@Slf4j
public class YashanDialect implements JdbcDataSourceDialect {

    private static final long serialVersionUID = 1L;

    private final YashanSourceConfigFactory configFactory;
    private final List<CatalogTable> catalogTables;

    public YashanDialect(
            YashanSourceConfigFactory configFactory, List<CatalogTable> catalogTables) {
        this.configFactory = configFactory;
        this.catalogTables = catalogTables;
    }

    @Override
    public String getName() {
        return YashanIncrementalSourceOptions.IDENTIFIER;
    }

    @Override
    public boolean isDataCollectionIdCaseSensitive(JdbcSourceConfig sourceConfig) {
        // YashanDB follows Oracle semantics: table names are case-insensitive by default
        return false;
    }

    @Override
    public List<TableId> discoverDataCollections(JdbcSourceConfig sourceConfig) {
        return YashanUtils.discoverTables(sourceConfig);
    }

    @Override
    public TableChanges.TableChange queryTableSchema(JdbcConnection jdbc, TableId tableId) {
        return YashanUtils.queryTableSchema(jdbc, tableId);
    }

    @Override
    public JdbcConnection openJdbcConnection(JdbcSourceConfig sourceConfig) {
        return YashanConnectionUtils.openJdbcConnection(sourceConfig);
    }

    @Override
    public ChunkSplitter createChunkSplitter(JdbcSourceConfig sourceConfig) {
        return new YashanChunkSplitter(sourceConfig);
    }

    @Override
    public FetchTask<SourceSplitBase> createFetchTask(SourceSplitBase sourceSplitBase) {
        if (sourceSplitBase.isSnapshotSplit()) {
            return new YashanSnapshotFetchTask(sourceSplitBase.asSnapshotSplit());
        } else {
            // TODO: Implement YStreamFetchTask for incremental capture
            return new YStreamFetchTask(sourceSplitBase);
        }
    }

    @Override
    public YashanSourceFetchTaskContext createFetchTaskContext(
            SourceSplitBase sourceSplitBase, JdbcSourceConfig taskSourceConfig) {
        return new YashanSourceFetchTaskContext(taskSourceConfig, this);
    }

}
