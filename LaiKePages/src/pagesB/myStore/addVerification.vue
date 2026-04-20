<template>
    <view class='container' style="min-height: 100vh;">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="title" :border="true" :bgColor='bgColor' titleColor="#000" ishead_w="2"></heads>
        <view class="box">
            <div class="addStore-list">
                <div>
                    <span class="must">*</span>
                    <p>{{ language.myStore.addVerification.hxrq }}</p>
                    <div class="addStoreInput" @click='openDate'>
                        <span v-if='startDate && endDate'>
                            {{startDate}}
                            {{language.myStore.verificationManagement.zhi}}
                            {{endDate}}
                        </span>
                        <view class="noValue" v-else>{{
                            language.myStore.addVerification.qxzhxrq
                        }}</view>
                        <img :src="jiantou" alt="" />
                    </div>
                </div>
            </div>
            <div class="addStore-list">
                <div>
                    <span class="must">*</span>
                    <p>{{ language.myStore.verificationManagement.hxsd }}</p>
                    <div class="addStoreInput" @tap="openDatetimePicker">
                        <view v-if="time_all">
                            <span>{{ time_all }}</span>
                        </view>
                        <view class="noValue" v-else>{{
                            language.myStore.addVerification.hxsd
                        }}</view>
                        <img :src="jiantou" alt="" />
                    </div>
                </div>
            </div>
            <div class="addStore-list">
                <div :class="teamSwitch == 1?'pad-kong':'pad-kong2'">
                    <span class="must">*</span>
                    <p>{{ language.myStore.addVerification.hxsx }}</p>
                    <div class="btn_danXuan">
                        <div @tap="_danXuan(1)">
                            <span :class="{active:teamSwitch == 1}">
                                <span :class="{btn_danXuan_xuanzhong:teamSwitch == 1}"></span>
                            </span>
                            <span>{{language.myStore.addVerification.wxz}}</span>
                        </div>
                        <div @tap="_danXuan(0)">
                            <span :class="{active:teamSwitch == 0}">
                                <span :class="{btn_danXuan_xuanzhong:teamSwitch == 0}"></span>
                            </span>
                            <span>{{language.myStore.addVerification.jtz}}</span>
                        </div>
                    </div>
                </div>
            </div>
            <div class="addStore-list" v-if="teamSwitch == 0">
                <div style="padding-bottom: 0;">
                    <div style="width: 190rpx;"></div>
                    <div class="addStoreInput">
                        <input type="number" @input="checkPositiveInteger"
                            :placeholder="language.myStore.addVerification.qsrhxsl" :placeholder-style="placeColor"
                            v-model="write_off_num" />
                    </div>
                </div>
            </div>
        </view>

        <view class="btn">
            <p class="save" @tap="btn()">{{language.myStore.addVerification.bc}}</p>
            <!-- <p class="delete" v-if="id" @tap="del()">{{language.myStore.addAccount.btn[1]}}</p> -->
        </view>
        <view class="xieyi mask" style="background-color: initial;" v-if="is_sus">
            <view style="width: 272rpx;height: 272rpx;background-color: rgba(51, 51, 51, .9);">
                <view style="margin: 32rpx 0;text-align: center;margin-top: 64rpx;">
                    <image style="width: 68rpx;height: 68rpx;" :src="sus"></image>
                </view>
                <view class="xieyi_title"
                    style="margin-bottom: 0;margin-top: 0;color: #fff;font-weight: 500;font-size: 32rpx;">
                    {{ is_title }}
                </view>
            </view>
        </view>
        <!-- 时间组件 -->
        <div class="mpvue-picker">
            <div :class="{'pickerMask':showPicker}" @click.stop="maskClick" catchtouchmove="true"></div>
            <div class="mpvue-picker-content " :class="{'mpvue-picker-view-show':showPicker}">
                <div class="mpvue-picker__hd" catchtouchmove="true">
                    {{ language.myStore.addVerification.hxrq }}
                </div>
                <view class="time">
                    <view class="time_txt">{{language.myStore.addVerification.xzrq}}</view>
                    <view class="time_time">
                        <view class="time_time_ks" :class="iShow == true?'time_time_ks_red':''"
                            @click.stop="open_time(1)">{{startDatetxt}}</view>
                        <view class="time_time_z">{{language.seckill.seckill.to}}</view>
                        <view class="time_time_ks" :class="iShow == false?'time_time_ks_red':''"
                            @click.stop="open_time(2)">{{endDatetxt}}</view>
                    </view>

                </view>
                <div style="display: flex;justify-content: center;align-items: center;background: #fff;">
                    <picker-view v-if="iShow" :value="value1" @change="bindChange1" class="mpvue-picker-view"
                        indicator-style="height: 40px" style="flex: 1;">
                        <picker-view-column>
                            <view :class="value1[0]==index?'pickerSelected':''" class="picker-item"
                                v-for="(item,index) in years" :key="index">{{item}}</view>
                        </picker-view-column>
                        <picker-view-column>
                            <view :class="value1[1]==index?'pickerSelected':''" class="picker-item"
                                v-for="(item,index) in months" :key="index">{{item}}</view>
                        </picker-view-column>
                        <picker-view-column>
                            <view :class="value1[2]==index?'pickerSelected':''" class="picker-item"
                                v-for="(item,index) in days" :key="index">{{item}}</view>
                        </picker-view-column>
                    </picker-view>
                    <picker-view v-else :value="value2" @change="bindChange1" class="mpvue-picker-view"
                        indicator-style="height: 40px;" style="flex: 1;">
                        <picker-view-column>
                            <view :class="value2[0]==idx?'pickerSelected':''" class="picker-item"
                                v-for="(item,idx) in years" :key="idx">{{item}}</view>
                        </picker-view-column>
                        <picker-view-column>
                            <view :class="value2[1]==idx?'pickerSelected':''" class="picker-item"
                                v-for="(item,idx) in months" :key="idx">{{item}}</view>
                        </picker-view-column>
                        <picker-view-column>
                            <view :class="value2[2]==idx?'pickerSelected':''" class="picker-item"
                                v-for="(item,idx) in days" :key="idx">{{item}}</view>
                        </picker-view-column>
                    </picker-view>
                </div>
                <view class="time_btn">
                    <view class="time_btn_cz" @click="showPicker=false">{{ language.editActivity.qx }}</view>
                    <view class="time_btn_ss" @click="axiosGoods">{{ language.editActivity.qr }}</view>
                </view>
            </div>
        </div>

        <!-- 时间选择 -->
        <time-picker ref="timePicker" :themeColor="themeColor" :urseTime="time_ys" :isType="0" @onConfirm="onConfirm1">
        </time-picker>
    </view>
