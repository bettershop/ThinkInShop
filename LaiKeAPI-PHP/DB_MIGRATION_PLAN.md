# 数据库迁移统一计划（LaikeAPI）

最后更新：2026-04-08
适用范围：`LaikeAPI`（PHP）对齐 `thinkinshop`（Java）数据库语义

## 1. 统一口径

1. 目标标准库：Java 侧 `v3_db`。
2. PHP 侧 `lkt_db` 以 Java 表语义和业务含义为准进行对齐。
3. 迁移策略：优先后端适配，非必要不改前端。
4. 契约原则：接口响应字段名、结构、业务语义保持稳定。
5. 版本管理口径：统一使用 SVN（`svn status` / `svn diff` / `svn log`）。
6. 实现约束：同库对齐场景禁止新增 service 层（包括 `*DomainService/*WriteService` 这类抽象层）。

## 1.2 代码改造硬约束（2026-04-08 新增）

1. `php` 与 `java` 同库迁移时，若表结构字段、接口返回结构、业务逻辑功能一致，则不改代码。
2. 仅当存在字段口径差异或接口返回结构差异时才改动，且必须采用最小修改。
3. 禁止为了“实现收敛”做额外分层重构（例如新增通用 service 层）。

## 1.1 当前环境基线（2026-04-07 已实测）

1. 代码基线：
   - `LaikeAPI`：`svn update` 后为 `r37652`
   - `thinkinshop`：`svn update` 后为 `r2902`
   - 两个工作副本本轮均无冲突（`svn status` 无 `C`）。
2. 数据库基线：
   - 本机：`127.0.0.1:3306/lkt_db`
   - 远端：`47.107.123.240:3386/v3_db`
   - 已完成远端 `v3_db -> 本地 lkt_db` 导入并通过基础计数校验。
3. 对齐基线：
   - 已执行 `scripts/db_full_alignment_check.sh`
   - 结果为 `PASS`（排除 `xxl_job%` 后表/字段/索引差异均为 0）。
4. 服务可用性基线：
   - PHP：`php -S 127.0.0.1:8000 -t public` 可访问并返回 `200`
   - Java：统一入口 `LaiKeApisApplication` 启动后，`/apis/` 与 `/apis/actuator/health` 返回 `200`。
5. 基线失效条件（任一触发需重跑基线）：
   - 执行过新的 `svn update`
   - 本地库重新导入或被人工改动
   - 核心配置变更（DB/Redis/端口/环境变量）
   - `scripts/` 下迁移相关脚本发生变更

## 2. 迁移目标

1. 去除 PHP 独有且与 Java 不一致的表依赖。
2. 用 Java 风格源表重建汇总值，减少冗余汇总字段。
3. 底层数据源迁移时，保持现网接口行为兼容。
4. 每个迁移项必须具备：
   - 映射定义
   - 代码改动清单
   - 验收清单
   - 回滚方案

## 3. 当前进度（已核实）

### 3.1 已完成（代码层）

1. `lkt_living_anchor` 业务依赖基本移除。
2. 当前业务代码中已无直接读写 `lkt_living_anchor`（历史 SQL 文件 `app/db_log` 除外）。
3. 直播佣金链路已切到 Java 风格来源：
   - `lkt_living_commission`（`status=101/100`）
   - `lkt_record`（`type=42`）
   - 直播间/粉丝等关联表

### 3.2 已完成（2026-04-07）

1. `lkt_import_log -> lkt_file_delivery(type=2)` 已完成代码切换并完成联调验收。
2. `app/admin/controller/admin/User.php` 已切换为：
   - `uploadAddUser()`：写 `lkt_file_delivery` 且 `type=2`
   - `uploadRecordList()`：查 `lkt_file_delivery` 且 `type=2`
   - `delUploadRecord()`：删 `lkt_file_delivery` 且 `type=2`
3. 验收结论（本地）：
   - 已完成 `47.107.123.240:3386/v3_db -> 本机 lkt_db` 导入
   - `lkt_file_delivery` 表数据正常（包含 `type=2` 记录）
   - 业务代码扫描确认无运行态 `lkt_import_log` 依赖（`app/db_log` 历史 SQL 除外）

### 3.3 历史确认项

1. 2026-04-04 已确认：`v3_db` 已删 Java-only 表：
   - `lkt_bargain_config`, `lkt_bargain_goods`, `lkt_bargain_order`, `lkt_bargain_record`, `lkt_plug_ins`, `lkt_user_role_copy1`
