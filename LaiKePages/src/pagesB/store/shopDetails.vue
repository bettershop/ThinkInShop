<template>
    <div class="container" :style="shop_css">
        <lktauthorize
            ref="lktAuthorizeComp"
            v-on:pChangeLoginStatus="changeLoginStatus"
        ></lktauthorize>
        <heads
            :title="language.storeDetail.title"
            :bgColor="bgColor"
            :titleColor="'#333333'"
            :ishead_w="2"
            style="font-weight: 7000;font-size: 40rpx;"
        ></heads>
        <div class="load" v-if="load">
            <div>
                <img :src="loadImg" />
                <p>{{ language.addStock.load }}……</p>
            </div>
        </div>
        <div v-else>
            <ul class="setup-ul">
                <li>
                    <text>{{ language.storeDetail.dpmc }}</text>
                    <p>{{ formData.shop_name }}</p>
                </li>
                <li>
                    <text>{{ language.storeDetail.dpjj }}</text>
                    <p style="width: 448rpx;text-align: right;">
                        {{ formData.shop_information }}
                    </p>
                </li>
            </ul>
            <ul class="setup-ul" style="margin: 22rpx 0; border-radius: 24rpx">
                <li>
                    <text>{{ language.storeDetail.dpdz }}</text>
                    <p style="width: 448rpx;text-align: right;">{{ formData.sheng }}{{ formData.shi }}{{ formData.xian }}{{ formData.address }}</p>
                </li>
                <li>
                    <text>{{ language.storeDetail.lxdh }}</text>
                    <p>{{ formData.mobile }}</p>
                </li>
                <li>
                    <text>{{ language.storeDetail.yysj }}</text>
                    <p>{{ formData.business_hours }}</p>
                </li>
            </ul>
            <ul class="setup-ul" style="border-radius: 24rpx" v-if="formData.shop_list && formData.shop_list.length > 0">
                <li
                    @tap="
                        _toAllGoods(
                            '/pagesA/myStore/storeList?isStore=true&shop_id=' +
                                shop_id,
                            'navigate'
                        )
                    "
                >
                    <text>{{ language.storeDetail.ckmd }}</text>
                    <div class="seeStore">
                        <img :src="jiantou" alt="" />
                    </div>
                </li>
            </ul>
        </div>
    </div>
</template>

<script>
export default {
    data() {
        return {
            title: "店铺详情",
            shop_css: "min-height:100vh",
            bgColor: [
                {
                    item: "#ffffff",
                },
                {
                    item: "#ffffff",
                },
            ],
            load: false,
            jiantou:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/jiantou2x.png",
            formData: {},
            shop_id: "",
            loadImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/5-121204193R7.gif',
        };
    },
    onShow() {},
    onLoad(option) {
        let longitude = uni.getStorageSync("longitude");
        let latitude = uni.getStorageSync("latitude");
        this.shop_id = option.shop_id;
        this.load=true
        this.$req
            .post({
                data: {
                    api:"mch.App.Mch.Store_homepage",
                    shop_id: option.shop_id,
                    type: option.type,
                    longitude,
                    latitude,
                },
            })
            .then((res) => {
                if (res.code == 200) {
                    this.formData = res.data;
                    this.load=false
                }
            })
    },
    methods: {
        changeLoginStatus() {},
        _toAllGoods(url, type, parameter) {
            if (url) {
                if (type == "navigate") {
                    uni.navigateTo({
                        url,
                    });
                    this.page = 1;
                    this.loadingType = 0;
                } else if (type == "switchTab") {
                    this.page = 1;
                    this.loadingType = 0;
                    uni.switchTab({
                        url,
                    });
                }
            }
        },
    },
};
</script>
<style scoped lang="less"> 
@import url("@/laike.less");
.container {
    background: #f4f5f6;
}
.setup-ul {
    position: relative;
    margin: initial;
    border-radius: 0 0 24rpx 24rpx;
    padding: 0 32rpx;
    color: #333333;
}

.setup-ul > li {
    display: flex;
    flex-flow: row nowrap;
    justify-content: space-between;
    align-items: center;

    font-size: 32rpx;
    background: #ffffff;
    padding: 32rpx 0;
    border-bottom: 0.5px solid #eee;
    text{
        color: #333;
        font-size: 32rpx;
    }
    p{
        color: #666666;
        font-size: 32rpx;
    }
}
.setup-ul > li:last-child {
    margin-bottom: initial;
    border-bottom: initial;
}

.seeStore {
    display: flex;
    align-items: center;
    .size(24rpx);
    color: #999999;
     
    font-weight: normal;
    box-sizing: border-box;

    img {
        width: 32rpx;
        height: 44rpx;
        margin-left: auto;
    }
}
</style>
