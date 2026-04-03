-- ################################ --
-- ################################ --
-- 这个脚本等框架更新后执行 目前不需要执行 --
-- ################################ --
-- ################################ --
ALTER TABLE  `lkt_data_dictionary_list`
    CHANGE COLUMN `text` `ctext` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '值' AFTER `value`;

ALTER TABLE `lkt_file_delivery`
    CHANGE COLUMN `text` `ctext` mediumtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '失败原因' AFTER `status`;

ALTER TABLE  `lkt_menu`
    CHANGE COLUMN `text` `ctext` mediumtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '菜单说明' AFTER `url`;

ALTER TABLE `lkt_user_role`
    CHANGE COLUMN `text` `ctext` mediumtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '角色说明' AFTER `name`;

ALTER TABLE lkt_files_record`
    CHANGE COLUMN `group` `group_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '分组' AFTER `store_type`;