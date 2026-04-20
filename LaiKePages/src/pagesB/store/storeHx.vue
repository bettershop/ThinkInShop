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
        <view v-for="(item, index) in mchStoreList" :key="index" :style="{borderRadius: index == 0 ? '0 0 24rpx 24rpx':'24rpx'}">
            <view>{{item.name}}</view>
            <view>
                <view>联系电话：{{item.mobile}}</view>
                <view>营业时间：{{item.business_hours}}</view>
            </view>
            <view>{{item.sheng}} {{item.shi}} {{item.xian}} {{item.address}}</view>
        </view>
    </view>
</template>

<script>
export default {
    data() {
        return{
            title: "核销门店",
            titleColor: "#333333", //标题字体颜色
            fanhui: 2,
            headBg: "#ffffff",
            mchStoreList: [],//虚拟商品-需要核销-核销门店信息
        }
    },
    onLoad(option) {
        this._axios(option.shop_id, option.pro_id)
    },
    onShow() {},
    methods: {
        _axios(shop_id, pro_id){
            let data = {
                api:"mch.App.Mch.See_my_store",
                shop_id,
                pro_id,
            }
            this.$req.post({ data }).then(res => {
                if (res.code == 200) {
                    this.mchStoreList = res.data.list
                } else {
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: 'none'
                    });
                }
            });
        }
    },
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
            >view:nth-child(1){
                font-size: 40rpx;
                color: #333333;
                margin-bottom: 24rpx;
            }
            >view:nth-child(2){
                font-size: 28rpx;
                color: #999999;
                display: flex;
                justify-content: space-between;
                margin-bottom: 24rpx;
            }
            >view:nth-child(3){
                font-size: 28rpx;
                color: #999999;
            }
        }
    }
</style>
