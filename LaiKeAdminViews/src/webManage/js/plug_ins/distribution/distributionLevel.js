import { mixinstest } from '@/mixins/index'
import { getStorage } from '@/utils/storage'
import { getButton } from '@/api/layout/information'
import { getGradeInfo, delGrade } from '@/api/plug_ins/distribution'
export default {
    name: 'distributionLevel',
    mixins: [mixinstest],

    data() {
        return {
            routerList:JSON.parse(sessionStorage.getItem('tabRouter')),
            radio1:'3',
            button_list:[],
            tableData: [
                {
                    levelId: 1,
                    levelName: '县级',
                    promotionCondition: '一次性消费满400元'
                }
            ],
            loading: true,
            // table高度
            tableHeight: null,
        }
    },

    created() {
        this.getGradeInfos()
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

        async getGradeInfos() {
            const res = await getGradeInfo({
                api: 'plugin.distribution.AdminDistribution.getGradeInfo',
                pageSize: this.pageSize,
                pageNo: this.dictionaryNum,
            })
            console.log(res);
            this.total = res.data.data.total
            this.tableData = res.data.data.list
            this.loading = false
            if(this.total < this.current_num) {
                this.current_num = this.total
            }
        },

        // 获取table高度
        getHeight(){
			this.tableHeight = this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
		},

        addLevel() {
            this.$router.push({
                path: '/plug_ins/distribution/addLevel'
            })
        },

        Edit(value) {
            this.$router.push({
                path: '/plug_ins/distribution/editorLevel',
                query: {
                    id: value.id
                }
            })
        },

        Delete(value) {
            this.$confirm(this.$t('distribution.distributionLevel.qsrc'), this.$t('distribution.ts'), {
                confirmButtonText: this.$t('distribution.okk'),
                cancelButtonText: this.$t('distribution.ccel'),
                type: 'warning'
              }).then(() => {
                delGrade({
                    api: 'plugin.distribution.AdminDistribution.delGrade',
                    id: value.id
                }).then(res => {
                    console.log(res);
                    if(res.data.code == '200') {
                        this.getGradeInfos()
                        this.$message({
                            type: 'success',
                            message: this.$t('distribution.sccg'),
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
            })
        },

        //选择一页多少条
        handleSizeChange(e){
            this.loading = true
            console.log(e);
            this.pageSize = e
            this.getGradeInfos().then(() => {
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
            this.getGradeInfos().then(() => {
                this.current_num = this.tableData.length === this.pageSize ? e * this.pageSize : this.total
                this.loading = false
            })
        },
    }
}