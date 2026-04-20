<template>
    <!-- is_showToast 1/2/3 注册页面register.vue有用到-->
    <!-- is_showToast 4 店铺预售商品yushou_goods.vue有用到-->
    <!-- is_showToast 5 个人资料myInfo.vue有用到-->
    <view class="showToast">
        <!-- 成功/失败提示弹窗-->
        <!-- _obj 需要传入参数 imgUrl  title -->
        <template v-if="is_showToast == 1">
            <view class="xieyi isShowToast">
                <view class="isShowToast_view">
                    <view class="isShowToast_view_img">
                        <image :src="is_showToast_obj.imgUrl"></image>
                    </view>
                    <view class="xieyi_title isShowToast_view_title">{{is_showToast_obj.title}}</view>
                </view>
            </view>
        </template>
        
        <!-- 富文本弹窗 一个按钮，点击就隐藏-->
        <!-- _obj 需要传入参数 title  contentNodes button -->
        <template v-if="is_showToast == 2">
            <view class="xieyi is_bgm" :class="{is_bgm_hide:is_hide}">
                <view class="is_oneBtn" :class="{is_Btn_hide:is_hide}">
                    <view class="xieyi_title">{{is_showToast_obj.title}}</view>
                    <view class="xieyi_text">
                        <toload :load="load">
                            <rich-text class="richtext" :nodes="is_showToast_obj.contentNodes" style="font-size: 14px;"></rich-text>
                        </toload>
                    </view>
                    <view class="xieyi_btm" @click="_richText()">{{is_showToast_obj.button}}</view>
                </view>
            </view>
        </template>
        
        <!-- 普通弹窗 一个按钮，点击就隐藏-->
        <!-- _obj 需要传入参数 title button -->
        <template v-if="is_showToast == 3">
            <view class="xieyi is_bgm" :class="{is_bgm_hide:is_hide}">
                <view class="is_twoBtn" :class="{is_Btn_hide:is_hide}">
                    <view class="xieyi_title" style="margin-bottom: 64rpx;">{{is_showToast_obj.title}}</view>
                    <view class="xieyi_btm" @click="_richText()">{{is_showToast_obj.button}}</view>
                </view>
            </view>
        </template>
        
        <!-- 取消/确认弹窗 两个按钮，取消直接隐藏，确认先隐藏再执行其他事件-->
        <!-- _obj 需要传入参数 title content button endButton -->
        <template v-if="is_showToast == 4">
            <view class="tishi_bg is_bgm" :class="{is_bgm_hide:is_hide}">
                <view class="tishi_nr is_oneBtn" :class="{is_Btn_hide:is_hide}">
                    <view>{{is_showToast_obj.title}}</view>
                    <view>{{is_showToast_obj.content}}</view>
                    <view class="tishi_btn">
                        <view @tap="_richText()">{{is_showToast_obj.button}}</view>
                        <view @tap="_richText('confirm')">{{is_showToast_obj.endButton}}</view>
                    </view>
                </view>
            </view>
        </template>
        
        <!-- 修改内容弹窗 input+两个按钮，取消直接隐藏，确认先隐藏再执行其他事件-->
        <template v-if="is_showToast == 5">
            <view class="tishi_bg is_bgm" :class="{is_bgm_hide:is_hide}">
                <view class="tishi_nr is_oneBtn" :class="{is_Btn_hide:is_hide}">
                    <view class="tishi_title">{{is_showToast_obj.title}}</view>
                    <input type="text" :placeholder="is_showToast_obj.placeholder" placeholder-style="color: #999999;" v-model="is_showToast_obj.content"/>
                    <view class="tishi_btn">
                        <view @tap="_richText()">{{is_showToast_obj.button}}</view>
                        <view @tap="_richText('confirm',is_showToast_obj.content)">{{is_showToast_obj.endButton}}</view>
                    </view>
                </view>
            </view>
        </template>
    </view>
</template>

<script>
    export default{
        props:{
            "is_showToast": {
                type: Number,
                default: 0,
                //0关闭弹窗  1成功失败提示弹窗  2富文本弹窗  3普通弹窗 一个按钮  4取消/确认弹窗 两个按钮
                //==1时 父组件需要使用setTimeout(()=>{this.is_showToast = 0},2000)，注意：此处时间必须是2秒
            },
            "is_showToast_obj": {
                type: Object,
                default: ()=>(
                    {
                        imgUrl: '',//显示图标
                        title: '',//提示文字
                        contentNodes: '',//富文本
                        button: '',//按钮
                        content: '',//文本内容
                        endButton: '',//确认按钮
                        placeholder: '',//input提示文字
                    }
                )
            },
            // --- 分割线 ---
            "load": {
                type: Boolean,
                default: false,
            },
        },
        data(){
            return {
                is_hide:false,//是否开启隐藏动画
            }
        },
        methods:{
            //content 传入父组件的内容
            //父组件调用方法时 请先设置is_showToast=0 关闭弹窗
            _richText(item,content){
                //执行隐藏动画
                this.is_hide = true
                //0.8秒后传值父组件，隐藏弹窗。并初始化is_hide
                setTimeout(()=>{
                    if(item == 'confirm'){
                        this.$emit("confirm",content)
                    } else {
                        this.$emit("richText",content)
                    }
                    this.is_hide = false
                },800)
            }
        }
    }
