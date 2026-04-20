<template>
    <div class="container"                 
        :style="{
            background: `linear-gradient(90deg,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`,
        }"> 
        <!-- 登录验证 -->
        <lktauthorize
            ref="lktAuthorizeComp"
            v-on:pChangeLoginStatus="changeLoginStatus"
        ></lktauthorize>
        <!-- 头部 -->
        <heads
            :title="language.store.title"
            :isAnd="isAnd"
            :titleColor="titleColor"
            :bgImg="headBg"
            :ishead_w="fanhui"
        ></heads>
        <!-- 内容 -->
        <div class="relative" v-if="!load">
            <!-- 店铺信息模块 -->
            <div class="storeTop">
                <div class="storeTop_title">
                    <div class="storeTopLeft">
                        <img
                            :src="shop_logo"
                            @error="handleErrorImg"
                            @tap="
                                _toAllGoods(
                                    '/pagesB/store/shopDetails?shop_id=' +
                                        shop_id +
                                        '&shop_list_id=' +
                                        shop_address_id +
                                        '&type=' +
                                        tabThree,
                                    'navigate'
                                )
                            "
                        />
                        <div class="storeTopLeftText">
                            <div class="storeName">
                                <div
                                    class="storeNameText"
                                    @tap="
                                        _toAllGoods(
                                            '/pagesB/store/shopDetails?shop_id=' +
                                                shop_id +
                                                '&shop_list_id=' +
                                                shop_address_id +
                                                '&type=' +
                                                tabThree,
                                            'navigate'
                                        )
                                    "
                                >
                                    <span class="ss_font">{{ shop_name }}</span>
                                    <div class="seeStore">
                                        <img :src="jiantou" alt="" />
                                    </div>
                                </div>

                                <div class="storeBtn">
                                    <!-- 分享按钮 -->
                                    <view class="fX">
                                        <view class="fXBtn">
                                            <image
                                                :src="img_fx2"
                                                @tap="showShareMask(shop_id)"
                                            ></image>
                                        </view>
                                    </view>
                                    <!-- 关注按钮 -->
                                    <view
                                        :class="{
                                            gz: true,
                                            changebgc:
                                                shoucang != language.store.sc,
                                        }"
                                        @tap="_collStore()"
                                    >
                                        <view
                                            class="gzBtn"
                                            v-if="shoucang == language.store.sc"
                                        >
                                            <image :src="attention_sc"></image>
                                        </view>
                                        <view style="">{{
                                            shoucang == language.store.sc
                                                ? language.store.gz
                                                : language.store.ygz
                                        }}</view>
                                    </view>
                                </div>
                            </div>
                            <div style="margin-top: 10rpx">
                                <span class="is_closed" v-if="is_open == 2">{{
                                    language.store.Closed
                                }}</span>
                                <span class="is_open" v-else>{{
                                    language.store.yyz
                                }}</span>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- 门店参数 -->
                <div class="storeSellNum">
                    <div class="storeSellNum_sale">
                        <div class="storeSellNum_span">
                            {{ quantity_on_sale ? quantity_on_sale : 0 }}
                        </div>
                        <div class="storeSellNum_sale1">
                            {{ language.store.sale }}
                        </div>
                    </div>
                    <div class="storeSellNum_Sold">
                        <div class="storeSellNum_span">
                            {{ quantity_sold ? quantity_sold : 0 }}
                        </div>
                        <div class="storeSellNum_Sold2">
                            {{ language.store.Sold }}
                        </div>
                    </div>

                    <div class="storeSellNum_people">
                        <div class="storeSellNum_span">
                            {{ collection_num ? collection_num : 0 }}
                        </div>
                        <div class="storeSellNum_peoplee">
                            {{ language.store.num }}
                        </div>
                    </div>
                </div>
            </div>
                <!-- 预售商品 -->
                <div
                    class="mytab_div"
                    @tap="
                    "
                >
                    <div class="mytab_div_img">
                        <img :src="dianpuzy8" alt="" />
                    </div>
                    <p>{{ language.store.yssp }}</p>
                </div>
                <!-- 限时秒杀 -->
                <div
                    class="mytab_div"
                >
                    <div class="mytab_div_img">
                        <img :src="dianpuzy6" alt="" />
                    </div>
                    <p>{{ language.store.xszk }}</p>
                </div>
                <!-- 拼团商品 -->
                <div
                    class="mytab_div"
                >
                    <div class="mytab_div_img">
                        <img :src="dianpuzy3" alt="" />
                    </div>
                    <p>{{ language.store.ptsp }}</p>
                </div>
                <!-- 竞拍商品 -->
                <div
                    class="mytab_div"
                    @tap="
                        navTo(
                                shop_id
                        )
                    "
                >
                    <div class="mytab_div_img">
                        <img :src="dianpuzy5" alt="" />
                    </div>
                    <p>{{ language.store.jpsp }}</p>
                </div>
                <!-- 省钱好券 -->
                <div
                    class="mytab_div"
                    @click="maskclick"
                    v-if="shaco_plugin.coupon == 1"
                >
                    <div class="mytab_div_img">
                        <img :src="dianpuzy4" alt="" />
                    </div>
                    <p>{{ language.store.sqhq }}</p>
                </div>
                <!-- 限时折扣 -->
                <div
                    class="mytab_div"
                    @tap="
                        navTo(
                        )
                    "
                >
                    <div class="mytab_div_img">
                        <img :src="xszk" alt="" />
                    </div>
                    <p>{{ language.store.xszka }}</p>
                </div>
            </div>
            <storeDiy v-if="is_diy" @getHomeBg="getHomeBg" :is_open="is_open" :shop_id="shop_id"></storeDiy>

            <view @touchmove.stop.prevent class="bounced" v-if="maskcoupon">
                <view class="bounced-box">
                    <view class="bounced-box-box">
                        <view class="bounced-box-box-title">{{
                            language.store.lqyhq
                        }}</view>
                        <image
                            :src="guanbi"
                            class="bounced-box-box-img"
                            @click="maskclick"
                        ></image>
                    </view>
                    <view class="bounced-box-list">
                        <scroll-view
                            class="bounced-box-list_scroll"
                            scroll-y="true"
                        >
                            <ul class="coupon_ul">
                                <li
                                    class="coupon_li"
                                    v-for="(item, index) in coupon_list"
                                    :class="{
                                        active_background: isNone[index],
                                    }"
                                    :key="index"
                                >
                                    <img
                                        class="bg_img"
                                        :src="
                                            isNone[index]
                                                ? coupon_bg1
                                                : item.point_type == 4 ||
                                                  item.point_type == 2
                                                ? coupon_bg2
                                                : coupon_bg
                                        "
                                    />
                                    <div class="coupon_li_data">
                                        <div
                                            class="coupon_left"
                                            :class="{
                                                active_border: isNone[index],
                                            }"
                                        >
                                            <p
                                                class="coupon_p"
                                                :style="
                                                    isNone[index]
                                                        ? 'color:#333333'
                                                        : 'color:#FA5151'
                                                "
                                            >
                                                {{ item.name }}
                                            </p>
                                            <div
                                                class="coupon_price"
                                                :class="{
                                                    active_color: isNone[index],
                                                }"
                                            >
                                                <div
                                                    class="coupon_price_div"
                                                    v-if="
                                                        item.activity_type == 2
                                                    "
                                                >
                                                    <span
                                                        class="coupon_price_span"
                                                        :class="{
                                                            active_color:
                                                                isNone[index],
                                                        }"
                                                        >{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</span
                                                    >
                                                    <span
                                                        class="coupon_price_money"
                                                        :class="{
                                                            active_color:
                                                                isNone[index],
                                                        }"
                                                        >{{ LaiKeTuiCommon.formatPrice(item.money) }}</span
                                                    >
                                                    <span
                                                        class="coupon_t"
                                                        :class="{
                                                            active_color:
                                                                isNone[index],
                                                        }"
                                                        >{{ item.limit }}</span
                                                    >
                                                </div>
                                                <div
                                                    class="coupon_price_div color_ff3"
                                                    v-else-if="
                                                        item.activity_type == 3
                                                    "
                                                >
                                                    <span
                                                        class="coupon_price_money"
                                                        :class="{
                                                            active_color:
                                                                isNone[index],
                                                        }"
                                                    >
                                                        {{ item.discount }}
                                                        <span
                                                            class="font_28"
                                                            :class="{
                                                                active_color:
                                                                    isNone[
                                                                        index
                                                                    ],
                                                            }"
                                                            >{{
                                                                language.shop
                                                                    .coupon.fold
                                                            }}</span
                                                        >
                                                    </span>
                                                    <span
                                                        class="coupon_t"
                                                        :class="{
                                                            active_color:
                                                                isNone[index],
                                                        }"
                                                        >{{ item.limit }}</span
                                                    >
                                                </div>
                                                <div
                                                    class="color_ff3"
                                                    v-else-if="
                                                        item.activity_type == 1
                                                    "
                                                >
                                                    <span
                                                        class="coupon_t ml_0"
                                                        :class="{
                                                            active_color:
                                                                isNone[index],
                                                        }"
                                                        >{{ item.limit }}</span
                                                    >
                                                </div>
                                            </div>
                                        </div>
                                        <div
                                            class="coupon_right"
                                            v-if="
                                                item.point !=
                                                language.store.qiangguang
                                            "
                                        >
                                            <button
                                                v-if="item.point_type == 1"
                                                @tap="clickCouponBtn(item)"
                                                type="button"
                                                class="coupon_but"
                                                :class="{
                                                    coupon_no: isNone[index],
                                                    coupon_red:
                                                        item.point ==
                                                        language.store.toUse,
                                                    coupon_red:
                                                        item.point ==
                                                        language.store.ljlq,
                                                }"
                                            >
                                                <span ref="point">{{
                                                    item.point ==
                                                    language.store.ljlq
                                                        ? language.shop.coupon
                                                              .points[0]
                                                        : item.point == ""
                                                        ? language.shop.coupon
                                                              .points[1]
                                                        : item.point == ""
                                                        ? language.shop.coupon
                                                              .points[2]
                                                        : language.store.ylq
                                                }}</span>
                                            </button>
                                        </div>
                                    </div>
                                </li>
                            </ul>
                        </scroll-view>
                    </view>
                </view>
            </view>
            <!-- 轮播图 -->
            <view v-if="banner && banner.length > 0 && !is_diy" style="position: relative">
                <swiper
                    class="swiperBox"
                    autoplay="true"
                    interval="3000"
                    circular="true"
                    @change="changeBanner"
                >
                    <swiper-item v-for="(item, index) in banner" :key="index">
                        <image
                            class="swiper-image"
                            :src="item.image"
                            @tap="
                                _toAllGoods(item.url, item.type, item.parameter)
                            "
                        >
                        </image>
                    </swiper-item>
                </swiper>
                <view class="swiper_dots_new" v-if="banner.length > 1">
                    <view>{{ dotIndex + 1 }}</view>
                    <view>{{ banner.length }}</view>
                </view>
            </view>
            <div class="topThreeBar" v-if="!is_diy">
                <div :class="{ active: tabThree == 1 }" @tap="changeTab(1)">
                    {{ language.store.tjsp }}
                    <p class="hen" v-if="tabThree == 1"></p>
                </div>
                <div :class="{ active: tabThree == 2 }" @tap="changeTab(2)">
                    {{ language.store.qbsp }}
                    <p class="hen" v-if="tabThree == 2"></p>
                </div>
                <div :class="{ active: tabThree == 3 }" @tap="changeTab(3)">
                    {{ language.store.spfl }}
                    <p class="hen" v-if="tabThree == 3"></p>
                </div>
            </div>

            <!-- 直播入口 -->
            <!-- #ifdef MP-WEIXIN -->
            <div class="liveIndex" v-if="showLive && !is_diy">
                <img :src="liveImg" alt="" />
                <view>
                    <view>{{ liveTitle }}</view>
                    <view class="tag" v-if="liveStatus">
                        <img :src="live" alt="" />
                        <text> {{ language.store.live_broadcast }}</text>
                    </view>
                    <view class="retag tag" v-else>
                        <img :src="replay" alt="" />
                        <text>{{ language.store.playback }}</text>
                    </view>
                    <navigator open-type="navigate" :url="liveUrl">{{
                        language.store.Watch_now
                    }}</navigator>
                </view>
            </div>
            <!-- #endif -->
            <!-- 推荐 -->
            <ul
                class="goods_ul"
                :class="banner && banner.length > 0 ? '' : 'bgColors'"
                v-if="tabThree == 1&& !is_diy"
                :style="banner && banner.length > 0 ? '' : 'padding-top: 0rpx;'"
            >
                <li
                    class="goods_like"
                    v-for="(item, index) in list"
                    :key="index"
                    @tap="_goods(item.id)"
                >
                    <div class="goods_like_img relative">
                        <img
                            lazy-load
                            :src="item.imgurl"
                            @error="handleErrorImg2(index)"
                            style="
                                width: 100%;
                                height: 100%;
                                border-radius: 16rpx;
                            "
                        />
                        <div v-if="item.status == 3" class="dowmPro">
                            {{ language.store.shelf }}
                        </div>
                        <div v-else-if="item.stockNum == 0" class="dowmPro">
                            {{ language.shoppingCart.soldOut }}
                        </div>
                    </div>
                    <p class="overtitle">{{ item.product_title }}</p>
                    <view class="goods_lable" v-if="item.s_type_list.length">
                        <view
                            :style="{ backgroundColor: item_1.color }"
                            v-for="(item_1, index_1) in item.s_type_list"
                            :key="index_1"
                            >{{ item_1.name }}</view
                        >
                    </view>
                    <div class="goods_mun">
                        <div class="allgoods_price">
                            <span class="red"
                                >{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}<span>{{ LaiKeTuiCommon.formatPrice(item.vip_yprice) }}</span></span
                            >
                        </div>
                        <template v-if="item.is_appointment != 2">
                            <img
                                v-if="is_open == 1"
                                @tap.stop="shopping_j(item)"
                                class="img"
                                :src="shopImg"
                            />
                            <img v-else class="img" :src="shopImgGray" />
                        </template>
                    </div>
                </li>
            </ul>
            <!-- 普通商品 -->
            <ul
                class="goods_ul"
                :class="banner && banner.length > 0 ? '' : 'bgColors'"
                v-if="tabThree == 2&& !is_diy"
                :style="banner && banner.length > 0 ? '' : 'padding-top: 0rpx;'"
            >
                <li
                    class="goods_like"
                    v-for="(item, index) in list"
                    :key="index"
                    @tap="_goods(item.id)"
                >
                    <div class="goods_like_img relative">
                        <img
                            lazy-load
                            :src="item.imgurl"
                            @error="handleErrorImg2(index)"
                            style="
                                width: 100%;
                                height: 100%;
                                border-radius: 8px;
                            "
                        />
                        <div v-if="item.status == 3" class="dowmPro">
                            {{ language.store.shelf }}
                        </div>
                        <div v-else-if="item.stockNum == 0" class="dowmPro">
                            {{ language.shoppingCart.soldOut }}
                        </div>
                    </div>
                    <p class="overtitle">{{ item.product_title }}</p>
                    <view class="goods_lable" v-if="item.s_type_list.length">
                        <view
                            :style="{ backgroundColor: item_1.color }"
                            v-for="(item_1, index_1) in item.s_type_list"
                            :key="index_1"
                            >{{ item_1.name }}</view
                        >
                    </view>
                    <div class="goods_mun">
                        <div class="allgoods_price">
                            <span class="red"
                                >{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}<span>{{ LaiKeTuiCommon.formatPrice(item.vip_yprice) }}</span></span
                            >
                        </div>
                        <template v-if="item.is_appointment != 2">
                            <img
                                v-if="is_open == 1"
                                @tap.stop="shopping_j(item)"
                                class="img"
                                :src="shopImg"
                            />
                            <img v-else class="img" :src="shopImgGray" />
                        </template>
                    </div>
                </li>
            </ul>
            <!-- 预售商品 -->
            <ul
                class="goods_ul"
                :class="banner && banner.length > 0 ? '' : 'bgColors'"
                v-if="tabThree == 4&& !is_diy"
                :style="banner && banner.length > 0 ? '' : 'padding-top: 0rpx;'"
            >
                <li
                    class="goods_like"
                    v-for="(item, index) in pro_list"
                    :key="index"
                    @tap="_goods3(item.id)"
                >
                    <div class="goods_like_img relative">
                        <image
                            lazy-load
                            :src="item.imgurl"
                            style="
                                width: 100%;
                                height: 100%;
                                border-radius: 16rpx;
                            "
                        />
                    </div>
                    <p class="overtitle">{{ item.product_title }}</p>
                    <view class="goods_lable" v-if="item.s_type_list.length">
                        <view
                            :style="{ backgroundColor: item_1.color }"
                            v-for="(item_1, index_1) in item.s_type_list"
                            :key="index_1"
                            >{{ item_1.name }}</view
                        >
                    </view>
                    <div class="goods_mun5">
                        <!-- 价格 -->
                        <div class="price2">
                            <p>
                                <span
                                    >￥<span>{{
                                        Number(item.price).toFixed(2)
                                    }}</span></span
                                >
                                <span>已定{{ item.volume }}件</span>
                            </p>
                        </div>
                        <!-- 定金 -->
                        <p class="price3" v-if="item.deposit">
                            {{ language.goods.goods.deposit }}
                            <text>
                                {{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(item.deposit) }}</text
                            >
                        </p>
                        <!-- 划掉的价格 -->
                        <p class="price3 new_price" v-else>
                            <text>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(item.price) }}</text>
                        </p>
                    </div>
                </li>
            </ul>
            <!-- 商品分类 -->
            <div
                v-if="tabThree == 3&& !is_diy"
                :style="
                    banner && banner.length > 0
                        ? ''
                        : 'padding-top: 0rpx;padding-bottom: 8rpx;'
                "
            >
                <ul class="ul_proClass" v-if="list && list.length">
                    <li
                        @tap="toGoods(item.pname, item.cid)"
                        v-for="(item, index) in list"
                        :key="index"
                        class="proClass"
                    >
                        <span v-if="item.pname">{{ item.pname }}</span>
                        <img :src="jiantou" alt="" />
                    </li>
                </ul>
            </div>
            <div
                class="nodata"
                v-if="
                    !is_diy && 
                    (tabThree == 1 && !list.length) ||
                    (tabThree == 2 && !list.length) ||
                    (tabThree == 3 && !list.length) ||
                    (tabThree == 4 && !pro_list.length)
                "
            >
                <image :src="noPro" mode="widthFix"></image>
                <p v-if="tabThree == 1">{{ language.goods.goodsDet.zwtjsp }}</p>
                <p v-if="tabThree == 2">{{ language.goods.goodsDet.zwsp }}</p>
                <p v-if="tabThree == 3">{{ language.goods.goodsDet.zwspfl }}</p>
                <p v-if="tabThree == 4">{{ language.store.zwyssp }}</p>
            </div>
        </div>
        <!-- 关注成功 -->
        <view
            class="xieyi mask"
            style="background-color: initial"
            v-if="is_sus"
        >
            <view
                style="
                    width: 272rpx;
                    height: 272rpx;
                    background-color: rgba(51, 51, 51, 0.9);
                "
            >
                <view
                    style="
                        margin: 32rpx 0;
                        text-align: center;
                        margin-top: 64rpx;
                    "
                >
                    <image
                        style="width: 68rpx; height: 68rpx"
                        :src="sus"
                    ></image>
                </view>
                <view
                    class="xieyi_title"
                    style="
                        margin-bottom: 0;
                        margin-top: 0;
                        color: #fff;
                        font-weight: 500;
                        font-size: 32rpx;
                    "
                >
                    {{ is_text }}
                </view>
            </view>
        </view>
        <!-- 提示弹窗 -->
        <view class="xieyi" v-if="is_ts">
            <view>
                <view
                    class="xieyi_title"
                    style="padding: 0 32px; margin-bottom: 64rpx"
                    >{{ address ? address : is_ts_tit }}</view
                >
                <view class="xieyi_btm" @click="_xieyiShow('ts')">{{
                    language.store.kwno
                }}</view>
            </view>
        </view>
        <!-- 提示 -->
        <view
            class="tishi"
            style="
                position: fixed;
                top: 50%;
                margin-top: -46rpx;
                left: 50%;
                margin-left: -128rpx;
            "
            v-if="is_tishi"
        >
            <view
                style="
                    width: 256rpx;
                    height: 92rpx;
                    background-color: rgba(0, 0, 0, 0.5);
                    border-radius: 48rpx;
                    color: #ffffff;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                "
            >
                <span style="font-size: 16px; font-weight: 400">{{
                    is_tishi_content
                }}</span>
            </view>
        </view>
        <!-- 提示 -->
        <view
            class="tishi"
            style="
                position: fixed;
                top: 50%;
                margin-top: -46rpx;
                left: 50%;
                margin-left: -128rpx;
                z-index: 9999;
            "
            v-if="is_lingqu_box"
        >
            <view
                style="
                    width: 256rpx;
                    height: 92rpx;
                    background-color: rgba(0, 0, 0, 0.5);
                    border-radius: 48rpx;
                    color: #ffffff;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                "
            >
                <span style="font-size: 16px; font-weight: 400">{{
                    is_lingqu
                }}</span>
            </view>
        </view>
        <!-- 商品规格 -->
        <skus ref="attrModal" @confirm="_confirm"></skus>
        <!-- 分享 -->
        <share
            ref="share"
            :share="share"
            :pro="pro"
            :pro_id="'1'"
            :type="shareType"
        ></share>

        <notice ref="mynotice" ></notice>
        <!-- 直播图标xxxx -->
        <view class="live_box" v-if="livingStatus&&mch_is_open==1">
            <view class="icon">
                <!-- <img :src="live" alt=""> -->
                <i class="one"></i>
                <i class="two"></i>
                <i class="three"></i>
            </view>
            <view class="text">店铺</view>
            <view class="text">直播中</view>
        </view>
    </div>
