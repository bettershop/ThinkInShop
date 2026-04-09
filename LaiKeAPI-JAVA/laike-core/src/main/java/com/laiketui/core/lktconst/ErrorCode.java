package com.laiketui.core.lktconst;

/**
 * 三个 ErrorCode 合并 - 20240611 - vvx
 */
public interface ErrorCode
{

    /**
     * 系统级错误
     */
    interface SysErrorCode
    {
        // 1-99
        /**
         * 图片上传失败
         */
        String UPLOAD_FAIL     = "1";
        /**
         * 短信发送失败
         */
        String SEND_SMS_FAIL   = "10";
        /**
         * redis 保存失败
         */
        String SAVE_REDIS_FAIL = "11";

        /**
         * 方法不存在
         */
        String FUNCTION_NOT_EXISTS = "12";
        /**
         * 方法错误
         */
        String FUNCTION_ERROR      = "13";

        /**
         * 自定义异常信息code
         */
        String ALL_CODE = "00000";
    }

    /**
     * 业务级错误
     */
    interface BizErrorCode
    {
        // 100 - 9999

        /**
         * 库存不足
         */
        String INSUFFICIENT_STOCK = "105";

        /**
         * 参数错误
         */
        String PARAMETER_VAL_ERROR = "109";

        /**
         * 商品已下架
         */
        String OFF_SHELF = "0105";

        /**
         * 业务异常
         */
        String ABNORMAL_BIZ = "110";

        /**
         * 非法入侵
         */
        String ILLEGAL_INVASION = "115";

        /**
         * 注册失败
         */
        String REGISTER_FAIL        = "199";
        /**
         * 注册失败,userid生成失败
         */
        String REGISTER_USERID_FAIL = "201";

        /**
         * 已经发货
         */
        String ORDER_DELIVERYED = "202";

        /**
         * 未登录
         */
        String LOGIN_INVALID         = "203";
        /**
         * 登录已过期
         */
        String LOGIN_INVALID_OVERDUE = "204";

        /**
         * 已经提醒过了,请稍后再试！
         */
        String ORDER_REMIND_ALREADY = "215";

        /**
         * 提醒发货失败
         */
        String ORDER_REMIND_FAILED = "216";

        /**
         * 账号已被注册
         */
        String REGISTER_ZH_FAIL    = "300";
        /**
         * 手机号已被注册
         */
        String REGISTER_PHONE_FIAL = "301";
        /**
         * 验证码错误
         */
        String PCODE_FIAL          = "302";
        /**
         * 验证码不存在或失效
         */
        String PCODE_INVALID       = "303";

        /**
         * 密码不正确
         */
        String LOGIN_PWD_FAIL             = "304";
        /**
         * 用户名或密码不正确
         */
        String LOGIN_NAME_FAIL            = "305";
        /**
         * 参数异常
         */
        String PARAMATER_ERROR            = "306";
        /**
         * 短信模板不存在
         */
        String SMS_TEMPLATE_NOT_EXIST     = "307";
        /**
         * 验证码未失效
         */
        String REGISTER_PCODE_NOT_INVALID = "308";
        /**
         * 未注册
         */
        String LOGIN_NOT_REGISTER         = "309";
        /**
         * 出生日期只能修改一次
         */
        String BIRTHDAY_LIMIT_ONE         = "310";
        /**
         * 用户名格式不正确
         */
        String USERNAME_NOT_CHECK         = "311";
        /**
         * 用户名格式不正确
         */
        String MIMA_NOT_CHECK             = "312";
        /**
         * 支付密码格式不正确
         */
        String PAY_PASSWORD_NOT_CHECK     = "313";
        /**
         * 账号 格式不正确
         */
        String ZHANGHAO_NOT_CHECK         = "314";
        /**
         * 手机号 格式不正确
         */
        String PHONE_NOT_CHECK            = "315";
        /**
         * 出生日期 格式不正确
         */
        String BIRTHDAY_NOT_CHECK         = "316";
        /**
         * 操作失败
         */
        String OPERATION_FAILED           = "317";

        /**
         * 未选择图片
         */
        String IMAGE_NOT_CHECKED    = "320";
        /**
         * 图片文件类型为空
         */
        String IMAGE_TYPE_NULL      = "321";
        /**
         * 图片文件类型有误
         */
        String IMAGE_TYPE_NOT_CHECK = "322";
        /**
         * 图片名不能为空
         */
        String IMAGE_NAME_NOT_NULL  = "323";

        /**
         * 店铺名称 格式不正确
         */
        String MCH_NAME_NOT_CHECK    = "324";
        /**
         * 店铺信息 格式不正确
         */
        String MCH_INFO_NOT_CHECK    = "325";
        /**
         * 店铺经营范围 格式不正确
         */
        String MCH_RANGE_NOT_CHECK   = "326";
        /**
         * 身份证 格式不正确
         */
        String MCH_IDCARD_NOT_CHECK  = "327";
        /**
         * 联系电话 格式不正确
         */
        String MCH_TEL_NOT_CHECK     = "328";
        /**
         * 联系地址 格式不正确
         */
        String MCH_ADDRESS_NOT_CHECK = "329";

        /**
         * 用户未设置密码
         */
        String USER_NOT_CONFIG_PWD = "330";

        /**
         * 用户不存在
         */
        String USER_NOT_EXIST        = "331";
        /**
         * 数据未找到
         */
        String DATA_NOT_EXIST        = "332";
        /**
         * 库存不足
         */
        String STOCK_INSUFFICIENT    = "333";
        /**
         * 商品未上架
         */
        String GOODS_OFF_SHELF       = "334";
        /**
         * 商品数据不完整
         */
        String GOODS_DATA_INCOMPLETE = "335";
        /**
         * 数据处理失败
         */
        String DATA_HANDLE_FAIL      = "336";
        /**
         * 数据错误
         */
        String DATA_ERROR            = "337";
        /**
         * 配置不存在
         */
        String DATA_NOT_CONFIG       = "338";

        /**
         * 登陆失败
         */
        String LOGIN_FAIL = "400";

        /**
         * 微信AccessToken获取失败
         */
        String GET_ACCESSTOKEN_FAIL = "401";

        /**
         * 微信Ticket获取失败
         */
        String GET_TICKET_FAIL                      = "402";
        /**
         * 数据非法
         */
        String DATA_ILLEGAL_FAIL                    = "412";
        /**
         * 数据已存在
         */
        String DATA_ALREADY_EXISTS                  = "413";
        /**
         * 权限不够
         */
        String INSUFFICIENT_PRIVILEGE               = "414";
        /**
         * 密码未变化
         */
        String USER_PASSWORD_REPEAT                 = "415";
        /**
         * 密码重试次数过多
         */
        String USER_PASSWORD_OVER                   = "416";
        /**
         * 提现金额小于最低提现金额
         */
        String WITHDRAWAL_LESS_THAN_AMT             = "417";
        /**
         * 提现金额大于最大提现金额
         */
        String WITHDRAWAL_GREATER_THAN_AMT          = "418";
        /**
         * 提现金额大于店主金额
         */
        String WITHDRAWAL_GREATER_THAN_CASHABLE_AMT = "419";
        /**
         * 交易还未审核
         */
        String WITHDRAWAL_APPLY_NOT_REVIEWED        = "420";
        /**
         * 交易还未审核
         */
        String WITHDRAWAL_EXCEED_UPPER_LIMIT        = "421";
        /**
         * 持卡人不正确
         */
        String BANK_CARDHOLDER_ERROR                = "422";
        /**
         * 卡号不正确
         */
        String BANKNO_ERROR                         = "423";
        /**
         * 卡号与银行不匹配
         */
        String BANKNO_NAME_ERROR                    = "424";
        /**
         * 请设置分佣
         */
        String PLEASE_SET_THE_FREIGHT_RATE_FIRST    = "425";

        /**
         * 优惠券筛选失败
         */
        String COUPON_SCREENING_ERROR = "426";

        /**
         * 店铺优惠券筛选失败
         */
        String COUPON_MCH_SCREENING_ERROR = "427";

        /**
         * 平台优惠券筛选失败
         */
        String COUPON_PLAFORM_SCREENING_ERROR = "428";

        /**
         * 会员优惠计算失败
         */
        String MEMBER_YOUHUI_ERROR = "429";

        /**
         * 订单运费计算失败
         */
        String ORDER_JS_FREIGHT_ERROR = "430";

        /**
         * 订单满减计算失败
         */
        String ORDER_SUBSTRACTION_JS_ERROR = "431";

        /**
         * 下单失败,请稍后再试
         */
        String ORDER_FAILED_TRY_AGAIN_LATER = "432";


        /**
         * 更新兑换券使用状态失败
         */
        String ORDER_FAILED_UPDATE_COUPON_STATUS = "433";

        /**
         * 添加优惠券关联订单数据失败
         */
        String ORDER_FAILED_ADD_COUPON_ASSOSIATED_ORDERINFO = "434";

        /**
         * 创建订单号失败
         */
        String ORDER_NUMBER_FAILED = "435";

        /**
         * 添加满减记录失败
         */
        String ORDER_FAILED_ADD_SUBTRACTION_RECORDER = "436";

        /**
         * 订单列表获取失败
         */
        String ORDER_LIST_FAILED = "437";

        /**
         * 优惠券拆分订单失败
         */
        String ORDER_COUPON_CF_ERROR = "438";

        /**
         * 金额错误
         */
        String AMOUNT_ERROR = "439";

        /**
         * 余额不足
         */
        String INSUFFICIENT_BALANCE = "440";

        /**
         * 订单数据错误
         */
        String ORDER_DATA_ERROR = "441";

        /**
         * 收货失败
         */
        String RECEIVING_FAILED = "500";

        /**
         * 网络繁忙
         */
        String BUSY_NETWORK         = "502";
        /**
         * 操作失败
         */
        String API_OPERATION_FAILED = "503";

        /**
         * 服务暂不可用
         */
        String API_TEMPORARILY_UNAVAILABLE = "504";

        /**
         * 未授权域名
         */
        String NO_LICENSE_DOMAIN = "504";

        /**
         * 未配置定位服务
         */
        String GPS_NOT_CONFIG           = "101";
        /**
         * 未获取到数据
         */
        String READ_NOT_DATA            = "102";
        /**
         * 请重新获取验证码
         */
        String READ_PHONE_CODE_NOT_DATA = "103";

        /**
         * 订单确认信息获取失败
         */
        String ORDER_SETTLEMENT_ERROR = "600";

        /**
         * 腾讯定位api错误
         */
        String TENGXUN_API_ERROR = "1001";

        /**
         * 下单失败
         */
        String PAYMENT_BUILDER_RQCODE_ERROR = "1002";
        /**
         * 退款失败
         */
        String PAYMENT_NOT_REFUND_ERROR     = "1101";

        /**
         * 支付密码不正确
         */
        String PAYMENT_PASSWORD_ERROR = "1102";

        /**
         * 支付失败
         */
        String PAYMENT_FAIL        = "1103";
        /**
         * 您已生成待付款订单，请前往支付！
         */
        String PLASE_GO_TO_PAYMENT = "1104";


        /**
         * 签到插件未启用
         */
        String PLUGIN_SIGIN_NOT_OPEN  = "3000";
        /**
         * 已经签到过了
         */
        String PLUGIN_DONE_DID_SIGIN  = "3001";
        /**
         * 签到活动还未开启
         */
        String PLUGIN_SIGN_NOT_START  = "3002";
        /**
         * 签到活动已经结束
         */
        String PLUGIN_SIGN_END        = "3003";
        /**
         * 签到失败
         */
        String PLUGIN_SIGN_ERROR      = "3004";
        /**
         * 优惠券插件未启用
         */
        String PLUGIN_COUPON_NOT_OPEN = "3010";


        /**
         * 秒杀-数据异常
         */
        String PLUGIN_SECONDS_DATA_ERROR      = "3100";
        /**
         * 秒杀-秒杀商品不存在
         */
        String PLUGIN_SECONDS_GOODS_NOT_EXIST = "3101";
        /**
         * 秒杀-库存不足
         */
        String PLUGIN_SECONDS_STOCK_OUT       = "3102";


        /**
         * 积分商城-积分商品不存在
         */
        String PLUGIN_INTEGRAL_GOODS_NOT_EXIST = "3200";

        /**
         * 积分订单下单失败
         */
        String PLUGIN_INTEGRAL_BUILDE_ORDER_ERROR         = "3201";
        /**
         * 下单失败-余额不足
         */
        String PLUGIN_INTEGRAL_BALANCE_UNABLE_TO_EXCHANGE = "3202";
        /**
         * 下单失败-积分不足
         */
        String PLUGIN_INTEGRAL_SCORE_UNABLE_TO_EXCHANGE   = "3203";
        /**
         * 下单失败-库存不足
         */
        String PLUGIN_INTEGRAL_STOCK_NOT_NUM              = "3204";


        /**
         * 砍价订单不存在
         */
        String PLUGIN_BARGAIN_ORDER_NOT_EXIST      = "3300";
        /**
         * 砍价商品不存在
         */
        String PLUGIN_BARGAIN_GOODS_NOT_EXIST      = "3301";
        /**
         * 砍价商品属性不存在
         */
        String PLUGIN_BARGAIN_GOODS_ATTR_NOT_EXIST = "3302";
        /**
         * 砍价商品库存不足
         */
        String PLUGIN_BARGAIN_GOODS_NOT_STOCK      = "3303";
        /**
         * 砍价失败
         */
        String PLUGIN_BARGAIN_PRICE_ERROR          = "3304";

        /**
         * 您的店铺账户尚有进行中订单,无法退还保证金
         */
        String MCH_RETURN_PROMISE_PRICE_ERROR = "3401";

        /**
         * EXCEL文件导出失败
         */
        String FILE_EXCEL_ERROR      = "4001";
        /**
         * EXCEL文件解析失败
         */
        String FILE_EXCEL_READ_ERROR = "4002";
        /**
         * EXCEL文件不存在
         */
        String FILE_EXCEL_NOT_EXISTS = "4004";


