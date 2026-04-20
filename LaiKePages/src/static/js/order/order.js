import Vue from 'vue'
// 加载数据
export function _axios(me) {
	me.axios_times++
	let LstoreType = Vue.prototype.LaiKeTuiCommon.getStoreType()
	if (me.group) {
		let action = 'order';
		// if (me.a_type == 1) {
		//     action = 'pthd_groupbuy';
		// }
			var data = {
				access_id: me.access_id,
				order_id: me.orde_id,
				api: `app.${action}.order_details`,
				store_type: LstoreType
			}
	} else {
		var data = {
			access_id: me.access_id,
			order_id: me.orde_id,
			api: 'app.order.order_details',
			store_type: LstoreType
		}
	}
	if (uni.getStorageSync('skipType')) {
		data.skipType = uni.getStorageSync('skipType')
	}
	if (me.r_status_ == 1 || me.r_status_ == 2 || me.r_status_ == 5 || me.r_status_ == 7) {
		data.api = 'app.order.index'
	}
	let TMPUrl = uni.getStorageSync('url') || Vue.prototype.LaiKeTuiCommon.LKT_API_URL;
	let storeType = Vue.prototype.LaiKeTuiCommon.getStoreType()
	if (Vue.prototype.LaiKeTuiCommon.VERSION !== 'gw') {
		if (TMPUrl.indexOf("&store_type") == -1) {
			TMPUrl = TMPUrl + `&store_type=${storeType}`
		}
	}
	uni.request({
		data,
		url: TMPUrl,
		header: {
			'content-type': 'application/x-www-form-urlencoded'
		},
		method: 'POST',
		success: function(res) {
			console.log(res)
			if (res.data.code == 200) {
				let {
					data: {
						data,
						data: {
							password_status,
							logistics,
							enterless,
							payment,
							sNo,
							id,
							remarks,
							subtraction_list
						}
					}
				} = res
				if (res.data.data.p_sNo) {
					me.p_sNo = res.data.data.p_sNo
				}
				me.write_store_num = data.write_store_num
				me.show_write_store = data.show_write_store
				me.appointment = data.appointment
				me.status = data.status
				me.isExtract = data.otype != 'pt'
				me.isOrderDel = data.isOrderDelBtn
				me.haveExpress = data.haveExpress
				me.self_lifting = data.self_lifting
				me.sale_type = data.sale_type
				me.delivery_status = data.delivery_status
				me.storeImg = data.list[0].shop_head || me.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					"images/icon/store.png" //店铺头像//店铺头像
				let stores = []
				data.list.filter(items => {
					if (!stores.some(item => item.shop_id == items.shop_id)) {
						stores.push({
							shop_name: items.shop_name,
							shop_id: items.shop_id,
							shop_logo: items.shop_logo,
						})
					}
				})
				data.stores = stores
				if (data.status == 0 && (data.otype == 'pt' || data.otype == 'PP')) {
					data.product_total2 = data.product_total.toFixed(2)
					me.is_end = data.is_end
					data.product_total = data.list[0].p_price = data.z_price = ((data.z_price * 100) / 100)
						.toFixed(2)
				}
				// me.is_end = data.is_end
				me.user_can_open = data.user_can_open
				me.user_can_can = data.user_can_can
				me.user_can_after = data.user_can_after
				me.payment = payment
				data.coupon_price = Number(data.coupon_price).toFixed(2)
				data.preferential_amount = Number(data.preferential_amount).toFixed(2)
				me.order = Object.assign({}, data)
				// if(res.data.data.otype == "FX"){me.order.list[0].refund = true}

				me.enterless = enterless
				me.load = false
				me.sNo = sNo
				me.order_id = id
				// me.remarks = JSON.parse(remarks)
				me.password_status = password_status
				me.cancelGoods = res.data.data.list[0].prompt
				me.is_distribution = res.data.data.list[0].is_distribution
				me.cancelGoodsReason = res.data.data.list[0].content || '暂无！'
				if (logistics && logistics.length > 0) {
					me.logistics = logistics
				} else {
					me.logistics = false
				}
				me.message = me.order.sNo
				me.otype = me.order.otype
				me.pttype = me.order.pttype
				me.gstatus = me.order.gstatus
				me.ordermsg = me.order.omsg
				me.p_id = me.order.pro_id
				me.subtraction_list = subtraction_list
				if (me.message.indexOf('KJ') > -1) {
					me.kanjia = true
				}
				if (me.otype == 'pt' || me.otype == 'PP') {
					me.endpay = me.order.z_price
					me.price = me.endpay
				} else {
					me.endpay = me.order.z_price
				}
				if (me.order.status == 2 || me.order.status == 3 || me.order.status == 5 || me.order
					.status ==
					8) {
					me.order_wl = true
					me.showPay1 = false
				} else if (me.order.status == 0) {
					if ((me.pttype != 'cantuan' || (me.pttype == 'cantuan' && me.gstatus.ptstatus == 1)) &&
						me.otype != 'JP') { //排除团成功或者团失败的拼团订单
						//待付款，倒计时关闭订单
						me.order_wl = false
						var time = me.order.add_time
						var Htime = time.split(' ')
						var Dtime = Htime[0].replace('-', '/')
						var Time = Dtime.replace('-', '/')
						var start = Time + ',' + Htime[1]
						var startTime = new Date(start)
						var endTime = startTime.getTime() + (me.order.order_failure * 60 * 60 * 1000)
						if (me.pttype == 'cantuan') {
							var gend = me.gstatus.endtime
							gend = new Date(gend.replace(/-/g, '/')).getTime()
							endTime = endTime > gend ? gend : endTime //如果结束时间比拼团结束时间长就用拼团时间
						}
						me.setTime = setInterval(function() {
							_time(endTime)
						}, 1000)

						function _time(endTime) {
							var myDate = new Date()
							me.time_c = parseInt((endTime - myDate.getTime()) / 1000) //得到剩余的毫秒数
							if (me.time_c >= 0) {
								me.time_D = Math.floor(me.time_c / 60 / 60 / 24) //得到天
								me.time_H = Math.floor(me.time_c / (60 * 60) % 24) //得到小时 取模24小时
								me.time_M = Math.floor(me.time_c / 60 % 60) //得到分钟
								me.time_S = Math.floor(me.time_c % 60) //得到秒数
								me.order_zt = me.language.order.order.remain + `${me.time_H}` + me.language
									.order.order.Time + `${me.time_M}` + me.language.order.order.branch +
									`${me.time_S}` + me.language.order.order.second
							} else {
								clearInterval(me.setTime)

							}
						}
					} else if (me.pttype == 'cantuan' && me.gstatus.ptstatus == 2) {
						me.showPay1 = false
						me.order_zt = me.language.toasts.order.axios[0]
						me.can_pay = false
						setTimeout(function() {
							uni.showToast({
								title: me.language.toasts.order.axios[0],
								icon: 'none',
								duration: 2000
							})
							return false
							var data = {
								api: 'app.order.removeOrder',
								order_id: me.orde_id,
								access_id: me.access_id
							}
							uni.request({
								data,
								url: uni.getStorageSync('url'),
								header: {
									'content-type': 'application/x-www-form-urlencoded'
								},
								method: 'POST',
								success: function(res) {
									let {
										data: {
											code,
											message
										}
									} = res
								},
								error: function(err) {
									console.log(err)
								}
							})
						}, 700)
					} else if (me.pttype == 'cantuan' && me.gstatus.ptstatus == 3) {
						me.order_zt = me.language.toasts.order.axios[1]
						me.can_pay = false

						setTimeout(function() {
							uni.showToast({
								title: me.language.toasts.order.axios[1],
								icon: 'none',
								duration: 2000
							})
							let data = {
								api: 'app.order.removeOrder',
								order_id: me.orde_id
							}

							me.$req.post({
								data
							})
						}, 700)
					}
				} else if (me.order.status == 1) {
					me.order_wl = false
					me.order_zt = me.language.toasts.order.axios[2]
				} else if (me.order.status == 7) {
					me.order_wl = false
					// me.order_zt = me.language.toasts.order.axios[3]
					if (me.setTime) {
						clearInterval(me.setTime)
					}
				}

			} else {
				uni.showToast({
					title: res.data.message,
					duration: 1500,
					icon: 'none'
				})

				if (res.data.message == '未登录' || res.data.message == '请登录！' || res.code == 203) {
					uni.redirectTo({
						url: '/pagesD/login/newLogin'
					})
				} else {
					setTimeout(() => {
						uni.navigateBack({
							delta: 1,
						});
					}, 1000)
				}
			}
		}
	})
}


