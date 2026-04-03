ALTER TABLE `lkt_session_id`
MODIFY COLUMN `session_id`  varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'session_id' AFTER `id`,
MODIFY COLUMN `content`  mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '内容' AFTER `session_id`;

ALTER TABLE `lkt_coupon_activity`
ADD COLUMN `consumption_threshold_type`  tinyint(4) NULL DEFAULT 1 COMMENT '消费门口  1.无门槛  2.有门槛' AFTER `discount`;
