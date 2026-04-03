-- 商品真是销量
alter table lkt_product_list add real_volume int(11) default 0 not null COMMENT '真实销量';

-- sku
alter table lkt_sku add is_examine int(11) default 0 not null COMMENT '审核状态 0.未审核  1.已审核';