        /**
         * 上级菜单不存在
         */
        String ERROR_CODE_SJCDBCZ         = "50001";
        /**
         * 网络异常
         */
        String ERROR_CODE_WLYC            = "50002";
        /**
         * 菜单不存在
         */
        String ERROR_CODE_CDBCZ           = "50003";
        /**
         * 请选择
         */
        String ERROR_CODE_QXZ             = "50004";
        /**
         * 请选择选中
         */
        String ERROR_CODE_QXZXZ           = "50005";
        /**
         * 菜单名称不能为空
         */
        String ERROR_CODE_CDMCBNWK        = "50006";
        /**
         * 菜单已存在
         */
        String ERROR_CODE_CDYCZ           = "50007";
        /**
         * 菜单上级不能是当前菜单
         */
        String ERROR_CODE_CDSJBNSDQCD     = "50008";
        /**
         * 操作失败
         */
        String ERROR_CODE_CZSB            = "50009";
        /**
         * 参数错误
         */
        String ERROR_CODE_CSCW            = "50010";
        /**
         * 角色不存在
         */
        String ERROR_CODE_JSBCZ           = "50011";
        /**
         * 角色名称不能为空
         */
        String ERROR_CODE_JSMCBNWK        = "50012";
        /**
         * 角色描述不能为空
         */
        String ERROR_CODE_JSMSBNWK        = "50013";
        /**
         * 请选择权限
         */
        String ERROR_CODE_QXZQX           = "50014";
        /**
         * 角色已存在
         */
        String ERROR_CODE_JSYCZ           = "50015";
        /**
         * 权限不足
         */
        String ERROR_CODE_QXBZ            = "50016";
        /**
         * 请选择用户
         */
        String ERROR_CODE_QXZYH           = "50017";
        /**
         * 请选择角色
         */
        String ERROR_CODE_QXZJS           = "50018";
        /**
         * 用户不存在
         */
        String ERROR_CODE_YHBCZ           = "50019";
        /**
         * 该用户未绑定该角色
         */
        String ERROR_CODE_GYHWBDGJS       = "50020";
        /**
         * 不能为空
         */
        String ERROR_CODE_BNWK            = "50021";
        /**
         * 添加失败
         */
        String ERROR_CODE_TJSB            = "50022";
        /**
         * 修改失败
         */
        String ERROR_CODE_XGSB            = "50023";
        /**
         * 删除失败
         */
        String ERROR_CODE_SCSB            = "50024";
        /**
         * 请选择至少一位会员
         */
        String ERROR_CODE_QXZZSYWHY       = "50025";
        /**
         * 优惠卷赠送失败
         */
        String ERROR_CODE_YHJZSSB         = "50026";
        /**
         * 店铺不存在
         */
        String ERROR_CODE_DPBCZ           = "50027";
        /**
         * 提现失败请稍后再试
         */
        String ERROR_CODE_TXSBQSHZS       = "50028";
        /**
         * 短信发送失败
         */
        String ERROR_CODE_DXFSSB          = "50029";
        /**
         * 银行卡不存在
         */
        String ERROR_CODE_YXKBCZ          = "50030";
        /**
         * 至少保留一个默认银行卡
         */
        String ERROR_CODE_ZSBLYGMRYXK     = "50031";
        /**
         * 网络繁忙请稍后再试
         */
        String ERROR_CODE_WLFMQSHZS       = "50032";
        /**
         * 默认银行卡无法修改
         */
        String ERROR_CODE_MRYXKWFXG       = "50033";
        /**
         * 银行卡已存在
         */
        String ERROR_CODE_YXKYCZ          = "50034";
        /**
         * 银行卡格式错误
         */
        String ERROR_CODE_YXKGSCW         = "50035";
        /**
         * 银行卡信息不匹配
         */
        String ERROR_CODE_YXKXXBPP        = "50036";
        /**
         * 默认数据无法删除
         */
        String ERROR_CODE_MRSJWFSC        = "50037";
        /**
         * 运费添加失败
         */
        String ERROR_CODE_YFTJSB          = "50038";
        /**
         * 运费模板不存在
         */
        String ERROR_CODE_YFMBBCZ         = "50039";
        /**
         * 商品不存在
         */
        String ERROR_CODE_SPBCZ           = "50040";
        /**
         * 自营店不能自选
         */
        String ERROR_CODE_ZYDBNZX         = "50041";
        /**
         * 该商品非自选商品
         */
        String ERROR_CODE_GSPFZXSP        = "50042";
        /**
         * 商品添加失败
         */
        String ERROR_CODE_SPTJSB          = "50043";
        /**
         * 商品数据不完整
         */
        String ERROR_CODE_SPSJBWZ         = "50044";
        /**
         * 规格添加失败
         */
        String ERROR_CODE_GGTJSB          = "50045";
        /**
         * 数据格式错误
         */
        String ERROR_CODE_SJGSCW          = "50046";
        /**
         * 商品删除失败
         */
        String ERROR_CODE_SPSCSB          = "50047";
        /**
         * 请选择商品
         */
        String ERROR_CODE_QXZSP           = "50048";
        /**
         * 状态未变化
         */
        String ERROR_CODE_ZTWBH           = "50049";
        /**
         * 图片不能为空
         */
        String ERROR_CODE_TPBNWK          = "50050";
        /**
         * 新密码不能为空
         */
        String ERROR_CODE_XMMBNWK         = "50051";
        /**
         * 原密码不能为空
         */
        String ERROR_CODE_YMMBNWK         = "50052";
        /**
         * 原密码不正确
         */
        String ERROR_CODE_YMMBZQ          = "50053";
        /**
         * 店铺名称不能为空
         */
        String ERROR_CODE_DPMCBNWK        = "50054";
        /**
         * 联系电话不能为空
         */
        String ERROR_CODE_LXDHBNWK        = "50055";
        /**
         * 店铺信息不能为空
         */
        String ERROR_CODE_DPXXBNWK        = "50056";
        /**
         * 经营范围不能为空
         */
        String ERROR_CODE_JYFWBNWK        = "50057";
        /**
         * 联系地址不能为空
         */
        String ERROR_CODE_LXDZBNWK        = "50058";
        /**
         * 店铺名称已存在
         */
        String ERROR_CODE_DPMCYCZ         = "50059";
        /**
         * 轮播图不能为空
         */
        String ERROR_CODE_LBTBNWK         = "50060";
        /**
         * 轮播图跳转路径不能为空
         */
        String ERROR_CODE_LBTTZLJBNWK     = "50061";
        /**
         * 轮播图序号不能为空
         */
        String ERROR_CODE_LBTXHBNWK       = "50062";
        /**
         * 轮播图不存在
         */
        String ERROR_CODE_LBTBCZ          = "50063";
        /**
         * 账户或密码不匹配
         */
        String ERROR_CODE_ZHHMMBPP        = "50064";
        /**
         * 您还未设置密码
         */
        String ERROR_CODE_NHWSZMM         = "50065";
        /**
         * 登录失败用户不存在
         */
        String ERROR_CODE_DLSBYHBCZ       = "50066";
        /**
         * 登陆失败参数错误
         */
        String ERROR_CODE_DLSBCSCW        = "50067";
        /**
         * 手机号格式不正确
         */
        String ERROR_CODE_SJHGSBZQ        = "50068";
        /**
         * 验证码不正确
         */
        String ERROR_CODE_YZMBZQ          = "50069";
        /**
         * 请重新获取验证码
         */
        String ERROR_CODE_QZXHQYZM        = "50070";
        /**
         * 验证码获取失败
         */
        String ERROR_CODE_YZMHQSB         = "50071";
        /**
         * 请输入密码
         */
        String ERROR_CODE_QSRMM           = "50072";
        /**
         * 请再次确认密码
         */
        String ERROR_CODE_QZCQRMM         = "50073";
        /**
         * 请输入位数的新密码
         */
        String ERROR_CODE_QSRWSDXMM       = "50074";
        /**
         * 新密码与原密码不相同
         */
        String ERROR_CODE_XMMYYMMBXT      = "50075";
        /**
         * 请重新获取手机验证码
         */
        String ERROR_CODE_QZXHQSJYZM      = "50076";
        /**
         * 账户不存在
         */
        String ERROR_CODE_ZHBCZ           = "50077";
        /**
         * 密码修改失败
         */
        String ERROR_CODE_MMXGSB          = "50078";
        /**
         * 图形验证码不正确
         */
        String ERROR_CODE_TXYZMBZQ        = "50079";
        /**
         * 未开通店铺或者店铺已注销
         */
        String ERROR_CODE_WKTDPHZDPYZX    = "50080";
        /**
         * 记录不存在
         */
        String ERROR_CODE_JLBCZ           = "50081";
        /**
         * 订单编辑失败
         */
        String ERROR_CODE_DDBJSB          = "50082";
        /**
         * 订单不存在
         */
        String ERROR_CODE_DDBCZ           = "50083";
        /**
         * 订单已发货不能进行修改
         */
        String ERROR_CODE_DDYFHBNJXXG     = "50084";
        /**
         * 验证码错误
         */
        String ERROR_CODE_YZMCW           = "50085";
        /**
         * 取货码不存在
         */
        String ERROR_CODE_QHMBCZ          = "50086";
        /**
         * 验证码已失效
         */
        String ERROR_CODE_YZMYSX          = "50087";
        /**
         * 验证码无效
         */
        String ERROR_CODE_YZMWX           = "50088";
        /**
         * 售后信息不存在
         */
        String ERROR_CODE_SHXXBCZ         = "50089";
        /**
         * 售后失败
         */
        String ERROR_CODE_SHSB            = "50090";
        /**
         * 规格不存在
         */
        String ERROR_CODE_GGBCZ           = "50091";
        /**
         * 竞拍配置未设置
         */
        String ERROR_CODE_JPPZWSZ         = "50092";
        /**
         * 竞拍标题不能重复
         */
        String ERROR_CODE_JPBTBNZF        = "50093";
        /**
         * 操作失败存在正在竞拍商品
         */
        String ERROR_CODE_CZSBCZZZJPSP    = "50094";
        /**
         * 竞拍不存在
         */
        String ERROR_CODE_JPBCZ           = "50095";
        /**
         * 最低人数不能小于
         */
        String ERROR_CODE_ZDRSBNXY        = "50096";
        /**
         * 等待时间不能小于
         */
        String ERROR_CODE_DDSJBNXY        = "50097";
        /**
         * 等待时间不能大于
         */
        String ERROR_CODE_DDSJBNDY        = "50098";
        /**
         * 保留天数不能小于
         */
        String ERROR_CODE_BLTSBNXY        = "50099";
        /**
         * 数据不存在
         */
        String ERROR_CODE_SJBCZ           = "50100";
        /**
         * 不能删除进行中的活动
         */
        String ERROR_CODE_BNSCJXZDHD      = "50101";
        /**
         * 优惠卷过期删除天数不能为空
         */
        String ERROR_CODE_YHJGQSCTSBNWK   = "50102";
        /**
         * 优惠卷活动过期删除天数不能为空
         */
        String ERROR_CODE_YHJHDGQSCTSBNWK = "50103";
        /**
         * 请选择优惠券类型
         */
        String ERROR_CODE_QXZYHQLX        = "50104";
        /**
         * 领取限制不能为空
         */
        String ERROR_CODE_LQXZBNWK        = "50105";
        /**
         * 活动不存在
         */
        String ERROR_CODE_HDBCZ           = "50106";
        /**
         * 置顶失败
         */
        String ERROR_CODE_ZDSB            = "50107";
        /**
         * 移动失败
         */
        String ERROR_CODE_YDSB            = "50108";
        /**
         * 导航栏不存在
         */
        String ERROR_CODE_DHLBCZ          = "50109";
        /**
         * 导航栏名称不能为空
         */
        String ERROR_CODE_DHLMCBNWK       = "50110";
        /**
         * 导航栏名称不能超过个字
         */
        String ERROR_CODE_DHLMCBNCGGZ     = "50111";
        /**
         * 导航栏图片不能为空
         */
        String ERROR_CODE_DHLTPBNWK       = "50112";
        /**
         * 跳转地址不能为空
         */
        String ERROR_CODE_TZDZBNWK        = "50113";
        /**
         * 开关失败
         */
        String ERROR_CODE_KGSB            = "50114";
        /**
         * 商品类别不存在
         */
        String ERROR_CODE_SPLBBCZ         = "50115";
        /**
         * 类别不存在
         */
        String ERROR_CODE_LBBCZ           = "50116";
        /**
         * 请选择活动类型
         */
        String ERROR_CODE_QXZHDLX         = "50117";
        /**
         * 活动已存在
         */
        String ERROR_CODE_HDYCZ           = "50118";
        /**
         * 活动标题不能为空
         */
        String ERROR_CODE_HDBTBNWK        = "50119";
        /**
         * 活动显示开关失败
         */
        String ERROR_CODE_HDXSKGSB        = "50120";
        /**
         * 换位其中一个商品不存在
         */
        String ERROR_CODE_HWQZYGSPBCZ     = "50121";
        /**
         * 活动商品不存在
         */
        String ERROR_CODE_HDSPBCZ         = "50122";
        /**
         * 活动商品移动失败
         */
        String ERROR_CODE_HDSPYDSB        = "50123";
        /**
         * 模板名称不能为空
         */
        String ERROR_CODE_MBMCBNWK        = "50124";
        /**
         * 模板不存在
         */
        String ERROR_CODE_MBBCZ           = "50125";
        /**
         * 配置不能为空
         */
        String ERROR_CODE_PZBNWK          = "50126";
        /**
         * 模板名称已存在
         */
        String ERROR_CODE_MBMCYCZ         = "50127";
        /**
         * 模板添加失败
         */
        String ERROR_CODE_MBTJSB          = "50128";
        /**
         * 模板设置失败
         */
        String ERROR_CODE_MBSZSB          = "50129";
        /**
         * 模板删除失败
         */
        String ERROR_CODE_MBSCSB          = "50130";
        /**
         * 参团人数参数不正确
         */
        String ERROR_CODE_CTRSCSBZQ       = "50131";
        /**
         * 拼团人数不能小于
         */
        String ERROR_CODE_PTRSBNXY        = "50132";
        /**
         * 时间参数不正确
         */
        String ERROR_CODE_SJCSBZQ         = "50133";
        /**
         * 商品名称已存在
         */
        String ERROR_CODE_SPMCYCZ         = "50134";
        /**
         * 时间与老活动相交
         */
        String ERROR_CODE_SJYLHDXJ        = "50135";
        /**
         * 商品规格不存在
         */
        String ERROR_CODE_SPGGBCZ         = "50136";
        /**
         * 请稍后再试
         */
        String ERROR_CODE_QSHZS           = "50137";
        /**
         * 请稍后重试
         */
        String ERROR_CODE_QSHZS_001       = "50138";
        /**
         * 请先编辑设置活动商品
         */
        String ERROR_CODE_QXBJSZHDSP      = "50139";
        /**
         * 请选择正确的商品
         */
        String ERROR_CODE_QXZZQDSP        = "50140";
        /**
         * 兑换所需积分需大于零
         */
        String ERROR_CODE_DHSXJFXDYL      = "50141";
        /**
         * 积分商品不存在
         */
        String ERROR_CODE_JFSPBCZ         = "50142";
        /**
         * 商品未上架
         */
        String ERROR_CODE_SPWSJ           = "50143";
        /**
         * 兑换价格需小于商品原本售价
         */
        String ERROR_CODE_DHJGXXYSPYBSJ   = "50144";
        /**
         * 商品已添加
         */
        String ERROR_CODE_SPYTJ           = "50145";
        /**
         * 列表有商品库存不足
         */
        String ERROR_CODE_LBYSPKCBZ       = "50146";
        /**
         * 此商品还有订单未完成不可删除
         */
        String ERROR_CODE_CSPHYDDWWCBKSC  = "50147";
        /**
         * 请输入商品数量
         */
        String ERROR_CODE_QSRSPSL         = "50148";
        /**
         * 请输入自动收货时间
         */
        String ERROR_CODE_QSRZDSHSJ       = "50149";
        /**
         * 库存不足
         */
        String ERROR_CODE_KCBZ            = "50150";
        /**
         * 商户不存在
         */
        String ERROR_CODE_SHBCZ           = "50151";
        /**
         * 商城不存在
         */
        String ERROR_CODE_SCBCZ           = "50152";
        /**
         * 拒绝理由不能为空
         */
        String ERROR_CODE_JJLYBNWK        = "50153";
        /**
         * 网络故障
         */
        String ERROR_CODE_WLGZ            = "50154";
        /**
         * 提现申请不存在
         */
        String ERROR_CODE_TXSQBCZ         = "50155";
        /**
         * 用户名下无店铺
         */
        String ERROR_CODE_YHMXWDP         = "50156";
        /**
         * 拒绝原因不能为空
         */
        String ERROR_CODE_JJYYBNWK        = "50157";
        /**
         * 提现失败
         */
        String ERROR_CODE_TXSB            = "50158";
        /**
         * 默认头像不能为空
         */
        String ERROR_CODE_MRTXBNWK        = "50159";
        /**
         * 删除天数不能为空
         */
        String ERROR_CODE_SCTSBNWK        = "50160";

