--新增电子面单修改

CREATE TABLE `lkt_express_subtable` (
                                        `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                        `store_id` int(11) DEFAULT '0' COMMENT '商城ID',
                                        `mch_id` int(11) DEFAULT '0' COMMENT '店铺ID',
                                        `express_id` int(11) DEFAULT '0' COMMENT '主表ID',
                                        `partner_id` text COMMENT '电子面单客户账户或月结账号',
                                        `partner_key` text COMMENT '电子面单密码',
                                        `partner_secret` text COMMENT '电子面单密钥',
                                        `partner_name` text COMMENT '电子面单客户账户名称',
                                        `express_net` text COMMENT '收件网点名称',
                                        `express_code` text COMMENT '电子面单承载编号',
                                        `check_man` text COMMENT '电子面单承载快递员名',
                                        `add_time` datetime DEFAULT NULL COMMENT '添加时间',
                                        `recovery` tinyint(4) DEFAULT '0' COMMENT '是否删除 0.未删除 1.已删除',
                                        PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='快递公司子表';


ALTER TABLE `lkt_express_delivery`
    MODIFY COLUMN `store_id` int(11) NOT NULL DEFAULT 0 COMMENT '商城ID' AFTER `id`,
    ADD COLUMN `childNum` text NULL COMMENT '子单号' AFTER `deliver_time`,
    ADD COLUMN `returnNum` text NULL COMMENT '回单号' AFTER `childNum`,
    ADD COLUMN `label` text NULL COMMENT '面单短链' AFTER `returnNum`,
    ADD COLUMN `kdComOrderNum` text NULL COMMENT '快递公司订单号' AFTER `label`,
    ADD COLUMN `is_status` tinyint(4) NULL DEFAULT 0 COMMENT '是否打印 0.未打印 1.已打印' AFTER `kdComOrderNum`,
    ADD COLUMN `subtable_id` int(11) NOT NULL COMMENT '快递公司子表ID' AFTER `is_status`;

ALTER TABLE `v3_db`.`lkt_express_delivery`
    MODIFY COLUMN `subtable_id` int(11) NOT NULL DEFAULT 0 COMMENT '快递公司子表ID' AFTER `is_status`;

ALTER TABLE `lkt_config`
    ADD COLUMN `express_secret` text NULL COMMENT 'secret在企业管理后台获取' AFTER `express_key`;

ALTER TABLE `lkt_config`
    ADD COLUMN `express_temp_id` text NULL COMMENT '主单模板' AFTER `express_secret`;