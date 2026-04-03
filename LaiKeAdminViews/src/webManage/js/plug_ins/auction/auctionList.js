
import {getAuctionList , switchShow} from '@/api/plug_ins/auction'
import { mixinstest } from '@/mixins/index'
import { getStorage } from '@/utils/storage'
import { getButton } from '@/api/layout/information'
export default {
    name: 'auctionList',
    mixins: [mixinstest],
    data() {
        return {
            radio1:'竞拍商品',
            statusList: [{
                value: '0',
                label: '待拍卖'
              }, {
                value: '1',
                label: '拍卖中'
              }, {
                value: '2',
                label: '已拍卖'
              }, {
                value: '3',
                label: '已流拍'
              }],//状态列表

            inputInfo: {
                auctionName:'',//用户ID/拍品名称/店铺名称
                status: '',//状态
                sessionName: '', //场次名称
            },
            button_list:[],
            tableData: [],
            loading: true,

            // 弹框数据
            ProData: [],
            tabKey: '0',
            gradeList: [],
            ProList: [],
            ChangeProList: [],

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
        this.getAuctionLists()
        this.getButtons()
    },

    mounted() {
        this.$nextTick(function() {
            this.getHeight()
        })
        window.addEventListener('resize',this.getHeight(),false)
    },

    watch: {
        'ChangeProList': {
            handler:function(val,oldval){
                this.selectUser = this.ChangeProList.length
                this.userIdList = this.ChangeProList.map(item => {
                    return item.user_id
                })
            }
        }
    },

    methods: {
        // 获取table高度
        getHeight(){
			this.tableHeight = this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
            console.log(this.$refs.tableFather.clientHeight);
		},
        //获取按纽权限
        async getButtons() {
            let route=getStorage('route')
            route.forEach(item => {
                if(item.path=='coupons'){                
                    return this.menuId=item.id                                        
                }
            });
            let buttonList = await getButton ({
            api:'saas.role.getButton',
            menuId: this.menuId,
            })
            this.button_list=buttonList.data.data
        },
        //查询table
        search(){
            this.getAuctionLists()
        },
        //获取竞拍商品列表
        async getAuctionLists(){
            const res = await getAuctionList({
                api: 'plugin.admin.auction.goods.getGoodsList',
                accessId: this.$store.getters.token,
                key:this.inputInfo.auctionName,
                status:this.inputInfo.status,
                name:this.inputInfo.sessionName,
                pageNo: this.dictionaryNum,
                pageSize: this.pageSize,
            })
            if(res.data.code == 200){
                this.tableData = res.data.data.list
                this.total = res.data.data.total
                this.loading = false
            }
            console.log('res',res);
        },
         // 重置
         reset() {
            this.inputInfo.auctionName = null
            this.inputInfo.status = null
            this.inputInfo.sessionName = null
        },

        // 是否显示
        switchs(value) {
            switchShow({
                api: 'plugin.admin.auction.goods.switch',
                accessId: this.$store.getters.token,
                id: value.acId
            }).then(res => {
                if(res.data.code == '200') {
                    this.getAuctionLists()
                    this.$message({
                        type: 'success',
                        message: '成功!',
                        offset: 100
                    })
                }
            })
        },

        // 查看
        View(value) {
            // this.$router.push({
            //     path: '/plug_ins/coupons/viewCoupons',
            //     query: {
            //         id: value.id,
            //         dictionaryNum: this.dictionaryNum,
            //         pageSize: this.pageSize
            //     }
            // })
        },

        // 编辑
        Edit(value) {
            // this.$router.push({
            //     path: '/plug_ins/coupons/editorCoupons',
            //     query: {
            //         id: value.id,
            //         dictionaryNum: this.dictionaryNum,
            //         pageSize: this.pageSize
            //     }
            // })
        },


        //选择一页多少条
        handleSizeChange(e){
            this.loading = true
            console.log('e',e);
            this.pageSize = e
            this.getAuctionLists().then(() => {
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
            this.getAuctionLists().then(() => {
                this.current_num = this.tableData.length === this.pageSize ? e * this.pageSize : this.total
                this.loading = false
            })
        },

    }
}