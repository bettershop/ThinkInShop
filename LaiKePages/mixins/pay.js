import {
	later
} from '@/common/util'
import {
	mapMutations
} from 'vuex';
// #ifdef H5
// 在 H5 中动态加载 Stripe.js，防止小程序/APP报错
  const script = document.createElement('script')
  script.src = 'https://js.stripe.com/v3/'
  document.head.appendChild(script)
// #endif

const PAY_TYPE = {
	ALI: 'ali',
	WX: 'wx',
	BAIDU: 'baidu',
	PAYPAL: 'paypal',
    STRIPE: 'stripe', // stripe支付
    OFFLINE_PAYMENT: 'offline_payment' // 线下支付
}

export default {
	data() {
		return {
			open_pay: {
				open_wallet: false, // 是否开启钱包支付
				open_wxpay: false, // 是否开启微信支付
				open_alipay: false, // 是否开启支付宝支付
				open_baidu: false, // 是否开启百度支付
				open_paypal: false, // 是否开启paypal支付
                open_stripe: false, // 是否开启stripe支付
                offline_payment: false, // 是否开启线下支付
			},

			pay_way: "",
			oderPayType: '', //订单支付方式 

			pay_display: false, // 显示余额支付密码框
			password_status: false, // 显示余额支付密码框
			pay_password: '', // 支付密码
			enterless: true, // true,密码错误，还能继续输入， false 密码错误达到上限
			payTitle: '', // 
			fastTap: false, //重复点击
            defaultpayment:'',
			payment: {
				H5_wechat: 0, // h5 微信支付
				jsapi_wechat: 0, // jsapi 微信支付
				mini_wechat: 0, // 小程序微信支付
				app_wechat: 0,
				alipay: 0,
				alipay_minipay: 0,
				tt_alipay: 0,
				wallet_pay: 0,
				paypal: 0,
                stripe: 0, // stripe支付
			},
            
			pay_type: '', // 支付方式 PAY_TYPE
			myappid: '', // 微信APPID
			code: '', // 微信授权 code
			showPay: false, // 从订单列表进来的订单需要获取订单详情
		}
	},
	//页面销毁重置code
	onUnload() {
		// 54430code只能用一次
		// 缓存一次上次使用的code
		console.log('页面离开调用，是否code有缓存xxx', this.code);
		uni.setStorageSync('pay_code', this.code)
		setTimeout(() => {
			this.code = ''
		})
	},
	methods: {
		...mapMutations({
			status: 'SET_STATUS'
		}),
		/**
		 * 客户端调用支付
		 * @returns {Promise<void>}
		 */
		async clientTransferPay() {
			let {
				data: paymentData,
				code,
				message
			} = await this.getPaymentData()
			if (code != 200) {
				//只有保证金判断
				if (this.payTitle && this.payTitle == '保证金') {
					this.payResult = '支付失败'
				}
				uni.showToast({
					title: message,
					icon: 'none'
				})
				return Promise.reject();
			}
			// 清空  
			uni.removeStorageSync('payTarget')
			let providerStr = ''
			if (this.pay_type == PAY_TYPE.WX) {
				providerStr = 'wxpay'
			} else if (this.pay_type == PAY_TYPE.ALI) {
				providerStr = 'alipay'
			}
			uni.hideLoading()
			// #ifdef H5
			this._h5_pay(paymentData)
			// #endif
			// #ifdef MP-WEIXIN
			this._wechat_mini_pay(paymentData)
			// #endif
			// #ifdef APP-PLUS 
			this._app_pay(providerStr, paymentData)
			// #endif
			// #ifdef MP-ALIPAY
			this._ali_mini_pay(paymentData)
			// #endif
			// #ifdef MP-TOUTIAO
			this._tt_pay(paymentData)
			// #endif
			// #ifdef MP-BAIDU
			this._baidu_pay(paymentData)
			// #endif
		},
		/**
		 * 取得调用支付的所需订单信息
		 * @param pay_type
		 * @returns {Promise<*>}
		 */
		async getPaymentData() {
			let data = {
				api: 'app.pay.index',
				total: this.total,
				remarks: this.remarks,
				sNo: this.sNo,
				title: this.title,
				order_list: this.order_pay_info ? this.order_pay_info : ''
			}
			let {
				type,
				code,
				store_type
			} = await this.getOrderInfoExt()
			data.type = type
			data.payType = type
			data.code = code
			data.openid = uni.getStorageSync('openid') || ''
			// // 54430code只能用一次
			// this.code=''
			let auth_code = await this.LaiKeTuiCommon.getMPAliAuthCode()
			if (auth_code) {
				// #ifdef MP-ALIPAY
				data.alimp_authcode = auth_code
				// #endif
				// #ifdef MP-TOUTIAO
				data.tt_authcode = auth_code
				// #endif
			}

			if (this.preSale) {
				if (uni.getStorageSync('payTarget')) {
					data.payTarget = uni.getStorageSync('payTarget')
				} else {
					if (this.sellMap.sellType == 1) {
						data.payTarget = 1
					} else {
						data.payTarget = 3
					}
				}
			}
			// VIP续约 使用贝宝充值 时 缓存订单信息
			if (type == 'paypal') {
				uni.setStorageSync('payRes', data.order_list)
			}
            if (type == 'stripe') {
                uni.setStorageSync('stripe', data.order_list)
            }
			return await this.$req.post({
				data
			})
		},
		_baidu_pay(paymentData) {
			swan.requestPolymerPayment({
				orderInfo: {
					'dealId': paymentData.dealId, // 后台配置
					'appKey': paymentData.appKey, // 后台配置
					'totalAmount': paymentData.totalAmount, // 订单金额
					'tpOrderId': paymentData.tpOrderId, // 商户平台自己记录的订单ID
					'dealTitle': paymentData.dealTitle, // 订单的名称
					'signFieldsRange': paymentData.signFieldsRange, // 固定值1
					'rsaSign': paymentData.rsaSign, // 对appKey+dealId+tpOrderId+totalAmount进行RSA加密后的签名，防止订单被伪造
					'bizInfo': paymentData.bizInfo // 订单详细信息
				},
				success: () => {
					uni.showToast({
						title: this.language.laikepay.paySuccess,
						duration: 1000,
						icon: 'none',
					})
					setTimeout(() => {
						uni.setStorageSync('payRes', this.order_pay_info)
						//第三方支付，不同插件跳转页面不同
						if (this.type == 'PT') {
							//拼团跳转页面支付结果页面
							let url3 = '/pagesA/group/group_end?sNo=' + this.sNo + '&order_id=' +
								this.order_id + '&returnR=1&cc=4' + '&openId=' + this.product.openId
							if (this.a_type == 1) {
								url3 += '&a_type=1'
							}
							if (this.isTeamLimit) {
								url3 += '&otype=1'
							}
							uni.redirectTo({
								url: url3
							})
						} else if (this.isBond) {
							uni.redirectTo({
								url: '/pagesC/bond/success?str=true'
							})
						} else {
							uni.redirectTo({
								url: '/pagesE/pay/PayResults'
							})
						}
						this.pay_display = false
					}, 1000)
				},
				fail: () => {
					this._pay_fail()
				}
			})
		},
		/**
		 * 头条支付
		 * @param paymentData
		 * @private
		 */
		_tt_pay(paymentData) {
			tt.requestPayment({
				data: paymentData,
				success: () => {
					uni.showToast({
						title: this.language.laikepay.paySuccess,
						duration: 1000,
						icon: 'none',
					})
					setTimeout(() => {
						uni.setStorageSync('payRes', this.order_pay_info)
						//第三方支付，不同插件跳转页面不同
						if (this.type == 'PT') {
							//拼团跳转页面支付结果页面
							let url3 = '/pagesA/group/group_end?sNo=' + this.sNo + '&order_id=' +
								this.order_id + '&returnR=1&cc=4' + '&openId=' + this.product.openId
							if (this.a_type == 1) {
								url3 += '&a_type=1'
							}
							if (this.isTeamLimit) {
								url3 += '&otype=1'
							}
							uni.redirectTo({
								url: url3
							})
						} else if (this.isBond) {
							uni.redirectTo({
								url: '/pagesC/bond/success?str=true'
							})
						} else {
							uni.redirectTo({
								url: '/pagesE/pay/PayResults'
							})
						}
						this.pay_display = false
					}, 1000)
				},
				fail: () => {
					this._pay_fail()
				}
			})
		},
		/**
		 * 阿里小程序支付
		 * @param paymentData
		 * @private
		 */
		_ali_mini_pay(paymentData) {
			my.tradePay({
				tradeNO: paymentData,
				success: (res) => {
					if (res.resultCode === 9000) {
						uni.showToast({
							title: this.language.laikepay.paySuccess,
							icon: 'none'
						})
						setTimeout(() => {
							uni.setStorageSync('payRes', this.order_pay_info)
							//第三方支付，不同插件跳转页面不同
							if (this.type == 'PT') {
								//拼团跳转页面支付结果页面
								let url3 = '/pagesA/group/group_end?sNo=' + this.sNo +
									'&order_id=' + this.order_id + '&returnR=1&cc=4' + '&openId=' +
									this.product.openId
								if (this.a_type == 1) {
									url3 += '&a_type=1'
								}
								if (this.isTeamLimit) {
									url3 += '&otype=1'
								}
								uni.redirectTo({
									url: url3
								})
							} else if (this.isBond) {
								uni.redirectTo({
									url: '/pagesC/bond/success?str=true'
								})
							} else {
								uni.redirectTo({
									url: '/pagesE/pay/PayResults'
								})
							}
							this.pay_display = false
						}, 1000)
					} else if (res.resultCode === 6001) {
						setTimeout(() => {
							this._pay_fail()
						}, 1000)
					}
				},
				fail: (res) => {
					uni.showToast({
						title: res.memo,
						icon: 'none'
					})
					setTimeout(() => {
						this._pay_fail()
					}, 1000)
				},
			})
		},
		/**
		 * app 支付
		 * @param providerStr
		 * @param paymentData
		 * @private
		 */
		_app_pay(providerStr, paymentData) {
			uni.requestPayment({
				provider: providerStr,
				orderInfo: paymentData, //订单数据
				success: (res) => {
					console.log(res, 'app_pay')
					uni.showToast({
						title: this.language.laikepay.paySuccess,
						duration: 1000,
						icon: 'none',
					})
					setTimeout(() => {
						uni.setStorageSync('payRes', this.order_pay_info)
						//第三方支付，不同插件跳转页面不同
						if (this.type == 'PT') {
							//拼团跳转页面支付结果页面
							let url3 = '/pagesA/group/group_end?sNo=' + this.sNo + '&order_id=' +
								this.order_id + '&returnR=1&cc=4' + '&openId=' + this.product.openId
							if (this.a_type == 1) {
								url3 += '&a_type=1'
							}
							if (this.isTeamLimit) {
								url3 += '&otype=1'
							}
							uni.redirectTo({
								url: url3
							})
						} else if (this.isBond) {
							uni.redirectTo({
								url: '/pagesC/bond/success?str=true'
							})
						} else {
							uni.redirectTo({
								url: '/pagesE/pay/PayResults'
							})
						}
						this.pay_display = false
					}, 1000)
				},
				fail: (err) => {
					console.log(err)
					this._pay_fail()
				},
				complete: () => {
					this.loading = false
				}
			})
		},
		/**
		 * 小程序支付
		 * @param paymentData
		 * @private
		 */
		_wechat_mini_pay(paymentData) {
			console.log('小程序支付', paymentData);
			this.loading = true
			uni.requestPayment({
				timeStamp: paymentData.timeStamp,
				nonceStr: paymentData.nonceStr,
				package: paymentData.package,
				signType: paymentData.signType,
				paySign: paymentData.paySign,
				success: () => {
					uni.showToast({
						title: this.language.laikepay.paySuccess,
						duration: 1000,
						icon: 'none'
					})
					console.log('微信支付252');
					setTimeout(() => {
						console.log('微信支付254--跳转');
						uni.setStorageSync('payRes', this.order_pay_info)
						//第三方支付，不同插件跳转页面不同
						if (this.type == 'PT') {
							let group_info = uni.getStorageSync("group_info");
							//拼团跳转页面支付结果页面  
							let url3 = url3 = '/pagesA/group/groupDetailed?pro_id=' + group_info
								.goodsId + '&goodsId=' + group_info.goodsId + '&acId=' + group_info
								.acId;
							if (this.a_type == 1) {
								url3 += '&a_type=1'
							}
							if (this.isTeamLimit) {
								url3 += '&otype=1'
							}
							console.log("url3=======》" + url3);
							uni.redirectTo({
								url: url3
							})
						} else if (this.isBond) {
							uni.redirectTo({
								url: '/pagesC/bond/success?str=true'
							})
						} else {
							uni.redirectTo({
								url: '/pagesE/pay/PayResults'
							})
						}
						console.log('微信支付--跳转');
					}, 300)
				},
				fail: (err) => {
					console.log(err)
					this._pay_fail()
				},
				complete: () => {
					this.loading = false
				}
			})
		},
		/**
		 * h5 支付（支付宝/微信支付---》非余额支付）
		 * @param paymentData
		 * @private
		 */
		_h5_pay(paymentData) {
			console.log("进入H5支付--》开始参数", paymentData);
            uni.setStorageSync('payRes', this.order_pay_info)
			if (this.pay_type == PAY_TYPE.ALI) {
				console.log("进入H5支付--》阿里支付--》支付类型", this.pay_type);
				const div = document.createElement('div');
				div.innerHTML = paymentData;
				document.body.appendChild(div);
				// 保存表单引用
				const form = document.forms[0];

				// 监听表单提交事件
				form.addEventListener('submit', () => {
					// 在表单提交时移除 div 元素
					div.remove();
				});
				form.submit();
			} else if (this.pay_type == PAY_TYPE.PAYPAL) {
				// 跳转 paypal 支付页面
				window.location.href = paymentData.data;
			} else if (this.pay_type == PAY_TYPE.OFFLINE_PAYMENT) {
                uni.navigateTo({
                    url: '/pagesB/order/myOrder?status=' + 1
                });
			} else if (this.pay_type == PAY_TYPE.STRIPE) {
				// 跳转 stripe 支付页面
                const stripe_id = paymentData.stripe_id
                const publishable_key = paymentData.publishable_key
                
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
			}else {
				//普通浏览器 选择微信支付
				if (paymentData && paymentData.pay_type && paymentData.pay_type == 'H5_wechat') {
					console.log("进入H5支付--》普通浏览器微信支付--》跳转URL", paymentData.url);
					window.location.href = paymentData.url;
					return;
				}
				//微信公众号 选择微信支付（微信聊天中打开的链接）
				let me = this

				function onBridgeReady() {
					console.log("进入H5支付--》微信公众号微信支付--》支付code", me.code);
					WeixinJSBridge.invoke(
						'getBrandWCPayRequest', {
							'appId': paymentData.appid, //公众号名称，由商户传入     
							'timeStamp': paymentData.timeStamp, //时间戳，自1970年以来的秒数     
							'nonceStr': paymentData.nonceStr, //随机串     
							'package': paymentData.package,
							'signType': paymentData.signType, //微信签名方式：     
							'paySign': paymentData.paySign //微信签名 
						},
						function(res) {
							console.log("进入H5支付--》微信公众号微信支付--》初始化code");
							//初始化code（此处不管支付成功/失败，必须跳转出去。code只能用一次，必须重新进入才可以获取新的code）
							// uni.setStorageSync('pay_code',this.code)
							// me.code = ''
							//是否支付成功
							if (res.err_msg === 'get_brand_wcpay_request:ok') {
								console.log("进入H5支付--》微信公众号微信支付--》支付成功", res);
								//存支付信息 用于支付结果页面
								uni.setStorageSync('payRes', me.order_pay_info)
								if (this.isBond) {
									window.location.href = uni.getStorageSync('h5_url') +
										'pagesC/bond/success?str=true'
									return
								}
								let type = ''
								if (me.type == 'PT') {
									type = '?type=PT'
								}
								//支付成功 跳转支付结果页面
								window.location.href = uni.getStorageSync('h5_url') + 'pagesE/pay/PayResults' + type
							} else {
								console.log("进入H5支付--》微信公众号微信支付--》支付失败", res);
								//支付失败  跳转支付结果页面
								me._pay_fail()
							}
						}
					)
				}
				//微信公众号 选择微信支付判断
				if (this.LaiKeTuiCommon.IS_SHARE_WECHAT_H5PAY) {
					//开启了 小程序内嵌H5（因要适配支付，所以微信公众号支付 -》小程序支付）
					//直接跳转当前小程序 进行小程序支付（支付逻辑在小程序进行）
					//pages/wxpay/payment/payment?timeStamp=&nonceStr=&package=&paySign=&total=&sNo=&orderTime=
					var jweixin = require('jweixin-module')
					console.log('weixin-js-sdk~~~', jweixin);
					let payRes = JSON.parse(uni.getStorageSync("payRes"))
					console.log('payRes~~~', payRes);
					let isType = 'gm' //普通商品支付：gm 
					let total_score = payRes.total_score || '' //积分商品支付
					let total = payRes.total
					let sNo = payRes.sNo
					let orderTime = payRes.orderTime
					let urls = '/pages/wxpay/payment/payment' +
						'?timeStamp=' + paymentData.timeStamp +
						'&nonceStr=' + paymentData.nonceStr +
						'&package=' + paymentData.package.slice(10) +
						'&paySign=' + paymentData.paySign +
						'&isType=' + isType +
						'&total_score=' + total_score +
						'&total=' + total +
						'&sNo=' + sNo +
						'&orderTime=' + orderTime
					console.log('urls~~~', urls);
					//跳转至目标小程序进行支付操作
					jweixin.miniProgram.navigateTo({
						url: urls,
						success(success) {
							console.log('跳转成功', success)
						},
						fail(fail) {
							console.log('跳转失败', fail)
						}
					})
				} else if (typeof WeixinJSBridge !== 'undefined') {
					//（微信公众中存在WeixinJSBridge）直接调用支付方法 
					onBridgeReady(paymentData)
				} else {
					//
					if (typeof WeixinJSBridge === 'undefined') {
						if (document.addEventListener) {
							document.addEventListener('WeixinJSBridgeReady', onBridgeReady, false)
						} else if (document.attachEvent) {
							document.attachEvent('WeixinJSBridgeReady', onBridgeReady)
							document.attachEvent('onWeixinJSBridgeReady', onBridgeReady)
						}
					}
				}
			}

		},
		async getOrderInfoExt() {
			let data = {
				type: '',
				code: '',
				store_type: 1
			}
			// #ifdef H5
			if (this.pay_type == PAY_TYPE.WX) {
				data.type = 'H5_wechat'
				let iswxbrowser = this.is_wx();
				console.log("iswxbrowser" + iswxbrowser);
				if (iswxbrowser) {
					data.type = 'jsapi_wechat'
					//开启了 小程序内嵌H5（因要适配支付，所以微信公众号支付 -》小程序支付）
					if (this.LaiKeTuiCommon.IS_SHARE_WECHAT_H5PAY) {
						data.type = 'mini_wechat'
					}
				}
				console.log("支付类型：" + data.type);
				data.code = this.code
			} else if (this.pay_type == PAY_TYPE.ALI) {
				data.type = 'alipay_mobile'
			}
			// #endif

			// #ifndef H5
			if (this.pay_type == PAY_TYPE.WX) {

				// #ifdef MP-WEIXIN

				return new Promise(resolve => {
					uni.login({
						success: (e) => {
							data.type = 'mini_wechat'
							data.code = e.code
							return resolve(data)
						}
					})
				})
				// #endif   

				// #ifdef APP
				data.type = 'app_wechat'
				data.pay_type = 'app_wechat'
				// #endif    

			} else if (this.pay_type == PAY_TYPE.ALI) {
				data.type = 'alipay'
				data.pay_type = 'alipay'
				// #ifdef MP-ALIPAY
				data.type = 'alipay_minipay'
				data.pay_type = 'alipay_minipay'
				// #endif
				// #ifdef MP-TOUTIAO
				data.type = 'tt_alipay'
				data.pay_type = 'tt_alipay'
				// #endif
			} else if (this.pay_type == PAY_TYPE.BAIDU) {
				// #ifdef MP-BAIDU
				data.type = 'baidu_pay'
				data.pay_type = 'baidu_pay'
				// #endif
			}
			// #endif 

			if (this.pay_type == PAY_TYPE.PAYPAL) {
				data.type = 'paypal'
				data.pay_type = 'paypal'
			}

            if (this.pay_type == PAY_TYPE.STRIPE) {
				data.type = 'stripe'
				data.pay_type = 'stripe'
			}
            console.log("支付类型：" + this.pay_type );
            if (this.pay_type == PAY_TYPE.OFFLINE_PAYMENT) {
				data.type = 'offline_payment'
				data.pay_type = 'offline_payment'
			}

			return data
		},
        //检查支付方式是否可用    
        isPayTypeAvailable(payType){
            switch (payType) {
                case 'wxPay': return this.open_pay.open_wxpay
                case 'aliPay': return this.open_pay.open_alipay
                case 'paypal': return this.open_pay.open_paypal
                case 'stripe': return this.open_pay.open_stripe
                case 'offline_payment': return this.open_pay.offline_payment
                case 'balance': return this.open_pay.open_wallet
                case 'baiduPay': return this.open_pay.open_baidu
                default: return false
            }
        },
		/**
		 * 显示能支付方式
		 */
		show_pay_way(defaultpayment) {

            let defaultPaytype = ''
            if (defaultpayment) {
                defaultPaytype = defaultpayment.defaultpayName;
            }
            
            let storeCurrency = uni.getStorageSync('currency') 
            if (Number.parseInt(this.payment.offline_payment)) {
                this.open_pay.offline_payment = true
                this.useWallet = true
            }            
            if(storeCurrency.currency_code == 'CNY')
            {
                // #ifdef H5
                this.open_pay.open_paypal = false; 
                this.open_pay.open_stripe = false; 
                if (this.is_wx()) {
                    if (Number.parseInt(this.payment.jsapi_wechat)) {
                        this.open_pay.open_wxpay = true
                        this.chooseWay('wxPay')
                    }
                } else {
                    //非微信浏览器
                    if (Number.parseInt(this.payment.H5_wechat)) {
                        this.open_pay.open_wxpay = true
                        this.chooseWay('wxPay')
                    }

                    if (Number.parseInt(this.payment.alipay_mobile)) {
                        this.open_pay.open_alipay = true
                        this.chooseWay('aliPay')
                    } 
                    
                    //ToDo 个人选择的默认币种是人民币的情况下，目前没法使用贝宝支付
                    let currency = uni.getStorageSync('currency')
                    if(currency.currency_code != 'CNY'){
                        if (Number.parseInt(this.payment.paypal)) {
                            this.chooseWay('paypal')
                            this.open_pay.open_paypal = true
                        }
                        if (Number.parseInt(this.payment.stripe)) {
                            this.chooseWay('stripe')
                            this.open_pay.open_stripe = true
                        }
                    }
                    if (Number.parseInt(this.payment.offline_payment)) {
                        this.open_pay.offline_payment = true
                        this.chooseWay('offline_payment')
                    }
                    
                }
                // #endif
                
                // #ifdef MP-WEIXIN
                if (Number.parseInt(this.payment.mini_wechat)) {
                    this.open_pay.open_wxpay = true
                    this.chooseWay('wxPay')
                }
                // #endif

                // #ifdef APP-PLUS
                if (Number.parseInt(this.payment.alipay)) {
                    this.open_pay.open_alipay = true
                    this.chooseWay('aliPay')
                }
                if (Number.parseInt(this.payment.app_wechat)) {
                    this.open_pay.open_wxpay = true
                    this.chooseWay('wxPay')
                }

                if (Number.parseInt(this.payment.baidu_pay)) {
                    this.open_pay.open_baidu = true
                }
                // #endif

                // #ifdef MP-BAIDU
                if (Number.parseInt(this.payment.baidu_pay)) {
                    this.open_pay.open_baidu = true
                }
                // #endif

                // #ifdef MP-ALIPAY
                if (Number.parseInt(this.payment.alipay_minipay)) {
                    this.open_pay.open_alipay = true
                    this.chooseWay('aliPay')
                }
                // #endif

                // #ifdef MP-TOUTIAO
                if (Number.parseInt(this.payment.tt_alipay)) {
                    this.open_pay.open_alipay = true
                    this.chooseWay('aliPay')
                }
                // #endif

                //钱包在最后执行 所以默认就是选择钱包支付
                if (Number.parseInt(this.payment.wallet_pay)) {
                    this.chooseWay('balance')
                    this.open_pay.open_wallet = true
                    this.useWallet = true
                }
            }
            else
            {

                //钱包在最后执行 所以默认就是选择钱包支付
                if (Number.parseInt(this.payment.wallet_pay)) {
                    this.chooseWay('balance')
                    this.open_pay.open_wallet = true
                    this.useWallet = true
                }
                
                if (Number.parseInt(this.payment.paypal)) {
                    this.chooseWay('paypal')
                    this.open_pay.open_paypal = true
                }

                if (Number.parseInt(this.payment.stripe)) {
                    this.chooseWay('stripe')
                    this.open_pay.open_stripe = true
                }
                
                //隐藏其他的支付方式
                this.open_pay.open_alipay = false
                this.open_pay.open_wxpay = false
                
            }
            //原逻辑不变如果设置默认支付方式则选中默认支付方式
            if (defaultpayment) {
                this.chooseWay(defaultPaytype)
            }

            if (!this.isPayTypeAvailable(defaultPaytype)) {
                if (Number.parseInt(this.payment.wallet_pay)) {
                    this.chooseWay('balance')
                    this.open_pay.open_wallet = true
                    this.useWallet = true
                }
            }
            
            
		},
		/**
		 * 选择支付方式
		 * @param way
		 */
		chooseWay(way) {
			switch (way) {
				case 'wxPay':
					// this._chooseWxPayWay()
					this.pay_way = 'wxPayStatue'
					break
				case 'aliPay':
					// this._chooseAlipayPayWay()
					this.pay_way = 'aliPayStatue'
					break
				case 'baidu':
					// this._chooseBaiduPayWay()
					this.pay_way = 'baidupayStatue'
					break
				case 'balance':
					// this._chooseBalancePayWay()
					this.pay_way = 'useWallet'
					this.oderPayType = "wallet_pay";
					break
				case 'paypal':
					this.pay_way = 'paypal'
					this.oderPayType = "paypal";
					break
                case 'stripe':
					this.pay_way = 'stripe'
					this.oderPayType = "stripe";
					break
                case 'offline_payment':
					this.pay_way = 'offline_payment'
					this.oderPayType = "offline_payment";
					break
				default:
					break
			}
			console.log("支付方式==>" + this.pay_way);
			//功能优化 bugID52195（确认订单页面使用）
			if (this.oderPayType || this.oderPayType === '') {
				this.oderPayType = way == 'balance' ? 'wallet_pay' : ''
			}
		},

		/**
		 * 支付失败
		 * @private
		 */
		_pay_fail() {
			console.log(this.isBond, 'this.isBondthis.isBondthis.isBondthis.isBondthis.isBond')
			if (this.isBond) {
				uni.redirectTo({
					url: '/pagesA/myStore/myStore'
				})
				return
			}
			let type = ''
			if (this.type == 'PT') {
				type = '&type=PT'
			}
			if (this.type == 'IN') {
				type = '&type=IN'
			}
			if (this.type == 'MS') {
				type = '&type=MS'
			}
			if (this.type == 'XQ') {
				type = '&type=GM'
			}
			this.$store.commit('SET_PAYFLAG', true)
			uni.redirectTo({
				//新的支付页面
				url: '/pagesE/pay/PayResults?i_state=false' + type
			})
		},
		_pay_fail_pt() {
			uni.showModal({
				title: this.language.laikepay.hint,
				content: this.language.laikepay.payFail2,
				confirmText: this.language.showModal.confirm,
				cancelText: this.language.showModal.cancel,
				success: res => {
					this.fastTap = false
					if (res.confirm) {
						this.status(1);
						//支付失败弹窗确认按钮
						// uni.redirectTo({
						//     url:'/pagesA/myStore/MyBidding/MyBidding?typeindex=3'//(跳到竞拍页(已拍下状态))
						// })
					} else if (res.cancel) {
						// uni.switchTab({
						//     url: '/pages/shell/shell?pageType=my'
						// })
					}
				}
			})
		},
		/**
		 * 余额支付时取消支付密码
		 */
		wallet_pay_cancel() {
			if (this.fastTap) return false
			this.pay_display = false
			this.pay_password = ''
			if (this.bondStatus) {
				return
			}
			this._pay_fail()
		},
		wallet_pay_cancel_pt() {
			if (this.fastTap) return false
			this.pay_display = false
			this.pay_password = ''
			this._pay_fail_pt()
		},
		/**
		 * 验证支付密码是否正确
		 * @param pay_password
		 * @returns {boolean}
		 */
		// check_pay_password 这个方法 是发请求先验证输入的密码是否正确，正确后调用支付接口 例如 _wallet_pay
		check_pay_password(pay_password) {
			if (!this.enterless) {
				uni.showToast({
					title: this.language.laikepay.mimaError,
					icon: 'none',
				})
				return false
			}
			this.pay_password = pay_password
			//发送请求，验证支付密码是否正确
			let data = {
				api: 'app.user.payment_password',
				password: pay_password
			}
			this.$req.post({
				data
			}).then(async res => {
				let {
					code,
					data: {
						enterless
					},
					message
				} = res
				//验证密码正确
				if (code == 200) {
					uni.removeStorageSync('address_ziti')
					this.pay_display = false
					uni.showLoading({
						title: this.language.showLoading.paying,
						mask: true,
					})
					if (!this.bondStatus) {
						//调用钱包支付_wallet_pay
						await this._wallet_pay()
					} else {
						await this.bond_pay() //保证金支付接口方法
					}
					uni.hideLoading()
				} else if (enterless) {
					//验证密码不正确
					this.pay_password = ''
					uni.hideLoading()
					uni.showToast({
						title: message == '已被锁定,请明天再试' ? message : this.language.laikepay.pswdFail,
						duration: 1000,
						icon: 'none'
					})
					this.enterless = enterless
				} else if (!enterless) {
					//验证密码不正确
					uni.hideLoading()
					this.enterless = false
				}
			})
		},
		/**
		 * 验证订单是否关闭
		 * @returns {Promise<void>}
		 * @private
		 */
		async _check_order_status() {
			let postData = {
				api: 'app.order.order_details', 
				order_id: this.order_id
			}
			if (this.pay_way == 'useWallet') {
				postData.pay = 'true'
			}
			if (this.ordertype == 'pt') {
				postData.action = 'groupbuy'
			}
			if (this.ordertype == 'PP') {
				postData.action = 'pthd_groupbuy'
			}
			let {
				code,
				data,
				message
			} = await this.$req.post({
				data: postData
			})

			if (code != 200) {
				uni.showToast({
					title: message,
					icon: 'error'
				})
				return
			}
			let {
				status,
				add_time,
				sNo,
				id,
				sellInfo,
				r_status,
				z_price
			} = data

            if(data.otype == 'IN'&& data.allow > data.user_score){
                uni.showToast({
                    icon:'none',
                    title:'您的积分不足~'
                })
                return
            } 
            if (this.pay_way == 'useWallet') {
				// this.checkIsPay(sNo)
				// 弹起软键盘
				this.pay_display = true
			}
			const data1 = {
				'orderTime': add_time,
				'sNo': sNo,
				'order_id': id,
				// 不知道为什么要取sellInfo 预售订单中的 total金额
				'total': sellInfo.total || z_price
			}
			uni.setStorageSync('payRes', JSON.stringify(data1))
			if (r_status != 0) {
				uni.showToast({
					title: '订单状态异常',
					icon: 'none'
				})
				setTimeout(() => {
					uni.navigateBack({
						delta: 1
					})
				}, 1000)
				return Promise.reject('订单已关闭，支付失败')
			}
			status = Number.parseInt(status)
			if (status == 7 || status == 6) {
				return Promise.reject('订单已关闭，支付失败')
			}
			return true
		},
		async checkIsPay(sNo) {
			const postData = {
				api: 'app.order.checkIsPay',
				sNo: sNo
			}
			let {
				code,
				data,
				message
			} = await this.$req.post({
				data: postData
			})
			if (code != 200) {
				uni.showToast({
					title: message,
					icon: 'none'
				})
			} else {
				// 弹起软键盘
				this.pay_display = true
			}
		},
		/**
		 * 余额支付
		 * @returns {Promise<void>}
		 * @private
		 */
		async _wallet_pay() {
			console.log("进入余额支付--》开始");
			//发请求，钱包支付
			let postData = {
				api: 'app.pay.wallet_pay',
				type: 'wallet_pay',
				address_id: this.address_id ? this.address_id : this.addre_id,
				order_list: this.order_pay_info,
				sNo: this.sNo,
				remarks: this.remarks,
				payment_money: this.total,
				pay_type: 'wallet_pay',
				parameter: this.parameter ? JSON.stringify(this.parameter) : ''
			}
			// 限时抢购
			if (this.payType === 'XSJG') {
				postData.payment_money = this.countPrice == 0 ? this.total : this.countPrice
			}
			console.log(888)
			//判断赋值postData
			if (this.bargain) {
				console.log(1)
				postData = this.getBargainWalletPayData(postData)
			} else if (this.seckill) {
				console.log(2)
				postData = this.getSeckillWalletPayData(postData)
			} else if (this.integral) {
				console.log(3)
				postData = this.getIntegralWalletPayData(postData)
			} else if (this.pay_name === 'jp') {
				console.log(4)
				postData = this.getJPWalletPayData(postData)
			}
			//判断赋值postData
			if (!this.is_express) {
				postData.shop_address_id = this.shop_address_id
			}
			if (this.preSale) {
				if (uni.getStorageSync('payTarget')) {
					postData.payTarget = uni.getStorageSync('payTarget')
				} else {
					if (this.sellMap.sellType == 1) {
						postData.payTarget = 1
					} else {
						postData.payTarget = 3
					}
				}
			}
			//开始请求
			let {
				code,
				message
			} = await this.$req.post({
				data: postData
			})
			uni.hideLoading()
			console.log(code)
			//返回 0
			if (code == 0) {
				uni.showToast({
					title: message,
					duration: 1000,
					icon: 'none'
				})
				return false;
			}
			//钱包支付成功
			if (code == 200) {
				uni.showToast({
					title: this.language.laikepay.paySuccess,
					duration: 1000,
					icon: 'none'
				})
				uni.setStorageSync('tiaozhuan_url', this.PayMargin_url)
			} else {
				//钱包支付失败
				uni.showToast({
					title: '支付失败',
					duration: 1000,
					icon: 'none'
				})
			}
			await later(1000)
			uni.setStorageSync('payRes', this.order_pay_info)
			var payType = uni.getStorageSync('payType')
			if (code == 200) {
				//这里用redirectTo 关闭里当前页面，成功后怎么返回当前页呢？
				if (this.integral) {
					uni.redirectTo({
						url: '/pagesE/pay/PayResults'
					})
				} else if (this.is_ms) {

					uni.redirectTo({
						url: '/pagesE/pay/PayResults'
					})
				} else if (this.isBond) {

					uni.redirectTo({
						url: '/pagesC/bond/success?str=true'
					})
				} else {
					uni.redirectTo({
						//新的支付页面
						url: '/pagesE/pay/PayResults'
					})
				}
				// uni.redirectTo({
				//     url: '/pagesE/pay/payResult'
				// })
			}
			// if(payType != 1){
			// uni.redirectTo({
			//     url: '/pagesE/pay/payResult'
			// })
			// }
		},
		/**
		 * 取得支付时的支付类型和终端类型
		 * @returns {{pay_type: string, store_type: number}}
		 */
		getPayTypeAndStoreType() {
			let data = {
				pay_type: 'wallet_pay',
			}
			if (this.pay_way == 'wxPayStatue') {
				// #ifdef MP-WEIXIN
				data.pay_type = 'mini_wechat'
				// #endif
				//#ifdef APP-PLUS
				data.pay_type = 'app_wechat'
				// #endif
				//#ifdef H5
				data.pay_type = 'H5_wechat'
				let iswxbrowser = this.is_wx();
				console.log("iswxbrowser" + iswxbrowser);
				if (iswxbrowser) {
					data.pay_type = 'jsapi_wechat'
				}
				console.log("支付类型：" + data.pay_type);
				// #endif
			} else if (this.pay_way == 'aliPayStatue') {
				// #ifdef MP-ALIPAY
				data.pay_type = 'alipay_minipay'
				// #endif
				//#ifdef APP-PLUS
				data.pay_type = 'alipay'
				// #endif
				//#ifdef MP-TOUTIAO
				data.pay_type = 'tt_alipay'
				this.tt_alipay_app = true
				// #endif
				// #ifdef H5
				data.pay_type = 'alipay_mobile'
				// #endif
			} else if (this.pay_way == 'baidupayStatue') {
				// #ifdef MP-BAIDU
				data.pay_type = 'baidu_pay'
				// #endif
			} else if (this.pay_way == 'paypal') {
				// #ifndef MP-WEIXIN
				// 除了微信小程序之外
				data.pay_type = 'paypal'
				// #endif
			}else if (this.pay_way == 'stripe') {
                // #ifndef MP-WEIXIN
                // 除了微信小程序之外
                data.pay_type = 'stripe'
                // #endif
            }else if (this.pay_way == 'offline_payment') {
                data.pay_type = 'offline_payment'
            }
			return data
		},
	},
	watch: {
		enterless(val) {
			if (val) {
				this.fastTap = true
				this.pay_display = false
				uni.showToast({
					title: this.language.laikepay.maxFail,
					duration: 1000,
					icon: 'none'
				})
			}
		}
	}
}
