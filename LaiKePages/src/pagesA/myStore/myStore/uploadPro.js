import htmlParser from '@/common/html-parser.js';
// 进入上传商品页面
export function LaiKeTui_axios(me) {
	let langCode = me.lang_code;
	uni.setStorageSync('lang_code', langCode)

	me.$req.post({data:{
		api: "mch.App.Mch.Upload_merchandise_page",
		shop_id: me.shop_id
	}}).then(res => {
		setTimeout(function() {
			me.loadFlag = true
		}, 300)

		let {
			code,
			data,
			message
		} = res

		if (code == 200) {

			me.haveStore = data.haveStore || 0
			me.res = data
			me.proFreightPickerArray = []
			for (var i = 0; i < data.freight_list.length; i++) {
				me.proFreightPickerArray.push(data.freight_list[i].name)

				if (data.freight_list[i].is_default == 1) {
					me.freightSet = {
						name: data.freight_list[i].name,
					}
					me.freightSetId = data.freight_list[i].id
				}
			}
			if (data.distributors) {
				for (var i = 0; i < data.distributors.length; i++) {
					me.proDistributorsPickerArray.push(data.distributors[i].name)
				}
			}

			me.show_adr = data.show_adr
			me.proUnitPickerArray = data.unit

			me.s_type = data.s_type
			me.plugin_list = data.plugin_list

			let plugin_list = {
				active: []
			}
			data.plugin_list.active.filter(item => {
				if (item.name == '正价') {
					item.status = true
					plugin_list.active.push(item)
				}
			})
			me.plugin_list = plugin_list

		} else {
			uni.showToast({
				title: message,
				duration: 1500,
				icon: 'none'
			})
		}
	})

}
// 进入编辑商品页面
export async function LaiKeTui_editor(me) {
	await me.$req.post({
		data: {
			api: "mch.App.Mch.Modify",
			shop_id: me.shop_id,
			p_id: me.p_id
		}
	}).then(res => {
		me.pageStatus = 1

		let {
			code,
			data,
			message
		} = res

		me.res = data
		setTimeout(() => {
			uni.hideLoading();
			me.isLoading = false
		}, 1000)
		if (res.code == 200) {

			//国家回显
			me.country_num = data.country_num
			me.countrySet = {
				name: data.country_name,
				num3: data.country_num
			}

			//语种回显
			me.lang_code = data.lang_code
			me.languageSet = {
				lang_name: data.lang_name,
				lang_code: data.lang_code
			}

			// 虚拟商品
			if (data.list.commodity_type == 1) {
				me.goodsType = data.list.commodity_type
				me.xnHxType = data.list.write_off_settings
				me.xnHxTime = data.list.is_appointment == 2 ? true : false
				me.xnHxShopId = data.list.write_off_mch_ids == '0' ? 0 : data.list.write_off_mch_ids
				me.xnHxShopName = data.write_off_mch_names
				if (me.xnHxShopId != '' && me.xnHxShopId != '0') {
					uni.setStorageSync('uploadProXnShopListName', me.xnHxShopName)
					uni.setStorageSync('uploadProXnShopListId', me.xnHxShopId)
				}
			}
			// 虚拟商品结束
			// me.tabKey = data.isVirtual
			me.frames_status = data.status
			me.video = data.video
			me.proVideo = data.proVideo
			me.proClass = data.product_class_list1
			me.chooseClass = data.product_class_list1
			me.type1html = '<video src="' + me.video +
				'" style="width:100%;height:100%;border-radius:8px" :controls="false" poster="' + me.video +
				'?x-oss-process=video/snapshot,t_0,f_jpg"></video>';
			me.type1html2 = '<video src="' + me.proVideo +
				'" style="width:100%;height:100%;border-radius:8px" :controls="false" poster="' + me
				.proVideo +
				'?x-oss-process=video/snapshot,t_0,f_jpg"></video>';
			if (data.list.content) {
				try {
					let IntroList2 = JSON.parse(data.list.content)
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
					me.isGoodsAdd = IntroList2
					if (me.isGoodsAdd.length <= 0) {
						me.isGoodsAdd = [{
							content: me.language.uploadPro.zwxq,
							pname: me.language.uploadPro.xq
						}]
					}
				} catch (error) {
					me.isGoodsAdd = [{
						content: data.list.content,
						pname: me.language.uploadPro.xq
					}]
				}
			}

			if (Array.isArray(data.product_class_list1) && data.product_class_list1.length > 0) {
				let proI = data.product_class_list1.length - 1
				me.proClassId = data.product_class_list1[proI].cid || ''
			} else {
				me.proClassId = data.product_class_list1.cid || ''
			}

			me.res = data
			me.proBrand = data.brand_class_list1

			me.proBrandId = data.brand_class_list1.brand_id || "" // 品牌ID
			me.proName = data.list.product_title
			me.vstName = data.list.subtitle
			me.keyWord = data.list.keyword
			me.proWegiht = data.list.weight
			me.proCode = data.list.scan
			me.showImg = data.imgurls
			me.cover_map = data.cover_map
			me.showImgOld = [...data.imgurls]
			me.$store.state.attr_group = data.attr

			me.$store.state.attr_arr = data.attrList
			let chooseAttr = ''
			for (let i = 0; i < data.attr.length; i++) {
				chooseAttr += ',' + data.attr[i].attr_group_name
			}
			chooseAttr = chooseAttr.replace(',', '')
			uni.setStorageSync('upload_attr_group', data.attr)
			uni.setStorageSync('upload_attr_arr', data.attrList)
			uni.setStorageSync('upload_chooseAttr', chooseAttr)
			me.attr_group = data.attr
			me.attr_arr = data.attrList
			me.show_adr = data.show_adr
			me.s_type = data.s_type
			me.plugin_list = data.plugin_list
			me.freightSet = data.freight_list1
			me.freightSetId = data.freight_list1.id
			me.distributorsSet = data.distributors1
			me.distributorsSetId = data.distributors1.id
			me.content = data.content
			//重新计算 用于显示商品介绍模块
			me.isGoodsAdd = JSON.parse(me.content)

			me.type_status = data.type_status

			if (me.distributorsSetId != 0) {
				me.show_adrStatus = false
				me.distributorsStatus = true
			}
			me.costM = Number(data.initial.cbj)
			me.oldM = data.initial.yj
			me.vshop = data.initial.volume
			me.sellM = Number(data.initial.sj)
			me.stock = data.initial.kucun
			me.unit = data.initial.unit
			me.stockWarn = data.initial.stockWarn
			me.arrimg = data.initial.attrImg

			if (data.initial.length > 1) {
				me.costM = Number(data.initial.cbj)
				me.oldM = data.initial.yj
				me.vshop = data.initial.volume
				me.sellM = Number(data.initial.sj)
				me.stock = data.initial.kucun
				me.unit = data.initial.unit
				me.stockWarn = data.initial.stockWarn
				me.arrimg = data.initial.attrImg
			}
			if (me.type == 'gys') {
				// 供应商 建议零售价
				me.costM = res.data.initial.msrp
			}
			me.s_typeStr = []
			for (var i = 0; i < data.s_type.length; i++) {
				if (data.s_type[i].status) {
					me.s_typeStr.push(data.s_type[i].value)
				}
			}
			for (var i = 0; i < data.show_adr.length; i++) {
				if (data.show_adr[i].status) {
					me.active.push(i)
				}
			}
			for (var i = 0; i < data.plugin_list.active.length; i++) {
				if (data.plugin_list.active[i].status) {
					me.active.push(data.plugin_list.active[i].value)
				}
				if (data.plugin_list.active[0].status == false) {
					me.show_adrStatus = false
				}
			}

			me.res.brand_class_list = data.list.brand_list

			if (data.brand_list) {
				for (var i = 0; i < data.brand_list.length; i++) {
					me.proBrandPickerArray.push(data.brand_list[i].brand_name)
				}
			}

			if (data.freight_list) {
				me.proFreightPickerArray = []
				for (var i = 0; i < data.freight_list.length; i++) {
					me.proFreightPickerArray.push(data.freight_list[i].name)
				}
			}

			if (data.distributors) {
				for (var i = 0; i < data.distributors.length; i++) {
					me.proDistributorsPickerArray.push(data.distributors[i].name)
				}
			}

			me.proUnitPickerArray = data.unit
			if (data.list.is_hexiao == 0) {
				me.downLineStatus = false
			} else if (data.list.is_hexiao == 1) {
				me.downLineStatus = true
			}
			me.downLineAdd = data.list.hxaddress

			setTimeout(function() {
				me.loadFlag = true
			}, 300)
		} else {
			uni.showToast({
				title: message,
				duration: 1500,
				icon: 'none'
			})
		}
	})
}

