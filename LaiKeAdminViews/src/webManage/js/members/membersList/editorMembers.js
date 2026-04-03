import {
  getUserInfo,
  updateUserById,
  goodsStatus,
  getItuList
} from '@/api/members/membersList'

import { getTime } from '@/utils/utils'
export default {
  name: 'editorMembers',

  data () {
    return {
      pickerOptionsStart: {
        // 时间不能大于当前时间
        disabledDate: time => {
          return time.getTime() > Date.now()
        }
      },
      laikeCurrencySymbol:'￥',
      membersGrade: [], // 会员等级
      state:'',
      userInfo: null,
      countryNum:'',
      restaurants:[],

      ruleForm: {
        headimgurl: '',
        user_name: '',
        grade: '',
        mobile: '',
        loginPwd: '',
        paypwd: '',
        money: '',
        score: '',
        birthday: '',
        sex: 0,
        mailbox:''
      },

      rule: {
        user_name: [
          { required: true, message: this.$t('editorMembers.qsryhmc'), trigger: 'blur' }
        ],
        mobile: [
          { required: true, message: this.$t('editorMembers.qsrsjhm'), trigger: 'blur' }
        ],
        loginPwd: [
          { required: true, message: this.$t('editorMembers.qsrdlmm'), trigger: 'blur' }
        ],
        mailbox: [
          { required: true, message: this.$t('merchants.addmerchants.qsryx'), trigger: 'blur' },
          {
            validator: (rule, value, callback) => {
              const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
              if (value && !emailRegex.test(value)) {
                callback(new Error(this.$t('merchants.addmerchants.yxgscw')));
              } else {
                callback();
              }
            },
            trigger: 'blur'
          }
        ],
      },

      sexList: [
        {
          value: 1,
          label: '男'
        },
        {
          value: 2,
          label: '女'
        },
        {
          value: 0,
          label: '未设置'
        },
      ],

      changePwd: false,
      changePayPwd: false,

      flag: false,
      flagType:'password',
    }
  },

  created () {
    this.laikeCurrencySymbol = this.LaiKeCommon.getDefaultCurrencySymbol();
    this.getUserInfos()
    this.goodsStatus()
  },

  beforeRouteLeave (to, from, next) {
    if (to.name == 'membersLists') {
      to.params.dictionaryNum = this.$route.query.dictionaryNum
      to.params.pageSize = this.$route.query.pageSize
      to.params.inputInfo = this.$route.query.inputInfo
    }
    next()
  },
  mounted(){
    if(sessionStorage.getItem('restaurants')){
        this.restaurants =JSON.parse( sessionStorage.getItem('restaurants'))

    }else{
        this.queryAdd()
    }
},
  watch: {
    'ruleForm.loginPwd': {
      handler: function () {
        if (this.ruleForm.loginPwd !== '******') {
          this.changePwd = true
        }
      }
    },

    'ruleForm.paypwd': {
      handler: function () {
        if (this.ruleForm.paypwd !== '******') {
          this.changePayPwd = true
        }
      }
    },
  },

  methods: {
    // 异步查询建议列表的方法
      querySearchAsync(queryString, cb) {
          // 模拟异步请求
          setTimeout(() => {
          const results = queryString
              ? this.restaurants.filter(this.createFilter(queryString))
              : this.restaurants;
          // 调用回调函数，将查询结果传递给组件
          cb(results);
          }, 300);
      },
      // 创建过滤函数，根据 name 和 zh_name 进行过滤
      createFilter(queryString) {
        return (country) => {
            const lowerCaseQuery = queryString.toLowerCase();
            return (
                country.name.toLowerCase().indexOf(lowerCaseQuery) > -1 ||
                country.code2.toLowerCase().indexOf(lowerCaseQuery) > -1 ||
                country.zh_name.toLowerCase().indexOf(lowerCaseQuery) > -1
            );
        };
      },
     // 选择建议项时触发的方法
     handleSelect(item) {
      console.log('选中的项:', item);
      this.state = item.code2; // 可以根据需求更新输入框显示的值
      this.countryNum = item.num3;
    },
    //小眼睛切换状态
    getFlag(){
      this.flag = !this.flag
      this.flagType = this.flag ? 'text' : 'password'//text为显示密码；password为隐藏密码
    },
    async getUserInfos () {
      const res = await getUserInfo({
        api: 'admin.user.getUserInfo',
        uid: this.$route.query.id
      })
      this.userInfo = res.data.data.list[0]
      //因为账号来源用的是公共方法，那在公共方法中增加一个类型判断 12 为PC端；
      //所以此处增加数据处理，如果是PC开头的来源都改为PC端。
      if(this.userInfo.source == 6 || this.userInfo.source == 7 || this.userInfo.source == 8 || this.userInfo.source == 9){
        this.userInfo.source = 12
      }
      this.ruleForm.headimgurl = res.data.data.list[0].headimgurl
      this.ruleForm.user_name = res.data.data.list[0].user_name
      this.ruleForm.grade = res.data.data.list[0].grade
      this.ruleForm.mobile = res.data.data.list[0].mobile
      this.ruleForm.loginPwd =
        res.data.data.list[0].loginPwd === false ? '' : '******'
      this.ruleForm.paypwd =
        res.data.data.list[0].isPaymentPwd === false ? '' : '******'
      this.ruleForm.money = res.data.data.list[0].money
      this.ruleForm.score = res.data.data.list[0].score
      this.ruleForm.birthday = res.data.data.list[0].birthday
      this.ruleForm.sex = res.data.data.list[0].sex
       if(this.ruleForm.sex){
        this.sexList = [
          {
            value: 1,
            label: '男'
          },
          {
            value: 2,
            label: '女'
          },
        ]

       }
      this.ruleForm.mailbox = res.data.data.list[0].e_mail // 邮箱
      this.state = res.data.data.list[0].cpc //国家区号
      this.countryNum = res.data.data.list[0].country_num //国家代码


      console.log(this.userInfo)
    },

    // 获取会员等级下拉
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
      levelList.unshift({
        value: 0,
        label: '普通会员'
      })

      this.membersGrade = levelList
    },

    queryAdd(){
      const data ={
          api:'admin.user.getItuList',
          keyword:this.keyword
      }
      getItuList(data).then(res=>{
        if(res.data.code == 200){
            this.restaurants = res.data.data
            sessionStorage.setItem('restaurants',JSON.stringify(this.restaurants))


        }
      })

    },
    getGrade (value) {
      if (typeof value !== 'number') {
        if (value == '普通会员') {
          return 0
        } else if (value == '钻石会员') {
          return 30
        } else if (value == '黄金会员') {
          return 25
        } else if (value == '白银会员') {
          return 24
        } else {
          return 34
        }
      } else {
        return value
      }
    },

    // 修改会员信息
    submitForm (formName) {
      this.$refs[formName].validate(async valid => {
        console.log(this.ruleForm)
        if (valid) {
          try {
            if (this.ruleForm.user_name.length > 16) {
              this.$message({
                message: '用户昵称不能大于16个字符',
                type: 'error',
                offset: 100
              })
              return
            }
            if(!this.state || this.state.length==0){
              this.$message({
                  message: '请选择区号',
                  type: 'error',
                  offset: 100
              })
              return
          }
          let isCpc = this.restaurants.some((item) => item.num3 == this.countryNum) 
          if (!isCpc) {
            this.$message({
              message: '区号不正确！',
              type: 'error',
              offset: 102
            })
            return
          }
            updateUserById({
              api: 'admin.user.updateUserById',
              userId: this.$route.query.id,
              uname: this.ruleForm.user_name,
              headerUrl: this.ruleForm.headimgurl,
              grade: this.getGrade(this.ruleForm.grade),
              phone: this.ruleForm.mobile,
              pwd: this.changePwd ? this.ruleForm.loginPwd : null,
              paypwd: this.changePayPwd ? this.ruleForm.paypwd : null,
              money: this.ruleForm.money,
              jifen: this.ruleForm.score,
              sex: this.ruleForm.sex,

              cpc: this.state, // 国家区号
              country_num: this.countryNum, // 国家代码
              e_mail: this.ruleForm.mailbox,// 邮箱

              birthday: this.ruleForm.birthday
                ? getTime(this.ruleForm.birthday)
                : null
            }).then(res => {
              console.log(res)
              if (res.data.code == '200') {
                this.$message({
                  message: this.$t('编辑成功'),
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
              showClose: true
            })
          }
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },

    oninput2 (num) {
      var str = num
      str = str.replace(/[^\.\d]/g, '')
      str = str.replace('.', '')

      return str
    },

    oninput (num, limit) {
      var str = num
      var len1 = str.substr(0, 1)
      var len2 = str.substr(1, 1)
      //如果第一位是0，第二位不是点，就用数字把点替换掉
      if (str.length > 1 && len1 == 0 && len2 != '.') {
        str = str.substr(1, 1)
      }
      //第一位不能是.
      if (len1 == '.') {
        str = ''
      }
      //限制只能输入一个小数点
      if (str.indexOf('.') != -1) {
        var str_ = str.substr(str.indexOf('.') + 1)
        if (str_.indexOf('.') != -1) {
          str = str.substr(0, str.indexOf('.') + str_.indexOf('.') + 1)
        }
      }
      //正则替换
      str = str.replace(/[^\d^\.]+/g, '') // 保留数字和小数点
      if (limit / 1 === 1) {
        str = str.replace(/^\D*([0-9]\d*\.?\d{0,1})?.*$/, '$1') // 小数点后只能输 1 位
      } else {
        str = str.replace(/^\D*([0-9]\d*\.?\d{0,2})?.*$/, '$1') // 小数点后只能输 2 位
      }

      if (
        this.ruleForm.price_type == 1 &&
        this.totlePrice !== 0 &&
        parseInt(this.ruleForm.price) > this.totlePrice
      ) {
        str = this.totlePrice
      }
      return str
    }
  }
}
