# M3-B1 订单域基线产物（P0）

更新时间：2026-04-14
范围：`lkt_order`、`lkt_order_details`、`lkt_return_order`、`lkt_order_data`、`lkt_express_delivery`

## 1. Gate 与扫描基线

1. 前置同步口径：
   - 本轮按计划延续 `M3` 迁移推进，当前工作以静态扫描和口径固化为主。
   - 当前仓库已按要求执行 `svn update`，后续若继续动代码，仍需先确认 `svn status` 再改。
2. 订单域核心入口已完成初扫：
   - `app/admin/controller/app/Order.php`
   - `app/admin/controller/mall/Order.php`
   - `app/common/DeliveryHelper.php`
   - `app/common/Order.php`
   - `app/admin/controller/app/Pay.php`
   - `app/admin/controller/admin/Order.php`
3. 订单域高频方法已定位：
   - C 端：`settlement/payment/index/order_details/return_method/delivery_delivery`
   - PC 端：`settlement/payment/index/orderDetails/cancellationOfApplication/returnDetails/returnDataList`
4. 本批为“基线整理版”：
   - 先固定表语义、接口入口、读写点和回归清单
   - 暂不做订单逻辑改写

## 2. 统一状态字典（草案）

说明：以下口径以当前 PHP 订单主链路扫描结果为准，后续若 Java 侧存在更细枚举，再做补充映射，但不先改外部返回语义。

### 2.1 `lkt_order.status` / `lkt_order_details.r_status`

| 状态码 | 当前语义 | 主要出现位置 |
|---|---|---|
| `0` | 待付款 | 订单列表、售后按钮、筛选 |
| `1` | 待发货 | 付款成功后的配送订单、发货前状态 |
| `2` | 待收货 | 已发货/待用户确认收货 |
| `5` | 已完成 | 自动收货、确认收货、结算完成 |
| `7` | 已关闭 | 取消、关闭、不可售后 |
| `8` | 待核销 | 自提/线下核销/虚拟订单分支的扩展态 |

### 2.2 `lkt_order.review_status`

| 状态码 | 语义 |
|---|---|
| `0` | 未上传凭证 |
| `1` | 待审核 |
| `2` | 通过 |
| `3` | 拒绝 |

### 2.3 `lkt_order.self_lifting`

| 状态码 | 语义 |
|---|---|
| `0` | 配送 |
| `1` | 自提 |
| `3` | 虚拟订单需要线下核销 |
| `4` | 虚拟订单无需线下核销 |

### 2.4 状态流关键点

1. 支付后状态推进：
   - 配送单：`0 -> 1`
   - 自提/线下核销单：`0 -> 2`
   - 虚拟无需核销单：`0 -> 5`
2. 发货后状态推进：
   - `lkt_order_details.r_status = 1` 的明细发货后会转为 `2`
   - 当同单全部明细完成后，父单 `lkt_order.status` 也会转为 `2`
3. 自动收货推进：
   - 定时任务会把 `r_status = 2` 的明细收束为 `5`
   - 同步把主单 `status` 收束为 `5`
4. 关闭单与售后限制：
   - `7` 会被当成关闭单处理，不再开放正常售后按钮
5. 核销分支：
   - `8` 只在部分核销/插件链路里出现，属于扩展态，不应当和普通配送态混用

## 3. 表语义映射表（旧字段 -> 新来源 -> 兼容规则）

### 2.1 `lkt_order`

