<template>
    <view class="homeVideo" :style="{marginTop: mbConfig + 'rpx'}">
        <video id="myVideo" :src="videoSrc"
               @error="videoErrorCallback" object-fit="cover" :poster="posterSrc" :title="title" controls></video>
    </view>
</template>

<script>
    export default {
        name: "homeVideo",
        props: {
            dataConfig: {
                type: Object,
                default: () => {}
            },
            opensetting: {
                type: Boolean
            }
        },
        data () {
            return {
                mbConfig:this.dataConfig.mbConfig.val,
                videoSrc: '',
                posterSrc: '',
                title: ''
                
            }
        },
        watch: {
            dataConfig () {
                this.videoSrc = this.dataConfig.swiperConfig.list[0].img
                this.title = this.dataConfig.swiperConfig.list[0].info.value
                this.posterSrc = this.dataConfig.posterConfig.list[0].img
                this.mbConfig = this.dataConfig.mbConfig.val
            }
        },
        created() {
            this.videoSrc = this.dataConfig.swiperConfig.list[0].img
            this.title = this.dataConfig.swiperConfig.list[0].info.value
            this.posterSrc = this.dataConfig.posterConfig.list[0].img
        },
        methods: {
            videoErrorCallback (e) {
                console.error('视频错误', e)
            }
        }
    }
</script>

<style scoped lang="less">
    .homeVideo {
        height: 450rpx;
        width: 100vw;
        #myVideo {
            width: 100vw;
            height: 450rpx;
        }
        video {
            width: 100vw;
            height: 450rpx;
        }
    }
</style>
