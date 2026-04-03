import { orderDetailsInfo } from '@/api/order/orderList'

export default {
    name: 'orderDetails',

    data() {
        return {
            dataInfo: null,
            goodsTables: [],
            totleInfo: null,
            noteList: [],
            selfLiftingName:''

        }
    },

    created() {
        this.orderDetailsInfos()
        this.selfLiftingName = this.$route.query.selfLiftingName
    },

    methods: {
        async orderDetailsInfos() {
            const res = await orderDetailsInfo({
                api: 'plugin.sec.order.orderDetailsInfo',
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

        // getSource(value) {
        //     if(value == '1') {
        //         return '小程序'
        //     } else if(value == '2') {
        //         return 'APP'
        //     } else if(value == '3') {
        //         return '支付宝小程序'
        //     } else if(value == '4') {
        //         return '头条小程序'
        //     } else if(value == '5') {
        //         return '百度小程序'
        //     } else if(value == '6') {
        //         return 'PC端'
        //     } else if(value == '7') {
        //         return 'H5移动端'
        //     }else {
        //         return '其它'
        //     }
        // }
        
    }
}