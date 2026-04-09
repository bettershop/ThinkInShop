-- 页面tab权限优化
ALTER TABLE `lkt_core_menu`
    ADD COLUMN `is_tab` int(4)  NULL DEFAULT 0  COMMENT '是否为tab页面    0不是  1是' AFTER `is_button`;