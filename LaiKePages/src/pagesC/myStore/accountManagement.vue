<template>
    <div class="store-container" style="min-height: 100vh;">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="language.myStore.accountManagement.title" :bgColor="bgColor" :titleColor="'#333333'" :ishead_w="2"></heads>
        <toload :load="loadFlag">
            <div class="store-item" v-for="(item, index) in shop_list" :key="index" @tap="edit(item)">
                <p>{{language.myStore.accountManagement.glyzh}}：{{ item.account_number }}</p>
                <div class="store-item-bottom">
                    <div>{{language.myStore.accountManagement.time}}：{{ item.add_date }}</div>
                </div>
                <p class="store-item-disc">{{language.myStore.accountManagement.endtime}}：{{item.last_time}}</p>
            </div>

            <div v-if="shop_list.length == 0" class="add-store">
                <div class='noStore'>
                    <img :src="noOrder" />
                    <span>{{language.myStore.accountManagement.tips}}～</span>
                </div>
                
                <div class="toAddDiv" @tap="add()"><span class="toAdd">{{language.myStore.accountManagement.btn}}</span>
                </div>
            </div>
            
            <div style="width:750rpx;height:222rpx ;"></div>
            
           <div v-if="shop_list.length > 0" class="storeList-bottom">
                <button @tap="add()">{{language.myStore.accountManagement.btn}}</button>
            </div>
        </toload>
    </div>
</template>

<script>
    export default {
        data() {
            return {
                title: '账号管理',
                bgColor:[{
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
                id:'',
                store_id: '',
                pageNo:1,
                loadingType: 0,
                isStore: '', //是否是从店铺主页跳转
            };
        },
        onLoad(option) {
            if (option.shop_id) {
                this.shop_id = option.shop_id;
            } else {
                this.shop_id = this.$store.state.shop_id;
            }
            
            if(option.mch_store_id){
                this.id=option.mch_store_id
            }
            
        },
        onShow() {
            this.clearType()
            if(this.isStore){
                this.title = this.language.storeList.title2
            }else{
                this.title = this.language.storeList.zhgl
            }
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
            clearType(){
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
                   api:'mch.App.Mch.StoreAdminList',
                   shop_id:this.shop_id,
                   mch_store_id: this.id,
                   pageNo:this.pageNo,
               };

                this.$req.post({ data }).then(res => {
                    uni.hideLoading();
                    if (res.code == 200) {
                        if(this.pageNo == 1){
                            this.shop_list = res.data.list;
                        }else{
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
            add(){
                this._navigateTo('/pagesC/myStore/addAccount?mch_store_id='+this.id + '&shop_id=' +this.shop_id);
                
            },
            // 编辑
            edit(item) {
                this._navigateTo('/pagesC/myStore/addAccount?item='+encodeURIComponent(JSON.stringify(item)) + '&arr=' + JSON.stringify(item) + '&shop_id=' +this.shop_id +'&mch_store_id='+this.id);
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
                    mask: true
                });
                var data = {
                    // module: 'app',
                    // action: 'mch',
                    // m: 'del_store',
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
        // background: rgba(249, 249, 249, 1)
    }
    .store-item-disc{
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
        &:first-child{
            border-radius: 0 0 24rpx 24rpx;
        }
        
        &-disc{
            margin-top: 16rpx;
            text-overflow: -o-ellipsis-lastline;
            overflow: hidden;
            text-overflow: ellipsis;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            line-clamp: 2;
            -webkit-box-orient: vertical;
        }
        
        &-bottom{
            margin-top: 24rpx;
            .business_hours{
                margin-bottom: 16rpx;
            }
        }
    }
    
    .add-store {
        width: 751rpx;
        text-align: center;
        img{
            width: 751rpx;
            height:400rpx;
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
    
    .storeList-bottom{
            position: fixed;
            bottom: 0;
            z-index: 999;
            width: 100%;
            min-height: 124rpx;
            display: flex;
            align-items: center;
            justify-content: center;
            background-color: #ffffff;
            box-shadow: 0 0 0 0 rgba(0,0,0,0.2);
            padding-bottom: constant(safe-area-inset-bottom); /* 兼容 iOS < 11.2 */
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
                padding:0;
                .solidBtn()
            }
        }
    
</style>
