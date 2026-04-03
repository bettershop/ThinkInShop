
ALTER TABLE `lkt_system_configuration` 
MODIFY COLUMN `logo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '登录页logo' AFTER `store_id`,
MODIFY COLUMN `copyright_information` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '版权信息' AFTER `logo`,
MODIFY COLUMN `record_information` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备案信息' AFTER `copyright_information`,
MODIFY COLUMN `link_to_landing_page` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '登录页友情链接' AFTER `record_information`,
MODIFY COLUMN `store_id_prefix` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商城id前缀' AFTER `add_time`,
MODIFY COLUMN `admin_default_portrait` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '默认头像设置' AFTER `store_id_prefix`;

ALTER TABLE `lkt_customer` 
MODIFY COLUMN `customer_number` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '客户编号' AFTER `admin_id`,
MODIFY COLUMN `name` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '姓名' AFTER `customer_number`,
MODIFY COLUMN `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机' AFTER `name`,
MODIFY COLUMN `company` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '公司名称' AFTER `price`,
MODIFY COLUMN `contact_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系地址' AFTER `is_default`,
MODIFY COLUMN `contact_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系电话' AFTER `contact_address`,
MODIFY COLUMN `copyright_information` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '版权信息' AFTER `contact_number`,
MODIFY COLUMN `record_information` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备案信息' AFTER `copyright_information`,
MODIFY COLUMN `official_website` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '官方网址' AFTER `record_information`,
MODIFY COLUMN `merchant_logo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商户logo' AFTER `official_website`;

ALTER TABLE `lkt_config` 
MODIFY COLUMN `company` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '公司名称' AFTER `logo1`,
MODIFY COLUMN `domain` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商城根目录域名' AFTER `appsecret`,
MODIFY COLUMN `H5_domain` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'H5地址' AFTER `app_domain_name`,
MODIFY COLUMN `wx_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '默认微信名称' AFTER `user_id`,
MODIFY COLUMN `wx_headimgurl` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '默认微信头像' AFTER `wx_name`,
MODIFY COLUMN `customer_service` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '客服' AFTER `wx_headimgurl`,
MODIFY COLUMN `pc_mch_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'PC店铺地址' AFTER `watermark_url`,
MODIFY COLUMN `accounts_set` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分账账号' AFTER `is_accounts`,
ADD COLUMN `logon_logo` varchar(255) NULL COMMENT '登录logo' AFTER `accounts_set`,
ADD COLUMN `copyright_information` varchar(255) NULL COMMENT '版权信息' AFTER `logon_logo`,
ADD COLUMN `record_information` varchar(255) NULL COMMENT '备案信息' AFTER `copyright_information`,
ADD COLUMN `link_to_landing_page` text NULL COMMENT '登录页友情链接' AFTER `record_information`,
ADD COLUMN `admin_default_portrait` varchar(255) NULL COMMENT '默认头像设置' AFTER `link_to_landing_page`;

ALTER TABLE `lkt_config` 
ADD COLUMN `android_download_link` varchar(255) NULL COMMENT '安卓下载地址' AFTER `admin_default_portrait`,
ADD COLUMN `ios_download_link` varchar(255) NULL COMMENT 'IOS下载地址' AFTER `android_download_link`;

CREATE TABLE `lkt_sensitive_words`  (
    `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `store_id` int(11) NULL DEFAULT NULL COMMENT '商城id',
    `word` text COMMENT '敏感词',
    `add_time` timestamp NULL DEFAULT NULL COMMENT '添加时间',
    `recycle` int(11) NULL DEFAULT 0 COMMENT '逻辑删除标志',
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='敏感词表';