2. 2026-04-04 已确认：`lkt_db` 已删 PHP-only 表：
   - `lkt_platform_activities`, `lkt_platform_activities_del`

### 3.4 约束回退记录（2026-04-08）

1. 按最新约束，已回退 Address 领域 service 层收敛实现：
   - 回退：`app/admin/controller/app/Address.php`
   - 回退：`app/admin/controller/mall/Address.php`
   - 删除：`app/common/AddressDomainService.php`
   - 删除：`app/common/AddressWriteService.php`
2. 后续 Address 与其他域迁移统一按“字段差异驱动 + 最小修改”执行，不再引入 service 层抽象。

## 4. 统一执行模板（每个迁移项都按此流程）

0. 前置（强制）
   - 执行 SVN 更新后再做任何数据库合并计划与操作。
   - 固定流程：`svn update` -> 处理冲突（如有）-> 再进入“盘点/映射/改造/验证”。
   - 若存在冲突，必须先完成冲突解决并确认工作副本可用，再继续后续步骤。

1. 盘点
   - 定位 PHP/Java 读写点
   - 按接口影响面分级（高/中/低）
2. 映射
   - 定义旧表/旧字段 -> 新来源表/新字段/计算规则
   - 明确空值、历史数据、默认值兼容规则
3. 改造
   - 替换控制器/服务层读写
   - 保持外部 API 契约不变
4. 验证
   - 同数据下对比改造前后接口结果
   - 校验列表、明细、汇总一致性
5. 回滚
   - 预留回滚策略（代码/SQL）
   - 记录变更批次与验证结论

## 4.1 数据库合并前 Gate（强制通过）

1. G1-SVN 同步：
   - 执行 `svn update`（两仓都要）
   - `svn status` 必须无冲突项（`C`）。
2. G2-数据库连通：
   - 本机 `lkt_db` 连通
   - 远端 `v3_db` 连通。
3. G3-导入演练：
   - 执行 `LOCAL_PASSWORD=*** LOCAL_DB=lkt_db bash scripts/import_online_db_to_local.sh`
   - 导入后基础计数校验通过。
4. G4-对齐检查：
   - 执行 `LOCAL_* + V3_*` 的 `scripts/db_full_alignment_check.sh`
   - 必须为 `PASS`（排除 `xxl_job%`）。
5. G5-服务冒烟：
   - PHP `php think` 通过
   - Java 统一入口 `LaiKeApisApplication` 可启动并返回健康检查 `200`。
6. G6-回滚快照：
   - 涉及破坏性 DDL/DML 前，必须先导出本地 `lkt_db` 快照（`mysqldump`）。
7. 任一 Gate 未通过时：
   - 迁移计划状态标记为“阻塞”
   - 禁止进入“改造/合并/提交流程”。

## 5. 里程碑

### M1（已完成）：`lkt_living_anchor` 去依赖

完成标准：
1. 无业务代码直接读写 `lkt_living_anchor`
2. 佣金汇总由 `lkt_living_commission + lkt_record(type=42)` 计算
3. 直播结算/回滚链路不再增减 anchor 汇总字段

### M2（已完成验收）：`lkt_import_log` 迁移到 `lkt_file_delivery(type=2)`

当前状态：代码改造与联调验收均已完成（2026-04-07）。

必要改造：
1. `uploadAddUser()` 写入 `lkt_file_delivery`，并固定 `type=2`
2. `uploadRecordList()` 查询 `lkt_file_delivery` 且 `type=2`
3. `delUploadRecord()` 从 `lkt_file_delivery` 删除
4. 对外字段保持兼容（`name/status/text/add_date` 等）

验收标准：
1. 导入成功/失败记录展示正确
2. 文件名、状态、时间筛选结果正确
3. 删除行为与现有前端预期一致
4. 不引入前端改造需求

本轮验收记录（2026-04-07）：
1. 数据准备：已将远端 `v3_db` 导入本地 `lkt_db`。
2. 数据核验：`lkt_file_delivery` 总数与 `type=2` 历史记录存在。
3. 代码核验：`uploadAddUser/uploadRecordList/delUploadRecord` 均指向 `lkt_file_delivery` 且带 `type=2` 条件。
4. 框架冒烟：`php think` 通过。

### M3（持续）：其他表逐域对齐

