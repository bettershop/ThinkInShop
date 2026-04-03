ALTER TABLE `lkt_mch` 
ADD COLUMN `is_self_delivery` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否支持商家自配 0.否 1.是' AFTER `sub_app_id`,
ADD COLUMN `new_user_order_num` int(11) NULL DEFAULT 0 COMMENT '新增下单客户数' AFTER `is_self_delivery`;

CREATE TABLE `lkt_self_delivery_info`  (
    `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `delivery_time` varchar(255) null COMMENT '配送时间',
    `delivery_period` varchar(255) NULL COMMENT '配送时间段 1.上午 2.下午',
    `phone` varchar(255) NULL COMMENT '配送人电话',
    `courier_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '配送人姓名',
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='订单配送信息表';


ALTER TABLE `lkt_order`
    MODIFY COLUMN `self_lifting` tinyint(4) NULL DEFAULT 0 COMMENT '自提 0.配送 1.自提 2.商家自配 3.虚拟订单需要线下核销 4.虚拟订单无需线下核销' AFTER `remarks`;

ALTER TABLE `lkt_order_details`
    ADD COLUMN `store_self_delivery` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商家自配信息id' AFTER `store_coupon_price`;
    
alter table lkt_mch_distribution_record
    modify add_date timestamp null comment '添加时间';

create table if not exists lkt_backup_config
(
    id           int(11) unsigned auto_increment comment '配置id' primary key,
    store_id     int default 0                    not null comment '商城id',
    is_open      int(11)  default 0               not null comment '是否开启自动备份,0关闭，1开启',
    execute_cycle    varchar(255)                          comment '执行周期',
    url          varchar(255)                              comment '保存路径',
    add_time     timestamp                                 comment '添加时间',
    query_data   int(11)                                   comment '1.每天 2.N天 3.每小时 4.N小时 5.N分钟 6.每周 7.每月',
    recycle   int default 0 not null  comment '回收站 0.显示 1.系统回收 2用户回收 3店铺回收'
) comment '数据备份配置表' collate = utf8mb4_general_ci;


create table if not exists lkt_backup_record
(
    id           int(11) unsigned auto_increment comment '记录id' primary key,
    store_id     int default 0                    not null comment '商城id',
    file_name    varchar(255)                              comment '文件名称',
    file_size    varchar(255)                              comment '文件大小',
    file_type    varchar(255)                              comment '文件类型',
    file_url     varchar(255)                              comment '文件路径',
    status       int(11)                                   comment '状态',
    add_time     timestamp                                  comment '添加时间',
    recycle   int default 0 not null comment '回收站 0.显示 1.系统回收 2用户回收 3店铺回收'
) comment '数据备份记录表' collate = utf8mb4_general_ci;

create table if not exists lkt_backup_time
(
    id           int(11) unsigned auto_increment comment '记录id' primary key,
    query_data   int(11) comment '1.每天 2.N天 3.每小时 4.N小时 5.N分钟 6.每周 7.每月',
    add_time     timestamp comment '添加时间',
    recycle   int default 0 not null comment '回收站 0.显示 1.系统回收 2用户回收 3店铺回收'
) comment '数据备份时间表' collate = utf8mb4_general_ci;

-- 商户删除平台管理活动表
DROP TABLE IF EXISTS `lkt_platform_activities_del`;
CREATE TABLE `lkt_platform_activities_del` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `platform_activities_id` int(11) DEFAULT NULL COMMENT '平台活动ID',
    `mch_id` int(11) DEFAULT NULL COMMENT '商户ID',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户删除平台管理活动表';

-- 平台活动表改名
ALTER TABLE platform_activities RENAME lkt_platform_activities;

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

ALTER TABLE `lkt_flashsale_label`
    ADD COLUMN `update_source` int(11) NOT NULL DEFAULT 1 COMMENT '最后的更新来源 1为h5商家 2为pc店铺' AFTER `mch_id`;

alter table lkt_auction_promise
    add type varchar(255) null comment '支付方式';

alter table lkt_auction_promise
    add address_id int null comment '收货地址id';

alter table lkt_auction_promise
    add source int null comment '来源 1.小程序 2.app 3.支付宝小程序 4.头条小程序 5.百度小程序 6.pc端 7.H5';

