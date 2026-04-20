<template>
  <div class="container" :style="{ backgroundImage: 'url(' + bg + ')' }">
    <heads :bgColor="bgColor" ></heads>
    <!-- 头部 -->
    <view class="title">
      <view>输入验证码</view>
      <view v-if="!fall">
        <view v-if="activeTab == 'phone'" class="verification-title">验证码将发送至 +{{ code2 }}{{ phone }}</view>
        <view v-if="activeTab == 'email'" class="verification-title">验证码将发送至 {{ email }}</view>
      </view>
      <view v-else class="verification-title"> 验证码发送失败,请重新获取</view>
    </view>
 
      <div class="set-password">
        <div class="uni-input">
          <image :src="xyzm" mode="aspectFill"></image>
          <input 
            class="input" 
            v-model="localCode"
            placeholder="请输入验证码"
            maxlength="6"
          />
          <div class="uni-code" :style="{color:time_code!='获取验证码'?'#666666':''}" @tap="goGetCode()">{{time_code}}</div>
        </div>
        <div class="uni-input">
          <image :src="xmm" mode="aspectFill"></image>
          <input
            v-if="isPwdVisible" 
            class="input" 
            v-model="localPassword"
            placeholder="请输入密码(6～16位字母或数字)"
            maxlength="16"
          />
          <input
             v-else 
            class="input" 
            v-model="localPassword"
            placeholder="请输入密码(6～16位字母或数字)"
            maxlength="16"
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
            placeholder="请确认密码"
            maxlength="16"
          />
          <input 
            v-else
            class="input" 
            v-model="confirmPassword"
            placeholder="请确认密码"
            maxlength="16"
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
        <!-- 确认按钮 -->
        <button class="uni-btn" type="primary" @tap="confirmation">确认</button>
      </div>
    </div>
</template>

<script>
import {img} from "@/static/js/login/imgList.js";
import { getCode } from '../../../static/js/login/code.js'
export default {
  data() {
    return {
      isPwdVisible: false,     // 主密码可见状态
      isConfirmPwdVisible: false, // 确认密码可见状态
      phone: '', // 手机号码
      email: '', // 邮箱号码
      code2: '',
      num3: '',
      time_code: '获取验证码',
      fall: false,
      localCode: '',
      localPassword: '',
      confirmPassword: '',
      activeTab: 'phone', // 默认显示手机号验证
      count: 0,
      timer: null,
      bgColor: [
          {
              item: '#FFFFFF'
          },
          {
              item: '#FFFFFF'
          }
      ],
      xmm: img(this).xmm,
      xshow: img(this).xshow,
      xhide: img(this).xhide,
      xgb: img(this).xgb,
      xyzm: img(this).xyzm,
      down: img(this).down,
      bg: img(this).bg,
    };
  },
  onLoad (option) {
    if (option.activeTab) {
      this.activeTab = option.activeTab
      this.phone = option.phone
      this.email = option.email
      this.code2 = option.code2
      this.num3 = option.num3
    }
  }, 
  methods: {
    // 新增密码可见性切换方法
    togglePasswordVisibility(type) {
      if (type === 'main') {
        this.isPwdVisible = !this.isPwdVisible
      } else if (type === 'confirm') {
        this.isConfirmPwdVisible = !this.isConfirmPwdVisible
      }
    },
    confirmation() {
      //判断验证码是否为空
      if (this.localCode == '') {
        uni.showToast({
          title:'验证码不能为空',
          icon:'none'
        })
        return
      }
      //判断密码是否为空
      if (this.localPassword == '') {
        uni.showToast({
          title:'密码不能为空',
          icon:'none'
        })
        return
      }
      //判断确认密码是否为空
      if (this.confirmPassword == '') {
        uni.showToast({
          title:'确认密码不能为空',
          icon:'none'
        })
        return
      }
      //判断密码长度是否小于6位
      if (this.localPassword.length < 6) {
        uni.showToast({
          title:'密码长度小于6位',
          icon:'none'
        })
        return
      }
      //判断密码是否一致
      if (this.localPassword != this.confirmPassword) {
        uni.showToast({
            title:'密码不一致',
            icon:'none'
          })
        return
      }
      const isEmail = this.activeTab === 'email';
      const data = {
        api: 'app.Login.forget_code',
        type: isEmail ? 1 : 0,
        password: this.localPassword,
        keyCode: this.localCode
      };
      
      if (isEmail) {
        data.e_mail = this.email;
      } else {
        data.cpc = this.code2;
        data.country_num = this.num3;
        data.tel = this.phone;
      }
      this.$req.post({data}).then(res => {
          if (res.code == 200) {
             uni.showToast({
                 title: '设置成功',
                 duration: 1000,
                 icon: 'success'
             })
             setTimeout(()=>{
                 uni.reLaunch({
                     url: '/pagesD/login/newLogin',
                 })
             },1500)
              
          }  else {
              uni.showToast({
                  title: res.message,
                  duration: 1000,
                  icon: 'none'
              })
          }  
      })      
      
    },
    //获取验证码
    goGetCode(){
      getCode(this,4)
    }
  }
};
</script>

<style scoped lang="less">
.container {
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
      .verification-title {
        margin-top: 24rpx;
        font: 400 28rpx/1 'PingFang SC';
      }
  }
  .set-password {
    margin-top: 20px;
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
      font-size: 32rpx;
      margin-top: 60rpx;
      background: linear-gradient(270deg, #FF6F6F 0%, #FA5151 100%);
      border-radius: 24rpx;
      padding: 14rpx 0;
      color: #FFFFFF;
      text-align: center;
      font-style: normal;
      text-transform: none;
      width: 100%;
    }

    .error {
      color: red;
      margin-top: 10px;
    }
  }
}
image {
  width: 40rpx;
  height: 40rpx;
  margin-right: 20rpx;
}
</style>