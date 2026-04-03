import { index, setPaymentSwitch } from "@/api/mall/payManagement";
import { mixinstest } from "@/mixins/index";
import { getStorage } from "@/utils/storage";
import ErrorImg from '@/assets/images/default_picture.png'

export default {
  name: "payList",

  mixins: [mixinstest],

  data() {
    return {
      language:"",
      tableData: [],
      loading: true,
      // table高度
      tableHeight: null,
      // 服务器地址
      sevUrl: process.env.VUE_APP_BASE_API.replace("gw", ""),
    };
  },

  created() {
    this.language = this.getCookit()
    if (this.$route.params.pageSize) {
      this.pagination.page = this.$route.params.dictionaryNum;
      this.dictionaryNum = this.$route.params.dictionaryNum;
      this.pageSize = this.$route.params.pageSize;
    }
    this.indexs();
  },

  mounted() {
    this.$nextTick(function () {
      this.getHeight();
    });
    window.addEventListener("resize", this.getHeight(), false);
  },

  beforeRouteLeave(to, from, next) {
    if (to.name == "parameterModify") {
      from.meta.keepAlive = true;
    } else {
      from.meta.keepAlive = false;
    }
    next();
  },

  methods: {
    // 获取cookiet
    getCookit(){
      let myCookie = document.cookie.split(';').map(item=>{
         let arr = item.split('=')
         return {name:arr[0],value:arr[1]}
       })
       let strCookit = ''
       myCookie.forEach(item=>{
         if(item.name.indexOf('language')!==-1){
           strCookit = item.value
         }
       })
       return strCookit
    },
    // 图片错误处理
    handleErrorImg(e){
      console.log('图片报错了',e.target.src);
      e.target.src=ErrorImg
    },
    getHeight() {
      this.tableHeight =
        this.$refs.tableFather.clientHeight - this.$refs.pageBox.clientHeight;
    },

    async indexs() {
      let { entries } = Object;
      let data = {
        api: "admin.payment.index",
        pageNo: this.dictionaryNum,
        pageSize: this.pageSize,
      };
      let formData = new FormData();
      for (let [key, value] of entries(data)) {
        formData.append(key, value);
      }
      const res = await index(formData);
      console.log(res);
      this.total = res.data.data.total;
      this.tableData = res.data.data.list;
      this.loading = false;
    },

    // 改变开启状态
    switchs(value) {
      let { entries } = Object;
      let data = {
        api: "admin.payment.setPaymentSwitch",
        id: value.id,
      };
      let formData = new FormData();
      for (let [key, value] of entries(data)) {
        formData.append(key, value);
      }
      setPaymentSwitch(formData).then((res) => {
        if (res.data.code == "200") {
          this.$message({
            type: "success",
            message: this.$t("payManagement.cg"),
            offset: 100,
          });
          this.indexs();
        }
      });
    },

    settingDefaultPaytype(value) {
      let { entries } = Object;
      let data = {
        api: "admin.payment.settingDefaultPaytype",
        id: value.id,
      };
      let formData = new FormData();
      for (let [key, value] of entries(data)) {
        formData.append(key, value);
      }
      setPaymentSwitch(formData).then((res) => {
        if (res.data.code == "200") {
          this.$message({
            type: "success",
            message: this.$t("payManagement.cg"),
            offset: 100,
          });
          this.indexs();
        }
      });
    },

    // 参数修改
    parameter(value) {
      this.$router.push({
        path: "/mall/payManagement/parameterModify",
        query: {
          id: value.id,
          dictionaryNum: this.dictionaryNum,
          pageSize: this.pageSize,
        },
      });
    },

    // 编辑Icon图标
    handleEditIcon(value) {
      this.$router.push({
        path: "/mall/payManagement/editIcon",
        query: {
          id: value.id,
          dictionaryNum: this.dictionaryNum,
          logo: value.logo,
          pageSize: this.pageSize,
          name: value.name,
          description:value.description
        },
      });
    },

    //选择一页多少条
    handleSizeChange(e) {
      this.loading = true;
      console.log(e);
      // this.current_num = e
      this.pageSize = e;
      this.indexs().then(() => {
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
      this.indexs().then(() => {
        this.current_num =
          this.tableData.length === this.pageSize
            ? e * this.pageSize
            : this.total;
        this.loading = false;
      });
    },
  },
};
