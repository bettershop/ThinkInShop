-- 供应商商品增加建议零售价字段
ALTER TABLE `lkt_configure` ADD COLUMN `msrp` decimal(12,2)  not null DEFAULT 0  COMMENT '建议零售价' AFTER `yprice`;