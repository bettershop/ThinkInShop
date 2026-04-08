--商城id前缀
ALTER TABLE lkt_system_configuration
    ADD COLUMN `store_id_prefix` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商城id前缀' AFTER `add_time`;