        /**
         * 最小提现金额不能为空
         */
        String ERROR_CODE_ZXTXJEBNWK            = "50161";
        /**
         * 最大提现金额不能为空
         */
        String ERROR_CODE_ZDTXJEBNWK            = "50162";
        /**
         * 手续费为大于小于的小数
         */
        String ERROR_CODE_SXFWDYXYDXS           = "50163";
        /**
         * 最小提现金额不能大于最大提现金额
         */
        String ERROR_CODE_ZXTXJEBNDYZDTXJE      = "50164";
        /**
         * 上传方式不能为空
         */
        String ERROR_CODE_SCFSBNWK              = "50165";
        /**
         * 请输入保证金
         */
        String ERROR_CODE_QSRBZJ                = "50166";
        /**
         * 请配置签到
         */
        String ERROR_CODE_QPZQD                 = "50167";
        /**
         * 请选择签到有效开始时间
         */
        String ERROR_CODE_QXZQDYXKSSJ           = "50168";
        /**
         * 请选择签到有效结束时间
         */
        String ERROR_CODE_QXZQDYXJSSJ           = "50169";
        /**
         * 签到有效时间错误
         */
        String ERROR_CODE_QDYXSJCW              = "50170";
        /**
         * 请填写每天签到有效次数
         */
        String ERROR_CODE_QTXMTQDYXCS           = "50171";
        /**
         * 每天签到有效次数请填写整数
         */
        String ERROR_CODE_MTQDYXCSQTXZS         = "50172";
        /**
         * 每天签到有效次数请填写大于的整数
         */
        String ERROR_CODE_MTQDYXCSQTXDYDZS      = "50173";
        /**
         * 每天可签到次数最多不能超过次
         */
        String ERROR_CODE_MTKQDCSZDBNCGC        = "50174";
        /**
         * 间隔小时不能为空
         */
        String ERROR_CODE_JGXSBNWK              = "50175";
        /**
         * 间隔时间设置超出限制请重新设置
         */
        String ERROR_CODE_JGSJSZCCXZQZXSZ       = "50176";
        /**
         * 领取积分不能为空
         */
        String ERROR_CODE_LQJFBNWK              = "50177";
        /**
         * 领取积分请填写整数
         */
        String ERROR_CODE_LQJFQTXZS             = "50178";
        /**
         * 领取积分请填写大于的整数
         */
        String ERROR_CODE_LQJFQTXDYDZS          = "50179";
        /**
         * 连续签到次数不能小于等于
         */
        String ERROR_CODE_LXQDCSBNXYDY          = "50180";
        /**
         * 次数没有依次递增
         */
        String ERROR_CODE_CSMYYCDZ              = "50181";
        /**
         * 奖励积分没有依次递增
         */
        String ERROR_CODE_JLJFMYYCDZ            = "50182";
        /**
         * 满减不存在
         */
        String ERROR_CODE_MJBCZ                 = "50183";
        /**
         * 活动标题已存在
         */
        String ERROR_CODE_HDBTYCZ               = "50184";
        /**
         * 活动名称不能为空
         */
        String ERROR_CODE_HDMCBNWK              = "50185";
        /**
         * 活动名称已存在
         */
        String ERROR_CODE_HDMCYCZ               = "50186";
        /**
         * 满减图片不能为空
         */
        String ERROR_CODE_MJTPBNWK              = "50187";
        /**
         * 满减应用范围不能为空
         */
        String ERROR_CODE_MJYYFWBNWK            = "50188";
        /**
         * 满减活动生效开始时间不能为空
         */
        String ERROR_CODE_MJHDSXKSSJBNWK        = "50189";
        /**
         * 满减活动生效结束时间不能为空
         */
        String ERROR_CODE_MJHDSXJSSJBNWK        = "50190";
        /**
         * 满减活动生效时间不正确
         */
        String ERROR_CODE_MJHDSXSJBZQ           = "50191";
        /**
         * 请设置满减活动生效结束时间
         */
        String ERROR_CODE_QSZMJHDSXJSSJ         = "50192";
        /**
         * 满减数值不正确
         */
        String ERROR_CODE_MJSZBZQ               = "50193";
        /**
         * 库存不存在
         */
        String ERROR_CODE_KCBCZ                 = "50194";
        /**
         * 地址数据格式不正确
         */
        String ERROR_CODE_DZSJGSBZQ             = "50195";
        /**
         * 门店不存在
         */
        String ERROR_CODE_MDBCZ                 = "50196";
        /**
         * 至少保留一个默认门店
         */
        String ERROR_CODE_ZSBLYGMRMD            = "50197";
        /**
         * 请输入账号密码
         */
        String ERROR_CODE_QSRZHMM               = "50198";
        /**
         * 请输入正确的商户编号
         */
        String ERROR_CODE_QSRZQDSHBH            = "50199";
        /**
         * 账号或密码错误请重新输入
         */
        String ERROR_CODE_ZHHMMCWQZXSR          = "50200";
        /**
         * 系统正在维护中
         */
        String ERROR_CODE_XTZZWHZ               = "50201";
        /**
         * 系统正在升级中
         */
        String ERROR_CODE_XTZZSJZ               = "50202";
        /**
         * 您的授权已到期请联系管理员再使用谢谢
         */
        String ERROR_CODE_NDSQYDQQLXGLYZSYXX    = "50203";
        /**
         * 您的商城已锁定请联系管理员再使用谢谢
         */
        String ERROR_CODE_NDSCYSDQLXGLYZSYXX    = "50204";
        /**
         * 您的授权已到期请联系客服完成续费再使用谢谢
         */
        String ERROR_CODE_NDSQYDQQLXKFWCXFZSYXX = "50205";
        /**
         * 账号已锁定请联系客服
         */
        String ERROR_CODE_ZHYSDQLXKF            = "50206";
        /**
         * 账号已被禁用若有疑问请与商城管理员联系
         */
        String ERROR_CODE_ZHYBJYRYYWQYSCGLYLX   = "50207";
        /**
         * 密码错误请重新输入
         */
        String ERROR_CODE_MMCWQZXSR             = "50208";
        /**
         * 商城没有管理员
         */
        String ERROR_CODE_SCMYGLY               = "50209";
        /**
         * 管理员密码长度为
         */
        String ERROR_CODE_GLYMMZDW              = "50210";
        /**
         * 原密与旧密码相同
         */
        String ERROR_CODE_YMYJMMXT              = "50211";
        /**
         * 版本配置信息不存在
         */
        String ERROR_CODE_BBPZXXBCZ             = "50212";
        /**
         * 名称不能为空
         */
        String ERROR_CODE_MCBNWK                = "50213";
        /**
         * 版本号不能为空
         */
        String ERROR_CODE_BBHBNWK               = "50214";
        /**
         * 版本号必须为正整数
         */
        String ERROR_CODE_BBHBXWZZS             = "50215";
        /**
         * 下载地址不能为空
         */
        String ERROR_CODE_XZDZBNWK              = "50216";
        /**
         * 是否自动更新不能为空
         */
        String ERROR_CODE_SFZDGXBNWK            = "50217";
        /**
         * 更新内容不能为空
         */
        String ERROR_CODE_GXNRBNWK              = "50218";
        /**
         * 当前版本号不能低于之前版本号
         */
        String ERROR_CODE_DQBBHBNDYZQBBH        = "50219";
        /**
         * 品牌不存在
         */
        String ERROR_CODE_PPBCZ                 = "50220";
        /**
         * 品牌类别不能为空
         */
        String ERROR_CODE_PPLBBNWK              = "50221";
        /**
         * 品牌名称不能为空
         */
        String ERROR_CODE_PPMCBNWK              = "50222";
        /**
         * 品牌名称已存在
         */
        String ERROR_CODE_PPMCYCZ               = "50223";
        /**
         * 参数不正确
         */
        String ERROR_CODE_CSBZQ                 = "50224";
        /**
         * 该品牌绑定了商品无法删除
         */
        String ERROR_CODE_GPPBDLSPWFSC          = "50225";
        /**
         * 开始时间不能大于结束时间
         */
        String ERROR_CODE_KSSJBNDYJSSJ          = "50226";
        /**
         * 物流不存在
         */
        String ERROR_CODE_WLBCZ                 = "50227";
        /**
         * 物流公司不存在
         */
        String ERROR_CODE_WLGSBCZ               = "50228";
        /**
         * 物流公司已存在
         */
        String ERROR_CODE_WLGSYCZ               = "50229";
        /**
         * 物流编码已存在
         */
        String ERROR_CODE_WLBMYCZ               = "50230";
        /**
         * 店铺不能为空
         */
        String ERROR_CODE_DPBNWK                = "50231";
        /**
         * 真实姓名不能为空
         */
        String ERROR_CODE_ZSXMBNWK              = "50232";
        /**
         * 身份证信号不能为空
         */
        String ERROR_CODE_SFZXHBNWK             = "50233";
        /**
         * 手机号不能为空
         */
        String ERROR_CODE_SJHBNWK               = "50234";
        /**
         * 详细地址不能为空
         */
        String ERROR_CODE_XXDZBNWK              = "50235";
        /**
         * 地址参数错误
         */
        String ERROR_CODE_DZCSCW                = "50236";
        /**
         * 店铺名称非法
         */
        String ERROR_CODE_DPMCFF                = "50237";
        /**
         * 商户账号不存在
         */
        String ERROR_CODE_SHZHBCZ               = "50238";
        /**
         * 自营店已存在
         */
        String ERROR_CODE_ZYDYCZ                = "50239";
        /**
         * 创建自营店铺失败
         */
        String ERROR_CODE_CJZYDPSB              = "50240";
        /**
         * 设置自营店铺失败
         */
        String ERROR_CODE_SZZYDPSB              = "50241";
        /**
         * 当前类别使用中删除失败
         */
        String ERROR_CODE_DQLBSYZSCSB           = "50242";
        /**
         * 类名不能为空
         */
        String ERROR_CODE_LMBNWK                = "50243";
        /**
         * 上级不存在
         */
        String ERROR_CODE_SJBCZ_001             = "50244";
        /**
         * 上级和当前等级不符
         */
        String ERROR_CODE_SJHDQDJBF             = "50245";
        /**
         * 类名已存在
         */
        String ERROR_CODE_LMYCZ                 = "50246";
        /**
         * 当前类别使用中编辑失败
         */
        String ERROR_CODE_DQLBSYZBJSB           = "50247";
        /**
         * 商品标签不存在
         */
        String ERROR_CODE_SPBQBCZ               = "50248";
        /**
         * 商品标签已存在
         */
        String ERROR_CODE_SPBQYCZ               = "50249";
        /**
         * 添加商品标签失败
         */
        String ERROR_CODE_TJSPBQSB              = "50250";
        /**
         * 删除商品标签失败
         */
        String ERROR_CODE_SCSPBQSB              = "50251";
        /**
         * 配置不存在
         */
        String ERROR_CODE_PZBCZ                 = "50252";
        /**
         * 修改配置失败
         */
        String ERROR_CODE_XGPZSB                = "50253";
        /**
         * 公告不存在
         */
        String ERROR_CODE_GGBCZ_001             = "50254";
        /**
         * 标题不能为空
         */
        String ERROR_CODE_BTBNWK                = "50255";
        /**
         * 公告类型不能为空
         */
        String ERROR_CODE_GGLXBNWK              = "50256";
        /**
         * 开始时间不能为空
         */
        String ERROR_CODE_KSSJBNWK              = "50257";
        /**
         * 结束时间不能为空
         */
        String ERROR_CODE_JSSJBNWK              = "50258";
        /**
         * 开始时间不能小于当前时间
         */
        String ERROR_CODE_KSSJBNXYDQSJ          = "50259";
        /**
         * 公告内容不能为空
         */
        String ERROR_CODE_GGNRBNWK              = "50260";
        /**
         * 系统公告不存在
         */
        String ERROR_CODE_XTGGBCZ               = "50261";
        /**
         * 评论不存在
         */
        String ERROR_CODE_PLBCZ                 = "50262";
        /**
         * 已经回复过了
         */
        String ERROR_CODE_YJHFGL                = "50263";
        /**
         * 回复失败
         */
        String ERROR_CODE_HFSB                  = "50264";
        /**
         * 同件数量不能为负数或零
         */
        String ERROR_CODE_TJSLBNWFSHL           = "50265";
        /**
         * 同单数量不能为负数或零
         */
        String ERROR_CODE_TDSLBNWFSHL           = "50266";
        /**
         * 自动收货时间不能为负数或零
         */
        String ERROR_CODE_ZDSHSJBNWFSHL         = "50267";
        /**
         * 订单过期删除时间不能为负数或零
         */
        String ERROR_CODE_DDGQSCSJBNWFSHL       = "50268";
        /**
         * 订单售后时间不能为负数或零
         */
        String ERROR_CODE_DDSHSJBNWFSHL         = "50269";
        /**
         * 请输入正确的提醒限制时间
         */
        String ERROR_CODE_QSRZQDTXXZSJ          = "50270";
        /**
         * 未知原因订单设置修改失败
         */
        String ERROR_CODE_WZYYDDSZXGSB          = "50271";
        /**
         * 请选择快递公司
         */
        String ERROR_CODE_QXZKDGS               = "50272";
        /**
         * 请输入快递单号
         */
        String ERROR_CODE_QSRKDDH               = "50273";
        /**
         * 快递单号已存在
         */
        String ERROR_CODE_KDDHYCZ               = "50274";
        /**
         * 订单详情获取失败
         */
        String ERROR_CODE_DDXQHQSB              = "50275";
        /**
         * 商品参数错误
         */
        String ERROR_CODE_SPCSCW                = "50276";
        /**
         * 会员不存在
         */
        String ERROR_CODE_HYBCZ                 = "50277";
        /**
         * 请选择收货地址
         */
        String ERROR_CODE_QXZSHDZ               = "50278";
        /**
         * 优惠金额输入有误
         */
        String ERROR_CODE_YHJESRYW              = "50279";
        /**
         * 下单失败
         */
        String ERROR_CODE_XDSB                  = "50280";
        /**
         * 计算失败
         */
        String ERROR_CODE_JSSB                  = "50281";
        /**
         * 获取物流信息失败
         */
        String ERROR_CODE_HQWLXXSB              = "50282";
        /**
         * 置顶异常
         */
        String ERROR_CODE_ZDYC                  = "50283";
        /**
         * 数据错误
         */
        String ERROR_CODE_SJCW                  = "50284";
        /**
         * 配置失败
         */
        String ERROR_CODE_PZSB                  = "50285";
        /**
         * 支付方式不存在
         */
        String ERROR_CODE_ZFFSBCZ               = "50286";
        /**
         * 无效位置
         */
        String ERROR_CODE_WXWZ                  = "50287";
        /**
         * 获取实例失败
         */
        String ERROR_CODE_HQSLSB                = "50288";
        /**
         * 添加数据异常
         */
        String ERROR_CODE_TJSJYC                = "50289";
        /**
         * 修改数据异常
         */
        String ERROR_CODE_XGSJYC                = "50290";
        /**
         * 删除数据异常
         */
        String ERROR_CODE_SCSJYC                = "50291";
        /**
         * 图片不存在
         */
        String ERROR_CODE_TPBCZ                 = "50292";
        /**
         * 管理员不存在
         */
        String ERROR_CODE_GLYBCZ                = "50293";
        /**
         * 账号已存在
         */
        String ERROR_CODE_ZHYCZ                 = "50294";
        /**
         * 角色名称不能超过个中文字长度
         */
        String ERROR_CODE_JSMCBNCGGZWZZD        = "50295";
        /**
         * 请选择绑定权限
         */
        String ERROR_CODE_QXZBDQX               = "50296";
        /**
         * 状态不能为空
         */
        String ERROR_CODE_ZTBNWK                = "50297";
        /**
         * 角色名称已存在
         */
        String ERROR_CODE_JSMCYCZ               = "50298";
        /**
         * 菜单数据错误
         */
        String ERROR_CODE_CDSJCW                = "50299";
        /**
         * 网络繁忙
         */
        String ERROR_CODE_WLFM                  = "50300";
        /**
         * 请先解除绑定关系再进行删除操作
         */
        String ERROR_CODE_QXJCBDGXZJXSCCZ       = "50301";
        /**
         * 小程序不存在
         */
        String ERROR_CODE_XCXBCZ                = "50302";
        /**
         * 设置业务域名失败
         */
        String ERROR_CODE_SZYWYMSB              = "50303";
        /**
         * 密钥不能为空
         */
        String ERROR_CODE_MYBNWK                = "50304";
        /**
         * 属性不存在
         */
        String ERROR_CODE_SXBCZ                 = "50305";
        /**
         * 属性名称不能为空
         */
        String ERROR_CODE_SXMCBNWK              = "50306";
        /**
         * 属性生效中禁止修改
         */
        String ERROR_CODE_SXSXZJZXG             = "50307";
        /**
         * 属性名称已存在
         */
        String ERROR_CODE_SXMCYCZ               = "50308";
        /**
         * 属性值不能为空
         */
        String ERROR_CODE_SXZBNWK               = "50309";
        /**
         * 属性名称不存在
         */
        String ERROR_CODE_SXMCBCZ               = "50310";
        /**
         * 属性值重复
         */
        String ERROR_CODE_SXZZF                 = "50311";
        /**
         * 属性使用中无法删除
         */
        String ERROR_CODE_SXSYZWFSC             = "50312";
        /**
         * 列表中属性生效中删除失败
         */
        String ERROR_CODE_LBZSXSXZSCSB          = "50313";
        /**
         * 请填写图片上传域名
         */
        String ERROR_CODE_QTXTPSCYM             = "50314";
        /**
         * 请填写本地存储位置
         */
        String ERROR_CODE_QTXBDCCWZ             = "50315";
        /**
         * 服务器繁忙
         */
        String ERROR_CODE_FWQFM                 = "50316";
        /**
         * 请填写存储空间名称
         */
        String ERROR_CODE_QTXCCKJMC             = "50317";
        /**
         * 请填写
         */
        String ERROR_CODE_QTX                   = "50318";
        /**
         * 菜单名称不能超过六个字符
         */
        String ERROR_CODE_CDMCBNCGLGZF          = "50319";
        /**
         * 菜单级别不能为空
         */
        String ERROR_CODE_CDJBBNWK              = "50320";
        /**
         * 请选择菜单默认图标
         */
        String ERROR_CODE_QXZCDMRTB             = "50321";
        /**
         * 请选择菜单选中图标
         */
        String ERROR_CODE_QXZCDXZTB             = "50322";
        /**
         * 菜单名称已存在
         */
        String ERROR_CODE_CDMCYCZ               = "50323";
        /**
         * 默认图标不能为空
         */
        String ERROR_CODE_MRTBBNWK              = "50324";
        /**
         * 选中图标不能为空
         */
        String ERROR_CODE_XZTBBNWK              = "50325";
        /**
         * 路径不能为空
         */
        String ERROR_CODE_LJBNWK                = "50326";
        /**
         * 上级菜单不能为空
         */
        String ERROR_CODE_SJCDBNWK              = "50327";
        /**
         * 一级菜单不可作为权限按钮
         */
        String ERROR_CODE_YJCDBKZWQXAN          = "50328";
        /**
         * 删除失败请先删除子菜单
         */
        String ERROR_CODE_SCSBQXSCZCD           = "50329";
        /**
         * 删除失败菜单使用中
         */
        String ERROR_CODE_SCSBCDSYZ             = "50330";
        /**
         * 菜单删除失败
         */
        String ERROR_CODE_CDSCSB                = "50331";
        /**
         * 列表中有管理员不存在
         */
        String ERROR_CODE_LBZYGLYBCZ            = "50332";
        /**
         * 禁用失败
         */
        String ERROR_CODE_JYSB                  = "50333";
        /**
         * 锁定失败
         */
        String ERROR_CODE_SDSB                  = "50334";
        /**
         * 启用失败
         */
        String ERROR_CODE_QYSB                  = "50335";
        /**
         * 商城价格不能为空
         */
        String ERROR_CODE_SCJGBNWK              = "50336";
        /**
         * 邮箱不能为空
         */
        String ERROR_CODE_YXBNWK                = "50337";
        /**
         * 到期时间不能为空
         */
        String ERROR_CODE_DQSJBNWK              = "50338";
        /**
         * 到期时间不能小于当前时间
         */
        String ERROR_CODE_DQSJBNXYDQSJ          = "50339";
        /**
         * 是否启用不能为空
         */
        String ERROR_CODE_SFQYBNWK              = "50340";
        /**
         * 密码不符合规范
         */
        String ERROR_CODE_MMBFHGF               = "50341";
        /**
         * 客户编号不能为空
         */
        String ERROR_CODE_KHBHBNWK              = "50342";
        /**
         * 商城根目录域名不能为空
         */
        String ERROR_CODE_SCGMLYMBNWK           = "50343";
        /**
         * 公司名称不能为空
         */
        String ERROR_CODE_GSMCBNWK              = "50344";
        /**
         * 版权信息不能为空
         */
        String ERROR_CODE_BQXXBNWK              = "50345";
        /**
         * 备案信息不能为空
         */
        String ERROR_CODE_BAXXBNWK              = "50346";
        /**
         * 商户不能为空
         */
        String ERROR_CODE_SHBNWK                = "50347";
        /**
         * 管理员账号不能为空
         */
        String ERROR_CODE_GLYZHBNWK             = "50348";
        /**
         * 请填写客户名称
         */
        String ERROR_CODE_QTXKHMC               = "50349";
        /**
         * 客户编号已存在
         */
        String ERROR_CODE_KHBHYCZ               = "50350";
        /**
         * 邮箱已存在
         */
        String ERROR_CODE_YXYCZ                 = "50351";
        /**
         * 商户名称已存在
         */
        String ERROR_CODE_KHMCYCZ               = "50352";
        /**
         * 管理员密码不能为空
         */
        String ERROR_CODE_GLYMMBNWK             = "50353";
        /**
         * 请选择角色权限
         */
        String ERROR_CODE_QXZJSQX               = "50354";
        /**
         * 管理员账号已存在
         */
        String ERROR_CODE_GLYZHYCZ              = "50355";
        /**
         * 商城添加失败
         */
        String ERROR_CODE_SCTJSB                = "50356";
        /**
         * 管理员添加失败
         */
        String ERROR_CODE_GLYTJSB               = "50357";
        /**
         * 商城管理员添加失败
         */
        String ERROR_CODE_SCGLYTJSB             = "50358";
        /**
         * 系统设置添加失败
         */
        String ERROR_CODE_XTSZTJSB              = "50359";
        /**
         * 支付方式添加失败
         */
        String ERROR_CODE_ZFFSTJSB              = "50360";
        /**
         * 商城修改失败
         */
        String ERROR_CODE_SCXGSB                = "50361";
        /**
         * 商城删除失败
         */
        String ERROR_CODE_SCSCSB                = "50362";
        /**
         * 数量请输入正整数
         */
        String ERROR_CODE_SLQSRZZS              = "50363";
        /**
         * 地址不存在
         */
        String ERROR_CODE_DZBCZ                 = "50364";
        /**
         * 联系人不能为空
         */
        String ERROR_CODE_LXRBNWK               = "50365";
        /**
         * 请选择省市辖区
         */
        String ERROR_CODE_QXZSSXQ               = "50366";
        /**
         * 请选择区县
         */
        String ERROR_CODE_QXZQX_001             = "50367";
        /**
         * 邮政编码不能为空
         */
        String ERROR_CODE_YZBMBNWK              = "50368";
        /**
         * 邮政编码为位数字
         */
        String ERROR_CODE_YZBMWWSZ              = "50369";
        /**
         * 联系人电话不能为空
         */
        String ERROR_CODE_LXRDHBNWK             = "50370";
        /**
         * 联系电话已存在
         */
        String ERROR_CODE_LXDHYCZ               = "50371";
        /**
         * 当前地址已是默认
         */
        String ERROR_CODE_DQDZYSMR              = "50372";
        /**
         * 设置失败
         */
        String ERROR_CODE_SZSB                  = "50373";
        /**
         * 上限至少一个
         */
        String ERROR_CODE_SXZSYG                = "50374";
        /**
         * 关键字不能为空
         */
        String ERROR_CODE_GJZBNWK               = "50375";
        /**
         * 关键字不能大于限制数量
         */
        String ERROR_CODE_GJZBNDYXZSL           = "50376";
        /**
         * 短信类别不能为空
         */
        String ERROR_CODE_DXLBBNWK              = "50377";
        /**
         * 短信类型不能为空
         */
        String ERROR_CODE_DXLXBNWK              = "50378";
        /**
         * 短信模板不能为空
         */
        String ERROR_CODE_DXMBBNWK              = "50379";
        /**
         * 短信模板内容不能为空
         */
        String ERROR_CODE_DXMBNRBNWK            = "50380";
        /**
         * 该类短信模板已添加请勿重复添加
         */
        String ERROR_CODE_GLDXMBYTJQWZFTJ       = "50381";
        /**
         * 模板参数数量不匹配期望数量
         */
        String ERROR_CODE_MBCSSLBPPQWSL         = "50382";
        /**
         * 短信配置模板不存在
         */
        String ERROR_CODE_DXPZMBBCZ             = "50383";
        /**
         * 短信签名不能为空
         */
        String ERROR_CODE_DXQMBNWK              = "50384";
        /**
         * 短信模板名称不能为空
         */
        String ERROR_CODE_DXMBMCBNWK            = "50385";
        /**
         * 短信代码不能为空
         */
        String ERROR_CODE_DXDMBNWK              = "50386";
        /**
         * 短信接收号码不能为空
         */
        String ERROR_CODE_DXJSHMBNWK            = "50387";
        /**
         * 短信内容不能为空
         */
        String ERROR_CODE_DXNRBNWK              = "50388";
        /**
         * 模板至少需要一个参数不正确
         */
        String ERROR_CODE_MBZSXYYGCSBZQ         = "50389";
        /**
         * 短信模板不存在
         */
        String ERROR_CODE_DXMBBCZ               = "50390";
        /**
         * 短信模板已存在
         */
        String ERROR_CODE_DXMBYCZ               = "50391";
        /**
         * 短信名称已存在
         */
        String ERROR_CODE_DXMCYCZ               = "50392";
        /**
         * 短信模板添加失败
         */
        String ERROR_CODE_DXMBTJSB              = "50393";
        /**
         * 该模板正在使用
         */
        String ERROR_CODE_GMBZZSY               = "50394";
        /**
         * 登录页不能为空
         */
        String ERROR_CODE_DLYBNWK               = "50395";
        /**
         * 登录链接参数错误
         */
        String ERROR_CODE_DLLJCSCW              = "50396";
        /**
         * 协议标题不能为空
         */
        String ERROR_CODE_XYBTBNWK              = "50397";
        /**
         * 内容不能为空
         */
        String ERROR_CODE_NRBNWK                = "50398";
        /**
         * 协议已存在
         */
        String ERROR_CODE_XYYCZ                 = "50399";
        /**
         * 协议不存在
         */
        String ERROR_CODE_XYBCZ                 = "50400";
        /**
         * 售后问题不能为空
         */
        String ERROR_CODE_SHWTBNWK              = "50401";
        /**
         * 支付问题不能为空
         */
        String ERROR_CODE_ZFWTBNWK              = "50402";
        /**
         * 请先设置基础配置
         */
        String ERROR_CODE_QXSZJCPZ              = "50403";
        /**
         * 退货政策不能为空
         */
        String ERROR_CODE_THZCBNWK              = "50404";
        /**
         * 取消订单不能为空
         */
        String ERROR_CODE_QXDDBNWK              = "50405";
        /**
         * 退款流程不能为空
         */
        String ERROR_CODE_TKLCBNWK              = "50406";
        /**
         * 退款说明不能为空
         */
        String ERROR_CODE_TKSMBNWK              = "50407";
        /**
         * 购物流程不能为空
         */
        String ERROR_CODE_GWLCBNWK              = "50408";
        /**
         * 支付方式不能为空
         */
        String ERROR_CODE_ZFFSBNWK              = "50409";
        /**
         * 小程序不能为空
         */
        String ERROR_CODE_XCXBNWK               = "50410";
        /**
         * 小程序密钥不能为空
         */
        String ERROR_CODE_XCXMYBNWK             = "50411";
        /**
         * 任务不存在
         */
        String ERROR_CODE_RWBCZ                 = "50412";
        /**
         * 任务名称不能为空
         */
        String ERROR_CODE_RWMCBNWK              = "50413";
        /**
         * 类别不能为空
         */
        String ERROR_CODE_LBBNWK                = "50414";
        /**
         * 品牌不能为空
         */
        String ERROR_CODE_PPBNWK                = "50415";
        /**
         * 淘宝链接不能为空
         */
        String ERROR_CODE_TBLJBNWK              = "50416";
        /**
         * 淘宝链接不正确
         */
        String ERROR_CODE_TBLJBZQ               = "50417";
        /**
         * 引导图不能为空
         */
        String ERROR_CODE_YDTBNWK               = "50418";
        /**
         * 引导图不存在
         */
        String ERROR_CODE_YDTBCZ                = "50419";
        /**
         * 行业不能为空
         */
        String ERROR_CODE_XYBNWK                = "50420";
        /**
         * 小程序模板不能为空
         */
        String ERROR_CODE_XCXMBBNWK             = "50421";
        /**
         * 模板简介不能为空
         */
        String ERROR_CODE_MBJJBNWK              = "50422";
        /**
         * 未授权请返回授权页
         */
        String ERROR_CODE_WSQQFHSQY             = "50423";
        /**
         * 小程序未审核
         */
        String ERROR_CODE_XCXWSH                = "50424";
        /**
         * 提交审核失败请查看审核日志
         */
        String ERROR_CODE_TJSHSBQCKSHRZ         = "50425";
        /**
         * 设置服务器域名失败
         */
        String ERROR_CODE_SZFWQYMSB             = "50426";
        /**
         * 审核提交失败
         */
        String ERROR_CODE_SHTJSB                = "50427";
        /**
         * 会员等级不存在
         */
        String ERROR_CODE_HYDJBCZ               = "50428";
        /**
         * 会员等级名称不能为空
         */
        String ERROR_CODE_HYDJMCBNWK            = "50429";
        /**
         * 会员折扣不能为空
         */
        String ERROR_CODE_HYZKBNWK              = "50430";
        /**
         * 包月金额不能为空
         */
        String ERROR_CODE_BYJEBNWK              = "50431";
        /**
         * 包季金额不能为空
         */
        String ERROR_CODE_BJJEBNWK              = "50432";
        /**
         * 包年金额不能为空
         */
        String ERROR_CODE_BNJEBNWK              = "50433";
        /**
         * 等级名称已存在
         */
        String ERROR_CODE_DJMCYCZ               = "50434";
        /**
         * 折扣率不能与其它级别想相同
         */
        String ERROR_CODE_ZKLBNYQTJBXXT         = "50435";
        /**
         * 会员等级规则未配置
         */
        String ERROR_CODE_HYDJGZWPZ             = "50436";
        /**
         * 包月金额应比上一级等级小
         */
        String ERROR_CODE_BYJEYBSYJDJX          = "50437";
        /**
         * 包季金额应比上一级等级小
         */
        String ERROR_CODE_BJJEYBSYJDJX          = "50438";
        /**
         * 包月金额应比下一级等级大
         */
        String ERROR_CODE_BYJEYBXYJDJD          = "50439";
        /**
         * 包季金额应比下一级等级大
         */
        String ERROR_CODE_BJJEYBXYJDJD          = "50440";
        /**
         * 包年金额应比下一级等级大
         */
        String ERROR_CODE_BNJEYBXYJDJD          = "50441";
        /**
         * 月费不得高于季费季费不得高于年费
         */
        String ERROR_CODE_YFBDGYJFJFBDGYNF      = "50442";
        /**
         * 已有用户为该等级会员不可删除
         */
        String ERROR_CODE_YYYHWGDJHYBKSC        = "50443";
        /**
         * 请完善系统设置
         */
        String ERROR_CODE_QWSXTSZ               = "50444";
        /**
         * 修改失败参数错误
         */
        String ERROR_CODE_XGSBCSCW              = "50445";
        /**
         * 用户名格式不正确
         */
        String ERROR_CODE_YHMGSBZQ              = "50446";
        /**
         * 手机号已被注册
         */
        String ERROR_CODE_SJHYBZC               = "50447";
        /**
         * 删除失败该用户有余额未使用
         */
        String ERROR_CODE_SCSBGYHYYEWSY         = "50448";
        /**
         * 删除失败该用户有未完成的订单
         */
        String ERROR_CODE_SCSBGYHYWWCDDD        = "50449";
        /**
         * 删除失败该用户有未注销的店铺
         */
        String ERROR_CODE_SCSBGYHYWZXDDP        = "50450";
        /**
         * 删除失败该用户有分销身份
         */
        String ERROR_CODE_SCSBGYHYFXSF          = "50451";
        /**
         * 用户名不能为空
         */
        String ERROR_CODE_YHMBNWK               = "50452";
        /**
         * 会员等级不能为空
         */
        String ERROR_CODE_HYDJBNWK              = "50453";
        /**
         * 密码不能为空
         */
        String ERROR_CODE_MMBNWK                = "50454";
        /**
         * 账号不能为空
         */
        String ERROR_CODE_ZHBNWK                = "50455";
        /**
         * 手机号码已存在
         */
        String ERROR_CODE_SJHMYCZ               = "50456";
        /**
         * 请先创建自营店
         */
        String ERROR_CODE_QXCJZYD               = "50457";
        /**
         * 注册失败请联系管理员
         */
        String ERROR_CODE_ZCSBQLXGLY            = "50458";
        /**
         * 提现记录不存在
         */
        String ERROR_CODE_TXJLBCZ               = "50459";
        /**
         * 服务器繁忙请稍后重试
         */
        String ERROR_CODE_FWQFMQSHZS            = "50460";
        /**
         * 最小充值金额不能小于等于
         */
        String ERROR_CODE_ZXCZJEBNXYDY          = "50461";
        /**
         * 最小提现金额不能小于等于
         */
        String ERROR_CODE_ZXTXJEBNXYDY          = "50462";
        /**
         * 最大提现金额不能小于等于
         */
        String ERROR_CODE_ZDTXJEBNXYDY          = "50463";
        /**
         * 最小提现金额不能小于等于手续费
         */
        String ERROR_CODE_ZXTXJEBNXYDYSXF       = "50464";
        /**
         * 添加秒杀活动失败
         */
        String ERROR_CODE_TJMSHDSB              = "50465";
        /**
         * 请登录
         */
        String ERROR_CODE_QDL                   = "50466";
        /**
         * 竞拍商品不存在
         */
        String ERROR_CODE_JPSPBCZ               = "50467";
        /**
         * 未设置密码
         */
        String ERROR_CODE_WSZMM                 = "50468";
        /**
         * 密码不正确
         */
        String ERROR_CODE_MMBZQ                 = "50469";
        /**
         * 余额不足
         */
        String ERROR_CODE_YEBZ                  = "50470";
        /**
         * 支付失败
         */
        String ERROR_CODE_ZFSB                  = "50471";
        /**
         * 不在涨价的时间请稍后再试
         */
        String ERROR_CODE_BZZJDSJQSHZS          = "50472";
        /**
         * 投标价格不得低于开盘价
         */
        String ERROR_CODE_TBJGBDDYKPJ           = "50473";
        /**
         * 投标价格不得低于最新报价
         */
        String ERROR_CODE_TBJGBDDYZXBJ          = "50474";
        /**
         * 砍价配置不存在
         */
        String ERROR_CODE_KJPZBCZ               = "50475";
        /**
         * 砍价商品不存在
         */
        String ERROR_CODE_KJSPBCZ               = "50476";
        /**
         * 商品属性信息不存在
         */
        String ERROR_CODE_SPSXXXBCZ             = "50477";
        /**
         * 商品库存不足
         */
        String ERROR_CODE_SPKCBZ                = "50478";
        /**
         * 参数异常
         */
        String ERROR_CODE_CSYC                  = "50479";
        /**
         * 砍价订单不存在
         */
        String ERROR_CODE_KJDDBCZ               = "50480";
        /**
         * 砍价失败
         */
        String ERROR_CODE_KJSB                  = "50481";
        /**
         * 未找到购物车数据
         */
        String ERROR_CODE_WZDGWCSJ              = "50482";
        /**
         * 未找到库存数据
         */
        String ERROR_CODE_WZDKCSJ               = "50483";
        /**
         * 未找到商品数据
         */
        String ERROR_CODE_WZDSPSJ               = "50484";
        /**
         * 二维码参数错误
         */
        String ERROR_CODE_EWMCSCW               = "50485";
        /**
         * 业务异常
         */
        String ERROR_CODE_YWYC                  = "50486";
        /**
         * 商城未配置
         */
        String ERROR_CODE_SCWPZ                 = "50487";
        /**
         * 数据异常
         */
        String ERROR_CODE_SJYC                  = "50488";
        /**
         * 图片生成失败
         */
        String ERROR_CODE_TPSCSB                = "50489";
        /**
         * 没有更多了
         */
        String ERROR_CODE_MYGDL                 = "50490";
        /**
         * 非法参数
         */
        String ERROR_CODE_FFCS                  = "50491";
        /**
         * 请输入正确的提现金额
         */
        String ERROR_CODE_JEQSRJE               = "50492";
        /**
         * 佣金更新失败
         */
        String ERROR_CODE_YJGXSB                = "50493";
        /**
         * 没有可提现金额
         */
        String ERROR_CODE_MYKTXJE               = "50494";
        /**
         * 取款金额是否小于或等于或大于现有金额
         */
        String ERROR_CODE_QKJESFXYHDYHDYXYJE    = "50495";
        /**
         * 提现金额小于最小提现金额
         */
        String ERROR_CODE_TXJEXYZXTXJE          = "50496";
        /**
         * 提现金额大于最大提现金额
         */
        String ERROR_CODE_TXJEDYZDTXJE          = "50497";
        /**
         * 提现金额小于等于手续费
         */
        String ERROR_CODE_TXJEXYDYSXF           = "50498";
        /**
         * 您还有一笔提现正在审核中
         */
        String ERROR_CODE_NHYYBTXZZSHZ          = "50499";
        /**
         * 一天只允许提现一次
         */
        String ERROR_CODE_YTZYXTXYC             = "50500";
        /**
         * 拼团数据不存在
         */
        String ERROR_CODE_PTSJBCZ               = "50501";
        /**
         * 修改订单状态失败
         */
        String ERROR_CODE_XGDDZTSB              = "50502";
        /**
         * 更多商品加载失败网络异常
         */
        String ERROR_CODE_GDSPJZSBWLYC          = "50503";
        /**
         * 位置获取失败网络异常
         */
        String ERROR_CODE_WZHQSBWLYC            = "50504";
        /**
         * 获取商品信息失败
         */
        String ERROR_CODE_HQSPXXSB              = "50505";
        /**
         * 首页秒杀获取失败网络异常
         */
        String ERROR_CODE_SYMSHQSBWLYC          = "50506";
        /**
         * 首页拼团商品流程出错
         */
        String ERROR_CODE_SYPTSPLCCC            = "50507";
        /**
         * 未获取到数据
         */
        String ERROR_CODE_WHQDSJ                = "50508";
        /**
         * 网路异常
         */
        String ERROR_CODE_WLYC_001              = "50509";
        /**
         * 读取消息失败网络异常
         */
        String ERROR_CODE_DQXXSBWLYC            = "50510";
        /**
         * 加载后续消息网络异常
         */
        String ERROR_CODE_JZHXXXWLYC            = "50511";
        /**
         * 该商城暂无模板
         */
        String ERROR_CODE_GSCZWMB               = "50512";
        /**
         * 获取微信模板异常
         */
        String ERROR_CODE_HQWXMBYC              = "50513";
        /**
         * 未登录
         */
        String ERROR_CODE_WDL                   = "50514";
        /**
         * 一键标记已读失败网络异常
         */
        String ERROR_CODE_YJBJYDSBWLYC          = "50515";
        /**
         * 获取消息详情失败网络异常
         */
        String ERROR_CODE_HQXXXQSBWLYC          = "50516";
        /**
         * 消息删除失败网络异常
         */
        String ERROR_CODE_XXSCSBWLYC            = "50517";
        /**
         * 获取微信模板失败网络异常
         */
        String ERROR_CODE_HQWXMBSBWLYC          = "50518";
        /**
         * 插件信息不存在
         */
        String ERROR_CODE_CJXXBCZ               = "50519";
        /**
         * 搜索首页网络异常
         */
        String ERROR_CODE_SSSYWLYC              = "50520";
        /**
         * 搜索详情网络异常
         */
        String ERROR_CODE_SSXQWLYC              = "50521";
        /**
         * 热搜网络异常
         */
        String ERROR_CODE_RSWLYC                = "50522";
        /**
         * 搜搜网络异常
         */
        String ERROR_CODE_SSWLYC                = "50523";
        /**
         * 搜索联想网络异常
         */
        String ERROR_CODE_SSLXWLYC              = "50524";
        /**
         * 操作异常
         */
        String ERROR_CODE_CZYC                  = "50525";
        /**
         * 礼包升级异常
         */
        String ERROR_CODE_LBSJYC                = "50526";
        /**
         * 该用户已经开通了分销
         */
        String ERROR_CODE_GYHYJKTLFX            = "50527";
        /**
         * 分销推荐人数据错误
         */
        String ERROR_CODE_FXTJRSJCW             = "50528";
        /**
         * 创建分销商异常
         */
        String ERROR_CODE_CJFXSYC               = "50529";
        /**
         * 商品数据错误
         */
        String ERROR_CODE_SPSJCW                = "50530";
        /**
         * 参数格式不正确
         */
        String ERROR_CODE_CSGSBZQ               = "50531";
        /**
         * 下单失败请稍后再试
         */
        String ERROR_CODE_XDSBQSHZS             = "50532";
        /**
         * 下单失败更新兑换券使用状态失败
         */
        String ERROR_CODE_XDSBGXDHQSYZTSB       = "50533";
        /**
         * 添加优惠券关联订单数据失败
         */
        String ERROR_CODE_TJYHQGLDDSJSB         = "50534";
        /**
         *
         */
        String ERROR_CODE_                      = "50535";
        /**
         * 订单列表获取失败
         */
        String ERROR_CODE_DDLBHQSB              = "50536";
        /**
         * 提醒发货失败
         */
        String ERROR_CODE_TXFHSB                = "50537";
        /**
         * 已经发货
         */
        String ERROR_CODE_YJFH                  = "50538";
        /**
         * 库存不充足
         */
        String ERROR_CODE_KCBCZ_001             = "50539";
        /**
         * 商品已下架
         */
        String ERROR_CODE_SPYXJ                 = "50540";
        /**
         * 非法入侵
         */
        String ERROR_CODE_FFRQ                  = "50541";
        /**
         * 图片上传失败
         */
        String ERROR_CODE_TPSCSB_001            = "50542";
        /**
         * 订单信息不存在
         */
        String ERROR_CODE_DDXXBCZ               = "50543";
        /**
         * 列表有商品再售后中
         */
        String ERROR_CODE_LBYSPZSHZ             = "50544";
        /**
         * 已达到售后申请限制
         */
        String ERROR_CODE_YDDSHSQXZ             = "50545";
        /**
         * 金额不能大于当前订单总金额
         */
        String ERROR_CODE_JEBNDYDQDDZJE         = "50546";
        /**
         * 生成售后订单失败
         */
        String ERROR_CODE_SCSHDDSB              = "50547";
        /**
         * 网络故障售后记录失败
         */
        String ERROR_CODE_WLGZSHJLSB            = "50548";
        /**
         * 售后确认收货异常
         */
        String ERROR_CODE_SHQRSHYC              = "50549";
        /**
         * 撤销审核失败
         */
        String ERROR_CODE_CXSHSB                = "50550";
        /**
         * 下单失败计算分佣失败
         */
        String ERROR_CODE_XDSBJSFYSB            = "50551";
        /**
         * 请先取消之前参团的订单
         */
        String ERROR_CODE_QXQXZQCTDDD           = "50552";
        /**
         * 支付失败拼团人数已满
         */
        String ERROR_CODE_ZFSBPTRSYM            = "50553";
        /**
         * 商品信息不完整
         */
        String ERROR_CODE_SPXXBWZ               = "50554";
        /**
         * 积分不足
         */
        String ERROR_CODE_JFBZ                  = "50555";
        /**
         * 积分商品库存不足
         */
        String ERROR_CODE_JFSPKCBZ              = "50556";
        /**
         * 订单不在待付款状态
         */
        String ERROR_CODE_DDBZDFKZT             = "50557";
        /**
         * 订单关闭失败
         */
        String ERROR_CODE_DDGBSB                = "50558";
        /**
         * 订单数据错误
         */
        String ERROR_CODE_DDSJCW                = "50559";
        /**
         * 运费信息错误
         */
        String ERROR_CODE_YFXXCW                = "50560";
        /**
         * 商品信息错误
         */
        String ERROR_CODE_SPXXCW                = "50561";
        /**
         * 该商品暂未上架
         */
        String ERROR_CODE_GSPZWSJ               = "50562";
        /**
         * 该商品不支持定金模式
         */
        String ERROR_CODE_GSPBZCDJMS            = "50563";
        /**
         * 该商品已过支付定金有效期
         */
        String ERROR_CODE_GSPYGZFDJYXQ          = "50564";
        /**
         * 该商品还未到支付尾款时间
         */
        String ERROR_CODE_GSPHWDZFWKSJ          = "50565";
        /**
         * 该商品已过尾款支付时间
         */
        String ERROR_CODE_GSPYGWKZFSJ           = "50566";
        /**
         * 该商品不支持订货模式
         */
        String ERROR_CODE_GSPBZCDHMS            = "50567";
        /**
         * 该商品已截止预售
         */
        String ERROR_CODE_GSPYJZYS              = "50568";
        /**
         * 该商品剩余预售库存
         */
        String ERROR_CODE_GSPSYYSKC             = "50569";
        /**
         * 该商品已售空
         */
        String ERROR_CODE_GSPYSK                = "50570";
        /**
         * 您已生成待付款订单请前往支付
         */
        String ERROR_CODE_NYSCDFKDDQQWZF        = "50571";
        /**
         * 未交保证金
         */
        String ERROR_CODE_WJBZJ                 = "50572";
        /**
         * 退款失败
         */
        String ERROR_CODE_TKSB                  = "50573";
        /**
         * 签到配置信息不存在
         */
        String ERROR_CODE_QDPZXXBCZ             = "50574";
        /**
         * 签到插件未启用
         */
        String ERROR_CODE_QDCJWQY               = "50575";
        /**
         * 签到数据加载失败
         */
        String ERROR_CODE_QDSJJZSB              = "50576";
        /**
         * 满减活动不存在
         */
        String ERROR_CODE_MJHDBCZ               = "50577";
        /**
         * 获取地址信息异常
         */
        String ERROR_CODE_HQDZXXYC              = "50578";
        /**
         * 支付宝支付出错
         */
        String ERROR_CODE_ZFBZFCC               = "50579";
        /**
         * 支付宝退款出错
         */
        String ERROR_CODE_ZFBTKCC               = "50580";
        /**
         * 店铺优惠券筛选失败
         */
        String ERROR_CODE_DPYHQSXSB             = "50581";
        /**
         * 店铺优惠券获取失败
         */
        String ERROR_CODE_DPYHQHQSB             = "50582";
        /**
         * 优惠券筛选失败
         */
        String ERROR_CODE_YHQSXSB               = "50583";
        /**
         * 优惠券筛选失败指定分类
         */
        String ERROR_CODE_YHQSXSBZDFL           = "50584";
        /**
         * 优惠券筛选失败指定商品
         */
        String ERROR_CODE_YHQSXSBZDSP           = "50585";
        /**
         * 平台优惠券筛选失败
         */
        String ERROR_CODE_PTYHQSXSB             = "50586";
        /**
         * 活动还未开始领取失败
         */
        String ERROR_CODE_HDHWKSLQSB            = "50587";
        /**
         * 活动已结束领取失败
         */
        String ERROR_CODE_HDYJSLQSB             = "50588";
        /**
         * 该优惠卷还未启动
         */
        String ERROR_CODE_GYHJHWQD              = "50589";
        /**
         * 您已经领取过了
         */
        String ERROR_CODE_NYJLQGL               = "50590";
        /**
         * 优惠卷不存在
         */
        String ERROR_CODE_YHJBCZ                = "50591";
        /**
         * 优惠券拆分订单失败
         */
        String ERROR_CODE_YHQCFDDSB             = "50592";
        /**
         * 优惠活动不存在
         */
        String ERROR_CODE_YHHDBCZ               = "50593";
        /**
         * 优惠券发行数量必须大于已领取数量
         */
        String ERROR_CODE_YHQFXSLBXDYYLQSL      = "50594";
        /**
         * 请选择优惠卷
         */
        String ERROR_CODE_QXZYHJ                = "50595";
        /**
         * 优惠卷不能为空
         */
        String ERROR_CODE_YHJBNWK               = "50596";
        /**
         * 优惠券名称已存在
         */
        String ERROR_CODE_YHQMCYCZ              = "50597";
        /**
         * 优惠卷面值不能为空
         */
        String ERROR_CODE_YHJMZBNWK             = "50598";
        /**
         * 优惠券使用门槛不能为空
         */
        String ERROR_CODE_YHQSYMJBNWK           = "50599";
        /**
         * 请输入正确的面值
         */
        String ERROR_CODE_QSRZQDMZ              = "50600";
        /**
         * 门槛必须大于面值
         */
        String ERROR_CODE_MJBXDYMZ              = "50601";
        /**
         * 折扣值不能为空
         */
        String ERROR_CODE_ZKZBNWK               = "50602";
        /**
         * 请输入正确的折扣值
         */
        String ERROR_CODE_QSRZQDZKZ             = "50603";
        /**
         * 等级不存在
         */
        String ERROR_CODE_DJBCZ                 = "50604";
        /**
         * 参数错误请使用商品
         */
        String ERROR_CODE_CSCWQSYSP             = "50605";
        /**
         * 列表中有商品不存在
         */
        String ERROR_CODE_LBZYSPBCZ             = "50606";
        /**
         * 请选择分类
         */
        String ERROR_CODE_QXZFL                 = "50607";
        /**
         * 请输入领取限制
         */
        String ERROR_CODE_QSRLQXZ               = "50608";
        /**
         * 优惠券剩余数量不足
         */
        String ERROR_CODE_YHQSYSLBZ             = "50609";
        /**
         * 优惠卷领取失败
         */
        String ERROR_CODE_YHJLQSB               = "50610";
        /**
         * 优惠卷赠送记录失败
         */
        String ERROR_CODE_YHJZSJLSB             = "50611";
        /**
         * 优惠卷数量扣减失败
         */
        String ERROR_CODE_YHJSLKJSB             = "50612";
        /**
         * 优惠劵活动不存在或者未启动
         */
        String ERROR_CODE_YHJHDBCZHZWQD         = "50613";
        /**
         * 字典不存在
         */
        String ERROR_CODE_ZDBCZ                 = "50614";
        /**
         * 生效状态禁止修改
         */
        String ERROR_CODE_SXZTJZXG              = "50615";
        /**
         * 字典名称不能为空
         */
        String ERROR_CODE_ZDMCBNWK              = "50616";
        /**
         * 数据名称存在
         */
        String ERROR_CODE_SJMCCZ                = "50617";
        /**
         * 字典值不能为空
         */
        String ERROR_CODE_ZDZBNWK               = "50618";
        /**
         * 字典值代码不能为空
         */
        String ERROR_CODE_ZDZDMBNWK             = "50619";
        /**
         * 字典值不存在
         */
        String ERROR_CODE_ZDZBCZ                = "50620";
        /**
         * 字典编码已存在
         */
        String ERROR_CODE_ZDBMYCZ               = "50621";
        /**
         * 字典目录不存在
         */
        String ERROR_CODE_ZDMLBCZ               = "50622";
        /**
         * 字典值已存在
         */
        String ERROR_CODE_ZDZYCZ                = "50623";
        /**
         * 字典编码不能为空
         */
        String ERROR_CODE_ZDBMBNWK              = "50624";
        /**
         * 字典已存在
         */
        String ERROR_CODE_ZDYCZ                 = "50625";
        /**
         * 字典明细不存在
         */
        String ERROR_CODE_ZDMXBCZ               = "50626";
        /**
         * 字典生效中无法删除
         */
        String ERROR_CODE_ZDSXZWFSC             = "50627";
        /**
         * 系统字典无法删除
         */
        String ERROR_CODE_XTZDWFSC              = "50628";
        /**
         * 该数据名称下有关联数据值无法删除
         */
        String ERROR_CODE_GSJMCXYGLSJZWFSC      = "50629";
        /**
         * 字典删除失败
         */
        String ERROR_CODE_ZDSCSB                = "50630";
        /**
         * 字典明细条目不存在
         */
        String ERROR_CODE_ZDMXTMBCZ             = "50631";
        /**
         * 字典生效中删除失败
         */
        String ERROR_CODE_ZDSXZSCSB             = "50632";
        /**
         * 该层级别正在使用无法删除
         */
        String ERROR_CODE_GCJBZZSYWFSC          = "50633";
        /**
         * 参数类型不正确
         */
        String ERROR_CODE_CSLXBZQ               = "50634";
        /**
         * 商品保存失败
         */
        String ERROR_CODE_SPBCSB                = "50635";
        /**
         * 商品规格库存保存失败
         */
        String ERROR_CODE_SPGGKCBCSB            = "50636";
        /**
         * 商品规格库存记录信息保存失败
         */
        String ERROR_CODE_SPGGKCJLXXBCSB        = "50637";
        /**
         * 请选择支持活动类型
         */
        String ERROR_CODE_QXZZCHDLX             = "50638";
        /**
         * 您有存在该商品请勿重复添加
         */
        String ERROR_CODE_NYCZGSPQWZFTJ         = "50639";
        /**
         * 请上传封面图
         */
        String ERROR_CODE_QSCFMT                = "50640";
        /**
         * 商品类别不在该品牌下
         */
        String ERROR_CODE_SPLBBZGPPX            = "50641";
        /**
         * 请填写属性
         */
        String ERROR_CODE_QTXSX                 = "50642";
        /**
         * 库存不能为空
         */
        String ERROR_CODE_KCBNWK                = "50643";
        /**
         * 属性保存失败
         */
        String ERROR_CODE_SXBCSB                = "50644";
        /**
         * 库存记录失败
         */
        String ERROR_CODE_KCJLSB                = "50645";
        /**
         * 库存预警记录失败
         */
        String ERROR_CODE_KCYJJLSB              = "50646";
        /**
         * 产品展示图上传失败
         */
        String ERROR_CODE_CPZSTSCSB             = "50647";
        /**
         * 请上传属性图片
         */
        String ERROR_CODE_QSCSXTP               = "50648";
        /**
         * 库存记录添加失败
         */
        String ERROR_CODE_KCJLTJSB              = "50649";
        /**
         * 商品上传失败
         */
        String ERROR_CODE_SPSCSB_001            = "50650";
        /**
         * 商品没有属性
         */
        String ERROR_CODE_SPMYSX                = "50651";
        /**
         * 请先去完善商品信息
         */
        String ERROR_CODE_QXQWSSPXX             = "50652";
        /**
         * 上架失败库存不足
         */
        String ERROR_CODE_SJSBKCBZ              = "50653";
        /**
         * 参数不完整
         */
        String ERROR_CODE_CSBWZ                 = "50654";
        /**
         * 非法数据
         */
        String ERROR_CODE_FFSJ                  = "50655";
        /**
         * 规则名称已存在请重新选择另一个名称
         */
        String ERROR_CODE_GZMCYCZQZXXZLYGMC     = "50656";
        /**
         * 运费添加修改失败
         */
        String ERROR_CODE_YFTJXGSB              = "50657";
        /**
         * 含有商品设置该运费规则不能删除
         */
        String ERROR_CODE_HYSPSZGYFGZBNSC       = "50658";
        /**
         * 您的店铺账户尚有进行中订单无法注销
         */
        String ERROR_CODE_NDDPZHSYJXZDDWFZX     = "50659";
        /**
         * 店铺注销失败
         */
        String ERROR_CODE_DPZXSB                = "50660";
        /**
         * 自营店不能删除
         */
        String ERROR_CODE_ZYDBNSC               = "50661";
        /**
         * 商城店铺配置未初始化
         */
        String ERROR_CODE_SCDPPZWCSH            = "50662";
        /**
         * 店铺未开启保证金
         */
        String ERROR_CODE_DPWKQBZJ              = "50663";
        /**
         * 保证金已经缴纳
         */
        String ERROR_CODE_BZJYJJN               = "50664";
        /**
         * 会员优惠计算失败
         */
        String ERROR_CODE_HYYHJSSB              = "50665";
        /**
         * 会员结算失败
         */
        String ERROR_CODE_HYJSSB                = "50666";
        /**
         * 未找到用户信息
         */
        String ERROR_CODE_WZDYHXX               = "50667";
        /**
         * 运费计算失败
         */
        String ERROR_CODE_YFJSSB                = "50668";
        /**
         * 创建订单号失败
         */
        String ERROR_CODE_CJDDHSB               = "50669";
        /**
         * 钱包支付失败
         */
        String ERROR_CODE_QBZFSB                = "50670";
        /**
         * 订单号为空
         */
        String ERROR_CODE_DDHWK                 = "50671";
        /**
         * 物流公司不正确
         */
        String ERROR_CODE_WLGSBZQ               = "50672";
        /**
         * 发货时快递单号已存在
         */
        String ERROR_CODE_FHSKDDHYCZ            = "50673";
        /**
         * 发货时快递单号输入错误
         */
        String ERROR_CODE_FHSKDDHSRCW           = "50674";
        /**
         * 发货时未填写快递单号
         */
        String ERROR_CODE_FHSWTXKDDH            = "50675";
        /**
         * 订单明细id不存在
         */
        String ERROR_CODE_DDMXBCZ               = "50676";
        /**
         * 确认收货失败
         */
        String ERROR_CODE_QRSHSB                = "50677";
        /**
         * 详单不存在
         */
        String ERROR_CODE_XDBCZ                 = "50678";
        /**
         * 发货失败
         */
        String ERROR_CODE_FHSB                  = "50679";
        /**
         * 获取物流信息异常
         */
        String ERROR_CODE_HQWLXXYC              = "50680";
        /**
         * 商品属性不存在
         */
        String ERROR_CODE_SPSXBCZ               = "50681";
        /**
         * 获取订单信息异常
         */
        String ERROR_CODE_HQDDXXYC              = "50682";
        /**
         * 支付回调出问题
         */
        String ERROR_CODE_ZFHDCWT               = "50683";
        /**
         * 预售支付回调异常
         */
        String ERROR_CODE_YSZFHDYC              = "50684";
        /**
         * 支付获取订单信息失败
         */
        String ERROR_CODE_ZFHQDDXXSB            = "50685";
        /**
         * 编辑订单失败
         */
        String ERROR_CODE_BJDDSB                = "50686";
        /**
         * 获取订单详情失败
         */
        String ERROR_CODE_HQDDXQSB              = "50687";
        /**
         * 获取支付配置信息
         */
        String ERROR_CODE_HQZFPZXX              = "50688";
        /**
         * 订单数据异常
         */
        String ERROR_CODE_DDSJYC                = "50689";
        /**
         * 获取支付配置信息失败
         */
        String ERROR_CODE_HQZFPZXXSB            = "50690";
        /**
         * 获取平台支付信息失败
         */
        String ERROR_CODE_HQPTZFXXSB            = "50691";
        /**
         * 推送订阅消息异常
         */
        String ERROR_CODE_TSDYXXYC              = "50692";
        /**
         * 请设置售后地址
         */
        String ERROR_CODE_QSZSHDZ               = "50693";
        /**
         * 订单已经结算
         */
        String ERROR_CODE_DDYJJS                = "50694";
        /**
         * 输入的金额有误请重新输入
         */
        String ERROR_CODE_SRDJEYWQZXSR          = "50695";
        /**
         * 门店编号不正确
         */
        String ERROR_CODE_MDBHBZQ               = "50696";
        /**
         * 店铺数据异常
         */
        String ERROR_CODE_DPSJYC                = "50697";
        /**
         * 退款失败店铺余额不足
         */
        String ERROR_CODE_TKSBDPYEBZ            = "50698";
        /**
         * 退款失败网络故障
         */
        String ERROR_CODE_TKSBWLGZ              = "50699";
        /**
         * 商品库存不存在
         */
        String ERROR_CODE_SPKCBCZ               = "50700";
        /**
         * 网络故障退款失败
         */
        String ERROR_CODE_WLGZTKSB              = "50701";
        /**
         * 快递公司不能为空
         */
        String ERROR_CODE_KDGSBNWK              = "50702";
        /**
         * 快递单号不能为空
         */
        String ERROR_CODE_KDDHBNWK              = "50703";
        /**
         * 售后订单不存在
         */
        String ERROR_CODE_SHDDBCZ               = "50704";
        /**
         * 详情不能为空
         */
        String ERROR_CODE_XQBNWK                = "50705";
        /**
         * 订单号不能为空
         */
        String ERROR_CODE_DDHBNWK               = "50706";
        /**
         * 未找到订单信息
         */
        String ERROR_CODE_WZDDDXX               = "50707";
        /**
         * 图片路径获取失败
         */
        String ERROR_CODE_TPLJHQSB              = "50708";
        /**
         * 图片分组不存在
         */
        String ERROR_CODE_TPFZBCZ               = "50709";
        /**
         * 图片外链上传失败
         */
        String ERROR_CODE_TPWLSCSB              = "50710";
        /**
         * 轮播图添加失败
         */
        String ERROR_CODE_LBTTJSB               = "50711";
        /**
         * 添加商品轮播图失败
         */
        String ERROR_CODE_TJSPLBTSB             = "50712";
        /**
         * 获取失败
         */
        String ERROR_CODE_HQSB                  = "50713";
        /**
         * 折扣获取失败
         */
        String ERROR_CODE_ZKHQSB                = "50714";
        /**
         * 获取用户信息失败
         */
        String ERROR_CODE_HQYHXXSB              = "50715";
        /**
         * 获取附近店铺失败
         */
        String ERROR_CODE_HQFJDPSB              = "50716";
        /**
         * 商品无库存信息
         */
        String ERROR_CODE_SPWKCXX               = "50717";
        /**
         * 商品已失效
         */
        String ERROR_CODE_SPYSX                 = "50718";
        /**
         * 签到流程网络异常
         */
        String ERROR_CODE_QDLCWLYC              = "50719";
        /**
         * 获取订单确认信息失败
         */
        String ERROR_CODE_HQDDQRXXSB            = "50720";
        /**
         * 操作频繁请稍后重试
         */
        String ERROR_CODE_CZPFQSHZS             = "50721";
        /**
         * 网络故障请稍后再试
         */
        String ERROR_CODE_WLGZQSHZS             = "50722";
        /**
         * 平台未配置短信服务
         */
        String ERROR_CODE_PTWPZDXFW             = "50723";
        /**
         * 您的上笔申请还未审核请稍后再试
         */
        String ERROR_CODE_NDSBSQHWSHQSHZS       = "50724";
        /**
         * 提现申请次数已达上限
         */
        String ERROR_CODE_TXSQCSYDSX            = "50725";
        /**
         * 用户与店铺不匹配
         */
        String ERROR_CODE_YHYDPBPP              = "50726";
        /**
         * 未获取到店铺配置信息
         */
        String ERROR_CODE_WHQDDPPZXX            = "50727";
        /**
         * 提现金额不能大于可提现金额
         */
        String ERROR_CODE_TXJEBNDYKTXJE         = "50728";
        /**
         * 提现金额大于现有金额
         */
        String ERROR_CODE_TXJEDYXYJE            = "50729";
        /**
         * 提现金额不能小于等于
         */
        String ERROR_CODE_TXJEBNXYDY            = "50730";
        /**
         * 提现金额不能低于最低提现金额
         */
        String ERROR_CODE_TXJEBNDYZDTXJE        = "50731";
        /**
         * 订单满减计算失败
         */
        String ERROR_CODE_DDMJJSSB              = "50732";
        /**
         * 添加满减记录失败
         */
        String ERROR_CODE_TJMJJLSB              = "50733";
        /**
         * 请先开通会员
         */
        String ERROR_CODE_QXKTHY                = "50734";
        /**
         * 请输入支付密码
         */
        String ERROR_CODE_QSRZFMM               = "50735";
        /**
         * 已被锁定请明天再试
         */
        String ERROR_CODE_YBSDQMTZS             = "50736";
        /**
         * 未设置支付密码
         */
        String ERROR_CODE_WSZZFMM               = "50737";
        /**
         * 支付密码不正确
         */
        String ERROR_CODE_ZFMMBZQ               = "50738";
        /**
         * 请输入正确的金额
         */
        String ERROR_CODE_QSRZQDJE              = "50739";
        /**
         * 用户余额不足不能进行扣除
         */
        String ERROR_CODE_YHYEBZBNJXKC          = "50740";
        /**
         * 用户消费积分不足不能进行扣除
         */
        String ERROR_CODE_YHXFJFBZBNJXKC        = "50741";
        /**
         * 用户积分不足不能进行扣除
         */
        String ERROR_CODE_YHJFBZBNJXKC          = "50742";
        /**
         * 钱包配置不存在
         */
        String ERROR_CODE_QBPZBCZ               = "50743";
        /**
         * 注册失败
         */
        String ERROR_CODE_ZCSB                  = "50744";
        /**
         * 注册失败网络繁忙
         */
        String ERROR_CODE_ZCSBWLFM              = "50745";
        /**
         * 用户为空
         */
        String ERROR_CODE_YHWK                  = "50746";
        /**
         * 积分支付失败
         */
        String ERROR_CODE_JFZFSB                = "50747";
        /**
         * 请求授权令牌失败
         */
        String ERROR_CODE_QQSQLPSB              = "50748";
        /**
         * 获取刷新接口调用令牌失败
         */
        String ERROR_CODE_HQSXJKDYLPSB          = "50749";
        /**
         * 请求预授权码失败
         */
        String ERROR_CODE_QQYSQMSB              = "50750";
        /**
         * 请求审核状态失败
         */
        String ERROR_CODE_QQSHZTSB              = "50751";
        /**
         * 小程序配置信息不存在
         */
        String ERROR_CODE_XCXPZXXBCZ            = "50752";
        /**
         * 目录名不能为空
         */
        String ERROR_CODE_MLMBNWK               = "50753";
        /**
         * 目录不存在
         */
        String ERROR_CODE_MLBCZ                 = "50754";
        /**
         * 目录名已存在
         */
        String ERROR_CODE_MLMYCZ                = "50755";
        /**
         * 目录创建失败
         */
        String ERROR_CODE_MLCJSB                = "50756";
        /**
         * 目录删除失败
         */
        String ERROR_CODE_MLSCSB                = "50757";
        /**
         * 分类删除失败
         */
        String ERROR_CODE_FLSCSB                = "50758";
        /**
         * 文件数据删除失败
         */
        String ERROR_CODE_WJSJSCSB              = "50759";
        /**
         * 缴纳金未缴
         */
        String ERROR_CODE_JNJWJ                 = "50760";
        /**
         * 您的店铺账户尚有进行中订单无法退还保证金
         */
        String ERROR_CODE_NDDPZHSYJXZDDWFTHBZJ  = "50761";
        /**
         * 您的店铺账户尚有上架的商品无法退还保证金
         */
        String ERROR_CODE_NDDPZHSYSJDSPWFTHBZJ  = "50762";
        /**
         * 未配置定位服务
         */
        String ERROR_CODE_WPZDWFW               = "50763";
        /**
         * 您已经成为店铺管理员不能申请
         */
        String ERROR_CODE_NYJCWDPGLYBNSQ        = "50764";
        /**
         * 店铺申请失败
         */
        String ERROR_CODE_DPSQSB                = "50765";
        /**
         * 店铺名称不合法
         */
        String ERROR_CODE_DPMCBHF               = "50766";
        /**
         * 无申请记录
         */
        String ERROR_CODE_WSQJL                 = "50767";
        /**
         * 请选择运费模板
         */
        String ERROR_CODE_QXZYFMB               = "50768";
        /**
         * 该商品不符合条件
         */
        String ERROR_CODE_GSPBFHTJ              = "50769";
        /**
         * 规格数量不能为
         */
        String ERROR_CODE_GGSLBNW               = "50770";
        /**
         * 列表中有规格不存在
         */
        String ERROR_CODE_LBZYGGBCZ             = "50771";
        /**
         * 出库数量不能大于等于当前库存数量
         */
        String ERROR_CODE_CKSLBNDYDYDQKCSL      = "50772";
        /**
         * 库存修改失败
         */
        String ERROR_CODE_KCXGSB                = "50773";
        /**
         * 商户未配置信息
         */
        String ERROR_CODE_SHWPZXX               = "50774";
        /**
         * 店铺信息修改失败
         */
        String ERROR_CODE_DPXXXGSB              = "50775";
        /**
         * 注销失败
         */
        String ERROR_CODE_ZXSB                  = "50776";
        /**
         * 请选择物流公司
         */
        String ERROR_CODE_QXZWLGS               = "50777";
        /**
         * 地址格式不正确
         */
        String ERROR_CODE_DZGSBZQ               = "50778";
        /**
         * 订单金额有误
         */
        String ERROR_CODE_DDJEYW                = "50779";
        /**
         * 订单修改失败
         */
        String ERROR_CODE_DDXGSB                = "50780";
        /**
         * 省市县不能为空
         */
        String ERROR_CODE_SSXBNWK               = "50781";
        /**
         * 添加运费失败
         */
        String ERROR_CODE_TJYFSB                = "50782";
        /**
         * 删除运费
         */
        String ERROR_CODE_SCYF                  = "50783";
        /**
         * 审核失败
         */
        String ERROR_CODE_SHSB_001              = "50784";
        /**
         * 评论不能为空
         */
        String ERROR_CODE_PLBNWK                = "50785";
        /**
         * 登录数据错误
         */
        String ERROR_CODE_DLSJCW                = "50786";
        /**
         * 请重新登录
         */
        String ERROR_CODE_QZXDL                 = "50787";
        /**
         * 获取用户信息异常
         */
        String ERROR_CODE_HQYHXXYC              = "50788";
        /**
         * 短信类型参数不存在
         */
        String ERROR_CODE_DXLXCSBCZ             = "50789";
        /**
         * 分销商不存在
         */
        String ERROR_CODE_FXSBCZ                = "50790";
        /**
         * 用户和推荐人不能为相同
         */
        String ERROR_CODE_YHHTJRBNWXT           = "50791";
        /**
         * 推荐人未变动
         */
        String ERROR_CODE_TJRWBD                = "50792";
        /**
         * 推荐人的上级不能为该用户的下级
         */
        String ERROR_CODE_TJRDSJBNWGYHDXJ       = "50793";
        /**
         * 推荐人不存在
         */
        String ERROR_CODE_TJRBCZ                = "50794";
        /**
         * 推荐人修改失败
         */
        String ERROR_CODE_TJRXGSB               = "50795";
        /**
         * 主号无法更改推荐人
         */
        String ERROR_CODE_ZHWFGGTJR             = "50796";
        /**
         * 分销等级修改失败
         */
        String ERROR_CODE_FXDJXGSB              = "50797";
        /**
         * 分销等级删除失败
         */
        String ERROR_CODE_FXDJSCSB              = "50798";
        /**
         * 等级名称不能为空
         */
        String ERROR_CODE_DJMCBNWK              = "50799";
        /**
         * 折扣请输入小于的值
         */
        String ERROR_CODE_ZKQSRXYDZ             = "50800";
        /**
         * 至少要有一个晋升条件
         */
        String ERROR_CODE_ZSYYYGJSTJ            = "50801";
        /**
         * 等级名称重复
         */
        String ERROR_CODE_DJMCZF                = "50802";
        /**
         * 升级条件不得与其他分销等级重复
         */
        String ERROR_CODE_SJTJBDYQTFXDJZF       = "50803";
        /**
         * 分销等级不存在
         */
        String ERROR_CODE_FXDJBCZ               = "50804";
        /**
         * 该等级存在分销商无法删除
         */
        String ERROR_CODE_GDJCZFXSWFSC          = "50805";
        /**
         * 层级不得大于
         */
        String ERROR_CODE_CJBDDY                = "50806";
        /**
         * 分销商品不存在
         */
        String ERROR_CODE_FXSPBCZ               = "50807";
        /**
         * 值不能为空
         */
        String ERROR_CODE_ZBNWK                 = "50808";
        /**
         * 直推或间推分销比例不能为空
         */
        String ERROR_CODE_ZTHJTFXBLBNWK         = "50809";
        /**
         * 该商品已被删除
         */
        String ERROR_CODE_GSPYBSC               = "50810";
        /**
         * 请输入正确的数量
         */
        String ERROR_CODE_QSRZQDSL              = "50811";
        /**
         * 出库库存不足
         */
        String ERROR_CODE_CKKCBZ                = "50812";
        /**
         * 入库库存不足
         */
        String ERROR_CODE_RKKCBZ                = "50813";
        /**
         * 活动还未开启
         */
        String ERROR_CODE_HDHWKQ                = "50814";
        /**
         * 活动已结束
         */
        String ERROR_CODE_HDYJS                 = "50815";
        /**
         * 签到失败
         */
        String ERROR_CODE_QDSB                  = "50816";
        /**
         * 您已经签到过了
         */
        String ERROR_CODE_NYJQDGL               = "50817";
        /**
         * 定金金额需低于属性售价
         */
        String ERROR_CODE_DJJEXDY               = "50818";
        /**
         * 预售数量需大于
         */
        String ERROR_CODE_YSSLXDY               = "50819";
        /**
         * 该商品上架后禁止更改类型数据
         */
        String ERROR_CODE_GSPSJHJZGGLXSJ        = "50820";
        /**
         * 该商品上架后禁止更改定金金额
         */
        String ERROR_CODE_GSPSJHJZGGDJJE        = "50821";
        /**
         * 请设置定金支付时间
         */
        String ERROR_CODE_QSZDJZFSJ             = "50822";
        /**
         * 请设置定金结束时间
         */
        String ERROR_CODE_QSZDJJSSJ             = "50823";
        /**
         * 定金结束时间不得在当前时间之前
         */
        String ERROR_CODE_DJJSSJBDZDQSJZQ       = "50824";
        /**
         * 支付定金开始时间需在结束时间之前
         */
        String ERROR_CODE_ZFDJKSSJXZJSSJZQ      = "50825";
        /**
         * 支付定金结束时间需在支付尾款时间之前
         */
        String ERROR_CODE_ZFDJJSSJXZZFWKSJZQ    = "50826";
        /**
         * 尾款支付时间不得设置当天
         */
        String ERROR_CODE_WKZFSJBDSZDT          = "50827";
        /**
         * 预售商品不可重复上架
         */
        String ERROR_CODE_YSSPBKZFSJ            = "50828";
        /**
         * 支付定金时间需在支付尾款时间之前
         */
        String ERROR_CODE_ZFDJSJXZZFWKSJZQ      = "50829";
        /**
         * 上架中的商品不可删除
         */
        String ERROR_CODE_SJZDSPBKSC            = "50830";
        /**
         * 删除预售商品失败
         */
        String ERROR_CODE_SCYSSPSB              = "50831";
        /**
         * 预售订单不存在
         */
        String ERROR_CODE_YSDDBCZ               = "50832";
        /**
         * 删除预售订单失败
         */
        String ERROR_CODE_SCYSDDSB              = "50833";
        /**
         * 待付款订单不可删除
         */
        String ERROR_CODE_DFKDDBKSC             = "50834";
        /**
         * 请输入定金预售规则
         */
        String ERROR_CODE_QSRDJYSGZ             = "50835";
        /**
         * 请输入订货预售规则
         */
        String ERROR_CODE_QSRDHYSGZ             = "50836";
        /**
         * 自动收货时间需超过天
         */
        String ERROR_CODE_ZDSHSJXCGT            = "50837";
        /**
         * 订单失效时间需超过小时
         */
        String ERROR_CODE_DDSXSJXCGXS           = "50838";
        /**
         * 订单售后时间需超过天
         */
        String ERROR_CODE_DDSHSJXCGT            = "50839";
        /**
         * 提醒限制需超过天
         */
        String ERROR_CODE_TXXZXCGT              = "50840";
        /**
         * 自动好评时间需超过天
         */
        String ERROR_CODE_ZDHPSJXCGT            = "50841";
        /**
         * 添加编辑失败
         */
        String ERROR_CODE_TJBJSB                = "50842";
        /**
         * 该商品已下架
         */
        String ERROR_CODE_GSPYXJ                = "50843";
        /**
         * 添加购物车失败
         */
        String ERROR_CODE_TJGWCSB               = "50844";
        /**
         * 评论内容不能为空
         */
        String ERROR_CODE_PLNRBNWK              = "50845";
        /**
         * 未找到该订单信息
         */
        String ERROR_CODE_WZDGDDXX              = "50846";
        /**
         * 评星超过范围
         */
        String ERROR_CODE_PXCGFW                = "50847";
        /**
         * 商品规格数据错误
         */
        String ERROR_CODE_SPGGSJCW              = "50848";
        /**
         * 已评论过了
         */
        String ERROR_CODE_YPLGL                 = "50849";
        /**
         * 请先评论后追评
         */
        String ERROR_CODE_QXPLHZP               = "50850";
        /**
         * 秒杀商品不存在
         */
        String ERROR_CODE_MSSPBCZ               = "50851";
        /**
         * 分销商品已被删除
         */
        String ERROR_CODE_FXSPYBSC              = "50852";
        /**
         * 返回结果为空
         */
        String ERROR_CODE_FHJGWK                = "50853";
        /**
         * 返回结果处理失败
         */
        String ERROR_CODE_FHJGCLSB              = "50854";
        /**
         * 获取请求结果失败
         */
        String ERROR_CODE_HQQQJGSB              = "50855";
        /**
         * 时间段不存在
         */
        String ERROR_CODE_SJDBCZ                = "50856";
        /**
         * 拼团商品不存在
         */
        String ERROR_CODE_PTSPBCZ               = "50857";
        /**
         * 标签名称不能为空
         */
        String ERROR_CODE_BQMCBNWK              = "50858";
        /**
         * 标签不存在
         */
        String ERROR_CODE_BQBCZ                 = "50859";
        /**
         * 显示中的标签不能修改
         */
        String ERROR_CODE_XSZDBQBNXG            = "50860";
        /**
         * 标签已存在
         */
        String ERROR_CODE_BQYCZ                 = "50861";
        /**
         * 删除失败标签必须要有一个为显示状态
         */
        String ERROR_CODE_SCSBBQBXYYYGWXSZT     = "50862";
        /**
         * 请先添加商品
         */
        String ERROR_CODE_QXTJSP                = "50863";
        /**
         * 标签下至少下要有一个显示商品
         */
        String ERROR_CODE_BQXZSXYYYGXSSP        = "50864";
        /**
         * 关闭失败标签必须要有一个为显示状态
         */
        String ERROR_CODE_GBSBBQBXYYYGWXSZT     = "50865";
        /**
         * 商品不能为空
         */
        String ERROR_CODE_SPBNWK                = "50866";
        /**
         * 结束时间不能小于当前时间
         */
        String ERROR_CODE_JSSJBNXYDQSJ          = "50867";
        /**
         * 时间段不能为空
         */
        String ERROR_CODE_SJDBNWK               = "50868";
        /**
         * 商品错误
         */
        String ERROR_CODE_SPCW                  = "50869";
        /**
         * 请选择商品规格
         */
        String ERROR_CODE_QXZSPGG               = "50870";
        /**
         * 列表有商品规格库存不足
         */
        String ERROR_CODE_LBYSPGGKCBZ           = "50871";
        /**
         * 库存添加失败
         */
        String ERROR_CODE_KCTJSB                = "50872";
        /**
         * 标签不能为空
         */
        String ERROR_CODE_BQBNWK                = "50873";
        /**
         * 请输入限购数量
         */
        String ERROR_CODE_QSRXGSL               = "50874";
        /**
         * 请输入预告时间
         */
        String ERROR_CODE_QSRYGSJ               = "50875";
        /**
         * 请输入活动消息推送时间
         */
        String ERROR_CODE_QSRHDXXTSSJ           = "50876";
        /**
         * 删除记录失败
         */
        String ERROR_CODE_SCJLSB                = "50877";
        /**
         * 时段数量必须大于等于个才能开启秒杀
         */
        String ERROR_CODE_SDSLBXDYDYGCNKQMS     = "50878";
        /**
         * 此商品已经产生订单删除商品失败
         */
        String ERROR_CODE_CSPYJCSDDSCSPSB       = "50879";
        /**
         * 删除商品失败
         */
        String ERROR_CODE_SCSPSB                = "50880";
        /**
         * 接口已过期
         */
        String ERROR_CODE_JKYGQ                 = "50881";
        /**
         * 秒杀配置错误
         */
        String ERROR_CODE_MSPZCW                = "50882";
        /**
         * 找不到秒杀配置
         */
        String ERROR_CODE_ZBDMSPZ               = "50883";
        /**
         * 手机格式不正确
         */
        String ERROR_CODE_SJGSBZQ               = "50884";
        /**
         * 添加地址失败
         */
        String ERROR_CODE_TJDZSB                = "50885";
        /**
         * 地址不能为空
         */
        String ERROR_CODE_DZBNWK                = "50886";
        /**
         * 移动失败请重试
         */
        String ERROR_CODE_YDSBQZS               = "50887";
        /**
         * 请重试
         */
        String ERROR_CODE_QZS                   = "50888";
        /**
         * 店铺未通过审核
         */
        String ERROR_CODE_DPWTGSH               = "50889";
        /**
         * 取消收藏
         */
        String ERROR_CODE_QXSZ                  = "50890";
        /**
         * 收藏失败
         */
        String ERROR_CODE_SZSB_001              = "50891";
        /**
         * 店铺地址不能为空
         */
        String ERROR_CODE_DPDZBNWK              = "50892";
        /**
         * 购买失败
         */
        String ERROR_CODE_GMSB                  = "50893";
        /**
         * 该账号已被注册
         */
        String ERROR_CODE_GZHYBZC               = "50894";
        /**
         * 该手机号码已经注册
         */
        String ERROR_CODE_GSJHMYJZC             = "50895";
        /**
         * 手机验证码不正确
         */
        String ERROR_CODE_SJYZMBZQ              = "50896";
        /**
         * 该手机号码未注册
         */
        String ERROR_CODE_GSJHMWZC              = "50897";
        /**
         * 两次密码不一致
         */
        String ERROR_CODE_LCMMBYZ               = "50898";
        /**
         * 密码与旧密码相同
         */
        String ERROR_CODE_MMYJMMXT              = "50899";
        /**
         * 退出失败
         */
        String ERROR_CODE_TCSB                  = "50900";
        /**
         * 出生日期只能修改一次
         */
        String ERROR_CODE_CSRQZNXGYC            = "50901";
        /**
         * 个人资料修改失败
         */
        String ERROR_CODE_GRZLXGSB              = "50902";
        /**
         * 新密码与原密码相同
         */
        String ERROR_CODE_XMMYYMMXT             = "50903";
        /**
         * 密码必须是六位
         */
        String ERROR_CODE_MMBXSLW               = "50904";
        /**
         * 新手机号和之前手机号一致
         */
        String ERROR_CODE_XSJHHZQSJHYZ          = "50905";
        /**
         * 身份校验失败
         */
        String ERROR_CODE_SFXYSB                = "50906";
        /**
         * 清理需要删除的优惠卷异常
         */
        String ERROR_CODE_QLXYSCDYHJYC          = "50907";
        /**
         * 清理已经结束的活动异常
         */
        String ERROR_CODE_QLYJJSDHDYC           = "50908";
        /**
         * 清理已经结束的优惠卷异常
         */
        String ERROR_CODE_QLYJJSDYHJYC          = "50909";
        /**
         * 佣金发放失败
         */
        String ERROR_CODE_YJFFSB                = "50910";
        /**
         * 佣金结算失败
         */
        String ERROR_CODE_YJJSSB                = "50911";
        /**
         * 定时处理签到异常
         */
        String ERROR_CODE_DSCLQDYC              = "50912";
        /**
         * 定时结算积分异常
         */
        String ERROR_CODE_DSJSJFYC              = "50913";
        /**
         * 定时清理店铺异常
         */
        String ERROR_CODE_DSQLDPYC              = "50914";
        /**
         * 收货人名称格式不正确位
         */
        String ERROR_CODE_SHRMCGSBZQW           = "50915";
        /**
         * 获取异常
         */
        String ERROR_CODE_HQYC                  = "50916";
        /**
         * 未找到店铺配置信息
         */
        String ERROR_CODE_WZDDPPZXX             = "50917";
        /**
         * 小程序未配置
         */
        String ERROR_CODE_XCXWPZ                = "50918";
        /**
         * 授权失败
         */
        String ERROR_CODE_SQSB                  = "50919";
        /**
         * 账号不存在
         */
        String ERROR_CODE_ZHBCZ_001             = "50920";
        /**
         * 更新购物车失败
         */
        String ERROR_CODE_GXGWCSB               = "50921";
        /**
         * 库存扣减失败
         */
        String ERROR_CODE_KCKJSB                = "50922";
        /**
         * 会员开通失败
         */
        String ERROR_CODE_HYKTSB                = "50923";
        /**
         * 余额支付失败
         */
        String ERROR_CODE_YEZFSB                = "50924";
        /**
         * 二维码获取失败
         */
        String ERROR_CODE_EWMHQSB               = "50925";
        /**
         * 分享海报背景图片未设置
         */
        String ERROR_CODE_FXHBBJTPWSZ           = "50926";
        /**
         * 未找到推广信息
         */
        String ERROR_CODE_WZDTGXX               = "50927";
        /**
         * 请选择需要上传的图片
         */
        String ERROR_CODE_QXZXYSCDTP            = "50928";
        /**
         * 密码未变化
         */
        String ERROR_CODE_MMWBH                 = "50929";
        /**
         * 密码已设置
         */
        String ERROR_CODE_MMYSZ                 = "50930";
        /**
         * 密码设置失败
         */
        String ERROR_CODE_MMSZSB                = "50931";
        /**
         * 新手机号不能和旧手机相同
         */
        String ERROR_CODE_XSJHBNHJSJXT          = "50932";
        /**
         * 提现失败请稍后重试
         */
        String ERROR_CODE_TXSBQSHZS_001         = "50933";
        /**
         * 理由不能为空
         */
        String ERROR_CODE_LYBNWK                = "50934";
        /**
         * 状态发生变化
         */
        String ERROR_CODE_ZTFSBH                = "50935";
        /**
         * 售后订单已撤销
         */
        String ERROR_CODE_SHDDYCX               = "50936";
        /**
         * 已过售后期
         */
        String ERROR_CODE_YGSHQ                 = "50937";
        /**
         * 商城未开自选商品
         */
        String ERROR_CODE_SCWKZXSP              = "50938";

