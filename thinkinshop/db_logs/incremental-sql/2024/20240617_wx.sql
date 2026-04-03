ALTER TABLE lkt_sign_record
    MODIFY COLUMN `score_invalid` timestamp NULL DEFAULT NULL COMMENT '积分过期时间  永不过期则为null' AFTER `record`,
    MODIFY COLUMN `frozen_time` timestamp NULL DEFAULT NULL COMMENT '冻结结束时间' AFTER `sNo`;