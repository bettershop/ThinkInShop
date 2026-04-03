import {isEmpty} from "element-ui/src/utils/util";

export default {
  name: 'country',

  //初始化数据
  data() {
    return {
      countriesList: [],
      districts: [],
      district_pid:'',
      district_level:'',
      district_country_num:'',
      district_name:'',
      ruleForm: {
        id: '',
        district_pid:'',
        district_country_num:'',
        district_name:''
      },
      rules: {
        district_name: [{required: true, message: this.$t('district.qsrdistrict_name'),trigger: 'blur'}],
        district_country_num: [{required: true, message: this.$t('district.qxzdistrict_country_num'), trigger: 'blur'}]
      }
    }
  },
  //组装模板
  created() {
    this.id = this.$route.params.id;
    if (!isEmpty(this.id)) {
      this.loadData(this.id)
    }
    this.loadPDistrictData();
    this.getCountrys();
  },


  // beforeRouteLeave (to, from, next) {
  //   if (to.name == 'logisticsCompanyList' && this.$route.name == 'logisticsCompanyEdit') {
  //     to.params.dictionaryNum = this.$route.query.dictionaryNum
  //     to.params.pageSize = this.$route.query.pageSize
  //   }
  //   next();
  // },

  methods: {
    // 获取国家列表
    async getCountrys() {
      const res = await this.LaiKeCommon.getCountries()
      this.countriesList = res.data.data
    },

    //获取上级地区信息
    async loadPDistrictData() {
      const res = await this.LaiKeCommon.select({
        api: 'admin.district.allDistrict',
      }).then(data => {
        this.districts = data.data.data.list;
      });
    },

    //获取所属国家num
    getIds(value) {
      this.district_country_num = value
    },

    //获取上级地区id
    getDistrictid(value) {

      this.district_pid = value.id
      this.ruleForm.district_pid = value.id

      // this.district_level = value.district_level || ''


      this.ruleForm.district_country_num = value.district_country_num
      this.district_country_num = value.district_country_num
      console.log(value,'value---value')
    },

    async loadData(id) {
      const res = await this.LaiKeCommon.select({
        api: 'admin.district.districtList',
        id: id,
      }).then(res => {
        this.ruleForm = res.data.data.model;
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
            await this.LaiKeCommon.add({
              api: 'admin.district.saveOrEditCountry',
              id: this.ruleForm.id,
              district_name: this.ruleForm.district_name,
              district_pid: this.ruleForm.district_pid,
              district_level:  2,
              district_country_num: this.ruleForm.district_country_num,
              district_num: 0,
              district_delete: 0,
              district_childcount: 0,
              is_show: 1 ,
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
