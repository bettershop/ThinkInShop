import { getStockInfo, addStock, batchAddStock } from '@/api/goods/inventoryManagement'
import { exports } from '@/api/export/index'
import { mixinstest } from '@/mixins/index'
import { getStorage, setStorage } from '@/utils/storage'
import ErrorImg from '@/assets/images/default_picture.png'

export default {
  name: 'inventoryList',
  mixins: [mixinstest],
  data() {
    return {
      laikeCurrencySymbol:'￥',
      languages: null,
      lang_code: null,
      radio1: this.$t('inventoryManagement.cklb'),
      inputInfo: {
        storeName: null,
        lang_code: null,
        goodsName: null
      },
      inventoryArr: [],
      tableData: [],
      loading: true,

      // 添加库存
      dialogVisible1: false,
      // 弹框数据
      dialogVisible2: false,
      ruleForm2: {
        addNum: '',
        total_num: null,
        num: null
      },
      rules2: {
        addNum: [
          {
            required: true,
            message: this.$t('inventoryManagement.qsrzjckl'),
            trigger: 'blur'
          }
        ]
      },
      ruleForm: {
        addStockVal: ''
      },
      // table高度
      tableHeight: null,
      // 导出弹框数据
      dialogVisible: false,
      menuId: '',
      rules: {
        addStockVal: [
          { required: true, message: this.$t('inventoryManagement.qsrspkcsl'), trigger: 'blur' },
          { pattern: /^(-\d+|\d+)$/, message: this.$t('inventoryManagement.qsrzqdkcsl'), trigger: 'blur' },
        ]
      }
    }
  },

  created() {
    this.laikeCurrencySymbol = this.LaiKeCommon.getDefaultCurrencySymbol();
    if (this.$route.query.productTitle) {
      this.inputInfo.goodsName = this.$route.query.productTitle
      this.inputInfo.lang_code = this.$route.query.lang_code
    } else {
      this.inputInfo.lang_code = this.LaiKeCommon.getUserLangVal();
    }
    this.getStockInfos()
    this.getLanguage();
  },

  mounted() {
    this.$nextTick(function () {
      this.getHeight()
    })
    window.addEventListener('resize', this.getHeight(), false)
  },

  methods: {

    async getLanguage() {
      try {
        const result = await this.LaiKeCommon.getLanguages();
        this.languages = result.data.data;
      } catch (error) {
        console.error('获取语种列表失败:', error);
      }
    },

    resetForm() {
      this.$nextTick(() => {
        this.$refs['ruleForm'].resetFields();
        this.ruleForm.addStockVal = ''
        this.dialogVisible1 = false

      })
    },
    submitForm() {
      this.$refs['ruleForm'].validate((valid) => {
        if (valid) {
          batchAddStock({
            api: 'admin.Goods.BatchAddStock',
            ids: JSON.stringify(this.inventoryArr.map(v => v.id)),
            addNum: this.ruleForm.addStockVal
          }).then(res => {
            if (res.data.code == 200) {
              this.succesMsg(this.$t('group.czcg'))
              this.getStockInfos()
            } else {
              this.errorMsg(res.data.data || res.data.message)
            }
          }).finally(() => {
            this.ruleForm.addStockVal = ''
            this.dialogVisible1 = false
          })
        } else {
          return false;
        }
      });
    },
    addInventory() {
      if (this.inventoryArr && this.inventoryArr.length == 0) {
        this.warnMsg(this.$t('inventoryManagement.qszzsyijsp'))
        return
      }
      this.dialogVisible1 = true

    },
    handleSelectionChange(value) {
      this.inventoryArr = value
    },

    // 图片错误处理
    handleErrorImg(e) {
      console.log('图片报错了', e.target.src);
      e.target.src = ErrorImg
    },
    // 获取table高度
    getHeight() {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
      console.log(this.$refs.tableFather.clientHeight)
    },
    async getStockInfos() {
      const res = await getStockInfo({
        api: 'admin.goods.getStockInfo',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        mchName: this.inputInfo.storeName,
        lang_code: this.inputInfo.lang_code,
        productTitle: this.inputInfo.goodsName
      })
      this.total = res.data.data.total
      this.tableData = res.data.data.list
      this.loading = false
      if (this.total < this.current_num) {
        this.current_num = this.total
      }
      console.log(res)
    },

    // 重置
    reset() {
      this.inputInfo.storeName = null
      this.inputInfo.goodsName = null
    },

    // 查询
    demand() {
      // this.currpage = 1
      // this.current_num = 10
      this.showPagebox = false
      this.loading = true
      this.dictionaryNum = 1
      this.getStockInfos().then(() => {
        this.loading = false
        if (this.tableData.length > 5) {
          this.showPagebox = true
        }
      })
    },

    enters() { },

    // 库存详情
    inventorydetails(value) {
      this.$router.push({
        path: '/goods/inventoryManagement/InventoryDetails',
        query: {
          id: value.pid,
          attrId: value.id
        }
      })
    },

    //选择一页多少条
    handleSizeChange(e) {
      this.loading = true
      console.log(e)
      // this.current_num = e
      this.pageSize = e
      this.getStockInfos().then(() => {
        this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1
        this.current_num =
          this.tableData.length === this.pageSize
            ? this.dictionaryNum * this.pageSize
            : this.total
        this.loading = false
      })
    },

    //点击上一页，下一页
    handleCurrentChange(e) {
      this.loading = true
      this.dictionaryNum = e
      this.currpage = (e - 1) * this.pageSize + 1
      this.getStockInfos().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total
        this.loading = false
      })
    },

    // 弹框方法
    dialogShow2(value) {
      console.log(value)
      this.dialogVisible2 = true
      this.ruleForm2.total_num = value.total_num
      this.ruleForm2.num = value.num
      this.id = value.id
      this.pid = value.pid
    },

    handleClose2(done) {
      this.dialogVisible2 = false
      this.$refs['ruleForm2'].clearValidate()
      this.ruleForm2.addNum = ''
      this.ruleForm2.total_num = null
      this.ruleForm2.num = null
    },

    // 添加库存
    determine(formName2) {
      this.$refs[formName2].validate(async valid => {
        if (valid) {
          try {
            if (Number(this.ruleForm2.addNum) < 0) {
              var addNum = this.ruleForm2.addNum.slice(1)
              if (Number(addNum) > Number(this.ruleForm2.num)) {
                this.$message({
                  message: this.$t('inventoryManagement.jqckbnxy'),
                  type: 'error',
                  offset: 102
                })
                return
              }
            }
            addStock({
              api: 'admin.goods.addStock',
              id: this.id,
              pid: this.pid,
              addNum: this.ruleForm2.addNum
            }).then(res => {
              console.log(res)
              if (res.data.code == '200') {
                this.$message({
                  message: this.$t('zdata.tjcg'),
                  type: 'success',
                  offset: 102
                })
                this.dialogVisible2 = false
                this.getStockInfos()
                this.handleClose2()
              }
            })
          } catch (error) {
            this.$message({
              message: this.$t('inventoryManagement.qsrzjckl'),
              type: 'error',
              offset: 102
            })
          }
        }
      })
    },

    oninput2(num) {
      var str = num
      str = str.replace(/[^\.\d]/g, '')
      str = str.replace('.', '')

      if (Number(str) === 0 && str != '') {
        return 1
      }

      return str
    },

    // 导出弹框方法
    dialogShow() {
      this.dialogVisible = true
    },

    handleClose(done) {
      this.dialogVisible = false
    },

    async exportPage() {
      exports(
        {
          api: 'admin.goods.getStockInfo',
          pageNo: this.dictionaryNum,
          pageSize: this.pageSize,
          exportType: 1,
          mchName: this.inputInfo.storeName,
          productTitle: this.inputInfo.goodsName
        },
        'Stock'
      )
    },

    async exportAll() {
      exports(
        {
          api: 'admin.goods.getStockInfo',
          pageNo: 1,
          pageSize: 999999,
          exportType: 1,
        },
        'allStock'
      )
    },

    async exportQuery() {
      exports(
        {
          api: 'admin.goods.getStockInfo',
          pageNo: 1,
          pageSize: this.pageSize,
          exportType: 1,
          mchName: this.inputInfo.storeName,
          productTitle: this.inputInfo.goodsName
        },
        'queryStock'
      )
    }
  }
}
