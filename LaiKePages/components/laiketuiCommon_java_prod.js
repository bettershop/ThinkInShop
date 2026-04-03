import {getLaiketuiNoRegisterLoginInfo} from '../common/laiketuiNoRegisterLogin.js'
import en_US from '../common/lang/en_US.js'
import zh_CN from '../common/lang/zh_CN.js'
import Vue from 'vue'

let toast;

/**
 * 是否 DEBUG, 开启 DEBUG 模式会在H5端显示 vconsole,并在控制台输入请求和返回信息
 */
const IS_DEBUG = false

/**
 * 是否开启在 APP 分享小程序
 */
const IS_SHARE_WECHAT_MINI_PROGRAM = true;

/**
 * 是否在微信小程序内嵌H5
 */
const IS_SHARE_WECHAT_H5PAY = false;

/**
 * 小程序 app分享到微信小程序的微信小程序原始ID -- 在微信小程序后台获取
 */
const WECHAT_MINI_PROGRAM_ID = 'gh_7b5143e4ddca';

/**
 * 商户/商城id:管理后台的 "平台" -> "商户列表"['商城列表'];默认商城ID = 1
 */
let DEFAULT_STORE_ID  = 1;


/**
 * 默认币种符号
 * @type {string}
 */
let DEFAULT_STORE_SYMBOL  = '￥';

/**
 * 商城默认币种的符号
 * @type {string}
 */
let DEFAULT_STORE_CODE  = 'CNY';

/**
 * 默认币种汇率
 * @type {number}
 */
let DEFAULT_STORE_EXCHANGERATE  = 1;

/**
 * 商户/商城id:管理后台的 "平台" -> "商户列表"
 */
const LKT_STORE_ID = DEFAULT_STORE_ID

/*
 * 类型：当前后台类型 JAVA/PHP
 */
//java
const LKT_TYPE = 'JAVA'
//PHP
//const LKT_TYPE = 'PHP'

/*
 * websocket 客服配置地址 
 */
//java



const LKT_WEBSOCKET = 'wss://java.houjiemeishi.com/wss/comps/onlineMessage/'
//PHP
// const LKT_WEBSOCKET = 'wss://tp.dev.laiketui.net/wss/' 

/**
 * 接口地址：网关域名
 */
// const LKT_ROOT_URL = 'http://192.168.1.9:21802/comps'  
// const LKT_ROOT_URL = 'http://localhost:21802/comps'

// //java 
const LKT_ROOT_URL = 'https://java.houjiemeishi.com'
//PHP
// const LKT_ROOT_URL = 'https://tp.dev.laiketui.net'
// const LKT_ROOT_URL = 'http://localhost:18001'

/**
 * 网关服务访问代理前缀
 */
const VERSION = 'gw'

/**
 * 带版本号的路径
 */
const LKT_ROOT_VERSION_URL = 'https://java.houjiemeishi.com/pic/'
// const LKT_ROOT_VERSION_URL = 'https://tp.dev.laiketui.net/pic/'

/**
 *    来客电商路径
 */
const LKT_API_URL =  LKT_ROOT_URL + '/' + VERSION + '?store_id=' + LKT_STORE_ID + '&2=2&'

/**
 * 来客电商H5默认路径s
 */
//java
const LKT_H5_DEFURL = 'https://java.houjiemeishi.com/#/'
//PHP
// const LKT_H5_DEFURL = 'https://tp.dev.laiketui.net/H5/#/'

/**
 * 网关入口地址
 */
const LKT_ENDURL = LKT_ROOT_URL + '/' + VERSION + '/'

/**
 *  移动端类型  1 微信小程序 2 app、H5  3 支付宝小程序 4 字节跳动小程序 5 百度小程序
 */
const DEFAULT_LKT_STORE_TYPE = 2

/**
 * 店铺类型 1 微信小程序 2 app、H5  3 支付宝小程序 4 字节跳动小程序 5 百度小程序
 */
