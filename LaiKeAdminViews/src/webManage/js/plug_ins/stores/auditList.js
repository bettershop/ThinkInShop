import { getMchExamineInfo, examineMch } from '@/api/plug_ins/stores'
import { exports } from '@/api/export/index'
import { mixinstest } from '@/mixins/index'
import { getStorage } from '@/utils/storage'
import { getButton } from '@/api/layout/information'
import ErrorImg from '@/assets/images/default_picture.png'

export default {
  name: 'auditList',
  mixins: [mixinstest],
  data() {
    return {
      routerList: JSON.parse(sessionStorage.getItem('tabRouter')),
      radio1: this.$t('stores.dpsh'),
      button_list: [],
      statusList: [
        {
          value: '0',
          label: this.$t('stores.auditList.dsh')
        },
        {
          value: '2',
          label: this.$t('stores.auditList.shbtg')
        }
      ], // 审核状态

      inputInfo: {
        status: null,
        storeName: null
      },

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
            message: this.$t('stores.auditList.qsrjj'),
            trigger: 'blur'
          }
        ]
      },
      id: null,
      // table高度
      tableHeight: null,

      // 导出弹框数据
      dialogVisible: false
    }
  },

  created() {
    if (this.$route.params.pageSize) {
      this.pagination.page = this.$route.params.dictionaryNum
      this.dictionaryNum = this.$route.params.dictionaryNum
      this.pageSize = this.$route.params.pageSize
    }
    this.$router.currentRoute.matched[2].meta.title = '店铺审核'

    this.getMchExamineInfos()
    this.getButtons()
  },

  mounted() {
    this.$nextTick(function () {
      this.getHeight()
    })
    window.addEventListener('resize', this.getHeight(), false)
  },

  methods: {
    // 图片错误处理
    handleErrorImg(e) {
      console.log('图片报错了', e.target.src)
      e.target.src = ErrorImg
    },
    toset() {
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
    getHeight() {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
    },
    //获取按纽权限
    async getButtons() {
      let route = getStorage('route')
      route.forEach(item => {
        if (item.path == 'stores') {
          item.children.forEach(e => {
            if (e.path == 'auditList') {
              return (this.menuId = e.id)
            }
          })
        }
      })
      console.log(this.menuId)
      let buttonList = await getButton({
        api: 'saas.role.getButton',
        menuId: this.menuId
      })
      this.button_list = buttonList.data.data
      this.button_list = buttonList.data.data.map(item => {
        return item.title
      })
    },
    async getMchExamineInfos() {
      const res = await getMchExamineInfo({
        api: 'mch.Admin.Mch.GetMchExamineInfo',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        reviewStatus: this.inputInfo.status,
        name: this.inputInfo.storeName
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
    reset() {
      this.inputInfo.status = null
      this.inputInfo.storeName = null
    },

    // 查询
    demand() {
      this.currpage = 1
      this.current_num = 10
      this.showPagebox = false
      this.loading = true
      this.dictionaryNum = 1
      this.getMchExamineInfos().then(() => {
        this.loading = false
        if (this.tableData.length > 5) {
          this.showPagebox = true
        }
      })
    },

    // 查看
    View(value) {
      this.$router.push({
        path: '/plug_ins/stores/viewStore',
        query: {
          id: value.id,
          status: value.examineName,
          dictionaryNum: this.dictionaryNum,
          pageSize: this.pageSize,
          storeAudit: true
        }
      })
    },

    // 通过
    Through(value) {
      this.$confirm(this.$t('stores.auditList.qrsh'), this.$t('stores.ts'), {
        confirmButtonText: this.$t('stores.okk'),
        cancelButtonText: this.$t('stores.ccel'),
        type: 'warning'
      })
        .then(() => {
          examineMch({
            api: 'mch.Admin.Mch.ExamineMch',
            reviewStatus: 1,
            mchId: value.id
          }).then(res => {
            console.log(res)
            if (res.data.code == '200') {
              this.getMchExamineInfos()
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
    Refused(value) { },

    //选择一页多少条
    handleSizeChange(e) {
      this.loading = true
      console.log(e)
      // this.current_num = e
      this.pageSize = e
      this.getMchExamineInfos().then(() => {
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
      this.getMchExamineInfos().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total
        this.loading = false
      })
    },

    // 弹框方法
    dialogShow2(value, toggle) {
      console.log(value)
      this.dialogVisible2 = true
      this.id = value.id
      this.ruleForm.reason = ''
    },

    handleClose2(done) {
      this.dialogVisible2 = false
      this.id = null
      this.$refs['ruleForm'].clearValidate()
    },

    submitForm2(formName) {
      this.$refs[formName].validate(async valid => {
        console.log(this.ruleForm)
        if (valid) {
          try {
            examineMch({
              api: 'mch.Admin.Mch.ExamineMch',
              reviewStatus: 2,
              mchId: this.id,
              text: this.ruleForm.reason
            }).then(res => {
              console.log(res)
              if (res.data.code == '200') {
                this.getMchExamineInfos()
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
          api: 'mch.Admin.Mch.GetMchExamineInfo',
          pageNo: this.dictionaryNum,
          pageSize: this.pageSize,
          exportType: 1,
          reviewStatus: this.inputInfo.status,
          name: this.inputInfo.storeName
        },
        'MchExamineInfo'
      )
    },

    async exportAll() {
      exports(
        {
          api: 'mch.Admin.Mch.GetMchExamineInfo',
          pageNo: 1,
          pageSize: 999999,
          exportType: 1,
          reviewStatus: this.inputInfo.status,
          name: this.inputInfo.storeName
        },
        'MchExamineInfo'
      )
    },

    async exportQuery() {
      exports(
        {
          api: 'mch.Admin.Mch.GetMchExamineInfo',
          pageNo: 1,
          pageSize: this.total,
          exportType: 1,
          reviewStatus: this.inputInfo.status,
          name: this.inputInfo.storeName
        },
        'MchExamineInfo'
      )
    }
  }
}
