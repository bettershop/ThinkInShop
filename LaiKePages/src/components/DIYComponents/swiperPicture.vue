<template>
    <view class="picture" :class="{fillet: !imgConfig}"
    :style="'background: linear-gradient(90deg, '+ bgColor[0].item +' 50%, '+ bgColor[1].item +' 100%);margin-top:'+pxToRpxNum(marginTop) +'rpx;padding: 0 '+paddinglr+'px;'">
        <image :src="LaiKeTuiCommon.getFastImg(imgUrls[0].img, 750, imageH)" v-if="imageH" lazy-load="true" :style="{height: pxToRpxNum(imageH) + 'rpx'}" @click="$emit('toUrl', imgUrls[0].info[1].value)"></image>
    </view>
</template>

<script>
    export default {
        name: "swiperPicture",
        props: {
            dataConfig: {
                type: Object,
                default: () => {
                }
            }
        },
        watch: {
            dataConfig () {
                this.imgConfig =  this.dataConfig.imgConfig.type //是否为圆角
                this.marginTop =  this.dataConfig.mbConfig.val //组件上边距
                this.paddinglr =  this.dataConfig.lrConfig.val //轮播左右边距
                this.bgColor =  this.dataConfig.bgColor.color //轮播背景颜色
                this.imgUrls = this.dataConfig.swiperConfig.list
                this.imageH = this.dataConfig.htConfig.val
            }
        },
        data () {
            return {
                imgConfig: this.dataConfig.imgConfig.type, //是否为圆角
                marginTop: this.dataConfig.mbConfig.val, //组件上边距
                paddinglr: this.dataConfig.lrConfig.val, //轮播左右边距
                bgColor: this.dataConfig.bgColor.color, //轮播背景颜色
                imageH: this.dataConfig.htConfig.val,
                imgUrls: [],
                img: ''
            }
        },
        created() {
            this.imgUrls = this.dataConfig.swiperConfig.list
        },
        mounted() {
            let that = this;
            this.$nextTick(function() {
                uni.getImageInfo({
                    src: that.setDomain(that.imgUrls[0].img),
                    success: function(res) {
                        // that.$set(that, 'imageH', res.height);
                    },
                    fail: function(error) {
                        console.log(error, 'error')
                    }
                })
            })
        },
        methods: {
            //替换安全域名
            setDomain: function(url) {
                url = url ? url.toString() : '';
                //本地调试打开,生产请注销
                if (url.indexOf("https://") > -1) return url;
                else return url.replace('http://', 'https://');
            },
        }
    }
</script>

<style scoped lang="less">
    .fillet {
        image {
            border-radius: 20rpx;
        }

        img {
            border-radius: 20rpx;
        }
    }
    .picture {
        image {
            width: 100%;
            vertical-align: bottom;
        }
    }
</style>
