ALTER TABLE `lkt_living_product`
    ADD COLUMN `live_price` decimal(12, 2) NOT NULL default 0 COMMENT '直播价格' AFTER `config_id`;