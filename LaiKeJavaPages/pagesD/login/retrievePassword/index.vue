<template>
    <div class="retrievePassword-container" :style="{ backgroundImage: 'url(' + bg + ')' }">
      <heads :bgColor="bgColor" :title='title' ></heads>
      <!-- 头部 -->
      <div class="title">
        <div>{{ activeTab === 'phone' ? language.newlogin.sjhyz : language.newlogin.yzyz }}</div>
      </div>

      
      <!-- 手机号验证 -->
      <PhoneVerification 
        v-if="activeTab === 'phone'" 
        :phone="phone"
        :code2="diqu.code2"
        @clearFrom="clearFrom"
        @inputPhone="inputPhone"
        @nextStep="goSetPassword"
      >
      </PhoneVerification>
  
      <!-- 邮箱验证 -->
      <EmailVerification 
        v-if="activeTab === 'email'" 
        :email="email"
        @clearFrom="clearFrom"
        @inputEmail="inputEmail"
        @nextStep="goSetPassword"
        >
      </EmailVerification>
    </div>
  </template>
  
  <script>

  import PhoneVerification from './components/PhoneVerification.vue';
  import EmailVerification from './components/EmailVerification.vue';
  import { next_one } from '@/static/js/login/login.js'
  import {img} from "@/static/js/login/imgList.js";
  export default {
    components: {
      PhoneVerification,
      EmailVerification,
    },
    onLoad(option) {
      this.activeTab = option.type || 'phone'; // 默认显示手机号验证
    },
    data() {
      return {
        activeTab: 'phone', 
        title:'',
        phone:'',
        email:'',
        diqu: {
          code2: '86',
          num3: '156'
        },
        bgColor: [
          {
              item: '#FFFFFF'
          },
          {
              item: '#FFFFFF'
          }
        ],
        bg: img(this).bg,
      };
    },
    onShow(){
      if(uni.getStorageSync('diqu')){
      	this.diqu = JSON.parse(uni.getStorageSync('diqu'))
      } 
    }, 
    methods: {
      goSetPassword() {
        let url = `/pagesD/login/retrievePassword/setPassword?activeTab=${this.activeTab}&phone=${this.phone}&email=${this.email}&code2=${this.diqu.code2}&num3=${this.diqu.num3}`
        next_one(this,url)
      },
      inputPhone(e) {
        this.phone = e
      },
      inputEmail(e) {
        this.email = e
      },
      clearFrom(){
        this.phone = '';
        this.email = '';
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
    min-height: 100vh;
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
        .description{
          margin-top: 24rpx;
          font-size: 28rpx;
          color: #333333;
        }
    }
  }
  </style>
