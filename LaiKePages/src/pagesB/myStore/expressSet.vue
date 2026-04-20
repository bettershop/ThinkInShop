<template>
    <div class="store-container" :style="shop_css">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="language.myStore.express.mdsz" :bgColor="bgColor" :titleColor="'#333333'" :ishead_w="2"></heads>
        <toload :load="loadFlag">
            <div class="store-item" v-for="(item, index) in shop_list" :key="index" >
                <div class="store-item-top">
                    <p>{{ item.kuaidi_name }}({{item.type}})</p>
                    <template>
                        <div @tap.stop="edit(item.id, item.express_id)">
                            {{language.storeList.edit}}
                        </div>
                    </template>
                </div>
                <div class="store-item-bottom">
                    <div class="business_hours">
                        <view>partnerId: {{ item.partnerId }}</view>
                    </div>
                </div>
            </div>
            <div v-if="shop_list.length == 0" class="add-store">
                <div class='noStore'>
                    <img :src="dizhiNo" />
                    <span>{{language.myStore.express.zsmymd}}</span>
                </div>
                <div class="toAddDiv" @tap="_navigateTo('/pagesB/myStore/expressAdd?type=1')"><span class="toAdd">{{language.myStore.expressAdd.tjsz}}</span>
                </div>
            </div>
            <div style="height: 120rpx;"></div>
            <div v-if="shop_list.length > 0" class="storeList-btn">
                <view class="storeList-bottom" @tap="_navigateTo('/pagesB/myStore/expressAdd?type=1')">{{language.myStore.expressAdd.tjsz}}</view>
            </div>
        </toload>
    </div>
</template>

<script>
    export default {
        data() {
            return {
                returnR: '',
                back: '',

                bgColor:[{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                quan_ho: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/new_red_select.png',
                Call_phone: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/Call_phone.png',
                xuanzhong_jl: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xuanzhong_jl.png',
                list: false,
                shop_id: '',
                shop_list: [],
                shop_css: '',
                storeEdit: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/storeEdit.png',
                quan_hui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/xuanzehui2x.png',
                quan_hei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/xuanzehei2x.png',
                dizhiNo: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/dizhiNo.png',

                loadFlag: false,

                store_id: '',
                isStore: '', //是否是从店铺主页跳转
            };
        },
        onLoad(option) {
            if (option.shop_id) {
                this.shop_id = option.shop_id;
            } else {
                this.shop_id = this.$store.state.shop_id;
            }
            if(option.isStore){
                this.isStore = option.isStore
            }
        },
        onShow() {
            
            this.$nextTick(() => {

                if (!this.list) {
                    uni.showLoading({
                        title: this.language.storeList.Tips[0],
                        mask: true
                    });
                }
                this._axios();
            });
        },
        methods: {

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
                    api:"mch.App.Mch.logistics_list",
                    page: 1,
                    pageSize: 9999,
                };
                this.$req.post({ data }).then(res => {
                    uni.hideLoading();
                    if (res.code == 200) {
                        this.shop_list = res.data.list;
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
          
            
            // 编辑
            edit(id, express_id) {
                this._navigateTo('/pagesB/myStore/expressAdd?id='+id + '&express_id=' + express_id);
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
    }

    .store-item-disc{
        margin-top: 0 !important;
    }
    .store-item {
        display: flex;
        position: relative;
        flex-direction: column;
        width: 100vw;
        background: rgba(255, 255, 255, 1);
        box-sizing: border-box;
        padding: 32rpx;
        font-size: 28rpx;
        color: #444444;
        margin-bottom: 16rpx;
        border-radius: 24rpx;
        &:first-child{
            border-radius: 0 0 24rpx 24rpx;
        }
        &-top{
            display: flex;
            align-items: center;
            
            p{
                font-size: 40rpx;
                
                font-weight: 500;
                color: #333333;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
                max-width: 460rpx;
            }
            
            >span{
                .center();
                width: 64rpx;
                height: 40rpx;
                background: @priceColor;
                border-radius:8rpx;
                color: #FFFFFF;
                font-size: 20rpx;
                margin-right: 16rpx;
            }
            
            div{
                margin-left: auto;
                .center();
                .size(28rpx);
                color: #D73B48;
                
                img{
                    width: 28rpx;
                    height: 28rpx;
                    margin-right: 10rpx;
                }
            }
        }
        
        &-disc{
            font-size: 28rpx;
            line-height: 32rpx;
            color: #999999;
            margin-top: 24rpx;
            text-overflow: -o-ellipsis-lastline;
            overflow: hidden;
            text-overflow: ellipsis;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            line-clamp: 2;
            -webkit-box-orient: vertical;
        }
        
        &-bottom{
            color: #999999;
            .size(28rpx);
            margin-top: 24rpx;
            .business_hours{
                display: flex;
                justify-content: space-between;
                margin-bottom: 16rpx;
                font-size: 28rpx;
                
                font-weight: 400;
            }
        }
    }

    
    .add-store {
        width: 750rpx;
        text-align: center;
        img{
            width: 750rpx;
            height:460rpx;
        }
        .noStore{
            
            padding-top: 104rpx;
            font-size: 28rpx;
            
            font-weight: 400;
            height: 668rpx;
            background: #FFFFFF;
            border-radius: 0px 0px 24rpx 24rpx;
            color: #999999;
            span{
                margin-top: -60rpx;
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
            padding:0;
            .solidBtn()
            
            
        }
    }
    
    
    .flex1 {
        flex: 1;
    }

    
    .storeList-btn{
           position: fixed;
           bottom: 0;
           width: 100%;
           min-height: 124rpx;
           display: flex;
           align-items: center;
           justify-content: center;
           background-color: #ffffff;
           box-shadow: 0 0 0 0 rgba(0,0,0,0.2);
           padding-bottom: constant(safe-area-inset-bottom); /* 兼容 iOS < 11.2 */
           padding-bottom: env(safe-area-inset-bottom);
            .storeList-bottom {
                width: 686rpx;
                height: 92rpx;
                font-size: 32rpx;
                color: #fff;
                text-align: center;
                line-height: 92rpx;
                border-radius: 52rpx;
                margin: 16rpx auto;
                padding:0;
                .solidBtn()
            }
        }
    
</style>
