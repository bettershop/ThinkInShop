<template>
    <view>
        <heads :title="language.myStore.Setdetails.title" :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
        <view class="title">
            <view :class="{Unsettled:datail.settlement_status==0}">{{store_default_currency_symbol}}{{datail.settlementPrice}}</view>
            <text>
                {{datail.settlement_status == 0?language.myStore.Setdetails.wjs:language.myStore.Setdetails.yjs}}
            </text>
        </view>
        <view class="detailList">
            <view class="box">
                <view class="Item">
                    <label>{{language.myStore.Setdetails.jsdh}}</label>
                    <text>{{datail.id}}</text>
                </view>
                <view class="Item">
                    <label>{{language.myStore.Setdetails.jssj}}</label>
                    <text>{{datail.arrive_time}}</text>
                </view>
            </view>
            
            <view class="box">
                <view class="Item">
                    <label>{{language.myStore.Setdetails.ddbh}}</label>
                    <text>{{datail.sNo}}</text>
                </view>
                <view class="Item">
                    <label>{{language.myStore.Setdetails.ddje}}</label>
                    <text>{{store_default_currency_symbol}}{{datail.z_price}}</text>
                </view>
                <view class="Item">
                    <label>{{language.myStore.Setdetails.ddscsj}}</label>
                    <text>{{datail.add_time}}</text>
                </view>            
                <view class="Item">
                    <label>{{language.myStore.Setdetails.dpyh}}</label>
                    <text>{{store_default_currency_symbol}}{{datail.mch_discount}}</text>
                </view>
                <view class="Item">
                    <label>{{language.myStore.Setdetails.ptyh}}</label>
                    <text>{{store_default_currency_symbol}}{{datail.preferential_amount}}</text>
                </view>
            </view>
            
            <view class="box">
                <view class="Item">
                    <label>{{language.myStore.Setdetails.yf}}</label>
                    <text>{{store_default_currency_symbol}}{{datail.z_freight}}</text>
                </view>
                <view class="Item">
                    <label>{{language.myStore.Setdetails.yj}}</label>
                    <text>{{store_default_currency_symbol}}{{datail.commission}}</text>
                </view>
                <view class="Item">
                    <label>{{language.myStore.Setdetails.thyj}}</label>
                    <text>{{store_default_currency_symbol}}{{datail.r_commission}}</text>
                </view>
                <view class="Item">
                    <label>{{language.myStore.Setdetails.tdje}}</label>
                    <text>{{store_default_currency_symbol}}{{datail.return_money}}</text>
                </view>
            </view>
        </view>
    </view>
</template>

<script>
    export default {
        data() {
            return {
                datail: {},
                bgColor:[{
                            item: '#ffffff'
                        },
                        {
                            item: '#ffffff'
                        }
                    ],
            }
        },
        onLoad(option) {
            this.datail = JSON.parse(option.arr)

            let storeCurrency = uni.getStorageSync('storeCurrency');
            this.store_default_exchange_rate = storeCurrency.exchange_rate;
            this.store_default_currency_symbol = storeCurrency.currency_symbol;
            this.store_default_currency_code = storeCurrency.currency_code;
            
        },
        methods: {
            
        },
    }
</script>
<style>
    page{
        background-color: #f4f5f6;
    }
</style>
<style lang="less" scoped> 
    @import url("@/laike.less");
    .title {
        display: flex;
        align-items: center;
        flex-direction: column;
        margin: 0 0 16rpx 0;
        border-radius: 0px 0px 24rpx 24rpx;
        background-color: #ffffff;
        text-align: center;
        padding: 64rpx 0 68rpx;
        view {
            font-size: 56rpx;
            font-family: DIN-Bold, DIN;
            font-weight: bold;
            color: #07C160;
        }
        text {
            padding-top: 32rpx;
            font-size: 32rpx;
            
            font-weight: 400;
            color: #666666;
        }
        .Unsettled{
            color: #FA5151 !important;
        }
    }
    .detailList {
      .box{          
          background-color: #ffffff;
          border-radius: 24rpx;
          margin-bottom: 16rpx;
          .Item {
              display: flex;
              align-items: center;
              justify-content: space-between;
              padding: 32rpx;
              border-bottom: 1rpx solid rgba(0, 0, 0, .1);
              box-sizing: border-box;
              font-size: 32rpx;
              
              color: #333333;
              &:nth-last-child(1){
                  border-bottom: 0;
              }
              label {
                  font-weight: 400;
              }
              text {                 
                 font-weight: 500;
              }
          }
      }
    }
</style>
