<template>
	<view class="group-wrapper" v-if="combinationList.length>0" :style="{marginTop: pxToRpxNum(mbConfig) + 'rpx', background: `linear-gradient(90deg,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`}">
        <view class="home_title">
            <view :style="{color: txtColor}">{{title}}</view>
            <image :src="LaiKeTuiCommon.getFastImg(titleBg, 222, 38)" lazy-load="true"></image>
        </view>

        <scroll-view scroll-x="true" class="group_content">
            <view class="seckill_content_view" style="padding: 0 30rpx;">
                <view
                    class="group_content_item"
                    v-for="(item, index) of combinationList"
                    :key="index"
                    :style="{marginRight: lrConfig + 'rpx'}"
                    @tap="$emit('toUrl', '/pagesA/group/group')"
                >
                    <image :src="LaiKeTuiCommon.getFastImg(item.imgurl, 200, 200)" lazy-load="true" mode=""></image>
                    <view>
                        <view class="group_content_item_title">{{ item.pro_name }}</view>
                        <view class="group_content_item_disc">
                            <image :src="LaiKeTuiCommon.getFastImg(group_icon, 30, 40)" lazy-load="true" mode=""></image>
                            {{ item.sum }}{{language.home.group_people}}
                        </view>
                        <view class="group_content_item_price" :style="{color: priceTxtColor}">
                            <text>{{ item.groupnum }}{{language.home.Spell_people}}</text>
                            {{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(item.kaiprice) }}
                        </view>
                        <text class="group_content_item_yprice">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(item.market_price) }}</text>
                    </view>
                </view>
                <view style="min-width: 6rpx"></view>
            </view>
        </scroll-view>
	</view>
</template>

<script>
	export default {
		name: 'combination',
		props: {
			dataConfig: {
				type: Object,
				default: () => {}
			}
		},
        watch: {
            dataConfig () {
                this.numConfig = this.dataConfig.numConfig.val;
                this.txtColor = this.dataConfig.titleTxtColor.color[0].item
                this.priceTxtColor = this.dataConfig.priceTxtColor.color[0].item
                this.title = this.dataConfig.titleConfig.value
                this.titleBg = this.dataConfig.imgConfig.url
                this.mbConfig = this.dataConfig.mbConfig.val
                this.lrConfig = this.dataConfig.lrConfig.val
                this.home_title_bg = this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/home_title_bg.png'
                this.group_icon = this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/group_icon.png'
                this.combinationList = this.dataConfig.list.splice(0, this.numConfig)
                this.bgColor = this.dataConfig.bgColor.color
            }
        },
		data() {
			return {
				combinationList: this.dataConfig.list,
				numConfig:this.dataConfig.numConfig.val,
				txtColor:this.dataConfig.titleTxtColor.color[0].item,
				priceTxtColor:this.dataConfig.priceTxtColor.color[0].item,
				title:this.dataConfig.titleConfig.value,
				titleBg:this.dataConfig.imgConfig.url,
				mbConfig:this.dataConfig.mbConfig.val,
				lrConfig:this.dataConfig.lrConfig.val,
                home_title_bg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/home_title_bg.png',
                group_icon: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/group_icon.png',
                bgColor: [
                    {
                        item: '#FFFFFF'
                    },
                    {
                        item: '#FFFFFF'
                    }
                ],
			};
		},
		created() { 
            this.combinationList = this.dataConfig.list.splice(0, this.numConfig)
            this.bgColor = this.dataConfig.bgColor.color
        }
	}
</script>

<style lang="less">
    .group-wrapper {
        padding-top: 10rpx;
        padding-bottom: 10rpx;
    }

    .home_title {
        position: relative;
        z-index: 10;
        margin: 0rpx 30rpx 0;
    }

    .home_title>view {
        font-size: 34rpx;
        line-height: 34rpx;
        color: #014343;
        font-weight: bold;
    }

    .home_title>image {
        position: absolute;
        width: 111rpx;
        height: 19rpx;
        top: 20rpx;
        left: 24rpx;
        z-index: -1;
    }

    .group_content {
        margin-top: 30rpx;

        .seckill_content_view {
            display: flex;
        }

        .group_content_item {
            display: flex;
            width: 560rpx;
            min-width: 560rpx;
            height: 260rpx;
            background: rgba(255, 255, 255, 1);
            box-shadow: 0rpx 2rpx 32rpx 0rpx rgba(0, 0, 0, 0.1);
            border-radius: 16rpx;
            padding: 30rpx 24rpx;
            box-sizing: border-box;
            margin-right: 24rpx;

            >image {
                width: 200rpx;
                height: 200rpx;
                border-radius: 10rpx;
                margin-right: 24rpx;
            }

            >view {
                flex: 1;
                display: flex;
                flex-direction: column;
                align-items: flex-start;
            }

            .group_content_item_title {
                font-size: 28rpx;
                color: #020202;
                line-height: 31rpx;
            }

            .group_content_item_disc {
                display: flex;
                align-items: center;
                height: 28rpx;
                padding: 0 14rpx 0 10rpx;
                background: rgba(255, 239, 238, 1);
                border-radius: 14rpx;
                font-size: 22rpx;
                line-height: 22rpx;
                color: #FF5041;
                margin-top: 16rpx;

                image {
                    width: 15rpx;
                    height: 20rpx;
                    margin-right: 4rpx;
                }
            }

            .group_content_item_price {
                font-size: 30rpx;
                line-height: 25rpx;
                font-weight: bold;
                margin-top: auto;

                text {
                    color: #020202;
                    font-size: 24rpx;
                    line-height: 24rpx;
                }
            }

            .group_content_item_yprice {
                font-size: 24rpx;
                line-height: 20rpx;
                color: #999999;
                text-decoration: line-through;
                margin-top: 14rpx;
            }
        }
    }
</style>
