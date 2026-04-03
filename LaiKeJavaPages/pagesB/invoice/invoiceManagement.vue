<template>
    <view class="container">
        <view class="position_head">
            <heads
                :title="language.invoiceManagement.title"
                returnR="8"
                ishead_w="2"
                :bgColor="bgColor"
                titleColor="#333333"
            ></heads>
            <view class="add_rise_management">
                <view class="rise_block">
                    <view class="rise_management" @tap="addInvoiceHeader">
                        <image :src="add" mode=""></image>
                        <p>{{ language.invoiceManagement.add }}</p>
                    </view>
                </view>
            </view>
            <ul class="nav_header">
                <li
                    class="header_li"
                    :class="{ header_border: status == index }"
                    v-for="(item, index) in language.invoiceManagement.type"
                    :key="index"
                    @tap="nav_index(index)"
                >
                    {{ item }}
                </li>
            </ul>
        </view>
        <ul class="invoiceList" v-if="list.length">
            <li class="invoice_li" v-for="(item, index) in list" :key="index">
                <view class="order_info">
                    <view class="order-head">
                        <p>{{ item.shop_name }}</p>
                        <p @tap="viewDetails(item)">
                            <span>{{ language.invoiceManagement.ckxq }}</span>
                            <image :src="invoice_more" mode=""></image>
                        </p>
                    </view>
                    <view class="order-bottom">
                        <p>
                            {{ language.invoiceManagement.ddsj }}：{{
                                item.add_time
                            }}
                        </p>
                    </view>
                </view>
                <view class="company_name" v-if="status != 0">
                    <p>{{ item.company_name }}</p>
                </view>
                <view class="invoice_li_bottom">
                    <view class="price">
                        <span v-if="status == 0">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(item.z_price) }}</span>
                        <span v-if="status == 1 || status == 2 || status == 3"
                            >{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(item.invoice_amount) }}</span
                        >
                    </view>
                    <view class="btn">
                        <p
                            class="red_btn"
                            v-if="status == 0"
                            @tap="applyInvoice(item)"
                        >
                            {{ language.invoiceManagement.btn[0] }}
                        </p>
                        <p
                            class="gray_btn"
                            v-if="status == 1"
                            @tap="undo(item)"
                        >
                            {{ language.invoiceManagement.btn[1] }}
                        </p>
                        <!-- 查看发票 -->
                        <p
                            class="gray_btn"
                            v-if="status == 2"
                            @tap="_seeImg(item)"
                        >
                            {{ language.invoiceManagement.btn[2] }}
                        </p>
                        <!-- 下载发票 -->
                        <p
                            class="red_btn"
                            v-if="status == 2"
                            @tap="download(item)"
                        >
                            {{ language.invoiceManagement.btn[3] }}
                        </p>
                        <p
                            class="red_btn"
                            v-if="status == 3"
                            @tap="again(item)"
                        >
                            {{ language.invoiceManagement.btn[4] }}
                        </p>
                    </view>
                </view>
            </li>
        </ul>
        <view class="no_header" v-else>
            <image :src="no_header_info" mode="heightFix"></image>
            <p>{{ noTitle }}</p>
        </view>
        <uni-load-more
            v-if="list.length > 10"
            :loadingType="loadingType"
        ></uni-load-more>

        <view class="mask_invoice" v-if="mask_type" @tap.stop="close">
            <div class="content" @tap.stop="">
                <image
                    class="invoice_img"
                    id="code_img"
                    :src="file"
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
        <view class="mask_addInvoice" v-if="isMaskInvoice">
            <view class="mask_invoice_content">
                <view class="invoice_content_top">
                    <p>{{ language.invoiceManagement.kpts }}</p>
                    <p class="content_p">
                        {{ language.invoiceManagement.tips }}
                    </p>
                </view>
                <view class="invoice_content_bottom">
                    <p @tap="isMaskInvoice = false">
                        {{ language.invoiceManagement.btn[5] }}
                    </p>
                    <p @tap="addHeader">
                        {{ language.invoiceManagement.btn[6] }}
                    </p>
                </view>
            </view>
        </view>
        
        <show-toast
            :is_showToast="is_showToast"
            :is_showToast_obj="is_showToast_obj"
        ></show-toast>
    </view>
</template>

<script>
import Vue from 'vue'
import showToast from "@/components/aComponents/showToast.vue";

