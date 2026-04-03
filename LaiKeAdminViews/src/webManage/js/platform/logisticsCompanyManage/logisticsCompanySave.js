import {index, save} from '@/api/Platform/logisticsCompanyManage'
import {isEmpty} from "element-ui/src/utils/util";

export default {
  name: 'logisticsCompanySave',

  //初始化数据
  data() {
    var validateSort = (rule, value, callback) => {
      if(value.length && value.length > 9) {
        callback(new Error("序号长度不能超过九位数"));
      } else {
        callback();
      }
    };
    return {
      formMain: {id: null},
      rules: {
        kuaidi_name: [{required: true, message: this.$t('logisticsCompanyManage.logisticsCompanySave.qsrwlgs'), trigger: 'blur'}],
        type: [{required: true, message: this.$t('logisticsCompanyManage.logisticsCompanySave.qsrbm'), trigger: 'blur'}],
        sort: [
          {required: true, message: this.$t('logisticsCompanyManage.logisticsCompanySave.qsrxh'), trigger: 'blur'},
          { validator: validateSort, trigger: "blur", required: true },
        ],
      },
    }
  },
  //组装模板
  created() {
    this.id = this.$route.params.id;
    if (!isEmpty(this.id)) {
      this.loadData(this.id)
    }
  },

  beforeRouteLeave (to, from, next) {        
    if (to.name == 'logisticsCompanyList' && this.$route.name == 'logisticsCompanyEdit') {
      to.params.dictionaryNum = this.$route.query.dictionaryNum
      to.params.pageSize = this.$route.query.pageSize
    }   
    next();
  },

  methods: {
    async loadData(id) {
      const res = await index({
        api: 'admin.express.index',
        id: id,
      }).then(data => {
        let obj = data.data.data.list[0];
        obj.is_open = obj.is_open === 1;
        this.formMain = data.data.data.list[0];
      });
    },
    //添加/编辑
    async Save(formName) {
      this.$refs[formName].validate(async (valid) => {
        console.log(this.ruleForm);
        if (valid) {
          try {
            if(this.formMain.kuaidi_name.length > 20) {
              this.$message({
                message: this.$t('logisticsCompanyManage.logisticsCompanySave.wlmcb'),
                type: 'error',
                offset: 102
              })
              return
            }
            let text = this.$t('zdata.bjcg');
            if (isEmpty(this.formMain.id)) {
              text = this.$t('zdata.tjcg');
            }
            const res = await index({
              api: 'admin.express.expressSave',
              id: this.formMain.id,
              sort: this.formMain.sort,
              name: this.formMain.kuaidi_name,
              code: this.formMain.type,
              switchse: this.formMain.is_open ? 1 : 2,
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
