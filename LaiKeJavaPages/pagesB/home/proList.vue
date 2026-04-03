<template>
    <view class="container" :style="grbj">
        <heads 
            :title="pageType==2?language.proList.title2:language.proList.title1" 
            :titleColor="titleColor" 
            :isAnd="isAnd" :border="true" 
            :bgColor='bgColor' 
            :bgImg="headBg"
            :ishead_w="2">
        </heads>
        <toload v-if="load"></toload>
        <view class="" v-if="list.length > 0 && !load">
            <view class="content">
                <waterfall 
                    class="waterfall"
                    ref="waterfall" 
                    :isPadding="'0'" 
                    :goodsLike="false"
                    :addShopCar="false"
                    :mchLogo="false"
                    :goodsPriceText="false"
                    @_seeGoods="_seeGoods"
                    @_addShopCar="_addShopCar"
                    :goodList="list">
                </waterfall>
            </view>
            <!-- 0下拉加载更多/1加载中/2没有更多了 -->
            <uni-load-more v-if="list.length>8" :loadingType="loadingType"></uni-load-more>
            <skus ref="attrModal" @confirm="_confirm"></skus>
        </view>
        <view class="nodata" v-else>
            <image :src="noPro" mode="aspectFit"></image>
            <p>{{language.mchList.nodata}}</p>
        </view>
    </view>
</template>

<script>
    import waterfall from '@/components/aComponents/waterfall.vue';
    import skus from '../../components/skus.vue';
    export default {
        data() {
            return {
                is_grade: false,
                title: '',
                list: [],
                page: 1,
                loadingType: 0,
                load: true,
                fastTap: true,
                proid: '',
                titleColor: '#333333', //标题字体颜色
                headBg: 'url(' + this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    'images/icon1/auction/grbj.png) 750rpx 100%;',
                grbj: 'background-color: #F4F5F6;background-image: url(' + this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    'images/icon1/auction/grbj.png);background-size: 100vw 100Vh;background-repeat: no-repeat;',
                bgColor: [{
                        item: '#FFFFFF'
                    },
                    {
                        item: '#FFFFFF'
                    },
                ],
                isAnd: true,
                shopImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/allgoods_shopImg.png',
                noPro: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/noPro.png',
                haveSkuBean: '',
                num: '',
                numb: '',
                pageType: '',
            };
        },
        components: {
            skus,
            waterfall
        },
        onLoad(option) {
            this.pageType = option.type
            this.page = 1
            this.axios()
        },
        onShow(){
           
        },
        watch: {
            list(e) {
            },
            load(e) {
            },
        },
        onShow() {
            
        },
        // 下拉加载
        onReachBottom: function() {
            if (this.loadingType != 0) {
                return
            }
            this.loadingType = 1

            this.page++
            this.axios()
            
        },
        onPageScroll(e) {
            //是否隐藏 header组件中的标题模块
            if (e.scrollTop > 0) {
                this.headBg = ''
                this.isAnd = false
            } else {
                this.headBg = 'url(' + this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    'images/icon1/auction/grbj.png) 750rpx 100%;'
                // #ifdef H5
                this.headBg = '#ffffff'
                // #endif
                this.isAnd = true
            }
        },
        methods: {
            //跳转
            toUrl(url) {
                uni.navigateTo({
                    url
                })
            },
            //查看详情
            _seeGoods(id){
                this.toUrl('/pagesC/goods/goodsDetailed?toback=true&pro_id='+id)
            },
            //加入购物车
            _addShopCar(item,id){
                this.shopping_j(id)
            },
            // 为你推荐商品右下角的小购物车图标
            shopping_j(id) {
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
                                type
                            }
                        } = res

                        this.$refs.attrModal.imgurl = pro.img_arr[0]
                        this.$refs.attrModal.num = pro.num
                        this.$refs.attrModal.price = qj_price
                        this.$refs.attrModal.skuBeanList = attribute_list
                        this.$refs.attrModal.initData()
                        this.$refs.attrModal._mask_display()
                    })
                    .catch(error => {
                        this.fastTap = true
                    })
            },
            // suk组件返回数据
            _confirm(sku) {
                Object.assign(this.$data, sku)
            
                if (!this.fastTap) {
                    return
                }
                this.fastTap = false
                if (Boolean(this.haveSkuBean)) {
                    if (this.num == 0) {
                        uni.showToast({
                            title: this.language.proList.Tips[0],
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
                            title: this.language.proList.Tips[0],
                            duration: 1000,
                            icon: 'none'
                        })
                        this.fastTap = true
                    } else {
                        uni.showToast({
                            title: this.language.proList.Tips[1],
                            duration: 1000,
                            icon: 'none'
                        })
                        this.fastTap = true
                    }
                }
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
                                uni.showToast({
                                    title: this.language.proList.Tips[2],
                                    icon: 'none'
                                })
            
                                this.$store.state.access_id = data.access_id
            
                                this.haveSkuBean = ''
                                this.$refs.attrModal._mask_f()
                                this.fastTap = true
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
            //数据初始化
            axios() {
                let data = {
                    api: 'app.index.index',
                    page: this.page, // 页面
                }

                if (this.pageType == 1) {
                    data.app = 'new_arrival'
                } else if (this.pageType == 2) {
                    data.app = 'recommend'
                }

                this.$req.post({
                    data
                }).then(res => {
                    let {
                        list,
                        grade,
                        isGrade
                    } = res.data
                    if (res.code == 200) {
                        this.is_grade = isGrade
                        this.load = false
                        list = list.map(({ stockNum, ...rest }) => {
                          return { contNum: stockNum, ...rest };
                        });
                        if (list) {
                            list.filter(item => {
                                item.vip_yprice = Number(item.vip_yprice).toFixed(2)
                                item.vip_price = Number(item.vip_price).toFixed(2)
                            })
                        }
                        this.list.push(...list)
                        //商品图片参数修改
                        this.list.forEach((item, index) => {
                            //可能有相同路径的图片，会无法加载@load事件；拼接随机字符串，可以解决这个问题
                            item.imgurl = item.imgurl + '?a=' + Math.floor(Math.random() * 1000000);
                        });
                        if (list.length > 0) {
                            this.loadingType = 0
                        } else {
                            this.loadingType = 2
                        }
                    } else {
                        this.load = false
                        uni.showToast({
                            title: res.message,
                            icon: 'none'
                        })
                    }
                })
            }
        
        },
    };
