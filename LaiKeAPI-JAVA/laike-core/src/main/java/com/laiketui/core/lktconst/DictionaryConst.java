package com.laiketui.core.lktconst;

/**
 * 字典映射
 *
 * @author Trick
 * @date 2020/9/28 13:52
 */
public interface DictionaryConst
{
    /**
     * 短信模板类型
     */
    String SMS_TEMPLATE_TYPE = "短信模板类型";

    /**
     * 短信模板类别
     */
    String SMS_TEMPLATE_CATEGORY = "短信模板类别";

    /**
     * 新品
     */
    String LKT_SPLX_001 = "1";
    /**
     * 热销
     */
    String LKT_SPLX_002 = "2";
    /**
     * 推荐
     */
    String LKT_SPLX_003 = "3";


    /**
     * 来源
     */
    interface StoreSource
    {
        /**
         * 小程序
         */
        String LKT_LY_001 = "1";
        /**
         * H5
         */
        String LKT_LY_002 = "2";
        /**
         * 支付宝小程序
         */
        String LKT_LY_003 = "3";
        /**
         * 字节跳动小程序
         */
        String LKT_LY_004 = "4";
        /**
         * 百度小程序
         */
        String LKT_LY_005 = "5";
        /**
         * pc端
         */
        String LKT_LY_006 = "6";
        /**
         * App
         */
        String LKT_LY_011 = "11";

        /**
         * 代客下单
         */
        String LKT_LY_013 = "13";
    }

    /**
     * 引导图类型
     */
    interface GuideType
    {
        /**
         * 启动引导图
         */
        Integer GUIDETYPE_00  = 1;
        /**
         * 安装引导图
         */
        Integer GUIDETYPE_001 = 0;
    }

    /**
     * 是否默认
     */
    interface DefaultMaven
    {
        /**
         * 默认
         */
        int DEFAULT_OK = 1;
        /**
         * 非默认
         */
        int DEFAULT_NO = 0;
    }

    /**
     * 1是0否
     */
    interface WhetherMaven
    {
        /**
         * 是
         */
        int WHETHER_OK = 1;
        /**
         * 否
         */
        int WHETHER_NO = 0;
    }

    /**
     * 订单类型
     * 用订单头部来区分订单类型,则需要按头部长度大小，从大到小判断
     */
    interface OrdersType
    {

        /**
         * 普通订单
         */
        String ORDERS_HEADER_GM = "GM";

        /**
         * 砍价头部
         */
        String ORDERS_HEADER_KJ = "KJ";

        /**
         * 拼团订单
         */
        String ORDERS_HEADER_PT = "PT";

        /**
         * 预售订单
         */
        String ORDERS_HEADER_PS = "PS";

        /**
         * 开团订单
         */
        String ORDERS_HEADER_KT = "KT";

        /**
         * 分销订单
         */
        String ORDERS_HEADER_FX = "FX";

        /**
         * 竞拍
         */
        String ORDERS_HEADER_JP = "JP";

        /**
         * 特惠
         */
        String ORDERS_HEADER_TH = "TH";

        /**
         * 秒杀
         */
        String ORDERS_HEADER_MS = "MS";

        /**
         * 竞拍
         */
        String ORDERS_HEADER_AC = "AC";
        /**
         * 竞拍保证金
         */
        String ORDERS_HEADER_JB = "JB";

        /**
         * 充值
         */
        String ORDERS_HEADER_CZ = "CZ";

        /**
         * 积分商城
         */
        String ORDERS_HEADER_IN = "IN";

        /**
         * vip订单
         */
        String ORDERS_HEADER_DJ = "DJ";

        /**
         * 平台拼团订单
         */
        String PTHD_ORDER_PP    = "PP";
        /**
         * 平台秒杀订单
         */
        String PTHD_ORDER_PM    = "PM";
        /**
         * 虚拟订单
         */
        String ORDERS_HEADER_VI = "VI";

