<template>
    <view class="isPublicModeMCH">
        <template v-if="isData && isData.length">
            <view class="content_newFour">
                <view>
                    <view class="content_newFour_tilte1">
                        {{ title }}
                    </view>
                    <view
                        class="content_newFour_tilte2"
                        @click="_seeMore('/pagesB/home/mchList')"
                    >
                        {{ language.home.more }}
                        <image
                            :src="jiantou"
                            lazy-load="true"
                            style="width: 32rpx; height: 44rpx"
                        ></image>
                    </view>
                </view>
                <view v-if="!opensetting">
                    <scroll-view scroll-x="true" class="scroll_view_gundong">
                        <view
                            class="content_newFour_content"
                            v-for="(item, index) of isData"
                            :key="index"
                            @tap="
                                _seeMore(
                                    '/pagesB/store/store?shop_id=' +
                                        item.shop_id
                                )
                            "
                        >
                            <view>
                                <image
                                    :src="LaiKeTuiCommon.getFastImg(item.backImgUrl ? item.backImgUrl : new_home_gudingImg, 480, 300)"
                                    mode="aspectFill"
                                    lazy-load="true"
                                ></image>
                            </view>
                            <view>
                                <view>
                                    <image
                                        :src="LaiKeTuiCommon.getFastImg(item.headimgurl, 120, 120)"
                                        mode="aspectFill"
                                        lazy-load="true"
                                        @error="handleErrorImg(index)"
                                    ></image>
                                </view>
                            </view>
                            <view>{{ item.name }}</view>
                            <view
                                class="content_newFour_btn"
                                @tap.stop="
                                    _collectMch(
                                        item.shop_id,
                                        item.collection_status
                                    )
                                "
                            >
                                <view v-if="!item.collection_status">{{
                                    language.home.gz
                                }}</view>
                                <view v-else class="yiguanzhu newBgcolor">{{
                                    language.home.ygz
                                }}</view>
                            </view>
                            <!-- 定位图标 -->
                            <div class="live_icon" v-if="item.livingStatus&&item.mch_is_open==1">
                                <!-- <img :src="live" alt=""> -->
                                <i class="one"></i>
                                <i class="two"></i>
                                <i class="three"></i>
                            </div>
                        </view>
                    </scroll-view>
                </view>
            </view>
        </template>
    </view>
</template>

<script>
export default {
    props: {
        //总数据
        isData: {
            type: Array,
            default: [],
        },
        //标题
        title: {
            type: String,
            default: "推荐商家",
        },
        // --- 分割线 ---

        //定位是否开启？？？
        opensetting: {
            type: Boolean,
            default: false,
        },
    },
    data() {
        return {
            new_home_chajian2:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/new_home_chajian2.png",
            jiantou:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/jiantou.png",
            new_home_gudingImg:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/new_home_gudingImg.png",
        };
    },
    methods: {
        handleErrorImg(index) {
            this.$emit("handleError", index);
        },
        //查看更多
        _seeMore(url) {
            this.$emit("_seeMore", url);
        },
        //查看店铺详情
        _seeGoods(id, p_id) {
            this.$emit("_seeGoods", id, p_id);
        },
        //关注店铺
        _collectMch(id, state) {
            this.$emit("_collectMch", id, state);
        },
    },
};
</script>

