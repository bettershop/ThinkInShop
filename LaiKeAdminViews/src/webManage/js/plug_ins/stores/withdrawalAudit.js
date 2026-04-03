import {
  getWithdrawalExamineInfo,
  withdrawalExamine
} from '@/api/plug_ins/stores'
import { exports } from '@/api/export/index'
import { mixinstest } from '@/mixins/index'
import { getStorage } from '@/utils/storage'
import { getButton } from '@/api/layout/information'
import ErrorImg from '@/assets/images/default_picture.png'

export default {
  name: 'withdrawalAudit',
  mixins: [mixinstest],
  data () {
    return {
      routerList: JSON.parse(sessionStorage.getItem('tabRouter')),
      radio1: this.$t('stores.txsh'),

      inputInfo: {
        storeName: null,
        phone: null,
        date: null,
        txType: null,
      },
      sourceList: [
        {
          value: "2",
          label: this.$t('微信零钱'),
        },
        {
          value: "1",
          label: this.$t('银行卡'),
        },
      ], // 提现方式
      button_list: [],
      tableData: [],
      loading: true,

      // 弹框数据
      dialogVisible2: false,
      ruleForm: {
        reason: ''
      },

      rules: {
        reason: [
          {
            required: true,
            message: this.$t('stores.withdrawalAudit.qsrjj'),
            trigger: 'blur'
          }
        ]
      },
      id: null,
      menuId: '',
      // table高度
      tableHeight: null,

      // 导出弹框数据
      dialogVisible: false
    }
  },

  created () {
    if(this.$route.query.billId){
      this.inputInfo.storeName = this.$route.query.billId
    }
    this.getWithdrawalExamineInfos()
    this.getButtons()
  },

  mounted () {
    this.$nextTick(function () {
      this.getHeight()
    })
    window.addEventListener('resize', this.getHeight(), false)
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
    //获取按纽权限
    async getButtons () {
      let route = getStorage('route')
      route.forEach(item => {
        if (item.path == 'stores') {
          item.children.forEach(e => {
            if (e.path == 'withdrawalAudit') {
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
      this.button_list = buttonList.data.data.map(item => {
        return item.title
      })
    },
    // 获取table高度
    getHeight () {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
    },
    async getWithdrawalExamineInfos () {
      const res = await getWithdrawalExamineInfo({
        api: 'mch.Admin.Mch.GetWithdrawalExamineInfo',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        mchNameAndPhone: this.inputInfo.storeName,
        phone: this.inputInfo.phone,
        startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
        endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
        withdrawStatus: this.inputInfo.txType,
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
      this.inputInfo.phone = null
      this.inputInfo.storeName = null
      this.inputInfo.date = null
      this.inputInfo.txType = null
    },

    // 查询
    demand () {
      this.currpage = 1
      this.current_num = 10
      this.showPagebox = false
      this.loading = true
      this.dictionaryNum = 1
      this.getWithdrawalExamineInfos().then(() => {
        this.loading = false
        if (this.tableData.length > 5) {
          this.showPagebox = true
        }
      })
    },

    // 通过
    Through (value) {
      this.$confirm(
        this.$t('stores.withdrawalAudit.qstg'),
        this.$t('stores.ts'),
        {
          confirmButtonText: this.$t('stores.okk'),
          cancelButtonText: this.$t('stores.ccel'),
          type: 'warning'
        }
      )
        .then(() => {
          withdrawalExamine({
            api: 'mch.Admin.Mch.WithdrawalExamine',
            id: value.id,
            stauts: 1
          }).then(res => {
            console.log(res)
            if (res.data.code == '200') {
              this.getWithdrawalExamineInfos()
              this.$message({
                type: 'success',
                message: this.$t('zdata.shcg'),
                offset: 102
              })
            }
          })
        })
        .catch(() => {
          // this.$message({
          //   type: 'info',
          //   message: '已取消',
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
      // this.current_num = e
      this.pageSize = e
      this.getWithdrawalExamineInfos().then(() => {
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
      this.getWithdrawalExamineInfos().then(() => {
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
            if (this.ruleForm.reason.length > 50) {
              this.$message({
                message: this.$t('stores.withdrawalAudit.jjlyw'),
                type: 'error',
                showClose: true
              })
              return
            }
            withdrawalExamine({
              api: 'mch.Admin.Mch.WithdrawalExamine',
              id: this.id,
              stauts: 2,
              text: this.ruleForm.reason
            }).then(res => {
              console.log(res)
              if (res.data.code == '200') {
                this.getWithdrawalExamineInfos()
                this.$message({
                  type: 'success',
                  message: this.$t('zdata.shcg'),
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
    },

    // 导出弹框方法
    dialogShow () {
      this.dialogVisible = true
    },

    handleClose (done) {
      this.dialogVisible = false
    },

    async exportPage () {
      exports(
        {
          api: 'mch.Admin.Mch.GetWithdrawalExamineInfo',
          pageNo: this.dictionaryNum,
          pageSize: this.pageSize,
          exportType: 1,
          mchNameAndPhone: this.inputInfo.storeName,
          phone: this.inputInfo.phone,
          startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
          endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
          withdrawStatus: this.inputInfo.txType,
        },
        'WithdrawalExamineInfo'
      )
    },

    async exportAll () {
      exports(
        {
          api: 'mch.Admin.Mch.GetWithdrawalExamineInfo',
          pageNo: 1,
          pageSize: this.total,
          exportType: 1,
        },
        'WithdrawalExamineInfo'
      )
    },

    async exportQuery () {
      exports(
        {
          api: 'mch.Admin.Mch.GetWithdrawalExamineInfo',
          pageNo: 1,
          pageSize: this.total,
          exportType: 1,
          mchNameAndPhone: this.inputInfo.storeName,
          phone: this.inputInfo.phone,
          startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
          endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
          withdrawStatus: this.inputInfo.txType,
        },
        'WithdrawalExamineInfo'
      )
    }
  }
}
