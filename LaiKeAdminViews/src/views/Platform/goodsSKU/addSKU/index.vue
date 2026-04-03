<template>
  <div class="container">
      <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="auto" class="demo-ruleForm">
        <el-form-item class="Select" :label="$t('goodsSKU.addSKU.sxmc')" prop="region">
          <el-select :class="$route.query.name?'select-input2':'select-input'" :disabled="$route.query.name" filterable v-model="ruleForm.region" :placeholder="$t('goodsSKU.addSKU.qxzsxmc')">
            <el-option v-for="item in Dictionary" :key="item.id" :label="item.name" :value="item.name">
              <div @click="getId(item)">{{ item.name }}</div>
            </el-option>
          </el-select>
          <el-button v-if="!$route.query.name" type="primary" @click="dialogShow">{{$t('goodsSKU.addSKU.tjmc')}}</el-button>
        </el-form-item>
        <el-form-item :label="$t('ssgj')" prop="country_num" class="in18_class">
          <el-select disabled   filterable v-model="ruleForm.country_num" :placeholder="$t('qxzssgj')">
            <el-option v-for="(item,index) in countriesList" :key="index" :label="item.zh_name" :value="item.num3">
              <div @click="getIds(item.num3)">{{ item.zh_name }}</div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('yz')" prop="lang_code"  class="in18_class" >
          <el-select disabled  filterable v-model="ruleForm.lang_code" :placeholder="$t('qxzyz')">
            <el-option v-for="(item,index) in languages" :key="index" :label="item.lang_name" :value="item.lang_code">
              <div @click="getlangCode(item.lang_code)">{{ item.lang_name }}</div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item class="attribute-values" :label="$t('goodsSKU.addSKU.sxz')" prop="attributeValues">
          <div class="add-info" v-for="(item,index) in attributeList" :key="index">
            <el-input v-model="attributeList[index].value" :placeholder="$t('goodsSKU.addSKU.qsrsxz')"></el-input>
            <div class="add-reduction">
              <i class="el-icon-remove-outline" @click="minus(index)" v-if="attributeList.length !== 1"></i>
              <i class="el-icon-circle-plus-outline" @click="addOne" v-show="index === attributeList.length - 1"></i>
              <span v-if="index == 0">{{$t('goodsSKU.addSKU.cdxz')}}</span>
            </div>
          </div>
        </el-form-item>
        <div class="form-footer2">
            <el-form-item>
              <el-button class="bgColor" type="primary" @click="submitForm('ruleForm')">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
              <el-button style="margin-left: 14px;" class="bdColor" @click="$router.go(-1)" plain>{{ $t('DemoPage.tableFromPage.cancel') }}</el-button>
            </el-form-item>
        </div>
        <div style="height: 40px;width: 100%;"></div>
      </el-form>

      <div class="dialog-block">
        <!-- 弹框组件 -->
        <el-dialog
          :title="$t('goodsSKU.addSKU.tjsxmc')"
          :visible.sync="dialogVisible"
          :before-close="handleClose"
        >
          <el-form :model="ruleForm2" :rules="rules2" ref="ruleForm2" label-width="100px" class="demo-ruleForm">
            <el-form-item :label="$t('ssgj')" prop="country_num" class="pop_in18_class" >
              <el-select class="select-input" filterable v-model="ruleForm2.country_num" :placeholder="$t('qxzssgj')">
                <el-option v-for="(item,index) in countriesList" :key="index" :label="item.zh_name" :value="item.num3">
                  <div @click="getIds(item.num3)">{{ item.zh_name }}</div>
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('yz')" prop="lang_code" class="pop_in18_class" >
              <el-select class="select-input" filterable v-model="ruleForm2.lang_code" :placeholder="$t('qxzyz')">
                <el-option v-for="(item,index) in languages" :key="index" :label="item.lang_name" :value="item.lang_code">
                  <div @click="getlangCode(item.lang_code)">{{ item.lang_name }}</div>
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('goodsSKU.addSKU.sjmc')" prop="dataName">
              <el-input v-model="ruleForm2.dataName"></el-input>
            </el-form-item>
            <el-form-item :label="$t('goodsSKU.addSKU.sfsx')" prop="status">
              <el-radio-group v-model="ruleForm2.status">
                <el-radio v-model="radio" label="1">{{$t('goodsSKU.addSKU.yes')}}</el-radio>
                <el-radio v-model="radio" label="0">{{$t('goodsSKU.addSKU.no')}}</el-radio>
              </el-radio-group>
            </el-form-item>
            <div class="form-footer">
              <el-form-item>
                <el-button class="bdColor" plain @click="dialogVisible = false">{{$t('goodsSKU.addSKU.ccel')}}</el-button>
                <el-button class="sub_bt" type="primary" @click="determine('ruleForm2')">{{$t('goodsSKU.addSKU.okk')}}</el-button>
              </el-form-item>
            </div>
          </el-form>
        </el-dialog>
      </div>
  </div>