        /**
         * 限时折扣订单
         */
        String ORDERS_HEADER_FS = "FS";

        /**
         * 店铺保证金 - 临时表+保证金表
         */
        String ORDERS_HEADER_MCH_PROMISE = "PR";

        /**
         * 平台标识
         */
        String PTHD_ORDER_HEADER      = "pthd_";
        /**
         * 会员赠送
         */
        String ORDERS_HEADER_VIP_GIVE = "vipzs";

        /**
         * 直播
         */
        String ORDERS_HEADER_ZB = "ZB";
    }

    /**
     * 任务类型
     */
    interface TaskType
    {
        String TASK = "_TASK";
    }

    /**
     * 支付类型
     */
    interface OrderPayType
    {
        /**
         * 优惠卷
         */
        String ORDERPAYTYPE_CONSUMER_PAY = "consumer_pay";

        /**
         * 头条支付宝APP支付
         */
        String ORDERPAYTYPE_TT_ALIPAY = "tt_alipay";

        /**
         * 中国银联手机支付
         */
        String ORDERPAYTYPE_WAP_UNIONPAY   = "wap_unionpay";
        /**
         * 微信APP支付
         */
        String ORDERPAYTYPE_APP_WECHAT     = "app_wechat";
        /**
         * 微信小程序支付
         */
        String ORDERPAYTYPE_MINI_WECHAT    = "mini_wechat";
        /**
         * 微信公众号支付
         */
        String ORDERPAYTYPE_JSAPI_WECHAT   = "jsapi_wechat";
        /**
         * 支付宝小程序
         */
        String ORDERPAYTYPE_ALIPAY_MINIPAY = "alipay_minipay";
        /**
         * 支付宝WAP
         */
        String ORDERPAYTYPE_ALIPAY_WAP     = "alipay_mobile";
        /**
         * 微信二维码支付
         */
        String ORDERPAYTYPE_PC_WECHAT      = "pc_wechat";
        /**
         * 支付宝二维码支付
         */
        String ORDERPAYTYPE_PC_ALIPAY      = "pc_alipay";
        /**
         * 手机H5微信支付
         */
        String ORDERPAYTYPE_H5_WECHAT      = "H5_wechat";
        /**
         * 支付宝手机支付
         */
        String ORDERPAYTYPE_ALIPAY         = "alipay";

        /**
         * 支付宝手机支付
         */
        String ALIPAY_MOBILE_PHP = "alipay_php";

        /**
         * TODO 支付宝手机支付_临时
         */
        String ORDERPAYTYPE_ALIPAY_TMP = "aliPay";

        /**
         * 贝宝支付
         */
        String PAYPAL_PAY = "paypal";

        /**
         * Stripe支付
         */
        String STRIPE_PAY = "stripe";

        /**
         * 百度小程序支付
         */
        String ORDERPAYTYPE_BAIDU_PAY = "baidu_pay";

        /**
         * 钱包支付
         */
        String ORDERPAYTYPE_WALLET_PAY = "wallet_pay";

        /**
         * 线下支付
         */
        String OFFLINE_PAY = "offline_pay";
    }

    /**
     * 订单状态
     */
    interface OrdersStatus
    {
        /**
         * 待付款
         */
        int ORDERS_R_STATUS_UNPAID      = 0;
        /**
         * 待发货
         */
        int ORDERS_R_STATUS_CONSIGNMENT = 1;
        /**
         * 待收货
         */
        int ORDERS_R_STATUS_DISPATCHED  = 2;

        /**
         * 已完成
         */
        int ORDERS_R_STATUS_COMPLETE     = 5;
        /**
         * 订单关闭
         */
        int ORDERS_R_STATUS_CLOSE        = 7;
        /**
         * 待核销
         */
        int ORDERS_R_STATUS_TOBEVERIFIED = 8;
    }

    /**
     * 通用菜单归类
     */
    interface MenuType
    {
        /**
         * 门店
         */
        int SHOP  = 1;
        /**
         * 店铺
         */
        int MCH   = 2;
        /**
         * 商城
         */
        int STORE = 3;
    }

