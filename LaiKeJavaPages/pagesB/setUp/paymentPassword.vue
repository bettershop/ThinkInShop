<template>
    <div class="box">
        <heads :title="language.setup2.paymentPass.title" :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
        <toload :load="load">
            <ul class="setup-ul yh-setup-ul">
                <li class="qu-box">
                    <view class="qu"  >
                        <view>{{diqu.code}} +{{diqu.code2}} </view>
                        <view class="san-jiao"></view>
                    </view>
                
                    <input type="number" style="" v-model="user_phone" disabled="true" /></li>

                <li class="last">
                    <input placeholder-style="color: #b8b8b8;" class="input" :class="{ marginleft: display }" type="number" :placeholder="language.setup2.payment.code_placeHolder" v-model="codeone" />

                    <div class="color yh-color" v-if="display" @tap="_verif()">{{language.setup2.paymentPass.obtain}}</div>

                    <div class="active yh-color" v-if="!display">
                        <span ref="time" class="yh-time" style="">{{ count }}S{{language.setup2.paymentPass.again}}</span>
                    </div>
                </li>

                <li>
                    <input 
                        placeholder-style="color: #b8b8b8;" 
                        class="input yh-input" type="number" size="6" 
                        :placeholder="language.setup2.paymentPass.pay_pass" 
                        :password="PassWD1"
                        v-model="newpassword"/>
                    <img v-if="newpassword.length" :src="PassWD1?del:delHide" @tap="_empty(1)" />
                </li>

                <li class="set_l">
                    <input
                        placeholder-style="color: #b8b8b8;" 
                        class="input yh-input" type="number" size="6" 
                        :placeholder="language.setup2.paymentPass.confirm_pass"
                        :password="PassWD2"
                        v-model="againpassword"/>
                    <img v-if="againpassword.length" :src="PassWD2?del:delHide" @tap="_empty(2)" />
                </li>

                <li id="setupButtomWrap" class="yh-setupButtomWrap" style="background-color: initial;">
                    <div 
                        class="yh-setup-buttom" 
                        :class="codeone.length > 5 && newpassword.length > 5 && againpassword.length > 5 ? 'setup-buttom' : 'setup-buttom1'" 
                        @tap="_sub">
                        {{language.setup2.paymentPass.confirm}}
                    </div>
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
import { _timeCommon } from '@/static/js/setUp/setUp.js';
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
            is_tishi_content: '修改成功',
            sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
            //
            del: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/pwShow.png',
            delHide: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/pwHide.png',
            load: false,
            title: '修改支付密码',
            oldpassword: '',
            input_oldpassword: '',
            newpassword: '',
            againpassword: '',
            PassWD1: true,
            PassWD2: true,
            codeone: '', // 验证码
            user_phone: '',
            count: '',
            display: true,
            rez: '', // 新密码是否满足正则匹配
            timer: null,
            fastTap: true,
            bgColor:[{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ]
        };
    },
    mounted() {
        this.user_phone = uni.getStorageSync('user_phone') ? uni.getStorageSync('user_phone') : this.$store.state.user_phone;
        this.load = true;
        if(!this.user_phone) {
            uni.showToast({
                title: this.language.setup2.paymentPass.Tips[0],
                duration: 1000,
                icon: 'none'
            });
            setTimeout(()=>{ 
                uni.redirectTo({
                    url: 'changePhone'
                });
            },1000)
            return;
        }
    },
    watch: {
        /**
         *
         * */
        newpassword(newValue, oldValue) {
            this.$nextTick(() => {
                if (newValue.toString().length > 6) {
                    this.newpassword = oldValue;
                }
            });
        },
        /**
         *
         * */
        againpassword(newValue, oldValue) {
            this.$nextTick(() => {
                if (newValue.toString().length > 6) {
                    this.againpassword = oldValue;
                }
            });
        }
    },
    methods: {
        /**
         *
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
                message_type1: 5 // 短信类别 5.修改支付密码
            };

            this.$req
                .post({ data })
                .then(res => {
                    this.fastTap = true;
                    let { code, message } = res;
                    if (code != 200) {
                        uni.showToast({
                            title: message,
                            duration: 1000,
                            icon: 'none'
                        });
                    } else {
                       _timeCommon(this);
                        const TIME_F = 300;
                        let count_f = TIME_F;
                        let timer_f = setInterval(() => {
                            if (count_f > 0 && count_f <= TIME_F) {
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
         *
         * */
        _empty(type) {
            if (type == 1) {
                this.PassWD1 = !this.PassWD1;
            } else if (type == 2) {
                this.PassWD2 = !this.PassWD2;
            }
        },
        /**
         *
         * */
        _newpassword() {
            var re = /^\d{6}$/;
            if (this.newpassword != '') {
                this.rez = re.test(this.newpassword);
                if (this.rez == true) {
                    return;
                } else {
                    this.newpassword = '';
                    uni.showToast({
                        title: this.language.setup2.paymentPass.Tips[1],
                        duration: 1000,
                        icon: 'none'
                    });
                }
            }
        },
        /**
         *
         * */
        _againpassword() {
            if (this.againpassword != '') {
                if (this.newpassword == this.againpassword) {
                    return;
                } else {
                    uni.showToast({
                        title: this.language.setup2.paymentPass.Tips[2],
                        duration: 1000,
                        icon: 'none'
                    });
                }
            }
        },
        /**
         *
         * */
        _sub() {
            if(this.codeone.length < 6 || this.newpassword.length < 6 || this.againpassword.length < 6){return}
            var re = /^\d{6}$/;
            if (this.newpassword != '') {
                this.rez = re.test(this.newpassword);
            }

            if (!this.fastTap) {
                return;
            }
            this.fastTap = false;
            if (this.newpassword == '' || this.againpassword == '' || this.codeone.length != 6) {
                uni.showToast({
                    title: this.language.setup2.paymentPass.Tips[3],
                    duration: 1000,
                    icon: 'none',
                    success: res => {
                        this.fastTap = true;
                    }
                });
            } else if (!this.rez) {
                uni.showToast({
                    title: this.language.setup2.paymentPass.Tips[1],
                    duration: 1000,
                    icon: 'none',
                    success: res => {
                        this.fastTap = true;
                    }
                });
            } else if (this.againpassword != '' && this.newpassword != this.againpassword) {
                uni.showToast({
                    title: this.language.setup2.paymentPass.Tips[2],
                    duration: 1000,
                    icon: 'none',
                    success: res => {
                        this.fastTap = true;
                    }
                });
            } else if (this.codeone.length != 6) {
                uni.showToast({
                    title: this.language.setup2.paymentPass.Tips[4],
                    duration: 1000,
                    icon: 'none',
                    success: res => {
                        this.fastTap = true;
                    }
                });
            } else if (this.newpassword != '' && this.newpassword == this.againpassword && this.codeone.length == 6) {
                //发送请求
                let data = {
                    api: 'app.user.modify_payment_password',
                    x_password: this.newpassword,
                    phoneNum: this.user_phone,
                    keyCode: this.codeone,
                    cpc: this.diqu.code2, // 国家/地区
                    country_num:this.diqu.num3,  // 国家代码
                };

                this.$req
                    .post({ data })
                    .then(res => {
                        this.fastTap = true;
                        let { code, message } = res;
                        if (code == 200) {
                            //新 修改成功 弹窗
                            this.is_tishi = true
                            setTimeout(() => {
                                this.is_tishi = false
                            }, 1500)   
                            setTimeout(() => {
                                uni.navigateBack({
                                    url: '/pagesB/setUp/setUp'
                                });
                            }, 1500);
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
                    title: this.language.setup2.paymentPass.Tips[3],
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
@import url('../../static/css/setUp/paymentPassword.less');
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
