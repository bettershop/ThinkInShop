<template>
    <view class="choose-address">
    <!-- 选择配送方式 -->
        <template>
            <view class="psfs">
                <view>配送方式</view>
                <view class="psfs-xz" @tap="_isShow">
                    <span>{{ isPsList[is_choose_index] }}</span><image :src="jiantou" mode="widthFix"></image>
                </view>
            </view>
        </template>
    <!-- 配送方式：自提 -->
        <template v-if="!is_express && shop_address.id > 0">
            <!-- 选择自提后显示 -->
            <div class="receiving">
                <!-- 选择自提店铺 -->
                <div class="afhalen_box afhalen_shop">
                    <view style="width: 638rpx; ">
                        <!-- 自提门店详细地址 -->
                        <view class="txtttile" @tap="navTo('/pagesA/myStore/storeList?shop_id='+shop_id + '&shop_address_id=' + shop_address.id)">{{shop_address.address}}</view>
                        <!-- 自提门店 省-市-县 -->
                        <view class="txtttile" @tap="navTo('/pagesA/myStore/storeList?shop_id='+shop_id + '&shop_address_id=' + shop_address.id)" style="color: #999999;font-weight: 400;margin-bottom: 16rpx;">{{shop_address.sheng}}{{shop_address.shi}}{{shop_address.xian}}</view>
                        <!-- 自提人 名称-电话 -->
                        <div class="afhalen_box yh-afhalen_box">
                            <input 
                                class="yh-afhalen-inputa" 
                                type="text" 
                                :placeholder="!value.name?language.chooseAddress.xm:''" 
                                :value="value.name" @input="onInputAddressName"/>
                            {{ value.cpc }}    
                            <input 
                                class="yh-afhalen-inputb" 
                                type="text" 
                                :placeholder="!value.tel?language.chooseAddress.shusj:''" 
                                :value="value.tel" @input="onInputAddressTel"/>
                        </div>
                    </view>
                    <!-- 去修改自提门店 -->
                    <view class="arrow_box"  @tap="navTo('/pagesA/myStore/storeList?shop_id='+shop_id + '&shop_address_id=' + shop_address.id)">
                        <img :src="jiantou" class='arrow' style="width: 32rpx;height: 44rpx;margin-top: 12rpx;" />
                    </view>
                </div>
            </div>
            <!-- 没有自提门店地址 -->
            <div @tap="navTo('/pagesA/myStore/storeList?shop_id='+shop_id)" class='address_two yh-address_two' v-if="!is_express&&shop_address.id === 0">
                <span>{{language.chooseAddress.selectStore}}</span>
            </div>
        </template>
    <!-- 配送方式：物流（快递） -->
        <template v-if='is_express'>
            <!-- 没有收货地址 -->
            <div class="wechat-not-address" v-if='is_express && !address_status'>
                <!-- 去添加收货地址 -->
                <div @tap="navTo('/pagesB/address/addAddress?state_addre=1&addNum=0')" class='add-address'>
                    <image :src="add_rise" />
                    <text>{{language.chooseAddress.addAddress}}</text>
                </div>
                <!-- #ifdef MP-WEIXIN -->
                <!-- 去获取微信收货地址 -->
                <div class="wechat-address-add" @click="chooseAddress">
                    <image :src="add1" />
                    <text>{{language.chooseAddress.autoAddAddress}}</text>
                </div>
                <!-- #endif -->
            </div>
            <!-- 选择快递后,显示地址 -->
            <div @tap="goChooseAddress" class='address_one' v-else>
                <div class="yh-one-box">
                    <p>{{value.address}}</p>
                    <p style="color: #999999;font-weight:400;margin-bottom: 16rpx;">{{value.sheng}}{{value.city}}{{value.quyu}}</p>
                    <span class="yh-one-spana">{{value.name}}</span>
                    <span class="yh-one-spanb">{{value.tel}}</span>
                </div>
                <view class="img_box">
                    <img :src="jiantou" class='arrow' />
                </view>
            </div>
        </template>
    <!-- 配送方式：商家配送 -->
        <template v-if='is_express && zpStatus && is_choose_type == 2'>
            <view class="sjps">
                <view>配送时间(选填)</view>
                <view class="sjps-time">
                    <!-- 未选择配送时间 -->
                    <view class="sjps-time-no" v-if="!virtualTimeEnd" @tap="virtualTime = true">
                        <span>请选择配送时间</span><image :src="jiantou" mode="widthFix"></image>
                    </view>
                    <!-- 已选择配送时间 -->
                    <view class="sjps-time-yes" v-else @tap="virtualTime = true">
                        <span>{{virtualTimeEnd}}</span><image :src="jiantou" mode="widthFix"></image>
                    </view>
                </view>
            </view>
        </template>
    <!-- 切换配送方式弹窗 -->
        <chooseS 
            ref="isLanguage" 
            :is_choose_obj='is_choose_obj' 
            :is_type="chooseType" 
            :is_choose="isPsList"
             @_choose="_choose" 
             @_isHide="_isHide">
        </chooseS>
    <!-- 商家配送 选择配送时间弹窗 -->
        <view class="virtual-time" v-if="virtualTime" @tap="_virtualTimeEnd" @touchmove.stop.prevent>
            <view @tap.stop class="safe-area-inset-bottom">
                <view>
                    选择配送时间
                    <image :src="guanbi" @tap="_virtualTimeEnd('quxiao')"></image>
                </view>
                <view @touchmove.stop>
                    <view>
                        <view @tap="_virtualTime(index)" v-for="(item, index) in storeHxTime" :class="item.choose?'virtual-first-active':''">{{item.time}}</view>
                    </view>
                    <view>
                        <view class="chooseTime" @tap="_virtualTimeKey('top')" :class="{virtualLastActive:storeHxTime[storeHxTimeIndex].value.top}">
                            <span>上午</span>
                            <span>9:00～12:00</span>
                        </view>
                        <view class="chooseTime" @tap="_virtualTimeKey('bottom')" :class="{virtualLastActive:storeHxTime[storeHxTimeIndex].value.bottom}">
                            <span>下午</span>
                            <span>14:00～18:00</span>
                        </view>
                        <view class="noTime" v-if="0">
                            <image mode="widthFix" :src="noTime"></image>
                            <span>暂无可配送时间</span>
                        </view>
                    </view>
                </view>
            </view>
        </view>
    </view>
