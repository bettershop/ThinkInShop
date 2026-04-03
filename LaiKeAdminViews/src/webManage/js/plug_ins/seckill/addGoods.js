import {
  getProList,
  get_class,
  addPro,
  getProAttrList,
} from "@/api/plug_ins/seckill";
import { mixinstest } from "@/mixins/index";

export default {
  name: "addGoods",
  mixins: [mixinstest],

  data() {
    return {
      pltext: this.$t("seckill.addGoods.qsrbfb"),
      ruleForm: {
        price_type: 0,
        price: "",
        // shelves_num: '',
        activity_time: "",
      },

      rules: {
        price: [
          {
            required: true,
            message: this.$t("seckill.addGoods.qtxmsjg"),
            trigger: "blur",
          },
        ],
        // shelves_num: [
        //     { required: true, message: '请填写上架库存', trigger: 'blur' }
        // ],
        activity_time: [
          {
            required: true,
            message: this.$t("seckill.addGoods.qxzhdsj"),
            trigger: "change",
          },
        ],
      },

      classList: [],
      brandList: [],
      inputInfo: {
        goodsClass: "",
        brand: "",
        name: "",
      },

      idList: [],

      tableData: [],

      priceList: [
        {
          value: 0,
          name: this.$t("seckill.addGoods.bfb"),
        },
        {
          value: 1,
          name: this.$t("seckill.addGoods.gdz"),
        },
      ],

      text: "%",

      totlePrice: 0,

      // 弹框数据
      dialogVisible3: false,
      ProList: [],

      goodsJson: [],
      goodsJson1: [],

      goodsNum: null,
      goodsId: null,
      attr: [],
    };
  },

  created() {
    this.getProLists();
    this.get_classs();
  },

  watch: {
    "ruleForm.price": {
      handler: function () {
        if (this.ruleForm.price_type == 0) {
          if (this.ruleForm.price >= 100) {
            this.ruleForm.price = 99;
          } else if (this.ruleForm.price < 0) {
            this.ruleForm.price = 1;
          }
        }
      },
    },
    "ruleForm.price_type": {
      handler: function () {
        if (this.ruleForm.price_type == 0) {
          this.pltext = this.$t("seckill.addGoods.qsrbfb");
          this.text = "%";
          // if(this.ruleForm.price >= 100) {
          //     this.ruleForm.price = 99
          // }
          this.ruleForm.price = "";
        } else {
          this.pltext = this.$t("seckill.addGoods.qsrmsj");
          this.text = this.$t("seckill.addGoods.yuan");
          this.ruleForm.price = "";
        }
      },
    },

    'ruleForm.shelves_num': {
      handler: function () {
        if (parseInt(this.ruleForm.shelves_num) == 0) {
          this.ruleForm.shelves_num = ''
        }

      }
    },

    "inputInfo.goodsClass": {
      handler: function () {
        this.inputInfo.brand = "";
      },
    },

  },

  methods: {
    getDate(item) {
      item.goodsNum = this.oninput2(item.goodsNum)
      if (item.goodsNum > item.stockNum) {
        this.warnMsg('上架库存不能大于库存！！')
        item.goodsNum = item.stockNum
      }
      if (this.goodsJson1 && this.goodsJson1.length > 0) {
        this.goodsJson1.forEach(i => {
          if (i.attr[0].attrId === item.attrId) {
            i.goodsNum = item.goodsNum;
          }
        })
      }
    },
    getRowKey(row) {
      return row.attrId;
    },
    async getProLists() {
      const res = await getProList({
        api: "plugin.sec.AdminSec.getProList",
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        myClass:
          this.inputInfo.goodsClass[this.inputInfo.goodsClass.length - 1],
        myBrand: this.inputInfo.brand,
        proName: this.inputInfo.name,
        labelId: this.$route.query.id,
      });
      console.log(res);
      this.current_num = 10;
      this.total = res.data.data.total;
      this.tableData = res.data.data.res;
      this.tableData.forEach(v => {
        this.$set(v, 'goodsNum', 1)
      })
      if (this.total < this.current_num) {
        this.current_num = this.total;
      }
      if (this.total == 0) {
        this.showPagebox = false;
      } else {
        this.showPagebox = true;
      }
    },

    reset() {
      (this.inputInfo.goodsClass = ""),
        (this.inputInfo.brand = ""),
        (this.inputInfo.name = "");
    },

    demand() {
      this.currpage = 1;
      this.current_num = 10;
      this.dictionaryNum = 1;
      this.getProLists();
    },

    //选择一页多少条
    handleSizeChange(e) {
      this.pageSize = e;
      this.getProLists().then(() => {
        this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1;
        this.current_num =
          this.tableData.length === this.pageSize
            ? this.dictionaryNum * this.pageSize
            : this.total;
      });
    },

    //点击上一页，下一页
    handleCurrentChange(e) {
      this.dictionaryNum = e;
      this.currpage = (e - 1) * this.pageSize + 1;
      this.getProLists().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total;
      });
    },

    // 弹框方法 - 多规格设置
    async specifications(value) {
      console.log(value);
      const res = await getProAttrList({
        api: "plugin.sec.AdminSec.getProAttrList",
        goodsId: value.id,
      });
      this.ProList = res.data.data.list;
      this.ProList = this.ProList.map((item) => {
        return {
          ...item,
          upNum: item.num,
        };
      });
      if (this.goodsJson.length > 0) {
        let list = this.goodsJson.filter((item2) => {
          return item2.goodsId == value.id;
        });
        if (list.length > 0) {
          list[0].attr.forEach((item1) => {
            this.ProList.forEach((item2, index) => {
              if (item2.id === item1.attrId) {
                this.ProList[index].upNum = item1.num;
              }
            });
          });
        }
      }
      console.log(res);
      this.dialogVisible3 = true;
    },

    AddProconfirm() {
      this.goodsId = this.ProList[0].goodsId;
      this.attr = this.ProList.map((item) => {
        return {
          attrId: item.id,
          num: item.upNum,
        };
      });

      this.attr.filter((item) => {
        this.goodsNum += parseInt(item.num);
      });

      if (
        this.goodsJson.some((item) => {
          return item.goodsId == this.goodsId;
        })
      ) {
        this.goodsJson.filter((item, index) => {
          if (item.goodsId == this.goodsId) {
            this.goodsJson.splice(index, 1);
          }
        });
        this.goodsJson.push({
          goodsId: this.goodsId,
          goodsNum: this.goodsNum,
          attr: this.attr,
        });
      } else {
        this.goodsJson.push({
          goodsId: this.goodsId,
          goodsNum: this.goodsNum,
          attr: this.attr,
        });
      }

      console.log(this.goodsJson);

      this.goodsNum = null;
      this.goodsId = null;
      this.attr = [];
      this.dialogVisible3 = false;
      this.$message({
        message: this.$t("zdata.tjcg"),
        type: "success",
        offset: 100,
      });
    },

    tableHeaderColor({ row, column, rowIndex, columnIndex }) {
      if (rowIndex === 0) {
        return "background-color: #f4f7f9;";
      }
    },

    // 获取商品类别
    async get_classs() {
      const res = await get_class({
        api: "admin.goods.choiceClass",
      });
      res.data.data.list.class_list[0].forEach((item, index) => {
        let obj = item;
        this.classList.push({
          value: obj.cid,
          label: obj.pname,
          index: index,
          children: [],
        });
      });
    },

    // 根据商品类别id获取商品品牌
    changeProvinceCity(value) {
      get_class({
        api: "admin.goods.choiceClass",
        classId: value.length > 1 ? value[value.length - 1] : value[0],
      }).then((res) => {
        // let num = this.$refs.myCascader.getCheckedNodes()[0].data.index;
        console.log('resres', res)
        this.brandList = res.data.data.list.brand_list;
        if (res.data.data.list.class_list[0].length !== 0) {
          this.$refs.myCascader.getCheckedNodes()[0].data.children = [];
          res.data.data.list.class_list[0].forEach((item, index) => {
            let obj = item;
            this.$refs.myCascader.getCheckedNodes()[0].data.children.push({
              value: obj.cid,
              label: obj.pname,
              index: index,
              children: [],
            });
          });
        }
      });
    },

    // 选框改变
    handleSelectionChange(val) {
      this.totlePrice = 0;
      this.idList = val.map((item) => {
        return item.id;
      });

      //val数组price最小值
      this.totlePrice = Math.min(...val.map(item => item.price));

      if (
        this.ruleForm.price_type == 1 &&
        parseInt(this.ruleForm.price) > this.totlePrice
      ) {
        this.ruleForm.price = this.totlePrice;
      }

      this.goodsJson1 = val.map((item) => {
        return {
          goodsId: item.id,
          // goodsNum: item.stockNum,
          goodsNum: item.goodsNum,
          attr: [
            {
              attrId: item.attrId,
              num: item.stockNum,
            },
          ],
        };
      });
      console.log(this.goodsJson1);
    },

    submitForm(formName) {

      if (this.ruleForm.price_type == 0 && this.ruleForm.price == 0) {
        this.$message({
          message: this.$t("seckill.addGoods.bfbbn"),
          type: "error",
          offset: 100,
        });
        return;
      }
      // this.goodsJson.filter((item, index) => {
      //   if (
      //     this.goodsJson1.some((items) => {
      //       return item.goodsId == items.goodsId;
      //     })
      //   ) {
      //     this.goodsJson1.filter((item1, index1) => {
      //       if (item1.goodsId == item.goodsId) {
      //         this.goodsJson1.splice(index1, 1);
      //       }
      //     });
      //   }
      // });
      // this.goodsJson = this.goodsJson.concat(this.goodsJson1);
      // this.goodsJson.filter((item, index) => {
      //   if (
      //     !this.idList.some((items) => {
      //       return item.goodsId == items;
      //     })
      //   ) {
      //     this.goodsJson.splice(index, 1);
      //   }
      // });

      if (!this.goodsJson1 || !this.goodsJson1.length) {
        this.$message({
          message: this.$t("seckill.addGoods.qxzsp"),
          type: "error",
          offset: 100,
        });
        return;
      }
      this.ProList.forEach(item => {
        this.goodsJson1.forEach(item2 => {
          if (item.goodsId == item2.id) {
            item2.attr.forEach(item3 => {
              if (item3.attrId == item.id) {
                item3.num = item.upNum
              }
            })
          }
        })
      })
      console.log('419419419  upNum', this.goodsJson1, this.idList);
      this.goodsJson1.forEach(v => {
        if (v.attr && v.attr[0]) {
          this.$set(v, 'attrId', v.attr[0].attrId)
          delete v.attr
        }
      })
      if (!this.ruleForm.activity_time) {
        this.$message({
          message: this.$t("seckill.addGoods.qxzhdsj"),
          type: "error",
          offset: 100
        });
        return;
      }
      this.$refs[formName].validate(async (valid) => {
        if (valid) {
          try {
            addPro({
              api: "plugin.sec.AdminSec.addPro",
              id: this.$route.query.id,
              goodsJson: encodeURIComponent(JSON.stringify(this.goodsJson1)),
              priceType: this.ruleForm.price_type,
              price: this.ruleForm.price,
              startDate: this.ruleForm.activity_time[0],
              endDate: this.ruleForm.activity_time[1],
            }).then((res) => {
              if (res.data.code == "200") {
                this.$message({
                  message: this.$t("seckill.cg"),
                  type: "success",
                  offset: 100,
                });
                this.$router.push({
                  path: "/plug_ins/seckill/goodsList",
                  query: {
                    id: this.$route.query.id,
                  },
                });
                //   this.$router.go(-1)
              }
            });
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

    oninput(num, limit) {
      var str = num;
      var len1 = str.substr(0, 1);
      var len2 = str.substr(1, 1);
      //如果第一位是0，第二位不是点，就用数字把点替换掉
      if (str.length > 1 && len1 == 0 && len2 != ".") {
        str = str.substr(1, 1);
      }
      //第一位不能是.
      if (len1 == ".") {
        str = "";
      }
      //限制只能输入一个小数点
      if (str.indexOf(".") != -1) {
        var str_ = str.substr(str.indexOf(".") + 1);
        if (str_.indexOf(".") != -1) {
          str = str.substr(0, str.indexOf(".") + str_.indexOf(".") + 1);
        }
      }
      //正则替换
      str = str.replace(/[^\d^\.]+/g, ""); // 保留数字和小数点
      if (limit / 1 === 1) {
        str = str.replace(/^\D*([0-9]\d*\.?\d{0,1})?.*$/, "$1"); // 小数点后只能输 1 位
      } else {
        str = str.replace(/^\D*([0-9]\d*\.?\d{0,2})?.*$/, "$1"); // 小数点后只能输 2 位
      }

      if (
        this.ruleForm.price_type == 1 &&
        this.totlePrice !== 0 &&
        parseInt(this.ruleForm.price) > this.totlePrice
      ) {
        str = this.totlePrice;
      }
      return str;
    },

    oninput2(num) {
      var str = num;
      str = str.replace(/[^\.\d]/g, "");
      str = str.replace(".", "");

      return str;
    },
  },
};
