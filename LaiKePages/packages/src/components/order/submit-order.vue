<template>
    <view class="submit-order">
        <div :class='ishide==1?"hide":""' class='foot'>
            <div>
                <p class="yh-pay-ye">{{language.submitOrder.total}}<span><span v-if="score">{{ language.order.order.jf }}{{ score }}</span><span>{{score ?'+':'' }}</span>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(total)}}</span></p>
                <span v-if="rate!='0' && rate!=0" class="pay-rate">{{language.submitOrder.rate}} <span v-if="score">{{ language.order.order.jf }}{{ score }}</span><span>{{score ?'+':'' }}</span> {{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(rate)}}</span>
            </div>
            <p @tap="$emit('submit')">{{language.submitOrder.submit}}</p>
        </div>
    </view>
</template>


<script>
    /**
     * submit-order 订单提交组件
     */
    export default {
        name: 'submit-order',
        props: {
            ishide: {
                default: 0,
                type: Number
            },
            total: {
                default: 0,
                type: [Number, String]
            },
            score: {
                default: 0,
                type: [Number, String]
            },
            rate: {
                default: 0,
                type: [Number, String]
            },
        },
        created(){
            this.setLang();
        }
    }
</script>
<style scoped>   
    .foot {
        /* #ifdef H5 */
        height: 108rpx;
        /* #endif */
        /* #ifdef MP-WEIXIN */
        height: 170rpx;
        /* #endif */
        /* #ifdef APP-PLUS */
        height: 110rpx;
        /* #endif */
        /* padding-top: 16rpx; */
        width: 100%;
        font-size: 30rpx;
        color: #020202;
        border-top: 1px solid #eee;
        position: fixed;
        left: 0;
        bottom: 0;
        z-index: 20;
        background-color: #fff;
        padding-left: 30rpx;
        display: flex;
        flex-flow: row nowrap;
        align-items: center;
        justify-content: space-between;
        box-sizing: border-box;
        padding-bottom: constant(safe-area-inset-bottom); /* 兼容 iOS < 11.2 */
        padding-bottom: env(safe-area-inset-bottom);
    }

    /* Less嵌套 &.hide 转换为CSS后代选择器 */
    .foot.hide {
        display: none;
    }

    /* Less嵌套 .yh-pay-ye 转换为CSS后代选择器 */
    .foot .yh-pay-ye { 
        font-size: 32rpx;
        line-height: 32rpx;
        color: #333333;
    }

    /* Less嵌套 .yh-pay-ye span 转换为CSS后代选择器 */
    .foot .yh-pay-ye span {
        /* Less变量 @priceColor 替换为实际业务色值（示例用常见的红色 #E64340，你可替换为项目真实值） */
        color: #E64340;
        font-weight: bold;
    }

    /* Less嵌套 .pay-rate 转换为CSS后代选择器 */
    .foot .pay-rate {
        /* Less混合宏 .size(22rpx) 展开为标准CSS */
        font-size: 22rpx;
        line-height: 22rpx;
        /* Less变量 @priceColor 替换为实际色值 */
        color: #E64340;
        margin-top: 13rpx;
    }

    /* Less嵌套 >div 转换为CSS子选择器 */
    .foot > div {
        display: flex;
        flex-direction: column;
    }

    /* Less嵌套 >p 转换为CSS子选择器 */
    .foot > p {
        color: #FFFFFF;
        border: 0!important;
        background: linear-gradient(270deg, #FF6F6F 0%, #FA5151 100%);
        display: flex;
        align-items: center;
        justify-content: center;
        width: 192rpx;
        height: 72rpx;
        font-size: 32rpx;
        line-height: 72rpx;
        margin-left: 30rpx;
        margin-right: 32rpx;
        border-radius: 36rpx;
    }
</style>
