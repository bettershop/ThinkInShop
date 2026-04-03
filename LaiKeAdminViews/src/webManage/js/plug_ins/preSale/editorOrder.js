import { editOrderView, saveEditOrder,orderDetailsInfo } from '@/api/order/orderList'
import cascade from '@/api/publics/cascade'

export default {
    name: 'editorOrder',
    data() {
        return {
            dataInfo: null,
            is_refund: false,
            noteList: [],
            productInfo:[],
            consigneeInfo:null,  //地址
            ruleForm: {
                status: null,
                name: '',
                mobile: '',
                sheng: '',
                shi: '',
                xian: '',
                r_address: '',
                remarks: '',
                pay_price: '',
            },

            rules:{
                name:[{required: true, message: '请填写收货人', trigger: 'blur'}],
                mobile:[{required: true, validator: (rule, value, callback) => {
                    if (!value) {
                      callback(new Error('请输入联系方式'))
                    } else {
                      const reg = /^1[3|4|5|6|7|8][0-9]\d{8}$/
                      if (reg.test(value)) {
                        callback()
                      } else {
                        return callback(new Error('请输入正确的电话'))
                      }
                    }
                  }, trigger: 'blur'}],
                xian:[{required: true, message: '请选择联系地址', trigger: 'change'}],
                r_address:[{required: true, message: '请填写详细地址', trigger: 'blur'}]
            },

            stateList: [
                {
                    value: '0',
                    label: '待付款'
                },
                {
                    value: '1',
                    label: '待发货'
                }
            ],// 订单状态

            //省市级联集
            shengList: {},
            shiList: {},
            xianList: {},
            isTag: true,

            list: ''
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
            const res = await orderDetailsInfo({
                api: 'plugin.presell.AdminPreSell.orderDetail',
                orderNo: this.$route.query.no
            })
            console.log(res.data.data,"shujujuu1");
            this.list = res.data.data.productInfo[0]
            this.dataInfo = res.data.data.essentialInfo      //预售
            this.productInfo =res.data.data.productInfo
            this.consigneeInfo = res.data.data.consigneeInfo    //地址
            this.is_refund=this.productInfo[0].isRefund==1 ? true:false //1退款成功
            this.ruleForm.name = this.consigneeInfo.consignee
            this.ruleForm.mobile = this.consigneeInfo.phone
            this.ruleForm.sheng = this.consigneeInfo.sheng
            this.ruleForm.shi = this.consigneeInfo.shi
            this.ruleForm.xian =  this.consigneeInfo.xian,
            this.ruleForm.r_address = this.consigneeInfo.address
            this.ruleForm.remarks = this.consigneeInfo.remarks

            if(this.dataInfo.statusDesc == '待付款' ) {
                this.ruleForm.pay_price = this.productInfo[0].z_price
            }
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
            let me = this
            this.$refs[formName].validate(async (valid) => {
            console.log(this.ruleForm);
            if (valid) {
                try {
                    if(!this.isTag) {
                        return
                    }
                    if(this.ruleForm.remarks.length > 100) {
                        this.$message({
                            message: '订单备注长度不能大于100个字符',
                            type: 'error',
                            offset:100
                        })
                        return
                    }
                    saveEditOrder({
                        api: 'plugin.presell.AdminPreSell.saveEditOrder',
                        orderNo: this.$route.query.no,
                        userName: this.ruleForm.name,
                        tel: this.ruleForm.mobile,
                        shen: this.ruleForm.sheng,
                        shi: this.ruleForm.shi,
                        xian: this.ruleForm.xian,
                        address: this.ruleForm.r_address,
                        remarks: this.ruleForm.remarks,
                        orderStatus: this.dataInfo.statusDesc == '待付款' ? this.ruleForm.status : null,
                        orderAmt: this.dataInfo.statusDesc == '待付款' ? this.ruleForm.pay_price : null,

                    }).then(res => {
                        console.log(res);
                        if(res.data.code == '200') {
                            this.isTag = false
                            setTimeout(function() {
                                me.isTag = true
                            },500)
                            this.$message({
                                message: this.$t('zdata.bjcg'),
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
