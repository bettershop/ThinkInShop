import { delectBid, seebidList } from '@/api/plug_ins/auction'
import { mixinstest } from '@/mixins/index'
import moment from 'moment'
export default {
    name: 'cashDeposit',
    mixins: [mixinstest],
    data() {
        return {
            specialId: '',//专场id-跳转过来时带到url中
            sessionId: '',//场次id-跳转过来时带到url中
            statusName: '',//状态
            couponsTypeList: [
                {
                    value: '1',
                    label: this.$t('auction.seeCheck.myq')
                },
                {
                    value: '2',
                    label: this.$t('auction.seeCheck.mjq')
                },
                {
                    value: '3',
                    label: this.$t('auction.seeCheck.zkq')
                }
            ],// 优惠券类型

            overdueList: [
                {
                    value: '0',
                    label: this.$t('auction.seeCheck.ygq')
                },
                {
                    value: '1',
                    label: this.$t('auction.seeCheck.wgq')
                },
            ],// 是否过期

            inputInfo: {
                bidName: '',
            },
            button_list: [],
            tableData: [],
            loading: true,

            // table高度
            tableHeight: null
        }
    },

    created() {
        if (this.$route.params.pageSize) {
            this.pagination.page = this.$route.params.dictionaryNum
            this.dictionaryNum = this.$route.params.dictionaryNum
            this.pageSize = this.$route.params.pageSize
        }
        this.specialId = this.$route.query.specialId//专场
        this.sessionId = this.$route.query.id//场次
        this.statusName = this.$route.query.statusName
        console.log('specialId', this.specialId);
        console.log('statusName', this.statusName);
        this.getbidList()
    },

    mounted() {
        this.$nextTick(function () {
            this.getHeight()
        })
        window.addEventListener('resize', this.getHeight(), false)
    },

    methods: {
        // 获取table高度
        getHeight() {
            this.tableHeight = this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
            console.log(this.tableHeight, 'this.tableHeight')
        },
        async getbidList() {
            const { data: res } = await seebidList({
                api: 'plugin.auction.AdminAuction.lookGoodsList',
                accessId: this.$store.getters.token,
                goodsName: this.inputInfo.bidName,//拍品名称
                specialId: this.specialId,//专场id-跳转过来时带到url中
                sessionId: this.sessionId,//场次id-跳转过来时带到url中
                pageNo: this.dictionaryNum,
                pageSize: this.pageSize,
            })
            if (res.code == 200) {
                this.total = res.data.total
                this.tableData = res.data.list
                if (this.tableData.length < 10) {
                    this.current_num = this.tableData.length
                }
                this.loading = false
                console.log('tableData', this.tableData);
            }
        },
        addevents() {
            this.$router.push({
                path: '/plug_ins/auction/releaseEvents'
            })
        },
        goDetails(row) {
            console.log('row', row);
            this.$router.push({
                path: '/plug_ins/auction/checkDetails',
                query: {
                    acId: row.id
                }
            })
        },
        // 重置
        reset() {
            this.inputInfo.bidName = ''
        },

        // 查询
        demand() {
            this.getbidList()
        },


        Delete(row) {
            console.log('row', row);
            this.$confirm(this.$t('auction.seeCheck.scqr'), this.$t('auction.ts'), {
                confirmButtonText: this.$t('auction.okk'),
                cancelButtonText: this.$t('auction.ccel'),
                type: 'warning'
            }).then(() => {
                delectBid({
                    api: 'plugin.auction.AdminAuction.delGoods',
                    acId: row.id
                }).then(res => {
                    console.log(res);
                    if (res.data.code == '200') {
                        this.getbidList()
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


        //选择一页多少条
        handleSizeChange(e) {
            this.loading = true
            console.log(e);
            // this.current_num = e
            this.pageSize = e
            this.getbidList().then(() => {
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
            this.getbidList().then(() => {
                this.current_num = this.tableData.length === this.pageSize ? e * this.pageSize : this.total
                this.loading = false
            })
        },
    }
}