</script>

<style lang="less">
    @import url("@/laike.less");
    @import url('../../static/css/home/proList.less');
    .waterfall{
        flex:1
    }
    .new_goods {
        .new_goods_shop_name {
            font-size: 24rpx;
            font-weight: 400;
            color: #999999;
            line-height: 32rpx;
            margin: 16rpx;
            margin-top: 10rpx;
            margin-bottom: 8rpx;
        }

        .new_goods_goods_name {
            font-size: 28rpx;
            font-weight: 400;
            color: #333333;
            line-height: 40rpx;
            margin: 8rpx 16rpx;
            margin-bottom: 16rpx;
        }

        .new_goods_goods_icon {
            >view {
                padding: 0;
                margin: 0;
                border: initial;
                min-width: 64rpx;
                height: 40rpx;
                border-radius: 8rpx;
                color: #ffffff;
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
    }

    .goods_mun {
        display: flex;
        align-items: flex-end;
        justify-content: space-between;
        margin-top: auto;
        padding-left: 16rpx;
        padding-bottom: 16rpx;
        box-sizing: border-box;

        >span {
            display: flex;
        }

        >img {
            width: 58rpx;
            height: 58rpx;
            margin-left: auto;
        }
    }

    .yprice {
        display: block;
        font-size: 22rpx;
        color: #999999;
        text-decoration: line-through;
        margin-left: 6rpx;
        line-height: 20rpx;
    }

    .allgoods_price {
        display: flex;
        flex-direction: column;
        align-items: flex-start;
    }

    .allgoods_price view {
        font-size: 32rpx;
        line-height: 28rpx;
        font-weight: bold;

        .currency {
            font-size: 24rpx;
            font-weight: 600;
            color: #FA5151;
            line-height: 32rpx;
        }

        .vip_price {
            font-size: 34rpx;
            font-family: DIN-Bold, DIN;
            font-weight: bold;
            color: #FA5151;
            line-height: 42rpx;
            margin-right: 10rpx;
        }

        .sales {
            font-size: 24rpx;
            font-weight: 400;
            color: #666666;
            line-height: 32rpx;
        }
    }

    .allgoods_price p {
        font-size: 24rpx;
        color: #999999;
        line-height: 24rpx;

        &.no_grade_vip_price {
            color: @btnBackground;
        }
    }
</style>
