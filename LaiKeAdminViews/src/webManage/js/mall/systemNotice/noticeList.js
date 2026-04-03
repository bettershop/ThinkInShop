import { index, read } from "@/api/mall/systemNotice/notice";

export default {
  name: "noticeList",

  //初始化数据
  data() {
    return {
      language:"",
      noticeList: {},
    };
  },
  //组装模板
  created() {
    this.language = this.getCookit()

    this.loadData();

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
    async loadData() {
      let { entries } = Object;
      let data = {
        api: "saas.sysNotice.getSysNoticeInfo",
        isStore:1,
        pageNo: 1,
        pageSize: 999,
      };
      let formData = new FormData();
      for (let [key, value] of entries(data)) {
        formData.append(key, value);
      }
      const res = await index(formData).then((data) => {
        this.noticeList = data.data.data.list;
      });
    },
    detail(id) {
      let { entries } = Object;
      let data = {
        api: "saas.sysNotice.readSysNotice",
        ids: id,
      };
      let formData = new FormData();
      for (let [key, value] of entries(data)) {
        formData.append(key, value);
      }
      read(formData).then((res) => {
        console.log(res);
        if (res.data.code == 200) {
          this.$router.push({
            path: "/mall/systemNotice/noticeDetail",
            query: {
              id: id,
            },
          });
        }
      });
    },
  },
};
