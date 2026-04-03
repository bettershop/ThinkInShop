<template>
    <view class="shareDemo">
        <lktauthorize ref="lktAuthorizeComp"></lktauthorize>
        
        <heads return-flag="1" title="分享示例"></heads>
        <!-- #ifdef H5 || MP-ALIPAY || MP-TOUTIAO || MP-BAIDU-->
        <button  v-clipboard:copy="shareHref" @click="showShareMask">分享按钮</button>
        <!-- #endif -->
        <!-- #ifndef H5 || MP-ALIPAY || MP-TOUTIAO || MP-BAIDU -->
        <button  @click="showShareMask">分享按钮</button>
        <!-- #endif -->


        <!-- 海报保存弹窗 -->
        <div class="mask" v-if="saveEWM">
            <div class="shareEwm">
                <image style="width: 80rpx;height: 80rpx;" v-if="!ewmImg" :src="load_img"></image>
                <img v-else :src="ewmImg" class="imgEwm" />

                <img :src="close" class="close" @tap="_closeAllMask" />

                <view class="saveEWMBtn" @tap="_downEWM()">
                    <img :src="saves" class="saves" />
                    {{language.goods.goodsDet.Save_picture}}
                </view>
            </div>
        </div>
        

        <!-- 分享弹框 -->
        <div class="mask" v-if="shareMask && !saveEWM" @tap="_closeAllMask">
            <!-- 邀请链接的弹框 -->
            <div class="shareMask" @tap.stop>
                <!-- 分享至 -->
                <div class="shareMaskTitle">{{language.goods.goodsDet.Share_with}}</div>

                <!-- #ifdef MP-WEIXIN -->
                <div class="sharepyq">
                    <div class="shareIcon">
                        <button class="share_btn" open-type="share">
                            <img :src="wx_img" />
                            <!-- 微信好友 -->
                            <p class="p">{{language.goods.goodsDet.Wechat_friends}}</p>
                        </button>
                    </div>
                    <div class="shareIcon" @tap="showSaveEWM('wx')">
                        <img :src="erm_img" />
                        <!-- 二维码分享 -->
                        <p>{{language.goods.goodsDet.QR}}</p>
                    </div>
                    <div class="clearfix"></div>
                </div>
                <!-- #endif -->
                
            </div>
        </div>
        
    </view>
</template>

<script>
export default {
        name: "shareDemo",
        data () {
            return {
                is_shop: true, // 是否是店铺分享
                shop_id: 1, // 店铺分享id
                sharehrefTitle: '', // 分享标题
                shareImg: '', // 分享图片
                ewmImg: '', // 保存的海报
                shareContent: '', // 分享内容
                shareHref: '', // H5 分享地址
                shareHref2: '', // 小程序分享页面路径
                shareMask: false, // 微信小程序下的显示分享判断
                saveEWM: false, // 显示海报
                load_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/5-121204193R7.gif',
                saves: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/save.png',
                wx_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/wechat.png',
                erm_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/ewmShare.png',
                
                shop_list: {
                    shop_name: '',
                    shop_logo: '',
                },

                pro: {
                    
                },
                
            }
        },
        onLoad () {
            
        },
        /**
         * 转发
         * pagesC/goods/goodsDetailed.vue 的 onShareAppMessage 是这么写的，未登录则调到登录页
         * 去掉了未登录状态就去登录页面，小程序用授权登录的，跳到登录页面，人家怎么登录
         * @param res
         * @returns {{path: string, success: success, imageUrl: string, bgImgUrl: string, title: string}}
         */
        onShareAppMessage (res) {
            return {
                title: this.sharehrefTitle,
                path: this.shareHref2,
                imageUrl: this.shareImg,
                bgImgUrl: this.shareImg,
                success: () => {
                    
                }
            };
        },
        
        methods: {
            showSaveEWM(string) {
                this.is_shop = true
                if (this.is_shop) {
                    this.LaiKeTuiShopEWM();
                } else {
                    this.LaiKeTuiShowSaveEWM(string);
                }
            },

            LaiKeTuiShopEWM () {
                let data = {
                    api: 'app.getcode.share_shop',
                    shop_id: this.shop_id,
                }
                this.$req.post({data}).then(res => {
                    if (res.code == 200) {
                        this.ewmImg = this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + res.data.imgUrl
                        this.saveEWM = true
                    } else {
                        uni.showToast({
                            title: res.msg,
                            duration: 1500,
                            icon: 'none'
                        })
                    }
                })
            },

            LaiKeTuiShowSaveEWM (string) {
                
            },

            _downEWM() {
                let me = this;
                uni.downloadFile({
                    url: this.ewmImg,
                    success(res) {
                        let filePath = res.tempFilePath;
                        uni.saveImageToPhotosAlbum({
                            filePath: filePath,
                            success () {
                                uni.showToast({
                                    title: me.language.goods.goodsDet.Save_success,
                                    duration: 1500,
                                    icon: 'none'
                                });
                                me.shareMask = false;
                                me.saveEWM = false;
                            },
                            fail: function() {
                                uni.showToast({
                                    title: me.language.goods.goodsDet.fail_success,
                                    duration: 1500,
                                    icon: 'none'
                                });
                            }
                        });
                    },
                    fail: function() {
                        uni.showToast({
                            title: me.language.goods.goodsDet.fail_success,
                            duration: 1500,
                            icon: 'none'
                        });
                    }
                });
            },
            
            _closeAllMask() {
                this.shareMask = false;
                this.saveEWM = false;
            },
            
            showShareMask (shop_id) {
                this.generateShareUrl(shop_id)
                
                this.isLogin(() => {
                    this.shareMask = true;
                    
                   
                })
            },
            /**
             * 取得分享的url
             * @param shop_id
             */
            generateShareUrl (shop_id) {
                if (shop_id && shop_id > 0) {
                    this.is_shop = true;
                    this.shop_id = shop_id;
                    this.sharehrefTitle = this.shop_list.shop_name;
                    this.shareImg = this.shop_list.shop_logo;
                    this.shareContent = this.shop_list.shop_name;
                    var url = uni.getStorageSync('h5_url');
                    this.shareHref = url + 'pagesB/store/store?is_share=true&shop_id=' + shop_id;
                    this.shareHref2 = '/pagesB/store/store?is_share=true&shop_id=' + shop_id;
                } else {
                    this.is_shop = false;
                    this.shareHref2 = '/pagesC/goods/goodsDetailed?productId=' + this.pro_id + '&isfx=true';
                    this.sharehrefTitle = this.pro.name;
                    this.shareImg = this.imgurl;
                    this.shareContent = this.pro.name;
                    var url = uni.getStorageSync('h5_url');
					
                    this.shareHref = url + 'pages/index/share?pages=goodsDetailed&productId=' + this.pro_id + '&isfx=true';
                    if (this.pro.is_distribution == 1) {
                        this.isDistribution = true;
                        this.shareHref = url + 'pages/index/share?pages=goodsDetailed&productId=' + this.pro_id + '&isfx=true&fatherId=' + this.pro.user_id;
                        this.shareHref2 = '/pagesC/goods/goodsDetailed?productId=' + this.pro_id + '&isfx=true&fatherId=' + this.pro.user_id;
                    }
                }
            }
        },
    }
