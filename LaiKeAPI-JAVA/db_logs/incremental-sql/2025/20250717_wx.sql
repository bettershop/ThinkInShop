-- 分类首页不显示问题
UPDATE lkt_product_class SET is_display = 1
-- is_display 默认为1
ALTER TABLE lkt_product_class
    MODIFY COLUMN `is_display` tinyint NULL DEFAULT 1 COMMENT '是否显示 0.不显示 1.显示' AFTER `recycle`;

ALTER TABLE `lkt_service_address`
    ADD COLUMN `cpc` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '86' COMMENT '区号' AFTER `is_default`;