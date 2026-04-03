<template>
    <view class="fixed" :style="{ height: fixed_height }">
        <!-- 切换导航-无左右滑动效果 只适配了小于等于6个切换导航 大于6个请不要使用-->
        <view class="switchNav">
            <ul
                class="is_switchNav"
                :style="{
                    borderRadius: is_switchNav_radius,
                    backgroundColor: is_switchNav_obj.backgroundColor,
                    paddingTop: is_switchNav_padT,
                    paddingBottom: is_switchNav_padB,
                }"
            > 
                <li
                    class="is_switchNav_li"
                    :class="{ is_switchNav_choose: isSwitchNavOne == index }"
                    :style="{
                        color:
                            isSwitchNavOne == index
                                ? is_switchNav_obj.choose_color
                                : is_switchNav_obj.color,
                    }"
                    v-for="(item, index) in is_switchNav"
                    :key="index"
                    @tap="_isSwitchNav(index)"
                >
                    {{ item }} 
                    <view
                        v-if="
                            !Array.isArray(tag_num) &&
                            index === is_switchNav.length - 1 &&
                            is_tag &&
                            isSwitchNavOne === is_switchNav.length - 1 &&
                            tag_num!=0
                        "
                        class="tag"
                        >{{ tag_num }}</view
                    >
                    <view
                        v-if=" 
                            Array.isArray(tag_num) &&
                            is_tag && 
                            tag_num[index] 
                        "
                        class="tag"
                        >{{ tag_num[index] }}</view
                    >
                </li>
                <!-- 
                计算：【一个格子居中left值 + 一个格子的长度 * N】
                一个格子的长度：750等比平分 2:1（375） 3:1（250） 4:1（187.5） 5:1（150） 6:1（125）
                一个格子居中left值： （一个格子的长度 - 下划线长度100）/ 2
                N的值：当前选择的下标
                -->
                <li
                    v-if="is_switchNav.length > 1"
                    class="is_switchNav_line"
                    :style="{
                        left:
                            (is_switchNav.length == 2
                                ? 137.5
                                : is_switchNav.length == 3
                                ? 75
                                : is_switchNav.length == 4
                                ? 43.75
                                : is_switchNav.length == 5
                                ? 25
                                : 12.5) +
                            (is_switchNav.length == 2
                                ? 375
                                : is_switchNav.length == 3
                                ? 250
                                : is_switchNav.length == 4
                                ? 187.5
                                : is_switchNav.length == 5
                                ? 150
                                : 125) *
                                isSwitchNavOne +
                            'rpx',
                        borderBottom: is_switchNav_obj.borderBottom,
                    }"
                ></li>
            </ul>
        </view>
    </view>
</template>

<script>
export default {
    props: {
        // 是否需要标记
        is_tag: {
            type: Boolean,
            default: false,
        },
        tag_num: {
            type: [Number,Array],
            default: 0,
        },
        //必传  显示导航
        is_switchNav: {
            type: Array,
            default: [],
        },
        //非必传
        is_switchNav_obj: {
            type: Object,
            default: () => ({
                color: "#999999", //未选中字体颜色
                choose_color: "#333333", //选中字体颜色
                borderBottom: "4rpx solid #FA5151", //选中的下划线
                backgroundColor: "#fff", //背景色
            }),
        },
        //非必传
        is_switchNav_radius: {
            type: String,
            default: "0 0 24rpx 24rpx", //圆角
        },
        //非必传
        is_switchNav_padT: {
            type: String,
            default: "32rpx", //上 内边距
        },
        //非必传
        is_switchNav_padB: {
            type: String,
            default: "48rpx", //下 内边距
        },
        // 是否最后一项不做下标处理
        is_no_tag: { type: Boolean, default: false },
        //当前选中的标签
        isSwitchNav:{
            type:Number,
            default:0
        }
    },
    data() {
        return {
            fixed_height: "", //高度占位，防止遮挡。
            is_choose: true, //防重复点击
            isSwitchNavOne:this.isSwitchNav
        };
    },
    mounted() {
        const query = uni.createSelectorQuery().in(this);
        query
            .select(".is_switchNav")
            .boundingClientRect((data) => {
                this.fixed_height = data.height + "px";
            })
            .exec();
    },
    methods: {
        //选择导航
        _isSwitchNav(index) {
            if (this.is_choose) { 
                if (this.is_no_tag && index === this.is_switchNav.length - 1) {
                    this.isSwitchNavOne = 0;
                } else {
                    this.isSwitchNavOne = index;
                }
                this.is_choose = false;
                setTimeout(() => { 
                    this.is_choose = true;
                }, 1000);
                this.$emit("choose", index);
            } else {
                console.log("防重复点击状态111", this.is_choose);
                uni.showToast({
                    title: this.$t('util.qwcfdj'),
                    icon:'none'
                })
            }
        },
    },
};
</script>

<style lang="less" scoped>
@import url("@/laike.less");
  .fixed{
      min-height: 94rpx;
  }
.switchNav {
    position: fixed;
    z-index: 999;
    width: 100%;
    height: auto;
}
.is_switchNav {
    width: 100%;
    height: auto;
    //color: #999999;
    //background-color: #fff;
    display: flex;
    align-items: center;
    justify-content: space-around;
    //border-radius: 0 0 24rpx 24rpx;
    //padding: 32rpx 0 48rpx 0;
    .is_switchNav_li {
        position: relative;
        z-index: 2;
        flex: 1;
        height: 70rpx;
        line-height: 70rpx;
        font-size: 28rpx;
        text-align: center;
        .tag {
            position: absolute;
            top: 0rpx;
            right: 35rpx;
            width: 32rpx;
            height: 32rpx;
            border-radius: 50%;
            box-sizing: border-box;
            background-color: #fa5151;
            color: #fff;
            font-size: 24rpx;
            text-align: center;
            line-height: 32rpx;
        }
    }
    .is_switchNav_choose {
        font-weight: bold;
        //color: #333333;
        font-size: 32rpx;
        transition: all 0.1s;
    }
    .is_switchNav_line {
        position: absolute;
        left: 0;
        z-index: 1;
        width: 100rpx;
        height: 70rpx;
        //border-bottom: 4rpx solid #FA5151;
        transition: all 0.5s;
    }
}
</style>
