

ALTER TABLE `lkt_coupon_activity` 
ADD COLUMN `image` varchar(255) NULL COMMENT '优惠券图片' AFTER `is_auto_push`;

ALTER TABLE `lkt_coupon_activity` 
CHANGE COLUMN `image` `cover_map` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '优惠券图片' AFTER `is_auto_push`;