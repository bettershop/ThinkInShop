
ALTER TABLE `lkt_seconds_activity` 
ADD COLUMN `attr_id` int(11) NOT NULL DEFAULT 0 COMMENT '规格ID' AFTER `goodsId`,
MODIFY COLUMN `label_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标签ID' AFTER `goodsId`;