<template>
  <div class="container">
    <div class="main-content">
      <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="110px" class="demo-ruleForm">
        <el-form-item :label="$t('ssgj')" prop="country_num">
          <el-select class="select-input" filterable v-model="ruleForm.country_num" :placeholder="$t('qxzssgj')">
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
        <el-form-item class="Select" :label="$t('Platform.adddictionary.sjmc')" prop="region">
          <el-select class="select-input select-num" v-model="ruleForm.region" filterable :placeholder="$t('Platform.adddictionary.qxzsjmc')">
            <el-option v-for="item in Dictionary" :key="item.id" :label="item.name" :value="item.name">
              <div @click="getId(item.id)">{{ item.name }}</div>
            </el-option>
          </el-select>
          <el-button type="primary" @click="dialogShow">{{$t('Platform.adddictionary.tjmc')}}</el-button>
        </el-form-item>
        <el-form-item class="code-input" :label="$t('Platform.adddictionary.sjbm')" prop="numberCode">
          <el-input v-model="ruleForm.numberCode" disabled></el-input>
        </el-form-item>
        <!-- 这个v-if="id == 17"没理解，并且ruleForm.name没有传递给后端，暂且注释 xuxiong  禅道268-->
       <el-form-item class="Select" :label="$t('Platform.adddictionary.sssx')" prop="name" v-if="id == 17">
          <el-select class="select-input" v-model="ruleForm.name" :placeholder="$t('Platform.adddictionary.qxzsssx')">
            <el-option v-for="item in ruleForm.list" :key="item.id" :label="item.ctext" :value="item.id">
              <div @click="getArrId(item.id)">{{ item.ctext }}</div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="code" prop="code">
          <el-input v-model="ruleForm.code" :placeholder="$t('Platform.adddictionary.qsrcode')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('Platform.adddictionary.zhi')" prop="value">
          <el-input v-model="ruleForm.value" :placeholder="$t('Platform.adddictionary.qsrzhi')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('Platform.adddictionary.sfsx')" prop="resource">
          <el-radio-group v-model="ruleForm.resource">
            <el-radio v-for="item in isRegisteredLists" :label="item.value" :key="item.value">{{item.name}}</el-radio>
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

    <div class="dialog-block">
      <!-- 弹框组件 -->
      <el-dialog
        :title="$t('Platform.adddictionary.tjsjmc')"
        :visible.sync="dialogVisible"
        :before-close="handleClose"
        :modal-append-to-body='true'
      >
        <el-form :model="ruleForm2" :rules="rules2" ref="ruleForm2" label-width="100px" class="demo-ruleForm">
          <el-form-item :label="$t('Platform.adddictionary.sjmc')" prop="dataName">
            <el-input v-model="ruleForm2.dataName"></el-input>
          </el-form-item>
          <el-form-item :label="$t('Platform.adddictionary.sfsx')" class="isSwich" prop="status" style="display: flex;">
            <el-radio-group v-model="ruleForm2.status">
              <el-radio v-for="item in isRegisteredList" :label="item.value" :key="item.value">{{item.name}}</el-radio>
            </el-radio-group>
          </el-form-item>
          <div class="form-footer">
            <el-form-item>
              <el-button class="fontColor" @click="dialogVisible = false">{{ $t('DemoPage.tableFromPage.cancel') }}</el-button>
              <el-button class="bgColor" type="primary" @click="determine('ruleForm2')">{{$t('Platform.adddictionary.save')}}</el-button>
            </el-form-item>
          </div>
        </el-form>
      </el-dialog>
    </div>


  </div>
</template>