| 旧字段（PHP） | 新来源（Java/v3） | 兼容规则 | 备注 |
|---|---|---|---|
| `id` | `lkt_order.id` | 自增主键保持不变 | 主键 |
| `store_id` | `lkt_order.store_id` | 全部查询/更新必须带店铺条件 | 多租户隔离 |
| `user_id` | `lkt_order.user_id` | 与 `lkt_user.user_id` 统一 | 用户主键 |
| `sNo` | `lkt_order.sNo` | 订单号沿用原字段 | 核心业务键 |
| `real_sno` | `lkt_order.real_sno` | 兼容拆单/真实单号语义 | 关联流水 |
| `status` | `lkt_order.status` | 状态码不改，沿用既有枚举 | 主状态 |
| `otype` | `lkt_order.otype` | 订单类型不改 | `GM/JP/...` |
| `pay` | `lkt_order.pay` | 支付方式字符串兼容 | 支付口径 |
| `z_price` | `lkt_order.z_price` | 价格两位小数语义不变 | 订单总额 |
| `pay_time` | `lkt_order.pay_time` | 时间字段原样保留 | 支付时间 |
| `add_time` | `lkt_order.add_time` | 创建时间原样保留 | 下单时间 |
| `name` | `lkt_order.name` | 收货人字段保留 | 展示字段 |
| `mobile` | `lkt_order.mobile` | 联系方式原样保留 | 展示字段 |
| `address` | `lkt_order.address` | 详细地址原样保留 | 展示字段 |
| `mch_id` | `lkt_order.mch_id` | 维持商家串联语义 | 多商家订单 |
| `self_lifting` | `lkt_order.self_lifting` | 0/1 语义不变 | 自提 |
| `review_status` | `lkt_order.review_status` | 保留评论状态枚举 | 订单评价 |
| `voucher` | `lkt_order.voucher` | 提货核销码原样保留 | 到店/核销场景 |
| `capture_id` | `lkt_order.capture_id` | PayPal 等支付回调保留 | 支付捕获 |
| `extraction_code` | `lkt_order.extraction_code` | 核销码保留 | 验证场景 |
| `extraction_code_img` | `lkt_order.extraction_code_img` | 图片码保留 | 核销场景 |
| `currency_symbol` | `lkt_order.currency_symbol` | 币种展示不改 | 金额展示 |
| `currency_code` | `lkt_order.currency_code` | 币种代码不改 | 国际化 |
| `exchange_rate` | `lkt_order.exchange_rate` | 汇率口径不改 | 跨币种 |
| `delivery_status` | `lkt_order.delivery_status` | 兼容确认收货状态 | PC 订单流 |
| `p_sNo` | `lkt_order.p_sNo` | 父单号/拆单号保留 | 预售/子单 |

### 2.2 `lkt_order_details`

