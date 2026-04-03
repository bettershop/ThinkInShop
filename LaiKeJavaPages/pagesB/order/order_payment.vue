<template>
    <view class="order_payment">
        <heads :isMs="type" :returnR="zfbPay?120:''" :title="language.order.order_pay.title" :bgColor="bgColor" :titleColor="titleColor"  ishead_w="2"></heads>
        <!-- #ifdef APP-PLUS -->
        
        <!-- #endif -->
        <paymodel  @cancel="wallet_pay_cancel" @success="check_pay_password" v-model="pay_display"/>
        <view v-if="otype == 'IN' && total<=0" class="score-box">
                <view class="title">
                    {{language.integral.integral_order.points_pay}}
                    <view class="title points-color">
                       （{{language.integral.integral_order.surplus_points}}
                        {{user_score}}）
                    </view>
                </view>
                <view class="points">
                    -{{language.integral.integral_order.points}}{{allow}}
                </view>
        </view> 
        <choose-pay 
        v-else
          ref="choosePay"
         :pay_way='pay_way'
         :open_pay='open_pay'
         :priceInfo='{total}'
         :pureIntegral="total>0"
         :user_money="user_money"
            @chooseWay="chooseWay($event)" />


        <submit-order
            :ishide="0"
            :total="total"
            :score="allow"
            @submit="order_pay('order_payment')"
        />
    </view>
