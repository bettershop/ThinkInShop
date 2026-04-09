ALTER TABLE `lkt_product_list`
    ADD COLUMN `write_off_settings` int(4) NULL COMMENT '核销设置 1.线下核销 2.无需核销' AFTER `lower_image`,
ADD COLUMN `write_off_mch_ids` varchar(100) NULL COMMENT '核销门店id  0全部门店,  1,2,3使用逗号分割' AFTER `write_off_settings`,
ADD COLUMN `is_appointment` int(4) NULL COMMENT '预约时间设置 1.无需预约下单 2.需要预约下单' AFTER `write_off_mch_ids`;

ALTER TABLE `lkt_product_list`
    MODIFY COLUMN `write_off_mch_ids` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 0 COMMENT '核销门店id  0全部门店,  1,2,3使用逗号分割' AFTER `write_off_settings`;


ALTER TABLE `lkt_product_list`
    MODIFY COLUMN `is_appointment` int(4) NULL DEFAULT 1 COMMENT '预约时间设置 1.无需预约下单 2.需要预约下单' AFTER `write_off_mch_ids`;

ALTER TABLE `lkt_configure`
    ADD COLUMN `write_off_num` int(11) NULL DEFAULT 0 COMMENT '虚拟商品可核销次数(暂时未使用，使用的总库存)' AFTER `supplier_superior`;


CREATE TABLE `lkt_mch_store_write`  (
                                     `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                     `store_id` int(11) NOT NULL DEFAULT 0 COMMENT '商城id',
                                     `mch_id` int(11) NOT NULL DEFAULT 0 COMMENT '店铺ID',
                                     `mch_store_id` int(11) NOT NULL COMMENT '门店id',
                                     `start_time` timestamp NULL COMMENT '开始时间',
                                     `end_time` timestamp NULL COMMENT '结束时间',
                                     `write_off_num` int(11) NULL DEFAULT 0 COMMENT '虚拟商品可核销次数 0无限制',
                                     `add_time` timestamp NULL COMMENT '添加时间',
                                     PRIMARY KEY (`id`)
);

ALTER TABLE `lkt_mch_store_write`
    MODIFY COLUMN `write_off_num` int(11) NULL DEFAULT 0 COMMENT '可预约核销次数 0无限制' AFTER `end_time`,
    ADD COLUMN `off_num` int(11) NULL COMMENT '已预约核销次数' AFTER `write_off_num`;

ALTER TABLE `lkt_mch_store_write`
    MODIFY COLUMN `off_num` int(11) NULL DEFAULT 0 COMMENT '已预约核销次数' AFTER `write_off_num`;

ALTER TABLE `lkt_order`
    ADD COLUMN `store_write_time` varchar(50) NULL COMMENT '虚拟商品预约时间' AFTER `pick_up_store`;
