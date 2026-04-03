ALTER TABLE lkt_config ADD COLUMN track_secret VARCHAR (32) COMMENT '17track密钥' AFTER cloud_notify;

ALTER TABLE lkt_order_details ADD COLUMN logistics text COMMENT '17track物流信息' AFTER r_sNo;