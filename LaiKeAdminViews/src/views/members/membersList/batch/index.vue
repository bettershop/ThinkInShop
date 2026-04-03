<template>
  <div class="container">
    <div class="operation-prompt">
      <div class="logo">
        <img src="@/assets/imgs/czts.png" alt="" />
        <span>{{$t('batch.drsm')}}</span>
      </div>
      <div class="prompt-content">
        <p>① {{$t('batch.text1')}}</p>
        <p>② {{$t('batch.text2')}}</p>
        <p>③ {{$t('batch.text3')}}</p>
      </div>
    </div>
    <div class="add-bank">
      <el-form
        :model="ruleForm"
        :rules="rules"
        ref="ruleForm"
        label-width="100px"
        class="demo-ruleForm"
      >

        <el-form-item class="Select" :label="$t('batchImport.scmb')" required="">
          <el-upload
            :disabled="tag"
            class="upload-demo"
            drag
            accept=".xlsx,.xls,.csv"
            action=""
            data=""
            :show-file-list="false"
            :before-upload="handleBeforeUpload"
            :on-success="handleUploadSuccess"
            :on-change="changes"
            multiple
          >
            <i class="el-icon-upload" v-show="!file"></i>
            <div class="el-upload__text" v-show="!file">{{$t('batchImport.djscwj')}}</div>
            <i class="el-icon-document" v-show="file"></i>
            <div class="el-upload__text" v-show="file">
              {{ fileName
              }}<span class="del" @click.stop="clearFile">{{$t('batchImport.sc')}}</span>
            </div>
          </el-upload>
          <!-- JAVA用user_Import.xlsx  PHP用user_Import.csv -->
          <span class="uploads"><a href="./user_Import.xlsx">{{$t('batchImport.xzpld')}}</a></span>
          <!-- <span class="uploads"><a href="./user_Import.csv">{{$t('batchImport.xzpld')}}</a></span> -->

        </el-form-item>
        <div class="form-footer">
          <el-form-item>
            <el-button
              class="bgColor"
              type="primary"
              @click="submitForm('ruleForm')"
              >{{$t('batchImport.dr')}}</el-button
            >
            <el-button class="bdColor" @click="$router.go(-1)" plain>{{
              $t("DemoPage.tableFromPage.cancel")
            }}</el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import { batchDelivery,} from '@/api/members/membersList'
