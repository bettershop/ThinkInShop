<template>
    <view class="container">
        <view class="container_top">
            <heads :title="language.activities_add.title"></heads>
            <productSelect :list1="list1" :list2="brand_class_list" @query="query"></productSelect>
        </view>

        <view class="proBox">
            <view class="pro" v-for="(item, index) in list" :key="index" @tap="checkChange(item)">
                <image class="xuanze" :src="item.checkMe ? xuanzehei : xuanzehui"></image>
                <image class="image" :src="item.img"></image>
                <view style="flex: 1;overflow: hidden;">
                    <view class="proTitle">{{ item.name }}</view>

                    <view class="proSize">{{language.activities_add.size}}{{ item.attr }}</view>

                    <view class="priceBox">
                        <view class="price">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(item.price) }}</view>
                        <view class="size">{{language.activities_add.kucun}}{{ item.num }}</view>
                    </view>
                </view>
            </view>
            <uni-load-more :loadingType="loadingType" v-if="list.length > 9"></uni-load-more>
        </view>

        <view class="mask_bg" v-if="classFlag || classFlag1 || brandFlag || proFlag" @tap="closeBg" @touchmove.stop.prevent></view>

        <view class="bottomBox">
            <view class="bottomBox_left">
                <view class="bottomBox_left_left" @tap="checkChange(false)">
                    <image class="xuanze" :src="qxFlag ? xuanzehei : xuanzehui" style="margin-right: 12rpx;"></image>
                    {{language.activities_add.checkAll}}
                </view>

                <view class="bottomBox_left_right">
                    {{language.activities_add.selected}}
                    <view class="bold" style="margin: 0 6rpx;">{{ checkNum }}</view>
                    {{language.activities_add.items}}
                </view>
            </view>

            <view class="bottomBox_right" @tap="toAdd">{{ btn_name }}</view>
        </view>
    </view>
</template>

