<template>
    <div class="store-container" :style="shop_css">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="'查看商品'" :bgColor="bgColor" :titleColor="'#333333'" :ishead_w="2"></heads>
        <toload :load="loadFlag">
                <!-- @tap="choose(item.id)"--点击跳转 -->
            <div class="store-item" v-for="(item, index) in shop_list" :key="index" >
                <div class="store-item_d">
                    <img :src="item.img" alt="" />
                    <div style="width: calc(100% - 84px)">
                        <div class="store-item-top">
                            
                            <p>{{ item.p_name }}</p>
                            
                            
                        </div>
                        <div class="store-item-bottom">
                            <div class="store-item-bottom_d" >
                                <view>{{ item.attribute }}</view>
                                <view>数量: {{ item.num }}</view>
                            </div>
                        </div>
                    </div>
                </div>
                
                
            </div>
            <div v-if="shop_list.length == 0" class="add-store">
                <div class='noStore'>
                    <img :src="dizhiNo" />
                    <span>{{language.myStore.express.zsmymd}}</span>
                </div>
                
            </div>

            <div style="height: 120rpx;"></div>

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
                id: '', //是否是从店铺主页跳转
            };
        },
        onLoad(option) {
            if (option.shop_id) {
                this.shop_id = option.shop_id;
            } else {
                this.shop_id = this.$store.state.shop_id;
            }

            this.id = option.id
        
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
                       
                        api:"mch.App.Mch.getPro",
                        id: this.id
                    };
                

                this.$req.post({ data }).then(res => {
                    uni.hideLoading();
                    if (res.code == 200) {
                        
                        this.shop_list = res.data.list;
                        if (this.shop_list) {
                            this.shop_css = 'min-height:100vh';
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
        .store-item_d{
            display: flex;
        }
        .store-item-top{
            height:90rpx;
            display:block;
        }
        img{
            width: 152rpx;
            height: 152rpx;
            border-radius: 16rpx;
            margin-right: 16rpx;
        }
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
        
    }
    
    
    .flex1 {
        flex: 1;
    }
    .store-item-bottom_d{
        display: flex;
        justify-content: space-between;
    }
    
</style>
