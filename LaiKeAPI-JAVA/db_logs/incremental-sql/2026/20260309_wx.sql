-- 2026-03-09 插件排序字段新增
-- 给 lkt_plugins 表增加 plugin_sort 字段，默认值0，store_id隔离
ALTER TABLE `lkt_plugins` 
ADD COLUMN `plugin_sort` INT NOT NULL DEFAULT 0 COMMENT '插件排序，数值越大越靠前' AFTER `store_id`,
ADD INDEX `idx_store_sort` (`store_id`, `plugin_sort` DESC);
