import { editPlug } from "@/api/mall/plugInsSet";

export default {
  name: "editPlugIns",
  data() {
    return {
      isMchPlugin: '',
      plugType: '',
      ruleForm: {
        plugin_name: "",
        content: "",
        status: 1,
      },
      rules: {
        plugin_name: [
          {
            required: true,
            message: this.$t("plugInsSet.editPlugIns.qsrcjmc"),
            trigger: "blur",
          },
        ],
        content: [
          {
            required: true,
            message: this.$t("plugInsSet.editPlugIns.qsrcjjs"),
            trigger: "blur",
          },
        ],
      },
      flag: true
    };
  },

  mounted() {
    this.ruleForm.plugin_name = this.$route.query.plugin_name;
    this.ruleForm.content = this.$route.query.content;
    this.ruleForm.status = this.$route.query.status;
    this.isMchPlugin = this.$route.query.isMchPlugin
    console.log('isMchPlugin', this.isMchPlugin);
    if (this.isMchPlugin == '1') {
      this.plugType = this.$t('plugInsSet.plugInsList.dp')
    } else {
      this.plugType = this.$t('plugInsSet.plugInsList.sc')
    }

  },
  methods: {
    submitForm(formName) {
      if (!this.flag) {
        return
      }
      this.$refs[formName].validate(async (valid) => {
        if (valid) {
          this.flag = false
          try {
            let { entries } = Object;

            let data = {
              api: "admin.plugin.manage.addPlugin",
              id: this.$route.query.id,
              content: this.ruleForm.content,
              // pluginSwitch: this.ruleForm.status,
            };
            if (this.isMchPlugin == '1') {
              data.pluginSwitch = this.ruleForm.status
            }
            let formData = new FormData();
            for (let [key, value] of entries(data)) {
              formData.append(key, value);
            }
            editPlug(formData).then((res) => {
              if (res.data.code == "200") {
                this.$message({
                  message: this.$t("zdata.bjcg"),
                  type: "success",
                  offset: 102,
                });
                this.$router.go(-1);
              }
            }).finally(() => {
              setTimeout(() => {
                this.flag = true
              }, 1500)
            })
          } catch (e) {
            this.flag = true

            this.$message({
              message: e.message,
              type: "error",
              offset: 102,
            });
          }
        } else {
          return false;
        }
      });
    },
  },
};
