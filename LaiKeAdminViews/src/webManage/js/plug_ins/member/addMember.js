import { getUser, addMember } from "@/api/plug_ins/members";
import { mixinstest } from "@/mixins/index";
import pageData from "@/api/constant/page";

export default {
  name: "addMember",
  mixins: [mixinstest],

  data() {
    return {
      page: pageData.data(),
      forbiddenTime: {
        disabledDate(time) {
          return time.getTime() < Date.now() - 8.64e7;
        },
      },
      ruleForm: {
        date: "",
        methods: "",
      },

      rules: {
        date: [
          {
            required: true,
            message: this.$t("member.addMember.qxzdqsj"),
            trigger: "change",
          },
        ],
        methods: [
          {
            required: true,
            message: this.$t("member.addMember.qxzktfs"),
            trigger: "change",
          },
        ],
      },

      classList: [],
      brandList: [],
      inputInfo: {
        userName: "",
        userId: "",
        phone: "",
      },

      sequenceList: [
        {
          value: "1",
          label: this.$t("member.addMember.yk"),
        },
        {
          value: "2",
          label: this.$t("member.addMember.jk"),
        },
        {
          value: "3",
          label: this.$t("member.addMember.nk"),
        },
      ], // 开通方式

      id: null,

      tableData: [],

      tableRadio: "",
      totlePrice: null,
      stock: null,
    };
  },

  created() {
    this.getUser();
  },

  watch: {
    //监听开通会员方式 1.月卡 2.季卡 3.年卡
    "ruleForm.methods": function (e) {
      //获取当前 年 月 日
      let data = new Date();
      let y = data.getFullYear();
      let m = data.getMonth() + 1;
      let d = data.getDate();
      console.log("当前时间", y + "-" + m + "-" + d);
      //判断会员方式 并赋值给 到期时间
      if (e == 1) {
        m += 1;
        this.ruleForm.date = y + "-" + m + "-" + d;
      } else if (e == 2) {
        m += 3;
        this.ruleForm.date = y + "-" + m + "-" + d;
      } else {
        y += 1;
        this.ruleForm.date = y + "-" + m + "-" + d;
      }
      console.log("会员到期时间", y + "-" + m + "-" + d);
    },
    //监听会员到期时间  案例：2022-11-02
    "ruleForm.date": function (e) {
      console.log(e);
    },
  },

  methods: {
    async getUser() {
      const res = await getUser({
        api: "plugin.member.AdminMember.GetUser",
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        userId: this.inputInfo.userId,
        userName: this.inputInfo.userName,
        phone: this.inputInfo.phone,
      });
      console.log(res);
      this.current_num = 10;
      this.total = res.data.data.total;
      this.tableData = res.data.data.list;
      if (this.total < this.current_num) {
        this.current_num = this.total;
      }
      if (this.total == 0) {
        this.showPagebox = false;
      } else {
        this.showPagebox = true;
      }
    },

    reset() {
      (this.inputInfo.userName = ""),
        (this.inputInfo.userId = ""),
        (this.inputInfo.phone = "");
    },

    demand() {
      this.currpage = 1;
      this.current_num = 10;
      this.dictionaryNum = 1;
      this.pageSize = 10;
      this.page.loading = true;
      this.showPagebox = false

      this.getUser().then(() => {
        this.page.loading = false;
        // this.showPagebox = false
        this.current_num =
          this.tableData.length === this.pageSize
            ? this.dictionaryNum * this.pageSize
            : this.total;
      });
    },

    handleSelectionChange(e) {
      console.log(e);
      this.id = e.user_id;
      console.log(this.id);
    },

    submitForm(formName) {
      this.$refs[formName].validate(async (valid) => {
        console.log(this.ruleForm);
        if (valid) {
          try {
            addMember({
              api: "plugin.member.AdminMember.AddMember",
              userIds: this.id,
              method: this.ruleForm.methods,
              overTime: this.ruleForm.date,
            }).then((res) => {
              if (res.data.code == "200") {
                this.$message({
                  message: this.$t("member.cg"),
                  type: "success",
                  offset: 100,
                });
                this.$router.go(-1);
              }
            });
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

    //选择一页多少条
    handleSizeChange(e) {
      this.pageSize = e;
      this.getUser().then(() => {
        this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1;
        this.current_num =
          this.tableData.length === this.pageSize
            ? this.dictionaryNum * this.pageSize
            : this.total;
      });
    },

    //点击上一页，下一页
    handleCurrentChange(e) {
      this.dictionaryNum = e;
      this.currpage = (e - 1) * this.pageSize + 1;
      this.getUser().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total;
      });
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

      if (this.totlePrice && parseInt(this.ruleForm.yuan) > this.totlePrice) {
        str = this.totlePrice;
      }
      return str;
    },

    oninput2(num) {
      var str = num;
      str = str.replace(/[^\.\d]/g, "");
      str = str.replace(".", "");

      return str;
    },
  },
};
