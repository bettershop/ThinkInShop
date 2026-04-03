ALTER TABLE `lkt_message_logging` 
MODIFY COLUMN `type` tinyint(3) NOT NULL DEFAULT 1 COMMENT '消息类型 1.订单(待发货) 2.订单(售后) 3.订单(提醒发货) 4.订单(订单关闭) 5.订单(新订单) 6.订单(收货) 7.商品(审核) 8.商品(下架) 9.商品(补货) 10.商品(新商品上架) 11.商品(分类) 12.商品(品牌) 13.提现 14.违规下架 15.商品删除 16.保证金审核消息通知 17.店铺商品审核消息通知' AFTER `mch_id`;

ALTER TABLE `lkt_sign_record` 
MODIFY COLUMN `type` int(4) NOT NULL DEFAULT 0 COMMENT '类型: 0:签到 1:消费 2:首次关注得积分  4:转积分给好友 5:系统扣除 6:系统充值 7:抽奖 8:会员购物积分 9:分销升级奖励积分 10:积分过期 11.开通会员 12.会员生日特权 13.冻结积分 14.售后退回' AFTER `sign_time`;

ALTER TABLE `lkt_product_list` 
ADD COLUMN `mch_sort` int(11) NULL DEFAULT 1 COMMENT '店铺排序值' AFTER `sort`;