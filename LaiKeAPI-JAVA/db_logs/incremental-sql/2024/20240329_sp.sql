ALTER TABLE `lkt_auction_product`
    ADD COLUMN `passed_status` int(2) NULL DEFAULT 1 COMMENT '流拍类型  1未付款流拍  2未达到人数流拍  ' AFTER `update_date`;