    /**
     * 通用 权限id类型
     */
    interface AuthorityType
    {
        /**
         * USER ID
         */
        int USERID   = 1;
        /**
         * 多商户管理后台 admin id
         */
        int ADMIN_ID = 2;
    }

    /**
     * 阿里接口错误代码
     */
    interface AliApiCode
    {
        /**
         * 卖家余额不足
         */
        String ALIPAY_ACQ_SELLER_BALANCE_NOT_ENOUGH = "ACQ.SELLER_BALANCE_NOT_ENOUGH";
    }

    /**
     * 售后类型 - re_type
     * 退款类型 1:退货退款  2:仅退款 3:换货
     */
    interface ReturnRecordReType
    {
        /**
         * 退货退款
         */
        Integer RETURNORDERSTATUS_REFUSE_REBACKGOODS_AMT = 1;
        /**
         * 仅退款
         */
        Integer RETURNORDERTYPE_REFUSE_AMT               = 2;
        /**
         * 换货
         */
        Integer RETURNORDERSTATUS_GOODS_REBACK           = 3;
    }


    /**
     * 售后订单状态 r_type
     * 100:不在退货退款状态
     * 0:审核中 1:同意并让用户寄回 2:拒绝(退货退款) 3:用户已快递 4:收到寄回商品,同意并退款
     * 5：拒绝并退回商品 8:拒绝(退款) 9:同意并退款 10:拒绝(售后)11:同意并且寄回商品 12售后结束 16供应商确认商品不影响二次销售并且同意
     */
    interface ReturnOrderStatus
    {
        /**
         * 审核中
         */
        Integer RETURNORDERSTATUS_EXAMEWAIT_STATUS       = 0;
        /**
         * 同意并让用户寄回
         */
        Integer RETURNORDERSTATUS_AGREE_REBACK           = 1;
        /**
         * 拒绝 退货退款(没有收到回寄商品)
         */
        Integer RETURNORDERSTATUS_REFUSE_REBACKGOODS_AMT = 2;
        /**
         * 用户已快递
         */
        Integer RETURNORDERSTATUS_USER_DELIVERED         = 3;
        /**
         * 收到寄回商品 同意并退款
         */
        Integer RETURNORDERSTATUS_RECEIVED_REBAKGOODS    = 4;
        /**
         * 拒绝并退回商品(拒绝回寄的商品 这个时候需要人工介入)
         */
        Integer RETURNORDERSTATUS_REFUSE_REBACKGOODS     = 5;
        /**
         * 拒绝 退款
         */
        Integer RETURNORDERSTATUS_REFUSE_AMT             = 8;
        /**
         * 同意并退款
         */
        Integer RETURNORDERSTATUS_AGREE_REBACK_AMT       = 9;
        /**
         * 拒绝 售后(用户还未寄回商品)
         */
        Integer RETURNORDERSTATUS_REFUSE_AFTER_SALE      = 10;
        /**
         * 同意并且寄回商品
         */
        Integer RETURNORDERSTATUS_AGREE_REBACK_GOODS     = 11;
        /**
         * 售后结束
         */
        Integer RETURNORDERSTATUS_AFTER_SALE_END         = 12;
        /**
         * 最终状态-人工审核成功
         */
        Integer RETURNORDERSTATUS_REFUSE_ARTIFICIAL_OK   = 13;

        /**
         * 订单未发货-系统自动急速退款
         */
        Integer RETURNORDERSTATUS_QUICK_REFUND = 15;

        /**
         * 供应商确认商品不影响二次销售并且同意
         */
        Integer RETURNORDERSTATUS_QUICK_SUPPLIER_AGRESS = 16;
    }

