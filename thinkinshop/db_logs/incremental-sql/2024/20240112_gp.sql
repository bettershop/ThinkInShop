-- 优化 47152

CREATE TABLE `lkt_record_details` (
                                      `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
                                      `store_id` int(11) NOT NULL DEFAULT '0' COMMENT '商城id',
                                      `money` decimal(12,2) NOT NULL COMMENT '操作金额',
                                      `user_money` decimal(12,2) NOT NULL COMMENT '账户余额',
                                      `type` tinyint(4) NOT NULL COMMENT '账单类型 1 余额充值(用户)  2余额充值(平台)  3活动佣金  4订单退款 5店铺保证金提取 6 平台扣款  7 订单支付  8 参与活动  9充值会员  10 店铺保证金支付  11余额提现',
                                      `money_type` int(4) NOT NULL COMMENT '1 收入类型  2支出类型',
                                      `money_type_name` tinyint(4) DEFAULT NULL COMMENT '收入/支出 名称   1用户充值 2平台充值 3 拼团活动开团佣金 4普通订单 5竞拍活动 6店铺保证金 7余额扣款 8预售定金缴纳  9会员开通',
                                      `record_time` datetime DEFAULT NULL COMMENT '到账时间/提现时间/支出时间',
                                      `record_notes` varchar(255) DEFAULT NULL COMMENT '备注',
                                      `type_name` varchar(255) DEFAULT NULL COMMENT '充值方式/退款类型/活动类型',
                                      `s_no` varchar(255) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '订单号',
                                      `title_name` varchar(100) CHARACTER SET utf8mb4 DEFAULT '' COMMENT '商品名字/活动名称',
                                      `activity_code` varchar(255) DEFAULT NULL COMMENT '活动编号',
                                      `mch_name` varchar(255) DEFAULT NULL COMMENT '店铺名称',
                                      `withdrawal_fees` varchar(20) DEFAULT NULL COMMENT '提现手续费',
                                      `withdrawal_method` varchar(20) DEFAULT NULL COMMENT '提现方式',
                                      `add_time` datetime DEFAULT NULL COMMENT '添加时间',
                                      PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作记录详情';

ALTER TABLE `lkt_record`
    ADD COLUMN `details_id` int(11) NULL DEFAULT NULL COMMENT '记录详情id' AFTER `main_id`;


ALTER TABLE `lkt_record_details`
    MODIFY COLUMN `money_type_name` tinyint(4) NULL DEFAULT NULL COMMENT '收入/支出 名称   1用户充值 2平台充值 3 拼团活动开团佣金 4普通订单 5竞拍活动 6店铺保证金 7余额扣款 8预售定金缴纳  9会员开通 10 普通订单(代客下单) 11拼团订单 12竟拍订单 13预售订单 14分稍订单  15秒杀订单 16积分兑换订单 17限时折扣订单' AFTER `money_type`;