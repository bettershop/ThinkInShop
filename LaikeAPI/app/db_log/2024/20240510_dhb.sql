
ALTER TABLE `lkt_user` 
MODIFY COLUMN `sex` int(11) NOT NULL DEFAULT 0 COMMENT '性别默认男 0:未知 1:男 2:女' AFTER `clientid`;

ALTER TABLE `lkt_online_message` 
MODIFY COLUMN `send_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '发送人id' AFTER `store_id`;