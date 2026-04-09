-- 优化 51494
ALTER TABLE `lkt_order_details`
    ADD COLUMN `deliver_num` int(11) NULL COMMENT '发货数量' AFTER `store_recycle`;

ALTER TABLE `lkt_order_details`
    MODIFY COLUMN `deliver_num` int(11) NULL DEFAULT 0 COMMENT '发货数量' AFTER `store_recycle`;

UPDATE lkt_order_details
SET deliver_num = 0
where deliver_num is null;

ALTER TABLE `lkt_order_details`
    MODIFY COLUMN `express_id` varchar(255) NULL DEFAULT NULL COMMENT '快递公司id ,号分隔' AFTER `r_type`,
    MODIFY COLUMN `courier_num` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '快递单号 ,号分隔' AFTER `express_id`;