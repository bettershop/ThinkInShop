import pay from './pay'
import bargain from './bargain'
import seckill from './seckill'


export default {
	mixins: [pay, bargain, seckill],
	data() {
		return {
			// 支付方式
			// 订单信息
			total: 0, // 订单总价, 计算方式， （商品总价 + 满减优惠 + 优惠券金额 ） * 分销折扣 * 等级折扣  + 运费
			products_total: 0, // 产品总价
			freight: 0, // 订单运费
			freightt: '0', // 订单运费显示的价格，是字符串类型
			coupon_money: 0, //优惠券金额
			reduce_money: 0, // 砍价金额
			discount: 1, // 分享折扣
			grade_rate: 1, // 会员等级折扣
			sNo: '', //订单编号

			// 用户相关信息
			user_money: 0, // 用户余额

			fastTap: false, // 防止重复点击

			buy_again: false, //是否为再次购买 true 是  false 否

			order_pay_info: null, // 订单json格式化后的订单信息 order_id, sNo,total

			order_type: 'type_',

			pay_name: '', // pt 普通商品,jp 竞拍商品

			// 地址
			is_express: 1, // 地址是否快递
			shop_status: false,
			shop_address_id: '',
			shop_list: {
				address: '',
				id: 0,
				mobile: '',
				name: ''
			},
			address_id: '',
			hc_address_id: '',
			address: {
				name: ''
			},
			adds_f: false, //地址状态
			remarks: '',
			pwd: '',

			a_type: '', //是否平台活动管理
            
            orderType:'',//订单类型 
		}
	},
	computed: {
		shop_id() {
			if (Array.isArray(this.pro) && this.pro.length > 0) {
				return this.pro[0].shop_id
			} else {
				return 0;
			}
		}
	},
	onUnload() {
		uni.removeStorageSync('goodsInfo')
		uni.removeStorageSync('bargain')
		// this.$store.state.cart_id = ''
		if (!this.bargain && this.pay_name !== 'jp') {
			if (this.sNo && this.order_id) {
				let order_list = JSON.stringify({
					total: Number.parseFloat(this.total),
					order_id: this.order_id,
					sNo: this.sNo
				})
				console.log(this.returnR);
				let data = {
					api: 'app.order.index',
					// app: 'leave_Settlement',
					// groupbuy,
					order_list: order_list,
					price: this.total,

				}
				// if (this.pay_way == "useWallet" && !this.$store.state.payflag) {
				// 	delete data.order_list
				// } else if (this.pay_way == "useWallet" && this.$store.state.payflag) {
				// 	data.pay = true
				// }
				if (this.pay_way == "useWallet") {
					data.pay = true
				}
				this.$store.commit('SET_PAYFLAG', false)
				if (this.a_type == 1 && this.returnR == 1) {
					data.action = 'pthd_groupbuy'
					data.m = 'goGroupLeaveSettlement'
				} else {
					//data.app = 'leave_Settlement'
				}
				//data.app = 'leave_Settlement'
				//孙浩说去掉这个拆单接口。如果其他地方有影响和他沟通逻辑

				//离开界面的时候去生成订单
				this.$req.post({
					data
				})

				if (this.buy_again) {
					let data = {
						api: 'app.order.del_cart',
						cart_id: this.cart_id
					}
					this.$req.post({
						data
					})
				}
			}
		}
		this.$store.state.order_id = ''
	},
	destroyed() {
		if (uni.getStorageSync('chooseAddress')) {
			uni.removeStorageSync('chooseAddress')
		}
	},
	watch: {
		address_id(n, o) {
			if (o) {
				this._axios('address')
			}
		}
	},
	methods: {
		getCode() {
			// console.log('---》当前路径：',window.location.href);
			// #ifdef H5 
			// H5打开微信支付
			let a = window.location.href
			console.log('---》获取路径中的code')
			if (a.split('?').length > 1) {
				let str = a.split('?')[1]
				let arr = str.split('&')
				for (let i in arr) {
					if (typeof arr[i] === 'string') {
						if (arr[i].substring(0, 4) === 'code') {
							str = arr[i].substring(5)
							this.code = str
						}
					}
				}
			}
			// 判断两次code是否一致
			console.log('判断code和上一次是否一样', uni.getStorageSync('pay_code'), this.code);

			if (uni.getStorageSync('pay_code') == this.code) {
				this.code = ""
			}
			// #endif
			console.log("---》code：", this.code);
		},
		checkCode() {
			// #ifdef H5
			if (
				this.code == "" &&
				!this.LaiKeTuiCommon.IS_SHARE_WECHAT_H5PAY
			) {
				if (!this.is_wx()) {
					console.log("不在微信浏览器里面不校验code");
					return false;
				}
				let storage = window.localStorage;
				storage.removeItem("bargain");
				storage.removeItem("bargain_id");
				storage.removeItem("order_no");
				storage.removeItem("product");
				uni.removeStorageSync("bargain");
				uni.removeStorageSync("commodity_type");
				// 50968
				this._toUrl();
			} else {
				this.commodity_type = uni.getStorageSync("commodity_type");
			}
			// #endif
		},
		is_wx() {
			let en = window?.navigator?.userAgent.toLowerCase();
			// 匹配en中是否含有MicroMessenger字符串
			return en?.match(/MicroMessenger/i) == "micromessenger";
		},

		/**
		 * 检测
		 * @private
		 */
		_checkSelectPayWay() {
			console.log("_checkSelectPayWay支付方式：" + this.pay_way)
			if (this.pay_way == '') {
				uni.showToast({
					title: this.language.toasts.order.choosePay,
					duration: 1000,
					icon: 'none'
				})
			}
			this.fastTap = false
		},
		/**
		 * 点击立即支付按钮触发
		 * @returns {Promise<boolean>}
		 */
		async order_pay() {
            console.log("order_pay");
			//快速点击立即支付按钮处理
			if (this.fastTap) return false;
			this.fastTap = true;

			// 必须选一种支付方式
			this._checkSelectPayWay();

			//虚拟订单判断 如果是需要预约的核销订单 预约时间不能为空 commodity_type 0：实物 1：虚拟商品
			if (this.commodity_type == 1 && this.address_status == 2) {
				if (this.storeHxTimeKeyID == '') {
					uni.showToast({
						title: '请选择预约时间',
						icon: 'none'
					})
					return
				}
			}
			//补丁：存一个cartPay参数，在确认订单中使用。用于解决小程序购物车取消支付时重复调用接口问题
			uni.setStorageSync("cartPay", {
				name: "点击了支付-》order_pay",
				value: true,
			});

			//这行代码必执行 !this.rightsArr 这个判断没有用
			// if (!this.rightsArr) {
			// this.commodity_type != 1 && !this.address_id && this.is_express之前if中的值（此判断会导致购买分销商品时出现bug33770,因为你删除最后一条地址返回到订单页后，this.address_id还是之前最后一条地址缓存id导致该bug出现）
			// 换成adds_f（是否有默认地址）以此来解决该问题。因为有地址就必有默认地址  

			if (this.sNo.indexOf("DJ") == -1) {
				if (
					this.commodity_type != 1 &&
					this.is_express &&
					!this.adds_f
				) {
					//未设置收货地址，未执行支付操作移除缓存cartPay参数
					uni.removeStorageSync("cartPay");
					return uni.showToast({
						title: this.language.toasts.order.setAdds,
						icon: "none",
					});
				}
				//如果是上门自提，用户姓名与电话不能为空(自提可以重新设置姓名与电话)
				console.log(
					"xxxxxxx",
					this.fullName,
					this.fullPhone,
					this.is_express
				);
				if (!this.is_express && (!this.fullName || !this.fullPhone)) {
					return uni.showToast({
						title: this.language.toasts.order.setAdds1,
						icon: "none",
					});
				}
			}

			// }

			//默认选择最优优惠卷
			if (this.couponList1 && this.couponList1.length) {
				let coupon_id = [];
				this.pro.filter((items, indexs) => {
					this.mchCouponIndex.filter((item, index) => {
						if (indexs == index) {
							if (item >= 0) {
								coupon_id.push(
									items.coupon_list[item].coupon_id
								);
							} else {
								coupon_id.push("0");
							}
						}
					});
				});
				if (this.couponIndex >= 0) {
					coupon_id.push(
						this.couponList1[this.couponIndex].coupon_id
					);
				} else {
					coupon_id.push("0");
				}
				coupon_id.push(
					this.couponList1[this.couponIndex].discount_type
				);
				this.coupon_id = coupon_id.join();
			}

			// 微信小程序支付和订阅消息处理
			// #ifdef MP-WEIXIN
			console.log("getWXTmplIds中返回tmplIds参数", this.tmplIds);
			wx.requestSubscribeMessage({
				tmplIds: this.tmplIds,
				success: (res) => {
					console.log("获取订阅消息授权success-->", res);
				},
				fail: (res) => {
					console.log("获取订阅消息授权fail-->", res);
				},
				complete: async () => {
					uni.showLoading({
						title: this.language.showLoading.waiting,
					});
					uni.hideLoading();
					if (this.pay_way == "useWallet") {
						if (this.password_status == 0 && !uni.getStorageSync('password_status')) {
							uni.showModal({
								title: this.language.showModal.hint,
								content: this.language.showModal.mima,
								confirmText: this.language.showModal.confirm,
								cancelText: this.language.showModal.cancel,
								showCancel: true,
								success: (resM) => {
									this.fastTap = false;
									if (resM.confirm) {
										uni.navigateTo({
											url: "/pagesB/setUp/payment",
										});
									}
								},
							});
							return false;
						}
						if (!this.sNo) {
							await this._getPayOrderInfo();
						}
						this._orderUseWalletPay();
					} else {
						uni.showLoading({
							title: this.language.showLoading.axiospaying,
							mask: true,
						});
						await this._getPayOrderInfo();
						await this._notUserWalletPay();
					}
				},
			});
			// #endif
            console.log(" 339-支付方式：", this.pay_way);
			// #ifndef MP-WEIXIN
			
			//用户余额
			if (this.pay_way == "useWallet") {
				// 判断是否设置了 支付密码
				if (this.password_status == 0 && !uni.getStorageSync('password_status')) {
					//未设置支付密码，未执行支付操作移除缓存cartPay参数
					uni.removeStorageSync("cartPay");
					uni.showModal({
						title: this.language.showModal.hint,
						content: this.language.showModal.mima,
						confirmText: this.language.showModal.confirm,
						cancelText: this.language.showModal.cancel,
						showCancel: true,
						success: (resM) => {
							this.fastTap = false;
							if (resM.confirm) {
								uni.navigateTo({
									url: "/pagesB/setUp/payment",
								});
							}
						},
					});
					return false;
				}
				console.log(this.sNo);
				if (!this.sNo) {
					await this._getPayOrderInfo();
				}
                // 如果是 余额零元购 则不弹起键盘
                if(this.total == 0){
                    this.showPay = true 
                    setTimeout(()=>{
                        this._wallet_pay()
                    },500)
                }
				this._orderUseWalletPay();
			} else {
				if (this.title != "会员支付") {
					await this._getPayOrderInfo();
				}
				await this._notUserWalletPay();
			}
			// #endif
            if (this.pay_way == "offline") {
              console.log(" 339-支付方式：", this.pay_way);
            }
		},
        refreshPageData() {
            this.$emit('refreshData'); // 触发事件
        },
		getStoreType() {
			// #ifdef APP-PLUS
			return 11;
			// #endif
			// #ifdef MP-ALIPAY
			return 3;
			// #endif
			// #ifdef MP-BAIDU
			return 5;
			// #endif
			// #ifdef MP-TOUTIAO
			return 4;
			// #endif
			// #ifdef MP-WEIXIN
			return 1;
			// #endif
			// #ifdef H5
			return 2;
			// #endif
		},

        /**
         * 取得请求支付所需的 pay_order_info【下单入口】
         * @returns {Promise<void>}
         * @private
         */
        async _getPayOrderInfo() {
            let remarks = this.remarks;
            console.log(111);
            if (typeof remarks != "string") {
                if (remarks && remarks != "" && remarks.length > 0) {
                    remarks = JSON.stringify(remarks);
                }
            }
            var vipsou = uni.getStorageSync("vipSource");
            console.log(this.coupon_id);
            if (this.title != "会员支付") {
                var postData = {
                    api:'app.order.payment',
                    cart_id: this.cart_id,
                    address_id: this.address_id,
                    coupon_id: this.coupon_id,
                    remarks: remarks,
                    vipSource: vipsou
                };
                // 普通订单 和 虚拟订单使用的积分抵用 且按钮打开时
                if (this.scoreDeductionValue && this.pointsFlag) {
                    postData.scoreDeduction = this.jfNumber //积分抵用
                    postData.scoreRatio = this.scoreRatio //积分比例
                } else if (this.scoreDeductionValue && !this.pointsFlag) {
                    postData.scoreDeduction = 0 //积分抵用
                    postData.scoreRatio = this.scoreRatio //积分比例
                }

			} else {
				var postData = {
					api: "plugin.member.AppMember.Payment",
					memberType: this.typeindex + 1,
					payType: this.type,
					couponId: this.couponId,
					amount: this.amount,
				};
			}
			postData.store_type = this.getStoreType();

			//配送方式
			postData.is_self_delivery = this.is_express
			//商家配送 配送时间
			if (this.deliveryTime && this.deliveryPeriod) {
				postData.deliveryTime = this.deliveryTime
				postData.deliveryPeriod = this.deliveryPeriod
			}

			if (this.commodity_type == 1) {
				postData.type = 'VI';
				if (this.address_status == 2) {
					postData.mchStoreWrite = this.storeHxTimeKeyID
					postData.mchStoreWriteTime = this.storeHxTimeKeyTime
					postData.shop_address_id = this.storeHx[0].id
				}
			}

			if (this.buy_again) {
				postData.buy_type = 1;
			}
			if (this.is_discount) {
				postData.type = "FS";
				postData.fs_id = this.fsid;
			}
			if (this.bargain) {
				postData = this.getBargainPayOrderInfo();
			} else if (this.seckill) {
				postData = this.getSeckillPayOrderInfo();
			} else if (this.integral) {
				postData = this.getIntegralPayOrderInfo();
			} else if (this.pay_name == "jp") {
				postData = this.getJPPayOrderInfo();
			} else if (this.is_distribution == 1) {
				postData.type = "FX";
				console.log(uni.getStorageSync("fatherId"));
				if (uni.getStorageSync("fatherId")) {
					postData.fatherId = uni.getStorageSync("fatherId");
				}
			} else if (this.isLive) {
				postData.type = 'ZB'
				postData.productType = 'ZB'
				postData.roomId = this.roomId
			}
			//预售
			if (this.preSale) {
				//定金 订货
				postData.type = "PS";
				if (uni.getStorageSync('payTarget')) {
					postData.payTarget = uni.getStorageSync('payTarget')
				} else {
					if (this.sellMap.sellType == 1) {
						postData.payTarget = 1;
					} else {
						postData.payTarget = 3;
					}
				}
			}

			if (this.cpId) {
				postData.product = this.cpId;
				postData.cart_id = "";
			}
			if (this.freights) {
				postData.freights = this.freights
			}
			//支付类型
			let {
				pay_type
			} = this.getPayTypeAndStoreType();
			postData.pay_type = pay_type;

			// 限时折扣
			if (this.payType == "XSJG") {
				// postData.add_good = this.addobject.id;
				postData.add_good = JSON.stringify(this.add_good);
			}
			console.log(this.payType);
			if (!this.is_express) {
				postData.shop_address_id = this.shop_address_id;
			}
			postData.fullName = this.fullName
			postData.fullPhone = this.fullPhone
            postData.fullcpc = this.fullcpc

            //用户选择货币信息
            let userCurrency = uni.getStorageSync("currency");
            if(userCurrency){
                let currency_code = userCurrency.currency_code ;
                let currency_symbol = userCurrency.currency_symbol ;
                let exchange_rate = userCurrency.exchange_rate ;
                postData.currency_code = currency_code;
                postData.currency_symbol = currency_symbol;
                postData.exchange_rate = exchange_rate;
            }

            //下单 
            let {
                data,
                code,
                message,
                status
            } = await this.$req.post({
                data: postData
            })
            if (data && data.order_id) {
                //存订单id 用于支付失败 点击重新支付
                uni.setStorageSync("order_id", data.order_id);
            }
            // 获取当前打开过的页面路由数组
            let routes = getCurrentPages();
            // 获取当前页面路由，也就是最后一个打开的页面路由
            let curRoute = routes[routes.length - 1].options; 
            //存商品信息 用于支付成功 点击再次购买
            uni.setStorageSync("options", curRoute);

			if (code == 200 || status == 1) {
				this.sNo = data.sNo;
				this.order_id = data.order_id;
				this.$store.state.order_id = data.order_id;
				this.orderTime = data.orderTime;
				//自提订单点击查看订单 要跳转到待收货页面 判断参数
				if (!this.is_express) {
					data.wuliu = "ziti";
				}
				if (this.sellMap && this.sellMap.sellType == 1) {
					//如果是预售定金模式 增加标示 用于支付结果页 查看详情使用
					data.typePs = "dj";
				}

				this.order_pay_info = JSON.stringify({
					...data,
					total_score: data.total_score ? Number(data.total_score || 0) : Number(postData
						.scoreDeduction || 0),
				});
				//如果是购物车下单，给订单增加判断cart_id参数，用于支付结果显示(支付成功没有再来一单按钮)
				if (this.isCar) {
					this.order_pay_info = JSON.parse(this.order_pay_info);
					this.order_pay_info.cart_id = this.isCar;
					this.order_pay_info = JSON.stringify(this.order_pay_info);
				} else if (this.again) {
					//如果是重新支付-待付款支付,同上
					this.order_pay_info = JSON.parse(this.order_pay_info);
					this.order_pay_info.cart_id = this.again;
					this.order_pay_info = JSON.stringify(this.order_pay_info);
				}
				console.log("支付订单信息", this.order_pay_info);
				uni.setStorageSync("payRes", this.order_pay_info);
			} else {
				console.log("这里跳转返回注释了", message);
				uni.showToast({
					title: message,
					duration: 1500,
					icon: "none",
				});

                setTimeout(async () => {
                    if (code == '51144') {
                        let params = {
                            api: 'app.common.fetchUserCurrencyInfo'
                        };
                        let {
                            data,
                            code,
                            message,
                            status
                        } = await this.$req.post({
                            data: params
                        })

                        if (code == 200) {
                            userCurrency.exchange_rate = data.exchange_rate;
                            uni.setStorageSync("currency", userCurrency);
                            //重新刷新订单确认页面
                            this.refreshPageData();
                        }
                    }
                }, 1500)
                
                //货币汇率已变更，请重新下单 更新用户币种汇率信息 之后可以重新下单
                
                
				if (code == '00000') {
					setTimeout(() => {
						// uni.navigateBack()
					}, 1500)
				}
				this.pay_display = false;
				if (code == '50974' || message == '地址超出配送范围') {
					uni.removeStorageSync('cartPay')
				}
				return Promise.reject();
			}
		},

		//支付下单调用方法
		async order_pay3() {
            console.log("order_pay3支付方式：" + this.pay_way);
			//检测用户数的选择支付方式，通过全局声明的参数
			this._checkSelectPayWay();
			//如果是微信支付--执行
			// #ifdef MP-WEIXIN
			wx.requestSubscribeMessage({
				tmplIds: this.tmplIds,
				complete: async () => {
					uni.showLoading({
						title: this.language.showLoading.waiting,
					});

					uni.hideLoading();
					console.log(this.pay_way);
					if (this.pay_way == "useWallet") {
						//判断是否 有支付密码
						if (this.password_status == false && !uni.getStorageSync('password_status')) {
							uni.showModal({
								title: this.language.showModal.hint,
								content: this.language.showModal.mima,
								confirmText: this.language.showModal.confirm,
								cancelText: this.language.showModal.cancel,
								showCancel: true,
								success: (resM) => {
									this.fastTap = false;
									if (resM.confirm) {
										uni.navigateTo({
											url: "/pagesB/setUp/payment",
										});
									}
								},
							});
							return false;
						}
						//下单支付方法，修改其中的接口需要参数
						await this._getPayOrderInfo3();
						//_getPayOrderInfo3 下单成功后调用 _orderUseWalletPay
						this._orderUseWalletPay();
					} else {
						uni.showLoading({
							title: this.language.showLoading.axiospaying,
							mask: true,
						});
						//同上--下面注释更多 这里不多写
						await this._getPayOrderInfo3();
						//同上--下面注释更多 这里不多写
						await this._notUserWalletPay();
					}
				},
			});
			// #endif

			//如果不是微信支付--执行
			// #ifndef MP-WEIXIN
			console.log(this.password_status, 222);
			//如果是余额支付
			if (this.pay_way == "useWallet") {
				//这里不是说 没输入密码！是没设置密码！
				//没有支付密码 就去【设置】支付密码
				if (this.password_status == 0 && !uni.getStorageSync('password_status')) {
					uni.showModal({
						title: this.language.showModal.hint,
						content: this.language.showModal.mima,
						confirmText: this.language.showModal.confirm,
						cancelText: this.language.showModal.cancel,
						showCancel: true,
						success: (resM) => {
							this.fastTap = false;
							if (resM.confirm) {
								uni.navigateTo({
									url: "/pagesB/setUp/payment",
								});
							}
						},
					});
					return false;
				}
				//已经设置了支付密码的 往下走
				//调用 _getPayOrderInfo3 下单
				await this._getPayOrderInfo3();

				//_getPayOrderInfo3 下单成功后调用 _orderUseWalletPay
				//_orderUseWalletPay 是 调用密码弹窗 输入密码
				//输入完成后会 调用 pay.js 中 check_pay_password 进行余额支付
				this._orderUseWalletPay();
				// check_pay_password 这个方法 是发请求先验证输入的密码是否正确，正确后调用支付接口 例如 _wallet_pay
			} else {
				//不是余额支付--执行
				uni.showLoading({
					title: this.language.showLoading.axiospaying,
					mask: true,
				});
				//同上，这是在 不是余额支付的情况下调用
				await this._getPayOrderInfo3();

				//判断是什么支付方式 微信、支付宝。。。
				await this._notUserWalletPay();
			}
			// #endif
		},

		//（可修改）单方法--自动返回支付接口需要的参数
		async _getPayOrderInfo3() {
			let remarks = this.remarks;
			//待补充注释
			if (typeof remarks != "string") {
				if (remarks && remarks != "" && remarks.length > 0) {
					remarks = JSON.stringify(remarks);
				}
			}
			console.log(this.apiUrl3, 89898989898, this.mainId3);

			//下单--请求需要的参数 （可修改：根据接口修改，支付类型由下面给予）
			let postData = {
				api: "plugin.auction.AppAuction.payPromise",
				specialId: this.specialId,
			};
			//待补充注释
			// if (this.buy_again) {
			//     postData.buy_type = 1;
			// }
			//待补充注释
			// if (this.bargain) {
			//     postData = this.getBargainPayOrderInfo()
			// } else if (this.seckill) {
			//     postData = this.getSeckillPayOrderInfo()
			// } else if (this.pay_name == 'jp') {
			//     postData = this.getJPPayOrderInfo()
			// } else if (this.is_distribution == 1) {
			//     postData.type = 'FX'
			// }
			//待补充注释
			uni.setStorageSync("payType", 1);

			//产品id
			if (this.cpId) {
				postData.product = this.cpId;
				postData.cart_id = "";
				// 从订单详情页获取 运费模板id
				if (this.freightId) {
					postData.product.push({
						freightId: this.freightId
					})
				}
			}
			//地址
			if (!this.is_express) {
				postData.shop_address_id = this.shop_address_id;
			}

			//重要一
			//返回--选择的支付类型
			let {
				pay_type
			} = this.getPayTypeAndStoreType();
			//赋予请求参数 payType 值
			postData.payType = pay_type || "wallet_pay";
			if (this.apiUrl3 || this.mainId3) {
				postData = {
					api: this.apiUrl3,
					mainId: this.mainId3,
					order_type: "JP",

					// store_type:this.getStoreType()// 订单来源
				};
				if (this.address_id) {
					postData.address_id = this.address_id;
				}
			}

			// 订单来源
			postData.store_type = this.getStoreType();
			//重要一
			//下单请求--返回【支付请求】所需的参数 赋值给 公共参数 然后在支付请求时使用。例如：余额pay.js 中的 _wallet_pay
			let {
				data,
				code,
				message,
				status
			} = await this.$req.post({
				data: postData,
			});

			if (data && data.order_id) {
				//存订单id 用于支付失败 点击重新支付
				uni.setStorageSync("order_id", data.order_id);
			}
			// 获取当前打开过的页面路由数组
			let routes = getCurrentPages();
			// 获取当前页面路由，也就是最后一个打开的页面路由
			let curRoute = routes[routes.length - 1].options;
			//存商品信息 用于支付成功 点击再次购买
			uni.setStorageSync("options", curRoute);

			//es6写法 res.data = data // res.code = code // res.message = message  // res.status = status
			if (code == 200) {
				//成功 后赋值 给 支付接口所需要的参数 （可修改）
				console.log(data, 'datadata');
				this.sNo = data.sNo;
				this.total = data.total;
				this.order_pay_info = JSON.stringify({
					...data
				});
				uni.setStorageSync("payRes", this.order_pay_info);
			} else if (code == 999) {
				//失败
				uni.showToast({
					title: message,
					duration: 1500,
					icon: "none",
				});
				return Promise.reject();
			} else {
				//失败
				uni.showToast({
					title: message,
					duration: 1500,
					icon: "none",
				});
				this.pay_display = false;
				setTimeout(() => {
					this.navBack();
				}, 1500);
				return Promise.reject();
			}
		},

		async _notUserWalletPay() {
			console.log(this.title);
			if (this.pay_way == "wxPayStatue") {
				this.pay_type = "wx";
			} else if (this.pay_way == "aliPayStatue") {
				this.pay_type = "ali";
			} else if (this.pay_way == "baidupayStatue") {
				this.pay_type = "baidu";
			} else if (this.pay_way == "paypal") {
				this.pay_type = "paypal";
			} else if (this.pay_way == "stripe") {
				this.pay_type = "stripe";
			}else if (this.pay_way == "offline_payment") {
				this.pay_type = "offline_payment";
			}
			await this.clientTransferPay();
		},

		/**
		 * 显示钱包余额支付的密码输入框
		 * @returns {boolean}
		 * @private
		 */
		_orderUseWalletPay() {
			console.log(
				"993993993",
				this.title,
				this.vipsou,
				this.total,
				this.user_money,
				this.type,
				"this.total > this.user_moneythis.total > this.user_money"
			);

			if (this.total > this.user_money) {
				uni.showToast({
					title: this.payTitle && this.payTitle == "保证金" ?
						"余额不足以支付保证金" : "余额不足",
					icon: "none",
					duration: 1500,
				});
				let type = this.title;
				if (this.type === 'IN') {
					type = "积分订单"
				}
				switch (type) { 
					case "竞拍保证金":
						break;
					case "会员支付":
						console.log("会员支付,余额不足。");
						break;
					case "秒杀订单":
						setTimeout(function() {
							uni.redirectTo({
								url: "/pagesB/seckill/seckill_my?order_type=" +
									"payment",
							});
						}, 1000);
						break;
					case "积分订单":
						setTimeout(function() {
							uni.redirectTo({
								url: "/pagesB/integral/exchange",
							});
						}, 1000);
						break;
					case "预售订单":
						setTimeout(function() {
							uni.redirectTo({
								url: "/pagesC/preSale/order/myOrder",
							});
						}, 1000);
						break;
					default: 
						if (this.type == "FX") {
							setTimeout(function() {
								uni.redirectTo({
									url: "/pagesB/order/myOrder?status=1&type=FX",
								});
							}, 1000);
						} else if (this.type == "JP") {} else if (this.type == "MS" || this.types == 'MS') {
							setTimeout(function() {
								uni.redirectTo({
									url: "/pagesB/seckill/seckill_my?order_type=" +
										"payment",
								});
							}, 1000);

						} else if (this.type == "ZB" || this.types == 'ZB') {
							setTimeout(function() {
								uni.redirectTo({
									url: "/pagesD/liveStreaming/liveStreamingOrder",
								});
							}, 1000);
						} else if (
							this.type != "PT" &&
							this.payTitle != "保证金"
						) { 
                            console.log(this.is_discount)
                            debugger
							// 实物订单支付失败
							if (this.commodity_type == 0) {
                                // 普通实物订单
                               
								setTimeout(function() {
									uni.redirectTo({
										url: "/pagesB/order/myOrder?status=1",
									});
								}, 1000);
                                // 限时折扣订单
                                // /pagesC/discount/discount_my
							} else {
								// 虚拟订单 支付失败 查看列表
								setTimeout(function() {
									uni.redirectTo({
										url: "/pagesB/order/myOrder?status=1&orderType_id=1",
									});
								}, 1000);
							}
						}
						break;
				}
				this.fastTap = false;
				return false;
			}
			//如果是购物车下单，给订单增加判断cart_id参数，用于支付结果显示(支付成功没有再来一单按钮)
			if (this.isCar) {
				this.order_pay_info = JSON.parse(this.order_pay_info);
				this.order_pay_info.cart_id = this.isCar;
				this.order_pay_info = JSON.stringify(this.order_pay_info);
			} else if (this.again) {
				//如果是重新支付-待付款支付,同上
				this.order_pay_info = JSON.parse(this.order_pay_info);
				this.order_pay_info.cart_id = this.again;
				this.order_pay_info = JSON.stringify(this.order_pay_info);
			}
			console.log(this.order_pay_info, 'this.order_pay_info')
			uni.setStorageSync("payRes", this.order_pay_info); 
			// showPay 当 为true时 则代表 需要通过 验证订单接口判断 是否显示软键盘  
            console.log('showPay=>',typeof this.showPay,this.showPay)
            if(typeof this.showPay != 'boolean'){
                this.pay_display = true;
                return
            }
			if (typeof this.showPay == 'boolean' && !this.showPay ) {
				this.pay_display = true;
			}
		},
	 
		/**
		 * 微信JSAPI支付获取code值
		 */
		_toUrl() {
			let locationUrl = window.location.href;
			console.log('112711271127', locationUrl);
			let arr = locationUrl.split("#");
			if (arr[1].includes('/pagesB/userVip/open_renew_membership')) {
				let li = arr[0].split("?");
				locationUrl = li[0] + '#' + arr[1]
			}
			var storage = window.localStorage;
			storage["bindding_num"] = this.bind_id;
			console.log("tourl时的bindding_num", this.bind_id);
			storage["address_id"] = this.address_id;
			console.log("tourl时的cart_id", this.cart_id);
			// storage['cart_id'] = this.cart_id
			if (this.commodity_type) {
				uni.setStorageSync("commodity_type", this.commodity_type);
			}

			// 拼团
			storage["product"] = this.cpId;

			let data = {
				type: "jsapi_wechat", 
				api: "app.order.get_config",
				url: locationUrl,
			};
			this.$req.post({
				data
			}).then((res) => {
				if (res.code == 200) {
					let myappid = res.data.config.appid;
					this.myappid = myappid;
					let myUrl = res.data.url;
					// alert(res.data.url)
					window.location.href =
						"https://open.weixin.qq.com/connect/oauth2/authorize?appid=" +
						myappid +
						"&redirect_uri=" +
						myUrl +
						"&response_type=code&scope=snsapi_base&state=STATE#wechat_redirect&datetime=" +
						new Date().getTime();
				} else {
					// uni.showToast({
					//     title: res.message,
					//     duration: 1500,
					//     icon: 'none'
					// })
				}
			});
		},
		/**
		 * 页面显示时触发。由于加了微信地址获取，改成获取并新增以后再触发一次
		 */
		show() {
			console.log('走了吗');
			// 每次进入页面接受自提选择的页面
			if (this.hc_address_id != this.$store.state.address_id) {
				this.hc_address_id = this.address_id =
					this.$store.state.address_id;
			}

			if (uni.getStorageSync("store_choose")) {
				uni.removeStorageSync("store_choose");
			}

			this.shop_address_id = uni.getStorageSync("shop_address_id");
			this._axios("onshow");

			// #ifdef MP-WEIXIN
			this.LaiKeTuiCommon.getWXTmplIds(this);
			// #endif

			this.fastTap = false;
		},
		handleOrderAddress(address, shop_list, shop_status) {
			if (shop_status && shop_list) {
				this.shop_status = shop_status;

				if (Array.isArray(shop_list)) {
					this.shop_address_id = "";
					uni.setStorageSync("shop_address_id", "");
				} else {
					this.shop_list = shop_list;
					this.shop_address_id = shop_list.id;
					uni.setStorageSync("shop_address_id", shop_list.id);
					console.log(this.shop_list);
					// 虚拟商品
					if (this.commodity_type == 1 && this.storeHx.length) {
						this.shop_address_id = this.storeHx[0].id
						uni.setStorageSync('shop_address_id', this.storeHx[0].id)
					} else {
						this.shop_address_id = shop_list.id
						uni.setStorageSync('shop_address_id', shop_list.id)
					}

				}
			}
			if (Array.isArray(address)) {
				address = {
					name: "",
				};
				this.shop_status = false;
				this.shop_address = {
					id: 0,
				};
				this.address_status = false;
				this.shop_list = {
					address: "",
					id: 0,
					mobile: "",
					name: "",
				};
			}
			this.address = address;

			if (this.address && this.address.id) {
				this.address_id = this.address.id;
			} else {
				this.address_id = "";
			}

			if (this.address && this.address.name) {
				this.adds_f = this.address.length !== 0;
			} else {
				this.adds_f = false;
			}
			if (this.address && this.address.name) {
				if (!this.$store.state.address_id_def) {
					this.$store.state.address_id_def = address.id;
				}
			}
		},
	},
};
