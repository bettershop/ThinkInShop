<template>
    <div class="container">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="language.myStore.dzmd" :returnR="returnR"  ishead_w="2" :bgColor="bgColor" titleColor="#333333"></heads>
        <div class="relative">
         <div class="position_head">
            <div class='home_navigation'>
                <div class='home_input'>
                    <img class='searchImg' :src="serchimg" alt="">
                    <input type="text" v-model="searchtxt" :placeholder="language.expressRecord.qsrkd" placeholder-style="color:#999999;font-size: 26upx" name="sourch"/>
                    <image v-show="searchtxt.length > 0" @click="searchtxt = ''" class="cancel" :src="sc_icon" mode=""></image>
                </div>
                <div class='search_btn' @tap='_search'>{{language.search2.search.search}}</div>
            </div>
            
            <div class="topTabBar">
                <div class="width_25" @tap="changeTabBar(1)">
                    <div :class="{ active: topTabBar == 1 }">
                        {{language.expressRecord.qb}}
                    </div>
                </div>
                <div class="width_25" @tap="changeTabBar(2)">
                    <div :class="{ active: topTabBar == 2 }">
                        {{language.expressRecord.ydy}}
                        
                    </div>
                </div>
                <div class="width_25" @tap="changeTabBar(3)">
                    <div :class="{ active: topTabBar == 3 }">
                        {{language.expressRecord.wdy}}
                    </div>
                </div>
            </div>
        </div>
            <div v-if="list.length > 0">
                <div class="orderlist-line"></div>
                <div v-for="(item, index) in list" :key="index">                    
                    <div class="orderList">
                        <div class="orderTitle">
                            <div>{{language.expressRecord.kddd}}： {{ item.kdComOrderNum }}</div>

                            <div class="jiantou" @tap='_changeDetail(item.sNo)'>
                                <img :src="jiantou" alt="" />
                            </div>
                        </div>
                        <div class="proList">
                           
                            <div class="pro_top">
                                <span>{{language.expressRecord.kddh}}：{{ item.courier_num }}</span>
                                <span>{{item.deliver_time}}</span>
                            </div>
                            <div class="pro_body">
                                <div class="pro_body_d">
                                    <p>{{ item.send_sheng }}</p>
                                    <p class="pro_body_d_p2">{{ item.send_name }}</p>
                                </div>
                                <img class="jt_icon" :src="jt_icon" alt="" />
                                <div class="pro_body_d">
                                    <p>{{ item.sheng }}</p>
                                    <p class="pro_body_d_p2">{{ item.name }}</p>
                                </div>
                            </div>
                            <div>{{language.storeMyOrder.orderNo}} {{ item.sNo }}</div>
                        </div>
                        <div class="proListDown " >

                            <div class="font_28" v-if='item.is_status==0'>{{language.expressRecord.wdy}}</div>
                            <div class="font_28" v-if='item.is_status==1'>{{language.expressRecord.ydy}}</div>
                            <div class="center ml_10" >
                                <div class="proBtn" @tap='bigimg=item.img'>{{language.expressRecord.ckmd}}</div>
                                <div class="proBtn" @tap='th1(item.id)'>{{language.expressRecord.cksp}}</div>

                                <div class="proBtn" @tap='_closeOrder(item.id)'>{{language.expressRecord.qxxd}}</div>
                            </div>
                            
                        </div>
                    </div>
                </div>
                
            </div>
            <div v-else class="noFindDiv">
                <div><img class="noFindImg" :src="noOrder" /></div>
                <span class="noFindText">{{language.expressRecord.zsmyj}}</span>
            </div>
        </div>
        <view class="xieyi mask" style="background-color: initial;" v-if="is_sus">
            <view style="width: 272rpx;height: 272rpx;background-color: rgba(51, 51, 51, .9);">
                <view style="margin: 32rpx 0;text-align: center;margin-top: 64rpx;">
                    <image style="width: 68rpx;height: 68rpx;" :src="sus"></image>
                </view>
                <view class="xieyi_title"
                    style="margin-bottom: 0;margin-top: 0;color: #fff;font-weight: 500;font-size: 32rpx;">
                    {{language.zdata.gbcg}}
                </view>
            </view>
        </view>
        <view class="bigimg" v-if='bigimg' @tap="bigimg=''">
            <img :src="bigimg" alt="" />
        </view>
    </div>
</template>

