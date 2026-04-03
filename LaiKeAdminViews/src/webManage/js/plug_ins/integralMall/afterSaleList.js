import { getRefundList } from '@/api/plug_ins/integralMall'
import { mixinstest } from '@/mixins/index'
import { exports } from '@/api/export/index'

export default {
    name: 'afterSaleList',
    mixins: [mixinstest],

    data() {
        return {
            radio1: '3',
            routerList: JSON.parse(sessionStorage.getItem("tabRouter")),
            stateList: [
                {
                    value: '7',
                    label: '待审核'
                },
                {
                    value: '1',
                    label: '退款中'
                },
                {
                    value: '2',
                    label: '退款成功'
                },
                {
                    value: '3',
                    label: '退款失败'
                },
                {
                    value: '4',
                    label: '换货中'
                },
                {
                    value: '5',
                    label: '换货成功'
                },
                {
                    value: '6',
                    label: '换货失败'
                },
            ],// 订单状态
            inputInfo: {
                orderNo: null,
                retype: null,
                state: null,
                date: null
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
        showPrice(price) {
          return parseInt(price).toFixed(0);
        },
        // 获取table高度
        getHeight(){
			      this.tableHeight = this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
            console.log(this.$refs.tableFather.clientHeight);
		    },
        async getRefundLists() {
            const res = await getRefundList({
                api: 'plugin.integral.order.index',
                pageNo: this.dictionaryNum,
                pageSize: this.pageSize,
                orderNo: this.inputInfo.orderNo ? this.inputInfo.orderNo : null,
                retype: this.inputInfo.retype ? this.inputInfo.retype : null,
                r_type: this.inputInfo.state ? this.inputInfo.state : null,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
                order_type:'return'
            })
            console.log(res);
            this.total = res.data.data.total
            this.tableData = res.data.data.list
            this.loading = false
            if(this.total < this.current_num) {
                this.current_num = this.total
            }
        },

        // async deliveryViews() {
        //     const res = await deliveryView({
        //         api: 'plugin.integral.order.deliveryView',
        //     })
        //     this.courierList = res.data.data.express
        // },

        // 重置
        reset() {
            this.inputInfo.orderNo = null,
            this.inputInfo.state = null,
            this.inputInfo.retype = null,
            this.inputInfo.date = null
        },

        // 查询
        demand() {
            this.currpage = 1
            this.current_num = 10
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

        View (value, type) {
          console.log(value, type)
          this.$router.push({
            path: '/plug_ins/integralMall/afterSaleDetails',
            query: {
              params: JSON.stringify({
                value: value,
                type: type
              })
            }
          })
        },


        //选择一页多少条
        handleSizeChange(e){
            this.loading = true
            // this.current_num = e
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
                api: 'plugin.integral.order.index',
                pageNo: this.dictionaryNum,
                pageSize: this.pageSize,
                exportType: 1,
                order_type:'return',
                orderNo: this.inputInfo.orderNo ? this.inputInfo.orderNo : null,
                retype: this.inputInfo.retype ? this.inputInfo.retype : null,
                r_type: this.inputInfo.state ? this.inputInfo.state : null,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
            },'pageorder')
        },

        async exportAll() {
            exports({
                api: 'plugin.integral.order.index',
                pageNo: 1,
                pageSize: this.total,
                exportType: 1,
                order_type:'return',
                orderNo: this.inputInfo.orderNo ? this.inputInfo.orderNo : null,
                retype: this.inputInfo.retype ? this.inputInfo.retype : null,
                r_type: this.inputInfo.state ? this.inputInfo.state : null,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
            },'allorder')
        },

        async exportQuery() {
            exports({
                api: 'plugin.integral.order.index',
                pageNo: 1,
                pageSize: this.total,
                exportType: 1,
                order_type:'return',
                orderNo: this.inputInfo.orderNo ? this.inputInfo.orderNo : null,
                retype: this.inputInfo.retype ? this.inputInfo.retype : null,
                r_type: this.inputInfo.state ? this.inputInfo.state : null,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
            },'queryorder')
        }
    }
}
