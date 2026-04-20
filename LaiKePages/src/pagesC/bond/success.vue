<template>
    <view class='container'>
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title='title' :bgColor="bgColor" titleColor="#333333" ishead_w="2"></heads>
        <view class="con" v-if="!str">
            <image class="img" :src="applySuc_apply"/>
            <view class="vtitle">{{isPass?language.bond.success.sqz:language.bond.success.tjcg}}</view>
           
        </view>
        <view class="con" v-else>
            <image class="img" :src="applySuc_apply"/>
            <view class="vtitle">{{language.bond.success.jncg}}</view>
        </view>
        <view class="btns" v-if="!isPass">
            <view class="gohome" @tap="reshop">{{str?language.bond.success.jrdp:language.bond.success.fhdpzy}}</view>
            <view class="gouser" @tap="goUser">{{language.bond.success.fh}}</view>
        </view>
    </view>
</template>

<script>
    import Heads from '../../components/header.vue'
    
    export default {
        data () {
            return {
                gouhei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/gouhei.png',
                applySuc_apply: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/applySuc_apply.png',
                str:false,
                title:'',
                bgColor: [{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                isPass:false,
            }
        },
        components: {
            Heads
        },
        onLoad (option) {
            this.str = option.str
        
            if(option&&option.isPass){
                this.isPass=option.isPass
            }
            if(option.token && option.PayerID && uni.getStorageSync('payRes')){
                this.str = true
                this.paypal_token = option.token
                this.baiBeiNo = JSON.parse(uni.getStorageSync('payRes')).sNo
                this.capture()
            }
        },
        onShow () {
           if(this.str){
               this.title = this.language.bond.success.jn
           }else{
               this.title = this.language.bond.success.th
           }
        },
        onUnload(){
            if( uni.getStorageSync('payRes')){
                uni.removeStorageSync('payRes')
            }
        },
        methods: {
            goUser(){
                uni.redirectTo({
                    url: '/pages/shell/shell?pageType=my'
                })
            },
            reshop(){
                uni.navigateTo({
                    url: '/pagesA/myStore/myStore'
                })
            },
            // 贝宝执行扣款
            async capture() {
            	const  data = {
            		api:'app.pay.capture',
            		orderId : this.paypal_token,
            		sNo : this.baiBeiNo
            	}
            	this.$req.post({data}).then(res => {
            	    uni.hideLoading()
            	    if(res.code == 200 ){
            	       // 成功
            	    } else {
            	        uni.showToast({
            	            title: res.message,
            	            icon: 'none'
            	        })
            	    }
            	}).catch(e => {
            	})
            },
		}	
    }
</script>

<style>
    page {
        background-color: #f4f5f6;
    }
</style>
<style lang="less" scoped> 
    @import url("@/laike.less");
    .con{
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding:104rpx 0 64rpx ;
        background: #FFFFFF;
        border-radius: 0px 0px 24rpx 24rpx;
        .img{
            width:750rpx;
            height: 460rpx;
            margin-bottom: 20rpx;
        }
        .vtitle{
            font-size: 32rpx;
            
            font-weight: 500;
            color: #333333;
        }
        .sub{
            margin-top: 32rpx;
            font-size: 28rpx;
            
            font-weight: 400;
            color: #999999;
        }
    }
    .btns{
        margin-top: 104rpx;
        view{
            padding: 24rpx 0;
            width: 686rpx;
            text-align: center;
            margin: 0 auto;
            border-radius: 52rpx;
            font-size: 32rpx;
            
            font-weight: 500;
        }
        .gouser{
            border: 1px solid #Fa5151;
            color: #Fa5151;
        }
        .gohome{
            margin-bottom:40rpx ;
           .solidBtn()
        }
    }
    
</style>
