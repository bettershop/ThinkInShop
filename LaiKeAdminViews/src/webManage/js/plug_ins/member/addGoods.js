import { getMenberList, addMemberPro } from "@/api/plug_ins/members";
import { choiceClass } from "@/api/goods/goodsList";
import { mixinstest } from "@/mixins/index";
import ErrorImg from "@/assets/images/default_picture.png";

export default {
  name: "integralGoodsList",
  mixins: [mixinstest],

  data() {
    return {
      inputInfo: {
        name: "",
        goodsClass: "",
        brand: "",
      },
      classList: [],
      brandList: [],
      button_list: [],
      tableData: [],
      loading: true,
      is_disabled: true,
      idList: [],
      menuId: "",
      ErrorImg: ErrorImg,
      tableSelect: [],
      ProList: [],

      dialogVisible3: false,
    };
  },

  created() {
    this.proList();
    this.choiceClasss();
  },

  mounted() {
    this.$nextTick(function () {
      this.getHeight();
    });
    window.addEventListener("resize", this.getHeight(), false);
  },

  methods: {
    // 图片错误处理
    handleErrorImg(e) {
      console.log("图片报错了", e.target.src);
      e.target.src = ErrorImg;
    },
    allDel() {
      this.ProList = [];
      this.tableSelect = [];
      this.$refs.table.clearSelection();
      this.dialogVisible3 = false;
    },
    async proList() {
      const res = await getMenberList({
        api: "plugin.member.AdminMember.ProList",
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        cid: this.inputInfo.goodsClass[this.inputInfo.goodsClass.length - 1],
        brandId: this.inputInfo.brand,
        productTitle: this.inputInfo.name,
        status: 2,
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
    // 获取商品类别
    async choiceClasss() {
      const res = await choiceClass({
        api: "admin.goods.choiceClass",
      });
      res.data.data.list.class_list[0].forEach((item, index) => {
        let obj = item;
        this.classList.push({
          value: obj.cid,
          label: obj.pname,
          index: index,
          level: obj.level,
          children: [],
        });
      });
    },

    // 根据商品类别id获取商品品牌
    changeProvinceCity(value) {
      this.inputInfo.brand = "";
      this.brandList = [];
      choiceClass({
        api: "admin.goods.choiceClass",
        classId: value.length > 1 ? value[value.length - 1] : value[0],
      }).then((res) => {
        let num = this.$refs.myCascader.getCheckedNodes()[0].data.index;
        this.brandList = res.data.data.list.brand_list;
        if (res.data.data.list.class_list[0].length !== 0) {
          this.$refs.myCascader.getCheckedNodes()[0].data.children = [];
          res.data.data.list.class_list[0].forEach((item, index) => {
            let obj = item;
            this.$refs.myCascader.getCheckedNodes()[0].data.children.push({
              value: obj.cid,
              label: obj.pname,
              index: index,
              level: obj.level,
              children: [],
            });
          });
        }
      });
    },

    // 加载所有分类
    async allClass(value) {
      for (let i = 0; i < value.length - 1; i++) {
        if (this.classIds.includes(value[i].value)) {
          choiceClass({
            api: "admin.goods.choiceClass",
            classId: value[i].value,
          }).then((res) => {
            if (res.data.data.list.class_list.length !== 0) {
              this.brandList = res.data.data.list.brand_list;
              res.data.data.list.class_list[0].forEach((item, index) => {
                let obj = item;
                value[i].children.push({
                  value: obj.cid,
                  label: obj.pname,
                  index: index,
                  children: [],
                });
              });

              this.allClass(value[i].children);
            }
          });
        } else {
          continue;
        }
      }
    },
    // 获取table高度
    getHeight() {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight;
      console.log(this.$refs.tableFather.clientHeight);
    },

    // 重置
    reset() {
      this.inputInfo.name = "";
      this.inputInfo.goodsClass = "";
      this.inputInfo.brand = "";
      this.brandList = [];
    },

    // 查询
    demand() {
      console.log(this.inputInfo.goodsClass);
      this.currpage = 1;
      this.current_num = 10;
      this.showPagebox = false;
      this.loading = true;
      this.dictionaryNum = 1;
      this.proList().then(() => {
        this.loading = false;
        if (this.tableData.length > 5) {
          this.showPagebox = true;
        }
      });
    },

    // 选框改变
    handleSelectionChange(val) {
      console.log(val);
      // this.idList = val

      this.tableSelect = val;
      this.ProList = this.tableSelect;
    },

    getRowKey(row) {
      return row.id;
    },

    view() {
      this.dialogVisible3 = true;
    },

    ChangeProdel(index, value) {
      this.ProList.splice(index, 1);
      // this.$refs.table.toggleRowSelection(this.ProList.find((item) => {
      //     return value.id == item.id
      // }),false)
      this.$refs.table.toggleRowSelection(value);
    },

    //选择一页多少条
    handleSizeChange(e) {
      this.loading = true;
      console.log(e);
      this.pageSize = e;
      this.proList().then(() => {
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
      this.proList().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total;
        this.loading = false;
      });
    },

    submitForm() {
      if (this.tableSelect.length == 0) {
        this.$message({
          type: "error",
          message: this.$t("member.addGoods.qxzsp"),
          offset: 100,
        });
        return;
      }
      this.idList = this.tableSelect
        .map((item) => {
          return item.id;
        })
        .join(",");
      addMemberPro({
        api: "plugin.member.AdminMember.AddMemberPro",
        proIds: this.idList,
      }).then((res) => {
        if (res.data.code == "200") {
          console.log(res);
          this.$message({
            type: "success",
            message: this.$t("member.cg"),
            offset: 100,
          });
          this.$router.go(-1);
        } else {
          this.$message({
            type: "error",
            message: res.message,
            offset: 100,
          });
        }
      });
    },
  },
};
