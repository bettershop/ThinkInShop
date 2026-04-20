<template>
	<view>
        <!-- 头部 -->
        <heads 
            :title="language.mystore_order.title" 
            :ishead_w="ishead_w" 
            :bgColor="bgColor" 
            :titleColor="titleColor"
            :returnR="returnR" 
            :types="types" 
            :order_type="order_type">
        </heads>
        <!-- 内容 -->
        <view class="pages">
            <template v-if="isData && isData.essentialInfo">
                <view class="pages_top">
                    <view class="pages_top_zt">{{isData.essentialInfo.statusDesc}}</view>
                    <view class="pages_top_ddh"  >
                        <template v-if="isData.productInfo[0].sellType == 1">
                            <template v-if="isData.productInfo[0].status == 0">
                                
                                <view v-if="depositPayTimeIsKs" class="pages_top_ddh_left">{{depositPayTime}}{{language.order.order.kstime}}</view>
                                <view v-else class="pages_top_ddh_left">{{language.mystore_order.newT[0]}}：{{payTime_star}}</view> 
                            </template>
                        </template>
                        <template v-if="isData.productInfo[0].sellType == 2">
                            <template v-if="isData.productInfo[0].status == 0">
                                <view class="pages_top_ddh_left">{{language.mystore_order.newT[1]}}：{{payTime_star}}</view>
                            </template>
                        </template>
                    </view>
                    <!-- 订单号 -->
                    <view class="pages_top_ddh">
                        <view class="pages_top_ddh_left">{{language.mystore_order.newT[2]}}：{{isData.essentialInfo.orderNo}}</view>
                        <view class="pages_top_ddh_right" @tap="_copySno">{{language.mystore_order.newT[3]}}</view>
                    </view>
                    <!-- 下单时间 -->
                    <view class="pages_top_time">{{language.mystore_order.newT[4]}}：{{isData.essentialInfo.addTime}}</view>
                </view>
                <view class="pages_lb" v-if="!setOrder">
                    <view class="paegs_lb_box" style="border: none;height: 140rpx;justify-content: flex-start;">
                        <view class="paegs_lb_box_left">{{language.mystore_order.newT[5]}}：</view>
                        <view class="paegs_lb_box_right">{{isData.essentialInfo.userName}}</view>
                    </view>
                </view>
            </template>
            <template v-if="isData && isData.consigneeInfo">
                <view class="dizhi" v-if="!setOrder">
                    <view class="dizhi_xx">{{isData.consigneeInfo.address}}</view>
                    <view class="dizhi_xxhs">{{isData.consigneeInfo.sheng}}{{isData.consigneeInfo.shi}}{{isData.consigneeInfo.xian}}</view>
                    <view class="dizhi_dh">
                        <view class="dizhi_dh_name">{{isData.consigneeInfo.consignee}}</view>
                        <view class="dizhi_dh_num">{{isData.consigneeInfo.phone}}</view>
                    </view>
                </view>
                <view class="pages_list" v-if="setOrder">
                    <view class="pages_list_box">
                        <view class="pages_list_box_name">
                            <view class="pages_list_box_name_txt" ><span style="color: #FA5151;"></span>{{language.mystore_order.Buyer}}：</view>
                            <view class="pages_list_box_name_input" style="text-align: inherit;border: none;">{{isData.essentialInfo.userName}}</view>
                        </view>
                        <view class="pages_list_box_name">
                            <view class="pages_list_box_name_txt">{{language.mystore_order.username}}</view>
                            <input class="pages_list_box_name_input" v-model="name" />
                        </view>
                        <view class="pages_list_box_name">
                            <view class="pages_list_box_name_txt">{{language.mystore_order.newT[6]}}</view>
                            <input class="pages_list_box_name_input" v-model="modle" />
                        </view>
                        <view class="pages_list_box_name">
                            <view class="pages_list_box_name_txt">{{language.mystore_order.newT[7]}}</view>
                            <div class="_input" @tap="showMulLinkageThreePicker">
                                <input type="text" disabled='true'
                                       :placeholder="language.mystore_order.address_placeholder" placeholder-style="color:#B8B8B8"
                                       @focus="hideKeyboard()"
                                       v-model="address"
                                />
                                <img :src="jiantou" class="jiantou" />
                            </div>
                        </view>
                        <view class="pages_list_box_name">
                            <view class="pages_list_box_name_txt">{{language.mystore_order.addressinfo}}</view>
                            <input class="pages_list_box_name_input" v-model="addressInfo" />
                        </view>
                    </view>
                </view>
            </template>
            <template v-if="isData && isData.productInfo">
                <view class="pages_box_box_body" style="margin-top: 16rpx;">
                    <view class="pages_box_box_left">
                        <image :src="isData.productInfo[0].img" style="width: 216rpx;height: 216rpx;border-radius: 16rpx;"></image>
                    </view>
                    <view class="pages_box_box_body_right">
                        <view class="pages_box_box_body_right_title">{{isData.productInfo[0].p_name}}</view>
                            <view class="pages_box_box_body_right_price">
                                <view v-if="isData.productInfo[0].p_price" class="pages_box_box_body_right_price_jf">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{isData.productInfo[0].p_price.toFixed(2)}}</view>
                                <view class="pages_box_box_body_right_price_num">x{{isData.productInfo[0].num}}</view>
                            </view>
                        <view class="pages_box_box_body_rigth_gg">{{isData.productInfo[0].size}}</view>
                    </view>
                </view>
                <view class="pages_lb">
                    <view class="paegs_lb_box" v-if="isData.productInfo[0].spz_price">
                        <view class="paegs_lb_box_left">{{language.mystore_order.Total}}</view>
                        <view class="paegs_lb_box_right">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{isData.productInfo[0].spz_price.toFixed(2)}}</view>
                    </view>
                    <view class="paegs_lb_box">
                        <view class="paegs_lb_box_left">{{language.order.order.freight}}</view>
                        <view class="paegs_lb_box_right">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{isData.productInfo[0].z_freight?isData.productInfo[0].z_freight.toFixed(2):'0.00'}}</view>
                    </view>
                    <view class="paegs_lb_box">
                        <view class="paegs_lb_box_left">{{language.order.order.Order_notes}}</view>
                        <view class="paegs_lb_box_right">{{isData.productInfo[0].remarks}}</view>
                        <!-- <input class="pages_list_box_name_input" style="border: none;" v-model="beizhu" /> -->
                    </view>
                    <view class="paegs_lb_box" v-if="isData.productInfo[0].z_price">
                        <view class="paegs_lb_box_left">{{language.mystore_order.newT[8]}}</view>
                        <view class="paegs_lb_box_right" style="color: #FA5151;">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{isData.productInfo[0].z_price.toFixed(2)}}</view>
                    </view>
                    <view class="paegs_lb_box" v-if="isData.productInfo[0].deposit">
                        <view class="paegs_lb_box_left">{{language.mystore_order.newT[9]}}</view>
                        <view class="paegs_lb_box_right" style="color: #FA5151;">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{JSON.parse(isData.productInfo[0].deposit).toFixed(2)}}</view>
                    </view>
                    <view class="paegs_lb_box" v-if="isData.productInfo[0].balance && isData.productInfo[0].z_freight">
                        <view class="paegs_lb_box_left">{{language.mystore_order.newT[10]}}</view>
                        <view class="paegs_lb_box_right" style="color: #FA5151;">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{(isData.productInfo[0].balance+isData.productInfo[0].z_freight).toFixed(2)}}</view>
                    </view>
                </view>
                <view class="pages_lb">
                    <view class="paegs_lb_box" v-if="isData.productInfo[0].z_price">
                        <view class="paegs_lb_box_left">{{language.mystore_order.Paid_in}}</view>
                        <view class="paegs_lb_box_right"  style="color: #FA5151;">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{isData.productInfo[0].z_price.toFixed(2)}}</view>
                    </view>
                </view>
            </template>
            <view style="height: 300rpx;"></view>
            <view class="btn">
                <view class="btn_box" v-if="!setOrder">
                    <template v-if="isData.productInfo[0].status == 0 || isData.productInfo[0].status == 1">
                        <view class="btn_box_ann" @tap="_setOrder">{{language.mystore_order.edit}}</view>
                    </template>
                    <template v-if="isData.productInfo[0].status == 1">
                        <view class="btn_box_ann" @click="delivery(isData.productInfo[0].id,isData.productInfo[0].num)">{{language.mystore_order.deliver}}</view>
                    </template>
                </view>
                <view v-else>
                    <view class="btnNew" @tap="_endOrder">{{language.mystore_order.newT[11]}}</view>
                </view>
            </view>
        </view>
        <mpvue-city-picker 
            ref="mpvueCityPicker"
            :themeColor="themeColor" 
            :pickerValueDefault="cityPickerValueDefault"      
            @onConfirm="onConfirm"
        >
        </mpvue-city-picker>
	</view>
