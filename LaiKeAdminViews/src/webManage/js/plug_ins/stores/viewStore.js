import { getMchInfo } from '@/api/plug_ins/stores'
import ErrorImg from '@/assets/images/default_picture.png'
export default {
  name: 'viewStore',

  data () {
    return {
      storeInfo: null,
      srcList: []
    }
  },

  created () {
    this.getMchInfos()
  },

  beforeRouteLeave (to, from, next) {
    if (to.name == 'auditList' || to.name == 'store') {
      to.params.dictionaryNum = this.$route.query.dictionaryNum
      to.params.pageSize = this.$route.query.pageSize
    }
    next()
  },

  methods: {
    // 图片错误处理
    handleErrorImg (e) {
      console.log('图片报错了', e.target.src)
      e.target.src = ErrorImg
    },

    async getMchInfos () {
      const res = await getMchInfo({
        api: 'mch.Admin.Mch.GetMchInfo',
        id: this.$route.query.id
      })

      console.log('res', res)
      this.storeInfo = res.data.data.list[0]
      this.srcList = this.storeInfo.business_license
      // if(this.storeInfo.business_license) {
      //     this.srcList.push(this.storeInfo.business_license)
      // }
    }
  }
}
