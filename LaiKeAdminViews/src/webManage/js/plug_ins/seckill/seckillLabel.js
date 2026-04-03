import {
  labelIndex,
  labelSwitch,
  sortMove,
  labelTop,
  delLabel,
  addLabel,
} from "@/api/plug_ins/seckill";
import { mixinstest } from "@/mixins/index";
import { getStorage } from "@/utils/storage";
import { getButton } from "@/api/layout/information";
export default {
  name: "seckillLabel",
  mixins: [mixinstest],

  data() {
    return {
      routerList: JSON.parse(sessionStorage.getItem("tabRouter")),
      radio1: "1",
      menuId: "",
      tableData: [],

      // 弹框数据
      dialogVisible: false,
      title: "",
      id: null,
      ruleForm: {
        name: "",
        title: "",
      },
      rules: {
        name: [
          {
            required: true,
            message: this.$t("seckill.seckillLabel.qsrbq"),
            trigger: "blur",
          },
        ],
      },
      button_list: [],
      previousId: null,

      loading: true,
      // table高度
      tableHeight: null,
      flag: true,
    };
  },

  created() {
    this.labelIndexs();
    this.getButtons();
    if (getStorage("laike_admin_userInfo").mchId == 0) {
      this.$message({
        type: "error",
        message: this.$t("plugInsSet.plugInsList.qtjdp"),
        offset: 100,
      });
      this.$router.push("/mall/fastBoot/index");
    }
  },

  mounted() {
    this.$nextTick(function () {
      this.getHeight();
    });
    window.addEventListener("resize", this.getHeight(), false);
  },

  methods: {
    async labelIndexs() {
      this.tableData
      const res = await labelIndex({
        api: "plugin.sec.AdminSec.index",
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
      });
      // if(res.data.code==51008){
      //     this.$router.push('/mall/fastBoot/index')
      // }
      console.log(res);
      this.current_num = 10;
      this.total = res.data.data.total;
      this.tableData = res.data.data.list;
      this.loading = false;
      if (this.total < this.current_num) {
        this.current_num = this.total;
      }
    },
    //获取按纽权限
    async getButtons() {
      let route = getStorage("route");
      route.forEach((item) => {
        if (item.path == "seckill") {
          item.children.forEach((e) => {
            if (e.path == "seckillLabel") {
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
      this.button_list = buttonList.data.data.map((item) => {
        return item.title;
      });
    },
    // 获取table高度
    getHeight() {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight;
    },

    // 是否显示
    switchs(value) {
      // if (!this.flag) return
      // this.flag = false

      labelSwitch({
        api: "plugin.sec.AdminSec.labelSwitch",
        id: value.id,
      }).then((res) => {
        if (res.data.code == "200") {
          this.$message({
            type: "success",
            message:
              value.is_show == 0
                ? this.$t("zdata.gbcg")
                : this.$t("zdata.kqcg"),
            offset: 100,
          });
        }
        this.labelIndexs();
      }).finally(() => {
        setTimeout(() => {
          // this.flag = true
        }, 1500)
      });
    },

    // 移动
    moveUp(value) {
      if (!this.flag) return
      this.flag = false

      if (value == 0 && this.currpage == 1) {
        sortMove({
          api: "plugin.sec.AdminSec.sortMove",
          moveId: this.tableData[value + 1].id,
          moveId1: this.tableData[value].id,
        }).then((res) => {
          if (res.data.code == "200") {
            this.$message({
              type: "success",
              message: this.$t("seckill.cg"),
              offset: 100,
            });
          }
          this.labelIndexs();
        }).finally(() => {
          setTimeout(() => {
            this.flag = true
          }, 1500)
        });
      } else {
        if (value == 0) {
          sortMove({
            api: "plugin.sec.AdminSec.sortMove",
            moveId: this.tableData[value].id,
            moveId1: this.previousId,
          }).then((res) => {
            if (res.data.code == "200") {
              console.log(res);
              this.$message({
                type: "success",
                message: this.$t("seckill.cg"),
                offset: 100,
              });
            }
            this.labelIndexs();
          }).finally(() => {
            setTimeout(() => {
              this.flag = true
            }, 1500)
          });
        } else {
          sortMove({
            api: "plugin.sec.AdminSec.sortMove",
            moveId: this.tableData[value - 1].id,
            moveId1: this.tableData[value].id,
          }).then((res) => {
            if (res.data.code == "200") {
              console.log(res);
              this.$message({
                type: "success",
                message: this.$t("seckill.cg"),
                offset: 100,
              });
            }
            this.labelIndexs();
          }).finally(() => {
            setTimeout(() => {
              this.flag = true
            }, 1500)
          });
        }
      }
    },

    // 置顶
    placedTop(value) {
      if (!this.flag) return
      this.flag = false
      labelTop({
        api: "plugin.sec.AdminSec.top",
        id: value.id,
      }).then((res) => {
        console.log(res);
        if (res.data.code == "200") {
          this.$message({
            type: "success",
            message: this.$t("seckill.cg"),
            offset: 100,
          });
        }
        this.labelIndexs();
      }).finally(() => {
        setTimeout(() => {
          this.flag = true
        }, 1500)
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
        this.$t("seckill.seckillLabel.scqr"),
        this.$t("seckill.ts"),
        {
          confirmButtonText: this.$t("seckill.okk"),
          cancelButtonText: this.$t("seckill.ccel"),
          type: "warning",
        }
      )
        .then(() => {
          if (!this.flag) return
          this.flag = false
          delLabel({
            api: "plugin.sec.AdminSec.delLabel",
            id: value.id,
          }).then((res) => {
            console.log(res);
            if (res.data.code == "200") {
              // this.labelIndexs()
              this.isFillList();
              this.$message({
                type: "success",
                message: this.$t("zdata.sccg"),
                offset: 100,
              });
            }
          }).finally(() => {
            setTimeout(() => {
              this.flag = true
            }, 1500)
          });
        })
        .catch(() => {
          setTimeout(() => {
            this.flag = true
          }, 1500)
          // this.$message({
          //     type: 'info',
          //     message: '已取消删除',
          //     offset: 100
          // });
        });
    },

    isFillList() {
      let totalPage = Math.ceil((this.total - 1) / this.pageSize);
      // let totalPage = this.orderList.length
      let dictionaryNum =
        this.dictionaryNum > totalPage ? totalPage : this.dictionaryNum;
      this.dictionaryNum = dictionaryNum < 1 ? 1 : dictionaryNum;
      console.log("this.dictionaryNum", this.dictionaryNum, this.pageSize);
      this.labelIndexs().then(() => {
        this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1;
        this.current_num =
          this.tableData.length === this.pageSize
            ? this.dictionaryNum * this.pageSize
            : this.total;
        this.loading = false;
      }); //数据初始化方法
    },
    // 添加商品
    addGoods(value) {
      this.$router.push({
        path: "/plug_ins/seckill/addGoods",
        query: {
          id: value.id,
        },
      });
    },

    // 商品列表
    goodsList(value) {
      this.$router.push({
        path: "/plug_ins/seckill/goodsList",
        query: {
          id: value.id,
        },
      });
    },

    // 弹框方法
    dialogShow(value) {
      if (value) {
        this.title = this.$t("seckill.bjbq");
        this.id = value.id;
        this.ruleForm.name = value.name;
        this.ruleForm.title = value.title;
      } else {
        this.title = this.$t("seckill.tjbq");
        this.id = null;
        this.ruleForm.name = "";
        this.ruleForm.title = "";
      }
      this.dialogVisible = true;
    },

    handleClose(done) {
      this.dialogVisible = false;
      this.$refs["ruleForm"].clearValidate();
    },

    // 新增标签
    determine(formName) {
      if (this.ruleForm.name.length > 4) {
        return this.$message({
          message: this.$t("seckill.seckillLabel.buncg4"),
          type: "error",
          offset: 100,
        });
      }
      if (this.ruleForm.title.length > 6) {
        return this.$message({
          message: this.$t("seckill.seckillLabel.buncg6"),
          type: "error",
          offset: 100,
        });
      }
      this.$refs[formName].validate(async (valid) => {
        if (valid) {
          try {
            if (this.ruleForm.name.length > 20) {
              this.$message({
                message: this.$t("seckill.seckillLabel.bqmcdy"),
                type: "error",
                offset: 100,
              });
              return;
            }
            if (!this.flag) return
            this.flag = false

            addLabel({
              api: "plugin.sec.AdminSec.addLabel",
              title: this.ruleForm.title,
              name: this.ruleForm.name,
              id: this.id ? this.id : null,
            }).then((res) => {
              console.log(res);
              if (res.data.code == "200") {
                this.$message({
                  message:
                    this.title == "编辑标签"
                      ? this.$t("zdata.bjcg")
                      : this.$t("zdata.tjcg"),
                  type: "success",
                  offset: 100,
                });
                this.dialogVisible = false;
                this.labelIndexs();
              }
            }).finally(() => {
              setTimeout(() => {
                this.flag = true
              }, 1500)
            });
          } catch (error) { }
        } else {
        }
      });
    },

    //选择一页多少条
    handleSizeChange(e) {
      this.loading = true;
      console.log(e);
      this.pageSize = e;
      this.labelIndexs().then(() => {
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
      if (this.currpage !== 1) {
        this.previousId = this.tableData[this.tableData.length - 1].id;
      }
      this.labelIndexs().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total;
        this.loading = false;
      });
    },
  },
};
