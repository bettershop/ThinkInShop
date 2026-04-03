alter table lkt_configure
    add live_price decimal(10, 2) default 0.00 not null comment '直播价格';

alter table lkt_group_open_record
    modify add_date timestamp not null comment '添加时间';