<template>
    <div class="login-button">
      <div class="login-agreement">
        <image class="login-agreement-img" 
          @click="setReading" :src="isReading ? xxycur : xxy" 
          mode="aspectFill">
        </image>{{ language.newlogin.wyydbty }} 
        <span class="login-agreement-text" @tap.shop="navTo('/pagesD/login/agreement')">《{{agreement1}}》</span> {{ language.newlogin.h }} 
        <span class="login-agreement-text" @tap.shop="navTo('/pagesD/login/privacy')">《{{agreement2}}》</span>
      </div>      
      <button :class="[isReading ? '' : 'uni-btn-disabled','uni-btn']" :disabled="!isReading" @click="handleRegistration">{{ language.newlogin.zc }}</button>
    </div>
  </template>
  
  <script>
  import {img} from "@/static/js/login/imgList.js";
  export default {
    props: {
    isReading: {
      type: Boolean,
      default: false
    },
  },
    data() {
      return {
        agreement1: '',
        agreement2: '',
        xxy: img(this).xxy,
        xxycur: img(this).xxycur,
      };
    },
    mounted() {
    this.gerXYName()
  },
    methods: {
      setReading() {
        this.$emit('setReading', !this.isReading);
      },
      navTo(url) {
        uni.navigateTo({
          url: url
        });
      },
      handleRegistration() {
        // 触发登录逻辑
        this.$emit('handleRegistration');
      },
      //获取协议名称
      gerXYName(){
        let data = {
            api: 'app.login.index',
        }
        this.$req.post({ data }).then(res => {
            if (res.code == 200) {
                this.agreement1 = res.data.Agreement
                this.agreement2 = res.data.Agreement_1
            }
        })
      },
    },
  };
  </script>
  
  <style scoped lang="less">
  .login-button {
    width: 100%;
  
    .uni-btn {
      font-size: 32rpx;
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
    .login-agreement{
      display: flex;
      justify-content: flex-start;
      margin: 28rpx 0;
      font-size: 24rpx;
      line-height: 26rpx;
      color: #999999;
      .login-agreement-text{
        color: #5297F7;
      }
      .login-agreement-img{
        width: 30rpx;
        height: 30rpx;
        margin-right: 10rpx;
      }
    }
  }
  </style>
