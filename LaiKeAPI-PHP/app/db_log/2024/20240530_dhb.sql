
ALTER TABLE `lkt_product_class` 
MODIFY COLUMN `english_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '英文名称' AFTER `pname`,
MODIFY COLUMN `bg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '小图标' AFTER `img`;
ALTER TABLE `lkt_product_list` 
MODIFY COLUMN `show_adr` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT ',1,' COMMENT '展示位置:1.\n首页 2.购物车 3.分类 4.我的-推荐' AFTER `search_num`;

ALTER TABLE `lkt_user` 
MODIFY COLUMN `money` decimal(12, 2) NOT NULL COMMENT '金额' AFTER `detailed_address`;

ALTER TABLE `lkt_payment_config` 
MODIFY COLUMN `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否显示 0否 1是' AFTER `pid`;
ALTER TABLE `lkt_files_record` 
MODIFY COLUMN `mch_id` int(11) NOT NULL DEFAULT 0 COMMENT '店铺ID' AFTER `add_time`;
