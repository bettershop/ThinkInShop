<template>
    <div class="retrievePassword-container" :style="{ backgroundImage: 'url(' + bg + ')' }">
      <!-- 头部 -->
      <div class="back" @click="goBack">
          <image :src="x" mode="aspectFill"></image>
        </div>
      <div class="title">{{ language.newlogin.sfyz }}</div>
      <div class="tab" @tap.shop="navTo('/pagesD/login/retrievePassword/index?type=phone')">
        <div class="tab-title">
          <image class="icon" :src="xsj" mode="aspectFill"></image>
          {{ language.newlogin.sjhyz }}
        </div>
        <image :src="iconGd" mode="aspectFill"></image>
      </div>
      <div class="tab" @tap.shop="navTo('/pagesD/login/retrievePassword/index?type=email')">
        <div class="tab-title">
          <image class="icon" :src="xyx" mode="aspectFill"></image>
          {{ language.newlogin.yzyz }}
        </div>
        <image :src="iconGd" mode="aspectFill"></image>
      </div>
    </div>
  </template>
  
  <script>
  import {img} from "@/static/js/login/imgList.js";
  export default {
    components: {

    },
    data() {
      return {
        activeTab: 'phone',
        showAgreement: false,
        x: img(this).x,
        xsj: img(this).xsj,
        xyx: img(this).xyx,
        iconGd: img(this).iconGd,
        bg: img(this).bg,
      };
    },
    methods: {
      goBack() {
        uni.navigateBack({
          delta: 1
        });
      }
    }
  };
  </script>
  
  <style scoped lang="less">
  .retrievePassword-container {
    display: flex;
    flex-direction: column;
    padding: 20px;
    background-color: #FFFFFF;
    background-size: cover;
    background-repeat: no-repeat;
    background-position: center;
    min-height: 100vh;
    width: 100%;
    .title{
        margin: 72rpx 0;
        font-weight: 500;
        font-size: 48rpx;
        color: #333333;
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
    .tab{
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 40rpx;
      padding: 32rpx 38rpx;
      background: #FFFFFF;
      box-shadow: 0rpx 0rpx 20rpx 2rpx rgba(212,40,45,0.12);
      font-size: 32rpx;
      border-radius: 16rpx;
      color: #333333;
      .tab-title{
        display: flex;
        align-items: center;
        font-size: 32rpx;
        color: #333333;
        line-height: 32rpx;
        .icon{
          width: 48rpx;
          height: 48rpx;
          margin-right: 20rpx;
        }
      }
      image {
        width: 36rpx;
        height: 36rpx;
      }
    }
  }
  </style>
