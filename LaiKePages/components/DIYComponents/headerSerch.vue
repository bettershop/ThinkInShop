<template>
    <div>
        <div class="container_top" :style="{height: height, background: `linear-gradient(90deg,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`, paddingBottom: 0}">
            <!-- #ifndef MP -->
            <view class="data_h"  v-if="isTop" :style="{ height: halfWidth , background: `linear-gradient(90deg,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`}"><view class="data_h_h" :style="{ height: halfWidth, background: `linear-gradient(90deg,${bgColor[0].item} 0%,${bgColor[1].item} 100%)` }"></view></view>
            <!-- #endif -->
            <!-- #ifdef MP -->
            <heads :title="language.home.title" v-if="isTop" :border="true" :returnFlag="true" :bgColor="bgColor" ishead_w="1"></heads>
            <!-- #endif -->
            <div class="header-wrap" :style="{position: isTop ? 'fixed' :'relative', zIndex: isTop ? 999 : 1 }">
                <view class="header" :style="'background: linear-gradient(90deg, '+ bgColor[0].item +' 0%, '+ bgColor[1].item +' 100%);margin-top:'+ pxToRpxNum(mbConfig) +'rpx;'">
                    <view class="serch-wrapper acea-row row-middle">
                        <view class="logo" v-if="tabConfig == 1">
                            <image :src="LaiKeTuiCommon.getFastImg(logoConfig, 136, 64)" lazy-load="true" mode=""></image>
                        </view>
                        <view class="address" v-if="tabConfig == 2" @click="$emit('toUrl', '/pagesB/home/chooseArea')">
                            <span>{{region && region.district ? region.district : '岳麓区'}}</span>
                            <image :src="jianX_h" lazy-load="true"></image>
                        </view>
                        <navigator url="/pagesA/search/search" class="input acea-row row-middle container_top_item_center" :class="[boxStyle?'':'fillet',tabConfig?'':'on',txtStyle?'row-center':'', msgStyle ? 'w85' : 'w100']" :style="{width: inputWidth}" hover-class="none">
                            <image :src="search2x" lazy-load="true"></image>
                            {{language.home.search_placeholder}}</navigator>

                        <div v-if="msgStyle" @click="$emit('toUrl', '/pagesB/message/systemMesage', true)" class="container_top_item_right" >
                            <image :src="xiaoxi2x" lazy-load="true"></image>
                            <view v-if="xxnum">{{ xxnum }}</view>

                        </div>
                    </view>
                </view>
            </div>

        </div>
    </div>
    
    
</template>

