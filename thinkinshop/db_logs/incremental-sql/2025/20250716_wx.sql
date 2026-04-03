ALTER TABLE lkt_mch_store
    ADD COLUMN `cpc` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '86' COMMENT '电话区号' AFTER `is_default`;

ALTER TABLE lkt_user_address
    ADD COLUMN `cpc` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '86' COMMENT '区号' AFTER `is_default`;