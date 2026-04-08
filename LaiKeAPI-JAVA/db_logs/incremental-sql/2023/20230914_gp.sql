--商品标签颜色优化
ALTER TABLE lkt_pro_label
    MODIFY COLUMN `color` varchar(50) NULL DEFAULT 1 COMMENT '颜色 编码' AFTER `add_time`;

--优化：商城默认角色
ALTER TABLE lkt_role
    MODIFY COLUMN `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 0:角色 1:客户端  3：商城默认角色' AFTER `permission`;