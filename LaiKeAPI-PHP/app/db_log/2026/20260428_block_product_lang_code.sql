ALTER TABLE `lkt_block_product`
ADD COLUMN `lang_code` varchar(20) NULL DEFAULT '' COMMENT '语种编码' AFTER `product_id`;
