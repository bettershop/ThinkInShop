<template>
  <div class="container">
    <div class="add-menu">
      <el-form :model="ruleForm" :rules="rules" ref="ruleForm"  class="picture-ruleForm" label-width="280px">

        <el-form-item :label="$t('ssgj')" prop="country_num">
          <el-select disabled="disabled" class="select-input" filterable v-model="ruleForm.country_num" :placeholder="$t('qxzssgj')">
            <el-option  v-for="(item,index) in countriesList" :key="index" :label="item.zh_name" :value="item.num3">
              <div @click="getIds(item.num3)">{{ item.zh_name }}</div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item :label="$t('yz')" prop="lang_code">
          <el-select disabled="disabled" class="select-input" filterable v-model="ruleForm.lang_code" :placeholder="$t('qxzyz')">
            <el-option v-for="(item,index) in languages" :key="index" :label="item.lang_name" :value="item.lang_code">
              <div @click="getlangCode(item.lang_code)">{{ item.lang_name }}</div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item :label="$t('permissions.addmenulevel.cdlx')" prop="menutype">
          <el-select class="select-input" v-model="ruleForm.menutype" :placeholder="$t('permissions.addmenulevel.qsrcdlx')">
            <el-option v-for="(item,index) in menuTypeList" :key="index" :label="item.label" :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item :label="$t('permissions.addmenulevel.cddj')" prop="menulevel" >
          <el-select class="select-input" v-model="ruleForm.menulevel" :placeholder="$t('permissions.addmenulevel.qsrcddj')">
            <el-option v-for="(item,index) in menuLevelList" :key="index" :label="item.label" :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item :label="$t('permissions.addmenulevel.sjcd1')" prop="superiorclass" v-if="inputShow !== 1">
          <div class="superior">
            <el-select v-show="inputShow >= 2" class="select-input" v-model="ruleForm.select_2" :placeholder="$t('permissions.addmenulevel.qxzyj')">
              <el-option v-for="(item,index) in options1" :key="index" :label="item.title" :value="item.id">
              </el-option>
            </el-select>
            <el-select v-show="inputShow >= 3" class="select-input" v-model="ruleForm.select_3" :placeholder="$t('permissions.addmenulevel.qxzej')">
              <el-option v-for="(item,index) in options2" :key="index" :label="item.title" :value="item.id">
              </el-option>
            </el-select>
            <el-select  v-show="inputShow >= 4" class="select-input" v-model="ruleForm.select_4" :placeholder="$t('permissions.addmenulevel.qxzsj')">
              <el-option v-for="(item,index) in options3" :key="index" :label="item.title" :value="item.id">
              </el-option>
            </el-select>
          </div>
        </el-form-item>


        <el-form-item :label="$t('permissions.addmenulevel.cdmc')" prop="menuname">
          <el-input v-model="ruleForm.menuname" :placeholder="$t('permissions.addmenulevel.qsrcdmc')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('permissions.addmenulevel.lj')" prop="path">
          <el-input v-model="ruleForm.path" :placeholder="$t('permissions.addmenulevel.qsrpath')"></el-input>
          <span style="position: absolute;top: 0;width:400px;color: #97A0B4;font-size: 14px;">{{
            $t("permissions.addmenulevel.ljts")
          }}</span>
        </el-form-item>

        <!-- <el-form-item :label="$t('permissions.addmenulevel.sfhx')" prop="is_core">
          <el-radio-group v-model="ruleForm.is_core">
            <el-radio v-for="label in coreList" :label="label.value" :key="label.value">{{label.label}}</el-radio>
          </el-radio-group>
        </el-form-item> -->

        <el-form-item :label="$t('permissions.addmenulevel.qxpz')" prop="isButton"  v-if="inputShow !== 1&&inputShow !== 2">
          <el-radio-group v-model="ruleForm.isButton">
            <el-radio v-for="label in isButtonList" :label="label.value" :key="label.value">{{label.label}}</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item
          v-if="inputShow !== 1&&inputShow !== 2"
          :label="$t('permissions.addmenulevel.sfwtabym')"
          prop="isTab"
          required
        >
          <el-radio-group v-model="ruleForm.isTab">
            <el-radio
              v-for="label in isTabList"
              :label="label.value"
              :key="label.value"
              >{{ label.label }}</el-radio
            >
          </el-radio-group>
        </el-form-item>

         <el-form-item :label="$t('permissions.addmenulevel.cddz')" prop="menulocal" v-if="inputShow !== 1">
          <el-input v-model="ruleForm.menulocal" :placeholder="$t('permissions.addmenulevel.qsrcddz')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('permissions.addmenulevel.dlmc')" prop="tourname">
          <el-input v-model="ruleForm.tourname" :placeholder="$t('permissions.addmenulevel.qsrdlmc')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('permissions.addmenulevel.dldz')" v-if="inputShow !== 1">
          <el-input v-model="ruleForm.menuPath" :placeholder="$t('permissions.addmenulevel.qsrdldz')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('permissions.addmenulevel.dljj')" prop="tourintroduction" v-if="inputShow !== 1">
          <el-input v-model="ruleForm.tourintroduction" :placeholder="$t('permissions.addmenulevel.qsrdljj')"></el-input>
        </el-form-item>
        <el-form-item class="upload-img" :label="$t('permissions.addmenulevel.mrtb')" prop="defaultLogo" v-if="inputShow === 1">
            <l-upload
              :limit="1"
              v-model="ruleForm.defaultLogo"
              :text="$t('permissions.addmenulevel.zdsc')"
            >
            </l-upload>
        </el-form-item>
        <el-form-item class="upload-img" :label="$t('permissions.addmenulevel.xztb')" prop="selectLogo" v-if="inputShow === 1">
            <l-upload
              :limit="1"
              v-model="ruleForm.selectLogo"
              :text="$t('permissions.addmenulevel.zdsc')"
            >
            </l-upload>
        </el-form-item>
        <el-form-item class="upload-img" :label="$t('permissions.addmenulevel.dltb')" prop="tourlogo" v-if="inputShow !== 1">
            <l-upload
              :limit="1"
              v-model="ruleForm.tourlogo"
              :text="$t('permissions.addmenulevel.zdsc64')"
            >
            </l-upload>
        </el-form-item>
        <div class="form-footer">
          <el-form-item>
            <el-button class="bgColor" type="primary" @click="submitForm('ruleForm')">{{ $t('DemoPage.tableFromPage.save') }}</el-button>
            <el-button class="bdColor" @click="$router.go(-1)" plain>{{ $t('DemoPage.tableFromPage.cancel') }}</el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import { addMenuInfo, getMenuLeveInfo } from '@/api/Platform/permissions'
