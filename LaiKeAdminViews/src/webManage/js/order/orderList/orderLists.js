import { orderList, delOrder, kuaidishow, getStoreList, verificationExtractionCode } from '@/api/order/orderList'
import { exports } from '@/api/export/index'
import { mixinstest } from '@/mixins/index'
import { getStorage } from '@/utils/storage'
import {mapMutations,mapState} from "vuex";
import ErrorImg from '@/assets/images/default_picture.png'
export default {
  name: 'orderLists',
  mixins: [mixinstest],
  data() {
    return {
      laikeCurrencySymbol: '￥',
      routerList: JSON.parse(sessionStorage.getItem('tabRouter')),
      radio1: '',
      stateList: [
        {
          value: '',
          label: this.$t('orderLists.qb')
        },
        {
          value: '0',
          label: this.$t('orderLists.dfk')
        },
        {
          value: '1',
          label: this.$t('orderLists.dfh')
        },
        {
          value: '2',
          label: this.$t('orderLists.dsh')
        },
        {
          value: '5',
          label: this.$t('orderLists.ywc')
        },
        {
          value: '7',
          label: this.$t('orderLists.ygb')
        }
      ], // 订单状态
      stateHxList: [
        {
          value: '',
          label: this.$t('orderLists.qb')
        },
        {
          value: '0',
          label: this.$t('orderLists.dfk')
        },
        {
          value: '8',
          label: this.$t('orderLists.dhx')
        },
        {
          value: '5',
          label: this.$t('orderLists.ywc')
        },
        {
          value: '7',
          label: this.$t('orderLists.ygb')
        }
      ], // 订单状态
      typeList: [
        {
          value: '1',
          label: this.$t('orderLists.yhxd')
        },
        // {
        //     value: '2',
        //     label: this.$t('orderLists.dpxd')
        // },
        {
          value: '3',
          label: this.$t('orderLists.ptxd')
        }
      ], // 订单类型
      inputInfo: {
        orderInfo: null,
        store: null,
        state: null,
        type: null,
        date: null
      },
      is_disabled: true, // 批量删除按钮
      goodsListType: 0, // 商品列表类型
      orderList: [], // 订单号集合

      tableData: [],
      loading: true,
      // 弹框数据
      dialogVisible2: false,
      ruleForm: {
        courier_company: '',
        courier_no: '',
        logistics: ''
      },
      // logisticsTracking: [],

      logisticsList: [],

      // 导出弹框数据
      dialogVisible: false,
      // table高度
      tableHeight: null,

      isTag: true,
      // 核销弹框数据
      dialogVisible3: false,
      ruleForm3: {
        verification: '',
        store: ''
      },
      storeList: [],
      rules3: {
        verification: [
          {
            required: true,
            message: this.$t('orderLists.qsrzt'),
            trigger: 'blur'
          }
        ],
        store: [
          {
            required: true,
            message: this.$t('orderLists.qxzhxmd'),
            trigger: 'change'
          }
        ]
      },
      //商家配送 填写配送员信息 弹框组件
      dialogVisible5: false,
      ruleForm5: {
        sNo: '',
        phone: '',
        courier_name: ''
      },
      rules5: {
        phone: [
          {
            required: true,
            message: this.$t('手机号不能为空'),
            trigger: 'blur'
          }
        ],
        courier_name: [
          {
            required: true,
            message: this.$t('名称不能为空'),
            trigger: 'blur'
          }
        ]
      },

      verificationId: '',

      dialogVisible4: false,
      rules4: {},
      ruleForm4: {
        name: '',
        mobile: '',
        num: '',
        sNo: '',
        p_price: '',
        store_name: '',
        time: '',
      },
      orInfoList: [],
      tableRadio: "",
      ordernoId: '',
      is_appointment: '',
      detailId: '',//订单详情id
    }
  },


  created() {
    this.laikeCurrencySymbol = this.LaiKeCommon.getDefaultCurrencySymbol();
    if (this.$route.params.pageSize) {
      this.pagination.page = this.$route.params.dictionaryNum
      this.dictionaryNum = this.$route.params.dictionaryNum
      this.pageSize = this.$route.params.pageSize
      this.radio1 = this.$route.params.radio1
    } else {
      // this.$router.currentRoute.matched[2].meta.title = ''
    }
    if (
      this.$route.query.no ||
      this.$route.query.value ||
      this.$route.query.orderNo ||
      this.$route.query.user_id
    ) {
      if (this.$route.query.no) {
        if (this.$route.query.self_lifting == 1) {
          this.goodsListType = 2
          this.radio1 = '2'
          this.isTag = false
        } else {
          this.goodsListType = 0
          this.radio1 = ''
          this.$router.currentRoute.matched[2].meta.title = ''
        }

        orderList({
          api: 'admin.order.index',
          pageNo: this.dictionaryNum,
          pageSize: this.pageSize,
          id: this.$route.query.no,
          selfLifting: this.goodsListType
        }).then(res => {
          console.log(res)
          this.total = res.data.data.total
          this.tableData = res.data.data.list
          this.loading = false
          if (this.total < this.current_num) {
            this.current_num = this.total
          }
          this.isTag = true
        })
      } else if (this.$route.query.value) {
        this.inputInfo.state = this.$route.query.value
        orderList({
          api: 'admin.order.index',
          pageNo: this.dictionaryNum,
          pageSize: this.pageSize,
          status: this.$route.query.value
        }).then(res => {
          console.log(res)
          this.total = res.data.data.total
          this.tableData = res.data.data.list
          this.loading = false
          if (this.total < this.current_num) {
            this.current_num = this.total
          }
        })
      } else if (this.$route.query.orderNo) {
        // this.inputInfo.orderInfo = this.$route.query.orderNo
        orderList({
          api: 'admin.order.index',
          pageNo: this.dictionaryNum,
          pageSize: this.pageSize,
          keyWord: this.$route.query.orderNo
        }).then(res => {
          console.log(res)
          this.total = res.data.data.total
          this.tableData = res.data.data.list
          this.loading = false
          if (this.total < this.current_num) {
            this.current_num = this.total
          }
        })
      } else if (this.$route.query.user_id) {
        this.inputInfo.orderInfo = this.$route.query.user_id
        orderList({
          api: 'admin.order.index',
          pageNo: this.dictionaryNum,
          pageSize: this.pageSize,
          keyWord: this.$route.query.user_id
        }).then(res => {
          console.log(res)
          this.total = res.data.data.total
          this.tableData = res.data.data.list
          this.loading = false
          if (this.total < this.current_num) {
            this.current_num = this.total
          }
        })
      }
    } else {
      // 如果不是 从消息跳转进来的则调用列表接口
      // if (!this.$route.query.selfLifting) {
      // this.orderLists()
      // }
    }
    this.$store.dispatch('orderNum/getOrderCount')
  },

  computed:{
    ...mapState('order', ['formData']),
  },
  mounted() {
    this.inputInfo.state = this.formData || ''
    // 如果是从消息跳转进来则跳转到对应的tab页面
    // 1 实物订单  3 虚拟订单 2 自提订单 5 配送订单
    // 反之 取第一个实物订单
    if (this.$route.query.selfLifting) {
      this.radio1 = this.$route.query.selfLifting
    } else {
      this.radio1 = this.$refs.tab_bun.$children[0].label
    }
    this.$nextTick(function () {
      this.getHeight()
    })
    window.addEventListener('resize', this.getHeight(), false)
  },

  watch: {
    radio1() {
      if (this.radio1 == '3') {
        this.SET_ORDER_STATUS('')
        this.inputInfo.state = this.formData || ''
      }
      if (!this.isTag) {
        return
      }
      if (
        this.$route.params.radio1 &&
        this.$route.params.radio1 !== this.radio1
      ) {
        this.pagination.page = 1
      }
      this.dictionaryNum = 1
      this.pageSize = 10
      this.currpage = 1
      this.current_num = 10
      if (this.radio1 == '1') {
        this.goodsListType = 1
        this.$router.currentRoute.matched[2].meta.title = '实物订单'
      } else if (this.radio1 == '2') {
        this.goodsListType = 2
        this.$router.currentRoute.matched[2].meta.title = '自提订单'
      } else if (this.radio1 == '3') {
        this.goodsListType = 3
        this.$router.currentRoute.matched[2].meta.title = '虚拟订单'
      } else if (this.radio1 == '5') {
        this.goodsListType = 5
        this.$router.currentRoute.matched[2].meta.title = '配送订单'
      }
      // else if(this.radio1 == '4') {
      //     this.goodsListType = 4
      //     this.$router.currentRoute.matched[2].meta.title = '活动订单'
      // }
      else {
        this.goodsListType = 0
        this.$router.currentRoute.matched[2].meta.title = ''
      }
      this.inputInfo.orderInfo = null
      this.inputInfo.store = null
      /* this.inputInfo.state = '' */
      this.inputInfo.type = null
      this.inputInfo.date = null

     /*  if (this.radio1 == 2) {
        // 自提订单默认待收货
        this.inputInfo.state = '2'
      } else if (this.radio1 != 3) {
        // 其他订单 默认查询代发货
        this.inputInfo.state = '1'
      } else {
        // 虚拟订单默认 待核销
        this.inputInfo.state = '8'
      } */
      this.orderLists()
      this.showPagebox = true
    },
    '$route.query.no': {
      handler: function () {
        if (this.$route.query.self_lifting == 1) {
          this.goodsListType = 2
          this.radio1 = '2'
          this.isTag = false
        } else {
          this.goodsListType = 0
          this.radio1 = ''
          this.$router.currentRoute.matched[2].meta.title = ''
        }

        orderList({
          api: 'admin.order.index',
          pageNo: this.dictionaryNum,
          pageSize: this.pageSize,
          id: this.$route.query.no,
          selfLifting: this.goodsListType
        }).then(res => {
          console.log(res)
          this.total = res.data.data.total
          this.tableData = res.data.data.list
          this.loading = false
          if (this.total < this.current_num) {
            this.current_num = this.total
          }
          this.isTag = true
        })
      },
      deep: true
    }
  },

  methods: {
    ...mapMutations('order', [
      'SET_ORDER_STATUS'
    ]),
     getOrderStatusText(orderStatus) {
     return  this.$enums.order.orderStatus.getOrderTypeLabel(orderStatus,this)
    },
    async getStore() {
      const res = await getStoreList({
        api: 'admin.order.getMch_store',
        mchId: getStorage('laike_admin_userInfo').mchId,
        sNo: this.ordernoId,
      })
      this.storeList = res.data.data.list
    },
    handleSelectionChange2(e) {
      this.tableRadio = e;
    },
    // 弹框方法
    dialogShow3(row) {
      console.log('row', row);
      this.is_appointment = row.is_appointment //1.无需预约下单 2.需要预约下单
      this.ordernoId = row.orderno
      this.verificationId = row.id
      this.getStore()
      this.dialogVisible3 = true
    },

    handleClose3() {
      this.dialogVisible3 = false
      this.ruleForm3.verification = ''
      this.ruleForm3.store = ''
      this.verificationId = ''
      this.ordernoId = ''
      setTimeout(() => {
        this.$refs['ruleForm3'].clearValidate()
      }, 100)
    },
    handleClose4() {
      this.handleClose3()
      this.dialogVisible4 = false
    },
    submitForm3(formName) {
      this.$refs[formName].validate(async valid => {
        console.log(this.ruleForm3)
        if (valid) {
          try {
            verificationExtractionCode({
              api: 'admin.Order.testExtractionCode',
              orderId: this.verificationId,
              mch_store_id: this.ruleForm3.store,
              extractionCode: this.ruleForm3.verification
            }).then(res => {
              console.log('res', res)
              if (res.data.code == '200') {
                this.ruleForm4.sNo = res.data.data.sNo
                this.ruleForm4.name = res.data.data.name
                this.ruleForm4.mobile = res.data.data.mobile
                this.ruleForm4.num = res.data.data.num
                this.ruleForm4.p_price = res.data.data.p_price
                this.ruleForm4.store_name = res.data.data.store_name
                this.ruleForm4.time = res.data.data.time
                this.orInfoList = res.data.data.por_list
                this.tableRadio = res.data.data.por_list?.[0]
                this.dialogVisible3 = false
                setTimeout(() => {
                  this.dialogVisible4 = true
                }, 200)
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
    submit() {
      verificationExtractionCode({
        api: 'admin.Order.VerificationExtractionCode',
        orderId: this.verificationId,
        pid: this.tableRadio.p_id,
        mch_store_id: this.ruleForm3.store,
        extractionCode: this.ruleForm3.verification
      }).then(res => {
        console.log('res', res)
        if (res.data.code == '200') {
          this.succesMsg(this.$t('zdata.hxcg'))
          this.handleClose4()
          this.orderLists()
        }
      })
    },
    // 跳转电子面单
    goPage(val) {
      this.$router.push({
        path: '/order/electronicSheetList/electronicList',
        query: {
          no: val.orderno
        }
      })
    },
    // 图片错误处理
    handleErrorImg(e) {
      console.log('图片报错了', e.target.src)
      e.target.src = ErrorImg
    },
    // 获取table高度
    getHeight() {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
      console.log(this.$refs.tableFather.clientHeight)
    },

    async orderLists() {
      this.loading = true
      const res = await orderList({
        api: 'admin.order.index',
        pageSize: this.pageSize,
        pageNo: this.dictionaryNum,
        keyWord: this.inputInfo.orderInfo ? this.inputInfo.orderInfo : null,
        mchName: this.inputInfo.store ? this.inputInfo.store : null,
        status: this.inputInfo.state ? this.inputInfo.state : null,
        operationType: this.inputInfo.type ? this.inputInfo.type : null,
        startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
        endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
        selfLifting: this.goodsListType
      })
      console.log(res)
      this.total = res.data.data.total
      this.tableData = res.data.data.list
      this.pagination.page = this.dictionaryNum
      this.loading = false
      if (this.total < this.current_num) {
        this.current_num = this.total
      }
      this.$nextTick(() => {
        this.$refs.table.doLayout()
      })
    },

    // 重置
    reset() {
      ; (this.inputInfo.orderInfo = null),
        (this.inputInfo.store = null),
        (this.inputInfo.state = null),
        (this.inputInfo.type = null),
        (this.inputInfo.date = null)
    },

    // 查询
    demand() {
      this.currpage = 1
      this.current_num = 10
      this.pageSize = 10
      this.showPagebox = false
      this.loading = true
      this.dictionaryNum = 1
      this.orderLists().then(() => {
        this.loading = false
        // if (this.tableData.length > 5) {
        this.showPagebox = true
        // }
      })
    },

    // 代客下单
    placeOrder() {
      this.$router.push('/order/orderList/valetOrder')
    },
    goBulk() {
      this.$router.push('/order/bulkDelivery/bulkList')
    },

    // 打印
    print() {
      if (this.orderList.length == 0) {
        this.$message({
          message: this.$t('orderLists.qxzdd'),
          type: 'warning',
          offset: 100
        })
      } else {
        let routeData = this.$router.resolve({
          path: '/print',
          query: {
            orderId: this.orderList
          }
        })

        window.open(routeData.href, '_blank')
      }
    },

    // 批量删除
    delAll() {
      this.$confirm(this.$t('orderLists.scqr'), this.$t('orderLists.ts'), {
        confirmButtonText: this.$t('orderLists.okk'),
        cancelButtonText: this.$t('orderLists.ccel'),
        type: 'warning'
      })
        .then(() => {
          delOrder({
            api: 'admin.order.del',
            orders: this.orderList
          }).then(res => {
            console.log(res)
            if (res.data.code == '200') {
              this.$message({
                message: this.$t('orderLists.cg'),
                type: 'success',
                offset: 100
              })
              // this.orderLists();
              this.isFillList()
              this.$store.dispatch('orderNum/getOrderCount')
            }
          })
        })
        .catch(() => {
          // this.$message({
          //   type: 'info',
          //   message: '已取消删除',
          //   offset: 100
          // });
        })
    },
    CancellationOfOrder(item) {
      this.$confirm(this.$t('orderLists.scqr1'), this.$t('orderLists.ts'), {
        confirmButtonText: this.$t('orderLists.okk'),
        cancelButtonText: this.$t('orderLists.ccel'),
        type: 'warning'
      })
        .then(() => {
          delOrder({
            api: 'admin.order.CancellationOfOrder',
            sNo: item.orderno
          }).then(res => {
            console.log(res)
            if (res.data.code == '200') {
              this.$message({
                message: this.$t('group.czcg'),
                type: 'success',
                offset: 100
              })
              // this.orderLists();
              this.isFillList()
            }
          })
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: this.$t('orderLists.yqx'),
            offset: 100
          });
        })
    },
    isFillList() {
      let totalPage = Math.ceil(
        (this.total - this.tableData.length) / this.pageSize
      )
      // let totalPage = this.orderList.length
      let dictionaryNum =
        this.dictionaryNum > totalPage ? totalPage : this.dictionaryNum
      this.dictionaryNum = dictionaryNum < 1 ? 1 : dictionaryNum
      console.log('this.dictionaryNum', this.dictionaryNum, this.pageSize)
      this.orderLists() //数据初始化方法
    },

    //选择一页多少条
    handleSizeChange(e) {
      this.loading = true
      console.log(e)
      // this.current_num = e
      this.pageSize = e
      this.orderLists().then(() => {
        this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1
        this.current_num =
          this.tableData.length === this.pageSize
            ? this.dictionaryNum * this.pageSize
            : this.total
        this.loading = false
      })
    },

    //点击上一页，下一页
    handleCurrentChange(e) {
      this.loading = true
      this.dictionaryNum = e
      this.currpage = (e - 1) * this.pageSize + 1
      this.orderLists().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total
        this.loading = false
      })
    },

    // 订单详情
    Details(value) {
      this.$router.push({
        path: '/order/orderList/orderDetails',
        query: {
          no: value.orderno,
          type: this.radio1,
          write_off_settings: value.write_off_settings
        }
      })
    },

    // 编辑订单
    Edit(value) {
      this.$router.push({
        path: '/order/orderList/editorOrder',
        query: {
          no: value.orderno,
          dictionaryNum: this.dictionaryNum,
          pageSize: this.pageSize,
          radio1: this.radio1,
          goodsListType: this.goodsListType
        }
      })
    },

    // 商品发货
    Delivery(value) {
      if (value.self_lifting == 2) {
        //商家配送 直接弹窗填写配送员信息
        this.dialogVisible5 = true
        //记录订单号
        this.ruleForm5.sNo = value.orderno
        this.detailId = value.detailId
      } else {
        this.SET_ORDER_STATUS(this.inputInfo.state)
        //去选择商品页面 发货
        this.$router.push({
          path: '/order/orderList/goodsDeliverys',
          query: {
            no: value.orderno,
            dictionaryNum: this.dictionaryNum,
            pageSize: this.pageSize,
            radio1: this.radio1
          }
        })
      }
    },
    //商家配送 弹窗取消
    handleClose5(done) {
      this.dialogVisible5 = false
      this.ruleForm5.verification = ''
      setTimeout(() => {
        this.$refs['ruleForm5'].clearValidate()
      }, 100)
    },
    //商家配送 弹窗确认发货
    submitForm5(value) {
      this.dialogVisible5 = false
      let api = 'admin.order.UnifiedShipment'
      let expressid = ''
      let courierNum = ''
      let type = '3'
      let psyInfo = {
        name: this.ruleForm5.courier_name,
        tel: this.ruleForm5.phone
      }
      let orderList = [
        {
          detailId: this.detailId,
          num: 1,
        },
      ]
      let list = {
        expressid, //物流公司id
        courierNum, //物流号
        type,	//发货类型 1普通发货 2电子面单 3商家配送
        psyInfo, //配送员信息
        orderList, //订单信息 detailId订单详情id num订单商品数量
      }
      list = JSON.stringify(list)
      kuaidishow({ api, list }).then(res => {
        console.log(res)
        if (res.data.code == 200) {
          this.$message({
            message: this.$t('发货成功'),
            type: 'success',
            offset: 100
          })
          this.orderLists()
        }
      })
    },

    // 查看物流
    Logistics(value) { },

    // 选框改变
    handleSelectionChange(val) {
      if (val.length == 0) {
        this.is_disabled = true
      } else {
        this.is_disabled = false
      }
      console.log(val)
      this.orderList = val.map(item => {
        return item.orderno
      })
      this.orderList = this.orderList.join(',')
    },

    // 弹框方法
    dialogShow2(value) {
      this.dialogVisible2 = true
      kuaidishow({
        api: 'admin.order.kuaidishow',
        orderno: value.orderno
      }).then(res => {
        console.log(res)
        this.logisticsList = res.data.data.list
        this.ruleForm.courier_company = res.data.data.list[0].courier_num
        this.ruleForm.courier_no = res.data.data.list[0].kuaidi_name
        this.ruleForm.logistics = ''
        // this.logisticsTracking = res.data.data.list[0].list;
      })
      console.log(value)
    },

    handleClose2(done) {
      this.dialogVisible2 = false
    },

    // 导出弹框方法
    dialogShow() {
      this.dialogVisible = true
    },

    handleClose(done) {
      this.dialogVisible = false
    },

    async exportPage() {
      exports(
        {
          api: 'admin.order.index',
          pageNo: this.dictionaryNum,
          pageSize: this.pageSize,
          exportType: 1,
          keyWord: this.inputInfo.orderInfo ? this.inputInfo.orderInfo : null,
          mchName: this.inputInfo.store ? this.inputInfo.store : null,
          status: this.inputInfo.state ? this.inputInfo.state : null,
          operationType: this.inputInfo.type ? this.inputInfo.type : null,
          startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
          endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
          selfLifting: this.goodsListType
        },
        'pageorder'
      )
    },

    async exportAll() {
      exports(
        {
          api: 'admin.order.index',
          pageNo: 1,
          pageSize: 999999,
          exportType: 1
          // keyWord: this.inputInfo.orderInfo ? this.inputInfo.orderInfo : null,
          // mchName: this.inputInfo.store ? this.inputInfo.store : null,
          // status: this.inputInfo.state ? this.inputInfo.state : null,
          // operationType: this.inputInfo.type ? this.inputInfo.type : null,
          // startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
          // endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
          // selfLifting: this.goodsListType,
        },
        'allorder'
      )
    },

    async exportQuery() {
      exports(
        {
          api: 'admin.order.index',
          pageNo: 1,
          pageSize: this.total,
          exportType: 1,
          keyWord: this.inputInfo.orderInfo ? this.inputInfo.orderInfo : null,
          mchName: this.inputInfo.store ? this.inputInfo.store : null,
          status: this.inputInfo.state ? this.inputInfo.state : null,
          operationType: this.inputInfo.type ? this.inputInfo.type : null,
          startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
          endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
          selfLifting: this.goodsListType
        },
        'queryorder'
      )
    }
  }
}
