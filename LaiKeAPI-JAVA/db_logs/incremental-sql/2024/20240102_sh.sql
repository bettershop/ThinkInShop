-- 配置信息增加商城逻辑
ALTER TABLE `lkt_plugins` ADD COLUMN `store_id` int(10,0) not null DEFAULT 0  COMMENT '商城id' AFTER `id`;