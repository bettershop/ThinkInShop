<template>
    <div class="conten">
        <!-- 头部 -->
        <heads
            :title="language.invoiceUpload.title"
            :border="true"
            :bgColor="bgColor"
            ishead_w="2"
            titleColor="#333333"
        ></heads>
        <!-- 发票信息 -->
        <view class="invoic_state">
            <view class="invoice_item">
                <view class="item_left">{{ language.invoiceManagementStore.kpyh }}</view>
                <view class="item_right">
                    <span v-if="true">{{ detail.user_name }}</span>
                </view>
            </view>
            <view class="hr"></view>
            <view class="invoice_item">
                <view class="item_left">{{ language.invoiceDetail.fptt }}</view>
                <view class="item_right">{{ detail.company_name }}</view>
            </view>
            <view class="hr"></view>
            <view class="invoice_item">
                <view class="item_left">{{ language.invoiceUpload.ddh }}</view>
                <view class="item_right">{{ detail.s_no }}</view>
            </view>
            <view class="hr"></view>
            <view class="invoice_item">
                <view class="item_left">{{ language.invoiceDetail.ddje }}</view>
                <view class="item_right" style="color: #fa5151"
                    >{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(detail.invoice_amount) }}</view
                >
            </view>
        </view>
        <!-- 上传发票 -->
        <view class="invoic_content">
            <view class="scimg">
                <span style="color: #fa5151">*</span>
                {{ language.invoiceUpload.title }}
            </view>
            <view class="upland_img">
                <!-- 上传发票的组件 -->
                <view class="img_box">
                    <div class="upImg1" v-if="cover_map">
                        <img
                            class="wh_100"
                            @click="_seeImg"
                            :src="cover_map"
                            alt=""
                        />
                        <img
                            :src="delImg"
                            @tap="_delImg2()"
                            class="delImg"
                            v-if="pageStatus != 2"
                        />
                    </div>
                    <div class="upImg" @tap="_chooseImg2()" v-else>
                        <img
                            :src="textIcon"
                            style="width: 48rpx; height: 48rpx"
                            alt=""
                        />
                    </div>
                </view>
                <view class="img_text">{{ language.invoiceUpload.text }}</view>
            </view>
        </view>
        <!-- 底部 -->
        <view class="btn">
            <view class="submit">
                <p @tap="handleSubmit">{{ language.openInvoice.btn }}</p>
            </view>
        </view>
        <!-- 查看发票 -->
        <view class="mask_invoice" v-if="mask_type" @tap.stop="close">
            <div class="content" @tap.stop="">
                <image
                    class="invoice_img"
                    id="code_img"
                    :src="cover_map"
                    mode="widthFix"
                ></image>
                <p @tap.stop="dow" v-if="!isShowImg">
                    <image :src="download_invoice" mode=""></image>
                    <span>{{ language.invoiceManagement.xzfp }}</span>
                </p>
                <img
                    :src="fpClose"
                    @tap.stop="close"
                    class="fp_close"
                    alt=""
                    v-if="isShowImg"
                />
            </div>
        </view>
        <show-toast
            :is_showToast="is_showToast"
            :is_showToast_obj="is_showToast_obj"
        ></show-toast>
    </div>
</template>
<script>
import {
    LaiKeTui_chooseImg2,
    LaiKeTui_delImg2,
} from "@/pagesA/myStore/myStore/uploadPro.js";
import showToast from "@/components/aComponents/showToast.vue";

