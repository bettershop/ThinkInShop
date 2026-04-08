CREATE TABLE `lkt_bbs_browse`  (
                                   `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
                                   `post_id` bigint(0) NOT NULL COMMENT '文章ID',
                                   `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'user_id',
                                   `recycle` tinyint(0) NULL DEFAULT 0 COMMENT '是否删除 0.未删除 1.删除',
                                   `create_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                   PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '浏览表' ROW_FORMAT = Dynamic;



CREATE TABLE `lkt_bbs_category`  (
                                     `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
                                     `store_id` int(0) NOT NULL DEFAULT 0 COMMENT '商城ID',
                                     `sid` bigint(0) NOT NULL DEFAULT 0 COMMENT '上级ID',
                                     `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类名称',
                                     `img`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '选中分类图片',
                                     `bimg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '未选中分类图片',
                                     `level` int(0) NOT NULL DEFAULT 0 COMMENT '等级',
                                     `sort_order` int(0) NULL DEFAULT 0 COMMENT '排序',
                                     `recycle` tinyint(0) NULL DEFAULT 0 COMMENT '是否删除 0.未删除 1.删除',
                                     `create_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                     `is_popular` tinyint(0) NULL DEFAULT 0 COMMENT '是否是热门 0.不是热门 1.热门',
                                     PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;



CREATE TABLE `lkt_bbs_config`  (
                                   `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
                                   `store_id` int(0) NOT NULL DEFAULT 0 COMMENT '商城ID',
                                   `is_status` tinyint(0) NULL DEFAULT 0 COMMENT '开关 0.关闭 1.开启',
                                   `definition_template_id` int(0) NULL DEFAULT NULL COMMENT '视频转码模板id',
                                   `sample_template_id` int(0) NULL DEFAULT NULL COMMENT '视频采样截图模板id',
                                   `license_url` varchar(255) NULL DEFAULT NULL COMMENT '播放器许可证url',
                                   `secret_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '密钥',
                                   `security_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '防盗链 key',
                                   `region` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '储存位置',
                                   `secret_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '密钥id',
                                   `appid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'appid',
                                   `expire_time` int(0) NULL DEFAULT NULL COMMENT '过期时间(单位：小时)',
                                   `notify_url` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '回调地址',
                                   `is_cdn` tinyint(0) NULL DEFAULT 0 COMMENT '是否开启cdn 0：关闭 1：开启',
                                   `cdn_url` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'cdn域名播放地址',
                                   `notice` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '须知',
                                   `create_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                   PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;


CREATE TABLE `lkt_bbs_forum`  (
                                  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
                                  `store_id` int(0) NOT NULL DEFAULT 0 COMMENT '商城ID',
                                  `name`        varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '圈子名称',
                                  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '简介',
                                  `cover_img`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '封面',
                                  `head_img`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '圈子头像',
                                  `category_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '圈子分类',
                                  `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'user_id',
                                  `need_platform_review` tinyint(0) NULL DEFAULT 0 COMMENT '是否需要种草官审核 0.不需要 1.需要',
                                  `status` tinyint(0) NULL DEFAULT 1 COMMENT '状态 1.待审核  2.审核通过 3.审核未通过',
                                  `refuse_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '拒绝理由',
                                  `like_num` int(0) NOT NULL DEFAULT 0 COMMENT '点赞数',
                                  `post_num` int(0) NOT NULL DEFAULT 0 COMMENT '发布文章数',
                                  `collect_num` int(0) NOT NULL DEFAULT 0 COMMENT '收藏数',
                                  `forward_num` int(0) NOT NULL DEFAULT 0 COMMENT '转发数',
                                  `follow_num` int(0) NOT NULL DEFAULT 0 COMMENT '关注数',
                                  `recycle` tinyint(0) NULL DEFAULT 0 COMMENT '是否删除 0.未删除 1.删除',
                                  `create_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                  `update_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
                                  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '种草官表' ROW_FORMAT = Dynamic;


CREATE TABLE `lkt_bbs_label`  (
                                  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
                                  `store_id` int(0) NOT NULL COMMENT '商城id',
                                  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户id',
                                  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标签名',
                                  `recycle` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除 0.未删除 1.删除',
                                  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '修改时间',
                                  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '标签表' ROW_FORMAT = Dynamic;


CREATE TABLE `lkt_bbs_label_post`  (
                                       `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
                                       `store_id` int(0) NOT NULL COMMENT '商城id',
                                       `label_id` bigint(0) NOT NULL COMMENT '标签id',
                                       `post_id` bigint(0) NOT NULL COMMENT '文章id',
                                       PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '标签-文章关联表' ROW_FORMAT = Dynamic;



CREATE TABLE `lkt_bbs_post`  (
                                 `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
                                 `store_id` int(0) NOT NULL DEFAULT 0 COMMENT '商城ID',
                                 `forum_id` bigint(0) DEFAULT NULL COMMENT '种草官ID',
                                 `category_id` varchar(255) CHARACTER SET utf8mb4 COLLATE  utf8mb4_general_ci NOT NULL COMMENT '分类',
                                 `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'user_id',
                                 `title`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '帖子标题',
                                 `pro_ids` varchar(255) NULL DEFAULT NULL COMMENT '商品id，逗号分割',
                                 `content`     text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '文章内容',
                                 `images`      text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '文章图片',
                                 `video_id` varchar(255) NULL DEFAULT NULL COMMENT '云点播视频id',
                                 `type` tinyint(1) NULL DEFAULT NULL COMMENT '文章类型 0：图片 1：视频 2：图文',
                                 `cover_img`   text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '封面图',
                                 `videos`      text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '视频',
                                 `text`        text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '纯文字',
                                 `comment_num` int(0) NOT NULL DEFAULT 0 COMMENT '评论数量',
                                 `browse_num` int(0) NOT NULL DEFAULT 0 COMMENT '浏览数量',
                                 `like_num` int(0) NOT NULL DEFAULT 0 COMMENT '点赞数',
                                 `collect_num` int(0) NOT NULL DEFAULT 0 COMMENT '收藏数',
                                 `forward_num` int(0) NOT NULL DEFAULT 0 COMMENT '转发数',
                                 `follow_num` int(0) NOT NULL DEFAULT 0 COMMENT '关注数',
                                 `refuse_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '拒绝理由',
                                 `status` tinyint(0) NULL DEFAULT 1 COMMENT '1待审核  2审核通过 3审核未通过',
                                 `is_home_recommend` tinyint(0) NULL DEFAULT 0 COMMENT '是否为推荐 0.不是推荐 1.推荐',
                                 `is_hide` tinyint(0) NULL DEFAULT 0 COMMENT '是否隐藏 0.不隐藏 1.隐藏',
                                 `recycle` tinyint(0) NULL DEFAULT 0 COMMENT '是否删除 0.未删除 1.删除',
                                 `create_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                 `update_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
                                 PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '文章表' ROW_FORMAT = Dynamic;



CREATE TABLE `lkt_bbs_post_action`  (
                                        `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
                                        `post_id` bigint(0) DEFAULT NULL COMMENT '文章ID',
                                        `forum_id` bigint(0) DEFAULT NULL COMMENT '种草官ID',
                                        `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'user_id',
                                        `be_user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '被关注的user_id',
                                        `action_type` tinyint(0) NULL DEFAULT 0 COMMENT '类型 1.点赞 2.收藏 3.转发 4.关注',
                                        `recycle` tinyint(0) NULL DEFAULT 0 COMMENT '是否删除 0.未删除 1.删除',
                                        `create_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                        PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '互动' ROW_FORMAT = Dynamic;


CREATE TABLE `lkt_bbs_video`  (
                                  `id` bigint(0) NOT NULL AUTO_INCREMENT,
                                  `store_id` int(0) NOT NULL DEFAULT 0 COMMENT '商城ID',
                                  `file_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '视频id',
                                  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '播放地址',
                                  `cover_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面图',
                                  `status` tinyint(1) NULL DEFAULT 0 COMMENT '状态   0:准备上传 1：上传完成 2：处理完成',
                                  `response_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '回调返回数据',
                                  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '视频表' ROW_FORMAT = Dynamic;


CREATE TABLE `lkt_bbs_post_comment`  (
                                         `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
                                         `post_id` bigint(0) NOT NULL COMMENT '文章ID',
                                         `top_id` bigint(0) NULL DEFAULT 0 COMMENT '顶级id',
                                         `reply_num` int NULL DEFAULT 0 COMMENT '回复数',
                                         `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'user_id',
                                         `like_num` int(0) NULL DEFAULT 0 COMMENT '点赞数',
                                         `content` text CHARACTER SET utf8mb4 COLLATE  utf8mb4_general_ci NOT NULL COMMENT '内容',
                                         `parent_id` bigint(0) NULL DEFAULT 0 COMMENT '回复ID',
                                         `recycle` tinyint(0) NULL DEFAULT 0 COMMENT '是否删除 0.未删除 1.删除',
                                         `create_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                         PRIMARY KEY (`id`) USING BTREE,
                                         INDEX `idx_parent_id`(`parent_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE =  utf8mb4_general_ci COMMENT = '评论' ROW_FORMAT = Dynamic;


    CREATE TABLE `lkt_bbs_comment_like`  (
                                             `id` bigint(0) NOT NULL AUTO_INCREMENT,
                                             `post_id` bigint(0) NULL DEFAULT NULL COMMENT '文章id',
                                             `comment_id` bigint(0) NOT NULL COMMENT '评论id',
                                             `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户id',
                                             PRIMARY KEY (`id`) USING BTREE
    ) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '评论点赞表' ROW_FORMAT = Dynamic;


CREATE TABLE `lkt_bbs_template_config`  (
                                            `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
                                            `store_id` int(0) NOT NULL COMMENT '商城id',
                                            `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模板名称',
                                            `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模板图标',
                                            `img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模板主题',
                                            `status` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用 0：未启用 1：启用',
                                            `recycle` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除 0：未删除 1：删除',
                                            `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '添加时间',
                                            PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '笔记图片模板' ROW_FORMAT = Dynamic;