    /**
     * 类型 0:登录/退出 1:添加 2:修改 3:删除 4:导出
     * 5:启用/禁用 6:通过/拒绝 10删除订单
     */
    interface adminRecordType
    {
        /**
         * 登录/退出
         */
        int ADMINRECORDTYPE_OUT_OR_IN      = 0;
        /**
         * 添加
         */
        int ADMINRECORDTYPE_ADD_DATA       = 1;
        /**
         * 修改
         */
        int ADMINRECORDTYPE_UPDATE_DATA    = 2;
        /**
         * 删除
         */
        int ADMINRECORDTYPE_DEL_DATA       = 3;
        /**
         * 导出
         */
        int ADMINRECORDTYPE_OUT_DATA       = 4;
        /**
         * 启用/禁用
         */
        int ADMINRECORDTYPE_OPEN_OR_STOP   = 5;
        /**
         * 通过/拒绝
         */
        int ADMINRECORDTYPE_PASS_OR_REFUSE = 6;
        /**
         * 删除订单
         */
        int ADMINRECORDTYPE_DEL_ORDERNO    = 7;
    }

    /**
     * 审核状态
     */
    interface ExameStatus
    {
        /**
         * 审核等待状态
         */
        String EXAME_WAIT_STATUS     = "0";
        /**
         * 审核通过状态
         */
        String EXAME_PASS_STATUS     = "1";
        /**
         * 审核未通过状态
         */
        String EXAME_NOT_PASS_STATUS = "2";
    }

    /**
     * 商品活动类型
     * [lkt_product_list.active 现在商品只有普通商品，其它商品都做成插件了]
     */
    interface GoodsActive
    {
        /**
         * 正价商品
         */
        Integer GOODSACTIVE_POSITIVE_PRICE = 1;
        /**
         * 支持拼团
         */
        Integer GOODSACTIVE_SUPPORT_PT     = 2;
        /**
         * 支持砍价
         */
        Integer GOODSACTIVE_POSITIVE_KJ    = 3;
        /**
         * 支持竞拍
         */
        Integer GOODSACTIVE_POSITIVE_JP    = 4;
        /**
         * 会员特惠
         */
        Integer GOODSACTIVE_VIP_DISCOUNT   = 6;
        /**
         * 积分
         */
        Integer GOODSACTIVE_INTEGRAL       = 7;
        /**
         * 秒杀
         */
        Integer GOODSACTIVE_SECONDS        = 8;
        /**
         * 满减
         */
        Integer SUBTRACTION_SECONDS        = 10;
        /**
         * 分销
         */
        Integer GOODSACTIVE_DISTRIBUTION   = 11;
        /**
         * 限时折扣 ： TODO
         */
        Integer GOODSACTIVE_FLASHSAL       = 11;

        /**
         * 直播
         */
        Integer GOODSACTIVE_LIVING = 12;
    }

    /**
     * 商品展示位
     */
    interface GoodsShowAdr
    {
        /**
         * 全部商品
         */
        Integer GOODSSHOWADR_DEFAULT = 0;
        /**
         * 首页
         */
        Integer GOODSSHOWADR_INDEX   = 1;

        /**
         * 购物车
         */
        Integer GOODSSHOWADR_CART = 2;

        /**
         * 分类
         */
        Integer GOODSSHOWADR_CATEGORIES = 3;

        /**
         * 我的-推荐
         */
        Integer GOODSSHOWADR_MY = 4;
    }

    /**
     * 商品审核状态
     * 1.待审核，2.审核通过，3.审核不通过，4.暂不审核
     */
    interface GoodsMchExameStatus
    {
        /**
         * 审核待提交状态
         */
        Integer EXAME_SUBMITTED       = 0;
        /**
         * 审核等待状态
         */
        Integer EXAME_WAIT_STATUS     = 1;
        /**
         * 审核通过状态
         */
        Integer EXAME_PASS_STATUS     = 2;
        /**
         * 审核未通过状态
         */
        Integer EXAME_NOT_PASS_STATUS = 3;
        /**
         * 暂不审核
         */
        Integer EXAME_STOP_STATUS     = 4;
    }