export function parseRich(me, richList, style) {
	for (let i = 0; i < richList.length; i++) {
		if (richList[i].children) {
			if (richList[i].name == 'strong' || richList[i].name == 'em') {
				let style1 = ''
				if (richList[i].name == 'em') {
					style1 = 'font-style: italic;'
				}
				if (richList[i].name == 'strong') {
					style1 = 'font-weight: bold;'
				}
				if (style) {
					style += style1
				} else {
					style = style1
				}
				parseRich(me, richList[i].children, style)
			} else {
				parseRich(me, richList[i].children)
			}

		} else {

			if (richList[i].name == 'img') {
				richList[i].name = 'image'
			}

			if (richList[i].name && richList[i].name != 'p' && richList[i].name != 'div' && richList[i].name !=
				'span') {
				if (richList[i].attrs) {
					me.richList.push({
						tagType: richList[i].name,
						value: richList[i].attrs.src,
						style: richList[i].attrs.style
					})
				}
			}

			if (richList[i].name == 'p' || richList[i].name == 'div' || richList[i].name == 'span' || richList[i]
				.type == 'text' || richList[i].text) {

				me.richList.push({
					tagType: 'p',
					value: richList[i].text,
					style: style
				})
			}

		}

	}

}

// 查看商品详情
export function LaiKeTui_see(me) {
	let data = {
		// module: 'app',
		// action: 'mch',
		// m: 'modify',
		api: "mch.App.Mch.Modify",
		shop_id: me.shop_id,
		p_id: me.p_id
	}

	if (me.status == 'seePro') {
		data.m = 'see'
	}

	me.$req.post({
		data
	}).then(res => {
		let {
			code,
			data,
			message
		} = res
		setTimeout(() => {
			uni.hideLoading();
			me.isLoading = false
		}, 1000)
		if (code == 200) {

			// 虚拟商品
			if (data.list.commodity_type == 1) {
				me.goodsType = data.list.commodity_type
				me.xnHxType = data.list.write_off_settings
				me.xnHxTime = data.list.is_appointment == 2 ? true : false
				me.xnHxShopId = data.list.write_off_mch_ids == '0' ? 0 : data.list.write_off_mch_ids
				me.xnHxShopName = data.write_off_mch_names
			}
			me.countrySet = {
				name: res.data.country_name,
				num3: res.data.country_num
			}
			// 虚拟商品结束
			me.showContent = true
			me.video = data.video
			me.proVideo = data.proVideo
			me.proClass = data.product_class_list1
			me.chooseClass = data.product_class_list1
			me.type1html = '<video src="' + me.video +
				'" style="width:100%;height:100%;border-radius:8px" :controls="false" poster="' + me.video +
				'?x-oss-process=video/snapshot,t_0,f_jpg"></video>';
			me.type1html2 = '<video src="' + me.proVideo +
				'" style="width:100%;height:100%;border-radius:8px" :controls="false" poster="' + me.proVideo +
				'?x-oss-process=video/snapshot,t_0,f_jpg"></video>';
			me.proClassId = data.product_class_list1.cid

			if (data.list.content) {
				try {
					let IntroList2 = JSON.parse(data.list.content)
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
					me.isGoodsAdd = IntroList2
					if (me.isGoodsAdd.length <= 0) {

						if (me.lang_text == 'zh_CN') {
							me.isGoodsAdd = [{
								content: '暂无商品详情',
								pname: '商品详情'
							}]
						} else {
							me.isGoodsAdd = [{
								content: 'No product details currently available',
								pname: 'Product details'
							}]
						}
					}
				} catch (error) {
					me.isGoodsAdd = [{
						content: data.list.content,
						pname: me.language.uploadPro.xq
					}]
				}
			}

			me.seeGoods = me.isGoodsAdd[0]

			if (data.product_class_list1.length > 1) {
				me.proClassId = data.product_class_list1[1].cid
			}

			me.proBrand = data.brand_class_list1
			me.proBrandId = data.brand_class_list1.brand_id
			me.unit = data.initial.unit
			me.proName = data.list.product_title
			me.vstName = data.list.subtitle
			me.keyWord = data.list.keyword
			me.proWegiht = data.list.weight //不需要kg
			me.proCode = data.list.scan
			me.showImg = data.imgurls
			me.cover_map = data.cover_map
			me.arrimg = data.initial.attrImg

			me.showImgOld = data.imgurls
			me.$store.state.attr_group = data.attr
			me.$store.state.attr_arr = data.attrList
			me.attr_group = data.attr
			me.attr_arr = data.attrList

			me.freightSet = data.freight_list1
			me.s_type = data.s_type
			me.plugin_list = data.plugin_list
			me.freightSetId = data.freight_list1.id
			me.show_adr = data.show_adr

			me.s_typeShow = ''
			for (var i in data.s_type) {
				if (data.s_type[i].status) {
					me.s_typeStr.push(data.s_type[i].value)
					me.s_typeShow += ',' + data.s_type[i].name
				}
			}
			for (var i in data.plugin_list.active) {
				if (data.plugin_list.active[i].status) {
					me.active.push(data.plugin_list.active[i].value)
				}
			}

			me.show_adrShow = ''
			for (var i in me.show_adr) {
				if (data.show_adr[i].status) {
					me.show_adrShow += ',' + data.show_adr[i].name
				}
			}

			me.s_typeShow = me.s_typeShow.replace(',', '')
			me.show_adrShow = me.show_adrShow.replace(',', '')
			if (!me.show_adrShow) {
				me.show_adrShow = '全部商品'
			}
			if (data.list.is_hexiao == 0) {
				me.downLineStatus = false
			} else if (data.list.is_hexiao == 1) {
				me.downLineStatus = true
			}
			me.downLineAdd = data.list.hxaddress
			// me.content = data.content
			if (data.richList) {
				try {
					const richList = JSON.parse(data.richList)
					me.richList = Array.isArray(richList) ? richList : []
					me.$store.state.goodsDetail = Array.isArray(richList) ? richList : []
				} catch (error) {
					me.richList = []
					me.$store.state.goodsDetail = []
				}
			} else {
				me.richList = []
				me.$store.state.goodsDetail = []
			}
			// if (me.content != '') {
			//     me.content = me.content.replace(new RegExp('<image', 'gm'), '<img').replace(new RegExp(
			//         '</image>', 'gm'), '')
			// } else {
			//     me.content = ' '
			// }

			setTimeout(function() {
				me.loadFlag = true
			}, 300)
		} else {
			uni.showToast({
				title: message,
				duration: 1500,
				icon: 'none'
			})
		}
	})
}

