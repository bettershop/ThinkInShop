import { getStockDetailInfo, addStock } from "@/api/goods/inventoryManagement";
import { exports } from "@/api/export/index";
import { mixinstest } from "@/mixins/index";
import { getStorage, setStorage } from "@/utils/storage";
import ErrorImg from "@/assets/images/default_picture.png";

export default {
  name: "InventoryWarning",
  mixins: [mixinstest],
  data() {
    return {
      laikeCurrencySymbol:'￥',
      languages: null,
      lang_code: null,

      radio1: this.$t("inventoryManagement.ckyj"),

      sortCriteria: '',
      IsItDescendingOrder1: '',
      IsItDescendingOrder: '',

      inputInfo: {
        storeName: null,
        lang_code: null,
        goodsName: null,
        date: null,
      },
      tableData: [],
      loading: true,

      // 弹框数据
      dialogVisible2: false,
      ruleForm2: {
        addNum: "",
        total_num: null,
        num: null,
      },
      rules2: {
        addNum: [
          {
            required: true,
            message: this.$t("inventoryManagement.qsrzjckl"),
            trigger: "blur",
          },
        ],
      },

      // table高度
      tableHeight: null,
      menuId: "",
      // 导出弹框数据
      dialogVisible: false,
    };
  },

  created() {
    this.laikeCurrencySymbol = this.LaiKeCommon.getDefaultCurrencySymbol();
    this.inputInfo.lang_code = this.LaiKeCommon.getUserLangVal();
    this.getStockDetailInfos();
    this.getLanguage();
  },

  mounted() {
    this.$nextTick(function () {
      this.getHeight();
    });
    window.addEventListener("resize", this.getHeight(), false);
  },

  methods: {
    async getLanguage() {
      try {
        const result = await this.LaiKeCommon.getLanguages();
        this.languages = result.data.data;
      } catch (error) {
        console.error('获取语种列表失败:', error);
      }
    },

    // 销量 升序、降序
    clickSort(type) {
      const statusArr = ['asc', 'desc']
      let index = statusArr.findIndex(v => v === this.IsItDescendingOrder)
      if (index == statusArr.length - 1) {
        index = 0
      } else {
        index = (index + 1) % statusArr.length
      }
      this.sortCriteria = type
      this.IsItDescendingOrder = statusArr[index]
      this.IsItDescendingOrder1 = ''

      this.getStockDetailInfos()
    },
    // 时间  升序、降序
    clickSort1(type) {
      const statusArr = ['asc', 'desc']
      let index = statusArr.findIndex(v => v === this.IsItDescendingOrder1)
      if (index == statusArr.length - 1) {
        index = 0
      } else {
        index = (index + 1) % statusArr.length
      }
      this.sortCriteria = type
      this.IsItDescendingOrder = ''
      this.IsItDescendingOrder1 = statusArr[index]
      this.getStockDetailInfos()
    },
    // 图片错误处理
    handleErrorImg(e) {
      console.log("图片报错了", e.target.src);
      e.target.src = ErrorImg;
    },
    // 获取table高度
    getHeight() {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight;
      console.log(this.$refs.tableFather.clientHeight);
    },
    async getStockDetailInfos() {
      const res = await getStockDetailInfo({
        api: "admin.goods.getStockDetailInfo",
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        mchName: this.inputInfo.storeName,
        productTitle: this.inputInfo.goodsName,
        lang_code: this.inputInfo.lang_code,
        startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
        endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
        type: 2,
        sortCriteria: this.sortCriteria,
        sort: this.IsItDescendingOrder || this.IsItDescendingOrder1
      });
      this.total = res.data.data.total;
      this.tableData = res.data.data.list;
      this.loading = false;
      if (this.total < this.current_num) {
        this.current_num = this.total;
      }
      console.log(res);
    },

    // 重置
    reset() {
      this.inputInfo.storeName = null;
      this.inputInfo.goodsName = null;
      this.inputInfo.lang_code = null;
      this.inputInfo.date = null;
    },

    // 查询
    demand() {
      // this.currpage = 1;
      // this.current_num = 10;
      this.showPagebox = false;
      this.loading = true;
      this.dictionaryNum = 1;
      this.getStockDetailInfos().then(() => {
        this.loading = false;
        if (this.tableData.length > 5) {
          this.showPagebox = true;
        }
      });
    },

    // 预警记录
    warningRecord(value) {
      this.$router.push({
        path: "/goods/InventoryWarnings/warningRecord",
        query: {
          id: value.goodsId,
          attrId: value.attrId,
        },
      });
    },

    // 添加库存
    addInventory(value) { },

    //选择一页多少条
    handleSizeChange(e) {
      this.loading = true;
      console.log(e);
      // this.current_num = e
      this.pageSize = e;
      this.getStockDetailInfos().then(() => {
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
      this.getStockDetailInfos().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total;
        this.loading = false;
      });
    },

    // 弹框方法
    dialogShow2(value) {
      console.log(value);
      this.dialogVisible2 = true;
      this.ruleForm2.total_num = value.total_num;
      this.ruleForm2.num = value.num;
      this.id = value.attrId;
      this.pid = value.goodsId;
    },

    handleClose2(done) {
      this.dialogVisible2 = false;
      this.$refs["ruleForm2"].clearValidate();
      this.ruleForm2.addNum = "";
      this.ruleForm2.total_num = null;
      this.ruleForm2.num = null;
    },

    // 添加库存
    determine(formName2) {
      this.$refs[formName2].validate(async (valid) => {
        if (valid) {
          try {
            addStock({
              api: "admin.goods.addStock",
              id: this.id,
              pid: this.pid,
              addNum: this.ruleForm2.addNum,
            }).then((res) => {
              console.log(res);
              if (res.data.code == "200") {
                this.$message({
                  message: this.$t("zdata.tjcg"),
                  type: "success",
                  offset: 102,
                });
                this.dialogVisible2 = false;
                this.getStockDetailInfos();
                this.handleClose2();
              }
            });
          } catch (error) {
            this.$message({
              message: "请输入增加库存量",
              type: "error",
              offset: 102,
            });
          }
        }
      });
    },

    oninput2(num) {
      var str = num;
      str = str.replace(/[^\.\d]/g, "");
      str = str.replace(".", "");

      if (Number(str) === 0 && str != "") {
        return 1;
      }

      return str;
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
          api: "admin.goods.getStockDetailInfo",
          pageNo: this.dictionaryNum,
          pageSize: this.pageSize,
          exportType: 1,
          type: 2,
          mchName: this.inputInfo.storeName,
          productTitle: this.inputInfo.goodsName,
          startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
          endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
        },
        "Stock"
      );
    },

    async exportAll() {
      exports(
        {
          api: "admin.goods.getStockDetailInfo",
          pageNo: 1,
          pageSize: 999999,
          exportType: 1,
          type: 2,
        },
        "Stock"
      );
    },

    async exportQuery() {
      exports(
        {
          api: "admin.goods.getStockDetailInfo",
          pageNo: 1,
          pageSize: this.total,
          exportType: 1,
          mchName: this.inputInfo.storeName,
          productTitle: this.inputInfo.goodsName,
          startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
          endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
          type: 2,
        },
        "Stock"
      );
    },
  },
};
