import {
  getCouponCardInfo,
  getAssignGoods,
  getAssignGoodsClass,
  addCoupon,
  allCoupons
} from '@/api/plug_ins/coupons'
import { goodsStatus } from '@/api/members/membersList'
import { getTime } from '@/utils/utils'
import treeTransfer from 'el-tree-transfer'
export default {
  name: 'editorCoupons',

  components: {
    treeTransfer
  },
  data() {
    return {
      ruleForm: {
        issuer: 0, //发行单位
        issue_number_type: 1, //发行数量
        consumption_threshold_type: 1, //消费门槛
        pickup_type: 0, //领取方式
        date_type: 1, //过期时间
        date_one: '', //设置指定过期时间
        date_num: '', //设置领取后多久失效
        limit_num: '', //使用限制
        coupons_type: '',
        coupons_name: '',
        coverMap: '',
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
        limit_count: 0,
        receive: ''
      },

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

      couponsTypeList: [], // 优惠券类型

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
      flag: true
    }
  },

  created() {
    console.log('进入了编辑页面')

    this.getCoupons()
    this.goodsStatus()
    this.getAssignGoods()
    this.getAssignGoodsClass()
    this.getCouponCardInfos()
    this.getCouponConfigInfo()
  },

  watch: {
    toData: {
      handler: function () {
        console.log(123456789)
        this.classIdList = []
        this.getClassId(this.toData)
        console.log(this.classIdList)
      },
      deep: true
    }
  },

  beforeRouteLeave(to, from, next) {
    if (to.name == 'couponsList') {
      to.params.dictionaryNum = this.$route.query.dictionaryNum
      to.params.pageSize = this.$route.query.pageSize
    }
    next()
  },

  methods: {
    issueChange() {
      if (this.ruleForm.issue_number_type == 2) {
        this.ruleForm.pickup_type = 1
      }
      this.ruleForm.issue_number = ''
    },
    //获取所有优惠券
    getCoupons() {
      allCoupons({
        api: 'plugin.coupon.Admincoupon.GetCouponTypeList'
      }).then(res => {
        this.couponsTypeList = res.data.data.typeList
      })
    },
    oninput(value) {
      var str = value
      if (Number(str) > 10) {
        str = 10
      }
      return str
    },
    async getCouponCardInfos() {
      const res = await getCouponCardInfo({
        api: 'plugin.coupon.Admincoupon.GetCouponCardInfo',
        mchId: 0,
        hid: this.$route.query.id
      })
      let couponsInfo = res.data.data.list[0]
      console.log('couponsInfo', couponsInfo)

      // for (const item of this.couponsTypeList) {
      //   if (item.label === couponsInfo.activity_type) {
      //     this.ruleForm.coupons_type = item.value
      //     break
      //   }
      // }

      // if(couponsInfo.activity_type == '会员赠券') {
      //   this.ruleForm.coupons_type = 4
      // } else if(couponsInfo.activity_type == '免邮券') {
      //   this.ruleForm.coupons_type = 1
      // }else if(couponsInfo.activity_type == '满减券') {
      //   this.ruleForm.coupons_type = 2
      // } else {
      //   this.ruleForm.coupons_type = 3
      // }
      this.ruleForm.pickup_type = couponsInfo.receive_type
      this.prochangedata = couponsInfo.goodsIdList
      this.ruleForm.issuer = couponsInfo.issuer
      this.ruleForm.date_type = couponsInfo.date_type
      this.ruleForm.date_one = couponsInfo.end_time
      this.ruleForm.date_num = couponsInfo.day ?? ''
      this.ruleForm.limit_num = couponsInfo.receive
      this.ruleForm.issue_number_type = couponsInfo.issue_number_type
      this.ruleForm.date_day = couponsInfo.day

      this.ruleForm.consumption_threshold_type =
        couponsInfo.consumption_threshold_type
      this.ruleForm.coupons_type = couponsInfo.couponType.toString()
      this.ruleForm.coupons_time = couponsInfo.day

      this.ruleForm.grade = couponsInfo.grade_id
      this.ruleForm.limit_count = couponsInfo.receive
      this.ruleForm.coupons_name = couponsInfo.name
      this.ruleForm.coverMap = couponsInfo.cover_map
      this.ruleForm.issue_number =
        couponsInfo.circulation == 9999999 ? '' : couponsInfo.circulation
      this.ruleForm.face_value = couponsInfo.money
      this.ruleForm.issue_discount = couponsInfo.discount
      this.ruleForm.consumption_threshold = couponsInfo.z_money
      this.ruleForm.receive = couponsInfo.receive
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
    },

    // 获取会员等级列表
    async goodsStatus() {
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

      this.membersGrade = levelList
    },

    async getAssignGoods() {
      const res = await getAssignGoods({
        api: 'plugin.coupon.Admincoupon.GetAssignGoods',
        pageSize: 9999
      })
      this.goodsList = res.data.data.list
    },

    async getCouponConfigInfo() {
      const res = await getAssignGoods({
        api: 'plugin.coupon.Admincoupon.GetCouponConfigInfo'
      })
      this.limit_type = res.data.data.data.limit_type
    },

    async getAssignGoodsClass() {
      const res = await getAssignGoodsClass({
        api: 'plugin.coupon.Admincoupon.GetAssignGoodsClass'
      })
      this.fromData = this.recursionNodes(res.data.data.list)
    },

    recursionNodes(childNodes) {
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

    getClassId(childNodes) {
      const nodes = childNodes
      for (const item of nodes) {
        console.log(item.cid)
        this.classIdList.push(item.cid)
        if (item.child && item.child.length > 0) {
          this.getClassId(item.child)
        }
      }
    },

    exgNumber() {
      this.ruleForm.limit_count = Number(this.ruleForm.limit_count)
      if (this.ruleForm.limit_count != this.ruleForm.limit_count.toFixed(0)) {
        this.$message({
          message: this.$t('coupons.addCoupons.lqxzb'),
          type: 'warning',
          offset: 100
        })
      }
    },

    // 切换模式 现有树形穿梭框模式transfer 和通讯录模式addressList
    changeMode() {
      if (this.mode == 'transfer') {
        this.mode = 'addressList'
      } else {
        this.mode = 'transfer'
      }
    },
    // 监听穿梭框组件添加
    add(fromData, toData, obj) {
      // 树形穿梭框模式transfer时，返回参数为左侧树移动后数据、右侧树移动后数据、移动的{keys,nodes,halfKeys,halfNodes}对象
      // 通讯录模式addressList时，返回参数为右侧收件人列表、右侧抄送人列表、右侧密送人列表
      console.log('fromData:', fromData)
      console.log('toData:', toData)
      console.log('obj:', obj)
    },
    // 监听穿梭框组件移除
    remove(fromData, toData, obj) {
      // 树形穿梭框模式transfer时，返回参数为左侧树移动后数据、右侧树移动后数据、移动的{keys,nodes,halfKeys,halfNodes}对象
      // 通讯录模式addressList时，返回参数为右侧收件人列表、右侧抄送人列表、右侧密送人列表
      console.log('fromData:', fromData)
      console.log('toData:', toData)
      console.log('obj:', obj)
    },

    // 弹框方法
    dialogShow() {
      this.dialogVisible = true
    },

    handleClose(done) {
      this.dialogVisible = false
      this.getAssignGoodsClass()
    },

    remove(node, data) {
      const parent = node.parent
      const children = parent.data.child || parent.data
      const index = children.findIndex(d => d.cid == data.cid)
      children.splice(index, 1)
    },

    submitForm(formName) {
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
                message: '会员不能使用免邮劵',
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
                  message: '发行数量不能小于限领数量',
                  type: 'error',
                  offset: 100
                })
                return
              }
            }
            if (this.ruleForm.limit_count == '') {
              this.$message({
                message: '请填写限领数量',
                type: 'error',
                offset: 100
              })
              return
            }
            let mylist = ''
            if (this.prochangedata != undefined) {
              this.prochangedata.forEach(element => {
                console.log(element)
                mylist = mylist + element.id + ','
              })

              mylist = mylist.substring(0, mylist.lastIndexOf(','))
            }
            if (this.ruleForm.issue_number_type == 2) {
              if (this.ruleForm.coupons_type !== 4) {
                if (this.ruleForm.issue_number == 0) {
                  this.$message({
                    message: '发行数量必须大于零',
                    type: 'error',
                    offset: 100
                  })
                  return
                }
              }
            }
            if (!this.flag) { return }

            this.flag = false

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
                  message: this.$t('zdata.bjcg'),
                  type: 'success',
                  offset: 102
                })
                this.$router.go(-1)
              }
            }).finally(() => {
              setTimeout(() => {
                this.flag = true
              }, 1500)
            })
            // }
          } catch (error) {
            setTimeout(() => {
              this.flag = true
            }, 1500)
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
