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

import org.apache.seatunnel.connectors.cdc.base.config.JdbcSourceConfig;

import io.debezium.jdbc.JdbcConnection;
import io.debezium.relational.TableId;
import io.debezium.relational.history.TableChanges;

import java.util.ArrayList;
import java.util.List;

/** YashanDB utility class. */
public class YashanUtils {

    private YashanUtils() {}

    /** Discover tables from the source configuration. */
    public static List<TableId> discoverTables(JdbcSourceConfig sourceConfig) {
        List<TableId> tableIds = new ArrayList<>();
        List<String> databaseList = sourceConfig.getDatabaseList();
        String defaultDatabase =
                (databaseList != null && !databaseList.isEmpty()) ? databaseList.get(0) : "default";
        List<String> tableList = sourceConfig.getTableList();
        if (tableList != null) {
            for (String table : tableList) {
                String[] parts = table.split("\\.");
                if (parts.length == 3) {
                    tableIds.add(new TableId(parts[0], parts[1], parts[2]));
                } else if (parts.length == 2) {
                    tableIds.add(new TableId(defaultDatabase, parts[0], parts[1]));
                } else {
                    tableIds.add(new TableId(defaultDatabase, null, parts[0]));
                }
            }
        }
        return tableIds;
    }

    public static TableChanges.TableChange queryTableSchema(JdbcConnection jdbc, TableId tableId) {
        return YashanSchema.queryTableSchema(jdbc, tableId);
    }
}
