alter table lkt_auction_session
    modify start_date timestamp not null comment '开始时间';

alter table lkt_auction_session
    modify end_date timestamp not null comment '结束时间';

alter table lkt_auction_session
    modify add_date timestamp null comment '添加时间';

alter table lkt_auction_session
    modify update_date timestamp null on update CURRENT_TIMESTAMP;

alter table lkt_auction_special
    modify add_date timestamp null comment '添加时间';

alter table lkt_auction_special
    modify update_date timestamp null on update CURRENT_TIMESTAMP;