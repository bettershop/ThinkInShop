a<template>
    <div class="box">
        <heads :title="status?language.setup2.loginPass.title:language.setup2.loginPass.title2" :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
        <toload :load="load">
            <ul class="setup-ul yh-setup-ul">
                <!-- 设置密码开始 -->
                <!-- 手机号码 -->
            <!--    <li v-if="!status" class="qu-box"> 
                        <view class="qu" >
                            <view>{{diqu.code}} +{{diqu.code2}} </view>
                            <view class="san-jiao"></view>
                        </view>
                    <input 
                        type="number" 
                        disabled="true" 
                        v-model="user_phone" 
                        readonly/>
                </li> -->
                <!-- 验证码 -->
             <!--   <li class="last" v-if="!status">
                    <input
                        type="number"
                        class="input"
                        placeholder-style="color:#b8b8b8;" 
                        :placeholder="language.setup2.loginPass.code_placeHolder" 
                        v-model="codeone"/>
                    <div class="color yh-color" v-if="display" @tap="_verif">{{language.setup2.loginPass.obtain}}</div>
                    <div class="active yh-color" v-if="!display">
                        <span ref="time" class="yh-time">{{ count }}S</span>
                        {{language.setup2.loginPass.again}}
                    </div>
                </li> -->
                <!-- 设置密码结束 -->
                
                <!-- 修改密码开始 -->
                <!-- 原密码 -->
                <li v-if="status">
                    <input
                        placeholder-style="color:#b8b8b8;" 
                        type="text"
                        :placeholder="language.setup2.loginPass.old_pass_placeHolder"
                        :password="LoginPWStatus"
                        v-model="password"/>
                    <img 
                        v-if="password.length"
                        :src="LoginPWStatus?pwHide:pwShow" 
                        @tap="pwStatus(1)" 
                        style='height: 40rpx;width: 40rpx;position: absolute;top: 30rpx;right: 30rpx;'/>
                </li>
                <!-- 输入新密码 -->
                <li style="position: relative;">
                    <input 
                        placeholder-style="color:#b8b8b8;" 
                        type="text"
                        :placeholder="language.setup2.loginPass.pass_placeHolder"
                        :password="LoginPWStatus1"
                        v-model="passone"/>
                    <img
                        v-if="passone.length"
                        :src="LoginPWStatus1?pwHide:pwShow" 
                        @tap="pwStatus(2)" 
                        style='height: 40rpx;width: 40rpx;position: absolute;top: 30rpx;right: 30rpx;'/>
                </li>
                <!-- 确认新密码 -->
                <li style="position: relative;">
                    <input 
                        placeholder-style="color:#b8b8b8;" 
                        type="text"
                        :placeholder="language.setup2.loginPass.new_password"
                        :password="LoginPWStatus2"
                        v-model="passtwo"/>
                    <img 
                        v-if="passtwo.length"
                        :src="LoginPWStatus2?pwHide:pwShow" 
                        @tap="pwStatus(3)" 
                        style='height: 40rpx;width: 40rpx;position: absolute;top: 30rpx;right: 30rpx;'/>
                </li>
                <!-- 修改密码结束 -->
                
                <!-- 确认按钮 -->
                <li id="setupButtomWrap" class="yh-setupButtomWrap">
                    <div class="yh-setup-buttom" :class="passone.length>5 && passtwo.length > 5 ? 'setup-buttom':'setup-buttom1'" @tap="_sub">
                        {{language.setup2.loginPass.confirm}}
                    </div>
                    <!-- #ifdef MP-WEIXIN -->
                    <div v-if="isset" class="setup-buttom yh-isset" @tap="_back">
                        {{language.setup2.loginPass.Back}}
                    </div>
                    <!-- #endif -->
                </li>
            </ul>
        </toload>
        <!-- 修改成功 -->
        <view class="xieyi" style="background-color: initial;" v-if="is_tishi">
            <view style="width: 272rpx;height: 272rpx;background-color: rgba(51, 51, 51, .9);">
                <view style="margin: 32rpx 0;text-align: center;margin-top: 64rpx;">
                    <image style="width: 68rpx;height: 68rpx;" :src="sus"></image>
                </view>
                <view class="xieyi_title" style="margin-bottom: 0;margin-top: 0;color: #fff;font-weight: 500;font-size: 32rpx;">{{is_tishi_content}}</view>
            </view>
        </view>
    </div>
</template>

