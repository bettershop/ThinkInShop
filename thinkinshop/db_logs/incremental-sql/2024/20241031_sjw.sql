CREATE TABLE `lkt_living_sensitive`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `word` mediumtext CHARACTER SET utf8 COLLATE utf8_bin NULL COMMENT '敏感词',
  `add_time` timestamp NULL DEFAULT NULL COMMENT '添加时间',
  `store_id` int(11) NULL DEFAULT NULL COMMENT '商城id',
  `recycle` int(11) NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`) USING BTREE
);
ALTER TABLE `lkt_auction_promise`
ADD COLUMN `back_time` timestamp NULL COMMENT '保证金退还时间或者是扣除时间' AFTER `source`;