<template>
    <view class="content">
        <!-- 头部 -->
        <heads
            :title="title"
            :titleColor="titleColor"
            :bgImg="headBg"
            :ishead_w="fanhui"
        ></heads>
        <!-- 列表 -->
        <view v-for="(item, index) in 4" :key="index" :style="{borderRadius: index == 0 ? '0 0 24rpx 24rpx':'24rpx'}">
            <view>
                <view>核销码：154254</view>
                <view>已核销</view>
            </view>
            <view>
                <view>核销门店：绿地中央广场店</view>
                <view>2024-05-10 12:00:15</view>
            </view>
        </view>
    </view>
</template>

<script>
export default {
    data() {
        return{
            title: "核销记录",
            titleColor: "#333333", //标题字体颜色
            fanhui: 2,
            headBg: "#ffffff",
        }
    },
    onLoad(option) {},
    onShow() {},
    methods: {},
};
</script>
<style>
    page{
        background-color: #f4f5f6;
    }
</style>
<style lang="less" scoped> 
    .content{
        >view{
            display: flex;
            flex-direction: column;
            justify-content: center;
            background-color: #ffffff;
            padding: 32rpx;
            box-sizing: border-box;
            margin-bottom: 16rpx;
            >view{
                display: flex;
                justify-content: space-between;
            }
            >view:first-child{
                margin-bottom: 24rpx;
                >view:nth-child(1){
                    font-size: 32rpx;
                    color: #333333;
                }
                >view:nth-child(2){
                    font-size: 32rpx;
                    color: #666666;
                }
            }
            >view:last-child{
                >view:nth-child(1){
                    font-size: 28rpx;
                    color: #666666;
                }
                >view:nth-child(2){
                    font-size: 24rpx;
                    color: #999999;
                }
            }
        }
    }
</style>
