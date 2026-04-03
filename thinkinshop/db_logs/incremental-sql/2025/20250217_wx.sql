alter table lkt_data_dictionary_name
    add lang_code VARCHAR(10) default 'zh_CN' not null comment '语种编码';

alter table lkt_data_dictionary_name
    add country_num int default 156 null comment '国家编码';

alter table lkt_freight
    add lang_code varchar(10) default 'zh_CN' null comment '语种编码';

alter table lkt_freight
    add country_num int default 156 null comment '国家编码默认156中国';

alter table lkt_pro_label
    add lang_code varchar(10) default 'zh_CN' null comment '语种编码默认中文简体';

alter table lkt_pro_label
    add country_num int default 156 null comment '国家语种默认156';


alter table lkt_product_class
    add lang_code varchar(10) default 'zh_CN' null comment '语种编码默认中文简体';

alter table lkt_product_class
    add country_num int default 156 null comment '国家语种默认156';


alter table lkt_brand_class
    change producer country_num varchar(255) null comment '产地';
alter table lkt_brand_class
    modify country_num int default 156 null comment '产地所属国家，默认156中国';

update lkt_brand_class  set country_num = 156  where country_num = 1;

alter table lkt_brand_class
    add lang_code varchar(10) default 'zh_CN' null comment '语种编码默认中文简体';

alter table lkt_data_dictionary_list
    add lang_code VARCHAR(20) default 'zh_CN' null comment '语种';

alter table lkt_data_dictionary_list
    add country_num int default 156 null comment '所属国家';



ALTER TABLE lkt_config
    ADD COLUMN `default_currency` varchar(255) NULL COMMENT '默认币种' AFTER `default_lang_code`;

-- 商城可以绑定多种货币，在平台由超管绑定；但是每个商城只有一种基础货币【计算货币】，移动商城、pc商城中用户选择不同的币种后将商品价格和订单计算的结算金额按照基础币种的汇率进行换算



ALTER TABLE lkt_brand_class
    MODIFY COLUMN `country_num` int NOT NULL DEFAULT 156 COMMENT '产地所属国家 默认 156 中国' AFTER `brand_y_name`;



CREATE TABLE lkt_currency (
  id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  currency_code CHAR(3) NOT NULL COMMENT 'ISO货币代码(如USD)',
  currency_name VARCHAR(50) NOT NULL COMMENT '货币名称',
  is_show int not null default  1 comment  '是否展示 0 不展示 1展示 ',
  currency_symbol VARCHAR(10) NOT NULL COMMENT '货币符号($)',
#   exchange_rate DECIMAL(10,4) NULL COMMENT '基础货币汇率',
  recycle int not null default  1 comment  '是否删除 0 未删除 1已删除 ',
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '平台货币信息';


CREATE TABLE lkt_currency_store (
    store_id int COMMENT '商城id',
    currency_id INT COMMENT '货币id',
    is_show int not null default  1 comment  '是否展示 0 不展示 1展示 ',
    default_currency INT default 0 COMMENT '是否商城基础货币【结算货币】默认 0 否 1 是',
    exchange_rate DECIMAL(10,4) NOT NULL COMMENT '基础货币汇率',
    recycle int not null default  1 comment  '是否删除 0 未删除 1已删除 ',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '商城货币';


ALTER TABLE lkt_user
    ADD COLUMN `preferred_currency` int NULL COMMENT '用户偏好货币id' AFTER `country_num`;


