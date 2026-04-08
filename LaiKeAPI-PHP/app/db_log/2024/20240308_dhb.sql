ALTER TABLE `lkt_order` 
ADD COLUMN `user_recycle` tinyint(4) NULL DEFAULT 1 COMMENT '用户删除 1.显示 2.删除' AFTER `VerifiedBy`,
ADD COLUMN `mch_recycle` tinyint(4) NULL DEFAULT 1 COMMENT '商户删除 1.显示 2.删除' AFTER `user_recycle`,
ADD COLUMN `store_recycle` tinyint(4) NULL DEFAULT 1 COMMENT '商城删除 1.显示 2.删除' AFTER `mch_recycle`;

ALTER TABLE `lkt_order_details` 
ADD COLUMN `third_party_id` varchar(255) NULL DEFAULT NULL COMMENT '第三方订单号' AFTER `deliver_num`,
ADD COLUMN `third_party_json` longtext NULL COMMENT '第三方订单信息' AFTER `third_party_id`;
