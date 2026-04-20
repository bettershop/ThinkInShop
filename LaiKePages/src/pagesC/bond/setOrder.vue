<template>
    <view class='container'>
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="language.bond.setOrder.title" :border="true" :bgColor='bgColor' titleColor="#000" ishead_w="2" returnR="15"></heads>
        <view class="box">
            <view class="titlebox">
                <view class="titlebox-title">{{language.bond.setOrder.bysz}}</view>
                <view class="titlebox-img">
                    <picker @pickstart="showPickerClumn" mode=selector :value="reachNumber" @change="bindFreightChange"
                        :range="number"  class="picker">
                        <view class="container_row" style="border-bottom: 1px solid rgb(237, 237, 237);">
                            <view class="freight">
                                <view class="container_row_right">
                                    <view v-if="(reachNumber != ''||reachNumber==0)&&orderInfo.package_settings != '0' ">
                                        {{language.bond.setOrder.tjspm}}&nbsp;{{number[reachNumber]}}&nbsp;{{language.bond.setOrder.jby}}</view>
                                    <view v-else-if="reachNumber == '' " class="set">{{language.bond.setOrder.tjsp0}}</view>
                                    <view v-else>{{language.bond.setOrder.tjspm}}&nbsp;{{number[reachNumber]}}&nbsp;{{language.bond.setOrder.jby}}</view>
                                </view>
                                <image class="jiantou" :src="fanghui"></image>
                            </view>

                        </view>
                    </picker>
                </view>
            </view>

            <view class="titlebox">
                <view class="titlebox-title">{{language.bond.setOrder.ddby}}</view>
                <view class="titlebox-img">
                    <picker @pickstart="showPickerClumn" mode=selector :value="typeIndex" @change="bindPickerChange"
                        :range="typeArr"  class="picker">
                        <view class="container_row" style="border-bottom: 1px solid rgb(237, 237, 237);">
                            <view class="freight">
                                <view class="container_row_right">
                                    <view v-if="(typeIndex != ''||typeIndex == 0)&&orderInfo.package_settings != '0'">
                                        {{language.bond.setOrder.tyddm}}&nbsp;{{typeArr[typeIndex]}}&nbsp;{{language.bond.setOrder.jby}}</view>
                                    <view v-else-if="typeIndex == '' " class="set">{{language.bond.setOrder.ytdd0}}</view>
                                    <view v-else>{{language.bond.setOrder.tyddm}}&nbsp;{{typeArr[typeIndex]}}&nbsp;{{language.bond.setOrder.jby}}</view>
                                </view>
                                <image class="jiantou" :src="fanghui"></image>
                            </view>

                        </view>
                    </picker>
                </view>
            </view>
            <view class="xieyi" style="background-color: initial;" v-if="is_sus">
                <view style="width: 272rpx;height: 272rpx;background-color: rgba(51, 51, 51, .9);">
                    <view style="margin: 32rpx 0;text-align: center;margin-top: 64rpx;">
                        <image style="width: 68rpx;height: 68rpx;" :src="sus"></image>
                    </view>
                    <view class="xieyi_title"
                        style="margin-bottom: 0;margin-top: 0;color: #fff;font-weight: 500;font-size: 32rpx;">
                        {{language.bond.setOrder.szgc}}</view>
                </view>
            </view>
            <view class="tips">
                {{language.bond.setOrder.zydsz}}
            </view>
        </view>
        <view class="btn" @tap="btn()">{{language.bond.setOrder.btn}}</view>
    </view>
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
                is_sus: false,
                fanghui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jiantou2x.png',
                orderInfo:"",
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
                reachNumber: 0, //满多少件包邮
                typeIndex: 0,
                number: [0],
                typeArr:[0],
            }
        },
        computed: {

        },
        onLoad(option) {
            for (let i = 1; i < 2000; i++) {
                this.number.push(i)
                this.typeArr.push(i)
            }
            // this._axios()
        },
        onShow() {
            this.isLogin(() => {
                this._axios()
            })
        },
        methods: {
            showPickerClumn(e) {
            },
            bindFreightChange(e) {
                this.reachNumber = e.target.value
            },
            bindPickerChange(e) {
               
                this.typeIndex = e.target.value
            },
            btn(){
                
                var me = this
                var data = {
                    
                    api:"mch.App.Mch.MchSaveConfig",
                    isType: '1',
                    mchId: uni.getStorageSync('mch_id'),
                    samePiece:this.number[this.reachNumber],
                    sameOrder:this.typeArr[this.typeIndex],
                    packageSettings:this.number[this.reachNumber]==0&&this.typeArr[this.typeIndex]==0?'0':'1'
                }
                this.$req.post({
                    data
                }).then(res => {                    
                        
                        if(res.code==200){
                            this.is_sus = true
                            setTimeout(() => {
                                this.is_sus = false
                            }, 1500);
                            setTimeout(()=>{
                                this.navBack()
                            },1500)
                        }
                })
            },
            _axios() {
                var me = this
                var data = {
                    
                    api:"mch.App.Mch.MchIndex",
                    isType: '1',
                    mchId: uni.getStorageSync('mch_id')
                }
                this.$req.post({
                    data
                }).then(res => {
                    if (res.code == 200) {
                        this.orderInfo=res.data
                         this.reachNumber = res.data.same_piece
                         this.typeIndex = res.data.same_order
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1000,
                            icon: 'none'
                        })
                    }
                })
            }
        },
    }
