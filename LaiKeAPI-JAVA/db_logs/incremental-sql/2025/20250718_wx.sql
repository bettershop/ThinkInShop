ALTER TABLE lkt_supplier
    ADD COLUMN `cpc` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '86' COMMENT '电话区号' AFTER `color`;

ALTER TABLE `lkt_customer`
    ADD COLUMN `cpc` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '86' COMMENT '区号' AFTER `store_langs`;

ALTER TABLE lkt_admin
    ADD COLUMN `cpc` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '86' COMMENT '区号' AFTER `lang`;