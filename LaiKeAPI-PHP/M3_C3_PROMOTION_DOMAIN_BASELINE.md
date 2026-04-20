# M3_C3_PROMOTION_DOMAIN_BASELINE.md - 商品促销域基线对齐文档

## 1. 业务目标
确保 `LaikeAPI (PHP)` 与 `thinkinshop (Java)` 在优惠券、满减、折扣等促销活动的配置与使用口径上 100% 对齐。

## 2. 核心表清单及对齐状态

| 表名 | PHP 使用 | Java 基准 | 对齐状态 |
| :--- | :---: | :---: | :--- |
| `lkt_coupon` | ✅ | ✅ | **无需改造** |
| `lkt_coupon_activity` | ✅ | ✅ | **无需改造** |
| `lkt_coupon_record` | ✅ | ✅ | **无需改造** |
| `lkt_full_reduction` | ✅ | ✅ | **无需改造** |
| `lkt_floor_price` | ✅ | ✅ | **无需改造** |
| `lkt_special` | ✅ | ✅ | **无需改造** |

## 3. 字段对齐验证结果

通过 `db_full_alignment_check.sh` 扫描，**所有促销相关表字段均无差异**。具体验证：
- `diff_columns_local_only.tsv`：无 coupon/floor/special/full_reduction 相关条目
- `diff_columns_v3_only.tsv`：无 coupon/floor/special/full_reduction 相关条目

## 4. 关键业务逻辑对齐状态

### 4.1 优惠券类型 (`activity_type`)
- `1=免邮 2=满减 3=折扣 4=会员赠送`
- 两端完全一致

### 4.2 优惠券状态 (`lkt_coupon.type`)
- `0:未使用 1:使用中 2:已使用 3:已过期`
- 两端完全一致

### 4.3 使用范围 (`lkt_coupon_activity.type`)
- `1：全部商品 2:指定商品 3：指定分类 4：充值会员`
- 两端完全一致

## 5. 改造执行记录 (M3-C3)
- **结论**：根据"表无差异不动代码"约束，本次不对 PHP/Java 代码做任何改造。
- **变更文件**：仅新建本基线文档 `M3_C3_PROMOTION_DOMAIN_BASELINE.md`。
- **代码改动**：0 个文件。

---
*最后更新：2026-04-17*
*执行约束：表无差异不动代码，仅有明显逻辑错误才改造*
