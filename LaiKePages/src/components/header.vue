<template>
    <div
        :style="'display:flex;padding-top:88rpx;height:' + BoxHeight"
        class="headerBox"
        v-if="isHeaderReady"
    >
        <!-- #ifndef MP-ALIPAY -->
        <div
            class="head"
            :class="{
                head_w: ishead_w == '1',
                bgno: bgImg != '' && isAnd,
                border: border == true,
                noBg: isBg == true,
                bgc_bai: scrollTops > 225,
            }"
            :style="{
                background: bgImg
                    ? bgImg
                    : `linear-gradient(90deg ,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`,
            }"
        >
            <div
                :class="{ white: !navWhite && !bgImg }"
                :style="{
                    height: halfWidth,
                    background: bgImg
                        ? bgImg
                        : `linear-gradient(90deg ,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`,
                }"
            ></div>
            <div class="header"> 
                <div
                    class="header-left"
                    @tap="_back1"
                    v-if="flag && !returnFlag"
                >
                    <img v-if="ishead_w == '1'" :src="back1" />
                    <img
                        v-else-if="ishead_w == '2' || ishead_w == '14'"
                        :src="new_back"
                        class="new_back"
                    />
                    <img
                        v-else-if="ishead_w == '3'"
                        :src="new_back_white"
                        class="new_back"
                    />
                    <img
                        v-else-if="ishead_w == '99'"
                        :src="bg_white"
                        class="new_backs"
                    />
                    <view
                        v-else-if="ishead_w == '98'"
                        class="left_title"
                        @tap.stop="_manage"
                        >{{ manage_text }}</view
                    >
                    <img class="hhhhh" v-else :src="back" />
                </div>
                <img
                    :src="bback"
                    @tap="_back1"
                    v-if="!flag && !returnFlag"
                    class="header_img"
                />
                <!-- 标题 / Tabs（可横向滑动） -->
                <scroll-view
                    v-if="tabs && tabs.length"
                    class="header-tabs"
                    :class="{ 'header-tabs--center': centerTabs }"
                    scroll-x
                    :show-scrollbar="false"
                    :scroll-into-view="activeTabId"
                    scroll-with-animation
                >
                    <view
                        v-for="(tab, idx) in tabs"
                        :key="tab.key || idx"
                        class="header-tab"
                        :id="`lk-tab-${idx}`"
                        :class="{ active: idx === activeTabIndex }"
                        @tap.stop="onTabTap(tab, idx)"
                    >
                        {{ tab.name }}
                    </view>
                </scroll-view>
                <p
                    v-else
                    class="header-title"
                    :style="{ color: titleColor }"
                    :class="{ title_w: ishead_w == '1', 'header-title--center': centerTitle }"
                >
                    {{ title }}
                </p>
                <div
                    class="header-right"
                    :class="{ header_kj: is_kj }"
                    :style="{ 'margin-top': headerRightMarginTop }"
                    v-if="rightText"
                    @click="$emit('right-click')"
                >
                    <div class="content">
                        <img :src="rightIcon" v-if="rightIcon" alt="" />
                        <!-- <p>{{rightText}}</p> -->
                    </div>
                </div>
            </div>
        </div>
        <!-- #endif -->
    </div>
</template>

