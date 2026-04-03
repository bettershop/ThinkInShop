<template>
    <view class="isPublicModeJF">
        <!-- 如果有积分商品；则用这种布局 -->
        <template v-if="isData && isData.list">
            <view class="content_newOne" :style="'background-image: url('+ new_home_bgm1 +')'">
                <!-- 积分 -->
                <view @click="_seeMoreJF('/pagesB/integral/integral?toBack=true&needLogin=1')">
                    <view class="content_newOne_oneTitle">
                        <view>{{language.proList.jfsc}}</view>
                        <view>{{language.proList.jfspr}}</view>
                        <view></view>
                    </view>
                    <view class="content_newOne_oneContent">
                        <view>
                            <image 
                                :src="LaiKeTuiCommon.getFastImg(isData.list[0] && isData.list[0].imgurl ? isData.list[0].imgurl : JFimg01, 340, 340)"
                                mode="aspectFill"
                                lazy-load="true">
                            </image>
                        </view>
                        <view class="content_newOne_img">
                            <view>
                                <image 
                                    :src="LaiKeTuiCommon.getFastImg(isData.list[1] && isData.list[1].imgurl ? isData.list[1].imgurl : JFimg02, 170, 170)"
                                    mode="aspectFill"
                                    lazy-load="true">
                                </image>
                            </view>
                            <view>
                                <image 
                                    :src="LaiKeTuiCommon.getFastImg(isData.list[2] && isData.list[2].imgurl ? isData.list[2].imgurl : JFimg02, 170, 170)"
                                    mode="aspectFill"
                                    lazy-load="true">
                                </image>
                            </view>
                        </view>
                    </view>
                </view>
                <!-- 新品/优选 -->
                <view>
                    <view @tap="_seeMore('/pagesB/home/proList?type=1', true)">
                        <view class="content_newOne_twoTitle">
                            <view>{{language.proList.title1}}</view>
                            <view>{{language.proList.hwsx}}</view>
                        </view>
                        <view class="content_newOne_img">
                            <view>
                                <image :src="LaiKeTuiCommon.getFastImg(isDataXP[0]?isDataXP[0]:XPimg01, 170, 170)" mode="aspectFill" lazy-load="true"></image>
                            </view>
                            <view>
                                <image :src="LaiKeTuiCommon.getFastImg(isDataXP[1]?isDataXP[1]:XPimg01, 170, 170)" mode="aspectFill" lazy-load="true"></image>
                            </view>
                        </view>
                    </view>
                    <view @tap="_seeMore('/pagesB/home/proList?type=2', true)">
                        <view class="content_newOne_twoTitle">
                            <view>{{language.proList.title2}}</view>
                            <view>{{language.proList.hwsx1}}</view>
                        </view>
                        <view class="content_newOne_img">
                            <view>
                                <image :src="LaiKeTuiCommon.getFastImg(isDataHW[0]?isDataHW[0]:HWimg01, 170, 170)" mode="aspectFill" lazy-load="true"></image>
                            </view>
                            <view>
                                <image :src="LaiKeTuiCommon.getFastImg(isDataHW[1]?isDataHW[1]:HWimg01, 170, 170)" mode="aspectFill" lazy-load="true"></image>
                            </view>
                        </view>
                    </view>
                </view>
            </view>
        </template>
        <!-- 如果没有积分商品；则用这种布局 -->
        <template v-else>
            <view class="content_top content_top_two" :style="'background-image: url('+ new_home_bgm2 +')'">
                <!-- 新品上市 -->
                <view class="content_top-box" @tap="_seeMore('/pagesB/home/proList?type=1', true)">
                    <view class="content_top-box-top">
                        <view class="content_top-box-top-title">{{language.proList.title1}}</view>
                        <view class="content_top-box-top-txt">{{language.proList.hwsx}}</view>
                   </view>
                    <view class="content_top-box-bottom">
                        <image 
                            :src="LaiKeTuiCommon.getFastImg(isDataXP[0]?isDataXP[0]:XPimg01, 170, 170)" 
                            mode="aspectFill"
                            lazy-load="true"
                            class="content_top-box-bottom-img">
                        </image>
                        <image 
                            :src="LaiKeTuiCommon.getFastImg(isDataXP[1]?isDataXP[1]:XPimg01, 170, 170)" 
                            mode="aspectFill"
                            lazy-load="true"
                            class="content_top-box-bottom-img">
                        </image>
                    </view>
                </view>
                <!-- 好物优选 -->
                <view class="content_top-box"  @tap="_seeMore('/pagesB/home/proList?type=2', true)">
                    <view class="content_top-box-top">
                        <view class="content_top-box-top-title">{{language.proList.title2}}</view>
                        <view class="content_top-box-top-txt">{{language.proList.hwsx1}}</view>
                   </view>
                    <view class="content_top-box-bottom">
                        <image 
                            :src="LaiKeTuiCommon.getFastImg(isDataHW[0]?isDataHW[0]:HWimg01, 170, 170)" 
                            mode="aspectFill"
                            lazy-load="true"
                            class="content_top-box-bottom-img">
                        </image>
                        <image 
                            :src="LaiKeTuiCommon.getFastImg(isDataHW[1]?isDataHW[1]:HWimg01, 170, 170)" 
                            mode="aspectFill"
                            lazy-load="true"
                            class="content_top-box-bottom-img">
                        </image>
                    </view>
                </view>
            </view>
        </template>
    </view>
