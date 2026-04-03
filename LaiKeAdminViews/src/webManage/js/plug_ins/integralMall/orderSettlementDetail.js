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
      goodsList: [],
      source: this.$myGetSource(),
      totalIntegral:""
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
        api: 'plugin.integral.order.settlementDetail',
        orderNo: this.$route.query.orderNo,
      }).then(data => {
        // if (!isEmpty(data)) {
          this.orderInfo = data.data.data;
          this.mainData = this.orderInfo.data;
          this.goodsList = this.orderInfo.detail;
          this.totalIntegral=this.goodsList[0].after_discount*this.goodsList[0].num
        // }
      });
    },

    tableHeaderColor({ row, column, rowIndex, columnIndex }) {
      if (rowIndex === 0) {
        return 'background-color: #f4f7f9;'
      }
    },
 
  }

}