<style lang="less" scoped>
@import url("@/laike.less");
.isPublicModeMCH {
    .content_newFour {
        width: 718rpx;
        height: auto;
        margin: 32rpx auto;
        margin-right: 0;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        box-sizing: border-box;
        > view {
            width: 100%;
        }
        > view:first-child {
            display: flex;
            flex-direction: row;
            align-items: center;
            justify-content: space-between;
            .content_newFour_tilte1 {
                min-width: 168rpx;
                //height: 32rpx;
                font-size: 32rpx;
                color: #333333;
                font-weight: bold;
                //font-style: oblique;
                > image {
                    width: 100%;
                    height: 100%;
                }
            }
            .content_newFour_tilte2 {
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 24rpx;
                color: #999999;
                line-height: 32rpx;
                margin-right: 32rpx;
            }
        }
        > view:last-child {
            width: 100%;
            margin-top: 24rpx;
            .scroll_view_gundong {
                //滚动
                width: 100%;
                white-space: nowrap;
                .content_newFour_content {
                    width: 200rpx;
                    height: auto;
                    background-color: #ffffff;
                    border-radius: 16rpx;
                    overflow: hidden;
                    padding-bottom: 16rpx;
                    box-sizing: border-box;
                    margin-right: 16rpx;
                    //滚动
                    display: inline-block;
                    position: relative;

                    > view:nth-child(1) {
                        width: 200rpx;
                        height: 200rpx;
                        border-radius: 16rpx;
                        overflow: hidden;
                        > image {
                            width: 100%;
                            height: 100%;
                        }
                    }
                    > view:nth-child(2) {
                        display: flex;
                        align-content: center;
                        justify-content: center;
                        > view {
                            width: 64rpx;
                            height: 64rpx;
                            border-radius: 64rpx;
                            overflow: hidden;
                            border: 1px solid rgba(0, 0, 0, 0.1);
                            margin-top: -32rpx;
                            position: relative;
                            z-index: 1;
                            > image {
                                width: 100%;
                                height: 100%;
                            }
                        }
                    }
                    > view:nth-child(3) {
                        font-size: 24rpx;
                        font-weight: 500;
                        color: rgba(0, 0, 0, 0.85);
                        line-height: 34rpx;
                        text-align: center;
                        margin: 8rpx auto;
                        // 超出隐藏
                        width: 180rpx;
                        height: auto;
                        display: block;
                        overflow: hidden; /*超出隐藏*/
                        text-overflow: ellipsis; /*隐藏后添加省略号*/
                        -webkit-box-orient: vertical;
                        -webkit-line-clamp: 1; //想显示多少行
                    }
                    .live_icon {
                        flex: none;
                        position: absolute;
                        width: 28rpx;
                        height: 28rpx;
                        top: 8rpx !important;
                        left: 8rpx !important;
                        border-radius: 50%;
                        background-color: #fa5151;
                        box-sizing: border-box;
                        overflow: hidden;
                        i {
                            width: 4rpx;
                            height: 12rpx;
                            background-color: #ffffff;
                            position: absolute;
                            bottom: 5rpx;
                            border-radius: 1.5rpx;
                            left: 0;
                        }
                        .one {
                            left: 4rpx;
                            width: 4rpx; /* 初始宽度 */
                            height: 12rpx; /* 高度 */
                            background-color: #ffffff; /* 背景颜色 */
                            animation: stretch 0.8s infinite alternate; /* 应用动画 */

                            @keyframes stretch {
                                0% {
                                    height: 12rpx; /* 动画开始时的宽度 */
                                }
                                100% {
                                    height: 18rpx; /* 动画结束时的宽度 */
                                }
                            }
                        }
                        .two {
                            left: 12rpx;
                            width: 4rpx; /* 初始宽度 */
                            height: 12rpx; /* 高度 */
                            background-color: #ffffff; /* 背景颜色 */
                            animation: stretch 0.2s infinite alternate; /* 应用动画 */

                            @keyframes stretch {
                                0% {
                                    height: 12rpx; /* 动画开始时的宽度 */
                                }
                                100% {
                                    height: 18rpx; /* 动画结束时的宽度 */
                                }
                            }
                        }
                        .three {
                            left: 20rpx;
                            width: 4rpx; /* 初始宽度 */
                            height: 12rpx; /* 高度 */
                            background-color: #ffffff; /* 背景颜色 */
                            animation: stretch 0.6s infinite alternate; /* 应用动画 */

                            @keyframes stretch {
                                0% {
                                    height: 12rpx; /* 动画开始时的宽度 */
                                }
                                100% {
                                    height: 18rpx; /* 动画结束时的宽度 */
                                }
                            }
                        }

                        // img {
                        //     width: 28rpx;
                        //     height: 28rpx;
                        //     vertical-align: middle;
                        // }
                    }
                    .content_newFour_btn {
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        > view {
                            width: 96rpx;
                            height: 48rpx;
                            background: rgba(250, 81, 81, 0.1);
                            border-radius: 24rpx;
                            font-size: 24rpx;
                            font-weight: 500;
                            color: #fa5151;
                            line-height: 48rpx;
                            text-align: center;
                        }
                        .yiguanzhu {
                            font-weight: 400 !important;
                            color: #999999 !important;
                        }
                    }
                    .newBgcolor {
                        background: rgba(244, 245, 246, 1) !important;
                    }
                }
            }
        }
    }
}
</style>
