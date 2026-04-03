<template>
    <div class="container">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="language.myFinance.title" :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
        <div class="relative container_content" style="padding-top: 120rpx;">
            <div class="topTabBar" :style="[{top: topTabBarTop}]" >
                <!-- 收入明细 -->
                <div class="width_50" @tap="changeTabBar(3)"><div :class="{ active: topTabBar == 3 }">{{language.storeTransactionRecords.tabList[0]}}</div></div>
                <!-- 支出明细 -->
                <div class="width_50" @tap="changeTabBar(4)"><div :class="{ active: topTabBar == 4 }">{{language.storeTransactionRecords.tabList[1]}}</div></div>
                
                
                <!-- 售后明细 -->
                <div class="width_50" @tap="changeTabBar(1)"><div :class="{ active: topTabBar == 1 }">{{language.myFinance.tabList[0]}}</div></div>
               
                <!-- 提现明细 -->
                <div class="width_50" @tap="changeTabBar(2)">
                    <div :class="{ active: topTabBar == 2 }">{{ language.myFinance.tabList[2] }}</div>
                </div>                
            </div>

            
            <div class="tixian" v-if="topTabBar==2">
                <view class="tabBox">
                    <view :class="{active: tabIndex == 0}" @tap="changeTab(0)">{{language.myFinance.tabList1[0]}}</view>
                    <view :class="{active: tabIndex == 1}" @tap="changeTab(1)">{{language.myFinance.tabList1[1]}}</view>
                    <view :class="{active: tabIndex == 2}" @tap="changeTab(2)">{{language.myFinance.tabList1[2]}}</view>
                    <view :class="{active: tabIndex == 3}" @tap="changeTab(3)">{{language.myFinance.tabList1[3]}}</view>
                </view>
                <view v-for="(items, indexs) in list" :key="indexs">
                    <view class="tixian_item" @tap="toTixian(items.id)">
                        <view class="tixian_item_left">
                            <view>{{ items.type_name }}</view>
                            <text class="tixian_item_left_addtime top_text">{{ items.addtime }}</text>
                        </view>
                        <view class="tixian_item_center">
                            <view v-if="items.status == 2" class="tixian_item_center_money">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(items.price) }}</view>
                            <view v-else class="tixian_item_center_money">-{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(items.price) }}</view>
                            <text v-if="items.status == 0" class="tixian_item_center_inReview top_text">{{ language.myFinance.inReview }}</text>
                            <text v-if="items.status == 1" class="tixian_item_center_done top_text">{{ language.myFinance.done }}</text>
                            <text v-if="items.status == 2" class="tixian_item_center_faidDisc top_text">{{ language.myFinance.faidDisc }}</text>
                        </view>
                        <image class="jiantou" :src="jiantou" mode=""></image>
                    </view>
                </view>

                <uni-load-more v-if="list.length>8"  :loadingType="loadingType"></uni-load-more>

                <div v-if="list.length == 0 && status" class="wsj_box">
                    <img class="wsj_img" :src="wushuju"/>
                    <div>{{ language.myFinance.noList }}</div>
                </div>
            </div>

            <template v-if="topTabBar!=2">
				<view style="height: 70rpx;">
					<div class='home_navigation' :class="is_mask?'is_mask':''">
					    <div class='home_input'>
					        <img class='searchImg' :src="serchimg" alt="">
					        <input type="text" v-model.trim="searchtxt" :placeholder="placeholder1" placeholder-style="color:#999999;" @confirm="_placeholder()" id='input' name="sourch"/>
					    </div>
					    <div class='search_btn' @tap='_timeMask'>{{search_time==''?language.myFinance.xzrq:search_time}}</div>
					</div>
				</view>
                <view class="allPrice" v-if="topTabBar == 3"><view><view>入账总金额：{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(income)}}</view></view></view>
                <div v-if="list.length > 0" style="flex: 1;" :style="{marginTop:topTabBar == 3?'0rpx':'32rpx'}">
                    <div class="orderList">
                        <ul class="orderTitle">
                            <li style="position: relative" v-for="(items, indexs) in list" :key="indexs">
                                <div class="list_group1">
                                    <span>{{ items.type_name }}</span>
                                </div>
                                
                                <div class="list_group2">
                                    <span>{{ items.remake?items.remake:items.type_name }}</span>
                                    <span>{{ items.addtime | dateFormat }}</span>
                                </div>
                                
                                    <span class="bold" v-if="items.status == 1">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(items.price?items.price:items.money) }}</span>
                                    <span class="bold" v-else-if="items.status == 2">-{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(items.price?items.price:items.money )}}</span>
                            </li>
                        </ul>
                    </div>
                    <uni-load-more v-if="list.length>8" :loadingType="loadingType"></uni-load-more>
                </div>
                <div v-if="list.length == 0 && status" class="wsj_box">
                    <img class="wsj_img" :src="wushuju" />
                    <div>{{language.myFinance.noList}}</div>
                </div>
            </template>
            
            
            
            <view class="mask"  v-if="is_mask" @click.stop="_timeMask()">
                <view class="time">
                    <view class="time_txt">{{language.myFinance.xzrq}}</view>
                    <view class="time_time">
                        <view class="time_time_ks" :style="{'color':startDatetxt==language.myFinance.kssj?'':' #333333'}" @click.stop="open_time(1)">{{startDatetxt}}</view>
                        <view class="time_time_z">{{language.myFinance.zhi}}</view>
                        <view class="time_time_ks" :style="{'color':endDatetxt==language.myFinance.jssj?'':' #333333'}" @click.stop="open_time(2)">{{endDatetxt}}</view>
                    </view>
                    <view class="time_btn">
                        <view class="time_btn_cz" @click.stop="reset">{{language.myFinance.chongz}}</view>
                        <view class="time_btn_ss" @click.stop="_axiosGoods">{{language.myFinance.shous}}</view>
                    </view>
                </view>
                <date ref="starttimePicker" :themeColor="themeColor" :is_min="is_min" :urseTime="start_time" @onConfirm="onConfirm1"></date>
            </view>
        </div>
    </div>
