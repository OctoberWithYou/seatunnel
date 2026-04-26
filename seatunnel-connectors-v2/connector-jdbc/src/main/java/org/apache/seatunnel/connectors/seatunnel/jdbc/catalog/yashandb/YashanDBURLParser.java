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

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * URL parser for YashanDB JDBC connection strings.
 *
 * <p>This parser extracts connection parameters from YashanDB JDBC URLs.
 * It supports the standard YashanDB JDBC URL format and provides
 * structured access to host, port, database, and query parameters.
 *
 * <p><b>Supported URL Format:</b>
 * <pre>
 * jdbc:yasdb://host:port/database[?parameters]
 * </pre>
 *
 * <p><b>Examples:</b>
 * <ul>
 *   <li><code>jdbc:yasdb://192.168.1.11:1688/ya</code></li>
 *   <li><code>jdbc:yasdb://localhost:1688/mydb?useSSL=true</code></li>
 *   <li><code>jdbc:yasdb://db.example.com:1688/prod</code></li>
 * </ul>
 *
 * <p><b>Not Supported:</b>
 * <ul>
 *   <li><code>jdbc:yashandb:thin:@//host:port/database</code> - Oracle-style thin URL</li>
 *   <li><code>jdbc:yashandb:thin:@host:port:database</code> - Oracle-style SID URL</li>
 * </ul>
 *
 * <p><b>URL Components:</b>
 * <ul>
 *   <li><b>host:</b> Database server hostname or IP address</li>
 *   <li><b>port:</b> Database server port (default YashanDB port is 1688)</li>
 *   <li><b>database:</b> Database/service name to connect to</li>
 *   <li><b>suffix:</b> Optional query parameters starting with "?"</li>
 * </ul>
 *
 * <p><b>Error Handling:</b>
 * If the URL does not match the expected format, the parser returns
 * a UrlInfo with the original URL and a default database name "temp".
 * This allows graceful degradation rather than throwing exceptions.
 *
 * @see JdbcUrlUtil.UrlInfo
 * @see YashanDBCatalogFactory
 */
public class YashanDBURLParser {

    /**
     * Regular expression pattern for parsing YashanDB JDBC URLs.
     *
     * <p>The pattern captures the following named groups:
     * <ul>
     *   <li><b>host:</b> The hostname part (any characters except colon)</li>
     *   <li><b>port:</b> The port number (digits only)</li>
     *   <li><b>database:</b> The database name (any characters, non-greedy)</li>
     *   <li><b>suffix:</b> Optional query parameters starting with "?"</li>
     * </ul>
     *
     * <p><b>Pattern Breakdown:</b>
     * <pre>
     * ^jdbc:yasdb://                    - URL prefix (required)
     * (?&lt;host&gt;[^:]+)                    - Hostname (named capture group)
     * :(?&lt;port&gt;\d+)                     - Port number (named capture group)
     * /(?&lt;database&gt;.+?)                 - Database name (named capture, non-greedy)
     * (?&lt;suffix&gt;\?.*)?                  - Optional query parameters (named capture)
     * $                                 - End of string
     * </pre>
     */
    // Support jdbc:yasdb://host:port/database format
    private static final Pattern YASHANDB_URL_PATTERN =
            Pattern.compile(
                    "^jdbc:yasdb://(?<host>[^:]+):(?<port>\\d+)/(?<database>.+?)(?<suffix>\\?.*)?$");

    /**
     * Parses a YashanDB JDBC URL and extracts connection parameters.
     *
     * <p>This method attempts to match the URL against the YashanDB URL pattern.
     * If successful, it extracts host, port, database, and optional query parameters.
     * If the URL does not match, it returns a fallback UrlInfo with default values.
     *
     * <p><b>Successful Parse Example:</b>
     * <pre>
     * Input:  "jdbc:yasdb://192.168.1.11:1688/ya?useSSL=true"
     * Output: UrlInfo {
     *   url: "jdbc:yasdb://192.168.1.11:1688/ya?useSSL=true",
     *   urlWithoutDatabase: "jdbc:yasdb://192.168.1.11:1688/",
     *   host: "192.168.1.11",
     *   port: 1688,
     *   defaultDatabase: "ya",
     *   suffix: "?useSSL=true"
     * }
     * </pre>
     *
     * <p><b>Failed Parse Example:</b>
     * <pre>
     * Input:  "jdbc:oracle:thin:@localhost:1521:orcl"
     * Output: UrlInfo {
     *   url: "jdbc:oracle:thin:@localhost:1521:orcl",
     *   urlWithoutDatabase: "jdbc:oracle:thin:@localhost:1521:orcl",
     *   host: null,
     *   port: null,
     *   defaultDatabase: "temp",
     *   suffix: null
     * }
     * </pre>
     *
     * @param url the JDBC URL to parse
     * @return a UrlInfo object containing parsed connection parameters,
     *         or a fallback UrlInfo if parsing fails
     */
    public static JdbcUrlUtil.UrlInfo parse(String url) {
        Matcher matcher = YASHANDB_URL_PATTERN.matcher(url);
        if (matcher.find()) {
            String host = matcher.group("host");
            Integer port = Integer.valueOf(matcher.group("port"));
            String database = matcher.group("database");
            String suffix = Optional.ofNullable(matcher.group("suffix")).orElse("");
            String urlWithoutDatabase = "jdbc:yasdb://" + host + ":" + port + "/";
            return new JdbcUrlUtil.UrlInfo(url, urlWithoutDatabase, host, port, database, suffix);
        }
        // Fallback for non-matching URLs - use "temp" as default database
        return new JdbcUrlUtil.UrlInfo(url, url, null, null, "temp", null);
    }
}