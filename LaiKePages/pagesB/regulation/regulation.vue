<template>
    <view>
        <heads
            :title="title"
            ishead_w="2"
            :bgColor="bgColor"
            :titleColor="titleColor"
        ></heads>
        <view class="page_box" v-html="content"></view>
    </view>
</template>

<script>
export default {
    data() {
        return {
            title: "积分规则",
            bgColor: [
                {
                    item: "#ffffff",
                },
                {
                    item: "#ffffff",
                },
            ],
            titleColor: "#333333",
            content: "",
            api: "",
        };
    },
    onLoad(option) {
        this.title = option.title;
        this.api = option.api;
        this._aiox();
    },
    methods: {
        _aiox() {
            let data = {
               
                api: this.api,
            };
            this.$req.post({ data }).then((res) => {
                if (res.code == 200) {
                    this.content = res.data.content||res.data.text||res.data.ruleText
                        
                } else {
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: "none",
                    });
                }
            });
        },
    },
};
</script>

<style lang="less">
.page_box {
    padding: 32rpx;
   /deep/p {
        img {
            width: 100%;
        }
    }
}
</style>
