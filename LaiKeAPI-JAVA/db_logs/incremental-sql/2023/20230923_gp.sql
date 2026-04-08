ALTER TABLE lkt_plugins
    ADD COLUMN `menu_id` varchar(50) NULL COMMENT '插件对应通用菜单表id(用于控制pc店铺插件菜单是否显示)' AFTER `content`;

update lkt_plugins set menu_id = '1598230490370801664' WHERE plugin_name = '优惠券'
update lkt_plugins set menu_id = '1479711563790155776' WHERE plugin_name = '秒杀'
update lkt_plugins set menu_id = '1479711767570415616' WHERE plugin_name = '积分商城'
update lkt_plugins set menu_id = '1560593459448905728' WHERE plugin_name = '竞拍'
update lkt_plugins set menu_id = '1668448694791307264' WHERE plugin_name = '预售'
update lkt_plugins set menu_id = '1650405998696857600' WHERE plugin_name = '拼团'
