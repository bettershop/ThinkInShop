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
              <!-- <el-tabs v-model="activeName">
                <el-tab-pane label="平台登录" name="first">
                  <el-form
                    :model="platformForm"
                    status-icon
                    :rules="rules1"
                    ref="platformForm"
                    label-width="100px"
                    class="demo-ruleForm"
                  >
                    <el-form-item prop="userName">
                      <el-input
                        placeholder="请输入管理员账号"
                        ref="user"
                        type="text"
                        v-model="platformForm.userName"
                        autocomplete="off"
                        @keyup.enter.native="platformLogin"
                        prefix-icon="el-icon-user"
                      ></el-input>
                    </el-form-item>
                    <el-form-item prop="pwd">
                      <el-input
                        placeholder="请输入密码"
                        ref="pass"
                        type="password"
                        v-model="platformForm.pwd"
                        autocomplete="off"
                        @keyup.enter.native="platformLogin"
                        prefix-icon="el-icon-lock"
                      ></el-input>
                    </el-form-item>
                    <div>
                      <el-form-item>
                        <el-button type="primary" @click.native.prevent="platformLogin">登录</el-button>
                      </el-form-item>
                    </div>
                  </el-form>
                </el-tab-pane>
                <el-tab-pane label="商户登录" name="second">
                  <el-form
                    :model="merchantsForm"
                    status-icon
                    :rules="rules2"
                    ref="merchantsForm"
                    label-width="100px"
                    class="demo-ruleForm"
                  >
                      <el-form-item prop="customerNumber">
                        <el-input
                          placeholder="请输入商户编号"
                          ref="userNum"
                          type="text"
                          v-model="merchantsForm.customerNumber"
                          autocomplete="off"
                          prefix-icon="el-icon-date"
                        ></el-input>
                      </el-form-item>

                      <el-form-item prop="userName">
                        <el-input
                          placeholder="请输入管理员账号"
                          ref="userName"
                          type="text"
                          v-model="merchantsForm.userName"
                          autocomplete="off"
                          prefix-icon="el-icon-user"
                        ></el-input>
                      </el-form-item>
                      <el-form-item prop="pwd">
                        <el-input
                          placeholder="请输入管理员密码"
                          ref="pwd"
                          type="password"
                          v-model="merchantsForm.pwd"
                          autocomplete="off"
                          prefix-icon="el-icon-lock"
                        ></el-input>
                      </el-form-item>
                    <div>
                      <el-form-item>
                        <el-button :loading="loading" type="primary" @click.native.prevent="merchantsLogin('merchantsForm')">登录</el-button>
                      </el-form-item>
                    </div>
                  </el-form>
                </el-tab-pane>
              </el-tabs> -->
              <el-form :model="platformForm" status-icon :rules="rules1" ref="platformForm" label-width="100px"
                class="demo-ruleForm">
                <el-form-item prop="userName">
                  <el-input :placeholder="$t('login.qsrgly')" ref="user" type="text" v-model="platformForm.userName"
                    autocomplete="off" @keyup.enter.native="platformLogin" prefix-icon="el-icon-user"></el-input>
                </el-form-item>
                <el-form-item prop="pwd">
                  <el-input :placeholder="$t('login.qsrmm')" ref="pass" type="password" v-model="platformForm.pwd"
                    autocomplete="off" @keyup.enter.native="platformLogin" prefix-icon="el-icon-lock"></el-input>
                </el-form-item>

                <!-- <el-form-item prop="imgCode" style="position: relative">
                  <el-input @keyup.enter.native="platformLogin" :placeholder="$t(`login.qsryzm`)"
                    v-model="platformForm.imgCode">
                    <div slot="prefix"
                      style="height: 100%;width: 1rem;margin-left: 6px;display: flex;justify-content: center;flex-direction:column">
                      <img style="width:1rem;height:1rem" :src="require('@/assets/images/shield_icon.png')" alt />
                    </div>
                  </el-input>
                  <div class="login-code" @click="handleVerificationCode" v-loading="is_img_loading">
                    <img :src="codeImg" alt="" />
                    <div class="codeImgGQ" v-if="codeImgGQ">
                      <i class="el-icon-refresh-right"></i>
                    </div>
                  </div>
                </el-form-item> -->

                <div>
                  <el-form-item>
                    <el-button type="primary" @click.native.prevent="platformLogin">{{ $t("login.dl") }}</el-button>
                  </el-form-item>
                </div>
              </el-form>
            </div>
          </div>
        </div>
      </div>

      <div class="footer">
        <div class="footer_box" style='display:none'>
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
            <span v-if="copyright_information" class="hr">{{
              $t("login.bqsy")
            }}</span>
            <span>{{ record_information }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>  
  export default {
    data() {
      var validateUser = (rule, value, callback) => {
        if (value === "") {
          callback(new Error(this.$t("login.qsrgly")));
        } else {
          // if (this.ruleForm.user !== "") {
          //   this.$refs.ruleForm.validateField("username");
          // }
          callback();
        }
      };
      var validatePass = (rule, value, callback) => {
        if (value === "") {
          callback(new Error(this.$t("login.qsrmm")));
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
          callback(new Error(this.$t("login.qsrshbh")));
        } else {
          // if (this.ruleForm.userNum !== "") {
          //   this.$refs.ruleForm.validateField("userNum");
          // }
          callback();
        }
      };
      var validateName = (rule, value, callback) => {
        if (value === "") {
          callback(new Error(this.$t("login.qsrgly")));
        } else {
          // if (this.ruleForm.name !== "") {
          //   this.$refs.ruleForm.validateField("name");
          // }
          callback();
        }
      };
      return {
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
    created() {
        this.$store.dispatch("user/getSystemIcon" )
    },
    //组件销毁时
    beforeDestroy() {
      clearTimeout(this.setTime); //清除定时器
    },

    methods: {
    
      envSetFun() {
        this.baseUrl = process.env.VUE_APP_BASE_API;
      },
      platformLogin() {
        let me = this;
        this.$refs.platformForm.validate((valid) => {
          if (valid) {
            if (this.loginType) {
              this.loginType = false;
              this.loading = true;
              this.$store
                .dispatch("user/login", this.platformForm)
                .then((res) => {
                  console.log(res, this.platformForm);
                  if (res.data.code == 200) {
                    me.loginType = false;
                  } else {
                    me.loginType = true;
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
                  me.loginType = true;
                  this.loading = false;
                  // this.handleVerificationCode();
                });
            }
          } else {
            console.log("error submit!!");
            // this.handleVerificationCode();
            return false;
          }
        });
      },
    },
  };
</script>

<style scoped lang="less">
  #container {
    width: 100%;
    height: 100%;
    background-color: #ffffff;
  }
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