| 旧字段（PHP） | 新来源（Java/v3） | 兼容规则 | 备注 |
|---|---|---|---|
| `id` | `lkt_order_details.id` | 自增主键保持不变 | 明细主键 |
| `store_id` | `lkt_order_details.store_id` | 必须带店铺条件 | 多租户隔离 |
| `r_sNo` | `lkt_order_details.r_sNo` | 关联订单号保留 | 订单关联键 |
| `p_id` | `lkt_order_details.p_id` | 商品主键原样保留 | 商品关联 |
| `sid` | `lkt_order_details.sid` | SKU/规格主键原样保留 | SKU 关联 |
| `p_name` | `lkt_order_details.p_name` | 商品名快照保留 | 下单快照 |
| `p_price` | `lkt_order_details.p_price` | 单价两位小数语义不变 | 金额字段 |
| `num` | `lkt_order_details.num` | 数量整型不变 | 件数 |
| `unit` | `lkt_order_details.unit` | 计量单位原样保留 | 展示字段 |
| `size` | `lkt_order_details.size` | 规格文本保留 | 展示字段 |
| `r_status` | `lkt_order_details.r_status` | 状态枚举不改 | 发货/售后状态 |
| `deliver_num` | `lkt_order_details.deliver_num` | 发货数量累计口径不改 | 分批发货 |
| `deliver_time` | `lkt_order_details.deliver_time` | 时间字段原样保留 | 发货时间 |
| `re_type` | `lkt_order_details.re_type` | 售后类型保留 | 退款/退货 |
| `re_apply_money` | `lkt_order_details.re_apply_money` | 申请退款金额保留 | 售后口径 |
| `re_photo` | `lkt_order_details.re_photo` | 图片证据保留 | 售后材料 |
| `re_time` | `lkt_order_details.re_time` | 售后时间保留 | 记录时间 |
| `real_money` | `lkt_order_details.real_money` | 实退金额保留 | 退款结算 |
| `audit_time` | `lkt_order_details.audit_time` | 审核时间保留 | 审核流 |
| `r_content` | `lkt_order_details.r_content` | 售后说明保留 | 文本 |
| `freight` | `lkt_order_details.freight` | 运费保留 | 金额字段 |
| `is_addp` | `lkt_order_details.is_addp` | 追加商品标记不变 | 补购场景 |
| `exchange_num` | `lkt_order_details.exchange_num` | 兑换数量保留 | 活动/核销 |
| `write_off_num` | `lkt_order_details.write_off_num` | 核销数量保留 | 线下核销 |
| `after_write_off_num` | `lkt_order_details.after_write_off_num` | 核销后数量保留 | 核销累计 |
| `mch_id` | `lkt_order_details.mch_id` | 商家归属保留 | 多商家场景 |
| `living_room_id` | `lkt_order_details.living_room_id` | 直播订单关联保留 | 直播域依赖 |
| `anchor_id` | `lkt_order_details.anchor_id` | 主播关联保留 | 直播域依赖 |
| `store_self_delivery` | `lkt_order_details.store_self_delivery` | 门店自提关联保留 | 自提链路 |
| `recycle` | `lkt_order_details.recycle` | 回收/删除语义不变 | 历史兼容 |
| `settlement_type` | `lkt_order_details.settlement_type` | 结算口径不改 | 财务链路 |
| `write_time` | `lkt_order_details.write_time` | 核销时间保留 | 到店核销 |
| `write_code` | `lkt_order_details.write_code` | 核销码保留 | 核销场景 |
| `mch_store_write_id` | `lkt_order_details.mch_store_write_id` | 商家门店核销关联保留 | 核销场景 |
| `p_integral` | `lkt_order_details.p_integral` | 积分字段保留 | 积分商品 |
| `score_deduction` | `lkt_order_details.score_deduction` | 积分抵扣保留 | 金额口径 |
| `after_discount` | `lkt_order_details.after_discount` | 折后金额保留 | 列表展示 |

### 2.3 关联表

| 表 | 作用 | 兼容规则 |
|---|---|---|
| `lkt_return_order` | 售后申请/退款/退货主表 | 状态码与类型码按现有字典保留 |
| `lkt_order_data` | 支付与扩展交易数据 | 作为支付辅助表，不改协议 |
| `lkt_express_delivery` | 发货明细与物流记录 | 分批发货、物流记录保留历史语义 |

## 4. 订单域 SQL 读写点清单（核心）

说明：本节仅列改造优先级最高的入口，便于后续逐条收敛。

