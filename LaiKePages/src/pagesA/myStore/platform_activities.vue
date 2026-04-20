<template>
    <view class="container">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="language.platform.title"></heads>

        <toload :load="load">
            <view class="activity" v-for="(item, index) in list" :key="index" >
                <image class="image" :src="item.image"></image>
                <view class="activity_right">
                    <view class="activity_title">
                        <view class="icon" v-if="item.type == 'pt'">{{language.platform.group}}</view>
                        <view class="icon1" v-else-if="item.type == 'ms'">{{language.platform.seckill}}</view>
                        <view class="view" :style="{textIndent: languageType=='en_US'?'90rpx':''}">{{ item.name }}</view>
                    </view>
                    <view class="activity_time">{{language.platform.Activity}}：{{ item.starttime }}{{language.platform.to}}{{ item.endtime }}</view>
                    <view class="activity_btnBox" @tap.stop>
                        <text class="status1" v-if="item.status == 1">{{language.platform.type[0]}}</text>
                        <text class="status2" v-if="item.status == 2">{{language.platform.type[1]}}</text>
                        <text class="status3" v-if="item.status == 3">{{language.platform.type[2]}}</text>

                        <view class="btn" v-if="item.status != 1 " @tap="toOrder(item)">
                            {{language.platform.myorder}}
                        </view>
                        
                        <view class="btn" v-if="item.status == 1 && item.status1 == 0" @tap="toDetail(item)">
                            {{language.platform.button[0]}}
                        </view>
                        <view class="btn" v-if="item.status == 2 || item.status1 == 1" @tap="toGoods(item)">
                            {{language.platform.button[1]}}
                        </view>
                        <view class="btn" v-if="item.status == 3 && item.status1 == 0">{{language.platform.button[2]}}</view>
                    </view>
                </view>
            </view>
            <view v-if="list.length == 0">
                <image class="no_active" :src="no_active"></image>
                <view style="font-size: 32rpx;text-align: center;color: #666;line-height: 32rpx;margin-top: 16rpx;">{{language.platform.Tips[0]}}~</view>
            </view>
            <loadmore v-if="list.length > 9" :loadingType="loadingType"></loadmore>
        </toload>
    </view>
</template>

<script>
import loadmore from '@/components/uni-load-more.vue';

export default {
    data() {
        return {
            title: '活动管理',
            
            page: 1,
            list: [],
            scrollFlag: true,
            load: false,
            loadingType: 0,

            no_active: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/no_active.png'
        };
    },
    components: { loadmore },
    onShow() {
        this.isLogin(()=>{
			
			this.list = [];
			this.scrollFlag = true;
			this.page = 1;
			this.axios();
		})
    },
    onReachBottom() {
        this.loadingType = 1;
        this.page++;
        this.axios();
    },
    methods: {
        toOrder (item) {
            this.navito('/pagesA/myStore/myOrder?platform_activities_id=' + item.id)
        },
        toDetail (item) {
            this.navito('/pagesA/myStore/activities_details?id=' + item.id + '&type=' + item.type)  
        },
        toGoods (item) {
            uni.navigateTo({
                url: '/pagesA/myStore/activities_pro?id=' + item.id + '&type=' + item.type
            })
        },
        changeLoginStatus(){
			this.list = [];
			this.scrollFlag = true;
			this.page = 1;
			this.axios();
		},
        axios() {
            let data = {
                api: 'app.platform_activities.index',
                page: this.page,
                mch_id: this.$store.state.shop_id ? this.$store.state.shop_id : uni.getStorageSync('shop_id')
            };
            this.$req.post({ data }).then(res => {
                let { code, data, message } = res;
                this.load = true;
                if (code == 200) {
                    data.filter(item => {
                        item.starttime = item.starttime.substr(0, 10);
                        item.endtime = item.endtime.substr(0, 10);
                    });

                    if (!data.length) {
                        this.loadingType = 2;
                    }

                    if (data.length > 10) {
                        this.loadingType = 0;
                    }

                    this.list.push(...data);
                } else {
                    uni.showToast({
                        title: message,
                        icon: 'none'
                    });
                }
            });
        },
        navito(url) {
            uni.navigateTo({
                url
            });
        }
    }
};
</script>

<style scoped lang="less">
@import url("@/laike.less");
.container {
    background-color: #f7f4f8;
    min-height: 100vh;

    .activity {
        display: flex;
        width: 100vw;
        height: 261rpx;
        background: rgba(255, 255, 255, 1);
        padding: 30rpx;
        box-sizing: border-box;

        .image {
            width: 220rpx;
            height: 200rpx;
            margin-right: 25rpx;
        }

        &_right {
            flex: 1;
            overflow: hidden;
            display: flex;
            flex-direction: column;
        }

        &_title {
            position: relative;
            margin-bottom: 20rpx;
            display: flex;
            .view {
                text-overflow: -o-ellipsis-lastline;
                overflow: hidden;
                text-overflow: ellipsis;
                display: -webkit-box;
                -webkit-line-clamp: 2;
                -webkit-box-orient: vertical;
                word-wrap: break-word;
                // text-indent: 70rpx;
                font-size: 28rpx;
                margin-left: 10rpx;
            }

            .icon,
            .icon1 {
                
                display: flex;
                align-items: center;
                justify-content: center;
                color: #fefefe;
                font-size: 22rpx;
                padding: 0rpx 12rpx;
                white-space: nowrap;
                border-radius: 20rpx;
                margin-right: 10rpx;
                
            }

            .icon {
                background: #ff3333;
            }

            .icon1 {
                background: #f1a100;
            }
        }

        &_time {
            color: #999999;
            font-size: 24rpx;
            line-height: 24rpx;
        }

        &_btnBox {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-top: auto;

            .status1 {
                font-size: 28rpx;
                color: @priceColor;
            }

            .status2 {
                font-size: 28rpx;
                color: @priceColor;
            }

            .status3 {
                font-size: 28rpx;
                color: #666666;
            }

            .btn {
                .outBtn();
                display: flex;
                align-items: center;
                justify-content: center;
                width: 140rpx;
                height: 50rpx;
                border-radius: 6rpx;
                box-sizing: border-box;
                font-size: 24rpx;
            }
        }
    }

    .no_active {
        display: block;
        margin: 200rpx auto 0;
        width: 240rpx;
        height: 272rpx;
    }
}
</style>
