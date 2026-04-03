<template>
    <view class="skeleton containerBg" :style="grbj">
        <div class="order_ii">
            <heads :title="language.allGoods.title" :bgColor='bgColor' :titleColor="'#333333'" :returnFlag="true"
                :bgImg="headBg"></heads>
            <div class="order_container skeleton">
                <div class="allgoods_b">
                    <!-- 左边的列表 -->
                    <!-- #ifdef H5 || MP -->
                    <ul class="allgoods_left" ref="allgoods_left" v-if="load">
                        <scroll-view scroll-y="true" class="allgoods_left_scroll">
                        <li v-for="(item, index) in centre_title" :class="{ color: title_index == index }" :key="index"
                            class="allgoods_l" @tap="_title(index)">
                            <div>{{ item.cate_name }}</div>
                            <div :class="{ isborder: title_index == index }"></div>
                        </li>
                        </scroll-view>
                    </ul>
                    <!-- #endif -->
                    <!-- #ifdef APP-PLUS -->
                    <ul class="allgoods_left" ref="allgoods_left" v-if="load" style="margin-top: 0 !important;">
                        <scroll-view scroll-y="true" class="allgoods_left_scroll">
                        <li v-for="(item, index) in centre_title" :class="{ color: title_index == index }" :key="index"
                            class="allgoods_l" @tap="_title(index)">
                            <div>{{ item.cate_name }}</div>
                            <div :class="{ isborder: title_index == index }"></div>
                        </li>
                        </scroll-view>
                    </ul>
                    <!-- #endif -->
                    <ul class="allgoods_left" ref="allgoods_left" v-else>
                        <li class="allgoods_l" v-for="(item, index) of 5" :key="index">
                            <div class="skeleton-rect" style="width: 100%;height: 26px">{{item}}</div>
                        </li>
                    </ul>
                    <!-- 右边的详细分类 -->
                    <div class="allgoods_right" v-if="load">
                         <!-- #ifdef APP-PLUS -->
                            <view style="height: 76rpx;"></view>
                        <!-- #endif -->
                        <div style="background-color: #FFFFFF;border-radius: 24rpx;margin-top: 24rpx;"
                            v-if="title_index==0" v-for="(items, index) in alllist.children" :key="index">
                            <view v-if="items.children.length > 0" style="display: flex;flex-direction: column;">
                                <view class="title_centren">{{items.cate_name}}</view>
                                <ul class="allgoods_u">
                                    <block v-for="(itemss, indexs) in items.children" :key="indexs">
                                        <li @tap="_goods(itemss.child_id, itemss.name)">
                                            <!-- 此v-if是为了解决@error无法在微信小程序上有效的问题 -->
                                            <img v-if="itemss.picture != ''" style="width: 120rpx;height: 120rpx;border-radius: 16rpx;"
                                                lazy-load="true" :src="itemss.picture" @error="handleErroeImage(items.children,indexs)"/>
                                            <img v-else style="width: 120rpx;height: 120rpx;border-radius: 16rpx;"
                                                lazy-load="true" src="../../static/img/Default_picture.png" @error="handleErroeImage(items.children,indexs)"/>    
                                            <p>{{ itemss.name }}</p>
                                        </li>
                                    </block>

                                </ul>
                            </view>

                            <div v-else>
                                <view class="title_centren" style="padding-top: 24rpx;">{{items.cate_name}}</view>
                                <div><img class="noFindImg" style="margin-top: 64rpx;" :src="noOrder" /></div>
                                <view class="noFindText"
                                    style="font-size: 26rpx;margin-top: 24rpx;padding-bottom: 64rpx;">
                                    {{language.allGoods.no_goods}}
                                </view>
                            </div>
                        </div>

                        <div class="rigth_box" style="background-color: #FFFFFF;overflow-y: auto;border-radius: 24rpx;margin-top: 24rpx;"
                            v-if="title_index!=0">
                            <view v-if="centre_list.length > 0" style="display: flex;flex-direction: column;">
                                <ul class="allgoods_u">
                                    <li @tap="_goods(items.child_id, items.name)" v-for="(items, index) in centre_list"
                                        :key="index">
                                        <!-- 此v-if是为了解决@error无法在微信小程序上有效的问题 -->
                                        <image v-if="items.picture != ''" style="width: 120rpx;height: 120rpx;border-radius: 16rpx;"
                                            lazy-load="true" :src="items.picture" @error="handleErroeImage2(items)"/>
                                        <image v-else style="width: 120rpx;height: 120rpx;border-radius: 16rpx;"
                                            lazy-load="true" src="../../static/img/Default_picture.png" />
                                        <p>{{ items.name }}</p>
                                    </li>
                                </ul>
                            </view>

                            <div v-else>
                                <div><img class="noFindImg" style="margin-top: 90%;" :src="noOrder" /></div>
                                <view class="noFindText"
                                    style="font-size: 26rpx;margin-bottom: 64rpx;margin-top: 24rpx;">
                                    {{language.allGoods.no_goods}}
                                </view>
                            </div>
                        </div>

                    </div>
                    <div class="allgoods_right" v-else>
                        <ul class="allgoods_u">
                            <li style="display:flex;flex-direction: column;justify-content:center;align-items:center;margin-top: 10rpx;"
                                v-for="(item, index) of 6" :key="index">
                                <div style="width: 60px;height: 60px;" class="skeleton-circle"></div>

                                <p class="skeleton-rect" style="width: 50px;margin-top: 20px;">{{item}}</p>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>

            <skeleton :loading="!load" :animation="true" bgColor="#FFF"></skeleton>
        </div>
    </view>