    /**
     * 店铺审核状态
     */
    interface MchExameStatus
    {
        /**
         * 审核等待状态
         */
        Integer EXAME_WAIT_STATUS     = 0;
        /**
         * 审核通过状态
         */
        Integer EXAME_PASS_STATUS     = 1;
        /**
         * 审核未通过状态
         */
        Integer EXAME_NOT_PASS_STATUS = 2;
    }

    interface UserMchApply
    {
        /**
         * 未申请
         */
        Integer NOT_APPLIED  = 0;
        /**
         * 审核中
         */
        Integer UNDER_REVIEW = 1;
        /**
         * 审核通过
         */
        Integer ADOPT        = 2;
        /**
         * 审核不通过
         */
        Integer FAIL         = 3;
    }


    /**
     * 结算方式
     *
     * @author Trick
     * @date 2020/11/3 14:16
     */
    interface MchConfigSettlement
    {
        String SETTLEMENT_TYPE0 = "0";
        String SETTLEMENT_TYPE1 = "1";
        String SETTLEMENT_TYPE2 = "2";
    }

    /**
     * 收藏类型
     */
    interface UserCollectionType
    {
        /**
         * 普通收藏
         */
        Integer COLLECTIONTYPE1 = 1;
        /**
         * 积分商城收藏
         */
        Integer COLLECTIONTYPE2 = 2;
    }

    /**
     * 活动状态
     */
    interface ProductStatus
    {
        /**
         * 活动未开启
         */
        Integer NOT_OPEN_STATUS              = 0;
        /**
         * 活动中
         */
        Integer PRODUCTSTATUS_SUCCESS_STATUS = 1;
        /**
         * 活动结束
         */
        Integer PRODUCTSTATUS_END_STATUS     = 2;
        /**
         * 流拍
         */
        Integer UNSOLD_STATUS                = 3;
    }

    /**
     * 回收站 0.不回收 1.回收
     */
    interface ProductRecycle
    {
        /**
         * 不回收
         */
        Integer NOT_STATUS    = 0;
        /**
         * 回收-系统回收
         */
        Integer RECOVERY      = 1;
        /**
         * 回收-用户回收
         */
        Integer RECOVERY_USER = 2;
        /**
         * 回收-店铺回收
         */
        Integer RECOVERY_MCH  = 3;
    }

    /**
     * 商品状态
     */
    interface GoodsStatus
    {
        /**
         * 待上架
         */
        Integer NOT_GROUNDING = 1;

        /**
         * 已上架
         */
        Integer NEW_GROUNDING = 2;

        /**
         * 已下架
         */
        Integer OFFLINE_GROUNDING = 3;

        /**
         * 违规下架
         */
        Integer VIOLATION = 4;

        /**
         * 断供
         */
        Integer UNABLE_TO_PAY = 5;

    }

    /**
     * 供应商商品状态
     */
    interface supplierGoodsStatus
    {
        /**
         * 断供
         */
        Integer UNABLE_TO_PAY = 0;

        /**
         * 已上架
         */
        Integer NEW_GROUNDING = 1;

    }

    /**
     * 消息状态
     */
    interface SystemMessageType
    {

        /**
         * 未读
         */
        Integer MESSAGE_READE_NO = 1;
        /**
         * 已读
         */
        Integer MESSAGE_READE_OK = 2;

    }

    /**
     * 秒杀插件 常量池
     */
    interface Seckill
    {
        String SECKILL_NOTICE = "秒杀预告";

        /**
         * 未开始
         */
        String SECKILL_STATUS_NOT_START = "未开始";
        /**
         * 进行中
         */
        String SECKILL_STATUS_START     = "进行中";
        /**
         * 秒杀结束
         */
        String SECKILL_STATUS_END       = "秒杀结束";

    }

