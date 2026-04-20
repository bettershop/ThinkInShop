<template>
    <div class="container">
        <lktauthorize
            ref="lktAuthorizeComp"
            v-on:pChangeLoginStatus="changeLoginStatus"
        ></lktauthorize>
        <heads
            :title="language.storeSetup.title"
            :ishead_w="2"
            :bgColor="bgColor"
            :titleColor="'#333333'"
        ></heads>

        <div class="content">
            <div class="contentBox">
                <!-- 头像设置 -->
                <div class="content_row">
                    <div class="content_row_left">
                        {{ language.storeSetup.setUp }}
                    </div>

                    <div class="content_row_right" @tap="_changeImg(0)">
                        <image
                            class="head_img"
                            :src="headImg"
                            @tap.stop="clickImg(0)"
                            mode="aspectFill"
                        ></image>
                        <div class="jiantouDiv">
                            <img :src="jiantou" alt="" />
                        </div>
                    </div>
                </div>
                <!-- 宣传图设置 -->
                <div class="content_row">
                    <div class="content_row_left" style="margin-right: 32rpx">
                        {{language.storeSetup.lgsz}}
                    </div>
                
                    <div class="content_row_right" @tap="_changeImg(3)">
                        
                        <div></div>
                        <div class="jiantouDiv">
                            <img :src="jiantou" alt="" />
                        </div>
                    </div>
                </div>
                <!-- 宣传图设置 -->
                <div class="content_row">
                    <div class="content_row_left" style="margin-right: 32rpx">
                        {{ language.storeSetup.xctsz }}
                    </div>

                    <div class="content_row_right" @tap="_changeImg(2)">
                       
                        <div></div>
                        <div class="jiantouDiv">
                            <img :src="jiantou" alt="" />
                        </div>
                    </div>
                </div>
                <!-- 店铺ID -->
                <div class="content_row">
                    <div class="content_row_left">
                        {{ language.storeSetup.dpid }}
                    </div>
                
                    <div class="content_row_right setGray" style="margin-left: 30rpx;">
                       {{ id }}
                    </div>
                </div>

                <!-- 店铺名称 -->
                <div class="content_row">
                    <div class="content_row_left">
                        {{ language.storeSetup.shopName }}
                    </div>

                    <div class="content_row_right" @tap="showEditor(4)">
                        <p class="right_info">{{ storeName }}</p>
                        <div class="jiantouDiv">
                            <img :src="jiantou" alt="" />
                        </div>
                    </div>
                   
                </div>
                <!-- 店铺信息 -->
                <div class="content_row">
                    <div class="content_row_left">
                        {{ language.storeSetup.storeTitle }}
                    </div>

                    <div class="content_row_right" @tap="showEditor(1)">
                        <p class="right_info">{{ storeTitle }}</p>
                        <div class="jiantouDiv">
                            <img :src="jiantou" alt="" />
                        </div>
                    </div>
                </div>

                <!-- 店铺分类 -->
                <div class="content_row">
                    <div class="content_row_left">
                        {{ language.storeSetup.dpfl }}
                    </div>
                    <div class="content_row_right">
                        <picker
                            @change="bindPickerChange1"
                            :value="mch_fl"
                            :range="mch_arr"
                            class="right_info"
                        >
                            <view style="display: flex">
                                <view class="uni-input">{{
                                    isShowClass
                                        ? mch_arr[mch_fl]
                                        : isShowClassName
                                }}</view>
                            </view>
                        </picker>
                        <view class="jiantouDiv">
                            <img :src="jiantou" alt="" />
                        </view>
                    </div>
                </div>
                <!-- 经营范围 -->
                <div class="content_row">
                    <div class="content_row_left">
                        {{ language.storeSetup.storeRange }}
                    </div>

                    <div class="content_row_right" @tap="showEditor(2)">
                        <p class="right_info">{{ storeRange }}</p>
                        <div class="jiantouDiv">
                            <img :src="jiantou" alt="" />
                        </div>
                    </div>
                </div>
                <!-- 经营状态 -->
                <div class="content_row">
                    <div class="content_row_left">
                        {{ language.storeSetup.status }}
                    </div>

                    <div class="content_row_right" @tap="bindPickerState">
                        <view
                            >{{ language.storeSetup.businessArr[businessIndex]
                            }}<span v-if="time_all && businessIndex == 1"
                                >({{ time_all }})</span
                            ></view
                        >

                        <div class="jiantouDiv">
                            <img :src="jiantou" alt="" />
                        </div>
                    </div>
                </div>
                
            </div>

            <div class="contentBox">
                <!-- 用户姓名 -->
                <div class="content_row">
                    <div class="content_row_left">
                        {{ language.storeSetup.userName }}
                    </div>

                    <div class="content_row_right setGray">
                        {{ userName }}
                    </div>
                </div>
                <!-- 身份证号 -->
                <div class="content_row">
                    <div class="content_row_left">
                        {{ language.storeSetup.userID }}
                    </div>

                    <div class="content_row_right setGray">
                        {{ userID }}
                    </div>
                </div>
                <!-- 联系电话 -->
                <div class="content_row">
                    <div class="content_row_left">
                        {{ language.storeSetup.userPhone }}
                    </div>

                    <div class="content_row_right" @tap="showEditor(3)">
                        <p class="right_info">+{{ cpc||'' }} {{ userPhone }}</p>
                        <div class="jiantouDiv">
                            <img :src="jiantou" alt="" />
                        </div>
                    </div>
                </div>
                <!-- 联系地址 -->
                <div class="content_row">
                    <div class="content_row_left">
                        {{ language.storeSetup.userAdd }}
                    </div>

                    <div class="content_row_right" @tap="modifyAddress">
                        <div class="content_row_right_address">
                            <p class="right_info">{{ userAdd }}{{ address }}</p>
                            <p></p>
                        </div>
                        <div class="jiantouDiv">
                            <img :src="jiantou" alt="" />
                        </div>
                    </div>
                </div>
            </div>

            <div class="contentBox marginBottom">
                <div class="content_row">
                    <div class="content_row_left">
                        {{ language.storeSetup.sfzckp }}
                    </div>
                    <div
                        class="content_row_right"
                        style="flex-direction: row-reverse"
                    >
                        <switch
                            :checked="isInvoice"
                            color="#FA5151"
                            @change="changeIsInvoice"
                            style="transform: scale(0.8)"
                        />
                    </div>
                </div>
                <div class="content_row" style="border-bottom: none;">
                    <div class="content_row_left">
                        是否支持配送
                    </div>
                    <div
                        class="content_row_right"
                        style="flex-direction: row-reverse"
                    >
                        <switch
                            :checked="is_self_delivery"
                            color="#FA5151"
                            @change="changeDelivery"
                            style="transform: scale(0.8)"
                        />
                    </div>
                </div>
                <div class="content_row" v-if="is_self_delivery">
                    <div class="content_row_left">
                        配送运费
                    </div>
                    <div
                        class="content_row_right"
                        style="font-size: 32rpx;color: #333333;justify-content: flex-end;"
                    >
                        <view class="dian"><span></span></view><view>0元配送</view>
                    </div>
                </div>
            </div>
        </div>
        <!-- 店铺注销 -->
        <view class="btn">
            <div class="bottom" @tap="cancellation(1)">
                {{ language.storeSetup.cancellation }}
            </div>
        </view>
        <!-- 修改店铺信息 -->
        <div class="mask" v-if="mask_display1">
            <div class="addMask textareaMask">
                <p class="addMask_title">{{ language.storeSetup.Tips[0] }}</p>
                <div style="position: relative">
                    <textarea
                        v-model="new_title"
                        maxlength="50"
                        :placeholder="language.storeSetup.Tips[1]"
                    />

                </div>
                <div class="btn">
                    <div class="" @tap="_cancel(1)">
                        {{ language.storeSetup.cancel }}
                    </div>
                    <div @tap="_confirm(1)">
                        {{ language.storeSetup.confirm }}
                    </div>
                </div>
            </div>
        </div>

        <!-- 营业状态 -->
        <div class="mask" v-if="pickerState">
            <div class="maskBox">
                <p class="maskBox_title">
                    <span></span>
                    <span>{{ language.storeSetup.xzyyzt }}</span>
                    <img class="closeImg" :src="closeImg" @tap="_closeState" />
                </p>
                <div class="maskBox_content">
                    <div class="maskBox_choose">
                        <div
                            class="state_box"
                            :class="{ stateActive: businessIndex == 1 }"
                            @tap="changeJobState(1)"
                        >
                            <img
                                :src="
                                    businessIndex == 0
                                        ? storeSetup_time
                                        : storeSetup_time2
                                "
                                class="stateImg"
                            />
                            <span class="state_text">{{
                                language.storeSetup.yysj
                            }}</span>
                        </div>
                        <div
                            class="state_box"
                            :class="{ stateActive: businessIndex == 0 }"
                            @tap="changeJobState(0)"
                        >
                            <img
                                :src="
                                    businessIndex == 1
                                        ? storeSetup_close
                                        : storeSetup_close2
                                "
                                class="stateImg"
                            />
                            <span class="state_text">{{
                                language.storeSetup.ydy
                            }}</span>
                        </div>
                    </div>
                    <div class="timeBtn" v-if="businessIndex == 1">
                        <div class="timeBtn_box" @tap="openDatetimePicker">
                            <span v-if="!time_all">{{
                                businessIndex == 1
                                    ? language.storeSetup.szyysj
                                    : ""
                            }}</span>
                            <span v-else> {{ time_all }}</span>
                            <img :src="jiantou" />
                        </div>
                    </div>
                    <div class="timeBtnTwo" v-if="businessIndex == 0">
                        <div class="timeBtn_box"></div>
                    </div>
                </div>
            </div>
        </div>

        <div class="mask" v-if="mask_display2">
            <div class="addMask">
                <p class="addMask_title">{{ language.storeSetup.Tips[2] }}</p>
                <div style="position: relative">
                    <input
                        v-model="new_range"
                        maxlength="20"
                        :placeholder="language.storeSetup.Tips[3]"
                    />

                    <!-- <span class="textLeng">{{new_range.length}}</span> -->
                </div>
                <div class="btn">
                    <div class="" @tap="_cancel(2)">
                        {{ language.storeSetup.cancel }}
                    </div>
                    <div @tap="_confirm(2)">
                        {{ language.storeSetup.confirm }}
                    </div>
                </div>
            </div>
        </div>

        <!-- 修改手机号码 -->
        <div class="mask" v-if="mask_display3">
            <div class="addMask">
                <p class="addMask_title">{{ language.storeSetup.Tips[4] }}</p>
                <div class="phoneBox">
                    <div class="phone-qu" @tap.shop="navTo('/pagesD/login/countryAndRegion')">
                        <div class="cpc">+{{cpc}} </div>
                        <div class="san-jiao"></div>
                    </div>
                    <input type="text" v-model="new_phone" @input="telNumInput":placeholder="language.storeSetup.qsrsjhm" />
                </div>
                <div class="btn">
                    <div class="" @tap="_cancel(3)">
                        {{ language.storeSetup.cancel }}
                    </div>
                    <div @tap="_confirm(3)">
                        {{ language.storeSetup.confirm }}
                    </div>
                </div>
            </div>
        </div>
        <!-- 修改店铺名称 -->
        <div class="mask" v-if="mask_display4">
            <div class="addMask">
                <p class="addMask_title">{{ language.storeSetup.Tips[15] }}</p>
                <input type="text" v-model="new_phone" :placeholder="language.storeSetup.qsrdpmc"/>
                <div class="btn">
                    <div class="" @tap="_cancel(4)">
                        {{ language.storeSetup.cancel }}
                    </div>
                    <div @tap="_confirm(4)">
                        {{ language.storeSetup.confirm }}
                    </div>
                </div>
            </div>
        </div>
        <!-- 店铺注销提示信息 -->
        <div class="mask" v-if="cancellationFlag">
            <div class="cancellationContent">
                <div class="cancellationContent_title">
                    {{ language.storeSetup.zxts }}
                    <div v-if="Number(cashable_money) == 0 && Number(account_money) == 0" style="margin-top: 30rpx;color: #999999;font-family: initial;font-weight: 400;">是否确认注销?</div>
                </div>
                <div
                    v-if="Number(cashable_money) != 0 || Number(account_money) != 0"
                    class="cancellationContent_text"
                    style="padding: 0 48rpx;">
                        {{ language.storeSetup.Tips[12] }},
                        {{ language.storeSetup.Tips[13] }},
                        {{ language.storeSetup.Tips[14] }}？
                </div>
 
                <div class="cancellationContent_btn">
                    <div @tap="cancellationFlag = false">
                        {{ language.storeSetup.cancel }}
                    </div>
                    <div @tap="cancellation">
                        {{ language.storeSetup.confirm }}
                    </div>
                </div>
            </div>
        </div>
        <!-- 注销成功 -->
        <view class="xieyi mask" style="background-color: initial;" v-if="is_sus">
            <view style="width: 272rpx;height: 272rpx;background-color: rgba(51, 51, 51, .9);">
                <view style="margin: 32rpx 0;text-align: center;margin-top: 64rpx;">
                    <image style="width: 68rpx;height: 68rpx;" :src="sus"></image>
                </view>
                <view class="xieyi_title"
                    style="margin-bottom: 0;margin-top: 0;color: #fff;font-weight: 500;font-size: 32rpx;">
                    {{ language.storeSetup.zxcg }}
                </view>
            </view>
        </view>
        <!-- 时间选择 -->
        <time-picker
            ref="timePicker"
            :themeColor="themeColor"
            :urseTime="time_ys"
            @onConfirm="onConfirm1"
        >
        </time-picker>
    </div>
