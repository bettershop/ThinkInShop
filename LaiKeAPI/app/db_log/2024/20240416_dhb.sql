ALTER TABLE `lkt_admin_record` 
ADD COLUMN `operator_id` int(11) NULL DEFAULT 0 COMMENT '操作人ID' AFTER `mch_id`;
