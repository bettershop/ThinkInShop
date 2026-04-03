import { getMenberList, delMember,getGoStore } from "@/api/plug_ins/members";
import { mixinstest } from "@/mixins/index";
import { getStorage } from "@/utils/storage";
import { getButton } from "@/api/layout/information";
import { exports } from "@/api/export/index";
import ErrorImg from "@/assets/images/default_picture.png";

export default {
  name: "integralGoodsList",
  mixins: [mixinstest],

  data() {
    return {
      routerList:JSON.parse(sessionStorage.getItem('tabRouter')),
      radio1: "1",

      inputInfo: {
        userName: null,
        sequence: "",
      },
      button_list: [],
      tableData: [],
      loading: true,
      is_disabled: true,
      idList: [],
      menuId: "",
      sequenceList: [
        {
          value: "0",
          label: this.$t("member.memberList.fou"),
        },
        {
          value: "1",
          label: this.$t("member.memberList.shi"),
        },
      ], // 是否过期
      // table高度
      tableHeight: null,
      // 导出弹框数据
      dialogVisible: false,
    };
  },

  created() {
    // this.handleGoStore()
    this.getMenberList();
    // this.getButtons()
    if (getStorage('laike_admin_userInfo').mchId == 0) {
      this.$message({
        type: 'error',
        message: this.$t('plugInsSet.plugInsList.qtjdp'),
        offset: 100
      })
      this.$router.push('/mall/fastBoot/index')
    }
  },

  mounted() {
    this.$nextTick(function () {
      this.getHeight();
    });
    window.addEventListener("resize", this.getHeight(), false);
  },

  methods: {
    // 判断是否跳转自营店
    handleGoStore() {
      getGoStore({ api: "saas.shop.checkShopHavaSelfOwnedShop" }).then((res) => {
        if (res.data.code == 51008) {
          this.$router.push("/mall/fastBoot/index");
        }
      });
    },
    // 图片错误处理
    handleErrorImg(e) {
      console.log("图片报错了", e.target.src);
      e.target.src = ErrorImg;
    },
    async getMenberList() {
      const res = await getMenberList({
        api: "plugin.member.AdminMember.GetMember",
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        condition: this.inputInfo.userName,
        isOver: this.inputInfo.sequence,
      });
      console.log(res);
      this.current_num = 10;
      this.total = res.data.data.total;
      this.tableData = res.data.data.list;
      this.loading = false;
      if (this.total < this.current_num) {
        this.current_num = this.total;
      }
      if (this.total == 0) {
        this.showPagebox = false;
      } else {
        this.showPagebox = true;
      }
    },
    //获取按纽权限
    async getButtons() {
      let route = getStorage("route");
      route.forEach((item) => {
        if (item.path == "integralMall") {
          item.children.forEach((e) => {
            if (e.path == "integralGoodsList") {
              return (this.menuId = e.id);
            }
          });
        }
      });
      let buttonList = await getButton({
        api: "saas.role.getButton",
        menuId: this.menuId,
      });
      this.button_list = buttonList.data.data;
    },
    // 获取table高度
    getHeight() {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight;
      console.log(this.$refs.tableFather.clientHeight);
    },

    // 重置
    reset() {
      this.inputInfo.userName = null;
      this.inputInfo.sequence = null;
    },

    // 查询
    demand() {
      this.currpage = 1;
      this.current_num = 10;
      this.showPagebox = false;
      this.loading = true;
      this.dictionaryNum = 1;
      this.pageSize = 10;
      this.getMenberList().then(() => {
        this.loading = false;
        if (this.tableData.length > 5) {
          this.showPagebox = true;
        }
      });
    },

    buyRecord(value) {
      this.$router.push({
        path: "/plug_ins/member/buyRecord",
        query: {
          id: value.user_id,
        },
      });
    },

    addMember() {
      this.$router.push({
        path: "/plug_ins/member/addMember",
      });
    },

    Edit(value) {
      this.$router.push({
        path: "/plug_ins/member/editorMenber",
        query: {
          id: value.user_id,
        },
      });
    },

    Delete(value) {
      this.$confirm(this.$t("member.memberList.scqr"), this.$t("member.ts"), {
        confirmButtonText: this.$t("member.okk"),
        cancelButtonText: this.$t("member.ccel"),
        type: "warning",
      })
        .then(() => {
          delMember({
            api: "plugin.member.AdminMember.DelMember",
            userIds: value.user_id,
          }).then((res) => {
            console.log(res);
            if (res.data.code == "200") {
              this.getMenberList();
              this.$message({
                type: "success",
                message: this.$t("member.cg"),
                offset: 100,
              });
            }
          });
        })
        .catch(() => {
          // this.$message({
          //   type: 'info',
          //   message: '已取消删除',
          //   offset: 100
          // });
        });
    },

    //选择一页多少条
    handleSizeChange(e) {
      this.loading = true;
      console.log(e);
      this.pageSize = e;
      this.getMenberList().then(() => {
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
      this.getMenberList().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total;
        this.loading = false;
      });
    },

    // 导出弹框方法
    dialogShow() {
      this.dialogVisible = true;
    },

    handleClose(done) {
      this.dialogVisible = false;
    },

    async exportPage() {
      exports(
        {
          api: "plugin.member.AdminMember.GetMember",
          pageNo: this.dictionaryNum,
          pageSize: this.pageSize,
          condition: this.inputInfo.userName,
          isOver: this.inputInfo.sequence,
          exportType: 1,
        },
        "pageorder"
      );
    },

    async exportAll() {
      exports(
        {
          api: "plugin.member.AdminMember.GetMember",
          pageNo: this.dictionaryNum,
          pageSize: 9999999,
          exportType: 1,
        },
        "allorder"
      );
    },

    async exportQuery() {
      exports(
        {
          api: "plugin.member.AdminMember.GetMember",
          pageNo: this.dictionaryNum,
          pageSize: this.total,
          condition: this.inputInfo.userName,
          isOver: this.inputInfo.sequence,
          exportType: 1,
        },
        "queryorder"
      );
    },
  },
};
