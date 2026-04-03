import { editOrderView, saveEditOrder } from '@/api/order/orderList'
import cascade from '@/api/publics/cascade'

export default {
    name: 'editorOrder',
    data() {
        return {
            dataInfo: null,
            goodsTables: [],
            totleInfo: null,

            ruleForm: {
                status: null,
                name: '',
                mobile: '',
                sheng: '',
                shi: '',
                xian: '',
                r_address: '',
                // remarks: '',
                pay_price: '',

                totalIntegral: ''
            },

            rules:{
                name:[{required: true, message: this.$t('integralMall.orderDetails.qtxshr'), trigger: 'blur'}],
                mobile:[{required: true, validator: (rule, value, callback) => {
                    if (!value) {
                      callback(new Error(this.$t('integralMall.orderDetails.qsrlxfs')))
                    } else {
                      const reg = /^1[3|4|5|6|7|8][0-9]\d{8}$/
                      if (reg.test(value)) {
                        callback()
                      } else {
                        return callback(new Error(this.$t('integralMall.orderDetails.qsrzq')))
                      }
                    }
                  }, trigger: 'blur'}],
                xian:[{required: true, message:  this.$t('integralMall.orderDetails.qxzlxdz'), trigger: 'change'}],
                r_address:[{required: true, message:  this.$t('integralMall.orderDetails.qtxxxdz'), trigger: 'blur'}]
            },

            stateList: [
                {
                    value: '0',
                    label:  this.$t('integralMall.orderDetails.dfk')
                },
                {
                    value: '1',
                    label:  this.$t('integralMall.orderDetails.dfh')
                }
            ],// 订单状态

            //省市级联集
            shengList: {},
            shiList: {},
            xianList: {}
        }
    },

    created() {
        this.getSheng()
        this.editOrderViews().then(() => {
            this.cascadeAddress();
        })
    },

    methods: {
        async editOrderViews() {
            const res = await editOrderView({
                api: 'plugin.integral.order.editOrderView',
                sNo: this.$route.query.no
            })
            console.log(res);
            this.dataInfo = res.data.data.data
            this.goodsTables = res.data.data.detail
            this.totleInfo = res.data.data

            this.ruleForm.name = res.data.data.data.name,
            this.ruleForm.mobile = res.data.data.data.mobile,
            this.ruleForm.sheng = res.data.data.data.sheng,
            this.ruleForm.shi = res.data.data.data.shi,
            this.ruleForm.xian = res.data.data.data.xian,
            this.ruleForm.r_address = res.data.data.data.r_address
            // this.ruleForm.remarks = Object.keys(res.data.data.data.remarks).length === 0 ? '' : ''

            if(this.dataInfo.status01 ==  0) {
                this.ruleForm.pay_price = this.totleInfo.pay_price
                this.ruleForm.status = '0'
            }
            this.totalIntegral = this.goodsTables[0].after_discount * this.goodsTables[0].num
        },



        // 获取省级
        async getSheng() {
            const res = await cascade.getSheng()
            this.shengList = res.data.data
        },

        // 获取市级
        async getShi(sid, flag) {
            const res = await cascade.getShi(sid)
            this.shiList = res.data.data
            if (!flag) {
                this.ruleForm.shi = "";
                this.ruleForm.xian = "";
            }
        },

        // 获取县级
        async getXian(sid, flag) {
            const res = await cascade.getXian(sid)
            this.xianList = res.data.data
            if (!flag) {
                this.ruleForm.xian = "";
            }
        },

        //省市级联回显
        async cascadeAddress() {
            //省市级联
            for (const sheng of this.shengList) {
            if (sheng.districtName === this.ruleForm.sheng) {
                await this.getShi(sheng.id, true);
                for (const shi of this.shiList) {
                if (shi.districtName === this.ruleForm.shi) {
                    await this.getXian(shi.id, true);
                    break;
                }
                }
                break;
            }
            }
        },

        submitForm(formName) {
            this.$refs[formName].validate(async (valid) => {
              console.log(this.ruleForm,this.dataInfo,this.dataInfo.status01,this.dataInfo.status01==0,this.ruleForm.status);
              if (valid) {
                try {
                    let data={
                        api: 'plugin.integral.order.saveEditOrder',
                        orderNo: this.$route.query.no,
                        userName: this.ruleForm.name,
                        tel: this.ruleForm.mobile,
                        shen: this.ruleForm.sheng,
                        shi: this.ruleForm.shi,
                        xian: this.ruleForm.xian,
                        address: this.ruleForm.r_address,
                        orderStatus: this.ruleForm.status,
                        // orderAmt: this.dataInfo.status01 ==  0 ? this.ruleForm.pay_price : null,
                        // remarks: this.ruleForm.remarks,

                    }
                    console.log(data)
                    saveEditOrder(data).then(res => {
                        console.log(res);
                        if(res.data.code == '200') {
                            this.$message({
                                message:  this.$t('zdata.bjcg'),
                                type: 'success',
                                offset:100
                            })
                            this.$router.go(-1)
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
