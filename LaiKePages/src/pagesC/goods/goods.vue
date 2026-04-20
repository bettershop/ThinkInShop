<template>
    <div class="conten" @tap="removeMask">
        <div ref="box" class="conten">
            <heads :title="title" :bgColor="bgColor" :titleColor="'#333333'" :ishead_w="2"></heads>
            <!-- 有商品的时候显示 -->
            <div style="height: 88rpx;">
                <div class='allgoods_s home_navigation'>
                    <div class='home_input'>
                        <img class='searchImg' :src="serchimg" alt="">
                        <input type="text" v-model="searchtxt"
                            :placeholder="language.search2.searchRes.search_placeholder" id='input'
                            :placeholder-style="placeStyle" name="sourch" />
                        <view @tap.stop='_searchB' class="searchBtn">{{language.yushou_settlement.ss}}</view>
                    </div>
                    <image class="verticalImg" :src="setMethods?horizontal:vertical" mode="widthFix" @tap="changeSet">
                    </image>
                </div>
            </div>
            

            <div style="height: 90rpx;">
                <div :class="filterMaskFlag?'topFilteraa':'topFilter'">
                    <div :class='{"active":searchType == ""}' @tap="clickType('')">
                        {{language.search2.searchRes.TopButton[0]}}</div>
                    <div :class='{"active":searchType == "volume"}' @tap="clickType('volume')">
                        {{language.search2.searchRes.TopButton[1]}}
                        <div class="sortingImgBox">
                            <img :src="searchType=='volume'&&sorting=='asc'?sortingToph:sortingTop">
                            <img :src="searchType=='volume'&&sorting=='desc'?sortingBottomh:sortingBottom">
                        </div>
                    </div>
                    <div :class='{"active":searchType == "price"}' @tap="clickType('price')">
                        {{language.search2.searchRes.TopButton[2]}}
                        <div class="sortingImgBox">
                            <img :src="searchType=='price'&&sorting=='asc'?sortingToph:sortingTop">
                            <img :src="searchType=='price'&&sorting=='desc'?sortingBottomh:sortingBottom">
                        </div>
                    </div>
                    <div :class='{"active":searchType == "comment_num"}' @tap="clickType('comment_num')">
                        {{language.search2.searchRes.TopButton[5]}}</div>
                    <div @tap.stop="shaixuanClick"
                        :class='{"active":filterMaskFlag || brand_id || min_price1 || max_price1}'>
                        {{language.search2.searchRes.TopButton[3]}}
                        <img :src="filterMaskFlag || brand_id || min_price1 || max_price1?filter_h:filter">
                    </div>

                    <div class="filterMaskDiv" v-if="filterMaskFlag" @touchmove.stop.prevent @tap.stop>
                        <div>
                            <div class="filterTitle" @tap="pinpaiClick"
                                :style="filterMaskFlag?'border-top: 1rpx solid #eee;':''">
                                {{language.search2.searchRes.TopButton[4]}}
                                <img :src="pinpaiFlag?jiantouTop:jiantouBottom" alt="">
                            </div>
                            <scroll-view v-if="pinpaiFlag" class="filterScroll" :scroll-y="true">
                                <div class="filterScrollItem">
                                    <div :class="{active: brand_id == item.brand_id}" v-for="(item,index) in brand_list"
                                        :key="index" @tap="changeBrand(item)">{{item.brand_name}}</div>
                                </div>
                            </scroll-view>
                        </div>

                        <div>
                            <div class="filterTitle">{{language.search2.searchRes.section}}</div>
                            <div class="pricerBox">
                                <input v-model="min_price" type="digit" :placeholder="language.search2.searchRes.min"
                                    placeholder-style="display: flex; justify-content: center;color: #B8B8B8;">
                                <span>-</span>
                                <input v-model="max_price" type="digit" :placeholder="language.search2.searchRes.max"
                                    placeholder-style="display: flex; justify-content: center;color: #B8B8B8;" />
                            </div>
                        </div>

                        <div class="filterBottom">
                            <div @tap="reset">{{language.search2.searchRes.Reset}}</div>
                            <div @tap="filterOk">{{language.search2.searchRes.determine}}</div>
                        </div>
                    </div>
                </div>
                <div class="filterMask" v-if="filterMaskFlag" @touchmove.stop.prevent></div>
            </div>

            <div class="skeleton" v-if="!load">
                <ul class="allgoods relative">
                    <li class="allgoods_li" v-for="(item, index) in 4" :key="index">
                        <div class="proImgBox">
                            <img class="skeleton-rect" src="/static/img/live.png" />
                        </div>
                        <p class="allgoods_name skeleton-rect" style="margin-bottom: 4px;">
                            {{language.goods.goods.Trade_name}}</p>
                        <p class="allgoods_subtitle skeleton-rect" style="width: 120px;">
                            {{language.goods.goods.subtitle}}</p>
                        <div class="skeleton-rect">
                            <span class="price"></span>
                            <span class="sales">{{language.goods.goods.sales_volume}}：10</span>
                            <img class="img" src="shopImg" />
                        </div>
                    </li>
                </ul>
            </div>
            <div v-else>
                <template v-if="pro_list.length > 0">
                    <!--搜索商品后商品展示模块-->
                    <ul class='home-hot margin-top' id="margin-top">
                        <li v-if='!success'>
                            <div class='empty_play'>
                                <img :src="no_search" />
                                <p>{{language.search2.searchRes.Tips[0]}}“{{searchtxt}}”{{language.search2.searchRes.Tips[1]}}<br>{{language.search2.searchRes.Tips[2]}}~
                                </p>
                            </div>
                        </li>
                        <li v-else>
                            <div class='allgoods_prev'></div>
                            <!-- 竖向排版 -->
                            <div class="goods_ul goods_ul-waterfall" v-if="setMethods">
                                <waterfall ref="waterfall" :goodsLike="false" :addShopCar="true" :mchLogo="false"
                                    :goodsPriceText="false" :isDataKO="isDataKO"
                                    :isHeight="pro_list.length > 0 ? minHeight : '0'" @_seeGoods="_seeGoods"
                                    @_addShopCar="shopping_j" :goodList="pro_list">
                                </waterfall>
                            </div>
                            <!-- 横向排版 -->
                            <ul class='allgoods_vertical' v-if="!setMethods" style="margin-top: 24rpx;">
                                <li class="allgoods_vertical_li" v-for='(items,index) in pro_list' :key='index'
                                    @tap="_seeGoods(items)">
                                    <view class="good_box">
                                        <view class="goodsImg">
                                            <image :src="items.imgurl"></image>
                                            <view v-if="items.status == 3" class="dowmPro">
                                                {{language.store.shelf}}
                                            </view>
                                            <view v-else-if="items.contNum == 0" class="dowmPro">
                                                {{language.shoppingCart.soldOut}}
                                            </view>
                                            <view v-else-if="items.num == 0 || items.num == '0'" class="dowmPro">
                                                {{language.goods.goods.Sold_out}}
                                            </view>
                                        </view>
                                        <view class="goodsInfo verticalStyle">
                                            <view class="storeName">{{items.mch_name}}</view>
                                            <view class="goodsName">{{items.product_title}}</view>
                                            <view class="goods_content_item_icon new_goods_goods_icon noPadding">
                                                <view :style="{backgroundColor: item_1.color}"
                                                    v-for="(item_1, index_1) in items.s_type_list" :key="index_1">
                                                    {{item_1.name}}</view>
                                            </view>
                                            <view class="goodsPrice">
                                                <view class="priceInfo">
                                                    <view class="x_price">
                                                        <span class="x_price_icon">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</span>
                                                        <span>{{LaiKeTuiCommon.formatPrice(items.price)}}</span>
                                                    </view>
                                                    <view class="y_price">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(items.yprice)}}</view>
                                                </view>
                                                <!-- 加入购物车 (实物商品) -->
                                                <template v-if="items.commodity_type == 0">
                                                    <view class="joinCart" @tap.stop="shopping_j(items, items.id)"
                                                        v-if="items.is_open == 1">
                                                        <image :src='shoppingImg'> </image>
                                                    </view>
                                                </template>
                                                <!-- 加入购物车 (虚拟商品) isAddCar=1 虚拟商品逻辑-无需核销/无需预约 才显示加入购物车 -->
                                                <template v-else>
                                                    <view class="joinCart" @tap.stop="shopping_j(items, items.id)"
                                                        v-if="items.is_open == 1 && items.isAddCar == 1">
                                                        <image :src='shoppingImg'> </image>
                                                    </view>
                                                </template>
                                            </view>
                                        </view>
                                    </view>
                                </li>
                            </ul>

                            <uni-load-more :loadingType="loadingType" v-if="searchgoods.length>9"></uni-load-more>
                        </li>
                    </ul>


                    <uni-load-more v-if="pro_list.length > 4" :loadingType="loadingType"
                        style="background-color: #f8f8f8;"></uni-load-more>
                </template>

                <!-- 无商品的时候显示 -->
                <div v-else class="relative">
                    <div class="search_no">
                        <div class="font_14 no_goods"><img class="img" :src="no_search" /></div>
                        <span class="span" style="font-size: 28rpx;">{{language.goods.goods.not}}！</span>
                    </div>
                </div>
            </div>
        </div>
        <!--  遮罩：点击购物小图标弹出  -->
        <skus ref="attrModal" @confirm="_confirm"></skus>

        <skeleton :animation="true" :loading="!load" bgColor="#FFF"></skeleton>
    </div>
