import { addBannerInfo } from "@/api/mall/pcBanner";
export default {
  name: "addBanner",
  data() {
    return {
      ruleForm: {
        img: "",
        url: "",
      },

      rules: {
        img: [
          {
            required: true,
            message: this.$t("pcBanner.bannerList.qsclbt"),
            trigger: "blur",
          },
        ],
      },
    };
  },

  methods: {
    submitForm(formName) {
      this.$refs[formName].validate(async (valid) => {
        console.log(this.ruleForm);
        if (valid) {
          try {
            let { entries } = Object;
            let data = {
                api: "admin.pc.addBannerInfo",
                path: this.ruleForm.url,
                imageUrl: this.ruleForm.img,
            };
            let formData = new FormData();
            for (let [key, value] of entries(data)) {
              formData.append(key, value);
            }
            addBannerInfo(formData).then((res) => {
              if (res.data.code == "200") {
                this.$message({
                  message: this.$t("pcBanner.bannerList.cg"),
                  type: "success",
                  offset: 100,
                });
                this.$router.go(-1);
              }
              console.log(res);
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
  },
};
