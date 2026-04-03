ALTER TABLE `lkt_configure` CHANGE `live_price` `live_price` DECIMAL(12,2) NOT NULL DEFAULT '0.00' COMMENT '直播价格';

ALTER TABLE `lkt_configure` CHANGE `bargain_price` `bargain_price` DECIMAL(12,2) NOT NULL DEFAULT '0.00' COMMENT '砍价开始价格';

ALTER TABLE `lkt_user` CHANGE `money` `money` DECIMAL(12,2) NOT NULL DEFAULT '0.00' COMMENT '金额';

ALTER TABLE `lkt_order` CHANGE `zhekou` `zhekou` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '分销折扣';

ALTER TABLE `lkt_order` 
ADD COLUMN `paypal_id` varchar(255) NULL COMMENT '贝宝支付订单id' AFTER `cancel_method`;
ALTER TABLE `lkt_order` 
ADD COLUMN `capture_id` varchar(255) NULL COMMENT '贝宝付款生成的ID' AFTER `paypal_id`;

ALTER TABLE `lkt_record` 
MODIFY COLUMN `type` tinyint(4) NOT NULL DEFAULT 0 COMMENT '类型 0:登录/退出 1:充值 2:申请提现 3:分享 4:余额消费 5:退款 6:红包提现 7:佣金 8:管理佣金 9:待定 10:消费金 11:系统扣款   12:给好友转余额 13:转入余额 14:系统充值 15:系统充积分 16:系统充消费金 17:系统扣积分 18:系统扣消费金 19:消费金解封 20:抽奖中奖 21:  提现成功 22:提现失败 23.取消订单  24分享获取红包 26 交竞拍押金 27 退竞拍押金 28 售后（仅退款） 29 售后（退货退款）30 会员返现  42.主播佣金' AFTER `event`;

ALTER TABLE `lkt_record_details` 
MODIFY COLUMN `record_notes` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注' AFTER `record_time`,
MODIFY COLUMN `type_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '充值方式/退款类型/活动类型' AFTER `record_notes`,
MODIFY COLUMN `s_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '订单号' AFTER `type_name`,
MODIFY COLUMN `title_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商品名字/活动名称' AFTER `s_no`,
MODIFY COLUMN `activity_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '活动编号' AFTER `title_name`,
MODIFY COLUMN `mch_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '店铺名称' AFTER `activity_code`,
MODIFY COLUMN `withdrawal_fees` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '提现手续费' AFTER `mch_name`,
MODIFY COLUMN `withdrawal_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '提现方式' AFTER `withdrawal_fees`;

ALTER TABLE `lkt_withdraw` 
MODIFY COLUMN `user_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户id' AFTER `store_id`,
MODIFY COLUMN `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '名称' AFTER `user_id`,
MODIFY COLUMN `wx_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '微信id' AFTER `name`,
MODIFY COLUMN `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机' AFTER `wx_id`,
MODIFY COLUMN `refuse` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '拒绝原因' AFTER `add_date`,
MODIFY COLUMN `txsno` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '提现订单号' AFTER `is_mch`,
MODIFY COLUMN `withdraw_status` int(11) NOT NULL DEFAULT 1 COMMENT '提现类型 1银行卡  2微信余额  3贝宝余额' AFTER `examine_date`;
MODIFY COLUMN `wx_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '提现到微信余额提现状态：1进行中 2已完成 3提现失败' AFTER `withdraw_status`,
MODIFY COLUMN `wx_open_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户openid' AFTER `recovery`,
MODIFY COLUMN `wx_son` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '微信提现到零钱订单号' AFTER `wx_open_id`,
MODIFY COLUMN `wx_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户微信名称' AFTER `wx_son`,
MODIFY COLUMN `realname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户微信余额提现真实姓名' AFTER `wx_name`,
ADD COLUMN `email` varchar(255) NULL COMMENT '贝宝提现邮箱' AFTER `realname`;


ALTER TABLE `lkt_user_distribution` CHANGE `commission` `commission` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '个人佣金';
ALTER TABLE `lkt_user_distribution` CHANGE `onlyamount` `onlyamount` DECIMAL(15,2) NOT NULL DEFAULT '0.00' COMMENT '累计消费';
ALTER TABLE `lkt_user_distribution` CHANGE `allamount` `allamount` DECIMAL(15,2) NOT NULL DEFAULT '0.00' COMMENT '销售业绩';
ALTER TABLE `lkt_user_distribution` CHANGE `one_put` `one_put` DECIMAL(11,2) NOT NULL DEFAULT '0.00' COMMENT '个人进货奖励已经发放最大条件';
ALTER TABLE `lkt_user_distribution` CHANGE `team_put` `team_put` DECIMAL(11,2) NOT NULL DEFAULT '0.00' COMMENT '团队业绩奖励已经发放最大';
ALTER TABLE `lkt_user_distribution` CHANGE `accumulative` `accumulative` DECIMAL(12,2) NOT NULL DEFAULT '0.00' COMMENT '累计佣金';
ALTER TABLE `lkt_user_distribution` CHANGE `tx_commission` `tx_commission` DECIMAL(12,2) NOT NULL DEFAULT '0.00' COMMENT '可提现佣金';=======
ALTER TABLE `lkt_user` CHANGE `money` `money` DECIMAL(12,2) NOT NULL DEFAULT '0.00' COMMENT '金额';

