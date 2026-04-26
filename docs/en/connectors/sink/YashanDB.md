import ChangeLog from '../changelog/connector-jdbc.md';

# YashanDB

> JDBC YashanDB Sink Connector

## Description

Write data to YashanDB via JDBC.

YashanDB is a relational database compatible with Oracle syntax and protocols. This connector uses the YashanDB JDBC driver for batch data writing.

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

## Supported DataSource Info

| Datasource | Supported Versions | Driver | Url | Maven |
|------------|-------------------|--------|-----|-------|
| YashanDB   | 23.x              | com.yashandb.jdbc.Driver | jdbc:yasdb://host:port/database | Please refer to YashanDB official documentation |

## Database Dependency

### For Spark/Flink Engine

> 1. You need to ensure that the YashanDB JDBC driver jar package has been placed in directory `${SEATUNNEL_HOME}/plugins/`.

### For SeaTunnel Zeta Engine

> 1. You need to ensure that the YashanDB JDBC driver jar package has been placed in directory `${SEATUNNEL_HOME}/lib/`.

## Sink Options

| Name | Type | Required | Default | Description |
|------|------|----------|---------|-------------|
| url | String | Yes | - | JDBC URL |
| driver | String | Yes | - | JDBC driver class |
| username | String | Yes | - | Database username |
| password | String | Yes | - | Database password |
| database | String | Yes | - | Database name |
| table | String | Yes | - | Table name |

## Task Example

### Simple

```hocon
sink {
  Jdbc {
    driver = "com.yashandb.jdbc.Driver"
    url = "jdbc:yasdb://localhost:1688/database"
    username = "username"
    password = "password"
    database = "database"
    table = "myschema.my_table"
  }
}
```

## Changelog

<ChangeLog />

## Limitations

- `USE_NATIVE_TYPE=FALSE` is not supported
- YashanDB MySQL compatibility mode is not supported
