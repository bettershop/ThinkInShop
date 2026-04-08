ALTER TABLE lkt_config ADD COLUMN is_open_cloud TINYINT(1) DEFAULT 0 COMMENT '是否开启快递100云打印 0：关闭 1：开启' AFTER express_secret;
ALTER TABLE lkt_config ADD COLUMN siid VARCHAR(64)  COMMENT '打印机设备码' AFTER is_open_cloud;
ALTER TABLE lkt_config ADD COLUMN cloud_notify VARCHAR(64) COMMENT '云打印回调地址' AFTER siid;

ALTER TABLE lkt_express_delivery ADD COLUMN task_id VARCHAR(64) COMMENT '快递100 taskid' AFTER express_id;
