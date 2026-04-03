<template>
    <view>
        <view class="pages">
            <!-- 头部 -->
            <heads :title="language.yushou_order.title" :border="true" :bgColor='bgColor' titleColor="#000" ishead_w="2"></heads>
            <!-- 搜索 -->
            <search :is_search_prompt="language.yushou_settlement.ss_ts" :is_search_type="1" @search="_search"></search>
            <!-- 退货数量显示 -->
            <view class="numb" v-if="status_id == 3">{{numb}}</view>
            <!-- 导航 -->
            <switchNavOne :is_switchNav="header" :is_switchNav_radius="status_id==3?'0 0 0 0':'0 0 24rpx 24rpx'" :is_switchNav_padT="'0'" :is_switchNav_padB="'24rpx'" @choose="_choose"></switchNavOne>
            <!-- 标签 -->
            <switchNavTwo v-if="status_id == 3" :is_switchLable="header_lable" @choose="_chooseLable"></switchNavTwo>
            <!-- 无数据 -->
            <div v-if="!isData.length" style="height: 100vh;width: 100%;display: flex;align-items: center;">
                <div class="noFindDiv" style="width: 100%;padding-top: 178rpx;height: 100%;">
                    <div><img class="noFindImg" :src="noOrder" /></div>
                    <span class="noFindText">{{language.order.myorder.no_order}}</span>
                </div>
            </div>
            <!-- 有数据 -->
            <view class="pages_box" v-if="isData.length">
                <view class="pages_box_box" v-for="(item,index) in isData" :key="item.id">
                    <!-- 头部 -->
                    <view class="pages_box_box_top">
                        <view class="pages_box_box_top_dd">{{language.yushou_order.ddh}}：{{item.sNo}}</view>
                        <view class="pages_box_box_top_zt">{{item.statusDesc}}</view>
                    </view>
                    <!-- 内容 -->
                    <view class="pages_box_box_body" @tap="_seeGoods(item)">
                        <view class="pages_box_box_left">
                            <template v-if="status_id == 3">
                                <image :src="item.img" style="width: 164rpx;height: 164rpx;border-radius: 16rpx;"></image>
                            </template>
                            <template v-else>
                                <image :src="item.imgUrl" style="width: 164rpx;height: 164rpx;border-radius: 16rpx;"></image>
                            </template>
                        </view>
                        <view class="pages_box_box_body_right" style="flex: 1;">
                            <view class="pages_box_box_body_right_title">{{item.product_title}}</view>
                            <view class="pages_box_box_body_rigth_gg">
                                <span>{{item.attrStr}}</span>
                                <span>{{language.yushou_order.g}} {{item.num}} {{language.yushou_order.j}}</span>
                            </view>
                            <view class="pages_box_box_body_right_price">
                                <view class="pages_box_box_body_right_price_jf" v-if="item.sell_type == 1">
                                    <span v-if="item.deposit">{{language.yushou_order.dj}}：{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{item.deposit.toFixed(2)}}</span>
                                    <span v-if="item.balance && item.freight">{{language.yushou_order.wk}}：{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{(item.balance + item.freight).toFixed(2)}}</span>
                                </view>
                                <view class="pages_box_box_body_right_price_jf" v-else>
                                    <template v-if="item.p_price">
                                        <span style="margin-left: 0;" v-if="status_id == 3">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{item.p_price.toFixed(2)}}</span>
                                    </template>
                                    <template v-if="item.z_price" >
                                        <span style="margin-left: 0;" v-if="status_id != 3">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{item.z_price.toFixed(2)}}</span>
                                    </template>
                                </view>
                            </view>
                        </view>
                    </view>
                    <!-- 按钮 -->
                    <view class="pages_box_box_jw">
                        <view class="pages_box_box_jw_sj">{{item.creteTime.substring(0,10)}}</view>
                        <!-- 定货 全部 -->
                        <view class="pages_box_box_jw_btn" v-if="item.sell_type == 2 && (item.status == 0)">
                            <!-- 编辑 -->
                            <view class="btn" @click="_navigateTo('/pagesC/synthesize/yushou_orderDetails?orderNo='+item.sNo+'&type=bj')">{{language.yushou_order.bjdd}}</view>
                        </view>
                        <!-- 待发货 -->
                        <view class="pages_box_box_jw_btn" v-if="(item.sell_type == 1 || item.sell_type == 2) && (item.status == 1)">
                            <!-- 编辑 -->
                            <view class="btn" @click="_navigateTo('/pagesC/synthesize/yushou_orderDetails?orderNo='+item.sNo+'&type=bj')">{{language.yushou_order.bjdd}}</view>
                            <!-- 发货 -->
                            <view class="btn" @click="_navigateTo('/pagesC/synthesize/yushou_fahuowuliu?orderDetailIds='+item.detailId + '&sNo=' + item.sNo+'&sum='+item.num)">{{language.yushou_order.fh}}</view>
                        </view>
                        <!-- 定货 待收货 -->
                        <view class="pages_box_box_jw_btn" v-if="item.sell_type == 2 && (item.status == 2)">
                            <!-- 查看物流 -->
                            <view class="btn" @click="_navigateTo('/pagesB/expressage/expressage?sNo='+item.sNo)">{{language.yushou_order.ckwl}}</view>
                        </view>
                        <!-- 退货 -->
                        <template v-if="status_id == 3">
                            <view class="pages_box_box_jw_btn" v-if="item.isManExamine">
                                 <!-- 人工处理 -->
                                 <view class="btn" @click="_navigateTo('/pagesA/myStore/returnDetail?&isRgcl=true'+'&sNo='+item.sNo+'&order_details_id='+item.id+'&yushou=true')">人工处理</view>
                            </view>
                            <view class="pages_box_box_jw_btn" v-if="(item.status == 999) && (item.r_type == '0' || item.r_type == '3')">
                                 <!-- 审核 -->
                                 <view class="btn" @click="_navigateTo('/pagesA/myStore/returnDetail?type=audit'+'&sNo='+item.sNo+'&order_details_id='+item.id+'&yushou=true')">{{language.yushou_order.sh}}</view>
                            </view>
                        </template>
                    </view>
                </view>
            </view>
        </view>
    </view>
