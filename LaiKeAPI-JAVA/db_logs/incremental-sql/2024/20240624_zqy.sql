alter table lkt_invoice_header
    modify add_time timestamp not null comment '添加时间';

alter table lkt_configure
    alter column bargain_price set default 0.0;