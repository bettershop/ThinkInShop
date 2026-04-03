<template>
	<view class="skeleton containerBg" :style="grbj">
		<div class="order_ii cart-order-li" ref="cartMod">
			<!-- #ifdef MP -->
			<heads :title="language.shoppingCart.title" @_manage="_manage" :manage_text="manage_text"
				:titleColor="'#333333'" :bgImg="headBg" ishead_w="98"></heads>
			<!-- #endif -->

			<!-- #ifndef MP -->
			<div :style="{ height: halfWidth }" class="data_h">
				<div :style="{ height: halfWidth }" class="data_h_h"></div>
			</div>
			<!-- #endif -->

			<div style="position: relative;">
				<!-- #ifndef MP -->
				<div class="page_title">
					<div :style="{ top: halfWidth,background:headBg}" class="header">

						<span class="span" v-if="Object.keys(pageConfig).length ">
							{{ pageConfig.pageNameShow ? pageConfig.pageName :''}}
						</span>
						<span class="span" v-else>{{language.shoppingCart.title}}</span>

						<div @tap="_manage" v-if="goods.length && manage_text == language.shoppingCart.topRight1">
							{{language.shoppingCart.topRight1}}
						</div>
						<div @tap="_manage" v-else-if="goods.length && manage_text != language.shoppingCart.topRight1">
							{{language.shoppingCart.topRight2}}
						</div>
					</div>
				</div>
				<!-- #endif -->
				<view class="skeleton cart-wrap " :style="{paddingBottom: paddingBottom}">
					<!-- 导航栏 -->
					<template v-if='header.length'>
						<switchNavOne ref="switchNavOne" :is_switchNav="header" :is_switchNav_obj='is_switchNav_obj'
							:is_switchNav_radius="'0 0 0 0'" :is_switchNav_padT="'0'" :is_switchNav_padB="'24rpx'"
							@choose="_header_index">
						</switchNavOne>
					</template>
					<!-- 未登录提示 -->
					<div class="loginDiv" v-if="!access_id1">
						<div class="loginText skeleton-rect" style="color: #FA5151;">
							{{language.shoppingCart.unLoginText}}
						</div>
						<div @tap="toLogin()" class="loginBtn skeleton-circle">
							<image :src="redfanhui" style="width: 32rpx;height: 44rpx;"></image>
						</div>
					</div>
					<div class="hr" style="background-color: transparent;height: 32rpx;"
						v-if="!access_id1 && (goods.length > 0 || goods.length > '0')"></div>
					<!--  有商品时显示   -->
					<ul class="goods" v-if="goods.length && load">
						<!-- 店铺名称 -->
						<div :key="index1" v-for="(item1, index1) in mch_list">
							<!-- 店铺之间分割线 -->
							<div class="hr" v-if="index1 > 0"></div>
							<div :key="'a' + item1.index1" class="shop-name">
								<!-- 单选按钮 -->
								<image v-if="item1.is_open != '1'" class="img1" :src="xuanzehui"></image>
								<image v-else class="img1" :src="display_img1[index1] ? quan_hei : quan_hui"
									@tap="_selectAllStore(item1, index1)">
								</image>
								<!-- 店铺logo -->
								<image :src="item1.head_img" class="img2"></image>
								<!-- 店铺名称 -->
								<p @tap="toStore(item1.id)">{{ item1.name }}</p>
								<!-- 箭头 进入店铺 -->
								<span @tap="toStore(item1.id)">
									<image :src="jiantou2x" class="img3"></image>
								</span>
							</div>
							<view class="line"></view>
							<!-- 加购的商品 -->
							<template v-for="(item, index) in goods">
								<li :key="index" v-if="item1.id === item.mch_id">
									<!-- 单选按钮 -->
									<image v-if="item1.is_open != '1'" class="store_img1" :src="xuanzehui"></image>
									<img v-if="item.stock > 0 && item1.is_open == 1" class="store_img1"
										:src="display_img[index] ? quan_hei : quan_hui"
										@tap="_checkedOne(item, index, item1, index1)" />
									<img v-if="item.stock <= 0 && item1.is_open == 1" id="gray_img" class="store_img1"
										:class="[manage ? 'active' : '']" style="border-radius: 50%;"
										:src="display_img[index] ? quan_hei : quan_hui"
										@tap="_checkedOne(item, index, item1, index1)" />
									<!-- 商品图 -->
									<div class="store_img">
										<img class="goods_img" :src="item.imgurl" @tap="_goodsDetailed(item.pid)"
											@error="handleErrorImg2(index)" />
									</div>
									<!-- 商品信息 -->
									<div class="goods_right">
										<div>
											<p @tap="_goodsDetailed(item.pid)" class="good_name">{{ item.pro_name }}</p>
											<div class="goodDetail"
												@tap="_mask_display(item.stock, item.price, item.imgurl, item.id, item.attribute_id, item.num)">
												<div :key="index5" v-for="(item5, index5) in item.skuBeanList">
													{{ item5.name}}
												</div>
												<img :src="jianX" class="img_X" />
											</div>
										</div>
										<div class="goods_bottom" v-if="item.stock>0">
											<p class="black">
												<span class="price">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</span>
												<span class="price1">{{ LaiKeTuiCommon.formatPrice(item.price) }}</span>
											</p>
											<div class="goods_mun">
												<span class="border-right">
													<img :src="item.num == 1 ? jian_hui : jian_hei"
														@tap="_reduce(index, item.id)" />
												</span>
												<input ref="inputs" type="text" @blur="noEmptyandzero(item,index)"
													:disabled="commodity_type == 1 && item.write_off_settings == 1"
													@input="changeNumb(item,index)" min="1" max="200"
													v-model.trim="item.num"
													style="width: 42rpx;min-width: 42rpx;text-align: center;color: #020202;font-size: 28rpx;margin-top: -4rpx;">
												<span class="border-left">
													<img :src="item.num >= 1 ? jia_hei : jia_hui"
														@tap="_add(item, index, item.stock, item.id)" />
												</span>
											</div>
										</div>
										<div class="goods_bottom noStock" v-else>
											{{language.shoppingCart.noStockDisc}}
										</div>
									</div>
									<view class="line line-top">
										<view class="line-gray"></view>
									</view>
								</li>
							</template>
						</div>
					</ul>
					<!--  失效商品   -->
					<ul class="goods" v-if="noStockList.length && load">
						<!-- 店铺名称 -->
						<div>
							<!-- 店铺之间分割线 -->
							<div class="hr"></div>
							<div class="shop-name">
								<div class="noStockTitle">
									{{language.shoppingCart.failureGood}}
								</div>
								<view class="noStockClear" @tap="clearNoStock">
									<image :src="deleteicon" style="width: 32rpx;height: 32rpx;margin-right: 8rpx;">
									</image>
									<view>{{language.shoppingCart.clearFailure}}</view>
								</view>
							</div>
							<view class="line"></view>
							<template v-for="(item, index) in noStockList">
								<li :key="index" @tap="_goodsDetailed(item.pid,item.recycle)">
									<div class="failureIcon"></div>
									<div class="store_img" style="margin-left: 0;"><img :src="item.imgurl"
											class="goods_img" @error="handleShixiao(index)" /></div>
									<div class="goods_right">
										<div>
											<p class="good_name" style="color: #999999;">{{ item.pro_name }}</p>
										</div>
										<div class="goods_bottom noStock" style="color: #020202;"
											v-if="item.recycle == 1">
											<!-- 商品已过期 -->
											{{language.shoppingCart.ygq}}
										</div>
										<div class="goods_bottom noStock" style="color: #020202;"
											v-if="item.goodsStatus == 1 && item.recycle != 1">
											<!-- 商品已下架 -->
											{{language.shoppingCart.proDown}}
										</div>
										<div class="goods_bottom noStock" style="color: #020202;"
											v-if="item.goodsStatus == 0 && item.recycle != 1 && item.goodsStatus != 1">
											<!-- 商品已售罄 -->
											{{language.shoppingCart.soldOut1}}
										</div>
									</div>
									<view class="line line-top">
										<view class="line-gray"></view>
									</view>
								</li>
							</template>
						</div>
					</ul>
					<div :style="{ marginBottom: h5Height }" class="bottom" v-if="goods.length && load">
						<div @tap="_selectAll" class="bottom_left">
							<img :src="selectAll ? quan_hei : quan_hui" class="quan_img" />
							<span>{{ language.shoppingCart.checkAll }}</span>
							<span class="span" v-if="manage" style="margin-left: 32rpx;">
								{{ language.shoppingCart.combined }}
								<span class="red"
									style="font-size: 34rpx;font-weight: bold;line-height: 28rpx;display: inline-block;font-family: DIN;">
									<span
										style="display: inline-block;font-size: 26rpx;line-height: 22rpx;">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</span>
									{{ LaiKeTuiCommon.formatPrice(total) }}
								</span>
							</span>
						</div>
						<div class="bottom_right mbf_box">
							<div @tap="_delete">
								<template>{{manage_pay}}</template>
								<span v-if="manage_text == language.shoppingCart.topRight1">({{ count }})</span>
							</div>
						</div>
					</div>
					<!--  没有商品时显示   -->
					<template v-if="butConfig">
						<!-- 是否使用 DIY 配置 -->
						<div class="empty skeleton-rect" v-if="noStockList.length==0 && !goods.length"
							:style="{'background-color': pageConfig.boxBackColor,'border-radius': pageConfig.boxRoundedVal+'px'}">
							<img :src="imgConfig.imgVal || kong" />
							<p>{{imgConfig.description}}</p>
							<div @tap="toHome()" class="toHomeBtn" v-if="butConfig.isShow" :style="{'border-radius': butConfig.roundedVal+'px',
                                     'background': butConfig.backgType == 0 ? butConfig.backgColor2 : `linear-gradient(to right,${butConfig.backgColor1},${butConfig.backgColor2})`
                                     }">{{butConfig.title}}</div>
						</div>
					</template>
					<template v-else>
						<div class="empty skeleton-rect" v-if="noStockList.length==0 && !goods.length">
							<img :src="kong" />
							<p>{{language.shoppingCart.noShop}}</p>
							<div @tap="toHome()" class="toHomeBtn">{{language.shoppingCart.noShopBtn}}</div>
						</div>
					</template>

					<div v-if="list.length > 0" class="discover_tj skeleton-rect" style="padding-top: 32rpx;">
						<h3 style="color:#333;margin: 20rpx 0px;font-weight: bold;">{{ language.my.wntj }}</h3>
					</div>
					<template v-if="load">
						<!-- 商品瀑布流 -->
						<waterfall ref="waterfall" :goodsLike="false" :addShopCar="true" :mchLogo="false"
							:goodsPriceText="false" :isDataKO="false" :isHeight="list.length > 0 ? minHeight : '0'"
							@_seeGoods="_goods" @_addShopCar="shopping_j" :goodList="list">
						</waterfall>
						<uni-load-more v-if="list.length>8" :loadingType="loadingType"
							style="background-color: #f4f5f6;" :style="{ paddingBottom: h5Height }"></uni-load-more>
						<view style="width: 100%;height: 104rpx;"></view>
					</template>
					<ul class="allgoods" v-else>
						<li :key="index" class="allgoods_li" v-for="(item,index) of 4">
							<div class="proImgBox skeleton-rect">
								<img :src="shopImg" class="allgoods_li_img" />
							</div>
							<p class="allgoods_li_title skeleton-rect" style="width: 100px;margin-top: 1px;">
								{{language.shoppingCart.bt}}
							</p>
							<p class="allgoods_li_subtitle skeleton-rect" style="width: 60px;margin-top: 1px;">
								{{language.shoppingCart.zbt}}
							</p>
							<div>
								<span class="allgoods_price skeleton-rect" style="width: 100px;">
									<span class="allgoods_price_money">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</span>
									<span class="allgoods_price_data">{{LaiKeTuiCommon.formatPrice(item)}}</span>
								</span>

								<img :src="shopImg" class="allgoods_shopImg skeleton-fillet" />
							</div>

						</li>
					</ul>
					<div class='delmask' v-if='display' @touchmove.stop.prevent>
						<div class="delmask_content">
							<p>{{ language.shoppingCart.delDisc }}</p>
							<div>
								<div class='cancel' @click="_mask_value('取消')">{{ language.shoppingCart.cancel }}</div>
								<div class='confirm' @click="_mask_value('确认')">{{ language.shoppingCart.confirm }}
								</div>
							</div>
						</div>
					</div>
					<div class='delmask1' v-if='display1' @touchmove.stop.prevent>
						<div class="delmask_content">
							<p>{{language.shoppingCart.qrsc}}</p>
							<div>
								<div class='cancel' @click="_mask_value1('取消')">{{ language.shoppingCart.cancel }}</div>
								<div class='confirm' @click="_mask_value1('确认')">{{ language.shoppingCart.confirm }}
								</div>
							</div>
						</div>
					</div>
					<view class="mask_addInvoice" v-if="isMaskRepeat">
						<view class="mask_invoice_content ">
							<view class="invoice_content_top">
								<p class="remarksContent">该商品已过期！</p>
							</view>
							<view class="invoice_content_bottom">
								<p @tap="isMaskRepeat = false">我知道了</p>
							</view>
						</view>
					</view>
					<!-- 删除提示语框 -->
					<view class="xieyi mask" style="background-color: initial;" v-if="is_sus">
						<view style="width: 272rpx;height: 272rpx;background-color: rgba(51, 51, 51, .9);">
							<view style="margin: 32rpx 0;text-align: center;margin-top: 64rpx;">
								<image style="width: 68rpx;height: 68rpx;" :src="sus"></image>
							</view>
							<view class="xieyi_title"
								style="margin-bottom: 0;margin-top: 0;color: #fff;font-weight: 500;font-size: 32rpx;">
								{{language.putForward.sccg}}
							</view>
						</view>
					</view>
					<view class="mask" v-if="is_gwc">
						<!-- 购物车添加成功 -->
						<view class="xieyi" style="background-color: initial;" v-if="is_gwc">
							<view style="width: 272rpx;height: 272rpx;background-color: rgba(51, 51, 51, .9);">
								<view style="margin: 32rpx 0;text-align: center;margin-top: 64rpx;">
									<image style="width: 68rpx;height: 68rpx;" :src="sus"></image>
								</view>
								<view class="xieyi_title"
									style="margin-bottom: 0;margin-top: 0;color: #fff;font-weight: 500;font-size: 32rpx;">
									{{language.goods.goodsDet.cgtjgwc }}
								</view>
							</view>
						</view>
					</view>
					<!--  黑条   -->
					<div class="safe-area-inset-bottom"></div>
				</view>
			</div>
			<skeleton :animation="true" :loading="!load" bgColor="#FFF"></skeleton>
			<skus ref="attrModal" @confirm="_confirm"></skus>
			<skus ref="attrModal1" @confirm="_confirm11" :noNumb="true"></skus>
			<!-- 公告提示 -->
			<notice ref="mynotice"></notice>
		</div>
	</view>
