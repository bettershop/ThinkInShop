<template>
    <div class="phone-verification">
      <div class="uni-input">
        <image :src="xsj" mode="aspectFill"></image>
        <div class="uni-input__area" @tap.shop="navTo('/pagesD/login/countryAndRegion')">
          {{ code2?'+' + code2 : language.newlogin.qh }}
          <image :src="down" mode="aspectFill"></image>
        </div>
        <input
          class="input" 
          v-model="localPhone"
          :placeholder="language.newlogin.qsrsjhm"
          @input="inputPhone"
        />
        <image v-if="localPhone != ''" :src="xgb" @click="clearFrom" mode="aspectFill"></image>
      </div>
      <button :class="[localPhone != '' ? '' : 'uni-btn-disabled','uni-btn']" :disabled="localPhone == ''" @click="nextStep">{{ language.newlogin.xyb }}</button>
    </div>
  </template>
  
  <script>
  import {img} from "@/static/js/login/imgList.js";
  export default {
    name: 'PhoneVerification',
    props: {
      phone: {
        type: String,
      },
      code2: {
        type: String,
        default: ''
      },      
    },
    data() {
      return {
        localPhone: this.phone,
        down: img(this).down,
        xgb: img(this).xgb,
        xsj: img(this).xsj,
      };
    },
    methods: {
      nextStep() {
        this.$emit('nextStep');
      },
      inputPhone() {
        // 触发登录逻辑
        this.$emit('inputPhone', this.localPhone);
      },
      clearFrom() {
        this.localPhone = '';
        this.$emit('clearFrom', '');
      },
    }
  };
  </script>
  
  <style scoped lang="less">
  .phone-verification {
    .uni-input {
      display: flex;
      align-items: center;
      width: 100%;
      height: 108rpx;
      margin-bottom: 20rpx;
      padding: 0 20rpx;
      box-sizing: border-box;
      border-radius: 24rpx;
      background: #FFFFFF;
      box-shadow: 0rpx 0rpx 10rpx 1rpx rgba(212,40,45,0.12);
      .input{
        height: 100%;
        flex: 1;
      }
      .uni-input__area {
        display: flex;
        align-items: center;
        font-size: 32rpx;
        color: #333333;
        image {
          width: 10rpx;
          height: 10rpx;
          margin-left: 8rpx;
        }
      }
    }
    .uni-btn {
      font-size: 32rpx;
      margin-top: 60rpx;
      background: linear-gradient( 270deg, #FF6F6F 0%, #FA5151 100%);
      border-radius: 24rpx;
      padding: 14rpx 0;
      color: #FFFFFF;
      text-align: center;
      font-style: normal;
      text-transform: none;
    }
    .uni-btn-disabled {
      opacity: 0.5;
    }
  }
  .error {
    color: red;
    margin-top: 10px;
  }
  image {
    width: 48rpx;
    height: 48rpx;
    margin-right: 20rpx;
  }
  </style>
