ALTER TABLE lkt_withdraw ADD COLUMN email VARCHAR(100) COMMENT '贝宝提现邮箱';

ALTER TABLE lkt_distribution_withdraw ADD COLUMN email VARCHAR(100) COMMENT '贝宝提现邮箱';

ALTER TABLE lkt_supplier_withdraw ADD COLUMN email VARCHAR(100) COMMENT '贝宝提现邮箱';

ALTER TABLE lkt_supplier_withdraw ADD COLUMN withdraw_status INT(4) COMMENT '提现类型 1:银行卡 2:微信余额 3:贝宝余额';