<script>
	let statusBarHeight = uni.getSystemInfoSync().statusBarHeight + 'px';
	export default {
		name: 'headerSerch',
		props: {
			dataConfig: {
				type: Object,
				default: () => {}
			},
            region: {
                type: Object,
                default: () => {}
            }
		},
        watch: {
            region (val) {
                console.log(val)
                // debugger
            },
            dataConfig () {
                this.xxnum = this.dataConfig.xxnum > 99 ? '99+' : Number(this.dataConfig.xxnum); //限制消息数超过99时显示99+
                this.bgColor = this.dataConfig.bgColor.color
                this.boxStyle =  this.dataConfig.boxStyle.type
                this.logoConfig =  this.dataConfig.logoConfig.url
                this.mbConfig =  this.dataConfig.mbConfig.val
                this.tabConfig =  this.dataConfig.tabConfig.tabVal
                this.txtStyle =  this.dataConfig.txtStyle.type
                this.isTop =  this.dataConfig.isTop
                this.msgStyle =  this.dataConfig.msgStyle.val
            }
        },
		data() {
			return {
                jianX_h: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jianX_h.png', 
                xiaoxi2x: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/auction/grxx.png',
                search2x: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/home/syss.png',
				statusBarHeight: statusBarHeight,
				bgColor: [
                    {
                        item: ''
                    },
                    {
                        item: ''
                    }
                ],
				boxStyle: this.dataConfig.boxStyle.type,
				logoConfig: this.dataConfig.logoConfig.url,
				mbConfig: this.dataConfig.mbConfig.val,
				tabConfig: this.dataConfig.tabConfig.tabVal,
				txtStyle: this.dataConfig.txtStyle.type,
                xxnum: '',
                isTop: this.dataConfig.isTop,
                msgStyle: this.dataConfig.msgStyle.val
			};
		},
        
        mounted() {
            this.xxnum = this.dataConfig.xxnum > 99 ? '99+' : Number(this.dataConfig.xxnum); //限制消息数超过99时显示99+
            this.bgColor = this.dataConfig.bgColor.color
            this.setLang()
        },
        computed: {
            inputWidth () {
                if (!this.tabConfig) {
                    return 'width: 85%'
                    if (this.msgStyle) {
                        
                    } else {
                        
                    }
                } else {
                    if (this.msgStyle) {
                        // return '100%'    
                    } else {
                        // 地址选择
                        if (this.tabConfig == 2) {
                            return  '76%'
                        }
                        return '78%'
                    }
                    
                }
            },
            halfWidth: function() {
                var gru = uni.getStorageSync('data_height') ? uni.getStorageSync('data_height') : this.$store.state.data_height;
                var heigh = parseInt(gru);

                // #ifdef MP
                heigh = 0;
                // #endif

                var he = heigh * 2;
                console.log(uni.upx2px(he) + 'px')
                return uni.upx2px(he) + 'px';
            },
            height () {
                var gru = uni.getStorageSync('data_height') ? uni.getStorageSync('data_height') : this.$store.state.data_height;
                var heigh = parseInt(gru); 
                var he = heigh * 2;
                he = he + 88
                // #ifdef MP
                if (this.isTop) {
                    he = he + 88
                }
                // #endif
                
                if (!this.isTop) {
                    // #ifdef MP
                        he = he - 88
                    // #endif
                    return  uni.upx2px(he) + 'px';
                }
                console.log(uni.upx2px(he) + 'px')
                return uni.upx2px(he) + 'px';
            }
        },
		methods: {
			
		},
        created(){
            this.setLang();
        }
	}
</script>

<style lang="less">
@import url("@/laike.less");
    .container_top {
        background: rgba(1, 67, 67, 1);

        .data_h_h{
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            z-index: 999;
        }
    } 
    .header-wrap {
        position: relative;
        height: 88rpx;
        width: 100%;
    }
    
    .header {
        width: 100%;
        height: 88rpx;
        border-bottom: none;
        .serch-wrapper {
            padding: 10rpx 25rpx 0 25rpx;
            .logo {
                width: 136rpx;
                height: 64rpx;
                margin-right: 10rpx;
                image {
                    width: 100%;
                    height: 100%;
                }
            }

            .address {
                width: 142rpx;
                overflow: hidden;
                font-size: 14px;
                color: #FFFFFF;
                margin-right: 10px;

                image {
                    width: 25rpx;
                    height: 14rpx;
                    margin-left: 10rpx;
                }
            }

            .container_top_item_right {
                display: flex;
                align-items: center;
                justify-content: center;
                position: relative;
                margin-left: 20rpx;
                width: 68rpx;
                height: 68rpx;
                background: rgba(255, 255, 255, 1);
                border-radius: 34rpx;
            }

            .container_top_item_right image {
                width: 50rpx;
                height: 50rpx;
            }

            .container_top_item_right view {
                position: absolute;
                right: -7rpx;
                top: 6rpx;
                padding: 4rpx 6rpx;
                background: rgba(255, 126, 0, 1);
                border-radius: 12rpx;
                font-size: 22rpx;
                line-height: 16rpx;
                color: #FFFFFF;
            }

            .input {
                
                box-sizing: border-box;
                width: 452rpx;
                height: 68rpx;
                padding: 0 0 0 20rpx;
                border: 1px solid #FFF;
                font-size: 28rpx;
                .searchColor();
                color:rgba(0,0,0,0.502);
                image {
                    width: 32rpx;
                    height: 32rpx;
                    margin-right: 13rpx;
                }
    
                // 没有logo，直接搜索框
                &.on{
                    width: 85%;
                }
                &.w85 {
                    /*width: 85%;*/
                }
                &.w100 {
                    width: 100%;
                }
                // 设置圆角
                &.fillet{
                    border-radius: 44rpx;
                }
                // 文本框文字居中
                &.row-center{
                    padding: 0;
                }
            }
        }
    }
</style>
