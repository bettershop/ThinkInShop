import { getRefundList } from '@/api/plug_ins/preSale'
import { mixinstest } from '@/mixins/index'
import { exports } from '@/api/export/index'

export default {
    name: 'afterSaleList',
    mixins: [mixinstest],

    data() {
        return {
            typeList:[
                {
                    value: '2',
                    label: this.$t('preSale.afterSaleList.jtk')
                },
                {
                    value: '1',
                    label: this.$t('preSale.afterSaleList.thtk')
                },
                {
                    value: '3',
                    label: this.$t('preSale.afterSaleList.hh')
                },
            ],//类型
            stateList: [
                {
                    value: '7',
                    label: this.$t('salesReturnList.shz')
                },
                {
                    value: '1',
                    label: this.$t('salesReturnList.tkz')
                },
                {
                    value: '2',
                    label: this.$t('salesReturnList.tkcg')
                },
                {
                    value: '3',
                    label: this.$t('salesReturnList.tksb')
                },
                {
                    value: '4',
                    label: this.$t('salesReturnList.hhz')
                },
                {
                    value: '5',
                    label: this.$t('salesReturnList.hhcg')
                },
                {
                    value: '6',
                    label: this.$t('salesReturnList.hhsb')
                },
            ],// 订单状态
            inputInfo: {
                orderNo: null,
                state: null,
                date: null,
                type: null,
                mchName: null
            },

            tableData: [],
            loading: true,

            id: null,
            toggle: null,

            type: '',

            content: '',

            courierList: [],

            // table高度
            tableHeight: null,

            // 导出弹框数据
            dialogVisible: false,
        }
    },

    created() {
        this.getRefundLists()
       // this.deliveryViews()
    },

    mounted() {
        this.$nextTick(function() {
            this.getHeight()
        })
        window.addEventListener('resize',this.getHeight(),false)
    },

    methods: {
        // 获取table高度
        getHeight(){
          this.tableHeight = this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
        },
        async getRefundLists() {
            const res = await getRefundList({
                api: 'plugin.presell.AdminPreSell.getRefundList',
                pageNo: this.dictionaryNum,
                pageSize: this.pageSize,
                condition:this.inputInfo.orderNo ?this.inputInfo.orderNo : null,
                reType: this.inputInfo.type ? parseInt(this.inputInfo.type) : null,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
                mchName: this.inputInfo.mchName ? this.inputInfo.mchName : null,
                status: this.inputInfo.state
            })
            // console.log(res,"售后");
            this.total = res.data.data.total
            this.tableData = res.data.data.list
            this.loading = false
            if(this.total < this.current_num) {
                this.current_num = this.total
            }
            if(this.total == 0) {
                this.showPagebox = false
            } else {
                this.showPagebox = true
            }
        },

        async deliveryViews() {
            const res = await deliveryView({
                api: 'admin.order.deliveryView',
            })
            this.courierList = res.data.data.express
        },

        // 重置
        reset() {
            this.inputInfo.orderNo = null,
            this.inputInfo.state = null,
            this.inputInfo.date = null,
            this.inputInfo.type = null
            this.inputInfo.mchName = null
        },

        // 查询
        demand() {
            //this.currpage = 1
            //this.current_num = 10
            this.showPagebox = false
            this.loading = true
            this.dictionaryNum = 1
            this.getRefundLists().then(() => {
                this.loading = false
                if(this.tableData.length > 5) {
                    this.showPagebox = true
                }
            })
        },

        View(value,type) {
            this.$router.push({
                path: '/plug_ins/preSale/afterSaleDetails',
                query: {
                    id: value.id,
                    type:type
                }
            })
        },

        //选择一页多少条
        handleSizeChange(e){
            this.loading = true
            this.pageSize = e
            this.getRefundLists().then(() => {
                this.currpage = ((this.dictionaryNum - 1) * this.pageSize) + 1
                this.current_num = this.tableData.length === this.pageSize ? this.dictionaryNum * this.pageSize : this.total
                this.loading = false
            })
        },

        //点击上一页，下一页
        handleCurrentChange(e){
            this.loading = true
            this.dictionaryNum = e
            this.currpage = ((e - 1) * this.pageSize) + 1
            this.getRefundLists().then(() => {
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
                api: 'plugin.presell.AdminPreSell.getRefundList',
                pageNo: this.dictionaryNum,
                pageSize: this.pageSize,
                exportType: 1,
                condition:this.inputInfo.orderNo ?this.inputInfo.orderNo : null,
                status: this.inputInfo.state ? parseInt(this.inputInfo.state) : null,
                reType: this.inputInfo.type ? parseInt(this.inputInfo.type) : null,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
                mchName: this.inputInfo.mchName ? this.inputInfo.mchName : null,
            },'pageorder')
        },

        async exportAll() {
            exports({
                api: 'plugin.presell.AdminPreSell.getRefundList',
                pageNo: 1,
                pageSize: this.total,
                exportType: 1,
                condition:this.inputInfo.orderNo ?this.inputInfo.orderNo : null,
                status: this.inputInfo.state ? parseInt(this.inputInfo.state) : null,
                reType: this.inputInfo.type ? parseInt(this.inputInfo.type) : null,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
                mchName: this.inputInfo.mchName ? this.inputInfo.mchName : null,
            },'allorder')
        },

        async exportQuery() {
            exports({
                api: 'plugin.presell.AdminPreSell.getRefundList',
                pageNo:1,
                pageSize: this.total,
                exportType: 1,
                status: this.inputInfo.state ? parseInt(this.inputInfo.state) : null,
                condition:this.inputInfo.orderNo ?this.inputInfo.orderNo : null,
                reType: this.inputInfo.type ? parseInt(this.inputInfo.type) : null,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
                mchName: this.inputInfo.mchName ? this.inputInfo.mchName : null,
            },'queryorder')
        }
    }
}