</template>

<script>
import skeleton from "@/components/skeleton";
    import {
        mapMutations
    } from 'vuex';
    import waterfall from "@/components/aComponents/waterfall.vue";
    import skus from '../../components/skus.vue';
    import { buildUrlFromBackend } from "@/common/pluginsutil.js";

    export default {
        data() {
            return {
                is_grade: false,
                fastTap: true,
                shopImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/allgoods_shopImg.png',
                closed: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/guanbi2x.png',
                noImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/noFind.png',
                title: '', //页面标题
                cid: '', //分类id
                loadingType: 0,
                page: 1, //分页码
                pro_list: [],
                jian_hei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jian2x.png',
                jian_hui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jianhui2x.png',
                jia_hei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jia+2x.png',
                jia_hui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/add+2x.png',
                shoppingImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/redgwc.png',
                shoppingImgGray: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/shopping_cart_gray.png',
                sc_icon: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/delete2x.png',
                no_search: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/no_search.png',
                attribute_id: '',
                attrList: '',
                skuBeanList: '',
                mask_display: false,
                ys_price: '', //规格初始价格
                num: '', //规格数量
                price: '', //规格价格
                imgurl: '', //规格图片
                scan_d: '', //收藏id
                pro: '', //商品信息
                load_next: true, //是否能继续下滑加载
                haveSkuBean: '', //选择规则属性
                numb: 1, //规格选择的数量
                load: false,
                proid: '',
                msg: '',
                shop_id: '',
                // 瀑布流相关参数
                listLeft: [],
                listRight: [],
                cardLeftHeight: 0,
                cardRightHeight: 0,
                handleNum: 0,
                class_pro: [],
                highKeys: {},
                skuName: 'SkuID',
                skuName1: 'Price',
                skuName2: 'Pic',
                skuName3: 'Stock',
                spliter: ',',
                list: {},
                result: {},
                ErrorImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/Default_picture.png',
                vertical: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    'images/icon1/home_searchResult/searchResult_vertical.png',
                horizontal: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    'images/icon1/home_searchResult/searchResult_horizontal.png',
                filter: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    'images/icon1/home_searchResult/searchResult_filter.png',
                sortingBottom: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    'images/icon1/home_searchResult/searchResult_bottom.png',
                sortingBottomh: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    'images/icon1/home_searchResult/searchResult_bottom_select.png',
                sortingTop: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    'images/icon1/home_searchResult/searchResult_top.png',
                sortingToph: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    'images/icon1/home_searchResult/searchResult_top_select.png',
                filter_h: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    'images/icon1/home_searchResult/searchResult_filter_select.png',
                jiantouBottom: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    'images/icon1/home_searchResult/searchResult_bottom.png',
                jiantouTop: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    'images/icon1/home_searchResult/searchResult_top.png',
                serchimg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/searchNew.png',
                sc_Btn: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    'images/icon1/home_searchResult/searchResult_Btn.png',
                placeStyle: 'color:#999999',
                setMethods: true,
                searchtxt: '',
                success: true, //搜索商品是否有数据返回
                searchgoods: '', //接收搜索后商品返回的数据
                bgColor: [{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                searchType: '',
                filterMaskFlag: false,
                pinpaiFlag: false,
                sorting: 'asc', //  desc：降序，  asc：升序
                brand_list: [],
                brand_id: '',
                min_price: '', //最低价
                max_price: '',
                brand_id1: '',
                min_price1: '', //最低价
                max_price1: '',
                isDataKO: false,
                minHeight: "", //瀑布流最小高度，用来固定切换导航时显示位置
            };
        },
        components: {
            skeleton,
            skus,
            waterfall
        },
        onLoad(option) {
            this.cid = option.cid;
            this.title = option.name;

            this.shop_id = option.shop_id;

            this._axios()
        },

        // 下拉加载
        onReachBottom: function() {
            if (this.loadingType == 2) {
                return
            }
            this.loadingType = 1;
            this.page++;
            this._axios(true)
        },
        methods: {
            // 破图
            handleImgError(index) {
                this.listLeft[index].imgurl = this.ErrorImg
            },
            handleImgErrorRight(index) {
                this.listRight[index].imgurl = this.ErrorImg
            },
            // 瀑布流相关start
            onImageLoad(e, id) {
                let divWidth = 336; //实际显示的单栏宽度，345upx
                let oImgW = e.detail.width; //图片原始宽度
                let oImgH = e.detail.height; //图片原始高度
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
                this.handleNum++;
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
                this.handleNum = 0;
            },
            // 瀑布流相关end
            changeSet() {
                this.setMethods = !this.setMethods

            },
            cleardata() {
                this.searchtxt = ''
            },
            _searchB() {
                this.page = 1
                uni.pageScrollTo({
                    duration: 0,
                    scrollTop: 0
                })
                this._axios()
            },
            changeBrand(item) {
                this.brand_id = item.brand_id
            },
            removeMask() {
                this.filterMaskFlag = false

                this.min_price = this.min_price1
                this.max_price = this.max_price1
                this.brand_id = this.brand_id1
            },
            reset() {
                this.min_price = ''
                this.max_price = ''
                this.brand_id = ''
            },
            filterOk() {
                this.min_price1 = this.min_price
                this.max_price1 = this.max_price
                this.brand_id1 = this.brand_id

                this.page = 1
                this._axios()
                this.filterMaskFlag = false
            },
            shaixuanClick() {
                this.filterMaskFlag = !this.filterMaskFlag

                if (!this.filterMaskFlag) {
                    this.min_price = this.min_price1
                    this.max_price = this.max_price1
                    this.brand_id = this.brand_id1
                }
            },
            pinpaiClick() {
                this.pinpaiFlag = !this.pinpaiFlag
            },
            clickType(type) {
                if ((type == 'volume' || type == 'price') && this.searchType == type) {
                    this.sorting = this.sorting == 'asc' ? 'desc' : 'asc'
                } else {
                    this.sorting = 'asc'
                }
                this.searchType = type

                this.page = 1
                this._axios();
            },
            _axios(tag) {
                //(非下拉)每次点击 都得初始化上一次商品瀑布流的数据
                this.isDataKO = !tag?true:false;
                //500毫秒，取消初始化
                setTimeout(() => {
                    this.isDataKO = false;
                }, 5000);
                let query_criteria = {
                    brand_id: this.brand_id1,
                    min_price: this.min_price1,
                    max_price: this.max_price1
                }

                let data = {
                    cid: this.cid,
                    api: 'app.search.listdetail',
                    page: this.page,
                    pro: '',
                    keyword: this.searchtxt,
                    sort_criteria: this.searchType,
                    query_criteria: JSON.stringify(query_criteria)
                };

                if (this.searchType == 'volume' || this.searchType == 'price') {
                    data.sort = this.sorting
                }

                if (this.shop_id) {
                    data.shop_id = this.shop_id;
                }
                this.$req.post({
                    data
                }).then(res => {
                    if (res.code == 200) {
                        if (this.page == 1) {
                            this.pro_list = []
                        }

                        let {
                            pro,
                            pname,
                            grade
                        } = res.data;
                        this.is_grade = grade == 0 ? false : true;

                        if (this.brand_list.length == 0) {
                            this.brand_list = res.data.brand_class_list
                        }

                        if (pro && pro.length > 0) {
                            pro.filter(item => {
                                item.vip_yprice = Number(item.vip_yprice).toFixed(2)
                                item.vip_price = Number(item.vip_price).toFixed(2)
                            })
                            //瀑布流相关start
                            // this.class_pro = pro.concat()
                            this.class_pro = this.class_pro.concat(pro);
                            if (!tag) {
                                this.empty();
                            }
                            setTimeout(() => {
                                this.waterfall();
                            }, 100)
                            // 瀑布流相关end
                        }
                        this.pro_list.push(...pro)
                        this.pro_list.forEach((item, index) => {
                            //可能有相同路径的图片，会无法加载@load事件；拼接随机字符串，可以解决这个问题
                            item.imgurl = item.imgurl + '?a=' + Math.floor(Math.random() * 1000000);
                            item.contNum = item.stockNum
                            item.volume = item.payPeople;
                        });
                        if (pname !== ''&& pname) {
                            this.title = pname;
                        } else {
                            if (this.title == '介绍') {
                                this.title = '好物优选'
                            } else if (this.title == '日用品') {
                                this.title = '新品上市'
                            }
                        }
                        if (res.data.pro.length == 10) {
                            this.loadingType = 0;
                        } else {
                            this.loadingType = 2;
                        }
                        if (pname) {
                            //#ifdef MP
                            uni.setNavigationBarTitle({
                                title: pname
                            });
                            // #endif
                        }
                        this.load = true
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        });
                    }
                })

            },
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
                let item = null;
                let id = payload;
                if (payload && typeof payload === 'object') {
                    item = payload;
                    id = payload.id;
                } else {
                    item = Array.isArray(this.pro_list)
                        ? this.pro_list.find((it) => String(it.id) === String(payload))
                        : null;
                    id = payload;
                }

                const url = this._resolveMarketingUrl(item && item.marketingParams);
                if (url) {
                    if (id) this.pro_id(id);
                    uni.navigateTo({ url });
                    return;
                }
                this._goods(id);
            },

            /*   跳转商品详情页    */
            _goods(id) {
                this.pro_id(id);
                uni.navigateTo({
                    url: '/pagesC/goods/goodsDetailed?toback=true&pro_id=' + id
                });
            },
            // 点击购物图标
            shopping_j(item, id) {
                if (!this.fastTap) {
                    return;
                }
                if (item.status == 3) {
                    uni.showToast({
                        title: '商品已下架',
                        duration: 1500,
                        icon: 'none'
                    });
                    return
                }
                if (item.num == 0) {
                    uni.showToast({
                        title: '商品已售罄',
                        duration: 1500,
                        icon: 'none'
                    });
                    return
                }
                this.fastTap = false;
                this.proid = id;
                var data = {
                    api: 'app.product.index',
                    pro_id: id
                };

                this.$req.post({
                    data
                }).then(res => {
                    this.fastTap = true;
                    if (res.code == 200) {
                        let {
                            data: {
                                collection_id,
                                pro,
                                comments,
                                attrList,
                                skuBeanList,
                                qj_price,
                                type,
                                attribute_list,
                                commodity_type,
                                write_off_settings
                            }
                        } = res;

                        this.ys_price = qj_price
                        this.attrList = attrList

                        this.$refs.attrModal.imgurl = pro.img_arr[0]
                        // this.$refs.attrModal.num = pro[].id
                        this.$refs.attrModal.price = qj_price
                        this.$refs.attrModal.skuBeanList = attribute_list
                        this.$refs.attrModal.initData()
                        this.$refs.attrModal._mask_display()
                        pro.commodity_type = commodity_type
                        pro.write_off_settings = write_off_settings
                        this.$refs.attrModal.pro2 = pro
                        
                        this.collection = type
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        });
                    }
                }).catch(error => {
                    this.fastTap = true;
                })
            },
            
            //打开遮罩层
            _mask_display() {
                this.mask_display = true;
                if (!this.haveSkuBean) {
                    for (var i = 0; i < this.attrList.length; i++) {
                        var attr = this.attrList[i].attr;
                        if (attr.length == 1) {
                            this.showState(0, i);
                        }
                    }
                }
            },
            //关闭遮罩层
            _mask_false() {
                // 关闭弹窗让购买数量重新设为默认
                this.numb = 1;
                this.mask_display = false;
                if (!this.haveSkuBean) {
                    for (var i = 0; i < this.attrList.length; i++) {
                        var attr = this.attrList[i].attr;
                        if (attr.length == 1) {
                            this.showState(0, i);
                        }
                    }
                }
            },
            //点击遮罩层的关闭按钮
            _mask_f() {
                this.haveSkuBean = '';
                this._mask_false();
                this.mask_display = false;
            },
            //确认
            _confirm(sku) {
                Object.assign(this.$data, sku)

                if (Boolean(this.haveSkuBean)) {
                    if (this.num == 0) {
                        uni.showToast({
                            title: this.language.goods.goods.Insufficient_stock,
                            duration: 1000,
                            icon: 'none'
                        });
                    } else if (this.num != 0) {
                        this._shopping();
                    }
                } else {
                    if (this.num == 0) {
                        uni.showToast({
                            title: this.language.goods.goods.Insufficient_stock,
                            duration: 1000,
                            icon: 'none'
                        });
                    } else {
                        uni.showToast({
                            title: this.language.goods.goods.Tips,
                            duration: 1000,
                            icon: 'none'
                        });
                    }
                }
            },
            //加入购物车
            _shopping() {
                if (this.haveSkuBean) {
                    var data = {
                        api: 'app.product.add_cart',
                        pro_id: this.proid,
                        attribute_id: this.haveSkuBean.cid,
                        num: this.numb,
                        type: 'addcart'
                    };
                    if (this.access_id) {
                        data.access_id = this.access_id;
                    } else {
                        data.access_id = '';
                    }
                    if (this.$store.state.nCart) {
                        data.cart_id = this.$store.state.nCart;
                    } else {
                        data.cart_id = '';
                    }

                    this.$req.post({
                        data
                    }).then(res => {
                        let {
                            code,
                            data,
                            message
                        } = res;
                        if (code == 200) {
                            this.$store.state.access_id = data.access_id

                            uni.showToast({
                                title: this.language.goods.goods.add_success,
                                duration: 1000,
                                icon: 'none'
                            });

                            this.haveSkuBean = ''
                            this.$refs.attrModal._mask_f()
                        } else {
                            uni.showToast({
                                title: message,
                                duration: 1500,
                                icon: 'none'
                            });
                        }
                        if (data && data.cart_id) {
                            if (!this.in_array(data.cart_id, this.$store.state.nCart)) {
                                this.$store.state.nCart.push(data.cart_id);
                            }
                        }
                    })

                } else {
                    this._mask_display();
                }
            },
            in_array(stringToSearch, arrayToSearch) {
                for (let s = 0; s < arrayToSearch.length; s++) {
                    let thisEntry = arrayToSearch[s].toString()
                    if (thisEntry == stringToSearch) {
                        return true
                    }
                }
                return false
            },
            ...mapMutations({
                pro_id: 'SET_PRO_ID',
                cindex: 'SET_CINDEX'
            })
        },
    };
</script>
<style>
    page {
        background-color: #f4f5f6;
    }
</style>
<style lang="less">
    @import url("@/laike.less");
    @import url('../../static/css/goods/goods.less');
</style>
