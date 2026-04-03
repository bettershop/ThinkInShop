

ALTER TABLE `lkt_return_record` 
ADD COLUMN `is_it_manual` tinyint(4) NOT NULL DEFAULT 1 COMMENT '是否是人工 0.系统自动 1.人工' AFTER `p_id`;