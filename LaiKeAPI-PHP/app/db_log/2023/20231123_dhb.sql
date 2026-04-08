
CREATE TABLE `lkt_mch_admin` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `store_id` int(11) DEFAULT '0' COMMENT '商城ID',
  `mch_id` int(11) DEFAULT '0' COMMENT '店铺ID',
  `mch_store_id` int(11) DEFAULT '0' COMMENT '店铺门店ID',
  `ip` varchar(255) NULL COMMENT 'IP'
  `access_id` varchar(255) NULL COMMENT '授权id'
  `account_number` varchar(255) NOT NULL COMMENT '账号',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `add_date` datetime DEFAULT NULL COMMENT '添加时间',
  `recycle` tinyint(1) NULL DEFAULT 0 COMMENT '回收站 0:不回收 1:回收 ',
  `last_time` datetime DEFAULT NULL COMMENT '最后一次登录时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB CHARSET=utf8 COMMENT='店铺管理员';


CREATE TABLE `lkt_mch_admin_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `store_id` int(11) DEFAULT '0' COMMENT '商城ID',
  `administrators_id` int(11) DEFAULT '0' COMMENT '管理员ID',
  `sNo` varchar(255) NOT NULL COMMENT '订单号',
  `add_date` datetime DEFAULT NULL COMMENT '添加时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB CHARSET=utf8 COMMENT='店铺管理员核销记录表';

ALTER TABLE `lkt_order` 
ADD COLUMN `VerifiedBy_type` tinyint(1) NULL DEFAULT 1 COMMENT '核销人类型 1.店主核销 2.门店核销' AFTER `supplier_id`,
ADD COLUMN `VerifiedBy` int(11) NULL DEFAULT 0 COMMENT '核销人ID' AFTER `VerifiedBy_type`;

ALTER TABLE `lkt_block_home` 
ADD COLUMN `enable_or_not` int(11) NULL DEFAULT 1 COMMENT '楼层是否开启显示 1显示  2不显示' AFTER `store_id`;

ALTER TABLE `lkt_configure` 
ADD COLUMN `msrp` decimal(12, 2) NOT NULL COMMENT '建议零售价' AFTER `yprice`;

ALTER TABLE `lkt_sku` 
ADD COLUMN `is_examine` int(11) NOT NULL DEFAULT 0 COMMENT '审核状态 0.未审核  1.已审核' AFTER `add_date`;

ALTER TABLE `lkt_file_delivery` 
MODIFY COLUMN `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id' FIRST;
ALTER TABLE `lkt_file_delivery` 
ADD COLUMN `store_id` int(11) NULL DEFAULT 0 COMMENT '商城ID' AFTER `id`;

ALTER TABLE `lkt_file_delivery` 
ADD COLUMN `order_num` int(11) NULL DEFAULT 0 COMMENT '发货订单数量/导入商品数' AFTER `update_date`,
MODIFY COLUMN `type` int(11) NULL DEFAULT 0 COMMENT '0导入上传商品  1批量发货' AFTER `order_num`;

ALTER TABLE `lkt_reply_comments` 
MODIFY COLUMN `sid` int(11) NULL DEFAULT 0 COMMENT '回复的上级id' AFTER `to_uid`;

ALTER TABLE `lkt_message_logging` 
MODIFY COLUMN `supplier_id` int(11) NOT NULL DEFAULT 0 COMMENT '供应商id' AFTER `to_url`;