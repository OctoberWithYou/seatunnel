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

package org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.yashandb;

import org.apache.seatunnel.connectors.seatunnel.jdbc.config.JdbcConnectionConfig;
import org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.DatabaseIdentifier;
import org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.JdbcDialect;
import org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.JdbcDialectFactory;

import com.google.auto.service.AutoService;

import javax.annotation.Nonnull;

/**
 * Factory for creating {@link YashanDBDialect} instances.
 *
 * <p>This factory is responsible for:
 * <ul>
 *   <li>Detecting if a JDBC URL is for YashanDB database</li>
 *   <li>Creating appropriate dialect instances for YashanDB connections</li>
 * </ul>
 *
 * <p>YashanDB (崖山数据库) is a relational database developed in China,
 * compatible with Oracle syntax and protocols. The JDBC URL format is:
 * <code>jdbc:yasdb://host:port/database</code>
 *
 * <p>This factory is automatically discovered via {@link AutoService} annotation
 * and registered in the SeaTunnel plugin system.
 *
 * @see YashanDBDialect
 * @see JdbcDialectFactory
 */
@AutoService(JdbcDialectFactory.class)
public class YashanDBDialectFactory implements JdbcDialectFactory {

    /**
     * Returns the factory identifier name.
     *
     * <p>This identifier is used to match the factory with the database type
     * specified in configuration files.
     *
     * @return the constant identifier "YASHANDB"
     */
    @Override
    public String dialectFactoryName() {
        return DatabaseIdentifier.YASHANDB;
    }

    /**
     * Checks if this factory can handle the given JDBC URL.
     *
     * <p>YashanDB uses the JDBC URL format: <code>jdbc:yasdb://...</code>
     * This method validates that the URL starts with this prefix.
     *
     * <p><b>Note:</b> Only <code>jdbc:yasdb://</code> format is supported.
     * Other formats like <code>jdbc:yashandb:thin:@//</code> are NOT supported.
     *
     * @param url the JDBC URL to check
     * @return true if the URL starts with "jdbc:yasdb:", false otherwise
     */
    @Override
    public boolean acceptsURL(String url) {
        return url.startsWith("jdbc:yasdb:");
    }

    /**
     * Creates a default {@link YashanDBDialect} instance.
     *
     * <p>This method creates a dialect with default configuration,
     * using original field naming convention and default BLOB handling.
     *
     * @return a new YashanDBDialect instance with default settings
     */
    @Override
    public JdbcDialect create() {
        return new YashanDBDialect();
    }

    /**
     * Creates a {@link YashanDBDialect} instance with field naming convention.
     *
     * <p>The fieldIde parameter controls how field names are transformed:
     * <ul>
     *   <li>"original" - keep original names as-is</li>
     *   <li>"uppercase" - convert to uppercase</li>
     *   <li>"lowercase" - convert to lowercase</li>
     * </ul>
     *
     * @param compatibleMode the compatible mode (not used for YashanDB)
     * @param fieldIde the field naming convention to use
     * @return a new YashanDBDialect instance with specified settings
     */
    @Override
    public JdbcDialect create(@Nonnull String compatibleMode, String fieldIde) {
        return create(compatibleMode, fieldIde, null);
    }

    /**
     * Creates a {@link YashanDBDialect} instance with full configuration.
     *
     * <p>This is the most comprehensive factory method that allows setting:
     * <ul>
     *   <li>Field naming convention (fieldIde)</li>
     *   <li>BLOB handling mode (handleBlobAsString)</li>
     * </ul>
     *
     * <p>The handleBlobAsString option controls how BLOB data is processed:
     * <ul>
     *   <li>true - treat BLOB as String type for easier processing</li>
     *   <li>false - treat BLOB as byte array (default)</li>
     * </ul>
     *
     * @param compatibleMode the compatible mode (not used for YashanDB)
     * @param fieldIde the field naming convention to use
     * @param jdbcConnectionConfig the JDBC connection configuration containing additional options
     * @return a new YashanDBDialect instance with all specified settings
     */
    @Override
    public JdbcDialect create(
            @Nonnull String compatibleMode,
            String fieldIde,
            JdbcConnectionConfig jdbcConnectionConfig) {
        boolean handleBlobAsString =
                jdbcConnectionConfig != null && jdbcConnectionConfig.isHandleBlobAsString();
        return new YashanDBDialect(fieldIde, handleBlobAsString);
    }
}