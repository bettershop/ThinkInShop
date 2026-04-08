

CREATE TABLE `lkt_living_sensitive`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `word` mediumtext CHARACTER SET utf8 COLLATE utf8_bin NULL COMMENT '敏感词',
  `add_time` timestamp NULL DEFAULT NULL COMMENT '添加时间',
  `store_id` int(11) NULL DEFAULT NULL COMMENT '商城id',
  `recycle` int(11) NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`) USING BTREE
);