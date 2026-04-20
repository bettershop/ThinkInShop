# M3_C2_PRODUCT_INVENTORY_BASELINE.md - 商品库存域基线对齐文档

## 1. 业务目标
统一 `LaikeAPI (PHP)` 与 `thinkinshop (Java)` 在商品下单扣减、取消返还、售后退回场景下的库存处理逻辑。确保在高并发场景下数据最终一致，并解决代售/多门店关联库存的同步问题。

## 2. 库存操作规范

### 2.1 扣减时机
- **普通订单**: 支付成功后扣减库存（PHP `Order.php -> up_order` 调用 `Modify_inventory`）。
- **秒杀/高频订单**: 下单时预扣 Redis 库存，支付回调后异步或同步更新 DB 库存。

### 2.2 核心算法
- **原子扣减**: 两端必须使用数据库原子操作 `SET num = num - ?`，严禁“查出-计算-写回”模式。
- **关联同步**: 
    - **代售商品**: 扣减子店商品库存时，必须同步扣减供应商（上级）商品库存。
    - **衍射商品**: PHP 中的 `commodity_str` 关联逻辑需与 Java 的 `synchronizationOtherStock` 保持口径一致。

## 3. 字段映射与状态位

| 功能项 | 字段/表 | 说明 |
| :--- | :--- | :--- |
| 当前库存 | `lkt_configure.num` | 规格级库存，对齐重点 |
| 总库存 | `lkt_product_list.num` | 商品级汇总库存 |
| 销量记录 | `lkt_product_list.real_volume` | 支付后增加，退款后扣减 |
| 流水记录 | `lkt_stock` | 记录每一次出入库详情 |

## 4. 逻辑对齐清单

### L1: 关联库存同步 (Linked Inventory)
- **描述**: PHP 侧通过 `unserialize(commodity_str)` 更新关联商品。
- **Java 现状**: 采用 `supplier_superior` 链式同步。
- **对齐要求**: 确保 PHP 在 `Modify_inventory` 中处理供应商商品时，正确更新 `supplier_superior` 对应的记录。

### L2: 库存预警触发
- **描述**: 当 `num <= min_inventory` 时触发。
- **对齐要求**: 两端统一写入 `lkt_stock` 类型为 `2` (预警)，并向 `lkt_message_logging` 写入提醒。

## 5. 改造执行记录 (M3-C2-A)
- [ ] 审计 PHP `Modify_inventory` 是否漏掉了对 `lkt_product_list.real_volume` 的退款回滚。
- [ ] 确保 PHP 侧在更新供应商库存时，使用的 SQL 条件与 Java `v3_db` 的主键定义一致。

---
*最后更新：2026-04-14*
