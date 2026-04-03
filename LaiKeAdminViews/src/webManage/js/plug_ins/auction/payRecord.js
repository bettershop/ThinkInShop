import { getCashList, delcash } from '@/api/plug_ins/auction'
import { mixinstest } from '@/mixins/index'

export default {
  name: 'payRecord',
  mixins: [mixinstest],

  data() {
    return {
      specialId: '',
      inputInfo: {
        key: '',//关键字 用户id/用户名称/联系电话
        type: '',//	操作类型 0=待缴纳 1=已缴纳 2=已退还
        name: '',
        time: []
      },
      options: [{
        value: '0',
        label: this.$t('auction.payRecord.djn')
      }, {
        value: '1',
        label: this.$t('auction.payRecord.yjn'),
      }, {
        value: '2',
        label: this.$t('auction.payRecord.yth')
      }],
      tableData: [],
      tableHeight: null
    }
  },

  created() {
    if (this.$route.params.pageSize) {
      this.pagination.page = this.$route.params.dictionaryNum
      this.dictionaryNum = this.$route.params.dictionaryNum
      this.pageSize = this.$route.params.pageSize
    }
    this.specialId = this.$route.query.specialId
    console.log('1111111', this.specialId);

    this.getList()
  },
  mounted() {
    // this.$nextTick(function () {
    this.getHeight()
    // })
    window.addEventListener('resize', this.getHeight(), false)
  },

  methods: {
    getHeight() {
      this.tableHeight = this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
      console.log(this.tableHeight, 'this.tableHeight')

    },
    async getList() {
      const res = await getCashList({
        api: 'plugin.auction.AdminAuction.getPromiseList',
        accessId: this.$store.getters.token,
        specialId: this.specialId,
        key: this.inputInfo.key,
        type: this.inputInfo.type,
        startDate: this.inputInfo.time?.[0] ?? '',
        endDate: this.inputInfo.time?.[1] ?? '',
      })
      this.total = res.data.data.total
      this.tableData = res.data.data.list
      if (this.tableData.length < 10) {
        this.current_num = this.tableData.length
      }
      console.log('tableData', this.tableData);
    },
    // 删除
    Delete(row) {
      console.log(row);
      this.$confirm(this.$t('auction.payRecord.scts'), this.$t('auction.ts'), {
        confirmButtonText: this.$t('auction.okk'),
        cancelButtonText: this.$t('auction.ccel'),
        type: 'warning'
      }).then(() => {
        delcash({
          api: 'plugin.auction.AdminAuction.delPromise',
          accessId: this.$store.getters.token,
          id: row.id
        }).then(res => {
          if (res.data.code == '200') {
            console.log(res);
            this.getList()
            this.$message({
              type: 'success',
              message: this.$t('auction.cg')
            });
          }
        })
      }).catch(() => {
        // this.$message({
        //   type: 'info',
        //   message: '已取消删除'
        // });          
      });
    },
    reset() {
      this.inputInfo.key = '',
        this.inputInfo.type = '',
        this.inputInfo.time = ''
    },

    demand() {
      this.getList()
    },

    //选择一页多少条
    handleSizeChange(e) {
      this.pageSize = e
      this.getList().then(() => {
        this.currpage = ((this.dictionaryNum - 1) * this.pageSize) + 1
        this.current_num = this.tableData.length === this.pageSize ? this.dictionaryNum * this.pageSize : this.total
      })
    },

    //点击上一页，下一页
    handleCurrentChange(e) {
      this.dictionaryNum = e
      this.currpage = ((e - 1) * this.pageSize) + 1
      this.getList().then(() => {
        this.current_num = this.tableData.length === this.pageSize ? e * this.pageSize : this.total
      })
    },

  }
}