<script>
import { dictionaryDirectories, directoriesCode, addDataName, addDictionaryTable } from '@/api/Platform/numerical'
export default {
  name: 'adddictionary',

  data() {
    return {
      countriesList: [],
      languages: [],
      lang_code: null,
      country_num: null,
      ruleForm: {
        region: '',
        numberCode: '',
        lang_code: '',
        country_num: '',
        code: '',
        value: '',
        resource: 1,
        list:[],
        name:''
      },
      // radio1: '1',
      rules: {
        country_num: [
          { required: true, message: this.$t('Platform.adddictionary.qxzssgj') , trigger: 'blur' }
        ],
        lang_code: [
          { required: true, message: this.$t('Platform.adddictionary.qxzyz') , trigger: 'blur' }
        ],
        region: [
          { required: true, message: this.$t('Platform.adddictionary.qxzsjmc'), trigger: 'change' }
        ],
        code: [
          { required: true, message: this.$t('Platform.adddictionary.qsrcode'), trigger: 'blur' },
        ],
        value: [
          { required: true, message: this.$t('Platform.adddictionary.qsrzhi'), trigger: 'blur' },
        ],
        resource: [
          { required: true, message: this.$t('Platform.adddictionary.yes'), trigger: 'change' }
        ],
        name: [
          { required: true, message: this.$t('Platform.adddictionary.qxzsssx'), trigger: 'change' }
        ],
      },
      Dictionary: [],
      id: null,
      arrId:null,

      // 弹框数据
      dialogVisible: false,
      ruleForm2: {
        dataName: '',
        status: 1,
      },
      isRegisteredList: [
        {
          value: 0,
          name: this.$t('Platform.adddictionary.no')
        },
        {
          value: 1,
          name: this.$t('Platform.adddictionary.yes')
        }
      ],

      isRegisteredLists: [
        {
          value: 0,
          name: this.$t('Platform.adddictionary.no')
        },
        {
          value: 1,
          name: this.$t('Platform.adddictionary.yes')
        }
      ],
      rules2: {
        dataName: [
          { required: true, message: this.$t('Platform.adddictionary.qsrsjmc'), trigger: 'change' }
        ],
        statues: [
          { required: true, message: this.$t('Platform.adddictionary.yes'), trigger: 'change' }
        ],
      }
    }
  },

  created() {
    this.getDictionaryDirectories()
    this.getCountrys()
    this.getLanguages();
  },

  watch: {
    'ruleForm.region': {
      handler() {
        this.getDirectoriesCode()
        console.log(this.id);
      },
    },
    'ruleForm.name': {
      handler() {
        this.getDirectoriesCode()
        console.log(this.id);
      },
    }
  },

  methods: {

    // 获取国家列表
    async getCountrys() {
      const res = await this.LaiKeCommon.getCountries()
      this.countriesList = res.data.data
    },

    getIds(value) {
      this.country_num = value
    },

    // 获取语言列表
    async getLanguages() {
      const res = await this.LaiKeCommon.getLanguages();
      this.languages = res.data.data
    },

    getlangCode(value) {
      this.lang_code = value
    },

    getId(value) {
      this.id = value
    },
    getArrId(val){
      this.arrId = val
    },

    // 获取菜单目录下拉
    async getDictionaryDirectories() {
      const res = await dictionaryDirectories({
        api: 'saas.dic.getDictionaryCatalogList',
      })
      this.Dictionary = res.data.data.data
      console.log(res);
    },

    // 获取目录编码
    async getDirectoriesCode() {
      const res = await directoriesCode({
        api: 'saas.dic.getDictionaryCode',
        id: this.id
      })
      this.ruleForm.numberCode = res.data.data.code
      this.ruleForm.list = res.data.data.list
      console.log(res);

    },

    // 添加/修改字典表明细
    submitForm(formName) {
      this.$refs[formName].validate(async (valid) => {
        console.log(this.ruleForm);
        if (valid) {
          try {
           /*  if(this.ruleForm.value.length > 30) {
              this.$message({
                message: this.$t('Platform.adddictionary.zmccd'),
                type: 'error',
                offset: 102
              })
              return
            } */
            addDictionaryTable({
              api: 'saas.dic.addDictionaryDetailInfo',
              dataCode: this.ruleForm.numberCode,
              valueCode: this.ruleForm.value,
              valueName: this.ruleForm.code,
              lang_code: this.ruleForm.lang_code,
              country_num: this.ruleForm.country_num,
              isOpen: parseInt(this.ruleForm.resource),
              sid: this.id,
              attrId: this.arrId,
            }).then(res => {
              if(res.data.code == '200') {
                this.$message({
                  message: this.$t('zdata.tjcg'),
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


    // 弹框方法
    dialogShow() {
      this.ruleForm2.dataName= '',
      this.ruleForm2.status= 1,
      this.dialogVisible = true
    },

    handleClose(done) {
      this.dialogVisible = false
    },

    // 添加字典目录
    determine(formName2) {
      this.$refs[formName2].validate(async (valid) => {
        if (valid) {
          try {
            if(this.ruleForm2.dataName.length > 20) {
              this.$message({
                message: this.$t('Platform.adddictionary.sjmccd'),
                type: 'error',
                offset: 102
              })
              return
            }
            addDataName({
              api: 'saas.dic.addDictionaryInfo',
              name: this.ruleForm2.dataName,
              isOpen: parseInt(this.ruleForm2.status),
              token: this.$store.getters.token,
            }).then(res => {
              if(res.data.code == '200') {
                console.log(res);
                this.getDictionaryDirectories()
                this.$message({
                  message: this.$t('zdata.tjcg'),
                  type: 'success',
                  offset: 102
                })
                this.dialogVisible = false
              }
            })
          } catch (error) {
            this.$message({
              message: this.$t('Platform.adddictionary.sjmcwk'),
              type: 'error',
              offset: 102
            })
          }
        } else {
          this.$message({
            message: this.$t('Platform.adddictionary.sjmcwk'),
            type: 'error',
            offset: 102
          })
          // return false;
        }
      });
    }

  }
}
</script>

<style scoped lang="less">
/deep/.app-main .container{
    min-height: 100% !important;
}
.container {
  // width: 100%;
  // height: 737px;
  // background-color: #fff;
  // padding-top: 40px;
  width: 100%;
  background-color: #fff;
  display: flex;
  justify-content: center;
  padding: 40px 0 0 0;
  border-radius: 4px;
  overflow: auto;
  .main-content {
    /deep/.el-form {
      // width: 692px;
      // width: 42%;
      // margin: 0 auto;
      .el-form-item {
        // display: flex;
        &:not(:first-child) {
          display: flex;
        }
        &:not(:last-child) {
          .el-form-item__content {
            margin-left: 0px !important;
            .el-radio-group{
              display: flex;
              align-items: center;
            }
          }
        }
      }
      .el-form-item__label {
        font-weight: normal;
      }

      .code-input {
        input {
          background-color: #F4F7F9;
          border: 1px solid #D5DBE8;
        }
      }

      .el-form-item__content {
        display: flex;
        .el-input {
          width: 580px;
          height: 40px;
          input {
            width: 580px;
            height: 40px;
          }
        }
      }
      .Select {
        .select-num {
          width: 474px;
          .el-input {
            width: 474px;
            input {
              width: 474px;
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

    }
  }

  .dialog-block {
    // 弹框样式
    /deep/.el-dialog {
      width: 580px;
      height: 296px;
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%,-50%);
      margin: 0 !important;
      .el-dialog__header {
        width: 100%;
        height: 58px;
        line-height: 58px;
        font-size: 16px;
        margin-left: 19px;
        font-weight: bold;
        border-bottom: 1px solid #E9ECEF;
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
        padding: 41px 60px 0px 60px !important;
        border-bottom: 1px solid #E9ECEF;
        .el-form-item__label {
          font-weight: normal;
        }
        .isSwich {
          .el-form-item__content {
            margin-left: 0 !important;
          }
        }
        .form-footer {
          width: 174px;
          height: 72px;
          position: absolute;
          bottom: 0;
          right: 0;
          .el-form-item {
            padding: 0 !important;
            height: 100%;
            .el-form-item__content {
              height: 100%;
              line-height: 72px;
              margin: 0 !important;
              .bgColor {
                margin-left: 14px;
              }
            }
          }
        }
      }
    }
  }

}
</style>
