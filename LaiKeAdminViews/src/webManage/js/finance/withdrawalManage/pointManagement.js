import { pointList } from '@/api/finance/withdrawalManage'
import { exports } from '@/api/export/index'
import { mixinstest } from '@/mixins/index'
import { getStorage } from '@/utils/storage'
import { getButton } from '@/api/layout/information'

export default {
  name: 'pointManagement',
  mixins: [mixinstest],
  data () {
    return {
      inputInfo: {
        userName: null,
        date:null,
      },

      tableData: [],
      loading: true,
      // table高度
      tableHeight: null,
      // 导出弹框数据
      dialogVisible: false
    }
  },

  created () {
    this.axios()
  },

  mounted () {
    this.$nextTick(function () {
      this.getHeight()
    })
    window.addEventListener('resize', this.getHeight(), false)
  },

  methods: {
    //初始化积分列表数据
    async axios () {
      const res = await pointList({
        api: 'admin.user.getUserIntegralInfo',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        userName: this.inputInfo.userName,
        startDate: this.inputInfo.date?.[0] ?? '',
        endDate: this.inputInfo.date?.[1] ?? ''
      })
      console.log('res:', res)

      if (res.data.code == 200) {
        this.loading = false
        this.tableData = res.data.data.list
        this.total = res.data.data.total
        console.log('tableData:', this.tableData)
      }
    },
    // 获取table高度
    getHeight () {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
      console.log(this.$refs.tableFather.clientHeight)
    },

    // 重置
    reset () {
      this.inputInfo.userName = null
      this.inputInfo.date = null
    },

    // 查询
    demand () {
      this.currpage = 1
      // this.current_num = 10
      this.showPagebox = false
      this.loading = true
      this.dictionaryNum = 1
      this.axios().then(() => {
        this.loading = false
        if (this.tableData.length > 5) {
          this.showPagebox = true
        }
      })
    },

    View (value) {
      this.$router.push({
        path: '/finance/pointSet/pointDetails',
        query: {
          id: value.user_id
        }
      })
    },

    //选择一页多少条
    handleSizeChange (e) {
      this.loading = true
      // this.current_num = e
      this.pageSize = e
      this.axios().then(() => {
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
      this.axios().then(() => {
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
          api: 'admin.user.getUserIntegralInfo',
          pageNo: this.dictionaryNum,
          pageSize: this.pageSize,
          exportType: 1,
          userName: this.inputInfo.userName,
          startDate: this.inputInfo.date?.[0] ?? '',
          endDate: this.inputInfo.date?.[1] ?? ''
        },
        '积分管理_导出本页'
      )
    },

    async exportAll () {
      exports(
        {
          api: 'admin.user.getUserIntegralInfo',
          pageNo: 1,
          pageSize: 9999,
          exportType: 1
        },
        '积分管理_导出全部'
      )
    },

    async exportQuery () {
      exports(
        {
          api: 'admin.user.getUserIntegralInfo',
          pageNo: 1,
          pageSize: this.total,
          exportType: 1,
          userName: this.inputInfo.userName,
          startDate: this.inputInfo.date?.[0] ?? '',
          endDate: this.inputInfo.date?.[1] ?? ''
        },
        '积分管理_导出查询'
      )
    }
  }
}
