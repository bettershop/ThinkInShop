ALTER TABLE `lkt_product_list`
MODIFY COLUMN `gongyingshang`  varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 0 COMMENT '供应商' AFTER `recycle`;