        /**
         * 登录信息发生变化,请重新登录!
         */
        String ERROR_CODE_DLXXFSBH_QSXDL = "50939";

        /**
         * 提交失败,请重新编辑商品
         */
        String ERROR_CODE_TJSB_QCXBJSP   = "50940";
        /**
         * 删除天数不能小于0
         */
        String ERROR_CODE_SCTSBNXY0      = "50941";
        /**
         * 属性使用中无法删除
         */
        String ERROR_CODE_SXSYZ_CZSB     = "50942";
        /**
         * 请设置分销等级
         */
        String ERROR_CODE_QSZFXDJ        = "50943";
        /**
         * 商品状态发生变化,请刷新列表
         */
        String ERROR_CODE_SPZTFSBH_QSXLB = "50944";
        /**
         * 请输入购物享折扣
         */
        String ERROR_CODE_QSRGWZK        = "50945";
        /**
         * 可提现金额不能大于累计佣金
         */
        String ERROR_CODE_KTXJEBNDYLJYJ  = "50946";
        /**
         * 该品牌绑定了商品无法删除
         */
        String ERROR_CODE_GPPBDLFLWFSC   = "该品牌绑定了分类,无法删除";
        /**
         * 会员插件未开启
         */
        String ERROR_CODE_HYCJWKQ        = "50947";