</script>
<style>
	    page {
	        background-color: #f4f5f6;
	    }
</style>
<style lang="less" scoped>
    @import url("@/laike.less");
    .xieyi {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background-color: rgba(0, 0, 0, .5);
        z-index: 99;
        display: flex;
        justify-content: center;
        align-items: center;
    
        >view {
            width: 640rpx;
            min-height: 100rpx;
            max-height: 486rpx;
            background: #FFFFFF;
            border-radius: 24rpx;
    
            .xieyi_btm {
                height: 108rpx;
                color: @D73B48;
                display: flex;
                justify-content: center;
                align-items: center;
                border-top: 0.5px solid rgba(0, 0, 0, .1);
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
    .box {
        border-radius: 0 0 24rpx 24rpx;
        padding-top: 16rpx;
        padding-bottom: 32rpx;
        background-color: #fff;
        .tips{
            width: 686rpx;
            margin: 8rpx auto;
            height: 72rpx;
            line-height: 72rpx;
            background: #F4F5F6;
            border-radius: 16rpx;
            padding: 0 16rpx;
           font-size: 28rpx;
           
           font-weight: 400;
           color: #666666;
           box-sizing: border-box;
        }
    }

    .titlebox {
        width: 100%;
        border-radius: 0px 0px 24rpx 24rpx;

        display: flex;
        justify-content: space-between;
        align-items: center;
        color: #333333;
    }

    .titlebox-title {
        font-size: 32rpx;
        margin-left: 32rpx;
        
        font-weight: 400;
        color: #333333;
    }

    .titlebox-img {
        width: 498rpx;
        height: 76rpx;
        padding: 0 16rpx;
        margin: 16rpx 32rpx 16rpx 0;
        display: flex;
        align-items: center;
        border-radius: 16rpx;
        border: 1rpx solid rgba(0, 0, 0, 0.1);

        .picker {
            width: 100%;
        }

        .container_row {
            flex: 1;

            .freight {
                flex: 1;
                display: flex;
                align-items: center;
                min-height: 76rpx;

                .container_row_right {
                    flex: 1;
                    display: flex;
                    align-items: center;
                    font-size: 32rpx;

                    .set {
                        color: #999999;
                    }
                }

                .jiantou {
                    width: 32rpx;
                    height: 44rpx;
                    margin-left: 17rpx;
                }

            }
        }
    }

    .btn {
        text-align: center;
        margin: 104rpx auto 0;
        width: 686rpx !important;
        transform: translate(0) !important;
        height: 92rpx;
        line-height: 92rpx;
        background: linear-gradient(270deg, #FF6F6F 0%, #FA5151 100%);
        border-radius: 52rpx;
        color: #FFFFFF;
        font-size: 32rpx;
    }
</style>
