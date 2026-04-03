ALTER TABLE `lkt_auction_config`
ADD COLUMN `agree_content`  text NULL COMMENT '竞拍协议' AFTER `deliver_remind`,
ADD COLUMN `agree_title`  varchar(255) NULL COMMENT '协议标题' AFTER `agree_content`;
ALTER TABLE `lkt_auction_special`
MODIFY COLUMN `id`  int(11) NOT NULL AUTO_INCREMENT COMMENT 'id' FIRST ;
ALTER TABLE `lkt_auction_session`
MODIFY COLUMN `id`  int(11) NOT NULL AUTO_INCREMENT COMMENT 'id' FIRST ,
MODIFY COLUMN `special_id`  int(11) NULL DEFAULT 0 COMMENT '专场id' AFTER `id`;
ALTER TABLE `lkt_auction_product`
MODIFY COLUMN `session_id`  int(11) NULL DEFAULT 0 COMMENT '场次id' AFTER `id`;
ALTER TABLE `lkt_auction_promise`
MODIFY COLUMN `special_id`  int(11) NULL DEFAULT 0 COMMENT '专场id' AFTER `add_time`;
ALTER TABLE `lkt_auction_remind`
MODIFY COLUMN `id`  int(11) NOT NULL AUTO_INCREMENT COMMENT 'id' FIRST ,
MODIFY COLUMN `special_id`  int(11) NULL DEFAULT NULL COMMENT '专场id' AFTER `id`;
ALTER TABLE `lkt_auction_collection`
MODIFY COLUMN `id`  int(11) NOT NULL AUTO_INCREMENT COMMENT 'id' FIRST ,
MODIFY COLUMN `session_id`  int(11) NULL DEFAULT NULL COMMENT '场次id' AFTER `id`;
ALTER TABLE `lkt_auction_promise`
MODIFY COLUMN `user_id`  varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户id' AFTER `store_id`;

ALTER TABLE `lkt_stock`
ADD COLUMN `mch_id`  int(11) NULL DEFAULT 0 COMMENT '店铺ID' AFTER `content`,
ADD COLUMN `is_acceptance`  tinyint(2) NULL DEFAULT 0 COMMENT '是否受理 0.否 1.是' AFTER `mch_id`;

