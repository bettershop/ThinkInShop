-- 商户删除平台管理活动表
DROP TABLE IF EXISTS `lkt_platform_activities_del`;
CREATE TABLE `lkt_platform_activities_del` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `platform_activities_id` int(11) DEFAULT NULL COMMENT '平台活动ID',
    `mch_id` int(11) DEFAULT NULL COMMENT '商户ID',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户删除平台管理活动表';

-- 平台活动表改名
ALTER TABLE platform_activities RENAME lkt_platform_activities;
