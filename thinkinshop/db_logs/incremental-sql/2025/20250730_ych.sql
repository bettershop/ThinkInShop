-- 为lkt_order_details表添加允许为NULL且默认值为0的产品积分字段p_integral
ALTER TABLE lkt_order_details
    ADD COLUMN p_integral INT DEFAULT 0 COMMENT '产品积分（单个产品对应的积分，允许为NULL）';
