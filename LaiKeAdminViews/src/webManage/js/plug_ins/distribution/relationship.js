import { mixinstest } from '@/mixins/index'
import { getStorage } from '@/utils/storage'
import { getButton } from '@/api/layout/information'
import { getRelationshipInfo } from '@/api/plug_ins/distribution'
export default {
    name: 'relationship',
    mixins: [mixinstest],

    data() {
        return {
            routerList:JSON.parse(sessionStorage.getItem('tabRouter')),
            radio1:'6',
            inputInfo: {
                user_id: null,
                referees_id: null,
                type: null,
                date: null
            },
            typeList: [
                {
                    value: '1',
                    label: '临时'
                },
                {
                    value: '2',
                    label: '永久'
                }
            ],// 类型
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         
            // table高度
            tableHeight: null,
            button_list:[],
            tableData: [],
            loading: true,
            showPagebox:true
        }
    },

    created() {
        this.getRelationshipInfos()
        this.typeList=[
            {
                value: '1',
                label: this.$t('distribution.relationship.ls')
            },
            {
                value: '2',
                label: this.$t('distribution.relationship.yj')
            }
        ]
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

        async getRelationshipInfos() {
            const res = await getRelationshipInfo({
                api: 'plugin.distribution.AdminDistribution.getRelationshipInfo',
                pageSize: this.pageSize,
                pageNo: this.dictionaryNum,
                userName: this.inputInfo.user_id,
                fatherId: this.inputInfo.referees_id,
                type: this.inputInfo.type,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
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

        reset() {
            this.inputInfo.user_id = null
            this.inputInfo.referees_id = null
            this.inputInfo.type = null
            this.inputInfo.date = null
        },
      
        demand() {
            this.currpage = 1
            this.current_num = 10
            this.showPagebox = false
            this.loading = true
            this.dictionaryNum = 1
            this.getRelationshipInfos().then(() => {
              this.loading = false
              if(this.tableData.length > 0) {
                this.showPagebox = true
              }
            })
        },

        //选择一页多少条
        handleSizeChange(e){
            this.loading = true
            console.log(e);
            this.pageSize = e
            this.getRelationshipInfos().then(() => {
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
            this.getRelationshipInfos().then(() => {
                this.current_num = this.tableData.length === this.pageSize ? e * this.pageSize : this.total
                this.loading = false
            })
        },

    }
}