        /**
         * 预警库存不能为零或小于零
         */
        String ERROR_CODE_YJKCBUNWL      = "51091";

        /**
         * 区号不合法
         */
        String ERROR_CODE_QHBHF = "51130";

        /**
         * 会员制插件code
         */
        interface MemberCode
        {
            /**
             * 弹窗关闭失败
             */
            String ERROR_CODE_TCGBSB = "60001";
        }

        /**
         * 店铺暂不支持发票功能
         */
        String ERROR_CODE_DPZBZCFPGN = "50948";

        /**
         * 该订单已申请开票
         */
        String ERROR_CODE_GDDYSQKP = "50949";

        /**
         * 供应商名称已存在
         */
        String ERROR_CODE_GYSMCYCZ = "50950";

        /**
         * 供应商账号已存在
         */
        String ERROR_CODE_GYSZHYCZ = "50951";

        /**
         * 供应商信息不存在
         */
        String ERROR_CODE_GYSXXBCZ = "50952";

        /**
         * 请先通过该分类的上级分类
         */
        String ERROR_CODE_QXTGGFLDSJFL = "50953";

        /**
         * 品牌所属分类不存在
         */
        String ERROR_CODE_PPSSFLBCZ = "50954";

        /**
         * 供应商账号不存在
         */
        String ERROR_CODE_GYSZHBCZ = "50955";

