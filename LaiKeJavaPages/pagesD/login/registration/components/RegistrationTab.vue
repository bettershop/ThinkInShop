<template>
  <div class="login-tab">
    <view class="tab-item" :class="{ active: activeTab === 'phone' }" @click="switchTab('phone')">
      <image :src="activeTab === 'phone' ? sjCur : sj" mode="aspectFill"></image>
      {{ language.newlogin.sjhzc }}
    </view>
    <view class="tab-item" :class="{ active: activeTab === 'email' }" @click="switchTab('email')">
      <image :src="activeTab === 'email' ? yxcur : yx" mode="aspectFill"></image>
      {{ language.newlogin.yxzc }}
    </view>
  </div>
</template>

<script>
import {img} from "@/static/js/login/imgList.js";
export default {
  props: {
    activeTab: {
      type: String,
      default: 'phone'
    }
  },
  methods: {
    switchTab(tab) {
      this.active = tab;
      this.$emit('switchTab', tab);
    }
  },
  data() {
      return {
        sj: img(this).sj,
        yx: img(this).yx,
        yxcur: img(this).yxcur,
        sjCur: img(this).sjCur,
      };
    },
};
</script>

<style scoped lang="less">
.login-tab {
  display: flex;
  justify-content: space-around;
  margin: 20px 0;
  width: 100%;
  background: #FFFFFF;
  box-shadow: 0px 0px 8px 1px rgba(212,40,45,0.08);
  border-radius: 24rpx;
  padding: 22rpx;
  .tab-item {
    width: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 16rpx 0;
    &.active {
      background: #FA5151;
      border: 2rpx solid #E5E5E5;
      border-radius: 24rpx;
      color: #FFFFFF;
    }
  }
}
image {
  width: 48rpx;
  height: 48rpx;
  margin-right: 20rpx;
}
</style>