<script>
export default {
    data() {
        return {
            flag: true,
            h5Translated: true,
            h5ReadyTimer: null,
            bback:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/bback.png",
            back:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/back2x.png",
            back1:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/back2x_w.png", 
            new_back:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/new_back.png",
            new_back_white:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/new_back_white.png",
            bg_white:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/bg_white.png",
            scrollTops: 0, //滚动条距离
            aTopHeight: "", //动态计算高度
            //manage_text:'', 
        };
    },
    computed: {
        isHeaderReady() {
            return process.env.UNI_PLATFORM !== 'h5' || this.h5Translated;
        },
        activeTabId() {
            return `lk-tab-${this.activeTabIndex || 0}`;
        },
        BoxHeight() {
            var gru = uni.getStorageSync("data_height")
                ? uni.getStorageSync("data_height")
                : this.$store.state.data_height;
            // #ifdef MP-TOUTIAO
            // const info = uni.getSystemInfoSync()
            // if(info.platform != 'ios'){
            // 	gru=0
            // }
            // #endif
            let height = 0;
            // #ifndef MP-ALIPAY
            var heigh = parseInt(gru) + uni.upx2px(88);
            height = heigh && heigh > 0 ? heigh : uni.upx2px(88);
            // #endif

            // #ifdef MP
            heigh += 44;
            // #endif

            // #ifdef APP-PLUS
            heigh += 44;
            // #endif

            return height + "px";
        },
        headerRightMarginTop() {
            // #ifdef MP
            return 0;
            // #endif
            // #ifndef MP
            return this.halfWidth;
            // #endif
        },
    },
    // props: [
    //     'title', 'returnR', 'navWhite', 'returnFlag', 'border', 'ishead_w', 'rightText', 'rightIcon','bgColor'
    // ],
    props: {
        title: {},
        // 可选：在 header 显示可横滑 tab（例如商品详情页活动类型）
        tabs: {
            type: Array,
            default: () => [],
        },
        activeTabIndex: {
            type: Number,
            default: 0,
        },
        centerTabs: {
            type: Boolean,
            default: false,
        },
        centerTitle: {
            type: Boolean,
            default: false,
        },
        returnR: {
            type: [String, Number],
            default: "",
        },
        manage_text: {
            type: String,
            default: "",
        },
        isMs: {
            type: String,
            default: "",
        },
        types: {
            type: String,
            default: "",
        },
        navWhite: {},
        returnFlag: {
            type: Boolean,
            default: false,
        },
        border: {},
        ishead_w: {
            type: [String, Number],
            default: "",
        },
        rightText: {
            type: String,
            default: "",
        },
        is_kj: {
            type: Boolean,
            default: false,
        },
        rightIcon: {},
        // 导航栏以及头部样式（可为图片或者背景颜色）
        bgImg: {
            type: String,
            default: "",
        },
        live: {
            type: [Array, Object],
            default:  () => [],
        },
        bgColor: {
            type: [Array, Object],
            default: () => [
                {
                    item: "#Fa5151",
                },
                {
                    item: "#Fa5151",
                },
            ],
        },
        titleColor: {
            type: String,
            default: "",
        },

        order_type: {
            type: String,
            default: "",
        },
        order_id: {
            type: [String, Number],
            default: "",
        },

        shopping: {
            type: Boolean,
            default: false,
        },
        isAnd: {
            type: Boolean,
            default: false,
        },
        isBg: {
            type: Boolean,
            default: false,
        },
        url: {
            type: String,
            default: "",
        },
    },
    mounted() {
        // #ifdef H5
        this.h5Translated = !!(window.__LKT_TRANSLATED__ || this.$isTranslated);
        if (!this.h5Translated) {
            this._onTranslationDone = () => {
                this.h5Translated = true;
                if (this.h5ReadyTimer) {
                    clearTimeout(this.h5ReadyTimer);
                    this.h5ReadyTimer = null;
                }
            };
            window.addEventListener('translationDone', this._onTranslationDone);
            // Fallback for H5 refresh: avoid keeping header hidden if translation event is delayed.
            this.h5ReadyTimer = setTimeout(() => {
                this.h5Translated = true;
                this.h5ReadyTimer = null;
            }, 1200);
        }
        // #endif

        //获取顶部高度
        let headerHeight = uni.getStorageSync("headerHeight");
        if (!headerHeight) {
            uni.createSelectorQuery()
                .in(this)
                .select(".headerBox")
                .boundingClientRect((data) => {
                    if (data) {
                        uni.setStorageSync("headerHeight", data.height);
                    }
                })
                .exec();
        }
        //获取本地参数systemIcon
        // const systemIcon = uni.getStorageSync("systemIcon");
 
    },
    beforeDestroy() {
        // #ifdef H5
        if (this.h5ReadyTimer) {
            clearTimeout(this.h5ReadyTimer);
            this.h5ReadyTimer = null;
        }
        if (this._onTranslationDone) {
            window.removeEventListener('translationDone', this._onTranslationDone);
            this._onTranslationDone = null;
        }
        // #endif
    },
    watch: { 
    },
    methods: {
        onTabTap(tab, idx) {
            this.$emit('tab-click', tab, idx);
        },

        _manage() {
            console.log(1);
            this.$emit("_manage");
        },
        _back1() {
                    // 针对 移动店铺端 编辑优惠券做处理
                    if(this.$store.state.editCoupon){
                    	this.$store.commit('SET_EDITCOUPON',false)
                    	return
                    }
                    this.flag = false;
                    if (this.returnR == "no") {
                        this.$emit("returnC");
                        this.flag = true;
                        return;
                    } 
                    switch (Number.parseInt(this.returnR)) {
                        case 1: 
                            setTimeout(() => {
                                //#ifdef H5
                                history.back();
                                // #endif
                                // #ifdef MP-WEIXIN || APP-PLUS
                                uni.navigateBack({
                                    delta: 1,
                                });
                                // #endif
                            }, 200);
                            break;
                        case 2:
                            uni.navigateTo({
                                url: "/pages/shell/shell?pageType=shoppingCart",
                            });
                            break;
                        case 3:
                            uni.redirectTo({
                                url: "/pagesD/login/newLogin.vue",
                            });
                            break;
                        case 4:
                            setTimeout(() => {
                                uni.navigateBack({
                                    delta: 2,
                                });
                            }, 200);
                            break;
                        case 5:
                            switch (this.isMs) {
                                case "IN":
                                    uni.redirectTo({
                                        url: "/pagesB/integral/exchange",
                                    });
                                    break;
                                case "MS":
                                    uni.redirectTo({
                                        url: "/pagesB/seckill/seckill_my",
                                    });
                                    break;
                                case "XQ":
                                    uni.redirectTo({
                                        url: "/pagesB/order/myOrder",
                                    });
                                    break;
                                case "SY":
                                    uni.redirectTo({
                                        url: "/pagesB/seckill/seckill?needLogin=1",
                                    });
                                    break;
                                default:
                                    uni.redirectTo({
                                        url: "/pagesB/order/myOrder",
                                    });
                            }
                            break;
                        case 6:
                            uni.navigateTo({
                                url: "/pages/shell/shell?pageType=home",
                            });
                            if (this.shopping) {
                                location.reload();
                            }
                            break;
                        case 7:
                            uni.navigateTo({
                                url: "/pages/shell/shell?pageType=my",
                            });
                            break;
                        case 8:
                            uni.navigateTo({
                                url: "/pages/shell/shell?pageType=my",
                            });
                            break;
                        case 9:
                            uni.redirectTo({
                                url: "/pagesA/myStore/myStore",
                            });
                            break;
                        case 10:
                            uni.redirectTo({
                                url: "/pagesA/distribution/distribution_center",
                            });
                            break;
                        case 11:
                            switch (this.types) {
                                case "IN":
                                    uni.redirectTo({
                                        url:
                                            "/pagesB/integral/exchange?order_type=" +
                                            this.order_type,
                                    });
                                    break;
                                case "MS":
                                    uni.redirectTo({
                                        url:
                                            "/pagesB/seckill/seckill_my?order_type=" +
                                            this.order_type,
                                    });
                                    break;
                                case "FS":
                                    uni.redirectTo({
                                        url:
                                            "/pagesC/discount/discount_my?order_type=" +
                                            this.order_type,
                                    });
                                    break;
                            }
                            break;
                        case 12:
                            setTimeout(() => {
                                uni.navigateBack({
                                    delta: 1,
                                });
                            }, 200);
                            break;
                        case 13:
                            uni.redirectTo({
                                url: "/pagesB/order/order?order_id=" + this.order_id + '&skipType=1',
                            });
                            break;
                        case 14:
                            uni.redirectTo({
                                url: "/pagesA/auction/Myauction",
                            });
                            break;
                        case 15:
                            uni.redirectTo({
                                url: "/pagesA/myStore/myStore",
                            });
                            break;
                        case 16:
                            uni.redirectTo({
                                url:
                                    "/pagesB/invoice/invoiceManagement?status=" +
                                    this.types,
                            });
                            break;
                        case 17:
                            uni.redirectTo({
                                url: "/pagesC/synthesize/Integral?type=yushou",
                            });
                            break;
                        case 18:
                            uni.redirectTo({
                                url: "/pagesA/myStore/myStore",
                            });
                            break;
                        case 19:
                            uni.redirectTo({
                                url: "/pagesC/myStore/group/groupActivity",
                            });
                            uni.removeStorageSync("shacoList");
                            uni.removeStorageSync("proIds");
                            break;
                        case 20:
                            uni.redirectTo({
                                url: "/pagesC/myStore/group/groupList",
                            });
                            uni.removeStorageSync("shacoList");
                            uni.removeStorageSync("proIds");
                            break;
                        case 21:
                            uni.navigateBack({
                                delta: 2,
                            });
                            break;
                        // 直播商品返回xxx
                        case 22:
                            this.$emit("handleGoUrl");
                            break;
                        case 120:
                            // #ifdef H5
                            //待付款订单 支付宝取消支付时，内置浏览器不会执行支付回调，还在此页面。
                            //此时点击返回需要关闭所有页面并返回首页，因此增加zfbPay参数用于判断。
                            location.reload();
                            // #endif
                            uni.navigateTo({
                                url: "/pages/shell/shell?pageType=home",
                            });
                            break;
                        case 121:
                            // #ifdef H5
                            // location.reload();
                            // #endif
                            uni.redirectTo({
                                url: "/pagesD/liveStreaming/liveGoods",
                            });
                            break;
                        case 122:
                            uni.redirectTo({
                                url: `/pagesD/liveStreaming/liveDetail??id=${this.live.id}&aid=${this.live.aid}`,
                            });
                        break;
                        //
                        default:
                            if (getCurrentPages().length > 1) { 
                                uni.setStorageSync('skipType',1)
                                setTimeout(() => {
                                    uni.navigateBack({
                                        delta: 1,
                                    });
                                }, 200);
                            } else {
                                uni.navigateTo({
                                    url: "/pages/shell/shell?pageType=home",
                                });
                            }
                    }
                    this.flag = true;
                },
    },
};
</script>

