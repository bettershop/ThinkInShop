import htmlParser from '../../../common/html-parser.js'

export function LaiKeTui_axios(me) {
	var vipSource = uni.getStorageSync('vipSource')
	var data = {
		api: 'app.product.index',
		pro_id: me.integral ? me.goodsId : me.pro_id,
		// pro_id: '155',
		vipSource: vipSource
	}
	if (me.bargain) {
		data.type = 'KJ'
		data.attr_id = me.attr_id
		data.bargain_id = me.bargain_id
	} else if (me.seckill) {
		if (me.b_type == '1') {
			data.type = 'PM'
		} else {
			data.type = 'MS'
		}


		data.navType = me.navType && me.navType != undefined ? me.navType : ''
		data.id = me.id && me.id != undefined ? me.id : ''
	} else if (me.isDistribution) {
		data.id = me.fx_id
		data.type = 'FX'
	} else if (me.integral) {
		data.type = 'IN'
		data.id = me.pro_id
		data.integralNum = me.integral
	} else if (me.is_discount) {
		data.type = 'FS'
		data.pro_id = me.pro_id
		data.id = me.fsid
	} else if (me.isLive) {
		data = {
			api: 'plugin.living.AppAnchorProduct.proDetails',
			proId: me.integral ? me.goodsId : me.pro_id,
			roomId: me.roomId
		}
	}
    


	me.$req.post({
		data
	}).then(res => {
		if (res.code != 200) {
			uni.showToast({
				title: res.message
			})
			setTimeout(function() {
				uni.redirectTo({
					url: '/pages/shell/shell?pageType=home'
				})
			}, 300)
			return
		}
		console.log(res)
		// uni.hideLoading()
		if (res && res.data) {
			me.login_status = res.data.login_status
		}
		me.load = false
		if (res.code == 200) {
			let {
				data: {
					collection_id,
					pro,
					access_id,
					comments,
					commentsTotal,
					commodity_type,
					attrList,
					skuBeanList,
					qj_price,
					type,
					logo,
					active,
					is_grade,
					shop_list,
					coupon_str,
					activity_type_codes,
					attribute_list,
					write_off_settings,
					mchStoreListNum,
					isAddCar,
					writeNumStatus
				}
			} = res
			//虚拟商品 1需要核销 2无需核销
			me.write_off_settings = write_off_settings || ''
			//虚拟商品-需要核销-核销门店数量
			me.mchStoreListNum = mchStoreListNum
			//虚拟商品-需要核销 1可加入购物车 2不可加入购物车
			me.isAddCar = isAddCar
			//虚拟商品-需要核销-是否存在可预约时间
			me.xnHxTime = writeNumStatus == 1 ? true : false
			//虚拟商品结束
			me.secStatus = pro.secStatus
			// 更改轮播图图片
			pro.img_arr = pro.img_arr.map(item => {
				return item = item + '?a=' + Math.floor(Math.random() * 1000000);
			})
			if (res.data.pro.content) {
				try {
					let IntroList2 = JSON.parse(res.data.pro.content)
					// IntroList2.forEach((item) =>{
					//     item.content = item.content.replace(/src/g,'style=\'width:100%!important;height: auto!important;\' src')
					//     .replace(/<table/g, `<table style="width:100%!important;"`)
					//     .replace(/<img/g, `<img style="width:100%!important;"`)
					//     .replace(/<div/g, `<p style="width:100%!important;"`)
					// })
					IntroList2 = IntroList2.map((item) => {
						return {
							pname: item.name,
							content: item.content.replace(/src/g,
									'style=\'width:100%!important;height: auto!important;\' src')
								.replace(/<table/g, `<table style="width:100%!important;"`)
								.replace(/<img/g, `<img style="width:100%!important;"`)
								.replace(/<div/g, `<p style="width:100%!important;"`)
						};
					});
					me.IntroList = IntroList2
					if (me.IntroList.length <= 0) {
						me.IntroList = [{
							content: me.language.goods.goods.zwxq,
							pname: me.language.uploadPro.title2
						}]
					}
				} catch (error) {
					me.IntroList = [{
						content: res.data.pro.content,
						pname: me.language.uploadPro.title2
					}]
				}
				console.log('wwwwwww', me.IntroList)
			}
			//   详情
			// me.isGoodsAdd = JSON.parse(res.data.pro.content)
			// me.isGoodsAdd.forEach((item,index)=>{
			//     item.pname = item.name
			// })
			// me.seeGoods = me.isGoodsAdd[0]
			//这段注释因为运行报错

			// me.pro = me.cutPicture(pro)
			// me.$set(me.pro, 'oneCover', me.cutPicture(pro).oneCover)
			// me.pro = me.cutPicture2(pro)
			// me.$set(me.pro, 'twoCover', me.cutPicture(pro).twoCover)
			// console.log('pro',me.pro);
			me.proVideo = pro.proVideo
			me.video = pro.video
			me.type1html = '<video src="' + me.video +
				'" style="width:100%;height:100%" :controls="false" poster="' + me.video +
				'?x-oss-process=video/snapshot,t_0,f_jpg"></video>';
			me.type1html2 = '<video src="' + me.proVideo +
				'" style="width:100%;height:100%;border-radius:8px" :controls="false" poster="' + me.proVideo +
				'?x-oss-process=video/snapshot,t_0,f_jpg"></video>';
			me.ys_price = qj_price
			// if(pro.num){
			me.kucunum = pro.num
			// }
			if (me.bargain) {
				me.cs_price = res.data.cs_price
				me.cs_num = res.data.cs_num
				me.cs_yprice = res.data.cs_yprice
				let bargain_list = res.data.bargain_info[0]

				if (data.type != "KJ") {
					me.leftTime = bargain_list.leftTime
					me.isbegin = bargain_list.isbegin
					me.hasorder = bargain_list.hasorder
					let bstatus = 3
					if (bargain_list.status == 0) {
						bstatus = 4
					} else if (bargain_list.status == 1) {
						bstatus = 0
					} else if (bargain_list.status == 2) {
						bstatus = 1
					} else if (bargain_list.status == 3) {
						bstatus = 2
					}
					if (bargain_list.leftTime <= 0) {
						bstatus = 3
					}
					me.brStatus = bstatus
					me.sNo_id = bargain_list.sNo_id
					console.log(bargain_list);
					console.log('sss' + bstatus);
				}


				if (me.leftTime) {
					me.SetTimeData();
				}
			}
			me.countdown = pro.remainingTime
			me.commentsTotal = commentsTotal;
			me.commodity_type = commodity_type;
			me.price = qj_price
			me.attrList = attrList
			console.log(attrList[0]);
			// me.haveSkuBean.name = attrList[0].all[0]
			console.log(me.haveSkuBean, '123123123123123');
			me.collection = type
			me.coupon_str = coupon_str ? "" : coupon_str;
			const apiVipPrice = pro.vip_price !== undefined && pro.vip_price !== null && pro.vip_price !== '' ? pro.vip_price : ''
			const apiVipYprice = pro.vip_yprice !== undefined && pro.vip_yprice !== null && pro.vip_yprice !== '' ? pro.vip_yprice : ''
			if (Object.prototype.hasOwnProperty.call(me, 'vipprice') && apiVipPrice !== undefined && apiVipPrice !== null && apiVipPrice !== '') {
				me.vipprice = apiVipPrice
			}
			if (Object.prototype.hasOwnProperty.call(me, 'pricel') && apiVipYprice !== undefined && apiVipYprice !== null && apiVipYprice !== '') {
				me.pricel = apiVipYprice
			}
			if (data.type == 'MS' || data.type == 'PM') {
				me._remain_time(pro)
			} else {
				pro.vip_price = Number(pro.vip_price).toFixed(2)
				pro.vip_yprice = Number(pro.vip_yprice).toFixed(2)
				me.pro = pro
				// pro.content = pro.content.replace(/src/g,
				//     'style=\'width:100%!important;height: auto!important;\' src').replace(
				//     /<table/g, `<table style="width:100%!important;"`)

				// pro.content = pro.content.replace(/src="http/g, 'src="https')
				// pro.content = htmlParser(pro.content)

			}
			me.is_grade = is_grade
			me.active = active
			me.$store.state.access_id = access_id
			uni.setStorageSync('access_id', access_id)

			me.status = pro.status
			me.collection_id = collection_id
			me.shop_list = shop_list
			if (comments) {
				me.comments = comments
			} else {
				me.comments = ''
			}
			me.shareContent = me.pro.name
			me.coupon_status = res.data.coupon_status
			if (typeof me.syncActivityEntries === 'function') {
				me.syncActivityEntries(activity_type_codes)
			} else if (Object.prototype.hasOwnProperty.call(me, 'activity_type_codes')) {
				me.activity_type_codes = activity_type_codes || null
			}
			if (me.$refs && me.$refs.attrModal) {
				me.$refs.attrModal.imgurl = pro.coverImage
				// me.$refs.attrModal.num = pro.num
				me.$refs.attrModal.num = 0
				me.$refs.attrModal.price = pro.price
				me.$refs.attrModal.skuBeanList = attribute_list || []
				// 仅在sku列表有数据时初始化，避免 undefined 报错
				if (me.$refs.attrModal.skuBeanList.length) {
					me.$refs.attrModal.initData()
				}
				pro.commodity_type = commodity_type
				pro.write_off_settings = write_off_settings
				me.$refs.attrModal.pro2 = pro
			}

			me.loadFlag = true

		} else {
			uni.showToast({
				title: res.message,
				duration: 1500,
				icon: 'none'
			})
		}
	}).catch((err) => {
		console.log('err:', err)
		me.load = false
	})
}

