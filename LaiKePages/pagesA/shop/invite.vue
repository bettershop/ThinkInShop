<template>
    <div class="invite" @tap="mdmDivHidden">
        <div class="data_h" :style="{ height: halfWidth }"><div class="data_h_h" :style="{ height: halfWidth }"></div></div>
        <div class="bgImgDiv"><img class="img" :src="bgImg1" /></div>
        <div class="load" v-if="load">
            <div>
                <img :src="loadGif" />
                <p>{{language.shop.invite.Loading}}</p>
            </div>
        </div>
        <div class="head" v-else :style="{ top: halfWidth }">
            <span href="javascript:void(0);" @tap="_back" v-if="flag"><img :src="back2x" /></span>
            <img :src="bback" @tap="_back" v-if="!flag" class="header_img" />
            <div class="sign_right" @tap="_complete_report">{{language.shop.invite.ruel}}</div>
        </div>
        <div class="invite_content">
            <div class="main">
                <img :src="textImg" class="textImg" />
                <div class="relative mt_85">
                    <img :src="coin1" class="coinImg coin1" />
                    <img :src="coin2" class="coinImg coin2" />
                    <img :src="coin1" class="coinImg coin3" />

                    <img :src="redImg" class="redImg" />
                    <div class="invite_user">{{language.shop.invite.Tips[0]}}</div>
                    <div class="invite_money">
                        <span class="span">{{ invitePrice }}</span>
                        {{language.shop.invite.yuan}}
                    </div>
                    <div class="invite_text">{{ text1 }}</div>
                </div>
                <div class="inviteMethod">
                    <div :style="{ width: storeWidth + '%' }" v-for="(item, index) in share_store" :key="index" @tap="_invite(item.name)">
                        <div><img class="img" :src="item.img" /></div>
                        <span class="span">{{ item.name }}</span>
                    </div>
                </div>
                <div class="invite_strategy">
                    <img class="img" :src="yqImg" />
                    <span style="font-size: 26rpx;color:#E3BC88;margin: 0 20rpx;">{{language.shop.invite.invite[0]}}</span>
                    <img class="img" :src="yqImg" />
                </div>
                <div class="strategy_content">
                    <div>
                        <div class="stepUp">
                            <div class="yuan">1</div>
                            <span class="span"></span>
                            <div class="yuan">2</div>
                            <span class="span"></span>
                            <div class="yuan">3</div>
                        </div>
                        <div class="stepDown">
                            <div>{{language.shop.invite.invite[1]}}</div>
                            <div>{{language.shop.invite.invite[2]}}</div>
                            <div>{{language.shop.invite.invite[3]}}</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="mdmImgDiv" v-if="mdmDiv">
            <img class="img" :src="mdmImg" @tap="_saveImg()" />
            <div @tap.stop="_saveImg()" class="mask">{{language.shop.invite.Tips[1]}}</div>
        </div>
        <div class="mask" v-if="complete_report">
            <div class="maskContent">
                <p class="maskContentTitle">{{language.shop.invite.ruel}}</p>
                <rich-text :nodes="rule" class="font_28"></rich-text>
                <div><button class="closeDiv" type="button" @tap="_oncomplete_report">{{language.shop.invite.got}}</button></div>
            </div>
        </div>
    </div>
</template>

<script>
import { mapMutations } from 'vuex';
import htmlParser from '@/common/html-parser.js';

