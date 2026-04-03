<template>
    <view class="MPLogin">
        <!-- 头部 -->
        <heads :title="title" :ishead_w="2" :bgColor="bgColor" titleColor="#333333"></heads>
        <!-- logo -->
        <view class="logo">
            <image :src="appLogo"></image>       
        </view>
        <!-- 绑定手机号 -->
        <view class="auth">
            <button :disabled="disabled" type="default" open-type="getUserInfo" @getuserinfo="_getuserinfo">开始授权</button>
        </view>
        <!-- 提示弹窗 -->
        <show-toast :is_showToast="is_showToast1" :is_showToast_obj="is_showToast_obj1"></show-toast>
        <!-- 获取微信名称弹窗 -->
        <uni-popup ref="popup" :mask-click="false">
            <view class="bounced">
                <view class="bounced_top">
                    <view class="bounced_top_img">
                        <image :src="user.avatarUrl" class="bounced_top_img_img"></image>
                    </view>
                    <view class="bounced_top_title">提现绑定微信名称</view>
                </view>
                <view class="bounced_body">
                    <view class="bounced_body_nc">
                        <view class="bounced_body_tx_txt">昵称</view>
                        <view class="bounced_body_tx_input">
                            <input type="nickname" class="weui-input" @blur="userNameInput" placeholder="请输入昵称" />
                        </view>
                    </view>
                </view>
                <view class="bounced_buttom">
                    <view class="bounced_buttom_left" @click="close">拒绝</view>
                    <view class="bounced_buttom_rigth" @click="_setWxName">允许</view>
                </view>
            </view>
        </uni-popup>
    </view>
</template>
<script>
    import htmlParser from '@/common/html-parser.js'
    import showToast from '@/components/aComponents/showToast.vue'
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
                title: '',
                type: '',//前一个页面
                appLogo:"",//logo
                disabled: false, //防止重复点击
                openid: '',//用户信息唯一openid
                nickName: '',//绑定微信名称
                is_showToast1: 0,//是否显示
                is_showToast_obj1: {},//弹窗显示文字
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
            }
        },
        components:{
            showToast
        },
        onLoad(option){
            //标题
            this.title = '绑定微信'
            //前一个页面
            this.type = option.type
            //获取logo
            this._gerXYName()
            //微信授权  一键授权绑定手机号开始
            this.authUser()
        },
        onShow(){
            this.$refs.popup.open('center')
        },
        methods: {
            //获取昵称输入内容
            userNameInput(e) {
                this.nickName = e.detail.value
            },
            //隐藏弹窗
            close() {
                this.$refs.popup.close()
            },
            //获取协议名称
            _gerXYName(){
                let data = {
                    api: 'app.login.index',
                }
                this.$req.post({ data }).then(res => {
                    if (res.code == 200) {
                        this.appLogo =res.data.appLogo
                    }
                })
            },
            //微信授权  这里只是授权拿到session_key，并没有登录成功
            authUser() {
                var me = this
                uni.login({
                    success: function(res) {
                        if (res.code) {
                            let data = {
                                code: res.code,
                                api: 'app.login.appletsParam',
                            }
                            me.$req.post({
                                data
                            }).then(res => {
                                if (res.code == 200) {
                                    me.openid = res.data.openid
                                } else {
                                }
                            })
                        } else {
                            uni.showToast({
                                title: me.language.toasts.authorized.failure,
                                duration: 1000,
                                icon: 'none'
                            })
                        }
                    },
                })
            },
            //获取用户信息
            _getuserinfo(e) {
                if(e && e.detail && e.detail.userInfo){
                } else {
                }
                this.$refs.popup.open('center')
            },
            //绑定微信名称
            _setWxName(userInfo){
                let data = {
                    api: 'app.user.wxBind',
                    openId: this.openid,
                    wxName: this.nickName,
                }
                this.$req.post({ data }).then(res => {
                    if (res.code == 200) {
                        this.is_showToast1 = 1
                        this.is_showToast_obj1.imgUrl = this.sus
                        this.is_showToast_obj1.title = '绑定成功'
                        setTimeout(()=>{
                            this.is_showToast1 = 0
                            uni.navigateBack({delta: 1})
                        }, 1500)
                    } else {
                        uni.showToast({
                            title: res.message,
                            icon: 'none'
                        })
                    }
                })
            },
        }
    }
</script>

<style lang="less" scoped> @import url("@/laike.less");
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
            height: 92rpx;
            background: #FA5151;
            border-radius: 46rpx;
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
