<template>
  <div class="container">
    <div class="add-menu">
      <el-form :model="ruleForm" :rules="rules" ref="ruleForm"  class="picture-ruleForm" label-width="auto">
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
        <el-form-item :label="$t('addgoodsclass1.flmc')" prop="classname">
          <el-input disabled v-model="ruleForm.classname" :placeholder="$t('addgoodsclass1.qsrflmc')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('addgoodsclass1.ywfbt')">
          <el-input disabled v-model="ruleForm.subtitle" :placeholder="$t('addgoodsclass1.qsrywfbt')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('addgoodsclass1.fldj')" prop="classlevel">
          <el-select disabled class="select-input" v-model="ruleForm.classlevel" :placeholder="$t('addgoodsclass1.qxzfldj')">
            <el-option v-for="(item,index) in menuLevelList" :key="index" :label="item.label" :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('addgoodsclass1.sjfl')" :prop="inputShow == 2?'select_2':inputShow == 3?'select_3':inputShow == 4?'select_4':'select_5'" v-if="inputShow !== 1">
          <div class="superior">
            <el-select disabled v-show="inputShow >= 2" class="select-input" v-model="ruleForm.select_2" :placeholder="$t('addgoodsclass1.qxzyij')">
              <el-option v-for="(item,index) in options1" :key="index" :label="item.pname" :value="item.cid">
              </el-option>
            </el-select>
            <el-select disabled v-show="inputShow >= 3" class="select-input" v-model="ruleForm.select_3" :placeholder="$t('addgoodsclass1.qxzerj')">
              <el-option v-for="(item,index) in options2" :key="index" :label="item.pname" :value="item.cid">
              </el-option>
            </el-select>
            <el-select disabled v-show="inputShow >= 4" class="select-input" v-model="ruleForm.select_4" :placeholder="$t('addgoodsclass1.qxzsanj')">
              <el-option v-for="(item,index) in options3" :key="index" :label="item.pname" :value="item.cid">
              </el-option>
            </el-select>
            <el-select disabled v-show="inputShow >= 5" class="select-input" v-model="ruleForm.select_5" :placeholder="$t('addgoodsclass1.qxzsij')">
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
        <el-form-item :label="$t('brandExamine.ssfl')" required>
          <el-input
            v-model="ruleForm.suppliername"
            disabled
            :placeholder="$t('viewgoodsclasslower.qsrgys')"
          ></el-input>
        </el-form-item>
        <el-form-item :label="$t('viewgoodsclasslower.tjsj')" required>
          <el-input
            v-model="ruleForm.time"
            disabled
            :placeholder="$t('viewgoodsclasslower.qsrtjsj')"
          ></el-input>
        </el-form-item><el-form-item :label="$t('viewgoodsclasslower.zt')" required>
          <el-input
            v-model="ruleForm.status"
            disabled
            placeholder=""
          ></el-input>
        </el-form-item>
        <!-- <div class="form-footer">
          <el-form-item>
            <el-button class="bgColor" type="primary" @click="submitForm('ruleForm')">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
            <el-button class="bdColor" @click="$router.go(-1)" plain>{{ $t('DemoPage.tableFromPage.cancel') }}</el-button>
          </el-form-item>
        </div> -->
      </el-form>
    </div>
  </div>
</template>

<script>
import { getClassInfo, addClass, getClassLevelTopAllInfo } from '@/api/goods/goodsClass'
export default {
  name: 'classDetail',

  data() {
    return {
      countriesList: [],
      languages: [],
      ruleForm: {
        classname: '',
        subtitle: '',
        classlevel: '',
        classLogo: '',
        superiorclass: '',
        select_2: '',
        select_3: '',
        select_4: '',
        select_5: '',
        suppliername: "",
        time: "",
        country_num:"",
        lang_code:"",
        status: ""
      },
      inputShow: 1,
      rules: {
        classname: [
          { required: true, message: '请填写分类名称', trigger: 'blur' }
        ],
        subtitle: [
          { required: true, message: '请填写英文标题', trigger: 'blur' }
        ],
        classlevel: [
          { required: true, message: '请填写分类等级', trigger: 'change' }
        ],
        select_2: [
					{ required: true, message: '请选择上级分类', trigger: 'blur' }
				],
				select_3: [
					{ required: true, message: '请选择上级分类', trigger: 'blur' }
				],
				select_4: [
					{ required: true, message: '请选择上级分类', trigger: 'blur' }
				],
				select_5: [
					{ required: true, message: '请选择上级分类', trigger: 'blur' }
				],
      },

      menuLevelList: [
        {
          value: 1,
          label: '一级'
        },
        {
          value: 2,
          label: '二级'
        },
        {
          value: 3,
          label: '三级'
        },
        {
          value: 4,
          label: '四级'
        },
        {
          value: 5,
          label: '五级'
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
    this.ruleForm.suppliername = this.$route.params.supplier_name
    this.ruleForm.time = this.$route.params.add_date
    this.ruleForm.status = this.$route.params.examineDesc

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


    // 获取语言列表
    async getLanguages() {
      const res = await this.LaiKeCommon.getLanguages();
      this.languages = res.data.data
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
        // this.options1 = res.data.data.classInfo
      }
    },

    // 获取当前分类的所有上级分类
    async getClassLevelTopAllInfos() {
      const res = await getClassLevelTopAllInfo({
        api: 'admin.supplier.goods.getClassLevelTopAllInfo',
        classId: this.$route.query.id
      })
      console.log(res);
      this.options1 = res.data.data.levelInfoList[0]
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
            if(this.ruleForm.classname.length > 15) {
              this.$message({
                message: '分类名称长度不能大于15个字符',
                type: 'error',
                offset:100
              })
              return
            }
            if(this.ruleForm.subtitle.length > 15) {
              this.$message({
                message: '英文副标题长度不能大于15个字符',
                type: 'error',
                offset:100
              })
              return
            }
            if(this.inputShow === 1) {
              addClass({
                api: 'admin.goods.addClass',
                className: this.ruleForm.classname,
                ename: this.ruleForm.subtitle,
                level: 0,
                fatherId: 0,
                classId: this.$route.params.cid,
                img: this.ruleForm.classLogo
              }).then(res => {
                if(res.data.code == '200') {
                  console.log(res);
                  this.$message({
                    message: '添加成功',
                    type: 'success',
                    offset:100
                  })
                  this.$router.go(-1)
                }
              })
            } else {
              addClass({
                api: 'admin.goods.addClass',
                className: this.ruleForm.classname,
                ename: this.ruleForm.subtitle,
                level: this.inputShow - 1,
                classId: this.$route.params.cid,
                fatherId: this.getfatherMenuId(this.inputShow),
                img: this.ruleForm.classLogo
              }).then(res => {
                if(res.data.code == '200') {
                  console.log(res);
                  this.$message({
                    message: '添加成功',
                    type: 'success',
                    offset:100
                  })
                  this.$router.go(-1)
                }
              })
            }
          } catch (error) {
            this.$message({
                message: error.message,
                type: 'error',
                showClose: true,
                offset: 100,
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
      .el-form-item__content {
        margin-left: 0px !important;
      }
      .el-form-item__content {
        width: 580px;
      }
      .select-input {
        width: 580px;
        height: 40px;
      }
    }

    .upload-img {
      pointer-events: none;
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
