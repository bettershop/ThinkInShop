<template>
  <div id="container">
    <div class="main">
      <div class="content">
        <div class="el-aside">
          <img src="../../assets/imgs/loginBg1.png" alt="" />
        </div>
        <div class="right_form">
          <div class="form">
            <div class="laike">
              <!-- <img :src="logo_img" /> -->
            </div>
            <div class="form_login">
              <el-form :model="merchantsForm" status-icon :rules="rules2" ref="merchantsForm" label-width="100px"
                class="demo-ruleForm">
                <el-form-item prop="customerNumber">
                  <el-input :placeholder="$t('login.qsrscbh')" ref="userNum" type="text"
                    v-model="merchantsForm.customerNumber" autocomplete="off" prefix-icon="el-icon-date"></el-input>
                </el-form-item>

                <el-form-item prop="userName">
                  <el-input :placeholder="$t('login.qsrsczh')" ref="userName" type="text"
                    v-model="merchantsForm.userName" autocomplete="off" prefix-icon="el-icon-user"></el-input>
                </el-form-item>
                <el-form-item prop="pwd">
                  <el-input :placeholder="$t('login.qsrmm')" ref="pwd" type="password" v-model="merchantsForm.pwd"
                    autocomplete="off" prefix-icon="el-icon-lock"></el-input>
                </el-form-item>
                <!-- 图形校验码 -->
                <el-form-item prop="imgCode" style="position: relative">
                  <el-input :placeholder="$t(`login.qsryzm`)" v-model="merchantsForm.imgCode">
                    <div slot="prefix"
                      style="height: 100%;width: 1rem;margin-left: 6px;display: flex;justify-content: center;flex-direction:column">
                      <img style="width:1rem;height:1rem" :src="require('@/assets/images/shield_icon.png')" alt />
                    </div>
                  </el-input>
                  <div class="login-code" @click="handleVerificationCode" v-loading="is_img_loading">
                    <img :src="codeImg" alt="" />
                    <!-- 图形验证码过期提示 -->
                    <div class="codeImgGQ" v-if="codeImgGQ">
                      <i class="el-icon-refresh-right"></i>
                    </div>
                  </div>
                </el-form-item>
                <div>
                  <el-form-item>
                    <el-button :loading="loading" type="primary"
                      @click.native.prevent="merchantsLogin('merchantsForm')">{{$t('login.dl')}}</el-button>
                  </el-form-item>
                </div>
              </el-form>
            </div>
          </div>
        </div>
      </div>

      <div class="footer">
        <div class="footer_box">
          <ul class="link-list">
            <li v-for="(item, index) in linkList" :key="index">
              <a :href="item.url" target="_blank" v-if="item.url">{{
                item.name
              }}</a>
              <span v-else>{{ item.name }}</span>
            </li>
          </ul>
          <div class="introduce">
            <span>{{ copyright_information }}</span>
            <span v-if="copyright_information" class="hr">{{$t('login.bqsy')}}</span>
            <span>{{ record_information }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 弹窗提示 -->
    <div class="mask" v-if="versionUpdate">
      <div class="mask-content">
        <div class="margin-left">
          <img src="../../assets/imgs/bg_1314.png" class="mask_bg" alt="">
        </div>
        <div class="margin-right">
          <h1>{{ systemMsgTitle }}</h1>
          <div class="title-content">
            <h2>公告内容</h2>
            <div class="content">
              <p v-html="content"></p>
            </div>
          </div>
          <el-button @click="closeVersion" type="primary" style="width: 113px;">我知道了</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  import {
    validUsername
  } from "@/utils/validate";
  import {
    index
  } from "@/api/Platform/system";
  import {
    getShopInfo
  } from "@/api/Platform/merchants";
  import axios from "axios";
  import Config from "@/packages/apis/Config";
  export default {
    data() {
      var validateUser = (rule, value, callback) => {
        if (value === "") {
          callback(new Error(this.$t('login.qsrsczh')));
        } else {
          // if (this.ruleForm.user !== "") {
          //   this.$refs.ruleForm.validateField("username");
          // }
          callback();
        }
      };
      var validatePass = (rule, value, callback) => {
        if (value === "") {
          callback(new Error(this.$t('login.qsrmm')));
        } else {
          // if (this.ruleForm.pass !== "") {
          //   this.$refs.ruleForm.validateField("password");
          // }
          callback();
        }
      };
      var validateCode = (rule, value, callback) => {
        if (value == "") {
          callback(new Error(this.$t("login.qsryzm")));
        } else {
          callback();
        }
      };
      var validateUserNum = (rule, value, callback) => {
        if (value === "") {
          callback(new Error(this.$t('login.qsrscbh')));
        } else {
          // if (this.ruleForm.userNum !== "") {
          //   this.$refs.ruleForm.validateField("userNum");
          // }
          callback();
        }
      };
      var validateName = (rule, value, callback) => {
        if (value === "") {
          callback(new Error(this.$t('login.qsrsczh')));
        } else {
          // if (this.ruleForm.name !== "") {
          //   this.$refs.ruleForm.validateField("name");
          // }
          callback();
        }
      };
      return {
        content: "",
        systemMsgTitle:"",
        versionUpdate: false,
        showVersion: false,
        platformForm: {
          userName: "",
          pwd: "",
          imgCodeToken: "",
          imgCode: ""
        },
        merchantsForm: {
          customerNumber: "",
          userName: "",
          pwd: "",
          imgCodeToken: "",
          imgCode: ""
        },
        rules1: {
          userName: [{
            validator: validateUser,
            trigger: "blur"
          }],
          pwd: [{
            validator: validatePass,
            trigger: "blur"
          }],
          imgCode: [{
            validator: validateCode,
            trigger: "blur"
          }]
        },
        rules2: {
          customerNumber: [{
            validator: validateUserNum,
            trigger: "blur"
          }],
          userName: [{
            validator: validateName,
            trigger: "blur"
          }],
          pwd: [{
            validator: validatePass,
            trigger: "blur"
          }],
          imgCode: [{
            validator: validateCode,
            trigger: "blur"
          }]
        },
        activeName: "first",
        loading: false,
        setTime: '',//定时器变量，用于销毁定时器
        codeImgGQ: false,//判断图形验证是否过期，后台写死120秒过期。
        linkList: [],
        baseUrl: "",
        copyright_information: "",
        record_information: "",
        logo_img: "",
        loginType: true,
        codeImg: "",
        imgCodeToken: "",
        is_img_loading: false

      };
    }, 
    async created() {
      this.$store .dispatch("user/getSystemIcon" )

      const res = await index({
        api: 'admin.system.getSetSystem',
      })
      if (res && res.data.code == "200" && res.data.data && res.data.data.config) {
        let info = res.data.data.config;
        this.linkList = JSON.parse(info.link_to_landing_page);
        this.copyright_information = info.copyright_information;
        this.record_information = info.record_information;
        this.logo_img = info.logo;
      }
      this.handleVerificationCode();
    },

    methods: {
      closeVersion() {
        this.showVersion = false
        this.versionUpdate = false
        window.sessionStorage.clear()
        window.location.reload(true);
      },
      // 获取验证码code   admin.saas.user.getCode
      handleVerificationCode() {
        this.codeImgGQ = false //初始化隐藏 图形验证码过期显示
        let url = Config.baseUrl
        this.is_img_loading = true

        axios({
            method: "post",
            url: url,
            params: {
              api: "admin.saas.user.getCode",
            },
          })
          .then((res) => {
            if (res.data.code == 200) {
              let reg = new RegExp('gw')
              let codeImg = "";
              let tmpCodeImg = res?.data?.data?.code_img;
              if(tmpCodeImg && tmpCodeImg.indexOf("http") == -1){
                  let reg = new RegExp('gw')
                  if (process.env.VUE_APP_PROXY_API.indexOf('gw') !== -1) {
                    this.codeImg = process.env.VUE_APP_PROXY_API?.replace(reg, '') +
                    res.data.data.code_img
                  } else {
                    this.codeImg = process.env.VUE_APP_PROXY_API?.replace(reg, '') + '/' + res.data.data.code_img
                  }
              } else {
                this.codeImg = tmpCodeImg
              }
              this.merchantsForm.imgCodeToken = res.data.data.code;
              //如果存在定时器 先清除原有️定时器
              if(this.setTime != ''){clearTimeout(this.setTime);}
              //开启新的定时器
              this.setTime = setTimeout(()=>{
                this.codeImgGQ = true
              },120000)
            }
            console.log("res", res);
          })
          .catch((error) => {
            console.log("error", error);
          }).finally(() => {
            this.is_img_loading = false
          })
      },
      envSetFun() {
        this.baseUrl = process.env.VUE_APP_BASE_API;
      },
      merchantsLogin(merchantsForm) {
        let me = this;
        this.$refs["merchantsForm"].validate((valid) => {
          if (valid) {
            if (this.loginType) {
              this.loginType = false;
              setTimeout(function() {
                me.loginType = true;
              }, 500);
              this.loading = true;
              this.$store
                .dispatch("user/login", this.merchantsForm)
                .then((res) => {
                  this.content = res.data.data.systemMsg
                  this.systemMsgTitle =  res.data.data.systemMsgTitle
                  if (this.content.length > 0 && res.data.data.systemMsgType == 1) {
                    this.versionUpdate = true
                    this.loading = false;
                    return
                  }
                  console.log('查看下数据359', this.redirect);
                  this.$router.push({
                    path: this.redirect || "/"
                  });
                  this.loading = false;
                  // 获取按钮权限
                  this.$store.dispatch("permission/getAsyncRoutes");
                })
                .catch(() => {
                  this.loading = false;
                  this.handleVerificationCode();
                });
            }
          } else {
            console.log("error submit!!");
            this.handleVerificationCode();
            return false;
          }
        });
      },
    },
  };
</script>

<style scoped lang="less">
  .codeImgGQ{
    width: 100%;
    height: 40px;
    background-color: rgba(0,0,0,.3);
    position: absolute;
    top: 0;
    z-index: 999;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 30px;
    color: #ffffff;
  }
  .mask {
    position: fixed;
    z-index: 99999;
    top: 0;
    bottom: 0;
    left: 0;
    right: 0;
    background: rgba(0, 0, 0, 0.6);
    display: block;

    .mask-content {
      display: flex;
      top: 50% !important;
      background: none;
      width: 1140px;
      height: auto !important;
      margin: 0 auto;
      position: absolute;
      left: 50%;
      transform: translate(-50%, -50%);
      border-radius: 12px;

      .margin-left {
        width: 667px;
        height: 636px;
        padding: 0;

        // background-color: pink;
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          margin: 0;
        }
      }

      .margin-right {
        flex: 1;
        background-color: #fff;
        padding-left: 75px;
        border-radius: 0 12px 12px 0;

        h1 {
          margin-top: 85px;
          // text-align: center;
          color: #333333;
          font-size: 32px;
        }

        .title-content {
          height: 350px;
          width: 322px;
          padding-top: 56px;
          margin-bottom: 40px;

          img {
            width: 100%;
          }

          .content {
            height: 267px;
            overflow-y: scroll;
          }

          h2 {
            font-size: 18px;
            margin-bottom: 12px;
            color: #333;
          }

          p {
            color: #666;
            font-size: 16px;
            word-wrap: break-word;
          }
        }
      }
    }
  }

  #container {
    width: 100%;
    height: 100%;
    background-color: #ffffff;
  }

  .main {
    width: 100%;
    height: 100%;
    background-image: url("../../assets/imgs/loginBg.png");
    background-repeat: no-repeat;
    background-position: left top;
    background-size: auto 100%;

    /deep/ .el-form-item__error {
      margin-left: 40px;
    }

    /deep/ .el-button {
      width: 399px;
      height: 60px;
      border-radius: 20px;

      button {
        width: 399px;
        height: 60px;
        background: linear-gradient(90deg, #2890ff 0%, #2890ff 100%);
        box-shadow: 0px 6px 20px 0px rgba(40, 144, 255, 0.53);
        border-radius: 20px;
      }
    }

    /deep/ .el-tabs {
      position: relative;
      top: 60px;
    }

    /deep/ .el-tabs__header {
      margin-bottom: 50px;
    }

    /deep/ .el-tabs__nav-wrap::after {
      content: "";
      position: absolute;
      left: 0;
      bottom: 0;
      width: 420px;
      height: 2px;
      background-color: #e4e7ed;
      z-index: 1;
    }

    /deep/ #tab-first {
      width: 210px;
      text-align: center;
      height: 16px;
      // font-size: 16px;
      font-family: Source Han Sans CN;
      font-weight: 400;
      // color: #999999;
    }

    /deep/ #tab-second {
      width: 200px;
      text-align: center;
    }

    /deep/ .content {
      display: flex;
      align-items: center;
      justify-content: center;
      box-sizing: border-box;
      width: 100%;
      height: 100%;
      z-index: 999;
    }

    .el-aside {
      width: 57%;

      img {
        width: 100%;
      }
    }

    /deep/ .image_box img {
      width: 1103px;
      height: 526px;
      border-style: none;
    }

    /deep/ .right_form {
      width: 800px;
      display: flex;
      box-sizing: border-box;
      flex: 1;
      flex-basis: auto;
    }

    /deep/ .form {
      margin-left: 50%;
      transform: translateX(-50%);
    }

    /deep/ .form img {
      margin-left: 50%;
      width: 151px;
      height: 50px;
      transform: translateX(-50%);
    }

    /deep/ .form_login {
      width: 420px;
    }

    /deep/ .demo-ruleForm {
      width: 420px;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;

      .el-form-item__content {
        position: relative;
      }

      .login-code {
        position: absolute;
        top: 50%;
        transform: translateY(-50%);
        right: 20px;
        width: 77px;
        height: 36px;
        // border-radius: 20px;
        // background-color: pink;
        overflow: hidden;
        line-height: 1;
        // padding-top: 12px;
        z-index: 99999;

        img {
          // border-radius: 20px;
          width: 77px;
          height: 36px;
        }
      }
    }

    /deep/.el-form {
      .el-form-item {
        width: 400px;
        height: 60px;
        margin-bottom: 30px;

        .el-form-item__content {
          margin-left: 0px !important;

          .el-input {
            width: 400px;
            height: 60px;

            input {
              width: 400px;
              height: 60px;
              border: 1px solid #d5dbe8;
              border-radius: 20px;
              padding-left: 50px;
            }

            .el-input__prefix {
              left: 20px;

              .el-input__icon {
                margin-top: 3px;
              }

              .el-input__icon:before {
                font-size: 16px;
              }
            }

            .el-input__suffix {
              right: 20px;
            }
          }

          .el-button {
            font-size: 16px;
          }
        }
      }
    }

    .footer {
      width: 100%;
      height: 94px;
      display: flex;
      justify-content: center;
      align-items: center;
      position: fixed;
      bottom: 0px;
    }

    .footer .footer_box {
      text-align: center;
      width: 100%;
      height: 94px;
      font-size: 12px;
      font-family: Source Han Sans CN;
      font-weight: 400;
      color: #97a0b4;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
    }

    .footer_box {
      ul {
        display: flex;

        li {
          &:not(:last-child)::after {
            content: "|";
            margin: 0 5px;
          }

          a {
            color: #778ba1;

            &:hover {
              color: #2890ff;
            }
          }
        }
      }

      .introduce {
        margin-top: 10px;
        display: flex;
        align-items: center;
        justify-content: center;

        .hr {
          margin: 0 5px;
        }
      }
    }

    /deep/.el-input__inner {
      border: 1px solid #d5dbe8;
    }

    /deep/.el-input__inner:hover {
      border: 1px solid #b2bcd4;
    }

    /deep/.el-input__inner:focus {
      border-color: #409eff !important;
    }

    /deep/.el-input__inner::-webkit-input-placeholder {
      color: #97a0b4;
    }

    .laike {
      margin-bottom: 80px;
    }
  }
</style>
