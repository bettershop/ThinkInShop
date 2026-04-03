<template>
    <div class="store-container" :style="shop_css">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="title" :bgColor="bgColor" :titleColor="'#333333'" :ishead_w="2"></heads>
        <toload :load="loadFlag">
            <div class="store-item" v-for="(item, index) in shop_list" :key="index" @tap="choseStore(item)">
                <div class="store-item-top">
                   <span class="default1Slot" v-if="user_status == 1 && item.is_default == 1&&!isStore">{{language.freight.default1}}</span>
                    <p>{{ item.name }}</p>
                    <template v-if="!isStore">
                        <div v-if="user_status == 1" @tap.stop="edit(item.id)">
                            <!-- 编辑图标 -->
                            {{language.storeList.edit}}
                        </div>
                        <div v-else @tap.stop="choose(item.id)"></div>
                    </template>
                </div>
                <div class="store-item-bottom">
                    <div class="business_hours">
                        <view v-if="!isStore">{{language.storeList.phone}}: {{ item.mobile }}</view>
                        <view>{{language.storeList.Business_Hours}} {{ item.business_hours }}</view>
                        <image @click.stop="callClick(item.mobile)" class="callimg" :src="Call_phone" v-if="isStore"></image>
                    </div>
                </div>
                <p class="store-item-disc">{{ item.sheng }}{{ item.shi }}{{ item.xian }}{{ item.address }}</p>
               <image v-if="item.checked"  :src="xuanzhong_jl" class="xuanzhong" style="width: 64rpx;height: 64rpx;"></image>
            </div>
            <div v-if="user_status == 1 && shop_list.length == 0" class="add-store">
                <div class='noStore'>
                    <img :src="dizhiNo" />
                    <span>{{language.storeList.nhmy}}</span>
                </div>
                <div class="toAddDiv" @tap="_navigateTo('/pagesA/myStore/addStore?type=1')"><span class="toAdd">{{language.storeList.tjxxmd}}</span>
                </div>
            </div>
            <div style="height: 120rpx;"></div>
            <div v-if="user_status == 1 && shop_list.length > 0" class="storeList-btn">
                <view class="storeList-bottom" @tap="_navigateTo('/pagesA/myStore/addStore?type=1')">{{language.storeList.tjxxmd}}</view>
            </div>
        </toload>
    </div>
</template>

<script>
    export default {
        data() {
            return {
                user_status: '', //用户等级，商户还是买家,1是卖家 否则是买家
                returnR: '',
                back: '',
                title: '门店管理',
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
                pro_id: '', //虚拟商品-线下核销-需要预约核销门店的商品id
                selectorSrcIndex: 0, //选中的地址下标
                selectorSrcFlag: false, //点击编辑或删除进入多选
                shop_list: [],
                shop_css: '',
                storeEdit: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/storeEdit.png',
                quan_hui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/xuanzehui2x.png',
                quan_hei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/xuanzehei2x.png',
                dizhiNo: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/dizhiNo.png',
                shop_address_id: '',
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
            if(option.pro_id){
                this.pro_id = option.pro_id;
            }
            if (option.shop_address_id) {
                this.shop_address_id = option.shop_address_id;
            }
            this.user_status = option.status_class;
            if(option.isStore){
                this.isStore = option.isStore
            }
        },
        onShow() {
            if(this.isStore){
                this.title = this.language.storeList.title2
            }else{
                this.title = this.language.storeList.title
            }
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
            account(id){
                this.navTo("/pagesC/myStore/accountManagement?mch_store_id=" + id +'&shop_id=' +this.shop_id);
            },
            writeo(){
                this.navTo("/pagesB/myStore/verificationManagement");
            },
            changeRadio(e){
                this.store_id = e.detail.value
            },
            changeLoginStatus() {
                this._axios();
            },
            _navigateTo(url) {
                uni.navigateTo({
                    url
                });
            },
            callClick(e){
                const res = uni.getSystemInfoSync();
                // ios系统默认有个模态框
                if (res.platform == 'ios') {
                	uni.makePhoneCall({
                		phoneNumber: e,
                		success() {
                		},
                		fail() {
                		}
                	})
                } else {
                	//安卓手机手动设置一个showActionSheet
                	uni.showActionSheet({
                		itemList: [e, '呼叫'],
                		success: function(res) {
                			if (res.tapIndex == 1) {
                				uni.makePhoneCall({
                					phoneNumber: e,
                				})
                			}
                		}
                	})
                }
            },
            _axios() {
                if(this.user_status == '') {
                    var data = {
                        api: 'app.mch.my_store',
                        shop_id: this.shop_id
                    };
                } else {
                    var data = {
                        api:"mch.App.Mch.See_my_store",
                        shop_id: this.shop_id
                    };
                    if(this.pro_id){
                        data.pro_id = this.pro_id
                    }
                }
                this.$req.post({ data }).then(res => {
                    uni.hideLoading();
                    if (res.code == 200) {
                        if (this.user_status != 1) {
                            res.data.list.map(item => {
                                item.checked = item.id == this.shop_address_id
                                return item;
                            })
                        }
                        this.shop_list = res.data.list;
                        if(!this.shop_address_id){
                            this.shop_list[0].checked = true
                        }
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
            //点击选择门店地址，在用户下单的时候，存门店地址信息。用此门店自提下单。
            choseStore(item) {
                //必须是买家 下单
                if(this.user_status == '' || this.user_status == undefined){
                    uni.setStorageSync('address_ziti', {name:"门店地址", value: item})
                    if(this.pro_id){
                        uni.setStorageSync('shop_address_id', item.id)
                    }
                    uni.navigateBack({
                        delta: 1
                    })
                }
            },
            choose (id) {
                this.shop_address_id = id;
                uni.setStorageSync('shop_address_id', id)
                this.shop_list.map(item => {
                    item.checked = item.id == this.shop_address_id
                    return item;
                })
                uni.navigateBack({
                    delta: 1
                })
            },
            // 编辑
            edit(id) {
                this._navigateTo('/pagesA/myStore/addStore?edit=true&id='+id);
            },
            // 删除
            del(e) {
                if(!this.store_id){
                    uni.showToast({
                        title: this.language.storeList.Tips[1],
                        icon: 'none'
                    })
                    return
                }
                uni.showLoading({
                    title: this.language.storeList.Tips[0],
                    confirmText: this.language.showModal.confirm,
                    cancelText: this.language.showModal.cancel,
                    mask: true
                });
                var data = {
                    api:"mch.App.Mch.Del_store",
                    id: this.store_id,
                    shop_id: this.shop_id
                };
                // 隐藏做门店编辑，做完解除
                this.$req.post({ data }).then(res => {
                    uni.hideLoading();
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: 'none'
                    });
                    this.store_id = ''
                    this._axios();
                });
            }
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
    .xuanzhong{
        position: absolute;
        right: 0;
        bottom: 0;
        width: 64rpx;
        height: 64rpx;
        z-index: 99;
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
    .callimg{
        width: 36rpx;
        height: 36rpx;
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
    .quanxuan-icon {
        width: 34rpx;
        height: 34rpx;
        vertical-align: middle;
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
