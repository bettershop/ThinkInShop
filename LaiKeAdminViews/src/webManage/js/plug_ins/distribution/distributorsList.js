import { mixinstest } from '@/mixins/index'
import { getStorage } from '@/utils/storage'
import { getButton } from '@/api/layout/information'
import { exports } from '@/api/export/index'
import { getDistributionInfo, editeReferences, editeGrade, getDistributionGradeList, getGrabbleListInfo, delDistribution, builderLevel } from '@/api/plug_ins/distribution'
export default {
    name: 'distributorsList',
    mixins: [mixinstest],

    data() {
        return {
            routerList: JSON.parse(sessionStorage.getItem('tabRouter')),
            radio1: '1',
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
                    value: 'id',
                    label: this.$t('distribution.distributorsList.yhid')
                },
                {
                    value: 'accumulative',
                    label: this.$t('distribution.distributorsList.ljyj')
                },
                {
                    value: 'add_date',
                    label: this.$t('distribution.distributorsList.jssj')
                }
            ],// 排序字段

            sequenceList: [
                {
                    value: '2',
                    label: this.$t('distribution.distributorsList.sx')
                },
                {
                    value: '1',
                    label: this.$t('distribution.distributorsList.jx')
                }
            ],// 排序

            tableData: [],
            loading: true,
            button_list: [],
            toggle: 1,

            // 开通分销商弹框数据
            dialogVisible3: false,
            ruleForm2: {
                level: ''
            },
            rules2: {
                search: [
                    { required: true, message: this.$t('distribution.distributorsList.qtxgwzs'), trigger: 'blur' }
                ],
                level: [
                    { required: true, message: this.$t('distribution.distributorsList.qtxgwzs'), trigger: 'blur' }
                ],
            },
            tableRadio: '',
            search: '',
            userdata: [],
            userchangedata: [],
            total2: 20,
            pagesizes2: [10, 25, 50, 100],
            pagination2: {
                page: 1,
                pagesize: 10,
            },
            currpage2: 1,
            current_num2: 10,
            dictionaryNum2: 1,
            pageSize2: 10,

            // 修改推荐人、等级弹框数据
            dialogVisible2: false,
            ruleForm: {
                user_id: '',
                level: '',
            },

            id: null,
            user_id: null,
            zhanghao: null,

            tag: '',

            // table高度
            tableHeight: null,

            // 导出弹框数据
            dialogVisible: false,
            showPagebox: true
        }
    },

    created() {
        this.getDistributionInfos()
        this.getDistributionGradeLists()
        getButton({ api: "saas.shop.checkShopHavaSelfOwnedShop" }).then((res) => {
            if (res.data.code == 51008) {
                this.$router.push("/mall/fastBoot/index");
            }
        });
        if (getStorage('laike_admin_userInfo').mchId == 0) {
            this.$message({
                type: 'error',
                message: this.$t('plugInsSet.plugInsList.qtjdp'),
                offset: 100
            })
            this.$router.push('/mall/fastBoot/index')
        }
        this.getButtons()
    },

    mounted() {
        this.$nextTick(function () {
            this.getHeight()
        })
        window.addEventListener('resize', this.getHeight(), false)
    },

    methods: {
        //获取按纽权限
        async getButtons() {
            let route = getStorage('route')
            route.forEach(item => {
                if (item.path == 'distribution') {
                    item.children.forEach(e => {
                        if (e.path == 'distributorsList') {
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
            this.button_list = buttonList.data.data.map(item => {
                return item.title
            })
        },


        async getDistributionInfos() {
            const res = await getDistributionInfo({
                api: 'plugin.distribution.AdminDistribution.getDistributionInfo',
                pageSize: this.pageSize,
                pageNo: this.dictionaryNum,
                userName: this.inputInfo.name,
                phone: this.inputInfo.phone,
                grade: this.inputInfo.level,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
                sortColumn: this.inputInfo.sort,
                sortType: this.inputInfo.sequence
            })

            this.total = res.data.data.total
            this.tableData = res.data.data.list
            this.loading = false
            if (this.total < this.current_num) {
                this.current_num = this.total
            }
            if (this.tableData.length > 0) {
                this.showPagebox = true
            }
        },

        async getGrabbleListInfo() {
            const res = await getGrabbleListInfo({
                api: 'plugin.distribution.AdminDistribution.getGrabbleListInfo',
                pageSize: this.pageSize2,
                pageNo: this.dictionaryNum2,
                userName: this.search
            })
            this.total2 = res.data.data.total
            this.userdata = res.data.data.list
            if (this.total2 < this.current_num2) {
                this.current_num2 = this.total2
            }

            console.log(res);
        },

        async getDistributionGradeLists() {
            const res = await getDistributionGradeList({
                api: 'plugin.distribution.AdminDistribution.getDistributionGradeList'
            })
            this.levelList = res.data.data.gradeInfoList
        },

        // 获取table高度
        getHeight() {
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
            this.loading = true
            this.showPagebox = false
            this.dictionaryNum = 1
            this.getDistributionInfos().then(() => {
                this.loading = false
                if (this.tableData.length > 0) {
                    this.showPagebox = true
                }
            })
        },

        // 开通分销商
        openDistributors() {
            // this.dialogVisible3 = true
            // // this.current_num = this.userdata.length
            // this.currpage2 = 1
            // this.pagination2.pageSize = 10

            // this.getGrabbleListInfo()
            this.$router.push({
                path: '/plug_ins/distribution/addDistributors',
                query: {
                    // id: value.user_id
                }
            })
        },

        Reset() {
            this.search = '' //会员ID,名称，手机号
        },

        query2() {
            this.currpage2 = 1
            this.current_num2 = 10
            this.dictionaryNum2 = 1
            this.pagination2.pagesize = 10
            this.getGrabbleListInfo()
        },

        confirm() {
            if (this.userchangedata.length == 0) {
                this.$message({
                    type: 'error',
                    message: this.$t('distribution.distributorsList.qxzyh'),
                    offset: 100
                })
            } else if (!this.ruleForm2.level) {
                this.$message({
                    type: 'error',
                    message: this.$t('distribution.distributorsList.qxzfx'),
                    offset: 100
                })
            } else {
                this.$confirm(this.$t('distribution.distributorsList.sfwgy'), this.$t('distribution.ts'), {
                    confirmButtonText: this.$t('distribution.okk'),
                    cancelButtonText: this.$t('distribution.ccel'),
                    type: 'warning'
                }).then(() => {
                    builderLevel({
                        api: 'plugin.distribution.AdminDistribution.builderLevel',
                        userid: this.userchangedata.user_id,
                        level: this.ruleForm2.level
                    }).then(res => {
                        console.log(res);
                        if (res.data.code == 200) {
                            this.$message({
                                type: 'success',
                                message: this.$t('distribution.xgcg'),
                                offset: 100
                            })
                            this.getDistributionInfos()
                            this.dialogVisible3 = false
                        }
                    })
                }).catch(() => {
                    // this.$message({
                    //     type: 'info',
                    //     message: '已取消'
                    // })
                })
            }
        },

        //选择一页多少条
        handleSizeChange2(e) {
            this.pageSize2 = e
            this.getGrabbleListInfo().then(() => {
                this.currpage2 = ((this.dictionaryNum2 - 1) * this.pageSize2) + 1
                this.current_num2 = this.userdata.length === this.pageSize2 ? this.dictionaryNum2 * this.pageSize2 : this.total2
            })
        },

        //点击上一页，下一页
        handleCurrentChange2(e) {
            this.dictionaryNum2 = e
            this.currpage2 = ((e - 1) * this.pageSize2) + 1
            this.getGrabbleListInfo().then(() => {
                this.current_num2 = this.userdata.length === this.pageSize2 ? e * this.pageSize2 : this.total2
            })
        },

        handleSelectionChange(e) {
            this.tableRadio = e
            this.userchangedata = e
        },

        Edit(value) {
            this.$router.push({
                path: '/plug_ins/distribution/editorDistributors',
                query: {
                    id: value.id
                }
            })
        },

        Delete(value) {
            this.$confirm(this.$t('distribution.distributorsList.scfx'), this.$t('distribution.ts'), {
                confirmButtonText: this.$t('distribution.okk'),
                cancelButtonText: this.$t('distribution.ccel'),
                type: 'warning'
            }).then(() => {
                delDistribution({
                    api: 'plugin.distribution.AdminDistribution.delDistribution',
                    id: value.id
                }).then(res => {
                    console.log(res);
                    if (res.data.code == '200') {
                        this.getDistributionInfos()
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

        View(value) {
            this.$router.push({
                path: '/plug_ins/distribution/viewSubordinate',
                query: {
                    id: value.user_id
                }
            })
        },

        tags(value) {
            if (this.tag == value.user_id) {
                this.tag = ''
            } else {
                this.tag = value.user_id
            }
        },

        //选择一页多少条
        handleSizeChange(e) {
            this.loading = true
            this.pageSize = e
            this.getDistributionInfos().then(() => {
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
            this.getDistributionInfos().then(() => {
                this.current_num = this.tableData.length === this.pageSize ? e * this.pageSize : this.total
                this.loading = false
            })
        },

        dialogShow2(value, tag) {
            this.dialogVisible2 = true
            this.toggle = tag
            this.id = value.id
            // this.user_id = value.pid
            this.zhanghao = value.zhanghao
            if (tag == 1) {
                this.ruleForm.user_id = this.user_id
            } else {
                this.ruleForm.level = `${value.level}`
            }
        },

        handleClose2() {
            this.dialogVisible2 = false
        },

        submitForm2() {
            if (this.toggle == 1) {
                if (!this.ruleForm.user_id) {
                    this.errorMsg(this.$t('distribution.distributorsList.qsrtjrID'));
                    return
                }
                editeReferences({
                    api: 'plugin.distribution.AdminDistribution.editeReferences',
                    id: this.id,
                    fatherUid: this.ruleForm.user_id
                }).then(res => {
                    if (res.data.code == '200') {
                        this.$message({
                            message: this.$t('distribution.xgcg'),
                            type: 'success',
                            offset: 100
                        })
                        this.dialogVisible2 = false
                        this.getDistributionInfos()
                    }
                })
            } else {
                editeGrade({
                    api: 'plugin.distribution.AdminDistribution.editeGrade',
                    id: this.id,
                    level: this.ruleForm.level
                }).then(res => {
                    if (res.data.code == '200') {
                        this.$message({
                            message: this.$t('distribution.xgcg'),
                            type: 'success',
                            offset: 100
                        })
                        this.dialogVisible2 = false
                        this.getDistributionInfos()
                    }
                })
            }
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
                pageNo: this.dictionaryNum,
                pageSize: this.pageSize,
                exportType: 1,
                userName: this.inputInfo.name,
                phone: this.inputInfo.phone,
                grade: this.inputInfo.level,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
                sortColumn: this.inputInfo.sort,
                sortType: this.inputInfo.sequence
            }, 'pageorder')
        },

        async exportAll() {
            exports({
                api: 'plugin.distribution.AdminDistribution.getDistributionInfo',
                pageNo: 1,
                pageSize: this.total,
                exportType: 1,
                userName: this.inputInfo.name,
                phone: this.inputInfo.phone,
                grade: this.inputInfo.level,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
                sortColumn: this.inputInfo.sort,
                sortType: this.inputInfo.sequence
            }, 'allorder')
        },

        async exportQuery() {
            exports({
                api: 'plugin.distribution.AdminDistribution.getDistributionInfo',
                pageNo: 1,
                pageSize: this.total,
                exportType: 1,
                userName: this.inputInfo.name,
                phone: this.inputInfo.phone,
                grade: this.inputInfo.level,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
                sortColumn: this.inputInfo.sort,
                sortType: this.inputInfo.sequence
            }, 'queryorder')
        }
    }
}
