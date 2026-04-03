<template>
    <div class="conten">
        <!-- 头部 -->
        <view class="bill_mask" v-if="versionUpdate3">
            <view class="bill_conten">
                <view class="bill_header">
                    {{ systemMsgTitle }}
                </view>
                <scroll-view
                    scroll-y="true"
                    show-scrollbar="true"
                    class="bill_body"
                >
                    <rich-text :nodes="content"></rich-text>
                </scroll-view>
                <view class="bill_footer">
                    <view class="foobtn" @click.stop="cancel()">
                        <view class="text">知道啦</view>
                        <view class="no_show">
                            <image
                                v-if="is_show"
                                @tap.stop="setSelected()"
                                class="lk-label-image"
                                :src="selectimg"
                            ></image>
                            <div
                                v-if="!is_show"
                                @tap.stop="setSelected()"
                                class="lk-label-divs"
                            ></div>
                            再也不显示了</view
                        >
                    </view>
                </view>
            </view>
        </view>
    </div>
</template>
<script>
export default {
    name: "Notice",
    data() {
        return {
            title: "标题",
            is_show: false,
            selectimg:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/new_red_select.png",
             
            versionUpdate3: false,
            content: "",
            systemMsgTitle: "",
        };
    }, 
    computed: {
    
    },
    mounted() {}, 
    //事件
    methods: {
        cancel() { 
            if (this.is_show) {
                let data = {
                    api: "app.Index.markToRead",
                    tell_id: uni.getStorageSync("laike_move_uaerInfo").tell_id,
                };
                this.$req.post({ data }).then((res) => {});
            }
            // uni.removeStorageSync('versionUpdate3')
            uni.setStorageSync("versionUpdate3", false);
            this.versionUpdate3 = false;
        },
        setSelected() {
            this.is_show = !this.is_show;
        },
        getData() {
            console.log(
                "父组件是否调用了我",
                uni.getStorageSync("versionUpdate3")
            ); 
            this.versionUpdate3 = uni.getStorageSync('versionUpdate3')
            let data2 = {
                api: "app.index.getUserTell",
            };
            this.$req.post({ data: data2 }).then((res2) => {
                console.log("141414141414", res2);
                if (res2.code == 200) {
 
                this.content = res2.data.systemMsg;
                this.systemMsgTitle = res2.data.systemMsgTitle;
                }
            });
        },
    },
};
</script> 
<style lang="less" scoped>
// 公告提示弹窗
.bill_mask {
    width: 100vw;
    height: 100vh;
    position: fixed;
    top: 0;
    // left: 0;
    transform: translateX(-50%) !important;
    left: 50% !important;
    // right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.6);
    z-index: 99999;
    display: flex;
    justify-content: center;
    align-items: center;
    .bill_conten {
        width: 92%;
        height: 1032rpx;
        background-color: #ffffff;
        border-radius: 24rpx;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        .bill_header {
            color: #333333;
            font-weight: bold;
            font-size: 32rpx;
            text-align: center;
            padding-top: 39rpx;
            padding-bottom: 34rpx;
        }
        .scroll {
            max-height: 480rpx;
            flex: 1;
        }

        .bill_body {
            padding: 0rpx 29rpx;
            margin-bottom: 15rpx;
            font-size: 32rpx;
            color: #999 !important;
            flex: 1;
            height: 480rpx;
            overflow-y: scroll;
            box-sizing: border-box;
            word-wrap: break-word;
            /deep/.rich-text {
                p {
                    margin-bottom: 64rpx !important;
                    span {
                        color: #999 !important;
                    }
                }
            }
        }
        .bill_footer {
            display: flex;
            justify-content: center;
            margin-bottom: 20rpx;
            height: 252rpx;
            // background-color: #D73B48;
            // border-top: 1rpx solid rgba(0, 0, 0, 0.1);

            .foobtn {
                // width: 300rpx;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                .text {
                    width: 590rpx;
                    height: 92rpx;
                    border-radius: 52rpx;
                    background-color: #ff6f6f;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    font-size: 32rpx;
                    font-weight: 500;
                    font-weight: 500;
                    color: #fff;
                    margin-bottom: 24rpx;
                }
                .no_show {
                    color: #666;
                    font-size: 28rpx;
                    display: flex;
                    align-items: center;
                    .lk-label-image {
                        width: 34rpx;
                        height: 34rpx;
                        margin-right: 16rpx;
                    }
                    .lk-label-divs {
                        width: 34rpx;
                        height: 34rpx;
                        border: 2rpx solid #b8b8b8;
                        border-radius: 50%;
                        margin-right: 16rpx;
                    }
                }
            }
        }
    }
}
</style>
