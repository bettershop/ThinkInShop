import { topList } from '@/api/finance/withdrawalManage'
import { exports } from '@/api/export/index'
import { mixinstest } from '@/mixins/index'
import { getStorage } from '@/utils/storage'
import { getButton } from '@/api/layout/information'

export default {
    name: 'rechargeList',
    mixins: [mixinstest],
    data() {
        return {
            laikeCurrencySymbol:'￥',
            allMoney: 0,
            exportType: '',//判断是否为导出
            TypeList: [
                {
                    value: '1',
                    label: this.$t('rechargeList.yhcz')
                },
                {
                    value: '11',
                    label: this.$t('rechargeList.xtkk')
                },
                {
                    value: '14',
                    label: this.$t('rechargeList.qxcz')
                },
            ],// 订单状态
            inputInfo: {
                userName: '',
                oType: '',
                date: ''
            },

            tableData: [],
            loading: true,
            type: '',
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
        this.$nextTick(function () {
            this.getHeight()
        })
        window.addEventListener('resize', this.getHeight(), false)
    },

    methods: {
        //初始化充值列表数据
        async axios() {
            const res = await topList({
                api: 'admin.user.getupInfo',
                pageNo: this.dictionaryNum,
                pageSize: this.pageSize,
                key: this.inputInfo.userName,
                oType: this.inputInfo.oType,
                startDate: this.inputInfo.date?.[0] ?? '',
                endDate: this.inputInfo.date?.[1] ?? '',
                // exportType:this.exportType??'',
            })
            console.log('res:', res);

            if (res.data.code == 200) {
                this.loading = false
                this.tableData = res.data.data.list
                this.allMoney = res.data.data.allMoney
                this.total = res.data.data.total
                if (res.data.data.total < 10) {
                    this.current_num = this.total
                }
                console.log('tableData:', this.tableData);
            }

        },
        // 获取table高度
        getHeight() {
            this.tableHeight = this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
            console.log(this.$refs.tableFather.clientHeight);
        },

        // 重置
        reset() {
            this.inputInfo.userName = '',
                this.inputInfo.oType = '',
                this.inputInfo.date = ''
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
                if (this.tableData.length > 5) {
                    this.showPagebox = true
                }
            })
        },

        //选择一页多少条
        handleSizeChange(e) {
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
        handleCurrentChange(e) {
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
                api: 'admin.user.getupInfo',
                pageNo: this.dictionaryNum,
                pageSize: this.pageSize,
                key: this.inputInfo.userName,
                oType: this.inputInfo.oType,
                startDate: this.inputInfo.date?.[0] ?? '',
                endDate: this.inputInfo.date?.[1] ?? '',
                exportType: 1,
            }, '充值列表_导出本页')
        },

        async exportAll() {
            exports({
                api: 'admin.user.getupInfo',
                pageNo: 1,
                pageSize: 9999,
                exportType: 1,
                // key:this.inputInfo.userName,
                // oType:this.inputInfo.oType,
                // startDate: this.inputInfo.date?.[0]??'',
                // endDate: this.inputInfo.date?.[1]??'',
            }, '充值列表_导出全部')
        },

        async exportQuery() {
            exports({
                api: 'admin.user.getupInfo',
                pageNo: 1,
                pageSize: this.total,
                exportType: 1,
                key: this.inputInfo.userName,
                oType: this.inputInfo.oType,
                startDate: this.inputInfo.date?.[0] ?? '',
                endDate: this.inputInfo.date?.[1] ?? '',
            }, '充值列表_导出查询')
        }
    }
}
