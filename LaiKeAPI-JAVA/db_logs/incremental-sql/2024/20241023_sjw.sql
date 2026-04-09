ALTER TABLE `lkt_seconds_activity`
ADD COLUMN `delete_method` int(11) NULL DEFAULT 0 COMMENT '删除方式 1手动删除 2定时任务删除 默认为0' AFTER `remind_json`;