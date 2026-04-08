-- 预售订单支付方式
alter table lkt_pre_sell_record
    add pay varchar(15) null COMMENT '支付方式';

ALTER TABLE lkt_pre_sell_record
    CHANGE pay_type is_deposit int(10) COMMENT '支付类型 0.定金 1.尾款';

ALTER TABLE lkt_pre_sell_record
    CHANGE is_pay is_balance int(10) default 0 COMMENT '是否支付 0.未支付 1.已支付';