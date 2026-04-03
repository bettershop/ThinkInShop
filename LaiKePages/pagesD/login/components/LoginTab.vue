<template>
  <div class="login-tab">
    
    <view class="tab-item" :class="{ activeTab: activeTab === 'phone' }" @click="switchTab('phone')">
      <image :src="activeTab === 'phone' ? sjCur : sj" mode="aspectFill"></image>
      {{language.newlogin.sjh}}
    </view>
    
    <view class="tab-item" :class="{ activeTab: activeTab === 'email' }" @click="switchTab('email')">
      <image :src="activeTab === 'email' ? yxcur : yx" mode="aspectFill"></image>
      {{language.newlogin.yx}}
    </view>
    
    <view class="tab-item" :class="{ activeTab: activeTab === 'account' }" @click="switchTab('account')">
      <image :src="activeTab === 'account' ? userCur : user" mode="aspectFill"></image>
      {{language.newlogin.zh}}
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
  data() {
    return {
      sj: img(this).sj,
      yx: img(this).yx,
      user: img(this).user,
      sjCur: img(this).sjCur,
      yxcur: img(this).yxcur,
      userCur: img(this).userCur,
    };
  },
  methods: {
    switchTab(tab) {
      this.$emit('switchTab', tab);
    }
  }
  
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
    width: 33%;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 16rpx 0;
    &.activeTab {
      background: #FA5151;
      border-radius: 24rpx;
      color: #FFFFFF;
    }
  }
}

image {
  width: 42rpx;
  height: 42rpx;
  margin-right: 8rpx;
}
</style>
