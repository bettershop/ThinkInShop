ALTER TABLE `lkt_img_group`
ADD COLUMN `is_show` int(11) NULL DEFAULT 1 COMMENT '该分类是否常驻显示，1为不常驻显示，2为常驻显示' AFTER `type`;
ALTER TABLE `lkt_files_record`
ADD COLUMN `add_user` varchar(255) NULL COMMENT '上传人' AFTER `name`;