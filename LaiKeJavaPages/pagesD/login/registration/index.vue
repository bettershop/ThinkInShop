<template>
    <div class="registration-container" :style="{ backgroundImage: 'url(' + bg + ')' }">
      <!-- 头部 -->
      <div class="registration-back" @click="goBack">
        <image :src="x" mode="aspectFill"></image>
      </div>
      <div class="registration-header">{{ language.newlogin.srzh }}</div>
  
      <!-- 登录方式切换 -->
      <RegistrationTab :activeTab="activeTab"  @switchTab="switchTab" />
  
      <!-- 登录表单 -->
      <RegistrationPhoneForm 
        v-if="activeTab === 'phone'" 
        :registrationPhone="formData.phone" 
        :registrationPassword="formData.password"
        :code="formData.code"
        :code2="diqu.code2"
        :num3="diqu.num3"
        @inputPhone= "inputPhone"
        @inputCode= "inputCode"
        @inputPassword="inputPassword"
        @clearFrom="clearFrom"
      />
      <RegistrationEmailForm 
        v-if="activeTab === 'email'" 
        :registrationEmail="formData.email"
        :registrationPassword="formData.password"
        @inputEmail="inputEmail"
        @inputCode= "inputCode"
        @inputPassword="inputPassword"
        @clearFrom="clearFrom"
      />
  
      <!-- 登录按钮 -->
      <RegistrationButton
        :isReading="isReading" 
        @setReading="setReading" 
        @handleRegistration="handleRegistration" 
      />
  
    </div>
  </template>
  
  <script>
  import RegistrationTab from './components/RegistrationTab.vue';
  import RegistrationPhoneForm from './components/RegistrationPhoneForm.vue';
  import RegistrationEmailForm from './components/RegistrationEmailForm.vue';
  import RegistrationButton from './components/RegistrationButton.vue';
  import showToast from "@/components/aComponents/showToast.vue"
  import {img} from "@/static/js/login/imgList.js";
  export default {
    components: {
      RegistrationTab,
      RegistrationButton,
      RegistrationPhoneForm,
      RegistrationEmailForm,
      showToast,
    },
    data() {
      return {
        activeTab: 'phone',
        showAgreement: false,
        formData: {
          phone: '',
          password: '',
          email: '',
          code: '',
        },
        isReading: false,
        fastTap: true,
        sus: img(this).sus,
        diqu: {
          code2: '86',
          num3: '156'
        },
        fatherId: '',
        tabbar_isShow: 1, // 1 手机号注册 2 邮箱注册
        fastTap: true,
        x: img(this).x,
        bg: img(this).bg,
      };
    },
    onShow(){
      if(uni.getStorageSync('diqu')){
      	this.diqu = JSON.parse(uni.getStorageSync('diqu'))
      } 
    },    
    methods: {
      handleRegistration() {
        if (!this.fastTap) {
          return
        }
        if (this.passone != this.passtwo) {
          uni.showToast({
              title: this.language.register.mimaTips2,
              duration: 1000,
              icon: 'none'
          })
          this.fastTap = true
          return
        }
        this.fastTap = false
        let data = {}
        // 手机号注册
        if(this.activeTab == 'phone'){     
          data ={
            api:'app.Login.user_register',                        
            type: 0, // 类型 0.手机号 1.邮箱
            phone: this.formData.phone, // 手机号码 
            password: this.formData.password,//密码
            keyCode: this.formData.code,// 验证码
            cpc: this.diqu.code2, // 国家/地区
            country_num:this.diqu.num3,  // 国家代码
          }
        }else{  
          const regex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}/g;
          if(!regex.test(this.formData.email)){
            uni.showToast({
                title:this.language.openInvoice.qsrzqddzyx,
                icon:'none'
            })
            this.fastTap = true
            return
          }
          // 邮箱注册
          data ={
            api:'app.Login.user_register',
            type: 1, // 类型 0.手机号 1.邮箱 
            e_mail:this.formData.email,  // 邮箱
            password:this.formData.password, //密码
            keyCode:this.formData.code // 验证码
          }
        }
        // 分享時 綁定人手機號
        if (this.fatherId != '') {
          data.pid = this.fatherId
        }
        // 补充变量url，解决uni.request中url为undefined的问题
        this.$req.post({data}).then(res => {
          this.fastTap = true
          if (res.code == 200 && res.data && res.data.access_id) {
              let {
                  access_id,
                  wx_status
              } = res.data
              
              //提示注册成功
              uni.showToast({
                  title: '注册成功',
                  icon: 'success',
                  duration: 1000
              })
              
              uni.setStorageSync('LoingByHand', true)
              uni.setStorageSync('access_id', access_id) 
                  setTimeout(function () {
                      uni.reLaunch({
                         url: '/pages/shell/shell?pageType=my',
                          success: function () {
                              if (wx_status != 1) {
                                  this.$store.state.shouquan = true
                              }
                          }
                      })
                  }, 1000) 
          } else {
            this.fastTap = true
            uni.showToast({
              title:res.message,
              icon:'none'
            })
          }
        }).catch(e => { this.fastTap = true })
      },
      goBack() {
        uni.navigateBack({
          delta: 1
        });
      },
      switchTab(tab) {
        this.activeTab = tab;
        this.tabbar_isShow = tab === 'phone' ? 1 : 2;
        this.formData.phone = '';
        this.formData.email = '';
        this.formData.code = '';
        this.formData.password = '';
      },
      inputPhone(e) {
        this.formData.phone = e
      },
      inputCode(e) {
        this.formData.code = e
      },
      inputEmail(e) {
        this.formData.email = e
      },
      inputPassword(e) {
        this.formData.password = e
      },
      clearFrom() {
        this.formData.phone = ''
        this.formData.email = ''
      },
      setReading(e) {
        this.isReading = e;
      },
    }
  };
  </script>
  
  <style scoped lang="less">
  .registration-container {
    display: flex;
    flex-direction: column;
    padding: 20px;
    background-color: #FFFFFF;
    background-size: cover;
    background-repeat: no-repeat;
    background-position: center;
    min-height: 100vh;
    width: 100%;
    .registration-header{
        margin: 72rpx 0;
        font-weight: 500;
        font-size: 48rpx;
        color: #333333;
    }
    .registration-back{
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
  </style>
