import ChangeLog from '../changelog/connector-cdc-yashandb.md';

# YashanDB-CDC

> YashanDB CDC Source Connector

> **⚠️ WARNING: This connector is not yet fully implemented and tested.**
> 
> Currently, only the snapshot phase (batch JDBC reading) works.
> The incremental phase (YStream CDC) is marked as TODO and is not functional.
> 
> For batch data reading, please use the [YashanDB JDBC Source](./YashanDB.md) instead.

## Description

Read YashanDB database change events (CDC) using snapshot and incremental capture.

**Snapshot Phase**: Reads full table data in parallel using JDBC chunked queries (Oracle-compatible SQL).

**Incremental Phase** (TODO): Uses YStream (YashanDB's native CDC API, analogous to Oracle XStream) to capture real-time DML and DDL changes from redo logs.

## Support Those Engines

> SeaTunnel Zeta<br/>

## Key Features

- [x] [batch](../../introduction/concepts/connector-v2-features.md)
- [x] [stream](../../introduction/concepts/connector-v2-features.md)
- [x] [exactly-once](../../introduction/concepts/connector-v2-features.md)
- [x] [column projection](../../introduction/concepts/connector-v2-features.md)
- [x] [parallelism](../../introduction/concepts/connector-v2-features.md)
- [x] [support user-defined split](../../introduction/concepts/connector-v2-features.md)

## Supported DataSource Info

| Datasource | Supported Versions | Driver | Maven |
|------------|-------------------|--------|-------|
| YashanDB   | 23.x              | com.yashandb.jdbc.Driver | Please refer to YashanDB official documentation |

## Database Dependency

> You need to ensure that the YashanDB JDBC driver jar package has been placed in directory `${SEATUNNEL_HOME}/lib/`.

## Data Type Mapping

See [YashanDB Source](./YashanDB.md) for the JDBC data type mapping.

## Source Options

| Name | Type | Required | Default | Description |
|------|------|----------|---------|-------------|
| hostname | String | Yes | - | Database hostname |
| port | Integer | Yes | - | Database port |
| username | String | Yes | - | Database username |
| password | String | Yes | - | Database password |
| database-names | List | Yes | - | List of database names to capture |
| schema-names | List | Yes | - | List of schema names to capture |
| table-names | List | No | - | List of table names to capture |
| table-pattern | String | No | - | Table name pattern (regex) |
| url | String | No | - | JDBC URL (overrides hostname and port) |
| startup.mode | Enum | No | initial | Startup mode: initial / latest / timestamp / specific |
| stop.mode | Enum | No | never | Stop mode: never / latest / timestamp / specific |
| use-select-count | Boolean | No | false | Use SELECT COUNT(*) for row count estimation |
| skip-analyze | Boolean | No | false | Skip ANALYZE TABLE when estimating row count |
| server-time-zone | String | No | UTC | Server time zone |
| connect.timeout | Long | No | 30000 | Connection timeout in milliseconds |
| connect.max-retries | Integer | No | 3 | Maximum connection retry attempts |
| connection.pool.size | Integer | No | 20 | JDBC connection pool size |
| schema-changes.enabled | Boolean | No | false | Enable schema evolution (DDL capture) |
| chunk-key.even-distribution.factor.upper-bound | Double | No | 100.0 | Chunk key distribution factor upper bound |
| chunk-key.even-distribution.factor.lower-bound | Double | No | 0.05 | Chunk key distribution factor lower bound |
| sample.sharding.threshold | Integer | No | 1000 | Sample sharding threshold for chunk splitting |

## YStream Prerequisites (Incremental Capture)

> **Note**: The incremental (YStream) capture part is marked as TODO and needs to be implemented separately.

### DBA Setup

```sql
-- Create YStream administrator user
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

-- Add table rules
BEGIN
  DBMS_YSTREAM_ADM.ADD_TABLE_RULES(
    server_name => 'SEATUNNEL_OUT',
    table_names => 'MYSCHEMA.*',
    operation => 'INSERT UPDATE DELETE'
  );
END;
/
```

## Task Example

### Simple (Snapshot Only)

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
    startup.mode = "initial"
  }
}
```

## Changelog

<ChangeLog />
