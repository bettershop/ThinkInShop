import { orderDetailsInfo } from '@/api/order/orderList'

export default {
    name: 'orderDetails',

    data() {
        return {
            dataInfo: null,
            goodsTables: [],
            totleInfo: null,
            noteList: [],
            totalIntegral: ''
        }
    },

    created() {
        this.orderDetailsInfos()
    },

    methods: {
        async orderDetailsInfos() {
            const res = await orderDetailsInfo({
                api: 'plugin.integral.order.orderDetailsInfo',
                sNo: this.$route.query.no
            })
            this.dataInfo = res.data.data.data
            this.goodsTables = res.data.data.detail
            this.totleInfo = res.data.data
            let remarksList = res.data.data.data.remarks
            this.currency_symbol = res.data.data.currency_symbol
            if(remarksList) {
                for(let key in remarksList){
                    this.noteList.push(remarksList[key])
                }
            }
            this.totalIntegral = this.goodsTables[0].after_discount * this.goodsTables[0].num
        },

    }
}
