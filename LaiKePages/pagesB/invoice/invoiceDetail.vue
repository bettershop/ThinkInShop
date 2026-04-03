<template>
    <div class="conten">
        <!-- 头部 -->
        <heads
            :title="language.invoiceDetail.title"
            :border="true"
            :bgColor="bgColor"
            ishead_w="2"
            titleColor="#333333"
        ></heads>
        <!-- 发票状态 -->
        <view class="invoic_state">
            <view class="invoice_item">
                <view class="item_left">{{ language.invoiceDetail.fpzt }}</view>
                <view class="item_right">
                    <span v-if="detail.invoice_status == 1">{{ language.invoiceDetail.state[0] }}</span>
                    <span v-if="detail.invoice_status == 3">{{ language.invoiceDetail.state[2] }}</span>
                    <span
                        v-if="detail.invoice_status == 2"
                        style="color: #07c160"
                        >{{ language.invoiceDetail.state[1] }}</span
                    >
                </view>
            </view>
            <template v-if="detail.invoice_status == 2">
                <view class="hr"></view>
                <view class="invoice_item">
                    <view class="item_left">{{ language.invoiceDetail.scsj }}</view>
                    <view class="item_right">{{ detail.file_time }}</view>
                </view>
                <view class="hr"></view>
                <view class="invoice_item">
                    <view class="item_left">{{ language.invoiceDetail.fptp }}</view>
                    <view class="item_right">
                        <img :src="detail.file" @click="_seeImg" alt="" />
                    </view>
                </view>
            </template>
        </view>
        <!-- 中间 -->
        <view class="invoic_content">
            <view class="invoice_item">
                <view class="item_left">{{ language.invoiceDetail.fpID }}</view>
                <view class="item_right">{{ detail.id }}</view>
            </view>
            <view class="hr"></view>

            <view class="invoice_item">
                <view class="item_left">{{ language.invoiceDetail.kpyh }}</view>
                <view class="item_right">{{ detail.user_name }}</view>
            </view>
            <view class="hr"></view>

            <view class="invoice_item">
                <view class="item_left">{{ language.invoiceDetail.ttlx}}</view>
                <view class="item_right">{{
                    detail.type == 1 ? "企业" : "个人"
                }}</view>
            </view>
            <view class="hr"></view>

            <view class="invoice_item">
                <view class="item_left">{{ language.invoiceDetail.fptt }}</view>
                <view class="item_right">{{ detail.company_name }}</view>
            </view>
            <view class="hr"></view>

            <view class="invoice_item">
                <view class="item_left">{{ language.invoiceDetail.sh }}</view>
                <view class="item_right">{{ detail.company_tax_number }}</view>
            </view>
            <view class="hr"></view>

            <view class="invoice_item">
                <view class="item_left">{{ language.invoiceDetail.dzyx }}</view>
                <view class="item_right">{{ detail.email }}</view>
            </view>
        </view>

        <!-- 底部footer -->
        <view class="invoic_footer">
            <view class="invoice_item">
                <view class="item_left">{{ language.invoiceDetail.ddbh }}</view>
                <view class="item_right">{{ detail.s_no }}</view>
            </view>
            <view class="hr"></view>

            <view class="invoice_item">
                <view class="item_left">{{ language.invoiceDetail.ddje }}</view>
                <view class="item_right">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(detail.invoice_amount) }}</view>
            </view>
            <view class="hr"></view>

            <view class="invoice_item">
                <view class="item_left">{{ language.invoiceDetail.tjsj }}</view>
                <view class="item_right">{{ detail.add_time }}</view>
            </view>
        </view>
        <!-- 查看发票 -->
        <view class="mask_invoice" v-if="mask_type" @tap.stop="close">
            <div class="content" @tap.stop="">
                <image
                    class="invoice_img"
                    id="code_img"
                    :src="detail.file"
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
    </div>
</template>
<script>
export default {
    data() {
        return {
            title: "发票详情",
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
            fpClose:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/fp_close.png",
            mask_type: false,
            isShowImg: false,
        };
    },
    onLoad(option) {
        this.id = option.id;
    },
    computed: {},
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
// 中间
.invoic_content {
    min-height: 104rpx;
    background-color: #fff;
    border-radius: 24rpx;
    box-sizing: border-box;
    margin-top: 16rpx;
}
// 底部
.invoic_footer {
    min-height: 104rpx;
    background-color: #fff;
    border-radius: 24rpx;
    box-sizing: border-box;
    margin-top: 16rpx;
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
