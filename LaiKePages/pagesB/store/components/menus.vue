<template>
    <view class='nav acea-row acea-row' :style="{marginTop:mbConfig+'rpx', background: `linear-gradient(90deg,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`}" v-if="menus.length">
        <lktauthorize ref="lktAuthorizeComp"></lktauthorize>
        <block v-for="(item,index) in menus" :key="index">
            <view class='item' :style="'width:'+ rowStyle +';color:'+titleColor" :url='item.info[1].value'
                  @click="menusTap(item.info[1].value)">
                <view class='pictrue'>
                    <image :src="item.img == ''? default_img : item.img "></image>
                </view>
                <view class="menu-txt">{{item.info[0].value}}</view>
            </view>
        </block>
    </view>
</template>

<script>
    export default {
        name: 'menus',
        props: {
            dataConfig: {
                type: Object,
                default: () => {
                }
            }
        },
        watch: {
            dataConfig() {
                this.bgColor = this.dataConfig.bgColor.color
                this.menus = this.dataConfig.menuConfig.list
            }
        },
        data() {
            return {
                menus: this.dataConfig.menuConfig.list,
                rowStyle: this.dataConfig.rowStyle.type ? '20%' : '25%',
                titleColor: this.dataConfig.titleColor.color[0].item,
                default_img:"../../static/img/default-img.png",
                mbConfig: this.dataConfig.mbConfig.val,
                bgColor: [{
                    item: '#FFFFFF'
                }, {
                    item: '#FFFFFF'
                }]
            };
        },
        created() {
            this.bgColor = this.dataConfig.bgColor.color
            this.menus = this.dataConfig.menuConfig.list
        },
        methods: {
            menusTap(url) {
                let isLogin = false;
                if (url.indexOf('needLogin=1') > -1) {
                    isLogin = true;
                }
                if(url.includes('/pagesA/shop/sign')){
                    this.$emit('toUrl', url, isLogin, 1)
                    return
                }
                
                this.$emit('toUrl', url, isLogin)
            }
        },
    }
</script>

<style scoped lang="less">
    .nav {
        padding: 50rpx 30rpx 10rpx;
        .item {
            /*margin-top: 30rpx;*/
            width: 20%;
            text-align: center;
            font-size: 24rpx;
            padding-bottom: 40rpx;

            .pictrue {
                width: 60rpx;
                height: 60rpx;
                margin: 0 auto;

                image {
                    width: 100%;
                    height: 100%;
                    /*border-radius: 50%;*/
                }
            }

            .menu-txt {
                margin-top: 15rpx;
            }

            &.four {
                width: 25%;

                .pictrue {
                    width: 90rpx;
                    height: 90rpx;
                }
            }
        }
    }
</style>
