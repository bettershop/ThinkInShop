<template>
    <view class='container'>
        <lktauthorize ref="lktAuthorizeComp" ></lktauthorize>
        <heads :title='language.bond.payText.title' :bgColor="bgColor" titleColor="#333333" ishead_w="2"></heads>
        <paymodel @cancel="wallet_pay_cancel" @success="check_pay_password" v-model="pay_display"/>
        <view class="con" v-if="show==1">
            <view class="top">
                <view class="top_price" :style="{background: 'url(' + payText_bgImg + ') 80% 45%'  }">
                    <view class="Nth" v-if="margin">
                      {{Number(margin).toFixed(2)}}
                    </view>
                    <view class="name">{{language.bond.payText.bzj}}({{language.freight_default.yuan}})</view>                    
                </view>
                
                <view class="btns">
                    <view class="btns_left" @tap="navTo('/pagesC/bond/paymentRecord')">{{language.bond.payText.jnjl}}</view>
                    <view class="btns_right" @tap="tagShow">{{language.bond.payText.title}}</view>
                </view>
            </view>
            
             <view class="bottom">
                 <view class="explain">{{language.bond.payText.bzjsm}}</view>
                 <view class="vHtml" v-html="margin_description"></view>
             </view>
        </view>
        <view class="con" style="background-color: #ffffff;" v-if="show==2">
            
			<choose-pay ref="choosePay"
			 :pay_way='pay_way'
			 :open_pay='open_pay'
			 :user_money="user_money"
			    @chooseWay="chooseWay($event)" />
            <submit-order
                :ishide="ishide"
                :total="margin"
                :rate="total_discount"
                @submit="order_pay2()"
            />
        </view>
    </view>
</template>

