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

package org.apache.seatunnel.connectors.seatunnel.jdbc.catalog.yashandb;

import org.apache.seatunnel.api.configuration.ReadonlyConfig;
import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.api.configuration.util.OptionValidationException;
import org.apache.seatunnel.api.table.catalog.Catalog;
import org.apache.seatunnel.api.table.factory.CatalogFactory;
import org.apache.seatunnel.api.table.factory.Factory;
import org.apache.seatunnel.common.utils.JdbcUrlUtil;
import org.apache.seatunnel.connectors.seatunnel.jdbc.config.JdbcCommonOptions;
import org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.DatabaseIdentifier;

import com.google.auto.service.AutoService;

import java.util.Optional;

/**
 * Factory for creating {@link YashanDBCatalog} instances.
 *
 * <p>This factory is responsible for:
 * <ul>
 *   <li>Creating catalog instances for YashanDB database connections</li>
 *   <li>Validating JDBC URL configuration</li>
 *   <li>Extracting database connection parameters from configuration</li>
 * </ul>
 *
 * <p>The factory is automatically discovered via {@link AutoService} annotation
 * and registered in the SeaTunnel plugin system.
 *
 * <p><b>Configuration Requirements:</b>
 * <ul>
 *   <li>URL: Must be in format <code>jdbc:yasdb://host:port/database</code></li>
 *   <li>Username: Database username for authentication</li>
 *   <li>Password: Database password for authentication</li>
 *   <li>Driver: JDBC driver class name (optional, defaults to com.yashandb.jdbc.Driver)</li>
 * </ul>
 *
 * <p><b>Optional Configuration:</b>
 * <ul>
 *   <li>Schema: Default schema name to use</li>
 *   <li>Decimal Type Narrowing: Whether to narrow NUMBER to smaller integer types</li>
 *   <li>Handle Blob As String: Whether to treat BLOB as String type</li>
 * </ul>
 *
 * @see YashanDBCatalog
 * @see YashanDBURLParser
 * @see CatalogFactory
 */
@AutoService(Factory.class)
public class YashanDBCatalogFactory implements CatalogFactory {

    /**
     * Returns the factory identifier name.
     *
     * <p>This identifier is used to match the factory with the database type
     * specified in configuration files.
     *
     * @return the constant identifier "YASHANDB"
     */
    @Override
    public String factoryIdentifier() {
        return DatabaseIdentifier.YASHANDB;
    }

    /**
     * Creates a {@link YashanDBCatalog} instance from configuration options.
     *
     * <p>This method:
     * <ol>
     *   <li>Extracts JDBC URL from configuration</li>
     *   <li>Parses URL to get host, port, and database name</li>
     *   <li>Validates that database name is present in URL</li>
     *   <li>Creates catalog with all connection parameters</li>
     * </ol>
     *
     * <p><b>URL Validation:</b>
     * The URL must contain a database name. If the URL format is invalid
     * or missing the database name, an {@link OptionValidationException} is thrown.
     *
     * <p><b>Example Valid URL:</b>
     * <code>jdbc:yasdb://192.168.1.11:1688/mydb</code>
     *
     * @param catalogName the name for the created catalog
     * @param options the configuration options containing connection parameters
     * @return a new YashanDBCatalog instance
     * @throws OptionValidationException if URL is invalid or missing database name
     */
    @Override
    public Catalog createCatalog(String catalogName, ReadonlyConfig options) {
        String urlWithDatabase = options.get(JdbcCommonOptions.URL);
        JdbcUrlUtil.UrlInfo urlInfo = YashanDBURLParser.parse(urlWithDatabase);
        Optional<String> defaultDatabase = urlInfo.getDefaultDatabase();
        if (!defaultDatabase.isPresent()) {
            throw new OptionValidationException(JdbcCommonOptions.URL);
        }
        return new YashanDBCatalog(
                catalogName,
                options.get(JdbcCommonOptions.USERNAME),
                options.get(JdbcCommonOptions.PASSWORD),
                urlInfo,
                options.get(JdbcCommonOptions.SCHEMA),
                options.get(JdbcCommonOptions.DECIMAL_TYPE_NARROWING),
                options.get(JdbcCommonOptions.DRIVER),
                options.getOptional(JdbcCommonOptions.HANDLE_BLOB_AS_STRING).orElse(false));
    }

    /**
     * Returns the option rule for catalog configuration validation.
     *
     * <p>The rule defines which options are required and which are optional
     * for creating a YashanDB catalog. This is used by SeaTunnel to validate
     * user configuration before creating the catalog.
     *
     * @return the option rule based on JDBC common catalog options
     */
    @Override
    public OptionRule optionRule() {
        return JdbcCommonOptions.BASE_CATALOG_RULE.build();
    }
}