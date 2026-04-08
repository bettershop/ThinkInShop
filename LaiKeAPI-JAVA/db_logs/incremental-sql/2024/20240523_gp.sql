ALTER TABLE `lkt_order`
    ADD COLUMN `wx_order_status` int(11) NULL COMMENT '微信小程序订单状态枚举：(1) 待发货；(2) 已发货；(3) 确认收货；(4) 交易完成；(5) 已退款。' AFTER `transaction_id`;