ALTER TABLE `lkt_mch_distribution_record`
    ADD COLUMN `is_platform_account` tinyint(4) NULL DEFAULT 0 COMMENT '是否是平台 0.不是 1.是' AFTER `add_date`;
ALTER TABLE `lkt_mch_distribution`
    ADD COLUMN `name` varchar(255) NULL COMMENT '分账接收方全称' AFTER `account`;
alter table lkt_order
    add dividend_status int default 0 null comment '分账状态 0.不分账 1.分账' after wx_order_status;
alter table lkt_mch_distribution_record
    modify sub_mch_id varchar(50)  null comment '子商户id';
alter table lkt_mch_distribution
    modify add_date timestamp null comment '添加时间';