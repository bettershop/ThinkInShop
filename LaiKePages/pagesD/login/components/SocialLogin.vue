<template>
  <div class="social-login">
    <view class="social-icons">
<!--      <image :src="zfb" @tap="aliLogin" v-if="isZfb" mode="aspectFill"></image>-->
<!--      <image :src="wx" @tap="wxLogin" v-if="isWx" mode="aspectFill"></image>-->
    </view>
  </div>
</template>

<script>
import {img} from "@/static/js/login/imgList.js";
export default {
  name: 'SocialLogin',
  data() {
    return {
      zfb: img(this).zfb,
      wx: img(this).wx,
      isWx: false,
      isZfb: false
    };
  },
  mounted() {
    if (this.isWechatH5()) {
      this.isWx = true
    }else {
       // #ifdef H5
         this.isZfb = true
       // #endif
    }
  },
  methods: {
       isWechatH5() {
         if (process.env.UNI_PLATFORM !== 'h5') return false;
         if (typeof window === 'undefined') return false;
         const ua = window.navigator.userAgent.toLowerCase();
         return /micromessenger/.test(ua) && !/miniprogram/.test(ua);
       },
        aliLogin() {               
            let data = {
                api: 'app.login.aliUserLoginByWeb',
                // #ifdef APP-PLUS
                store_type: 11,
                // #endif
                // #ifdef H5
                store_type: 2
                // #endif
            }
            this.$req.post({data}).then(res => {
                let urls = res.data.url
                // #ifdef APP-PLUS
                //要配置za.html 中间页面
                plus.runtime.openURL(urls, err => {
                    uni.showToast({
                        title: '打开支付宝失败！请检查是否已安装？',
                        icon: 'none' 
                    });
                },'com.eg.android.AlipayGphone');
                // #endif
                // #ifdef H5
                window.location.href = urls
                // #endif                   
            })
        },
        wxLogin(e) {
            // #ifdef H5
            let data = {
                api: 'app.login.getWxAppId'
            }
            this.$req.post({data}).then(res => {
                let appid = res.data.appId//项目appid
                let urls = this.LaiKeTuiCommon.LKT_H5_DEFURL + 'pages/tabBar/my'
                let url=encodeURIComponent(urls)//这里的是回调地址要与申请的地址填写一致
                let scopes="snsapi_userinfo"//表示授权的作用域，多个可以用逗号隔开，snsapi_base表示静默授权，snsapi_userinfo表示非静默授权
                let mainstate=Math.random()//state：用于保持请求和回调的状态，授权请求后原样带回给第三方。该参数可用于防止csrf攻击（跨站请求伪造攻击）可设置为简单的随机数加session进行校验
                window.location.href =`https://open.weixin.qq.com/connect/oauth2/authorize?appid=${appid}&redirect_uri=${url}&response_type=code&scope=${scopes}&state=${mainstate}#wechat_redirect`
            })
            // #endif
        }, 
  },
};
</script>

<style scoped lang="less">
.social-login {
  margin-bottom: 72rpx;
  padding: 0rpx 40rpx;
  .uni-title {
    font-size: 14px;
    color: #999;
    margin-bottom: 10px;
  }

  .social-icons {
    display: flex;
    justify-content: center;

    image {
      width: 64px;
      height: 64px;
    }
  }
}
</style>