</template>

<script>
    import timePicker from "../../components/time-picker.vue";
    let date = new Date()
    let hour = date.getHours()
    let minute = date.getMinutes()
    let second = date.getSeconds()

    let hours = []
    let minutes = []
    let seconds = []

    for (let i = 0; i < 24; i++) {
        if (i < 10) {
            hours.push('0' + i)
            continue
        }
        hours.push(i)
    }

    for (let i = 0; i < 60; i++) {
        if (i < 10) {
            minutes.push('0' + i)
            seconds.push('0' + i)
            continue
        }
        minutes.push(i)
        seconds.push(i)
    }

    const years = []
    const year = date.getFullYear()
    const months = []
    const month = date.getMonth() + 1
    const days = []
    const day = date.getDate()
    for (let i = year; i <= year + 5; i++) {
        years.push(i)
    }
    for (let i = 1; i <= 12; i++) {
        if (i < 10) {
            i = '0' + i
        }
        months.push(i)
    }
    for (let i = 1; i <= 31; i++) {
        if (i < 10) {
            i = '0' + i
        }
        days.push(i)
    }
    export default {
        data() {
            return {
                bgColor: [{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                fanghui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/member/fanghui.png',
                pwShow: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/pwHide.png',
                pwHide: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/pwShow.png',
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                    "images/icon/jiantou2x.png",
                id: '',
                add_date: '',
                account: '',
                password: '',
                LoginPWStatus: true,
                is_sus: false,
                title: '',
                is_title: '',
                shop_id: '',
                mch_store_id: '',
                iShow: false,
                teamSwitch: 1,
                placeColor: "color:#b8b8b8",
                themeColor: "#D73B48",
                time_all: "", //时间段
                time_ys: "", // 原始时间
                //时间控件
                hours,
                minutes,
                seconds,
                value: [hour, minute, second],

                /* 是否显示控件 */
                showPicker: false,
                /* 选择时间段 */
                time: [years[0] + '-' + months[month - 1] + '-' + days[day - 1]],
                // 时间选择器
                years,
                months,
                days,
                value1: [0, month - 1, day - 1],
                value2: [0, month - 1, day - 1],
                start_time: '',
                startDatetxt: '开始时间',
                startDate: '',
                endDate: '',
                endDatetxt: '结束时间',
                write_off_num: 0, //次数，0为无限制
            }
        },
        computed: {

        },
        components: {
            timePicker,
        },
        onLoad(option) {
            if (option && option.item) {
                var optionItem = JSON.parse(decodeURIComponent(option.item));
                this.id = optionItem.id
                this.startDate = optionItem.startDate
                this.endDate = optionItem.endDate
                this.startDatetxt = optionItem.startDate
                this.endDatetxt = optionItem.endDate
                this.time_all = optionItem.startTime + '~' + optionItem.endTime
                this.time_ys = this.time_all
                if(optionItem.num != 0){
                    this.teamSwitch = 0
                    this.write_off_num = optionItem.num
                }
                this.iShow = true
            }
            if (option && option.mch_store_id) {
                this.mch_store_id = option.mch_store_id
            }
            if (option && option.shop_id) {
                this.shop_id = option.shop_id;
            } else {
                this.shop_id = this.$store.state.shop_id;
            }
            if (this.id) {
                this.title = this.language.myStore.addVerification.bjhxsj
            } else {
                this.title = this.language.myStore.addVerification.tjhxsj
            }
        },
        onShow() {
            this.currentTime()
            this.isLogin(() => {

            })
        },
        methods: {
            _danXuan(value) {
                this.teamSwitch = value
                if (value == 1) {
                    this.write_off_num = 0
                } else {
                    this.write_off_num = ''
                }
            },
            // 密码是否可见 1登录密码 2注册密码 3再次输入注册密码
            pwStatus(type) {
                if (type == 1) {
                    this.LoginPWStatus = !this.LoginPWStatus
                }
            },
            openDate() {
                this.showPicker = true
                if (this.type != 2) {
                    this.startDatetxt = this.onstart
                }
            },
            axiosGoods(e) {
                if (this.startDatetxt != '开始时间') {
                    this.startDate = this.startDatetxt
                } else {
                    this.startDate = ''
                }
                if (this.endDatetxt != '结束时间') {
                    this.endDate = this.endDatetxt
                } else {
                    this.endDate = ''
                }
                if (this.startDate == '') {
                    uni.showToast({
                        title: this.language.editActivity.kssjbnwk,
                        icon: 'none'
                    });
                    return
                }
                if (this.endDate == '') {
                    uni.showToast({
                        title: this.language.editActivity.jssjbnwk,
                        icon: 'none'
                    });
                    return
                }
                let obj1 = new Date(this.startDate) //开始时间
                let obj = new Date(this.endDate) //结束时间
                if (obj1.getTime() > obj.getTime()) {
                    uni.showToast({
                        title: this.language.editActivity.kssjbndysjsj,
                        icon: 'none'
                    })
                    this.endDatetxt = '结束时间'
                    this.endDate = ''
                    return
                }
                this.showPicker = false
            },
            currentTime() {
                var date = new Date()
                var year = date.getFullYear() //月份从0~11，所以加一
                var dateArr = [
                    date.getMonth() + 1,
                    date.getDate(),
                ]
                var dateArr2 = [
                    date.getMonth() + 1,
                    date.getDate() - 1,
                ]
                //如果格式是MM则需要此步骤，如果是M格式则此循环注释掉
                for (var i = 0; i < dateArr.length; i++) {
                    if (dateArr[i] >= 1 && dateArr[i] <= 9) {
                        dateArr[i] = '0' + dateArr[i]
                    }
                }
                for (var i = 0; i < dateArr2.length; i++) {
                    if (dateArr2[i] >= 1 && dateArr2[i] <= 9) {
                        dateArr2[i] = '0' + dateArr2[i]
                    }
                }
                var strDate =
                    year +
                    '-' +
                    dateArr[0] +
                    '-' +
                    dateArr[1]
                let dateTime = new Date().getFullYear() /* 获取现在的年份 */
                dateTime = new Date(new Date().setFullYear(dateTime + 1)).getFullYear()
                var strDate1 =
                    dateTime +
                    '-' +
                    dateArr2[0] +
                    '-' +
                    dateArr2[1]
                this.onstart = strDate
                this.onend = strDate1
            },
            open_time(e) {
                if (e == 1) {
                    this.iShow = true
                }
                if (e == 2) {
                    this.iShow = false
                }

            },
            maskClick() {
                this.pickerCancel()
            },
            pickerCancel() {
                this.showPicker = false
            },
            pickerConfirm() {
                this.showPicker = false
                this.$emit('onConfirm', this.time)
            },
            showPickerView() {
                this.showPicker = true
            },
            bindChange1(e) {
                const val = e.detail.value
                let days = []

                if (this.isLeapYear(this.years[val[0]]) && this.months[val[1]] == '02') {
                    for (let i = 1; i <= 29; i++) {
                        if (i < 10) {
                            i = '0' + i
                        }
                        days.push(i)
                    }
                } else if (this.months[val[1]] == '02') {
                    for (let i = 1; i <= 28; i++) {
                        if (i < 10) {
                            i = '0' + i
                        }
                        days.push(i)
                    }
                } else {
                    if (this.months[val[1]] == '01' || this.months[val[1]] == '03' || this.months[val[1]] == '05' ||
                        this.months[val[1]] == '07' || this.months[val[1]] == '08' || this.months[val[1]] == '10' ||
                        this.months[val[1]] == '12') {
                        for (let i = 1; i <= 31; i++) {
                            if (i < 10) {
                                i = '0' + i
                            }
                            days.push(i)
                        }
                    } else {
                        for (let i = 1; i <= 30; i++) {
                            if (i < 10) {
                                i = '0' + i
                            }
                            days.push(i)
                        }
                    }

                }
                this.days = days
                if (this.iShow == true) {
                    this.value1 = val
                    this.startDatetxt = this.years[val[0]] + '-' + this.months[val[1]] + '-' + this.days[val[2]]
                } else {
                    this.value2 = val
                    this.endDatetxt = this.years[val[0]] + '-' + this.months[val[1]] + '-' + this.days[val[2]]
                }
            },

            // 求闰年
            isLeapYear(year) {
                var cond1 = year % 4 == 0; //条件1：年份必须要能被4整除
                var cond2 = year % 100 != 0; //条件2：年份不能是整百数
                var cond3 = year % 400 == 0; //条件3：年份是400的倍数
                //当条件1和条件2同时成立时，就肯定是闰年，所以条件1和条件2之间为“与”的关系。
                //如果条件1和条件2不能同时成立，但如果条件3能成立，则仍然是闰年。所以条件3与前2项为“或”的关系。
                //所以得出判断闰年的表达式：
                var cond = cond1 && cond2 || cond3;
                if (cond) {
                    return true;
                } else {
                    return false;
                }
            },
            // 点击核销时段
            openDatetimePicker() {
                this.$refs.timePicker.show(this.time_ys);
            },
            // 核销时段-确定
            onConfirm1(e) {
                var start = e[0].replace(/undefined/g, "00");
                var end = e[1].replace(/undefined/g, "00");
                this.time_all = start + "~" + end;
                this.time_ys = this.time_all;
            },
            checkPositiveInteger(e) {
                e.target.value = e.target.value.replace(/^0|[^\d]|[.]/g, '')
                this.$nextTick(() => {
                    this.write_off_num = e.target.value
                })
            },
            btn() {
                let write_date, write_time
                if (this.startDate == '' || this.endDate == '') {
                    uni.showToast({
                        title: this.language.myStore.addVerification.qxzhxrq,
                        duration: 1000,
                        icon: 'none'
                    })
                    return
                }
                if (this.time_all == '') {
                    uni.showToast({
                        title: this.language.myStore.addVerification.hxsd,
                        duration: 1000,
                        icon: 'none'
                    })
                    return
                }
                write_date = this.startDate + ',' + this.endDate
                write_time = this.time_all.split('~').join(',')
                if (this.teamSwitch == 0 && this.write_off_num == '') {
                    uni.showToast({
                        title: this.language.myStore.addVerification.qsrjtz,
                        duration: 1000,
                        icon: 'none'
                    })
                    return
                }
                var me = this
                var data = {
                    api: 'mch.App.Mch.addAppointmenTime',
                    mch_id: this.shop_id,
                    mch_store_id: this.mch_store_id,
                    write_date: write_date,
                    write_time: write_time,
                    write_off_num: this.write_off_num
                }
                //弃用编辑
                // if(this.id){
                //     data.api = 'mch.App.Mch.editAppointmenTime'
                //     data.w_id = this.id
                // }
                this.$req.post({
                    data
                }).then(res => {
                    if (res.code == 200) {
                        //弃用编辑
                        // if (this.id) {
                        //     this.is_title = this.language.myStore.addAccount.bjcg
                        // } else {
                        // this.is_title = this.language.myStore.addAccount.tjcg
                        // }
                        this.is_title = this.language.myStore.addAccount.tjcg
                        this.is_sus = true
                        setTimeout(() => {
                            this.is_sus = false
                            this.navBack()
                        }, 1500)
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1000,
                            icon: 'none'
                        })
                    }
                })
            },
            del() {
                var me = this
                var data = {
                    api: 'mch.App.Mch.DelStoreAdmin',
                    shop_id: this.shop_id,
                    mch_store_id: this.mch_store_id,
                    id: this.id
                }
                this.$req.post({
                    data
                }).then(res => {
                    if (res.code == 200) {
                        this.is_title = this.language.zdata.sccg
                        this.is_sus = true
                        setTimeout(() => {
                            this.is_sus = false
                            this.navBack()
                        }, 1500)
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1000,
                            icon: 'none'
                        })
                    }
                })
            },
        },
    }
