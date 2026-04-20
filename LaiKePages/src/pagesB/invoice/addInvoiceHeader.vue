<template>
    <view class="container">
        <view class="position_head">
            <heads
                :title="
                    id
                        ? language.headerManagement.edit
                        : language.addInvoiceHeader.title
                "
                ishead_w="2"
                :bgColor="bgColor"
                titleColor="#333333"
            ></heads>
        </view>
        <view class="content">
            <view class="header_type">
                <p class="header_type_left">
                    {{ language.addInvoiceHeader.ttlx }}
                </p>
                <view class="header_type_right">
                    <p @tap="changeType(1)">
                        <image
                            :src="
                                selectType == 1
                                    ? select_header
                                    : no_select_header
                            "
                            mode=""
                        ></image>
                        <span>{{ language.addInvoiceHeader.dw }}</span>
                    </p>
                    <p @tap="changeType(2)">
                        <image
                            :src="
                                selectType == 2
                                    ? select_header
                                    : no_select_header
                            "
                            mode=""
                        ></image>
                        <span>{{ language.addInvoiceHeader.grkp }}</span>
                    </p>
                </view>
            </view>
            <view class="header_info">
                <ul class="enterprise" v-if="selectType == 1">
                    <li>
                        <p class="header_info_left">
                            <span>*</span>{{ language.addInvoiceHeader.mc }}
                        </p>
                        <view class="header_info_right">
                            <input
                                type="text"
                                placeholder-style="color:999999"
                                :placeholder="language.addInvoiceHeader.shumc"
                                v-model="companyName"
                            />
                        </view>
                    </li>
                    <li>
                        <p class="header_info_left">
                            <span>*</span>{{ language.addInvoiceHeader.gs }}
                        </p>
                        <view class="header_info_right">
                            <input
                                type="text"
                                placeholder-style="color:999999"
                                :placeholder="language.addInvoiceHeader.shugs"
                                @input="numberFixedDigit3"
                                v-model="companyTaxNumber"
                            />
                        </view>
                    </li>
                    <li>
                        <p class="header_info_left">
                            {{ language.addInvoiceHeader.dz }}
                        </p>
                        <view class="header_info_right">
                            <input
                                type="text"
                                placeholder-style="color:999999"
                                :placeholder="language.addInvoiceHeader.shudz"
                                v-model="registerAddress"
                            />
                        </view>
                    </li>
                    <li>
                        <p class="header_info_left">
                            {{ language.addInvoiceHeader.dh }}
                        </p>
                        <view class="header_info_right">
                            <input
                                placeholder-style="color:999999"
                                @blur="numberFixedDigit2"
                                :placeholder="language.addInvoiceHeader.shudh"
                                v-model="registerPhone"
                            />
                        </view>
                    </li>
                    <li>
                        <p class="header_info_left">
                            {{ language.addInvoiceHeader.khyh }}
                        </p>
                        <view class="header_info_right">
                            <input
                                type="text"
                                placeholder-style="color:999999"
                                :placeholder="language.addInvoiceHeader.shukhyh"
                                v-model="depositBank"
                            />
                        </view>
                    </li>
                    <li>
                        <p class="header_info_left">
                            {{ language.addInvoiceHeader.yhzh }}
                        </p>
                        <view class="header_info_right">
                            <input
                                type="number"
                                placeholder-style="color:999999"
                                @input="numberFixedDigit"
                                :placeholder="language.addInvoiceHeader.shuyhzh"
                                v-model="bankNumber"
                            />
                        </view>
                    </li>
                    <li>
                        <p class="header_info_left">
                            {{ language.addInvoiceHeader.swmr }}
                        </p>
                        <view class="header_info_right">
                            <switch
                                :checked="isDefault"
                                color="#FA5151"
                                @change="changeIsDefault"
                            />
                        </view>
                    </li>
                </ul>
                <ul class="personal" v-if="selectType == 2">
                    <li>
                        <p class="header_info_left">
                            <span>*</span>{{ language.addInvoiceHeader.ttmc }}
                        </p>
                        <view class="header_info_right">
                            <input
                                type="text"
                                placeholder-style="color:999999"
                                :placeholder="language.addInvoiceHeader.shuttmc"
                                v-model="headerName"
                            />
                        </view>
                    </li>
                    <li>
                        <p class="header_info_left">
                            {{ language.addInvoiceHeader.swmr }}
                        </p>
                        <view class="header_info_right">
                            <switch
                                :checked="isDefault2"
                                color="#FA5151"
                                @change="changeIsDefault2"
                            />
                        </view>
                    </li>
                </ul>
            </view>
        </view>
        <view class="btn">
            <p @tap="submit">{{ language.addInvoiceHeader.bc }}</p>
        </view>
        <show-toast
            :is_showToast="is_showToast"
            :is_showToast_obj="is_showToast_obj"
        ></show-toast>
    </view>
</template>

