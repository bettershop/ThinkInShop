
ALTER TABLE `lkt_auction_promise` 
MODIFY COLUMN `store_id` int(11) NOT NULL DEFAULT 0 COMMENT '商城id' AFTER `id`,
MODIFY COLUMN `is_back` int(2) NULL DEFAULT NULL COMMENT '是否退款成功  1-成功 2-失败' AFTER `is_pay`,
MODIFY COLUMN `address_id` int(11) NOT NULL DEFAULT 0 COMMENT '收货地址id' AFTER `type`,
MODIFY COLUMN `source` int(11) NOT NULL DEFAULT 0 COMMENT '来源 1.小程序 2.app 3.支付宝小程序 4.头条小程序 5.百度小程序 6.pc端 7.H5' AFTER `address_id`;

ALTER TABLE `lkt_cart` 
MODIFY COLUMN `user_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户id' AFTER `token`;

ALTER TABLE `lkt_comments` 
MODIFY COLUMN `type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '评价类型-KJ 砍价评论-PT 拼团评论' AFTER `review_time`;

ALTER TABLE `lkt_config` 
MODIFY COLUMN `company` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '公司名称' AFTER `logo1`;
MODIFY COLUMN `domain` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商城根目录域名' AFTER `appsecret`,
MODIFY COLUMN `app_domain_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'APP域名' AFTER `domain`;
MODIFY COLUMN `uploadImg_domain` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片上传域名' AFTER `ip`,
MODIFY COLUMN `uploadImg` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片上传位置' AFTER `upserver`,
MODIFY COLUMN `upload_file` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件上传位置' AFTER `uploadImg`,
MODIFY COLUMN `mch_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信支付key' AFTER `modify_date`,
MODIFY COLUMN `mch_cert` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付证书文件地址' AFTER `mch_key`;

ALTER TABLE `lkt_core_menu` 
MODIFY COLUMN `name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单标识' AFTER `title`,
MODIFY COLUMN `module` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单模块标识' AFTER `image1`,
MODIFY COLUMN `action` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单文件标识' AFTER `module`,
MODIFY COLUMN `url` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路径' AFTER `action`;

ALTER TABLE `lkt_customer` 
MODIFY COLUMN `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱' AFTER `status`;

ALTER TABLE  `lkt_data_dictionary_list` 
MODIFY COLUMN `value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'value' AFTER `s_name`;

ALTER TABLE  `lkt_data_dictionary_name` 
MODIFY COLUMN `dic_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典代码' AFTER `admin_name`;

ALTER TABLE  `lkt_ds_country` 
MODIFY COLUMN `code2` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL AFTER `code`;

ALTER TABLE  `lkt_hotkeywords` 
MODIFY COLUMN `keyword` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '关键词' AFTER `num`;

ALTER TABLE  `lkt_jump_path` 
MODIFY COLUMN `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标题' AFTER `type`,
MODIFY COLUMN `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路径' AFTER `name`;

ALTER TABLE  `lkt_mch_class` 
MODIFY COLUMN `img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片' AFTER `name`;

ALTER TABLE  `lkt_menu` 
MODIFY COLUMN `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '页面路径 [vue-动态路由里的 component]' AFTER `path`;

ALTER TABLE  `lkt_order` 
MODIFY COLUMN `p_sNo` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '父类订单号' AFTER `grade_fan`;

ALTER TABLE  `lkt_product_img` 
MODIFY COLUMN `seller_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户id' AFTER `product_id`;

ALTER TABLE  `lkt_product_list` 
MODIFY COLUMN `product_number` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品编号' AFTER `store_id`;
MODIFY COLUMN `scan` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '条形码' AFTER `label`;
MODIFY COLUMN `richList` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '前端店铺商品详情插件' AFTER `content`;

ALTER TABLE  `lkt_reply_comments` 
MODIFY COLUMN `content` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '评价内容' AFTER `uid`;

ALTER TABLE  `lkt_return_order` 
MODIFY COLUMN `content` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '售后原因' AFTER `re_type`;

ALTER TABLE  `lkt_return_record` 
MODIFY COLUMN `courier_num` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '快递单号' AFTER `express_id`;
MODIFY COLUMN `explain` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注' AFTER `re_time`;

ALTER TABLE  `lkt_seconds_activity` 
MODIFY COLUMN `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '活动名称' AFTER `max_num`;

ALTER TABLE  `lkt_seconds_config` 
MODIFY COLUMN `rule` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '规则' AFTER `pcAdImg`;

ALTER TABLE  `lkt_sku` 
MODIFY COLUMN `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据名称' AFTER `code`;

ALTER TABLE  `lkt_stock` 
MODIFY COLUMN `user_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '购买方' AFTER `type`;

ALTER TABLE  `lkt_supplier` 
MODIFY COLUMN `business_scope` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '经营范围' AFTER `dic_id`;

ALTER TABLE  `lkt_system_message` 
MODIFY COLUMN `senderid` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发送人ID' AFTER `store_id`,
MODIFY COLUMN `recipientid` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '接收人ID' AFTER `senderid`;

ALTER TABLE  `lkt_upload_set` 
MODIFY COLUMN `attrvalue` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '值' AFTER `attr`;