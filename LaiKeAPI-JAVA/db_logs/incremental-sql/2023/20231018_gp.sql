--新增管理员默认头像
ALTER TABLE lkt_system_configuration
    ADD COLUMN `admin_default_portrait` varchar(255) NULL COMMENT '管理员默认头像' AFTER `store_id_prefix`;