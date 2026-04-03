import { memberProList, delMemberPro } from '@/api/plug_ins/members'
import { mixinstest } from '@/mixins/index'
import { getStorage } from '@/utils/storage'
import { getButton } from '@/api/layout/information'
import ErrorImg from '@/assets/images/default_picture.png'

export default {
    name: 'memberGoods',
    mixins: [mixinstest],

    data() {
        return {
            routerList: JSON.parse(sessionStorage.getItem('tabRouter')),
            radio1: '3',

            inputInfo: {
                goodsName: null,
            },
            button_list: [],
            tableData: [],
            loading: true,
            is_disabled: true,
            idList: [],
            menuId: '',
            sequenceList: [
                {
                    value: '0',
                    label: this.$t('member.memberGoods.fou')
                },
                {
                    value: '1',
                    label: this.$t('member.memberGoods.shi')
                }
            ],// 是否过期
            // table高度
            tableHeight: null,
            // 导出弹框数据
            dialogVisible: false,
        }
    },

    created() {
        if (this.$route.params.pageSize) {
            this.pagination.page = this.$route.params.dictionaryNum
            this.dictionaryNum = this.$route.params.dictionaryNum
            this.pageSize = this.$route.params.pageSize
            this.inputInfo = this.$route.params.inputInfo
        }
        this.memberProList()
        // this.getButtons()
    },

    mounted() {
        this.$nextTick(function () {
            this.getHeight()
        })
        window.addEventListener('resize', this.getHeight(), false)
    },

    methods: {
        // 图片错误处理
        handleErrorImg(e) {
            console.log('图片报错了', e.target.src);
            e.target.src = ErrorImg
        },
        async memberProList() {
            const res = await memberProList({
                api: 'plugin.member.AdminMember.MemberProList',
                pageNo: this.dictionaryNum,
                pageSize: this.pageSize,
                condition: this.inputInfo.goodsName,
                status: 2
            })
            console.log(res);
            this.current_num = 10
            this.total = res.data.data.total
            this.tableData = res.data.data.list
            this.loading = false
            this.$nextTick(function () {
                this.getHeight()
            })
            if (this.total < this.current_num) {
                this.current_num = this.total
            }
            if (this.total == 0) {
                this.showPagebox = false
            } else {
                this.showPagebox = true
            }

            if (this.tableData.length == 0 && this.dictionaryNum != 1) {
                this.dictionaryNum -= 1
                this.memberProList()
            }
        },
        //获取按纽权限
        async getButtons() {
            let route = getStorage('route')
            route.forEach(item => {
                if (item.path == 'integralMall') {
                    item.children.forEach(e => {
                        if (e.path == 'integralGoodsList') {
                            return this.menuId = e.id
                        }
                    })
                }
            });
            let buttonList = await getButton({
                api: 'saas.role.getButton',
                menuId: this.menuId,
            })
            this.button_list = buttonList.data.data
        },
        // 获取table高度
        getHeight() {
            this.tableHeight = this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
            console.log(this.$refs.tableFather.clientHeight);
        },

        // 重置
        reset() {
            this.inputInfo.goodsName = null
        },

        // 查询
        demand() {
            this.currpage = 1
            this.current_num = 10
            this.showPagebox = false
            this.loading = true
            this.dictionaryNum = 1
            this.memberProList().then(() => {
                this.loading = false
                if (this.tableData.length > 5) {
                    this.showPagebox = true
                }
            })
        },

        buyRecord(value) {
            this.$router.push({
                path: '/plug_ins/member/buyRecord',
                query: {
                    id: value.user_id
                }
            })
        },


        addGoods() {
            this.$router.push({
                path: '/plug_ins/member/addGoods',
            })
        },


        Edit(value) {
            this.$router.push({
                path: '/goods/goodslist/releasephysical',
                query: {
                    id: value.id,
                    name: 'editor',
                    status: value.status_name,
                    classId: value.product_class,
                    dictionaryNum: this.dictionaryNum,
                    pageSize: this.pageSize,
                    inputInfo: this.inputInfo,
                    editOrType: 'huiYuan'
                },
            })
        },

        // 选框改变
        handleSelectionChange(val) {
            if (val.length == 0) {
                this.is_disabled = true
            } else {
                this.is_disabled = false
            }
            console.log(val);
            this.idList = val.map(item => {
                return item.id
            })
            this.idList = this.idList.join(',')
        },

        delAll() {
            this.$confirm(this.$t('member.memberGoods.qrsc'), this.$t('member.ts'), {
                confirmButtonText: this.$t('member.okk'),
                cancelButtonText: this.$t('member.ccel'),
                type: 'warning'
            }).then(() => {
                delMemberPro({
                    api: 'plugin.member.AdminMember.DelMemberPro',
                    proIds: this.idList
                }).then(res => {
                    console.log(res);
                    if (res.data.code == '200') {
                        this.memberProList()
                        this.$message({
                            type: 'success',
                            message: this.$t('member.cg'),
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
            });
        },

        Delete(value) {
            this.$confirm('确认要删除该会员商品吗？', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                delMemberPro({
                    api: 'plugin.member.AdminMember.DelMemberPro',
                    proIds: value.id
                }).then(res => {
                    console.log(res);
                    if (res.data.code == '200') {
                        this.memberProList()
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
        handleSizeChange(e) {
            this.loading = true
            console.log(e);
            this.pageSize = e
            this.memberProList().then(() => {
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
            this.memberProList().then(() => {
                this.current_num = this.tableData.length === this.pageSize ? e * this.pageSize : this.total
                this.loading = false
            })
        },

    }
}