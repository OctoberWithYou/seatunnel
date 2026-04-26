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

package org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.source.reader.fetch;

import io.debezium.connector.base.ChangeEventQueue;
import io.debezium.pipeline.DataChangeEvent;
import io.debezium.pipeline.ErrorHandler;
import io.debezium.pipeline.spi.OffsetContext;
import io.debezium.pipeline.spi.Partition;
import io.debezium.relational.RelationalDatabaseSchema;
import io.debezium.relational.Table;
import io.debezium.relational.Tables;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.seatunnel.api.table.type.SeaTunnelRowType;
import org.apache.seatunnel.connectors.cdc.base.config.JdbcSourceConfig;
import org.apache.seatunnel.connectors.cdc.base.dialect.JdbcDataSourceDialect;
import org.apache.seatunnel.connectors.cdc.base.relational.JdbcSourceEventDispatcher;
import org.apache.seatunnel.connectors.cdc.base.source.offset.Offset;
import org.apache.seatunnel.connectors.cdc.base.source.reader.external.JdbcSourceFetchTaskContext;
import org.apache.seatunnel.connectors.cdc.base.source.split.SourceSplitBase;
import org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.source.YashanDialect;

import io.debezium.config.Configuration;
import io.debezium.relational.TableId;

/** YashanDB CDC fetch task context. */
public class YashanSourceFetchTaskContext extends JdbcSourceFetchTaskContext {

    private final YashanDialect dialect;

    public YashanSourceFetchTaskContext(JdbcSourceConfig sourceConfig, YashanDialect dialect) {
        super(sourceConfig, dialect);
        this.dialect = dialect;
    }

    @Override
    public RelationalDatabaseSchema getDatabaseSchema() {
        return null;
    }

    @Override
    public SeaTunnelRowType getSplitType(Table table) {
        return null;
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return null;
    }

    @Override
    public JdbcSourceEventDispatcher getDispatcher() {
        // TODO: Implement event dispatcher for YashanDB CDC full-load
        return null;
    }

    @Override
    public OffsetContext getOffsetContext() {
        return null;
    }

    @Override
    public Partition getPartition() {
        return null;
    }

    @Override
    public void configure(SourceSplitBase sourceSplitBase) {

    }

    @Override
    public ChangeEventQueue<DataChangeEvent> getQueue() {
        return null;
    }

    @Override
    public Tables.TableFilter getTableFilter() {
        return null;
    }

    @Override
    public Offset getStreamOffset(SourceRecord record) {
        return null;
    }

    @Override
    public void close() {

    }
}
