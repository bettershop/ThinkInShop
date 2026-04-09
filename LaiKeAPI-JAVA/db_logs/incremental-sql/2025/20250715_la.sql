ALTER TABLE lkt_files_record
    ADD COLUMN img_type TINYINT(1) COMMENT '图片类型 0:业务图片 1：diy图片' DEFAULT 0 AFTER image_name;
ALTER TABLE lkt_files_record
    ADD COLUMN diy_img_type TINYINT(1) COMMENT 'diy模块图片类型 0：系统图片 1：自定义图片' AFTER img_type;
ALTER TABLE lkt_files_record
    ADD COLUMN diy_id INT COMMENT '主题id'  AFTER diy_img_type;