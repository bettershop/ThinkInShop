<template>
    <div :style="{background: bgColor2[0].item}">
        <view class="homeAdv"
              :style="{background: `linear-gradient(90deg,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`, marginTop: `${marginTop}rpx`}"
              :class="{onTopLeft: bgTopLeftStyle, onTopRight: bgTopRightStyle, onBottomLeft: bgBottomLeftStyle, onBottomRight: bgBottomRightStyle}">
            <image v-for="(item, index) of imgUrls"  :src="item.img == '' ? default_img : item.img" @error="errorImg(index)" :class="[imgConfig?'':'fillet']" @click="$emit('toUrl', item.info[1].value)"></image>
        </view>    
    </div>
    
</template>

<script>
    export default {
        name: "homeAdv",
        props: {
            dataConfig: {
                type: Object,
                default: () => {
                }
            }
        },
        watch: {
            dataConfig() {
                this.imgUrls = this.dataConfig.advConfig.list 
                this.bgColor = this.dataConfig.bgColor.color
                this.bgColor2 = this.dataConfig.bgColor2.color
                this.imgConfig = this.dataConfig.imgConfig.type
                this.marginTop =  this.dataConfig.mbConfig.val
                this.bgTopLeftStyle =  this.dataConfig.bgTopLeftConfig.type
                this.bgTopRightStyle =  this.dataConfig.bgTopRightConfig.type
                this.bgBottomLeftStyle =  this.dataConfig.bgBottomLeftConfig.type
                this.bgBottomRightStyle =  this.dataConfig.bgBottomRightConfig.type
            }  
        },
        data () {
            return {
                imgUrls: [], //广告数据
                bgColor: this.dataConfig.bgColor.color,
                bgColor2: this.dataConfig.bgColor2.color,
                imgConfig: this.dataConfig.imgConfig.type, //是否为圆角
                marginTop: this.dataConfig.mbConfig.val, //组件上边距
                bgTopLeftStyle: this.dataConfig.bgTopLeftConfig.type,
                bgTopRightStyle: this.dataConfig.bgTopRightConfig.type,
                bgBottomLeftStyle: this.dataConfig.bgBottomLeftConfig.type,
                bgBottomRightStyle: this.dataConfig.bgBottomRightConfig.type,
                default_img:"../../static/img/default-img.png",
            }
        },
        created() {
            this.imgUrls = this.dataConfig.advConfig.list
        },
        methods: {
            errorImg(index) {
               this.imgUrls[index].img="../../static/img/default-img.png"
            },
        }
    }
</script>

<style scoped lang="less">
    .homeAdv {
        padding: 24rpx 24rpx;
        display: flex;
        justify-content: space-between;
        border-radius: 30rpx;

        &.onTopLeft {
            border-top-left-radius: 0;
        }

        &.onTopRight {
            border-top-right-radius: 0;
        }

        &.onBottomLeft {
            border-bottom-left-radius: 0;
        }

        &.onBottomRight {
            border-bottom-right-radius: 0;
        }
        
        image {
            width: 332rpx;
            height: 160rpx;
            vertical-align: bottom;
            
            &.fillet {
                border-radius: 20rpx;
            }
        }
    }
</style>