<script>
import productSelect from './components/product-select'
export default {
    data() {
        return {
            title: '添加活动商品',
            
            id: '',
            list: [],
            classList: [],
            classList1: [],
            brandList: [],
            cid: '',
            cid1: '',
            cid2: '',
            brand_id: '',
            product_title: '',

            down: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/down.png',
            xuanzehui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xuanzehui2x.png',
            xuanzehei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xuanzehei2x.png',

            classFlag: false,
            classFlag1: false,
            brandFlag: false,
            proFlag: false,

            qxFlag: false,
            page: 1,
            loadingType: 0,
            btn_name: '活动设置',
            type: '',
            platform_activities_id: '',
            list1: [],
            brand_class_list: []
        };
    },
    components: {
        productSelect
    },
    onLoad(option) {
        this.id = option.id;
        this.platform_activities_id = option.platform_activities_id;
        this.type = option.type;
        
    },
    onShow() {
        if (this.type == 'pt') {
            this.btn_name = this.language.activities_add.btn_name_pt
        } else if (this.type == 'ms') {
            this.btn_name = this.language.activities_add.btn_name_ms
        }else{
            this.btn_name = this.language.activities_add.btn_name
        }

        this.list = [];
        this.loadingType = 0;
        this.page = 1;
        this.qxFlag = false;
        this.axios();
    },
    onReachBottom() {
        if (this.loadingType != 0) {
            return;
        }
        this.loadingType = 1;
        this.page++;
        this.axios();
    },
    computed: {
        checkNum() {
            var num = 0;
            this.list.filter(item => {
                if (item.checkMe) {
                    num++;
                }
            });
            return num;
        },
        checkList() {
            var arr = [];
            this.list.filter(item => {
                if (item.checkMe) {
                    item.act_price = parseFloat(Number(item.costprice) * 0.8);
                    item.act_num = parseInt(Number(item.num) * 0.1);
                    if (item.act_num < 1) {
                        item.act_num = 1;
                    }
                    arr.push(item);
                }
            });
            return arr;
        }
    },
    watch: {
        classFlag(val) {
            if (val) {
                this.classFlag1 = false;
                this.brandFlag = false;
                this.proFlag = false;
            }
        },
        classFlag1(val) {
            if (val) {
                this.classFlag = false;
                this.brandFlag = false;
                this.proFlag = false;
            }
        },
        brandFlag(val) {
            if (val) {
                this.classFlag1 = false;
                this.classFlag = false;
                this.proFlag = false;
            }
        },
        proFlag(val) {
            if (val) {
                this.classFlag1 = false;
                this.brandFlag = false;
                this.classFlag = false;
            }
        }
    },
    methods: {
        toAdd() {
            if (this.checkNum == 0) {
                uni.showToast({
                    title: this.language.activities_add.pleaseChoose,
                    icon: 'none'
                });
                return;
            }
            uni.setStorageSync('checkProList', this.checkList);
            uni.navigateTo({
                url: '/pagesA/myStore/activities_set?id=' + this.id + '&type=' + this.type + '&platform_activities_id=' + this.platform_activities_id
            });
        },
        queding() {
            this.proFlag = false;
            this.list = [];
            this.loadingType = 0;
            this.page = 1;
            this.qxFlag = false;
            this.axios();
        },
        quxiao() {
            this.proFlag = false;
            this.product_title = '';
        },
        changeBrand(item) {
            item.checkMe = !item.checkMe;
            if (item.checkMe) {
                this.brandList.filter(items => {
                    if (item.brand_id != items.brand_id) {
                        items.checkMe = false;
                    }
                });
                this.brand_id = item.brand_id;
            } else {
                this.brand_id = '';
            }
        },
        query (obj) {
            this.cid = obj.cid;
            this.brand_id = obj.brand_id;
            this.product_title = obj.product_title;
            this.list = [];
            this.loadingType = 0;
            this.page = 1;
            this.qxFlag = false;
            this.axios()
        },
        checkChange(item) {
            if (item) {
                item.checkMe = !item.checkMe;

                let qxFlag = true;
                this.list.filter(items => {
                    if (!items.checkMe) {
                        qxFlag = false;
                    }
                });

                this.qxFlag = qxFlag;
            } else {
                this.qxFlag = !this.qxFlag;

                this.list.filter(items => {
                    items.checkMe = this.qxFlag;
                });
            }
        },
        closeBg() {
            this.classFlag = false;
            this.classFlag1 = false;
            this.brandFlag = false;
            this.proFlag = false;
        },
        axios() {
            let data = {
                api: 'app.platform_activities.listdetail',
                activityType: this.type,
                platform_activities_id: this.platform_activities_id,
                mch_id: this.$store.state.shop_id ? this.$store.state.shop_id : uni.getStorageSync('shop_id'),
                page: this.page,
                cid: this.cid,
                brand_id: this.brand_id,
                product_title: this.product_title
            };
            this.$req.post({ data }).then(res => {
                let { code, pro, message, attr, brand_class_list } = res.data;
                if (code == 200) {
                    this.classList = attr;
                    attr.unshift({
                        cid: '',
                        pname: this.language.activities_add.classify,
                        res: []
                    })
                    this.list1 = attr;
                    
                    brand_class_list.unshift({
                        brand_id: '',
                        brand_name: this.language.activities_add.brand
                    })
                    this.brand_class_list = brand_class_list;

                    if (pro.length > 0) {
                        pro.filter(item => {
                            item.checkMe = false;
                        });
                        this.list.push(...pro);
                        this.qxFlag = false;
                    }

                    if (pro.length > 9) {
                        this.loadingType = 0;
                    } else {
                        this.loadingType = 2;
                    }
                } else {
                    uni.showToast({
                        title: message,
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

    &_top {
        position: sticky;
        top: 0;
        z-index: 999;

        .searhBox {
            display: flex;
            background: #ffffff;

            &_item {
                flex: 1;
                position: relative;
                display: flex;
                align-items: center;
                justify-content: center;
                height: 100rpx;
                border-bottom: 1px solid #eeeeee;
                box-sizing: border-box;

                .down {
                    width: 20rpx;
                    height: 10rpx;
                    margin-left: 14rpx;
                }

                &_list {
                    position: absolute;
                    top: 100rpx;
                    left: 0;
                    padding: 0 30rpx;
                    background-color: #ffffff;
                    max-height: 320rpx;
                    overflow-y: auto;

                    .li {
                        width: 300rpx;
                        height: 80rpx;
                        line-height: 80rpx;
                        padding: 0 10rpx;
                        box-sizing: border-box;
                    }

                    .active {
                        background-color: #eeeeee;
                    }
                }

                &_proName {
                    position: absolute;
                    top: 100rpx;
                    right: 0;
                    padding: 0 30rpx;
                    background-color: #ffffff;
                    height: 302rpx;
                    width: 100vw;
                    box-sizing: border-box;

                    .title {
                        padding: 27rpx 0 20rpx;
                        font-size: 26rpx;
                        color: #666666;
                        line-height: 26rpx;
                    }

                    .proName {
                        width: 100%;
                        height: 70rpx;
                        border: 1px solid rgba(230, 230, 230, 1);
                        border-radius: 6rpx;
                        box-sizing: border-box;
                        padding: 0 20rpx;
                        font-size: 24rpx;
                    }

                    .btnBox {
                        display: flex;
                        align-items: center;
                        justify-content: space-between;
                        height: 120rpx;
                        margin-top: 40rpx;

                        .leftBtn,
                        .rightBtn {
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            width: 330rpx;
                            height: 80rpx;
                            border-radius: 8rpx;
                            font-size: 28rpx;
                        }

                        .leftBtn {
                            .outBtn();
                        }

                        .rightBtn {
                            .solidBtn();
                        }
                    }
                }
            }
        }
    }

    .proBox {
        padding: 104rpx 0 20rpx;

        .pro {
            display: flex;
            align-items: center;
            padding: 30rpx;
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
                    color: #ff0000;
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
    }

    .mask_bg {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background-color: rgba(0, 0, 0, 0.4);
        z-index: 101;
    }

    .bottomBox {
        display: flex;
        position: fixed;
        bottom: 0;
        left: 0;
        right: 0;
        height: 98rpx;
        background-color: #ffffff;
        z-index: 99;

        &_left {
            flex: 1;
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 0 30rpx;
            border-top: 1px solid #e6e6e6;

            &_left {
                display: flex;
                align-items: center;
                height: 100%;
                width: 200rpx;
            }

            &_right {
                display: flex;
                align-items: center;
                font-size: 28rpx;
                color: #666666;

                .bold {
                    color: #ff0000;
                    font-weight: bold;
                }
            }
        }

        &_right {
            width: 240rpx;
            height: 98rpx;
            box-sizing: border-box;
            font-size: 30rpx;
			.solidBtn();
            .center();
        }
    }

    .xuanze {
        width: 34rpx;
        height: 34rpx;
        margin-right: 30rpx;
    }
    
    .searhBox_item,.bottomBox_left_left{
        font-size: 32rpx;
    }
    
}
</style>
