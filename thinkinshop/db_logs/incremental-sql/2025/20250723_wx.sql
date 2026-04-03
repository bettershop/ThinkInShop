DELIMITER
//

-- lkt_brand_class插入触发器
CREATE TRIGGER before_insert_lkt_brand_class
    BEFORE INSERT
    ON lkt_brand_class
    FOR EACH ROW
BEGIN
    IF NEW.notset = 1 THEN
        IF EXISTS (
            SELECT 1 FROM lkt_brand_class
            WHERE store_id = NEW.store_id
              AND lang_code = NEW.lang_code
              AND notset = 1
        ) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '每个 `store_id` 和 `lang_code` 组合只允许有一条 `notset = 1` 的记录';
END IF;
END IF;
END;
//


-- lkt_product_class插入前
CREATE TRIGGER before_insert_lkt_product_class
    BEFORE INSERT
    ON lkt_product_class
    FOR EACH ROW
BEGIN
    IF NEW.notset = 1 THEN
        IF EXISTS (
            SELECT 1 FROM lkt_product_class
            WHERE store_id = NEW.store_id
              AND lang_code = NEW.lang_code
              AND notset = 1
        ) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '每个 store_id 和 lang_code 只允许有一条 notset = 1 的记录';
END IF;
END IF;
END;
//

DELIMITER ;