        /**
         * 供应商账号到期
         */
        String ERROR_CODE_GYSZHDQ = "50956";

        /**
         * 供应商账号锁定
         */
        String ERROR_CODE_GYSZHSD       = "50957";
        /**
         * 确认密码不能为空
         */
        String ERROR_CODE_QRMMBNWK      = "50958";
        /**
         * 新密码和旧密码相同
         */
        String ERROR_CODE_XMMHJMMXT     = "50959";
        /**
         * 确认密码与新密码需一致
         */
        String ERROR_CODE_QRMMYXMMXYZ   = "50960";
        /**
         * 店铺分类已存在
         */
        String ERROR_CODE_DPFLYCZ       = "50961";
        /**
         * 商品已售罄或未上架无法购买
         */
        String ERROR_CODE_SPYSQHWSJWFGM = "50962";

        /**
         * 会员插件未开启
         */
        String ERROR_CODE_YEYCGSX = "50963";

        /**
         * 请输入默认运费
         */
        String ERROR_CODE_QSRMRYFGZ     = "50964";
        /**
         * 用户已经创建店铺
         */
        String ERROR_CODE_DUPLICATE     = "50965";
        /**
         * 请上传身份证正反面/营业执照
         */
        String ERROR_CODE_QSCSFZZFMYYZZ = "50966";

