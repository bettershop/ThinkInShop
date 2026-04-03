import { getStorage } from '@/utils/storage'
import { getButton } from '@/api/layout/information'
import { mixinstest } from '@/mixins/index'
import { exports } from '@/api/export/index'
import { getDistributionInfo, getDistributionGradeList } from '@/api/plug_ins/distribution'
export default {
    name: 'distributorsList',
    mixins: [mixinstest],

    data() {
        return {
            inputInfo: {
                name: null,
                phone: null,
                level: null,
                date: null,
                sort: null,
                sequence: null
            },
            levelList: [],// 分销等级

            sortList: [
                {
                    value: 'user_id',
                    label: this.$t('distribution.viewSubordinate.yhid')
                },
                {
                    value: 'tx_commission',
                    label: this.$t('distribution.viewSubordinate.ljyj')
                },
                {
                    value: 'add_date',
                    label: this.$t('distribution.viewSubordinate.jssj')
                }
            ],// 排序字段

            sequenceList: [
                {
                    value: '2',
                    label: this.$t('distribution.viewSubordinate.sx')
                },
                {
                    value: '1',
                    label: this.$t('distribution.viewSubordinate.jx')
                }
            ],// 排序

            tableData: [],
            loading: false,
            button_list:[],
            
            // table高度
            tableHeight: null,

            // 导出弹框数据
            dialogVisible: false,
            showPagebox:true
        }
    },

    created() {
        this.getDistributionInfos()
        this.getDistributionGradeLists()
        this.getButtons()
    },

    mounted() {
        this.$nextTick(function() {
            this.getHeight()
        })
        window.addEventListener('resize',this.getHeight(),false)
    },

    methods: {
        rowstyles({row,rowIndex}){
            let styleJson = {
                "border":"none"
            };
            return styleJson
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
            console.log(this.button_list,"获取按纽权限")
        },

        async getDistributionInfos() {
            const res = await getDistributionInfo({
                api: 'plugin.distribution.AdminDistribution.getDistributionInfo',
                pageSize: this.pageSize,
                pageNo: this.dictionaryNum,
                fatherUid: this.$route.query.id,
                userName: this.inputInfo.name,
                phone: this.inputInfo.phone,
                grade: this.inputInfo.level,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
                sortColumn: this.inputInfo.sort,
                sortType: this.inputInfo.sequence
            })
            console.log(res);
            this.total = res.data.data.total
            this.tableData = res.data.data.list
            this.loading = false
            if(this.total < this.current_num) {
                this.current_num = this.total
            }
            if(this.tableData.length > 0) {
                this.showPagebox = true
            }else{
                this.showPagebox=false
            }
        },

        async getDistributionGradeLists() {
            const res = await getDistributionGradeList({
                api: 'plugin.distribution.AdminDistribution.getDistributionGradeList'
            })
            this.levelList = res.data.data.gradeInfoList
        },
        
        // 获取table高度
        getHeight(){
			this.tableHeight = this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
		},
        
        // 重置
        reset() {
            this.inputInfo.name = null
            this.inputInfo.phone = null
            this.inputInfo.level = null
            this.inputInfo.date = null
            this.inputInfo.sort = null
            this.inputInfo.sequence = null
        },
  
        // 查询
        demand() {
            this.currpage = 1
            this.current_num = 10
            this.showPagebox = false
            this.loading = true
            this.dictionaryNum = 1
            this.getDistributionInfos().then(() => {
                this.loading = false
                if(this.tableData.length > 0) {
                    this.showPagebox = true
                }
            })
        },

        //选择一页多少条
        handleSizeChange(e){
            this.loading = true
            this.pageSize = e
            this.getDistributionInfos().then(() => {
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
            this.getDistributionInfos().then(() => {
                this.current_num = this.tableData.length === this.pageSize ? e * this.pageSize : this.total
                this.loading = false
            })
        },

        dialogShow2(value,tag) {
            this.dialogVisible2 = true
            this.toggle = tag
        },

        handleClose2() {
            this.dialogVisible2 = false
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
                api: 'plugin.distribution.AdminDistribution.getDistributionInfo',
                pageSize: this.pageSize,
                pageNo: this.dictionaryNum,
                exportType: 1,
                fatherUid: this.$route.query.id,
                userName: this.inputInfo.name,
                phone: this.inputInfo.phone,
                grade: this.inputInfo.level,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
                sortColumn: this.inputInfo.sort,
                sortType: this.inputInfo.sequence
            },'pageorder')
        },
    
        async exportAll() {
            exports({
                api: 'plugin.distribution.AdminDistribution.getDistributionInfo',
                pageNo: 1,
                pageSize: this.total,
                exportType: 1,
                fatherUid: this.$route.query.id,
                userName: this.inputInfo.name,
                phone: this.inputInfo.phone,
                grade: this.inputInfo.level,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
                sortColumn: this.inputInfo.sort,
                sortType: this.inputInfo.sequence
            },'allorder')
        },
    
        async exportQuery() {
            exports({
                api: 'plugin.distribution.AdminDistribution.getDistributionInfo',
                pageSize: this.total,
                pageNo: this.dictionaryNum,
                exportType: 1,
                fatherUid: this.$route.query.id,
                userName: this.inputInfo.name,
                phone: this.inputInfo.phone,
                grade: this.inputInfo.level,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
                sortColumn: this.inputInfo.sort,
                sortType: this.inputInfo.sequence
            },'queryorder')
        }
    }
}