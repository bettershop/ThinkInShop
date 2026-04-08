ALTER TABLE lkt_admin
    ADD COLUMN color VARCHAR(2000) DEFAULT '{"name":"topNav.Blue","icon":0,"color":"#2d6dcc","key":0}' NULL COMMENT '商城样式颜色，使用英文单词表示（如red）';

ALTER TABLE lkt_supplier
    ADD COLUMN color VARCHAR(2000) DEFAULT '{"name":"topNav.Blue","icon":0,"color":"#2d6dcc","key":0}' NULL COMMENT '商城样式颜色，使用英文单词表示（如red）';
