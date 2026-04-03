<template>
    <view class="container">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="title"></heads>
        <view class="content">
            <view class="list" @tap="toUrl('/pagesA/myStore/setMoney')">
                <view class="list_left">
                    <image :src="ecode" mode=""></image>
                    {{language.MScashier.ewmsk}}
                </view>
                <view class="list_right">
                    {{language.MScashier.szje}}
                    <image :src="jiantou"></image>
                </view>
            </view>
            <view class="list ecode" v-if="setMoneyObj.money">
                <view class="ecode_title">{{language.MScashier.sewmxw}}</view>
                <view class="ecode_price">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(setMoneyObj.money)}}</view>
                <view class="ecode_name" v-if="setMoneyObj.remark">{{setMoneyObj.remark}}</view>
                <image :src="code_url"></image>
                <view v-if="showRefresh" class="ecode_bottom" @tap="shuaxin()" >
                    {{language.MScashier.skmysx}}，
                    <view>{{language.MScashier.qsx}}</view>
                    <image :src="refresh" mode=""></image>
                </view>
            </view>
            <view class="list" @tap="toUrl('/pagesA/myStore/gatheringRecord')">
                <view class="list_left">
                    <image :src="skrcord" mode=""></image>
                    {{language.MScashier.skjl}}
                </view>
                <view class="list_right">
                    <image :src="jiantou"></image>
                </view>
            </view>
        </view>
    </view>
</template>

<script>
export default {
    data() {
        return {
            title: '',
            jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jiantou.png',
            ecode: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/mch/ecode.png',
            skrcord: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/mch/skrcord.png',
            refresh: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/mch/refresh.png',
            showRefresh: false,
            setMoneyObj: {},
            shop_id:'',
            type:'mini_wechat',
            money:'',
            remark:'',
            code_url:'',
            id:'',
            timeI: '',
            sencord: 60
        };
    },
    onLoad() {
        this.$store.state.setMoneyObj = {};
    },
    onShow() {
        this.shop_id = uni.getStorageSync('shop_id') ? uni.getStorageSync('shop_id') : this.$store.state.shop_id;
        this.showRefresh = false;
        this.setMoneyObj = this.$store.state.setMoneyObj;
        this.$store.state.setMoneyObj = {};
        this.title=this.language.MScashier.skm
        this.isLogin(()=>{
            if(this.setMoneyObj.money)
            {
                this.axios();
            }
		})
    },
    methods: {
        changeLoginStatus(){
            this.axios()
        },
        axios(){
            this.$req
                .post({
                    data: {
                        api: 'app.mch.cashier',
                        shop_id: this.shop_id,
                        type: this.type,
                        money:this.setMoneyObj.money,
                        remark:this.setMoneyObj.remark
                    }
                })
                .then(res => {
                    let { code, data, message } = res
                    this.loadFlag = true;
                    uni.hideLoading();
                    if (code == 200) {
                        this.code_url = this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + data.code_url;
                        this.id = data.id;
                        clearInterval(this.timeI);
                        this.showRefresh = false;
                        this.sencord = 60;
                        this.timeI = setInterval(()=>{
                            this.sencord--;
                            if(this.sencord == 0){
                                this.showRefresh = true;
                                clearInterval(this.timeI);
                            }
                        },1000)
                    } else {
                        uni.showToast({
                            title: message,
                            duration: 1500,
                            icon: 'none'
                        });
                    }
                });
        },
        shuaxin(){
            this.$req
                .post({
                    data: {
                        api: 'app.mch.refresh',
                        shop_id: this.shop_id,
                        id: this.id
                    }
                })
                .then(res => {
                    let { code, message } = res
                    uni.showToast({
                        title: message,
                        duration: 1500,
                        icon: 'none'
                    });
                    setTimeout(() => {
                        this.axios()
                    }, 1500);
                });
        },
        toUrl(url){
            this.isLogin(()=>{
                uni.navigateTo({
                    url
                })
            })
        }
    }
};
</script>

<style scoped lang="less">
@import url("@/laike.less");
@import url('../../static/css/myStore/cashier.less');
</style>
