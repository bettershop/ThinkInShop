<template>
    <view class="fixed" :style="{ height: fixed_height }">
        <view class="isSearch">
            <!-- 有搜索按钮的 搜索框 -->
            <template v-if="is_search_type == 1">
                <view
                    class="is_search"
                    :style="{
                        borderRadius: is_search_radius,
                        paddingTop: is_search_padT,
                        paddingBottom: is_search_padB,
                    }"
                >
                    <view>
                        <view class="search_input" :style="{
                                backgroundColor: is_background}">
                            <image
                                class="searchImg"
                                :src="serchimg"
                                alt=""
                            ></image>
                            <input
                                v-model="is_search"
                                type="text"
                                :placeholder="is_search_prompt"
                                placeholder-style="color:#999"
                            />
                            <image
                                v-show="is_search.length > 0"
                                @tap="_clearData"
                                class="cancel"
                                :src="sc_icon"
                                mode=""
                            ></image>
                        </view>
                        <view
                            class="search_btn"
                            @tap="_isSearch"
                            :style="{
                                backgroundColor: is_search_btn_bgColor,
                                color: is_search_btn_color,
                                marginLeft: is_search_left,
                                border: is_border,
                            }"
                        >
                            {{ btn }}
                        </view>
                    </view>
                </view>
            </template>
            <!-- 没有搜索按钮的 搜索框 按确认键搜索-->
            <template v-if="is_search_type == 2">
                <view
                    class="is_search"
                    :style="{
                        borderRadius: is_search_radius,
                        paddingTop: is_search_padT,
                        paddingBottom: is_search_padB,
                    }"
                >
                    <view>
                        <view class="search_input no_btn">
                            <image
                                class="searchImg"
                                :src="serchimg"
                                alt=""
                            ></image>
                            <input
                                @confirm="_isSearch"
                                v-model="is_search"
                                type="text"
                                :placeholder="is_search_prompt"
                                placeholder-style="color:#999"
                            />
                            <image
                                v-show="is_search.length > 0"
                                @tap="_clearData"
                                class="cancel"
                                :src="sc_icon"
                                mode=""
                            ></image>
                        </view>
                    </view>
                </view>
            </template>
        </view>
    </view>
</template>

<script>
export default {
    props: {
        //必传  搜索种类 0隐藏 1有搜索按钮  2没有搜索按钮
        is_search_type: {
            type: Number,
            default: 0,
        },
        //非必传  圆角 （请用rpx）
        is_search_radius: {
            type: String,
            default: "0 0 0 0",
        },
        //非必传  上内边距
        is_search_padT: {
            type: String,
            default: "0",
        },
        //非必传  下内边距
        is_search_padB: {
            type: String,
            default: "0",
        },
        is_search_left: {
            type: String,
            default: "0",
        },
        //非必传  搜索提示文字
        is_search_prompt: {
            type: String,
            default: "请输入订单号",
        },

        // --- 分割线 ---

        //非必传  搜索按钮背景色
        is_search_btn_bgColor: {
            type: String,
            default: "initial",
        },
        //非必传  搜索按钮字体颜色
        is_search_btn_color: {
            type: String,
            default: "#333333",
        },
        is_background:{
            type: String,
            default: "#f3f3f3",
        },
        is_border: {
            type: String,
            default: "1rpx solid #eee;",
        },
    },
    data() {
        return {
            fixed_height: "", //高度占位，防止遮挡。
            is_search: "", //搜索的内容
            is_choose: true, //防重复点击
            serchimg:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/searchNew.png", //搜索
            sc_icon:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/delete2x.png", //清除
        };
    },
    mounted() {
        const query = uni.createSelectorQuery().in(this);
        query
            .select(".is_search")
            .boundingClientRect((data) => {
                this.fixed_height = data.height + "px";
            })
            .exec();
    },
    computed: {
        btn() {
            let new_data = this.language.yushou_settlement.ss;
            return new_data;
        },
    },
    methods: {
        //点击搜索
        _isSearch() {
            if (this.is_choose) {
                this.is_choose = false;
                setTimeout(() => {
                    this.is_choose = true;
                }, 1000);
                this.$emit("search", this.is_search);
            } else {
                console.log("防重复点击状态", !this.is_choose);
            }
        },
        //清除搜索文字
        _clearData() {
            this.is_search = "";
            this.$emit("clearch");
        },
    },
};
</script>

<style lang="less" scoped>
@import url("@/laike.less");
.isSearch {
    position: fixed;
    z-index: 999;
    width: 100%;
    height: auto;
}
.is_search {
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: #fff;
    > view {
        display: flex;
        align-items: center;
        justify-content: space-between;
        width: 100%;
        overflow: hidden;
        z-index: 99;
        padding: 20rpx 24rpx;
        box-sizing: border-box;
        .no_btn {
            width: auto;
            flex: 1;
        }
        .search_input {
            position: relative;
            display: flex;
            align-items: center;
            padding: 0 57rpx;
            width: 576rpx;
            height: 68rpx;
            background: rgba(243, 243, 243, 1);
            border-radius: 35rpx;
            box-sizing: border-box;
            input {
                flex: 1;
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
            width: 120rpx;
            height: 64rpx;
            text-align: center;
            line-height: 64rpx;
            border-radius: 32rpx;
            font-size: 28rpx;
            font-weight: 500;
            border: 1rpx solid #eee;
            white-space: nowrap;
        }
    }
}
</style>
