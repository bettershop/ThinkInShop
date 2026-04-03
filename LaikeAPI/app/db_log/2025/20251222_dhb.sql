
ALTER TABLE `lkt_order` 
ADD COLUMN `voucher` text NULL COMMENT '凭证' AFTER `stripe_payment_intent`,
ADD COLUMN `review_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '凭证审核状态 0.未上传凭证 1.待审核 2.通过 3.拒绝' AFTER `voucher`,
ADD COLUMN `review_time` datetime NULL COMMENT '审核时间' AFTER `review_status`,
ADD COLUMN `reason_for_rejection` varchar(255) NULL COMMENT '拒绝原因' AFTER `review_time`;

ALTER TABLE `lkt_menu` 
MODIFY COLUMN `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
MODIFY COLUMN `sid` int(11) NOT NULL DEFAULT 0 AFTER `type`;

ALTER TABLE `lkt_message` 
ADD COLUMN `international` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否是国际化 0.国内 1.国际' AFTER `add_time`;

ALTER TABLE `lkt_supplier` 
ADD COLUMN `cpc` varchar(255) NULL DEFAULT 86 COMMENT '区号' AFTER `contacts`,
MODIFY COLUMN `contact_phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '电话号码' AFTER `contacts`;

ALTER TABLE `lkt_service_address` 
ADD COLUMN `cpc` varchar(255) NULL DEFAULT 86 COMMENT '区号' AFTER `is_default`;

ALTER TABLE `lkt_mch_store` 
ADD COLUMN `cpc` varchar(255) NULL DEFAULT 86 COMMENT '区号' AFTER `is_default`;

ALTER TABLE `lkt_user_address` 
ADD COLUMN `cpc` varchar(255) NULL DEFAULT 86 COMMENT '区号' AFTER `is_default`;

ALTER TABLE `lkt_mch_store` 
ADD COLUMN `code` int(11) NOT NULL DEFAULT 0 COMMENT '邮政编号' AFTER `cpc`;

ALTER TABLE `lkt_order` 
ADD COLUMN `cpc` varchar(50) NULL DEFAULT 86 COMMENT '区号' AFTER `name`;

ALTER TABLE `lkt_order` 
ADD COLUMN `shop_cpc` varchar(50) NULL COMMENT '自提点区号，如果不是自提订单就是用户区号' AFTER `sNo`;

ALTER TABLE `lkt_order` 
ADD COLUMN `code` int(11) NOT NULL DEFAULT 0 COMMENT '邮政编号' AFTER `address`;

ALTER TABLE `lkt_ds_country` 
ADD COLUMN `national_flag` varchar(255) NULL COMMENT '国旗' AFTER `is_show`;

ALTER TABLE `lkt_service_address` 
ADD COLUMN `country_num` int(11) NOT NULL DEFAULT 156 COMMENT '国家代码 默认中国编码：156' AFTER `cpc`;

ALTER TABLE `tp_db`.`lkt_config` 
MODIFY COLUMN `express_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '查询接口地址' AFTER `is_express`,
MODIFY COLUMN `express_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户编号' AFTER `express_address`,
MODIFY COLUMN `express_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '接口调用key' AFTER `express_number`,
MODIFY COLUMN `express_secret` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'secret在企业管理后台获取' AFTER `express_tel`,
ADD COLUMN `is_open_cloud` tinyint(1) NULL DEFAULT 0 COMMENT '是否开启快递100云打印 0：关闭 1：开启' AFTER `express_tempId`,
ADD COLUMN `siid` varchar(64) NULL COMMENT '打印机设备码' AFTER `is_open_cloud`,
ADD COLUMN `cloud_notify` varchar(64) NULL COMMENT '云打印回调地址' AFTER `siid`,
ADD COLUMN `express_temp_id` text NULL COMMENT '主单模板' AFTER `cloud_notify`,
MODIFY COLUMN `watermark_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '水印名称' AFTER `Hide_your_wallet`,
MODIFY COLUMN `watermark_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '水印网址' AFTER `watermark_name`,
MODIFY COLUMN `app_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '小程序首页标题' AFTER `pc_mch_path`,
MODIFY COLUMN `app_logo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '小程序授权登录logo' AFTER `app_title`,
ADD COLUMN `store_name` varchar(255) NULL COMMENT '商城名称' AFTER `app_logo`,
ADD COLUMN `html_icon` varchar(255) NULL COMMENT '网页标题icon' AFTER `store_name`;