export default {
  name: 'addmenulevel',

  data() {
    return {
      countriesList: [],
      languages: [],
      lang_code: null,
      country_num: null,
      ruleForm: {
        lang_code: "",
        country_num: "",
        menutype: '',
        menulevel: 1,
        menuname: '',
        path: '',
        is_core: 1,
        select_2: '',
        select_3: '',
        select_4: '',
        menulocal: '',
        tourname: '',
        tourintroduction: '',
        defaultLogo: '',
        selectLogo: '',
        isButton:0,
        tourlogo: '',
        menuPath: '',
        isTab:0
      },

      rules: {

        country_num: [
          { required: true, message: this.$t('qxzssgj') , trigger: 'blur' }
        ],

        lang_code: [
          { required: true, message: this.$t('qxzyz') , trigger: 'blur' }
        ],

        menutype: [
          { required: true, message: this.$t('permissions.addmenulevel.qsrcdlx'), trigger: 'change' }
        ],
        menulevel: [
          { required: true, message: this.$t('permissions.addmenulevel.qsrcddj'), trigger: 'change' }
        ],
        menuname: [
          { required: true, message: this.$t('permissions.addmenulevel.qsrcdmc'), trigger: 'blur' }
        ],
        path: [
          { required: true, message: this.$t('permissions.addmenulevel.qsrpath'), trigger: 'blur' }
        ],
        is_core: [
          { required: true, message: this.$t('permissions.addmenulevel.qxzsfhx'), trigger: 'change' }
        ],
        isButton: [
          { required: true, message: this.$t('permissions.addmenulevel.qxzsfqx'), trigger: 'change' }
        ],
        isTab:[{
            required: true,
            message: this.$t("permissions.addmenulevel.qsrsfwqx"),
            trigger: "change",
          },],
        menulocal: [
          { required: true, message: this.$t('permissions.addmenulevel.qsrcddz'), trigger: 'blur' }
        ],
        defaultLogo: [
          { required: true, message: this.$t('permissions.addmenulevel.qscmrtb'), trigger: 'change' }
        ],
        selectLogo: [
          { required: true, message: this.$t('permissions.addmenulevel.qscxztb'), trigger: 'change' }
        ],
      },

      inputShow: 1,

      menuTypeList: [
        {
          value: 1,
          label: this.$t('permissions.addmenulevel.sc')
        },
        {
          value: 0,
          label: this.$t('permissions.addmenulevel.kzt')
        }
      ],

      coreList: [
        {
          value: 1,
          label: this.$t('permissions.addmenulevel.yes')
        },
        {
          value: 0,
          label: this.$t('permissions.addmenulevel.no')
        }
      ],

      menuLevelList: [
        {
          value: 1,
          label: this.$t('permissions.addmenulevel.yjcd')
        },
        {
          value: 2,
          label: this.$t('permissions.addmenulevel.ejcd')
        },
        {
          value: 3,
          label: this.$t('permissions.addmenulevel.sjcd')
        },
        {
          value: 4,
          label: this.$t('permissions.addmenulevel.sijcd')
        }
      ],
      isButtonList: [
        {
          value: 1,
          label: this.$t('permissions.addmenulevel.zc')
        },
        {
          value: 0,
          label: this.$t('permissions.addmenulevel.bzc')
        }
      ],
      isTabList:[{
          value: 1,
          label: this.$t("permissions.addmenulevel.shi"),
        },
        {
          value: 0,
          label: this.$t("permissions.addmenulevel.bushi"),
        },],
      options1: [],

      options2: [],

      options3: [],

      options4: [],
    }
  },

  watch: {
    'ruleForm.defaultLogo'() {
      if(this.ruleForm.defaultLogo) {
        this.$refs.ruleForm.clearValidate('defaultLogo')
      }
    },

    'ruleForm.selectLogo'() {
      if(this.ruleForm.selectLogo) {
        this.$refs.ruleForm.clearValidate('selectLogo')
      }
    },

    'ruleForm.menulevel'() {
      if(this.ruleForm.menulevel === 2) {
        this.inputShow = 2
        this.ruleForm.select_3 = ''
        this.ruleForm.select_4 = ''
      } else if (this.ruleForm.menulevel === 3) {
        this.inputShow = 3
        this.ruleForm.select_4 = ''
      } else if (this.ruleForm.menulevel === 4) {
        this.inputShow = 4
      }else {
        this.inputShow = 1
        this.ruleForm.select_2 = ''
        this.ruleForm.select_3 = ''
        this.ruleForm.select_4 = ''
      }
    },

    'ruleForm.select_2'(newVal,oldVal){
			if(oldVal) {
        this.ruleForm.select_3 = ''
        this.ruleForm.select_4 = ''
      }
      getMenuLeveInfo({
        api: 'saas.role.getMenuLeveInfo',
        pageSize: 100,
        sid: newVal
      }).then(res => {
        this.options2 = res.data.data.list

        this.ruleForm.lang_code = res.data?.data?.list[0]?.lang_code;
        this.ruleForm.country_num = res.data?.data?.list[0]?.country_num;
      })
		},

		'ruleForm.select_3'(newVal,oldVal){
			console.log(newVal);
      if(oldVal) {
        this.ruleForm.select_4 = ''
      }
      getMenuLeveInfo({
        api: 'saas.role.getMenuLeveInfo',
        pageSize: 100,
        sid: newVal
      }).then(res => {
        this.options3 = res.data.data.list

        this.ruleForm.lang_code = res.data?.data?.list[0]?.lang_code;
        this.ruleForm.country_num = res.data?.data?.list[0]?.country_num;
      })
		},

    'ruleForm.select_4'(newVal){
			console.log(newVal);
      this.ruleForm.select_5 = ''
      getMenuLeveInfo({
        api: 'saas.role.getMenuLeveInfo',
        pageSize: 100,
        sid: newVal
      }).then(res => {
        this.options4 = res.data.data.list

        this.ruleForm.lang_code = res.data?.data?.list[0]?.lang_code;
        this.ruleForm.country_num = res.data?.data?.list[0]?.country_num;
      })
		},
  },

  created() {

    // this.ruleForm.lang_code = this.LaiKeCommon.getUserLangVal();

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

    this.getMenuLeveInfos()
    this.ruleForm.menulevel = this.$route.query.menulevel
    this.ruleForm.menuname = this.$route.params.title
    this.ruleForm.path = this.$route.params.module
    this.ruleForm.is_core = this.$route.params.is_core
    this.ruleForm.menulocal = this.$route.params.url
    this.ruleForm.tourname = this.$route.params.guide_name
    this.ruleForm.defaultLogo = this.$route.params.image
    this.ruleForm.selectLogo = this.$route.params.image1
    this.ruleForm.menutype = this.$route.params.type
    this.ruleForm.tourintroduction = this.$route.params.briefintroduction
    this.ruleForm.tourlogo = this.$route.params.image1
    this.ruleForm.isButton = this.$route.params.is_button
    this.ruleForm.isTab = this.$route.params.is_tab
    this.ruleForm.menuPath = this.$route.params.action
    if(this.$route.query.menulevel == 2) {
        getMenuLeveInfo({
          api: 'saas.role.getMenuLeveInfo',
          pageSize: 100,
          id: this.$route.params.id
        }).then(res => {
          console.log(res);
          this.ruleForm.select_2 = res.data.data.list[0].s_id
        })
    } else if (this.$route.query.menulevel == 3) {
        getMenuLeveInfo({
          api: 'saas.role.getMenuLeveInfo',
          pageSize: 100,
          id: this.$route.params.s_id
        }).then(res => {
          this.ruleForm.select_2 = res.data.data.list[0].s_id
          this.ruleForm.select_3 = res.data.data.list[0].id
        })
    } else if (this.$route.query.menulevel == 4) {
      getMenuLeveInfo({
        api: 'saas.role.getMenuLeveInfo',
        pageSize: 100,
        id: this.$route.params.s_id
      }).then(res => {
        console.log(res);
        this.ruleForm.select_2 = res.data.data.list[0].s_id
        this.ruleForm.select_3 = this.ruleForm.select_3 = res.data.data.list[0].id
        this.ruleForm.select_4 = this.$route.params.id
      })

    }
  },

  beforeRouteLeave (to, from, next) {
    if (to.name == 'menulist' || to.name == 'viewmenu') {
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

    // 获取等级菜单列表
    async getMenuLeveInfos() {
      const res = await getMenuLeveInfo({
        api: 'saas.role.getMenuLeveInfo',
        pageSize: 100,
      })
      console.log(res);
      this.options1 = res.data.data.list
      console.log(this.options1);
    },

    submitForm(formName) {
      this.$refs[formName].validate(async (valid) => {
        console.log(this.ruleForm);
        if (valid) {
          try {
            if(this.inputShow === 1) {
              addMenuInfo({
                api: 'saas.role.addMenuInfo',
                menuClass: this.getType(this.ruleForm.menutype),
                level: this.ruleForm.menulevel,
                menuName: this.ruleForm.menuname,
                path: this.ruleForm.path,
                menuUrl: this.ruleForm.menulocal,
                guideName :this.ruleForm.tourname,
                defaultLogo: this.ruleForm.defaultLogo,
                chekedLogo: this.ruleForm.selectLogo,
                isCore: this.ruleForm.is_core,
                menuClass: this.ruleForm.menutype,
                mid: this.$route.params.id,
                isButton:this.ruleForm.isButton,
                isTab:this.ruleForm.isTab

              }).then(res => {
                if(res.data.code == '200') {
                  console.log(res);
                  this.$message({
                    message: this.$t('commonLanguage.bjcg'),
                    type: 'success',
                    offset:100
                  })
                  this.$router.go(-1)
                }
              })
            } else {
              addMenuInfo({
                api: 'saas.role.addMenuInfo',
                menuClass: this.getType(this.ruleForm.menutype),
                level: this.ruleForm.menulevel,
                menuName: this.ruleForm.menuname,
                path: this.ruleForm.path,
                fatherMenuId: this.getfatherMenuId(this.inputShow),
                menuUrl: this.ruleForm.menulocal,
                guideName :this.ruleForm.tourname,
                briefintroduction: this.ruleForm.tourintroduction,
                chekedLogo: this.ruleForm.tourlogo,
                isCore: this.ruleForm.is_core,
                menuClass: this.ruleForm.menutype,
                mid: this.$route.params.id,
                isButton:this.ruleForm.isButton,
                menuPath: this.ruleForm.menuPath,
                isTab:this.ruleForm.isTab

              }).then(res => {
                if(res.data.code == '200') {
                  console.log(res);
                  this.$message({
                    message: this.$t('commonLanguage.bjcg'),
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
      if(value === '商城菜单') {
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
          return this.ruleForm.select_3
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
  height: 100%;
  background-color: #fff;
  padding: 40px 0 0 0;
  color: #414658;
  overflow: hidden;
  overflow-y: scroll;
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
