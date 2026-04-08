ALTER TABLE `lkt_img_group`
MODIFY COLUMN `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '分组名称' AFTER `store_id`;