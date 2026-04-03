ALTER TABLE `lkt_configure`
    ADD COLUMN `write_off_num` int(11) NULL DEFAULT 0 COMMENT '虚拟商品可核销次数(暂时未使用，使用的总库存)' AFTER `supplier_superior`;

ALTER TABLE `lkt_product_list`
    ADD COLUMN `write_off_settings` int(4) NULL COMMENT '核销设置 1.线下核销 2.无需核销' AFTER `lower_image`,
    ADD COLUMN `write_off_mch_ids` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 0 COMMENT '核销门店id  0全部门店,  1,2,3使用逗号分割' AFTER `write_off_settings`,
    ADD COLUMN `is_appointment` int(4) NULL DEFAULT 1 COMMENT '预约时间设置 1.无需预约下单 2.需要预约下单' AFTER `write_off_mch_ids`,
    MODIFY COLUMN `freight` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '运费' AFTER `distributor_id`;

CREATE TABLE `lkt_mch_store_write`  (
    `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `store_id` int(11) NOT NULL DEFAULT 0 COMMENT '商城id',
    `mch_id` int(11) NOT NULL DEFAULT 0 COMMENT '店铺ID',
    `mch_store_id` int(11) NOT NULL COMMENT '门店id',
    `start_time` timestamp NULL COMMENT '开始时间',
    `end_time` timestamp NULL COMMENT '结束时间',
    `period` varchar(255) NULL COMMENT '默认时间段',
    `write_off_num` int(11) NULL DEFAULT 0 COMMENT '可预约核销次数 0无限制',
    `off_num` int(11) NULL DEFAULT 0 COMMENT '已预约核销次数',
    `add_time` timestamp NULL COMMENT '添加时间',
    `recycle` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
)ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='虚拟商品订单核销表';

ALTER TABLE `lkt_order`
    ADD COLUMN `store_write_time` varchar(50) NULL COMMENT '虚拟商品预约时间' AFTER `pick_up_store`,
    MODIFY COLUMN `status` tinyint(4) NULL DEFAULT 0 COMMENT '状态 0:未付款 1:未发货 2:待收货 5:已完成 7:订单关闭 8:待核销' AFTER `order_failure_time`,
    MODIFY COLUMN `self_lifting` tinyint(4) NULL DEFAULT 0 COMMENT '自提 0.配送 1.自提 3.虚拟订单需要线下核销 4.虚拟订单无需线下核销' AFTER `remarks`;

ALTER TABLE  `lkt_order_details`
    ADD COLUMN `living_room_id` varchar(255) NULL COMMENT '直播间id' AFTER `third_party_json`,
    ADD COLUMN `anchor_id` varchar(255) NULL COMMENT '主播id' AFTER `living_room_id`,
    ADD COLUMN `commission` decimal(12,2) NULL DEFAULT '0.00' COMMENT '金' AFTER `anchor_id`,
    ADD COLUMN `write_off_num` int(10) NULL COMMENT '虚拟商品待核销次数' AFTER `commission`,
    ADD COLUMN `after_write_off_num` int(10) NULL COMMENT '虚拟商品已核销次数' AFTER `write_off_num`,
    MODIFY COLUMN `r_status` tinyint(4) NULL DEFAULT 0 COMMENT '状态 0:未付款 1:未发货 2:待收货 5:已完成  7:订单关闭 8:待核销' AFTER `arrive_time`,
    ADD COLUMN `mch_store_write_id` int(10) NULL COMMENT '预约门店id' AFTER `after_write_off_num`,
    ADD COLUMN `write_time` varchar(255) NULL COMMENT '虚拟商品预约时间' AFTER `mch_store_write_id`,
    ADD COLUMN `write_time_id` int(10) NULL COMMENT '预约时段id' AFTER `write_time`;

ALTER TABLE  `lkt_return_order`
    ADD COLUMN `r_write_off_num` int(10) NULL COMMENT '虚拟商品退款核销次数' AFTER `is_agree`;

ALTER TABLE  `lkt_record_details`
    MODIFY COLUMN `money_type_name` tinyint(4) NULL DEFAULT NULL COMMENT '收入/支出 名称   1.用户充值 2.平台充值 3.拼团活动开团佣金 4.普通订单 5.竞拍活动 6.店铺保证金 7.余额扣款 8.预售定金缴纳  9.会员开通 10.普通订单(代客下单) 11.拼团订单 12.竟拍订单 13.预售订单 14.分稍订单 15.秒杀订单 16.积分兑换订单 17.限时折扣订单 18.竞拍保证金缴纳 19.虚拟订单' AFTER `money_type`;

CREATE TABLE  `lkt_write_record`  (
    `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
    `write_store_id` varchar(255) NULL COMMENT '核销门店id',
    `write_time` timestamp NULL DEFAULT NULL COMMENT '核销时间',
    `write_code` varchar(255) NULL COMMENT '核销码',
    `s_no` varchar(255) NULL COMMENT '订单号',
    `p_id` int(11) NULL COMMENT '订单详情id',
    `status` int(11) NULL DEFAULT NULL COMMENT '核销状态 1.为已核销完 2.为退款状态中 3.为还有核销次数',
    PRIMARY KEY (`id`)
)ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='核销记录表';



ALTER TABLE `lkt_mch_store_write` 
MODIFY COLUMN `off_num` varchar(255) NULL COMMENT '已预约核销次数' AFTER `write_off_num`;

ALTER TABLE `lkt_order_details` 
ADD COLUMN `platform_coupon_price` decimal(12, 2) NULL COMMENT '平台优惠券优惠金额' AFTER `write_time_id`,
ADD COLUMN `store_coupon_price` decimal(12, 2) NULL COMMENT '店铺优惠券优惠金额' AFTER `platform_coupon_price`;


