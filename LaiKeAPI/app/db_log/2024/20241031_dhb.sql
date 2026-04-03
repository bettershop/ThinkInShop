ALTER TABLE `lkt_flashsale_activity` 
CHANGE COLUMN `goodsId` `goods_id` int(11) NOT NULL COMMENT '商品id' AFTER `store_id`;