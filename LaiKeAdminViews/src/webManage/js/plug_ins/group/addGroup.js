import { mapState } from 'vuex'
import { getStore, addGroup, getStoreInfo } from '@/api/plug_ins/group'
import { getStorage } from '@/utils/storage'
import { choiceClass } from '@/api/goods/goodsList'
import { mixinstest } from '@/mixins/index'
export default {
  name: 'addGroup',
  mixins: [mixinstest],
  computed: {
    ...mapState({
      selection: 'selection'
    })
  },
  data () {
    return {
      pickerOptions: {
        disabledDate (time) {
          return time.getTime() < Date.now() - 8.64e7
        }
      },
      ruleForm: {
        name: '', //活动名称
        teamSwitch: 0, //团长参团限制
        againSwitch: 0, //同一商品参团限制
        // showSwitch: 0, //showSwitch
        date: [],
        type: 1
      },
      attributeList: [],
      inputInfo: {
        goodsBrandId: '',
        goodsClass: '',
        goodsName: ''
      },
      rules: {
        name: [
          {
            required: true,
            message: this.$t('group.addGroup.qsrhdmc'),
            trigger: 'blur'
          }
        ],
        date: [
          {
            required: true,
            message: this.$t('group.addGroup.qsrpthdsj'),
            trigger: 'change'
          }
        ]
      },
      tableList: [],
      brandList: [],
      classList: [],
      tabKey: this.$t('group.addGroup.xzsp'),
      inputInfo: {
        goodsClass: '',
        goodsBrandId: '',
        goodsName: ''
      },
      tableData: [],
      loading: true,
      // table高度
      tableHeight: null,
      ekko: 0,
      includesArr: []
    }
  },

  created () {
    if (this.$route.params.pageSize) {
      this.pagination.page = this.$route.params.dictionaryNum
      this.dictionaryNum = this.$route.params.dictionaryNum
      this.pageSize = this.$route.params.pageSize
    }
    this.currentTime()
    this.getStoreList()
    this.choiceClasss()
    if (this.attributeList.length == 0) {
      this.attributeList.push({
        num: '',
        canDiscount: '',
        openDiscount: ''
      })
    }
    if (this.$route.query.ekko) {
      this.ekko = this.$route.query.ekko
      if (this.ekko == 1) {
        this.$router.currentRoute.matched[2].meta.title = '编辑'
      } else if (this.ekko == 2) {
        this.tabKey = this.$t('group.addGroup.ckxq')
        this.$router.currentRoute.matched[2].meta.title = '查看详情'
      }
      this.getInfo() //数据回显
      this.getStoreInfos()
    }
  },
  mounted () {
    this.$nextTick(function () {
      this.getHeight()
    })
    window.addEventListener('resize', this.getHeight(), false)
  },
  methods: {
    layoutChange(){
      // this.$nextTick(() => {
      //   this.$refs.table.doLayout();
      //   this.$refs.Shacotable.doLayout();
      // });
      // this.demand()
    },
    async getStoreList () {
      const res = await getStore({
        api: 'plugin.group.AdminGroup.GoodsList',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        classId:
          this.inputInfo.goodsClass[this.inputInfo.goodsClass.length - 1],
        goodsBrandId: this.inputInfo.goodsBrandId,
        goodsName: this.inputInfo.goodsName
      })
      this.total = res.data.data.total
      this.tableData = res.data.data.list
      this.loading = false
      if (this.total < this.current_num) {
        this.current_num = this.total
      }
    },
    async getInfo () {
      const res = await getStoreInfo({
        api: 'plugin.group.AdminGroup.GroupActivityById',
        id: this.$route.query.id ?? ''
      })
      let arr = res.data.data
      this.ruleForm.name = arr.name
      // this.ruleForm.showSwitch = arr.is_show
      this.ruleForm.type = 2
      this.ruleForm.date = [arr.startDate, arr.endDate]
      this.ruleForm.teamSwitch = arr.teamLimit
      this.ruleForm.againSwitch = arr.goodsLimit
      this.attributeList = arr.rule
    },
    async getStoreInfos () {
      const res = await getStore({
        api: 'plugin.group.AdminGroup.GoodsList',
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        activityId: this.$route.query.id ?? ''
      })
      this.tableList = res.data.data.list
      this.tableList.forEach(item => {
        this.$refs.table.toggleRowSelection(item)
      })
      console.log('tableList', this.tableList)

      this.tabKey = this.$t('group.addGroup.yxsp')
      this.getStoreList()
    },
    cloRow () {
      this.tableList = []
      this.$refs.table.clearSelection()
    },
    getHeight () {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
    },
    reset () {
      this.inputInfo.goodsClass = ''
      this.inputInfo.goodsBrandId = ''
      this.inputInfo.goodsName = ''
      this.brandList =[]
    },
    demand () {
      // this.currpage = 1
      // this.current_num = 10
      // this.showPagebox = false
      this.loading = true
      this.dictionaryNum = 1
      this.getStoreList().then(() => {
        this.loading = false
      })
    },
    //选择一页多少条
    handleSizeChange (e) {
      this.loading = true
      console.log('e', e)
      // this.current_num = e
      this.pageSize = e
      this.getStoreList().then(() => {
        this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1
        this.current_num =
          this.tableData.length === this.pageSize
            ? this.dictionaryNum * this.pageSize
            : this.total
        this.loading = false
      })
    },

    //点击上一页，下一页
    handleCurrentChange (e) {
      this.loading = true
      this.dictionaryNum = e
      this.currpage = (e - 1) * this.pageSize + 1
      this.getStoreList().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total
        this.loading = false
      })
    },
    currentTime () {
      var date = new Date()
      var year = date.getFullYear() //月份从0~11，所以加一
      var dateArr = [date.getMonth() + 1, date.getDate()]
      var dateArr2 = [date.getMonth() + 1, date.getDate() - 1]
      //如果格式是MM则需要此步骤，如果是M格式则此循环注释掉
      for (var i = 0; i < dateArr.length; i++) {
        if (dateArr[i] >= 1 && dateArr[i] <= 9) {
          dateArr[i] = '0' + dateArr[i]
        }
      }
      var strDate = year + '-' + dateArr[0] + '-' + dateArr[1]
      let dateTime = new Date().getFullYear() /* 获取现在的年份 */
      dateTime = new Date(new Date().setFullYear(dateTime + 1)).getFullYear()
      var strDate1 = dateTime + '-' + dateArr2[0] + '-' + dateArr2[1]
      this.ruleForm.date = [strDate, strDate1]
      console.log('this.ruleForm.date', this.ruleForm.date)
    },
    minus (index) {
      if (this.attributeList.length !== 0) {
        this.attributeList.splice(index, 1)
      }
    },
    Delete (index, row) {
      this.tableList.splice(index, 1)
      // this.$refs.table.toggleRowSelection(
      //   this.tableData.find(item => {
      //     return row.id == item.id
      //   }),
      //   false
      // )
      this.$refs.table.selection.forEach(item => {
        if (row.id == item.id) {
          this.$refs.table.toggleRowSelection(item, false)
        }
      })
    },
    rowKeys (row) {
      // console.log('row',row);
      return row.id
    },
    addOne () {
      this.attributeList.push({
        num: '',
        canDiscount: '',
        openDiscount: ''
      })
    },
    // 获取商品类别
    async choiceClasss () {
      const res = await choiceClass({
        api: 'admin.goods.choiceClass'
      })
      res.data.data.list.class_list[0].forEach((item, index) => {
        let obj = item
        this.classList.push({
          value: obj.cid,
          label: obj.pname,
          index: index,
          level: obj.level,
          children: []
        })
      })
    },
    // 根据商品类别id获取商品品牌
    changeProvinceCity (value) {
      this.inputInfo.goodsBrandId = ''
      this.brandList=[]
      choiceClass({
        api: 'admin.goods.choiceClass',
        classId: value.length > 1 ? value[value.length - 1] : value[0]
      }).then(res => {
        let num = this.$refs.myCascader.getCheckedNodes()[0].data.index
        this.brandList = res.data.data.list.brand_list
        if (res.data.data.list.class_list[0].length !== 0) {
          this.$refs.myCascader.getCheckedNodes()[0].data.children = []
          res.data.data.list.class_list[0].forEach((item, index) => {
            let obj = item
            this.$refs.myCascader.getCheckedNodes()[0].data.children.push({
              value: obj.cid,
              label: obj.pname,
              index: index,
              level: obj.level,
              children: []
            })
          })
        }
      })
    },
    // 加载所有分类
    async allClass (value) {
      for (let i = 0; i < value.length - 1; i++) {
        if (this.classIds.includes(value[i].value)) {
          choiceClass({
            api: 'admin.goods.choiceClass',
            classId: value[i].value
          }).then(res => {
            if (res.data.data.list.class_list.length !== 0) {
              this.brandList = res.data.data.list.brand_list
              res.data.data.list.class_list[0].forEach((item, index) => {
                let obj = item
                value[i].children.push({
                  value: obj.cid,
                  label: obj.pname,
                  index: index,
                  children: []
                })
              })

              this.allClass(value[i].children)
            }
          })
        } else {
          continue
        }
      }
    },
    handleSelectionChange (e) {
      this.tableList = e
    },

    submitForm (formName) {
      this.includesArr = []
      var arr = []
      this.attributeList.forEach(item => {
        arr.push(item.num)
      })
      arr.reduce((total, item, index) => {
        if (total[item]) {
          total[item].push(index)
          this.includesArr.push(index)
        } else {
          total[item] = [index]
        }
        return total
      }, {})
      try {
        this.attributeList.forEach((item, index) => {
          if (this.includesArr.includes(index) == true) {
            throw Error()
          }
        })
      } catch (error) {
        this.$message({
          showClose: true,
          message: this.$t('group.addGroup.ptlxcf'),
          type: 'error'
        })
        return
      }
      var storeList = []
      this.tableList.forEach(item => {
        storeList.push(item.id)
      })
      var goodsIds = storeList.join(',')
      console.log('goodsIds', goodsIds) 
      this.$refs[formName].validate(async valid => {
        if (valid) {
          try { 
            if (this.attributeList.some(row => row.num == '')) {
              this.$message({
                type: 'error',
                message: this.$t('group.addGroup.ptnrtxwz'),
                offset: 100,
              })
              
              return
            }
            if (this.attributeList.some(row => row.canDiscount == '')) {
              this.$message({
                type: 'error',
                message: this.$t('group.addGroup.ctzktxwz'),
                offset: 100,
              })
              return
            }
            
            if (this.attributeList.some(row => row.openDiscount == '')) {
              if (this.ruleForm.teamSwitch == 1) {
                this.$message({
                  type: 'error',
                  message: this.$t('group.addGroup.tzzktxwz'),
                  offset: 100,
                })
              } else {
                this.$message({
                  type: 'error',
                  message: this.$t('group.addGroup.tzyjtxwz'),
                  offset: 100,
                })
              }
              return
            }
            let data = {
              api: 'plugin.group.AdminGroup.AddGroupActivity',
              // mchId: getStorage('mchId'),
              id: this.$route.query.id ?? '',
              goodsIds: goodsIds, //商品集
              name: this.ruleForm.name, //活动名称
              againSwitch: this.ruleForm.againSwitch, //同一商品参团限制
              // showSwitch: this.ruleForm.showSwitch, //是否显示
              teamSwitch: this.ruleForm.teamSwitch, //是否显示
              startDate: this.ruleForm.date?.[0] ?? '',
              endDate: this.ruleForm.date?.[1] ?? '',
              groupRule: JSON.stringify(this.attributeList)
            }
            let { entries } = Object
            let formData = new FormData()
            for (let [key, value] of entries(data)) {
              formData.append(key, value)
            }
            addGroup(formData).then(res => {
              if (res.data.code == '200') {
                this.$message({
                  showClose: true,
                  message: this.$t('group.addGroup.tjcg'),
                  type: 'success',
                  offset: 100
                })
                this.$router.go(-1)
              }
            })
          } catch (error) { 
            this.$message({
              message: error.message,
              type: 'error',
              offset: 100
            })
            
          }
        } else {
          console.log('error submit!!')
          return false
        }
      })
    }
  }
}