目标：
1. 在不改前端契约的前提下，完成剩余业务域从 PHP 旧口径到 Java `v3_db` 口径的逐域对齐。

优先级分层（先 P0，再 P1，再 P2）：
1. P0：高风险高频链路（用户、订单、支付、结算、直播/分销核心插件）
2. P1：经营分析链路（报表、统计、导出）
3. P2：低频后台能力（配置类、工具类、历史兼容接口）

每个域必须交付的标准产物：
1. 差异清单：旧表/旧字段与 `v3_db` 对应关系（包含“无 1:1 映射”的处理方案）
2. 映射文档：字段来源、计算规则、精度规则、空值/历史值兼容规则
3. 改造清单：涉及的 PHP 文件、SQL 读写点、接口清单
4. 验证记录：改造前后接口对比、聚合值对比、抽样明细对比
5. 回滚说明：代码回退点、SQL 回退点、影响范围

每个域的启动条件（全部满足才开工）：
1. 已完成目标域 SQL 读写点扫描（读/写分别标注）
2. 已确认影响接口清单（含 APIKey、调用端）
3. 已确认是否存在历史数据回填或兼容转换需求

每个域的完成条件（全部满足才合并）：
1. 目标域旧表依赖已全部替换或有明确兼容层说明
2. 本文“契约保护清单”全部通过
3. 本文“最低测试基线”全部通过
4. 迁移批次记录已追加到“变更记录”

推荐推进节奏（可按人力调整）：
1. 单批只做 1 个业务域，避免跨域并行导致口径漂移
2. 每批按 3 个阶段推进：盘点与映射 -> 代码改造 -> 验证与回滚演练
3. 上一批未达“完成条件”前，不开启下一批

卡点处理口径：
1. 遇到“无 1:1 映射”时，优先采用“可计算口径”替代，不新增长期冗余汇总字段
2. 遇到历史脏数据时，先定义兼容规则并在接口层兜底，再安排数据修复批次
3. 如需临时兼容字段，必须写明下线条件和最晚下线批次

M3 具体执行批次（基础流程优先）：

M3 当前建议执行批次（下一轮落地）：
1. 批次名称：`M3-A1 用户域基线批（P0）`
2. 目标：先建立用户域“映射 + 接口 + SQL”三张基表，作为后续用户域改造入口。
3. 交付物（必须全部产出）：
   - 用户域字段映射表（旧字段 -> 新来源 -> 兼容规则）
   - 用户域 SQL 读写点清单（文件路径 + SQL片段 + 影响接口）
   - 用户域接口清单（接口路径 + apiKey + 调用端）
   - 用户域回归清单（登录/资料/地址）
4. 进入 M3-A1 的前提：
   - 通过“4.1 Gate”全部项
   - 已确认本地 `lkt_db` 与远端 `v3_db` 对齐 `PASS`
5. M3-A1 完成判定：
   - 交付物齐全且可复核
   - 无新增接口契约变更
   - 迁移变更记录已追加本文件。
6. 当前状态（2026-04-07）：
   - `M3_A1_USER_DOMAIN_BASELINE.md` 已提交，三张基表产物已落库。
   - 环境冒烟通过（PHP/Java 均返回 `200`）。
   - `M3-A2` 已完成收口，下一步进入 `M3-B1`（订单域基线批）。

### M3-A（P0）用户域

目标：
1. 用户身份、登录态、基础资料相关数据口径先统一，作为后续订单/商品链路前置依赖。

核心表（以扫描结果为准）：
1. `lkt_user`
2. `lkt_user_address`
3. `lkt_record`（用户行为/余额流水中与用户域相关类型）
4. 其他用户域扩展表（会员等级、标签、关系链）

核心接口范围：
1. 用户登录/注册
2. 用户资料与地址管理
3. 会员基础信息查询（管理端与C端）

必做动作：
1. 输出用户域“字段映射表”（旧字段 -> 新来源/计算口径）
2. 清理用户域直连旧口径表的 SQL
3. 固化手机号、国家码、区号等字段规则与唯一性规则

验收门槛：
1. 登录、资料查询、地址增删改查全链路通过
2. 用户主键/业务键（`id`/`user_id`）映射一致
3. 用户相关响应字段无新增/无缺失/无重命名

### M3-B（P0）订单域

目标：
1. 打通“下单 -> 支付 -> 履约 -> 完结/关闭”主链路的统一口径。

