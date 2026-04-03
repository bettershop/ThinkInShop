<template>
    <view class="container" :class="{ pb: tabIndex == 0 }">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <view class="container_top">
            <heads :title="language.activities_pro.title"></heads>

            <view class="tab">
                <view class="li" :class="{ active: tabIndex == 0 }" @tap="tabIndex = 0">{{language.activities_pro.goodList}}</view>
                <view class="li" :class="{ active: tabIndex == 1 }" @tap="tabIndex = 1">{{language.activities_pro.examined}}</view>
            </view>
        </view>
        <toload :load="load" v-if="!load"></toload>
        <view v-else>
            <view class="pro" v-for="(item, index) in list" :key="index">
                <view class="pro_top">
                    <image class="image" :src="item.img"></image>
                    <view class="pro_top_right">
                        <view class="proTitle" :style="tabIndex == 1 ? 'display: block;white-space: nowrap;overflow: hidden;text-overflow: ellipsis;' : ''">
                            {{ item.group_title }}
                        </view>
                        <view class="proSize">{{language.activities_pro.size}} {{ item.names }}</view>
                        <view class="proPrice" :style="tabIndex == 1 ? 'margin-top: 20rpx' : ''">
                            <view class="red" :class="{ black: tabIndex == 1 }">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(item.price) }}</view>
                            <view>{{language.activities_pro.kucun}} {{ item.num }}</view>
                        </view>
                        <view
                            v-if="tabIndex == 1"
                            style="white-space: nowrap;overflow: hidden;text-overflow: ellipsis;font-size: 28rpx;line-height: 28rpx;color:#FF7E00;margin-top: 12rpx"
                        >
                            {{ item.audit_status == 0 ? language.activities_pro.pendingReview : item.audit_status == 1 ? language.activities_pro.inReview : language.activities_pro.auditFailure + item.reason }}
                        </view>
                    </view>
                </view>
                <view class="pro_bottom">
                    <view class="btn" v-if="item.audit_status == 0" @tap="delPro(item)">{{language.activities_pro.delete}}</view>
                    <view class="btn" v-if="item.audit_status == 1" @tap="reqSH(item, 0)">{{language.activities_pro.undo}}</view>

                    <view class="btn" @tap="toEdit(item)" v-if="item.audit_status == 2">{{language.activities_pro.addInventory}}</view>
                    <view class="btn" @tap="toEdit1(item)" v-if="item.audit_status != 1 && item.audit_status != 2">{{language.activities_pro.editor}}</view>
                    <view class="btn" v-if="item.audit_status == 0" @tap="reqSH(item, 1)">{{language.activities_pro.submit}}</view>
                </view>
            </view>

            <view
                class="btnBox"
                v-if="tabIndex == 0 && show && list.length"
                @click="navTo('/pagesA/myStore/activities_add?id=' + id + '&type=' + type + '&platform_activities_id=' + id)"
            >
                <view class="btn">{{language.activities_pro.addGoods}}</view>
            </view>

            <div class="noFindDiv" v-if="!list.length && tabIndex == 0 && load">
                <div><img :src="noCol" class="noFindImg" /></div>
                <span class="noFindText">{{language.activities_pro.nogoods}}</span>
                <div class="noFindButton" v-if="show" @click="navTo('/pagesA/myStore/activities_add?id=' + id + '&type=' + type + '&platform_activities_id=' + id)">
                    {{language.activities_pro.addGoods}}
                </div>
            </div>
            <div class="noFindDiv" v-if="!list.length && tabIndex == 1 && load">
                <div><img :src="noCol" class="noFindImg" /></div>
                <span class="noFindText">{{language.activities_pro.nogoods1}}</span>
                <div class="noFindButton" v-if="show" @click="navTo('/pagesA/myStore/activities_add?id=' + id + '&type=' + type + '&platform_activities_id=' + id)">
                    {{language.activities_pro.addGoods}}
                </div>
            </div>
        </view>

        <div class="kucun-mask">
            <div class="mask" v-if="display" @touchmove.stop.prevent>
                <div class="mask_content">
                    <div class="title">{{language.activities_pro.addInventory}}</div>
                    <input
                        type="text"
                        v-model="act_num"
                        :placeholder="language.activities_pro.addInventoryPlaceholder"
                        placeholder-class="input-placeholder"
                        placeholder-style="color: #B8B8B8;font-weight: 500;font-size: 28rpx;padding-left: 21rpx"
                    />
                    <div class="total">
                        <img :src="tishi" alt="" />
                        <p>{{language.activities_pro.totalInventory}} {{ num1 }}</p>
                    </div>
                    <div class="btns">
                        <div class="cancel" @click="cancel()">{{language.activities_pro.cancel}}</div>
                        <div class="confirm" @click="confirm()">{{language.activities_pro.confirm}}</div>
                    </div>
                </div>
            </div>
        </div>
    </view>