// 直播店铺获取商品详情
export function LaiKeTui_ZBsee(me) {
	let data = {
		// module: 'app',
		// action: 'mch',
		// m: 'modify',
		// api: "mch.App.Mch.Modify",
		// api:"plugin.living.AppAnchorProduct.Modify",
		api: "plugin.living.AppMchProduct.Modify",
		shop_id: me.shop_id,
		p_id: me.p_id,
	}

	if (me.status == 'seePro') {
		data.m = 'see'
	}

	me.$req.post({
		data
	}).then(res => {
		let {
			code,
			data,
			message
		} = res

		if (code == 200) {
			me.showContent = true
			me.video = data.video
			me.proVideo = data.proVideo
			me.proClass = data.product_class_list1
			me.chooseClass = data.product_class_list1
			me.type1html = '<video src="' + me.video +
				'" style="width:100%;height:100%;border-radius:8px" :controls="false" poster="' + me.video +
				'?x-oss-process=video/snapshot,t_0,f_jpg"></video>';
			me.type1html2 = '<video src="' + me.proVideo +
				'" style="width:100%;height:100%;border-radius:8px" :controls="false" poster="' + me.proVideo +
				'?x-oss-process=video/snapshot,t_0,f_jpg"></video>';
			me.proClassId = data.product_class_list1.cid

			if (data.list.content) {
				try {
					let IntroList2 = JSON.parse(data.list.content)
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
					me.isGoodsAdd = IntroList2
					if (me.isGoodsAdd.length <= 0) {
						// me.isGoodsAdd = [{
						//     content: me.language.uploadPro.zwxq,
						//     pname: me.language.uploadPro.xq
						// }]
						if (me.lang_text == 'zh_CN') {
							me.isGoodsAdd = [{
								content: '暂无商品详情',
								pname: '商品详情'
							}]
						} else {
							me.isGoodsAdd = [{
								content: 'No product details currently available',
								pname: 'Product details'
							}]
						}
					}
				} catch (error) {
					me.isGoodsAdd = [{
						content: data.list.content,
						pname: me.language.uploadPro.xq
					}]
				}
			}
			// me.content = data.content
			// me.isGoodsAdd = JSON.parse(me.content)
			// me.isGoodsAdd.forEach((item,index)=>{
			//     item.pname = item.name
			// })
			me.seeGoods = me.isGoodsAdd[0]

			if (data.product_class_list1.length > 1) {
				me.proClassId = data.product_class_list1[1].cid
			}

			me.proBrand = data.brand_class_list1
			me.proBrandId = data.brand_class_list1.brand_id
			me.unit = data.initial.unit
			me.proName = data.list.product_title
			me.vstName = data.list.subtitle
			me.keyWord = data.list.keyword
			// me.proWegiht = parseInt(data.list.weight) + 'kg'//不需要四舍五入
			// me.proWegiht = data.list.weight + 'kg'
			me.proWegiht = data.list.weight //不需要kg
			me.proCode = data.list.scan
			me.showImg = data.imgurls
			me.cover_map = data.cover_map
			me.arrimg = data.initial.attrImg
			// me.receivingForm=[]
			// if(data.list.receiving_form&&data.list.receiving_form.length>1){
			//     me.receivingForm=data.list.receiving_form.split(',')//配送方式
			// }else{
			//     me.receivingForm.push(data.list.receiving_form)
			// }
			me.showImgOld = data.imgurls
			me.$store.state.attr_group = data.attr
			me.$store.state.attr_arr = data.attrList
			me.attr_group = data.attr
			me.attr_arr = data.attrList
			// if(me.attr_arr){
			//     me.arrimg = me.attr_arr[0].img
			// }
			me.freightSet = data.freight_list1
			me.s_type = data.s_type
			me.plugin_list = data.plugin_list
			me.freightSetId = data.freight_list1.id
			me.show_adr = data.show_adr

			me.s_typeShow = ''
			for (var i in data.s_type) {
				if (data.s_type[i].status) {
					me.s_typeStr.push(data.s_type[i].value)
					me.s_typeShow += ',' + data.s_type[i].name
				}
			}
			for (var i in data.plugin_list.active) {
				if (data.plugin_list.active[i].status) {
					me.active.push(data.plugin_list.active[i].value)
				}
			}

			me.show_adrShow = ''
			for (var i in me.show_adr) {
				if (data.show_adr[i].status) {
					me.show_adrShow += ',' + data.show_adr[i].name
				}
			}

			me.s_typeShow = me.s_typeShow.replace(',', '')
			me.show_adrShow = me.show_adrShow.replace(',', '')
			if (!me.show_adrShow) {
				me.show_adrShow = '全部商品'
			}
			if (data.list.is_hexiao == 0) {
				me.downLineStatus = false
			} else if (data.list.is_hexiao == 1) {
				me.downLineStatus = true
			}
			me.downLineAdd = data.list.hxaddress
			// me.content = data.content
			if (data.richList) {
				try {
					const richList = JSON.parse(data.richList)
					me.richList = Array.isArray(richList) ? richList : []
					me.$store.state.goodsDetail = Array.isArray(richList) ? richList : []
				} catch (error) {
					me.richList = []
					me.$store.state.goodsDetail = []
				}
			} else {
				me.richList = []
				me.$store.state.goodsDetail = []
			}
			// if (me.content != '') {
			//     me.content = me.content.replace(new RegExp('<image', 'gm'), '<img').replace(new RegExp(
			//         '</image>', 'gm'), '')
			// } else {
			//     me.content = ' '
			// }

			setTimeout(function() {
				me.loadFlag = true
			}, 300)
		} else {
			uni.showToast({
				title: message,
				duration: 1500,
				icon: 'none'
			})
		}
	})
}

