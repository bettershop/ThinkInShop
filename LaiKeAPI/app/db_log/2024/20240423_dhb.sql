
ALTER TABLE `lkt_product_list` 
MODIFY COLUMN `show_adr` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '1' COMMENT '展示位置:1.首页 2.购物车 3.分类 4.我的-推荐' AFTER `search_num`;

ALTER TABLE `lkt_withdraw` 
ADD COLUMN `withdraw_status` int(11) NOT NULL DEFAULT 1 COMMENT '提现类型 1银行卡  2微信余额' AFTER `examine_date`,
ADD COLUMN `wx_status` varchar(255) NULL COMMENT '提现到微信余额提现状态：1进行中 2已完成 3提现失败' AFTER `withdraw_status`,
ADD COLUMN `wx_open_id` varchar(255) NULL COMMENT '用户openid' AFTER `recovery`,
ADD COLUMN `wx_son` varchar(255) NULL COMMENT '微信提现到零钱订单号' AFTER `wx_open_id`,
ADD COLUMN `wx_name` varchar(255) NULL COMMENT '用户微信名称' AFTER `wx_son`,
ADD COLUMN `realname` varchar(255) NULL COMMENT '用户微信余额提现真实姓名' AFTER `wx_name`;

ALTER TABLE `lkt_withdraw` 
MODIFY COLUMN `examine_date` timestamp(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '提现审核时间' AFTER `txsno`;


ALTER TABLE lkt_config
    ADD COLUMN `is_accounts` int(11) DEFAULT NULL COMMENT '是否分账';
# 商城分账账户
ALTER TABLE lkt_config
    ADD COLUMN `accounts_set` varchar(255) DEFAULT NULL COMMENT '分账账号';

    
ALTER TABLE lkt_mch
    ADD COLUMN `sub_mch_id` varchar(50) DEFAULT NULL COMMENT '子商户号';

ALTER TABLE lkt_mch
    ADD COLUMN `sub_app_id` varchar(100) DEFAULT NULL COMMENT '子商户应用id';


CREATE TABLE `lkt_mch_distribution` (
    `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
    `mch_id` int(11) NOT NULL DEFAULT 0 COMMENT '店铺id',
    `d_type` varchar(50) NOT NULL COMMENT '分账接收方类型 MERCHANT_ID：商户号  PERSONAL_OPENID：个人openid（由父商户APPID转换得到） PERSONAL_SUB_OPENID: 个人sub_openid（由子商户APPID转换得到）',
    `account` varchar(100) NOT NULL COMMENT '分账接收方账号',
    `relationship` varchar(100) NOT NULL COMMENT '分账接收方关系 SERVICE_PROVIDER：服务商 STORE：门店 STAFF：员工 STORE_OWNER：店主 PARTNER：合作伙伴 HEADQUARTER：总部 BRAND：品牌方 DISTRIBUTOR：分销商 USER：用户 SUPPLIER： 供应商 CUSTOM：自定义',
    `proportion` decimal(12, 2) NOT NULL COMMENT '分账比例',
    `add_date` datetime DEFAULT NULL COMMENT '添加时间',
    `isAccounts` int(11) NOT NULL DEFAULT 0 COMMENT '是否分账',
    `accountsSet` int(11) NOT NULL DEFAULT 0 COMMENT '分账账号',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='店铺分账账户信息';

CREATE TABLE `lkt_mch_distribution_record` (
    `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
    `mch_id` int(11) NOT NULL DEFAULT 0 COMMENT '店铺id',
    `sub_mch_id` varchar(50) NOT NULL COMMENT '子商户id',
    `account` varchar(100) NOT NULL COMMENT '分账接收方账号',
    `order_no` varchar(100) NOT NULL COMMENT '订单号(自己)',
    `wx_order_no` varchar(100) NOT NULL COMMENT '订单号(微信)',
    `out_order_no` varchar(100) NOT NULL COMMENT '分账单号',
    `total_amount` decimal(12, 2) NOT NULL COMMENT '分账总金额',
    `amount` decimal(12, 2) NOT NULL COMMENT '分账金额',
    `proportion` decimal(12, 2) DEFAULT NULL COMMENT '分账比例',
    `r_type` int(10) NOT NULL DEFAULT 1 COMMENT '记录类型 1.分账 2.回退',
    `add_date` datetime DEFAULT NULL COMMENT '添加时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='店铺分账记录信息';

ALTER TABLE `lkt_order` 
ADD COLUMN `Dividend_status` tinyint(4) NULL DEFAULT 0 COMMENT '分账状态 0.不分账 1.分账' AFTER `transaction_id`;

CREATE TABLE `lkt_promise_record` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `store_id` int(11) NOT NULL COMMENT '商城ID',
    `mch_id` int(11) NOT NULL COMMENT '店铺ID',
    `money` decimal(10,2) NOT NULL COMMENT '保证金金额',
    `type` int(11) NOT NULL COMMENT '记录类型 1:缴纳，2:退还',
    `status` varchar(255) NOT NULL COMMENT '审核状态 0=审核中 1=通过 2=拒绝',
    `add_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '添加时间',
    `remarks` varchar(255) COMMENT '备注',
    `promise_sh_id` VARBINARY(255) COMMENT '保证金审核id',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='保证金审核记录表';