<script>
    import {
        _timeCommon
    } from '@/static/js/setUp/setUp.js'
    export default {
        data() {
            return {
                diqu:{
                	code:'CN',
                	code2:'86',
                    num3: 156
                },
                //弹窗
                is_tishi: false,
                is_tishi_content: '',
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
                //
                load: false,
                title: '修改登录密码',
                title1: '设置登录密码',
                status: true,
                display: true,
                count: '',
                timer: null,
                code: '',
                name: '',
                password: '', //旧密码
                LoginPWStatus: true,
                LoginPWStatus1: true,
                LoginPWStatus2: true,
                pwShow: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/pwHide.png',
                pwHide: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/pwShow.png',
                passone: '', //新密码
                passtwo: '', //确认密码
                codeone: '', // 验证码
                user_phone: '',//手机号
                rez: '', // 新密码是否满足正则匹配
                fastTap: true,
                isset: false,
                back1: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/back2x.png',
                bgColor:[{
                            item: '#ffffff'
                        },
                        {
                            item: '#ffffff'
                        }
                    ]
            };
        },
        onLoad(res) {
            this.status = res.havePass == 'true';
            this.is_tishi_content = this.status?'修改成功':'设置成功'
            // #ifdef MP-WEIXIN
            var pages = getCurrentPages();
            var beforePage = pages[pages.length - 2];
            if (!beforePage) {
                this.isset = true;
            }
            // #endif
            this.load = true;
        },  
        onUnload(){
        	uni.removeStorageSync('diqu')
        },
        onShow() {
            if(uni.getStorageSync('diqu')){
            	this.diqu = JSON.parse(uni.getStorageSync('diqu'))
            } 
            //重新获取缓存中的user_phone
            this.user_phone = uni.getStorageSync('user_phone') ? uni.getStorageSync('user_phone') : this.$store.state.user_phone;
            // 手机号判断
            // if (!this.user_phone &&!this.status) {
            //     uni.showToast({
            //         title: this.language.setup2.paymentPass.Tips[0],
            //         duration: 1000,
            //         icon: 'none'
            //     });
            //     setTimeout(()=>{                    
            //         uni.redirectTo({
            //             url: '/pagesB/setUp/changePhone'
            //         }); 
            //     },1000)
            // }
        },
        methods: {
            pwStatus(type){
                let me = this
                if (type == 1) {
                    me.LoginPWStatus = !me.LoginPWStatus
                } else if (type == 2) {
                    me.LoginPWStatus1 = !me.LoginPWStatus1
                } else if (type == 3) {
                    me.LoginPWStatus2 = !me.LoginPWStatus2
                }
            },
            /**
             *
             * */
            _back() {
                uni.redirectTo({
                    url: '/pages/shell/shell?pageType=my'
                });
            },
            /**
             * 验证码发送1分钟倒计时
             * */
            _verif() {
                if (!this.fastTap) {
                    return;
                }
                this.fastTap = false;
                var data = {
                    api: 'app.user.secret_key',
                    phone: this.user_phone,
                    message_type: 0, // 短信类型 0.验证码 1.短信通知
                    message_type1: 4, // 短信类别 4.修改登录密码
                    cpc: this.diqu.code2, // 国家/地区
                    country_num:this.diqu.num3,  // 国家代码
                };

                this.$req
                    .post({
                        data
                    })
                    .then(res => {
                        this.fastTap = true;
                        let {
                            code,
                            message
                        } = res;
                        if (code != 200) {
                            uni.showToast({
                                title: message,
                                duration: 1000,
                                icon: 'none'
                            });
                        } else if (code == 200) {
                            // 倒计时数秒方法
                            _timeCommon(this);
                            const TIME_F = 300;
                            let count_f = TIME_F;
                            let timer_f = setInterval(() => {
                                if (count_f > 0) {
                                    count_f--;
                                } else {
                                    clearInterval(timer_f);
                                    timer_f = null;
                                    this.code = '';
                                }
                            }, 1000);
                        }
                    })
                    .catch(error => {
                        this.fastTap = true;
                    });
            },
            /**
             * 第一次密码输入
             * */
            _passone() {
                var re = /^[a-z0-9]{6,15}$/i;
                if (this.passone != '') {
                    var rez = re.test(this.passone);
                    if (rez == true) {
                        this.allok = true;
                    } else {
                        uni.showToast({
                            title: this.language.setup2.loginPass.Tips[0],
                            duration: 1000,
                            icon: 'none'
                        });
                        this.allok = false;
                        return false;
                    }
                }
            },
            /**
             * 确认密码
             * */
            _passtwo() {
                if (this.passone != '' && this.passtwo != '') {
                    if (this.passone == this.passtwo && this.passtwo != '') {
                        this.allok = true;
                    } else {
                        uni.showToast({
                            title: this.language.setup2.loginPass.Tips[1],
                            duration: 1000,
                            icon: 'none'
                        });
                        this.allok = false;
                        return false;
                    }
                }
            },
            /**
             *优化后用原密码修改密码的方式
             * */
            _sub() {
                if(this.passone.length < 6 || this.passtwo.length < 6){return}
                var re = /^[a-z0-9]{6,15}$/i;
                if (this.passone != '') {
                    this.rez = re.test(this.passone);
                }
                if (!this.fastTap) {
                    return;
                }
                this.fastTap = false;
                if (this.passone == '' || this.passtwo == '' ) {
                    uni.showToast({
                        title: this.language.setup2.loginPass.Tips[2],
                        duration: 1000,
                        icon: 'none',
                        success: res => {
                            this.fastTap = true;
                        }
                    });
                } else if (!this.rez) {
                    uni.showToast({
                        title: this.language.setup2.loginPass.Tips[3],
                        duration: 1000,
                        icon: 'none',
                        success: res => {
                            this.fastTap = true;
                        }
                    });
                } else if (this.passtwo != '' && this.passone != this.passtwo) {
                    uni.showToast({
                        title: this.language.setup2.loginPass.Tips[4],
                        duration: 1000,
                        icon: 'none',
                        success: res => {
                            this.fastTap = true;
                        }
                    });
                } else if (this.passone != '' && this.passone == this.passtwo ) {
                    // 发送请求
                    if (this.status) {
                        var data = {
                            api: 'app.user.updatepassword', //更新密码

                            password: this.password,
                            newPwd: this.passone,
                            confirm: this.passtwo,
                            
                            cpc: this.diqu.code2, // 国家/地区
                            country_num:this.diqu.num3,  // 国家代码
                        };
                    } else {
                        var data = {
                            api: 'app.user.set_password', //设置登录密码
                            password: this.passtwo,
                            phone: this.user_phone,
                            // keyCode: this.codeone,
                            
                            cpc: this.diqu.code2, // 国家/地区
                            country_num:this.diqu.num3,  // 国家代码
                        };
                    }

                    this.$req.post({
                            data
                        }).then(res => {
                            this.fastTap = true;
                            let {
                                code,
                                message
                            } = res;
                            if (code == 200) {
                                //设置成功
                                this.is_tishi = true
                                setTimeout(() => {
                                    this.is_tishi = false
                                }, 1000)   
                                setTimeout(() => {
                                    uni.redirectTo({
                                        url: '/pages/shell/shell?pageType=my'
                                    })
                                }, 1000);
                            } else {
                                uni.showToast({
                                    title: message,
                                    duration: 1000,
                                    icon: 'none'
                                });
                            }
                        })
                        .catch(error => {
                            this.fastTap = true;
                        });
                } else {
                    uni.showToast({
                        title: this.language.setup2.loginPass.Tips[2],
                        duration: 1000,
                        icon: 'none',
                        success: res => {
                            this.fastTap = true;
                        }
                    });
                }
            }
           
        }
    };
