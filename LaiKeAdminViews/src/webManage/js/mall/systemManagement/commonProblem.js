import {
  getSystemIndex,
  updateCommonProblem,
  saveAgreement,
} from "@/api/mall/systemManagement";
import { VueEditor } from "vue2-editor";
import Config from "@/packages/apis/Config";
import axios from "axios";
import { getStorage, setStorage } from "@/utils/storage";
import $ from "jquery";

export default {
  name: "commonProblem",
  components: {
    VueEditor,
  },
  data() {
    return {
      radio1: this.$t("systemManagement.cjwt"),
      actionUrl: Config.baseUrl,
      ruleForm: {
        after_sales_issues: "",
        payment_issues: "",
      },
      rules: {
        after_sales_issues: [
          { required: true, message: "售后问题不能为空", trigger: "blur" },
        ],
        payment_issues: [
          { required: true, message: "支付问题不能为空", trigger: "blur" },
        ],
      },
    };
  },

  created() {
    this.getBase();
    this.getSystemIndexs();
  },

  methods: {
    getBase() {
      this.goodsEditorBase = Config.baseUrl
    },
    tabJump() {
      this.$router.push("/mall/agreement/protocolConfiguration");
      setStorage("menuId", 600);
    },
    async getSystemIndexs() {
      let { entries } = Object;
      let data = {
        api: "admin.system.getSystemIndex",
      };
      let formData = new FormData();
      for (let [key, value] of entries(data)) {
        formData.append(key, value);
      }
      const res = await getSystemIndex(formData);
      let systemInfo = res.data.data.data;
      this.ruleForm.after_sales_issues = systemInfo.after_sales_issues;
      this.ruleForm.payment_issues = systemInfo.payment_issues;
      console.log(res);
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
          accessId: this.$store.getters.token,
        },
        data: formData,
      })
        .then((result) => {
          let url = result.data.data.imgUrls[0]; // 返回给你的图片路径
          Editor.insertEmbed(cursorLocation, "image", url);
          // Editor.setSelection(length + 1);
          resetUploader();
        })
        .catch((err) => {
          console.log(err);
        });
    },

    submitForm() {
      saveAgreement({
        api: "admin.system.updateCommonProblem",
        returnProblem: this.ruleForm.after_sales_issues,
        payProblem: this.ruleForm.payment_issues,
      }).then(res => {
        if (res.data.code == '200') {
          this.$message({
            message: this.$t("zdata.baccg"),
            type: "success",
            offset: 102,
          });
          this.getSystemIndexs();
        }
      })
    },
  },
};
