

ALTER TABLE `lkt_auction_promise` 
ADD COLUMN `back_time` timestamp(0) NULL COMMENT '退款时间' AFTER `source`;