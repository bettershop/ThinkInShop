// import { orderList, delOrder, closeOrder } from '@/api/plug_ins/seckill'
import { orderList, delOrder, closeOrder } from '@/api/plug_ins/auction'
import { kuaidishow } from '@/api/order/orderList'
import { exports } from '@/api/export/index'
import { mixinstest } from '@/mixins/index'
import { getStorage } from '@/utils/storage'
import { getButton } from '@/api/layout/information'
export default {
  name: 'auctionOrder',
  mixins: [mixinstest],

  data() {
    return {
      routerList: JSON.parse(sessionStorage.getItem('tabRouter')),
      radio1: this.$t('auction.jpdd'),
      inputInfo: {
        name: null,
        mchName: null,
        state: '1',
        date: null
      },
      stateList: [
        {
          value: '0',
          label: this.$t('auction.auctionOrder.dfk')
        },
        {
          value: '1',
          label: this.$t('auction.auctionOrder.dfh')
        },
        {
          value: '2',
          label: this.$t('auction.auctionOrder.dsh')
        },
        {
          value: '5',
          label: this.$t('auction.auctionOrder.ywc')
        },
        {
          value: '7',
          label: this.$t('auction.auctionOrder.ygb')
        }
      ], // 订单状态

      tableData: [],
      loading: true,
      button_list: [],
      idList: [],
      idPrintList: [],
      is_disabled: true,

      // 弹框数据
      dialogVisible2: false,
      ruleForm: {
        courier_company: '',
        courier_no: '',
        logistics: [],
      },

      // table高度
      tableHeight: null,

      // 导出弹框数据
      dialogVisible: false
    }
  },

  created() {
    if (this.$route.query.no) {
      orderList({
        api: 'plugin.auction.AdminAuction.index',
        pageSize: this.pageSize,
        pageNo: this.dictionaryNum,
        keyWord: this.inputInfo.name,
        mchName: this.inputInfo.mchName,
        status: this.inputInfo.state,
        id: this.$route.query.no,
        startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
        endDate: this.inputInfo.date ? this.inputInfo.date[1] : null
      }).then(res => {
        console.log(res);
        this.total = res.data.data.total
        this.tableData = res.data.data.list
        this.loading = false
        if (this.total < this.current_num) {
          this.current_num = this.total
        }
      })
    } else {
      this.orderLists()
    }
    this.getButtons()
  },

  mounted() {
    this.$nextTick(function () {
      this.getHeight()
    })
    window.addEventListener('resize', this.getHeight(), false)
  },

  methods: {
    rowstyles({ row, rowIndex }) {
      let styleJson = {
        border: 'none'
      }
      return styleJson
    },
    //获取按纽权限
    async getButtons() {
      let route = getStorage('route')
      route.forEach(item => {
        if (item.path == 'seckill') {
          item.children.forEach(e => {
            if (e.path == 'seckillOrder') {
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
      console.log(this.button_list, '获取按纽权限')
    },
    async orderLists() {
      const res = await orderList({
        api: 'plugin.auction.AdminAuction.index',
        pageSize: this.pageSize,
        pageNo: this.dictionaryNum,
        keyWord: this.inputInfo.name,
        mchName: this.inputInfo.mchName,
        status: this.inputInfo.state,
        startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
        endDate: this.inputInfo.date ? this.inputInfo.date[1] : null
      })
      console.log(res)
      this.total = res.data.data.total
      this.tableData = res.data.data.list
      this.loading = false
      if (this.total < this.current_num) {
        this.current_num = this.total
      }
    },

    // 获取table高度
    getHeight() {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
    },

    // 重置
    reset() {
      this.inputInfo.name = null
      this.inputInfo.mchName = null
      this.inputInfo.state = null
      this.inputInfo.date = null
    },

    // 查询
    demand() {
      this.currpage = 1
      this.current_num = 10
      // this.showPagebox = false
      this.loading = true
      this.dictionaryNum = 1
      this.orderLists().then(() => {
        this.loading = false
        // if(this.tableData.length > 5) {
        //     this.showPagebox = true
        // }
      })
    },

    // 订单售后
    afterSales() {
      this.$router.push({
        path: '/plug_ins/seckill/afterSaleList'
      })
    },

    // 评价管理
    evaluation() {
      this.$router.push({
        path: '/plug_ins/auction/evaluateList'
      })
    },

    // 订单结算
    settlement() {
      this.$router.push({
        path: '/plug_ins/auction/orderSettlementList'
      })
    },

    // 订单打印
    print() {
      if (this.idPrintList.length == 0) {
        this.$message({
          message: this.$t('auction.auctionOrder.qxzdd'),
          type: 'warning',
          offset: 100
        })
      } else {
        let routeData = this.$router.resolve({
          path: '/print',
          query: {
            orderId: this.idPrintList
          }
        })

        window.open(routeData.href, '_blank')
      }
    },

    // 批量删除
    delAll() {
      this.$confirm(this.$t('auction.auctionOrder.scts'), this.$t('auction.ts'), {
        confirmButtonText: this.$t('auction.okk'),
        cancelButtonText: this.$t('auction.ccel'),
        type: 'warning'
      })
        .then(() => {
          delOrder({
            // api: 'plugin.sec.order.delOrder',
            api: 'plugin.auction.order.delOrder',
            id: this.idList
          }).then(res => {
            console.log(res)
            if (res.data.code == '200') {
              this.orderLists()
              this.$message({
                type: 'success',
                message: this.$t('auction.cg'),
                offset: 100
              })
            }
          })
        })
        .catch(() => {
          // this.$message({
          //   type: 'info',
          //   message: '已取消删除',
          //   offset: 100
          // })
        })
    },

    handleSelectionChange(val) {
      if (val.length !== 0) {
        this.is_disabled = false
      } else {
        this.is_disabled = true
      }
      this.idList = val.map(item => {
        return item.id
      })
      this.idPrintList = val.map(item => {
        return item.orderno
      })
      this.idList = this.idList.join(',')
      this.idPrintList = this.idPrintList.join(',')
    },

    // 订单详情
    Details(row) {
      console.log(row)
      this.$router.push({
        path: '/plug_ins/auction/orderDetails',
        query: {
          no: row.orderno
        }
      })
    },

    // 编辑订单
    Edit(value) {
      console.log(value)
      this.$router.push({
        path: '/plug_ins/auction/editorOrder',
        query: {
          no: value.orderno
        }
      })
    },

    // 商品发货
    Delivery(value) {
      console.log(value)
      this.$router.push({
        path: '/plug_ins/auction/goodsDelivery',
        query: {
          no: value.orderno
        }
      })
    },

    // 关闭订单
    closeOrder(value) {
      console.log(value)
      closeOrder({
        // api: 'plugin.sec.order.closeOrder',
        api: 'plugin.auction.order.closeOrder',
        id: value.id
      }).then(res => {
        if (res.data.code == '200') {
          console.log(res)
          this.$message({
            type: 'success',
            message: this.$t('auction.cg'),
            offset: 100
          })
          this.orderLists()
        }
      })
    },

    // 弹框方法
    dialogShow2(value) {
      this.dialogVisible2 = true
      kuaidishow({
        // api: 'plugin.sec.order.kuaidishow',
        api: 'plugin.auction.order.kuaidishow',
        orderno: value.orderno
      }).then(res => {
        console.log(res)
        this.ruleForm.courier_company = res.data.data.list[0].kuaidi_name
        this.ruleForm.courier_no = res.data.data.list[0].courier_num
        this.ruleForm.logistics = res.data.data.list[0].list
      })
      console.log(value)
    },

    handleClose2(done) {
      this.dialogVisible2 = false
    },

    //选择一页多少条
    handleSizeChange(e) {
      this.loading = true
      this.pageSize = e
      this.orderLists().then(() => {
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
      this.orderLists().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total
        this.loading = false
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
          // api: 'plugin.sec.order.index',
          api: 'plugin.auction.AdminAuction.index',
          pageNo: this.dictionaryNum,
          pageSize: this.pageSize,
          exportType: 1,
          keyWord: this.inputInfo.name,
          mchName: this.inputInfo.mchName,
          status: this.inputInfo.state,
          startdate: this.inputInfo.date ? this.inputInfo.date[0] : null,
          enddate: this.inputInfo.date ? this.inputInfo.date[1] : null
        },
        'pageorder'
      )
    },

    async exportAll() {
      exports(
        {
          // api: 'plugin.sec.order.index',
          api: 'plugin.auction.AdminAuction.index',
          pageNo: 1,
          pageSize: this.total,
          exportType: 1
        },
        'allorder'
      )
    },

    async exportQuery() {
      exports(
        {
          // api: 'plugin.sec.order.index',
          api: 'plugin.auction.AdminAuction.index',
          pageNo: 1,
          pageSize: this.total,
          exportType: 1,
          keyWord: this.inputInfo.name,
          mchName: this.inputInfo.mchName,
          status: this.inputInfo.state,
          startdate: this.inputInfo.date ? this.inputInfo.date[0] : null,
          enddate: this.inputInfo.date ? this.inputInfo.date[1] : null
        },
        'queryorder'
      )
    }
  }
}
