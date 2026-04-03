CREATE TABLE `lkt_promise_record` (
                                        `id` int(11) NOT NULL AUTO_INCREMENT,
                                        `store_id` int(11) NOT NULL COMMENT '商城ID',
                                        `mch_id` int(11) NOT NULL COMMENT '店铺ID',
                                        `money` decimal(10,2) NOT NULL COMMENT '保证金金额',
                                        `type` int(11) NOT NULL COMMENT '记录类型 1:缴纳，2:退还',
                                        `status` varchar(255) NOT NULL COMMENT '审核状态 0=审核中 1=通过 2=拒绝',
                                        `add_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '添加时间',
                                        `remarks` varchar(255) COMMENT '备注',
                                        `promise_sh_id` VARBINARY(255) COMMENT '保证金审核id',
                                        PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='保证金审核记录表';