<template>
    <view class="reduction">
        <view class="home_title">
            <view :style="{color: txtColor}">{{title}}</view>
            <image :src="LaiKeTuiCommon.getFastImg(titleBg, 222, 38)" lazy-load="true"></image>
        </view>
        <view class="swiper" :class="[imgConfig?'':'fillet']" :style="'padding: 0 '+ paddinglr +'rpx;'"
            v-if="imgUrls.length">

            <swiper class="swiper-content" :class="[imgConfig?'':'fillet']" :style="'height:'+ pxToRpxNum(imageH) +'rpx;'"
                :autoplay="true" interval="3000" circular="true" @change="changeBanner">
                <block v-for="(item,index) in imgUrls" :key="index">
                    <swiper-item>
                        <view @click="$emit('toUrl', '/pagesB/home/substration?title='+item.title+'&id='+item.id)"
                            class='slide-navigator acea-row row-between-wrapper'>
                            <image :style="{height: pxToRpxNum(imageH) +'rpx'}" :src="LaiKeTuiCommon.getFastImg(item.image, 750, imageH)" lazy-load="true" class="slide-image"></image>
                        </view>
                    </swiper-item>
                </block>
            </swiper>

            <view class="swiper_dots" :style="{backgroundColor: docBackgroundColor}">
                <view :class="{ active: dotIndex == index }" v-for="(item, index) in imgUrls" :key="index"
                    :style="{backgroundColor: dotIndex == index ? docSelectColor : docColor}"></view>
            </view>
        </view>
    </view>
</template>

<script>
    export default {
        name: 'reduction',
        props: {
        	dataConfig: {
        		type: Object,
        		default: () => {}
        	}
        },
        data() {
            return {
                titleBg: 'https://xiaochengxu.houjiemeishi.com/V3/images/icon1/home_title_bg.png',
                txtColor: '#232323',
                title: '满减',
                indicatorDots: false,
                circular: true,
                autoplay: true,
                interval: 3000,
                duration: 500,
                imgUrls: [], //图片轮播数据
                bgColor: this.dataConfig.bgColor.color, //轮播前景颜色
                bgColor2: this.dataConfig.bgColor2.color, //轮播背景颜色
                marginTop: this.dataConfig.mbConfig.val, //组件上边距
                paddinglr: this.dataConfig.lrConfig.val, //轮播左右边距
                docConfig: this.dataConfig.docConfig.type, //指示点样式
                imgConfig: this.dataConfig.imgConfig.type, //是否为圆角
                docSelectColor: '',
                imageH: this.dataConfig.heightConfig.val, //轮播图高度
                docColor: '',
                docBackgroundColor: '', 
                swiperType: this.dataConfig.tabConfig.tabVal,
                itemEdge:this.dataConfig.itemEdge.val,
                dotIndex: 0, //轮播图下标
            }
        },
        watch: {
            dataConfig () {
                this.bgColor = this.dataConfig.bgColor.color //轮播前景颜色
                this.bgColor2 = this.dataConfig.bgColor2.color //轮播背景颜色
                this.marginTop = this.dataConfig.mbConfig.val //组件上边距
                this.paddinglr = this.dataConfig.lrConfig.val //轮播左右边距
                this.docConfig = this.dataConfig.docConfig.type //指示点样式
                this.imgConfig = this.dataConfig.imgConfig.type //是否为圆角
                this.imageH = this.dataConfig.heightConfig.val //轮播图高度
                this.swiperType = this.dataConfig.tabConfig.tabVal
                this.itemEdge = this.dataConfig.itemEdge.val
            
                this.imgUrls = this.dataConfig.list
                this.docSelectColor = this.dataConfig.docSelectColorConfig.color[0].item
                this.docColor = this.dataConfig.docColorConfig.color[0].item
                this.docBackgroundColor = this.dataConfig.docBackgroundColorConfig.color[0].item
            }
        },
        created() {
            this.imgUrls = this.dataConfig.list
            this.docSelectColor = this.dataConfig.docSelectColorConfig.color[0].item
            this.docColor = this.dataConfig.docColorConfig.color[0].item
            this.docBackgroundColor = this.dataConfig.docBackgroundColorConfig.color[0].item
        },
        methods: {
            /*
                轮播图切换
            */
            changeBanner(e) {
                this.dotIndex = e.detail.current;
            }
        }
    }
</script>

<style lang="less" scoped> @import url("@/laike.less");
    .reduction {
        padding-top: 10rpx;
        padding-bottom: 10rpx;

        .home_title {
            position: relative;
            z-index: 10;
            margin: 0rpx 30rpx 30rpx;
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
    }

    .swiper {
        z-index: 20;
        position: relative;
        overflow: hidden;


        &.fillet {
            border-radius: 20rpx;
        }

        /* 设置圆角 */
        .swiper-content {
            overflow: hidden;

            &.fillet {
                border-radius: 16rpx;
                transform: translateY(0);
            }
        }


        swiper,
        .swiper-item,
        image {
            width: 100%;
            display: block;
        }

        // 圆形指示点
        &.circular {
            .uni-swiper-dot {
                width: 10rpx !important;
                height: 10rpx !important;
                background: rgba(0, 0, 0, .4) !important
            }

            .uni-swiper-dot-active {
                background: #fff !important
            }
        }

        // 方形指示点
        &.square {
            .uni-swiper-dot {
                width: 20rpx !important;
                height: 5rpx !important;
                border-radius: 3rpx;
                background: rgba(0, 0, 0, .4) !important
            }

            .uni-swiper-dot-active {
                background: #fff !important
            }
        }
    }

    .item-img image {
        display: block;
        width: 100%;
    }

    .swiper_dots {
        position: absolute;
        display: flex;
        align-items: center;
        bottom: 12rpx;
        left: 50%;
        transform: translateX(-50%);
        height: 24rpx;
        border-radius: 12rpx;
        padding: 0 12rpx;
    }

    .swiper_dots>view {
        width: 14rpx;
        height: 14rpx;
        border-radius: 50%;
        /*background-color: #024444;*/
    }

    .swiper_dots>.active {
        /*background-color: #FF7F00;*/
    }

    .swiper_dots>view:not(:last-child) {
        margin-right: 14rpx;
    }
</style>
