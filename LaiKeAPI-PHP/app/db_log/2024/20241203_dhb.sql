
ALTER TABLE `lkt_return_order` 
MODIFY COLUMN `r_type` tinyint(1) NOT NULL DEFAULT 100 COMMENT '类型 100:不在退货退款状态0:审核中 1:同意并让用户寄回 2:拒绝(退货退款) 3:用户已快递 4:收到寄回商品,同意并退款 5:拒绝并退回商品 8:拒绝(退款) 9:同意并退款 10:拒绝(售后) 11:同意并且寄回商品 12:售后结束 13:人工售後完成 15:极速退款 16 收到回寄商品,确定不影响二次销售' AFTER `r_content`;