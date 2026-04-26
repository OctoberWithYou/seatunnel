import ChangeLog from '../changelog/connector-jdbc.md';

# YashanDB

> JDBC YashanDB 数据汇连接器

## 描述

通过 JDBC 向 YashanDB 写入数据。

YashanDB（崖山数据库）是一款兼容 Oracle 语法和协议的关系型数据库。此连接器使用 YashanDB JDBC 驱动进行批量数据写入。

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

## 支持的数据源信息

| 数据源    | 支持版本 | 驱动                      | URL                                        | Maven                    |
|----------|---------|---------------------------|--------------------------------------------|--------------------------|
| YashanDB | 23.x    | com.yashandb.jdbc.Driver  | jdbc:yasdb://host:port/database    | 请参考 YashanDB 官方文档 |

## 数据库依赖

### Spark/Flink 引擎

> 1. 需要确保 YashanDB JDBC 驱动 jar 包已放置在 `${SEATUNNEL_HOME}/plugins/` 目录下。

### SeaTunnel Zeta 引擎

> 1. 需要确保 YashanDB JDBC 驱动 jar 包已放置在 `${SEATUNNEL_HOME}/lib/` 目录下。

## 数据汇选项

| 名称 | 类型 | 必选 | 默认值 | 描述 |
|------|------|------|--------|------|
| url | String | 是 | - | JDBC URL |
| driver | String | 是 | - | JDBC 驱动类 |
| username | String | 是 | - | 数据库用户名 |
| password | String | 是 | - | 数据库密码 |
| database | String | 是 | - | 数据库名称 |
| table | String | 是 | - | 表名 |

## 示例

### 简单示例

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

## 更新日志

<ChangeLog />

## 限制说明

- 不支持 `USE_NATIVE_TYPE=FALSE` 模式
- 不支持 YashanDB 的 MySQL 兼容模式
