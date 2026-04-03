import { get_class } from '@/api/plug_ins/integralMall'
import { getGoodsDistributionInfo, getDistributionGradeList, addDistributionGoods } from '@/api/plug_ins/distribution'

export default {
  name: 'editorGoods',

  data() {
    return {
      customerDistributteSet: [],
      ruleForm: {
        distributionSet: 2,
        level: '',
        direct_drive: '',
        between_push: '',
        pv: ''
      },

      rules: {
        distributionSet: [
          { required: true, message: this.$t('distribution.addGoods.qxzfxsz'), trigger: 'change' }
        ],
        pv: [
          { required: true, message: this.$t('distribution.addGoods.qsrpvz'), trigger: 'blur' }
        ]
      },

      distributionSetList: [
        {
          value: 2,
          name: this.$t('distribution.addGoods.zdysz')
        },
        {
          value: 1,
          name: this.$t('distribution.addGoods.syfxdj')
        },
      ],

      levelList: [],

      select: '0',

      id: null,
      sid: null,

      tableData: [],
    }
  },

  created() {
    this.getGoodsDistributionInfo()
    this.getDistributionGradeLists()
  },

  methods: {
    async getGoodsDistributionInfo() {
      // 分销商品信息
      const res = await getGoodsDistributionInfo({
        api: 'plugin.distribution.AdminDistribution.getGoodsDistributionInfo',
        pageSize: this.pageSize,
        pageNo: this.dictionaryNum,
        id: this.$route.query.id
      })
      console.log('res', res)
      this.id = res.data.data.list[0].id
      this.sid = res.data.data.list[0].s_id
      this.tableData = res.data.data.list
      this.ruleForm.distributionSet = res.data.data.list[0].type
      this.select = res.data.data.list[0].directUnit ? res.data.data.list[0].directUnit : '%'
      this.ruleForm.direct_drive = res.data.data.list[0].directAmt
      this.ruleForm.between_push = res.data.data.list[0].indirectAmt
      this.ruleForm.level = res.data.data.list[0].uplevel == 0 ? '' : `${res.data.data.list[0].uplevel}`
      this.ruleForm.pv = res.data.data.list[0].pv
      // 等级规则
      this.customerDistributteSet = res.data.data.list[0].customerDistributteSet

      if (!this.customerDistributteSet) {
        this.customerDistributteSet = []
        this.getDistributionGradeLists();
      }
    },

    async getDistributionGradeLists() {
      const res = await getDistributionGradeList({
        api: 'plugin.distribution.AdminDistribution.getDistributionGradeList'
      })

      this.levelList = res.data.data.gradeInfoList

      for (let i = 0; i < this.levelList.length; i++) {
        let gradeInfo = {};
        gradeInfo['id'] = this.levelList[i]['value']
        gradeInfo['direct_m'] = 0
        gradeInfo['indirect_m'] = 0
        //(0-100] 后台计算的时候 * 0.01 了
        gradeInfo['diy_discount'] = 100
        gradeInfo['direct_mode_type'] = 1
        gradeInfo['indirect_mode_type'] = 1
        gradeInfo['gradeName'] = this.levelList[i]['label']
        this.customerDistributteSet.push(gradeInfo)
      }

      console.log(this.customerDistributteSet);

      this.levelList.unshift({
        label: this.$t('distribution.addGoods.qxzkjs'),
        value: ""
      })
    },

    tableHeaderColor({ row, column, rowIndex, columnIndex }) {
      if (rowIndex === 0) {
        return 'background-color: #f4f7f9;'
      }
    },

    submitForm(formName) {

      // if (this.ruleForm.distributionSet == 2) {
      //   if (!this.ruleForm.direct_drive) {
      //     this.$message({
      //       message: this.$t('distribution.addGoods.qsrztfy'),
      //       type: 'error',
      //       offset: 100
      //     })
      //     return
      //   }
      //   if (!this.ruleForm.between_push) {
      //     this.$message({
      //       message: this.$t('distribution.addGoods.qsrjtfy'),
      //       type: 'error',
      //       offset: 100
      //     })
      //     return
      //   }
      // }

      this.$refs[formName].validate(async (valid) => {
        console.log(this.ruleForm);
        if (valid) {
          try {
            // debugger
            let customerRuleSet = [];
            if (this.ruleForm.distributionSet == 2) {
              for (let i = 0; i < this.customerDistributteSet.length; i++) {
                customerRuleSet.push(this.customerDistributteSet[i]);
                let diy_discount = customerRuleSet[i].diy_discount;
                if (diy_discount < 0 || diy_discount > 100) {
                  this.$message({
                    message: this.$t('distribution.addGoods.diy_discount'),
                    type: 'error',
                    offset: 100
                  })
                  return
                }
              }
            } else {
              customerRuleSet = null
            }

            let data = {
              api: 'plugin.distribution.AdminDistribution.addDistributionGoods',
              id: this.id,
              sid: this.sid,
              uplevel: this.ruleForm.level ? this.ruleForm.level : 0,
              distributionRule: this.ruleForm.distributionSet,
              directType: this.select == '%' || this.select == '0' ? 0 : 1,
              directM: this.ruleForm.direct_drive || '',
              indirectType: this.select == '%' || this.select == '0' ? 0 : 1,
              indirectM: this.ruleForm.between_push || '',
              pv: this.ruleForm.pv
            }

            if (customerRuleSet != null) {
              data.customerDistributteSet = JSON.stringify(customerRuleSet);
            }

            addDistributionGoods(data).then(res => {
              if (res.data.code == '200') {
                this.$message({
                  message: this.$t('distribution.cg'),
                  type: 'success',
                  offset: 100
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

    oninput(num, limit) {
      var str = num
      var len1 = str.substr(0, 1)
      var len2 = str.substr(1, 1)
      //如果第一位是0，第二位不是点，就用数字把点替换掉
      if (str.length > 1 && len1 == 0 && len2 != ".") {
        str = str.substr(1, 1)
      }
      //第一位不能是.
      if (len1 == ".") {
        str = ""
      }
      //限制只能输入一个小数点
      if (str.indexOf(".") != -1) {
        var str_ = str.substr(str.indexOf(".") + 1)
        if (str_.indexOf(".") != -1) {
          str = str.substr(0, str.indexOf(".") + str_.indexOf(".") + 1)
        }
      }
      //正则替换
      str = str.replace(/[^\d^\.]+/g, '') // 保留数字和小数点
      if (limit / 1 === 1) {
        str = str.replace(/^\D*([0-9]\d*\.?\d{0,1})?.*$/, '$1') // 小数点后只能输 1 位
      } else {
        str = str.replace(/^\D*([0-9]\d*\.?\d{0,2})?.*$/, '$1') // 小数点后只能输 2 位
      }
      return str
    },

    oninput2(num) {
      var str = num
      str = str.replace(/[^\.\d]/g, '');
      str = str.replace('.', '');

      return str
    },
  }
}
