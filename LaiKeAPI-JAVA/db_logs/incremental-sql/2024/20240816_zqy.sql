create table if not exists lkt_backup_config
(
    id           int(11) unsigned auto_increment comment '配置id' primary key,
    store_id     int default 0                    not null comment '商城id',
    is_open      int(11)  default 0               not null comment '是否开启自动备份,0关闭，1开启',
    execute_cycle    varchar(255)                          comment '执行周期',
    url          varchar(255)                              comment '保存路径',
    add_time     timestamp                                 comment '添加时间',
    query_data   int(11)                                   comment '前端回显需要',
    recycle   int default 0 not null  comment '回收站 0.显示 1.系统回收 2用户回收 3店铺回收'
) comment '数据备份配置表' collate = utf8mb4_general_ci;


create table if not exists lkt_backup_record
(
    id           int(11) unsigned auto_increment comment '记录id' primary key,
    store_id     int default 0                    not null comment '商城id',
    file_name    varchar(255)                              comment '文件名称',
    file_size    varchar(255)                              comment '文件大小',
    file_type    varchar(255)                              comment '文件类型',
    file_url     varchar(255)                              comment '文件路径',
    status       int(11)                                   comment '状态',
    add_time     timestamp                                  comment '添加时间',
    recycle   int default 0 not null comment '回收站 0.显示 1.系统回收 2用户回收 3店铺回收'
) comment '数据备份记录表' collate = utf8mb4_general_ci;