export default {
    data() {
        return {
            coin1: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/coin1.png',
            coin2: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/coin2.png',
            yqImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/yq.png',
            yq1: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/yq1.png',
            yq2: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/yq2.png',
            yq3: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/yq3.png',
            loadGif: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/5-121204193R7.gif',
            bgImg1: '',
            textImg: '',
            redImg: '',
            title: '邀请有奖',
            back2x: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/back2x.png',
            bback: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/bback.png',
            flag: true,
            guanbi: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/qiandaoguanbi2x.png',
            complete_report: false,
            text1: '',
            invitePrice: '',
            share_store: '',
            storeWidth: '',
            mdmImg: '',
            mdmDiv: false,
            load: true,
            logo: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/lktlogo.png',
            shareContent: '一起来用来客推吧！', //分享的内容
            fastTap: true,
            rule: '', //活动规则
            shareHref: '' //分享链接
        };
    },
    computed: {
        halfWidth: function() {
            var gru = uni.getStorageSync('data_height') ? uni.getStorageSync('data_height') : this.$store.state.data_height;
            var heigh = parseInt(gru);
            return heigh + 'px';
        }
    },
    onLoad() {
		this.setLang();
		this.shareContent = this.language.showModal.shareContent
		
        uni.getProvider({
            service: 'share',
            success: e => {
                let data = [];
                for (let i = 0; i < e.provider.length; i++) {
                    switch (e.provider[i]) {
                        case 'weixin':
                            data.push({
                                name: '分享到微信好友',
                                id: 'weixin',
                                sort: 0
                            });
                            data.push({
                                name: '分享到微信朋友圈',
                                id: 'weixin',
                                type: 'WXSenceTimeline',
                                sort: 1
                            });
                            break;
                        case 'sinaweibo':
                            data.push({
                                name: '分享到新浪微博',
                                id: 'sinaweibo',
                                sort: 2
                            });
                            break;
                        case 'qq':
                            data.push({
                                name: '分享到QQ',
                                id: 'qq',
                                sort: 3
                            });
                            break;
                        default:
                            break;
                    }
                }
                this.providerList = data.sort((x, y) => {
                    return x.sort - y.sort;
                });
            },
            fail: e => {
                uni.showModal({
                    content: this.language.showModal.loginNo,
					confirmText: this.language.showModal.confirm,
					cancelText: this.language.showModal.cancel,
                    showCancel: false
                });
            }
        });
    },
    onShow() {
        
        this._request();
    },
    methods: {
        _saveImg() {
            let me = this;
            if (!this.fastTap) {
                return;
            }
            this.fastTap = false;
            uni.downloadFile({
                src: me.mdmImg,
                success: function(image) {
                    uni.saveImageToPhotosAlbum({
                        filePath: image.tempFilePath,
                        success: function() {
                            me.fastTap = true;
                            uni.showToast({
                                title: me.language.shop.invite.Tips[2],
                                icon: 'none',
                                duration: 2000
                            });
                            me.mdmDiv = false;
                        },
                        error: function() {
                            me.fastTap = true;
                            uni.showToast({
                                title: me.language.shop.invite.Tips[3],
                                icon: 'none',
                                duration: 2000
                            });
                            me.mdmDiv = false;
                        }
                    });
                }
            });
        },
        _invite(type) {
            if (!this.access_id) {
                uni.navigateTo({
                    url: '/pagesD/login/newLogin'
                });
                return;
            }
            if (!this.fastTap) {
                return;
            }
            if (type == '面对面邀请') {
                let data = {
                    api: 'app.invitation.invitation_picture'
                };

                this.$req.post({ data }).then(res => {
                    this.fastTap = true;
                    if (res.code == 200) {
                        if (res.status == 1) {
                            this.mdmImg = res.url;
                            this.mdmDiv = true;
                        }
                    }
                });
            } else if (type == '朋友圈') {
                uni.share({
                    provider: 'weixin',
                    scene: 'WXSenceTimeline',
                    type: 0,
                    href: this.shareHref,
                    title: this.language.shop.invite.Tips[4],
                    summary: this.shareContent,
                    imageUrl: this.logo,
                    success: res => {
                        this.fastTap = true;
                    },
                    fail: err => {
                        this.fastTap = true;
                    }
                });
            } else if (type == '微信') {
                uni.share({
                    provider: 'weixin',
                    scene: 'WXSceneSession',
                    type: 0,
                    href: this.shareHref,
                    title: this.language.shop.invite.Tips[4],
                    summary: this.shareContent,
                    imageUrl: this.logo,
                    success: res => {
                        this.fastTap = true;
                    },
                    fail: err => {
                        this.fastTap = true;
                    }
                });
            } else if (type == 'QQ') {
                uni.share({
                    provider: 'qq',
                    type: 1,
                    href: this.shareHref,
                    title: this.language.shop.invite.Tips[4],
                    summary: this.shareContent,
                    imageUrl: this.logo,
                    success: res => {
                        this.fastTap = true;
                    },
                    fail: err => {
                        this.fastTap = true;
                    }
                });
            }
        },
        _complete_report() {
            this.complete_report = true;
        },
        _oncomplete_report() {
            this.complete_report = false;
        },
        _back() {
            this.flag = false;
            uni.navigateBack({
                delta: 1
            });
        },
        _back1() {
            if (this.return_r == 1) {
                uni.navigateBack({
                    delta: 2
                });
            } else if (this.return_r == 2) {
                uni.navigateBack({
                    delta: 3
                });
            } else if (this.return_r == 3) {
                uni.navigateTo({
                    url: '/pagesD/login/newLogin?register_type=1'
                });
            } else {
                uni.navigateBack({
                    delta: 1
                });
            }
            this.flag = true;
        },
        _request() {
            var me = this;
            let data = {
                api: 'app.invitation.index'
            };

            this.$req.post({ data }).then(res => {
                if (res.code == 200) {
                    let { status, share_store, price, data } = res;
                    if (status == 1) {
                        this.bgImg1 = data.bg_img;
                        this.textImg = data.sucai2_img;
                        this.redImg = data.sucai1_img;
                        this.text1 = data.text1;
                        this.invitePrice = price;
                        this.share_store = share_store;
                        this.storeWidth = 100 / share_store.length;
                        this.rule = htmlParser(data.rule);
                    } else {
                        uni.showToast({
                            title: this.language.shop.invite.Tips[5],
                            icon: 'none',
                            duration: 1500
                        });
                    }
                } else {
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: 'none'
                    });
                }

                this.load = false;
            });
        },
        mdmDivHidden() {
            if (this.mdmDiv) {
                this.mdmDiv = false;
                this.fastTap = true;
            }
        }
    }
};
</script>

<style scoped lang="less">
@import url("@/laike.less");
@import url('../../static/css/shop/invite.less');
</style>
