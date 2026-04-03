ALTER TABLE `lkt_supplier`
    MODIFY COLUMN `business_scope` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '经营范围' AFTER `dic_id`;