<template>
    <view class="liveItem">
        <view class="box" v-for="item in liveList" :key="item.roomid">
            <view class="imgBox" @tap="toRoom(item)">
                <img :src="item.cover_img" alt="" />
                <view class="tips">
                    <view v-if="item.live_status == 101" class="red">
                        <image :src="live" />
                        <text>{{language.liveItem.live}}</text>
                    </view>
                    <view v-else-if="item.live_status == 102" class="black">
                        <image :src="noStart" />
                        <text>{{language.liveItem.notBegun}}</text>
                    </view>
                    <view v-else-if="item.live_status == 103" class="blue">
                        <image :src="replay" />
                        <text>{{language.liveItem.playback}}</text>
                    </view>
                </view> 
            </view>
            <view class="under">
                <view class="authorImg">
                    <image :src="item.feeds_img"></image>
                </view>
                <view class="topTitle">{{ item.name }}</view>
                <view class="bottomTitle">
                    <text>{{ item.anchor_name }}</text>
                </view>
            </view>
        </view>
    </view>
</template>

<script>
import { mapMutations } from 'vuex';

export default {
    data() {
        return {
            live: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/live.png',
            noStart: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/noStart.png',
            replay: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/replay.png'
        };
    },
    props: ['liveList', 'temp'],
    created() {
        this.setLang();
    },
    methods: {
        toRoom(item) {
            if (item.live_status == 102) {
                uni.showToast({
                    title: this.language.liveItem.liveNotStarted,
                    icon: 'none'
                });
                return;
            }
            uni.navigateTo({
                url: `plugin-private://wx2b03c6e691cd7370/pages/live-player-plugin?room_id=${item.roomid}`
            });
        }, 
    }
};
</script>
<style lang="less" scoped> @import url("@/laike.less");
    
.liveItem {
        width: 95%;
        margin:36rpx auto;
        display: grid;
        grid-column-gap: 20rpx;
        grid-template-columns: 1fr 1fr;
        font-size: 24rpx;
    }

    .box {
        // border-radius: 20rpx 20rpx 0 0;
        // box-shadow: 0px 0px 15px 2px #eee;
        background: #FFFFFF;
        border-radius: 24rpx;
        margin-bottom: 16rpx;

        .imgBox {
            img {
                width: 100%;
                height: 436rpx;
                border-radius: 16rpx 16rpx 0 0;
            }

            position: relative;

            .tips {
                position: absolute;
                top: 16rpx;
                left: 16rpx;
                font-size: 24rpx;
                color: #FFFFFF;
                // width: 124rpx;
                height: 36rpx;
                line-height: 36rpx;
                // background: #000000;
                border-radius: 18rpx;
                box-sizing: border-box;
                text{
                    margin: 0 12rpx 0 4rpx;
                }
                view {
                    // padding: 8rpx 10rpx;
                    border-radius: 18rpx;
                    display: flex;
                    align-items: center;

                    image {
                        width: 36rpx;
                        height: 36rpx;
                        // margin-right: 18rpx;
                        vertical-align: middle;
                    }
                }
            }

            // .imgfooter {
            //     width: 94%;
            //     position: absolute;
            //     bottom: 0;
            //     font-size: 24rpx;
            //     padding: 10rpx;
            //     color: #fff;
            //     background-image: linear-gradient(to top, #333, transparent);
            // }
        }

        .under {
            // margin: 10rpx;
            padding: 40rpx 24rpx 24rpx 24rpx;
            position: relative;
            .authorImg{
                position: absolute;
                top: -24rpx;
                left: 24rpx;
                width: 48rpx;
                height: 48rpx;
                // background: #EDF1F5;
                // border: 2rpx solid #FFFFFF;
                image{
                    width: 100%;
                    height: 100%;
                    border-radius: 50%;
                }
            }
            .topTitle{
                font-size: 28rpx;
                
                font-weight: 600;
                color: #333333;
                margin:0rpx 0 8rpx;
            }
            .bottomTitle{
                font-size: 24rpx;
                
                font-weight: 400;
                color: #666666;
            }
        }
    }

    .red {
        // background-image: linear-gradient(to right, #ff7272, #ff4444);
        background-color: rgba(0,0,0,.4);
    }

    .blue {
        // background-image: linear-gradient(to right, #50b6f4, #4b9af3);
        background-color: rgba(0,0,0,.4);
    }

    .black {
        // background-image: linear-gradient(to right, #ffa03a, #ffbd42);
        background-color: rgba(0,0,0,.4);
    }
</style>
