<template>
    <div class="conten">
        <!-- 头部 -->
        <heads
            :title="title"
            :border="true"
            :bgColor="bgColor"
            ishead_w="2"
            titleColor="#333333"
        ></heads>
        <!-- 搜索和数据过滤 -->
        <view class="search_and_nav">
            <view class="search">
                <search
                    :is_search_prompt="language.storeInvoice.sNo"
                    :is_search_type="2"
                    @search="_seart"
                    @clearch="_clearch"
                ></search>
            </view>
            <view class="nav">
                <ul class="nav_header">
                    <li
                        class="header_li"
                        :class="{ header_border: status == index }"
                        v-for="(item, index) in language.storeInvoice.type"
                        :key="index"
                        @tap="nav_index(index)"
                    >
                        {{ item }}
                    </li>
                </ul>
            </view>
        </view>
        <!-- 内容区域 -->
        <view class="content_invoic">
            <scroll-view
                :scroll-y="true"
                style="height: 100%"
                @scrolltolower="scrollBottomMovie"
                v-if="list.length > 0"
            >
                <view
                    class="content_invoic_item"
                    v-for="(item, index) in list"
                    :key="index"
                >
                    <view class="invoic_sno">
                        <view class="sno_left">{{language.invoiceManagementStore.ddh}}：{{ item.s_no }}</view>
                        <!-- 事件未触发 -->
                        <view class="sno_right">
                            <span class="red" v-if="item.invoice_status == 1"
                                >{{ language.invoiceManagementStore.state[0] }}</span
                            >
                            <span class="red" v-if="item.invoice_status == 2"
                                >{{ language.invoiceManagementStore.state[1] }}</span
                            >
                            <span class="red" v-if="item.invoice_status == 3"
                                >{{ language.invoiceManagementStore.state[2] }}</span
                            >
                        </view>
                    </view>
                    <!-- 订单text  -->
                    <view class="sno_text">
                        <p>{{language.invoiceManagementStore.kpyh}}：{{ item.user_name }}</p>
                        <p class="time">{{language.invoiceManagementStore.tjsj}}：{{ item.add_time }}</p>
                    </view>
                    <!-- 公司名称 -->
                    <view class="gs_title"> {{ item.company_name }} </view>
                    <!-- 底部按钮footer -->
                    <view class="footer2">
                        <view class="footer_left">
                            {{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(item.invoice_amount) }}
                        </view>
                        <view class="footer_right">
                            <view
                                class="btn_xq btn"
                                @click="goToDetail(item.id)"
                                >{{ language.invoiceManagementStore.fpxq }}</view
                            >
                            <view
                                class="btn_sc btn"
                                v-if="item.invoice_status == 1"
                                @click="goToUpload(item.id)"
                                >{{ language.invoiceManagementStore.scfp }}</view
                            >
                            <view
                                class="btn_del btn"
                                v-if="item.invoice_status == 3"
                                @click="delInvoic(item.id)"
                                >{{ language.invoiceManagementStore.sc }}</view
                            >
                        </view>
                    </view>
                </view>
            </scroll-view>
            <view class="no_header" v-else>
                <image :src="no_header_info" mode="heightFix"></image>
                <p>{{ noTitle }}</p>
            </view>
        </view>
        <show-toast
            :is_showToast="is_showToast"
            :is_showToast_obj="is_showToast_obj"
        ></show-toast>
    </div>
</template>
<script>
import search from "@/components/aComponents/search.vue";
import showToast from "@/components/aComponents/showToast.vue";

