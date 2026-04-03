<template>
    <view class="page">
        <!-- 头部 -->
        <heads :title="title" :bgColor="bgColor" titleColor="#333333" ishead_w="2"></heads>
        <!-- 内容 -->
        <view class="content">
            <view class="content-erCodeImg">
                <view>
                    <image :class="erCodeData.status != 2 && erCodeData.status != 8 ? 'opcity':''" :src="erCodeData.extraction_code_img"></image>
                    <image v-if="erCodeData.status != 2 && erCodeData.status != 8" class="content-erCodeImg-yhxImg" :src="type!='VI'?yhxImg:xnyhx"></image>
                </view>
                <view>{{erCodeData.extraction_code}}</view>
                <view>{{language.erCode.jcwemcsgsjhx}}</view>
            </view>
            <view class="content-goods">
                <view class="content-goods-header">
                    <view>
                        <span>{{language.erCode.ddh}}：</span>
                        <span>{{erCodeData.sNo}}</span>
                    </view>
                    <view>
                        <span>{{language.erCode.spsl}}：</span>
                        <span>{{erCodeData.num}}</span>
                    </view>
                </view>
                <view class="content-goods-list" v-for="(item , index) in erCodeData.por_list">
                    <view>
                        <view>
                            <image :src="item.img"></image>
                        </view>
                        <view>
                            <view>{{item.product_title}}</view>
                            <view>
                                <span>{{currency_symbol}}{{LaiKeTuiCommon.getPriceWithExchangeRate(item.p_price,exchange_rate)}}</span>
                                <span>x{{item.num}}</span>
                            </view>
                            <view>
                                <span>{{item.size}}</span>
                                <span>
                                    <template v-if="type == 'VI'">
                                        {{
                                            item.status == 1 ? language.erCode.dsy
                                            :item.status == 2 ? language.erCode.ysy:language.erCode.bcz
                                        }}
                                    </template>
                                </span>
                            </view>
                        </view>
                    </view>
                    <view v-if="type == 'VI'" @tap="_navigateTo(item.is_write == 2?'/pagesB/erCode/hxJl?orderDetailId=' + item.dId:'')">
                        <view>待核销次数</view>
                        <view>
                            <span>
                                <template v-if="item.status == 1">
                                    {{item.write_off_num}}
                                </template>
                                <template v-else-if="item.status == 2">
                                    {{language.erCode.yhx}}
                                </template>
                            </span>
                            <image v-if="item.is_write == 2" :src="baga"></image>
                        </view>
                    </view>
                </view>
            </view>
        </view>
    </view>
</template>

<script>
export default {
    data() {
        return {
            bgColor: [
                {
                    item: "rgba(255,255,255, 1)",
                },
                {
                    item: "rgba(255,255,255, 1)",
                },
            ],
            title: '',//标题
            erCodeData: {},//数据
            currency_symbol: '￥',//数据
            exchange_rate: 1,//数据
            yhxImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/finish3x.png',
            xnyhx: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/xnyhx.png',
            baga: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/baga.png',
            type: "", //订单类型
        };
    },
    onLoad(option) {
        let str = decodeURIComponent(option.dataItem)
        if(str != "undefined"){
            this.erCodeData = JSON.parse(str);
            //受图片带的参数影响，有的图片转换后可能打不开。去掉参数
            if(this.erCodeData.extraction_code_img){
                // 同时兼容 不带参数 的图片格式
                const index = this.erCodeData.extraction_code_img.indexOf('?')
                if(index>0){
                    this.erCodeData.extraction_code_img =  this.erCodeData.extraction_code_img.slice(0, index)
                }
            } 
        }
        //订单类型
        let type = this.erCodeData.sNo.substring(0, 2);
        this.type = type;
        
        this.currency_symbol = this.erCodeData.currency_symbol;
        this.exchange_rate = this.erCodeData.exchange_rate;
        
        if(this.type != 'VI'){
            this.title = this.language.erCode.tqm
        } else {
            this.title = this.language.erCode.hxm
        }
    },
    methods: {
        _navigateTo(url) {
            uni.navigateTo({
                url,
            });
        },
    },
};
</script>

<style>
page {
    background-color: #f4f5f6;
}
</style>

