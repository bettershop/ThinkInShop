--全端商城公告优化修改
ALTER TABLE `lkt_system_tell`
    ADD COLUMN `store_tell` int(4) NOT NULL DEFAULT 1 COMMENT '管理后台公告  1未选择 2选择' AFTER `is_read`,
ADD COLUMN `user_tell` int(4) NOT NULL DEFAULT 1 COMMENT '用户公告  1未选择 2选择' AFTER `store_tell`,
ADD COLUMN `mch_tell` int(4) NOT NULL DEFAULT 1 COMMENT '商户公告  1未选择 2选择' AFTER `user_tell`,
ADD COLUMN `supplier_tell` int(4) NOT NULL DEFAULT 1 COMMENT '供应商公告  1未选择 2选择' AFTER `mch_tell`,
ADD COLUMN `mch_son_tell` int(4) NOT NULL DEFAULT 1 COMMENT '门店端公告  1未选择 2选择' AFTER `supplier_tell`;

ALTER TABLE `lkt_system_tell`
    MODIFY COLUMN `type` tinyint(4) NULL DEFAULT NULL COMMENT '公告类型: 1--系统维护  2--版本更新  3--普通公告' AFTER `title`;


CREATE TABLE `lkt_system_tell_user` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `store_id` int(11) NOT NULL COMMENT '商城ID',
    `tell_id` int(11) NOT NULL COMMENT '公告id',
    `read_id` varchar(50) NOT NULL COMMENT '标记以读用户id',
    `store_type` int(4) NOT NULL COMMENT '标记以读用户所在端 1微信小程序 , 2 h5, 11 app, 7 pc店铺, 6 pc商城 8 管理后台 9 PC门店核销, 10H5门店核销',
    `is_supplier` int(4) NOT NULL DEFAULT '0' COMMENT '是否为供应商  0不是  1是',
    `add_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '添加时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户端用户标记公告以读记录表';