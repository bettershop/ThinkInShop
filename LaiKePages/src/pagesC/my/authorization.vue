<template>
    <view class="MPLogin" :style="{ backgroundImage: 'url(' + bg + ')' }">
        <!-- 头部 -->
        <heads :title="language.toasts.login.denglu" :ishead_w="2" :bgColor="bgColor" titleColor="#333333"></heads>
        <!-- logo -->
        <view class="logo">
            <image :src="appLogo"></image>       
        </view>
        <!-- 绑定手机号 -->
        <view class="auth">
            <button :disabled="disabled" type="default" open-type="getPhoneNumber" @getphonenumber="getPhoneNumber" @tap="_shouquan">微信手机号一键登录</button>
            <button :disabled="disabled" class="login" type="default" @tap="_navigateTo">密码登录/注册</button>
        </view>
        <!-- 协议 -->
        <view class='agreement'>
            <view @tap="radio_a" class="agreementImg">
                <image :src="imgurl"></image>
                <!-- 放大点击事件范围 -->
                <view class="isClick"></view>
            </view>
            {{language.toasts.login.yjbd}}
            <span style="color: #5297F7;" @tap.shop="navTo('/pagesD/login/agreement')">《{{Agreement}}》</span>
            <span style="text-decoration:none">{{language.toasts.login.he}}</span>
            <span style="color: #5297F7;" @tap.shop="navTo('/pagesD/login/privacy')">《{{Agreement2}}》</span>
        </view>
        <!-- 协议弹窗 -->
        <show-toast :is_showToast="is_showToast" :is_showToast_obj="is_showToast_obj" :load="load" @richText="_xieyiShow1"></show-toast>
    </view>
