<template>
    <div class="mybody">
        <heads :bgColor='bgColor' :ishead_w="ishead_w" titleColor="#333333" :title="mylei!=1?'提现':'充值'"></heads>
        <div class="success_head">
            <img :src="gouhei" />
            <p class="success_title">{{ title_p }}</p>
            <p class="success_title_disc" v-if="isBankCard">{{language.rechargeSucess.Tips}}</p>
        </div>
        <div class="hr"></div>
        <ul class="ulbox">
            <li v-if="mylei!=1">
                <p style="width: 192rpx;font-weight: 500;">{{language.rechargeSucess.txxx}}</p>
               
            </li>
            <li v-else>
                <p style="width: 192rpx;font-weight: 500;">{{language.rechargeSucess.czxx}}</p>
               
            </li>
            <li v-if="type">
                <p style="width: 192rpx;font-weight: 500;">{{ card }}：</p>
                <div>
                    <span>{{ back }}</span>
                </div>
            </li>
            <li>
                <p style="width: 192rpx;font-weight: 500;">{{ text_p }}：</p>
                <p>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(money) }}</p>
            </li>
            <li v-if="mylei!=1">
                <p style="width: 192rpx;font-weight: 500;">{{language.rechargeSucess.sxf}}：</p>
                <p>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(poundages) }}</p>
            </li>
            <li v-if="mylei!=1">
                <p style="width: 192rpx;font-weight: 500;">{{language.rechargeSucess.dzje}}：</p>
                <p>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice((Number(money) - Number(poundages))) }}</p>
            </li>
            <li  v-if="isBankCard">
                <p style="width: 192rpx;font-weight: 500;">{{ card }}：</p>
                <div v-if="tjType == 'paypal'" class="isBankCard-wx">
                    <image :src="paypallogo"></image>
                    <span>paypal支付</span>
                </div>
                <div v-if="tjType == 'stripe'" class="isBankCard-wx">
                    <image :src="stripe_logo"></image>
                    <span>stripe支付</span>
                </div>
                <div v-else-if="tjType != 'WX'">
                    <span>{{id_catd}}</span>
                </div>
                <div v-else class="isBankCard-wx">
                    <image :src="wx"></image>
                    <span>微信零钱</span>
                </div>
            </li>
            <li>
                <p style="width: 192rpx;font-weight: 500;">{{language.rechargeSucess.czsj}}：</p>
                <p>{{ time }}</p>
            </li>
            
            
        </ul>
        <!-- 订单备注 -->
        <ul class="ulbox" style="margin-top: 10rpx;">
            <li>
                <p style="width: 192rpx;font-weight: 500;">{{language.pay.orderDetailsr.Order_notes}}：</p>
                <p style="width: 540rpx;overflow: auto">{{ remarks }}</p>
            </li>
        </ul>
        
        <!-- 返回首页 -->
        <div class="go_shopping" @tap="_toHome()">
            {{language.rechargeSucess.Back_home}}
        </div>
        <!-- 返回店铺 -->
        <div v-if="store" class="look_order" @tap="_back1()">
            {{language.rechargeSucess.Back_shop}}
        </div>
        <!-- 返回上一页（我的钱包） -->
        <div v-if="!store && !LaiKeTuiCommon.IS_SHARE_WECHAT_H5PAY" class="look_order" style="color: #FA5151;" @tap="_back()">
            {{language.rechargeSucess.Back_wallet}}
        </div>
    </div>
</template>

