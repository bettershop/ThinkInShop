<template>
    <view class='container' style="min-height: 100vh;">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="title" :border="true" :bgColor='bgColor' titleColor="#000" ishead_w="2"></heads>
        <view class="box">
            <view class="titlebox-time" v-if="id">{{language.myStore.addAccount.cjsj}}：{{add_date}}</view>
            
            <view class="titlebox">
                <view class="titlebox-title">{{language.myStore.addAccount.glyzh}}</view>
                <view class="titlebox-img" :style="{background:id?' #F4F5F6':''}">
                    <view class="container_row" style="border-bottom: 1px solid rgb(237, 237, 237);">
                        <view class="freight">
                            <input :disabled="iShow" :placeholder="language.myStore.addAccount.shuzh" v-model='account'/>      
                        </view>
                    </view>                    
                </view>
            </view>

            <view class="titlebox">
                <view class="titlebox-title">{{language.myStore.addAccount.glymm}}</view>
                <view class="titlebox-img">
                    <view class="container_row" style="border-bottom: 1px solid rgb(237, 237, 237);position: relative">
                        <view class="freight">
                            <input class='PWS-input' :placeholder="language.myStore.addAccount.shumm" :password="LoginPWStatus" v-model='password'  type="text"/>        
                            <image v-if="password.length > 0" :src="LoginPWStatus?pwHide:pwShow"
                                 style='height: 42rpx;width: 42rpx;' class='PWS-img'  @tap="pwStatus(1)"/>
                        </view>
                    
                    </view>
                </view>
            </view>
        </view>
        
        <view class="btn">
            <p class="save" @tap="btn()">{{language.myStore.addAccount.btn[0]}}</p>
            <p class="delete" v-if="id" @tap="del()">{{language.myStore.addAccount.btn[1]}}</p>
        </view>
        <view class="xieyi mask" style="background-color: initial;" v-if="is_sus">
            <view style="width: 272rpx;height: 272rpx;background-color: rgba(51, 51, 51, .9);">
                <view style="margin: 32rpx 0;text-align: center;margin-top: 64rpx;">
                    <image style="width: 68rpx;height: 68rpx;" :src="sus"></image>
                </view>
                <view class="xieyi_title"
                    style="margin-bottom: 0;margin-top: 0;color: #fff;font-weight: 500;font-size: 32rpx;">
                    {{ is_title }}
                </view>
            </view>
        </view>
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
                fanghui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/member/fanghui.png',
                pwShow: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/pwHide.png',
                pwHide: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/pwShow.png',
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
                id:'',
                add_date:'',
                account:'',
                password:'',
                LoginPWStatus: true,
                is_sus:false,
                title:'',
                is_title:'',
                shop_id:'',
                mch_store_id:'',
                iShow:false,
            }
        },
        computed: {

        },
        onLoad(option) {
            if(option&&option.item){
                var optionItem=JSON.parse(decodeURIComponent(option.item));
                this.id=optionItem.id
                this.account=optionItem.account_number
                this.password=optionItem.password
                this.add_date=optionItem.add_date
                this.iShow = true
            }
            if(option&&option.arr){
            } 
            if(option&&option.id){
                this.id=option.id
                this.iShow = true
            }   
            if(option&&option.mch_store_id){
                this.mch_store_id=option.mch_store_id
            } 
            if(option&&option.shop_id) {
                this.shop_id = option.shop_id;
            } else {
                this.shop_id = this.$store.state.shop_id;
            }
            if(this.add_date){
                this.title = this.language.myStore.addAccount.bjgly
            }else{
                this.title = this.language.myStore.addAccount.tjgly
            }
        },
        onShow() {
            this.isLogin(() => {
                
            })
        },
        methods: {
            // 密码是否可见 1登录密码 2注册密码 3再次输入注册密码
            pwStatus (type) {
               if (type == 1) {
                   this.LoginPWStatus = !this.LoginPWStatus
               } 
            },
            btn(){
                var me = this
                var data = {
                   api:'mch.App.Mch.AddStoreAdmin',
                   id:this.id,
                   shop_id:this.shop_id,
                   mch_store_id: this.mch_store_id,
                   account_number:this.account,
                   password:this.password
                }
                this.$req.post({
                    data
                }).then(res => {                    
                        if(res.code==200){
                            if(data.id){
                                this.is_title = this.language.myStore.addAccount.bjcg
                            }else{
                                this.is_title = this.language.myStore.addAccount.tjcg
                            }
                            this.is_sus = true
                            setTimeout(()=>{
                                this.is_sus = false
                                this.navBack()
                            },1500)
                        }else{
                            uni.showToast({
                                title: res.message,
                                duration: 1000,
                                icon: 'none'
                            })
                        }
                })
            },
            del(){
                var me = this
                var data = {
                   api:'mch.App.Mch.DelStoreAdmin',
                   shop_id:this.shop_id,
                   mch_store_id: this.mch_store_id,
                   id:this.id
                }
                this.$req.post({
                    data
                }).then(res => {                    
                        if(res.code==200){
                            this.is_title = this.language.zdata.sccg
                            this.is_sus = true
                            setTimeout(()=>{
                                this.is_sus = false
                                this.navBack()
                            },1500)
                        }else{
                            uni.showToast({
                                title: res.message,
                                duration: 1000,
                                icon: 'none'
                            })
                        }
                })
            },
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

    .box {
        border-radius: 0 0 24rpx 24rpx;
        padding-top: 16rpx;
        padding-bottom: 24rpx;
        background-color: #fff;
    }
    .btn{
        transform: none !important;
    }
    .titlebox {
        width: 100%;
        border-radius: 0px 0px 24rpx 24rpx;

        display: flex;
        justify-content: space-between;
        align-items: center;
        color: #333333;
    }
    .titlebox-time{
        font-size: 32rpx;
        
        font-weight: 400;
        color: #333333;
        margin:32rpx 32rpx 20rpx 32rpx;
    }
    .titlebox-title {
        font-size: 32rpx;
        margin-left: 32rpx;
        
        font-weight: 400;
        color: #333333;
    }

    .titlebox-img {
        min-width: 470rpx;
        max-width: 498rpx;
        height: 76rpx;
        padding: 0 16rpx;
        margin: 12rpx 32rpx 12rpx 0;
        // display: flex;
        // align-items: center;
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
                font-size: 32rpx;
                input{
                    width: 100%;
                    // font-weight: 500;
                    color: #333333;
                }
                /deep/.uni-input-input{
                    font-size: 32rpx;
                }
                .jiantou {
                    width: 15rpx;
                    height: 27rpx;
                    margin-left: 17rpx;
                }

            }
        }
    }
    .btn{
        p{
            width: 686rpx;
            height: 92rpx;
            line-height: 92rpx;
            text-align: center;        
            font-size: 32rpx;
            border-radius: 52rpx;
        }
        .save {
            margin: 104rpx auto 40rpx;       
           .solidBtn()
        }
        .delete{
            margin: 0 auto;
           border: 1px solid #Fa5151;
           color: #Fa5151;
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
        > view {
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
    .PWS-input /deep/.uni-input-input{
            // width: 412rpx !important;
            // margin-left: 74rpx; 
            // padding-right:44rpx;
    }
    .PWS-img{ 
           z-index: 99;
           background-color: #fff;
           position: absolute;
        right: 4rpx;
    }
    
</style>
