<template>
    <view class="u-countdown">
        <view v-if="separator == 'colon'">
            <view class="u-countdown-item" :style="[itemStyle]"
                v-if="showDays && (hideZeroDay || (!hideZeroDay && d != '00'))">
                <view class="u-countdown-time" :style="[letterStyle]">
                    {{ d }}
                </view>
            </view>
            <view class="u-countdown-colon"
                :style="{fontSize: separatorSize + 'rpx', color: separatorColor, paddingBottom: separator == 'colon' ? '4rpx' : 0}"
                v-if="showDays && (hideZeroDay || (!hideZeroDay && d != '00'))">
            </view>
            <view class="u-countdown-item" :style="[itemStyle]" v-if="showHours">
                <view class="u-countdown-time" :style="{ fontSize: fontSize + 'rpx', color: color}">
                    {{ h }}
                    <!-- <text v-if="isShow">DAYS</text> -->
                </view>
            </view>
            <view class="u-countdown-colon"
                :style="{fontSize: separatorSize + 'rpx', color: separatorColor, paddingBottom: separator == 'colon' ? '4rpx' : 0}"
                v-if="showHours">
            </view>
            <view class="u-countdown-item" :style="[itemStyle]" v-if="showMinutes">
                <view class="u-countdown-time" :style="{ fontSize: fontSize + 'rpx', color: color}">
                    {{ i }}
                    <!-- <text v-if="isShow">HOURS</text> -->
                </view>
            </view>
            <view class="u-countdown-colon"
                :style="{fontSize: separatorSize + 'rpx', color: separatorColor, paddingBottom: separator == 'colon' ? '4rpx' : 0}"
                v-if="showMinutes">
            </view>
            <view class="u-countdown-item" :style="[itemStyle]" v-if="showSeconds">
                <view class="u-countdown-time" :style="{ fontSize: fontSize + 'rpx', color: color}">
                    {{ s }}
                    <!-- <text v-if="isShow">MINS</text> -->
                </view>

            </view>
            <view class="u-countdown-colon"
                :style="{fontSize: separatorSize + 'rpx', color: separatorColor, paddingBottom: separator == 'colon' ? '4rpx' : 0}"
                v-if="showSeconds && separator == 'zh'">
                {{language.datatime.second}}
            </view>
        </view>
        <view v-else style="display: flex;">
            <view v-if="showDays && (hideZeroDay || (!hideZeroDay && d != '00'))">
                <view class="u-countdown-item" :style="[itemStyle]"
                    v-if="showDays && (hideZeroDay || (!hideZeroDay && d != '00'))">
                    <view class="u-countdown-time" :style="[letterStyle]">
                        {{ d }}

                    </view>
                </view>
                <view class="u-countdown-colon"
                    :style="{fontSize: separatorSize + 'rpx', color: separatorColor, paddingBottom: separator == 'colon' ? '4rpx' : 0}"
                    v-if="showDays && (hideZeroDay || (!hideZeroDay && d != '00'))">
                    {{language.bargain.days}}
                </view>
            </view>
            <view class="u-countdown-colon"
                :style="{fontSize: separatorSize + 'rpx', color: separatorColor, paddingBottom: separator == 'colon' ? '4rpx' : 0,padding:'0rpx',height: '46rpx'}"
                v-if="showDays && (hideZeroDay || (!hideZeroDay && d != '00'))">
            </view>
            <view v-if="showHours">
                <view class="u-countdown-item" :style="[itemStyle]" v-if="showHours">
                    <view class="u-countdown-time" :style="{ fontSize: fontSize + 'rpx', color: color}">
                        {{ h }}
                        <!-- <text v-if="isShow">DAYS</text> -->
                    </view>
                </view>

              <view class="u-countdown-colon"
                    :style="{fontSize: separatorSize + 'rpx', color: separatorColor, paddingBottom: separator == 'colon' ? '4rpx' : 0}"
                    v-if="showHours">
                    {{ isTime?language.datatime.hour:':'}}
                </view>
            </view>
            <view class="u-countdown-colon"
                :style="{fontSize: separatorSize + 'rpx', color: separatorColor, paddingBottom: separator == 'colon' ? '4rpx' : 0,padding:'0rpx',height: '46rpx'}"
                v-if="showHours">
            </view>
            <view v-if="showMinutes">
                <view class="u-countdown-item" :style="[itemStyle]" v-if="showMinutes">
                    <view class="u-countdown-time" :style="{ fontSize: fontSize + 'rpx', color: color}">
                        {{ i }}
                        <!-- <text v-if="isShow">HOURS</text> -->
                    </view>
                </view>
               <view class="u-countdown-colon"
                    :style="{fontSize: separatorSize + 'rpx', color: separatorColor, paddingBottom: separator == 'colon' ? '0rpx' : 0}"
                    v-if="showMinutes">
                    {{ isTime?language.datatime.min:':'}}
                </view>
            </view>
            <view class="u-countdown-colon"
                :style="{fontSize: separatorSize + 'rpx', color: separatorColor, paddingBottom: separator == 'colon' ? '4rpx' : 0,height: '46rpx'}"
                v-if="showMinutes">
            </view>
            <view v-if="showSeconds">
                <view class="u-countdown-item" :style="[itemStyle]" v-if="showSeconds">
                    <view class="u-countdown-time" :style="{ fontSize: fontSize + 'rpx', color: color}">
                        {{ s }}
                        <!-- <text v-if="isShow">MINS</text> -->
                    </view>

                </view>
              <!-- <view class="u-countdown-colon"
                    :style="{fontSize: separatorSize + 'rpx', color: separatorColor, paddingBottom: separator == 'colon' ? '4rpx' : 0}"
                    v-if="showSeconds">
                    {{ isTime?language.datatime.second:':'}}
                </view> -->
            </view>
        </view>
    </view>
