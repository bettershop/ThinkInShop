import { orderDetailsInfo } from '@/api/order/orderList'

export default {
  name: 'orderDetails',

  data () {
    return {
      dataInfo: null,
      goodsTables: [],
      totleInfo: null,
      noteList: []
    }
  },

  created () {
    this.orderDetailsInfos()
  },

  methods: {
    async orderDetailsInfos () {
      const res = await orderDetailsInfo({
        api: 'plugin.group.AdminGroupOrder.OrderDetailsInfo',
        sNo: this.$route.query.no
      })
      this.dataInfo = res.data.data.data
      this.goodsTables = res.data.data.detail
      this.totleInfo = res.data.data
      let remarksList = res.data.data.data.remarks
      if (remarksList) {
        for (let key in remarksList) {
          this.noteList.push(remarksList[key])
        }
      }
      console.dir(this.goodsTables)
    },

  }
}
