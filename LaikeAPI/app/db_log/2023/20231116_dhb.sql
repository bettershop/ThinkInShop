
ALTER TABLE `lkt_group_activity`
ADD COLUMN `is_custom`  tinyint(4) NULL DEFAULT 0 COMMENT '时间是否自定义 0不是 1是' AFTER `end_date`;

ALTER TABLE `lkt_ui_navigation_bar` 
MODIFY COLUMN `add_date` datetime(0) NULL COMMENT '添加时间' AFTER `sort`,
ADD COLUMN `is_login` tinyint(1) NULL DEFAULT 1 COMMENT '是否需要登录 0 不需要登录 1 需要登录' AFTER `isshow`;

ALTER TABLE `lkt_coupon_presentation_record` 
MODIFY COLUMN `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '手机号' AFTER `user_id`,
MODIFY COLUMN `add_date` datetime(0) NULL DEFAULT NULL COMMENT '添加时间' AFTER `activity_type`;

ALTER TABLE `lkt_product_config` 
ADD COLUMN `is_display_sell_put` tinyint(4) NULL DEFAULT 0 COMMENT '是否显示已售罄商品 0.不显示  1.显示' AFTER `is_open`;
