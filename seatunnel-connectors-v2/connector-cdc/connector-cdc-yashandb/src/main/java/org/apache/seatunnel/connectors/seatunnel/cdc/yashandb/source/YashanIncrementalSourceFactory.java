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

import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.api.options.ConnectorCommonOptions;
import org.apache.seatunnel.api.source.SeaTunnelSource;
import org.apache.seatunnel.api.source.SourceSplit;
import org.apache.seatunnel.api.table.catalog.CatalogTable;
import org.apache.seatunnel.api.table.catalog.CatalogTableUtil;
import org.apache.seatunnel.api.table.catalog.TablePath;
import org.apache.seatunnel.api.table.connector.TableSource;
import org.apache.seatunnel.api.table.factory.Factory;
import org.apache.seatunnel.api.table.factory.TableSourceFactoryContext;
import org.apache.seatunnel.connectors.cdc.base.config.JdbcSourceTableConfig;
import org.apache.seatunnel.connectors.cdc.base.option.SourceOptions;
import org.apache.seatunnel.connectors.cdc.base.option.StartupMode;
import org.apache.seatunnel.connectors.cdc.base.option.StopMode;
import org.apache.seatunnel.connectors.cdc.base.source.BaseChangeStreamTableSourceFactory;
import org.apache.seatunnel.connectors.cdc.base.utils.CatalogTableUtils;

import com.google.auto.service.AutoService;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/** Factory for {@link YashanIncrementalSource}. */
// TODO: YashanDB CDC connector is not yet fully implemented and tested.
// Currently only batch JDBC source/sink is supported.
// Do not enable this factory until CDC functionality is complete.
// @AutoService(Factory.class)
@Slf4j
public class YashanIncrementalSourceFactory extends BaseChangeStreamTableSourceFactory {
    @Override
    public String factoryIdentifier() {
        return YashanIncrementalSourceOptions.IDENTIFIER;
    }

    @Override
    public OptionRule optionRule() {
        return YashanIncrementalSourceOptions.getBaseRule()
                .required(YashanIncrementalSourceOptions.USERNAME, YashanIncrementalSourceOptions.PASSWORD)
                .exclusive(ConnectorCommonOptions.TABLE_NAMES, ConnectorCommonOptions.TABLE_PATTERN)
                .bundled(YashanIncrementalSourceOptions.HOSTNAME, YashanIncrementalSourceOptions.PORT)
                .optional(YashanIncrementalSourceOptions.URL)
                .optional(YashanIncrementalSourceOptions.DATABASE_NAMES)
                .optional(YashanIncrementalSourceOptions.SCHEMA_NAMES)
                .optional(YashanIncrementalSourceOptions.USE_SELECT_COUNT)
                .optional(YashanIncrementalSourceOptions.SKIP_ANALYZE)
                .optional(YashanIncrementalSourceOptions.SERVER_TIME_ZONE)
                .optional(YashanIncrementalSourceOptions.CONNECT_TIMEOUT_MS)
                .optional(YashanIncrementalSourceOptions.CONNECT_MAX_RETRIES)
                .optional(YashanIncrementalSourceOptions.CONNECTION_POOL_SIZE)
                .optional(YashanIncrementalSourceOptions.CHUNK_KEY_EVEN_DISTRIBUTION_FACTOR_LOWER_BOUND)
                .optional(YashanIncrementalSourceOptions.CHUNK_KEY_EVEN_DISTRIBUTION_FACTOR_UPPER_BOUND)
                .optional(YashanIncrementalSourceOptions.SAMPLE_SHARDING_THRESHOLD)
                .optional(YashanIncrementalSourceOptions.TABLE_NAMES_CONFIG)
                .optional(SourceOptions.SCHEMA_CHANGES_ENABLED)
                .optional(YashanIncrementalSourceOptions.STARTUP_MODE, YashanIncrementalSourceOptions.STOP_MODE)
                .conditional(
                        YashanIncrementalSourceOptions.STARTUP_MODE,
                        StartupMode.SPECIFIC,
                        SourceOptions.STARTUP_SPECIFIC_OFFSET_POS)
                .conditional(
                        YashanIncrementalSourceOptions.STOP_MODE,
                        StopMode.SPECIFIC,
                        SourceOptions.STOP_SPECIFIC_OFFSET_POS)
                .conditional(
                        YashanIncrementalSourceOptions.STARTUP_MODE,
                        StartupMode.TIMESTAMP,
                        SourceOptions.STARTUP_TIMESTAMP)
                .conditional(
                        YashanIncrementalSourceOptions.STOP_MODE,
                        StopMode.TIMESTAMP,
                        SourceOptions.STOP_TIMESTAMP)
                .conditional(
                        YashanIncrementalSourceOptions.STARTUP_MODE,
                        StartupMode.INITIAL,
                        SourceOptions.EXACTLY_ONCE)
                .build();
    }

    @Override
    public Class<? extends SeaTunnelSource> getSourceClass() {
        return YashanIncrementalSource.class;
    }

    @Override
    public <T, SplitT extends SourceSplit, StateT extends Serializable>
            TableSource<T, SplitT, StateT> restoreSource(
                    TableSourceFactoryContext context, List<CatalogTable> restoreTables) {
        return () -> {
            try {
                Class.forName("com.yashandb.jdbc.Driver");
            } catch (Exception e) {
                log.warn("Failed to load JDBC driver {}", "com.yashandb.jdbc.Driver", e);
            }
            List<CatalogTable> catalogTables =
                    CatalogTableUtil.getCatalogTables(
                            context.getOptions(), context.getClassLoader());
            boolean enableSchemaChange =
                    context.getOptions()
                            .getOptional(SourceOptions.SCHEMA_CHANGES_ENABLED)
                            .orElse(false);
            if (!restoreTables.isEmpty() && enableSchemaChange) {
                catalogTables = mergeTableStruct(catalogTables, restoreTables);
            }

            Optional<List<JdbcSourceTableConfig>> tableConfigs =
                    context.getOptions()
                            .getOptional(YashanIncrementalSourceOptions.TABLE_NAMES_CONFIG);
            if (tableConfigs.isPresent()) {
                catalogTables =
                        CatalogTableUtils.mergeCatalogTableConfig(
                                catalogTables, tableConfigs.get(), s -> TablePath.of(s, true));
            }
            return new YashanIncrementalSource(context.getOptions(), catalogTables);
        };
    }
}
