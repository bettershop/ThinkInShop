<template>
    <div>
        <heads :title="title" :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
        <toload :load="load">
            <ul class="setup-ul yh-setup-ul">
                <!-- 手机号 -->
                <li class="qu-box">
                    <view class="qu" @tap.shop="navTo('/pagesD/login/countryAndRegion')">
                        <view>{{diqu.code}} +{{diqu.code2}} </view>
                        <view class="san-jiao"></view>
                    </view>
                    <input 
                        type="number"
                        placeholder-style="color: #b8b8b8;"
                        :placeholder="language.setup2.changePhone.phone_placeholder" 
                        v-model="newphone" 
                         @input="_changeStep"
                       />
                </li>
                <!-- 验证码 -->
                <li class="last">
                    <input 
                        type="number"
                        class="input"
                        placeholder-style="color: #b8b8b8;" 
                        :placeholder="language.setup2.changePhone.code_placeHolder" 
                        v-model="codeone" 
                        @input="_changeStep"/>
                    <div class="color yh-color" v-if="display" @tap="_verif">
                        {{language.setup2.changePhone.obtain}}
                    </div>
                    <div v-else class="active yh-color">
                        <span ref="time" class="yh-time">{{ count }}s</span>
                        {{language.setup2.changePhone.again}}
                    </div>
                </li>
                <!-- 确认按钮 -->
                <li id="setupButtomWrap" class="yh-setupButtomWrap">
                    <div class="setup-buttom yh-isSure" :class="{ sure: isSure }" @tap="_sub">
                        {{language.setup2.changePhone.confirm}}
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
import { telephone } from '../../common/landing.js';
import { _timeCommon } from '@/static/js/setUp/setUp.js'

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
            title: '',
            display: true,
            count: '',
            timer: null,
            code: '',
            newphone: '',
            passtwo: '',
            codeone: '', // 验证码
            oldphone: '', // 旧手机号
            phone_next: '', // 新手机号码输入正确返回值
            fastTap: true,
            isSure: false,
            load: false,
            verifyPhone: false,
            bgColor:[{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ]
        };
    },
    onShow(){
        if(uni.getStorageSync('diqu')){
        	this.diqu = JSON.parse(uni.getStorageSync('diqu'))
        } 
    },
    onUnload(){
    	uni.removeStorageSync('diqu')
    },
    onLoad(option) {
        this._getUserInfor()
		this.setLang();
        if(option.verifyPhone){this.verifyPhone = true}
        this.load = true;
    },
    methods: {
        /**
         *
         * */
        async _getUserInfor(){
            let data = {
              api: 'app.user.index',
            }
            await this.$req.post({data}).then(res => {
                let userPhone = ''
                if(res.code == 200){
                    userPhone = res.data.data.user.mobile
                    if(userPhone || uni.getStorageSync('user_phone')){
                        this.title = this.language.setUp.editPhone
                    } else {
                        this.title = this.language.setUp.changePhone
                    }
                    //#ifdef MP-WEIXIN
                    uni.setNavigationBarTitle({
                        title: this.title
                    });
                    // #endif
                }
                this.oldphone = userPhone ? userPhone : uni.getStorageSync('user_phone');
                //this.newphone = this.oldphone
            })
        },
        /**
         *
         * */
        _changeStep(e) {
            if (this.newphone.length>0 && this.codeone.length == 6) {
                this.isSure = true;
            }  else {
                this.isSure = false;
            }
        },
        /**
         * 手机号码验证
         * */
        _telephone() {
            if (this.oldphone != this.newphone) {
                this.phone_next = telephone(this.newphone);
            } else {
                uni.showToast({
                    title: this.language.setup2.changePhone.Tips[0],
                    duration: 1000,
                    icon: 'none'
                });
            }
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
                phone: this.newphone,
                message_type: 0, // 短信类型 0.验证码 1.短信通知
                message_type1: 3 ,// 短信类别 3.修改手机号
                cpc: this.diqu.code2, // 国家/地区
                country_num:this.diqu.num3,  // 国家代码
                
            };

            if (this.newphone) {
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
            } else {
                this.fastTap = true;
                uni.showToast({
                    title: this.language.setup2.changePhone.Tips[1],
                    duration: 1000,
                    icon: 'none'
                });
            }
        },
        /**
         *
         * */
        _sub() {
            if(!this.isSure){return}
            if (!this.fastTap) {
                return;
            }
            this.fastTap = false;
            if (this.codeone == '' || this.newphone == '') {
                uni.showToast({
                    title: this.language.setup2.changePhone.Tips[2],
                    icon: 'none',
                    success: res => {
                        this.fastTap = true;
                    }
                });
            } else if (this.codeone.length != 6) {
                uni.showToast({
                    title: this.language.setup2.changePhone.Tips[3],
                    icon: 'none',
                    success: res => {
                        this.fastTap = true;
                    }
                });
            } else if (this.oldphone != this.newphone) {
                this.fastTap = true; 
            } else if (this.oldphone == this.newphone) {
                uni.showToast({
                    title: this.language.setup2.changePhone.Tips[4],
                    icon: 'none',
                    success: res => {
                        this.fastTap = true;
                    }
                });
            }

            if (this.codeone.length == 6 && this.newphone != '' && this.oldphone != this.newphone  ) {
                //发送请求
                
                if (this.verifyPhone) {
                    var data = {
                        api: 'app.user.synchronizeAccount',
                        mobile: this.newphone,
                        keyCode: this.codeone,
                        cpc: this.diqu.code2, // 国家/地区
                        country_num:this.diqu.num3,  // 国家代码
                    };
                    
                    this.$req.post({data}).then(res => {
                        uni.showToast({
                            title: res.message,
                            icon: 'none'
                        })
                        
                        if (res.code == 200) {
                            setTimeout(() => {
                                this.is_tishi = false
                                uni.redirectTo({
                                    url:'/pages/shell/shell?pageType=my'
                                })
                            }, 1000)   
                        }
                    })
                    
                } else {
                    var data = {
                        api: 'app.user.update_phone',
                        y_phone: this.oldphone,
                        x_phone: this.newphone,
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
                                }, 1000)   

                                uni.setStorageSync('user_phone', this.newphone);
                                this.$store.state.user_phone = this.newphone;
                                setTimeout(() => {
                                    // if(this.oldphone == ''){
                                    //     uni.redirectTo({
                                    //         url: '/pagesB/setUp/payment'
                                    //     })
                                    // }else{
                                        uni.navigateBack({
                                            delta: 1
                                        })
                                    // }
                                }, 1000);
                            } else {
                                uni.showToast({
                                    title: message,
                                    icon: 'none'
                                });
                            }
                        })
                        .catch(error => {
                            this.fastTap = true;
                        });
                }
                
                
                
            } else {
                this.fastTap = true;
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
@import url('../../static/css/setUp/changePhone.less');
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
