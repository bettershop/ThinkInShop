import {
  getTiebaList,
  getSwitch,
  startGroup,
  operation,
  addLabel
} from '@/api/plug_ins/tieba'
import { mixinstest } from '@/mixins/index'

export default {
  name: 'groupGoods',
  mixins: [mixinstest],
  data() {
    return {
      routerList: JSON.parse(sessionStorage.getItem('tabRouter')),
      radio1: this.$t('tieaba.htgl'),
      inputInfo: {
        name: null,
      },
      tableData: [],
      loading: true,
      // table高度
      tableHeight: null,
      changeCouList: [],
      laike_admin_uaerInfo: '',
      dialogVisible4: false,
      labelsInfo: {
        name: '',
      },
      classList: [],
      id: ''
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

  methods: {

    addLabels(row) {
      this.dialogVisible4 = true
      console.log(row,'row')
      if (row.name !='') {
        this.id = row.id
        this.labelsInfo.name = row.name
      } else {
        this.id = ''
        this.labelsInfo.name = ''
      }
    },
    submit() {
      addLabel({
        api: 'plugin.bbs.Adminbbs.editLabel',
        id: this.id ? this.id : '',
        name: this.labelsInfo?.name,
      }).then(res => {
        if (res.data.code == '200') {
          this.dialogVisible4 = false
          this.inputInfo.name = ''
          this.getList()
          this.$message({
            type: 'success',
            message: this.$t('group.czcg'),
            offset: 100
          })
        }
      })
    },

    // 查看关联
    viewDetails(row) {
      this.$router.push({
        path: '/plug_ins/tieba/articleManagement',
        query: {
          label_id: row.id
        }
      })
    },
    async getList() {
      const res = await getTiebaList({
        api: 'plugin.bbs.Adminbbs.label',
        page: this.dictionaryNum,
        pagesize: this.pageSize,
        name: this.inputInfo.name,
      })
      this.total = res.data.data.total
      this.tableData = res.data.data.list
      this.loading = false
      if (this.total < this.current_num) {
        this.current_num = this.total
      }

    },
    // 重置
    reset() {
      this.inputInfo.name = ''
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
    Delete(row) {
      this.$confirm('确认要删除吗?', this.$t('zdata.ts'), {
        confirmButtonText: this.$t('zdata.ok'),
        cancelButtonText: this.$t('zdata.off'),
        type: 'warning'
      })
        .then(() => {
          operation({
            api: 'plugin.bbs.Adminbbs.delLabel',
            ids: row.id
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
  }
}
