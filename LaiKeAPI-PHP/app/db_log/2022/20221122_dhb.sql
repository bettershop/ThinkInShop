
CREATE TABLE `lkt_pre_sell_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键自增',
  `store_id` int(11) NOT NULL COMMENT '商城id',
  `is_open` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否开启 1 是 0 否',
  `deposit_desc` text NOT NULL COMMENT '订金预售说明',
  `balance_desc` text NOT NULL COMMENT '订货预售说明',
  `order_failure` int(11) NOT NULL DEFAULT '0' COMMENT '订单失效 (单位 秒)',
  `order_after` int(11) NOT NULL DEFAULT '604800' COMMENT '订单售后时间 (单位秒)',
  `auto_the_goods` int(11) NOT NULL DEFAULT '7' COMMENT '自动收货时间（单位秒）',
  `deliver_remind` int(11) NOT NULL DEFAULT '3600' COMMENT '提醒发货限制 间隔(单位秒)',
  `package_settings` tinyint(4) NOT NULL DEFAULT '0' COMMENT '多件包邮设置 0.未开启 1.开启',
  `same_piece` int(11) NOT NULL DEFAULT '0' COMMENT '同件',
  `same_order` int(11) NOT NULL DEFAULT '0' COMMENT '同单',
  `auto_good_comment_day` int(5) NOT NULL DEFAULT '0' COMMENT '自动评价设置几天后自动好评',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB CHARSET=utf8 COMMENT='预售配置信息';

CREATE TABLE `lkt_pre_sell_goods` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '唯一标识',
  `store_id` int(11) NOT NULL COMMENT '商城ID',
  `product_id` int(20) NOT NULL COMMENT '商品ID',
  `product_title` varchar(255) NOT NULL COMMENT '商品名称',
  `sell_type` int(10) NOT NULL DEFAULT '1' COMMENT '预售类型 1.订金模式 2.订货模式',
  `deposit` decimal(20,2) DEFAULT NULL COMMENT '订金金额',
  `pay_type` int(10) DEFAULT NULL COMMENT '订金支付类型',
  `deposit_start_time` datetime DEFAULT NULL COMMENT '订金支付开始时间',
  `deposit_end_time` datetime DEFAULT NULL COMMENT '订金支付结束时间',
  `balance_pay_time` datetime DEFAULT NULL COMMENT '尾款支付日期',
  `sell_num` int(20) DEFAULT NULL COMMENT '预售数量',
  `surplus_num` int(20) DEFAULT NULL COMMENT '剩余数量',
  `end_day` int(20) DEFAULT '1' COMMENT '截止天数',
  `deadline` datetime DEFAULT NULL COMMENT '截止时间',
  `delivery_time` int(10) NOT NULL COMMENT '发货时间(天)',
  `is_on_shelf` int(10) NOT NULL DEFAULT '0' COMMENT '是否上架过',
  `is_display` int(10) NOT NULL DEFAULT '1' COMMENT '是否显示 0.否  1.是',
  `is_delete` int(10) NOT NULL DEFAULT '0' COMMENT '是否删除  0.否  1.是',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB CHARSET=utf8 COMMENT='预售商品表';

CREATE TABLE `lkt_pre_sell_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id自增',
  `store_id` int(11) NOT NULL COMMENT '商城id',
  `user_id` varchar(50) NOT NULL COMMENT '用户id',
  `product_id` int(11) NOT NULL COMMENT '商品id',
  `attr_id` int(11) NOT NULL COMMENT '规格id',
  `price` decimal(11,2) NOT NULL COMMENT '当前付款金额',
  `deposit` decimal(11,2) DEFAULT NULL COMMENT '应付订金金额',
  `balance` decimal(11,2) DEFAULT NULL COMMENT '应付尾款金额',
  `num` int(11) NOT NULL COMMENT '数量',
  `sNo` varchar(255) NOT NULL COMMENT '预售订单号',
  `is_deposit` int(10) DEFAULT NULL COMMENT '是否支付订金  0.否  1.是',
  `is_balance` int(10) DEFAULT NULL COMMENT '是否支付尾款  0.否  1.是',
  `is_refund` int(10) DEFAULT '0' COMMENT '是否退款成功  0.否  1.是',
  `is_delete` int(10) NOT NULL DEFAULT '0' COMMENT '是否删除 1是 0否',
  `add_time` datetime DEFAULT NULL COMMENT '订金支付时间',
  `pay_balance_time` datetime DEFAULT NULL COMMENT '尾款支付时间',
  `order_info` text COMMENT '订单信息',
  `order_details_info` text COMMENT '订单详情信息',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB CHARSET=utf8 COMMENT='预售记录表';