</template>

<script>
    /**
     * countDown 倒计时
     * @description 该组件一般使用于某个活动的截止时间上，通过数字的变化，给用户明确的时间感受，提示用户进行某一个行为操作。
     * @tutorial https://www.uviewui.com/components/countDown.html
     * @property {String Number} timestamp 倒计时，单位为秒
     * @property {Boolean} autoplay 是否自动开始倒计时，如果为false，需手动调用开始方法。见官网说明（默认true）
     * @property {String} separator 分隔符，colon为英文冒号，zh为中文（默认colon）
     * @property {String Number} separator-size 分隔符的字体大小，单位rpx（默认30）
     * @property {String} separator-color 分隔符的颜色（默认#303133）
     * @property {String Number} font-size 倒计时字体大小，单位rpx（默认30）
     * @property {Boolean} show-border 是否显示倒计时数字的边框（默认false）
     * @property {Boolean} hide-zero-day 当"天"的部分为0时，隐藏该字段 （默认true）
     * @property {String} border-color 数字边框的颜色（默认#303133）
     * @property {String} bg-color 倒计时数字的背景颜色（默认#ffffff）
     * @property {String} color 倒计时数字的颜色（默认#303133）
     * @property {String} height 数字高度值(宽度等同此值)，设置边框时看情况是否需要设置此值，单位rpx（默认auto）
     * @property {Boolean} show-days 是否显示倒计时的"天"部分（默认true）
     * @property {Boolean} show-hours 是否显示倒计时的"时"部分（默认true）
     * @property {Boolean} show-minutes 是否显示倒计时的"分"部分（默认true）
     * @property {Boolean} show-seconds 是否显示倒计时的"秒"部分（默认true）
     * @event {Function} end 倒计时结束
     * @event {Function} change 每秒触发一次，回调为当前剩余的倒计秒数
     * @example <u-count-down ref="uCountDown" :timestamp="86400" :autoplay="false"></u-count-down>
     */
    export default {
        name: 'u-count-down',
        props: {
            // 倒计时的时间，秒为单位
            timestamp: {
                type: [Number, String],
                default: 0
            },
            // 是否自动开始倒计时
            autoplay: {
                type: Boolean,
                default: true
            },
            // 用英文冒号(colon)或者中文(zh)当做分隔符，false的时候为中文，如："11:22"或"11时22秒"
            separator: {
                type: String,
                default: 'colon'
            },
            // 分隔符的大小，单位rpx
            separatorSize: {
                type: [Number, String],
                default: 24
            },
            // 分隔符颜色
            separatorColor: {
                type: String,
                default: "#303133"
            },
            // 字体颜色
            color: {
                type: String,
                default: '#303133'
            },
            // 字体大小，单位rpx
            fontSize: {
                type: [Number, String],
                default: 24
            },
            // 背景颜色
            bgColor: {
                type: String,
                default: ''
            },
            // 数字框高度，单位rpx
            height: {
                type: [Number, String],
                default: 'auto'
            },
            // 是否显示数字框
            showBorder: {
                type: Boolean,
                default: false
            },
            // 边框颜色
            borderColor: {
                type: String,
                default: '#303133'
            },
            // 是否显示秒
            showSeconds: {
                type: Boolean,
                default: true
            },
            // 是否显示分钟
            showMinutes: {
                type: Boolean,
                default: true
            },
            // 是否显示小时
            showHours: {
                type: Boolean,
                default: true
            },
            // 是否显示“天”
            showDays: {
                type: Boolean,
                default: true
            },
            // 当"天"的部分为0时，不显示
            hideZeroDay: {
                type: Boolean,
                default: false
            },
            isShow: {
                type: Boolean,
                default: false
            },
            isTime: {
                type: Boolean,
                default: false
            }
        },
        watch: {
            // 监听时间戳的变化
            time_difference(newVal, oldVal) {
                // 如果倒计时间发生变化，清除定时器，重新开始倒计时
                this.clearTimer();
                this.start();
            }
        },
        data() {
            return {
                d: '00', // 天的默认值
                h: '00', // 小时的默认值
                i: '00', // 分钟的默认值
                s: '00', // 秒的默认值
                timer: null, // 定时器
                seconds: 0, // 记录不停倒计过程中变化的秒数
                time_difference: 0,
            };
        },
        computed: {
            // 倒计时item的样式，item为分别的时分秒部分的数字
            itemStyle() {
                let style = {};
                if (this.height) {
                    style.height = this.height + 'rpx';
                    style.width = this.height + 'rpx';
                    style.marginBottom = '10rpx'
                }
                if (this.showBorder) {
                    style.borderStyle = 'solid';
                    style.borderColor = this.borderColor;
                    style.borderWidth = '1px';
                }
                if (this.bgColor) {
                    style.backgroundColor = this.bgColor;
                }
                return style;
            },
            // 倒计时数字的样式
            letterStyle() {
                let style = {};
                if (this.fontSize) style.fontSize = this.fontSize + 'rpx';
                if (this.color) style.color = this.color;
                return style;
            }
        },
        mounted() {
            // 自动倒计时逻辑 - 极致体积优化
              this.autoplay && this.timestamp && (() => {
                // 简化截止时间处理：合并字符串操作，减少临时变量
                const deadline = this.timestamp.substr(0,10).replace(/-/g,'/') + this.timestamp.substr(10);
                // 简化当前时间处理：一行生成兼容苹果的时间字符串
                const d = new Date();
                const now = `${d.getFullYear()}/${d.getMonth()+1}/${d.getDate()} ${d.getHours()}:${d.getMinutes()}:${d.getSeconds()}`;
                // 计算时间差（合并运算，减少变量）
                this.time_difference = (Date.parse(deadline) - Date.parse(now))/1000;
                this.start();
              })();
        },

        methods: {

            // 倒计时
            start() {
                // 避免可能出现的倒计时重叠情况
                this.clearTimer();
                if (this.time_difference <= 0) return;
                this.seconds = Number(this.time_difference);
                this.formatTime(this.seconds);
                this.timer = setInterval(() => {
                    this.seconds--;
                    // 发出change事件
                    this.$emit('change', this.seconds);
                    if (this.seconds < 0) {
                        return this.end();
                    }
                    this.formatTime(this.seconds);
                }, 1000);
            },
            // 格式化时间
            formatTime(seconds) {
                // 小于等于0的话，结束倒计时
                seconds <= 0 && this.end();
                let [day, hour, minute, second] = [0, 0, 0, 0];
                day = Math.floor(seconds / (60 * 60 * 24));
                // 判断是否显示“天”参数，如果不显示，将天部分的值，加入到小时中
                // hour为给后面计算秒和分等用的(基于显示天的前提下计算)
                hour = Math.floor(seconds / (60 * 60)) - day * 24;
                // showHour为需要显示的小时
                let showHour = null;
                if (this.showDays) {
                    showHour = hour;
                } else {
                    // 如果不显示天数，将“天”部分的时间折算到小时中去
                    showHour = Math.floor(seconds / (60 * 60));
                }
                minute = Math.floor(seconds / 60) - hour * 60 - day * 24 * 60;
                second = Math.floor(seconds) - day * 24 * 60 * 60 - hour * 60 * 60 - minute * 60;
                // 如果小于10，在前面补上一个"0"
                showHour = showHour < 10 ? '0' + showHour : showHour;
                minute = minute < 10 ? '0' + minute : minute;
                second = second < 10 ? '0' + second : second;
                day = day < 10 ? '0' + day : day;
                this.d = day;
                this.h = showHour;
                this.i = minute;
                this.s = second;
            },
            // 停止倒计时
            end() {
                this.clearTimer();
                this.$emit('end', {});
            },
            // 清除定时器
            clearTimer() {
                if (this.timer) {
                    // 清除定时器
                    clearInterval(this.timer);
                    this.timer = null;
                }
            }
        },
        beforeDestroy() {
            clearInterval(this.timer);
            this.timer = null;
        }
    };
