<template>
    <div class="container">
        <heads :title="language.storeRefund.title"></heads>
        <toload :load="load">
            <div class="relative" :style="{ marginTop: halfWidth }">
                <div class="container_data">
                    <div class="proListUp pd_30">
                        <div class="proListUpLeft"><img :src="pro.imgurl" /></div>
                        <div class="proListUpRight">
                            <div class="proTitle">{{ pro.p_name }}</div>
                            <div class="proPro">{{ pro.size }}</div>
                        </div>
                        <div class="proSellData">
                            <div class="sellMoney">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(pro.p_price) }}</div>
                        </div>
                    </div>
                    <div class="hr"></div>
                    <div class="formList pd_30">
                        <div class="leftDiv">{{language.storeRefund.Return_type[0]}}</div>
                        <div v-if="pro.re_type == 1" class="RightDiv">{{language.storeRefund.Return_type[1]}}</div>
                        <div v-if="pro.re_type == 2" class="RightDiv">{{language.storeRefund.Return_type[2]}}</div>
                        <div v-if="pro.re_type == 3" class="RightDiv">{{language.storeRefund.Return_type[3]}}</div>
                    </div>
                    <div class="formList pd_30">
                        <div class="leftDiv">{{language.storeRefund.refund_amount}}</div>
                        <div class="RightDiv">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(pro.re_apply_money) }}</div>
                    </div>
                    <div class="formList pd_30">
                        <div class="leftDiv">{{language.storeRefund.Return_why}}</div>
                        <div class="RightDiv">{{ pro.content }}</div>
                    </div>
                    <div class="formList pd_30 miaoshu">
                        <div class="leftDiv">{{language.storeRefund.picture}}</div>
                        <div class="RightDiv"><img :src="item" @tap="seeImg(index)" v-for="(item, index) in returnImg" :key="index" /></div>
                    </div>
                </div>
            </div>
        </toload>
        <div class="mask" v-if="mask_display1">
            <div class="mask_cont">
                <p>{{language.storeRefund.Tips[0]}}？</p>
                <div class="mask_button">
                    <div class="mask_button_left" @tap="_cancel(1)">{{language.storeRefund.cancel}}</div>
                    <div @tap="_confirm(1)">{{language.storeRefund.confirm}}</div>
                </div>
            </div>
        </div>
        <div class="mask" v-if="mask_display2">
            <div class="mask_cont">
                <p class="p1">{{language.storeRefund.Tips[1]}}</p>
                <input type="text" v-model="reason" :placeholder="language.storeRefund.Tips[2]" :placeholder-style="placeStyle" />
                <div class="mask_button">
                    <div class="mask_button_left" @tap="_cancel(2)">{{language.storeRefund.cancel}}</div>
                    <div @tap="_confirm(2)">{{language.storeRefund.confirm}}</div>
                </div>
            </div>
        </div>
        <div class="mask" v-if="mask_display3">
            <div class="mask_cont">
                <p class="p1">{{language.storeRefund.Tips[3]}}</p>
                <input type="digit" v-model="refundM" @input="_checkMoney($event)" :placeholder="language.storeRefund.Tips[4]" :placeholder-style="placeStyle" />
                <div class="div1">
                    <img :src="warnIng" class="img" />
                    <span class="span">{{language.storeRefund.Tips[5]}}{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(refundNum) }}，{{language.storeRefund.Tips[6]}}</span>
                </div>
                <div class="mask_button">
                    <div class="mask_button_left" @tap="_cancel(3)">{{language.storeRefund.cancel}}</div>
                    <div @tap="_confirm(3)">{{language.storeRefund.confirm}}</div>
                </div>
            </div>
        </div>
        <div class="refundBtn" v-if="load">
            <div class="no" @tap="_no(pro.r_type)">{{language.storeRefund.refuse}}</div>
            <div class="yes" @tap="_yes(pro.re_type, pro.r_type)">{{language.storeRefund.adopt}}</div>
        </div>
    </div>
</template>