| 表 | 文件与行 | 影响接口(apiKey) | 读/写 | SQL片段/行为 |
|---|---|---|---|---|
| `lkt_order` | `app/admin/controller/app/Order.php:72` | `app.order.settlement` | 读 | 下单前确认页入口 |
| `lkt_order` | `app/admin/controller/app/Order.php:154` | `app.order.payment` | 写 | 生成订单入口 |
| `lkt_order` | `app/admin/controller/app/Order.php:304` | `app.order.index` | 读 | 订单列表入口 |
| `lkt_order_details` | `app/admin/controller/app/Order.php:750` | `app.order.order_details0` | 读 | 订单明细入口 |
| `lkt_order_details` | `app/admin/controller/app/Order.php:764` | `app.order.order_details` | 读 | 订单明细入口 |
| `lkt_return_order` | `app/admin/controller/app/Order.php:1612` | `app.order.return_method` | 读/写 | 售后入口 |
| `lkt_order_details` | `app/admin/controller/app/Order.php:1943` | `app.order.delivery_delivery` | 读/写 | 确认收货/履约推进 |
| `lkt_order` | `app/admin/controller/mall/Order.php:71` | `mall.order.settlement` | 读 | PC 确认订单页 |
| `lkt_order` | `app/admin/controller/mall/Order.php:137` | `mall.order.payment` | 写 | PC 生成订单 |
| `lkt_order` | `app/admin/controller/mall/Order.php:177` | `mall.order.index` | 读 | PC 订单列表 |
| `lkt_order_details` | `app/admin/controller/mall/Order.php:276` | `mall.order.orderDetails` | 读 | PC 订单详情 |
| `lkt_return_order` | `app/admin/controller/mall/Order.php:691` | `mall.order.cancellationOfApplication` | 读/写 | 售后申请 |
| `lkt_return_order` | `app/admin/controller/mall/Order.php:1137` | `mall.order.returnDetails` | 读 | 售后详情 |
| `lkt_return_order` | `app/admin/controller/mall/Order.php:1510` | `mall.order.returnDataList` | 读 | 售后列表 |
| `lkt_order_details` | `app/common/DeliveryHelper.php:85` | 发货链路 | 读 | 订单与明细联查 |
| `lkt_order_details` | `app/common/DeliveryHelper.php:185` | 发货链路 | 写 | `update lkt_order_details set ...` |
| `lkt_express_delivery` | `app/common/DeliveryHelper.php:197` | 发货链路 | 写 | 写入物流明细 |
| `lkt_order` | `app/common/DeliveryHelper.php:385` | 发货/自提链路 | 写 | 更新订单状态 |
| `lkt_order` | `app/common/Order.php:96` | 拆单/子单链路 | 读 | 按 `p_sNo` 查主单 |
| `lkt_order_details` | `app/common/Order.php:183` | 分销结算链路 | 读 | `lkt_order + lkt_order_details` 联查 |
| `lkt_order` | `app/admin/controller/app/Pay.php:250` | `app.pay.*` | 读 | 读取订单金额 |
| `lkt_order_data` | `app/admin/controller/app/Pay.php:1684` | PayPal 回调链路 | 读 | 读取扩展交易数据 |
| `lkt_order` | `app/admin/controller/admin/Order.php:62` | `admin.order.*` | 读 | 后台订单详情列表 |
| `lkt_order` | `app/admin/controller/admin/Order.php:252` | `admin.order.*` | 读 | 门店/商家订单查询 |
| `lkt_order_details` | `app/admin/controller/admin/Order.php:270` | `admin.order.*` | 读 | 明细查询 |

## 5. 订单域接口清单（路径 + apiKey + 调用端）

### 4.1 C 端订单

| 接口路径 | apiKey | PHP 对应方法 | 调用端 |
|---|---|---|---|
| `/app/order/settlement` | `app.order.settlement` | `app/Order::settlement` | C端 |
| `/app/order/payment` | `app.order.payment` | `app/Order::payment` | C端 |
| `/app/order/index` | `app.order.index` | `app/Order::index` | C端 |
| `/app/order/order_details` | `app.order.order_details` | `app/Order::order_details` | C端 |
| `/app/order/return_method` | `app.order.return_method` | `app/Order::return_method` | C端 |
| `/app/order/delivery_delivery` | `app.order.delivery_delivery` | `app/Order::delivery_delivery` | C端 |

### 4.2 PC / 商城端订单