</script>

<style lang="less" scoped> 
    @import url("@/laike.less");
    //使用动画
    .isShowToast_view {
        position: relative;
        //单独动画写法
        //animation: anShowToast 0.3s both;
        //多段动画写法
        animation-name: anShowToast1,anHideToast1;
        animation-duration: 0.3s,0.5s;
        animation-delay: 0s,1.6s;
        animation-fill-mode: both,initial;
    }
    .is_bgm{
        animation: anShowToast4 0.3s both;
    }
    .is_bgm_hide{
        animation: anHideToast3 0.5s 0.3s both;
    }
    .is_oneBtn{
        animation: anShowToast3 0.3s 0.3s both;
    }
    .is_twoBtn{
        animation: anShowToast2 0.3s 0.3s both;
    }
    .is_Btn_hide{
        animation: anHideToast2 0.3s both;
    }
    //上下弹跳 显示动画
    @keyframes anShowToast1 {
        0% {
        opacity: 0;
        transform: translateY(100%)
        }
    
        60% {
        transform: translateY(-10%);
        }
    
        80% {
        transform: translateY(10%);
        }
    
        100% {
        opacity: 1;
        transform: translateY(0%)
        }
    }
    //左右弹跳 显示动画
    @keyframes anShowToast2 {
        0% {
        opacity: 0;
        transform: translatex(100%)
        }
    
        60% {
        transform: translatex(-10%);
        }
    
        80% {
        transform: translatex(10%);
        }
    
        100% {
        opacity: 1;
        transform: translatex(0%)
        }
    }
    //下滑 显示动画
    @keyframes anShowToast3 {
       0% {
       opacity: 0;
       margin-top: -200rpx;
       }
           
       100% {
       opacity: 1;
       margin-top: 0;
       }
    }
    //缓慢 显示动画
    @keyframes anShowToast4 {
       0% {
       opacity: 0;
       }
           
       100% {
       opacity: 1;
       }
    }
    //上滑 隐藏动画
    @keyframes anHideToast1 {
        0% {
        opacity: 1;
        transform: translateY(0%)
        }
    
        100% {
        opacity: 0;
        transform: translateY(-100%)
        }
    }
    //缩小 隐藏动画
    @keyframes anHideToast2 {
        0% {
        opacity: 1;
        transform: scale(1);
        }
    
        100% {
        opacity: 0;
        transform: scale(0);
        }
    }
    //左滑 隐藏动画
    @keyframes anHideToast3 {
        0% {
        transform: translateX(0%)
        }
            
        100% {
        transform: translateX(-100%)
        }
    }
    .showToast{
        .xieyi{
            position: fixed;
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
        .isShowToast{
            background-color: initial;
            .isShowToast_view{
                width: 272rpx;
                height: 272rpx;
                background-color: rgba(51, 51, 51, .9);
                .isShowToast_view_img{
                    margin: 32rpx 0;
                    text-align: center;
                    margin-top: 64rpx;
                    >image{width: 68rpx;height: 68rpx;}
                }
                .isShowToast_view_title{
                    margin-bottom: 0;
                    margin-top: 0;
                    color: #fff;
                    font-weight: 500;
                    font-size: 32rpx;
                }
            }
        }
        .tishi_bg{
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background-color: rgba(0, 0, 0, .5);
            z-index: 99;
            display: flex;
            justify-content: center;
            align-items: center;
            .tishi_nr{
                width: 640rpx;
                min-height: 100rpx;
                max-height: 486rpx;
                background: #FFFFFF;
                border-radius: 24rpx;
                >view:nth-child(1){
                    font-size: 32rpx;
                    font-weight: 500;
                    color: #333333;
                    line-height: 44rpx;
                    margin-top: 64rpx;
                    text-align: center;
                }
                >view:nth-child(2){
                    font-size: 32rpx;
                    font-weight: 400;
                    color: #999999;
                    line-height: 44rpx;
                    margin-top: 32rpx;
                    margin-bottom: 66rpx;
                    text-align: center;
                }
                >input{
                    width: 544rpx;
                    height: 66rpx;
                    margin: 0 auto;
                    margin-bottom: 62rpx;
                    border-bottom: 1px solid rgba(0, 0, 0, 0.1);
                    text-align: left;
                }
                .tishi_title{
                    margin-bottom: 32rpx;
                }
                .tishi_btn{
                    width: 100%;
                    height: 106rpx;
                    border-top: 2rpx solid rgba(0,0,0,.1);
                    display: flex;
                    >view{
                        flex: 1;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        
                        font-size: 32rpx;
                        font-weight: 500;
                        color: #D73B48;
                        line-height: 44rpx;
                    }
                    >view:first-child{border-right: 2rpx solid rgba(0,0,0,.1);color: #333333;}
                }
            }
        }
    
    }
</style>
