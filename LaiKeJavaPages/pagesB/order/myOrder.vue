<template>
    <div class="container">
        <lktauthorize
            ref="lktAuthorizeComp"
            v-on:pChangeLoginStatus="changeLoginStatus"
        ></lktauthorize>
        <!-- 头部 -->
        <heads
            :title="title"
            ishead_w="2"
            :bgColor="bgColor"
            titleColor="#333333"
        ><!-- 49904bug returnR本来是7改成1 -->
        </heads>
        <!-- 搜索 -->
        <search
            ref="search"
            :is_search_prompt="language.waterfall.sNo"
            :is_search_type="2"
            @search="_seart"
        >
        </search>
        <template v-if="type!='FX'">
            <!-- 导航栏 -->
            <switchNavOne
                ref="switchNavOne"
                :is_switchNav="header"
                :is_switchNav_radius="'0 0 0 0'"
                :is_switchNav_padT="'0'"
                :is_switchNav_padB="'24rpx'"
                @choose="_header_index"
            >
            </switchNavOne>
            <!-- 标签 --> 
            <switchNavTwo ref="switchNavTwo" :is_switchLable="header_label" @choose="_header_label_index"></switchNavTwo>
        </template>
        <template v-else>
            <!-- 导航栏 -->
            <switchNavOne
                ref="switchNavOne"
                :is_switchNav="header_label"
                :is_switchNav_radius="'0 0 0 0'"
                :is_switchNav_padT="'0'"
                :is_switchNav_padB="'24rpx'"
                @choose="_header_label_index"
            >
            </switchNavOne>
        </template>
        <!-- 骨架屏组件 -->
        <template>
            <view style="position: relative; top: 24rpx">
                <view v-if="load">
                    <view class="skeleton">
                        <view class="orderlist">
                            <ul
                                class="order_goods orderlist-head"
                                style="padding-right: 15px"
                                v-for="(item, index) of 4"
                                :key="index"
                            >
                                <li
                                    class="dd-boxa"
                                    style="
                                        height: 20px;
                                        margin-top: 10px;
                                        margin-bottom: 10px;
                                    "
                                >
                                    <p
                                        class="shopName skeleton-rect"
                                        style="
                                            width: 100px;
                                            margin-left: 28rpx;
                                            opacity: 0;
                                        "
                                    >
                                        0
                                    </p>
                                    <p class="red"></p>
                                </li>
                                <li
                                    class="order_two"
                                    style="
                                        display: flex;
                                        justify-content: flex-start;
                                    "
                                >
                                    <view
                                        class="left skeleton-rect"
                                        style="
                                            width: 60px;
                                            height: 60px;
                                            margin-right: 10px;
                                        "
                                    ></view>
                                    <view class="right">
                                        <view
                                            class="skeleton-rect"
                                            style="
                                                width: 250px;
                                                height: 22px;
                                                margin-bottom: 16px;
                                            "
                                        ></view>
                                        <view
                                            class="skeleton-rect"
                                            style="width: 250px; height: 22px"
                                        ></view>
                                    </view>
                                </li>
                                <li
                                    class="skeleton-rect"
                                    style="
                                        height: 51px;
                                        width: 95%;
                                        margin-left: 28rpx;
                                    "
                                ></li>
                            </ul>
                        </view>
                    </view>
                </view>
                <skeleton
                    :loading="load"
                    :animation="true"
                    bgColor="#FFF"
                ></skeleton>
            </view>
        </template>

        <div v-if="!load">
            <div class="orderlist" v-if="order.length > 0">
                <div class="orderlist-line"></div>

                <ul
                    class="order_goods orderlist-head animation"
                    v-for="(item, index) in order"
                    :key="index"
                    :style="{
                        animationDelay:
                            ((index + 1) * 0.1 > 1 ? 0.5 : (index + 1) * 0.1) +
                            's',
                    }"
                >
                    <!-- 订单头部 -->
                    <li class="order_one dd-boxa" v-if="!item.ismch">
                        <!-- 订单头部显示订单号 -->
                        <p v-if="!item.shop_name">{{ item.sNo }}</p>

                        <p
                            v-else
                            @tap.stop="_goStore(item.shop_id)"
                            class="shopName"
                        >
                            <img :src="item.shopHead_img" class="storeImg" @error="handleErrorImgHeard(index)"/>
                            {{ item.ismch ? "" : item.shop_name }}
                            <img class="dd-boxa-img" :src="jiantou" />
                        </p>

                        <p class="red" v-if="item.codetext">
                            {{ item.codetext }}
                        </p>
                        <p class="red" v-else>
                            {{
                                item.status == 0
                                    ? language.order.myorder.dfk
                                    : item.status == 1
                                    ? language.order.myorder.dfh
                                    : item.status == 2
                                    ? language.order.myorder.dsh
                                    : item.status == 3 ||
                                      item.status == 5
                                    ? language.order.myorder.jycg
                                    : item.status == 8
                                    ? language.toasts.myOrder.dhx
                                    : item.status == 7 || item.status == 6
                                    ? language.order.myorder.jygb
                                    : item.status == 4
                                    ? cancelGoods
                                    : item.status == 12
                                    ? language.order.myorder.ddwc
                                    : item.status == 10 || item.status == 11
                                    ? language.order.myorder.ptsb
                                    : item.status == 9
                                    ? language.order.myorder.ptz
                                    : ""
                            }}
                        </p>
                    </li>

                    <!-- 订单商品详情 -->
                    <li
                        class="order_two" 
                        :style="{ padding: item.ismch ? '' : '' }"
                        v-for="(orders, index1) in item.list"
                        :key="index1"
                        @tap="
                            _navigateTo(item.status, item.id, item.otype, item)
                        "
                    >
                        <!-- 商品详情头部 -->
                        <div style="padding-top: 0;"
                            class="order_one dd-boxb ismch"
                            :style="{
                                borderTop: index1 == 0 ? 'none' : '',
                                marginTop: index1 == 0 ? '0' : '',
                            }"
                            v-if="item.ismch && orders.shop_name"
                        >
                            <p v-if="!item.ismch">{{ item.shop_name }}</p>

                            <p
                                v-else
                                @tap.stop="_goStore(orders.shop_id)"
                                style="color: #666"
                                class="shopName"
                            >
                                <img
                                    :src="item.shopHead_img"
                                    class="storeImg"
                                />

                                {{ orders.shop_name }}

                                <img class="dd-boxb-img" style="width: 32rpx;height: 44rpx;" :src="jiantou" />
                            </p>

                            <p class="red" v-if="index1 === 0">
                                {{ item.codetext }}
                            </p>
                        </div>

                        <!-- 订单商品图 -->
                        <img
                            :src="orders.imgurl"
                            class="orderImg"
                            @error="handleErrorImg(index,index1)"
                        />

                        <!-- 订单商品标题 -->
                        <div class="goodsTitle">
                            <!-- TODO：微信小程序不支持动态css -->

                            <p class="order_p_name commodity-title">
                                <span
                                    v-if="
                                        item.otype == 'pt' || item.otype == 'PP'
                                    "
                                    class="ptCrl"
                                    >{{
                                        language.order.orderSearch.Button[0]
                                    }}</span
                                >
                                <span
                                    v-else-if="item.otype == 'KJ'"
                                    class="ptCrl commodity-type-kj"
                                    >{{
                                        language.order.orderSearch.Button[1]
                                    }}</span
                                >
                                <span
                                    v-else-if="item.otype == 'JP'"
                                    class="ptCrl commodity-type-jp"
                                    >{{
                                        language.order.orderSearch.Button[2]
                                    }}</span
                                >
                                <span
                                    v-else-if="item.otype == 'FX'"
                                    class="ptCrl commodity-type-px"
                                    >{{
                                        language.order.orderSearch.Button[3]
                                    }}</span
                                >
                                <span
                                    v-else-if="item.otype == 'integral'"
                                    class="ptCrl commodity-type-integral"
                                    >{{
                                        language.order.orderSearch.Button[4]
                                    }}</span
                                >
                                <span
                                    v-else-if="
                                        item.otype == 'MS' || item.otype == 'PM'
                                    "
                                    class="ptCrl commodity-type-ms"
                                    >{{
                                        language.order.orderSearch.Button[5]
                                    }}</span
                                >

                                {{ orders.p_name }}
                            </p>

                            <p class="color_one myflex">
                                <span class="ovhide">{{ orders.size }}</span> 
                                <span style="color:#FA9D3B" v-if='orders.isReturn'>退款成功</span>
                            </p>

                            <!-- 订单商品右 -->
                            <div class="commodity-price">
                                <!-- 商品价格积分 -->
                                <p v-if="orders.p_price >= 0">
                                    <view v-if="item.otype == 'KJ'"
                                        ><span class="priceImg">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</span
                                        >{{
                                            LaiKeTuiCommon.formatPrice(
                                            item.spz_price.toFixed(2) ||
                                            orders.p_price.toFixed(2))
                                        }}</view
                                    >
                                    <view v-if="item.otype == 'JP'"
                                        ><span class="priceImg">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</span
                                        >{{
                                            LaiKeTuiCommon.formatPrice((
                                                item.jp && item.jp.jp_price
                                            ).toFixed(2) ||
                                            orders.p_price)
                                        }}</view
                                    >
                                    <view
                                        v-if="
                                            item.otype != 'JP' &&
                                            item.otype != 'KJ' &&
                                            item.otype != 'integral'
                                        "
                                        ><span class="priceImg">{{item.currency_symbol}}</span
                                        >{{ LaiKeTuiCommon.getPriceWithExchangeRate(orders.p_price,item.exchange_rate)}}</view
                                    >

                                    <span
                                        class="commodity-price-integral"
                                        v-if="orders.integral > 0"
                                    >
                                        <span class="priceImg">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</span
                                        >{{ LaiKeTuiCommon.formatPrice(orders.p_price) }}+
                                        <img
                                            class="commodity-price-img"
                                            :src="integral_hei"
                                        />
                                        {{ orders.integral }}
                                    </span>
                                </p>
                                <p v-else>
                                    <span
                                        class="commodity-price-integral"
                                        v-if="orders.integral > 0"
                                    >
                                        <img
                                            class="commodity-price-img"
                                            :src="integral_hei"
                                        />
                                        {{ orders.integral }}
                                    </span>
                                </p>
                                <!-- 购买数量 -->
                                <p class="color_two">
                                    {{ language.order.myorder.gong }} {{ orders.num }} {{ orders.unit }}
                                </p>

                                <!-- 订单右边的按钮 -->
                                <!-- 退款换货 -->
                                <template v-if="item.shop_id != 0">
                                    <div
                                        class="retreat"
                                        style="color: #ff8800"
                                        v-if="
                                            (orders.r_status == 4 ||
                                                orders.r_status == 6) &&
                                            orders.re_type != 0
                                        "
                                    >
                                        {{
                                            orders.re_type == 1 ||
                                            orders.re_type == 2
                                                ? "退款"
                                                : "退换"
                                        }}{{
                                            orders.r_status == 4 ? "中" : "成功"
                                        }}
                                    </div>
                                   
                                </template>
                            </div>
                        </div>

                        <!-- TODO: -->
                        <div
                            style="height: 20rpx; width: 100%"
                            v-if="
                                item.subtraction_list.product_title &&
                                item.list.length - 1 == index1
                            "
                        ></div>

                        <img
                            v-if="
                                item.subtraction_list.product_title &&
                                item.list.length - 1 == index1
                            "
                            :src="item.subtraction_list.imgurl"
                        />

                        <div
                            style="width: 400rpx"
                            v-if="
                                item.subtraction_list.product_title &&
                                item.list.length - 1 == index1
                            "
                        >
                            <p class="order_p_name" style="height: 80rpx">
                                <span style="color: red"
                                    >【{{ language.order.myorder.zp }}】</span
                                >

                                {{ item.subtraction_list.product_title }}
                            </p>

                            <p class="color_one"></p>
                        </div>

                        <div
                            style="flex: 1; padding-right: 30rpx"
                            v-if="
                                item.subtraction_list.product_title &&
                                item.list.length - 1 == index1
                            "
                        >
                            <p>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}0.00</p>
                            <p class="color_two">
                                {{ language.order.myorder.gong }}
                                {{ item.subtraction_list.num }}
                                {{ item.subtraction_list.unit }}
                            </p>

                            <!-- 退款换货 -->
                            <div
                                class="retreat"
                                style="color: #ff8800"
                                v-if="
                                    (orders.r_status == 4 ||
                                        orders.r_status == 6) &&
                                    orders.re_type != 0
                                "
                            >
                                {{
                                    orders.re_type == 1 || orders.re_type == 2
                                        ? "退款"
                                        : "退换"
                                }}
                                {{ orders.r_status == 4 ? "中" : "成功" }}
                            </div>
                        </div>
                    </li>

                    <!-- 商品底部 -->
                    <li class="order_last commodity-footer">
                        <!--  <p class="apply_invoice">
                                <span
                                v-if="item.isInvoice == 1"
                                @tap="applyInvoice(item)"
                                >{{ language.order.myorder.sqkp }}</span
                            > 
                        </p>-->
                        <!-- 实物商品列表使用 -->
                      <btnsList 
                            class="btns-List" 
                            orderType='regularOrder' 
                            :buttonList='item.get_button_list '
                            :orde_id="item.id"
                            :sNo="item.sNo"
                            :orderList="item.list"
                            :invoicePrice='item.invoicePrice || 0'
                            @showToastMask="showToastMask"
                            :parentMethod="refresh"
                            :otype="item.otype"
                            :item='item' 
                            :sno='item.sNo' 
                        ></btnsList>
                   
                    </li>
                </ul>

                <uni-load-more
                    v-if="order.length > 9"
                    :loadingType="loadingType"
                ></uni-load-more>
            </div>

            <div
                v-if="order.length < 1 && !load"
                style="
                    height: 100vh;
                    position: absolute;
                    top: 0;
                    width: 100%;
                    display: flex;
                    align-items: center;
                "
            >
                <div class="noFindDiv">
                    <div><img class="noFindImg" :src="noOrder" /></div>
                    <span class="noFindText">{{
                        language.order.myorder.no_order
                    }}</span>
                    <div
                        @tap="_toHome()"
                        style="
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            margin-top: 60rpx;
                        "
                    >
                        <span class="goHome">{{
                            language.order.myorder.go_shopping
                        }}</span>
                    </div>
                </div>
            </div>

            <view class="mask_invoice" v-if="isMaskInvoice">
                <view class="mask_invoice_content">
                    <view class="invoice_content_top">
                        <p>{{ language.order.myorder.kpts }}</p>
                        <p>{{ language.order.myorder.gddfp }}</p>
                    </view>
                    <view
                        class="invoice_content_bottom"
                        @tap="isMaskInvoice = false"
                    >
                        <p>{{ language.order.myorder.wzdl }}</p>
                    </view>
                </view>
            </view>
        </div>

        <tqm />

        <view class="mask_addInvoice" v-if="isMaskInvoice3">
            <view class="mask_invoice_content">
                <view class="invoice_content_top">
                    <p>{{ language.order.myorder.kpts }}</p>
                    <p>{{ language.order.myorder.nidqzw }}</p>
                </view>
                <view class="invoice_content_bottom">
                    <p @tap="setIsMaskInvoice3(false)">
                        {{ language.order.myorder.qx }}
                    </p>
                    <p @tap="addHeader">{{ language.order.myorder.ljtj }}</p>
                </view>
            </view>
        </view>
        <show-toast
            :is_showToast="is_showToast"
            :is_showToast_obj="is_showToast_obj"
            @richText="richText"
            @confirm="confirm"
        ></show-toast>
        <delModel
            v-model="isDelModel"
            :nocancel="true"
            :content="language.order.myorder.Tips[0]"
            @on-click="onModeOk"
        ></delModel>
        <skeleton :animation="true" :loading="load" bgColor="#FFF"></skeleton>
    </div>
