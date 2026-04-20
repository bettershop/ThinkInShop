<template>
    <div class="mpvue-picker">
        <div
            :class="{ pickerMask: showPicker }"
            @click.stop="maskClick"
            catchtouchmove="true"
        ></div>
        <div
            class="mpvue-picker-content"
            :class="{ 'mpvue-picker-view-show': showPicker }"
        >
            <div class="mpvue-picker__hd" catchtouchmove="true">
                <div class="mpvue-picker__action" @click.stop="pickerCancel">
                    {{ language.datatime.cancel }}
                </div>
                <div
                    class="mpvue-picker__action"
                    :style="{ color: themeColor }"
                    @click.stop="pickerConfirm"
                >
                    {{ language.datatime.confirm }}
                </div>
            </div>
            <div
                style="
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    background: #fff;
                "
            >
                <picker-view
                    :value="value1"
                    @change="bindChange1"
                    class="mpvue-picker-view"
                    indicator-style="height: 40px;"
                    style="flex: 1"
                >
                    <picker-view-column>
                        <view
                            :class="value1[0] == index ? 'pickerSelected' : ''"
                            class="picker-item"
                            v-for="(item, index) in years"
                            :key="index"
                            >{{ item }} {{ language.datatime.year }}</view
                        >
                    </picker-view-column>
                    <picker-view-column>
                        <view
                            :class="value1[1] == index ? 'pickerSelected' : ''"
                            class="picker-item"
                            v-for="(item, index) in months"
                            :key="index"
                            >{{ item }} {{ language.datatime.month }}</view
                        >
                    </picker-view-column>
                    <picker-view-column v-if="!is_days">
                        <view
                            :class="value1[2] == index ? 'pickerSelected' : ''"
                            class="picker-item"
                            v-for="(item, index) in days"
                            :key="index"
                            >{{ item }} {{ language.datatime.days }}</view
                        >
                    </picker-view-column>
                </picker-view>
                <picker-view
                    v-if="is_min"
                    :value="value2"
                    @change="bindChange2"
                    class="mpvue-picker-view"
                    indicator-style="height: 40px;"
                    style="flex: 1"
                >
                    <picker-view-column>
                        <view
                            class="picker-item"
                            v-for="(item, index) in hours"
                            :key="index"
                            >{{ item }} {{ language.datatime.hour }}</view
                        >
                    </picker-view-column>
                    <picker-view-column>
                        <view
                            class="picker-item"
                            v-for="(item, index) in minutes"
                            :key="index"
                            >{{ item }} {{ language.datatime.min }}</view
                        >
                    </picker-view-column>
                    <picker-view-column>
                        <view
                            class="picker-item"
                            v-for="(item, index) in seconds"
                            :key="index"
                            >{{ item }} {{ language.datatime.second }}</view
                        >
                    </picker-view-column>
                </picker-view>
            </div>
        </div>
    </div>
</template>

<script>
const d = new Date();
// 抽离通用补零函数（复用所有数字补零场景）
const padZero = n => n < 10 ? `0${n}` : n;

// 生成时分秒数组（合并循环，减少冗余）
const hours = Array.from({length:24}, (_,i) => padZero(i));
const [minutes, seconds] = [Array.from({length:60}, (_,i) => padZero(i)), [...hours.slice(0,60)]];

// 生成年月日数组（精简循环+变量）
const year = d.getFullYear();
const years = Array.from({length:6}, (_,i) => year + i);
const months = Array.from({length:12}, (_,i) => padZero(i+1));
const days = Array.from({length:31}, (_,i) => padZero(i+1));

// 原代码保留的当前时间变量（按需保留）
const hour = d.getHours();
const minute = d.getMinutes();
const second = d.getSeconds();
const month = d.getMonth() + 1;
const day = d.getDate();

