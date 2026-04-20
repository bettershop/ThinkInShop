<template>
    <view class="container">
        <lktauthorize ref="lktAuthorizeComp"></lktauthorize>
        <heads :title="title"></heads>
        <view class="content">
            <view class="money">
                <view class="money_title">{{language.setMoney.skje}}</view>
                <view class="money_bottom">
                    <view>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</view>
                    <input type="digit" v-model="money" :placeholder="language.setMoney.srskje" placeholder-style="color: #B8B8B8;">
                </view>
            </view>
            <view class="remark">
                <image :src="edit"></image>
                <input v-model="remark" type="text" :placeholder="language.setMoney.tjbz" placeholder-style="color: #B8B8B8;">
            </view>
        </view>
        <view class="btn" :style="{opacity: haveClick?'':'0.4'}" @tap="submit">{{language.setMoney.qr}}</view>
    </view>
</template>

<script>
import { mapMutations } from 'vuex';
export default {
    data() {
        return {
            title: '',
            edit: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/mch/edit.png',
            money: '',
            remark: '',
        };
    },
    computed: {
        haveClick(){
            let flag = false;
            if(this.money){
                flag = true;
            }
            return flag
        }
    },
    onShow() {
        this.title=this.language.setMoney.title
    },
    methods: {
        ...mapMutations({
            setMoney: 'setMoney'
        }),
        submit(){
            this.setMoney({
                money: this.money,
                remark: this.remark,
            })
            uni.navigateBack({
                delta: 1
            })
        }
    }
};
</script>

<style scoped lang="less">
@import url("@/laike.less");
@import url('../../static/css/myStore/setMoney.less');
</style>