<style lang="less" scoped>
.page{
    .content{
        .content-xnHxCs{
            background-color: #ffffff;
            >view{
                padding: 32rpx 0;
                box-sizing: border-box;
                margin: 0 32rpx;
                display: flex;
                align-items: center;
                justify-content: space-between;
                border-bottom: 2rpx solid rgba(0, 0, 0, .1);
                >view:nth-child(1){
                    font-size: 32rpx;
                    color: #333333;
                }
                >view:nth-child(2){
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    >span{
                        font-size: 32rpx;
                        color: #FA5151;
                    }
                    >image{
                        width: 32rpx;
                        height: 44rpx;
                        margin-left: 8rpx;
                    }
                }
            }
        }
        .content-erCodeImg{
            width: auto;
            height: auto;
            background-color: #ffffff;
            border-radius: 0 0 24rpx 24rpx;
            
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding-bottom: 80rpx;
            box-sizing: border-box;
            >view{
                display: flex;
                align-items: center;
                justify-content: center;
            }
            >view:first-child{
                padding-top: 80rpx;
                box-sizing: border-box;
                position: relative;
                .opcity{
                    opacity: 0.3;
                }
                >image:nth-child(1){
                    width: 360rpx;
                    height: 360rpx;
                    border: 2rpx solid #F4F5F6;
                    border-radius: 32rpx;
                }
                .content-erCodeImg-yhxImg{
                    width: 108rpx;
                    height: 108rpx;
                    position: absolute;
                    margin-top: 130rpx;
                    margin-left: 130rpx;
                }
            }
            >view:nth-child(2){
                color: #333333;
                font-size: 40rpx;
                font-family: DIN;
                margin-top: 24rpx;
            }
            >view:nth-child(3){
                color: #999999;
                font-size: 24rpx;
                margin-top: 16rpx;
            }
        }
    
        .content-goods{
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            margin-top: 24rpx;
            .content-goods-header{
                width: 100%;
                display: flex;
                align-items: center;
                justify-content: space-between;
                padding: 32rpx 32rpx 0 32rpx;
                box-sizing: border-box;
                background-color: #ffffff;
                border-radius: 24rpx 24rpx 0 0;
                >view{
                    >span{
                        font-size: 28rpx;
                        color: #333333;
                        line-height: 40rpx;
                    }
                }
            }
            >view:nth-child(2){
                border-radius: 0 0 24rpx 24rpx !important;
            }
            .content-goods-list{
                width: 100%;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                padding: 32rpx;
                box-sizing: border-box;
                margin-bottom: 16rpx;
                background-color: #ffffff;
                border-radius: 24rpx;
                >view:nth-child(1){
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    >view:nth-child(1){
                        display: flex;
                        margin-right: 20rpx;
                        >image{
                            width: 152rpx;
                            height: 152rpx;
                            border-radius: 16rpx;
                        }
                    }
                    >view:nth-child(2){
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                        justify-content: space-between;
                        >view:nth-child(1){
                            font-size: 32rpx;
                            color: #333333;
                            width: 512rpx;
                            height: auto;
                            display:-webkit-inline-box;
                            overflow: hidden; /*超出隐藏*/
                            text-overflow: ellipsis;/*隐藏后添加省略号*/
                            -webkit-box-orient:vertical; 
                            -webkit-line-clamp:2; //想显示多少行
                        }
                        >view:nth-child(2){
                            width: 100%;
                            display: flex;
                            align-items: center;
                            justify-content: space-between;
                            >span{
                                font-size: 24rpx;
                                color: #999999;
                                line-height: 32px;
                            }
                            >span:nth-child(1){
                                font-size: 32rpx;
                                color: #FA5151;
                            }
                        }
                        >view:nth-child(3){
                            width: 100%;
                            display: flex;
                            align-items: center;
                            justify-content: space-between;
                            >span{
                                font-size: 24rpx;
                                color: #999999;
                            }
                            >span:nth-child(2){
                                font-size: 24rpx;
                                color: #FA9D3B;
                            }
                        }
                    }
                }
                >view:nth-child(2){
                    width: 100%;
                    padding: 22rpx 28rpx;
                    box-sizing: border-box;
                    border-radius: 16rpx;
                    background-color: #F4F5F6;
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                    margin-top: 24rpx;
                    >view:nth-child(1){
                        font-size: 32rpx;
                        color: #333333;
                    }
                    >view:nth-child(2){
                        display: flex;
                        align-items: center;
                        justify-content: space-between;
                        >span{
                            font-size: 32rpx;
                            color: #FA5151;
                        }
                        >image{
                            width: 32rpx;
                            height: 44rpx;
                            margin-left: 8rpx;
                        }
                    }
                }
                
            }
        }
    }
}
</style>
