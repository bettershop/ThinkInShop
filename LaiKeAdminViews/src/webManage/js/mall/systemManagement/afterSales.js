import {
  getSystemIndex,
  updateRefundService,
  saveAgreement,
} from "@/api/mall/systemManagement";
import { VueEditor } from "vue2-editor";
import OSS from "ali-oss";
import $ from "jquery";
import Config from "@/packages/apis/Config";
import axios from "axios";
import { getStorage, setStorage } from "@/utils/storage";
export default {
  name: "afterSales",
  components: {
    VueEditor,
  },
  data() {
    return {
      radio1: this.$t("systemManagement.shfw"),
      actionUrl: Config.baseUrl,
      ruleForm: {
        return_policy: "",
        cancellation_order: "",
        refund_process: "",
        refund_instructions: "",
        goodsEditorBase: "",
      },
      rules: {
        return_policy: [
          {
            required: true,
            message: this.$t("systemManagement.tkzcwk"),
            trigger: "blur",
          },
        ],
        cancellation_order: [
          {
            required: true,
            message: this.$t("systemManagement.qxddwk"),
            trigger: "blur",
          },
        ],
        refund_process: [
          {
            required: true,
            message: this.$t("systemManagement.tklcwc"),
            trigger: "blur",
          },
        ],
        refund_instructions: [
          {
            required: true,
            message: this.$t("systemManagement.tksmwk"),
            trigger: "blur",
          },
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
      }
      let formData = new FormData();
      for (let [key, value] of entries(data)) {
        formData.append(key, value);
      }
      const res = await getSystemIndex(formData);
      let systemInfo = res.data.data.data;
      this.ruleForm.return_policy = systemInfo.return_policy;
      this.ruleForm.cancellation_order = systemInfo.cancellation_order;
      this.ruleForm.refund_process = systemInfo.refund_process;
      this.ruleForm.refund_instructions = systemInfo.refund_instructions;
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
        api: "admin.system.updateRefundService",
        refundPolicy: this.ruleForm.return_policy,
        cancelOrderno: this.ruleForm.cancellation_order,
        refundMoney: this.ruleForm.refund_process,
        refundExplain: this.ruleForm.refund_instructions,
      }).then(res => {
        if (res.data.code == '200') {
          this.$message({
            message: this.$t("zdata.baccg"),
            type: "success",
            offset: 102,
          });
        }
      })
    },
  },
};
