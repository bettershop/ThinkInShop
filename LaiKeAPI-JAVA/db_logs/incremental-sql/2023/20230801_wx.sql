-- 图片索引问题
ALTER TABLE `lkt_files_record` CHANGE `image_name` `image_name` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '图片名称';
ALTER TABLE `lkt_files_record` ADD INDEX(`image_name`);
ALTER TABLE `lkt_files_record` ADD INDEX(`add_time`);