<template>
    <view>
        <heads :title="language.myStore.dzmd" :border="true" :bgColor='bgColor' titleColor="#000" ishead_w="2" returnR="18"></heads>
        <view class="titlebox" @click="navito(0)">
            <view class="titlebox-title">{{language.myStore.dzmd}}</view>
            <view class="titlebox-img">
                <image class="titlebox-img-img" :src="fanghui"></image>
            </view>
        </view>
        <view class="titlebox" @click="navito(1)">
            <view class="titlebox-title">{{language.myStore.express.mdsz}}</view>
            <view class="titlebox-img">
                <image class="titlebox-img-img" :src="fanghui"></image>
            </view>
        </view>
    </view>
</template>

<script>
    export default {
        data() {
            return {
                bgColor: [{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                fanghui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/member/fanghui.png',
                status:0,
            }
        },
        
        methods: {
            navito(e){
                if(e==0){
                    uni.navigateTo({
                        url:'./expressRecord'
                    })
                }else{
                    uni.navigateTo({
                        url:'./expressSet'
                    })
                }
            },

        }
    }
</script>
<style>
	    page {
	        background-color: #f4f5f6;
	    }
</style>
<style scoped lang="less">
    @import url("@/laike.less");
    .titlebox {
        width: 100%;
        border-radius: 16rpx;
        margin-top: 24rpx;
        background-color: #fff;
        height: 108rpx;
        display: flex;
        justify-content: space-between;
        align-items: center;
        color: #333333;
    }

    .titlebox-title {
        font-size: 32rpx;
        margin-left: 32rpx;
    }

    .titlebox-img {
        width: 10rpx;
        height: 100%;
        margin-right: 20rpx;
        display: flex;
        justify-content: center;
        align-items: center;
    }

    .titlebox-img-img {
        display: flex;
        width: 10rpx;
        height: 20rpx;
    }
</style>
