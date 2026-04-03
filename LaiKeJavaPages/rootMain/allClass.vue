<template>
	<view class="allclass" :style="style" @touchmove.stop >
		<heads :title="language.productSelect.classify" :titleColor="'#333333'" :bgColor="bgColor" ishead_w="98">
		</heads>
		<!-- 顶部搜索 -->
		<view class="class-header" v-if="isShow&&navIndex != 0 || sid != ''" :style="{height: headerH}">
			<view class="header-view" :style="{paddingTop: data_height}">
				<view>
					<view class="header-view-back" @tap="_back" v-if="sid != ''">
						<image :src="new_back"></image>
					</view>
					<view class="header-view-search" v-if="navIndex != 0">
						<image class="serchimg" :src="serchimg" mode="widthFix"></image>
						<input v-model="is_search" type="text"
							:placeholder="`${placeholder}`"
							placeholder-style="color:#999" />
						<image class="schimg" :src="sc_icon" mode="widthFix" v-if="is_search.length>0"
							@tap="is_search=''"></image>
						<view class="search-btn" @tap="_search(is_search)">{{language.search2.search.search}}</view>
					</view>
			 
					<view class="header-view-choose" v-if="isStyleShow" @tap="chooseType = !chooseType">
						<image :src="chooseType?vertical:horizontal"
							:style="{width: navIndex > 0 ? '54rpx': '0', height: navIndex > 0 ? '54rpx': '0'}"></image>
					</view>
				</view>
			</view>
		</view> 
		<!-- 内容 -->   
		<view class="class-content"> 
			<!-- 左侧导航 -->
             
			<view class="class-content-left" :style="{width: chooseType ? '0': scrollW}">
               
				<view class="isScrollView" id="isScrollView1" :style="{width: chooseType ? '0': scrollW}">
                    <!-- 一级分类 -->
					<scroll-view class="isScrollView_nav" scroll-y="true" scroll-with-animation="true"
						:scroll-into-view="scrollIndex" :style="scrollH">
						<view class="isScrollView_nav_view">
							<view class="isScrollView_nav_item" :id="'scroll'+index"
								:class="{ active: index == navIndex, set: index === navIndexs}"
								v-for="(item, index) of is_ScrollView" :key="index" @tap="_changeNav(item, index)"
                                :style="{
                                    background: `${index == navIndex ? activeBgColor : ''} !important`,
                                    color: `${index == navIndex ? color : ''} !important`,
                                }"
                                >
								<view>{{ item.cate_name }}</view>
							</view>
						</view>
					</scroll-view>
				</view>
			</view>
			<!-- 右侧内容 -->
			<view class="class-content-right"> 
				<!-- 全部分类 -->
              
				<view class="right-all" v-if="navIndex == 0">
					<scroll-view class="right-all-nav" style="overflow-y: auto;" scroll-y="true"
						scroll-with-animation="true" :style="nonelistHength">
                        <view class="right-all-item"
                            v-for="(item, index) in is_ScrollViewAll"
                            :key="index"
                            @tap="_onAllBlockTap(item, index)"
                            :id="'id'+item.cate_id">
                            <view class="all-item-title" @tap.stop="_onAllTitleTap(item, index)">{{item.cate_name}}</view>
                            <view class="all-item-content" v-if="item.children && item.children.length > 0">
                                <view class="content-item" v-for="(itm, idx) in item.children" :key="idx"
                                    @tap.stop="_goXiaj(item,itm)">
                                    <image :src="itm.picture" mode='aspectFit'></image>
                                    <view>{{itm.name}}</view>
                                </view>
                            </view>
                            <view class="all-item-content-no" v-else>
                                <image :src="no_search"></image>
                                <view>{{ (language.zdata && language.zdata.zwsj) ? language.zdata.zwsj : '暂无分类' }}</view>
                            </view>
                        </view>
					</scroll-view>
				</view>
				<!-- 子类商品列表 -->
				<view class="right-item" v-else>
					<view>
						<!-- 次一级导航 -->
						<view class="right-item-top" :style="{height: headerH2}"
							v-if="is_ScrollView[navIndex].children.length>0">
							<view class="isScrollView" id="isScrollView2">
								<scroll-view class="isScrollView_nav" scroll-x="true" scroll-with-animation="true"
									:scroll-into-view="scrollIndex2" :style="chooseType?'width:726rpx':scrollH2">
									<view class="isScrollView_nav_view">
										<view class="isScrollView_nav_item" :id="'scroll2'+index"
											:class="{ active: index == navIndex2 }"
											v-for="(item, index) of is_ScrollView[navIndex].children" :key="index"
											@tap="_changeNav2(item, index)">
											<image :src="item.picture" mode="aspectFit"></image>
											<view>{{ item.name }}</view>
										</view>
									</view>
								</scroll-view>
							</view>
						</view>
						<!-- 筛选 -->
						<view class="right-item-shaix">
							<view :style="chooseType?'width:726rpx':''">
								<view :class="{active: shaixType==1}" @tap="_shaixType(1)">
									{{language.search2.searchRes.zh}}
								</view>
								<view :class="{active: shaixType==2}" @tap="_shaixType(2)">
									{{language.search2.searchRes.xl}}
									<view class="volume-px">
										<image :src="volumePx=='asc'?sortingToph:sortingTop" mode=""></image>
										<image :src="volumePx=='desc'?sortingBottomh:sortingBottom" mode=""></image>
									</view>
								</view>
								<view :class="{active: shaixType==3}" @tap="_shaixType(3)">
									{{language.search2.searchRes.jg}}
									<view class="price-px">
										<image :src="pricePx=='asc'?sortingToph:sortingTop" mode=""></image>
										<image :src="pricePx=='desc'?sortingBottomh:sortingBottom" mode=""></image>
									</view>
								</view>

								<view :class="{active: shaixType==5}" @tap="_shaixType(5)">
									{{language.search2.searchRes.sx}}
									<view class="filter-sx">
										<image :src="shaixType==5?filter_h:filter_hu" mode=""></image>
									</view>
								</view>
							</view>
						</view>
						<!-- 横向排列 --> 
						<view class="item-type1" v-if="!chooseType" ref='itemType1Scroll'>
							<template v-if="class_pro.length">
								<scroll-view class="item-type1-scroll" scroll-y="true" scroll-with-animation="true"
                                    id='scrollContainer'
									:scroll-top="itemTypeScroll" :style="goodsHeight" @scrolltolower="pageScroll" @scroll="onScroll">
									<view class="item-type1-goods" v-for="(item, index) in class_pro" :key="index"
										@tap.stop="_seeGoods(item)">
										<view class="goods-img">
											<image :src="item.imgurl"></image>
											<view v-if="item.status == 3 || item.contNum == 0" class="dowmPro">
												{{item.status == 3?language.waterfall.type[0]:item.contNum == 0?language.waterfall.type[1]:''}}
											</view>
										</view>
										<view class="goods-text">
											<view>
												{{item.product_title}}
											</view>
											<view>
												<span v-for="(it, ix) in item.s_type_list" :key="it.id"
													:style="{backgroundColor: it.color}">{{it.name}}</span>
											</view>
											<view class="goods-text-price">
												<view>
													<view class="price">
														<view>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</view>
														<view>{{LaiKeTuiCommon.formatPrice(item.price)}}</view>
														<view>
															{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(item.yprice)}}
														</view>
													</view>
													<view class="shopName">{{item.mch_name}}</view>
												</view>
												<view v-if="item.is_appointment != 2" class="jiagou"
													@tap.stop="_addShopCar(item, item.pid)">
													<image :src="addShopCarImgSrc"></image>
												</view>
											</view>
										</view>
									</view>
								</scroll-view>
							</template>
							<template v-else>
								<view class="item-type1-scroll-no" :style="goodsHeight">
									<view class="goods-no">
										<image :src="no_search"></image>
										<view>暂无商品</view>
									</view>
								</view>
							</template>
						</view>
						<!-- 纵向排列 -->
						<view class="item-type2" v-if="chooseType" :style="goodsHeight">
							<scroll-view class="item-type2-scroll" scroll-y="true" scroll-with-animation="true"
								:scroll-top="itemTypeScroll" :style="goodsHeight">
								<view class="item-type2-goods">
									<waterfall ref="waterfall" :goodsLike="false" :addShopCar="true" :mchLogo="false"
										:goodsPriceText="false" :isDataKO="isDataKO" @_seeGoods="_seeGoods"
										@_addShopCar="_addShopCar" :goodList="class_pro">
									</waterfall>
								</view>
							</scroll-view>
						</view>
					</view>
				</view>
			</view>
		</view> 
		<!-- 筛选弹窗 -->
		<screenPicker :showScreenPicker="shaixShare" :brand_list="brand_list" @confirm="handleScreenConfirm"
			@reset="resetScreen" @cancel="shaixShare = false" />
		<!--  遮罩：点击购物小图标弹出  -->
		<skus ref="attrModal" @confirm="_confirm"></skus>
	</view>
