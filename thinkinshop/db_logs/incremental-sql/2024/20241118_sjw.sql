ALTER TABLE `lkt_coupon_activity`
ADD COLUMN `cover_map` varchar(255) NULL COMMENT '优惠券图片' AFTER `is_auto_push`;
ALTER TABLE `lkt_sign_record`
ADD COLUMN `notice` int(11) NOT NULL DEFAULT 0 COMMENT '是否通知 0否1是' AFTER `frozen_time`;