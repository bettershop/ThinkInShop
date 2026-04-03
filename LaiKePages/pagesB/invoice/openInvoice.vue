<template>
    <view class="container">
        <view class="position_head">
            <heads
                :title="language.openInvoice.title"
                ishead_w="2"
                :bgColor="bgColor"
                titleColor="#333333"
            ></heads>
        </view>
        <view class="header_info">
            <view class="content">
                <ul>
                    <li>
                        <p class="title">{{ language.openInvoice.ttlx }}</p>
                        <p class="con">
                            {{ defaultHeader.type == 1 ? "企业单位" : "个人" }}
                        </p>
                    </li>
                    <li>
                        <p class="title">{{ language.openInvoice.gsmc }}</p>
                        <p class="con">{{ defaultHeader.company_name }}</p>
                    </li>
                    <li v-if="defaultHeader.type == 1">
                        <p class="title">{{ language.openInvoice.gssh }}</p>
                        <p class="con">
                            {{ defaultHeader.company_tax_number }}
                        </p>
                    </li>
                    <li>
                        <p class="title">{{ language.openInvoice.zje }}</p>
                        <p class="con red">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(price) }}</p>
                    </li>
                </ul>

                <view class="emile">
                    <p class="emile_left">
                        <span>*</span>
                        <span>{{ language.openInvoice.dzyx }}</span>
                    </p>
                    <p class="emile_right">
                        <input
                            type="text"
                            :placeholder="language.openInvoice.shudzyx"
                            v-model="emile"
                        />
                    </p>
                </view>
            </view>
        </view>

        <view class="btn">
            <view class="change_header">
                <p @tap="changeHeader">{{ language.openInvoice.ghtt }}</p>
                <image
                    @tap="changeHeader"
                    :src="change_header_more"
                    mode=""
                ></image>
            </view>
            <view class="submit">
                <p @tap="submit">{{ language.openInvoice.btn }}</p>
            </view>
        </view>

        <view class="headerMask" v-if="showMask">
            <view class="mask_header">
                <p></p>
                <p>{{ language.openInvoice.fptt }}</p>
                <image :src="Close_header" mode="" @tap="close"></image>
            </view>
            <view class="mask_content">
                <ul>
                    <li v-for="(item, index) in invoiceHeaderList" :key="index">
                        <p class="header_info_left">
                            <span>{{ item.company_name }}</span>
                            <span>{{ item.company_tax_number }}</span>
                        </p>
                        <p class="header_info_right">
                            <image
                                :src="
                                    item.id == headerId
                                        ? select_header
                                        : no_select_header
                                "
                                mode=""
                                @tap="selectHeader(item)"
                            ></image>
                        </p>
                    </li>
                </ul>
            </view>
        </view>
        <view class="model" v-if="showMask"> </view>
    </view>
</template>

<script>
export default {
    data() {
        return {
            title: "开发票",
            sNo: "",
            price: "",
            defaultHeader: {},
            invoiceHeaderList: [],
            change_header_more:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/change_header_more.png",
            select_header:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/select_header.png",
            no_select_header:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/no_select_header.png",
            Close_header:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/Close_header.png",
            bgColor: [
                {
                    item: "#ffffff",
                },
                {
                    item: "#ffffff",
                },
            ],
            showMask: false,
            headerId: "",
            emile: "",

            id: "",
        };
    },

    onShow() {
        if (this.id) {
            this.getDetails();
        } else {
            this.getDefault();
        }
        this.getList();
    },

    onLoad(option) {
        if (option.id) {
            this.id = option.id;
        }
        if (option.sNo) {
            this.sNo = option.sNo;
            this.price = option.price;
        }
    },

    methods: {
        numberFixedDigit2(e) {
            // emile
            var emial = e.target.value;
            var tel = /^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/;
            if (tel.test(emial)) {
                return true;
            }

            this.$nextTick(() => {
                this.emile = e.target.value = "";
            });
        },
        getDetails() {
            let data = {
                api: "app.invoiceInfo.getDetails",
                id: this.id,
            };

            this.$req
                .post({ data })
                .then((res) => {
                    this.sNo = res.data.s_no;
                    this.price = res.data.invoice_amount;
                    this.defaultHeader = JSON.parse(res.data.invoice_header);
                    this.headerId = this.defaultHeader.id;
                    this.emile = res.data.email;
                })
                .catch((err) => {
                    uni.showToast({
                        title: err.message,
                        duration: 1500,
                        icon: "none",
                    });
                });
        },
        getDefault() {
            let data = {
                api: "app.invoiceHeader.getDefault",
            };

            this.$req
                .post({ data })
                .then((res) => {
                    this.defaultHeader = res.data;
                    this.headerId = this.defaultHeader.id;
                })
                .catch((err) => {
                    uni.showToast({
                        title: err.message,
                        duration: 1500,
                        icon: "none",
                    });
                });
        },
        getList() {
            let data = {
                api: "app.invoiceHeader.getList",
            };

            this.$req
                .post({ data })
                .then((res) => {
                    this.invoiceHeaderList = res.data.list;
                })
                .catch((err) => {
                    uni.showToast({
                        title: err.message,
                        duration: 1500,
                        icon: "none",
                    });
                });
        },
        close() {
            this.showMask = false;
        },
        changeHeader() {
            this.showMask = true;
        },
        selectHeader(item) {
            this.defaultHeader = item;
            this.headerId = item.id;
        },

        submit() {
            if (!this.emile) {
                uni.showToast({
                    title: this.language.openInvoice.qsryx,
                    duration: 1500,
                    icon: "none",
                });
                return;
            }
            let tel = /^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/;
            if (!tel.test(this.emile)) {
                uni.showToast({
                    title: this.language.openInvoice.qsrzqddzyx,
                    duration: 1500,
                    icon: "none",
                });
                return;
            }
           
            if (this.emile.length > 50) {
                uni.showToast({
                    title:  this.language.openInvoice.yxcdbcg,
                    duration: 1500,
                    icon: "none",
                });
                return;
            }
            let data = {
                api: "app.invoiceInfo.applyInvoicing",
                headId: this.headerId,
                type: this.defaultHeader.type,
                companyName: this.defaultHeader.company_name,
                companyTaxNumber: this.defaultHeader.company_tax_number
                    ? this.defaultHeader.company_tax_number
                    : "",
                amount: this.price,
                sNo: this.sNo,
                email: this.emile,
            };
            if (this.id) {
                data.id = this.id;
            }

            this.$req
                .post({ data })
                .then((res) => {
                    if (res.code == 200) {
                        uni.navigateTo({
                            url: "/pagesB/invoice/applySuccess",
                        });
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: "none",
                        });
                    }
                })
                .catch((err) => {
                    uni.showToast({
                        title: err.message,
                        duration: 1500,
                        icon: "none",
                    });
                });
        },
    },
};
</script>

<style lang="less" scoped>
@import url("@/laike.less");
@import url("../../static/css/invoice/openInvoice.less");
</style>
