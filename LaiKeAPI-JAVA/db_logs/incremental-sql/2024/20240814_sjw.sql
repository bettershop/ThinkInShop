ALTER TABLE `lkt_mch`
    ADD COLUMN `is_self_delivery` int(11) NOT NULL DEFAULT 0 COMMENT '是否支持商家自配 0.否 1.是' AFTER `sub_app_id`;
CREATE TABLE `lkt_self_delivery_info`  (
                                     `id` int NOT NULL AUTO_INCREMENT,
                                     `delivery_time` varchar(255) NULL COMMENT '配送时间',
                                     `delivery_period` varchar(255) NULL COMMENT '配送时间段 1为上午2为下午',
                                     `phone` varchar(255) NULL COMMENT '配送人电话',
                                     `courier_name` varchar(255) NULL COMMENT '配送人姓名',
                                     PRIMARY KEY (`id`)
);
ALTER TABLE `lkt_order`
    MODIFY COLUMN `self_lifting` tinyint(4) NULL DEFAULT 0 COMMENT '自提 0.配送 1.自提 2.商家自配 3.虚拟订单需要线下核销 4.虚拟订单无需线下核销' AFTER `remarks`;
ALTER TABLE `lkt_order_details`
    ADD COLUMN `store_self_delivery` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商家自配信息id' AFTER `store_coupon_price`;
ALTER TABLE `lkt_self_delivery_info`
    MODIFY COLUMN `courier_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '配送人姓名' AFTER `phone`;