function getStoreType () {
    let store_type = DEFAULT_LKT_STORE_TYPE
    // #ifdef APP-PLUS
    // store_type = 2
    store_type = 11
    // #endif
    // #ifdef MP-ALIPAY
    store_type = 3
    // #endif
    // #ifdef MP-BAIDU
    store_type = 5
    // #endif
    // #ifdef MP-TOUTIAO
    store_type = 4
    // #endif
    // #ifdef MP-WEIXIN
    store_type = 1
    // #endif
    // #ifdef H5
    // store_type = 7
    store_type = 2
    // #endif
    return store_type
}

/**
 * 发货公共方法 - 商品发货
 * @param {Object} _this    this指向当前data
 * @param {Object} list     请求参数: store_id、store_type、access_id 这三个参数是必传(已加在请求拦截中)
 * @param {Object} api      请求接口 预留其他api
 * list: {
 * 		expressid:1, //物流公司id
 * 		courierNum:'111111', //物流号
 * 		type:1,	//发货类型 1普通发货 2电子面单 3商家配送
 * 		psyInfo:{
 *			name:'张三', //姓名
 *			tel:'18888888888', //电话
 *		}, //配送员信息
 * 		orderList:[
 *			{
 *				detailId:12323, //详情id
 *				num:1, //发货商品数量
 *			},
 *		], //订单信息
 * }
 */
async function orderSend(_this, list, api){
    // 如果是电子面单 清空物流号
    if(list.type == 2){
        list.courierNum = ''
    }else{
        if(list.expressid == '' || list.courierNum == '' ){

            uni.showToast({
                title: '请完善物流信息！',
                icon: 'error'
            })
            return
        }
    }
    let data = {
        api: api || 'mch.App.Mch.UnifiedShipment',
        list: JSON.stringify(list)
    }
    return await _this.$req.post({ data })
}

/**
 * 分享公共方法 - 获取分享参数
 * @param {Object} _this    this指向当前data
 * @param {Object} mchId    店铺id：id>0，则说明是店铺分享
 * @param {Object} pages    分享页面的路径 - 最前面不需要/  案例: 'pagesB/store/store'
 * @param {Object} states   分享页面 - 需要携带的参数   案例: 'is_share=true&shop_id=' + shop
 * @param {Object} plugIn   分享插件类型 - 普通商品（'gm'） 分销商品（'fx'） 竞拍商品（'jp'）
 * @param {Object} goods    分享商品信息 - goods{id:商品id, title:商品名称, imgUrl:商品图片, price:商品金额}
 */