</template>

<script>
	import {
		mapMutations,
		mapState
	} from 'vuex'
	import notice from '@/components/notice.vue'
	import waterfall from "@/components/aComponents/waterfall.vue";
	import skus from '@/components/skus.vue';
	import switchNavOne from "@/components/aComponents/switchNavOne.vue";
	import {
		getDIYPageInfoById
	} from "@/common/util.js";
      import skeleton from "@/components/skeleton";
	export default {
		data() {
			return {
				numb: 1,
				is_sus: false,
				access_id1: false,
				grbj: 'background-image: url(' + this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					'images/icon1/auction/grbj.png);background-size: 100vw 100vh;background-repeat: no-repeat;',
				nrlflag: false, //是否开启授权登录
				shopImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/allgoods_shopImg.png',
				sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
				redfanhui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/redfanhui.png',
				kong: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/lllaaa.png',
				headBg: 'url(' + this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					'images/icon1/auction/grbj.png) 750rpx 100%;',
				jianX: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					'images/icon1/home_searchResult/searchResult_bottom.png',
				jiantou2x: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jiantou2x.png',
				deleteicon: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/deleteicon.png',
				quan_hui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/xuanzehui2x.png',
				xuanzehui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/xuanzehui.png',
				quan_hei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/new_red_select.png',
				jian_hui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jianhui2x.png',
				jian_hei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jian2x.png',
				jia_hei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jia+2x.png',
				jia_hui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/add+2x.png',
				manage: true, //编辑、管理状态
				manage_text: '编辑',
				manage_pay: '去结算',
				content: '确认删除选中的商品吗？', //给遮罩层传值
				display: false, //遮罩层显示状态
				display_img1: [], //店铺圆圈的选中状态
				display_img: [], //圆圈的选中状态
				selectArray: [], //存储选中商品
				selectStore: [], //存储选中店铺
				selectStoreArray: [], //存储店铺中选中的商品
				storeArray: [], //按店铺分商品
				goods: [], //初始化页面的商品数量
				selectAll: false, //全选状态
				is_gwc: false, //购物车添加成功提示
				load: false,
				isMaskRepeat: false,
				attribute_id: '',
				attrList: '',
				skuBeanList: '',
				num: '', //规格数量  
				haveSkuBean: '', //选择规则属性
				shopping_id: '', //单个商品购物车id
				jia_img: [],
				list: [],
				mch_list: [],
				fastTap: true,
				sku_list: {},
				result: {},
				tj_page: 1,
				loadingType: 0,
				changeAttrNum: 0,
				noStockList: [],
				commodity_type: 0, //1虚拟商品
				display1: false,
				minHeight: "", //瀑布流最小高度，用来固定切换导航时显示位置  
				header: [],
				is_switchNav_obj: {
					color: '#999999', //未选中字体颜色
					choose_color: '#333333', //选中字体颜色
					borderBottom: '4rpx solid #FA5151', //选中的下划线
					backgroundColor: 'rgba(0, 0, 0, 0)', //背景色
				},
				pageConfig: {},
				butConfig: {},
				imgConfig: {}
			}
		},
        props:{
            cartHeads:{
                type: Object,
                default: () => ({
                    headBg: '#ffffff',
                    backgroundColor: '#ffffff'
                })
            }
        },
		components: {
            skeleton,
			notice,
			skus,
			waterfall,
			switchNavOne,
		},
		onTabItemTap(e) {
			this.LaiKeTuiCommon.closeMPAuthWin(this)
		},
		mounted() {
			// this.triggerMounted()

		},
		onReachBottom() {
			if (this.loadingType != 0) {
				return
			}
			this.loadingType = 1
			this.tj_page++
			this._axios()
		}, 
		watch: {
            // 页面滚动监听
            cartHeads:{
                handler(newVal){
                    if (!newVal || typeof newVal !== 'object') return
                    this.headBg = newVal.headBg || this.headBg
                    this.is_switchNav_obj.backgroundColor = newVal.backgroundColor || this.is_switchNav_obj.backgroundColor
                }
            },
			'goods': {
				handler(newVal, oldVal) {

					this.$nextTick(() => {
						this.goods = newVal
					})
				},
				deep: true,
				immediate: true
			}
		},
      
		methods: {
			// mounted 钩子 函数的逻辑
			triggerMounted() {
                this.syncMallTitle()
				let needRegister = uni.getStorageSync('needRegister')
				// 判断是否是授权登录
				this.nrlflag = needRegister == this.LaiKeTuiCommon.LKT_NRL_TYPE.NRL

				const pageInfo = getDIYPageInfoById({
					key: 'homeItem17569763719112'
				})

				if (pageInfo) {
					this.getDiyinfo(pageInfo)
				} else {
					this.header = [this.language.shoppingCart.swsp, this.language.shoppingCart.xnsp]
					this.pageConfig = {}
					this.butConfig = false
				}

				this.$nextTick(() => {
					let tabbarObj = []
					try {
						const tabbar = uni.getStorageSync('tabbar')
						tabbarObj = tabbar ? JSON.parse(tabbar) : []
					} catch (e) {
						tabbarObj = []
					}
					const index = tabbarObj.findIndex(v => (v.url || v.pagePath) == 'pages/tabBar/shoppingCart')
					uni.setStorageSync('tabbarIndex', index > -1 ? index : 2)
				})

				const tabber_info = uni.getStorageSync('tabbar_info')
				if (tabber_info) {
					let obj = {}
					if (typeof tabber_info == 'string') {
						obj = JSON.parse(tabber_info)
					} else {
						obj = tabber_info
					}
					if (obj.backType == 0) {
						// 颜色设置
						this.grbj = {
							'backgroundColor': obj.colorTwo
						}
					} else {
						// 图片设置
						this.grbj = {
							backgroundImage: `url(${obj.imgurl})`,
							backgroundRepeat: 'no-repeat',
							backgroundSize: '100% 100%'
						}
					}
				}

				this.loadingType = 0
				this.tj_page = 1

				this.display = false
				this.LaiKeTuiCommon.getUrlFirst(this._axios)


				for (var i = 0; i < this.goods.length; i++) {
					this.display_img[i] = false
				}
				this.selectArray = []
				for (var i = 0; i < this.mch_list.length; i++) {
					this.display_img1[i] = false
				}
				this.selectStore = []
				if (this.goods.length == 0) {
					this.manage_text = this.language.shoppingCart.topRight1
				} else {
					this.manage_text = this.language.shoppingCart.topRight1
				}
				this.manage_pay = this.language.shoppingCart.qjs
				this.selectAll = false
				this.manage = true
				uni.setNavigationBarTitle({
					title: this.language.shoppingCart.title
				});
				// 公告提示
				setTimeout(() => {
					this.$refs.mynotice.getData()
				}, 0)
			},
			getDiyinfo(pageInfo) {


				const key = Object.keys(pageInfo)[0]
				this.pageConfig = pageInfo[key].pageConfig
				// 设置商品类型查询
				if (this.pageConfig && Object.keys(this.pageConfig).length) {
					this.header = this.pageConfig.pageTypeList.map(v => v.name)
					if (!this.pageConfig.tabIsShow) {
						this.header = []
					}
					const {
						butConfig,
						imgConfig
					} = this.pageConfig
					// 没有商品时 显示的 购物按钮
					this.butConfig = butConfig
					// 图片显示
					this.imgConfig = imgConfig
				}
			},
			/**
			 * 导航栏切换
			 */
			_header_index(index) {
				this.changeTab(index)
			},
			handleShixiao(index) {
				this.noStockList[index].imgurl = require("@/static/img/Default_picture.png")
			},
			handleErrorImg2(index) {
				this.goods[index].imgurl = require("@/static/img/Default_picture.png")
			},
			// 图片报错显示
			handleErroeImage(index) {
				this.list[index].imgurl = require("@/static/img/Default_picture.png")
			},
			changeTab(index) {
				this.commodity_type = index;
				this._axios()
			},
			noEmptyandzero(item, index) {
				this.$nextTick(function() {
					let stock = item.stock;
					let id = item.id;
					let num = this.goods[index].num;

					if (num == "" || num == 0) {
						this.$set(item, "num", 1);
					}

					if (num != 0 && !Number(num)) {
						this.$set(item, "num", 1);
					}

					if (num < 0) {
						this.$set(item, "num", 1);
					}

					if (num != "" && this.goods[index].num < 1) {
						this.$set(item, "num", 1);
						uni.showToast({
							title: this.language.shoppingCart.reduceNo,
							duration: 1000,
							icon: 'none'
						})
					} else if (this.goods[index].num <= stock && this.goods[index].num >= 1) {
						this.jia_img = true
						this._munajax(num, id);
						// 计算购物车总数量
						this._cartAllNum()
					} else if (this.goods[index].num > stock) {
						this.jia_img = false
						this.$set(item, "num", stock);
						uni.showToast({
							title: this.language.shoppingCart.addNo,
							duration: 1000,
							icon: 'none'
						})
					}
				})
			},
			changeNumb(item, index) {
				this.$nextTick(function() {
					let stock = item.stock;
					let id = item.id;
					let num = this.goods[index].num;

					if (num != "" && num == 0) {
						this.$set(item, "num", 1);
						return
					}

					if (num != 0 && !Number(num)) {
						this.$set(item, "num", '');
						return
					}

					if (num < 0) {
						this.$set(item, "num", '');
						return;
					}


					if (num != "" && this.goods[index].num < 1) {
						this.$set(item, "num", 1);
						uni.showToast({
							title: this.language.shoppingCart.reduceNo,
							duration: 1000,
							icon: 'none'
						})
					} else if (this.goods[index].num <= stock && this.goods[index].num >= 1) {
						this.jia_img = true
						this._munajax(num, id);
						// 计算购物车总数量
						this._cartAllNum()
					} else if (this.goods[index].num > stock) {
						this.jia_img = false
						this.$set(item, "num", stock);
						uni.showToast({
							title: this.language.shoppingCart.addNo,
							duration: 1000,
							icon: 'none'
						})
					}
				})
			},
			clearNoStock() {
				this.display1 = true
			},

			toStore(id) {
				uni.navigateTo({
					url: '/pagesB/store/store?shop_id=' + id
				})
			},
			changeLoginStatus() {
				this._axios()
			},
			// 点击确定购买之后，如果库存不为零。则运行
			_shopping(id) {
				if (this.haveSkuBean) {
					var data = {
						api: 'app.product.add_cart',
						pro_id: this.proid,
						attribute_id: this.haveSkuBean.cid,
						num: this.numb,
						type: 'addcart'
					}

					this.$req
						.post({
							data
						})
						.then(res => {
							let {
								code,
								data,
								message
							} = res
							if (code == 200) {
								setTimeout(() => {
									this.is_gwc = true
									setTimeout(() => {
										this.is_gwc = false
									}, 1500)
								}, 500)
								this.$store.state.access_id = data.access_id

								this.haveSkuBean = ''
								this.$refs.attrModal._mask_f()
								setTimeout(() => {
									this.fastTap = true
									this.tj_page = 1
									this._axios()
								}, 500)
							} else {
								uni.showToast({
									title: message,
									icon: 'none'
								})
								this.fastTap = true
							}
						})
						.catch(error => {
							this.fastTap = true
						})
				} else {

					this.fastTap = true
				}
			},
			// 为你推荐商品右下角的小购物车图标
			shopping_j(item, id) {
				if (item.status == 3) {
					uni.showToast({
						title: this.language.shoppingCart.proDown,
						icon: "none"
					})
					return
				}
				if (item.num == 0) {
					uni.showToast({
						title: this.language.shoppingCart.soldOut1,
						icon: "none"
					})
					return
				}
				if (!this.fastTap) {
					return
				}
				this.fastTap = false
				this.proid = id
				var data = {
					api: 'app.product.index',
					pro_id: id
				}

				this.$req
					.post({
						data
					})
					.then(res => {
						this.fastTap = true
						let {
							data: {
								collection_id,
								pro,
								comments,
								attrList,
								skuBeanList,
								attribute_list,
								qj_price,
								type,
								commodity_type,
								write_off_settings
							}
						} = res
						this.ys_price = qj_price
						this.attrList = attrList

						this.$refs.attrModal.imgurl = pro.img_arr[0]
						this.$refs.attrModal.num = pro.num
						this.$refs.attrModal.price = qj_price
						this.$refs.attrModal.skuBeanList = attribute_list
						//虚拟商品 加入购物车不需要显示 库存
						if (commodity_type == 1) {
							this.$refs.attrModal.pro2 = {
								commodity_type,
								write_off_settings
							}
						}
						this.$refs.attrModal.initData()
						this.$refs.attrModal._mask_display()

						this.collection = type
					})
					.catch(error => {
						this.fastTap = true
					})
			},
			// 跳转至为你推荐的商品详情
			_goods(id) {
				this.$store.commit('SET_PRO_ID', id)
				uni.navigateTo({
					url: '/pagesC/goods/goodsDetailed?toback=true&pro_id=' + id
				})
			},
			// 登录
			toLogin() {
				if (!this.nrlflag) {
					uni.navigateTo({
						url: '/pagesD/login/newLogin?landing_code=1'
					})
				} else {
					uni.navigateTo({
						url: '/pagesC/my/authorization'
					})
				}
			},
			//打开遮罩层
			toHome() {
				// if(this.butConfig){ 
				//     // DIY 配置
				//     uni.switchTab({
				//         url: this.butConfig.toUrl
				//     })
				// }else{
				uni.redirectTo({
					url: '/pages/shell/shell?pageType=home'
				})
				// }
				uni.setStorageSync('isHomeShow', true)
			},
			// 购物车商品中，点击规格
			_mask_display(stock, price, imgurl, id, attribute_id, num) {
				this.shopping_id = id
				this.changeAttrNum = num
				var data = {
					api: 'app.cart.dj_attribute',

					pro_id: id
				}
				if (this.access_id) {
					data.cart_id = id
				} else {
					data.cart_id = id
				}

				this.$req.post({
					data
				}).then(res => {
					let {
						data
					} = res
					this.attrList = data[0].attrList

					this.$refs.attrModal1.skuBeanList = data[0].attribute_list
					this.$refs.attrModal1.initData(true)
					this.$refs.attrModal1._mask_display()
					//虚拟商品不需要显示库存
					if (this.commodity_type == 1) {
						this.$refs.attrModal1.pro2 = {
							commodity_type: this.commodity_type,
							write_off_settings: 1
						}
					}
					if (Number(stock) == 0) {
						this.$refs.attrModal1.imgurl = imgurl
						this.$refs.attrModal1.price = price
						this.$refs.attrModal1.num = stock
					}

					// 以下为初始化选中
					if (Number(stock) > 0) {
						let sku_list = this.$refs.attrModal1.sku_list
						for (let i in sku_list.items) {
							if (sku_list.items[i].sku == attribute_id) {
								this.$refs.attrModal1.haveSkuBean = {
									name: sku_list.items[i].path,
									cid: attribute_id,
									skus: sku_list.items[i]
								};
							}
						}

						let selectName = this.$refs.attrModal1.haveSkuBean.name.split(',')

						for (let i in sku_list.result) {

							for (let k in sku_list.result[i]) {

								let flag = selectName.some(item => item == k)

								if (flag) {
									this.$refs.attrModal1.handleActive(i, sku_list.result[i][k])
								}

							}

						}
					}

				})
			},
			// 遮罩层中，点击确认按钮
			_confirm(sku) {
				Object.assign(this.$data, sku)
				this._confirm1()
			},
			_confirm1() {
				if (!this.fastTap) {
					return
				}
				this.fastTap = false
				if (Boolean(this.haveSkuBean)) {
					if (this.num == 0) {
						uni.showToast({
							title: this.language.shoppingCart.kucunTips,
							duration: 1000,
							icon: 'none'
						})
						this.fastTap = true
					} else if (this.num != 0) {
						this._shopping()
					}
				} else {
					if (this.num == 0) {
						uni.showToast({
							title: this.language.shoppingCart.kucunTips,
							duration: 1000,
							icon: 'none'
						})
						this.fastTap = true
					} else {
						uni.showToast({
							title: this.language.shoppingCart.chooseAttrTips,
							duration: 1000,
							icon: 'none'
						})
						this.fastTap = true
					}
				}
			},
			//确认
			_confirm11(sku) {
				Object.assign(this.$data, sku)
				if (Boolean(this.haveSkuBean)) {
					let Stock = this.haveSkuBean.skus.Stock
					if (this.changeAttrNum > Stock) {
						uni.showToast({
							title: this.language.shoppingCart.kucunTips,
							icon: 'none'
						})
						return
					}

					let data = {
						api: 'app.cart.modify_attribute',
						cart_id: this.shopping_id,
						attribute_id: this.haveSkuBean.cid
					}
					this.$req.post({
						data
					}).then(res => {
						let {
							code,
							message
						} = res
						if (code == 200) {
							this.$refs.attrModal1._mask_false()
							this.tj_page = 1
							this._axios()
						} else {
							uni.showToast({
								title: message,
								icon: 'none'
							})
						}
					})
				} else {
					uni.showToast({
						title: this.language.shoppingCart.chooseAttrTips,
						duration: 1000,
						icon: 'none'
					})
				}
			},
			_axios() {
				if (!this.load) {
					uni.showLoading({
						title: this.language.showLoading.waiting
					})
				}

				var data = {
					api: 'app.cart.index',
					cart_id: this.$store.state.nCart,
					page: this.tj_page,
					commodity_type: this.commodity_type
				}
				this.$req.post({
					data
				}).then(res => {
					if (!this.load) {
						uni.hideLoading()
					}

					let {
						data,
						data0,
						list,
						mch_list,
						login_status,
						grade,
						isGrade
					} = res.data

					if (login_status != 0) {
						this.access_id1 = true
					} else {
						this.access_id1 = false
					}
					if (res.code == 200) {

						this.selectArray = []
						this.selectStore = []
						this.selectStoreArray = []
						this.storeArray = []
						this.selectAll = false
						this.display_img1 = []
						this.display_img = []

						this.noStockList = data0

						if (this.tj_page == 1) {
							this.list = []
						}

						list.filter(item => {
							item.vip_price = Number(item.vip_price).toFixed(2)
							item.vip_yprice = Number(item.vip_yprice).toFixed(2)
							item.imgurl = item.imgurl + '?a=' + Math.floor(Math.random() * 1000000);
							item.contNum = item.stockNum
							item.volume = item.payPeople
						})
						this.list.push(...list)
						if (list.length > 0) {
							this.loadingType = 0
						} else {
							this.loadingType = 2
						}

						//获取店铺信息
						let newMch_list = []
						mch_list.filter(items => {
							let flag = data.some((item) => {
								return item.mch_id == items.id
							})

							if (flag) {
								newMch_list.push(items)
							}
						})
						this.mch_list = newMch_list
						//每个店铺 添加isAllSelect参数 用于判断当前店铺是否全选
						this.mch_list.forEach((item, index) => {
							item.isAllSelect = false
						})

						//获取加购的商品
						this.goods = data
						//每个商品 添加isSelect参数 用于判断当前商品是否选择
						this.goods.forEach((item, index) => {
							item.isSelect = false
						})

						// 计算购物车商品总数量
						this._cartAllNum()
						// 按店铺分商品
						this._productDividedByStore()
					} else {
						this.access_id1 = false
						this.goods = []
					}
					this.load = true
				})
			},
			...mapMutations({
				cart_id: 'SET_CART_ID',
				setProId: 'SET_PRO_ID',
				order_id: 'SET_ORDER_ID',
				address_id: 'SET_ADDRESS_ID',
				setCartNum: 'SET_CART_NUM'
			}),
			//数量减少
			_reduce(index, id) {
				var num = this.goods[index].num
				if (this.goods[index].num <= 1) {
					this.goods[index].num = 1
					uni.showToast({
						title: this.language.shoppingCart.reduceNo,
						duration: 1000,
						icon: 'none'
					})
					return false
				} else {
					this.goods[index].num--
					this._munajax(this.goods[index].num, id)
					// 计算购物车总数量
					this._cartAllNum()
				}
			},
			//数量增加
			_add(itm, index, stock, id) {
				//虚拟商品-需要预约时间（不能多个一起下单）
				if (this.commodity_type == 1 && itm.write_off_settings == 1) {
					uni.showToast({
						title: '一次只能购买1个',
						duration: 1000,
						icon: 'none'
					});
					return
				}
				let num = this.num ? this.num : stock
				if (Number(this.goods[index].num) < Number(num)) {
					this.goods[index].num++
					this._munajax(this.goods[index].num, id)
					this.jia_img = true
					// 计算购物车总数量
					this._cartAllNum()
				} else {
					this.jia_img = false
					uni.showToast({
						title: this.language.shoppingCart.addNo,
						duration: 1000,
						icon: 'none'
					})
				}
			},

			_munajax(goods, id) {
				let arr = [{
					num: goods,
					cart_id: Number(id)
				}]
				let data = {
					api: 'app.cart.up_cart',
					goods: JSON.stringify(arr)
				}

				this.$req.post({
					data
				}).then(res => {})
			},
			//编辑管理状态
			_manage() {
				//每次编辑都清空选择
				this.selectAll = false
				this.selectArray = []
				//商品
				for (var i = 0; i < this.goods.length; i++) {
					this.$set(this.display_img, i, false)
				}
				//店铺
				this.selectStore = []
				this.selectStoreArray = []
				if (this.storeArray) {
					for (var i = 0; i < this.storeArray.length; i++) {
						this.$set(this.display_img1, i, false)
					}
				}

				this.manage = !this.manage
				if (this.manage) {
					this.manage_text = this.language.shoppingCart.topRight1
					this.manage_pay = this.language.shoppingCart.qjs
				} else {
					this.manage_text = this.language.shoppingCart.topRight2
					this.manage_pay = this.language.shoppingCart.delBtn
				}

				//重新按照店铺分一下商品,如果是编辑则把库存不足的元素也拿出来
				this._productDividedByStore()
			},
			//节奏遮罩层传值
			_mask_value(mask_value) {

				var cart_id = []
				var cart_str = ''
				for (var i = 0; i < this.selectArray.length; i++) {
					cart_id.push(Number(this.selectArray[i].id))
					cart_str = cart_str + Number(this.selectArray[i].id) + ','
				}
				if (mask_value == '取消') {
					this.display = false
				} else if (this.manage_pay == this.language.shoppingCart.delBtn && mask_value == '确认') {
					// #ifdef MP-ALIPAY
					cart_id = cart_str
					// #endif

					var data = {
						api: 'app.cart.delcart',
					}

					// #ifdef MP-BAIDU
					for (let i in cart_id) {
						data[`cart_id[${i}]`] = cart_id[i]
					}
					// #endif

					// #ifndef MP-BAIDU
					data['cart_id'] = cart_id
					// #endif

					this.display_img1 = []

					this.$req.post({
						data
					}).then(res => {
						if (res.code == 200) {
							this.is_sus = true
							setTimeout(() => {
								this.is_sus = false
							}, 1500)
							this.manage_text = this.language.shoppingCart.topRight1
							this.manage_pay = this.language.shoppingCart.qjs
							this.manage = true
							this.selectAll = false
							this.selectArray = []
							for (var i = 0; i < this.goods.length; i++) {
								this.$set(this.display_img, i, false)
							}
							for (var i = 0; i < cart_id.length; i++) {
								var n = this.$store.state.nCart.indexOf(cart_id[i])
								if (n > -1) {
									this.$store.state.nCart.splice(n, 1)
								}
							}

							this.display = false
							this._axios()
							// 计算购物车总数量
							this._cartAllNum()
						} else {
							uni.showToast({
								title: res.err,
								duration: 1500,
								icon: 'none'
							})
						}
					})
				}
			},

			_mask_value1(mask_value) {
				if (mask_value == '取消') {
					this.display1 = false
				} else {
					let id = ''
					this.noStockList.filter(item => {
						id += ',' + item.id
					})
					id = id.replace(',', '')
					let data = {
						api: 'app.cart.delcart',
						cart_id: id
					}
					this.$req.post({
						data
					}).then(res => {
						uni.showToast({
							title: res.message,
							icon: 'none'
						})

						if (res.code == 200) {
							this.tj_page = 1
							this._axios()
						}
					})
					this.display1 = false
				}
			},
			openWindows(param) {
				let that = this
				let content = '';
				let winWidth
				let winHeight
				// 缩写法   
				//uni.getSubNVueById('popup').show();
				// 对子窗体添加一些自定义的配置
				// 通过 ID 获取 subNVues 原生子窗体的实例
				//获取的"popup"要和pages.json里面配置的id一致(非常重要，否侧会报错)
				const subNVue = uni.getSubNVueById('popup');
				// const subVue = uni.getSubNVueById("popup");
				//向子窗口传入参数
				uni.$emit('hello-popup', {
					type: param,
					content: content,
					winWidth: winWidth,
					winHeight: winHeight,
				})
				// 设置子窗口的样式
				subNVue.setStyle({
					width: winWidth,
					height: winHeight,
				})
				subNVue.show('slide-out-top', 3000);
			},
			//删除商品
			_delete() {
				// debugger
				// return
				if (this.manage_pay == this.language.shoppingCart.delBtn && this.selectArray.length > 0) {
					this.display = true
					// #ifndef H5
					// uni.hideTabBar()
					// #endif
				} else if (this.manage_pay == this.language.shoppingCart.delBtn) {
					uni.showToast({
						title: this.language.shoppingCart.chooseDelPro,
						duration: 1000,
						icon: 'none'
					})
				}

				if (this.manage_pay == this.language.shoppingCart.qjs && this.selectArray.length > 0) {
					this.isLogin(() => {
						var cart_id = []
						var goods = []
						for (var i = 0; i < this.selectArray.length; i++) {
							cart_id.push(this.selectArray[i].id)
							goods.push({
								num: this.selectArray[i].num,
								cart_id: Number(this.selectArray[i].id)
							})
						}

						this.$store.state.cart_id = cart_id
						this.cart_id(cart_id)
						this.order_id('')
						this.address_id('')

						// #ifdef H5
						var storage = window.localStorage
						storage.cart_id = cart_id
						// #endif

						uni.navigateTo({
							url: '/pagesE/pay/orderDetailsr?cart_id=' + cart_id +
								'&canshu=false&returnR=1&commodity_type=' + this.commodity_type
						})
					})
				} else if (this.manage_pay == this.language.shoppingCart.qjs) {
					uni.showToast({
						title: this.language.shoppingCart.choosePayPro,
						duration: 1000,
						icon: 'none'
					})
				}
			},
			//单选 (goods,goods,mch,mch)
			_checkedOne(item, indexli, item1, index1) {
				//是否是编辑状态标识
				let isEdit = false

				var cum = this.selectArray.findIndex(items => {
					return items.id == item.id
				})
				// 如果是有的话 点击就是取消
				//判断点击的传入的值是否存在数组中，如果没有添加，如果有删除，同时设定选中状态（第一次点击添加进数组，第二次点击从数组中删除）
				if (this.manage_pay == this.language.shoppingCart.delBtn) {
					isEdit = true
					if (cum < 0) {
						// 点击新增
						this.selectArray.push(item)
						this.$set(this.display_img, indexli, true)
					} else {
						// 点击取消
						var i = this.selectArray.findIndex(items => {
							return items.id == item.id
						})

						this.selectArray.splice(i, 1)

						this.$set(this.display_img, indexli, false)
					}
				} else if (this.manage_pay == this.language.shoppingCart.qjs) {
					if (cum < 0 && item.stock > 0) {
						// 点击新增
						this.selectArray.push(item)
						this.$set(this.display_img, indexli, true)
					} else if (cum >= 0 && item.stock > 0) {
						// 点击取消
						var i = this.selectArray.findIndex(items => {
							return items.id == item.id
						})

						this.selectArray.splice(i, 1)

						this.$set(this.display_img, indexli, false)
					} else {
						//单选库存不足
						uni.showToast({
							title: this.language.shoppingCart.prokucunTips,
							duration: 1000,
							icon: 'none'
						})
						return
					}
				}

				let haveGoodsList = this.goods.filter(items => {
					if (isEdit) {
						//编辑可以勾选库存不足的商品
						return true
					} else {
						return Number(items.stock) > 0
					}
				})
				//根据产品选状态，设定全选状态
				if (this.selectArray.length > 0 && this.selectArray.length == haveGoodsList.length) {
					this.selectAll = true
				} else {
					this.selectAll = false
				}
				//根据产品选状态，设定店铺全选状态
				// 添加或删除店铺选择数组
				if (item.mch_id == item1.id) {
					if (!this.selectStoreArray[index1]) {
						this.selectStoreArray[index1] = []
					}

					var isSlectedInStore = this.selectStoreArray[index1].findIndex(items => {
						return items.id == item.id
					})
					if (isSlectedInStore < 0) {
						this.selectStoreArray[index1].push(item)
					} else {
						var i = this.selectStoreArray[index1].findIndex(items => {
							return items.id == item.id
						})
						this.selectStoreArray[index1].splice(i, 1)
					}

					var isSlected = this.selectStore.findIndex(items => {
						return items.id == item1.id
					})
					if (this.selectStoreArray[index1].length == this.storeArray[index1].children.length) {
						if (isSlected < 0) {
							this.$set(this.display_img1, index1, true)
							this.selectStore.push(item1)
						}
					} else {
						if (isSlected >= 0) {
							this.$set(this.display_img1, index1, false)

							var i = this.selectStore.findIndex(items => {
								return items.id == item1.id
							})

							this.selectStore.splice(i, 1)
						}
					}
				}
			},
			//全选
			_selectAll() {
				//可以被选中的商品下标集
				var arr = []
				//用来判断是否选择了禁用的商品
				let shacoStatus = 0
				let haveGoodsList = []
				for (var i = 0; i < this.goods.length; i++) {
					if (this.goods[i].stock > 0 && this.goods[i].is_open == '1') {
						//存储可以被选中的商品下标
						arr.push(i)
						//存储当前商品信息
						haveGoodsList.push(this.goods[i])
					} else if (this.goods.length == 1 && this.goods[i].is_open != '1') {
						shacoStatus = 1
					}
				}
				if (shacoStatus == 1) {
					uni.showToast({
						title: this.language.shoppingCart.prokucunTips2,
						duration: 1000,
						icon: 'none'
					})
					return
				}
				if (this.manage_pay == this.language.shoppingCart.delBtn) {
					this.selectAll = !this.selectAll
					if (this.selectAll) {
						for (var i = 0; i < haveGoodsList.length; i++) {
							this.$set(this.selectArray, i, haveGoodsList[i])
							this.$set(this.display_img, i, true)
						}
						//店铺
						if (this.mch_list) {
							for (var i = 0; i < this.mch_list.length; i++) {
								this.$set(this.selectStore, i, this.mch_list[i])
								this.$set(this.display_img1, i, true)
								//if (this.storeArray.children) {
								//解决 46110  真 Tm 难找 ！！！
								if (this.storeArray.length) {
									if (this.storeArray[i].children) {
										for (var j = 0; j < this.storeArray[i].children.length; j++) {
											if (!this.selectStoreArray[i]) {
												this.selectStoreArray[i] = []
											}
											this.$set(this.selectStoreArray[i], j, this.storeArray[i].children[j])
										}
									}
								}
							}
						}
					} else {
						this.selectArray = []
						for (var i = 0; i < haveGoodsList.length; i++) {
							this.$set(this.display_img, i, false)
						}
						//店铺
						this.selectStore = []
						this.selectStoreArray = []
						if (this.storeArray) {
							for (var i = 0; i < this.storeArray.length; i++) {
								this.$set(this.display_img1, i, false)
							}
						}
					}
				} else if (this.manage_pay == this.language.shoppingCart.qjs) {
					if (arr.length > 0) {
						this.selectAll = !this.selectAll
						if (this.selectAll) {
							//所有商品
							for (var i = 0; i < haveGoodsList.length; i++) {
								this.$set(this.selectArray, i, haveGoodsList[i])
								this.$set(this.display_img, arr[i], true)
							}
							//店铺
							if (this.mch_list) {
								for (var i = 0; i < this.mch_list.length; i++) {
									this.$set(this.selectStore, i, this.mch_list[i])
									this.$set(this.display_img1, i, true)
									if (this.storeArray[i].children) {
										for (var j = 0; j < this.storeArray[i].children.length; j++) {
											if (!this.selectStoreArray[i]) {
												this.selectStoreArray[i] = []
											}
											this.$set(this.selectStoreArray[i], j, this.storeArray[i].children[j])
										}
									}
								}
							}
						} else {
							this.selectArray = []
							for (var i = 0; i < haveGoodsList.length; i++) {
								this.$set(this.display_img, i, false)
							}
							//店铺
							this.selectStore = []
							this.selectStoreArray = []
							if (this.storeArray) {
								for (var i = 0; i < this.storeArray.length; i++) {
									this.$set(this.display_img1, i, false)
								}
							}
						}
					} else {
						uni.showToast({
							title: this.language.shoppingCart.prokucunTips1,
							duration: 1000,
							icon: 'none'
						})
					}
				}
			},
			// 全选某店铺商品
			_selectAllStore(item, index) {
				var storeId = this.mch_list[index].id
				var isSlected = this.selectStore.findIndex(items => {
					return items.id == storeId
				})

				let havePro = this.goods.some(items => {
					return items.mch_id == storeId && Number(items.stock) > 0
				})

				if (!havePro) {
					return
				}

				// 判断此店铺是否被选中
				if (isSlected < 0) {
					// 否 则选中店铺
					for (var j = 0; j < this.goods.length; j++) {
						var cum = this.selectArray.findIndex(items => {
							return items.id == this.goods[j].id
						})

						if (storeId == this.goods[j].mch_id) {
							//是否可以勾选标识
							let isChceked = cum < 0;

							if (isChceked) {
								//大于库存才勾选
								isChceked = Number(this.goods[j].stock) > 0
								//购物车编辑的时候才可以勾选,编辑可以勾选库存不足的商品
								if (this.manage_pay == this.language.shoppingCart.delBtn) {
									isChceked = true;
								}
							}

							if (isChceked) {
								//满足条件则勾选当前商品
								this.selectArray.push(this.goods[j])
								this.$set(this.display_img, j, true)
								if (!this.selectStoreArray[index]) {
									this.selectStoreArray[index] = []
								}
								this.selectStoreArray[index].push(this.goods[j])
							}
						}
					}
					this.selectStore.push(item)

					this.$set(this.display_img1, index, true)
				} else {
					// 是 则取消店铺
					for (var j = 0; j < this.goods.length; j++) {
						var cum = this.selectArray.findIndex((selectItem) => selectItem.id === this.goods[j].id)

						if (storeId == this.goods[j].mch_id) {
							if (cum >= 0) {
								this.selectArray.splice(cum, 1)
								this.$set(this.display_img, j, false)
								if (!this.selectStoreArray[index]) {
									this.selectStoreArray[index] = []
								}

								//解决 46107
								this.selectStoreArray[index] = []
							}
						}
					}

					let cum3 = this.selectStore.findIndex((selectItem) => selectItem.id === item.id)
					this.selectStore.splice(cum3, 1)

					this.$set(this.display_img1, index, false)
				}

				//根据产品选状态，设定全选状态
				let haveGoodsList = this.goods.filter(items => {
					return Number(items.stock) > 0
				})

				if (this.selectArray.length > 0 && this.selectArray.length == haveGoodsList.length) {
					this.selectAll = true
				} else {
					this.selectAll = false
				}
			},
			// 按店铺分商品
			_productDividedByStore() {
				let haveGoodsList = this.goods.filter(items => {
					if (this.manage_pay == this.language.shoppingCart.delBtn) {
						//编辑可以选择所有商品
						return true
					}
					return Number(items.stock) > 0
				})

				if (this.mch_list && haveGoodsList) {
					for (var i = 0; i < this.mch_list.length; i++) {
						if (!this.storeArray[i]) {
							this.storeArray[i] = []
						}
						this.storeArray[i].push(this.mch_list[i])

						if (!this.storeArray[i].children) {
							this.storeArray[i].children = []
						}
						for (var j = 0; j < haveGoodsList.length; j++) {
							if (this.mch_list[i].id == haveGoodsList[j].mch_id) {
								var storeI = this.storeArray[i].children.findIndex(it => {
									return it.id == haveGoodsList[j].id
								})

								if (storeI < 0) {
									this.storeArray[i].children.push(haveGoodsList[j])
								}
							}
						}
					}
				}
			},
			_goodsDetailed(pro_id, recycle) {
				this.setProId(pro_id)
				if (recycle == 1) {
					this.isMaskRepeat = true
				} else {
					uni.navigateTo({
						url: '/pagesC/goods/goodsDetailed?toback=true&pro_id=' + pro_id
					})
				}
			},
			// 计算购物车商品总数量
			_cartAllNum() {
				var data = this.goods
				this.setCartNum(data.length)

			}
		},
		computed: {
			count: function() {
				var count = 0
				for (var i = 0; i < this.selectArray.length; i++) {
					count += Number(this.selectArray[i].num)
				}
				return count
			},
			h5Height: function() {
				var height
				// #ifdef H5
				height = 120 + 'upx'
				// #endif
				// #ifndef H5
				height = 0 + 'upx'
				// #endif
				return height
			},
			paddingBottom() {
				let height = 0;
				// #ifdef H5
				if (this.goods.length) {
					height = 200;
				} else {
					height = 100;
				}
				// #endif
				// #ifndef H5
				height = 0
				// #endif
				return uni.upx2px(height) + 'px'
			},
			total() {
				var total = 0
				for (var i = 0; i < this.selectArray.length; i++) {
					if (this.selectArray[i]) {
						total += new Number(this.selectArray[i].price) * parseInt(this.selectArray[i].num)
					}
				}
				return total.toFixed(2)
			},
			...mapState({
				tabBarList: 'tabBarList',
			})
		},
	}
</script>
<style>
	page {
		background-color: #F4f5f6;
	}
</style>
<style lang="less" scoped>
	@import url("@/laike.less");
	@import url('@/static/css/tabBar/shoppingCart.less');

	#gray_img.active {
		background-color: #f4f4f4;
	}

	.header {
		border-bottom: 0 !important;
	}

	.header .span {
		font-size: 36rpx;
	}

	/deep/.isWaterfall {
		padding-top: 0 !important;
	}
</style>
