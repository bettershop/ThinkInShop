import { geteventsList ,switchShow , delEvents} from '@/api/plug_ins/auction'
import { mixinstest } from '@/mixins/index'
import moment from 'moment'
export default {
    name: 'eventsList',
    mixins: [mixinstest],
    data() {
        return {
            id:'',//专场id
            inputInfo: {
                key:'',//关键字 id/专场名称/店铺名称
                status:'',//状态 1=未开始 2=进行中 3=已结束 4=已拍卖 5=已流拍
                time:'',//时间
            },
            options: [{
                value: '1',
                label: this.$t('auction.eventsList.wks')
              }, {
                value: '2',
                label: this.$t('auction.eventsList.jxz'),
              }, {
                value: '3',
                label: this.$t('auction.eventsList.yjs')
              }],
            //   , {
            //     value: '4',
            //     label: '已拍卖'
            //   }, {
            //     value: '5',
            //     label: '已流拍'
            //   }],
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
        if(this.$route.query.id){
            this.id = this.$route.query.id
            console.log('id',this.id);
        }
        
        this.geteventsLists()
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
        async geteventsLists(){
            const res = await geteventsList({
                api:'plugin.auction.AdminAuction.getSessionList',
                accessId: this.$store.getters.token,
                key:this.inputInfo.key,
                status:this.inputInfo.status,
                startDate:this.inputInfo.time?.[0]??'',
                endDate:this.inputInfo.time?.[1]??'',
                // id:this.id,//场次id
                specialId:this.id,//专场id
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
        //发布场次
        addevents(){
            this.$router.push({
                path: '/plug_ins/auction/releaseEvents',
                query: {
                    type: 1,
                    kep_id:this.id,
                  }
              })
        },
        //查看拍品
        seeCheck(row){
            console.log('row',row);
            this.$router.push({
                path: '/plug_ins/auction/seeCheck',
                query:{
                    id:row.id,
                    statusName:row.statusName,
                }
              })
        },
        // 是否显示
        switchs(row) {
            switchShow({
                api: 'plugin.auction.AdminAuction.switchSession',
                accessId: this.$store.getters.token,
                id: row.id
            }).then(res => {
                if(res.data.code == '200') {
                    this.geteventsLists()
                    this.$message({
                        type: 'success',
                        message:this.$t('auction.cg'),
                        offset: 100
                    })
                }
            })
        },
         // 重置
         reset() {
            this.inputInfo.key = ''
            this.inputInfo.status = ''
            this.inputInfo.time = ''
        },
  
        // 查询
        demand() {
           this.geteventsLists()
        },

        // 查看
        View(value) {
            this.$router.push({
                path: '/plug_ins/coupons/viewCoupons',
                query: {
                    id: value.id,
                    dictionaryNum: this.dictionaryNum,
                    pageSize: this.pageSize
                }
            })
        },

        // 编辑
        Edit(row) {
            this.$router.push({
                path: '/plug_ins/auction/releaseEvents',
                query: {
                    id: row.id,
                    type:2,
                    kep_id:this.id,
                }
            })
        },

        Delete(row) {
            this.$confirm(this.$t('auction.eventsList.scts'), this.$t('auction.ts'), {
                confirmButtonText: this.$t('auction.okk'),
                cancelButtonText: this.$t('auction.ccel'),
                type: 'warning'
              }).then(() => {
                delEvents({
                    api: 'plugin.auction.AdminAuction.delSession',
                    id: row.id
                }).then(res => {
                    console.log(res);
                    if(res.data.code == '200') {
                        this.geteventsLists()
                        this.$message({
                            type: 'success',
                            message: this.$t('auction.cg'),
                            offset: 100
                        })
                    }
                })
            }).catch(() => {
            // this.$message({
            //     type: 'info',
            //     message: '已取消删除',
            //     offset: 100
            // });          
            });
        },

        // 领取记录
        Receive(value) {
            this.$router.push({
                path: '/plug_ins/coupons/getRecord',
                query: {
                    id: value.id
                }
            })
        },

        // 赠送记录
        Giving(value) {
            this.$router.push({
                path: '/plug_ins/coupons/givingRecords',
                query: {
                    id: value.id
                }
            })
        },

        //选择一页多少条
        handleSizeChange(e){
            this.loading = true
            console.log(e);
            // this.current_num = e
            this.pageSize = e
            this.geteventsLists().then(() => {
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
            this.geteventsLists().then(() => {
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

        addUser() {
            if(this.pageSize < this.userTotal || this.pageSize - this.userTotal < 10) {
                this.loadings = true
                this.pageSize += 10
                getGiveUserInfo({
                    api: 'plugin.coupon.Admincoupon.ReceiveUserCoupon',
                    pageNo: this.dictionaryNum,
                    pageSize: this.pageSize,
                    name: this.uname,
                    grade: this.grade,
                    hid: this.hid
                }).then(res => {
                    this.ProList = res.data.data.userList
                    this.loadings = false
                })
            } else {
                this.is_show = false
            }
        },

        // 获取会员等级列表
        async goodsStatus() {
            const res = await goodsStatus({
                api: 'admin.user.goodsStatus'
            })

            let levelList = res.data.data.map(item => {
                return {
                    value: item.id,
                    label: item.name
                }
            })
            levelList.unshift({
                value: 0,
                label: '普通会员'
            })

            this.gradeList = levelList
        },
        Reset() {
            this.grade = null,
            this.userName = null
        },

        query() {
            this.pageSize = 10
            this.is_show = true
            this.getGiveUserInfos(this.hid)
            this.$refs.multipleTable.clearSelection()
        },

        rowKeys(row) {
            return row.id
        },

        handleSelectionChange2(e) {
            this.ChangeProList = e
        },

        DeleteP(index,value) {
            this.ChangeProList.splice(index,1)
            this.$refs.multipleTable.toggleRowSelection(this.ProList.find((item) => {
                return value.user_id == item.user_id
            }),false)
        },

        givingCoupons(value) {
            this.getGiveUserInfos(value.id)
            this.hid = value.id
            this.pageSize = 10
            this.is_show = true
            this.dialogVisible3 = true
        },

        handleClose2() {
            this.dialogVisible3 = false
            this.ProList = []
            this.ChangeProList = []
            this.userTotal = null
            this.selectUser = null
            this.grade = null
            this.userName = null
            this.$refs.multipleTable.clearSelection()
        },

        AddProconfirm() {
            receiveUserCoupon({
                api: 'plugin.coupon.Admincoupon.ReceiveUserCoupon',
                userIds: this.userIdList.join(','),
                hid: this.hid
            }).then(res => {
                if(res.data.code == '200') {
                    this.$message({
                        message: '赠送成功',
                        type: 'success',
                        offset:100
                    })
                    this.handleClose2()
                }
            })
        },
    }
}