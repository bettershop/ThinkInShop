
ALTER TABLE `lkt_order_data` 
ADD COLUMN `source` int(11) NOT NULL DEFAULT 1 COMMENT '来源' AFTER `pay_type`;

ALTER TABLE `lkt_record` 
ADD COLUMN `details_id` int(11) NOT NULL DEFAULT 0 COMMENT '记录详情id' AFTER `main_id`;