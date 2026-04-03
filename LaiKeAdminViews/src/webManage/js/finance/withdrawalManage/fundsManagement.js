import { fundList } from '@/api/finance/withdrawalManage'
import { exports } from '@/api/export/index'
import { mixinstest } from '@/mixins/index'
import { getStorage } from '@/utils/storage'
import { getButton } from '@/api/layout/information'

export default {
    name: 'fundsManagement',
    mixins: [mixinstest],
    data() {
        return {
            laikeCurrencySymbol:'￥',
            exportType:'',//判断是否为导出
            sourceList: [
                {
                    value: '1',
                    label: this.$t('fundsManagement.lbList1')
                },
                {
                    value: '2',
                    label: this.$t('fundsManagement.lbList2')
                },
                // {
                //     value: '3',
                //     label: '支付宝小程序'
                // },
                // {
                //     value: '4',
                //     label: '头条小程序'
                // },
                // {
                //     value: '5',
                //     label: '百度小程序'
                // },
                {
                    value: '6',
                    label: this.$t('fundsManagement.lbList6')
                },
                {
                    value: '7',
                    label: this.$t('fundsManagement.lbList7')
                },
            ],// 订单状态
            inputInfo: {
                userName: null,
                source: null,
                date: null,
            },

            tableData: [],
            loading: true,
            // table高度
            tableHeight: null,
            // 导出弹框数据
            dialogVisible: false,
        }
    },

    created() {
        this.laikeCurrencySymbol = this.LaiKeCommon.getDefaultCurrencySymbol();
        this.axios()
    },

    mounted() {
        this.$nextTick(function() {
            this.getHeight()
        })
        window.addEventListener('resize',this.getHeight(),false)
    },

    methods: {
        //初始化充值列表数据
        async axios(){
            const res = await fundList({
                api: 'admin.user.getUserMoneyInfo',
                pageNo: this.dictionaryNum,
                pageSize: this.pageSize,
                userName:this.inputInfo.userName,
                source:this.inputInfo.source,
                startDate: this.inputInfo.date?.[0]??'',
                endDate: this.inputInfo.date?.[1]??'',
            })
            console.log('res:',res);

            if(res.data.code == 200){
                this.loading = false
                this.tableData = res.data.data.list
                this.total = res.data.data.total
                console.log('tableData:',this.tableData);
            }

        },
        // 获取table高度
        getHeight(){
			      this.tableHeight = this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
            console.log(this.$refs.tableFather.clientHeight);
		    },

        // 重置
        reset() {
            this.inputInfo.userName = null,
            this.inputInfo.source = null,
            this.inputInfo.date = null
        },

        // 查询
        demand() {
            this.currpage = 1
            // this.current_num = 10
            this.showPagebox = false
            this.loading = true
            this.dictionaryNum = 1
            this.axios().then(() => {
                this.loading = false
                if(this.tableData.length > 5) {
                    this.showPagebox = true
                }
            })
        },

        View(value) {
            console.log('value',value);

            this.$router.push({
                path: '/finance/fundsSet/fundsDetails',
                query: {
                    id: value.user_id
                }
            })
        },

        //选择一页多少条
        handleSizeChange(e){
            this.loading = true
            // this.current_num = e
            this.pageSize = e
            this.axios().then(() => {
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
            this.axios().then(() => {
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
                api: 'admin.user.getUserMoneyInfo',
                pageNo: this.dictionaryNum,
                pageSize: this.pageSize,
                exportType: 1,
                userName:this.inputInfo.userName,
                source:this.inputInfo.source,
                startDate: this.inputInfo.date?.[0]??'',
                endDate: this.inputInfo.date?.[1]??'',
            },'资金管理_导出本页')
        },

        async exportAll() {
            exports({
                api: 'admin.user.getUserMoneyInfo',
                pageNo: 1,
                pageSize: 9999,
                exportType: 1,
                // userName:this.inputInfo.userName,
                // source:this.inputInfo.source,
                // startDate: this.inputInfo.date?.[0]??'',
                // endDate: this.inputInfo.date?.[1]??'',
            },'资金管理_导出全部')
        },

        async exportQuery() {
            exports({
                api: 'admin.order.getRefundList',
                pageNo: 1,
                pageSize: this.total,
                exportType: 1,
                userName:this.inputInfo.userName,
                source:this.inputInfo.source,
                startDate: this.inputInfo.date?.[0]??'',
                endDate: this.inputInfo.date?.[1]??'',
            },'资金管理_导出查询')
        }
    }
}
