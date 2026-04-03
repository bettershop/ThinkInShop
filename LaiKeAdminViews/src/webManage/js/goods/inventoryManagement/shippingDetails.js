import { getStockDetailInfo } from "@/api/goods/inventoryManagement";
import { exports } from "@/api/export/index";
import { mixinstest } from "@/mixins/index";
import { getStorage, setStorage } from "@/utils/storage";
import { getButton } from "@/api/layout/information";
import ErrorImg from "@/assets/images/default_picture.png";

export default {
  name: "shippingList",
  mixins: [mixinstest],
  data() {
    return {
      laikeCurrencySymbol:'￥',
      languages: null,
      lang_code: null,

      radio1: this.$t("inventoryManagement.chxq"),

      inputInfo: {
        storeName: null,
        goodsName: null,
        lang_code: null,
        date: null,
      },

      tableData: [],
      loading: true,
      // table高度
      tableHeight: null,
      button_list: [],
      // 导出弹框数据
      dialogVisible: false,
      menuId: "",
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
        lang_code: this.inputInfo.lang_code,
        productTitle: this.inputInfo.goodsName,
        startDate: this.inputInfo.date ? this.inputInfo.date[0] : null,
        endDate: this.inputInfo.date ? this.inputInfo.date[1] : null,
        type: 1,
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
    warningRecord(value) {},

    // 添加库存
    addInventory(value) {},

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
          type: 1,
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
          type: 1,
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
          type: 1,
        },
        "Stock"
      );
    },
  },
};
