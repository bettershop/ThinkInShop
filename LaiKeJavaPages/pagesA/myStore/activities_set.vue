<template>
    <view class="container">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="title" :returnR="returnR"></heads>
        <template v-if="!edit">
            <view class="proBox" v-for="(item, index) in list" :key="index">
                <view class="pro">
                    <image class="image" :src="item.img"></image>
                    <view style="flex: 1;overflow: hidden;">
                        <view class="proTitle">{{ item.name }}</view>

                        <view class="proSize">{{language.activities_set.size}} {{ item.attr }}</view>

                        <view class="priceBox">
                            <view class="price">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(item.price) }}</view>
                            <view class="size">{{language.activities_set.kucun}} {{ item.num }}</view>
                        </view>
                    </view>
                </view>
                
                <view class="proIpt">
                    <view class="text">{{language.activities_set.activeKucun}}</view>
                    <input class="ipt" type="number" min="1" :max="item.num" :placeholder="language.activities_set.activeKucunPlaceholder" v-model="item.act_num" />
                </view>
            </view>

            <view class="btnBox">
                <view class="btn" @tap="request(true)">{{language.activities_set.submit}}</view>
            </view>
        </template>

        <template v-else>
            <view class="proBox">
                <view class="pro">
                    <image class="image" :src="list.img"></image>
                    <view style="flex: 1;overflow: hidden;">
                        <view class="proTitle">{{ list.group_title }}</view>

                        <view class="proSize">{{language.activities_set.size}} {{ list.names }}</view>

                        <view class="priceBox">
                            <view class="price">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(list.costprice) }}</view>
                            <view class="size">{{language.activities_set.kucun}} {{ list.num1 }}</view>
                        </view>
                    </view>
                </view>
                

                <view class="proIpt">
                    <view class="text fw500">{{language.activities_set.activeKucun}}</view>
                    <input class="ipt" type="number" min="1" :max="list.num1" :placeholder="language.activities_set.activeKucunPlaceholder" v-model="list.num" />
                </view>
                <view class="proIpt">
                    <img :src="tishi" alt="">
                    <view class="text total">{{language.activities_set.totalInventory}}{{ list.num1 }}</view>
                </view>
            </view>

            <view class="btnBox"><view class="btn" @tap="request(true)">{{btnText}}</view></view>
        </template>
    </view>
</template>

