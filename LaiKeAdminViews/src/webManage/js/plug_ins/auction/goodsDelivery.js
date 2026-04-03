import { deliveryView, deliverySave } from '@/api/order/orderList'

export default {
    name: 'goodsDelivery',

    data() {
        return {
            goodsTables: [],
            goodsNum: null,
            flag: false,
            courierList: [],
            id: null,
            // 弹框数据
            dialogVisible: false,
            ruleForm2: {
                kuaidi_name: null,
                kuaidi_no: null,
            },
            rules2: {
                kuaidi_name: [
                    { required: true, message: '请填写快递名称', trigger: 'change' }
                ],
                kuaidi_no: [
                    { required: true, message: '请填写快递单号', trigger: 'blur' }
                ],
            },
            logistics_type: false, //电子面单

        }
    },

    created() {
        this.deliveryViews()
    },

    methods: {
        handleSelecChange(item) {
            console.log('当前选中的快递公司～', item)
            let obj = this.courierList.find(item2 => item2.id == item)
            this.logistics_type = obj.logistics_type
        },
        async searchExpress(sNo) {
            const res = await deliveryView({
                api: 'admin.order.GetLogistics',
                sNo
            })
            this.courierList = res.data.data.list
        },
        async deliveryViews() {
            this.searchExpress(this.$route.query.no)
            const res = await deliveryView({
                // api: 'plugin.sec.order.deliveryView',
                api: 'admin.order.deliveryView',
                sNo: this.$route.query.no
            })
            this.goodsTables = res.data.data.goods
            if (this.goodsTables && this.goodsTables.length == 1) {
                this.$refs.table.toggleAllSelection()
            }
            this.goodsNum = res.data.data.goods.num
            //this.courierList = res.data.data.express.list
            // this.id = res.data.data.goods[0].id
            this.id = res.data.data.goods.map(item => {
                return item.id
            })
        },

        handleSelectionChange(val) {
            console.log(val);
            if (val.length !== 0) {
                this.flag = true
            } else {
                this.flag = false
            }
        },

        // 弹框方法
        dialogShow() {
            if (this.flag) {
                this.dialogVisible = true
            } else {
                this.$message({
                    message: '请选择至少一款产品',
                    type: 'error',
                    offset: 100
                })
            }
        },

        handleClose(done) {
            this.dialogVisible = false
            this.$refs.ruleForm2.clearValidate()
        },

        // 发货
        determine(formName2) {
            this.$refs[formName2].validate(async (valid) => {
                console.log(this.ruleForm2);
                if (valid) {
                    try {
                        if (!this.logistics_type && (this.ruleForm2.kuaidi_no.length < 10 || this.ruleForm2.kuaidi_no.length > 30)) {
                            this.$message({
                                message: '快递单号输出格式有误，必须大于等于10哦',
                                type: 'warning',
                                offset: 100
                            })
                            return
                        } else {
                            let api = 'admin.order.UnifiedShipment'
                            let expressid = this.ruleForm2.kuaidi_name
                            let courierNum = !this.logistics_type ? this.ruleForm2.kuaidi_no : ''
                            let type = this.logistics_type ? '2' : '1'
                            let psyInfo = ''
                            let orderList = [
                                {
                                    detailId: this.id.join(','),
                                    num: 1,
                                },
                            ]
                            let list = {
                                expressid, //物流公司id
                                courierNum, //物流号
                                type,	//发货类型 1普通发货 2电子面单 3商家配送
                                psyInfo, //配送员信息
                                orderList, //订单信息 detailId订单详情id num订单商品数量
                            }
                            list = JSON.stringify(list)
                            deliverySave({ api, list }).then(res => {
                                if (res.data.code == '200') {
                                    this.$message({
                                        message: '发货成功',
                                        type: 'success',
                                        offset: 100
                                    })
                                    this.$router.go(-1)
                                }
                                console.log(res);
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
                    console.log('error submit!!');
                    return false;
                }
            });
        },
    }
}
