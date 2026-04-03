import { getRefundList,examine } from '@/api/order/salesReturn'
import { deliveryView } from '@/api/order/orderList'
export default {
    name: 'salesReturnDetails',

    data() {
        return {
          laikeCurrencySymbol:'￥',
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
            order_currency_code:'CNY',
            order_currency_symbol:'￥',
            order_exchange_rate:'1',
            mydata:'',
            toggle:'',
            ruleForm:{
                y_refund:'',
                s_refund:'',
                kuaidi_no:'',
                kuaidi_name:'',
                reason:'',
            },
            type:'',
            courierList: [],
            dialogVisible2:false,
            toggleTitle: '',
            examineInfo:'',
            fangdou:true,
            orderType:'',//0为实物 2无需线下核销 3需要线下核销
        }
    },

    created() {
      this.laikeCurrencySymbol = this.LaiKeCommon.getDefaultCurrencySymbol();
        if(this.$route.query.id) {
            getRefundList({
                api: 'admin.order.getRefundList',
                orderId: this.$route.query.id
            }).then(res => {
                this.examineInfo=res.data.data.examineInfo
                this.goodsDate.push(res.data.data.list)
                this.totle_price = res.data.data.list.total
                this.applyInfo = res.data.data.list
                this.rdata = res.data.data.rdata
                this.sales_imgs = res.data.data.imgs
                this.orderType = res.data.data.type
            })
            this.mydata.type=2
        } else {
            if(this.$route.query.params){
                this.mydata=JSON.parse(this.$route.query.params)
                console.log(this.mydata,'this.mydata',this.mydata.type)

                //货币
                this.order_currency_code = this.mydata.value.currency_code;
                this.order_currency_symbol = this.mydata.value.currency_symbol;
                this.order_exchange_rate = this.mydata.value.exchange_rate;
            }

            this.getRefundLists()
        }
        this.deliveryViews()
    },

    methods: {

        async getRefundLists() {
            const res = await getRefundList({
                api: 'admin.order.getRefundList',
                id: this.mydata.value.id
            })
            this.examineInfo=res.data.data.examineInfo
            this.goodsDate.push(res.data.data.list)
            this.totle_price = res.data.data.list.total
            this.applyInfo = res.data.data.list
            this.rdata = res.data.data.rdata
            this.sales_imgs = res.data.data.imgs
            this.orderType = res.data.data.type
            this.toggleTitle = this.applyInfo.re_type == 1 ? this.$t('salesReturnDetails.qtxrh') : this.applyInfo.re_type == 2 ? this.$t('salesReturnDetails.qtxje') : this.$t('salesReturnDetails.qhh')
        },
        // 弹框方法
        dialogShow2(value,toggle) {
            console.log(value);
            this.dialogVisible2 = true
            this.id = value.id
            this.sNo=value.sNo
            this.toggle = toggle
            this.ruleForm.y_refund = ''
            this.ruleForm.s_refund = ''
            this.ruleForm.reason = ''
            this.ruleForm.kuaidi_name = ''
            this.ruleForm.kuaidi_no = ''

            if(this.toggle == 1) {
                this.ruleForm.y_refund = this.LaiKeCommon.formatPrice(value.re_money,value.exchange_rate)
                this.ruleForm.s_refund = this.LaiKeCommon.formatPrice(value.re_money,value.exchange_rate)
                if(value.re_type==2){
                    this.type=9
                }else if(value.re_type==1){
                    this.type=4
                }
                if(value.r_type==5){
                    this.type=13
                }

            }else if(this.toggle == 3){
                if(value.re_type==1){
                    this.type=3
                }
                if(value.re_type==3){
                    this.type=11
                }
            } else if(this.toggle == 2) {
                if(value.r_type == '0'){//审核中
                    if(value.re_type == '3'){
                        this.type = 10;
                    } else if (value.re_type == '2'){
                        this.type = 8;
                    }else{
                        this.type=2
                    }

                }else if(value.r_type == '3'){//已回寄
                    if(value.re_type == '3'||value.re_type == '1')
                    this.type = 5;
                }else if(value.r_type == '5'){
                    this.type=14
                }
            }

            console.log("r_type:",value.r_type,',re_type:',value.re_type,',type:',this.type,',toggle:',this.toggle)
        },
        async deliveryViews() {
            const res = await deliveryView({
                api: 'admin.order.deliveryView',
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
        submitForm1(value,toggle) {
            this.id = value.id
            this.sNo=value.sNo
            this.toggle = toggle
            if(!this.fangdou){
                return
            }
            let price = this.ruleForm.s_refund?this.ruleForm.s_refund:0
            if(price > 0){
            price = Number(price/this.order_exchange_rate).toFixed(2);
            }
            this.fangdou=false
            if(value.r_type == '0'){
                if(value.re_type == '1'){
                    this.type = 1;
                    this.content = this.$t('salesReturnDetails.qdytg');
                }else if(value.re_type == '3'){
                    this.type = 1;
                    this.content = this.$t('salesReturnDetails.qdytg');

                }

            }


            this.$confirm(this.content, this.$t('salesReturnDetails.ts'), {
                confirmButtonText: this.$t('salesReturnDetails.okk'),
                cancelButtonText: this.$t('salesReturnDetails.ccel'),
                type: 'warning'
            }).then(() => {
                examine({
                    api: 'admin.order.examine',
                    id: this.id,
                    type: this.type,
                    price: price,
                    sNo:this.sNo,
                }).then(res => {
                    console.log(res);
                    this.fangdou=true

                    if(res.data.code == '200') {
                        this.$router.back()
                        this.$message({
                            type: 'success',
                            message: this.$t('salesReturnDetails.cg'),
                            offset: 100
                        })
                    }
                })
            })
        },
        submitForm2(formName) {
            if(!this.fangdou){
                return
            }
            this.fangdou=false
            this.$refs[formName].validate(async (valid) => {
              console.log(this.ruleForm);
              let price = this.ruleForm.s_refund?this.ruleForm.s_refund:0
              if(price > 0){
                price = Number(price/this.order_exchange_rate).toFixed(2);
              }
              if (valid) {
                try {
                    if(this.toggle === 1 || this.toggle == 3) {
                        if(this.type == 4 || this.type == 9) {
                            examine({
                                api: 'admin.order.examine',
                                id: this.id,
                                type: this.type,
                                price: price,
                                sNo:this.sNo,
                            }).then(res => {
                                console.log(res);
                                this.fangdou=true

                                if(res.data.code == '200') {
                                    this.dialogVisible2 = false
                                    this.$router.back()
                                    this.$message({
                                        type: 'success',
                                        message: this.$t('zdata.shcg'),
                                        offset: 102
                                    })
                                }
                            })
                        } else if(this.type == 11) {
                            if(this.ruleForm.kuaidi_no.length < 10 || this.ruleForm.kuaidi_no.length > 30){
                                this.$message({
                                    message:this.$t('salesReturnDetails.kddgsc'),
                                    type:'warning',
                                    offset:100
                                })
                                this.fangdou=true
                                return
                            }
                            examine({
                                api: 'admin.order.examine',
                                id: this.id,
                                type: this.type,
                                price: price,
                                expressId: this.ruleForm.kuaidi_name,
                                courierNum: this.ruleForm.kuaidi_no,
                            }).then(res => {
                                console.log(res);
                                this.fangdou=true

                                if(res.data.code == '200') {
                                    this.dialogVisible2 = false
                                    this.$router.back()
                                    this.$message({
                                        type: 'success',
                                        message: this.$t('salesReturnDetails.cg'),
                                        offset: 100
                                    })
                                }
                            })
                        }else {
                            examine({
                                api: 'admin.order.examine',
                                id: this.id,
                                price: price,
                                type: this.type
                            }).then(res => {
                                console.log(res);
                                this.fangdou=true

                                if(res.data.code == '200') {
                                    this.dialogVisible2 = false
                                    this.$router.back()
                                    this.$message({
                                        type: 'success',
                                        message: this.$t('salesReturnDetails.cg'),
                                        offset: 100
                                    })
                                }
                            })
                        }

                    } else {
                        if(this.ruleForm.reason == '') {
                            this.$message({
                                type: 'error',
                                message: this.$t('salesReturnDetails.qtxly'),
                                offset: 100
                            })
                            this.fangdou=true
                            return
                        }
                        if(this.ruleForm.reason.length > 50) {
                            this.$message({
                                type: 'error',
                                message: '理由不能大于50个字符!',
                                offset: 100
                            })
                            this.fangdou=true
                            return
                        }
                        examine({
                            api: 'admin.order.examine',
                            id: this.id,
                            text: this.ruleForm.reason,
                            type: this.type
                        }).then(res => {
                            console.log(res);
                            this.fangdou=true

                            if(res.data.code == '200') {
                                this.dialogVisible2 = false
                                this.$router.back()
                                this.$message({
                                    type: 'success',
                                    message: this.$t('salesReturnDetails.cg'),
                                    offset: 100
                                })
                            }
                        })
                    }
                } catch (error) {
                this.fangdou=true
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
