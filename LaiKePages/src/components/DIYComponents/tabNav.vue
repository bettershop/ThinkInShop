<template>
	<view class="navTabBox"
		:style="'background: linear-gradient(90deg, '+ bgColor[0].item +' 50%, '+ bgColor[1].item +' 100%);margin-top:'+pxToRpxNum(mbConfig)+'rpx;color:'+txtColor+';'">
		<view class="goodsBox">
			<scroll-view scroll-x="true" class="goods_title">
				<view class="seckill_content_view" style="padding-bottom: 16rpx;">
					<view @tap="changeBottom(index)" class="goods_title_item"  v-for="(item, index) of bottom_list"
						:key="index" :style="{borderRightColor: borderRightColor, height: Number(shop_id) > 0 ? 'auto' : undefined}" 
						:class="{active: index == bottomIndex}">
						<view :style="{color: index == bottomIndex ? txtSelectColor : txtColor}">{{ item.pname }}</view>
						<text
							:style="{color: index == bottomIndex ? txtSelectColor : txtColor}">{{ item.english_name }}</text>
						<view v-if="index == bottomIndex" class="active-view"
							:style="{backgroundColor: selectBorderColor}"></view>
					</view>
				</view>
			</scroll-view>
			<!-- <div class='filterTopFilter' :class="{'fiterMask':filterMaskFlag}" v-if='Number(shop_id)==0'>
				<div class="filter-left">
					<div :class='{"active":!filterMaskFlag && searchType == ""}' @tap="clickType('')">
						{{language.search2.searchRes.TopButton[0]}}
					</div>
					<div :class='{"active":!filterMaskFlag && searchType == "volume"}' @tap="clickType('volume')">
						{{language.search2.searchRes.TopButton[1]}}
						<div class="sortingImgBox">
							<img class="sortingImg" :src="searchType=='volume'&&sorting=='asc'?sortingToph:sortingTop">
							<img class="sortingImg"
								:src="searchType=='volume'&&sorting=='desc'?sortingBottomh:sortingBottom">
						</div>
					</div>
					<div :class='{"active":!filterMaskFlag && searchType == "price"}' @tap="clickType('price')">
						{{language.search2.searchRes.TopButton[2]}}
						<div class="sortingImgBox">
							<img class="sortingImg" :src="searchType=='price'&&sorting=='asc'?sortingToph:sortingTop">
							<img class="sortingImg"
								:src="searchType=='price'&&sorting=='desc'?sortingBottomh:sortingBottom">
						</div>
					</div>
				</div>
				<view class="fiter_line"></view>
				<img class="layoutBox" :src="layout?party:two" @click="changeLayout">
			</div> -->

			<div class="goods_ul goods_ul-waterfall" v-if="show&&layout">
				<div class="goods_ul_left">
					<template v-if="listLeft.length>0">
						<div class="goods_like goods_ul_left_li" v-for="(item, index) in listLeft" :key="index"
							@tap="_goods(item.id)">
							<div class="goods_like_img relative" style="margin: 0 auto 10rpx">
								<image lazy-load :src="item.cover_map || item.imgurl" data-name="0" mode="widthFix"
									width="100%" @load="onImageLoad($event, 'goods_ul_left_li' + index )" />

								<div v-if="item.status == 3" class="dowmPro">
									{{language.store.shelf}}
								</div>
								<div v-else-if="item.contNum == '0'" class="dowmPro">
									<!-- 已售罄 -->
									{{language.shoppingCart.soldOut}}
								</div>
							</div>
							<view :id="'goods_ul_left_li' + index">
								<view class="shop_name">
									<!-- 店铺名称 -->
									<span> {{item.mch_name}} </span>
								</view>
								<p class="overtitle">{{ item.product_title || item.name }}</p>
								<view class="goods_content_item_icon" v-if='item.rx || item.tj || item.xp'>
									<view v-if="item.rx">{{language.home.hot}}</view>
									<view v-if="item.tj" style="color: #FE9331;border-color: #FE9331;">
										{{language.home.recommend}}
									</view>
									<view v-if="item.xp" style="color: #42B4B3;border-color: #42B4B3;">
										{{language.home.New_products}}
									</view>
								</view>
								<!-- <p class="overflowText">{{ item.subtitle }}</p> -->
								<div class="goods_mun">
									<div class="allgoods_price" v-if="is_grade">
										<view class='red'>
											<text class="currency">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</text>
											<span
												class="vip_price">{{LaiKeTuiCommon.formatPrice(item.vip_price)}}</span> 
											<text class="lod-price" style="text-decoration: line-through;">
												{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(item.vip_yprice)}}
											</text>
										</view>

									</div>
									<div class="allgoods_price" v-else>
										<view class='red'>
											<text class="currency">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</text>
											<span
												class="vip_price">{{LaiKeTuiCommon.formatPrice(item.vip_yprice)}}</span>
											<span class="sales">{{ item.volume }} {{language.waterfall.type[3]}}</span>
										</view>
									</div>
									<!-- 加入购物车 (实物商品) -->
									<template >
										<view class="joinCart" @tap.stop="addShopping(item, item.id)"
											 >
											<image :src='shoppingImg'> </image>
										</view>
									</template>
									<!-- 加入购物车 (虚拟商品) isAddCar=1 虚拟商品逻辑-无需核销/无需预约 才显示加入购物车 -->
								<!-- 	<template v-else>
										<view class="joinCart" @tap.stop="addShopping(item, item.id)"
											v-if="is_open == 1 && item.isAddCar == 1">
											<image :src='shoppingImg'> </image>
										</view>
									</template> -->

								</div>
							</view>
							<!-- <view class="discover" @tap.stop="$emit('toUrl', '/pagesC/collection/discover?pro_id=' + item.id)">{{language.home.Look}}</view> -->
						</div>
					</template>
				</div>
				<div class="goods_ul_right">
					<template v-if="listRight.length>0">
						<div class="goods_like goods_ul_right_li" v-for="(item, index) in listRight" :key="index"
							@tap="_goods(item.id)">
							<div class="goods_like_img relative" style="margin: 0 auto 10rpx">
								<image lazy-load :src="item.cover_map || item.imgurl" data-name="1" mode="widthFix"
									width="100%" @load="onImageLoad($event, 'goods_ul_right_li' + index)" />

								<div v-if="item.status == 3" class="dowmPro">
									{{language.store.shelf}}
								</div>
								<div v-else-if="item.num == '0'" class="dowmPro">
									<!-- 已售罄 -->
									{{language.shoppingCart.soldOut}}
								</div>
							</div>
							<view :id="'goods_ul_right_li' + index">
								<p class="overtitle">{{ item.product_title || item.name }}</p>
								<view class="goods_content_item_icon" v-if='item.rx || item.tj || item.xp'> 
									<view v-if="item.rx">{{language.home.hot}}</view>
									<view v-if="item.tj" style="color: #FE9331;border-color: #FE9331;">
										{{language.home.recommend}}
									</view>
									<view v-if="item.xp" style="color: #42B4B3;border-color: #42B4B3;">
										{{language.home.New_products}}
									</view>
								</view>
								<div class="goods_mun">
									<div class="allgoods_price" v-if="is_grade">
										<view class='red'>
											<text class="currency">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</text>
											<span
												class="vip_price">{{LaiKeTuiCommon.formatPrice(item.vip_price)}}</span>
											<span class="sales"> {{ item.volume }} {{language.waterfall.type[3]}}</span>
										</view>
										<p style="text-decoration: line-through;">
											{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(item.vip_yprice)}}
										</p>
									</div>
									<div class="allgoods_price" v-else>
										<view class='red'>
											<text class="currency">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</text>
											<span
												class="vip_price">{{LaiKeTuiCommon.formatPrice(item.vip_yprice)}}</span>
											<span class="sales"> {{ item.volume }} {{language.waterfall.type[3]}}</span>
										</view>
									</div>
									<!-- 加入购物车 (实物商品)  DIY 加入购物车 没有 判断-->
									<template >
										<view class="joinCart" @tap.stop="addShopping(item, item.id)"
											 >
											<image :src='shoppingImg'> </image>
										</view>
									</template>
									<!-- 加入购物车 (虚拟商品) isAddCar=1 虚拟商品逻辑-无需核销/无需预约 才显示加入购物车 -->
								<!-- 	<template v-else>
										<view class="joinCart" @tap.stop="addShopping(item, item.id)"
											v-if="is_open == 1 && item.isAddCar == 1">
											<image :src='shoppingImg'> </image>
										</view>
									</template> -->
								</div>
							</view>
							<!-- <view class="discover" @tap.stop="$emit('toUrl', '/pagesC/collection/discover?pro_id=' + item.id)">{{language.home.Look}}</view> -->


						</div>
					</template>
				</div>
			</div>
			<ul class='allgoods_vertical' v-if="show&&!layout">
				<li class="allgoods_vertical_li" v-for='(items,index) in productList' :key='index'
					@tap="_goods(items.id)">
					<view class="good_box">
						<view class="goodsImg">
							<img :src="items.imgurl" @error="handleErrorImg(index)" />
							<div v-if="items.recycle == 1">{{language.collection.isDel}}</div>
							<template v-if="items.num == 0 && items.status == 3">
								<div v-if="items.status == 3" class="dowmPro">
									{{language.store.shelf}}
								</div>
							</template>
							<template v-else>
								<div v-if="items.num == 0" class="dowmPro">
									<!-- 已售罄 -->
									{{language.shoppingCart.soldOut}}
								</div>
								<div v-if="items.status == 3" class="dowmPro">
									{{language.store.shelf}}
								</div>
							</template>
						</view>
						<view class="goodsInfo verticalStyle">
							<view class="storeName">{{items.mch_name}}</view>
							<view class="goodsName">{{items.product_title}}</view>
							<view class="goods_content_item_icon new_goods_goods_icon noPadding"
								v-if="items.s_type_list.length">
								<view :style="{'backgroundColor':item_1.color}"
									v-for="(item_1, index_1) in items.s_type_list" :key="index_1">{{item_1.name}}</view>
							</view>
							<view v-else class="goods_content_item_icon new_goods_goods_icon noPadding white_h"></view>
							<view class="goodsPrice">
								<view class="priceInfo">
									<view class="x_price">
										<span class="x_price_icon">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</span>
										<span>{{LaiKeTuiCommon.formatPrice(items.price?items.price:'0.00')}}</span>
									</view>
									<view class="y_price">
										{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(items.vip_yprice)}}
									</view>
								</view>
								<!-- 加入购物车 (实物商品) -->
								<template v-if="items.commodity_type == 0">
									<view class="joinCart_one" @tap.stop="addShopping(items, items.id)"
										v-if="is_open == 1">
										<image :src='shoppingImg'> </image>
									</view>
								</template>
								<!-- 加入购物车 (虚拟商品) isAddCar=1 虚拟商品逻辑-无需核销/无需预约 才显示加入购物车 -->
								<template v-else>
									<view class="joinCart_one" @tap.stop="addShopping(items, items.id)"
										v-if="is_open == 1 && items.isAddCar == 1">
										<image :src='shoppingImg'> </image>
									</view>
								</template>
							</view>
						</view>
					</view>
				</li>
			</ul>
		</view>
		<skus ref="attrModal" product_type="1" :stock="language.goods.goodsDet.stock"
			:nums="language.goods.goodsDet.number" :confirm="language.goods.goodsDet.confirm" @confirm="_confirm">
		</skus>
		<uni-load-more v-if="class_pro.length > 0" :loadingType="loadingType"></uni-load-more>
	</view>
