<template>
    <view class="isPublicMode">
        <template v-if="1">
            <!-- 自定义活动商品 -->
            <view class="is_public_mode">
                <!-- 活动专题 -->
                <view class="mode_title">
                    <view>{{isData.name}}</view>
                    <image :src="LaiKeTuiCommon.getFastImg(titleBg, 200, 40)" lazy-load="true"></image>
                </view>
                <!-- 活动内容 -->
                <scroll-view scroll-x="true" class="mode_content">
                    <view class="mode_content_view" >
                        <view
                            class="mode_content_item"
                            v-for="(item, index) of isData.list" :key="index"
                            @tap="_seeGoods(item.id)">
                                <!-- 商品图 -->
                                <image :src="LaiKeTuiCommon.getFastImg(item.imgurl, 200, 200)" lazy-load="true" mode="aspectFill"></image>
                                <view>
                                    <!-- 商品标题 -->
                                    <view class="mode_content_item_title">{{ item.product_title }}</view>
                                    <!-- 商品价格 -->
                                    <view class="mode_content_item_mun">                                             
                                        <view class="mode_content_item_price" >
                                            <view>
                                                <text class="currency">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</text>
                                                <text class="vip_price">{{LaiKeTuiCommon.formatPrice(item.price)}}</text>
                                                <text class="sales">{{language.bidding_detailed.soldOut}} {{ item.volume }} {{language.group.unit}}</text>
                                            </view>                                               
                                        </view>
                                    </view>
                                </view>
                        </view>
                        <!-- 占位 -->
                        <view style="min-width: 6rpx"></view>
                    </view>
                </scroll-view>
            </view>
        </template>
    </view>
</template>

<script>
    export default{
        props:{
            //总数据
            "isData": {
                type: Object,
                default: {}, 
            },
            // --- 分割线 ---
        },
        data(){
            return{
                titleBg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/home_title_bg.png',
            }
        },
        methods:{
            //查看商品详情
            _seeGoods(id){
                this.$emit('_seeGoods', id)
            }
        }
    }
</script>

<style lang="less" scoped> @import url("@/laike.less");
    .isPublicMode{
        .is_public_mode{
            .mode_title{
                position: relative;
                z-index: 10;
                margin: 38rpx 30rpx 0;
                >view {
                    font-size: 34rpx;
                    line-height: 34rpx;
                    color: @btnBackground;
                    font-weight: bold;
                }
                >image {
                    position: absolute;
                    width: 111rpx;
                    height: 19rpx;
                    top: 20rpx;
                    left: 24rpx;
                    z-index: -1;
                }
            }
            .mode_content{
                margin-top: 30rpx;
                .mode_content_view{
                    display: flex;
                    padding: 0 30rpx;
                    .mode_content_item{
                        display: flex;
                        width: 560rpx;
                        min-width: 560rpx;
                        height: 260rpx;
                        background: rgba(255, 255, 255, 1);
                        box-shadow: 0rpx 2rpx 32rpx 0rpx rgba(0, 0, 0, 0.1);
                        border-radius: 16rpx;
                        padding: 30rpx 24rpx;
                        box-sizing: border-box;
                        margin-right: 24rpx;
                        >image {
                            width: 200rpx;
                            height: 200rpx;
                            border-radius: 10rpx;
                            margin-right: 24rpx;
                        }
                        >view {
                            flex: 1;
                            display: flex;
                            flex-direction: column;
                            align-items: flex-start;
                            .mode_content_item_title{
                                color: #020202;
                                font-size: 28rpx;
                                line-height: 31rpx;
                            }
                            .mode_content_item_mun{
                                display: flex;
                                align-items: flex-end;
                                justify-content: space-between;
                                margin-top: auto;
                                padding-left: 16rpx;
                                padding-bottom: 16rpx;
                                box-sizing: border-box;
                                >img {
                                    width: 58rpx;
                                    height: 58rpx;
                                    margin-left: auto;
                                }
                                .mode_content_item_price{
                                    display: flex;
                                    flex-direction: column;
                                    align-items: flex-start;
                                    >view{
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
                                            font-family: DIN;
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
                                    >p{
                                        color: #999999;
                                        font-size: 24rpx;
                                        line-height: 24rpx;
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
