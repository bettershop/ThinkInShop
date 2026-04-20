<template>
    <view class="pages" :style="{width: width_max,height: height_max}" @click.stop="closeclick">
        <scroll-view  :scroll-top="scrollTopHeight"  scroll-y="true" class="scrollbox" :style="{height: height_max,width: width_max}">
            <!-- 用户聊天 -->
            <view class="overflowH" :id="index==ChatArray.length-1?'bottom':''" v-for="(item,index) in ChatArray" :key="index" v-if="ChatArray&&ChatArray.length&&is_store==0">
                <view class="scrollbox_left" v-if="item.is_mch_send==1" :style="!width_max?'width: 440rpx;':''">
                    <image v-if="is_xs" :src="item.img" class="scrollbox_left_img"></image>
                    <view class="scrollbox_left_box">
                        <view class="scrollbox_left_box_name" v-if="is_xs&&item.is_mch_send==1">{{item.nike_name}}
                        </view>
                        <image v-if="item.content.text&&item.content.text.slice(0,4)=='http'"
                            @click.stop="select_img(item.content)" :src="item.content.text" class="scrollbox_rigth_box_image">
                        </image>
                        <view  v-else-if="item.content.text && item.content.text.length>0"  class="scrollbox_rigth_box_conten">{{item.content.text}}</view>

                    </view>
                </view>
                <view class="scrollbox_rigth" v-if="item.is_mch_send==0">
                    <view class="scrollbox_rigth_box">
                        <image v-if="item.content.text&&item.content.text.slice(0,4)=='http'"
                            @click.stop="select_img(item.content)" :src="item.content.text" class="scrollbox_rigth_box_image">
                        </image>
                        <view  v-else-if="item.content.text && item.content.text.length>0"  class="scrollbox_rigth_box_conten">{{item.content.text}}</view>
                        <!-- 订单信息 --> 
                        <view v-else-if='item.content.order && item.content.order.orderId'  class="scrollbox_rigth_box_conten" >
                            <view class="">
                                <view class="">我要咨询这笔订单</view>
                                <view class="goods-info">
                                    <view class="information">
                                        <view >
                                            <image :src="item.content.order.imgUrl || ErrorImg" @error='handleErrorImg(index)' />
                                        </view>
                                        <view style="margin-left: 16rpx;">
                                            <!-- <view class="product-name">{{item.content.order.orderName}}</view> -->
                                            <view class="price">合计:{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(item.content.order.price || 0)}}</view>
                                            <view class="product-spec">{{language.collection.g}}{{item.content.order.num}}{{language.chooseGoods.jsp}}</view>
                                        </view>  
                                    </view>
                                    <view class='order-id'>{{language.yushou_order.ddh}} &nbsp;{{item.content.order.orderNo}}</view>
                                    <view>{{language.myStore.addAccount.cjsj}} &nbsp; {{item.content.order.addTime | dateFormat}}</view>
                                    <view class="controls-but"  >
                                        <view @tap='selectOrderInfo(item.content.order,false)' style="font-size: 28rpx;">
                                            {{language.group_end.checkOrder}}
                                        </view>
                                    </view>
                                </view>
                            </view>
                        </view>
                        <!-- 商品显示 -->
                        <view class="scrollbox_rigth_box_conten " v-if="item.content.pro && item.content.pro.product_title " >
                            <view class=""> 
                                <view class="goods-info">
                                    <view class="information">
                                        <view >
                                            <image :src="item.content.pro.imgUrl ||item.content.pro.imgurl || ErrorImg" @error='temporarilyErrorImg(index)' />
                                        </view>
                                        <view  class="temporarily-info">
                                            <view class="">                                                
                                                <view class="product-name temporarily-name"  >{{item.content.pro.product_title}} </view>
                                                <view  style="display: flex;align-items: end;">
                                                    <view class="">                                                
                                                        <view class="price">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(item.content.pro.price || 0)}}</view> 
                                                        <view class=" ">{{language.collection.g}}{{item.content.pro.payPeople ||0}}{{language.goods.goods.rzf}}</view>
                                                    </view> 
                                                </view>  
                                            </view> 
                                            <view class="controls-but ">
                                                <!-- 去购买 -->
                                                <view @tap='goBuy(item.content.pro)' class="go-buy">
                                                    {{language.liveReplay.go_buy}}
                                                </view>
                                            </view>
                                        </view>   
                                    </view> 
                                
                                </view>
                            </view>
                    </view>
                     
                    <image v-if="is_xs" :src="user_img" class="scrollbox_rigth_img"></image>
                </view>
                </view>
                
            </view>
            <!-- 店铺聊天 -->
            <view class="overflowH is_mch" v-for="(item,index) in ChatArray" :key="index" v-if="ChatArray&&ChatArray.length&&is_store==1">
                <view class="scrollbox_left" v-if="item.is_mch_send==0" :style="!width_max?'width: 440rpx;':''">
                    <image v-if="is_xs" :src="item.img" class="scrollbox_left_img"></image>
                    <view class="scrollbox_left_box">
                        <view class="scrollbox_left_box_name" v-if="is_xs">{{item.nike_name}}</view>
                        <image v-if="item.content.text&&item.content.text.slice(0,4)=='http'"
                            @click.stop="select_img(item.content)" :src="item.content.text" class="scrollbox_rigth_box_image">
                        </image>
                        <view  v-else-if="item.content.text && item.content.text.length>0"  class="scrollbox_rigth_box_conten">{{item.content.text}}</view>
                       <!-- 订单信息 -->
                       <view v-else-if='item.content.order && item.content.order.orderId'  class="scrollbox_rigth_box_conten" >
                           <view class="">
                               <view class="">我要咨询这笔订单</view>
                               <view class="goods-info">
                                   <view class="information">
                                       <view >
                                           <image :src="item.content.order.imgUrl || ErrorImg" @error='handleErrorImg(index)' />
                                       </view>
                                       <view style="margin-left: 16rpx;">
                                           <view class="product-name">{{item.content.order.orderName}}</view>
                                           <view class="price">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(item.content.order.price || 0)}}</view>
                                           <view class="product-spec">{{language.collection.g}}{{item.content.order.num}}{{language.chooseGoods.jsp}}</view>
                                       </view>  
                                   </view>
                                   <view class='order-id'>{{language.yushou_order.ddh}} &nbsp;{{item.content.order.orderNo}}</view>
                                   <view>{{language.myStore.addAccount.cjsj}} &nbsp; {{item.content.order.addTime | dateFormat}}</view>
                                   <view class="controls-but">
                                       <view @tap='selectOrderInfo(item.content.order,true)' style="font-size: 28rpx;">
                                           {{language.group_end.checkOrder}}
                                       </view>
                                   </view>
                               </view>
                           </view>
                       </view>
                       <!-- 商品显示 -->
                       <view class="scrollbox_rigth_box_conten " v-if="item.content.pro && item.content.pro.product_title  " >
                           <view class=""> 
                               <view class="goods-info">
                                   <view class="information">
                                       <view >
                                           <image :src="item.content.pro.imgUrl ||item.content.pro.imgurl || ErrorImg" @error='temporarilyErrorImg(index)' />
                                       </view>
                                       <view  class="temporarily-info">
                                           <view class="">                                                
                                               <view class="product-name temporarily-name" >{{item.content.pro.product_title}} </view>
                                               <view  style="display: flex;align-items: end;">
                                                   <view class="">                                                
                                                       <view class="price">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(item.content.pro.price || 0)}}</view> 
                                                       <view class=" ">{{language.collection.g}}{{item.content.pro.payPeople ||0}}{{language.goods.goods.rzf}}</view>
                                                   </view> 
                                               </view> 
                                           </view> 
                                       </view>   
                                   </view> 
                               
                               </view>
                           </view>
                           </view>
                    </view>
                </view>
                <view class="scrollbox_rigth" v-if="item.is_mch_send==1">
                    <view class="scrollbox_rigth_box">
                        <image @click.stop="select_img(item.content)" v-if="item.content&&item.content.length>4&&item.content.slice(0,4)=='http'"
                            :src="item.content" class="scrollbox_rigth_box_image"></image>
                        <view v-else class="scrollbox_rigth_box_conten">{{item.content.text}}</view>
                        
                    </view>
                    <image v-if="is_xs" :src="userimg" class="scrollbox_rigth_img"></image>
                </view>
            </view>
            <view id='bottom' style="height: 20rpx;"></view>
        </scroll-view>
    </view>
