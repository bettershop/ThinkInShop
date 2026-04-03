// 密码是否可见 1登录密码 2注册密码 3再次输入注册密码
export function lkt_pwStatus (type, me) {
    if (type == 1) {
        me.LoginPWStatus = !me.LoginPWStatus
    } else if (type == 2) {
        me.regPWStatus1 = !me.regPWStatus1
    } else if (type == 3) {
        me.regPWStatus2 = !me.regPWStatus2
    }
}

//手机号输入失焦 type2验证码登录输入手机号，3注册输入手机号
export function lkt_telephone (type, me) {
    if (type == 1) {
        me.passLoginCodePH = me.passLoginCodePH1
        me.account_f = false
    } else if (type == 2) {
        console.log('我输入了验证码登录手机号')
        me.codeLoginCodePH = me.codeLoginCodePH1
        me.pone_f = false
    } else if (type == 3) {
        console.log('我输入了注册手机号')
        me.phone_y = false
    }
}

//获取验证码 type1验证码登录 2注册
export function lkt_phone_code (type, me) {
    if (!me.fastTap) {
        return
    }
    me.fastTap = false
    console.log(111)
    if (me.phone) {
        if (me.one_code == 1 && !me.count) {
            //手机号
            me.old_phone = me.phone
            //请求验证码
            let data = {
                api: 'app.user.secret_key',
                phone: me.phone,
                cpc:me.diqu.code2
            }
            if (type == 1) {
                data.message_type = 0 // 短信类型 0.验证码 1.短信通知
                data.message_type1 = 1 // 短信类别 1.登录
            } else {
                data.message_type = 0 // 短信类型 0.验证码 1.短信通知
                data.message_type1 = 2 // 短信类别 2.注册
            }
            me.$req.post({data}).then(res => {
                var _code = {
                    200: me.language.toasts.login.fsOk,
                    219: me.language.toasts.login.no_config,
                    220: me.language.toasts.login.fsFail,
                    20001: me.language.toasts.login.fsFail,
                    50721: me.language.toasts.login.fsFail,
                }
                if (_code[res.code]) {
                    uni.showToast({
                        title: _code[res.code],
                        duration: 1500,
                        icon: 'none'
                    })
                    if(res.code == '50721'){
                        //初始化验证码提示
                        clearInterval(me.timer)
                        me.timer = ''
                        me.count = ''
                        me.one_code = 1
                        me.time_code = me.language.login.page2.getCode
                    }
                    if(res.code == '200'){
                        //验证码倒计时
                        const TIME_COUNT = 60
                        if(uni.getStorageSync('language') == 'en_US'){
                            me.time_code = me.language.login.page2.countdown + ' ' + TIME_COUNT + `s`
                        }else{
                            me.time_code = TIME_COUNT + `s` + me.language.login.page2.countdown
                        }
                        if (!me.timer) {
                            me.count = TIME_COUNT
                            me.timer = setInterval(() => {
                                if (me.count > 0 && me.count <= TIME_COUNT) {
                                    me.count--
                                    if(uni.getStorageSync('language') == 'en_US'){
                                        me.time_code = me.language.login.page2.countdown + ' ' + me.count + `s`
                                    }else{
                                        me.time_code = me.count + `s` + me.language.login.page2.countdown
                                    }
                                } else {
                                    clearInterval(me.timer)
                                    me.time_code = me.language.login.page2.getCode
                                    me.timer = null
                                    me.count = ''
                                }
                            }, 1000)
                        }
                    }
                } else {
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: 'none'
                    })
                }
                if (type == 1) {
                    me.phone_codeStatus1 = true
                    if (me.phone_code.length == 6 && me.phone.length >0 && me.phone_codeStatus1 == true) {
                        me.codeLoginBtnStatus = true
                    } else {
                        me.codeLoginBtnStatus = false
                    }
                } else if (type == 2) {
                    me.phone_codeStatus2 = true
                    if (me.phone.length >0 && me.phone_codeStatus2 && me.phone_code.length == 6 &&
                        me.passtwo.length > 5 && me.phone_code.length == 6 && me.passone.length == me.passtwo.length) {
                        me.regBtnStatus = true
                    } else {
                        me.regBtnStatus = false
                    }
                }
                me.fastTap = true
            })
        } else {
            me.fastTap = true
        }
    } else {
        uni.showToast({
            title: me.language.toasts.login.tel,
            duration: 1000,
            icon: 'none'
        })
        me.fastTap = true
    }
}

//登录
export function submitLogin(me){
    let data = {
        isLogin:1
    }
    
    if (me.code) {
        //验证码登录
        data.api= 'app.register.login'
        data.keyCode= me.code
        data.type= me.activeTab == 'phone'?1:2 //1：手机号 2：账号/邮箱     
    }else{
        //密码登录
        data.api= 'app.login.login'
        data.type= me.activeTab == 'phone' ? 1 : (me.activeTab == 'email' ? 2 : 3)// 类型 1.手机号 2.邮箱  3.账号
        data.password= me.password
        data.access_id= me.$store.state.access_id
        //登录
        if (verification(me)) {
            return
        }
    } 

    if(me.activeTab == 'phone'){
        data.cpc= me.code2 // 区号
        data.country_num= me.num3  // 国家代码
        data.phone= me.phone  // 手机号码
    }else if(me.activeTab == 'email'){
      data.phone= me.email// 邮箱
    }else if(me.activeTab == 'account'){
      data.phone= me.account// 账号
    }

    login(me,data)
}


