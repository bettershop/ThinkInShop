ALTER TABLE lkt_jump_path
    ADD COLUMN mch_id INT COMMENT '店铺id' AFTER store_id;
ALTER TABLE lkt_jump_path
    ADD COLUMN lang_code VARCHAR(10) DEFAULT 'zh_CN' COMMENT '国际化语言 默认：中文简体 zh_CN' AFTER mch_id;