// 限时折扣商品详情
// 直播店铺获取商品详情
export function LaiKeTui_ZKsee(me) {
	let data = {
		// module: 'app',
		// action: 'mch',
		// m: 'modify',
		// api: "mch.App.Mch.Modify",
		// api:"plugin.living.AppAnchorProduct.Modify",
		// api:"plugin.Mch.flashsale.getProDetail", 
		api: 'plugin.flashsale.AppMchFlashSale.getProDetail',
		// shop_id: me.shop_id,
		goodId: me.p_id
	}

	if (me.status == 'seePro') {
		data.m = 'see'
	}

	me.$req.post({
		data
	}).then(res => {
		let {
			code,
			data,
			message
		} = res

		if (code == 200) {
			me.showContent = true
			me.video = data.video
			me.proVideo = data.proVideo
			me.proClass = data.product_class_list1
			me.chooseClass = data.product_class_list1
			me.type1html = '<video src="' + me.video +
				'" style="width:100%;height:100%;border-radius:8px" :controls="false" poster="' + me.video +
				'?x-oss-process=video/snapshot,t_0,f_jpg"></video>';
			me.type1html2 = '<video src="' + me.proVideo +
				'" style="width:100%;height:100%;border-radius:8px" :controls="false" poster="' + me.proVideo +
				'?x-oss-process=video/snapshot,t_0,f_jpg"></video>';
			me.proClassId = data.product_class_list1.cid

			if (data.list.content) {
				try {
					let IntroList2 = JSON.parse(data.list.content)
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
					me.isGoodsAdd = IntroList2
					if (me.isGoodsAdd.length <= 0) {
						// me.isGoodsAdd = [{
						//     content: me.language.uploadPro.zwxq,
						//     pname: me.language.uploadPro.xq
						// }]
						if (me.lang_text == 'zh_CN') {
							me.isGoodsAdd = [{
								content: '暂无商品详情',
								pname: '商品详情'
							}]
						} else {
							me.isGoodsAdd = [{
								content: 'No product details currently available',
								pname: 'Product details'
							}]
						}
					}
				} catch (error) {
					me.isGoodsAdd = [{
						content: data.list.content,
						pname: me.language.uploadPro.xq
					}]
				}
			}

			me.seeGoods = me.isGoodsAdd[0]

			if (data.product_class_list1.length > 1) {
				me.proClassId = data.product_class_list1[1].cid
			}

			me.proBrand = data.brand_class_list1
			me.proBrandId = data.brand_class_list1.brand_id
			me.unit = data.initial.unit
			me.proName = data.list.product_title
			me.vstName = data.list.subtitle
			me.keyWord = data.list.keyword
			me.proWegiht = data.list.weight //不需要kg
			me.proCode = data.list.scan
			me.showImg = data.imgurls
			me.cover_map = data.cover_map
			me.arrimg = data.initial.attrImg

			me.showImgOld = data.imgurls
			me.$store.state.attr_group = data.attr
			me.$store.state.attr_arr = data.attrList
			me.attr_group = data.attr
			me.attr_arr = data.attrList

			me.freightSet = data.freight_list1
			me.s_type = data.s_type
			me.plugin_list = data.plugin_list
			me.freightSetId = data.freight_list1.id
			me.show_adr = data.show_adr

			me.s_typeShow = ''
			for (var i in data.s_type) {
				if (data.s_type[i].status) {
					me.s_typeStr.push(data.s_type[i].value)
					me.s_typeShow += ',' + data.s_type[i].name
				}
			}
			for (var i in data.plugin_list.active) {
				if (data.plugin_list.active[i].status) {
					me.active.push(data.plugin_list.active[i].value)
				}
			}

			me.show_adrShow = ''
			for (var i in me.show_adr) {
				if (data.show_adr[i].status) {
					me.show_adrShow += ',' + data.show_adr[i].name
				}
			}

			me.s_typeShow = me.s_typeShow.replace(',', '')
			me.show_adrShow = me.show_adrShow.replace(',', '')
			if (!me.show_adrShow) {
				me.show_adrShow = '全部商品'
			}
			if (data.list.is_hexiao == 0) {
				me.downLineStatus = false
			} else if (data.list.is_hexiao == 1) {
				me.downLineStatus = true
			}
			me.downLineAdd = data.list.hxaddress
			// me.content = data.content
			if (data.richList) {
				try {
					const richList = JSON.parse(data.richList)
					me.richList = Array.isArray(richList) ? richList : []
					me.$store.state.goodsDetail = Array.isArray(richList) ? richList : []
				} catch (error) {
					me.richList = []
					me.$store.state.goodsDetail = []
				}
			} else {
				me.richList = []
				me.$store.state.goodsDetail = []
			}
			setTimeout(function() {
				me.loadFlag = true
			}, 300)
		} else {
			uni.showToast({
				title: message,
				duration: 1500,
				icon: 'none'
			})
		}
	})
}

export function LaiKeTui_ZBseeTwo(me) {
	let data = {
		// module: 'app',
		// action: 'mch',
		// m: 'modify',
		// api: "mch.App.Mch.Modify",
		// api:"plugin.living.AppAnchorProduct.Modify",
		api: "plugin.living.AppAnchorProduct.Modify",
		// shop_id: me.shop_id,
		p_id: me.p_id,
		roomId: me.zb_id
	}

	if (me.status == 'seePro') {
		data.m = 'see'
	}

	me.$req.post({
		data
	}).then(res => {
		let {
			code,
			data,
			message
		} = res

		if (code == 200) {
			me.showContent = true
			me.video = data.video
			me.proVideo = data.proVideo
			me.proClass = data.product_class_list1
			me.chooseClass = data.product_class_list1
			me.type1html = '<video src="' + me.video +
				'" style="width:100%;height:100%;border-radius:8px" :controls="false" poster="' + me.video +
				'?x-oss-process=video/snapshot,t_0,f_jpg"></video>';
			me.type1html2 = '<video src="' + me.proVideo +
				'" style="width:100%;height:100%;border-radius:8px" :controls="false" poster="' + me.proVideo +
				'?x-oss-process=video/snapshot,t_0,f_jpg"></video>';
			me.proClassId = data.product_class_list1.cid

			if (data.list.content) {
				try {
					let IntroList2 = JSON.parse(data.list.content)
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
					me.isGoodsAdd = IntroList2
					if (me.isGoodsAdd.length <= 0) {
						// me.isGoodsAdd = [{
						//     content: me.language.uploadPro.zwxq,
						//     pname: me.language.uploadPro.xq
						// }]
						if (me.lang_text == 'zh_CN') {
							me.isGoodsAdd = [{
								content: '暂无商品详情',
								pname: '商品详情'
							}]
						} else {
							me.isGoodsAdd = [{
								content: 'No product details currently available',
								pname: 'Product details'
							}]
						}
					}
				} catch (error) {
					me.isGoodsAdd = [{
						content: data.list.content,
						pname: me.language.uploadPro.xq
					}]
				}
			}
			// me.content = data.content
			// me.isGoodsAdd = JSON.parse(me.content)
			// me.isGoodsAdd.forEach((item,index)=>{
			//     item.pname = item.name
			// })
			me.seeGoods = me.isGoodsAdd[0]

			if (data.product_class_list1.length > 1) {
				me.proClassId = data.product_class_list1[1].cid
			}

			me.proBrand = data.brand_class_list1
			me.proBrandId = data.brand_class_list1.brand_id
			me.unit = data.initial.unit
			me.proName = data.list.product_title
			me.vstName = data.list.subtitle
			me.keyWord = data.list.keyword
			// me.proWegiht = parseInt(data.list.weight) + 'kg'//不需要四舍五入
			// me.proWegiht = data.list.weight + 'kg'
			me.proWegiht = data.list.weight //不需要kg
			me.proCode = data.list.scan
			me.showImg = data.imgurls
			me.cover_map = data.cover_map
			me.arrimg = data.initial.attrImg
			// me.receivingForm=[]
			// if(data.list.receiving_form&&data.list.receiving_form.length>1){
			//     me.receivingForm=data.list.receiving_form.split(',')//配送方式
			// }else{
			//     me.receivingForm.push(data.list.receiving_form)
			// }
			me.showImgOld = data.imgurls
			me.$store.state.attr_group = data.attr
			me.$store.state.attr_arr = data.attrList
			me.attr_group = data.attr
			me.attr_arr = data.attrList
			// if(me.attr_arr){
			//     me.arrimg = me.attr_arr[0].img
			// }
			me.freightSet = data.freight_list1
			me.s_type = data.s_type
			me.plugin_list = data.plugin_list
			me.freightSetId = data.freight_list1.id
			me.show_adr = data.show_adr

			me.s_typeShow = ''
			for (var i in data.s_type) {
				if (data.s_type[i].status) {
					me.s_typeStr.push(data.s_type[i].value)
					me.s_typeShow += ',' + data.s_type[i].name
				}
			}
			for (var i in data.plugin_list.active) {
				if (data.plugin_list.active[i].status) {
					me.active.push(data.plugin_list.active[i].value)
				}
			}

			me.show_adrShow = ''
			for (var i in me.show_adr) {
				if (data.show_adr[i].status) {
					me.show_adrShow += ',' + data.show_adr[i].name
				}
			}

			me.s_typeShow = me.s_typeShow.replace(',', '')
			me.show_adrShow = me.show_adrShow.replace(',', '')
			if (!me.show_adrShow) {
				me.show_adrShow = '全部商品'
			}
			if (data.list.is_hexiao == 0) {
				me.downLineStatus = false
			} else if (data.list.is_hexiao == 1) {
				me.downLineStatus = true
			}
			me.downLineAdd = data.list.hxaddress
			// me.content = data.content
			if (data.richList) {
				try {
					const richList = JSON.parse(data.richList)
					me.richList = Array.isArray(richList) ? richList : []
					me.$store.state.goodsDetail = Array.isArray(richList) ? richList : []
				} catch (error) {
					me.richList = []
					me.$store.state.goodsDetail = []
				}
			} else {
				me.richList = []
				me.$store.state.goodsDetail = []
			}
			// if (me.content != '') {
			//     me.content = me.content.replace(new RegExp('<image', 'gm'), '<img').replace(new RegExp(
			//         '</image>', 'gm'), '')
			// } else {
			//     me.content = ' '
			// }

			setTimeout(function() {
				me.loadFlag = true
			}, 300)
		} else {
			uni.showToast({
				title: message,
				duration: 1500,
				icon: 'none'
			})
		}
	})
}

