CREATE TABLE `lkt_mch_distribution` (
`id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
`mch_id` int(11) NOT NULL COMMENT '店铺id',
`d_type` varchar(50) NOT NULL COMMENT '分账接收方类型 MERCHANT_ID：商户号  PERSONAL_OPENID：个人openid（由父商户APPID转换得到） PERSONAL_SUB_OPENID: 个人sub_openid（由子商户APPID转换得到）',
`account` varchar(100) NOT NULL COMMENT '分账接收方账号',
`relationship` varchar(100) NOT NULL COMMENT '分账接收方关系 SERVICE_PROVIDER：服务商 STORE：门店 STAFF：员工 STORE_OWNER：店主 PARTNER：合作伙伴 HEADQUARTER：总部 BRAND：品牌方 DISTRIBUTOR：分销商 USER：用户 SUPPLIER： 供应商 CUSTOM：自定义',
`proportion` decimal(12, 2) NOT NULL COMMENT '分账比例',
`add_date` datetime DEFAULT NULL COMMENT '添加时间',
PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='店铺分账账户信息';

CREATE TABLE `lkt_mch_distribution_record` (
       `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
       `mch_id` int(11) NOT NULL COMMENT '店铺id',
       `sub_mch_id` varchar(50) NOT NULL COMMENT '子商户id',
       `account` varchar(100) NOT NULL COMMENT '分账接收方账号',
       `order_no` varchar(100) NOT NULL COMMENT '订单号(自己)',
       `wx_order_no` varchar(100) NOT NULL COMMENT '订单号(微信)',
       `out_order_no` varchar(100) NOT NULL COMMENT '分账单号',
       `total_amount` decimal(12, 2) NOT NULL COMMENT '分账总金额',
       `amount` decimal(12, 2) NOT NULL COMMENT '分账金额',
       `proportion` decimal(12, 2) DEFAULT NULL COMMENT '分账比例',
       `r_type` int(10) not null COMMENT '记录类型 1.分账 2.回退',
       `add_date` datetime DEFAULT NULL COMMENT '添加时间',
       PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='店铺分账记录信息';


ALTER TABLE lkt_mch
    ADD COLUMN `sub_mch_id` varchar(50) DEFAULT NULL COMMENT '子商户号';

ALTER TABLE lkt_mch
    ADD COLUMN `sub_app_id` varchar(100) DEFAULT NULL COMMENT '子商户应用id';

# 微信分账：1 分账 0 不分账
ALTER TABLE lkt_config
    ADD COLUMN `is_accounts` int(11) DEFAULT NULL COMMENT '是否分账';
# 商城分账账户
ALTER TABLE lkt_config
    ADD COLUMN `accounts_set` varchar(255) DEFAULT NULL COMMENT '分账账号';

ALTER TABLE lkt_order
    ADD COLUMN `transaction_id` VARCHAR(100) not NULL COMMENT '微信返回支付单号唯一标识';

ALTER TABLE `lkt_order`
    MODIFY COLUMN `transaction_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '微信返回支付单号唯一标识' AFTER `store_recycle`;



