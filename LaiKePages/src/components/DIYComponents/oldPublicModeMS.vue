<template>
    <view class="isPublicModeMS">
        <template v-if="secList && secList.length">
            <view class="content_newFive">
                <view>
                    <view class="content_newFive_tilte1">
                        {{secTitle}}
                    </view>
                    <view class="content_newFive_tilte2" @tap="_seeMore('/pagesB/seckill/seckill')">
                        {{language.home.more}}
                        <image :src="jiantou" lazy-load="true" style="width: 32rpx;height: 44rpx;"></image>
                    </view>
                </view>
                <view>
                    <view  
                        class="content_newFive_content" 
                        :style="index==1?'margin-top: 16rpx;':''"
                        v-for="(item, index) of secList" :key="index"
                        v-if="index < 2"
                        @tap="_seeGoods(item)">
                            <view>
                                <image :src="LaiKeTuiCommon.getFastImg(item.imgUrl, 192, 192)" mode="scaleToFill"
                                    lazy-load="true" @error="setDefaultImage(index)" style="width: 96px;height:96px;"></image>
                            </view>
                            <view class="content_newFive_name">
                                <view class="overflowHide">{{item.goodsName}}</view>
                                <view>
                                    <view>{{language.seckill.seckill.Robbed}}{{item.planBar}}%</view>
                                    <view :style="'width:'+ item.planBar +'%'"></view>
                                </view>
                                <view class="content_newFive_price">
                                    <view>
                                        {{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}<span>{{LaiKeTuiCommon.formatPrice(item.secPrice)}}</span><span>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(item.goodsPrice)}}</span>
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
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jiantou.png',
                new_home_chajian3: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/new_home_chajian3.png',
                secList: [], //限时秒杀：商品列表
                secTitle:''
            }
        },
        created() {
            uni.$on('update',(data)=>{
                // this.isData = []
                this.language.seckill.seckill.Panic[3] = this.language.seckill.seckill.Panic[3]
                this.language.seckill.seckill.Robbed = this.language.seckill.seckill.Robbed
            })
            this.getSeckill()
        },
        methods:{
            setDefaultImage(index){
                this.secList[index].imgUrl= "../../static/img/Default_picture.png"
            },
            //限时秒杀：秒杀商品
            getSeckill() {
            	let data = {
            		api: 'plugin.sec.AppSec.goodsList',
            		pageNo: 1,
            		pageSize: 3,
            	};
            	this.$req.post({
            		data
            	}).then((res) => {
            		let {
            			data
            		} = res;
            		if (res.code == 200) {
            			this.secList = res.data.list;
            			this.secTitle = res.data.title;
            		}
            	});
            }, 
            //查看更多
            _seeMore(url){
                this.isLogin(() => {
                	uni.navigateTo({
                		url: url,
                	});
                });
            },
            //查看商品详情
            _seeGoods(item){ 
                let types = null;
                if (item.secStatus == 2) {
                	types = 1;
                } else if (item.secStatus == 4) {
                	types = 2;
                } else if (item.secStatus == 5) {
                	types = 3;
                } else {
                	types = 0;
                }
                uni.navigateTo({
                	url: "/pagesB/seckill/seckill_detail?pro_id=" +
                		item.goodsId +
                		"&navType=" +
                		types +
                		"&id=" +
                		item.secGoodsId +
                		"&type=MS",
                });
            },
        }
    }
</script>

<style lang="less" scoped> @import url("@/laike.less");
    .isPublicModeMS{
        .content_newFive{
            width: 686rpx;
            height: auto;
            margin: 32rpx auto;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            //padding: 24rpx;
            box-sizing: border-box;
            >view{width: 100%;}
            >view:first-child{
                display: flex;
                flex-direction: row;
                align-items: center;
                justify-content: space-between;
                .content_newFive_tilte1{
                    min-width: 168rpx;
                    //height: 32rpx;
                    font-size: 32rpx;
                    color: #333333;
                    font-weight: bold;
                    //font-style: oblique;
                    >image{width: 100%;height: 100%;}
                }
                .content_newFive_tilte2{
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
                //>view:first-child{margin-bottom: 16rpx;}
                .content_newFive_content{
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
                    .content_newFive_name{
                        flex: 1;
                        display: flex;
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
                            width: 212rpx;
                            height: 40rpx;
                            border-radius: 8rpx;
                            overflow: hidden;
                            border: 1rpx solid #FA5151;
                            position: relative;
                            >view:nth-child(1){
                                width: 212rpx;
                                height: 40rpx;
                                line-height: 40rpx;
                                text-align: center;
                                font-size: 24rpx;
                                font-weight: 400;
                                color: #FA5151;
                                position: absolute;
                                z-index: 1;
                            }
                            >view:nth-child(2){
                                height: 100%;
                                background-color: rgba(250, 81, 81, .2);
                            }
                        }
                        .content_newFive_price{
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
</style>