</template>

<script>
    export default {
        props: {
            //最大宽度
            "width_max": {
                type: String,
                require: true
            },
            //最大高度
            "height_max": {
                type: String,
                require: true
            },
            //头像是否显示
            "is_xs": {
                type: Boolean,
                require: true
            },
            //聊天记录
            "ChatArray": {
                type: Array,
                default: [],
            },
            //是否店铺
            "is_store": {
                type: Number,
                require: true
            },
            "user_img":{
                type:String,
            },
            "temporarily":{
                type:Object, 
            }
        },
        data() {
            return { 
                // width_max: '',//盒子宽。
                scrollTop: 9999,
                scrollTopHeight:'',
                red_gth: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/red_gth.png',
                ErrorImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/Default_picture.png',
            }
        },
        watch: {
            height_max(e){
                console.log('监听到 height 发生变化', e);
                this.scrollTopHeight = this.ChatArray.length * 120 + Math.random()
            },
            ChatArray(e){
                console.log('监听到 聊天内容 发生变化','ChatArray');
                this.$nextTick(()=>{
                    this.scrollTopHeight = this.ChatArray.length * 120 + Math.random()
                })
            },
            temporarily(){
                console.log('监听到 temporarily 发生变化');
            }
        },
        updated() {
        },
        mounted(){
           
        },
        methods: {
            // 图片报错处理
            handleErrorImg(e){
                console.log('图片报错');
                setTimeout(()=>{
                   this.ChatArray[index].orderMsg.imgUrl =this.ErrorImg
                },0)
            },
            temporarilyErrorImg(e){
                console.log('图片报错');
                setTimeout(()=>{
                   this.temporarily.imgUrl =this.ErrorImg
                },0)
            },
            select_img(e) {
                // debugger
                let imgsArray = [];
                imgsArray[0] = e.text;
                uni.previewImage({
                    current: 0,
                    urls: imgsArray
                });
            },
            closeclick() {
                this.$emit('is_expressionboxa', false)
                this.$emit('is_photoboxa', false)
            },
            selectOrderInfo(item,falg){
                if(falg){
                    // 商家点击
                    this.navTo('/pagesA/myStore/order?order_id='+item.orderNo)
                }else{
                    // 用户点击
                      this.navTo('/pagesB/order/order?order_id='+item.orderId)
                }
            },
            // 去购买
            goBuy(item){
             this.navTo('/pagesC/goods/goodsDetailed?toback=true&pro_id='+item.id)
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
    .goods-info {
        .order-id {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            width: 340rpx;
            margin-bottom: 20rpx;
            color:#000
        }
        image {
            border-radius: 25rpx;
            width: 140rpx;
            height: 140rpx;
        } 
        .information {
            display: flex;
        }
        .temporarily-info{
            margin-left: 16rpx;
        }
        .temporarily-name{
            margin-bottom: 20rpx;
             width:270rpx !important
        }
        .controls-but{
            display: flex; 
                justify-content: end;
                margin-top: 24rpx;
            view{ 
                background-color: #FFEDED;
                color: #FA5151;
                margin: 10rpx;
                padding: 4rpx 20rpx;
                border-radius: 20rpx;
            }
        }
         .go-buy{
             background: linear-gradient(to right, #eeb12e, #f8574f);
             color: #fff !important; 
             margin: 0rpx !important;
             margin-left: 50rpx !important;
         }
        view {
            view {  
             .price{
                 color: #000;
                 font-size: 30rpx;  
                 font-weight: 800;
             }
             .product-name,.product-spec {
                 display: block;
                 white-space: nowrap;
                 overflow: hidden;
                 text-overflow: ellipsis;
                 width: 188rpx !important;
                 
             }
             .product-name{
                    font-family: system-ui;
                 color: #323232;
             }
             .product-spec{
                 color: #999999;
             } 
            }
            
        } 
    }
    .pages {
        // width: 100%;
        display: flex;
        height: 80vh;
        justify-content: flex-end;
        flex-direction: column;
        z-index: 10;
        padding-left: 5rpx;
        box-sizing: border-box;
    }

    .scroll_rigth_box_img {
        width: 32rpx;
        height: 32rpx;
        position: absolute;
        left: -40rpx;
        top: 26rpx;
    }

    .scrollbox {
        display: flex;
        flex-direction: column;
        width: 750rpx;
        height: 500rpx;
        background-color: #fff;
        z-index: 98;
    }
    
    .overflowH{
        overflow: hidden;
    }

    .scrollbox_left {
        display: flex;
        padding-top: 42rpx;
    }

    .scrollbox_left_img {
        width: 68rpx;
        height: 68rpx;
        border-radius: 52rpx;
    }

    .scrollbox_left_box {
        margin-left: 20rpx;
        display: flex;
        flex-direction: column;
        border-radius: 0rpx 16rpx 16rpx 16rpx;
        max-width: 384rpx;
    }

    .scrollbox_left_box_name {
        font-size: 24rpx;
         
        font-weight: 400;
        color: #707070;
    }

    .scrollbox_left_box_conten {
        margin-top: 4rpx;
        font-size: 24rpx;
         
        font-weight: 400;
        line-height: 36rpx;
        color: #333333;
        z-index: 99;
        padding: 24rpx;
        border-radius: 0rpx 16rpx 16rpx 16rpx;
        
        word-wrap: break-word;
    }

    .scrollbox_left_box_image {
        width: 100rpx;
        height: 100rpx;
        margin-top: 4rpx;
        font-size: 24rpx;
         
        font-weight: 400;
        line-height: 36rpx;
        color: #333333;
        background-color: #FFFFFF;
        z-index: 99;
        padding: 24rpx;
        border-radius: 0rpx 16rpx 16rpx 16rpx;
    }

    .scrollbox_rigth {
        display: flex;
        justify-content: flex-end;
        margin-top: 32rpx;
    }

    .scrollbox_rigth_img {
        width: 68rpx;
        height: 68rpx;
        border-radius: 52rpx;
    }

    .scrollbox_rigth_box {
        position: relative;

        margin-right: 24rpx;
        display: flex;
        flex-direction: column;
        max-width: 404rpx;
    }

    .scrollbox_rigth_box_name {
        font-size: 24rpx;
         
        font-weight: 400;
        color: #707070;
    }

    .scrollbox_rigth_box_conten {
        margin-top: 4rpx;
        font-size: 24rpx;
         
        font-weight: 400;
        line-height: 36rpx;
        color: #333333;
        background-color: #FFFFFF;
        z-index: 99;
        padding: 24rpx;
        border-radius: 16rpx 0rpx 16rpx 16rpx;
        position: relative;
        
        word-wrap: break-word;
    }

    .scrollbox_rigth_box_image {
        width: 100rpx;
        height: 100rpx;
        margin-top: 4rpx;
        font-size: 24rpx;
         
        font-weight: 400;
        line-height: 36rpx;
        color: #333333;
        background-color: #FFEDED;
        z-index: 99;
        padding: 24rpx;
        border-radius: 16rpx 0rpx 16rpx 16rpx;
        position: relative;
    }
    .is_mch .scrollbox_rigth_box_conten{
            border-radius: 0rpx 16rpx  16rpx 16rpx !important;
        }
</style>