        /**
         * 订单未付款,不能修改
         */
        String ERROR_CODE_DDWFKBNXG = "50967";

        /**
         * 达到秒杀商品上限
         */
        String ERROR_CODE_DDMSSPSX = "50968";

        /**
         * 重复下单
         */
        String ERROR_CODE_CFXD = "50969";

        /**
         * 一级菜单不可设置为tab页面
         */
        String ERROR_CODE_YJCDBKSZWTABYM = "50970";

        /**
         * 店铺已打烊
         */
        String ERROR_CODE_DPYDY = "50971";

        /**
         * 请选择店铺分类
         */
        String ERROR_CODE_QXZDPFL = "50972";

        /**
         * 请勿提交空白文件
         */
        String ERROR_CODE_QWTJKBWJ = "50973";

        /**
         * 地址超出配送范围
         */
        String ERROR_CODE_DZCCPSFW = "50974";

        /**
         * 该商品已违规下架，上架失败
         */
        String ERROR_CODE_GSPYWGXJSJSB = "50975";

        /**
         * 该商品已断供，上架失败
         */
        String ERROR_CODE_GSPYDGSJSB = "50976";

        /**
         * 插件未配置
         */
        String ERROR_CODE_CJWPZ = "50977";

        /**
         * 插件未开启
         */
        String ERROR_CODE_CJWKQ = "50978";

        /**
         * 当前店铺有未退还的保证金
         */
        String ERROR_CODE_DQDPYWTHDBZJ      = "50979";
        /**
         * 请选择开通用户
         */
        String ERROR_CODE_QXZKTYH           = "50980";
        /**
         * 到期时间需在当前时间之后
         */
        String ERROR_CODE_DQSJXZDQSJZH      = "50981";
        /**
         * 审核中的商品不可删除
         */
        String ERROR_CODE_SHZDSPBKSC        = "50982";
        /**
         * 定金模式商品已过尾款时间不可上架
         */
        String ERROR_CODE_DJMSSPYGWKSJBKSJ  = "50983";
        /**
         * 订货模式商品已过截止时间不可上架
         */
        String ERROR_CODE_DHMSSPYGJZSJBKSJ  = "50984";
        /**
         * 活动结束时间必须大于开始时间
         */
        String ERROR_CODE_HDJSSJBXDYKSSJ    = "50985";
        /**
         * 申请信息未更新,请勿重复提交
         */
        String ERROR_CODE_SQXXWGXQWCXTJ     = "50986";
        /**
         * 秒杀价格不能大于售价
         */
        String ERROR_CODE_MSJGBNDYSJ        = "50987";
        /**
         * 非自营店商品不可删除
         */
        String ERROR_CODE_FZYDSPBKSC        = "50988";
        /**
         * 店铺默认宣传图不能为空
         */
        String ERROR_CODE_DPMRXCTBNWK       = "50989";
        /**
         * 请输入入驻审核时间
         */
        String ERROR_CODE_QSRRZSHSJ         = "50990";
        /**
         * 开通设置不得为空
         */
        String ERROR_CODE_KTSZBNWK          = "50991";
        /**
         * 请完善开通设置信息
         */
        String ERROR_CODE_QWSKTSZ           = "50992";
        /**
         * 会员方式价格需大于0
         */
        String ERROR_CODE_HYFSJGXDYZ        = "50993";
        /**
         * 会员开通价格不得超过8位数
         */
        String ERROR_CODE_HYKTJGBDCGEWS     = "50994";
        /**
         * 会员权益设置不得为空
         */
        String ERROR_CODE_HYQYSZBDWK        = "50995";
        /**
         * 会员生日特权开启时,获得积分倍数不得为空
         */
        String ERROR_CODE_HYSRTQKQSJFBSBDWK = "50996";
        /**
         * 获得积分倍数需大于0
         */
        String ERROR_CODE_JFBSXDYZ          = "50997";
        /**
         * 获得积分倍数不得超过8位数
         */
        String ERROR_CODE_JFBSBDCGEWS       = "50998";
        /**
         * 会员折扣设置不得为空
         */
        String ERROR_CODE_HYZKSZBDWK        = "50999";
        /**
         * 会员折扣设置需在1-10之间
         */
        String ERROR_CODE_HYZKSZXZOTZJ      = "51000";
        /**
         * 续费提醒天数设置不得为空
         */
        String ERROR_CODE_XFTXTSSZBDWK      = "51001";
        /**
         * 续费提醒设置不能超过2位数
         */
        String ERROR_CODE_XFTXSZBNCGTWS     = "51002";
        /**
         * 该分类下暂未绑定商品
         */
        String ERROR_CODE_GFLXZWBDSP        = "51003";
        /**
         * 过期时间不能小于当前时间
         */
        String ERROR_CODE_GQSJBNXYDQSJ      = "51004";
        /**
         * 相同商品数量需大于0
         */
        String ERROR_CODE_XTSPSLXDYZ        = "51005";
        /**
         * 当前时间已有限时折扣活动！
         */
        String ERROR_CODE_DQSJYYZKHD        = "51006";

        /**
         * 限时折扣时间已结束
         */
        String ERROR_CODE_XSZKSJYJS = "51007";

        /**
         * 请添加自营店
         */
        String ERROR_CODE_QTJZYD = "51008";

        /**
         * 优惠劵使用中，有订单未支付无法删除记录
         */
        String ERROR_CODE_YHJSYZYDDWZFWFSC = "51009";

        /**
         * 请输入加购价
         */
        String ERROR_CODE_QSRJGJ = "51010";

        /**
         * 请输入折扣值
         */
        String ERROR_CODE_QSRZKZ = "51011";

        /**
         * 请输入购买上限
         */
        String ERROR_CODE_QSRGMSZ = "51012";

        /**
         * 管理员账号重复
         */
        String ERROR_CODE_GLYZHCF = "51013";

        /**
         * 重复操作
         */
        String ERROR_CODE_CFCZ = "51014";

        /**
         * 商品删除失败,商品加入拼团活动，对应拼团活动未结束
         */
        String ERROR_CODE_PTHDWJS = "51015";

        /**
         * 商品删除失败,商品加入竞拍活动，对应竞拍活动未结束
         */
        String ERROR_CODE_JPHDWJS = "51016";

        /**
         * 最小提现金额至少需要0.01
         */
        String ERROR_CODE_ZXTXJEZSXY = "51017";

        /**
         * 接收方不能为空
         */
        String ERROR_CODE_JSFBNWK = "51018";

        /**
         * 拼团活动已存在
         */
        String ERROR_CODE_PTHSYCZ = "51019";

        /**
         * 该类型在该时间段中已存在生效中的公告
         */
        String ERROR_CODE_GLXZGSJDZYCZSXDGG = "51020";

        /**
         * 快递100未配置请联系管理员
         */
        String ERROR_CODE_KD100WPZ = "51021";

        /**
         * 商城电子面单未配置请联系管理员
         */
        String ERROR_CODE_PTDZMDWPZ = "51022";

        /**
         * 该物流公司已配置电子面单
         */
        String ERROR_CODE_GWLGSYPZDZMD = "51023";


        /**
         * 请配置正确的电子面单数据
         */
        String ERROR_CODE_QPZJQDDZMDSJ = "51024";

        /**
         * oss无删除权限
         */
        String ERROR_CODE_OOSWSCQX = "51025";

        /**
         * 续费提醒天数需大于零
         */
        String ERROR_CODE_XFTXTSXDYL = "51026";

        /**
         * 请先进行微信绑定
         */
        String ERROR_CODE_QXJXWXBD = "51027";


        /**
         * 该微信已绑定其他账号
         */
        String ERROR_CODE_GWXYBDQTZH = "51028";

        /**
         * 提现金额大于2000请填写真实姓名
         */
        String ERROR_CODE_QTXZSXM = "51029";

        /**
         * V3微信余额提现未配置
         */
        String ERROR_CODE_WXYRTXWPZ = "51030";

        /**
         * 已付款，请勿重复支付
         */
        String ERROR_CODE_YFKQECFZF = "51031";

        /**
         * 优惠劵已使用请重新下单
         */
        String ERROR_CODE_YHJYSYONG = "51032";

        /**
         * 请勿重复请求
         */
        String ERROR_CODE_QWCFTJ = "51033";

        /**
         * 请稍后再试
         */
        String ERROR_CODE_QWSHZS = "51034";

        /**
         * 参团折扣不能为空
         */
        String ERRCODE_CTZKBNWK = "51034";

        /**
         * 团长折扣不能为空
         */
        String ERRCODE_TZZKBNWK = "51035";

        /**
         * 参团优惠必须小于团长优惠
         */
        String ERRCODE_CTYHBXXYTZYH = "51036";

        /**
         * 关系绑定设置不能修改,请联系管理员
         */
        String ERRCODE_GXBDSZBNXZ = "51038";

        /**
         * 只能发货自己店铺的商品
         */
        String ERRCODE_ZNFHZJDPDSP = "51039";

        /**
         * 账户已存在，请重新输入
         */
        String ERRCODE_ZHYCZQCXSR = "51040";

        /**
         * 请先成为分销商
         */
        String ERRCODE_QXCWFXS = "51041";

        /**
         * 订单已被删除，无法查看详情
         */
        String ERRCODE_DDYBSC = "51042";

        /**
         * 该商户使用时间已到期，请先修改到期时间再启用！
         */
        String ERRCODE_GSHSYSJYDQ = "51043";

        /**
         * 该供应商账户余额未提现
         */
        String ERRCODE_GGYSZHYEWTX = "51044";

        /**
         * 该供应商存在代发货订单
         */
        String ERRCODE_GGYSCZDFHDD = "51045";

        /**
         * 请填写排序值
         */
        String ERRCODE_QTXPXZ = "51046";

        /**
         * 请设置必填项
         */
        String ERRCODE_QSZBTX = "51047";

        /**
         * 请输入增加库存值
         */
        String ERRCODE_QSRZJKCZ = "51048";


        /**
         * 优惠劵活动已过期
         */
        String ERROR_CODE_YHJHDYGQ = "51049";

        /**
         * 请完善属性
         */
        String ERRCODE_QWSSX = "51050";

        /**
         * 锁定设置不能为空
         */
        String ERRCODE_SDSZBNWK = "51051";


        /**
         * 请选择预约时间
         */
        String ERRCODE_QXZYYSJ = "51052";

        /**
         * 当前预约时间以约满,请重新选择
         */
        String ERRCODE_DQYYSJYRM = "51053";

