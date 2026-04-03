import {isEmpty} from "element-ui/src/utils/util";

export default {
  name: 'country',

  //初始化数据
  data() {
    return {
      ruleForm: {
        id: '',
        name: '',
        zh_name: '',
        num3: '',
        code: '',
        code2: '',
        national_flag: ''
      },
      rules: {
        name: [{required: true, message: this.$t('countryManage.qsrname'),trigger: 'blur'}],
        zh_name: [{required: true, message: this.$t('countryManage.qsrzh_name'), trigger: 'blur'}],
        national_flag: [{required: true, message: this.$t('systemConfig.qxzlogo'), trigger: 'change'}],
        code2: [{required: true, message: this.$t('countryManage.qsrcode2'), trigger: 'blur'}],
        code: [{required: true, message: this.$t('countryManage.qsrcode'), trigger: 'blur'}],
        num3: [{required: true, message: this.$t('countryManage.qsrnum3'), trigger: 'blur'}]
      }
    }
  },
  //组装模板
  created() {
    this.id = this.$route.params.id;
    if (!isEmpty(this.id)) {
      this.loadData(this.id)
    }
  },

  // beforeRouteLeave (to, from, next) {
  //   if (to.name == 'logisticsCompanyList' && this.$route.name == 'logisticsCompanyEdit') {
  //     to.params.dictionaryNum = this.$route.query.dictionaryNum
  //     to.params.pageSize = this.$route.query.pageSize
  //   }
  //   next();
  // },

  methods: {
    async loadData(id) {
      const res = await this.LaiKeCommon.select({
        api: 'admin.country.countryList',
        id: id,
      }).then(data => {
        this.ruleForm = data.data.data.list[0];
      });
    },
    //添加/编辑
    async Save(formName) {
      this.$refs[formName].validate(async (valid) => {
        console.log(this.ruleForm);
        if (valid) {
          try {
            let text = this.$t('zdata.bjcg');
            if (isEmpty(this.ruleForm.id)) {
              text = this.$t('zdata.tjcg');
            }
            const res = await this.LaiKeCommon.add({
              api: 'admin.country.saveOrEditCountry',
              id: this.ruleForm.id,
              zh_name: this.ruleForm.zh_name,
              name: this.ruleForm.name,
              code: this.ruleForm.code,
              code2: this.ruleForm.code2,
              num3: this.ruleForm.num3 ,
              is_show: 1 ,
              national_flag: this.ruleForm.national_flag
            }).then(data => {
              if (!isEmpty(data) && data.data.code == '200') {
                this.$message({
                  message: text ,
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
