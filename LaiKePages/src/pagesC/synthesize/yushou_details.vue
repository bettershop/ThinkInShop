<template>
    <view>
        <view class="pages">
            <template v-if="type == 'yuding'">
                <heads :title="language.xl_details.title[1]" :border="true" :bgColor='bgColor' titleColor="#000" ishead_w="2"></heads>
            </template>
            <template v-if="type == 'xiaoliang'">
                <heads :title="language.xl_details.title[0]" :border="true" :bgColor='bgColor' titleColor="#000" ishead_w="2"></heads>
            </template>
            <!-- 标签 -->
            <view :style="{height:fixed_height}">
                <ul class="order_header" :style="{borderRadius:is_mask?'0 0 0 0':''}">
                    <li class="header_label" v-if="type == 'yuding'">{{language.xl_details.list[0]}}：{{total}}{{language.xl_details.list[7]}}</li>
                    <li class="header_label" v-if="type == 'xiaoliang'">{{language.xl_details.list[1]}}：{{total}}{{language.xl_details.list[7]}}</li>
                    <li class="header_label_data" @tap="is_mask = !is_mask">
                        <view>
                            <span class="newCv">{{startDate?startDate:language.xl_details.list[2]}}</span>
                           <!-- <image :src="xuanze_data"></image> -->
                        </view>
                    </li>
                </ul>
            </view>
            <!-- 无数据显示 -->
            <div v-if="0" style="height: 100vh;width: 100%;display: flex;align-items: center;">
                <div class="noFindDiv" style="width: 100%;padding-top: 178rpx;height: 100%;">
                    <div><img class="noFindImg" :src="noOrder" /></div>
                    <span class="noFindText">{{language.order.myorder.no_order}}</span>
                </div>
            </div>
            <!-- 有数据显示 -->
            <view v-else class="pages_box">
                <view class="pages_box_box">
                    <view class="pages_box_box_body" @click="to_detal" v-for="(item,index) in isData" :key="item.id">
                        <view class="pages_box_box_left">
                            <image :src="item.headimgurl" style="width: 64rpx;height: 64rpx;border-radius: 50%;"></image>
                        </view>
                        <view class="pages_box_box_body_right">
                            <view class="pages_box_box_body_right_title">
                                <span>
                                    {{item.user_name}}
                                    {{type == 'yuding'?language.xl_details.list[3]:language.xl_details.list[4]}}
                                    {{item.num}}{{language.xl_details.list[7]}}
                                </span>
                                <span v-if="type == 'yuding'">{{item.statusDesc}}</span>
                                <span v-else></span>
                            </view>
                            <view class="pages_box_box_body_rigth_gg">{{item.attrStr}}</view>
                            <view class="pages_box_box_body_right_price">
                                <view class="pages_box_box_body_right_price_jf">{{item.sNo}}</view>
                                <view class="pages_box_box_body_right_price_jf">{{item.creteTime}}</view>
                            </view>
                        </view>
                    </view>
                </view>
            </view>
        </view>
        <view class="mask"  v-if="is_mask" @click.stop="is_mask = !is_mask">
            <view class="time">
                <view class="time_txt">{{language.xl_details.list[2]}}</view>
                <view class="time_time">
                    <view class="time_time_ks" :style="{'color':startDatetxt=='开始时间'?'#999999':' #333333'}" @click.stop="open_time(1)">{{startDate?startDate:startDatetxt}}</view>
                    <view class="time_time_z">{{language.xl_details.list[7]}}</view>
                    <view class="time_time_ks" :style="{'color':endDatetxt=='结束时间'?'#999999':' #333333'}" @click.stop="open_time(2)">{{endDate?endDate:endDatetxt}}</view>
                </view>
                <view class="time_btn">
                    <view class="time_btn_cz" @click.stop="reset">{{language.xl_details.list[5]}}</view>
                    <view class="time_btn_ss" @click.stop="_axios">{{language.xl_details.list[6]}}</view>
                </view>
            </view>
            <date ref="starttimePicker" :themeColor="themeColor" :is_min="is_min" @onConfirm="onConfirm1"></date>
        </view>
    </view>
</template>

