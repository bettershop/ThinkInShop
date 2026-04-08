alter table lkt_auction_special
    modify start_date timestamp not null comment '开始时间';

alter table lkt_auction_special
    modify end_date timestamp not null comment '结束时间';

alter table lkt_auction_special
    modify sign_end_date timestamp null comment '报名截至时间';

alter table lkt_menu
    modify add_date timestamp comment '创建时间';

alter table lkt_menu
    modify update_date timestamp comment '修改时间';

alter table lkt_user_role
    modify add_date timestamp null comment '创建时间';

alter table lkt_user_role
    modify update_date timestamp null comment '修改时间';

alter table lkt_user_authority
    modify add_date timestamp null comment '创建时间';

alter table lkt_user_authority
    modify update_date timestamp null comment '修改时间';

alter table lkt_flashsale_label
    modify starttime timestamp null comment '活动开始时间';

alter table lkt_flashsale_label
    modify endtime timestamp null comment '活动结束时间';

alter table lkt_flashsale_label
    modify add_date timestamp null comment '添加时间';

alter table lkt_flashsale_label
    modify update_date timestamp null on update CURRENT_TIMESTAMP;