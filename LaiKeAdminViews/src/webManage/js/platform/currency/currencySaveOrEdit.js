

import {isEmpty} from "element-ui/src/utils/util";

export default {
  name: 'country',

  //初始化数据
  data() {
    return {
      currency_code: '',
      currency_name: '',
      currency_symbol: '',
      ruleForm: {
        id: '',
        currency_code: '',
        currency_name: '',
        currency_symbol: '',
      },
      rules: {
        currency_name: [{required: true, message: this.$t('currency.qsrcurrencyname'),trigger: 'blur'}],
        currency_code: [{required: true, message: this.$t('currency.qsrzhcurrencycode'), trigger: 'blur'}],
        currency_symbol: [{required: true, message: this.$t('currency.qsrcurrencysymbol'), trigger: 'blur'}],
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


  methods: {
    async loadData(id) {
      const res = await this.LaiKeCommon.select({
        api: 'admin.currency.currencyList',
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
              api: 'admin.currency.saveOrEditCurrency',
              id: this.ruleForm.id,
              currency_name: this.ruleForm.currency_name,
              currency_code: this.ruleForm.currency_code,
              currency_symbol: this.ruleForm.currency_symbol,
              is_show: 1
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

