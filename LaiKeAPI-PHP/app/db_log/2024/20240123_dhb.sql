
ALTER TABLE `lkt_order_details` 
ADD COLUMN `user_recycle` tinyint(4) NULL DEFAULT 1 COMMENT '用户删除 1.显示 2.删除' AFTER `is_addp`,
ADD COLUMN `mch_recycle` tinyint(4) NULL DEFAULT 1 COMMENT '商户删除 1.显示 2.删除' AFTER `user_recycle`,
ADD COLUMN `store_recycle` tinyint(4) NULL DEFAULT 1 COMMENT '商城删除 1.显示 2.删除' AFTER `mch_recycle`,
ADD COLUMN `deliver_num` int(11) NULL DEFAULT 0 COMMENT '发货数量' AFTER `store_recycle`;

ALTER TABLE `lkt_order_details` 
MODIFY COLUMN `express_id` text NULL COMMENT '快递公司id' AFTER `r_type`,
MODIFY COLUMN `courier_num` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '快递单号' AFTER `express_id`;

CREATE TABLE `lkt_express_delivery` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `store_id` int(11) DEFAULT '0' COMMENT '商城ID',
  `sNo` varchar(255) NULL COMMENT '订单号',
  `order_details_id` int(11) DEFAULT '0' COMMENT '订单详情ID',
  `express_id` int(11) DEFAULT '0' COMMENT '快递公司ID',
  `courier_num` varchar(255) NOT NULL COMMENT '快递单号',
  `num` int(11) DEFAULT '0' COMMENT '商品数量',
  `deliver_time` datetime DEFAULT NULL COMMENT '发货时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB CHARSET=utf8 COMMENT='快递记录表';