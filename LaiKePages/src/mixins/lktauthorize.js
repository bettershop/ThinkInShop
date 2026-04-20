import LaiketuiWeixinAuth from "../components/laiketuiauth/mpweixin/mpweixin.js"; 
export default {
        data () {
            return {
                showWin: false,//是否弹出授权窗 
            }
        },
        created() {
            this.setLang();
        }, 
        watch:{
            showWin(val){
                if(val){
                    this.setLang();
                    uni.navigateTo({
                        url: '/pagesC/my/authorization'
                    })
                    this.showWin = false
                }
            }
        },
        methods: {
            closeWin () {
                this.showWin = false
                this.$emit('cancle')
            },

            /**
             * 认证后处理
             */
            handleAfterAuth (frompage, forwardUrl, callback, args) { 
                console.log('LaiketuiWeixinAuth1',LaiketuiWeixinAuth)
                var _this = this
                var me = frompage
                me.LaiKeTuiCommon.getLKTApiUrl().then(()=> {
                    // TODO 可能有登录过期的问题
                    let LoingByHand = uni.getStorageSync('LoingByHand')//登录方式，true注册登录  false第三方登录
                    let needRegister = uni.getStorageSync('needRegister')//登录方式，1注册登录（RL）2第三方登录（NRL）
                    if (needRegister == me.LaiKeTuiCommon.LKT_NRL_TYPE.NRL && !LoingByHand) {
                        // #ifdef MP-ALIPAY
                        _this.LaiketuiAliAuth.laiketui_mp_alipay_check(_this, me, callback, args)
                        // #endif
                        // #ifdef MP-TOUTIAO
                        _this.LaiketuiTTAuth.laiketui_mp_tt_check(_this, me, callback, args)
                        // #endif
                        // #ifdef MP-WEIXIN
                        LaiketuiWeixinAuth.laiketui_mp_weixin_checksession(_this, me, callback, args)
                        // #endif
                        // #ifdef MP-BAIDU
                        _this.LaiketuiBDAuth.laiketui_mp_baidu_check(_this, me, callback, args)
                        // #endif
                    } else {
                        me.LaiKeTuiCommon.laikeCheckTimeout(me,forwardUrl).then(function (data) {
                            if (data.visitor) {
								me.access_id1 = false
                                uni.showToast({
                                    title: me.language.lktauthorize.pleaseLogin,
                                    icon: 'none',
                                    duration: 1000
                                })
                                setTimeout(function () {
                                    if (forwardUrl) {
                                        uni.navigateTo({
                                            url: forwardUrl,
                                        })
                                    } else {
                                        uni.navigateTo({
                                            url: '/pagesD/login/newLogin',
                                        })
                                    }
                                }, 1000)
                            } else {
                                if (callback && typeof (callback) == 'function') {
                                    if (args) {
                                        callback(args)
                                    } else {
                                        callback()
                                    }
                                }
                            }
                        })
                    }
                })
            },

            //微信授权
            bindGetUserInfo (res) {
                LaiketuiWeixinAuth.laiketui_mp_weixin_auth(res, this)
            },

            //头条授权
            ttAuth (res) {
                this.LaiketuiTTAuth.laiketui_mp_tt_userInfo(this)
            },

            //百度授权
            bdAuth (res) {
                this.LaiketuiBDAuth.laiketui_mp_baidu_userInfo(this)
            },

            //支付宝授权
            onGetAuthorize (res) {
                me.LaiketuiAliAuth.laiketui_mp_alipay_userInfo(res, this)
            }
        }
    }
