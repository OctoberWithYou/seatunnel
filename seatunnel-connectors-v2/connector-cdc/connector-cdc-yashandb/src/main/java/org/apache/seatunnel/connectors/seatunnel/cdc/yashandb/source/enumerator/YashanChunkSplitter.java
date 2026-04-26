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

package org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.source.enumerator;

import io.debezium.jdbc.JdbcConnection;
import io.debezium.relational.Column;
import io.debezium.relational.Table;
import io.debezium.relational.TableId;
import org.apache.seatunnel.api.table.type.SeaTunnelDataType;
import org.apache.seatunnel.api.table.type.SeaTunnelRowType;
import org.apache.seatunnel.connectors.cdc.base.config.JdbcSourceConfig;
import org.apache.seatunnel.connectors.cdc.base.dialect.JdbcDataSourceDialect;
import org.apache.seatunnel.connectors.cdc.base.source.enumerator.splitter.AbstractJdbcSourceChunkSplitter;
import org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.source.YashanDialect;

import java.sql.SQLException;

/** YashanDB table chunk splitter for parallel snapshot reading. */
public class YashanChunkSplitter extends AbstractJdbcSourceChunkSplitter {

    public YashanChunkSplitter(JdbcSourceConfig sourceConfig) {
        super(sourceConfig, new YashanDialect(null, null));
    }

    @Override
    public Object[] queryMinMax(JdbcConnection jdbc, TableId tableId, String columnName) throws SQLException {
        return new Object[0];
    }

    @Override
    public Object queryMin(JdbcConnection jdbc, TableId tableId, String columnName, Object excludedLowerBound) throws SQLException {
        return null;
    }

    @Override
    public Object[] sampleDataFromColumn(JdbcConnection jdbc, TableId tableId, String columnName, int samplingRate) throws Exception {
        return new Object[0];
    }

    @Override
    public Object queryNextChunkMax(JdbcConnection jdbc, TableId tableId, String columnName, int chunkSize, Object includedLowerBound) throws SQLException {
        return null;
    }

    @Override
    public Long queryApproximateRowCnt(JdbcConnection jdbc, TableId tableId) throws SQLException {
        return 0L;
    }

    @Override
    public String buildSplitScanQuery(Table table, SeaTunnelRowType splitKeyType, boolean isFirstSplit, boolean isLastSplit) {
        return "";
    }

    @Override
    public SeaTunnelDataType<?> fromDbzColumn(Column splitColumn) {
        return null;
    }
}
