# 数据库命名约束规范 (Database Naming Convention)

## 1. 总体原则
为了确保 `LaikeAPI (PHP)` 与 `thinkinshop (Java)` 在统一数据库环境下的兼容性与可维护性，所有数据库对象必须遵循统一的命名规范。

## 2. 表命名规范 (Table Naming)
- **风格**：必须使用**全小写**字母。
- **分隔符**：单词之间必须使用**下划线** (`_`) 分隔（即 `snake_case`）。
- **前缀**：所有表必须以 `lkt_` 为前缀。
- **示例**：
    - 正确：`lkt_order_details`, `lkt_product_list`
    - 错误：`lkt_OrderDetails`, `lkt_hotkeywords`, `lkt_setscore`

## 3. 字段命名规范 (Column Naming)
- **风格**：必须使用**全小写**字母。
- **分隔符**：单词之间必须使用**下划线** (`_`) 分隔（即 `snake_case`）。
- **禁忌**：严禁使用 `PascalCase` (大驼峰), `camelCase` (小驼峰) 或包含大写字母。
- **示例**：
    - 正确：`order_id`, `add_time`, `s_no`, `trade_no`
    - 错误：`orderId`, `AddTime`, `sNo`, `Article_id`, `H5_domain`

## 4. 索引命名规范 (Index Naming)
- **主键**：固定为 `PRIMARY`。
- **普通索引**：`idx_表名简写_字段名` (全小写)。
- **唯一索引**：`uk_表名简写_字段名` (全小写)。

## 5. 迁移执行要求
- 所有不符合规范的表名和字段名必须通过增量 SQL 脚本进行重命名。
- 重命名时必须同步更新关联的代码引用（PHP/Java）。
- 增量 SQL 脚本应包含 `RENAME TABLE` 和 `ALTER TABLE ... CHANGE` 语句。

## 6. Java 侧开发指导 (Java Development Guide)
为了保持 Java 实体类符合 Java 命名规范 (camelCase) 同时数据库符合 `snake_case`：
- **Entity 映射**：使用 `@Column(name = "snake_case_name")` 注解映射数据库字段。
- **配置项**：确保 `mybatis.configuration.map-underscore-to-camel-case=true` 已开启，实现自动转换。
- **示例**：
    ```java
    // 数据库字段为 s_no
    @Column(name = "s_no")
    private String sNo; 
    ```

## 7. PHP 侧开发指导 (PHP Development Guide)
- **Model 映射**：ThinkPHP 模型默认支持下划线风格。
- **查询调用**：在 `where()` 或 `field()` 中使用标准的下划线字段名。
- **示例**：
    ```php
    // 正确调用方式
    $order = Db::name('order')->where('s_no', $sNo)->find();
    ```

---
*最后更新：2026-04-15*
