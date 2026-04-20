<template>
    <div class="container">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="language.storeMyOrder.title" :returnR="returnR"  ishead_w="2" :bgColor="bgColor" titleColor="#333333"></heads>
        <view style="height: 120rpx;">
            <div class="position_head">
                <div class='home_navigation'>
                    <div class='home_input'>
                        <img class='searchImg' :src="serchimg" alt="">
                        <input type="text" v-model="searchtxt" :placeholder="language.storeMyOrder.searchPlace" placeholder-style="color:#999999;font-size: 26upx" name="sourch"/>
                        <image v-show="searchtxt.length > 0" @click="searchtxt = ''" class="cancel" :src="sc_icon" mode=""></image>
                    </div>
                    <div class='search_btn' @tap='_search'>{{language.search2.search.search}}</div>
                </div>
            </div>
        </view>
        <!-- 导航栏 -->
        <switchNavOne 
            ref="switchNavOne" 
            :is_switchNav="header" :is_switchNav_radius="'0 0 0 0'"
            :is_switchNav_padT="'0'" :is_switchNav_padB="'24rpx'" 
            @choose="_header_index">
        </switchNavOne>
        <!-- 标签 -->
        <switchNavTwo 
            ref="switchNavTwo"
            :is_switchLable="header_label" 
            @choose="_header_label_index">
        </switchNavTwo>
        <div class="relative">
            <div v-if="list.length > 0">
                <div class="orderlist-line"></div>
                <div v-for="(item, index) in list" :key="index">                    
                    <div class="orderList" v-for="(items, indexs) in item.res" :key="indexs">
                        <div class="orderTitle">
                            <div class='order-id' @tap='onCopy( items.sNo)'>{{language.storeMyOrder.orderNo}} {{ items.sNo }}</div>
                            <div v-if="order_type == 'return'">
                                {{items.list[0].prompt}}
                            </div>
                            <div v-else>
                                <span v-if="items.status == 0">{{language.storeMyOrder.obligation}}</span>
                                <span v-else-if="items.status == 1 && (items.self_lifting == '0'|| items.self_lifting == '2')">{{language.storeMyOrder.waitSend}}</span>
                                <span v-else-if="items.status == 2">{{language.storeMyOrder.waitReceiving}}</span>
                                <span v-else-if="items.status == 5">{{language.storeMyOrder.done}}</span>
                                <span v-else-if="items.status == 7">{{language.storeMyOrder.orderClosed}}</span>
                                <span v-else-if="items.status == 8">待核销</span>
                                <span v-else>
                                    <span v-if="items.list[0].r_type == 0">{{language.storeMyOrder.returnReview}}</span>
                                    <span v-else-if="items.list[0].r_type == 1 && items.list[0].re_type == 3">{{language.storeMyOrder.agreeReplacement}}</span>
                                    <span v-else-if="items.list[0].r_type == 1">{{language.storeMyOrder.agreeReturn}}</span>
                                    <span v-else-if="items.list[0].r_type == 2">{{language.storeMyOrder.refusedReturn}}</span>
                                    <span v-else-if="items.list[0].r_type == 3">{{language.storeMyOrder.receiveMerchant}}</span>
                                    <span v-else-if="items.list[0].r_type == 4">{{language.storeMyOrder.agreeRefund}}</span>
                                    <span v-else-if="items.list[0].r_type == 5">{{language.storeMyOrder.rejectReturn}}</span>
                                    <span v-else-if="items.list[0].r_type == 8">{{language.storeMyOrder.refusedRefund}}</span>
                                    <span v-else-if="items.list[0].r_type == 9">{{language.storeMyOrder.agreeAndRefund}}</span>
                                    <span v-else-if="items.list[0].r_type == 11">{{language.storeMyOrder.sendMerchandise}}</span>
                                    <span v-else-if="items.list[0].r_type == 12">{{language.storeMyOrder.afterComplete}}</span>
                                </span>
                            </div>
                        </div>
                        <div class="proList" v-for="(itemList, indexList) in items.list" :key="indexList">
                            <div class="proListUp ml_60" @tap="_changeDetail(items.sNo)" v-if="items.status == 1 && items.list.length > 1 && (items.fhRadios && fhRadios)">
                                <div class="fhRadios">
                                    <label class="radio" v-if="itemList.r_status == 1">
                                        <radio class="radio_color" value="1" @tap.stop="_changeFh(itemList.id, index, indexs, indexList)" :checked="itemList.status" />
                                    </label>
                                </div>
                                <div class="proListUpLeft"><img :src="itemList.imgurl==''?ErrorImg:itemList.imgurl" @error="handleErrorImg(index,indexList)" /></div>
                                <div class="proListUpRight">
                                    <div class="proTitle">
                                        <span v-if="itemList.otype == 'pt' || itemList.otype == 'PP'" class="ptCrl">{{language.storeMyOrder.pt}}</span>
                                        <span v-else-if="itemList.otype == 'KJ'" class="ptCrl KJ_color">{{language.storeMyOrder.kj}}</span>
                                        <span v-else-if="itemList.otype == 'JP'" class="ptCrl JP_color">{{language.storeMyOrder.jp}}</span>
                                        <span v-else-if="itemList.otype == 'FX'" class="ptCrl FX_color">{{language.storeMyOrder.fx}}</span>
                                        <span v-else-if="items.self_lifting == '1'" class="ptCrl ZT_color">{{language.storeMyOrder.zt}}</span>
                                        <span v-else-if="itemList.otype == 'MS' ||  itemList.otype == 'PM'" class="ptCrl MS_color">{{language.storeMyOrder.seckill}}</span>
                                        
                                        {{ itemList.p_name }}
                                    </div>
                                    <div class="proPro myflex">{{ itemList.size }} <span style="color:#FA9D3B" v-if='itemList.isReturn'>退款成功</span></div>
                                    
                                    <div class="proSellData">
                                        <div class="sellMoney">
                                            {{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(itemList.p_price) }} 
                                            <span v-if="items.allow > 0">
                                                +
                                                <img class="integral-img" :src="integral_hei" />
                                                {{ items.allow }}
                                            </span>
                                        </div>
                                        <div class="sellMoney mr">共 {{ itemList.num }} {{ itemList.unit }}</div>
                                    </div>
                                </div>
                                
                            </div>
                            <div v-else class="proListUp" @tap="_changeDetail(items.sNo)">
                                <div class="proListUpLeft"><img :src="itemList.imgurl==''?ErrorImg:itemList.imgurl" @error="handleErrorImg(index,indexList)"  /></div>
                                <div class="proListUpRight">
                                    <div class="proTitle">
                                        <span v-if="itemList.otype == 'pt' || itemList.otype == 'PP'" class="ptCrl">{{language.storeMyOrder.pt}}</span>
                                        <span v-else-if="itemList.otype == 'KJ'" class="ptCrl KJ_color">{{language.storeMyOrder.kj}}</span>
                                        <span v-else-if="itemList.otype == 'JP'" class="ptCrl JP_color">{{language.storeMyOrder.jp}}</span>
                                        <span v-else-if="itemList.otype == 'FX'" class="ptCrl FX_color">{{language.storeMyOrder.fx}}</span>
                                        <span v-else-if="itemList.otype == 'MS' ||  itemList.otype == 'PM'" class="ptCrl MS_color">{{language.storeMyOrder.ms}}</span>
                                        {{ itemList.p_name }}
                                    </div>
                                    <div class="proPro myflex">{{ itemList.size }} <span style="color:#FA9D3B" v-if='itemList.isReturn'>退款成功</span></div>
                                    
                                    <div class="proSellData">
                                        <div class="sellMoney">
                                            {{items.currency_symbol || '￥'}} {{ LaiKeTuiCommon.getPriceWithExchangeRate(itemList.p_price,items.exchange_rate) }} 
                                            <span v-if="items.allow > 0">
                                                +
                                                <img class="integral-img" :src="integral_hei" />
                                                {{ items.allow }} 
                                            </span>
                                        </div>
                                        <div class="sellMoney mr">{{language.yushou_order.g}} {{ itemList.num }} {{ itemList.unit }} </div>
                                    </div>
                                </div>
                               
                            </div>
                        </div>
                        <div class="proListDown ml_60" v-if="items.status == 1 && items.list.length > 1 && (items.fhRadios && fhRadios)">
                            <div class="fhRadios1">
                                <label class="radio"><radio class="radio_color" value="1" @tap="_changeFh1(index, indexs)" :checked="changeFh1" /></label>
                            </div>
                            
                            <div class="font_28">{{ item.time }}</div>
                            <div class="center"  v-if="order_type != 'return' && items.self_type != 2">
                                <div class="proBtn" @tap="th1(items.sNo)">{{language.storeMyOrder.editOrder}}</div>
                                <div class="proBtn" @tap="_showFhDiv(items.sNo, index, indexs)">{{language.storeMyOrder.delivery}}</div>
                            </div>
                        </div>
                        <div class="proListDown " v-else-if="order_type == 'return'">
                            <div class="returnLeft">
                                <!-- 1、退货退款 2、退款 3、换货 -->
                                <img :src="items.list[0].re_type==1?storeOrder_thtk:items.list[0].re_type==2?storeOrder_tk:storeOrder_th" alt="">
                                {{items.list[0].type}}
                            </div>
                            
                            <div class="center ml_10">
                               <div class="proBtn" @tap="toReturn(items.sNo, items.list[0].id)" v-if="items.list[0].r_type == 0 || items.list[0].r_type == 3">{{language.storeMyOrder.audit}}</div>
                               <div class="proBtn" v-else @tap="toReDetail(items.sNo, items.list[0].id)">{{language.afterSale.details}}</div>
                            </div>
                            
                        </div>
                        <div class="proListDown " v-else>
                           
                            <div class="font_28">{{ item.time }}</div>
                            <div class="center ml_10" v-if="items.list.length && (items.self_lifting == '0' || items.self_lifting == '2')">
                                <div class="proBtn" @tap="toexp(items.sNo)" v-if="items.logistics_type">{{language.myStore.dzmd}}</div>
                                <div class="proBtn" @tap="_closeOrder(items.sNo)" v-if="items.status == 0">{{language.storeMyOrder.closeOrder}}</div>
                                <div class="proBtn" @tap="toReturn(items.sNo, items.list[0].id)" v-if="items.list[0].r_type == 0">{{language.storeMyOrder.audit}}</div>
                                <div class="proBtn" @tap="th1(items.sNo)" v-if="((items.status == 0 && !items.pay_time && items.order_status) || items.status == 1) && order_type != 'return'">{{language.storeMyOrder.editOrder}}</div>
                                <div
                                    class="proBtn"
                                    @tap="_showFhDiv1(items.list.length, items.sNo, items.list[0].id, index, indexs, items)"
                                    v-if="(items.status == 1 || ( items.list[0].r_type == 5)) && order_type != 'return'"
                                >
                                    <!-- 发货 -->
                                    {{language.storeMyOrder.delivery}}
                                </div>
                                <!-- 取消订单 -->
                                <div class="proBtn"  v-if="(items.status == 1 || ( items.list[0].r_type == 5)) && order_type != 'return'" @tap="cancelOrder(items.sNo)">{{language.bargain.cancelOrder}}</div>
                                <div class="proBtn" v-if="items.haveExpress" @tap="_seeWL(items.sNo)">{{language.storeMyOrder.checkLogistics}}</div>
                                <div class="proBtn" @tap="returnM(items.sNo, items.list[0].id, items.list[0].re_type)" v-if="items.list[0].r_type == 3 && items.list[0].re_type != 3">
                                    {{language.storeMyOrder.refund}}
                                </div>
                                <div
                                    class="proBtn"
                                    @tap="_showFhDiv2(items.list.length, items.sNo, items.list[0].id, index, indexs)"
                                    v-if="items.list[0].r_type == 3 && items.list[0].re_type == 3"
                                >
                                    {{language.storeMyOrder.backSend}}
                                </div>
                                <div class="proBtn" @tap="refuse(items.sNo, items.list[0].id, items.list[0].r_type)" v-if="items.list[0].r_type == 3">{{language.storeMyOrder.refuse}}</div>
                            </div>
                            <div class="center ml_10" v-if="items.self_lifting == '1' && items.status == 0">
                                <!-- 自提 -->
                                <div class="proBtn" @tap="toexp(items.sNo)" v-if="items.logistics_type">{{language.myStore.dzmd}}</div>
                                <div class="proBtn" @tap="_closeOrder(items.sNo)">{{language.storeMyOrder.closeOrder}}</div>
                                <div class="proBtn" @tap="th1(items.sNo)">{{language.storeMyOrder.editOrder}}</div>
                            </div>
                            <div class="center ml_10" v-if="items.self_lifting != '0' && (items.status == 2 || items.status == 8)">
                                <!-- 自提 -->
                                <div class="proBtn" @tap="toexp(items.sNo)" v-if="items.self_lifting == '1' && items.logistics_type">{{language.myStore.dzmd}}</div>
                                <!-- 核销 -->
                                <div class="proBtn" v-if="items.self_lifting != '2'" @tap="QRx(items)">{{language.storeMyOrder.verificationCode}}</div>
                            </div>
                        </div>
                    </div>
                </div>
                
            </div>
            <div v-else class="noFindDiv">
                <div><img class="noFindImg" :src="noOrder" /></div>
                <span class="noFindText">{{language.storeMyOrder.noOrder}}</span>
            </div>
            <uni-load-more v-if="listLength > 3" :loadingType="loadingType"></uni-load-more>
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
        <mpvue-picker
            v-if="show"
            :themeColor="themeColor"
            ref="mpvuePicker"
            :mode="mode"
            :deepLength="deepLength"
            :pickerValueDefault="pickerValueDefault"
            @onConfirm="onConfirm"
            :pickerValueArray="pickerValueArray"
        ></mpvue-picker>
        <div class="mask" v-if="fhDiv">
            <div class="fhDiv">
                <div class="fhDivTitle">
                    <span class="span">{{language.storeMyOrder.information}}</span>
                    <img class="img" @tap="_closeFhDiv()" :src="guanbi" />
                </div>
                <div class="formList">
                    <div class="leftText1"><span>{{language.storeMyOrder.courierName}}</span></div>
                    <div class="rightInput1" @tap="showSinglePicker()">
                        <input type="text" disabled="true" :placeholder-style="placeStyle" v-model="proClass" :placeholder="language.storeMyOrder.select_express" />
                        <div class="jiantouDiv"><img :src="jiantou" alt="" /></div>
                    </div>
                </div>
                <div class="formList">
                    <div class="leftText1"><span>{{language.storeMyOrder.courierNumner}}</span></div>
                    <div class="rightInput1"><input type="text" :placeholder-style="placeStyle" v-model="courier_num" :placeholder="language.storeMyOrder.courierNumner_placeholder" /></div>
                </div>
                <div class="fhSubmit" @tap="_send()">{{language.storeMyOrder.submit}}</div>
            </div>
        </div>
        <div class="mask" v-if="mask_display2">
            <div class="mask_cont">
                <p>{{language.storeMyOrder.refusalReasons}}</p>
                <input type="text" v-model="reason" :placeholder="language.storeMyOrder.refusalPlaceholder" :placeholder-style="placeStyle" />
                <div class="mask_button">
                    <div class="mask_button_left" @tap="_cancel(2)">{{language.storeMyOrder.cancel}}</div>
                    <div @tap="_confirm(2)">{{language.storeMyOrder.confirm}}</div>
                </div>
            </div>
        </div>
        <div class="mask" v-if="mask_display3">
            <div class="mask_cont">
                <p>{{language.storeMyOrder.refundSum}}</p>
                <input type="digit" v-model="refundM" @input="_checkMoney($event)" :placeholder="language.storeMyOrder.refundSum1" :placeholder-style="placeStyle" />
                <div class="mask_cont_div center">
                    <img :src="warnIng" class="img" />
                    <span>{{language.storeMyOrder.refundSumDisc1}} {{ LaiKeTuiCommon.DEFAULT_STORE_SYMBOL }}{{ refundNum }} {{language.storeMyOrder.refundSumDisc2}}</span>
                </div>
                <div class="mask_button">
                    <div class="mask_button_left" @tap="_cancel(3)">{{language.storeMyOrder.cancel}}</div>
                    <div @tap="_confirm(3)">{{language.storeMyOrder.confirm}}</div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    import switchNavOne from "@/components/aComponents/switchNavOne.vue";
    import switchNavTwo from '@/components/aComponents/switchNavTwo.vue'
    import mpvuePicker from '../../components/mpvuePicker.vue';
    import {
        LaiKeTui_axios,
        LaiKeTui_changeTabBar,
        LaiKeTui_changeFh,
        LaiKeTui_changeFh1,
        LaiKeTui_showFhDiv,
        LaiKeTui_showFhDiv1,
        LaiKeTui_closeOrder,
        LaiKeTui_returnM,
        LaiKeTui_send,
        LaiKeTui_confirm
    } from '@/pagesA/myStore/myStore/myOrder.js';
    export default {
        data() {
            return {
                commodity_type: 0,//0实物商品 1虚拟商品
                ErrorImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/Default_picture.png',
                warnIng: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/warnIng.png',
                refundNum: '',
                refundM: '',
                is_sus:false,
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
                reason: '',
                mask_display1: false,
                mask_display2: false,
                mask_display3: false,
                noOrder: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/noOrder.png',
                loadingType: 0,
                express_id: '',
                title: '我的订单',
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
                header: [],
                changeFh: [],
                changeFh1: false,
                fhRadios: false,
                fhDiv: false,
                kuaidiList: '',
                themeColor: '#D73B48',
                mode: '',
                deepLength: 1,
                pickerValueDefault: [0],
                pickerValueArray: [],
                show: false,
                proClass: '',
                courier_num: '',
                shop_id: '',
                order_type: '',
                list: [],
                page: 1,
                sNo: '',
                fastTap: true,
                payment_num: '',
                return_num: '',
                send_num: '',
                status: '',
                orderList_id: [],
                nodata: 0, //1代表没有数据，-1有数据
                returnR: 1,
                re_type: 1,
                placeStyle: 'color:#b8b8b8;font-size:28upx',
                is_tui: true,
                r_type: 0,
                refund_re_type: 0,
                searchtxt: '',
                platform_activities_id: '',
                bgColor: [
                    {
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
            };
        },
        components: {
            mpvuePicker,
            switchNavOne,
            switchNavTwo
        },
        computed:{
            header_label(){
                let that = this.language.order.myorder;
                if(this.commodity_type == 0){
                    return [that.whole, that.to_paid, that.to_delivered, this.language.goodsType.tk]
                } else {
                    return [that.whole, that.to_paid, this.language.toasts.myOrder.dhx, this.language.goodsType.tk]
                }
            },
            listLength(){
                let num = 0
                this.list.filter(items=>{
                    num += items.res.length
                })
                return num
            },
            now: function() {
                var year = new Date().getFullYear();
                var month = new Date().getMonth() + 1;
                var date = new Date().getDate();
                var date1 = year + '-' + month + '-' + date;
                return date1;
            }
        },
        onLoad(option) {
            this.access_id = uni.getStorageSync('access_id') ? uni.getStorageSync('access_id') : this.$store.state.access_id;
            if (option.platform_activities_id) {
                this.platform_activities_id = option.platform_activities_id
            }
            this.isLogin(()=>{})
            this.status = option.status;
            if (this.status == 'all') {
                this.order_type = '';
                this.topTabBar = 1;
            } else if (this.status == 'dfk') {
                this.order_type = 'payment';
                this.topTabBar = 2;
            } else if (this.status == 'dfh') {
                this.order_type = 'send';
                this.topTabBar = 3;
                if(this.commodity_type == 1){
                    this.order_type = 'receipt';
                }
            } else if (this.status == 'sh') {
                this.order_type = 'return';
                this.topTabBar = 4;
            }
            if(option.status == 2 && option.orderType_id == 1){
                this.order_type = 'receipt';
                this.topTabBar = 3;
                this.commodity_type = 1
                this.$nextTick(()=>{
                this.$refs.switchNavOne.isSwitchNav = 1
                })
            }

            
        },
        onShow() {
            this.access_id = uni.getStorageSync('access_id') ? uni.getStorageSync('access_id') : this.$store.state.access_id;
            this.shop_id = uni.getStorageSync('shop_id') ? uni.getStorageSync('shop_id') : this.$store.state.shop_id;
            
            this.page = 1
            this.loadingType = 0
            this._axios();
            this.$nextTick(()=>{
             this.$refs.switchNavTwo.isSwitchLable = this.topTabBar-1
             
            })
            this.header =[this.language.toasts.myOrder.swdd, this.language.toasts.myOrder.xndd]
        },
        methods: {
            handleErrorImg(index1,index2){
                this.list[index1].list[index2].imgurl = this.ErrorImg
            },
            toexp(sNo){
                uni.navigateTo({
                    url: '/pagesB/myStore/expressRecord?sNo=' + sNo
                });
            },
            _search(){
                this.page = 1
                this.loadingType = 0
                LaiKeTui_axios(this);
            },
            changeLoginStatus() {
                this.access_id = uni.getStorageSync('access_id');
                this._axios();
            },
            /**
             * 类型切换
             */
            _header_index(index) {
                this.commodity_type = index
                this.$refs.switchNavTwo.isSwitchLable =0
                LaiKeTui_changeTabBar(this, 1);
                this._axios();
            },
            /**
             * 导航栏切换
             */
            _header_label_index(index) {
                this.searchtxt = ''
                this.page = 1
                this.loadingType = 0
                let num = index + 1
                LaiKeTui_changeTabBar(this, num);
            },
            _axios() {
                LaiKeTui_axios(this);
            },
            _changeDetail(id) {
                if(this.order_type == 'return'){
                    return
                }
                
                uni.navigateTo({
                    url: '/pagesA/myStore/order?order_id=' + id
                });
            },
            _changeFh(id, index, index1, index2) {
                LaiKeTui_changeFh(this, id, index, index1, index2);
            },
            _changeFh1(index, index1) {
                LaiKeTui_changeFh1(this, index, index1);
            },
            // 编辑订单
            th1(sNo) {
                uni.navigateTo({
                    url: '/pagesA/myStore/order?editor=1&order_id=' + sNo
                });
            },
            // 发货  单件商品
            _showFhDiv(sNo, index, index1) {
                LaiKeTui_showFhDiv(this, sNo, index, index1);
            },
            // 发货
            _showFhDiv1(length, sNo, id, index, indexs, items) {
                if(items.self_lifting == 2){
                    uni.navigateTo({
                        url: '/pagesB/myStore/shipments?id=' + items.sNo
                    });
                } else {
                    uni.navigateTo({
                        url: '/pagesA/myStore/shipments?id=' + items.sNo
                    });
                }
            },
            // 回寄
            _showFhDiv2(length, sNo, id, index, indexs) {
                this.re_type = 3;
                this.orderList_id = id;
                LaiKeTui_showFhDiv1(this, sNo, length, index, indexs);
            },
            // 关闭订单
            _closeOrder(sNo) {
                uni.showModal({
                    title: this.language.Redeem_order.tipsa[0],
                    content: this.language.showModal.delOrder,
                    confirmText: this.language.showModal.confirm,
                    cancelText: this.language.showModal.cancel,
                    cancelColor:'#333333 !important',
                    confirmColor:'#D73B48 !important',
                    success: (e)=>{
                        if(e.confirm){
                            LaiKeTui_closeOrder(this, sNo);
                        }
                    }
                })
            },
            // 审核
            toReturn(sNo, order_details_id) {
                
                uni.navigateTo({
                    url: '/pagesA/myStore/returnDetail?type=audit&&sNo=' + sNo + '&order_details_id=' + order_details_id
                });
            },
            toReDetail(sNo, order_details_id){
                uni.navigateTo({
                    url: '/pagesA/myStore/returnDetail?sNo=' + sNo + '&order_details_id=' + order_details_id
                });
            },
            cancelOrder(sNo){ 
                let data = {
                   api:'mch.App.Mch.CancellationOfOrder',
                    sNo: sNo,
                    shop_id: this.shop_id,
                };
                 this.$req.post({ data }).then(res => {
                     if(res.code == 200){
                         uni.showToast({
                             title:this.language.zdata.czcg,
                             icon:"none"
                         })
                         setTimeout(()=>{
                             this._axios();
                         },1500)
                     }else{
                         uni.showToast({
                             title:res.message,
                             icon:'error'
                         })
                     }
                 })
            },
            // 查看物流
            _seeWL(sNo) {
                let data = {
                    api: 'app.order.logistics',
                    id: sNo,
                    access_id: this.access_id,
                    type: '',
                    o_source: 1,
                };

                if (this.source == 1) {
                    data.type = 'pond';
                }

                this.$req.post({ data }).then(res => {
                    let { code, data, message } = res
                    uni.hideLoading();
                    if (code == 200) {
                        if (data.list.length > 1||!data.isPackage) {
                            uni.navigateTo({
                                url: '/pagesB/expressage/expressage?sNo=' + sNo
                            });
                        } else {
                            uni.navigateTo({
                                url: '/pagesC/expressage/expressage?list=' + JSON.stringify(data.list[0]) + '&sNo=' + sNo
                            });
                        }
                    } else {
                        uni.showToast({
                            title: message,
                            duration: 1500,
                            icon: 'none'
                        });
                    }
                });
            },
            // 退款
            returnM(sNo, order_details_id, type) {
                //同意退款
                this.refund_re_type = type;
                LaiKeTui_returnM(this, sNo, order_details_id);
            },
            // 拒绝
            refuse(sNo, order_details_id, type) {
                //拒绝退货
                this.sNo = sNo;
                this.orderList_id = order_details_id;
                this.mask_display2 = true;
                this.r_type = type;
            },
            _closeFhDiv() {
                this.fhDiv = false;
                this.fhRadios = false;
            },
            showSinglePicker() {
                this.show = true;
                this.mode = 'selector';
                this.deepLength = 1;
                this.pickerValueDefault = [0];
                this.$refs.mpvuePicker.show();
            },
            // 物流提交
            _send() {
                LaiKeTui_send(this);
            },
            // 拒绝理由取消
            _cancel(type) {
                if (type == 1) {
                    this.mask_display1 = false;
                } else if (type == 2) {
                    this.mask_display2 = false;
                } else if (type == 3) {
                    this.mask_display3 = false;
                }
            },
            // 拒绝理由提交
            _confirm(type) {
                LaiKeTui_confirm(this, type);
            },
            // 请输入退款金额
            _checkMoney(e) {
                if (Number(e.target.value) > this.refundNum) {
                    uni.showToast({
                        title: this.language.storeMyOrder.maximumTips,
                        duration: 1500,
                        icon: 'none'
                    });
                    this.refundM = this.refundNum;
                }
            },
            // 选择快递公司
            onConfirm(e) {
                this.proClass = e.label;
                this.show = false;
                this.express_id = this.kuaidiList[e.index[0]].id;
            },
            th() {
                uni.navigateTo({
                    url: '/pagesA/myStore/storeRefund'
                });
            },
            onCopy(orderId) {
            
                var me = this;
                uni.setClipboardData({
                    data: orderId,
                    success: function(res) {
                        uni.showToast({
                            title: me.language.order.order.copy_success,
                            duration: 1500,
                            icon: "none",
                        });
                    },
                });
            },
            // 跳转至手动输入验证码页面
            QRx(item) {
                let url = '/pagesA/myStore/QRcode?order_id=' + item.id
                //虚拟商品核销
                if(item.list[0].commodity_type == 1){
                    //xnGoodsId：虚拟商品id   xnGoodsHx：虚拟商品核销是否选择过门店（2:未选择门店 需要先去选择门店）
                    url = '/pagesA/myStore/QRcode?xnGoodsId=' + item.sNo + '&xnGoodsHx=' + item.show_store_name + '&shop_id=' + item.mch_id + '&order_id=' + item.id
                }
                uni.navigateTo({
                    url: url
                });
            },
            // 自提扫码
            QRs(id) {
                // #ifndef H5
                uni.scanCode({
                    success: (rew) => {
                        if (!rew.result) {
                            return;
                        }
                        uni.showLoading({
                            title: this.language.toload.load
                        });
                        var data = {
                            
                            api:'mch.App.Mch.Sweep_extraction_code',
                            access_id: this.access_id,
                            shop_id: this.shop_id,
                            order_id: id,
                            extraction_code: rew.result
                        };

                        this.$req.post({data}).then(res=>{
                            let { code, data, message } = res
                            uni.hideLoading();
                            if (code == 200) {
                                me.order_id = data.order_id;
                                me.p_price = data.p_price;
                                me.sNo = data.sNo;
                                uni.showToast({
                                    title: message,
                                    duration: 1500,
                                    icon: 'none'
                                });
                                // 成功后跳转 QRsuccess页面
                                uni.redirectTo({
                                    url: '/pagesC/myStore/QRsuccess?p_price=' + me.p_price + '&sNo=' + me.sNo + '&order_id=' + me.order_id
                                });
                            } else {
                                uni.showToast({
                                    title: message,
                                    duration: 1500,
                                    icon: 'none'
                                });
                            }
                        })
                    }
                });
                // #endif
                // #ifdef H5
                uni.showToast({
                    icon: 'none',
                    title: this.language.storeMyOrder.noScan
                });
                // #endif
            }
        },
        onReachBottom: function() {
            if (this.loadingType != 0) {
                return;
            }
            this.loadingType = 1;
            
            this.page += 1;
            
            var data = {
                api:'mch.App.Mch.My_order',
                access_id: this.access_id,
                shop_id: this.shop_id,
                order_type: this.order_type,
                page: this.page,
                keyword:this.searchtxt
            };
            //虚拟商品
            if(this.commodity_type == 1){
                data.order_headr_type = 'VI'
            }
            this.$req.post({ data }).then(res => {
                let { code, data, message } = res;
                if (data.list.length > 0) {
                    
                    if (this.order_type == 'return') {
                        
                        for (let i =0; i < data.list.length; i ++ ) {
                            for (let i1 =0; i1 < data.list[i].res.length; i1 ++ ) {
                                if(!this.LaiKeTuiCommon.isempty(data.list[i].res[i1][0])){
                                    data.list[i].res[i1] = data.list[i].res[i1][0]
                                }else{
                                    break;
                                }
                            }
                        }
                        for (let i =0; i < data.list.length; i ++ ) {
                            for (let i1 =0; i1 < data.list[i].res.length; i1 ++ ) {
                                if (!Array.isArray(data.list[i].res[i1].list)) {
                                    data.list[i].res[i1].list = [
                                        data.list[i].res[i1].list
                                    ]
                                }
                            }
                        }
                        this.list.push(...data.list)
                    }else{
                        this.list = this.list.concat(data.list);
                    }
                    
                    this.loadingType = 0;
                } else {
                    this.loadingType = 2;
                }
            });
            
        },
      
    };
</script>

<style>
    page{
        background: #F4F5F6
    }
</style>
<style lang="less">
    @import url("@/laike.less");
    @import url('../../static/css/myStore/myOrder.less');
    
    .myflex{
        display: flex;
        justify-content: space-between;
    }
    /deep/.switchLable{
        z-index: 999;
    }
</style>
