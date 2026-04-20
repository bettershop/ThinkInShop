<template>
    <div class="container">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="title" :returnR="backUrl" ishead_w="3" :bgColor="bgColor" :titleColor="titleColor"></heads>
        <div class="load" v-if="load">
            <div>
                <img :src="loadImg" />
                <p>{{language.mystore_order.Loading}}</p>
            </div>
        </div>
        <div v-else class="relative">
            <div class="order_zt">
                <p class="order_zt_title" v-if="list.status == 0">{{language.storeMyOrder.obligation}}</p>
                <p class="order_zt_title" v-else-if="list.status == 1">{{language.storeMyOrder.waitSend}}</p>
                <p class="order_zt_title" v-else-if="list.status == 2">{{language.storeMyOrder.waitReceiving}}</p>
                <p class="order_zt_title" v-else-if="list.status == 5">{{language.storeMyOrder.done}}</p>
                <p class="order_zt_title" v-else-if="list.status == 7">
                    {{language.storeMyOrder.orderClosed}} 
                    <span style="margin-left: 16rpx;" v-if='!list.pay_time'>
                        ({{list.cancel_method==1?'已取消':'超时关闭'}})
                    </span>
                </p>
                <p class="order_zt_title" v-else-if="list.status == 8">待核销</p>
                <div class="order_wl">
                    <div class="orderTitle">
                        <div class='order-id'>{{language.mystore_order.order}}：{{ list.sNo }}</div>
                        <div class="copy_btn" @tap="onCopy()">{{language.mystore_order.copy}}</div>
                    </div>
                </div>
                <p class="order_p">{{language.mystore_order.order_time}}：{{ list.add_time }}</p>
            </div>
            <!-- 供应商订单查看详情 -->
            <template v-if='supplierInfo && supplierInfoType'>
                <!-- 买家 -->
                <ul  class="order_supplierInfo "> 
                    <li class="order_last">
                       <view class="order_color supplierinfo_item">
                           <p>{{language.supplierMod.mj}} </p> 
                           <p>{{list.user_name}}</p> 
                       </view> 
                    </li>
                </ul>
                <!-- 收货地址地址 -->
                <ul  class="order_supplierInfo ">
                    <li class="order_last">
                       <view class="  delivery_address_address">
                           <!-- 详细地址 -->
                           <view>{{list.address}}</view>
                           <!-- 省市区 -->
                           <view>{{list.sheng}}{{list.shi}}{{list.xian}}</view>   
                       </view> 
                       <view class="order_color supplierinfo_item userinof">
                           <view>{{list.name}} </view> 
                           <view> {{list.mobile}}</view>
                       </view> 
                    </li>
                </ul>
                <!-- 发货方消息 -->
                <ul  class="order_supplierInfo ">
                    <li class="order_last">
                       <view class="order_color supplierinfo_item">
                           <p>{{language.supplierMod.fhfxx}}</p> 
                       </view> 
                       <view class="order_color supplierinfo_item">
                           <p>{{language.supplierMod.lxr}}</p> 
                           <p>{{supplierInfo.supplierName}}</p>
                       </view> 
                       <view class="order_color supplierinfo_item">
                           <p>{{language.supplierMod.lxdh}}</p> 
                           <p>{{supplierInfo.supplierPhone}}</p>
                       </view> 
                       <view class="order_color supplierinfo_item">
                           <p>{{language.supplierMod.fhdz}}</p> 
                           <p>{{supplierInfo.supplierAddress}}</p>
                       </view> 
                    </li>
                </ul>
            </template>
            
            <!-- 虚拟商品 需预约核销门店信息 show_write_store:1需预约 -->
            <template v-if="list.otype == 'VI' && show_write_store == 1 && !supplierInfoType">
                <!-- 虚拟商品无预约 -->
                <view v-if="list.show_appointment != 1 && list.list.length == 1" class="hxShop" @tap="_navTo('/pagesB/store/storeHx?shop_id='+shop_id+'&pro_id='+list.p_id)">
                    <p>适用核销门店</p>
                    <view>
                        <span>{{write_store_num}}家</span>
                        <image :src="baga"></image>
                    </view>
                </view>
                <!-- 虚拟商品预约 预约详情 -->
                <view v-if="list.show_appointment == 1" class="hxTime">
                    <view>
                        预约时间：{{list.appointment.time}}
                    </view>
                    <view>
                        <span>预约门店：{{list.appointment.name}}</span>
                        <span>{{list.appointment.address}}</span>
                    </view>
                </view>
            </template>
            
            <!-- 直播的主播信息 -->
            <!-- <view class="order_live" v-if="order.otype==='ZB'">
                <view class="live_img">
                    <img :src="list.live_img" alt="">
                </view>
                <view class="live_tetx">来自"{{ order.anchor_name }}"直播间的推荐</view>
            </view> -->
            
            <!-- 实物商品才显示 -->
            <!-- 供应商端不显示 -->
            <template v-if="list.otype != 'VI' && !supplierInfoType">
                <template>
                    <!-- 查看 商家配送信息 -->
                    <ul class="order_xx" v-if="diplay">
                        <li class="border_bottom sjps-css" v-if="list.storeSelfInfo && list.storeSelfInfo.delivery_time">
                            <view>配送时间</view>
                            <view>{{list.storeSelfInfo.delivery_time}} {{list.storeSelfInfo.delivery_period == 1 ? '上午':'下午'}}</view>
                        </li>
                        <template v-if="list.storeSelfInfo && list.storeSelfInfo.courier_name">
                            <li class="border_bottom sjps-css" style="border-top: 2rpx solid rgba(0, 0, 0, .1);border-bottom: 2rpx solid rgba(0, 0, 0, .1);">
                                <view>配送员姓名</view>
                                <view>{{list.storeSelfInfo.courier_name}}</view>
                            </li>
                            <li class="border_bottom sjps-css">
                                <view>配送员电话</view>
                                <view>{{list.storeSelfInfo.phone}}</view>
                            </li>
                        </template>
                    </ul>
                </template>
                <template>
                    <!-- 查看 地址信息 -->
                    <ul class="order_xx" v-if="diplay && !supplierInfoType">
                        <li class="border_bottom">
                            <div class="orderInfo">
                                <div class="userAddressInfo">
                                    <p class="userAddress_xx">{{ list.address }}</p>
                                    <p class="userAddress">{{list.sheng}}{{list.shi}}{{list.xian}}</p>
                                </div>
                                <div class="userInfo">{{ list.name }} {{code2?`${code2} - `:''}}{{ list.mobile }}</div>
                            </div>
                        </li>
                    </ul>
                    <!-- 编辑 用户信息 -->
                    <ul class="order_xx" v-else>
                        <li class="border_top">
                            <p>{{userInfo}}</p>
                        </li>
                        <!-- 收件人 -->
                        <li class="border_bottom">
                            <div class="edit_add">
                                <span class="fl">{{language.mystore_order.username}}</span>
                            </div>
                            <div class="_input">
                                
                                <input type="text" name="name" v-model="list.name" maxlength="20" />
                            </div>
                        </li>
                        <!-- 联系电话 -->
                        <li class="border_bottom">
                            <div class="edit_add ">
                                <span class="fl">{{language.mystore_order.phone}}</span>
                            </div>
                            <div class="_input">
                                <div class="uni-input__area" @tap.shop="navTo('/pagesD/login/countryAndRegion')">
                                   {{ code2?'+' + code2 : '国家/地区' }}
                                   <image :src="down" mode="aspectFill"></image>
                                </div>
                                <input type="number" name="name" v-model="list.mobile" />
                            </div>
                        </li>
                        <!-- 所在地区 -->
                        <!-- 自提订单 编辑不显示 地区 -->
                        <li class="border_bottom" @click="showMulLinkageThreePicker()"  
                            v-if="list.self_lifting != 1 && (code2 == '86' || code2 == '852' || code2 == '853')">
                            <div class="edit_add  ">
                                <span class="fl">{{language.mystore_order.address}} </span>
                            </div>
                            <div class="_input">
                                <input type="text" disabled='true' :placeholder="language.mystore_order.address_placeholder"
                                    placeholder-style="color:#B8B8B8" @focus="hideKeyboard()" v-model="city_all" />
                                <img :src="jiantou" class="jiantou" />
                            </div>
                        </li>
                        <!-- 详细地址 -->
                        <li class="border_bottom" v-if="list.self_lifting != 1">
                            <div class="edit_add ">
                                <span class="fl">{{language.mystore_order.addressinfo}}</span>
                            </div>
                            <div class="_input">
                                <input type="text" name="name" v-model="list.address" />
                            </div>
                        </li>
                        <!-- 商家配送时间 -->
                        <li class="border_bottom" @tap="virtualTime = true" v-if="list.storeSelfInfo && list.storeSelfInfo.delivery_time">
                            <div class="edit_add  ">
                                <span class="fl">配送时间</span>
                            </div>
                            <div class="_input">
                                <input type="text" disabled='true' :placeholder="list.storeSelfInfo.delivery_time"
                                    placeholder-style="color:#B8B8B8" v-model="virtualTimeEnd" />
                                <img :src="jiantou" class="jiantou" />
                            </div>
                        </li>
                    </ul>
                </template>
            </template>
            
            <!-- 商品信息 -->
            <ul class="order_goods">
                <li class="order_goods_li" v-for="(item, index) in list.list" :key="index">
                    <div class="order_two">
                        <img :src="item.pic?item.pic:ErrorImg" @error="handleErrorImg(index)"/>
                        <div class="order_two_div1">
                            <p class="order_p_name">{{ item.p_name }}</p>

                            <div class="order_two_div2">
                                <!-- 价格 -->
                                <p class="flex_1">
                                    {{list.currency_symbol}}{{ LaiKeTuiCommon.getPriceWithExchangeRate(Number(item.p_price),list.exchange_rate) }}
                                    <span v-if="list.allow > 0">
                                        +
                                        <img class="integral-img" :src="integral_hei" />
                                        {{ list.allow }}
                                    </span>
                                </p>
                                <!-- 数量 -->
                                <p class="color_two">x{{ item.num }}</p>
                            </div>
                            <div class="order_tow_div3">
                                <p class="color_one">{{ item.size }}</p> 
                                <!-- 查看售后 -->
                                <p class="color_one proBtn" v-if=' item.showReturn && supplierInfoType' @tap="queryItem(item)"> 
                                    {{language.order.order.button[4]}} </p> 
                            </div>
                            <div class="btn">                    
                                <div class="order_goods_adiv">
                                    <div class="retreat sqtk" 
                                    v-if='item.refundShowBtn && supplierInfoType'
                                    @click.stop="
                                           navTo(
                                               '/pagesD/supplier/returnDetail?sNo=' +
                                                   list.sNo+
                                                   '&order_details_id=' +
                                                   (item.returndId || 0 )+ 
                                                   '&supplier=true'
                                           )
                                       ">
                                        {{
                                           language.order.order
                                               .button[4]
                                       }} 
                                    </div>
                                </div>
                            </div>
                        </div>

                    </div>
                    <!-- 虚拟商品 商品核销信息 -->
                    <!-- self_lifting:  0.配送 1.自提 2.商家自配 3.虚拟订单需要线下核销 4.虚拟订单无需线下核销 -->
                    <!-- 只有虚拟订单需要线下核销的视频 才会显示核销次数 -->
                    <template v-if="list.otype == 'VI'  && item.self_lifting == 3 ">
                        <view @tap="_navTo(item.is_write == 2?'/pagesB/erCode/hxJl?orderDetailId=' + item.id:'')" class="XN-hxnum">
                            <view>待核销次数</view>
                            <view>
                                <span>
                                    <template v-if="item.status == 1">
                                        {{item.write_off_num}}
                                    </template>
                                    <template v-else-if="item.status == 2">
                                        已核销完
                                    </template>
                                </span>
                                <image v-if="item.is_write == 2" :src="baga"></image>
                            </view>
                        </view>
                    </template>
                </li>
                <li class="order_last">
                    <div class="order_color">
                        <p>{{language.mystore_order.Total}}</p>
                        <p v-if="list.status == 0 && (list.otype == 'pt' || list.otype == 'PP')">
                            {{list.currency_symbol}}{{ LaiKeTuiCommon.getPriceWithExchangeRate(Number(list.spz_price),list.exchange_rate) }}
                            <span v-if="list.allow > 0">
                                +
                                <img class="integral-img" :src="integral_hui" />
                                {{ list.allow }}
                            </span>
                        </p>
                        <p v-else>
                            {{list.currency_symbol}}{{ LaiKeTuiCommon.getPriceWithExchangeRate(Number(list.spz_price),list.exchange_rate) }}
                            <span v-if="list.allow > 0">
                                +
                                <img class="integral-img" :src="integral_hui" />
                                {{ list.allow }}
                            </span>
                        </p>
                    </div>
                    <!-- 直播增加的数据 -->
                    <!-- <div class="order_color">
                        <p>店铺优惠</p>
                        <p>
                            {{list.currency_symbol}}{{ list.coupon_price }}
                        </p>
                    </div> -->
                    <template v-if="list.otype == 'GM' || list.otype == 'VI'">
                        <!-- 店铺优惠 条件 oupon_price>0 -->
                        <div class="order_color" v-if="list.coupon_price>0">
                            <p>{{language.mystore_order.amount[0]}}</p>
                            <p>
                                {{list.currency_symbol}}{{ LaiKeTuiCommon.getPriceWithExchangeRate(Number(list.coupon_price),list.exchange_rate) }}
                            </p>
                        </div>
                        <!-- 平台优惠 条件 discount_type  -->
                        <div class="order_color" v-if="list.discount_type">
                            <p>{{list.discount_type}}{{language.mystore_order.amount[1]}}</p>
                            <p>
                                {{list.currency_symbol}}{{ LaiKeTuiCommon.getPriceWithExchangeRate(Number(list.preferential_amount),list.exchange_rate) }}
                            </p>
                        </div>
                        <!-- 会员优惠 条件 grade_rate > 0 && grade_rate < 1 -->
                        <div class="order_color" v-if="list.grade_rate > 0 && list.grade_rate < 1">
                            <p>{{language.mystore_order.amount[2]}}</p>
                            <p>{{list.currency_symbol}}{{ LaiKeTuiCommon.getPriceWithExchangeRate(Number(list.grade_rate_amount),list.exchange_rate) }}</p>
                        </div>
                    </template>
                    <template v-else>
                        <div class="order_color" v-if="list.coupon_money != 0 && list.coupon_money != '0.00'">
                            <p>{{language.mystore_order.amount[3]}}</p>
                            <p>
                                {{list.currency_symbol}}{{ LaiKeTuiCommon.getPriceWithExchangeRate(Number(list.coupon_money),list.exchange_rate) }}
                                <span>{{ list.coupon_name }}</span>
                            </p>
                        </div>
                        <div class="order_color" v-if="list.comm_discount < 1 && list.comm_discount > 0">
                            <p>{{language.mystore_order.amount[4]}}</p>
                            <p>{{ list.comm_discount * 10 }}{{language.mystore_order.fracture}}</p>
                        </div>
                        <div class="order_color" v-if="list.grade_rate > 0 && list.grade_rate < 1">
                            <p>{{language.mystore_order.amount[5]}}</p>
                            <p>{{ list.grade_rate * 10 }}{{language.mystore_order.fracture}}</p>
                        </div>
                        <div class="order_color" v-if="list.reduce_money != 0 && list.reduce_money != '0.00'">
                            <p>{{language.mystore_order.amount[6]}}</p>
                            <p>{{list.currency_symbol}}{{ LaiKeTuiCommon.getPriceWithExchangeRate(Number(list.reduce_money),list.exchange_rate) }}</p>
                        </div>
                    </template>
                    <div class="order_color">
                        <p>{{language.mystore_order.freight}}</p>
                        <p>{{list.currency_symbol}}{{ LaiKeTuiCommon.getPriceWithExchangeRate(Number(list.z_freight),list.exchange_rate) }}</p>
                    </div>
                    <!-- 订单总价 -->
                    <div class="order_z_price order_color">
                        <p>{{language.mystore_order.Order_total}}</p>

                        
                        <p style="color:#FA5151">
                            {{list.currency_symbol}}{{ LaiKeTuiCommon.getPriceWithExchangeRate(Number(list.old_total),list.exchange_rate) }}
                            <span v-if="list.allow > 0">
                                +
                                <img class="integral-img" :src="integral_hei" />
                                {{ list.allow }}
                            </span>
                        </p>
                    </div>
                    <!-- 订单备注 -->
                    <div class="order_color" style="align-items: baseline;" :style="supplierInfoType && 'margin-bottom: 100rpx'">
                        <p>{{language.mystore_order.Order_notes}}</p>
                        <p class="order_remarks" style="
                        font-weight: 400;
                        color: #999999;
                        padding: 40rpx 0;
                        width: 500rpx;
                        word-wrap: break-word;
                        white-space: normal;
                        padding:32rpx 0;">{{ list.remarks }}</p>
                    </div>
                </li>
               
            </ul>
            <div v-if="!diplay" style="height: 98rpx;"></div>
        </div>
        <!-- 供应商重置按钮 -->
        <template v-if='supplierInfoType'>            
            <view class="order_foot">
                <!-- 供应商一键代发 -->
                <view  class="order_button" @tap="_showFhDiv1(list.sNo)" v-if="list.status == 1 && list.is_lssued !='1'"> 
                    {{language.supplierMod.yjdf}}
                </view> 
                <!-- 供应商 查看售后详情 -->
              <!--  <view  class="order_button" @tap="_changeDetail(list.sNo)" v-if="list.status == 1 && list.is_lssued !='1'">
                    {{language.supplierMod.yjdf}}
                </view> -->
            </view>
        </template>
        <div class="h_98" v-if="diplay && !supplierInfoType"> 
            <div class="order_foot" v-if="list.haveExpress">
                <div class="order_button" @tap="_seeWL(list.sNo)">{{language.mystore_order.View_Logistics}}</div>
            </div> 
            <div class="order_foot" v-else-if="list.status != 7&&((list.status == 0 && list.order_status) || list.status == 1 || list.status >= 2) ">
                <div v-if="list.status < 2 && list.sale_type != 2 && list.otype != 'VI'" class="order_button mr_20" @tap="_edit()">
                    <!-- 编辑 -->
                    {{language.mystore_order.edit}}
                </div>
                <div v-if="list.self_lifting!=2 && ((list.status >= 2&&list.isLogistics)||list.haveExpress)" class="order_button" @tap="_seeWL(list.sNo)">
                    <!-- 查看物流 -->
                    {{language.mystore_order.View_Logistics}}
                </div>
                <div v-if="list.status == 1 && list.sale_type != 2" class="order_button" @tap="_showFhDiv(list)">
                    <!-- 发货 -->
                    {{language.mystore_order.deliver}}
                </div>
            </div>
            <div class="order_foot" v-else-if="list.self_lifting == '1' && list.status == 2">
                <!-- #ifdef H5 -->
                <div class="order_button" @tap="QRx">{{language.mystore_order.QR}}</div>
                <!-- #endif -->
                <!-- #ifndef H5 -->
                <div class="order_button mr_10" @tap="QRx">{{language.mystore_order.QR}}</div>
                <div class="order_button" @tap="QRs">{{language.mystore_order.QRs}}</div>
                <!-- #endif -->
            </div>
        </div>
        <view class="edit_btn" v-if="!diplay && okBtn" @tap="_ok()">
            <div class='edit_bottomBtn'>{{language.mystore_order.complete}}</div>
        </view>
        <div class="mask" v-if="fhDiv">
            <div class="fhDiv">
                <div class="fhDivTitle">
                    <span class="span">{{language.mystore_order.Tips[0]}}</span>
                    <img class="img" @tap="_closeFhDiv()" :src="guanbi" />
                </div>
                <div class="formList">
                    <div class="leftText1"><span>{{language.mystore_order.express[0]}}</span></div>
                    <div class="rightInput1" @tap="showSinglePicker()">
                        <input type="text" disabled="true" :placeholder-style="placeStyle" v-model="proClass"
                            :placeholder="language.mystore_order.Tips[1]" />
                        <div class="jiantouDiv"><img :src="jiantou" alt="" /></div>
                    </div>
                </div>
                <div class="formList">
                    <div class="leftText1"><span>{{language.mystore_order.express[1]}}</span></div>
                    <div class="rightInput1"><input type="number" :placeholder-style="placeStyle" v-model="courier_num"
                            :placeholder="language.mystore_order.Tips[2]" /></div>
                </div>
                <div class="fhSubmit" @tap="_send()">{{language.mystore_order.Submit}}</div>
            </div>
        </div>
        <view class="xieyi mask" style="background-color: initial;" v-if="is_sus">
            <view style="width: 272rpx;height: 272rpx;background-color: rgba(51, 51, 51, .9);">
                <view style="margin: 32rpx 0;text-align: center;margin-top: 64rpx;">
                    <image style="width: 68rpx;height: 68rpx;" :src="sus"></image>
                </view>
                <view class="xieyi_title"
                    style="margin-bottom: 0;margin-top: 0;color: #fff;font-weight: 500;font-size: 32rpx;">
                    编辑成功
                </view>
            </view>
        </view>
        <mpvue-city-picker :themeColor="themeColor" ref="mpvueCityPicker" :pickerValueDefault="cityPickerValueDefault"
            @onConfirm="onConfirm"></mpvue-city-picker>
        <mpvue-picker v-if="show" :themeColor="themeColor" ref="mpvuePicker" :mode="mode" :deepLength="deepLength"
            :pickerValueDefault="pickerValueDefault" @onConfirm="onConfirm2"
            :pickerValueArray="pickerValueArray"></mpvue-picker>
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
    </div>
