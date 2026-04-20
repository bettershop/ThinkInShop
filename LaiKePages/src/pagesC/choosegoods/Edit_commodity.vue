<template>
    <view>
        <heads :title="language.Edit_commodity.titile" :border="true" :bgColor='bgColor' titleColor="#000" ishead_w="2"></heads>
        <view class="pages">
            <view class="pages_box">
                <view class="pages_box_box">
                    <view class="pages_box_img">
                        <image :src="list.imgurl" style="width: 152rpx;height: 152rpx;border-radius: 16rpx;"></image>
                    </view>
                    <view class="pages_box_detal">
                        <view class="pages_box_detal_txt">{{list.product_title}}</view>
                        <view class="pages_box_detal_price">
                            <view class="pages_box_detal_price_left">
                                <view class="pages_box_detal_price_jg">{{language.Integral_goods.jf}}{{list.integral}} + {{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{list.money}}</view>
                                <view class="pages_box_detal_price_kc">{{language.Edit_commodity.kc}}:{{list.attrNum}}</view>
                            </view>
                            <view>
                            </view>
                        </view>
                    </view>
                </view>
                <view class="addCoupon_list">
                    <view class="addCoupon_list_left">
                        <text>*</text>
                        {{language.Edit_commodity.dhje}}
                    </view>
                    <view class="input_box">
                        <input  class="addCoupon_list_ipt" v-model="z_money" @blur="handleInput($event,'z_money')" type="digit"
                            placeholder="0.00" placeholder-style="color:#FA5151">
                        {{language.storeCoupon.yuan}}
                    </view>
                </view>
                <view class="addCoupon_list">
                    <view class="addCoupon_list_left">
                        <text>*</text>
                        {{language.Edit_commodity.dhjf}}
                    </view>
                    <view class="input_box">
                        <input  class="addCoupon_list_ipt" v-model="integral" @blur="handleInput($event,'integral')" type="digit"
                            placeholder="0.00" placeholder-style="color:#FA5151">
                        {{language.Edit_commodity.jf}}
                    </view>
                </view>
                <view class="addCoupon_list" >
                    <view class="addCoupon_list_left">
                        <text>*</text>
                        {{language.Edit_commodity.sjkc}}
                    </view>
                    <view class="input_box" style="background-color: #f4f5f6;">
                        <input disabled="true" class="addCoupon_list_ipt" style="color:#333333;" v-model="inventory" type="number"
                            placeholder="0" placeholder-style="color:#333333;">
                    </view>
                </view>
                <view class="addCoupon_list" >
                    <view class="addCoupon_list_left">
                        {{language.Edit_commodity.sykc}}
                    </view>
                    <view class="input_box" style="background-color: #f4f5f6;">
                        <input disabled="true" class="addCoupon_list_ipt" style="color:#333333;" v-model="residue" type="number"
                            placeholder="0" placeholder-style="color:#333333;">
                    </view>
                </view>
                <view class="addCoupon_list" >
                    <view class="addCoupon_list_left">
                        {{language.Edit_commodity.zfkc}}
                    </view>
                    <view class="input_box">
                        <input class="addCoupon_list_ipt" style="color:#333333;" v-model="addNum" @blur="handleInput($event,'addNum')" type="number"
                            placeholder="0" placeholder-style="color:#333333;">
                    </view>
                </view>
            </view>
            <!-- 提示 -->
            <view class="xieyi" style="background-color: initial;z-index: 9999;" v-if="is_sus">
                <view style="width: 272rpx;height: 272rpx;background-color: rgba(51, 51, 51, .9);">
                    <view style="margin: 32rpx 0;text-align: center;margin-top: 64rpx;">
                        <image style="width: 68rpx;height: 68rpx;" :src="sus"></image>
                    </view>
                    <view class="xieyi_title" style="margin-bottom: 0;margin-top: 0;color: #fff;font-weight: 500;font-size: 32rpx;">{{language.Edit_commodity.bccg}}</view>
                </view>
            </view>
            <view class="btn">
                <div class='bottom' @tap="toAddPro">{{language.Edit_commodity.bc}}</div>
            </view>
        </view>
    </view>
</template>

<script>
    import {
        oninput
    } from '@/common/util'
    export default {
        data() {
            return {
                z_money:'',
                integral:'',
                is_sus:false,
                addNum:'',
                inventory:'',
                residue:'',
                bgColor: [{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                list:'',
                sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
                Activities_set: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/Activities_set.png',
            }
        },
        onLoad(e) {
            this.id = e.id
            this._axios()
        },
        methods: {
             handleInput(e,val) {
                 // 只能返回正 整数 和小数点后两位的结果 
                this[val] = oninput(e.detail.value,2)    
            },
            _axios () {
                var data = {
                    api:'plugin.integral.AppIntegral.index',
                    goodsName: '',
                    id:this.id,
                }
                this.$req.post({data}).then(res=>{
                    if (res.code == 200) {
                        this.list = res.data.list[0]
                        this.z_money = this.list.money
                        this.integral = this.list.integral
                        this.inventory = this.list.max_num
                        this.residue = this.list.num
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        })
                    }
                })
                
            },
            toAddPro(){
                var data = {
                    api:'plugin.integral.AppIntegral.addIntegral',
                    goodsid: this.list.goods_id,
                    id:this.id,
                    attrId:this.list.attrId,
                    stockNum:this.inventory,
                    integral:this.integral,
                    money:this.z_money,
                    addNum:this.addNum,
                }
                this.$req.post({data}).then(res=>{
                    if (res.code == 200) {
                        this.is_sus = true
                        setTimeout(()=>{
                            this.is_sus = false
                            uni.navigateBack({
                            })
                        },1500)
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        })
                    }
                })

            }
        }
    }
