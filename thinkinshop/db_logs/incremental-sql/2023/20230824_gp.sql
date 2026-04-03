--功能优化 禅道：35075
ALTER TABLE `lkt_config`
    ADD COLUMN `app_title` varchar(100) NULL COMMENT '小程序首页标题' AFTER `pc_mch_path`,
    ADD COLUMN `app_logo` varchar(100) NULL COMMENT '小程序授权登录logo' AFTER `app_title`;

CREATE TABLE `lkt_pc_mall_bottom` (
                                      `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
                                      `store_id` int(11) NOT NULL COMMENT '商城id',
                                      `image` varchar(255) NOT NULL COMMENT '图片地址',
                                      `title` varchar(100) DEFAULT NULL COMMENT '标题',
                                      `subheading` varchar(100) DEFAULT NULL COMMENT '副标题',
                                      `sort` int(11) NOT NULL DEFAULT '0' COMMENT '排序号',
                                      `add_date` datetime DEFAULT NULL COMMENT '添加时间',
                                      `update_date` datetime DEFAULT NULL COMMENT '修改时间',
                                      PRIMARY KEY (`id`)
                                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='PC商城底部栏图片配置';

CREATE TABLE `lkt_pc_mall_config` (
                                      `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
                                      `store_id` int(11) NOT NULL COMMENT '商城id',
                                      `type` int(11) DEFAULT NULL COMMENT '配置类型 1浏览器标签 2 登录页配置 3 首页配置',
                                      `value` text COMMENT '配置值',
                                      PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COMMENT='PC商城页面显示配置';



ALTER TABLE `lkt_pc_mall_config`
    ADD COLUMN `add_date` datetime NULL COMMENT '添加时间' AFTER `value`,
    ADD COLUMN `update_date` datetime NULL COMMENT '修改时间' AFTER `add_date`;