ALTER TABLE `lkt_order` CHANGE `zhekou` `zhekou` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '分销折扣';

ALTER TABLE `lkt_order` 
ADD COLUMN `paypal_id` varchar(255) NULL COMMENT '贝宝支付订单id' AFTER `cancel_method`;
ALTER TABLE `lkt_order` 
ADD COLUMN `capture_id` varchar(255) NULL COMMENT '贝宝付款生成的ID' AFTER `paypal_id`;

ALTER TABLE `lkt_record` 
MODIFY COLUMN `type` tinyint(4) NOT NULL DEFAULT 0 COMMENT '类型 0:登录/退出 1:充值 2:申请提现 3:分享 4:余额消费 5:退款 6:红包提现 7:佣金 8:管理佣金 9:待定 10:消费金 11:系统扣款   12:给好友转余额 13:转入余额 14:系统充值 15:系统充积分 16:系统充消费金 17:系统扣积分 18:系统扣消费金 19:消费金解封 20:抽奖中奖 21:  提现成功 22:提现失败 23.取消订单  24分享获取红包 26 交竞拍押金 27 退竞拍押金 28 售后（仅退款） 29 售后（退货退款）30 会员返现  42.主播佣金' AFTER `event`;

ALTER TABLE `lkt_record_details` 
MODIFY COLUMN `record_notes` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注' AFTER `record_time`,
MODIFY COLUMN `type_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '充值方式/退款类型/活动类型' AFTER `record_notes`,
MODIFY COLUMN `s_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '订单号' AFTER `type_name`,
MODIFY COLUMN `title_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商品名字/活动名称' AFTER `s_no`,
MODIFY COLUMN `activity_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '活动编号' AFTER `title_name`,
MODIFY COLUMN `mch_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '店铺名称' AFTER `activity_code`,
MODIFY COLUMN `withdrawal_fees` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '提现手续费' AFTER `mch_name`,
MODIFY COLUMN `withdrawal_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '提现方式' AFTER `withdrawal_fees`;

ALTER TABLE `lkt_withdraw` 
MODIFY COLUMN `user_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户id' AFTER `store_id`,
MODIFY COLUMN `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '名称' AFTER `user_id`,
MODIFY COLUMN `wx_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '微信id' AFTER `name`,
MODIFY COLUMN `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机' AFTER `wx_id`,
MODIFY COLUMN `refuse` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '拒绝原因' AFTER `add_date`,
MODIFY COLUMN `txsno` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '提现订单号' AFTER `is_mch`,
MODIFY COLUMN `withdraw_status` int(11) NOT NULL DEFAULT 1 COMMENT '提现类型 1银行卡  2微信余额  3贝宝余额' AFTER `examine_date`;
MODIFY COLUMN `wx_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '提现到微信余额提现状态：1进行中 2已完成 3提现失败' AFTER `withdraw_status`,
MODIFY COLUMN `wx_open_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户openid' AFTER `recovery`,
MODIFY COLUMN `wx_son` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '微信提现到零钱订单号' AFTER `wx_open_id`,
MODIFY COLUMN `wx_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户微信名称' AFTER `wx_son`,
MODIFY COLUMN `realname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户微信余额提现真实姓名' AFTER `wx_name`,
ADD COLUMN `email` varchar(255) NULL COMMENT '贝宝提现邮箱' AFTER `realname`;


ALTER TABLE `lkt_user_distribution` CHANGE `commission` `commission` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '个人佣金';
ALTER TABLE `lkt_user_distribution` CHANGE `onlyamount` `onlyamount` DECIMAL(15,2) NOT NULL DEFAULT '0.00' COMMENT '累计消费';
ALTER TABLE `lkt_user_distribution` CHANGE `allamount` `allamount` DECIMAL(15,2) NOT NULL DEFAULT '0.00' COMMENT '销售业绩';
ALTER TABLE `lkt_user_distribution` CHANGE `one_put` `one_put` DECIMAL(11,2) NOT NULL DEFAULT '0.00' COMMENT '个人进货奖励已经发放最大条件';
ALTER TABLE `lkt_user_distribution` CHANGE `team_put` `team_put` DECIMAL(11,2) NOT NULL DEFAULT '0.00' COMMENT '团队业绩奖励已经发放最大';
ALTER TABLE `lkt_user_distribution` CHANGE `accumulative` `accumulative` DECIMAL(12,2) NOT NULL DEFAULT '0.00' COMMENT '累计佣金';
ALTER TABLE `lkt_user_distribution` CHANGE `tx_commission` `tx_commission` DECIMAL(12,2) NOT NULL DEFAULT '0.00' COMMENT '可提现佣金';

ALTER TABLE `lkt_return_order` 
ADD COLUMN `audit_status` tinyint(4) NULL DEFAULT 0 COMMENT '审核状态 0.正常 1.极速退款等待回调 2.不是极速退款等待回调' AFTER `r_write_off_num`;

ALTER TABLE `lkt_return_order` 
ADD COLUMN `refund_Id` varchar(255) NULL COMMENT 'paypal生成的退款ID' AFTER `audit_status`;
