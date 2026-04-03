
ALTER TABLE `lkt_freight` 
MODIFY COLUMN `freight` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '指定运费' AFTER `type`,
MODIFY COLUMN `no_delivery` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '不配送地区' AFTER `is_no_delivery`,
MODIFY COLUMN `default_freight` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '默认运费' AFTER `supplier_id`,
ADD COLUMN `threeIdsList` text NULL COMMENT '不配送地区ID' AFTER `default_freight`;