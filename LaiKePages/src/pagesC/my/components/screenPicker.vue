<template>
    <view v-if="showScreenPicker">
      <view class="screen-picker-modal">
        <view class="mask" @click="cancel"></view>
        <view class="content">
          <view class="picker-header">
            <image class="close"  :src="close2x " @click="cancel"></image>
            筛选
          </view>
          <view class="screen-text">搜索</view>
          <view class="search-box">
            <input type="text" v-model="specify_sNo" placeholder="请输入订单号关键字搜索" />
          </view>
          <view class="screen-text">类型</view>
          <view class="type-options">
            <view 
              class="option" 
              :class="{ active: specify_type === option.value }" 
              v-for="option in dicType" 
              :key="option.value" 
              @click="selectType(option.value)"
            >
              {{ option.ctext }}
            </view>
          </view>
          <view class="buttons">
            <button @click="reset">重置</button>
            <button @click="confirm">确定</button>
          </view>
        </view>
      </view>
    </view>
  </template>
  
  <script>
  export default {
    props: {
      showScreenPicker: {
        type: Boolean,
        default: false
      },
      dicType: {
        type: [String, Array],
        default: []
      },
    },
    data() {
      return {
        specify_type: '',
        specify_sNo: '',
        close2x: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/close2x.png',
      };
    },

    methods: {
      selectType(value) {
        this.specify_type = value;
      },
      reset() {
        this.specify_type = '';
        this.specify_sNo = '';
        this.$emit('reset');
      },
      confirm() {
        this.$emit('confirm', {
            specify_type: this.specify_type,
            specify_sNo: this.specify_sNo
        });
      },
      cancel() {
        this.$emit('cancel');
      }
    }
  };
  </script>
  
  <style lang="less" scoped>
  .screen-picker-modal {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    z-index: 999;
  
    .mask {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0,0,0,0.5);
    }
  
    .content {
      position: relative;
      background: #fff;
      border-radius: 20rpx 20rpx 0 0;
      padding:0 32rpx 50rpx 32rpx;
      z-index: 99999;
      .picker-header{
        padding: 52rpx 0;
        display: flex;
        justify-content: center;
        align-items: center;
        font-size: 36rpx;
        position: relative;
      }
      .close {
        position: absolute;
        right:0rpx;
        top: 50%;
        transform: translateY(-50%);
        cursor: pointer;
        width: 40rpx;
        height: 40rpx;
      }
      .screen-text {
        font-size: 30rpx;
        color: #333333;
        margin-bottom: 28rpx;
      }
      .search-box {
        width: 686rpx;
        border-radius: 30rpx;
        margin-bottom: 48rpx;
        border: 1px solid #ddd;
        background: #F6F5F8;
        padding: 12rpx 32rpx;
        box-sizing: border-box;
        input {
          width: 100%;
          height: 100%;
          ::placeholder {
            color: #BBBBBB;
          }
        }
      }
  
      .type-options {
        display: flex;
        flex-wrap: wrap;
        margin-bottom: 34rpx;
        max-height: 260rpx;
        overflow-y: auto;
        .option {
          width: 214rpx;
          height: 60rpx;
          border-radius: 30rpx;
          background: #F6F5F8;
          color: #666666;
          font-size: 24rpx;
          display: flex;
          cursor: pointer;
          justify-content: center;
          align-items: center;
          box-sizing: border-box;
          margin-right: 22rpx;
          margin-bottom: 24rpx;
  
          &.active {
            background: rgba(250,81,81,0.1);
            color: #FA5151;
            border-color: #ff6b6b;
          }
        }
        .option:nth-child(3n) {
            margin-right:0rpx;
        }

      }
  
      .buttons {
        display: flex;
        justify-content: flex-end;
        padding-top: 14rpx;
        border-top: 1rpx solid #eee;
        button {
          padding: 10px 20px;
          border: none;
          border-radius: 5px;
          cursor: pointer;
  
          &:first-child {
            width: 220rpx;
            height: 92rpx;
            border-radius: 52rpx;
            background: #fff;
            border: 1px solid #ddd;
            color: #333;
            margin-right: 10px;
            display: flex;
            justify-content: center;
            align-items: center;
          }
  
          &:last-child {
            color: #fff;
            width: 440rpx;
            height: 92rpx;
            background: linear-gradient( 270deg, #FF6F6F 0%, #FA5151 100%);
            border-radius: 52rpx;
            display: flex;
            justify-content: center;
            align-items: center;
          }
        }
      }
    }
  }
  </style>
