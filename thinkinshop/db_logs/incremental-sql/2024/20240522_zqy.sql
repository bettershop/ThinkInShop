create table if not exists lkt_living_comment
(
    id           int(11) unsigned auto_increment comment '评论id'
    primary key,
    store_id     int default 0                    not null comment '商城id',
    living_id    int                              not null comment '直播间id',
    user_id      char(15)                         null comment '用户id',
    comment      varchar(255) collate utf8mb4_bin not null comment '评论',
    p_id         int                              null comment '上级ID',
    add_time     timestamp                        null comment '添加时间',
    recycle      int default 0                    not null comment '回收站 0.显示 1.系统回收 2用户回收 3店铺回收',
    comment_type int                              null comment '评论类型 1评论，2关注，3点赞,4加入'
    )
    comment '直播间评论' collate = utf8mb4_general_ci;

create table if not exists lkt_living_commission
(
    id         int(11) unsigned auto_increment comment '佣金id'
    primary key,
    store_id   int            default 0    not null comment '商城id',
    user_id    char(15)                    null comment '用户id',
    living_id  int                         not null comment '直播间id',
    s_no        varchar(255)                not null comment '订单号',
    commission decimal(10, 2) default 0.00 not null comment '佣金金额',
    status     int                         not null comment '结算状态（101未发放，100已发放）',
    add_time   timestamp                   null comment '添加时间',
    recycle    int                         not null comment '回收站 0.显示 1.系统回收 2用户回收 3店铺回收'
    )
    comment '佣金表';

create table if not exists lkt_living_config
(
    id            int(11) unsigned auto_increment comment '配置id'
    primary key,
    store_id      int default 0 not null comment '商城id',
    is_open       int default 0 not null comment ' 获取是否开启插件 0-不开启 1-开启',
    push_url      varchar(255)  not null comment '推流地址',
    play_url      varchar(255)  not null comment '播放地址',
    agree_title   varchar(255)  not null comment '协议标题',
    agree_content text          not null comment '协议内容',
    add_time      timestamp     null comment '添加时间',
    recycle       int default 0 not null comment '回收站 0.显示 1.系统回收 2用户回收 3店铺回收'
    )
    comment '直播配置';

create table if not exists lkt_living_follow_list
(
    id        int auto_increment comment '关注列表id'
    primary key,
    store_id  int          default 0   not null comment '商城id',
    user_id   char(15)                 null comment '用户id',
    anchor_id varchar(255) default '0' not null comment '主播id',
    add_time  timestamp                null comment '添加时间',
    recycle   int                      not null comment '回收站 0.显示 1.系统回收 2用户回收 3店铺回收'
    )
    comment '用户关注列表';

create table if not exists lkt_living_product
(
    id        int(11) unsigned auto_increment comment '商品id'
    primary key,
    store_id  int default 0 not null comment '商城id',
    living_id int           not null comment '直播间id',
    pro_id    int           not null comment '产品id',
    config_id int           not null comment '规格id',
    num       int default 0 not null comment '数量',
    sort      int default 1 not null comment '排序',
    xl_num    int default 0 not null comment '销量',
    add_time  timestamp     null comment '添加时间',
    recycle   int default 0 not null comment '回收站 0.显示 1.系统回收 2用户回收 3店铺回收'
    )
    comment '直播间产品';

create table if not exists lkt_living_room
(
    id                 int(11) unsigned auto_increment comment 'id'
    primary key,
    store_id           int          default 0  not null comment '商城id',
    living_theme       varchar(255)            null comment '直播的标题',
    living_description varchar(255)            null comment '直播简介',
    start_time         timestamp               null comment '直播开始时间',
    living_time        char(15)                null comment '直播时长 分钟',
    end_time           timestamp               null comment '直播结束时间',
    living_status      int          default 0  not null comment '	直播的状态，0：预约中，1：直播中，2：已结束，3：已取消',
    living_img         varchar(255)            null comment '直播间封面图地址',
    add_time           timestamp               null comment '添加时间',
    user_id            varchar(15)             null comment '主播用户id',
    living_num         int          default 0  not null comment '直播间观看人数',
    living_review_num  int          default 0  not null comment '直播间评论数',
    push_url           varchar(255) default '' null comment '直播间推流地址',
    play_url           varchar(255) default '' not null comment '播放地址',
    like_num           int          default 0  not null comment '点赞数',
    recycle            int          default 0  not null comment '回收站 0.显示 1.系统回收 2用户回收 3店铺回收'
    )
    comment '直播间';

create table if not exists lkt_living_statistics
(
    id         int(11) unsigned auto_increment comment '统计表id'
    primary key,
    store_id   int            default 0    not null comment '商城id',
    user_id    char(15)                    null comment '主播id',
    commission decimal(10, 2) default 0.00 not null comment '佣金金额',
    fans_num   int            default 0    not null comment '粉丝数量',
    venue_num  int            default 0    not null comment '场次数',
    add_time   timestamp                   null comment '添加时间',
    recycle    int                         not null comment '回收站 0.显示 1.系统回收 2用户回收 3店铺回收',
    room_id    int                         not null comment '直播间id'
    )
    comment '统计表';

create table if not exists lkt_living_user_like
(
    id        int(11) unsigned auto_increment comment '我的点赞id'
    primary key,
    store_id  int default 0 not null comment '商城id',
    user_id   char(15)      null comment '用户id',
    living_id int default 0 not null comment '直播间id',
    dzs_num   int default 0 not null comment '点赞数',
    add_time  timestamp     null comment '添加时间',
    recycle   int default 0 not null comment '回收站 0.显示 1.系统回收 2用户回收 3店铺回收'
    )
    comment '我的点赞';


create table if not exists lkt_living_audience(
    id        int(11) unsigned auto_increment comment '观众id'
    primary key,
    store_id  int default 0 not null comment '商城id',
    user_id   char(15)      null comment '用户id',
    living_id int default 0 not null comment '直播间id',
    start_time timestamp  null comment '开始时间',
    end_time timestamp  null comment '结束时间',
    duration  long  null comment '观看时长',
    add_time  timestamp     null comment '添加时间',
    recycle   int default 0 not null comment '回收站 0.显示 1.系统回收 2用户回收 3店铺回收'
) comment '直播间观众';

alter table lkt_order_details
    add living_room_id varchar(255) null comment '直播间id';

alter table lkt_order_details
    add anchor_id varchar(255) null comment '主播id';

alter table lkt_order_details
    add commission decimal(10,2) null comment '佣金';

alter table lkt_configure
    add commission varchar(25) null comment '分佣比例';