        /**
         * 属性核销次数不能为空
         */
        String ERROR_SCHXCSBNWK = "51054";

        /**
         * 请先结束正在进行中的直播
         */
        String ERROR_YYZZBDZB = "51055";

        /**
         * 核销次数用完
         */
        String ERROR_HXCSYW = "51056";

        /**
         * 不是正在直播状态
         */
        String ERROR_BSZZZBZT = "51057";

        /**
         * 当前账号正在直播，请在主播中心入口进入
         */
        String ERROR_DQYZZBJ = "51058";

        /**
         * 添加的核销时间重叠
         */
        String ERROR_TJDSJCD = "51059";

        /**
         * 该商品应在其他门店核销
         */
        String ERROR_GSPYZQTMDHX = "51060";

        /**
         * 该门店下还有订单未关闭
         */
        String ERROR_GMDXHYDDWGB = "51061";

        /**
         * 线下核销商品不能重复添加购物车
         */
        String ERROR_XXHXSPBNCFTJGWC = "51062";

        /**
         * 已经是商品的最后一个门店不可删除
         */
        String ERROR_YJSSPDZHYGMDBKSC = "51063";

        /**
         * 购物车中已存在该商品规格
         */
        String ERROR_GWCZYCZGSPGG = "51064";

        /**
         * 还有未支付的需预约的虚拟订单，请先支付或取消
         */
        String ERROR_YWZFDXYYDD = "51065";

        /**
         * 退款金额应该小于应退金额
         */
        String ERROR_TKJEYGXYYTJE = "51066";

        /**
         * 商品已被主播加入直播间，暂时无法被移除
         */
        String ERROR_SPYBZBJRZBJ = "51067";

        /**
         * 已超参团限制
         */
        String ERRCODE_YCCTXZ = "51068";

        /**
         * 已超参团限制
         */
        String ERRCODE_YCKTXZ = "51069";

        /**
         * 配送员姓名不能为空
         */
        String ERRCODE_PSYXMBNWK = "51070";

        /**
         * 请填写配送信息
         */
        String ERRCODE_QTXPSXX = "51071";

        /**
         * 充值金额/积分不能大于9位数
         */
        String ERRCODE_YEHJFDDSX = "51072";

        /**
         * 文件名称重复
         */
        String ERRCODE_WJMCCF = "51073";

        /**
         * 默认logo不能为空
         */
        String ERROR_CODE_MRLGBNWK = "51074";

        /**
         * 指定运费和不发地区重复
         */
        String ERROR_CODE_ZDYFHBFDQCF = "51075";

        /**
         * 直播价不能低于成本价
         */
        String ERROR_CODE_ZBJBNDYCBJ = "51076";

        /**
         * 手机号与账号不匹配
         */
        String ERRCODE_SJHYZHBPP = "51077";

        /**
         * 供应商提现设置未配置
         */
        String ERRCODE_GYSTXSZWPZ = "51078";

        /**
         * 售价必须大于供货价
         */
        String ERRCODE_SJBXDYGHJ = "51079";

        /**
         * 请输入分类名称
         */
        String ERRCODE_QSRDPMC = "51080";

        /**
         * 请选择是否显示
         */
        String ERRCODE_QXZSFXX = "51081";

        /**
         * 状态为MERCHANT_ID时name必填
         */
        String ERRCODE_ZTWMERCHANTIDSNAMEBT = "51082";

        /**
         * 商城电子面单未配置模版ID
         */
        String ERROR_CODE_PTDZMDWPZMBID = "51083";

        /**
         * 有敏感词
         */
        String ERROR_CODE_YMGC = "51084";

        /**
         * partnerId不能为空
         */
        String ERROR_CODE_PARTHER = "51085";

        /**
         * 商品副标题不能为空
         */
        String ERROR_CODE_SPFBTBNWK = "51086";

        /**
         * 非自营店不能添加菜单，请联系管理员
         */
        String ERROR_CODE_FZYWFTJCDQLXGLY = "51087";

        /**
         * 系统检测该品牌名称已存在，已自动拒绝
         */
        String ERROR_CODE_XTJCGPPMCYCZ = "51088";

        /**
         * 系统检测该分类名称已存在，已自动拒绝
         */
        String ERROR_CODE_XTJCGFLMCYCZ = "51089";

        /**
         * 系统检测该分类名称已存在，已自动拒绝
         */
        String ERROR_CODE_FLYCZ = "51090";

        /**
         * 分销商品未设置自定义分销规则
         */
        String ERROR_CODE_FXSPWSZDIYRULE = "51092";

        /**
         * 加购商品价格变动
         */
        String ERROR_CODE_JGSPBD = "51093";

        /**
         * 贝宝回调失败
         */
        String ERROR_CODE_BBHDSB = "51094";


        /**
         * 积分抵扣超过最大支付金额
         */
        String ERROR_CODE_JFDKCW = "51095";

        /**
         * 比例设置发生改变
         */
        String ERROR_CODE_BLSZFSGB = "51096";

        /**
         * 邮箱格式错误
         */
        String ERROE_CODE_YXGSCW = "51097";

        /**
         * 邮箱发送频率过高
         */
        String ERROR_CODE_YXFSPLGG = "51098";

        /**
         * 邮箱不存在
         */
        String ERROR_CODE_YXBCZ = "51099";

        /**
         * 邮箱发送失败
         */
        String ERROR_CODE_YXFSSB = "51100";

        /**
         * 邮箱已被注册
         */
        String ERR0R_CODE_YXYBZC = "51101";

        /**
         * 该邮箱与当前绑定的邮箱相同
         */
        String ERROR_CODE_GYXYDQBDYXXT = "51102";

        /**
         *平台图片配置未配置
         */
        String ERROR_CODE_PTTPWPZ = "51103";

        /**
         * 请先创建门店
         */
        String ERROR_CODE_QXCJMD = "51104";

        /**
         * 请输入正确的短信模板信息
         */
        String ERROR_CODE_QSRZQDMBXX = "51105";

        /**
         * 登录失败，请输入正确的邮箱号
         */
        String ERROR_CODE_DLSBQSRZQDYXH = "51106";

        /**
         * 请上传正确的导入模板
         */
        String ERROR_CODE_QSCZQDRMB = "51107";
        /**
         * 语种已存在
         */
        String ERROR_CODE_YZYCZ = "51108";

        /**
         * 请选择语种
         */
        String ERROR_CODE_QXZYZ = "51109";

        /**
         * 国家已存在
         */
        String ERROR_CODE_GJYCZ = "51110";

        /**
         * 地区已存在
         */
        String ERROR_CODE_DQYCZ = "51111";

        /**
         * 导入失败
         */
        String ERROR_CODE_DRSB = "51112";

        /**
         * 过期时间必须晚于当前时间
         */
        String ERROR_CODE_GQSJBXWYDQSJ = "51113";

        /**
         * 短信类别已存在
         */
        String ERROR_CODE_DXLBYCZ = "51114";

        /**
         * 请输入PayPall邮箱地址
         */
        String ERROR_CODE_QSRPPYXDZ = "51115";

        /**
         * 商城未设置语种
         */
        String ERROR_CODE_SCWSZYZ = "51116";

        /**
         * 手机号或密码不匹配
         */
        String ERROR_CODE_SJHHMMBPP = "51117";

        /**
         * 店铺提现未配置
         */
        String ERROR_CODE_DPTXWPZ = "51118";

        /**
         * 手机号不存在
         */
        String ERROE_CODE_SJHBCZ = "51119";

        /**
         * 团队已满
         */
        String ERROR_CODE_TDYM = "51120";

        /**
         * 运费模板发送改变，请重新支付
         */
        String ERROR_CODE_YFMBFSGB = "51121";

        /**
         * 加购价需小于零售价
         */
        String ERROR_CODE_JGJXXYLSJ = "51122";


        /**
         * 默认商城未设置货币信息
         */
        String ERROR_CODE_MRSCWSZHBXX = "51123";


        /**
         * 邮箱或密码不匹配
         */
        String ERROR_CODE_YXHMMBPP = "51124";

        /**
         * 订单正在处理中，请勿重复提交
         */
        String ERROR_CODE_DDZZCLZ = "51125";

        /**
         * 订单已支付
         */
        String ERROR_CODE_DDYZF = "51126";

        /**
         * 验证码错误，请重新输入
         */
        String ERROR_CODE_YZMCWQCXSR = "51127";

        /**
         * 当前订单已在其他设备处理中
         */
        String ERROR_CODE_DQYHYZQTSBDL = "51128";

        /**
         * 商城名称已存在
         */
        String ERROR_CODE_SCMCCZ = "51129";

        /**
         * 货币不存在
         */
        String ERROR_CODE_HBBCZ = "51131";

        /**
         * 自提订单无需发货
         */
        String ERROR_CODE_ZTDDWXFH = "51132";

        /**
         * 配送订单不可使用批量发货模板
         */
        String ERROR_CODE_PSDDBKSYPLFH = "51133";

        /**
         * 商城运行过程中不允许修改默认货币
         */
        String ERROR_CODE_SCYSZMRHB = "51134";

        /**
         * 超过该订单最大抵扣积分！
         */
        String ERROR_CODE_CGGDDZDDKJF = "51135";

        /**
         * 订单已关闭，请勿重复提交
         */
        String ERROR_CODE_DDYGB = "51136";

        /**
         * 页面名称已存在
         */
        String ERROR_CODE_YMMCYCZ = "51137";

        /**
         * 链接不能为空
         */
        String ERROR_CODE_LJDZBNWK = "51138";

        /**
         * 请输入正确的链接地址
         */
        String ERROR_CODE_QSRZQDLJDZ = "51139";

        /**
         * PayPal不支持的货币
         */
        String ERROR_CODE_PAYPALBZCHB = "51140";

        /**
         * 提现类型不存在
         */
        String ERROR_CODE_TXLBWZ = "51141";

        /**
         * 系统主题图片不可删除
         */
        String ERROR_CODE_XTZTTPBKSC = "51141";

        /**
         * stripe不支持的货币
         */
        String ERROR_CODE_STRIPEBZCHB = "51142";

        /**
         * OpenID不存在
         */
        String ERROR_CODE_OPENIDBCZ = "51143";

        /**
         * 货币汇率已变更，请重新下单
         */
        String ERROR_CODE_HBHLYBG = "51144";

        /**
         * 低于最低支付金额
         */
        String ERROR_CODE_DYZDZFJE = "51145";

        /**
         * 未设置默认币种
         */
        String ERROR_CODE_WSZMRBZ = "51146";

        /**
         * 未设置默认语种
         */
        String ERROR_CODE_WSZMRYZ = "51147";

        /**
         * 该币种已存在
         */
        String ERROR_CODE_BZYCZ = "51149";

        /**
         * 获取页面数据失败
         */
        String ERROR_CODE_HQYMSJSB = "51150";

        /**
         * 获取微信支付平台序列号证书失败
         */
        String ERROR_CODE_HQZSSB = "51151";


        /**
         * 请先配置邮箱服务
         */
        String ERROR_CODE_QXPZYXFW = "51152";


        /**
         * 该分类已绑定店铺,请解绑后操作
         */
        String ERROR_CODE_DPFLZZSY      = "51153";

        /**
         *该分类正在使用，不能删除！
         */
        String ERROR_CODE_FLZZSY = "51154";

        /**
         * 话题名称不能为空
         */
        String ERROR_CODE_HTMCBNWK = "51155";

        /**
         * 话题名称已存在
         */
        String ERROR_CODE_HTMCYCZ = "51156";


        /**
         * 请在管理后台配置云点播参数
         */
        String ERROR_CODE_YDBPZWK = "51157";

        /**
         * 请配置cdn域名播放地址
         */
        String ERROR_CODE_QPZCDNDZ = "51158";

        /**
         * 线下核销或需预约商品不可加入购物车
         */
        String ERROR_CODE_BKJRGWC = "51159";

        /**
         * 上传文件数量超出限制
         */
        String ERROR_CODE_TOO_MANY_FILES = "51160";

        /**
         * 文件大小超限
         */
        String ERROR_CODE_FILE_TOO_LARGE = "51161";

        /**
         * 文件类型不支持
         */
        String ERROR_CODE_UNSURPPORTED_FILETYPE = "51162";

        /**
         * 文件上传校验失败（通用）
         */
        String ERROR_CODE_FILE_VALIDATION_FAILED = "51163";

        /**
         * 上传总大小超出限制
         */
        String ERROR_CODE_TOTAL_SIZE_EXCEEDED = "51164";

        /**
         * 17Track 密钥未配置
         */
        String ERROR_CODE_TRACKMYWPZ = "51165";

        /**
         * 17 track 发货失败
         */
        String ERROR_CODE_TRACKFHSB = "51166";

        /**
         * 成本价需低于售价
         */
        String ERROR_CODE_CBJXDYSJ = "51167";

        /**
         * 售价不能为空
         */
        String ERROR_CODE_SJBNWK = "51168";

        /**
         * 商品标题不能超过20个中文字长度
         */
        String ERROR_CODE_SPBTCD = "51169";

        /**
         * 商品标题不能为空
         */
        String ERROR_CODE_SPBTBNWK = "51170";

        /**
         * 关键词不能为空
         */
        String ERROR_CODE_GJCBNWK = "51171";

        /**
         * 销量请输入整数
         */
        String ERROR_CODE_XLQSRZS = "51172";

        /**
         * 重量不能为负数
         */
        String ERROR_CODE_ZLBNWFS = "51173";

        /**
         * 商品类别不能为空
         */
        String ERROR_CODE_SPLBBNWK = "51174";

        /**
         * 请上传商品图片
         */
        String ERROR_CODE_QSCSPTP = "51175";

        /**
         * 成本价不能为空
         */
        String ERROR_CODE_CBJBNWK = "51177";

        /**
         * 原价不能为空
         */
        String ERROR_CODE_YJBNWK = "51178";

        /**
         * 属性图不能为空
         */
        String ERROR_CODE_SXTBNWK = "51180";

        /**
         * 请先设置数据库备份配置
         */
        String ERROR_CODE_QXSZSJKBFPZ = "51181";

        /**
         * 请先添加售后地址
         */
        String ERROR_CODE_QXTJSHDZ = "51182";

        /**
         * 竞拍插件code
         *
         * @author Trick
         * @date 2022/7/1 17:04
         */
        interface PluginAuctionCode
        {
            /**
             * 专场名称不能为空
             */
            String ERROR_CODE_ZCMCBNWK       = "100001";
            /**
             * 专场保证金必须大于0
             */
            String ERROR_CODE_ZCBZJBNWK      = "100002";
            /**
             * 报名截至时间不能为空
             */
            String ERROR_CODE_BMJZSJBNWK     = "100003";
            /**
             * 开始时间不能为空
             */
            String ERROR_CODE_KSSJBNWK       = "100004";
            /**
             * 结束时间不能为空
             */
            String ERROR_CODE_JSJZSJBNWK     = "100005";
            /**
             * 开始时间不能大于结束时间
             */
            String ERROR_CODE_KSSJBNDYJSSJ   = "100006";
            /**
             * 开始时间不能小于报名截止时间
             */
            String ERROR_CODE_KSSJBNXYBMJZSJ = "100007";
            /**
             * 专场图片不能为空
             */
            String ERROR_CODE_ZCTPBNWK       = "100008";
            /**
             * 专场预告不能为空
             */
            String ERROR_CODE_ZCYGBNWK       = "100009";
            /**
             * 专场名称已存在
             */
            String ERROR_CODE_ZCMCYCZ        = "100010";
            /**
             * 专场佣金不能为空
             */
            String ERROR_CODE_ZCYJBNWK       = "100011";

            /**
             * 场次名称不能为空
             */
            String ERROR_CODE_CCMCBNWK       = "100012";
            /**
             * 专场ID不能为空
             */
            String ERROR_CODE_ZCIDBNWK       = "100013";
            /**
             * 场次商品规格id不能为空
             */
            String ERROR_CODE_ZCSPGGIDBNWK   = "100014";
            /**
             * 竞拍商品不存在
             */
            String ERROR_CODE_JPSPBNWK       = "100015";
            /**
             * 场次名称已存在
             */
            String ERROR_CODE_CCMCYCZ        = "100016";
            /**
             * 商品已参加活动
             */
            String ERROR_CODE_SPYCJHD        = "100017";
            /**
             * 只有未开始的活动才能操作
             */
            String ERROR_CODE_ZYWKSDHDCNCZ   = "100018";
            /**
             * 开始时间不能大于专场时间
             */
            String ERROR_CODE_KSSHBNDYZCSJ   = "100019";
            /**
             * 列表有商品已存在
             */
            String ERROR_CODE_LBYSPYCZ       = "100020";
            /**
             * 已缴纳过保证金
             */
            String ERROR_CODE_YJNGBZJ        = "100021";
            /**
             * 出价失败
             */
            String ERROR_CODE_CJSB           = "100022";
            /**
             * 已截拍
             */
            String ERROR_CODE_YJP            = "100023";
            /**
             * 活动还未开始
             */
            String ERROR_CODE_HDHWKS         = "100024";
            /**
             * 未缴纳过保证金
             */
            String ERROR_CODE_WJNGBZJ        = "100025";
            /**
             * 只有未开始的才能删除
             */
            String ERROR_CODE_ZYWKSDCNSC     = "100026";
            /**
             * 起拍价默认值不能为空
             */
            String ERROR_CODE_MRQPJZBNWK     = "100027";
            /**
             * 单位不能为空
             */
            String ERROR_CODE_DWBNWK         = "100028";
            /**
             * 默认加价幅度不能为空
             */
            String ERROR_CODE_MRJJFDBNWK     = "100029";
            /**
             * 该专场非报名专场
             */
            String ERROR_CODE_GZCFBMZC       = "100030";
            /**
             * 进行中的活动不能操作
             */
            String ERROR_CODE_JXZDHDBNCZ     = "100031";
            /**
             * 已设置提醒
             */
            String ERROR_CODE_YSZTX          = "100032";
            /**
             * 竞拍未配置
             */
            String ERROR_CODE_JPWPZ          = "100033";
            /**
             * 竞拍专场开始不能添加场次
             */
            String ERROR_CODE_JPZCYKSBNTJCC  = "100034";
            /**
             * 专场开始时间不能小于当前时间
             */
            String ERROR_CODE_ZCKSSJBNXYDQSJ = "100035";
        }

    }

    /**
     * 容器类错误
     */
    interface ContainerErrorCode
    {
        // 10000- 99999
        /**
         * redis中 相同key 对应不同对象
         */
        String REDIS_MUTIVALS = "10000";

        /**
         * 验证码错误
         */
        String JWT_VERIFY_FAIL = "10001";

        /**
         * 网关参数错误
         */
        String WG_ERROR_PARAMS = "10002";

        /**
         * 接口异常结果
         */
        String GW_API_FAIL = "10003";

        /**
         * 接口不存在
         */
        String GW_API_NOT_EXISTENT = "10004";

        /**
         * 接口调用失败
         */
        String GW_API_EXECUTE_FAIL = "10005";

    }


}

