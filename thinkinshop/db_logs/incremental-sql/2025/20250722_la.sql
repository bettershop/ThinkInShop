ALTER TABLE lkt_diy_page
    ADD COLUMN type TINYINT(1) COMMENT '类型 1：系统页面 2：自定义页面' AFTER page_name;