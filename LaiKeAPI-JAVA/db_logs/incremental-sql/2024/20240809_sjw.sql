ALTER TABLE `lkt_mch_store_write`
    MODIFY COLUMN `off_num` text CHARACTER SET utf8 COLLATE utf8_bin NULL COMMENT '已预约核销次数' AFTER `write_off_num`;