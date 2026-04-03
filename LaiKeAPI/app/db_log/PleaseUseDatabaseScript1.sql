-- 保留1个商城
-- 营销活动
TRUNCATE TABLE lkt_activity;
-- 营销活动中的商品
TRUNCATE TABLE lkt_activity_pro;
-- 后台管理员 
DELETE FROM lkt_admin  WHERE id not in (1,2); -- 商城1的商城管理员id=541
update lkt_admin set shop_id = 0,role = 0;
-- 操作日志
TRUNCATE TABLE lkt_admin_record;
-- 协议配置
TRUNCATE TABLE lkt_agreement;
-- 单篇文章表
TRUNCATE TABLE lkt_article;
-- 竞拍收藏
TRUNCATE TABLE lkt_auction_collection;
-- 竞拍配置
TRUNCATE TABLE lkt_auction_config;
-- 竞拍商品
TRUNCATE TABLE lkt_auction_product;
-- 竞拍保证金
TRUNCATE TABLE lkt_auction_promise;
-- 竞价记录
TRUNCATE TABLE lkt_auction_record;
-- 竞拍提醒
TRUNCATE TABLE lkt_auction_remind;
-- 竞拍场次
TRUNCATE TABLE lkt_auction_session;
-- 竞拍专场
TRUNCATE TABLE lkt_auction_special;
-- 角色菜单绑定表
TRUNCATE TABLE lkt_authority_mapping;
-- 数据备份配置表
TRUNCATE TABLE lkt_backup_config;
-- 数据备份记录表
TRUNCATE TABLE lkt_backup_record;
-- 银行卡
TRUNCATE TABLE lkt_bank_card;
-- 首页轮播图
TRUNCATE TABLE lkt_banner;
-- pc商城首页楼层
TRUNCATE TABLE lkt_block_home;
-- pc商城首页楼层中的商品
TRUNCATE TABLE lkt_block_product;
-- 提现保证金记录表
TRUNCATE TABLE lkt_bond;
-- 商品品牌表
TRUNCATE TABLE lkt_brand_class;
-- 再次购买
TRUNCATE TABLE lkt_buy_again;
-- 购物车
TRUNCATE TABLE lkt_cart;
-- 收银台记录表
TRUNCATE TABLE lkt_cashier_record;
-- 客户端表
-- TRUNCATE TABLE lkt_client;
-- 评价记录
TRUNCATE TABLE lkt_comments;
-- 评价图片
TRUNCATE TABLE lkt_comments_img;
-- 商城配置
DELETE FROM lkt_config WHERE id <> 1;
-- 商品规格
TRUNCATE TABLE lkt_configure;
-- 核心菜单
-- TRUNCATE TABLE lkt_core_menu;
-- 优惠券
TRUNCATE TABLE lkt_coupon;
-- 优惠劵活动表
TRUNCATE TABLE lkt_coupon_activity;
-- 优惠券配置表
TRUNCATE TABLE lkt_coupon_config;
-- 优惠券赠送记录
TRUNCATE TABLE lkt_coupon_presentation_record;
-- 优惠券使用订单记录
TRUNCATE TABLE lkt_coupon_sno;
-- 商城
DELETE FROM lkt_customer WHERE id <> 1;
-- 数据字典目录表
TRUNCATE TABLE lkt_data_dictionary_list;
-- 数据字典名称表
TRUNCATE TABLE lkt_data_dictionary_name;
-- 分销配置
TRUNCATE TABLE lkt_distribution_config;
-- 分销商品
TRUNCATE TABLE lkt_distribution_goods;
-- 分销等级
TRUNCATE TABLE lkt_distribution_grade;
-- 分销系统收益统计报表(php没有)
-- TRUNCATE TABLE lkt_distribution_income;
-- 分销日志
TRUNCATE TABLE lkt_distribution_log;
-- 分销排名
TRUNCATE TABLE lkt_distribution_ranking;
-- 分销记录
TRUNCATE TABLE lkt_distribution_record;
-- 分销提现
TRUNCATE TABLE lkt_distribution_withdraw;
-- 首页diy模版
TRUNCATE TABLE lkt_diy;
-- （PHP没有）
-- TRUNCATE TABLE lkt_diy_page;
-- Diy项目配置信息（PHP没有）
-- TRUNCATE TABLE lkt_diy_project;
-- 草稿箱
TRUNCATE TABLE lkt_drafts;
-- 国家信息
-- TRUNCATE TABLE lkt_ds_country;
-- 版本表
TRUNCATE TABLE lkt_edition;
-- 快递公司
-- TRUNCATE TABLE lkt_express;
-- 快递记录表
TRUNCATE TABLE lkt_express_delivery;
-- 快递公司子表
TRUNCATE TABLE lkt_express_subtable;
-- 推广图片表
TRUNCATE TABLE lkt_extension;
-- 批量发货
TRUNCATE TABLE lkt_file_delivery;
-- 上传文件记录表
TRUNCATE TABLE lkt_files_record;
-- 钱包配置表
TRUNCATE TABLE lkt_finance_config;
-- 限时折扣活动
TRUNCATE TABLE lkt_flashsale_activity;
-- 限时折扣加购商品
TRUNCATE TABLE lkt_flashsale_addgoods;
-- 限时折扣配置
TRUNCATE TABLE lkt_flashsale_config;
-- 限时折扣活动
TRUNCATE TABLE lkt_flashsale_label;
-- 限时折扣商品
TRUNCATE TABLE lkt_flashsale_pro;
-- 限时折扣记录
TRUNCATE TABLE lkt_flashsale_record;
-- 运费表
TRUNCATE TABLE lkt_freight;
-- 拼团订单
TRUNCATE TABLE lkt_go_group_order;
-- 拼团订单详情
TRUNCATE TABLE lkt_go_group_order_details;
-- 拼团活动表
TRUNCATE TABLE lkt_group_activity;
-- 拼团配置
TRUNCATE TABLE lkt_group_config;
-- 拼团商品表
TRUNCATE TABLE lkt_group_goods;
-- 开团表
TRUNCATE TABLE lkt_group_open;
-- 拼团详情表
TRUNCATE TABLE lkt_group_open_record;
-- 拼团订单设置
TRUNCATE TABLE lkt_group_order_config;
-- 拼团商品表【弃用】
TRUNCATE TABLE lkt_group_product;
-- 引导图
TRUNCATE TABLE lkt_guide;
-- 导览菜单
TRUNCATE TABLE lkt_guide_menu;
-- 热搜词【移动端】
TRUNCATE TABLE lkt_hotkeywords;
-- 图片分组
TRUNCATE TABLE lkt_img_group;
-- 文件导入记录表（java没有）
TRUNCATE TABLE lkt_import_log;
-- 积分商城分类
TRUNCATE TABLE lkt_integral_class;
-- 积分配置表
TRUNCATE TABLE lkt_integral_config;
-- 积分商城商品表
TRUNCATE TABLE lkt_integral_goods;
-- 发票抬头表
TRUNCATE TABLE lkt_invoice_header;
-- 发票信息表
TRUNCATE TABLE lkt_invoice_info;
-- 路径跳转表
TRUNCATE TABLE lkt_jump_path;
-- 会员等级变更记录表
TRUNCATE TABLE lkt_level_update;
-- 主播表（java没有）
TRUNCATE TABLE lkt_living_anchor;
-- 直播间观众
TRUNCATE TABLE lkt_living_audience;
-- 直播间评论
TRUNCATE TABLE lkt_living_comment;
-- 佣金表
TRUNCATE TABLE lkt_living_commission;
-- 直播配置
TRUNCATE TABLE lkt_living_config;
-- 用户关注列表
TRUNCATE TABLE lkt_living_follow_list;
-- 直播间产品
TRUNCATE TABLE lkt_living_product;
-- 直播间
TRUNCATE TABLE lkt_living_room;
-- 统计表
TRUNCATE TABLE lkt_living_sensitive;
-- （PHP没有）
-- TRUNCATE TABLE lkt_living_statistics;
-- 我的点赞
TRUNCATE TABLE lkt_living_user_like;
-- 地址信息
TRUNCATE TABLE lkt_map;
-- 商户
TRUNCATE TABLE lkt_mch;
-- 入驻商户账户收支记录表
TRUNCATE TABLE lkt_mch_account_log;
-- 店铺管理员
TRUNCATE TABLE lkt_mch_admin;
-- 店铺管理员核销记录
TRUNCATE TABLE lkt_mch_admin_record;
-- 店铺浏览记录表
TRUNCATE TABLE lkt_mch_browse;
-- 店铺购买力表
TRUNCATE TABLE lkt_mch_buy_power;
-- 店铺分类
TRUNCATE TABLE lkt_mch_class;
-- 店铺配置表
TRUNCATE TABLE lkt_mch_config;
-- 分账账户信息
TRUNCATE TABLE lkt_mch_distribution;
-- 分账记录信息
TRUNCATE TABLE lkt_mch_distribution_record;
-- 店铺交易数据表
TRUNCATE TABLE lkt_mch_order_record;
-- 店铺保证金记录表
TRUNCATE TABLE lkt_mch_promise;
-- 店铺数据汇总表
TRUNCATE TABLE lkt_mch_statistics;
-- 店铺门店
TRUNCATE TABLE lkt_mch_store;
-- 店铺门店账号表（PHP没有）
-- TRUNCATE TABLE lkt_mch_store_account;
-- 虚拟商品订单核销表
TRUNCATE TABLE lkt_mch_store_write;
-- 会员配置
TRUNCATE TABLE lkt_member_config;
-- 会员商品表
TRUNCATE TABLE lkt_member_pro;
-- 通用 菜单表
-- TRUNCATE TABLE lkt_menu;
-- 短信模板列表
TRUNCATE TABLE lkt_message;
-- 短信配置
TRUNCATE TABLE lkt_message_config;
-- 短信列表
TRUNCATE TABLE lkt_message_list;
-- 店铺消息记录表
TRUNCATE TABLE lkt_message_logging;
-- 模板消息表【微信小程序订阅消息】
TRUNCATE TABLE lkt_notice;
-- IM聊天记录
TRUNCATE TABLE lkt_online_message;
-- 订单
TRUNCATE TABLE lkt_order;
-- 订单设置
TRUNCATE TABLE lkt_order_config;
-- 充值、会员续费、保证金等非订单类费用记录
TRUNCATE TABLE lkt_order_data;
-- 订单详情
TRUNCATE TABLE lkt_order_details;
-- 订单报表信息
TRUNCATE TABLE lkt_order_report;
-- 支付方式表
-- TRUNCATE TABLE lkt_payment;
-- 支付方式参数表
TRUNCATE TABLE lkt_payment_config;
-- PC商城底部栏图片配置
TRUNCATE TABLE lkt_pc_mall_bottom;
-- PC商城页面显示配置
TRUNCATE TABLE lkt_pc_mall_config;
-- 平台活动表
TRUNCATE TABLE lkt_platform_activities;
-- 商户删除平台管理活动表
TRUNCATE TABLE lkt_platform_activities_del;
-- 插件表(php没有)
-- TRUNCATE TABLE lkt_plug_ins;
-- PC商城首页普通展示
DELETE FROM lkt_plugins WHERE store_id not in (0,1);
-- 预售配置表
TRUNCATE TABLE lkt_pre_sell_config;
-- 预售商品表
TRUNCATE TABLE lkt_pre_sell_goods;
-- 预售记录表
TRUNCATE TABLE lkt_pre_sell_record;
-- 订单打印配置
TRUNCATE TABLE lkt_print_setup;
-- 商品标签
TRUNCATE TABLE lkt_pro_label;
-- 商品分类
TRUNCATE TABLE lkt_product_class;
-- 商品配置
TRUNCATE TABLE lkt_product_config;
-- 商品图片
TRUNCATE TABLE lkt_product_img;
-- 商品信息
TRUNCATE TABLE lkt_product_list;
-- 商品编码 无用
TRUNCATE TABLE lkt_product_number;
-- 保证金审核记录表
TRUNCATE TABLE lkt_promise_record;
-- 保证金审核
TRUNCATE TABLE lkt_promise_sh;
-- 操作记录表 
TRUNCATE TABLE lkt_record;
-- 操作记录表详情
TRUNCATE TABLE lkt_record_details;
-- 评论回复
TRUNCATE TABLE lkt_reply_comments;
-- 用户退货表
TRUNCATE TABLE lkt_return_goods;
-- 售后订单表
TRUNCATE TABLE lkt_return_order;
-- 售后记录表
TRUNCATE TABLE lkt_return_record;
-- 商城角色表
TRUNCATE TABLE lkt_role;
-- 商城角色权限绑定表
TRUNCATE TABLE lkt_role_menu;
-- 积分过期记录表
TRUNCATE TABLE lkt_score_over;
-- 秒杀活动
TRUNCATE TABLE lkt_seconds_activity;
-- 秒杀配置
TRUNCATE TABLE lkt_seconds_config;
-- 秒杀活动单天数删除记录表
TRUNCATE TABLE lkt_seconds_day_delete;
-- 秒杀标签【类似时间段】
TRUNCATE TABLE lkt_seconds_label;
-- 秒杀商品
TRUNCATE TABLE lkt_seconds_pro;
-- 秒杀记录
TRUNCATE TABLE lkt_seconds_record;
-- 秒杀提醒
TRUNCATE TABLE lkt_seconds_remind;
-- 秒杀时段
TRUNCATE TABLE lkt_seconds_time;
-- 订单配送信息表
TRUNCATE TABLE lkt_self_delivery_info;
-- 敏感词表
TRUNCATE TABLE lkt_sensitive_words;
-- 售后地址
TRUNCATE TABLE lkt_service_address;
-- session_id表
TRUNCATE TABLE lkt_session_id;
-- 公告列表
TRUNCATE TABLE lkt_set_notice;
-- 积分抵用消费金额表
TRUNCATE TABLE lkt_setscore;
-- 签到配置表
TRUNCATE TABLE lkt_sign_config;
-- 签到记录（积分）
TRUNCATE TABLE lkt_sign_record;
-- 属性表
TRUNCATE TABLE lkt_sku;
-- 商品库存表
TRUNCATE TABLE lkt_stock;
-- 满减表(php没有)
-- TRUNCATE TABLE lkt_subtraction;
-- 满减表设置(php没有)
-- TRUNCATE TABLE lkt_subtraction_config;
-- 满减记录表(php没有)
-- TRUNCATE TABLE lkt_subtraction_record;
-- 供应商
TRUNCATE TABLE lkt_supplier;
-- 供应商收支记录表
TRUNCATE TABLE lkt_supplier_account_log;
-- 供应商银行卡
TRUNCATE TABLE lkt_supplier_bank_card;
-- 商品品牌表
TRUNCATE TABLE lkt_supplier_brand;
-- 供应商配置表
TRUNCATE TABLE lkt_supplier_config;
-- 供应商商品配置表
TRUNCATE TABLE lkt_supplier_configure;
-- 供应商订单运费结算
TRUNCATE TABLE lkt_supplier_order_fright;
-- 供应商商品表
TRUNCATE TABLE lkt_supplier_pro;
-- 商品分类
TRUNCATE TABLE lkt_supplier_pro_class;
-- 供应商库存记录表
TRUNCATE TABLE lkt_supplier_stock;
-- 供应商提现列表
TRUNCATE TABLE lkt_supplier_withdraw;
-- 系统公告阅读表
TRUNCATE TABLE lkt_system_bulletin_reading;
-- 商城基本信息配置表
TRUNCATE TABLE lkt_system_configuration;
-- 系统消息表
TRUNCATE TABLE lkt_system_message;
-- 系统公告表
TRUNCATE TABLE lkt_system_tell;
-- 系统公告商户查阅(PHP没有)
-- TRUNCATE TABLE lkt_system_tell_store;
-- 用户端标记公告以读
TRUNCATE TABLE lkt_system_tell_user;
-- 第三方授权表
UPDATE lkt_third SET ticket = NULL,ticket_time = '0000-00-00 00:00:00',token = NULL,token_expires = NULL,appid = NULL,appsecret = NULL,check_token = NULL,encrypt_key = NULL,serve_domain = NULL,work_domain = NULL,redirect_url = NULL,mini_url = NULL,kefu_url = NULL,qr_code = NULL,H5 = NULL,endurl = NULL;
-- 第三方用户映射表
TRUNCATE TABLE lkt_third_mapping;
-- 授权小程序信息表
TRUNCATE TABLE lkt_third_mini_info;
-- 小程序模板表【目前未用，微信小程序第三方授权发布的功能目前不支持】
TRUNCATE TABLE lkt_third_template;
-- UI导航栏表
TRUNCATE TABLE lkt_ui_navigation_bar;
-- 文件上传配置表
TRUNCATE TABLE lkt_upload_set;
-- 用户信息表
DELETE FROM lkt_user WHERE id <> 1;
-- 用户地址表
TRUNCATE TABLE lkt_user_address;
-- 用户通用权限绑定
TRUNCATE TABLE lkt_user_authority;
-- 用户银行卡
TRUNCATE TABLE lkt_user_bank_card;
-- 用户收藏表
TRUNCATE TABLE lkt_user_collection;
-- 分销用户表
TRUNCATE TABLE lkt_user_distribution;
-- 等级会员首次开通表
TRUNCATE TABLE lkt_user_first;
-- 用户浏览足迹
TRUNCATE TABLE lkt_user_footprint;
-- 用户会员登录
TRUNCATE TABLE lkt_user_grade;
-- 通用菜单角色表
TRUNCATE TABLE lkt_user_role;
-- 会员规则表
TRUNCATE TABLE lkt_user_rule;
-- 用户搜索
TRUNCATE TABLE lkt_user_search;
-- 提现记录
TRUNCATE TABLE lkt_withdraw;
-- 核销记录表
TRUNCATE TABLE lkt_write_record;


