import {index, save} from '@/api/finance/withdrawalManage'
import pageData from '@/api/constant/page'
import { exports } from '@/api/export'
import {isEmpty} from "element-ui/src/utils/util";
import { mixinstest } from '@/mixins/index'
import { getStorage } from '@/utils/storage'
import { getButton } from '@/api/layout/information'

export default {
  name: 'withdrawalRecordList',
  mixins: [mixinstest],
  data() {
    return {
      laikeCurrencySymbol:'￥',
      radio1: this.$t('withdrawalExamineList.txjl'),
      routerList:JSON.parse(sessionStorage.getItem('tabRouter')),
      page: pageData.data(),
      dialogVisible: false,
      button_list:[],
      // table高度
      tableHeight: null,
      menuId:'',
      sourceList: [
        {
          value: "2",
          label: this.$t('微信零钱'),
        },
        {
          value: "1",
          label: this.$t('银行卡'),
        },
      ], // 提现方式
      statusList: [
        {
          value: 1,
          label: this.$t('stores.withdrawalRecord.shytg')
        },
        {
          value: 2,
          label: this.$t('stores.withdrawalRecord.yjj')
        }
      ], // 审核状态
      dkstatusList: [
        {
          value: 1,
          label: this.$t('进行中')
        },
        {
          value: 2,
          label: this.$t('已完成')
        },
        {
          value: 2,
          label: this.$t('提现失败')
        }
      ], // 提现状态
    }
  },

  created() {
    this.laikeCurrencySymbol = this.LaiKeCommon.getDefaultCurrencySymbol();
    this.loadData()
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
          if(item.path=='withdrawalManage'){
              item.children.forEach(e => {
                  if(e.path=='withdrawalRecordList'){
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
    async loadData() {
      const res = await index({
        api: 'admin.user.getWithdrawalRecord',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        userNameAndPhone: this.page.inputInfo.name,
        phone: this.page.inputInfo.phone,
        startDate: isEmpty(this.page.inputInfo.date) ? "" : this.page.inputInfo.date[0],
        endDate: isEmpty(this.page.inputInfo.date) ? "" : this.page.inputInfo.date[1],
        withdrawStatus: this.page.inputInfo.txType,
        status: this.page.inputInfo.status,
        wxStatus:this.page.inputInfo.dkstatus,
      })
      this.total = res.data.data.total
      this.page.tableData = res.data.data.list
      this.page.loading = false
      if(res.data.data.total < 10) {
        this.current_num = this.total
      }
    },

    // 重置
    reset() {
      this.page.inputInfo.phone = null
      this.page.inputInfo.name = null
      this.page.inputInfo.date = null
      this.page.inputInfo.txType = null
      this.page.inputInfo.status = null
      this.page.inputInfo.dkstatus = null
    },

    // 查询
    demand() {
      this.currpage = 1
      this.current_num = 10
      this.page.showPagebox = false
      this.page.loading = true
      this.dictionaryNum = 1
      this.loadData().then(() => {
        this.page.loading = false
        if (this.page.tableData.length > 5) {
          this.page.showPagebox = true
        }
      })
    },

    //选择一页多少条
    handleSizeChange(e) {
      console.log(e);
      this.page.loading = true
      // this.current_num = e
      this.pageSize = e
      this.loadData().then(() => {
        this.currpage = ((this.dictionaryNum - 1) * this.pageSize) + 1
        this.current_num = this.tableData.length === this.pageSize ? this.dictionaryNum * this.pageSize : this.total
        this.loading = false
      })
    },

    //点击上一页，下一页
    handleCurrentChange(e) {
      this.page.loading = true
      this.dictionaryNum = e
      this.currpage = ((e - 1) * this.pageSize) + 1
      this.loadData().then(() => {
        this.current_num = this.page.tableData.length === this.pageSize ? e * this.pageSize : this.total
        this.page.loading = false
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
        api: 'admin.user.getWithdrawalRecord',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        exportType: 1,
        userNameAndPhone: this.page.inputInfo.name,
        phone: this.page.inputInfo.phone,
        startDate: isEmpty(this.page.inputInfo.date) ? "" : this.page.inputInfo.date[0],
        endDate: isEmpty(this.page.inputInfo.date) ? "" : this.page.inputInfo.date[1],
        withdrawStatus: this.page.inputInfo.txType,
        status: this.page.inputInfo.status,
        wxStatus:this.page.inputInfo.dkstatus,
      },'WithdrawalInfo')
    },

    async exportAll() {
      exports({
        api: 'admin.user.getWithdrawalRecord',
        pageNo: 1,
        pageSize: 9999,
        exportType: 1,
      },'WithdrawalInfo')
    },

    async exportQuery() {
      exports({
        api: 'admin.user.getWithdrawalRecord',
        pageNo: 1,
        pageSize: this.total,
        exportType: 1,
        userNameAndPhone: this.page.inputInfo.name,
        phone: this.page.inputInfo.phone,
        startDate: isEmpty(this.page.inputInfo.date) ? "" : this.page.inputInfo.date[0],
        endDate: isEmpty(this.page.inputInfo.date) ? "" : this.page.inputInfo.date[1],
        withdrawStatus: this.page.inputInfo.txType,
        status: this.page.inputInfo.status,
        wxStatus:this.page.inputInfo.dkstatus,
      },'WithdrawalInfo')
    }
  }
}
