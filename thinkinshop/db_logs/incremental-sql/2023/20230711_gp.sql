ALTER TABLE `lkt_comments`
    ADD COLUMN `is_look` int(11) NOT NULL DEFAULT 0 COMMENT '商家是否以查看评论 0未查看 1已查看' AFTER `order_detail_id`;

ALTER TABLE lkt_group_activity`
    ADD COLUMN `is_custom` int(11) NOT NULL COMMENT '时间是否自定义' AFTER `end_date`;

ALTER TABLE `lkt_group_activity`
MODIFY COLUMN `is_custom` int(11) NOT NULL DEFAULT 0 COMMENT '时间是否自定义 0不是 1是' AFTER `end_date`;


ALTER TABLE `lkt_config`
MODIFY COLUMN `upserver` tinyint(4) NOT NULL DEFAULT 2 COMMENT '上传服务器:1,本地　2,阿里云 3,腾讯云 4,七牛云 5,minio' AFTER `uploadImg_domain`;