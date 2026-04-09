DROP TABLE IF EXISTS `lkt_diy_page`;
CREATE TABLE `lkt_diy_page` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '页面id',
    `store_id` int(11) NOT NULL COMMENT '商城id',
    `page_title` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '页面名称',
    `page_type` int(2) NOT NULL COMMENT '页面类型',
    `page_context` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '页面内容',
    `status` int(1) NOT NULL DEFAULT '1' COMMENT '状态 1 启用 2 不启用',
    `is_del` int(1) NOT NULL DEFAULT '2' COMMENT '删除 1是 2否',
    `mch_id` int(11) DEFAULT NULL COMMENT '店铺id',
    `sp_id` int(11) DEFAULT NULL COMMENT '供应商id',
    `add_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
    `update_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='DIY单个页面的配置信息';


DROP TABLE IF EXISTS `lkt_diy_project`;
CREATE TABLE `lkt_diy_project` (
       `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '项目id',
       `store_id` int(11) NOT NULL COMMENT '商城id',
       `admin_id` int(11) NOT NULL COMMENT '管理员id',
       `pro_title` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '项目名称',
       `industry` tinyblob COMMENT '所属行业',
       `pro_config` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '项目配置',
       `logo` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '项目logo',
       `cover` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '封面',
       `pro_pages` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '页面集合',
       `status` int(1) NOT NULL DEFAULT '1' COMMENT '状态 1启用 2不启用',
       `is_del` int(1) NOT NULL DEFAULT '2' COMMENT '删除  1 是 2 否',
       `mch_id` int(11) DEFAULT NULL COMMENT '店铺id',
       `sp_id` int(11) DEFAULT NULL COMMENT '供应商id',
       `add_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
       `update_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
       PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Diy项目配置信息';
