ALTER TABLE `lkt_mch`
ADD COLUMN `isInvoice`  tinyint(1) NULL DEFAULT 0 COMMENT '是否开发票' AFTER `is_margin`;
