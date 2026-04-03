ALTER TABLE `lkt_product_list`
MODIFY COLUMN `label`  varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品标签' AFTER `subtitle`;
ALTER TABLE `lkt_configure`
MODIFY COLUMN `color`  char(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '颜色' AFTER `name`,
MODIFY COLUMN `size`  varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '尺码' AFTER `color`;
ALTER TABLE `lkt_configure`
MODIFY COLUMN `bar_code`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '条形码' AFTER `unit`;

ALTER TABLE `lkt_data_dictionary_list`
MODIFY COLUMN `s_name`  varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '上级名称' AFTER `sid`;

