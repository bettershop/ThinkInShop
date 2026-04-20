export default {
    data () {
        return {
            use_coupon: false,
            coupon_list: '', //可使用优惠券
            coupon_id: 0,
            coupon_name: '',
            coupon_status: false,
        }
    },
    methods: {
        /**
         * 点击优惠券选择弹窗
         */
        coupon() {
            let data = {
                api: 'app.Coupon.my_coupon',
                cart_id: this.cart_id,
                product: this.cpId
            }

            this.$req.post({data}).then(res => {
                let { list } = res
                this.coupon_list = list
                this.use_coupon = true
            })
        },
        /**
         * 关闭优惠券选择框
         * @private
         */
        closeCoupon () {
            // TODO 请求后台来计算价格
            let data = {
                api: 'app.Coupon.getvou',
                cart_id: this.cart_id,
                address_id: this.address_id,
                coupon_id: this.coupon_id,
                product: this.cpId
            }

            this.$req.post({data}).then(res => {
                this.coupon_name = res.coupon_name
                this.coupon_money = Number.parseFloat(res.money)
                
                this._axios && this._axios()
            })

            this.use_coupon = false
        },
        /**
         * 选择优惠券时切换
         * @param index
         * @param id
         * @param coupon_status
         * @returns {boolean}
         * @private
         */
        coupon_use (index, id, coupon_status) {

            if (!coupon_status) {
                uni.showToast({
                    title: this.language.pay.orderDetailsr.cannotCoupon,
                    icon: 'none',
                    duration: 1500
                })
                return false
            }
            this.coupon_id = id
        },
    }
}
