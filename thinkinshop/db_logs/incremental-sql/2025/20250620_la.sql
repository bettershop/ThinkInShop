ALTER TABLE lkt_diy
    ADD COLUMN tab_bar MEDIUMTEXT COMMENT '导航' AFTER value;
ALTER TABLE lkt_diy
    ADD COLUMN tabber_info MEDIUMTEXT COMMENT '导航配置信息' AFTER value;