
CREATE TABLE `lkt_express_subtable` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `store_id` int(11) DEFAULT '0' COMMENT '商城ID',
  `mch_id` int(11) DEFAULT '0' COMMENT '店铺ID',
  `express_id` int(11) DEFAULT '0' COMMENT '主表ID',
  `partnerId` text NULL COMMENT '电子面单客户账户或月结账号',
  `partnerKey` text NULL COMMENT '电子面单密码',
  `partnerSecret` text NULL COMMENT '电子面单密钥',
  `partnerName` text NULL COMMENT '电子面单客户账户名称',
  `net` text NULL COMMENT '收件网点名称',
  `code` text NULL COMMENT '电子面单承载编号',
  `checkMan` text NULL COMMENT '电子面单承载快递员名',
  `add_time` datetime DEFAULT NULL COMMENT '添加时间',
  `recovery` tinyint(4) NULL DEFAULT 0 COMMENT '是否删除 0.未删除 1.已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB CHARSET=utf8 COMMENT='快递公司子表';


ALTER TABLE `lkt_express_delivery` 
MODIFY COLUMN `store_id` int(11) NOT NULL DEFAULT 0 COMMENT '商城ID' AFTER `id`,
ADD COLUMN `childNum` text NULL COMMENT '子单号' AFTER `deliver_time`,
ADD COLUMN `returnNum` text NULL COMMENT '回单号' AFTER `childNum`,
ADD COLUMN `label` text NULL COMMENT '面单短链' AFTER `returnNum`,
ADD COLUMN `kdComOrderNum` text NULL COMMENT '快递公司订单号' AFTER `label`,
ADD COLUMN `is_status` tinyint(4) NULL DEFAULT 0 COMMENT '是否打印 0.未打印 1.已打印' AFTER `kdComOrderNum`,
ADD COLUMN `subtable_id` int(11) NOT NULL COMMENT '快递公司子表ID' AFTER `is_status`;

ALTER TABLE `lkt_config` 
ADD COLUMN `express_secret` text NULL COMMENT 'secret在企业管理后台获取' AFTER `express_tel`;

ALTER TABLE `lkt_config` 
ADD COLUMN `express_tempId` text NULL COMMENT '主单模板' AFTER `express_secret`;