 
    /**
     *  微信授权
     * @param {Object} res
     * @param {Object} authPage
     */
    function laiketui_mp_weixin_auth (res, authPage) {
        var me = authPage
        
        let userInfo = {}
		uni.navigateTo({
			url:'/pagesC/my/authorization'
		})
        // wx.getUserProfile({
        //     desc: '获取你的昵称、头像、地区及性别', // 声明获取用户个人信息后的用途，后续会展示在弹窗中，请谨慎填写
        //     success: (res) => {
        //         userInfo = res.userInfo;
        //         if (userInfo) {
        //             uni.login({
        //                 success: function (res) {
        //                     if (res.code) {
        //                         let data = {
        //                             nickName: userInfo.nickName,
        //                             headimgurl: userInfo.avatarUrl,
        //                             sex: userInfo.gender,
        //                             code: res.code,
        //                             module: 'app',
        //                             action: 'login',
        //                             app: 'user',
        //                             pid: uni.getStorageSync('fatherId'),
        //                         }
        //                         me.$req.post({
        //                             data
        //                         }).then(res => {
        //                             me.showWin = false;
        //                             if(!res){
        //                                 me.showWin = true;
        //                                 return;
        //                             }
        //                             let code = res.code;
        //                             if(code == 200){
        //                                 uni.setStorageSync('isHomeShow',1)
                                        
        //                                 let access_id = res.data.access_id
        //                                 let userinfo = {}
        //                                 userinfo['openid'] = res.data.openid
        //                                 userinfo['session_key'] = res.data.session_key
        //                                 userinfo['user'] = res.data.user
        //                                 me.access_id = res.data.access_id
        //                                 me.$store.state.access_id = res.data.access_id
        //                                 uni.setStorageSync('userinfo', userinfo)
        //                                 uni.setStorageSync('user_id', res.data.user_id)
                                        
        //                                 //调用父类的改变登录状态方法
        //                                 //手动登陆标志为false
        //                                 uni.setStorageSync('LoingByHand', false)
        //                                 uni.setStorageSync('laiketuiAccessId', me.access_id)
        //                                 uni.setStorageSync('online', true)
                                       
        //                                 if(me.title == '个人中心'){
        //                                     me.changeLoginStatus()
        //                                 }else{
        //                                     me.access_id1 = true
        //                                     me.$emit('pChangeLoginStatus')
        //                                 }
                						
        //                                 uni.removeStorageSync('signFlag');
                						
        //                                 uni.showToast({
        //                                     title: me.language.toasts.authorized.success,
        //                                     duration: 1000,
        //                                     icon: 'none'
        //                                 })
                                        
        //                                 // let userphone = uni.getStorageInfoSync("user_phone");
        //                                 setTimeout(() => {
        //                                     uni.navigateTo({
        //                                         url:'/pagesC/my/authorization'
        //                                     })
        //                                 },500)
                                        
        //                             } else {
        //                                 let msg = res.message;
        //                                 uni.showToast({
        //                                     title: ''+msg,
        //                                     duration: 2000,
        //                                     icon: 'none'
        //                                 })
        //                             }
        //                         })
        //                     } else {
        //                         console.log('授权失败')
        //                         me.showWin = false
        //                         uni.showToast({
        //                             title: me.language.toasts.authorized.failure,
        //                             duration: 1000,
        //                             icon: 'none'
        //                         })
        //                     }
        //                 },
        //             })
        //         } else {
        //             console.log('点击了拒绝授权')
        //             me.showWin = false
        //         }
        //     }
        // })
        
    }

    /**
     * 关闭微信后重新登陆来客系统获取相关操作钥匙
     * @param {Object} me
     */
    function reloadToLaikeOS (me, callback, args) {
        return new Promise(function (laikeok, error) {
            uni.login({
                success: function (res) {
                    if (res.code) {
                        let data = {
                            code: res.code,
                            api: 'app.login.user',
                            pid: uni.getStorageSync('fatherId'),
                        }
                        me.$req.post({
                            data
                        }).then(res => {
                            me.showWin = false
                            let access_id = res.data.access_id
                            let userinfo = {}
                            userinfo['openid'] = res.data.openid
                            userinfo['session_key'] = res.data.session_key
                            userinfo['user'] = res.data.user
                            me.access_id = res.data.access_id
                            me.$store.state.access_id = res.data.access_id
                            uni.setStorageSync('userinfo', userinfo)
                            me.access_id1 = true
                            uni.setStorageSync('LoingByHand', false)
                            uni.setStorageSync('laiketuiAccessId', me.access_id)
                            uni.setStorageSync('online', true)
                            me.$emit('pChangeLoginStatus')

                            if (callback && args) {
                                callback(args)
                            } else if (callback) {
                                callback()
                            }
                        })
                    } else {
                        me.showWin = false
                        uni.showToast({
                            title: me.language.toasts.authorized.reLoginFailed,
                            duration: 1000,
                            icon: 'none'
                        })
                    }
                },
            })
        })
    }

    /**
     * 授权过后，没有失效时
     * @param {Object} obj
     */
    function laiketui_mp_weixin_load (fromPage, callback, args,authPage) {
        var me = fromPage;
        let p = new Promise((resolve, reject) => {
            var userinfo = uni.getStorageSync('userinfo') || []
            if (userinfo['openid']) {
                laikeGetRightToken(me, userinfo['openid'], callback, args,authPage).then(function (token) {
                    resolve(token)
                })
            } else {
                var user_bedel = uni.getStorageSync('userbedel') ;
                console.log("****************"+user_bedel);
                if(user_bedel){
                    //需要重新授权
                    if(authPage){
                        authPage.showWin = true;
                    }
                }else{
                    reloadToLaikeOS(me, callback, args);
                }
            }
        })
        p.then(function (token) {
            var userinfo = uni.getStorageSync('userinfo') || []
            var headimgurl = userinfo.user.headimgurl || ''
            me.$store.state.access_id = token
            uni.setStorageSync('access_id', token)
            me.access_id = me.$store.state.access_id
            me.login_status = 1
            if (callback && args) {
                callback(args)
            } else if (callback && !args) {
                callback()
            }
        })
    }

    /**
     * 检测accessID 是否过期，若过期则生成新accessid
     * @param {Object} access_id
     */
    function laikeGetRightToken (me, openid, callback, args,authPage ) {
        var data = {
            api: 'app.login.login_access',
            openid: openid,
            store_type: 1,
            access_id: uni.getStorageSync('laiketuiAccessId'),
        }
        return new Promise((resolve, reject) => {
            uni.request({
                url: me.$store.state.url,
                data,
                success: function (res) {
                    if(res.data.code != '200'){
                        // #ifdef MP-WEIXIN
                        uni.removeStorageSync('userinfo');
                        uni.navigateTo({
                            url: '/pagesC/my/authorization'
                        })
                        // #endif
                    }
                    var code = res.data.code
                    let token = res.data.data.access_id
                    console.log('laikeLoginTimeout：')
                    console.log(JSON.stringify(res))
                    if (code == 200 && token) {
                        resolve(token)
                    } else {
                        if(code == 228){
                            //用户在数据库中被删除了
                            uni.setStorageSync('userbedel',true) ;
                            uni.setStorageSync('userinfo',null) ;
                            uni.setStorageSync('access_id','') ;
                            uni.showToast({
                                title: me.language.toasts.authorized.noUser,
                                icon: 'none',
                                duration: 2000
                            });
                            if (callback && args) {
                                callback(args)
                            } else if (callback) {
                                callback()
                            }
                            return;
                        }
                        
                        let message = res.data.message || me.language.toasts.authorized.logonException
                        uni.showToast({
                            title: message,
                            icon: 'none',
                            duration: 1500
                        })
                    }
                }
            })
        })
    }

    /**
     * 检测微信的session是否失效
     * @param {Object} authPage
     * @param {Object} fromPage
     */
    function laiketui_mp_weixin_checksession (authPage, fromPage, callback, args) {
        var _this = authPage
        var me = fromPage;
        uni.checkSession({
            success: function (res) {
                console.log(res, '登录未过期')
                var access_id = uni.getStorageSync('laiketuiAccessId') ;
                if(!access_id){
                    me.access_id1 = false
                    _this.showWin = true;
                    if(!me.fastTap){
                       me.fastTap = true; 
                    }
                }else{
                    _this.showWin = false
                    laiketui_mp_weixin_load(me, callback, args,authPage)
                }
            },
            fail: function (res) {
                me.access_id1 = false
                console.log(res, '登录过期了')
                uni.showToast({
                    title:me.language.message.noLogin,
                    icon:'none'
                })
                _this.showWin = true
            }
        })
    }

    /**
     * 游客类型界面需要检测授权是否过期调用
     * @param {Object} fromPage
     * @param {Object} callback
     * @param {Object} args
     */
    function laiketui_mp_weixin_checkauth (fromPage, callback, args) {
        var me = fromPage
        let LoingByHand = uni.getStorageSync('LoingByHand')
        let needRegister = uni.getStorageSync('needRegister')
        if (needRegister == me.LaiKeTuiCommon.LKT_NRL_TYPE.NRL && !LoingByHand) {
            uni.checkSession({
                success: function (res) {
                    console.log(res, '登录未过期')
                    laiketui_mp_weixin_load(me, callback, args)
                },
                fail: function (res) {
                    console.log(res, '登录过期了')
                }
            })
        }
    }

    export default {
        laiketui_mp_weixin_auth,
        laiketui_mp_weixin_load,
        laiketui_mp_weixin_checksession,
        laiketui_mp_weixin_checkauth,
    }
 
