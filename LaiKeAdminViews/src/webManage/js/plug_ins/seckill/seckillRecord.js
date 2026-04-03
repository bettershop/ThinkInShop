import { getSecRecord, delSecRecord } from "@/api/plug_ins/seckill";
import { mixinstest } from "@/mixins/index";
import { getStorage } from "@/utils/storage";
import { getButton } from "@/api/layout/information";
import ErrorImg from "@/assets/images/default_picture.png";
export default {
  name: "seckillRecord",
  mixins: [mixinstest],

  data() {
    return {
      ErrorImg: ErrorImg,
      routerList: JSON.parse(sessionStorage.getItem("tabRouter")),
      radio1: "2",
      statusList: [
        {
          value: "0",
          label: this.$t("seckill.seckillRecord.wyy"),
        },
        {
          value: "1",
          label: this.$t("seckill.seckillRecord.yyz"),
        },
        {
          value: "2",
          label: this.$t("seckill.seckillRecord.dy"),
        },
      ], // 会员等级
      menuId: "",
      inputInfo: {
        user: null,
        goodsName: null,
        date: null,
      },
      button_list: [],
      tableData: [],
      loading: true,
      // table高度
      tableHeight: null,
    };
  },

  created() {
    if (this.$route.query.name) {
      this.inputInfo.goodsName = this.$route.query.name;
      this.inputInfo.date = this.$route.query.date;
    }
    this.getSecRecords();
    this.getButtons();
  },

  mounted() {
    this.$nextTick(function () {
      this.getHeight();
    });
    window.addEventListener("resize", this.getHeight(), false);
  },

  methods: {
    async getSecRecords() {
      const res = await getSecRecord({
        api: "plugin.sec.AdminSec.getSecRecord",
        pageSize: this.pageSize,
        pageNo: this.dictionaryNum,
        user: this.inputInfo.user,
        goodsName: this.inputInfo.goodsName,
        startdate: this.inputInfo.date ? this.inputInfo.date[0] : null,
        enddate: this.inputInfo.date ? this.inputInfo.date[1] : null,
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

    // 获取table高度
    getHeight() {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight;
    },
    //获取按纽权限
    async getButtons() {
      let route = getStorage("route");
      route.forEach((item) => {
        if (item.path == "seckill") {
          item.children.forEach((e) => {
            if (e.path == "seckillRecord") {
              return (this.menuId = e.id);
            }
          });
        }
      });

      let buttonList = await getButton({
        api: "saas.role.getButton",
        token: getStorage("laike_admin_userInfo").token,
        storeId: getStorage("rolesInfo").storeId,
        menuId: this.menuId,
      });
      this.button_list = buttonList.data.data;
      console.log(this.button_list, "获取按纽权限");
    },
    // 重置
    reset() {
      this.inputInfo.user = null;
      this.inputInfo.goodsName = null;
      this.inputInfo.date = null;
    },
    // 查询
    demand() {
      this.showPagebox = false;
      this.loading = true;
      this.dictionaryNum = 1;
      this.getSecRecords().then(() => {
        this.loading = false;
        if (this.tableData.length > 5) {
          this.showPagebox = true;
        }
      });
    },

    // 查看
    View(value) {
      this.$router.push({
        path: "/plug_ins/stores/viewStore",
        query: {
          id: value.id,
        },
      });
    },

    // 修改
    Modify(value) {
      this.$router.push({
        path: "/plug_ins/stores/editorStore",
        query: {
          id: value.id,
        },
      });
    },

    // 删除
    Delete(value) {
      this.$confirm(
        this.$t("seckill.seckillRecord.scts"),
        this.$t("seckill.ts"),
        {
          confirmButtonText: this.$t("seckill.okk"),
          cancelButtonText: this.$t("seckill.ccel"),
          type: "warning",
        }
      )
        .then(() => {
          delSecRecord({
            api: "plugin.sec.AdminSec.delSecRecord",
            rid: value.id,
          }).then((res) => {
            console.log(res);
            if (res.data.code == "200") {
              //   this.getSecRecords();
              this.isFillList();
              this.$message({
                type: "success",
                message: this.$t("zdata.sccg"),
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
    isFillList() {
      let totalPage = Math.ceil((this.total - 1) / this.pageSize);
      // let totalPage = this.orderList.length
      let dictionaryNum =
        this.dictionaryNum > totalPage ? totalPage : this.dictionaryNum;
      this.dictionaryNum = dictionaryNum < 1 ? 1 : dictionaryNum;
      console.log("this.dictionaryNum", this.dictionaryNum, this.pageSize);
      this.getSecRecords().then(() => {
        this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1;
        this.current_num =
          this.tableData.length === this.pageSize
            ? this.dictionaryNum * this.pageSize
            : this.total;
        this.loading = false;
      }); //数据初始化方法
    },

    //选择一页多少条
    handleSizeChange(e) {
      this.loading = true;
      console.log(e);
      // this.current_num = e
      this.pageSize = e;
      this.getSecRecords().then(() => {
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
      this.getSecRecords().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total;
        this.loading = false;
      });
    },
  },
};
