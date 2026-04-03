<template>
    <view class="navTabBox"
          :style="'background: linear-gradient(90deg, '+ bgColor[0].item +' 50%, '+ bgColor[1].item +' 100%);margin-top:'+mbConfig+'rpx;color:'+txtColor+';'">
        <view class="goodsBox">
            <scroll-view scroll-x="true" class="goods_title">
                <view class="seckill_content_view" style="padding-bottom: 16rpx;">
                    <view @tap="changeBottom(index)" class="goods_title_item"
                          v-for="(item, index) of bottom_list" :key="index"
                          :style="{borderRightColor: borderRightColor}"
                          :class="{active: index == bottomIndex}"
                    >
                        <view :style="{color: index == bottomIndex ? txtSelectColor : txtColor}">{{ item.pname }}</view>
                        <text :style="{color: index == bottomIndex ? txtSelectColor : txtColor}">{{ item.english_name }}</text>
                        <view v-if="index == bottomIndex" class="active-view" :style="{backgroundColor: selectBorderColor}"></view>
                    </view>
                </view>
            </scroll-view>

            <div class="goods_ul goods_ul-waterfall" v-if="show">

                <div class="goods_ul_left">
                    <template v-if="listLeft.length>0">
                        <div class="goods_like goods_ul_left_li" v-for="(item, index) in listLeft" :key="index" @tap="_goods(item.id)">
                            <div class="goods_like_img relative" style="margin: 0 auto 10rpx">
                                <image  lazy-load :src="item.cover_map || item.imgurl" data-name="0" mode="widthFix" width="100%" @load="onImageLoad($event, 'goods_ul_left_li' + index )"/>

                                <div v-if="item.status == 3" class="dowmPro" >
                                    {{language.store.shelf}}
                                </div>
                                <div v-else-if="item.contNum == '0'" class="dowmPro" >
                                    <!-- 已售罄 -->
                                    {{language.shoppingCart.soldOut}}
                                </div>
                            </div>
                            <view :id="'goods_ul_left_li' + index">
                                <p class="overtitle">{{ item.product_title || item.name }}</p>
                                <view class="goods_content_item_icon" v-if="item.rx|| item.tj||item.xp">
                                    <view v-if="item.rx">{{language.home.hot}}</view>
                                    <view v-if="item.tj" style="color: #FE9331;border-color: #FE9331;">{{language.home.recommend}}</view>
                                    <view v-if="item.xp" style="color: #42B4B3;border-color: #42B4B3;">{{language.home.New_products}}</view>
                                </view>
                                <!-- <p class="overflowText">{{ item.subtitle }}</p> -->
                                <div class="goods_mun">
                                    <div class="allgoods_price" v-if="is_grade">
                                        <view class='red'>
                                            <text class="currency">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</text>
                                            <text class="vip_price">{{LaiKeTuiCommon.formatPrice(item.vip_price)}}</text>
                                            <text class="sales">{{language.home.sales}} {{ item.volume }} {{language.home.piece}}</text>
                                        </view>
                                        <p style="text-decoration: line-through;">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(item.vip_yprice)}}</p>
                                    </div>
                                    <div class="allgoods_price" v-else>
                                        <view class='red'>
                                            <text class="currency">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</text>
                                            <text class="vip_price">{{LaiKeTuiCommon.formatPrice(item.vip_yprice)}}</text>
                                            <text class="sales">{{language.home.sales}} {{ item.volume }} {{language.home.piece}}</text>
                                        </view>
                                    </div>


                                </div>
                            </view>
                           <!-- <view class="discover" @tap.stop="$emit('toUrl', '/pagesC/collection/discover?pro_id=' + item.id)">{{language.home.Look}}</view> -->
                        </div>
                    </template>
                </div>
                <div class="goods_ul_right">
                    <template v-if="listRight.length>0">
                        <div class="goods_like goods_ul_right_li" v-for="(item, index) in listRight" :key="index" @tap="_goods(item.id)">
                            <div class="goods_like_img relative" style="margin: 0 auto 10rpx">
                                <image  lazy-load :src="item.cover_map || item.imgurl" data-name="1" mode="widthFix" width="100%" @load="onImageLoad($event, 'goods_ul_right_li' + index)"/>

                                <div v-if="item.status == 3" class="dowmPro" >
                                    {{language.store.shelf}}
                                </div>
                                <div v-else-if="item.num == '0'" class="dowmPro" >
                                    <!-- 已售罄 -->
                                    {{language.shoppingCart.soldOut}}
                                </div>
                            </div>
                            <view :id="'goods_ul_right_li' + index">
                                <p class="overtitle">{{ item.product_title || item.name }}</p>
                                <view class="goods_content_item_icon" v-if="item.rx|| item.tj||item.xp">
                                    <view v-if="item.rx">{{language.home.hot}}</view>
                                    <view v-if="item.tj" style="color: #FE9331;border-color: #FE9331;">{{language.home.recommend}}</view>
                                    <view v-if="item.xp" style="color: #42B4B3;border-color: #42B4B3;">{{language.home.New_products}}</view>
                                </view>
                                <div class="goods_mun">
                                    <div class="allgoods_price" v-if="is_grade">
                                        <view class='red'>
                                            <text class="currency">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</text>
                                            <text class="vip_price">{{LaiKeTuiCommon.formatPrice(item.vip_price)}}</text>
                                            <text class="sales">{{language.home.sales}} {{ item.volume }} {{language.home.piece}}</text>
                                        </view>
                                        <p style="text-decoration: line-through;">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(item.vip_yprice)}}</p>
                                    </div>
                                    <div class="allgoods_price" v-else>
                                        <view class='red'>
                                            <text class="currency">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</text>
                                            <text class="vip_price">{{LaiKeTuiCommon.formatPrice(item.vip_yprice)}}</text>
                                            <text class="sales">{{language.home.sales}} {{ item.volume }} {{language.home.piece}}</text>
                                        </view>

                                    </div>


                                </div>
                            </view>
                            <!-- <view class="discover" @tap.stop="$emit('toUrl', '/pagesC/collection/discover?pro_id=' + item.id)">{{language.home.Look}}</view> -->

                            
                        </div>
                    </template>
                </div>
            </div>
        </view>

        <uni-load-more v-if="class_pro.length > 0" :loadingType="loadingType"></uni-load-more>
    </view>
