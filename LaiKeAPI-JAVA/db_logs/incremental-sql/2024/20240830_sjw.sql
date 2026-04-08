ALTER TABLE `lkt_files_record`
    ADD COLUMN `type` int(11) NOT NULL DEFAULT 1 COMMENT '资源类型，1图片，2视频' AFTER `supplier_id`;
ALTER TABLE `lkt_flashsale_label`
    ADD COLUMN `update_source` int(11) NOT NULL DEFAULT 1 COMMENT '最后的更新来源 1为h5商家 2为pc店铺' AFTER `mch_id`;
ALTER TABLE `lkt_img_group`
    ADD COLUMN `supplier_id` int(10) NULL DEFAULT NULL COMMENT '供应商id' AFTER `is_default`,
ADD COLUMN `mch_id` int(11) NULL DEFAULT NULL COMMENT '店铺ID' AFTER `supplier_id`;
ALTER TABLE `lkt_files_record`
    ADD COLUMN `name` varchar(255) NULL COMMENT '用户自定义的名称' AFTER `type`;
ALTER TABLE `lkt_img_group`
    ADD COLUMN `type` int(10) NULL DEFAULT 1 COMMENT '分类类型，图片1  视频2' AFTER `mch_id`;