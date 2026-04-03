import { getRefundList,examine } from '@/api/order/salesReturn'
import { deliveryView } from '@/api/order/orderList'
import ErrorImg from '@/assets/images/default_picture.png'
export default {
    name: 'afterSaleDetails',

    data() {
        return {
            header: {
                'background-color': '#F4F7F9',
                'font-weight': 'bold',
                'border-bottom': '1px solid #E9ECEF',
            },

            goodsDate: [],
            totle_price: '',

            applyInfo: {},
            rdata: [],
            sales_imgs: [],

            id: null,
            toggle: null,
            p_price:'',
            type: '',
            r_type: '',//订单状态 0:审核中 1:同意并让用户寄回, 2:拒绝 退货退款(没有收到回寄商品) 3:用户已快递 4:收到寄回商品 同意并退款
                       // 5:拒绝并退回商品(拒绝回寄的商品 这个时候需要人工介入) 8:拒绝 退款 9:同意并退款 10:拒绝 售后(用户还未寄回商品)
                       // 11:同意并且寄回商品 12:售后结束 13:最终状态-人工审核成功
            re_type: '',//退款类型 1:退货退款  2:退款 3:换货
            prompt:'',
            courierList: [],

            // 拒绝弹框数据
            dialogVisible3: false,
            ruleForm1: {
                reason: '',
            },
            rules: {
                reason: [
                    { required: true, message: this.$t('seckill.afterSaleDetails.qtxjjly'), trigger: 'blur' }
                ],
            },

            // 退换物流弹框数据
            dialogVisible4: false,
            ruleForm2: {
                kuaidi_name: null,
                kuaidi_no: null,
            },
            rules1: {
                kuaidi_name: [
                    { required: true, message: this.$t('seckill.afterSaleDetails.qxzkd'), trigger: 'change' }
                ],
                kuaidi_no: [
                    { required: true, message: this.$t('seckill.afterSaleDetails.qtxkdd'), trigger: 'blur' }
                ],
            },

            // 退款弹框
            dialogVisible2:false,
            ruleForm:{
                y_refund:'',
                s_refund:'',
            },
            examineInfo: [],
            examineResult: '',
        }
    },

    created() {
        this.getRefundLists()
        this.deliveryViews()
    },

    methods: {
        handleErrorImg(e){
          console.log('图片报错了', e.target.src)
          e.target.src = ErrorImg
        },

        handleClose2(done) {
            this.dialogVisible2 = false
            this.$refs['ruleForm'].clearValidate()
        },

        submitForm4(formName) {
            this.$refs[formName].validate(async (valid) => {
              console.log(this.ruleForm);
              if (valid) {
                try {
                    examine({
                        api: 'plugin.presell.AdminPreSell.examine',
                        id: this.id,
                        type: this.type,
                        price: this.ruleForm.s_refund,
                        expressId: this.ruleForm2.kuaidi_name,
                        courierNum: this.ruleForm2.kuaidi_no,

                    }).then(res => {
                        console.log(res,7777);
                        if(res.data.code == '200') {
                            this.handleClose2()
                            this.getRefundLists()
                            this.$message({
                                type: 'success',
                                message: this.$t('seckill.cg'),
                                offset: 100
                            })
                            this.dialogVisible4=false
                        }
                    })
                } catch (error) {
                  this.$message({
                    message: error.message,
                    type: 'error',
                    showClose: true
                  })
                }
              } else {
                console.log('error submit!!');
                return false;
              }
            });
        },

        async getRefundLists() {
            const res = await getRefundList({
                api: 'plugin.presell.AdminPreSell.getRefundList',
                id: this.$route.query.id
            })
            if(this.goodsDate.length == 0) {
                this.goodsDate.push(res.data.data.list)
            }
            this.totle_price =res.data.data.list.total
            this.applyInfo =res.data.data.list
            this.rdata = res.data.data.rdata
            this.sales_imgs = res.data.data.imgs
            this.r_type =res.data.data.list.r_type
            this.re_type= res.data.data.list.re_type
            this.id = res.data.data.list.id
            this.prompt =res.data.data.list.prompt
            this.p_price= res.data.data.list.p_price
            this.re_money = res.data.data.list.re_apply_money
            this.examineInfo = res.data.data.examineInfo
            this.examineResult = res.data.data.examineInfo.length ? res.data.data.examineInfo[0].examineResult : ''
            console.log(this.applyInfo,999999);
        },

        async deliveryViews() {
            const res = await deliveryView({
                api: 'admin.order.deliveryView',
            })
            this.courierList = res.data.data.express.list
        },

        /**
         * 订单审核
         * @param {Object} toggle 1通过 2拒绝
         */
        dialogShow2(value) {
          switch (value){
            case 1:
              //审核通过
              this._shTG()
              break
            case 2://审核拒绝
              this._shJJ()
              break
            default:
              console.log('订单审核状态错误！', value)
          }
        },
        /**
         * 订单审核通过
         */
        _shTG(){

          if(this.re_type == 3 && this.r_type == 3) {
              this.dialogVisible4 = true
              this.ruleForm.s_refund = this.totle_price
              console.log(this.re_money,this.totle_price,'this.totle_price');
              this.type = 11
              return
          }
          if(this.r_type == 0 || this.r_type == 3) {
              this.type = this.re_type == 2 ? 9 : this.r_type == 3 && this.r_type!=11 ? 4 :1
              let tips = this.re_type!= 2 && this.r_type!=3 && this.r_type!=11 ? '确定要通过该用户的申请,并让用户寄回?' :
              this.r_type == '3' && this.r_type != '11' ? '确定已到货并退款到用户?' : '确定要通过该用户的申请?'
              if((this.re_type == 1 && this.r_type == 3) || (this.re_type == 2 && this.r_type == 0)){
                  this.ruleForm.y_refund = this.re_money
                  this.ruleForm.s_refund = this.re_money
                  this.dialogVisible2 = true
                  return
              }
              this._shenhe(tips)
          } else {
              this.dialogVisible4 = this.re_type == 3 && this.r_type != 11? true :false
              this.type = 11
          }
        },
        /**
         * 通过审核
         */
        _shenhe(tips){
          this.$confirm(tips, this.$t('seckill.ts'), {
              confirmButtonText: this.$t('seckill.okk'),
              cancelButtonText: this.$t('seckill.ccel'),
              type: 'warning'
            }).then(() => {
              console.log('data~', this.id, this.type, this.re_money);
              examine({
                  //api: 'plugin.presell.MchPreSell.examine',
                  api: 'plugin.presell.AdminPreSell.examine',
                  id: this.id,
                  type: this.type,
                  price: this.re_money
              }).then(res => {
                  if(res.data.code == '200') {
                      this.dialogVisible2 = false
                      this.getRefundLists()
                      this.$message({
                          type: 'success',
                          message: this.$t('zdata.shcg'),
                          offset: 100
                      })
                      this.getRefundLists()
                  }
              })
            }).catch(() => {});
        },
        /**
         * 订单审核拒绝
         */
        _shJJ(){
          //审核中
          if (this.r_type == '0') {
            //换货
            if (this.re_type == '3') {
              this.type = 10
            }
            //退款
            else if (this.re_type == '2') {
              this.type = 8
            }
            //退货退款
            else if(this.re_type == '1'){
              this.type = 10
            }else{
              this.type = 2
            }
          }
          //用户已快递
          if (this.r_type == '3') {
            //退货退款｜｜换货
            if (this.re_type == '3' || this.re_type == '1'){
              this.type = 5
            }
          }
          //拒绝并退回商品
          if (this.r_type == '5') {
            this.type = 14
          }
          //拒绝 填写理由弹窗
          this.dialogVisible3 = true
        },
        /**
         * 拒绝填写理由 --》取消拒绝
         * @param {Object} done
         */
        handleClose3(done) {
            this.dialogVisible3 = false
            this.$refs['ruleForm1'].clearValidate()
        },
        /**
         * 拒绝填写理由 --》确认拒绝
         * @param {Object} formName
         */
        submitForm2(formName) {
            this.$refs[formName].validate(async (valid) => {
              console.log(this.ruleForm1);
              if (valid) {
                try {
                    examine({
                        api: 'plugin.presell.AdminPreSell.examine',
                        id: this.id,
                        text: this.ruleForm1.reason,
                        type: this.type,
                        price: this.re_money
                    }).then(res => {
                        console.log(res,111);
                        if(res.data.code == '200') {
                            this.dialogVisible3 = false
                            this.$message({
                                type: 'success',
                                message: this.$t('seckill.cg'),
                                offset: 100
                            })
                            this.getRefundLists()
                        }
                    })
                } catch (error) {
                  this.$message({
                    message: error.message,
                    type: 'error',
                    showClose: true
                  })
                }
              } else {
                console.log('error submit!!');
                return false;
              }
            });
        },

        dialogShow5() {
            this.dialogVisible2 = true
            this.type = 13
            this.ruleForm.y_refund = this.re_money
            this.ruleForm.s_refund = this.re_money
            this.$refs['ruleForm'].clearValidate()
        },

        // 填写退还物流
        handleClose4(done) {
            this.dialogVisible4 = false
            this.$refs['ruleForm2'].clearValidate()
        },

        submitForm3(formName) {
            this.$refs[formName].validate(async (valid) => {
              console.log(this.ruleForm2);
              //人工退款逻辑，如果输入 实退金额大于应退金额 默认用应退金额
              let tuikuan_moneny = 0
              if(this.ruleForm.s_refund){
                tuikuan_moneny = this.ruleForm.s_refund<=this.ruleForm.y_refund?this.ruleForm.s_refund:this.ruleForm.y_refund
              } else {
                tuikuan_moneny = this.re_money
              }
              if (valid) {
                try {
                    examine({
                        api: 'plugin.presell.AdminPreSell.examine',
                        id: this.id,
                        type: this.type,
                        // expressId: this.ruleForm2.kuaidi_name,
                        // courierNum: this.ruleForm2.kuaidi_no,
                        price: tuikuan_moneny
                    }).then(res => {
                        console.log(res,111);
                        if(res.data.code == '200') {
                            this.dialogVisible4 = false
                            this.dialogVisible2 = false
                            this.$message({
                                type: 'success',
                                message: this.$t('seckill.cg'),
                                offset: 100
                            })
                            this.getRefundLists()
                        }
                    })
                } catch (error) {
                  this.$message({
                    message: error.message,
                    type: 'error',
                    showClose: true
                  })
                }
              } else {
                console.log('error submit!!');
                return false;
              }
            });
        },


    }
}