核心表（以扫描结果为准）：
1. `lkt_order`
2. `lkt_order_details`
3. `lkt_record`（订单相关资金/操作流水类型）
4. 订单履约与售后关联表（发货、退款、核销）

核心接口范围：
1. 订单列表/详情
2. 下单与支付后状态推进
3. 发货、收货、关闭、售后相关接口

必做动作：
1. 明确订单状态枚举统一字典（`status`、`r_status`、售后状态）
2. 统一金额字段计算顺序（商品金额、优惠、运费、实付）
3. 对账 SQL：订单主表、明细表、流水表三方一致性校验

验收门槛：
1. 订单状态流转无回退、无跳级异常
2. 同订单在列表与详情中的金额一致
3. 管理端与用户端订单视图字段语义一致

当前状态（2026-04-14）：
1. 已完成订单域初步静态扫描，核心入口与关键 SQL 读写点已定位。
2. 已新增订单域基线文档：`M3_B1_ORDER_DOMAIN_BASELINE.md`。
3. 已落订单域扫描产物：
   - `runtime/m3_b1_order_domain/php_order_refs.txt`
   - `runtime/m3_b1_order_domain/php_delivery_refs.txt`
   - `runtime/m3_b1_order_domain/php_pay_refs.txt`
   - `runtime/m3_b1_order_domain/php_admin_order_refs.txt`
4. 已补订单域状态字典草案：
   - `runtime/m3_b1_order_domain/php_order_status_dictionary.txt`
5. 已完成首批最小改造：
   - `app/admin/controller/admin/Order.php` 订单导出补齐 `8=待核销`
   - `app/admin/controller/plugin/sec/Order.php` 订单详情/列表补齐 `8=待核销`
6. 下一步按“字段映射 + 状态字典 + 接口清单”继续细化，再进入最小改造。

### M3-C（P0）商品域

目标：
1. 统一商品主数据口径，保障商品展示、上下架、价格展示稳定。

核心表（以扫描结果为准）：
1. `lkt_product_list`
2. `lkt_product_class`
3. 商品扩展属性/品牌/图集相关表

核心接口范围：
1. 商品列表/详情（C端、管理端、商户端）
2. 上下架与基础信息编辑
3. 商品搜索与筛选

必做动作：
1. 统一商品可售状态、回收状态、商户状态口径
2. 统一商品基础价格字段来源（展示价/划线价/活动价边界）
3. 排查商品域 SQL 中对历史冗余字段的依赖

验收门槛：
1. 同一商品在各端列表与详情基础信息一致
2. 上下架后缓存与数据库状态一致
3. 搜索与筛选结果不出现跨状态脏数据

### M3-D（P0）SKU/库存域

目标：
1. 统一 SKU 属性、库存流水、可售库存口径，避免超卖与库存错账。

核心表（以扫描结果为准）：
1. `lkt_configure`（SKU配置）
2. `lkt_stock`（库存流水）
3. 与 SKU 属性值、库存预警、库存锁定相关表

核心接口范围：
1. SKU 列表/详情
2. 库存变更（下单扣减、取消回滚、手工调整）
3. 低库存预警与库存报表

必做动作：
1. 明确“可售库存、锁定库存、总库存”计算公式
2. 统一 `lkt_stock.type` 枚举定义与业务动作映射
3. 校验订单扣减与库存流水逐单可追溯

验收门槛：
1. 扣减/回滚后库存数值守恒
2. SKU 维度库存与商品维度汇总一致
3. 低库存预警结果与实际库存一致

### M3-E（P1）数据库字典与口径治理

目标：
1. 建立跨域统一数据库字典，避免同字段不同义、同状态不同码。

输出物（必须落库到文档）：
1. 表字典：表用途、主键、核心索引、读写方
2. 字段字典：字段含义、单位、是否可空、默认值、来源口径
3. 枚举字典：状态码与业务语义（如 `order.status`、`record.type`、`file_delivery.type`）
4. 兼容字典：历史字段保留原因、下线条件、计划下线批次

落地规则：
1. 每完成一个域，就增量更新数据库字典
2. 字典未更新视为迁移批次未完成
3. 后续新增表/字段必须先入字典再上线

### M3-F（P0）全表核对与收口（强制）

强制目标：
1. 除 `xxl_job` 相关表外，PHP `lkt_db` 与 Java `v3_db` 的业务库处理口径必须全量对齐。
2. 这里的“对齐”不仅是表结构，还包括代码读写与业务计算口径一致。

