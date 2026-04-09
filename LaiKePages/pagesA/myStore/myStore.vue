<template>
    <div class="container myStore">
        <lktauthorize
            ref="lktAuthorizeComp"
            v-on:pChangeLoginStatus="changeLoginStatus"
        ></lktauthorize>
        <heads
            :title="language.myStore.title"
            :bgColor="bgColor"
            titleColor="#333333"
            :returnR="returnR"
            ishead_w="2"
        >
        </heads>
        <div v-if="!load">
            <div class="checkDiv" v-if="haveStore == 1 && is_lock == 0">
                <div class="noFindDiv">
                    <div style="noFindImgDiv">
                        <img class="noFindImg" :src="checkImg" />
                    </div>
                    <div class="checkDivData">
                        {{ language.applySuc.submittedSuccse }}
                    </div>
                    <span class="noFindText font_24"
                        >{{ language.myStore.inReviewDisc1 }}{{ auto_examine
                        }}{{ language.myStore.inReviewDisc2 }}</span
                    >
                    <br />
                    <span class="noFindText font_24">{{
                        language.applySuc.waitReview
                    }}</span>
                </div>
            </div>
            <div class="checkDiv" v-else-if="haveStore == 2 && is_lock == 0">
                <div class="myStoreBox">
                    <!-- 头像/店铺名字以及简介（点击可进入店铺设置页面） -->
                    <div class="myStoreTop">
                        <div @tap="_navigateTo2('../myStore/storeSetup')">
                            <view class="imgs">
                                <image
                                    :src="info.headImg"
                                    mode="aspectFill"
                                ></image>
                            </view>
                        </div>
                        <div class="storeInfo">
                            <div class="infoName">
                                <div
                                    @tap="_navigateTo2('../myStore/storeSetup')"
                                >
                                    {{ info.name }}
                                </div>
                                <div class="btnDiv">
                                    <div class="QRcode" @tap="toMessage">
                                        <img :src="myStore_news" alt="" />
                                        <view
                                            class="xxnum"
                                            :style="{
                                                padding:
                                                    noread > 9 ? '0 6rpx' : '',
                                            }"
                                            v-if="noread > 0"
                                        >
                                            {{ noread > 99 ? "99+" : noread }}
                                        </view>
                                    </div>
                                    <div
                                        @tap="showShareMask(shop_id)"
                                        class="QRcode"
                                    >
                                        <img :src="mch_fxx" alt="" />
                                    </div>
                                    <div
                                        v-if="mch_status == 1"
                                        @tap="QRcode"
                                        class="QRcode"
                                    >
                                        <img :src="mystore_qr" alt="" />
                                    </div>
                                </div>
                            </div>
                            <p
                                class="infoShop"
                                @tap="_navigateTo2('../myStore/storeSetup')"
                            >
                                {{
                                    info.shop_information == ""
                                        ? language.myStore.mchGood 
                                        : info.shop_information
                                }}
                            </p>
                        </div>
                    </div>
                    <div class="myStoreDataDiv">
                        <view class="myStoreDataDiv-title">{{
                            language.myStore.jysj
                        }}</view>
                        <!-- 今日订单 -->
                        <div
                            @tap="_navigateTo('../myStore/myOrder?status=all')"
                        >
                            <p class="dataBlack">
                                {{ info.order_num ? info.order_num : "0" }}
                            </p>
                            <p class="dataGray">
                                {{ language.myStore.todayOrder }}
                            </p>
                        </div>
                        <!-- 待发货订单 -->
                        <div
                            @tap="_navigateTo('../myStore/myOrder?status=dfh')"
                        >
                            <p class="dataBlack">
                                {{ info.order_num1 ? info.order_num1 : "0" }}
                            </p>
                            <p class="dataGray">
                                {{ language.myStore.noDeliveryOrder }}
                            </p>
                        </div>
                        <!-- 售后订单 -->
                        <div @tap="_navigateTo('../myStore/myOrder?status=sh')">
                            <p class="dataBlack">
                                {{ info.order_num2 ? info.order_num2 : "0" }}
                            </p>
                            <p class="dataGray">
                                {{ language.myStore.afterSales }}
                            </p>
                        </div>
                        <div
                            style="margin-top: 12rpx"
                            @tap="_navigateTo('../myStore/myCli')"
                        >
                            <p class="dataBlack">
                                {{ info.visitor_num ? info.visitor_num : "0" }}
                            </p>
                            <!-- 访客数 -->
                            <p class="dataGray">
                                {{ language.myStore.visitors }}
                            </p>
                        </div>
                        <div
                            style="margin-top: 12rpx"
                            @tap="_navigateTo('../myStore/myOrder?status=all')"
                        >
                            <p class="dataBlack">
                                {{ info.income != "0.00" ? info.income : "0" }}
                            </p>
                            <!-- 今日新增收入 -->
                            <p class="dataGray">
                                {{ language.myStore.newIncome }}
                            </p>
                        </div>
                        <div
                            style="margin-top: 12rpx"
                            @tap="
                                _navigateTo(
                                    '../myStore/myPro?up1=' +
                                        _up1 +
                                        '&up2=' +
                                        _up2
                                )
                            "
                        >
                            <p class="dataBlack">
                                {{
                                    info.goodsUpNum != "0.00"
                                        ? info.goodsUpNum
                                        : "0"
                                }}
                            </p>
                            <!-- 上架商品数 -->
                            <p class="dataGray">
                                {{ language.myStore.shelvesNum }}
                            </p>
                        </div>
                    </div>

                    <view class="myStoreSetupDiva">
                        <!-- 管理商品信息 -->
                        <div
                            @tap="
                                _navigateTo(
                                    '../myStore/myPro?up1=' +
                                        _up1 +
                                        '&up2=' +
                                        _up2
                                )
                            "
                        >
                            <img :src="wdsp" alt="" />
                            <view class="setupBlacka">
                                <view class="setupBlacka-title">{{
                                    language.myStore.myPro
                                }}</view>
                                <view class="setupBlacka-titlel">{{
                                    language.myStore.glspxx
                                }}</view>
                            </view>
                        </div>
                        <!-- 管理订单信息 -->
                        <div @tap="_navigateTo('../myStore/myOrder')">
                            <img :src="wddd" alt="" />
                            <view class="setupBlacka">
                                <view class="setupBlacka-title">{{
                                    language.myStore.myOrder
                                }}</view>
                                <view class="setupBlacka-titlel">{{
                                    language.myStore.glddxx
                                }}</view>
                            </view>
                        </div>
                        <!-- 发布商品 -->
                        <div
                            @tap="handleRelease('../myStore/uploadPro')"
                            v-if="_up2"
                        >
                            <img :src="fbsp" alt="" />
                            <view class="setupBlacka">
                                <view class="setupBlacka-title">{{
                                    language.myStore.shelves
                                }}</view>
                                <view class="setupBlacka-titlel">{{
                                    language.myStore.fbxsp
                                }}</view>
                            </view>
                        </div>
                        <!-- 账户资金明细 -->
                        <div @tap="_navigateTo('../myStore/myCha')">
                            <img :src="zjgl" alt="" />
                            <view class="setupBlacka">
                                <view class="setupBlacka-title">{{
                                    language.myStore.withdrawal
                                }}</view>
                                <view class="setupBlacka-titlel">{{
                                    language.myStore.zhzjmx
                                }}</view>
                            </view>
                        </div>
                    </view>
                    <!-- 营销工具 -->
                    <view class="tool">
                        <view class="tool-title">{{language.myStore.yxgj}} </view>
                                <view class="toollist-box-txt">{{
                                    language.myStore.yhq
                                }}</view>
                            </view>
                            <!-- 竞拍 -->
                            <view
                                class="toollist-box"
                                v-if="hidden"
                            >
                                <image
                                    :src="wdpm"
                                    style="width: 72rpx; height: 72rpx"
                                    class="toollist-box-img"
                                ></image>
                                <view class="toollist-box-txt">{{
                                    language.myStore.jp
                                }}</view>
                            </view>
                            <!-- 报名活动 -->
                            <view
                                class="toollist-box"
                                v-if="hidden"
                            >
                                <image
                                    :src="bmhd"
                                    style="width: 72rpx; height: 72rpx"
                                    class="toollist-box-img"
                                ></image>
                                <view class="toollist-box-txt">{{
                                    language.myStore.bmhd
                                }}</view>
                            </view>
                            <!-- 积分商城 -->
                            <view
                                class="toollist-box"
                                @tap="
                                    _navigateTo(
                                    )
                                "
                            >
                                <image
                                    :src="jfsc"
                                    style="width: 72rpx; height: 72rpx"
                                    class="toollist-box-img"
                                ></image>
                                <view class="toollist-box-txt">{{
                                    language.myStore.jfsc
                                }}</view>
                            </view>
                            <!-- 供应商 -->
                            <view
                                class="toollist-box" 
                            >
                                <image
                                    :src="ptcj"
                                    style="width: 72rpx; height: 72rpx"
                                    class="toollist-box-img"
                                ></image>
                                <view class="toollist-box-txt">{{
                                  language.myStore.supplier
                                }}</view>
                            </view>
                            <!-- 拼团 -->
                            <view
                                class="toollist-box"
                                @tap="
                                    _navigateTo(
                                    )
                                "
                            >
                                <image
                                    :src="ptcj"
                                    style="width: 72rpx; height: 72rpx"
                                    class="toollist-box-img"
                                ></image>
                                <view class="toollist-box-txt">{{
                                    language.myStore.ptgl
                                }}</view>
                            </view>
                            <!-- 预售 -->
                            <view
                                class="toollist-box"
                                @tap="
                                    _navigateTo(
                                    )
                                "
                            >
                                <image
                                    :src="ysgl"
                                    style="width: 72rpx; height: 72rpx"
                                    class="toollist-box-img"
                                ></image>
                                <view class="toollist-box-txt">{{
                                    language.myStore.ysgl
                                }}</view>
                            </view>
                            <!-- 直播 -->
                            <!-- #ifdef H5 -->
                            <view
                                class="toollist-box"
                                @tap="
                                    _navigateTo(
                                    )
                                "
                            >
                                <image
                                    :src="zbcj"
                                    style="width: 72rpx; height: 72rpx"
                                    class="toollist-box-img"
                                ></image>
                                <view class="toollist-box-txt">{{
                                    language.myStore.zbgl
                                }}</view>
                            </view>
                            <!-- #endif -->
                             <!-- 限时折扣 -->
                             <view
                                class="toollist-box"
                                @tap="
                                    _navigateTo(
                                    )
                                "
                            >
                                <image
                                    :src="xszk"
                                    style="width: 72rpx; height: 72rpx"
                                    class="toollist-box-img"
                                ></image>
                               <view class="toollist-box-txt"> {{
                                   language.myStore.xszk
                               }}</view>
                            </view>
                        </view>
                    </view>

                    <!-- 其他工具 -->
                    <view class="tool">
                        <view class="tool-title">{{
                            language.myStore.qtgj
                        }}</view>
                        <view class="toollist">
                            <!-- 自选商品 -->
                            <view
                                v-if="isZx"
                                class="toollist-box"
                                @tap="
                                    _navigateTo4(
                                        '/pagesA/myStore/choose_shopping?up1=' +
                                            _up1 +
                                            '&up2=' +
                                            _up2
                                    )
                                "
                            >
                                <image
                                    :src="zxsp"
                                    style="width: 72rpx; height: 72rpx"
                                    class="toollist-box-img"
                                ></image>
                                <view class="toollist-box-txt">{{
                                    language.myStore.zxsp
                                }}</view>
                            </view> 
                            <!-- 订单结算 -->
                            <view
                                class="toollist-box"
                                @tap="_navigateTo('/pagesC/myStore/OrderSet')"
                            >
                                <image
                                    :src="ddjs"
                                    style="width: 72rpx; height: 72rpx"
                                    class="toollist-box-img"
                                ></image>
                                <view class="toollist-box-txt">{{
                                    language.myStore.ddjs
                                }}</view>
                            </view>
                            <!-- 分账记录 -->
                            <view
                                class="toollist-box"
                                @tap="
                                    _navigateTo('/pagesB/myStore/ledgerRecord')
                                "
                            >
                                <image
                                    :src="fzjl"
                                    style="width: 72rpx; height: 72rpx"
                                    class="toollist-box-img"
                                ></image>
                                <view class="toollist-box-txt">{{
                                    language.myStore.fzjl
                                }}</view>
                            </view>
                            <!-- 订单设置 -->
                            <view
                                class="toollist-box"
                                @tap="_navigateTo('/pagesC/bond/setOrder')"
                            >
                                <image
                                    :src="ddsz"
                                    style="width: 72rpx; height: 72rpx"
                                    class="toollist-box-img"
                                ></image>
                                <view class="toollist-box-txt">{{
                                    language.myStore.ddsz
                                }}</view>
                            </view>
                            <!-- 电子面单 -->
                            <view
                                class="toollist-box"
                                @tap="_navigateTo('/pagesB/myStore/express')"
                            >
                                <image
                                    :src="dzmd"
                                    style="width: 72rpx; height: 72rpx"
                                    class="toollist-box-img"
                                ></image>
                                <view class="toollist-box-txt">{{
                                    language.myStore.dzmd
                                }}</view>
                            </view>
                            <!-- 粉丝 -->
                            <view
                                class="toollist-box"
                                @tap="_navigateTo('../myStore/myCli')"
                            >
                                <image
                                    :src="fs"
                                    style="width: 72rpx; height: 72rpx"
                                    class="toollist-box-img"
                                ></image>
                                <view class="toollist-box-txt">{{
                                    language.myStore.fs
                                }}</view>
                            </view>
                            <!-- 保证金管理 -->
                            <view
                                class="toollist-box"
                                @tap="_navigateTo3('/pagesC/bond/index')"
                            >
                                <image
                                    :src="bzjgl"
                                    style="width: 72rpx; height: 72rpx"
                                    class="toollist-box-img"
                                >
                                </image>
                                <view class="toollist-box-txt">{{
                                    language.myStore.bzjgl
                                }}</view>
                            </view>
                            <!-- 运费模板 -->
                            <view
                                class="toollist-box"
                                @tap="_navigateTo('../myStore/freight')"
                            >
                                <image
                                    :src="yfmb"
                                    style="width: 72rpx; height: 72rpx"
                                    class="toollist-box-img"
                                ></image>
                                <view class="toollist-box-txt">{{
                                    language.myStore.yfmb
                                }}</view>
                            </view>
                            <!-- 收银买单 -->
                            <view
                                class="toollist-box"
                                @tap="_navigateTo('/pagesC/myStore/cashier')"
                            >
                                <image
                                    :src="symd"
                                    style="width: 72rpx; height: 72rpx"
                                    class="toollist-box-img"
                                ></image>
                                <view class="toollist-box-txt">{{
                                    language.myStore.symd
                                }}</view>
                            </view>
                            <!-- 门店管理 -->
                            <view
                                class="toollist-box"
                                @tap="
                                    _navigateTo(
                                        '../myStore/storeList?status_class=1&shop_id=' +
                                            shop_id
                                    )
                                "
                            >
                                <image
                                    :src="mdgl"
                                    style="width: 72rpx; height: 72rpx"
                                    class="toollist-box-img"
                                ></image>
                                <view class="toollist-box-txt">{{
                                    language.myStore.mdgl
                                }}</view>
                            </view>
                            <!-- 银行卡管理 -->
                            <view
                                class="toollist-box"
                                @tap="
                                    _navigateTo(
                                        '/pagesB/myWallet/bankCard?type=store'
                                    )
                                "
                            >
                                <image
                                    :src="yhk"
                                    style="width: 72rpx; height: 72rpx"
                                    class="toollist-box-img"
                                ></image>
                                <view class="toollist-box-txt">{{
                                    language.myStore.yhkgl
                                }}</view>
                            </view>
                            <!--  发票管理 -->
                            <view
                                class="toollist-box"
                                @tap="
                                    _navigateTo(
                                        '/pagesB/invoice/invoiceManagementStore'
                                    )
                                "
                                v-if="isInvoice"
                            >
                                <image
                                    :src="fpgl"
                                    style="width: 72rpx; height: 72rpx"
                                    class="toollist-box-img"
                                ></image>
                                <view class="toollist-box-txt">{{
                                    language.myStore.fpgl
                                }}</view>
                            </view>
                            <!-- 店铺设置 -->
                            <view
                                class="toollist-box"
                                @tap="_navigateTo2('../myStore/storeSetup')"
                            >
                                <image
                                    :src="dpsz"
                                    style="width: 72rpx; height: 72rpx"
                                    class="toollist-box-img"
                                ></image>
                                <view class="toollist-box-txt">{{
                                    language.myStore.dpsz
                                }}</view>
                            </view>
                            <!-- 店铺主页 -->
                            <view class="toollist-box" @tap="_goStore()">
                                <image
                                    :src="mch_zy"
                                    style="width: 72rpx; height: 72rpx"
                                    class="toollist-box-img"
                                >
                                </image>
                                <view class="toollist-box-txt">{{
                                    language.myStore.dpzy
                                }}</view>
                            </view>
                            <!-- 客服 -->
                            <view
                                class="toollist-box"
                                @click="nato('kefu')"
                                style="position: relative"
                            >
                                <image
                                    :src="dianpu_kefu"
                                    style="width: 72rpx; height: 72rpx"
                                    class="toollist-box-img"
                                >
                                </image>
                                <view class="toollist-box-txt">{{
                                    language.myStore.wdkf
                                }}</view>
                                <span
                                    v-if="mchOnlineMessageNotRead"
                                    class="my-kefu-red"
                                ></span>
                            </view>
                        </view>
                    </view>
                    <div class="support">
                        <div>~ {{ language.myStore.wsydx }} ~</div>
                    </div>
                </div>
            </div>
            <div class="relative" v-else-if="haveStore == 0 && is_lock == 0">
                <div class="myStoreDiv">
                    <div class="myStoreImgDiv">
                        <img class="img" :src="noStore" />
                    </div>
                    <span class="noFindText1">{{
                        language.myStore.noStore
                    }}</span>
                    <!-- 申请开店 -->
                    <div class="toApplyDiv" @tap="_toApply()">
                        <span class="toApply">{{
                            language.myStore.apply
                        }}</span>
                    </div>
                </div>
            </div>
            <!-- 审核未通过 -->
            <div class="checkDiv1" v-else-if="haveStore == 3 && is_lock == 0">
                <div class="noFindDiv failDiv">
                    <div><img class="noFindImg" :src="checkFalse" /></div>
                    <div class="checkDivData">
                        {{ language.myStore.Unapprove }}
                    </div>
                </div>
                <div class="noFindDiv_fail">
                    <div class="noFindText1 font_24">
                        {{ language.myStore.reason }}
                    </div>
                    <div class="reasonDiv">{{ reason }}</div>
                </div>
                <!-- 审核未通过按钮 -->
                <div class="btn">
                    <div class="bottom" @tap="_toApply1">
                        {{ language.myStore.toApply }}
                    </div>
                </div>
            </div>
            <div class="checkDiv1" v-else-if="is_lock == 1">
                <div class="noFindDiv failDiv">
                    <div><img class="noFindImg" :src="checkFalse" /></div>
                    <div class="checkDivData">{{ language.myStore.dpyzx }}</div>
                    <div class="font_two">{{ language.myStore.yzdzx }}</div>
                </div>
                <div class="toApplyDiv2" @tap="_toApply()">
                    <span class="toApply3">{{ language.myStore.sqdp }}</span>
                </div>
            </div>
        </div>

        <div class="load" v-else>
            <div>
                <img :src="loadGif" />
                <p>{{ language.toload.load }}</p>
            </div>
        </div>
        <share
            ref="share"
            :share="share"
            :pro="pro"
            :pro_id="'1'"
            :type="shareType"
        ></share>
        <!-- 物流模板弹窗 -->
        <view class="mask" v-if="is_freight">
            <view class="delMask">
                <p>{{ language.storeCoupon.Tips }}</p>

                <text>{{ language.storeCoupon.wlmubantishi }}</text>
                <view>
                    <view @tap="is_freight = false">{{
                        language.storeCoupon.cancel
                    }}</view>
                    <view style="color: #d73b48" @tap="handleOk">{{
                        language.storeCoupon.confirm
                    }}</view>
                </view>
            </view>
        </view>
    </div>
