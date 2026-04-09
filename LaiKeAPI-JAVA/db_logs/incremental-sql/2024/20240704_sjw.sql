ALTER TABLE `lkt_order`
    MODIFY COLUMN `self_lifting` tinyint(4) NULL DEFAULT 0 COMMENT '自提 0.配送 1.自提 3.虚拟订单需要核销 4.虚拟订单无需核销' AFTER `remarks`;
ALTER TABLE `lkt_record_details`
    MODIFY COLUMN `money_type_name` tinyint(4) NULL DEFAULT NULL COMMENT '收入/支出 名称   1用户充值 2平台充值 3 拼团活动开团佣金 4普通订单 5竞拍活动 6店铺保证金 7余额扣款 8预售定金缴纳  9会员开通 10 普通订单(代客下单) 11拼团订单 12竟拍订单 13预售订单 14分稍订单  15秒杀订单 16积分兑换订单 17限时折扣订单 18竞拍保证金缴纳 19虚拟订单' AFTER `money_type`;
ALTER TABLE `lkt_order_details`
    ADD COLUMN `write_time_id` int(10) NULL COMMENT '预约时段id' AFTER `write_time`;