</script>
<style>
    page {
        background-color: #f4f5f6;
    }
</style>
<style scoped lang="less">
    @import url("@/laike.less");
    .xieyi{
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background-color: rgba(0, 0, 0, .5);
        z-index: 99;
        display: flex;
        justify-content: center;
        align-items: center;
        >view{
            width: 640rpx;
            min-height: 100rpx;
            max-height: 486rpx;
            background: #FFFFFF;
            border-radius: 24rpx;
            .xieyi_btm{
                height: 108rpx;
                color: @D73B48;
                display: flex;
                justify-content: center;
                align-items: center;
                border-top: 0.5px solid rgba(0,0,0,.1);
                font-weight: bold;
                font-size: 32rpx;
            }
            .xieyi_title{
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
            .xieyi_text{
                width: 100%;
                max-height: 236rpx;
                overflow-y: scroll;
                padding: 0 32rpx;
                box-sizing: border-box;
            }
        }
    }
    .input_box{
        // width: auto;
        width: 510rpx;
        border: 1rpx solid #eee;
        padding: 16rpx;
        border-radius: 16rpx;
        display: flex;
        justify-content: center;
        align-items: center;
    }
    .pages_box{
        width: auto;
        display: flex;
        flex-direction: column;
        background-color: #fff;
        padding: 32rpx;
        border-radius: 24rpx;
        margin-top: 24rpx;
    }
    .pages{
        width: 100%;
    }

    .pages_box_detal_price_left{
        display: flex;
        justify-content: center;
        align-items: center;
    }
    .pages_box_detal_price_kc {
        font-size: 24rpx;
        font-weight: normal;
        color: #999999;
        margin-left: 20rpx;
    }

    .pages_box_detal_price_jg {
        font-size: 32rpx;
        font-weight: 500;
        font-weight: normal;
        color: #FA5151;

    }

    .pages_box_detal_price {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-top: 24rpx;
    }

    .pages_box_detal_txt {
        text-overflow: -o-ellipsis-lastline;
        overflow: hidden; //溢出内容隐藏
        text-overflow: ellipsis; //文本溢出部分用省略号表示
        display: -webkit-box; //特别显示模式
        -webkit-line-clamp: 2; //行数
        line-clamp: 2;
        -webkit-box-orient: vertical; //盒子中内容竖直排列
        font-size: 32rpx;
         
        font-weight: normal;
        color: #333333;
    }

    .pages_box_detal {
        margin-left: 24rpx;
        width: 510rpx;
        display: flex;
        flex-direction: column;
    }

    .pages_box_img {
        display: flex;
        justify-content: center;
        align-items: center;
    }

    .pages_box_box {
        background-color: #fff;
        width: auto;
        height: 152rpx;
        border-bottom: 1rpx solid #eee;
        align-items: center;
        display: flex;
        padding-bottom: 16rpx;
    }



    .btn {
        position: fixed;
        bottom: 0;
        width: 100%;
        height: 124rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #ffffff;
        box-shadow: 0 0 0 0 rgba(0, 0, 0, 0.2);
        padding-bottom: constant(safe-area-inset-bottom);
        /* 兼容 iOS < 11.2 */
        padding-bottom: env(safe-area-inset-bottom);
    }

    .bottom {
        width: 686rpx;
        height: 92rpx;
        font-size: 32rpx;
        color: #fff;
        text-align: center;
        line-height: 92rpx;
        border-radius: 52rpx;
        margin: 0 auto;
        padding: 0;
        .solidBtn()
    }
    .addCoupon{
        
        &_list{
            display: flex;
            align-items: center;
            .size(28rpx);
            box-sizing: border-box;
            margin-top: 24rpx;
            &_left{
                width: 172rpx;
                .size(28rpx);
                color: #020202;
                
                text{
                    color: #FF0000;
                    margin-right: 10rpx;
                }
                text.none{
                    opacity: 0;
                }
            }
            
            &_ipt{
                flex: 1;
                font-size: 28rpx;
                display: flex;
                    align-items: center;
                    color: #FA5151;
                .radioBox{
                    display: flex;
                    align-items: center;
                    label{
                        
                    }
                    
                    .scale8{
                        transform: scale(0.8);
                        width: 44rpx;
                        margin-left: -5rpx;
                        margin-right: 5rpx;
                    }
                }
            }
        }
        
        &_btn {
            position: fixed;
            bottom: 0;
            width: 100%;
            height: 124rpx;
            display: flex;
            align-items: center;
            justify-content: center;
            background-color: #ffffff;
            box-sizing: unset;
            box-shadow: 0 0 0 0 rgba(0,0,0,0.2);
            padding-bottom: constant(safe-area-inset-bottom); /* 兼容 iOS < 11.2 */
            padding-bottom: env(safe-area-inset-bottom);
        
        view{
            width: 686rpx;
            height: 92rpx;
            font-size: 32rpx;
            color: #fff;
            text-align: center;
            line-height: 92rpx;
            border-radius: 52rpx;
            margin: 0 auto;
            padding:0;
            .solidBtn()
        }
    
    }
    
    }
    @media screen and (min-width: 600px) {
        .xieyi{
            top:100%;
        }
    }
</style>
