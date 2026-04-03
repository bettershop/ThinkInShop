import { mixinstest } from '@/mixins/index'
import { getStorage } from '@/utils/storage'
import { getButton } from '@/api/layout/information'
import { getDistributionTopInfo, delBatchRank, getDistributionUserInfo, addDistributionRankInfo } from '@/api/plug_ins/distribution'

export default {
    name: 'commissionList',
    mixins: [mixinstest],

    data() {
        return {
            radio1:'4',
            radio: 1,
            button_list:[],
            tableData: [],
            loading: true,

            idList: [],
            is_disabled: true,

            // 新增用户排行
            dialogVisible3: false,
            ruleForm2: {
                commission: ''
            },
            rules2: {
                search: [
                    { required: true, message: '请填写购物赠送积分比例', trigger: 'blur' }
                ],
            },
            tableRadio: {},
            search: '',
            userdata: [],
            userchangedata: [],
            total2:20,
            pagesizes2: [10, 25, 50, 100],
            pagination2: {
                page: 1,
                pagesize: 10,
            },
            currpage2: 1,
            current_num2: 10,
            dictionaryNum2: 1,
            pageSize2: 10,

            show: false,
            id: null,
            // table高度
            tableHeight: null,
            showPagebox:true
        }
    },

    created() {
        this.getDistributionTopInfos()
    },

    mounted() {
        this.$nextTick(function() {
            this.getHeight()
        })
        window.addEventListener('resize',this.getHeight(),false)
    },

    watch: {
        radio() {
            this.currpage = 1
            this.current_num = 10
            this.showPagebox = false
            this.loading = true
            this.dictionaryNum = 1
            this.getDistributionTopInfos().then(() => {
              this.loading = false
              if(this.tableData.length > 0) {
                this.showPagebox = true
              }
            })
        }
    },

    methods: {
        // 获取table高度
        getHeight(){
			this.tableHeight = this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
		},

        async getDistributionTopInfos() {
            const res = await getDistributionTopInfo({
                api: 'plugin.distribution.AdminDistribution.getDistributionTopInfo',
                pageSize: this.pageSize,
                pageNo: this.dictionaryNum,
                type: this.radio
            })
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

        async getDistributionUserInfo() {
            const res = await getDistributionUserInfo({
                api: 'plugin.distribution.AdminDistribution.getDistributionUserInfo',
                pageSize: this.pageSize2,
                pageNo: this.dictionaryNum2,
                name: this.search
            })
            this.total2 = res.data.data.total
            this.userdata = res.data.data.list
            if(this.total2 < this.current_num2) {
                this.current_num2 = this.total2
            }
        },

        // 批量删除
        delAll() {
            this.$confirm('是否删除选中佣金排行？', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                this.idList = this.idList.map(item => {
                    return item.id
                })
                this.idList = this.idList.join(',')
                delBatchRank({
                    api: 'plugin.distribution.AdminDistribution.delBatchRank',
                    ids: this.idList
                }).then(res => {
                    if(res.data.code == '200') {
                        console.log(res);
                        this.getDistributionTopInfos()
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
                    message: '已取消删除'
                })
            });
        },

        Delete(value) {
            this.$confirm('是否删除选中佣金排行？', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                delBatchRank({
                    api: 'plugin.distribution.AdminDistribution.delBatchRank',
                    ids: value.id
                }).then(res => {
                    if(res.data.code == '200') {
                        console.log(res);
                        this.getDistributionTopInfos()
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
                    message: '已取消删除'
                })
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
        },

        // 开通分销商
        openDistributors(tag,value) {
            this.dialogVisible3 = true
            this.currpage2 = 1
            // this.pagination2.pageSize = 10
            this.userdata = []
            if(tag == 1) {
                this.show = false
                this.getDistributionUserInfo()
            } else {
                this.show = true
                this.id = value.id
                this.userdata.push(value)
                this.tableRadio = value
                this.userchangedata = this.userdata
                this.ruleForm2.commission = value.commission
                this.$refs.multipleTable.toggleRowSelection(this.userdata[0]);
                console.log(value);
            }
            
        },

        Reset() {
            this.search = '' //会员ID,名称，手机号
        },

        query2() {
            this.currpage2 = 1
            this.current_num2 = 10
            this.dictionaryNum2 = 1
            this.pagination2.pagesize = 10
            this.getDistributionUserInfo()
        },

        confirm() {
            if(this.userchangedata.length == 0) {
                this.$message({
                    type: 'error',
                    message: '请选择用户',
                    offset: 100
                })
            }else {
                if(!this.show) {
                    addDistributionRankInfo({
                        api: 'plugin.distribution.AdminDistribution.addDistributionRankInfo',
                        userId: this.userchangedata.user_id,
                        type: this.radio
                    }).then(res => {
                        console.log(res);
                        if(res.data.code == 200) {
                            this.$message({
                                type: 'success',
                                message: '成功!',
                                offset: 100
                            })
                            this.getDistributionTopInfos()
                            this.tableRadio = {}
                            this.userdata = []
                            this.userchangedata = []
                            this.dialogVisible3 = false
                        }
                    })
                } else {
                    addDistributionRankInfo({
                        api: 'plugin.distribution.AdminDistribution.addDistributionRankInfo',
                        id: this.id,
                        userId: this.userchangedata.user_id,
                        commission: this.ruleForm2.commission
                    }).then(res => {
                        console.log(res);
                        if(res.data.code == 200) {
                            this.$message({
                                type: 'success',
                                message: '成功!',
                                offset: 100
                            })
                            this.getDistributionTopInfos()
                            this.dialogVisible3 = false
                        }
                    })
                }
                
            }
        },

        //选择一页多少条
        handleSizeChange2(e){
            this.pageSize2 = e
            this.getDistributionUserInfo().then(() => {
                this.currpage2 = ((this.dictionaryNum2 - 1) * this.pageSize2) + 1
                this.current_num2 = this.userdata.length === this.pageSize2 ? this.dictionaryNum2 * this.pageSize2 : this.total2
            })
        },

        //点击上一页，下一页
        handleCurrentChange2(e){
            this.dictionaryNum2 = e
            this.currpage2 = ((e - 1) * this.pageSize2) + 1
            this.getDistributionUserInfo().then(() => {
                this.current_num2 = this.userdata.length === this.pageSize2 ? e * this.pageSize2 : this.total2
            })
        },

        handleSelectionChange2(e) {
            this.tableRadio = e
            this.userchangedata = e
        },

        //选择一页多少条
        handleSizeChange(e){
            this.loading = true
            console.log(e);
            this.pageSize = e
            this.getDistributionTopInfos().then(() => {
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
            this.getDistributionTopInfos().then(() => {
                this.current_num = this.tableData.length === this.pageSize ? e * this.pageSize : this.total
                this.loading = false
            })
        },

        oninput(num, limit) {
            var str = num
            var len1 = str.substr(0, 1)
            var len2 = str.substr(1, 1)
            //如果第一位是0，第二位不是点，就用数字把点替换掉
            if (str.length > 1 && len1 == 0 && len2 != ".") {
              str = str.substr(1, 1)
            }
            //第一位不能是.
            if (len1 == ".") {
              str = ""
            }
            //限制只能输入一个小数点
            if (str.indexOf(".") != -1) {
              var str_ = str.substr(str.indexOf(".") + 1)
              if (str_.indexOf(".") != -1) {
                str = str.substr(0, str.indexOf(".") + str_.indexOf(".") + 1)
              }
            }
            //正则替换
            str = str.replace(/[^\d^\.]+/g, '') // 保留数字和小数点
            if (limit / 1 === 1) {
              str = str.replace(/^\D*([0-9]\d*\.?\d{0,1})?.*$/,'$1') // 小数点后只能输 1 位
            } else {
              str = str.replace(/^\D*([0-9]\d*\.?\d{0,2})?.*$/,'$1') // 小数点后只能输 2 位
            }
            return str
        },

    }
}