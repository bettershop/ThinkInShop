CREATE TABLE `lkt_living_config`  (
    `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `store_id` int(11) NOT NULL DEFAULT 0 COMMENT '商城ID',
    `is_open` int(11) NOT NULL DEFAULT 0 COMMENT '获取是否开启插件 0-不开启 1-开启',
    `mch_is_open` int(11) NOT NULL DEFAULT 0 COMMENT '店铺端入口设置 0-不开启 1-开启',
    `push_url` varchar(255) not null COMMENT '推流地址',
    `play_url` varchar(255) not null COMMENT '播放地址',
    `agree_title` varchar(255) not null COMMENT '协议标题',
    `agree_content` text NULL COMMENT '协议内容',
    `add_time` timestamp NULL COMMENT '添加时间',
    `recycle` int(11) NOT NULL DEFAULT 0 COMMENT '回收站 0.显示 1.系统回收 2用户回收 3店铺回收',
    `license_key` varchar(255) not null COMMENT '腾讯云直播播放器的key',
    `license_url` varchar(255) not null COMMENT '腾讯云直播播放器的url',
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='直播配置';

CREATE TABLE `lkt_living_anchor`  (
    `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `store_id` int(11) NOT NULL DEFAULT 0 COMMENT '商城ID',
    `user_id` varchar(255) null COMMENT '主播用户ID',
    `commission` decimal(10, 2) default 0.00 not null COMMENT '佣金金额',
    `onlyamount` decimal(10, 2) default 0.00 not null COMMENT '累计佣金',
    `tx_commission` decimal(10, 2) default 0.00 not null COMMENT '可提现佣金',
    `recycle` int(11) default 0 not null COMMENT '回收站 0.显示 1.系统回收 2用户回收 3店铺回收',
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='主播表';

CREATE TABLE `lkt_living_room`  (
    `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `store_id` int(11) NOT NULL DEFAULT 0 COMMENT '商城ID',
    `living_theme` varchar(255) null COMMENT '直播的标题',
    `living_description` varchar(255) null COMMENT '直播简介',
    `start_time` timestamp NULL COMMENT '直播开始时间',
    `living_time` varchar(255) null COMMENT '直播时长',
    `end_time` timestamp NULL COMMENT '直播结束时间',
    `living_status` int(11) NOT NULL DEFAULT 0 COMMENT '直播的状态，0：预约中，1：直播中，2：已结束，3：已取消',
    `living_img` varchar(255) null COMMENT '直播间封面图地址',
    `add_time` timestamp NULL COMMENT '添加时间',
    `user_id` varchar(255) null COMMENT '主播用户ID',
    `living_num` int(11) NOT NULL DEFAULT 0 COMMENT '直播间观看人数',
    `living_review_num` int(11) NOT NULL DEFAULT 0 COMMENT '直播间评论数',
    `push_url` varchar(255) default '' null COMMENT '直播间推流地址',
    `play_url` varchar(255) default '' not null COMMENT '播放地址',
    `like_num` int(11) default 0 not null COMMENT '点赞数',
    `recycle` int(11) default 0 not null COMMENT '回收站 0.显示 1.系统回收 2用户回收 3店铺回收',
    `sort` int(11) default 0 not null COMMENT '排序',
    `living_fans_num` int(11) default 0 not null COMMENT '观看粉丝数',
    `fans_num` int(11) default 0 not null COMMENT '新增粉丝数',
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='直播间';

CREATE TABLE `lkt_living_follow_list`  (
    `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `store_id` int(11) NOT NULL DEFAULT 0 COMMENT '商城ID',
    `user_id` varchar(255) null COMMENT '用户ID',
    `anchor_id` varchar(255) default '0' not null COMMENT '主播ID',
    `add_time` timestamp NULL COMMENT '添加时间',
    `recycle` int(11) default 0 not null COMMENT '回收站 0.显示 1.系统回收 2用户回收 3店铺回收',
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户关注列表';

CREATE TABLE `lkt_living_commission`  (
    `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `store_id` int(11) NOT NULL DEFAULT 0 COMMENT '商城ID',
    `user_id` varchar(255) null COMMENT '用户ID',
    `living_id` varchar(255) default '0' not null COMMENT '直播间ID',
    `s_no` varchar(255) not null COMMENT '订单号',
    `commission` decimal(10, 2) default 0.00 not null COMMENT '佣金金额',
    `status` int(11) not null COMMENT '结算状态（101未发放，100已发放）',
    `add_time` timestamp NULL COMMENT '添加时间',
    `recycle` int(11) default 0 not null COMMENT '回收站 0.显示 1.系统回收 2用户回收 3店铺回收',
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='佣金表';

CREATE TABLE `lkt_living_product`  (
    `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `store_id` int(11) NOT NULL DEFAULT 0 COMMENT '商城ID',
    `living_id` int(11) default '0' not null COMMENT '直播间ID',
    `pro_id` int(11) default '0' not null COMMENT '产品ID',
    `config_id` int(11) default '0' not null COMMENT '规格ID',
    `total_num` int(11) default '0' not null COMMENT '上架库存',
    `num` int(11) default '0' not null COMMENT '剩余库存',
    `sort` int(11) default '1' not null COMMENT '排序',
    `xl_num` int(11) default '0' not null COMMENT '销量',
    `add_time` timestamp NULL COMMENT '添加时间',
    `recycle` int(11) default 0 not null COMMENT '回收站 0.显示 1.系统回收 2用户回收 3店铺回收',
    `represent` int(11) default 0 not null COMMENT '是否在讲解（每个直播间只能有一个讲解的产品，默认0不讲解）',
    `sort_num` int(11) default 0 not null COMMENT '序号',
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='直播间产品';

CREATE TABLE `lkt_living_audience`  (
    `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `store_id` int(11) NOT NULL DEFAULT 0 COMMENT '商城ID',
    `user_id` varchar(255) null COMMENT '用户ID',
    `living_id` int(11) default '0' not null COMMENT '直播间ID',
    `start_time` timestamp NULL COMMENT '开始时间',
    `end_time` timestamp NULL COMMENT '结束时间',
    `add_time` timestamp NULL COMMENT '添加时间',
    `recycle` int(11) default 0 not null COMMENT '回收站 0.显示 1.系统回收 2用户回收 3店铺回收',
    `sc` int(11) default '0' not null COMMENT '观看时长',
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='直播间观众';

ALTER TABLE `lkt_mch_store_write` 
MODIFY COLUMN `off_num` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '已预约核销次数' AFTER `write_off_num`;

ALTER TABLE `lkt_configure` 
ADD COLUMN `commission` varchar(255) NULL COMMENT '分佣比例' AFTER `write_off_num`,
ADD COLUMN `update_time` timestamp(0) NULL COMMENT '修改佣金比例的时间' AFTER `commission`,
ADD COLUMN `live_price` decimal(12, 2) NOT NULL COMMENT '直播价格' AFTER `update_time`;
ALTER TABLE `lkt_product_list` 
MODIFY COLUMN `commodity_type` tinyint(2) NULL DEFAULT 0 COMMENT '商品类型 0.实物商品 1.虚拟商品 2.直播商品' AFTER `product_number`;

ALTER TABLE `lkt_user` 
ADD COLUMN `token_living_pc` varchar(255) NULL COMMENT '主播pc的token' AFTER `is_default_birthday`;

CREATE TABLE `lkt_living_user_like`  (
    `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `store_id` int(11) NOT NULL DEFAULT 0 COMMENT '商城ID',
    `user_id` varchar(255) null COMMENT '用户ID',
    `living_id` int(11) default '0' not null COMMENT '直播间ID',
    `dzs_num` int(11) default '0' not null COMMENT '点赞数',
    `add_time` timestamp NULL COMMENT '添加时间',
    `recycle` int(11) default 0 not null COMMENT '回收站 0.显示 1.系统回收 2用户回收 3店铺回收',
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='我的点赞';

CREATE TABLE `lkt_living_comment`  (
    `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `store_id` int(11) NOT NULL DEFAULT 0 COMMENT '商城ID',
    `living_id` int(11) default '0' not null COMMENT '直播间ID',
    `user_id` varchar(255) null COMMENT '用户ID',
    `comment` varchar(255) null COMMENT '评论',
    `p_id` int(11) default '0' not null COMMENT '上级ID',
    `add_time` timestamp NULL COMMENT '添加时间',
    `recycle` int(11) default 0 not null COMMENT '回收站 0.显示 1.系统回收 2用户回收 3店铺回收',
    `comment_type` int(11) default '0' not null COMMENT '评论类型 1评论，2关注，3点赞,4加入',
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='直播间评论';

ALTER TABLE `lkt_living_product` 
ADD COLUMN `live_price` decimal(12, 2) NOT NULL COMMENT '直播价格' AFTER `config_id`;


-- BAE CONSULTING
-- 1613499679

-- 曲江新村物业
-- 1683633058

-- 赛德康清洁
-- 1640909844



-- 山禾集
-- APPID: wx28a199c85a26c2cc
-- KEY: dd75c387b65dabf19527176923f819bd
-- 商户号: 1671842319
-- 密钥: djfu784h7ryt8jg7jn5u87gnj87huy76



-- 原本数据
-- APPID: wx5ae2bb641565e4a3
-- KEY: da53084518c63d2047b105d51a12d0da
-- 商户号: 1516978921
-- 密钥: WPCD6ZZMY7CLTWZQPNZ3H2PLDF4YOQPQ
-- apiclient_cert.pem: 
-- -----BEGIN CERTIFICATE-----
-- MIID9jCCAt6gAwIBAgIUaZ7fJEms5FVPSVFFz9NtGAnp6TgwDQYJKoZIhvcNAQEL
-- BQAwXjELMAkGA1UEBhMCQ04xEzARBgNVBAoTClRlbnBheS5jb20xHTAbBgNVBAsT
-- FFRlbnBheS5jb20gQ0EgQ2VudGVyMRswGQYDVQQDExJUZW5wYXkuY29tIFJvb3Qg
-- Q0EwHhcNMjIxMTE2MDY0NjQzWhcNMjcxMTE1MDY0NjQzWjCBhzETMBEGA1UEAwwK
-- MTUxNjk3ODkyMTEb MBkGA1UECgwS5b6u5L+h5ZWG5oi357O757ufMTMwMQYDVQQL
-- DCrmuZbljZflo7nmi77mjYzlj7fnvZHnu5zmioDmnK/mnInpmZDlhazlj7gxCzAJ
-- BgNVBAYMAkNOMREwDwYDVQQHDAhTaGVuWmhlbjCCASIwDQYJKoZIhvcNAQEBBQAD
-- ggEPADCCAQoCggEBAMPsx30avifwgP8W67qVvrZ8si4rLu7SKlsWHqWMUBkDF82y
-- e1HLSziz2CzekSSuM3iNXsMGEDxZnohFO44w7ua2QWg/94jsCE04yWmsgXW70eTw
-- uuziM6y00X6+5RD6UFR+bKVhfzB/2zn3wt8F0W/v1n22xOFAtzAys3gltgzIldD2
-- 4VXKCPPYBbZF579U6RYemit1VNS7lMLOAYDuWkRTp7JeT8N2N1M9+Qkt8w2K1cIm
-- r+tl8c5xhkRqeRrIV7gVs0ASG8qThtkQxDITiPvpgp7G+rTleMzvEDV6GZZMy9XF
-- 9Nu1Lftm5GX0MNQ3xjkHdmWEoH72Wv1zkDb4r9UCAwEAAaOBgTB/MAkGA1UdEwQC
-- MAAwCwYDVR0PBAQDAgP4MGUGA1UdHwReMFwwWqBYoFaGVGh0dHA6Ly9ldmNhLml0
-- cnVzLmNvbS5jbi9wdWJsaWMvaXRydXNjcmw/Q0E9MUJENDIyMEU1MERCQzA0QjA2
-- QUQzOTc1NDk4NDZDMDFDM0U4RUJEMjANBgkqhkiG9w0BAQsFAAOCAQEAcqijmnfi
-- S6Q1Y4uPf4vhj4Ml4RCga4WMD5dhflX71vIro4NTSjSOUxeV2C+OvlpzShkud/QA
-- mtP0y6A6lk3FqNu9qztigkHZ/AdVY352g61XpX12laAKSVeULinEQjDytHN9JTj+
-- A4oU7r4YI1Px2s7zierQmOBVM7YuvgUv0DZ67k3OnJwME6Bz/2fN0PJE6rCSUlw+
-- hshd6dKRsNmsNSW/ITY1D8OzAT4RLHLrfN4y7zsH8aQKGq4Bj7qLz7Bvtc+1W14+
-- UNWE8iXL40shmto+6elWDc0Dz0mZJ8WwIgkl6vFejOkpcOhFcqxkxR4Ql1n2/YdR
-- ySX/ylifgld9gQ==
-- -----END CERTIFICATE-----

-- apiclient_key.pem: 
-- -----BEGIN PRIVATE KEY-----
-- MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDD7Md9Gr4n8ID/
-- Fuu6lb62fLIuKy7u0ipbFh6ljFAZAxfNsntRy0s4s9gs3pEkrjN4jV7DBhA8WZ6I
-- RTuOMO7mtkFoP/eI7AhNOMlprIF1u9Hk8Lrs4jOstNF+vuUQ+lBUfmylYX8wf9s5
-- 98LfBdFv79Z9tsThQLcwMrN4JbYMyJXQ9uFVygjz2AW2Ree/VOkWHpordVTUu5TC
-- zgGA7lpEU6eyXk/DdjdTPfkJLfMNitXCJq/rZfHOcYZEankayFe4FbNAEhvKk4bZ
-- EMQyE4j76YKexvq05XjM7xA1ehmWTMvVxfTbtS37ZuRl9DDUN8Y5B3ZlhKB+9lr9
-- c5A2+K/VAgMBAAECggEBAJJfmYIrspkgIx9wvlg9e6fWbT/03zFpLl+ynN7NfyHQ
-- n737dApduSr5oa2T8hfXVXjhFUTaiNKtmbTxFFdc037l9uOttn9C58jZfhBG4slU
-- srE58Ez9ieGjjUgF0hjnCZC2ivDbcCyJuP0LzzjTne7k3fg5zckDklid6FsAwYu2
-- GMZLOpau39IEfsuE2+ZMlbcsvMdHv/JAiX3P1WkgbFslA94/D9zf/iqkprL3JUOn
-- KiGcsfaE6BZ5m3c1+b0sjdH5ziMjgnvjI5YVOMWi1EdztgSBj/H0BYM7d35XKgKe
-- PSWiaVET1/sLS/IQ4sOhfAN9HU7N6R1cjBmNYBn0e0ECgYEA6rNI2NdhhU72jScC
-- dK7H0F/tt3vHNfq2Q9DvqtDpfv7/Kl2kFLrhdunfyRkEBHTkamTadbLtguOwhTIj
-- 1CjrFt165M1cJSIRcq1BCRHiej4UdzgsTtc6YAW6B7vfvJN3m4xQAoHJHgpQfU4g
-- HrQuOJuF6vMVrFG8SH2zA5w0tdkCgYEA1bSjaiR9lSev067AZ344+sjldXDh8H7P
-- vvDrojbPzMo+MJRtXCbFfJVmAIxyIGYz7QSQpn9VZhrbrjcxQS2apZ2cR8F/ENXi
-- JpxuGV76Y2vsACiFy/C4qZTUirAMswWSO6zmRZJKS8h0fYw7fLDN0E40OzXOqwBz
-- jBfyhoWjoF0CgYB049XkKWDog+tHYaZXdrUNkrlSKNOOjs8391dmO6YPGfD7qKeP
-- q4OfNdu+zDa0AYPBeXlUJUklZ0EDFXXCtJ5hRO1wBtddpxoJg0BgxAeH2mNW/SMU
-- IR1xtWhxcaB8yvVHAxrWJM2FqJ1tyvs0T4i1qL+JxIeGyy567B+8LJAUAQKBgGkv
-- VPOMbGd6tjcpnzgRT3hYY2BPpFWcBSLFMCWSduskbEG6/KzLxI5Sdnc0qYO66jnx
-- 8kSRDfaIp7HzJoIfMSvtDJvg6Grf60T339ULA5Dl8K4PQ/OiUzHcuFRe3JFM37IR
-- ovGBVKJCUtvspubzmwm5REoUbNmMClNdAhkmM+hdAoGARcylKv9Lhj1S2A7MNmOw
-- wpy0s0b1Df4UcD+nTnxKn5U+rq98uIZLcEepFaHLQ8+OU33ABOHczPdzq6F3UNVV
-- LbmsWLXVXyYiow9XIl0khx+1UPVavL0HEUPmbDKMJYmLAKjPAs2X+VuYJdsBdou1
-- t5c7yw0Ev9KoJnzj8HtWml8=
-- -----END PRIVATE KEY-----
