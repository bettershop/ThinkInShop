import {
  getMchInfo,
  delMchInfo,
  getMchFl,
  goStoreInfo,
  getUrl,
  getDownload
} from '@/api/plug_ins/stores'
import { mixinstest } from '@/mixins/index'
import { getStorage } from '@/utils/storage'
import { getButton } from '@/api/layout/information'
import { downloadXlsx } from '@/utils/excel'
import { error } from 'jquery'
import ErrorImg from '@/assets/images/default_picture.png'

export default {
  name: 'store',
  mixins: [mixinstest],
  data() {
    return {
      routerList: JSON.parse(sessionStorage.getItem('tabRouter')),
      radio1: this.$t('stores.dp'),
      statusList: [
        // {
        //     value: '0',
        //     label: '未营业'
        // },
        {
          value: '1',
          label: this.$t('stores.store.yyz')
        },
        {
          value: '2',
          label: this.$t('stores.store.ydy')
        }
      ], // 会员等级
      button_list: [],
      inputInfo: {
        status: null,
        storeName: null,
        promiseStatus: null,
        mac_choose_fl: null
      },
      menuId: '',
      tableData: [],
      loading: true,
      // table高度
      tableHeight: null,
      //自营店id
      laike_admin_userInfo: '',
      tag: '',
      rules2: {
        time: [
          {
            required: true,
            message: this.$t('stores.store.qxzzdrq'),
            trigger: 'change'
          }
        ]
      },
      ruleForm2: {
        time: ''
      },
      dialogVisible1: false,
      choose_fl: [
        {
          value: '0',
          label: '分类1'
        },
        {
          value: '1',
          label: '分类2'
        },
        {
          value: '2',
          label: '分类3'
        }
      ] // 店铺分类
    }
  },

  created() {
    if (getStorage('laike_admin_userInfo').mchId == 0) {
      this.$message({
        type: 'error',
        message: this.$t("plugInsSet.plugInsList.qtjdp"),
        offset: 102
      })
      this.$router.push('/mall/fastBoot/index')
    }
    if (this.$route.params.pageSize) {
      this.dictionaryNum = this.$route.params.dictionaryNum
      this.pageSize = this.$route.params.pageSize
    }
    this.getMchInfos()
    this.getMchFls()
    this.getButtons()
    this.laike_admin_userInfo = getStorage('laike_admin_userInfo').mchId
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
      console.log('图片报错了', e.target.src)
      e.target.src = ErrorImg
    },
    toset() {
      if (getStorage('laike_admin_userInfo').mchId !== 0) {
        this.$router.push('/plug_ins/stores/storeSet')
      } else {
        this.$message({
          type: 'error',
          message: '请添加店铺!',
          offset: 102
        })
        this.$router.push('/mall/fastBoot/index')
      }
    },
    // 获取table高度
    getHeight() {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
    },
    //获取按纽权限
    async getButtons() {
      let route = getStorage('route')
      route.forEach(item => {
        if (item.path == 'stores') {
          item.children && item.children.forEach(e => {
            if (e.path == 'store') {
              return (this.menuId = e.id)
            }
          })
        }
      })
      let buttonList = await getButton({
        api: 'saas.role.getButton',
        menuId: this.menuId
      })
      this.button_list = buttonList.data.data
      this.button_list = buttonList.data.data.map(item => {
        return item.title
      })
      console.log(this.button_list, '获取按纽权限')
    },
    tags(value) {
      if (this.tag == value.id) {
        this.tag = ''
      } else {
        this.tag = value.id
      }
    },
    // 查看
    seeLedger(row) {
      this.$router.push({
        path: '/plug_ins/stores/LedgerSetting',
        query: {
          id: row.id,
          dictionaryNum: this.dictionaryNum,
          pageSize: this.pageSize
        }
      })
    },
    dialogShow2() {
      this.$nextTick(() => {
        this.$refs['ruleForm2'].clearValidate()
      })
      this.dialogVisible1 = true
    },
    handleClose2() {
      this.ruleForm2.time = ''
      this.dialogVisible1 = false
      this.$nextTick(() => {
        this.$refs['ruleForm2'].clearValidate()
      })
    },
    submitForm2(formName2) {
      this.$refs[formName2].validate(async valid => {
        console.log(this.ruleForm)
        if (valid) {
          try {
            getUrl({
              api: 'admin.divideAccount.applyBilling',
              date: this.ruleForm2.time
            }).then(res => {
              console.log('res', res)
              if (res.data.code == '200') {
                var download_url = res.data.data.download_url
                getDownload({
                  api: 'admin.divideAccount.downloadBilling',
                  url: download_url,
                  exportType: 1
                }).then(res2 => {
                  downloadXlsx(res2, '分账账单')
                  this.handleClose2()
                  this.$message({
                    message: this.$t('stores.store.xzcg'),
                    type: 'success',
                    offset: 102
                  })
                })
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
    async getMchInfos() {
      const res = await getMchInfo({
        api: 'mch.Admin.Mch.GetMchInfo',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        isOpen: this.inputInfo.status,
        name: this.inputInfo.storeName,
        promiseStatus: this.inputInfo.promiseStatus,
        cid: this.inputInfo.mac_choose_fl
      })
      this.total = res.data.data.total
      this.tableData = res.data.data.list
      console.log(this.tableData)
      this.loading = false
      if (this.total < this.current_num) {
        this.current_num = this.total
      }
      console.log(res)
    },

    async getMchFls() {
      const res = await getMchFl({
        api: 'mch.Admin.Mch.MchClassList'
      })
      console.log('店铺分类res', res)
      if (res.data.code == 200) {
        this.choose_fl = res.data.data.list
      } else {
        console.log(res.data.message)
      }
    },

    // 重置
    reset() {
      this.inputInfo.status = null
      this.inputInfo.storeName = null
      this.inputInfo.promiseStatus = null
      this.inputInfo.mac_choose_fl = null
    },

    // 查询
    demand() {
      this.loading = true
      this.dictionaryNum = 1
      this.handleCurrentChange(1)
      // this.getMchInfos().then(() => {
      //   this.loading = false
      // })
    },

    // 查看
    View(value) {
      this.$router.push({
        path: '/plug_ins/stores/viewStore',
        query: {
          id: value.id,
          dictionaryNum: this.dictionaryNum,
          pageSize: this.pageSize,
          mchLook: true
        }
      })
    },

    // 修改
    Modify(value) {
      this.$router.push({
        path: '/plug_ins/stores/editorStore',
        query: {
          id: value.id,
          dictionaryNum: this.dictionaryNum,
          pageSize: this.pageSize
        }
      })
    },
    Viewdata(row) {
      goStoreInfo({
        api: 'mch.Admin.Mch.StoreLookMch',
        mchId: row.id
      }).then(res => {
        console.log('res', res)
        if (res.data.code == '200') {
          //postMessage用法
          // const data = { token: res.data.data.token };
          // const targetUrl = res.data.data.pcMchPath
          // const childWindow = window.open(targetUrl, '_blank');
          // setTimeout(() => {
          //     childWindow.postMessage(data, targetUrl);
          // }, 100);
          //widow.name用法
          const targetUrl = res.data.data.pcMchPath
          const rargeObj = [res.data.data.token, res.data.data.store_id]
          window.open(targetUrl, rargeObj)
        }
      })
    },
    // 删除
    Delete(value) {
      this.$confirm(this.$t('stores.store.scdp'), this.$t('stores.ts'), {
        confirmButtonText: this.$t('stores.okk'),
        cancelButtonText: this.$t('stores.ccel'),
        type: 'warning'
      })
        .then(() => {
          delMchInfo({
            api: 'mch.Admin.Mch.DelMchInfo',
            mchId: value.id
          }).then(res => {
            console.log(res)
            if (res.data.code == '200') {
              this.getMchInfos()
              this.$message({
                type: 'success',
                message: this.$t('stores.sccg'),
                offset: 102
              })
            }
          })
        })
        .catch(() => {
          // this.$message({
          //     type: 'info',
          //     message: '已取消删除',
          //     offset: 102
          // });
        })
    },

    //选择一页多少条
    handleSizeChange(e) {
      this.loading = true
      console.log(e)
      // this.current_num = e
      this.pageSize = e
      this.getMchInfos().then(() => {
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
      this.getMchInfos().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total
        this.loading = false
      })
    }
  }
}
