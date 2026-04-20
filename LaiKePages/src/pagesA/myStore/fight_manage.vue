<template>
    <div class="container">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>

        <heads :title="language.fight_manage.title" :returnR="returnR"></heads>

        <div class="relative">
            <div class="topTabBar">
                <div class="width_25" @tap="changeTabBar(4)"><div style="width: 50%;" :class="{ active: topTabBar == 4 }">{{language.fight_manage.tabTitle[0]}}</div></div>

                <div class="width_25" @tap="changeTabBar(5)"><div style="width: 50%;" :class="{ active: topTabBar == 5 }">{{language.fight_manage.tabTitle[1]}}</div></div>

                <div class="width_25" @tap="changeTabBar(6)"><div style="width: 50%;" :class="{ active: topTabBar == 6 }">{{language.fight_manage.tabTitle[2]}}</div></div>
            </div>
            <div v-if="list.length > 0">
                <div v-for="(item, index) in list" :key="index">
                    <div class="date">
                        {{ item.time }}
                        <span v-if="item.time == now">{{language.fight_manage.today}}</span>
                    </div>
                    <div class="orderList" v-for="(items, indexs) in item.res" :key="indexs">
                        <div class="orderTitle">
                            <div>{{language.fight_manage.orderNo}} {{ items.sNo }}</div>
                            <div>
                                <span v-if="items.status == 0 && items.self_lifting == '0'">{{language.fight_manage.obligation}}</span>
                                <span v-else-if="items.status == 1 && items.self_lifting == '0'">{{language.fight_manage.waitSend}}</span>
                                <span v-else-if="items.status == 2 && items.self_lifting == '0'">{{language.fight_manage.waitReceiving}}</span>
                                <span v-else-if="items.status == 2 && items.self_lifting == '1'">{{language.fight_manage.waitSend}}</span>
                                <span v-else-if="items.status == 3">{{language.fight_manage.comment}}</span>
                                <span v-else-if="items.status == 4">
                                    <span v-if="items.list[0].r_type == 0">{{language.fight_manage.returnReview}}</span>
                                    <span v-else-if="items.list[0].r_type == 1 && items.list[0].re_type == 3">{{language.fight_manage.agreeReplacement}}</span>
                                    <span v-else-if="items.list[0].r_type == 1">{{language.fight_manage.agreeReturn}}</span>
                                    <span v-else-if="items.list[0].r_type == 2">{{language.fight_manage.refusedReturn}}</span>
                                    <span v-else-if="items.list[0].r_type == 3">{{language.fight_manage.receiveMerchant}}</span>
                                    <span v-else-if="items.list[0].r_type == 4">{{language.fight_manage.agreeRefund}}</span>
                                    <span v-else-if="items.list[0].r_type == 5">{{language.fight_manage.rejectReturn}}</span>
                                    <span v-else-if="items.list[0].r_type == 8">{{language.fight_manage.refusedRefund}}</span>
                                    <span v-else-if="items.list[0].r_type == 9">{{language.fight_manage.agreeAndRefund}}</span>
                                    <span v-else-if="items.list[0].r_type == 11">{{language.fight_manage.sendMerchandise}}</span>
                                    <span v-else-if="items.list[0].r_type == 12">{{language.fight_manage.afterComplete}}</span>
                                </span>
                                <span v-else-if="items.status == 5">{{language.fight_manage.done}}</span>
                                <span v-else-if="items.status == 6">{{language.fight_manage.orderClosed}}</span>
                            </div>
                        </div>
                        <div class="proList" v-for="(itemList, indexList) in items.list" :key="indexList">
                            <div class="proListUp ml_60" @tap="_changeDetail(items.sNo)" v-if="items.status == 1 && items.list.length > 1 && (items.fhRadios && fhRadios)">
                                <div class="fhRadios">
                                    <label class="radio" v-if="itemList.r_status == 1">
                                        <radio class="radio_color" value="1" @tap.stop="_changeFh(itemList.id, index, indexs, indexList)" :checked="itemList.status" />
                                    </label>
                                </div>
                                <div class="proListUpLeft"><img :src="itemList.imgurl" /></div>
                                <div class="proListUpRight">
                                    <div class="proTitle">
                                        <span v-if="itemList.otype == 'pt' || itemList.otype == 'PP'" class="ptCrl">{{language.fight_manage.pt}}</span>
                                        <span v-else-if="itemList.otype == 'KJ'" class="ptCrl KJ_color">{{language.fight_manage.kj}}</span>
                                        <span v-else-if="itemList.otype == 'JP'" class="ptCrl JP_color">{{language.fight_manage.jp}}</span>
                                        <span v-else-if="itemList.otype == 'FX'" class="ptCrl FX_color">{{language.fight_manage.fx}}</span>
                                        <span v-else-if="items.self_lifting == '1'" class="ptCrl ZT_color">{{language.fight_manage.zt}}</span>
                                        {{ itemList.p_name }}
                                    </div>
                                    <div class="proPro mt_40">{{ itemList.size }}</div>
                                </div>
                                <div class="proSellData">
                                    <div class="sellMoney">
                                        {{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(itemList.p_price) }}
                                        <span v-if="items.allow > 0">
                                            +
                                            <img class="integral-img" :src="integral_hei" />
                                            {{ items.allow }}
                                        </span>
                                    </div>
                                    <div class="sellMoney mt_40">x{{ itemList.num }}</div>
                                </div>
                            </div>
                            <div v-else class="proListUp" @tap="_changeDetail(items.sNo)">
                                <div class="proListUpLeft"><img :src="itemList.imgurl" /></div>
                                <div class="proListUpRight">
                                    <div class="proTitle">
                                        <span v-if="itemList.otype == 'pt' || itemList.otype == 'PP'" class="ptCrl">{{language.fight_manage.pt}}</span>
                                        <span v-else-if="itemList.otype == 'KJ'" class="ptCrl KJ_color">{{language.fight_manage.kj}}</span>
                                        <span v-else-if="itemList.otype == 'JP'" class="ptCrl JP_color">{{language.fight_manage.jp}}</span>
                                        <span v-else-if="itemList.otype == 'FX'" class="ptCrl FX_color">{{language.fight_manage.fx}}</span>
                                        {{ itemList.p_name }}
                                    </div>
                                    <div class="proPro mt_20">{{ itemList.size }}</div>
                                </div>
                                <div class="proSellData">
                                    <div class="sellMoney">
                                        {{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(itemList.p_price) }}
                                        <span v-if="items.allow > 0">
                                            +
                                            <img class="integral-img" :src="integral_hei" />
                                            {{ items.allow }}
                                        </span>
                                    </div>
                                    <div class="sellMoney mt_20">x{{ itemList.num }}</div>
                                </div>
                            </div>
                        </div>
                        <div class="proListDown ml_60" v-if="items.status == 1 && items.list.length > 1 && (items.fhRadios && fhRadios)">
                            <div class="fhRadios1">
                                <label class="radio"><radio class="radio_color" value="1" @tap="_changeFh1(index, indexs)" :checked="changeFh1" /></label>
                            </div>
                            <div class="font_28">
                                {{language.fight_manage.total}}
                                <span class="color_ff0">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(items.z_price) }}</span>
                                <span class="color_ff0" v-if="items.allow > 0">
                                    +
                                    <img class="integral-img" :src="integral_hong" />
                                    {{ items.allow }}
                                </span>
                            </div>
                            <div class="center">
                                <div class="proBtn" @tap="th1(items.sNo)">{{language.fight_manage.editOrder}}</div>
                                <div class="proBtn" @tap="_showFhDiv(items.sNo, index, indexs)">{{language.fight_manage.delivery}}</div>
                            </div>
                        </div>
                        <div class="proListDown " v-else>
                            <div class="font_28">
                                {{language.fight_manage.total}}
                                <span class="color_ff0">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(items.z_price) }}</span>
                                <span class="color_ff0" v-if="items.allow > 0">
                                    +
                                    <img class="integral-img" :src="integral_hong" />
                                    {{ items.allow }}
                                </span>
                            </div>
                            <div class="center ml_10" v-if="items.self_lifting == '0' || items.status == 4">
                                <div class="proBtn" @tap="_closeOrder(items.sNo)" v-if="items.status == 0">{{language.fight_manage.closeOrder}}</div>
                                <div class="proBtn" @tap="toReturn(items.sNo, items.list[0].id)" v-if="items.list[0].r_type == 0 && items.status == 4">{{language.fight_manage.audit}}</div>
                                <div class="proBtn" @tap="th1(items.sNo)" v-if="(items.status == 0 && !items.pay_time && items.order_status) || items.status == 1">{{language.fight_manage.editOrder}}</div>
                                <div
                                    class="proBtn"
                                    @tap="_showFhDiv1(items.list.length, items.sNo, items.list[0].id, index, indexs, items)"
                                    v-if="items.status == 1 || (items.status == 4 && items.list[0].r_type == 5)"
                                >
                                    {{language.fight_manage.delivery}}
                                </div>
                                <div class="proBtn" v-if="items.status == 2 || (items.status == 4 && items.list[0].r_type == 3)" @tap="_seeWL(items.sNo)">{{language.fight_manage.checkLogistics}}</div>
                                <div class="proBtn" @tap="returnM(items.sNo, items.list[0].id)" v-if="items.status == 4 && items.list[0].r_type == 3 && items.list[0].re_type != 3">
                                    {{language.fight_manage.refund}}
                                </div>
                                <div
                                    class="proBtn"
                                    @tap="_showFhDiv2(items.list.length, items.sNo, items.list[0].id, index, indexs)"
                                    v-if="items.status == 4 && items.list[0].r_type == 3 && items.list[0].re_type == 3"
                                >
                                    {{language.fight_manage.backSend}}
                                </div>
                                <div class="proBtn" @tap="refuse(items.sNo, items.list[0].id)" v-if="items.status == 4 && items.list[0].r_type == 3">{{language.fight_manage.refuse}}</div>
                            </div>
                            <div class="center ml_10" v-if="items.self_lifting == '1' && items.status == 2">
                                <!-- 自提 -->
                                <div class="proBtn" @tap="QRx(items.id)">{{language.fight_manage.verificationCode}}</div>
                                <div class="proBtn" @tap="QRs(items.id)">{{language.fight_manage.scanQRcode}}</div>
                            </div>
                        </div>
                        <div v-if="indexs + 1 != item.res.length" class="hr"></div>
                    </div>
                </div>
                <uni-load-more v-if="list.length > 10" :loadingType="loadingType"></uni-load-more>
            </div>

            <div v-else-if="nodata == 1" class="noFindDiv">
                <div><img class="noFindImg" :src="noOrder" /></div>
                <span class="noFindText">{{language.fight_manage.noOrder}}</span>
            </div>
        </div>
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
                    <span class="span">{{language.fight_manage.information}}</span>
                    <img class="img" @tap="_closeFhDiv()" :src="guanbi" />
                </div>
                <div class="formList">
                    <div class="leftText1"><span>{{language.fight_manage.courierName}}</span></div>
                    <div class="rightInput1" @tap="showSinglePicker()">
                        <input type="text" disabled="true" :placeholder-style="placeStyle" v-model="proClass" :placeholder="language.fight_manage.select_express" />
                        <div class="jiantouDiv"><img :src="jiantou" alt="" /></div>
                    </div>
                </div>
                <div class="formList">
                    <div class="leftText1"><span>{{language.fight_manage.courierNumner}}</span></div>
                    <div class="rightInput1"><input type="text" :placeholder-style="placeStyle" v-model="courier_num" :placeholder="language.fight_manage.courierNumner_placeholder" /></div>
                </div>
                <div class="fhSubmit" @tap="_send()">{{language.fight_manage.submit}}</div>
            </div>
        </div>
        <div class="mask" v-if="mask_display2">
            <div class="mask_cont">
                <p>{{language.fight_manage.refusalReasons}}</p>
                <input type="text" v-model="reason" :placeholder="language.fight_manage.refusalPlaceholder" :placeholder-style="placeStyle" />
                <div class="mask_button">
                    <div class="mask_button_left" @tap="_cancel(2)">{{language.fight_manage.cancel}}</div>
                    <div @tap="_confirm(2)">{{language.fight_manage.confirm}}</div>
                </div>
            </div>
        </div>
        <div class="mask" v-if="mask_display3">
            <div class="mask_cont">
                <p>{{language.fight_manage.refundSum}}</p>
                <input type="digit" v-model="refundM" @input="_checkMoney($event)" :placeholder="language.fight_manage.refundSum1" :placeholder-style="placeStyle" />
                <div class="mask_cont_div center">
                    <img :src="warnIng" class="img" />
                    <span>{{language.fight_manage.refundSumDisc1}}{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(refundNum) }}{{language.fight_manage.refundSumDisc2}}</span>
                </div>
                <div class="mask_button">
                    <div class="mask_button_left" @tap="_cancel(3)">{{language.fight_manage.cancel}}</div>
                    <div @tap="_confirm(3)">{{language.fight_manage.confirm}}</div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
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
            m: 'pt_order',
            warnIng: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/warnIng.png',
            refundNum: '',
            refundM: '',
            reason: '',
            mask_display1: false,
            mask_display2: false,
            mask_display3: false,
            noOrder: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/noOrder.png',
            loadingType: 0,
            express_id: '',
            title: '拼团管理',
            
            topTabBar: 4,
            integral_hong: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/integral.png',
            integral_hei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/integral_hei.png',
            testImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/yhqBg.png',
            jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jiantou2x.png',
            guanbi: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/guanbi2x.png',
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
            order_type: 'success',
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
            returnR: 9,
            re_type: 1,
            placeStyle: 'color:#b8b8b8;font-size:28upx',
            is_tui: true
        };
    },
    onLoad(option) {
        

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
        } else if (this.status == 'sh') {
            this.order_type = 'return';
            this.topTabBar = 4;
        }
    },
    onShow() {
        
        this.shop_id = uni.getStorageSync('shop_id') ? uni.getStorageSync('shop_id') : this.$store.state.shop_id;

        this._axios();
    },
    methods: {
        changeLoginStatus() {
            this._axios();
        },
        changeTabBar(num) {
            LaiKeTui_changeTabBar(this, num);
        },
        _axios() {
            LaiKeTui_axios(this);
        },
        _changeDetail(id) {
            uni.navigateTo({
                url: '../myStore/order?order_id=' + id
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
            uni.navigateTo({
                url: '/pagesA/myStore/shipments?shops=' + JSON.stringify(items)
            });
        },
        // 回寄
        _showFhDiv2(length, sNo, id, index, indexs) {
            this.re_type = 3;
            this.orderList_id = id;
            LaiKeTui_showFhDiv1(this, sNo, length, index, indexs);
        },
        // 关闭订单
        _closeOrder(sNo) {
            LaiKeTui_closeOrder(this, sNo);
        },
        // 审核
        toReturn(sNo, order_details_id) {
            uni.navigateTo({
                url: '/pagesA/myStore/storeRefund?sNo=' + sNo + '&order_details_id=' + order_details_id
            });
        },
        // 查看物流
        _seeWL(sNo) {
            let data = {
                api: 'app.order.logistics',
                id: sNo,
                o_source: 1,
                type: ''
            };

            if (this.source == 1) {
                data.type = 'pond';
            }

            this.$req
                .post({ data })
                .then(res => {
                    uni.hideLoading();
                    if (res.code == 200) {
                        let data = res.res;
                        if (data.length > 1) {
                            uni.navigateTo({
                                url: '/pagesB/expressage/expressage?sNo=' + sNo
                            });
                        } else {
                            uni.navigateTo({
                                url: '/pagesC/expressage/expressage?list=' + JSON.stringify(data[0]) + '&sNo=' + sNo
                            });
                        }
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        });
                    }
                })
                .catch(error => {
                    uni.showToast({
                        title: this.language.fight_manage.LogFailLoadDBSystem,
                        duration: 2000,
                        icon: 'none'
                    });
                });
        },
        // 退款
        returnM(sNo, order_details_id) {
            //同意退款
            LaiKeTui_returnM(this, sNo, order_details_id);
        },
        // 拒绝
        refuse(sNo, order_details_id) {
            //拒绝退货
            this.sNo = sNo;
            this.orderList_id = order_details_id;
            this.mask_display2 = true;
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
                    title: this.language.fight_manage.maximumTips,
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
        // 跳转至手动输入验证码页面
        QRx(id) {
            uni.navigateTo({
                url: '/pagesA/myStore/QRdraw?order_id=' + id
            });
        },
        // 自提扫码
        QRs(id) {
            var me = this;
            // #ifndef H5
            uni.scanCode({
                success: function(rew) {
                    if (!rew.result) {
                        return;
                    }
                    uni.showLoading({
                        title: this.language.showLoading.loading
                    });
                    var data = {
                        
                        api:"mch.App.Mch.Sweep_extraction_code",
                        shop_id: me.shop_id,
                        order_id: id,
                        extraction_code: rew.result
                    };
                    this.$req.post({data}).then(res => {
                        uni.hideLoading();
                        if (res.code == 200) {
                            me.order_id = res.data.order_id;
                            me.p_price = res.data.p_price;
                            me.sNo = res.data.sNo;
                            uni.showToast({
                                title: res.message,
                                duration: 1500,
                                icon: 'none'
                            });
                            // 成功后跳转 QRsuccess页面
                            uni.redirectTo({
                                url: '/pagesC/myStore/QRsuccess?p_price=' + me.p_price + '&sNo=' + me.sNo + '&order_id=' + me.order_id
                            });
                        } else {
                            uni.showToast({
                                title: res.message,
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
                title: this.language.fight_manage.noScan
            });
            // #endif
        }
    },
    onReachBottom: function() {
        if (this.loadingType != 0) {
            return;
        }
        this.loadingType = 1;
        this.page ++;
        LaiKeTui_axios(this, 1)
    },
    computed: {
        now: function() {
            var year = new Date().getFullYear();
            var month = new Date().getMonth() + 1;
            var date = new Date().getDate();
            var date1 = year + '-' + month + '-' + date;
            return date1;
        }
    },
    components: {
        mpvuePicker
    }
};
</script>

<style lang="less">
@import url("@/laike.less");
@import url('../../static/css/myStore/myOrder.less');
</style>