<script>
import showToast from "@/components/aComponents/showToast.vue";
export default {
    data() {
        return {
            //提示
            is_showToast: 0, //0隐藏弹窗  1成功提示弹窗
            is_showToast_obj: {}, //imgUrl提示图标  title提示文字
            sus:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/sus.png",
            title: "添加发票抬头",
            select_header:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/select_header.png",
            no_select_header:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/no_select_header.png",
            selectType: 1,

            companyName: "",
            companyTaxNumber: "",
            registerAddress: "",
            registerPhone: "",
            depositBank: "",
            bankNumber: "",
            isDefault: true,

            headerName: "",
            isDefault2: true,

            id: "",

            bgColor: [
                {
                    item: "#ffffff",
                },
                {
                    item: "#ffffff",
                },
            ],
        };
    },
    components: {
        showToast,
    },
    onLoad(option) {
        if (option.id) {
            this.id = option.id;
        }
    },

    onShow() {
        if (this.id) {
            this.getHeaderInfo();
        }
    },

    methods: {
        numberFixedDigit(e) {
            // 只能输入整数
            e.target.value = e.target.value.replace(/^0|[^\d]|[.]/g, "");
            this.$nextTick(() => {
                this.bankNumber = e.target.value;
            });
        },
        numberFixedDigit2(e) {
            let mobile = this.registerPhone;
            let tel = /^(?:(?:\d{3}-)?\d{8}|^(?:\d{4}-)?\d{7,8})(?:-\d+)?$/;
            let phone =
                /^(((13[0-9]{1})|(15[0-9]{1})|(18[0-9]{1})|(16[0-9]{1})|(17[0-9]{1})|(19[0-9]{1}))+\d{8})$/;
            if (mobile.length == 11) {
                //手机号码
                if (phone.test(mobile)) {
                    return true;
                }
            } else if (tel.test(mobile)) {
                return true;
            }
            this.$nextTick(() => {
                this.registerPhone = e.target.value = "";
            });
        },
        numberFixedDigit3(e) {
            e.target.value = e.target.value.replace(/[^\w]/g, "");
            this.$nextTick(() => {
                this.companyTaxNumber = e.target.value;
            });
        },
        changeIsDefault(value) {
            this.isDefault = value.detail.value;
        },
        changeIsDefault2(value) {
            this.isDefault2 = value.detail.value;
        },
        getHeaderInfo() {
            let data = {
                api: "app.invoiceHeader.getList",
                id: this.id,
            };

            this.$req
                .post({ data })
                .then((res) => {
                    let info = res.data.list[0];
                    if (res.code == 200) {
                        this.selectType = info.type;
                        if (this.selectType == 1) {
                            this.companyName = info.company_name;
                            this.companyTaxNumber = info.company_tax_number;
                            this.registerAddress = info.register_address;
                            this.registerPhone = info.register_phone;
                            this.depositBank = info.deposit_bank;
                            this.bankNumber = info.bank_number;
                            this.isDefault = info.is_default ? true : false;
                        } else {
                            this.headerName = info.company_name;
                            this.isDefault2 = info.is_default ? true : false;
                        }
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
        changeType(value) {
            this.selectType = value;
        },
        submit() {
            if (this.selectType == 1) {
                if (!this.companyName) {
                    uni.showToast({
                        title: this.language.addInvoiceHeader.shumc,
                        duration: 1500,
                        icon: "none",
                    });
                    return;
                }
                if (!this.companyTaxNumber) {
                    uni.showToast({
                        title: this.language.addInvoiceHeader.shugs,
                        duration: 1500,
                        icon: "none",
                    });
                    return;
                }
                if (this.companyName.length > 50) {
                    uni.showToast({
                        title: this.language.addInvoiceHeader.gsmcbcg,
                        duration: 1500,
                        icon: "none",
                    });
                    return;
                }
                if (this.companyTaxNumber.length > 50) {
                    uni.showToast({
                        title: this.language.addInvoiceHeader.gsshbcg,
                        duration: 1500,
                        icon: "none",
                    });
                    return;
                }
                let data = {
                    api: "app.invoiceHeader.addOrUpdate",
                    id: this.id,
                    type: 1,
                    companyName: this.companyName,
                    companyTaxNumber: this.companyTaxNumber,
                    registerAddress: this.registerAddress,
                    depositBank: this.depositBank,
                    bankNumber: this.bankNumber,
                    registerPhone: this.registerPhone,
                    isDefault: this.isDefault ? 1 : 0,
                };

                this.$req
                    .post({ data })
                    .then((res) => {
                        if (res.code == 200) {
                            this.is_showToast = 1;
                            this.is_showToast_obj.imgUrl = this.sus;
                            this.is_showToast_obj.title = this.id
                                ? this.language.zdata.bjcg
                                : this.language.zdata.tjcg;
                            setTimeout(() => {
                                this.is_showToast = 0;
                                uni.navigateBack({
                                    delta: 1,
                                });
                            }, 1000);
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
            } else {
                if (!this.headerName) {
                    uni.showToast({
                        title: "请输入抬头名称",
                        duration: 1500,
                        icon: "none",
                    });
                    return;
                }
                if (this.headerName.length > 50) {
                    uni.showToast({
                        title: "抬头名称长度不能超过50个字符",
                        duration: 1500,
                        icon: "none",
                    });
                    return;
                }
                let data = {
                    api: "app.invoiceHeader.addOrUpdate",
                    id: this.id,
                    type: 2,
                    companyName: this.headerName,
                    isDefault: this.isDefault2 ? 1 : 0,
                };

                this.$req
                    .post({ data })
                    .then((res) => {
                        if (res.code == 200) {
                            this.is_showToast = 1;
                            this.is_showToast_obj.imgUrl = this.sus;
                            this.is_showToast_obj.title = this.id
                                ? this.language.zdata.bjcg
                                : this.language.zdata.tjcg;
                            setTimeout(() => {
                                this.is_showToast = 0;
                                uni.navigateBack({
                                    delta: 1,
                                });
                            }, 1000);
                        }
                    })
                    .catch((err) => {
                        uni.showToast({
                            title: err.message,
                            duration: 1500,
                            icon: "none",
                        });
                    });
            }
        },
    },
};
</script>

<style lang="less" scoped>
@import url("@/laike.less");
@import url("../../static/css/invoice/addInvoiceHeader.less");
</style>
