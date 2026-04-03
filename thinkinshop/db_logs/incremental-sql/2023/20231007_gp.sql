--订单失效时间
ALTER TABLE lkt_order
    ADD COLUMN `order_failure_time` int(11) NULL DEFAULT NULL  COMMENT '订单当前失效时间' AFTER `arrive_time`;
