# M3_B3_REFUND_DOMAIN_BASELINE.md - 售后与退款域基线对齐文档

## 1. 业务目标
统一 `LaikeAPI (PHP)` 与 `thinkinshop (Java)` 在售后申请审核、退款金额计算及资产（余额、积分、优惠券）回滚上的逻辑。

## 2. 退款金额口径 (Refund Amount Standard)
- **计算公式**: `最大可退金额 = 订单详情.after_discount (实付) + 订单详情.freight (运费) + 加购商品金额`。
- **Java 基准**: `PublicRefundServiceImpl.java` 中 `maxTuiMoney = goodsPayPrice.add(freight)`。
- **PHP 对齐**: `RefundUtils.php` 需确保在计算默认退款金额时包含运费，且上限校验逻辑一致。

## 3. 资产回滚规则 (Asset Rollback Rules)

### 3.1 优惠券回退
- **规则**: 
    - 如果是整单退款（主单下所有子单均已退款或正在退当前最后一单），则回退主单使用的优惠券。
    - 如果是部分退款，不回退优惠券。
- **状态位**: `lkt_coupon_res.status` 恢复为 `0` (未使用)。

### 3.2 积分回退
- **规则**: 退还该商品下单时抵扣的积分 (`lkt_order_details.score_deduction`)。

## 4. 逻辑对齐清单

### L1: 虚拟商品退款
- **规则**: 虚拟商品退款需回滚 `after_write_off_num` (剩余核销次数)，并扣减已核销金额。
- **Java 实现**: `viReturnOrderHandle` 方法。

### L2: 分销佣金扣除
- **规则**: 发生退款时，必须标记对应分销记录为失效或扣回已发放佣金。

## 5. 改造执行记录 (M3-B3-A)
- [ ] 修正 PHP `RefundUtils.php` 中的 `maxTuiMoney` 计算逻辑，引入运费退还支持。
- [ ] 统一 PHP 侧售后状态码映射，确保 `4, 9, 13` 等成功状态在 Java 侧能被正确识别为 `7` (已关闭)。

---
*最后更新：2026-04-14*
