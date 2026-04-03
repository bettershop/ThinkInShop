<template>
    <view class='container'>
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title='language.bond.index.title' :bgColor="bgColor" titleColor="#333333" ishead_w="2"></heads>
        
        <view class="con">
            <view class="top">
                <view class="top_price" :style="{background: 'url(' + payText_bgImg + ') 80% 45%'  }">
                    <view class="Nth">
                      {{Number(money).toFixed(2)}}
                    </view>
                    <view class="name">{{language.bond.index.bzj}}({{language.freight_default.yuan}})</view>                    
                </view>
                
                <view class="btns">
                    <view class="btns_left" @tap="tui()">{{language.bond.index.thbzj}}</view>
                    <view class="btns_right"@tap="navTo('/pagesC/bond/paymentRecord')">{{language.bond.index.jnjl}}</view>
                </view>
            </view>
            
             <view class="bottom">
                 <view class="explain">{{language.bond.index.bzjsm}}</view>
                 <view class="vHtml" v-html="margin_description"></view>
             </view>
        </view>
        <!-- 错误提示弹窗 -->
        <show-toast :is_showToast="is_showToast" :is_showToast_obj="is_showToast_obj" @richText="_xieyiShow2"></show-toast>
    </view>
</template>

<script>
    import Heads from '../../components/header.vue'
    import showToast from '@/components/aComponents/showToast.vue'
    export default {
        data () {
            return {
                money:0,
                margin_description:'',
                payDate:'',
                refusedWhy:[],
                status:2,
                payText_bgImg:this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/payText_bgImg.png',
                bgColor: [{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                //组件化弹窗
                is_showToast: 0, //0隐藏弹窗  1成功提示弹窗
                is_showToast_obj: {}, //imgUrl提示图标  title提示文字
            }
        },
        components: {
            Heads,
            showToast
        },
        computed: {
            
        },
        onLoad (option) {
            // this._axios()
        },
        onShow () {
            this.isLogin(()=>{
                this._axios()
            })
        },
        methods: {
            _xieyiShow2(){
                this.is_showToast = 0
            },
            tui(){
                if(this.status==3){
                    uni.navigateTo({
                        url: '/pagesC/bond/success?isPass=true'
                    });
                    return
                }
                uni.showModal({
                    title: this.language.bond.index.thbzj,
                    content: this.language.bond.index.rrthd,
                    confirmColor:'#D73B48',
                	confirmText: this.language.showModal.confirm,
                	cancelText: this.language.showModal.cancel,
                    success:(e)=>{
                        if(e.confirm){
                            var data = {
                                
                                api:"mch.App.Promise.InsertPromisePrice",
                                mch_id:uni.getStorageSync('mch_id')
                            }
                            this.$req.post({data}).then(res => {
                                if (res.code == 200) {
                                   uni.navigateTo({
                                       url: '/pagesC/bond/success'
                                   });
                                }else if(res.code == 203){
                                    uni.showModal({
                                        title: ' ',
                                        content: res.message,
                                        confirmColor:'#333333',
                                    	confirmText: '知道了',
                                        showCancel:false,
                                        success() {
                                            uni.navigateTo({
                                                url:'/pagesD/login/newLogin'
                                            })
                                        }
                                    })
                                }else if(res.code == 413){
                                    uni.navigateTo({
                                        url: '/pagesC/bond/success?isPass=true'
                                    });
                                }else{
                                    this.is_showToast = 3
                                    this.is_showToast_obj.title = res.message
                                    this.is_showToast_obj.button = this.language.showModal.confirm
                                    
                                }
                            })
                        }else{
                            return
                        }
                    }
                })
            },
			_axios() {
			    var me = this
			    var data = {
			        
                    api:"mch.App.Promise.PromiseManage",
			        mch_id:uni.getStorageSync('mch_id')
			    }
			    this.$req.post({data}).then(res => {
			        if (res.code == 200) {
			            this.money=res.data.promisePrice
                        this.payDate = res.data.payDate
			            this.margin_description=res.data.promiseText
                        this.refusedWhy=res.data.refusedWhy
                        this.status=res.data.status
			        }else{
			            uni.showToast({
			                title:res.message,
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
   @import url("../../static/css/myStore/bond/payText.less");   
   /deep/.showToast .xieyi > view .xieyi_title{       
       margin: 64rpx 48rpx  !important;
       text-align: center;
       
       font-weight: normal;
   }
   /deep/.showToast .xieyi > view .xieyi_btm{
       height: 125rpx !important;
       
       font-weight: normal;
       color: #D73B48;
   }
</style>
