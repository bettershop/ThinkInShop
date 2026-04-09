-- 优化 51494
--分商品数量优化补充

CREATE TABLE `lkt_express_delivery` (
                                        `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                        `store_id` int(11) DEFAULT '0' COMMENT '商城ID',
                                        `sNo` varchar(255) DEFAULT NULL COMMENT '订单号',
                                        `order_details_id` int(11) DEFAULT '0' COMMENT '订单详情ID',
                                        `express_id` int(11) DEFAULT '0' COMMENT '快递公司ID',
                                        `courier_num` varchar(255) NOT NULL COMMENT '快递单号',
                                        `num` int(11) DEFAULT '0' COMMENT '商品数量',
                                        `deliver_time` datetime DEFAULT NULL COMMENT '发货时间',
                                        PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COMMENT='快递记录表';