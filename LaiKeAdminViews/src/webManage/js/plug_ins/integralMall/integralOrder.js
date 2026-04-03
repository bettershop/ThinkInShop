import { orderList, delOrder, closeOrder } from '@/api/plug_ins/integralMall'
import { kuaidishow } from '@/api/order/orderList'
import { exports } from '@/api/export/index'
import { mixinstest } from '@/mixins/index'
import { getStorage } from '@/utils/storage'
import { getButton } from '@/api/layout/information'
import { deliveryView, deliverySave, deliverySubmission } from '@/api/order/orderList'
import ErrorImg from '@/assets/images/default_picture.png'


export default {
    name: 'integralOrder',
    mixins: [mixinstest],

    data() {
        return {
            routerList: JSON.parse(sessionStorage.getItem('tabRouter')),
            radio1: '2',
            inputInfo: {
                name: null,
                mchName: null,
                state: '1',
                date: null
            },
            stateList: [
                {
                    value: '0',
                    label: this.$t('integralOrder.dfk')
                },
                {
                    value: '1',
                    label: this.$t('integralOrder.dfh')
                },
                {
                    value: '2',
                    label: this.$t('integralOrder.dsh')
                },
                {
                    value: '5',
                    label: this.$t('integralOrder.ywc')
                },
                {
                    value: '7',
                    label: this.$t('integralOrder.ygb')
                }
            ],// 订单状态
            logisticsList: [],
            tableData: [],
            loading: true,
            button_list: [],
            idList: [],
            idPrintList: [],
            is_disabled: true,

            // 弹框数据
            dialogVisible2: false,
            ruleForm: {
                courier_company: '',
                courier_no: '',
                logistics: ''
            },

            // table高度
            tableHeight: null,

            // 导出弹框数据
            dialogVisible: false,
            menuId: '',


            dialogVisible3: false,
            ruleForm2: {
                kuaidi_name: null,
                kuaidi_no: null,
            },
            rules2: {
                kuaidi_name: [
                    { required: true, message: this.$t('integralMall.goodsDelivery.qxzkdgs'), trigger: 'change' }
                ],
                kuaidi_no: [
                    { required: true, message: this.$t('integralMall.goodsDelivery.qtxfddh'), trigger: 'blur' }
                ],
            },
            courierList: [],
            fhid: '',
            logistics_type: false, //电子面单
        }
    },

    created() {
        if (this.$route.query.no) {
            orderList({
                api: 'plugin.integral.order.index',
                pageSize: this.pageSize,
                pageNo: this.dictionaryNum,
                keyWord: this.inputInfo.name,
                mchName: this.inputInfo.mchName,
                status: this.inputInfo.state,
                id: this.$route.query.no,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null
            }).then(res => {
                console.log(res);
                this.total = res.data.data.total
                this.tableData = res.data.data.list


                this.loading = false
                if (this.total < this.current_num) {
                    this.current_num = this.total
                }
            })
        } else {
            this.orderLists()
        }

        this.getButtons()
        this.deliveryViews()
    },

    mounted() {
        this.$nextTick(function () {
            this.getHeight()
        })
        window.addEventListener('resize', this.getHeight(), false)
    },

    methods: {
        handleSelecChange(item) {
            console.log('当前选中的快递公司～', item)
            let obj = this.courierList.find(item2 => item2.id == item)
            this.logistics_type = obj.logistics_type
        },
        // 查看快递公司
        async searchExpress(sNo) {
            const res = await deliveryView({
                api: 'admin.order.GetLogistics',
                sNo
            })
            this.courierList = res.data.data.list
        },
        handleSelectAll() {
            if (this.tableData.length == 0) {
                this.$refs.table.clearSelection()
            }
        },
        // 图片错误处理
        handleErrorImg(e) {
            console.log('图片报错了', e.target.src);
            e.target.src = ErrorImg
        },
        async deliveryViews() {

            const res = await deliveryView({
                api: 'plugin.integral.order.deliveryView',
                sNo: this.fhid.sNo
            })
            // this.goodsTables = res.data.data.goods
            // this.goodsNum = res.data.data.goods.num
            // this.courierList = res.data.data.express.list
            // this.id = res.data.data.goods[0].id
            // this.id = res.data.data.goods.map(item => {
            //     return item.id
            // })
        },



        handleClose3(done) {
            this.dialogVisible3 = false
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
                                message: this.$t('integralMall.goodsDelivery.dy10'),
                                type: 'warning',
                                offset: 100
                            })
                            return
                        } else {
                            deliverySubmission({
                                api: "admin.order.UnifiedShipment",
                                list: JSON.stringify({
                                    // 物流公司id
                                    expressid: this.ruleForm2.kuaidi_name,
                                    // 物流单号  选择电子面单 不传 物流单号
                                    courierNum: !this.logistics_type ? this.ruleForm2.kuaidi_no : '',
                                    //发货类型 1普通发货 2电子面单 3商家配送
                                    type: this.logistics_type ? 2 : 1,
                                    // 订单信息
                                    orderList: [{ 'detailId': this.fhid.oid, 'num': this.fhid.num }],
                                    // 配送员信息
                                    psyInfo: {}
                                })

                            }).then(res => {
                                if (res.data.code == '200') {
                                    this.$message({
                                        message: this.$t('integralMall.goodsDelivery.cg'),
                                        type: 'success',
                                        offset: 100

                                    })
                                    this.orderLists()
                                    this.dialogVisible3 = false

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
        rowstyles({ row, rowIndex }) {
            let styleJson = {
                "border": "none"
            };
            return styleJson
        },
        //获取按纽权限
        async getButtons() {
            let route = getStorage('route')
            route.forEach(item => {
                if (item.path == 'integralMall') {
                    item.children.forEach(e => {
                        if (e.path == 'integralOrder') {
                            return this.menuId = e.id
                        }
                    })
                }
            });
            let buttonList = await getButton({
                api: 'saas.role.getButton',
                menuId: this.menuId,
            })
            this.button_list = buttonList.data.data
        },
        async orderLists() {
            const res = await orderList({
                api: 'plugin.integral.order.index',
                pageSize: this.pageSize,
                pageNo: this.dictionaryNum,
                keyWord: this.inputInfo.name,
                mchName: this.inputInfo.mchName,
                status: this.inputInfo.state,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null
            })
            console.log(res);
            this.total = res.data.data.total
            this.tableData = res.data.data.list

            this.$store.dispatch("orderNum/getOrderInCount", this.total);
            this.loading = false
            if (this.total < this.current_num) {
                this.current_num = this.total
            }
        },
        // 获取table高度
        getHeight() {
            this.tableHeight = this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
        },

        // 重置
        reset() {
            this.inputInfo.name = null
            this.inputInfo.mchName = null
            this.inputInfo.state = null
            this.inputInfo.date = null
        },

        // 查询
        demand() {
            // this.showPagebox = false
            this.currpage = 1
            this.current_num = 10
            this.loading = true
            this.dictionaryNum = 1
            this.orderLists().then(() => {
                this.loading = false
                // if(this.tableData.length > 5) {
                //     this.showPagebox = true
                // }
            })
        },

        // 订单售后
        afterSales() {
            this.$router.push({
                path: '/plug_ins/integralMall/afterSaleList'
            })
        },

        // 评价管理
        evaluation() {
            this.$router.push({
                path: '/plug_ins/integralMall/commentList'
            })
        },

        // 订单结算
        settlement() {
            this.$router.push({
                path: '/plug_ins/integralMall/orderSettlementList'
            })
        },

        // 订单打印
        print() {
            if (this.idList.length == 0) {
                this.$message({
                    message: this.$t('integralOrder.qxzdd'),
                    type: 'warning',
                    offset: 100
                })
            } else {
                let routeData = this.$router.resolve({
                    path: "/print",
                    query: {
                        orderId: this.idPrintList
                    },
                });

                window.open(routeData.href, '_blank');
            }
        },

        // 批量删除
        delAll() {
            this.$confirm(this.$t('integralOrder.scqr'), this.$t('integralOrder.ts'), {
                confirmButtonText: this.$t('integralOrder.okk'),
                cancelButtonText: this.$t('integralOrder.ccel'),
                type: 'warning'
            }).then(() => {
                delOrder({
                    api: 'plugin.integral.order.delOrder',
                    id: this.idList
                }).then(res => {
                    console.log(res);
                    if (res.data.code == '200') {
                        this.orderLists()
                        this.$message({
                            type: 'success',
                            message: this.$t('integralOrder.cg'),
                            offset: 100
                        })
                    }
                })
            }).catch(() => {
                // this.$message({
                //   type: 'info',
                //   message: '已取消删除',
                //   offset: 100
                // });          
            });
        },

        handleSelectionChange(val) {
            if (val.length !== 0) {
                this.is_disabled = false
            } else {
                this.is_disabled = true
            }
            this.idList = val.map(item => {
                return item.id
            })
            this.idPrintList = val.map(item => {
                return item.orderno
            })
            this.idList = this.idList.join(',')
            this.idPrintList = this.idPrintList.join(',')
        },

        // 订单详情
        Details(value) {
            console.log(value);
            this.$router.push({
                path: '/plug_ins/integralMall/orderDetails',
                query: {
                    no: value.orderno
                }
            })
        },

        // 编辑订单
        Edit(value) {
            console.log(value);
            this.$router.push({
                path: '/plug_ins/integralMall/editorOrder',
                query: {
                    no: value.orderno
                }
            })
        },

        getExpressStr(value) {
            if (value && value !== ',') {
                let str = []
                value.split(',').map(item => {
                    if (item) {
                        str.push(item)
                    }
                })
                return str.join(',')
            } else {
                return '暂无'
            }
        },

        // 商品发货
        Delivery(value) {
            // console.log(value);
            // this.dialogVisible3 = true
            // this.searchExpress(value.id)
            // this.fhid = {
            //     sNo: value.id,
            //     oid: value.detailId,
            //     // 件数
            //     num: value.needNum
            // }

            this.$router.push({
                path: '/plug_ins/integralMall/goodsDelivery',
                query: {
                    no: value.orderno
                }
            })
        },

        // 关闭订单
        closeOrder(value) {
            console.log(value);
            closeOrder({
                api: 'plugin.integral.order.closeOrder',
                id: value.id
            }).then(res => {
                if (res.data.code == '200') {
                    console.log(res);
                    this.$message({
                        type: 'success',
                        message: '操作成功！',
                        offset: 100
                    })
                    this.orderLists()
                }
            })
        },

        // 弹框方法
        dialogShow2(value) {
            this.dialogVisible2 = true
            kuaidishow({
                api: 'plugin.integral.order.kuaidishow',
                orderno: value.orderno
            }).then(res => {
                console.log(res);
                this.logisticsList = res.data.data.list
                this.ruleForm.courier_company = res.data.data.list[0].kuaidi_name
                this.ruleForm.courier_no = res.data.data.list[0].courier_num
                this.ruleForm.logistics = ''
            })
            console.log(value);

        },

        handleClose2(done) {
            this.dialogVisible2 = false
        },

        //选择一页多少条
        handleSizeChange(e) {
            this.loading = true
            this.pageSize = e
            this.orderLists().then(() => {
                this.currpage = ((this.dictionaryNum - 1) * this.pageSize) + 1
                this.current_num = this.tableData.length === this.pageSize ? this.dictionaryNum * this.pageSize : this.total
                this.loading = false
            })
        },

        //点击上一页，下一页
        handleCurrentChange(e) {
            this.loading = true
            this.dictionaryNum = e
            this.currpage = ((e - 1) * this.pageSize) + 1
            this.orderLists().then(() => {
                this.current_num = this.tableData.length === this.pageSize ? e * this.pageSize : this.total
                this.loading = false
            })
        },

        // 导出弹框方法
        dialogShow() {
            this.dialogVisible = true
        },

        showPrice(price) {
            return parseInt(price).toFixed(0)
        },

        handleClose(done) {
            this.dialogVisible = false
        },

        async exportPage() {
            exports({
                api: 'plugin.integral.order.index',
                pageNo: this.dictionaryNum,
                pageSize: this.pageSize,
                exportType: 1,
                keyWord: this.inputInfo.name,
                mchName: this.inputInfo.mchName,
                status: this.inputInfo.state,
                startdate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                enddate: this.inputInfo.date ? this.inputInfo.date[1] : null
            }, 'pageorder')
        },

        async exportAll() {
            exports({
                api: 'plugin.integral.order.index',
                pageNo: 1,
                pageSize: 999999,
                exportType: 1,
                // keyWord: this.inputInfo.name,
                // mchName: this.inputInfo.mchName,
                // status: this.inputInfo.state,
                // startdate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                // enddate: this.inputInfo.date ? this.inputInfo.date[1] : null
            }, 'allorder')
        },

        async exportQuery() {
            exports({
                api: 'plugin.integral.order.index',
                pageNo: 1,
                pageSize: this.total,
                exportType: 1,
                keyWord: this.inputInfo.name,
                mchName: this.inputInfo.mchName,
                status: this.inputInfo.state,
                startdate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                enddate: this.inputInfo.date ? this.inputInfo.date[1] : null
            }, 'queryorder')
        }
    }
}