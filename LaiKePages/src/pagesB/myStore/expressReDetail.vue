<template>
    <view>
        <heads :title="language.myStore.Setdetails.title" :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
        
        <view class="detailList">
            <view class="box">
                <view class="Item">
                    <label>快递订单ID</label>
                    <text><text v-if='datail.kdComOrderNum' class="copy" type="button" @tap="onCopy(datail.kdComOrderNum)">{{language.expressage.copy}}</text>{{datail.kdComOrderNum}}</text>
                </view>
                <view class="Item">
                    <label>快递公司</label>
                    <text>{{datail.kuaidi_name}}</text>
                </view>
                <view class="Item">
                    <label>快递单号</label>
                    <text><text  class="copy" type="button" @tap="onCopy(datail.courier_num)">{{language.expressage.copy}}</text>{{datail.courier_num}}</text>
                </view>
            </view>
            
            <view class="box">
                <view class="mybody">
                    <view class="mybody_l">
                       <img :src="jjicon" alt="" /> 
                    </view>
                    <view class="mybody_r">
                        <p>{{datail.send_name}} {{datail.send_tel}}</p>
                        <p class="mybody_r_p2">{{datail.send_sheng}}{{datail.send_shi}}{{datail.send_xian}}{{datail.send_address}}</p>
                    </view>
                </view>
            </view>
            <view class="box">
                <view class="mybody">
                    <view class="mybody_l">
                       <img :src="sjicon" alt="" /> 
                    </view>
                    <view class="mybody_r">
                        <p>{{datail.name}} {{datail.mobile}}</p>
                        <p class="mybody_r_p2">{{datail.sheng}}{{datail.shi}}{{datail.xian}}{{datail.address}}</p>
                    </view>
                </view>
            </view>
            
            <view class="box">
                
                <view class="Item">
                    <label>订单编号</label>
                    <text><text class="copy" type="button" @tap="onCopy(datail.sNo)">{{language.expressage.copy}}</text>{{datail.sNo}}</text>
                </view>
                <view class="Item">
                    <label>发货时间</label>
                    <text>{{datail.deliver_time}}</text>
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
                jjicon:this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL+'images/icon1/jjicon.png',
                sjicon:this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL+'images/icon1/sjicon.png',
                bgColor:[{
                            item: '#ffffff'
                        },
                        {
                            item: '#ffffff'
                        }
                    ],
                sNo:'',
            }
        },
        onLoad(option) {
            this.sNo=option.id
            this._axios()
        },
        methods: {
            onCopy (item) {
                
                // #ifndef MP-WEIXIN
                uni.setClipboardData({
                    data: item,
                    success: function (res) {
                        
                    }
                })
                // #endif
               
               
                uni.showToast({
                    title: this.language.expressage.copy_success,
                    icon: 'none',
                    duration: 1500
                })
            
                
                // #ifdef MP-WEIXIN
                copyText('', item)
                // #endif
            },
            _axios() {
                
                let data = {
                    api:'mch.App.Mch.ShippingRecords',
                    sNo: this.sNo,
                }
                this.$req.post({data}).then(res => {
                    let { code, data, message } = res
                    uni.hideLoading()
                    if (code == 200) {
                
                        this.datail = data.list[0];
                        
                        
            
                    } else if (code == 404) {
                        uni.showToast({
                            title: message,
                            duration: 1500,
                            icon: 'none'
                        })
                        setTimeout(function() {
                            uni.navigateTo({
                                url: '../../pagesD/login/newLogin?landing_code=1',
                            })
                        }, 1500)
                    } else {
                        uni.showToast({
                            title: message,
                            duration: 1500,
                            icon: 'none'
                        })
                    }
                })
            },
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
                 font-size: 28rpx;
              }
          }
          &:first-child{
              border-radius: 0 0 24rpx 24rpx;
          }
      }
      .mybody{
          padding: 32rpx 0;
          display: flex;
          .mybody_l{
              img{
                  margin: 50rpx 36rpx;
                  width: 48rpx;
                  height: 48rpx;
              }
          }
          .mybody_r{
              .mybody_r_p2{
                  color: #999999;
                  margin-top: 16rpx;
              }
          }
      }
    }
    .copy{
        color: #Fa5151;
        margin-right: 16rpx;
    }
</style>
