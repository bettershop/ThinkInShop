ALTER TABLE lkt_config ADD COLUMN track_secret VARCHAR (32) COMMENT '17track密钥' AFTER cloud_notify;
ALTER TABLE lkt_express_delivery ADD COLUMN task_id VARCHAR(64) COMMENT '快递100 taskid' AFTER express_id;

ALTER TABLE `lkt_auction_special` 
ADD COLUMN `sort` int(11) NULL DEFAULT 0 COMMENT '排序' AFTER `img`;

ALTER TABLE `lkt_auction_product` 
ADD COLUMN `sort` int(11) NULL DEFAULT 0 COMMENT '排序' AFTER `attr_id`;

ALTER TABLE `lkt_auction_product` 
ADD COLUMN `passed_status` int(11) NOT NULL DEFAULT 1 COMMENT '流拍类型  1未付款流拍  2未达到人数流拍  ' AFTER `update_date`;