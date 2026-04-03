<template>
    <div>
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="language.setup2.payment.title" :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
        <ul class="setup-ul yh-setup-ul">
            <template >
                <li class="qu-box" v-if ='havaPayPwd'>
                    <view class="qu"  >
                        <view>{{diqu.code}} +{{diqu.code2}} </view>
                        <view class="san-jiao"></view>
                    </view>
                    <input type="number" v-model="user_phone" disabled="true" />
                </li>

                <li class="last" v-if ='havaPayPwd'>
                    <input placeholder-style="color: #b8b8b8;font-size:30rpx" class="input" :class="{ marginleft: display }"
                        type="number" :placeholder="language.setup2.payment.code_placeHolder" v-model="codeone"
                        style="flex: 1;" />

                    <div class="color yh-color" v-if="display" @tap="_verif()">{{language.setup2.payment.obtain}}</div>

                    <div class="active yh-color" v-if="!display">
                        <span ref="time" class="yh-time">{{ count }}S{{language.setup2.payment.again}}</span>
                    </div>
                </li>
            </template>
            
            <li>
                <input 
                    placeholder-style="color: #b8b8b8;" 
                    class="input yh-input" type="number" size="6" 
                    :placeholder="language.setup2.payment.pay_pass" 
                    :password="PassWD1"
                    v-model="onepay"/>
                <img :src="PassWD1?del:delHide" v-if="onepay.length" @tap="_blur(1)" />
            </li>
            
            <li class="set_l">
                <input
                    placeholder-style="color: #b8b8b8;" 
                    class="input yh-input" type="number" size="6" 
                    :placeholder="language.setup2.payment.confirm_pass"
                    :password="PassWD2"
                    v-model="twopay"/>
                <img :src="PassWD2?del:delHide" v-if="twopay.length" @tap="_blur(2)" />
            </li>
            
            <li class="yh-setupButtomWrap" style="padding: 0;">
                <div class="setup-buttom yh-setup-buttom"
                    v-if ='havaPayPwd'
                    :class="( codeone.length == 6 && onepay.length == 6 && twopay.length == 6) ? '':'action'"
                    @tap="_sub">{{language.setup2.payment.confirm}}</div>
                <div class="setup-buttom yh-setup-buttom"
                        v-if ='!havaPayPwd'
                        :class="(onepay.length == 6 && twopay.length == 6) ? '':'action'"
                        @tap="_sub">{{language.setup2.payment.confirm}}</div>
            </li>
        </ul>
    </div>
</template>

