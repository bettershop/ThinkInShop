ALTER TABLE `lkt_pro_label`
MODIFY COLUMN `color`  varchar(255) NULL DEFAULT '' COMMENT '颜色' AFTER `add_time`;

ALTER TABLE `lkt_product_list`
ADD COLUMN `zixuan_id`  int(11) NULL DEFAULT 0 COMMENT '自选id' AFTER `receiving_form`,
ADD COLUMN `video`  varchar(255) NULL COMMENT '视频文件' AFTER `zixuan_id`,
ADD COLUMN `pro_video`  varchar(255) NULL COMMENT '商品视频' AFTER `video`,
ADD COLUMN `real_volume`  int(11) NULL DEFAULT 0 COMMENT '真实销量' AFTER `pro_video`;

