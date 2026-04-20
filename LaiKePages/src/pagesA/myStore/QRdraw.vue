<template>
    <div class="container">
        <lktauthorize ref="lktAuthorizeComp"></lktauthorize>
        <heads :title="language.QRdraw.title"></heads>
        <div class="QR-content">
            <p>{{language.QRdraw.QR}}</p>
            <input type="text" :placeholder="language.QRdraw.QR_placeholder" :placeholder-style="placeStyle" v-model="shop_code" />
        </div>
        <button @tap="QRsuccess" class="QR-btn">{{language.QRdraw.verification}}</button>
    </div>
</template>

<script>
export default {
    data() {
        return {
            title: '验证码提取',
            
            shop_code: '',
            order_id: '',
            p_price: '',
            sNo: '',
            placeStyle: 'color:#888888;'
        };
    },
    onLoad(option) {
        if (option.order_id) {
            this.order_id = option.order_id;
        }
    },
    onShow() {
        this.title=this.language.QRdraw.title
        this.shop_id = uni.getStorageSync('shop_id') ? uni.getStorageSync('shop_id') : this.$store.state.shop_id;
    },
    methods: {
        _navigateTo(url) {
            uni.navigateTo({
                url
            });
        },
        // 点击确认按钮提交验证码
        QRsuccess(e) {
            // 成功后跳转 QRsuccess页面

            uni.showLoading({
                title: this.language.QRdraw.Tips[0]
            });
            var me = this;
            if (me.shop_code == '') {
                uni.showToast({
                    title: this.language.QRdraw.Tips[1],
                    duration: 1500,
                    icon: 'none'
                });
                return;
            }
            var data = {
                api: 'app.mch.verification_extraction_code',
                
                shop_id: me.shop_id,
                order_id: me.order_id,
                extraction_code: me.shop_code
            };
            this.$req.post({data}).then(res => {
                uni.hideLoading();
                if (res.code == 200) {
                    me.order_id = res.data.order_id;
                    me.p_price = res.data.p_price;
                    me.sNo = res.data.sNo;
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: 'none'
                    });
                    uni.redirectTo({
                        url: '/pagesC/myStore/QRsuccess?p_price=' + me.p_price + '&sNo=' + me.sNo + '&order_id=' + me.order_id
                    });
                } else {
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: 'none'
                    });
                }
            })
            
        }
    }
};
</script>

<style scoped lang="less">
@import url("@/laike.less");
@import url('../../static/css/myStore/QRdraw.less');
</style>