// 获取分类
export function LaiKeTui_showProClass(me) {

	let data = {
		// action: 'mch',
		// module: 'app',
		// m: 'get_class',
		api: "mch.App.Mch.Get_class",
		shop_id: me.shop_id,
		class_str: me.proClassId,
		brand_str: me.proBrandId,
	}
	me.$req.post({
		data
	}).then(res => {
		let {
			code,
			data,
			message
		} = res

		if (code == 200) {
			me.chooseClassFlag = true

			me.chooseClass = me.chooseClass.filter(item => {
				return item.pname != "请选择"
			})

			if (me.chooseClass.length == 0) {
				me.chooseClass.push({
					pname: me.language.uploadPro.choice
				})
			} else {

				if (data.list.class_list && data.list.class_list[0]) {

					let flag = data.list.class_list[0].some(item => {
						return me.chooseClass[me.chooseClass.length - 1].pname == item.pname
					})

					if (!flag) {
						me.chooseClass.push({
							pname: me.language.uploadPro.choice
						})
					}

				}

			}

			if (data.list.class_list.length != 0) {
				if (data.list.class_list[0] && me.chooseClass[0].pname == me.language.uploadPro.choice) {
					data.list.class_list[0].filter(item => {
						item.status = false
					})
				}
				me.arrClass = data.list.class_list[0]
			}

			me.res.brand_class_list = data.list.brand_list.filter(item => Number(item.notset) !== 1);
			me.proBrandPickerArray = []
			for (var i = 0; i < data.list.brand_list.length; i++) {
				me.proBrandPickerArray.push(data.list.brand_list[i].brand_name)
			}
		} else {
			uni.showToast({
				title: message,
				duration: 1500,
				icon: 'none'
			})
		}
	})
}

// 选择分类
export function LaiKeTui_onConfirmProClass(me, item) {
	let data = {
		// action: 'mch',
		// module: 'app',
		// m: 'choice_class',
		api: "mch.App.Mch.Choice_class",
		shop_id: me.shop_id,
		cid: item.cid,
		brand_str: me.proBrandId,
	}
	me.$req.post({
		data
	}).then(res => {
		let {
			code,
			data,
			message
		} = res



		if (code == 200) {
			var chooseClass_num = me.chooseClass.length - 1
			me.proClassId = item.cid
			me.proClass = item.pname

			if (me.chooseClass[chooseClass_num].pname == me.language.uploadPro.choice) {
				me.chooseClass[chooseClass_num] = item
				if (data.list.class_list.length == 0) {
					me.chooseClassFlag = false
				} else {
					me.arrClass = data.list.class_list[0]
					me.chooseClass.push({
						pname: me.language.uploadPro.choice
					})
				}
			}
			me.proBrandPickerArray = []
			me.res.brand_class_list = data.list.brand_list.filter(item => Number(item.notset) !== 1);
			var proBrandId = me.proBrandId
			me.proBrand = ''
			me.proBrandId = '0'
			for (var i = 0; i < data.list.brand_list.length; i++) {
				if (proBrandId == data.list.brand_list[i].brand_id) {
					me.proBrandId = data.list.brand_list[i].brand_id
					me.proBrand = data.list.brand_list[i].brand_name
				}
				me.proBrandPickerArray.push(data.list.brand_list[i].brand_name)
			}

		} else {
			uni.showToast({
				title: message,
				duration: 1500,
				icon: 'none'
			})
		}
	})

}

// 选择品牌
export function LaiKeTui_showProBrand(me) {
	me.proBrandShow = true
	me.mode = 'selector'
	me.deepLength = 1
	me.proBrandPickerDefault = [0]
	var timing
	setTimeout(() => {
		timing = me.$refs.proBrandPicker
	}, 10)
	setTimeout(() => {
		timing.show()
	}, 100)
}

// 点击品牌
export function LaiKeTui_onConfirmProBrand(me, e) {
	if (me.pageStatus == 1) {
		me.proBrand = {
			brand_name: e.label
		}
	} else {
		me.proBrand = e.label
	}
	me.show = false
	if (me.res.brand_class_list[e.index[0]]) {
		me.proBrandId = me.res.brand_class_list[e.index[0]].brand_id
	}

}

// 删除图片
export function LaiKeTui_delImg(me, index) {
	if (in_array(me.showImg[index], me.showImgNewAdd)) {
		var a1 = me.showImgNewAdd.indexOf(me.showImg[index])
		me.showImgNewAdd.splice(a1, 1)
	}
	if (in_array(me.showImg[index], me.showImgOld)) {
		var a1 = me.showImgOld.indexOf(me.showImg[index])
		me.showImgOld.splice(a1, 1)
	}
	me.showImg.splice(index, 1)
}

// 删除封面图片
export function LaiKeTui_delImg2(me) {
	me.cover_map = '';
}
// 删除封面图片
export function LaiKeTui_delImg3(me) {
	me.arrimg = '';
}
// 删除展示视频
export function LaiKeTui_delVideo(me) {
	me.video = '';
}
// 删除展示视频
export function LaiKeTui_delVideo2(me) {
	me.proVideo = '';
}
// 设为主图
export function LaiKeTui_setMain(me, index) {
	var img = me.showImg[index]
	me.showImg.splice(index, 1)
	me.showImg.unshift(img)
	//将新图上传为主图
	if (index >= me.showImgOld.length) {
		var index1 = index - me.showImgOld.length
		var img1 = me.showImgNewAdd[index1]
		me.showImgNewAdd.splice(index1, 1)
		me.showImgNewAdd.unshift(img1)
	}
}

// 选择图片
export function LaiKeTui_chooseImg(me, num) {
	var count = me.upImgNum[num]
	// #ifdef MP-WEIXIN
	uni.chooseMedia({
		count: count,
		mediaType: ['image'],
		success: res => {
			let tempFilePaths = []
			res.tempFiles.forEach(item => {
				tempFilePaths.push(item.tempFilePath)
			})

			uploadPictures(tempFilePaths, me)
		},
	})
	// #endif

	// #ifndef MP-WEIXIN
	uni.chooseImage({
		count: count,
		success: res => {
			let tempFilePaths = res.tempFilePaths
			uploadPictures(tempFilePaths, me)
		},
	})
	// #endif    
}

