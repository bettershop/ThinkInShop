import { seeGiveCouponLogger, allCoupons } from '@/api/plug_ins/coupons'
import { mixinstest } from '@/mixins/index'
import { exports } from '@/api/export/index'
export default {
  name: 'givingRecords',
  mixins: [mixinstest],
  data () {
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
      inputInfo: {
        keyWord: null,
        state: null,
        type: null
      },

      tableData: [],
      loading: true,
      // table高度
      tableHeight: null,
      dialogVisible: false
    }
  },

  created () {
    this.getCoupons()
    this.seeGiveCouponLoggers()
  },

  mounted () {
    this.$nextTick(function () {
      this.getHeight()
    })
    window.addEventListener('resize', this.getHeight(), false)
  },

  methods: {
    //获取所有优惠券
    getCoupons () {
      allCoupons({
        api: 'plugin.coupon.Admincoupon.GetCouponTypeList'
      }).then(res => {
        this.couponsTypeList = res.data.data.typeList
      })
    },
    // 导出弹框方法
    dialogShow () {
      this.dialogVisible = true
    },
    handleClose (done) {
      this.dialogVisible = false
    },
    // 获取table高度
    getHeight () {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
      console.log(this.$refs.tableFather.clientHeight)
    },
    async seeGiveCouponLoggers () {
      const res = await seeGiveCouponLogger({
        api: 'plugin.coupon.Admincoupon.SeeGiveCouponLogger',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        hid: this.$route.query.id,
        state: this.inputInfo.state,
        type: this.inputInfo.type,
        keyWord: this.inputInfo.keyWord
      })
      this.current_num = 10
      this.total = res.data.data.total
      this.tableData = res.data.data.list
      this.sNo = res.data.data.sNo
      this.loading = false
      if (this.total < this.current_num) {
        this.current_num = this.total
      }
      console.log(res)
    },

    // 重置
    reset () {
      this.inputInfo.keyWord = null
      this.inputInfo.type = null
      this.inputInfo.state = null
    },

    // 查询
    demand () {
      this.currpage = 1
      this.current_num = 10
      this.showPagebox = false
      this.loading = true
      this.dictionaryNum = 1
      this.seeGiveCouponLoggers().then(() => {
        this.loading = false
        if (this.tableData.length > 5) {
          this.showPagebox = true
        }
      })
    },

    //选择一页多少条
    handleSizeChange (e) {
      this.loading = true
      // this.current_num = e
      this.pageSize = e
      this.seeGiveCouponLoggers().then(() => {
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
      this.seeGiveCouponLoggers().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total
        this.loading = false
      })
    },

    goOrderList (value) {
      this.$router.push({
        path: '/order/orderList/orderLists',
        query: {
          orderNo: value
        }
      })
    },
    async exportPage() {
        exports({
            api: 'plugin.coupon.Admincoupon.SeeGiveCouponLogger',
            pageNo: this.dictionaryNum,
            pageSize: this.pageSize,
            hid: this.$route.query.id,
            state: this.inputInfo.state,
            type: this.inputInfo.type,
            keyWord: this.inputInfo.keyWord,
            exportType: 1,
        },'pageorder')
    },

    async exportAll() {
        exports({
            api: 'plugin.coupon.Admincoupon.SeeGiveCouponLogger',
            pageNo: this.dictionaryNum,
            pageSize: this.total,
            hid: this.$route.query.id,
            state: this.inputInfo.state,
            type: this.inputInfo.type,
            keyWord: this.inputInfo.keyWord,
            exportType: 1,
        },'allorder')
    },

    async exportQuery() {
        exports({
            api: 'plugin.coupon.Admincoupon.SeeGiveCouponLogger',
            pageNo: this.dictionaryNum,
            pageSize: this.total,
            hid: this.$route.query.id,
            state: this.inputInfo.state,
            type: this.inputInfo.type,
            keyWord: this.inputInfo.keyWord,
            exportType: 1,
        },'queryorder')
    }
  }
}
