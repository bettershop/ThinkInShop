<template>
    <view class="container">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="language.myStore.cashier.title" :bgColor="bgColor" titleColor="#333333" ishead_w="2"></heads>
        <view class="content">
            <view class="ecode" @tap="choiceImg()">
                <view class="no_ecode">
                    <view class="list_left">
                        <image :src="ecode" mode=""></image>
                        {{language.myStore.cashier.img}}
                    </view>
                    <view class="list_right" >
                        <image :src="jiantou" v-if="code_img==''"></image>
                        
                        <span class="tips_title" v-else>{{language.myStore.cashier.cxsc}}</span>
                    </view>
                </view>
                <view class="have_ecode" v-if="code_img">
                    <image :src="code_img"></image>
                </view>
            </view>
           
        </view>
        <!-- 提示 -->
        <showToast
            :is_showToast="is_showToast" 
            :is_showToast_obj="is_showToast_obj">
        </showToast>
    </view>
</template>

<script>
import showToast from '@/components/aComponents/showToast.vue'
export default {
    data() {
        return {
            title: '收款码',
            jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jiantou.png',
            ecode: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/add-bank-card.png',
            skrcord: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/skrcord.png',
            shop_id:'',
            sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
            bgColor: [{
                    item: '#ffffff'
                },
                {
                    item: '#ffffff'
                }
            ],
            
            code_img:'',//用户上传的收款码
            is_showToast: 0,//
            is_showToast_obj: {},//
        };
    },
    components: {
        showToast
    },
    onLoad() {
        this._axios()
    },
    onShow() {
        this.shop_id = uni.getStorageSync('shop_id') ? uni.getStorageSync('shop_id') : this.$store.state.shop_id;
        
    },
    methods: {
        choiceImg () {
            uni.chooseImage({
                count: 1,
                success: res => {
                    let data = {
                        src_img: res.tempFilePaths[0],
                    }
                    
                     this.$req.upLoad(res.tempFilePaths[0], data).then(res => {
                        let {
                            code,
                            message,
                            data
                        } = res
                        if (code == 200) {
                             this.$req
                                 .post({
                                     data: {
                                         api:'app.mch.collectionCode',
                                         mchId:this.shop_id,
                                         code:data
                                     }
                                 })
                                 .then(res => {
                                     if(res.code==200){
                                         this.is_showToast = 1
                                         this.is_showToast_obj.imgUrl = this.sus
                                         this.is_showToast_obj.title = this.language.myStore.cashier.sccg
                                         setTimeout(()=>{
                                             this.is_showToast = 0
                                         },1500) 
                                     }
                                     
                                     
                                 });
                                 this._axios()
                        }else{
                            uni.showToast({
                                title:message,
                                duration: 1500,
                                icon: 'none'
                            })
                        } 
                    }) 
                    
                }
            })
        },
        changeLoginStatus(){
            this._axios()
        },
        _axios() {
            this.$req
                .post({
                    data: {
                        
                        api:"mch.App.Mch.Index"
                    }
                })
                .then(res => {
                    if (res.code == 200) {
                        this.code_img = res.data.mch_data.collection_code;
                    }
                });
        },
        toUrl(url){
            this.isLogin(()=>{
                uni.navigateTo({
                    url
                })
            })
        }
    }
};
</script>

<style scoped lang="less">
@import url("@/laike.less");
.container{
    min-height: 100vh;
    background-color: #F6F6F6;
    .content{
        overflow: hidden;
        .list{
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 48rpx;
            border-radius: 20rpx;
            background-color: #FFFFFF;
            margin: 32rpx;
            font-size: 32rpx;
            
            font-weight: 500;
            color: #333333;
            &_left{
                display: flex;
                align-items: center;
                image{
                    width: 48rpx;
                    height: 48rpx;
                    margin-right: 8rpx;
                }
            }
            &_right{
                display: flex;
                align-items: center;
                image{
                    width: 32rpx;
                    height: 44rpx;
                    margin-left: 18rpx;
                }
            }
        }
        .ecode{
            padding:0 48rpx;
            border-radius: 20rpx;
            background-color: #FFFFFF;
            margin: 32rpx;
            font-size: 32rpx;
            
            font-weight: 500;
            color: #333333;
        }
        .no_ecode{
            padding: 48rpx 0;
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: space-between;
            .tips_title{
                font-size: 28rpx;
                
                font-weight: 400;
                color: #999999;
            }
        }
        .have_ecode{
            border-top:1rpx solid rgba(0,0,0,.1) ;
            padding: 48rpx 0;
            width: 590rpx;
            height: 590rpx;
            image{
                width: 100%;
                height: 100%;
            }
        }
    }
}

</style>
