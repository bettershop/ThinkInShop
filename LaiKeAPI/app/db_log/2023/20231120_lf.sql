ALTER TABLE `lkt_system_configuration` ADD COLUMN `admin_default_portrait` varchar(255) DEFAULT NULL COMMENT '管理员默认头像';
ALTER TABLE `lkt_core_menu` ADD COLUMN `is_tab` int(4) DEFAULT '0' COMMENT '是否为tab页面    0不是  1是';
ALTER TABLE `lkt_plugins` ADD COLUMN `menu_id` varchar(50) DEFAULT NULL COMMENT '插件对应通用菜单表id(用于控制pc店铺插件菜单是否显示)';
ALTER TABLE `lkt_plugins` ADD COLUMN  `is_mch_plugin` int(11) NOT NULL DEFAULT '1' COMMENT '插件是否支持pc店铺 1支持 2不支持';
