ALTER TABLE `lkt_mch_store`
ADD COLUMN `recycle`  tinyint(4) NULL DEFAULT 0 COMMENT '回收站 0.不回收 1.回收' AFTER `is_default`;