</template>

<script>
import share from "@/components/share.vue";
export default {
    data() {
        return {
            load: true,
            limitsOfAuthority:[],//部分按钮权限显示
            loadGif:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/5-121204193R7.gif",

            title: "我的店铺",
            fastTap: true,
            reason: "",
            noStore:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/dizhiNo.png",
            checkImg:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/applySuc_apply.png",
            haveStore: 3,
            pa_flag: 0,
            wdsp:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/auction/wdsp.png",
            ptcj:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/auction/ptcj.png",
            hyj:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/auction/yhj.png",
            wddd:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/auction/wddd.png",
            fbsp:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/auction/fbsp.png",
            zjgl:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/auction/zjgl.png",
            yhk:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/auction/yhk.png",
            bzjgl:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/auction/bzjgl.png",
            ddjs:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/auction/ddjs.png",
            fzjl:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/auction/fzjl.png",
            dpsz:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/auction/dpsz.png",
            bmhd:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/auction/bmhd.png",
            mdgl:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/auction/mdgl.png",
            symd:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/auction/symd.png",
            fs:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/auction/fs.png",
            wdpm:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/auction/wdpm.png",
            ddsz:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/auction/ddsz.png",
            dzmd:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/auction/dzmd.png",
            fpgl:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/auction/store_invoice_icon.png",
            yfmb:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/auction/yfmb.png",
            zxsp:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/auction/zxsp.png",
            fpgl:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/auction/fpgl.png",
            myCli:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/myCli.png",
            livePlayImg:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/hhhh2x.png",
            storeLogo:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/storeLogo.png",
            myPro:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/myPro.png",
            ULPro:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/ULPro.png",
            myOrder:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/myOrder.png",
            storeSet:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/storeSet.png",
            storeSite:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/storeSite.png",
            mch_code:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/mch/mch_code.png",
            checkFalse:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/myStore_applyFail.png",
            myCha:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/myCha.png",
            myCard:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/yhkgl.png",
            mystore_skill:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/mchSeckill.png",
            mystore_activity:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/mchActivity.png",
            mystore_group:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/mchGroup.png",
            myzx:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/commodity_up2x.png",
            mch_coupon_i:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/mch_coupon_i.png",
            mch_freight:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/mch_freight.png",
            mystore_qr:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/mystore_qr.png",
            myStore_news:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/myStore_news.png",
            ico_bond:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/ico_bond.png",
            jfsc:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/jfsc.png",
            ysgl:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/ysgl.png",
            zbcj:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/zbcj.png",
            xszk:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/xszkdp.png",
            mch_fxx:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/mch_fxx.png",
            mch_zy:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/mch_zy.png",
            dianpu_kefu:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/dianpu_kefu.png",
            bgColor: [
                {
                    item: "#ffffff",
                },
                {
                    item: "#ffffff",
                },
            ],
            info: "",
            returnR: 7,
            livePlayUrl:
                "plugin-private://wx2b03c6e691cd7370/pages/live-player-plugin?room_id=1",

            style:
                "background-image: url(" +
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "/images/icon1/mystore_bg.png);background-size: 100% auto;background-repeat: no-repeat;",

            _up1: "",
            _up2: "",
            auto_examine: 3, //入驻申请审核时间
            mch_status: 0, //是否有门店；1有门店
            coupon_status: false, // 是否开启了优惠券插件
            zx: false,
            is_margin: false, //是否缴纳保证金
            isPromiseSwitch: false, //是否开启保证金管理
            isPromiseExamine: true, //是否处于保证金退还审核状态(true可以操作)
            hidden: "",
            go_group: false,
            intergral: false,
            isZx: false,
            is_lock: 0, //0是未注销 1是已注销
            shop_id: "",
            freightList: [],
            is_freight: false,
            noread: 0, //店铺消息数量
            presell: "", //是否显示预售管理
            mch_is_open:false,
            isInvoice: true, //是否显示发票管理
            lkt_type: "", //接口后台类
            shareType: "",
            mchOnlineMessageNotRead: 0, //店铺是否有客服未读消息 -》 0没有 >0有
            gesTimes: "", //定时器
            mchMessage: true, //是否发送监听请求
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
            pro: {},
            is_Payment: true,
        };
    },
    onLoad() {
        this.lkt_type = this.LaiKeTuiCommon.LKT_TYPE
            ? this.LaiKeTuiCommon.LKT_TYPE
            : "JAVA";
    },
    onShow() {
        this.isLogin(() => {
            this._axios().then(() => {
                this.handleGetFreight();
            });
            this.hindee();
            //先初始化清除
            clearInterval(this.gesTimes);
            this.gesTimes = "";
            this.gesTimes = setInterval(() => {
                this.getmchOnlineMessageNotRead();
            }, 2000);
        });
    },
    onHide() {
        clearInterval(this.gesTimes);
        this.gesTimes = "";
    },
    //微信小程序好友分享
    onShareAppMessage: function (res) {
        //隐藏分享弹窗
        this.$refs.share._closeAllMask();
        let shareTitle = ""; //分享标题
        let shareImageUrl = ""; //分享图片
        if (this.share.mch.type) {
            //店铺分享
            shareTitle = this.info.shop_name;
            shareImageUrl = this.info.posterImg;
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
    components: {
        share,
    },
    methods: {
        //客服是否存在未读消息
        getmchOnlineMessageNotRead() {
            // tsog 测试场景使用 为了好找到接口 没有实际业务场景
            if(uni.getStorageInfoSync('tsog')){
                return
            }
            if (!this.mchMessage) {
            }
            if(uni.getStorageSync('stop')){
                clearInterval(this.gesTime)
                this.gesTime = ''
                return
            }
            let data = {
                api: "app.message.messageNotReade",
            };
            this.mchMessage = false;
            this.$req
                .post({
                    data,
                })
                .then((res) => {
                    this.mchMessage = true;
                    if (res.code == 200) {
                        this.mchOnlineMessageNotRead =
                            res.data.mchOnlineMessageNotRead;
                    } else {
                        clearInterval(this.gesTimes);
                        this.gesTimes = "";
                    }
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
            let plugIn = "dp";
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
        // 当物流模板为空
        handleOk() {
            uni.navigateTo({
                url: "/pagesA/myStore/freight_add?type=add",
            });
            this.is_freight = false;
        },
        // 发布商品的前提校验
        handleRelease(url) {
            if (this.is_Payment) {
                // 保证金是否处于退还审核
                if (this.isPromiseExamine) {
                    // if (true) {
                    if (this.freightList.length <= 0) {
                        this.is_freight = true;
                    } else {
                        uni.navigateTo({
                            url: url,
                        });
                    }
                } else {
                    uni.showModal({
                        title: this.language.myStore.bzjth_title,
                        content: this.language.myStore.bzjth_content,
                        confirmColor: "#D73B48",
                        confirmText: this.language.showModal.confirm,
                        showCancel: false,
                        success: (e) => {
                            return;
                        },
                    });
                }
            } else {
                // 未缴纳保证金
                uni.showModal({
                    title: this.language.myStore.bzj_title,
                    content: this.language.myStore.bzj_content,
                    confirmColor: "#D73B48",
                    confirmText: this.language.showModal.confirm,
                    cancelText: this.language.showModal.cancel,
                    success: (e) => {
                        if (e.confirm) {
                            uni.navigateTo({
                                url: "/pagesC/bond/payText",
                            });
                        } else {
                            return;
                        }
                    },
                });
            }
        },
        // 获取物流模板信息
        handleGetFreight() {
            if (this.shop_id == "" || this.shop_id == undefined) {
                return;
            }
            let data = {
                api: "mch.App.Mch.Upload_merchandise_page",

                shop_id: this.shop_id,
            };
            this.$req
                .post({
                    data,
                })
                .then((res) => {
                    let { code, data, message } = res;
                    if (code == 200) {
                        this.freightList = data.freight_list;
                    } else {
                        uni.showToast({
                            title: message,
                            duration: 1500,
                            icon: "none",
                        });
                    }
                });
        },
        nato(item) {
            if (item == "yushou") {
                uni.navigateTo({
                    url: "/pagesD/synthesize/Integral?type=yushou",
                });
            } else if (item == "jifen") {
                uni.navigateTo({
                    url: "/pagesD/synthesize/Integral?type=jifen",
                });
            } else if (item == "kefu") {
                uni.navigateTo({
                    url: "/pagesB/message/buyers_service/buyers_service",
                });
            }
        },
        hindee() {
            this.$req
                .post({
                    data: {
                        api: "app.plugins.getPluginAll",
                    },
                })
                .then((res) => {
                    setTimeout(() => {
                        this.load = false;
                    }, 200);
                    if (res.code == 200) {
                        this.limitsOfAuthority = res.data
                         
                        this.hidden = res.data.auction;
                        this.go_group = res.data.go_group;
                        this.intergral = res.data.integral;
                        this.coupon_status = res.data.coupon;
                        this.presell = res.data.presell;
                        if(res.data.living&&res.data.mch_is_open==1){
                            this.mch_is_open = true
                        }
                        // 发票的
                        // this.isInvoice = res.data.
                    }
                });
        },
        changeLoginStatus() {
            this._axios();
        },
        _goStore() {
            uni.navigateTo({
                url: "/pagesB/store/store?shop_id=" + this.shop_id,
            });
        },
        _navigateTo2(url) {
            uni.navigateTo({
                url,
            });
        },
        _navigateTo3(url) {
            if (this.is_Payment) {
                // 保证金是否处于退还审核
                if (this.isPromiseExamine) {
                    uni.navigateTo({
                        url,
                    });
                } else {
                    uni.showModal({
                        title: this.language.myStore.bzjth_title,
                        content: this.language.myStore.bzjth_content,
                        confirmColor: "#D73B48",
                        confirmText: this.language.showModal.confirm,
                        showCancel: false,
                        success: (e) => {
                            return;
                        },
                    });
                }
            } else {
                if (this.isPromiseSwitch) {
                    uni.showModal({
                        title: this.language.myStore.bzj_title,
                        content: this.language.myStore.bzj_content,
                        confirmColor: "#D73B48",
                        confirmText: this.language.showModal.confirm,
                        cancelText: this.language.showModal.cancel,
                        success: (e) => {
                            if (e.confirm) {
                                uni.navigateTo({
                                    url: "/pagesC/bond/payText",
                                });
                            } else {
                                return;
                            }
                        },
                    });
                } else {
                    uni.navigateTo({
                        url: "/pagesC/bond/payText",
                    });
                }
            }
        },
        _navigateTo4(url) {
            if (this.is_Payment) {
                // 保证金是否处于退还审核
                if (this.isPromiseExamine) {
                    uni.navigateTo({
                        url,
                    });
                } else {
                    uni.showModal({
                        title: this.language.myStore.bzjth_title,
                        content: this.language.myStore.bzjth_content,
                        confirmColor: "#D73B48",
                        confirmText: this.language.showModal.confirm,
                        showCancel: false,
                        success: (e) => {
                            return;
                        },
                    });
                }
            } else {
                uni.showModal({
                    title: this.language.myStore.bzj_title,
                    content: this.language.myStore.bzj_content,
                    confirmColor: "#D73B48",
                    confirmText: this.language.showModal.confirm,
                    cancelText: this.language.showModal.cancel,
                    success: (e) => {
                        if (e.confirm) {
                            uni.navigateTo({
                                url: "/pagesC/bond/payText",
                            });
                        } else {
                            return;
                        }
                    },
                });
            }
        },
        _navigateTo(url) {
            // 是否缴纳保证金
            uni.navigateTo({
                url,
            });
            return;
            // if (this.is_margin || !this.isPromiseSwitch) {
            //     // 保证金是否处于退还审核
            //     if (this.isPromiseExamine) {
            //             uni.navigateTo({
            //                 url,
            //             });
            //     } else {
            //         uni.showModal({
            //             title: this.language.myStore.bzjth_title,
            //             content: this.language.myStore.bzjth_content,
            //             confirmColor: "#D73B48",
            //             confirmText: this.language.showModal.confirm,
            //             showCancel: false,
            //             success: (e) => {
            //                 return;
            //             },
            //         });
            //     }
            // } else {
            //         if(this.isPromiseSwitch){
            //             uni.showModal({
            //                 title: this.language.myStore.bzj_title,
            //                 content: this.language.myStore.bzj_content,
            //                 confirmColor: "#D73B48",
            //                 confirmText: this.language.showModal.confirm,
            //                 cancelText: this.language.showModal.cancel,
            //                 success: (e) => {
            //                     if (e.confirm) {
            //                         uni.navigateTo({
            //                             url: "/pagesC/bond/payText",
            //                         });
            //                     } else {
            //                         return;
            //                     }
            //                 },
            //             });
            //         }
            // }
        },
        _toApply() {
            uni.navigateTo({
                url: "../myStore/applyStore",
            });
        },
        _toApply1() {
            uni.navigateTo({
                url: "../myStore/applyStore?goOn=true",
            });
        },
        f1(p1, p2) {
            for (var p in p2) {
                if (p2[p] == p1) {
                    return true;
                }
            }
            return false;
        },

        async _axios() {
            this.livePlayUrl =
                "plugin-private://wx2b03c6e691cd7370/pages/live-player-plugin?room_id=1";

            await this.$req
                .post({
                    data: {
                        api: "mch.App.Mch.Index",
                    },
                })
                .then((res) => {
                    setTimeout(() => {
                        this.load = false;
                    }, 200);
                    if (res.code == 200) {
                        this.auto_examine = res.data.auto_examine; //入驻申请审核时间
                        this.mch_status = res.data.mch_data.mch_status;
                        this.is_Payment = res.data.is_Payment;
                        this.commodity_setup =
                            res.data.mch_data.commodity_setup;
                        this.pa_flag = res.data.pa_flag;
                        this._up1 = this.f1(2, this.commodity_setup);
                        this._up2 = this.f1(1, this.commodity_setup);
                        this.zx = res.data.zx;
                        this.is_margin = res.data.mch_data.isPromisePay;
                        this.isPromiseSwitch =
                            res.data.mch_data.isPromiseSwitch;
                        this.isPromiseExamine =
                            res.data.mch_data.isPromiseExamine;
                        uni.setStorageSync("_up1", this._up1);
                        uni.setStorageSync("_up2", this._up2);
                        this.noread = res.data.noread;
                        this.isZx = res.data.zx;
                        this.$store.commit('SET_ISFB',res.data.isFb || false) ; 
                        this.haveStore = res.data.status;
                        //直播间房号
                        let roomid = 1;
                        if (res.data.mch_data.roomid) {
                            roomid = res.data.mch_data.roomid;
                        }
                        this.reason = res.data.mch_data.review_result;
                        this.info = res.data.mch_data;
                        if (roomid == 0) {
                            roomid = 1;
                        }
                        this.livePlayUrl =
                            "plugin-private://wx2b03c6e691cd7370/pages/live-player-plugin?room_id=";
                        this.livePlayUrl = this.livePlayUrl + roomid;
                        uni.setStorageSync("mch_id", res.data.mch_data.shop_id);
                        uni.setStorage({
                            key: "shop_id",
                            data: res.data.mch_data.shop_id,
                        });
                        this.shop_id = res.data.mch_data.shop_id;
                        this.$store.state.shop_id = res.data.mch_data.shop_id;
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: "none",
                        });
                    }
                    if (res.code == 50080) {
                        this.is_lock = 1;
                    }
                });
        },
        // 店铺消息页
        toMessage() {
            // 这块样式与个人中心一样,只是数据展示不一样(接口不一样)
            this._navigateTo("/pagesA/myStore/myStoreMessage/storeMesage");
        },
        // 扫码核销
        QRcode() {
            this._navigateTo("../myStore/QRcode");
        },
    },
};
</script>
<style>
page {
    background-color: #f4f5f6;
}
</style>
<style scoped lang="less">
@import url("@/laike.less");
@import url("../../static/css/myStore/myStore.less");

.my-kefu-red {
    width: 16rpx;
    height: 16rpx;
    background-color: red;
    border-radius: 50%;
    position: absolute;
    top: 0;
    margin-left: 50rpx;
}

.delMask {
    position: absolute;
    left: 50%;
    transform: translateX(-50%);
    width: 640rpx;
    height: auto;
    /* #ifdef APP-PLUS */
    height: 388rpx;
    /* #endif */
    background: rgba(255, 255, 255, 1);
    border-radius: 23rpx;
}

.delMask > p {
    text-align: center;
    padding: 64rpx 48rpx 0 48rpx;
    justify-content: center;
}

.delMask > text {
    display: block;
    text-align: center;
    color: #999;
    font-size: 32rpx;
    line-height: 48rpx;
    padding: 24rpx 48rpx 64rpx 48rpx;
}

.delMask > view {
    display: flex;
}

.delMask > view view {
    flex: 1;
    height: 93rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #333;
    font-size: 34rpx;
    box-sizing: border-box;
    border-top: 1px solid #eeeeee;
}

.delMask > view view:last-child {
    border-left: 1px solid #eeeeee;
    color: #020202;
}
</style>
