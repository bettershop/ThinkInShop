ALTER TABLE `lkt_supplier_config`
MODIFY COLUMN `is_examine`  int(11) NOT NULL DEFAULT 1 COMMENT '供应商商品是否需要审核 0.否 1.是' AFTER `wallet_unit`,
ADD COLUMN `withdrawal_instructions`  text NULL COMMENT '提现说明' AFTER `is_examine`;