    /**
     * 产品值属性 1：新品,2：热销，3：推荐
     * 请使用
     * ProLabelModelMapper.getProLabelNew
     * ProLabelModelMapper.getProLabelTop
     * ProLabelModelMapper.getProLabelHot
     */
    interface GoodsStype
    {
        /**
         * 新品
         */
        Integer NEW_PRODUCT = 1;

        /**
         * 热销
         */
        Integer HOT_GROUNDING = 2;

        /**
         * 推荐
         */
        Integer TOP_GROUNDING = 3;

    }

    /**
     * 收支状态
     * lkt_mch_account_log.status
     */
    interface MchAccountLogStatus
    {
        /**
         * 收入
         */
        Integer MCHACCOUNTLOG_STATUS_INCOME = 1;

        /**
         * 支出
         */
        Integer MCHACCOUNTLOG_STATUS_EXPENDITURE = 2;
    }

    /**
     * 收支状态
     * 类型：1.订单 2.退款 3.提现
     * lkt_mch_account_log.type
     */
    interface MchAccountLogType
    {
        /**
         * 订单
         */
        Integer MCHACCOUNTLOG_TYPE_ORDER = 1;

        /**
         * 退款
         */
        Integer MCHACCOUNTLOG_TYPE_REFUND = 2;

        /**
         * 提现
         */
        Integer MCHACCOUNTLOG_TYPE_WITHDRAWAL = 3;

        /**
         * 保证金
         */
        Integer MCHACCOUNTLOG_TYPE_PROMISE = 4;

        /**
         * 供应商
         */
        Integer MCHACCOUNTLOG_TYPE_SUPPLIER = 5;

        /**
         * 拼团佣金
         */
        Integer MCHACCOUNTLOG_TYPE_GROUP = 6;
    }

    /**
     * 库存记录类型
     * 请使用 StockModel.StockType
     */
    @Deprecated
    interface StockType
    {
        /**
         * 入库
         */
        Integer STOCKTYPE_WAREHOUSING = 0;

        /**
         * 出库
         */
        Integer AGREEMENTTYPE_WAREHOUSING_OUT = 1;

        /**
         * 预警
         */
        Integer AGREEMENTTYPE_WAREHOUSING_WARNING = 2;
    }

    /**
     * 属性类型
     * 请使用
     * SkuModel.SKU_TYPE_ATTRIBUTE_NAME
     * SkuModel.SKU_TYPE_ATTRIBUTE_VALUE
     */
    @Deprecated
    interface SkuType
    {
        /**
         * 属性名称
         */
        Integer SKUTYPE_NAME = 1;

        /**
         * 属性值
         */
        Integer SKUTYPE_VALUE = 2;

    }

    /**
     * 地理位置
     */
    interface Position
    {
        /**
         * 省
         */
        Integer LEVEL_2 = 2;

        /**
         * 市
         */
        Integer LEVEL_3 = 3;

        /**
         * 县/区
         */
        Integer LEVEL_4 = 4;
    }

    /**
     * 协议类型
     */
    interface AgreementType
    {
        /**
         * 注册协议
         */
        Integer AGREEMENTTYPE_REGISTER = 0;

        /**
         * 店铺协议
         */
        Integer AGREEMENTTYPE_MCH = 1;

        /**
         * 隐私协议
         */
        Integer AGREEMENTTYPE_PRIVACY = 2;

        /**
         * 隐私协议
         */
        Integer AGREEMENTTYPE_MEMBER = 3;
    }

    /**
     * 售后类型
     */
    interface ServiceAddressType
    {
        /**
         * 发货地址
         */
        Integer SERVICEADDRESSTYPE_DELIVER   = 1;
        /**
         * 售后地址
         */
        Integer ServiceAddressType_AfterSale = 2;
    }

    /**
     * 字典目录
     */
    interface DicName
    {
        /**
         * 商品活动类型
         */
        String DIC_GOODS_ACTIVE = "商品活动类型";
    }

