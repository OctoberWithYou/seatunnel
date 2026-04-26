# YashanDB Connector Design Document

## 1. Overview

YashanDB is a relational database compatible with Oracle syntax and protocols. This design covers two connectors:

| Connector | Type | Module | Description |
|-----------|------|--------|-------------|
| **YashanDB** | JDBC (Batch) | `connector-jdbc` | Batch read/write via JDBC |
| **YashanDB-CDC** | CDC (Incremental) | `connector-cdc-yashandb` (new) | Real-time CDC via **YStream** (analogous to Oracle XStream) |

YStream is YashanDB's native change data capture API — similar to Oracle XStream/LogMiner — providing a stream of DML/DDL change events from redo logs.

---

## 2. Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                    User Config (.conf)                        │
│  source { type = "Jdbc" driver="..." url="jdbc:yashandb:..."│
│  source { type = "YashanDB-CDC" ... }                        │
└──────────────────────────┬───────────────────────────────────┘
                           │
           ┌───────────────┴───────────────┐
           ▼                               ▼
┌──────────────────────┐     ┌──────────────────────────┐
│   connector-jdbc      │     │  connector-cdc-yashandb   │
│   (Batch Read/Write)  │     │  (CDC via YStream)        │
│                       │     │                           │
│  YashanDBDialect      │     │  YashanDBIncrementalSource│
│  YashanDBCatalog      │     │  YashanDBDialect (CDC)    │
│  TypeMapper/Converter │     │  YStreamOffset            │
│  RowConverter         │     │  YStreamFetchTask         │
└──────────┬───────────┘     └────────────┬──────────────┘
           │                              │
           ▼                              ▼
┌──────────────────────────────────────────────────────────────┐
│              YashanDB JDBC Driver (yashandb-jdbc)             │
│              YStream Client Library (yashandb-ystream)        │
└──────────────────────────────────────────────────────────────┘
```

---

## 3. Part 1: JDBC Batch Connector

### 3.1 Files to Create (under `connector-jdbc`)

All new files go inside existing package structure:

```
connector-jdbc/src/main/java/org/apache/seatunnel/connectors/seatunnel/jdbc/
├── internal/dialect/yashandb/
│   ├── YashanDBDialect.java              # SQL dialect (ORA_HASH, MERGE INTO, etc.)
│   ├── YashanDBDialectFactory.java       # @AutoService(JdbcDialectFactory.class)
│   ├── YashanDBTypeMapper.java           # JDBC type → SeaTunnel type
│   ├── YashanDBTypeConverter.java        # Type conversion logic
│   └── YashanDBJdbcRowConverter.java     # Row-level conversion
├── catalog/yashandb/
│   ├── YashanDBCatalog.java              # Metadata queries
│   ├── YashanDBCatalogFactory.java       # @AutoService(Factory.class)
│   ├── YashanDBCreateTableSqlBuilder.java # DDL generation
│   ├── YashanDBDataTypeConvertor.java     # Type mapping for catalog
│   └── YashanDBURLParser.java            # jdbc:yashanb:thin:@//host:port/db
```

### 3.2 Key Class Details

#### 3.2.1 YashanDBDialectFactory

```java
@AutoService(JdbcDialectFactory.class)
public class YashanDBDialectFactory implements JdbcDialectFactory {
    @Override
    public String dialectFactoryName() {
        return DatabaseIdentifier.YASHANDB;  // "YashanDB"
    }

    @Override
    public boolean acceptsURL(String url) {
        return url.startsWith("jdbc:yashanb:");  // or jdbc:yashandb:
    }

    @Override
    public JdbcDialect create() { return new YashanDBDialect(); }
}
```

#### 3.2.2 YashanDBDialect

- Implements `JdbcDialect`
- `dialectName()` → `DatabaseIdentifier.YASHANDB`
- `quoteIdentifier()` → double quotes `"identifier"` (Oracle-compatible)
- `hashModForField()` → `ORA_HASH(field, MOD_VALUE)` or YashanDB equivalent
- `getUpsertStatement()` → `MERGE INTO ... USING DUAL ON (...) WHEN MATCHED THEN UPDATE ... WHEN NOT MATCHED THEN INSERT ...`
- `getRowConverter()` → `new YashanDBJdbcRowConverter()`
- `getJdbcDialectTypeMapper()` → `new YashanDBTypeMapper()`
- `getTypeConverter()` → `new YashanDBTypeConverter()`

#### 3.2.3 YashanDBTypeMapper

Maps YashanDB types to SeaTunnel `SeaTunnelDataType`:
- `NUMBER(p,s)` → `DecimalType` / `BigIntType` / `IntType` depending on precision/scale
- `VARCHAR2(n)` → `StringType`
- `CHAR(n)` → `StringType`
- `DATE` → `LocalTimeType`
- `TIMESTAMP` → `TimestampType`
- `CLOB` → `StringType`
- `BLOB` → `BytesType`
- `RAW(n)` → `BytesType`
- etc.

#### 3.2.4 YashanDBCatalog

- Extends `AbstractJdbcCatalog`
- Queries `ALL_TABLES`, `ALL_TAB_COLUMNS`, `ALL_CONSTRAINTS`, `ALL_CONS_COLUMNS`, `ALL_INDEXES`, `ALL_IND_COLUMNS`
- Schema-qualified table paths: `SCHEMA.TABLE`

#### 3.2.5 YashanDBCatalogFactory

```java
@AutoService(Factory.class)
public class YashanDBCatalogFactory implements CatalogFactory {
    @Override
    public String factoryIdentifier() {
        return DatabaseIdentifier.YASHANDB;
    }
    // ...
}
```

### 3.3 Files to Modify (connector-jdbc)

| File | Change |
|------|--------|
| `DatabaseIdentifier.java` | Add `public static final String YASHANDB = "YashanDB";` |
| `connector-jdbc/pom.xml` | Add YashanDB JDBC driver dependency |

---

## 4. Part 2: CDC Connector (YashanDB-CDC via YStream)

### 4.1 New Module: `connector-cdc-yashandb`

```
connector-cdc/connector-cdc-yashandb/
├── pom.xml
└── src/main/java/org/apache/seatunnel/connectors/seatunnel/cdc/yashandb/
    ├── config/
    │   ├── YashanDBSourceConfig.java          # CDC source config POJO
    │   └── YashanDBSourceConfigFactory.java    # Config factory
    ├── source/
    │   ├── YashanDBDialect.java                # CDC DataSourceDialect impl
    │   ├── YashanDBIncrementalSource.java      # Main CDC source (extends IncrementalSource)
    │   ├── YashanDBIncrementalSourceFactory.java # @AutoService(Factory.class), identifier="YashanDB-CDC"
    │   ├── YashanDBIncrementalSourceOptions.java # Connector-specific options
    │   ├── YashanDBSchemaChangeResolver.java   # Schema change handling
    │   ├── enumerator/
    │   │   └── YashanDBChunkSplitter.java      # Table chunk splitting for parallel snapshot
    │   ├── offset/
    │   │   ├── YStreamOffset.java              # YStream offset (SCN-based, like RedoLogOffset)
    │   │   └── YStreamOffsetFactory.java       # Offset factory
    │   └── reader/fetch/
    │       ├── YashanDBSourceFetchTaskContext.java
    │       ├── ystream/
    │       │   ├── YStreamFetchTask.java       # YStream incremental fetch
    │       │   └── YStreamEventProcessor.java   # YStream event → SeaTunnel record
    │       └── scan/
    │           ├── YashanDBSnapshotFetchTask.java
    │           ├── YashanDBSnapshotSplitReadTask.java
    │           └── SnapshotSplitChangeEventSourceContext.java
    └── utils/
        ├── YashanDBConnectionUtils.java
        ├── YashanDBSchema.java
        ├── YashanDBTypeUtils.java
        └── YashanDBUtils.java
```

### 4.2 YStream Integration Details

YStream is analogous to Oracle XStream. The integration approach:

```
                         ┌─────────────────────────┐
                         │   YashanDB Server         │
                         │   ┌─────────────────────┐│
                         │   │  Redo Logs           ││
                         │   │    ↓                 ││
                         │   │  YStream Outbound    ││
                         │   │  Server              ││
                         │   └──────────┬──────────┘│
                         └──────────────┼───────────┘
                                        │ TCP (YStream Protocol)
                         ┌──────────────┼───────────┐
                         │   YStream Client Library  │
                         │   (yashandb-ystream.jar)  │
                         └──────────────┬───────────┘
                                        │
                         ┌──────────────┼───────────┐
                         │   YStreamFetchTask        │
                         │   ┌─────────────────────┐ │
                         │   │ 1. Connect to YStream│ │
                         │   │ 2. Subscribe to LCR  │ │
                         │   │ 3. Receive LCR events│ │
                         │   │ 4. Convert to        │ │
                         │   │    SeaTunnel records │ │
                         │   └─────────────────────┘ │
                         └──────────────────────────┘
```

#### YStreamFetchTask (pseudocode)

```java
public class YStreamFetchTask {
    // 1. Open YStream client connection
    // 2. Attach to outbound server with SCN offset
    // 3. Loop: receive Logical Change Records (LCRs)
    //    - RowLCR → INSERT/UPDATE/DELETE SeaTunnel records
    //    - DDLLCR → SchemaChangeEvent
    // 4. Commit SCN position to checkpoint
    // 5. Handle reconnect with last committed SCN
}
```

#### YStreamOffset

```java
public class YStreamOffset extends ChangeStreamOffset {
    // Uses SCN (System Change Number) as offset — same concept as Oracle's RedoLog SCN
    // Format: {"scn": "1234567890", "commit_scn": "1234567890"}
}
```

### 4.3 Key CDC Classes

#### 4.3.1 YashanDBIncrementalSourceFactory

```java
@AutoService(Factory.class)
public class YashanDBIncrementalSourceFactory extends BaseChangeStreamTableSourceFactory {
    @Override
    public String factoryIdentifier() {
        return "YashanDB-CDC";  // Used in config: type = "YashanDB-CDC"
    }

    @Override
    public OptionRule optionRule() {
        // Required: username, password, hostname, port
        // Optional: database-names, schema-names, table-names, table-pattern
        //           startup.mode, stop.mode, use-select-count, skip-analyze,
        //           server-time-zone, connect.timeout, connection.pool.size,
        //           chunk-key.even-distribution.factor.*, sample.sharding.threshold,
        //           schema-changes.enabled
    }
}
```

#### 4.3.2 YashanDBDialect (CDC)

```java
public class YashanDBDialect implements JdbcDataSourceDialect {
    // Implements table discovery, schema queries, chunk splitting
    // Similar to OracleDialect in CDC module
}
```

#### 4.3.3 YashanDBIncrementalSource

```java
public class YashanDBIncrementalSource<T> extends IncrementalSource<T, JdbcSourceConfig>
        implements SupportParallelism, SupportSchemaEvolution {

    static final String IDENTIFIER = "YashanDB-CDC";

    @Override
    public OffsetFactory createOffsetFactory(ReadonlyConfig config) {
        return new YStreamOffsetFactory(...);
    }

    @Override
    public Optional<String> driverName() {
        return Optional.of("com.yashandb.jdbc.Driver");
    }
}
```

---

## 5. Complete File Manifest

### 5.1 New Files

| # | File Path | Description |
|---|-----------|-------------|
| **JDBC Dialect (connector-jdbc)** | | |
| 1 | `connector-jdbc/src/main/java/.../jdbc/internal/dialect/yashandb/YashanDBDialect.java` | SQL Dialect |
| 2 | `connector-jdbc/src/main/java/.../jdbc/internal/dialect/yashandb/YashanDBDialectFactory.java` | Dialect SPI |
| 3 | `connector-jdbc/src/main/java/.../jdbc/internal/dialect/yashandb/YashanDBTypeMapper.java` | Type mapping |
| 4 | `connector-jdbc/src/main/java/.../jdbc/internal/dialect/yashandb/YashanDBTypeConverter.java` | Type converter |
| 5 | `connector-jdbc/src/main/java/.../jdbc/internal/dialect/yashandb/YashanDBJdbcRowConverter.java` | Row converter |
| **JDBC Catalog (connector-jdbc)** | | |
| 6 | `connector-jdbc/src/main/java/.../jdbc/catalog/yashandb/YashanDBCatalog.java` | Catalog |
| 7 | `connector-jdbc/src/main/java/.../jdbc/catalog/yashandb/YashanDBCatalogFactory.java` | Catalog SPI |
| 8 | `connector-jdbc/src/main/java/.../jdbc/catalog/yashandb/YashanDBCreateTableSqlBuilder.java` | DDL builder |
| 9 | `connector-jdbc/src/main/java/.../jdbc/catalog/yashandb/YashanDBDataTypeConvertor.java` | Type convertor |
| 10 | `connector-jdbc/src/main/java/.../jdbc/catalog/yashandb/YashanDBURLParser.java` | URL parser |
| **JDBC Unit Tests (connector-jdbc)** | | |
| 11 | `connector-jdbc/src/test/java/.../jdbc/internal/dialect/yashandb/YashanDBTypeConverterTest.java` | Type conversion test |
| 12 | `connector-jdbc/src/test/java/.../jdbc/internal/dialect/yashandb/YashanDBCreateTableSqlBuilderTest.java` | DDL test |
| 13 | `connector-jdbc/src/test/java/.../jdbc/catalog/yashandb/YashanDBCatalogTest.java` | Catalog test |
| **CDC Module POM** | | |
| 14 | `connector-cdc/connector-cdc-yashandb/pom.xml` | Module POM |
| **CDC Config** | | |
| 15 | `connector-cdc-yashandb/src/main/java/.../cdc/yashandb/config/YashanDBSourceConfig.java` | Source config |
| 16 | `connector-cdc-yashandb/src/main/java/.../cdc/yashandb/config/YashanDBSourceConfigFactory.java` | Config factory |
| **CDC Source** | | |
| 17 | `connector-cdc-yashandb/src/main/java/.../cdc/yashandb/source/YashanDBDialect.java` | CDC dialect |
| 18 | `connector-cdc-yashandb/src/main/java/.../cdc/yashandb/source/YashanDBIncrementalSource.java` | CDC source |
| 19 | `connector-cdc-yashandb/src/main/java/.../cdc/yashandb/source/YashanDBIncrementalSourceFactory.java` | CDC SPI |
| 20 | `connector-cdc-yashandb/src/main/java/.../cdc/yashandb/source/YashanDBIncrementalSourceOptions.java` | CDC options |
| 21 | `connector-cdc-yashandb/src/main/java/.../cdc/yashandb/source/YashanDBSchemaChangeResolver.java` | Schema evolution |
| **CDC Enumerator** | | |
| 22 | `connector-cdc-yashandb/src/main/java/.../cdc/yashandb/source/enumerator/YashanDBChunkSplitter.java` | Table splitter |
| **CDC Offset** | | |
| 23 | `connector-cdc-yashandb/src/main/java/.../cdc/yashandb/source/offset/YStreamOffset.java` | YStream offset |
| 24 | `connector-cdc-yashandb/src/main/java/.../cdc/yashandb/source/offset/YStreamOffsetFactory.java` | Offset factory |
| **CDC Reader: YStream Fetch** | | |
| 25 | `connector-cdc-yashandb/src/main/java/.../cdc/yashandb/source/reader/fetch/YashanDBSourceFetchTaskContext.java` | Fetch context |
| 26 | `connector-cdc-yashandb/src/main/java/.../cdc/yashandb/source/reader/fetch/ystream/YStreamFetchTask.java` | YStream incremental |
| 27 | `connector-cdc-yashandb/src/main/java/.../cdc/yashandb/source/reader/fetch/ystream/YStreamEventProcessor.java` | Event processing |
| **CDC Reader: Snapshot** | | |
| 28 | `connector-cdc-yashandb/src/main/java/.../cdc/yashandb/source/reader/fetch/scan/YashanDBSnapshotFetchTask.java` | Snapshot fetch |
| 29 | `connector-cdc-yashandb/src/main/java/.../cdc/yashandb/source/reader/fetch/scan/YashanDBSnapshotSplitReadTask.java` | Snapshot read |
| 30 | `connector-cdc-yashandb/src/main/java/.../cdc/yashandb/source/reader/fetch/scan/SnapshotSplitChangeEventSourceContext.java` | Context |
| **CDC Utils** | | |
| 31 | `connector-cdc-yashandb/src/main/java/.../cdc/yashandb/utils/YashanDBConnectionUtils.java` | Connection util |
| 32 | `connector-cdc-yashandb/src/main/java/.../cdc/yashandb/utils/YashanDBSchema.java` | Schema util |
| 33 | `connector-cdc-yashandb/src/main/java/.../cdc/yashandb/utils/YashanDBTypeUtils.java` | Type util |
| 34 | `connector-cdc-yashandb/src/main/java/.../cdc/yashandb/utils/YashanDBUtils.java` | General util |
| **CDC Unit Tests** | | |
| 35 | `connector-cdc-yashandb/src/test/java/.../cdc/yashandb/source/YashanDBIncrementalSourceFactoryTest.java` | Factory test |
| 36 | `connector-cdc-yashandb/src/test/java/.../cdc/yashandb/utils/YashanDBUtilsTest.java` | Utils test |
| **Documentation (English)** | | |
| 37 | `docs/en/connectors/source/YashanDB.md` | JDBC Source doc |
| 38 | `docs/en/connectors/sink/YashanDB.md` | JDBC Sink doc |
| 39 | `docs/en/connectors/source/YashanDB-CDC.md` | CDC Source doc |
| 40 | `docs/en/connectors/changelog/connector-cdc-yashandb.md` | CDC changelog |
| **Documentation (Chinese)** | | |
| 41 | `docs/zh/connectors/source/YashanDB.md` | JDBC Source doc (zh) |
| 42 | `docs/zh/connectors/sink/YashanDB.md` | JDBC Sink doc (zh) |
| 43 | `docs/zh/connectors/source/YashanDB-CDC.md` | CDC Source doc (zh) |
| 44 | `docs/zh/connectors/changelog/connector-cdc-yashandb.md` | CDC changelog (zh) |
| **Documentation Icons** | | |
| 45 | `docs/images/icons/YashanDB.svg` | YashanDB icon |
| 46 | `docs/images/icons/YashanDB CDC.svg` | YashanDB CDC icon |

### 5.2 Files to Modify

| # | File Path | Change |
|---|-----------|--------|
| M1 | `connector-jdbc/src/main/java/.../jdbc/internal/dialect/DatabaseIdentifier.java` | Add `YASHANDB = "YashanDB"` |
| M2 | `connector-jdbc/pom.xml` | Add YashanDB JDBC driver dependency |
| M3 | `connector-cdc/pom.xml` | Add `<module>connector-cdc-yashandb</module>` |
| M4 | `seatunnel-dist/pom.xml` | Add `connector-cdc-yashandb`, `yashandb-jdbc` driver, `yashandb-ystream` client dependencies |
| M5 | `seatunnel-dist/src/main/assembly/assembly-bin-ci.xml` | Include `com.yashandb:yashandb-jdbc:jar` in `<dependencySets>` |
| M6 | `docs/en/connectors/changelog/connector-jdbc.md` | Add YashanDB support entry |
| M7 | `docs/en/connectors/source/Jdbc.md` | Add YashanDB to supported databases list |
| M8 | `docs/en/connectors/sink/Jdbc.md` | Add YashanDB to supported databases list |
| M9 | `docs/zh/connectors/changelog/connector-jdbc.md` | Add YashanDB support entry (zh) |
| M10 | `docs/zh/connectors/source/Jdbc.md` | Add YashanDB to supported databases list (zh) |
| M11 | `docs/zh/connectors/sink/Jdbc.md` | Add YashanDB to supported databases list (zh) |
| M12 | `docs/en/introduction/configuration/sink-options-placeholders.md` | Add YashanDB placeholder docs if applicable |
| M13 | `docs/en/faq.md` | Add YashanDB FAQ if applicable |

---

## 6. Dependency Management

### 6.1 Maven Dependencies

**connector-jdbc/pom.xml:**
```xml
<properties>
    <yashandb.version>x.x.x</yashandb.version>
</properties>
<dependency>
    <groupId>com.yashandb</groupId>
    <artifactId>yashandb-jdbc</artifactId>
    <version>${yashandb.version}</version>
    <scope>provided</scope>
</dependency>
```

**connector-cdc-yashandb/pom.xml:**
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.apache.seatunnel</groupId>
            <artifactId>connector-cdc-base</artifactId>
            <version>${project.version}</version>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>org.apache.seatunnel</groupId>
            <artifactId>connector-jdbc</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.yashandb</groupId>
            <artifactId>yashandb-jdbc</artifactId>
            <version>${yashandb.version}</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>com.yashandb</groupId>
            <artifactId>yashandb-ystream</artifactId>
            <version>${yashandb.version}</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 6.2 Distribution Assembly

**assembly-bin-ci.xml:** Add YashanDB JDBC driver to `<dependencySets>`:
```xml
<include>com.yashandb:yashandb-jdbc:jar</include>
<include>com.yashandb:yashandb-ystream:jar</include>
```

**seatunnel-dist/pom.xml:** Add to `<dependencyManagement>`:
```xml
<dependency>
    <groupId>org.apache.seatunnel</groupId>
    <artifactId>connector-cdc-yashandb</artifactId>
    <version>${project.version}</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>com.yashandb</groupId>
    <artifactId>yashandb-jdbc</artifactId>
    <version>${yashandb.version}</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>com.yashandb</groupId>
    <artifactId>yashandb-ystream</artifactId>
    <version>${yashandb.version}</version>
    <scope>provided</scope>
</dependency>
```

---

## 7. Configuration Examples

### 7.1 JDBC Batch Source (YashanDB)

```hocon
source {
  Jdbc {
    driver = "com.yashandb.jdbc.Driver"
    url = "jdbc:yashanb:thin:@//localhost:1688/ORCL"
    username = "yashandb"
    password = "password"
    query = "SELECT * FROM myschema.my_table"
  }
}
```

### 7.2 JDBC Batch Sink (YashanDB)

```hocon
sink {
  Jdbc {
    driver = "com.yashandb.jdbc.Driver"
    url = "jdbc:yashanb:thin:@//localhost:1688/ORCL"
    username = "yashandb"
    password = "password"
    database = "ORCL"
    table = "myschema.my_table"
  }
}
```

### 7.3 CDC Source (YashanDB-CDC with YStream)

```hocon
source {
  YashanDB-CDC {
    hostname = "localhost"
    port = 1688
    username = "yashandb"
    password = "password"
    database-names = ["ORCL"]
    schema-names = ["MYSCHEMA"]
    table-names = ["MYSCHEMA.MY_TABLE"]

    startup.mode = "initial"   # initial / latest / timestamp / specific
    stop.mode = "never"        # never / latest / timestamp / specific

    # YStream-specific options (if needed)
    # ystream.server-name = "SEATUNNEL_OUT"
    # ystream.connect-timeout = 30
    # ystream.buffer-size = 1048576

    schema-changes.enabled = true
    server-time-zone = "Asia/Shanghai"
  }
}
```

---

## 8. YStream CDC Protocol Details

### 8.1 Key Concepts

| YashanDB Concept | Oracle Equivalent | Description |
|------------------|-------------------|-------------|
| YStream | XStream | Native CDC API |
| LCR (Logical Change Record) | LCR | Row-level change record |
| Outbound Server | Outbound Server | Server-side YStream process |
| SCN (System Change Number) | SCN | Monotonic change identifier |
| Redo Log | Redo Log | Transaction log (change source) |
| Position | Position | Offset pointer (SCN-based) |

### 8.2 YStream Outbound Setup (DBA Prerequisites)

```sql
-- Create YStream administrator
CREATE USER ystream_admin IDENTIFIED BY password;
GRANT CONNECT, RESOURCE, DBA TO ystream_admin;

-- Create outbound server
BEGIN
  DBMS_YSTREAM_ADM.CREATE_OUTBOUND(
    server_name => 'SEATUNNEL_OUT',
    source_database => 'ORCL'
  );
END;
/

-- Add table rules (which tables to capture)
BEGIN
  DBMS_YSTREAM_ADM.ADD_TABLE_RULES(
    server_name => 'SEATUNNEL_OUT',
    table_names => 'MYSCHEMA.*',
    operation => 'INSERT UPDATE DELETE'
  );
END;
/
```

### 8.3 Snapshot + Incremental Flow

```
┌─────────────────────────────────────────────────────────┐
│ 1. SNAPSHOT PHASE                                       │
│    - Split tables into chunks by primary key / ORA_HASH │
│    - Read each chunk with SELECT (snapshot isolation)   │
│    - Record current SCN as low watermark                │
│    - After all chunks read, record high water SCN       │
├─────────────────────────────────────────────────────────┤
│ 2. INCREMENTAL PHASE (YStream)                          │
│    - Connect to YStream outbound server                 │
│    - Start from low watermark SCN                       │
│    - Receive LCR stream                                 │
│    - Filter LCRs between low/high watermark for dedup   │
│    - Output change records to downstream                │
│    - Periodically commit SCN to checkpoint              │
└─────────────────────────────────────────────────────────┘
```

---

## 9. Implementation Order

Recommended implementation order (each step builds on the previous):

| Phase | Task | Est. Effort |
|-------|------|-------------|
| **Phase 1** | YashanDB JDBC Dialect (batch read/write) | Small |
| | - DatabaseIdentifier constant | |
| | - YashanDBDialect + Factory | |
| | - YashanDBTypeMapper/Converter | |
| **Phase 2** | YashanDB Catalog (metadata) | Small |
| | - YashanDBCatalog + Factory | |
| | - YashanDBCreateTableSqlBuilder | |
| | - YashanDBDataTypeConvertor | |
| **Phase 3** | Unit tests for JDBC | Small |
| **Phase 4** | Documentation (JDBC Source/Sink) | Small |
| **Phase 5** | YashanDB-CDC Module (incremental) | Medium-Large |
| | - Module scaffolding + POM | |
| | - CDC dialect, config, options | |
| | - YStream offset, fetch task | |
| | - Snapshot fetch | |
| | - Schema change resolver | |
| **Phase 6** | CDC Unit tests | Medium |
| **Phase 7** | E2E tests (if test container available) | Medium |
| **Phase 8** | Documentation (CDC, changelog) | Small |
| **Phase 9** | Distribution assembly updates | Small |

---

## 10. Risks and Considerations

1. **YashanDB JDBC Driver Availability**: Verify the JDBC driver artifact coordinates (`com.yashandb:yashandb-jdbc` is a placeholder — use actual GAV). If the driver is not in Maven Central, it must be installed to a private repository or local `.m2`.

2. **YStream Client Library**: The YStream client library (`yashandb-ystream`) may have different packaging than assumed. If it bundles the JDBC driver, exclude transitive dependencies as needed.

3. **SQL Compatibility**: While YashanDB is Oracle-compatible, actual SQL dialect differences should be verified:
   - `ORA_HASH` availability
   - `MERGE INTO ... USING DUAL` syntax
   - System catalog views (`ALL_TABLES`, `ALL_TAB_COLUMNS`, etc.)
   - Data type names (`VARCHAR2`, `NUMBER`, `CLOB`, etc.)

4. **YStream vs Oracle XStream/LogMiner**: If YStream API differs significantly from Oracle's XStream, the `YStreamFetchTask` implementation will diverge more from `OracleRedoLogFetchTask`. The Oracle CDC module uses Debezium's LogMiner adapter — YashanDB-CDC likely cannot reuse Debezium at all and must implement the YStream client protocol directly.

5. **License Compatibility**: YashanDB JDBC driver and YStream client licenses must be compatible with Apache 2.0 for inclusion in the SeaTunnel distribution.

6. **Oracle Compatibility Mode**: If YashanDB supports an "Oracle compatibility mode", users might try to reuse the existing `Oracle` dialect. The `acceptsURL()` matching must ensure `jdbc:oracle:thin:` vs `jdbc:yashanb:` URLs are unambiguous.
