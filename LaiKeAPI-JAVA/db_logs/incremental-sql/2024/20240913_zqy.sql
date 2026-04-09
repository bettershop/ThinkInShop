alter table lkt_auction_promise
    add type varchar(255) null comment '支付方式';

alter table lkt_auction_promise
    add address_id int null comment '收货地址id';

alter table lkt_auction_promise
    add source int null comment '来源 1.小程序 2.app 3.支付宝小程序 4.头条小程序 5.百度小程序 6.pc端 7.H5';