</template>

<script>
    import mpvueCityPicker from '@my-miniprogram/src/components/mpvue-citypicker/mpvueCityPicker'; 
    import {copyText} from "@/common/util";
	export default {
		data() {
			return {
				bgColor: [{
				        item: '#FA5151 '
				    },
				    {
				        item: '#D73B48'
				    }
				],
                themeColor: '#D73B48',
                cityPickerValueDefault: [0, 0, 0],
				ishead_w: 3,
				titleColor: '#ffffff',
                order_type: '',
                returnR: '',
                types: '',
                name:'',
                modle:'',
                beizhu:'',
                address:'',
                addressInfo: '',
                setOrder: false,
                page: 1,
                isData: [],
                depositPayTimeIsKs: false, //尾款支付是否开始
                depositPayTime: '07月20日 00:00:00 ', //尾款支付时间
                payTime_star: '',//订单支付倒计时
                time_c: '', //待付款倒计时，时间差
                time_D: '', //待付款倒计时，天
                time_H: '', //待付款倒计时，小时
                time_M: '', //待付款倒计时，分
                time_s: '', //待付款倒计时，秒
                setTime: '',//倒计时
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jiantou2x.png',
			}
		},
        components:{
            mpvueCityPicker
        },
        onLoad(option) {
            if(option.orderNo){
                this.orderNo = option.orderNo
            }
            if(option.type == 'bj'){
                this._setOrder()
            }
        },
        onShow() {
            //解决发货完成时，会返回此页面数据没有刷新问题
            this._axios()
        },
		methods: {
            onConfirm(e) {
                this.address = e.label
                let arr = e.label.split('-')
            },
            showMulLinkageThreePicker () {
                this.$refs.mpvueCityPicker.show()
            },
            hideKeyboard () {
                uni.hideKeyboard()
            },
			delivery(id,sum){
                uni.navigateTo({
                    url:'/pagesC/synthesize/yushou_fahuowuliu?orderDetailIds='+id+'&sNo='+this.isData.essentialInfo.orderNo+'&sum='+sum
                })
            },
            /**
             * 预售订单 复制订单号
             * @param null
             */
            _copySno(){
                copyText('', this.isData.essentialInfo.orderNo);
                uni.showToast({
                    title: this.language.goods.goodsDet.copy_success,
                    duration: 1500,
                    icon: 'none'
                });
            },
            /**
             * 预售订单 编辑订单
             * @param null
             */
            _setOrder(){
                this.setOrder = true
            },
            /**
             * 预售订单 修改订单
             * @param null
             */
            _endOrder(){
                if(this.address.length < 3){
                    uni.showToast({
                        title: '所在地区请填写正确的省市县',
                        icon: 'none'
                    })
                    return
                }
                this.setOrder = false
                let data = {
                    //api: 'app.mchPreSell.saveEditOrder',
                    api: 'plugin.presell.AppPreSell.saveEditOrder',
                    orderNo: this.orderNo,
                    userName: this.name,
                    tel: this.modle,
                    shen: this.address.substring(0,1),
                    shi: this.address.substring(1,2),
                    xian: this.address.substring(2),
                    address:this.addressInfo,
                    pageNo: this.page,
                    pageSize: 10,
                }
                this.$req.post({data}).then(res=>{
                    if(res.code == 200){
                        this._axios()
                        uni.showToast({
                            title: '修改成功',
                            icon: 'none'
                        })
                    } else {
                    }
                })
            },
            /**
             * 预售订单 数据初始化
             * @param null
             */
            _axios(){
                let data = {
                    //api: 'app.mchPreSell.orderDetail',
                    api: 'plugin.presell.AppPreSell.orderDetail',
                    orderNo: this.orderNo,
                    pageNo: this.page,
                    pageSize: 10,
                }
                this.$req.post({data}).then(res=>{
                    if(res.code == 200){
                        this.isData = res.data
                        // 订单失效时间
                        let isEndTime = res.data.essentialInfo.order_failure | 0 
                        if(this.isData.essentialInfo.addTime){
                            const {essentialInfo} = this.isData
                            
                            let date3 = new Date()
                            let year1 = date3.getFullYear();
                            let month1 = date3.getMonth() + 1;
                            let day1 = date3.getDate();
                            let hour1 = date3.getHours();
                            let minute1 = date3.getMinutes();
                            let second1 = date3.getSeconds();
                            //兼容苹果手机浏览器，格式为'/'
                            const nowTime = Date.parse(year1 + '/' + month1 + '/' + day1 + ' ' + hour1 + ':' + minute1 + ':' + second1)
                            
                            let date_start = essentialInfo.endTime.substr(0, 10).split('-').join('/');
                            let time_start =  essentialInfo.endTime.substr(10);   
                            const StartTimes = Date.parse(date_start + time_start) 
                            
                            let startDay= essentialInfo.endTime.substring(5,7)
                            let day2= essentialInfo.endTime.substring(8,10)
                            // 尾款开始时间 ， 当前时间
                            if (StartTimes > nowTime) { //还没开始付尾款
                             this.depositPayTimeIsKs = true
                            this.depositPayTime=startDay+'月'+day2+'日'+''+time_start
                            } else {
                              // 尾款支付倒计时开始
                              this._isTime(essentialInfo.endTime, 24)
                            }
                            
                        }else{
                            this._isTime(res.data.essentialInfo.addTime, isEndTime)
                        }
                        this.name = res.data.consigneeInfo.consignee
                        this.modle = res.data.consigneeInfo.phone
                        this.address = res.data.consigneeInfo.sheng + res.data.consigneeInfo.shi + res.data.consigneeInfo.xian
                        this.addressInfo = res.data.consigneeInfo.address
                        
                        //尾款支付时间
                        // let DJPayTime = res.data.essentialInfo.depositPayTime
                        // this.depositPayTime = DJPayTime.substring(5,7) + '月' + DJPayTime.substring(8,10) + '日' + DJPayTime.substring(10) + ' '
                        // //获取当前时间戳
                        // let aTime = new Date().getTime();
                        // //获取尾款支付时间戳
                        // let bTime = Date.parse(DJPayTime)
                        // //判断是否开始支付尾款
                        // if(aTime>bTime){
                        //     this.depositPayTimeIsKs = false
                        // } else {
                        //     this.depositPayTimeIsKs = true
                        // }
                    } else {
                    }
                })
            },
            /**
             * 预售订单 倒计时 
             * @param null
             */
            _isTime(add_time, order_failure){
                let me = this
                let time = add_time
                let Htime = time.split(' ')
                let Dtime = Htime[0].replace('-', '/')
                let Time = Dtime.replace('-', '/')
                let start = Time + ',' + Htime[1]
                let startTime = new Date(start)
                let endTime = startTime.getTime() + (order_failure * 60 * 60 * 1000)
                me.setTime = setInterval(function() {
                    me._time(endTime)
                }, 1000)
            },
            /**
             * 预售订单 时间计算
             * @param null
             */
            _time(endTime) {
                let me = this
                let myDate = new Date()
                me.time_c = parseInt((endTime - myDate.getTime()) / 1000) //得到剩余的毫秒数
                if (me.time_c >= 0) {
                    me.time_D = Math.floor(me.time_c / 60 / 60 / 24) //得到天
                    me.time_H = Math.floor(me.time_c / (60 * 60) % 24) //得到小时 取模24小时
                    me.time_M = Math.floor(me.time_c / 60 % 60) //得到分钟
                    me.time_S = Math.floor(me.time_c % 60) //得到秒数
                    me.payTime_star = `${me.time_H}`+me.language.order.order.Time+`${me.time_M}`+me.language.order.order.branch+`${me.time_S}`+'秒'
                } else {
                    clearInterval(me.setTime)
                }
            },
		}
	}
