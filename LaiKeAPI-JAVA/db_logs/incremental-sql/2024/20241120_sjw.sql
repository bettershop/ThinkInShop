ALTER TABLE `lkt_product_class`
ADD COLUMN `mch_id` int(11) NULL COMMENT '店铺id' AFTER `is_default`,
ADD COLUMN `examine` int(11) NULL COMMENT '审核状态 0.待审核 1.审核通过 2.不通过' AFTER `mch_id`,
ADD COLUMN `remark` varchar(255) NULL COMMENT '审核备注' AFTER `examine`,
ADD COLUMN `supplier_id` int(11) NULL COMMENT '供应商id' AFTER `remark`;
ALTER TABLE `lkt_brand_class`
ADD COLUMN `examine` int(11) NULL COMMENT '审核状态 0.待审核 1.审核通过 2.不通过' AFTER `is_default`,
ADD COLUMN `remark` varchar(255) NULL COMMENT '审核备注' AFTER `examine`,
ADD COLUMN `mch_id` int(11) NULL COMMENT '店铺id' AFTER `remark`,
ADD COLUMN `supplier_id` int(11) NULL COMMENT '供应商id' AFTER `mch_id`;