export default {
    data() {
        return {
            title: "上传发票",
            bgColor: [
                {
                    item: "#FFFFFF",
                },
                {
                    item: "#FFFFFF",
                },
            ],
            id: "",
            detail: {},
            cover_map: "", // 图片保存
            delImg:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/delete2x.png",
            jiantou:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/jiantou2x.png",
            textIcon:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/xiangji2x.png",
            fpClose:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/fp_close.png",
            mask_type: false,
            isShowImg: false,
            is_showToast: 0, //是否显示
            is_showToast_obj: {}, //弹窗显示文字
            sus:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/sus.png",
            del:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/delete2x.png",
        };
    },
    onLoad(option) {
        this.id = option.id;
    },
    computed: {},
    components: { showToast },
    mounted() {},
    // 页面卸载
    onUnload() {},
    onShow() {
        this.handleDetail();
    },
    // 上拉加载
    onReachBottom: function () {},
    // 下拉刷新
    onPullDownRefresh() {},
    //滚动条事件
    onPageScroll(e) {},
    //事件
    methods: {
        // 上传发票
        handleSubmit() {
            let data = {
                api: "mch.App.Invoice.UploadInvoice",
                id: this.id,
                shop_id: uni.getStorageSync("shop_id")
                    ? uni.getStorageSync("shop_id")
                    : "",
                filePath: this.cover_map,
            };
            this.$req.post({ data }).then((res) => {
                if (res.code == 200) {
                    this.is_showToast = 1;
                    this.is_showToast_obj.imgUrl = this.sus;
                    this.is_showToast_obj.title = "提交成功";
                    setTimeout(() => {
                        this.is_showToast = 0;
                        uni.navigateBack({
                            delta: 1,
                        });
                    }, 1000);
                }
            });
        },
        handleDetail() {
            let data = {
                api: "mch.App.Invoice.GetInfo",
                id: this.id,
                shop_id: uni.getStorageSync("shop_id")
                    ? uni.getStorageSync("shop_id")
                    : "",
            };
            this.$req.post({ data }).then((res) => {
                if (res.code == 200) {
                    this.detail = res.data;
                }
            });
        },
        // 选择封面图片
        _chooseImg2() {
            LaiKeTui_chooseImg2(this,true);
        },
        // 删除图片
        _delImg2() {
            LaiKeTui_delImg2(this);
        },
        // 查看图片
        _seeImg() {
            this.mask_type = true;
            this.isShowImg = true;
        },
        close() {
            this.mask_type = false;
            this.isShowImg = false;
        },
    },
};
</script>
<style>
page {
    background-color: #f4f5f6;
}
</style>
<style lang="less" scoped>
.mask_invoice {
    position: fixed;
    left: 0;
    top: 0;
    width: 100vw;
    height: 100vh;
    z-index: 999;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 999;
    .content {
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        width: 100%;
        z-index: 9999;
        .invoice_img {
            width: 100%;
        }
        .fp_close {
            position: absolute;
            top: 10rpx;
            right: 10rpx;
            width: 32rpx;
            height: 32rpx;
            margin: 12rpx;
        }
        p {
            width: 332rpx;
            height: 92rpx;
            background: #fa5151;
            border-radius: 46rpx;
            color: #ffffff;
            margin-top: 188rpx;
            display: flex;
            align-items: center;
            justify-content: center;
            image {
                width: 40rpx;
                height: 40rpx;
            }
            span {
                font-size: 32rpx;
                font-weight: 500;
                color: #ffffff;
            }
        }
    }
}
.invoic_state {
    min-height: 104rpx;
    background-color: #fff;
    border-radius: 0 0 24rpx 24rpx;
    box-sizing: border-box;
}
.invoic_content {
    min-height: 104rpx;
    background-color: #fff;
    border-radius: 24rpx;
    box-sizing: border-box;
    margin-top: 16rpx;
    padding: 32rpx;
    display: flex;
    .scimg {
        color: #333333;
        font-size: 32rpx;
        margin-top: 12rpx;
        margin-right: 34rpx;
    }
    .upland_img {
        .img_box {
            .upImg {
                width: 144rpx;
                height: 144rpx;
                border: 1rpx dashed #cccccc;
                display: flex;
                justify-content: center;
                align-items: center;
                border-radius: 16rpx;
            }
            .upImg1 {
                width: 144rpx;
                height: 144rpx;
                position: relative;
                margin-right: 10rpx;
                margin-bottom: 16rpx;
                border-radius: 16rpx;
                /deep/.uni-video-cover {
                    display: none !important;
                }
            }
            .wh_100 {
                width: 100% !important;
                height: 100% !important;
                border-radius: 16rpx !important;
                z-index: 99 !important;
            }
            .delImg {
                width: 32rpx;
                height: 32rpx;
                position: absolute;
                top: 8rpx;
                right: 8rpx;
                z-index: 2;
            }
        }
        .img_text {
            color: #999999;
            font-size: 24rpx;
            margin-top: 16rpx;
        }
    }
}
// 底部提交
.btn {
    position: fixed;
    bottom: 0;
    left: 0;
    width: 100%;
    background-color: #ffffff;
    /* #ifdef MP-WEIXIN */
    padding-bottom: constant(safe-area-inset-bottom); /* 兼容 iOS < 11.2 */
    padding-bottom: env(safe-area-inset-bottom);
    /* #endif */

    .submit {
        width: 100%;
        background: #ffffff;
        padding: 16rpx 32rpx;
        box-sizing: border-box;
        p {
            width: 100%;
            height: 92rpx;
            background: linear-gradient(270deg, #ff6f6f 0%, #fa5151 100%);
            border-radius: 52rpx;
            color: #ffffff;
            font-size: 32rpx;
            display: flex;
            justify-content: center;
            align-items: center;
        }
    }
}
.invoice_item {
    min-height: 104rpx;
    padding: 32rpx;
    display: flex;
    justify-content: space-between;
    box-sizing: border-box;
    .item_left {
        color: #333333;
        font-size: 32rpx;
    }
    .item_right {
        color: #333333;
        font-size: 32rpx;
        font-weight: 500;
        img {
            width: 216rpx;
            height: 144rpx;
        }
    }
}

.hr {
    width: 686rpx;
    height: 1rpx;
    background-color: rgba(0, 0reen, 0, 0.1);
    margin: 0 auto;
}
</style>