</template>

<script>
    export default {
        name: 'tabNav',
        props: {
            dataConfig: {
                type: Object,
                default: () => {
                }
            },
            page: {
                type: Number
            },
            is_grade: {
                type: Boolean
            }
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
                show: true
            };
        },

        watch: {
            dataConfig (){
                //修改：bug56676 定制项目反馈DIY问题 主线也存在
                // this.bgColor =  this.dataConfig.bgColor.color || []
                // this.mbConfig =  this.dataConfig.mbConfig.val
                // this.txtColor =  this.dataConfig.txtColor.color[0].item
                // this.txtSelectColor =  this.dataConfig.txtSelectColor.color[0].item
                // this.borderRightColor =  this.dataConfig.rightBorderColor.color[0].item
                // this.selectBorderColor =  this.dataConfig.selectBorderColor.color[0].item
                // let list2 = this.dataConfig.list;
                // if(list2){
                //     list2.filter(item=>{
                //         item.list.filter(it=>{
                //             it.vip_price = Number(it.vip_price).toFixed(2)
                //             it.vip_yprice = Number(it.vip_yprice).toFixed(2)
                //         })
                //     })
                //     this.bottom_list = list2;
                // }
                // this.show = false
                // this.class_pro = this.dataConfig.list?this.dataConfig.list[this.bottomIndex].list.concat():''
                // this.empty();
                // setTimeout(() => {
                //     this.waterfall();
                //     this.show = true
                // }, 100)
                // this.class_cid = this.dataConfig.list?this.dataConfig.list[this.bottomIndex].cid:''
            },
            page (val) {
                if (val > 1) {
                    this.loadingType = 1;
                    this.$emit('loadingType', 1)
                    var data = {
                        api: 'app.index.get_more',
                        page: this.page,
                        cid: this.class_cid
                    };

                    this.$req.post({ data }).then(res => {
                        let { data } = res;
                        if(res.code == 200){
                            if (data.length > 0) {
                                data.filter(item=>{
                                    item.vip_price = Number(item.vip_price).toFixed(2)
                                    item.vip_yprice = Number(item.vip_yprice).toFixed(2)
                                })
                            
                                this.class_pro = this.class_pro.concat(data);
                                setTimeout(() => {
                                    this.waterfall();
                                }, 1)
                                this.loadingType = 0;
                                this.$emit('loadingType', 0)
                            } else {
                                this.loadingType = 2;
                                this.$emit('loadingType', 2)
                            }
                        }
                    });
                }
            }
        },
        created() {
            // 获取设备宽度
            uni.getSystemInfo({
                success: (e)  =>{
                    this.isWidth = e.windowWidth / 5
                }
            })
            this.axios()
            this.setLang()
        },
        methods: {
            axios(){
                let data = {
                    api: 'app.index.classList',
                }
                
                this.$req.post({data}).then(res=>{
                    let { data } = res
                    if(res.code == 200){
                        
                        let list2 = res.data.list;
                        list2.filter(item=>{
                            item.list.filter(it=>{
                                it.vip_price = Number(it.vip_price).toFixed(2)
                                it.vip_yprice = Number(it.vip_yprice).toFixed(2)
                            })
                        })
                        
                        this.bottom_list = list2;
                        
                        this.class_pro = res.data.list[this.bottomIndex].list.concat()
                        
                        setTimeout(() => {
                            this.waterfall();
                        }, 1)
                        
                        this.class_cid = res.data.list[this.bottomIndex].cid
                    }else{
                        uni.showToast({
                            title: res.message,
                            icon: 'none'
                        })
                    }
                })
            },
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
            },
            _goods(id) {
                this.$store.state.pro_id = id;
                uni.navigateTo({
                    url: '/pagesC/goods/goodsDetailed?toback=true&pro_id='+id
                });
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
            },
            changeBottom(index) {
                this.bottomIndex = index
                this.$emit('changePage', 1)
                this.class_cid = this.bottom_list[index].cid
                this.empty();
                this.class_pro = this.bottom_list[index].list.concat()
                setTimeout(() => {
                    this.waterfall();
                }, 1)

                if (this.class_pro.length > 0) {
                    this.loadingType = 0
                    this.$emit('loadingType', 0)
                } else {
                    this.loadingType = 2
                    this.$emit('loadingType', 2)
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

            .goods_title_item{
                display: flex;
                align-items: center;
                justify-content: space-between;
                flex-direction: column;
                height: 60rpx;
                padding: 0 30rpx;
                position: relative;
            }

            .goods_title .goods_title_item:not(:last-child){
                border-right: 1px solid #A1CECE;
            }

            .goods_title_item.active view{
                font-weight: bold;
            }

            .goods_title_item.active text{
            }

            .active-view{
                content: '';
                position: absolute;
                bottom: -16rpx;
                left: 50%;
                transform: translateX(-50%);
                width:30rpx;
                height:6rpx;
                border-radius:3rpx;
            }

            .goods_title_item view{
                text-align: center;
                font-size: 34rpx;
                color: #020202;
                line-height: 34rpx;
                white-space: nowrap;
            }
            .goods_title_item text{
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

            .goods_like > p {
                padding: 0 20rpx;
                margin-top: 20rpx;
                text-align: left;
            }

            .overtitle {
                font-size: 26rpx;
                line-height: 30rpx;
                margin-top: 4rpx!important;
                margin-bottom: 10rpx;
                overflow: hidden;
                text-overflow: ellipsis;
                display: -webkit-box;
                -webkit-box-orient: vertical;
                -webkit-line-clamp: 2;
                text-align: left;
                padding: 0 20rpx;
            }

            .goods_mun {
                display: flex;
                align-items: flex-end;
                justify-content: space-between;
                margin-top: auto;
                padding: 0 0 20rpx 20rpx;
            }

            .goods_mun>span{
                display: flex;
            }

            .goods_mun img {
                width: 58rpx;
                height: 58rpx;
                margin-left: auto;
            }

            .goods_mun .yprice{
                display: block;
                font-size: 22rpx;color: #999999;
                text-decoration: line-through;
                margin-left: 6rpx;
                line-height: 20rpx;
            }

            .red{
                color: @priceColor;
            }


            .allgoods_price{
                display: flex;
                flex-direction: column;
                align-items: flex-start;
            }
            .allgoods_price view{
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
                }
                
                .sales {
                    font-size: 24rpx;
                    color: #999999;
                }
                
                
            }
            .allgoods_price p{
                font-size: 24rpx;
                color: #999999;
                line-height: 24rpx;

                &.no_grade_vip_price {
                    color: @btnBackground;
                }
            }


            .vip_price{
                color: @btnBackground;
            }

            .goods_ul-waterfall .goods_like_img {
                width: 100%;
                height: 100%;
                image {
                    width: 100%;
                }
            }

            .goods_content_item_bottom{
                display: flex;
                align-items: center;
                margin-top: 15rpx;
            }
            .goods_content_item_bottom text{
                font-size: 20rpx;
                color: #999999;
                line-height: 20rpx;
            }
            .discover-wrap {
                display: flex;
                flex-direction: column;
                height: 100%;
            }
            .discover{
                position: absolute;
                display: flex;
                right: 0;
                bottom: 0;
                align-items: center;
                justify-content: center;
                width:90rpx;
                height:36rpx;
                background:rgba(118,230,182,1);
                border-radius:10rpx 0rpx 0rpx 0rpx;
                margin-left: auto;
                font-size: 22rpx;
                color: #014241;
            }

            .goods_content_item_icon{
                display: flex;
                /*margin-top: 20rpx;*/
                margin-right: 24rpx;
                padding-left: 20rpx;
                margin-bottom: 16rpx;
            }

            .goods_content_item_icon>view{
                display: flex;
                align-items: center;
                justify-content: center;
                width:62rpx;
                height:30rpx;
                border:1px solid rgba(255,80,65,1);
                /*border-radius:13rpx;*/
                box-sizing: border-box;
                padding: 2rpx 4rpx;
                font-size: 20rpx;
                color: #FF5041;
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
</style>
