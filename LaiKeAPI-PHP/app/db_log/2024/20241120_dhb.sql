

ALTER TABLE `lkt_product_class` 
ADD COLUMN `mch_id` int(11) NOT NULL DEFAULT 0 COMMENT '店铺ID' AFTER `is_default`,
ADD COLUMN `supplier_id` int(11) NOT NULL DEFAULT 0 COMMENT '供应商ID' AFTER `mch_id`,
ADD COLUMN `examine` tinyint(4) NULL DEFAULT 0 COMMENT '审核状态 0.待审核 1.审核通过 2.不通过' AFTER `supplier_id`;
ADD COLUMN `remark` text NULL COMMENT '拒绝原因' AFTER `examine`;

ALTER TABLE `lkt_brand_class` 
ADD COLUMN `examine` tinyint(4) NULL DEFAULT 0 COMMENT '审核状态 0.待审核 1.审核通过 2.不通过' AFTER `is_default`,
ADD COLUMN `remark` text NULL COMMENT '拒绝理由' AFTER `examine`,
ADD COLUMN `mch_id` int(11) NULL DEFAULT 0 COMMENT '店铺ID' AFTER `remark`,
ADD COLUMN `supplier_id` int(11) NULL DEFAULT 0 COMMENT '供应商ID' AFTER `mch_id`;