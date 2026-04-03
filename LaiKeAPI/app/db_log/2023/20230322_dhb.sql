ALTER TABLE `lkt_message_config`
MODIFY COLUMN `SignName`  varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '短信签名' AFTER `accessKeySecret`;

ALTER TABLE `lkt_product_list`
ADD COLUMN `examine_time`  datetime NULL DEFAULT NULL COMMENT '审核时间' AFTER `pro_introduce`,
ADD COLUMN `violation_time`  datetime NULL DEFAULT NULL COMMENT '违规下架时间' AFTER `examine_time`,
ADD COLUMN `receiving_form`  varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '1' COMMENT '收货形式 1.邮寄 2.自提' AFTER `violation_time`;

ALTER TABLE `lkt_mch`
MODIFY COLUMN `head_img`  varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '店铺头像' AFTER `poster_img`;

ALTER TABLE `lkt_customer`
MODIFY COLUMN `company`  text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公司名称' AFTER `price`;
ALTER TABLE `lkt_customer`
MODIFY COLUMN `name`  text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名' AFTER `customer_number`;

ALTER TABLE `lkt_order`
ADD COLUMN `is_lssued`  int(2) NULL DEFAULT 0 COMMENT '店铺是否操作代发 0.否 1.是' AFTER `operation_type`;

ALTER TABLE `lkt_order_details`
ADD COLUMN `supplier_settlement`  decimal(12,2) NULL DEFAULT NULL COMMENT '总供货价' AFTER `recycle`;


