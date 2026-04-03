ALTER TABLE lkt_message ADD COLUMN international TINYINT (1) COMMENT '是否是国际短信 0：否 1：是' DEFAULT 0 AFTER SignName;

ALTER TABLE lkt_ds_country ADD COLUMN national_flag VARCHAR(255) COMMENT '国旗' AFTER code;

ALTER TABLE lkt_service_address ADD COLUMN country_num INT DEFAULT 156 COMMENT '国家代码 默认156' AFTER cpc