</template>

<script>
    import chooseS from "@/components/aComponents/choose.vue"
    import {
        later
    } from '@/common/util'
    /**
     * choose-address 选择地址业务组件
     * @description 用于订单确认页选择门店和收货地址
     * @property {Boolean} shop_status 是否可以选择门店（默认false）
     * @property {Number | String} shop_id 门店id（默认 0）
     * @property {String | Number} size 开关尺寸，单位rpx（默认50）
     * @property {Object} shop_address 门店地址 
     * @property {Object} value 收货人地址
     * @property {Boolean } address_status 是否设置了默认地址（默认false）
     * @property {Boolean} is_express 是否使用快递方式配送（默认true）
     * @event {Function} change 在switch打开或关闭时触发
     * @event {Function} input 修改地址是触发
     * @example <choose-address
             :shop_address="shop_list"
             :shop_id="pro[0].shop_id"
             :shop_status="shop_status === 1"
             v-model="address"
             :address_status="adds_f"
             :is_express="is_express"
             @change="sChange"
             />

            pagesE/pay/orderDetailsr.vue
     */
    export default {
        name: 'choose-address',
        data() {
            return {
                guanbi: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/guanbi2x.png',
                noTime: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/zwyysj.png',
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jiantou2x.png',
                add: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/add_address.png',
                add1: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/get_wechat_address.png',
                add_rise: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/add_rise.png',
                chooseType: 0, //选择配送弹窗
                is_choose_obj:{
                    title: '选择配送方式',
                    colorLeft: '#999999',
                    colorRight: '#FA5151',
                    background: '#F4F5F6',//显示图标
                    borderRadius: '24rpx 24rpx 0 0',//提示文字
                },
                is_choose_index: 0,//选择的配送方式
                is_choose_type: '',//记录选择的类型
                isPsList: ['快递配送'],
                virtualTime: false,
                storeHxTime:[],
                storeHxTimeIndex: 0,
                virtualTimeEnd: '',//最终显示
            }
        },
        components:{
            chooseS,
        },
        props: {
            shop_status: {
                default: false,
                type: Boolean
            },
            all_pro: {
                default: '',
                type: [String, Array]
            },
            // 操又是一个接口不规范
            shop_id: {
                default: 0,
                type: [Number, String]
            },
            // 有毒，接口返回为空的时候是个数组，有数据的时候是个对象
            shop_address: {
                default: {
                    address: '',
                    id: 0,
                    mobile: '',
                    name: ''
                },
                type: [Object, Array]
            },
            value: {
                default: {
                    name: '',
                    tel: '',
                    address_xq: '',
                    id: 0
                },
                type: Object
            },
            address_status: {
                default: false,
                type: Boolean
            },
            is_express: {
                default: 0,
                type: [Number, String]
            },
            is_supplier_pro: {
                default: true,
                type: Boolean
            },
            zpStatus: {
                default: false,
                type: Boolean
            },
        },
        created() {
            if(this.shop_status && this.is_supplier_pro){
                this.isPsList.push('上门自提')
            }
            if(this.zpStatus){
                this.isPsList.push('商家配送')
            }
            this.setLang();
        },
        mounted() {
            if(this.zpStatus){
                for(var a = 1; a<8; a++){
                    this.getTime(a)
                }
                console.log(this.storeHxTime)
            }
        },
        computed: {},
        methods: {
            //商家配送时间（当前至往后7天时间列表）
            getTime(index){
                var nowDate = new Date();
                nowDate.setDate(nowDate.getDate() + index);
                var yy = nowDate.getFullYear();
                var mm = nowDate.getMonth() + 1;
                var dd = nowDate.getDate();
                var hh = nowDate.getHours();
                var mmi = nowDate.getMinutes();
                var ss = nowDate.getSeconds();
                var endD = yy + "-" + mm + "-" + dd
                //console.log('获取往后7天时间列表～', endD);
                let obj = {
                    time: '',
                    choose: false,
                    value: {
                        top: false,
                        bottom: false
                    }
                }
                obj.time = endD
                if(index == 1){
                   obj.choose = true
                   obj.value.top = true
                }
                this.storeHxTime.push(obj)
                return endD
            },
            //选择商家配送日期
            _virtualTime(index){
                this.storeHxTime.forEach((it, ix)=>{
                    if(ix == index){
                        it.choose = true
                    } else {
                        it.choose = false
                    }
                })
                this.storeHxTimeIndex = index
            },
            //选择商家配送时间段
            _virtualTimeKey(item){
                if(0){return console.log('此时间段无法预约～')}
                this.storeHxTime.forEach((it, ix)=>{
                    if(ix == this.storeHxTimeIndex){
                        if(item == 'top'){
                            it.value.top = true
                        } else {
                            it.value.top = false
                        }
                        if(item == 'bottom'){
                            it.value.bottom = true
                        } else {
                            it.value.bottom = false
                        }
                    } else {
                        it.value.top = false
                        it.value.bottom = false
                    }
                })
                this._virtualTimeEnd()
            },
            //选择商家配送 最终传值
            _virtualTimeEnd(type){
                this.virtualTime = false
                let deliveryTime = ''
                let deliveryPeriod = ''
                if(type == 'quxiao'){
                    this.virtualTimeEnd = false
                    return this.$emit('sjps', deliveryTime, deliveryPeriod)
                } else {
                    this.storeHxTime.forEach((item, index)=>{
                        let it = item.value
                        for (let key in it){
                            if(it[key]){
                                deliveryTime = item.time
                                deliveryPeriod = (key == 'bottom' ? 2 : 1)
                            }
                        }
                    })
                    this.$emit('sjps', deliveryTime, deliveryPeriod)
                    if(deliveryTime && deliveryPeriod){
                        this.virtualTimeEnd = deliveryTime + '  ' + (deliveryPeriod == 1 ? '上午' : '下午')
                    }
                }
            },
            //显示
            _isShow(){
                this.chooseType = 2
            },
            //隐藏
            _isHide(){
                this.chooseType = 0
            },
            //选择配送方式 
            _choose(index){
                //当前组件的配送方式：取值 0快递配送 1上门自提 2商家配送
                this.is_choose_index = index
                //传给父组件的配送方式：取值 0自提 1快递 2商家配送...??
                let type = ''
                switch (this.isPsList[index]){
                    case '快递配送':
                        type = 1
                        break
                    case '上门自提':
                        type = 0
                        break
                    case '商家配送':
                        type = 2
                        break
                    default:
                        type = 1
                        break
                }
                this.is_choose_type = type
                this.$emit('change', type)
                this._isHide()
            },
            /**
             * 去修改收货地址
             */
            goChooseAddress() {
                var pro_id = this.all_pro && this.all_pro.length ? this.all_pro.join(",") : '';
                this.navTo('/pagesB/address/receivingAddress?state_manage=1&addre_id=' + this.value.id + '&product=' +  pro_id)
            },
            /**
             * 去获取微信收货地址
             */
            chooseAddress() {
                uni.chooseAddress({
                    success: async res => {
                        console.log(res)
                        await this.insertAddress(res)
                        this.$emit('show')
                    }
                })
            },
            async insertAddress(res) {
                var data = {
                    api: 'app.address.SaveAddress',
                    user_name: res.userName,
                    mobile: res.telNumber,
                    place: res.provinceName + '-' + res.cityName + '-' + res.countyName,
                    is_default: 1,
                    address: res.detailInfo
                }
                let { address_id } = await this.$req.post({ data })
                this.$store.state.address_id_def = address_id
                this.$emit('change', 'AddrChange'); 
                uni.setStorageSync('lkt_address_id_def', address_id)
            },
            //自提人信息
            onInputAddressName(e) {
                this.$emit('input', {
                    name: e.detail.value,
                    tel: this.value.tel,
                    address_xq: this.value.address_xq,
                    id: this.value.id
                })
            },
            //自提人信息
            onInputAddressTel(e) {
                this.$emit('input', {
                    name: this.value.name,
                    tel: e.detail.value,
                    address_xq: this.value.address_xq,
                    id: this.value.id
                })
            },
        }
    }
