ALTER TABLE lkt_message_logging
    MODIFY COLUMN add_date timestamp NULL DEFAULT '0000-00-00 00:00:00' COMMENT '添加时间' AFTER read_or_not;

ALTER TABLE lkt_customer
    MODIFY COLUMN add_date timestamp NULL DEFAULT NULL COMMENT '购买时间' AFTER `function`;

ALTER TABLE lkt_express
    MODIFY COLUMN add_date timestamp NOT NULL COMMENT '添加时间' AFTER `recycle`;

ALTER TABLE lkt_sku
    MODIFY COLUMN add_date timestamp NULL DEFAULT '0000-00-00 00:00:00' COMMENT '添加时间' AFTER `recycle`;

ALTER TABLE lkt_mch
    MODIFY COLUMN last_login_time timestamp NULL DEFAULT NULL COMMENT '最后登录时间' AFTER is_open_coupon;

ALTER TABLE lkt_mch_class
    MODIFY COLUMN add_date timestamp NOT NULL COMMENT '添加时间' AFTER `recycle`;

ALTER TABLE lkt_mch_promise
    MODIFY COLUMN add_date timestamp NULL DEFAULT NULL COMMENT '添加时间' AFTER is_return_pay;

ALTER TABLE lkt_promise_sh
    MODIFY COLUMN add_date timestamp NULL DEFAULT NULL COMMENT '添加时间' AFTER is_pass;

ALTER TABLE lkt_customer
    MODIFY COLUMN end_date timestamp NULL DEFAULT NULL COMMENT '到期时间' AFTER add_date;


ALTER TABLE lkt_pc_mall_bottom
    MODIFY COLUMN add_date timestamp NULL DEFAULT NULL COMMENT '添加时间' AFTER sort,
    MODIFY COLUMN update_date timestamp NULL DEFAULT NULL COMMENT '修改时间' AFTER add_date;

ALTER TABLE lkt_message
    MODIFY COLUMN add_time timestamp NOT NULL COMMENT '创建时间' AFTER content;

ALTER TABLE lkt_product_list
    MODIFY COLUMN outage_time timestamp NULL DEFAULT NULL COMMENT '断供时间' AFTER lower_remark;

ALTER TABLE lkt_product_list
    MODIFY COLUMN examine_time timestamp NULL DEFAULT NULL COMMENT '审核时间' AFTER pro_introduce;

ALTER TABLE lkt_product_list
    MODIFY COLUMN violation_time timestamp NULL DEFAULT NULL COMMENT '违规下架时间' AFTER examine_time;

ALTER TABLE lkt_block_product
    MODIFY COLUMN add_date timestamp NULL DEFAULT NULL COMMENT '添加时间' AFTER sort;

ALTER TABLE lkt_block_home
    MODIFY COLUMN add_date timestamp NULL DEFAULT NULL COMMENT '添加时间' AFTER keyword;

ALTER TABLE lkt_brand_class
    MODIFY COLUMN brand_time timestamp NULL DEFAULT NULL COMMENT '时间' AFTER status;

ALTER TABLE lkt_supplier_brand
    MODIFY COLUMN brand_time timestamp NULL DEFAULT NULL COMMENT '时间' AFTER status;

ALTER TABLE lkt_express_delivery
    MODIFY COLUMN deliver_time timestamp NULL DEFAULT NULL COMMENT '发货时间' AFTER num;

ALTER TABLE lkt_express_subtable
    MODIFY COLUMN add_time timestamp NULL DEFAULT NULL COMMENT '添加时间' AFTER check_man;

ALTER TABLE lkt_return_order
    MODIFY COLUMN re_time timestamp NOT NULL COMMENT '申请售后时间' AFTER real_money;

ALTER TABLE lkt_file_delivery
    MODIFY COLUMN add_date timestamp NULL DEFAULT NULL COMMENT '发货时间' AFTER mch_id,
    MODIFY COLUMN update_date timestamp NULL DEFAULT NULL AFTER add_date;

ALTER TABLE lkt_group_open
    MODIFY COLUMN add_date timestamp NOT NULL COMMENT '添加时间' AFTER is_settlement,
    MODIFY COLUMN end_date timestamp NOT NULL COMMENT '拼团结束时间' AFTER add_date,
    MODIFY COLUMN update_date timestamp NULL DEFAULT NULL COMMENT '修改时间' AFTER end_date;

ALTER TABLE lkt_coupon_sno
    MODIFY COLUMN add_date timestamp NULL DEFAULT '0000-00-00 00:00:00' COMMENT '添加时间' AFTER recycle;

ALTER TABLE lkt_coupon_presentation_record
    MODIFY COLUMN add_date timestamp NULL DEFAULT '0000-00-00 00:00:00' COMMENT '添加时间' AFTER activity_type;

ALTER TABLE lkt_pre_sell_record
    MODIFY COLUMN add_time timestamp NULL DEFAULT NULL COMMENT '订金支付时间' AFTER is_delete,
    MODIFY COLUMN pay_balance_time timestamp NULL DEFAULT NULL COMMENT '尾款支付时间' AFTER add_time;

ALTER TABLE lkt_supplier
    MODIFY COLUMN expire_date timestamp NOT NULL COMMENT '到期时间' AFTER price,
    MODIFY COLUMN add_date timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间' AFTER recovery,
    MODIFY COLUMN last_login_date timestamp NULL DEFAULT NULL COMMENT '最后登录时间' AFTER surplus_balance;

ALTER TABLE lkt_integral_goods
    MODIFY COLUMN add_time timestamp NULL DEFAULT NULL COMMENT '添加时间' AFTER is_delete,
    MODIFY COLUMN update_time timestamp NULL DEFAULT NULL COMMENT '修改时间' AFTER add_time;

ALTER TABLE lkt_plugins
    MODIFY COLUMN optime timestamp NULL DEFAULT NULL COMMENT '安装时间' AFTER plugin_img;

ALTER TABLE lkt_member_config
    MODIFY COLUMN add_time timestamp NOT NULL COMMENT '添加时间' AFTER member_equity;

ALTER TABLE lkt_promise_record
    MODIFY COLUMN status varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT 0 COMMENT '审核状态 0=审核中 1=通过 2=拒绝' AFTER type;



































