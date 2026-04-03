import {
  bannerIndexs,
  delBanner,
  topBannerById,
  addBannerInfo,
} from "@/api/mall/pcBanner";
import { mixinstest } from "@/mixins/index";
import ErrorImg from '@/assets/images/default_picture.png'

export default {
  name: "bannerListPC",
  mixins: [mixinstest],
  data() {
    return {
      tableData: [],
      loading: true,
      // table高度
      tableHeight: null,
      //   新增轮播图
      addBanner: false,
      editBanner: false,
      editId: "",
      ruleForm: {
        img: "",
        url: "",
        sort: "",
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

  created() {
    this.bannerIndexs();
  },

  mounted() {
    // this.$nextTick(function () {
    //   this.getHeight();
    // });
    // window.addEventListener("resize", this.getHeight(), false);
  },

  methods: {
     // 图片错误处理
     handleErrorImg(e){
      console.log('图片报错了',e.target.src);
      e.target.src=ErrorImg
    },
    submitForm(formName) {
      this.$refs.addruleForm.validate(async (valid) => {
        console.log(this.ruleForm);
        if (valid) {
          try {
            let { entries } = Object;
            let data = {
              api: "admin.pc.addBannerInfo",
              path: this.ruleForm.url,
              imageUrl: this.ruleForm.img,
              sort: this.ruleForm.sort,
            };
            let formData = new FormData();
            for (let [key, value] of entries(data)) {
              formData.append(key, value);
            }
            addBannerInfo(formData).then((res) => {
              if (res.data.code == "200") {
                this.$message({
                  message: this.$t("pcBanner.bannerList.tianjiachengg"),
                  type: "success",
                  offset: 100,
                });
                //   this.$router.go(-1);
                this.bannerIndexs();
                // this.addBanner = false;
                this.handleClose()
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
    // 轮播图弹窗界面
    // PC轮播图页面
    // 清空数据
    handleCloseData() {
      this.ruleForm = {
        img: "",
        url: "",
        sort: "",
      };
      // this.$nextTick(() => {
      //   this.$refs.uplaodBanner.fileList=[]
      //   this.$refs.addupload.fileList=[]
      // });
      setTimeout(()=>{
        this.$refs.uplaodBanner.fileList=[]
        this.$refs.addupload.fileList=[]
      })
    },
    handleClose(){
      setTimeout(()=>{
        this.$refs.addupload.fileList=[]
      })
      this.addBanner = false;
    },
    addbannerClose() {
      // this.$nextTick(() => {
        //   this.$refs.addupload.fileList=[]
        // });
        setTimeout(()=>{
          this.$refs.addupload.fileList=[]
        })
        this.addBanner = false;
    },
    editBannerClose() {
      this.editBanner = false;
    },
    async handleEditBanner(vale) {
      this.editId = vale.id;
      let { entries } = Object;
      let data = {
        api: "admin.pc.getBannerInfo",
        id: vale.id,
      };
      let formData = new FormData();
      for (let [key, value] of entries(data)) {
        formData.append(key, value);
      }
      const res = await bannerIndexs(formData);
      this.ruleForm.img = res.data.data.list[0].image;
      this.ruleForm.url = res.data.data.list[0].url;
      this.ruleForm.sort = res.data.data.list[0].sort;

      this.editBanner = true;
    },
    // 编辑的保存
    handleEditSubmitForm(formName) {
      this.$refs[formName].validate(async (valid) => {
        console.log(this.ruleForm);
        if (valid) {
          try {
            let { entries } = Object;
            let data = {
              api: "admin.pc.addBannerInfo",
              path: this.ruleForm.url,
              imageUrl: this.ruleForm.img,
              id: this.editId,
              sort: this.ruleForm.sort,
            };
            let formData = new FormData();
            for (let [key, value] of entries(data)) {
              formData.append(key, value);
            }
            addBannerInfo(formData).then((res) => {
              if (res.data.code == "200") {
                this.$message({
                  message: this.$t("pcBanner.bannerList.bjcg"),
                  type: "success",
                  offset: 100,
                });
                this.bannerIndexs();
                this.editBanner = false;
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
    // 获取table高度
    // getHeight() {
    //   this.tableHeight =
    //     this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight;
    // },

    async bannerIndexs() {
      let { entries } = Object;
      let data = {
        api: "admin.pc.getBannerInfo",
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
      };
      let formData = new FormData();
      for (let [key, value] of entries(data)) {
        formData.append(key, value);
      }
      const res = await bannerIndexs(formData);
      this.current_num = 10;
      this.total = res.data.data.total;
      this.tableData = res.data.data.list;
      this.loading = false;
      if (this.total < this.current_num) {
        this.current_num = this.total;
      }
      if (this.total == 0) {
        this.showPagebox = false;
      }
      console.log(res);
    },

    // 置顶
    placedTop(value) {
      let { entries } = Object;
      let data = {
        api: "admin.pc.topBannerById",
        id: value.id,
      };
      let formData = new FormData();
      for (let [key, value] of entries(data)) {
        formData.append(key, value);
      }
      topBannerById(formData).then((res) => {
        console.log(res);
        if (res.data.code == "200") {
          this.bannerIndexs();
          this.$message({
            type: "success",
            message: this.$t("pcBanner.bannerList.cg"),
            offset: 100,
          });
        }
      });
    },

    // 编辑
    Edit(value) {
      this.$router.push({
        path: "/mall/pcBanner/editorBanner",
        query: {
          id: value.id,
        },
      });
    },

    // 删除
    Delete(value) {
      this.$confirm(
        this.$t("pcBanner.bannerList.scts"),
        this.$t("pcBanner.bannerList.ts"),
        {
          confirmButtonText: this.$t("pcBanner.bannerList.okk"),
          cancelButtonText: this.$t("pcBanner.bannerList.ccel"),
          type: "warning",
        }
      )
        .then(() => {
          let { entries } = Object;
          let data = {
            api: "admin.pc.delBannerById",
            id: value.id,
          };
          let formData = new FormData();
          for (let [key, value] of entries(data)) {
            formData.append(key, value);
          }
          delBanner(formData).then((res) => {
            console.log(res);
            if (res.data.code == "200") {
              this.bannerIndexs();
              this.$message({
                type: "success",
                message: this.$t("pcBanner.bannerList.shanchuchengg"),
                offset: 100,
              });
            }
          });
        })
        .catch(() => {
          //   this.$message({
          //     type: 'info',
          //     message: '已取消删除',
          //     offset: 100
          //   });
        });
    },

    //选择一页多少条
    handleSizeChange(e) {
      this.loading = true;
      console.log(e);
      // this.current_num = e
      this.pageSize = e;
      this.bannerIndexs().then(() => {
        this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1;
        this.current_num =
          this.tableData.length === this.pageSize
            ? this.dictionaryNum * this.pageSize
            : this.total;
        this.loading = false;
      });
    },

    //点击上一页，下一页
    handleCurrentChange(e) {
      this.loading = true;
      this.dictionaryNum = e;
      this.currpage = (e - 1) * this.pageSize + 1;
      this.bannerIndexs().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total;
        this.loading = false;
      });
    },
  },
};