</template>

<script>
     import notice from '@/components/notice.vue'
import skus from "../../components/skus.vue";
import share from "@/components/share.vue";
import storeDiy from "./storeDiy.vue";
import { mapMutations, mapState } from "vuex";

// #ifdef H5
// #endif
export default {
    data() {
        return {
            bgColor: [
                {
                    item: "",
                },
                {
                    item: "",
                },
            ],
            is_tishi: false,
            is_tishi_content: "暂无门店位置信息",
            maskcoupon: false,
            isFlashSale: false,
            is_diy: false, //diy：是否显示
            is_ts: false, //提示
            is_lingqu_box: false,
            is_lingqu: "领取成功",
            is_ts_tit: "暂无地址信息！", //提示内容
            dotIndex: 0, //轮播图下标
            load: true,
            shop_list: [], //线下门店
            store_bottom_flag: false,
            tabThree: 1,
            live:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/live.png", //直播icon

            ErrorImg:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/Default_picture.png",
            sus:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/sus.png",
            dianpuzy1:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/dianpuzy (1).png",
            dianpuzy2:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "/images/icon/dianpuzy (2).png",
            dianpuzy3:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/dianpuzy (3).png",
            dianpuzy4:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/dianpuzy (4).png",
            dianpuzy5:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/dianpuzy (5).png",
            dianpuzy6:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/dianpuzy (6).png",
            dianpuzy7:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/dianpuzy (7).png",
            dianpuzy8:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/dianpuzy (8).png",
            saves:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/save.png",
            share_img:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "/images/icon/share.png",
            back2:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/back2x.png",
            disc:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/2222x.png",
            jiantou:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/jiantou2x.png",
            blueRight:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/blueRight.png",
            title_index: 0,
            wx_img:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/wechat.png",
            erm_img:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/ewmShare.png",
            scImg:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/sc2x.png",
            close:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/close_bb.png",
            live:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/live.png",
            replay:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/replay.png",
            xszk:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/xszk.png",
            mch_coupon_bg:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/mch_coupon_bg.png",
            mch_coupon_bgh:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/mch_coupon_bgh.png",
            mch_coupon_ylq:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/mch_coupon_ylq.png",
            mch_coupon_yqg:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/mch_coupon_yqg.png",
            mch_fx:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/mch_fx.png",
            red_jt:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/red_jt.png",
            mch_dz:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/store_address.png",
            mch_sc:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/mch_sc.png",
            mch_sch:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/mch_schh.png",
            shopImg:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/shop/shop_gouwuche.png",
            shopImgGray:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/shopping_cart_gray.png",
            copy_link:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/copy_link.png",
            noPro:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/noPresale.png",
            img_wsc:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/shop/shop_weishoucan.png",
            img_ysc:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/shop/shop_yishoucan.png",
            img_fx:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/shop/shop_fenxiang.png",
            img_fx2:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/goodsDetailed_ffxx.png",
            img_wz:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/shop/shop_weizhi.png",
            headBg:
                "url(" +
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/auction/grbj.png) 750rpx 100%",
            shopBg:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/shop/shopBg.png",
            guanbi:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/guanbi2x.png",
            receive_img:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/lingqu2x.png",
            noreceive_img:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/qiangguang2x.png",
            coupon_bg1:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/coupon_js.png",
            back_img:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/back2x.png",
            huiquan_img:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/huiquan2x.png",
            coupon_on:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/coupon/coupon_on.png",
            coupon_no:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/coupon_no.png",
            coupon_icon:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/coupon_icon.png",
            coupon_bg:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/coupon_hybj.png",
            coupon_bg2:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/coupon_bg2.png",
            grbj:
                "background-image: url(" +
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/auction/grbj.png);background-size: 100vw 100vh;background-repeat: no-repeat;",
            title: "店铺主页",
            titleColor: "#333333", //标题字体颜色
            fanhui: 2,
            isNone: [], //是否是已抢光状态，true已抢光
            isAnd: true,
            mask: false,
            shoucang: "收藏",
            collStatus: true,
            fastTap: true,
            shop_id: 1,
            list: "",
            proList: "",
            page: 1,
            loadingType: 0,
            shop_name: "",
            shop_logo: "",
            collection_num: "",
            collection_status: "",
            quantity_on_sale: "",
            quantity_sold: "",
            shareHref: "", //分享的链接
            shareHref2: "", //转发的链接
            shareContent: "一起来用来客推吧！", //分享的内容
            sharehrefTitle: "一起来用来客推吧!", //分享的链接的标题
            shareImg: "", //分享的图片
            allPro: "",
            shareDiv: false,
            shareMask: false,
            saveEWM: false,
            ewmImg: "",
            is_share: false,
            storeTop_content_img:
                "https://xiaochengxu.laiketui.com/V3/images/icon1/storeBottom.png",
            store_sc:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/store_sc.png",
            attention_sc:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/attention_sc.png",
            store_ysc:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/store_ysc.png",
            liveImg: "",
            liveTitle: "",
            showLive: false,
            liveUrl: "",
            liveStatus: false,
            liveGoods: [], //直播商品
            is_open: 0,
            shareH5Mask: false,
            coupon_list: [],
            haveSkuBean: "",
            num: "",
            numb: "",
            banner: [],
            distance: "",
            address: "",
            shop_address_id: "",
            pro_list: [],
            is_open: 1,
            //卡券插件显示
            isOpenCoupon: 0,
            mobilePhone: "",
            is_isShowTime: "",
            is_business_hours: "",
            shareType: "",
            share: {
                mch: {
                    type: false, //是否店铺分享
                    mchId: "", //店铺id
                },
                goods: {
                    title: "", //分享-商品标题
                    imgUrl: "", //分享-商品图片
                    price: "", //分享-商品金额
                },
                shareHref: "", //分享-链接
                shareType: "", //分享-类型：普通商品（shareType: 'gm'） 分销商品（shareType: 'fx'） 竞拍商品（shareType: 'jp'）
            },
            poster_img: "", //店铺宣传图
            pro: {},
            start: 1,
            limit: 999,
            is_sus: false, //提示
            is_text: "",
            shaco_plugin: {}, //存取插件配置参数
            livingStatus:false,
            mch_is_open:0,
        };
    },
    components: {
        notice,
        skus,
        share,
        storeDiy
    },
    onLoad(option) {
        this.shop_id = option.shop_id;
        if (option.fatherId) {
            uni.setStorageSync("fatherId", option.fatherId);
        }
        this.setMchLL()
    },
    watch: {
    
        collection_status: function (e) {
            if (e == 0) {
                this.shoucang = this.language.store.sc;
            } else {
                this.shoucang = this.language.store.ygz;
            }
        },
    },
    // 补充onShow,从登录页面回来后刷新登录状态
    onShow() {
        if (uni.getStorageSync("shop_address_id")) {
            this.shop_address_id = uni.getStorageSync("shop_address_id");
            uni.removeStorageSync("shop_address_id");
        }

        this.getCoupon();
        this.getPlus();
        this.axios();
        this.loadingType = 0;
        this.page = 1;
        // 调用弹窗
        setTimeout(() => {
            this.$refs.mynotice.getData();
        }, 0);
    },
    computed: {
        width1: function () {
            var width;
            width = this.list.length * 150;
            return uni.upx2px(width) + "px";
        },
        ...mapState({
            _cart_num: "cart_num",
        }),
    },
    //微信小程序好友分享
    onShareAppMessage: function (res) {
        //隐藏分享弹窗
        this.$refs.share._closeAllMask();
        let shareTitle = ""; //分享标题
        let shareImageUrl = ""; //分享图片
        if (this.share.mch.type) {
            //店铺分享
            shareTitle = this.shop_name;
            shareImageUrl = this.poster_img;
        } else {
            //商品分享
            shareTitle = this.share.goods.title;
            shareImageUrl = this.share.goods.imgUrl;
        }
        //分享路径
        let sharePath = this.$refs.share.share_href;

        return {
            title: shareTitle,
            path: sharePath,
            imageUrl: shareImageUrl,
        };
    },
    onPageScroll(e) {
        //是否隐藏 header组件中的标题模块
        if (e.scrollTop > 0) {
            this.headBg = "#ffffff";
            this.isAnd = false;
        } else {
            this.isAnd = true;
        }
    },
    onReachBottom: function () {
        // tabThree 1推荐 2全部商品 3商品分类
        if (this.loadingType != 0) {
            return;
        }
        this.loadingType = 1;
        if (this.tabThree != 3) {
            var data = {
                api: "mch.App.Mch.Store_homepage_load",
                pagesize: 10,
                page: this.page + 1,

                shop_id: this.shop_id,
                type: this.tabThree,
            };
            if (this.list.length > 0) {
                this.$req
                    .post({
                        data,
                    })
                    .then((res) => {
                        let { data } = res;
                        this.page += 1;
                        if (data.list.length > 0) {
                            if (this.tabThree == 1 || this.tabThree == 2) {
                                data.list.filter((item) => {
                                    item.vip_price = Number(
                                        item.vip_price
                                    ).toFixed(2);
                                    item.vip_yprice = Number(
                                        item.vip_yprice
                                    ).toFixed(2);
                                });
                            }

                            this.list = this.list.concat(data.list);
                            this.loadingType = 0;
                        } else {
                            this.loadingType = 2;
                        }
                    });
            }
        }
    },
    methods: {
        getHomeBg(e){ 
            this.bgColor = e;
        },
        setMchLL() {
            let data = {
                api: "mch.App.Mch.browse_record",
                shop_id: this.shop_id,
            };
            this.$req.post({ data }).then((res) => {
                if(res.code == 200){
                } else {
                }
            });
        },
        handleErrorImg2(index) {
            setTimeout(() => {
                this.list[index].imgurl = this.ErrorImg;
            }, 0);
        },
        // 图片报错处理
        handleErrorImg(index) {
            setTimeout(() => {
                this.shop_logo = this.ErrorImg;
            }, 0);
        },
        navTo(url) {
            uni.navigateTo({
                url: url,
            });
        },
        maskclick() {
            this.maskcoupon = !this.maskcoupon;
        },
        //关闭协议弹窗
        _xieyiShow(e) {
            if (e == "tc") {
                this.is_tc = !this.is_tc;
            } else {
                this.is_ts = !this.is_ts;
            }
        },
        changeBanner(e) {
            this.dotIndex = e.detail.current;
        },
        _toAllGoods(url, type, parameter) {
            if (url) {
                if (type == "navigate") {
                    uni.navigateTo({
                        url,
                    });
                    this.page = 1;
                    this.loadingType = 0;
                } else if (type == "switchTab") {
                    this.page = 1;
                    this.loadingType = 0;
                    uni.switchTab({
                        url,
                    });
                }
            }
        },
        _confirm(sku) {
            Object.assign(this.$data, sku);

            if (!this.fastTap) {
                return;
            }
            this.fastTap = false;
            if (Boolean(this.haveSkuBean)) {
                if (this.num == 0) {
                    uni.showToast({
                        title: this.language.store.Tips[0],
                        duration: 1000,
                        icon: "none",
                    });
                    this.fastTap = true;
                } else if (this.num != 0) {
                    this._shopping();
                }
            } else {
                if (this.num == 0) {
                    uni.showToast({
                        title: this.language.store.Tips[0],
                        duration: 1000,
                        icon: "none",
                    });
                    this.fastTap = true;
                } else {
                    uni.showToast({
                        title: this.language.store.Tips[1],
                        duration: 1000,
                        icon: "none",
                    });
                    this.fastTap = true;
                }
            }
        },
        // 点击确定购买之后，如果库存不为零。则运行
        _shopping(id) {
            if (this.haveSkuBean) {
                var data = {
                    api: "app.product.add_cart",
                    pro_id: this.proid,
                    attribute_id: this.haveSkuBean.cid,
                    num: this.numb,
                    type: "addcart",
                };

                this.$req
                    .post({
                        data,
                    })
                    .then((res) => {
                        let { code, data, message } = res;
                        if (code == 200) {
                            uni.showToast({
                                title: this.language.store.Tips[2],
                                icon: "none",
                            });

                            this.$store.state.access_id = data.access_id;

                            this.haveSkuBean = "";
                            this.$refs.attrModal._mask_f();
                            this.fastTap = true;

                            this.cart_num(this.numb + this._cart_num);
                        } else {
                            uni.showToast({
                                title: message,
                                icon: "none",
                            });
                            this.fastTap = true;
                        }
                    })
                    .catch((error) => {
                        this.fastTap = true;
                    });
            } else {
                this.fastTap = true;
            }
        },
        // 为你推荐商品右下角的小购物车图标
        shopping_j(item) {
            if (item.status == 3) {
                uni.showToast({
                    title: this.language.shoppingCart.proDown,
                    duration: 1500,
                    icon: "none",
                });
                return;
            }
            if (item.stockNum == 0) {
                uni.showToast({
                    title: this.language.shoppingCart.soldOut1,
                    duration: 1500,
                    icon: "none",
                });
                return;
            }
            if (!this.fastTap) {
                return;
            }
            this.fastTap = false;
            this.proid = item.id;
            var data = {
                api: "app.product.index",
                pro_id: item.id,
            };

            this.$req
                .post({
                    data,
                })
                .then((res) => {
                    this.fastTap = true;
                    let {
                        data: {
                            pro,
                            attribute_list,
                            qj_price,
                            commodity_type,
                            write_off_settings
                        },
                    } = res;

                    this.$refs.attrModal.imgurl = pro.img_arr[0];
                    this.$refs.attrModal.num = pro.num;
                    this.$refs.attrModal.price = qj_price;
                    this.$refs.attrModal.skuBeanList = attribute_list;
                    //虚拟商品 加入购物车不需要显示 库存
                    if(commodity_type == 1){
                        this.$refs.attrModal.pro2 = {commodity_type, write_off_settings}
                    }
                    this.$refs.attrModal.initData();
                    this.$refs.attrModal._mask_display();
                })
                .catch((error) => {
                    this.fastTap = true;
                });
        },
        clickCouponBtn(item) {
            if (item.point == "立即领取") {
                this.isLogin(() => {
                    if (!this.fastTap) {
                        return;
                    }
                    this.fastTap = false;

                    let data = {
                        api: "plugin.coupon.Appcoupon.Receive",
                        id: item.id,
                    };

                    this.$req
                        .post({
                            data,
                        })
                        .then((res) => {
                            let { code, message } = res;
                            if (code == 200) {
                                this.is_lingqu_box = true;
                                setTimeout(() => {
                                    this.getCoupon();
                                    this.is_lingqu_box = false;
                                }, 1500);
                            } else {
                                uni.showToast({
                                    title: message,
                                    duration: 1500,
                                    icon: "none",
                                });
                                this.fastTap = true;
                            }
                        });
                });
            }
        },
        async getLiveRoomImg(id) {
            let data = {
                api: "app.liveBroadcast.getLiveList",
                start: this.start,
                limit: this.limit,
            };
            let res = await this.$req.post({
                data,
            });
            if (res.code == 200) {
                res.data.list.room_info.filter((item) => {
                    if (item.roomid == id) {
                        this.liveImg = item.cover_img;
                        this.liveTitle = item.name;
                        if (item.live_status == 101) {
                            this.showLive = true;
                            this.liveStatus = true;
                            this.liveUrl = `plugin-private://wx2b03c6e691cd7370/pages/live-player-plugin?room_id=${item.roomid}`;
                        } else if (item.live_status == 103) {
                            this.liveStatus = false;
                            this.showLive = true;
                            this.liveUrl = `../../pagesC/liveReplay/liveReplay?roomID=${item.roomid}&title=${item.name}&img=${item.cover_img}`;
                            this.liveGoods = item.goods;
                            this.replayGoods(item.goods);
                        } else {
                            this.showLive = false;
                        }
                    }
                });
            }
        },
        _tishi() {
            if (!this.address) {
                this.is_tishi = !this.is_tishi;
                setTimeout(() => {
                    this.is_tishi = !this.is_tishi;
                }, 1000);
                return;
            }
            this.is_ts = !this.is_ts;
        },
        _back() {
            uni.navigateBack({
                delta: 1,
            });
        },
        changeLoginStatus() {
            this.axios();
        },
        toGoods(name, cid) {
            uni.navigateTo({
                url:
                    "/pagesC/goods/goods?cid=" +
                    cid +
                    "&name=" +
                    name +
                    "&shop_id=" +
                    this.shop_id,
            });
        },
        changeTab(num) {
            this.tabThree = num;
            this.page = 1;
            this.loadingType = 0;
            this.axios();
            if (num == 4) {
                this.preSale1();
            }
        },
        _discover(id) {
            this.$store.state.pro_id = id;
            uni.navigateTo({
                url: "/pagesC/collection/discover?pro_id=" + id,
            });
        },
        // 详情
        _goods(id) {
            this.$store.state.pro_id = id;
            uni.navigateTo({
                url:
                    "/pagesC/goods/goodsDetailed?toback=true&pro_id=" +
                    id +
                    "&mch_id=" +
                    this.shop_id,
            });
        },
        /**
         * 分享功能
         * @param {Object} shop 店铺id：0商品分享 !=0店铺分享
         */
        showShareMask(shop) {
            //验证登录
            this.isLogin(() => {
                //获取分享信息 这里会存在生命周期问题 显示弹窗用$nextTick延迟执行
                this.getGoodsDetailParams(shop);
                this.$nextTick(() => {
                    this.$refs.share.showShareMask();
                });
            });
        },
        /**
         * 重定向分享页面 + 携带参数
         * @param {Object} shop 店铺id：0商品分享 !=0店铺分享
         */
        getGoodsDetailParams(shop) {
            //参数说明：
            //shop      (店铺id)          店铺分享shop>0，否则为商品分享
            //goodsInfor(分享商品信息)     title商品名称、imgUrl商品图片、price商品金额（如果不是商品详情页面的分享可以不传）
            //plugIn    (分享插件类型)     gm普通商品、fx分销商品、jp竞拍商品...
            //pages     (重定向页面)       分享页面的路径
            //states    (重定向页面的参数)  分享的页面需要携带的参数
            let plugIn = "gm";
            let pages = "";
            let states = "";
            let goodsInfor = {
                title: "",
                imgUrl: "",
                price: "",
            };
            if (shop && shop > 0) {
                //店铺分享
                pages = "/pagesB/store/store";
                states =
                    "?shop_id=" +
                    shop +
                    "&fatherId=" +
                    uni.getStorageSync("user_id");
            } else {
                //商品分享
                pages = "";
                states = "";
            }
            //获取分享参数
            this.LaiKeTuiCommon.shareModel(
                this,
                shop,
                pages,
                states,
                plugIn,
                goodsInfor
            );
        },
        _goods3(id) {
            uni.navigateTo({
                url:
                    "/pagesC/preSale/goods/goodsDetailed?toback=true&pro_id=" +
                    id +
                    "&url=" +
                    this.url,
            });
        },
        _collStore() {
            if (!this.fastTap) {
                return;
            }
            this.fastTap = false;
            this.LaiKeTuiCommon.lktDelaySetVal(this);
            this.isLogin(() => {
                var data = {
                    api: "mch.App.Mch.Collection_shop",
                    shop_id: this.shop_id,
                };
                this.collection_status = this.collection_status == 0 ? 1 : 0;
                this.$req
                    .post({
                        data,
                    })
                    .then((res) => {
                        if (this.shoucang == this.language.store.sc) {
                            this.is_text = this.language.goods.goodsDet.qxgzcg;
                        } else {
                            this.is_text = this.language.goods.goodsDet.gzcg;
                        }
                        this.is_sus = true;
                        if (res.code == 200) {
                            setTimeout(() => {
                                this.is_sus = false;
                                this.axios();
                                this.title_index = 0;
                                this.proList = this.allPro[0];
                            }, 1500);
                        } else {
                            uni.showToast({
                                title: res.message,
                                duration: 1500,
                                icon: "none",
                            });
                        }
                    })
                    .catch((error) => {
                        this.fastTap = true;
                    });
            });
        },
        getPlus() {
            let data = {
                api: "app.index.pluginStatus",
                mchId: this.shop_id,
            };
            this.$req.post({ data }).then((res) => {
                this.shaco_plugin = res.data.plugin;
            });
        },
        getCoupon() {
            let data = {
                api: "plugin.coupon.Appcoupon.MchCoupon",
                shop_id: this.shop_id,
            };
            this.$req
                .post({
                    data,
                })
                .then((res) => {
                    if (res.code == 200) {
                        if (res.data.list && res.data.list.length > 0) {
                            res.data.list.filter((item) => {
                                item.money = Number(item.money);
                                item.discount = Number(item.discount);
                            });
                        }
                        this.coupon_list = res.data.list;
                        if (this.coupon_list && this.coupon_list.length) {
                            let arr = [];
                            for (let i = 0; i < this.coupon_list.length; i++) {
                                arr.push(false);
                                if (this.coupon_list[i].point === "已抢光") {
                                    arr[i] = true;
                                }
                            }
                            this.isNone.push(...arr);
                        }
                        this.fastTap = true;
                    }
                });
        },
        // 预售商品
        preSale1() {
            let data = {
                api: "plugin.presell.AppPreSell.getPreGoodsList",
                type: "1",
                mchId: this.shop_id,
            };
            this.$req
                .post({
                    data,
                })
                .then((res) => {
                    if (res.code == 200) {
                        this.pro_list = res.data.goodsList;
                    }
                });
        },
        //
        axios() {
            uni.showLoading({
                title: this.language.showLoading.loading,
                mask: true,
            });

            let longitude = uni.getStorageSync("longitude");
            let latitude = uni.getStorageSync("latitude");

            this.$req
                .post({
                    data: {
                        api: "mch.App.Mch.Store_homepage",
                        shop_id: this.shop_id,
                        shop_list_id: this.shop_address_id,
                        type: this.tabThree,
                        longitude,
                        latitude,
                    },
                })
                .then((res) => {
                    this.load = false;
                    uni.hideLoading();
                    if (res.code == 200) {
                        let { data: data1 } = res;
                        this.livingStatus = res.data.livingStatus
                        this.is_diy = res.data.hasDiy
                        if(this.is_diy == false){ 
                            this.bgColor = [
                              { item: "#F6F6F6" },
                              { item: "#F6F6F6" }
                            ]; 
                        }
                        this.mch_is_open = res.data.mch_is_open
                        if (this.tabThree == 1 || this.tabThree == 2) {
                            data1.list.filter((item) => {
                                item.vip_price = Number(item.vip_price).toFixed(
                                    2
                                );
                                item.vip_yprice = Number(
                                    item.vip_yprice
                                ).toFixed(2);
                            });
                        }
                        this.is_open = data1.is_open;
                        this.list = data1.list;
                        this.list = this.list.map((item) => {
                            return {
                                ...item,
                                imgurl:
                                    item.imgurl +
                                    "?a=" +
                                    Math.floor(Math.random() * 1000000),
                            };
                        });
                        this.isFlashSale = data1.isFlashSale;
                        this.shop_name = data1.shop_name;
                        this.shop_logo = data1.shop_logo;
                        this.poster_img = data1.poster_img;
                        this.is_open = data1.is_open;
                        this.collection_num = data1.collection_num;
                        this.collection_status = data1.collection_status;
                        this.quantity_on_sale = data1.quantity_on_sale;
                        this.quantity_sold = data1.quantity_sold;
                        this.banner = data1.banner;
                        this.shopBg = this.banner[0]
                            ? this.banner[0].image
                            : "";
                        data1.distance = (
                            Number(data1.distance) / 1000
                        ).toFixed(1);
                        this.distance = data1.distance;
                        this.address =
                            data1.sheng +
                            " " +
                            data1.shi +
                            " " +
                            data1.xian +
                            " " +
                            data1.address;
                        this.mobilePhone = data1.mobile;
                        this.is_isShowTime = data1.isShowTime;
                        this.is_business_hours = data1.business_hours;

                        this.sharehrefTitle = this.shop_name;
                        this.shareImg = this.shop_logo;
                        this.shareContent = this.shop_name;

                        // 线下门店
                        this.shop_list = res.data.shop_list;
                        // #ifdef MP-WEIXIN
                        this.getLiveRoomImg(data1.roomid);
                        // #endif
                        this.isOpenCoupon = res.data.isOpenCoupon;
                    } else {
                    }
                })
                .catch((error) => {
                    this.load = false;
                    uni.hideLoading();
                });
        },
        storeTop_content_show() {
            this.store_bottom_flag = !this.store_bottom_flag;
            if (this.store_bottom_flag) {
                this.storeTop_content_img =
                    this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    "images/icon1/storeTop.png";
            } else {
                this.storeTop_content_img =
                    this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    "images/icon1/storeBottom.png";
            }
        },
        ...mapMutations({
            replayGoods: "SET_REPLAY_GOODS",
            cart_num: "SET_CART_NUM",
        }),
    },
};
</script>