export async function uploadPictures(res, me) {
	let list = []
	if (me.pageStatus == 1) {
		list = await getImageList(res, me)
		me.showImgNewAdd = me.showImgNewAdd.concat(list)
	} else {
		list = await getImageList(res, me)
	}
	me.showImg = me.showImg.concat(list)
}

// 选择图片
export function LaiKeTui_chooseImg2(me, isRef = false, size = 500) {
	// #ifdef MP-WEIXIN
	uni.chooseMedia({
		count: 1,
		mediaType: ['image'],
		success: res => {
			let type = res.type
			let filesSize = res.tempFiles[0].size
			let tempFilePaths = []
			res.tempFiles.forEach(item => {
				tempFilePaths.push(item.tempFilePath)
			})
			uploadPictures2(tempFilePaths, type, filesSize, me, isRef = false, size)
		},
	})
	// #endif

	// #ifndef MP-WEIXIN
	uni.chooseImage({
		count: 1,
		success: res => {
			let type = res.type
			let filesSize = res.tempFilePaths[0].size
			let tempFilePaths = res.tempFilePaths
			uploadPictures2(tempFilePaths, type, filesSize, me, isRef = false, size)
		},
	})
	// #endif
}
export async function uploadPictures2(res, type, filesSize, me, isRef = false, size) {
	let list = await getImageList(res, me)
	if (isRef) {
		if (type.indexOf('jpg') && type.indexOf('png') == -1 && type.indexOf('jpeg') == -1) {
			uni.showToast({
				title: '不支持该文件类型',
				duration: 1000,
				icon: 'none'
			})
			// 5120 
		} else if (filesSize > (size * 1024)) {
			uni.showToast({
				title: '文件大小不能超过' + size + "kb",
				duration: 1000,
				icon: 'none'
			})
		} else {
			me.cover_map = list[0]
		}
	} else {
		me.cover_map = list[0]
	}
}

// 选择图片
export function LaiKeTui_chooseImg4(me) {
	// #ifdef MP-WEIXIN
	uni.chooseMedia({
		count: 1,
		mediaType: ['image'],
		success: async (res) => {
			let tempFilePaths = []
			res.tempFiles.forEach(item => {
				tempFilePaths.push(item.tempFilePath)
			})
			let list = await getImageList(tempFilePaths, me)
			me.arrimg = list[0]
		},
	})
	// #endif

	// #ifndef MP-WEIXIN
	uni.chooseImage({
		count: 1,
		success: async (res) => {
			let list = await getImageList(res.tempFilePaths, me)
			me.arrimg = list[0]
		},
	})
	// #endif
}
// 选择展示视频
export function LaiKeTui_chooseVideo(me) {
	uni.chooseVideo({
		compressed: false,
		success: async (res) => {
			let list = await getImageList([res.tempFilePath], me)
			me.video = list[0]
		}
	})
}
// 选择商品视频
export function LaiKeTui_chooseVideo2(me) {
	uni.chooseVideo({
		compressed: false,
		success: async (res) => {
			let list = await getImageList([res.tempFilePath], me)
			me.proVideo = list[0]
		}
	})
}
// 选择图片
export function LaiKeTui_chooseImg3(me) {
	uni.chooseImage({
		count: 9,
		success: async (res) => {
			let list = await getImageList(res.tempFilePaths, me)
			me.cover_mapa = list
			var lista = me.cover_mapa
			this.$emit('imgs', lista)
		}
	})
}

export async function getImageList(tempFilePaths, me) {
	let list = tempFilePaths
	uni.showLoading({
		title: me.language.showLoading.upLoading,
		mask: true
	})
	for (let key of list.keys()) {
		let res = await me.$req.upLoad(list[key])
		list[key] = res.data
	}
	uni.hideLoading()
	return Promise.resolve(list);
}

// 成本价
export function LaiKeTui_cbj(me, e) {
	if (me.status == 'editor') {
		for (var i = 0; i < me.attr_arr.length; i++) {
			me.attr_arr[i].cbj = e.detail.value
		}
	}
}
// 原价
export function LaiKeTui_yj(me, e) {
	if (me.status == 'editor') {
		for (var i = 0; i < me.attr_arr.length; i++) {
			me.attr_arr[i].yj = e.detail.value
		}
	}
}

// 虚拟销量
export function LaiKeTui_volume(me, e) {
	if (me.status == 'editor') {
		for (var i = 0; i < me.attr_arr.length; i++) {
			me.attr_arr[i].volume = e.detail.value
		}
	}
}

// 售价
export function LaiKeTui_sj(me, e) {
	if (me.status == 'editor') {
		for (var i = 0; i < me.attr_arr.length; i++) {
			me.attr_arr[i].sj = e.detail.value
		}
	}
}

// 库存
export function LaiKeTui_kc(me, e) {
	if (me.status == 'editor') {
		for (var i = 0; i < me.attr_arr.length; i++) {
			me.attr_arr[i].kucun = e.detail.value
		}
	}
}

// 库存预警
export function LaiKeTui_kcyj(me, e) {
	if (me.status == 'editor') {
		for (var i = 0; i < me.attrList.length; i++) {
			me.attrList[i].stockWarn = e.detail.value
		}
	}
}

// 显示单位
export function LaiKeTui_showProUnit(me) {
	me.proUnitShow = true
	me.mode = 'selector'
	me.deepLength = 1
	me.proUnitPickerDefault = [0]
	var timing
	setTimeout(() => {
		timing = me.$refs.proUnitPicker
	}, 10)
	setTimeout(() => {
		timing.show()
	}, 100)
}

// 设置属性
export function LaiKeTui_setAttr(me) {
	// pageStatus:'',//[0-上传，1-编辑，2-查看]
	/**
	 * costM 成本价
	 * oldM  原价
	 * sellM  售价
	 * stock  库存
	 * stockWarn  库存预警
	 * attr_group 属性组
	 */
	// 检查基本信息是否完整
	const isBasicInfoComplete = me.costM && me.oldM && me.sellM;
	// 检查库存信息是否有效（实物商品）
	const isStockInfoValid = me.goodsType !== 1 ? (me.stock && me.stockWarn) : true;
	// 发布时候检查类型
	const isPublish = me.pageStatus == 0 && me.type != 'yushou' && me.type != 'gys';
	// 虚拟商品不能编辑属性
	const isVirtualEdit = me.attr_group.length <= 0 && me.pageStatus == 1;
	// 检查发布类型
	if (me.goodsType == 1) {
		if (isVirtualEdit) {
			uni.showToast({
				title: me.language.toasts.uploadPro.bnbj,
				duration: 1500,
				icon: 'none'
			});
			return
		}
	} else {
		//提示完善信息
		if (isPublish) {
			if (!isBasicInfoComplete || !isStockInfoValid) {
				uni.showToast({
					title: me.language.toasts.uploadPro.setAttr,
					duration: 1500,
					icon: 'none'
				});
				return;
			}
		} else {
			if (!isBasicInfoComplete) {
				uni.showToast({
					title: me.language.toasts.uploadPro.setAttr,
					duration: 1500,
					icon: 'none'
				});
				return;
			}
		}
	}
	// 构造参数字符串
	const params =
		`costM=${me.costM}&oldM=${me.oldM}&sellM=${me.sellM}&stock=${me.stock}&stockWarn=${me.stockWarn}&unit=${me.unit}&pageStatus=${me.pageStatus}&type=${me.type}&arrimg=${me.arrimg}&goodsType=${me.goodsType}&xnHxType=${me.xnHxType}&yushou_type=${me.yushou_type}` //1-定金模式  2-订货模式;
	// 跳转到设置属性页面
	if (typeof me._saveUploadProDraft === 'function') {
		me._saveUploadProDraft();
	}
	uni.navigateTo({
		url: `../myStore/setAttr?${params}`
	});
}

