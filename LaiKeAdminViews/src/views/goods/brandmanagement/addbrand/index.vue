<template>
  <div class="container">
    <div class="add-menu">
      <el-form :model="ruleForm" :rules="rules" ref="ruleForm"  class="picture-ruleForm" label-width="90px">

        <el-form-item :label="$t('ssgj')" prop="countries">
          <el-select class="select-input" filterable v-model="ruleForm.countries" :placeholder="$t('qxzssgj')">
            <el-option v-for="(item,index) in countriesList" :key="index" :label="item.zh_name" :value="item.num3">
              <div @click="getIds(item.num3)">{{ item.zh_name }}</div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('yz')" prop="lang_code">
          <el-select class="select-input" filterable v-model="ruleForm.lang_code" :placeholder="$t('qxzyz')">
            <el-option v-for="(item,index) in languages" :key="index" :label="item.lang_name" :value="item.lang_code">
              <div @click="getlangCode(item.lang_code)">{{ item.lang_name }}</div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item :label="$t('brandmanagement.addbrand.ppmc')" prop="brandname">
          <el-input v-model="ruleForm.brandname" :placeholder="$t('brandmanagement.addbrand.qsrppmc')"></el-input>
        </el-form-item>
        <el-form-item class="upload-img" :label="$t('brandmanagement.addbrand.pplg')" prop="brandLogo">
            <l-upload
              :limit="1"
              v-model="ruleForm.brandLogo"
              :text="$t('brandmanagement.addbrand.jy120')"
            >
            </l-upload>
        </el-form-item>
        <el-form-item :label="$t('brandmanagement.addbrand.ssfl')" prop="brandtype">
          <el-select class="select-input" multiple filterable v-model="ruleForm.brandtype" :placeholder="$t('brandmanagement.addbrand.qxzssfl')">
            <el-option
              v-for="item in brandTypeList"
              :key="item.cid"
              :label="item.pname"
              :value="item.cid">
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item class="textarea" :label="$t('brandmanagement.addbrand.bz')" prop="note">
          <el-input type="textarea" v-model="ruleForm.note" :placeholder="$t('brandmanagement.addbrand.qtxbz')"></el-input>
        </el-form-item>
        <div class="form-footer">
          <el-form-item>
            <el-button class="bgColor" type="primary" @click="submitForm('ruleForm')">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
            <el-button class="bdColor left_kep" @click="$router.go(-1)" plain>{{ $t('DemoPage.tableFromPage.cancel') }}</el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import { addBrand, getClassInfo, getCountry } from '@/api/goods/brandManagement'
export default {
  name: 'addbrand',

  data() {
    return {
      ruleForm: {
        brandname: '',
        brandLogo: '',
        brandtype: '',
        countries: '',
        lang_code: '',
        note: '',
      },
      rules: {
        lang_code: [
          { required: true, message: this.$t('qxzyz') , trigger: 'blur' }
        ],
        brandtype: [
          { required: true, message: this.$t('brandmanagement.addbrand.qxzssfl') , trigger: 'blur' }
        ],
        countries: [
          { required: true, message: this.$t('brandmanagement.addbrand.qxzssgj') , trigger: 'change' }
        ],
        brandname: [
          { required: true, message: this.$t('brandmanagement.addbrand.qsrppmc') , trigger: 'blur' }
        ],
        brandLogo: [
          { required: true, message: this.$t('brandmanagement.addbrand.qscpplg') , trigger: 'change' }
        ]
      },

      brandTypeList: [],

      countriesList: [],
      languages: [],
      lang_code: null,

      id: null,
      countriesId: null
    }
  },

  created() {
    this.ruleForm.lang_code = this.LaiKeCommon.getUserLangVal()
    this.getClassInfos(this.ruleForm.lang_code)
    this.getCountrys()
    this.getLanguages();
  },

  watch: {
    'ruleForm.brandLogo'() {
      if(this.ruleForm.brandLogo) {
        this.$refs.ruleForm.clearValidate('brandLogo')
      }
    },
    'ruleForm.lang_code'(val,old) {
      if(val) {
          //联动所属分类 只查询对应的语种分类
        this.getClassInfos(val)
      }
    },
  },

  methods: {
    // 获取所属分类
    async getClassInfos(language) {

        if(!language){
           language = this.LaiKeCommon.getUserLangVal();
        }

        const res = await getClassInfo({
            api: 'admin.goods.getClassInfo',
            pageNo: 1,
            lang_code:language,
            pageSize: 999,
        })

        console.log(res);
        this.brandTypeList = res.data.data.classInfo
    },

    // 获取国家列表
    async getCountrys() {
        const res = await this.LaiKeCommon.getCountries()
        this.countriesList = res.data.data
    },

    getIds(value) {
      this.countriesId = value
    },

    // 获取语言列表
    async getLanguages() {
        const res = await this.LaiKeCommon.getLanguages();
        this.languages = res.data.data
    },

    getlangCode(value) {
      this.lang_code = value
    },

    submitForm(formName) {
      this.$refs[formName].validate(async (valid) => {
        console.log(this.ruleForm);
        if (valid) {
        try {
          if(this.ruleForm.brandname.length > 15) {
            this.$message({
              message: '品牌名称长度不能大于15个字符',
              type: 'error',
              offset:102
            })
            return
          }
          if(this.ruleForm.note.length > 50) {
            this.$message({
              message: '备注长度不能大于50个字符',
              type: 'error',
              offset:102
            })
            return
          }
          addBrand({
              api: 'admin.goods.addBrand',
              brandName: this.ruleForm.brandname,
              brandLogo: this.ruleForm.brandLogo,
              brandClass: this.ruleForm.brandtype.join(','),
              country_num: this.countriesId,
              lang_code: this.ruleForm.lang_code,
              remarks: this.ruleForm.note
          }).then(res => {
            if(res.data.code == '200') {
              console.log(res);
              this.$message({
                  message: this.$t('zdata.xzcg'),
                  type: 'success',
                  offset: 102
              })
              // this.$router.go(-1)
              this.$router.push("/goods/brandmanagement/brandlist");
            }
          })
        } catch (error) {
          this.$message({
              message: error.message,
              type: 'error',
              showClose: true
          })
        }
        } else {
          console.log('error submit!!');
          return false;
        }
      });
    },

  }
}
</script>

<style scoped lang="less">
/deep/ .el-tag__close.el-icon-close {
    background-color: #97a0b4 !important;
    color: #fff !important;
}
/deep/ .el-tag.el-tag--info .el-tag__close:hover {
    color: #fff !important;
    background-color: #b2bcd1 !important;
}
.container {
  width: 100%;
  height: 737px;
  background-color: #fff;
  padding: 40px 0 0 0;
  color: #414658;
  border-radius: 4px;
  /deep/.add-menu {
    display: flex;
    justify-content: center;
    .el-form-item {
      display: flex;
      &:not(:last-child) {
        .el-form-item__content {
          margin-left: 0px !important;
        }
      }
      .el-form-item__content {
        width: 580px;
      }
      .select-input {
        width: 580px;
        // height: 40px;
      }
      .left_kep{
        margin-left: 14px;
      }
    }

  }

  /deep/.el-form-item__label {
    font-weight: normal;
  }
}
</style>
