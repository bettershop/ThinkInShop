<template>
    <div>
        <template v-if="myShowIsBill">
            <toload v-if="guiderImg.length == 0"></toload>
            <div v-else class="relative">
                <div class="guiderBtn" @tap="countDown(true)">
                    {{ time }} {{ language.index.In }}
                </div>
                <swiper
                    class="swiper"
                    circular="false"
                    autoplay="autoplay"
                    interval="1000"
                    :indicator-dots="guiderImg && guiderImg.length > 1"
                >
                    <swiper-item
                        v-for="(item, index) in guiderImg"
                        :key="index"
                    >
                        <image class="image" :src="item.image"></image>
                    </swiper-item>
                </swiper>
            </div>
        </template>
        <template v-else>
            <view class="skeleton containerBg" :style="grbj">
                <view class="hearder">维护公告</view>
                <view class="state_img">
                    <img :src="selectimg" alt="" />
                </view>
                <view class="heard_title">{{ bill_title }}</view>
                <view class="notice_content">
                    <rich-text :nodes="bill_details"></rich-text>
                </view>
            </view>
        </template>
    </div>
</template>

<script>
import { getLaiketuiNoRegisterLoginInfo } from "../../common/laiketuiNoRegisterLogin.js"; 
export default {
    data() {
        return { 
            guiderImg: [], // 轮播图
            time: 3, //倒计时秒数设定
            autoplay: true, //是否自动切换
            clear: "",
            selectimg:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/search_not.png",
            grbj:
                "background-image: url(" +
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/auction/grbj.png);background-size: 100vw 100vh;background-repeat: no-repeat;",
            bgColor: [
                {
                    item: "#ffffff",
                },
                {
                    item: "#ffffff",
                },
            ],
            is_bill: false,
            myShowIsBill: true,
            bill_title: "",
            bill_details:
                "<p>公告提示内容</p><p>公告提示内容</p><p>公告提示内容</p><p>公告提示内容</p><p>公告提示内容</p><p>公告提示内容</p><p>公告提示内容</p> ",
        };
    },
    onLoad(options) { 
        // #ifdef H5 
        if(options.eliminate){
            uni.clearStorage();
        }
         this.LaiKeTuiCommon.initStoreID(options.store_id);
        // #endif
        
        // #ifdef MP-WEIXIN
        let p = null;
        p = this.gd();

        if (p) {
            p.then((url) => {
                this._geturl(url, () => {
                    this.getImg();
                    uni.getSystemInfo({
                        success: (res) => {
                            this.$store.state.data_height = res.statusBarHeight;
                        },
                    });
                });
            });
        }
        // #endif

        // #ifndef MP-WEIXIN
        var url = this.LaiKeTuiCommon.LKT_API_URL;
        this._geturl(url, () => {
            this.getImg();
            uni.getSystemInfo({
                success: (res) => {
                    this.$store.state.data_height = res.statusBarHeight;
                },
            });
        });
        // #endif
    },
    onUnload() {
        clearInterval(this.clear);
    },
    onShow() {
        this.autoplay = true;
        this.time = 3;
        this.syncMallTitle();
    },
    methods: {
        // 获取轮播图
        getImg: function () {
            let data2 = {
                api: "app.index.getUserTell",
            };

            this.$req.post({ data: data2 }).then((res2) => {
                if (res2.code == 200) {
                    if (res2.data.systemMsgType == 1) {
                        this.is_bill = true;
                        this.bill_details = res2.data.systemMsg;
                        this.bill_title = res2.data.systemMsgTitle;
                    }
                }

                let data = {
                    api: "app.index.guided_graph",
                    // #ifdef MP-WEIXIN || MP-ALIPAY
                    guideType: 1,
                    // #endif
                    // #ifndef MP-WEIXIN
                    guideType: 2,
                    // #endif
                };
                if (res2.code == 200) {
                    this.$req.post({ data }).then((res) => {
                        let { data } = res;
                        // 当有轮播图的时候才显示本页面，否则直接跳转到home页
                        if (data && data.length > 0) {
                            this.guiderImg = data;
                            // 启动倒计时
                            this.clear = setInterval(this.countDown, 1000);
                        } else {
                            if (this.is_bill) {
                                this.myShowIsBill = false;
                            } else { 
                               uni.redirectTo({
                                   url: "/pages/shell/shell?pageType=home",
                               });
                            }
                        }
                    });
                }
            });
        },
        // 倒计时
        countDown(type) {
            if (this.time == 1 || type) {
                if (this.is_bill) {
                    this.myShowIsBill = false;
                } else { 
                   uni.redirectTo({
                       url: "/pages/shell/shell?pageType=home",
                   });
                }
                clearInterval(this.clear);
            } else if (this.time > 1) {
                this.time--;
            }
        },
        //第三方授权，接口地址获取函数
        gd() {
            let extConfig = uni.getExtConfigSync ? uni.getExtConfigSync() : {};
            if (extConfig.url) {
                this.$store.state.url = extConfig.url;
            } else {
                this.$store.state.url = this.LaiKeTuiCommon.LKT_API_URL;
            }
            this.$store.state.url = this.$store.state.url + "store_type=1";
            let request_url = this.$store.state.url;
            //获取是否免密码登录开关
            if (!uni.getStorageSync("needRegister")) {
                getLaiketuiNoRegisterLoginInfo(1);
            }
            return Promise.resolve(request_url);
        },
        // 获取公共链接地址
        async _geturl(request_url, callback) {
           
            let store_type;
            // #ifdef APP-PLUS
            store_type = 11;
            // #endif
            // #ifdef MP-ALIPAY
            store_type = 3;
            // #endif
            // #ifdef MP-BAIDU
            store_type = 5;
            // #endif
            // #ifdef MP-TOUTIAO
            store_type = 4;
            // #endif
            // #ifdef MP-WEIXIN
            store_type = 1;
            // #endif
            // #ifdef H5
            store_type = 2;
            // #endif

            this.$store.state.url = this.LaiKeTuiCommon.LKT_API_URL;
            this.$store.state.h5_url = this.LaiKeTuiCommon.LKT_H5_DEFURL;
            this.$store.state.endurl = this.LaiKeTuiCommon.LKT_ENDURL; 
            
            this.$store.state.url =
                this.$store.state.url + "&store_type=" + store_type;
            uni.setStorageSync("url", this.$store.state.url);
            uni.setStorageSync("h5_url", this.$store.state.h5_url);
            uni.setStorageSync("endurl", this.$store.state.endurl);

            callback && callback();
        },
    },
};
</script>

<style lang="less">
@import url("../../static/css/index/index.less");
.containerBg {
    height: 100vh;
}
.hearder {
    height: 88rpx;
    display: flex;
    justify-content: center;
    align-items: center;
    color: #000000;
    font-size: 32rpx;
    font-weight: 500;
}
.state_img {
    width: 100%;
    height: 460rpx;
    img {
        width: 100%;
        height: 100%;
    }
    margin-bottom: 80rpx;
}
.heard_title {
    text-align: center;
    color: #333333;
    font-size: 32rpx;
    font-weight: 500;
    margin-bottom: 32rpx;
}
.notice_content {
    width: 654rpx;
    height: 580rpx;
    margin-top: 32rpx;
    overflow-y: scroll;
    margin: 0 auto;
    color: #999999;
    font-size: 32rpx;
}
</style>