import { getStorage } from "@/utils/storage";
import Config from "@/packages/apis/Config";
let actionUrl = Config.baseUrl;
export default {
  name: "batch",

  data() {
    return {
      actionUrl,
      ruleForm: {
        template: "",
        goodsClass: "",
        goodsBrand: "",
        freight: "",
      },
      rules: {
        template: [
          { required: true, message: this.$t('batchImport.qscmb'), trigger: "change" },
        ],
        goodsClass: [
          { required: true, message: this.$t('batchImport.qxzspf'), trigger: "change" },
        ],
        goodsBrand: [
          { required: true, message: this.$t('batchImport.qxzspp'), trigger: "change" },
        ],
        freight: [{ required: true, message: this.$t('batchImport.qxzyf'), trigger: "change" }],
      },
      classList: [],
      brandList: [],
      freightList: [],
      file: null,
      fileName: "",
      tag: false,
      base: "../../../../assets/product_Import.xlsx",
    };
  },

  computed: {
    uploadData() {
      {
        return {
          api: "admin.mch.order.batchDelivery",
          storeId: getStorage("laike_admin_userInfo").storeId,
          accessId: getStorage("laike_admin_userInfo").token,
        };
      }
    },
  },

  created() {
    this.getBase();
  },

  methods: {
    frontDownload() {
      var a = document.createElement("a"); //创建一个<a></a>标签
      a.href = "./product_Import.xlsx"; // 给a标签的href属性值加上地址，注意，这里是绝对路径，不用加 点.
      a.download = "下载啦.xlsx"; //设置下载文件文件名，这里加上.xlsx指定文件类型，pdf文件就指定.fpd即可
      a.style.display = "none"; // 障眼法藏起来a标签
      document.body.appendChild(a); // 将a标签追加到文档对象中
      a.click(); // 模拟点击了a标签，会触发a标签的href的读取，浏览器就会自动下载了
      a.remove(); // 一次性的，用完就删除a标签
    },
    getBase() {
      this.base =
          process.env.VUE_APP_BASE +
          process.env.VUE_APP_IMAGE +
          "/public/%E6%89%B9%E9%87%8F%E5%8F%91%E8%B4%A7%E6%A8%A1%E6%9D%BF.xlsx";
    },
    // 上传之前的处理
    handleBeforeUpload(file) {
      this.fileName = file.name;
      this.file = file;
      this.tag = true;
    },

    // 上传成功
    handleUploadSuccess(res) {
      console.log(res);
      if (res.code == "200") {
        this.$message({
          type: "success",
          message: this.$t('zdata.cg'),
          offset: 100,
        });
      } else {
        this.$message({
          type: "error",
          message: res.message,
          offset: 100,
        });
      }
    },

    changes() {},

    clearFile() {
      this.file = "";
      this.tag = false;
    },

    submitForm(formName) {
      this.$refs[formName].validate(async (valid) => {
        console.log(this.ruleForm);
        if (valid) {
          try {
            if (!this.file) {
              this.$message({
                type: "error",
                message: this.$t('batchImport.qscwj'),
                offset: 100,
              });
            } else {
              this.$store.commit("loading/SET_LOADING");
              let { entries } = Object;
              let data = {
                api: "admin.user.uploadAddUser",
              };
              let formData = new FormData();
              for (let [key, value] of entries(data)) {
                formData.append(key, value);
              }

              formData.append("image", this.file);
              batchDelivery(formData).then((res) => {
                console.log('res',res);

                if (res.data.code == '200') {
                  this.$message({
                    type: "success",
                    message: this.$t('batchImport.pldrsm'),
                    offset: 100,
                  });
                   setTimeout(()=>{
                     this.$router.push("/members/membersList/batchRecord");
                     this.$store.commit("loading/SET_LOADING");
                  },1500)
                } else {
                  // this.$message({
                  //     type: 'error',
                  //     message: '发货失败，请前往批量发货记录查看原因',
                  //     offset: 100
                  // })
                  setTimeout(()=>{
                     this.$router.push("/members/membersList/batchRecord");
                     this.$store.commit("loading/SET_LOADING");
                  },1500)
                }
              });
            }
          } catch (error) {
            this.$message({
              message: error.message,
              type: "error",
              showClose: true,
            });
          }
        } else {
          console.log("error submit!!");
          return false;
        }
      });
    },
  },
};
</script>

<style scoped lang="less">
.container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  // margin-bottom: 10px;
  .operation-prompt {
    width: 100%;
    // height: 224px;
    background: #e9f4ff;
    border: 1px dashed #2890ff;
    border-radius: 4px;
    padding: 17px 0 23px 17px;
    margin-bottom: 16px;
    box-sizing: border-box;
    .logo {
      display: flex;
      align-items: center;
      margin-bottom: 10px;
      img {
        width: 20px;
        height: 20px;
      }
      span {
        padding-left: 5px;
        font-size: 14px;
        font-weight: bold;
        color: #2890ff;
      }
    }
    .prompt-content {
      padding-left: 23px;
      p {
        color: #6a7076;
        &:not(:first-child) {
          margin-top: 8px;
        }
      }
    }
  }
  .add-bank {
    width: 100%;
    flex: 1;
    background-color: #fff;
    display: flex;
    justify-content: center;
    padding-top: 40px;
    box-sizing: border-box;
    border-radius: 4px;
    /deep/.el-form {
      .el-form-item__label {
        font-weight: normal;
      }
      .Select {
        .el-form-item__content {
          line-height: 1;
          .upload-demo {
            width: 580px;
            height: 180px;
            margin-bottom: 20px;
            .el-upload {
              width: 580px;
              .el-upload-dragger {
                width: 580px;
                .el-icon-document {
                  font-size: 46px;
                  margin-top: 40px;
                  color: #c0c4cc;
                }
                .el-upload__text {
                  color: #414658;
                  .del {
                    color: #2890ff;
                    margin-left: 10px;
                  }
                }
                .el-icon-document {
                  margin-bottom: 20px;
                }
              }
            }
          }
          .uploads {
            color: #2890ff;
            cursor: pointer;
          }
        }
      }

      .select-input {
        width: 580px;
      }
    }
  }
}
</style>
