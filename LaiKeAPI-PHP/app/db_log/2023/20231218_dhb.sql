ALTER TABLE `lkt_product_list` 
ADD COLUMN `lower_image` varchar(255) NULL COMMENT '违规下架原因' AFTER `real_volume`;