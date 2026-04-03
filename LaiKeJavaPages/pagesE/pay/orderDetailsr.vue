<template>
    <div class="cart_f pages-pay-orderDetailsr" ref='cart'>

        <heads :title='language.pay.orderDetailsr.title' :returnR='returnR' :bgColor="bgColor" :titleColor="'#333333'"
            :ishead_w="2"></heads>
        <view v-if="commodity_type == 0" style="height: 12rpx;width:750rpx;position: relative;background-color: #fff;" class="nav_line">
            <image :src="orderDetailsr_fgx"  
                style="width: 750rpx;height: 12rpx;background-color: #fff;position: absolute;top: 0;" class="nav_line">
            </image>
        </view>
        <paymodel @cancel="wallet_pay_cancel" @success="check_pay_password" v-model="pay_display" />
        <mock :load="load"></mock>

        <div class="order_detail" v-if="load" style="background-color: #F4F5F6;">
            <!--商品列表-->
            <template>
                <view style="height: 2rpx;background-color: #fff;"></view>
                <template>
                    <!-- 虚拟商品 -->
                    <view v-if="commodity_type == 1 && address_status == 2" class="virtual">
                        <view @tap="_navTo('/pagesB/store/storeHxList?shop_id='+shop_id+'&pro_id='+allPro[0]+'&shop_address_id='+shop_address_id)">
                            <view>
                                <view>{{storeHx[0] && storeHx[0].name || '请选择核销门店'}}</view>
                                <view>
                                    <image :src="jiantou"></image>
                                </view>
                            </view>
                            <view v-if="storeHx.length">{{storeHx[0].sheng}}{{storeHx[0].shi}}{{storeHx[0].xian}}{{storeHx[0].address}}</view>
                        </view>
                        <view>
                            <view>预约时间</view>
                            <view v-if="!storeHx" class="virtualTisi" @tap="virtualTime = true">需选择核销门店</view>
                            <view v-else-if="storeHxTimeKeyID==''" class="virtualTisi" @tap="_virtualShow">需选择预约时间</view>
                            <view v-else class="virtualTime" @tap="virtualTime = true">
                                <span v-if="storeHxTimeKeyID">{{storeHxTimeKeyTime}} {{storeHxTimeKeyData}}</span>
                                <image :src="jiantou"></image>
                            </view>
                        </view>
                    </view>
                    <!-- 实物商品 -->
                    <choose-address
                        ref='chooseAddress'
                        v-if='commodity_type == 0 || commodity_type == 2'
                        v-model="address"
                        :all_pro="allPro" 
                        :shop_address="shop_list" 
                        :shop_id="shop_id"
                        :shop_status="shop_status == 1"
                        :address_status="adds_f" 
                        :is_express="is_express"
                        :is_supplier_pro="is_supplier_pro" 
                        :zpStatus="zpStatus"
                        @show="show" 
                        @change="sChange" 
                        @sjps="_sjps"
                        @input="usermod"/>
                    <!--灰色间隔-->
                    <div v-if="commodity_type == 0 || commodity_type == 2" class="yh-line"></div>
                </template>
            </template>
            <div>
                <ul :key='indexs' v-for='(items,indexs) in pro' style="margin-bottom: 16rpx;" :style="{borderRadius:commodity_type == 1 && address_status == 1?'0 0 24rpx 24rpx':'24rpx'}">
                    <li class='storeLi' v-if='items.shop_id' @tap="toUrl('/pagesB/store/store?shop_id='+items.shop_id)">
                        <img :src="items.head_img" class="yh-storeLi-imga" @error="handleErrorImgLogo(indexs)">
                        {{items.shop_name}}
                    </li>
                    <li :key='index' class='goods' v-for="(item,index) in items.list"
                        @tap="toUrl('/pagesC/goods/goodsDetailed?toback=true&pro_id='+item.pid+'&mch_id='+items.shop_id)">
                        <img :src="item.img"  @error="handleErrorImg(indexs, index)"/>
                        <div class='goods_right'>
                            <p style="height: 88rpx;">{{item.product_title}}</p>
                            <p class="size">
                                {{item.color?item.color:''}}{{item.name?item.name:''}}{{item.size?item.size:''}}</p>
                            <div class='goods_price'>
                                <p>
                                    <span class="yh-goods_bottom-span"></span>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(item.price)}}
                                </p>
                                <div class='format'>
                                    <p class="yh-format-pa" style="margin-right: 0;color: #020202;">x{{item.num}}</p>
                                </div>
                            </div>
                        </div>
                    </li>

                    <li v-if="gift_list&&gift_list.id&&items.shop_id&&gift_list.admin_mch_id == items.shop_id"
                        class='goods'
                        @tap="toUrl('/pagesC/goods/goodsDetailed?toback=true&pro_id='+gift_list.pid+'&mch_id='+gift_list.mch_id)">
                        <img :src="gift_list.img" />
                        <div class='goods_right'>
                            <p><span class="zpIcon">{{language.order.myorder.zp}}</span>{{gift_list.product_title}}</p>
                            <p class="size">{{gift_list.size?gift_list.size:''}}</p>
                            <div class='goods_price'>
                                <p>
                                    <span class="yh-goods_bottom-span">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</span>{{LaiKeTuiCommon.formatPrice(gift_list.price)}}
                                </p>
                                <div class='format'>
                                    <p class="yh-format-pa" style="margin-right: 0;color: #020202;">x1</p>
                                </div>
                            </div>
                        </div>
                    </li>

                    <template v-if="!bargain&&!seckill">
                        <li class='order_coupon' v-if="commodity_type != 1&&pro.length>1">
                            <span>{{language.pay.orderDetailsr.freight}}</span>
                            <span v-if="items.freight_price==0">{{language.pay.orderDetailsr.Free_mail}}</span>
                            <span v-else>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(items.freight_price)}}</span>
                        </li>
                        <li class='order_coupon' v-if="items.coupon_list&&items.coupon_list.length>0&&is_supplier_pro&&is_hy!='true' ">
                            <span>{{language.pay.orderDetailsr.dpyh}}</span>
                            <div class="pickerBox">
                                <picker @change="mchCouponChange" :data-id="indexs" value="0" :range="items.couponList">
                                    <view class="uni-input"
                                        :style="items.couponList[mchCouponIndex[indexs]]==language.open_renew_membership.noquan?'color: rgb(153, 153, 153);':'color:#333333;'"
                                        v-if="mchCouponIndex[indexs]>=0">{{items.couponList[mchCouponIndex[indexs]]}}
                                    </view>
                                    <view class="uni-input" style="color: #f4f5f6;" v-else>
                                        {{language.pay.orderDetailsr.zwkyy}}</view>
                                </picker>
                                
                                <img :src="jiantou" class='arrow' style='margin-left: 20rpx;' />
                            </div>
                        </li>

                        <li v-if="!mask" class='order_coupona'
                            style="height: 108rpx;padding: 0;justify-content: space-between;" @click="maskclick(indexs)">
                            <!-- 订单备注 -->
                            <span>{{language.pay.orderDetailsr.Order_notes}}</span>
                            <span class="yh-order_coupon-spana"
                                style="display: flex;margin-right: 0;align-items: center;margin-left: 32rpx;">
                                <input style="width: 100%;
                                z-index:0;"
                                    :disabled="true"
                                    :placeholder="language.pay.orderDetailsr.order_placeholder_wbz"
                                    placeholder-style="color:#999999;font-size:32rpx;justify-content: flex-end;"
                                    type="text" v-model="remarks[indexs]" maxlength="25">
                                <img :src="jiantou" class='arrow' style='margin-left: 8rpx;' />
                            </span>
                        </li>
                    </template>
                </ul>
            </div>


            <ul v-if="!bargain&&!seckill" style="border-radius: 24rpx;"> <!-- 商品总价  配送运费 购物折扣 会员优惠 优惠券 合计 -->
                <li class='order_coupon'>
                    <span>{{language.pay.orderDetailsr.spzj}}</span>
                    <div class="pickerBox">
                        <view class="uni-input">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(products_total)}}</view>
                    </div>
                </li>
                <li class='order_coupon' v-if="discount">
                    <span>{{language.pay.orderDetailsr.gwzk}}</span>
                    <div class="pickerBox">
                        <view class="uni-input">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(discount)}}</view>
                    </div>
                </li>
                <!-- 虚拟商品不显示运费 -->
                <template v-if="commodity_type != 1">
                    <!-- 配送运费 -->
                    <li class='order_coupon'>
                        <span>{{language.pay.orderDetailsr.psyf}}</span>
                        <div class="pickerBox">
                            <view class="uni-input">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(freight) }}</view>
                        </div>
                    </li>
                </template>
                <li class='order_coupon' v-if='isDistribution  '>
                    <span>{{language.pay.orderDetailsr.fxzk}}</span>
                    <div class="pickerBox">
                        <view class="uni-input">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(total_discount) }}</view>
                    </div>
                </li>
                <li class='order_coupon' v-if="is_hy==true||is_hy=='true'||is_hy==1">
                    <span>{{language.pay.orderDetailsr.hyyh}}</span>
                    <div class="pickerBox">
                        <view class="uni-input">{{grade_rate_amount}}</view>
                    </div>
                </li>
                <!-- 平台优惠/满减活动 -->
                <li class='order_coupon' v-if="couponList.length>0 && is_supplier_pro&&is_hy!='true'&&is_hy!=1&&!roomId">
                    <span>{{language.pay.orderDetailsr.Discount[0]}}</span>
                    <div class="pickerBox" @click="_choose">
                        <view class="uni-input" style="font-size: 32rpx;color: #333333;" v-if="couponIndex>=0">{{couponList[couponIndex]}}</view>
                        <view class="picher_placeholder" v-else>{{language.pay.orderDetailsr.Discount[1]}}</view>
                        <img :src="jiantou" class='arrow' style='margin-left: 8rpx;'/>
                    </div>
                </li>
                <!-- 积分抵用 只有普通商品才有 -->
                <li v-if='scoreDeductionValue' style="border-bottom: 1rpx solid #eee;" >
                    <view class="order_coupon1">                        
                        <span>{{language.integral.integral_order.points_pay}}（{{language.integral.integral_order.surplus_points}}{{score}}{{language.integral.integral_order.points}}）</span>
                        <view class="pickerBox">
                            <view class="uni-input" style="font-size: 32rpx;color: #333333;" > </view> 
                           <img :src="pointsFlag?quan_ho:quan_hui" class='quan_img' @tap='pointsFlag =!pointsFlag' />
                        </view>
                    </view> 
                   <view class="order_coupon1"  >
                       <view class="">
                           <input type="number" v-if="pointsFlag" :placeholder="placeholder" v-model='jfNumber' @input='verifyInput' class='jf-input'  />
                           <text v-else style="color:#aaa">{{placeholderText}}</text>
                       </view>
                       <view class="pickerBox">
                           <view class="uni-input" style="font-size: 32rpx;color: #333333;" > </view> 
                           <text>- {{LaiketuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(Number(scoreDeductionPrice || 0),exchange_rate)}}</text>
                       </view>
                   </view>
                </li>
                <!-- 合计 -->
                <li class='order_coupon' v-if="couponList.length>0&&vipprice==0" style="border-bottom: 0;">
                    <span>{{language.pay.orderDetailsr.hj}}</span>
                    <div class="pickerBox">
                        <view class="picher_placeholder">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(total,exchange_rate)}}</view>
                    </div>
                </li>
            </ul>
            <!--运费、优惠券、合计信息-->
            <ul v-else>
                <li class='order_coupon' v-if="!bargain && coupon_status && is_distribution!=1">
                    <span>{{language.pay.orderDetailsr.Discount[2]}}</span>
                    <div @tap="coupon()">
                        <span>{{coupon_name? coupon_name : language.pay.orderDetailsr.Tips[1]+ coupon_money +language.pay.orderDetailsr.Tips[2] }}</span>
                        <img :src="jiantou" class='arrow' style='margin-left: 20rpx;' />
                    </div>
                </li>
                <li class='order_coupon'
                    v-if="!bargain && isDistribution==false && is_distribution!=1 && is_subtraction == 1 && ((reduce_money == 0 && reduce_name != '')||(reduce_money > 0 && reduce_name == ''))">
                    <span>{{language.pay.orderDetailsr.Discount[3]}}</span>
                    <span v-if="reduce_money == 0 && reduce_name != ''">{{reduce_name}}</span>
                    <span v-else>{{reduce_money}}</span>
                </li>
                <li class='order_coupon' v-if="grade_rate!=1">
                    <span>{{language.pay.orderDetailsr.Discount[4]}}</span>
                    <span>{{grade_rate*10}}{{language.pay.orderDetailsr.fracture}}</span>
                </li>
                <li class='order_coupon' v-if="is_distribution==1 && discount!=1">
                    <span>{{language.pay.orderDetailsr.Discount[5]}}</span>
                    <span>{{discount*10}}{{language.pay.orderDetailsr.fracture}}</span>
                </li>
                <li class='order_coupon'>
                    <span>{{language.pay.orderDetailsr.freight}}</span>
                    <span v-if="freightt==0">{{language.pay.orderDetailsr.Free_mail}}</span>
                    <span v-else>{{ LaiKeTuiCommon.DEFAULT_STORE_SYMBOL }}{{LaiKeTuiCommon.formatPrice(freightt)}}</span>
                </li> 
                <li class='order_coupon'>
                    <span></span>
                    <span class="yh-order_coupon-spanb" v-if='!bargain'>
                        <!-- #ifndef MP-BAIDU || MP-ALIPAY -->
                        {{language.pay.orderDetailsr.total}}：<font class="yh-order_coupon-fonta">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(total)}}</font>
                        <!-- #endif -->
                        <!-- #ifdef MP-BAIDU || MP-ALIPAY-->
                        {{language.pay.orderDetailsr.total}}：<span class="yh-order_coupon-fonta">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(total)}}</span>
                        <!-- #endif -->
                    </span>
                </li>
            </ul>
            <ul>
                <li class="yh-items"></li>
            </ul>
            <!-- 支付方式组件 --> 
			<choose-pay ref="choosePay" style="margin-bottom: 96rpx;"
			 :pay_way='pay_way'
             :priceInfo='{total}'
			 :open_pay='open_pay'
			 :user_money="user_money"
			    @chooseWay="chooseWay($event)" />
                
            <submit-order v-if="pay_way != 'offline_payment'" :ishide="ishide" :total="total" :rate="total_discount" @submit="order_pay()" />
            <div v-else class="submit-offline" >
                <div class="sub-button" @click="order_pay()">确定</div>
            </div>
        </div>

        <!-- 高度屏幕小的时候解决选择不到微信支付的问题 -->
        <div class="yh-wx-pay"></div>
        
        <!-- 备注弹窗 -->
        <view @touchmove.stop.prevent @click="maskclicka" class="bounced" v-if="mask">
            <view class="bounced-box" style="z-index: 99999;" @click.stop>
                <view class="bounced-box-box">
                    <view class="bounced-box-box-title">{{language.pay.orderDetailsr.ddbz}}</view>
                    <image  class="bounced-box-box-img"></image>
                </view>
                <view class="bounced-box-list">
                    <view class="bounced-box-list_title">
                        <textarea class="bounced-box-list_title_inptut" v-model="newremak"
                            :placeholder="language.pay.orderDetailsr.order_placeholder"
                            adjust-position="true" @blur="_ipt_blur" @input="inechange($event,bzIndex)" maxlength="200">
                           </textarea>
                        <view class="bounced-box-list_title_num">
                            <span style="color: #000000;">{{inechangenum}}</span>
                            /200
                        </view>
                    </view>
                </view>
                <view class="bounced-box-list_btn">
                    <view class="bounced-box-list_btn_box" @click="maskclick(true)">
                        {{language.pay.orderDetailsr.qd}}</view>
                </view>
            </view>
        </view>
        <!--  优惠券弹窗    -->
        <chooseS
            ref="isLanguage" 
            :is_choose_obj='is_choose_obj' 
            :is_type="chooseType" 
            :is_choose="couponList"
            :is_index="couponIndex"
             @_choose="bindCouponChange" 
             
             @_isHide="_isHide">
        </chooseS>
        <!--  优惠券    -->
        <div class='copon_div' v-if="use_coupon">
            <ul class='coupon_ul'>
                <li :key='index' @tap="coupon_use(index,item.id,item.coupon_status)" class='coupon_sue'
                    v-for="(item,index) in coupon_list">
                    <span v-if="item.activity_type == 1"
                        v-text="item.name?item.name:''+':'+language.pay.orderDetailsr.Free_mail"></span>
                    <span v-else
                        v-text="item.name&&item.name==language.pay.orderDetailsr.Tips[0]?language.pay.orderDetailsr.Tips[0]:item.name+language.pay.orderDetailsr.Tips[1]+item.money+language.pay.orderDetailsr.Tips[2]+item.coupon_name"></span>
                    <img :src="item.id==coupon_id?quan_hei:quan_hui" class="yh-img-s" />
                </li>
            </ul>
            <div @tap="closeCoupon" class="copou_close">{{language.pay.orderDetailsr.close}}</div>
        </div>
        <!-- 虚拟线下核销预约时间 -->
        <view class="virtual-time" v-if="virtualTime" @tap="virtualTime = false" @touchmove.stop.prevent>
            <view @tap.stop class="safe-area-inset-bottom">
                <view>
                    预约时间
                    <image :src="guanbi" @tap="virtualTime = false"></image>
                </view>
                <view @touchmove.stop>
                    <view>
                        <view @tap="_virtualTime(index)" v-for="(item, index) in storeHxTime" :class="storeHxTimeKey==index?'virtual-first-active':''">{{item.value}}</view>
                    </view>
                    <view v-if="storeHxTime && storeHxTime[storeHxTimeKey]" :style="{alignContent: storeHxTime[storeHxTimeKey].item.length == 0?'center':'flex-start'}">
                        <view class="chooseTime" @tap="_virtualTimeKey(itm)" v-for="(itm, inx) in storeHxTime[storeHxTimeKey].item" :class="{virtualLastActive:itm.timeId==storeHxTimeKeyTimeID,virtualLastOver:itm.write_status!=1}">
                            <span>{{itm.time_range}}</span>
                            <span>{{itm.write_status==1?'可预约':'已约满'}}</span>
                        </view>
                        <view class="noTime" v-if="storeHxTime[storeHxTimeKey].item.length == 0">
                            <image mode="widthFix" :src="noTime"></image>
                            <span>暂无可预约时间</span>
                        </view>
                    </view>
                </view>
            </view>
        </view>
        
        <!-- 弹窗组件 -->
        <show-toast :is_showToast="is_showToast" :is_showToast_obj="is_showToast_obj" :isOneBtn="isOneBtn" @confirm="confirm"></show-toast>
    </div>