// 收藏
export function LaiKeTui_collection(me) {
	if (!me.fastTap) {
		return
	}

	var data = {
		api: 'app.addFavorites.index',
	}

	if (me.is_jifen) {
		data.type = 2
	}

	me.fastTap = false
	setTimeout(function() {
		me.fastTap = true
	}, 1000)

	me.$refs.lktAuthorizeComp.handleAfterAuth(me, '../../pagesD/login/newLogin', function() {
		if (me.collection) {
			data.app = 'removeFavorites'
			data.collection = []
			data.collection.push(me.collection_id)
			me.$req.post({
				data
			}).then(res => {
				me.fastTap = true
				if (res.code == 200) {
					me.collection = false
					// me.is_sus = true
					me.is_susa = true
					setTimeout(() => {
						me.is_susa = false
					}, 3000)
					// uni.showToast({
					//     title: res.message,
					//     duration: 1000,
					//     icon: 'none'
					// })
				} else {
					uni.showToast({
						title: res.message,
						duration: 1000,
						icon: 'none'
					})
				}
			})
		} else {
			data.app = 'index'
			data.pro_id = me.pro_id
			me.$req.post({
				data
			}).then(res => {
				if (res.code == 200) {
					me.collection = true
					me.collection_id = res.data.collection_id
					// uni.showToast({
					//     title: me.language.toasts.goodsDetailed.szOk,
					//     duration: 1000,
					//     icon: 'none'
					// })
					me.is_sus = true
					setTimeout(() => {
						me.is_sus = false
					}, 3000)

				} else {
					uni.showToast({
						title: res.message,
						duration: 1000,
						icon: 'none'
					})
				}
			})
		}
	})
}

