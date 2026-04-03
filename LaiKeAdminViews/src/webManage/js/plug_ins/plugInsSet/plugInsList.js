import { plugList, plugSwitch, plugSaveSort } from "@/api/mall/plugInsSet";
import { mixinstest } from "@/mixins/index";
import { getStorage } from "@/utils/storage";
import { getButton } from "@/api/layout/information";

export default {
  name: "plugInsList",
  mixins: [mixinstest],
  beforeDestroy() {
    window.removeEventListener("resize", this.handleResize, false);
  },
  data() {
    return {
      tableData: [],
      loading: true,
      // table高度
      tableHeight: null,
      laike_admin_userInfo: "",
      isShow: true,
      button_list: [],
    };
  },

  created() {
    if (this.$route.params.pageSize) {
      this.pagination.page = this.$route.params.dictionaryNum;
      this.dictionaryNum = this.$route.params.dictionaryNum;
      this.pageSize = this.$route.params.pageSize;
    }
    this.laike_admin_userInfo = getStorage("laike_admin_userInfo").mchId;
    if (getStorage("laike_admin_userInfo").mchId == 0) {
      this.$message({
        type: "error",
        message: this.$t("plugInsSet.plugInsList.qtjdp"),
        offset: 100,
      });
      this.$router.push("/mall/fastBoot/index");
    }
    this.getPlugList();
    // 获取按钮权限
    this.getButtons();
  },

  mounted() {
    this.$nextTick(() => {
      this.getHeight();
      // 初次渲染后强制重新计算列宽，避免出现滚动条后表头/内容对不齐
      this.$refs.table && this.$refs.table.doLayout();
    });
    // 监听窗口变化（注意：这里要传函数引用，不要直接执行）
    window.addEventListener("resize", this.handleResize, false);
  },

  methods: {
    handleResize() {
      this.getHeight();
      this.$nextTick(() => {
        this.$refs.table && this.$refs.table.doLayout();
      });
    },

    async getButtons() {
      let route = getStorage("route");
      route.forEach((item) => {
        if (item.path == "plugInsSet") {
          return (this.menuId = item.id);
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
      console.log("按钮权限xxxx", this.button_list);
    },
    // 获取table高度
    getHeight() {
      this.tableHeight = this.$refs.tableFather.clientHeight;
    },
    async getPlugList() {
      this.loading = true;
      let { entries } = Object;
      let data = {
        api: "admin.plugin.manage.index",
      };
      let formData = new FormData();
      for (let [key, value] of entries(data)) {
        formData.append(key, value);
      }
      const res = await plugList(formData);
      this.tableData = res.data.data.list;
      this.loading = false;
      this.$nextTick(() => {
        // 数据变化后重新计算列宽，避免滚动条出现导致对不齐
        this.$refs.table && this.$refs.table.doLayout();
      });
      console.log("res", res);
    },
    // 是否显示
    switchs(row) {
      let { entries } = Object;
      let data = {
        api: "admin.plugin.manage.pluginSwitch",
        id: row.id,
      };
      let formData = new FormData();
      for (let [key, value] of entries(data)) {
        formData.append(key, value);
      }
      plugSwitch(formData).then((res) => {
        if (res.data.code == "200") {
          this.getPlugList();
          this.$message({
            type: "success",
            message: this.$t("zdata.czcg"),
            offset: 100,
          });
        }
      });
    },
    // 编辑
    Edit(row) {
      console.log("row", row);

      this.$router.push({
        path: "/plug_ins/plugInsSet/editPlugIns",
        query: {
          id: row.id,
          plugin_name: row.plugin_name,
          content: row.content,
          status: row.status,
          isMchPlugin: row.isMchPlugin,
        },
      });
    },
    // 配置
    Collocate(row) {
      console.log("row", row.plugin_code);
      switch (row.plugin_code) {
        case "go_group":
          this.$router.push({
            path: "/plug_ins/plugInsSet/groupSetInfo",
            query: {
              pluginCode: row.plugin_code,
            },
          });
          break;
        case "auction":
          this.$router.push({
            path: "/plug_ins/plugInsSet/auctionSetInfo",
            query: {
              pluginCode: row.plugin_code,
            },
          });
          break;
        case "integral":
          this.$router.push({
            path: "/plug_ins/plugInsSet/integralSetInfo",
            query: {
              pluginCode: row.plugin_code,
            },
          });
          break;
        case "seconds":
          this.$router.push({
            path: "/plug_ins/plugInsSet/seckillSetInfo",
            query: {
              pluginCode: row.plugin_code,
            },
          });
          break;
        case "distribution":
          this.$router.push({
            path: "/plug_ins/plugInsSet/distributionSetInfo",
            query: {
              pluginCode: row.plugin_code,
            },
          });
          break;
        case "coupon":
          this.$router.push({
            path: "/plug_ins/plugInsSet/couponsSetInfo",
            query: {
              pluginCode: row.plugin_code,
            },
          });
          break;
        case "presell":
          this.$router.push({
            path: "/plug_ins/plugInsSet/preSaleSetInfo",
            query: {
              pluginCode: row.plugin_code,
            },
          });
          break;
        case "member":
          this.$router.push({
            path: "/plug_ins/plugInsSet/memberSetInfo",
            query: {
              pluginCode: row.plugin_code,
            },
          });
          break;
        case "mch":
          this.$router.push({
            path: "/plug_ins/plugInsSet/storeSetInfo",
            query: {
              pluginCode: row.plugin_code,
            },
          });
          break;
        case "flashsale":
          this.$router.push({
            path: "/plug_ins/plugInsSet/discountSetInfo",
            query: {
              pluginCode: row.plugin_code,
            },
          });
          break;
        case "living":
          this.$router.push({
            path: "/plug_ins/plugInsSet/liveSetInfo",
            query: {
              pluginCode: row.plugin_code,
            },
          });
          break;
        case "advertising":
          this.$router.push({
            path: "/plug_ins/plugInsSet/forum",
            query: {
              pluginCode: row.plugin_code,
            },
          });
          break;
        case "PC":
          this.$router.push({
            path: "/mall/systemManagement/basicConfigurationCommon/PCSetter",
            query: {
              pluginCode: row.plugin_code,
            },
          });
          break;
      }
    },

    // 保存插件排序
    async saveSort(row) {
      let { entries } = Object;
      let data = {
        api: "admin.plugin.manage.saveSort",
        pluginId: row.id,
        pluginSort: row.plugin_sort,
      };
      let formData = new FormData();
      for (let [key, value] of entries(data)) {
        formData.append(key, value);
      }
      const res = await plugSaveSort(formData);
      if (res.data.code == "200") {
        this.getPlugList();
        this.$message({
          type: "success",
          message: this.$t("plugInsSet.plugInsList.pxbcg"),
          offset: 100,
        });
      } else {
        this.$message({
          type: "error",
          message: res.data.msg || this.$t("plugInsSet.plugInsList.bcsb"),
          offset: 100,
        });
      }
    },

    dateFormat(fmt, date) {
      let ret = "";
      date = new Date(date);
      const opt = {
        "Y+": date.getFullYear().toString(), // 年
        "m+": (date.getMonth() + 1).toString(), // 月
        "d+": date.getDate().toString(), // 日
        "H+": date.getHours().toString(), // 时
        "M+": date.getMinutes().toString(), // 分
        "S+": date.getSeconds().toString(), // 秒
        // 有其他格式化字符需求可以继续添加，必须转化成字符串
      };
      for (let k in opt) {
        ret = new RegExp("(" + k + ")").exec(fmt);
        if (ret) {
          fmt = fmt.replace(
            ret[1],
            ret[1].length == 1 ? opt[k] : opt[k].padStart(ret[1].length, "0")
          );
        }
      }
      return fmt;
    },
  },
};
