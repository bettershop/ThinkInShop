import { bannerIndexs, delBanner, topBannerById } from "@/api/mall/pcBanner";
import { mixinstest } from "@/mixins/index";
export default {
  name: "playlist",
  mixins: [mixinstest],
  data() {
    return {
      tableData: [],
      loading: true,
      // table高度
      tableHeight: null,
    };
  },

  created() {
    this.bannerIndexs();
  },

  mounted() {
    this.$nextTick(function () {
      this.getHeight();
    });
    window.addEventListener("resize", this.getHeight(), false);
  },

  methods: {
    // 获取table高度
    getHeight() {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight;
    },

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
                message: this.$t("pcBanner.bannerList.cg"),
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
