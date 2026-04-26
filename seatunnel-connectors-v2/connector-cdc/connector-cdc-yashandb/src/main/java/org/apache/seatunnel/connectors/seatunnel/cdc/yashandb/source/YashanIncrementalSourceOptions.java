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

import org.apache.seatunnel.api.configuration.Option;
import org.apache.seatunnel.api.configuration.Options;
import org.apache.seatunnel.api.configuration.SingleChoiceOption;
import org.apache.seatunnel.connectors.cdc.base.option.JdbcSourceOptions;
import org.apache.seatunnel.connectors.cdc.base.option.SourceOptions;
import org.apache.seatunnel.connectors.cdc.base.option.StartupMode;
import org.apache.seatunnel.connectors.cdc.base.option.StopMode;

import java.util.Arrays;
import java.util.List;

/** YashanDB-CDC connector options. Extends {@link JdbcSourceOptions} for common JDBC CDC options. */
public class YashanIncrementalSourceOptions extends JdbcSourceOptions {

    public static final String IDENTIFIER = "YashanDB-CDC";

    public static final Option<String> URL =
            Options.key("url")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("YashanDB JDBC URL. Overrides hostname and port if set.");

    public static final Option<List<String>> SCHEMA_NAMES =
            Options.key("schema-names")
                    .listType()
                    .noDefaultValue()
                    .withDescription("YashanDB schema names to capture");

    public static final SingleChoiceOption<StartupMode> STARTUP_MODE =
            (SingleChoiceOption)
                    Options.key(SourceOptions.STARTUP_MODE_KEY)
                            .singleChoice(
                                    StartupMode.class,
                                    Arrays.asList(
                                            StartupMode.INITIAL,
                                            StartupMode.LATEST,
                                            StartupMode.TIMESTAMP))
                            .defaultValue(StartupMode.INITIAL)
                            .withDescription("Startup mode for YashanDB CDC source");

    public static final SingleChoiceOption<StopMode> STOP_MODE =
            (SingleChoiceOption)
                    Options.key(SourceOptions.STOP_MODE_KEY)
                            .singleChoice(StopMode.class, Arrays.asList(StopMode.NEVER))
                            .defaultValue(StopMode.NEVER)
                            .withDescription("Stop mode for YashanDB CDC source");

    public static final Option<Boolean> USE_SELECT_COUNT =
            Options.key("use-select-count")
                    .booleanType()
                    .defaultValue(false)
                    .withDescription("Use SELECT COUNT(*) for row count estimation");

    public static final Option<Boolean> SKIP_ANALYZE =
            Options.key("skip-analyze")
                    .booleanType()
                    .defaultValue(false)
                    .withDescription("Skip ANALYZE TABLE when estimating row count");
}
