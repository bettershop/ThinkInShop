import { getPartRecord, delPartRecord } from '@/api/plug_ins/group'
import { mixinstest } from '@/mixins/index'
import { exports } from '@/api/export/index'
export default {
  name: 'groupRecord',
  mixins: [mixinstest],
  data () {
    return {
      routerList:JSON.parse(sessionStorage.getItem('tabRouter')),
      radio1: this.$t('group.ctjl'),
      groupState: [
        {
          value: 0,
          label: this.$t('group.participationRecord.ptz')
        },
        {
          value: 1,
          label: this.$t('group.participationRecord.ptcg')
        },
        {
          value: 2,
          label: this.$t('group.participationRecord.ptsb')
        }
      ],
      inputInfo: {
        status: null,
        userName: null,
        teamName: null,
        key: null
      },
      tableData: [],
      loading: true,
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
      const res = await getPartRecord({
        api: 'plugin.group.AdminGroup.JoinRecordList',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        status: this.inputInfo.status,
        userName: this.inputInfo.userName,
        teamName: this.inputInfo.teamName,
        key: this.inputInfo.key
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
      this.inputInfo.userName = null
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
    Delete (row) {
      this.$confirm(
        this.$t('group.participationRecord.qdscsxctjl'),
        this.$t('zdata.ts'),
        {
          confirmButtonText: this.$t('zdata.ok'),
          cancelButtonText: this.$t('zdata.off'),
          type: 'warning'
        }
      )
        .then(() => {
          delPartRecord({
            api: 'plugin.group.AdminGroup.DelJoinRecord',
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
    },

    async exportPage () {
      exports(
        {
          api: 'plugin.group.AdminGroup.JoinRecordList',
          pageNo: this.dictionaryNum,
          pageSize: this.pageSize,
          status: this.inputInfo.status,
          userName: this.inputInfo.userName,
          teamName: this.inputInfo.teamName,
          key: this.inputInfo.key,
          exportType: 1
        },
        'pageorder'
      )
    },

    async exportAll () {
      exports(
        {
          api: 'plugin.group.AdminGroup.JoinRecordList',
          pageNo: this.dictionaryNum,
          pageSize: 99999,
          status: this.inputInfo.status,
          userName: this.inputInfo.userName,
          teamName: this.inputInfo.teamName,
          key: this.inputInfo.key,
          exportType: 1
        },
        'allorder'
      )
    },

    async exportQuery () {
      exports(
        {
          api: 'plugin.group.AdminGroup.JoinRecordList',
          pageNo: this.dictionaryNum,
          pageSize: 99999,
          status: this.inputInfo.status,
          userName: this.inputInfo.userName,
          teamName: this.inputInfo.teamName,
          key: this.inputInfo.key,
          exportType: 1
        },
        'queryorder'
      )
    }
  }
}
