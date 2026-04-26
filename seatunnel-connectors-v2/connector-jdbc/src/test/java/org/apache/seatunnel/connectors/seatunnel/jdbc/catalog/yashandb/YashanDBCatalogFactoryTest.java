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
import org.apache.seatunnel.api.configuration.util.OptionValidationException;
import org.apache.seatunnel.api.table.catalog.Catalog;
import org.apache.seatunnel.api.table.factory.Factory;
import org.apache.seatunnel.connectors.seatunnel.jdbc.config.JdbcCommonOptions;
import org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.DatabaseIdentifier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/** Test for {@link YashanDBCatalogFactory}. */
public class YashanDBCatalogFactoryTest {

    @Test
    public void testFactoryIdentifier() {
        YashanDBCatalogFactory factory = new YashanDBCatalogFactory();
        Assertions.assertEquals(DatabaseIdentifier.YASHANDB, factory.factoryIdentifier());
    }

    @Test
    public void testOptionRule() {
        YashanDBCatalogFactory factory = new YashanDBCatalogFactory();
        Assertions.assertNotNull(factory.optionRule());
    }

    @Test
    public void testCreateCatalogWithValidUrl() {
        YashanDBCatalogFactory factory = new YashanDBCatalogFactory();

        Map<String, Object> configMap = new HashMap<>();
        configMap.put(JdbcCommonOptions.URL.key(), "jdbc:yasdb://192.168.1.11:1688/testdb");
        configMap.put(JdbcCommonOptions.USERNAME.key(), "SYS");
        configMap.put(JdbcCommonOptions.PASSWORD.key(), "yasdb_123");

        ReadonlyConfig config = ReadonlyConfig.fromMap(configMap);
        Catalog catalog = factory.createCatalog("yashandb_catalog", config);

        Assertions.assertNotNull(catalog);
        Assertions.assertTrue(catalog instanceof YashanDBCatalog);
    }

    @Test
    public void testCreateCatalogWithSchema() {
        YashanDBCatalogFactory factory = new YashanDBCatalogFactory();

        Map<String, Object> configMap = new HashMap<>();
        configMap.put(JdbcCommonOptions.URL.key(), "jdbc:yasdb://192.168.1.11:1688/testdb");
        configMap.put(JdbcCommonOptions.USERNAME.key(), "SYS");
        configMap.put(JdbcCommonOptions.PASSWORD.key(), "yasdb_123");
        configMap.put(JdbcCommonOptions.SCHEMA.key(), "PUBLIC");

        ReadonlyConfig config = ReadonlyConfig.fromMap(configMap);
        Catalog catalog = factory.createCatalog("yashandb_catalog", config);

        Assertions.assertNotNull(catalog);
    }

    @Test
    public void testCreateCatalogWithDriver() {
        YashanDBCatalogFactory factory = new YashanDBCatalogFactory();

        Map<String, Object> configMap = new HashMap<>();
        configMap.put(JdbcCommonOptions.URL.key(), "jdbc:yasdb://192.168.1.11:1688/testdb");
        configMap.put(JdbcCommonOptions.USERNAME.key(), "SYS");
        configMap.put(JdbcCommonOptions.PASSWORD.key(), "yasdb_123");
        configMap.put(JdbcCommonOptions.DRIVER.key(), "com.yashandb.jdbc.Driver");

        ReadonlyConfig config = ReadonlyConfig.fromMap(configMap);
        Catalog catalog = factory.createCatalog("yashandb_catalog", config);

        Assertions.assertNotNull(catalog);
    }

    @Test
    public void testCreateCatalogWithDecimalTypeNarrowing() {
        YashanDBCatalogFactory factory = new YashanDBCatalogFactory();

        Map<String, Object> configMap = new HashMap<>();
        configMap.put(JdbcCommonOptions.URL.key(), "jdbc:yasdb://192.168.1.11:1688/testdb");
        configMap.put(JdbcCommonOptions.USERNAME.key(), "SYS");
        configMap.put(JdbcCommonOptions.PASSWORD.key(), "yasdb_123");
        configMap.put(JdbcCommonOptions.DECIMAL_TYPE_NARROWING.key(), true);

        ReadonlyConfig config = ReadonlyConfig.fromMap(configMap);
        Catalog catalog = factory.createCatalog("yashandb_catalog", config);

        Assertions.assertNotNull(catalog);
    }

    @Test
    public void testCreateCatalogWithHandleBlobAsString() {
        YashanDBCatalogFactory factory = new YashanDBCatalogFactory();

        Map<String, Object> configMap = new HashMap<>();
        configMap.put(JdbcCommonOptions.URL.key(), "jdbc:yasdb://192.168.1.11:1688/testdb");
        configMap.put(JdbcCommonOptions.USERNAME.key(), "SYS");
        configMap.put(JdbcCommonOptions.PASSWORD.key(), "yasdb_123");
        configMap.put(JdbcCommonOptions.HANDLE_BLOB_AS_STRING.key(), true);

        ReadonlyConfig config = ReadonlyConfig.fromMap(configMap);
        Catalog catalog = factory.createCatalog("yashandb_catalog", config);

        Assertions.assertNotNull(catalog);
    }

    @Test
    public void testCreateCatalogWithInvalidUrlNoDatabase() {
        YashanDBCatalogFactory factory = new YashanDBCatalogFactory();

        Map<String, Object> configMap = new HashMap<>();
        configMap.put(JdbcCommonOptions.URL.key(), "jdbc:yasdb://192.168.1.11:1688/");
        configMap.put(JdbcCommonOptions.USERNAME.key(), "SYS");
        configMap.put(JdbcCommonOptions.PASSWORD.key(), "yasdb_123");

        ReadonlyConfig config = ReadonlyConfig.fromMap(configMap);

        Assertions.assertThrows(OptionValidationException.class, () -> {
            factory.createCatalog("yashandb_catalog", config);
        });
    }

    @Test
    public void testCreateCatalogWithMissingUrl() {
        YashanDBCatalogFactory factory = new YashanDBCatalogFactory();

        Map<String, Object> configMap = new HashMap<>();
        configMap.put(JdbcCommonOptions.USERNAME.key(), "SYS");
        configMap.put(JdbcCommonOptions.PASSWORD.key(), "yasdb_123");

        ReadonlyConfig config = ReadonlyConfig.fromMap(configMap);

        Assertions.assertThrows(Exception.class, () -> {
            factory.createCatalog("yashandb_catalog", config);
        });
    }

    @Test
    public void testFactoryIsDiscoverable() {
        ServiceLoader<Factory> loader = ServiceLoader.load(Factory.class);
        boolean found = false;
        for (Factory factory : loader) {
            if (factory instanceof YashanDBCatalogFactory) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found, "YashanDBCatalogFactory should be discoverable via ServiceLoader");
    }

    @Test
    public void testCatalogName() {
        YashanDBCatalogFactory factory = new YashanDBCatalogFactory();

        Map<String, Object> configMap = new HashMap<>();
        configMap.put(JdbcCommonOptions.URL.key(), "jdbc:yasdb://192.168.1.11:1688/testdb");
        configMap.put(JdbcCommonOptions.USERNAME.key(), "SYS");
        configMap.put(JdbcCommonOptions.PASSWORD.key(), "yasdb_123");

        ReadonlyConfig config = ReadonlyConfig.fromMap(configMap);
        Catalog catalog = factory.createCatalog("my_custom_catalog", config);

        Assertions.assertEquals("my_custom_catalog", catalog.getName());
    }
}