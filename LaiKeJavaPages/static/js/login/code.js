//获取验证码
export function getCode (me,number) {
    let data
    if(me.activeTab == 'phone'){
        data ={
            api: 'app.user.secret_key',
            phone: me.phone,
            cpc:me.code2,
        }
        data.message_type1 = number
        data.message_type = 0
    }else{
        data = {
            api:'app.User.send_email_verification_code',
            email:me.email
        }         
    }

    me.$req.post({data}).then(res => {
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
                        me.time_code = '重新获取'
                        me.timer = null
                        me.count = ''
                    }
                }, 1000)
            }
        } else {
          uni.showToast({
            title: res.message,
            duration: 1500,
            icon: 'none'
          })
          clearInterval(me.timer)
          me.time_code = '重新获取'
          me.fall = true
          me.timer = null
          me.count = ''          
        }          
    })
}
