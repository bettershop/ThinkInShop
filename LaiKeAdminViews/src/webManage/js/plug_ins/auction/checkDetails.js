import { checkDetails} from '@/api/plug_ins/auction'
import { mixinstest } from '@/mixins/index'
import moment from 'moment'
export default {
    name: 'checkDetails',
    mixins: [mixinstest],
    data() {
        return {
            acId:'',//竞拍商品
            inputInfo: {
                key:'',//关键字 竞拍id、用户id、用户名称
            },
            button_list:[],
            tableData: [],
            loading: true,

            // table高度
            tableHeight: null
        }
    },

    created() {
        if(this.$route.params.pageSize) {
            this.pagination.page = this.$route.params.dictionaryNum
            this.dictionaryNum = this.$route.params.dictionaryNum
            this.pageSize = this.$route.params.pageSize
        }
        this.acId = this.$route.query.acId
        console.log('acId',this.acId);
        
        this.getList()
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
            console.log(this.$refs.tableFather.clientHeight);
		},
        async getList(){
            const res = await checkDetails({
                api:'plugin.auction.AdminAuction.bidList',
                accessId: this.$store.getters.token,
                acId:this.acId,
                key:this.inputInfo.key,
                pageNo: this.dictionaryNum,
                pageSize: this.pageSize,
            })
            console.log('res',res);
            if(res.data.code == 200){
                this.total = res.data.data.total
                this.tableData = res.data.data.list
                this.loading = false
            }
        },
         // 重置
         reset() {
            this.inputInfo.key = ''
        },
  
        // 查询
        demand() {
           this.getList()
        },

        //选择一页多少条
        handleSizeChange(e){
            this.loading = true
            console.log(e);
            // this.current_num = e
            this.pageSize = e
            this.getList().then(() => {
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
            this.getList().then(() => {
                this.current_num = this.tableData.length === this.pageSize ? e * this.pageSize : this.total
                this.loading = false
            })
        },

    }
}