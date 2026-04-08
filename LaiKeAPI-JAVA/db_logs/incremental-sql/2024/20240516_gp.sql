ALTER TABLE `lkt_finance_config`
    MODIFY COLUMN `service_charge` decimal(12, 3) NOT NULL DEFAULT 0.00 COMMENT '手续费' AFTER `max_amount`;

ALTER TABLE `lkt_mch_config`
    MODIFY COLUMN `service_charge` decimal(12, 3) NULL DEFAULT 0.00 COMMENT '手续费' AFTER `max_charge`;

ALTER TABLE `lkt_supplier_config`
    MODIFY COLUMN `commission` decimal(12, 3) NULL DEFAULT NULL COMMENT '手续费' AFTER `max_withdrawal`;

ALTER TABLE `v3_db`.`lkt_order`
    MODIFY COLUMN `p_sNo` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '父类订单号' AFTER `grade_fan`;