-- 积分商城设置表
CREATE TABLE `lkt_integral_config` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `store_id` int(11) NOT NULL DEFAULT '0' COMMENT '商城id',
  `bg_img` varchar(255) DEFAULT NULL,
  `content` mediumtext,
  `status` int(5) NOT NULL DEFAULT '0' COMMENT '插件状态 0关闭 1开启',
  `same_piece` int(11) DEFAULT NULL COMMENT '同件n件包邮',
  `package_settings` int(11) NOT NULL DEFAULT '0' COMMENT '多件包邮设置 0.未开启 1.开启',
  `order_failure` int(11) NOT NULL DEFAULT '0' COMMENT '订单失效 (单位 秒)',
  `order_after` int(3) NOT NULL DEFAULT '604800' COMMENT '订单售后时间 (单位秒)',
  `auto_the_goods` int(11) NOT NULL DEFAULT '604800' COMMENT '自动收货时间',
  `deliver_remind` int(11) NOT NULL DEFAULT '3600' COMMENT '提醒发货限制 间隔(单位 秒)',
  `auto_good_comment_day` int(5) NOT NULL DEFAULT '0' COMMENT '自动评价设置几后自动好评',
  `proportion` decimal(10,0) DEFAULT NULL COMMENT '积分比例(购物赠送积分=购物交易金额*赠送比例)',
  `give_status` int(11) DEFAULT '0' COMMENT '发放状态(0=收货后 1=付款后)',
  `overdue_time` int(11) DEFAULT '604800' COMMENT '积分有效时间从积分获取后开始计算，至 x 失效',
  `mch_id` int(11) NOT NULL DEFAULT 0 COMMENT '店铺id',
  `ams_time` int(11) DEFAULT '0' COMMENT '收货后多少天返回积分，至 x 失效',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='积分商城设置表';

-- 积分商城商品表
CREATE TABLE `lkt_integral_goods` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `store_id` int(11) NOT NULL DEFAULT '0' COMMENT '商城id',
  `goods_id` int(11) NOT NULL DEFAULT 0 COMMENT '商品ID',
  `attr_id` int(11) NOT NULL DEFAULT 0 COMMENT '属性id',
  `integral` int(11) NOT NULL DEFAULT '0' COMMENT '兑换所需积分',
  `money` decimal(11,2) NOT NULL DEFAULT '0.00' COMMENT '兑换所需余额',
  `num` int(11) NOT NULL DEFAULT '0' COMMENT '库存数量',
  `max_num` int(11) NOT NULL DEFAULT '0' COMMENT '上架库存',
  `sort` int(11) NOT NULL DEFAULT '1' COMMENT '排序',
  `is_delete` smallint(6) NOT NULL DEFAULT '0' COMMENT '是否删除:0否   1是',
  `add_time` datetime DEFAULT NULL COMMENT '添加时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB CHARSET=utf8mb4 COMMENT='积分商城商品表';

ALTER TABLE `lkt_order`
ADD COLUMN `integral_id`  int(11) NULL DEFAULT 0 COMMENT '积分商品ID' AFTER `deposit`;
ALTER TABLE `lkt_auction_config`
ADD COLUMN `order_failure`  int(11) NULL DEFAULT 0 COMMENT '订单失效 (单位 秒)' AFTER `is_open`,
ADD COLUMN `auto_the_goods`  int(11) NULL DEFAULT 0 COMMENT '自动收货时间' AFTER `order_failure`,
ADD COLUMN `deliver_remind`  int(11) NULL DEFAULT 0 COMMENT '发货提醒设置' AFTER `auto_the_goods`;
