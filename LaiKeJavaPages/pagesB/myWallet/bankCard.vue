<template>
  <view class="bankCard">
      <heads :title="language.bankCard.title" :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
      <toload :load="load" >
          <view class="list-item" v-for="(item, index) of list" :key="index" @click="toBankCardDetail(item, index)" :style="item.Bank_name==language.bankCard.yh[0]?Bank_color:(item.Bank_name==language.bankCard.yh[1]?Bank_color1:(item.Bank_name==language.bankCard.yh[2]?Bank_color2:(item.Bank_name==language.bankCard.yh[3]?Bank_color3:Bank_color4)))">
              
              <view style="height: 80rpx;margin-top: 30rpx;display: flex;">
                  <image class="list_icon_img" :src="item.Bank_name==language.bankCard.yh[0]?zhaoshang:(item.Bank_name==language.bankCard.yh[1]?pingan:(item.Bank_name==language.bankCard.yh[2]?nongye:(item.Bank_name==language.bankCard.yh[3]?jianshe:moren_yinghan)))"></image>
                  <view>
                      <text class="card-name" style="font-size: 32rpx;">{{ item.Bank_name }}</text>
                      <text class="card-name">{{ item.branchName }}</text>
                  </view>
                <view v-if="index==0" class="mricon">{{language.bankCard.mr}}</view>
              </view>

              <view class="bottom">
                  <view class="round-wrap">
                      <view class="round"></view>
                      <view class="round"></view>
                      <view class="round"></view>
                      <view class="round"></view>
                  </view>
                  <view class="round-wrap">
                      <view class="round"></view>
                      <view class="round"></view>
                      <view class="round"></view>
                      <view class="round"></view>
                  </view>
                  <view class="round-wrap">
                      <view class="round"></view>
                      <view class="round"></view>
                      <view class="round"></view>
                      <view class="round"></view>
                  </view>

                  <text class="num">{{ item.Bank_card_number }}</text>
              </view>
          </view>
              
      </toload>

      <view class="btn" v-if="load" @click="navTo('./bankCardAdd?type=' + type)">
          <image :src="gw_img"></image>
          <view>{{language.bankCard.tjyhk}}</view>
      </view>
      <view style="height: 20rpx;">
          
      </view>
  </view>
</template>

<script>
export default {
  name: "bankCard",
    data () {
      return {
          // title: '银行卡管理',
          title: '银行卡管理',
          list: [],
          Bank_color:'background: linear-gradient(135deg, #F87373 0%, #F6504D 100%);',
          Bank_color1:'background: linear-gradient(315deg, #FC9153 0%, #FAAD5D 100%);',
          Bank_color2:'background: linear-gradient(313deg, #28A991 0%, #2FC6A9 100%);',
          Bank_color3:'background: linear-gradient(134deg, #59B6F8 0%, #3497EE 100%);',
          Bank_color4:'background: #FA5151;',
          load: false,
          bgColor:[{
                  item: '#ffffff'
              },
              {
                  item: '#ffffff'
              }
          ],
          type: '',
          gw_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/add-bank-card.png',
          zhaoshang: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/zhangshang.png',
          nongye: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/nongye.png',
          pingan: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/pingan.png',
          jianshe: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jianshe.png',
          moren_yinghan: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/moren_yinghan.png',
      }
    },
    onLoad (option) {
       this.type = option.type
    },
    onShow() {
      // this.load = false;
      this.title = this.language.bankCard.title
      this._axios();
    },
    methods: {
        _axios() {
            let data = {}
            if (this.type === 'store') {
                data = {
                    
                    api:"mch.App.MchBank.BankList"
                }
            } else {
                data = {
                    api: 'app.user.bank_list',
                }
            }
            
            this.$req.post({data}).then(res => {
                try {
                    if (this.type === 'store') {
                        res.data=res.data.list
                    }
                    this.list = res.data
                } catch (e) {
                }
                setTimeout(() => {
                    this.load = true;
                }, 300)
            })
        },
        toBankCardDetail (item, index) {
            let params = {
                bankName: item.Bank_name,
                cardNum: item.Bank_card_number,
                branchName:item.branchName,
                id: item.id,
                storeType: this.type,
                type: index % 2 === 0 ? 1 : 2, // 1绿色，2红色
            }
            let params_str = Object.keys(params).map(key => key + "=" + params[key]).join("&");
            this.navTo(`./bankCardDetail?${params_str}`);
        }
    },
}
</script>

<style lang="less" scoped> 
    @import url("@/laike.less");
    .mricon{
        position: absolute;
        top: 56rpx;
        right: 44rpx;
        width: 64rpx;
        height: 40rpx;
        background: #FFFFFF;
        border-radius: 8rpx;
        font-size: 24rpx;
        
        font-weight: 400;
        color: #333333;
        line-height: 40rpx;
    }
    .btn{
          background: #FFF;
          display: flex;
          align-items: center;
          width: 690rpx !important;
          margin: auto;
          height: 140rpx;
          justify-content: center;
          flex-direction: row;
          border-radius: 24rpx;
          image {
              width: 39rpx;
              height: 39rpx;              
              margin-left: 30rpx;
              margin-right: 20rpx;
          }
          
          view {
              font-size: 32rpx;                
              
              font-weight: 500;
              color: #333333;
          }
      }
      .list_icon_img{
          width: 80rpx;
          height: 80rpx;
          margin-right: 16rpx;
      }
.bankCard {
    padding-top: 30rpx;
    min-height: 100vh;
    background: #f6f6f6;
    .list-item {
        width: 690rpx;
        height: 256rpx;
        border-radius: 24rpx;
        text-align: center;
        margin: 0 auto;
        margin-bottom: 20rpx;
        padding: 30rpx;
        box-sizing: border-box;
        position: relative;
        overflow: hidden;
        .default {
            position: absolute;
            top: 0;
            right: 0;
            width: 56rpx;
            height: 28rpx;
            background: #FF7D00;
            border-bottom-left-radius: 10rpx;
            display: flex;
            align-items: center;
            justify-content: center;
            text {
                transform: scale(0.83);
                font-size: 24rpx;
                
                font-weight: 500;
                color: #FFFFFF;
            }
            
        }

        
        &.add {
            padding: 0;
            background: #FFF;
            display: flex;
            align-items: center;
            justify-content: center;
            flex-direction: row;
            
            image {
                width: 39rpx;
                height: 39rpx;
                display: block;
                margin-left: 30rpx;
                margin-right: 20rpx;
            }
            
            text {
                font-size: 32rpx;                
                
                font-weight: 500;
                color: #333333;
            }
        }
        
        .card-name {
            font-size: 28rpx;
            
            font-weight: 500;
            color: #FFFFFF;
            line-height: 26rpx;
            height: 26rpx;
            display: flex;
            margin-bottom: 23rpx;
        }
        
        .bottom {
            height: 40rpx;
            display: flex;
            align-items: center;
            font-size: 22rpx;
            margin-top: 24rpx;
            margin-left: 110rpx;
            .round-wrap {
                display: flex;
                align-items: center;
                margin-right: 59rpx;
                .round {
                    height: 10rpx;
                    width: 10rpx;
                    border-radius: 50%;
                    background: #FFF;
                    margin-right: 10rpx;
                }
            }
            text {
                display: block;
                align-items: center;
                font-size: 40rpx;
                
                font-weight: 500;
                color: #FFFFFF;
                
                &.num {
                    
                    font-size: 44rpx;
                }
            }
        }
    }
}

</style>
