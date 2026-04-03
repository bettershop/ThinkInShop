<template>
    <div class="container">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="language.storeTransactionRecords.title"></heads>
        <div class="relative container_content">
            <div class="topTabBar">
                <div class="width_50" @tap="changeTabBar(1)">
                    <div :class="{ active: topTabBar == 1 }">{{ language.storeTransactionRecords.tabList[0] }}</div>
                </div>
                <div class="width_50" @tap="changeTabBar(2)">
                    <div :class="{ active: topTabBar == 2 }">{{ language.storeTransactionRecords.tabList[1] }}</div>
                </div>
            </div>
            
            <div style="height: 104rpx;margin-bottom: 1rpx">
                <div class='allgoods_s home_navigation'>
                    <div class='home_input'>
                        <img class='searchImg' :src="serchimg" alt="">
                        <input type="text" v-model="searchtxt" :placeholder="language.fxProduct.search_placeholder2" id='input' 
                               name="sourch"/>
                    </div>
                    <div class="search_btn" @tap='_searchB'>{{language.search2.searchRes.search_button}}</div>
                </div>
            </div>
            

            <div class="tixian" v-if="topTabBar==2">
                <div v-if="list.length > 0" style="flex: 1;margin-top: 96rpx;">
                    <div class="orderList" v-for="(item, index) in list" :key="index">
                        <ul class="orderTitle">
                            
                           <li class="today_div">
                                <div class="date">
                                    {{ item.time }}
                                    <span v-if="item.time == now">{{ language.myFinance.today }}</span>
                                </div>
                            </li>

                            <li v-for="(items, indexs) in item.res" :key="indexs">
                                <div class="list_group1">
                                    <view><span>{{ items.type_name }}</span>  <span style="margin-left: 16rpx;">{{items.remake}}</span></view>
                                    <template v-if="type != 3">
                                        <span class="bold" v-if="items.status == 1">+{{ items.price }}</span>
                                        <span class="bold" v-else-if="items.status == 2">-{{ items.price }}</span>
                                    </template>
                                    <template v-else>
                                        <span class="bold" v-if="items.status == 1">+{{ items.integral }}</span>
                                        <span class="bold" v-else-if="items.status == 2">-{{ items.integral }}</span>
                                    </template>
                                </div>

                                <div class="list_group2">
                                    <span>{{ items.date }}</span>
                                    <span class="yue_money" v-if="type != 3">{{ language.myFinance.balance }} {{
                                            items.account_money
                                        }}</span>
                                    <span class="yue_money"
                                          v-else>{{ language.myFinance.integral }} {{ items.integral_money }}</span>
                                </div>
                            </li>
                        </ul>
                    </div>
                    <div v-if="list.length > 0 && loadingType == 2" class="nomore">{{ language.myFinance.noMore }}</div>
                </div>
                <div v-if="list.length == 0 && status" class="wsj_box">
                    <img class="wsj_img" :src="wushuju"/>
                    <div>{{ language.myFinance.noList }}</div>
                </div>
            </div>

            <template v-if="topTabBar!=2">
                <div v-if="list.length > 0" style="flex: 1;margin-top: 96rpx;">
                    <div class="orderList" v-for="(item, index) in list" :key="index">
                        <ul class="orderTitle">
                            <li class="today_div">
                                <div class="date">
                                    {{ item.time }}
                                    <span v-if="item.time == now">{{ language.myFinance.today }}</span>
                                </div>
                            </li>

                            <li v-for="(items, indexs) in item.res" :key="indexs">
                                <div class="list_group1">
                                    <view><span>{{ items.type_name }}</span>  <span style="margin-left: 16rpx;">{{items.remake}}</span></view>
                                    <template v-if="type != 3">
                                        <span class="bold" v-if="items.status == 1">+{{ items.price }}</span>
                                        <span class="bold" v-else-if="items.status == 2">-{{ items.price }}</span>
                                    </template>
                                    <template v-else>
                                        <span class="bold" v-if="items.status == 1">+{{ items.integral }}</span>
                                        <span class="bold" v-else-if="items.status == 2">-{{ items.integral }}</span>
                                    </template>
                                </div>

                                <div class="list_group2">
                                    <span>{{ items.date }}</span>
                                    <span class="yue_money" v-if="type != 3">{{ language.myFinance.balance }} {{
                                            items.account_money
                                        }}</span>
                                    <span class="yue_money"
                                          v-else>{{ language.myFinance.integral }} {{ items.integral_money }}</span>
                                </div>
                            </li>
                        </ul>
                    </div>
                    <div v-if="list.length > 0 && loadingType == 2" class="nomore">{{ language.myFinance.noMore }}</div>
                </div>
                <div v-if="list.length == 0 && status" class="wsj_box">
                    <img class="wsj_img" :src="wushuju"/>
                    <div>{{ language.myFinance.noList }}</div>
                </div>
            </template>

        </div>
    </div>
