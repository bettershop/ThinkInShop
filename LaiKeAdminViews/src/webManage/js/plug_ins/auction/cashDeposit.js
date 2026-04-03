import { getCashList , delcash } from '@/api/plug_ins/auction'
import { mixinstest } from '@/mixins/index'
import { getStorage } from '@/utils/storage'
import { getButton } from '@/api/layout/information'
export default {
    name: 'cashDeposit',
    mixins: [mixinstest],
    data() {
        return {
            radio1:'保证金记录',

            cashTypeList: [
                {
                    value: '0',
                    label: '待缴纳'
                },
                {
                    value: '1',
                    label: '已缴纳'
                },
                {
                    value: '2',
                    label: '已退还'
                }
            ],// 优惠券类型

            inputInfo: {
                cashid: '',//用户ID/用户名称/联系电话
                name: '',//专场名称
                cashType: '', //操作类型
                time:'',//时间
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
        this.getCashLists()
        // this.goodsStatus()
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
        async getCashLists(){
            const res = await getCashList({
                api:'plugin.auction.AdminAuction.getPromiseList',
                accessId: this.$store.getters.token,
                key:this.inputInfo.cashid,
                specialName:this.inputInfo.name,
                type:this.inputInfo.cashType,
                startDate:this.inputInfo.time?.[0]??'',
                endDate:this.inputInfo.time?.[1]??'',
                pageNo: this.dictionaryNum,
                pageSize: this.pageSize,
            })
            if(res.data.code == 200){
                this.total = res.data.data.total
                this.tableData = res.data.data.list
                this.loading = false
            }
            console.log('res',res);
            
        },

         // 重置
         reset() {
            this.inputInfo.cashid = ''
            this.inputInfo.name = ''
            this.inputInfo.cashType = ''
            this.inputInfo.time = ''
        },
        search(){
            this.getCashLists()
        },


        // 是否显示
        switchs(value) {
            activityisDisplay({
                api: 'plugin.coupon.Admincoupon.ActivityisDisplay',
                hid: value.id
            }).then(res => {
                if(res.data.code == '200') {
                    this.getCouponCardInfos()
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
        //     this.$router.push({
        //         path: '/plug_ins/coupons/viewCoupons',
        //         query: {
        //             id: value.id,
        //             dictionaryNum: this.dictionaryNum,
        //             pageSize: this.pageSize
        //         }
        //     })
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
        //删除保证金记录
        Delete(row) {
            this.$confirm('是否删除此保证金记录？', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
              }).then(() => {
                delcash({
                    api: 'plugin.auction.AdminAuction.delPromise',
                    accessId: this.$store.getters.token,
                    id: row.id //保证金id
                }).then(res => {
                    if(res.data.code == '200') {
                        this.getCashLists()
                        this.$message({
                            type: 'success',
                            message: '删除成功!',
                            offset: 100
                        })
                    }
                })
            }).catch(() => {
            this.$message({
                type: 'info',
                message: '已取消删除',
                offset: 100
            });          
            });
        },


        //选择一页多少条
        handleSizeChange(e){
            this.loading = true
            console.log(e);
            // this.current_num = e
            this.pageSize = e
            this.getCashLists().then(() => {
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
            this.getCashLists().then(() => {
                this.current_num = this.tableData.length === this.pageSize ? e * this.pageSize : this.total
                this.loading = false
            })
        },

        // 弹框方法
        async getGiveUserInfos(value) {
            const res = await getGiveUserInfo({
                api: 'plugin.coupon.Admincoupon.ReceiveUserCoupon',
                pageNo: this.dictionaryNum,
                pageSize: this.pageSize,
                name: this.userName,
                grade: this.grade,
                hid: value
            })
            console.log(res);
            this.is_show = true
            this.userTotal = res.data.data.total
            this.ProList = res.data.data.userList
            if(this.ProList.length < 6) {
                this.is_show = false
            }
        },
    }
}