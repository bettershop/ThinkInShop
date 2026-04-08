ALTER TABLE `lkt_config`
    MODIFY COLUMN `is_push` tinyint(4) NULL DEFAULT 1 COMMENT '是否推送 0.不推送 1.推送' AFTER `exp_time`;