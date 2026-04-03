-- 订单详情表中的mch_id 字段更新值
UPDATE lkt_order_details as d
set d.mch_id = (SELECT p.mch_id from lkt_product_list as p where p.id = d.p_id)