    /**
     * 插件
     */
    interface Plugin
    {
        /**
         * 优惠卷
         */
        String COUPON             = "coupon";
        /**
         * 店铺
         */
        String MCH                = "mch";
        /**
         * 会员
         */
        String MEMBER             = "member";
        /**
         * 平台活动
         */
        String PLATFORMACTIVITIES = "platform_activities";
        /**
         * 秒杀
         */
        String SECONDS            = "seconds";
        /**
         * 预售
         */
        String PRESELL            = "presell";
        /**
         * 签到
         */
        String SIGN               = "sign";
        /**
         * 拼团
         */
        String GOGROUP            = "go_group";
        /**
         * 砍价
         */
        String BARGAIN            = "bargain";
        /**
         * 分销
         */
        String DISTRIBUTION       = "distribution";
        /**
         * 积分商城
         */
        String INTEGRAL           = "integral";
        /**
         * 满减
         */
        String SUBTRACTION        = "subtraction";
        /**
         * diy
         */
        String DIY                = "diy";
        /**
         * 竞拍
         */
        String AUCTION            = "auction";

        /**
         * 钱包
         */
        String WALLET    = "wallet";
        /**
         * 限时折扣
         */
        String FLASHSALE = "flashsale";


        /**
         * 直播
         */
        String LIVING = "living";

        /**
         * 种草
         */
        String ZC = "zc";


    }

    /**
     * 积分类型
     * 类型: 0:签到 1:消费 2:首次关注得积分 3:转积分给好友 4:好友转积分 5:系统扣除 6:系统充值 7:抽奖 8:会员购物积分 9:分销升级奖励积分 10:积分过期
     */
    interface IntegralType
    {
        /**
         * 签到
         */
        Integer SIGN                   = 0;
        /**
         * 消费
         */
        Integer CONSUMPTION            = 1;
        /**
         * 首次关注得积分
         */
        Integer FIRST_CONCERN          = 2;
        /**
         * 转积分给好友
         */
        Integer TRANSFER_FRIEND        = 3;
        /**
         * 好友转积分
         */
        Integer FRIEND_TRANSFER        = 4;
        /**
         * 系统扣除
         */
        Integer SYSTEM_DEDUCTION       = 5;
        /**
         * 系统充值
         */
        Integer SYSTEM_RECHARGE        = 6;
        /**
         * 抽奖
         */
        Integer LUCK_DRAW              = 7;
        /**
         * 会员购物积分
         */
        Integer SHOP_INTEGRAL          = 8;
        /**
         * 分销升级奖励积分
         */
        Integer DISTRIBUTION_BONUS     = 9;
        /**
         * 积分过期
         */
        Integer OVERDUE_INTEGRAL       = 10;
        /**
         * 开通会员
         */
        Integer OPEN_MEMBER            = 11;
        /**
         * 会员生日特权
         */
        Integer MEMBER_BIRTHDAY        = 12;
        /**
         * 冻结积分
         */
        Integer INTEGRAL_FROZEN        = 13;
        /**
         * 售后退回
         */
        Integer INTEGRAL_REFUND_RETURN = 14;
    }


    /**
     * 菜单相关
     */
    interface MenuCore
    {
        /**
         * 平台顶级菜单id
         */
        Integer MENU_CORE_PT_ID = 1;
    }

    /**
     * 会员开通方式
     */
    interface MemberType
    {
        /**
         * 月卡
         */
        Integer MONTH         = 1;
        String  MONTH_STRING  = "月卡";
        /**
         * 季卡
         */
        Integer SEASON        = 2;
        String  SEASON_STRING = "季卡";
        /**
         * 年卡
         */
        Integer YEAR          = 3;
        String  YEAR_STRING   = "年卡";
    }

    /**
     * 订单关闭方式
     */
    interface CancelMethod
    {
        /**
         * 自动取消
         */
        Integer AUTO = 0;

        /**
         * 手动取消
         */
        Integer SELF = 1;

        /**
         * 退款取消
         */
        Integer REFUND = 2;

    }

}
