ALTER TABLE lkt_diy
    ADD COLUMN `lang_code` varchar(255) NULL DEFAULT 'zh_CN' COMMENT '语言' AFTER `cover`;