| 接口路径 | apiKey | PHP 对应方法 | 调用端 |
|---|---|---|---|
| `/mall/order/settlement` | `mall.order.settlement` | `mall/Order::settlement` | 商城端 |
| `/mall/order/payment` | `mall.order.payment` | `mall/Order::payment` | 商城端 |
| `/mall/order/index` | `mall.order.index` | `mall/Order::index` | 商城端 |
| `/mall/order/orderDetails` | `mall.order.orderDetails` | `mall/Order::orderDetails` | 商城端 |
| `/mall/order/cancellationOfApplication` | `mall.order.cancellationOfApplication` | `mall/Order::cancellationOfApplication` | 商城端 |
| `/mall/order/returnDetails` | `mall.order.returnDetails` | `mall/Order::returnDetails` | 商城端 |
| `/mall/order/returnDataList` | `mall.order.returnDataList` | `mall/Order::returnDataList` | 商城端 |

## 6. 订单域回归清单

| 用例 | 接口 | 校验点 | 状态 |
|---|---|---|---|
| 确认订单页 | `app.order.settlement` / `mall.order.settlement` | 商品、优惠、地址、运费展示一致 | 待执行 |
| 生成订单 | `app.order.payment` / `mall.order.payment` | 订单号、金额、状态初始值一致 | 待执行 |
| 订单列表 | `app.order.index` / `mall.order.index` | 分页、筛选、状态标签一致 | 待执行 |
| 订单详情 | `app.order.order_details` / `mall.order.orderDetails` | 金额、物流、状态展示一致 | 待执行 |
| 售后申请 | `app.order.return_method` / `mall.order.cancellationOfApplication` | 售后类型、退款金额、状态流一致 | 待执行 |
| 确认收货 | `app.order.delivery_delivery` | 状态推进与库存/记录联动一致 | 待执行 |
| 售后列表 | `mall.order.returnDataList` | 列表与详情字段语义一致 | 待执行 |

## 7. 当前风险与后续改造建议

1. 风险：订单域跨表依赖很多，尤其是 `lkt_order`、`lkt_order_details`、`lkt_return_order` 与发货链路同时参与，改动面容易外溢。
2. 风险：C 端与 PC 端存在同名不同实现的历史代码，先统一字段和状态字典，再考虑微调逻辑。
3. 风险：支付回调、发货、自提、售后会共享订单状态，必须先固定状态字典后再改写流程。
4. 建议：M3-B1 先做“字段映射 + 接口清单 + 状态字典”三件事，不直接改支付和售后核心逻辑。

## 8. 本批产物文件

1. 本文档：`M3_B1_ORDER_DOMAIN_BASELINE.md`
2. 后续建议补充的扫描产物：
   - `runtime/m3_b1_order_domain/php_order_refs.txt`
   - `runtime/m3_b1_order_domain/php_delivery_refs.txt`
   - `runtime/m3_b1_order_domain/php_pay_refs.txt`
   - `runtime/m3_b1_order_domain/php_admin_order_refs.txt`
   - `runtime/m3_b1_order_domain/php_order_status_dictionary.txt`

## 9. 变更记录

### 记录 1（2026-04-17）

1. 迁移项：`M3-B1 订单域状态字典对齐（self_lifting=3 分支）`
2. 变更文件：
   - `app/common/Order.php`
   - `app/admin/controller/mall/Pay.php`
   - `app/admin/controller/admin/Order.php`
   - `app/admin/controller/plugin/sec/Order.php`
3. 验证结果：
   - `app/common/Order.php` 中 `up_order` 方法：`self_lifting=3` 的主单 `status` 和子单 `r_status` 已统一赋值为 `8`（待核销），与 Java `PublicOrderServiceImpl.orderPayment` 逻辑对齐。
   - `app/admin/controller/mall/Pay.php` 中 `gndd` 方法：`self_lifting=3` 的明细 `r_status` 已从错误值修正为 `8`。
   - `app/admin/controller/admin/Order.php`、`app/admin/controller/plugin/sec/Order.php`：订单导出与列表中的 `self_lifting=3` 场景已补齐 `8=待核销` 的状态标签和文案。
4. 回滚说明：
   - 代码回滚：按 SVN 回退上述 4 个 PHP 文件
   - 数据回滚：如已写库，需按订单号还原 `status/r_status` 字段