// 加入购物车
export function LaiKeTui_shopping(me) {
	me.type = 2
	if (!me.fastTap) {
		return
	}
	me.fastTap = false
	if (me.haveSkuBean) {
		var data = {
			api: 'app.product.add_cart',
			pro_id: me.pro_id,
			attribute_id: me.haveSkuBean.cid,
			num: me.numb,
			type: 'addcart'
		}
		me.$req.post({
			data
		}).then(res => {
			me.fastTap = true
			let {
				code,
				data,
				message
			} = res
			console.log('data', data);
			if (code == 200) {
				// uni.showToast({
				//     title: me.language.toasts.goodsDetailed.addOk,
				//     duration: 1000,
				//     icon: 'none'
				// })
				setTimeout(() => {
					me.is_gwc = true
					setTimeout(() => {
						me.is_gwc = false
					}, 1500)
				}, 500)

				me.haveSkuBean = ''
				me.cart_num(me.numb + me._cart_num)
				me.allCartNum = me._cart_num
				me.getCart()
				me.$refs.attrModal._mask_false()
				if (data.cart_id) {
					if (!in_array(data.cart_id, me.$store.state.nCart)) {
						me.$store.state.nCart.push(data.cart_id)
					}
				}
			} else {
				// me.haveSkuBean.cid = ''
				me.haveSkuBean = null
				uni.showToast({
					title: message,
					duration: 1500,
					icon: 'none'
				})
			}
		})
	} else {
		me.fastTap = true
		me._mask_display()
	}
}


