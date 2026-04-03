CREATE TABLE `lkt_auction_special` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `store_id` int(11) NOT NULL COMMENT '商城id',
  `mch_id` int(11) DEFAULT '0' COMMENT '店铺id',
  `name` varchar(255) NOT NULL COMMENT '专场名称',
  `img` varchar(255) NOT NULL COMMENT '专场图片',
  `commission` decimal(10,2) DEFAULT NULL COMMENT '佣金',
  `promise_amt` decimal(12,2) DEFAULT '0.00' COMMENT '保证金金额',
  `look_count` int(11) DEFAULT '0' COMMENT '围观人数',
  `start_date` datetime NOT NULL COMMENT '开始时间',
  `end_date` datetime NOT NULL COMMENT '结束时间',
  `sign_end_date` datetime DEFAULT NULL COMMENT '报名截至时间',
  `is_show` int(11) NOT NULL DEFAULT '1' COMMENT '是否显示',
  `status` int(11) DEFAULT '1' COMMENT '1=未开始 2=进行中 3=已结束',
  `type` int(11) DEFAULT '1' COMMENT '专场类型 1=店铺专场 2=普通专场 3=报名专场',
  `unit` int(11) DEFAULT '1' COMMENT '起拍价默认单位 1=固定金额 2=价格百分比',
  `number` decimal(12,2) NOT NULL COMMENT '起拍价默认值',
  `mark_up_amt` decimal(12,2) NOT NULL COMMENT '默认加价金额',
  `content` mediumtext NOT NULL COMMENT '专场预告',
  `is_settlement` int(11) DEFAULT '0' COMMENT '是否结算 0=否 1=是',
  `recovery` int(11) DEFAULT '0' COMMENT '回收标识',
  `add_date` datetime DEFAULT NULL COMMENT '添加时间',
  `update_date` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB CHARSET=utf8 COMMENT='专场表';

CREATE TABLE `lkt_auction_session` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `special_id` varchar(50) DEFAULT NULL COMMENT '专场id',
  `name` varchar(255) NOT NULL COMMENT '场次名称',
  `start_date` datetime NOT NULL COMMENT '开始时间',
  `end_date` datetime NOT NULL COMMENT '结束时间',
  `status` int(11) DEFAULT '1' COMMENT '1=未开始 2=进行中 3=已结束',
  `is_show` int(11) DEFAULT '1' COMMENT '是否显示',
  `recovery` int(11) DEFAULT '0' COMMENT '回收标识',
  `add_date` datetime DEFAULT NULL COMMENT '添加时间',
  `update_date` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB CHARSET=utf8mb4 COMMENT='竞拍场次表';

CREATE TABLE `lkt_auction_product` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `session_id` varchar(50) DEFAULT NULL COMMENT '场次id',
  `goods_id` varchar(50) DEFAULT NULL COMMENT '商品id',
  `attr_id` int(11) DEFAULT '0' COMMENT '商品规格id',
  `starting_amt` decimal(12,2) DEFAULT '0.00' COMMENT '起拍价',
  `mark_up_amt` decimal(12,2) DEFAULT '0.00' COMMENT '加价幅度',
  `goods_price` decimal(12,2) DEFAULT '0.00' COMMENT '商品价格',
  `price` decimal(12,2) DEFAULT '0.00' COMMENT '当前出价',
  `user_id` varchar(50) DEFAULT NULL COMMENT '最终得主',
  `status` int(11) DEFAULT '0' COMMENT '0=待拍卖 1=拍卖中 2=已拍卖 3=已流拍',
  `sNo` varchar(50) DEFAULT NULL COMMENT '关联订单号',
  `is_show` int(11) DEFAULT '0' COMMENT '是否显示',
  `recovery` int(11) DEFAULT '0' COMMENT '回收标识',
  `add_date` datetime DEFAULT NULL COMMENT '添加时间',
  `update_date` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB CHARSET=utf8mb4 COMMENT='竞拍商品表';

CREATE TABLE `lkt_auction_promise` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `store_id` int(11) DEFAULT NULL COMMENT '商城id',
  `user_id` varchar(50) DEFAULT NULL COMMENT '用户id',
  `promise` decimal(12,2) DEFAULT NULL COMMENT '保证金额',
  `add_time` timestamp NULL DEFAULT NULL COMMENT '添加时间',
  `special_id` varchar(50) DEFAULT NULL COMMENT '专场id',
  `trade_no` varchar(50) DEFAULT NULL COMMENT '订单编号',
  `is_pay` int(2) DEFAULT NULL COMMENT '是否成功支付 0失败,1成功',
  `is_back` int(2) DEFAULT NULL COMMENT '是否退款成功  0-成功 1-失败',
  `is_deduction` int(2) DEFAULT '0' COMMENT '是否扣除 0-未扣除,1-已扣除',
  `allow_back` tinyint(2) DEFAULT '1' COMMENT '是否符合退款标准  0：不符合 1：符合',
  `recovery` int(11) DEFAULT '0' COMMENT '回收标识',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB CHARSET=utf8mb4 COMMENT='竞拍保证金表';

CREATE TABLE `lkt_auction_collection` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `session_id` varchar(50) DEFAULT NULL COMMENT '场次id',
  `user_id` varchar(50) NOT NULL COMMENT '收藏人',
  `add_date` datetime DEFAULT NULL COMMENT '添加时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB CHARSET=utf8mb4 COMMENT='竞拍收藏表';

CREATE TABLE `lkt_auction_remind` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `special_id` varchar(50) DEFAULT NULL COMMENT '专场id',
  `user_id` varchar(50) NOT NULL COMMENT '被提醒人',
  `is_remind` int(11) NOT NULL DEFAULT '0' COMMENT '是否提醒过',
  `add_date` datetime DEFAULT NULL COMMENT '添加时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB CHARSET=utf8mb4 COMMENT='竞拍专场提醒表';


ALTER TABLE `lkt_user_collection`
MODIFY COLUMN `type`  int(11) NULL DEFAULT 1 COMMENT '收藏类型 1.普通收藏 2.积分商城收藏 3.竞拍商品收藏' AFTER `mch_id`;

ALTER TABLE `lkt_order_data`
ADD COLUMN `order_type`  varchar(255) NULL COMMENT '订单类型' AFTER `trade_no`,
ADD COLUMN `pay_type`  varchar(255) NULL DEFAULT 'wallet_pay' COMMENT '支付方式' AFTER `status`;

