import { getspecialLlist, delespecial , switchShow } from '@/api/plug_ins/auction'
import { mixinstest } from '@/mixins/index'
import { getStorage } from "@/utils/storage";

export default {
  name: 'specialLlist',
  mixins: [mixinstest],
  data () {
    return {
      routerList:JSON.parse(sessionStorage.getItem('tabRouter')),
      radio1: this.$t('auction.zclb'),
      type: '', //判断是否为编辑
      statusList: [
        {
          value: '1',
          label: this.$t('auction.specialLlist.wks')
        },
        {
          value: '2',
          label: this.$t('auction.specialLlist.jxz')
        },
        {
          value: '3',
          label: this.$t('auction.specialLlist.yjs')
        }
      ],
      specialList: [
        {
          value: '1',
          label: this.$t('auction.specialLlist.dpzc')
        },
        {
          value: '2',
          label: this.$t('auction.specialLlist.ptzc')
        },
        {
          value: '3',
          label: this.$t('auction.specialLlist.bmzc')
        }
      ],
      inputInfo: {
        key: '', //关键字 id/专场名称/店铺名称
        status: '', //状态 1=未开始 2=进行中 3=已结束
        time: '', //时间
        type:'',//专场类型 1=店铺专场 2=普通专场 3=报名专场
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
    getspecialLlist({ api: "plugin.sec.order.orderCount" }).then((res) => {
      if (res.data.code == 51008) {
        this.$router.push("/mall/fastBoot/index");
      }
    });
    if (getStorage('laike_admin_userInfo').mchId == 0) {
      this.$message({
        type: 'error',
        message: this.$t('plugInsSet.plugInsList.qtjdp'),
        offset: 100
      })
      this.$router.push('/mall/fastBoot/index')
    }
    this.getspecialLlists()
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

    async getspecialLlists () {
      const res = await getspecialLlist({
        api: 'plugin.auction.AdminAuction.getSpecialList',
        type:this.inputInfo.type,
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        accessId: this.$store.getters.token,
        key: this.inputInfo.key, //关键字 id/专场名称/店铺名称
        status: this.inputInfo.status, //状态 1=未开始 2=进行中 3=已结束
        id: '', //专场id(详情)
        startDate:this.inputInfo.time?.[0]??'',
        endDate:this.inputInfo.time?.[1]??'',
      })

      if (res.data.code == 200) {
        console.log('res', res)
        this.total = res.data.data.total
        this.tableData = res.data.data.list
        if (res.data.data.total < 10) {
          this.current_num = this.total;
        }
        this.loading = false
      }

      console.log('res', res)
    },
    // 是否显示
    switchs (row) {
      console.log('row',row);

      switchShow({
        api: 'plugin.auction.AdminAuction.switchSpecial',
        accessId: this.$store.getters.token,
        id: row.id
      }).then(res => {
        if (res.data.code == '200') {
          this.getspecialLlists()
          this.$message({
            type: 'success',
            message: $t('auction.cg'),
            offset: 100
          })
        }
      })
    },
    // 重置
    reset () {
      this.inputInfo.type = ''
      this.inputInfo.key = ''
      this.inputInfo.status = ''
      this.inputInfo.time = ''
    },

    search () {
      this.getspecialLlists()
    },

    // 编辑
    addspecialList () {
      this.$router.push({
        path: '/plug_ins/auction/specialRelease',
        query: {
          type: 2
        }
      })
    },
    //跳转缴纳记录
    Payrecord (row) {
      console.log('row',row);

      this.$router.push({
        path: '/plug_ins/auction/payRecord',
        query: {
          specialId: row.id
        }
      })
    },
    //跳转场次列表
    Seecheck (row) {
      console.log('row', row)
      this.$router.push({
        path: '/plug_ins/auction/eventsList',
        query: {
          id: row.id
        }
      })
    },

    // 编辑
    Edit (row) {
      this.$router.push({
        path: '/plug_ins/auction/specialRelease',
        query: {
          row: row,
          type: 1
        }
      })
    },
    //跳转拍品列表
    Seebid (row) {
      console.log('row', row)
      this.$router.push({
        path: '/plug_ins/auction/seeCheck',
        query: {
          specialId: row.id,
          statusName: row.statusName
        }
      })
    },
    Delete (row) {
      console.log('row', row)
      this.$confirm(this.$t('auction.specialLlist.scts'), this.$t('auction.ts'), {
        confirmButtonText: this.$t('auction.okk'),
        cancelButtonText: this.$t('auction.ccel'),
        type: 'warning'
      })
        .then(() => {
          delespecial({
            api: 'plugin.auction.AdminAuction.delSpecial',
            accessId: this.$store.getters.token,
            id: row.id
          }).then(res => {
            console.log('del', res)
            if (res.data.code == 200) {
              this.getspecialLlists()
              this.$message({
                type: 'success',
                message: this.$t('auction.sccg'),
                offset: 100
              })
            }
          })
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除',
            offset: 100
          })
        })
    },

    //选择一页多少条
    handleSizeChange (e) {
      this.loading = true
      console.log(e)
      // this.current_num = e
      this.pageSize = e
      this.getspecialLlists().then(() => {
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
      this.getspecialLlists().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total
        this.loading = false
      })
    }
  }
}
