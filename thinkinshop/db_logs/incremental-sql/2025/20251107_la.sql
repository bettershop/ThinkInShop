ALTER TABLE lkt_auction_special ADD COLUMN sort INT COMMENT '排序' AFTER img;
ALTER TABLE lkt_auction_product ADD COLUMN sort INT COMMENT '排序' AFTER attr_id;