</template>
<script>
    import htmlParser from '@/common/html-parser.js'
    import showToast from '@/components/aComponents/showToast.vue'
    import {img} from "@/static/js/login/imgList.js";
    export default {
        data() {
            return {
                bgColor: [
                    {
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                user: {
                    avatarUrl: '',
                    nickName: '',
                    gender: '0',
                },
                ewmLink: '',
                isShows: true,//授权登录弹窗
                name: '',
                imgUrl: '',
                Agreement: '',
                Agreement2: '',
                appLogo:"",//logo
                content: '',
                contentNodes: '',
                openType: '',
                load: false,
                is_showToast: 0,
                is_showToast_obj: {},
                r1_checked: false,
                disabled: false, //防止重复点击
                openidPromise: null,
                logUrl: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/logUrl.png',
                imgurl:this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/xuanzehui2x.png',
                bg: img(this).bg,
            }
        },
        components:{
            showToast
        },
        onLoad(){
            //获取协议名称
            this._gerXYName()
            //获取默认头像呢称
            this._getImgName()
            //退出当前登录 解决被挤掉时 被挤的人因为微信seeion没有过期所以还在登录状态问题
            this.quit()
            //微信授权  一键授权绑定手机号开始
            this.authUser()
        },
        onShow(){},
        methods: {
            //跳转
            _navigateTo(){
                uni.navigateTo({
                    url: '/pagesD/login/newLogin'
                })
            },
            //勾选协议提示
            _shouquan(){
                if(this.r1_checked == false){
                    uni.showToast({
                        title: this.language.toasts.login.qxgxxy,
                        icon: 'none'
                    })
                    return
                }
                //开启防止重复点击
                this.disabled = true
            },
            //我知道了
            _xieyiShow1(){
                this.is_showToast = 0
            },
            //选择协议
            radio_a(){
                
                this.r1_checked = !this.r1_checked
                this.imgurl = this.r1_checked ? 
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/new_red_select.png'
                :
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/xuanzehui2x.png'
                
            },
            //获取协议名称
            _gerXYName(){
                let data = {
                    api: 'app.login.index',
                }
                this.$req.post({ data }).then(res => {
                    if (res.code == 200) {
                        this.Agreement = res.data.Agreement
                        this.Agreement2 = res.data.Agreement_1
                        this.appLogo =res.data.appLogo
                    }
                })
            },
            //获取默认头像呢称
            _getImgName(){
                let data = {
                    api: 'app.user.index',
                };
                this.$req.post({
                    data
                }).then(res => {
                    if(res.code == 200){
                        this.name = res.data.defaultUserName
                        this.imgUrl = res.data.defaultHeaderImgUrl
                    }
                })
            },
            _getStorageUserinfo() {
                let userinfo = uni.getStorageSync('userinfo') || {}
                if (typeof userinfo === 'string') {
                    try {
                        userinfo = JSON.parse(userinfo)
                    } catch (error) {
                        userinfo = {}
                    }
                }
                return userinfo || {}
            },
            //微信授权  这里只是授权拿到openid，并没有登录成功
            authUser(force = false) {
                const userinfo = this._getStorageUserinfo()
                if (!force && userinfo.openid) {
                    return Promise.resolve(userinfo.openid)
                }
                if (this.openidPromise) {
                    return this.openidPromise
                }
                const me = this
                this.openidPromise = new Promise((resolve) => {
                    uni.login({
                        success: function(res) {
                            if (!res.code) {
                                uni.showToast({
                                    title: me.language.toasts.authorized.failure,
                                    duration: 1000,
                                    icon: 'none'
                                })
                                resolve('')
                                return
                            }
                            let data = {
                                code: res.code,
                                api: 'app.login.appletsParam',
                            }
                            me.$req.post({ data }).then(res => {
                                if (res.code == 200 && res.data && res.data.openid) {
                                    let localUserinfo = me._getStorageUserinfo()
                                    localUserinfo['openid'] = res.data.openid
                                    // 提示漏洞，不能使用session_key，这个可以被别人拿去加密使用
                                    // localUserinfo['session_key'] = res.data.session_key
                                    uni.setStorageSync('userinfo', localUserinfo)
                                    resolve(res.data.openid)
                                } else {
                                    resolve('')
                                }
                            }).catch(() => {
                                resolve('')
                            })
                        },
                        fail() {
                            resolve('')
                        }
                    })
                }).finally(() => {
                    me.openidPromise = null
                })
                return this.openidPromise
            },
            //获取需要绑定的手机号
            async getPhoneNumber(e) {
                //需要勾选协议
                if(this.r1_checked == false){
                    return
                }
                if (!e.detail.encryptedData || !e.detail.iv) {
                    uni.showToast({
                        title: this.language.toasts.login.qxzqtfs,
                        icon: 'none'
                    })
                    this.disabled = false
                    return
                }
                let openid = this._getStorageUserinfo().openid
                if (!openid) {
                    openid = await this.authUser(true)
                }
                if (!openid) {
                    uni.showToast({
                        title: this.language.toasts.authorized.failure,
                        icon: 'none'
                    })
                    this.disabled = false
                    return
                }
                let data =  {
                        openid: openid,
                        encryptedData: e.detail.encryptedData,
                        iv: e.detail.iv,
                        api: 'app.login.getWxInfo',
                    }
                this.$req.post({data}).then((res)=>{
                    if (res.data && res.data.phoneNumber) {
                        //存缓存 用于绑定手机号
                        uni.setStorageSync('user_phone', res.data.phoneNumber)
                        //注册登录
                        this._login()
                    } else {
                        uni.showToast({
                            title: this.language.toasts.login.qxzqtfs,
                            icon: 'none'
                        })
                        //关闭防止重复点击
                        this.disabled = false
                    }
                }).catch(() => {
                    uni.showToast({
                        title: this.language.toasts.login.dlsb,
                        icon: 'none'
                    })
                    this.disabled = false
                });
            },
            //注册登录  这里进行登录操作
            _login(){
                let data = {
                    nickName: this.name?this.name:'来客推',
                    headimgurl: this.imgUrl?this.imgUrl:this.user.avatarUrl,
                    sex: this.user.gender,
                    openid: uni.getStorageSync('userinfo').openid,
                    api: 'app.login.user',
                    pid: uni.getStorageSync('fatherId'),
                }
                this.$req.post({data}).then(res => {
                    let code = res.code;
                    if (code == 200) {
                        this.isShows = false
                        uni.setStorageSync('isHomeShow', 1)
                        let access_id = res.data.access_id
                        let userinfo = uni.getStorageSync('userinfo')
                        userinfo['user'] = res.data.user
                        this.access_id = res.data.access_id
                        this.$store.state.access_id = res.data.access_id
                        uni.setStorageSync('userinfo', userinfo)
                        uni.setStorageSync('user_id', res.data.user.user_id)
                        //调用父类的改变登录状态方法
                        //手动登陆标志为false
                        uni.setStorageSync('LoingByHand', false)
                        uni.setStorageSync('laiketuiAccessId', this.access_id)
                        uni.setStorageSync('online', true)
                         // 缓存公告
                         uni.setStorageSync("laike_move_uaerInfo", res.data)
                            // 是否提示公告弹窗
                            if((uni.getStorageSync('laike_move_uaerInfo').systemMsgType == 2||uni.getStorageSync('laike_move_uaerInfo').systemMsgType == 3) && uni.getStorageSync('laike_move_uaerInfo').type != 0) {
                            uni.setStorageSync('versionUpdate3',true)
                        }
                        
                        // 是否显示
                        if (this.title == '个人中心') {
                            this.changeLoginStatus()
                        } else {
                            this.access_id1 = true
                            this.$emit('pChangeLoginStatus')
                        }
                        uni.removeStorageSync('signFlag');
                        //账号合并
                        this._userMerging()
                    } else {
                        uni.showToast({
                            title: this.language.toasts.login.dlsb,
                            icon: 'none'
                        })
                        //关闭防止重复点击
                        this.disabled = false
                    }
                })
            },
            //获取用户信息 判断是否需要进行账号合并
            _userMerging(){
                let data = {
                  api: 'app.user.index',
                  mobile: uni.getStorageSync('user_phone') ? uni.getStorageSync('user_phone') : ''
                };
                this.$req.post({data}).then(res => {
                    if(res.code == 200){
                        let synchronize_type = res.data.data.synchronize_type
                        if (synchronize_type == 1) {
                            var data = {
                                api: 'app.user.synchronizeAccount',
                                mobile: uni.getStorageSync('user_phone'),
                            };
                            // #ifdef MP-WEIXIN
                                data.source = 1
                            // #endif
                            
                            // #ifdef MP-ALIPAY
                                data.source = 2
                            // #endif
                            this.$req.post({data}).then(res => {
                                if (res.code == 200) {
                                    let userinfo = uni.getStorageSync('userinfo')
                                    userinfo['user'] = res.data
                                    uni.setStorageSync('userinfo', userinfo)
                                    uni.setStorageSync('user_id', userinfo['user'].user_id)
                                } else {
                                    uni.showToast({
                                      title: '账号同步失败！请稍后再试。',
                                      icon: 'none'
                                    })
                                    return
                                }
                            })
                        } else {
                        }
                        //返回上一页
                        setTimeout(()=>{
                            uni.navigateBack({
                                delta: 1,
                            })
                        },1000)
                    } else {
                        uni.showToast({
                          title: '获取用户信息失败！请稍后再试。',
                          icon: 'none'
                        })
                        //关闭防止重复点击
                        this.disabled = false
                    }
                })
            },
            //退出登录并清除缓存
            quit(){
                uni.removeStorage({
                    key: 'history'
                });
                uni.removeStorage({
                    key: 'user_phone'
                });
                uni.removeStorage({
                    key: 'hotStatu'
                });
                this.$store.state.cart = [];
                this.$store.state.cart_id = [];
                this.$store.state.nCart = [];
                this.$store.state.shouquan = false;
                uni.removeStorageSync('userinfo');
                uni.removeStorageSync('laiketuiAccessId');
                uni.removeStorageSync('online');
                uni.removeStorageSync('LoingByHand');
                
                uni.removeStorageSync('setLangFlag')
                let data = {
                    api: 'app.login.quit',
                };
                this.$req.post({data}).then(res => {
                    this.$store.state.access_id = '';
                    uni.setStorageSync('isHomeShow',1)
                    uni.removeStorage({
                        key: 'access_id'
                    });
                });
            },
            //用户协议
            _yhxy(e){
                var me = this
                let data = {
                    api: 'app.login.register_agreement',
                }
                this.$req.post({data}).then(res => {
                    if (res.data.content) {
                        //吧view标签替换为html的P标签
                        me.content = '<div style=\'padding-left:8px;padding-right:8px;color: #999999;\'>' + res.data.content.replace(new RegExp('<view', 'gm'), '<p').replace(new RegExp('</view>', 'gm'), '</p>') + '</div>'
                        var htmlString = me.content.replace(/\\/g, '').replace(/<img/g, '<img style="display:none;"')
                        htmlString = htmlString.replace('<div style=\'padding-left:8px;padding-right:8px;\'>', '<div style="padding: 25px 15px;">')
                        me.contentNodes = htmlParser(htmlString)
                        // #ifndef MP-ALIPAY
                        setTimeout(function () {
                            me.load = true
                        }, 50)
                        // #endif
                        // #ifdef MP-ALIPAY
                        me.load = true
                        // #endif
                        me.is_showToast = 2
                        me.is_showToast_obj.title = me.language.toasts.login.yhxy
                        me.is_showToast_obj.button = me.language.toasts.login.wzdl
                        me.is_showToast_obj.contentNodes = me.contentNodes
                    }  
                })
            },
            //隐私协议
            _ysxy(e){    
                var me = this
                let data = {
                    api: 'app.login.privacy_agreement',
                }
                this.$req.post({data}).then(res => {
                    if (res.data.content) {
                        //吧view标签替换为html的P标签
                        me.content = '<div style=\'padding-left:8px;padding-right:8px;color: #999999;\'>' + res.data.content.replace(new RegExp('<view', 'gm'), '<p').replace(new RegExp('</view>', 'gm'), '</p>') + '</div>'
                        var htmlString = me.content.replace(/\\/g, '').replace(/<img/g, '<img style="display:none;"')
                        htmlString = htmlString.replace('<div style=\'padding-left:8px;padding-right:8px;\'>', '<div style="padding: 25px 15px;">')
                        me.contentNodes = htmlParser(htmlString)
                        // #ifndef MP-ALIPAY
                        setTimeout(function () {
                            me.load = true
                        }, 50)
                        // #endif
                        // #ifdef MP-ALIPAY
                        me.load = true
                        // #endif
                        me.is_showToast = 2
                        me.is_showToast_obj.title = me.language.toasts.login.yszc
                        me.is_showToast_obj.button = me.language.toasts.login.wzdl
                        me.is_showToast_obj.contentNodes = me.contentNodes
                    }else{
                        me.load = true
                    }
                })
            },
        
        }
    }
</script>

<style lang="less" scoped> @import url("@/laike.less");
    .MPLogin{
        background-size: cover;
        background-repeat: no-repeat;
        background-position: center;
        min-height: 100vh;
        width: 100%;
    }
    .isClick{
        position: absolute;
        z-index: 999;
        width: 100rpx;
        height: 100rpx;
    }
    
    .popover{
        position: fixed;
        top: 0;
        bottom: 0;
        left:0;
        right: 0;
        z-index: 999;
        background-color: rgba(0, 0, 0, .5); 
        display: flex;
        align-items: center;
        justify-content: center;
    }
    

    .bounced_buttom_left {

        width: 50%;

        color: #808080;

        height: 80rpx;

        line-height: 80rpx;

        text-align: center;

    }



    .bounced_buttom_rigth {

        width: 50%;

        color: #157dfb;

        height: 80rpx;

        line-height: 80rpx;

        text-align: center;

    }



    .bounced_buttom {

        width: 100%;

        height: 130rpx;

        display: flex;

        justify-content: center;

        align-items: center;

        flex-direction: row;

    }



    .weui-input {

        display: flex;

    }



    .bounced_body_tx_input {

        width: 70%;

        height: 100rpx;

        display: flex;

        justify-content: center;

        align-items: center;

    }



    .bounced_body_tx_tbn {

        width: 100rpx;

        height: 100rpx;

    }



    .avatar-wrapper {

        width: 100rpx;

        height: 100rpx;

        padding: 0;

    }



    .bounced_body_nc {

        margin-top: 30rpx;

        width: 100%;

        height: 130rpx;

        display: flex;

        justify-content: space-between;

        border-bottom: #999999 solid 2rpx;

    }



    .bounced_body_tx_txt {

        height: 100rpx;

        line-height: 100rpx;

        text-align: center;

    }



    .bounced_body {

        width: 60%;

        display: flex;

        flex-direction: column;

        margin-top: 50rpx;

    }



    .bounced_body_tx {

        width: 100%;

        height: 130rpx;

        display: flex;

        justify-content: space-between;

        border-bottom: #999999 solid 2rpx;

    }



    .avatar {

        width: 100rpx;

        height: 100rpx;

    }



    .bounced {

        width: 700rpx;

        background-color: #FFFFFF;

        display: flex;

        flex-direction: column;

        justify-content: center;

        align-items: center;

        border-radius: 20rpx;

    }



    /deep/.bounced_top_img_img {

        width: 100rpx;

        height: 100rpx;

        border-radius: 50rpx;

    }



    .bounced_top {

        width: 100%;

        margin-top: 50rpx;

        display: flex;

        flex-direction: column;

        justify-content: center;

        align-items: center;

    }



    .bounced_top_img {

        width: 100rpx;

        height: 100rpx;

        margin-bottom: 20rpx;

    }

    .logo{
        display: flex;
        justify-content: center;
        padding-top: 152rpx;
        padding-bottom: 208rpx;
        >image{
            width: 160rpx;
            height: 160rpx;
            border-radius: 50%;
        }
    }

    .auth {
        display: flex;
        align-items: center;
        flex-direction: column;
        button {
            width: 686rpx;
            height: 100rpx;
            background: #FA5151;
            border-radius: 24rpx;
            font-size: 32rpx;
            color: #FFFFFF;
            line-height: 92rpx;
        }
        button::after {
            border: 0;
        }
        .login{
            background: initial;
            border: 2rpx solid #FA5151;
            color: #FA5151;
            margin-top: 40rpx;
        }
    }
    
    .agreement{
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24rpx;
        line-height: 32rpx;
        color: #999999;
        margin-top: 96rpx;
        .agreementImg{
            position: relative;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-right: 16rpx;
            >image{
                width: 32rpx;
                height: 32rpx;
            }
        }
    }

</style>
