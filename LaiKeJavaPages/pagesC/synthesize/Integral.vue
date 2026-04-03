<template>
	<view>
        <template v-if="type == 'jifen'">
            <heads :title="language.integral.integral.jfsc" :returnR="1" :border="true" :bgColor='bgColor' titleColor="#000" ishead_w="2"></heads>
        </template>
        <template v-if="type == 'yushou'">
            <heads :title="language.integral.integral.ysgl" :returnR="1" :border="true" :bgColor='bgColor' titleColor="#000" ishead_w="2"></heads>
        </template>
        <!-- 积分商城 -->
        <view class="pages" v-if="type == 'jifen'">
            <!-- 积分商品 -->
            <view class="pages_jfsp animation1" @click="to_goods">
                <view class="pages_jfsp_txt">{{language.integral.integral.jfsp}}</view>
                <image class="jiantou" :src="jiantou"></image>
            </view>
            <!-- 兑换订单 -->
            <view class="pages_jfsp animation2"  @click="to_order">
                <view class="pages_jfsp_txt">{{language.integral.integral.dhdd}}</view>
                <image class="jiantou" :src="jiantou"></image>
            </view>
            <!-- 评价管理 -->
            <view class="pages_jfsp animation3" @click="to_evaluation">
                <view class="pages_jfsp_txt">{{language.integral.integral.pjgl}}</view>
                <image class="jiantou" :src="jiantou"></image>
                <view class="message-quantity" v-if="messageQuantity">{{messageQuantity}}</view>
            </view>
        </view>
        <!-- 预售管理 -->
        <view class="pages" v-if="type == 'yushou'">
            <!-- 预售商品 -->
            <view class="pages_jfsp animation1" @click="_navigateto('/pagesC/synthesize/yushou_goods')">
                <view class="pages_jfsp_txt">{{language.integral.integral.yssp}}</view>
                <image class="jiantou" :src="jiantou"></image>
            </view>
            <!-- 预售订单 -->
            <view class="pages_jfsp animation2" @click="_navigateto('/pagesC/synthesize/yushou_order')">
                <view class="pages_jfsp_txt">{{language.integral.integral.ysdd}}</view>
                <image class="jiantou" :src="jiantou"></image>
            </view>
            <!-- 订单结算 -->
            <view class="pages_jfsp animation3"  @click="_navigateto('/pagesC/synthesize/yushou_settlement')">
                <view class="pages_jfsp_txt">{{language.integral.integral.ddjs}}</view>
                <image class="jiantou" :src="jiantou"></image>
            </view>
            <!-- 评价管理 -->
            <view class="pages_jfsp animation4" @click="_navigateto('/pagesC/synthesize/yushou_Evaluation')">
                <view class="pages_jfsp_txt">{{language.integral.integral.pjgl}}</view>
                <image class="jiantou" :src="jiantou"></image>
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
                messageQuantity:10,
                title: '', //标题
                jiantou: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/jiantou2x.png',
                type: '',//进入类型 jifen-积分商品  yushou-预售管理 
			}
		},
        onLoad(option) {
            //获取进入类型 jifen-积分商品  yushou-预售管理
            if(option.type){
                this.type = option.type
                if(this.type == 'yushou'){
                    uni.setNavigationBarTitle({
                        title: '预售管理',
                    })
                }
            }
        },
        onShow(){
            if(this.type == 'jifen'){                
                this.evaluationUnreadNumer()
            }
        },
		methods: {
            //跳转
            _navigateto(url){
                uni.navigateTo({
                    url: url
                })
            },
            //评价管理
            to_evaluation(){
                uni.navigateTo({
                    url:'/pagesC/evaluate/Evaluation/Evaluation'
                })
            },
            // 积分商品 评价管理未读消息数量
            evaluationUnreadNumer(){
                const data = {
                    api:'plugin.integral.AppIntegral.getCommentsCount'
                }
                this.$req.post({data}).then(res=>{
                    if(res.code == 200){
                        this.messageQuantity = res.data
                    }
                })
            },
            //积分商品
			to_goods(){
                uni.navigateTo({
                    url:'/pagesB/integral/Integral_goods'
                })
            },
            //兑换订单
            to_order(){
                uni.navigateTo({
                    url:'/pagesC/redeemOrder/Redeem_order',
                })
            },
            
		}
	}
</script>

<style scoped lang="less">
    @import url('@/laike.less');
    .animation1{
        animation: anShowToast5 .5s both;
    }
    .animation2{
        animation: anShowToast5 .6s both;
    }
    .animation3{
        animation: anShowToast5 .7s both;
        position: relative;
    }
    .message-quantity{ 
        position: absolute;
        left: 154rpx;
        top: 24rpx;
        background-color: red;
        color: #fff;
        border-radius: 20rpx;
        padding: 0rpx 8rpx;
        font-size: 20rpx;
    }
    .animation4{
        animation: anShowToast5 .8s both;
    }
    /* 右滑 显示动画 */
    @keyframes anShowToast5 {
       0% {
       opacity: 0;
       margin-left: -100%;
       }
           
       100% {
       opacity: 1;
       margin-left: 0;
       }
    }
    page{
        background-color: #f4f5f6;
    }
    .pages_jfsp_txt{
        margin-left: 32rpx;
        font-size: 32rpx;
        
        font-weight: normal;
        color: #333333;
    }
    .pages{
        width: 100%;
        display: flex;
        flex-direction: column;
    }
    .pages_jfsp{
        margin-top: 24rpx;
        width: 100%;
        height: 108rpx;
        background-color: #fff;
        border-radius: 16rpx;
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
.jiantou{
    width:32rpx;
    height:44rpx;
    margin-right: 32rpx;
}
</style>