<style lang="less" scoped>
@import url("@/laike.less");
@import url("../../static/css/store/store.less");
/deep/ .translateSelectLanguage {
    display: none !important;
}
/deep/ .allgoods_vertical{
    background-color: transparent;
    .allgoods_vertical_li {
        background-color: #fff;
        padding-bottom: 0rpx;
    
    }
    .goodsImg {
        margin:16rpx;
    }
}
/deep/.virtualKeyBoardHeight{
    display: none;
}

.bounced-box-list_scroll_box {
    width: 100%;
    height: 800rpx;
}
.bounced-box-list_scroll {
    width: 100%;
    height: 800rpx;
}
.bounced {
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
    position: fixed;
    left: 0;
    top: 0;
    z-index: 1000;
}
.order_detail {
    // padding-top: 16rpx;
}
.bounced-box {
    bottom: 0;
    width: 100%;
    height: 55%;
    background: #fff;
    position: fixed;
    z-index: 999;
    border-radius: 24rpx 24rpx 0px 0px;
}
.bounced-box-list_title {
    width: 654rpx;
    height: 494rpx;
    border-radius: 20rpx;
    border: 1rpx solid rgba(0, 0, 0, 0.1);
    display: flex;
    flex-direction: column;
}
.bounced-box-list_title_num {
    padding-left: 32rpx;
    text-align: right;
    padding-right: 32rpx;
}
.bounced-box-list_title_inptut {
    padding: 32rpx 32rpx 0 32rpx;
    width: 590rpx;
    height: 400rpx;
}
.bounced-box-box {
    width: 100%;
    display: flex;
    height: 136rpx;
    display: flex;
    background-color: #f4f5f6;
    border-radius: 24rpx 24rpx 0px 0px;
    align-items: center;
    justify-content: center;
}
.bounced-box-list {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 50%;
    margin-top: 48rpx;
    height: 800rpx;
}
.bounced-box-box-title {
    margin-left: 270rpx;
    font-size: 40rpx;

    font-weight: 500;
    color: #333333;
}
.bounced-box-list_btn {
    margin-top: 64rpx;
    display: flex;
    justify-content: center;
    width: 100%;
    align-items: center;
    height: 124rpx;
    border-top: 1rpx solid #eee;
}
.bounced-box-list_btn_box {
    width: 686rpx;
    height: 92rpx;
    background: linear-gradient(270deg, #ff6f6f 0%, #fa5151 100%);
    border-radius: 52rpx;
    line-height: 92rpx;
    text-align: center;
    color: #fff;

    font-weight: 500;
    font-size: 32rpx;
}
.bounced-box-box-img {
    width: 32rpx;
    height: 32rpx;
    margin-left: 200rpx;
}
.couponbox_txt {
    color: #fa5151;
    margin-left: 16rpx;
}
.couponbox {
    width: 686rpx;
    height: 72rpx;
    line-height: 72rpx;
    margin-left: 32rpx;
    background: transparent;
    background-color: rgba(250, 81, 81, 0.3);
    border-radius: 16rpx;
    display: flex;
    justify-content: space-between;
}
.bgColors {
}
.bgImg {
    position: absolute;
    top: 0;
    width: 100%;
    height: 448rpx;
    background-size: contain;
    .bgColor {
        width: 100%;
        height: 100%;
        background: linear-gradient(
            180deg,
            rgba(0, 0, 0, 0.3) 0%,
            #000000 100%
        );
    }
}
.bgImg::after {
    content: "";
    display: block;
    width: 100%;
    height: 150rpx;
    background: linear-gradient(180deg, #000000 0%, rgba(0, 0, 0, 0) 100%);
}
.fiexd {
    width: 102rpx;
    height: 390rpx;
    background: rgba(255, 255, 255, 0.9);
    box-shadow: 0 0 24rpx 0 rgba(0, 0, 0, 0.12);
    border-radius: 16rpx 2rpx 2rpx 16rpx;
    border: 1rpx solid rgba(0, 0, 0, 0.1);
    position: fixed;
    right: 0;
    bottom: 100rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    > view {
        flex: 1;
        width: 100%;
        > view:nth-child(1) {
            padding-top: 24rpx;
            padding-bottom: 8rpx;
            display: flex;
            align-items: center;
            justify-content: center;
            > image {
                width: 34rpx;
                height: 34rpx;
            }
            > button {
                width: 100%;
                height: initial;
                border: none;
                border-radius: initial;
                padding: 0;
                margin: 0;
                line-height: 0;
                background-color: transparent !important;
                .btn_img {
                    width: 34rpx;
                    height: 34rpx;
                }
            }
            > button::after {
                border: none;
            }
        }
        > view:nth-child(2) {
            font-size: 24rpx;
            font-weight: 400;
            color: #333333;
            line-height: 34rpx;
            text-align: center;
            margin-bottom: 18rpx;
        }
        .fiexd_xian {
            width: 48rpx;
            height: 2rpx;
            background: #000000;
            opacity: 0.1;
            margin: 0 auto;
        }
        .fiexd_xian1 {
            width: 32rpx;
            height: 2rpx;
            background: #cccccc;
            border-radius: 10rpx;
            margin: 0 auto;
            margin-bottom: 8rpx;
        }
    }
    .wZ {
        padding-bottom: 8rpx;
        box-sizing: border-box;
    }
}
.swiper_dots_new {
    width: 80rpx;
    height: 36rpx;
    background-color: rgba(0, 0, 0, 0.4);
    display: flex;
    border-radius: 18rpx;

    position: absolute;
    right: 48rpx;
    bottom: 16rpx;
    > view:first-child {
        width: 44rpx;
        height: 36rpx;
        background-color: rgba(0, 0, 0, 0.5);
        border-radius: 18rpx;
    }
    > view:last-child {
        padding-left: 8rpx;
    }
    > view {
        font-size: 24rpx;
        font-weight: 500;
        color: #ffffff;
        text-align: center;
    }
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
.limit {
    display: flex;
    width: 100%;
    justify-content: center;
    align-items: center;
    image {
        border-radius: 24rpx;
        width: 686rpx;
        height: 252rpx;
    }
}
.mytab {
    display: flex;
    flex-direction: row;
    flex-wrap: wrap;
    margin: 12rpx 0;
}

.mytab_div {
    width: 126rpx;
    height: 128rpx;
    text-align: center;
    font-size: 24rpx;
    margin-left: 48rpx;
}
.mytab_div_img {
    height: 72rpx;
    margin-bottom: 14rpx;
    img {
        height: 72rpx;
        width: 72rpx;
    }
}
.goods_lable {
    display: flex;
    flex-wrap: wrap;
    margin-bottom: 8rpx;
    padding-left: 16rpx;
    view {
        padding: 8rpx;
        font-size: 20rpx;
        color: #fff;
        height: 40rpx;
        border-radius: 8rpx;
        margin-right: 8rpx;
        margin-bottom: 8rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        box-sizing: border-box;
    }
}
</style>
