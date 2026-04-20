<template>
    <div>
        <lktauthorize ref="lktAuthorizeComp"></lktauthorize>
        <heads :title="language.QRsuccess.title"></heads>
        <div class="QR-card">
            <div>
                <img :src="order.gouhei2ximg" />
                <p>{{language.QRsuccess.Validation}}</p>
            </div>
            <p>{{language.QRsuccess.Tips[0]}}~</p>
        </div>
        <div class="hr"></div>
        <div class="QR-text">
            <p>
                {{language.QRsuccess.order_number}}：
                <span>{{ order.number }}</span>
            </p>
            <p>
                {{language.QRsuccess.Payment}}：
                <span>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(order.price)}}</span>
            </p>
            <button @tap="myOrder" class="myOrder" style="margin-top: 60rpx;border: 0;">{{language.QRsuccess.View_order}}</button>
            <button @tap="Return">{{language.QRsuccess.Back}}</button>
        </div>
    </div>
</template>

<script>
export default {
    data() {
        return {
            title: '扫码提取',
            order: {
                number: '',
                price: '',
                gouhei2ximg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/gouhei.png'
            }
        };
    },
    onLoad(option) {
        this.order.price = option.p_price;
        this.order.number = option.sNo;
    },
    methods: {
        _navigateTo(url) {
            uni.navigateTo({
                url
            });
        },
        // 查看订单
        myOrder() {
            uni.navigateTo({
                url: '../myStore/order?order_id=' + this.order.number
            });
        },
        // 返回按钮
        Return() {
            uni.navigateBack({
                delta: 1
            });
        }
    }
};
</script>

<style scoped lang="less">
@import url("@/laike.less");
@import url('../../static/css/myStore/QRsuccess.less');
</style>
