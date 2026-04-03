import { getCouponCardInfo, addCoupon, getAssignGoods, getAssignGoodsClass ,allCoupons } from '@/api/plug_ins/coupons'
import { getTime } from '@/utils/utils'
export default {
    name: 'viewCoupons',
    data() {
        return {
            ruleForm: {
                issuer:0,//发行单位
                issue_number_type:1,//发行数量
                consumption_threshold_type:1,//消费门槛
                pickup_type:0,//领取方式
                date_type:1,//过期时间
                date_one:'',//设置指定过期时间
                date_num:'',//设置领取后多久失效
                limit_num:'',//使用限制
                coupons_type: '',
                coupons_name: '',
                issue_number: '',
                num: '',
                face_value: '',
                consumption_threshold: '',
                available_range: '',
                date: [],
                instructions: '',
                select_goods: [],
                discount:''
            },

            defaultProps: {
                children: 'child',
                label: 'cname'
            },
            couponsTypeList: [],// 优惠券类型
            availableRangeList: [
                {
                    value: 1,
                    name: '全部商品'
                },
                {
                    value: 2,
                    name: '指定商品'
                },
                {
                    value: 3,
                    name: '指定分类'
                },
                {
                    value: 4,
                    name: '充值会员'
                }
            ],

            goodsList: [], // 商品列表

            toData:[]
        }
    },

    created() {
        this.getCoupons()
        this.getAssignGoods()
        this.getCouponCardInfos()
    },

    beforeRouteLeave (to, from, next) {        
        if (to.name == 'couponsList') {
          to.params.dictionaryNum = this.$route.query.dictionaryNum
          to.params.pageSize = this.$route.query.pageSize
        }   
        next();
      },

    methods: {
            //获取所有优惠券
            getCoupons(){
                allCoupons({
                    api: 'plugin.coupon.Admincoupon.GetCouponTypeList',
                }).then(res =>{
                    this.couponsTypeList = res.data.data.typeList
                    
                })
            },
        async getCouponCardInfos() {
            const res = await getCouponCardInfo({
              api: 'plugin.coupon.Admincoupon.GetCouponCardInfo',
              mchId: 0,
              hid: this.$route.query.id,
            })
            let couponsInfo = res.data.data.list[0]
            this.ruleForm.pickup_type = couponsInfo.receive_type
            this.prochangedata = couponsInfo.goodsIdList
            this.ruleForm.issuer = couponsInfo.issuer
            this.ruleForm.date_type = couponsInfo.date_type
            this.ruleForm.date_one = couponsInfo.end_time
            this.ruleForm.date_num = couponsInfo.day??''
            this.ruleForm.limit_num = couponsInfo.receive
            this.ruleForm.issue_number_type = couponsInfo.issue_number_type
            this.ruleForm.consumption_threshold_type = couponsInfo.consumption_threshold_type
            this.ruleForm.coupons_time = couponsInfo.day
            this.ruleForm.discount = couponsInfo.discount

            this.ruleForm.coupons_type = couponsInfo.couponType.toString()
            this.ruleForm.coupons_name = couponsInfo.name
            this.ruleForm.issue_number = couponsInfo.circulation
            this.ruleForm.num = couponsInfo.num
            this.ruleForm.face_value = couponsInfo.money
            this.ruleForm.consumption_threshold = couponsInfo.z_money
            for (const item of this.availableRangeList) {
              if(item.name === couponsInfo.type) {
                this.ruleForm.available_range = item.value
                break
              }
            }
            if(couponsInfo.goodsIdList) {
                this.ruleForm.select_goods = couponsInfo.goodsIdList.map(item => {
                  return item.id
                })
            }
            if(couponsInfo.classIdList) {
                this.toData = couponsInfo.classIdList.map(item => {
                    return {
                        child: [],
                        cid: item.cid,
                        cname: item.pname,
                        level: item.level,
                        sid: item.sid
                    }
                })
            }
            this.ruleForm.date[0] = getTime(couponsInfo.start_time)
            this.ruleForm.date[1] = getTime(couponsInfo.end_time)
            this.ruleForm.instructions = couponsInfo.Instructions
            console.log(res);
        },

        async getAssignGoods() {
            const res = await getAssignGoods({
              api: 'plugin.coupon.Admincoupon.GetAssignGoods',
            })
            this.goodsList = res.data.data.list
        },
    }
}