</script>
<style>
    page {
        background-color: #f4f5f6;
    }
</style>
<style lang="less" scoped>
    @import url("@/laike.less");

    .addStore-list {
        position: relative;
    }

    .addStore-list>div {
        font-size: 32rpx;
        color: #333333;
        font-size: 32rpx;
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding-bottom: 24rpx
    }

    .addStore-list p {
        padding: 16rpx 0;
        min-width: 130rpx;
        font-weight: 400;
        margin-right: 34rpx;
        font-weight: 400 !important;
    }

    .addStore-list .addStoreInput {
        display: flex;
        align-items: center;
        justify-content: space-between;
        flex: 1;
        height: 76rpx;
        font-weight: 400 !important;
        color: #333333;
        border-radius: 16rpx;
        padding: 16rpx;
        border: 1rpx solid rgba(0, 0, 0, .1);

        >uni-view>span {
            font-weight: 400 !important;
        }

        input {
            flex: 1;
            margin-right: 32rpx;
            font-size: 32rpx;
        }

        input::placeholder {
            font-weight: 400;
        }

        /deep/.uni-input-input {
            font-size: 32rpx;

        }

        .noValue {
            font-size: 32rpx;

            font-weight: 400;
            color: rgb(184, 184, 184);
        }

        .youValue {
            font-size: 32rpx;
            font-weight: normal;
            color: #333333;
        }

        .youValue2 {
            font-weight: normal;
            font-size: 32rpx;
            color: #999999;
        }
    }

    .addStore-list img {
        width: 32rpx;
        height: 44rpx;
    }

    .addStore-list .xuanze {
        width: 32rpx;
        height: 32rpx;
        margin-left: auto;
    }

    .must {
        color: #FA5151;
        margin-right: 10rpx;
    }

    .pad-kong {
        padding-bottom: 0 !important;
    }
    .pad-kong2 {
        padding-bottom: 8px !important;
    }
    .btn_danXuan {
        display: flex;
        align-items: center;
        min-height: 76rpx;
        flex: 1;

        >div {
            display: flex;
            align-items: center;
            margin-right: 32rpx;

            >span:first-child {
                width: 32rpx;
                height: 32rpx;
                border: 1.5px solid #CCCCCC;
                border-radius: 50%;
                margin-right: 16rpx;
                display: flex;
                align-items: center;
                justify-content: center;

                .btn_danXuan_xuanzhong {
                    display: inline-block;
                    width: 16rpx;
                    height: 16rpx;
                    background-color: #CCCCCC;
                    border-radius: 50%;
                }
            }

            >span:last-child {
                font-size: 32rpx;
                color: #333333;
            }

            .active {
                border-color: #FA5151 !important;

                >span {
                    background-color: #FA5151 !important;
                }
            }
        }
    }

    .box {
        border-radius: 0 0 24rpx 24rpx;
        padding: 32rpx;
        // padding-top: 16rpx;
        // padding-bottom: 24rpx;
        background-color: #fff;
    }

    .btn {
        transform: none !important;
    }

    .btn {
        p {
            width: 686rpx;
            height: 92rpx;
            line-height: 92rpx;
            text-align: center;
            font-size: 32rpx;
            border-radius: 52rpx;
        }

        .save {
            margin: 104rpx auto 40rpx;
            .solidBtn()
        }

        .delete {
            margin: 0 auto;
            border: 1px solid #Fa5151;
            color: #Fa5151;
        }
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

        >view {
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

    //时间选择
    .mpvue-picker-view {
        position: relative;
        bottom: 0;
        left: 0;
        width: 100%;
        height: 238px;
        background-color: rgba(255, 255, 255, 1);
    }

    .picker-item {
        text-align: center;
        line-height: 40px;
        text-overflow: ellipsis;
        white-space: nowrap;
        font-size: 16px;
    }

    .pickerSelected {
        color: #FA5151;
    }

    /deep/.head {
        z-index: 998;
    }

    .pickerMask {
        position: fixed;
        z-index: 998;
        top: 0;
        right: 0;
        left: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.6);
    }

    .mpvue-picker-content {
        position: fixed;
        bottom: 0;
        left: 0;
        width: 100%;
        transition: all 0.3s ease;
        transform: translateY(100%);
        z-index: 998;
    }

    .mpvue-picker-view-show {
        transform: translateY(0);
    }

    .mpvue-picker__hd {
        text-align: center;
        // display: flex;
        padding: 24rpx 30rpx;
        background-color: #F4F5F6;
        height: 112rpx;
        position: relative;
        text-align: center;
        font-size: 40rpx;
        border-radius: 24rpx 24rpx 0rpx 0rpx;
    }

    .mpvue-picker__hd:after {
        content: ' ';
        position: absolute;
        left: 0;
        bottom: 0;
        right: 0;
        height: 1px;
        border-bottom: 1px solid #e5e5e5;
        color: #e5e5e5;
        transform-origin: 0 100%;
        transform: scaleY(0.5);
    }

    .mpvue-picker__action {
        display: block;
        flex: 1;
        color: #1aad19;
        line-height: 76rpx;
    }

    .mpvue-picker__action:first-child {
        text-align: left;
        color: #888;
        line-height: 76rpx;
    }

    .mpvue-picker__action:last-child {
        text-align: right;
    }

    /* #ifdef H5 */
    .time {
        height: 244rpx;
        background: #ffffff;


        display: flex;
        flex-direction: column;
        z-index: 3000;
    }

    /* #endif */
    /* #ifdef MP-WEIXIN */
    .time {
        height: 244rpx;
        background: #ffffff;


        display: flex;
        flex-direction: column;
        z-index: 3000;
    }

    /* #endif */
    .time_time {
        width: 100%;
        display: flex;
        justify-content: center;
        align-items: center;
    }

    .time_time_ks {
        width: 304rpx;
        height: 92rpx;
        text-align: center;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #F4F5F6;
        border-radius: 16rpx;
        font-size: 32rpx;
        color: #999999;
    }

    .time_time_ks_red {
        width: 300rpx;
        height: 88rpx;
        background: rgba(250, 81, 81, 0.1);
        border: 2rpx solid #FA5151;
        font-size: 32rpx;
        color: #FA5151;
    }

    .time_time_z {
        font-size: 32rpx;

        font-weight: 400;
        color: #999999;
        margin-left: 24rpx;
        margin-right: 22rpx;
    }

    .time_txt {
        margin-top: 32rpx;
        margin-bottom: 24rpx;
        margin-left: 32rpx;
        font-size: 32rpx;

        font-weight: 400;
    }

    .time_btn {
        padding: 40rpx;
        display: flex;
        justify-content: center;
        align-items: center;
        background-color: #ffffff;
    }

    .time_btn_cz {
        width: 332rpx;
        height: 88rpx;
        border-radius: 52rpx;
        border: 2rpx solid #FA5151;
        font-size: 32rpx;

        font-weight: 500;
        color: #FA5151;
        text-align: center;
        line-height: 88rpx;
        margin-right: 22rpx;
    }

    .time_btn_ss {
        text-align: center;
        line-height: 88rpx;
        width: 332rpx;
        height: 88rpx;
        border-radius: 52rpx;
        border: 2rpx solid #FA5151;
        font-size: 32rpx;
        background-color: #FA5151;

        font-weight: 500;
        color: #ffffff;
    }

    .rightInput1_myinput {
        position: absolute;

        right: 60rpx;
        font-size: 24rpx;
    }

    .color1 {
        color: #999999;
    }
</style>
