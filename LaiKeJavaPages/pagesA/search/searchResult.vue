<template>
    <div class="container" @tap="removeMask">
        <!--头部模块-->
        <heads :title='language.search2.searchRes.title' :border="true"  :bgColor='bgColor' ishead_w="2"  titleColor="#333333" ></heads>
        <div style="height: 88rpx;">
            <div class='allgoods_s home_navigation'>
                
                <div class='home_input' :style="topTabBar ==2?'width:720rpx;':''">
                    <img class='searchImg' :src="serchimg" alt="">
                    <input type="text" v-model="searchtxt" :placeholder="language.search2.searchRes.search_placeholder" id='input' :placeholder-style="placeStyle"
                           name="sourch"/>
                    
                    <!-- 搜索按钮 -->
                    <view v-if="topTabBar!=2" @tap.stop='_searchB' class="searchBtn">{{language.order.myorder.search}}</view>
                </div>
                <div class="search_btn" v-if="topTabBar==2" @tap='_searchB'>{{language.search2.searchRes.search_button}}</div>
                <image class="verticalImg" v-if="topTabBar!=2" :src="setMethods?horizontal:vertical" mode="widthFix" @tap="changeSet"></image>
            </div>
        </div>

        <!-- 导航切换栏 -->
        <div style="height: 102rpx;" >
            <div :class='topTabBar!=2?"topTabBara":"topTabBar"'>
                <div :class='{"active":topTabBar == 0}' @tap="_search(0)">{{language.search2.searchRes.TabBar[0]}}</div>
                
                <div :class='{"active":topTabBar == 2}' @tap="_search(2)">{{language.search2.searchRes.TabBar[2]}}</div>
            </div>
        </div>
        
        
        <div style="height: 88rpx;" v-if='topTabBar != 2'>
            <div class='topFilter' :class="{'fiterMask':filterMaskFlag}">
                <div :class='{"active":!filterMaskFlag && searchType == ""}' @tap="clickType('')">{{language.search2.searchRes.TopButton[0]}}</div>
                <div :class='{"active":!filterMaskFlag && searchType == "volume"}' @tap="clickType('volume')">
                    {{language.search2.searchRes.TopButton[1]}}
                    <div class="sortingImgBox">
                        <img :src="searchType=='volume'&&sorting=='asc'?sortingToph:sortingTop">
                        <img :src="searchType=='volume'&&sorting=='desc'?sortingBottomh:sortingBottom">
                    </div>
                </div>
                <div :class='{"active":!filterMaskFlag && searchType == "price"}' @tap="clickType('price')">
                    {{language.search2.searchRes.TopButton[2]}}
                    <div class="sortingImgBox">
                        <img :src="searchType=='price'&&sorting=='asc'?sortingToph:sortingTop">
                        <img :src="searchType=='price'&&sorting=='desc'?sortingBottomh:sortingBottom">
                    </div>
                </div>
                <div :class='{"active":!filterMaskFlag && searchType == "comment_num"}' @tap="clickType('comment_num')">{{language.search2.searchRes.TopButton[5]}}</div>
                <div @tap.stop="shaixuanClick" :class='{"active":filterMaskFlag || brand_id || min_price1 || max_price1}'>
                    {{language.search2.searchRes.TopButton[3]}}
                    <img :src="filterMaskFlag || brand_id || min_price1 || max_price1?filter_h:filter">
                </div>
                
                <div class="filterMaskDiv" v-if="filterMaskFlag" @touchmove.stop.prevent @tap.stop>
                    <div v-if="brand_list.length>0">
                        <div class="filterTitle" @tap="pinpaiClick" style="border-top:1rpx solid rgba(0,0,0,.1);">
                            {{language.search2.searchRes.TopButton[4]}}
                            <img :src="pinpaiFlag?jiantouTop:jiantouBottom" alt="">
                        </div>
                        <scroll-view v-if="pinpaiFlag" class="filterScroll" :scroll-y="true">
                            <div class="filterScrollItem">
                                <div :class="{active: brand_id == item.brand_id}" v-for="item,index of brand_list" :key="index" @tap="changeBrand(item)">{{item.brand_name}}</div>
                            </div>
                        </scroll-view>
                    </div>
                    
                    <div>
                        <div class="filterTitle">{{language.search2.searchRes.section}}</div>
                        <div class="pricerBox">
                            <input v-model="min_price" type="number" :placeholder="language.search2.searchRes.min" placeholder-style="display: flex; justify-content: center;color: #B8B8B8;">
                            <span>—</span>
                            <input v-model="max_price" type="number" :placeholder="language.search2.searchRes.max" placeholder-style="display: flex; justify-content: center;color: #B8B8B8;"/>
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

        <!--搜索商品后商品展示模块-->
        <ul class='home-hot margin-top' id="margin-top" v-if="topTabBar!=2">
            <li v-if='!success'>
                <div class='empty_play'>
                    <img :src="no_search"/>
                    <p>{{language.search2.searchRes.Tips[0]}}“{{searchtxt}}”{{language.search2.searchRes.Tips[1]}}<br>{{language.search2.searchRes.Tips[2]}}~</p>
                </div>
            </li>
            <li v-else>
                <div class='allgoods_prev'></div>
                <!-- 竖向排版 -->
                <div class="goods_ul goods_ul-waterfall" v-if="setMethods">
                
                    <div class="goods_ul_left">
                        <template v-if="listLeft.length>0">
                            <div class="goods_like goods_ul_left_li" v-for="(item, index) in listLeft" :key="index" @tap="_seeGoods(item)">
                                <!-- 商品图片 -->
                                <div class="goods_like_img relative">
                                    <image  lazy-load :src="item.cover_map || item.imgurl" data-name="0" mode="widthFix" width="100%" @load="onImageLoad($event, 'goods_ul_left_li' + index )"/>
                                    <div v-if="item.status == 3" class="dowmPro" >
                                        {{language.store.shelf}}
                                    </div>
                					<div v-else-if="item.num == 0" class="dowmPro" >
                                        {{language.shoppingCart.soldOut}}
                					</div>
                                </div>
                                <!-- 文字内容 -->
                                <view :id="'goods_ul_left_li' + index" class="new_goods">
                                    <!-- 店铺名称 -->
                                    <div class="goods_shopBox new_goods_shop_name">
                                        <!-- <img :src="item.logo" alt=""> -->
                                        {{item.mch_name}}
                                    </div>
                                    <!-- 商品名称 -->
                                    <p class="overtitle new_goods_goods_name">{{ item.product_title || item.name }}</p>
                                    <!-- 标签 -->
                                    <view class="goods_content_item_icon new_goods_goods_icon" v-if="item.s_type_list.length">
                                        <view :style="{'backgroundColor':item_1.color}" v-for="(item_1, index_1) in item.s_type_list" :key="index_1">{{item_1.name}}</view>
                                    </view>
                                    <!-- 价格 -->
                                    <div class="goods_mun">                                         
                                        <div class="allgoods_price" >
                                            <view class='red'>
                                                <text class="currency">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</text>
                                                <text class="vip_price">{{LaiKeTuiCommon.formatPrice(item.price?item.price:'0.00')}}</text>
                                                <text class="sales">{{ item.volume }}{{language.waterfall.type[3]}}</text>
                                            </view>                                              
                                        </div>
                                        <template v-if="item.is_appointment != 2">
                                            <div class="joinCart" @tap.stop="shopping_j(item.id,item)" v-if="item.is_open == 1">
                                                <img :src='shoppingImg' /> 
                                            </div>
                                        </template>
                                    </div>
                                </view>
                            </div>
                        </template>
                    </div>
                    
                    <div class="goods_ul_right">
                        <template v-if="listRight.length>0">
                            <div class="goods_like goods_ul_right_li" v-for="(item, index) in listRight" :key="index" @tap="_seeGoods(item)">
                                <!-- 商品图片 -->
                                <div class="goods_like_img relative">
                                    <image  lazy-load :src="item.cover_map || item.imgurl" data-name="1" mode="widthFix" width="100%" @load="onImageLoad($event, 'goods_ul_right_li' + index)"/>
                                    <template v-if="item.num == 0 && item.status == 3">
                                        <div v-if="item.status == 3" class="dowmPro" >
                                            {{language.store.shelf}}
                                        </div>
                                    </template>
                                    <template v-else>
                                        <div v-if="item.num == 0" class="dowmPro" >
                                            <!-- 已售罄 -->
                                            {{language.shoppingCart.soldOut}}
                                        </div>
                                        <div v-if="item.status == 3" class="dowmPro" >
                                            {{language.store.shelf}}
                                        </div>
                                    </template>
                                </div>
                                <!-- 文字内容 -->
                                <view :id="'goods_ul_right_li' + index" class="new_goods">
                                    <!-- 店铺名称 -->
                                    <div class="goods_shopBox new_goods_shop_name">
                                        {{item.mch_name}}
                                    </div>
                                    <!-- 商品名称 -->
                                    <p class="overtitle new_goods_goods_name">{{ item.product_title || item.name }}</p>
                                    <!-- 标签 -->
                                    <view class="goods_content_item_icon new_goods_goods_icon" v-if="item.s_type_list.length">
                                         <view :style="{'backgroundColor':item_1.color}" v-for="(item_1, index_1) in item.s_type_list" :key="index_1">{{item_1.name}}</view>
                                    </view>
                                    <!-- 价格 -->
                                    <div class="goods_mun">                                         
                                        <div class="allgoods_price" >
                                            <view class='red'>
                                                <text class="currency">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</text>
                                                <text class="vip_price">{{LaiKeTuiCommon.formatPrice(item.price?item.price:'0.00')}}</text>
                                                <text class="sales">{{ item.volume }}{{language.waterfall.type[3]}}</text>
                                            </view>                                           
                                        </div>
                                        <template v-if="item.is_appointment != 2">
                                            <div class="joinCart" @tap.stop="shopping_j(item.id,item)" v-if="item.is_open == 1">
                                                <img :src='shoppingImg' /> 
                                            </div>
                                        </template>
                                    </div>
                                </view>
                            </div>
                        </template>
                    </div>
                </div>
                <!-- 横向排版 -->
               <ul class='allgoods_vertical' v-show="!setMethods && searchgoods.length>0">
                    <li class="allgoods_vertical_li" v-for='(items,index) in searchgoods' :key='index' @tap="_seeGoods(items)">
                        <view class="good_box">
                            <view class="goodsImg">
                                <img :src="items.imgurl" @error="handleErrorImg(index)"/>
                                <div v-if="items.recycle == 1">{{language.collection.isDel}}</div>
                                <template v-if="items.num == 0 && items.status == 3">
                                    <div v-if="items.status == 3" class="dowmPro" >
                                        {{language.store.shelf}}
                                    </div>
                                </template>
                                <template v-else>
                                    <div v-if="items.num == 0" class="dowmPro" >
                                        <!-- 已售罄 -->
                                        {{language.shoppingCart.soldOut}}
                                    </div>
                                    <div v-if="items.status == 3" class="dowmPro" >
                                        {{language.store.shelf}}
                                    </div>
                                </template>
                            </view>
                            <view class="goodsInfo verticalStyle">
                                <view class="storeName">{{items.mch_name}}</view>
                                <view class="goodsName">{{items.product_title}}</view>
                                <view class="goods_content_item_icon new_goods_goods_icon noPadding" v-if="items.s_type_list.length">
                                    <view :style="{'backgroundColor':item_1.color}" v-for="(item_1, index_1) in items.s_type_list" :key="index_1">{{item_1.name}}</view>
                                </view>
                                 <view v-else class="goods_content_item_icon new_goods_goods_icon noPadding white_h"></view>
                                <view class="goodsPrice">
                                    <view class="priceInfo">
                                        <view class="x_price">
                                            <span class="x_price_icon">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</span>
                                            <span>{{LaiKeTuiCommon.formatPrice(items.price?items.price:'0.00')}}</span>
                                        </view>
                                        <view class="y_price">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(items.vip_yprice)}}</view>
                                    </view>
                                    <template v-if="items.is_appointment != 2">
                                        <view class="joinCart" @tap.stop="shopping_j(items.id,items)" v-if="items.is_open == 1" >
                                            <image :src='shoppingImg'> </image>
                                        </view>
                                    </template>
                                </view>
                            </view>
                        </view>
                    </li>
                </ul>

                <uni-load-more :loadingType="loadingType" v-if="searchgoods.length>4"></uni-load-more>
            </li>
        </ul>
        <div v-else>
            <ul class="shoping" v-if='success'>
                <li v-for="(item, index) in searchgoods" :key="index">
                    <view style="display: flex;width: 100%;margin-left: 24rpx;margin-top: 24rpx;">
                        <div class="box">
                            <img :src="item.logo?item.logo:defaultPicture" @error="handleShopImg(index)" />
                        </div>
                        <div class="shop-prop">
                            <view class="shop-content">
                                <div class="shop-title">{{item.name}}
                                <view class="shop-remark">{{item.shop_information}}</view>
                                </div>
                                <button class="baguette" @tap="_goStore(item.id)">进入店铺</button>
                            </view>
                            
                        </div>
                    </view>
                    <view class="shop-count" style="display: flex;justify-content: space-between;align-items: center;width: 100%;margin-top: 32rpx;margin-bottom: 24rpx;">
                        <view style="width: auto;display: flex;align-items: center;flex-direction: column;height: 84rpx;margin-left: 24rpx;">
                            <p class="sum">{{item.quantity_on_sale}}</p>
                            <p class="sum-name">{{language.search2.searchRes.sale}}</p>
                        </view>
                        <view style="width: auto;display: flex;align-items: center;flex-direction: column;height: 84rpx;">
                            <p class="sum">{{item.quantity_sold}}</p>
                            <p class="sum-name">{{language.search2.searchRes.Sold}}</p>
                        </view>
                        <view style="width: auto;display: flex;align-items: center;flex-direction: column;height: 84rpx;margin-right: 24rpx;">
                            <p class="sum">{{item.follow}}</p>
                            <p class="sum-name">{{language.search2.searchRes.num}}</p>
                        </view>
                    </view>
                    
                    
                </li>
            </ul>
            <uni-load-more :loadingType="loadingType"  v-if="searchgoods.length>9"></uni-load-more>
            <ul class='home-hot margin-top' id="margin-top">
                <li v-if='!success'>
                    <div class='empty_play'>
                        <img :src="no_search"/>
                        <p>{{language.search2.searchRes.Tips[0]}}“{{searchtxt}}”{{language.search2.searchRes.Tips[1]}}<br>{{language.search2.searchRes.Tips[2]}}~</p>
                    </div>
                </li>
            </ul>
        </div>
        <div slot="bottom" class="mint-loadmore-bottom">
            <span v-if="bottomStatus === 'pull'">{{bottomPullText}}</span>
            <span v-if="bottomStatus === 'drop'">{{bottomDropText}}</span>
        </div>
        <div class="goods_like_bottom" v-if='loading'>
            <div class='goods_like_hr'></div>
            <span>{{language.search2.searchRes.Tips[3]}}</span>
            <div class='goods_like_hr'></div>
        </div>
        

        <skus ref="attrModal" @confirm="_confirm"></skus>
    </div>