</template>
<script>
    import paymodel from '@/components/paymodel.vue'
    import choosePay from '@/components/choose-pay.vue'
    import submitOrder from '@my-miniprogram/src/components/order/submit-order.vue'
    import mixinsPay from '../../mixins/pay'
    import mixinsOrder from '../../mixins/order'
    import { later } from '@/common/util'

    export default {
        data () {
            return {
                tmplIds: ['2KrrJchj92YRKhZZ0SSHz76dmrT0cLBJ2Wfe0', 'ncs3u3Bmmi0jW7EXAik4KQvxF3JxbaulWNwbLXDto', 'CZAPo_TqOOeC5K7XYvBeB_LXmyXKIhXkZROArNZDwQ8'],//微信订阅消息模板
                ordertype: '',
                return_r:5,
                type: '',
                types: '',
                allow:'',//商品所需积分
                user_score:'',//用户积分
                sellType: '',
                otype:'',
                is_preSale: '',
                bgColor:[{
                        item: '#fff'
                    },
                    {
                        item: '#fff'
                    }
                ],
                titleColor:"#000000",
                again: true,//重新支付-待付款支付
                //待付款订单 支付宝取消支付时，内置浏览器不会执行支付回调，还在此页面。
                //此时点击返回需要关闭所有页面并返回首页，因此增加zfbPay参数用于判断。
                zfbPay: false,
                remove_new:true
            }
        },
        mixins: [mixinsPay, mixinsOrder],
        components: { paymodel, choosePay, submitOrder },
        onLoad (option) {
            this.sellType = option.sellType
            this.type = option.type 
            if(option.type == 'MS' || option.ordertype == 'MS') {
                this.types = 'MS'
            }
            if(option.type == 'IN' || option.ordertype == 'IN') {
                this.types = 'IN'
            }
            if(option.type == 'ZB' || option.ordertype == 'ZB') {
                this.types = 'ZB'
            }
            uni.setStorageSync('order_payment',JSON.stringify(option))
            this.ordertype = option.ordertype
            
            this.getCode()
            this.order_id = option.order_id
            if (option.showPay) {
                this.showPay = true
            }
            if(option.is_pt){
                this.return_r = ''
            }
            this.checkCode()
            // this._axios()
            
        },
        onShow() {
            this._axios()
           
            
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
        methods: {
            /**
             * 点击立即支付按钮触发
             * @returns {Promise<boolean>}
             */
            async order_pay () {
                //待付款订单 支付宝取消支付时，内置浏览器不会执行支付回调，还在此页面。
                //此时点击返回需要关闭所有页面并返回首页，因此增加zfbPay参数用于判断。
                  
                this.zfbPay = true
				//积分抵扣余额不需要 进行积分判断 因为实物商品积分抵扣 已经扣除了 积分
                if(this.sNo.substring(0,2).toUpperCase() == 'IN'){
                    if(this.allow>this.user_score){
                        uni.showToast({
                            icon:'none',
                            title:'用户积分余额不足'
                        })
                        return
                    }
                } 
                if (this.fastTap) return false
                this.fastTap = true 
                if (this.showPay) {
                    try {
                        await this._check_order_status();
                        
                    } catch(e) {
                        uni.showToast({
                            title: e,
                            icon: 'none'
                        })
                        this.fastTap = false
                        return false;
                    }
                }
                
                this._checkSelectPayWay()
                // #ifdef MP-WEIXIN
                uni.showLoading({
                    title: this.language.order.order_pay.Tips[0]
                })
                wx.requestSubscribeMessage({
                    tmplIds: this.tmplIds,
                    complete:async () => {
                        uni.hideLoading()
                        if (this.pay_way=='useWallet') {
                            if (!this.sNo) {
                                await this._getPayOrderInfo()
                            }
                            this._orderUseWalletPay()
                        } else {
                            uni.showLoading({
                                title: this.language.order.order_pay.Tips[1],
                                mask: true,
                            })
                            this._notUserWalletPay()
                        }
                    }
                })
                // #endif
                // #ifndef MP-WEIXIN
                if (this.pay_way=='useWallet') {
                    if (!this.sNo) {
                        await this._getPayOrderInfo()
                    }
                    this._orderUseWalletPay()
                } else {
                    uni.showLoading({
                        title: this.language.order.order_pay.Tips[1],
                        mask: true,
                    })
                    this._notUserWalletPay()
                }
                // #endif

            },
            /**
             * 取得调用支付的所需订单信息
             * @param pay_type
             * @returns {Promise<*>}
             */
            async getPaymentData () {
                let data = {
                    api: 'app.pay.index',
                    total: this.total,
                    sNo: this.sNo,
                    title: this.payTitle
                }
				
				if(this.is_preSale) {
                    if(uni.getStorageSync('payTarget')){
                        data.payTarget = uni.getStorageSync('payTarget')
                    }else{  
                        if(this.sellType==1){
                               data.payTarget = 2
                        }else{
                            data.payTarget =  3
                        }
                    }
				} 
                let { type, code, store_type } = await this.getOrderInfoExt()
                data.type = type
                data.code = code
                data.store_type = store_type
                data.openid = uni.getStorageSync('openid') || ''
                let auth_code = await this.LaiKeTuiCommon.getMPAliAuthCode()
                if (auth_code) {
                    // #ifdef MP-ALIPAY
                    data.alimp_authcode = auth_code
                    // #endif
                    // #ifdef MP-TOUTIAO
                    data.tt_authcode = auth_code
                    // #endif
                }

                return await this.$req.post({ data })
            },
            /**
             * 余额支付
             * @returns {Promise<void>}
             * @private
             */
            async _wallet_pay () {
                let postData = {
                    api: 'app.pay.wallet_pay',
                    type: 'wallet_pay',
                    order_id: this.order_id,
                    sNo: this.sNo,
                    // parameter: 'order',不知道这是啥玩意儿 2022年8月11日20:24:51
                    payment_money: this.total
                }
                if(this.sellType && this.sellType == 1) {
                    postData.payTarget =  2
                }
                if(this.sellType && this.sellType == 2) {
                    postData.payTarget =  3
                }
                let res = await this.$req.post({ data: postData })
                let { code } = res
                uni.hideLoading()
                if (code == 200) {
                    uni.showToast({
                        title: this.language.order.order_pay.Tips[2],
                        duration: 1000,
                        icon: 'none'
                    })
                    await later(1000)
                    uni.setStorageSync('payRes', this.order_pay_info)
                    if(this.is_preSale) {
                        uni.redirectTo({
                            url: '/pagesE/pay/PayResults?i_state=' + this.is_preSale
                            
                        })
                    } else {
                        uni.redirectTo({
                            url: '/pagesE/pay/PayResults'
                        })
                    }
                } else {
                    uni.showToast({
                        title: res.message,
                        duration: 1000,
                        icon: 'none'
                    })
                    return false;
                }
            },
            _axios () {
                if (uni.getStorageSync('order_payment')) {
                    let info = JSON.parse(uni.getStorageSync('order_payment'))
                    this.order_id = info.order_id
                    this.ordertype = info.ordertype
                    if (info.showPay) {
                        this.showPay = true
                    }
                }
                
                let action = 'order'
                if (this.ordertype === 'PT') {
                    action = 'groupbuy' 
                } else if (this.ordertype === 'KJ') {
                    action = 'bargain'
                } else if (this.ordertype === 'PP') {
                    action = 'pthd_groupbuy'
                }
                
                let data = {
                    order_id: this.order_id,
                    api: `app.${action}.order_details`,
                }
                this.$req.post({ data }).then(res => {
                    let {
                        code,
                        message,
                        data: {
                            add_time,
                            z_price: total,
                            payment,
                            defaultpayment,
                            user_money,
                            sNo,
                            list,
                            id
                        }
                    } = res
                    if(code != 200){
                        uni.showToast({
                            title:message,
                            icon:'error'
                        })
                        return
                    }
                    if(res.data.otype == 'PS') {
                        this.total = res.data.sellInfo.total.toFixed(2)
                        this.total = parseFloat(this.total)
                        this.is_preSale = true
                    }else if(res.data.otype=='IN'){
                        this.otype = res.data.otype
                            this.open_pay.open_wxpay = false
                            this.open_pay.open_alipay = false
                            this.total = total.toFixed(2)
                            this.total = parseFloat(this.total)
                    }else {
                        this.total = total.toFixed(2)
                        this.total = parseFloat(this.total)
                    }
                    this.payment = payment
                    this.defaultpayment = defaultpayment
                    this.user_money = Number.parseFloat(user_money)
                    this.sNo = sNo
                    this.payTitle = list[0].p_name
                    this.allow = res.data.allow? res.data.allow : res.data.omsg.scoreDeduction
                    this.user_score = res.data.user_score
                    this.order_pay_info = JSON.stringify({
                        order_id: id,
                        total: this.total,
                        sNo,
                        orderTime: add_time,
                        total_score:this.allow,
                    })
                    
                    this.show_pay_way(this.defaultpayment)
                    if (res.data.r_status != 0) {
                        uni.showToast({
                            title: '订单状态异常',
                            icon: 'none'
                        })
                        setTimeout(() => {
                            uni.navigateBack({
                                delta: 1
                            })
                        }, 1000)
                    }
                })
            },
           
        },
        
    }   
</script>

<style>
    page{
        background-color: #f4f5f6;
    }
</style>
<style lang="less" scoped> @import url("@/laike.less");
    .choose-pay{
        border-radius:0 0 26rpx 26rpx;
    }
    .score-box{
        background: #FFFFFF;
        display: flex;
        flex-flow: row nowrap;
        justify-content: space-between;
        align-items: center;
        padding: 30rpx 36rpx;
        .points{
            color:#FA5151;
            font-size: 32rpx;
        }
        .title {
            display: flex;
            justify-content: center;
            align-items: center;
        }

        .points-color {
            font-size: 28rpx !important;
            color: #999999 !important;
        }
    }
</style>