<style scoped lang="less">
@import url("@/laike.less");
.header-title {
}

.header-tabs {
    position: relative;
    width: 100%;
    height: 88rpx;
    line-height: 88rpx;
    padding-left: 160rpx;
    padding-right: 160rpx;
    box-sizing: border-box;
    white-space: nowrap;
    background: #ffffff;
    text-align: left;
}

.header-tabs--center {
    text-align: center;
}

.header-tab {
    display: inline-block;
    height: 88rpx;
    line-height: 88rpx;
    padding: 0 22rpx;
    font-size: 30rpx;
    color: #333333; // 黑字
    opacity: 0.65;
}

.header-tab.active {
    opacity: 1;
    font-weight: 600;
}

.header-title--center {
    padding-left: 160rpx;
    padding-right: 160rpx;
    box-sizing: border-box;
}

.left_title {
    font-size: 28rpx;

    font-weight: 400;
    line-height: 88rpx;
    text-align: center;
    color: #333333;
}

.border {
    border-bottom: none !important;
}

.bgc_bai {
    background-color: white !important;
}

.bgno {
    background-color: initial !important;
}

.head {
    position: fixed;
    left: 0;
    top: 0;
    background-color: @titleBackground;
    width: 100%;
    z-index: 999;

    //     border-bottom: 1rpx solid #eee;

    &.head_w {
        background: transparent;
        border-bottom: 0;
    }

    &.noBg {
        background: transparent !important;
    }

    .white {
        background: #ffffff;
    }
    .header-right {
        width: 88rpx !important;
        right: 0;
    }

    .header {
        color: @titleColor;
        border: none;

        .header_img {
            top: 46rpx !important;
            left: 10rpx !important;
            width: 64rpx !important;
            height: 64rpx !important;
        }

        &-title {
            text-align: center;
            width: 100%;
            height: 100%;
            line-height: 88rpx;
            color: @titleColor;
            font-size: 36rpx;

            &.title_w {
                color: #ffffff;
            }
        }

        &-left {
            height: 88rpx;
            width: 160rpx;
            position: absolute;
            z-index: 9999;

            img {
                position: absolute;
                top: 20rpx;
                left: 32rpx;
                width: 24rpx;
                height: 36rpx;
            }

            .new_back {
                width: 24rpx;
                height: 48rpx;
            }

            .new_backs {
                width: 64rpx;
                height: 64rpx;
                position: absolute;
                top: 12rpx;
                left: 12rpx;
            }
        }

        &-right {
            position: absolute;

            /*  #ifndef MP  */
            top: 0;
            /*  #endif  */

            /*  #ifdef MP  */
            //background: #F6f6f6;
            /*  #endif  */

            line-height: 44px;
            color: #020202;
            font-size: 16px;
            display: flex;
            height: 88rpx;
            align-items: center;
            flex-direction: row-reverse;
            width: 100%;
            padding-right: 24rpx;

            img {
                width: 30rpx;
                height: 30rpx;
                margin-right: 10rpx;
            }

            .content {
                display: flex;
                height: 100%;
                align-items: center;
                /*  #ifndef MP  */
                color: @titleColor;
                /*  #endif  */
                /*  #ifdef MP  */
                color: #020202;
                /*  #endif  */
            }
        }

        &_kj {
            /* #ifdef MP */
            background: rgba(0, 0, 0, 0.6);
            color: #ffffff;
            border-radius: 30rpx 0px 0px 30rpx;
            width: 148rpx;
            height: 60rpx;
            right: 0;
            margin-top: 30rpx !important;
            padding-right: 0;
            /* #endif */

            .content {
                /* #ifdef MP */
                color: #ffffff;
                /* #endif */
            }
        }
    }
}
</style>
