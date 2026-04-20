<template>
    <div class="box">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :bgColor='bgColor' :ishead_w="ishead_w" titleColor="#333333" :title="language.recharge.title"></heads>
        <div class="recharge_ts">
            <view>{{language.recharge.money}}</view>
        </div>
        <div class="recharge_money">
            <view>
                <span class="span">{{currency_symbol}}</span>
                <input 
                    placeholder-style="color: #999999;font-size:24rpx;font-family: DIN-Bold, DIN"
                    style="height: 50rpx;"
                     type="digit" min="0.01" maxlength="9"
                    :value="price" :placeholder="rechargeTitle" @blur="_money" @input="_checkValue" />
            </view>
            <view class="hx"></view>
            <view class='order_coupona'
                style="height: 108rpx;padding: 0;justify-content: space-between;" @click="maskclick()">
                <!-- 订单备注 -->
                <span>{{language.pay.orderDetailsr.Order_notes}}</span>
                <span class="yh-order_coupon-spana"
                    style="display: flex;margin-right: 0;align-items: center;margin-left: 32rpx;">
                    <input style="width: 100%;z-index:9;"
                        placeholder-style="color: #999999;font-size:24rpx;font-family: DIN-Bold, DIN"
                        :disabled="true"
                        :placeholder="language.pay.orderDetailsr.order_placeholder_wbz" 
                        type="text" v-model="remarks" maxlength="25">
                    <img :src="jiantou" class='arrow' style='margin-left: 8rpx;' />
                </span>
            </view>
        </div>
        <ul class="choose_ul">
            <li class="pay">{{language.recharge.paymentStyle}}</li>
            <!-- #ifdef APP-PLUS || H5-->
            <li class="pay" @tap="_pay(1)" v-if="open_pay.open_wxpay" style="border-bottom: 0;">
                <div class="pay_div">
                    <img class="pay_img" :src="pay_weix" />
                    <p>{{language.recharge.weChat}}</p>
                </div>
                <img class="quan_img" :src="pay_index == 1 ? quan_hei : quan_hui" />
            </li>
            <view style="width: 686rpx;background-color: #eee;height: 1rpx;margin-left: 32rpx;"></view>
            <li class="pay" @tap="_pay(0)" v-if="open_pay.open_alipay && !LaiKeTuiCommon.IS_SHARE_WECHAT_H5PAY" style="border-bottom: 0;">
                <div class="pay_div">
                    <img class="pay_img" :src="pay_zhifub" />
                    <p>{{language.recharge.alipay}}</p>
                </div>
                <img class="quan_img" :src="pay_index == 0 ? quan_hei : quan_hui" />
            </li>
            <view style="width: 686rpx;background-color: #eee;height: 1rpx;margin-left: 32rpx;"></view>
            <li class="pay" @tap="_pay(4)" v-if="open_pay.paypal" style="border-bottom: 0;">
                <div class="pay_div">
                    <img class="pay_img" :src="paypallogo" />
                    <p>{{language.recharge.paypal}}</p>
                </div>
                <img class="quan_img" :src="pay_index ==  4 ? quan_hei : quan_hui" />
            </li>
            <li class="pay" @tap="_pay(5)" v-if="open_pay.stripe" style="border-bottom: 0;">
                <div class="pay_div">
                    <img class="pay_img" :src="stripe_logo" />
                    <p>{{language.recharge.stripe}}</p>
                </div>
                <img class="quan_img" :src="pay_index ==  5 ? quan_hei : quan_hui" />
            </li>

            
            <!-- #endif -->
            <!-- 之前这块是#ifdef MP-WEIXIN||H5切是注释的情况 -->
            <!-- #ifdef MP-WEIXIN-->
            <li class="pay" @tap="_pay(3)" v-if="open_pay.open_wxpay == true">
                <div class="pay_div">
                    <img class="pay_img" :src="pay[1].img" />
                    <p>{{language.recharge.weChat}}</p>
                </div>
                <img class="quan_img" :src="quan_hei" />
            </li>
            <!-- #endif -->
            <!-- 结束 -->
            <!-- #ifdef MP-ALIPAY || MP-TOUTIAO -->
            <li class="pay" @tap="_pay(0)" v-if="open_pay.open_alipay">
                <div class="pay_div">
                    <img class="pay_img" :src="pay[0].img" />
                    <p>{{language.recharge.alipay}}</p>
                </div>
                <img class="quan_img" :src="quan_hei" />
            </li>
            <!-- #endif -->

            <!-- #ifdef MP-BAIDU -->
            <li class="pay" @tap="_pay(2)" v-if="payment.baidu_pay == 1">
                <div class="pay_div">
                    <img class="pay_img" :src="pay_bd" />
                    <p>{{language.recharge.baiduPay}}</p>
                </div>
                <img class="quan_img" :src="quan_hei" />
            </li>
            <!-- #endif -->
        </ul>
        <div v-if='price > 0' class="setup-buttom" @tap="pay1()">{{language.recharge.btn}}</div>
        <div v-else class="setup-buttom1">{{language.recharge.btn}}</div>
        
        <!-- 备注弹窗 -->
        <view @touchmove.stop.prevent @click="maskclicka" class="bounced" v-if="mask">
            <view class="bounced-box" style="z-index: 99999;" @click.stop>
                <view class="bounced-box-box">
                    <view class="bounced-box-box-title">{{language.pay.orderDetailsr.ddbz}}</view>
                    <image  class="bounced-box-box-img"></image>
                </view>
                <view class="bounced-box-list">
                    <view class="bounced-box-list_title">
                        <textarea class="bounced-box-list_title_inptut" v-model="newremak"
                            :placeholder="language.pay.orderDetailsr.order_placeholder"
                            adjust-position="true" @blur="_ipt_blur" @input="inechange($event)" maxlength="200">
                           </textarea>
                        <view class="bounced-box-list_title_num">
                            <span style="color: #000000;">{{inechangenum}}</span>
                            /200
                        </view>
                    </view>
                </view>
                <view class="bounced-box-list_btn">
                    <view class="bounced-box-list_btn_box" @click="maskclick(true)">
                        {{language.pay.orderDetailsr.qd}}</view>
                </view>
            </view>
        </view>
    </div>
