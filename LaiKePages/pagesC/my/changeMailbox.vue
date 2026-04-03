<template>
    <view class="MPLogin" :class="email&&'box'">
        <!-- 头部 -->
        <heads :title="title" :ishead_w="2" :bgColor="bgColor" titleColor="#333333"></heads>
        <!-- 已绑定的情况下 -->
		<template v-if="email">
            <view class="email-box">
                
                <view class="email-info">
                    <view>您已绑定的邮箱地址</view>
                    <view>{{email}}</view>
                </view>
                <view class="up-email-but" @tap='upEmail'>
                    更换登录邮箱
                </view>
            </view> 
        </template>
        <!-- 更换绑定邮箱 -->
        <template v-else>
            <view class="up-email-box"> 
                <view class="email-info">
                    <input placeholder="请输入邮箱地址" v-model="emailText" />
                </view>
                <view class="email-info">
                  <div class="codeBox">
                      <input 
                          type="number"
                          maxlength="6"
                          :placeholder="language.login.page2.code_placeHolder"
                          placeholder-style="color:#999999;" 
                          style="margin-bottom: 0;border: 0;flex: 1;color: #333333;"
                          v-model="phone_code"
                          @input='_codeChangeCode'
                          @focus='_codeF()'
                          @blur='_codeB()'/>
                      <p class='login_p' :style="{color:time_code!='获取验证码'?'#666666':''}" @tap="getAutByEmail()">
                          {{time_code}}
                      </p>
                  </div>
                </view>
                <view class="up-email-but" @tap="submit">
                    确认
                </view>
            </view> 
        </template>
    </view>
</template>
<script> 
    import showToast from '@/components/aComponents/showToast.vue'
    export default {
        data() {
            return { 
                phone_code:'',
                title:'',
                email:'',
                time_code:'',
                emailText:'',
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
                logUrl: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/logUrl.png',
                imgurl:this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/xuanzehui2x.png',
            }
        },
        components:{
            showToast
        },
        onLoad(option){
            if(option.email){
                this.email = option.email
                this.title = '已绑定邮箱'
            }else{
                this.title = '更换绑定邮箱'
            }
        },
        onShow(){
            this.time_code = this.language.login.page2.getCode
        },
        methods: {
            //跳转
            _navigateTo(){
                uni.navigateTo({
                    url: '/pagesD/login/newLogin'
                })
            }, 
            upEmail(){
              this.email = ''  
            },
            getAutByEmail(){
                if(this.emailText && this.emailText.length == 0){
                    uni.showToast({
                        title:this.language.login.page2.tel_EmailHolder,
                        icon:'none'
                    })
                    return
                }
                const regex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}/g;
                 if(!regex.test(this.emailText)){
                     uni.showToast({
                         title:this.language.openInvoice.qsrzqddzyx,
                         icon:'none'
                     }) 
                     return
                 }
                
                const data ={
                    api:'app.User.send_email_verification_code',
                    email:this.emailText
                }
                this.$req.post({data}).then(res => {
                    if(res.code == 200){
                        uni.showToast({
                           title:this.language.login.page2.sendCode,
                           icon:'none'
                        })
                        //验证码倒计时
                        const TIME_COUNT = 60
                        if(uni.getStorageSync('language') == 'en'){
                            this.time_code = this.language.login.page2.countdown + ' ' + TIME_COUNT + `s`
                        }else{
                            this.time_code = TIME_COUNT + `s` + this.language.login.page2.countdown
                        }
                        if (!this.timer) {
                            this.count = TIME_COUNT
                            this.timer = setInterval(() => {
                                if (this.count > 0 && this.count <= TIME_COUNT) {
                                    this.count--
                                    if(uni.getStorageSync('language') == 'en'){
                                        this.time_code = this.language.login.page2.countdown + ' ' + this.count + `s`
                                    }else{
                                        this.time_code = this.count + `s` + this.language.login.page2.countdown
                                    }
                                } else {
                                    clearInterval(this.timer)
                                    this.time_code = this.language.login.page2.getCode
                                    this.timer = null
                                    this.count = ''
                                }
                            }, 1000)
                        }
                    }else{
                        uni.showToast({
                           title:res.message,
                           icon:'none'
                        })
                    }
                })
            },
            submit(){
                if(!this.emailText || this.emailText.length == 0){
                    uni.showToast({
                        title:this.language.login.page2.tel_EmailHolder,
                        icon:'none'
                    })
                    return
                }
                if(!this.phone_code || this.phone_code.length == 0){
                    uni.showToast({
                        title: this.language.register.yzmEmpty,
                        icon:'none'
                    })
                    return
                }
                const regex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}/g;
                 if(!regex.test(this.emailText)){
                     uni.showToast({
                         title:this.language.openInvoice.qsrzqddzyx,
                         icon:'none'
                     }) 
                     return
                 }
                const data ={
                    api: 'app.user.set_user',
                    type:1,
                    e_mail:this.emailText,
                    keyCode:this.phone_code,
                }
                this.$req.post({data}).then(res=>{
                    if(res.code == 200){
                        uni.showToast({
                            title:'操作成功!',
                            icon:'success'
                        })
                        setTimeout(()=>{                            
                            uni.reLaunch({
                                url: '/pages/shell/shell?pageType=my',
                                success: function () {
                                    if (wx_status != 1) {
                                        me.$store.state.shouquan = true
                                    }
                                }
                            })
                        },1500)
                    }else{
                        uni.showToast({
                            title:res.message,
                            icon:'none'
                        })
                    }
                })
            }
        }
    }
</script>

<style lang="less" scoped>
    @import url("@/laike.less");
     .box{
        height: 100vh !important;
        background-color: #f4f5f6 !important;
    }
    .isClick{
        position: absolute;
        z-index: 999;
        width: 100rpx;
        height: 100rpx;
    }
    .email-box{
            background: #fff;
            border-radius: 50rpx;
            padding: 40rpx 0rpx 112rpx;
            margin-top: 20rpx;
        text-align: center;
        .email-info {
            view:nth-child(1){            
                font-size: large;
                font-weight: 700;
                margin: 26rpx 0rpx;
            }
            view:nth-child(2){
                color:#999999
            }
        }
        .up-email-but{
            color: #fff;
            border-radius: 100rpx;
            height: 80rpx;
            width: 300rpx;
            line-height: 80rpx;
            background: linear-gradient(to right, #fb5256, #f26e6e);
            margin-left: 50%;
            transform: translate(-50%);
            margin-top: 60rpx;
        }
    }
    .up-email-box{
        margin: 0rpx 40rpx;
        margin-top: 20rpx;
        >view {
            border-bottom: 1rpx solid #999;
            padding-bottom: 10rpx;
            margin-bottom: 20rpx;
        }
        >view:last-child {
             border: none;
        }
        .login_p {
            z-index: 99;
            padding: 10rpx;
            color: @D73B48;
            font-size: 32rpx;
            padding-right: 0;
        }
        
        .email-info{
            height: 60rpx;
        }
        .codeBox[class] {
            // width: 90%;
            margin: 0 auto;
            //border-bottom: 1px solid #dddddd;
            display: flex;
            align-items: center;
        }
        .codeBox{
            >input{
                border-bottom: initial;
                color: #333333;
                font-size: 32rpx;
            }
        }
        .up-email-but{
            text-align: center;
            color: #fff;
            border-radius: 100rpx;
            height: 80rpx; 
            line-height: 80rpx;
            background: linear-gradient(to right, #fb5256, #f26e6e);
            margin-top: 60rpx;
        }
    }

</style>