</template>

<script>
    function InitTime(endtime){
        var dd,hh,mm,ss = null;
        var time = parseInt(endtime) - new Date().getTime();
        if(time<=0){
            return '结束'
        }else{
            dd = Math.floor(time / 60 / 60 / 24);
            hh = Math.floor((time / 60 / 60) % 24);
            mm = Math.floor((time / 60) % 60);
            ss = Math.floor(time  % 60);
            var str = dd+"天"+hh+"小时"+mm+"分"+ss+"秒";
            return str;
        }
    }
    import date from '../../components/date-time-picker.vue'
export default {
    data() {
        return {
            title: '账户明细',
            page: 1,
            topTabBar: 3,
            shop_id: '',
            type: 2,
            list: [],
            status: false,
            wushuju: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/auction/noShop.png',
            jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jiantou2x.png',
            serchimg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/searchNew.png',
            placeholder1: '请输入订单号',
            loadingType: 0,
            tabIndex: 0,  //提现明细tab
            
            searchtxt:'',
            
            is_mask:false,
            start_time: '',
            startDatetxt:'',
            startDate:'',
            endDate:'',
            endDatetxt:'',         
            search_time:'',
            is_ks:false,
            themeColor: '#FA5151',
            is_min:false,
            income: '',//入账总金额
            bgColor: [{
                    item: '#ffffff'
                },
                {
                    item: '#ffffff'
                }
            ],
        };
    },
    components: {
        date
    },
    onLoad(option) {
        this.setLang();
        uni.showLoading({
            title: this.language.toload.loading
        });
    },
    onShow () {
        this.placeholder1 = this.language.storeMyOrder.searchPlace
        this.shop_id = uni.getStorageSync('shop_id') ? uni.getStorageSync('shop_id') : this.$store.state.shop_id;
        this.list = [];
        this.page = 1;
        // this._axios();
        // 收入明细
        this.changeTabBar(3)
    },
    onReachBottom() {
        if (this.loadingType == 1 || this.loadingType == 2) {return false}
        this.loadingType = 1;
        this.page ++;
        this._axios()
    },
    methods: {
        _timeMask(){            
            this.is_mask = !this.is_mask
        },
        _placeholder(){
            this.list = [];
            this.page = 1;
            this._axios()
        },
        reset(){
            this.startDatetxt=this.language.myFinance.kssj
            this.endDatetxt=this.language.myFinance.jssj
            this.searchtxt=''//输入内容
            this.placeholder1 = this.language.storeMyOrder.searchPlace//选择日期文字提示
            this.startDate=''
            this.endDate=''
            this.search_time=''
            this.list = [];
            this.is_mask=false
            this._placeholder()
        },
        // 营业时间-确定
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
        open_time(e){
            if(e==1){
                this.is_ks = true
            }
            if(e==2){
                this.is_js = true
            }
                this.$refs.starttimePicker.show(this.start_time);
        },
        toTixian(id){
            uni.navigateTo({
                url: './tixianDetail?id='+id
            })
        },
        changeLoginStatus() {
            this._axios();
        },
        changeTab(index){
            this.tabIndex = index
            this.list = [];
            this.page = 1;
            this._axios();
        },
        changeTabBar(num) {
            this.topTabBar = num;
            this.type = num;
            this.list = [];
            this.status = false;
            this.page = 1;
            this.is_mask=false
            // 重置搜索数据
            this.reset()
        },
        _axiosGoods() {
            let m = this
            m.is_mask=false//关闭弹窗
            if(m.startDatetxt==this.language.myFinance.kssj&&m.endDatetxt==this.language.myFinance.jssj){
                return
            }
            m.search_time=m.startDatetxt+'-'+m.endDatetxt
            m._placeholder()
        },
        _axios() {
            uni.showLoading({
                title: this.language.toload.loading,
                mask: true
            });
            let data={
                        pageNo: this.page,
                        pageSize: 10,
                        shop_id: this.shop_id,
                        
                        api:"mch.App.Mch.Detail",
                        type: this.type,
                        tabIndex: this.tabIndex,
                    }
            if(this.topTabBar!=2){
                // 搜索条件
                data.orderNo=this.searchtxt,
                data.startDay=this.startDatetxt==this.language.myFinance.kssj?'':this.startDatetxt,
                data.endDay=this.endDatetxt==this.language.myFinance.jssj?'':this.endDatetxt
            }
            this.$req
                .post({
                    data
                })
                .then(res => {
                    uni.hideLoading();
                    if (res.code == 200) {
                        this.list = this.list.concat(res.data.list);
						this.income = res.data.income
                        if (res.data.list.length > 0) {
                            this.loadingType = 0
                        } else {
                            this.loadingType = 2
                        }
                        
                        if (this.list.length == 0) {
                            this.status = true;
                        }
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        });
                    }
                });
        }
    },
    computed: {
        now: function() {
            var year = new Date().getFullYear();
            var month = new Date().getMonth() + 1;
            var date = new Date().getDate();
            var date1 = year + '-' + month + '-' + date;
            return date1;
        },
        topTabBarTop () {
            var gru = uni.getStorageSync('data_height') ? uni.getStorageSync('data_height') : this.$store.state.data_height
            // #ifdef MP-TOUTIAO
            
            // #endif
            let height = 0
            // #ifndef MP-ALIPAY
            var heigh = parseInt(gru) + uni.upx2px(88)
            height = heigh && heigh > 0 ? heigh : uni.upx2px(88)
            // #endif

            // #ifdef MP
            heigh += 44
            // #endif

            
            return height + 'px'
        }
    }
};
</script>
<style>
    page {
        background-color: #f4f5f6;
    }
</style>
<style lang="less">
@import url("@/laike.less");
@import url('../../static/css/myStore/myFinance.less');
.allPrice{
	// height: 200rpx;
	>view{
		position: fixed;
		width: 100%;
		// height: 200rpx;
		background-color: #ffffff;
		border-radius: 0 0 24rpx 24rpx;
		z-index: 98;
		padding: 32rpx;
		box-sizing: border-box;
		display: flex;
		>view{
			flex: 1;
            height: 108rpx;
			background-color: rgba(250, 81, 81, .1);
			border-radius: 20rpx;
			font-size: 32rpx;
			color: #FA5151;
			display: flex;
			align-items: center;
			padding-left: 48rpx;
			box-sizing: border-box;
		}
	}
}
</style>