范围定义（必须同时满足）：
1. 业务表范围：`lkt_%` 表全量纳入。
2. 排除范围：`xxl_job%`（调度中间件独立管理，不纳入业务库对齐）。
3. 其他中间件库（如 nacos/seata）不纳入本计划。

核对维度（逐表执行）：
1. 表级：表是否存在、存储引擎、字符集/排序规则。
2. 字段级：字段名、类型、长度、是否可空、默认值、注释。
3. 索引级：主键、唯一索引、普通索引、联合索引顺序。
4. 枚举级：状态码与业务含义（如 `status/type/recycle`）是否一致。
5. 处理级：PHP 与 Java 在该表上的读写入口、聚合算法、状态流转是否一致。

执行步骤（固定）：
1. 先导出两库元数据清单（`lkt_db` / `v3_db`，排除 `xxl_job%`）。
2. 生成四类差异报告：表差异、字段差异、索引差异、枚举差异。
3. 按“先结构后逻辑”的顺序修复：先消除结构差异，再消除处理口径差异。
4. 每修复一批，回填本计划“变更记录”，并更新数据库字典。
5. 优先使用自动化脚本执行：`scripts/db_full_alignment_check.sh`（输出差异报告到 `runtime/db_align_reports/`）。

建议 SQL（可直接执行）：
1. 表清单：
```sql
SELECT table_name, engine, table_collation
FROM information_schema.tables
WHERE table_schema = 'lkt_db'
  AND table_name LIKE 'lkt\\_%'
  AND table_name NOT LIKE 'xxl_job%';
```
```sql
SELECT table_name, engine, table_collation
FROM information_schema.tables
WHERE table_schema = 'v3_db'
  AND table_name LIKE 'lkt\\_%'
  AND table_name NOT LIKE 'xxl_job%';
```
2. 字段清单：
```sql
SELECT table_name, column_name, column_type, is_nullable, column_default, column_comment
FROM information_schema.columns
WHERE table_schema = 'lkt_db'
  AND table_name LIKE 'lkt\\_%'
  AND table_name NOT LIKE 'xxl_job%';
```
```sql
SELECT table_name, column_name, column_type, is_nullable, column_default, column_comment
FROM information_schema.columns
WHERE table_schema = 'v3_db'
  AND table_name LIKE 'lkt\\_%'
  AND table_name NOT LIKE 'xxl_job%';
```
3. 索引清单：
```sql
SELECT table_name, index_name, non_unique, seq_in_index, column_name
FROM information_schema.statistics
WHERE table_schema = 'lkt_db'
  AND table_name LIKE 'lkt\\_%'
  AND table_name NOT LIKE 'xxl_job%';
```
```sql
SELECT table_name, index_name, non_unique, seq_in_index, column_name
FROM information_schema.statistics
WHERE table_schema = 'v3_db'
  AND table_name LIKE 'lkt\\_%'
  AND table_name NOT LIKE 'xxl_job%';
```

收口标准（必须全部为真）：
1. 表差异数 = 0（排除 `xxl_job%` 后）。
2. 字段差异数 = 0（类型/默认值/可空性一致）。
3. 索引差异数 = 0（索引名、列顺序、唯一性一致）。
4. 枚举/状态语义差异数 = 0（数据库字典有记录）。
5. PHP 与 Java 同场景关键接口返回一致（字段与业务含义一致）。
6. 未完成上述 1-5 之前，迁移计划不得标记“完成”。

执行顺序（固定）：
1. M3-A 用户域
2. M3-B 订单域
3. M3-C 商品域
4. M3-D SKU/库存域
5. M3-E 数据库字典治理与收口
6. M3-F 全表核对与最终收口（除 `xxl_job%`）

## 6. 契约保护清单

每个迁移项完成前必须全部满足：
1. 接口路径和 `apiKey` 不变
2. 请求参数名不变
3. 响应字段名不变
4. 分页/排序行为不变
5. 金额与佣金精度规则不变（需两位小数的维持两位）
6. 日志与审计链路可追溯

## 7. 最低测试基线

1. 接口冒烟：列表、详情、写操作、导入/导出路径
2. 数据一致性：聚合值前后对比 + 抽样行级对比
3. 回归重点：
   - 直播插件链路
   - 会员导入链路
