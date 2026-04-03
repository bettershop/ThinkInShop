<template>
	<view>
        <heads :title="language.myStore.QRfailed.title" :bgColor="bgColor" titleColor="#333333"  ishead_w="2"></heads>
        <div class="QR-card">
            <view class="orderInfo">
               <image :src="QRcode_fail" style=" width: 68rpx;height: 68rpx;"></image>
               <view>{{language.myStore.QRfailed.sb}}</view>
            </view>
           <view class="myOrder button" @tap="rescanCode">{{language.myStore.QRfailed.cxsm}}</view>
           <view class="button" @tap="returnHome">{{language.myStore.QRfailed.btn}}</view>
        </div>
	</view>
</template>

<script>
	export default {
		data() {
			return {
                QRcode_fail: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/QRcode_fail.png',
				bgColor: [{
				        item: '#ffffff'
				    },
				    {
				        item: '#ffffff'
				    }
				],
			};
		},
        methods: {
            // 重新扫码
            rescanCode() {
               this.navBack()
            },
            // 返回首页
            returnHome() {
               this.navToHome()
            }
        }
	}
</script>
<style>
    page {
        background-color: #f4f5f6;
    }
</style>
<style lang="less" scoped>
@import url("@/laike.less");
@import url('../../static/css/myStore/QRfailed.less');
</style>
