import {save, index} from "@/api/order/orderSettlement";
import { getStoreList, getRecordList} from '@/api/order/orderList'
import { getStorage } from '@/utils/storage'
import {isEmpty} from "element-ui/src/utils/util";
import {del} from "@/api/order/comment";

export default {
  name: 'orderSettlementDetail',
  //初始化数据
  data() {
    return {
      laikeCurrencySymbol:'￥',
      mainData: {},
      orderInfo: {},
      goodsList: [],
      source: this.$myGetSource(),
      write_off_settings: 2,
      otype:'GM',
      dialogVisible: false,
      storeList: [],
      dialogVisible2: false,
      recordList: [],
    }
  },
  //组装模板
  created() {
    this.laikeCurrencySymbol = this.LaiKeCommon.getDefaultCurrencySymbol();
    if (this.$route.query.write_off_settings) {
      this.write_off_settings = this.$route.query.write_off_settings
    }
    if (this.$route.query.otype) {
      this.otype = this.$route.query.otype
    }
    this.loadData();
  },
  mounted() {
  },
  methods: {
    async loadData() {
      await index({
        api: 'admin.orderSettlement.detail',
        orderNo: this.$route.query.orderNo,
      }).then(data => {
        if (!isEmpty(data)) {
          this.orderInfo = data.data.data;
          this.mainData = this.orderInfo.data;
          this.goodsList = this.orderInfo.detail;
        }
      });
    },
    async getRecord () {
      const res = await getRecordList({
        api: 'admin.order.getWrite_record',
        sNo	:this.mainData.sNo
      })
      this.recordList = res.data.data.list
    },
    async getStore (row) {
      const res = await getStoreList({
        api: 'admin.order.getMch_store',
        mchId: getStorage('laike_admin_userInfo').mchId,
        pid:row.p_id
      })
      this.storeList = res.data.data.list
    },
    openRecord () {
      this.getRecord()
      this.dialogVisible2 = true
    },
    openStore (row) {
      this.getStore(row)
      this.dialogVisible = true
    },
    handleClose () {
      this.dialogVisible = false
    },
    handleClose2 () {
      this.dialogVisible2 = false
    },
    natoa(e){
      console.log(e)
      this.$router.push({
          path: '/goods/inventoryManagement/inventoryList',
          query: {
          productTitle:e.p_name
          }
      })
    },
    nato(e){
       const  oldkey = 'pic'
       const  newkey = 'imgurl'
       e[newkey] = e[oldkey]
       delete e[oldkey];
       console.log(e)
      this.$router.push({
          path: '/order/salesReturn/salesReturnList',
          query: {
              sNo:e.sNo
          }
      })
    },

  }

}
