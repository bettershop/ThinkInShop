import { pointDetailsList } from "@/api/finance/withdrawalManage";
import { exports } from "@/api/export/index";
import { mixinstest } from "@/mixins/index";
import { getStorage } from "@/utils/storage";
import { getButton } from "@/api/layout/information";

export default {
  name: "rechargeList",
  mixins: [mixinstest],
  data() {
    return {
      userid: "",
      exportType: "", //判断是否为导出
      kepList: [
        {
          value: "1",
          label: this.$t("pointDetails.rz"),
        },
        {
          value: "2",
          label: this.$t("pointDetails.zc"),
        },
      ],
      TypeList: [
        {
          value: "6",
          label: this.$t("pointDetails.xtcz"),
        },
        {
          value: "5",
          label: this.$t("pointDetails.xtkc"),
        },
        {
          value: "10",
          label: this.$t("pointDetails.jfgq"),
        },
        {
          value: "8",
          label: this.$t("pointDetails.yhgw"),
        },
        {
          value: "11",
          label: this.$t("pointDetails.gmhy"),
        },
        {
          value: "1",
          label: this.$t("pointDetails.gwxh"),
        },
        {
          value: "12",
          label: this.$t("pointDetails.sysr"),
        },
        {
          value: "13",
          label: this.$t("pointDetails.djjf"),
        },
      ],
      inputInfo: {
        userName: "",
        oType: "",
        date: "",
        ostatus: "",
      },

      tableData: [],
      loading: true,
      type: "",
      // table高度
      tableHeight: null,
      button_list: [],
      // 导出弹框数据
      dialogVisible: false,
    };
  },

  created() {
    this.getButtons();
  },

  mounted() {
    this.$nextTick(function () {
      this.getHeight();
    });
    this.userid = this.$route.query.id;
    console.log("userid:", this.userid);
    this.axios();
    window.addEventListener("resize", this.getHeight(), false);
  },

  methods: {
    getType(value) {
      switch (value) {
        case 6:
          return this.$t("pointDetails.xtcz");
        case 5:
          return this.$t("pointDetails.xtkc");
        case 10:
          return this.$t("pointDetails.jfgq");
        case 8:
          return this.$t("pointDetails.yhgw");
        case 11:
          return this.$t("pointDetails.gmhy");
        case 1:
          return this.$t("pointDetails.gwxh");
        case 12:
          return this.$t("pointDetails.sysr");
        case 13:
          return this.$t("pointDetails.djjf");
        case 14:
          return this.$t("pointDetails.qdjf");
        default:
          break;
      }
    },
    //初始化资金详情列表数据
    async axios() {
      const res = await pointDetailsList({
        api: "admin.user.getUserIntegralInfo_see",
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        userName: this.inputInfo.userName,
        oType: this.inputInfo.oType,
        startDate: this.inputInfo.date?.[0] ?? "",
        endDate: this.inputInfo.date?.[1] ?? "",
        userid: this.userid,
      });
      console.log("res:", res);

      if (res.data.code == 200) {
        this.loading = false;
        this.tableData = res.data.data.list;
        this.total = res.data.data.total;
        console.log("tableData:", this.tableData);
      }
    },
    // 获取table高度
    getHeight() {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight;
      console.log(this.$refs.tableFather.clientHeight);
    },
    //获取按纽权限
    async getButtons() {
      let route = getStorage("route");
      route.forEach((item) => {
        if (item.path == "salesReturn") {
          return (this.menuId = item.id);
        }
      });
      let buttonList = await getButton({
        api: "saas.role.getButton",
        menuId: this.menuId,
      });
      this.button_list = buttonList.data.data;
    },

    // 重置
    reset() {
      (this.inputInfo.oType = ""), (this.inputInfo.date = "");
    },

    // 查询
    demand() {
      this.currpage = 1;
      this.current_num = 10;
      this.showPagebox = false;
      this.loading = true;
      this.dictionaryNum = 1;
      this.axios().then(() => {
        this.loading = false;
        if (this.tableData.length > 5) {
          this.showPagebox = true;
        }
      });
    },

    //选择一页多少条
    handleSizeChange(e) {
      this.loading = true;
      // this.current_num = e
      this.pageSize = e;
      this.axios().then(() => {
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
      this.axios().then(() => {
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
          api: "admin.user.getUserIntegralInfo_see",
          pageNo: this.dictionaryNum,
          pageSize: this.pageSize,
          exportType: 1,
          userName: this.inputInfo.userName,
          oType: this.inputInfo.oType,
          startDate: this.inputInfo.date?.[0] ?? "",
          endDate: this.inputInfo.date?.[1] ?? "",
          userid: this.userid,
        },
        "查看详情_导出本页"
      );
    },

    async exportAll() {
      exports(
        {
          api: "admin.user.getUserIntegralInfo_see",
          pageNo: 1,
          pageSize: this.total,
          exportType: 1,
          userName: this.inputInfo.userName,
          oType: this.inputInfo.oType,
          startDate: this.inputInfo.date?.[0] ?? "",
          endDate: this.inputInfo.date?.[1] ?? "",
          userid: this.userid,
        },
        "查看详情_导出全部"
      );
    },

    async exportQuery() {
      exports(
        {
          api: "admin.user.getUserIntegralInfo_see",
          pageNo: 1,
          pageSize: this.total,
          exportType: 1,
          userName: this.inputInfo.userName,
          oType: this.inputInfo.oType,
          startDate: this.inputInfo.date?.[0] ?? "",
          endDate: this.inputInfo.date?.[1] ?? "",
          userid: this.userid,
        },
        "查看详情_导出查询"
      );
    },
  },
};
