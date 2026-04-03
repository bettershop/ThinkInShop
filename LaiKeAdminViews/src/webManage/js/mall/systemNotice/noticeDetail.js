import {index} from '@/api/mall/systemNotice/notice'

export default {
  name: 'noticeDetail',

  //初始化数据
  data() {
    return {
      notice: {},
    }
  },
  //组装模板
  created() {
    this.loadData()
  },

  methods: {
    async loadData() {
      console.log(this.$route.query.id)
      let { entries } = Object;
      let data = {
        api: 'saas.sysNotice.getSysNoticeInfo',
        id: this.$route.query.id,
      };
      let formData = new FormData();
      for (let [key, value] of entries(data)) {
        formData.append(key, value);
      }
      const res = await index(formData).then(data => {
        this.notice = data.data.data.list[0];
      });
    }


  }

}
