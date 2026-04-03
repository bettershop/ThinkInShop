import { getWithdrawalInfo } from '@/api/plug_ins/stores'
import { exports } from '@/api/export/index'
import { getStorage } from '@/utils/storage'
import { mixinstest } from '@/mixins/index'
import ErrorImg from '@/assets/images/default_picture.png'

export default {
  name: 'withdrawalRecord',
  mixins: [mixinstest],
  data () {
    return {
      routerList: JSON.parse(sessionStorage.getItem('tabRouter')),
      radio1: this.$t('stores.txjl'),

      inputInfo: {
        storeName: null,
        phone: null,
        date: null,
        txType: null,
        status: null,
        dkstatus: null,
      },
      sourceList: [
        {
          value: "2",
          label: this.$t('stores.withdrawalRecord.wxlq'),
        },
        {
          value: "1",
          label: this.$t('stores.withdrawalRecord.yhk'),
        },
      ], // 提现方式
      statusList: [
        {
          value: 1,
          label: this.$t('stores.withdrawalRecord.shytg')
        },
        {
          value: 2,
          label: this.$t('stores.withdrawalRecord.yjj')
        }
      ], // 审核状态
      dkstatusList: [
        {
          value: 1,
          label: this.$t('stores.withdrawalRecord.jxz')
        },
        {
          value: 2,
          label: this.$t('stores.withdrawalRecord.ywc')
        },
        {
          value: 3,
          label: this.$t('stores.withdrawalRecord.txsb')
        }
      ], // 提现状态
      tableData: [],
      loading: true,
      // table高度
      tableHeight: null,

      // 导出弹框数据
      dialogVisible: false
    }
  },

  created () {
    this.getWithdrawalInfos()
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
    // 获取table高度
    getHeight () {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
    },
    async getWithdrawalInfos () {
      const res = await getWithdrawalInfo({
        api: 'mch.Admin.Mch.GetWithdrawalInfo',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        userNameAndPhone: this.inputInfo.storeName,
        phone: this.inputInfo.phone,
        status: this.inputInfo.status,
        startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
        endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
        withdrawStatus: this.inputInfo.txType,
        status: this.inputInfo.status,
        wxStatus:this.inputInfo.dkstatus,
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
      this.inputInfo.status = null
      this.inputInfo.dkstatus = null
    },

    // 查询
    demand () {
      this.currpage = 1
      this.current_num = 10
      this.showPagebox = false
      this.loading = true
      this.dictionaryNum = 1
      this.getWithdrawalInfos().then(() => {
        this.loading = false
        if (this.tableData.length > 5) {
          this.showPagebox = true
        }
      })
    },

    //选择一页多少条
    handleSizeChange (e) {
      this.loading = true
      console.log(e)
      // this.current_num = e
      this.pageSize = e
      this.getWithdrawalInfos().then(() => {
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
      this.getWithdrawalInfos().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total
        this.loading = false
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
          api: 'mch.Admin.Mch.GetWithdrawalInfo',
          pageNo: this.dictionaryNum,
          pageSize: this.pageSize,
          exportType: 1,
          userNameAndPhone: this.inputInfo.storeName,
          phone: this.inputInfo.phone,
          status: this.inputInfo.status,
          startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
          endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
          withdrawStatus: this.inputInfo.txType,
          status: this.inputInfo.status,
          wxStatus:this.inputInfo.dkstatus,
        },
        'WithdrawalInfo'
      )
    },

    async exportAll () {
      exports(
        {
          api: 'mch.Admin.Mch.GetWithdrawalInfo',
          pageNo: 1,
          pageSize: 999999,
          exportType: 1,
        },
        'WithdrawalInfo'
      )
    },

    async exportQuery () {
      exports(
        {
          api: 'mch.Admin.Mch.GetWithdrawalInfo',
          pageNo: 1,
          pageSize: this.total,
          exportType: 1,
          userNameAndPhone: this.inputInfo.storeName,
          phone: this.inputInfo.phone,
          status: this.inputInfo.status,
          startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
          endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
          withdrawStatus: this.inputInfo.txType,
          status: this.inputInfo.status,
          wxStatus:this.inputInfo.dkstatus,
        },
        'WithdrawalInfo'
      )
    }
  }
}
