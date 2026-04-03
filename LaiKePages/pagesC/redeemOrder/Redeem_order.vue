<template>
    <view>
        <view class="pages">
            <heads
                :title="language.Redeem_order.title"
                :border="true"
                :bgColor="bgColor"
                titleColor="#000"
                ishead_w="2"
            ></heads>
            <view class="search">
                <view>
                    <view class="search_input">
                        <image class="searchImg" :src="serchimg" alt=""></image>
                        <input
                            v-model="name"
                            type="text"
                            :placeholder="language.Redeem_order.shuddh"
                        />
                        <image
                            v-show="name.length > 0"
                            @click="cleardata"
                            class="cancel"
                            :src="sc_icon"
                            mode=""
                        >
                        </image>
                    </view>
                    <view class="search_btn" @tap="_search">{{
                        language.choosePro.searchBtn
                    }}</view>
                </view>
            </view>
            <ul class="order_header">
                <li
                    class="header_li"
                    :class="{ header_border: status_id == index }"
                    v-for="(item, index) in header"
                    :key="item.id"
                    @tap="_header_index(index)"
                >
                    <span v-if="item == '全部'">{{
                        language.Redeem_order.qb
                    }}</span>
                    <span v-if="item == '待付款'">{{
                        language.Redeem_order.dfk
                    }}</span>
                    <span v-if="item == '待发货'">{{
                        language.Redeem_order.dfh
                    }}</span>
                    <span v-if="item == '退款/售后'">{{
                        language.Redeem_order.tksh
                    }}</span>
                </li>
            </ul>

            <view class="pages_box">
                <view
                    class="pages_box_box"
                    v-for="(item, index) in list"
                    :key="index"
                >
                    <view class="pages_box_box_top"> 
                        <view class="pages_box_box_top_dd order-id"
                            >{{ language.Redeem_order.ddh }}：{{
                                item.orderno
                            }}</view
                        >
                        <view class="pages_box_box_top_zt">
                            <div v-if="status == '2'">
                                {{item.prompt}}
                            </div>
                            <div v-else>
                                <span v-if="item.orderStatus == 0">{{language.storeMyOrder.obligation}}</span>
                                <span v-else-if="item.orderStatus == 1 && (item.self_lifting == '0'|| item.self_lifting == '2')">{{language.storeMyOrder.waitSend}}</span>
                                <span v-else-if="item.orderStatus == 2">{{language.storeMyOrder.waitReceiving}}</span>
                                <span v-else-if="item.orderStatus == 5">{{language.storeMyOrder.done}}</span>
                                <span v-else-if="item.orderStatus == 7">{{language.storeMyOrder.orderClosed}}</span>
                                <span v-else-if="item.orderStatus == 8">待核销</span>
                                <span v-else>
                                    <span v-if="item.r_type == 0">{{language.storeMyOrder.returnReview}}</span>
                                    <span v-else-if="item.r_type == 1 && item.re_type == 3">{{language.storeMyOrder.agreeReplacement}}</span>
                                    <span v-else-if="item.r_type == 1">{{language.storeMyOrder.agreeReturn}}</span>
                                    <span v-else-if="item.r_type == 2">{{language.storeMyOrder.refusedReturn}}</span>
                                    <span v-else-if="item.r_type == 3">{{language.storeMyOrder.receiveMerchant}}</span>
                                    <span v-else-if="item.r_type == 4">{{language.storeMyOrder.agreeRefund}}</span>
                                    <span v-else-if="item.r_type == 5">{{language.storeMyOrder.rejectReturn}}</span>
                                    <span v-else-if="item.r_type == 8">{{language.storeMyOrder.refusedRefund}}</span>
                                    <span v-else-if="item.r_type == 9">{{language.storeMyOrder.agreeAndRefund}}</span>
                                    <span v-else-if="item.r_type == 11">{{language.storeMyOrder.sendMerchandise}}</span>
                                    <span v-else-if="item.r_type == 12">{{language.storeMyOrder.afterComplete}}</span>
                                </span>
                            </div>
                        </view>
                    </view>
                    <view class="pages_box_box_body" @click="to_detal(item)">
                        <view class="pages_box_box_left">
                            <image
                                :src="item.goodsImgUrl"
                                style="
                                    width: 164rpx;
                                    height: 164rpx;
                                    border-radius: 16rpx;
                                "
                            >
                            </image>
                        </view>
                        <view class="pages_box_box_body_right">
                            <view class="pages_box_box_body_right_title">
                                {{ item.goodsName }}</view
                            >
                            <view class="pages_box_box_body_rigth_gg">{{
                                item.attrStr
                            }}</view>
                            <view class="pages_box_box_body_right_price">
                                <view class="pages_box_box_body_right_price_jf"
                                    >{{ language.Redeem_order.jf
                                    }}{{ item.orderAllow }}+{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{
                                        item.after_discount
                                    }}</view
                                > 
                                <view class="pages_box_box_body_right_price_num"
                                    >{{ language.Redeem_order.g }}{{ item.num
                                    }}{{ language.Redeem_order.j }}</view
                                >
                            </view>
                        </view>
                    </view>

                    <view class="pages_box_box_jw">
                        <view v-if="status != '2'" class="pages_box_box_jw_sj">{{
                            item.createDate
                        }}</view>
                        <div v-else style="display: flex;align-items: center;font-size: 28rpx;">
                            <img v-if="item.re_type == 2 || item.r_type == 4 || item.r_type == 9|| item.r_type == 15" :src="after_tk" class="after_icon">
                            <img v-if="item.re_type == 3 || item.r_type == 12  && item.r_type != 4" :src="after_hh" class="after_icon">
                            <img v-if="item.re_type == 1 && item.r_type != 4 "  :src="after_th" class="after_icon">
    
                            {{ item.re_type == 1 ? language.afterSale.return_types[0] : item.re_type == 2 ? language.afterSale.return_types[1] : item.re_type == 3 && item.r_type == 12 ? language.afterSale.return_types[2] : language.afterSale.return_types[3] }}
                            {{ item.r_type == 4 || item.r_type == 9|| item.r_type == 15 ? language.afterSale.return_end : '' }}
                        </div>
                        <view class="pages_box_box_jw_btn">
                            <view class="btn" @tap="toReturn(item.sNo, item.re_id)" v-if="(item.r_type == 0||item.r_type == 1||item.r_type == 3) && status">{{language.storeMyOrder.audit}}</view>
                            <div class="btn" v-if="status == '2' && item.r_type != 0"  @tap="toReDetail(item.sNo, item.re_id)">{{language.afterSale.details}}</div>
                            <view
                                class="btn"
                                v-if="item.status == '待付款'"
                                @click="toAddPro(item.id)"
                                >{{ language.Redeem_order.gbdd }}</view
                            >
                            <view
                                class="btn"
                                v-if="
                                    item.status == '待付款' ||
                                    item.status == '待发货'
                                "
                                @click="tobjdd(item)"
                                >{{ language.Redeem_order.bjdd }}</view
                            >
                            <!-- 发货位置 已经移动到详情页当中了 -->
                           <!-- <view
                                class="btn"
                                v-if="item.status == '待发货'"
                                @click="tofahuo(item.detailId,item.orderno,item.num)"
                                >{{ language.Redeem_order.fh }}</view
                            > -->
                            <view
                                class="btn"
                                v-if="(item.status == '订单完成' ||
                                    item.status == '待收货'||item.status == '已完成' ) && status != '2'
                                    
                                "
                                @click="towuliu(item.orderno)"
                                >{{ language.Redeem_order.ckwl }}</view
                            >
                        </view>
                    </view>

                    <!-- 提示 -->
                    <view
                        class="xieyi"
                        style="background-color: initial; z-index: 9999"
                        v-if="is_sus"
                    >
                        <view
                            style="
                                width: 272rpx;
                                height: 272rpx;
                                background-color: rgba(51, 51, 51, 0.9);
                            "
                        >
                            <view
                                style="
                                    margin: 32rpx 0;
                                    text-align: center;
                                    margin-top: 64rpx;
                                "
                            >
                                <image
                                    style="width: 68rpx; height: 68rpx"
                                    :src="sus"
                                ></image>
                            </view>
                            <view
                                class="xieyi_title"
                                style="
                                    margin-bottom: 0;
                                    margin-top: 0;
                                    color: #fff;
                                    font-weight: 500;
                                    font-size: 32rpx;
                                "
                                >关闭成功</view
                            >
                        </view>
                    </view>

                    <!-- 删除提示框 -->
                    <view class="tishi_bg" v-if="is_tishi">
                        <view class="tishi_nr">
                            <view>{{ language.Redeem_order.tips[0] }}</view>
                            <view>{{ language.Redeem_order.tips[1] }}</view>
                            <view class="tishi_btn">
                                <view @tap="_quxiao">{{
                                    language.Redeem_order.tips[2]
                                }}</view>
                                <view @tap="_queren">{{
                                    language.Redeem_order.tips[3]
                                }}</view>
                            </view>
                        </view>
                    </view>

                    <!-- 关闭订单提示框 -->
                    <view class="tishi_bg" v-if="is_guanbi">
                        <view class="tishi_nr">
                            <view>{{ language.Redeem_order.tipsa[0] }}</view>
                            <view>{{ language.Redeem_order.tipsaa[1] }}</view>
                            <view class="tishi_btn">
                                <view @tap="_quxiaoa">{{
                                    language.Redeem_order.tipsa[2]
                                }}</view>
                                <view @tap="_querena">{{
                                    language.Redeem_order.tipsa[3]
                                }}</view>
                            </view>
                        </view>
                    </view>
                </view>
                <div v-if="list.length < 1">
                    <div class="noFindDiv">
                        <div><img class="noFindImg" :src="noOrder" /></div>
                        <span class="noFindText">{{
                            language.order.myorder.no_order
                        }}</span>
                    </div>
                </div>
            </view>
        </view>
    </view>
