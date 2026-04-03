<template>
    <div class="store-container" style="min-height: 100vh;">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="language.myStore.verificationManagement.title" :bgColor="bgColor" :titleColor="'#333333'"
            :ishead_w="2"></heads>
        <toload :load="loadFlag">
            <div class="store-item" v-for="(item, index) in shop_list" :key="index">
                <div class="hxBox">
                    <div class="hxData">
                        {{language.myStore.verificationManagement.hxrq}}:
                        {{ item.startDate }}
                        {{language.myStore.verificationManagement.zhi}}
                        {{ item.endDate }}
                    </div>
                    <!-- @tap="edit(item)" -->
                    <div class="hxEdit" @tap="remove(item)">
                        {{language.myStore.verificationManagement.sc}}
                    </div>
                </div>
                <div class="hxBox">
                    <div class="hxTime">
                        {{language.myStore.verificationManagement.hxsd}}:
                        {{ item.startTime }} ~ {{ item.endTime }}
                    </div>
                    <div class="hxTime">
                        {{language.myStore.verificationManagement.hxsx}}:
                        {{ item.num == 0?language.myStore.verificationManagement.bxz:item.num }}
                    </div>
                </div>
            </div>

            <div v-if="shop_list.length == 0" class="add-store">
                <div class='noStore'>
                    <img :src="noOrder" />
                    <span>{{language.myStore.verificationManagement.tips}}～</span>
                </div>

                <div class="toAddDiv" @tap="add()"><span
                        class="toAdd">{{language.myStore.verificationManagement.btn}}</span>
                </div>
            </div>

            <div style="width:750rpx;height:222rpx ;"></div>

            <div v-if="shop_list.length > 0" class="storeList-bottom">
                <button @tap="add()">{{language.myStore.verificationManagement.btn}}</button>
            </div>
            <view class="xieyi mask" style="background-color: initial;" v-if="is_sus">
                <view style="width: 272rpx;height: 272rpx;background-color: rgba(51, 51, 51, .9);">
                    <view style="margin: 32rpx 0;text-align: center;margin-top: 64rpx;">
                        <image style="width: 68rpx;height: 68rpx;" :src="sus"></image>
                    </view>
                    <view class="xieyi_title"
                        style="margin-bottom: 0;margin-top: 0;color: #fff;font-weight: 500;font-size: 32rpx;">
                        {{ is_text }}
                    </view>
                </view>
            </view>
        </toload>
    </div>
</template>

<script>
    export default {
        data() {
            return {
                bgColor: [{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                loding: false,
                shop_id: '',
                shop_list: [],
                storeEdit: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/storeEdit.png',
                noOrder: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/noOrder.png',
                loadFlag: false,
                id: '',
                store_id: '',
                pageNo: 1,
                loadingType: 0,
                isStore: '', //是否是从店铺主页跳转
                is_sus: false,
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
            };
        },
        onLoad(option) {
            if (option.shop_id) {
                this.shop_id = option.shop_id;
            } else {
                this.shop_id = this.$store.state.shop_id;
            }

            if (option.mch_store_id) {
                this.id = option.mch_store_id
            }

        },
        onShow() {
            this.clearType()
            this.$nextTick(() => {
                if (!this.loding) {
                    uni.showLoading({
                        title: this.language.storeList.Tips[0],
                        mask: true
                    });
                }
                this._axios();
            });
        },
        onReachBottom() {
            if (this.loadingType == 1 || this.loadingType == 2) {
                return false
            }
            this.loadingType = 1;
            this.pageNo++;
            this._axios()
        },
        methods: {
            //清除状态初始化
            clearType() {
                this.pageNo = 1
                this.loadingType = 0
            },
            changeLoginStatus() {
                this._axios();
            },
            _navigateTo(url) {
                uni.navigateTo({
                    url
                });
            },
            _axios() {
                var data = {
                    api: 'mch.App.Mch.getAppointmenTime',
                    mch_id: this.shop_id,
                    mchStoreId: this.id,
                    pageNo: this.pageNo,
                };

                this.$req.post({
                    data
                }).then(res => {
                    uni.hideLoading();
                    if (res.code == 200) {
                        if (this.pageNo == 1) {
                            this.shop_list = res.data.list;
                        } else {
                            this.shop_list.push(...res.data.list)
                        }
                        if (res.data.list.length > 0) {
                            this.loadingType = 0
                        } else {
                            this.loadingType = 2
                        }
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        });
                    }

                    this.loadFlag = true;
                });
            },
            add() {
                this._navigateTo('/pagesB/myStore/addVerification?mch_store_id=' + this.id + '&shop_id=' + this
                .shop_id);
            },
            // 编辑
            edit(item) {
                this._navigateTo('/pagesB/myStore/addVerification?item=' + encodeURIComponent(JSON.stringify(item)) +
                    '&shop_id=' + this.shop_id + '&mch_store_id=' + this.id);
            },
            // 删除
            remove(row) {
                if(row.notDelete == 0){
                        let me = this;
                        var data = {
                            api:"mch.App.Mch.deleteAppointmenTime",
                            id: row.id,
                        };
                        uni.showModal({
                            title: this.language.myStore.verificationManagement.ts,
                            confirmText: this.language.showModal.confirm,
                            cancelText: this.language.showModal.cancel,
                            content: this.language.myStore.verificationManagement.sfscghxsj,
                            confirmColor: "#D73B48",
                            success: function (res) {
                                if (res.confirm) {
                                    me.$req.post({ data }).then((res) => {
                                        me.is_text = me.language.myStore.verificationManagement.sccg
                                        me.is_sus = true
                                        me.clearType()
                                        setTimeout(() => {
                                            me.is_sus = false
                                            me._axios()
                                        }, 1500);
                                    });
                                } else if (res.cancel) {
                                }
                            },
                        });
                }else{
                  uni.showModal({
                      title: this.language.myStore.verificationManagement.ts,
                      confirmText: this.language.myStore.verificationManagement.ckdd,
                      cancelText: this.language.showModal.cancel,
                      content: this.language.myStore.verificationManagement.text1,
                      confirmColor: "#D73B48",
                      success: function (res) {
                          if (res.confirm) {
                              uni.navigateTo({
                                  url: '/pagesA/myStore/myOrder?status=2&orderType_id=1'
                              });
                          } else if (res.cancel) {
                          }
                      },
                  });  
                }
            },
        }
    };
