<template>
    <div>
        <div class="skeleton" v-if="!load">
            <div class="order_detail" style="height: 100vh;">
                <div style="height: 77px;" class="skeleton-rect"></div>

                <!--灰色间隔-->
                <div class="yh-line"></div>
                <!--商品列表-->
                <div >
                    <ul>
                        <li class='storeLi'>
                            <div style="height: 20px" class="skeleton-rect">
                                <img src="" class="yh-storeLi-imga">
                                <span class="">{{language.Edit_commodity.dpmc}}</span>
                                <img src="" class="yh-jiantou-img">
                            </div>

                        </li>
                        <li  class='goods'>
                            <img class="skeleton-rect" src=""/>
                            <div class='goods_right'>
                                <p class="skeleton-rect">商品标题商品标题商品标题商品标题商品标题</p>
                                <div class='goods_bottom'>
                                    <p style="width: 60px;" class="skeleton-rect"><span class="yh-goods_bottom-span">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</span>99.99</p>

                                    <div class='format skeleton-rect'>
                                        <p class="yh-format-p">
                                            {{language.Edit_commodity.gg}}：M 四字规格</p>
                                        <p class="yh-format-pa">x1</p>
                                    </div>
                                </div>
                            </div>
                        </li>
                        <div class="yh-items"></div>
                    </ul>
                </div>

                <!--运费、优惠券、合计信息-->
                <ul>
                    <li class='order_coupon'>
                        <span class="skeleton-rect">{{language.Edit_commodity.yf}}</span>
                        <span class="skeleton-rect">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}5.00</span>
                    </li>
                    <li class='order_coupon'>
                        <span class="skeleton-rect">{{language.Edit_commodity.ddbz}}</span>
                        <span class="yh-order_coupon-spana"><input class="skeleton-rect" :placeholder="language.Edit_commodity.xt" placeholder-style="color:#b8b8b8;font-size:28rpx; " type="text"
                        ></span>
                    </li>
                    <li class='order_coupon' >
                        <span></span>
                        <span class="yh-order_coupon-spanb skeleton-rect" >
						<!-- #ifndef MP-BAIDU || MP-ALIPAY -->
						{{language.Edit_commodity.hj}}：<font class="yh-order_coupon-fonta">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}74</font>
                            <!-- #endif -->
                            <!-- #ifdef MP-BAIDU || MP-ALIPAY-->
						{{language.Edit_commodity.hj}}：<span class="yh-order_coupon-fonta">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}74</span>
                            <!-- #endif -->
					</span>
                    </li>
                </ul>

                <div style="height: 157px;" class="skeleton-rect">

                </div>

                <div style="height: 49px;position: fixed;bottom: 0;width: 100%;display:flex;justify-content: space-between">
                    <div style="display:flex;justify-content:center;align-items:center;">
                        <p style="width: 110px;height: 21px;margin-left: 16px;" class="skeleton-rect">{{language.Edit_commodity.fkje}}：{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}</p>
                    </div>
                    <div style="width: 120px;" class="skeleton-rect">{{language.Edit_commodity.ljzf}}</div>
                </div>
            </div>
        </div>
        <skeleton :animation="true" :loading="!load" bgColor="#FFF"></skeleton>
    </div>
    
</template>

<script>
    import skeleton from "@/components/skeleton";
    export default {
        props: {
            load: {
                type: Boolean
            }  
        },
        data () {
            return {
                
            }
        },
        components: {
            skeleton,
        }
    }
</script>