</template>

<script>
export default {
    data() {
        return {
            load: false,
            title: '活动商品',
            display: false,
            id: '',
            list: [],
            tabIndex: 0,
            type: '',
            show: false,
            tishi: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/tishi.png',
            noCol: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/lALPDgtYusvs3aLM-szc_220_250.png',
            num: 0,
            num1: '',
            pro: {},
            act_num: '',
            price: '',
            product_id: ''
        };
    },
    watch: {
        tabIndex(val) {
            this.load = false;
            this.axios();
        }
    },
    onLoad(option) {
        this.id = option.id;
        this.type = option.type;
    },
    onShow() {
        this.isLogin(async () => {
            await this.getDetail();
            await this.axios();
            if (this.tabIndex == 0) {
                let now = new Date().getTime();
                let end = new Date(this.pro.join_endtime).getTime();
                this.show = now < end;
            }
        });
    },
    methods: {
        changeLoginStatus () {
            
        },
        toEdit1(pro) {
            uni.setStorageSync('proObject', pro);
            uni.navigateTo({
                url: '/pagesA/myStore/activities_set?edit=true&type=' + this.type
            });
        },
        async getDetail() {
            let postData = {
                api: 'app.platform_activities.activity_details',
                activityType: this.type,
                id: this.id
            };
            let { code, data, message } = await this.$req.post({ data: postData });
            if (code == 200) {
                this.pro = data[0];
            } else {
                uni.showToast({
                    title: message,
                    icon: 'none'
                });
            }
        },
        async confirm() {
            if (!this.act_num) {
                uni.showToast({
                    title: this.language.activities_pro.inventoryTips,
                    icon: 'none'
                });
                return false;
            }
            
            if ( (Number.parseInt(this.num) + Number.parseInt(this.act_num)) > this.num1) {
                uni.showToast({
                    title: this.language.activities_pro.addinventoryTips,
                    icon: 'none'
                });
                return false;
            }
            
            let data = {
                api: 'app.platform_activities.add_num',
                id: this.product_id,
                activityType: this.type,
                num: Number.parseInt(this.act_num) 
            };

            let { code, message } = await this.$req.post({ data });
            uni.showToast({
                title: message,
                icon: 'none'
            });
            if (code == 200) {
                this.load = false;
                this.display = false;
                this.axios();
            }
        },
        cancel() {
            this.act_num = '';
            this.display = false;
        },
        delPro(pro) {
            let data = {
                api: 'app.platform_activities.del_pro',
                activityType: this.type,
                id: pro.id
            };
            this.$req
                .post({
                    data
                })
                .then(res => {
                    let { code, pro, message } = res;
                    uni.showToast({
                        title: message,
                        icon: 'none'
                    });
                    if (code == 200) {
                        this.axios();
                    }
                });
        },
        reqSH(pro, type) {
            let data = {
                api: 'app.platform_activities.modify_pro',
                id: pro.id,
                activityType: this.type,
                type
            };
            this.$req
                .post({
                    data
                })
                .then(res => {
                    let { code, pro, message } = res;
                    uni.showToast({
                        title: message,
                        icon: 'none'
                    });
                    if (code == 200) {
                        this.axios();
                    }
                });
        },
        toEdit(pro) {
            this.product_id = pro.id;
            this.num1 = pro.num1;
            this.num = pro.num;
            this.price = pro.price;
            this.display = true;
        },
        async axios() {
            let data = {
                api: 'app.platform_activities.pro_list',
                id: this.id,
                type: this.tabIndex,
                activityType: this.type,
                mch_id: this.$store.state.shop_id ? this.$store.state.shop_id : uni.getStorageSync('shop_id')
            };

            let res = await this.$req.post({ data });
            let { code, pro, message } = res.data;
            if (code == 200) {
                
                this.list = pro;
                this.load = true;
            } else {
                uni.showToast({
                    title: message,
                    icon: 'none'
                });
            }
        }
    }
};
</script>

<style scoped lang="less">
@import url("@/laike.less");
@import url('../../static/css/myStore/activities_pro.less');
</style>