export default {
    data() {
        return {
            hours,
            minutes,
            seconds,
            value: [hour, minute, second],

            value2: [],
            /* 是否显示控件 */
            showPicker: false,
            /* 选择时间段 */
            time: [
                years[0] + "-" + months[month - 1] + "-" + days[day - 1],
                hours[0] + ":" + minutes[0] + ":" + seconds[0],
            ],

            // 时间选择器
            years,
            months,
            days,
            value1: [0, month - 1, day - 1],
        };
    },
    // is_days是否显示日（默认显示），is_min是否显示时分秒（默认显示）
    props: ["themeColor", "urseTime", "is_min", "is_days", "is_noweData"],
    methods: {
        show(data) {
            if (data) {
                let sparr = [];
                let arr = data.split(" ");

                for (let item of arr) {
                    if (item.includes(":")) {
                        sparr.push(item.split(":"));
                    } else {
                        sparr.push(item.split("-"));
                    }
                }

                years.filter((item, index) => {
                    if (item == sparr[0][0]) {
                        sparr[0][0] = index;
                    }
                });

                months.filter((item, index) => {
                    if (item == sparr[0][1]) {
                        sparr[0][1] = index;
                    }
                });

                days.filter((item, index) => {
                    if (item == sparr[0][2]) {
                        sparr[0][2] = index;
                    }
                });
                if (sparr[1] != undefined) {
                    sparr[1].filter((item, index) => {
                        sparr[1][index] = Number(item);
                    });
                }
                this.value1 = [];
                this.value2 = [];
                this.value1.push(...sparr[0]);
                if (sparr[1] != undefined) {
                    this.value2.push(...sparr[1]);
                }

                // 进来的时候，初始化选择的时间为原来的营业时间
                this.time = arr;
            }
            setTimeout(() => {
                this.showPicker = true;
            }, 0);
        },
        maskClick() {
            this.pickerCancel();
        },
        pickerCancel() {
            this.showPicker = false;
        },
        pickerConfirm() {
            // 获取现在的时分秒
            let date = new Date();
            console.log("获取现在的年月日时分秒", date.getTime());
           let dateios = this.time[0].replace(/-/g, '/')
            console.log(
                "获取选择的年月日时分秒",
                dateios,
                this.time,
                Date.parse(new Date(this.time[0] + " " + this.time[1]))
                
            );
            if (this.is_noweData) {
                if (date.getTime() > Date.parse(new Date(dateios + " " + this.time[1]))) {
                    uni.showToast({
                        title: "不能小于当前时间",
                        duration: 1000,
                        icon: "none",
                    });
                } else {
                    this.$emit("onConfirm", this.time);
                    this.showPicker = false;
                }
            } else {
                this.$emit("onConfirm", this.time);
                this.showPicker = false;
            }
        },
        showPickerView() {
            this.showPicker = true;
        },
        bindChange1(e) {
            const val = e.detail.value;

            let days = [];

            if (
                this.isLeapYear(this.years[val[0]]) &&
                this.months[val[1]] == "02"
            ) {
                for (let i = 1; i <= 29; i++) {
                    if (i < 10) {
                        i = "0" + i;
                    }
                    days.push(i);
                }
            } else if (this.months[val[1]] == "02") {
                for (let i = 1; i <= 28; i++) {
                    if (i < 10) {
                        i = "0" + i;
                    }
                    days.push(i);
                }
            } else {
                if (
                    this.months[val[1]] == "01" ||
                    this.months[val[1]] == "03" ||
                    this.months[val[1]] == "05" ||
                    this.months[val[1]] == "07" ||
                    this.months[val[1]] == "08" ||
                    this.months[val[1]] == "10" ||
                    this.months[val[1]] == "12"
                ) {
                    for (let i = 1; i <= 31; i++) {
                        if (i < 10) {
                            i = "0" + i;
                        }
                        days.push(i);
                    }
                } else {
                    for (let i = 1; i <= 30; i++) {
                        if (i < 10) {
                            i = "0" + i;
                        }
                        days.push(i);
                    }
                }
            }
            this.days = days;
            this.value1 = val;
            this.time[0] =
                this.years[val[0]] +
                "-" +
                this.months[val[1]] +
                "-" +
                this.days[val[2]];
        },
        bindChange2(e) {
            const val = e.detail.value;
            let minute = this.minutes[val[1]]==undefined?'00':this.minutes[val[1]]
            let send =  this.minutes[val[2]]==undefined?'00':this.minutes[val[2]]
            this.time[1] =this.hours[val[0]] +":" +minute +":" +send;
        },
        // 求闰年
        isLeapYear(year) {
            var cond1 = year % 4 == 0; //条件1：年份必须要能被4整除
            var cond2 = year % 100 != 0; //条件2：年份不能是整百数
            var cond3 = year % 400 == 0; //条件3：年份是400的倍数 //当条件1和条件2同时成立时，就肯定是闰年，所以条件1和条件2之间为“与”的关系。 //如果条件1和条件2不能同时成立，但如果条件3能成立，则仍然是闰年。所以条件3与前2项为“或”的关系。 //所以得出判断闰年的表达式：
            var cond = (cond1 && cond2) || cond3;
            if (cond) {
                return true;
            } else {
                return false;
            }
        },
    },
    created() {
        this.setLang();

        let val = this.value1;
        let days = [];
        if (
            this.isLeapYear(this.years[val[0]]) &&
            this.months[val[1]] == "02"
        ) {
            // 闰年二月
            for (let i = 1; i <= 29; i++) {
                if (i < 10) {
                    i = "0" + i;
                }
                days.push(i);
            }
        } else if (this.months[val[1]] == "02") {
            // 非闰年二月
            for (let i = 1; i <= 28; i++) {
                if (i < 10) {
                    i = "0" + i;
                }
                days.push(i);
            }
        } else {
            // 非二月
            if (
                this.months[val[1]] == "01" ||
                this.months[val[1]] == "03" ||
                this.months[val[1]] == "05" ||
                this.months[val[1]] == "07" ||
                this.months[val[1]] == "08" ||
                this.months[val[1]] == "10" ||
                this.months[val[1]] == "12"
            ) {
                for (let i = 1; i <= 31; i++) {
                    if (i < 10) {
                        i = "0" + i;
                    }
                    days.push(i);
                }
            } else {
                for (let i = 1; i <= 30; i++) {
                    if (i < 10) {
                        i = "0" + i;
                    }
                    days.push(i);
                }
            }
        }
        this.days = days;
    },
    watch: {},
};
</script>

<style scoped lang="less">
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

.pickerMask {
    position: fixed;
    z-index: 1000;
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
    z-index: 3000;
}

.mpvue-picker-view-show {
    transform: translateY(0);
}

.mpvue-picker__hd {
    display: flex;
    padding: 9px 15px;
    background-color: #f4f5f6;
    height: 112rpx;
    position: relative;
    text-align: center;
    font-size: 17px;
    border-radius: 24rpx 24rpx 0rpx 0rpx;
}

.pickerSelected {
    color: #fa5151;
}
.mpvue-picker__hd:after {
    content: " ";
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
</style>