<script>
    import date from '@/components/date-time-picker.vue'
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
                title: '',
                type: '',//xiaoliang-销量详情  yuding-预定详情
                is_tishi:false,
                is_sus:false,
                status_id: '0', //选中的 订单模式
                label_id: '0', //选中的 标签
                fixed_height:'',
                name: '',
                page: 1,
                productId: '',
                is_mask: false,
                is_ks: false,
                is_js: false, 
                startDatetxt: '开始时间',
                endDatetxt: '结束时间',
                startDate: '', // 开始时间
                endDate: '', // 结束时间
                themeColor: '#FA5151',
                is_min: false,
                total: '',
                isData: [],
                header_label: ['已上架', '已下架', '待上架', '已结束'],
                serchimg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/searchNew.png',
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
                sc_icon: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/delete2x.png',
                noOrder: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/noOrder.png',
                xuanze_data: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jiantou2x.png',
            }
        },
        components: { date },
        mounted(){
            const query = uni.createSelectorQuery().in(this);
            query.select('.order_header').boundingClientRect(data => {
              this.fixed_height = data.height + 'px'
            }).exec();
        },
        onLoad(option) {
            if(option.id){
                this.productId = option.id
            }
            if(option.type){
                if(option.type == 'yuding'){
                    this.type = 'yuding'
                    this._axios()
                }else if(option.type == 'xiaoliang'){
                    this.type = 'xiaoliang'
                    this._axios()
                }
            }
        },
        methods: {
            //跳转
            _navigateto(url){
                uni.navigateTo({
                    url: url
                })
            },
            /**
             * 预售商品 数据初始化
             * @param null
             */
            _axios(){
                this.is_mask = false
                let data = {
                    //api: 'app.mchPreSell.getOrderList',
                    api: 'plugin.presell.AppPreSell.getOrderList',
                    productId: this.productId,
                    pageNo: this.page,
                    pageSize: 99999,
                } 
                if(this.startDate){
                    data.startDate = this.startDate
                }
                if(this.endDate){
                    data.endDate = this.endDate
                }
                if(this.type == 'yuding'){
                    data.isRefund = '0'
                    data.statusList = '0,1,2,5'
                }
                if(this.type == 'xiaoliang'){
                    data.statusList = '5'
                }
                this.$req.post({data}).then(res=>{ 
                    if(res.code == 200){
                        //bug 50496
                        //this.total = res.data.orderProNum     
                        this.total = res.data.total
                        this.isData = res.data.list
                    } else {
                    }
                })
            },
            /**
             * 选择日期
             * @param null
             */
            open_time(e){
                if(e==1){
                    this.is_ks = true
                }
                if(e==2){
                    this.is_js = true
                }
                this.$refs.starttimePicker.show(this.start_time);
            },
            /**
             * 选择日期-确定
             * @param null
             */
            onConfirm1(e) {
                var start = e[0].replace(/undefined/g, '00');
                var end = e[1].replace(/undefined/g, '00');
                this.start_time = start + ' ' + end;
                if(this.is_ks==true){
                    this.startDate = start
                    this.startDatetxt = start
                    this.is_ks = false
                }
                if(this.is_js == true){
                    this.endDate = start
                    this.endDatetxt = start
                    this.is_js = false
                }
            },
            /**
             * 选择日期-重置
             * @param null
             */
            reset(){
                this.startDatetxt='开始时间'
                this.endDatetxt='结束时间'
                this.startDate=''
                this.endDate=''
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
    @import url('../../static/css/myStore/myFinance.less');
    .newCv{
        width: 100rpx;
        height: auto;
        display: inline-block;
        overflow: hidden;
        text-overflow: ellipsis;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 1;
        white-space: nowrap;
    }
    .tishi_bg{
        position: fixed;
        top: 0;
        bottom: 0;
        left: 0;
        right: 0;
        z-index: 999;
        background-color: rgba(0,0,0,.5);
        .tishi_nr{
            width: 640rpx;
            height: 358rpx;
            background: #FFFFFF;
            border-radius: 24rpx;
            margin: 0 auto;
            position: relative;
            top: 50%;
            transform: translateY(-160rpx);
            overflow: hidden;
            >view:nth-child(1){
                font-size: 32rpx;
                font-weight: 500;
                color: #333333;
                line-height: 44rpx;
                margin-top: 64rpx;
                text-align: center;
            }
            >view:nth-child(2){
                font-size: 32rpx;
                font-weight: 400;
                color: #999999;
                line-height: 44rpx;
                margin-top: 32rpx;
                margin-bottom: 66rpx;
                text-align: center;
            }
            .tishi_btn{
                width: 100%;
                height: 106rpx;
                border-top: 2rpx solid rgba(0,0,0,.1);
                display: flex;
                >view{
                    flex: 1;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    
                    font-size: 32rpx;
                    font-weight: 500;
                    color: #D73B48;
                    line-height: 44rpx;
                }
                >view:first-child{border-right: 2rpx solid rgba(0,0,0,.1);color: #333333;}
            }
        }
    }
    .xieyi{
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background-color: rgba(0, 0, 0, .5);
        z-index: 99;
        display: flex;
        justify-content: center;
        align-items: center;
        >view{
            width: 640rpx;
            min-height: 100rpx;
            max-height: 486rpx;
            background: #FFFFFF;
            border-radius: 24rpx;
            .xieyi_btm{
                height: 108rpx;
                color: @D73B48;
                display: flex;
                justify-content: center;
                align-items: center;
                border-top: 0.5px solid rgba(0,0,0,.1);
                font-weight: bold;
                font-size: 32rpx;
            }
            .xieyi_title{
                display: flex;
                justify-content: center;
                align-items: center;
                font-size: 32rpx;
                
                font-weight: 500;
                color: #333333;
                line-height: 44rpx;
                margin-top: 64rpx;
                margin-bottom: 32rpx;
                font-weight: bold;
                font-size: 32rpx;
            }
            .xieyi_text{
                width: 100%;
                max-height: 236rpx;
                overflow-y: scroll;
                padding: 0 32rpx;
                box-sizing: border-box;
            }
        }
    }
    .btn{
        text-align: center;
        border-radius: 52rpx;
        font-size: 24rpx;
        color: #333333;
        background: #F4F5F6;
        margin-left: 24rpx;
        padding: 12rpx 24rpx;
        box-sizing: border-box;
    }
    .pages_box_box_jw_btn{
        >view:first-child{
            margin-left: 0;
        }
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
        justify-content: end;
        align-items: center;
    }
    .pages_box_box_body_right_price_num{
        font-size: 24rpx;
        color: #999999;
    }
    .pages_box_box_body_right_price_jf{
        font-size: 24rpx;
        color: #999999;
    }
    .pages_box_box_body_right_price{
        margin-top: 8rpx;
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
    .pages_box_box_body_rigth_gg{
        font-size: 24rpx;
        color: #999999;
        margin-top: 8rpx;
    }
    .pages_box_box_body_right_title {
        width: 100%;
        font-size: 32rpx;
        font-weight: normal;
        color: #333333;
        display: flex;
        align-items: center;
        justify-content: space-between;
        >span:first-child{
            width: 500rpx;
            font-size: 28rpx;
            color: #333333;
        }
        >span:last-child{
            font-size: 24rpx;
            color: #FA5151;
            line-height: 32rpx;
        }
    }

    .pages_box_box_body_right {
        width: 100%;
        margin-left: 24rpx;
        display: flex;
        flex-direction: column;
    }

    .pages_box_box_left {
        width: 64rpx;
        height: 64rpx;
    }

    .pages_box_box_body {
        display: flex;
        padding-bottom: 20rpx;
        border-bottom: 2rpx solid rgba(0,0,0,.1);
        margin-bottom: 20rpx;
    }

    .pages_box_box_top_zt {
        font-size: 24rpx;
         
        font-weight: normal;
        color: #FA5151;
    }

    .pages_box_box_top_dd {
        font-size: 28rpx;
         
        font-weight: normal;
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
        min-height: 100vh;
        height: 100%;
        display: flex;
        padding: 32rpx;
        flex-direction: column;
        background-color: #fff;
        border-radius: 24rpx 24rpx 0 0;
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

    .header_border {
        // color: @btnBackground;
        font-weight: bold;
        color: #333333;
        font-size: 32rpx;
    }

    .header_border::after {
        position: absolute;
        content: '';
        left: 50%;
        transform: translateX(-50%);
        bottom: 0;
        width: 200rpx;
        height: 4rpx;
        background: #FA5151;
        border-radius: 4rpx;
    }

    .header_li {
        width: 33%;
        text-align: center;
        height: 70rpx;
        line-height: 70rpx;
        //     border-bottom: 1rpx solid #eee;
        font-size: 28rpx;
        position: relative;
    }
    
    .new_margin{
        padding-bottom: 48rpx !important;
        padding-top: 32rpx;
    }
    
    .order_header {
        display: flex;
        justify-content: space-between;
        width: 100%;
        padding: 0 32rpx;
        border-radius: 0 0 24rpx 24rpx;
        background-color: #fff;
        position: fixed;
        z-index: 999;
        .header_label{
            height: 112rpx;
            line-height: 112rpx;
            font-size: 32rpx;
            color: #333333;
        }
        .header_label_data{
            display: flex;
            align-items: center;
            >view{
                display: flex;
                align-items: center;
                justify-content: center;
                padding: 16rpx 32rpx;
                background: #F4F5F6;
                border-radius: 32rpx;
                >span{
                    font-size: 24rpx;
                    color: #999999;
                    line-height: 40rpx;
                    //margin-right: 16rpx;
                }
                >image{
                    width: 18rpx;
                    height: 36rpx;
                }
            }
        }
    }
    .time{
        top: 198rpx !important;
        /* #ifdef MP-WEIXIN */
        top: 288rpx !important;
        /* #endif */
    }
    .pages {
        width: 100%;
    }
</style>
