import { getRefundList, examine } from '@/api/order/salesReturn'
import { deliveryView } from '@/api/order/orderList'
export default {
  name: 'salesReturnDetails',

  data() {
    return {
      header: {
        'background-color': '#F4F7F9',
        'font-weight': 'bold',
        'border-bottom': '1px solid #E9ECEF'
      },

      goodsDate: [],
      totle_price: '',
      applyInfo: {},
      rdata: [],
      sales_imgs: [],
      mydata: '',
      toggle: '',
      ruleForm: {
        y_refund: '',
        s_refund: '',
        kuaidi_no: '',
        kuaidi_name: '',
        reason: ''
      },
      type: '',
      courierList: [],
      dialogVisible2: false,

      examineInfo: '',
      falg: true
    }
  },

  created() {
    if (this.$route.query.id) {
      getRefundList({
        api: 'plugin.group.AdminGroupOrder.GetRefundById',
        id: this.$route.query.id
      }).then(res => {
        this.examineInfo = res.data.data.examineInfo
        this.goodsDate.push(res.data.data.list)
        this.totle_price = res.data.data.list.total
        this.applyInfo = res.data.data.list
        this.rdata = res.data.data.rdata
        this.sales_imgs = res.data.data.imgs
      })
    } else {
      this.getRefundLists()
    }

    this.deliveryViews()
    this.mydata = this.$route.query
    console.log(this.mydata, 'this.mydata', this.mydata.type)
  },

  methods: {
    async getRefundLists() {
      console.log('this.$route.query', this.$route.query)

      const res = await getRefundList({
        api: 'plugin.group.AdminGroupOrder.GetRefundById',
        id: this.$route.query.value.id
      })
      console.log(res)
      this.examineInfo = res.data.data.examineInfo
      this.goodsDate.push(res.data.data.list)
      console.log('goodsDate', this.goodsDate)

      this.totle_price = res.data.data.list.total
      this.applyInfo = res.data.data.list
      this.rdata = res.data.data.rdata
      this.sales_imgs = res.data.data.imgs
    },
    // 弹框方法
    dialogShow2(value, toggle) {
      console.log(value)
      this.dialogVisible2 = true
      this.id = value.id
      this.sNo = value.sNo
      this.toggle = toggle
      this.ruleForm.y_refund = ''
      this.ruleForm.s_refund = ''
      this.ruleForm.reason = ''
      this.ruleForm.kuaidi_name = ''
      this.ruleForm.kuaidi_no = ''

      if (this.toggle == 1) {
        this.ruleForm.y_refund = value.re_money
        this.ruleForm.s_refund = value.re_money
        if (value.re_type == 2) {
          this.type = 9
        } else if (value.re_type == 1) {
          this.type = 4
        }
        if (value.r_type == 5) {
          this.type = 13
        }
      } else if (this.toggle == 3) {
        if (value.re_type == 1) {
          this.type = 3
        }
        if (value.re_type == 3) {
          this.type = 11
        }
      } else if (this.toggle == 2) {
        if (value.r_type == '0') {
          if (value.re_type == '3') {
            this.type = 10
          } else if (value.re_type == '2') {
            this.type = 8
          } else {
            this.type = 2
          }
        } else if (value.r_type == '3') {
          if (value.re_type == '3' || value.re_type == '1') this.type = 5
        } else if (value.r_type == '5') {
          this.type = 14
        }
      }

      console.log(
        'r_type:',
        value.r_type,
        ',re_type:',
        value.re_type,
        ',type:',
        this.type,
        ',toggle:',
        this.toggle
      )
    },
    async deliveryViews() {
      const res = await deliveryView({
        api: 'plugin.group.AdminGroupOrder.DeliveryView'
      })
      this.courierList = res.data.data.express.list
    },
    handleClose2(done) {
      this.dialogVisible2 = false
      this.id = null
      this.toggle = null
      this.$refs['ruleForm'].clearValidate()
    },
    // 通过
    submitForm1(value, toggle) {
      this.id = value.id
      this.sNo = value.sNo
      this.toggle = toggle

      if (value.r_type == '0') {
        if (value.re_type == '1') {
          this.type = 1
          this.content = '确定要通过该用户的申请,并让用户寄回?'
        } else if (value.re_type == '3') {
          this.type = 1
          this.content = '确定要通过该用户的申请,并让用户寄回?'
        }
      }
      if (!this.falg) {
        return
      }
      this.falg = false
      this.$confirm(this.content, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        examine({
          api: 'plugin.group.AdminGroupOrder.Examine',
          id: this.id,
          type: this.type,
          price: this.ruleForm.s_refund ? this.ruleForm.s_refund : 0,
          sNo: this.sNo
        }).then(res => {
          console.log(res)
          if (res.data.code == '200') {
            this.$router.go(-1)
            this.$message({
              type: 'success',
              message: '成功!',
              offset: 100
            })
          }
        }).finally(() => {
          setTimeout(() => {
            this.falg = true
          }, 1500)
        })
      })
    },
    submitForm2(formName) {
      this.$refs[formName].validate(async valid => {
        console.log(this.ruleForm)
        if (valid) {
          try {
            if (this.toggle === 1 || this.toggle == 3) {
              if (this.type == 4 || this.type == 9) {
                if (!this.falg) {
                  return
                }
                this.falg = false
                examine({
                  api: 'plugin.group.AdminGroupOrder.Examine',
                  id: this.id,
                  type: this.type,
                  price: this.ruleForm.s_refund ? this.ruleForm.s_refund : 0,
                  sNo: this.sNo
                }).then(res => {
                  console.log(res)
                  if (res.data.code == '200') {
                    this.dialogVisible2 = false
                    this.$router.go(-1)
                    this.$message({
                      type: 'success',
                      message: '成功!',
                      offset: 100
                    })
                  }
                }).finally(() => {
                  setTimeout(() => {
                    this.falg = true
                  }, 1500)
                })
              } else if (this.type == 11) {
                if (
                  this.ruleForm.kuaidi_no.length < 10 ||
                  this.ruleForm.kuaidi_no.length > 30
                ) {
                  this.$message({
                    message: '快递单号输出格式有误，长度必须大于等于10小于30哦',
                    type: 'warning',
                    offset: 100
                  })
                  return
                }
                examine({
                  api: 'plugin.group.AdminGroupOrder.Examine',
                  id: this.id,
                  type: this.type,
                  price: this.ruleForm.s_refund ? this.ruleForm.s_refund : 0,
                  expressId: this.ruleForm.kuaidi_name,
                  courierNum: this.ruleForm.kuaidi_no
                }).then(res => {
                  console.log(res)
                  if (res.data.code == '200') {
                    this.dialogVisible2 = false
                    this.$router.go(-1)
                    this.$message({
                      type: 'success',
                      message: '成功!',
                      offset: 100
                    })
                  }
                })
              } else {
                examine({
                  api: 'plugin.group.AdminGroupOrder.Examine',
                  id: this.id,
                  price: this.ruleForm.s_refund ? this.ruleForm.s_refund : 0,
                  type: this.type
                }).then(res => {
                  console.log(res)
                  if (res.data.code == '200') {
                    this.dialogVisible2 = false
                    this.$router.go(-1)
                    this.$message({
                      type: 'success',
                      message: '成功!',
                      offset: 100
                    })
                  }
                })
              }
            } else {
              if (this.ruleForm.reason == '') {
                this.$message({
                  type: 'error',
                  message: '请填写理由!',
                  offset: 100
                })
                return
              }
              if (this.ruleForm.reason.length > 50) {
                this.$message({
                  type: 'error',
                  message: '理由不能大于50个字符!',
                  offset: 100
                })
                return
              }
              examine({
                api: 'plugin.group.AdminGroupOrder.Examine',
                id: this.id,
                text: this.ruleForm.reason,
                type: this.type
              }).then(res => {
                console.log(res)
                if (res.data.code == '200') {
                  this.dialogVisible2 = false
                  this.$router.go(-1)
                  this.$message({
                    type: 'success',
                    message: '成功!',
                    offset: 100
                  })
                }
              })
            }
          } catch (error) {
            this.$message({
              message: error.message,
              type: 'error',
              showClose: true
            })
          }
        } else {
          console.log('error submit!!')
          return false
        }
      })
    }
  }
}
