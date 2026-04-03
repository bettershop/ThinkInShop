import {save, index} from "@/api/order/orderSettlement";
import {isEmpty} from "element-ui/src/utils/util";
import {del} from "@/api/order/comment";

export default {
  name: 'orderSettlementDetail',
  //初始化数据
  data() {
    return {
      mainData: {},
      orderInfo: {},
      consigneeInfo:{},
      goodsList: [],
      source: this.$myGetSource(),
      refund:null,
      list:[],
      returnStatus: ''
    }
  },
  //组装模板
  created() {
    this.loadData();
  },
  mounted() {
  },
  methods: {
    async loadData() {
      await index({
       api:'plugin.presell.AdminPreSell.orderDetail',
       orderNo: this.$route.query.orderNo
      }).then(data => {
        if (!isEmpty(data)) {
          console.log(data.data.data);
          this.consigneeInfo=data.data.data.consigneeInfo
          this.mainData = data.data.data.essentialInfo;
          this.goodsList = data.data.data.productInfo;
          this.list=data.data.data.productInfo[0]
          this.is_refund=this.list.isRefund==1 ? true:false //1退款
          this.returnStatus = data.data.data.returnStatus
        }
      });
    },

    view(value) {
      console.log(value);
      this.$router.push({
          path: '/plug_ins/preSale/afterSaleDetails',
          query: {
              id: value.id,
          }
      })
  },

    tableHeaderColor({ row, column, rowIndex, columnIndex }) {
      if (rowIndex === 0) {
        return 'background-color: #f4f7f9;'
      }
    },
    
  }

}
