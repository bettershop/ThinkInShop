import { save, index } from "@/api/order/orderSettlement";
import { exports } from "@/api/export";
import pageData from "@/api/constant/page";
import { isEmpty } from "element-ui/src/utils/util";
import { del } from "@/api/order/comment";
import { mixinstest } from '@/mixins/index'
import { getStorage } from '@/utils/storage'

export default {
  name: 'orderSettlement',
  mixins: [mixinstest],
  //初始化数据
  data() {
    return {
      laikeCurrencySymbol:'￥',
      page: pageData.data(),
      search: {},
      mainData: {},
      summary: {},
      //导出弹窗
      isExportBox: false,
      // table高度
      tableHeight: null
    }
  },
  //组装模板
  created() {
    this.laikeCurrencySymbol = this.LaiKeCommon.getDefaultCurrencySymbol();
    console.log(this.laikeCurrencySymbol)
    this.loadData();
  },
  mounted() {
    this.$nextTick(function () {
      this.getHeight()
    })
    window.addEventListener('resize', this.getHeight(), false)
  },
  methods: {
    // 获取table高度
    getHeight() {
      this.tableHeight = this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
    },
    async loadData() {
      await index({
        api: 'admin.orderSettlement.index',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        search: this.search.orderNo,
        mchName: this.search.mchName,
        status: this.search.type,
        startDate: isEmpty(this.search.time) ? "" : this.search.time[0],
        endDate: isEmpty(this.search.time) ? "" : this.search.time[1],
      }).then(data => {
        if (!isEmpty(data)) {
          let total = data.data.data.total
          this.summary = data.data.data.summary
          data = data.data.data.list;

          this.page.tableData = data;
          this.total = total
          if (this.total.total < 10) {
            this.current_num = this.total
          }
          if (this.total < this.current_num) {
            this.current_num = this.total
          } else {
            this.currpage = ((this.dictionaryNum - 1) * this.pageSize) + 1

            this.current_num = this.page.tableData.length === this.pageSize ? this.dictionaryNum * this.pageSize : this.total
          }
          this.page.loading = false
        }
      });
    },
    tbl(tblId) {
      this.loadData();
    },
    //选择一页多少条
    handleSizeChange(e) {
      this.page.loading = true
      this.pageSize = e
      this.loadData().then(() => {
        this.currpage = ((this.dictionaryNum - 1) * this.pageSize) + 1
        this.current_num = this.page.tableData.length === this.pageSize ? this.dictionaryNum * this.pageSize : this.total
        this.page.loading = false
      })
    },
    // 查询
    demand() {
      this.showPagebox = false
      this.page.loading = true
      this.dictionaryNum = 1
      this.loadData().then(() => {
        this.page.loading = false
        if (this.page.tableData.length > 5) {
          this.showPagebox = true
        }
      })
    },
    //点击上一页，下一页
    handleCurrentChange(e) {
      this.page.loading = true
      this.dictionaryNum = e
      this.currpage = ((e - 1) * this.pageSize) + 1
      this.loadData().then(() => {
        this.current_num = this.page.tableData.length === this.pageSize ? e * this.pageSize : this.total
        this.page.loading = false
      })

    },
    // 重置
    reset() {
      this.search = { orderNo: null, mchName: null, type: null, time: null };
    },
    See(row) {
      this.$router.push({
        name: 'orderSettlementDetails',
        query: {
          orderNo: row.sNo,
          write_off_settings: row.write_off_settings,
          otype: row.otype
        },
      })
    },
    async Del(id) {
      this.$confirm(this.$t('orderSettlementList.qrsc'), this.$t('orderSettlementList.ts'), {
        confirmButtonText: this.$t('orderSettlementList.okk'),
        cancelButtonText: this.$t('orderSettlementList.ccel'),
        type: 'warning'
      }).then(() => {
        del({
          api: 'admin.orderSettlement.del',
          id: id
        }).then(res => {
          // this.demand();
          this.isFillList()
          this.$message({
            type: 'success',
            message: this.$t('orderSettlementList.cg'),
            offset: 102
          })
        })
      }).catch(() => {
      })
    },
    isFillList() {
      let totalPage = Math.ceil((this.total - 1) / this.pageSize)
      // let totalPage = this.orderList.length
      let dictionaryNum = this.dictionaryNum > totalPage ? totalPage : this.dictionaryNum
      this.dictionaryNum = dictionaryNum < 1 ? 1 : dictionaryNum
      console.log('this.dictionaryNum', this.dictionaryNum, this.pageSize);
      this.loadData() //数据初始化方法
    },
    //导出
    async exportPage() {
      await exports({
        api: 'admin.orderSettlement.index',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        exportType: 1,
        search: this.search.orderNo,
        mchName: this.search.mchName,
        status: this.search.type,
        startDate: isEmpty(this.search.time) ? "" : this.search.time[0],
        endDate: isEmpty(this.search.time) ? "" : this.search.time[1],
      }, 'orderSettlement')
    },
    async exportAll() {
      console.log(this.total);
      await exports({
        api: 'admin.orderSettlement.index',
        pageNo: 1,
        pageSize: 999999,
        exportType: 1,
      }, 'orderSettlement')
    },
    async exportQuery() {
      await exports({
        api: 'admin.orderSettlement.index',
        pageNo: 1,
        pageSize: this.total,
        search: this.search.orderNo,
        mchName: this.search.mchName,
        status: this.search.type,
        exportType: 1,
        startDate: isEmpty(this.search.time) ? "" : this.search.time[0],
        endDate: isEmpty(this.search.time) ? "" : this.search.time[1],
      }, 'orderSettlement')
    },
    isExportBoxClose() {
      this.isExportBox = !this.isExportBox;
    }


  }

}