<script>
    import {
        telephone
    } from '../../common/landing.js';
    import {
        _timeCommon
    } from '@/static/js/setUp/setUp.js';


    export default {
        data() {
            return {
                diqu:{
                	code:'CN',
                	code2:'86',
                    num3: 156
                },
                title: '设置支付密码',
                display: true,
                count: '',
                timer: null,
                code: '',
                onepay: '', // 第一次支付密码
                twopay: '', // 第二次支付密码
                codeone: '', // 验证码
                user_phone: '', // 旧手机号

                bgColor: [{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                guan: false,
                guan_word: '',
                fastTap: true,
                PassWD1: true,
                PassWD2: true,
                del: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/pwShow.png',
                delHide: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/pwHide.png',
                type:false,
                havaPayPwd:true
            };
        },
        onLoad(option) {
            this.type = option.type
            this.havaPayPwd  = uni.getStorageSync('havaPayPwd')
        },
        onShow() {
            if(uni.getStorageSync('diqu')){
            	this.diqu = JSON.parse(uni.getStorageSync('diqu'))
            } 
            this.isLogin(() => {
                this.user_phone = uni.getStorageSync('user_phone') ? uni.getStorageSync('user_phone') : this
                    .$store.state.user_phone || ''; 
                if(havaPayPwd){  
                    if ( !this.user_phone ) {
                        uni.showToast({
                            title: this.language.setup2.payment.Tips[0],
                            duration: 1000,
                            icon: 'none'
                        });
                       setTimeout(()=>{
                           if(this.type){
                               // 个人中心设置使用
                               uni.redirectTo({
                                   url: 'changePhone'
                               });
                           }else{
                               // 订单详情页使用
                                uni.navigateTo({
                                    url: 'changePhone'
                                });
                           }
                           
                       },1000)
                        return;
                    }
                } 
            })
        },
     
        methods: {
            /**
             *
             * */
            changeLoginStatus() {

            },
            /**
             * 验证码发送1分钟倒计时
             * */
            _verif() {
                var data = {
                    api: 'app.user.secret_key',
                    phone: this.user_phone,
                    message_type: 0, // 短信类型 0.验证码 1.短信通知
                    message_type1: 5 // 短信类别 5.修改支付密码
                };
                
                if (!this.fastTap) {
                    return;
                }
                this.fastTap = false;

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
                        } else {
                            _timeCommon(this);
                            const TIME_F = 60;
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
            _blur(type) {
                if (type == 1) {
                    this.PassWD1 = !this.PassWD1;
                } else if (type == 2) {
                    this.PassWD2 = !this.PassWD2;
                }
                var val = this.onepay;
                if (type == 2) {
                    val = this.twopay;
                }
                var numReg = /^[0-9]+$/;
                var numRe = new RegExp(numReg);
                if (val.length != 6) {
                    uni.showToast({
                        title: this.language.setup2.payment.pay_pass,
                        duration: 1500,
                        icon: 'none'
                    });
                    return false;
                }
                if (!numRe.test(val)) {
                    if (type == 1) {
                        this.onepay = '';
                    } else if (type == 2) {
                        this.twopay = '';
                    }
                    uni.showToast({
                        title: this.language.setup2.payment.Tips[2],
                        duration: 1500,
                        icon: 'none'
                    });
                    return false;
                }
            },
            /**
             *
             * */
            _sub() {
                if(this.havaPayPwd){
                    if(this.codeone.length != 6 || this.onepay.length != 6 || this.twopay.length != 6){return}
                }else{
                    if(  this.onepay.length != 6 || this.twopay.length != 6){return}
                }
                var numReg = /^[0-9]+$/;
                var numRe = new RegExp(numReg);
                if (!numRe.test(this.onepay) || !numRe.test(this.twopay)) {
                    uni.showToast({
                        title: this.language.setup2.payment.Tips[2],
                        duration: 1500,
                        icon: 'none'
                    });
                    return false;
                }
                if (!this.fastTap) {
                    return;
                }
                this.fastTap = false;

                if (this.onepay != '' && this.onepay != this.twopay) {
                    uni.showToast({
                        title: this.language.setup2.payment.Tips[3],
                        duration: 1000,
                        icon: 'none',
                        success: res => {
                            this.fastTap = true;
                        }
                    });
                } else if (this.onepay.length != 6 && this.onepay.length != 6) {
                    uni.showToast({
                        title: this.language.setup2.payment.Tips[4],
                        duration: 1000,
                        icon: 'none',
                        success: res => {
                            this.fastTap = true;
                        }
                    });
                } else if (this.onepay != '' && this.onepay === this.twopay) {
                    var data = {
                        api: 'app.user.set_payment_password',
                        phone: this.user_phone,
                        password: this.twopay,
                        keyCode: this.codeone,
                        cpc: this.diqu.code2, // 国家/地区
                        country_num:this.diqu.num3,  // 国家代码
                    };
                    if(!this.havaPayPwd){
                        delete data.keyCode
                    }
                    this.$req
                        .post({
                            data
                        })
                        .then(res => {
                            let {
                                code,
                                message
                            } = res;

                            if (code == 200) {
                                uni.showToast({
                                    title: this.language.setup2.payment.Tips[5],
                                    duration: 1500,
                                    icon: 'none'
                                });
                                
                                uni.setStorageSync('password_status',true)
                                setTimeout(() => {
                                    this.fastTap = true;
                                    uni.navigateBack({
                                        delta:1
                                    });
                                }, 1500);
                            } else {
                                uni.showToast({
                                    title: message,
                                    duration: 1000,
                                    icon: 'none',
                                    success: res => {
                                        this.fastTap = true;
                                    }
                                });
                            }
                        })
                        .catch(error => {
                            this.fastTap = true;
                        });
                } else {
                    this.fastTap = true;
                }
            }
        }
    };
</script>


<style>
    page {
        background-color: #F4F5F6;
    }
</style>
<style scoped lang="less">
    @import url("@/laike.less");
    @import url('../../static/css/setUp/payment.less');
</style>
