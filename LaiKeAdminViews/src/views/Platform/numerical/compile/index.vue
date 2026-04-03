<template>
  <div class="container">
    <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm">
      <el-form-item :label="$t('ssgj')" prop="country_num">
        <el-select disabled class="select-input" filterable v-model="ruleForm.country_num" :placeholder="$t('qxzssgj')">
          <el-option v-for="(item,index) in countriesList" :key="index" :label="item.zh_name" :value="item.num3">
            <div @click="getIds(item.num3)">{{ item.zh_name }}</div>
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item :label="$t('yz')" prop="lang_code">
        <el-select disabled class="select-input" filterable v-model="ruleForm.lang_code" :placeholder="$t('qxzyz')">
          <el-option v-for="(item,index) in languages" :key="index" :label="item.lang_name" :value="item.lang_code">
            <div @click="getlangCode(item.lang_code)">{{ item.lang_name }}</div>
          </el-option>
        </el-select>
      </el-form-item>
        <el-form-item class="Select code-input" :label="$t('Platform.adddictionary.sjmc')" prop="name">
          <el-select :disabled="true" class="select-input" v-model="ruleForm.name">
            <el-option label="name" value="name">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item class="code-input" :label="$t('Platform.adddictionary.sjbm')" prop="numberCode">
          <el-input :disabled="true" v-model="ruleForm.numberCode"></el-input>
        </el-form-item>
        <el-form-item class="code-input" label="code" prop="code">
          <el-input :disabled="true" v-model="ruleForm.code"></el-input>
        </el-form-item>
        <el-form-item :label="$t('Platform.adddictionary.zhi')" prop="value">
          <el-input v-model="ruleForm.value"></el-input>
        </el-form-item>
        <el-form-item :label="$t('Platform.adddictionary.sfsx')" prop="resource">
          <el-radio-group v-model="ruleForm.resource">
            <el-radio v-model="ruleForm.resource" label="1">{{$t('Platform.adddictionary.yes')}}</el-radio>
            <el-radio v-model="ruleForm.resource" label="0">{{$t('Platform.adddictionary.no')}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <div class="form-footer">
          <el-form-item>
            <el-button class="bgColor" type="primary" @click="submitForm('ruleForm')">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
            <el-button class="bdColor" @click="$router.go(-1)" plain>{{ $t('DemoPage.tableFromPage.cancel') }}</el-button>
          </el-form-item>
        </div>
    </el-form>
  </div>
</template>

<script>
import { addDictionaryTable } from '@/api/Platform/numerical'

export default {
  name: 'compile',

  data() {
    return {
      countriesList: [],
      languages: [],
      lang_code: null,
      country_num: null,
      ruleForm: {
        name: '',
        numberCode: '',
        lang_code: '',
        country_num: '',
        code: '',
        value: '',
        resource: '1',
      },
      id: null,
      radio: '1',
      rules: {
        // name: [{ required: true, trigger: 'blur' }],
        // numberCode: [{ required: true, trigger: 'blur' }],
        // code: [{ required: true, trigger: 'blur' }],
        value: [{ required: true,message: this.$t("terminalConfig.qsrz"), trigger: 'blur' }],
        // resource: [{ required: true, trigger: 'change' }],
      }
    }
  },

  created() {
    console.log(this.$route.params);
    this.ruleForm.name= this.$route.params.name,
    this.ruleForm.numberCode= this.$route.params.code,
    this.ruleForm.code= this.$route.params.value,
    this.ruleForm.value= this.$route.params.text,
    this.ruleForm.resource= `${this.$route.params.status}`
    this.id = this.$route.params.sid

    this.getCountrys().then(() => {
      if(this.$route.params.country_num) {
        this.ruleForm.country_num = parseInt(this.$route.params.country_num)
      }
    })

    this.getLanguages().then(() => {
      if(this.$route.params.lang_code) {
        this.ruleForm.lang_code = this.$route.params.lang_code
      }
    })

  },

  beforeRouteLeave (to, from, next) {
    if (to.name == 'dictionarylist') {
      to.params.dictionaryNum = this.$route.params.dictionaryNum || this.$route.query.dictionaryNum
      to.params.pageSize = this.$route.params.pageSize || this.$route.query.pageSize
      to.params.searchInfo = this.$route.params.searchInfo
    }
    next();
  },


  methods: {
    // 获取国家列表
    async getCountrys() {
      const res = await this.LaiKeCommon.getCountries()
      this.countriesList = res.data.data
    },


    // 获取语言列表
    async getLanguages() {
      const res = await this.LaiKeCommon.getLanguages();
      this.languages = res.data.data
    },

    // 添加/修改字典表明细
    submitForm(formName) {
      this.$refs[formName].validate(async (valid) => {
        console.log(this.ruleForm);
        if (valid) {
          try {
            addDictionaryTable({
              api: 'saas.dic.addDictionaryDetailInfo',
              id: this.$route.params.id,
              dataCode: this.ruleForm.numberCode,
              valueCode: this.ruleForm.value,
              valueName: this.ruleForm.code,
              lang_code: this.ruleForm.lang_code,
              country_num: this.ruleForm.country_num,
              isOpen: parseInt(this.ruleForm.resource),
              sid: this.id,
            }).then(res => {
              if(res.data.code == '200') {
                this.$message({
                  message: this.$t('zdata.bjcg'),
                  type: 'success',
                  offset: 102
                })
                this.$router.go(-1)
              }
            })
          } catch (error) {
            this.$message({
                message: error.message,
                type: 'error',
                showClose: true,
                offset: 102
            })
          }
        } else {
            console.log('error submit!!');
            return false;
        }
      });
    },

    back() {},
  }
}
</script>

<style scoped lang="less">
.container {
  width: 100%;
  height: 737px;
  background-color: #fff;
  padding-top: 40px;
  /deep/.el-form {
    width: 692px;
    margin: 0 auto;
    .Select {
      .select-input {
        width: 580px;
        .el-input {
          width: 580px;
          .el-input__inner {
            width: 580px;
          }
        }
      }
      button {
        width: 96px;
        height: 38px;
        border: 1px solid #2890FF;
        border-radius: 4px;
        background-color: #fff;
        color: #2890FF;
        margin-left: 10px;
      }

    }
    .el-form-item__content {
      .el-input {
        width: 580px;
        .el-input__inner {
          width: 580px;
        }
      }
    }


    .code-input {
      input {
        background-color: #F4F7F9;
        border: 1px solid #D5DBE8;
      }
    }

  }

}
</style>
