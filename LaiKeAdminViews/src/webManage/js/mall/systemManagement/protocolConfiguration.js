import { getAgreementIndex, delAgreement } from "@/api/mall/systemManagement";
import { mixinstest } from "@/mixins/index";
import { getStorage } from "@/utils/storage";
import { getButton } from "@/api/layout/information";

export default {
  name: "protocolConfiguration",
  mixins: [mixinstest],
  data() {
    return {
      radio1: this.$t("systemManagement.xypz"),
      button_list: [],
      tableData: [],
      loading: true,
      // table高度
      tableHeight: null,
      menuId: "",
      upgrade:""
    };
  },

  created() {
    this.getAgreementIndexs();
    this.getButtons();
  },

  mounted() {
    this.$nextTick(function () {
      this.getHeight();
    });
    window.addEventListener("resize", this.getHeight(), false);
  },

  methods: {
    //获取按纽权限
    async getButtons() {
      let route = getStorage("route");
      route.forEach((item) => {
        if (item.path == "protocolConfiguration") {
          return (this.menuId = item.id);
        }
      });
      let buttonList = await getButton({
        api: "saas.role.getButton",
        menuId: this.menuId,
      });
      // let buttonList = await getButton({
      //     api: 'saas.role.getButton',
      //     token: getStorage('laike_admin_userInfo').token,
      //     storeId: getStorage('rolesInfo').storeId,
      //     menuId: getStorage('menuId'),
      // })
      this.button_list = buttonList.data.data;
      this.button_list = buttonList.data.data.map((item) => {
        return item.title;
      });
      console.log(this.button_list, "按纽权限");
    },
    getHeight() {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight;
    },
    async getAgreementIndexs() {
      let { entries } = Object;
      let data = {
        api: "admin.system.getAgreementIndex",
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
      };
      let formData = new FormData();
      for (let [key, value] of entries(data)) {
        formData.append(key, value);
      }
      const res = await getAgreementIndex(formData);
      this.current_num = 10;
      this.total = res.data.data.list.length;
      this.tableData = res.data.data.list;
      this.upgrade = res.data.data.upgrade;
      this.loading = false;
      if (this.total < this.current_num) {
        this.current_num = this.total;
      }
      if (this.tableData.length < 5) {
        this.showPagebox = false;
      }
      console.log(res);
    },

    // 编辑
    Edit(value) {
      this.$router.push({
        path: "/mall/agreement/editorAgreement",
        query: {
          id: value.id,
          shaco: 1,
        },
      });
    },

    // 删除
    Delete(value) {
      this.$confirm(
        this.$t("systemManagement.protocolConfiguration.scts"),
        this.$t("systemManagement.protocolConfiguration.ts"),
        {
          confirmButtonText: this.$t(
            "systemManagement.protocolConfiguration.okk"
          ),
          cancelButtonText: this.$t(
            "systemManagement.protocolConfiguration.ccel"
          ),
          type: "warning",
        }
      )
        .then(() => {
          let { entries } = Object;
          console.log('xxxxx',value);
          let data = {
            api: "admin.system.delAgreement",
            id: value.id,
          }
          let formData = new FormData();
          for (let [key, value] of entries(data)) {
            formData.append(key, value);
          }
          delAgreement(formData).then((res) => {
            console.log(res);
            if (res.data.code == "200") {
              this.getAgreementIndexs();
              this.$message({
                type: "success",
                message: this.$t("zdata.sccg"),
                offset: 102,
              });
            }
          });
        })
        .catch(() => {
          // this.$message({
          //     type: 'info',
          //     message: '已取消删除',
          //     offset: 100
          // });
        });
    },

    //选择一页多少条
    handleSizeChange(e) {
      this.loading = true;
      console.log(e);
      // this.current_num = e
      this.pageSize = e;
      this.getAgreementIndexs().then(() => {
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
      this.getAgreementIndexs().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total;
        this.loading = false;
      });
    },
  },
};
