<template>
    <view class="isPublicModeFX">
        <!-- 分销商品广告图 -->
		<template v-if="fxBg">
			<view class="package">
				<image :src="LaiKeTuiCommon.getFastImg(fxBg, 750, 240)" mode="widthFix" lazy-load="true" @click="
                        navTo(
                            '/pagesA/distribution/fxPackageProduct'
                        )
                    "></image>
			</view>
		</template> 
        <template v-if="isData && isData.length">
            <view  class="content_newThree">
                <view>
                    <view class="content_newThree_tilte1">
                        {{title}}
                    </view>
                    <view class="content_newThree_tilte2" @tap="_seeMore('/pagesA/distribution/fxProduct')">
                        {{language.home.more}}
                        <image :src="jiantou" lazy-load="true" style="width: 32rpx;height: 44rpx;"></image>
                    </view>
                </view>
                <view>
                    <view 
                        class="content_newThree_content"
                        :style="index==1?'margin-top: 16rpx;':''"
                        v-for="(item, index) of isData" :key="index" 
                        v-if="index < 2" 
                        @tap="_seeGoods(item.id, item.p_id)">
                            <view>
                                <image :src="LaiKeTuiCommon.getFastImg(item.imgurl, 192, 192)" mode="scaleToFill"
                                    lazy-load="true" @error="setDefaultImage(index)" style="width: 96px;height:96px;"/>
                            </view>
                            <view class="content_newThree_name">
                                <view class="overflowHide">{{item.product_title}}</view>
                                <view>{{language.home.share_make}} {{item.fx_price?item.fx_price.toFixed(2):'0.00'}}</view>
                                <view class="content_newThree_price">
                                    <view>
                                        {{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}<span>{{LaiKeTuiCommon.formatPrice(item.price)}}</span><span>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(item.price)}}</span>
                                    </view>
                                    <view>
                                        {{language.seckill.seckill.Panic[3]}}
                                    </view>
                                </view>
                            </view>
                    </view>
                </view>
            </view>
        </template>
    </view>
</template>

<script>
    export default{
        props:{ 
        },
        data(){
            return{
                new_home_chajian1: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/new_home_chajian1.png',
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jiantou.png',
                isData: [],
                title: 'GO! 分享赚',
                fxBg:''
            }
        },
        created() {
        
            this.queryList( )
             
        },
        methods:{
            setDefaultImage(index){
                this.isData[index].imgurl= "../../static/img/Default_picture.png"
            },
            //查看更多
            _seeMore(url){
                this.$emit('_seeMore', url)
            },
            //查看商品详情
            _seeGoods(id, p_id){
                // this.$emit('_seeGoods', id, p_id)
                this.$store.state.pro_id = id; 
                uni.navigateTo({
                    url: "/pagesC/goods/goodsDetailed?isDistribution=true&toback=true&pro_id=" +
                        p_id +
                        "&fx_id=" +
                        id,
                });
            },
            queryList(limitNum = 3 ){
                let data = {
                	api: "app.index.distribution_list",
                    limit_num:limitNum
                };
                this.$req
                	.post({
                		data,
                	})
                	.then((res) => {
                		try{
                		    if (res.code == 200) {
                		        this.isData = res.data.distribution_list.list;  
                                this.fxBg = res.data.distribution_list.ad_image;
                		    }else{
                		        uni.showToast({
                		            icon:'none',
                		            title:res.message
                		        })
                		    }
                		}catch(err){
                		    this.isData = []
                		    console.error(err)
                		}
                	});
            }
        }
    }
</script>

<style lang="less" scoped> @import url("@/laike.less");
    .isPublicModeFX{
        .content_newThree{
            width: 686rpx;
            height: auto;
            margin: 32rpx auto;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            box-sizing: border-box;
            >view{width: 100%;}
            >view:first-child{
                display: flex;
                flex-direction: row;
                align-items: center;
                justify-content: space-between;
                .content_newThree_tilte1{
                    min-width: 168rpx;
                    font-size: 32rpx;
                    color: #333333;
                    font-weight: bold;
                    >image{width: 100%;height: 100%;}
                }
                .content_newThree_tilte2{
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    
                    font-size: 24rpx;
                    color: #999999;
                    line-height: 32rpx;
                }
            }
            >view:last-child{
                display: flex;
                flex-direction: column;
                justify-content: center;
                align-items: center;
                margin-top: 24rpx;
                .content_newThree_content{
                    width: 100%;
                    display: flex;
                    flex-direction: row;
                    align-items: center;
                    background-color: #ffffff;
                    padding: 16rpx;
                    box-sizing: border-box;
                    border-radius: 16rpx;
                    >view:first-child{
                        width: 176rpx;
                        height: 176rpx;
                        border-radius: 16rpx;
                        overflow: hidden;
                        >image{width: 100%;height: 100%;}
                    }
                    .content_newThree_name{
                        flex: 1;
                        flex-direction: column;
                        justify-content: space-between;
                        margin-left: 24rpx;
                        >view:nth-child(1){
                            font-size: 28rpx;
                            font-weight: 500;
                            color: rgba(0,0,0,0.85);
                            line-height: 40rpx;
                            margin: 8rpx 0;
                        }
                        >view:nth-child(2){
                            //width: 174rpx;
                            //height: 40rpx;
                            display: inline-block !important;
                            padding-right: 12rpx;
                            background: rgba(250,81,81,0.1);
                            border-radius: 4rpx 20rpx 20rpx 4rpx;
                            border-left: 4rpx solid #FA5151;
                            // 字体
                            font-size: 24rpx;
                            font-weight: 500;
                            color: #FA5151;
                            line-height: 40rpx;
                            padding-left: 4rpx;
                            box-sizing: border-box;
                        }
                        .content_newThree_price{
                            display: flex;
                            flex-direction: row;
                            align-items: center;
                            justify-content: space-between;
                            margin-top: 16rpx;
                            >view:first-child{
                                font-size: 24rpx;
                                font-weight: 600;
                                color: #FA5151;
                                line-height: 32rpx;
                                >span:nth-child(1){
                                    font-size: 34rpx;
                                    font-weight: bold;
                                    font-family: DIN-Bold, DIN;
                                    line-height: 42rpx;
                                    margin: 0 10rpx 0 4rpx;
                                }
                                >span:nth-child(2){color: #999999;font-weight: 400;text-decoration: line-through;}
                            }
                            >view:last-child{
                                width: 148rpx;
                                height: 56rpx;
                                background-color: rgba(250, 81, 81, .1);
                                border-radius: 28rpx;
                                font-size: 24rpx;
                                font-weight: 500;
                                color: #FA5151;
                                line-height: 56rpx;
                                text-align: center;
                            }
                        }
                    }
                }
            }
        }
    }
    .package {
        image {
            border-radius: 24rpx;
            width: 686rpx;
            height: 180rpx;
            display: block;
            margin: 0 auto;
            margin-top: 40rpx;
        }
   }
</style>
