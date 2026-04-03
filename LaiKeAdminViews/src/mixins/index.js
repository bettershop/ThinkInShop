import { dictionaryList } from '@/api/Platform/numerical'
export const mixinstest = {
  data() {
    return {
      total: 10,
      pagesizes: [10, 25, 50, 100],
      pagination: {
        page: 1,
        pagesize: 10,
      },
      currpage: 1,
      current_num: 10,
      dictionaryNum: 1,
      pageSize: 10,
      showPagebox: true,
    }
  },
  created() {
    dictionaryList({
      api: 'saas.dic.getDictionaryInfo',
      key: '分页',
      status: 1,
    }).then(res => {
      if (res.data.data.list.length !== 0) {
        const list = []
        res.data.data.list.map(item => {
          if (item.status == 1) {
            list.push(item)
          }
        })
        let pagesize = list.map(item => {
          return parseInt(item.text)
        })
        pagesize = pagesize.sort(function (a, b) { return a - b })
        this.pagesizes = pagesize
      }

    })
  },
  methods: {
    isIOS() {
      // navigator.userAgent 包含设备和浏览器信息
      const userAgent = window.navigator.userAgent.toLowerCase();
      console.log('设备信息', userAgent)
      // 匹配 iPhone、iPad、iPod（覆盖 iOS 全设备）
      if (!/windows/.test(userAgent)) {

      }

      return !/windows/.test(userAgent);
    },
  },
  computed: {
  },
  watch: {
  }
}