</template>

<script>
    // #ifdef H5
    // 在 H5 中动态加载 Stripe.js，防止小程序/APP报错
      const script = document.createElement('script')
      script.src = 'https://js.stripe.com/v3/'
      document.head.appendChild(script)
    // #endif
    import heads from '../../components/header.vue';
    import mixinsOrder from '../../mixins/order'
    import {getDateAll} from'../../common/util.js'
    import laikepay from "@/components/laikepay.vue";
    export default {
        data() {
            return {
                open_pay:{
                  open_alipay: false,
                  open_wxpay: false,
                  paypal: false,
                  stripe: false,
                },
                //注：当前页面按照商城默认币种充值和后台保持一致：前端用户充值时候显示的充值方式，跟随后台商城设置的默认币种来改变 
                currency_code: this.LaiKeTuiCommon.DEFAULT_STORE_EXCHANGERATE,
                currency_symbol: this.LaiKeTuiCommon.DEFAULT_STORE_SYMBOL,
                exchange_rate: this.LaiKeTuiCommon.DEFAULT_STORE_EXCHANGERATE,
                showPaypal:false,//注：由于贝宝不支持使用人民币收款，所以在商城使用人民币收款的时候不展示贝宝支付方式
                title: '充值',
                pay_y: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/yuezhifu2x.png',
                pay_b: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/lALPBb.png',
                quan_hui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xuanzehui2x.png',
                quan_hei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/new_red_select.png',
                pay_bd: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/baiduzhifu2x.png',
                quan_ho: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/new_red_select.png',
                pay_zhifub: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/llzfb.png',
                paypallogo: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/beibao.png',
                stripe_logo: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/stripe_logo.png',
                pay_weix: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/llwx.png',
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jiantou2x.png',
                pay: [{
                        img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/zhifubaozhifu2x.png',
                        name: '支付宝'
                    },
                    {
                        img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/weixinzhifu2x.png',
                        name: '微信支付'
                    }
                ], //支付方式
                pay_index: 0,
                pay_style: '', //支付方式
                price: '',
                enterPay: false,
                bgColor: [{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                ishead_w: '2',
                fastTap: true,
                pay_provider: '',
                order_list: '',
                code: '',
                firstFlag: true,
                value_inp: '',
                min_amount: '', //最小充值金额
                rechargeTitle: '',
                back1: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/back2x.png',
                payment: {},
                defaultpayment: {},
                mask:false,
                newremak:'', //留言备注
                remarks:'',
                inechangenum: 0,
                
            };
        },
        mounted() {
            this.isLogin(() => {
                this._pay(1);
                // #ifdef H5
                this._pay(4);
                // #endif
                // #ifdef MP-BAIDU
                this._pay(2);
                // #endif
                this._axios();
            })
        },
        onShow() {
            this.getUserCurrencyInfo();
            this._axios();
            // #ifdef H5
            if(this.LaiKeTuiCommon.IS_SHARE_WECHAT_H5PAY){return}
            
            let iswx = this.is_wx();
            if (!iswx) {
                return;
            }
            var a = window.location.href;

            if (a.split('?').length > 1) {
                var str = a.split('?')[1];
                var arr = str.split('&');
                for (var i in arr) {
                    if (typeof arr[i] == 'String' || typeof arr[i] == 'string') {
                        if (arr[i].substring(0, 4) == 'code') {
                            str = arr[i].substring(5);
                            this.code = str;
                        }
                    }
                }
            }

            if (this.code == '') {
                this.toUrl();
            }
            // #endif
        },
        watch: {
            price: function(newValue, oldValue) {
                this.$nextTick(() => {
                    if (newValue > 0.01) {
                        if (Math.abs(newValue - oldValue) == 0) {
                            this.enterPay = true;
                            let num = Number(newValue);
                            if (isNaN(num)) {
                                this.price = '0';
                            } else {
                                this.price = num;
                            }
                        } else if (Math.abs(newValue - oldValue) < 0.01) {
                            this.enterPay = true;
                            let num = Number(newValue).toFixed(2);
                            if (isNaN(num)) {
                                this.price = '0';
                            } else {
                                this.price = num;
                            }
                        } else {
                            this.enterPay = true;
                            let num = Number(newValue);
                            if (isNaN(num)) {
                                this.price = '0';
                            } else {
                                this.price = num;
                            }
                        }
                    } else {
                        // 不可以提交
                        this.enterPay = false;
                    }
                });
            }
        },
        methods: {
            maskclick(e) {  
                this.mask = !this.mask 
                if(this.mask){
                    this.newremak = this.remarks
                }else{
                    this.remarks = this.newremak
                }
            },
            maskclicka(){
                this.mask = false
            },
            inechange(e) {  
                this.inechangenum = e.detail.cursor 
            },
            getTimeNow(){
                const date = new Date();
                const year = date.getFullYear();
                const month = date.getMonth() + 1;
                const day = date.getDate();
                const hours = date.getHours();
                const minutes = date.getMinutes();
                const seconds = date.getSeconds();
                var time = year+'-'+month+'-'+day+' '+this.compareNumber(hours)+':'+this.compareNumber(minutes)+':'+this.compareNumber(seconds)
                
                return time
            },
            _ipt_blur(){
                uni.pageScrollTo({
                		scrollTop: 0,
                		duration: 0
                	});
            },
            compareNumber (num) {
                  return num < 10 ? '0' + num : num
            },
            is_wx() {
                let en = window.navigator.userAgent.toLowerCase()
                // 匹配en中是否含有MicroMessenger字符串
                return en.match(/MicroMessenger/i) == 'micromessenger'
            },
            /**
             * 支付方式显示判断
             * */
            isPayShow: function() {

                //注：由于贝宝不支持使用人民币收款，所以在商城使用人民币收款的时候不展示贝宝支付方式
                this.showPaypal = this.currency_code != 'CNY' ;
                
                // #ifdef H5
                let iswx = this.is_wx();
                if(this.showPaypal)
                {
                    if (this.payment.paypal == 1) {
                        this.open_pay.paypal = true;
                    }
                    if (this.payment.stripe == 1) {
                        this.open_pay.stripe = true;
                    }
                }
                if (iswx ) {
                    if (this.payment.jsapi_wechat == 1) {
                        this.open_pay.open_wxpay = true;
                    }
                } else {
                    if (this.payment.H5_wechat == 1) {
                        this.open_pay.open_wxpay = true;
                    }
                }
                // #endif

                // #ifdef MP-WEIXIN
                if (this.payment.mini_wechat == 1) {
                    this.open_pay.open_wxpay = true;
                }
                // #endif                
                // #ifdef APP-PLUS 
                if (this.payment.app_wechat == 1) {
                    this.open_pay.open_wxpay = true;
                }

                // #endif
                //h5/app支付宝支付
                // #ifdef H5 || APP-PLUS
                if (this.payment.alipay_mobile == 1) {
                    this.open_pay.open_alipay = true;
                }
                // #endif


                // #ifdef MP-ALIPAY
                if (this.payment.alipay_minipay == 1) {
                    this.open_pay.open_alipay = true;
                }
                // #endif

                // #ifdef MP-TOUTIAO
                if (this.payment.tt_alipay == 1) {
                    this.open_pay.open_alipay = true;
                }
                // #endif
            },
            changeLoginStatus() {
                this._axios();
            },
            _back() {
                uni.navigateBack({
                    delta: 1
                });
            },
            _axios() {
                var data = {
                    api: 'app.recharge.index',

                };

                this.$req.post({
                    data
                }).then(res => {
                    let {
                        data: {
                            min_cz,
                            defaultpayment,
                            payment
                        }
                    } = res;
                    if (res.code == 200) {
                        this.min_amount = min_cz;
                        this.payment = payment;
                        this.defaultpayment = defaultpayment;
                        
                        if(this.defaultpayment){
                            switch (this.defaultpayment.defaultpayName)
                            {
                                case 'aliPay': this.pay_index = 0;break;
                                // #ifdef APP-PLUS || H5
                                case 'wxPay': this.pay_index = 1;break;
                                // #endif
                                // case '': this.pay_index = 2;break;百度的暂时不考虑
                                // #ifdef MP-WEIXIN
                                case 'wxPay': this.pay_index = 3;break;
                                // #endif
                                case 'paypal': this.pay_index = 4;break;
                                case 'stripe': this.pay_index = 5;break;
                            }
                        }
                        
                        if(this.min_amount){
                            this.rechargeTitle = this.language.recharge.money_placeholder + ' ' + this.min_amount;
                        }else{
                            this.rechargeTitle = this.language.recharge.money_placeholder + ' 0.01' ;
                        }

                        this.isPayShow();
                    }
                });
            },
            _money(e) {
                this.price = Number(e.detail.value);
                var monrys = Number(this.min_amount);
                if (this.price < monrys) {
                    this.price = '';
                    uni.showToast({
                        title: `${this.language.recharge.toasts[0]} ${monrys} ${this.language.recharge.toasts[1]}`,
                        duration: 1500,
                        icon: 'none'
                    });
                }
                this.fastTap = true;
            },
            toUrl() {
                var locationUrl = window.location.href;
                var data = {
                    type: 'jsapi_wechat',
                    api: 'app.order.get_config',
                    url: locationUrl
                };

                this.$req.post({
                    data
                }).then(res => {
                    if (res.code == 200) {
                        var myappid = res.data.config.appid;
                        this.myappid = myappid;
                        var myUrl = res.data.url;


                        var url =
                            'https://open.weixin.qq.com/connect/oauth2/authorize?appid=' +
                            myappid +
                            '&redirect_uri=' +
                            myUrl +
                            '&response_type=code&scope=snsapi_base&state=STATE#wechat_redirect';
                        window.location.href = url;
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        });
                    }
                });
            },
            _checkValue(e) {
                this.price = Number(e.detail.value);
                var monrys = Number(this.min_amount);
                if (this.price < monrys) {
                    this.fastTap = false;
                } else {
                    this.fastTap = true;
                }
            },
            pay1() {
                //是否可虚拟支付
                let flag = this.LaiKeTuiCommon.ttIOSCantVisualpay();
                if (!flag) {
                    return;
                }

                if (!this.fastTap) {
                    return;
                }
                this.fastTap = false;
                this.firstFlag = false;
                //是否输入了金额
                if (this.price == '') {
                    uni.showToast({
                        title: this.language.recharge.toasts[2],
                        duration: 1000,
                        icon: 'none'
                    });
                    this.fastTap = true;
                    this.firstFlag = true;
                    return;
                }

                uni.showLoading({
                    title: this.language.showLoading.axiospaying,
                    mask: true
                });
                
                // #ifdef MP-WEIXIN
                this.weixinPay();
                // #endif
                
                // #ifndef MP-ALIPAY || MP-WEIXIN || MP-TOUTIAO
                this.pay_wx();
                // #endif
                
                // #ifdef MP-ALIPAY
                this.alipay_minipay();
                // #endif
                
                // #ifdef MP-TOUTIAO
                this.tt_pay();
                // #endif
                
                // #ifdef MP-BAIDU
                this.baidu_pay();
                // #endif
            },
            //百度小程序支付
            baidu_pay() {
                // #ifdef MP-BAIDU
                this.getOrderInfo('baidu_pay').then(orderInfo => {
                    uni.hideLoading();
                    laikepay.baidu_pay(this, orderInfo);
                });
                // #endif
            },

            //头条小程序支付
            tt_pay() {
                // #ifdef MP-TOUTIAO
                this.getOrderInfo('tt_alipay').then(orderInfo => {
                    uni.hideLoading();
                   laikepay.tt_pay(this, orderInfo);
                });
                // #endif
            },

            getOrderInfo(pay_type) {
                if (!pay_type) {
                    uni.showToast({
                        title: this.language.recharge.toasts[3],
                        duration: 1000,
                        icon: 'none'
                    });
                    this.firstFlag = true;
                    return;
                }
                let me = this;
                let time = new Date().getTime();
                let is_sNo = this.getCZNo()
                let data = {
                    access_id: this.access_id,
                    order_list: this.order_list,
                    sNo: is_sNo,
                    title: '充值',
                    // module: 'app',
                    // action: 'pay',
                    api:'app.pay.index',
                    type: pay_type,
                    currency_symbol:me.currency_symbol,
                    currency_code:me.currency_code,
                    exchange_rate:me.exchange_rate,
                    total: this.price,
                    remarks:this.remarks
                };
                uni.hideLoading();


                /**
                 * 如果是贝宝充值 需要缓存 数据信息
                 */
                if(pay_type == 'paypal'){
                    const payObj ={
                        time: getDateAll(),
                        total: this.price,
                        sNo: is_sNo,
                        remarks:this.remarks //订单备注
                    } 
                    uni.setStorageSync('payObj',JSON.stringify(payObj))
                }
                if(pay_type == 'stripe'){
                    const payObj ={
                        time: getDateAll(),
                        total: this.price,
                        sNo: is_sNo,
                        remarks:this.remarks //订单备注
                    } 
                    uni.setStorageSync('payObj',JSON.stringify(payObj))
                } 
                

                // #ifdef H5
                data.code = this.code;
                data.openid = uni.getStorageSync('openid') || ''
                // #endif
              
                let p = this.LaiKeTuiCommon.getMPAliAuthCode();
                return p.then(authcode => {
                    if (authcode) {
                        // #ifdef MP-ALIPAY
                        data.alimp_authcode = authcode;
                        // #endif
                        // #ifdef MP-TOUTIAO
                        data.tt_authcode = authcode;
                        // #endif
                    }
                    return new Promise((laikeOK, fail) => {
                        uni.request({
                            url: uni.getStorageSync('url'),
                            data,
                            success: result => { 
                                if (result.statusCode == 200) {
                                    // #ifdef H5
                                    
                                    if (data.type == 'paypal'){
                                        // debugger
                                        // 跳转贝宝支付页面
                                        window.location.href = result.data.data.data;
                                    }

                                    if (data.type == 'stripe') {
				                        // 跳转 stripe 支付页面
                                        const stripe_id = result.data.data.stripe_id
                                        const publishable_key = result.data.data.publishable_key
                                        
                                        // 初始化 Stripe（用你的 publishable key）
                                        const stripe = Stripe(publishable_key);
                                        
                                        // 重定向到 Stripe Checkout
                                        stripe.redirectToCheckout({
                                          sessionId: stripe_id
                                        }).then(function (result) {
                                          if (result.error) {
                                            uni.showToast({
                                              title: result.error.message,
                                              icon: 'none'
                                            });
                                          }
                                        });
                                    }


                                    // #endif
                                    // #ifndef MP-ALIPAY
                                    // alipay_mobile代表h5的支付宝参数
                                    if (pay_type == 'alipay_mobile') {
                                        const div = document.createElement('div');
                                        div.innerHTML = result.data.data;
                                        document.body.appendChild(div);
                                        document.forms[0].submit();
                                    }

                                    if (result && result.data && result.data.data.pay_type == 'H5_wechat') {
                                        window.location.href = result.data.data.url;
                                        return;
                                    }
                                    laikeOK(result.data);
                                    // #endif
                                    // #ifdef MP-ALIPAY
                                    let tno = result.data;
                                    laikeOK(tno.substr(1, tno.length));
                                    // #endif
                                }
                            },
                            fail: e => {
                                // #ifdef MP-ALIPAY
                                let tno = e.data;
                                if (tno.length > 28) {
                                    laikeOK(tno.substr(tno.indexOf('s') + 1, tno.length -
                                        4));
                                } else {
                                    this._payFail();
                                }
                                // #endif
                            }
                        });
                    });
                });
            },

            //获取充值订单号
            getCZNo() {
                let time = new Date().getTime();
                return (
                    'CZ' +
                    time +
                    Math.floor(Math.random() * 10) +
                    Math.floor(Math.random() * 10) +
                    Math.floor(Math.random() * 10) +
                    Math.floor(Math.random() * 10) +
                    Math.floor(Math.random() * 10) +
                    Math.floor(Math.random() * 10)
                );
            },
            //支付宝小程序支付
            alipay_minipay() {
                // #ifdef MP-ALIPAY
                this.getOrderInfo('alipay_minipay').then(trade_no => {
                    my.tradePay({
                        tradeNO: trade_no,
                        success: res => {
                            if (res.resultCode == 9000) {
                                uni.showToast({
                                    title: this.language.recharge.toasts[4],
                                    icon: 'none'
                                });
                                this.rechargeSuccess('alipay');
                            } else if (res.resultCode == 6001) {
                                setTimeout(() => {
                                    this._payFail();
                                }, 1000);
                            }
                        },
                        fail: res => {
                            uni.showToast({
                                title: res.memo,
                                icon: 'none'
                            });
                            setTimeout(() => {
                                this._payFail();
                            }, 1000);
                        }
                    });
                });
                // #endif
            },
            rechargeSuccess(paytype) {
                setTimeout(() => {
                    uni.redirectTo({
                        url: 'rechargeSuccess?mylei=1&money=' + this.price + '&_type=' + paytype+'&time='+this.getTimeNow()+'&remarks='+this.remarks
                    });
                }, 1000);
            },
            //微信小程序充值
            weixinPay() {
                var me = this;
                this.loading = true;
                uni.login({
                    provider: 'weixin',
                    success: e => {
                        var pay_type = 'mini_wechat';
                        let data = {
                            code: e.code,
                            access_id: me.access_id,
                            sNo: me.getCZNo(),
                            title: '充值',
                            // module: 'app',
                            // action: 'pay',
                            api:'app.pay.index',
                            type: pay_type,
                            
                            currency_symbol:me?.currency_symbol,
                            currency_code:me.currency_code,
                            exchange_rate:me.exchange_rate,
                            
                            total: me.price,
                            remarks:this.remarks
                        };
                        uni.request({
                            url: uni.getStorageSync('url'),
                            data,
                            success: res => {
                                if (res.statusCode !== 200) {
                                    uni.showModal({
                                        title: me.language.recharge.modalTips[0],
                                        confirmText: me.language.paymodel.iSee,
                                        showCancel: false,//没有取消按钮的弹框

                                        success: function(res) {
                                            me.fastTap = true;
                                        }
                                    });
                                } else if (res.statusCode == 200) {
                                    let paymentData = res.data.data;
                                    uni.requestPayment({
                                        provider: 'wxpay',
                                        timeStamp: paymentData.timeStamp,
                                        nonceStr: paymentData.nonceStr,
                                        package: paymentData.package,
                                        signType: paymentData.signType,
                                        paySign: paymentData.paySign,
                                        remarks:this.remarks,
                                        success: res => {
                                            uni.showToast({
                                                title: me.language.recharge
                                                    .toasts[5],
                                                duration: 1000,
                                                icon: 'none'
                                            });
											 
                                            setTimeout(function() {
                                                uni.redirectTo({
                                                    url: 'rechargeSuccess?mylei=1&type=wx&_type=wx&money=' +
                                                        me.price+'&time='+me.getTimeNow() +'&remarks='+this.remarks
                                                });
                                            }, 1000);
                                            uni.hideLoading();
                                        },
                                        fail: res => {
                                            uni.showModal({
                                                title: me.language
                                                    .recharge.modalTips[0],
                                                confirmText: me.language.paymodel.iSee,
                                                showCancel: false,//没有取消按钮的弹框

                                                success: function(res) {
                                                    me.fastTap = true;
                                                }
                                            });
                                            uni.hideLoading();
                                        }
                                    });
                                } else {
                                    uni.showModal({
                                        title: me.language.recharge.modalTips[0],
                                        confirmText: me.language.paymodel.iSee,
                                        showCancel: false,//没有取消按钮的弹框

                                        success: function(res) {
                                            me.fastTap = true;
                                        }
                                    });
                                    uni.hideLoading();
                                }
                            },
                            fail: e => {
                                this.loading = false;
                                uni.showModal({
                                    title: me.language.recharge.modalTips[0],
                                    confirmText: me.language.paymodel.iSee,
                                    showCancel: false,//没有取消按钮的弹框

                                    success: function(res) {
                                        me.fastTap = true;
                                    }
                                });
                                uni.hideLoading();
                            }
                        });
                    },
                    fail: e => {
                        this.loading = false;
                        uni.showModal({
                            title: me.language.recharge.modalTips[0],
                            confirmText: me.language.paymodel.iSee,
                            showCancel: false,//没有取消按钮的弹框

                            success: function(res) {
                                me.fastTap = true;
                            }
                        });
                        uni.hideLoading();
                    }
                });
            },
            ///支付失败
            _payFail() {
                uni.showModal({
                    title: this.language.recharge.modalTips[1],
                    confirmText: this.language.paymodel.iSee,
                    showCancel: false,//没有取消按钮的弹框

                    success: res => {
                        this.firstFlag = true;
                        // #ifdef H5
                        var url = uni.getStorageSync('h5_url') + 'pagesB/myWallet/myWallet';
                        if (res.cancel) {
                            url = uni.getStorageSync('h5_url') + 'pages/shell/shell?pageType=home';
                        }
                        setTimeout(function() {
                            window.location.href = url;
                        }, 1000);

                        // #endif
                        // #ifndef H5
                        this.$store.state.payRes = this.order_list;
                        if (res.confirm) {
                            uni.redirectTo({
                                url: './myWallet'
                            });
                        } else if (res.cancel) {
                            uni.redirectTo({
                                url: '/pages/shell/shell?pageType=home'
                            });
                        }
                        // #endif
                    }
                });
            },

            async pay_wx() { 
                let me = this;
                var pay_type = 'app_wechat';
                var store_type = 1;
                var providerStr = '';
                if (me.pay_provider == 'alipay_mobile' || me.pay_provider == 'alipay') {
                    // #ifdef H5
                    providerStr = 'alipay_mobile';
                    pay_type = 'alipay_mobile';
                    // #endif
                    // #ifndef H5
                    providerStr = 'alipay';
                    pay_type = 'alipay';
                    // #endif 
                } else if(me.pay_provider == 'H5_wechat' || me.pay_provider == 'app_wechat') {
                    providerStr = 'wxpay';
                    //#ifdef APP-PLUS
                    pay_type = 'app_wechat';
                    // #endif
                    //#ifdef H5
                    let iswx = this.is_wx();
                    if (iswx) {
                        pay_type = 'jsapi_wechat';
                        //开启了 小程序内嵌H5（因要适配支付，所以微信公众号支付 -》小程序支付）
                        if(this.LaiKeTuiCommon.IS_SHARE_WECHAT_H5PAY){
                            pay_type = 'mini_wechat'
                        }
                    } else {
                        pay_type = 'H5_wechat';
                    }
                    // #endif
                    store_type = 2;
                } else if(me.pay_provider == 'paypal' ) {
                    pay_type = "paypal"; 
                } else if(me.pay_provider == 'stripe' ) {
                    pay_type = "stripe"; 
                }
                let orderInfo = await me.getOrderInfo(pay_type);

                if (orderInfo.code != 200) {
                    me._payFail();
                    return;
                }
                uni.hideLoading();
                // #ifdef H5
                if (pay_type == 'alipay') {
                    var url = uni.getStorageSync('endurl') + 'order/' + me.sNo + '_alipay.html';
                    window.location.href = url;
                } else if (pay_type == 'jsapi_wechat' || pay_type == 'mini_wechat') {
                    var paymentData = orderInfo.data;
                    function onBridgeReady() {
                        WeixinJSBridge.invoke(
                            'getBrandWCPayRequest', {
                                appId: paymentData.appid, //公众号名称，由商户传入
                                timeStamp: paymentData.timeStamp, //时间戳，自1970年以来的秒数
                                nonceStr: paymentData.nonceStr, //随机串
                                package: paymentData.package,
                                signType: paymentData.signType, //微信签名方式：
                                paySign: paymentData.paySign 
                                // ,//微信签名 remarks:this.remarks
                            },
                            function(res) {
                                me.firstFlag = true;
                                me.code == '';
                                var url = window.location.href;
                                var preUrl = url.split('#')[0];
                             
                                var params = {
                                    mylei: 1,
                                    type: 'jsapi_wechat', //公众号支付
                                    money: me.price,
                                    time: me.getTimeNow() ,
                                    remarks :me.remarks
                                };
                                
                                // 将对象转换为查询字符串
                                var queryString = Object.keys(params).map(function(key) {
                                    return encodeURIComponent(key) + '=' + encodeURIComponent(params[key]);
                                }).join('&'); 
                                
                                var succUrl = preUrl.concat('#/pagesE/pay/payResults') 
                                // var succUrl = preUrl.concat('#/pagesB/myWallet/rechargeSuccess');
                                // 如果原 URL 中没有查询字符串，则添加 `?`，否则添加 `&`
                                
                                if (preUrl.indexOf('?') === -1) {
                                    succUrl = succUrl.concat('?' + queryString);
                                } else {
                                    succUrl = succUrl.concat('&' + queryString);
                                }

                                //支付成功
                                if (res.err_msg == 'get_brand_wcpay_request:ok') {
                                    me.$store.state.payRes = me.order_list;
                                    var price = me.price1;
                                    var sno = me.sNo;
                                    window.location.href = uni.getStorageSync('h5_url') +
                                        'pagesB/myWallet/myWallet';
                                } else {
                                    //支付失败
                                    me._payFail();
                                }
                            }
                        );
                    }
                    //微信公众号 选择微信支付判断
                    if(this.LaiKeTuiCommon.IS_SHARE_WECHAT_H5PAY){
                        //开启了 小程序内嵌H5（因要适配支付，所以微信公众号支付 -》小程序支付）
                        //直接跳转当前小程序 进行小程序支付（支付逻辑在小程序进行）
                        //pages/wxpay/payment/payment?timeStamp=&nonceStr=&package=&paySign=&mylei=&money=&time=
                        var jweixin = require('jweixin-module')
                        let isType = 'cz' //充值支付：cz
                        let urls = '/pages/wxpay/payment/payment' + 
                                    '?timeStamp=' + paymentData.timeStamp + 
                                    '&nonceStr=' + paymentData.nonceStr + 
                                    '&package=' + paymentData.package.slice(10) + 
                                    '&paySign=' + paymentData.paySign + 
                                    '&isType=' + isType + 
                                    '&total=' + this.price + 
                                    '&orderTime=' + this.getTimeNow() + 
                                    '&remarks='+ this.remarks 
                        //跳转至目标小程序进行支付操作
                        jweixin.miniProgram.navigateTo({
                            url: urls, 
                            success(success) {
                            },
                            fail(fail) {
                            }
                        })
                    } else if(typeof WeixinJSBridge != 'undefined') { 
                        onBridgeReady(paymentData);
                    } else {
                        if (typeof WeixinJSBridge == 'undefined') {
                            if (document.addEventListener) {
                                document.addEventListener('WeixinJSBridgeReady', onBridgeReady, false);
                            } else if (document.attachEvent) {
                                document.attachEvent('WeixinJSBridgeReady', onBridgeReady);
                                document.attachEvent('onWeixinJSBridgeReady', onBridgeReady);
                            }
                        }
                    }
                } else {
                }
                // #endif
                // #ifndef H5 || MP-BAIDU
                uni.requestPayment({
                    provider: providerStr,
                    orderInfo: orderInfo.data, //订单数据
                    success: function(res) {
                        uni.showToast({
                            title: me.language.recharge.toasts[5],
                            duration: 1000,
                            icon: 'none'
                        });

                        var url = 'rechargeSuccess?mylei=1&money=' + me.price;+'&time='+me.getTimeNow()+'&remarks='+this.remarks
                        if ('alipay' == providerStr) {
                            url = url + '&_type=alipay';
                        } else if ('wxpay' == providerStr) {
                            url = url + '&_type=wx';
                        }

                        setTimeout(function() {
                            uni.redirectTo({
                                url: url
                            });
                        }, 1000);
                        me.fastTap = true;
                    },
                    fail: function(err) {
                        uni.showModal({
                            title: me.language.recharge.modalTips[0],
                            confirmText: me.language.paymodel.iSee,
                            showCancel: false,//没有取消按钮的弹框

                            success: function(res) {}
                        });
                        me.fastTap = true;
                    }
                });
                // #endif
            },

            getUserCurrencyInfo(){ 
                let currency = uni.getStorageSync('currency');
                //用户自己选择的货币符号
                this.currency_symbol = currency.currency_symbol
                //汇率
                this.exchange_rate = currency.exchange_rate
                //货币符号
                this.currency_code = currency.currency_code;

            },

            //支付方式选择
            _pay(index) {
                this.pay_index = index;
                this.pay_style = index + 1;

                if (index == 0) {
                    // #ifdef H5
                    this.pay_provider = 'alipay_mobile';
                    // #endif
                    // #ifndef H5
                    this.pay_provider = 'alipay';
                    // #endif
                } else if (index == 1) {
                    this.pay_provider = 'app_wechat';
                } else if (index == 3) {
                    this.pay_provider = 'H5_wechat';
                } else if (index == 2) {
                    this.pay_provider = 'baidu_pay';
                } else if (index == 4) {
                    this.pay_provider = 'paypal';
                } else if (index == 5) {
                    this.pay_provider = 'stripe';
                }
                
            }
        },
        components: {
            heads
        }
    };
</script>

<style scoped lang="less">
    @import url("@/laike.less");
    @import '../../static/css/myWallet/recharge.less'; 
</style>
