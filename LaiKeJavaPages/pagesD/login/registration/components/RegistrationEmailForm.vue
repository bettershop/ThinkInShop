<template>
  <div class="login-form">
    <div class="uni-input">
      <image :src="xyx" mode="aspectFill"></image>
      <input
        class="input" 
        v-model="email"
        :placeholder="language.newlogin.qsryxh"
        @blur="inputEmail"
        maxlength="24"
        type="text"
      />
      <image v-if="email != ''" :src="xgb" @click="clearFrom" mode="aspectFill"></image>
    </div>
    <div class="uni-input">
      <image :src="xyzm" mode="aspectFill"></image>
      <input 
        class="input" 
        v-model="localCode"
        @blur="inputCode"
        :placeholder="language.newlogin.qsryzm"
        maxlength="6"
      />
      <div class="uni-code" :style="{color:time_code!= language.newlogin.hqyzm ? '#666666' : ''}" @tap="goGetCode()">{{time_code}}</div>
    </div>
    <div class="uni-input">
      <image :src="xmm" mode="aspectFill"></image>
        <input 
          v-if="isPwdVisible"
          class="input" 
          v-model="password"
          :placeholder="language.newlogin.qsrmmzm"
          @input="inputPassword"
        />
        <input 
          v-else
          class="input" 
          v-model="password"
          :placeholder="language.newlogin.qsrmmzm"
          @input="inputPassword"
          type="password"
        />
      <div class="passwordShow">
        <image 
          :src="isPwdVisible ? xshow : xhide" 
          mode="aspectFill"
          @click="togglePasswordVisibility('main')"
        ></image>
      </div>

    </div>
    <div class="uni-input">
      <image :src="xmm" mode="aspectFill"></image>
      <input 
        v-if="isConfirmPwdVisible"
        class="input" 
        v-model="confirmPassword"
        :placeholder="language.newlogin.qqrmm"
        maxlength="16"
        @blur="inputConfirmPassword"
      />
      <input
        v-else 
        class="input" 
        v-model="confirmPassword"
        :placeholder="language.newlogin.qqrmm"
        maxlength="16"
        @blur="inputConfirmPassword"
        type="password"
      />
      <div class="passwordShow">
        <image 
          :src="isConfirmPwdVisible  ? xshow : xhide" 
          mode="aspectFill"
          @click="togglePasswordVisibility('confirm')"
        ></image>
      </div>
    </div>
  </div>
</template>

<script>
import {img} from "@/static/js/login/imgList.js";
import { getCode } from '../../../../static/js/login/code.js'
export default {
  props: {
    registrationEmail: {
      type: String,
    },
    registrationPassword: {
      type: String,
    },
    code: {
      type: String,
      default: ''
    },  
  },
  created() {
    this.time_code = this.language.newlogin.hqyzm
  },
  data() {
    return {
      email: this.registrationEmail,
      password: this.registrationPassword,
      localCode: this.code,
      confirmPassword: this.registrationPassword,
      activeTab: 'email', // 默认显示邮箱验证
      time_code: '获取验证码',
      count: 60,
      isPwdVisible: false,     // 主密码可见状态
      isConfirmPwdVisible: false, // 确认密码可见状态
      xyx: img(this).xyx,
      xmm: img(this).xmm,
      xshow: img(this).xshow,
      xhide: img(this).xhide,
      xgb: img(this).xgb,
      xyzm: img(this).xyzm,
      down: img(this).down,
    };
  },
  methods: {
    //手机号输入
    inputEmail() {
      this.$emit('inputEmail', this.email);
    },
    //验证码输入
    inputCode() {
      this.$emit('inputCode', this.localCode);
    },
    //密码输入
    inputPassword() {
      this.$emit('inputPassword', this.password)
    },
    //确认密码输入
    inputConfirmPassword() {
      //判断密码是否一致
      if (this.password != this.confirmPassword) {
        uni.showToast({
            title:'密码不一致',
            icon:'none'
          })
      }
    },
    //手机号清空
    clearFrom() {
      this.email = '';
      this.$emit('clearFrom', '');
    },
    // 新增密码可见性切换方法
    togglePasswordVisibility(type) {
      if (type === 'main') {
        this.isPwdVisible = !this.isPwdVisible
      } else if (type === 'confirm') {
        this.isConfirmPwdVisible = !this.isConfirmPwdVisible
      }
    },
    //获取验证码
    goGetCode(){
      if (this.email == '') {
        uni.showToast({
          title: '请输入邮箱号',
          icon: 'none'
        })
        return
      }
      getCode(this,2)
    }
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
    margin-bottom: 20rpx;
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
    .uni-code{
      font-size: 32rpx;
      color: #D73B48;
    }
    .passwordShow {
      width: 100rpx;
      display: flex;
      align-items: center;
      justify-content: flex-end;
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
}
image {
  width: 40rpx;
  height: 40rpx;
  margin-right: 20rpx;
}


</style>
