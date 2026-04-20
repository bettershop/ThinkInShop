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
        <view v-for="(item, index) in list" :key="index" :style="{borderRadius: index == 0 ? '0 0 24rpx 24rpx':'24rpx'}">
            <view>
                <view>
                    {{item.status == 1||item.status == 3?language.erCode.hxm+'：' + item.write_code : language.erCode.tkhxcs+'：' + item.refund_num}}
                </view>
                <view :style="{color: item.status == 1||item.status == 3?'#666666':'#FA9D3B'}">
                    {{item.status == 1||item.status == 3?language.erCode.yhx:item.status == 4?language.erCode.tkcg :item.status == 5?language.erCode.tksb:language.erCode.tkz}}
                </view>
            </view>
            <view>
                <view>
                    {{item.status == 1||item.status == 3? language.erCode.hxmd + '：' + item.name: language.erCode.tkje + '：' + LaiKeTuiCommon.getPriceWithExchangeRate(item.refund_price,item.exchange_rate)}}
                </view>
                <view class="timexn">
                    {{item.write_time}}
                </view>
            </view>
        </view>
    </view>
</template>

<script>
export default {
    data() {
        return{
            title: "",
            titleColor: "#333333", //标题字体颜色
            fanhui: 2,
            headBg: "#ffffff",
            orderDetailId: '',//id
            list: [],
        }
    },
    onLoad(option) {
        this.orderDetailId = option.orderDetailId
    },
    onShow() {
        this._axios()
    },
    methods: {
        _axios(){
            let data = {
                api: 'app.order.get_write_record',
                id: this.orderDetailId
            } 
            this.$req.post({data}).then(res=>{ 
                if(res.code == 200){
                    this.list = res.data.list
                    this.title = this.language.erCode.hxjl + '(' + res.data.num + ')'
                } else {
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
                font-size: 32rpx;
                color: #333333;
                display: flex;
                justify-content: space-between;
                margin-bottom: 24rpx;
            }
            >view:nth-child(2){
                font-size: 28rpx;
                color: #666666;
                display: flex;
                justify-content: space-between;
                .timexn{
                    font-size: 24rpx;
                    color: #999999;
                }
            }
        }
    }
</style>
