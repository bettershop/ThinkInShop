import {
  integralGoodsList,
  delIntegralGoods,
  topIntegralGoods,
} from "@/api/plug_ins/integralMall";
import { mixinstest } from "@/mixins/index";
import { getStorage } from "@/utils/storage";
import { getButton } from "@/api/layout/information";
import ErrorImg from "@/assets/images/default_picture.png";

export default {
  name: "integralGoodsList",
  mixins: [mixinstest],

  data() {
    return {
      routerList: JSON.parse(sessionStorage.getItem("tabRouter")),
      radio1: "1",

      inputInfo: {
        goodsName: null,
        mchName: null,
      },
      button_list: [],
      tableData: [],
      loading: true,
      is_disabled: true,
      idList: [],
      menuId: "",
      // table高度
      tableHeight: null,
      form: {
        num: "",
      },
      dialogFormVisible: false,
      thisadd: "",
    };
  },

  created() {
    this.integralGoodsLists();
    this.getButtons();
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
    handleSelectAll() {
      if (this.tableData.length == 0) {
        this.$refs.table.clearSelection();
      }
    },
    // 图片错误处理
    handleErrorImg(e) {
      console.log("图片报错了", e.target.src);
      e.target.src = ErrorImg;
    },
    async integralGoodsLists() {
      const res = await integralGoodsList({
        api: "plugin.integral.AdminIntegral.index",
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        goodsName: this.inputInfo.goodsName,
        mchName: this.inputInfo.mchName,
      });
      // if (res.data.code == 51008) {
      //   this.$router.push("/mall/fastBoot/index");
      // }
      console.log(res);
      this.current_num = 10;
      this.total = res.data.data.num;
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
      this.inputInfo.goodsName = null;
      this.inputInfo.mchName = null;
    },

    // 查询
    demand() {
      this.currpage = 1;
      this.current_num = 10;
      this.showPagebox = false;
      this.loading = true;
      this.dictionaryNum = 1;
      this.integralGoodsLists().then(() => {
        this.loading = false;
        if (this.tableData.length > 5) {
          this.showPagebox = true;
        }
      });
    },
    godetail(e) {
      this.$router.push({
        path: "/plug_ins/integralMall/integralGoodsListDetail",
        query: {
          id: e.id,
        },
      });
    },

    delAll() {
      this.$confirm(
        this.$t("integralGoodsList.qrsc"),
        this.$t("integralGoodsList.ts"),
        {
          confirmButtonText: this.$t("integralGoodsList.okk"),
          cancelButtonText: this.$t("integralGoodsList.ccel"),
          type: "warning",
        }
      )
        .then(() => {
          delIntegralGoods({
            api: "plugin.integral.AdminIntegral.del",
            ids: this.idList,
          }).then((res) => {
            console.log(res);
            if (res.data.code == "200") {
              this.integralGoodsLists();
              this.$message({
                type: "success",
                message: this.$t("zdata.plsccg"),
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

    addGoods() {
      this.$router.push({
        path: "/plug_ins/integralMall/addIntegralGoods",
      });
    },
    addkc(e) {
      this.thisadd = e;
      this.dialogFormVisible = true;
      this.form.num = "";
    },

    savekc() {
      if (this.form.num.trim().length <= 0) {
        return this.$message({
          message: this.$t("zdata.slqtxsz"),
          type: "warning",
          offset: 100,
        });
      }
      topIntegralGoods({
        api: "plugin.integral.AdminIntegral.addStock",
        proId: this.thisadd.id,
        num: this.form.num,
      }).then((res) => {
        console.log(res);
        if (res.data.code == "200") {
          this.$message({
            message: this.$t("zdata.tjcg"),
            type: "success",
            offset: 100,
          });
          this.dialogFormVisible = false;
          this.integralGoodsLists();
        }
      });
    },
    // 置顶
    placedTop(value) {
      topIntegralGoods({
        api: "plugin.integral.AdminIntegral.top",
        id: value.id,
      }).then((res) => {
        console.log(res);
        if (res.data.code == "200") {
          this.$message({
            type: "success",
            message: this.$t("integralGoodsList.cg"),
            offset: 100,
          });
        }
        this.integralGoodsLists();
      });
    },

    Edit(value) {
      this.$router.push({
        path: "/plug_ins/integralMall/editorIntegralGoods",
        query: {
          id: value.id,
        },
      });
    },
    onAndOff(e) {
      topIntegralGoods({
        api: "plugin.integral.AdminIntegral.onAndOff",
        proId: e.id,
      }).then((res) => {
        console.log(res);
        if (res.data.code == "200") {
          this.$message({
            type: "success",
            message: res.data.message,
            offset: 100,
          });
        }
        this.integralGoodsLists();
      });
    },
    Delete(value) {
      this.$confirm(
        this.$t("integralGoodsList.qrsc"),
        this.$t("integralGoodsList.ts"),
        {
          confirmButtonText: this.$t("integralGoodsList.okk"),
          cancelButtonText: this.$t("integralGoodsList.ccel"),
          type: "warning",
        }
      )
        .then(() => {
          delIntegralGoods({
            api: "plugin.integral.AdminIntegral.del",
            ids: value.id,
          }).then((res) => {
            console.log(res);
            if (res.data.code == "200") {
              this.integralGoodsLists();
              this.$message({
                type: "success",
                message: this.$t("zdata.sccg"),
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
      this.integralGoodsLists().then(() => {
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
      this.integralGoodsLists().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total;
        this.loading = false;
      });
    },

    // 选框改变
    handleSelectionChange(val) {
      if (val.length == 0) {
        this.is_disabled = true;
      } else {
        this.is_disabled = false;
      }
      console.log(val);
      this.idList = val.map((item) => {
        return item.id;
      });
      this.idList = this.idList.join(",");
    },
  },
};
