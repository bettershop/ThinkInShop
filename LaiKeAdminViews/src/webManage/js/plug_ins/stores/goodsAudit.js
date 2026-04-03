import { getGoodsExamineInfo, goodsExamine } from '@/api/plug_ins/stores'
import { mixinstest } from '@/mixins/index'
import { getStorage } from '@/utils/storage'
import { getButton } from '@/api/layout/information'
import ErrorImg from '@/assets/images/default_picture.png'

export default {
  name: 'goodsAudit',
  mixins: [mixinstest],
  data () {
    return {
      routerList: JSON.parse(sessionStorage.getItem('tabRouter')),
      radio1: this.$t('stores.spsh'),

      inputInfo: {
        goodsName: null,
        storeName: null
      },
      button_list: [],
      tableData: [],
      loading: true,
      menuId: '',
      // 弹框数据
      dialogVisible2: false,
      ruleForm: {
        reason: ''
      },

      rules: {
        reason: [
          {
            required: true,
            message: this.$t('stores.goodsAudit.qsrjj'),
            trigger: 'blur'
          }
        ]
      },

      id: null,
      // table高度
      tableHeight: null
    }
  },

  created () {
    if (this.$route.params.pageSize) {
      this.pagination.page = this.$route.params.dictionaryNum
      this.dictionaryNum = this.$route.params.dictionaryNum
      this.pageSize = this.$route.params.pageSize
    }
    if (this.$route.query.goodsId) {
      this.inputInfo.goodsName = this.$route.query.no
      getGoodsExamineInfo({
        api: 'mch.Admin.Mch.GetGoodsExamineInfo',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        goodsId: this.$route.query.goodsId
      }).then(res => {
        console.log(res)
        this.current_num = 10
        this.total = res.data.data.total
        this.tableData = res.data.data.list
        this.loading = false
        if (this.total < this.current_num) {
          this.current_num = this.total
        }
      })
    } else {
      this.getGoodsExamineInfos()
      this.getButtons()
    }
    // this.getGoodsExamineInfos()
  },

  mounted () {
    this.$nextTick(function () {
      this.getHeight()
    })
    window.addEventListener('resize', this.getHeight(), false)
  },

  watch: {
    '$route.query.no': {
      handler: function () {
        this.inputInfo.goodsName = this.$route.query.no
        getGoodsExamineInfo({
          api: 'mch.Admin.Mch.GetGoodsExamineInfo',
          pageNo: this.dictionaryNum,
          pageSize: this.pageSize,
          goodsName: this.$route.query.name
        }).then(res => {
          console.log(res)
          this.current_num = 10
          this.total = res.data.data.total
          this.tableData = res.data.data.list
          this.loading = false
          if (this.total < this.current_num) {
            this.current_num = this.total
          }
        })
      },
      deep: true
    }
  },

  methods: {
    // 图片错误处理
    handleErrorImg (e) {
      console.log('图片报错了', e.target.src)
      e.target.src = ErrorImg
    },
    toset () {
      if (getStorage('laike_admin_userInfo').mchId !== 0) {
        this.$router.push('/plug_ins/stores/storeSet')
      } else {
        this.$message({
          type: 'error',
          message: '请添加店铺!',
          offset: 102
        })
        this.$router.push('/mall/fastBoot/index')
      }
    },
    // 获取table高度
    getHeight () {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
    },
    //获取按纽权限
    async getButtons () {
      let route = getStorage('route')
      route.forEach(item => {
        if (item.path == 'stores') {
          item.children.forEach(e => {
            if (e.path == 'goodsAudit') {
              return (this.menuId = e.id)
            }
          })
        }
      })
      let buttonList = await getButton({
        api: 'saas.role.getButton',
        menuId: this.menuId
      })
      this.button_list = buttonList.data.data
      console.log(this.button_list, '获取按纽权限')
    },
    async getGoodsExamineInfos () {
      const res = await getGoodsExamineInfo({
        api: 'mch.Admin.Mch.GetGoodsExamineInfo',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        mchName: this.inputInfo.storeName,
        goodsName: this.inputInfo.goodsName
      })
      this.current_num = 10
      this.total = res.data.data.total
      this.tableData = res.data.data.list
      this.loading = false
      if (this.total < this.current_num) {
        this.current_num = this.total
      }
      console.log(res)
    },

    // 重置
    reset () {
      this.inputInfo.goodsName = null
      this.inputInfo.storeName = null
    },

    // 查询
    demand () {
      this.currpage = 1
      this.current_num = 10
      this.showPagebox = false
      this.loading = true
      this.dictionaryNum = 1
      this.getGoodsExamineInfos().then(() => {
        this.loading = false
        if (this.tableData.length > 5) {
          this.showPagebox = true
        }
      })
    },

    // 查看
    View (value) {
      this.$router.push({
        path: '/plug_ins/stores/viewGoods',
        query: {
          id: value.id,
          dictionaryNum: this.dictionaryNum,
          pageSize: this.pageSize
        }
      })
    },

    // 通过
    Through (value) {
      this.$confirm(this.$t('stores.goodsAudit.qrtg'), this.$t('stores.ts'), {
        confirmButtonText: this.$t('stores.okk'),
        cancelButtonText: this.$t('stores.ccel'),
        type: 'warning'
      })
        .then(() => {
          goodsExamine({
            api: 'mch.Admin.Mch.GoodsExamine',
            goodsId: value.id,
            status: 1
          }).then(res => {
            console.log(res)
            if (res.data.code == '200') {
              this.getGoodsExamineInfos()
              this.$message({
                type: 'success',
                message: this.$t('stores.tcgc'),
                offset: 102
              })
            }
          })
        })
        .catch(() => {
          // this.$message({
          //   type: 'info',
          //   message: '已取消删除',
          //   offset: 102
          // });
        })
    },

    // 拒绝
    Refused (value) {},

    //选择一页多少条
    handleSizeChange (e) {
      this.loading = true
      console.log(e)
      this.pageSize = e
      this.getGoodsExamineInfos().then(() => {
        this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1
        this.current_num =
          this.tableData.length === this.pageSize
            ? this.dictionaryNum * this.pageSize
            : this.total
        this.loading = false
      })
    },

    //点击上一页，下一页
    handleCurrentChange (e) {
      this.loading = true
      this.dictionaryNum = e
      this.currpage = (e - 1) * this.pageSize + 1
      this.getGoodsExamineInfos().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total
        this.loading = false
      })
    },

    // 弹框方法
    dialogShow2 (value, toggle) {
      console.log(value)
      this.dialogVisible2 = true
      this.id = value.id
      this.ruleForm.reason = ''
    },

    handleClose2 (done) {
      this.dialogVisible2 = false
      this.id = null
      this.$refs['ruleForm'].clearValidate()
    },

    submitForm2 (formName) {
      this.$refs[formName].validate(async valid => {
        console.log(this.ruleForm)
        if (valid) {
          try {
            if (this.ruleForm.reason.length > 100) {
              this.$message({
                type: 'error',
                message: this.$t('stores.goodsAudit.jjlyw'),
                offset: 102
              })
              return
            }
            goodsExamine({
              api: 'mch.Admin.Mch.GoodsExamine',
              goodsId: this.id,
              status: 0,
              text: this.ruleForm.reason
            }).then(res => {
              console.log(res)
              if (res.data.code == '200') {
                this.getGoodsExamineInfos()
                this.$message({
                  type: 'success',
                  message: this.$t('stores.jjcg'),
                  offset: 102
                })
                this.handleClose2()
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
          console.log('error submit!!')
          return false
        }
      })
    }
  }
}
