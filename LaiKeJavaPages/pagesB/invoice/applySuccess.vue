<template>
    <view class="container">
        <view class="position_head">
            <heads :title="language.appluSuccess.title" ishead_w="2" :returnR="7" :bgColor="bgColor" titleColor="#333333"></heads>
        </view>
        <view class="main">
            <image :src="applySuccess" mode="widthFix"></image>
            <p>{{language.appluSuccess.sqcg}}</p>
        </view>
        <view class="return">
            <p @tap="returns">{{language.appluSuccess.fhfpgl}}</p>
        </view>
    </view>
</template>

<script>
    export default {
        data() {
            return {
                title: '开发票',
                applySuccess: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/applySuccess.png',
                bgColor: [
                    {
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
            }
        },
        
        onShow() {
            
        },
        
        onLoad(option) {
            
        },
        
        methods: {
            returns() {
                uni.navigateTo({
                    url: '/pagesB/invoice/invoiceManagement'
                })
            }
        },
    }    
</script>

<style lang="less" scoped> 
    @import url("@/laike.less");
    @import url('../../static/css/invoice/applySuccess.less');
</style>