-- 商品分类(默认1商城其他)
INSERT INTO `lkt_product_class`(`sid`, `pname`, `english_name`, `img`, `bg`, `level`, `sort`, `store_id`, `add_date`, `recycle`, `is_display`, `is_default`) VALUES (0, '其它', '', '', NULL, 0, 29, 1, '2023-10-01 00:00:00', 0, 1, 1);
-- 商品品牌(默认1商城其他)
INSERT INTO `lkt_brand_class` (`brand_id`, `store_id`, `brand_pic`, `brand_image`, `brand_name`, `brand_y_name`, `producer`, `remarks`, `status`, `brand_time`, `sort`, `recycle`, `categories`, `is_default`) VALUES (1, 1, '1613473045068574720.png', NULL, '其他', '', '1', NULL, 0, date_format(now(), '%Y-%m-%d %H:%I:%S'), 233, 0, ',1,', 1);

INSERT INTO `lkt_pro_label`( `store_id`, `name`, `add_time`, `color`) VALUES ( 1, '新品', '2023-10-01 00:00:00', '#FA5151');
INSERT INTO `lkt_pro_label`( `store_id`, `name`, `add_time`, `color`) VALUES ( 1, '热销', '2023-10-01 00:00:00', '#FA9D3B');
INSERT INTO `lkt_pro_label`( `store_id`, `name`, `add_time`, `color`) VALUES ( 1, '推荐', '2023-10-01 00:00:00', '#1485EE');
