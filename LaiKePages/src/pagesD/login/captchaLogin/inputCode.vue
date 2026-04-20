<template>
  <view class="page-box">
    <view class="container">
    <heads :bgColor="bgColor" :title='title' ></heads>
    <view class="title">
      <view>{{ language.newlogin.qsrmm }}</view>
      <view v-if="activeTab == 'phone'" class="verification-title">{{ language.newlogin.yzmyfsz }} {{ code2 }}+{{ phone }}</view>
      <view v-if="activeTab == 'email'" class="verification-title">{{ language.newlogin.yzmyfsz }} {{ email }}</view>
    </view>
    
    <view class="verification-code">
    <!-- 删除原有input-container相关代码 -->
    <!-- 替换为验证码显示框 -->
    <view class="code-display">
      <view 
        v-for="(_, index) in codeLength" 
        :key="index"
        class="code-item"
        :class="{ active: currentIndex === index }"
      >
        {{ verificationCode[index] || '' }}
      </view>

     </view>
     <view class="verification-timer" :style="{color:time_code!= language.newlogin.zxhq ?'#666666':''}" @tap="reGetCode()">{{ time_code }}</view>
  </view>
  </view>
      <!-- 自定义数字键盘 -->
      <view class="custom-keyboard">
      <view class="keyboard-row" v-for="(row, i) in keyboardLayout" :key="i">
        <view 
          v-for="(num, j) in row" 
          :key="j"
          class="keyboard-btn"
          @touchstart="handleKeyPress(num)"
        >
          {{ num }}
        </view>
      </view>
      <view class="keyboard-row">
        <view class="keyboard-btn empty"></view>
        <view 
          class="keyboard-btn"
          @touchstart="handleKeyPress(0)"
        >0</view>
        <view 
          class="keyboard-btn delete-btn"
          @touchstart="handleDelete"
        >⌫</view>
      </view>
    </view>
  </view>
</template>

<script>
const CODE_LENGTH = 6; // 验证码位数

import { mapMutations } from 'vuex'
import { getCode } from '../../../static/js/login/code.js'
import { submitLogin } from '../../../static/js/login/login.js'
export default {
  data() {
    return {
      verificationCode: Array(CODE_LENGTH).fill(''),
      currentIndex: 0,
      keyboardLayout: [
        [1, 2, 3],
        [4, 5, 6],
        [7, 8, 9]
      ],
      realCode: '', // H5用真实输入值
      codeLength: CODE_LENGTH,
      title: '',
      bgColor: [
        {
            item: '#FFFFFF'
        },
        {
            item: '#FFFFFF'
        }
     ],
      isSwitchLable: 0, // 0: 手机号 1: 邮箱
      code: '', // 验证码
      phone: '', // 手机号码
      email: '', // 邮箱号码
      code2: '',
      num3: '',
      fatherId: '',
      is_ts: false,
      agreement1: '',
      agreement2: '',
      logo: '',
      company: '',
      activeTab: 'phone',
      time_code: '重新获取',
      count: 60,
      timer: null,
    };
  },

  onUnload(){
    uni.removeStorageSync('diqu')
  },
  onLoad (option) {
    // #ifdef MP-WEIXIN
    wx.onKeyboardHeightChange(res => {
      if (res.height === 0) {
        this.currentIndex = Math.min(this.currentIndex, CODE_LENGTH - 1);
      }
    });
    // #endif
    if (option.activeTab) {
      this.activeTab = option.activeTab
      this.phone = option.phone
      this.email = option.email
      this.code2 = option.code2
      this.num3 = option.num3
    }
    // #ifdef H5
    let ua = navigator.userAgent.toLowerCase();
    if (ua.match(/MicroMessenger/i) != "micromessenger"){
        this.showWxLogin = false
    } else {
        this.showAliLogin = false
    }
    // #endif
    uni.removeStorageSync('signFlag');
    let data = {
        api: 'app.login.index',}
    this.$req.post({data}).then(res => {
        this.agreement1 = res.data.Agreement
        this.agreement2 = res.data.Agreement_1
        this.logo = res.data.logo
        this.company = res.data.company  
    })

    // 获取服务供应商
    uni.getProvider({
        service: 'oauth',
        success: res => {
            this.provider = res.provider
        }
    })
            
    //判断landing_code是否存在，存在则是登录验证未通过进入，登录成功后需要返回上一页
    if (option.landing_code) {
        this.togoodsDetail = option.landing_code
    }
    
    //获取、绑定上级id
    if (option.fatherId) {
        this.fatherId = option.fatherId
    }
    this.toHome = option.toHome;
    if (uni.getStorageSync('fatherId')) {
        this.fatherId = uni.getStorageSync('fatherId')
    }
    getCode(this,1)
  },  
  methods: {
    // 修改handleKeyPress方法
    handleKeyPress(num) {
      if (this.currentIndex >= CODE_LENGTH) return;

      // 新增首位不能为0的判断
      if (this.currentIndex === 0 && num === 0) {
        uni.showToast({
          title: '首位不能为0',
          icon: 'none'
        });
        return;
      }

      this.$set(this.verificationCode, this.currentIndex, num.toString());
      this.currentIndex = Math.min(this.currentIndex + 1, CODE_LENGTH - 1);

      // 自动提交检测
      if (this.verificationCode.every(v => v)) {
        this.code = this.verificationCode.join('');
        this.submitVerification();
      }
    },

    handleRealInput(e) {
      let input = e.detail.value.replace(/\D/g, '');
      
      // 处理首位0的情况（仅在长度大于1时移除）
      if (input.length > 1 && input[0] === '0') {
        input = input.substring(1);
      }
      
      this.realCode = input.slice(0, CODE_LENGTH);
      
      // 同步到显示数组
      this.verificationCode = this.realCode.split('').slice(0, CODE_LENGTH);
      this.currentIndex = this.verificationCode.length;
      
      // 自动聚焦
      this.$refs.realInput?.focus();
    },
    
    // 增强删除处理
    handleDelete() {
      this.currentIndex = Math.max(this.currentIndex - 1, 0);
      this.$set(this.verificationCode, this.currentIndex, '');
      
      // 当删除到首位时重置焦点
      if (this.currentIndex === 0) {
        this.currentIndex = 0;
        this.verificationCode = Array(CODE_LENGTH).fill('');
      }
    },

    ...mapMutations({
      set_access_id: 'SET_ACCESS_ID',
      user_phone: 'SET_USER_PHONE'
    }),

    focusNextInput(currentIndex) {
      const nextIndex = Math.min(currentIndex + 1, CODE_LENGTH - 1);
      this.setFocus(nextIndex);
    },
    
    focusPrevInput(currentIndex) {
      const prevIndex = Math.max(currentIndex - 1, 0);
      this.setFocus(prevIndex);
    },
    
    // 优化删除处理
    // 修改后的handleDelete方法
    handleDelete() {
      // 先清空当前光标位置的内容
      if (this.currentIndex < CODE_LENGTH) {
        this.$set(this.verificationCode, this.currentIndex, '');
      }
      
      // 处理向前删除逻辑
      if (this.currentIndex > 0) {
        this.currentIndex = Math.max(this.currentIndex - 1, 0);
        this.$set(this.verificationCode, this.currentIndex, ''); // 新增这行确保前一位内容清除
      }
    
      // 当删除到首位时强制聚焦到第一个位置
      if (this.currentIndex === 0) {
        this.verificationCode = Array(CODE_LENGTH).fill('');
      }
    },

    setFocus(index) {
      this.focusedIndex = index;
      this.$nextTick(() => {
        // #ifdef MP-WEIXIN
        // 微信小程序专用聚焦方案
        const query = uni.createSelectorQuery().in(this);
        query.select(`#input${index}`).fields({
          node: true,
          size: true
        }).exec((res) => {
          if (res[0] && res[0].node) {
            res[0].node.focus = true;
          }
        });
        // #endif
        //#ifdef H5
        const inputRef = this.$refs[`input${index}`];
        const nativeInput = inputRef?.[0]?.$el?.querySelector('input') || inputRef?.[0]?.$el;
        
        if (nativeInput) {
          // 添加延迟确保DOM更新完成
          setTimeout(() => {
            try {
              const len = this.verificationCode[index].length;
              // 仅当有内容时设置光标
              if (len > 0 && nativeInput.setSelectionRange) {
                nativeInput.setSelectionRange(len, len);
              }
              nativeInput.focus();
            } catch(e) {
              console.warn('光标设置失败:', e);
            }
          }, 50);
        }
        //#endif
      });
    },
    
    // 验证码登录
    submitVerification(){
      submitLogin(this)
    }, 
    //重新获取二维码
    reGetCode(){
      getCode(this,1)
    }   
  }
};
</script>

