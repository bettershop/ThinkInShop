
ALTER TABLE `lkt_order` 
ADD COLUMN `cancel_method` tinyint(4) NULL DEFAULT 0 COMMENT '订单取消方式 0 定时任务取消订单  1 手动点击取消订单' AFTER `Dividend_status`;