</template>

<script> 
	import {
		mapMutations
	} from 'vuex';
	import skus from '@/components/skus.vue';
	import screenPicker from './components/screenPicker.vue';
	import waterfall from "@/components/aComponents/waterfall.vue"; 
	import { buildUrlFromBackend } from "@/common/pluginsutil.js";
    import {
        getDIYPageInfoById
    } from "@/common/util.js";
	export default {
		data() {
			return { 
				bgColor: [{
						item: "rgba(255, 255, 255, 0)",
					},
					{
						item: "rgba(255, 255, 255, 0)",
					},
				], 
				headerH: '', //顶部高度 
				headerH2: '', //次一级导航高度
				serchimg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + "images/icon1/searchNew.png", //搜索
				sc_icon: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + "images/icon1/delete2x.png", //清除 
				is_search: '', //搜索内容
				data_height: '', //手机信息栏高度
				scrollH: '', //左侧导航高度（不设置，不能滚动）
				scrollH2: '', //右侧次一级导航宽度（不设置，不能滚动）
				scrollW: '', //左侧导航宽度，用于占位
				is_ScrollView: [], //左侧导航
				is_ScrollViewAll: [], //右侧 全部分类 导航
				navIndex: 0, //当前选中的下标
				navIndexs: '', //右侧滚动到哪个分类了
				scrollIndex: '', //对应子标签id位置
				chooseType: false, //显示类型
				navIndex2: null, //当前选中的下标（次一级）
				scrollIndex2: '', //对应子标签id位置（次一级）
				sortingBottom: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					'images/icon1/home_searchResult/searchResult_bottom.png',
				sortingBottomh: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					'images/icon1/home_searchResult/searchResult_bottom_select.png',
				sortingTop: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					'images/icon1/home_searchResult/searchResult_top.png',
				sortingToph: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					'images/icon1/home_searchResult/searchResult_top_select.png',
				filter_hu: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					'images/icon1/home_searchResult/searchResult_filter.png',
				filter_h: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					'images/icon1/home_searchResult/searchResult_filter_select.png',
				volumePx: '', //销量排序 asc desc
				pricePx: '', //价格排序 asc desc 
				shaixType: 1, //当前排序类型 1综合 2销量 3价格 4评价 5筛选
				goodsHeight: '', // 商品列表高度
				class_pro: [], //分类商品：商品列表
				page: 1, //分页
				isDataKO: false, //是否清除上一次的数据瀑布流
				new_back: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + "images/icon/new_back.png",
				addShopCarImgSrc: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/redgwc.png',
				vertical: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					'images/icon1/home_searchResult/searchResult_vertical.png',
				horizontal: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					'images/icon1/home_searchResult/searchResult_horizontal.png', 
				shaixShare: false, //筛选弹窗
				brand_list: [], //筛选品牌列表 
				min_price: '', //最低价
				max_price: '', //最高价
				brand_id: '', //品牌id 
				sid: '', //上级id
				itemTypeScroll: -1, //商品列表置顶
				no_search: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/coupon_qsy.png',
				fastTap: true,
				haveSkuBean: "",
				proid: "",
				num: "",
				numb: "",

				cid: '',
				keyword: '',
				query_criteria: '',
				nonelistHength: '', // 右侧可视区域高度 
				classInfoHeight: '',
                tabbarShow:false,
                styleType: 1,
                type:'pureColor',
                isShow: true,
                isStyleShow: true,
                placeholder : '请输入标题',
                activeBgColor : '',
                color : '',
                pureColor:[
                  {
                    item: '#ffffff'
                  }
                ],
                graduatedColor:[
                  {
                    item: '#ffffff'
                  },
                  {
                    item: '#ffffff'
                  }
                ], 
                style:'background-image: url(' + this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
					'images/icon1/auction/grbj.png);background-size: 100vw 100vh;background-repeat: no-repeat;'
			};
		},
		components: {
			waterfall,
			skus,
			screenPicker, 
		},
		computed: {
			listHeight() {
				let a = (Number(this.headerH.substring(0, 2)) || 0) + 'px'
				let b = (Number(this.headerH2.substring(0, 3)) || 0) + 'px'
                const c=  `height: calc( 100vh - ${this.classInfoHeight} - 104px - ${b} );` 
				return c
			}
		},
		watch: {
			listHeight(val) {
				this.goodsHeight = val 
			},
			navIndex(val) {
				if (val > 0) {
					if (this.is_ScrollView[val].children.length == 0) {
						this.headerH2 = '0px' 
					} else {
						this.headerH2 = '106px'
					}
                    this.scrollH = `height: calc(100vh - ${this.classInfoHeight} - 55px);`
				} else{
                    // 一级分类
                    this.scrollH = `height: calc(100vh - ${this.classInfoHeight} + 55px);` 
                }
			},
            
		},
		onPageScroll(e) {
			if (this.navIndex > 0) {
				this.navIndexs = ''
				return
			}
			const query = uni.createSelectorQuery().in(this);
			this.is_ScrollViewAll.forEach((item, index) => {
				let name = '#id' + item.cate_id
				query
					.select(name)
					.boundingClientRect((data) => {
						if (data.top >= 0 && data.top <= 50) {
							this.navIndexs = index + 1
						}
					})
					.exec();
			})
		},
	 
		mounted() {
            /** 防止 从别的页面跳转过来 因为 页面栈缓存不触发  mounted 钩子 而封装的普通函数
            * 改为通过 shell 手动触发 
            */
            this.triggerMounted()
		},
		 
		methods: {
            ...mapMutations({ 
            	setCartNum: 'SET_CART_NUM'
            }),
            triggerMounted(){
                this.syncMallTitle()
                //如果有image有mode="widthFix"属性，mode会在最后执行，会造成以下方法获取高度出错误，用setTimeout可解决。
                const query = uni.createSelectorQuery().in(this);
                query
                	.select(".header-view")
                	.boundingClientRect((data) => {
                		this.headerH = 50 + "px";
                		let height = (data?.height + 50) + "px"; 
                	})
                	.exec();
                this.data_height = uni.getStorageSync("data_height") + 'px'
                
                this._axios()
                
                
                const tabber_info = uni.getStorageSync('tabbar_info') 
                
                if(tabber_info){ 
                    const  tabberInfo = JSON.parse(tabber_info)  
                    this.style =`background-color:${tabberInfo.colorTwo}`
                }
                
                const pageInfo = getDIYPageInfoById({
                    key:'homeItem17569763719111'
                }) 
                if(pageInfo && Object.keys(pageInfo).length){ 
                    const key = Object.keys(pageInfo)[0]
                    let pageConfig = pageInfo[key]
                    this.setConfig(pageConfig) 
                }
                
                this.$nextTick(()=>{
                     const index = JSON.parse(uni.getStorageSync('tabbar')).findIndex(v=>(v.url || v.pagePath) == 'pages/tabBar/allClass')
                     uni.setStorageSync('tabbarIndex',index) 
                })
                  
                this.getCart();
                this.getSystemInfo()
            },
            setConfig(data) {
               
               if (data) {
                 let info = data.c_classification_info
                 this.styleType = info.styleType 
                 this.type = info.type;
                 this.isShow = info.isShow;
                 this.isStyleShow = info.isStyleShow;
                 this.placeholder = info.placeholder;
                 this.activeBgColor = info.bgColor[0].item;
                 this.color = info.color[0].item;
                 this.pureColor = info.pureColor;
                 this.graduatedColor = info.graduatedColor;
                 this.chooseType = info.styleType == 1 ? true : false;
                 
                 const tabber_info = uni.getStorageSync('tabbar_info')
                 if(tabber_info){
                     
                     let obj  = {}
                     if(typeof tabber_info == 'string'){
                         obj = JSON.parse(tabber_info)
                     }else{
                         obj =  tabber_info
                     }
                     if(obj.backType == 0){
                         // 颜色设置
                         this.style = { backgroundColor: obj.colorTwo }
                     }else {
                         // 图片设置
                         this.style =  {
                                backgroundImage: `url(${obj.imgurl})`,   
                                backgroundRepeat: 'no-repeat',  
                                backgroundSize: '100% 100%'  
                            } 
                     } 
                 }
               }
            },
 			handleScreenConfirm(data) {
				this.sort = data.sort;
				let query_criteria = {
					brand_id: data.brand_id,
					min_price: data.min_price,
					max_price: data.max_price
				}
				query_criteria = JSON.stringify(query_criteria)
			 
				this.shaixType = data.sort != '' ? 4 : '';
				this.page = 1;
				this.shaixShare = false;
				this.getList(this.cid, this.keyword, query_criteria);
			},
			resetScreen() {
				this.sort = '';
				this.shaixType = 1;
				let query_criteria = {
					brand_id: '',
					min_price: '',
					max_price: ''
				}
				query_criteria = JSON.stringify(query_criteria)
				this.shaixShare = false;
				this.page = 1;
				this.getList(this.cid, this.keyword, query_criteria);
			},
			getSystemInfo() {
				uni.getSystemInfo({
					success: (res) => { 
						// 减去hader组件、减去搜索框高度 、减去 刘海高度、减去 tabbar组件 
                        let tabbatHeight = 0
                        if(!this.tabbarShow ){
                            tabbatHeight = res.windowBottom
                        }else{
                            tabbatHeight = 65
                        } 
						const height = (44 + 44 + res.statusBarHeight + tabbatHeight) + 'px'
						this.classInfoHeight = height
						// 设置 分类以及 全部分类右侧内容的显示高度
						 this.nonelistHength = `height: calc(100vh - ${height} - 18px);`
                        this.scrollH = `height: calc(100vh - ${this.classInfoHeight} - 18px);`
					},
					fail: (err) => {
						console.error('获取系统信息失败:', err);
					}
				});
			},
			/**
			 * @param {Object} item
			 * 商品查询
			 */
            pageScroll(e){ 
               const { scrollTop, scrollHeight } = e.detail;  
                 // 判断是否滑动到了最底部
                 
            },
             // onScroll(e) {
             //   const { scrollTop, scrollHeight,clientHeight } = e.detail;
             //    let he 
             //    if(this.is_ScrollView[this.navIndex].children.length>0){
             //        he = scrollHeight-30
             //    }else{
             //        he = scrollHeight-150
             //    }
             //    uni.showToast({
             //        title:`${scrollTop + 412}-- ${he}`,
             //        icon:'none'
             //    })
             //    if (Number(scrollTop + 412)  >= Number(he)) {
             //        this.page ++
             //       this.getList(this.cid,this.keyword,this.query_criteria)
             //    } 
             //  },
             onScroll(e) { 
               // 1. 解构滚动参数（只取确定存在的 scrollTop）
               const { scrollTop, scrollHeight } = e.detail || {};
               // 容错：scrollTop/scrollHeight 不存在则直接返回
               if (scrollTop === undefined || scrollHeight === undefined) return;
             
               // 2. 获取滚动容器的实际可视高度（关键：替代 undefined 的 clientHeight）
               const query = uni.createSelectorQuery().in(this);
               // 注意：替换为你的 scroll-view 容器的类名/ID（比如 #scrollContainer 或 .scroll-view）
               query.select('#scrollContainer').boundingClientRect(rect => {
                 if (!rect) return; // 容器未找到则返回
                 const clientHeight = rect.height; // 容器的实际可视高度
             
                 // 3. 容错处理：is_ScrollView 取值
                 const currentScrollView = this.is_ScrollView?.[this.navIndex] || {};
                 const childrenLen = currentScrollView.children?.length || 0;
             
                 // 4. 滚动到底部判断（通用公式）
                 const loadThreshold = 50; // 提前50px加载，可调整
                 const isReachBottom = scrollTop + clientHeight >= scrollHeight - loadThreshold;
             
                 // 调试用
                 // 5. 触底加载更多
                 if (isReachBottom) { 
                   this.page++;
                   this.getList(this.cid, this.keyword, this.query_criteria) 
                 }
               }).exec();
             },
			/**
			 * 查看下级分类
			 */
			_goXiaj(item,itm) {
				uni.pageScrollTo({
					scrollTop: 0,
					duration: 100
				});
                uni.navigateTo({
                    url: `/pagesC/goods/goods?cid=${itm.child_id}&name=${itm.name}`
                });
				// this._axios(item.cate_id)
			},
            /**
             * 全部分类标题点击
             * 标题始终可触发左侧对应分类tab
             */
            _onAllTitleTap(item, index) {
                this._changeNav(item, index + 1);
            },
            /**
             * 全部分类整块点击
             * 仅无二级分类时，允许整块触发左侧分类tab
             */
            _onAllBlockTap(item, index) {
                const children = Array.isArray(item?.children) ? item.children : [];
                if (children.length > 0) {
                    return;
                }
                this._changeNav(item, index + 1);
            },
			/**
			 * 查看商品详情
			 * @param {Object} id
			 */
			// 营销商品跳转解析（与首页一致）
			_resolveMarketingUrl(marketingParams) {
				if (!marketingParams) return "";
				let params = marketingParams;
				if (typeof params === "string") {
					const trimmed = params.trim();
					if (!trimmed || trimmed === "null" || trimmed === "undefined") return "";
					if ((trimmed.startsWith("{") && trimmed.endsWith("}")) || (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
						try {
							params = JSON.parse(trimmed);
						} catch (e) {
							params = trimmed;
						}
					} else {
						params = trimmed;
					}
				}
				if (Array.isArray(params)) {
					for (let i = 0; i < params.length; i += 1) {
						const url = this._resolveMarketingUrl(params[i]);
						if (url) return url;
					}
					return "";
				}
				if (params && typeof params === "object") {
					const plugin = params.plugin || params.pluginCode || params.plugin_key || params.code || params.type;
					const paramStr = params.paramStr || params.params || params.param || params.query || params.urlparam;
					if (plugin) {
						const url = buildUrlFromBackend(plugin, paramStr);
						if (url) return url;
					}
					const keys = Object.keys(params);
					for (let i = 0; i < keys.length; i += 1) {
						const key = keys[i];
						const value = params[key];
						if (!value) continue;
						if (typeof value === "string") {
							const url = buildUrlFromBackend(key, value);
							if (url) return url;
						} else if (typeof value === "object") {
							const innerParam = value.paramStr || value.params || value.param || value.query || value.urlparam;
							const url = buildUrlFromBackend(key, innerParam);
							if (url) return url;
						}
					}
					return "";
				}
				if (typeof params === "string") {
					const trimmed = params.trim();
					if (!trimmed) return "";
					const parts = trimmed.split("?");
					if (parts.length > 1 && parts[0]) {
						const url = buildUrlFromBackend(parts[0], parts.slice(1).join("?"));
						if (url) return url;
					}
				}
				return "";
			},

			// 点击商品：优先跳转营销插件详情，否则普通详情
			_seeGoods(payload) {
				let target = null;
				let id = payload;
				if (payload && typeof payload === "object") {
					target = payload;
					id = payload.pid || payload.id;
				} else {
					target = this.class_pro.find((item) => String(item.pid || item.id) === String(payload));
					id = payload;
				}
				const url = this._resolveMarketingUrl(target && target.marketingParams);
				if (url) {
					uni.navigateTo({ url });
					return;
				}
				this._goods(id);
			},

			_goods(id) {
				uni.navigateTo({
					url: "/pagesC/goods/goodsDetailed?toback=true&pro_id=" + id,
				});
			},
			/**
			 * 加入购物车
			 * @param {Object} item
			 * @param {Object} id
			 */
			_addShopCar(item, id) {
				if (item && item.status == 3) {
					uni.showToast({
						title: "商品已下架",
						icon: "none",
					});
					return;
				}
				if (item && item.num == 0) {
					uni.showToast({
						title: "商品已售罄",
						icon: "none",
					});
					return;
				}
				this.shopping_j(id);
			},
			/**
			 * 显示sku弹窗
			 * @param {Object} id
			 */
			shopping_j(id) {
				if (!this.fastTap) {
					return;
				}
				this.fastTap = false;
				this.proid = id;
				var data = {
					api: "app.product.index",
					pro_id: id,
				};
				this.$req.post({
						data
					}).then((res) => {
						this.fastTap = true;
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
							},
						} = res;
						this.$refs.attrModal.imgurl = pro.img_arr[0];
						this.$refs.attrModal.num = pro.num;
						this.$refs.attrModal.price = qj_price;
						this.$refs.attrModal.skuBeanList = attribute_list;
						this.$refs.attrModal.initData();
						this.$refs.attrModal._mask_display();
					})
					.catch((error) => {
						this.fastTap = true;
					});
			},
			/**
			 * sku组件点击确认
			 * @param {Object} sku
			 */
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
			/**
			 * 调用加入购物车接口
			 * @param {Object} id
			 */
			_shopping(id) {
				if (this.haveSkuBean) {
					var data = {
						api: "app.product.add_cart",
						pro_id: this.proid,
						attribute_id: this.haveSkuBean.cid,
						num: this.numb,
						type: "addcart",
					};
					this.$req.post({
							data
						}).then((res) => {
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
								this.getCart()
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
			 
			/**
			 * 返回上一级
			 */
			_back() {
				this.navIndex = 0
				this.navIndex2 = null
				this.navIndexs = ''
				this._axios(this.sid)
			},
			/**
			 * 搜索数据
			 */
			_search(is_search, query_criteria) {
				if (this.navIndex == 0) {} else {
					this.page = 1
					if (this.navIndex && this.navIndex2 != null) {
						this.getList(this.is_ScrollView[this.navIndex].children[this.navIndex2].child_id, is_search || '',
							query_criteria || '')
					} else {
						this.getList(this.is_ScrollView[this.navIndex].cate_id, is_search || '', query_criteria || '')
					}
				}
			},
			/**
			 * 获取商品列表
			 * @param {Object} cid
			 */
			async getList(cid, keyword, query_criteria) {
				if (this.navIndex == 0) {
					return
				}
				if (this.page == 1) {
					//每次点击 都得初始化上一次商品瀑布流的数据
					this.isDataKO = true;
					//500毫秒，取消初始化
					setTimeout(() => {
						this.isDataKO = false;
					}, 500);
				}
				this.cid = cid
				this.keyword = keyword
				this.query_criteria = query_criteria

				let data = {
					api: 'app.search.listdetail',
					page: this.page,
					keyword: keyword || '',
					query_criteria: query_criteria || '',
					cid,
				}
				switch (this.shaixType) {
					case 2:
						//销量排序
						data.sort_criteria = 'volume'
						data.sort = this.volumePx
						break
					case 3:
						//价格排序
						data.sort_criteria = 'price'
						data.sort = this.pricePx
						break
					case 4:
						//评论数排序
						data.sort_criteria = 'comment_num'
						data.sort = this.sort
						break
					default:
				}
				await  this.$req.post({
					data
				}).then(res => {
					if (res.code != 200) {
						uni.showToast({
							title: res.message,
							icon: 'none'
						})
						return
					}
					res.data.pro.forEach((item, index) => {
						if (item.imgurl == '') {
							item.imgurl =  require("@/static/img/Default_picture.png") 
						}
						//可能有相同路径的图片，会无法加载@load事件；拼接随机字符串，可以解决这个问题
						item.imgurl = item.imgurl + '?a=' + Math.floor(Math.random() * 1000000);
						item.volume = item.payPeople;
					});
					if (this.page == 1) {
						this.class_pro = res.data.pro
					} else {
						this.class_pro.push(...res.data.pro)
					}
					this.brand_list = res.data.brand_class_list
				})
			},
			/**
			 * 接口数据
			 */
			_axios(cid) {
				var data = {
					api: 'app.search.index',
					cid: cid || ''
				};
				this.$req.post({
					data
				}).then(res => {
					let {
						data
					} = res;
                    const list = Array.isArray(data.List) ? data.List : [];
                    const allListChildren = Array.isArray(data?.allList?.children) ? data.allList.children : [];
                    const sourceList = allListChildren.length ? allListChildren : list;

					sourceList.forEach((item) => {
                        const children = Array.isArray(item.children) ? item.children : [];
						children.forEach((it) => {
							if (it.picture == '') {
								it.picture = require("@/static/img/Default_picture.png");
							}
						})
					})
					this.is_ScrollView = JSON.parse(JSON.stringify(sourceList))
					this.is_ScrollView.unshift(data.allList)
					this.is_ScrollViewAll = sourceList
					this.sid = data.sid
					this.$nextTick(() => {
						this.get_scrollW()
					})
					//
					uni.stopPullDownRefresh()
				})
			},
			/**
			 * 获取左侧导航高度
			 */
			get_scrollW() {
				const query = uni.createSelectorQuery().in(this);
				query
					.select("#isScrollView1")
					.boundingClientRect((data) => {
						this.scrollW = data.width + "px";
						let width = (data.width + 12) + "px";
						if (uni.getStorageSync('getSystemWidth') && uni.getStorageSync('getSystemWidth').value > 920) {
							this.scrollH2 = `width: calc(375px - ${width});`
						} else {
							this.scrollH2 = `width: calc(375px - ${width});`
                            
						}
					})
					.exec();
			},
	 
			/**
			 * 排序类型
			 */
			_shaixType(val) {
				if (this.shaixType == val && val == 2) {
					//销量排序
					this.pricePx = ''
					if (this.volumePx == 'asc') {
						this.volumePx = 'desc'
					} else {
						this.volumePx = 'asc'
					}
				} else if (this.shaixType == val && val == 3) {
					//价格排序
					this.volumePx = ''
					if (this.pricePx == 'asc') {
						this.pricePx = 'desc'
					} else {
						this.pricePx = 'asc'
					}
				} else if (this.shaixType != val && val == 2) {
					this.volumePx = 'asc'
					this.pricePx = ''
				} else if (this.shaixType != val && val == 3) {
					this.pricePx = 'asc'
					this.volumePx = ''
				} else {
					this.volumePx = ''
					this.pricePx = ''

				}
				this.shaixType = val
				if (this.shaixType != 5) {
					this._search()
				} else {
					this.shaixShare = true
				}
			},
			/**
			 * 切换导航
			 * @param {Object} item
			 * @param {Object} index
			 */
			_changeNav(item, index) {
				//防重复点击
				if (this.navIndex == index) {
					return
				}
				//初始化数据
				this.navIndexs = ''
				this.navIndex2 = null
				this.page = 1
				this.is_search = ''
				uni.pageScrollTo({
					scrollTop: 0,
					duration: 0,
				});
				//选中的当前下标
				this.navIndex = index
				//分类导航自动滚动
				this.scrollIndex = 'scroll' + (index - 1)
				//获取商品列表
				this.getList(this.is_ScrollView[index].cate_id)
			},
			/**
			 * 切换导航 （次一级）
			 * @param {Object} item
			 * @param {Object} index
			 */
			_changeNav2(item, index) {
				//防重复点击
				if (this.navIndex2 == index) {
					return
				}
				//初始化数据
				this.page = 1
				this.is_search = ''
				//选中的当前下标
				this.navIndex2 = index
				//分类导航自动滚动
				this.scrollIndex2 = 'scroll2' + (index - 1)
				//获取商品列表
				this.getList(this.is_ScrollView[this.navIndex].children[index].child_id)
			},
		 
			/**
			 *  获取tabbar购物车右上角数量（小红点）
			 */
			getCart() {
				var data = {
					api: 'app.cart.index',
					cart_id: '',
					page: 1
				}
				this.$req.post({
					data
				}).then(res => {
					let {
						code,
						data: {
							data,
							list,
							mch_list,
							login_status,
							grade
						},
						message
					} = res
					if (code == 200) { 
						 this.setCartNum(data.length)
					} 
				})
			},
		}
	};
</script>
<style>
	page {
		background-color: #f4f5f6;
	}
</style>
<style scoped lang="less">
	/deep/.uni-scroll-view::-webkit-scrollbar {
		display: none;
	}

	/deep/ .uni-scroll-view,
	.uni-scroll-view-content {
		height: 100%;
	}

	.allclass {
		.class-header {
			.header-view {
				width: 100%;
				display: flex;
				align-items: center;
				justify-content: center;
				position: fixed;
				top: 88rpx;
				z-index: 999;

				>view {
					flex: 1;
					padding: 12rpx 32rpx 28rpx 32rpx;
					box-sizing: border-box;
					display: flex;
					align-items: center;
					justify-content: space-between;

					.header-view-back {
						display: flex;
						margin-right: 32rpx;

						>image {
							width: 24rpx;
							height: 48rpx;
						}
					}

					.header-view-search {
						flex: 1;
						background-color: #FFFFFF;
						border-radius: 32rpx;
						padding: 4rpx;
						box-sizing: border-box;
						display: flex;
						align-items: center;
						justify-content: space-between;
						margin-right: 24rpx;

						.serchimg {
							width: 30rpx;
							height: 30rpx;
							margin-left: 12rpx;
						}

						>input {
							flex: 1;
							font-size: 24rpx;
							margin-left: 12rpx;
						}

						.schimg {
							width: 30rpx;
							height: 30rpx;
							margin-right: 16rpx;
						}

						.search-btn {
							display: flex;
							align-items: center;
							justify-content: center;
							padding: 8rpx 24rpx;
							box-sizing: border-box;
							background-color: #FA5151;
							font-weight: 500;
							font-size: 28rpx;
							color: #FFFFFF;
							border-radius: 28rpx;
						}
					}

					.header-view-zTop {
						display: flex;
						margin-right: 16rpx;

						>image {
							width: 54rpx;
							height: 54rpx;
							border-radius: 50%;
							overflow: hidden;
						}
					}

					.header-view-choose {
						display: flex;
						background-color: #ffffff;
						border-radius: 50%;
						overflow: hidden;

						>image {
							width: 54rpx;
							height: 54rpx;
							transition: width 0.5s;
							transition: height 0.5s;
						}
					}
				}
			}
		}

		.class-content {
			display: flex;

			.class-content-left {
				transition: width 0.5s;
                min-width: 184rpx;
				.isScrollView {
					// position: fixed;
					z-index: 999;
					transition: width 0.5s;
   min-width: 184rpx;
					.isScrollView_nav {
						.isScrollView_nav_view {
							display: flex;
							flex-direction: column;
							padding-bottom: 16rpx;

							.isScrollView_nav_item {
								display: flex;
								align-items: center;
								justify-content: flex-start;
								padding: 26rpx 40rpx;
								position: relative;
                                color: #020202;
								>view {
									font-size: 28rpx;
									// color: #020202;
									max-width: 128rpx;
									display: -webkit-box;
									-webkit-line-clamp: 1;
									/* 显示的行数 */
									-webkit-box-orient: vertical;
									overflow: hidden;
								}
							}

							.active {
								background-color: #FFFFFF;
								border-radius: 0 16rpx 16rpx 0;
								padding-right: 24rpx;
                                color: #FA5151;
							}

							.active>view {
								font-weight: 500;
								font-size: 30rpx;
								// color: #FA5151;
								flex: 1;
							}

							.set>view {
								color: #FA5151;
							}

							.isScrollView_nav_item.active:after {
								content: '';
								position: absolute;
								left: 0;
								width: 8rpx;
								height: 44rpx;
								background: rgba(250, 81, 81, 1.0);
								border-radius: 0 24rpx 24rpx 0;
							}
						}
					}
				}
			}

			.class-content-right {
				flex: 1;
				position: relative;

				.right-all {
					padding: 0 10rpx 0 24rpx;
					box-sizing: border-box;

					//position: absolute;
					//transition: left 0.5s;
					>.right-all-nav {
						// min-height: 100vh;
						// background-color: #ffffff;
						display: flex;
						flex-direction: column;

						.right-all-item {
							flex: 1;
							padding: 24rpx;
							box-sizing: border-box; 
                                background-color: #fff;
                                border-radius: 24rpx;
                                margin-bottom: 24rpx;
                       


							.all-item-title {
								font-weight: bold;
								font-size: 32rpx;
								color: rgba(0, 0, 0, 0.85);
								display: flex;
								margin-left: 16rpx;
								margin-bottom: 24rpx;
							}

							.all-item-content-no {
								display: flex;
								flex-direction: column;
								align-items: center;
								justify-content: center;

								>image {
									width: 480rpx;
									height: 248rpx;
								}

								>view {
									font-size: 28rpx;
									color: #999999;
								}
							}

							.all-item-content {
								display: flex;
								flex-wrap: wrap;

								.content-item {
									display: flex;
									flex-direction: column;
									align-items: center;
									justify-content: center;
									margin-right: 8rpx;
									margin-bottom: 24rpx;

									>image {
										width: 156rpx;
										height: 156rpx;
										margin-bottom: 16rpx;
									}

									>view {
										text-align: center;
										font-size: 28rpx;
										color: rgba(0, 0, 0, 0.7);
										width: 156rpx;
										display: -webkit-box;
										-webkit-line-clamp: 1;
										/* 显示的行数 */
										-webkit-box-orient: vertical;
										overflow: hidden;
									}
								}

								.content-item:nth-child(3n) {
									margin-right: 0;
								}
							}
						}
					}
				}

				.right-item {
					padding: 0 0 0 24rpx;
					box-sizing: border-box;
					transition: padding 0.5s;

					//position: absolute;
					//transition: left 0.5s;
					>view {
						display: flex;
						flex-direction: column;
						transition: width 0.5s;

						.right-item-top {
							.isScrollView {
								position: fixed;
								z-index: 999;
                                 

								.isScrollView_nav {
									transition: width 0.5s;

									.isScrollView_nav_view {
										display: flex;
										padding-bottom: 16rpx;

										.isScrollView_nav_item {
											display: flex;
											flex-direction: column;
											align-items: center;
											justify-content: center;
											margin-right: 16rpx;
											background-color: #ffffff;
											border-radius: 16rpx;

											>image {
												width: 140rpx;
												height: 140rpx;
												border-radius: 16rpx;
											}

											>view {
												padding: 8rpx;
												box-sizing: border-box;
												text-align: center;
												font-size: 28rpx;
												color: #333333;
												line-height: 40rpx;
												width: 140rpx;
												display: -webkit-box;
												-webkit-line-clamp: 1;
												/* 显示的行数 */
												-webkit-box-orient: vertical;
												overflow: hidden;
											}
										}

										.isScrollView_nav_item.active {
											background: rgba(250, 81, 81, 0.1);
											border-radius: 16rpx  ;
											border: 2rpx solid #FA5151;

											>image {
												width: 136rpx;
												height: 136rpx;
											}
										}

										.isScrollView_nav_item.active>view {
											color: #FA5151;
										}
									}
								}
							}
						}

						.right-item-shaix {
							height: 72rpx;
							display: flex;

							>view {
								transition: width 0.5s;
								flex: 1;
								display: flex;
								align-items: center;
								justify-content: space-between;
								height: 72rpx;
								padding: 16rpx 0;
								padding-right: 24rpx;
								box-sizing: border-box;

								// position: fixed;
								>view {
									display: flex;
									align-items: center;
									font-size: 28rpx;
									color: #999999;
								}

								.active {
									color: #FA5151;
								}
							}

							.volume-px {
								display: flex;
								flex-direction: column;
								align-items: center;
								justify-content: center;

								>image {
									width: 16rpx;
									height: 8rpx;
									margin-left: 8rpx;
								}

								>image:first-child {
									margin-bottom: 10rpx;
								}
							}

							.price-px {
								display: flex;
								flex-direction: column;
								align-items: center;
								justify-content: center;

								>image {
									width: 16rpx;
									height: 8rpx;
									margin-left: 8rpx;
								}

								>image:first-child {
									margin-bottom: 10rpx;
								}
							}

							.filter-sx {
								display: flex;
								flex-direction: column;
								align-items: center;
								justify-content: center;

								>image {
									width: 20rpx;
									height: 20rpx;
									margin-left: 8rpx;
								}
							}
						}

						.item-type1 {
							padding-right: 24rpx;
							box-sizing: border-box;

							.item-type1-scroll-no {
								display: flex;
								align-items: center;
								justify-content: center;
								background: #FFFFFF;
								border-radius: 24rpx 24rpx 0 0;
								padding: 24rpx 24rpx 0 24rpx;
								box-sizing: border-box;

								.goods-no {
									display: flex;
									flex-direction: column;
									align-items: center;
									justify-content: center;

									>image {
										width: 480rpx;
										height: 248rpx;
									}

									>view {
										font-size: 28rpx;
										color: #999999;
									}
								}
							}

							.item-type1-scroll {
								display: flex;
								flex-direction: column;
								background: #FFFFFF;
								border-radius: 24rpx 24rpx 0 0;
								padding: 24rpx;
								box-sizing: border-box;

								.item-type1-goods {
									background: #F6F6F6;
									border-radius: 12rpx;
									padding: 8rpx;
									box-sizing: border-box;
									display: flex;
									align-items: center;
									justify-content: space-between;
									margin-bottom: 16rpx;

									.goods-img {
										display: flex;
										margin-right: 8rpx;
										position: relative;

										>image {
											width: 142rpx;
											height: 142rpx;
											border-radius: 16rpx;
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

									.goods-text {
										flex: 1;
										display: flex;
										flex-direction: column;

										>view:nth-child(1) {
											font-size: 24rpx;
											color: #333333;
											white-space: nowrap;
											/* 不换行 */
											overflow: hidden;
											/* 隐藏超出的内容 */
											text-overflow: ellipsis;
											/* 用省略号表示被隐藏的部分 */
											max-width: 290rpx;
											/* 设置最大宽度以限制文本的显示长度 */
										}

										>view:nth-child(2) {
											display: flex;
											margin-top: 8rpx;

											>span {
												font-size: 16rpx;
												color: #FFFFFF;
												padding: 2rpx 8rpx;
												box-sizing: border-box;
												margin-right: 8rpx;
												border-radius: 4rpx;
												background-color: red;
											}
										}

										.goods-text-price {
											display: flex;
											align-items: center;
											justify-content: space-between;
											margin-top: 8rpx;

											>view:nth-child(1) {
												display: flex;
												flex-direction: column;

												.price {
													display: flex;

													>view:nth-child(1) {
														font-size: 20rpx;
														color: #FA5151;
													}

													>view:nth-child(2) {
														font-family: DIN;
														font-size: 28rpx;
														color: #FA5151;
														margin-left: 2rpx;
													}

													>view:nth-child(3) {
														font-size: 20rpx;
														color: #999999;
														line-height: 32rpx;
														text-decoration-line: line-through;
														margin-left: 8rpx;
													}
												}

												.shopName {
													font-size: 20rpx;
													color: #999999;
													margin-top: 8rpx;
												}
											}

											.jiagou {
												display: flex;
												padding-right: 16rpx;
												box-sizing: border-box;

												>image {
													width: 48rpx;
													height: 48rpx;
													border-radius: 50%;
													background: rgba(250, 81, 81, 0.1);
												}
											}
										}
									}
								}
                                .item-type1-goods:last-child{
                                    margin-bottom: 24rpx;
                                }
							}
						}

						.item-type2 {
							.item-type2-scroll {
								.item-type2-goods {
									/deep/.isWaterfall {
										padding-left: 8rpx !important;
									}
								}
							}
						}
					}
				}

			}

		}

	}
</style>
