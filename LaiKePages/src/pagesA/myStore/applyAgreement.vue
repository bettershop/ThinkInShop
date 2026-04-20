<template>
    <div class="us-all">
        <heads :title="title"  :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
        <div v-if="content" class="logo"><rich-text class="richtext" :nodes="content"></rich-text></div>
    </div>
</template>

<script>
import htmlParser from '../../common/html-parser.js';

export default {
    data() {
        return {
            title: '',
            log: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/15306155488.png',
            content: '',
            bgColor: [{
                    item: '#ffffff'
                },
                {
                    item: '#ffffff'
                }
            ],
        };
    },
    onLoad() {
        uni.showLoading({
            title: this.language.applyAgreement.load
        });

        this.$req
            .post({
                data: {
                    api:"mch.App.Mch.Agreement",
                }
            })
            .then(res => {
                uni.hideLoading();
                if (res.code == 200) {
                    var str = res.data.agreement
                        .replace(/<strong><span/g, "<span style='font-weight:bold;'")
                        .replace(/span><\/strong>/g, 'span>')
                        .replace(/<strong>/g, '<span style="font-weight:bold;">')
                        .replace(/strong>/g, 'span>');
                    this.content = htmlParser(str);

                    if (res.data.name) {
                        this.title = res.data.name;
                    } else {
                        this.title = this.language.applyAgreement.title;
                    }
                } else {
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: 'none'
                    });
                }
            });
    }
};
</script>

<style scoped lang="less">
@import url("@/laike.less");
@import url('../../static/css/myStore/applyAgreement.less');
</style>
