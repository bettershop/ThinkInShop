import { PlugSet, getPlugSet } from "@/api/mall/plugInsSet";
import Config from "@/packages/apis/Config";
import { VueEditor } from "vue2-editor";
import { getStorage } from "@/utils/storage";
import axios from "axios";
export default {
  name: "groupSetInfo",
  components: {
    VueEditor,
  },
  data() {
    return {
      ruleForm: {
        isOpen: 0,
        autoTheGoods: "",
        // afterSwitch: 0,
        orderAfter: "",
        goodSwitch: 0,
        autoGoodCommentDay: "",
        autoCommentContent: "",
        content: "",
      },

      rules: {
        autoTheGoods: [
          {
            required: true,
            message: this.$t("plugInsSet.groupSetInfo.qsrzdshsj"),
            trigger: "blur",
          },
        ],
        // orderAfter: [
        //   { required: true, message: '请输入订单收货时间', trigger: 'change' }
        // ],
        // autoGoodC ommentDay: [
        //   { required: true, message: '请填写好评设置', trigger: 'blur' }
        // ],
      },

      actionUrl: Config.baseUrl,
      flag: true
    };
  },

  created() {
    this.getPlugSetInfo();
  },
  beforeRouteLeave(to, from, next) {
    if (JSON.stringify(this.ruleForm) == sessionStorage.getItem('ruleForm_group')) {
      next()
    } else {
      console.log('表单变化，询问是否保存')
      next(false)
      this.$confirm(
        this.$t('coupons.couponsSet.sjygx'),
        this.$t('coupons.ts'),
        {
          distinguishCancelAndClose: true,
          confirmButtonText: this.$t('coupons.okk'),
          cancelButtonText: this.$t('coupons.ccel'),
          type: 'warning'
        }
      )
        .then(() => {
          // this.submitForm('ruleForm')
          next()
        })
        .catch(() => {
          // next()
          // next('/plug_ins/plugInsSet/plugInsList')
        })
    }
  },
  methods: {
    async getPlugSetInfo() {
      let { entries } = Object;
      let data = {
        api: "admin.plugin.manage.getOrderConfig",
        pluginCode: this.$route.query.pluginCode ?? "",
      };
      let formData = new FormData();
      for (let [key, value] of entries(data)) {
        formData.append(key, value);
      }
      const res = await getPlugSet(formData);
      let arr = res.data.data;
      this.ruleForm.autoTheGoods = arr.autoTeGood;
      this.ruleForm.orderAfter = arr.orderAfter / 60 / 60 / 24;
      // if (this.ruleForm.orderAfter == 15) {
      //   this.ruleForm.afterSwitch = 0;
      // } else {
      //   this.ruleForm.afterSwitch = 1;
      // }
      this.ruleForm.autoGoodCommentDay = arr.commentDay;
      if (this.ruleForm.autoGoodCommentDay == 0) {
        this.ruleForm.goodSwitch = 0;
      } else {
        this.ruleForm.goodSwitch = 1;
      }
      this.ruleForm.autoCommentContent = arr.autoCommentContent;
      this.ruleForm.isOpen = arr.isOpen
      this.ruleForm.content = arr.content ? arr.content : "";
      sessionStorage.setItem('ruleForm_group', JSON.stringify(this.ruleForm))
    },
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
          console.log(result);
          let url = result.data.data.imgUrls[0]; // 返回给你的图片路径
          Editor.insertEmbed(cursorLocation, "image", url);
          // Editor.setSelection(length);
          resetUploader();
        })
        .catch((err) => {
          console.log(err);
        });
    },
    afterChange(row) {
      if (row == 0) {
        this.ruleForm.orderAfter = 15;
      }
    },
    goodChange(row) {
      if (row == 0) {
        this.ruleForm.autoGoodCommentDay = 0;
        this.ruleForm.autoCommentContent = '';
      } else {
        this.ruleForm.autoGoodCommentDay = 3;
        this.ruleForm.autoCommentContent = '好评内容';
      }
    },
    back() {
      this.$router.push({
        path: '/plug_ins/plugInsSet/plugInsList'
      })
    },
    submitForm(formName) {
      if (this.ruleForm.autoTheGoods == "") {
        this.ruleForm.autoTheGoods = 7;
      }
      this.$refs[formName].validate(async (valid) => {
        if (valid) {
          try {
            // if (this.ruleForm.afterSwitch == 1) {
            //   if (this.ruleForm.orderAfter == "") {
            //     this.warnMsg(
            //       this.$t("plugInsSet.groupSetInfo.ddshsjbnwk")
            //     );
            //     return;
            //   }
            // }
            // if (this.ruleForm.goodSwitch == 0) {
            //   if (
            //     this.ruleForm.autoGoodCommentDay == "" ||
            //     this.ruleForm.autoCommentContent == ""
            //   ) {
            //     this.ruleForm.autoGoodCommentDay = 3
            //     this.ruleForm.autoCommentContent = '好评内容'
            //     // this.warnMsg(
            //     //   this.$t("plugInsSet.groupSetInfo.kqhpszbnwk")
            //     // );
            //     // return;
            //   }
            // }
            let data = {
              api: "admin.plugin.manage.addPluginConfig",
              isOpen: this.ruleForm.isOpen,
              pluginCode: this.$route.query.pluginCode ?? "",
              autoTheGoods: this.ruleForm.autoTheGoods,
              orderAfter: this.ruleForm.orderAfter,
              orderFailure: 0,
              autoGoodCommentDay: this.ruleForm.autoGoodCommentDay,
              autoCommentContent: this.ruleForm.autoCommentContent,
              content: this.ruleForm.content,
            };
            let { entries } = Object;
            let formData = new FormData();
            for (let [key, value] of entries(data)) {
              formData.append(key, value);
            }
            if (!this.flag) {
              return
            }
            this.flag = false
            PlugSet(formData).then((res) => {
              if (res.data.code == "200") {
                this.getPlugSetInfo();
                this.$message({
                  message: this.$t("plugInsSet.czcg"),
                  type: "success",
                  offset: 100,
                });
                setTimeout(() => {
                  // this.$router.go(-1);
                  this.$router.push({
                    path: '/plug_ins/plugInsSet/plugInsList'
                  })
                }, 1000);
              }
            }).finally(() => {
              setTimeout(() => {
                this.flag = true
              }, 1500)
            })
          } catch (error) {
            this.$message({
              message: error.message,
              type: "error",
              offset: 100,
            });
          }
        } else {
          console.log("error submit!!");
          return false;
        }
      });
    },
  },
};