</script>
<style>
    page {
        background-color: #f4f5f6;
    }
</style>
<style scoped lang="less">
    @import url("@/laike.less");

    .store-container {
        background: #F6F6F6;
        // background: rgba(249, 249, 249, 1)
    }

    .store-item-disc {
        margin-top: 24rpx !important;
    }

    .store-item {
        display: flex;
        flex-direction: column;
        width: 100vw;
        // min-height: 226rpx;
        background: rgba(255, 255, 255, 1);
        box-sizing: border-box;
        padding: 32rpx;
        font-size: 32rpx;

        font-weight: 400;
        color: #333333;
        margin-bottom: 16rpx;
        border-radius: 24rpx;

        .hxBox {
            display: flex;
            align-items: center;
            justify-content: space-between;

            .hxData {
                font-size: 32rpx;
                color: #333333;
            }

            .hxEdit {
                font-size: 24rpx;
                color: #FA5151;
            }

            .hxTime {
                font-size: 28rpx;
                color: #666666;
            }

            &:first-child {
                margin-bottom: 16rpx;
            }
        }

        &:first-child {
            border-radius: 0 0 24rpx 24rpx;
        }

        &-disc {
            margin-top: 16rpx;
            text-overflow: -o-ellipsis-lastline;
            overflow: hidden;
            text-overflow: ellipsis;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            line-clamp: 2;
            -webkit-box-orient: vertical;
        }

        &-bottom {
            margin-top: 24rpx;

            .business_hours {
                margin-bottom: 16rpx;
            }
        }
    }


    .xieyi {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background-color: rgba(0, 0, 0, 0.5);
        z-index: 99;
        display: flex;
        justify-content: center;
        align-items: center;

        >view {
            width: 640rpx;
            min-height: 100rpx;
            max-height: 486rpx;
            background: #ffffff;
            border-radius: 24rpx;

            .xieyi_btm {
                height: 108rpx;
                color: @D73B48;
                display: flex;
                justify-content: center;
                align-items: center;
                border-top: 0.5px solid rgba(0, 0, 0, 0.1);
                font-weight: bold;
                font-size: 32rpx;
            }

            .xieyi_title {
                display: flex;
                justify-content: center;
                align-items: center;
                font-size: 32rpx;

                font-weight: 500;
                color: #333333;
                line-height: 44rpx;
                margin-top: 64rpx;
                margin-bottom: 32rpx;
                font-weight: bold;
                font-size: 32rpx;
            }

            .xieyi_text {
                width: 100%;
                max-height: 236rpx;
                overflow-y: scroll;
                padding: 0 32rpx;
                box-sizing: border-box;
            }
        }
    }

    .add-store {
        width: 751rpx;
        text-align: center;

        img {
            width: 751rpx;
            height: 400rpx;
        }

        .noStore {

            padding-top: 104rpx;
            font-size: 28rpx;

            font-weight: 400;
            height: 668rpx;
            background: #FFFFFF;
            border-radius: 0px 0px 24rpx 24rpx;
            color: #999999;

            span {
                margin-top: -20rpx;
                font-size: 28rpx;

                font-weight: 400;
                color: #333333;
                display: block;
            }
        }

        .toAddDiv {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 686rpx;
            height: 92rpx;
            font-size: 32rpx;
            color: #fff;
            text-align: center;
            line-height: 92rpx;
            border-radius: 52rpx;
            margin: 104rpx auto;
            padding: 0;
            .solidBtn()
        }
    }


    .flex1 {
        flex: 1;
    }

    .quanxuan-icon {
        width: 34rpx;
        height: 34rpx;
        vertical-align: middle;
    }

    .storeList-bottom {
        position: fixed;
        bottom: 0;
        z-index: 999;
        width: 100%;
        min-height: 124rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #ffffff;
        box-shadow: 0 0 0 0 rgba(0, 0, 0, 0.2);
        padding-bottom: constant(safe-area-inset-bottom);
        /* 兼容 iOS < 11.2 */
        padding-bottom: env(safe-area-inset-bottom);

        button {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 686rpx;
            height: 92rpx;
            font-size: 32rpx;
            border-radius: 52rpx;
            color: #fff;
            text-align: center;
            line-height: 92rpx;
            margin: 16rpx auto;
            border: none !important;
            padding: 0;
            .solidBtn()
        }
    }
</style>
