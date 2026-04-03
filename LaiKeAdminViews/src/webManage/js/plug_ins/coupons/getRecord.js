import { seeCouponLogger, allCoupons } from '@/api/plug_ins/coupons'
import { mixinstest } from '@/mixins/index'
import { exports } from '@/api/export/index'
export default {
  name: 'getRecord',
  mixins: [mixinstest],
  data() {
    return {
      stateList: [
        {
          value: '0',
          label: this.$t('coupons.getRecord.wsy')
        },
        {
          value: '1',
          label: this.$t('coupons.getRecord.syz')
        },
        {
          value: '2',
          label: this.$t('coupons.getRecord.ysy')
        },
        {
          value: '3',
          label: this.$t('coupons.getRecord.ygq')
        }
      ], // 订单状态
      typeList: [
        {
          value: '1',
          label: this.$t('coupons.getRecord.myq')
        },
        {
          value: '2',
          label: this.$t('coupons.getRecord.mjq')
        },
        {
          value: '3',
          label: this.$t('coupons.getRecord.zkq')
        }
      ], // 优惠券类型
      inputInfo: {
        orderNo: null,
        membersName: null,
        state: null,
        type: null
      },

      tableData: [],
      loading: true,

      sNo: null,
      // table高度
      tableHeight: null,
      couponsTypeList: [],
      person: 0,
      manNum: 0,
      dialogVisible: false,
    }
  },

  created() {
    this.getCoupons()
    this.seeCouponLoggers()
  },

  mounted() {
    this.$nextTick(function () {
      this.getHeight()
    })
    window.addEventListener('resize', this.getHeight(), false)
  },

  methods: {
    //获取所有优惠券
    getCoupons() {
      allCoupons({
        api: 'plugin.coupon.Admincoupon.GetCouponTypeList'
      }).then(res => {
        this.couponsTypeList = res.data.data.typeList
      })
    },
    // 导出弹框方法
    dialogShow() {
      this.dialogVisible = true
    },
    handleClose(done) {
      this.dialogVisible = false
    },
    // 获取table高度
    getHeight() {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
      console.log(this.$refs.tableFather.clientHeight)
    },
    async seeCouponLoggers() {
      const res = await seeCouponLogger({
        api: 'plugin.coupon.Admincoupon.SeeCouponLogger',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        hid: this.$route.query.id,
        status: this.inputInfo.state,
        keyWord: this.inputInfo.keyWord,
        type: this.inputInfo.type
      })
      this.current_num = 10
      this.total = res.data.data.total
      // this.person = res.data.data.NumberOfRecipients
      this.person = res.data.data.person
      this.manNum = res.data.data.manNum
      this.tableData = res.data.data.list
      this.sNo = res.data.data.sNo
      this.loading = false
      if (this.total < this.current_num) {
        this.current_num = this.total
      }
      console.log(res)
    },

    // 重置
    reset() {
      this.inputInfo.keyWord = null
      this.inputInfo.type = null
      this.inputInfo.state = null
    },

    // 查询
    demand() {
      this.showPagebox = false
      this.loading = true
      this.dictionaryNum = 1
      this.seeCouponLoggers().then(() => {
        this.loading = false
        if (this.tableData.length > 5) {
          this.showPagebox = true
        }
      })
    },

    //选择一页多少条
    handleSizeChange(e) {
      this.loading = true
      // this.current_num = e
      this.pageSize = e
      this.seeCouponLoggers().then(() => {
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
      this.seeCouponLoggers().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total
        this.loading = false
      })
    },

    goOrderList(value) {
      this.$router.push({
        path: '/order/orderList/orderLists',
        query: {
          orderNo: value
        }
      })
    },

    async exportPage() {
      exports({
        api: 'plugin.coupon.Admincoupon.SeeCouponLogger',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        hid: this.$route.query.id,
        status: this.inputInfo.state,
        keyWord: this.inputInfo.keyWord,
        type: this.inputInfo.type,
        exportType: 1,
      }, 'pageorder')
    },

    async exportAll() {
      exports({
        api: 'plugin.coupon.Admincoupon.SeeCouponLogger',
        pageNo: this.dictionaryNum,
        pageSize: this.total,
        hid: this.$route.query.id,
        status: this.inputInfo.state,
        keyWord: this.inputInfo.keyWord,
        type: this.inputInfo.type,
        exportType: 1,
      }, 'allorder')
    },

    async exportQuery() {
      exports({
        api: 'plugin.coupon.Admincoupon.SeeCouponLogger',
        pageNo: this.dictionaryNum,
        pageSize: this.total,
        hid: this.$route.query.id,
        status: this.inputInfo.state,
        keyWord: this.inputInfo.keyWord,
        type: this.inputInfo.type,
        exportType: 1,
      }, 'queryorder')
    }
  }
}
