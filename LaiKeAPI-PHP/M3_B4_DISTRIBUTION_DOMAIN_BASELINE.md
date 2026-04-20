# M3_B4_DISTRIBUTION_DOMAIN_BASELINE.md - 分销佣金域基线对齐文档

## 1. 业务目标
确保 `LaikeAPI (PHP)` 与 `thinkinshop (Java)` 在分销佣金计算、分销等级管理、提现链路上的数据库口径 100% 对齐。

## 2. 核心表清单及对齐状态

| 表名 | PHP 使用 | Java 基准 | 对齐状态 | 说明 |
| :--- | :---: | :---: | :--- | :--- |
| `lkt_distribution_config` | ✅ | ✅ | **无需改造** | 分销开关/广告配置，字段完全一致 |
| `lkt_distribution_goods` | ✅ | ✅ | **无需改造** | 分销商品 PV/规则配置，字段完全一致 |
| `lkt_distribution_grade` | ✅ | ✅ | **无需改造** | 分销商等级/折扣，字段完全一致 |
| `lkt_distribution_record` | ✅ | ✅ | **无需改造** | 佣金记录，核心字段 `money/type/status/level/genre` 与 Java 对齐 |
| `lkt_distribution_log` | ✅ | ✅ | **无需改造** | 分销商信息修改日志，字段完全一致 |
| `lkt_distribution_ranking` | ✅ | ✅ | **无需改造** | 分佣排行，字段完全一致 |
| `lkt_distribution_withdraw` | ✅ | ✅ | **无需改造** | 提现申请，字段完全一致 |
| `lkt_user_distribution` | ✅ | ✅ | **无需改造** | 用户分销关系，字段完全一致 |
| `lkt_distribution_income` | ❌ | ✅ | **Java 独有** | Java 侧新增的分销收益日报表（store_id/user_id/estimated_income/order_num 等），PHP 不依赖此表 |

## 3. 关键业务逻辑对齐状态

### 3.1 佣金计算 (`lkt_distribution_record` 写入)
- **PHP 实现**：`Distribution.php` -> `payment()` -> `$action->order_distribution($pid, $sNo)` -> 写入 `lkt_distribution_record`
- **Java 实现**：`PluginsDistributionAdminServiceImpl` / `PluginsDistributionCategoryServiceImpl`
- **对齐结论**：✅ 核心写入字段 (`money`, `type=1`, `status`, `level`, `genre`) 完全对齐

### 3.2 分销等级折扣
- **PHP 实现**：`Distribution.php` -> `get_distribution_level()` -> 查 `lkt_user_distribution` + `lkt_distribution_grade`
- **对齐结论**：✅ `level`, `discount`, `sets` (JSON) 结构对齐

### 3.3 提现审批
- **PHP 实现**：`lkt_distribution_withdraw` -> `status` (0审核中/1通过/2拒绝)
- **Java 实现**：同上
- **对齐结论**：✅ 状态位完全对齐

## 4. 差异说明

### 4.1 `lkt_distribution_income` (Java 独有)
- **用途**：分销收益统计日报，记录每个分销商每日的预估收益 (`estimated_income`)、有效订单数 (`order_num`)、新增客户 (`new_customer`) 等。
- **PHP 侧**：不读写此表。PHP 的分销收益由 `lkt_distribution_record` 实时计算汇总。
- **影响范围**：无影响。Java 侧可独立维护此表。

## 5. 改造执行记录 (M3-B4)
- **结论**：根据"表无差异则不动代码"约束，本次不对 PHP 代码做任何改造。
- **变更文件**：仅新建本基线文档 `M3_B4_DISTRIBUTION_DOMAIN_BASELINE.md`。
- **代码改动**：0 个文件。

## 6. 后续建议
- 如需将 PHP 分销收益也写入 `lkt_distribution_income`（实现双侧同步统计），可启动专项改造。
- 建议定期核查两端的 `lkt_distribution_record.type` 口径是否保持一致（1:转入/收入, 2:提现, 8:积分获奖, 9:转出）。

---
*最后更新：2026-04-17*
*执行约束：表无差异不动代码，仅有明显逻辑错误才改造*
