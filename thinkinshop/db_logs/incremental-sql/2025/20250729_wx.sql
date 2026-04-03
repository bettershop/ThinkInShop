ALTER TABLE lkt_user
    ADD INDEX idx_store_access(store_id, access_id);