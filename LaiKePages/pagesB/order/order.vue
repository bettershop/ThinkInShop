<template>
    <div class="order">
        <heads :title="language.order.order.title" :ishead_w="ishead_w" :bgColor="bgColor" :titleColor="titleColor"
            :returnR="returnR" :types="types" :order_type="order_type"></heads>
        <toload v-if="load"></toload>
        <div v-else style="position: relative">
            <div class="order_zt" id="order_zt">
                <p>{{orderStatus}}</p>
                <div class="order_p">
                    <p>{{orderStatusDetails}}</p>
                </div>
            </div>
            
            <!-- 直播的主播信息 -->
            <view class="order_live" v-if="order.otype==='ZB'">
                <view class="live_img">
                    <img :src="order.live_img" alt="">
                </view>
                <view class="live_tetx">来自"{{ order.anchor_name }}"直播间的推荐</view>
            </view>
            
            <!-- 地址信息 -->
            <div class="address_one" :style="order.otype==='ZB'?'margin-top: 16rpx;':'margin-top: 24rpx;'" v-if="otype != 'VI'">
                <p class="address_one_a">{{ order.address }}</p>
                <p class="address_one_c">
                    {{
                        `${order.omsg.sheng}${order.omsg.shi}${order.omsg.xian}`
                    }}
                </p>
                <p class="address_one_b">{{ order.name }} {{ order.mobile }}</p>
            </div>
            
            <template v-if="otype == 'VI' && show_write_store == 1">
                <!-- 虚拟商品无预约 适用核销门店 -->
                <view v-if="order.show_appointment != 1 && order.list.length == 1" class="hxShop" @tap="_navigateTo('/pagesB/store/storeHx?shop_id=' + order.list[0].shop_id + '&pro_id=' + order.list[0].p_id)">
                    <p>适用核销门店</p>
                    <view>
                        <span>{{write_store_num}}家</span>
                        <image :src="baga"></image>
                    </view>
                </view>
                <!-- 虚拟商品预约 预约详情 -->
                <view v-if="order.show_appointment == 1" class="hxTime">
                    <view>
                        预约时间：{{appointment.time}}
                    </view>
                    <view>
                        <span>预约门店：{{appointment.name}}</span>
                        <span>{{appointment.address}}</span>
                    </view>
                </view>
            </template>
            <!-- order_goods 增加间距逻辑 bugid 56065 （虚拟商品无需核销）--> 
            <ul class="order_goods" :style="{marginTop:otype == 'VI' && show_write_store != 1?'24rpx':''}">
                <li class="order_goods_lis lis_one" v-for="(items, indexs) of order.stores">
                    <div class="order_one" @click="_goStore(items.shop_id)">
                        <img :src="storeImg" />
                        {{ items.shop_name }}
                        <img class="dd-boxa-img" style="width: 32rpx;height: 44rpx;" :src="jiantou" />
                    </div>

                    <template v-for="(item, index) of order.list">
                        <template v-if="item.shop_id == items.shop_id">
                            <div class="order_two" :key="index" 
                             @tap=" _goods(
                                            item.p_id,
                                            item.recycle,
                                            item.pluginId
                                        )
                                    ">
                                <img :src="item.imgurl" class="goodsImg" @error="handleErrorImg(index)" />
                                <div class="order_two_a">
                                    <p class="order_p_name">
                                        {{ item.p_name }}
                                    </p>
                                    <view class="price_num">
                                        <p v-if="
                                                order.status == 0 &&
                                                (order.otype == 'pt' ||
                                                    order.otype == 'PP')
                                            " class="price">
                                            <span class="price_symbol">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</span>{{
                                                LaiKeTuiCommon.formatPrice(Number(
                                                    order.product_total2
                                                ))
                                            }}
                                        </p>
                                        <p v-else-if="order.otype == 'KJ'" class="price">
                                            <span class="price_symbol">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</span>{{
                                                LaiKeTuiCommon.formatPrice( Number(
                                                    order.omsg.spz_price
                                                ))
                                            }}
                                        </p>
                                        <p v-else-if="order.otype == 'IN'" style="width: 400rpx" class="price">
                                            <span v-if="item.p_price > 0">
                                                {{ language.order.order.jf
                                                }}{{
                                                    order.list[0]
                                                        .scoreDeduction
                                                }}+
                                                <span class="price_symbol">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</span>{{
                                                    LaiKeTuiCommon.formatPrice(Number(
                                                        item.p_price
                                                    ))
                                                }}</span>
                                            <span v-else>
                                                {{ language.order.order.jf
                                                }}{{
                                                    order.list[0].scoreDeduction
                                                }}</span>
                                        </p>
                                        <p v-else class="price">
                                            <span class="price_symbol">
                                                {{order.omsg.currency_symbol || order.currency_symbol}}
                                            </span>
                                            {{LaiKeTuiCommon.getPriceWithExchangeRate(item.p_price,order.omsg.exchange_rate || order.exchange_rate)}}
                                        </p>
                                        <p class="color_two">x{{ item.num }}</p>
                                    </view>
                                    <p class="color_one XN-ysy">
                                        <span>{{item.size}}</span>
                                        <!-- write_off_settings 1.虚拟商品线下核销 2.虚拟商品无需核销 -->
                                        <span v-if="item.write_off_settings == 1" class="xnStatus">{{item.r_status==5?'已使用':'待使用'}}</span>
                                    </p>
                                </div>
                            </div>
                            
                            <div class="btns" :class="item.is_addp && 'addp'" >  
                                <!-- 申请售后按钮 -->
                                <div class="order_goods_adiv">
                                    <!-- 查看售后 -->
                                    <!-- is_addp 字段 只有 显示折扣订单才会有 ，等于1 则表示 是加购的副商品 --> 
                                    <btnsList
                                         v-if="!iszb"
                                         orderType='regularOrder' 
                                         :buttonList='item.get_order_details_button'
                                         :orde_id="orde_id"
                                         :sNo="sNo"
                                         :item="item"
                                         :orderList="order.list"
                                         @refresh="_axios" 
                                     ></btnsList>
                                       
                                </div>
                            </div>
                            <div class="complete complete_b" :key="index + 'com'" v-if="
                                    item.r_status == 4 &&
                                    item.r_type == 2 &&
                                    display_t
                                ">
                                <div class="complete_qiandao">
                                    <img class="complete_img" :src="guanbi" @tap="_onafter" />
                                    <p>
                                        {{ language.order.order.Reasons }}：{{
                                            rr_content
                                        }}
                                    </p>
                                </div>
                            </div>
                            <div class="" :key="index + 'com_1'"></div>
                        </template>
                    </template>
                    <div class="subtraction_list" v-if="subtraction_list">
                        <div class="order_two">
                            <img :src="subtraction_list.imgurl" />
                            <div class="order_two_a">
                                <span style="color: red">【{{ language.order.order.gift }}】</span>
                                {{ subtraction_list.product_title }}
                            </div>
                            <div>
                                <p>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}0.00</p>
                                <p class="color_two">
                                    x{{ subtraction_list.num }}
                                </p>
                            </div>
                        </div>
                        <div class=""></div>
                    </div>
                </li>
                
                <li class="order_last">
                    <div class="order_color">
                        <p class="goodsTotal">
                            {{ language.order.order.total }} 
                        </p>
                        <p class="goodsPrice" v-if="
                                order.status == 0 &&
                                (order.otype == 'pt' || order.otype == 'PP')
                            ">
                            {{order.omsg.currency_symbol || order.currency_symbol }}{{ LaiKeTuiCommon.getPriceWithExchangeRate(Number(order.product_total2),order.omsg.exchange_rate  ||  order.exchange_rate) }} 
                        </p>
                        <p v-else-if="order.allow >0" class="order_color_a goodsPrice">
                            {{ language.order.order.jf }}{{ order.allow }}
                            <span v-if="order.product_total > 0">+{{order.omsg.currency_symbo || order.currency_symboll}}{{ LaiKeTuiCommon.formatPrice(order.product_total) }}  </span>
                        </p>
                        <p class="goodsPrice" v-else>
                            {{order.omsg.currency_symbol || order.currency_symbol}}{{ LaiKeTuiCommon.getPriceWithExchangeRate(Number(order.product_total),order.omsg.exchange_rate  ||  order.exchange_rate) }} 
                        </p>
                    </div>
                    <div class="order_color" v-if="otype == 'ZB'&&order.coupon_price>0">
                        <p class="goodsTotal">
                            店铺优惠
                        </p>
                        <p class="goodsPrice">
                            {{order.omsg.currency_symbol || order.currency_symbol}}
                            {{ LaiKeTuiCommon.getPriceWithExchangeRate(Number(order.coupon_price),order.omsg.exchange_rate  ||  order.exchange_rate) }} 
                        </p>
                    </div>
                    <template v-if="otype == 'GM' || otype == '' || otype == 'VI'">
                        <div class="order_color" v-if="order.coupon_price > 0">
                            <p>{{ language.order.order.Discount[0] }}</p>
                            <p>{{order.omsg.currency_symbol || order.currency_symbol}}
                            {{ LaiKeTuiCommon.getPriceWithExchangeRate(Number(order.coupon_price),order.omsg.exchange_rate  ||  order.exchange_rate) }} </p>
                        </div>
                        <div v-if="order.discount_type" class="order_color">
                            <p>
                                {{ order.discount_type
                                }}{{ language.order.order.Discount[1] }}
                            </p>
                            <p>
                                {{order.omsg.currency_symbol}}{{
                                    LaiKeTuiCommon.getPriceWithExchangeRate(Number(order.preferential_amount),order.omsg.exchange_rate  ||  order.exchange_rate)
                                }}
                            </p>
                        </div>
                    </template>
                    <template v-else>
                        <div class="order_color" v-if="
                                is_distribution != 1 &&
                                order.comm_discount == 1 &&
                                otype != 'JP' &&
                                otype != 'KJ' &&
                                otype != 'pt' &&
                                otype != 'pp' &&
                                otype != 'MS' &&
                                otype != 'PM' &&
                                otype != 'vipzs' &&
                                otype != 'VIP'
                            ">
                            <p>{{ language.order.order.Discount[3] }}</p>
                            <p>
                                {{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}
                            {{ LaiKeTuiCommon.getPriceWithExchangeRate(Number(order.coupon_price),order.omsg.exchange_rate  ||  order.exchange_rate) }} 
                                <span v-if="isDiscount">{{
                                    order.coupon_name
                                }}</span>
                            </p>
                        </div>
                        <div class="order_color" v-if="
                                order.grade_rate < 1 &&
                                order.grade_rate > 0 &&
                                otype != 'vipzs' &&
                                otype != 'VIP' &&
                                otype != 'MS'
                            ">
                            <p>{{ language.order.order.Discount[5] }}</p>
                            <p>
                                {{ order.grade_rate * 10
                                }}{{ language.order.order.fracture }}
                            </p>
                        </div>
                        <div class="order_color" v-if="
                                is_distribution != 1 &&
                                order.comm_discount == 1 &&
                                otype != 'KJ' &&
                                otype != 'JP' &&
                                otype != 'pt' &&
                                otype != 'pp' &&
                                otype != 'MS' &&
                                otype != 'PM' &&
                                otype != 'vipzs' &&
                                otype != 'VIP'
                            ">
                            <p>{{ language.order.order.Discount[6] }}</p>
                            <p>{{ order.coupon_activity_name }}</p>
                        </div>
                    </template>
                    <!-- 运费 -->
                    <div class="order_color" v-if="otype != 'VI'">
                        <p>{{ language.order.order.freight }}</p>
                        <p>
                            {{order.omsg.currency_symbol || order.currency_symbol }}{{ LaiKeTuiCommon.getPriceWithExchangeRate(Number(order.old_freight),order.omsg.exchange_rate  ||  order.exchange_rate) }}
                        </p>
                    </div>
                    <!-- 分销折扣 -->
                    <div class="order_color" v-if="otype == 'FX'">
                        <p>{{ language.pay.orderDetailsr.fxzk }}</p>
                        <p>{{order.omsg.currency_symbol || order.currency_symbol}}{{ LaiKeTuiCommon.getPriceWithExchangeRate(Number(order.comm_amount),order.omsg.exchange_rate  ||  order.exchange_rate) }}</p>
                    </div>
                    <!-- 会员优惠 -->
                    <div class="order_color" v-if="order.omsg.grade_fan>0">
                        <p>{{ language.order.order.hyyh }}</p>
                        <p>
                            {{order.omsg.currency_symbol || order.currency_symbol}}{{ LaiKeTuiCommon.getPriceWithExchangeRate(order.omsg.grade_fan,order.omsg.exchange_rate  ||  order.exchange_rate) }}
                        </p>
                    </div>
                    <!-- 订单总价 -->
                    <div class="order_color">
                        <p class="margin-top-zj mt_0 ">
                            {{ language.order.order.order_total }}
                        </p>
                        <p class="margin-top-zj mt_0" v-if="order.otype != 'IN'">
                            {{order.omsg.currency_symbol || order.currency_symbol }}{{ LaiKeTuiCommon.getPriceWithExchangeRate(order.old_total,order.omsg.exchange_rate ||  order.exchange_rate) }}
                        </p>
                        <p class="margin-top-zj margin-top-flex mt_0" v-else>
                            {{ language.order.order.jf }}{{ order.allow }}
                            <span v-if="order.old_total > 0">+
                                {{order.omsg.currency_symbol || order.currency_symbol}}{{ LaiKeTuiCommon.getPriceWithExchangeRate(order.old_total,order.omsg.exchange_rate ||  order.exchange_rate) }}
                            </span>
                        </p>
                    </div>
                    <!-- 购物折扣 -->
                    <!-- 分销订单不显示 -->
                    <div class="order_color" v-if="
                           otype != 'FX' &&
                            order.comm_discount != 0 && order.comm_discount != 1
                        ">
                        <p>{{ language.order.order.gwzk }}</p>
                        <p>
                            {{order.omsg.currency_symbol || order.currency_symbol}}{{ LaiKeTuiCommon.getPriceWithExchangeRate(order.comm_discount,order.omsg.exchange_rate  ||  order.exchange_rate) }}
                        </p>
                    </div>
                </li>
                <!-- 间隔线 -->
                <!-- <li class="order_pay_li mt_0"></li> -->
                <!-- 实际付款 -->
                <li class="order_pay">
                    <div class="order_color" style="margin-bottom: 0">
                        <p v-if='order.status==0||(order.status==7&&!order.omsg.pay_time)'>{{language.yhk}}</p>
                        <p v-else>{{ language.order.order.Actual_payment }}</p>
                        <p class="z_price_bold" v-if=" order.allow == 0">
                            {{order.omsg.currency_symbol || order.currency_symbol}}{{ LaiKeTuiCommon.getPriceWithExchangeRate(order.old_total,order.omsg.exchange_rate ||  order.exchange_rate) }}
                        </p>
                        <p class="z_price_color" v-else>
                            {{ language.order.order.jf }}{{ order.allow }}
                            <span>+{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(order.old_total)}}</span>
                        </p>
                    </div>
                </li>

                <!-- 订单备注 -->
                <li class="order_pay order_pay_remark" v-if="order.remarks">
                    <div class="order_color order_remarks"
                        style="min-height: 108rpx;margin-bottom:0rpx;padding-top: 32rpx;">
                        <p>{{ language.order.order.Order_notes }}</p>
                        <p class="order_color_b" style="place-self: none;margin-bottom:32rpx;">
                            <view style="font-weight: 400;overflow: hidden;
          -webkit-line-clamp: 2;
          text-overflow: ellipsis;
              text-align: end;
          display: -webkit-box;
          -webkit-box-orient: vertical;" class="order_color_b_content" @tap="addRemarks" v-if="!isMoreRemarks">
                                {{ order.remarks }}
                            </view>
                            <view class="order_color_b_content" style="font-weight: 400;overflow: hidden;
          -webkit-line-clamp: 2;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-box-orient: vertical;" @tap="addRemarks" v-if="isMoreRemarks">{{ order.remarks }}</view>
                        </p>
                    </div>
                </li>
            </ul>
            <div class="order_xx" :class="
                    !order.isInvoice &&
                    otype != 'VI' &&
                    (order.status == 3 ||
                        order.status == 5 ||
                        order.status == 8)
                        ? 'order_invoice_xx'
                        : ''
                ">
                <view class="order_xx_top">
                    <p>{{ language.order.order.order_number }}</p>
                    <p>
                        <span @tap="onCopy()">{{
                            language.order.order.copy
                        }}</span>
                        <text class="order-id">{{ order.sNo }}</text>
                    </p>
                </view>
                <view class="order_xx_top">
                    <p>{{ language.order.order.Order_time }}</p>
                    <p>{{ order.add_time }}</p>
                </view>
                <view class="order_xx_top" v-if="order.storeSelfInfo && order.storeSelfInfo.delivery_time">
                    <p>配送时间</p>
                    <p>{{ order.storeSelfInfo.delivery_time + '  ' + (order.storeSelfInfo.delivery_period == 1?'上午':'下午') }}</p>
                </view>
                <view class="order_xx_top" v-if="order.storeSelfInfo && order.storeSelfInfo.courier_name">
                    <p>配送员姓名</p>
                    <p>{{ order.storeSelfInfo.courier_name}}</p>
                </view>
                <view class="order_xx_bottom" v-if="order.storeSelfInfo && order.storeSelfInfo.phone">
                    <p>配送员电话</p>
                    <p>{{ order.storeSelfInfo.phone}}</p>
                </view>
            </div>
            
            <!-- 虚拟商品联系客服 -->
            <view v-if="otype == 'VI'" class="hxKf" @tap="_navigateTo('/pagesB/message/buyers_service/Regular_customer?mch_id='+order.list[0].shop_id)">
                <image :src="my_kefu"></image>
                <span>联系客服</span>
            </view>

            <view v-if="
                    !order.isInvoice &&
                    (order.status == 3 ||
                        order.status == 5 ||
                        order.status == 8)
                " :class="
                    !order.isInvoice &&
                    (order.status == 3 ||
                        order.status == 5 ||
                        order.status == 8)
                        ? 'order_invoice'
                        : ''
                ">
                <view class="order_invoice_tips">
                    ~ {{ language.order.order.cspbj }} ~
                </view>
            </view>
            
            <div class="order_foot-wrap" style="height: 98rpx">
                <div class="order_foot"  >  
                 <btnsList 
                        v-if='!iszb'
                       orderType='regularOrder' 
                       :buttonList='order.get_button_list'
                       :orde_id="orde_id"
                       :sNo="sNo"
                       :item="order" 
                       :jstk="_jstk"
                       :orderList="order.list"
                       :otype="order.otype" 
                       @showToastMask="showToastMask" 
                       @pay_display="goPay_display"
                   ></btnsList>
                  <!-- <btns 
                        form="order"
                        :status="status" 
                        :isOrderDel="isOrderDel" 
                        :haveExpress='haveExpress' 
                        :isExtract="isExtract"
                        :self_lifting="self_lifting" 
                        :sale_type="sale_type" :delivery_status="delivery_status"
                        :orde_id="orde_id" 
                        :otype="otype" 
                        :sNo="sNo" 
                        :isItem="order"
                        :goodsNum="order.list.length" 
                    @refresh="_axios"/> -->
                    <!-- 非限时折扣的 申请售后 -->  
                </div>
            </div>

            <!-- 备注弹窗（样式类与发票弹窗相似） -->
            <view class="mask_addInvoice" v-if="isMaskRemarks">
                <view class="mask_invoice_content">
                    <view class="invoice_content_top mask_remarks_content">
                        <p class="remarksTitle">
                            {{ language.order.order.bz }}
                        </p>
                        <p class="remarksContent">{{ order.remarks }}</p>
                    </view>
                    <view class="invoice_content_bottom">
                        <p @tap="isMaskRemarks = false">
                            {{ language.order.order.wzdl }}
                        </p>
                    </view>
                </view>
            </view>

            <view class="mask_addInvoice" v-if="isMaskInvoice">
                <view class="mask_invoice_content">
                    <view class="invoice_content_top">
                        <p>{{ language.order.order.kpts }}</p>
                        <p>{{ language.order.order.ndqzw }}</p>
                    </view>
                    <view class="invoice_content_bottom">
                        <p @tap="isMaskInvoice = false">
                            {{ language.order.order.qx }}
                        </p>
                        <p @tap="addHeader">{{ language.order.order.ljtj }}</p>
                    </view>
                </view>
            </view>
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
            <tqm></tqm>
        </div>
        <payModel @cancel="wallet_pay_cancel" @success="check_pay_password" v-model="pay_display" />
    </div>
