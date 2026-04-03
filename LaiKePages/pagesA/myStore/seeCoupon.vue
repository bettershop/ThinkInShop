<template>
    <view class="container">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="language.seeCoupon.title" :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
        <view class="page_top"  v-if="list.length != 0" >
            <view class="page_top_txt">
                <image :src="icon_Chart" style="margin-left:28rpx;margin-right:20rpx;width: 36rpx;height: 36rpx;"></image>
                <view class="page_top_title">{{language.seeCoupon.zj}}{{' '}}<span style="font-weight: bold;">{{manNum}}</span>{{' '}}{{language.seeCoupon.rlqz}}{{' '}}<span style="font-weight: bold;">{{total}}</span>{{' '}}{{language.seeCoupon.Zhang}}</view>
                
            </view>
        </view>
        <view class="list" v-for="item,index of list" :key="index">
            <view>
                <view>{{language.seeCoupon.yh}}：{{item.user_name}}</view>
                <text>{{language.seeCoupon.lqsj}}：{{item.add_time}}</text>
            </view>
            <text>{{language.seeCoupon.receive}}<text style="color: #fa5151;">{{item.receive}}</text>{{language.seeCoupon.Zhang}}</text>
        </view>
       <view
            v-if='is_show'
           style="
               width: 100%;
               color: #fff;
               text-align: center;
               margin-top: 48rpx;
               font-size: 24rpx;
           ">
           {{ language.loadMore.contentText.contentnomore }}
       </view>
        <view v-if="noRecord && list.length == 0" class="noRecord">
            <image :src="noRecord"></image>
            <text>{{language.seeCoupon.noRecord}}</text>
        </view>
    </view>
</template>

<script>
export default {
    data() {
        return {
            title: '领取记录',
            bgColor:[{
                    item: '#ffffff'
                },
                {
                    item: '#ffffff'
                }
            ],
            is_show:false,
            id: '',
            page: 1,
            list: [],
            noRecord: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/coupon_qsy.png',
            icon_Chart: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/icon-Chart.png',
            isLoad: false,
            manNum:0,
            total:0,
        };
    },
    onLoad(option) {
        this.id = option.id
    },
    onShow() {
        this.isLogin(()=>{})
        this.title=this.language.seeCoupon.title
        this.page = 1
        this.axios()
    },
    onReachBottom(){
        if(this.is_show) return
        this.page ++ 
        this.axios()
    },
    methods: {
		changeLoginStatus(){
            
			this.page = 1
			this.axios()
		},
        axios(){
            let data = {
               
                api:"plugin.coupon.AppMchcoupon.SeeCoupon",
                mch_id: uni.getStorageSync('shop_id'), // 店铺id
                id: this.id,
                page: this.page
            }
            
            this.$req.post({data}).then(res=>{
                this.isLoad = true
                this.total = res.data.total
                this.manNum = res.data.Number_of_recipients
                if(this.page == 1){
                    this.list = []
                }
                if(res.data.list && res.data.list.length == 0){
                    this.is_show = true
                    
                }
                this.list.push(...res.data.list)
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
@import url('../../static/css/myStore/seeCoupon.less');
</style>
