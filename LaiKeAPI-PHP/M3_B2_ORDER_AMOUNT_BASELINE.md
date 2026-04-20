# M3_B2_ORDER_AMOUNT_BASELINE.md - 订单金额计算域基线对齐文档

## 1. 业务目标
统一 `LaikeAPI (PHP)` 与 `thinkinshop (Java)` 在订单结算（Settlement）与下单（Payment）阶段的金额计算逻辑，确保在多优惠（会员折扣、满减、优惠券、积分抵扣）叠加场景下，实付金额（total）在两端完全一致。

## 2. 计算优先级 (Order of Operations)
两端必须遵循以下计算顺序：

1.  **商品总价 (Products Total)**: 所有商品 `price * num` 的累加。
2.  **会员折扣 (Member Discount)**: 基于用户等级计算会员价，得出 `grade_rate_amount`。
3.  **运费 (Freight)**: 根据收货地址和运费模板计算 `yunfei`。
4.  **店铺优惠 (Mch Coupon)**: 应用店铺级优惠券/满减。
5.  **平台优惠 (Platform Coupon)**: 应用平台级优惠券/满减。
6.  **积分抵扣 (Score Deduction)**: 最后应用积分抵扣金额。

**最终公式**:
`实付金额 (total) = (商品总价 - 会员折扣 - 店铺优惠 - 平台优惠) + 运费 - 积分抵扣`

## 3. 精度与舍入规则
-   **存储精度**: 所有金额字段在数据库中应为 `decimal(12,2)`。
-   **计算精度**: 
    -   PHP 侧使用 `round($total, 2)`。
    -   Java 侧使用 `BigDecimal` 并在涉及除法或比例时显式指定 `RoundingMode.HALF_UP` (除非业务特殊要求)。
-   **最小支付金额**:
    -   非钱包支付：最小为 `0.01`。
    -   钱包支付：允许为 `0.00` (与 Java 保持一致，PHP 需修正)。

## 4. 核心逻辑对齐要求

### L1: 积分抵扣上限
- **规则**：积分抵扣金额不得超过 `(商品总价 - 所有优惠)`，即运费部分不允许被积分抵扣。
- **Java 实现**：`maxTotal = payTotal.subtract(yunfei)`。
- **PHP 现状**：未严格限制，需补齐。

### L2: 虚拟订单状态与金额
- **规则**：虚拟订单（self_lifting=3,4）运费强制为 0。

## 5. 改造执行记录 (M3-B2-A)
- [ ] 修正 PHP `NormalOrder.php` 中的最小支付金额逻辑（区分钱包支付）。
- [ ] 在 PHP 侧增加积分抵扣不得抵扣运费的校验。
- [ ] 审计 `Tools::get_products_data0` 确保会员价计算公式与 Java 一致。

---
*最后更新：2026-04-14*
