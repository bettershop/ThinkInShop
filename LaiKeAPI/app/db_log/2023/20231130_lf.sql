ALTER TABLE `lkt_order_details` ADD COLUMN `is_addp` tinyint(1) DEFAULT '0' COMMENT '是否加购商品';

CREATE TABLE `lkt_flashsale_activity` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id自增',
  `store_id` int(11) NOT NULL COMMENT '商城id',
  `goodsId` int(11) NOT NULL COMMENT '商品id',
  `label_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '活动id',
  `flashsale_price` decimal(11,2) NOT NULL COMMENT '折扣价格',
  `price_type` int(11) NOT NULL DEFAULT '0' COMMENT '价格单位 0=百分比 1=固定值',
  `discount` decimal(10,2) DEFAULT '0.00' COMMENT '折扣',
  `buylimit` int(11) DEFAULT NULL COMMENT '购买上限',
  `num` int(11) NOT NULL COMMENT '活动库存',
  `max_num` int(11) DEFAULT NULL COMMENT '最大数量',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '活动名称',
  `status` int(1) NOT NULL DEFAULT '1' COMMENT '活动状态 1 未开始 2 进行中 3结束',
  `type` tinyint(1) DEFAULT NULL COMMENT '活动类型',
  `starttime` datetime NOT NULL COMMENT '活动开始时间',
  `endtime` datetime NOT NULL COMMENT '活动结束时间',
  `isshow` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否显示 1 是 0 否',
  `is_delete` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除 1 是 0 否',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `update_date` datetime DEFAULT NULL COMMENT '修改时间',
  `isNotice` int(11) NOT NULL DEFAULT '0' COMMENT '是否通知 0=未通知 1=已通知',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='限时折扣活动表';

CREATE TABLE `lkt_flashsale_addgoods` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `store_id` int(11) NOT NULL DEFAULT '0',
  `goods_id` int(11) NOT NULL COMMENT '商品ID',
  `attr_id` int(11) DEFAULT NULL COMMENT '属性id',
  `goods_price` decimal(11,2) NOT NULL DEFAULT '0.00' COMMENT '商品价格',
  `price` decimal(11,2) NOT NULL DEFAULT '0.00' COMMENT '加购价格',
  `num` int(11) NOT NULL DEFAULT '0' COMMENT '库存数量',
  `max_num` int(11) NOT NULL DEFAULT '0' COMMENT '上架库存',
  `sort` int(11) NOT NULL DEFAULT '1' COMMENT '排序',
  `is_delete` smallint(6) NOT NULL DEFAULT '0' COMMENT '是否删除:0否   1是',
  `add_time` datetime DEFAULT NULL COMMENT '添加时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `status` int(11) NOT NULL DEFAULT '1' COMMENT '上架状态 1待上架， 2已上架 ， 3已下架',
  `mch_id` int(11) NOT NULL DEFAULT '0' COMMENT '店铺ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='限时折扣加购商品表';

CREATE TABLE `lkt_flashsale_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键自增',
  `store_id` int(11) NOT NULL COMMENT '商城id',
  `is_open` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否开启 1 是 0 否',
  `buy_num` int(11) NOT NULL COMMENT '活动商品默认限购数量',
  `imageurl` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '轮播图',
  `is_sales` int(11) NOT NULL DEFAULT '0' COMMENT '是否开启规划',
  `remind` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '限时活动提醒 （单位：秒）',
  `rule` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规则',
  `order_failure` int(11) NOT NULL DEFAULT '0' COMMENT '订单失效 (单位 秒)',
  `order_after` int(11) NOT NULL DEFAULT '604800' COMMENT '订单售后时间 (单位秒)',
  `auto_the_goods` int(11) NOT NULL DEFAULT '7' COMMENT '自动收货时间',
  `deliver_remind` int(11) NOT NULL DEFAULT '3600' COMMENT '提醒发货限制 间隔(单位 秒)',
  `package_settings` tinyint(4) NOT NULL DEFAULT '0' COMMENT '多件包邮设置 0.未开启 1.开启',
  `same_piece` int(11) NOT NULL DEFAULT '0' COMMENT '同件',
  `same_order` int(11) NOT NULL DEFAULT '0' COMMENT '同单',
  `auto_good_comment_day` int(5) NOT NULL DEFAULT '0' COMMENT '自动评价设置几后自动好评',
  `auto_good_comment_content` varchar(255) DEFAULT NULL COMMENT '好评内容',
  `mch_id` int(11) NOT NULL COMMENT '店铺id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='限时折扣设置';

CREATE TABLE `lkt_flashsale_label` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id自增',
  `store_id` int(11) DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '活动名称',
  `status` int(1) DEFAULT '1' COMMENT '活动状态 1 未开始 2 进行中 3结束',
  `discount` decimal(10,2) DEFAULT NULL COMMENT '折扣',
  `buylimit` int(11) DEFAULT NULL COMMENT '购买上限',
  `is_show` int(11) NOT NULL DEFAULT '0',
  `starttime` datetime DEFAULT NULL COMMENT '活动开始时间',
  `endtime` datetime DEFAULT NULL COMMENT '活动结束时间',
  `sort` int(11) DEFAULT NULL COMMENT '序号',
  `content` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '活动说明',
  `add_date` datetime DEFAULT NULL COMMENT '添加时间',
  `update_date` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `recovery` int(11) DEFAULT '0' COMMENT '是否回收',
  `mch_id` int(11) NOT NULL COMMENT '店铺id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='限时折扣活动表';

CREATE TABLE `lkt_flashsale_pro` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id自增',
  `store_id` int(11) NOT NULL COMMENT '商城id',
  `activity_id` int(50) NOT NULL COMMENT '插件活动id',
  `goods_id` int(11) DEFAULT NULL COMMENT '商品ID',
  `attr_id` int(11) DEFAULT '0' COMMENT '规格id',
  `num` int(11) NOT NULL COMMENT '活动库存',
  `max_num` int(11) DEFAULT NULL COMMENT '最大数量',
  `add_time` datetime NOT NULL COMMENT '添加日期',
  `is_delete` int(1) NOT NULL DEFAULT '0' COMMENT '是否删除 1 是 0 否',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='限时折扣商品表';

CREATE TABLE `lkt_flashsale_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id自增',
  `store_id` int(11) NOT NULL COMMENT '店铺id',
  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户id',
  `activity_id` int(11) NOT NULL COMMENT '活动id',
  `time_id` int(11) NOT NULL COMMENT '时段id',
  `pro_id` int(11) NOT NULL COMMENT '商品id',
  `attr_id` int(11) NOT NULL COMMENT '规格id',
  `sec_id` int(11) NOT NULL COMMENT '限时折扣id',
  `price` decimal(11,2) NOT NULL COMMENT '价格',
  `num` int(11) NOT NULL COMMENT '数量',
  `sNo` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '限时折扣订单',
  `is_delete` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除 1是 0否',
  `add_time` datetime NOT NULL COMMENT '添加日期',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='限时折扣记录表';