</script>

<style scoped lang="less">
.mask {
    right: 0;
    bottom: 0;
    display: flex;
    align-items: center;
    height: 100vh;
    width: 100%;
    background-color: rgba(000, 000, 000, 0.5);
    position: fixed;
    top: 0;
    left: 0;
    z-index: 999 !important;

    .shareMask {
        width: 100%;
        max-height: 90%;
        background-color: #fff;
        position: absolute;
        bottom: 0;

        .shareMaskTitle {
            height: 100rpx;
            line-height: 100rpx;
            font-size: 36rpx;
            text-align: center;
            color: #020202;
        }

        .sharepyq {
            width: 70%;
            margin: 0 auto;


            .shareIcon {
                width: 50%;
                float: left;
                text-align: center;

                .share_btn {
                    background: transparent;
                    padding: 0;
                    margin: 0;
                    border-radius: 0;
                    width: 230rpx;
                    height: 200rpx;
                }

                .share_btn::after {
                    border: none;
                }

                .share_btn .p {
                    margin-top: -26rpx;
                }

                img {
                    width: 80rpx;
                    height: 80rpx;
                    margin-bottom: 25rpx;
                }

                p {
                    height: 50rpx;
                    line-height: 50rpx;
                    font-size: 24rpx;
                    margin-bottom: 25rpx;
                    color: #666;
                    text-align: center;
                }
            }
        }
    }
    
    
    .shareEwm {
        position: absolute;
        left: 10%;
        top: 10%;
        height: 964rpx;
        width: 600rpx;
        background-color: #fff;
        border-radius: 8px;
        overflow: hidden;

        display: flex;
        align-items: center;
        justify-content: center;

        .imgEwm {
            width: 100%;

            /* #ifdef MP-TOUTIAO */
            height: 92%;
            /* #endif */


            /* #ifndef MP-TOUTIAO */
            height: 100%;
            /* #endif */

            border-top-left-radius: 8px;
            border-top-right-radius: 8px;
            
        }

        .close {
            width: 40rpx;
            height: 40rpx;
            position: absolute;
            top: 24rpx;
            right: 24rpx;
        }

        .saveEWMBtn {
            border-top: 1px solid #E6E6E6;
            width: 100%;
            height: 100rpx;
            font-size: 32rpx;
            background-color: #fff;
            border-bottom-left-radius: 8px;
            border-bottom-right-radius: 8px;
            line-height: 100rpx;
            color: #000;
            position: absolute;
            bottom: 0;
            text-align: center;

            display: flex;
            justify-content: space-around;

            .saves {
                width: 26rpx;
                height: 26rpx;
                padding-right: 20rpx;
            }
        }
    }
}

</style>
