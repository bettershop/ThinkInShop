<template>
    <div class="container">
        <heads
            :title="language.storeMyCha.title"
            :bgColor="bgColor"
            titleColor="#333333"
            ishead_w="2"
        ></heads>

        <view class="con">
            <view class="top">
                <view
                    class="top_price"
                    :style="{
                        background: 'url(' + account_bg + ') 0% 0% /100%',
                    }"
                >
                    <view class="top_price_account">
                        <view class="Nth">
                            {{store_default_currency_symbol}}{{ cashAmt ? cashAmt : 0 }}
                        </view>
                        <view class="name">{{
                            language.storeMyCha.money
                        }}</view>
                    </view>
                    <view class="top_price_money">
                        <view class="Nth">
                            {{store_default_currency_symbol}}{{ account_money ? account_money : 0 }}
                        </view>
                        <view class="name">{{
                            language.storeMyCha.yjdzje
                        }}</view>
                    </view>
                </view>

                <view class="btns">
                    <view class="btns_left" @tap="_toDetail()">{{
                        language.storeMyCha.jymx
                    }}</view>
                    <view class="btns_right" @tap="_toCha()">{{
                        language.storeMyCha.ljtx
                    }}</view>
                </view>
            </view>

            <view class="bottom">
                <view class="explain">{{
                    language.storeMyCha.instructions
                }}</view>
                <div><rich-text :nodes="illustrate"></rich-text></div>
            </view>
        </view>
       
        <showToast  :is_showToast="is_showToast" 
            :is_showToast_obj="is_showToast_obj" @richText="handleConfirm" @confirm="handleConfirm"> </showToast>
    </div>
</template>

<script>
import htmlParser from "@/common/html-parser.js";
import showToast from "@/components/aComponents/showToast.vue"

export default {
    data() {
        return {
            store_default_currency_code:'CNY',
            store_default_currency_symbol:'￥',
            store_default_exchange_rate:1,
            title: "我的提现",
            is_showToast: 0, //0隐藏弹窗  1成功提示弹窗
            is_showToast_obj: {}, //imgUrl提示图标  title提示文字
            load: false,
            mingxi:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/mingxi.png",
            tixian:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/tixian.png",
            jiantou:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/jiantou2x.png",
            shop_id: "",
            account_money: 0.0, //预计到帐金额
            cashAmt: 0.0, //账户金额
            all_money: 0.0,
            integral_money: 0,
            illustrate: "",

            account_bg:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/account_bg.png",
            bgColor: [
                {
                    item: "#ffffff",
                },
                {
                    item: "#ffffff",
                },
            ],
            isPromiseExamine: true,
            is_Payment: true,
        };
    },
    onShow() {
        this.shop_id = uni.getStorageSync("shop_id")
            ? uni.getStorageSync("shop_id")
            : this.$store.state.shop_id;
        this._axios();
        let storeCurrency = uni.getStorageSync('storeCurrency');
        this.store_default_exchange_rate = storeCurrency.exchange_rate;
        this.store_default_currency_symbol = storeCurrency.currency_symbol;
        this.store_default_currency_code = storeCurrency.currency_code;
    },
    components: {
        showToast,
    },
    methods: {
        _axios() {
            this.$req
                .post({
                    data: {
                       
                        api: "mch.App.Mch.My_wallet",
                        shop_id: this.shop_id,
                    },
                })
                .then((res) => {
                    this.load = true;
                    if (res.code == 200) {
                        this.isPromiseExamine = res.data.isPromiseExamine;
                        this.is_Payment = res.data.is_Payment;
                        this.account_money = res.data.account_money;
                        this.cashAmt = res.data.cashAmt;
                        this.integral_money = res.data.integral_money;
                        this.all_money = res.data.all_money;
                        this.illustrate = res.data.illustrate ? htmlParser(res.data.illustrate) :'';
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: "none",
                        });
                    }
                    this.load = true;
                });
        },
        _toCha() {
            if (this.is_Payment) {
                // 保证金是否处于退还审核
                if (this.isPromiseExamine) {
                   this.$req
                       .post({
                           data: {
                               api: "mch.App.Mch.My_wallet",
                               shop_id: this.shop_id,
                           },
                       })
                       .then((res) => {
                           if (res.code == 200 && res.data.PopUpContent == "") {
                               uni.navigateTo({
                                   url: "/pagesB/myWallet/putForward?type=store",
                               });
                           }else{
                               this.is_showToast =3
                               this.is_showToast_obj.title = res.data.PopUpContent
                               this.is_showToast_obj.button = '我知道了'
                           }
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
        handleConfirm(){
            this.is_showToast = 0
        },
        _toDetail() {
            uni.navigateTo({
                url: "./myFinance",
            });
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
@import url("../../static/css/myStore/myCha.less");
</style>