</template>

<script>
    export default{
        props:{
            "isJF":{
                type: Number,
                default:0,
            },
            //积分 数据
            "isData": {
                type: Object,
                default: {}, 
            },
            //新品 数据
            "isDataXP": {
                type: Array,
                default: [], 
            },
            //好物 数据
            "isDataHW": {
                type: Array,
                default: [], 
            },
            // --- 分割线 ---
        },
        data(){
            return{
                //背景图
                new_home_bgm1: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/new_home_bgm1.png',
                new_home_bgm2: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/new_home_bgm2.png',
                //默认图
                JFimg01: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/JFimg01.png',
                JFimg02: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/JFimg02.png',
                XPimg01: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/XPimg01.png',
                HWimg01: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/HWimg01.png',
            }
        },
        methods:{
            //查看更多（好物新品）
            _seeMore(url, status){
                this.$emit('_seeMore', {url:url, status:status})
            },
            //查看更多（积分商城）
            _seeMoreJF(url){
                this.$emit('_seeMoreJF', url)
            },
        }
    }
</script>

<style lang="less" scoped> @import url("@/laike.less");
    .isPublicModeJF{
        .content_newOne{
            width: 686rpx;
            height: auto;
            background-color: #ffffff;
            border-radius: 24rpx;
            margin: 32rpx auto;
            margin-top: 0;
            display: flex;
            flex-direction: row;
            // align-items: center;
            justify-content: space-around;
            background-size: cover;
            padding: 24rpx 0;
            box-sizing: border-box;
            >view{
                flex: 1;
                padding: 0 24rpx;
                box-sizing: border-box;
            }
            >view:first-child{
                border-right: 1px solid #F4F5F6;
                .content_newOne_oneTitle{
                    display: flex;
                    flex-direction: column;
                    >view:nth-child(1){font-size: 32rpx;font-weight: 500;color: #333333;}
                    >view:nth-child(2){font-size: 20rpx;color: #999999;margin: 8rpx 0 16rpx 0;}
                    >view:nth-child(3){width: 48rpx;height: 8rpx;background-color: #ffc300;border-radius: 6rpx;}
                }
                .content_newOne_oneContent{
                    display: flex;
                    flex-direction: column;
                    margin-top: 40rpx;
                    >view:nth-child(1){
                        width: 100%;
                        height: 170rpx;
                        border-radius: 16rpx;
                        overflow: hidden;
                        >image{width: 100%;height: 100%;}
                    }
                    .content_newOne_img{
                        width: 100%;
                        display: flex;
                        flex-direction: row;
                        align-items: center;
                        justify-content: space-between;
                        margin-top: 12rpx;
                        >view{width: 140rpx;height: 140rpx;border-radius: 16rpx;overflow: hidden;>image{width: 100%;height: 100%;}}
                    }
                }
            }
            >view:last-child{
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                >view{
                    flex: 1;
                    .content_newOne_twoTitle{
                        display: flex;
                        flex-direction: row;
                        >view:nth-child(1){font-size: 32rpx;font-weight: 500;color: #333333;}
                        >view:nth-child(2){font-size: 20rpx;color: #999999;margin-left: 8rpx;line-height: 44rpx;}
                    }
                    .content_newOne_img{
                        width: 100%;
                        display: flex;
                        flex-direction: row;
                        align-items: center;
                        justify-content: space-between;
                        margin-top: 16rpx;
                        >view{width: 140rpx;height: 140rpx;border-radius: 16rpx;overflow: hidden;>image{width: 100%;height: 100%;}}
                        >view:last-child{margin-left: 18rpx;}
                    }
                }
                >view:first-child{padding-bottom: 34rpx}
                >view:last-child{padding-top: 34rpx;border-top: 1px solid #F4F5F6;}
            }
        }
        .content_top_two{
            margin-bottom: 38rpx;
            background-color: #ffffff;
            padding: 24rpx 0;
            box-sizing: border-box;
            background-size: cover;
            border-radius: 24rpx;
            >view:first-child{
                border-right: 1px solid #F4F5F6;
            }
            .content_top-box{
                background-color: initial;
                width: 316rpx;
                height: auto;
                margin: 0;
                padding: 0 24rpx;
                border: initial;
                border-radius: initial;
                display: flex;
                flex-direction: column;
                .content_top-box-top{
                    height: auto;
                    line-height: initial;
                    align-items: center;
                    display: flex;
                    .content_top-box-top-title{
                        margin-left: 0;
                        font-size: 32rpx;
                        font-weight: 500;
                        color: #333333;
                    }
                    .content_top-box-top-txt{
                        font-size: 20rpx;
                        color: #999999;
                        margin-left: 8rpx;
                    }
                }
                .content_top-box-bottom{
                    margin-top: 16rpx;
                    .content_top-box-bottom-img{
                        width: 136rpx;
                        height: 136rpx;
                        margin-left: 0;
                        border-radius: 16rpx;
                    }
                    >image:first-child{
                        margin-right: 12rpx;
                    }
                }
            }
        }
        .content_top {
            display: flex;
            justify-content: space-between;
            margin: 0 30rpx;
        }
    
    }
</style>
