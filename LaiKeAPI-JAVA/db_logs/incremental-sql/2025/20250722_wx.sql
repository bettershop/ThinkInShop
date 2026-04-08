ALTER TABLE lkt_product_list
    MODIFY COLUMN `product_class` varchar (32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '产品类别' AFTER `scan`,
    MODIFY COLUMN `brand_id` int NULL DEFAULT 0 COMMENT '品牌ID' AFTER `status`;

ALTER TABLE `lkt_product_class`
    ADD COLUMN `notset` int NULL DEFAULT 0 COMMENT '未设置 默认值为0  为1的时候表示是未设置 ' AFTER `country_num`;

ALTER TABLE `lkt_brand_class`
    ADD COLUMN `notset` int NULL DEFAULT 0 COMMENT '为1 表示为未设置查询条件' AFTER `lang_code`;