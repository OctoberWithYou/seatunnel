import ChangeLog from '../changelog/connector-jdbc.md';

# YashanDB

> JDBC YashanDB 数据源连接器

## 描述

通过 JDBC 从 YashanDB 读取外部数据源数据。

YashanDB（崖山数据库）是一款兼容 Oracle 语法和协议的关系型数据库。此连接器使用 YashanDB JDBC 驱动进行批量数据读取。

## 支持引擎

> Spark<br/>
> Flink<br/>
> SeaTunnel Zeta<br/>

## 核心特性

- [x] [批处理](../../introduction/concepts/connector-v2-features.md)
- [ ] [流处理](../../introduction/concepts/connector-v2-features.md)
- [x] [精确一次](../../introduction/concepts/connector-v2-features.md)
- [x] [列投影](../../introduction/concepts/connector-v2-features.md)
- [x] [并行度](../../introduction/concepts/connector-v2-features.md)
- [x] [支持自定义分片](../../introduction/concepts/connector-v2-features.md)

> 支持查询SQL，可以实现投影效果。

## 支持的数据源信息

| 数据源    | 支持版本 | 驱动                      | URL                                        | Maven                    |
|----------|---------|---------------------------|--------------------------------------------|--------------------------|
| YashanDB | 23.x    | com.yashandb.jdbc.Driver  | jdbc:yasdb://host:port/database    | 请参考 YashanDB 官方文档 |

## 数据库依赖

### Spark/Flink 引擎

> 1. 需要确保 YashanDB JDBC 驱动 jar 包已放置在 `${SEATUNNEL_HOME}/plugins/` 目录下。

### SeaTunnel Zeta 引擎

> 1. 需要确保 YashanDB JDBC 驱动 jar 包已放置在 `${SEATUNNEL_HOME}/lib/` 目录下。

## 数据类型映射

| YashanDB 数据类型 | SeaTunnel 数据类型 |
|-------------------|-------------------|
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

> 注意：暂不支持 XML、JSON、ROWID、UROWID、ST_GEOMETRY、BOX2D、BFILE 和 UDT 类型。TIME、TIMESTAMP 和 INTERVAL DAY TO SECOND 的 fractional_seconds_precision 精度仅支持到 6 位。
>
> **限制说明：**
> - 不支持 `USE_NATIVE_TYPE=FALSE` 模式
> - 不支持 YashanDB 的 MySQL 兼容模式

## 数据源选项

| 名称 | 类型 | 必选 | 默认值 | 描述 |
|------|------|------|--------|------|
| url | String | 是 | - | JDBC URL，例如：jdbc:yasdb://localhost:1688/database |
| driver | String | 是 | - | JDBC 驱动类，例如：com.yashandb.jdbc.Driver |
| username | String | 是 | - | 数据库用户名 |
| password | String | 是 | - | 数据库密码 |
| query | String | 是 | - | 读取数据的 SQL 查询 |

## 示例

### 简单示例

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

## 更新日志

<ChangeLog />