</template>

<script>
export default {
    data() {
        return {
            bgColor: [
                {
                    item: "#ffffff",
                },
                {
                    item: "#ffffff",
                },
            ],
            list: [],
            is_tishi: false,
            loadingType: 0,
            is_guanbi: false,
            is_sus: false,
            id: "", //订单id
            status_id: "0", //订单状态
            order_type: "", //订单类型
            noOrder:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/noOrder.png",
            serchimg:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/searchNew.png",
            sus:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/sus.png",
            name: "",
            after_tk: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/tuikuan2x.png',
            after_th: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/tuihuo2x.png',
            after_hh: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/huanhuo.png',
            status: "",
            pageNo: 1,
            header: ["全部", "待付款", "待发货", "退款/售后"],
            sc_icon:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/delete2x.png",
        };
    },
    onLoad(e) {
        this._axios();
    },
    onShow() {
        // this.status_id = 0
        this._axios();
        this.pageNo = 1;
    },
    onReachBottom() {
        if (this.loadingType == 1 || this.loadingType == 2) {
            return false;
        }
        this.loadingType = 1;
        this.pageNo++;
        this._axios();
    },
    methods: {
        cleardata() {
            this.name = "";
        },
        tobjdd(e) {
            uni.navigateTo({
                url:
                    "/pagesC/pointsDetails/Points_details?sNo=" +
                    e.orderno +
                    "&is_bj=true",
            });
        },
        _search() {
            this.pageNo = 1
            this._axios();
        },
        _axios() {
            var data = {
                api: "plugin.integral.AppIntegral.orderIndex",
                pageNo: this.pageNo,
                keyWord: this.name,
                status: this.status,
            };
            if (this.status != 0 && this.status != 2) {
                data.status = this.status;
            }
            if (this.status == 2) {
                data.order_type = this.order_type;
            }
            this.$req.post({ data }).then((res) => {
                if (res.code == 200) {
                    if (this.pageNo == 1) {
                        this.list = res.data.list;
                    } else {
                        this.list.push(...res.data.list);
                    }
                    if (res.data.list.length > 0) {
                        this.loadingType = 0;
                    } else {
                        this.loadingType = 2;
                    }
                } else {
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: "none",
                    });
                }
            });
        },
        tofahuo(e,orderno,num) {
            uni.navigateTo({
                url:
                    "/pagesD/synthesize/yushou_fahuowuliu?orderDetailIds=" +
                    e +
                    "&is_integral=true&sNo="+orderno +
                    "&num="+num,
            });
        },
        towuliu(e) {
            uni.navigateTo({
                url:
                    "/pagesC/expressage/expressage?sNo=" +
                    e +
                    "&is_integral=true",
            });
        },
        to_detal(e) {
            uni.navigateTo({
                url: "/pagesC/pointsDetails/Points_details?sNo=" + e.orderno,
            });
        },
        _header_index(index) {
            this.status_id = index;
            this.pageNo = 1;
            switch (index) {
                case 0:
                    this.status = "";
                    break;
                case 1:
                    this.status = 0;
                    break;
                case 2:
                    this.status = 1;
                    break;
                case 3:
                    this.status = 2;
                    this.order_type = 'return';
                    break;
            }
            this._axios();
        },
        _quxiao() {
            this.is_tishi = false;
        },
        _quxiaoa() {
            this.is_guanbi = false;
        },
        _queren() {
            this.is_sus = true;
            this.is_tishi = false;
            setTimeout(() => {
                this.is_sus = false;
            }, 1500);
        },
        _querena(e) {
            var data = {
                api: "plugin.mch.integral.order.closeOrder",
                id: this.id,
            };
            this.$req.post({ data }).then((res) => {
                if (res.code == 200) {
                    this.is_guanbi = false;
                    this._axios();
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: "none",
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
        toAddPro(e) {
            // this.is_tishi = true
            this.id = e;
            this.is_guanbi = true;
        },
        // 审核
        toReturn(sNo, order_details_id) {
            uni.navigateTo({
                url: '/pagesA/myStore/returnDetail?type=audit&&sNo=' + sNo + '&order_details_id=' + order_details_id
            });
        },
        toReDetail(sNo, order_details_id){
            uni.navigateTo({
                url: '/pagesA/myStore/returnDetail?sNo=' + sNo + '&order_details_id=' + order_details_id
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
@import url("@/laike.less");
.after_icon{
    width: 36rpx;
    height: 36rpx;
    margin-right: 10rpx;
}
.order-id{
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    width: 560rpx;
}
.tishi_bg {
    position: fixed;
    top: 0;
    bottom: 0;
    left: 0;
    right: 0;
    z-index: 999;
    background-color: rgba(0, 0, 0, 0.1);
    .tishi_nr {
        width: 640rpx;
        height: 358rpx;
        background: #ffffff;
        border-radius: 24rpx;
        margin: 0 auto;
        position: relative;
        top: 50%;
        transform: translateY(-160rpx);
        overflow: hidden;
        > view:nth-child(1) {
            font-size: 32rpx;
            font-weight: 500;
            color: #333333;
            line-height: 44rpx;
            margin-top: 64rpx;
            text-align: center;
        }
        > view:nth-child(2) {
            font-size: 32rpx;
            font-weight: 400;
            color: #999999;
            line-height: 44rpx;
            margin-top: 32rpx;
            margin-bottom: 66rpx;
            text-align: center;
        }
        .tishi_btn {
            width: 100%;
            height: 106rpx;
            border-top: 2rpx solid rgba(0, 0, 0, 0.1);
            display: flex;
            > view {
                flex: 1;
                display: flex;
                align-items: center;
                justify-content: center;

                font-size: 32rpx;
                font-weight: 500;
                color: #d73b48;
                line-height: 44rpx;
            }
            > view:first-child {
                border-right: 2rpx solid rgba(0, 0, 0, 0.1);
                color: #333333;
            }
        }
    }
}
.xieyi {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
    z-index: 99;
    display: flex;
    justify-content: center;
    align-items: center;
    > view {
        width: 640rpx;
        min-height: 100rpx;
        max-height: 486rpx;
        background: #ffffff;
        border-radius: 24rpx;
        .xieyi_btm {
            height: 108rpx;
            color: @D73B48;
            display: flex;
            justify-content: center;
            align-items: center;
            border-top: 0.5px solid rgba(0, 0, 0, 0.1);
            font-weight: bold;
            font-size: 32rpx;
        }
        .xieyi_title {
            display: flex;
            justify-content: center;
            align-items: center;
            font-size: 32rpx;

            font-weight: 500;
            color: #333333;
            line-height: 44rpx;
            margin-top: 64rpx;
            margin-bottom: 32rpx;
            font-weight: bold;
            font-size: 32rpx;
        }
        .xieyi_text {
            width: 100%;
            max-height: 236rpx;
            overflow-y: scroll;
            padding: 0 32rpx;
            box-sizing: border-box;
        }
    }
}
.btn {
    width: 144rpx !important;
    height: 56rpx;
    line-height: 56rpx;
    text-align: center;
    border-radius: 52rpx;
    font-size: 24rpx;

    font-weight: normal;
    color: #333333;
    background: #f4f5f6;
    margin-left: 24rpx;
}
.pages_box_box_jw_btn {
    display: flex;
}
.pages_box_box_jw_sj {
    font-size: 28rpx;

    font-weight: normal;
    color: #999999;
}
.pages_box_box_jw {
    margin-top: 26rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.pages_box_box_body_right_price_num {
    font-size: 24rpx;

    font-weight: normal;
    color: #999999;
}
.pages_box_box_body_right_price_jf {
    font-size: 32rpx;
    font-weight: 500;
    color: #fa5151;
}
.pages_box_box_body_right_price {
    margin-top: 24rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.pages_box_box_body_rigth_gg {
    font-size: 24rpx;

    font-weight: normal;
    color: #999999;
    margin-top: 20rpx;
}
.pages_box_box_body_right_title {
    font-size: 32rpx;

    font-weight: normal;
    color: #333333;
    width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
}

.pages_box_box_body_right {
    width: 498rpx;
    margin-left: 24rpx;
    display: flex;
    flex-direction: column;
}

.pages_box_box_left {
    width: 164rpx;
    height: 164rpx;
}

.pages_box_box_body {
    margin-top: 26rpx;
    display: flex;
}

.pages_box_box_top_zt {
    font-size: 24rpx;

    font-weight: normal;
    color: #fa5151;
}

.pages_box_box_top_dd {
    font-size: 28rpx;

    font-weight: normal;
    color: #333333;
}

.pages_box_box_top {
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.pages_box_box {
    // width: 100%;
    display: flex;
    padding: 32rpx;
    flex-direction: column;
    background-color: #fff;
    border-radius: 24rpx;
    margin-bottom: 16rpx;
}

.pages_box {
    margin-top: 24rpx;
    width: 100%;
    display: flex;
    flex-direction: column;
}

.noFindDiv {
    width: 100%;
    padding-top: 198rpx;
    height: 100%;
    background-color: #f4f5f6;

    .noFindText {
        color: #888888;
    }

    .noFindImg {
        width: 750rpx;
        height: 394rpx;
    }
}

.header_border {
    // color: @btnBackground;
    font-weight: bold;
    color: #333333;
    font-size: 32rpx;
}

.header_border::after {
    position: absolute;
    content: "";
    // width: 40rpx;
    // height: 6rpx;
    // background: @btnBackground;
    left: 50%;
    transform: translateX(-50%);
    bottom: 0;
    // border-radius: 5rpx;

    width: 64rpx;
    height: 4rpx;
    background: #fa5151;
    border-radius: 4rpx;
}

.header_li {
    width: 33%;
    text-align: center;
    height: 70rpx;
    line-height: 70rpx;
    font-size: 32rpx;
    font-weight:600;
    position: relative;
}

.order_header,
.order_header {
    justify-content: space-around;
    width: 100%;
    padding-bottom: 24rpx;
    border-radius: 0px 0px 24rpx 24rpx;
    display: flex;
    background-color: #fff;
    color: #999999;
}

.pages {
    width: 100%;
}

.search {
    height: 108rpx;
    background-color: #fff;
    // border-radius: 0 0 24rpx 24rpx;
    display: flex;
    justify-content: center;
    align-items: center;

    > view {
        display: flex;
        align-items: center;
        width: 100vw;
        padding: 4rpx 8rpx 18rpx;
        overflow: hidden;
        // position: fixed;
        z-index: 99;

        .search_input {
            position: relative;
            display: flex;
            align-items: center;
            padding: 0 57rpx;
            width: 542rpx;
            margin-left: 24rpx;
            height: 68rpx;
            background: rgba(243, 243, 243, 1);
            border-radius: 35rpx;
            box-sizing: border-box;

            input {
                font-size: 24rpx;
                margin-left: 8rpx;
            }

            .cancel {
                position: absolute;
                top: 50%;
                transform: translateY(-50%);
                width: 30rpx;
                height: 30rpx;
                right: 20rpx;
            }
        }

        .searchImg {
            width: 30rpx;
            height: 30rpx;
            left: 20rpx;
            top: 50%;
            transform: translateY(-50%);
        }

        .search_btn {
            // padding: 0 30rpx 0 18rpx;
            width: 120rpx;
            height: 64rpx;
            text-align: center;
            line-height: 64rpx;
            border-radius: 32px;
            font-size: 28rpx;

            font-weight: 500;
            color: #fa5151;
            // background-color: #feeded;
            border: 1rpx solid #eee;
            color: #333333;
            white-space: nowrap;
            margin-left: 24rpx;
        }
    }
}
</style>