// 立即购买
export function LaiKeTui_buy_handle(me) {
	//虚拟商品-需核销（不存在可核销时间提示）
	if (me.commodity_type == 1 && me.write_off_settings == 1 && me.xnHxTime == false) {
		me.is_showToast = 4
		me.is_showToast_obj.title = '温馨提示'
		me.is_showToast_obj.content = '该商品暂无排期时间可约，感谢关注！'
		me.is_showToast_obj.endButton = '我知道了'
		return
	}

	if (me.fastTap) {
		me.fastTap = false
		me.type = 3
		me.$refs.lktAuthorizeComp.handleAfterAuth(me, '/pagesD/login/newLogin?landing_code=1', function() {
			//正常登录未超时
			if (me.haveSkuBean) {
				var product = []
				product.push({
					pid: me.integral ? me.goodsId : me.pro_id
					// pid: '155'
				})
				product.push({
					cid: me.haveSkuBean.cid
				})

				product.push({
					num: me.numb
				})

				//直播间id
				if (me.roomId) {
					product.push({
						roomId: me.roomId
					});
				}

				if (!me.integral) {
					product.push({
						sec_id: me.option.id
					})
				}

				console.log('product', product)

				product = JSON.stringify(product)
				let data = {
					api: 'app.product.immediately_cart',
					product,
				}
				if (me.commodity_type == 1) {
					data.orderType = 'VI'
				}
				console.log(me)
				console.log(me.integral, 1222223123);
				if (me.integral == true || me.integral) {
					data.orderType = 'IN'
				}
				if (me.roomId) {
					data.orderType = 'ZB'
				}
				me.$req.post({
					data
				}).then(res => {
					me.clicktimes = []
					if (res.code == 200) {
						// let url = '../../pagesC/discount/discount_order_detail?product=' + product +
						//     '&isDistribution=' + me.isDistribution +
						//     '&canshu=false&returnR=0&commodity_type='+me.commodity_type+'&is_hy='+me.is_hy
						let url = '/pagesE/pay/orderDetailsr?product=' + product +
							'&isDistribution=' + me.isDistribution + '&isLive=' + me.isLive +
							'&canshu=false&returnR=0&commodity_type=' + me.commodity_type + '&is_hy=' +
							me.is_hy + '&room_id=' + me.roomId
						if (me.pages == 'pagesB') {

							if (me.b_type == 1) {
								url = '/pagesB/seckill/seckillDetailsr?product=' + product +
									'&isDistribution=' +
									me.isDistribution + '&isLive=' + me.isLive +
									'&canshu=false&returnR=0&b_type=1'
							} else {
								url = '/pagesB/seckill/seckillDetailsr?product=' + product +
									'&isDistribution=' +
									me.isDistribution + '&isLive=' + me.isLive +
									'&canshu=false&returnR=0'
							}
						}
						if (me.integral) {
							url = '/pagesB/integral/integral_order?product=' + product + '&id=' + me
								.pro_id
						}
						if (me.pages == 'pagesC') {
							// console.log('xxxxxx695695695695',url);
							url = '/pagesE/pay/orderDetailsr?product=' + product +
								'&isDistribution=' + me.isDistribution + '&isLive=' + me.isLive +
								'&canshu=false&returnR=0&commodity_type=' + me.commodity_type +
								'&is_hy=' + me.is_hy
						}
						me.$refs.attrModal._mask_f()
						uni.navigateTo({
							url: url,
							fail(err) {
								console.log(err);
							}
						})
						me.fastTap = true
					} else {
						uni.showToast({
							title: res.message,
							duration: 1500,
							icon: 'none'
						})
						setTimeout(function() {
							me._axios()
							me.fastTap = true
						}, 1500)
					}
				})
			} else {
				me.$refs.attrModal._mask_display()
				me.fastTap = true
				console.log(me.is_seckill, 'me.is_seckillme.is_seckillme.is_seckillme.is_seckill')
				if (me.is_seckill) {
					console.log('me.is_seckillme.is_seckillme.is_seckillme.is_seckill')
					// 以下为初始化选中
					if (!me.$refs.attrModal.haveSkuBean) {
						let sku_list = me.$refs.attrModal.sku_list
						for (let i in sku_list.items) {
							if (i == 0 && Number(sku_list.items[i].Stock) > 0) {
								me.$refs.attrModal.haveSkuBean = {
									name: sku_list.items[i].path,
									cid: sku_list.items[i].sku,
									skus: sku_list.items[i]
								};
							}
						}

						let selectName = me.$refs.attrModal.haveSkuBean.name?.split(',')
						for (let i in sku_list.result) {

							for (let k in sku_list.result[i]) {

								let flag = selectName.some(item => item == k)

								if (flag) {
									me.$refs.attrModal.handleActive(i, sku_list.result[i][k])
								}

							}

						}
					}
				}

			}
		})
	}
}

