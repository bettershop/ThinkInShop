
ALTER TABLE `lkt_express_delivery` 
ADD COLUMN `logistics` text NULL COMMENT '物流信息' AFTER `subtable_id`,
ADD COLUMN `logistics_method` tinyint(4) NULL DEFAULT 0 COMMENT '快递类型 0.快递100  1.17track' AFTER `logistics`;