export default {
    data() {
        return {
            title: "发票管理",
            status: 0,
            is_showToast: 0, //是否显示
            is_showToast_obj: {}, //弹窗显示文字
            sus:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/sus.png",
            del:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/delete2x.png",
            no_header_info:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/no_header_info.png",
            noTitle: "暂无开票申请哦~",
            bgColor: [
                {
                    item: "#FFFFFF",
                },
                {
                    item: "#FFFFFF",
                },
            ],
            list: [],
            page: 1,
            pagesize: 10,
            loadingType: 0,
            condition: "",
        };
    },
    onLoad(option) {
        
    },
    computed: {},
    components: { search, showToast },
    mounted() {},
    // 页面卸载
    onUnload() {},
    onShow() {
        this.noTitle = this.language.invoiceManagement.noTitleList[4]
        this.title = this.language.invoiceManagement.title
        this.page = 1;
        this.pagesize = 10;
        this.list = [];
        this.getInvoiceList();
    },
    // 上拉加载
    onReachBottom: function () {},
    // 下拉刷新
    onPullDownRefresh() {},
    //滚动条事件
    onPageScroll(e) {},
    //事件
    methods: {
        scrollBottomMovie() {
            this.page++;
            if (this.loadingType != 0) {
                return;
            }
            this.getInvoiceList();
        },
        _clearch() {
            this.page = 1;
            this.pagesize = 10;
            this.list = [];
            this.condition = "";
            this.getInvoiceList();
        },
        _seart(item) {
            this.page = 1;
            this.pagesize = 10;
            this.list = [];
            this.condition = item;
            this.getInvoiceList();
        },
        // 切换nav
        nav_index(index) {
            this.status = index;
            this.page = 1;
            this.pagesize = 10;
            this.list = [];
            this.getInvoiceList();
        },
        // 获取发票列表数据
        getInvoiceList() {
            let data = {
                api: "mch.App.Invoice.GetList",
                invoiceStatus: this.status,
                condition: this.condition,
                page: this.page,
                pagesize: this.pagesize,
                shop_id: uni.getStorageSync("shop_id")
                    ? uni.getStorageSync("shop_id")
                    : "",
            };
            this.$req.post({ data }).then((res) => {
                if (res.code == 200) {
                    this.list = this.list.concat(res.data.list);
                    if (res.data.list.length < 10) {
                        this.loadingType = 2;
                    } else {
                        this.loadingType = 0;
                    }
                } else {
                    this.loadingType = 2;
                }
            });
        },
        // 跳转到发票详情
        goToDetail(id) {
            uni.navigateTo({
                url: "/pagesB/invoice/invoiceDetail?id=" + id,
            });
        },
        // 跳转到上传发票界面
        goToUpload(id) {
            uni.navigateTo({
                url: "/pagesB/invoice/invoiceUpload?id=" + id,
            });
        },
        // 删除数据
        delInvoic(id) {
            let data = {
                api: "mch.App.Invoice.DelInvoice",
                id: id,
                shop_id: uni.getStorageSync("shop_id")
                    ? uni.getStorageSync("shop_id")
                    : "",
            };
            uni.showModal({
                title: this.language.invoiceManagementStore.ts,
                content: this.language.invoiceManagementStore.sfscfp,
                confirmText: this.language.order.myorder.confirm,
                cancelText: this.language.order.myorder.cancel,
                success: (res) => {
                    if(res.confirm){                        
                        this.$req.post({ data }).then((res) => {
                            if (res.code == 200) {
                                this.is_showToast = 1;
                                this.is_showToast_obj.imgUrl = this.sus;
                                this.is_showToast_obj.title = this.language.invoiceManagementStore.sccg;
                                setTimeout(() => {
                                    this.is_showToast = 0;
                                    this.page = 1;
                                    this.pagesize = 10;
                                    this.list = [];
                                    this.getInvoiceList();
                                }, 1000);
                            }
                        });
                    }
                },
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
<style lang="less" scoped>
.search_and_nav {
    height: 192rpx;
    background-color: #fff;
    border-radius: 0 0 24rpx 24rpx;
    box-sizing: border-box;
    .search {
        height: 88rpx;
        box-sizing: border-box;
    }
    .nav {
        height: 104rpx;
        box-sizing: border-box;
        .nav_header {
            box-sizing: border-box;
            width: 100%;
            background: #ffffff;
            border-radius: 24rpx;
            display: flex;
            align-items: center;
            justify-content: space-evenly;
            margin-top: 24rpx;
            padding: 32rpx 0 40rpx 0;
            li {
                width: 25%;
                position: relative;
                display: flex;
                justify-content: center;
                font-size: 28rpx;
                font-weight: 500;
                color: #999999;
            }
            .header_border {
                font-size: 32rpx;
                font-weight: 500;
                color: #333333;
                &::after {
                    position: absolute;
                    content: "";
                    width: 96rpx;
                    height: 4rpx;
                    background: #fa5151;
                    left: 50%;
                    transform: translateX(-50%);
                    bottom: -12rpx;
                    border-radius: 5rpx;
                }
            }
        }
    }
}
// 列表内容
.content_invoic {
    height: calc(100vh - 330rpx);
    margin-top: 50rpx;
    box-sizing: border-box;
    overflow: scroll;
    .content_invoic_item {
        background-color: #fff;
        border-radius: 24rpx;
        padding: 32rpx;
        margin-bottom: 16rpx;
        .invoic_sno {
            display: flex;
            justify-content: space-between;
            .sno_left {
                color: #333333;
                font-size: 28rpx;
            }
            .sno_right {
                font-size: 24rpx;
                .red {
                    color: #fa5151;
                }
            }
        }
        .sno_text {
            width: 686rpx;
            height: 144rpx;
            border-radius: 20rpx;
            background-color: #f4f5f6;
            margin-top: 32rpx;
            padding: 24rpx;
            box-sizing: border-box;
            p {
                color: #333333;
                font-size: 28rpx;
            }
            .time {
                margin-top: 16rpx;
            }
        }
        .gs_title {
            color: #333333;
            font-size: 32rpx;
            font-weight: 500;
            margin-top: 24rpx;
        }
        .footer2 {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-top: 30rpx;
            margin-bottom: 6rpx;
            .footer_left {
                font-size: 32rpx;
                color: #fa5151;
                font-weight: 500;
            }
            .footer_right {
                display: flex;
                .btn {
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    width: 144rpx;
                    height: 56rpx;
                    border-radius: 28rpx;
                    background-color: #f4f5f6;
                    color: #333333;
                    font-size: 24rpx;
                    margin-right: 24rpx;
                }
                .btn:last-child{
                    margin: 0rpx;
                }
            }
        }
    }
    .no_header {
        width: 100%;
        height: 100%;
        background-color: #ffffff;
        flex: 1;
        border-radius: 0px 0px 24rpx 24rpx;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        margin-top: 16rpx;
        border-radius: 24rpx 24rpx 0 0;
        image {
            height: 460rpx;
        }
        p {
            font-size: 28rpx;
            color: #333333;
        }
    }
}
</style>