</template>

<script>
      import skeleton from "@/components/skeleton";
    import {
        mapMutations,
        mapState
    } from 'vuex';

    export default {
        data() {
            return {
                loadGif: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/loading.gif',
                noOrder: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/flzwsp.png',
                title_index: 0, //商品导航切换
                centre_title: '', //左侧分类标题
                centre_list: '', //右侧详细分类
                cid: '', //分类id
                headBg: 'url(' + this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    'images/icon1/auction/grbj.png) 750rpx 100%;',
                load: false,
                cc: 0, //导航下标
                alllist: [],
                grbj: 'background-image: url(' + this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    'images/icon1/auction/grbj.png);background-size: 100vw 100vh;background-repeat: no-repeat;',
                skeleton_left: [

                ],
                bgColor: [{
                        item: '#FFFFFF !important'
                    },
                    {
                        item: '#FFFFFF !important'
                    }
                ],
                Width: false
            };
        },
        components:{
            skeleton,
        },
        computed: {
            h5Height: function() {
                var height = 100;
                // #ifndef H5
                height = 0;
                // #endif
                return uni.upx2px(height) + 'px';
            },
            ...mapState({
                _cart_num: 'cart_num'
            })
        },
        onPullDownRefresh() {
            this.LaiKeTuiCommon.getUrlFirst(this._axios);
            this.storag = uni.getStorageSync('history');
        },
       
        mounted() {
              this.LaiKeTuiCommon.getUrlFirst(this._axios);
              
              
            this.getCart();

            this.cc = this.$store.state.cindex;
            this.storag = uni.getStorageSync('history');
        },
        onPageScroll(e) {
            let me = this
            let query = uni.createSelectorQuery().in(this);
            query.select('.order_ii').boundingClientRect(data => {
                if (data.top < -5) {
                    me.headBg = '#ffffff'
                } else {
                    me.headBg = 'transparent'
                }
                // 这个 
            }).exec();
        },
        methods: {
            handleErroeImage(list,index){
                list[index].picture= require("@/static/img/Default_picture.png") 
            },
            handleErroeImage2(list){
                list.picture= require("@/static/img/Default_picture.png") 
            },
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
                        if (data) {
                            this.cart_num(data.length)
                        }

                        //添加，移除tabbar购物车小点提醒
                        if (this._cart_num) {
                            var cart_num_str = String(this._cart_num)

                            // #ifdef MP-WEIXIN
                            uni.setTabBarBadge({
                                index: 2,
                                text: cart_num_str
                            })
                            // #endif

                            // #ifndef MP-WEIXIN
                            uni.setTabBarBadge({
                                index: 2,
                                text: cart_num_str
                            })
                            // #endif

                        } else {
                            // #ifdef MP-WEIXIN
                            uni.removeTabBarBadge({
                                index: 2
                            })
                            // #endif

                            // #ifndef MP-WEIXIN
                            uni.removeTabBarBadge({
                                index: 2
                            })
                            // #endif
                        }
                    } else {
                        // #ifdef MP-WEIXIN
                        uni.removeTabBarBadge({
                            index: 2
                        })
                        // #endif

                        // #ifndef MP-WEIXIN
                        uni.removeTabBarBadge({
                            index: 2
                        })
                        // #endif
                    }
                })
            },
            _axios() {
                var data = {
                    api: 'app.search.index',};

                this.$req.post({
                    data
                }).then(res => {
                    let {
                        data
                    } = res;
                    this.centre_title = data.List;
                    this.alllist = data.allList
                    this.centre_title.unshift(data.allList)
                    this.centre_list = data.List[0];
                    this.cc = this.$store.state.cindex;
                    this._title(this.cc);
                    this.load = true;
                    uni.stopPullDownRefresh()
                })

            },
            /*   商品导航切换    */
            _title(index) {
                this.title_index = index;
                this.centre_list = this.centre_title[index].children;
                this.cindex(index);
            },
            /*   跳转商品展示页面    */
            _goods(id, name) {
                uni.navigateTo({
                    url: '/pagesC/goods/goods?cid=' + id + '&name=' + name
                });
            },
            ...mapMutations({
                cindex: 'SET_CINDEX',
                cart_num: 'SET_CART_NUM'
            })
        }
    };
</script>
<style>
    page {
        background-color: #f4f5f6;
    }
</style>
<style scoped lang="less">
    @import url("@/laike.less");
    @import url('@/static/css/tabBar/allGoods.less');

    /deep/.head {
        background-color: transparent;
    }
</style>
