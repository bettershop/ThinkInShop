CREATE TABLE `lkt_drafts`  (
                               `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
                               `mch_id` int(11) NULL DEFAULT NULL COMMENT '店铺ID',
                               `store_id` int(11) NULL COMMENT '商城id',
                               `supplier_id` int(11) NULL DEFAULT NULL COMMENT '供应商ID',
                               `text` text COMMENT '草稿数据',
                               `add_time` timestamp NULL DEFAULT NULL COMMENT '添加时间',
                               PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='草稿箱';