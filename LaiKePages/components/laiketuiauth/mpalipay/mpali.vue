<script>
    /**
     * 检查是否已经授权
     * @param {Object} fromPage
     * @param {Object} callback
     * @param {Object} args
     */
    function laiketui_mp_alipay_check (authpage, me, callback, args) {
        //获取code
        my.getAuthCode({
            success: (res) => {
                const data = {
                    alimp_auth_code: res.authCode,
                    api: 'app.login.mpaliUserLogin',
                }
                me.$req.post({ data }).then((res) => {
                    var code = res.code
                    // var msg = res.msg
                    var msg = res.message
                    if (code == 200) {
                        //查询到了用户信息
                        // let userinfo = res.userInfo
                        let userinfo = res.data.userInfo
                        if (authpage != null) {
                            authpage.showWin = false
                        }
                        let access_id = userinfo.access_id
                        uni.setStorageSync('zfb_id', userinfo['zfb_id'])
                        me.access_id = access_id
                        me.$store.state.access_id = access_id
                        uni.setStorageSync('userinfo', userinfo)
                        uni.setStorageSync('access_id', access_id)
                        uni.setStorageSync('laiketuiAccessId', me.access_id)
                        uni.setStorageSync('online', true)
                        if (me.changeLoginStatus) {
                            me.changeLoginStatus()
                        }
                        if (callback && args) {
                            callback(args)
                        } else if (callback) {
                            callback()
                        }

                    } else if (code == 203) {
                        // 没有查询到用户信息，弹窗授权
                        if (authpage != null) {
                            me.access_id1 = false
                            authpage.showWin = true
                        }

                        // let zfb_id = res.zfb_id
                        let zfb_id = res.data.zfb_id
                        if (zfb_id) {
                            uni.setStorageSync('zfb_id', zfb_id)
                        }
                    } else {
                        //其他
                        if (authpage != null) {
                            authpage.showWin = false
                        }
                        my.showToast({
                            type: 'fail',
                            content: msg,
                            duration: 1000
                        })
                    }
                }).catch((erro) => {
                    console.log(erro)
                    my.showToast({
                        type: 'fail',
                        content: '操作失败',
                        duration: 1000
                    })
                })
            },
        })
    }

    /**
     * 授权并获取用户信息
     * @param {Object} res
     * @param {Object} me
     */
    function laiketui_mp_alipay_userInfo (res, me) {
        my.getOpenUserInfo({
            fail: (res) => {
                console.log(res)
            },
            success: (res) => {
                let userInfo = JSON.parse(res.response).response
                let zfb_id = uni.getStorageSync('zfb_id')

                const data = {
                    alimp_auth_code: res.authCode,
                    api: 'app.login.updateAliUser',
                    nickName: userInfo.nickName,
                    headimgurl: userInfo.avatar,
                    sex: userInfo.gender,
                    zfb_id: zfb_id
                }
                me.$req.post({ data }).then((res1) => {
                    var code = res1.code
                    if (code == 200) {
                        uni.setStorageSync('isHomeShow',1)
                        
                        me.showWin = false
                        // let userinfo = res1.userInfo
                        let userinfo = res1.data.userInfo
                        let access_id = userinfo.access_id
                        uni.setStorageSync('zfb_id', userinfo['zfb_id'])
                        me.access_id = access_id
                        me.$store.state.access_id = access_id
                        uni.setStorageSync('userinfo', userinfo)
                        uni.setStorageSync('access_id', access_id)
                        uni.setStorageSync('LoingByHand', false)
                        uni.setStorageSync('laiketuiAccessId', me.access_id)
                        uni.setStorageSync('online', true)
                        
                        if(me.title == '个人中心'){
                            me.changeLoginStatus()
                        }else{
                            me.access_id1 = true
                            me.$emit('pChangeLoginStatus')
                        }
                        
                        my.showToast({
                            type: 'Success',
                            content: '授权成功',
                            duration: 1000
                        })
                    } else {
                        my.showToast({
                            type: 'fail',
                            content: '操作失败',
                            duration: 1000
                        })
                    }
                }).catch(erro => {
                    me.showWin = false
                })
            }
        })
    }

    export default {
        laiketui_mp_alipay_check,
        laiketui_mp_alipay_userInfo,
    }
</script>
