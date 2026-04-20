<template>
<div class="login-page" :style="{ backgroundImage: 'url(' + bg + ')' }">
  <div class="login-container">
    <!-- 头部 -->
    <div class="back" @click="goBack">
      <image :src="x" mode="aspectFill"></image>
    </div>
    <div class="login-header">{{ language.newlogin.hydl }}</div>
    <div class="uni-input" v-if="activeTab === 'phone'">
      <image :src="xsj" mode="aspectFill"></image>
      <div class="uni-input__area" @tap.shop="navTo('/pagesD/login/countryAndRegion')">
        {{ diqu.code2?'+' + diqu.code2 : '国家/地区' }}
        <image :src="down" mode="aspectFill"></image>
      </div>
      <input
        class="input" 
        v-model="phone"
        :placeholder="language.newlogin.qsrsjhm"
      />
      <image v-if="phone != ''" :src="xgb" @click="clearFrom" mode="aspectFill"></image>
    </div>
    <div class="uni-input" v-if="activeTab === 'email'">
      <image :src="xyx" mode="aspectFill"></image>
      <input
        class="input" 
        v-model="email"
        :placeholder="language.newlogin.qsryxh"
      />
      <image v-if="email != ''" :src="xgb" @click="clearFrom" mode="aspectFill"></image>
    </div>
    <div @click="activeTab = activeTab === 'phone' ? 'email' : 'phone'" class="login-forget">{{ activeTab === 'phone'?language.newlogin.qhyx:language.newlogin.qhhm }}</div>
    <!-- 登录按钮 -->
    <LoginButton 
      :isReading="isReading" 
      @setReading="setReading" 
      @handleLogin="handleLogin" 
    />
  </div>
  <!-- 社交登录 -->
  <!-- <SocialLogin type="code" /> -->
</div>
</template>
  
  <script>
  import LoginButton from './components/LoginButton.vue';
  import SocialLogin from '../components/SocialLogin.vue';
  import { next_one } from '@/static/js/login/login.js'
  import {img} from "@/static/js/login/imgList.js";  
  export default {
    components: {
      LoginButton,
      SocialLogin,
    },
    data() {
      return {
        activeTab: 'phone',
        phone: '',
        email: '',
        isReading: false,
        diqu: {
          code2: '86',
          num3: '156'
        },
        x: img(this).x,
        xyx: img(this).xyx,
        xgb: img(this).xgb,
        xsj: img(this).xsj,
        down: img(this).down,
        bg: img(this).bg,
      };
    },
    onShow(){
      if(uni.getStorageSync('diqu')){
      	this.diqu = JSON.parse(uni.getStorageSync('diqu'))
      }       
    },
    methods: {
      handleLogin() {
        if (this.activeTab === 'phone') {
          if (!this.phone) {
            uni.showToast({
              title: '请输入手机号',
              icon: 'none'
            });
            return;
          }
          const reg = /^(13[0-9]|14[0-9]|15[0-9]|16[0-9]|17[0-9]|18[0-9]|19[0-9])\d{8}$/;
          if (!reg.test(this.phone)) {
            uni.showToast({
              title: '请输入正确的手机号',
              icon: 'none'
            });
            return;
          }
        } else {
          if (!this.email) {
            uni.showToast({
              title: '请输入邮箱号',
              icon: 'none'
            });
            return;
          }
          const reg = /^[A-Za-z0-9\u4e00-\u9fa5\-_]+@[A-Za-z0-9_-]+(\.[A-Za-z0-9_-]+)+$/;
          if (!reg.test(this.email)) {
            uni.showToast({
              title: '请输入正确的邮箱号',
              icon: 'none'
            });
            return;
          }
        }
        let url = `/pagesD/login/captchaLogin/inputCode?activeTab=${this.activeTab}&phone=${this.phone}&email=${this.email}&code2=${this.diqu.code2}&num3=${this.diqu.num3}`
        uni.navigateTo({
          url: url
        });
      },
      clearFrom() {
        this.phone = '';
        this.email = '';
      },
      setReading(e) {
        this.isReading = e;
      },
      goBack() {
        uni.navigateBack({
          delta: 1
        });
      }
    }
  };
  </script>
  
<style scoped lang="less">
.login-page{
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background-size: cover;
  background-repeat: no-repeat;
  background-position: center;
  min-height: 100vh;
  width: 100%;
}
  .login-container {
    // display: flex;
    // flex-direction: column;
    padding: 20px;
    background-color: #FFFFFF;
    .login-header{
        margin: 72rpx 0;
        font-weight: 500;
        font-size: 48rpx;
        color: #333333;
    }
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
      width: 100%;
      height: 40px;
      background-color: #007aff;
      color: #fff;
      border: none;
      border-radius: 4px;
    }
    .login-forget{
      display: flex;
      justify-content: flex-end;
      font-weight: normal;
      font-size: 28rpx;
      color: #666666;
    }
    .back{
      /* #ifdef MP-WEIXIN */
      margin: 60rpx 0 20rpx 0;
      /* #endif */
      font-weight: 500;
      font-size: 48rpx;
      color: #333333;
      image {
        width: 24rpx;
        height: 24rpx;
      }
    }
  }
  image {
    width: 48rpx;
    height: 48rpx;
    margin-right: 20rpx;
  }
  </style>
