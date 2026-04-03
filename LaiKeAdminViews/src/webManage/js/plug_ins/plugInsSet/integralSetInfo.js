import { getConfigInfo, addConfigInfo } from "@/api/plug_ins/integralMall";
import { VueEditor } from "vue2-editor";
import OSS from "ali-oss";
import Config from "@/packages/apis/Config";
import axios from "axios";
import { getStorage, setStorage } from "@/utils/storage";
export default {
  name: "mallSet",
  components: {
    VueEditor,
  },

  data() {
    //校验 积分和金额比例
     var validatePass2 = (rule, value, callback) => {
       if(Number(value ||0) ==0){
         return callback(new Error(this.$t("integralMall.mallSet.jfblnim")));
       } else if (!this.ruleForm.moneyNumber) {
          return callback(new Error(this.$t("integralMall.mallSet.jsrjebl")));
        } else if(Number(this.ruleForm.moneyNumber ||0) ==0) {
          return callback(new Error(this.$t("integralMall.mallSet.jeblnim")));
        }else {
          callback();
        }
      };
    return {
      isop: false,
      radio1: "3",
      actionUrl: Config.baseUrl,
      ruleForm: {
        integralNumber:'',
        moneyNumber:'',
        scoreRatio:'',
        isOpen: 1,
        integral_proportion: "",
        amsTime: "",
        issue_time: 0,
        overdue_set: "",
        slideshow: "",
        package_mail: 0,
        package_num: "",
        automatic_time: "",
        failure_time: "",
        afterSales_time: "",
        remind_day: "",
        remind_hours: "",
        auto_remind_day: "",
        autoCommentContent: "",
        content: "",
      },
      moneyUnit:'CNY',
      sortList:[
        {
          label:'CNY',
          value:'CNY'
        }
      ],
      rules: {
        integral_proportion: [
          {
            required: true,
            message: this.$t("integralMall.mallSet.qsrgwzs"),
            trigger: "blur",
          },
        ],
        amsTime: [
          {
            required: true,
            message: this.$t("integralMall.mallSet.qsrts"),
            trigger: "blur",
          },
        ],
        overdue_set: [
          {
            required: true,
            message: this.$t("integralMall.mallSet.qsrjfgq"),
            trigger: "blur",
          },
        ],
        automatic_time: [
          {
            required: true,
            message: this.$t("integralMall.mallSet.qsrzdsh"),
            trigger: "blur",
          },
        ],
        failure_time: [
          {
            required: true,
            message: this.$t("integralMall.mallSet.qsrddsx"),
            trigger: "blur",
          },
        ],
        afterSales_time: [
          {
            required: true,
            message: this.$t("integralMall.mallSet.qsrddsh"),
            trigger: "blur",
          },
        ],
        remind_day: [
          {
            required: true,
            message: this.$t("integralMall.mallSet.qsrtx"),
            trigger: "blur",
          },
        ],
        auto_remind_day: [
          {
            required: true,
            message: this.$t("integralMall.mallSet.qsrzd"),
            trigger: "blur",
          },
        ],
        autoCommentContent: [
          {
            required: true,
            message: this.$t("integralMall.mallSet.qsrzd"),
            trigger: "blur",
          },
        ],
        remind_hours: [
          {
            required: true,
            message: this.$t("integralMall.mallSet.qsrtx"),
            trigger: "blur",
          },
        ],
        integralNumber:[
          { required: true, message:  this.$t("integralMall.mallSet.jsrjfbl") , trigger: 'blur' },
          { validator: validatePass2, trigger: 'blur' }
        ]
      },

      issueTimeList: [
        {
          value: 0,
          name: this.$t("integralMall.mallSet.shh"),
        },
        {
          value: 1,
          name: this.$t("integralMall.mallSet.fkh"),
        },
      ],
      flag: true

    };
  },

  created() {
    this.getConfigInfos();
  },
  beforeRouteLeave(to, from, next) {
    if (
      JSON.stringify(this.ruleForm) ==
      sessionStorage.getItem("ruleForm_integra")
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

  methods: {

    back() {
      this.$router.push({
        path: "/plug_ins/plugInsSet/plugInsList",
      });
    },
    agreeChange() {
      this.$message({
        message: this.$t("integralMall.mallSet.qhff"),
        type: "warning",
        offset: 100,
      });
    },
    async getConfigInfos() {
      const res = await getConfigInfo({
        api: "plugin.integral.AdminIntegral.getConfigInfo",
      });
      console.log(res);
      const Config = res.data.data.config;
      if (!Config) {
        sessionStorage.setItem(
          "ruleForm_integra",
          JSON.stringify(this.ruleForm)
        );
        return;
      }
      this.ruleForm.isOpen = Config.status;
      this.ruleForm.slideshow = Config.bg_img;

      this.ruleForm.integral_proportion = Config.proportion;
      this.ruleForm.amsTime = Config.ams_time;
      this.ruleForm.issue_time = Config.give_status;
      this.ruleForm.overdue_set = Config.overdue_time / 86400;

      this.ruleForm.package_mail = Config.package_settings;
      this.ruleForm.package_num = Config.same_piece;
      this.ruleForm.automatic_time = Config.auto_the_goods / 86400;
      this.ruleForm.failure_time = Config.order_failure / 3600;
      this.ruleForm.afterSales_time = Config.order_after / 86400;
      this.ruleForm.remind_day = parseInt(
        Math.floor(Config.deliver_remind / 86400)
      );
      this.ruleForm.remind_hours = Math.floor(
        ((Config.deliver_remind - this.ruleForm.remind_day * 3600 * 24) %
          86400) /
        3600
      );
      this.ruleForm.auto_remind_day = Config.auto_good_comment_day / 86400;
      this.ruleForm.autoCommentContent = Config.auto_good_comment_content;
      // this.ruleForm.scoreRatio = Config.scoreRatio//积分比例
      if(Config.score_ratio){
        const price = Config.score_ratio.split(':')
        this.ruleForm.integralNumber = price[0]||1
        this.ruleForm.moneyNumber = price[1]||1
      }else{
        this.ruleForm.integralNumber = 1
        this.ruleForm.moneyNumber = 1
      }
      this.ruleForm.content = Config.content;
      this.isop = this.ruleForm.auto_remind_day > 0 ? true : false;
      sessionStorage.setItem("ruleForm_integra", JSON.stringify(this.ruleForm));
    },

    // handleImageAdded(file, Editor, cursorLocation, resetUploader) {
    //     let random_name = new Date().getTime() + '.' + file.name.split('.').pop()
    //     const client = new OSS({
    //       region: "oss-cn-shenzhen.aliyuncs.com",
    //       secure: true,
    //       endpoint: 'oss-cn-shenzhen.aliyuncs.com',
    //       accessKeyId: "LTAI4Fm8MFnadgaCdi6GGmkN",
    //       accessKeySecret: "NhBAJuGtx218pvTE4IBTZcvRzrFrH4",
    //       bucket: 'laikeds'
    //     });
    //     client.multipartUpload(random_name, file)
    //     .then(res => {
    //       console.log(res);
    //       Editor.insertEmbed(cursorLocation, 'image', res.res.requestUrls[0])
    //       Editor.setSelection(length + 1)
    //       resetUploader()
    //     })
    //     .catch(err => {
    //       console.log(err)
    //     })
    // },
    handleImageAdded(file, Editor, cursorLocation, resetUploader) {
      var formData = new FormData();
      formData.append("file", file); //第一个file 后台接收的参数名
      axios({
        url: this.actionUrl, //上传路径
        method: "POST",
        params: {
          api: "resources.file.uploadFiles",
          storeId: getStorage("laike_admin_userInfo").storeId,
          groupId: -1,
          uploadType: 2,
          coverage:1,
          accessId: this.$store.getters.token,
        },
        data: formData,
      })
        .then((result) => {
          let url = result.data.data.imgUrls[0]; // 返回给你的图片路径
          Editor.insertEmbed(cursorLocation, "image", url);
          // Editor.setSelection(length + 1)
          resetUploader();
        })
        .catch((err) => {
          console.log(err);
        });
    },

    submitForm(formName) {
      this.$refs[formName].validate(async (valid) => {
        console.log(this.ruleForm);
        if (valid) {
          try {
            if (!this.flag) {
              return
            }
            this.flag = false
            addConfigInfo({
              api: "plugin.integral.AdminIntegral.addConfigInfo",
              status: this.ruleForm.isOpen,
              imgUrls: this.ruleForm.slideshow,
              proportion: this.ruleForm.integral_proportion,
              amsTime:
                this.ruleForm.issue_time == 0 ? this.ruleForm.amsTime : "",
              giveStatus: this.ruleForm.issue_time,
              overdueTime: parseInt(this.ruleForm.overdue_set) * 86400,

              isFreeShipping: this.ruleForm.package_mail,
              goodsNum: Math.round(this.ruleForm.package_num),
              autoReceivingGoodsDay: Math.round(this.ruleForm.automatic_time),
              orderInvalidTime: Math.round(this.ruleForm.failure_time),
              returnDay: Math.round(this.ruleForm.afterSales_time),
              deliverRemind: parseInt(this.ruleForm.remind_hours) * 3600,
              // deliverRemind: parseInt(this.ruleForm.remind_day) == 0 ? parseInt(this.ruleForm.remind_hours) * 3600 : parseInt(this.ruleForm.remind_day) * 86400  + parseInt(this.ruleForm.remind_hours) * 3600,
              autoCommentDay: Math.round(this.ruleForm.auto_remind_day),
              autoCommentContent: this.ruleForm.autoCommentContent,
              scoreRatio:`${this.ruleForm.integralNumber}:${this.ruleForm.moneyNumber}` ,//积分比例
              content: this.ruleForm.content,
            }).then((res) => {
              if (res.data.code == "200") {
                this.$message({
                  message: this.$t("plugInsSet.czcg"),
                  type: "success",
                  offset: 100,
                });
                sessionStorage.setItem(
                  "ruleForm_integra",
                  JSON.stringify(this.ruleForm)
                );
                setTimeout(() => {
                  this.$router.push({
                    path: "/plug_ins/plugInsSet/plugInsList",
                  });
                }, 1000);
              }
            }).finally(() => {
              setTimeout(() => {
                this.flag = true
              }, 1500)
            })

          } catch (error) {
            setTimeout(() => {
              this.flag = true
            }, 1500)
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

    oninput2(num) {
      var str = num;
      str = str.replace(/[^\.\d]/g, "");
      str = str.replace(".", "");

      return str;
    },
    oninput3(num) {
      var str = num;
      var str = num;
      str = str.replace(/[^\.\d]/g, "");
      str = str.replace(".", "");
      if (str == 0) {
        return ''
      } else {
        return str;
      }

      // var reg = /^[1-9]\d*$/;
      // if (reg.test(str)) {
      //   return str;
      // } else {
      //   return "";
      // }
    },
    mychange(e) {
      if (e) {
        this.ruleForm.auto_remind_day = 1;
      } else {
        this.ruleForm.auto_remind_day = 0;
      }
    },
  },
};
