<template>
  <div class="container">

  <div class="basic-configuration merchants-list"  >
      <el-form
        :model="ruleForm"
        :rules="rules"
        label-position="right"
        ref="ruleForm"
        label-width="auto"
        class="form-search"
      >
        <div>
          <div class="form-card">
            <div class="title"></div>

            <el-form-item
              :label="$t('systemsSetter.scmc')"
            >
              <el-input
                v-model="ruleForm.store_name"
                style="width: 370px"
              ></el-input>
            </el-form-item>
            <el-form-item
              :label="$t('systemsSetter.ydddz')"
              prop="h_Address"
            >
              <el-input
                v-model="ruleForm.h_Address"
                style="width: 370px"
              ></el-input>
              <span class="prompt"
                > {{ $t(`systemsSetter.yyztgl`) }} </span
              >
            </el-form-item>
            <el-form-item :label="$t('systemManagement.qdxx')">
              <el-input
                @blur="
                  ruleForm.messageSaveDay = oninput2(ruleForm.messageSaveDay)
                "
                @keyup.native="
                  ruleForm.messageSaveDay = oninput3(ruleForm.messageSaveDay)
                "
                v-model="ruleForm.messageSaveDay"
                style="width: 370px"
              >
                <template slot="append">{{
                  $t("systemManagement.day")
                }}</template>
              </el-input>
              <span class="prompt"
                > {{ $t(`systemManagement.znxxtshi`) }} </span
              >
            </el-form-item>
            <el-form-item :label="$t('systemManagement.ydddl')">
              <el-input
                v-model="ruleForm.login_validity"
                @keyup.native="
                  ruleForm.login_validity = oninput3(ruleForm.login_validity)
                "
                @blur="
                  ruleForm.login_validity = oninput2(ruleForm.login_validity)
                "
                style="width: 370px"
              >
                <template slot="append">{{
                  $t("systemManagement.hour")
                }}</template>
              </el-input>
              <span class="prompt"
                > {{ $t(`systemManagement.yddyhtishi`) }} </span
              >
            </el-form-item>
            <el-form-item
              :label="$t('systemConfig.bqxx')"
            >
              <el-input
                v-model="formMain.copyright_information"
                :placeholder="$t('systemConfig.qsrbqxx')"
                style="width: 370px"
              ></el-input>
            </el-form-item>
            <el-form-item
              :label="$t('systemsSetter.baxx')"
            >
              <el-input
                v-model="formMain.record_information"
                :placeholder="$t('systemConfig.qsrbaxx')"
                style="width: 370px"
              ></el-input>
            </el-form-item>

           <el-form-item class="link" :label="$t('systemConfig.dlyyq')">
          <div
            v-for="(item, index) in formMain.linkPage"
            :key="index"
            class="linkBox"
          >
            <el-input
              class="link-name"
              v-model="item.name"
              :placeholder="$t('systemConfig.qsrljmc')"
            ></el-input>
            <el-input
              class="link-local"
              v-model="item.url"
              :placeholder="$t('systemConfig.qsrljdz')"
            >
            </el-input>
            <div class="add-reduction">
              <i
                class="el-icon-remove-outline"
                @click="delUrl(index)"
                v-if="
                  formMain.linkPage.length > 0 && formMain.linkPage.length != 1
                "
              ></i>
              <i
                class="el-icon-circle-plus-outline"
                @click="addUrl()"
                v-if="index + 1 === formMain.linkPage.length"
              ></i>
              <span class="prompt2" v-if="index == 0">{{
                $t("systemConfig.yypt")
              }}</span>
            </div>
          </div>
        </el-form-item>
          </div>

          <div class="footerBox">
            <el-button
              type="primary"
              class="footer-save bgColor mgleft"
              @click="submitForm('ruleForm')"
              >{{ $t("DemoPage.tableFromPage.save") }}</el-button
            >
          </div>
        </div>

      </el-form>

    </div>
</div>
</template>


