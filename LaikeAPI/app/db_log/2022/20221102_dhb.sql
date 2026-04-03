CREATE TABLE `lkt_members_config` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `store_id` int(11) NOT NULL DEFAULT '0' COMMENT '商城id',
  `is_open` int(11) NOT NULL DEFAULT '0' COMMENT '会员插件开关 0.关 1.开',
  `open_config` text NOT NULL COMMENT '开通设置',
  `birthday_open` int(10) NOT NULL DEFAULT '0' COMMENT '会员生日特权开关 0.关 1.开',
  `points_multiple` int(10) DEFAULT '1' COMMENT '积分倍数',
  `bonus_points_open` int(10) NOT NULL DEFAULT '0' COMMENT '会员赠送积分开关 0.关 1.开',
  `bonus_points_config` text COMMENT '会员赠送积分设置',
  `member_discount` decimal(10,2) NOT NULL COMMENT '会员打折率',
  `renew_open` int(10) NOT NULL DEFAULT '0' COMMENT '续费提醒开关 0.关 1.开',
  `renew_day` int(10) DEFAULT NULL COMMENT '开始续费提醒天数',
  `member_equity` text NOT NULL COMMENT '会员权益设置',
  `add_time` datetime NOT NULL COMMENT '添加时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB CHARSET=utf8 COMMENT='会员设置表';

ALTER TABLE `lkt_user`
ADD COLUMN `is_grade`  tinyint(4) NULL DEFAULT 0 COMMENT '是否会员 0.否 1.是' AFTER `last_time`;

CREATE TABLE `lkt_members_pro` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `store_id` int(11) NOT NULL DEFAULT '0' COMMENT '商城id',
  `pro_id` int(11) NOT NULL DEFAULT '0' COMMENT '商品id',
  `recovery` int(10) NOT NULL DEFAULT '0' COMMENT '是否回收 0.否 1是',
  `add_time` datetime  COMMENT '添加时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB CHARSET=utf8 COMMENT='会员商品表';