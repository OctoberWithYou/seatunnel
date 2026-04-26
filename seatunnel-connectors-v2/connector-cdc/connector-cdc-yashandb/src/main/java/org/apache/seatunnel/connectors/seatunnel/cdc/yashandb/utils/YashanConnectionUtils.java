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

import io.debezium.config.Configuration;
import io.debezium.jdbc.JdbcConfiguration;
import io.debezium.jdbc.JdbcConnection;

import java.util.Properties;

/** YashanDB connection utility. */
public class YashanConnectionUtils {

    private static final String YASHANDB_CONNECTION_URL =
            "jdbc:yashanb:thin:@//${hostname}:${port}/${dbname}";

    private YashanConnectionUtils() {}

    public static JdbcConnection openJdbcConnection(JdbcSourceConfig sourceConfig) {
        String driverClassName = sourceConfig.getDriverClassName();
        String hostname = sourceConfig.getHostname();
        int port = sourceConfig.getPort();
        String username = sourceConfig.getUsername();
        String password = sourceConfig.getPassword();
        String databaseName =
                sourceConfig.getDatabaseList() != null && !sourceConfig.getDatabaseList().isEmpty()
                        ? sourceConfig.getDatabaseList().get(0)
                        : "";

        Properties props = new Properties();
        props.setProperty("database.dbname", databaseName);
        props.setProperty("database.hostname", hostname);
        props.setProperty("database.port", String.valueOf(port));
        props.setProperty("database.user", username);
        props.setProperty("database.password", password);

        Configuration config = Configuration.from(props);
        JdbcConfiguration jdbcConfig = JdbcConfiguration.adapt(config);

        return new JdbcConnection(
                jdbcConfig,
                JdbcConnection.patternBasedFactory(
                        YASHANDB_CONNECTION_URL, driverClassName, YashanConnectionUtils.class.getClassLoader()),
                "\"", "\"");
    }
}
