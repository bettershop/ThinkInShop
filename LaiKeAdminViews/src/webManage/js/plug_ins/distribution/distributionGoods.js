import { mixinstest } from '@/mixins/index'
import { getStorage } from '@/utils/storage'
import { getButton } from '@/api/layout/information'
import { getGoodsDistributionInfo, getDistributionGradeList, delDistributionGoods } from '@/api/plug_ins/distribution'
export default {
    name: 'distributionGoods',
    mixins: [mixinstest],

    data() {
        return {
            routerList:JSON.parse(sessionStorage.getItem('tabRouter')),
            radio1:'2',
            inputInfo: {
                no: null,
                goodsName: null,
                level: null,
                mchName:null
            },
            levelList: [],// 分销等级
            idList: [],
            button_list:[],
            tableData: [],
            is_disabled: true,
            loading: true,

            // table高度
            tableHeight: null,
            showPagebox:true
        }
    },

    created() {
        this.getGoodsDistributionInfo()
        this.getDistributionGradeLists()
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

        async getGoodsDistributionInfo() {
            const res = await getGoodsDistributionInfo({
                api: 'plugin.distribution.AdminDistribution.getGoodsDistributionInfo',
                pageSize: this.pageSize,
                pageNo: this.dictionaryNum,
                goodsNo: this.inputInfo.no,
                goodsName: this.inputInfo.goodsName,
                level: this.inputInfo.level,
                mchName:this.inputInfo.mchName
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

        reset() {
            this.inputInfo.no = null
            this.inputInfo.goodsName = null
            this.inputInfo.level = null
            this.inputInfo.mchName=null
        },
      
        demand() {
            this.currpage = 1
            this.current_num = 10
            this.showPagebox = false
            this.loading = true
            this.dictionaryNum = 1
            this.getGoodsDistributionInfo().then(() => {
              this.loading = false
              if(this.tableData.length > 0) {
                this.showPagebox = true
              }
            })
        },

        addGoods() {
            this.$router.push({
                path: '/plug_ins/distribution/addGoods',
            })
        },

        // 批量删除
        delAll() {
            this.$confirm(this.$t('distribution.distributionGoods.sfsc'), this.$t('distribution.ts'), {
                confirmButtonText: this.$t('distribution.okk'),
                cancelButtonText: this.$t('distribution.ccel'),
                type: 'warning'
            }).then(() => {
                this.idList = this.idList.map(item => {
                    return item.id
                })
                this.idList = this.idList.join(',')
                delDistributionGoods({
                    api: 'plugin.distribution.AdminDistribution.delDistributionGoods',
                    ids: this.idList
                }).then(res => {
                    if(res.data.code == '200') {
                        console.log(res);
                        this.getGoodsDistributionInfo()
                        this.$message({
                            type: 'success',
                            message: this.$t('distribution.sccg'),
                            offset: 100
                        })
                    }
                })
            }).catch(() => {
                // this.$message({
                //     type: 'info',
                //     message: '已取消删除'
                // })
            });
        },

        Edit(value) {
            this.$router.push({
                path: '/plug_ins/distribution/editorGoods',
                query: {
                    id: value.id
                }
            })
        },

        Delete(value) {
            this.$confirm(this.$t('distribution.distributionGoods.sfsc'), this.$t('distribution.ts'), {
                confirmButtonText: this.$t('distribution.okk'),
                cancelButtonText: this.$t('distribution.ccel'),
                type: 'warning'
            }).then(() => {
                delDistributionGoods({
                    api: 'plugin.distribution.AdminDistribution.delDistributionGoods',
                    ids: value.id
                }).then(res => {
                    if(res.data.code == '200') {
                        console.log(res);
                        this.getGoodsDistributionInfo()
                        this.$message({
                            type: 'success',
                            message: this.$t('distribution.sccg'),
                            offset: 100
                        })
                    }
                })
            }).catch(() => {
                // this.$message({
                //     type: 'info',
                //     message: '已取消删除'
                // })
            })
        },

        //选择一页多少条
        handleSizeChange(e){
            this.loading = true
            console.log(e);
            this.pageSize = e
            this.getGoodsDistributionInfo().then(() => {
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
            this.getGoodsDistributionInfo().then(() => {
                this.current_num = this.tableData.length === this.pageSize ? e * this.pageSize : this.total
                this.loading = false
            })
        },

        // 选框改变
        handleSelectionChange(val) {
            if(val.length > 0) {
                this.is_disabled = false
                this.idList = val
            } else {
                this.is_disabled = true
            }
            console.log(val);
        },
    }
}