// 确认
export function LaiKeTui_confirm(me) {
	console.log(me)
	console.log('766766766766', me.num)
	console.log(me.pro)
	console.log(me.haveSkuBean)
	console.log(me.type)
	console.log(me.vipprice);
	if (Boolean(me.haveSkuBean)) {
		if (me.num == 0) {
			uni.showToast({
				title: me.language.toasts.goodsDetailed.kucun,
				duration: 1000,
				icon: 'none'
			})
		} else if (me.num != 0) {
			if (me.type == 1) {
				me.$refs.attrModal._mask_false()
				me.pay_lx('pt')
			} else if (me.type == 2) {
				me._shopping()
				me.pay_lx('pt')
			} else if (me.type == 3) {
				me._buy()
				me.pay_lx('pt')
			}
		}
	} else {
		if (me.num == 0) {
			uni.showToast({
				title: me.language.toasts.goodsDetailed.kucun,
				duration: 1000,
				icon: 'none'
			})
		} else {
			uni.showToast({
				title: me.language.toasts.goodsDetailed.choose,
				duration: 1000,
				icon: 'none'
			})
		}
	}
}


export function in_array(stringToSearch, arrayToSearch) {
	for (let s = 0; s < arrayToSearch.length; s++) {
		let thisEntry = arrayToSearch[s].toString()
		if (thisEntry == stringToSearch) {
			return true
		}
	}
	return false
}

export function LaiKeTuiSetTimeData(me) {
	var data = me.leftTime
	setInterval(function() {
		var t = --data
		var d = Math.floor(t / 60 / 60 / 24)
		var h = Math.floor((t / 3600) - (d * 24))
		var m = Math.floor((t - h * 60 * 60 - d * 24 * 60 * 60) / 60)
		var s = t % 60
		if (h < 10) h = '0' + h
		if (m < 10) m = '0' + m
		if (s < 10) s = '0' + s
		me.hour = h
		me.mniuate = m
		me.second = s
		me.day = d
		if (me.leftTime <= 0) {
			me.hour = 0
			me.mniuate = 0
			me.second = 0
			me.day = 0
		}
	}, 1000)
}

export function LaiKeTuiToBr(me) {
	var data = {
		api: 'app.login.token',
	}
	me.$req.post({
		data
	}).then(res => {
		if (res.code == 404 || res.data.login_status == 0) {
			me.$refs.lktAuthorizeComp.handleAfterAuth(me, '../../pagesD/login/newLogin?landing_code=1')
		} else {
			var bstatus = 0
			if (me.brStatus == 0) {
				bstatus = 1
			} else if (me.brStatus == 1) {
				bstatus = 2
			} else if (me.brStatus == 2) {
				bstatus = 3
			} else if (me.brStatus == 3) {
				bstatus = -1
			}
			uni.redirectTo({
				url: '../../pagesC/bargain/bargainIng?proId=' + me.pro_id + '&attr_id=' + me.attr_id +
					'&order_no=' + me.order_no +
					'&brStatus=' + bstatus + '&bargain_id=' + me.bargain_id
			})
		}
	})
}
