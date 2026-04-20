export default {
    data () {
        return {
            seckill: false, // 是否是秒杀订单    
            sec_id: 0, // 秒杀id
            time_id: 0, // 秒杀 time_id
            platform_activities_id: 0, // 秒杀 time_id
            activity_id: 0, // 秒杀 time_id
        }
    },
    methods: {
        getSeckillPayOrderInfo() {
            let type = 'MS';
            if (this.b_type == 1) {
                type = 'PM'
            }
            
            return {
                api: 'app.order.payment',
                cart_id: this.cart_id,
                address_id: this.address_id,
                type: type,
                coupon_id: this.coupon_id,
                remarks: this.remarks,
                activity_id: this.activity_id, //秒杀活动id
                time_id: this.time_id, //时段id
                platform_activities_id: this.platform_activities_id,
                sec_id: this.sec_id,
				store_type : this.getStoreType()
            }
        },
        
        getSeckillWalletPayData (postData) {
            return {
                ...postData,
                activity_id: this.activity_id,
                time_id: this.time_id,
                payment_money: this.total
            }
        }
    }
}