</template>

<script>
export default {
    data() {
        return {
            title: '账户明细',

            topTabBar: 1,
            shop_id: '',
            type: 1,
            list: [],
            status: false,
            wushuju: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/wuzhangdan.png',
            jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jiantou2x.png',
             serchimg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/searchNew.png',
             searchtxt: '',
            loadingType: 0,
            tabIndex: 0,  //提现明细tab
            page: 0,
        };
    },

    onLoad(option) {
        this.setLang();
        uni.showLoading({
            title: this.language.toload.loading
        });
    },
    onShow() {
        this.shop_id = uni.getStorageSync('shop_id') ? uni.getStorageSync('shop_id') : this.$store.state.shop_id;
        this.page = 0;
        this.list = [];
        this._axios();
    },
    methods: {
        onReachBottom () {
            if (this.loadingType == 1 || this.loadingType == 2) {return false}
            this.loadingType = 1;
            this.page ++;
            this._axios('account_details_load')
        },
        toTixian(id) {
            uni.navigateTo({
                url: './tixianDetail?id=' + id
            })
        },
        changeLoginStatus() {
            this._axios();
        },
        changeTab(index) {
            this.tabIndex = index
            this._axios();
        },
        changeTabBar(num) {
            this.topTabBar = num;
            this.type = num;
            this.list = [];
            this.status = false;
            this.page = 0;
            this._axios();
        },
        _searchB(){
            this.page = 1
            uni.pageScrollTo({
                duration:0,
                scrollTop:0
            })
            this.list = [];
            this._axios()
        },
        _axios(m = 'account_details') {
            uni.showLoading({
                title: this.language.toload.loading,
                mask: true
            });
            this.$req
                .post({
                    data: {
                        shop_id: this.shop_id,
                        module: 'app',
                        action: 'mch',
                        m,
                        page: this.page,
                        pege: this.page,
                        type: this.type,
                        tabIndex: this.tabIndex,
                        keyWord:this.searchtxt
                    }
                })
                .then(res => {
                    uni.hideLoading();
                    if (res.code == 200) {
                        if (this.topTabBar != 2) {
                            res.data.list.forEach(r => {
                                for (let item of r.res) {
                                    item.addtime = item.addtime.split(' ')[1];
                                }
                            });
                        }
                        this.list = this.list.concat(res.data.list);
                        if (this.list.length == 0) {
                            this.status = true;
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
    },
    computed: {
        now: function () {
            var year = new Date().getFullYear();
            var month = new Date().getMonth() + 1;
            var date = new Date().getDate();
            var date1 = year + '-' + month + '-' + date;
            return date1;
        },
        topTabBarTop() {
            var gru = uni.getStorageSync('data_height') ? uni.getStorageSync('data_height') : this.$store.state.data_height
            // #ifdef MP-TOUTIAO
            
            // #endif
            let height = 0
            // #ifndef MP-ALIPAY
            var heigh = parseInt(gru) + uni.upx2px(88)
            height = heigh && heigh > 0 ? heigh : uni.upx2px(88)
            // #endif

            // #ifdef MP
            heigh += 44
            // #endif


            return height + 'px'
        }
    }
};
</script>

<style lang="less">
@import url("@/laike.less");
@import url('../../static/css/myStore/records.less');
</style>
