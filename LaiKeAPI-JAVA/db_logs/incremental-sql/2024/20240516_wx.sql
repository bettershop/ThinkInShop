ALTER TABLE lkt_group_activity
    MODIFY COLUMN `start_date` timestamp NOT NULL COMMENT '开始时间' AFTER `status`,
    MODIFY COLUMN `end_date` timestamp NOT NULL COMMENT '结束时间' AFTER `start_date`,
    MODIFY COLUMN `add_date` timestamp NOT NULL COMMENT '添加时间' AFTER `is_show`;


ALTER TABLE lkt_group_open_record
    MODIFY COLUMN `add_date` timestamp NOT NULL COMMENT '添加时间' AFTER `sno`;

ALTER TABLE lkt_distribution_grade
    MODIFY COLUMN `datetime` timestamp NOT NULL COMMENT '创建时间' AFTER `sets`;