function shareModel(_this, mchId, pages, states, plugIn, goods){
    //去掉states中首字符 '?'
    states = shareModel_replaceFirstChar('first', states, '?', '')
    //去掉pages中首字符 '/' 
    pages = shareModel_replaceFirstChar('first', pages, '/', '')
    //去掉pages中尾字符 '?'
    pages = shareModel_replaceFirstChar('end', pages, '?', '')
    //处理pages中的 / 以 .- 替代，在home.vue分享中间页替换回 /
    pages = pages.replace(/\//g,'.-')
    //店铺分享 - mchId>0，则说明是店铺分享
    if(mchId){
        _this.share.mch.type = true
        _this.share.mch.mchId = mchId
    }else{
        _this.share.mch.type = false
        _this.share.mch.mchId = mchId
    }
    //分享-商品id
    _this.share.goods.id = goods.id
    //分享-商品名称
    _this.share.goods.title = goods.title
    //分享-商品图片
    _this.share.goods.imgUrl = goods.imgUrl
    //分享-商品金额
    _this.share.goods.price = goods.price
    //分享插件类型
    _this.share.shareType = plugIn
    //拼接后，传给组件使用
    _this.share.shareHref = 'share=true&pages=' + pages + '&' + states
}

/**
 * 匹配字符并替换
 * @param {Object} type     匹配位置：first首字符 end尾字符
 * @param {Object} str      需要替换字符的字符串
 * @param {Object} oldChar  匹配字符 /
 * @param {Object} newChar  替换成 ''
 */
function shareModel_replaceFirstChar(type, str, oldChar, newChar) {
    switch(type){
        case 'first':
            if (str.charAt(0) === oldChar) {
                str = str.slice(1) + newChar
            }
            break
        case 'end':
            if (str.charAt(str.length - 1) === oldChar){
                str = str.slice(0, -1) + newChar
            }
            break
        default:
            console.log('不存在此匹配位置～', type)
    }
    return str;
}

/**
 * 跳转到登录界面
 *
 * @param {Object} page
 * @param {Object} forwardUrl
 */
function toLogin (page, forwardUrl) {
    var me = page
    me.$store.state.access_id = uni.getStorageSync('laiketuiAccessId') ? uni.getStorageSync('laiketuiAccessId') : uni.getStorageSync(
        'access_id')
    var access_id = me.$store.state.access_id
    if (access_id == undefined) {
        access_id = ''
    }
    var data = {
        api: 'app.login.token',
        access_id,
    }
    uni.request({
        url: LKT_API_URL,
        data,
        success: function (res) {
            if (res.data.code == 404) {
                uni.showToast({
                    title: me.language.laiketuiCommon.noLogin,
                    duration: 1000,
                    icon: 'none'
                })
                var url = forwardUrl
                setTimeout(function () {
                    uni.navigateTo({
                        url: url
                    })
                }, 1000)
            } else {
                me.changeLoginStatus()
            }
        }
    })
}

/**
 * 这个只有微信小程序用
 * @param {Object} me
 */
function getWXTmplIds (me) {

    let data = {
        api: 'app.message.getWXTemplates',

        store_type: 1,
    }

    me.$req.post({
        data
    }).then(res => {
        let data = res.data
        let code = res.code
        if (code == 200) {
            me.tmplIds = data
        } else {
            console.log('获取微信小程序订阅模板失败')
        }

    }).catch(function (res1) {
        uni.showToast({
            title: me.language.laiketuiCommon.forFailure,
            duration: 1000,
            icon: 'none'
        })
    })
}

/**
 * 获取用户的Token
 */
function getLKTAccessToken (obj) {
    var me = obj
    return new Promise((resolve, reject) => {
        var token = uni.getStorageSync('access_id') ? uni.getStorageSync('access_id') : obj.$store.state.access_id
        me.$store.state.access_id = token
        var access_id = me.$store.state.access_id
        var data = {
            api: 'app.login.token',
            access_id,
        }
        uni.request({
            url: uni.getStorageSync("url"),
            data,
            success: function (res) {
                var code = res.data.code
                if (code == 200) {
                    me.$store.state.access_id = access_id
                    me.access_id = me.$store.state.access_id
                    uni.setStorageSync('laiketuiAccessId', me.access_id)
                    uni.setStorageSync('online', true)
                } else {
                    me.$store.state.access_id = ''
                    me.access_id = ''
                    uni.setStorageSync('laiketuiAccessId', '')
                    uni.setStorageSync('online', false)
                }
                resolve(me)
            },
            fail (res) {
                reject(res)
            }
        })
    })
}

/**
 * 获取界面url请求地址
 */
function getLKTApiUrl () {
    var me = this
    return new Promise((resolve, reject) => {
        // #ifdef MP-WEIXIN
        laikeMPWeixinExtUrl().then(function (request_url) {
            return getApiUrl(request_url)
        }).then(function () {
            resolve('')
        })
        // #endif

        // #ifndef MP-WEIXIN 
        var request_url = uni.getStorageSync("url")
        getApiUrl(request_url).then(function () {
            resolve('')
        })
        // #endif

        // #ifndef APP-PLUS || H5
        getLaiketuiNoRegisterLoginInfo(1)
        // #endif

    })
}

/**
 * 关闭小程序授权弹出窗
 */
function closeMPAuthWin (me) {
    // #ifdef MP
    var userinfo = uni.getStorageSync('userinfo') || []
    if (userinfo['openid']) {
        //关闭
        me.$refs.lktAuthorizeComp.closeWin()
    }
    // #endif
}

/**
 * 获取路径（url ）
 */
async function getApiUrl (request_url) {
    let api_url = request_url
    let h5_url = LKT_H5_DEFURL
    let endurl = LKT_ENDURL

    if(api_url.indexOf("&store_type") == -1 ){
        // #ifdef APP-PLUS
        api_url = api_url + '&store_type=11'
        // #endif
        // #ifdef MP-ALIPAY
        api_url = api_url + '&store_type=3'
        // #endif
        // #ifdef MP-BAIDU
        api_url = api_url + '&store_type=5'
        // #endif
        // #ifdef MP-TOUTIAO
        api_url = api_url + '&store_type=4'
        // #endif
        // #ifdef MP-WEIXIN
        api_url = api_url + '&store_type=1'
        // #endif
        // #ifdef H5
        api_url = api_url + '&store_type=2'
        // #endif
    }

    uni.setStorageSync('url', api_url)
    uni.setStorageSync('h5_url', h5_url)
    uni.setStorageSync('endurl', endurl)
}

/**
 * 获取微信小程序配置的路径
 */
function laikeMPWeixinExtUrl () {
    let extConfig = uni.getExtConfigSync? uni.getExtConfigSync(): {}
    let mpextURL = ''
    if (extConfig.url) {
        mpextURL = extConfig.url
    } else {
        mpextURL = LKT_API_URL
    }
    mpextURL = mpextURL + 'store_type=1'
    uni.setStorageSync('url', mpextURL)
    //获取是否免密码登录开关
    getLaiketuiNoRegisterLoginInfo(1)
    return Promise.resolve(mpextURL)
}

/**
 * 获取推送消息的客户端ID
 */
function getClientid () {
    let CID = plus.push.getClientInfo()
    return CID.clientid
}

/**
 * div按钮无重复点击
 * @param {Object} frompage
 * @param {Object} callback
 * @param {Object} opts
 */
function laiketuiNoDoublePress (frompage, callback, opts) {
    let me = frompage
    var len = me.clicktimes.length
    var now = new Date().getTime()
    let lastclickBuyBtn = len > 0 ? me.clicktimes[len - 1] : now
    me.clicktimes.push(now)
    len = me.clicktimes.length
    //第一次进的时候时间数组长度为一、或者两次点击时间间隔大于等于一秒
    if (len == 1 || (now - lastclickBuyBtn) >= 1000) {
        if (opts) {
            callback(opts)
        } else {
            callback()
        }
    }
    if (len >= 3) {
        me.clicktimes.shift()
    }
}

/**
 * 延迟2秒赋值，防止重复点击
 */
function lktDelaySetVal (me) {
    setTimeout(function () {
        if (!me.fastTap) {
            me.fastTap = true
        }
        if (me.isClick) {
            me.isClick = false
        }
        if (!me.signFinish) {
            me.signFinish = true
        }
    }, 1500)
}

/**
 *    登录超时处理:自己在调用的页面定义timeout()函数，逻辑自定义。
 *    1：这个超时的处理针对 H5 app 小程序的人工登陆操作
 *    2：不针对小程序授权操作；小程序授权一次成功后后面的操作静默授权
 *    @param {Object} frompage
 */
function laikeCheckTimeout (frompage,forwardUrl) {
    let me = frompage
    var access_id = uni.getStorageSync('access_id') ? uni.getStorageSync('access_id') : me.$store.state.access_id
    var data = {
        api: 'app.login.token',
        language: uni.getStorageSync('language') || '',
        access_id,
    }
    return new Promise((resolve, reject) => {
        uni.request({
            url: uni.getStorageSync("url"),
            data,
            success: function (res) {
                var code = res.data.code
                console.log("登录验证返回code>>");
                console.log(code);
                if(code)
                {
                    if (code == 200 && res.data.data.login_status == 1) { //登录未失效的用户
                        me.$store.state.access_id = access_id
                        me.access_id = me.$store.state.access_id
                        uni.setStorageSync('laiketuiAccessId', me.access_id)
                        uni.setStorageSync('online', true)
                        resolve(me)
                    } else if (code == 200 && res.data.data.login_status == 0) {
                        //游客
                        resolve({
                            visitor: true
                        })
                    } else if (code == 404) {
                        if (me._back1 && typeof (me._back1)) {
                            if (typeof (me._back1) == 'function' || typeof (me._back1) == 'FUNCTION') {
                                me._back1()
                            }
                        } else {
                            uni.showToast({
                                title: me.language.laiketuiCommon.noLogin,
                                icon: 'none',
                                duration: 1500
                            })
                            setTimeout(function () {
                                if(forwardUrl){
                                    uni.navigateTo({
                                        url: forwardUrl
                                    })
                                }else{
                                    uni.navigateTo({
                                        url: '/pagesD/login/newLogin?toHome=true'
                                    })
                                }

                            }, 1000)
                        }
                    } else {
                        //登录超时
                        me.$store.state.access_id = ''
                        me.access_id = ''
                        uni.setStorageSync('laiketuiAccessId', '')
                        uni.setStorageSync('online', false)
                        uni.removeStorage({
                            key: 'history'
                        })
                        uni.removeStorage({
                            key: 'user_phone'
                        })
                        uni.removeStorage({
                            key: 'hotStatu'
                        })
                        uni.removeStorage({
                            key: 'access_id'
                        })
                        me.$store.state.cart = []
                        me.$store.state.cart_id = []
                        me.$store.state.nCart = []
                        /**
                         * @param {Object} res
                         */
                        //每个页面自己去实现timeout函数
                        if (me.timeout && typeof (me.timeout)) {
                            if (typeof (me.timeout) == 'function' || typeof (me.timeout) == 'FUNCTION') {
                                me.timeout()
                            }
                        } else {
                            //若是没有自定义timeout函数则进入下面跳转
                            uni.showToast({
                                title: me.language.laiketuiCommon.noLogin,
                                icon: 'none',
                                duration: 1500
                            })
                            setTimeout(function () {

                                if(forwardUrl){
                                    uni.navigateTo({
                                        url: forwardUrl
                                    })
                                }else{
                                    uni.navigateTo({
                                        url: '/pagesD/login/newLogin?toHome=true'
                                    })
                                }

                            }, 1000)
                        }
                    }
                }
                else
                {
                    uni.showToast({
                        title: "服务繁忙，请稍后重试",
                        icon: 'none',
                        duration: 1000
                    })
                }
            },
            fail (res) {
                console.log("登录接口报错>>");
                console.log(res);
                reject(res)
            }
        })
    })
}

/**
 *
 */
function laikeVisitorToLogin () {
    if(uni.getStorageSync('language') == 'en_US'){
        toast = en_US.laiketuiCommon
    }else{
        toast = zh_CN.laiketuiCommon
    }

    //游客
    uni.showToast({
        title: toast.noLogin,
        icon: 'none',
        duration: 1500
    })
    setTimeout(function () {
        uni.navigateTo({
            url: '../../pagesD/login/newLogin'
        })
    }, 1000)
}

/**
 * 获取授权码
 */
function getMPAliAuthCode () {
    return new Promise((laikeRes) => {
        // #ifdef MP-ALIPAY
        my.getAuthCode({
            success: (res) => {
                laikeRes(res.authCode)
            },
        })
        // #endif

        // #ifdef MP-TOUTIAO
        tt.login({
            success (res) {
                laikeRes(res.code)
            },
            fail (res) {
                console.log(`login调用失败`)
            }
        })
        // #endif

        // #ifndef MP-ALIPAY || MP-TOUTIAO
        laikeRes('')
        // #endif

    })
}

/**
 * 先获取url 再做其他
 * @param {Object} cb
 */
function getUrlFirst (cb) {
    let p = getLKTApiUrl()
    if (cb && (typeof (cb) == 'function' || typeof (cb) == 'FUNCTION')) {
        p.then(function () {
            cb()
        })
    }
    return p
}

/**
 * 头条小程序IOS不支持虚拟支付
 */
function ttIOSCantVisualpay () {
    // #ifdef MP-TOUTIAO
    if(uni.getStorageSync('language') == 'en_US'){
        toast = en_US.laiketuiCommon
    }else{
        toast = zh_CN.laiketuiCommon
    }

    const info = uni.getSystemInfoSync()
    if (info.platform == 'ios') {

        uni.showToast({
            icon: 'none',
            title: toast.iosFail
        })
        return false
    }
    // #endif
    return true
}

/**
 * 来客电商免密码登录状态
 */
const LKT_NRL_TYPE = {
    RL: 1, //注册登录
    NRL: 2 //免密码登录
}

/**
 * 分享绑定上级
 * @param isfx
 * @param fatherId
 * @returns {Promise<void>}
 */
async function bindPID (isfx, fatherId) {
    if (isfx && fatherId) {
        let data = {
            api: 'app.login.chang_pid',
            pid: fatherId
        }
        let res = Vue.prototype.$req.post({data})
        console.log(res);
    }
}


//判断是否为空值
function isempty (val) {
    if(typeof(val) == 'undefined') {
        return true;
    }
    if(val=='' || val=='null' || val=='undefined' || val==null){
        return true;
    }
    return false;
}

/**
 * 初始化
 * @param {Object} storeID 商户id
 */
function initStoreID(storeID){
    let api_url = LKT_API_URL
    // #ifdef H5  
    uni.removeStorageSync("url");
    storeID = !storeID ? DEFAULT_STORE_ID: storeID;
    uni.setStorageSync("store_id",storeID)
    api_url = LKT_ROOT_URL + '/' +  VERSION + '?store_id=' + storeID
    console.log("initStoreID：" + api_url);
    uni.setStorageSync('url', api_url);
    // #endif  
}

/**
 * 获取所有语种
 */
function getLanguages(){
    let data = {
        api: "app.common.getLangs"
    }
    let res = Vue.prototype.$req.post({data})
    return res;
}

/**
 * 获取所有货币
 */
function getCurrencys(){
    let data = {
        api: "app.common.getCurrencys"
    }
    let res = Vue.prototype.$req.post({data})
    return res;
}

/**
 * 获取所有国家
 */
function getCountryList(){
    let data = {
        api: "app.common.getCountry"
    }
    let res = Vue.prototype.$req.post({data})
    return res;
}

/**
 * 获取商城默认货币
 */
function getDefaultCurrencys(){
    let data = {
        api: "app.common.getStoreDefaultCurrency"
    }
    let res = Vue.prototype.$req.post({data})
    return res;
}

/**
 * 计算价格，格式化价格
 * 其他场景计算公式： 用户选择的币种汇率 ✖️ 基本价格 = 用户选择的币种汇率和币种计算出的金额
 */
function formatPrice(basePrice) {
    let currency = uni.getStorageSync('currency')
    let storeCurrency = uni.getStorageSync('storeCurrency')
    if (currency) {
        //汇率
        DEFAULT_STORE_EXCHANGERATE = currency.exchange_rate
    } else if(storeCurrency) {
        //汇率
        DEFAULT_STORE_EXCHANGERATE = storeCurrency.exchange_rate
    } else {
        let  result = getDefaultCurrencys();
        let storeCurrency = result.data;
        uni.setStorageSync('storeCurrency',storeCurrency);
        uni.setStorageSync('currency',storeCurrency);
        //汇率
        DEFAULT_STORE_EXCHANGERATE = storeCurrency.exchange_rate
    }
    let rate = DEFAULT_STORE_EXCHANGERATE ;
    let formattedPrice = (basePrice * rate).toFixed(2);
    return formattedPrice;
}

/**
 * 计算价格（正向）
 * 订单场景：下单时的汇率 ✖️ 基本价格 = 下单时汇率和币种计算出的金额
 */
/**
 * OSS图片加速：按尺寸裁剪（仅对阿里OSS且未签名URL生效）
 */
function getFastImg(url, width, height) {
    if (!url || typeof url !== 'string') return url;
    if (url.indexOf('x-oss-process=') !== -1) return url;
    if (!/aliyuncs\.com/i.test(url)) return url;
    if (url.indexOf('Signature=') !== -1 || url.indexOf('OSSAccessKeyId=') !== -1) return url;
    const sep = url.indexOf('?') !== -1 ? '&' : '?';
    let process = `x-oss-process=image/resize,m_fill,w_${width || 750}`;
    if (height) process += `,h_${height}`;
    return `${url}${sep}${process}`;
}

function getPriceWithExchangeRate(basePrice,exchange_rate) {

    if(!exchange_rate){
        exchange_rate = 1;
    }

    let formattedPrice = (basePrice * exchange_rate).toFixed(2);
    return formattedPrice;
}

/**
 * 计算价格（反向）
 * 退款售后场景：下单时汇率和币种计算出的金额 ➗ 下单时的汇率 = 基本价格
 */
function getBasePriceFromExchange(convertedPrice, exchange_rate) {
    if (!exchange_rate || exchange_rate === 0) {
        exchange_rate = 1;
    }
    let basePrice = (convertedPrice / exchange_rate).toFixed(2);
    return basePrice;
}


/**
 *  【余额】场景计算价格：基础货币向买家当前选择的货币转换
 */
function storeCurrency2UserCurrencyPrice(basePrice,curent_user_currency_exchange_rate) {
    if(!curent_user_currency_exchange_rate){
        curent_user_currency_exchange_rate = 1;
    }
    let formattedPrice = (basePrice / curent_user_currency_exchange_rate ).toFixed(2);
    return formattedPrice;
}



export default {
    IS_DEBUG,
    isempty,
    LKT_STORE_ID,
    LKT_TYPE,
    VERSION,
    LKT_ROOT_VERSION_URL,
    LKT_ROOT_URL,
    LKT_API_URL,
    LKT_H5_DEFURL,
    LKT_ENDURL,
    LKT_NRL_TYPE,
    IS_SHARE_WECHAT_MINI_PROGRAM,
    WECHAT_MINI_PROGRAM_ID,
    toLogin,
    getLKTAccessToken,
    getClientid,
    laiketuiNoDoublePress,
    laikeCheckTimeout,
    laikeVisitorToLogin,
    lktDelaySetVal,
    getLKTApiUrl,
    getApiUrl,
    getMPAliAuthCode,
    getUrlFirst,
    getStoreType,
    shareModel,
    orderSend,
    ttIOSCantVisualpay,
    closeMPAuthWin,
    getWXTmplIds,
    bindPID,
    initStoreID,
    DEFAULT_STORE_SYMBOL,
    DEFAULT_STORE_EXCHANGERATE,
    DEFAULT_STORE_CODE,
    LKT_WEBSOCKET,
    getLanguages,
    getCurrencys,
    formatPrice,
    getFastImg,
    getPriceWithExchangeRate,
    storeCurrency2UserCurrencyPrice,
    getBasePriceFromExchange,
    getDefaultCurrencys,
    getCountryList
}
