ALTER TABLE lkt_pre_sell_record
    ADD COLUMN paypal_id VARCHAR(255) NULL DEFAULT NULL COMMENT '贝宝支付订单id';