

ALTER TABLE `lkt_integral_config` 
MODIFY COLUMN `auto_good_comment_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '自动评价内容' AFTER `auto_good_comment_day`;