<style lang="less" scoped> 
    // @import url("@/style/laike.less");
    
    // @import url("@/static/css/pay/orderDetailsr.less");
    
    
    .storeLi {
        font-size: 28rpx;
        color: #333333;
        margin-left: 32rpx;
        height: 69rpx;
        display: flex;
        align-items: flex-end;
    }
    
    .btn1 {
        margin-left: 11rpx;
        color: #9D9D9D;
    }
    
    /* Less变量 @priceColor 替换为项目常用的红色 #FA5151（你可根据实际值调整） */
    .btn2 {
        margin-left: 11rpx;
        color: #FA5151;
    }
    
    .hide {
        display: none !important;
    }
    
    /* Less变量 @btnBackground 替换为项目常用的渐变红 #FA5151（你可根据实际值调整） */
    .payBtn {
        position: absolute;
        bottom: 0;
        left: 0;
        width: 100%;
        border-bottom-left-radius: 20rpx;
        border-bottom-right-radius: 20rpx;
        height: 80rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #ffffff;
        background-color: #FA5151;
        font-size: 28rpx;
    }
    
    .maskTitle {
        text-align: center;
        font-size: 28rpx;
        position: relative;
    }
    
    .guanbiImg {
        width: 30rpx;
        height: 30rpx;
        position: absolute;
        top: 20rpx;
        right: 40rpx;
        z-index: 999999;
    }
    
    .animate {
        width: 80%;
        left: 10%;
        margin: 0 auto;
        position: fixed;
        height: 350px;
        border-radius: 20rpx;
        padding: 20rpx;
        padding-top: 60rpx;
        background-color: #fff;
        animation: myPay 0.5s 1;
        animation-timing-function: ease;
        animation-fill-mode: forwards;
    }
    
    .animate1 {
        width: 80%;
        left: 10%;
        margin: 0 auto;
        position: fixed;
        height: 350px;
        border-radius: 20rpx;
        padding: 20rpx;
        padding-top: 60rpx;
        background-color: #fff;
        animation: myPay1 0.5s 1;
        animation-timing-function: ease;
        animation-fill-mode: forwards;
    }
    
    @keyframes myPay {
        from {
            bottom: -1300rpx;
        }
    
        to {
            bottom: 300rpx;
        }
    }
    
    @keyframes myPay1 {
        from {
            bottom: 300rpx;
        }
    
        to {
            bottom: -1300rpx;
        }
    }
    
    uni-input div input {
        color: #000000 !important;
    }
    /* Less /deep/ 转换为 ::v-deep（uniapp跨端兼容） */
    ::v-deep .choose-address .address_one .arrow{
        width: 32rpx;
        height: 44rpx;
    }
    .deco {
        width: 100%;
        height: 100%;
        color: #9D9D9D;
        margin-top: 20rpx;
        border-top: 1px solid #eee;
        padding-top: 30rpx;
        /* padding-left: 30rpx; */
        display: flex;
        align-items: center;
        justify-content: space-between;
    }
    .choose-address{
        border-radius: 0 0 24rpx 24rpx;
    }
    .cart_f {
        /* min-height: 100vh; */
    }
    
    /* Less /deep/ 转换为 ::v-deep + 展开多选择器 */
    ::v-deep .address_one,
    ::v-deep .goods,
    ::v-deep .goods_right > div,
    ::v-deep .foot,
    ::v-deep .format,
    ::v-deep .coupon_sue,
    ::v-deep .order_coupona,
    ::v-deep .order_coupon {
        display: flex;
        flex-flow: row nowrap;
        justify-content: space-between;
        align-items: center;
    }
    .order_coupon input{
        font-size: 14px;
    }
    
    .order_coupon .input-placeholder {
        /* left: 70px; */
    }
    
    .address_one {
        padding: 30rpx;
        font-size: 28rpx;
        color: #020202;
    }
    
    .address_one p {
        font-size: 24rpx;
        color: #9D9D9D;
        margin-top: 20rpx;
    }
    
    .address_two {
        height: 90rpx;
        text-align: center;
        line-height: 90rpx;
        font-size: 30rpx;
        color: #888;
    }
    
    .goods:last-child {
        border: none;
    }
    
    .goods{
        padding:24rpx 0rpx 28rpx 0;
        border-bottom: 1rpx solid #eee;
        margin-left: 32rpx;
        margin-right: 32rpx;
    }
    .goods > img {
        width: 216rpx;
        height: 216rpx;
        border-radius: 16rpx;
        margin-right: 24rpx;
        vertical-align: sub;
    }
    
    .goods_right {
        display: flex;
        flex-direction: column;
        width: 446rpx;
        height: 216rpx;
    }
    
    .goods_right > p {
        white-space: nowrap;
        overflow: hidden;
        color: #333333;
        font-size: 32rpx;
        text-overflow: ellipsis;
    }
    
    .goods_bottom p ,.goods_price p{
        font-size: 32rpx;
        font-weight: 500;
        color: #FA5151;
    }
    
    .goods_right .goods_bottom,.goods_price{
        margin-top: 30rpx;
        padding: 0;
    }
    
    .goods_right .size{
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        color: #999999;
        font-size: 24rpx;
        margin-top: 16rpx;
    }
    
    .format > p {
        font-size: 24rpx;
        color: #333;
    }
    
    .format > p:first-child {
        font-size: 20rpx;
        color: #9D9D9D;
        margin-right: 30rpx;
    }
    
    .foot {
        height: 186rpx;
        width: 100%;
        justify-content: space-between;
        font-size: 30rpx;
        color: #020202;
        border: 1rpx solid #eee;
        position: fixed;
        left: 0;
        bottom: 0;
        z-index: 20;
        padding-bottom: constant(safe-area-inset-bottom); /* 兼容 iOS < 11.2 */
        padding-bottom: env(safe-area-inset-bottom);
        background-color: #fff;
        padding-left: 30rpx;
    }
    
    /* Less /deep/ + 嵌套 转换为 ::v-deep + 展开层级 */
    ::v-deep .foot > p { 
        width: 240rpx;
        /* Less变量 @btnBackground 替换为 #FA5151 */
        background-color: #FA5151;
        color: #fff;
        line-height: 98rpx;
        text-align: center;
        margin-left: 30rpx;
    }
    
    /*余额支付密码框*/
    .payment_pass,
    .copon_div {
        height: 100vh;
        width: 100%;
        background-color: rgba(000, 000, 000, 0.5);
        position: fixed;
        top: 0;
        left: 0;
        z-index: 45;
        display: flex;
        justify-content: center;
        align-items: center;
    }
    
    .payment_c {
        width: 550rpx;
        height: 400rpx;
        background-color: #fff;
        text-align: center;
        font-size: 30rpx;
        border-radius: 23rpx;
        position: relative;
    }
    
    .payment_tt {
        border-top: 1px solid #eee;
        width: 100%;
        font-size: 34rpx;
        color: #005edf;
        height: 94rpx;
        position: absolute;
        bottom: 0;
        left: 0;
    }
    
    .cancel {
        border-right: 1px solid #eee;
        color: #999999;
    }
    
    .confirm {
        color: #020202;
    }
    
    .cancel,
    .confirm {
        width: 50%;
        float: left;
        height: 100%;
        line-height: 98rpx;
    }
    
    .payment_c > p {
        line-height: 160rpx;
        font-size: 30rpx;
        color: #020202;
    }
    
    .pwd-wrap,
    .pwd-wrap li {
        display: flex;
        flex-flow: row nowrap;
        justify-content: center;
        align-items: center;
    }
    
    .xian_d {
        width: 100%;
    }
    
    .pwd-wrap li {
        width: 80rpx;
        height: 80rpx;
        border: 1px solid #eee;
        border-right: none;
        text-align: center;
        position: relative;
    }
    
    .pwd-wrap li:last-child {
        border-right: 1px solid #eee;
    }
    
    .masd {
        width: 80rpx;
        height: 80rpx;
        padding: 0;
        position: absolute;
        top: 0;
        left: 0;
        border: 0;
        text-align: center;
    }
    
    .payment_ww {
        font-size: 32rpx;
        color: #020202;
        margin-top: 120rpx;
    }
    
    .payment_boo {
        line-height: 94rpx;
    }
    .bounced {
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.5);
        position: fixed;
        left: 0;
        top: 0;
        z-index: 8000;
    }
    .order_detail{
        /* padding-top: 16rpx; */
    }
    .bounced-box {
        bottom: 0;
        width: 100%;
        /*height: 55%;*/
        background: #fff;
        position: fixed;
        z-index: 999;
        border-radius: 24rpx 24rpx 0px 0px;
    }
    .bounced-box-list_title{
        width: 654rpx;
        height: 494rpx;
        border-radius: 20rpx;
        border: 1rpx solid rgba(0,0,0,0.1);
        display: flex;
        flex-direction: column;
    }
    .bounced-box-list_title_num{
        padding-left: 32rpx;
        text-align: right;
        padding-right: 32rpx;
    }
    .bounced-box-list_title_inptut{
        padding:  32rpx 32rpx 0 32rpx;
        width: 590rpx;
        height: 400rpx;
    }
    .bounced-box-box {
        width: 100%;
        display: flex;
        height: 136rpx;
        display: flex;
        background-color: #f4f5f6;
        border-radius: 24rpx 24rpx 0px 0px;
        align-items: center;
        justify-content: center
    }
    .bounced-box-list{
        display: flex;
        align-items: center;
        justify-content: center;
        margin-top: 48rpx;
    }
    .bounced-box-box-title {
        margin-left: 270rpx;
        font-size: 40rpx;
        font-weight: 500;
        color: #333333;
    }
    .bounced-box-list_btn{
        margin-top: 64rpx;
        display: flex;
        justify-content: center;
        width: 100%;
        align-items: center;
        height: 124rpx;
        border-top: 1rpx solid #eee;
        padding-bottom: constant(safe-area-inset-bottom); /* 兼容 iOS < 11.2 */
        padding-bottom: env(safe-area-inset-bottom);
    }
    .bounced-box-list_btn_box{
        width: 686rpx;
        height: 92rpx;
        background: linear-gradient(270deg, #FF6F6F 0%, #FA5151 100%);
        border-radius: 52rpx;
        line-height: 92rpx;
        text-align: center;
        color: #fff;
        font-weight: 500;
        font-size: 32rpx;
    }
    .bounced-box-box-img {
        width: 32rpx;
        height: 32rpx;
        margin-left: 200rpx;
    }
    .copon_div {
        align-items: flex-end;
    }
    
    .coupon_sue,
    .order_coupon {
        /* padding: 32rpx; */
        margin-left: 32rpx;
        margin-right: 32rpx;
        height: 108rpx;
        color: #020202;
        font-size: 28rpx;
    }
    .order_coupona {
        /* padding: 32rpx; */
        margin-left: 32rpx;
        margin-right: 32rpx;
        height: 108rpx;
        color: #020202;
        font-size: 28rpx;
    }
    .order_coupon span {
        font-size: 32rpx;
        color: #333333;
    }
    .order_coupona span {
        font-size: 32rpx;
        color: #333333;
    }
    .coupon_sue {
        border-bottom: 1rpx solid #eee;
    }
    
    .coupon_ul {
        height: 45%;
        /* background-color: #fff; */
        position: relative;
        padding: 0;
        overflow: hidden;
        overflow-y: auto;
        margin-bottom: 100rpx;
    }
    
    .copou_close {
        padding: 30rpx;
        color: #020202;
        font-size: 30rpx;
        border-top: 1px solid #dddddd;
        position: absolute;
        bottom: 0;
        left: 0;
        width: 100%;
        text-align: center;
        z-index: 45;
        background-color: #020202;
        color: #fff;
    }
    
    .order_coupon {
        border-bottom: 1rpx solid #eee;
    }
    .order_coupona {
        /* #ifdef APP */
        border-bottom: 1rpx solid #eee;
        /* #endif */
        /* #ifndef APP */
        /* border-bottom: 1rpx solid #eee; */
        /* #endif */
    }
    
    .pay_yue {
        display: flex;
        align-items: center;
    }
    
    .head,
    .order_header {
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
    
    .head {
        height: 88rpx;
        padding: 0 30rpx;
        border-bottom: 1rpx solid #eee;
        font-size: 40rpx;
        color: #242424;
    }
    
    .head_img {
        width: 24rpx;
        height: 36rpx;
    }
    
    .head_search {
        width: 40rpx;
        height: 40rpx;
        visibility: hidden;
    }
    
    .order_header {
        justify-content: space-around;
        width: 100%;
        border-bottom: 1px solid #D2D2D2;
    }
    
    .header_li {
        width: 25%;
        text-align: center;
        height: 84rpx;
        line-height: 84rpx;
        border-bottom: 1rpx solid #eee;
        font-size: 30rpx;
        color: #9D9D9D;
    }
    
    .header_border {
        color: #020202;
        font-weight: bold;
        position: relative;
    }
    
    .header_border:after {
        border-bottom: 5rpx solid #020202;
        position: absolute;
        bottom: 0;
        width: 50%;
        content: "";
        left: 25%;
    }
    
    .last_right > div {
        justify-content: center;
    }
    
    .position_head {
        position: fixed;
        left: 0;
        top: 0;
        width: 100%;
        background-color: #fff;
        z-index: 35;
    }
    
    .receiving {
        width: 100%;
    }
    
    .afhalen_box {
        display: flex;
        justify-content: space-between;
        align-items: center;
        width: 100%;
        padding: 30rpx;
        color: #020202;
        font-size: 28rpx;
    }
    
    .afhalen_box input {
        height: 60rpx;
        font-size: 26rpx;
        color: #242424;
    }
    
    .switch-box {
        display: flex;
        justify-content: space-around;
        align-items: center;
        font-size: 24rpx;
        position: relative;
        width: 144rpx;
        height: 60rpx;
        border-radius: 30rpx;
        color: #fff;
        background-color: #4CD864
    }
    
    .switch-item {
        position: absolute;
        top: 2rpx;
        left: 0;
        width: 72rpx;
        height: 56rpx;
        border-radius: 28rpx;
        background: rgba(255, 255, 255, 1);
        box-shadow: -1px 5px 5px 0px rgba(13, 4, 8, 0.15);
        transition: -webkit-transform .3s;
        -webkit-transition: -webkit-transform .3s;
        transition: transform .3s;
        transition: transform .3s, -webkit-transform .3s;
    }
    
    .afhalen_shop img {
        width: 15rpx;
        height: 27rpx;
    }
    
    /* 优化 */
    .yh-afhalen_box {
        justify-content: flex-start;
        padding: 20rpx 30rpx;
    }
    
    .yh-afhalen-inputa {
        width: 130rpx;
        text-overflow: ellipsis;
    }
    
    .yh-afhalen-inputb {
        flex: 1;
    }
    
    .yh-address_two {
        text-align: left;
        padding-left: 15px;
    }
    
    .yh-one-box {
        width: 90%;
    }
    
    .yh-one-spana {
        margin-right: 30rpx;
        font-weight: bold;
    }
    
    .yh-one-spanb {
        font-weight: bold;
    }
    
    /* Less /deep/ + @bgColor 转换为 ::v-deep + 实际色值 #F4F5F6 */
    ::v-deep .yh-line {
        width: 100%;
        height: 16rpx !important;
        background-color: #F4F5F6;
    }
    
    .yh-storeLi-imga {
        width: 40rpx;
        height: 40rpx;
        border-radius: 50%;
        margin-right: 20rpx;
    }
    
    .yh-jiantou-img {
        width: 15rpx;
        height: 30rpx;
        margin-left: 20rpx;
    }
    
    .yh-goods_bottom-span {
        font-size: 22rpx;
    }
    
    .yh-format {
        clear: both;
    }
    
    .yh-format-p {
        font-weight: 500;
    }
    
    .yh-format-pa {
        color: #333333 !important;
        font-size: 28rpx !important;
    }
    
    .yh-items {
        width: 100%;
        height: 16rpx;
        /* Less变量 @bgColor 替换为 #F4F5F6 */
        background-color: #F4F5F6;
    }
    .djs{
        position: fixed;
        left: 0;
        bottom:100rpx;
        width: 100%;
        padding-bottom: constant(safe-area-inset-bottom); /* 兼容 iOS < 11.2 */
        padding-bottom: env(safe-area-inset-bottom);
        background-color: #feeded;
        border-radius: 24rpx 24rpx 0rpx 0rpx;
        color: #FA5151;
    }
    .djs_box{
        margin-left: 32rpx;
        font-size: 28rpx;
        font-weight: 500;
        padding-top: 24rpx;
        padding-bottom: 24rpx;
        color: #FA5151;
    }
    
    .yh-order_coupon-spana {
        /* width: 52%; */
        flex: 1;
        /* margin-right: 32rpx; */
        text-align: right;
    }
    
    .yh-order_coupon-spanb {
        font-size: 30rpx;
    }
    
    .yh-order_coupon-spanc {
        font-size: 28rpx;
        color: #020202;
    }
    
    /* Less变量 @priceColor 替换为 #FA5151 */
    .yh-order_coupon-fonta {
        color: #FA5151;
        font-size: 30rpx;
        font-weight: bold;
    }
    
    .yh-pay-div {
        width: 100%;
    }
    
    .yh-pay_yue {
        position: relative;
        width: 100%;
        justify-content: space-between;
    }
    
    .yh-pay_yue-div {
        display: flex;
        align-items: center;
    }
    
    .yh-pay_yue-p {
        width: 400rpx;
    }
    
    .yh-pay_yue-p-font {
        font-size: 28rpx;
        font-weight: bold;
    }
    
    .yh-deco-num {
        color: #020202;
        font-size: 28rpx;
        width: 150rpx;
    }
    
    .yh-deco-je {
        border: 2rpx #B8B8B8 solid;
        padding: 10rpx 10rpx 10rpx 0;
        width: 200rpx;
        border-radius: 6rpx;
    }
    
    .yh-deco-je-input {
        text-align: right;
        font-size: 28rpx;
        color: #000000;
    }
    
    .yh-pay {
        width: 100%;
    }
    
    .yh-pay-wx-d {
        position: relative;
        width: 100%;
        justify-content: space-between;
    }
    
    .yh-pay-s {
        display: flex;
        align-items: center;
    }
    
    .yh-pay_div {
        margin-left: 30rpx;
    }
    
    .yh-pay_div-p {
        width: 400rpx;
    }
    
    /* Less嵌套语法展开 */
    .yh-pay-ye {
        font-size: 32rpx;
        color: #666666;
    }
    .yh-pay-ye span{
        font-size: 32rpx;
        font-weight: 500;
        color: #FA5151;
    }
    
    .yh-wx-pay {
        height: 110rpx;
        width: 100%;
    }
    
    .yh-pay-numbera {
        position: absolute;
        z-index: 222;
        width: 100%;
        height: 88rpx;
        opacity: 0;
        -webkit-text-security: disc;
        text-security: disc;
    }
    
    .yh-pay-numberb {
        position: absolute;
        z-index: 222;
        width: 100%;
        height: 88rpx;
        opacity: 0;
        -webkit-text-security: disc;
        text-security: disc;
    }
    
    .yh-forget {
        color: #0080FF;
        font-size: 28rpx;
        text-align: right;
        margin-top: 10rpx;
        margin-right: 40rpx;
    }
    
    .yh-img-s {
        width: 34rpx;
        height: 34rpx;
    }
    
    .gopay {
        display: flex;
        justify-content: center;
        align-items: center;
        /* Less变量 @btnBackground 替换为 #FA5151 */
        background-color: #FA5151;
        color: #fff;
        width: 100%;
        height: 100rpx;
    }
    
    .yh-mp {
        height: 178rpx;
        position: fixed;
        background: #FFFFFF;
        z-index: 999;
    }
    
    .yh-mps {
        width: 100%;
        height: 88rpx;
    }
    
    .yh-mp-p {
        font-size: 32rpx;
    }
    
    .yh-address {
        width: 90%;
    }
    
    .yh-address-name {
        margin-right: 30rpx;
        font-weight: bold;
    }
    
    .yh-address-tel {
        font-weight: bold;
    }
    
    .box_top{
        border-radius: 24rpx;
        background-color: #fff;
    }
    
    .yh-storeLi {
        border-bottom: 1rpx solid #eee;
        color: #666666;
    }
    
    .yh-storeImg {
        width: 30rpx;
        height: 30rpx;
        margin-right: 20rpx;
    }
    
    .yh-jiantou {
        width: 15rpx;
        height: 30rpx;
        margin-left: 20rpx;
    }
    
    .yh-money[class] {
        display: flex;
        justify-content: center;
        align-items: center;
        font-weight: normal;
    }
    
    .yh-money-span {
        font-size: 22rpx;
    }
    
    .yh-integral {
        height: 23rpx;
        width: 23rpx;
        margin-right: 3px;
    }
    
    .yh-format-p1 {
        font-weight: 500;
        font-size: 24rpx;
        font-weight: 500;
        color: #999999;
        transform: scale(0.83);
        margin-right: 0 !important;
    }
    
    .yh-format-p2 {
        font-weight: 500;
        color: #020202;
    }
    
    .yh-format {
        clear: both;
    }
    
    .yh-integral_hui {
        display: flex;
        justify-content: center;
        align-items: center;
    }
    
    .yh-integral_color {
        font-size: 28rpx !important;
        color: #999999 !important;
    }
    
    .yh-user_score {
        height: 23rpx;
        width: 23rpx;
        margin-right: 3px;
    }
    
    /* Less变量 @priceColor 替换为 #FA5151 */
    .yh-freightt {
        color: #FA5151;
        font-size: 30rpx;
        font-weight: bold;
    }
    
    .yh-heji {
        font-size: 32rpx;
        font-weight: bold;
    }
    
    .yh-integral_hong {
        height: 26rpx;
        width: 26rpx;
        margin-right: 6rpx;
    }
    
    .yh-integral_hongs {
        height: 26rpx;
        width: 26rpx;
        margin-right: 3px;
    }
    
    /* Less变量 @priceColor 替换为 #FA5151 */
    .yh-font {
        color: #FA5151;
        font-size: 30rpx;
        font-weight: bold;
    }
    
    .yh-mpwx {
        position: absolute;
        z-index: 222;
        width: 100%;
        height: 88rpx;
        opacity: 0;
        -webkit-text-security: disc;
        text-security: disc;
        margin-left: -100%;
        padding-right: 100%;
    }
    
    .yh-mpwxs {
        position: absolute;
        z-index: 222;
        width: 100%;
        height: 88rpx;
        opacity: 0;
        -webkit-text-security: disc;
        text-security: disc;
    }
    
    .yh-wjmi {
        color: #0080FF;
        font-size: 28rpx;
        text-align: right;
        margin-top: 10rpx;
        margin-right: 40rpx;
    }
    
    .pickerBox{
        display: flex;
        align-items: center;
        text-overflow: ellipsis;
        overflow: hidden;
        white-space: nowrap;
    }
    
    .picher_placeholder{
        color: #FA5151;
        font-size: 32rpx;
        font-weight: 500;
    }
    
    .uni-input {
        overflow:hidden;
        text-overflow:ellipsis;
        white-space:nowrap;
        max-width: 290rpx;
        color: #333333;
        font-size: 32rpx;
    }
    
    .order_coupon{
        margin-left: 30rpx;
        padding-left: 0;
    }
    
    /* Less变量 @priceColor 替换为 #FA5151 + 展开嵌套 */
    .vip_rate[class]{
        color: #FA5151;
        font-size: 22rpx;
        line-height: 22rpx;
        margin-top: 8rpx;
    }
    
    .yh-format-p1 {
        color: #020202 !important;
    }
    
    .goods {
        border-bottom: 1rpx solid #eee !important;
    }
    @media screen and (min-width: 600px) {
        .pickerBox{
            max-width:200px !important;
        }
        uni-picker{
            width: 100% !important;
        }
    }
</style>
