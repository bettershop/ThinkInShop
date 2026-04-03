import { mixinstest } from '@/mixins/index'
import { getStorage } from '@/utils/storage'
import { getButton } from '@/api/layout/information'
import { exports } from '@/api/export/index'
import { getWithdrawalRecordInfo, delWithdrawalRecord, withdrawalExamine } from '@/api/plug_ins/distribution'
export default {
    name: 'withdrawalRecord',
    mixins: [mixinstest],

    data() {
        return {
            routerList:JSON.parse(sessionStorage.getItem('tabRouter')),
            radio1:'7',
            inputInfo: {
                name: null,
                bankName: null,
                examineType: null,
                date: null
            },
            typeList: [
                {
                    value: '0',
                    label: this.$t('distribution.withdrawalRecord.shz')
                },
                {
                    value: '1',
                    label: this.$t('distribution.withdrawalRecord.shtg')
                },
                {
                    value: '2',
                    label: this.$t('distribution.withdrawalRecord.jj')
                }
            ],// 分销等级

            tableData: [],

            // table高度
            tableHeight: null,
            button_list:[],
            tableData: [],
            loading: true,

            // 弹框数据
            dialogVisible2: false,
            ruleForm: {
                reason: '',
            },

            rules: {
                reason: [
                { required: true, message: this.$t('distribution.withdrawalRecord.qsrjjly'), trigger: 'blur' }
                ],
            },

            id: null,

            // 导出弹框数据
            dialogVisible: false,
            showPagebox:true
        }
    },

    created() {
        this.getWithdrawalRecordInfos()
        this.getButtons()
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

        //获取按纽权限
        async getButtons() {
            let route=getStorage('route')
            route.forEach(item => {
                if(item.path=='seckill'){
                    item.children.forEach(e => {
                        if(e.path=='seckillOrder'){
                        return this.menuId=e.id                       
                        }
                    })
                }
            });     

            let buttonList = await getButton ({
            api:'saas.role.getButton',       
            menuId:this.menuId, 
            })
            this.button_list=buttonList.data.data
            console.log(this.button_list,"获取按纽权限")
        },

        async getWithdrawalRecordInfos() {
            const res = await getWithdrawalRecordInfo({
                api: 'plugin.distribution.AdminDistribution.getWithdrawalRecordInfo',
                pageSize: this.pageSize,
                pageNo: this.dictionaryNum,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
                examineType: this.inputInfo.examineType,
                name: this.inputInfo.name,
                bankName: this.inputInfo.bankName
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

        reset() {
            this.inputInfo.name = null
            this.inputInfo.bankName = null
            this.inputInfo.examineType = null
            this.inputInfo.date = null
        },
      
        demand() {
            this.currpage = 1
            this.current_num = 10
            this.showPagebox = false
            this.loading = true
            this.dictionaryNum = 1
            this.getWithdrawalRecordInfos().then(() => {
              this.loading = false
              if(this.tableData.length > 0) {
                this.showPagebox = true
              }
            })
        },

        Delete(value) {
            this.$confirm('是否删除该提现记录？', this.$t('distribution.ts'), {
                confirmButtonText: this.$t('distribution.okk'),
                cancelButtonText: this.$t('distribution.ccel'),
                type: 'warning'
            }).then(() => {
                delWithdrawalRecord({
                    api: 'plugin.distribution.AdminDistribution.delWithdrawalRecord',
                    id: value.id
                }).then(res => {
                    if(res.data.code == '200') {
                        console.log(res);
                        this.getWithdrawalRecordInfos()
                        this.$message({
                            type: 'success',
                            message: this.$t('distribution.cg'),
                            offset: 100
                        })
                    }
                })
            }).catch(() => {
                // this.$message({
                //     type: 'info',
                //     message: '已取消删除'
                // })
            })
        },

        audit(value) {
            this.$confirm(this.$t('distribution.withdrawalRecord.qrtg'), this.$t('distribution.ts'), {
                confirmButtonText: this.$t('distribution.okk'),
                cancelButtonText: this.$t('distribution.ccel'),
                type: 'warning'
            }).then(() => {
                withdrawalExamine({
                    api: 'plugin.distribution.AdminDistribution.withdrawalExamine',
                    examineType: 1,
                    wid: value.id,
                }).then(res => {
                    if(res.data.code == '200') {
                        console.log(res);
                        this.getWithdrawalRecordInfos()
                        this.$message({
                            type: 'success',
                            message: this.$t('distribution.cg'),
                            offset: 100
                        })
                    }
                })
            }).catch(() => {
                // this.$message({
                //     type: 'info',
                //     message: '已取消删除'
                // })
            })
        },

        // 弹框方法
        dialogShow2(value) {
            console.log(value);
            this.dialogVisible2 = true
            this.id = value.id
            this.ruleForm.reason = ''
        },

        handleClose2(done) {
            this.dialogVisible2 = false
            this.id = null
            this.$refs['ruleForm'].clearValidate()
          },
      
        submitForm2(formName) {
            this.$refs[formName].validate(async (valid) => {
              console.log(this.ruleForm);
              if (valid) {
                try {
                withdrawalExamine({
                    api: 'plugin.distribution.AdminDistribution.withdrawalExamine',
                    examineType: 0,
                    wid: this.id,
                    refuse: this.ruleForm.reason
                }).then(res => {
                    console.log(res);
                    if(res.data.code == '200') {
                    this.getWithdrawalRecordInfos()
                    this.$message({
                        type: 'success',
                        message: this.$t('distribution.cg'),
                        offset: 100
                    })
                    this.handleClose2()
                    }
                })
                } catch (error) {
                  this.$message({
                    message: error.message,
                    type: 'error',
                    showClose: true
                  })
                }
              } else {
                console.log('error submit!!');
                return false;
              }
            });
        },

        refused(value) {
            this.$confirm(this.$t('distribution.withdrawalRecord.qrjj'), this.$t('distribution.ts'), {
                confirmButtonText: this.$t('distribution.okk'),
                cancelButtonText: this.$t('distribution.ccel'),
                type: 'warning'
            }).then(() => {
                withdrawalExamine({
                    api: 'plugin.distribution.AdminDistribution.withdrawalExamine',
                    examineType: 0,
                    wid: value.id,
                    refuse: 'hhh'
                }).then(res => {
                    if(res.data.code == '200') {
                        console.log(res);
                        this.getWithdrawalRecordInfos()
                        this.$message({
                            type: 'success',
                            message: this.$t('distribution.cg'),
                            offset: 100
                        })
                    }
                })
            }).catch(() => {
                // this.$message({
                //     type: 'info',
                //     message: '已取消删除'
                // })
            })
        },

        //选择一页多少条
        handleSizeChange(e){
            this.loading = true
            console.log(e);
            this.pageSize = e
            this.getWithdrawalRecordInfos().then(() => {
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
            this.getWithdrawalRecordInfos().then(() => {
                this.current_num = this.tableData.length === this.pageSize ? e * this.pageSize : this.total
                this.loading = false
            })
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
                api: 'plugin.distribution.AdminDistribution.getWithdrawalRecordInfo',
                pageSize: this.pageSize,
                pageNo: this.dictionaryNum,
                exportType: 1,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
                examineType: this.inputInfo.examineType,
                name: this.inputInfo.name,
                bankName: this.inputInfo.id
            },'pageorder')
        },
    
        async exportAll() {
            exports({
                api: 'plugin.distribution.AdminDistribution.getWithdrawalRecordInfo',
                pageNo: 1,
                pageSize: this.total,
                exportType: 1,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
                examineType: this.inputInfo.examineType,
                name: this.inputInfo.name,
                bankName: this.inputInfo.id
            },'allorder')
        },
    
        async exportQuery() {
            exports({
                api: 'plugin.distribution.AdminDistribution.getWithdrawalRecordInfo',
                pageSize: this.total,
                pageNo: this.dictionaryNum,
                exportType: 1,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
                examineType: this.inputInfo.examineType,
                name: this.inputInfo.name,
                bankName: this.inputInfo.id
            },'queryorder')
        }

    }
}