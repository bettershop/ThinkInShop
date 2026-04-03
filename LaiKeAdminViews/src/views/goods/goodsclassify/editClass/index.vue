<template>
  <div class="container">
    <div class="add-menu">
      <el-form :model="ruleForm" :rules="rules" ref="ruleForm"  class="picture-ruleForm" label-width="auto">
        <el-form-item :label="$t('ssgj')" prop="country_num">
          <el-select class="select-input" disabled filterable v-model="ruleForm.country_num" :placeholder="$t('qxzssgj')">
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
        <el-form-item :label="$t('addgoodsclass1.flmc')" prop="classname">
          <el-input v-model="ruleForm.classname" maxlength="15" show-word-limit :placeholder="$t('addgoodsclass1.qsrflmc')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('addgoodsclass1.ywfbt')">
          <el-input v-model="ruleForm.subtitle" maxlength="15" :placeholder="$t('addgoodsclass1.qsrywfbt')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('addgoodsclass1.fldj')" prop="classlevel">
          <el-select class="select-input" disabled v-model="ruleForm.classlevel" :placeholder="$t('addgoodsclass1.qxzfldj')">
            <el-option v-for="(item,index) in menuLevelList" :key="index" :label="item.label" :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item :label="$t('addgoodsclass1.sjfl')" :prop="inputShow == 2?'select_2':inputShow == 3?'select_3':inputShow == 4?'select_4':'select_5'" v-if="inputShow !== 1">
          <div class="superior">
            <el-select v-show="inputShow >= 2" :disabled="$route.query.classlevel > 1 ? true : false" class="select-input" v-model="ruleForm.select_2" :placeholder="$t('addgoodsclass1.qxzyij')">
              <el-option v-for="(item,index) in options1" :key="index" :label="item.pname" :value="item.cid">
              </el-option>
            </el-select>
            <el-select v-show="inputShow >= 3" :disabled="$route.query.classlevel > 1 ? true : false" class="select-input" v-model="ruleForm.select_3" :placeholder="$t('addgoodsclass1.qxzerj')">
              <el-option v-for="(item,index) in options2" :key="index" :label="item.pname" :value="item.cid">
              </el-option>
            </el-select>
            <el-select  v-show="inputShow >= 4" :disabled="$route.query.classlevel > 1 ? true : false" class="select-input" v-model="ruleForm.select_4" :placeholder="$t('addgoodsclass1.qxzsanj')">
              <el-option v-for="(item,index) in options3" :key="index" :label="item.pname" :value="item.cid">
              </el-option>
            </el-select>
            <el-select  v-show="inputShow >= 5" :disabled="$route.query.classlevel > 1 ? true : false" class="select-input" v-model="ruleForm.select_5" :placeholder="$t('addgoodsclass1.qxzsij')">
              <el-option v-for="(item,index) in options4" :key="index" :label="item.pname" :value="item.cid">
              </el-option>
            </el-select>
          </div>
        </el-form-item>
        <el-form-item class="upload-img" :label="$t('addgoodsclass1.fltb')" prop="classLogo">
            <l-upload
              :limit="1"
              v-model="ruleForm.classLogo"
              :text="$t('addgoodsclass1.fltbbz')"
            >
            </l-upload>
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
import { getClassInfo, addClass, getClassLevelTopAllInfo } from '@/api/goods/goodsClass'
export default {
  name: 'editClass',

  data() {
    return {
      countriesList: [],
      languages: [],
      lang_code: null,
      country_num: null,
      ruleForm: {
        classname: '',
        subtitle: '',
        lang_code: '',
        country_num: '',
        classlevel: '',
        classLogo: '',
        superiorclass: '',
        select_2: '',
        select_3: '',
        select_4: '',
        select_5: '',
      },
      inputShow: 1,
      rules: {
        country_num: [
          { required: true, message: this.$t('addgoodsclass1.qxzssgj') , trigger: 'blur' }
        ],
        lang_code: [
          { required: true, message: this.$t('addgoodsclass1.qxzflyz') , trigger: 'blur' }
        ],
        classname: [
          { required: true, message: this.$t('addgoodsclass1.qsrflmc') , trigger: 'blur' }
        ],
        subtitle: [
          { required: true, message: this.$t('addgoodsclass1.qsrywfbt') , trigger: 'blur' }
        ],
        classlevel: [
          { required: true, message: this.$t('addgoodsclass1.qxzfldj') , trigger: 'change' }
        ],
        select_2: [
					{ required: true, message: this.$t('addgoodsclass1.qxzyij') , trigger: 'blur' }
				],
				select_3: [
					{ required: true, message: this.$t('addgoodsclass1.qxzerj') , trigger: 'blur' }
				],
				select_4: [
					{ required: true, message: this.$t('addgoodsclass1.qxzsanj') , trigger: 'blur' }
				],
				select_5: [
					{ required: true, message: this.$t('addgoodsclass1.qxzsij') , trigger: 'blur' }
				],
      },

      menuLevelList: [
        {
          value: 1,
          label: this.$t('addgoodsclass1.yij')
        },
        {
          value: 2,
          label: this.$t('addgoodsclass1.erj')
        },
        {
          value: 3,
          label: this.$t('addgoodsclass1.sanj')
        },
        {
          value: 4,
          label: this.$t('addgoodsclass1.sij')
        },
        {
          value: 5,
          label: this.$t('addgoodsclass1.wuj')
        }
      ],

      options1: [],

      options2: [],

      options3: [],

      options4: [],
    }
  },

  created() {
    this.getClassInfos()
    this.ruleForm.classname = this.$route.params.pname
    this.ruleForm.subtitle = this.$route.params.english_name
    this.ruleForm.classLogo = this.$route.params.img

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

    if(this.$route.query.classlevel == 2) {
      this.ruleForm.classlevel = 2
      this.inputShow = 2
      this.getClassLevelTopAllInfos()
    } else if(this.$route.query.classlevel == 3) {
      this.ruleForm.classlevel = 3
      this.inputShow = 3
      this.getClassLevelTopAllInfos()
    } else if(this.$route.query.classlevel == 4) {
      this.ruleForm.classlevel = 4
      this.inputShow = 4
      this.getClassLevelTopAllInfos()
    } else if(this.$route.query.classlevel == 5) {
      this.ruleForm.classlevel = 5
      this.inputShow = 5
      this.getClassLevelTopAllInfos()
    } else {
      this.ruleForm.classlevel = 1
    }

    // this.getCountrys();
    // this.getLanguages();

  },

  beforeRouteLeave (to, from, next) {
    if (to.name == 'viewClass' || to.name == 'goodsclassifylist') {
      to.params.dictionaryNum = this.$route.query.dictionaryNum
      to.params.pageSize = this.$route.query.pageSize
    }
    next();
  },

  watch: {
    'ruleForm.classlevel'() {
      if(this.ruleForm.classlevel === 2) {
        this.inputShow = 2
      } else if (this.ruleForm.classlevel === 3) {
        this.inputShow = 3
      } else if (this.ruleForm.classlevel === 4) {
        this.inputShow = 4
      } else if (this.ruleForm.classlevel === 5) {
        this.inputShow = 5
      } else {
        this.inputShow = 1
      }
    },

    'ruleForm.select_2'(newVal,oldVal){
      if(oldVal) {
        console.log(newVal)
        this.ruleForm.select_3 = ''
        this.ruleForm.select_4 = ''
        this.ruleForm.select_5 = ''
        this.options2 = []
        this.options3 = []
        this.options4 = []
        if(newVal) {
          getClassInfo({
            api: 'admin.goods.getClassInfo',
            pageSize: 100,
            classId: newVal,
            type: 1
          }).then(res => {
            this.options2 = res.data.data.classInfo
          })
        }
      }
	  },

    'ruleForm.select_3'(newVal,oldVal){
      if(oldVal) {
        console.log(newVal);
        this.ruleForm.select_4 = ''
        this.ruleForm.select_5 = ''
        this.options3 = []
        this.options4 = []
        if(newVal) {
          getClassInfo({
            api: 'admin.goods.getClassInfo',
            pageSize: 100,
            classId: newVal,
            type: 1
          }).then(res => {
            console.log(res);
            this.options3 = res.data.data.classInfo
          })
        }
      }
    },

    'ruleForm.select_4'(newVal,oldVal){
      if(oldVal) {
        console.log(newVal);
        this.ruleForm.select_5 = ''
        this.options4 = []
        if(newVal) {
          getClassInfo({
            api: 'admin.goods.getClassInfo',
            pageSize: 100,
            classId: newVal,
            type: 1
          }).then(res => {
            console.log(res);
              this.options4 = res.data.data.classInfo
          })
        }
      }
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

    async getClassInfos() {
      const res = await getClassInfo({
        api: 'admin.goods.getClassInfo',
        pageSize: 100,
      })
      if(this.$route.query.classlevel == 1) {
        let list = []
        res.data.data.classInfo.map(item => {
          if(this.ruleForm.classname != item.pname) {
            list.push(item)
          }
        })
        this.options1 = list
        console.log(this.options1)
      } else {
        this.options1 = res.data.data.classInfo
      }
    },

    // 获取当前分类的所有上级分类
    async getClassLevelTopAllInfos() {
      const res = await getClassLevelTopAllInfo({
        api: 'admin.goods.getClassLevelTopAllInfo',
        classId: this.$route.query.id
      })
      console.log(res);
      // this.options1 = res.data.data.levelInfoList[0]
      this.ruleForm.select_2 = res.data.data.levelInfoList[0][0].cid
      this.options2 = res.data.data.levelInfoList[1] ? res.data.data.levelInfoList[1] : []
      this.ruleForm.select_3 = res.data.data.levelInfoList[1][0].cid ? res.data.data.levelInfoList[1][0].cid : ''
      this.options3 = res.data.data.levelInfoList[2] ? res.data.data.levelInfoList[2] : []
      this.ruleForm.select_4 = res.data.data.levelInfoList[2][0].cid ? res.data.data.levelInfoList[2][0].cid : ''
      this.options4 = res.data.data.levelInfoList[3] ? res.data.data.levelInfoList[3] : []
      this.ruleForm.select_5 = res.data.data.levelInfoList[3][0].cid ? res.data.data.levelInfoList[3][0].cid : ''
    },

    submitForm(formName) {
      this.$refs[formName].validate(async (valid) => {
        console.log(this.ruleForm);
        if (valid) {
          try {
            //直接用maxlength="15"来判断长度不能大于15个字符，不需要这样去判断 --禅道53479
            // if(this.ruleForm.classname.length > 15) {
            //   this.$message({
            //     message: '分类名称长度不能大于15个字符',
            //     type: 'error',
            //     offset:102
            //   })
            //   return
            // }
            // if(this.ruleForm.subtitle.length > 15) {
            //   this.$message({
            //     message: '英文副标题长度不能大于15个字符',
            //     type: 'error',
            //     offset:102
            //   })
            //   return
            // }
            if(this.inputShow === 1) {
              addClass({
                api: 'admin.goods.addClass',
                className: this.ruleForm.classname,
                ename: this.ruleForm.subtitle,
                lang_code: this.ruleForm.lang_code,
                country_num: this.ruleForm.country_num,
                level: 0,
                fatherId: 0,
                classId: this.$route.params.cid,
                img: this.ruleForm.classLogo
              }).then(res => {
                if(res.data.code == '200') {
                  console.log(res);
                  this.$message({
                    message: this.$t('zdata.bjcg'),
                    type: 'success',
                    offset:102
                  })
                  this.$router.go(-1)
                }
              })
            } else {
              addClass({
                api: 'admin.goods.addClass',
                className: this.ruleForm.classname,
                ename: this.ruleForm.subtitle,
                lang_code: this.ruleForm.lang_code,
                country_num: this.ruleForm.country_num,
                level: this.inputShow - 1,
                classId: this.$route.params.cid,
                fatherId: this.getfatherMenuId(this.inputShow),
                img: this.ruleForm.classLogo
              }).then(res => {
                if(res.data.code == '200') {
                  console.log(res);
                  this.$message({
                    message: this.$t('zdata.bjcg'),
                    type: 'success',
                    offset:102
                  })
                  this.$router.go(-1)
                }
              })
            }
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

    getType(value) {
      if(value === '商城') {
          return 0
      } else {
          return 1
      }
    },

    getfatherMenuId(value) {
      if(this.inputShow === 2) {
        return this.ruleForm.select_2
      } else if(this.inputShow === 3) {
        if(this.ruleForm.select_3 === '') {
          return this.ruleForm.select_2
        } else {
          return this.ruleForm.select_3
        }
      } else if(this.inputShow === 4) {
        if(this.ruleForm.select_4 === '') {
          return this.ruleForm.select_3
        } else if(this.ruleForm.select_3 === '') {
          return this.ruleForm.select_2
        } else {
          return this.ruleForm.select_4
        }
      } else {
        if(this.ruleForm.select_5 === '') {
          return this.ruleForm.select_4
        } else if(this.ruleForm.select_4 === '') {
          return this.ruleForm.select_3
        } else if(this.ruleForm.select_3 === '') {
          return this.ruleForm.select_2
        } else {
          return this.ruleForm.select_5
        }
      }
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
        height: 40px;
      }
    }

    .superior {
      display: flex;
      width: 580px;
      .el-select {
        width: auto;
        flex: 1;
        &:not(:first-child) {
          margin-left: 8px;
        }
        .el-input {
          width: auto;
        }
      }
    }

  }

  /deep/.el-form-item__label {
    font-weight: normal;
  }
}
</style>