<script>
    export default {
        data() {
            return {
                returnImg: '',
                pro: '',
                title: '退货审核',
                load: false,
                testImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/yhqBg.png',
                warnIng: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/warnIng.png',
                mask_display1: false,
                mask_display2: false,
                mask_display3: false,
                reason: '',
                refundM: '',
                order_details_id: '',
                sNo: '',
                r_type: '',
                re_type: '',
                refundNum: '',
                placeStyle: 'color:#b8b8b8;font-size:28upx'
            };
        },
        onLoad(option) {
            this.shop_id = uni.getStorageSync('shop_id') ? uni.getStorageSync('shop_id') : this.$store.state.shop_id;
            this.order_details_id = option.order_details_id;
            this.sNo = option.sNo;
            this._axios();
        },
        methods: {
            _checkMoney(e) {
                if (Number(e.target.value) > this.refundNum) {
                    uni.showToast({
                        title: this.language.storeRefund.Tips[7],
                        duration: 1500,
                        icon: 'none'
                    });
                    this.refundM = this.refundNum;
                }
            },
            seeImg(index) {
                uni.previewImage({
                    current: '0',
                    urls: this.returnImg
                });
            },
            _axios() {
                this.$req
                    .post({
                        data: {

                            shop_id: this.shop_id,
                            sNo: this.sNo,
                            id: this.order_details_id,
                            api: 'app.mch.return_page',
                        }
                    })
                    .then(res => {
                        if (res.code == 200) {
                            this.pro = res.data.list[0];

                            let returnImg = []
                            res.data.imgs.filter(item=>{
                                if(item){
                                    returnImg.push(item)
                                }
                            })
                            this.returnImg = returnImg;
                            this.r_type = res.data.list[0].r_type;
                            this.re_type = res.data.list[0].re_type;
                            this.load = true;
                        } else {
                            uni.showToast({
                                title: res.message,
                                duration: 1500,
                                icon: 'none'
                            });
                            this.load = true;
                        }
                    });
            },
            _yes(re_type) {
                if (re_type == 1) {
                    this.mask_display1 = true;
                } else if (re_type == 2) {
                    this.mask_display3 = true;

                    this.$req
                        .post({
                            data: {
                                shop_id: this.shop_id,
                                sNo: this.sNo,
                                id: this.order_details_id,
                                api: 'app.mch.return_amount',

                            }
                        })
                        .then(res => {
                            if (res.code == 200) {
                                this.refundM = res.data.price;
                                this.refundNum = res.data.price;
                            } else {
                                uni.showToast({
                                    title: res.message,
                                    duration: 1500,
                                    icon: 'none'
                                });
                            }
                        });
                } else if (re_type == 3) {
                    this.mask_display1 = true;
                }
            },
            _no() {
                this.mask_display2 = true;
            },
            _confirm(type) {
                var data = {
                    shop_id: this.shop_id,
                    sNo: this.sNo,
                    id: this.order_details_id,
                    
                api:'mch.App.Mch.Examine',

                };
                if (type == 1) {
                    this.mask_display1 = false;
                    data.r_type = 1;

                    this.$req.post({ data }).then(res => {
                        if (res.code == 200) {
                            uni.showToast({
                                title: this.language.storeRefund.Tips[8],
                                duration: 1500,
                                icon: 'none'
                            });
                            setTimeout(function() {
                                uni.navigateBack({
                                    delta: 1
                                });
                            }, 1500);
                        } else {
                            uni.showToast({
                                title: res.message,
                                duration: 1500,
                                icon: 'none'
                            });
                        }
                    });
                } else if (type == 2) {
                    
                    data.text = this.reason;
                    if (this.re_type == 1) {
                        data.r_type = 2;
                    } else if (this.re_type == 2) {
                        data.r_type = 8;
                    } else if (this.re_type == 3) {
                        data.r_type = 10;
                    }
                    
                    if (!data.text) {
                        uni.showToast({
                            title: this.language.storeRefund.Tips[2],
                            icon: 'none'
                        })
                        return false;
                    }
                    this.mask_display2 = false;

                    this.$req.post({ data }).then(res => {
                        if (res.code == 200) {
                            uni.showToast({
                                title: this.language.storeRefund.Tips[9],
                                duration: 1500,
                                icon: 'none'
                            });
                            setTimeout(function() {
                                uni.navigateBack({
                                    delta: 1
                                });
                            }, 1500);
                        } else {
                            uni.showToast({
                                title: res.message,
                                duration: 1500,
                                icon: 'none'
                            });
                        }
                    });
                } else if (type == 3) {
                    this.mask_display3 = false;
                    data.price = this.refundM;
                    data.r_type = 9;
                    if(this.r_type == 1){
                        data.r_type = 4
                    }
                    this.$req.post({ data }).then(res => {
                        if (res.code == 200) {
                            uni.showToast({
                                title: this.language.storeRefund.Tips[10],
                                duration: 1500,
                                icon: 'none'
                            });
                            setTimeout(function() {
                                uni.navigateBack({
                                    delta: 1
                                });
                            }, 1500);
                        } else {
                            uni.showToast({
                                title: res.message,
                                duration: 1500,
                                icon: 'none'
                            });
                        }
                    });
                }
            },
            _cancel(type) {
                if (type == 1) {
                    this.mask_display1 = false;
                } else if (type == 2) {
                    this.mask_display2 = false;
                } else if (type == 3) {
                    this.mask_display3 = false;
                }
            }
        }
    };
</script>

<style lang="less">
    @import url("@/laike.less");
    @import url('../../static/css/myStore/storeRefund.less');
</style>
