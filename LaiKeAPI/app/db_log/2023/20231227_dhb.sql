
ALTER TABLE `lkt_group_order_config` 
ADD COLUMN `is_open` tinyint(4) NULL DEFAULT 0 COMMENT '是否开启拼团插件 插件状态 0关闭 1开启' AFTER `update_date`;