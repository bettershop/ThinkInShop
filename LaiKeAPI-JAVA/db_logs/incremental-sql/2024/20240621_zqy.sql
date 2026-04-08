alter table lkt_configure
    alter column msrp set default 0.00;

alter table lkt_invoice_info
    modify file_time timestamp null comment '上传发票文件时间';


alter table lkt_invoice_info
    modify add_time timestamp not null comment '添加时间';