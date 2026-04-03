ALTER TABLE `lkt_seconds_config` ADD COLUMN `auto_good_comment_content` VARCHAR(255) NULL COMMENT '好评内容' AFTER `auto_good_comment_day`;
ALTER TABLE `lkt_integral_config` ADD COLUMN `auto_good_comment_content` VARCHAR(255) NULL COMMENT '好评内容' AFTER `auto_good_comment_day`;
ALTER TABLE `lkt_pre_sell_config` ADD COLUMN `auto_good_comment_content` VARCHAR(255) NULL COMMENT '好评内容' AFTER `auto_good_comment_day`;
ALTER TABLE `lkt_seconds_config` MODIFY COLUMN `remind` VARCHAR(100) NOT NULL COMMENT '秒杀活动提醒 （单位：秒）';