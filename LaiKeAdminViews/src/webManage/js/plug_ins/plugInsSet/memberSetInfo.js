import {
  getMemberConfig,
  addOrUpdate,
  updateMember,
} from "@/api/plug_ins/members";
import { getStorage, setStorage } from "@/utils/storage";
import Config from "@/packages/apis/Config";
import $ from "jquery";
export default {
  name: "editorMenber",

  data() {
    return {
      radio1: "2",

      ruleForm: {
        isOpen: 1,
        open_config: [
          {
            day: "",
            openMethod: this.$t("member.memberSet.yk"),
            openMethodName: "",
            price: "",
            priceForDay: "",
            points: "",
          },
          {
            day: "",
            openMethod: this.$t("member.memberSet.jk"),
            openMethodName: "",
            price: "",
            priceForDay: "",
            points: "",
          },
          {
            day: "",
            openMethod: this.$t("member.memberSet.nk"),
            openMethodName: "",
            price: "",
            priceForDay: "",
            points: "",
          },
        ],
        birthdayOpen: 1,
        pointsMultiple: "",
        bonusPointsOpen: "",
        bonusPointsConfig: [
          {
            openMethod: this.$t("member.memberSet.yk"),
            points: "",
          },
          {
            openMethod: this.$t("member.memberSet.jk"),
            points: "",
          },
          {
            openMethod: this.$t("member.memberSet.nk"),
            points: "",
          },
        ],
        memberDiscount: "",
        renewOpen: 1,
        renewDay: "",
        memberEquity: [],
      },

      rules: {
        memberDiscount: [
          {
            required: true,
            message: this.$t("member.memberSet.qtxhy"),
            trigger: "blur",
          },
        ],
      },

      sequenceList: [
        {
          value: "1",
          label: this.$t("member.memberSet.yk"),
        },
        {
          value: "2",
          label: this.$t("member.memberSet.jk"),
        },
        {
          value: "3",
          label: this.$t("member.memberSet.nk"),
        },
      ],

      equity: {
        equityName: "", //权益名称
        englishName: "", //英文名称
        icon: "", //图标
      },
      flag: true
    };
  },

  created() {
    this.getBase();
    this.getMemberConfig();
  },
  beforeRouteLeave(to, from, next) {
    if (
      JSON.stringify(this.ruleForm) == sessionStorage.getItem("ruleForm_member")
    ) {
      next();
    } else {
      console.log("表单变化，询问是否保存");
      next(false);
      this.$confirm(
        this.$t("coupons.couponsSet.sjygx"),
        this.$t("coupons.ts"),
        {
          distinguishCancelAndClose: true,
          confirmButtonText: this.$t("coupons.okk"),
          cancelButtonText: this.$t("coupons.ccel"),
          type: "warning",
        }
      )
        .then(() => {
          // this.submitForm();
          next();
        })
        .catch(() => {
          // next();
          // next('/plug_ins/plugInsSet/plugInsList')
        });
    }
  },

  mounted() {
    this.$nextTick(function () {
      this.getHeight();
    });
    window.addEventListener("resize", this.getHeight(), false);
  },

  methods: {
    back() {
      this.$router.push({
        path: "/plug_ins/plugInsSet/plugInsList",
      });
    },
    getBase() {
      this.goodsEditorBase = Config.baseUrl;
    },
    async getMemberConfig() {
      const res = await getMemberConfig({
        api: "plugin.member.AdminMember.getMemberConfig",
      });
      console.log(res);
      const Config = res.data.data;
      if (!Config) {
        sessionStorage.setItem(
          "ruleForm_member",
          JSON.stringify(this.ruleForm)
        );
        return;
      }
      this.ruleForm.isOpen = Config.is_open;
      if (Config.open_config && Config.open_config.length) {
        this.ruleForm.open_config = Config.open_config;
      }
      this.ruleForm.birthdayOpen = Config.birthday_open;
      this.ruleForm.pointsMultiple = Config.points_multiple;
      this.ruleForm.bonusPointsOpen = Config.bonus_points_open;
      if (Config.bonus_points_config && Config.bonus_points_config.length) {
        this.ruleForm.bonusPointsConfig = Config.bonus_points_config;
      }
      this.ruleForm.memberDiscount = Config.member_discount;
      this.ruleForm.renewOpen = Config.renew_open;
      this.ruleForm.renewDay = Config.renew_day;
      this.ruleForm.memberEquity = Config.member_equity == "" ? [] : Config.member_equity;
      sessionStorage.setItem("ruleForm_member", JSON.stringify(this.ruleForm));
    },

    delEquity(index) {
      this.ruleForm.memberEquity.splice(index, 1);
    },

    add() {
      if (this.ruleForm.memberEquity.length == 4) {
        this.$message({
          message: this.$t("member.memberSet.hyqyzd"),
          type: "error",
          showClose: true,
        });
        return;
      }
      if (
        !this.equity.equityName ||
        !this.equity.englishName ||
        !this.equity.icon
      ) {
        this.$message({
          message: this.$t("member.memberSet.qwsqy"),
          type: "error",
          showClose: true,
        });
        return;
      }
      this.ruleForm.memberEquity.push(this.equity);
      this.equity = {
        equityName: "", //权益名称
        englishName: "", //英文名称
        icon: "", //图标
      };
      this.$refs.upload.fileList = [];
    },

    reset() {
      this.equity = {
        equityName: "", //权益名称
        englishName: "", //英文名称
        icon: "", //图标
      };
      this.$refs.upload.fileList = [];
    },

    submitForm(formName) {
      let is_f;
      if (
        this.ruleForm.open_config[0].points !== null &&
        this.ruleForm.open_config[0].openMethodName !== null &&
        this.ruleForm.open_config[0].price !== null &&
        this.ruleForm.open_config[1].points !== null &&
        this.ruleForm.open_config[1].openMethodName !== null &&
        this.ruleForm.open_config[1].price !== null &&
        this.ruleForm.open_config[2].points !== null &&
        this.ruleForm.open_config[2].openMethodName !== null &&
        this.ruleForm.open_config[2].price !== null
      ) {
        console.log("267xxx");
        if (
          this.ruleForm.open_config[0].points.length <= 0 ||
          this.ruleForm.open_config[0].openMethodName.length <= 0 ||
          this.ruleForm.open_config[0].price.length <= 0 ||
          this.ruleForm.open_config[1].points.length <= 0 ||
          this.ruleForm.open_config[1].openMethodName.length <= 0 ||
          this.ruleForm.open_config[1].price.length <= 0 ||
          this.ruleForm.open_config[2].points.length <= 0 ||
          this.ruleForm.open_config[2].openMethodName.length <= 0 ||
          this.ruleForm.open_config[2].price.length <= 0
        ) {
          is_f = false;
        } else {
          is_f = true;
        }
      } else {
        console.log("269xxx");
        is_f = false;
      }
      if (!is_f) {
        return this.$message({
          message: this.$t("zdata.ktszsjtx"),
          type: "error",
          offset: 100,
        });
      }

      if (
        this.ruleForm.birthdayOpen == 1 &&
        this.ruleForm.pointsMultiple.length <= 0
      ) {
        return this.$message({
          message: "会员生日特权不能为空",
          type: "error",
          offset: 100,
        });
      }

      this.$refs[formName].validate(async (valid) => {
        console.log(this.ruleForm);
        if (valid) {
          try {
            if (
              this.ruleForm.renewOpen == 1 &&
              this.ruleForm.renewDay.length <= 0
            ) {
              return this.$message({
                message: "续费提醒设置不能为空",
                type: "error",
                offset: 100,
              });
            }

            if (!this.flag) {
              return
            }
            this.flag = false
            const res = await updateMember({
              storeId: getStorage("rolesInfo").storeId,
              // accessId: getStorage("laike_admin_userInfo").token,
              storeType: 8,
              api: "plugin.member.AdminMember.addOrUpdate",
              isOpen: this.ruleForm.isOpen,
              openConfig: JSON.stringify(this.ruleForm.open_config),
              birthdayOpen: this.ruleForm.birthdayOpen,
              pointsMultiple: this.ruleForm.pointsMultiple,
              bonusPointsOpen: this.ruleForm.bonusPointsOpen,
              bonusPointsConfig: JSON.stringify(
                this.ruleForm.bonusPointsConfig
              ),
              memberDiscount: this.ruleForm.memberDiscount,
              renewOpen: this.ruleForm.renewOpen,
              renewDay: this.ruleForm.renewDay,
              memberEquity: JSON.stringify(this.ruleForm.memberEquity),
            }).finally(() => {
              setTimeout(() => {
                this.flag = true
              }, 1500)
            })
            if (res.data.code == "200") {
              this.$message({
                message: this.$t("plugInsSet.czcg"),
                type: "success",
                offset: 100,
              });
              this.getMemberConfig();
              setTimeout(() => {
                this.$router.push({
                  path: "/plug_ins/plugInsSet/plugInsList",
                });
              }, 1000);
            } else {
              // this.$message({
              //   message: res.message,
              //   type: "error",
              //   offset: 100,
              // });
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
    oninput3(num) {
      var str = num;
      str = str.replace(/[^\.\d]/g, "");
      str = str.replace(".", "");
      return str == 0 ? 1 : str;
    },
    oninput4(num) {
      // var str = num;
      // let text = /^(?!(0[0-9]{0,}$))[0-9]{1,}[.]{0,}[0-9]{0,1}$/
      let text = /^[0-9]{1,}[.]{0,}[0-9]{0,2}$/;
      if (text.test(num)) {
        return num;
      }
      // str = str.replace(/[^\.\d]/g, "");
      // str = str.replace(".", "");
      // str = str.replace(/^-?\d{1,3}(,\d{3})*(\.\d{1,2})?$/,"")
      // return str == 0 ? 0.01 : str;
    },
    handleBlue(item) {
      if (item === undefined || item == 0 || item === '') {
        console.log("失去焦点", item);
        return 0.01;
      } else {
        return item;
      }
    },
  },
};