</template>

<script>
    import {
        _after,
        _axios
    } from "../../static/js/order/order.js";
    // #ifdef H5
    import {
        copyText
    } from "@/common/util.js";
    // #endif
    import {
        mapMutations,mapState
    } from "vuex";
    import btns from "@/pagesB/order/components/btns";
    import btnsList from "@/pagesB/order/components/btnsList";
    import tqm from "@/pagesB/order/components/tqm";
    import showToast from "@/components/aComponents/showToast.vue";
    import mixinsPay from '../../mixins/pay'
    import payModel from '@/components/paymodel.vue'
    import mixinsOrder from '../../mixins/order'
    
    export default {
        mixins: [mixinsPay,mixinsOrder],
        data() {
            return {
                returnR: "",
                types: "",
                password_status: "",
                integral_hui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    "images/icon/integral_hui.png",
                integral_hei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    "images/icon/integral_hei.png",
                integral_hong: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    "images/icon1/integral.png",
                finish2x: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    "images/icon/finish2x.png",
                storeImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    "images/icon/store.png",
                guanbiImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    "images/icon/close2x.png",
                quan_hui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    "images/icon/xuanzehui2x.png",
                quan_hei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    "images/icon/xuanzehei2x.png",
                guanbi: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    "images/icon/qiandaoguanbi2x.png",
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    "images/icon/jiantou2x.png",
                orderbg_dsh: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    "images/icon1/orderbg_dsh.png",
                orderbg_qt: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    "images/icon1/orderbg_qt.png",
                baga: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/baga.png',
                my_kefu: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/my_kefu.png',
                notWallte: false,
                useWallte: false,
                pay_display: false, //支付弹窗
                iscan: "",
                focus: true,
                a_type: "",
                msg: "",
                remarks: "", //订单备注
                frist_show: true,
                msgLength: 0,
                digits: ["", "", "", "", "", ""],
                pay_index: -1,
                can_pay: true,
                pay_style: "", //支付方式
                content: "是否抵扣余额？",
                enterless: true,
                z_price_: "", //最初默认总价
                title: "订单详情",
                orde_id: "", //订单ID
                showBtn: true,
                sale_type: "",
                delivery_status: "",
                self_lifting: "",
                order: "",
                order_wl: "", //物流信息显示状态
                order_zt: "", //
                time_c: "", //待付款倒计时，时间差
                time_D: "", //待付款倒计时，天
                time_H: "", //待付款倒计时，小时
                time_M: "", //待付款倒计时，分
                time_s: "", //待付款倒计时，秒
                message: "",
                stau_num: 0, //提醒发货
                orderInfo: [], //订单信息
                count: "",
                load: true,
                user_can_open: "", //是否能够继续开团
                user_can_can: "", //是否能够继续参团
                user_can_after: "", //是否申请售后
                logistics: [],
                display_t: false,
                rr_content: "",
                sNo: "",
                fastTap: true,
                otype: "", //订单类型
                endpay: "", //剩余支付
                pttype: "", //剩余支付
                // gstatus:'',
                ordermsg: "", //订单信息
                p_id: "", //产品id
                kanjia: false,
                firstFlag: true,
                head: true, //头部切换
                r_status_: "",
                status: "",
                isExtract: "",
                isOrderDel: "",
                haveExpress: false,
                has_status: false,
                cancelGoods: "", //退货中的状态
                cancelGoodsReason: "", //退货原因
                flag: false,
                is_remove_order: false,
                is_distribution: 0,
                is_end: false, //拼团活动是否结束（true结束 false未结束）
                axios_times: 0,

                subtraction_list: null,
                p_sNo: 0,
                group: 0, // 是否是拼团订单,

                order_type: "",

                bgColor: [{
                        item: "#FA5151 ",
                    },
                    {
                        item: "#D73B48",
                    },
                ],
                ishead_w: 3,
                titleColor: "#ffffff",
                isBg: true,
                isBlock: false,
                isMaskInvoice: false, //发票提示弹窗
                isMaskRemarks: false, //备注信息弹窗
                isMoreRemarks: false, //订单备注是否超过两行显示省略号
                show_write_store: '',//虚拟商品 是否线下预约 1需要预约
                write_store_num: '',//虚拟商品 核销门店数量
                is_showToast:'',
                is_showToast_obj: {},
                jstkItem: {},//需要极速退款的item
                appointment: '',//虚拟商品 预约时间信息
                iszb:false,
            };
        }, 
        computed: {
            ...mapState({
                isMaskInvoice3: "isMaskInvoice3",
                refreshList:'refreshList', 
                jstkSwitch:'jstk'
            }),
            //订单状态
            orderStatus(){
                let orderStatus = ''
                //待付款
                if(this.order.status == 0){
                    orderStatus = this.language.order.order.state[0]
                } 
                //待发货
                else if(this.order.status == 1){
                    orderStatus = this.language.order.order.state[1]
                } 
                //待收货
                else if(this.order.status == 2){
                    if(this.order.self_lifting != 1){
                        orderStatus = this.language.order.order.state[2]
                    } else if(this.order.self_lifting == 1){
                        orderStatus = this.language.order.order.state[8]
                    } else {
                        orderStatus = '不满足的条件 status == 2'
                    }
                }
                //后台也不知道这个值干嘛用的 申说以前有用到，现在没用了，不知道是不是这样先留着
                else if(this.order.status == 3){
                    orderStatus = '不满足的条件 status == 3'
                } 
                //后台也不知道这个值干嘛用的 申说以前有用到，现在没用了，不知道是不是这样先留着
                else if(this.order.status == 4){
                    orderStatus = '不满足的条件 status == 4'
                } 
                //已完成
                else if(this.order.status == 5){
                    orderStatus = this.language.order.order.state[3]
                } 
                //后台也不知道这个值干嘛用的 申说以前有用到，现在没用了，不知道是不是这样先留着
                else if(this.order.status == 6){
                    orderStatus = '不满足的条件 status == 6'
                } 
                //订单关闭
                else if(this.order.status == 7){
                    orderStatus = this.language.order.order.state[4]
                } 
                //待核销
                else if(this.order.status == 8){
                    orderStatus = '待核销'
                }
                //后台也不知道这个值干嘛用的 申说以前有用到，现在没用了，不知道是不是这样先留着
                else if(this.order.status == 9){
                    //orderStatus = this.language.order.order.state[7]
                    orderStatus = '不满足的条件 status == 9'
                } 
                //后台也不知道这个值干嘛用的 申说以前有用到，现在没用了，不知道是不是这样先留着
                else if(this.order.status == 10){
                    //orderStatus = this.language.order.order.state[6]
                    orderStatus = '不满足的条件 status == 10'
                } 
                //后台也不知道这个值干嘛用的 申说以前有用到，现在没用了，不知道是不是这样先留着
                else if(this.order.status == 11){
                    //orderStatus = this.language.order.order.state[6]
                    orderStatus = '不满足的条件 status == 11'
                } 
                //后台也不知道这个值干嘛用的 申说以前有用到，现在没用了，不知道是不是这样先留着
                else if(this.order.status == 12){
                    //orderStatus = this.language.order.order.state[5]
                    orderStatus = '不满足的条件 status == 12'
                } 
                else {
                    orderStatus = '不满足的条件 status == ' + this.order.status
                }
                return  orderStatus
            },
            //订单状态 -下面的详情描述
            orderStatusDetails(){
                let orderStatusDetails = ''
                //待付款
                if(this.order.status == 0){
                    orderStatusDetails = this.order_zt
                } 
                //待发货
                else if(this.order.status == 1){
                    orderStatusDetails = this.language.order.order.ddfh
                } 
                //待收货
                else if(this.order.status == 2){
                    if(this.order.self_lifting == 0){
                        orderStatusDetails = this.language.order.order.logistics[3]
                    } else if(this.order.self_lifting == 1){
                        orderStatusDetails = this.language.order.order.logistics[4]
                    }  else if(this.order.self_lifting == 2){
                        orderStatusDetails = '商家配送中'
                    } 
                } 
                //后台也不知道这个值干嘛用的 申说以前有用到，现在没用了，不知道是不是这样先留着
                else if(this.order.status == 3){
                    orderStatusDetails = '不满足的条件 status == 3'
                } 
                //后台也不知道这个值干嘛用的 申说以前有用到，现在没用了，不知道是不是这样先留着
                else if(this.order.status == 4){
                    orderStatusDetails = language.order.order.Reasons + '：' + this.cancelGoodsReason
                } 
                //已完成
                else if(this.order.status == 5){
                    if(this.order.otype == 'VI'){
                        orderStatusDetails = '下次再来咯～'
                    } else {
                        orderStatusDetails = this.language.order.order.ndspy 
                    }
                } 
                //后台也不知道这个值干嘛用的 申说以前有用到，现在没用了，不知道是不是这样先留着
                else if(this.order.status == 6){
                    orderStatusDetails = '不满足的条件 status == 6'
                } 
                //订单关闭
                else if(this.order.status == 7){
                    if(this.order.cancel_method == 0){
                        orderStatusDetails = '店铺关闭订单'
                    } else if(this.order.cancel_method == 1){
                        orderStatusDetails = '取消关闭'
                    } else if(this.order.cancel_method == 2){
                        orderStatusDetails = '不要灰心，去看看其他宝贝啦~'
                    } else {
                        orderStatusDetails = '不满足的条件 status == 7'
                    }
                } 
                //待核销
                else if(this.order.status == 8){
                    orderStatusDetails = '商品待使用'
                } 
                //后台也不知道这个值干嘛用的 申说以前有用到，现在没用了，不知道是不是这样先留着
                else if(this.order.status == 9){
                    orderStatusDetails = '不满足的条件 status == 9'
                } 
                //后台也不知道这个值干嘛用的 申说以前有用到，现在没用了，不知道是不是这样先留着
                else if(this.order.status == 10){
                    orderStatusDetails = '不满足的条件 status == 10'
                } 
                //后台也不知道这个值干嘛用的 申说以前有用到，现在没用了，不知道是不是这样先留着
                else if(this.order.status == 11){
                    orderStatusDetails = '不满足的条件 status == 11'
                } 
                //后台也不知道这个值干嘛用的 申说以前有用到，现在没用了，不知道是不是这样先留着
                else if(this.order.status == 12){
                    orderStatusDetails = '不满足的条件 status == 12'
                } 
                else {
                    orderStatusDetails = '不满足的条件 status == ' + this.order.status
                }
                return  orderStatusDetails
            },
            //是否显示折扣
            isDiscount: function() {
                if (this.order.coupon_name) {
                    if (this.order.coupon_name == "(0折)") {
                        return 0;
                    } else {
                        return 1;
                    }
                }
            }, 
        },
        components: {
            btns,
            btnsList,
            tqm,
            showToast,
            payModel
        },
        onUnload(){
            if(uni.getStorageSync('skipType')){
                uni.removeStorageSync('skipType')
            }
        },
        onLoad(option) {

            this.order_type = option.order_type;
            this.orde_id = option.order_id;
            this.group = option.group;
            this.iszb = option.iszb || false;
            if (option.a_type == 1) {
                this.a_type = 1;
            }
            if (option.returnR != undefined) {
                this.returnR = option.returnR;
                this.types = option.types;
            }

            // #ifdef H5
            if (option.mweb == 1) {
                history.pushState(null, null, document.URL);
                window.addEventListener("popstate", function() {
                    history.pushState(null, null, document.URL);
                });
            }

            // #endif

            this.r_status_ = option.status;
            if (this.r_status_ != "") {
                this.has_status = true;
            }
        },
        onShow() {
            this.setIsMaskInvoice3(false)
            this.isMaskInvoice = false;
            this.isBlock = false;
            _axios(this);
        },
        beforeDestroy() {
            clearInterval(this.timer);
            clearInterval(this.setTime);
        },
        methods: {
            async goPay_display(e) {
                this.sNo = e.sNo
                this.order_id = e.id
                this.pay_way='useWallet'
                this.payment_money = 0
                this.order_pay_info = JSON.stringify({
                    order_id: e.id,
                    total: e.total,
                    sNo:e.sNo,
                    orderTime: e.add_time,
                    total_score:e.allow,
                })
                this._check_order_status()
            },
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
             * 极速退款 - 确认
             */
            confirm(){
                this.SET_JSTK('')
                //隐藏弹窗
                this.is_showToast = 0
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
                    explain
                }
                if(this.jstkItem.vipGoods){
                    data.oType = 'GM'
                }
                if (this.otype == 'IN' ||this.otype == 'ZB' ) {
                    data.oType = this.otype 
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
            /**
             * 极速退款 - 取消
             */
            richText(){
                //隐藏弹窗
                this.is_showToast = 0
                this.SET_JSTK('')
            },
            handleErrorImg(index) {
                setTimeout(() => {
                    this.order.list[index].imgurl =
                        "../../static/img/Default_picture.png";
                }, 0);
            },
            // 删除订单
            onScddClick() {
                if (!this.fastTap) return false;
                this.fastTap = false;
                uni.showModal({
                    title: this.language.order.myorder.prompt,
                    content: this.language.order.myorder.sure,
                    confirmText: this.language.order.myorder.confirm,
                    cancelText: this.language.order.myorder.cancel,
                    success: (res) => {
                        if (res.confirm) {
                            /*发送请求*/
                            let data = {
                                api: "app.order.del_order",
                                order_id: this.orde_id
                            };

                            this.$req.post({
                                data
                            }).then((res) => {
                                this.fastTap = true;
                                let {
                                    code,
                                    message
                                } = res;
                                if (code == 200) {
                                    uni.showToast({
                                        title: this.language.order.myorder
                                            .delete_success,
                                        duration: 1000,
                                        icon: "none",
                                    });

                                    uni.navigateBack();
                                } else {
                                    uni.showToast({
                                        title: message,
                                        duration: 1000,
                                        icon: "none",
                                    });
                                }
                            });
                        } else if (res.cancel) {
                            this.fastTap = true;
                        }
                    },
                });
            },
            expandMore() {
                const query = uni.createSelectorQuery();
                if (query.select(".order_color_b")) {
                    query
                        .select(".order_color_b")
                        .boundingClientRect((data) => {
                            if (data && data.height > 42) {
                                this.isMoreRemarks = true;
                            } else {
                                this.isMoreRemarks = false;
                            }
                        })
                        .exec();
                }
            },
            addRemarks() {
                this.isMaskRemarks = true;
            },
            addHeader() {
                uni.navigateTo({
                    url: "/pagesB/invoice/addInvoiceHeader",
                });
            },
            applyInvoice() {
                let data = {
                    api: "app.invoiceHeader.getDefault",
                };
                this.$req
                    .post({
                        data,
                    })
                    .then((res) => {
                        if (res.data && res.data.id) {
                            uni.navigateTo({
                                url: "/pagesB/invoice/openInvoice?sNo=" +
                                    this.order.sNo +
                                    "&price=" +
                                    this.order.invoicePrice,
                            });
                        } else {
                            this.isMaskInvoice = true;
                        }
                    });
            },
            _axios() {
                _axios(this);
            },
            _goStore(shop_id) {
                uni.navigateTo({
                    url: "/pagesB/store/store?shop_id=" + shop_id,
                });
            },
            _navigateTo(url) {
                uni.navigateTo({
                    url,
                });
            },
            ...mapMutations({
                cart_id: "SET_CART_ID",
                order_id: "SET_ORDER_ID",
                address_id: "SET_ADDRESS_ID",
                pro_id: "SET_PRO_ID",
                setIsMaskInvoice3:'SET_ISMASKINVOICE3',
                SET_JSTK:'SET_JSTK',
            }),
            onCopy: function() {
                var me = this;
                uni.setClipboardData({
                    data: this.message,
                    success: function(res) {
                        uni.showToast({
                            title: me.language.order.order.copy_success,
                            duration: 1500,
                            icon: "none",
                        });
                    },
                });
               
            },
            comment(order_details_id, comments_type) {
                if (comments_type == 1) {
                    uni.navigateTo({
                        url: "/pagesC/evaluate/evaluating?order_details_id=" +
                            order_details_id +
                            "&num=all",
                    });
                } else {
                    uni.navigateTo({
                        url: "/pagesC/evaluate/evaluating?order_details_id=" +
                            order_details_id +
                            "&add=true&num=all",
                    });
                }
            },
            _after(e, id, content, r_status, comments_type) {
                _after(e, id, content, r_status, this, comments_type);
            },
            // 限时折扣订单 售后
            _after1() {
                let order_details_id = []
                for (let i = 0; i < this.order.list.length; i++) {
                    let id = this.order.list[i].id;
                    order_details_id.push(id);
                }
              uni.navigateTo({
                  url: '/pagesA/returnGoods/returnGoods?order_details_id=' + order_details_id + '&order_anking=2&rType=true&isbatch=true' + '&r_status=' + this.order.list[0].r_status,
              });
            }, 
            _onafter() {
                this.display_t = false;
            },
            _goods(id, recycle, pluginId) {
                if (this.otype && (this.otype == "MS" || this.otype == "IN" || this.otype == "FS")) {
                    return;
                }
                if (recycle == 1) {
                    uni.showToast({
                        title: this.language.order.order.goodHasDel,
                        icon: "none",
                    });
                    return;
                }
                if (this.order.otype == "integral") {
                    uni.navigateTo({
                        url: "/pagesB/integral/integral_detail?pro_id=" + this.p_sNo,
                    });
                    return;
                }

                if (this.order.otype == "MS") {
                    uni.navigateTo({
                        url: "/pagesB/seckill/seckill_detail?pro_id=" +
                            this.order.list[0].p_id +
                            "&navType=" +
                            this.order.status +
                            "&id=" +
                            this.order.list[0].pluginId +
                            "&type=MS",
                    });
                    return;
                }

                if (this.order.otype == "FX") {
                    uni.navigateTo({
                        url: "/pagesC/goods/goodsDetailed?pro_id=" +
                            id +
                            "&isDistribution=true&toback=true" +
                            "&fx_id=" +
                            pluginId,
                    });
                    return;
                }

                uni.navigateTo({
                    url: "/pagesC/goods/goodsDetailed?pro_id=" + id,
                });
            },
        },
        watch: {
            time_c(newvalue, oldvalue) {
                if (newvalue == 0) {
                    _axios(me);
                }
            },
            refreshList(newvalue, oldvalue){
                this._axios()
            },
            jstkSwitch(newvalue){
                if(newvalue){
                    this._jstk(JSON.parse(newvalue))
                }else{
                    this.richText()
                }
            }
        },
    };
</script>

<style lang="less" scoped>
    @import url("@/laike.less");
    @import url("../../static/css/order/order.less");
    
    .XN-ysy{
        display: flex;
        align-items: center;
        justify-content: space-between;
        .xnStatus{
            color: #FA9D3B;
        }
    }
    .addp {
        border-bottom: none;
    }
    .hxTime{
        background-color: #fff;
        border-radius: 12rpx;
        padding: 32rpx;
        box-sizing: border-box;
        margin: 24rpx 0 16rpx 0;
        display: flex;
        flex-direction: column;
        >view:nth-child(1){
            font-weight: 500;
            font-size: 32rpx;
            color: #333333;
            margin-bottom: 24rpx;
        }
        >view:nth-child(2){
            display: flex;
            flex-direction: column;
            justify-content: center;
            >span:nth-child(1){
                font-weight: 500;
                font-size: 32rpx;
                color: #333333;
                margin-bottom: 8rpx;
            }
            >span:nth-child(2){
                font-size: 28rpx;
                color: #999999;
            }
        }
    }
    .hxKf{
        height: 56px;
        color: #333333;
        font-size: 16px;
        background-color: #fff;
        border-radius: 12px;
        padding: 0 15px;
        box-sizing: border-box;
        margin: 16rpx 0 94rpx 0;
        display: flex;
        align-items: center;
        justify-content: center;
        >image{
            width: 56rpx;
            height: 56rpx;
            margin-right: 12rpx;
        }
        >span{
            font-size: 32rpx;
            color: #333333;
        }
    }
    .hxShop{
        height: 56px;
        color: #333333;
        font-size: 16px;
        background-color: #fff;
        border-radius: 12px;
        padding: 0 15px;
        box-sizing: border-box;
        margin: 24rpx 0 16rpx 0;
        display: flex;
        align-items: center;
        justify-content: space-between;
        >p{
            font-size: 32rpx;
            color: #333333;
        }
        >view{
            display: flex;
            align-items: center;
            >span{
                font-size: 16px;
                color: #999999;
                margin-right: 6px;
            }
            >image{
                width: 32rpx;
                height: 44rpx;
            }
        }
    }
</style>
