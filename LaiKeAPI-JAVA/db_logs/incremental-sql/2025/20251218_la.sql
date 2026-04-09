ALTER TABLE lkt_config ADD COLUMN html_icon VARCHAR(255) COMMENT '网页标题icon' AFTER app_logo;
ALTER TABLE lkt_config ADD COLUMN store_name VARCHAR(255) COMMENT '商城名称' AFTER app_logo;