<template>
  <div class="container">
    <div class="parameters-change">
      <el-form :model="ruleForm" :rules="rules1" ref="payPalForm" class="wx-form" label-width="210px">

        <el-form-item label="授权域名" class="prompt " style="margin: 0px;">
          <div class="red-prompt">
            <span style=" width: max-content;">{{ ruleForm.domain }}</span>
          </div>
        </el-form-item>

        <el-form-item label="授权证书" class="prompt" prop="cert_p12">
          <el-upload :action="actionUrl" :data="uploadData" :show-file-list="true" :on-success="handleAvatarSuccess" :on-change="handleFileChange" :before-remove="delFile" :multiple="false" :file-list="cert_p12_list" accept="" :limit='1'>
            <el-button size="small" type="primary">{{$t('payManagement.scwj')}}</el-button>
          </el-upload>

        </el-form-item>

        <div class="form-footer">
          <el-form-item>
            <el-button class="bgColor" type="primary" @click="submitForm('payPalForm')">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
            <el-button class="bdColor" plain @click="goToPurchase">还没有授权? 去购买</el-button>
          </el-form-item>
        </div>
      </el-form>

      <div style="width:121px"></div>

    </div>
  </div>
</template>

   <script>
import { paymentParmaInfo, setPaymentParma } from '@/api/mall/payManagement'
import Config from "@/packages/apis/Config";
import { getStorage } from '@/utils/storage'
let { entries } = Object
export default {
  name: 'parameterModify',

  data() {
    return {
      ruleForm: {
        status: '',
        domain: '',
        cert_p12: '',
      },
      fileBinary: '', // 存储获取到的二进制数据（ArrayBuffer 类型）
      cert_p12_list: [],
      actionUrl: Config.baseUrl,
      rules1: {
        cert_p12: [
          { required: true, message: '请上传文件', trigger: 'change' }
        ],
        domain: [
          { required: true, message: this.$t('payManagement.qsrcdmc'), trigger: 'blur' }
        ],
      },
    }
  },

  computed: {
    uploadData() {
      {
        return {
          api: 'admin.system.uploadAuth',
          storeId: getStorage('laike_admin_userInfo').storeId,
          accessId: this.$store.getters.token
        }
      }
    }
  },
  watch: {
    'ruleForm.cert_p12': {
      handler(newVal, loading) {
        this.$refs.payPalForm.validateField('cert_p12')
      },
      deep: true
    },
  },
  beforeRouteLeave(to, from, next) {
    if (to.name == 'payList') {
      to.params.dictionaryNum = this.$route.query.dictionaryNum
      to.params.pageSize = this.$route.query.pageSize
    }

    // this.delFile()
    next();
  },

  created() {
    this.paymentParmaInfos()
  },

  methods: {
    handleAvatarSuccess(res, file) {
      console.log(res)
      if (res.code === '200') {
        this.ruleForm.cert_p12 = res.data.domain
        this.ruleForm.domain = res.data.domain
      } else {

        this.$message({
          message: res.message,
          type: 'error',
          offset: 100
        })
        setTimeout(() => {
          this.cert_p12_list = []
        }, 1000)
      }
    },

    // 新增跳转方法
    goToPurchase() {
      // 新标签页打开（推荐，不丢失当前页面）
      window.open('https://www.laiketui.com/', '_blank')
      // 若需在当前页面跳转，改用：
      // window.location.href = 'https://www.laiketui.com/'
    },
    delFile(file) {
      this.ruleForm.cert_p12 = ''
      this.ruleForm.domain = ''
    },
    handleFileChange(file, fileList) {
      // 1. 更新文件列表（与组件的 file-list 同步）
      // this.cert_p12_list = fileList;
      console.log(fileList)
      const rawFile = file.raw;
      if (!rawFile) return; // 防止无文件时报错

      const reader = new FileReader();

      reader.onload = (e) => {
        this.fileBinary = e.target.result;
        // 若需转为其他格式（如 Base64），可改用 reader.readAsDataURL(rawFile)，此时 result 为 Base64 字符串
      };

      // 5. 读取失败的回调
      reader.onerror = (error) => {
        this.$message.error("文件读取失败，请重新选择");
      };

      // 6. 执行读取操作：以 ArrayBuffer 格式读取文件（核心步骤）
      reader.readAsArrayBuffer(rawFile);
    },
    async paymentParmaInfos() {
      let data1 = {
        api: 'admin.system.getAuthPath',
      }
      let formData = new FormData()
      for (let [key, value] of entries(data1)) {
        formData.append(key, value)
      }
      const res = await paymentParmaInfo(formData)

      const { data, code } = res.data
      if (code === '200') {
        this.ruleForm.domain = data.domain
        this.ruleForm.pub_pem = data.path
        if (data.domain && data.domain.length > 0) {

          this.cert_p12_list = [{ name: 'laike_license', url: '' }]
        }
      }
    },

    submitForm(formName) {

      this.$refs[formName].validate(async (valid) => {
        if (valid) {

          let mydata = {
            api: 'admin.system.uploadAuth',
            isSave: 1,
            file: new Blob([this.fileBinary])
          }
          let myformData = new FormData()
          for (let [key, value] of entries(mydata)) {
            if (key == 'json') {
              myformData.append(key, JSON.stringify(value))
            } else {
              myformData.append(key, value)
            }
          }
          setPaymentParma(myformData).then(res => {
            if (res.data.code != '200') {
              this.$message({
                message: res.data.message,
                type: 'error',
                offset: 100
              })
              return
            } else {
              this.$message({
                message: this.$t('zdata.xgcg'),
                type: 'success',
                offset: 100
              })

            }
          })
        }
      });
    },
  }
}
   </script>

   <style scoped lang="less">
.container {
  width: 100%;
  height: 737px;
  background-color: #fff;
  padding: 40px 0 0 0;
  color: #414658;
  /deep/.parameters-change {
    display: flex;
    justify-content: center;
    height: 100%;
    overflow: hidden;
    overflow-y: auto;
    .wx-form {
      .el-form-item {
        display: flex;
        &:not(:last-child) {
          .el-form-item__content {
            margin-left: 0px !important;
            //   新增
            //   margin-bottom: 20px;
          }
        }
        .el-form-item__content {
          display: flex;
          align-items: center;
          width: 580px;
        }
        .select-input {
          width: 580px;
          height: 40px;
        }
      }
      .prompt {
        position: relative;
        // margin-bottom: 20px;
        .red-prompt {
          top: 0;
          // position: absolute;
          left: 590px;
          width: 560px;
          height: 30px;
          margin-top: -10px;
          span {
            display: inline-block;
            height: 30px;
            color: #97a0b4;
          }
        }
      }

      .addHeight {
        margin-bottom: 62px;
        .el-form-item__content {
          height: 100px;
          .el-textarea {
            height: 100px;
            textarea {
              height: 100px;
            }
          }
        }
      }

      .form-footer {
        padding-bottom: 20px;
      }
    }
  }

  .black-prompt {
    color: #97a0b4;
    margin-top: 10px;
    height: 30px;
    margin-top: -10px;
  }

  /deep/.el-form-item__label {
    font-weight: normal;
  }

  .form-footer {
    .el-form-item {
      .el-form-item__content {
        button {
          &:first-child {
            margin-left: 0;
          }
        }
      }
    }
  }
}
</style>
