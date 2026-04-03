import { orderDetailsInfo } from '@/api/plug_ins/supplier'

export default {
    name: 'orderDetails',

    data() {
        return {
            dataInfo: null,
            goodsTables: [],
            totleInfo: null,
            noteList: []
        }
    },

    created() {
        this.orderDetailsInfos()
    },

    methods: {
        async orderDetailsInfos() {
            const res = await orderDetailsInfo({
                api: 'supplier.Admin.SupplierOrders.OrderDetailsInfo',
                sNo: this.$route.query.no
            })
            this.dataInfo = res.data.data.data			
            this.goodsTables = res.data.data.detail
            this.totleInfo = res.data.data
            let remarksList = res.data.data.data.remarks
            if(remarksList) {
                for(let key in remarksList){
                    this.noteList.push(remarksList[key])
                }
            }
            console.dir(this.goodsTables)
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