import {
  getTiebaList,
  getSwitch,
  startGroup,
  operation
} from '@/api/plug_ins/tieba'
import { mixinstest } from '@/mixins/index'

export default {
  name: 'groupGoods',
  mixins: [mixinstest],
  data() {
    return {
      routerList: JSON.parse(sessionStorage.getItem('tabRouter')),
      radio1: this.$t('tieaba.tbgl'),
      groupState: [
        {
          value: 0,
          label: this.$t('group.groupGoods.wks')
        },
        {
          value: 1,
          label: this.$t('group.groupGoods.jxz')
        },
        {
          value: 2,
          label: this.$t('group.groupGoods.yjs')
        },

      ],
      inputInfo: {
        status: null,
        name: null,
        cid: [],
        review: '',

      },
      tableData: [],
      loading: true,
      // table高度
      tableHeight: null,
      changeCouList: [],
      laike_admin_uaerInfo: '',
      dialogVisible4: false,
      itemInfo: {},
      classList: []
    }
  },

  created() {
    if (this.$route.params.pageSize) {
      this.pagination.page = this.$route.params.dictionaryNum
      this.dictionaryNum = this.$route.params.dictionaryNum
      this.pageSize = this.$route.params.pageSize
    }
    this.getList()
  },

  mounted() {
    this.$nextTick(function () {
      this.getHeight()
    })
    window.addEventListener('resize', this.getHeight(), false)
  },

  methods: {

    // 查看
    viewDetails(row) {
      operation({
        api: 'plugin.bbs.Adminbbs.viewTieba',
        id: row.id
      }).then(res => {
        console.log(res)
        if (res.data.code == '200') {
          this.dialogVisible4 = true
          this.itemInfo = res.data.data.list

        } else {
          this.$message({
            type: 'error',
            message: res.data.msg,
            offset: 100
          })
        }
      })
    },
    // 获取table高度
    getHeight() {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
      console.log(this.$refs.tableFather.clientHeight)
    },
    // 种草管理列表获取
    async getList() {
      const res = await getTiebaList({
        api: 'plugin.bbs.Adminbbs.list',
        page: this.dictionaryNum,
        pagesize: this.pageSize,
        keywords: this.inputInfo.keywords,
        status: this.inputInfo.status,
        cid: this.inputInfo.cid[this.inputInfo.cid.length - 1],
        review: this.inputInfo.review
      })

      if (res.data.code == 200) {
        this.total = res.data.data.total
        this.tableData = res.data.data.list
        this.loading = false
        if (this.total < this.current_num) {
          this.current_num = this.total
        }
      }


    },
    changeProvinceCity(e) {
      console.log(e)
    },
    // 重置
    reset() {
      this.inputInfo.status = null
      this.inputInfo.keywords = null
      this.inputInfo.cid = []
      this.inputInfo.review = ''
    },

    // 查询
    demand() {
      this.currpage = 1
      this.current_num = 10
      this.loading = true
      this.dictionaryNum = 1
      this.getList().then(() => {
        this.loading = false
      })
    },

    // 是否显示
    switchs(row) {
      getSwitch({
        api: 'plugin.bbs.Adminbbs.underReview',
        id: row.id
      }).then(res => {
        if (res.data.code == '200') {
          this.getList()
          this.$message({
            type: 'success',
            message: this.$t('group.czcg'),
            offset: 100
          })
        }
      })
    },



    bulkDel() {
      if (this.changeCouList.length == 0) {
        this.$message.warning(this.$t('group.groupGoods.zsxzytsj'))
        return
      }
      var arrList = []
      var flxList = []
      this.changeCouList.forEach(item => {
        flxList.push(item.id)
      })
      var arrList = flxList.join(',')
      this.$confirm(this.$t('group.groupGoods.qrsc'), this.$t('zdata.ts'), {
        confirmButtonText: this.$t('zdata.ok'),
        cancelButtonText: this.$t('zdata.off'),
        type: 'warning'
      })
        .then(() => {
          operation({
            api: 'plugin.group.AdminGroup.DelGroup',
            ids: arrList
          })
            .then(res => {
              if (res.data.code == '200') {
                this.getList()
                this.$message({
                  type: 'success',
                  message: this.$t('group.scgc'),
                  offset: 100
                })
              }
            })
            .catch(error => {
              // 请求失败
              this.$message({
                message: error.message,
                type: 'warning'
              })
            })
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: this.$t('group.yqxsc')
          })
        })
    },
    Delete(row) {
      this.$confirm('确认要删除吗?', this.$t('zdata.ts'), {
        confirmButtonText: this.$t('zdata.ok'),
        cancelButtonText: this.$t('zdata.off'),
        type: 'warning'
      })
        .then(() => {
          operation({
            api: 'plugin.bbs.Adminbbs.delTieba',
            id: row.id
          }).then(res => {
            console.log(res)
            if (res.data.code == '200') {
              this.$message({
                type: 'success',
                message: this.$t('group.scgc'),
                offset: 100
              })
              this.getList()
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
    end(row) {
      this.$confirm(
        this.$t('group.groupGoods.qrtqjssxpt'),
        this.$t('zdata.ts'),
        {
          confirmButtonText: this.$t('zdata.ok'),
          cancelButtonText: this.$t('zdata.off'),
          type: 'warning'
        }
      )
        .then(() => {
          startGroup({
            api: 'plugin.group.AdminGroup.StartGroup',
            id: row.id,
            type: 0
          }).then(res => {
            console.log(res)
            if (res.data.code == '200') {
              this.getList()
              this.$message({
                type: 'success',
                message: this.$t('group.groupGoods.szcg'),
                offset: 100
              })
            }
          })
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: this.$t('group.groupGoods.yqxsz'),
            offset: 100
          })
        })
    },
    start(row) {
      this.$confirm(
        this.$t('group.groupGoods.qrtqkssxpt'),
        this.$t('zdata.ts'),
        {
          confirmButtonText: this.$t('zdata.ok'),
          cancelButtonText: this.$t('zdata.off'),
          type: 'warning'
        }
      )
        .then(() => {
          startGroup({
            api: 'plugin.group.AdminGroup.StartGroup',
            id: row.id,
            type: 1
          }).then(res => {
            console.log(res)
            if (res.data.code == '200') {
              this.getList()
              this.$message({
                type: 'success',
                message: this.$t('group.groupGoods.szcg'),
                offset: 100
              })
            }
          })
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: this.$t('group.groupGoods.yqxsz'),
            offset: 100
          })
        })
    },

    //选择一页多少条
    handleSizeChange(e) {
      this.loading = true
      console.log('e', e)
      // this.current_num = e
      this.pageSize = e
      this.getList().then(() => {
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
      this.getList().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total
        this.loading = false
      })
    },
    rowKeys(row) {
      return row.id
    },

    handleSelectionChange(e) {
      this.changeCouList = e
    }
  }
}