<script>

    export default {
        data() {
            return {
                warnIng: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/warnIng.png',

                is_sus:false,
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',


                noOrder: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/noOrder.png',
                loadingType: 0,

                title: '',
                access_id: '',
                topTabBar: 1,
                integral_hong: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/integral.png',
                integral_hei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/integral_hei.png',
                testImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/yhqBg.png',
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jiantou2x.png',
                guanbi: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/guanbi2x.png',
                storeOrder_th: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/huanhuo.png',
                storeOrder_thtk: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/tuihuo2x.png',
                storeOrder_tk: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/tuikuan2x.png',
                serchimg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/searchNew.png',
                sc_icon: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL+'images/icon1/delete2x.png',
                jt_icon: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL+'images/icon1/jt_icon.png',



                shop_id: '',
                order_type: '',
                list: [],
                page: 1,
                sNo: '',




                returnR: 1,

                placeStyle: 'color:#b8b8b8;font-size:28upx',


                
                searchtxt: '',

                bgColor: [
                    {
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                bigimg:'',
                
            };
        },
        onLoad(option) {
            this.access_id = uni.getStorageSync('access_id') ? uni.getStorageSync('access_id') : this.$store.state.access_id;

            this.isLogin(()=>{})
            if(option.sNo){
                this.searchtxt = option.sNo;
            }
            
        },
        onShow() {
            this.access_id = uni.getStorageSync('access_id') ? uni.getStorageSync('access_id') : this.$store.state.access_id;
            this.shop_id = uni.getStorageSync('shop_id') ? uni.getStorageSync('shop_id') : this.$store.state.shop_id;
            
            this.page = 1
            this.loadingType = 0
            this._axios();
        },
        methods: {
            _search(){
                this.page = 1
                this.loadingType = 0
                this._axios();
            },
            changeLoginStatus() {
                this.access_id = uni.getStorageSync('access_id');
                this._axios();
            },
            changeTabBar(num) {
                this.searchtxt = ''
                this.page = 1
                this.loadingType = 0
                this.topTabBar = num
                if (num == 1) {
                    this.order_type = ''
                } else if (num == 2) {
                    this.order_type = '1'
                } else if (num == 3) {
                    this.order_type = '0'
                }
                this.list = []
                this._axios()
            },
            _axios() {
                uni.showLoading({
                    title: this.language.MsIndex.loading
                })
                let data = {
                    
                    api:'mch.App.Mch.ShippingRecords',
                    status: this.order_type,
                    search: this.searchtxt,
                    page: this.page,
                }
                this.$req.post({data}).then(res => {
                    let { code, data, message } = res
                    uni.hideLoading()
                    if (code == 200) {
                
                        this.list = data.list;
                        
                        
                    
                        this.page = 1;
                        
                        
                
                        if (data.list.length == 0) {
                            this.nodata = 1
                        } else {
                            this.nodata = -1
                        }

                    } else if (code == 404) {
                        uni.showToast({
                            title: message,
                            duration: 1500,
                            icon: 'none'
                        })
                        setTimeout(function() {
                            uni.navigateTo({
                                url: '../../pagesD/login/newLogin?landing_code=1',
                            })
                        }, 1500)
                    } else {
                        uni.showToast({
                            title: message,
                            duration: 1500,
                            icon: 'none'
                        })
                    }
                })
            },
            _changeDetail(sNo) {
                
                uni.navigateTo({
                    url: '/pagesB/myStore/expressReDetail?id=' + sNo
                });
            },


            // 编辑订单
            th1(sNo) {
                uni.navigateTo({
                    url: '/pagesB/myStore/expressRePro?&id=' + sNo
                });
            },

        

            // 关闭订单
            _closeOrder(id) {
                uni.showModal({
                    title: this.language.Redeem_order.tipsa[0],
                    content: '确定要取消该电子面单发货吗？',
                    confirmText: this.language.showModal.confirm,
                    cancelText: this.language.showModal.cancel,
                    cancelColor:'#333333 !important',
                    confirmColor:'#D73B48 !important',
                    success: (e)=>{
                        if(e.confirm){
                            let data = {
                                
                                api:"mch.App.Mch.CancelElectronicWaybill",
                                id
                            }
                            this.$req.post({data}).then(res => {
                                let { code, data, message } = res;
                                if (code == 200) {
                            
                                    this.is_sus = true
                                    setTimeout(function() {
                                        this.is_sus = false
                                        this._axios()
                                    }, 1500)
                                } else if (code == 404) {
                                    uni.showToast({
                                        title: message,
                                        duration: 1500,
                                        icon: 'none'
                                    })
                                    setTimeout(function() {
                                        uni.navigateTo({
                                            url: '../../pagesD/login/newLogin?landing_code=1',
                                        })
                                    }, 1500)
                                } else {
                                    uni.showToast({
                                        title: message,
                                        duration: 1500,
                                        icon: 'none'
                                    })
                                }
                            })
                        }
                    }
                })
            },
            
            
        }, 

        onReachBottom: function() {
            if (this.loadingType != 0) {
                return;
            }
            this.loadingType = 1;
            
            this.page += 1;
            
            var data = {

                api:'mch.App.Mch.ShippingRecords',
                access_id: this.access_id,
                search: this.searchtxt,
                status: this.order_type,
                page: this.page
            };

            this.$req.post({ data }).then(res => {
                let { code, data, message } = res;
                if (data.list.length > 0) {
                    
                    
                    this.list = this.list.concat(data.list);
                    
                
                    this.loadingType = 0;
                } else {
                    this.loadingType = 2;
                }
            });
        },
        computed: {
           
            
        },
        components: {
        }
    };
</script>

<style>
    page{
        background: #F4F5F6
    }
</style>
<style lang="less">
    @import url("@/laike.less");
    .container {
        min-height: 100vh;
        width: 100%;
        overflow: hidden;
    }
    
    .mpvue-picker-content {
        z-index: 99999999999999;
    }
    
    .integral-img {
        height: 26rpx;
        width: 26rpx;
        margin-right: 6rpx;
        position: relative;
        top: 4rpx;
    }
    


    .sellMoney {
       font-size: 32rpx;
       
       font-weight: 500;
       color: #FA5151;
        text-align: right;
    }
    
    .proListUpRight {
        position: relative;
        flex: 1;
        overflow: hidden;
        padding-left: 24rpx;
        display: flex;
        flex-direction: column;
    }
    
    .proSellData {
        font-size: 24rpx;
        color: #999999;
        display: flex;
        align-items: center;
        div {
        }
    }
    
    
    .proTitle {
        font-size: 32rpx;
        
        font-weight: 400;
        color: #333333;
        overflow: hidden;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
        text-overflow: ellipsis;
    }
    
    .proPro {
        font-size: 24rpx;
        
        font-weight: 400;
        color: #999999;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        margin-top: 16rpx;
        margin-bottom: 24rpx;
    }
    .xieyi {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background-color: rgba(0, 0, 0, 0.5);
        z-index: 99;
        display: flex;
        justify-content: center;
        align-items: center;
        > view {
            width: 640rpx;
            min-height: 100rpx;
            max-height: 486rpx;
            background: #ffffff;
            border-radius: 24rpx;
            .xieyi_btm {
                height: 108rpx;
                color: @D73B48;
                display: flex;
                justify-content: center;
                align-items: center;
                border-top: 0.5px solid rgba(0, 0, 0, 0.1);
                font-weight: bold;
                font-size: 32rpx;
            }
            .xieyi_title {
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
            .xieyi_text {
                width: 100%;
                max-height: 236rpx;
                overflow-y: scroll;
                padding: 0 32rpx;
                box-sizing: border-box;
            }
        }
    }
    .proListUp {
        width: 100%;
        margin-bottom: 20rpx;
        display: flex;
        align-items: center;
        justify-content: space-around;
        position: relative;
    }
    .proListUpLeft {
        position: relative;
    }
    .proListUpLeft img {
        width: 164rpx;
        height: 164rpx;
        border-radius: 16rpx;
    }
    
    .proListDown {
        margin-top: 23rpx;
        display: flex;
        align-items: center;
        justify-content: space-between;
        background-color: #fff;
        padding: 0 32rpx;
        position: relative;
    }
    
    .proBtn {
        width: 144rpx;
        height: 56rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24rpx;
        margin-left: 24rpx;
        background: #F4F5F6;
        border-radius: 28rpx;
        font-weight: 500;
        color: #333333;
    }
    
    .proList {
        padding: 0 32rpx;
        background-color: #fff;
    }
    
    
    .date {
        font-size: 22rpx;
        color: #999;
        padding: 20rpx 30rpx;
        background: #f6f6f6;
    }
    
    .topTabBar {
        display: flex;
        align-items: center;
        
        
        justify-content: space-around;
        width: 100%;
        padding-bottom: 24rpx;
        border-radius: 0px 0px 24rpx 24rpx;
    }
    .topTabBar>div{
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;    
    }
    
    .topTabBar>div div {
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
    }
    
    .active {
        
        
        color: #333333;
        font-weight: bold;
        font-size: 32rpx;
        &::after{
        position: absolute;
        content: '';
        
        left: 50%;
        transform: translateX(-50%);
        bottom: 0;
        
        width: 104rpx;
        height: 4rpx;
        background: #FA5151;
        border-radius: 4rpx;
    }
    }
    

    
    .width_25 {
        
        width: 25%;
        text-align: center;    
        height:70rpx;
        line-height:70rpx;
        font-size: 28rpx;
        color: #999999;
        position: relative;
    }
    .color_ff3 {
        color: #ff3333;
    }
    
    .ml_10 {
        margin-left: 10rpx;
    }
    
    .ml_60 {
        margin-left: 60rpx;
    }
    
    .mr {
        flex: 1;
        font-size: 24rpx;
        
        font-weight: 400;
        color: #999999;
    }
    
    .mt_20 {
        margin-top: 20rpx;
    }
    
    .font_28 {
        font-size: 28rpx;
        font-weight: 400;
        color: #FA5151;
    }
    
    .color_ff0 {
        color: @uni-text-color-obvious;
    }
    
    .center {
        display: flex !important;
        align-items: center !important;
        justify-content: center !important;
    }
    
    .radio_color {
        color: #000000;
    }
    

    
    .hr {
        width: 100%;
        height: 20rpx;
        background-color: #f6f6f6;
    }
    
    .noFindDiv {
        width: 100% !important;
        padding-top: 178rpx !important;
        height: 100% !important;
        .noFindImg{
            width: 750rpx;
            height: 394rpx;
        }
    }

    

    
    
 
    .position_head {
        position: sticky;
        top: 0;
        width: 100%;
        background-color: #fff;
        z-index: 100;
        border-radius: 0 0 24rpx 24rpx;
    }
    
    .home_navigation {
        background-color: #ffffff;
        border: 0;
        padding-bottom: 10rpx;
        box-sizing: border-box;
        height: 120rpx;
        .search_btn {
            margin-left: 24rpx;
            font-size: 28rpx;
            color: #Fa5151;
            min-width: 120rpx;
            text-align: center;
            height: 64rpx;
            line-height: 64rpx;
            background: rgba(250, 81, 81, 0.1);
            border-radius: 32rpx;
        }
        .home_input {
            width: 100% !important;
        }
        
        .home_input input{
            height: 64rpx;
            border-radius: 35rpx;
            color: #333333;
            padding-left: 64rpx;
        }
        
        .input-placeholder[class]{
            display: block;
        }
        
        .home_input .cancel{
            position: absolute;
            top: 50%;
            transform: translateY(-50%);
            width: 30rpx;
            height: 30rpx;
            right: 20rpx;
        }
        
        .searchImg{
            width: 30rpx;
            height: 30rpx;
            left: 26rpx;
            top: 50%;
            transform: translateY(-50%);
        }
    
    }
    
    .noFindText {
        font-size: 14px;
    }
     .orderlist-line {
        height: 24rpx;
        background-color: #f6f6f6;
    }
    .orderList{
        background: #FFFFFF;
        overflow: hidden;
        border-radius: 24rpx;
        padding: 32rpx 0;
        margin-bottom: 16rpx;
    }
    
    .orderDetail {
        height: 180rpx;
        padding: 30rpx;
    }
    
    .orderTitle {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0 32rpx;
        background-color: #fff;
        font-size: 28rpx;
        color: #333333;
        margin-bottom: 24rpx;
    }
    
    .orderTitle > .jiantou {
       width: 32rpx;
       height: 44rpx;
       img{
           width: 32rpx;
           height: 44rpx;
       }
    }
    .pro_top{
        display: flex;
        justify-content: space-between;
        font-size: 24rpx;
        color: #999999;
    }
    .pro_body{
        height: 140rpx;
        display: flex;
        justify-content: space-around;
        background: #F4F5F6;
        border-radius: 20rpx;
        margin: 12rpx 0;
        padding: 0 38rpx;
    }
    .pro_body_d{
        text-align: center;
        padding-top: 26rpx;
    }
    .pro_body_d_p2{
        color: #999999;
    }
    .bigimg{
        position: fixed;
        top: 0;
        left: 0;
        background-color: rgba(0, 0, 0, 0.1);
        width: 100%;
        height: 100vh;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        z-index: 1000;
        img{
            width: 100%;
        }
    }
    .jt_icon{
        width: 144rpx;
        height: 16rpx;
        margin-top: 60rpx;
    }
</style>