</template>

<script>
    import mpvuePicker from '../../components/mpvuePicker.vue';
import {img} from "@/static/js/login/imgList.js";
    import {
        LaiKeTui_axios,
        LaiKeTui_showFhDiv,
        LaiKeTui_ok,
        LaiKeTui_send,
    } from '@/pagesA/myStore/myStore/order.js';

    import {
        copyText
    } from '@/common/util.js';
   import mpvueCityPicker from '@my-miniprogram/src/components/mpvue-citypicker/mpvueCityPicker';
    

    export default {
        data() {
            return {
                orderList_id: '',
                express_id: '',
                imgurl: '',
                 down: img(this).down,
                closeImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/guanbi2x.png',
                mask_display: false,
                proClass: '',
                is_sus: false,
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
                shop_id: '',
                p_id: '',
                code2: '86',
                baga: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/baga.png',
                ErrorImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/Default_picture.png',
                integral_hui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/integral_hui.png', 
                integral_hei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/integral_hei.png', 
                guanbi: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/guanbi2x.png', 
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jiantou2x.png', 
                loadImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/5-121204193R7.gif', 
                title: '订单详情',
                order_id: '',
                load: true,
                diplay: true,
                fhDiv: false,
                supplierInfoType:false,
                list: '',
                attrList: '',
                skuBeanList: '',
                haveSkuBean: '',
                okBtn: true,
                sNo: '',
                courier_num: '',
                kuaidiList: '',
                themeColor: '#D73B48',
                mode: '',
                deepLength: 1,
                pickerValueDefault: [0],
                pickerValueArray: [],
                show: false,
                backUrl: 'myOrder',
                finishBlur: false, //是否完成bulr计算
                fastTap: true,
                placeStyle: 'color:#b8b8b8;font-size:28upx',
                show_write_store: '',//
                write_store_num: '',//
                price: '',
                num: '',
                highKeys: {},
                skuName: 'SkuID',
                skuName1: 'Price',
                skuName2: 'Pic',
                skuName3: 'Stock',
                spliter: ',',
                sku_list: {},
                result: {},
                cityPickerValueDefault: [0, 0, 0],
                city_all: '',
                editor: '',
                supplierInfo:{},
                bgColor: [{
                        item: '#FA5151 '
                    },
                    {
                        item: '#D73B48'
                    }
                ],
                titleColor: '#ffffff',
                virtualTime: false,
                storeHxTime:[],
                storeHxTimeIndex: 0,
                virtualTimeEnd: '',//最终显示 商家配送时间
                deliveryTime: '',//商家配送日期 传给后台参数
                deliveryPeriod: '',//商家配送时间 传给后台参数
            };
        },
        watch:{
          'is_sus'(newValue,oldValue){
              if(!newValue){
                  this.navBack()
              }
          }
        },
        computed: {
            userInfo(){
                let name = ''
                if(this.list.storeSelfInfo && this.list.storeSelfInfo.delivery_time){
                    name = '配送信息'
                } else {
                    name = this.language.mystore_order.shrxx
                }
                return name
            }
        },
        onLoad(option) {
            if (option.editor == 1) {
                this.editor = option.editor;
            }
            if(option.type == 'supplier'){
                this.supplierInfoType = true
            }
            this.order_id = option.order_id;
            
            this.isLogin(() => {
                this.shop_id = uni.getStorageSync('shop_id') ? uni.getStorageSync('shop_id') : this.$store.state
                    .shop_id;
                this._axios();
            })
        },
        onShow() { 
            if(uni.getStorageSync('diqu')){
              let diqu = JSON.parse(uni.getStorageSync('diqu'))
              this.code2 = diqu?.code2 
            } 
            // 标题统一都是订单详情了，没有编辑订单标题
            this.title = this.language.mystore_order.title 
        },
        methods: {
            // 供应商订单 查看售后详情
            queryItem(item){
                  this.navTo('/pagesD/supplier/returnDetail?sNo='+this.list.sNo+'&order_details_id='+item.returnId+'&supplier=true')
            },
            // 供应商 售后订单查看详情
            _changeDetail(orderid,id) { 
                this.navTo('./returnDetail?sNo='+orderid+'&order_details_id='+id+'&supplier=true')
            },
            // 供应商 一键代发接口
            _showFhDiv1(orders) {
                uni.showLoading({
                    title:"请稍后..."
                })
                this.$req.post({
                    data:{
                        api: 'supplier.AppMch.Orders.oneClickDistribution',
                        orders
                    }
                }).then(res=>{
                    uni.hideLoading()
                    if(res.code == 200){
                       uni.showToast({
                           title: '操作成功',
                           duration: 1500,
                           icon: 'none',
                           success:()=>{
                               this._axios()
                           }
                       });
                    }
                })
            }, 
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
                   // if(this.list.storeSelfInfo.delivery_period == 1){
                   //     obj.value.top = true
                   // } else {
                   //     obj.value.bottom = true
                   // }
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
                    this.virtualTimeEnd = this.list.storeSelfInfo?.delivery_time + ' ' + (this.list.storeSelfInfo?.delivery_period == 1 ? '上午':'下午')
                    this.deliveryTime = this.list.storeSelfInfo?.delivery_time
                    this.deliveryPeriod = this.list.storeSelfInfo?.delivery_period
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
                    this.deliveryTime = deliveryTime
                    this.deliveryPeriod = deliveryPeriod
                    if(deliveryTime && deliveryPeriod){
                        this.virtualTimeEnd = deliveryTime + '  ' + (deliveryPeriod == 1 ? '上午' : '下午')
                    }
                }
            },
            //路由跳转
            _navTo(url){
                uni.navigateTo({
                    url: url
                });
            },
            handleErrorImg(index){
                this.list.list[index].pic = this.ErrorImg
            },
            changeLoginStatus() {
                this._axios();
            },
            showMulLinkageThreePicker() {
                this.$refs.mpvueCityPicker.show()
            },
            hideKeyboard() {
                uni.hideKeyboard()
            },

            /*
                 ——————sku核心算法 开始——————
             */
            powerset(arr) {
                let ps = [
                    []
                ];
                for (let i = 0; i < arr.length; i++) {
                    for (let j = 0, len = ps.length; j < len; j++) {
                        ps.push(ps[j].concat(arr[i]));
                    }
                }

                return ps;
            },

            /**
             * 初始化数据
             * @return
             */
            initData() {
                this.result = {};
                this.keys = this.getAllKeys(); //arrKeys["颜色", "尺码", "型号"]

                for (let i = 0; i < this.keys.length; i++) {
                    this.highKeys[this.keys[i]] = false; //所有的都为false
                }

                this.sku_list = this.combineAttr(this.skuBeanList, this.keys);

                this.buildResult(this.sku_list.items);
                this.updateStatus(this.getSelectedItem(), true);


                // 初始筛选出库存为0的属性
                let filterObj = {}
                for (let i in this.result) {
                    if ((!i.includes(',')) && this.result[i].skus.Stock == 0) {
                        filterObj[i] = this.result[i]
                    }
                }

                for (let i in this.sku_list.result) {

                    for (let k in this.sku_list.result[i]) {

                        for (let j in filterObj) {
                            if (k == j) {
                                this.sku_list.result[i][k].disabled = true
                            }
                        }

                    }

                }
                // 筛选结束

                this.showResult();
            },

            /**
             * 正常属性点击
             */
            handleNormalClick(key, value) {
                this.finishBlur = true

                let list = JSON.parse(JSON.stringify(this.sku_list));

                for (let i in list.result[key]) {
                    if (i != value.name) {
                        list.result[key][i].active = false;
                    } else {
                        list.result[key][i].active = true;
                    }
                }

                this.sku_list = list;
            },

            /**
             * 无效属性点击
             */
            handleDisableClick(key, value) {
                this.sku_list.result[key][value.name]['disabled'] = false;
                // 清空高亮行的已选属性状态（因为更新的时候默认会跳过已选状态）
                for (let i in this.sku_list.result) {
                    if (i != key) {
                        for (let x in this.sku_list.result[i]) {
                            this.sku_list.result[i][x].active = false;
                        }
                    }
                }

                this.updateStatus(this.getSelectedItem());
            },

            /**
             * 高亮行
             */
            highAttributes: function() {
                for (let key in this.sku_list.result) {
                    this.highKeys[key] = true;
                    for (let attr in this.sku_list.result[key]) {
                        if (this.sku_list.result[key][attr].active === true) {
                            this.highKeys[key] = false;
                            break;
                        }
                    }
                }
            },

            /**
             * 点击事件处理
             * @param  key   点击的行
             * @param  value 点击的按钮的数据
             */
            handleActive: function(key, value) {
                if (value.disabled === true) {
                    uni.showToast({
                        title: this.language.mystore_order.Tips[3],
                        icon: 'none'
                    })
                    return
                }

                if (value.active == true) {
                    return false;
                }

                this.handleNormalClick(key, value);
                if (value.disabled === true) {
                    this.handleDisableClick(key, value);
                }

                this.updateStatus(this.getSelectedItem());
                this.highAttributes();
                this.showResult();
            },

            /**
             * 计算属性
             * @param  {[type]} data [description]
             * @param  {[type]} keys [description]
             * @return {[type]}      [description]
             */
            combineAttr(data, keys) {
                let allKeys = [];
                let result = {};

                for (let i = 0; i < data.length; i++) {
                    let item = data[i];
                    let values = [];

                    for (let j = 0; j < keys.length; j++) {
                        let key = keys[j];
                        if (!result[key]) {
                            result[key] = {};
                        }

                        if (!result[key][item[key]]) {
                            result[key][item[key]] = {
                                name: item[key],
                                active: false,
                                disabled: item['Stock'] > 0 ? false : true
                            };
                        }

                        values.push(item[key]);
                    }

                    allKeys.push({
                        path: values.join(this.spliter),
                        sku: item['SkuID'],
                        price: item['Price'],
                        Pic: item['Pic'],
                        Stock: item['Stock']
                    });
                }

                return {
                    result: result,
                    items: allKeys
                };
            },

            isJSON(str) {
                if (typeof str == 'string') {
                    try {
                        var obj = JSON.parse(str);
                        return true;
                    } catch (e) {
                        return false;
                    }
                }
            },

            /**
             * 获取所有属性
             * @return {[type]} [description]
             */
            getAllKeys() {
                let arrKeys = [];
                for (let attribute in this.skuBeanList[0]) {
                    if (!this.skuBeanList[0].hasOwnProperty(attribute)) {
                        continue;
                    }

                    if (attribute !== this.skuName && attribute !== this.skuName1 && attribute !== this.skuName2 &&
                        attribute !== this.skuName3) {
                        arrKeys.push(attribute);
                    }
                }
                return arrKeys;
            },

            getAttruites(arr) {
                let result = [];
                for (let i = 0; i < arr.length; i++) {
                    result.push(arr[i].path);
                }

                return result;
            },

            /**
             * 生成所有子集是否可选、库存状态 map
             */
            buildResult(items) {
                let allKeys = this.getAttruites(items);

                let attr = {};
                //价格 , 库存, 图片 赋值
                for (let i = 0; i < allKeys.length; i++) {
                    let curr = allKeys[i];
                    let sku = items[i].sku;
                    let Pic = items[i].Pic;
                    let price = items[i].price;
                    let Stock = items[i].Stock;
                    let values = curr.split(this.spliter);
                    let allSets = this.powerset(values);

                    // 每个组合的子集
                    for (let j = 0; j < allSets.length; j++) {
                        let set = allSets[j];
                        let key = set.join(this.spliter);
                        if (key && !this.result[key]) {
                            this.result[key] = {
                                skus: {
                                    sku,
                                    Pic,
                                    price,
                                    Stock
                                }
                            };

                            if ((!key.includes(',') && !attr[key]) || (key.includes(',') && key.split(',').length <
                                    allKeys[i].split(',').length)) {
                                attr[key] = {
                                    skus: {
                                        sku,
                                        Pic,
                                        price,
                                        Stock
                                    }
                                };
                            }
                        }
                    }
                }

                for (let i in attr) {
                    attr[i].skus.Stock = 0;
                    for (let k in this.result) {
                        if (i != k && k.split(',').length == allKeys[0].split(',').length && k.includes(i)) {
                            attr[i].skus.Stock += Number(this.result[k].skus.Stock);
                        } else if (k.split(',').length == allKeys[0].split(',').length) {
                            let flag = [];

                            k.split(',').filter(item => {
                                i.split(',').filter(it => {
                                    if (item == it) {
                                        flag.push(true);
                                    }
                                });
                            });

                            if (flag.length == i.split(',').length) {
                                attr[i].skus.Stock += Number(this.result[k].skus.Stock);
                            }
                        }
                    }
                }

                Object.assign(this.result, attr);
            },

            /**
             * 获取选中的信息
             * @return Array
             */
            getSelectedItem() {
                let result = [];
                for (let attr in this.sku_list.result) {
                    let attributeName = '';
                    for (let attribute in this.sku_list.result[attr]) {
                        if (this.sku_list.result[attr][attribute].active === true) {
                            attributeName = attribute;
                        }
                    }

                    result.push(attributeName);
                }

                return result;
            },

            /**
             * 更新所有属性状态
             */
            updateStatus(selected, type) {
                for (let i = 0; i < this.keys.length; i++) {
                    let key = this.keys[i],
                        data = this.sku_list.result[key],
                        hasActive = !!selected[i],
                        copy = selected.slice();

                    for (let j in data) {
                        let item = data[j]['name'];
                        if (selected[i] == item) {
                            continue;
                        }

                        copy[i] = item;
                        let curr = this.trimSpliter(copy.join(this.spliter), this.spliter);

                        if (type) {
                            this.sku_list.result[key][j]['disabled'] = this.result[curr] ? false : true;
                        } else {
                            this.sku_list.result[key][j]['disabled'] = this.result[curr].skus.Stock > 0 ? false : true;
                        }
                    }
                }
            },

            trimSpliter(str, spliter) {
                let reLeft = new RegExp('^' + spliter + '+', 'g');
                let reRight = new RegExp(spliter + '+$', 'g');
                let reSpliter = new RegExp(spliter + '+', 'g');
                return str
                    .replace(reLeft, '')
                    .replace(reRight, '')
                    .replace(reSpliter, spliter);
            },

            /**
             * 初始化选中
             * @param  mixed|Int|String SkuID 需要选中的SkuID
             * @return {[type]}       [description]
             */
            initSeleted(a) {
                for (let i in this.skuBeanList) {
                    if (this.skuBeanList[i][this.skuName] == a) {
                        for (let x in this.skuBeanList[i]) {
                            if (x !== this.skuName && x !== this.skuName1 && x !== this.skuName2 && x !== this
                                .skuName3) {
                                this.sku_list.result[x][this.skuBeanList[i][x]].active = true;
                            }
                        }
                        break;
                    }
                }
            },

            /**
             * 显示选中的信息
             * @return
             */
            showResult() {
                let result = this.getSelectedItem();
                let s = [];
                for (let i = 0; i < result.length; i++) {
                    let item = result[i];
                    if (!!item) {
                        s.push(item);
                    }
                }

                if (s.length > 0) {
                    this.num = this.result[s.join(this.spliter)].skus.Stock;
                    this.imgurl = this.result[s.join(this.spliter)].skus.Pic
                }

                if (s.length == this.keys.length) {
                    let curr = this.result[s.join(this.spliter)];
                    if (curr) {
                        this.SkuID = curr.skus.sku;
                        this.Pic = curr.skus.Pic;
                        this.price = curr.skus.price;
                        this.Stock = curr.skus.Stock;
                    }

                    this.haveSkuBean = {
                        name: s.join(this.spliter),
                        cid: curr.skus.sku,
                        skus: curr.skus
                    };
                } else {
                    this.haveSkuBean = '';
                }
            },

            /* 
             ——————sku核心算法 结束——————
        */

            _axios() {
                LaiKeTui_axios(this);
                if (this.editor == 1&&!this.list.haveExpress) {
                    this.diplay = false;
                } else {
                    this.diplay = true;
                }
            },
            // 复制
            onCopy: function() {

                var me = this;
                uni.setClipboardData({
                    data: me.list.sNo,
                    success: function(res) {
                        uni.showToast({
                            title: me.language.order.order.copy_success,
                            duration: 1500,
                            icon: "none",
                        });
                    },
                });
            },
            // 改变完成
            changeFinish(type) {
                if ('f' == type) {
                    this.finishBlur = true;
                    return;
                }
                this.finishBlur = false;
            },
            // 运费改变
            changeFreight(e) {
                this.list.z_freight = e.detail.value;
                this.finishBlur = true;
            },
            // 编辑订单
            _edit() {
                this.title = this.language.mystore_order.Tips[5]
                this.diplay = false;
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

                this.$req.post({
                    data
                }).then(res => {
                    let {
                        code,
                        message,
                        data,
                        isPackage
                    } = res;
                    uni.hideLoading();
                    if (code == 200) {
                        if (data.length > 1||!isPackage) {
                            uni.navigateTo({
                                url: '/pagesB/expressage/expressage?sNo=' + sNo
                            });
                        } else {
                            uni.navigateTo({
                                url: '/pagesC/expressage/expressage?list=' + JSON.stringify(data[0]) +
                                    '&sNo=' + sNo
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
            // 发货
            _showFhDiv(items) {
                //如果是商家配送-跳转填写配送员信息页面
                if(this.list.self_lifting == 2){
                    uni.navigateTo({
                        url: '/pagesB/myStore/shipments?id=' + items.sNo
                    });
                } else {
                    //选择商品 去发货
                    uni.navigateTo({
                        url: '/pagesA/myStore/shipments?id=' + items.sNo
                    });
                }
            },
            // 完成
            _ok() {
                LaiKeTui_ok(this);
            },
            // 关闭物流信息
            _closeFhDiv() {
                this.fhDiv = false;
            },
            // 显示物流公司
            showSinglePicker() {
                this.show = true;
                this.mode = 'selector';
                this.deepLength = 1;
                this.pickerValueDefault = [0];
                this.$refs.mpvuePicker.show();
            },
            // 发货
            _send() {
                LaiKeTui_send(this);
            },

            onConfirm(e) {
                this.city_all = e.label
                let arr = e.label.split('-')
                
            },
            onConfirm2(e) {
                this.proClass = e.label;
                this.show = false;
                this.express_id = this.kuaidiList[e.index[0]].id;
            },

            _mask_false() {
                this.mask_display = false;
                this.okBtn = true;
            },
            // 跳转至手动输入验证码页面
            QRx() {
                uni.navigateTo({
                    url: '/pagesA/myStore/QRcode'
                });
            },
            // 自提扫码
            QRs() {
                // #ifndef H5
                uni.scanCode({
                    success: rew => {
                        uni.showLoading({
                            title: this.language.mystore_order.Tips[6],
                        });
                        QRs(this, rew);
                    }
                });
                // #endif
                // #ifdef H5
                uni.showToast({
                    icon: 'none',
                    title: this.language.mystore_order.Tips[7],
                });
                // #endif
            }
        },
        components: {
            mpvuePicker,
            mpvueCityPicker,
        }
    };
</script>
<style>
    page {
        background: #F4F5F6
    }
</style>
<style lang="less" scoped>
    @import url("@/laike.less");
    @import url('../../static/css/myStore/order.less');
    .uni-input__area {
        display: flex;
        align-items: center;
        font-size: 32rpx;
        color: #333333;
        image {
          width: 10rpx;
          height: 10rpx;
          margin-left: 8rpx;
        }
    }
    .sjps-css{
        margin-bottom: 0;
        padding: 32rpx 0;
        box-sizing: border-box;
        font-size: 32rpx;
        color: #333333;
    }
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
        >view{
            >view:nth-child(1){
                padding: 40rpx;
                box-sizing: border-box;
                background-color: #F4F5F6;
                border-radius: 24rpx 24rpx 0 0;
                text-align: center;
                font-size: 40rpx;
                color: #333333;
                position: relative;
                >image{
                    width: 28rpx;
                    height: 28rpx;
                    position: absolute;
                    top: 40rpx;
                    right: 40rpx;
                }
            }
            >view:nth-child(2){
                height: 600rpx;
                background-color: #ffffff;
                display: flex;
                align-items: center;
                justify-content: center;
                >view:first-child{
                    width: 224rpx;
                    height: 600rpx;
                    overflow-y: auto;
                    background-color: #F4F5F6;
                    >view{
                        padding: 32rpx;
                        box-sizing: border-box;
                        font-size: 28rpx;
                        color: #666666;
                        text-align: center;
                    }
                    .virtual-first-active{
                        background: #FFFFFF;
                        font-weight: 500;
                        font-size: 28rpx;
                        color: #FA5151;
                    }
                }
                >view:last-child{
                    display: flex;
                    flex-direction: column;
                    flex: 1;
                    height: 600rpx;
                    overflow-y: auto;
                    padding: 32rpx;
                    box-sizing: border-box;
                    .chooseTime{
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
                        >span:first-child{
                            width: 100%;
                            font-weight: 500;
                            font-size: 32rpx;
                            color: #666666;
                        }
                        >span:last-child{
                            width: 100%;
                            font-size: 28rpx;
                            color: #666666;
                            margin-top: 8rpx;
                        }
                    }
                    .noTime{
                        flex: 1;
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                        justify-content: center;
                        >image{
                            width: 100%;
                        }
                        >span{
                            font-size: 28rpx;
                            color: #333333;
                        }
                    }
                    .virtualLastActive{
                        border: 2rpx solid #FA5151;
                        background-color: rgba(250,81,81,0.1);
                        >span:first-child{
                            color: #FA5151;
                        }
                        >span:last-child{
                            color: #FA5151;
                        }
                    }
                    .virtualLastOver{
                        background-color: #F4F5F6;
                        >span:first-child{
                            color: #999999;
                        }
                        >span:last-child{
                            color: #999999;
                        }
                    }
                }
            }
            >view:nth-child(3){
                min-height: 20rpx;
                background-color: #ffffff;
                box-sizing: border-box;
            }
        }
    }
    .delivery_address_address{
        margin-top: 40rpx;
        view:nth-child(1){
            color: #8a8e91;
        }
    }
    .userinof{
        view:nth-child(2){
            margin-left: 40rpx;
        }
    }
    .XN-hxnum{
        margin: 0 32rpx 32rpx 32rpx;
        background: #F4F5F6;
        border-radius: 16rpx;
        padding: 24rpx;
        box-sizing: border-box;
        display: flex;
        align-items: center;
        justify-content: space-between;
        >view:first-child{
            font-size: 32rpx;
            color: #333333;
        }
        >view:last-child{
            display: flex;
            align-items: center;
            >span{
                font-size: 32rpx;
                color: #FA5151;
                margin-right: 12rpx;
            }
            >image{
                width: 32rpx;
                height: 44rpx;
            }
        }
    }
</style>
