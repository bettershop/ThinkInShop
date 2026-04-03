import { mixinstest } from '@/mixins/index'
import { getStorage } from '@/utils/storage'
import { getButton } from '@/api/layout/information'
import { exports } from '@/api/export/index'
import { getRecordInfo } from '@/api/plug_ins/distribution'
export default {
    name: 'commissionRecord',
    mixins: [mixinstest],

    data() {
        return {
            routerList:JSON.parse(sessionStorage.getItem('tabRouter')),
            radio1:'5',
            inputInfo: {
                userName: null,
                fromId: null,
                type: null,
                date: null
            },
            levelList: [
                {
                    value: '1',
                    label: this.$t('distribution.commissionRecord.ztfy')
                },
                {
                    value: '2',
                    label: this.$t('distribution.commissionRecord.jtfy')
                },
                {
                    value: '3',
                    label: this.$t('distribution.commissionRecord.pjj')
                },
                {
                    value: '4',
                    label: this.$t('distribution.commissionRecord.jcj')
                }
            ],// 分销等级

            tableData: [],

            // table高度
            tableHeight: null,
            button_list:[],
            loading: true,

            // 导出弹框数据
            dialogVisible: false,
        }
    },

    created() {
        this.getRecordInfo()
        this.getButtons()
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

        //获取按纽权限
        async getButtons() {
            let route=getStorage('route')
            route.forEach(item => {
                if(item.path=='seckill'){
                    item.children.forEach(e => {
                        if(e.path=='seckillOrder'){
                        return this.menuId=e.id
                        }
                    })
                }
            });

            let buttonList = await getButton ({
            api:'saas.role.getButton',
            menuId:this.menuId,
            })
            this.button_list=buttonList.data.data
        },

        async getRecordInfo() {
            const res = await getRecordInfo({
                api: 'plugin.distribution.AdminDistribution.getRecordInfo',
                pageSize: this.pageSize,
                pageNo: this.dictionaryNum,
                userName: this.inputInfo.userName,
                fromId: this.inputInfo.fromId,
                type: this.inputInfo.type,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
            })
            console.log(res)
            this.total = res.data.data.total
            this.tableData = res.data.data.list
            this.loading = false
            if(this.total < this.current_num) {
                this.current_num = this.total
            }
        },

        reset() {
            this.inputInfo.userName = null
            this.inputInfo.fromId = null
            this.inputInfo.type = null
            this.inputInfo.date = null
        },

        demand() {
            this.currpage = 1
            this.current_num = 10
            this.showPagebox = false
            this.loading = true
            this.dictionaryNum = 1
            this.getRecordInfo().then(() => {
              this.loading = false
              if(this.tableData.length > 5) {
                this.showPagebox = true
              }
            })
        },

        //选择一页多少条
        handleSizeChange(e){
            this.loading = true
            console.log(e);
            this.pageSize = e
            this.getRecordInfo().then(() => {
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
            this.getRecordInfo().then(() => {
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
                api: 'plugin.distribution.AdminDistribution.getRecordInfo',
                pageSize: this.pageSize,
                pageNo: this.dictionaryNum,
                exportType: 1,
                userName: this.inputInfo.userName,
                fromId: this.inputInfo.fromId,
                type: this.inputInfo.type,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
            },'pageorder')
        },

        async exportAll() {
            exports({
                api: 'plugin.distribution.AdminDistribution.getRecordInfo',
                pageNo: 1,
                pageSize: this.total,
                exportType: 1,
                userName: this.inputInfo.userName,
                fromId: this.inputInfo.fromId,
                type: this.inputInfo.type,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
            },'allorder')
        },

        async exportQuery() {
            exports({
                api: 'plugin.distribution.AdminDistribution.getRecordInfo',
                pageNo: 1,
                pageSize: this.total,
                exportType: 1,
                userName: this.inputInfo.userName,
                fromId: this.inputInfo.fromId,
                type: this.inputInfo.type,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
            },'queryorder')
        }

    }
}