</script>
<style>
    page{
        background-color: #F4F5F6;
    }
</style>
<style scoped lang="less">
    @import url("@/laike.less");
    @import url('../../static/css/setUp/loginPassword.less');
    input {
        font-size: 32rpx;
    }
    .xieyi{
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background-color: rgba(0, 0, 0, .5);
        z-index: 99;
        display: flex;
        justify-content: center;
        align-items: center;
        >view{
            width: 640rpx;
            min-height: 100rpx;
            max-height: 486rpx;
            background: #FFFFFF;
            border-radius: 24rpx;
            .xieyi_btm{
                height: 108rpx;
                color: @D73B48;
                display: flex;
                justify-content: center;
                align-items: center;
                border-top: 0.5px solid rgba(0,0,0,.1);
                font-weight: bold;
                font-size: 32rpx;
            }
            .xieyi_title{
                display: flex;
                justify-content: center;
                align-items: center;
                font-size: 32rpx;
                
                font-weight: 500;
                color: #333333;
                line-height: 44rpx;
                margin-top: 64rpx;
                margin-bottom: 32rpx;
                font-weight: bold;
                font-size: 32rpx;
            }
            .xieyi_text{
                width: 100%;
                max-height: 236rpx;
                overflow-y: scroll;
                padding: 0 32rpx;
                box-sizing: border-box;
            }
        }
    }
</style>