// 售后
export function _after(e, id, content, r_status, me, comments_type = null) {
	var order_details_id = []
	order_details_id.push(id)
	if (r_status == 1 || r_status == 0 || r_status == 5 || r_status == 2 || comments_type == 1 || comments_type == 2 ||
		r_status == 12) {
		if (me.order.list.length > 1) {
			if (r_status == 1) {
				uni.navigateTo({
					url: '/pagesA/returnGoods/returnGoods?rType=true&order_details_id=' + order_details_id +
						'&order_anking=1&r_status=' + r_status + '&otype=' + me.otype
				})
			} else {
				uni.navigateTo({
					// url:'batchOrder?orde_id='+this.orde_id
					url: '/pagesA/returnGoods/returnGoods?order_details_id=' + order_details_id +
						'&order_anking=1&r_status=' + r_status + '&otype=' + me.otype
				})
			}
		} else {
			uni.navigateTo({
				url: '/pagesA/returnGoods/returnGoods?rType=true&order_details_id=' + order_details_id +
					'&order_anking=1&r_status=' + r_status + '&otype=' + me.otype
			})
		}
	} else if (r_status == 4) {

	} else if (r_status == 8) {
		uni.navigateTo({
			url: '/pagesA/returnGoods/returnGoods?rType=true&order_details_id=' + order_details_id +
				'&order_anking=1&r_status=' + r_status + '&otype=' + me.otype
		})
	} else if (comments_type == 1) {
		uni.navigateTo({
			url: '../evaluate/evaluating?order_id=' + id + '&num=one'
		})
	}
}