</template>

<script>
import timePicker from "../../components/time-picker.vue";

export default {
    data() {
        return {
            title: "",
            headImg: "",
            logoImg: "",
            posterImg: "",
            storeName: "",
            storeTitle: "",
            storeRange: "",
            userName: "",
            userID: "",
            userPhone: "",
            userAdd: "",
            new_title: "",
            new_range: "",
            new_phone: "",
            is_sus:false,
            shop_id: "",
            sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
            jiantou:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/jiantou2x.png",
            storeSetup_close:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/storeSetup_close.png",
            storeSetup_close2:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/storeSetup_close2.png",
            storeSetup_time:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/storeSetup_time.png",
            storeSetup_time2:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/storeSetup_time2.png",

            textImg:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/yhqBg.png",
            closeImg:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/close2x.png",
            mask_display1: false,
            mask_display2: false,
            mask_display3: false,
            mask_display4: false,
            is_self_delivery: 0,
            businessIndex: 1,
            address: "",
            cancellationFlag: false,
            platform1: "android", //平台
            themeColor: "#D73B48",
            time_all: "", //时间段
            time_ys: "", // 原始时间
            cashable_money: "", // 可提现金额
            account_money:'',
            isInvoice: true,
            cid: 0, //分类id
            isShowClass: true, //选中且是否显示该分类（false说明该分类目前我选中，但是设置了未显示）
            isShowClassName: "", //选中且未显示的分类名
            is_open: "", //营业状态
            mch_fl: 0, //店铺分类 当前选中
            mch_arr: [], //店铺分类列表
            mch_data: [], //店铺分类总数据
            bgColor: [
                {
                    item: "#ffffff",
                },
                {
                    item: "#ffffff",
                },
            ],
            pickerState: false,
            chooseState: false,
            id:'',
            cpc: '', //国家国家/地区
            diqu:{
                code:'CN',
                code2:'86',
                num3:156
            },
        };
    },
    components: {
        timePicker,
    },
    onShow() {
        this.title = this.language.storeSetup.dpsz;
        this.platform1 = uni.getSystemInfoSync().platform;
        if(uni.getStorageSync('diqu')){
            this.diqu = JSON.parse(uni.getStorageSync('diqu'))
            this.cpc = this.diqu.code2
        }
    },
    onLoad() {
        this.isLogin(() => {
            this.shop_id = uni.getStorageSync("shop_id")
                ? uni.getStorageSync("shop_id")
                : this.$store.state.shop_id;
            this._axios();
        });
    },
    onUnload(){
        uni.removeStorageSync('diqu')
    },
    methods: {
        changeDelivery(){
            this.is_self_delivery = !this.is_self_delivery
            this.$req
                .post({
                    data: {
                        api:"mch.App.Mch.Set_shop",
                        shop_id: this.shop_id,
                        is_self_delivery: this.is_self_delivery?1:0,
                    },
                })
                .then((res) => {
                    if (res.code == 200) {
                        if (this.is_self_delivery) {
                            uni.showToast({
                                title: "开启成功",
                                duration: 1500,
                                icon: "none",
                            });
                        } else {
                            uni.showToast({
                                title: "关闭成功",
                                duration: 1500,
                                icon: "none",
                            });
                        }
                        this._axios();
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: "none",
                        });
                    }
                });
        },
        changeIsInvoice(value) {
            this.isInvoice = value.detail.value;
            this.$req
                .post({
                    data: {
                        
                        api:"mch.App.Mch.Set_shop",

                        shop_id: this.shop_id,
                        isInvoice: this.isInvoice ? 1 : 0,
                    },
                })
                .then((res) => {
                    if (res.code == 200) {
                        if (this.isInvoice) {
                            uni.showToast({
                                title: "开启成功",
                                duration: 1500,
                                icon: "none",
                            });
                        } else {
                            uni.showToast({
                                title: "关闭成功",
                                duration: 1500,
                                icon: "none",
                            });
                        }
                        this._axios();
                        
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: "none",
                        });
                    }
                });
        },
        // 点击营业时间
        openDatetimePicker() {
            this.$refs.timePicker.show(this.time_ys);
        },
        // 营业时间-确定
        onConfirm1(e) {
            var start = e[0].replace(/undefined/g, "00");
            var end = e[1].replace(/undefined/g, "00");
            this.time_all = start + "~" + end;
            this.time_ys = this.time_all;
        },
        /* 
                店铺注销
            */
        cancellation(type) {
            if (type == 1) {
                this.cancellationFlag = true;
                return;
            }
            let data = {
               
                api:"mch.App.Mch.Cancellation_shop",
                shop_id: this.shop_id,
            };

            this.$req
                .post({
                    data,
                })
                .then((res) => {
                    this.cancellationFlag = false;

                    if (res.code == 200) {
                       
                        this.is_sus = true
                        setTimeout(() => {
                            this.is_sus = false
                            uni.redirectTo({
                                url: "/pages/shell/shell?pageType=my",
                            });
                        }, 1500);
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: "none",
                        });
                    }
                });
        },
        modifyAddress() {
            uni.navigateTo({
                url:
                    "/pagesA/myStore/modifyAdress?shop_id=" +
                    this.shop_id +
                    "&userAdd=" +
                    this.userAdd +
                    "&address=" +
                    this.address,
            });
        },
        bindPickerState() {
            this.pickerState = true;
        },
        _closeState() {
            if (!this.time_all&&this.is_open===1) {
                uni.showToast({
                    title: '请选择营业时间',
                    duration: 1500,
                    icon: "none",
                });
                return
            }
            if(this.time_all&&this.time_all.split('~')[0]===this.time_all.split('~')[1]){
                uni.showToast({
                    title: '开始时间不能等于结束时间哦',
                    duration: 1500,
                    icon: "none",
                });
                return
            }
            let data = {
               
                api:"mch.App.Mch.Set_shop",
                shop_id: this.shop_id,
                isOpen:this.is_open,
                businessHours:this.is_open==1?this.time_all:''
            };
            this.$req
                .post({
                    data,
                })
                .then((res)=>{
                    if (res.code == 200) {
                                uni.showToast({
                                    title: this.language.storeSetup.Tips[8],
                                    duration: 1500,
                                    icon: "none",
                                });
                                setTimeout(() => {
                                    this._axios();
                                }, 1500);
                            } else {
                                uni.showToast({
                                    title: res.message,
                                    duration: 1500,
                                    icon: "none",
                                });
                            }
                    this.pickerState = false;
                })
        },
        changeJobState(e) {
            this.businessIndex = e;
            if (this.businessIndex == 0) {
                this.is_open = 2;
            } else {
                this.is_open = 1;
            }
        },
        bindPickerChange(e) {

            this.$req
                .post({
                    data: {
                       
                        api:"mch.App.Mch.Set_shop",
                        shop_id: this.shop_id,
                        is_open: is_open,
                    },
                })
                .then((res) => {
                    if (res.code == 200) {
                        uni.showToast({
                            title: this.language.storeSetup.Tips[8],
                            duration: 1500,
                            icon: "none",
                        });
                        setTimeout(() => {
                            this._axios();
                        }, 1500);
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: "none",
                        });
                    }
                });
        },
        //店铺分类
        bindPickerChange1(e) {
            this.mch_fl = e.detail.value;
            this.$req
                .post({
                    data: {
                        
                        api:"mch.App.Mch.Set_shop",
                        shop_id: this.shop_id,
                        cid: this.mch_data[this.mch_fl].id,
                    },
                })
                .then((res) => {
                    if (res.code == 200) {
                        uni.showToast({
                            title: this.language.storeSetup.Tips[8],
                            duration: 1500,
                            icon: "none",
                        });
                        setTimeout(() => {
                            this._axios();
                        }, 1500);
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: "none",
                        });
                    }
                });
        },
        changeLoginStatus() {
            this._axios();
        },
        plusXing(str, frontLen, endLen) {
            var len = str.length - frontLen - endLen;
            var xing = "";
            for (var i = 0; i < len; i++) {
                xing += "*";
            }
            return (
                str.substring(0, frontLen) +
                xing +
                str.substring(str.length - endLen)
            );
        },
        clickImg(type) {
            var imgs = [];
            if (type == 0) {
                imgs.push(this.headImg);
            } else if (type == 2) {
                imgs.push(this.posterImg);
            }

            uni.previewImage({
                urls: imgs, //需要预览的图片http链接列表，多张的时候，url直接写在后面就行了
                current: "", // 当前显示图片的http链接，默认是第一个
                success: function (res) {},
                fail: function (res) {},
                complete: function (res) {},
            });
        },
        _changeImg(type) {
            if(type==0){
                var me = this;
                let formData = {
                    access_id: me.access_id,
                    shop_id: me.shop_id,
                    
                    api:"mch.App.Mch.Set_shop",
                };
                
                uni.chooseImage({
                    count: 1,
                    success: function (res) {
                        if (type == 2) {
                            // 宣传图
                            formData.posterImg = res.tempFilePaths[0];
                        } else if (type == 0) {
                            // 头像
                            formData.headImg = res.tempFilePaths[0];
                        }
                        uni.uploadFile({
                            url: uni.getStorageSync("url"),
                            filePath: res.tempFilePaths[0],
                            name: "file",
                            formData,
                            success: function (uploadFileRes) {
                                var data = uploadFileRes.data;
                                var json = {};
                                // #ifndef H5
                                if (me.platform1 == "android") {
                                    let str = data
                                        .replace("\r\n/g", "")
                                        .replace(/\n/g, "")
                                        .replace(/\r/g, "")
                                        .replace(/\\/g, "");
                                    var retData = str.split(",");
                                    json.code = retData[0].split(":")[1];
                                } else {
                                    json = JSON.parse(data);
                                }
                                // #endif
                
                                // #ifdef H5
                                json = JSON.parse(data);
                                // #endif
                
                                if (json.code == 200) {
                                    uni.showToast({
                                        title: me.language.storeSetup.Tips[8],
                                        icon: "none",
                                    });
                                    setTimeout(function () {
                                        me._axios();
                                    }, 1000);
                                } else {
                                    if (me.platform1 != "android") {
                                        uni.showToast({
                                            title: me.language.storeSetup.Tips[9],
                                            duration: 1500,
                                            icon: "none",
                                        });
                                    }
                                }
                            },
                        });
                    },
                });
            }else{
                let img
                if(type==2){
                    img=this.posterImg
                }else if(type==3){
                    img=this.logoImg
                }
                uni.navigateTo({
                    url:"/pagesB/store/storeSetupImg?shop_id=" +
                        this.shop_id +
                        "&type=" +
                        type
                })
            }
            
            
            
        },
        showEditor(type) {
            if (type == 1) {
                this.new_title = this.storeTitle;
                this.mask_display1 = true;
            } else if (type == 2) {
                this.new_range = this.storeRange;
                this.mask_display2 = true;
            } else if (type == 3) {
                this.new_phone = this.userPhone
                this.mask_display3 = true;
            } else if (type == 4) {
                this.new_phone = this.storeName
                this.mask_display4 = true;
            }
        },
        // 手机号码校验（只能输入数字和+-符号33825）
        telNumInput(e) {
            const o = e.target;
            const inputRule = /[^\d\+-]/g; //修改inputRule 的值
            this.$nextTick(function () {
                this.new_phone = o.value.replace(inputRule, "");
            });
        },
        _confirm(type) {
            if (type == 1) {
                this.mask_display1 = false;
                    this.$req
                        .post({
                            data: {
                                
                                api:"mch.App.Mch.Set_shop",

                                shop_id: this.shop_id,
                                shop_information: this.new_title,
                            },
                        })
                        .then((res) => {
                            if (res.code == 200) {
                                uni.showToast({
                                    title: this.language.storeSetup.Tips[8],
                                    duration: 1500,
                                    icon: "none",
                                });
                                setTimeout(() => {
                                    this._axios();
                                }, 1500);
                            } else {
                                uni.showToast({
                                    title: res.message,
                                    duration: 1500,
                                    icon: "none",
                                });
                            }
                        });
              
            } else if (type == 2) {
                this.mask_display2 = false;
                if (this.new_range) {
                    this.$req
                        .post({
                            data: {
                                
                                api:"mch.App.Mch.Set_shop",

                                shop_id: this.shop_id,
                                shop_range: this.new_range,
                            },
                        })
                        .then((res) => {
                            if (res.code == 200) {
                                uni.showToast({
                                    title: this.language.storeSetup.Tips[8],
                                    duration: 1500,
                                    icon: "none",
                                });
                                setTimeout(() => {
                                    this._axios();
                                }, 1500);
                            } else {
                                uni.showToast({
                                    title: res.message,
                                    duration: 1500,
                                    icon: "none",
                                });
                            }
                        });
                } else {
                    uni.showToast({
                        title: this.language.storeSetup.Tips[10],
                        duration: 1500,
                        icon: "none",
                    });
                }
            } else if (type == 3) {
                this.mask_display3 = false;
                if (this.new_phone.length >= 4 && this.new_phone.length <= 15) {
                    this.$req
                        .post({
                            data: {
                               
                                api:"mch.App.Mch.Set_shop",
                                cpc: this.cpc,
                                shop_id: this.shop_id,
                                tel: this.new_phone,
                            },
                        })
                        .then((res) => {
                            if (res.code == 200) {
                                uni.showToast({
                                    title: this.language.storeSetup.Tips[8],
                                    duration: 1500,
                                    icon: "none",
                                });
                                this.new_phone = "";
                                setTimeout(() => {
                                    this._axios();
                                }, 1500);
                            } else {
                                uni.showToast({
                                    title: res.message,
                                    duration: 1500,
                                    icon: "none",
                                });
                            }
                        });
                } else {
                    uni.showToast({
                        title: this.language.storeSetup.Tips[11],
                        duration: 1500,
                        icon: "none",
                    });
                }
            } else if (type == 4) {
                if (this.new_phone) {
                    this.mask_display4 = false;
                    this.$req
                        .post({
                            data: {
                                
                                api:"mch.App.Mch.Set_shop",

                                shop_id: this.shop_id,
                                name: this.new_phone,
                            },
                        })
                        .then((res) => {
                            if (res.code == 200) {
                                uni.showToast({
                                    title: this.language.storeSetup.Tips[8],
                                    duration: 1500,
                                    icon: "none",
                                });
                                setTimeout(() => {
                                    this._axios();
                                }, 1500);
                            } else {
                                uni.showToast({
                                    title: res.message,
                                    duration: 1500,
                                    icon: "none",
                                });
                            }
                        });
                } else {
                    uni.showToast({
                        title: "请输入店铺名称",
                        duration: 1500,
                        icon: "none",
                    });
                }
            }
        },
        _cancel(type) {
            if (type == 1) {
                this.mask_display1 = false;
            } else if (type == 2) {
                this.mask_display2 = false;
            } else if (type == 3) {
                this.new_phone = "";
                this.mask_display3 = false;
            } else if (type == 4) {
                this.new_phone = "";
                this.mask_display4 = false;
            }
        },
        axios_fl() {
            let data = {
                api: "app.index.mchClass"
            };
            this.$req
                .post({
                    data,
                })
                .then((res) => {
                    let { code, message, data } = res;
                    if (code == 200) {
                        this.mch_data = data.list;
                        this.isShowClass = this.mch_data.some(
                            (item) => item.id == this.cid
                        );
                        this.mch_data.sort((a, b) => {
                            if (a.id === this.cid) {
                                return -1;
                            } else if (b.id === this.cid) {
                                return 1;
                            } else {
                                return 0; // 其他情况不需要改变位置
                            }
                        });
                        this.mch_arr = [];
                        this.mch_fl = 0;
                        data.list.forEach((item, index) => {
                            if (item.is_display) {
                                this.mch_arr.push(item.name);
                            }
                        });
                    } else {
                        uni.showToast({
                            title: message,
                            icon: "none",
                        });
                    }

                    this.loadFlag = true;
                });
        },
        _axios() {
            this.$req
                .post({
                    data: {
                       
                        api:"mch.App.Mch.Into_set_shop",

                        shop_id: this.shop_id,
                    },
                })
                .then((res) => {
                    if (res.code == 200) {
                        var data = res.data.list[0];
                        this.headImg = data.headImg;
                        this.logoImg = data.logo;
                        this.posterImg = data.posterImg;
                        this.id = data.id;
                        this.is_self_delivery = data.is_self_delivery
                        this.storeName = data.name;
                        this.storeRange = data.shop_range;
                        this.storeTitle = data.shop_information;
                        this.userName = data.realname;
                        this.userID = data.ID_number.replace(
                            /^(\d{8})\d{6}(\d+)/,
                            "$1******$2"
                        );
                        this.userPhone = data.tel.replace(
                            /^(\d{3})\d{4}(\d+)/,
                            "$1****$2"
                        );
                        if (this.code2 == '86' || this.code2 == '852' || this.code2 == '853') {
                             this.userAdd =
                            data.sheng + "-" + data.shi + "-" + data.xian;
                        }else {
                            this.userAdd = '';
                        }
                        this.cpc = data.cpc
                        this.address = data.address;
                        this.cashable_money = data.cashable_money;
                        this.account_money = data.account_money
                        this.isInvoice = data.isInvoice == 0 ? false : true;
                        this.cid = data.cid; //分类id
                        this.isShowClassName = data.cname;
                        this.time_all=data.business_hours
                        this.time_ys =data.business_hours
                        // 营业状态
                        this.is_open = data.is_open;
                        this.axios_fl();
                        if (data.is_open == 1) {
                            this.businessIndex = 1;
                        } else {
                            this.businessIndex = 0;
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
    },
};
</script>

<style lang="less">
@import url("@/laike.less");
@import url("../../static/css/myStore/storeSetup.less");
// 一下是java样式优化从其他页面复制来(因class名一样)
/deep/.uni-input-input {
    font-size: 32rpx;
}
.dian{
    width: 32rpx;
    height: 32rpx;
    border: 2rpx solid #FA5151;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 16rpx;
    >span{
        width: 24rpx;
        height: 24rpx;
        background-color: #FA5151;
        border-radius: 50%;
    }
}
.textLeng {
    position: absolute;
    right: 40rpx;
    bottom: 2rpx;
    font-size: 28rpx;
    line-height: 28rpx;
    color: #999999;
}
.xieyi {
    position: fixed;
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
/* 遮罩层规格样式 */
.mask {
    width: 100%;
    background-color: rgba(000, 000, 000, 0.5);
    position: fixed;
    top: 0;
    left: 0;
    z-index: 999 !important;
    .phoneBox{
        display: flex;
        align-items: center;
        margin: 0 48rpx;
        border-bottom: 0.5rpx solid #E5E5E5;
        padding-bottom: 12rpx;
        uni-input{
            border: none !important;
        }
    }
    
}
.cancellationContent {
    width: 640rpx;
    min-height: 280rpx;
    background: rgba(255, 255, 255, 1);
    border-radius: 23rpx;
}
.cancellationContent_title {
    width: 544rpx;
    margin: 64rpx auto;
    text-align: center;
    font-size: 32rpx;
    
    //font-weight:  500;
    color: #333333;
}
.cancellationContent_text {
    margin: 62rpx auto 66rpx;
    text-align: center;
    line-height: 36.5rpx;
    font-size: 32rpx;
    
    //font-weight:  400;
    color: #999999;
}

.cancellationContent_btn {
    display: flex;
    align-items: center;
    height: 108rpx;
    border-top: 1rpx solid rgba(0, 0, 0, 0.1);
    box-sizing: border-box;
}

.cancellationContent_btn > div {
    flex: 1;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32rpx;
    color: #333333;
}

.cancellationContent_btn > div:last-child {
    color: #d73b48;
    border-left: 1rpx solid rgba(0, 0, 0, 0.1);
}

.contentBox .textarea {
    height: 208rpx;
    margin-bottom: 20rpx;
}

.phone-qu{
	display: flex;
    align-items: center;
    .cpc{
      width: 100rpx;
    }
	img{
		padding :0px
	}
}
</style>