<script>
import {
  getSystemIndex,
  saveAgreement
} from '@/api/mall/systemManagement'
export default {
  // mixins:[basicConfiguration],
  name: 'systemConfig',

  //初始化数据
  data() {
    return {
      radio1:1,
      language:"",
      formMain: {
        logonLogo:'',
        copyright_information:'',
        record_information:'',
        adminDefaultPortrait:'',
        linkPage:[{}]
      },
      dialogVisible:false,
      ruleForm2:{
        id:'',
      },
      formRules : {
        logonLogo: [{required: true, message: this.$t('systemConfig.qxzlogo'), trigger: 'change'}],
        copyright_information: [{required: true, message: this.$t('systemConfig.qsrbqxx'), trigger: 'blur'}],
        record_information: [{required: true, message: this.$t('systemConfig.qsrbaxx'), trigger: 'blur'}],
         adminDefaultPortrait:[{required: true,message: this.$t('systemConfig.qscmrtp'),trigger: 'change' }]
      },
      ruleForm: {
        messageSaveDay: '',
        login_validity: '',
        watermarkName: '',
        watermarkUrl: '',
        h_Address: '',
        store_name:"",
      },
      rules2: {
        id: [
          {
            required: true,
            message: this.$t('systemConfig.text2'),
            trigger: 'blur'
          }
        ],
      },
      rules: {
        printName: [
          {
            required: true,
            message: this.$t('systemManagement.dymcts'),
            trigger: 'blur'
          }
        ],
        printUrl: [
          {
            required: true,
            message: this.$t('systemManagement.dywzts'),
            trigger: 'blur'
          }
        ],

        express_secret: [
          {
            required: true,
            message: this.$t('systemManagement.qsrsecret'),
            trigger: 'blur'
          }
        ],
        address: [
          {
            required: true,
            message: this.$t('systemManagement.dydzts'),
            trigger: 'blur'
          }
        ],
        phone: [
          {
            required: true,
            message: this.$t('systemManagement.dydhts'),
            trigger: 'blur'
          }
        ],

        tx_key: [
          {
            required: true,
            message: this.$t('systemManagement.qtxkfz'),
            trigger: 'blur'
          }
        ],
        watermarkName: [
          {
            required: true,
            message: this.$t('systemManagement.qtxmc'),
            trigger: 'blur'
          }
        ],
        watermarkUrl: [
          {
            required: true,
            message: this.$t('systemManagement.qtxwz'),
            trigger: 'blur'
          }
        ],
        limitNum: [
          {
            required: true,
            message: this.$t('searchConfig.qsrgjcsx'),
            trigger: 'blur'
          }
        ],
        keyword: [
          {
            required: true,
            message: this.$t('searchConfig.qsrgjc'),
            trigger: 'blur'
          }
        ],
        accountsSet: [
          {
            required: true,
            message: this.$t('systemManagement.qsrdycfzzh'),
            trigger: 'blur'
          }
        ],
        h_Address: [
          {
            required: true,
            message: this.$t('systemManagement.qtxh5'),
            trigger: 'blur'
          }
        ],
      },

      isRegisteredList: [
        {
          value: 2,
          name: this.$t('systemManagement.zc')
        },
        {
          value: 0,
          name: this.$t('systemManagement.mzc')
        }
      ],
      goodsEditorBase: ''
    }
  },
  //组装模板
  created() {
    this.language = this.getCookit()
    this.getSystemIndexs()
  },

  methods: {
    // 获取基础配置
    async getSystemIndexs() {
      let { entries } = Object
      let data = {
        api: 'admin.system.GetBasicConfiguration'
      }
      let formData = new FormData()
      for (let [key, value] of entries(data)) {
        formData.append(key, value)
      }
      const res = await getSystemIndex(formData)
      let systemInfo = res.data.data.list
      console.log('systemInfo',systemInfo)

      // 前端配置
      this.ruleForm.messageSaveDay = Number(systemInfo.messageSaveDay || 0)
      this.ruleForm.login_validity =  Number(systemInfo.appLoginValid || 0)
      this.ruleForm.h_Address =  systemInfo.h5_domain || ''

      // 水印配置
      this.ruleForm.watermarkName = systemInfo.watermark_name || ''
      this.ruleForm.watermarkUrl = systemInfo.watermark_url || ''
      this.ruleForm.store_name = systemInfo.store_name || ''

      // 系统配置

      // 登录页logo
      this.formMain.logonLogo = systemInfo.logon_logo
      // 版权信息
      this.formMain.copyright_information = systemInfo.copyright_information
      // 备案信息
      this.formMain.record_information = systemInfo.record_information

      // 登录页友情链接
      this.formMain.linkPage = systemInfo.link_to_landing_page ? JSON.parse(systemInfo.link_to_landing_page ) :[{}]
      // 默认头像设置
      this.formMain.adminDefaultPortrait = systemInfo.admin_default_portrait
    },
    // 设置基础配置
    submitForm( ) {
        this.$validateAndScroll('ruleForm').then((value) => {
          console.log('value',value)
                if(typeof value =='boolean' &&  value){
                    try {
                      saveAgreement({
                        api: 'admin.system.SetBasicConfiguration',
                        ...this.ruleForm,
                        pageDomain: this.ruleForm.h_Address,      //h5地址
                        logon_logo: this.formMain.logonLogo ,     // 登录logo
                        copyright_information: this.formMain.copyright_information ,      // 版权信息
                        record_information: this.formMain.record_information ,       // 备案信息
                        link_to_landing_page: this.formMain.linkPage ? JSON.stringify(this.formMain.linkPage) :'' ,      // 登录页友情链接
                        admin_default_portrait: this.formMain.adminDefaultPortrait,       // 默认头像设置

                      }).then(res => {
                        if (res.data.code == '200') {
                          this.$message({
                            message: this.$t('zdata.baccg'),
                            type: 'success',
                            offset: 102
                          })
                          this.getSystemIndexs()
                        }
                      })
                    } catch (err) {
                      this.$message({
                        message: err,
                        type: 'error',
                        offset: 102
                      })
                    }
                }
            })

    },
    // 获取cookiet
    getCookit(){
      let myCookie = document.cookie.split(';').map(item=>{
         let arr = item.split('=')
         return {name:arr[0],value:arr[1]}
       })
       let strCookit = ''
       myCookie.forEach(item=>{
         if(item.name.indexOf('language')!==-1){
           strCookit = item.value
         }
       })
       return strCookit
    },
    addId(){
      this.dialogVisible = true
    },
    handleClose(){
      this.ruleForm2.id = ''
      this.$nextTick(() => {
        this.$refs['ruleForm2'].clearValidate()
      })
      this.dialogVisible = false

    },

    addUrl() {
      this.formMain.linkPage.push({})
    },
    delUrl(index) {
      this.formMain.linkPage.splice(index, 1)
    },
    determine(formName){
      this.$refs[formName].validate(async (valid) => {
        if (valid) {
        //  this.formMain.store_id_prefix = this.ruleForm2.id
         this.handleClose()
        } else {
          return false;
        }
      })
    },

  }
}

