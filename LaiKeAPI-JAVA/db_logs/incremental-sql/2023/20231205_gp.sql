-- 优化49609
ALTER TABLE `lkt_order`
ADD COLUMN `user_recycle` int(4) NULL COMMENT '用户删除 1.显示 2.删除' AFTER `is_lssued`,
ADD COLUMN `mch_recycle` int(4) NULL COMMENT '商户删除 1.显示 2.删除' AFTER `user_recycle`,
ADD COLUMN `store_recycle` int(4) NULL COMMENT '商城删除 1.显示 2.删除' AFTER `mch_recycle`;


ALTER TABLE `lkt_order_details`
ADD COLUMN `user_recycle` int(4) NULL DEFAULT NULL COMMENT '用户删除 1.显示 2.删除' AFTER `supplier_settlement`,
ADD COLUMN `mch_recycle` int(4) NULL DEFAULT NULL COMMENT '商户删除 1.显示 2.删除' AFTER `user_recycle`,
ADD COLUMN `store_recycle` int(4) NULL DEFAULT NULL COMMENT '商城删除 1.显示 2.删除' AFTER `mch_recycle`;

ALTER TABLE `lkt_order`
    MODIFY COLUMN `user_recycle` int(4) NULL DEFAULT 1 COMMENT '用户删除 1.显示 2.删除' AFTER `is_lssued`,
    MODIFY COLUMN `mch_recycle` int(4) NULL DEFAULT 1 COMMENT '商户删除 1.显示 2.删除' AFTER `user_recycle`,
    MODIFY COLUMN `store_recycle` int(4) NULL DEFAULT 1 COMMENT '商城删除 1.显示 2.删除' AFTER `mch_recycle`;

ALTER TABLE `lkt_order_details`
    MODIFY COLUMN `user_recycle` int(4) NULL DEFAULT 1 COMMENT '用户删除 1.显示 2.删除' AFTER `supplier_settlement`,
    MODIFY COLUMN `mch_recycle` int(4) NULL DEFAULT 1 COMMENT '商户删除 1.显示 2.删除' AFTER `user_recycle`,
    MODIFY COLUMN `store_recycle` int(4) NULL DEFAULT 1 COMMENT '商城删除 1.显示 2.删除' AFTER `mch_recycle`;