function login(me,data){
    if (me.fatherId != '') {
      data.pid = me.fatherId
    }
   
    me.$req.post({data}).then(res => {
        if (res.code == 200 && res.data && res.data.access_id) {
            let {
                access_id,
                user_id,
            } = res.data
          
            me.$store.state.access_id = []
             
            uni.showToast({
                title: me.language.login.loginSuc,
                duration: 1000,
                icon: 'success'
            })
            uni.setStorageSync("isxufei",true)
            uni.setStorageSync('isHomeShow',1)
            
            me.set_access_id(access_id)
            uni.setStorageSync('access_id', access_id)
            uni.setStorageSync('user_phone', me.phone)
            uni.setStorageSync('user_id', user_id) 
            // 缓存公告
            uni.setStorageSync("laike_move_uaerInfo", res.data)
            // 是否提示公告弹窗
            if((uni.getStorageSync('laike_move_uaerInfo').systemMsgType == 2||uni.getStorageSync('laike_move_uaerInfo').systemMsgType == 3) && uni.getStorageSync('laike_move_uaerInfo').type != 0) {
                uni.setStorageSync('versionUpdate3',true)
            }
            //免注册登录下用账户或手机号登录
            uni.setStorageSync('LoingByHand', true)

            //缓存userinfo，小程序也可以密码登录，分享有问题.
            let userinfo = uni.getStorageSync('userinfo') || '{}'
            if(typeof userinfo == 'string'){
                userinfo = JSON.parse(userinfo)
            }
            userinfo.user = res.data
            uni.setStorageSync('userinfo', userinfo)

            //商城默认币种不可能为空
            let storeCurrency = userinfo.user.storeCurrency || {};
            uni.setStorageSync('storeCurrency',storeCurrency);
            uni.setStorageSync('language',userinfo.user.lang);
            uni.setStorageSync('lang_code',userinfo.user.lang);
            

            //个人选择的默认币种 可能为空
            let preferred_currency = userinfo.user.preferred_currency || {}
            if(!preferred_currency)
            {
                uni.setStorageSync('preferred_currency',storeCurrency.id);
                uni.setStorageSync('currency',storeCurrency);
                me.LaiKeTuiCommon.DEFAULT_STORE_SYMBOL = storeCurrency.currency_symbol
                me.LaiKeTuiCommon.DEFAULT_STORE_EXCHANGERATE = storeCurrency.exchange_rate;
            }
            else
            {
                uni.setStorageSync('preferred_currency',userinfo.user.preferred_currency);
                uni.setStorageSync('currency',userinfo.user.userCurrency);
                me.LaiKeTuiCommon.DEFAULT_STORE_SYMBOL = userinfo.user.userCurrency.currency_symbol
                me.LaiKeTuiCommon.DEFAULT_STORE_EXCHANGERATE = userinfo.user.userCurrency.exchange_rate
            }
            // 验证码登录场景
            
            setTimeout(()=>{
               goBack()
            },1500)
            
          } else {
              uni.showToast({
                  title: res.message,
                  icon:'none'
              })
              // goBack()
          }  
      })  
}

function goBack() {
    const pages = getCurrentPages();
    const len = pages.length;
    console.log('pages',pages)
    // 没有上一页时返回主页
    if (len >= 2) { 
        const pageName = (pages[pages.length-1].route || '').replace(/^\//, ''); 
        let list = ['pagesD/login/captchaLogin/inputCode'] 
        if(list.includes(pageName)){
            // #ifdef H5
                uni.navigateBack({
                    delta:3
                })
            // #endif
            // #ifdef MP-WEIXIN
                uni.navigateBack({
                    delta:4
                })
            // #endif
        }else{
            // #ifdef H5
                uni.navigateBack({
                    delta:1
                })
            // #endif
            // #ifdef MP-WEIXIN
                uni.navigateBack({
                    delta:2
                })
            // #endif
        }
    }else{
        //页面栈只有一条信息时
        uni.redirectTo({
            url:'/pages/shell/shell?pageType=my'
        })
    }
}

function  verification(me){
    // 校验手机号
    if(me.activeTab === 'phone' && (!me.phone || me.phone.length == 0)){
        uni.showToast({
            title: me.language.register.tel_placeHolder,
            icon:'none'
        })
        return true
    }
    if(me.activeTab === 'email' && (!me.email || me.email.length == 0)){
        uni.showToast({
            title: me.language.register.tel_EmailHolder,
            icon:'none'
        })
        return true
    }
    if(me.activeTab === 'account' && (!me.account || me.account.length == 0)){
        uni.showToast({
            title: me.language.register.tel_placeHolder,
            icon:'none'
        })
        return true
    }
}



export function next_one (me,url) { 
    if (me.activeTab === 'phone') {
        if (!me.phone) {
          uni.showToast({
            title: '请输入手机号',
            icon: 'none'
          });
          return;
        }
    } else {
        if (!me.email) {
          uni.showToast({
            title: '请输入邮箱号',
            icon: 'none'
          });
          return;
        }
    }
    let data = {
       api:'app.Login.forget_zhanghao',
        type: me.activeTab === 'email'?1:0, //类型 0.手机号 1.邮箱
        cpc: me.activeTab === 'email' ? '': me.diqu.code2 , // 区号
        country_num: me.activeTab === 'email' ? '' : me.diqu.num3 , // 国家代码
        zhanghao: me.activeTab === 'email' ? me.email : me.phone// 手机号 或是邮箱
    }
    me.$req.post({data}).then(res => {
        if (res.code == 200) {
            uni.navigateTo({
              url: url
            });
        }else{
            uni.showToast({
                title: res.message,
                duration: 1000,
                icon: 'none'
            })
        }
    })
    
}
