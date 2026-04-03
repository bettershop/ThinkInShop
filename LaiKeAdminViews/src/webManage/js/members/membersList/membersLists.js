import {
  getUserInfo,
  userRechargeMoney,
  delUserById,
  updateUserGradeById,
  goodsStatus,
  method,
} from "@/api/members/membersList";
import { getStorage } from "@/utils/storage";
import { exports } from "@/api/export/index";
import { mixinstest } from "@/mixins/index";
import ErrorImg from "@/assets/images/default_picture.png";
import { getItuList } from '@/api/members/membersSet'

export default {
  name: "membersLists",
  mixins: [mixinstest],
  data() {
    return {
      laikeCurrencySymbol:'￥',
      radio1: this.$t("membersLists.yhlb"),
      membersGrade: [], // 会员等级

      overdueList: [
        {
          value: "0",
          label: this.$t("membersLists.wgq"),
        },
        {
          value: "1",
          label: this.$t("membersLists.ygq"),
        },
      ], // 是否过期

      methodList: [],

      sourceList: [
        {
          value: "6",
          label: this.$t('fundsManagement.lbList6'),
        },
        {
          value: "11",
          label: this.$t('fundsManagement.lbList2'),
        },
        {
          value: "2",
          label: this.$t('fundsManagement.lbList8'),
        },
        {
          value: "1",
          label: this.$t('fundsManagement.lbList1'),
        },
      ], // 账号来源

      inputInfo: {
        // uid: null,
        uname: null,
        grade: null,
        isOverdue: null,
        source: null,
        tel: null,
      },

      tableData: [],
      loading: true,

      tag: "",

      // 弹框数据
      dialogVisible2: false,
      ruleForm: {
        balance: "",
        integral: "",
        level: "",
        effect_time: "1",
        remake: ""
      },
      rules: {
        balance: [
          {
            required: true,
            message: this.$t("membersLists.qsczje"),
            trigger: "blur",
          },
        ],
        integral: [
          {
            required: true,
            message: this.$t("membersLists.qsczjf"),
            trigger: "blur",
          },
        ],
        level: [
          {
            required: true,
            message: this.$t("membersLists.qxzhy"),
            trigger: "change",
          },
        ],
        remake: [
          {
            required: true,
            message: this.$t("membersLists.qsrbzxx"),
            trigger: "blur",
          },
        ]
      },

      id: null,
      userId: null,
      toggle: null,
      // table高度
      tableHeight: null,
      // 导出弹框数据
      dialogVisible: false,
      isClick: true,

      state:'',
      restaurants:[]
    };
  },

  created() {
    this.laikeCurrencySymbol = this.LaiKeCommon.getDefaultCurrencySymbol();
    if (this.$route.params.pageSize) {
      this.pagination.page = this.$route.params.dictionaryNum;
      this.dictionaryNum = this.$route.params.dictionaryNum;
      this.pageSize = this.$route.params.pageSize;
      this.inputInfo = this.$route.params.inputInfo;
    }
    this.getUserInfos();
    this.goodsStatus();
    this.methods();
  },

  mounted() {
    this.$nextTick(function () {
      this.getHeight();
    });
    window.addEventListener("resize", this.getHeight(), false);

    if(sessionStorage.getItem('restaurants')){
      this.restaurants =JSON.parse( sessionStorage.getItem('restaurants'))
    }else{
        this.queryAdd()
    }
  },

  methods: {
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
     // 异步查询建议列表的方法
    querySearchAsync(queryString, cb) {
      console.log(queryString,'queryString');
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
    // 创建过滤函数，根据 name、zh_name 和 code2 进行过滤
    createFilter(queryString) {
      return (country) => {
          const lowerCaseQuery = queryString.toLowerCase();
          console.log(lowerCaseQuery, 'lowerCaseQuery');
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

    // 图片错误处理
    handleErrorImg(e) {
      console.log("图片报错了", e.target.src);
      e.target.src = ErrorImg;
    },
    // 去订单列表页
    toOrder(value) {
      this.$router.push({
        path: "/order/orderList/orderLists",
        query: {
          user_id: value.user_id,
        },
      });
    },
    // 获取table高度
    getHeight() {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight;
      console.log(this.$refs.tableFather.clientHeight);
    },

    async getUserInfos() {
      const res = await getUserInfo({
        api: "admin.user.getUserInfo",
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        // uid: this.inputInfo.uid,
        key: this.inputInfo.uname,
        grade: this.inputInfo.grade,
        isOverdue: this.inputInfo.isOverdue,
        source: this.inputInfo.source,
        tel: this.inputInfo.tel,
        vague: 1 ,//是否模糊查询  1 是 / 0 否
        cpc:this.state, // 国家区号
        country_num : this.countryNum, // 国家代码
      });
      this.total = res.data.data.total;
      this.tableData = res.data.data.list;
      //因为账号来源用的是公共方法，那在公共方法中增加一个类型判断 12 为PC端；
      //所以此处增加数据处理，如果是PC开头的来源都改为PC端。
      this.tableData.forEach((item, index) => {
        if (item.source == 6 || item.source == 7 || item.source == 8 || item.source == 9) {
          this.tableData[index].source = 12
        }
      })
      this.loading = false;
      if (this.total < this.current_num) {
        this.current_num = this.total;
      }
      console.log(res);
    },

    async methods() {
      const res = await method({
        api: "admin.user.getUserGradeType",
      });
      console.log(res);
      this.methodList = res.data.data.gradeType;
    },

    // 获取会员等级列表
    async goodsStatus() {
      const res = await goodsStatus({
        api: "admin.user.goodsStatus",
      });

      console.log(res);
      let levelList = res.data.data.map((item) => {
        return {
          value: item.id,
          label: item.name,
        };
      });
      levelList.unshift({
        value: 0,
        label: "普通会员",
      });

      this.membersGrade = levelList;
    },

    // 重置
    reset() {
      this.inputInfo.uname = null;
      this.inputInfo.grade = null;
      this.inputInfo.isOverdue = null;
      this.inputInfo.source = null;
      this.inputInfo.tel = null;
      this.state = '';
    },

    // 查询
    demand() {
      //this.currpage = 1;
      //this.current_num = 10;
      this.showPagebox = false;
      this.loading = true;
      this.dictionaryNum = 1;
      this.getUserInfos().then(() => {
        this.loading = false;
        if (this.tableData.length > 5) {
          this.showPagebox = true;
        }
      });
    },

    // 查看
    View(value) {
      this.$router.push({
        path: "/members/membersList/viewMembers",
        query: {
          id: value.user_id,
          dictionaryNum: this.dictionaryNum,
          pageSize: this.pageSize,
          inputInfo: this.inputInfo,
        },
      });
    },
    // 编辑
    Edit(value) {
      this.$router.push({
        path: "/members/membersList/editorMembers",
        query: {
          id: value.user_id,
          dictionaryNum: this.dictionaryNum,
          pageSize: this.pageSize,
          inputInfo: this.inputInfo,
        },
      });
    },

    // 删除
    Delete(value) {
      this.$confirm(this.$t("membersLists.scqr"), this.$t("membersLists.ts"), {
        confirmButtonText: this.$t("membersLists.okk"),
        cancelButtonText: this.$t("membersLists.ccel"),
        type: "warning",
      })
        .then(() => {
          delUserById({
            api: "admin.user.delUserById",
            id: value.id,
          }).then((res) => {
            console.log(res);
            if (res.data.code == "200") {
              this.getUserInfos();
              this.$message({
                type: "success",
                message: this.$t("zdata.sccg"),
                offset: 100,
              });
            }
          });
        })
        .catch(() => {
          //   this.$message({
          //     type: 'info',
          //     message: '已取消删除',
          //     offset: 100
          //   });
        });
    },

    // 代客下单
    valetOrder(value) {
      this.$router.push({
        path: "/order/orderList/valetOrder",
        query: {
          id: value,
        },
      });
    },

    tags(value) {
      if (this.tag == value.user_id) {
        this.tag = "";
      } else {
        this.tag = value.user_id;
      }
    },

    addClass({ row, column, rowIndex, columnIndex }) {
      if (columnIndex == 14) {
        return "add";
      }
    },

    //选择一页多少条
    handleSizeChange(e) {
      this.loading = true;
      console.log(e);
      // this.current_num = e
      this.pageSize = e;
      this.getUserInfos().then(() => {
        this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1;
        this.current_num =
          this.tableData.length === this.pageSize
            ? this.dictionaryNum * this.pageSize
            : this.total;
        this.loading = false;
      });
    },

    //点击上一页，下一页
    handleCurrentChange(e) {
      this.loading = true;
      this.dictionaryNum = e;
      this.currpage = (e - 1) * this.pageSize + 1;
      this.getUserInfos().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total;
        this.loading = false;
      });
    },

    // 添加会员
    addMembers() {
      if (getStorage("laike_admin_userInfo").mchId !== 0) {
        this.$router.push({
          path: "/members/membersList/addMembers",
        });
      } else {
        this.$message({
          type: "error",
          message: "请添加店铺!",
          offset: 100,
        });
        this.$router.push("/mall/fastBoot/index");
      }
    },

    // 弹框方法
    dialogShow2(value, toggle) {
      this.isClick = true;
      console.log(value);
      this.dialogVisible2 = true;
      this.id = value.id;
      this.userId = value.user_id;
      this.toggle = toggle;
      this.ruleForm.remake = ''
      this.ruleForm.balance = "";
      this.ruleForm.integral = "";
      if (this.toggle == 3) {
        this.ruleForm.effect_time = "1";
        this.membersGrade.filter((item) => {
          if (item.label == value.grade) {
            this.ruleForm.level = item.value;
          }
        });
      }
    },

    handleClose2(done) {
      this.dialogVisible2 = false;
      this.id = null;
      this.userId = null;
      this.toggle = null;
      this.$refs["ruleForm"].clearValidate();
    },

    submitForm2(formName) {
      this.$refs[formName].validate(async (valid) => {
        console.log(this.ruleForm);
        if (valid) {
          try {
            if (this.isClick) {
              this.isClick = false;
              let that = this;
              setTimeout(function () {
                that.isClick = true;
              }, 300);
              if (this.toggle === 1) {
                userRechargeMoney({
                  api: "admin.user.userRechargeMoney",
                  id: this.id,
                  money: this.ruleForm.balance,
                  remake: this.ruleForm.remake,
                  type: 1,
                }).then((res) => {
                  console.log(res);
                  if (res.data.code == "200") {
                    this.dialogVisible2 = false;
                    this.getUserInfos();
                    this.$message({
                      type: "success",
                      message: this.$t("zdata.czcg"),
                      offset: 100,
                    });
                  }
                });
              } else if (this.toggle === 2) {
                this.isClick = false;
                userRechargeMoney({
                  api: "admin.user.userRechargeMoney",
                  id: this.id,
                  money: this.ruleForm.integral,
                  type: 3,
                }).then((res) => {
                  console.log(res);
                  if (res.data.code == "200") {
                    this.dialogVisible2 = false;
                    this.getUserInfos();
                    this.$message({
                      type: "success",
                      message: this.$t("zdata.czcg"),
                      offset: 100,
                    });
                  }
                });
              } else {
                this.isClick = false;
                updateUserGradeById({
                  api: "admin.user.updateUserGradeById",
                  userId: this.userId,
                  grade: this.ruleForm.level,
                  gradeType: this.ruleForm.effect_time,
                }).then((res) => {
                  console.log(res);
                  if (res.data.code == "200") {
                    this.dialogVisible2 = false;
                    this.getUserInfos();
                    this.$message({
                      type: "success",
                      message: this.$t("zdata.czcg"),
                      offset: 100,
                    });
                  }
                });
              }
            }
          } catch (error) {
            this.$message({
              message: error.message,
              type: "error",
              showClose: true,
            });
          }
        } else {
          console.log("error submit!!");
          return false;
        }
      });
    },

    // 账号来源
    getSource(value) {
      if (value == '1') {
        return '小程序'
      } else if (value == '11') {
        return 'APP端'
      } else if (value == '3') {
        return '支付宝小程序'
      } else if (value == '4') {
        return '头条小程序'
      } else if (value == '5') {
        return '百度小程序'
      } else if (value == "6" || value == "7" || value == "8") {
        return 'PC端'
      } else if (value == '2') {
        return 'H5移动端'
      } else {
        return '其它'
      }
    },

    // 导出弹框方法
    dialogShow() {
      this.dialogVisible = true;
    },

    handleClose(done) {
      this.dialogVisible = false;
    },

    async exportPage() {
      exports(
        {
          api: "admin.user.getUserInfo",
          pageNo: this.dictionaryNum,
          pageSize: this.pageSize,
          exportType: 1,
          // uid: this.inputInfo.uid,
          key: this.inputInfo.uname,
          grade: this.inputInfo.grade,
          isOverdue: this.inputInfo.isOverdue,
          source: this.inputInfo.source,
          tel: this.inputInfo.tel,
          vague: 1 //是否模糊查询  1 是 / 0 否

        },
        "memberslist"
      );
    },

    async exportAll() {
      exports(
        {
          api: "admin.user.getUserInfo",
          pageNo: 1,
          pageSize: 999999,
          exportType: 1,
          vague: 1 //是否模糊查询  1 是 / 0 否

          // uid: this.inputInfo.uid,
          // key: this.inputInfo.uname,
          // grade: this.inputInfo.grade,
          // isOverdue: this.inputInfo.isOverdue,
          // source: this.inputInfo.source,
          // tel: this.inputInfo.tel,
        },
        "memberslist"
      );
    },

    async exportQuery() {
      exports(
        {
          api: "admin.user.getUserInfo",
          pageNo: 1,
          pageSize: this.total,
          exportType: 1,
          // uid: this.inputInfo.uid,
          key: this.inputInfo.uname,
          grade: this.inputInfo.grade,
          isOverdue: this.inputInfo.isOverdue,
          source: this.inputInfo.source,
          tel: this.inputInfo.tel,
          vague: 1 //是否模糊查询  1 是 / 0 否
        },
        "memberslist"
      );
    },

    oninput2(num, limit) {
      var str = num;
      str = str.replace(/[^\.\d]/g, "");
      str = str.replace(".", "");
      str = str.replace(/[^\-?\d.]/g, "");
      return str;
    },
    oninput(num, limit) {
      var str = num;
      var len1 = str.substr(0, 1);
      var len2 = str.substr(1, 1);
      //如果第一位是0，第二位不是点，就用数字把点替换掉
      if (str.length > 1 && len1 == 0 && len2 != ".") {
        str = str.substr(1, 1);
      }
      //第一位不能是.
      if (len1 == ".") {
        str = "";
      }
      //限制只能输入一个小数点
      if (str.indexOf(".") != -1) {
        var str_ = str.substr(str.indexOf(".") + 1);
        if (str_.indexOf(".") != -1) {
          str = str.substr(0, str.indexOf(".") + str_.indexOf(".") + 1);
        }
      }
      //正则替换
      str = str.replace(/[^\d^\.]+/g, ""); // 保留数字和小数点
      if (limit / 1 === 1) {
        str = str.replace(/^\D*([0-9]\d*\.?\d{0,1})?.*$/, "$1"); // 小数点后只能输 1 位
      } else {
        str = str.replace(/^\D*([0-9]\d*\.?\d{0,2})?.*$/, "$1"); // 小数点后只能输 2 位
      }

      if (
        this.ruleForm.price_type == 1 &&
        this.totlePrice !== 0 &&
        parseInt(this.ruleForm.price) > this.totlePrice
      ) {
        str = this.totlePrice;
      }
      return str;
    },
  },
};
