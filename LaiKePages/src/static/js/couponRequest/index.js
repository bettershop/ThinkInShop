/**
 * 优惠券 公用api 请求
 * 与原有逻辑 没有任何变化 2025-12-13
 */

// 获取优惠券
export function LaiKeTuiGetCoupon(me) {
	me.couponMask = true
	var data = {
		api: 'app.coupon.pro_coupon',
		pro_id: me.pro_id,
	}
	me.$req.post({
		data
	}).then(res => {
		if (res.code == 200) {
			res.data.list.filter(item => {
				item.limit = item.limit.replace('满', '满 ' + this.LaiKeTuiCommon.DEFAULT_STORE_SYMBOL)
					.replace('可用', ' 可用')
				item.money = Number(item.money)
				item.discount = Number(item.discount)
			})

			me.coupon_list = res.data.list
		} else {
			uni.showToast({
				title: res.message,
				duration: 1500,
				icon: 'none'
			})
		}
		me.fastTap = true
	})
}

// 领取优惠券
export function LaiKeTui_receive(me, id) {
	if (!me.fastTap) {
		return
	}
	me.fastTap = false
	if (!me.access_id) {
		me.$refs.lktAuthorizeComp.handleAfterAuth(me, '../../pagesD/login/newLogin?landing_code=coupon')
		return
	}
	var data = {
		// module: 'app',
		// action: 'coupon',
		// app: 'receive',
		api: "plugin.coupon.Appcoupon.Receive",
		id: id
	}
	me.$req.post({
		data
	}).then(res => {
		let code = res.code
		if (code == 200) {
			uni.showToast({
				title: me.language.toasts.goodsDetailed.lqOk,
				duration: 1500,
				icon: 'none'
			})
			setTimeout(function() {
				me.getCoupon()
			}, 1500)
		} else {
			uni.showToast({
				title: res.message,
				duration: 1500,
				icon: 'none'
			})
			setTimeout(function() {
				me.getCoupon()
			}, 1500)
		}
	}).catch((err) => {
		me.fastTap = true
	})
}