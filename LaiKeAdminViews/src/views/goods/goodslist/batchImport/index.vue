<template>
  <div class="container">
    <div class="operation-prompt">
      <div class="logo">
        <img src="@/assets/imgs/czts.png" alt="" />
        <span>{{$t('batchImport.drsm')}}</span>
      </div>
      <div class="prompt-content">
        <p>① {{$t('batchImport.gjsyx')}}</p>
        <p>② {{$t('batchImport.txexc')}}</p>
        <p>③ {{$t('batchImport.xyxxz')}}</p>
        <p>
          &nbsp;&nbsp;
          {{$t('batchImport.spfmt')}}
        </p>
        <p>④ {{$t('batchImport.excwj')}}</p>
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

        <el-form-item
          :label="$t('ssgj')"
          prop="country_num">
          <el-select
            class="select-input"
            filterable
            v-model="ruleForm.country_num"
            :placeholder="$t('qxzssgj')"
          >
            <el-option
              v-for="item in countriesList"
              :key="item.id"
              :label="item.zh_name"
              :value="item.num3"
            >
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item :label="$t('yz')" prop="lang_code">
          <el-select
            class="select-input"
            v-model="ruleForm.lang_code"
            @change="handleLangChange"
            :placeholder="$t('qxzyz')"
          >
            <el-option
              v-for="item in languages"
              :key="item.lang_code"
              :label="item.lang_name"
              :value="item.lang_code"
            >
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item :label="$t('batchImport.spfl')" class="goods-class" prop="goodsClass">
          <el-cascader
            v-model="ruleForm.goodsClass"
            class="select-input"
            ref="myCascader"
            :placeholder="$t('batchImport.qxzspf')"
            :options="classList"
            :props="{ checkStrictly: true }"
            @change="changeProvinceCity"
            clearable
          >
          </el-cascader>
        </el-form-item>
        <el-form-item class="goods-brand" :label="$t('batchImport.sppp')" prop="goodsBrand">
          <el-select
            class="select-input"
            v-model="ruleForm.goodsBrand"
            :placeholder="$t('batchImport.qxzspp')"
          >
            <el-option
              v-for="item in brandList"
              :key="item.brand_id"
              :label="item.brand_name"
              :value="item.brand_id"
            >
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item class="freight-set" :label="$t('batchImport.yf')" prop="freight">
          <el-select
            class="select-input"
            v-model="ruleForm.freight"
            :placeholder="$t('batchImport.qxzyf')"
          >
            <el-option
              v-for="item in freightList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            >
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item class="Select" :label="$t('batchImport.scmb')">
          <el-upload
            :disabled="tag"
            class="upload-demo"
            drag
            accept=".xlsx,.xls,.csv"
            action=""
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
          <span class="uploads"><a href="./product_Import.xlsx">{{$t('batchImport.xzpld')}}</a></span>
          <!-- <span class="uploads"><a href="./product_Import.csv">{{$t('batchImport.xzpld')}}</a></span> -->

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
import { batchDelivery, choiceClass } from "@/api/goods/goodsList";
import { getFreightInfo } from "@/api/goods/freightManagement";
import { getStorage } from "@/utils/storage";
import Config from "@/packages/apis/Config";
let actionUrl = Config.baseUrl;
export default {
  name: "batchImport",

  data() {
    return {
      actionUrl,
      countriesList: [],
      //是否编辑编辑的时候国家和语种禁止选择
      edit_disabled:false,
      languages: [],
      ruleForm: {
        template: "",
        goodsClass: "",
        goodsBrand: "",
        lang_code: "",
        freight: "",
        country_num: "",
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
        lang_code: [
          {
            required: true,
            message: this.$t("releasephysical.qxzyz"),
            trigger: "change",
          },
        ],
        country_num: [
          {
            required: true,
            message: this.$t("releasephysical.qxzssgj"),
            trigger: "change",
          },
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
    // this.getBase();

    const initLang = this.LaiKeCommon.getUserLangVal();
    this.ruleForm.lang_code = initLang;
    this.getCountrys();
    this.getLanguage();

    // 使用统一方法加载依赖语言的数据
    this.loadLanguageDependentData(initLang).then(() => {
      // 如果有预填分类，再处理
      if (this.$route.params.inputInfo?.goodsClass?.length > 1) {
        this.classIds = this.$route.params.inputInfo.goodsClass;
        this.allClass(this.classList);
      }
    });

  },
  methods: {

    handleLangChange(newLang) {
      // 清空依赖于语言的字段
      this.ruleForm.goodsClass = '';
      this.ruleForm.goodsBrand = '';
      this.ruleForm.freight = '';

      // 重置相关列表
      this.classList = [];
      this.brandList = [];
      this.freightList = [];

      // 重新加载数据
      this.loadLanguageDependentData(newLang);
    },

    async loadLanguageDependentData(lang) {

      // 1. 加载商品分类（一级）
      await this.choiceClasss(lang);

      // 2. 加载运费模板
      await this.getFreightInfos(lang);
      // 注意：品牌是根据分类动态加载的，所以这里不清空 brandList 也没关系，
      // 但用户需要重新选择分类才能看到品牌

    },


    async getCountrys() {
      try {
        const result = await this.LaiKeCommon.getCountries();
        this.countriesList =  result.data.data;
      } catch (error) {
        console.error('获取国家列表失败:', error);
      }
    },

    async getLanguage() {
      try {
        const result = await this.LaiKeCommon.getLanguages();
        this.languages = result.data.data;
      } catch (error) {
        console.error('获取语种列表失败:', error);
      }
    },

    frontDownload() {
      var a = document.createElement("a"); //创建一个<a></a>标签
      a.href = "./product_Import.xlsx"; // 给a标签的href属性值加上地址，注意，这里是绝对路径，不用加 点.
      a.download = "下载啦.xlsx"; //设置下载文件文件名，这里加上.xlsx指定文件类型，pdf文件就指定.fpd即可
      a.style.display = "none"; // 障眼法藏起来a标签
      document.body.appendChild(a); // 将a标签追加到文档对象中
      a.click(); // 模拟点击了a标签，会触发a标签的href的读取，浏览器就会自动下载了
      a.remove(); // 一次性的，用完就删除a标签
    },
    // 获取商品类别
    async choiceClasss(lang = this.ruleForm.lang_code) {
      const res = await choiceClass({
        api: "admin.goods.choiceClass",
        lang_code: lang
      });
      res.data.data.list.class_list[0].forEach((item, index) => {
        let obj = item;
        this.classList.push({
          value: obj.cid,
          label: obj.pname,
          index: index,
          level: obj.level,
          children: [],
        });
      });
    },

    // 根据商品类别id获取商品品牌
    changeProvinceCity(value) {
      this.ruleForm.goodsBrand = "";
      this.brandList = []
      choiceClass({
        api: "admin.goods.choiceClass",
        lang_code:this.ruleForm.lang_code,
        classId: value.length > 1 ? value[value.length - 1] : value[0],
      }).then((res) => {
        let num = this.$refs.myCascader.getCheckedNodes()[0].data.index;
        this.brandList = res.data.data.list.brand_list;
        if (res.data.data.list.class_list[0].length !== 0) {
          this.$refs.myCascader.getCheckedNodes()[0].data.children = [];
          res.data.data.list.class_list[0].forEach((item, index) => {
            let obj = item;
            this.$refs.myCascader.getCheckedNodes()[0].data.children.push({
              value: obj.cid,
              label: obj.pname,
              index: index,
              level: obj.level,
              children: [],
            });
          });
        }
      });
    },

    // 加载所有分类
    async allClass(value) {
      for (let i = 0; i < value.length - 1; i++) {
        if (this.classIds.includes(value[i].value)) {
          choiceClass({
            api: "admin.goods.choiceClass",
            classId: value[i].value,
          }).then((res) => {
            if (res.data.data.list.class_list.length !== 0) {
              this.brandList = res.data.data.list.brand_list;
              res.data.data.list.class_list[0].forEach((item, index) => {
                let obj = item;
                value[i].children.push({
                  value: obj.cid,
                  label: obj.pname,
                  index: index,
                  children: [],
                });
              });

              this.allClass(value[i].children);
            }
          });
        } else {
          continue;
        }
      }
    },

    // 获取运费列表
    async getFreightInfos(lang = this.ruleForm.lang_code) {
      const res = await getFreightInfo({
        api: "admin.goods.getFreightInfo",
        pageSize: 999,
        lang_code:lang,
        mchId: this.mchid
          ? this.mchid
          : getStorage("laike_admin_userInfo").mchId,
      });
      this.freightList = res.data.data.list;
      //   if(!this.ruleForm.freight) {
      //     this.ruleForm.freight = this.freightList[0].id
      //   }
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
                api: "admin.goods.uploadAddGoods",
                productClassId:
                  this.ruleForm.goodsClass[this.ruleForm.goodsClass.length - 1],
                brandId: this.ruleForm.goodsBrand,
                country_num: this.ruleForm.country_num,
                lang_code: this.ruleForm.lang_code,
                freightId: this.ruleForm.freight,
              };
              let formData = new FormData();
              for (let [key, value] of entries(data)) {
                formData.append(key, value);
              }

              formData.append("image", this.file);
              batchDelivery(formData).then((res) => {
                if (res.data.data) {
                  this.$message({
                    type: "success",
                    message: this.$t('batchImport.pldrsm'),
                    offset: 100,
                  });
                  // this.$router.push("/goods/bulkDeliveryRecord");
                   setTimeout(()=>{
                    this.$router.push("/goods/goodslist/bulkDeliveryRecord");
                    this.$store.commit("loading/SET_LOADING");
                  },1500)

                } else {
                  // this.$message({
                  //     type: 'error',
                  //     message: '发货失败，请前往批量发货记录查看原因',
                  //     offset: 100
                  // })
                  // this.$router.push("/goods/bulkDeliveryRecord");
                   setTimeout(()=>{
                    this.$router.push("/goods/goodslist/bulkDeliveryRecord");

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
    height: 224px;
    background: #e9f4ff;
    border: 1px dashed #2890ff;
    border-radius: 4px;
    padding: 17px 0 17px 17px;
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
