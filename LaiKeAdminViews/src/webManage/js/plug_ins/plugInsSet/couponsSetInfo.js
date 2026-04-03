import {
  getCouponConfigInfo,
  addCouponConfig,
  allCoupons
} from '@/api/plug_ins/coupons'
export default {
  name: 'couponsSet',

  data() {
    return {
      loading: true,
      radio1: this.$t('coupons.yhqsz'),
      fangdou: true,
      ruleForm: {
        switchs: 0, // 是否开启优惠券
        // isShow:0,
        // clear_coupon: 1, // 是否自动清除过期优惠券
        // clear_coupon_day: 1,
        // clear_activity: 1, // 是否自动清除过期活动
        // clear_activity_day: 1,
        // limit_get: 1, // 限领设置
        coupons_type: [] // 享受优惠商品
      },

      clearCouponList: [
        {
          value: 1,
          name: this.$t('coupons.couponsSet.shi')
        },
        {
          value: 0,
          name: this.$t('coupons.couponsSet.fou')
        }
      ],

      clearActivityList: [
        {
          value: 1,
          name: this.$t('coupons.couponsSet.shi')
        },
        {
          value: 0,
          name: this.$t('coupons.couponsSet.fou')
        }
      ],

      limitGetList: [
        {
          value: 0,
          name: this.$t('coupons.couponsSet.mrxl')
        },
        {
          value: 1,
          name: this.$t('coupons.couponsSet.mrkl')
        }
      ],

      checkAll: false,
      isIndeterminate: false,
      couponsTypeList: []
    }
  },

  created() {
    // this.ruleForm.switchs = 1
    this.getCouponConfigInfos()
    this.getCoupons()
  },
  beforeRouteLeave(to, from, next) {
    if (
      JSON.stringify(this.ruleForm) == sessionStorage.getItem('ruleForm_coupon')
    ) {
      next()
    } else {
      console.log('表单变化，询问是否保存')
      next(false)
      this.$confirm(
        this.$t('coupons.couponsSet.sjygx'),
        this.$t('coupons.ts'),
        {
          distinguishCancelAndClose: true,
          confirmButtonText: this.$t('coupons.okk'),
          cancelButtonText: this.$t('coupons.ccel'),
          type: 'warning'
        }
      )
        .then(() => {
          // this.submitForm()
          next()
        })
        .catch(() => {
          // next()
          // next('/plug_ins/plugInsSet/plugInsList')
        })
    }
  },
  methods: {
    compareData() {
      //如果是newAdd为undefined证明不是增加新数据，直接不提示。
      if (this.$route.query.newAdd == undefined) {
        // 目前this.form是组件中的form
        let compare =
          JSON.stringify(this.ruleForm) === JSON.stringify(this.ruleFormCopy)
        return compare
      } else {
        return true
      }
    },
    beforeRouteLeave(to, from, next) {
      const flag = this.compareData()
      // 1、如果在草稿状态下，则进行路由守卫
      if (this.documentState == 0) {
        // 2、如果页面内容不相同，则提示保存
        if (!flag) {
          setTimeout(() => {
            // 做是否离开判断？
            // 还是做是否保存判断？
            this.$confirm(
              this.$t('coupons.couponsSet.nwbc'),
              this.$t('coupons.ts'),
              {
                type: 'warning',
                closeOnClickModal: false,
                closeOnPressEscape: false
              }
            )
              .then(() => {
                // 选择确定
                // 1、选择离开
                next()
                // 2、选择保存
                // this.saveDocument('ruleForm', false, 1).then((res) => {
                //   next()
                // })
              })
              .catch(() => {
                // 选择取消
                // this.$message({
                //   message: '',
                //   type: 'info'
                // })
                // next()
              })
          }, 200)
        } else {
          // 如果相同，则直接进入下个页面
          next()
        }
      } else {
        next()
      }
    },
    async getCouponConfigInfos() {
      const res = await getCouponConfigInfo({
        api: 'plugin.coupon.Admincoupon.GetCouponConfigInfo'
      })
      console.log(res)
      let setInfo = res.data.data.data
      if (!setInfo) {
        return
      }
      this.ruleForm.switchs = setInfo.is_status
      // this.ruleForm.isShow = setInfo.is_show
      // this.ruleForm.clear_coupon = setInfo.coupon_del
      // this.ruleForm.clear_coupon_day = setInfo.coupon_day
      // this.ruleForm.clear_activity = setInfo.activity_del
      // this.ruleForm.clear_activity_day = setInfo.activity_day
      // this.ruleForm.limit_get = setInfo.limit_type
      this.ruleForm.coupons_type = setInfo.coupon_type && setInfo.coupon_type.split(',') || []
      if (this.ruleForm.coupons_type.length == 3 ) {
        this.checkAll = true
        this.isIndeterminate = false
      }
      
      if (
        this.ruleForm.coupons_type.length < 3 &&
        this.ruleForm.coupons_type.length > 0
      ) {
        this.checkAll = false
        this.isIndeterminate = true
      } 
      
      sessionStorage.setItem('ruleForm_coupon', JSON.stringify(this.ruleForm))
    },

    handleCheckAllChange(val) {
      console.log(this.checkAll)
      this.ruleForm.coupons_type = val
        ? this.couponsTypeList.map(item => {
          return item.key
        })
        : []
      this.isIndeterminate = false
    },

    handleCheckedCitiesChange(value) {
      let checkedCount = value.length
      this.checkAll = checkedCount === this.couponsTypeList.length
      this.isIndeterminate =
        checkedCount > 0 && checkedCount < this.couponsTypeList.length
      if (this.ruleForm.coupons_type.length == 3) {
        this.checkAll = true
      }
      if (
        this.ruleForm.coupons_type.length < 3 &&
        this.ruleForm.coupons_type.length > 0
      ) {
        this.checkAll = false
        this.isIndeterminate = true
      }
    },
    //获取所有优惠券
    getCoupons() {
      allCoupons({
        api: 'plugin.coupon.Admincoupon.GetCouponTypeList'
      }).then(res => {
        this.couponsTypeList = res.data.data.typeList
      })
    },
    back() {
      this.$router.push({
        path: '/plug_ins/plugInsSet/plugInsList'
      })
    },
    submitForm() {
      console.log(this.fangdou)
      if (!this.fangdou) {
        return
      }
      this.fangdou = false
      // if(this.ruleForm.clear_coupon == 1 && this.ruleForm.clear_coupon_day === '') {
      //     this.$message({
      //         type: 'error',
      //         message: '请输入优惠券过期删除天数!',
      //         offset: 100
      //     })
      //     return
      // }
      // if(this.ruleForm.clear_activity == 1 && this.ruleForm.clear_activity_day === '') {
      //     this.$message({
      //         type: 'error',
      //         message: '请输入优惠券活动过期删除天数!',
      //         offset: 100
      //     })
      //     return
      // }
      // if(this.ruleForm.clear_coupon == 1 && this.ruleForm.clear_coupon_day === '0') {
      //     this.$message({
      //         type: 'error',
      //         message: '优惠券过期删除天数不能为零!',
      //         offset: 100
      //     })
      //     return
      // }
      // if(this.ruleForm.clear_activity == 1 && this.ruleForm.clear_activity_day === '0') {
      //     this.$message({
      //         type: 'error',
      //         message: '优惠券活动过期删除天数不能为零!',
      //         offset: 100
      //     })
      //     return
      // }
      addCouponConfig({
        api: 'plugin.coupon.Admincoupon.AddCouponConfig',
        isOpen: this.ruleForm.switchs || 0,
        isAutoClearCoupon: this.ruleForm.clear_coupon
          ? this.ruleForm.clear_coupon
          : '',
        autoClearCouponDay: this.ruleForm.clear_coupon_day
          ? this.ruleForm.clear_coupon_day
          : '',
        isAutoClearaAtivity: this.ruleForm.clear_activity
          ? this.ruleForm.clear_activity
          : '',
        autoClearaAtivityDay: this.ruleForm.clear_activity_day
          ? this.ruleForm.clear_activity_day
          : '',
        limitType: this.ruleForm.limit_get ? this.ruleForm.limit_get : '',
        // isShow:this.ruleForm.isShow,
        couponType: this.ruleForm.coupons_type.join(',')
      }).then(res => {
        if (res.data.code == '200') {
          this.getCouponConfigInfos()
          this.$message({
            type: 'success',
            message: '操作成功',
            offset: 102
          })
          setTimeout(() => {
            // this.$router.go(-1)
            this.$router.push({
              path: '/plug_ins/plugInsSet/plugInsList'
            })
          }, 1000)
        }
        setTimeout(() => {
          this.fangdou = true
        }, 1500)
      })
    }
  }
}
