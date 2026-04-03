ALTER TABLE `lkt_user_collection` 
MODIFY COLUMN `type` int(11) NULL DEFAULT 1 COMMENT '收藏类型 1.普通收藏 2.积分商城收藏 3.竞拍' AFTER `mch_id`;

ALTER TABLE `lkt_distribution_record` 
ADD COLUMN `user_level` int(11) NOT NULL DEFAULT 0 COMMENT '用户当前分销等级' AFTER `status`;

ALTER TABLE `lkt_product_list` 
MODIFY COLUMN `status` tinyint(3) NOT NULL DEFAULT 1 COMMENT '状态 1:待上架 2:上架 3:下架 4.违规下架 5.断供' AFTER `min_inventory`;