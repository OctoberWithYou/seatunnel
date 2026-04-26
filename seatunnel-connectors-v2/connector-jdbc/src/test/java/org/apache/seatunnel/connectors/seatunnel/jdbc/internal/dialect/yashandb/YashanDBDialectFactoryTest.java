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
import org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.JdbcDialect;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Test for {@link YashanDBDialectFactory}. */
public class YashanDBDialectFactoryTest {

    private final YashanDBDialectFactory factory = new YashanDBDialectFactory();

    @Test
    public void testDialectFactoryName() {
        Assertions.assertEquals("YashanDB", factory.dialectFactoryName());
    }

    @Test
    public void testAcceptsURLStandard() {
        Assertions.assertTrue(factory.acceptsURL("jdbc:yasdb://localhost:1688/mydb"));
    }

    @Test
    public void testAcceptsURLWithIP() {
        Assertions.assertTrue(factory.acceptsURL("jdbc:yasdb://192.168.1.11:1688/ya"));
    }

    @Test
    public void testAcceptsURLWithParameters() {
        Assertions.assertTrue(factory.acceptsURL("jdbc:yasdb://host:1688/db?useSSL=true"));
    }

    @Test
    public void testAcceptsURLWithHostname() {
        Assertions.assertTrue(factory.acceptsURL("jdbc:yasdb://db.example.com:1688/prod"));
    }

    @Test
    public void testDoesNotAcceptOracleThinURL() {
        // Oracle-style thin URL is NOT supported
        Assertions.assertFalse(factory.acceptsURL("jdbc:yashandb:thin:@//localhost:1688/mydb"));
    }

    @Test
    public void testDoesNotAcceptOracleSIDURL() {
        Assertions.assertFalse(factory.acceptsURL("jdbc:yashandb:thin:@localhost:1688:mydb"));
    }

    @Test
    public void testDoesNotAcceptMySQLURL() {
        Assertions.assertFalse(factory.acceptsURL("jdbc:mysql://localhost:3306/mydb"));
    }

    @Test
    public void testDoesNotAcceptPostgreSQLURL() {
        Assertions.assertFalse(factory.acceptsURL("jdbc:postgresql://localhost:5432/mydb"));
    }

    @Test
    public void testDoesNotAcceptOracleURL() {
        Assertions.assertFalse(factory.acceptsURL("jdbc:oracle:thin:@localhost:1521:orcl"));
    }

    @Test
    public void testCreateDefaultDialect() {
        JdbcDialect dialect = factory.create();
        Assertions.assertNotNull(dialect);
        Assertions.assertTrue(dialect instanceof YashanDBDialect);
        Assertions.assertEquals("YashanDB", dialect.dialectName());
    }

    @Test
    public void testCreateDialectWithFieldIde() {
        JdbcDialect dialect = factory.create("", "uppercase");
        Assertions.assertNotNull(dialect);
        Assertions.assertTrue(dialect instanceof YashanDBDialect);
    }

    @Test
    public void testCreateDialectWithFieldIdeLowercase() {
        JdbcDialect dialect = factory.create("", "lowercase");
        Assertions.assertNotNull(dialect);
        Assertions.assertTrue(dialect instanceof YashanDBDialect);
    }

    @Test
    public void testCreateDialectWithFieldIdeOriginal() {
        JdbcDialect dialect = factory.create("", "original");
        Assertions.assertNotNull(dialect);
        Assertions.assertTrue(dialect instanceof YashanDBDialect);
    }

    @Test
    public void testCreateDialectWithHandleBlobAsString() {
        JdbcConnectionConfig config = new JdbcConnectionConfig();
        config.setHandleBlobAsString(true);

        JdbcDialect dialect = factory.create("", "original", config);
        Assertions.assertNotNull(dialect);
        Assertions.assertTrue(dialect instanceof YashanDBDialect);
    }

    @Test
    public void testCreateDialectWithHandleBlobAsBytes() {
        JdbcConnectionConfig config = new JdbcConnectionConfig();
        config.setHandleBlobAsString(false);

        JdbcDialect dialect = factory.create("", "original", config);
        Assertions.assertNotNull(dialect);
        Assertions.assertTrue(dialect instanceof YashanDBDialect);
    }

    @Test
    public void testCreateDialectWithNullConfig() {
        JdbcDialect dialect = factory.create("", "original", null);
        Assertions.assertNotNull(dialect);
        Assertions.assertTrue(dialect instanceof YashanDBDialect);
    }

    @Test
    public void testCreateDialectChain() {
        // Test the chain of create methods
        JdbcDialect dialect1 = factory.create();
        JdbcDialect dialect2 = factory.create("", "uppercase");
        JdbcDialect dialect3 = factory.create("", "lowercase", null);

        Assertions.assertNotNull(dialect1);
        Assertions.assertNotNull(dialect2);
        Assertions.assertNotNull(dialect3);
    }
}