</script>
<style scoped>  
   .psfs{
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 32rpx 0;
        box-sizing: border-box;
        margin: 0 32rpx;
        border-bottom: 2rpx solid rgba(0, 0, 0, .1);  
    }
    .psfs view:first-child{
        font-size: 32rpx;
        color: #333333;
    } 
    
    .psfs .psfs-xz{
        display: flex;
        align-items: center;
        justify-content: space-between; 
    }
    .psfs .psfs-xz > span{
         font-size: 28rpx;
         color: #333333;
         margin-right: 8rpx;
     }
    .psfs .psfs-xz > image{
         width: 32rpx;
     }
    
    /* Less /deep/ 转换为 CSS ::v-deep（兼容小程序/uniapp） */
    ::v-deep .sjps{
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 32rpx 0;
        box-sizing: border-box;
        margin: 0 32rpx;
        border-top: 2rpx solid rgba(0, 0, 0, .1);
    }
    .sjps > view:first-child{
        font-size: 32rpx;
        color: #333333;
    }
    
    .sjps-time{
        display: flex;
    }
    .sjps-time > view {
        display: flex;
        align-items: center;
        justify-content: space-between;
    }
    .sjps-time > view > span{
        font-size: 28rpx;
        color: #999999;
        margin-right: 8rpx;
    }
    .sjps-time > view > image{
        width: 32rpx;
    }
    
    .sjps-time-no{}
    
    .sjps-time-yes{}
    
    .virtual-time{
        position: fixed;
        top: 0;
        bottom: 0;
        left: 0;
        right: 0;
        background-color: rgba(0, 0, 0, .5);
        z-index: 9999;
        display: flex;
        flex-direction: column;
        justify-content: flex-end; 
    }
    .virtual-time > view {
        
    }
    .virtual-time > view > view:nth-child(2){
        height: 600rpx;
        background-color: #ffffff;
        display: flex;
        align-items: center;
        justify-content: center;
    }
    .virtual-time > view > view:nth-child(2) > view:first-child{
        width: 224rpx;
        height: 600rpx;
        overflow-y: auto;
        background-color: #F4F5F6;
    }
    .virtual-time > view > view:nth-child(2) > view:first-child > view{
        padding: 32rpx;
        box-sizing: border-box;
        font-size: 28rpx;
        color: #666666;
        text-align: center;
    }
    .virtual-time > view > view:nth-child(2) > view:first-child .virtual-first-active{
        background: #FFFFFF;
        font-weight: 500;
        font-size: 28rpx;
        color: #FA5151;
    }
    .virtual-time > view > view:nth-child(2) > view:last-child{
        display: flex;
        flex-direction: column;
        flex: 1;
        height: 600rpx;
        overflow-y: auto;
        padding: 32rpx;
        box-sizing: border-box;
    }
    .virtual-time > view > view:nth-child(2) > view:last-child .chooseTime{
        width: 100%;
        height: max-content;
        padding: 16rpx 0;
        box-sizing: border-box;
        border-radius: 16rpx;
        border: 2rpx solid rgba(0,0,0,0.1);
        display: inline-block;
        text-align: center;
        margin-bottom: 30rpx;
        display: flex;
        flex-direction: column;
    }
    .virtual-time > view > view:nth-child(2) > view:last-child .chooseTime > span:first-child{
        width: 100%;
        font-weight: 500;
        font-size: 32rpx;
        color: #666666;
    }
    .virtual-time > view > view:nth-child(2) > view:last-child .chooseTime > span:last-child{
        width: 100%;
        font-size: 28rpx;
        color: #666666;
        margin-top: 8rpx;
    }
    .virtual-time > view > view:nth-child(2) > view:last-child .noTime{
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
    }
    .virtual-time > view > view:nth-child(2) > view:last-child .noTime > image{
        width: 100%;
    }
    .virtual-time > view > view:nth-child(2) > view:last-child .noTime > span{
        font-size: 28rpx;
        color: #333333;
    }
    .virtual-time > view > view:nth-child(2) > view:last-child .virtualLastActive{
        border: 2rpx solid #FA5151;
        background-color: rgba(250,81,81,0.1);
    }
    .virtual-time > view > view:nth-child(2) > view:last-child .virtualLastActive > span:first-child{
        color: #FA5151;
    }
    .virtual-time > view > view:nth-child(2) > view:last-child .virtualLastActive > span:last-child{
        color: #FA5151;
    }
    .virtual-time > view > view:nth-child(2) > view:last-child .virtualLastOver{
        background-color: #F4F5F6;
    }
    .virtual-time > view > view:nth-child(2) > view:last-child .virtualLastOver > span:first-child{
        color: #999999;
    }
    .virtual-time > view > view:nth-child(2) > view:last-child .virtualLastOver > span:last-child{
        color: #999999;
    }
    .virtual-time > view > view:nth-child(3){
        min-height: 20rpx;
        background-color: #ffffff;
        box-sizing: border-box;
    }
    .virtual-time > view > view:nth-child(1){
        padding: 40rpx;
        box-sizing: border-box;
        background-color: #F4F5F6;
        border-radius: 24rpx 24rpx 0 0;
        text-align: center;
        font-size: 40rpx;
        color: #333333;
        position: relative;
    }
    .virtual-time > view > view:nth-child(1) > image{
        width: 28rpx;
        height: 28rpx;
        position: absolute;
        top: 40rpx;
        right: 40rpx;
    }
    .img_box {
        display: flex;
        height: 50rpx;
    }

    .Receiving_left {
        width: 346rpx;
        height: 56rpx;
        display: flex;
        justify-content: center;
        color: #333333;
        font-size: 26rpx;
        align-items: center;
    }

    .afhalen_shop {
        display: flex;
        justify-content: center;
        align-items: center;
    }

    .Receiving_right {
        width: 346rpx;
        height: 56rpx;
        display: flex;
        justify-content: center;
        color: #333333;
        font-size: 26rpx;
        align-items: center;
        background-color: #fff;
        border-radius: 28rpx;
    }

    .Receiving {
        width: 702rpx;
        height: 64rpx;
        display: flex;
        justify-content: center;
        align-items: center;
        border-radius: 32rpx;
        background-color: #f4f5f6;
        margin-left: 24rpx;
    }

    .choose-address {
        background-color: #fff;
        /* #ifdef MP-WEIXIN */
        border-radius: 0 0 24rpx 24rpx; 
        /* #endif */
    }
    .choose-address .afhalen_box {
        display: flex;
        justify-content: space-between;
        align-items: center;
        width: 100%;
        padding: 30rpx;
        color: #020202;
        font-size: 28rpx;
    }
    .choose-address .afhalen_box .switch-box {
        display: flex;
        justify-content: space-around;
        align-items: center;
        font-size: 24rpx;
        position: relative;
        width: 144rpx;
        height: 60rpx;
        border-radius: 30rpx;
        color: #fff;
        background-color: #4CD864;
    }
    .choose-address .afhalen_box .switch-box .switch-item {
        position: absolute;
        top: 2rpx;
        left: 0;
        width: 72rpx;
        height: 56rpx;
        border-radius: 28rpx;
        background: rgba(255, 255, 255, 1);
        box-shadow: -1px 5px 5px 0px rgba(13, 4, 8, 0.15);
        transition: transform .3s;
    }
    .choose-address .afhalen_box input {
        height: 60rpx;
        font-size: 26rpx;
        color: #242424;
    } 
    .choose-address .arrow_box {
        display: flex;
        height: 102rpx;
    }
    .choose-address .txtttile {
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        width: 638rpx;
        font-size: 16px;
        color: #333333;
        margin-bottom: 4px;
        font-weight: 500;
    }
    .choose-address .receiving {
        width: 100%;
    }
    .choose-address .receiving .afhalen_shop img {
        width: 32rpx;
        height: 44rpx;
    }
    .choose-address .receiving .afhalen_shop text {
        font-weight: 500;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        width: 638rpx;
        color: #333333;
        margin-bottom: 12px;
    }
    .choose-address .receiving .yh-afhalen_box {
        justify-content: flex-start;
        padding: 0rpx 0 0rpx 0rpx;
    }
    .choose-address .receiving .yh-afhalen_box .yh-afhalen-inputa {
        width: 130rpx;
        font-size: 28rpx;
        border: 1rpx solid #eee;
        height: 56rpx;
        padding: 4rpx 16rpx;
        border-radius: 16rpx;
    }
    .choose-address .receiving .yh-afhalen_box .yh-afhalen-inputb {
        flex: 1;
        font-size: 28rpx;
        margin-left: 16rpx;
        border: 1rpx solid #eee;
        height: 56rpx;
        padding: 4rpx 16rpx;
        border-radius: 16rpx;
    }
    .choose-address > .address_two {
        height: 90rpx;
        text-align: center;
        line-height: 90rpx;
        font-size: 30rpx;
        color: #888;
    }
    .choose-address > .address_two.yh-address_two {
        text-align: left;
        padding-left: 15px;
    }
    .choose-address .address_one {
        display: flex;
        flex-flow: row nowrap;
        justify-content: space-between;
        align-items: center;
        padding: 32rpx;
        box-sizing: border-box;
        padding-top: 24rpx;
        font-size: 28rpx;
        color: #020202;
    }
    .choose-address .address_one .yh-one-box {
        width: 90%;
    }
    .choose-address .address_one .yh-one-box .yh-one-spana {
        font-size: 28rpx;
        color: #333333;
        margin-right: 30rpx;
    }
    .choose-address .address_one .yh-one-box .yh-one-spanb {
        font-size: 28rpx;
        color: #333333;
    }
    .choose-address .address_one p {
        overflow: hidden; /*多出的隐藏*/
        text-overflow: ellipsis; /*多出部分用...代替*/
        display: -webkit-box; /*定义为盒子模型显示*/
        -webkit-line-clamp: 2; /*用来限制在一个块元素显示的文本的行数*/
        -webkit-box-orient: vertical; /*从上到下垂直排列子元素（设置伸缩盒子的子元素排列方式）*/
        font-weight: 500;
        font-size: 32rpx;
        color: #333333;
        margin-bottom: 8rpx;
    }
    .choose-address .address_one .arrow {
        width: 32rpx;
        height: 44rpx;
        vertical-align: sub;
        position: relative;
    }
    .choose-address .wechat-not-address {
        display: flex;
        height: 200rpx;
        background: #FFF;
        text-align: center;
        font-size: 24rpx;
        color: #666;
        justify-content: center;
        align-items: center;
    }
    .choose-address .wechat-not-address .add-address {
        height: 140rpx;
        width: 336rpx;
        background: rgba(250, 81, 81, 0.1);
        border-radius: 16rpx;
        display: flex;
        justify-content: center;
        margin-right: 22rpx;
        align-items: center;
        color: #FA5151;
    }
    .choose-address .wechat-not-address image {
        width: 48rpx;
        height: 48rpx;
        display: block;
        margin-right: 16rpx;
    }
    .choose-address .wechat-not-address text {
        width: 192rpx;
        height: 44rpx;
        font-size: 32rpx;
        font-weight: 500;
        line-height: 44rpx;
    }
    .choose-address .wechat-not-address .space {
        width: 67rpx;
    }

    .wechat-address-add {
        width: 336rpx;
        height: 140rpx;
        border-radius: 16rpx;
        background: rgba(7, 193, 96, 0.1);
        display: flex;
        justify-content: center;
        align-items: center;
        color: #07C160;
    }
</style>
