--快递100默认开启
ALTER TABLE `v3_db`.`lkt_config`
MODIFY COLUMN `is_express` tinyint(4) NULL DEFAULT 1 COMMENT '是否开启快递100  0.不开启 1.开\r\n\r\n启' AFTER `push_MasterECRET`;