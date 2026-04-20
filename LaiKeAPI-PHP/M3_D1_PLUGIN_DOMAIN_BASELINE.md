# M3_D1_PLUGIN_DOMAIN_BASELINE.md - 插件域基线对齐文档

## 1. 业务目标
统一各营销插件（秒杀、拼团、竞拍、满减、优惠券等）在 `LaikeAPI (PHP)` 与 `thinkinshop (Java)` 中的数据结构与核心状态流转逻辑。

## 2. 核心插件表清单

### 2.1 秒杀 (Seconds)
- `lkt_seconds_pro`: 秒杀商品
- `lkt_seconds_activity`: 秒杀活动
- `lkt_seconds_record`: 秒杀记录

### 2.2 竞拍 (Auction)
- `lkt_auction_product`: 竞拍商品
- `lkt_auction_session`: 竞拍场次
- `lkt_auction_special`: 竞拍专题
- `lkt_auction_record`: 出价记录
- `lkt_auction_promise`: 保证金记录

### 2.3 拼团 (Group Buy)
- `lkt_pt_go_group_order`: 拼团订单
- `lkt_pt_seconds_pro`: 拼团商品

## 3. 状态字典对齐

### 3.1 竞拍商品状态 (`lkt_auction_product.status`)

| 状态位 | PHP 定义 | Java 定义 | 对齐策略 |
| :--- | :--- | :--- | :--- |
| `0` | 待开始 | 待开始 | 已对齐 |
| `1` | 进行中 | 进行中 | 已对齐 |
| `2` | 已结束 | 已结束 | 已对齐 |
| `3` | 已失效/删除 | 已失效 | 已对齐 |

## 4. 逻辑对齐清单

### L1: 秒杀价格精度对齐
- **描述**：PHP 侧在计算秒杀价时可能存在舍入差异。
- **对齐要求**：统一采用 `round(price, 2)`。

### L2: 拼团人数与状态对齐
- **描述**：拼团成功的判定逻辑。
- **对齐要求**：PHP 与 Java 需统一使用 `lkt_pt_go_group_order.ptstatus = 2` 表示拼团成功。

## 5. 改造执行记录 (M3-D1-A)
- [ ] 审计并对齐 `app/common/Plugin/Auction.php` 状态流转。
- [ ] 审计并对齐 `app/common/Plugin/GroupBuy.php` 拼团成功逻辑。

---
*最后更新：2026-04-14*
