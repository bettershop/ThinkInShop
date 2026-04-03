import { orderList, delOrder, closeOrder } from '@/api/plug_ins/preSale'
import { kuaidishow } from '@/api/order/orderList'
import { exports } from '@/api/export/index'
import { mixinstest } from '@/mixins/index'
import { getStorage } from '@/utils/storage'
import { getButton } from '@/api/layout/information'
import { deliveryView, deliverySave } from '@/api/order/orderList'

export default {
    name: 'preSaleOrder',
    mixins: [mixinstest],

    data() {
        return {
            routerList: JSON.parse(sessionStorage.getItem('tabRouter')),
            radio1: '4',
            total: null,
            inputInfo: {
                name: null,
                type: null,
                state: null,
                date: null,
                mchName: null
            },
            typeList: [{
                value: '1',
                label: this.$t('preSale.scheduledRecord.djms')
            },
            {
                value: '2',
                label: this.$t('preSale.scheduledRecord.dhms')
            }
            ], // 预售类型
            button_list: [],
            menuId: '',
            stateList: [
                {
                    value: '0',
                    label: this.$t('orderLists.dfk')
                },
                {
                    value: '1',
                    label: this.$t('orderLists.dfh')
                },
                {
                    value: '2',
                    label: this.$t('orderLists.dsh')
                },
                {
                    value: '5',
                    label: this.$t('orderLists.ywc')
                },
                {
                    value: '7',
                    label: this.$t('orderLists.ygb')
                }
            ],// 订单状态
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
            inOrderNum: '',
            // table高度
            tableHeight: null,

            // 发货弹框数据
            dialogVisible3: false,
            courierList: [],
            id: '',
            ruleForm2: {
                kuaidi_name: null,
                kuaidi_no: null,
            },
            rules2: {
                kuaidi_name: [
                    { required: true, message: "请填写快递名称", trigger: "change" },
                ],
                kuaidi_no: [
                    { required: true, message: "请填写快递单号", trigger: "blur" },
                ],
            },

            // 导出弹框数据
            dialogVisible: false,

            logistics_type: false, //电子面单
        }
    },

    created() {
        if (this.$route.query.no) {
            orderList({
                api: 'plugin.presell.AdminPreSell.getOrderList',
                pageSize: this.pageSize,
                pageNo: this.dictionaryNum,
                keyWord: this.inputInfo.name,
                sellType: this.inputInfo.type ? parseInt(this.inputInfo.type) : null,//类型
                status: this.inputInfo.state,
                id: this.$route.query.no,
                mchName: this.inputInfo.mchName ? this.inputInfo.mchName : null,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
            }).then(res => {
                console.log(res.data.data.list, 99999999);
                this.total = res.data.data.total
                this.tableData = res.data.data.list
                this.tableData.forEach((item, index) => {
                    switch (item.sell_type) {
                        case 1 || '1':
                            item.sellTypeDesc = this.$t('preSale.goodsList.djmc')
                            break
                        case 2 || '2':
                            item.sellTypeDesc = this.$t('preSale.goodsList.dhmc')
                            break
                        default:
                            item.sellTypeDesc = this.$t('没有这个判断类型，来这改')
                            break
                    }
                    switch (item.status) {
                        case 1 || '1':
                            item.statusDesc = this.$t('preSale.scheduledRecord.dfh')
                            break
                        case 2 || '2':
                            item.statusDesc = this.$t('preSale.scheduledRecord.dsh')
                            break
                        case 5 || '5':
                            item.statusDesc = this.$t('preSale.scheduledRecord.ywc')
                            break
                        case 7 || '7':
                            item.statusDesc = this.$t('preSale.scheduledRecord.ygb')
                            break
                        default:
                            item.statusDesc = this.$t('preSale.scheduledRecord.dfk')
                            break
                    }
                })
                this.loading = false
                if (this.total < this.current_num) {
                    this.current_num = this.total
                }
            })
        } else {
            this.orderLists()
        }
        //this.deliveryViews() //后台说这是发货接口不要调用
        this.getButtons()
        this.inOrderNum = getStorage('inOrderNum')
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
        handleSelectAll() {
            if (this.tableData.length == 0) {
                this.$refs.table.clearSelection()
            }
        },
        async searchExpress(sNo) {
            const res = await deliveryView({
                api: 'admin.order.GetLogistics',
                sNo
            })
            this.courierList = res.data.data.list
        },
        async deliveryViews(sNo) {
            this.searchExpress(sNo)
            const res = await deliveryView({
                api: "admin.order.deliveryView",
                sNo
            });
            this.goodsTables = res.data.data.goods;
            this.goodsNum = res.data.data.goods.num;
            //this.courierList = res.data.data.express.list;
        },
        // 弹框方法
        // Delivery(value) {
        //     this.deliveryViews(value.sNo)
        //     this.id = value.detailId
        //     this.dialogVisible3 = true;
        //     this.$refs.ruleForm2.clearValidate();
        // },

        handleClose3(done) {
            this.ruleForm2.kuaidi_name = ''
            this.ruleForm2.kuaidi_no = ''
            this.$refs.ruleForm2.clearValidate();
            this.dialogVisible3 = false;
        },
        // 发货
        determine(formName2) {
            this.$refs[formName2].validate(async (valid) => {
                console.log(this.ruleForm2);
                if (valid) {
                    try {
                        if (!this.logistics_type && (this.ruleForm2.kuaidi_no.length < 10 || this.ruleForm2.kuaidi_no.length > 30)) {
                            this.$message({
                                message: "快递单号输入格式错误，必须大于等于10位哦",
                                type: "error",
                                offset: 100,
                            });
                            return;
                        } else {
                            let api = 'admin.order.UnifiedShipment'
                            let expressid = this.ruleForm2.kuaidi_name
                            let courierNum = !this.logistics_type ? this.ruleForm2.kuaidi_no : ''
                            let type = this.logistics_type ? '2' : '1'
                            let psyInfo = ''
                            let orderList = [
                                {
                                    detailId: this.id,
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
                                if (res.data.code == "200") {
                                    this.$message({
                                        message: "发货成功",
                                        type: "success",
                                        offset: 100,
                                    });
                                    this.handleClose3()
                                    this.orderLists();
                                }
                                console.log(res);
                            });
                        }
                    } catch (error) {
                        this.$message({
                            message: error.message,
                            type: "error",
                            showClose: true,
                            offset: 100,
                        });
                    }
                } else {
                    console.log("error submit!!");
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
            const route = getStorage('route')
            route.forEach(item => {
                if (item.redirect == '/plug_ins/preSale/goodsList') {
                    item.children.forEach(e => {
                        if (e.path == 'preSaleOrder') {
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
            this.button_list = buttonList.data.data.map(item => {
                return item.title
            })

        },

        async orderLists() {
            const res = await orderList({
                api: 'plugin.presell.AdminPreSell.getOrderList',
                pageSize: this.pageSize,
                pageNo: this.dictionaryNum,
                keyWord: this.inputInfo.name,
                sellType: this.inputInfo.type ? parseInt(this.inputInfo.type) : null,//类型
                status: this.inputInfo.state,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
                mchName: this.inputInfo.mchName ? this.inputInfo.mchName : null,
            })
            console.log(res.data.data.list, 99999999);
            this.total = res.data.data.total
            this.tableData = res.data.data.list
            this.tableData.forEach((item, index) => {
                switch (item.sell_type) {
                    case 1 || '1':
                        item.sellTypeDesc = this.$t('preSale.goodsList.djmc')
                        break
                    case 2 || '2':
                        item.sellTypeDesc = this.$t('preSale.goodsList.dhmc')
                        break
                    default:
                        item.sellTypeDesc = this.$t('没有这个判断类型，来这改')
                        break
                }
                switch (item.status) {
                    case 1 || '1':
                        item.statusDesc = this.$t('preSale.scheduledRecord.dfh')
                        break
                    case 2 || '2':
                        item.statusDesc = this.$t('preSale.scheduledRecord.dsh')
                        break
                    case 5 || '5':
                        item.statusDesc = this.$t('preSale.scheduledRecord.ywc')
                        break
                    case 7 || '7':
                        item.statusDesc = this.$t('preSale.scheduledRecord.ygb')
                        break
                    default:
                        item.statusDesc = this.$t('preSale.scheduledRecord.dfk')
                        break
                }
            })
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
            this.inputInfo.type = null
            this.inputInfo.state = null
            this.inputInfo.date = null
        },

        // 查询
        demand() {
            //this.currpage = 1
            //this.current_num = 10
            this.showPagebox = false
            this.loading = true
            this.dictionaryNum = 1
            this.orderLists().then(() => {
                this.loading = false
                if (this.tableData.length > 5) {
                    this.showPagebox = true
                }
            })
        },

        // 订单售后
        afterSales() {
            this.$router.push({
                path: '/plug_ins/preSale/afterSaleList'
            })
        },

        // 评价管理
        evaluation() {
            this.$router.push({
                path: '/plug_ins/preSale/evaluateList'
            })
        },

        // 订单结算
        settlement() {
            this.$router.push({
                path: '/plug_ins/preSale/orderSettlementList'
            })
        },

        // 订单打印
        print() {
            if (this.idPrintList.length == 0) {
                this.$message({
                    message: this.$t('preSale.preSaleOrder.qxzdd'),
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
            this.$confirm(this.$t('preSale.preSaleOrder.qrsc'), this.$t('preSale.ts'), {
                confirmButtonText: this.$t('preSale.okk'),
                cancelButtonText: this.$t('preSale.ccel'),
                type: 'warning'
            }).then(() => {
                delOrder({
                    api: 'plugin.presell.AdminPreSell.deleteOrder',
                    orderList: this.idList
                }).then(res => {
                    console.log(res);
                    if (res.data.code == '200') {
                        this.isFillList()
                        this.$message({
                            type: 'success',
                            message: this.$t('preSale.cg'),
                            offset: 100
                        })
                    }
                })
            }).catch(() => {
                // this.$message({
                //     type: 'info',
                //     message: '已取消删除',
                //     offset: 100
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
                return item.sNo
            })
            this.idList = this.idList.join(',')
            this.idPrintList = this.idPrintList.join(',')

        },

        // 订单详情
        Details(value) {
            this.$router.push({
                path: '/plug_ins/preSale/orderDetails',
                query: {
                    no: value.sNo
                }
            })
        },

        // 编辑订单
        Edit(value) {
            this.$router.push({
                path: '/plug_ins/preSale/editorOrder',
                query: {
                    no: value.sNo,
                }
            })
        },

        // 商品发货
        Delivery(value) {
            this.$router.push({
                path: '/plug_ins/preSale/goodsDelivery',
                query: {
                    no: value.sNo
                }
            })
        },

        //删除当前页面最后数据 页面需要--
        isFillList() {
            let totalPage = Math.ceil((this.total - 1) / this.pageSize)
            let dictionaryNum = this.dictionaryNum > totalPage ? totalPage : this.dictionaryNum
            this.dictionaryNum = dictionaryNum < 1 ? 1 : dictionaryNum
            this.orderLists() //数据初始化方法
        },
        // 删除订单
        closeOrder(value) {
            console.log(value);
            this.$confirm(this.$t('preSale.preSaleOrder.qrsc'), this.$t('preSale.ts'), {
                confirmButtonText: this.$t('preSale.okk'),
                cancelButtonText: this.$t('preSale.ccel'),
                type: 'warning'
            }).then(() => {
                closeOrder({
                    api: 'plugin.presell.AdminPreSell.deleteOrder',
                    orderList: value.id
                }).then(res => {
                    if (res.data.code == '200') {
                        console.log(res);
                        this.$message({
                            type: 'success',
                            message: this.$t('preSale.cg'),
                            offset: 100
                        })
                        this.isFillList()
                    }
                })
            }).catch(() => {
                // this.$message({
                //     type: 'info',
                //     message: '已取消删除',
                //     offset: 100
                // });
            });

        },

        // 弹框方法
        dialogShow2(value) {
            this.dialogVisible2 = true
            kuaidishow({
                api: 'plugin.presell.AdminPreSell.logisticsInfo',
                orderNo: value.sNo
            }).then(res => {
                console.log(res);
                this.ruleForm.courier_company = res.data.data.list[0].courier_num
                this.ruleForm.courier_no = res.data.data.list[0].kuaidi_name
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

        handleClose(done) {
            this.dialogVisible = false
        },

        async exportPage() {
            exports({
                api: 'plugin.presell.AdminPreSell.getOrderList',
                pageNo: this.dictionaryNum,
                pageSize: this.pageSize,
                exportType: 1,
                keyWord: this.inputInfo.name,
                sellType: this.inputInfo.type ? parseInt(this.inputInfo.type) : null,//类型
                status: this.inputInfo.state,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
                mchName: this.inputInfo.mchName ? this.inputInfo.mchName : null,
            }, 'pageorder')
        },

        async exportAll() {
            exports({
                api: 'plugin.presell.AdminPreSell.getOrderList',
                pageNo: 1,
                pageSize: 999999,
                exportType: 1,
            }, 'allorder')
        },

        async exportQuery() {
            exports({
                api: 'plugin.presell.AdminPreSell.getOrderList',
                pageNo: 1,
                pageSize: this.total,
                exportType: 1,
                keyWord: this.inputInfo.name,
                sellType: this.inputInfo.type ? parseInt(this.inputInfo.type) : null,//类型
                status: this.inputInfo.state,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
                mchName: this.inputInfo.mchName ? this.inputInfo.mchName : null,
            }, 'queryorder')
        }
    }
}
