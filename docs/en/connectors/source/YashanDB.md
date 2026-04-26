import ChangeLog from '../changelog/connector-jdbc.md';

# YashanDB

> JDBC YashanDB Source Connector

## Description

Read external data source data through JDBC from YashanDB.

YashanDB is a relational database compatible with Oracle syntax and protocols. This connector uses the YashanDB JDBC driver for batch data reading.

## Support Those Engines

> Spark<br/>
> Flink<br/>
> SeaTunnel Zeta<br/>

## Key Features

- [x] [batch](../../introduction/concepts/connector-v2-features.md)
- [ ] [stream](../../introduction/concepts/connector-v2-features.md)
- [x] [exactly-once](../../introduction/concepts/connector-v2-features.md)
- [x] [column projection](../../introduction/concepts/connector-v2-features.md)
- [x] [parallelism](../../introduction/concepts/connector-v2-features.md)
- [x] [support user-defined split](../../introduction/concepts/connector-v2-features.md)

> supports query SQL and can achieve projection effect.

## Supported DataSource Info

| Datasource | Supported Versions | Driver | Url | Maven |
|------------|-------------------|--------|-----|-------|
| YashanDB   | 23.x              | com.yashandb.jdbc.Driver | jdbc:yasdb://host:port/database | Please refer to YashanDB official documentation |

## Database Dependency

### For Spark/Flink Engine

> 1. You need to ensure that the YashanDB JDBC driver jar package has been placed in directory `${SEATUNNEL_HOME}/plugins/`.

### For SeaTunnel Zeta Engine

> 1. You need to ensure that the YashanDB JDBC driver jar package has been placed in directory `${SEATUNNEL_HOME}/lib/`.

## Data Type Mapping

| YashanDB Data Type | SeaTunnel Data Type |
|-------------------|---------------------|
| TINYINT<br/>SMALLINT | SMALLINT |
| INT<br/>INTEGER | INT |
| BIGINT | BIGINT |
| NUMBER(precision <= 1, scale == 0) | BOOLEAN |
| NUMBER(1 < precision <= 9, scale == 0) | INT |
| NUMBER(9 < precision <= 18, scale == 0) | BIGINT |
| NUMBER(18 < precision, scale == 0) | DECIMAL(38, 0) |
| NUMBER(scale > 0) | DECIMAL(precision, scale) |
| BINARY_FLOAT<br/>REAL | FLOAT |
| BINARY_DOUBLE<br/>DOUBLE | DOUBLE |
| BIT | BOOLEAN |
| CHAR<br/>VARCHAR | STRING |
| NCHAR<br/>NVARCHAR | STRING |
| CLOB<br/>NCLOB | STRING |
| BLOB | BYTES |
| RAW | BYTES |
| BOOLEAN | BOOLEAN |
| DATE | LOCAL_DATE_TIME |
| TIME(fractional_seconds_precision <= 6) | LOCAL_TIME |
| TIMESTAMP(fractional_seconds_precision <= 6)<br/>TIMESTAMP WITH TIME ZONE(fractional_seconds_precision <= 6)<br/>TIMESTAMP WITH LOCAL TIME ZONE(fractional_seconds_precision <= 6) | LOCAL_DATE_TIME |
| INTERVAL YEAR TO MONTH<br/>INTERVAL DAY TO SECOND(fractional_seconds_precision <= 6) | STRING |

> Note: XML, JSON, ROWID, UROWID, ST_GEOMETRY, BOX2D, BFILE, and UDT types are not currently supported. TIME, TIMESTAMP, and INTERVAL DAY TO SECOND support fractional seconds precision up to 6 digits.
>
> **Limitations:**
> - `USE_NATIVE_TYPE=FALSE` is not supported
> - YashanDB MySQL compatibility mode is not supported

## Source Options

| Name | Type | Required | Default | Description |
|------|------|----------|---------|-------------|
| url | String | Yes | - | JDBC URL, e.g.: jdbc:yasdb://localhost:1688/database |
| driver | String | Yes | - | JDBC driver class, e.g.: com.yashandb.jdbc.Driver |
| username | String | Yes | - | Database username |
| password | String | Yes | - | Database password |
| query | String | Yes | - | SQL query to read data |

## Task Example

### Simple

```hocon
source {
  Jdbc {
    driver = "com.yashandb.jdbc.Driver"
    url = "jdbc:yasdb://localhost:1688/database"
    username = "username"
    password = "password"
    query = "SELECT * FROM myschema.my_table"
  }
}
```

## Changelog

<ChangeLog />
