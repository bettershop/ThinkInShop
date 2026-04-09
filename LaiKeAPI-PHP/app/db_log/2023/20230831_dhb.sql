ALTER TABLE `lkt_mch`
ADD COLUMN `pc_mch_path`  varchar(255) NULL COMMENT 'pc店铺地址' AFTER `collection_code`,
ADD COLUMN `is_open_coupon`  int(11) NULL DEFAULT 1 COMMENT '是否开启店铺主页领卷人口' AFTER `pc_mch_path`,
ADD COLUMN `last_login_time`  datetime NULL DEFAULT NULL COMMENT '最后登录时间' AFTER `is_open_coupon`;
ALTER TABLE `lkt_mch_config`
ADD COLUMN `poster_img`  varchar(255) NULL COMMENT '店铺新增宣传图' AFTER `same_order`,
ADD COLUMN `head_img`  varchar(255) NULL COMMENT '店铺头像' AFTER `poster_img`,
ADD COLUMN `auto_examine`  int(11) NULL DEFAULT 1 COMMENT '店铺自动审核天数' AFTER `head_img`,
ADD COLUMN `auto_log_off`  int(11) NULL DEFAULT 0 COMMENT '自动注销时间(月)' AFTER `auto_examine`,
ADD COLUMN `withdrawal_time_open`  int(5) NULL DEFAULT 0 COMMENT '提现时间开关 0.不限制 1.指定日期 2.指定时间段' AFTER `auto_log_off`,
ADD COLUMN `withdrawal_time`  varchar(50) NULL COMMENT '指定时间(时间段:15-20)' AFTER `withdrawal_time_open`;
