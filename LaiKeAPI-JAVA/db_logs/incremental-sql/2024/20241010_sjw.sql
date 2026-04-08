ALTER TABLE `lkt_product_list`
ADD COLUMN `supplier_status` int(11) NULL COMMENT '供应商商品状态 用于在供应商端展示 0断供 1上架' ;