4. 强制流程：每次代码改动后，必须先完成本地测试/检查再进入提交流程。
5. 若因环境限制无法完成全量测试，必须在结果中明确：
   - 已执行的本地测试项
   - 未执行测试项及原因
   - 对应风险范围

## 7.1 当前未完成项（截至 2026-04-07）

1. 变更记录需持续追加：
   - 后续每个迁移批次完成后，必须补全“变更记录（执行记录）”。
2. 环境口径风险提示：
   - 本计划默认按 `dev` 配置与 `LaiKeApisApplication` 统一入口执行；若切换其他 profile，需重新执行 Gate。

## 7.2 最小改动执行规则（2026-04-07 重新评估）

1. 迁移改造仅针对“同名表字段差异”或“同业务语义字段差异”触发，禁止实现层重构式改造。
2. 非字段差异导致的问题，优先通过：
   - 参数映射
   - 字段兼容兜底
   - 条件分支最小补丁
   解决，不做服务层抽象扩展。
3. 接口契约与行为保持优先级最高：
   - 不改路径
   - 不改参数名
   - 不改返回结构
   - 不改变历史返回码语义
4. 每批最多改一个业务点，禁止“顺手重构”。
5. 每批必须附带同账号接口级回测结果（统一账号：`000000/000000`）。

## 8. 变更记录规范

每次迁移批次补充以下信息：
1. 日期
2. 迁移项
3. 变更文件
4. 验证结果
5. 回滚说明

## 9. 变更记录（执行记录）

### 记录 1（2026-04-07）

1. 迁移项：M2 `lkt_import_log -> lkt_file_delivery(type=2)` 联调验收收口
2. 变更文件：
   - `app/admin/controller/admin/User.php`
   - `scripts/import_online_db_to_local.sh`
   - `scripts/db_full_alignment_check.sh`
   - `PAUSE_RESUME_NOTE.md`
   - `DB_MIGRATION_PLAN.md`
3. 验证结果：
   - 远端 `v3_db` 已成功导入本地 `lkt_db`
   - `lkt_file_delivery` 查询与 `type=2` 记录正常
   - `php think` 冒烟通过
4. 回滚说明：
   - 代码回滚：按 SVN 变更文件逐个回退
   - 数据回滚：重新执行导库脚本恢复目标库快照

### 记录 2（2026-04-07）

1. 迁移项：迁移计划二次检查与完善（Gate + 环境基线 + 下一批任务定义）
2. 变更文件：
   - `DB_MIGRATION_PLAN.md`
3. 验证结果：
   - 计划口径已统一到 `lkt_db/v3_db`
   - 合并前强制流程（SVN/Gate）已固化
   - 下一批执行目标（`M3-A1`）已明确可交付标准
4. 回滚说明：
   - 文档变更可按 SVN 直接回退该文件

### 记录 3（2026-04-07）

1. 迁移项：`M3-A1 用户域基线批（P0）` 产物落库 + 环境实测复核
2. 变更文件：
   - `M3_A1_USER_DOMAIN_BASELINE.md`
   - `DB_MIGRATION_PLAN.md`
   - `scripts/db_full_alignment_check.sh`
3. 验证结果：
   - `lkt_user/lkt_user_address/lkt_record` 本地 `lkt_db` 与远端 `v3_db` 字段清单对比 `NO_DIFF`
   - 数据量一致：`158 / 102 / 12706`
   - PHP 冒烟：`php think` 通过，`php -S` 本地访问 `200`
   - Java 统一入口：`laike-apis` 启动后 `/apis/` 与 `/apis/actuator/health` 均 `200`
4. 回滚说明：
   - 文档回滚：按 SVN 回退 `M3_A1_USER_DOMAIN_BASELINE.md` 与 `DB_MIGRATION_PLAN.md`
   - 脚本回滚：按 SVN 回退 `scripts/db_full_alignment_check.sh`

### 记录 4（2026-04-07）

1. 迁移项：`M3-A2` 第一批代码改造（地址双实现公共能力收敛）
2. 变更文件：
   - `app/common/AddressDomainService.php`
   - `app/admin/controller/app/Address.php`
   - `app/admin/controller/mall/Address.php`
   - `M3_A2_ADDRESS_CONVERGENCE_PLAN.md`
3. 验证结果：
   - 已将地区级联、`place` 解析、`address_xq` 组装、名称/ID路径转换收敛到公共服务
   - 语法检查通过：`php -l`（上述 3 个 PHP 文件）
   - `php think route:list` 执行通过