// 运费
export function LaiKeTui_showProFreight(me) {
	me.proFreightShow = true
	me.mode = 'selector'
	me.deepLength = 1
	me.proFreightPickerDefault = [0]
	setTimeout(function() {
		me.$refs.proFreightPicker.show()
	}, 300)

}

// 点击运费
export function LaiKeTui_onConfirmProFreight(me, e) {
	if (me.pageStatus == 1 || me.pageStatus == 0) {
		me.freightSet = {
			name: e.label
		}
		me.proCountryPickerDefault = [e.index[0]];
	} else {
		me.freightSet = {
			name: e.label
		}
	}
	me.show = false
	me.freightSetId = me.res.freight_list[e.index[0]].id
}

// 国家
export function LaiKeTui_showProCountry(me) {

	me.proCountryPickerArray = [];
	for (let i = 0; i < me.countryList.length; i++) {
		me.proCountryPickerArray.push(me.countryList[i].name)
	}

	me.proCountryShow = true
	me.mode = 'selector'
	me.deepLength = 1
	me.proCountryPickerDefault = [0]

	var timing = null;
	setTimeout(() => {
		timing = me.$refs.proCountryShowPicker.show()
	}, 100)

}

// 点击国家
export function LaiKeTui_onConfirmProCountry(me, e) {
	if (me.pageStatus == 1) {
		me.countrySet.name = e.label
	} else {
		me.countrySet = e.label
	}
	me.show = false
	me.country_num = me.countryList[e.index[0]].num3
}

// 语种
export function LaiKeTui_showProLanguage(me) {

	me.proLanguagePickerArray = [];
	for (let i = 0; i < me.languages.length; i++) {
		me.proLanguagePickerArray.push(me.languages[i].lang_name)
	}

	me.proLanguageShow = true
	me.mode = 'selector'
	me.deepLength = 1
	me.proLanguagePickerDefault = [0]

	var timing = null;
	setTimeout(() => {
		timing = me.$refs.proLanguageShowPicker.show()
	}, 100)

}

// 点击语种
export function LaiKeTui_onConfirmProLanguage(me, e) {
	if (me.pageStatus == 1) {
		me.languageSet.lang_name = e.label
	} else {
		me.languageSet = {
			lang_name: e.label
		}
	}
	me.show = false

	me.lang_code = me.languages[e.index[0]].lang_code
}

// 显示标签
export function LaiKeTui_chooseSType(me, index) {
	//标签选择不能超过3个
	if (me.s_typeStr.length > 2) {
		if (me.s_type[index].status == false) {
			uni.showToast({
				title: '最多只能选择3个标签',
				icon: 'none'
			})
		}
		me.s_type[index].status = false
	} else {
		me.s_type[index].status = !me.s_type[index].status
	}
	//初始化 选中标签
	me.s_typeStr = []
	//选中的标签 存入新数组
	for (var i in me.s_type) {
		var a = me.s_type[i].status
		if (a) {
			me.s_typeStr.push(i)
		}
	}
}

// 支持活动
export function LaiKeTui_changeActive(me, num) {
	if (me.type_status == 1) {
		uni.showToast({
			title: me.language.toasts.uploadPro.changeActive[0],
			duration: 1500,
			icon: 'none'
		})
	} else if (me.type_status == 2) {
		uni.showToast({
			title: me.language.toasts.uploadPro.changeActive[1],
			duration: 1500,
			icon: 'none'
		})
	} else {
		var a = 0
		me.plugin_list.active[num].status = !me.plugin_list.active[num].status
		me.active = me.plugin_list.active[num].value
		for (var i = 0; i < me.plugin_list.active.length; i++) {
			me.plugin_list.active[i].status = false
		}
		me.plugin_list.active[num].status = true
		if (me.plugin_list.active[num].value == 1) {
			me.show_adrStatus = true
			me.distributorsStatus = false
		} else if (me.plugin_list.active[num].value == 5) {
			me.show_adrStatus = false
			me.distributorsStatus = true
		} else {
			me.show_adrStatus = false
			me.distributorsStatus = false
		}
	}
}

// 显示位置
export function LaiKeTui_chooseShowAdr(me, row, index) {
	me.display_position = ''
	me.show_adr = me.show_adr.map(item => {
		if (item.value == row.value) {
			return {
				...item,
				status: true
			};
		} else {
			return {
				...item,
				status: false
			};
		}
	});
	me.display_position = row.value
}

// 等级绑定
export function LaiKeTui_showProDistributors(me) {
	me.proDistributorsShow = true
	me.mode = 'selector'
	me.deepLength = 1
	me.proDistributorsPickerDefault = [0]
	me.$refs.proDistributorsPicker.show()
}

// 点击分销等级
export function LaiKeTui_onConfirmProDistributors(me, e) {
	if (me.pageStatus == 1) {
		me.distributorsSet.name = e.label
	} else {
		me.distributorsSet = e.label
	}
	me.show = false
	me.distributorsSetId = me.res.distributors[e.index[0]].id
}

// 提交
export function LaiKeTui_Check(me, type) {
	if (!me.fastTap) {
		return
	}
	// 虚拟商品 如果没有操作核销设置时 默认是线下核销 并设置 库存预警 为1
	if (!me.stock && me.goodsType == 1) {
		me.stock = 1
		me.stockWarn = 1
	}
	// 初始值
	me.initial = 'cbj=' + me.costM + ',yj=' + me.oldM + ',sj=' + me.sellM + ',kucun=' + me.stock + ',unit=' + me.unit +
		',stockWarn=' + me.stockWarn + ',attrImg=' + me.arrimg
	me.fastTap = false

	// if (me.proName && me.keyWord && me.proWegiht && me.proClass && me.proBrand && me.unit && me.freightSet && me.showImg
	//     .length > 0 && me.attr_arr.length > 0 && me.cover_map) {
	// me.vstName &&
	let noEmpty = me.proName && me.keyWord && me.proClass && me.proBrand && me.unit && me.freightSet && me
		.showImg
		.length > 0 && me.arrimg && me.cover_map;

	if (me.tabKey == 1) {
		// && me.vstName
		noEmpty = me.proName && me.keyWord && me.proClass && me.proBrand && me.unit && me.showImg.length >
			0 && me.arrimg && me.cover_map;
	}
	if (noEmpty) {
		// 商品售价可以比商品原价低。为了后面进行引流 禅道1118
		// if (Number(me.costM) > Number(me.sellM)) {
		//     me.fastTap = true
		//     me.check_Flag = true
		//     uni.showToast({
		//         title: me.language.toasts.uploadPro.cbjFail,
		//         icon: 'none'
		//     })
		//     return
		// }
		// 虚拟商品 不需要 进行库存判断
		if (me.goodsType != 1 && Number(me.stockWarn) >= Number(me.stock)) {
			me.fastTap = true
			me.check_Flag = true
			uni.showToast({
				title: me.language.toasts.uploadPro.fbYs[18],
				icon: 'none'
			})
			return
		}
		if (me.downLineStatus) {
			if (me.downLineAdd) {
				me._request(type)
			} else {
				me.fastTap = true
				me.check_Flag = true
				uni.showToast({
					title: me.language.toasts.uploadPro.check[0],
					duration: 1500,
					icon: 'none'
				})
			}
		} else {
			me._request(type)
		}
	} else {
		uni.showToast({
			title: me.language.toasts.uploadPro.check[1],
			duration: 1500,
			icon: 'none'
		})
		me.fastTap = true
		me.check_Flag = true
	}
}

