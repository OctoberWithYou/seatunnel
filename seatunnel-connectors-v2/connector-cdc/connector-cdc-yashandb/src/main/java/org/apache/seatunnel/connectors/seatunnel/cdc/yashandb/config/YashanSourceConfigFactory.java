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

package org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.config;

import org.apache.seatunnel.connectors.cdc.base.config.JdbcSourceConfig;
import org.apache.seatunnel.connectors.cdc.base.config.JdbcSourceConfigFactory;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Properties;

/** Factory for creating {@link YashanSourceConfig}. */
@Slf4j
public class YashanSourceConfigFactory extends JdbcSourceConfigFactory {

    private static final long serialVersionUID = 1L;
    private static final String DRIVER_CLASS_NAME = "com.yashandb.jdbc.Driver";

    private List<String> schemaList;
    private Boolean useSelectCount;
    private Boolean skipAnalyze;

    public JdbcSourceConfigFactory schemaList(List<String> schemaList) {
        this.schemaList = schemaList;
        return this;
    }

    public JdbcSourceConfigFactory useSelectCount(Boolean useSelectCount) {
        this.useSelectCount = useSelectCount;
        return this;
    }

    public JdbcSourceConfigFactory skipAnalyze(Boolean skipAnalyze) {
        this.skipAnalyze = skipAnalyze;
        return this;
    }

    @Override
    public JdbcSourceConfig create(int subtask) {
        try {
            Class.forName(DRIVER_CLASS_NAME);
        } catch (Exception e) {
            log.warn("Failed to load JDBC driver {}", DRIVER_CLASS_NAME, e);
        }

        Properties props = new Properties();
        // JDBC connection properties (NOT Debezium — YashanDB does not use Debezium)
        if (originUrl != null) {
            props.setProperty("database.url", originUrl);
        } else {
            if (hostname == null) {
                throw new IllegalArgumentException("hostname is required when url is not configured");
            }
            props.setProperty("database.hostname", hostname);
            if (port == 0) {
                throw new IllegalArgumentException("port is required when url is not configured");
            }
            props.setProperty("database.port", String.valueOf(port));
        }
        props.setProperty("database.user", username);
        props.setProperty("database.password", password);
        props.setProperty("connect.timeout.ms", String.valueOf(connectTimeoutMillis));

        if (schemaList != null) {
            props.setProperty("schema.include.list", String.join(",", schemaList));
        }
        if (tableList != null) {
            props.setProperty("table.include.list", String.join(",", tableList));
        }

        if (dbzProperties != null) {
            props.putAll(dbzProperties);
        }

        return new YashanSourceConfig(
                startupConfig,
                stopConfig,
                databaseList,
                tableList,
                splitSize,
                splitColumn,
                distributionFactorUpper,
                distributionFactorLower,
                sampleShardingThreshold,
                inverseSamplingRate,
                props,
                DRIVER_CLASS_NAME,
                hostname,
                port,
                username,
                password,
                originUrl,
                fetchSize,
                serverTimeZone,
                connectTimeoutMillis,
                connectMaxRetries,
                connectionPoolSize,
                exactlyOnce);
    }
}
