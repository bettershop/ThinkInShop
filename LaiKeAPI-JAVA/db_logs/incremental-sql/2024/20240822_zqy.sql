create table if not exists lkt_mch_statistics
(
    id           int(11) unsigned auto_increment comment '店铺数据汇总id' primary key,
    store_id     int default 0                    not null comment '商城id',
    mch_id       int(11)                          not null comment '店铺id',
    pending_shipment   int(11) default 0          not null comment '待发货订单',
    obligation   int(11) default 0                not null comment '待付款订单',
    refund_order int(11) default 0                not null comment '退款订单',
    audit_order  int(11) default 0                not null comment '待审核订单',
    audit_pro    int(11) default 0                not null comment '待审核商品',
    ckbz_pro     int(11) default 0                not null comment '库存不足商品',
    djs_order    int(11) default 0                not null comment '待结算订单',
    dsh_order    int(11) default 0                not null comment '待收货订单',
    sj_pro       int(11) default 0                not null comment '上架商品',
    xj_pro       int(11) default 0                not null comment '下架商品',
    pro_class    int(11) default 0                not null comment '商品分类',
    pro_brand    int(11) default 0                not null comment '商品品牌',
    sale_pro_sku int(11) default 0                not null comment '销售商品sku',
    pro_sku      int(11) default 0                not null comment '商品sku数量',
    customer_num int(11) default 0              not null comment '总客单数量',
    attention_user_num int(11) default 0              not null comment '关注的客户',
    access_user_num   int(11)  default 0              not null comment '访问的客户',
    new_pay_user int(11)  default 0              not null  comment '新增下单客户',
    djs_money    Decimal(10,2)  default 0              not null comment '待结算金额',
    ytx_money    Decimal(10,2)   default 0             not null comment '已提现金额',
    return_money Decimal(10,2)   default 0             not null  comment '退款金额',
    add_date     timestamp                                 comment '添加时间',
    recycle   int default 0 not null comment '回收站 0.显示 1.系统回收 2用户回收 3店铺回收'
    ) comment '店铺数据汇总表' collate = utf8mb4_general_ci;


create table if not exists lkt_mch_buy_power
(
    id           int(11) unsigned auto_increment comment '店铺购买力id' primary key,
    store_id     int default 0                    not null comment '商城id',
    mch_id       int(11)                          not null comment '店铺id',
    user_id      varchar(255)                              comment '用户id',
    money        Decimal(10,2)  default 0         not null comment '金额',
    add_date     timestamp                                 comment '添加时间',
    recycle   int default 0 not null comment '回收站 0.显示 1.系统回收 2用户回收 3店铺回收'
    ) comment '店铺购买力表' collate = utf8mb4_general_ci;


create table if not exists lkt_mch_order_record
(
    id           int(11) unsigned auto_increment comment '店铺交易数据id' primary key,
    store_id     int default 0                    not null comment '商城id',
    mch_id       int(11)                          not null comment '店铺id',
    count_day    timestamp                                     comment '日期',
    order_number int(11)                          not null comment '订单数量',
    money        Decimal(10,2)  default 0         not null comment '金额',
    add_date     timestamp                                 comment '添加时间',
    recycle   int default 0 not null comment '回收站 0.显示 1.系统回收 2用户回收 3店铺回收'
    ) comment '店铺交易数据表' collate = utf8mb4_general_ci;


alter table lkt_mch
    add new_user_order_num int default 0 null comment '新增下单客户数';