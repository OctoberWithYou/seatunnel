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

import org.apache.seatunnel.common.utils.JdbcUrlUtil;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Test for {@link YashanDBURLParser}. */
public class YashanDBURLParserTest {

    @Test
    public void testParseStandardUrl() {
        String url = "jdbc:yasdb://192.168.1.11:1688/ya";
        JdbcUrlUtil.UrlInfo urlInfo = YashanDBURLParser.parse(url);

        Assertions.assertEquals(url, urlInfo.getUrl());
        Assertions.assertEquals("jdbc:yasdb://192.168.1.11:1688/", urlInfo.getUrlWithoutDatabase());
        Assertions.assertEquals("192.168.1.11", urlInfo.getHost());
        Assertions.assertEquals(1688, urlInfo.getPort());
        Assertions.assertTrue(urlInfo.getDefaultDatabase().isPresent());
        Assertions.assertEquals("ya", urlInfo.getDefaultDatabase().get());
        Assertions.assertEquals("", urlInfo.getSuffix());
    }

    @Test
    public void testParseUrlWithParameters() {
        String url = "jdbc:yasdb://localhost:1688/mydb?useSSL=true&connectTimeout=5000";
        JdbcUrlUtil.UrlInfo urlInfo = YashanDBURLParser.parse(url);

        Assertions.assertEquals(url, urlInfo.getUrl());
        Assertions.assertEquals("jdbc:yasdb://localhost:1688/", urlInfo.getUrlWithoutDatabase());
        Assertions.assertEquals("localhost", urlInfo.getHost());
        Assertions.assertEquals(1688, urlInfo.getPort());
        Assertions.assertTrue(urlInfo.getDefaultDatabase().isPresent());
        Assertions.assertEquals("mydb", urlInfo.getDefaultDatabase().get());
        Assertions.assertEquals("?useSSL=true&connectTimeout=5000", urlInfo.getSuffix());
    }

    @Test
    public void testParseUrlWithIpAndPort() {
        String url = "jdbc:yasdb://10.0.0.1:1688/testdb";
        JdbcUrlUtil.UrlInfo urlInfo = YashanDBURLParser.parse(url);

        Assertions.assertEquals(url, urlInfo.getUrl());
        Assertions.assertEquals("jdbc:yasdb://10.0.0.1:1688/", urlInfo.getUrlWithoutDatabase());
        Assertions.assertEquals("10.0.0.1", urlInfo.getHost());
        Assertions.assertEquals(1688, urlInfo.getPort());
        Assertions.assertEquals("testdb", urlInfo.getDefaultDatabase().get());
    }

    @Test
    public void testParseUrlWithHostname() {
        String url = "jdbc:yasdb://db.example.com:1688/production";
        JdbcUrlUtil.UrlInfo urlInfo = YashanDBURLParser.parse(url);

        Assertions.assertEquals(url, urlInfo.getUrl());
        Assertions.assertEquals("jdbc:yasdb://db.example.com:1688/", urlInfo.getUrlWithoutDatabase());
        Assertions.assertEquals("db.example.com", urlInfo.getHost());
        Assertions.assertEquals(1688, urlInfo.getPort());
        Assertions.assertEquals("production", urlInfo.getDefaultDatabase().get());
    }

    @Test
    public void testParseUrlWithNonStandardPort() {
        String url = "jdbc:yasdb://localhost:8888/mydb";
        JdbcUrlUtil.UrlInfo urlInfo = YashanDBURLParser.parse(url);

        Assertions.assertEquals(8888, urlInfo.getPort());
        Assertions.assertEquals("mydb", urlInfo.getDefaultDatabase().get());
    }

    @Test
    public void testParseInvalidUrlFormat() {
        // Oracle-style thin URL (not supported)
        String url = "jdbc:yashandb:thin:@//localhost:1688/mydb";
        JdbcUrlUtil.UrlInfo urlInfo = YashanDBURLParser.parse(url);

        // Should return fallback info with "temp" as default database
        Assertions.assertEquals(url, urlInfo.getUrl());
        Assertions.assertEquals(url, urlInfo.getUrlWithoutDatabase());
        Assertions.assertNull(urlInfo.getHost());
        Assertions.assertNull(urlInfo.getPort());
        Assertions.assertTrue(urlInfo.getDefaultDatabase().isPresent());
        Assertions.assertEquals("temp", urlInfo.getDefaultDatabase().get());
    }

    @Test
    public void testParseInvalidUrlMissingDatabase() {
        String url = "jdbc:yasdb://localhost:1688";
        JdbcUrlUtil.UrlInfo urlInfo = YashanDBURLParser.parse(url);

        // Should return fallback since database is missing
        Assertions.assertEquals("temp", urlInfo.getDefaultDatabase().get());
    }

    @Test
    public void testParseInvalidUrlMissingPort() {
        String url = "jdbc:yasdb://localhost/mydb";
        JdbcUrlUtil.UrlInfo urlInfo = YashanDBURLParser.parse(url);

        // Should return fallback since port is missing
        Assertions.assertEquals("temp", urlInfo.getDefaultDatabase().get());
    }

    @Test
    public void testParseInvalidUrlWrongPrefix() {
        String url = "jdbc:mysql://localhost:3306/mydb";
        JdbcUrlUtil.UrlInfo urlInfo = YashanDBURLParser.parse(url);

        // Should return fallback since prefix is wrong
        Assertions.assertEquals("temp", urlInfo.getDefaultDatabase().get());
    }

    @Test
    public void testParseUrlWithEmptySuffix() {
        String url = "jdbc:yasdb://host:1688/db";
        JdbcUrlUtil.UrlInfo urlInfo = YashanDBURLParser.parse(url);

        Assertions.assertEquals("", urlInfo.getSuffix());
    }

    @Test
    public void testParseUrlWithQuestionMarkOnly() {
        String url = "jdbc:yasdb://host:1688/db?";
        JdbcUrlUtil.UrlInfo urlInfo = YashanDBURLParser.parse(url);

        Assertions.assertEquals("?", urlInfo.getSuffix());
    }

    @Test
    public void testParseUrlWithComplexDatabaseName() {
        String url = "jdbc:yasdb://host:1688/db_name_with_underscores";
        JdbcUrlUtil.UrlInfo urlInfo = YashanDBURLParser.parse(url);

        Assertions.assertEquals("db_name_with_underscores", urlInfo.getDefaultDatabase().get());
    }
}