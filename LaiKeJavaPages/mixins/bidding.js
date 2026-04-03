export default {
    data () {
      return {
          bind_id: '', //竞拍商品id
          jp_address: '',
          jp_imgurl: '',
          jp_name: '',
      }  
    },
    methods: {
        getJPPayOrderInfo () {
            return {
                api: 'app.order.payment',
                address_id: this.address_id,
                type: 'JP',
                auction_id: this.bind_id,
                coupon_id: this.coupon_id,
                remarks: this.remarks,
            }
        },
        getJPWalletPayData (postData) {
            return {
                ...postData
            }
        }
    }
}
