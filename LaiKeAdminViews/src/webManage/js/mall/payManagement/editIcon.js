import log from "@/libs/util.log";
import { getStorage } from "@/utils/storage";
import { editPayIcon, uploadIcon } from "@/api/mall/payManagement";
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
          { required: true, message: this.$t("DemoPage.tableFromPage.qscicontb"), trigger: "change" },
        ],
      },
      videoLoading2: false,
      isShowPopup:false
    };
  },
  computed: {},
  beforeRouteLeave (to, from, next) {
    if (to.name == 'payList') {
      to.params.dictionaryNum = this.$route.query.dictionaryNum
      to.params.pageSize = this.$route.query.pageSize
    }
    next();
  },
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
      console.log("自定义上传方式",file);
      this.myfile = file;
    },
    // 自定义上传接口
    async uploadFile(uploadFile, uploadFiles) {
      console.log("自定义上传方式",uploadFile);
      // this.beforeAvatarUpload(uploadFile)
      let data1 = {
        api: "admin.payment.setPaymentLoge",
        image: this.myfile,
      };
      if(!this.myfile){
        return
      }
      this.videoLoading2 = true;
      let { entries } = Object;
      let formData = new FormData();
      for (let [key, value] of entries(data1)) {
        formData.append(key, value);
      }
      try {
        const res = await uploadIcon(formData);
        if (res.data.code == 200) {
          // this.$message({
          //   type: "success",
          //   message: "上传成功",
          //   offset: 100,
          // });
          this.formData.Logo = res.data.data;
        } else {
          // this.$message({
          //   type: "warrang",
          //   message: this.$t("DemoPage.tableFromPage.scsb"),
          //   offset: 100,
          // });
        }
      } catch (error) {
        console.log(error);
      } finally {
        setTimeout(() => {
          this.videoLoading2 = false
        }, 1000)
      }
    },
    async handleOK() {
      if(this.formData.Logo.length<=0){
        return this.$message({
            type: "warning",
            message: this.$t("DemoPage.tableFromPage.tphmsc"),
            offset: 100,
          });
      }
      let { entries } = Object;
      let data = this.formData;
      let formData1 = new FormData();
      for (let [key, value] of entries(data)) {
        formData1.append(key, value);
      }
      const res = await editPayIcon(formData1);
      if (res.data.code == 200) {
        this.$message({
          message: this.$t('zdata.bjcg'),
          type: 'success',
          offset: 100
        })
        this.$router.go(-1);
      }
      console.log("54545454", res);
    },
    handleDelIcon() {
      this.formData.Logo = "";
    },
  },
};