<script>
export default {
    data() {
        return {
            title: '活动设置',
            platform_activities_id: '',
            list: [],
            id: '',
            edit: false,
            type: '',
            returnR: 0,
            tishi: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/tishi.png',
            btnText: '',
        };
    },
    computed: {
        reqData() {
            var arr = [];
            if (!this.edit) {
                this.list.filter(item => {
                    arr.push({
                        id: item.attr_id,
                        price: item.act_price,
                        num: item.act_num,
                        pid: item.id
                    });
                });
            }
            return arr;
        }
    },
    onLoad(option) {
        
        this.type = option.type;

        if (option.edit) {
            this.list = uni.getStorageSync('proObject');
            this.edit = true;
            uni.removeStorageSync('proObject');
            return;
        }

        this.id = option.id;
        this.platform_activities_id = option.platform_activities_id;
        

        this.list = uni.getStorageSync('checkProList');

        uni.removeStorageSync('checkProList');
    },
    onShow() {
        this.tishi = this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/tishi.png'
        if (this.edit) {
            this.title = this.language.activities_set.title_edit;
            this.btnText = this.language.activities_pro.submit
            return;
        } else {
            this.btnText = this.language.activities_pro.submit1
        }
        
        if (this.type == 'pt') {
            this.title = this.language.activities_set.title_pt;
        } else if (this.type == 'ms') {
            this.title = this.language.activities_set.title_ms;
        }else{
            this.title = this.language.activities_set.title;
        }
    },
    methods: {
		changeLoginStatus(){},
        request(flag) {
            if (this.edit) {
                let maxNum = this.list.num1
                let act_num = this.list.num

                if (Number.parseInt(act_num) > Number.parseInt(maxNum)) {
                    uni.showToast({
                        title: this.language.activities_set.error1,
                        icon: 'none'
                    });
                    return;
                }
                
                let data = {
                    api: 'app.platform_activities.modify_pro',
                    id: this.list.id,
                    type: 1,
                    activityType: this.type,
                    platform_activities_id: this.platform_activities_id,
                    num: this.list.num,
                    price: this.list.price
                };
                this.$req
                    .post({
                        data
                    })
                    .then(res => {
                        let { code, message } = res;
                        uni.showToast({
                            title: message,
                            icon: 'none'
                        });
                        if (code == 200) {
                            uni.navigateBack({
                                delta: 1
                            });

                            
                        }
                    });
                return;
            }

            let maxNum = this.list[0].num
            let act_num = this.reqData[0].num
            
            if (Number.parseInt(act_num) > Number.parseInt(maxNum)) {
                uni.showToast({
                    title: this.language.activities_set.error1,
                    icon: 'none'
                });
                return;
            }
            
            let data = {
                module: 'app',
                action: 'platform_activities',
                m: this.type == 'pt' ? 'add_pt_pro' : 'add_ms_pro',
                activityType: this.type, 
                mch_id: this.$store.state.shop_id || uni.getStorageSync('shop_id'),
                id: this.id,
                platform_activities_id: this.platform_activities_id,
                data: JSON.stringify(this.reqData)
            };

            if (flag) {
                data.audit_status = 1;
            } else {
                data.audit_status = 0;
            }

            this.$req.post({ data }).then(res => {
                let { code, pro, message, attr } = res;
                if (code == 200) {
                    uni.showToast({
                        title: this.language.activities_set.success,
                        icon: 'none'
                    });
                    uni.redirectTo({
                        url: '/pagesA/myStore/activities_pro?id=' +  this.id + '&type=' + this.type
                    })
                } else {
                    uni.showToast({
                        title: message ? message : res.status,
                        icon: 'none'
                    });
                }
            });
        }
    }
};
</script>

<style scoped lang="less">
@import url("@/laike.less");
.container {
    background-color: #f6f6f6;
    min-height: 100vh;
    padding-bottom: 100rpx;
    box-sizing: border-box;

    .proBox {
        padding-left: 30rpx;
        background-color: #ffffff;
        margin-bottom: 20rpx;

        .pro {
            display: flex;
            align-items: center;
            padding: 30rpx 30rpx 30rpx 0;
            background-color: #ffffff;
            border-bottom: 1px solid #e6e6e6;

            .image {
                width: 120rpx;
                height: 120rpx;
                margin-right: 24rpx;
            }

            .proTitle {
                color: #020202;
                font-size: 26rpx;
                line-height: 26rpx;
                white-space: nowrap;
                font-weight: 500;
                overflow: hidden;
                text-overflow: ellipsis;
                
            }

            .proSize {
                color: #999999;
                font-size: 24rpx;
                line-height: 24rpx;
                margin-top: 20rpx;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
            }

            .priceBox {
                display: flex;
                align-items: center;
                margin-top: 30rpx;

                .price {
                    color: @priceColor;
                    font-size: 24rpx;
                    line-height: 24rpx;
                    font-weight: bold;
                }

                .size {
                    color: #999999;
                    font-size: 24rpx;
                    line-height: 24rpx;
                    margin-left: 60rpx;
                }
            }
        }

        .proIpt {
            display: flex;
            align-items: center;
            height: 80rpx;
            img {
                width: 24rpx;
                height: 24rpx;
                margin-left: 16rpx;
                margin-right: 4px;
            }

            .text {
                width: 160rpx;
                color: #020202;
                font-size: 26rpx;
                text-align: right;
                font-weight: 500;
                &.total {
                    width: 278rpx;
                    text-align: left;
                    color: #999999;
                    font-size: 22rpx;
                }
            }

            .ipt {
                flex: 1;
                font-size: 28rpx;
                padding-left: 10rpx;
                font-weight: 500;
            }
        }
    }

    .btnBox {
        position: fixed;
        bottom: 0;
        left: 0;
        right: 0;
        height: 98rpx;
        display: flex;
        font-size: 32rpx;

        .btn {
			.solidBtn();
            flex: 1;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
        }
    }
}
</style>
