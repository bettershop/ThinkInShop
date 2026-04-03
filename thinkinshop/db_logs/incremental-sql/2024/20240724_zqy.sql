alter table lkt_living_room
    add living_fans_num int(11) default 0 not null comment '观看粉丝数';

alter table lkt_living_room
    add fans_num int(11) default 0 not null comment '新增粉丝数';

alter table lkt_living_config
    add mch_is_open int(11) default 0 not null comment '店铺端入口设置 0-不开启 1-开启' after is_open;

alter table lkt_living_product
    add represent int(11) default 0 not null comment '是否在讲解（每个直播间只能有一个讲解的产品，默认0不讲解）';

alter table lkt_living_product
    add sort_num int(11) default 0 not null comment '序号';