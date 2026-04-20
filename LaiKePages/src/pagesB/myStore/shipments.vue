<template>
    <view class="container">
        <heads  :title="title" :bgColor="bgColor" titleColor="#000" :ishead_w="ishead_w"></heads>
        <template>
            <view class="_ul">
                <view class="_li">
                    <text>配送员姓名</text>
                    <view class="_input"><input type="text" :placeholder="placeholderName" v-model="courier_name" placeholder-style="color: #999999;"/></view>
                </view>
                <view class="_li">
                    <text>配送员电话</text>
                    <view class="_input"><input type="text" :placeholder="placeholderPhone" v-model="phone" placeholder-style="color: #999999;"/></view>
                </view>
            </view>
            <view class="btn">
                <div class='bottomBtn' @tap="getLogistics">确认</div>
            </view>
        </template>
        <!-- 提示 -->
        <showToast
            :is_showToast="is_showToast" 
            :is_showToast_obj="is_showToast_obj">
        </showToast>
    </view>
</template>

<script>
import showToast from "@/components/aComponents/showToast.vue"
export default {
    data() {
        return {
            bgColor: [{
                    item: '#fff'
                },
                {
                    item: '#fff'
                }
            ],
            ishead_w:2,
            sNo: '',
            shop_id: '',
            phone: '',
            courier_name: '',
            is_showToast: 0,//
            is_showToast_obj: {},//
            sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
        };
    },
    components:{
        showToast
    },
    watch:{
        courier_num(){
            this.$nextTick(function() {
                this.courier_num = this.courier_num.replace(/[\W]/g, "")//只允许输入数字和字母（如需限制其它类型则换个正则即可）
            })
        }
    },
    computed: {
        title(){
            return '填写配送员信息'
        },
        placeholderName(){
            return '请输入配送员姓名'
        },
        placeholderPhone(){
            return  '请输入配送员电话'
        }
    },
    onLoad(option) {
        this.shop_id = uni.getStorageSync('shop_id') ? uni.getStorageSync('shop_id') : this.$store.state.shop_id
        this.sNo = option.id
    },
    onShow() {},
    methods: {
        getLogistics() {
            let data={
                api:'mch.App.Mch.selfSend',
                shop_id: this.shop_id,
                sNo: this.sNo,
                phone: this.phone,
                courier_name: this.courier_name,
            }
            this.$req.post({data}).then(res => {
                if (res.code == 200) {
                    this.is_showToast = 1
                    this.is_showToast_obj.imgUrl = this.sus
                    this.is_showToast_obj.title = '发货成功'
                    setTimeout(()=>{
                        this.is_showToast = 0
                    },1000) 
                    setTimeout(()=>{
                        uni.navigateBack({delta: 2})
                    }, 1000)
                } else {
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: 'none'
                    });
                }
            });
        },
    }
};
</script>
<style>
page{
    background-color: #f4f5f6;
}
</style>
<style scoped lang="less">
@import url("@/laike.less");
.container {
    min-height: 100vh;
    background-color: #f4f4f4;
    padding-bottom: 98rpx;
    box-sizing: border-box;
    ._ul {
        background-color: #ffffff;
        padding: 32rpx;
        border-radius:0 0 24rpx 24rpx;
        margin-bottom: 24rpx;
    }
    ._ul > ._li:not(:last-child) {
        margin-bottom: 24rpx;
    }
    ._li {
        display: flex;
        align-items: center;
        height: 90rpx;
        
        box-sizing: border-box;
    }
    ._li text {
       font-size: 32rpx;
       
       font-weight: 400;
       color: #333333;
    }
    ._li ._input {
        margin-left: 60rpx;
        display: flex;
        align-items: center;
        justify-content: space-between;
        flex: 1;
        padding: 16rpx;
        font-size: 32rpx;
        
        font-weight: 400;
        color: #333333;
        border-radius: 16rpx;
        border: 1rpx solid rgba(0,0,0,0.1);
    }
    .btn{
        position: fixed;
        bottom: 0;
        width: 100%;
        height: 124rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #ffffff;
        box-shadow: 0 0 0 0 rgba(0,0,0,0.2);
        padding-bottom: constant(safe-area-inset-bottom); /* 兼容 iOS < 11.2 */
        padding-bottom: env(safe-area-inset-bottom);
    }
    .bottomBtn {
        width: 686rpx;
        height: 92rpx;
        font-size: 32rpx;
        color: #fff;
        text-align: center;
        line-height: 92rpx;
        border-radius: 52rpx;
        margin: 0 auto;
        padding:0;
        .solidBtn()
    }
}
</style>
