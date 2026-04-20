<template>
    <view>
        <heads
            :title="'订单详情'"
            :ishead_w="ishead_w"
            :bgColor="bgColor"
            :titleColor="titleColor"
            :returnR="returnR"
            :types="types"
            :order_type="order_type"
        ></heads>
        <view class="pages">
            <view class="pages_top">
                <view class="pages_top_zt">{{ datalist.status }}</view>
                <view class="pages_top_ddh">
                    <view class="pages_top_ddh_left"
                        >{{ language.yushou_order.ddh }}：{{
                            datalist.id
                        }}</view
                    >
                    <view
                        class="pages_top_ddh_right"
                        @click="copyLink(datalist.id)"
                        >{{ language.order.order.copy }}</view
                    >
                </view>
                <view class="pages_top_time"
                    >{{ language.order.order.Order_time }}：{{
                        datalist.add_time
                    }}</view
                >
            </view>

            <view class="pages_list">
                <view class="pages_list_box">
                    <view
                        :class="
                            is_bj
                                ? 'pages_list_box_name'
                                : 'pages_list_box_namea'
                        "
                    >
                        <view class="pages_list_box_name_txt"
                            ><span style="color: #fa5151"></span
                            >{{ language.mystore_order.Buyer }}：</view
                        >
                        <view
                            class="pages_list_box_name_input"
                            style="text-align: inherit; border: none"
                            >{{ datalist.user_name }}</view
                        >
                    </view>
                    <view class="pages_list_box_name" v-if="is_bj">
                        <view class="pages_list_box_name_txt">{{
                            language.mystore_order.username
                        }}</view>
                        <input
                            class="pages_list_box_name_input"
                            v-model="name"
                        />
                    </view>
                    <view class="pages_list_box_name" v-if="is_bj">
                        <view class="pages_list_box_name_txt">{{
                            language.afterSale.phone
                        }}</view>
                        <input
                            class="pages_list_box_name_input"
                            v-model="modle"
                        />
                    </view>
                    <view class="pages_list_box_name" v-if="is_bj">
                        <view class="pages_list_box_name_txt">{{
                            language.storeModifyAdress.area
                        }}</view>
                        <div class="_input" @tap="showMulLinkageThreePicker">
                            <input
                                type="text"
                                disabled="true"
                                :placeholder="
                                    language.mystore_order.address_placeholder
                                "
                                placeholder-style="color:#B8B8B8"
                                @focus="hideKeyboard()"
                                v-model="address"
                            />
                            <img :src="jiantou" class="jiantou" />
                        </div>
                    </view>
                    <view class="pages_list_box_name" v-if="is_bj">
                        <view class="pages_list_box_name_txt">{{
                            language.mystore_order.addressinfo
                        }}</view>
                        <input
                            class="pages_list_box_name_input"
                            v-model="r_address"
                        />
                    </view>
                </view>
            </view>
            <!-- 查看订单才会显示 -->
            <view class="dizhi" v-if="!is_bj">
                <view class="dizhi_xx">{{ r_address }}</view>
                <view class="dizhi_xxhs">{{ address }}</view>
                <view class="dizhi_dh">
                    <view class="dizhi_dh_name">{{ name }}</view>
                    <view class="dizhi_dh_num">{{ modle }}</view>
                </view>
            </view>

            <view class="pages_box_box_body">
                <view class="pages_box_box_left">
                    <image
                        :src="detaillist.pic"
                        style="
                            width: 216rpx;
                            height: 216rpx;
                            border-radius: 16rpx;
                        "
                    >
                    </image>
                </view>
                <view class="pages_box_box_body_right">
                    <view class="pages_box_box_body_right_title">
                        {{ detaillist.p_name }}</view
                    >
                    <view class="pages_box_box_body_right_price">
                        <view class="pages_box_box_body_right_price_jf"
                            >{{ language.Edit_commodity.jf
                            }}{{ detaillist.p_integral }}+{{currency_symbol}}{{
                                Number(alldata.detail[0].p_price).toFixed(2)
                            }}</view
                        >
                        <view class="pages_box_box_body_right_price_num">
                            {{ language.groupOrder.together }}{{ detaillist.num
                            }}{{ language.goods.goods.j }}</view
                        >
                    </view>
                    <view class="pages_box_box_body_rigth_gg">{{
                        detaillist.size
                    }}</view>
                </view>
            </view>

            <view class="pages_lb">
                <view class="paegs_lb_box">
                    <view class="paegs_lb_box_left">{{
                        language.mystore_order.Total
                    }}</view>
                    <view class="paegs_lb_box_right"
                        ><span v-if="detaillist.allow"
                            >{{ language.Edit_commodity.jf
                            }}{{ detaillist.allow }}+</span
                        >{{currency_symbol}}{{ Number(alldata.spz_price).toFixed(2) }}</view
                    >
                </view>
                <view class="paegs_lb_box">
                    <view class="paegs_lb_box_left">{{
                        language.order.order.freight
                    }}</view>
                    <view class="paegs_lb_box_right"
                        >{{currency_symbol}}{{ Number(alldata.z_freight).toFixed(2) }}</view
                    >
                </view>
                <view class="paegs_lb_box">
                    <view class="paegs_lb_box_left">{{
                        language.order.order.Order_notes
                    }}</view>
                    <input
                        class="pages_list_box_name_input"
                        disabled="true"
                        style="border: none"
                        v-model="beizhu"
                    />
                </view>
                <view class="paegs_lb_box no-border">
                    <view class="paegs_lb_box_left">{{
                        language.order.order.order_total
                    }}</view>
                    <view class="paegs_lb_box_right" style="color: #fa5151"
                        ><span v-if="detaillist.allow"
                            >{{ language.Edit_commodity.jf
                            }}{{ detaillist.allow }}+</span
                        >{{currency_symbol}}{{Number( detaillist.after_discount).toFixed(2) }}</view
                    >
                </view>
            </view>

            <view class="pages_lb">
                <view class="paegs_lb_box no-border">
                    <view class="paegs_lb_box_left">{{
                        language.mystore_order.Paid_in
                    }}</view>
                    <view class="paegs_lb_box_right" style="color: #fa5151"
                        ><span v-if="detaillist.allow"
                            >{{ language.Edit_commodity.jf
                            }}{{ detaillist.allow }}+</span
                        >{{currency_symbol}}{{Number( detaillist.after_discount).toFixed(2) }}</view
                    >
                </view>
            </view>
            <view style="height: 300rpx"></view>
            <view class="btn_ff " v-if="datalist.status !== '订单关闭'"> 
                    <div v-if="is_bj" class="bottom" @tap="throttle(toAddPro) ">
                        {{ language.SpecialSettings.wc }}
                    </div> 
                <template
                    v-if="!is_bj && datalist.status == '待发货'"
                >
                    <view class="btn_box_ann" @click="editor">{{
                        language.mystore_order.edit
                    }}</view>
                    <view class="btn_box_ann" @click="delivery">{{
                        language.mystore_order.deliver
                    }}</view>
                </template>
                <!--查看物流  -->
                <template  v-if="
                                    datalist.status == '订单完成' ||
                                    datalist.status == '待收货'
                                ">
                    <view class="btn_box_ann"   @click="towuliu(datalist.id)">
                        {{ language.mystore_order.View_Logistics }}
                    </view>
                </template>
                <template v-if="!is_bj && datalist.status == '待付款'">
                    <view class="btn_box_ann" @click="editor">{{
                        language.mystore_order.edit
                    }}</view>
                    <view class="btn_box_ann"  @click="toAddPro2(datalist.oid)">关闭订单</view>
                </template>
            </view>
            <mpvue-city-picker
                ref="mpvueCityPicker"
                :themeColor="themeColor"
                :pickerValueDefault="cityPickerValueDefault"
                @onConfirm="onConfirm"
            >
            </mpvue-city-picker>
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
</template>

