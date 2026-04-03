import { orderDetailsInfo, getStoreList, getRecordList } from '@/api/order/orderList'
import { getStorage } from '@/utils/storage'
import ErrorImg from '@/assets/images/default_picture.png'

export default {
  name: 'orderDetails',

  data() {
    return {
      laikeCurrencySymbol:'￥',
      conut: 0,
      dataInfo: null,
      goodsTables: [],
      totleInfo: null,
      noteList: [],
      type: '',
      write_off_settings: 2,
      dialogVisible: false,
      storeList: [],
      dialogVisible2: false,
      recordList: [],
    }
  },

  created() {
    this.laikeCurrencySymbol = this.LaiKeCommon.getDefaultCurrencySymbol();
    if (this.$route.query.type) {
      this.type = this.$route.query.type
    }
    if (this.$route.query.write_off_settings) {
      this.write_off_settings = this.$route.query.write_off_settings
    }
    this.orderDetailsInfos()
  },

  methods: {
    nato(e) {
      const oldkey = 'pic'
      const newkey = 'imgurl'
      e[newkey] = e[oldkey]
      delete e[oldkey]
      console.log(e)
      this.$router.push({
        path: '/order/salesReturn/salesReturnList',
        query: {
          sNo: e.sNo
        }
      })
    },
    async getStore(row) {
      const res = await getStoreList({
        api: 'admin.order.getMch_store',
        mchId: getStorage('laike_admin_userInfo').mchId,
        pid: row.p_id
      })
      this.storeList = res.data.data.list
    },
    async getRecord() {
      const res = await getRecordList({
        api: 'admin.order.getWrite_record',
        sNo: this.dataInfo.sNo
      })
      this.recordList = res.data.data.list
    },
    openStore(row) {
      this.getStore(row)
      this.dialogVisible = true
    },
    handleClose() {
      this.dialogVisible = false
    },
    openRecord() {
      this.getRecord()
      this.dialogVisible2 = true
    },
    handleClose2() {
      this.dialogVisible2 = false
    },
    // 图片错误处理
    handleErrorImg(e) {
      console.log('图片报错了', e.target.src)
      e.target.src = ErrorImg
    },
    async orderDetailsInfos() {
      const res = await orderDetailsInfo({
        api: 'admin.order.orderDetailsInfo',
        sNo: this.$route.query.no
      })
      this.dataInfo = res.data.data.data
      this.goodsTables = res.data.data.detail
      if (this.goodsTables && this.goodsTables.length) {
        this.goodsTables.forEach(v => {
          this.conut += Number(v.num)
        })
      } else {
        this.conut = 0
      }
      this.totleInfo = res.data.data
      let remarksList = res.data.data.data.remarks
      if (remarksList) {
        for (let key in remarksList) {
          this.noteList.push(remarksList[key])
        }
      }
      console.dir(this.goodsTables)
    }
  }
}