<style lang="less">
.page-box{
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.container {
  padding: 40rpx;
  background: #fff;
 
  
  .title {
    margin: 72rpx 0;
    font: 500 48rpx/1 'PingFang SC';
    color: #333;
    
    .verification-title {
      margin-top: 24rpx;
      font: 400 28rpx/1 'PingFang SC';
    }
  }
}

.input-container {
  display: flex;
  justify-content: space-between;
  margin-bottom: 40rpx;
  
  .input-item {
    width: 92rpx;
    height: 116rpx;
    background: #F4F5F6;
    border-radius: 12rpx;
    text-align: center;
    color: #333;
    font-size: 40rpx;
    caret-color: #ff0000;
    
    /* #ifdef MP-WEIXIN */
    cursor-color: #ff0000;
    cursor-spacing: 10rpx;
    /* #endif */
    
    &::placeholder {
      color: #ccc;
    }
  }
}

.verification-timer {
  font: 400 28rpx/1 'PingFang SC';
  color: #5196F7;
}

.code-display {
  display: flex;
  justify-content: center;
  margin: 60rpx 0 40rpx;
  
  .code-item {
    width: 90rpx;
    height: 120rpx;
    margin: 0 10rpx;
    border: 2rpx solid #ddd;
    border-radius: 12rpx;
    font-size: 48rpx;
    background: #F4F5F6;
    font-family: DIN;
    display: flex;
    align-items: center;
    justify-content: center;
    
    &.active {
      border-color: #5196F7;
      box-shadow: 0 4rpx 12rpx rgba(81,150,247,0.2);
    }
  }
}

.custom-keyboard {
  background: #f8f8f8;
  padding: 30rpx 0;
  
  .keyboard-row {
    display: flex;
    justify-content: center;
    margin: 20rpx 0;
  }
  
  .keyboard-btn {
    width: 200rpx;
    height: 90rpx;
    background: #fff;
    border-radius: 12rpx;
    margin: 0 15rpx;
    text-align: center;
    line-height: 90rpx;
    font-size: 40rpx;
    transition: all 0.1s;
    
    &:active {
      background: #e0e0e0;
    }
    
    &.delete-btn {
      font-size: 50rpx;
      line-height: 80rpx;
    }
    
    &.empty {
      visibility: hidden;
    }
  }
}
</style>
