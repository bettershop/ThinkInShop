import { getGroupRecord, delRecord } from '@/api/plug_ins/group'
import { mixinstest } from '@/mixins/index'
export default {
  name: 'groupRecord',
  mixins: [mixinstest],
  data () {
    return {
      routerList:JSON.parse(sessionStorage.getItem('tabRouter')),
      radio1: this.$t('group.ktjl'),
      couponsTypeList: [], // 优惠券类型
      groupState: [
        {
          value: 0,
          label: this.$t('group.groupRecord.ptz')
        },
        {
          value: 1,
          label: this.$t('group.groupRecord.ptcg')
        },
        {
          value: 2,
          label: this.$t('group.groupRecord.ptsb')
        }
      ],
      inputInfo: {
        status: null,
        teamName: null,
        key: null
      },
      tableData: [],
      loading: true,

      // 弹框数据
      tableHeight: null
    }
  },

  created () {
    if (this.$route.params.pageSize) {
      this.pagination.page = this.$route.params.dictionaryNum
      this.dictionaryNum = this.$route.params.dictionaryNum
      this.pageSize = this.$route.params.pageSize
    }
    this.getRecord()
  },

  mounted () {
    this.$nextTick(function () {
      this.getHeight()
    })
    window.addEventListener('resize', this.getHeight(), false)
  },
  methods: {
    // 获取table高度
    getHeight () {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
      console.log(this.$refs.tableFather.clientHeight)
    },
    async getRecord () {
      const res = await getGroupRecord({
        api: 'plugin.group.AdminGroup.OpenRecordList',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        status: this.inputInfo.status,
        teamName: this.inputInfo.teamName,
        key: this.inputInfo.key,
        activityId: this.$route.query.activityId ?? ''
      })
      this.total = res.data.data.total
      this.tableData = res.data.data.list
      this.loading = false
      if (this.total < this.current_num) {
        this.current_num = this.total
      }
    },

    // 重置
    reset () {
      this.inputInfo.status = null
      this.inputInfo.teamName = null
      this.inputInfo.key = null
    },

    // 查询
    demand () {
      this.currpage = 1
      this.current_num = 10
      this.showPagebox = false
      this.loading = true
      this.dictionaryNum = 1
      this.getRecord().then(() => {
        this.loading = false
        if (this.tableData.length > 5) {
          this.showPagebox = true
        }
      })
    },
    // 查看
    View (row) {
      this.$router.push({
        path: '/plug_ins/group/tourDetails',
        query: {
          id: row.id
        }
      })
    },

    Delete (row) {
      this.$confirm(
        this.$t('group.groupRecord.qdscsxktjl'),
        this.$t('zdata.ts'),
        {
          confirmButtonText: this.$t('zdata.ok'),
          cancelButtonText: this.$t('zdata.off'),
          type: 'warning'
        }
      )
        .then(() => {
          delRecord({
            api: 'plugin.group.admin.delOpenRecord',
            id: row.id
          }).then(res => {
            console.log(res)
            if (res.data.code == '200') {
              this.getRecord()
              this.$message({
                type: 'success',
                message: this.$t('group.scgc'),
                offset: 100
              })
            }
          })
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: this.$t('group.yqxsc'),
            offset: 100
          })
        })
    },
    //选择一页多少条
    handleSizeChange (e) {
      this.loading = true
      console.log('e', e)
      // this.current_num = e
      this.pageSize = e
      this.getRecord().then(() => {
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
      this.getRecord().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total
        this.loading = false
      })
    }
  }
}
