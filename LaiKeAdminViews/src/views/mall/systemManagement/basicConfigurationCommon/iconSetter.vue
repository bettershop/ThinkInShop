<template>
  <div class="container">
    <div class="basic-configuration merchants-list"  >
     <el-form
       :model="formMain"
       :rules="formRules"
       ref="ruleForm1"
       :label-width="language == 'en' ? 'auto' : '120px'"
       class="form-search"
     >
      <!-- 系统配置 -->
      <div>
        <div class="form-card">
          <div class="title">{{ $t("systemManagement.qdpz") }}</div>

            <el-form-item :label="$t('systemConfig.yddrktp')" prop="logonLogo" required>
              <l-upload
                :limit="1"
                :size="86"
                :heightSize="86"
                v-model="formMain.app_logo"
                :text="$t('systemConfig.yddrktpjycc')"
              >
              </l-upload>
            </el-form-item>
            <el-form-item :label="$t('systemConfig.llqicon')" prop="logonLogo" required>
              <l-upload
                :limit="1"
                :size="86"
                :heightSize="86"
                v-model="formMain.html_icon"
                :text="$t('systemConfig.llqiconjycc')"
              >
              </l-upload>
            </el-form-item>
            <el-form-item :label="$t('systemConfig.htdllg')" prop="logonLogo" required>
              <l-upload
                :limit="1"
                :size="152"
                :heightSize="50"
                v-model="formMain.logonLogo"
                :text="$t('systemConfig.htdllgjycc')"
              >
              </l-upload>
            </el-form-item>
            <el-form-item :label="$t('systemConfig.schtlg')" prop="store_logo" required>
              <l-upload
                :limit="1"
                :size="152"
                :heightSize="50"
                v-model="formMain.store_logo"
                :text="$t('systemConfig.schtzsjlg')"
              >
              </l-upload>
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
// import basicConfiguration from "@/webManage/js/mall/systemManagement/basicConfiguration";
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
        app_logo:'',
        html_icon:'',
        store_logo:'',
      },
      dialogVisible:false,
      ruleForm2:{
        id:'',
      },
      formRules : {
        logonLogo: [{required: true, message: this.$t('systemConfig.qxzlogo'), trigger: 'change'}],
        app_logo: [{required: true, message: this.$t('systemConfig.qxzlogo'), trigger: 'change'}],
        html_icon: [{required: true, message: this.$t('systemConfig.qxzlogo'), trigger: 'change'}],
        store_logo: [{required: true, message: this.$t('systemConfig.qxzlogo'), trigger: 'change'}],
      },

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
      // 登录页logo
      this.formMain.logonLogo = systemInfo.logon_logo
      this.formMain.app_logo = systemInfo.app_logo
      this.formMain.html_icon = systemInfo.html_icon
      this.formMain.store_logo = systemInfo.store_logo
      
     
    },
   
    // 设置基础配置
    submitForm( ) {
        this.$validateAndScroll('ruleForm1').then((value) => {
          console.log('value',value)
                if(typeof value =='boolean' &&  value){
                    try {
                      saveAgreement({
                        api: 'admin.system.SetBasicConfiguration',
                        logon_logo: this.formMain.logonLogo ,     // 登录logo
                        app_logo: this.formMain.app_logo ,         // app logo
                        html_icon: this.formMain.html_icon ,       // 浏览器图标
                        store_logo: this.formMain.store_logo ,         // 商城logo
                      }).then(res => {
                        if (res.data.code == '200') {
                          this.$message({
                            message: this.$t('zdata.baccg'),
                            type: 'success',
                            offset: 102
                          })
                           this.setDynamicFavicon(this.formMain.html_icon)
                           
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
    .linkBox{
      display: flex;
      margin-bottom: 1.375rem;
      // margin-top: 10px;
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



