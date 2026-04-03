import {
    getSecRecord,
    delSecRecord
  } from '@/api/plug_ins/preSale'
  import {
    mixinstest
  } from '@/mixins/index'
  import {
    exports
  } from '@/api/export/index'
  import { getButton } from '@/api/layout/information'
  import { getStorage } from '@/utils/storage'

  export default {
    name: 'scheduledRecord',
    mixins: [mixinstest],

    data() {
      return {
        routerList:JSON.parse(sessionStorage.getItem('tabRouter')),
        inOrderNum:'',
        radio1: '2',
        button_list:[],
        menuId:'',
        stateList: [{
            value: '0',
            label: this.$t('preSale.scheduledRecord.dfk')
          },
          {
            value: '1',
            label: this.$t('preSale.scheduledRecord.dfh')
          },
          {
            value: '2',
            label: this.$t('preSale.scheduledRecord.dsh')
          },
          {
            value: '5',
            label: this.$t('preSale.scheduledRecord.ywc')
          },
          // {
          //   value: '7',
          //   label: this.$t('preSale.scheduledRecord.ygb')
          // }
        ], // 订单状态
        typeList: [{
            value: '1',
            label: this.$t('preSale.scheduledRecord.djms')
          },
          {
            value: '2',
            label: this.$t('preSale.scheduledRecord.dhms')
          }
        ], // 预售类型

        inputInfo: {
          type: null,
          state: null,
          goodsName: null,
          date: null,
          mchName: null
        },
        tableData: [],
        loading: true,
        // table高度
        tableHeight: null,
        // 导出弹框数据
        dialogVisible: false,
      }
    },

    created() {

      if (this.$route.query.id) {
        this.inputInfo.goodsName=this.$route.query.id
      }
      this.getSecRecords()
      this.getButtons()
      this.inOrderNum=getStorage('inOrderNum')
    },

    mounted() {
      this.$nextTick(() => {
        this.getHeight()
      })
      window.addEventListener('resize', this.getHeight(), false)
    },
    watch: {
      $route() {
        this.$nextTick(() => {
          // myTable是表格的ref属性值
          if (this.$refs.table && this.$refs.table.doLayout) {
            this.$refs.table.doLayout();
          }
        })
      }
   },
    methods: {
    //获取按纽权限
    async getButtons() {
      const route=getStorage('route')
      route.forEach(item => {
          if(item.redirect=='/plug_ins/preSale/goodsList'){
              item.children.forEach(e => {
                  if(e.path=='scheduledRecord'){
                      return this.menuId=e.id
                  }
              })
          }
      });
      let buttonList = await getButton ({
      api:'saas.role.getButton',
      menuId: this.menuId,
      })
      this.button_list=buttonList.data.data
      console.log(this.button_list,"获取按纽权限",this.menuId)
      },

      async getSecRecords() {
        const res = await getSecRecord({
          api: 'plugin.presell.AdminPreSell.getOrderList',
          pageSize: this.pageSize,
          pageNo: this.dictionaryNum,
          sellType: this.inputInfo.type ? this.inputInfo.type : null,
          product: this.inputInfo.goodsName,
          startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
          endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
          isRefund:0,
          statusList: '0,1,2,5',
          mchName: this.inputInfo.mchName ? this.inputInfo.mchName : null,
          status:this.inputInfo.state ? this.inputInfo.state : null,
        })
        console.log('11111--》', res);
        this.total = res.data.data.total
        this.tableData = res.data.data.list
        this.tableData.forEach((item, index)=>{
          switch (item.sell_type) {
              case 1||'1':
                  item.sellTypeDesc = this.$t('preSale.goodsList.djmc')
                  break
              case 2||'2':
                  item.sellTypeDesc = this.$t('preSale.goodsList.dhmc')
                  break
              default:
                  item.sellTypeDesc = this.$t('没有这个判断类型，来这改')
                  break
          }
          switch (item.status) {
              case 1||'1':
                  item.statusDesc = this.$t('preSale.scheduledRecord.dfh')
                  break
              case 2||'2':
                  item.statusDesc = this.$t('preSale.scheduledRecord.dsh')
                  break
              case 5||'5':
                  item.statusDesc = this.$t('preSale.scheduledRecord.ywc')
                  break
              case 7||'7':
                  item.statusDesc = this.$t('preSale.scheduledRecord.ygb')
                  break
              default:
                  item.statusDesc = this.$t('preSale.scheduledRecord.dfk')
                  break
          }
        })
        this.loading = false
        if (this.total < this.current_num) {
          this.current_num = this.total
        }
        if (this.total == 0) {
          this.showPagebox = false
        } else {
          this.showPagebox = true
        }
      },

      // 获取table高度
      getHeight() {
        this.tableHeight = this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
      },

      // 重置
      reset() {
        this.inputInfo.type = null
        this.inputInfo.state = null
        this.inputInfo.goodsName = null
        this.inputInfo.date = null
        this.inputInfo.mchName = null
      },

      // 查询
      demand() {
        this.showPagebox = false
        this.loading = true
        this.dictionaryNum = 1
        this.getSecRecords().then(() => {
          this.loading = false
          if (this.tableData.length > 5) {
            this.showPagebox = true
          }
        })
      },


      //选择一页多少条
      handleSizeChange(e) {
        this.loading = true
        console.log(e);
        // this.current_num = e
        this.pageSize = e
        this.getSecRecords().then(() => {
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
        this.getSecRecords().then(() => {
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
          api: 'plugin.presell.AdminPreSell.getOrderList',
          pageNo: this.dictionaryNum,
          pageSize: this.pageSize,
          exportType: 1,
          status: this.inputInfo.state,
          startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
          endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
          sellType: this.inputInfo.type ? parseInt(this.inputInfo.type) : null,//类型
          product:this.inputInfo.goodsName ? this.inputInfo.goodsName :null ,
          mchName: this.inputInfo.mchName ? this.inputInfo.mchName : null,
          statusList: '0,1,2,5',
          isRefund:0,
        }, 'pageorder')
      },

      async exportAll() {
        exports({
          api: 'plugin.presell.AdminPreSell.getOrderList',
          pageNo: 1,
          pageSize: 999999,
          exportType: 1,
        }, 'allorder')
      },

      async exportQuery() {
        exports({
          api: 'plugin.presell.AdminPreSell.getOrderList',
          pageNo:1,
          pageSize: this.total,
          exportType: 1,
          status: this.inputInfo.state,
          startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
          endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
          sellType: this.inputInfo.type ? parseInt(this.inputInfo.type) : null,//类型
          product:this.inputInfo.goodsName ? this.inputInfo.goodsName :null ,
          mchName: this.inputInfo.mchName ? this.inputInfo.mchName : null,
          statusList: '0,1,2,5',
          isRefund:0,
        }, 'queryorder')
      }
    }
  }
