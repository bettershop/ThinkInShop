<template>
  <div class="phone-verification">
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
    <button :class="[localEmail != '' ? '' : 'uni-btn-disabled','uni-btn']" :disabled="localEmail == ''" @click="nextStep">{{ language.newlogin.xyb }}</button>
  </div>
</template>

<script>
import {img} from "@/static/js/login/imgList.js";
export default {
  name: 'EmailVerification',
  props: {
    email: {
      type: String,
    },   
  },
  data() {
    return {
      localEmail : this.email,
      xyx: img(this).xyx,
      xsj: img(this).xsj,
      xgb: img(this).xgb,
    };
  },
  methods: {
    nextStep() {
      this.$emit('nextStep');
    },
    inputEmail() {
      // 触发登录逻辑
      this.$emit('inputEmail', this.localEmail);
    },
    clearFrom() {
      this.localEmail = '';
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
