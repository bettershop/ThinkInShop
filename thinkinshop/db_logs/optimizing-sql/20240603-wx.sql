ALTER TABLE `lkt_guide_menu`
    ADD INDEX `menu_id_idx` (`menu_id`) USING BTREE,
    ADD INDEX `role_id_idx` (`role_id`) USING BTREE;

ALTER TABLE `lkt_core_menu`
    ADD INDEX `id` (`id`) USING BTREE,
    ADD INDEX `s_id` (`s_id`) USING BTREE,
    ADD INDEX `sort` (`sort`) USING BTREE,
    ADD INDEX `recycle` (`recycle`) USING BTREE;

ALTER TABLE `lkt_role_menu`
    ADD INDEX `role_id` (`role_id`) USING BTREE;

ALTER TABLE `lkt_role`
    ADD INDEX `status` (`status`) USING BTREE,
    ADD INDEX `id` (`id`) USING BTREE,
    ADD INDEX `store_id` (`store_id`) USING BTREE,
    ADD INDEX `admin_id`(`admin_id`) USING BTREE;


ALTER TABLE `lkt_menu`
    ADD INDEX `id` (`id`) USING BTREE,
    ADD INDEX `sid` (`sid`) USING BTREE,
    ADD INDEX `type` (`type`) USING BTREE;

ALTER TABLE `lkt_role_menu`
    ADD INDEX `is_display` (`is_display`) USING BTREE;

