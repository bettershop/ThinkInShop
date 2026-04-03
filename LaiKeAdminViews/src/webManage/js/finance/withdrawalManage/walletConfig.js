import {index, save} from '@/api/finance/withdrawalManage'
import {isEmpty} from "element-ui/src/utils/util";
import { router } from '@/router';


export default {
  name: 'walletConfig',
  data() {
    return {
      laikeCurrencySymbol:'￥',
      radio1: this.$t('withdrawalExamineList.qbcs'),
      routerList:JSON.parse(sessionStorage.getItem('tabRouter')),
      mainForm: {
        min_cz: null,
        min_amount: null,
        max_amount: null,
        service_charge: null,
        unit: null,
      },
      rules: {
        min_cz: [
          {required: true, message: this.$t('walletConfig.qsrzxczje'), trigger: 'blur'},
          { pattern: /(^[1-9]([0-9]+)?(\.[0-9]{1,2})?$)|(^(0){1}$)|(^[0-9]\.[0-9]([0-9])?$)/, message: '请输入正确的格式,可保留两位小数' }
        ],
        min_amount: [
          {required: true, message: this.$t('walletConfig.qsrzxtx'), trigger: 'blur'},
          { pattern: /(^[1-9]([0-9]+)?(\.[0-9]{1,2})?$)|(^(0){1}$)|(^[0-9]\.[0-9]([0-9])?$)/, message: '请输入正确的格式,可保留两位小数' }
        ],
        max_amount: [
          {required: true, message: this.$t('walletConfig.qsrzdtx'), trigger: 'blur'},
          { pattern: /(^[1-9]([0-9]+)?(\.[0-9]{1,2})?$)|(^(0){1}$)|(^[0-9]\.[0-9]([0-9])?$)/, message: '请输入正确的格式,可保留两位小数' }
        ],
        service_charge: [
          {required: true, message: this.$t('walletConfig.qsrsxf'), trigger: 'blur',show:false},
          { pattern: /(^[1-9]([0-9]+)?(\.[0-9]{0,1})?$)|(^(0){1}$)|(^[0-9]\.[0-9]?$)/, message: '请输入正确的格式,可保留1位小数' }
        ],
        unit: [
          {required: true, message: this.$t('walletConfig.qsrqbdw'), trigger: 'blur'},
        ],
      }
    }
  },

  created() {
    this.laikeCurrencySymbol = this.LaiKeCommon.getDefaultCurrencySymbol();
    this.loadData()
  },

  methods: {
    // ruleForm() {
    //   this.mainForm.rules = {
    //     min_cz: [{required: true, message: '请输入最小充值金额', trigger: 'change'}],
    //     min_amount: [{required: true, message: '请输入最小提现金额', trigger: 'change'}],
    //     max_amount: [{required: true, message: '请输入最大提现金额', trigger: 'change'}],
    //     service_charge: [{required: true, message: '请输入手续费', trigger: 'change'}],
    //     unit: [{required: true, message: '请输入钱包单位', trigger: 'change'}],
    //   }
    // },
    async loadData() {
      const res = await index({
        api: 'admin.user.getWalletInfo',
      }).then(data => {
        console.log(data);
        let info = data.data.data.data
        // this.mainForm = data.data.data.data;
        this.mainForm.min_cz = info.min_cz.toFixed(2)
        this.mainForm.min_amount = info.min_amount.toFixed(2)
        this.mainForm.max_amount = info.max_amount.toFixed(2)
        this.mainForm.service_charge = info.service_charge==0?0:(info.service_charge*100).toFixed(1)
        // this.mainForm.unit = info.unit
        // this.ruleForm()
      })
    },
    submitForm(formName) {
      this.$refs[formName].validate(async (valid) => {
        if(Number(this.mainForm.max_amount) < Number(this.mainForm.min_amount)){
          this.$message({
            type: 'warning',
            message: this.$t('walletConfig.zdbxdy'),
            offset: 100
          })
          return
        }
        if (valid) {
          try {
            save({
              api: 'admin.user.setWalletInfo',
              minMoney: this.mainForm.min_cz,
              minOutMoney: this.mainForm.min_amount,
              maxOutMoney: this.mainForm.max_amount,
              serviceMoney: (this.mainForm.service_charge/100).toFixed(3),
              // unit: this.mainForm.unit,
            }).then(res => {
              if (res.data.code == '200') {
                this.loadData()
                this.$message({
                  message: this.$t("zdata.baccg"),
                  type: "success",
                  offset: 100,
                });
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