</template>

<script>
    import search from '@/components/aComponents/search.vue'
    import switchNavOne from '@/components/aComponents/switchNavOne.vue'
    import switchNavTwo from '@/components/aComponents/switchNavTwo.vue'
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
                //header: ['全部', '待付款', '待发货', '退货'],
                //header_lable: ['仅退款', '退货退款', '换货'],
                status_id: '', //订单状态： 0全部 1待付款 2待发货 3退货
                is_tuihuo: 1,//退货状态： 0仅退款 1退货退款 2换货
                page: 1,
                keyWord: '',
                isData: [],
                numb: 0,
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
                noOrder: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/noOrder.png',
            }
        },
        components:{
            switchNavOne,
            switchNavTwo,
            search
        },
        computed:{
            header(){
                let new_data = []
                new_data[0] = this.language.yushou_order.nav[0]
                new_data[1] = this.language.yushou_order.nav[1]
                new_data[2] = this.language.yushou_order.nav[2]
                new_data[3] = this.language.yushou_order.nav[3]
                return new_data
            },
            header_lable(){
                let new_data = []
                new_data[0] = this.language.yushou_order.nav[4]
                new_data[1] = this.language.yushou_order.nav[5]
                new_data[2] = this.language.yushou_order.nav[6]
                return new_data
            }
        },
        onLoad(option) {
            this._axios()
        },
        onShow() {
            if(this.status_id == 3){
                this._chooseLable() 
                return
            }
            this._axios()
        },
        methods: {
            //查看详情
            _seeGoods(item){
                if(this.status_id == 3){
                    this._navigateTo('/pagesA/myStore/returnDetail?sNo='+item.sNo+'&order_details_id='+item.id+'&yushou=true')
                    return
                }
                this._navigateTo('/pagesC/synthesize/yushou_orderDetails?orderNo='+item.sNo)
            },
            //跳转
            _navigateTo(url){
                uni.navigateTo({
                    url: url
                })
            },
            //切换
            _choose(index){
                if(index == 3){
                    this.status_id = index
                    this._chooseLable(0) 
                    return
                }
                this.status_id = index - 1
                this._axios()
            },
            //搜索
            _search(item){
                this.keyWord = item
                if(this.status_id == 3){
                    this._axiosTh()
                    return
                }
                this._axios()
            },
            //退货
            _chooseLable(index){
                if(index == 0){
                    this.is_tuihuo = 2
                } else if(index == 1){
                    this.is_tuihuo = 1
                } else if(index == 2) {
                    this.is_tuihuo = 3
                }
                this._axiosTh()
            },
            /**
             * 预售订单 数据初始化
             * @param null
             */
            _axios(){
                this.is_mask = false
                let data = {
                    //api: 'app.mchPreSell.getOrderList',
                    api: 'plugin.presell.AppPreSell.getOrderList',
                    pageNo: this.page,
                    pageSize: 999,
                }
                if(this.keyWord){
                    data.keyWord = this.keyWord
                }
                if(this.status_id >= 0){
                    data.status = this.status_id
                }
                this.$req.post({data}).then(res=>{ 
                    if(res.code == 200){
                        this.isData = res.data.list
                    } else {
                        uni.showToast({
                            title:res.message,
                            icon:'none'
                        })
                    }
                })
            },
            /**
             * 预售订单 退货 数据初始化
             * @param null
             */
            _axiosTh(){
                this.is_mask = false
                let data = {
                    //api: 'app.mchPreSell.getRefundList',
                    api: 'plugin.presell.AppPreSell.getRefundList',
                    reType: this.is_tuihuo,
                    pageNo: this.page,
                    pageSize: 999,
                }
                if(this.keyWord){
                    data.orderno = this.keyWord
                }
                this.$req.post({data}).then(res=>{ 
                    if(res.code == 200){
                        this.numb = res.data.total
                        this.isData = res.data.list
                        this.isData.forEach((item,index)=>{
                            item.statusDesc = item.prompt
                            item.product_title = item.p_name
                            item.imgUrl = item.imgurl
                            item.attrStr = item.size
                            item.sell_type = item.sellType
                            item.creteTime = item.re_time
                            item.status = 999
                        })
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
        color: #333333;
        background: #F4F5F6;
        margin-left: 24rpx;
    }
    .pages_box_box_jw_btn{
        display: flex;
    }
    .pages_box_box_jw_sj{
        font-size: 28rpx;
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
        color: #999999;
    }
    .pages_box_box_body_right_price_jf{
        font-size: 32rpx;
        color: #666666;
        >span:first-child{
            margin-right: 20rpx;
        }
        >span:last-child{
            color: #FA5151;
        }
    }
    .pages_box_box_body_right_price{
        margin-top: 24rpx;
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
    .pages_box_box_body_rigth_gg{
        font-size: 24rpx;
        color: #999999;
        margin-top: 20rpx;
        
        display: flex;
        align-items: center;
        justify-content: space-between;
    }
    .pages_box_box_body_right_title {
        font-size: 32rpx;
        color: #333333;
        width: 100%;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        -webkit-box-orient: vertical;
    }

    .pages_box_box_body_right {
        width: 498rpx;
        margin-left: 24rpx;
        display: flex;
        flex-direction: column;
    }

    .pages_box_box_left {
        width: 164rpx;
        height: 164rpx;
    }

    .pages_box_box_body {
        margin-top: 26rpx;
        display: flex;
    }

    .pages_box_box_top_zt {
        font-size: 24rpx;
        color: #FA5151;
    }

    .pages_box_box_top_dd {
        font-size: 28rpx;
        color: #333333;
    }

    .pages_box_box_top {
        width: 100%;
        display: flex;
        justify-content: space-between;
        align-items: center;
    }

    .pages_box_box {
        // width: 100%;
        display: flex;
        padding: 32rpx;
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
    .numb{
        position: fixed;
        right: 24rpx;
        background-color: #f43530;
        color: #fff;
        width: auto;
        height: 30rpx;
        line-height: 30rpx;
        border-radius: 30rpx;
        min-width: 30rpx;
        font-size: 22rpx;
        white-space: nowrap;
        z-index: 1000;
        display: flex;
        align-items: center;
        justify-content: center;
    }
    .pages {
        width: 100%;
    }
</style>
