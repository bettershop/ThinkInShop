ALTER TABLE `lkt_order`
MODIFY COLUMN `remarks`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '订单备注' AFTER `baiduId`;

ALTER TABLE `lkt_record`
MODIFY COLUMN `main_id`  varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_croatian_ci NULL DEFAULT NULL COMMENT '外键id' AFTER `is_mch`;

ALTER TABLE `lkt_mch`
ADD COLUMN `poster_img`  varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '店铺新增宣传图' AFTER `cid`,
ADD COLUMN `head_img`  varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '店铺头像' AFTER `poster_img`,
ADD COLUMN `collection_code`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收款码' AFTER `head_img`;

