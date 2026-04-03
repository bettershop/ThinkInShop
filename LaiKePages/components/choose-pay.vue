<template>
    <view class="choose-pay">
        <!--支付方式-->
        <ul class="safe-area-inset-bottom list">
            <div>
                <li class='order_coupon' v-if="falg == false">
                    <span class="yh-order_coupon-spanc">{{language.choosePay.choosePay}}</span>
                </li>
                <view>
                    <!-- 钱包支付 -->
                    <li @tap='chooseWay("balance")' class='pay' v-if="open_pay.open_wallet ">
                        <div class="yh-pay">
                            <div class="pay_yue yh-pay-wx-d">
                                <div class="yh-pay-s">
                                    <img :src="pay_y" alt="" class='pay_img'>
                                    <div class="yh-pay_div">
                                        <p class="yh-pay_div-p">
                                            {{language.choosePay.yuePay}} 
                                            <text style="color: #999999; width: 100%;">（{{language.choosePay.yue}}{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(user_money.toFixed(2)) }}）</text>
                                        </p>
                                    </div>
                                </div>
                                <img :src="pay_way=='useWallet'?quan_ho:quan_hui" class='quan_img' />
                            </div>
                        </div>
                    </li>
                
                <!-- 如果 订单金额为零则不显示 第三方支付选项 -->
                    <!-- 微信支付 -->
                    <!-- #ifdef APP-PLUS || MP-WEIXIN || H5 -->
                    <li @tap='chooseWay("wxPay")' class='pay' v-if="open_pay.open_wxpay&&pureIntegral  ">
                        <div class="yh-pay">
                            <div class="pay_yue yh-pay-wx-d">
                                <div class="yh-pay-s">
                                    <img :src="pay_w" alt="" class='pay_img'>
                                    <div class="yh-pay_div">
                                        <p class="yh-pay_div-p">
                                            {{language.choosePay.weChatPay}}
                                        </p>
                                    </div>
                                </div>
                                <img :src="pay_way=='wxPayStatue'?quan_ho:quan_hui" class='quan_img' />
                            </div>
                        </div>
                    </li>
                    <!-- #endif -->

                    <!-- 支付宝支付 -->
                    <!-- #ifdef APP-PLUS || MP-ALIPAY || MP-TOUTIAO || H5 -->
                    <li @tap='chooseWay("aliPay")' class='pay' v-if="open_pay.open_alipay&&pureIntegral  ">
                        <div class="yh-pay">
                            <div class="pay_yue yh-pay-wx-d">
                                <div class="yh-pay-s">
                                    <img :src="pay_z" alt="" class='pay_img'>
                                    <div class="yh-pay_div">
                                        <p class="yh-pay_div-p">
                                            {{language.choosePay.alipay}} 
                                        </p>
                                    </div>
                                </div>
                                <img :src="pay_way=='aliPayStatue'?quan_ho:quan_hui" class='quan_img' />
                            </div>
                        </div>
                    </li>
                    <!-- #endif -->
              
                    <!-- #ifdef H5 -->
                    <!-- PayPal支付 --> 
                    <li @tap='chooseWay("paypal")' class='pay' v-if="open_pay.open_paypal&&pureIntegral" >
                        <div class="yh-pay">
                            <div class="pay_yue yh-pay-wx-d">
                                <div class="yh-pay-s">
                                    <img :src="paypallogo" alt="" class='pay_img'>
                                    <div class="yh-pay_div">
                                        <p class="yh-pay_div-p">
                                            {{language.choosePay.paypal}}
                                        </p>
                                    </div>
                                </div>
                                <img :src="pay_way=='paypal'?quan_ho:quan_hui" class='quan_img' />
                            </div>
                        </div>
                    </li>
                    <!-- #endif -->
                    <!-- #ifdef H5 -->
                    <!-- Stripe支付 --> 
                    <li @tap='chooseWay("stripe")' class='pay' v-if="open_pay.open_stripe&&pureIntegral" >
                        <div class="yh-pay">
                            <div class="pay_yue yh-pay-wx-d">
                                <div class="yh-pay-s">
                                    <img :src="stripe_logo" alt="" class='pay_img'>
                                    <div class="yh-pay_div">
                                        <p class="yh-pay_div-p">
                                            {{language.choosePay.stripe}}
                                        </p>
                                    </div>
                                </div>
                                <img :src="pay_way=='stripe'?quan_ho:quan_hui" class='quan_img' />
                            </div>
                        </div>
                    </li>
                
                <!-- #endif -->
                    <!--线下支付 --> 
                    <li @tap='chooseWay("offline_payment")' class='pay' v-if="open_pay.offline_payment&&pureIntegral">
                        <div class="yh-pay">
                            <div class="pay_yue yh-pay-wx-d">
                                <div class="yh-pay-s">
                                    <img :src="stripe_logo" alt="" class='pay_img'>
                                    <div class="yh-pay_div">
                                        <p class="yh-pay_div-p">
                                           线下支付
                                        </p>
                                    </div>
                                </div>
                                <img :src="pay_way=='offline_payment'?quan_ho:quan_hui" class='quan_img' />
                            </div>
                        </div>
                    </li>
                </view>
            </div>
        </ul> 
    </view>
