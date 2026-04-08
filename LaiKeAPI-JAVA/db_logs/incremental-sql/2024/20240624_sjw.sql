ALTER TABLE `lkt_order_details`
    ADD COLUMN `write_off_num` int(10) NULL COMMENT '虚拟商品可核销次数' AFTER `commission`;
ALTER TABLE `lkt_order_details`
    MODIFY COLUMN `write_off_num` int(10) NULL DEFAULT NULL COMMENT '虚拟商品待核销次数' AFTER `commission`,
    ADD COLUMN `after_write_off_num` int(10) NULL COMMENT '虚拟商品已核销次数' AFTER `write_off_num`;
ALTER TABLE `lkt_return_order`
    ADD COLUMN `r_write_off_num` int(10) NULL COMMENT '虚拟商品退款核销次数' AFTER `is_agree`;
ALTER TABLE `lkt_order`
    MODIFY COLUMN `status` tinyint(4) NULL DEFAULT 0 COMMENT '状态 0:未付款 1:未发货 2:待收货 5:已完成  7:订单关闭8:待核销' AFTER `order_failure_time`;
ALTER TABLE `lkt_order_details`
    MODIFY COLUMN `r_status` tinyint(4) NULL DEFAULT 0 COMMENT '状态 0:未付款 1:未发货 2:待收货 5:已完成  7:订单关闭 8:待核销' AFTER `arrive_time`;
ALTER TABLE `lkt_order_details`
    ADD COLUMN `mch_store_write_id` int(10) NULL COMMENT '预约门店id' AFTER `after_write_off_num`;
ALTER TABLE `lkt_order_details`
    ADD COLUMN `write_time` varchar(255) NULL COMMENT '虚拟商品预约时间' AFTER `mch_store_write_id`;
CREATE TABLE `lkt_write_record`
(
    `id`             int          NOT NULL COMMENT 'id',
    `write_store_id` varchar(255) NULL COMMENT '核销门店id',
    `write_time`     timestamp    NULL COMMENT '核销时间',
    `write_code`     varchar(255) NULL COMMENT '核销码',
    `s_no`           varchar(255) NULL COMMENT '订单号',
    PRIMARY KEY (`id`)
);
ALTER TABLE `lkt_write_record`
    MODIFY COLUMN `write_time` timestamp NULL DEFAULT NULL COMMENT '核销时间' AFTER `write_store_id`,
    COMMENT = '核销记录表';
ALTER TABLE `lkt_write_record`
    MODIFY COLUMN `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id' FIRST;
ALTER TABLE `lkt_write_record`
    ADD COLUMN `p_id` int(11) NULL COMMENT '订单详情id' AFTER `s_no`;
ALTER TABLE `lkt_write_record`
    ADD COLUMN `status` int(11) NULL COMMENT '核销状态，1为已核销，2为退款状态中' AFTER `p_id`;
ALTER TABLE `lkt_product_list`
    MODIFY COLUMN `freight` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '运费' AFTER `distributor_id`;
ALTER TABLE `lkt_write_record`
    MODIFY COLUMN `status` int(11) NULL DEFAULT NULL COMMENT '核销状态，1为已核销完，2为退款状态中，3为还有核销次数' AFTER `p_id`;
