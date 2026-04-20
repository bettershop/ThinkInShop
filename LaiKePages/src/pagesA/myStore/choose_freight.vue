<template>
    <view class="container">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="language.choose_freight.title" ishead_w="2" :bgColor="bgColor" titleColor="#333333"></heads>
        
        
        <view class="main" v-if="list.length > 0">
            <view class="main-item" v-for="(item, index) of list" :key="index" >
                <image :src="item.imgurl" mode="" class="product-img"></image>
                <view class="main-content">
                    <view class="content-title">{{ item.product_title }}</view>
                    <view class="money">
                        <span class="price">{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{ LaiKeTuiCommon.formatPrice(item.price) }}</span>
                        <span class="kucun">{{language.choose_shopping.kucun}}{{item.num}}</span>
                    </view>
                </view>
            </view>            
        </view>
        
        
        <view class="list" v-if="list&&list.length <= 4">
            <view class="list_left">
                <span class="must">*</span>
                <span>{{language.choose_freight.freight}}</span>
            </view>
            <view class="list_right" @click="_openMethod">
                    <view class="left_box">
                        <view v-if="freightIndex < 0">{{language.choose_freight.freightPH}}</view>
                        <view v-else>{{freightArr[freightIndex]}}</view>
                        <view style="line-height: 0 !important;"><img :src="jiantou" /></view>
                    </view>
            </view>
        </view>
        <!-- 距离底部按钮高度 -->
        <view style="width: 750rpx;" :style="{height:Undershoot}"></view>
        
        <view class="bottomBox">
            <view class="list" v-if="list&&list.length > 4">
                <view class="list_left">
                    <span class="must">*</span>
                    <span>{{language.choose_freight.freight}}</span>
                </view>
                <view class="list_right" @click="_openMethod">
                           <view class="left_box">
                            <view v-if="freightIndex < 0">{{language.choose_freight.freightPH}}</view>
                            <view style="line-height: 0 !important;" v-else>{{freightArr[freightIndex]}}</view>
                             <img :src="jiantou" />
                           </view>
                </view>
            </view>
            <view class="btnBox" @tap="add">
                <view class="bottomBoxBtn">
                    {{language.delModel.confirm}}
                </view>
            </view>
        </view>
        <view class="xieyi mask" style="background-color: initial;" v-if="is_sus">
            <view style="width: 272rpx;height: 272rpx;background-color: rgba(51, 51, 51, .9);">
                <view style="margin: 32rpx 0;text-align: center;margin-top: 64rpx;">
                    <image style="width: 68rpx;height: 68rpx;" :src="sus"></image>
                </view>
                <view class="xieyi_title"
                    style="margin-bottom: 0;margin-top: 0;color: #fff;font-weight: 500;font-size: 32rpx;">
                    {{language.putForward.tjcg}}
                </view>
            </view>
        </view>
        <chooseS
            ref="isLanguage" 
            :is_choose_obj='is_choose_obj' 
            :is_type="chooseType" 
            :is_choose="freightArr"
            :is_choose_index="is_choose_index"
             @_choose="_choose" 
             @_isHide="_isHide">
        </chooseS>
    </view>
</template>

<script>
import { mapMutations } from 'vuex'
import chooseS from "@/components/aComponents/choose.vue"
export default {
    data() {
        return {
            is_choose_obj:{},
            chooseType:0,
            is_choose_index:0,
            fastTap: true,
            shop_id: '',
            pro_id: '',
            urlString: '',
            freight_list: [],
            freightArr: [],
            list:[],//所选商品集合
            freightIndex: -1,
            jiantou:
                this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL +
                "images/icon/jiantou2x.png",
            bgColor: [
                {
                    item: '#ffffff'
                },
                {
                    item: '#ffffff'
                }
            ],
            Undershoot:'',
            is_sus:false,
            sus: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/sus.png',
        };
    },
    components:{
        chooseS,
    },
    onLoad(option) {
        this.shop_id = option.shop_id;
        this.pro_id = option.pro_id;
        this.urlString = '?up1=' + option.up1 + '&up2=' + option.up2;
        this.list=uni.getStorageSync('selectArray') 
    },
    mounted() {
        // 设置元素距离下边的距离
        uni.createSelectorQuery().in(this).select(".bottomBox").boundingClientRect(data => {
          this.heightEle = data.height                 
          this.Undershoot=this.heightEle+'px'
        }).exec()
    },
    onShow() {
        this.isLogin(()=>{})
        this.axios();
        this.is_choose_obj = {
            title: this.language.freight_add.qxyfmb,
            colorLeft: '#999999',
            colorRight: '#FA5151',
            background: '#F4F5F6',//显示图标
            borderRadius: '24rpx 24rpx 0 0',//提示文字
        }
    },
    onUnload() {
        uni.removeStorageSync('selectArray')
    },
    methods: {
        // 选择计算方式
        _choose(index) {
            this.chooseClassFlag = false;
            this.freightIndex = index;
            this.is_choose_index = index
            this.chooseType = 0
            this.typeIndex = index
        },
        // 打开选择运费模板
        _openMethod() {
            this.chooseType = 2
        },
        //隐藏
        _isHide(){
            this.chooseType = 0
        },
        // 关闭计算方式
        _closeMethod() {
            this.chooseClassFlag = false;
        },
        ...mapMutations({ setchooseProFlag: 'setchooseProFlag' }),
        changeF(e){
            this.freightIndex = e.detail.value;
        },
        changeLoginStatus(){
            this.axios();
        },
        axios(){
            let data = {
                api:"mch.App.Mch.Upload_merchandise_page",
                shop_id: this.shop_id
            }
            this.$req.post({data}).then(res=>{
                let { code, data, message } = res;
                if(code == 200){
                    this.freightArr = [];
                    data.freight_list.filter((item,index)=>{
                        this.freightArr.push(item.name)
                        if(item.is_default == 1) {
                            this.freightIndex = index
                            this.is_choose_index = index
                        }
                    })
                    this.freight_list = data.freight_list;
                    
                }else{
                    uni.showToast({
                        title: message,
                        icon: 'none'
                    })
                }
            })
        },
        add() {
            if(this.freightIndex < 0){
                uni.showToast({
                    title: this.language.choose_freight.freightPH,
                    icon: 'none'
                })
                return
            }
            if (!this.fastTap) {
                return;
            }
            this.fastTap = false;
            var data = {
                // module: 'app',
                // action: 'mch',
                // m: 'add_goods',
                api:'mch.App.Mch.add_goods',
                shop_id: this.shop_id,
                pro_id: this.pro_id,
                freight_id: this.freight_list[this.freightIndex].id
            };
            
            this.$req.post({data}).then(res=>{
                if (res.code == 200) {
                    this.is_sus = true
                    setTimeout(() => {
                        this.is_sus = false
                        this.setchooseProFlag(true)
                        uni.redirectTo({
                            url: '/pagesA/myStore/myPro' + this.urlString
                        });
                    }, 1500);
                } else {
                    uni.showToast({
                        title: res.message,
                        duration: 1500,
                        icon: 'none'
                    });
                    return;
                }
            })
        }
    },
};
</script>