</template>

<script>
    export default {
        data() {
            return {
                pay_y: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/yuezhifu2x.png',
                quan_hui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/xuanzehui2x.png', 
                quan_ho: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/new_red_select.png',
                pay_bd: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/baiduzhifu2x.png',
                pay_z: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/zhifubaozhifu2x.png',
                pay_w: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/weixinzhifu2x.png',
                paypallogo: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/beibao.png',
                stripe_logo: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/stripe_logo.png',
                falg:false,
                showPay: false, 
            }
        },
        props: {
            open_pay:{
                default: {},
                type:Object
            },
            paypals: {
                default: false,
                type: Boolean
            },   
            user_money: {
                default: 0,
                type: Number
            },
            
            pay_way:{
                default: '',
                type:String
            },
            pureIntegral: {
                default: true,
                type: Boolean
            },   
            // 金额信息 可包含 金额、汇率 等信息
            priceInfo:{
                type:Object,
                default:()=>{}
            }
        },
        computed: {
            // 安全获取 total 并转为数字
            totalAmount() {
                const total = this.priceInfo && typeof this.priceInfo === 'object'
                    ? this.priceInfo.total
                    : 0;
                       console.log(total ,'totaltotaltotal')
                return Number(total) || 0;
            },
       
        },
        watch:{
            priceInfo: {
                handler(newVal) {
                    // 同样避免直接用 ?.，保持逻辑一致
                    
                     
                    const total = newVal && typeof newVal === 'object' ? newVal.total : 0;
                    if (Number(total) === 0) {
                        this.showPay = true;
                        this.falg = true; // 修正拼写
                        this.chooseWay("balance");
                    }
                },
                deep: true,
                immediate: true
            }
        },
        created(){
            this.setLang();
        },
        mounted() {  
        },
        methods: {
            chooseWay(type) {
                console.log("选择支付方式："+type);
                this.$emit('chooseWay', type)
            }
        }
    }
</script>

<style>
    page{
        background: #F4F5F6;
    }
</style>
<style lang="less" scoped>

    @import url("@/laike.less");
    .choose-pay {
        background-color: #ffffff;
        /* #ifdef MP-WEIXIN */
        margin-bottom: 100rpx;
        /* #endif */
        border-radius: 24rpx;
        .yh-line {
            width: 100%;
            height: 10rpx;
            background-color: #F4F4F4;
        }

        .list {
            /* #ifdef MP-WEIXIN */
            padding-bottom: 0;
            /* #endif */
            .order_coupon {
                display: flex;
                flex-flow: row nowrap;
                justify-content: space-between;
                align-items: center;                
                padding: 32rpx 0;
                box-sizing: border-box;
                margin: 0 32rpx;
                border-bottom: 1rpx solid #eee;
                
                span {
                    font-size: 28rpx;
                }
            
                .yh-order_coupon-spanc {
                    font-size: 32rpx;
                    color: #333333;
                    
                }
            }
            
            .pay {
                padding: 32rpx 0;
                border-bottom: 1rpx solid #eee;
                font-size: 24rpx;
                color: #333333;
                margin: 0 32rpx 0 34rpx;
                &:nth-last-child(1){
                    border: 0;
                }
                .yh-pay {
                    width: 100%;
            
                    .pay_yue {
                        display: flex;
                        align-items: center;
            
                        &.yh-pay-wx-d {
                            position: relative;
                            width: 100%;
                            justify-content: space-between;
            
            
                            .yh-pay-s {
                                width: 100%;
                                display: flex;
                                align-items: center;
            
                                .pay_img {
                                    width: 44rpx;
                                    height: 44rpx;
                                }
            
                                .yh-pay_div {
                                    margin-left: 30rpx;
                                    font-size: 28rpx;
            
                                    .yh-pay_div-p {
                                        width: 100%;
                                    }
            
                                }
            
                            }
            
                            /*    选项圆圈     */
                            .quan_img {
                                /* #ifdef MP-WEIXIN */
                                width: 36rpx;
                                /* #endif */
                                /* #ifndef MP-WEIXIN */
                                width: 34rpx;
                                /* #endif */
                                
                                margin-right: 4rpx;
                                height: 34rpx;
                            }
                        }
                    }
                }
            
            
            }
            
        }
    }
</style>>