</template>

<script>
    import {mapMutations} from 'vuex'
    import skus from '../../components/skus.vue';
    import heads from '@/components/header.vue'
    import uniLoadMore from '@/components/uni-load-more.vue'
    import { buildUrlFromBackend } from '../../common/pluginsutil.js'

    export default {
        data () {
            return {
                title: '搜索结果',
                //商品默认图片
                defaultPicture: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/Default_picture.png',
                back_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/back2x.png',
                empic_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/spnull2x.png',
                serchimg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/searchNew.png',
                shoppingImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/redgwc.png',
                shoppingImgGray: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/shopping_cart_gray.png',
                filter: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/home_searchResult/searchResult_filter.png',
                filter_h: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/home_searchResult/searchResult_filter_select.png',
                sortingBottom: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/home_searchResult/searchResult_bottom.png',
                sortingBottomh: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/home_searchResult/searchResult_bottom_select.png',
                sortingTop: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/home_searchResult/searchResult_top.png',
                sortingToph: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/home_searchResult/searchResult_top_select.png',
                jiantouBottom: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/home_searchResult/searchResult_bottom.png',
                jiantouTop: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/home_searchResult/searchResult_top.png',
                no_search: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/no_search.png',
                sc_icon: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL+'images/icon/goodsDetailed_close.png',
                sc_Btn: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL+'images/icon1/home_searchResult/searchResult_Btn.png',
                vertical: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL+'images/icon1/home_searchResult/searchResult_vertical.png',
                horizontal: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL+'images/icon1/home_searchResult/searchResult_horizontal.png',
                
                host: '',
                searchtxt: '',
                storag: '', //	接收历史记录的值
                prompt: '', //	接收输入提示的值
                searchgoods: '', //接收搜索后商品返回的数据
                success: true, //搜索商品是否有数据返回
                s_type: '', //判断从那个页面进入
                page: 1, //加载页面
                // allLoaded: false,
                autoFill: false, //若为真，loadmore 会自动检测并撑满其容器
                bottomStatus: '',
                bottomPullText: '上拉加载更多...',
                bottomDropText: '释放更新...',
                loading: false,
                topTabBar: 0, // 0|1，商品|店铺
                placeStyle: 'color:#999999',

                loadingType: 1,

                mask_display1: false,
                attribute_id: '',
                attrList: '',
                skuBeanList: '',
                num: '', //规格数量
                price: '', //规格价格
                imgurl: '', //规格图片
                haveSkuBean: '', //选择规则属性
                shopping_id: '', //单个商品购物车id
                closeImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/guanbi2x.png',
                jian_hui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jianhui2x.png',
                jian_hei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jian2x.png',
                jia_hei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jia+2x.png',
                jia_hui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/add+2x.png',
                fastTap: true,
                proid: '',
                numb: 1,
                
                highKeys: {},
                skuName: 'SkuID',
                skuName1: 'Price',
                skuName2: 'Pic',
                skuName3: 'Stock',
                spliter: ',',
                list: {},
                result: {},
                
                searchType: '',
                filterMaskFlag: false,
                
                pinpaiFlag: true,
                sorting: 'asc', //  desc：降序，  asc：升序
                brand_list: [],
                brand_id: '',
                min_price: '', //最低价
                max_price: '',
                brand_id1: '',
                min_price1: '', //最低价
                max_price1: '',
                mch_status:false,
                bgColor: [
                    {
                        item: '#FFFFFF'
                    },
                    {
                        item: '#FFFFFF'
                    }
                ],
                setMethods:false,//默认排版方式
                // 瀑布流相关参数
                listLeft: [],
                listRight: [],
                cardLeftHeight: 0,
                cardRightHeight: 0,
                handleNum: 0,
                class_pro: [],
                falg:true,
                 
               
            }
        },
        onReachBottom () {
            if (this.loadingType != 0) {
                return
            }
            
            
            this.loadingType = 1

            this.page++

            let query_criteria = {
                brand_id: this.brand_id1,
                min_price: this.min_price1,
                max_price: this.max_price1
            }
            var data = {
                api: 'app.search.search',
                num: this.page,
                type: this.topTabBar,
                keyword: this.searchtxt,
                sort_criteria: this.searchType,
                query_criteria: JSON.stringify(query_criteria)
            }
            
            if(this.searchType == 'volume' || this.searchType == 'price'){
                data.sort = this.sorting
            }else if(this.searchType == 'comment_num'){
                data.sort = 'desc'
            } 
            
            this.$req.post({ data }).then(res => {
                let { code, message } = res
                if (code == 200) {
                    let data = res.data.list
                    
                    if(this.topTabBar == 0){
                        data.filter(item=>{
                            item.vip_price = Number(item.vip_price).toFixed(2)
                            item.vip_yprice = Number(item.vip_yprice).toFixed(2)
                        })
                    }
                    // 瀑布流相关start
                    this.class_pro = this.class_pro.concat(data);
                     // 瀑布流相关end
                    this.searchgoods.push(...data)
                    if(this.page >=2){
                        this.waterfall()
                    }
                    if (data.length > 9) {
                        this.loadingType = 0
                    } else {
                        this.loadingType = 2
                    }
                } else {
                    this.loadingType = 0
                    uni.showToast({
                        title: message,
                        duration: 1500,
                        icon: 'none'
                    })
                }
            })
        },
        onLoad (option) {
            this.LaiKeTuiCommon.getLKTApiUrl()
            this.searchtxt = option.searchtxt
            this.topTabBar = parseInt(option.topTabBar)
            this.s_type = this.$store.state.type
            this.$nextTick(() => {
                this._axios()
            })

        },
        components: {
            heads, uniLoadMore, skus
        },
        computed: {
            halfWidth: function () {
                var gru = uni.getStorageSync('data_height') ? uni.getStorageSync('data_height') : this.$store.state.data_height
                var heigh = parseInt(gru)
                // #ifdef MP
                heigh = 0
                // #endif
                return heigh + 'px'
            }
        },
        methods: {
            handleShopImg(index){
                this.searchgoods[index].logo = this.defaultPicture
            },
            handleErrorImg(index) {
                    this.searchgoods[index].imgurl = this.defaultPicture
            },
            changeSet(){
              this.loadingType = 0
              this.setMethods=!this.setMethods
            },
            // 瀑布流相关start
            onImageLoad(e, id){
                let divWidth = 336;			//实际显示的单栏宽度，345upx
                let oImgW = e.detail.width; //图片原始宽度
                let oImgH = e.detail.height; //图片原始高度
                let rImgH = Math.round(divWidth*oImgH/oImgW * 100) / 100;	//重新计算当前载入的card的高度
            
                const query = uni.createSelectorQuery().in(this);
            
                let height = 0;
            
                query.select('#' + id).boundingClientRect(data => {
                    height = data.height;
                }).exec();
            
                rImgH = rImgH + (height * 2);
            
                if(e.target.dataset.name == 0){
                    this.cardLeftHeight += rImgH
                }else{
                    this.cardRightHeight += rImgH
                }
            
                this.waterfall()
                this.handleNum ++;
            },
            waterfall(){
                if(this.class_pro.length == 0){
                    return
                }
                if(this.cardLeftHeight>this.cardRightHeight){
                    let cardList = this.class_pro.splice(0,1);		//初始化图片显示
                    this.listRight.push(cardList[0]);
                }else if(this.cardLeftHeight<=this.cardRightHeight){
                    let cardList = this.class_pro.splice(0,1);		//初始化图片显示
                    this.listLeft.push(cardList[0]);
                }
            },
            empty () {
                this.listRight = [];
                this.listLeft = [];
                this.cardLeftHeight = 0;
                this.cardRightHeight = 0;
                this.handleNum = 0;
            },
            // 瀑布流相关end
            
            
            cleardata(){
                this.searchtxt = ''  
            },
            changeBrand(item){
                this.brand_id = item.brand_id
            },
            removeMask(){
                this.filterMaskFlag = false
                
                this.min_price = this.min_price1
                this.max_price = this.max_price1
                this.brand_id = this.brand_id1
            },
            reset(){
                this.min_price = ''
                this.max_price = ''
                this.brand_id = ''
            },
            filterOk(){
                this.min_price1 = this.min_price
                this.max_price1 = this.max_price
                this.brand_id1 = this.brand_id
                
                if(Number(this.max_price1) < Number(this.min_price1)){
                    uni.showModal({
                        content: '最高价不能低于最低价',
                        showCancel: false
                    });
                    this.max_price1 = '';
                    this.max_price = '';
                    return ;
                }
                
                this.page = 1
                this._axios()
                this.filterMaskFlag = false
            },
            shaixuanClick(){
                this.filterMaskFlag = !this.filterMaskFlag
                
                if(!this.filterMaskFlag){
                    this.min_price = this.min_price1
                    this.max_price = this.max_price1
                    this.brand_id = this.brand_id1
                }
            },
            pinpaiClick(){
                this.pinpaiFlag = !this.pinpaiFlag
            },
            clickType(type){
                if((type == 'volume' || type == 'price') && this.searchType == type){
                    this.sorting = this.sorting=='asc'?'desc':'asc'
                }else{
                    this.sorting = 'asc'
                }
                this.searchType = type
                
                this.page = 1
                this._axios();
            },
            _searchB(){
                this.page = 1
                uni.pageScrollTo({
        　　　　　　duration:0,
        　　　　　　scrollTop:0
        　　　　})
                this._axios()
            },
            // 点击确定购买之后，如果库存不为零。则运行
            _shopping (id) {
                var me = this
                if (this.haveSkuBean) {
                    var data = {
                        api: 'app.product.add_cart',
                        pro_id: this.proid,
                        attribute_id: this.haveSkuBean.cid,
                        num: this.numb,
                        type: 'addcart',
                        access_id: this.$store.state.access_id
                    }

                    uni.request({
                        data,
                        url: uni.getStorageSync('url'),
                        header: {
                            'content-type': 'application/x-www-form-urlencoded'
                        },
                        method: 'POST',
                        success: (res) => {
                           
                            if(res.code != 200) {
                                uni.showToast({
                                    title: res.data.message,
                                    duration: 1500,
                                    icon: 'none'
                                })
                            }
                            let {
                                data: {
                                    code,
                                    data,
                                    message
                                }
                            } = res
                            if (code == 200) {
                                uni.showToast({
                                    title: this.language.search2.searchRes.Tips[4],
                                    duration: 1000,
                                    icon: 'none'
                                })
                                me.access_id = data.access_id
                                me.$store.state.access_id = data.access_id

                                this.fastTap = true
                                me.haveSkuBean = ''
                                me.$refs.attrModal._mask_f()
                            } else {
                                this.fastTap = true
                            }
                        },
                        error: function (err) {
                            this.fastTap = true
                        }
                    })
                } else {
                    this._mask_display()
                    this.fastTap = true
                }
            },
            // 遮罩层中，点击确认按钮
            _confirm(sku){
                Object.assign(this.$data,sku)
                this._confirm1()
            },
            _confirm1 () {
                if (!this.fastTap) {
                    return
                }
                this.fastTap = false
                if (Boolean(this.haveSkuBean)) {
                    if (this.num == 0) {
                        uni.showToast({
                            title: this.language.search2.searchRes.Tips[5],
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
                            title: this.language.search2.searchRes.Tips[5],
                            duration: 1000,
                            icon: 'none'
                        })
                        this.fastTap = true
                    } else {
                        uni.showToast({
                            title: this.language.search2.searchRes.Tips[6],
                            duration: 1000,
                            icon: 'none'
                        })
                        this.fastTap = true
                    }
                }
            },
            // 为你推荐商品右下角的小购物车图标
            shopping_j (id,item) {
                if(item&&item.status == 3){
                    uni.showToast({
                        title: '商品已下架',
                        icon: 'none'
                    })
                    return
                }
                if(item&&item.num == 0){
                    uni.showToast({
                        title: '商品已售罄',
                        icon: 'none'
                    })
                    return
                }
                var me = this
                if (!this.fastTap) {
                    return
                }
                this.fastTap = false
                this.proid = id
                let token = uni.getStorageSync('access_id')
                var data = {
                    api: 'app.product.index',
                    pro_id: id,
                    access_id: token
                }
                uni.request({
                    data,
                    url: uni.getStorageSync('url'),
                    header: {
                        'content-type': 'application/x-www-form-urlencoded'
                    },
                    method: 'POST',
                    success: (res) => {
                        
                        me.fastTap = true
                        let {
                            data: {
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
                       if(commodity_type == 1){
                           this.$refs.attrModal.pro2 = {commodity_type, write_off_settings}
                       }
                       me.$refs.attrModal.initData()
                       // 当只有一个规格且库存为0的时候不让它进sku
                       let flag=skuBeanList.every((item,index)=>{
                           return item.count==0
                       })
                       if(flag){
                           uni.showToast({
                               title:this.language.toasts.goodsDetailed.kucun,
                               icon:'none'
                           })
                           return
                       }
                       me.$refs.attrModal._mask_display()
                       
                        me.collection = type
                    },
                    error: function (err) {
                        me.fastTap = true
                    }
                })

            },
            // 点击搜索框返回搜索页面
            handleinput () {
                uni.hideKeyboard()
                uni.navigateBack({
                    delta: 1
                })
            },
            // 点击搜索图标回到进入搜索功能的页面
            _goBack () {
                uni.navigateBack({
                    delta: 2
                })
            },
            // 点击去逛逛回到商城首页
            _home () {
                uni.redirectTo({
                    url: '/pages/shell/shell?pageType=home'
                })
            }, 
            _axios(){ 
               const throttledFunc  = this.throttle(this._axios1) ;
               throttledFunc()
            },
              throttle(func, delay = 1500) {
                let lastExecTime = 0;
                let isPending = false; // 标记异步是否完成
                return async (...args) => {
                  const now = Date.now();
                  if (now - lastExecTime < delay || isPending) return;
                  
                  isPending = true;
                  try {
                    await func.apply(this, args); // 等待异步执行完成
                  } catch (e) {
                    console.error('执行失败：', e);
                  } finally {
                    lastExecTime = Date.now();
                    isPending = false;
                  }
                };
              },
            async _axios1 () { 
                if(this.page == 1){
                    uni.pageScrollTo({ 　　　　　　
            　　　　　　duration: 0,//过渡时间必须为0，否则运行到手机会报错
            　　　　　　scrollTop: 0 //滚动到实际距离是元素距离顶部的距离减去最外层盒子的滚动距离（如res.top - data.top）
            　　　　})
                }
                
                var me = this
                let query_criteria = {
                    brand_id: this.brand_id1,
                    min_price: this.min_price1,
                    max_price: this.max_price1
                }
                var data = {
                    api: 'app.search.search',
                    num: this.page,
                    type: this.topTabBar,
                    keyword: this.searchtxt,
                    sort_criteria: this.searchType,
                    query_criteria: JSON.stringify(query_criteria)
                    
                }
                
                if(this.searchType == 'volume' || this.searchType == 'price'){
                    data.sort = this.sorting
                }else if(this.searchType == 'comment_num'){
                    data.sort = 'desc'
                } 
                      await this.$req.post({data}).then(res => {
                        if (res.code == 200) {
                            if(this.brand_list.length == 0){
                                this.brand_list = res.data.brand_class_list
                            }
                            
                            var data = res.data.list
                            this.mch_status = res.data.mch_status
                            if (data.length) {
                                if(this.topTabBar != 2){
                                    data.filter(item=>{
                                        item.vip_price = Number(item.vip_price).toFixed(2)
                                        item.vip_yprice = Number(item.vip_yprice).toFixed(2)
                                    })
                                    //瀑布流相关start
                                    this.class_pro = data.concat()
                                    this.empty();
                                    setTimeout(() => {
                                        this.waterfall();
                                    }, 100)
                                    // 瀑布流相关end
                                }
                                
                                me.searchgoods = data
                                me.success = true
                                
                                if (data.length > 8) {
                                    me.loadingType = 0
                                } else {
                                     this.empty()//瀑布流相关
                                    me.loadingType = 2
                                }
                            } else {
                                me.searchgoods = '未找到相关宝贝'
                                me.success = false
                            }
                        } else {
                            me.success = false
                            uni.showToast({
                                title: res.message,
                                duration: 1500,
                                icon: 'none'
                            })
                        }
                    })
                   
            },
        
            // 点击导航切换商品/店铺
            _search (type) {
                this.searchgoods = ''
                this.topTabBar = type
                this.success = true
                this.page = 1
                this.empty()
                this._axios()
            },
            // 进店
            _goStore (shop_id) {
                uni.navigateTo({
                    url: '../../pagesB/store/store?shop_id=' + shop_id
                })
            },
           
            // 下拉加载
            loadBottom () {
                var me = this
                var data = {
                    api: 'app.search.search',
                    page: this.page
                }
                if (this.searchgoods.length > 0) {
                    uni.request({
                        data,
                        url: uni.getStorageSync('url'),
                        header: {
                            'content-type': 'application/x-www-form-urlencoded'
                        },
                        method: 'POST',
                        success: (res) => {
                            if (res.data.code == 200) {
                                let {
                                    data: {
                                        data
                                    }
                                } = res
                                me.page += 1
                                if (data.length < 10 && data.length > 0) {
                                    me.searchgoods = me.searchgoods.concat(data)
                                } else {
                                    // me.allLoaded = true
                                    me.loading = true
                                }
                                me.$refs.loadmore.onBottomLoaded()
                            } else {
                                uni.showToast({
                                    title: res.data.message,
                                    duration: 1500,
                                    icon: 'none'
                                })
                            }
                        },
                        error: function (err) {
                        },
                    })
                }
            },
            handleBottomChange (status) {
                this.bottomStatus = status
            },
            // 营销商品跳转解析（同首页逻辑）
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
            _seeGoods(item) {
                if (!item) return;
                const id = item.id;
                const url = this._resolveMarketingUrl(item.marketingParams);
                if (url) {
                    if (id) this.pro_id(id);
                    uni.navigateTo({ url });
                    return;
                }
                this._goods(id);
            },

            /*   跳转商品详情页    */
            _goods (id) {
                this.pro_id(id)
                uni.navigateTo({
                    url: '/pagesC/goods/goodsDetailed?toback=true&pro_id='+id
                })
            },
            ...mapMutations({
                pro_id: 'SET_PRO_ID',
                type: 'SET_TYPE',
                scroll_t: 'SET_SCROLL_T'
            }),
        },
        beforeDestroy () {
            this.type('')
        },
        
        updated () {
            this.$nextTick(function () {
                let position = this.$store.state.scroll_t //返回页面取出来
            })
        }
    }
</script>

<style scoped lang="less">
    @import url("@/laike.less");
    @import url("../../static/css/search/searchResult.less");
    .mask {
        height: 100vh;
        width: 100%;
        background-color: rgba(000, 000, 000, 0.5);
        position: fixed;
        top: 0;
        left: 0;
        z-index: 999;
    }
    .white_h{
        height: 48rpx;
    }
    .cha {
        width: 24rpx;
        height: 24rpx;
        position: absolute;
        top: 0;
        right: 0;
    }

    .back {
        background-color: #fff;
        height: 52rpx;
        border-radius: 8rpx;
    }

    .orange {
        background-color: #FFFFFF;
        color: @btnBackground;
        height: 52rpx;
        border: 2rpx solid @btnBackground !important;
        border-radius: 8rpx;
    }

    .select {
        color: #666666;
        height: 56rpx;
        border: 0rpx solid rgba(2, 2, 2, 1) !important;
        background: rgba(248, 248, 248, 1);
        border-radius: 8rpx;
    }

    .select > view {
        margin: 0px 30rpx;
    }

    .orange > view {
        margin: 0px 30rpx;
    }

    .mask_d {
        height: auto;
    }

    .mask_two li {
        float: left;
        width: auto;
        min-width: 60rpx;
        border: 1px solid #eee;
        margin: 24rpx 20rpx 0 0;
        padding: 0 10rpx;
    }

    .queren_div {
        height: 98rpx;
    }

    .mask_num {
        display: flex;
        align-items: center;
        justify-content: space-between;
        height: 90rpx;
    }

    .mask_quren {
        position: absolute;
    }


    .border-right {
        border-right: 1px solid #ddd !important;
    }

    .border-left {
        border-left: 1px solid #ddd !important;
    }
    
 



.goods_content_item_icon{
    display: flex;
    margin-right: 24rpx;
    padding-left: 20rpx;
    margin-bottom: 16rpx;
    flex-wrap: wrap;
}

.goods_content_item_icon>view{
    display: flex;
    align-items: center;
    justify-content: center;
    width:auto;
    padding: 0 6rpx;
    margin-bottom: 10rpx;
    height:30rpx;
    border:1px solid #42B4B3;
    box-sizing: border-box;
    padding: 2rpx 4rpx;
    font-size: 20rpx;
    margin-right: 10rpx;
}

    .new_goods_goods_icon{
        >view{
            padding: 0;
            margin: 0;
            margin-bottom: 8rpx;
            border: initial;
            min-width: 64rpx;
            height: 40rpx;
            border-radius: 8rpx;
            color: #ffffff;
            background:#42B4B3;
            margin-right: 8rpx;
        }
        .color1{background: #FA5151;}
        .color2{background: #FA9D3B;}
        .color3{background: #1485EE;}
    }
</style>
