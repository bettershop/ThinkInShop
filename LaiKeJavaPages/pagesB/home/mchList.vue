<template>
    <view class="container">
        <heads
            :title="language.mchList.title"
            :border="true"
            :bgColor="bgColor"
            ishead_w="2"
            titleColor="#333333"
        ></heads>
        <!-- 分类 -->
        <view
            :style="{ height: fixed_height ? fixed_height : '88rpx' }"
            v-if="mch_arr_s && mch_arr_s.length"
        >
            <scroll-view-nav
                :is_ScrollView="mch_arr_s"
                @_changeNav="fl_cid"
            ></scroll-view-nav>
        </view>
        <!-- 内容 -->
        <view class="content">
            <view class="list" v-for="(item, index) of list" :key="index">
                <view class="list_right">
                    <view class="list_right_top">
                        <view class="list_left " :class="item.livingStatus&&item.mch_is_open==1?'list_left_line':''">
                            <image :src="item.headimgurl == '' ? default_img : item.headimgurl" mode=""></image>
                            <view v-if="item.is_open == 2">{{
                                language.mchList.Closed
                            }}</view>
                            <!-- 定位图标 -->
                            <div class="live_icon" v-if="item.livingStatus&&item.mch_is_open==1">
                                <!-- <img :src="live" alt=""> -->
                                <i class="one"></i>
                                <i class="two"></i>
                                <i class="three"></i>
                            </div>
                        </view>
                        <view class="new_class">
                            <view class="list_right_top_title">
                                <view class="shop_name">
                                    <view class="tag"  v-if="item.livingStatus&&item.mch_is_open==1">直播中</view>
                                    {{ item.name }}
                                </view>
                                <view
                                    class="shop_class"
                                    v-if="item.shop_information"
                                    >{{ item.shop_information }}</view
                                >
                            </view>
                            <view
                                class="list_right_top_btn"
                                @tap="
                                    toUrl(
                                        '/pagesB/store/store?shop_id=' +
                                            item.shop_id
                                    )
                                "
                                >{{ language.mchList.store }}</view
                            >
                        </view>
                    </view>
                    <view class="list_right_bottom">
                        <view>
                            <view>{{ item.quantity_on_sale }}</view>
                            <text>{{ language.mchList.sale }}</text>
                        </view>
                        <view>
                            <view>{{ item.quantity_sold }}</view>
                            <text>{{ language.mchList.Sold }}</text>
                        </view>
                        <view>
                            <view>{{ item.collection_num }}</view>
                            <text>{{ language.mchList.collectors }}</text>
                        </view>
                    </view>
                </view>
            </view>
        </view>
    </view>
</template>

<script>
import scrollViewNav from "@/components/aComponents/switchNavThree.vue";
export default {
    data() {
        return {
            bgColor: [
                {
                    item: "#FFFFFF",
                },
                {
                    item: "#FFFFFF",
                },
            ],
            title: "优选店铺",
            page: 1,
            loadingType: 0,
            list: [],
            mch_arr: [], //店铺分类列表
            mch_arr_s: [], //店铺分类 传入组件适配
            mch_data: [], //店铺分类总数据
            fixed_height: "",
            live:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon1/live.png", //直播icon
            default_img:"../../static/img/default-img.png",
            cid: "", //分类id
        };
    },
    components: {
        scrollViewNav,
    },
    beforeUpdate() {},
    updated() {
        const query = uni.createSelectorQuery().in(this);
        query
            .select(".isScrollView")
            .boundingClientRect((data) => {
                this.fixed_height = data.height + "px";
            })
            .exec();
    },
    onLoad(option) {
        this.page = 1;
        this.axios_fl();
    },
    //上拉加载更多
    onReachBottom() {
        if (this.loadingType == 0) {
            this.page += 1;
            this.axios(1, this.cid);
        }
    },
    onShow() {},
    methods: {
        fl_cid(item, index) {
            this.page = 1;
            let cid = this.mch_data[index].id;
            this.cid = cid;
            //滚到顶部
            uni.pageScrollTo({
                scrollTop: 0,
                duration: 0,
            });
            this.axios(1, cid);
        },
        toUrl(url) {
            uni.navigateTo({
                url,
            });
        },
        axios(fl, fl_cid) {
            let data = {
                api: "app.index.recommend_stores",
                longitude: uni.getStorageSync("longitude"), // 经度
                latitude: uni.getStorageSync("latitude"), // 纬度
                page: this.page, // 页面
            };

            //判断是否 选择的是 分类店铺
            if (fl) {
                data.cid = fl_cid;
            }

            this.$req.post({ data }).then((res) => {
                let { data } = res;
                if (res.code == 200) {
                    if (this.page == 1) {
                        this.list = [];
                    }
                    this.list.push(...data.list);

                    if (data.list.length > 0) {
                        this.loadingType = 0;
                    } else {
                        this.loadingType = 2;
                    }
                } else {
                    uni.showToast({
                        title: res.message,
                        icon: "none",
                    });
                }
            });
        },
        //获取分类
        axios_fl() {
            let data = {
                api: "app.index.mchClass",
            };
            this.$req.post({ data }).then((res) => {
                let { code, message, data } = res;
                if (code == 200) {
                    //返回的分类列表
                    this.mch_data = data.list;
                    //分类的名称存入数组
                    this.mch_arr = [];
                    data.list.forEach((item, index) => {
                        this.mch_arr.push(item.name);
                    });
                    //适配公共组件
                    if (this.mch_arr.length) {
                        this.mch_arr_s = this.mch_arr.map((item, index) => {
                            return (item = { pname: item });
                        });
                    }
                } else {
                    uni.showToast({
                        title: message,
                        icon: "none",
                    });
                }
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
<style lang="less">
@import url("@/laike.less");
@import url("../../static/css/home/mchList.less");
// 图标
/deep/.list_right {
    .list_right_top {
        .list_left_line {
            border: 2rpx solid #fa5151;
            border-radius: 16rpx;
            img {
                border-radius: 16rpx;
            }
        }
        .list_left {
            position: relative;
            .live_icon {
                flex: none;
                position: absolute;
                width: 28rpx;
                height: 28rpx;
                bottom: 4rpx !important;
                right: 4rpx !important;
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
        }
    }
}
/deep/.isScrollView {
    position: fixed;
    z-index: 999;
    width: 100%;
    height: auto;
    background-color: #ffffff;
    padding-bottom: 10rpx;
    border-radius: 0 0 24rpx 24rpx;
}
/deep/.isScrollView_nav_item {
    justify-content: center !important;
}
/deep/.isScrollView_nav_item.active:after {
    bottom: 0 !important;
    width: 96rpx !important;
    height: 4rpx !important;
}
.new_class {
    flex: 1;
    display: flex;
    justify-content: space-between;
}
.shop_class {
    font-size: 24rpx;
    color: #999999;
    width: 300rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    margin-top: 10rpx;
}
/deep/.shop_name {
    width: 316rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 32rpx !important;
    display: flex;
    align-items: center;
    .tag {
        box-sizing: border-box;
        width: 88rpx;
        height: 40rpx;
        background-color: #fa5151;
        color: #ffffff;
        font-size: 24rpx !important;
        display: flex;
        justify-content: center;
        align-items: center;
        border-radius: 16rpx;
        margin-right: 16rpx;
    }
}
</style>
