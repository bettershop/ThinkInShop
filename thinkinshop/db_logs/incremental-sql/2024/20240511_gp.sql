ALTER TABLE `lkt_seconds_activity`
    MODIFY COLUMN `starttime` timestamp NOT NULL COMMENT '活动开始时间' AFTER `type`,
    MODIFY COLUMN `endtime` timestamp NOT NULL COMMENT '活动结束时间' AFTER `starttime`,
    MODIFY COLUMN `create_date` timestamp NULL DEFAULT NULL COMMENT '创建时间' AFTER `is_delete`,
    MODIFY COLUMN `update_date` timestamp NULL DEFAULT NULL COMMENT '修改时间' AFTER `create_date`;

ALTER TABLE `lkt_seconds_label`
    MODIFY COLUMN `add_date` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '添加时间' AFTER `sort`,
    MODIFY COLUMN `update_date` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP AFTER `add_date`;


ALTER TABLE `lkt_seconds_record`
    MODIFY COLUMN `add_time` timestamp NOT NULL COMMENT '添加日期' AFTER `is_delete`;

ALTER TABLE `lkt_seconds_remind`
    MODIFY COLUMN `add_time` timestamp NOT NULL COMMENT '添加时间' AFTER `pro_id`;

ALTER TABLE `lkt_seconds_time`
MODIFY COLUMN `starttime` timestamp NOT NULL COMMENT '开始时间' AFTER `name`,
MODIFY COLUMN `endtime` timestamp NOT NULL COMMENT '结束时间' AFTER `starttime`;

ALTER TABLE `lkt_seconds_pro`
    MODIFY COLUMN `add_time` timestamp NOT NULL COMMENT '添加日期' AFTER `max_num`;

ALTER TABLE `lkt_seconds_day_delete`
    MODIFY COLUMN `add_time` timestamp NOT NULL COMMENT '删除时间' AFTER `day`;

ALTER TABLE `lkt_order_details`
    MODIFY COLUMN `re_time` timestamp NULL DEFAULT NULL COMMENT '申请退款时间' AFTER `real_money`;

ALTER TABLE `lkt_order_details`
    MODIFY COLUMN `audit_time` timestamp NULL DEFAULT NULL COMMENT '审核时间' AFTER `re_photo`;