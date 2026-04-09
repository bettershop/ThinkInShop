alter table lkt_living_config
    add license_key varchar(255) not null comment '腾讯云直播播放器的key';

alter table lkt_living_config
    add license_url varchar(255) not null comment '腾讯云直播播放器的url';