</template>

<script>
import { getSkuAttributeList, addSkuName, addSku, getSkuInfo } from '@/api/Platform/goodsSku'
export default {
  name: 'addSKU',

  data() {
    return {
      countriesList: [],
      languages: [],
      lang_code: null,
      country_num: null,
      ruleForm: {
        region: '',
        lang_code: '',
        country_num: '',
      },
      sid: '',
      rules: {
        region: [
          { required: true, message: this.$t('goodsSKU.addSKU.qxzsxmc'), trigger: 'change' }
        ]
      },
      Dictionary: [],
      attributeList: [],
      attribute: '',

      // 弹框数据
      dialogVisible: false,
      ruleForm2: {
        dataName: '',
        lang_code: '',
        country_num: '',
        status: '1',
      },
      rules2: {
        dataName: [
          { required: true, message: this.$t('goodsSKU.addSKU.qtxsxmc'), trigger: 'change' }
        ],
        statues: [
          { required: true, message: this.$t('goodsSKU.addSKU.yes'), trigger: 'change' }
        ],
        country_num: [
          { required: true, message: this.$t('qxzssgj') , trigger: 'blur' }
        ],
        lang_code: [
          { required: true, message: this.$t('qxzyz') , trigger: 'blur' }
        ],
      }
    }
  },

  created() {
    this.getSkuAttributeLists()
    if(this.$route.query.name) {
      this.getSkuInfos().then(() => {
        this.attributeList.push({
          value: ''
        })
      })
      this.$router.currentRoute.matched[2].meta.title = '编辑SKU'
    } else {
      this.$router.currentRoute.matched[2].meta.title = '添加SKU'
    }

console.log("###############")
console.log(this.$route.query)
console.log("###############")

    if(this.attributeList.length == 0) {
      this.attributeList.push({
        value: ''
      })
    }

    this.getCountrys().then(() => {
      if(this.$route.query.country_num) {
        this.ruleForm.country_num = parseInt(this.$route.query.country_num)
      }
    })

    this.getLanguages().then(() => {
      if(this.$route.query.lang_code) {
        this.ruleForm.lang_code = this.$route.query.lang_code
      }
    })

  },

  watch: {
    'attributeList': {
      handler:function() {
        if(this.attributeList.length !== 0) {
          this.attribute = this.attributeList.map(item => {
            if(item.value !== '') {
              return item.value
            }
          }).join(',')
        }
      },
      deep: true
    }
  },

  beforeRouteLeave (to, from, next) {
    if (to.name == 'goodsSKUlist' && this.$route.query.name == 'editor') {
      to.params.dictionaryNum = this.$route.query.dictionaryNum
      to.params.pageSize = this.$route.query.pageSize
    }
    next();
  },

  methods: {

    // 获取国家列表
    async getCountrys() {
      const res = await this.LaiKeCommon.getCountries()
      this.countriesList = res.data.data
    },

    getIds(value) {
      this.ruleForm2.country_num = value
    },

    // 获取语言列表
    async getLanguages() {
      const res = await this.LaiKeCommon.getLanguages();
      this.languages = res.data.data
    },

    getlangCode(value) {
      this.ruleForm2.lang_code = value
    },

    async getSkuAttributeLists() {
      const res = await getSkuAttributeList({
        api: 'saas.dic.getSkuAttributeList',
        pageSize: 999
      })
      this.Dictionary = res.data.data.list
      console.log(res);
    },

    async getSkuInfos() {
      const res = await getSkuInfo({
        api: 'saas.dic.getSkuInfo',
        sid: this.$route.query.id,
        pageSize: 999
      })
      console.log(res);
      this.ruleForm.region = this.$route.query.sname
      var attrList = res.data.data.list.map(item => {
        return {
          value: item.name
        }
      })
      console.log(attrList);
      this.attributeList = attrList

    },

    getId(val) {
      this.ruleForm.lang_code = val.lang_code
      this.ruleForm.country_num = val.country_num
      this.sid = val.id
    },

    addOne() {
      this.attributeList.push(
        {
          value: ''
        }
      )
    },

    minus(index) {
      if(this.attributeList.length !== 0) {
        this.attributeList.splice(index, 1)
      }
    },

    // 弹框方法
    dialogShow() {
      this.ruleForm2.dataName= '',
      this.ruleForm2.lang_code= '',
      this.ruleForm2.country_num= '',
      this.ruleForm2.status= '1',
      this.dialogVisible = true
    },

    handleClose(done) {
      this.dialogVisible = false
    },

    radio() {

    },

    // 添加SKU
    submitForm(formName) {
      this.$refs[formName].validate(async (valid) => {
        if (valid) { 
          if (this.attributeList.some(item => item.value === '')) {
             this.errorMsg( this.$t('goodsSKU.addSKU.qsrsxz')
               );
             return;
          }

          try {
            addSku({
              api: 'saas.dic.addSku',
              sid: this.$route.query.id ? this.$route.query.id : this.sid,
              attributeList: this.attribute ? this.attribute : null,
              country_num: this.ruleForm.country_num,
              lang_code: this.ruleForm.lang_code,
              type: this.$route.query.id ? 1 : 0
            }).then(res => {
              console.log(res);
              if(res.data.code == '200') {
                if(this.$route.query.name){
                  this.$message({
                  message: this.$t('zdata.bjcg'),
                  type: 'success',
                  offset:102,
                })
                }else{
                  this.$message({
                  message: this.$t('zdata.tjcg'),
                  type: 'success',
                  offset:102,
                })
                }
                this.$router.go(-1)
              }

            })
          } catch ({message}) {
            console.log(message);
            this.$message({
              message: message.message,
              type: 'error',
              showClose: true,
              offset:102,
            })
          }
        } else {
            console.log('error submit!!');
            return false;
        }
      });
    },

    // 添加商品名称
    determine(formName2) {
      this.$refs[formName2].validate(async (valid) => {
        if (valid) {
          try {
            if(this.ruleForm2.dataName.length > 50) {
              this.$message({
                message: this.$t('goodsSKU.addSKU.sxmccd'),
                type: 'error',
                offset: 102
              })
              return
            }
            addSkuName({
              api: 'saas.dic.addSkuName',
              skuName: this.ruleForm2.dataName,
              lang_code: this.ruleForm2.lang_code,
              country_num: this.ruleForm2.country_num,
              isOpen: parseInt(this.ruleForm2.status),
            }).then(res => {
              if(res.data.code == '200') {
                console.log(res);
                this.getSkuAttributeLists()
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
              message: this.$t('goodsSKU.addSKU.sjmcwk'),
              type: 'error',
              offset: 102
            })
          }
        } else {
          this.$message({
            message: this.$t('goodsSKU.addSKU.sjmcwk'),
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
.sub_bt{
  margin-left: 14px;
}
.container {
  width: 100%;
  background-color: #fff;
  display: flex;
  justify-content: center;
  padding: 40px 0 0 0;
  border-radius: 4px;
  overflow: auto;
  /deep/.el-form {
    .el-form-item__label {
      font-weight: normal;
    }
    .el-form-item__content {
        //display: flex;
        // .el-input {
        //   width: 580px;
        //   height: 40px;
        //   input {
        //     width: 580px;
        //     height: 40px;
        //   }
        // }
      }
    .Select {
      .select-input {
        width: 580px;
      }
      .select-input2{
        width: 580px;
      }
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

    .in18_class{
      .el-select {
        width: 580px;
      }
    }


    .attribute-values {
      .el-form-item__content {
        display: flex;
        flex-direction: column;
        .add-info {
          display: flex;
          &:not(:last-child) {
            margin-bottom: 20px;
          }
          .el-input {
            width: 580px;
            height: 40px;
            input {
              width: 580px;
              height: 40px;
            }
          }

          .add-reduction {
            display: flex;
            align-items: center;
            span {
              margin-left: 10px;
              color: #97a0b4;
            }
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
  }
  .form-footer2{
    .el-form-item{
      margin-bottom: 0 !important;
    }
  }
  .dialog-block {

    .pop_in18_class{
      .el-select {
        width: 359px;
      }
    }
    // 弹框样式
    /deep/.el-dialog {
      width: 580px;
      height: 430px;
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
          top: 0;
        }
        .el-dialog__title {
          font-weight: normal;
        }

      }

      .el-dialog__body {
        padding: 41px 60px 14px 60px !important;
        border-bottom: 1px solid #E9ECEF;
        /deep/.form-footer {
          width: 100%;
          height: 72px;
          position: absolute;
          bottom: 0;
          right: 0;
          display: flex;
          justify-content: flex-end;
          padding-right: 20px;
          .el-form-item {
            padding: 0 !important;
            .el-form-item__content {
              height: 100%;
              line-height: 72px;
              margin: 0 !important;

            }
          }
        }
      }
    }
  }

}
</style>
