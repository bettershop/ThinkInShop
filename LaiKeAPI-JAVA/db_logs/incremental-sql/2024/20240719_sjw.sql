ALTER TABLE `lkt_write_record`
    MODIFY COLUMN `status` int(11) NULL DEFAULT NULL COMMENT '核销状态，1为已核销完，2为退款状态中，3为还有核销次数 , 4为退款成功' AFTER `p_id`;
ALTER TABLE `lkt_order`
    ADD COLUMN `cancel_method` int(2) NULL DEFAULT 0 COMMENT '订单取消方式 0 定时任务取消订单  1 手动点击取消订单' AFTER `wx_order_status`;