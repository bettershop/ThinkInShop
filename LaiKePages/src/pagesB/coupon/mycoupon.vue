<template>
    <view class="container">
        <heads :title="language.mycoupon.title" :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
        <!-- 导航栏 -->
        <view class="nav">
            <view 
                :class="active == index ? 'active' : ''" 
                :key="index" 
                v-for="(item, index) in language.mycoupon.tabBar"
                @tap="navTo(index)"
            >
                {{ item }} 
                <span v-if="index == 0 && wsy_num>0">({{wsy_num}})</span>
                <span v-if="index == 2 && ygq_num>0">({{ygq_num}})</span>
                <span v-if="index == 1 && ysy_num>0">({{ysy_num}})</span>
            </view>
        </view>
        <div class="skeleton" v-if="!load">
            <div class="content">
                <div class="coupon" style="height: 248rpx;" v-for="item of 3" :key="item">
                    <div class=" coupon_top" style="height: 186rpx;">
                        <div class="coupon_top_left">
                            <view>
                                <span class="coupon_name skeleton-rect" style="width: 100px;">xx</span>
                            </view>
                            <span class="skeleton-rect">xx {{language.mycoupon.endTime}}</span>
                        </div>
                    </div>
                    <view class="coupon_bottom skeleton-rect">
                        2
                    </view>
                </div>
            </div>

        </div>
        <template v-if="load">
            <scroll-view v-if="list.length > 0" class="content" scroll-y="true">
                <view class="compile" v-if="active!=0">
                    <view>{{yhjtxt}}：{{overdueNum}}{{useNum}}{{language.mycoupon.zhang}}</view>
                    <view class="compile_txt" @click="compileclick">{{compiletxt}}</view>
                </view>

                <view class="page" v-for="(item, index) in list" :key="index">
                    <view class="page_box" :style="!is_compile&&active!=0?'':'display:flex;'">
                        <view class="box_box" v-if="is_compile&&active!=0">
                            <image v-if="updata" style="width: 32rpx;height: 32rpx;" @click="choose_id(index)"
                                :src="item.is_check==true?mycoupon_yxz:mycoupon_wxz"></image>
                            <!-- <view></view> -->
                        </view>
                        <view :class="item.Instructions && active == 0?'note':'note2'">
                            <view :class="active == 0 ? 'coupon' : 'coupon1'"
                                :style="active==0 ? bg_img1 : (active==1?bg_img2:bg_img3)">
                                <image v-if="(item.activity_type == 4 || item.activity_type == 5) && active == 0"
                                    class="vip_img" :src="vip_img"></image>
                                <view class="coupon_top"
                                    :class="item.activity_type == 5 && active == 0 ? 'coupon_top1' : active == 1 || active == 2 ? 'coupon_top2' : ''"
                                    :style="item.activity_type == 5 && active == 0 ? 'border-color:#B0E1FF;' : ''">

                                    <!--  免邮 -->
                                    <view v-if="item.activity_type == 1" class="coupon_top_right"
                                        :class="active == 1 || active == 2?'hei':''">
                                        <view>
                                            <text class="coupon_top_right_data">{{language.mycoupon.coupon_mail}}</text>
                                        </view>
                                        <span>{{ item.limit }}</span>
                                    </view>
                                    <!--  满减 -->
                                    <view v-if="item.activity_type == 2" class="coupon_top_right"
                                        :class="active == 1 || active == 2?'hei':''">
                                        <view>
                                            {{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}
                                            <text class="coupon_top_right_data">{{ LaiKeTuiCommon.formatPrice(item.money) }}</text>
                                        </view>
                                        <span>{{ item.limit }}</span>
                                    </view>
                                    <!-- 折扣 -->
                                    <view v-else-if="item.activity_type == 3" class="coupon_top_right"
                                        :class="active == 1 || active == 2?'hei':''">
                                        <view>
                                            <text class="coupon_top_right_data">{{ item.discount }}</text>
                                            {{language.mycoupon.coupon_discount}}
                                        </view>
                                        <span>{{ item.limit }}</span>
                                    </view>
                                    <!-- 会员赠送券 -->
                                    <view v-else-if="item.activity_type == 4" class="coupon_top_right"
                                        :class="active == 1 || active == 2?'hei':''">
                                        <view v-if="item.money == 0">
                                            <text class="coupon_top_right_data">{{ item.discount }}</text>
                                            {{language.mycoupon.coupon_discount}}
                                        </view>
                                        <view v-else>
                                            {{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}
                                            <text class="coupon_top_right_data">{{ LaiKeTuiCommon.formatPrice(item.money) }}</text>
                                        </view>
                                        <span>{{ item.limit }}</span>
                                    </view>
                                    <!-- 兑换 -->
                                    <view v-else-if="item.activity_type == 5" class="coupon_top_right"
                                        :style="active == 0 ? bg_img1 + 'border-color:#B0E1FF;' : active == 1 ? bg_img4 : bg_img3">
                                        <image class="duihuan_img" :src="item.imgurl"></image>
                                    </view>

                                    <view class="coupon_top_left">
                                        <view>
                                            <span class="coupon_name"
                                                :style="active!=0?'color:#999999':'color:#FA5151'">{{ item.name ? item.name : item.product_title }}</span>
                                        </view>
                                        <span v-if="!item.expiry_time" class="left_span_txt"
                                            :style="active!=0?'color:#999999':'color:#FA5151'">{{language.mycoupon.txt}}
                                        </span>
                                        <span v-else class="left_span_txt"
                                            :style="active!=0?'color:#999999':'color:#FA5151'">{{ item.expiry_time }}{{language.mycoupon.endTime}}
                                        </span>
                                        <view class="conn-buttom" v-if="item.Instructions"
                                            :style="active!=0?'background-color: #fff;':''" @tap="discTap(index)">
                                            <view class="coupon_bottom_lefgt"
                                                :style="active!=0?'background-color: #fff;':''">
                                                {{language.mycoupon.instructions}}
                                                <image :class="!item.ishow?'coupon_bottom_img2':'coupon_bottom_img3'"
                                                    :src="discFlagbottom"></image>
                                            </view>
                                        </view>
                                    </view>

                                    <view class="coupon_bottom" :style="is_compile==false&&active==0?'':'width:60rpx;'">
                                        <view v-if="active == 0" class="coupon_bottom_right" @tap="to_detail(item)">
                                            {{language.mycoupon.toUse}}
                                        </view>
                                        <view v-if="item.discFlag" class="coupon_bottom_b">
                                            <text v-if="item.activity_type == 5" class="font_22">
                                                {{language.mycoupon.instructionsText[0]}}
                                                <br />
                                                {{language.mycoupon.instructionsText[1]}}{{ item.valid }}{{language.mycoupon.instructionsText[2]}}
                                            </text>
                                            <text v-else class="font_22">{{ item.Instructions }}</text>
                                        </view>
                                    </view>
                                </view>
                            </view>
                            <view class="note-box" v-if="item.Instructions && item.ishow">
                                <span class="note-font">{{ item.Instructions }}</span>
                            </view>
                        </view>
                    </view>
                    <!--            <view class="note-box" v-if="item.Instructions && item.ishow">
                        <span class="note-font">{{ item.Instructions }}</span>
                    </view> -->
                </view>
            </scroll-view>
            <view v-else class="no-bargain">
                <img :src="nobargainImg" />
                <p>{{language.mycoupon.noCoupon}}</p>
            </view>
        </template>
        <view class="All_box" v-if="is_compile==true&&active!=0">
            <view class="All_box_left" @click="allclick" v-if="is_is_all">
                <image class="All_box_left_img" :src="is_all?mycoupon_yxz:mycoupon_wxz"></image>
                <view class="All_box_left_txt">{{language.mycoupon.btn[0]}}</view>
            </view>
            <view class="All_box_right" @click="deleteclick">{{language.mycoupon.btn[1]}}</view>
        </view>
        <!-- <button v-if="load" class="mycoupon_bottom" @tap="toCoupon"> -->
        <!--  <image :src="coupon_icon"style="width: 40rpx;height: 36rpx;margin-right: 12rpx;"></image>
        {{language.mycoupon.bottomBtn}}</button> -->
        <div v-if="load && isCouponShow" class="mycoupon_bottom" @tap="toCoupon">
            <image :src="coupon_icon" style="width: 40rpx;height: 36rpx;margin-right: 12rpx;"></image>
            <span style="color: #333333;font-size: 32rpx;">{{language.mycoupon.bottomBtn}}</span>
        </div>
        <skeleton :animation="true" :loading="!load" bgColor="#FFF"></skeleton>
        <!-- 提示弹窗 -->
        <!--        <view class="xieyi" v-if="is_ts">
            <view>

                <scroll-view scroll-y="true"  class="ruleModal">
                    <view class="xieyi_title" style="margin-bottom: 64rpx;">{{is_ts_tit}}</view>
                </scroll-view>
                <view class="xieyi_btm" @click.stop="_xieyiShow('ts')">{{language.mycoupon.know}}</view>
            </view>
        </view> -->
        <!-- 提示 -->
        <view class="tishi"
            style="z-index: 999; position: fixed;top: 50%;margin-top: -46rpx;left: 50%;margin-left: -128rpx;"
            v-if="is_tishi">
            <view
                style="width: 256rpx;height: 92rpx;background-color: rgba(0, 0, 0, .8);border-radius: 48rpx;color: #ffffff;display: flex;align-items: center;justify-content: center;">
                <span style="font-size: 16px;font-weight: 400;">{{is_tishi_content}}</span>
            </view>
        </view>
    </view>
</template>

<script>
import skeleton from "@/components/skeleton";
    export default {
        data() {
            return {
                title: '优惠券',
                active: 0,
                list: [],
                is_tishi: false,
                is_tishi_content: '删除成功',
                yhjtxt: '过期优惠券',
                discArray: [],
                //弹窗
                is_ts: false,
                is_ts_tit: '',
                loop: true,
                useNum: '', //已使用优惠卷
                overdueNum: '', //过期优惠卷
                is_all: false,
                isCouponShow: '',
                is_is_all: true, //刷新用的
                is_compile: false,
                compiletxt: '编辑',
                bg_color1: 'border-color:#B0E1FF;background: linear-gradient(to top, #F7FCFF 0%, #F2FAFF 100%);',
                vip_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/coupon/vip.png',
                coupon_icon: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/coupon_icon.png',
                mycoupon_wxz: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/mycoupon_wxz.png',
                mycoupon_yxz: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/mycoupon_yxz.png',
                nobargainImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/mycoupon_k.png',
                mycoupon_ygq: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/mycoupon_ygq.png',
                mycoupon_ysy: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/mycoupon_ysy.png',
                mycoupon_ygq_vip: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/mycoupon_vipygq.png',
                discFlagtop: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/coupon/top.png',
                discFlagbottom: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/mycoupon_genduo.png',
                load: false,
                updata: true,
                bgColor: [{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ], 
                wsy_num: '',
                ygq_num: '',
                ysy_num: '',
            };
        },
        computed: {
            bg_img1() {
                let width = uni.upx2px(138);
                return `background: url(${this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL}images/icon1/mycoupon_redbj.png);background-size:100% 100%;`;
            },
            bg_img2() {
                let width = uni.upx2px(88);
                return `background: url(${this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL}images/icon1/mycoupon_ysy.png);background-size:100% 100%;border-color:rgb(221, 221, 221)`;
            },
            bg_img3() {
                let width = uni.upx2px(88);
                return `background: url(${
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL
            }images/icon1/mycoupon_ygq.png);background-size:750rpx 100%;border-color:rgb(221, 221, 221)`;
            },
            bg_img4() {
                let width = uni.upx2px(88);
                return `border-color:#DDDDDD;background: url(${this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL}images/icon1/beenuse.png) no-repeat top right/${width}px ${width}px;`;
            }
        },
        onLoad(option) {
            if (option.loop) {
                this.loop = false;
            }

            for (let i = 0; i < this.list.length; i++) {
                this.discArray.push(false);
            }
        },
        onShow() { 
            this._axios();
        },
        components: { 
            skeleton
        },
        methods: {
            //删除
            deleteclick() {
                var newlist = []
                for (var i = 0; i < this.list.length; i++) {
                    if (this.list[i].is_check) {
                        newlist.push(this.list[i].id)
                        newlist.join(',')
                    }
                }
                if (newlist.length == 0) {
                    uni.showToast({
                        icon: 'none',
                        title: this.language.mycoupon.qxzyhq
                    })
                    return
                }
                let data = {
                    api: "plugin.coupon.Appcoupon.BatchDel",
                    ids: newlist
                };

                this.$req.post({
                    data
                }).then(res => {
                    if (res.code == 200) {
                        this.is_tishi = true
                        setTimeout(() => {
                            this.is_tishi = false
                        }, 1000)
                        this._axios()
                    } else {
                        uni.showToast({
                            icon: 'none',
                            title: res.message
                        })
                    }
                })
            },
            //全选
            allclick() {
                for (var i = 0; i < this.list.length; i++) {
                    if (this.is_all == true) {
                        this.list[i].is_check = false
                    } else {
                        this.list[i].is_check = true
                    }
                }

                this.is_all = !this.is_all
                this.is_is_all = false
                this.is_is_all = true
                this.updata = false
                this.updata = true
            },
            //选中
            choose_id(e) {
                this.list[e].is_check = !this.list[e].is_check
                var newlist = []
                var truelist = [] //用于接受 列表已经勾选
                var falselist = [] //以上取反
                this.list.forEach(item => { //判断是否全选

                    if (item.is_check) {
                        this.is_all = true
                        truelist.push(item.is_check)
                    } else {
                        this.is_all = false
                        falselist.push(item.is_check)
                    }
                })
                if (this.list.length == truelist.length) { //是全选
                    this.is_all = true
                    this.is_is_all = false
                    this.is_is_all = true
                } else { //不是全选
                    this.is_all = false
                    this.is_is_all = false
                    this.is_is_all = true
                }
                for (var i = 0; i < this.list.length; i++) { //选中更换状态
                    if (this.list[i].is_check) {
                        this.list[i].is_check = true
                    } else {
                        this.list[i].is_check = false
                    }
                }
                this.updata = false
                this.updata = true
            },
            //编辑
            compileclick() {
                this.is_compile = !this.is_compile
                if (this.compiletxt == this.language.mycoupon.bj) {
                    this.compiletxt = this.language.mycoupon.wc
                } else {
                    this.compiletxt = this.language.mycoupon.bj
                }
            },
            // 导航切换
            navTo(e) {
                this.list = [];
                this.active = e;
                this.useNum = ''
                this.overdueNum = ''
                if (this.active == 1) {
                    this.yhjtxt = this.language.mycoupon.ysyyhq
                } else if (this.active == 2) {
                    this.yhjtxt = this.language.mycoupon.gqyhq
                }
                this.load = false;
                this.is_compile = false
                this.compiletxt = this.language.mycoupon.bj
                this._axios();
            },
            //关闭协议弹窗
            _xieyiShow(e) {
                this.is_ts = !this.is_ts
            },
            // 使用说明
            discTap(index) {
                this.list[index].ishow = !this.list[index].ishow
                this.$set(this.list[index], 'ishow', this.list[index].ishow);
            },
        
            // 立即使用优惠券
            to_detail(item) {

                if (item.mch_id != 0) {
                    uni.navigateTo({
                        url: '/pagesB/store/store?shop_id=' + item.mch_id
                    })
                } else if (item.skip_type === 2) {
                    if (!item.url) {
                        uni.redirectTo({
                            url: '/pages/shell/shell?pageType=home'
                        });
                        return false;
                    }
                    uni.navigateTo({
                        url: item.url
                    });
                } else {
                    if (!item.url) {
                        uni.redirectTo({
                            url: '/pages/shell/shell?pageType=home'
                        });
                        return false;
                    }
                    uni.redirectTo({
                        url: item.url
                    });
                }
                // }
            },
            _axios() {
                let data = {

                    api: "plugin.coupon.Appcoupon.MyCoupon",
                    type: this.active
                };

                this.$req.post({
                    data
                }).then(res => {
                    this.load = true
                    uni.hideLoading();
                    if (res.code == 200) {
                        this.list = res.data.list.map(item => {
                            item.ishow = false
                            return item
                        })
                        this.isCouponShow = res.data.isCouponShow
                        this.wsy_num = res.data.wsy_num
                        this.ygq_num = res.data.ygq_num
                        this.ysy_num = res.data.ysy_num
                    }
                    if (res.data.useNum) {
                        this.useNum = res.data.useNum
                    }
                    if (res.data.overdueNum) {
                        this.overdueNum = res.data.overdueNum
                    }
                    for (var a in this.list) {
                        this.list[a].is_check = false
                    }
                })

            },
            // 去领券中心
            toCoupon() {
                uni.navigateTo({
                    url: '/pagesA/shop/coupon?loop=false'
                });
            }
        }
    };
</script>
<style>
    page {
        background-color: #f4f5f6;
    }
</style>
<style scoped lang="less">
    @import url("@/laike.less");
    @import url('../../static/css/coupon/coupon.less');

    .note {
        width: 100%;
        border-radius: 24rpx;
        border: 2rpx solid #feeded;
        border-top: none;
    }
    .note2 {
        width: 100%;
        border-radius: 24rpx;
        border: 2rpx solid #f4f5f6;
        border-top: none;
    }
    .note-box {
        border-radius: 24rpx;
        background: #FFFFFF;
        padding: 4rpx 8rpx 8rpx 8rpx;
    }

    .note-font {
        font-weight: 400;
        font-size: 24rpx;
        color: #999999;
    }

    .no-bargain p {
        color: #333333 !important;
    }

    .All_box_right {

        width: 176rpx;
        height: 72rpx;
        line-height: 72rpx;
        text-align: center;
        background: #FA5151;
        border-radius: 36rpx;
        color: #FFFFFF;
        margin-right: 32rpx;
    }

    .All_box_left_img {
        width: 32rpx;
        height: 32rpx;
    }

    .All_box_left_txt {
        margin-left: 16rpx;
    }

    .All_box_left {
        display: flex;
        justify-content: center;
        align-items: center;
        color: #999999;

        font-size: 28rpx;
        margin-left: 32rpx;
    }

    .All_box {
        width: 100%;
        height: 104rpx;
        display: flex;
        justify-content: space-between;
        align-items: center;
        // padding-bottom: 110rpx;
        background-color: #FFFFFF;
        position: fixed;
        bottom: 0;
        padding-bottom: constant(safe-area-inset-bottom);
        /* 兼容 iOS < 11.2 */
        padding-bottom: env(safe-area-inset-bottom);
        left: 0;
        z-index: 999;
        border-top: 1rpx solid #eee;
    }

    .mycoupon_bottom {
        display: flex;

        justify-content: space-between;

        align-items: center;
    }

    .mycoupon_bottom {
        // .solidBtn();

        border-top: 1rpx solid #eee;
        background-color: #fff;
        width: 100%;
        height: 98rpx;
        color: #333333;

        position: fixed;
        bottom: 0;
        left: 0;
        font-size: 32rpx;
        justify-content: center;
        z-index: 99;
    }

    .page {
        display: flex;
        flex-direction: column;
        // border: 1px solid #feeded;
        // border-top: none;
        margin-bottom: 30rpx;
        // border-radius: 24rpx;
    }

    .compile_txt {
        color: #999999;
    }

    .page_box {
        display: flex;
        // flex-direction: row;
    }

    .box_box {
        margin-right: 32rpx;
        display: flex;
        width: 32rpx;
        justify-content: center;
        align-items: center;
    }

    .compile {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 32rpx;
        font-size: 28rpx;

        color: #333333;
    }

    .active {
        font-size: 32rpx !important;
        font-weight: 500 !important;
        color: #333333 !important;
        >span{
            color: #FF0000;
        }
    }

    .nav {
        height: 120rpx !important;
        background: #FFFFFF;
        border-radius: 0 0 24rpx 24rpx;
        border-bottom: initial !important;
        padding-top: 8rpx;
        box-sizing: border-box;
    }

    .nav .active::after {
        width: 96rpx;
        left: 76rpx;
        background: @tabColor;
    }

    .nav view {
        height: 80rpx !important;
        font-size: 28rpx;

        font-weight: 500;
        color: #999999;
    }

    .coupon_top_right_data {
        font-size: 48rpx !important;
        font-family: DIN-Bold, DIN;
        font-weight: bold;
        // color: #FA5151;
    }

    .coupon_btn {
        height: 104rpx;
        background: #FFFFFF;
        box-shadow: 0 0 0 0 rgba(0, 0, 0, 0.2);
        font-size: 32rpx;

        font-weight: 400;
        color: #333333;
        display: flex;
        position: fixed;
        justify-content: center;
        align-items: center;
    }

    .coupon_btn:after {
        border-radius: initial;
    }

    .content {
        background-color: #FFFFFF;
        border-radius: 24rpx 24rpx 0 0;
        margin-top: 24rpx;
    }


    .coupon {
        border: initial;
        border-radius: 24rpx;
        background: rgba(250, 81, 81, .1);
        margin-bottom: 0 !important;
        width: 100%;
    }


    .coupon_top_right {}

    .xieyi {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background-color: rgba(0, 0, 0, .5);
        z-index: 500;
        display: flex;
        justify-content: center;
        align-items: center;

        >view {
            width: 640rpx;
            min-height: 100rpx;
            max-height: 486rpx;
            background: #FFFFFF;
            border-radius: 24rpx;

            .xieyi_btm {
                height: 108rpx;
                color: @D73B48;
                display: flex;
                justify-content: center;
                align-items: center;
                border-top: 0.5px solid rgba(0, 0, 0, .1);
                font-weight: bold;
                font-size: 32rpx;
            }

            .ruleModal {
                height: 262rpx;
                padding: 64rpx 48rpx;
            }

            .xieyi_title {
                width: 544rpx;
                display: flex;
                justify-content: center;
                align-items: center;
                font-size: 32rpx;

                font-weight: 500;
                color: #333333;
                line-height: 44rpx;

                font-size: 32rpx;
            }

            .xieyi_text {
                width: 100%;
                max-height: 236rpx;
                overflow-y: scroll;
                padding: 0 32rpx;
                box-sizing: border-box;
            }
        }
    }

    @media screen and (min-width: 600px) {
        .xieyi {
            transform: translate(0) !important;
            left: 0 !important;
        }
    }
</style>
