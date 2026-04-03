import {
  orderList,
  delOrder,
  closeOrder,
  deliveryView,
} from "@/api/plug_ins/group";
import { exports } from "@/api/export/index";
import { kuaidishow } from "@/api/order/orderList";
import { mixinstest } from "@/mixins/index";
import { deliverySave } from "@/api/order/orderList";
export default {
  name: "groupOrderList",
  mixins: [mixinstest],
  data() {
    return {
      routerList: JSON.parse(sessionStorage.getItem('tabRouter')),
      radio1: this.$t("group.ptdd"),
      stateList: [
        {
          value: "0",
          label: this.$t("orderLists.dfk"),
        },
        {
          value: "1",
          label: this.$t("orderLists.dfh"),
        },
        {
          value: "2",
          label: this.$t("orderLists.dsh"),
        },
        {
          value: "5",
          label: this.$t("orderLists.ywc"),
        },
        {
          value: "7",
          label: this.$t("orderLists.ygb"),
        },
      ], // 订单状态
      typeList: [
        {
          value: "1",
          label: this.$t("orderLists.yhxd"),
        },
        {
          value: "2",
          label: this.$t("orderLists.dpxd"),
        },
        {
          value: "3",
          label: this.$t("orderLists.ptxd"),
        },
      ], // 订单类型
      inputInfo: {
        orderInfo: null,
        store: null,
        state: null,
        type: null,
        date: null,
      },
      is_disabled: true, // 批量删除按钮
      goodsListType: 0, // 商品列表类型
      orderList: [], // 订单号集合

      tableData: [],
      loading: true,
      // button_list: [],
      // 弹框数据
      dialogVisible2: false,
      ruleForm: {
        courier_company: "",
        courier_no: "",
        logistics: "",
      },
      ruleForm4: {
        return_money: '',
        orderno: '',
        endDate: '',
      },
      logisticsTracking: [],

      logisticsList: [],

      // 导出弹框数据
      dialogVisible: false,
      dialogVisible3: false,

      dialogVisible4: false,
      // table高度
      tableHeight: null,
      ruleForm2: {
        kuaidi_name: null,
        kuaidi_no: null,
      },
      detailId: "",
      courierList: [],
      rules2: {
        kuaidi_name: [
          {
            required: true,
            message: this.$t("group.groupOrderList.qxzkdgs"),
            trigger: "change",
          },
        ],
        kuaidi_no: [
          {
            required: true,
            message: this.$t("group.groupOrderList.qtxkddh"),
            trigger: "blur",
          },
        ],
      },
      logistics_type: false, //电子面单

    };
  },

  created() {
    if (this.$route.query.no) {
      orderList({
        api: "plugin.group.AdminGroupOrder.Index",
        pageSize: this.pageSize,
        pageNo: this.dictionaryNum,
        id: this.$route.query.no,
        keyWord: this.inputInfo.orderInfo,
        status: this.inputInfo.state,
        startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
        endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
      }).then((res) => {
        console.log(res);
        this.total = res.data.data.total;
        this.tableData = res.data.data.list;
        this.loading = false;
        if (this.total < this.current_num) {
          this.current_num = this.total;
        }
      });
    } else {
      this.orderLists();
    }
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
      console.log(this.$refs.tableFather.clientHeight);
    },
    handleSelecChange(item) {
      console.log('当前选中的快递公司～', item)
      let obj = this.courierList.find(item2 => item2.id == item)
      this.logistics_type = obj.logistics_type
    },
    async searchExpress(sNo) {
      const res = await deliveryView({
        api: 'admin.order.GetLogistics',
        sNo
      })
      this.courierList = res.data.data.list
    },
    async deliveryViews(item, row) {
      this.searchExpress(item)
      this.detailId = row;
      const res = await deliveryView({
        api: "plugin.group.AdminGroupOrder.DeliveryView",
        sNo: item,
      });
      //this.courierList = res.data.data.express.list;
    },
    async orderLists() {
      const res = await orderList({
        api: "plugin.group.AdminGroupOrder.Index",
        pageSize: this.pageSize,
        pageNo: this.dictionaryNum,
        keyWord: this.inputInfo.orderInfo,
        status: this.inputInfo.state,
        startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
        endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
      });
      console.log(res);
      this.total = res.data.data.total;
      this.tableData = res.data.data.list;
      this.loading = false;
      this.sizeMeth()
    },
    sizeMeth() {
      this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1
      this.current_num =
        this.tableData.length === this.pageSize
          ? this.dictionaryNum * this.pageSize
          : this.total
    },
    // 重置
    reset() {
      (this.inputInfo.orderInfo = null),
        (this.inputInfo.store = null),
        (this.inputInfo.state = null),
        (this.inputInfo.type = null),
        (this.inputInfo.date = null);
    },

    // 查询
    demand() {
      // this.currpage = 1;
      // this.current_num = 10;
      // this.showPagebox = false;
      this.loading = true;
      this.dictionaryNum = 1;
      // this.pageSize = 10
      this.orderLists().then(() => {
        this.loading = false;
        if (this.tableData.length > 5) {
          this.showPagebox = true;
        }
      });
    },

    // 打印
    print() {
      if (this.orderList.length == 0) {
        this.$message({
          message: this.$t("orderLists.qxzdd"),
          type: "warning",
          offset: 100,
        });
      } else {
        let routeData = this.$router.resolve({
          path: "/print",
          query: {
            orderId: this.orderList,
          },
        });

        window.open(routeData.href, "_blank");
      }
    },

    // 批量删除
    delAll() {
      this.$confirm(this.$t("orderLists.scqr"), this.$t("zdata.ts"), {
        confirmButtonText: this.$t("zdata.ok"),
        cancelButtonText: this.$t("zdata.off"),
        type: "warning",
      })
        .then(() => {
          delOrder({
            api: "plugin.group.AdminGroupOrder.DelOrder",
            id: this.orderList,
          }).then((res) => {
            console.log(res);
            if (res.data.code == "200") {
              this.$message({
                message: this.$t("group.scgc"),
                type: "success",
                offset: 100,
              });
              this.orderLists();
            }
          });
        })
        .catch(() => {
          this.$message({
            type: "info",
            message: this.$t("group.yqxsc"),
            offset: 100,
          });
        });
    },
    //关闭订单
    shut() {
      this.$confirm(
        this.$t("group.groupOrderList.gbddtext"),
        this.$t("zdata.ts"),
        {
          confirmButtonText: this.$t("zdata.ok"),
          cancelButtonText: this.$t("zdata.off"),
          type: "warning",
        }
      )
        .then(() => {
          closeOrder({
            api: "plugin.group.AdminGroupOrder.CloseOrder",
            orders: this.orderList,
          }).then((res) => {
            console.log(res);
            if (res.data.code == "200") {
              this.$message({
                message: this.$t("group.scgc"),
                type: "success",
                offset: 100,
              });
              this.orderLists();
              // this.$store.dispatch('orderNum/getOrderCount')
            }
          });
        })
        .catch(() => {
          this.$message({
            type: "info",
            message: this.$t("group.yqxsc"),
            offset: 100,
          });
        });
    },
    //选择一页多少条
    handleSizeChange(e) {
      this.loading = true;
      console.log(e);
      // this.current_num = e
      this.pageSize = e;
      this.orderLists().then(() => {
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
      this.orderLists().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total;
        this.loading = false;
      });
    },
    Refund(row) {
      console.log(row,'------');
      this.ruleForm4.return_money = row.return_money
      this.ruleForm4.orderno = row.orderno
      this.ruleForm4.endDate = row.endDate
      this.dialogVisible4 = true
    },
    // 订单详情
    Details(value) {
      this.$router.push({
        path: "/plug_ins/group/orderDetails",
        query: {
          no: value.orderno,
        },
      });
    },
    //跳转售后管理
    saleOrder() {
      this.$router.push({
        path: "/plug_ins/group/salesReturnList",
      });
    },
    //跳转评价管理
    evaluation() {
      this.$router.push({
        path: "/plug_ins/group/commentList",
      });
    },
    //跳转订单结算
    settle() {
      this.$router.push({
        path: "/plug_ins/group/orderSettlementList",
      });
    },
    // 编辑订单
    Edit(value) {
      this.$router.push({
        path: "/plug_ins/group/editorOrder",
        query: {
          no: value.orderno,
          dictionaryNum: this.dictionaryNum,
          pageSize: this.pageSize,
          radio1: this.radio1,
        },
      });
    },

    // 商品发货
    Delivery(value) {
      //this.dialogVisible3 = true;
      //this.deliveryViews(value.orderno, value.detailId);
      this.$router.push({
        path: '/plug_ins/group/goodsDeliverys',
        query: {
          no: value.orderno,
          dictionaryNum: this.dictionaryNum,
          pageSize: this.pageSize,
          radio1: this.radio1
        }
      })
    },
    // 发货
    determine(formName2) {
      this.$refs[formName2].validate(async (valid) => {
        console.log(this.ruleForm2);
        if (valid) {
          try {
            if (!this.logistics_type && (this.ruleForm2.kuaidi_no.length < 10 || this.ruleForm2.kuaidi_no.length > 30)) {
              this.$message({
                message: this.$t("group.groupOrderList.day10"),
                type: "warning",
                offset: 100,
              });
              return;
            } else {
              let api = 'admin.order.UnifiedShipment'
              let expressid = this.ruleForm2.kuaidi_name
              let courierNum = !this.logistics_type ? this.ruleForm2.kuaidi_no : ''
              let type = this.logistics_type ? '2' : '1'
              let psyInfo = ''
              let orderList = [
                {
                  detailId: this.detailId,
                  num: 1,
                },
              ]
              let list = {
                expressid, //物流公司id
                courierNum, //物流号
                type,	//发货类型 1普通发货 2电子面单 3商家配送
                psyInfo, //配送员信息
                orderList, //订单信息 detailId订单详情id num订单商品数量
              }
              list = JSON.stringify(list)
              deliverySave({ api, list }).then(res => {
                if (res.data.code == "200") {
                  this.$message({
                    message: this.$t("group.groupOrderList.fhcg"),
                    type: "success",
                    offset: 100,
                  });
                  this.orderLists();
                  this.handleClose3();
                }
              });
            }
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
    // 查看物流
    Logistics(value) { },

    // 选框改变
    handleSelectionChange(val) {
      if (val.length == 0) {
        this.is_disabled = true;
      } else {
        this.is_disabled = false;
      }
      console.log(val);
      this.orderList = val.map((item) => {
        return item.orderno;
      });
      this.orderList = this.orderList.join(",");
    },

    // 弹框方法
    dialogShow2(value) {
      this.dialogVisible2 = true;
      kuaidishow({
        api: "plugin.group.AdminGroupOrder.Kuaidishow",
        orderno: value.orderno,
      }).then((res) => {
        console.log(res);
        this.logisticsList = res.data.data.list;
        this.ruleForm.courier_company = res.data.data.list[0].courier_num;
        this.ruleForm.courier_no = res.data.data.list[0].kuaidi_name;
        this.ruleForm.logistics = "";
        this.logisticsTracking = res.data.data.list[0].list;
      });
      console.log(value);
    },

    handleClose2(done) {
      this.dialogVisible2 = false;
    },

    // 导出弹框方法
    dialogShow() {
      this.dialogVisible = true;
    },

    handleClose(done) {
      this.dialogVisible = false;
    },
    handleClose4(done) {
      this.dialogVisible4 = false;
    },
    handleClose3(done) {
      this.dialogVisible3 = false;
      this.$refs.ruleForm2.clearValidate();
      this.ruleForm2.kuaidi_name = "";
      this.ruleForm2.kuaidi_no = "";
    },
    async exportPage() {
      exports(
        {
          api: "plugin.group.AdminGroupOrder.Index",
          pageNo: this.dictionaryNum,
          pageSize: this.pageSize,
          exportType: 1,
          keyWord: this.inputInfo.name,
          status: this.inputInfo.state,
          startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
          endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
        },
        "pageorder"
      );
    },

    async exportAll() {
      exports(
        {
          api: "plugin.group.AdminGroupOrder.Index",
          pageNo: 1,
          pageSize: this.total,
          exportType: 1,
        },
        "allorder"
      );
    },

    async exportQuery() {
      exports(
        {
          api: "plugin.group.AdminGroupOrder.Index",
          pageNo: 1,
          pageSize: this.total,
          exportType: 1,
          keyWord: this.inputInfo.name,
          status: this.inputInfo.state,
          startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
          endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
        },
        "queryorder"
      );
    },
  },
};