4. 回滚说明：
   - 代码回滚：按 SVN 回退上述 3 个 PHP 文件
   - 文档回滚：按 SVN 回退 `M3_A2_ADDRESS_CONVERGENCE_PLAN.md`

### 记录 5（2026-04-07）

1. 迁移项：`M3-A2` 第二批代码改造（地址写流程收敛）
2. 变更文件：
   - `app/common/AddressWriteService.php`
   - `app/admin/controller/app/Address.php`
   - `app/admin/controller/mall/Address.php`
   - `M3_A2_ADDRESS_CONVERGENCE_PLAN.md`
   - `DB_MIGRATION_PLAN.md`
3. 验证结果：
   - 已将地址 `save/update/delete/setDefault` 收敛到公共写服务
   - 双控制器已改为“参数适配 + 返回封装”
   - 语法检查通过：`php -l`（上述 3 个 PHP 文件）
   - `php think route:list` 执行通过
4. 回滚说明：
   - 代码回滚：按 SVN 回退上述 3 个 PHP 文件
   - 文档回滚：按 SVN 回退本文件与 `M3_A2_ADDRESS_CONVERGENCE_PLAN.md`

### 记录 6（2026-04-07）

1. 迁移项：`M3-A2` 第三批收口（最小改动口径）
2. 变更文件：
   - `scripts/address_gateway_smoke.sh`
   - `M3_A2_ADDRESS_CONVERGENCE_PLAN.md`
   - `DB_MIGRATION_PLAN.md`
3. 验证结果：
   - 统一账号登录回测通过：`mall.Login.login=200`、`app.login.login=200`
   - 地址读接口回测通过：`mall/app` 地址列表与编辑页读取可达
   - `app.Address.getCountyInfo=109` 保持历史语义不变
4. 回滚说明：
   - 脚本回滚：按 SVN 回退 `scripts/address_gateway_smoke.sh`
   - 文档回滚：按 SVN 回退上述 2 个文档文件

### 记录 7（2026-04-17）

1. 迁移项：`M3-B1 订单域状态字典对齐（self_lifting=3 分支）`
2. 变更文件：
   - `app/common/Order.php`
   - `app/admin/controller/mall/Pay.php`
   - `app/admin/controller/admin/Order.php`
   - `app/admin/controller/plugin/sec/Order.php`
   - `M3_B1_ORDER_DOMAIN_BASELINE.md`
3. 验证结果：
   - `self_lifting=3`（虚拟订单需要线下核销）的订单在支付后，`status` 和 `r_status` 已统一赋值为 `8`（待核销），与 Java `PublicOrderServiceImpl.orderPayment` 逻辑对齐。
   - `app/admin/controller/mall/Pay.php` 中 `gndd` 方法的 `r_status` 赋值错误已修正。
   - `M3_B1_ORDER_DOMAIN_BASELINE.md` 已更新状态字典与变更记录。
4. 回滚说明：
   - 代码回滚：按 SVN 回退上述 4 个 PHP 文件
   - 文档回滚：按 SVN 回退 `M3_B1_ORDER_DOMAIN_BASELINE.md`

### 记录 8（2026-04-17）

1. 迁移项：`M3-C1 商品域基线对齐（多语言与虚拟商品细分）`
2. 变更文件：
   - `app/common/Product.php`
   - `app/admin/controller/app/Product.php`
   - `M3_C1_PRODUCT_DOMAIN_BASELINE.md`
3. 验证结果：
   - 商品列表查询已兼容 `commodity_type in (1, 2, 3)`，确保 Java 侧写入的细分虚拟商品类型在 PHP 侧可见。
   - 商品列表查询已默认过滤 `lang_pid is null`，避免多语言副本在主列表中重复显示。
   - 移动端商品详情已兼容 `commodity_type=3` 的核销逻辑。
4. 回滚说明：
   - 代码回滚：按 SVN 回退上述 2 个 PHP 文件
   - 文档回滚：删除 `M3_C1_PRODUCT_DOMAIN_BASELINE.md`

### 记录 9（2026-04-17）

1. 迁移项：`M3-B2 订单金额计算逻辑对齐（积分抵扣与最小支付金额）`
2. 变更文件：
   - `app/common/Plugin/NormalOrder.php`
   - `M3_B2_ORDER_AMOUNT_BASELINE.md`
