import {
  getCouponCardInfo,
  addCoupon,
  getCouponConfigInfo,
  getAssignGoods,
  getAssignGoodsClass
} from '@/api/plug_ins/coupons'
import { goodsStatus } from '@/api/members/membersList'
import { getTime } from '@/utils/utils'
import treeTransfer from 'el-tree-transfer'
import request from '@/api/https'
export default {
  name: 'addCoupons',
  components: {
    treeTransfer
  },
  data () {
    return {
      forbiddenTime: {
        disabledDate (time) {
          return time.getTime() < Date.now() - 8.64e7
        }
      },
      ruleForm: {
        issuer: 0, //发行单位 0商城 1自营店
        issue_number_type: 1, //发行数量 1不限制 2设置数量 输入框参数是以前的issue_number
        consumption_threshold_type: 1, //消费门槛 1无门槛 2设置金额 consumption_threshold
        pickup_type: 0, //领取方式 0手动领取 1系统赠送
        date_type: 1, //过期时间  1过期时间  2设置指定过期时间  3设置领取后多久失效
        date_one: '', //设置指定过期时间input
        date_day: '', //设置领取后多久失效input
        limit_num: '', //使用限制input
        coupons_type: '',
        coupons_name: '',
        grade: '',
        type: 1,
        issue_number: '',
        issue_discount: '',
        face_value: '',
        consumption_threshold: '',
        available_range: 1,
        date: '',
        coupons_time: '',
        instructions: '',
        select_goods: [],
        limit_count: 1,
        coverMap: '', //优惠券图片
      },
      inputInfo: {
        cid: '',
        brandId: ''
      },
      search: '',
      prochangedata: [],
      prochangedata2: [],
      // prolist:[],
      prodata: [],
      currpage: 1,
      current_num: '',
      total: 0,

      pagination: {
        page: 1,
        pagesize: 10
      },
      pagesizes: [10, 25, 50, 100],
      classList: [],
      brandList: [],
      rules: {
        issuer: [
          {
            required: true,
            message: this.$t('coupons.addCoupons.qxzfxdw'),
            trigger: 'blur'
          }
        ],
        coupons_type: [
          {
            required: true,
            message: this.$t('coupons.addCoupons.qxzyhq'),
            trigger: 'change'
          }
        ],
        coupons_name: [
          {
            required: true,
            message: this.$t('coupons.addCoupons.qtxthq'),
            trigger: 'blur'
          }
        ],
        coverMap: [
          {
            required: true,
            message: this.$t('请添加优惠券图片'),
            trigger: 'blur'
          }
        ],
        grade: [
          {
            required: true,
            message: this.$t('coupons.addCoupons.qxzhy'),
            trigger: 'change'
          }
        ],
        issue_number: [
          {
            required: true,
            message: this.$t('coupons.addCoupons.qtxfx'),
            trigger: 'blur'
          }
        ],
        face_value: [
          {
            required: true,
            message: this.$t('coupons.addCoupons.qtxmz'),
            trigger: 'blur'
          }
        ],
        issue_discount: [
          {
            required: true,
            message: this.$t('coupons.addCoupons.qtxzk'),
            trigger: 'blur'
          }
        ],
        consumption_threshold: [
          {
            required: true,
            message: this.$t('coupons.addCoupons.qtxmk'),
            trigger: 'blur'
          }
        ],
        date: [
          {
            required: true,
            message: this.$t('coupons.addCoupons.qtxyx'),
            trigger: 'change'
          }
        ],
        coupons_time: [
          {
            required: true,
            message: this.$t('coupons.addCoupons.qtxyx'),
            trigger: 'change'
          }
        ]
      },

      defaultProps: {
        children: 'child',
        label: 'cname'
      },

      membersGrade: [], // 会员等级
      typeList: [
        {
          value: 1,
          name: this.$t('coupons.addCoupons.mj')
        },
        {
          value: 0,
          name: this.$t('coupons.addCoupons.zk')
        }
      ],

      couponsTypeList: [], // 优惠券类型

      availableRangeList: [
        {
          value: 1,
          name: this.$t('coupons.addCoupons.qbsp')
        },
        {
          value: 2,
          name: this.$t('coupons.addCoupons.zdsp')
        },
        {
          value: 3,
          name: this.$t('coupons.addCoupons.zdfl')
        },
        {
          value: 4,
          name: this.$t('coupons.addCoupons.czhy')
        }
      ],

      classIdList: [],

      goodsList: [], // 商品列表

      // 弹框数据
      dialogVisible: false,
      title: ['选择分类', '已选'],
      mode: 'transfer',
      fromData: [],
      toData: [],
      limit_type: 0,
      dialogVisible2: false,
      store_num: 0,
      is_status: 1,
      zy_coupon:1,
    }
  },

  created () {
    this.getAssignGoods()
    this.getAssignGoodsClass()
    this.goodsStatus()
    this.getCouponConfigInfos()
    this.getprolist()
    if (this.$route.query.id) {
      this.getCouponCardInfos()
    }
    this.choiceClasss()
  },
  computed: {
    showItem () {
      let showItem1 =
        (this.currpage - 1) * this.pagination.pagesize +
        this.pagination.pagesize
      if (showItem1 > this.total) {
        showItem1 = this.total
      }
      let showItem =
        (this.currpage - 1) * this.pagination.pagesize + 1 + '-' + showItem1
      return showItem
    }
  },
  watch: {
    toData: {
      handler: function () {
        this.classIdList = []
        this.getClassId(this.toData)
      }
    },
    'ruleForm.issuer':{
      handler: function (newValue,oldValue) {
         
          this.getprolist() 
         
      }
    },
    'ruleForm.coupons_type': {
      handler: function () {
        if (this.ruleForm.coupons_type !== 4) {
          this.ruleForm.type = null
        } else {
          this.ruleForm.type = 1
        }
      }
    }

    // 'ruleForm.limit_count':{
    //   handler(){
    //     if(this.ruleForm.limit_count < 1){
    //       this.ruleForm.limit_count = 1
    //       this.$message({
    //         message: '数量不能小于1',
    //         type: 'warning',
    //         offset:100
    //       })
    //     }
    //   }
    // },
  },

  methods: {
    getRowKey (row) {
      return row.id
    },
    issueChange () {
      if (this.ruleForm.issue_number_type == 2) {
        this.ruleForm.pickup_type = 0
      } else {
        this.ruleForm.issue_number = ''
      }
    },
    thresholdChange () {
      if (this.ruleForm.consumption_threshold_type == 1) {
        this.ruleForm.consumption_threshold = ''
      }
    },
    dateChange () {
      this.ruleForm.date_one = ''
      this.ruleForm.date_day = ''
    },
    // 获取商品类别
    async choiceClasss () {
      const res = await getCouponCardInfo({
        api: 'admin.goods.choiceClass'
      })
      res.data.data.list.class_list[0].forEach((item, index) => {
        let obj = item
        this.classList.push({
          value: obj.cid,
          label: obj.pname,
          index: index,
          children: []
        })
      })
    },
    // 根据商品类别id获取商品品牌
    changeProvinceCity (value) {
      this.inputInfo.brandId = []
      getCouponCardInfo({
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
              children: []
            })
          })
        }
      })
    },
    AddPro () {
      this.dialogVisible2 = true
      this.current_num = this.prodata.length
      this.currpage = 1
      this.pagination.pageSize = 10
      // this.$refs.multipleTable.clearSelection();
      console.log(this.prochangedata)
    },
    delPro () {
      this.getCouponCardInfos = []
      this.prochangedata = []
      this.$refs.multipleTable.clearSelection()
    },
    ChangeProdel (index) {
      this.prochangedata.splice(index, 1)
    },
    handleSelectionChange (e) {
      console.log(e)
      this.prochangedata2 = e
      this.store_num = e.length
      // this.handleCurrentChange2(1)
    },
    Reset () {
      this.search = ''
      this.inputInfo.cid = ''
      this.inputInfo.brandId = ''
    },
    query () {
      this.getprolist()
    },
    confirm () {
      if (this.prochangedata2.length == 0) {
        this.$message({
          message: this.$t('coupons.addCoupons.qxzsp'),
          type: 'error',
          offset: 100
        })
        return
      }
      this.prochangedata = [...this.prochangedata, ...this.prochangedata2]
      for (var i = 0; i < this.prochangedata.length; i++) {
        for (var j = i + 1; j < this.prochangedata.length; j++) {
          if (this.prochangedata[i].id === this.prochangedata[j].id) {
            this.prochangedata.splice(j, 1)
            j = j - 1
          }
        }
      }
      this.dialogVisible2 = false
      // this.$refs.multipleTable.clearSelection();
      // this.getprolist()
    },
    //选择一页多少条
    handleSizeChange (e) {
      this.pagination.pagesize = e
      // if (this.dialogVisible3) {
      this.getprolist()
      // } else {
      //     this.getUserList()
      // }
    },
    //点击上一页，下一页
    handleCurrentChange (e) {
      this.currpage = e
      // if (this.dialogVisible3) {
      this.getprolist()
      // } else {
      //     this.getUserList()
      // }
    },
    //点击上一页，下一页
    // handleCurrentChange2(e) {
    //   this.prolist=this.prochangedata.slice((e-1)*10,e*10)
    // },

    getprolist (type) {
      request({
        method: 'post',
        params: {
          api: 'plugin.coupon.Admincoupon.GetSpecifiedGoodsInfo',
          pageNo: this.currpage,
          pagesize: this.pagination.pagesize,
          productTitle: this.search,
          cid: this.inputInfo.cid[this.inputInfo.cid.length - 1],
          brandId: this.inputInfo.brandId,
          isZy:this.ruleForm.issuer == 1 ? 1:0
        }
      }).then(res => {
        console.log(res)
        this.total = Number(res.data.data.total)
        this.prodata = res.data.data.list
        this.current_num = this.prodata.length
        if (this.total < this.current_num) {
          this.current_num = this.total
        }
      })
    },
    async getCouponCardInfos () {
      const res = await getCouponCardInfo({
        api: 'plugin.coupon.Admincoupon.GetCouponCardInfo',
        mchId: 0,
        hid: this.$route.query.id
      })
      let couponsInfo = res.data.data.list[0]
      // for (const item of this.couponsTypeList) {
      //   if (item.label === couponsInfo.activity_type) {
      //     this.ruleForm.coupons_type = item.value
      //     break
      //   }
      // }

      if (couponsInfo.activity_type == '会员赠券') {
        this.ruleForm.coupons_type = 4
      } else if (couponsInfo.activity_type == '免邮券') {
        this.ruleForm.coupons_type = 1
      } else if (couponsInfo.activity_type == '满减券') {
        this.ruleForm.coupons_type = 2
      } else {
        this.ruleForm.coupons_type = 3
      }

      this.ruleForm.coupons_time = couponsInfo.day

      this.ruleForm.grade = couponsInfo.grade_id
      this.ruleForm.limit_count = couponsInfo.receive
      this.ruleForm.coupons_name = couponsInfo.name

      this.ruleForm.issue_number = couponsInfo.circulation
      this.ruleForm.face_value = couponsInfo.money
      this.ruleForm.issue_discount = couponsInfo.discount
      this.ruleForm.consumption_threshold = couponsInfo.z_money
      for (const item of this.availableRangeList) {
        if (item.name === couponsInfo.type) {
          this.ruleForm.available_range = item.value
          break
        }
      }
      if (couponsInfo.goodsIdList) {
        this.ruleForm.select_goods = couponsInfo.goodsIdList.map(item => {
          return item.id
        })
      }

      console.log(this.ruleForm.select_goods)
      if (couponsInfo.classIdList) {
        this.toData = couponsInfo.classIdList
        // this.toData = couponsInfo.classIdList.map(item => {
        //   return {
        //     child: [],
        //     cid: item.cid,
        //     cname: item.pname,
        //     level: item.level,
        //     sid: item.sid
        //   }
        // })
      }
      this.ruleForm.date = [
        getTime(couponsInfo.start_time),
        getTime(couponsInfo.end_time)
      ]
      this.ruleForm.instructions = couponsInfo.Instructions
      console.log(res)
      let myid = ''
      this.ruleForm.select_goods.forEach(element => {
        myid = myid + element + ','
      })

      request({
        method: 'post',
        params: {
          api: 'plugin.coupon.Admincoupon.GetSpecifiedGoodsInfo',
          // pageNo:this.currpage ,
          // pageSize:this.pagination.pagesize,
          id: myid
        }
      }).then(res => {
        console.log(res)
        this.prochangedata = res.data.data.list
        console.log(this.prochangedata)

        // this.handleCurrentChange2(1)
      })
      this.prochangedata.forEach(item => {
        this.$refs.multipleTable.toggleRowSelection(item)
      })
    },
    oninput (value) {
      var str = value
      if (Number(str) > 10) {
        str = 10
      }
      return str
    },
    exgNumber () {
      this.ruleForm.limit_count = Number(this.ruleForm.limit_count)
      if (this.ruleForm.limit_count != this.ruleForm.limit_count.toFixed(0)) {
        this.$message({
          message: this.$t('coupons.addCoupons.lqxzb'),
          type: 'warning',
          offset: 100
        })
      }
    },

    async getAssignGoods () {
      const res = await getAssignGoods({
        api: 'plugin.coupon.Admincoupon.GetAssignGoods',
        pageSize: 9999
      })
      console.log(res)
      this.goodsList = res.data.data.list
    },

    async getAssignGoodsClass () {
      const res = await getAssignGoodsClass({
        api: 'plugin.coupon.Admincoupon.GetAssignGoodsClass'
      })
      this.fromData = this.recursionNodes(res.data.data.list)
    },

    // 获取会员等级列表
    async goodsStatus () {
      const res = await goodsStatus({
        api: 'admin.user.goodsStatus'
      })

      console.log(res)
      let levelList = res.data.data.map(item => {
        return {
          value: item.id,
          label: item.name
        }
      })
      // levelList.unshift({
      //     value: 0,
      //     label: '普通会员'
      // })

      this.membersGrade = levelList
    },

    async getCouponConfigInfos () {
      const res = await getCouponConfigInfo({
        api: 'plugin.coupon.Admincoupon.GetCouponConfigInfo'
      })
      let obj = res.data.data.typeList
      this.is_status = res.data.data.data.is_status
      this.zy_coupon = res.data.data.data.zy_coupon
      this.limit_type = res.data.data.data.limit_type
      for (var i in obj) {
        this.couponsTypeList.push({
          value: parseInt(i),
          label: obj[i]
        })
      }
    },

    recursionNodes (childNodes) {
      const nodes = childNodes
      nodes.map((item, index) => {
        if (item.child && item.level > 1) {
          item.level = item.level + '-' + (index + 1)
        }
        if (item.child) {
          this.recursionNodes(item.child)
        }
      })
      return nodes
    },

    getClassId (childNodes) {
      const nodes = childNodes
      for (const item of nodes) {
        this.classIdList.push(item.cid)
        if (item.child) {
          this.getClassId(item.child)
        }
      }
    },

    // 切换模式 现有树形穿梭框模式transfer 和通讯录模式addressList
    changeMode () {
      if (this.mode == 'transfer') {
        this.mode = 'addressList'
      } else {
        this.mode = 'transfer'
      }
    },
    // 监听穿梭框组件添加
    add (fromData, toData, obj) {
      // 树形穿梭框模式transfer时，返回参数为左侧树移动后数据、右侧树移动后数据、移动的{keys,nodes,halfKeys,halfNodes}对象
      // 通讯录模式addressList时，返回参数为右侧收件人列表、右侧抄送人列表、右侧密送人列表
      console.log('fromData:', fromData)
      console.log('toData:', toData)
      console.log('obj:', obj)
    },
    // 监听穿梭框组件移除
    remove (fromData, toData, obj) {
      // 树形穿梭框模式transfer时，返回参数为左侧树移动后数据、右侧树移动后数据、移动的{keys,nodes,halfKeys,halfNodes}对象
      // 通讯录模式addressList时，返回参数为右侧收件人列表、右侧抄送人列表、右侧密送人列表
      console.log('fromData:', fromData)
      console.log('toData:', toData)
      console.log('obj:', obj)
    },

    // 弹框方法
    dialogShow () {
      this.dialogVisible = true
      // this.getAssignGoodsClass()
    },

    handleClose (done) {
      this.dialogVisible = false
      this.getAssignGoodsClass()
    },

    remove2 (node, data) {
      const parent = node.parent
      const children = parent.data.child || parent.data
      const index = children.findIndex(d => d.cid === data.cid)
      children.splice(index, 1)

      for (var i = 0; i < this.toData.length; i++) {
        if (this.toData[i].child.length == 0) {
          this.toData.splice(i, 1)
        }
      }
    },

    submitForm (formName) {
      // this.ruleForm.limit_count = Number(this.ruleForm.limit_count)
      // if(this.ruleForm.limit_count != this.ruleForm.limit_count.toFixed(0)){
      //   return this.ruleForm.limit_count = this.ruleForm.limit_count.toFixed(0)
      // }
      this.$refs[formName].validate(async valid => {
        if (valid) {
          try {
            if (
              this.ruleForm.available_range == 4 &&
              this.ruleForm.coupons_type == 1
            ) {
              this.$message({
                message: this.$t('coupons.addCoupons.czhyb'),
                type: 'error',
                offset: 100
              })
              return
            }
            if (this.ruleForm.issue_number_type == 2) {
              if (
                Number(this.ruleForm.issue_number) <
                Number(this.ruleForm.limit_count)
              ) {
                this.$message({
                  message: this.$t('coupons.addCoupons.fxslb'),
                  type: 'error',
                  offset: 100
                })
                return
              }
            }
            if (this.ruleForm.limit_count == '') {
              this.$message({
                message: this.$t('coupons.addCoupons.qtxxl'),
                type: 'error',
                offset: 100
              })
              return
            }
            let mylist = ''
            this.prochangedata.forEach(element => {
              console.log(element)
              mylist = mylist + element.id + ','
            })
            mylist = mylist.substring(0, mylist.lastIndexOf(','))
            if (this.ruleForm.issue_number_type == 2) {
              if (this.ruleForm.coupons_type !== 4) {
                if (this.ruleForm.issue_number == 0) {
                  this.$message({
                    message: this.$t('coupons.addCoupons.fxslbx'),
                    type: 'error',
                    offset: 100
                  })
                  return
                }
              }
            }
            addCoupon({
              api: 'plugin.coupon.Admincoupon.AddCoupon',
              mchId: 0,
              id: this.$route.query.id ? this.$route.query.id : '',
              activityType: this.ruleForm.coupons_type,
              name: this.ruleForm.coupons_name,
              circulation: this.ruleForm.issue_number,
              money:
                this.ruleForm.coupons_type == 2 ? this.ruleForm.face_value : '',
              discount:
                this.ruleForm.coupons_type == 3
                  ? this.ruleForm.issue_discount
                  : '',
              zmoney: this.ruleForm.consumption_threshold,
              type: this.ruleForm.available_range,
              menuList: this.ruleForm.available_range == 2 ? mylist : '',
              classList:
                this.ruleForm.available_range == 3
                  ? this.classIdList.join(',')
                  : '',
              // startTime: this.ruleForm.date[0],
              // endTime: this.ruleForm.date[1],
              instructions: this.ruleForm.instructions,
              limitCount: this.ruleForm.limit_count,
              day: this.ruleForm.date_day,
              issueUnit: this.ruleForm.issuer,
              receiveType: this.ruleForm.pickup_type,
              end_time: this.ruleForm.date_one,
              cover_map: this.ruleForm.coverMap,
            }).then(res => {
              if (res.data.code == '200') {
                this.$message({
                  message: this.$t('coupons.tjcg'),
                  type: 'success',
                  offset: 102
                })
                this.$router.go(-1)
              }
            })
            // }
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