</template>

<script>
	import skus from '../../components/skus.vue';
	export default {
		name: 'tabNav',
		props: {
			dataConfig: {
				type: Object,
				default: () => {}
			},
			page: {
				type: Number
			},
			is_grade: {
				type: Boolean
			},
			is_open: {
				type: Boolean
			},
			shop_id: {
				type: Number,
                default:0
			},
		},
		data() {
			return { 
				tabTitle: [],
				tabLeft: 0,
				isWidth: 0, //每个导航栏占位
				tabClick: 0, //导航栏被点击
				isLeft: 0, //导航栏下划线位置
				bgColor: this.dataConfig.bgColor.color || [],
				mbConfig: this.dataConfig.mbConfig.val,
				txtColor: this.dataConfig.txtColor.color[0].item,
				txtSelectColor: this.dataConfig.txtSelectColor.color[0].item,
				borderRightColor: this.dataConfig.rightBorderColor.color[0].item,
				selectBorderColor: this.dataConfig.selectBorderColor.color[0].item,
				sortingBottom: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					'images/icon1/home_searchResult/searchResult_bottom.png',
				sortingBottomh: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					'images/icon1/home_searchResult/searchResult_bottom_select.png',
				sortingTop: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					'images/icon1/home_searchResult/searchResult_top.png',
				sortingToph: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					'images/icon1/home_searchResult/searchResult_top_select.png',
				party: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/home_searchResult/party.png',
				two: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/home_searchResult/two.png',
				shoppingImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/redgwc.png',
                //商品默认图片
                defaultPicture: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/Default_picture.png',
				bottomIndex: 0,
				class_cid: '',
				loadingType: 0,
				bottom_list: [],
				class_pro: [],
				shopImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/allgoods_shopImg.png',
				listLeft: [],
				listRight: [],
				cardLeftHeight: 0,
				cardRightHeight: 0,
				show: true,
				searchType: '',
				filterMaskFlag: false,
				productList: [],
				layout: true,
				sorting: 'asc', //  desc：降序，  asc：升序
				fastTap: true,
				haveSkuBean: '', //选择规则属性
				numb: 1,
				allList: [], //所有商品列表
			};
		},
		components: {
			skus
		},
		watch: { 
            'shop_id':{
                handler(newVal,lodVal){
                    if(newVal > 0){ 
                        this.layout = false
                    }
                },
                immediate:true
            },
			page(val) {
				if (val > 1) {
					this.loadingType = 1;

					this.$emit('loadingType', 1, this.sorting, this.searchType)
				 
					this.$req.post({
						api: 'app.index.get_more',
						page: this.page,
						cid: this.class_cid,
						sort: this.sorting,
						sort_criteria: this.searchType
					}).then(res => {
						let {
							data
						} = res;
						if (res.code == 200) {
							if (data.length > 0) {
								data.filter(item => {
									item.vip_price = Number(item.vip_price).toFixed(2)
									item.vip_yprice = Number(item.vip_yprice).toFixed(2)
								})

								this.class_pro = this.class_pro.concat(data);
								setTimeout(() => {
									this.waterfall();
								}, 1)
								this.loadingType = 0;
								this.$emit('loadingType', 0, this.sorting, this.searchType)
							} else {
								this.loadingType = 2;
								this.$emit('loadingType', 2, this.sorting, this.searchType)
							}
						}
					});
				}
			}
		},
		created() {
			// 获取设备宽度
			uni.getSystemInfo({
				success: (e) => {
					this.isWidth = e.windowWidth / 5
				}
			})
			this.axios()
			this.setLang()
		},
		methods: {
			handleErrorImg(index) {
				this.productList[index].imgurl = this.defaultPicture
			},
			//确认
			_confirm(sku) { 
				Object.assign(this.$data, sku);
				if (!this.fastTap) {
					return;
				}
				this.fastTap = false;
				if (Boolean(this.haveSkuBean)) {
					if (this.num == 0) {
						uni.showToast({
							title: this.language.proList.Tips[0],
							duration: 1000,
							icon: "none",
						});
						this.fastTap = true;
					} else if (this.num != 0) {
						this._shopping();
					}
				} else {
					if (this.num == 0) {
						uni.showToast({
							title: this.language.proList.Tips[0],
							duration: 1000,
							icon: "none",
						});
						this.fastTap = true;
					} else {
						uni.showToast({
							title: this.language.proList.Tips[1],
							duration: 1000,
							icon: "none",
						});
						this.fastTap = true;
					}
				}
			},
			addShopping(item, id) {
				if (item && item.status == 3) {
					uni.showToast({
						title: '商品已下架',
						icon: 'none'
					})
					return
				}
				if (item && item.num == 0) {
					uni.showToast({
						title: '商品已售罄',
						icon: 'none'
					})
					return
				}
				var me = this
				console.log(this.fastTap)
				if (!this.fastTap) {
					return
				}
				this.fastTap = false
				this.proid = id
				let token = uni.getStorageSync('access_id')
			  
              var data = {
                  api: "app.product.index",
                  pro_id: id,
              };
              
              this.$req
                  .post({
                      data,
                  }).then((res) => {
                    me.fastTap = true
                    if(res.code != 200 ){
                        uni.showToast({
                            title:res.message,
                            icon:'none'
                        })
                        return
                    }
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
                                           
                    me.ys_price = qj_price
                    // me.goods=
                    me.attrList = attrList
                    
                    
                    me.$refs.attrModal.imgurl = pro.img_arr[0]
                    me.$refs.attrModal.num = pro.num
                    me.$refs.attrModal.price = qj_price
                    me.$refs.attrModal.skuBeanList = attribute_list
                    //虚拟商品 加入购物车不需要显示 库存
                    if (commodity_type == 1) {
                    	this.$refs.attrModal.pro2 = {
                    		commodity_type,
                    		write_off_settings
                    	}
                    }
                    me.$refs.attrModal.initData()
                    // 当只有一个规格且库存为0的时候不让它进sku
                    let flag = skuBeanList.every((item, index) => {
                    	return item.count == 0
                    })
                    if (flag) {
                    	uni.showToast({
                    		title: this.language.toasts.goodsDetailed.kucun,
                    		icon: 'none'
                    	})
                    	return
                    }
                    me.$refs.attrModal._mask_display()
                    
                    me.collection = type
                })
                .finally((error) => {
                    this.fastTap = true;
                });
             
			},
			_shopping() {
				if (this.haveSkuBean) {
					this.$req.post({
                        data:{
							api: "app.product.add_cart",
							pro_id: this.proid,
							attribute_id: this.haveSkuBean.cid,
							num: this.numb,
							type: "addcart",
							page: this.page,
							cartType: 2
						}}).then((res) => {
							let {
								code,
								data,
								message
							} = res;
							if (code == 200) {
								uni.showToast({
									title: this.language.proList.Tips[2],
									icon: "none",
								});
								this.$store.state.access_id = data.access_id
								this.haveSkuBean = "";
								this.$refs.attrModal._mask_f();
								this.fastTap = true;
								this.getCartNum()
							} else {
								uni.showToast({
									title: message,
									icon: "none",
								});
								this.fastTap = true;
							}
						})
						.catch((error) => {
							this.fastTap = true;
						});
				} else {
					this.fastTap = true;
				}
			},
			clickType(type) {
				if ((type == 'volume' || type == 'price') && this.searchType == type) {
					this.sorting = this.sorting == 'asc' ? 'desc' : 'asc'
				} else {
					this.sorting = 'asc'
				}
				this.searchType = type

				this.page = 1
				this.axios();
			},
			changeLayout() {
				this.layout = !this.layout
				let index = this.bottomIndex 
				if (!this.layout) {
					this.productList = this.allList[this.bottomIndex].list
				} else {
					this.$emit('changePage', 1)
					this.class_cid = this.bottom_list[index].cid
					this.empty();
					this.class_pro = this.bottom_list[index].list.concat()
					setTimeout(() => {
						this.waterfall();
					}, 1)

					if (this.class_pro.length > 0) {
						this.loadingType = 0
						this.$emit('loadingType', 0, this.sorting, this.searchType)
					} else {
						this.loadingType = 2
						this.$emit('loadingType', 2, this.sorting, this.searchType)
					}
				}

			},
			axios() {
				let data = {
					api: 'app.index.classList',
				}
				if (this.searchType != '') {
					data.sort_criteria = this.searchType
					data.sort = this.sorting
				}
				if (this.shop_id) {
					data.shop_id = this.shop_id
				}
				this.$req.post({
					data
				}).then(res => {
					let {
						data
					} = res
					if (res.code == 200) {
						console.log(data, 'data------data')

						let list2 = res.data.list;
						list2.filter(item => {
							item.list.filter(it => {
								it.vip_price = Number(it.vip_price).toFixed(2)
								it.vip_yprice = Number(it.vip_yprice).toFixed(2)
							})
						})

						this.bottom_list = list2;
						this.class_cid = res.data.list[this.bottomIndex]?.cid
						this.empty();
						this.class_pro = res.data.list[this.bottomIndex]?.list.concat() || []
						this.productList = res.data.list[this.bottomIndex]?.list
						this.allList = res.data.list
						setTimeout(() => {
							this.waterfall();
						}, 1)
					} else {
						uni.showToast({
							title: res.message,
							icon: 'none'
						})
					}
				})
			},
			onImageLoad(e, id) {
				let divWidth = 336; //实际显示的单栏宽度，345upx
				let oImgW = e.detail.width; //图片原始宽度
				let oImgH = e.detail.height; //图片原始高度
				console.log(oImgH);
				let rImgH = Math.round(divWidth * oImgH / oImgW * 100) / 100; //重新计算当前载入的card的高度
 
				const query = uni.createSelectorQuery().in(this);

				let height = 0;

				query.select('#' + id).boundingClientRect(data => {
					height = data.height;
				}).exec();

				rImgH = rImgH + (height * 2);

				if (e.target.dataset.name == 0) {
					this.cardLeftHeight += rImgH
				} else {
					this.cardRightHeight += rImgH
				}

				this.waterfall()
			},
			_goods(id) {
				this.$store.state.pro_id = id;
				uni.navigateTo({
					url: '/pagesC/goods/goodsDetailed?toback=true&pro_id=' + id
				});
			},
			waterfall() {
				if (this.class_pro.length == 0) {
					return
				}
				if (this.cardLeftHeight > this.cardRightHeight) {
					let cardList = this.class_pro.splice(0, 1); //初始化图片显示
					this.listRight.push(cardList[0]);
				} else if (this.cardLeftHeight <= this.cardRightHeight) {
					let cardList = this.class_pro.splice(0, 1); //初始化图片显示
					this.listLeft.push(cardList[0]);
				}
			},
			empty() {
				this.listRight = [];
				this.listLeft = [];
				this.cardLeftHeight = 0;
				this.cardRightHeight = 0;
			},
			changeBottom(index) {
				this.bottomIndex = index
				if (!this.layout) {
					this.productList = this.allList[this.bottomIndex].list
				} else { 
					this.$emit('changePage', 1)
					this.class_cid = this.bottom_list[index].cid
					this.empty();
					this.class_pro = this.bottom_list[index].list.concat()
					setTimeout(() => {
						this.waterfall();
					}, 1)

					if (this.class_pro.length > 0) {
						this.loadingType = 0
						this.$emit('loadingType', 0, this.sorting, this.searchType)
					} else {
						this.loadingType = 2
						this.$emit('loadingType', 2, this.sorting, this.searchType)
					}
				}
			},
		}
	}
