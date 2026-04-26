import ChangeLog from '../changelog/connector-cdc-yashandb.md';

# YashanDB-CDC

> YashanDB CDC 数据源连接器

> **⚠️ 警告：此连接器尚未完全实现和测试。**
> 
> 目前仅快照阶段（批量 JDBC 读取）可用。
> 增量阶段（YStream CDC）标记为 TODO，暂不可用。
> 
> 批量数据读取请使用 [YashanDB JDBC Source](./YashanDB.md)。

## 描述

通过快照和增量捕获读取 YashanDB 数据库变更事件（CDC）。

**快照阶段**：使用 JDBC 分块查询并行读取全量表数据（兼容 Oracle 的 SQL 语法）。

**增量阶段**（TODO）：使用 YStream（YashanDB 原生 CDC API，类似于 Oracle XStream）从 redo log 中捕获实时 DML 和 DDL 变更。

## 支持引擎

> SeaTunnel Zeta<br/>

## 核心特性

- [x] [批处理](../../introduction/concepts/connector-v2-features.md)
- [x] [流处理](../../introduction/concepts/connector-v2-features.md)
- [x] [精确一次](../../introduction/concepts/connector-v2-features.md)
- [x] [列投影](../../introduction/concepts/connector-v2-features.md)
- [x] [并行度](../../introduction/concepts/connector-v2-features.md)
- [x] [支持自定义分片](../../introduction/concepts/connector-v2-features.md)

## 支持的数据源信息

| 数据源    | 支持版本 | 驱动                      | Maven                    |
|----------|---------|---------------------------|--------------------------|
| YashanDB | 23.x    | com.yashandb.jdbc.Driver  | 请参考 YashanDB 官方文档 |

## 数据库依赖

> 需要确保 YashanDB JDBC 驱动 jar 包已放置在 `${SEATUNNEL_HOME}/lib/` 目录下。

## 数据类型映射

参见 [YashanDB Source](./YashanDB.md) 的 JDBC 数据类型映射。

## 数据源选项

| 名称 | 类型 | 必选 | 默认值 | 描述 |
|------|------|------|--------|------|
| hostname | String | 是 | - | 数据库主机名 |
| port | Integer | 是 | - | 数据库端口 |
| username | String | 是 | - | 数据库用户名 |
| password | String | 是 | - | 数据库密码 |
| database-names | List | 是 | - | 要捕获的数据库名称列表 |
| schema-names | List | 是 | - | 要捕获的 schema 名称列表 |
| table-names | List | 否 | - | 要捕获的表名列表 |
| table-pattern | String | 否 | - | 表名匹配模式（正则表达式） |
| url | String | 否 | - | JDBC URL（覆盖 hostname 和 port） |
| startup.mode | Enum | 否 | initial | 启动模式: initial / latest / timestamp / specific |
| stop.mode | Enum | 否 | never | 停止模式: never / latest / timestamp / specific |
| use-select-count | Boolean | 否 | false | 使用 SELECT COUNT(*) 进行行数估算 |
| skip-analyze | Boolean | 否 | false | 跳过 ANALYZE TABLE 行数估算 |
| server-time-zone | String | 否 | UTC | 服务器时区 |
| connect.timeout | Long | 否 | 30000 | 连接超时（毫秒） |
| connect.max-retries | Integer | 否 | 3 | 最大连接重试次数 |
| connection.pool.size | Integer | 否 | 20 | JDBC 连接池大小 |
| schema-changes.enabled | Boolean | 否 | false | 启用 schema 变更捕获 |
| chunk-key.even-distribution.factor.upper-bound | Double | 否 | 100.0 | 分块键分布因子上限 |
| chunk-key.even-distribution.factor.lower-bound | Double | 否 | 0.05 | 分块键分布因子下限 |
| sample.sharding.threshold | Integer | 否 | 1000 | 分块采样阈值 |

## YStream 前置条件（增量捕获）

> **注意**：增量（YStream）捕获部分标记为 TODO，需要单独实现。

### DBA 设置

```sql
-- 创建 YStream 管理员用户
CREATE USER ystream_admin IDENTIFIED BY password;
GRANT CONNECT, RESOURCE, DBA TO ystream_admin;

-- 创建 outbound server
BEGIN
  DBMS_YSTREAM_ADM.CREATE_OUTBOUND(
    server_name => 'SEATUNNEL_OUT',
    source_database => 'ORCL'
  );
END;
/

-- 添加表规则
BEGIN
  DBMS_YSTREAM_ADM.ADD_TABLE_RULES(
    server_name => 'SEATUNNEL_OUT',
    table_names => 'MYSCHEMA.*',
    operation => 'INSERT UPDATE DELETE'
  );
END;
/
```

## 示例

### 简单示例（仅快照）

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

## 更新日志

<ChangeLog />
