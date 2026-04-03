import { orderList } from '@/api/plug_ins/supplier'
import { exports } from '@/api/export/index'
import { mixinstest } from '@/mixins/index'
import ErrorImg from '@/assets/images/default_picture.png'

export default {
    name: 'supplierOrder',
    mixins: [mixinstest],
    data() {
        return {
            routerList: JSON.parse(sessionStorage.getItem('tabRouter')),
            radio1: this.$t('orderLists.ddlb'),
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
                    label: this.$t('orderLists.ddgb')
                }
            ],// 订单状态
            typeList: [
                {
                    value: '1',
                    label: this.$t('orderLists.yhxd')
                },
                {
                    value: '2',
                    label: this.$t('orderLists.dpxd')
                },
                {
                    value: '3',
                    label: this.$t('orderLists.ptxd')
                }
            ],// 订单类型
            inputInfo: {
                orderInfo: null,
                store: null,
                state: '1',
                type: null,
                date: null
            },

            tableData: [],
            loading: true,

            // 导出弹框数据
            dialogVisible: false,
            orderList: '',
            // table高度
            tableHeight: null
        }
    },

    created() {
        if (this.$route.query.no) {
            orderList({
                api: 'supplier.Admin.SupplierOrders.OrderIndex',
                pageSize: this.pageSize,
                pageNo: this.dictionaryNum,
                id: this.$route.query.no,
                keyWord: this.inputInfo.orderInfo ? this.inputInfo.orderInfo : null,
                mchName: this.inputInfo.store ? this.inputInfo.store : null,
                status: this.inputInfo.state ? this.inputInfo.state : null,
                operationType: this.inputInfo.type ? this.inputInfo.type : null,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
                selfLifting: this.goodsListType,
            }).then(res => {
                console.log(res);
                this.total = res.data.data.total
                this.tableData = res.data.data.list
                this.pagination.page = this.dictionaryNum
                this.loading = false
                if (this.total < this.current_num) {
                    this.current_num = this.total
                }
                this.$nextTick(() => {
                    this.$refs.table.doLayout();
                });
            })
        } else {
            this.orderLists()
        }
    },

    mounted() {
        this.$nextTick(function () {
            this.getHeight()
        })
        window.addEventListener('resize', this.getHeight(), false)
    },

    methods: {
        // 图片错误处理
        handleErrorImg(e) {
            console.log('图片报错了', e.target.src);
            e.target.src = ErrorImg
        },
        // 获取table高度
        getHeight() {
            this.tableHeight = this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
            console.log(this.$refs.tableFather.clientHeight);
        },

        async orderLists() {
            const res = await orderList({
                api: 'supplier.Admin.SupplierOrders.OrderIndex',
                pageSize: this.pageSize,
                pageNo: this.dictionaryNum,
                keyWord: this.inputInfo.orderInfo ? this.inputInfo.orderInfo : null,
                mchName: this.inputInfo.store ? this.inputInfo.store : null,
                status: this.inputInfo.state ? this.inputInfo.state : null,
                operationType: this.inputInfo.type ? this.inputInfo.type : null,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
                selfLifting: this.goodsListType,
            })
            console.log(res);
            this.total = res.data.data.total
            this.tableData = res.data.data.list
            this.pagination.page = this.dictionaryNum
            this.loading = false
            if (this.total < this.current_num) {
                this.current_num = this.total
            }
            this.$nextTick(() => {
                this.$refs.table.doLayout();
            });
        },

        // 重置
        reset() {
            this.inputInfo.orderInfo = null,
                this.inputInfo.store = null,
                this.inputInfo.state = null,
                this.inputInfo.type = null,
                this.inputInfo.date = null
        },

        // 查询
        demand() {
            this.currpage = 1
            this.current_num = 10
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

        //选择一页多少条
        handleSizeChange(e) {
            this.loading = true
            console.log(e);
            // this.current_num = e
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

        // 订单详情
        Details(value) {
            this.$router.push({
                path: '/plug_ins/supplier/orderDetail',
                query: {
                    no: value.orderno,
                }
            })
        },

        // 选框改变
        handleSelectionChange(val) {
            console.log(val);
            this.orderList = val.map(item => {
                return item.orderno
            })
            this.orderList = this.orderList.join(',')
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
                api: 'supplier.Admin.SupplierOrders.OrderIndex',
                pageNo: this.dictionaryNum,
                pageSize: this.pageSize,
                exportType: 1,
                keyWord: this.inputInfo.orderInfo ? this.inputInfo.orderInfo : null,
                mchName: this.inputInfo.store ? this.inputInfo.store : null,
                status: this.inputInfo.state ? this.inputInfo.state : null,
                operationType: this.inputInfo.type ? this.inputInfo.type : null,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
                selfLifting: this.goodsListType,
            }, 'pageorder')
        },

        async exportAll() {
            exports({
                api: 'supplier.Admin.SupplierOrders.OrderIndex',
                pageNo: 1,
                pageSize: this.total,
                exportType: 1,
                keyWord: this.inputInfo.orderInfo ? this.inputInfo.orderInfo : null,
                mchName: this.inputInfo.store ? this.inputInfo.store : null,
                status: this.inputInfo.state ? this.inputInfo.state : null,
                operationType: this.inputInfo.type ? this.inputInfo.type : null,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
                selfLifting: this.goodsListType,
            }, 'allorder')
        },

        async exportQuery() {
            exports({
                api: 'supplier.Admin.SupplierOrders.OrderIndex',
                pageNo: 1,
                pageSize: this.total,
                exportType: 1,
                keyWord: this.inputInfo.orderInfo ? this.inputInfo.orderInfo : null,
                mchName: this.inputInfo.store ? this.inputInfo.store : null,
                status: this.inputInfo.state ? this.inputInfo.state : null,
                operationType: this.inputInfo.type ? this.inputInfo.type : null,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
                selfLifting: this.goodsListType,
            }, 'queryorder')
        }
    }
}