ALTER TABLE `lkt_plugins`
ADD COLUMN `store_id`  int(11) NULL DEFAULT 0 COMMENT '商城ID' AFTER `id`;
ALTER TABLE `lkt_plugins`
MODIFY COLUMN `status`  int(1) NULL DEFAULT 1 COMMENT '插件状态 0.关闭 1.开启' AFTER `optime`,
MODIFY COLUMN `flag`  int(1) NOT NULL DEFAULT 0 COMMENT '是否有权限 0.有 1.没有' AFTER `status`;
