
ALTER TABLE `lkt_user` 
MODIFY COLUMN `money` decimal(12, 2) NOT NULL COMMENT '金额' AFTER `detailed_address`;


ALTER TABLE `lkt_files_record` 
ADD COLUMN `type` tinyint(4) NULL DEFAULT 1 COMMENT '资源类型，1图片，2视频' AFTER `supplier_id`,
ADD COLUMN `name` varchar(255) NULL COMMENT '用户自定义的名称' AFTER `type`,
ADD COLUMN `add_user` varchar(255) NULL COMMENT '上传人' AFTER `name`;

ALTER TABLE `lkt_img_group` 
ADD COLUMN `supplier_id` int(11) NULL COMMENT '供应商id' AFTER `is_default`,
ADD COLUMN `mch_id` int(11) NULL COMMENT '店铺ID' AFTER `supplier_id`,
ADD COLUMN `type` tinyint(4) NULL DEFAULT 1 COMMENT '资源类型，1图片，2视频' AFTER `mch_id`,
ADD COLUMN `is_show` tinyint(4) NULL DEFAULT 1 COMMENT '该分类是否常驻显示，1为不常驻显示，2为常驻显示' AFTER `type`;

ALTER TABLE `lkt_img_group` 
MODIFY COLUMN `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分组名称' AFTER `store_id`;