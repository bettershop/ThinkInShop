<template>
    <div class="store-container" :style="shop_css">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="title" :bgColor="bgColor" :titleColor="'#333333'" :ishead_w="2"></heads>
        <div class="ts_box" v-if="!isStore && (shop_list && shop_list.length<=0)">
             <image class="ts_img" :src="warnIng"></image>
            <span class="ts_font">{{language.storeList.szhhxsj}}</span> 
         </div>
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
                        <div v-else @tap.stop="choose(item.id)">
                        </div>
                    </template>
                    
                </div>
                <div class="store-item-bottom">
                    <div :class="user_status == 1?'business_hours':'business_hours2'">
                        <view v-if="!isStore">{{language.storeList.phone}}: {{ item.mobile }}</view>
                        <view>{{language.storeList.Business_Hours}} {{ item.business_hours }}</view>
                        <image @click.stop="callClick(item.mobile)" class="callimg" :src="Call_phone" v-if="isStore"></image>
                    </div>
                </div>
                <p class="store-item-disc" v-if="user_status == 1">{{ item.sheng }}{{ item.shi }}{{ item.xian }}{{ item.address }}</p>
                <p class="store-item-disc2" v-else>{{ item.sheng }} {{ item.shi }} {{ item.xian }} {{ item.address }}</p>
                
                <view class="btBox"  v-if="user_status == 1">
                    <!-- 账号管理 -->
                    <view class="bt" @tap="account(item.id)">{{language.storeList.zhgl}}</view>
                    <!-- 核销时间 -->
                    <view class="bt" @tap="writeo(item)">{{language.storeList.hxsj}}</view>
                </view>

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
                warnIng: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/warnIng.png',
                quan_ho: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/new_red_select.png',
                Call_phone: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/Call_phone.png',
                xuanzhong_jl: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xuanzhong_jl.png',
                list: false,
                shop_id: '',
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
            writeo(row){
                // this.navTo("/pagesB/myStore/verificationManagement");
                this.navTo("/pagesB/myStore/verificationManagement?mch_store_id=" + row.id +'&shop_id=' +this.shop_id);
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
                if(this.user_status == '')
                {
                    var data = {
                        api: 'app.mch.my_store',
                        shop_id: this.shop_id
                    };
                }
                else
                {
                    var data = {
                        
                        api:"mch.App.Mch.See_my_store",
                        shop_id: this.shop_id
                    };
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
    @import url('../../static/css/myStore/storeList.less');
</style>
