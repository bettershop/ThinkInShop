<template>
    <view class='nav acea-row acea-row' :style="{marginTop:pxToRpxNum(mbConfig)+'rpx', background: `linear-gradient(90deg,${bgColor[0].item} 0%,${bgColor[1].item} 100%)`}" v-if="menus.length"> 
         
        <block v-for="(item,index) in menus" :key="index">
            <view class='item' :style="'width:'+ rowStyle +';color:'+titleColor" :url='item.info[1].value'
                  @click="menusTap(item.info[1].value,menus)">
                <view class='pictrue'>
                    <image :src="LaiKeTuiCommon.getFastImg(item.img == '' ? default_img : item.img, 144, 144)" lazy-load="true"></image>
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
            },
            shop_id: {
                type: Number,
                default:0
            },
        },
        watch: {
            shop_id:{
                handler(newVal,lodVal){
                    if(newVal && newVal>0){
                        this. shopId =shop_id
                    }
                },
                immediate:true
            },
            dataConfig() {
                this.bgColor = this.dataConfig.bgColor.color
                this.menus = this.dataConfig.menuConfig.list
            },
        },
        data() {
            return {
                shopId:0,
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
            console.log('menus dataConfig::',this.dataConfig)
            this.bgColor = this.dataConfig.bgColor.color
            this.menus = this.dataConfig.menuConfig.list
        },
        methods: {
            menusTap(url,item) { 
                let isLogin = false;
                if (url.indexOf('needLogin=1') > -1) {
                    isLogin = true;
                }
                if(url.includes('/pagesA/shop/sign')){
                    this.$emit('toUrl', url, isLogin, 1)
                    return
                }
                //判断url有?和参数了，没有就加上?mch_id
                if(this.shopId){                    
                    if(url.indexOf('?') == -1){
                        url = url + '?mch_id=' + this.shopId
                    }else{
                        url = url + '&mch_id=' + this.shopId
                    }
                }
                this.$emit('toUrl', url , isLogin)
            }
        },
    }
</script>

<style scoped lang="less">
    .nav {
        padding: 8rpx 24rpx 0rpx !important;
        .item {
            /*margin-top: 30rpx;*/
            width: 20%;
            text-align: center;
            font-size: 24rpx;
            padding-bottom: 8rpx;

            .pictrue {
               width: 72rpx;
               height: 72rpx;
                margin: 0 auto;

                image {
                    width: 72rpx;
                    height: 72rpx;
                    /*border-radius: 50%;*/
                }
            }

            .menu-txt {
                margin-top: 15rpx;
                font-family: Source Han Sans, Source Han Sans;
                font-weight: 500;
                font-size: 24rpx;
                color: #333333;
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