</template>

<script>
    import showToast from '@/components/aComponents/showToast.vue' 
    import paymodel from '@/components/paymodel.vue'
    import mixinsOrder from '../../mixins/order'
    import mixinCoupon from '../../mixins/coupon'
    import choosePay from '@/components/choose-pay.vue'
    import chooseAddress from '@/pagesE/components/order/choose-address.vue'
    import submitOrder from '@/pagesE/components/order/submit-order.vue'
    import mock from '@/pagesE/components/skeleton/orderDetailsrMock.vue'
    import chooseS from "@/components/aComponents/choose.vue"
    import {
        oninput2
    } from '@/common/util'
    import LaiketuiCommon from "../../components/laiketuiCommon.js";
    export default {
        mounted(){
            this.$on('refreshData', this._axios);
        },
        beforeDestroy(){
            // 销毁时移除事件监听，避免内存泄漏
            this.$off('refreshData', this._axios);
        },
        computed: {
            LaiketuiCommon() {
                return LaiketuiCommon
            }
        },
        data() {
            return { 
                jfNumber:'',
                pointsFlag:false, // 是否被打卡
                placeholderText:'',
                placeholder:'',
                scoreDeductionPriceMax:0,
                scoreRatio:'',// 积分比例
                scoreDeductionPrice:0,
                payment: '',
                defaultpayment: 'wallet_pay',
                isDistribution: false, //是否是分销商品？true是，false不是
                isLive:false,//是否是直播商品
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jiantou2x.png',
                load: false,
                returnR: 0,
                newremak:'',
                exchange_rate:1,
                zsbz:'',
                cpId: '',
                title: '确认订单信息',
                grade_money: '', //会员优惠
                is_subtraction: 1,
                inechangenum: 0,
                reduce_name: '',
                pro: [], //购买商品列表
                noTime: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/zwyysj.png',
                storeImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/store.png',
                quan_hei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/xuanzehei2x.png',
                quan_hui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/xuanzehui2x.png',
                
                quan_ho: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/new_red_select.png',
                orderDetailsr_fgx: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/orderDetailsr_fgx.png',
                ishide: 0, //是否隐藏底部支付栏 1隐藏 0不隐藏
                sNo: '',
                name: '',
                is_distribution: 0,
                cart_id: '',
                pay_name: '', //pt是普通订单过来的
                fullName: '',
                fullPhone: '',
                fullcpc: '',
                firstFlag: true,
                tmplIds: ['2KrrJchj92YRKhZZ0SSHz76dmrT0cLBJ2Wfe0', 'ncs3u3Bmmi0jW7EXAik4KQvxF3JxbaulWNwbLXDto',
                    'CZAPo_TqOOeC5K7XYvBeB_LXmyXKIhXkZROArNZDwQ8'
                ], //微信订阅消息模板
                guanbi: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/guanbi2x.png',
                coupon_status: false,
                couponList: [], // 平台优惠券
                couponList1: [], // 平台优惠券
                couponIndex: 0,
                grade_rate_amount: '', //会员优惠
                mchCouponIndex: [],
                mask: false,
                allPro: [],
                is_hy: false,
                canshu: false,
                proindex: 0,
                is_plugin: false, // 是否是插件商品
                is_grade: false, //是否是会员
                total_discount: 0, //会员优惠总金额 分销折扣
                scoreDeductionValue:false, // 积分抵用显示
                score:0, // 可用积分
                cs: false,
                bgColor: [{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                gift_list: '',
                vipprice: '',
                address: {},
                discount: 0, //折扣
                isCar: false, //是否购物车进入
                bzIndex: 0,//订单备注当前index
                is_supplier_pro: true, // 判断是否为供应商商品,false 是.true 不是 
                oderPayType: 'wallet_pay',//当前支付方式，切换支付方式需要重新渲染数据，且余额支付接口需传pay_type：wallet_pay（下单接口也要）；此功能是优化功能0元支付 bugID 52195。
                chooseType: 0,//选择语言
                is_choose_obj:{
                    title: '',
                    colorLeft: '#999999',
                    colorRight: '#FA5151',
                    background: '#F4F5F6',//显示图标
                    borderRadius: '24rpx 24rpx 0 0',//提示文字
                },
                commodity_type: '',//订单类型 0实物 1虚拟 2直播订单
                virtualTime: false,//虚拟商品-是否显示-需预约-线下核销门店弹窗
                address_status: '',//虚拟商品-是否预约 1无预约 2需预约
                storeHx: [],//虚拟商品-需预约-核销门店信息
                storeHxTime: {},//虚拟商品-需预约-核销门店-可预约时间信息
                storeHxTimeKey: 0,//虚拟商品-需预约-核销门店-可预约时间-日期信息
                storeHxTimeKeyTimeID: '',//虚拟商品-需预约-核销门店-可预约时间-时间段id
                storeHxTimeKeyID: '',//虚拟商品-需预约-核销门店-可预约时间-时间段id
                storeHxTimeKeyData: '',//虚拟商品-需预约-核销门店-可预约时间-时间段
                storeHxTimeKeyTime: '',//虚拟商品-需预约-核销门店-可预约时间-时间段日期
                roomId:'',//直播间id
                is_showToast: 0,//是否显示
                is_showToast_obj: {},//弹窗显示文字
                isOneBtn: true,//弹窗只显示一个按钮
                zpStatus: true,//是否显示商家配送
                deliveryTime: '',//商家配送日期
                deliveryPeriod: '',//商家配送时间
                jfFalge:true,
                isShowDiscount:false,
                tiem:null,
                freights:[]
            }
        },
        mixins: [mixinsOrder, mixinCoupon],
        watch: {
            oderPayType(value){
                this.queryOrderPrice()
            },
            pay_way(value){
            },
            // 输入积分时
            async jfNumber(value){
                if(this.jfFalge ){
                    this.jfFalge = false
                    await this.queryOrderPrice()
                }else{
                    uni.showToast({
                        title:'请稍后',
                        icon:'none'
                    })
                }
            },
            async pointsFlag(value){  
               await this.queryOrderPrice() 
            }
        },
        
        onLoad(option) { 

            this.cpId = option.product
            this.cart_id = option.cart_id 
            this.placeholder = this.language.pay.orderDetailsr.qsrjf
            //获取缓存中的支付方式
            if(uni.getStorageSync('oderPayType')){
                let oderPayType = uni.getStorageSync('oderPayType')
                if(oderPayType.wallet_pay){this.oderPayType = 'wallet_pay'}
            }
            //初始化移除cartPay参数
            uni.removeStorageSync('cartPay')
            //初始化移除自提点选中地址
            uni.removeStorageSync('address_ziti')
            this.vipprice = option.vipprice
            this.is_hy = option.is_hy
            if (!option.vipprice) {
                this.vipprice = uni.getStorageSync('vipSource') == '' ? '0' : uni.getStorageSync('vipSource')
                // uni.removeStorageSync('vipSource')
            }
            if(option.room_id){
                this.roomId=option.room_id
            }
            this.commodity_type = option.commodity_type;
            this.getCode()
            this.canshu = uni.getStorageSync('canshu')
            this.$store.state.address_id = this.$store.state.address_id_def
            this.cpId = option.product

            this.returnR = option.returnR
            if (option.canshu) {
                this.canshu = option.canshu
            }else{
                this.canshu = false
            }

            if (this.cpId && JSON.parse(this.cpId).findIndex(item => item.sec_id) === 3) {
                this.sec_id = JSON.parse(this.cpId)[3].sec_id
                this.seckill = true
                this.activity_id = this.$store.state.seckilldata.activity_id
                this.platform_activities_id = this.$store.state.seckilldata.platform_activities_id
            }

            if (option.buy_again == true || option.buy_again == 'true') {
                this.buy_again = true
            }
            if (option.a_type == 1) {
                this.a_type == 1
            }
            this.order_id = this.$store.state.order_id
            this.cart_id = this.$store.state.cart_id
            if (Array.isArray(this.cart_id)) {
                this.cart_id = '';
            }

            if (this.cart_id) {
                uni.setStorageSync('cart_id', this.cart_id)
            }

            if (option.cart_id) {
                this.isCar = true
                this.cart_id = option.cart_id
                this.$store.state.cart_id = this.cart_id
                uni.setStorageSync('cart_id', this.cart_id)
            }

            // #ifdef H5 
            let storage = window.localStorage
            if (storage['bargain']) {
                this.cpId = storage['product']
            }

            if (storage['bargain']) {
                this.bargain = storage['bargain'] ? storage['bargain'] : this.bargain
                this.bargain_id = storage['bargain_id'] ? storage['bargain_id'] : this.bargain_id
                this.order_no = storage['order_no'] ? storage['order_no'] : this.order_no
            }

            if (storage['cart_id'] != null && storage['cart_id'] !== '') {
                this.cart_id = storage['cart_id']
            }


            if (!this.$store.state.address_id) {
                this.$store.state.address_id = storage['address_id']
            }

            if (window.location.href.indexOf('isDistribution=true') > -1) {
                this.isDistribution = true
            }
            if (window.location.href.indexOf('isLive=true') > -1) {
                this.isLive = true
            }
            // #endif

            if (this.cpId) {
                uni.setStorageSync('goodsInfo', this.cpId)
            }

            if (option.canshu) {
                uni.setStorageSync('canshu', option.canshu)
            }

            if ((option.isDistribution == 1 || option.isDistribution == 'true') && option.isDistribution != 'false') {
                this.isDistribution = true
            }

            this.bargain = option.bargain
            this.bargain_id = option.bargain_id
            this.order_no = option.order_no
			
            // #ifdef H5 
            //解决获取code后价格变为普通商品价格的问题
            if (option.product) {
                let kanjia = JSON.parse(option.product)
                if (kanjia[3]) {
                    this.bargain = kanjia[3] && kanjia[3].bargain ? kanjia[3].bargain : this.bargain
                    this.bargain_id = kanjia[4] && kanjia[4].bargain_id ? kanjia[4].bargain_id : this.bargain_id
                    this.order_no = kanjia[5] && kanjia[5].order_no ? kanjia[5].order_no : this.order_no
                }
            }
            // #endif

            this.pay_name = this.$store.state.pay_lx
            this.checkCode()
            //这里有个问题，没有设置支付密码的时候，去设置成功后返回此页面，还是提示未设置支付密码；需要改到onShow中
            this._axios()
        },
        onShow() {
            //放在这里页面也有问题，切换地址的时候返回页面会刷新请求，造成切换地址不成功。BUG ID50160
            // this._axios()
            this.couponIndex = 0
            //这里影响到了购物车下单，小程序微信支付、支付宝支付。
            //去支付时，取消支付返回此页面会再一次调用接口，但是购物车中并没有已经下单了的购物车id，会提示库存不足。
            //不用this.show()会又其他影响，知道后我再备注。
            //补丁：支付时在缓存中存一个参数，如果存在这个参数则不会再此调用show()方法。onLoad时需要删除这个缓存，进行初始化。
            if(uni.getStorageSync('cartPay') && uni.getStorageSync('cartPay').value){return}
            this.show()
        },
        onUnload() {
             uni.removeStorageSync('password_status')
            // #ifdef H5
            if (location.hostname === 'localhost') return false;
            setTimeout(() => { 
                let state = window.location.href.replace(/\?code=.*?\//, '#/')
                history.replaceState(null, null, state)
            }, 300)
            // #endif

        },
        methods: {
            // 获取订单金额
            async queryOrderPrice(){
                this.bindCouponChange(this.couponIndex,'watch')
                uni.showLoading({
                    title:'加载中...'
                })
                let product = uni.getStorageSync('goodsInfo')
                var vipSource = uni.getStorageSync('vipSource') == '' ? '0' : uni.getStorageSync('vipSource')
                let data = {
                    api: 'app.order.Settlement',//app.groupbuy.payfor => app.order.Settlement
                    cart_id: this.cart_id,
                    coupon_id: this.coupon_id ? this.coupon_id : '0',
                    vipSource: vipSource,
                    canshu: true,
                    pay_type: this.oderPayType
                }
                //配送方式
                data.is_self_delivery = this.is_express
                if (this.commodity_type == 1) {
                    data.product_type = 'VI';
                }
                // 普通商品 和 虚拟商品 计算金额 传递 积分字段
                if(this.pointsFlag && this.scoreDeductionValue){
                    // 积分
                    data.scoreDeduction = this.jfNumber 
                }else{
                    // 积分
                    data.scoreDeduction = 0 
                }
                if (this.cpId) {
                    data.product = product || this.cpId;
                    data.cart_id = ''
                }
                //获取快递地址信息
                if (this.address_id) {
                    data.address_id = this.address_id;
                }
                //获取自提门店信息 (is_express是否选择快递方式)
                if(!this.is_express){
                    if(uni.getStorageSync('address_ziti') && uni.getStorageSync('address_ziti').value){
                        //优先使用缓存中存在选择的门店地址信息。
                        data.shop_address_id = uni.getStorageSync('address_ziti').value.id
                    } else {
                        data.shop_address_id = this.shop_address_id
                    }
                } 
                if (this.buy_again) {
                    data.buy_type = 1;
                }
                if (this.isDistribution) {
                    data.product_type = 'FX'
                    data.type = 'FX'
                    this.type='FX'
                }
                if (this.isLive) {
                    data.product_type = 'ZB'
                    data.type = 'ZB'
                    this.type='ZB'
                } 
                if(this.tiem){
                    this.tiem = null
                }
                await this.$req.post({data}).then(res => {
                     uni.hideLoading()
                    if(res.code == 200 && res.data){
                        /**
                         * 普通订单 和 虚拟订单 的 积分抵扣 走的积分抵用判断
                         * 当可抵用的积分金额 大于 最大可抵扣的金额时
                         * 重置上一次输入的信息
                         */
                        if(Number(this.jfNumber || 0) > Number(this.score)){
                            uni.showToast({
                                title:this.language.integral.integral_order.ts,
                                icon:'none', 
                            })
                          
                            return
                        }else if(this.scoreDeductionValue && this.pointsFlag && (Number(this.scoreDeductionPriceMax || 0) < Number(res.data.scoreDeductionPrice || 0))){
                            // 改为后端判断
                            // const jf =this.scoreRatio.split(":")[0]
                            // const jfmax = Number(this.scoreDeductionPriceMax || 0) * jf
                            // uni.showToast({
                            //     title:'最多可抵用积分 '+jfmax,
                            //     icon:'none',
                                
                            // })
                            // setTimeout(()=>{
                            //     this.jfFalge = true
                            //     this.jfNumber = uni.getStorageSync('priceNumber')
                            // },1500)
                            // return
                        }else{
                            uni.hideLoading()
                            this.jfFalge = true
                             // uni.setStorageSync('priceNumber', this.jfNumber)
                        }
                        uni.hideLoading()
                        // this.placeholderText= `${this.language.pay.orderDetailsr.syqbjfkdk}` + this.LaiKeTuiCommon.DEFAULT_STORE_SYMBOL + res.data.scoreDeductionPrice_all
                        this.scoreDeductionPrice = Number(res.data.scoreDeductionPrice ||0)
                        this.total = res.data.total
                        setTimeout(()=>{
                            this.jfFalge = true
                            // this.jfNumber = uni.getStorageSync('priceNumber')
                        },150)
                    } else {
                        this.jfFalge = true
                        // 积分抵扣余额 独有的自定义code码
                        const codeLisr = ['50742','51135']
                        if(codeLisr.includes(res.code)){
                            this.jfNumber = ''
                            this.placeholder =this.placeholderText
                        }
                        uni.hideLoading()
                        this.tiem = setTimeout(()=>{
                            uni.showToast({
                                title: res.message,
                                icon: 'none', 
                            })
                        },500)
                       
                    }
                }).catch(e => {
                })
            },
            verifyInput(e){
                 // 获取输入的值
                const input = e.target.value;
                // 将输入的值转换为数字 
               this.jfNumber = Number( oninput2(input+'')).toString()
            },
            //弹窗点击事件
            confirm(){
                this.is_showToast = 0
            },
            //虚拟商品 选择核销时间
            _virtualShow(){
                if(this.storeHxTime && this.storeHxTime.length){
                    this.virtualTime = true
                } else {
                    this.is_showToast = 4
                    this.is_showToast_obj.title = '温馨提示'
                    this.is_showToast_obj.content = '该门店暂无排期时间可约，请更换门店！'
                    this.is_showToast_obj.endButton = '我知道了'
                }
            },
            //虚拟商品 选择核销日期
            _virtualTime(key){
                this.storeHxTimeKey = key
            },
            //虚拟商品 选择核销时间段
            _virtualTimeKey(item){
                this.storeHxTimeKeyTimeID = item.timeId
                this.storeHxTimeKeyID = item.w_id
                this.storeHxTimeKeyData = item.time_range
                this.storeHxTimeKeyTime = this.storeHxTime[this.storeHxTimeKey].value
                this.virtualTime = false
            },
            //虚拟商品跳转
            _navTo(url){
                //初始化key 解决切换门店时1门店存在的日期  2门店不存在报错问题
                this._virtualTime(0)
                uni.navigateTo({
                    url: url
                });
            },
            //选择
            _choose(){
                this.is_choose_obj.title = this.language.open_renew_membership.select
                this.chooseType = 2
                // this.changeLang(index)
            },
            //选择积分
            _choose_js(){
                this.is_choose_obj.title = '积分'
                this.chooseType = 2
                // this.changeLang(index)
            },
            //隐藏
            _isHide(){
                this.chooseType = 0
            },
            _ipt_blur(){
                uni.pageScrollTo({
                		scrollTop: 0,
                		duration: 0
                	});
            },
            inechange(e, l) {
                if(e.detail.value){
                    this.remarks[this.proindex] = e.detail.value
                }else{
                    this.remarks[this.proindex] = this.newremak
                }
                this.zsbz = e.detail.value
                this.inechangenum = e.detail.cursor
                
            },
            maskclick(e) {
                this.bzIndex = e
                this.proindex = e
                this.newremak =this.remarks[this.proindex]
                this.mask = !this.mask
                if(this.proindex == true){
                    this.newremak = this.remarks[this.proindex]
                }
                if(this.zsbz){
                    this.remarks[this.proindex]=this.zsbz
                    this.inechangenum = this.remarks[this.proindex].length
                }
                this.zsbz = ''
            },
            maskclicka(){
                this.mask = false
            },
            toUrl(url) {
                uni.navigateTo({
                    url
                })
            },
            mchCouponChange(e) {
                this.canshu = true
                let i = Number(e.currentTarget.dataset.id)

                this.mchCouponIndex.splice(i, 1, e.detail.value)
                let coupon_id = []
                this.pro.filter((items, indexs) => {
                    this.mchCouponIndex.filter((item, index) => {

                        if (indexs == index) {
                            if (item >= 0) {
                                coupon_id.push(items.coupon_list[item].coupon_id)
                            } else {
                                coupon_id.push('0')
                            }
                        }

                    })
                })
                //处理平台优惠券
                if (this.couponIndex >= 0) {
                    coupon_id.push(this.couponList1[this.couponIndex].coupon_id)
                } else {
                    coupon_id.push('0')
                }
                //平台优惠券标识
                coupon_id.push(this.couponList1[this.couponIndex].discount_type)

                this.coupon_id = coupon_id.join()
                this._axios()
            },
            // 图片报错处理
            handleErrorImg(indexs, index) {
                this.pro[indexs].list[index].img =
                    "../../static/img/Default_picture.png";
            },
            // logo图片处理
            handleErrorImgLogo(indes){
                this.pro[indes].head_img = "../../static/img/Default_picture.png";
            },
            bindCouponChange(e, type) {
                this.couponIndex = e
                this.canshu =  true 
                // this.mchCouponIndex  店铺优惠券选中的下标 []
                // this.couponIndex  平台优惠券选中的下标
                // this.couponList  平台优惠券展示的数组['','','']
                // this.couponList1  平台优惠券的数据[{},{},{}]
                let coupon_id = []
                this.pro.filter((items, indexs) => {
                    this.mchCouponIndex.filter((item, index) => {
                        if (indexs == index) {
                            if (item >= 0) {
                                coupon_id.push(items.coupon_list[item].coupon_id)
                            } else {
                                coupon_id.push('0')
                            }
                        }
                    })
                })
                if (this.couponIndex >= 0) {
                    coupon_id.push(this.couponList1[this.couponIndex].coupon_id)
                } else {
                    coupon_id.push('0')
                }
                coupon_id.push(this.couponList1[this.couponIndex].discount_type)
                this.coupon_id = coupon_id.join()
                this.chooseType = 0
                //监听中调用 不触发数据渲染
                if(type != 'watch'){
                    this._axios()
                }
            },
            usermod(e) {
                // this._axios('onshow')
                this.fullName = e.name
                this.fullPhone = e.tel
                this.fullcpc = e.cpc
            },
            //商家配送 选择的时间
            _sjps(deliveryTime, deliveryPeriod){
                this.deliveryTime = deliveryTime
                this.deliveryPeriod = deliveryPeriod
            },
            sChange (is_express) {
                this.coupon_id = 0
                if(is_express == true){
                    this.fullName = ''
                    this.fullPhone = ''
                    this.fullcpc = ''
                }
                if(is_express != 'AddrChange'){
                    this.is_express = is_express;
                }
                
                if (is_express) {
                    if(is_express != 'AddrChange'){
                        this.shop_list = {
                            id: 0
                        }
                    } 
                    this._axios('onshow')
                } else {
                    this._axios('onshow')
                }
            },
            /**
             * 对象倒叙
             * @param {Object} obj
             */
            reverseObject(obj) {
                const keys = Object.keys(obj);
                const reversed = keys.reduceRight((result, key) => {
                    result[key] = obj[key];
                    return result;
                }, {});
                return reversed;
            },
            
            _axios(type = '') {
                let product = uni.getStorageSync('goodsInfo')
                // debugger
                var vipSource = uni.getStorageSync('vipSource') == '' ? '0' : uni.getStorageSync('vipSource')
                let data = {
                    api: 'app.order.Settlement',
                    cart_id: this.cart_id,
                    coupon_id: this.coupon_id ? this.coupon_id : '0',
                    vipSource: vipSource,
                    canshu :this.canshu,
                    pay_type: this.oderPayType
                }
                //配送方式
                data.is_self_delivery = this.is_express
                if(data.is_self_delivery ==2 || data.is_self_delivery == 0){
                    data.canshu = true
                }
                //
                if (this.canshu) {
                    data.canshu = this.canshu;
                }
                if (this.commodity_type == 1) {
                    data.product_type = 'VI';
                    if(this.address_status == 2){
                        if(uni.getStorageSync('address_ziti') && uni.getStorageSync('address_ziti').value){
                            //优先使用缓存中存在选择的门店地址信息。
                            data.shop_address_id = uni.getStorageSync('address_ziti').value.id
                        } else {
                            data.shop_address_id = this.shop_address_id
                        }
                    }
                }
                if (this.cpId) {
                    data.product = product || this.cpId;
                    data.cart_id = ''
                }
                //获取快递地址信息
                if (this.address_id) {
                    data.address_id = this.address_id;
                }
                // 普通商品 和 虚拟商品 计算金额 传递 积分字段
                if(this.pointsFlag && this.scoreDeductionValue){
                    // 积分
                    data.scoreDeduction = this.jfNumber 
                }else{
                    // 积分
                    data.scoreDeduction = 0 
                }
                
                //实物商品 获取自提门店信息 (is_express是否选择快递方式)
                if(this.commodity_type == 0 && !this.is_express){
                    if(uni.getStorageSync('address_ziti') && uni.getStorageSync('address_ziti').value){
                        //优先使用缓存中存在选择的门店地址信息。
                        data.shop_address_id = uni.getStorageSync('address_ziti').value.id
                    } else {
                        data.shop_address_id = this.shop_address_id
                    }
                } 

                if (this.buy_again) {
                    data.buy_type = 1;
                }

                if (this.isDistribution) {
                    data.product_type = 'FX'
                    data.type = 'FX'
                    this.type='FX'
                }
                if (this.isLive) {
                    data.product_type = 'ZB'
                    data.type = 'ZB'
                    this.type='ZB'
                }
                this.$req.post({
                    data
                }).then(res => {
                    this.canshu = false
                   
                    // 报错库存不足的时候 status = 1, 其他的时候会返回 code 不等于 200 的值
                    if (res.status === 0 || (res.hasOwnProperty('code') && res.code != 200)) {
                        this.load = true
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        });
                        if(res.code != '00000' && res.code != '51135'){
                        	setTimeout(() => {
                        		this.navBack()
                        	}, 1500)
                        }
                    } else {
                        var {
                            data: {
                                total_discount,
                                address,
                                coupon_id,
                                payment,
                                defaultpayment,
                                freight,
                                password_status,
                                products,
                                products_total,
                                total,
                                user_money,
                                enterless,
                                time_id,
                                is_distribution,
                                discount,
                                grade_money,
                                discountMoney,
                                grade_rate,
                                coupon_list,
                                total_discount,
                                shop_status,
                                shop_list,
                                grade_rate_amount,
                                reduce_name,
                                is_subtraction,
                                gift_list,
                                admin_mch_id,
                                address_status,
                                mchStoreList,
                                is_supplier_pro,
                                zpStatus,
                                scoreDeductionValue,
                                score,
                                scoreRatio,
                                isShowDiscount
                            }
                        } = res;
                        // 虚拟商品 
                            this.isShowDiscount =isShowDiscount
                            if(mchStoreList && mchStoreList.length>0){
                                //核销门店
                                this.storeHx = mchStoreList
                                //核销日期排序
                                let arr = []
                                for (let a in mchStoreList[0].date){
                                    arr.push(a)
                                }
                                arr.sort()
                                //核销日期排序后 obj转为数组
                                arr = arr.map((item, index)=>{
                                    return {value: item, item: mchStoreList[0].date[item]}
                                })
                                //给核销时间段加上唯一id
                                let timeId = 1
                                if(arr && arr.length>0){
                                    for(var i=0; i<=arr.length; i++){
                                        if(arr[i] && arr[i].item && arr[i].item.length){
                                            arr[i].item.forEach(itm => {
                                                this.$set(itm, 'timeId', timeId)
                                                timeId++
                                            })
                                        }
                                    }
                                }
                                this.storeHxTime = arr
                                this.storeHxTimeKeyTimeID = ''
                                this.storeHxTimeKeyID = ''
                                this.storeHxTimeKeyData = ''
                                this.storeHxTimeKeyTime = ''
                            }
                            this.scoreDeductionValue = scoreDeductionValue
                            // 只有普通订单和 虚拟订单能显示积分抵用组合支付
                            if(scoreDeductionValue){
                                // 积分最大可抵用金额
                                this.scoreDeductionPriceMax = res.data.total1
                                // 可抵用积分金额
                                this.placeholderText= `${this.language.pay.orderDetailsr.syqbjfkdk}` + this.LaiKeTuiCommon.DEFAULT_STORE_SYMBOL + this.LaiKeTuiCommon.formatPrice(res.data.scoreDeductionPrice_all)
                                // 积分比例
                                this.scoreRatio = scoreRatio
                                // 汇率
                                this.exchange_rate = res.data.exchange_rate
                            }
                            this.score = score
                              
                        // 虚拟商品结束
                        this.address_status = address_status
                        this.total_discount = total_discount;
                        this.grade_rate_amount = grade_rate_amount
                        this.zpStatus = zpStatus==1?true:false
                
                        if (gift_list && !Array.isArray(gift_list)) {
                            gift_list.admin_mch_id = admin_mch_id;
                            this.gift_list = gift_list;
                        } else {
                            this.gift_list = '';
                        }
                        // 砍价商品请求的数据
                        let coupon_money = this.isDistribution ? res.coupon_money : res.data.coupon_money
                        let reduce_money = this.isDistribution ? res.reduce_money : res.data.reduce_money
                        this.coupon_money = coupon_money

                        this.total = Number(total).toFixed(2)
                          
                          
                          //解决 自定义上门自提地址 更换优惠券 变更问题   
                          // 订单配送方式
                          const orderType = this.$refs['chooseAddress']?.is_choose_index || 0
                          // 虚拟商品不运行地址逻辑,因为后端结算金额接口没有返回address地址对象
                          if(data.product_type  != 'VI' ){
                              if(orderType == 1 && address){ 
                                  const {name ,tel,cpc} = address
                                  this.fullName = this.fullName == name? name: this.fullName
                                  this.fullPhone = this.fullPhone == tel? tel: this.fullPhone
                                  this.fullcpc = this.fullcpc == cpc? cpc: this.fullcpc
                                  if(this.fullName.length > 0 && this.fullPhone.length > 0){ 
                                      address = {...address,name:this.fullName,tel:this.fullPhone} 
                                  }
                              }else{ 
                                  // 原逻辑
                                   this.fullName = address.name || ''
                                   this.fullPhone = address.tel || ''
                                   this.fullcpc = address.cpc || ''
                              }
                          }
                          
                          //处理收货地址的逻辑 
                          this.handleOrderAddress(address, shop_list, shop_status)
                          
                        if (coupon_list && coupon_list.length > 0) {
                            this.couponList1 = coupon_list
                            this.couponList = []
                            coupon_list.filter((item, index) => {
                                if(item.coupon_name == '不使用优惠券'){
                                    this.couponList.push(this.language.open_renew_membership.noquan)
                                }else{
                                    this.couponList.push(item.coupon_name)
                                }
                                if (item.coupon_status) {
                                    this.couponIndex = index
                                }
                            })
                            this.coupon_status = true
                        }
                        this.time_id = time_id
                        this.payment = payment
                        this.defaultpayment = defaultpayment
                        if(this.$refs.choosePay){
                            
                            //余额支付
                            if (this.payment.wallet_pay == 1) {
                                this.$refs.choosePay.chooseWay('useWallet')
                            }
                            //#ifdef MP-WEIXIN
                                if (this.payment.jsapi_wechat == 1) {
                                    this.$refs.choosePay.chooseWay('wxPay')
                                }
                            //#endif

                            //#ifdef H5
                            //判断是浏览器h5还是微信h5
                            if (this.is_wx()) {
                                if (this.payment.jsapi_wechat == 1) {
                                    this.$refs.choosePay.chooseWay('wxPay')
                                }
                            }else{
                                //H5微信支付
                                if (this.payment.H5_wechat == 1) {
                                    this.$refs.choosePay.chooseWay('wxPay')
                                }
                                //H5支付宝支付
                                if (this.payment.alipay == 1) {
                                    this.$refs.choosePay.chooseWay('aliPay')
                                }
                                //H5贝宝
                                if (this.payment.paypal == 1) {
                                    this.$refs.choosePay.chooseWay('paypal')
                                }
                            }
                            //#endif
                        }

                        if (products && products.length > 0) {
                            // 竞拍商品请求数据
                            this.pro = products
                            this.payTitle = products[0].list[0].product_title
                            //微信支付 支付通知需要用到的参数 bugid 55401
                            this.title = this.payTitle
                            this.is_plugin = !products[0].hasOwnProperty('shop_subtotal')

                            let flag = false
                            if (this.remarks == '') {
                                flag = true
                                this.remarks = []
                            }
                            let arr = []
                            products.forEach(v=>{
                                arr.push(...v.list.map(i=>`${i.pid}:${Number(i.freight)}`))
                            })
                            this.freights =  arr
                            this.mchCouponIndex = []
                            this.allPro = [],
                                products.filter(items => {
                                    if (flag) {
                                        this.remarks.push('')
                                    }
                                    // 拿到product（商品）里的pid，用于地址查询传参
                                    if (items.list) {
                                        items.list.filter(i => {

                                            this.allPro.push(i.pid)
                                        })
                                    }

                                    let couponIndex = -1
                                    items.couponList = []
                                    if (items.coupon_list && items.coupon_list.length) {
                                        items.coupon_list.filter((item, index) => {
                                            items.couponList.push(item.coupon_name)

                                            if (item.coupon_status) {
                                                couponIndex = index
                                            }
                                        })
                                    }
                                    this.mchCouponIndex.push(couponIndex)
                                })
                        }
                        if(user_money){
                            this.user_money = Number.parseFloat(user_money)
                        }
                        
                        this.password_status = password_status
                        
                        if (type !== 'onshow') {
                            this.coupon_id = coupon_id
                            for(var i = 0; i<this.couponList1.length;i++){
                                if(this.couponList1[i].coupon_status==true){
                                    this.coupon_id = this.couponList1[i].coupon_id
                                }
                            }
                        } else {
                            if (uni.getStorageSync('chooseAddress')) {
                                this.address = uni.getStorageSync('chooseAddress')
                            }
                        }
                        
                        
                        this.enterless = enterless
                        this.is_distribution = is_distribution
                        this.discount = discountMoney
                        
                        this.is_grade = (grade_rate > 0 && grade_rate < 1) ? true : false
                    
                        this.reduce_money = reduce_money
                        this.reduce_name = reduce_name
                        this.is_subtraction = is_subtraction
                        this.products_total = products_total
                        this.grade_money = grade_money
                        
                        // 运费赋值留到最后，计算价格是放在运费的监听函数里
                        this.freightt = parseFloat(freight).toFixed(2)
                        this.freight = freight
                        this.total_discount=total_discount
                        this.is_supplier_pro = is_supplier_pro
                        
                        this.show_pay_way(this.defaultpayment)

                        uni.setStorageSync('cart_id', this.cart_id)
                        this.load = true
                    }
                    
                }).catch(e => {
                    this.load = true
                })
            }
        },
        components: { 
            paymodel,
            choosePay,
            chooseAddress,
            submitOrder,
            mock,
            chooseS,
            showToast
        }
    }
</script>

<style lang="less" scoped>
    @import url("@/laike.less");
    @import url("@/static/css/pay/orderDetailsr.less");
 
    ul {
        background-color: #fff;
    }
    .jf-input{
        width:400rpx
    }
    .order_coupon1{
        display: flex;
        flex-flow: row nowrap;
        justify-content: space-between;
        align-items: center;
        margin-left: 30rpx;
        padding-left: 0;
        margin-right: 32rpx;
        height: 50rpx;
        color: #020202;
        font-size: 28rpx;
    }
    .zpIcon {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        width: 54rpx;
        height: 26rpx;
        border: 1px solid #FF7D00;
        border-radius: 4rpx;
        color: #FF7D00;
        margin-right: 6rpx;
    }
    /deep/.yh-order_coupon-spana{
        .uni-input-input{
                white-space: nowrap;
                text-overflow: ellipsis;
                overflow: hidden;
            } 
        
    }
    /* 修复配送方式样式：choose-address 压缩版样式异常时，强制覆盖 */
    /deep/.choose-address .psfs{
        display: flex !important;
        align-items: center !important;
        justify-content: space-between !important;
        margin: 0 32rpx !important;
        padding: 32rpx 0 !important;
        box-sizing: border-box !important;
        border-bottom: 2rpx solid rgba(0, 0, 0, 0.1) !important;
    }
    /deep/.choose-address .psfs > view:first-child{
        font-size: 32rpx !important;
        color: #333333 !important;
        line-height: 44rpx !important;
    }
    /deep/.choose-address .psfs .psfs-xz{
        display: inline-flex !important;
        align-items: center !important;
        justify-content: flex-end !important;
    }
    /deep/.choose-address .psfs .psfs-xz > span{
        font-size: 28rpx !important;
        color: #333333 !important;
        margin-right: 8rpx !important;
        line-height: 40rpx !important;
    }
    /deep/.choose-address .psfs .psfs-xz > image{
        width: 32rpx !important;
        height: 44rpx !important;
        flex-shrink: 0 !important;
    }
    
    .virtual{
        display: flex;
        flex-direction: column;
        justify-content: center;
        background-color: #ffffff;
        border-radius: 0 0 24rpx 24rpx;
        margin-bottom: 16rpx;
        >view:first-child{
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            margin: 32rpx 32rpx 0 32rpx;
            padding-bottom: 32rpx;
            box-sizing: border-box;
            border-bottom: 2rpx solid rgba(0, 0, 0, .1);
            >view:nth-child(1){
                display: flex;
                align-items: center;
                justify-content: space-between;
                >view:nth-child(1){
                    font-weight: 500;
                    font-size: 32rpx;
                    color: #333333;
                }
                >view:nth-child(2){
                    display: flex;
                    >image{
                        width: 32rpx;
                        height: 44rpx;
                    }
                }
            }
            >view:nth-child(2){
                font-size: 28rpx;
                color: #666666;
                margin-top: 12rpx;
            }
        }
        >view:last-child{
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 32rpx;
            box-sizing: border-box;
            >view:nth-child(1){
                font-size: 32rpx;
                color: #333333;
            }
            .virtualTisi{
                font-size: 32rpx;
                color: #999999;
            }
            .virtualTime{
                display: flex;
                >span{
                    font-weight: 500;
                    font-size: 32rpx;
                    color: #333333;
                    margin-right: 8rpx;
                }
                >image{
                    width: 32rpx;
                    height: 44rpx;
                }
            }
        }
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
                    flex-direction: row;
                    flex-wrap: wrap;
                    flex: 1;
                    height: 600rpx;
                    overflow-y: auto;
                    padding: 32rpx;
                    box-sizing: border-box;
                    >view:nth-child(2n){
                        margin-left: 20rpx;
                    }
                    .chooseTime{
                        width: 220rpx;
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
.submit-offline{
    position: fixed;
    bottom: 0;
    width: 100%;
    padding: 24rpx 32rpx;
    background: #fff;
    z-index: 1000;
    display: flex;
    justify-content: center;
    align-items: center;
    box-shadow: 0 -2rpx 8rpx rgba(0, 0, 0, 0.1);
    .sub-button{
       width: 100%;
       height: 100rpx;
       display: flex;
       justify-content: center;
       align-items: center;
       background: linear-gradient(270deg, #ff6f6f 0, #fa5151 100%);
       border-radius: 50rpx;
       color: #fff;
    }   
}    
 
</style>
