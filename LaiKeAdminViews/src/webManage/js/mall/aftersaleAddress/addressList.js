import { index, del } from "@/api/mall/aftersaleAddress/aftersaleAddress";
import pageData from "@/api/constant/page";
import { isEmpty } from "element-ui/src/utils/util";
import { mixinstest } from "@/mixins/index";
import { getStorage } from "@/utils/storage";
import { getButton } from "@/api/layout/information";

export default {
  name: "Aftersaleaddress",
  mixins: [mixinstest],
  //初始化数据
  data() {
    return {
      page: pageData.data(),
      // table高度
      tableHeight: null,

    };
  },
  //组装模板
  created() {
    if (this.$route.params.pageSize) {
      this.pagination.page = this.$route.params.dictionaryNum;
      this.dictionaryNum = this.$route.params.dictionaryNum;
      this.pageSize = this.$route.params.pageSize;
    }
    this.loadData();
  },

  mounted() {
    this.$nextTick(function () {
      this.getHeight();
    });
    window.addEventListener("resize", this.getHeight(), false);
  },

  methods: {
    getHeight() {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight;
      },
    async loadData() {
      await this.getAddress().then();
    },
    // 获取品牌信息
    async getAddress() {
      let { entries } = Object;
      let data = {
        api: "admin.system.getAddressInfo",
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
        name: this.page.inputInfo.name,
        type: 2,
      };
      let formData = new FormData();
      for (let [key, value] of entries(data)) {
        formData.append(key, value);
      }
      const res = await index(formData);
      this.total = res.data.data.total;
      this.page.tableData = res.data.data.list;
      this.page.loading = false;
      if (res.data.data.total < 10) {
        this.current_num = this.total;
      }
      if (res.data.data.total > 5) {
        this.page.showPagebox = true;
      }
      if (this.total < this.current_num) {
        this.current_num = this.total;
      }
    },

    // 重置
    reset() {
      this.page.inputInfo.name = "";
    },

    // 查询
    demand() {
      this.currpage = 1;
      this.current_num = 10;
      this.page.showPagebox = false;
      this.page.loading = true;
      this.dictionaryNum = 1;
      this.loadData().then(() => {
        this.page.loading = false;
        if (this.page.tableData.length > 5) {
          this.page.showPagebox = true;
        }
      });
    },

    //选择一页多少条
    handleSizeChange(e) {
      this.page.loading = true;
      this.pageSize = e;
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
      console.log('128128128',e);
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

    // 设置默认
    async Default(id) {
      let { entries } = Object;
      let data = {
        api: "admin.system.setDefaultAddress",
        id: id,
      };
      let formData = new FormData();
      for (let [key, value] of entries(data)) {
        formData.append(key, value);
      }
      const res = await index(formData);
      if(res.data.code==200){
        this.$message({
          type: "success",
          message: this.$t("zdata.czcg"),
          offset:102,
        });
        this.$nextTick(() => {
          this.$refs.table.doLayout()
        })
      }
      //重新加载列表
      this.demand();
    },

    //添加/编辑
    Save(id) {
      let flag = "save";
      if (!isEmpty(id)) {
        flag = "edit";
      }
      this.$router.push({
        name: flag,
        params: { id: id },
        query: {
          dictionaryNum: this.dictionaryNum,
          pageSize: this.pageSize,
        },
      });
    },

    // 删除
    Delete(id) {
      this.$confirm(
        this.$t("aftersaleAddress.scts"),
        this.$t("aftersaleAddress.ts"),
        {
          confirmButtonText: this.$t("aftersaleAddress.okk"),
          cancelButtonText: this.$t("aftersaleAddress.ccel"),
          type: "warning",
        }
      )
        .then(() => {
          let { entries } = Object;
          let data = {
            api: "admin.system.delAddress",
            type: 2,
            id: id,
          };
          let formData = new FormData();
          for (let [key, value] of entries(data)) {
            formData.append(key, value);
          }
          del(formData).then((res) => {
            this.isFillList();
            this.$message({
              type: "success",
              message: this.$t("zdata.sccg"),
              offset:102
            });
          });
        })
        .catch(() => {
          // this.$message({
          //   type: 'info',
          //   message: '已取消删除'
          // })
        });
    },
    isFillList () {
      let totalPage = Math.ceil((this.total - 1) / this.pageSize)
      let dictionaryNum =this.dictionaryNum > totalPage ? totalPage : this.dictionaryNum
      this.dictionaryNum = dictionaryNum < 1 ? 1 : dictionaryNum
      this.getAddress() //数据初始化方法
    },
  },
};