export function LaiKeTui_request(me, type) {
	uni.showLoading({
		title: me.language.showLoading.waiting,
	})
	var active_str = ''
	var show_adr_str = ''
	var s_type = ''
	for (var j in me.show_adr) {
		if (me.show_adr[j].status) {
			show_adr_str += me.show_adr[j].value + ','
		}
	}
	if (show_adr_str == '') {
		show_adr_str += '3,'
	}
	for (var l in me.plugin_list.active) {
		if (me.plugin_list.active[l].status) {
			active_str = me.plugin_list.active[l].value
		}
	}
	for (var j in me.s_type) {
		if (me.s_type[j].status) {
			s_type += me.s_type[j].value + ','
		}
	}

	//商品详情
	// let content = me.getGoodsDetailsContext()
	let content = me.content
	let richList = JSON.stringify(me.richList)
	// return
	//pageStatus : [0-上传，1-编辑，2-查看]
	for (let i = 0; i < me.isGoodsAdd.length; i++) {
		if (me.isGoodsAdd[i].content === '') {
			uni.showToast({
				title: me.language.uploadPro.tb + me.isGoodsAdd[i].name + me.language.uploadPro.dnrbnwk,
				duration: 1500,
				icon: 'none'
			})
			me.check_Flag = true
			me.fastTap = true
			return
		}
	}
	let formcontent = JSON.stringify(me.IntroList)
	var strList = []
	var attrList = []
	if (me.attr_arr.length <= 0) {
		strList = [{
			"cbj": me.costM,
			"yj": me.oldM,
			"sj": me.sellM,
			"unit": me.unit,
			"kucun": me.stock,
			"img": me.arrimg,
			"bar_code": "",
			"attr_list": [{
				"attr_id": "",
				"attr_name": "默认",
				"attr_group_name": "默认"
			}],
			"cid": ""
		}]
	} else {
		strList = me.attr_arr.forEach(item => {
			Object.assign(item, {
				unit: me.unit
			})
		})
		strList = me.attr_arr
	}
	if (me.attr_group.length <= 0) {
		attrList = [{
			"attr_group_name": "默认",
			"attr_list": [{
				"attr_name": "默认"
			}]
		}]
	} else {
		attrList = me.attr_group
	}
	if (me.pageStatus == 0) {
		var data = {
			shop_id: me.shop_id,
			cover_map: me.cover_map,
			showImg: me.showImg,
			// m: 'upload_merchandise',
			// module: 'app',
			// action: 'mch',
			api: "mch.App.Mch.Upload_merchandise",
			p_id: me.p_id,
			product_title: me.proName,
			subtitle: me.vstName,
			scan: me.proCode,
			product_class_id: me.proClassId,
			brand_id: me.proBrandId,
			keyword: me.keyWord,
			weight: me.proWegiht,
			attr_group: JSON.stringify(attrList),
			attr_arr: JSON.stringify(strList),
			freight_id: me.freightSetId,
			display_position: show_adr_str,
			s_type: s_type,
			active: active_str,
			distributor_id: me.distributorsSetId,
			is_hexiao: me.downLineStatus,
			hxaddress: me.downLineAdd || '',
			unit: me.unit,
			// receivingForm:me.receivingForm.toString(),//配送方式1.邮寄 2.自提
			stockWarn: me.stockWarn,
			initial: me.initial,
			volume: me.LaiKeTuiCommon.isempty(me.vshop) ? 0 : me.vshop,
			content: content,
			richList: richList,
			video: me.video,
			proVideo: me.proVideo
		}
		if (me.goodsType == 1) {
			data.commodity_type = 1
			data.write_off_settings = me.xnHxType
			data.is_appointment = me.xnHxTime ? 2 : 1
			data.write_off_mch_ids = me.xnHxShopId
			data.freight_id = -1
		}
		if (type == 'no') {
			data.mch_status = 4
		} else if (type == 'yes') {
			data.mch_status = 1
		}

		//国家
		if (me.country_num) {
			data.country_num = me.country_num;
		}

		//语种
		if (me.lang_code) {
			data.lang_code = me.lang_code;
		}

		me.$req.post({
			data
		}).then(res => {
			if (res.code == 200) {

				uni.hideLoading()
				me.is_sus = true
				setTimeout(function() {
					me.is_sus = false
					uni.navigateBack({
						delta: 1
					})
					me.attr_group = []
					me.attr_arr = []
				}, 1500)
			} else {
				me.fastTap = true
				me.check_Flag = true
				uni.showToast({
					title: res.message,
					duration: 1500,
					icon: 'none'
				})
			}
		})
	} else if (me.pageStatus == 1) {
		var data = {
			showImgOld: me.showImgOld.join(','),
			shop_id: me.shop_id,
			showImg: me.showImg,
			api: "mch.App.Mch.Re_edit",
			p_id: me.p_id,
			cover_map: me.cover_map,
			product_title: me.proName,
			subtitle: me.vstName,
			scan: me.proCode,
			product_class_id: me.proClassId,
			brand_id: me.proBrandId,
			keyword: me.keyWord,
			weight: me.proWegiht,
			attr_group: JSON.stringify(attrList),
			attr_arr: JSON.stringify(strList),
			// attr_group: encodeURI(JSON.stringify(me.attr_group)),
			// attr_arr: encodeURI(JSON.stringify(me.attr_arr)),
			freight_id: me.freightSetId,
			display_position: show_adr_str,
			s_type: s_type,
			active: active_str,
			is_hexiao: me.downLineStatus,
			hxaddress: me.downLineAdd || '',
			firstPage: me.showImg[0],
			distributor_id: me.distributorsSetId == null ? "" : me.distributorsSetId,
			unit: me.unit,
			stockWarn: me.stockWarn,
			// receivingForm:me.receivingForm.toString(),//配送方式1.邮寄 2.自提
			initial: me.initial,
			volume: me.LaiKeTuiCommon.isempty(me.vshop) ? 0 : me.vshop,
			content: content,
			// content:formcontent,
			richList: richList,
			video: me.video,
			proVideo: me.proVideo
			// isVirtual: me.tabKey,
		}
		if (me.goodsType == 1) {
			data.commodity_type = 1
			data.write_off_settings = me.xnHxType
			data.is_appointment = me.xnHxTime ? 2 : 1
			data.write_off_mch_ids = me.xnHxShopId
			data.freight_id = -1
		}
		if (type == 'no') {
			data.mch_status = 4
		} else if (type == 'yes') {
			data.mch_status = 1
		}
		if (me.type == 'gys') {
			data.api = 'supplier.AppMch.Goods.addGoods'
			data.unitType = 1
		}

		//国家
		if (me.country_num) {
			data.country_num = me.country_num;
		}

		//语种
		if (me.lang_code) {
			data.lang_code = me.lang_code;
		}

		me.$req.post({
			data
		}).then(res => {

			if (res.code == 200) {
				uni.hideLoading()
				me.is_sus = true
				setTimeout(function() {
					me.is_sus = false
					uni.navigateBack({
						delta: 1
					})
					me.attr_group = []
					me.attr_arr = []
				}, 1500)
			} else {
				me.fastTap = true
				me.check_Flag = true
				uni.showToast({
					title: res.message,
					duration: 1500,
					icon: 'none'
				})
			}
		})

	}
}

export function in_array(stringToSearch, arrayToSearch) {
	for (let s = 0; s < arrayToSearch.length; s++) {
		let thisEntry = arrayToSearch[s]
		if (thisEntry == stringToSearch) {
			return true
		}
	}
	return false
}
