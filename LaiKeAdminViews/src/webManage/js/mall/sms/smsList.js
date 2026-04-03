import { index, save, saveConfigInfo} from "@/api/mall/sms/smsManage";
import pageData from "@/api/constant/page";
import { isEmpty } from "element-ui/src/utils/util";
import { del } from "@/api/mall/aftersaleAddress/aftersaleAddress";
import { setStorage, getStorage } from "@/utils/storage";
import { mixinstest } from "@/mixins/index";
import { getButton } from "@/api/layout/information";

export default {
  name: "smsList",
  mixins: [mixinstest],
  //初始化数据
  data() {
    return {
      routerList:JSON.parse(sessionStorage.getItem('tabRouter')),
      tabPosition: "2",
      tabPositionKey: "smsTbl",
      flag: false,
      button_list: [],
      page: pageData.data(),
      mainForm: {},
      total: "",
      // table高度
      tableHeight: null,
    };
  },
  //组装模板
  created() {
    const routeTab = this.$route.query.tab;
    const storageTab = getStorage(this.tabPositionKey);
    const targetTab = !isEmpty(routeTab) ? routeTab : storageTab;
    if (!isEmpty(targetTab)) {
      this.tabPosition = `${targetTab}`;
    }
    this.loadData();
  },
  mounted() {
    const tabButtons = (this.$refs.tab_bun && this.$refs.tab_bun.$children) || [];
    const availableTabs = tabButtons.map((item) => item.label);
    if (availableTabs.length > 0 && !availableTabs.includes(this.tabPosition)) {
      this.tabPosition = availableTabs[0];
    }
    this.tbl()
    this.$nextTick( ()=> {
      this.getHeight()
    })
    window.addEventListener('resize', this.getHeight(), false)
  },

  watch: {
    tabPosition() {
      if (this.tabPosition == 1) {
        this.$router.currentRoute.matched[2].meta.title = "短信列表";
      } else if (this.tabPosition == 2) {
        this.$router.currentRoute.matched[2].meta.title = "国内短信模板";
      }  else if (this.tabPosition == 4) {
        this.$router.currentRoute.matched[2].meta.title = "国外短信模板";
      } else {
        this.$router.currentRoute.matched[2].meta.title = "核心设置";
      }
    },
  },
  methods: {
    // 获取table高度
    getHeight() {
      this.$nextTick(()=>{
        this.tableHeight = this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight
        console.log(this.tableHeight,'tableHeight',this.$refs.tableFather.clientHeight);
      })
    },
    rules() {
      this.mainForm.rules = {
        accessKeyId: [
          {
            required: true,
            message: this.$t("smsList.qsralyid"),
            trigger: "blur",
          },
        ],
        accessKeySecret: [
          {
            required: true,
            message: this.$t("smsList.qsralymy"),
            trigger: "blur",
          },
        ],
      };
    },

    async loadData() {
      // let tbl = getStorage(this.tabPositionKey, this.tabPosition);
      // if (!isEmpty(tbl)) {
      //   this.tabPosition = tbl;
      // }
      
      //默认短信列表
      if (this.tabPosition === "2"|| this.tabPosition === "4") {
        //短信模板
        let { entries } = Object;
        let data = {
          api: "admin.system.getSmsTemplateInfo",
          international:this.tabPosition === "2"?0:1,
        };
        let formData = new FormData();
        for (let [key, value] of entries(data)) {
          formData.append(key, value);
        }
          await index(formData).then((data) => {
          console.log(data.data);
          this.total = data.data.data.total;
          this.page.tableData = data.data.data.list;
          this.page.loading = false;
          if (data.data.data.total < 10) {
            this.current_num = this.total;
          }
          if (this.total < this.current_num) {
            this.current_num = this.total;
          }
        });
      } else if (this.tabPosition === "3") {
        //核心设置
        let { entries } = Object;
        let data = {
          api: "admin.system.getTemplateConfigInfo",
        };
        let formData = new FormData();
        for (let [key, value] of entries(data)) {
          formData.append(key, value);
        }
        const res = await index(formData).then((data) => {
          data = data.data.data.data;
          console.log(data);
          if (data == null) {
            this.mainForm = {
              accessKeyId: '',
              accessKeySecret: '',
              add_time: "",
              id: 1,
              signName: "",
              store_id: getStorage("laike_admin_userInfo").storeId,
            };
          } else {
            this.mainForm = data;
          }
          this.rules();
          this.$refs.ruleForm.resetFields();
        });
      }

    },
    tbl() {
      setStorage(this.tabPositionKey, this.tabPosition);
      this.page = pageData.data();
      //解决数据源发生变化不重新渲染表格的问题
      this.flag = !this.flag;
      this.loadData();
      this.$nextTick( ()=> {
        this.getHeight()
      })
      window.addEventListener('resize', this.getHeight(), false)
    },
    // 查询
    demand() {
      this.currpage = 1;
      this.current_num = 10;
      this.showPagebox = false;
      this.page.loading = true;
      this.dictionaryNum = 1;
      this.loadData().then(() => {
        this.page.loading = false;
        if (this.page.tableData.length > 5) {
          this.showPagebox = true;
        }
      });
    },
    //选择一页多少条
    handleSizeChange(e) {
      this.page.loading = true;
      this.page.pageSize = e;
      this.loadData().then(() => {
        this.currpage = (this.dictionaryNum - 1) * this.pageSize + 1;
        this.current_num =
          this.page.tableData.length === this.pageSize
            ? this.dictionaryNum * this.pageSize
            : this.total;
        this.page.loading = false;
      });
    },
    //点击上一页，下一页
    handleCurrentChange(e) {
      this.page.loading = true;
      this.dictionaryNum = e;
      this.currpage = (e - 1) * this.pageSize + 1;
      this.loadData().then(() => {
        this.current_num =
          this.page.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total;
        this.page.loading = false;
      });
    },
    Save(id) {
      // let flag = "smsSave";
      // if (!isEmpty(id)) {
      //   flag = "smsEdit";
      // }
      // if (this.tabPosition === "2") {
        let flag = "smsTemplateSave";
        if (!isEmpty(id)) {
          flag = "smsTemplateEdit";
        }
      // }
      this.$router.push({
        name: flag,
        query: {
          id: id,
          shaco: 1,
          international:this.tabPosition === "2"?0:1,
        },
      });
    },
    Add(id) {
      // let flag = "smsSave";
      // // 默认 smsSave  如果没有 id 则是新增  有id 则是编辑
      // if (!isEmpty(id)) {
      //   flag = "smsEdit";
      // }
      // // 国内短信模板  如果没有 id 则是新增  有id 则是编辑  
      // if (this.tabPosition === "2") {
      let flag = "smsTemplateSave";
        if (!isEmpty(id)) {
          flag = "smsTemplateEdit";
        }
      // }
      this.$router.push({
        name: flag,
        query: {
          id: id,
          international:this.tabPosition === "2"?0:1,
        },
      });
    },
    Delete(id) {
      let { entries } = Object;
      let data = {
        api: "admin.system.delMessage",
        id: id,
      };

      
      this.$confirm(this.$t("smsList.scqr"), this.$t("smsList.ts"), {
        confirmButtonText: this.$t("smsList.okk"),
        cancelButtonText: this.$t("smsList.ccel"),
        type: "warning",
      })
        .then(() => {
          let formData = new FormData();
          for (let [key, value] of entries(data)) {
            formData.append(key, value);
          }
          del(formData).then((res) => {
            console.log(res);
            if ((res.data.code ==200)) {
              // if (!isEmpty(res.data)) {
              this.$message({
                type: "success",
                message: this.$t("zdata.sccg"),
                offset: 102,
              });
              // }
              this.demand();
            }else{
              this.$message({
                type: "error",
                message: res.data.message,
                offset: 102,
              });
            }
          });
        })
        .catch(() => {
          // this.$message({
          //   type: 'info',
          //   message: '已取消删除'
          // })
        });
    },
    async saveConfig(formName) {
      this.$refs[formName].validate(async (valid) => {
        if (valid) {
          try {
            let { entries } = Object;
            let data = {
              api: "admin.system.addTemplateConfig",
              key: this.mainForm.accessKeyId,
              secret: this.mainForm.accessKeySecret,
            };
            let formData = new FormData();
            for (let [key, value] of entries(data)) {
              formData.append(key, value);
            }
            saveConfigInfo(formData).then((data) => {
              if (!isEmpty(data)) {
                this.$message({
                  message: this.$t("zdata.baccg"),
                  type: "success",
                  offset: 102,
                });
              }
            });
          } catch (e) {
            this.$message({
              message: e.message,
              type: "error",
              showClose: true,
            });
          }
        } else {
          return false;
        }
      });
    },
  },
};
