ALTER TABLE lkt_map
    CHANGE COLUMN `GroupID` `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '地区id' FIRST,
    CHANGE COLUMN `G_CName` `district_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '地区名称' AFTER `id`,
    CHANGE COLUMN `G_ParentID` `district_pid` int(11) NULL DEFAULT NULL COMMENT '父地区id' AFTER `district_name`,
    CHANGE COLUMN `G_ShowOrder` `district_show_order` int(11) NULL DEFAULT NULL COMMENT '显示排序' AFTER `district_pid`,
    CHANGE COLUMN `G_Level` `district_level` int(11) NULL DEFAULT NULL COMMENT '层级' AFTER `district_show_order`,
    CHANGE COLUMN `G_ChildCount` `district_childcount` int(11) NULL DEFAULT NULL COMMENT '子地区数（未用）' AFTER `district_level`,
    CHANGE COLUMN `G_Delete` `district_delete` int(11) NULL DEFAULT 0 COMMENT '是否删除' AFTER `district_childcount`,
    CHANGE COLUMN `G_Num` `district_num` int(11) NULL DEFAULT 0 COMMENT '（未用）' AFTER `district_delete`,
    ADD COLUMN `district_country_num` int(3) NULL COMMENT '国家编码' AFTER `district_num`,
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (`id`) USING BTREE;

update lkt_map set district_country_num = 156 ;


alter table lkt_order_data
    modify trade_no char(39) null comment '订单号';