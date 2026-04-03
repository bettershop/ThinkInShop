--商品新增视频字段
alter table lkt_product_list add video varchar(255) null COMMENT '视频文件';

alter table lkt_product_list add pro_video varchar(255) null COMMENT '商品视频';