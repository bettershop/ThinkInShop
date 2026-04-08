ALTER TABLE lkt_integral_config ADD COLUMN score_ratio VARCHAR(16) COMMENT '积分支付比例' AFTER `STATUS`
ALTER TABLE lkt_order ADD COLUMN score_deduction int COMMENT '积分支付抵扣' AFTER `grade_rate`