<style scoped lang="less">
    @import url("@/laike.less");
    .container{
        min-height: 100vh;
        background-color: #F4F5F6;
        .list_right{
            display: block !important;
            padding: 14rpx 16rpx !important;
            img{
                width: 32rpx;
                height: 44rpx;
            }
        }
        .left_box{
            display: flex;
            justify-content: space-between;
            align-items: center;
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
        
        .list{
            display: flex;
            align-items: center;
            min-height: 90rpx;
            background: #FFFFFF;
            margin-top: 16rpx;
            border-radius: 24rpx;
            padding: 32rpx;
            box-sizing: border-box;
            .must{
               color: #FA5151;
               margin-right: 10rpx;
            }
            &_left{
                width: 156rpx;
                font-size: 32rpx;
                
                font-weight: 400;
                line-height: 28rpx;
                margin-right: 32rpx;

            }
            
            &_right{
                border: 1rpx solid  rgba(0, 0, 0, 0.1);
                border-radius: 16rpx;
                padding: 16rpx;
                flex: 1;
                overflow: hidden;
                display: flex;
                align-items: center;
                
                picker{
                    flex: 1;
                }
                view{
                    font-size: 32rpx;
                    color: #333333;
                    line-height: 32rpx;
                }
                
                image{
                    width: 15rpx;
                    height: 27rpx;
                    margin-left: auto;
                }
                
            }
            
        }
        
        .bottom{
            position: fixed;
            left: 0;
            bottom: 0;
            right: 0;
            height: 98rpx;
            background: @btnBackground;
            .center();
            font-size: 30rpx;
            color: #FFFEFE;
        }
        
    }
    .main {
        position: relative;
        width: 750rpx;
        border: 1px solid rgba(0,0,0,0);//解决外边距塌陷问题
         background: #FFFFFF;
         border-radius: 0 0 24rpx 24rpx ;
        .noFindImg{
            margin-top: 300rpx;
            width: 750rpx;
            height: 460rpx;
        }
        .noFindText{
            font-size: 28rpx;
            
            font-weight: 400;
            color: #333333;
            margin-top: -46rpx;
            display: block;
        }
    }
    
    .main-item {
        display: flex;
        justify-content: space-between;
        align-items: center; 
       
        border-bottom: 1rpx solid rgba(0,0,0,0.1);
        padding: 32rpx 0;
        margin: 0 32rpx;
        &:nth-last-child(1){
            border: 0;
        }
    }
    .main-item .focus {
        width: 34rpx;
        height: 34rpx;
    }
    .main-item .product-img {
        width: 152rpx;
        height: 152rpx;
        margin-right: 24rpx;
        margin-left: 32rpx;
        border-radius: 16rpx;
        overflow: hidden;
    }
    .main-item .main-content {
        align-self: start;
        flex: 1;
        min-height: 120rpx;
    }
    .main-content .content-title {
        font-size: 32rpx;
        
        font-weight: 400;
        color: #333333;
    }
    
    .main-content .money {
        padding-top: 20rpx;
        font-size: 32rpx;
        
        font-weight: 500;
        color: #FA5151;
        display: flex;
        align-items: center;
        justify-content: space-between;
        .kucun{
            font-size: 24rpx;
            
            font-weight: 400;
            color: #999999;
        }
    }
    .bottomBox {
        position: fixed;
        bottom: 0;
        width: 100%; 
        background-color:  #F4F5F6;
        box-sizing: unset;
        box-shadow: 0 0 0 0 rgba(0,0,0,0.2);
        
        .btnBox{
            width: 750rpx;
            height: 124rpx;
            margin-top: 12rpx;
            background-color: #ffffff;
            display: flex;
            align-items: center;
            padding-bottom: constant(safe-area-inset-bottom); /* 兼容 iOS < 11.2 */
            padding-bottom: env(safe-area-inset-bottom);
        }
        .bottomBoxBtn {
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
</style>
