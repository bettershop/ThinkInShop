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
    name: 'salesRecord',
    mixins: [mixinstest],

    data() {
      return {
        routerList:JSON.parse(sessionStorage.getItem('tabRouter')),
        radio1: '3',
        button_list:[],
        menuId:'',
        stateList: [{
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
        typeList: [{
          value: '1',
          label: this.$t('preSale.scheduledRecord.djms')
        },
        {
          value: '2',
          label: this.$t('preSale.scheduledRecord.dhms')
        }
        ], // 预售类型
        bool:false,
        inputInfo: {
          type: null,
          goodsName: null,
          date: null,
          mchName: null
        },
        //total:0,
        inOrderNum:'',
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
      this.$nextTick(function() {
        this.getHeight()
      })
      window.addEventListener('resize', this.getHeight(), false)
    },

    methods: {
    //获取按纽权限
    async getButtons() {
      const route=getStorage('route')
      route.forEach(item => {
          if(item.redirect=='/plug_ins/preSale/goodsList'){
              item.children.forEach(e => {
                  if(e.path=='salesRecord'){
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

      },

      async getSecRecords() {
        const res = await getSecRecord({
          api: 'plugin.presell.AdminPreSell.getOrderList',
          pageSize: this.pageSize,
          pageNo: this.dictionaryNum,
          sellType: this.inputInfo.type ? this.inputInfo.type : null,
          product:this.inputInfo.goodsName,
          mchName: this.inputInfo.mchName ? this.inputInfo.mchName : null,
          startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
          endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
          status:5,
        })
        console.log(res);
        this.current_num = 10
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
          sellType: this.inputInfo.type ? this.inputInfo.type : null,
          product:this.inputInfo.goodsName,
          mchName: this.inputInfo.mchName ? this.inputInfo.mchName : null,
          startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
          endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
          status:5,
        }, 'pageorder')
      },

      async exportAll() {
        exports({
          api: 'plugin.presell.AdminPreSell.getOrderList',
          pageNo: 1,
          pageSize: 99999,
          exportType: 1,
          status: 5,
        }, 'allorder')
      },

      async exportQuery() {
        exports({
          api: 'plugin.presell.AdminPreSell.getOrderList',
          pageNo:1,
          pageSize: this.total,
          exportType: 1,
          sellType: this.inputInfo.type ? this.inputInfo.type : null,
          product:this.inputInfo.goodsName,
           mchName: this.inputInfo.mchName ? this.inputInfo.mchName : null,
          startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
          endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
          status:5,
        }, 'queryorder')
      }
    }
  }