<script>
export default {
    data() {
        return {
            paypallogo: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/beibao.png',
            stripe_logo: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/stripe_logo.png',
            gouhei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/gouhei.png',
            type: '',
            title: '',
            title_p: '',
            text_p: '',
            bgColor: [{
                                item: '#ffffff'
                            },
                            {
                                item: '#ffffff'
                            }
                        ],
                        ishead_w:'2',
            card: '',
            remarks:'',
            back: '',
            back_number: '',
            money: '',
            flag: true,
            store: false,
            isBankCard: false, //是否是银行卡支付
            back_type: '', // 1 从提现详情重新申请过来的
            poundages: '',//手续费用
            realMoney: '',
            id_catd:'',//带银行名称的储蓄卡信息
            mylei:'',
            time:'',
            tjType: '',//提现类型 默认为银行卡 ‘WX’为微信提现
            wx: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/wx.png',
        };
    },
    onLoad(option) {
		this.setLang();
		this.tjType = option.tjType||''
		this.back_type = option.back_type
		
        var str = option.id_catd;
        this.id_catd=str
        this.money = option.money;
        if(option.type){
            this.type = option.type;
        }else{
            this.type = option._type;
        }
        this.poundages = option.poundages ? Number(option.poundages).toFixed(2) : 0
        this.mylei = option.mylei
        this.time = option.time
        this.remarks = option.remarks
        if (option._type == 'wx') {
            this.isBankCard = false;
        }

        if (this.type == undefined) {
            this.type = option._type;
        }
        if (option.mylei == 1) {
            this.title = this.language.rechargeSucess.title;
            this.title_p = this.language.rechargeSucess.title;
            this.text_p = this.language.rechargeSucess.text_p;
            if (this.type) {
                this.card = this.language.rechargeSucess.card;
                if (this.type == 'wx') {
                    this.back = this.language.rechargeSucess.back_wx;
                    this.isBankCard = false;
                } else if (this.type == 'alipay' || this.type=='alipay_mobile') {
                    this.back = this.language.rechargeSucess.back_ali;
                    this.isBankCard = false;
                }
            } else {
                this.card = this.language.rechargeSucess.card1;
            }
        } else {
            this.isBankCard = true;
            this.title = this.language.rechargeSucess.title1;
            this.title_p = this.language.rechargeSucess.title_p1;
            this.text_p = this.language.rechargeSucess.text_p1;
            this.card = this.language.rechargeSucess.card1;
            this.back = option.id_name;
            this.back_number = str.substr(str.length - 5);
            this.money = option.id_monsy;
        }
        this.realMoney = (Number(this.money) - Number(this.poundages)).toFixed(2)
        
        // 如果是贝宝 充值时渲染
        if(uni.getStorageSync('payObj') && this.mylei == 1){
            this.paypal_token = option.token
            const payObj= JSON.parse(uni.getStorageSync('payObj'))
            this.remarks = payObj.remarks
            this.time = payObj.time
            this.money = Number(payObj.total || 0).toFixed(2)
            this.sNo = payObj.sNo
            this.capture()
        }
        //#ifdef H5
        window.document.title = this.title
        // #endif
    },
    onUnload(){
        if(uni.getStorageSync('payObj')){
            uni.removeStorageSync('payObj')
        }
    },
    methods: {
        _toHome() {
            uni.redirectTo({
                url: '/pages/shell/shell?pageType=home'
            });
        },
        _back() {
            this.flag = false;
            
            if (this.back_type == 1) {
                uni.redirectTo({
                    url: '/pagesA/myStore/myCha'
                })
            } else {
                uni.redirectTo({
                    url: '/pagesB/myWallet/myWallet'
                })
                // uni.navigateBack({
                //     delta: 1
                // });
            }
            
            
        },
        _back1() {
            this.flag = false;
            uni.navigateBack({
                delta: 3
            });
        },
        //贝宝执行扣款
        async capture() {
            // let paypal_token = this.paypal_token;
        	// let sNo = this.payReslist.sNo;
        	const  data = {
        		api:'app.pay.capture',
        		orderId : this.paypal_token,
        		sNo : this.sNo
        	}
        	await this.$req.post({data}).then(res => {
        	    uni.hideLoading()
        	    if(res.code == 200 ){
        	       // 成功
        	    } else {
        	        uni.showToast({
        	            title: res.message,
        	            icon: 'none'
        	        })
        	    }
        	}).catch(e => {
        	})
        }
    }
};
</script>

<style scoped lang="less">
@import url("@/laike.less");
@import url('../../static/css/myWallet/rechargeSuccess.less');
.mybody{
    min-height: 100vh;
    background-color: #f4f5f6;
}
</style>