export default {
    data() {
        return {
            //提示
            is_showToast: 0, //0隐藏弹窗  1成功提示弹窗
            is_showToast_obj: {}, //imgUrl提示图标  title提示文字
            fpClose:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/fp_close.png",
            sus:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/sus.png",
            title: "发票管理",
            add:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/add_rise.png",
            invoice_more:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/invoice_more.png",
            download_invoice:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/download_invoice.png",
            no_header_info:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/no_header_info.png",
            // vip_close.png
            vip_close:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/vip_close.png",
            nav: ["待开票", "申请中", "已完成", "已撤销"],
            status: 0,
            page: 1,
            loadingType: 0,
            list: [],

            mask_type: false,
            qrcode_img: "",
            bgColor: [
                {
                    item: "#ffffff",
                },
                {
                    item: "#ffffff",
                },
            ],
            file: "",

            isMaskInvoice: false,

            isH5: false,
            noTitle: "",
            isShowImg: false,
            is_tsy: false,
            tsy_text: "",
        };
    },
    components: {
        showToast,
    },
    onLoad(option) {
        if (option.status) {
            this.status = option.status;
        }
    },

    onShow() {
        // #ifdef H5
        this.isH5 = true;
        // #endif
        this.noTitle = this.language.invoiceManagement.noTitleList[0];
        this.page = 1;
        this.isMaskInvoice = false;
        if (this.status == 0) {
            this.getOrderList();
        } else {
            this.getInvoiceList();
        }
    },

    /**
     * 上拉触底事件处理
     * */
    onReachBottom() {
        if (this.loadingType != 0) {
            return;
        }
        this.loadingType = 1;
        this.page++;
        if (this.status == 0) {
            this.getOrderList();
        } else {
            this.getInvoiceList();
        }
    },

    methods: {
        /**
         *  查看图片
         */
        _seeImg(item) {
           
            this.file = item.file;
            this.mask_type = true;
            this.isShowImg = true;
        },
        viewDetails(item) {
            let LstoreType = Vue.prototype.LaiKeTuiCommon.getStoreType();
            let me = this;
            let data = {
                access_id: me.access_id,
                order_id: item.order_id,
                api: "app.order.order_details",
                store_type: LstoreType,
            };
            this.$req.post({ data }).then((res) => {
                if (res.code == 200) {
                    uni.navigateTo({
                        url:
                            "/pagesB/order/order?order_id=" +
                            item.order_id +
                            "&types=" +
                            this.status +
                            "&returnR=16",
                    });
                } else {
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: "none",
                    });
                }
            });
        },
        addHeader() {
            uni.navigateTo({
                url: "/pagesB/invoice/addInvoiceHeader",
            });
        },
        _downEWM() {
            let me = this;
            uni.downloadFile({
                url: me.file,
                success(res) {
                    let filePath = res.tempFilePath;
                    uni.saveImageToPhotosAlbum({
                        filePath: filePath,
                        success() {
                            uni.showToast({
                                title: me.language.goods.goodsDet.Save_success,
                                duration: 1500,
                                icon: "none",
                            });
                            me.shareMask = false;
                            me.saveEWM = false;
                        },
                        fail: function () {
                            uni.showToast({
                                title: me.language.goods.goodsDet.fail_success,
                                duration: 1500,
                                icon: "none",
                            });
                        },
                    });
                },
                fail: function () {
                    uni.showToast({
                        title: me.language.goods.goodsDet.fail_success,
                        duration: 1500,
                        icon: "none",
                    });
                },
            });
        },
        handleH5DownImg() {
            let b = new Date().getTime();
            // 解决图片跨域问题，跨域在图片后面拼接一串字符
            let userHeadUrl = this.file + "?b=" + b;

            let image = new Image();
            // 解决跨域 Canvas 污染问题
            image.setAttribute("crossOrigin", "anonymous");
            image.onload = function () {
                let canvas = document.createElement("canvas");
                canvas.width = image.width;
                canvas.height = image.height;
                let context = canvas.getContext("2d");
                context.drawImage(image, 0, 0, image.width, image.height);
                let url = canvas.toDataURL("image/png"); //得到图片的base64编码数据
                let a = document.createElement("a"); // 生成一个a元素
                let event = new MouseEvent("click"); // 创建一个单击事件
                a.download = "发票" || "图片"; // 设置图片名称
                a.href = url; // 将生成的URL设置为a.href属性
                a.dispatchEvent(event); // 触发a的单击事件
            };
            image.src = userHeadUrl;
            setTimeout(() => {
                this.mask_type = false;
                this.isShowImg = false;
            }, 2000);
            
        },
        dow() {
            if (this.isH5) {
                this.handleH5DownImg();
                return false;
                var oA = document.createElement("a");
                oA.download = ""; // 设置下载的文件名，默认是’下载’
                oA.href = this.file;
                document.body.appendChild(oA);
                oA.click();
                oA.remove(); // 下载之后把创建的元素删除
                // #ifdef H5
                // #endif
            } else {
                this._downEWM();
            }
            return false;

            let me = this;
            uni.downloadFile({
                url: this.file,
                success(res) {
                    let filePath = res.tempFilePath;
                    uni.saveImageToPhotosAlbum({
                        filePath: filePath,
                        success() {
                            uni.showToast({
                                title: me.language.store.Tips[4],
                                duration: 1500,
                                icon: "none",
                            });
                            me.shareMask = false;
                            me.saveEWM = false;
                        },
                        fail: function () {
                            uni.showToast({
                                title: me.language.store.Tips[5],
                                duration: 1500,
                                icon: "none",
                            });
                        },
                    });
                },
                fail: function () {
                    uni.showToast({
                        title: me.language.store.Tips[5],
                        duration: 1500,
                        icon: "none",
                    });
                },
            });
        },
        addInvoiceHeader() {
            uni.navigateTo({
                url: "/pagesB/invoice/headerManagement",
            });
        },
        getOrderList() {
            let data = {
                pageNo: this.page,
                pageSize: 10,
                api: "app.invoiceInfo.getToInvoiced",
            };

            this.$req
                .post({ data })
                .then((res) => {
                    if (res.code == 200) {
                        let { list } = res.data;
                        if (this.page == 1) {
                            this.list = list;
                        } else {
                            this.list = this.list.concat(list);
                        }
                        if (list.length < 10) {
                            this.loadingType = 2;
                        } else {
                            this.loadingType = 0;
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
        getInvoiceList() {
            let data = {
                pageNo: this.page,
                pageSize: 10,
                api: "app.invoiceInfo.getInvoiceInfo",
                status: this.status,
            };

            this.$req
                .post({ data })
                .then((res) => {
                    if (res.code == 200) {
                        let { list } = res.data;
                        if (this.page == 1) {
                            this.list = list;
                        } else {
                            this.list = this.list.concat(list);
                        }
                        if (list.length < 10) {
                            this.loadingType = 2;
                        } else {
                            this.loadingType = 0;
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
        nav_index(index) {
            this.page = 1;
            this.status = index;
            this.list = [];
            this.$nextTick(() => {
                if (index == 0) {
                    this.noTitle =
                        this.language.invoiceManagement.noTitleList[0];
                } else if (index == 1) {
                    this.noTitle =
                        this.language.invoiceManagement.noTitleList[1];
                } else if (index == 2) {
                    this.noTitle =
                        this.language.invoiceManagement.noTitleList[2];
                } else {
                    this.noTitle =
                        this.language.invoiceManagement.noTitleList[3];
                }
            });
            if (index == 0) {
                this.getOrderList();
            } else {
                this.getInvoiceList();
            }
        },
        applyInvoice(item) {
            let data = {
                api: "app.invoiceHeader.getDefault",
            };
            this.$req.post({ data }).then((res) => {
                if (res.data && res.data.id) {
                    uni.navigateTo({
                        url:
                            "/pagesB/invoice/openInvoice?sNo=" +
                            item.sNo +
                            "&price=" +
                            item.z_price,
                    });
                } else {
                    this.isMaskInvoice = true;
                }
            });
        },
        again(item) {
            if (item.invoiceTimeout) {
                uni.showToast({
                    title: "订单已超时，不可重新开票",
                    duration: 1500,
                    icon: "none",
                });
                return;
            }
            uni.navigateTo({
                url: "/pagesB/invoice/openInvoice?id=" + item.id,
            });
        },
        undo(item) {
            let data = {
                api: "app.invoiceInfo.revoke",
                id: item.id,
            };

            this.$req
                .post({ data })
                .then((res) => {
                    if (res.code == 200) {
                       
                        this.is_showToast = 1;
                        this.is_showToast_obj.imgUrl = this.sus;
                        this.is_showToast_obj.title = this.language.zdata.cxcg;
                        setTimeout(() => {
                            this.is_showToast = 0;
                        }, 1500);
                        if (this.status == 0) {
                            this.getOrderList();
                        } else {
                            this.getInvoiceList();
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
        download(item) {
            this.file = item.file;
            this.mask_type = true;
            this.tsy_text = item.company_name;
        },
        close() {
            this.mask_type = false;
            this.isShowImg = false;
        },
    },
};
</script>

<style lang="less" scoped>
@import url("@/laike.less");
@import url("../../static/css/invoice/invoiceManagement.less");
/deep/.uni-body
    .uni-system-preview-image
    uni-swiper
    .uni-swiper-navigation-hide {
    opacity: 0 !important;
}
</style>
