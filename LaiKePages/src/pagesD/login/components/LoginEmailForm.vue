<template>
  <div class="login-form">
    <div class="uni-input">
      <image :src="xyx" mode="aspectFill"></image>
      <input
        class="input" 
        v-model="localEmail"
        :placeholder="language.newlogin.qsryxh"
        @input="inputEmail"
      />
      <image v-if="localEmail != ''" :src="xgb" @click="clearFrom" mode="aspectFill"></image>
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
    email: {
      type: String,
    },
    password: {
      type: String,
    },    
  },
  data() {
    return {
      localEmail: this.email,
      localPassword: this.password,
      xyx: img(this).xyx,
      xmm: img(this).xmm,
      xshow: img(this).xshow,
      xhide: img(this).xhide,
      xgb: img(this).xgb,
      isPwdVisible: false,
    };
  },
  methods: {
    inputEmail() {
      // 触发登录逻辑
      this.$emit('inputEmail', this.localEmail);
    },
    togglePasswordVisibility() {
      this.isPwdVisible = !this.isPwdVisible
    },
    inputPassword() {
      // 触发登录逻辑
      this.$emit('inputPassword', this.localPassword);
    },
    clearFrom() {
      this.localEmail = '';
      this.$emit('clearFrom');
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
