<template>
    <div class="login-form">
      <div class="uni-input">
        <image :src="xsj" mode="aspectFill"></image>
        <div class="uni-input__area" @tap.shop="navTo('/pagesD/login/countryAndRegion')">
          {{ code2?'+' + code2 : '国家/地区' }}
          <image :src="down" mode="aspectFill"></image>
        </div>
        <input
          class="input" 
          v-model="localPhone"
          type="number"
          :placeholder="language.newlogin.qsrsjhm"
          @input="inputPhone"
        />
        <image v-if="localPhone != ''" :src="xgb" @click="clearFrom" mode="aspectFill"></image>
      </div>
      <div class="uni-input">
        <image :src="xmm" mode="aspectFill"></image>
        <input 
          v-if="isPwdVisible"
          class="input" 
          v-model="localPassword"
          :placeholder="language.newlogin.qsrmm"
          @input="inputPassword"
        />
        <input 
          v-else
          class="input" 
          v-model="localPassword"
          :placeholder="language.newlogin.qsrmm"
          @input="inputPassword"
          type="password"
        />
        <div class="passwordShow">
          <image 
            :src="isPwdVisible ? xshow : xhide" 
            mode="aspectFill"
            @click="togglePasswordVisibility()"
          ></image>
        </div>
      </div>
    </div>
  </template>
  
  <script>
  import {img} from "@/static/js/login/imgList.js";
  export default {
    props: {
      phone: {
        type: String,
      },
      password: {
        type: String,
      },
      code2: {
        type: String,
        default: ''
      },      
    },
    data() {
      return {
        xsj: img(this).xsj,
        xmm: img(this).xmm,
        xshow: img(this).xshow,
        xhide: img(this).xhide,
        xgb: img(this).xgb,
        down: img(this).down,
        localPhone: this.phone,
        localPassword: this.password,
        isPwdVisible: false,
      };
    },
    methods: {
      inputPhone() {
        // 触发登录逻辑
        this.$emit('inputPhone', this.localPhone);
      },
      inputPassword() {
        // 触发登录逻辑
        this.$emit('inputPassword', this.localPassword);
      },
      togglePasswordVisibility() {
        this.isPwdVisible = !this.isPwdVisible
      },
      clearFrom() {
        this.localPhone = '';
        this.$emit('clearFrom', '');
      },
    },
  };
  </script>
  
  <style scoped lang="less">
  .login-form {
    width: 100%;
    .uni-input {
      display: flex;
      align-items: center;
      width: 100%;
      height: 108rpx;
      margin-bottom: 32rpx;
      padding: 0 20rpx;
      box-sizing: border-box;
      border-radius: 24rpx;
      background: #FFFFFF;
      box-shadow: 0rpx 0rpx 10rpx 1rpx rgba(212,40,45,0.12);
      .input{
        height: 100%;
        flex: 1;
        border: none;
        outline: none;
      }
      .passwordShow {
        width: 100rpx;
        display: flex;
        align-items: center;
        justify-content: flex-end;
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
  }
  image {
    width: 40rpx;
    height: 40rpx;
    margin-right: 20rpx;
  }
  </style>
