<template>
    <view class="container" >
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="title" :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
        <view class="page_top" v-if='aggregate>0'>
            <view class="page_top_txt">
                <image :src="icon_Chart" style="margin-left:28rpx;margin-right:20rpx;width: 36rpx;height: 36rpx;"></image>
                <view class="page_top_title">{{language.couponrecord.zjsy}}{{' '}}<span style="font-weight: bold;">{{aggregate}}</span>{{' '}}{{language.storeCoupon.zhang}}</view>
                
            </view>
        </view>
        <view class="list" v-for="item,index of list" :key="index">
            <view>
                <view>{{language.couponrecord.yh}}：{{item.userName}}</view>
                <text>领取<text style="color: #fa5151;">{{item.receiveNum}}</text>张，使用<text style="color: #fa5151;">{{item.useNum}}</text>张</text>
                <text>{{language.couponrecord.sysj}}：{{item.receiveDate}}</text>
                <view class="fenbian_box">
                    <text>{{language.couponrecord.gldd}}：{{item.orderNo}}</text>
                    <text style="color: #fa5151;" @click="copy(item.orderNo)">{{language.couponrecord.fz}}</text>
                </view>
                </text>
            </view>
        </view>
        
       <view v-if="noRecord && list.length == 0" class="noRecord">
            <image :src="noRecord"></image>
            <text>{{language.couponrecord.zwsj}}~</text>
        </view>
    </view>
</template>

<script>
export default {
    data() {
        return {
            title: '使用记录',
            bgColor:[{
                    item: '#ffffff'
                },
                {
                    item: '#ffffff'
                }
            ],
            id: '',
            page: 1,
            aggregate:'',//总计
            icon_Chart: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/icon-Chart.png',
            list: [],
            noRecord: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/coupon_qsy.png',
            isLoad: false
        };
    },
    onLoad(option) {
        this.id = option.id
    },
    onShow() {
        this.isLogin(()=>{})
        
        this.page = 1
        this.axios()
    },
    methods: {
		changeLoginStatus(){
			this.page = 1
			this.axios()
		},
        copy(e){
            uni.setClipboardData({
            	data: e,
            	success: function () {
            	}
            });
        },
        axios(){
            let data = {
                
                api:"plugin.coupon.AppMchcoupon.MchUseRecord",
                mch_id: uni.getStorageSync('shop_id'), // 店铺id
                id: this.id,
                page: this.page
            }
            
            this.$req.post({data}).then(res=>{
                this.isLoad = true
                this.aggregate = res.data.total
                if(this.page == 1){
                    this.list = []
                }
                this.list.push(...res.data.resultList)
            })
            
        },
    },
};
</script>
<style>
    page{
        background-color: #f4f5f6;
    }
</style>
<style lang="less">
@import url("@/laike.less");
    .fenbian_box{
        width: 100%;
        display: flex;
        justify-content: space-between;
    }
.page_top{
    background-color: #fff;
    width: 100%;
    height: 156rpx;
    display: flex;
    align-items: center;
    margin-bottom: 16rpx;
    border-radius: 0 0 24rpx 24rpx;
}
.page_top_txt{
    display: flex;
    width: 686rpx;
    border-radius: 16rpx;
    height: 92rpx;
    display: flex;
    align-items: center;
    margin: 32rpx;
    background-color: #feeded;
}
.page_top_title{
     
    font-weight: 300;
    color: #FA5151;
    font-size: 32rpx;
}
.container{
    min-height: 100vh;
    background-color: #F5F5F5;
    
    .list{
        display: flex;
        align-items: center;
        height: 276rpx;
        background-color: #FFFFFF;
        border-radius: 24rpx;
        margin-top: 8rpx;
        
        >view{
            display: flex;
            flex-direction: column;
            padding: 32rpx;
            width: 100%;
            view{
                font-size: 32rpx;
                
                font-weight: 400;
                color: #333333;
                margin-bottom: 8rpx;
            }
            
            text{
                font-size: 28rpx;
                
                font-weight: 400;
                color: #999999;
                margin-top: 16rpx;
            }
        }
        
        >text{
            font-size: 28rpx;
            
            font-weight: 400;
            color: #999999;
            margin-left: auto;
            margin-right: 30rpx;
            span{
                color: #FA5151;
            }
        }
    }

    .list:not(:last-child){
        border-bottom: 1px solid #E5E5E5;
    }

}


.noRecord{
    display: flex;
    flex-direction: column;
    align-items: center;
    padding-top: 200rpx;
    
    image{
        width: 750rpx;
        height: 388rpx;
        margin-bottom: 40rpx;
    }
    
    text{
        font-size: 28rpx;
        color: #333333;
    }
    
}

</style>
