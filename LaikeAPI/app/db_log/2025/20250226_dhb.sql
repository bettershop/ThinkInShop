
ALTER TABLE `lkt_user` 
MODIFY COLUMN `user_name` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户昵称' AFTER `user_id`,
MODIFY COLUMN `access_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '授权id' AFTER `user_name`,
MODIFY COLUMN `access_key` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '授权密钥' AFTER `access_id`,
MODIFY COLUMN `wx_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '微信id' AFTER `access_key`,
MODIFY COLUMN `gzh_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '公众号id' AFTER `wx_name`,
MODIFY COLUMN `zfb_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付宝id' AFTER `gzh_id`,
MODIFY COLUMN `bd_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '百度id' AFTER `zfb_id`,
MODIFY COLUMN `tt_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头条id' AFTER `bd_id`,
MODIFY COLUMN `clientid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '推送客户端ID' AFTER `tt_id`,
MODIFY COLUMN `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '省' AFTER `headimgurl`,
MODIFY COLUMN `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '市' AFTER `province`,
MODIFY COLUMN `county` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '县' AFTER `city`,
MODIFY COLUMN `detailed_address` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '详细地址' AFTER `county`,
MODIFY COLUMN `password` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付密码' AFTER `lock_score`,
MODIFY COLUMN `e_mail` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱' AFTER `Register_data`,
MODIFY COLUMN `real_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '真实姓名' AFTER `e_mail`,
MODIFY COLUMN `mobile` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号' AFTER `real_name`,
MODIFY COLUMN `wechat_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '微信号' AFTER `birthday`,
MODIFY COLUMN `address` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '地址' AFTER `wechat_id`,
MODIFY COLUMN `Bank_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '银行名称' AFTER `address`,
MODIFY COLUMN `Cardholder` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '持卡人' AFTER `Bank_name`,
MODIFY COLUMN `Bank_card_number` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '银行卡号' AFTER `Cardholder`,
MODIFY COLUMN `Referee` char(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '推荐人' AFTER `share_num`,
MODIFY COLUMN `access_token` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '访问令牌' AFTER `Referee`,
MODIFY COLUMN `img_token` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分享图片id' AFTER `consumer_money`,
MODIFY COLUMN `mima` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '密码' AFTER `zhanghao`,
MODIFY COLUMN `parameter` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '参数' AFTER `verification_time`,
MODIFY COLUMN `tui_id` char(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '会员推荐人id' AFTER `grade`,
MODIFY COLUMN `mch_token` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '店铺token' AFTER `is_box`,
MODIFY COLUMN `lang` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '语言' AFTER `mch_token`,
MODIFY COLUMN `user_from_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '兵器设备用户ID' AFTER `lang`,
MODIFY COLUMN `token_living_pc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '主播pc的token' AFTER `is_default_birthday`;

ALTER TABLE `lkt_user` 
ADD COLUMN `cpc` VARCHAR(16) COMMENT 'ITU电话代码' AFTER user_name,
ADD COLUMN `country_num` INT DEFAULT 156 COMMENT '国家代码' AFTER cpc

ALTER TABLE `lkt_session_id` 
MODIFY COLUMN `type` tinyint(4) NULL DEFAULT 0 COMMENT '类型 0.发送短信 1.商\n\n\r\n品 2.退货申请 3.评论 4.邮箱验证码' AFTER `add_date`;