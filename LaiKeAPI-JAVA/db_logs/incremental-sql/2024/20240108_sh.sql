-- 订单临时表来源
ALTER TABLE `lkt_order_data` ADD COLUMN `source` int(10) not null DEFAULT 1 COMMENT '来源';