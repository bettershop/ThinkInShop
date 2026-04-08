--优化订单打印配置
CREATE TABLE `lkt_print_setup` (
                                   `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id自增',
                                   `store_id` int(11) NOT NULL COMMENT '商城id',
                                   `mch_id` int(11) DEFAULT NULL COMMENT '店铺id',
                                   `print_name` varchar(50) DEFAULT NULL COMMENT '打印名称',
                                   `print_url` varchar(100) DEFAULT NULL COMMENT '打印网址',
                                   `sheng` varchar(50) DEFAULT NULL COMMENT '省',
                                   `shi` varchar(50) DEFAULT NULL COMMENT '市',
                                   `xian` varchar(50) DEFAULT NULL COMMENT '县',
                                   `address` varchar(100) DEFAULT NULL COMMENT '详细地址',
                                   `phone` varchar(50) DEFAULT NULL COMMENT '联系电话',
                                   `add_time` datetime DEFAULT NULL COMMENT '添加时间',
                                   PRIMARY KEY (`id`),
                                   KEY `store_id` (`store_id`) USING BTREE,
                                   KEY `mch_id` (`mch_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='订单打印配置';