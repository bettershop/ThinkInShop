--积分商品上下架状态
ALTER TABLE `v3_db`.`lkt_integral_goods`
    ADD COLUMN `status` int(11) NOT NULL DEFAULT 1 COMMENT '上架状态 1待上架， 2已上架 ， 3已下架' AFTER `sort`;