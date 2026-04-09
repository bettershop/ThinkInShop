-- pc店铺设置商品排序
ALTER TABLE `lkt_product_list` ADD COLUMN `mch_sort` int(10)  not null DEFAULT 1  COMMENT '店铺排序值' AFTER `sort`;