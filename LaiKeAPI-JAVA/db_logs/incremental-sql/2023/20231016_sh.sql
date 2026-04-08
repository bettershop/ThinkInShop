-- 商品配置
ALTER TABLE lkt_product_config
    ADD COLUMN `is_display_sell_put` tinyint(4) NOT NULL DEFAULT 1 COMMENT '是否显示已售罄商品 0.不显示  1.显示';

