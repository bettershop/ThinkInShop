<template>
    <view>
        <view class="pages">
            <heads :title="language.yushou_settlement.title" :border="true" :bgColor='bgColor' titleColor="#000" ishead_w="2"></heads>
            <!-- 搜索 -->
            <search :is_search_prompt="language.yushou_settlement.ss_ts" :is_search_type="1" @search="_search"></search>
            <!-- 导航切换 -->
            <switchNavOne :is_switchNav="header" :is_switchNav_padT="'0'" :is_switchNav_padB="'24rpx'" @choose="_choose"></switchNavOne>
            <!-- 占位 -->
            <!-- <view :style="{height:is_top2}"></view> -->
            <!-- 无数据显示 -->
            <div v-if="isData.length == 0" style="height: 100vh;width: 100%;display: flex;align-items: center;">
                <div class="noFindDiv" style="width: 100%;padding-top: 178rpx;height: 100%;">
                    <div><img class="noFindImg" :src="noOrder" /></div>
                    <span class="noFindText">{{language.order.myorder.no_order}}</span>
                </div>
            </div>
            <!-- 有数据显示 -->
            <view v-else class="pages_box">
                <view class="pages_box_box" v-for="(item,index) in isData" :key="item.id">
                    <view class="pages_box_box_top">
                        <view class="pages_box_box_top_zt">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{Number(item.z_price ||0).toFixed(2)}}</view>
                        <view v-if="status_id == 0" class="pages_box_box_top_dd">{{item.arrive_time}}</view>
                    </view>
                    <view class="pages_box_box_body">
                        <view class="pages_box_box_body_right">
                            <view class="pages_box_box_body_right_title">{{language.yushou_settlement.bh}}：{{item.sNo}}</view>
                            <view class="pages_box_box_body_right_title">{{language.yushou_settlement.je}}：{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{Number(item.z_price ||0).toFixed(2)}}</view>
                            <view class="pages_box_box_body_right_title">{{language.yushou_settlement.td}}：{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ Number(item.reduce_price ||0).toFixed(2)}}</view>
                            <view class="pages_box_box_body_right_title">{{language.yushou_settlement.sc}}：{{item.add_time}}</view>
                        </view>
                    </view>
                </view>
            </view>
            
        </view>
    </view>
</template>

<script>
    import search from '@/components/aComponents/search.vue'
    import switchNavOne from '@/components/aComponents/switchNavOne.vue'
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
                is_tishi:false,
                is_sus:false,
                status: 1,
                status_id: 0, //订单状态
                page: 1,
                isData: [],
                //header: ['已结算', '待结算'],
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
                noOrder: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/noOrder.png',
            }
        },
        onLoad(option) {
            this._axios()
        },
        computed:{
            header(){
                let new_data = []
                new_data[0] = this.language.yushou_settlement.yjs
                new_data[1] = this.language.yushou_settlement.djs
                return new_data
            }
        },
        components:{
            switchNavOne,
            search
        },
        methods: {
            towuliu(){
                uni.navigateTo({
                    url:'/pagesC/expressage/expressage'
                })
            },
            to_detal(){
                uni.navigateTo({
                    url:'/pagesC/pointsDetails/Points_details'
                })
            },
            //搜索
            _search(item){
                this.search = item
                this._axios()
            },
            //状态
            _choose(index){
                this.status_id = index
                if(index == 0){
                    this.status = 1
                } else if(index == 1){
                    this.status = 0
                }
                this._axios()
            },
            _quxiao(){
                this.is_tishi = false
            },
            _queren(){
                this.is_sus= true
                this.is_tishi = false
                setTimeout(()=>{
                    this.is_sus = false
                },1500)
            },
            toAddPro(){
                this.is_tishi = true
            },
            /**
             * 预售订单结算 数据初始化
             * @param null
             */
            _axios(){
                let data = {
                    //api: 'app.mchPreSell.orderSettlement',
                    api: 'plugin.presell.AppPreSell.orderSettlement',
                    status: this.status,
                    orderType: 'PS',
                    pageNo: this.page,
                    pageSize: 999,
                }
                if(this.search){
                    data.search = this.search
                }
                this.$req.post({data}).then(res=>{
                    if(res.code == 200){
                        this.isData = res.data.list
                    } else {
                    }
                })
            },
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
    // .pages_box{
    //     animation: anShowToast5 1s both;
    // }
    //右滑 显示动画
    @keyframes anShowToast5 {
       0% {
       opacity: 0;
       margin-left: -100%;
       }
           
       100% {
       opacity: 1;
       margin-left: 0;
       }
    }
    .btn{
        width: 144rpx;
        height: 56rpx;
        line-height: 56rpx;
        text-align: center;
        border-radius: 52rpx;
        font-size: 24rpx;
        
        font-weight: normal;
        color: #333333;
        background: #F4F5F6;
        margin-left: 24rpx;
    }
    .pages_box_box_jw_btn{
        display: flex;
    }
    .pages_box_box_jw_sj{
        font-size: 28rpx;
         
        font-weight: normal;
        color: #999999;
    }
    .pages_box_box_jw{
        margin-top: 26rpx;
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
    .pages_box_box_body_right_price_num{
        font-size: 24rpx;
         
        font-weight: normal;
        color: #999999;
    }
    .pages_box_box_body_right_price_jf{
        font-size: 32rpx;
        
        font-weight: normal;
        color: #FA5151;
    }
    .pages_box_box_body_right_price{
        margin-top: 24rpx;
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
    .pages_box_box_body_rigth_gg{
        font-size: 24rpx;
         
        font-weight: normal;
        color: #999999;
        margin-top: 20rpx;
    }
    .pages_box_box_body_right_title {
        font-size: 28rpx;
        color: #666666;
        margin-bottom: 16rpx;
    }

    .pages_box_box_body_right {
        width: 100%;
        display: flex;
        flex-direction: column;
        >view:last-child{
            margin-bottom: 0;
        }
    }

    .pages_box_box_left {
        width: 164rpx;
        height: 164rpx;
    }

    .pages_box_box_body {
        display: flex;
        padding: 32rpx;
        box-sizing: border-box;
    }

    .pages_box_box_top_zt {
        font-size: 32rpx;
        font-weight: 500;
        color: #FA5151;
    }

    .pages_box_box_top_dd {
        font-size: 28rpx;
        color: #666666;
        line-height: 32rpx;
        transition: all .5s;
    }

    .pages_box_box_top {
        width: 100%;
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 32rpx;
        box-sizing: border-box;
        border-bottom: 2rpx solid rgba(0,0,0,.1);
    }

    .pages_box_box {
        display: flex;
        flex-direction: column;
        background-color: #fff;
        border-radius: 24rpx;
        margin-bottom: 24rpx;
    }

    .pages_box {
        margin-top: 24rpx;
        width: 100%;
        display: flex;
        flex-direction: column;
    }

    .noFindDiv {
        width: 100%;
        padding-top: 430rpx;
        height: 100%;
        background-color: #F4F5F6;

        .noFindText {
            color: #888888;
        }

        .noFindImg {
            width: 750rpx;
            height: 394rpx;
        }
    }
    
    .pages {
        width: 100%;
    }
</style>