3. 验证结果：
   - 统一了积分抵扣上限：积分仅能抵扣商品金额，不能抵扣运费，与 Java `OrderDubboServiceImpl` 逻辑对齐。
   - 统一了最小支付金额：非钱包支付最小 `0.01`，钱包支付允许 `0.00`。
   - 修正了 `settlement` 和 `payment` 中金额计算的精度处理。
4. 回滚说明：
   - 代码回滚：按 SVN 回退 `app/common/Plugin/NormalOrder.php`
   - 文档回滚：删除 `M3_B2_ORDER_AMOUNT_BASELINE.md`

### 记录 10（2026-04-17）

1. 迁移项：`M3-B3 售后与退款逻辑对齐（退款金额口径）`
2. 变更文件：
   - `app/common/Plugin/RefundUtils.php`
   - `M3_B3_REFUND_DOMAIN_BASELINE.md`
3. 验证结果：
   - 统一了最大退款金额口径：退款申请自动包含运费部分，且增加了严格的上限校验，防止超额退款，与 Java `PublicRefundServiceImpl.java` 逻辑对齐。
   - 创建了售后基线文档，定义了资产回滚规则。
4. 回滚说明：
   - 代码回滚：按 SVN 回退 `app/common/Plugin/RefundUtils.php`
   - 文档回滚：删除 `M3_B3_REFUND_DOMAIN_BASELINE.md`

### 记录 11（2026-04-17）

1. 迁移项：`M3-C2 商品库存与销量对齐（Java 真实销量补齐）`
2. 变更文件：
   - `ProductListModelMapper.java`
   - `PublicOrderServiceImpl.java`
   - `M3_C2_PRODUCT_INVENTORY_BASELINE.md`
3. 验证结果：
   - 在 Java 支付回调逻辑中补齐了 `real_volume` (真实销量) 的原子更新。
   - 补齐了供应商商品销量的同步更新逻辑。
   - 对齐了 PHP `Order.php:200` 的核心业务逻辑，解决了两端支付后销量统计不一致的问题。
4. 回滚说明：
   - 代码回滚：按 SVN 回退 Java 侧上述 2 个文件
   - 文档回滚：删除 `M3_C2_PRODUCT_INVENTORY_BASELINE.md`

### 记录 12（2026-04-17）

1. 迁移项：`M3-B4 分销佣金域对齐`
2. 变更文件：
   - `M3_B4_DISTRIBUTION_DOMAIN_BASELINE.md`
3. 验证结果：
   - 对 9 张分销相关表（`lkt_distribution_config/goods/grade/record/log/ranking/withdraw/user_distribution/income`）进行了全面扫描。
   - 8 张核心表 PHP 与 Java 字段完全一致，**无需改造代码**。
   - `lkt_distribution_income` 为 Java 独有统计表，不影响现有业务。
4. 回滚说明：
   - 文档回滚：删除 `M3_B4_DISTRIBUTION_DOMAIN_BASELINE.md`

### 记录 13（2026-04-17）

1. 迁移项：`M3-C3 商品促销域对齐`
2. 变更文件：
   - `M3_C3_PROMOTION_DOMAIN_BASELINE.md`
3. 验证结果：
   - 对 `lkt_coupon/lkt_coupon_activity/lkt_coupon_record/lkt_full_reduction/lkt_floor_price/lkt_special` 等促销表进行了全面扫描。
   - **所有促销表字段均无差异**，根据"表无差异不动代码"约束，未改动任何代码。
4. 回滚说明：
   - 文档回滚：删除 `M3_C3_PROMOTION_DOMAIN_BASELINE.md`

### 记录 14（2026-04-17）

1. 迁移项：`M3-D2 拼团域对齐`
2. 变更文件：
   - `M3_D2_GROUPBUY_DOMAIN_BASELINE.md`
3. 验证结果：
   - 对 `lkt_pt_go_group_order/lkt_pt_seconds_pro/lkt_pt_record` 等拼团表进行了全面扫描。
   - **所有拼团表字段均无差异**，根据"表无差异不动代码"约束，未改动任何代码。
4. 回滚说明：
   - 文档回滚：删除 `M3_D2_GROUPBUY_DOMAIN_BASELINE.md`

---

除非你明确指定新的规则，否则本文件是 `LaikeAPI` 数据库迁移工作的统一执行口径。