<script>
    import Heads from '../../components/header.vue'
    import mixinsPay from '../../mixins/pay'
    import mixinsOrder from '../../mixins/order'
    import choosePay from '@/components/choose-pay.vue'
    import submitOrder from '@my-miniprogram/src/components/order/submit-order.vue'
    import paymodel from '@/components/paymodel.vue'
    
    export default {
        data () {
            return {
                show:1,
                margin:'',
                margin_description:'',
                ishide:0,
                total_discount:'0',
                user_money:'0',
                
                commodity_type:'1',
                payTitle:'保证金',
                onpage:true,
				bondStatus:true, //控制取消支付的彈框
                
                payResult: '',
                isBond: true,
                
                payText_bgImg:this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/payText_bgImg.png',
                bgColor: [{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
            }
        },
        mixins: [mixinsPay,mixinsOrder],
        components: {
            Heads,
            paymodel,
            choosePay,
            submitOrder
        },
        computed: {
            
        },
        onLoad (option) {
            this.getCode()
            this.checkCode()
        },
        onUnload () {
            // #ifdef H5
            if (location.hostname === 'localhost') return false;
            setTimeout(() => {
                let state = window.location.href.replace(/\?code=.*?\//, '#/')
                history.replaceState(null, null, state)
            }, 300)
            // #endif
            
        },
        onShow () {
            this._axios()
        },
        watch: {
        },
        mounted() {
          
        },
        methods: {
            tagShow() {
              this.show = 2  
            },
            goto(url) {
                uni.navigateTo({
                    url:url
                })
            },
            _axios () {
                var me = this
                var data = {
                    api:"mch.App.Promise.Index"
                }
                this.$req.post({data}).then(res => {
                    if (res.code == 200) {
                        this.margin=res.data.promisePrice*1
                        this.margin_description=res.data.promiseText
                        this.user_money=res.data.userMoney*1
                        this.payment = res.data.payment
                        this.defaultpayment = res.data.defaultpayment
                        this.password_status = res.data.isSetPayment
                        this.total = this.margin
                        this.show_pay_way(this.defaultpayment)
                    }else{
                        uni.showToast({
                            title:res.message,
                            duration: 1000,
                            icon: 'none'
                        })
                    }
                })
            },
            //只有保证金支付用到了
            async order_pay2 () {
                //检测用户数的选择支付方式，通过全局声明的参数
                this._checkSelectPayWay()
                //如果是微信支付--执行
                // #ifdef MP-WEIXIN
                // this._toUrl()
                wx.requestSubscribeMessage({
                    tmplIds: this.tmplIds,
                    complete:async () => {
                        uni.showLoading({
                            title: this.language.showLoading.waiting
                        })
                        uni.hideLoading()
                        if (this.pay_way=='useWallet') {
                            //判断是否 有支付密码
                            if (this.password_status == false) {
                                uni.showModal({
                                    title: this.language.showModal.hint,
                                    content: this.language.showModal.mima,
            						confirmText: this.language.showModal.confirm,
            						cancelText: this.language.showModal.cancel,
                                    showCancel: true,
                                    success: (resM) =>  {
                                        this.fastTap = false
                                        if (resM.confirm) {
                                            uni.navigateTo({
                                                url: '/pagesB/setUp/payment'
                                            })
                                        }
                                    }
                                })
                                return false;
                            }
                            
                            if (this.pwd != '') {
                                await this._getPayOrderInfo2()
                            }
                            this._orderUseWalletPay()
                            
                        } else {
                            uni.showLoading({
                                title: this.language.showLoading.axiospaying,
                                mask: true,
                            })
                            await this._getPayOrderInfo2()
                            await this._notUserWalletPay()
                        }
                    }
                })
                // #endif
                //如果不是微信支付--执行
                // #ifndef MP-WEIXIN
                //如果是余额支付
                if (this.pay_way=='useWallet') {
                    //没有支付密码 就去设置支付密码
                    if (this.password_status == 0) {
                        
                        uni.showModal({
                            title: this.language.showModal.hint,
                            content: this.language.showModal.mima,
            				confirmText: this.language.showModal.confirm,
            				cancelText: this.language.showModal.cancel,
                            showCancel: true,
                            success: (resM) =>  {
                                this.fastTap = false
                                if (resM.confirm) {
                                    uni.navigateTo({
                                        url: '/pagesB/setUp/payment'
                                    })
                                }
                            }
                        })
                        return false;
                    }
                    //如果支付密码 已经输入了 就直接调用_getPayOrderInfo2
                    if (this.pwd != '') {
                        await this._getPayOrderInfo2()
                    }
                    //有支付密码，但是没输入；调用输入弹窗输入密码  
                    //输入完成后调用pay.js中check_pay_password 进行余额支付
                    this._orderUseWalletPay()
                } else {
                    //不是余额支付--执行
                    uni.showLoading({
                        title: this.language.showLoading.axiospaying,
                        mask: true,
                    })
                    await this._getPayOrderInfo2()
                    //判断是什么支付方式 微信、支付宝。。。
                    await this._notUserWalletPay()
                }
                // #endif
            
            },
            async _getPayOrderInfo2 () {
                let me = this
                let remarks = this.remarks
                if(typeof remarks != 'string'){
                    if( remarks&& remarks !="" && remarks.length > 0 ){
                        remarks = JSON.stringify(remarks)
                    }
                    
                }
                
                let postData = {
                    api:"mch.App.Promise.Payment",
                    mch_id: uni.getStorageSync('mch_id'),
                    money: this.margin,
                    pwd: this.pay_password
                }
                let { pay_type } = this.getPayTypeAndStoreType()
                
                postData.payType = pay_type || 'wallet_pay'
                let {
                    data,
                    code,
                    message,
                    status
                } = await this.$req.post({ data: postData })
                if (code == 200) {
                    this.sNo = data.orderNo;
                    this.order_id = data.orderId;
                    this.$store.state.order_id = data.orderNo;
                    //手动获取下单时间
                    let time = new Date().getTime() + 8 * 60 * 60 * 1000;
                    let orderTimes = new Date(time).toISOString().replace(/T/, ' ').replace(/\..+/, '').substring(0, 19)
                    this.order_pay_info = JSON.stringify({ 
                        total:data.total,
                        sNo: data.orderNo,
                        order_id: data.orderId,
                        orderTime: orderTimes,
                    })
                    uni.setStorageSync('payRes', this.order_pay_info)
                }else if(code == 999){
                    uni.showToast({
                        title: message,
                        duration: 1500,
                        icon: 'none'
                    })
                     return Promise.reject()
                } else {
                    uni.showToast({
                        title: message,
                        duration: 1500,
                        icon: 'none'
                    })
                    this.pay_display = false
                    setTimeout(() => {
                        this.navBack()
                    }, 1500)
                    return Promise.reject()
                }
            },
            async bond_pay () {
                let me = this
                let remarks = this.remarks
                if(typeof remarks != 'string'){
                    if( remarks&& remarks !="" && remarks.length > 0 ){
                        remarks = JSON.stringify(remarks)
                    }
                    
                }
                
                let postData = {
                    api:"mch.App.Promise.Payment",
                    mch_id: uni.getStorageSync('mch_id'),
                    money: this.margin,
                    pwd: this.pay_password
                }
            
                let { pay_type } = this.getPayTypeAndStoreType()
                
                postData.payType = pay_type || 'wallet_pay'
                let {
                    data,
                    code,
                    message,
                    status
                } = await this.$req.post({ data: postData })
                if (code == 200) {
                    
                    this.sNo = data.orderNo;
                    this.order_id = data.orderId;
                    this.$store.state.order_id = data.orderNo;
                    this.order_pay_info = JSON.stringify({ 
                        total:data.total,
                        sNo: data.orderNo,
                        order_id: data.orderId
                    })
                    
                    setTimeout(()=>{                    
                        // 此处加一层this.payTitle != '保证金'（因为保证金h5支付宝支付时会存在还未支付就显示结果页的bug——>31373）
                        if(me.payResult == '支付失败'){
                            return 
                        }else if(me.payResult != '支付失败'&&me.isBond&&this.payTitle != '保证金') {
                            uni.redirectTo({
                                url: '/pagesA/myStore/myStore'
                            })
                        }
                        else if(this.payTitle == '保证金'){
                            uni.redirectTo({
                                 url: '/pagesC/bond/success?str=true'
                            })
                        }
                        
                    },2000)
                    
                }else if(code == 999){
                    uni.showToast({
                        title: message,
                        duration: 1500,
                        icon: 'none'
                    })
                     return Promise.reject()
                } else {
                    uni.showToast({
                        title: message,
                        duration: 1500,
                        icon: 'none'
                    })
                    this.pay_display = false
                    setTimeout(() => {
                        this.navBack()
                    }, 1500)
                    return Promise.reject()
                }
            },
        },
        
       
    }
</script>
<style>
    page {
        background-color: #f4f5f6;
    }
</style>
<style lang="less" scoped> 
    @import url("@/laike.less");
    @import url("../../static/css/myStore/bond/payText.less");
    
</style>
