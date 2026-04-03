import {index, del, save} from '@/api/Platform/logisticsCompanyManage'
import pageData from '@/api/constant/page'
import { mixinstest } from '@/mixins/index'

export default {
  mixins: [mixinstest],
  data () {
    return {
      isSwitchFlag:false,
      default_lang_code:null,
      languages:[],
      page: pageData.data(),
      // table高度
      tableHeight: null,
      haveDefaultCurrency:false,
      defaultCurrency:true,
      switchFlag: false,
      isNone:'',
      ruleForm: {
        default_lang_code: ''
      },
      rules: {
        default_lang_code: [
          {
            required: true,
            message: this.$t('qxzyz'),
            trigger: 'blur'
          }
        ]
      }
    }
  },

  created () {
    this.index();
    this.loadData();
  },

  methods: {

    async loadData() {
      const res = await index({
        api: 'admin.currencyStore.currencyStoreList',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize
        // keyWord: this.page.inputInfo.name,
        // sortType: this.sortType
      });
      this.total = res.data.data.total
      this.page.tableData = res.data.data.list
      this.isSwitchFlag = this.page.tableData && this.page.tableData.some(item => {
        // 额外处理：确保 item.default_currency 不为 null/undefined，避免判断出错
        return item.default_currency === 1;
      });
      this.haveDefaultCurrency = res.data.data.haveDefaultCurrency != null
      this.page.loading = false
      if (res.data.data.total < 10) {
        this.current_num = this.total
      }
      if(res.data.data.total < 5) {
        this.page.showPagebox = false
      }
      if (this.total < this.current_num) {
        this.current_num = this.total
      }
    },

    async index() {
      try {
        const result =  await this.LaiKeCommon.select({
          api:"admin.system.storeIntenationSetting"
        });

        this.getLanguage().then(()=>{
          let lang_code = result.data?.data?.list?.default_lang_code;
          console.log();
          console.log("lang_code:"+lang_code);
          console.log();
          if(lang_code){
            this.ruleForm.default_lang_code =  "" + lang_code ;
          }
          this.isNone =this.ruleForm.default_lang_code
        });

      } catch (error) {
        console.error('获取语种列表失败:', error);
      }
    },

    async getLanguage() {
      try {
        const result = await this.LaiKeCommon.getLanguages();
        this.languages = result.data.data;
      } catch (error) {
        console.error('获取语种列表失败:', error);
      }
    },

    submitForm () {
      this.LaiKeCommon.add({
        api: 'admin.system.addOrUpdateStoreIntenationSetting',
        default_lang_code: this.ruleForm.default_lang_code
      }).then(res => {
        if (res.data.code == '200') {
          this.$message({
            message: this.$t('zdata.baccg'),
            type: 'success',
            offset: 102
          })
          this.index()
        }
      })

    },

    // 获取cookiet
    getCookit(){
      let myCookie = document.cookie.split(';').map(item=>{
        let arr = item.split('=')
        return {name:arr[0],value:arr[1]}
      })
      let strCookit = ''
      myCookie.forEach(item=>{
        if(item.name.indexOf('language')!==-1){
          strCookit = item.value
        }
      })
      return strCookit
    },
    getHeight(){
      this.tableHeight = this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
    },
    //选择一页多少条
    handleSizeChange(e) {
      this.page.loading = true
      this.pageSize = e
      this.loadData().then(() => {
        this.currpage = ((this.dictionaryNum - 1) * this.pageSize) + 1
        this.current_num = this.page.tableData.length === this.pageSize ? this.dictionaryNum * this.pageSize : this.total
        this.page.loading = false
      })
    },

    //点击上一页，下一页
    handleCurrentChange(e) {
      this.page.loading = true
      this.dictionaryNum = e
      this.currpage = ((e - 1) * this.pageSize) + 1
      this.loadData().then(() => {
        this.current_num = this.page.tableData.length === this.pageSize ? e * this.pageSize : this.total
        this.page.loading = false
      })

    },
    handleSelectionChange(val) {
      if(val.length==0){
        this.is_disabled = true
      }else{
        this.is_disabled = false
      }
      this.idList = val.map(item => {
        return item.id
      })
    },
    //开关
    async Switch(item) {

      if(this.haveDefaultCurrency)
      {
        this.$message({
          type: 'error',
          message: this.$t('internationalsetting.yszmrhb'),
          offset: 100,           // 距离顶部的位置（根据需要调整）
          zIndex: 99999,       // 确保z-index足够高
        })
        return
      }

      this.$confirm(this.$t('internationalsetting.shfzwmrhb'), this.$t('internationalsetting.ts'), {
        confirmButtonText: this.$t('internationalsetting.okk'),
        cancelButtonText: this.$t('internationalsetting.ccel'),
        type: 'warning'
      }).then(() => {
        const res = index({
          api: 'admin.currencyStore.setDefaultCurrency',
          currency_id: item.currency_id,
          store_id: item.store_id
        }).then(data => {
          if(data.data.code == '200') {
            this.$message({
              type: 'success',
              message: this.$t('zdata.czcg'),
              offset:102,
            })
            console.log(data)
            let currency_symbol = data?.data?.data?.currency_symbol
            sessionStorage.setItem('currency_symbol_'+item.store_id, currency_symbol);
            console.log("currency_symbol"+currency_symbol);
            this.loadData();
          }
        });
      })

    },

    //设置汇率
    async settingRates(obj) {
      await this.LaiKeCommon.add({
        api: 'admin.currencyStore.saveOrEditCurrencyStore',
        currency_id: obj.currency_id ,
        store_id: obj.store_id ,
        exchange_rate: obj.exchange_rate
      }).then(res => {
        if(res.data.code == '200') {
          this.$message({
            type: 'success',
            message: this.$t('zdata.czcg'),
            offset:102,
          })
          this.loadData();
        }
      })
    },

    handleInput(row) {
      let value = row.exchange_rate;
      // 去除非数字和非小数点字符
      value = value.replace(/[^\d.]/g, '');

      // 处理输入只有小数点的情况
      if (value === '.') {
        value = '0.';
      }

      // 确保只有一个小数点
      const dotIndex = value.indexOf('.');
      if (dotIndex!== -1) {
        const beforeDot = value.slice(0, dotIndex);
        const afterDot = value.slice(dotIndex + 1).replace(/\./g, '');
        value = beforeDot + '.' + afterDot;
      }

      // 限制小数点后最多四位
      const parts = value.split('.');
      if (parts.length === 2) {
        parts[1] = parts[1].slice(0, 4);
        value = parts.join('.');
      }

      // 去除开头多余的 0，但保留 0. 这种情况
      if (value.startsWith('0') && value.length > 1 &&!value.startsWith('0.')) {
        // 这里修改为只保留一个 0
        value = value.replace(/^0+/, '0');
      }

      // 如果处理后为空字符串，默认设为 0
      if (value === '') {
        value = '0';
      }

      row.exchange_rate = value;
    },

    //排序
    sortChange(obj) {
      console.log('sort',obj);
      // this.sortType = 0;
      if (obj.order === 'descending') {
        this.sortType = 1;
      }
      if (obj.order === 'ascending') {
        this.sortType = 0;
      }
    },
    // 删除
    Delete(row) {
      let currency_id = row.currency_id;
      this.$confirm(this.$t('internationalsetting.qrsc'), this.$t('internationalsetting.ts'), {
        confirmButtonText: this.$t('internationalsetting.okk'),
        cancelButtonText: this.$t('internationalsetting.ccel'),
        type: 'warning'
      }).then(() => {
        del({
          api: 'admin.currencyStore.delCurrencyStore',
          currency_id: currency_id
        }).then(res => {
          if(res.data.code == '200') {
            this.loadData();
            this.$message({
              type: 'success',
              message: this.$t('zdata.sccg'),
              offset:102,
            })
          }
        })
      }).catch((err) => {
        // this.$message({
        //   type: 'info',
        //   message: '已取消删除'
        // })
      })
    }

  }
}
