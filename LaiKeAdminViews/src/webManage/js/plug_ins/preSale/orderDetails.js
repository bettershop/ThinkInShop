import { orderDetailsInfo } from '@/api/order/orderList'

export default {
    name: 'orderDetails',

    data() {
        return {
            dataInfo: null,
            is_refund: false,
            noteList: [],
            productInfo:[],
            list:{},
            consigneeInfo:null  //地址
        }
    },

    created() {
        this.orderDetailsInfos()
    },

    methods: {
        async orderDetailsInfos() {
            const res = await orderDetailsInfo({
                api: 'plugin.presell.AdminPreSell.orderDetail',  
                orderNo: this.$route.query.no
            })
            console.log(res.data.data,"shujujuu1");
            this.dataInfo = res.data.data.essentialInfo      //预售
            this.productInfo =res.data.data.productInfo  
            this.list=res.data.data.productInfo[0]
            this.consigneeInfo = res.data.data.consigneeInfo    //地址
            this.is_refund=this.productInfo[0].isRefund==1 ? true:false //1退款成功              
        },
        //跳售后详情页面
        afterSaleDetails(id){
            this.$router.push({
                path:'/plug_ins/preSale/afterSaleDetails',
                query: {
                    id:id
                }
            })
        },
        
    }
}