</script>

<style lang="less">
	@import url("@/laike.less");

	.navTabBox {
		.goodsBox {
			padding-top: 10px;

			.seckill_content_view {
				display: flex;
			}

			.goods_title_item {
				display: flex;
				align-items: center;
				justify-content: space-between;
				flex-direction: column;
				height: 60rpx;
				padding: 0 30rpx;
				position: relative;
			}

			.goods_title .goods_title_item:not(:last-child) {
				// border-right: 1px solid #A1CECE;
			}

			.goods_title_item.active view {
				font-weight: bold;
                font-family: Source Han Sans, Source Han Sans;
                font-weight: 350;
                font-size: 32rpx;
                color: #333333;
			}

			.goods_title_item.active text {}

			.active-view {
				content: '';
				position: absolute;
				bottom: -16rpx;
				left: 50%;
				transform: translateX(-50%); 
                width: 96rpx;
                height: 4rpx;
				border-radius: 3rpx;
			}

			.goods_title_item view {
				text-align: center; 
				color: #020202;
				line-height: 34rpx;
                
				white-space: nowrap;
                font-family: Source Han Sans, Source Han Sans;
                font-weight: 350;
                font-size: 28rpx;
                color: #999999;
               
			}

			.goods_title_item text {
				color: #666666;
				font-size: 22rpx;
				line-height: 22rpx;
				white-space: nowrap;
			}


			.goods_ul {
				padding: 30rpx 30rpx 0;
				justify-content: space-between;
				flex-flow: row wrap;
				display: flex;
				font-size: 28rpx; 
                min-height: 500rpx;
			} 

			.goods_like {
				display: flex;
				flex-direction: column;
				width: 336rpx;
				border-radius: 10rpx;
				text-align: center;
				margin: 0 0 20rpx;
				background-color: #fff;
				overflow: hidden;
				position: relative;
			}

			.goods_like_img {
				width: 335rpx;
				height: 335rpx;
				margin: 10rpx auto;
			}

			.goods_like>p {
				padding: 0 20rpx;
				margin-top: 20rpx;
				text-align: left;
			}

			.overtitle { 
				line-height: 30rpx;
				margin-top: 4rpx !important;
				margin-bottom: 10rpx;
				overflow: hidden;
				text-overflow: ellipsis;
				display: -webkit-box;
				-webkit-box-orient: vertical;
				-webkit-line-clamp: 2;
				text-align: left;
				padding: 0 20rpx;
                
                font-family: Source Han Sans, Source Han Sans;
                font-weight: 500;
                font-size: 28rpx;
                color: #333333;
			}

			.goods_mun {
				display: flex;
				align-items: flex-end;
				justify-content: space-between;
				margin-top: auto;
				padding: 0 20rpx 20rpx 20rpx;
			}

			.goods_mun>span {
				display: flex;
			}

			.goods_mun img {
				width: 58rpx;
				height: 58rpx;
				margin-left: auto;
			}

			.goods_mun .yprice {
				display: block;
				font-size: 22rpx;
				color: #999999;
				text-decoration: line-through;
				margin-left: 6rpx;
				line-height: 20rpx;
			}

			.red {
				color: @priceColor;
			}


			.allgoods_price {
				display: flex;
				flex-direction: column;
				align-items: flex-start;
			}

			.allgoods_price view {
				font-size: 32rpx;
				line-height: 28rpx;
				margin-bottom: 10rpx;
				font-weight: bold;

				.currency {
					font-size: 24rpx;
					color: @priceColor;
				}

				.vip_price {
					font-size: 32rpx;
					color: @priceColor;
					margin-right: 10rpx;
                    
                    font-family: Source Han Sans, Source Han Sans;
                    font-weight: 700;
                    font-size: 34rpx;
                    color: #FA5151;
                    line-height: 34rpx;
				}

				.sales {
					font-size: 24rpx;
                    font-family: Source Han Sans, Source Han Sans;
                    font-weight: 500; 
                    color: #666666;
				}


			}

			.allgoods_price p,
			.lod-price {
				font-size: 24rpx;
				color: #999999;
				line-height: 24rpx;

				&.no_grade_vip_price {
					color: @btnBackground;
				}
			}


			.vip_price {
				color: @btnBackground;
			}

			.goods_ul-waterfall .goods_like_img {
				width: 100%;
				height: 100%;

				image {
					width: 100%;
				}
			}

			.goods_content_item_bottom {
				display: flex;
				align-items: center;
				margin-top: 15rpx;
			}

			.goods_content_item_bottom text {
				font-size: 20rpx;
				color: #999999;
				line-height: 20rpx;
			}

			.discover-wrap {
				display: flex;
				flex-direction: column;
				height: 100%;
			}

			.discover {
				position: absolute;
				display: flex;
				right: 0;
				bottom: 0;
				align-items: center;
				justify-content: center;
				width: 90rpx;
				height: 36rpx;
				background: rgba(118, 230, 182, 1);
				border-radius: 10rpx 0rpx 0rpx 0rpx;
				margin-left: auto;
				font-size: 22rpx;
				color: #014241;
			}

			.goods_content_item_icon {
				display: flex;
				/*margin-top: 20rpx;*/
				margin-right: 24rpx;
				padding-left: 20rpx;
				margin-bottom: 16rpx;
			}

			.goods_content_item_icon>view {
				display: flex;
				align-items: center;
				justify-content: center;
				width: 62rpx;
				height: 30rpx;
				// border:1px solid rgba(255,80,65,1);
				/*border-radius:13rpx;*/
				box-sizing: border-box;
				padding: 2rpx 4rpx;
				font-size: 20rpx;
				// color: #FF5041;
				margin-right: 10rpx;
			}
		}

		.dowmPro {
			width: 120rpx;
			height: 120rpx;
			border-radius: 50%;
			background-color: rgba(0, 0, 0, 0.6);
			position: absolute;
			top: 50%;
			left: 50%;
			transform: translate(-50%, -50%);
			color: #FFFFFF;
			font-size: 26rpx;
			line-height: 120rpx;
			text-align: center;
		}
	}

	.filterTopFilter {
		display: flex;
		align-items: center;
		height: 88rpx;
		z-index: 99;
		border-radius: 0px 0px 24rpx 24rpx;
		margin: 0 32rpx;

		.filter-left {
			flex: 1;
			display: flex;
			justify-content: space-between;

			>div {
				height: 100%;
				font-size: 28rpx;
				.center();
				color: #999999;

				.sortingImgBox {
					display: flex;
					flex-direction: column;
					justify-content: center;
					margin-left: 8rpx;
					height: 100%;

					img {
						width: 16rpx;
						height: 8rpx;
					}

					img:first-child {
						margin-bottom: 8rpx;
					}
				}

				.sortingImg {
					width: 20rpx;
					height: 20rpx;
					margin-left: 8rpx;
				}
			}
		}

		.active {
			color: #FA5151 !important;
		}

		.layoutBox {
			width: 40rpx;
			height: 40rpx;
		}

		.fiter_line {
			margin: 0 28rpx;
			width: 2rpx !important;
			background: rgba(0, 0, 0, 0.1);
			height: 20rpx;
		}
	}

	.fiterMask {
		border-radius: 0;
	}

	.allgoods_vertical {
		border-radius: 24rpx;
		background: #FFFFFF;
		padding: 10rpx 32rpx 32rpx;
        min-height: 500rpx;

		.allgoods_vertical_li {
			width: 100%;
			border-bottom: 1rpx solid rgba(0, 0, 0, .1);
			padding-bottom: 24rpx;

			&:nth-last-child(1) {
				border-bottom: 0;
				padding-bottom: 0;
			}

			.good_box {
				display: flex;
				align-items: center;
				justify-content: space-between;
				margin: 24rpx 0 0;

				.goodsImg {
					width: 222rpx;
					height: 222rpx;
					margin-right: 16rpx;
					position: relative;

					>div {
						.center();
						position: absolute;
						left: 50%;
						top: 50%;
						transform: translate(-50%, -50%);
						width: 60%;
						height: 60%;
						background-color: rgba(0, 0, 0, 0.5);
						border-radius: 50%;
						color: #FFFFFF;
						font-size: 28rpx;
					}

					image {
						border-radius: 16rpx;
						width: 100%;
						height: 100%;
					}

					img {
						border-radius: 16rpx;
						width: 100%;
						height: 100%;
					}
				}

				.goodsInfo {
					flex: 1;
					min-height: 222rpx;

					.storeName {
						font-size: 24rpx;

						font-weight: 400;
						color: #999999;
						margin-top: 16rpx;
						margin-bottom: 8rpx;
					}

					.goodsName {
						// height: 80rpx;
						font-size: 28rpx;

						font-weight: 500;
						color: #333333;
						margin-bottom: 12rpx;
					}

					.noPadding {
						padding-left: 0;
					}

					.goodsPrice {
						display: flex;
						align-items: center;
						justify-content: space-between;
						margin-bottom: 18rpx;

						// margin-top:24rpx;
						.priceInfo {
							flex: 1;
							display: flex;
							align-items: flex-end;

							.x_price {
								font-size: 34rpx;
								font-family: DIN-Bold, DIN;
								font-weight: bold;
								color: #FA5151;

								.x_price_icon {
									font-size: 24rpx;
								}
							}

							.y_price {
								text-decoration: line-through;
								margin-left: 8rpx;
								font-size: 24rpx;

								font-weight: 400;
								color: #999999;
							}
						}

					}

				}

				.verticalStyle {
					display: flex;
					flex-direction: column;
					justify-content: space-between;
				}
			}
		}
	}

	.joinCart {
		position: absolute;
		bottom: 16rpx;
		right: 16rpx;
		width: 48rpx;
		height: 48rpx;

		image {
			width: 48rpx !important;
			height: 48rpx !important;
		}
	}

	.joinCart_one {
		image {
			width: 48rpx !important;
			height: 48rpx !important;
		}
	}

	.goods_content_item_icon {
		display: flex;
		margin-right: 24rpx;
		padding-left: 20rpx;
		margin-bottom: 16rpx;
		flex-wrap: wrap;
	}

	.goods_content_item_icon>view {
		display: flex;
		align-items: center;
		justify-content: center;
		width: auto;
		padding: 0 6rpx;
		margin-bottom: 10rpx;
		height: 30rpx;
		border: 1px solid #42B4B3;
		box-sizing: border-box;
		padding: 2rpx 4rpx;
		font-size: 20rpx;
		margin-right: 10rpx;
	}

	.new_goods_goods_icon {
		>view {
			padding: 0;
			margin: 0;
			margin-bottom: 8rpx;
			border: initial;
			min-width: 64rpx;
			height: 40rpx;
			border-radius: 8rpx;
			color: #ffffff;
			background: #42B4B3;
			margin-right: 8rpx;
		}

		.color1 {
			background: #FA5151;
		}

		.color2 {
			background: #FA9D3B;
		}

		.color3 {
			background: #1485EE;
		}
	}

	.shop_name {
		display: flex;
		align-items: center;
		margin: 16rpx;
		margin-top: 10rpx;
		margin-bottom: 8rpx;

		/deep/ .isImage {
			margin-right: 12rpx;
		}

		>span {
			color: #999;
			font-size: 24rpx;
			line-height: 32rpx;
            font-family: Source Han Sans, Source Han Sans;
            font-weight: 500; 
		}
	}
  
</style>