</script>
<style>
    page{
        background-color: #f4f5f6;
    }
</style>
<style scoped lang="less">
    @import url('@/laike.less');
    .jiantou {
        width: 32rpx;
        height: 44rpx;
    }
    ._input{
        padding: 16rpx 20rpx;
        border: 1rpx solid #eee;
        display: flex;
        align-items: center;
        flex: 1;
        border-radius: 16rpx;
        justify-content: space-between;
        margin-right: 14rpx;
        >input{
            flex: 1;
        }
    }
    .btn_box_ann{
        width: 176rpx;
        height: 72rpx;
        line-height: 72rpx;
        text-align: center;
        font-size: 28rpx;
        
        font-weight: normal;
        color: #333333;
        margin-right: 22rpx;
        background-color: #F4F5F6;
        border-radius: 52rpx;
        
    }
    .btn_box{
        display: flex;
        justify-content: flex-end;
        width: 100%;
        margin-right: 32rpx;
    }
    .dizhi_dh_name{
        margin-right: 10rpx;
    }
    .dizhi_dh{
        display: flex;
        font-size: 28rpx;
         
        font-weight: normal;
        color: #333333;
    }
    .dizhi_xxhs{
        font-size: 28rpx;
         
        font-weight: normal;
        margin-top: 10rpx;
        margin-bottom: 18rpx;
        color: #999999;
    }
    .dizhi_xx{
        font-size: 32rpx;
        
        font-weight: normal;
        color: #333333;
    }
    .dizhi{
        width: 100%;
        padding: 32rpx;
        box-sizing: border-box;
        margin-top: 8rpx;
        border-radius: 24rpx;
        background-color: #fff;
    }
    .bottom {
        width: 686rpx;
        height: 92rpx;
        font-size: 32rpx;
        color: #fff;
        text-align: center;
        line-height: 92rpx;
        border-radius: 52rpx;
        margin: 0 auto;
        padding: 0;
        .solidBtn()
    }
    .btn {
        position: fixed;
        bottom: 0;
        width: 100%;
        height: 124rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #ffffff;
        box-shadow: 0 0 0 0 rgba(0, 0, 0, 0.2);
        padding-bottom: constant(safe-area-inset-bottom);
        /* 兼容 iOS < 11.2 */
        padding-bottom: env(safe-area-inset-bottom);
        .btnNew{
            width: 686rpx;
            height: 92rpx;
            color: #ffffff;
            display: flex;
            align-items: center;
            justify-content: center;
            background: linear-gradient(270deg, #FF6F6F 0%, #FA5151 100%);
            border-radius: 48rpx;
        }
    }
    .paegs_lb_box_left{
        font-size: 32rpx;
         
        font-weight: normal;
        color: #333333;
    }
    .paegs_lb_box{
        margin-left: 32rpx;
        margin-right: 32rpx;
        height: 108rpx;
        display: flex;
        justify-content: space-between;
        align-items: center;
        border-bottom: 1rpx solid #eee;
    }
    .pages_lb{
        width: 100%;
        display: flex;
        flex-direction: column;
        background-color: #FFFFFF;
        border-radius: 24rpx;
        margin-top: 24rpx;
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
        margin-top: 34rpx;
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
        font-size: 32rpx;
         
        font-weight: normal;
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
        width: 446rpx;
        margin-left: 24rpx;
        display: flex;
        flex-direction: column;
    }
    
    .pages_box_box_left {
        width: 216rpx;
        height: 216rpx;
    }
    
    .pages_box_box_body {
        margin-top: 26rpx;
        display: flex;
        background-color: #FFFFFF;
        border-radius: 24rpx;
        padding: 32rpx;
    }
    .pages_list_box_name_input{
        // margin-right: 16rpx;
        padding-right: 16rpx;
        height: 76rpx;
        border: 1rpx solid #eee;
        width: 495rpx;
        border-radius: 16rpx;
        // text-align: end;
    }
    .pages_list_box_name_txt{
        font-size: 32rpx;
        //margin-right: 28rpx;
         
        font-weight: normal;
        color: #333333;
        line-height: 76rpx;
        width: 168rpx;
    }
    .pages_list_box_name{
        display: flex;
        height: 76rpx;
        line-height: 76rpx;
        width: 100%;
        margin-bottom: 24rpx;
    }
    .pages_list_box{
        padding: 32rpx;
        border-radius: 24rpx;
        background-color: #FFFFFF;
        display: flex;
        flex-direction: column;
    }
    .pages_list{
        width: 100%;
        margin-top: 24rpx;
        display: flex;
        flex-direction: column;
    }
    .pages_top_time{
        margin-left: 48rpx;
        margin-top: 18rpx;
        font-size: 32rpx;
         
        font-weight: normal;
        color: rgba(255,255,255,0.9);
    }
    .pages_top_ddh_right{
        font-size: 28rpx;
        margin-right: 32rpx;
        
        font-weight: normal;
        color: #FFFFFF;
    }
    .pages_top_ddh_left{
        font-size: 32rpx;
         
        font-weight: normal;
        color: rgba(255,255,255,0.9);
    }
    .pages_top_zt{
        margin-top: 48rpx;
        font-size: 48rpx;
        
        font-weight: normal;
        color: #FFFFFF;
        margin-left: 48rpx;
    }
    .pages_top_ddh{
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-left: 48rpx;
        margin-top: 18rpx;

    }
    .pages_top{
        width: 100%;
        //height: 336rpx;
        border-radius:  0 0 24rpx 24rpx;
        background: linear-gradient(90deg, #FA5151 0%, #D73B48 100%);
        display: flex;
        flex-direction: column;
        padding-bottom: 48rpx;
        box-sizing: border-box;
    }
.pages{
    width: 100%;
}
</style>
