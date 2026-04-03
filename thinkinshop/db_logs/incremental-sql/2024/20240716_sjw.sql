ALTER TABLE `lkt_order_details`
    ADD COLUMN `platform_coupon_price` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '平台优惠券优惠金额' AFTER `write_time_id`,
    ADD COLUMN `store_coupon_price`    decimal(12, 2) NULL DEFAULT 0.00 COMMENT '店铺优惠券优惠金额' AFTER `platform_coupon_price`;