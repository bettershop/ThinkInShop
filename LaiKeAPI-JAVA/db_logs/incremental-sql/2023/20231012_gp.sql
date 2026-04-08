--优化插件是否支持pc店铺
ALTER TABLE lkt_plugins
    ADD COLUMN `is_mch_plugin` int(11) NOT NULL DEFAULT 1 COMMENT '插件是否支持pc店铺 1支持 2不支持' AFTER `menu_id`;

update lkt_plugins set is_mch_plugin = 2 WHERE id IN (6,7,9,12,13,15);



--优化  商品楼层
ALTER TABLE lkt_block_home
    ADD COLUMN `enable_or_not` int(11) NULL COMMENT '楼层是否开启显示 1显示  2不显示' AFTER `store_id`;

ALTER TABLE lkt_block_home
    MODIFY COLUMN `enable_or_not` int(11) NULL DEFAULT 1 COMMENT '楼层是否开启显示 1显示  2不显示' AFTER `store_id`;