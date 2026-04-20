export default {
    data () {
        return {
            bargain: false, // 是否是砍价订单    
            bargain_id: '', // 砍价id    
            order_no: '', // 砍价订单号
        }
    },
    methods: {
        getBargainPayOrderInfo () {
            return {
                api: 'app.order.payment',
                cart_id: this.cart_id,
                address_id: this.address_id,
                type: 'KJ',
                coupon_id: this.coupon_id,
                bargain_id: this.bargain_id,
                bargain_order_no: this.order_no,
                remarks: this.remarks,
            }
        },
        getBargainWalletPayData (postData) {
            return {
                ...postData,
                order_no: this.order_no,
                type_: 'KJ'
            }
        },

    }
}
