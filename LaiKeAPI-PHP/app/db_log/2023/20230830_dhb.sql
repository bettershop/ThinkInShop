ALTER TABLE `lkt_config`
ADD COLUMN `pc_store_address`  varchar(255) NULL COMMENT 'PC店铺地址' AFTER `Hide_your_wallet`;
ALTER TABLE `lkt_config`
CHANGE COLUMN `pc_store_address` `pc_mch_path`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'PC店铺地址' AFTER `Hide_your_wallet`,
ADD COLUMN `watermark_name`  varchar(255) NULL COMMENT '水印名称' AFTER `Hide_your_wallet`,
ADD COLUMN `watermark_url`  text NULL COMMENT '水印网址' AFTER `watermark_name`,
ADD COLUMN `app_title`  varchar(255) NULL COMMENT '小程序首页标题' AFTER `pc_mch_path`,
ADD COLUMN `app_logo`  varchar(255) NULL COMMENT '小程序授权登录logo' AFTER `app_title`;