</template>

<script>
import skeleton from "@/components/skeleton";
import search from "@/components/aComponents/search.vue";
import switchNavOne from "@/components/aComponents/switchNavOne.vue";
import switchNavTwo from '@/components/aComponents/switchNavTwo.vue' 
import {mapMutations, mapState} from "vuex"; 
import showToast from "@/components/aComponents/showToast.vue";
import delModel from "@/components/delModel.vue";
import PROCESS from "./DataProcess.js";
import btns from "@/pagesB/order/components/btns";
import btnsList from "@/pagesB/order/components/btnsList";
import tqm from "@/pagesB/order/components/tqm";
import {
    getDIYPageInfoById
} from "@/common/util.js";
import {
    queryOrder
} from "@/common/enus.js";
export default {
    data() {
        return {
            receiving_check: {
                por_list: [],
            },
            receiving_shop: false,
            can_del: true, //是否能删除订单
            fastTap: true,
            title: "我的订单",
            ErrorImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/Default_picture.png',
            jiantou:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/jiantou2x.png",
            storeImg:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/new_store.png",
            serchimg:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/searchNew.png",
            noOrder:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/noOrder.png",
            bback:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/bback.png",
            search2x:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/search2x.png",
            integral_hong:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/integral.png",
            integral_hei:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/integral_hei.png",
            sc_icon:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/delete2x.png",
            sus:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/sus.png", //成功提示图标
            head: true, //头部切换
            header: [],
            order: [], //订单数据
            status_id: "", //订单状态
            orderType_id: "",//订单类型
            sreach_value: "", //搜索框的值
            page: 1, //加载页面
            allLoaded: false,
            loading: false,
            load: true,
            timer: null,
            count: "",
            flag: true,
            loadingType: 0,
            value: "",
            pay_name: "MYORDER",
            shouhuoData: [],
            isDelModel: false,
            bgColor: [
                {
                    item: "#ffffff",
                },
                {
                    item: "#ffffff",
                },
            ],
            isMaskInvoice: false, 
            type: "", //FX分销进入
            // 弹窗提示参数
            is_showToast: 0,
            is_showToast_obj: {},
            jstkItem: {},//需要极速退款的item
            afterSale: false, //是否是售后
            //虚拟商品
            orderType: '',// VI虚拟商品
            //虚拟商品结束
            
            ordTypeObj: {}
        };
    },
    computed:{
        header_label(){
            let that = this.language.order.myorder;
            if(this.type == 'FX'){
                return [that.whole, that.to_paid, that.to_delivered, that.to_Receiving, that.to_evaluated, that.sh]
            } else if(this.orderType == 'VI'){
                return [that.whole, that.to_paid, this.language.toasts.myOrder.dhx, that.to_evaluated]
            } else {
                // DIY兼容
                // const pageInfo = getDIYPageInfoById({
                // 	key: "homeItem17569763719113"
                // })
                // if (pageInfo) {
                // 	const key = Object.keys(pageInfo)[0] 
                // 	const {
                // 		orderCondfig, 
                // 	} = pageInfo[key]
                //     return [{type:'',name:that.whole},...orderCondfig.orderList]
                // }else{
                // }
                    return [that.whole, that.to_paid, that.to_delivered, that.to_Receiving, that.to_evaluated]
            }
        },
        ...mapState({
            isMaskInvoice3: "isMaskInvoice3",
            refreshList:'refreshList',
            jstkSwitch:'jstk'
        }),
    },
    /**
     * 上拉触底事件处理
     * */
    onReachBottom() {
        if (this.loadingType != 0) {
            return;
        }
        this.loadingType = 1; 
        this.page++;
        this._axios();
    },
    /**
     * 监听页面加载
     * */
    onLoad(option) {
        this.setLang();
        this.orderType_id = option.orderType_id || '';
        this.status_id = option.status;
        this.type = option.type;
        if (this.type == 'FX') {
            this.title = this.language.order.myorder.fxdd;
        } else {
            this.title = this.language.order.myorder.title;
        }
        this.header =[this.language.toasts.myOrder.swdd,this.language.toasts.myOrder.xndd]
    }, 
    watch: {
        value: function (newValue, oldValue) {
            this.changeValue();
        },
        refreshList(newvalue){
            this.refresh(newvalue)
        },
        jstkSwitch(newvalue){
            if(newvalue){
                this._jstk(JSON.parse(newvalue))
            }else{
                this.richText()
            }
        }
    },
    /**
     * 监听实例销毁之前
     * */
    beforeDestroy() {
        this.load = true;
        clearInterval(this.timer);
        this.order = [];
    },
    /**
     * 监听页面显示
     * */
    onShow() {
        if(uni.getStorageSync('skipType')){
            uni.removeStorageSync('skipType')
        }
        this.sreach_value = ""; //清除搜索内容
        this.setIsMaskInvoice3(false)
        this.load = true;
        this.order = [];
        this.page = 1;
        this.loadingType = 0;
        this.flag = true;
        this._axios();
        uni.reLaunch();
        uni.setStorageSync("vipSource", "0");
        if (this.afterSale) {
            let me = this;
            me.$nextTick(() => {
                me.$refs.switchNavOne._isSwitchNav(0);
            });
        }
    },
    mounted() {
        setTimeout(()=>{
            //订单类型
            if(this.orderType_id){
                this.$refs.switchNavOne._isSwitchNav(this.orderType_id);
            }
            //订单状态
            this.$refs.switchNavTwo.isSwitchLable = this.status_id;
        }, 1000)
    },
    components: {
        showToast,
        delModel,
        btnsList,
        btns,
        tqm,
        search,
        switchNavOne,
        switchNavTwo,
        skeleton
    },
    methods: {
        /**
         * 极速退款
         * @param {Object} item 子组件传出参数
         */
        _jstk(item){
            this.is_showToast = 4
            this.is_showToast_obj.title = this.language.jstk.jstktx
            this.is_showToast_obj.content =  this.language.jstk.qdyjstkm
            this.is_showToast_obj.button = this.language.jstk.qx
            this.is_showToast_obj.endButton = this.language.jstk.qr
            this.jstkItem = item
        },
        /**
         * 极速退款 - 取消
         */
        richText(){
            //隐藏弹窗
            this.is_showToast = 0
            this.SET_JSTK('')
        },
        /**
         * 极速退款 - 确认
         */
        confirm(){
            //隐藏弹窗
            this.is_showToast = 0
            this.SET_JSTK('')
            // -- 开始请求 --
            let type = 2                                                // 2为仅退款状态
            let order_details_id = ''                                   // 订单id
            this.jstkItem.list.forEach((item, index)=>{
                order_details_id = order_details_id + item.id + ','
            })
            let refund_apply_money = this.jstkItem.z_price              // 订单总价
            let refund_amount = this.jstkItem.z_price                   // 订单总价
            let explain = '极速退款'                                     // 订单备注
            let data = {
                api: 'app.order.ReturnData',
                type,
                order_details_id,
                refund_apply_money,
                refund_amount,
                explain,
            } 
            if(this.jstkItem.vipGoods){
                data.oType = 'GM'
            }
            this.$req.post({ data }).then(res => {
                uni.showToast({
                    title: res.message,
                    icon: 'none'
                });
                if(res.code == 200 && res.data) {
                    //用于在退款结果页面显示数据
                    uni.setStorageSync('orderEnd', res.data)
                    uni.navigateTo({
                        url: "/pagesA/returnGoods/refund?refund_type=2&order_details_id=" + order_details_id + '&return_suess=true&order_end=true&tkType=js'
                    });
                }
            })
            .catch(error => {
                console.error('极速退款 - 确认~', error);
            });
        },
        handleErrorImgHeard(index){
            this.order[index].shopHead_img=this.ErrorImg
        },
        // 图片报错处理
        handleErrorImg(index, index1) {
            this.order[index].list[index1].imgurl=this.ErrorImg
        },
        addHeader() {
            uni.navigateTo({
                url: "/pagesB/invoice/addInvoiceHeader",
            });
        },
        ...mapMutations({
            cart_id: "SET_CART_ID",
            order_id: "SET_ORDER_ID",
            address_id: "SET_ADDRESS_ID",
            status: "SET_STATUS",
            setIsMaskInvoice3:'SET_ISMASKINVOICE3',
            SET_JSTK:'SET_JSTK', 
        }),
        
        cleardata() {
            this.sreach_value = "";
            this.$nextTick(()=>{
                this.$refs.search._clearData()
            })
        },
        onModeOk() {
            this.isDelModel = !this.isDelModel;
        },
        /**
         *
         * 设置请求数据选中项类型
         * '' 全部 ||payment(待付款) send(代发货) receipt(待收货) evaluete(待评价) return(售后) write(待核销)"
         * 状态 0:未付款 1:未发货 2:待收货 5:已完成  7:订单关闭 8:待核销
         */
        getOrderType(data) {
           
            // DIY 环境下使用
            if(this.ordTypeObj && Object.keys(this.ordTypeObj).length){
                data.queryOrderType = queryOrder(this.ordTypeObj.type)
                return
            }
            const sIdNumb = Number(this.status_id)
            if (sIdNumb === 0) {
                data.queryOrderType = "";
            } else if (sIdNumb === 1) {
                data.queryOrderType = "payment";
            } else if (sIdNumb === 2) {
                // 虚拟商品没有待发货
                if(this.orderType == 'VI'){
                    data.queryOrderType = "receipt";
                } else {
                    data.queryOrderType = "send";
                }
            } else if (sIdNumb === 3) {
                if(this.orderType == 'VI'){
                    data.queryOrderType = "evaluete";
                } else {
                    data.queryOrderType = "receipt";
                }
            } else if (sIdNumb === 4) {
                data.queryOrderType = "evaluete";
            }
            
            
            
            
            //兼容java
            // data.queryOrderType=data.order_type;
        },
        changeLoginStatus() {
            this._axios();
        },
        _goStore(shop_id) {
            uni.navigateTo({
                url: "/pagesB/store/store?shop_id=" + shop_id,
            });
        },
        /**
         * 去逛逛
         */
        _toHome() {
            if ((this.type == "FX")) {
                uni.navigateTo({
                    url: "/pagesA/distribution/fxProduct",
                });
            } else {
                uni.redirectTo({
                    url: "/pages/shell/shell?pageType=home",
                });
            }
        },
        /**
         *
         * 查看订单详情
         */
        _navigateTo(status, id, otype, item) {
            if (item.shop_id == 0) {
                return;
            }

            let url = "/pagesB/order/order?order_id=" + id;

            if (status == 0) {
                url += "&showPay=true";
            }

            // #ifdef H5
            url = "/pagesB/order/order?order_id=" + id;
            // #endif

            this.navTo(url);
           

            this.flag = false;
        },
        /**
         * 类型切换
         */
        _header_index(index){
            if(index == 1){
                this.orderType = 'VI'
            } else {
                this.orderType = ''
            }
            this.$refs.switchNavTwo._isSwitchLable(this.status_id || 0)
        },
        /**
         * 导航栏切换
         */
        _header_label_index(index) {
            this.afterSale = false;
            const typeStr = Object.prototype.toString.call(index) 
            if(typeStr === '[object Number]'){
                //回到顶部
                if (index == 5) {
                    this.afterSale = true;
                    uni.navigateTo({
                        url: "/pagesC/afterSale/afterSale?type=FX",
                    });
                    return;
                }
                this.load = true;
                this.order = [];
                this.isLogin(() => {
                    this.page = 1; 
                    this.status(index);
                    this.status_id = Number(this.$store.state.status);
                    this._axios();
                });
            }else{
                // DIY
                this.ordTypeObj = index
                 this._axios();
            }
        },
        // 自定义提示框
        showToastMask(parameter) {
            this.is_showToast = 1;
            this.is_showToast_obj.imgUrl =
                parameter.imgUrl == 0 ? this.sus : "";
            this.is_showToast_obj.title = parameter.title;
            setTimeout(() => {
                this.is_showToast = 0;
            }, 1500);
        },
        refresh(orderId) {  
            this.load = true; 
            this.order = [];
            this.page = 1;
            this.loadingType = 0;
            this._axios(); 
        },
        /**
         *
         * 加载订单数据
         * */
        async _axios() {
            let data = {
                page: this.page,
                api: "app.order.index",
            };
            if (this.type == "FX") {
                //分销订单
                data.order_type = "FX";
            } else if(this.orderType == 'VI') {
                //虚拟订单
                data.order_type = "VI";
            }
            // 判断订单状态
            this.getOrderType(data);
            // 先判断是否登录或者号被挤掉再请求订单数据
            await this.isLogin(async () => {
                await this.$req
                    .post({ data })
                    .then((res) => {
                        this.load = false;

                        let {
                            data: { order },
                        } = res;

                        PROCESS(order, data.order_type, this);
                        
                        if (Array.isArray(order)) {
                            order = order.map((item) => {
                                if (
                                    Array.isArray(item.subtraction_list) ||
                                    !item.subtraction_list
                                ) {
                                    item.subtraction_list = {
                                        product_title: "",
                                        num: "",
                                        imgurl: "",
                                    };
                                }
                                return item;
                            });
                        }

                        if (this.page > 1) {
                            this.order.push(...order);  
                            // 使用 Map 来去重
                            const uniqueOrders = new Map();
                            let arr = []
                            // 遍历 order 数组
                            for (const item of this.order) {
                                // 使用 sNo 作为 Map 的键
                                uniqueOrders.set(item.sNo, item);
                            }
                            
                            // 将去重后的元素推入 this.order
                            arr.push(...Array.from(uniqueOrders.values()));
                            this.order=JSON.parse(JSON.stringify(arr))
                        } else {
                            this.order = order;
                        }
                        

                        if (order.length < 10) {
                            this.allLoaded = true;
                            this.loadingType = 2;
                        } else {
                            this.loadingType = 0;
                            this.allLoaded = false;
                        }
                        //跳转分销中心
                        if(this.type == "FX"&&res.data.isDistribution==0){
                            let isdi=true
                            for(let i in res.data.res_order){
                                if(res.data.res_order[i]>0){
                                    isdi=false
                                    return
                                }
                            }
                            if(isdi){
                                uni.redirectTo({
                                   url: '/pagesA/distribution/distribution_center'
                                });  
                            }
                            
                        }
                    })
                    .catch((err) => {
                        uni.showToast({
                            title: err.message,
                            duration: 1500,
                            icon: "none",
                        });
                    });
            });
        },
        /**
         * 评价
         */
        comment(orders) {
            let { id: order_details_id, comments_type } = orders;
            if (comments_type == 1) {
                uni.navigateTo({
                    url:
                        "../evaluate/evaluating?order_details_id=" +
                        order_details_id +
                        "&num=all",
                });
            } else {
                uni.navigateTo({
                    url:
                        "../evaluate/evaluating?order_details_id=" +
                        order_details_id +
                        "&add=true&num=all",
                });
            }
        },
        /**
         * 搜索
         */
        _seart(item) {
            if (item == "") {
                uni.showToast({
                    title: this.language.order.myorder.search_gjc,
                    duration: 1000,
                    icon: "none",
                });
            } else {
                uni.navigateTo({
                    url:
                        "/pagesB/order/orderSearch?sreach_value=" +
                        item +
                        "&type=" +
                        this.type,
                });
                this.cleardata()  
            }
        },
        inputConfir() {
            if (this.sreach_value === "") {
                uni.showToast({
                    title: this.language.order.myorder.search_gjc,
                    duration: 1000,
                    icon: "none",
                });
            } else {
                uni.navigateTo({
                    url:
                        "/pagesB/order/orderSearch?sreach_value=" +
                        this.sreach_value,
                });
            }
        },
        returnGoods(orders) {
            let str = "";
            if (orders.re_type == 1 || orders.re_type == 2) {
                str = "退款";
            } else {
                str = "退换";
            }
            if (orders.r_status == 4) {
                str += "中";
            } else {
                str += "成功";
            }

            return str;
        },
    },
};
</script>

<style>
page {
    background: #f4f5f6;
}
</style>
<style lang="less" scoped> @import url("@/laike.less");
@import url("../../static/css/order/myOrder.less");
/deep/.skeleton-p {
    top: 390rpx !important;
}
/deep/.switchLable{
    z-index: 999 !important;
}
.myflex{
    display: flex;
    justify-content: space-between;
}
.ovhide{
    width: 400rpx;
    height: auto;
    display: inline-block;
    overflow: hidden;
    text-overflow: ellipsis;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 1;
}
.btns-List{
    width: 100%;
}
</style>