</script>

<style scoped lang="scss">
    .u-countdown {
        /* #ifndef APP-NVUE */
        display: inline-flex;
        /* #endif */
        align-items: center;
    }

    .u-countdown-item {
        // display: flex;
        display: inline-block;
        flex-direction: row;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        // padding: 4rpx 2rpx;
        border-radius: 4rpx;
        white-space: nowrap;
        margin: 0rpx 4rpx;
        transform: translateZ(0);
      
    }

    .u-countdown-time {
        position: relative;
        margin: 0;
        padding: 0;
        line-height: 1;
        height: 34rpx;
        border-radius: 4rpx;
        text-align: center;
        line-height: 34rpx;
    }

    .u-countdown-time text {
        font-size: 24rpx;
        font-weight: 400;
        color: #000000;
        position: absolute;
        bottom: -20rpx;
        left: 50%;
        transform: translateX(-50%);
    }

    .u-countdown-colon {
        // display: flex;
        display: inline-block;
        flex-direction: row;
        justify-content: center;
        // padding: 0 5rpx;
        line-height: 1;
        align-items: center;
        padding-bottom: 4rpx;
    }

    .u-countdown-scale {
        transform: scale(0.9);
        transform-origin: center center;
    }

    .coundown .u-countdown-colon {
        display: flex;
    }
</style>