</script>
<style scoped lang="less">
@import "../../../../webManage/css/mall/systemManagement/basicConfiguration.less";
/deep/ .form-search{
  padding-bottom: 1px !important
}
.f_container{
  padding: 0 0 20px 0;
}
.container {
  width: 100%;
  // height: 737px;
  // background-color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: start;
  align-items: center;
  overflow: hidden;
  overflow-y: auto;
  border-radius: 4px;
  .gz_bt{
    color: #2890FF;
    border: 0.0625rem solid #2890FF;
    background-color: #fff;
  }
  .prompt{
    position: absolute;
    width: 420px;
    left: 100;
  }

  .dialog-block {
    // 弹框样式
    /deep/.el-dialog {
      width: 580px;
      height: 252px;
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      margin: 0 !important;
      .proportion {
        margin-bottom: 0;
      }
      .el-dialog__header {
        width: 100%;
        height: 58px;
        line-height: 58px;
        font-size: 16px;
        margin-left: 19px;
        font-weight: bold;
        border-bottom: 1px solid #e9ecef;
        box-sizing: border-box;
        margin: 0;
        padding: 0 0 0 19px;
        .el-dialog__headerbtn {
          font-size: 18px;
          top: 0 !important;
        }
        .el-dialog__title {
          font-weight: normal;
        }
      }

      .el-dialog__body {
        padding: 40px !important;
        .el-form-item__label {
          font-weight: normal;
        }
        .form-footer {
          width: 100%;
          height: 72px;
          position: absolute;
          bottom: 0;
          right: 0;
          border-top: 1px solid #e9ecef;
          .el-form-item {
            text-align: right;
            margin-right: 10px;
            padding: 0 !important;
            height: 100%;
            .el-form-item__content {
              height: 100%;
              line-height: 72px;
              margin: 0 !important;
            }
          }
        }
        .el-input {
          width: 360px;
        }
      }
    }
  }
  .xtpz {
        margin-bottom: 40px !important;
  }
  /deep/.xtpz .el-form {
    .el-form-item__label {
      font-weight: normal;
    }
    .copyright, .record{
      .el-form-item__content {
        .el-input {
          width: 580px;
          height: 40px;
          input {
            width: 580px;
            height: 40px;
          }
        }
      }
    }

  }
    .linkBox{
      display: flex;
      margin-bottom: 1.375rem;
      width: 50%;
    }

    .link {
      margin-bottom: 0;
      .el-form-item__content {
        display: flex;
        flex-direction: column;
        .link-name {
          width: 180px;
          height: 40px;
          input {
            width: 180px;
            height: 40px;
          }
        }
        .link-local {
          width: 390px;
          height: 40px;
          margin-left: 10px;
          input {
            width: 390px;
            height: 40px;
          }
        }

        .add-reduction {
          position: relative;
          i {
            font-size: 20px;
            margin-left: 10px;
          }
          .el-icon-circle-plus-outline {
            color: #2890FF;
          }
          span {
            margin-left: 14px;
            position: relative;
            bottom: 3px;
            color: #97A0B4;
          }
          .prompt2{
            position: absolute;
            width: 420px;
            left: 100;
          }
        }
      }
    }

    .add-link {
      .el-form-item__content {
        display: flex;
        flex-direction: column;

        .add-info {
          display: flex;
          &:not(:last-child) {
            margin-bottom: 20px;
          }
          .link-names {
            width: 180px;
            height: 40px;
            input {
              width: 180px;
              height: 40px;
            }
          }
          .link-locals {
            width: 390px;
            height: 40px;
            margin-left: 10px;
            input {
              width: 390px;
              height: 40px;
            }
          }

          .add-reduction {
            i {
              font-size: 20px;
              margin-left: 10px;
            }
            .el-icon-circle-plus-outline {
              color: #2890FF;
            }
          }
        }

      }
    }

    .el-input {
      width: 580px;
    }
  .form-footer {
    .bdColor {
      margin-left: 14px;
    }
    button {
      &:first-child {
        margin-left: 0;
      }
    }
  }

}

/deep/.el-form-item {
  .prompt {
    margin-left: 14px;
    color: #97a0b4;
  }
}
.btn-nav{
  margin-bottom:20px
}
</style>



