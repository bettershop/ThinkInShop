-- ============================================================
-- 废弃表清理 SQL (V3 独有 9 张废弃表)
-- 执行前请确保已完成本地 mysqldump 快照备份
-- 执行后请重启 Java 服务确保 Mapper 缓存刷新
-- ============================================================

-- 砍价插件表 (4张)
DROP TABLE IF EXISTS `lkt_bargain_config`;
DROP TABLE IF EXISTS `lkt_bargain_goods`;
DROP TABLE IF EXISTS `lkt_bargain_order`;
DROP TABLE IF EXISTS `lkt_bargain_record`;

-- 后台菜单备份表 (1张)
DROP TABLE IF EXISTS `lkt_core_menu1`;

-- 平台活动表 (2张)
DROP TABLE IF EXISTS `lkt_platform_activities`;
DROP TABLE IF EXISTS `lkt_platform_activities_del`;

-- 插件表 (1张)
DROP TABLE IF EXISTS `lkt_plug_ins`;

-- 用户角色备份表 (1张)
DROP TABLE IF EXISTS `lkt_user_role_copy1`;
