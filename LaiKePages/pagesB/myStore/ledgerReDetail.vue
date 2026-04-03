<template>
    <view>
        <heads :title="language.myStore.ledgerRecord.fzjl" :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
        <view class="title">
            <view class="">{{datail.amount}}</view>
            <text>
               {{language.myStore.ledgerReDetail.fzje}}
            </text>
        </view>
        <view class="detailList">
            <view class="box">
                <view class="Item">
                    <label>{{language.myStore.ledgerReDetail.fzzh}}</label>
                    <text>{{datail.account}}</text>
                </view>
                <view class="Item">
                    <label>{{language.myStore.ledgerReDetail.clsj}}</label>
                    <text>{{datail.add_date}}</text>
                </view>
            </view>
            
            <view class="box">
                <view class="Item">
                    <label>{{language.myStore.ledgerReDetail.ddbh}}</label>
                    <text>{{datail.order_no}}</text>
                </view>
                <view class="Item">
                    <label>{{language.myStore.ledgerReDetail.ddje}}</label>
                    <text>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(datail.z_price)}}</text>
                </view>           
                <view class="Item">
                    <label>{{language.myStore.ledgerReDetail.tdje}}</label>
                    <text>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(datail.refund_price)}}</text>
                </view>
                <view class="Item">
                    <label>{{language.myStore.ledgerReDetail.yf}}</label>
                    <text>{{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}{{LaiKeTuiCommon.formatPrice(datail.z_freight)}}</text>
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
            color: #333333;
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
