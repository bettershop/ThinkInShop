import log from "@/libs/util.log";
import { getStorage } from "@/utils/storage";
import { editPayIcon, uploadIcon } from "@/api/Platform/payManagement";
import ErrorImg from '@/assets/images/default_picture.png'
export default {
  data() {
    return {
      myfile: "",
      sevUrl: process.env.VUE_APP_BASE_API.replace("gw", ""),
      formData: {
        // storeId: getStorage("laike_admin_userInfo").storeId,
        // storeType: 8,
        api: "admin.payment.setPayment",
        id: "",
        name: "",
        remark: "",
        Logo: "",
      },
      rules: {
        Logo: [
          { required: true, message: "请上传icon支付图标", trigger: "change" },
        ],
      },
      videoLoading2: false,
      isShowPopup:false
    };
  },
  computed: {},
  created() {
    this.handleGetQuery();
  },
  mounted() {},
  methods: {
     // 图片错误处理
     handleErrorImg(e){
      console.log('图片报错了',e.target.src);
      e.target.src=ErrorImg
    },
    handleRemove() {},
    handleGetQuery() {
      console.log("xxxxxxxx", this.$route.query);
      this.formData.name = this.$route.query.name;
      this.formData.Logo = this.$route.query.logo;
      this.formData.remark = this.$route.query.description;
      this.formData.id = this.$route.query.id;
    },
    handleAvatarSuccess(response, file, fileList) {},
    beforeAvatarUpload(file) {
      //   this.formData.image = file;
      this.myfile = file;
    },
    // 自定义上传接口
    async uploadFile(params) {
      console.log("自定义上传方式");
      let data1 = {
        api: "admin.payment.setPaymentLoge",
        image: this.myfile,
      };
      let { entries } = Object;
      let formData = new FormData();
      for (let [key, value] of entries(data1)) {
        formData.append(key, value);
      }
      const res = await uploadIcon(formData);
      if (res.data.code == 200) {
        this.$message({
          type: "success",
          message: "上传成功",
          offset: 100,
        });
        this.formData.Logo = res.data.data;
      } else {
        this.$message({
          type: "warrang",
          message: "上传失败",
          offset: 100,
        });
      }
    },
    async handleOK() {
      let { entries } = Object;
      let data = this.formData;
      let formData1 = new FormData();
      for (let [key, value] of entries(data)) {
        formData1.append(key, value);
      }
      const res = await editPayIcon(formData1);
      if (res.data.code == 200) {
        this.$router.go(-1);
      }
      console.log("54545454", res);
    },
    handleDelIcon() {
      this.formData.Logo = "";
      this.$refs.cityList.style.display = "none";
    },
  },
};
