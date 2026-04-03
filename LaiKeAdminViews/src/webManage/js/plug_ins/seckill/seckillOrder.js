import { orderList, delOrder, closeOrder } from '@/api/plug_ins/seckill'
import { kuaidishow } from '@/api/order/orderList'
import { exports } from '@/api/export/index'
import { mixinstest } from '@/mixins/index'
import { getStorage } from '@/utils/storage'
import { getButton } from '@/api/layout/information'
export default {
    name: 'seckillOrder',
    mixins: [mixinstest],

    data() {
        return {
            routerList:JSON.parse(sessionStorage.getItem('tabRouter')),
            radio1: '3',
            inputInfo: {
                name: null,
                mchName: null,
                state: null,
                date: null,
                pluginSelfLifting:null
            },
            ysState:[
                {
                  label:this.$t('seckill.seckillOrder.kd'),
                  value:"0"
                },
                {
                  label:this.$t('seckill.seckillOrder.zt'),
                  value:"1"
                }
              ],
            stateList: [
                {
                    value: '0',
                    label: this.$t('seckill.seckillOrder.dfk')
                },
                {
                    value: '1',
                    label: this.$t('seckill.seckillOrder.dfh')
                },
                {
                    value: '2',
                    label: this.$t('seckill.seckillOrder.dsh')
                },
                {
                    value: '5',
                    label: this.$t('seckill.seckillOrder.ywc')
                },
                {
                    value: '7',
                    label: this.$t('seckill.seckillOrder.ygb')
                }
            ],// 订单状态

            tableData: [],
            loading: true,
            button_list: [],
            idList: [],
            idPrintList: [],
            is_disabled: true,

            // 弹框数据
            dialogVisible2: false,
            ruleForm: {
                courier_company: '',
                courier_no: '',
                logistics: ''
            },
            logisticsList: [],

            // table高度
            tableHeight: null,

            // 导出弹框数据
            dialogVisible: false,
            dialogVisible3:false,
            verificationId:"",
            ruleForm3: {
              verification: ''
            },
      
            rules3: {
              verification: [
                {
                  required: true,
                  message: this.$t('orderLists.qsrzt'),
                  trigger: 'blur'
                }
              ]
            },
        }
    },

    created() {
        if (this.$route.query.no) {
            orderList({
                api: 'plugin.sec.order.index',
                pageSize: this.pageSize,
                pageNo: this.dictionaryNum,
                keyWord: this.inputInfo.name,
                mchName: this.inputInfo.mchName,
                status: this.inputInfo.state,
                id: this.$route.query.no,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
                pluginSelfLifting:this.inputInfo.pluginSelfLifting,

            }).then(res => {
                console.log(res);
                this.total = res.data.data.total
                this.tableData = res.data.data.list
                this.loading = false
                if (this.total < this.current_num) {
                    this.current_num = this.total
                }
            })
        } else {
            this.orderLists()
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
         // 弹框方法
      dialogShow3 (value) {
        console.log(value)
        this.verificationId = value.id
        this.ruleForm3.verification = ""
        this.dialogVisible3 = true
      },
  
      handleClose3 (done) {
        this.dialogVisible3 = false
        this.ruleForm.verification = ''
        this.verificationId = ''
        setTimeout(() => {
          this.$refs['ruleForm3'].clearValidate()
        }, 100)
      },
  
      submitForm3 (formName) {
        this.$refs[formName].validate(async valid => {
          console.log(this.ruleForm3)
          if (valid) {
            try {
                closeOrder({
                api: 'admin.Order.VerificationExtractionCode',
                orderId: this.verificationId,
                extractionCode: this.ruleForm3.verification
              }).then(res => {
                console.log(res)
                if (res.data.code == '200') {
                  this.orderLists()
                  this.$message({
                    type: 'success',
                    message: this.$t('zdata.cg'),
                    offset: 100
                  })
                  this.handleClose3()
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
            console.log('error submit!!')
            return false
          }
        })
      },
        handleSelectAll(){
            if(this.tableData.length==0){
              this.$refs.table.clearSelection()
            }
          },
        rowstyles({ row, rowIndex }) {
            let styleJson = {
                "border": "none"
            };
            return styleJson
        },
        //获取按纽权限
        async getButtons() {
            let route = getStorage('route')
            route.forEach(item => {
                if (item.path == 'seckill') {
                    item.children.forEach(e => {
                        if (e.path == 'seckillOrder') {
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
            this.button_list = buttonList.data.data.map((item) => {
                return item.title;
            });
            console.log(this.button_list, "获取按纽权限")
        },
        async orderLists() {
            const res = await orderList({
                api: 'plugin.sec.order.index',
                pageSize: this.pageSize,
                pageNo: this.dictionaryNum,
                keyWord: this.inputInfo.name,
                mchName: this.inputInfo.mchName,
                status: this.inputInfo.state,
                startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
                pluginSelfLifting:this.inputInfo.pluginSelfLifting,

            })
            console.log(res);
            this.total = res.data.data.total
            this.tableData = res.data.data.list
            this.loading = false
            if (this.total < this.current_num) {
                this.current_num = this.total
            }
        },

        // 获取table高度
        getHeight() {
            this.tableHeight = this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
        },

        // 重置
        reset() {
            this.inputInfo.name = null
            this.inputInfo.mchName = null
            this.inputInfo.state = null
            this.inputInfo.date = null
            this.inputInfo.pluginSelfLifting = null
        },

        // 查询
        demand() {
            this.currpage = 1
            this.current_num = 10
            // this.showPagebox = false
            this.loading = true
            this.dictionaryNum = 1
            this.orderLists().then(() => {
                this.loading = false
                // if(this.tableData.length > 5) {
                //     this.showPagebox = true
                // }
            })
        },

        // 订单售后
        afterSales() {
            this.$router.push({
                path: '/plug_ins/seckill/afterSaleList'
            })
        },

        // 评价管理
        evaluation() {
            this.$router.push({
                path: '/plug_ins/seckill/commentList'
            })
        },

        // 订单结算
        settlement() {
            this.$router.push({
                path: '/plug_ins/seckill/orderSettlementList'
            })
        },

        // 订单打印
        print() {
            if (this.idPrintList.length == 0) {
                this.$message({
                    message: this.$t('seckill.seckillOrder.qxzdda'),
                    type: 'warning',
                    offset: 100
                })
            } else {
                let routeData = this.$router.resolve({
                    path: "/print",
                    query: {
                        orderId: this.idPrintList
                    },
                });

                window.open(routeData.href, '_blank');
            }
        },

        // 批量删除
        delAll() {
            this.$confirm(this.$t('seckill.seckillOrder.qrsc'), this.$t('seckill.ts'), {
                confirmButtonText: this.$t('seckill.okk'),
                cancelButtonText: this.$t('seckill.ccel'),
                type: 'warning'
            }).then(() => {
                delOrder({
                    api: 'plugin.sec.order.delOrder',
                    id: this.idList
                }).then(res => {
                    console.log(res);
                    if (res.data.code == '200') {
                        this.orderLists()
                        this.$message({
                            type: 'success',
                            message: this.$t('zdata.sccg'),
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
            this.$confirm(this.$t('seckill.seckillOrder.qrsc2'), this.$t('seckill.ts'), {
                confirmButtonText: this.$t('seckill.okk'),
                cancelButtonText: this.$t('seckill.ccel'),
                type: 'warning'
            }).then(() => {
                delOrder({
                    api: 'plugin.sec.order.delOrder',
                    id: value.id
                }).then(res => {
                    console.log(res);
                    if (res.data.code == '200') {
                        this.orderLists()
                        this.$message({
                            type: 'success',
                            message: this.$t('zdata.sccg'),
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

        handleSelectionChange(val) {
            if (val.length !== 0) {
                this.is_disabled = false
            } else {
                this.is_disabled = true
            }
            this.idList = val.map(item => {
                return item.id
            })
            this.idPrintList = val.map(item => {
                return item.orderno
            })
            this.idList = this.idList.join(',')
            this.idPrintList = this.idPrintList.join(',')

        },

        // 订单详情
        Details(value) {
            console.log(value);
            this.$router.push({
                path: '/plug_ins/seckill/orderDetails',
                query: {
                    no: value.orderno,
                    selfLiftingName:value.selfLiftingName
                }
            })
        },

        // 编辑订单
        Edit(value) {
            console.log(value);
            this.$router.push({
                path: '/plug_ins/seckill/editorOrder',
                query: {
                    no: value.orderno,
                    selfLiftingName:value.selfLiftingName
                }
            })
        },

        // 商品发货
        Delivery(value) {
            console.log(value);
            this.$router.push({
                path: '/plug_ins/seckill/goodsDelivery',
                query: {
                    no: value.orderno
                }
            })
        },

        // 关闭订单
        closeOrder(value) {
            console.log(value);
            closeOrder({
                api: 'plugin.sec.order.closeOrder',
                id: value.id
            }).then(res => {
                if (res.data.code == '200') {
                    console.log(res);
                    this.$message({
                        type: 'success',
                        message: this.$t('seckill.cg'),
                        offset: 100
                    })
                    this.orderLists()
                }
            })
        },

        // 弹框方法
        dialogShow2(value) {
            this.dialogVisible2 = true
            kuaidishow({
                api: 'plugin.sec.order.kuaidishow',
                orderno: value.orderno
            }).then(res => {
                console.log(res);
                this.logisticsList = res.data.data.list
                this.ruleForm.courier_company = res.data.data.list[0].kuaidi_name
                this.ruleForm.courier_no = res.data.data.list[0].courier_num
                this.ruleForm.logistics = res.data.data.list[0].list
            })
            console.log(value);

        },

        handleClose2(done) {
            this.dialogVisible2 = false
        },

        //选择一页多少条
        handleSizeChange(e) {
            this.loading = true
            this.pageSize = e
            this.orderLists().then(() => {
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
            this.orderLists().then(() => {
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
                api: 'plugin.sec.order.index',
                pageNo: this.dictionaryNum,
                pageSize: this.pageSize,
                exportType: 1,
                keyWord: this.inputInfo.name,
                mchName: this.inputInfo.mchName,
                status: this.inputInfo.state,
                startdate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                enddate: this.inputInfo.date ? this.inputInfo.date[1] : null,
                pluginSelfLifting:this.inputInfo.pluginSelfLifting,

            }, 'pageorder')
        },

        async exportAll() {
            exports({
                api: 'plugin.sec.order.index',
                pageNo: 1,
                pageSize: 99999999,
                exportType: 1,
                // keyWord: this.inputInfo.name,
                // mchName: this.inputInfo.mchName,
                // status: this.inputInfo.state,
                // startdate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                // enddate: this.inputInfo.date ? this.inputInfo.date[1] : null
            }, 'allorder')
        },

        async exportQuery() {
            exports({
                api: 'plugin.sec.order.index',
                pageNo: 1,
                pageSize: this.total,
                exportType: 1,
                keyWord: this.inputInfo.name,
                mchName: this.inputInfo.mchName,
                status: this.inputInfo.state,
                startdate: this.inputInfo.date ? this.inputInfo.date[0] : null,
                enddate: this.inputInfo.date ? this.inputInfo.date[1] : null,
                pluginSelfLifting:this.inputInfo.pluginSelfLifting,

            }, 'queryorder')
        }
    }
}