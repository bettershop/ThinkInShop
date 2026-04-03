import {
  getUserInfo,
  goodsStatus,
  saveAddress,
} from "@/api/members/membersList";
import { choiceClass } from "@/api/goods/goodsList";
import cascade from "@/api/publics/cascade";
import {
  helpOrder,
  getGoodsConfigureList,
  Settlement,
} from "@/api/order/orderList";
import ErrorImg from '@/assets/images/default_picture.png'
export default {
  name: "valetOrder",

  data() {
    return {
      laikeCurrencySymbol: '￥',
      user_id: "",
      header: {
        "background-color": "#F4F7F9",
        "font-weight": "bold",
        "border-bottom": "1px solid #E9ECEF",
      },
      conut: 0,
      dialogVisible: false,
      tableRadio: "",
      gradeList: [
        {
          value: "0",
          label: "普通会员",
        },
        {
          value: "30",
          label: "钻石会员",
        },
        {
          value: "25",
          label: "黄金会员",
        },
        {
          value: "24",
          label: "白银会员",
        },
        {
          value: "34",
          label: "测试等级",
        },
      ],
      gradeValue: "",
      search: "",
      userdata: [],

      user_currency_code:'CNY',
      user_currency_symbol:'$',
      user_exchange_rate:1,

      userchangedata: [],
      currpage: 1,
      current_num: "",
      total: 0,
      total2: 0,
      pagination: {
        page: 1,
        pagesize: 10,
      },
      pagesizes: [10, 25, 50, 100],

      dialogVisible2: false,
      ruleForm: {
        name: "",
        tel: "",
        sheng: "",
        city: "",
        quyu: "",
        address: "",
      },
      rules: {
        name: [
          {
            required: true,
            message: "请输入收货人名称",
            trigger: "blur",
          },
        ],
        tel: [
          {
            required: true,
            message: "请输入手机号",
            trigger: "blur",
          },
        ],
        quyu: [
          {
            required: true,
            message: "请选择联系地址",
            trigger: "change",
          },
        ],
        address: [
          {
            required: true,
            message: "请输入详细地址",
            trigger: "blur",
          },
        ],
      },

      ProData: [],
      OldProData: [], //旧数据保存
      dialogVisible3: false,
      tabKey: "0",
      BrandList: [],
      ProList: [],
      ChangeProList: [],
      BrandValue: "",
      sendClass: "",
      Proname: "",
      options: [],

      tableData: [],
      wipe_off: 0,
      sheng: [],
      shengName: "",
      shi: [],
      shiName: "",
      xian: [],
      xianName: "",
      addressInfo: {},
      AddIndex: "",
      cid: "",
      Info: {},

      classList: [],
      brandList: [],
      goodsClass: "",
      brand: "",

      //省市级联集
      shengList: {},
      shiList: {},
      xianList: {},

      // 结算信息
      discount: 0, // 折扣
      total_price: 0, // 总价
      zfreight: 0, // 运费
      discount_price: 0, // 会员折扣
      knock: 0, // 立减
      combined: 0, // 合计支付

      userId: null,
      addressId: null,
      products: [],
      wipeOff: null,

      adr: false,
      itemKey: "",

      isOfflinePayment: 1,//支付方式
    };
  },

  computed: {
    showItem2() {
      let showItem1 =
        (this.currpage - 1) * this.pagination.pagesize +
        this.pagination.pagesize;
      if (showItem1 > this.total) {
        showItem1 = this.total;
      }

      let showItem =
        (this.currpage - 1) * this.pagination.pagesize + 1 + "-" + showItem1;
      return showItem;
    },
    showItem() {
      let showItem1 =
        (this.currpage - 1) * this.pagination.pagesize +
        this.pagination.pagesize;
      if (showItem1 > this.total2) {
        showItem1 = this.total2;
      }

      let showItem =
        (this.currpage - 1) * this.pagination.pagesize + 1 + "-" + showItem1;
      return showItem;
    },
  },

  watch: {
    sendClass(val, nval) {
      if (val.length == 0) {
        this.BrandList = [];
      }
    },

    tableData: {
      handler: function (newValue, oldValue) {
        this.userId = this.tableData[0].user_id;
        this.addressId = this.tableData[0]?.userAddress?.id ? this.tableData[0]?.userAddress?.id : this.addressId;
        console.log("addressId", this.addressId);

        this.discount = newValue[0].gradeDiscount;
        // if(newValue[0].gradeDiscount !== '暂无折扣' && total_price) {
        //     this.discount_price = this.total_price - this.total_price * this.discount
        //     this.combined = this.total_price * this.discount - this.knock
        // }

        if (this.tableData[0].userAddress == null) {
          this.adr = true;
        } else {
          this.adr = false;
        }
        console.log("products222", this.products);

        if (this.ProData.length) {
          Settlement({
            api: "admin.valetOrder.Settlement",
            userId: this.userId,
            addressId: this.addressId,
            products: encodeURIComponent(JSON.stringify(this.products)),
            wipeOff: this.knock,
          }).then((res) => {
            if (res.data.code == "200") {
              this.ProData = res.data.data.goosdList;
              this.total_price = res.data.data.goodsPriceTotal;
              this.zfreight = res.data.data.zfreight;
              this.discount_price = res.data.data.vipDiscountPrice;
              this.combined = res.data.data.payPrice;
            }
          });
        }
      },
      deep: true,
    },
    ProData: {
      handler: function (newValue, oldValue) {
        this.conut = 0
        if (newValue && newValue.length > 0) {

          newValue.forEach(v => {
            this.conut += Number(v.nums)
          })
        } else {
          this.conut = 0
        }
      },
      deep: true,
    },

    discount() {
      if (this.discount === null || this.discount === "暂无折扣") {
        this.discount_price = null;
      } else {
        this.discount_price =
          this.total_price - this.total_price * this.discount;
        this.combined = this.total_price * this.discount - this.knock;
      }
    },

    knock: {
      handler: function () {
        if (this.knock == "") {
          this.knock = 0;
        }
      },
    },
    "ruleForm.quyu": {
      handler: function () {
        if (this.ruleForm.quyu != "") {
          this.$refs.ruleForm.clearValidate("quyu");
        }
      },
    },

    "ruleForm.sheng": {
      handler: function () {
        this.ruleForm.city = "";
        this.ruleForm.quyu = "";
      },
    },

    "ruleForm.city": {
      handler: function () {
        this.ruleForm.quyu = "";
      },
    },
  },

  created() {
    this.laikeCurrencySymbol = this.LaiKeCommon.getDefaultCurrencySymbol();
    this.getSheng();
    this.getUserList();
    this.getProList();
    this.choiceClasss();
    this.goodsStatus();
    if (this.$route.query.id) {
      getUserInfo({
        api: "admin.user.getUserInfo",
        pageSize: this.pagination.pagesize,
        pageNo: this.currpage,
        key: this.$route.query.id,
        vague: 0, //是否模糊查询  1 是 / 0 否
      }).then((res) => {
        this.tableData = res.data.data.list;
      });
    }
  },

  methods: {
    // 图片错误处理
    handleErrorImg(e) {
      console.log('图片报错了', e.target.src);
      e.target.src = ErrorImg
    },
    // 计算运费
    handleChargeCarriage(scope, index) {
      // scope.sort=scope.sort.replace(/^(0+)|[^\d]+/g,'')
      this.ProData[index.$index].freight = scope.freight.replace(/[^\d.]/g, "");
      this.zfreight = this.ProData.reduce((prev, cur) => {
        if (this.ProData.length <= 1) {
          return Number(this.ProData[0].freight);
        } else {
          return Number(prev.freight) + Number(cur.freight);
        }
      }, 0);
    },
    // 失去焦点
    handleChangeFreigh(scope, index) {
      this.products = this.ProData.map((item) => {
        return {
          id: item.attr_id,
          pid: item.goodsId,
          num: item.nums,
          freight: item.freight.length <= 0 ? 0 : Number(item.freight/this.user_exchange_rate).toFixed(2),
        };
      });
      if (scope.freight.length <= 0) {
        this.ProData[index.$index].freight = 0;
      }
      console.log("每个商品的运费", this.products);
      this.wipeBlur();
    },
    // 删除商品
    handleDel(val) {
      this.products = this.ProData.map((item) => {
        console.log("每个商品的运费", item);
        if (val.id !== item.id) {
        } else {
          return {
            id: item.attr_id,
            pid: item.goodsId,
            num: item.nums,
            freight: item.freight,
          };
        }
      });

      this.wipeBlur('is_delete');
    },
    // 获取会员等级列表
    async goodsStatus() {
      const res = await goodsStatus({
        api: "admin.user.goodsStatus",
      });

      let levelList = res.data.data.map((item) => {
        return {
          value: item.id,
          label: item.name,
        };
      });
      levelList.unshift({
        value: "0",
        label: "普通会员",
      });

      this.gradeList = levelList;
    },

    rowKeys(row) {
      return row.attr_id;
    },
    rowKeys2(row) {
      return row.attr_id;
    },
    AddPro() {

      //如果没有选择用户先不弹出商品
      if (this.tableData.length == 0) {
        this.$message({
          message: "请选择用户",
          type: "error",
          offset: 100,
        });
        return;
      }

      this.dialogVisible3 = true;
      this.current_num = this.ProList.length;
      this.currpage = 1;
      this.pagination.pageSize = 10;
      this.pagination = {
        page: 1,
        pagesize: 10,
      };
    },
    AddUser() {
      this.dialogVisible = true;
      this.current_num = this.userdata.length;
      this.currpage = 1;
      this.pagination.pageSize = 10;
      this.pagination = {
        page: 1,
        pagesize: 10,
      };
    },
    handleChange(val, index) {
      if (val == undefined) {
        this.ChangeProList[index].nums = 1;
      } else {
        var row = parseInt(val.toString());
        this.ChangeProList[index].nums = Number(row);
      }
      this.ChangeProList = [...this.ChangeProList];
    },
    handleClose() {
      this.ruleForm.name = "";
      this.ruleForm.tel = "";
      this.ruleForm.sheng = "";
      this.ruleForm.city = "";
      this.ruleForm.quyu = "";
      this.ruleForm.address = "";
      this.dialogVisible2 = false;
      this.$refs["ruleForm"].clearValidate();
    },
    addAddress(index) {
      this.AddIndex = index;
      this.dialogVisible2 = true;
    },
    AddProconfirm() {
      this.itemKey = Math.random();
      this.ProData = this.ChangeProList;
      this.total_price = 0;
      if (this.ProData.length !== 0) {
        this.ProData.forEach((item) => {
          this.total_price += item.price * item.nums;
        });

        // if(this.discount && this.discount !== '暂无折扣') {
        //     this.discount_price = this.total_price - this.total_price * this.discount
        //     this.combined = this.total_price * this.discount - this.knock
        // } else {
        //     this.combined = this.total_price - this.knock
        // }
      }
      this.products = this.ProData.map((item) => {
        return {
          id: item.attr_id,
          pid: item.goodsId,
          num: item.nums,
          freight: item.freight ? item.freight : -1,
        };
      });

      if (this.OldProData.length <= 0) {
      } else {
        this.products.forEach((item1) => {
          this.OldProData.forEach((item2) => {
            if (item1.id === item2.attr_id && item1.pid === item2.goodsId) {
              item1.freight = item2.freight;
            } else {
            }
          });
        });
      }

      console.log(
        "每个商品的运费467",
        this.products,
        this.ProData,
        this.OldProData
      );
      if (this.tableData.length) {
        Settlement({
          api: "admin.valetOrder.Settlement",
          userId: this.userId,
          addressId: this.addressId,
          products: encodeURIComponent(JSON.stringify(this.products)),
          wipeOff: this.knock,
        }).then((res) => {
          this.ProData = res.data.data.goosdList;
          // 原始数据保存

          this.total_price = res.data.data.goodsPriceTotal;
          this.zfreight = res.data.data.zfreight;
          this.discount_price = res.data.data.vipDiscountPrice;
          this.combined = res.data.data.payPrice;
        });
      }
      this.dialogVisible3 = false;
    },
    ChangeProdel(index, value) {
      this.ChangeProList.splice(index, 1);
      this.ProData = this.ProData.filter((item) => {
        return item.attr_id != value.attr_id;
      });
      for (let i = 0; i < this.ProList.length; i++) {
        if (value.attr_id == this.ProList[i].attr_id) {
          var index = i;
        }
      }
      this.products = this.products.filter((item) => {
        return item.id != value.attr_id;
      });
      this.ProList[index].nums = 1;
      this.ChangeProList = this.ProData;
      this.$nextTick(() => {
        this.$refs.goodsListTable.toggleRowSelection(
          this.ProList.find((item) => {
            return value.attr_id == item.attr_id;
          }),
          false
        );
      });
      this.handleDel(value);
    },
    Addconfirm() {
      this.$refs.ruleForm.validate((valid) => {
        if (valid) {
          saveAddress({
            api: "admin.user.saveAddress",
            userId: this.tableData[0].user_id,
            userName: this.ruleForm.name,
            mobile: this.ruleForm.tel,
            address: this.ruleForm.address,
            isDefault: 0,
            place:
              this.ruleForm.sheng +
              "-" +
              this.ruleForm.city +
              "-" +
              this.ruleForm.quyu,
          }).then((res) => {
            if (res.data.code == "200") {
              console.log('556556556', res);
              this.tableData[0].userAddress = this.ruleForm;
              this.tableData = [...this.tableData];
              this.$set(this.tableData)
              this.addressId = res.data.data.id
              this.ruleForm = {
                name: "",
                tel: "",
                sheng: "",
                city: "",
                quyu: "",
                address: "",
              };
              this.handleClose();
            }
          });
        } else {
          console.log("error submit!!");
          return false;
        }
      });
    },
    query() {
      this.currpage = 1;
      //this.pagination.pagesize = 10;
      this.getProList();
    },
    query2() {
      this.currpage = 1;
      //this.pagination.pagesize = 10;
      this.getUserList();
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
          children: [],
        });
      });
    },

    // 根据商品类别id获取商品品牌
    changeProvinceCity(value) {
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
              children: [],
            });
          });
        }
      });
    },

    // 获取产品列表
    async getProList() {
      const res = await getGoodsConfigureList({
        api: "admin.goods.getGoodsConfigureList",
        pageNo: this.currpage,
        pageSize: this.pagination.pagesize,
        cid: this.goodsClass[this.goodsClass.length - 1],
        brandId: this.brand,
        productTitle: this.Proname,
        isSupplier: 2,
      });
      this.ProList = res.data.data.goodsList;
      this.current_num = this.ProList.length * this.currpage;
      this.total2 = Number(res.data.data.total);
      this.ProList.filter((item) => {
        item.nums = 1;
      });
    },
    wipeBlur(e) {

      if (this.tableData.length == 0) {
        this.$message({
          message: "请选择用户",
          type: "error",
          offset: 100,
        });
        this.knock = 0;
        return;
      }


      console.log("每个商品的运费", this.products);
      Settlement({
        api: "admin.valetOrder.Settlement",
        userId: this.userId,
        addressId: this.addressId,
        products: encodeURIComponent(JSON.stringify(this.products)),
        wipeOff: Number(this.knock/this.user_exchange_rate).toFixed(2),
      }).then((res) => {
        if (res.data.code == 200) {

          this.OldProData = res.data.data.goosdList;
          this.total_price = res.data.data.goodsPriceTotal;
          this.zfreight = res.data.data.zfreight;
          this.discount_price = res.data.data.vipDiscountPrice;
          this.combined = res.data.data.payPrice;

          if (e == 'is_delete') {
            this.$message({
              message: "删除成功",
              type: "success",
              offset: 102,
            });
            this.knock = 0;
            return;
          }
        } else {
          this.$message({
            message: res.data.message,
            type: "error",
            offset: 102,
          });
        }
      });
    },
    getUserInfo(type) {
    },
    async getUserList() {
      const res = await getUserInfo({
        api: "admin.user.getUserInfo",
        pageSize: this.pagination.pagesize,
        pageNo: this.currpage,
        key: this.search,
        grade: this.gradeValue,
      });
      this.total = Number(res.data.data.total);
      this.userdata = res.data.data.list;
      this.current_num = this.userdata.length;
      if (this.total < this.current_num) {
        this.current_num = this.total;
      }
    },
    Reset() {
      this.gradeValue = "";
      this.search = ""; //会员ID,名称，手机号
      this.Proname = "";
      this.BrandValue = "";
      this.BrandList = [];
      this.brandList = [];
      this.sendClass = "";
      (this.cid = ""), (this.goodsClass = ""), (this.brand = "");
    },
    confirm() {
      if (this.userchangedata == null || this.userchangedata.length == 0) {
        this.$message({
          message: "请选择用户",
          type: "error",
          offset: 100,
        });
        return;
      }
      this.user_id = "";
      this.dialogVisible = false;
      this.tableData = [];
      this.tableData.push(this.userchangedata);

      console.log(this.userchangedata);
      this.user_currency_code = this.userchangedata.currency_code
      this.user_currency_symbol = this.userchangedata.currency_symbol
      this.user_exchange_rate = this.userchangedata.exchange_rate

    },
    handleSelectionChange(e) {
      this.tableRadio = e;
      this.userchangedata = e;
    },
    handleSelectionChange2(e) {
      this.ChangeProList = e;
    },
    //选择一页多少条
    handleSizeChange(e) {
      this.pagination.pagesize = e;
      if (this.dialogVisible3) {
        this.getProList();
      } else {
        this.getUserList();
      }
    },
    //点击上一页，下一页
    handleCurrentChange(e) {
      this.currpage = e;
      if (this.dialogVisible3) {
        this.getProList();
      } else {
        this.getUserList();
      }
    },
    cancel() {
      window.location.href = "javascript:history.go(-1)";
    },
    getQueryVariable(variable) {
      var query = window.location.search.substring(1);
      var vars = query.split("&");
      for (var i = 0; i < vars.length; i++) {
        var pair = vars[i].split("=");
        if (pair[0] == variable) {
          return pair[1];
        }
      }
      return false;
    },

    // 获取省级
    async getSheng() {
      const res = await cascade.getSheng();
      this.shengList = res.data.data;
    },

    // 获取市级
    async getShi(sid, flag) {
      const res = await cascade.getShi(sid);
      this.shiList = res.data.data;
      if (!flag) {
        this.ruleForm.shi = "";
        this.ruleForm.xian = "";
      }
    },

    // 获取县级
    async getXian(sid, flag) {
      const res = await cascade.getXian(sid);
      this.xianList = res.data.data;
      if (!flag) {
        this.ruleForm.xian = "";
      }
    },

    //省市级联回显
    async cascadeAddress() {
      //省市级联
      for (const sheng of this.shengList) {
        if (sheng.districtName === this.ruleForm.sheng) {
          await this.getShi(sheng.id, true);
          for (const shi of this.shiList) {
            if (shi.districtName === this.ruleForm.shi) {
              await this.getXian(shi.id, true);
              break;
            }
          }
          break;
        }
      }
    },

    submitForm() {
      if (this.tableData.length == 0) {
        this.$message({
          message: "请选择用户",
          type: "error",
          offset: 100,
        });
      } else if (this.ProData.length == 0) {
        this.$message({
          message: "请选择商品",
          type: "error",
          offset: 100,
        });

      } else if (this.adr == true) {
        this.$message({
          message: "请添加收货地址",
          type: "error",
          offset: 100,
        });
      } else {
        this.products.map((item) => {
          if (item.freight <= 0) {
            item.freight = 0;
          }
        });
        // this.products.map((item) => {
        //   this.ProData.forEach((item2) => {
        //     if (item.id === item2.attr_id && item.pid === item2.goodsId) {
        //       item.freight = item2.freight;
        //     }
        //   });
        // });
        console.log("XXXXXXXXX957", this.products, this.ProData);
        helpOrder({
          api: "admin.order.helpOrder",
          userId: this.userId,
          addressId: this.addressId,
          isOfflinePayment: this.isOfflinePayment,
          products: encodeURIComponent(JSON.stringify(this.products)),
          wipeOff: Number(this.knock/this.user_exchange_rate).toFixed(2),
          // products: encodeURIComponent(JSON.stringify(this.products)),
          // wipeOff: this.discount_price
          //   ? this.discount_price + this.knock
          //   : this.knock,
        }).then((res) => {
          if (res.data.code == "200") {
            this.$message({
              message: "成功",
              type: "success",
              offset: 100,
            });
            this.$router.go(-1);
          }
        });
      }
    },
  },
};