<script>
import mpvueCityPicker from '@my-miniprogram/src/components/mpvue-citypicker/mpvueCityPicker';
import { copyText } from "@/common/util";
export default {
    data() {
        return {
            bgColor: [
                {
                    item: "#FA5151 ",
                },
                {
                    item: "#D73B48",
                },
            ],
            themeColor: "#D73B48",
            cityPickerValueDefault: [0, 0, 0],
            jiantou:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/jiantou2x.png",
            ishead_w: 3,
            titleColor: "#ffffff",
            order_type: "",
            returnR: "",
            addressdetails: "",
            types: "",
            name: "",
            alldata: "",
            modle: "",
            beizhu: "",
            address: "",
            r_address: "",
            id: "",
            datalist: "",
            detaillist: "",
            is_bj: false,
            is_guanbi: false,
            currency_symbol: "",
        };
    },
    onLoad(e) {
        this.id = e.sNo;
        this.is_bj = e.is_bj;
        this._axios();
    },
    components: {
        mpvueCityPicker,
    },
    methods: {
        // 关闭订单
        _quxiaoa() {
            this.is_guanbi = false;
        },
        _querena(e) {
            var data = {
                api: "plugin.mch.integral.order.closeOrder",
                id: this.id,
            };
            this.$req.post({ data }).then((res) => {
                if (res.code == 200) {
                    this.is_guanbi = false;
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: "none",
                    });
                    uni.navigateBack({
                            delta: 1,
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
        toAddPro2(e) {
            // this.is_tishi = true
            this.id = e;
            this.is_guanbi = true;
        },
        towuliu(e) {
            uni.navigateTo({
                url:
                    "/pagesC/expressage/expressage?sNo=" +
                    e +
                    "&is_integral=true",
            });
        },
        copyLink(e) {
            copyText("", e);
            uni.showToast({
                title: this.language.goods.goodsDet.copy_success,
                duration: 1500,
                icon: "none",
            });
        },
        onConfirm(e) {
            this.address = e.label;
            let arr = e.label.split("-");
            this.datalist.sheng = arr[0];
            this.datalist.shi = arr[1];
            this.datalist.xian = arr[2];
        },
        showMulLinkageThreePicker() {
            this.$refs.mpvueCityPicker.show();
        },
        async toAddPro() {
            var data = {
                api: "plugin.integral.AppIntegral.saveEditOrder",
                orderNo: this.id,
                userName: this.name,
                tel: this.modle,
                address: this.r_address,
                shen: this.datalist.sheng,
                shi: this.datalist.shi,
                xian: this.datalist.xian,
            };
            await this.$req
                .post({
                    data,
                })
                .then((res) => {
                    if (res.code == 200) {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: "none",
                        });
                        uni.navigateBack();
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: "none",
                        });
                    }
                });
        },
        editor() {
            this.is_bj = true;
        },
        _axios() {
            var data = {
                api: "plugin.integral.AppIntegral.orderDetailsInfo",
                sNo: this.id,
            };
            this.$req
                .post({
                    data,
                })
                .then((res) => {
                    if (res.code == 200) {
                        this.datalist = res.data.data;
                        this.name = res.data.data.name;
                        this.r_address = res.data.data.r_address;
                        this.alldata = res.data;
                        this.modle = res.data.data.mobile;
                        this.beizhu = res.data.remark;
                        this.currency_symbol = res.data.currency_symbol;
                        this.address =
                            this.datalist.sheng +
                            "-" +
                            this.datalist.shi +
                            "-" +
                            this.datalist.xian;
                        this.detaillist = res.data.detail[0];
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: "none",
                        });
                    }
                });
        },
        delivery() {
            uni.navigateTo({
                url:
                    "/pagesD/synthesize/yushou_fahuowuliu?orderDetailIds=" +
                    this.detaillist.id +
                    "&is_integral=true&sNo="+this.datalist.id+
                   '&sum='+this.detaillist.num
            });
        },
        /**
                     * 防抖 异方法优化 减少 重复触发
                     * 使用注意项，需要将执行的异步方法 修改为 同步方法
                     * @param {Object} func 需要执行的方法 （必传）
                     * @param {Object} _this this指向
                     * @param {number || String} text 文案
                     * @param {number} tiem 文案
                     * 
                     */
                    throttle (func,tiem=1500,text ) {
                        try{
                            if(!func || Object.prototype.toString.call(func) !== '[object Function]'){
                                 throw new error(`TypeError: ${func} is not function!!`)
                            }
                            
                            const falg = this.$store.state.falg
                            return async (...args)=> {
                                const context = this; 
                                if(!falg){ 
                                    uni.showLoading({
                                        title: `${text|| this.language.util.qsh}`,
                                         mask:true //是否显示透明蒙层，防止触摸穿透
                                    }) 
                                    
                                    setTimeout(()=>{
                                       uni.hideLoading()
                                    },Number(tiem))
                                    return
                                } 
                                 uni.showLoading({
                                        title:this.language.util.qsh,
                                        mask:true
                                }) 
                                this.$store.commit('upfalg',false); 
                                try{
                                    await func.apply(context, args);  
                                }catch(e){
                                    console.error(e);
                                    this.$store.commit('upfalg',true);
                                }
                                uni.hideLoading()
                                setTimeout(()=>{
                                    this.$store.commit('upfalg',true); 
                                },Number(tiem))
                            };
                        }catch(e){
                            console.error(e);
                            this.$store.commit('upfalg',true);
                        } 
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
.jiantou {
    width: 32rpx;
    height: 44rpx;
}
._input {
    padding: 16rpx 20rpx;
    border: 1rpx solid #eee;
    display: flex;
    align-items: center;
    flex: 1;
    border-radius: 16rpx;
    // text-align: end;
    justify-content: space-between;
    > input {
        flex: 1;
    }
}
.btn_box_ann {
    width: 176rpx;
    height: 72rpx;
    line-height: 72rpx;
    text-align: center;
    font-size: 28rpx;

    font-weight: normal;
    color: #333333;
    margin-right: 22rpx;
    background-color: #f4f5f6;
    border-radius: 52rpx;
}

.btn_box {
    display: flex;
    justify-content: flex-end;
    width: 100%;
    margin-right: 32rpx;
    transform: translateX(-50%) !important;
    left: 50% !important;
}

.dizhi_dh_name {
    margin-right: 10rpx;
}

.dizhi_dh {
    display: flex;
    font-size: 28rpx;

    font-weight: normal;
    color: #333333;
}

.dizhi_xxhs {
    font-size: 28rpx;

    font-weight: normal;
    margin-top: 10rpx;
    margin-bottom: 18rpx;
    color: #999999;
}

.dizhi_xx {
    font-size: 32rpx;
    font-weight: 500;
    color: #333333;
}

.dizhi {
    width: 100%;
    padding: 32rpx;
    box-sizing: border-box;
    margin-top: 8rpx;
    border-radius: 24rpx;
    background-color: #fff;
}

.bottom {
    width: 686rpx;
    height: 92rpx;
    font-size: 32rpx;
    color: #fff;
    text-align: center;
    line-height: 92rpx;
    border-radius: 52rpx;
    margin: 0 auto;
    padding: 0;
    .solidBtn();
}

.btn_ff {
    position: fixed;
    bottom: 0;
    width: 100%;
    height: 124rpx;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    background-color: #ffffff;
    box-shadow: 0 0 0 0 rgba(0, 0, 0, 0.2);
    transform: translateX(-50%) !important;
        left: 50% !important;
    padding-bottom: constant(safe-area-inset-bottom);
    /* 兼容 iOS < 11.2 */
    padding-bottom: env(safe-area-inset-bottom);
}

.paegs_lb_box_left {
    font-size: 32rpx;

    font-weight: normal;
    color: #333333;
}

.paegs_lb_box {
    margin-left: 32rpx;
    margin-right: 32rpx;
    height: 108rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1rpx solid #eee;
}
.no-border{
    border-bottom: none;
}
.pages_lb {
    width: 100%;
    display: flex;
    flex-direction: column;
    background-color: #ffffff;
    border-radius: 24rpx;
    margin-top: 16rpx;
}

.pages_box_box_body_right_price_num {
    font-size: 24rpx;

    font-weight: normal;
    color: #999999;
}

.pages_box_box_body_right_price_jf {
    font-size: 32rpx;

    font-weight: normal;
    color: #fa5151;
}

.pages_box_box_body_right_price {
    margin-top: 34rpx;
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
    width: 446rpx;
    margin-left: 24rpx;
    display: flex;
    flex-direction: column;
}

.pages_box_box_left {
    width: 216rpx;
    height: 216rpx;
}

.pages_box_box_body {
    margin-top: 16rpx;
    display: flex;
    background-color: #ffffff;
    border-radius: 24rpx;
    padding: 32rpx;
}

.pages_list_box_name_input {
    // margin-right: 16rpx;
    padding-right: 16rpx;
    height: 76rpx;
    border: 1rpx solid #eee;
    width: 495rpx;
    border-radius: 16rpx;
    text-indent: 20rpx;
    // text-align: end;
}

.pages_list_box_name_txt {
    font-size: 32rpx;
    font-weight: normal;
    color: #333333;
}

.pages_list_box_name {
    display: flex;
    height: 76rpx;
    line-height: 76rpx;
    width: 100%;
    margin-bottom: 24rpx;
}
.pages_list_box_namea {
    display: flex;
    height: 76rpx;
    line-height: 76rpx;
    width: 100%;
}

.pages_list_box {
    padding: 32rpx;
    border-radius: 24rpx;
    background-color: #ffffff;
    display: flex;
    flex-direction: column;
}

.pages_list {
    width: 100%;
    margin-top: 24rpx;
    display: flex;
    flex-direction: column;
}

.pages_top_time {
    margin-left: 48rpx;
    margin-top: 18rpx;
    font-size: 28rpx;

    font-weight: normal;
    color: rgba(255, 255, 255, 0.9);
}

.pages_top_ddh_right {
    font-size: 28rpx;
    margin-right: 32rpx;

    font-weight: normal;
    color: #ffffff;
}

.pages_top_ddh_left {
    font-size: 28rpx;

    font-weight: normal;
    color: rgba(255, 255, 255, 0.9);
}

.pages_top_zt {
    margin-top: 48rpx;
    font-size: 48rpx;

    font-weight: normal;
    color: #ffffff;
    margin-left: 48rpx;
}

.pages_top_ddh {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-left: 48rpx;
    margin-top: 18rpx;
}

.pages_top {
    width: 100%;
    height: 282rpx;
    border-radius: 0 0 24rpx 24rpx;
    background: linear-gradient(90deg, #fa5151 0%, #d73b48 100%);
    display: flex;
    flex-direction: column;
}

.pages {
    width: 100%;
}
</style>
