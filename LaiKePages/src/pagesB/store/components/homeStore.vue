<template>
    <view class="mch" v-if="r_mch.length" :style="{background: `linear-gradient(90deg,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`, marginTop: mbConfig + 'rpx',}">
        <view class="home_title">
            <view :style="{color: txtColor}">{{title}}</view>
            <image :src="titleBg"></image>
            <view class="more" @tap="toUrl('/pagesB/home/mchList')">
                {{language.home.more}}
                <image :src="jiantou" lazy-load="true"></image>
            </view>
        </view>

        <scroll-view scroll-x="true" class="mch_content" v-if="!opensetting">
            <div style="display:flex;">
                <view class="mch_content_item" v-for="(item, index) of r_mch" :key="index" @tap="$emit('toUrl', '/pagesB/home/mchList')" :style="{marginRight: lrConfig + 'rpx'}">
                    <image :src="item.logo == ''? default_img : item.logo " @error="handleNavLisImg(index)" lazy-load="true" mode=""></image>
                    <view>{{ item.name }}</view>
                    <!-- <text>{{item.distance}}km</text> -->
                </view>
            </div>
        </scroll-view>
        <view v-else>
            <!-- #ifdef MP -->
            <button class="openSetting" type="primary" open-type="openSetting" @tap="$emit('openSetting')">{{language.home.access_to_locate}}</button>
            <!-- #endif -->
            <!-- #ifndef MP -->
            <view class="locationDisc">{{language.home.please_open_locate}}</view>
            <!-- #endif -->
        </view>
    </view>
</template>

<script>
    export default {
        name: "homeStore",
        props: {
            dataConfig: {
                type: Object,
                default: () => {}
            },
            opensetting: {
                type: Boolean
            },
        },
        watch: {
            dataConfig () {
                
                this.bgColor = this.dataConfig.bgColor.color
                this.numberConfig = this.dataConfig.numberConfig.val
                this.title = this.dataConfig.titleConfig.value
                this.txtColor = this.dataConfig.titleTxtColor.color[0].item
                this.titleBg = this.dataConfig.imgConfig.url
                this.lrConfig =  this.dataConfig.lrConfig.val
                this.mbConfig = this.dataConfig.mbConfig.val
            }
        },
        data () {
            return {
                home_title_bg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/home_title_bg.png',
                r_mch: [],
                numberConfig:this.dataConfig.numberConfig.val,
                bgColor: [
                    {
                        item: '#FFFFFF'
                    },
                    {
                        item: '#FFFFFF'
                    }
                ],
                title:this.dataConfig.titleConfig.value,
                txtColor:this.dataConfig.titleTxtColor.color[0].item,
                titleBg:this.dataConfig.imgConfig.url,
                lrConfig: this.dataConfig.lrConfig.val,
                mbConfig:this.dataConfig.mbConfig.val,
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jiantou2x.png',
                default_img:"../../static/img/default-img.png",
            }
        },
        created() {
            this.setLang();
        },
        mounted() {
            
            this.bgColor = this.dataConfig.bgColor.color
            this.axios()
        },
        
        methods: {
            handleNavLisImg(index){
               this.r_mch[index].logo="../../static/img/default-img.png"
            },
            axios(){
                let data = {
                    api: 'app.index.recommend_stores',
                    longitude: uni.getStorageSync('longitude'), // 经度
                    latitude:uni.getStorageSync('latitude'), // 纬度
                    pageNo: 1,
                    pageSize: this.numberConfig
                }
                
                this.$req.post({data}).then(res=>{
                    if(res.code == 200){
                        this.r_mch = res.data.list.slice(0,this.numberConfig)
                    }else{
                        uni.showToast({
                            title: res.message,
                            icon: 'none'
                        })
                    }
                })
            },
            toUrl(url) {
                if (url.includes('tabBar')) {
                    uni.showTabBar({
                        url
                    });
                } else {
                    uni.navigateTo({
                        url
                    });
                }
            }
        }
    }
</script>

<style scoped lang="less">
    .mch {
        padding-top: 10rpx;
        padding-bottom: 10rpx;
        width: 100% !important;
        padding: 5px 0 !important;
        border-radius: 0 !important;
    }
    .home_title {
        position: relative;
        z-index: 10;
        margin: 0rpx 30rpx 0;
        display: flex;
        justify-content: space-between;
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
    
    .more {
        display: flex;
        align-items: center;
        color: #515a6e !important;
        font-size: 26rpx !important;
        margin-left: auto;
        height: 36rpx;
    }
    
    .more image {
        width: 12rpx;
        height: 22rpx;
        margin-left: 12rpx;
    }
    
    .mch_content{
        /*display: flex;*/
        margin: 30rpx;
        width: 688rpx;
    }

    .mch_content_item{
        display: flex;
        flex-direction: column;
        width:216rpx;
        height:316rpx;
        background:rgba(255,255,255,1);
        border-radius:10rpx;
        margin-right: 20rpx;
    }

    .mch_content_item>image{
        width:216rpx;
        height:216rpx;
        border-radius:10rpx 10rpx 0rpx 0rpx;
    }

    .mch_content_item>view{
        font-size: 24rpx;
        line-height: 24rpx;
        color: #000000;
        margin: 20rpx 14rpx 18rpx;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }
    .mch_content_item>text{
        color: #999999;
        font-size: 24rpx;
        line-height: 20rpx;
        margin: 0 14rpx;
    }
</style>
