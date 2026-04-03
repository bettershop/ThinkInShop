-- 为订单表新增 stripe 会话ID和支付意图ID（放表末尾）
ALTER TABLE `lkt_order`
    ADD COLUMN `stripe_id`             VARCHAR(255) NULL COMMENT 'stripe支付会话ID（用于前端调起支付）',
    ADD COLUMN `stripe_payment_intent` VARCHAR(255) NULL COMMENT 'stripe支付意图ID（用于退款、查询订单状态）';

-- 为店铺保证金表新增 stripe 会话ID和支付意图ID（放表末尾）
ALTER TABLE `lkt_mch_promise`
    ADD COLUMN `stripe_id`             VARCHAR(255) NULL COMMENT 'stripe支付会话ID（用于前端调起支付）',
    ADD COLUMN `stripe_payment_intent` VARCHAR(255) NULL COMMENT 'stripe支付意图ID（用于退款、查询订单状态）';

-- 为预售记录表新增 stripe 会话ID和支付意图ID（放表末尾）
ALTER TABLE `lkt_pre_sell_record`
    ADD COLUMN `stripe_id`             VARCHAR(255) NULL COMMENT 'stripe支付会话ID（用于前端调起支付）',
    ADD COLUMN `stripe_payment_intent` VARCHAR(255) NULL COMMENT 'stripe支付意图ID（用于退款、查询订单状态）';

-- 添加 stripe_email 字段（带注释：Stripe 连接子账户邮箱）
ALTER TABLE lkt_user
    ADD COLUMN stripe_email VARCHAR(255) DEFAULT NULL COMMENT 'Stripe 连接子账户邮箱';

-- 添加 account_id 字段（带注释：Stripe 连接子账户 ID）
ALTER TABLE lkt_user
    ADD COLUMN stripe_account_id VARCHAR(255) DEFAULT NULL COMMENT 'Stripe 连接子账户 ID';

-- 为提现记录表新增 account_id 字段（带注释：Stripe 连接子账户 ID）
ALTER TABLE lkt_withdraw
    ADD COLUMN stripe_account_id VARCHAR(255) DEFAULT NULL COMMENT 'Stripe 连接子账户 ID';


